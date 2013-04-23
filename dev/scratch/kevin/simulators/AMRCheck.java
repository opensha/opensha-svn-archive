package scratch.kevin.simulators;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.opensha.commons.data.function.DiscretizedFunc;
import org.opensha.commons.data.function.EvenlyDiscretizedFunc;
import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.LocationUtils;
import org.opensha.commons.gui.plot.PlotLineType;
import org.opensha.sha.gui.infoTools.GraphiWindowAPI_Impl;
import org.opensha.sha.gui.infoTools.PlotCurveCharacterstics;
import org.opensha.sha.simulators.eqsim_v04.EQSIM_Event;
import org.opensha.sha.simulators.eqsim_v04.General_EQSIM_Tools;
import org.opensha.sha.simulators.eqsim_v04.RectangularElement;

import scratch.UCERF3.utils.IDPairing;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class AMRCheck {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		File dir = new File("/home/kevin/Simulators");
		File geomFile = new File(dir, "ALLCAL2_1-7-11_Geometry.dat");
		System.out.println("Loading geometry...");
		General_EQSIM_Tools tools = new General_EQSIM_Tools(geomFile);
//		File eventFile = new File(dir, "eqs.ALLCAL2_RSQSim_sigma0.5-5_b=0.015.barall");
		File eventFile = new File(dir, "eqs.ALLCAL2_RSQSim_sigma0.5-5_b=0.015.long.barall");
		System.out.println("Loading events...");
		tools.read_EQSIMv04_EventsFile(eventFile);
		List<EQSIM_Event> events = tools.getEventsList();
		System.out.println("Done loading events.");
		
		Map<IDPairing, Double> elemDistsMap = Maps.newHashMap();
		
		MagRangeRuptureIdentifier sevenPlusIden = new MagRangeRuptureIdentifier(7, 10);
		
		// we don't want aftershock sequences - crude approximation, find out no other M7's within 5 years before
		// -5 here because we'er actually filtering out foreshocks
		QuietPeriodIdenMatcher quietIden = new QuietPeriodIdenMatcher(sevenPlusIden, -5, 0, sevenPlusIden);
		
		List<EQSIM_Event> matches = quietIden.getMatches(events);
		
		System.out.println("# Matches: "+matches.size());
		
		int daysBefore = 100;
		double distThresh = 100d;
		
		EvenlyDiscretizedFunc beforeFunc = new EvenlyDiscretizedFunc(-100, daysBefore, 1d);
		Map<Integer, Location> centerLocsMap = Maps.newHashMap();
		for (RectangularElement elem : tools.getElementsList())
			centerLocsMap.put(elem.getID(), elem.getCenterLocation());
		
		int startInd = 0;
		
		for (int j=0; j<matches.size(); j++) {
			EQSIM_Event e = matches.get(j);
			if (j % 1000 == 0)
				System.out.println("Processing match "+j);
			double eventTime = e.getTimeInYears();
			double startTime = eventTime - (double)daysBefore * BatchPlotGen.DAYS_PER_YEAR;
			
			for (int i=startInd; i<events.size(); i++) {
				EQSIM_Event o = events.get(i);
				double oTime = o.getTimeInYears();
				if (oTime < startTime) {
					startInd = i;
					continue;
				} else if (oTime >= eventTime) {
					break;
				}
				// see if it's within the distance cutoff
				boolean within = false;
				outsideLoop:
				for (RectangularElement elem1 : e.getAllElements()) {
					int id1 = elem1.getID();
					for (RectangularElement elem2 : o.getAllElements()) {
						int id2 = elem2.getID();
						if (id1 == id2) {
							within = true;
							break outsideLoop;
						}
						IDPairing pair;
						if (id1 < id2)
							pair = new IDPairing(id1, id2);
						else
							pair = new IDPairing(id2, id1);
						Double dist = elemDistsMap.get(pair);
						if (dist == null) {
							dist = LocationUtils.horzDistanceFast(
									centerLocsMap.get(id1), centerLocsMap.get(id2));
							elemDistsMap.put(pair, dist);
						}
						if (dist <= distThresh) {
							within = true;
							break outsideLoop;
						}
					}
				}
				if (within) {
					int dayBin = (int)((oTime - startTime) / BatchPlotGen.DAYS_PER_YEAR);
					beforeFunc.add(dayBin, 1d);
				}
			}
		}
		beforeFunc.scale(1d/(double)matches.size());
		
		ArrayList<DiscretizedFunc> funcs = Lists.newArrayList();
		funcs.add(beforeFunc);
		ArrayList<PlotCurveCharacterstics> chars = Lists.newArrayList();
		chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 1f, Color.BLACK));
		
		GraphiWindowAPI_Impl gw = new GraphiWindowAPI_Impl(funcs, "# Events Per Day Before Mainshock", chars);
	}

}
