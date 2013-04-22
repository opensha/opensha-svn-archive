package scratch.kevin.simulators;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.opensha.commons.data.function.DiscretizedFunc;
import org.opensha.commons.data.function.EvenlyDiscretizedFunc;
import org.opensha.commons.gui.plot.PlotLineType;
import org.opensha.sha.gui.infoTools.GraphiWindowAPI_Impl;
import org.opensha.sha.gui.infoTools.PlotCurveCharacterstics;
import org.opensha.sha.simulators.eqsim_v04.EQSIM_Event;
import org.opensha.sha.simulators.eqsim_v04.General_EQSIM_Tools;

import com.google.common.collect.Lists;

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
		
		MagRangeRuptureIdentifier sevenPlusIden = new MagRangeRuptureIdentifier(7, 10);
		
		// we don't want aftershock sequences - crude approximation, find out no other M7's within 5 years before
		// -5 here because we'er actually filtering out foreshocks
		QuietPeriodIdenMatcher quietIden = new QuietPeriodIdenMatcher(sevenPlusIden, -5, 0, sevenPlusIden);
		
		List<EQSIM_Event> matches = quietIden.getMatches(events);
		
		System.out.println("# Matches: "+matches.size());
		
		int daysBefore = 100;
		
		EvenlyDiscretizedFunc beforeFunc = new EvenlyDiscretizedFunc(-100, daysBefore, 1d);
		
		int startInd = 0;
		
		for (int j=0; j<matches.size(); j++) {
			EQSIM_Event e = matches.get(j);
			if (j % 1000 == 0)
				System.out.println("Processing match "+j);
			double eventTime = e.getTimeInYears();
			double startTime = eventTime - (double)daysBefore * BatchPlotGen.DAYS_PER_YEAR;
			
			for (int i=startInd; i<events.size(); i++) {
				double oTime = events.get(i).getTimeInYears();
				if (oTime < startTime) {
					startInd = i;
					continue;
				} else if (oTime >= eventTime) {
					break;
				}
				int dayBin = (int)((oTime - startTime) / BatchPlotGen.DAYS_PER_YEAR);
				beforeFunc.add(dayBin, 1d);
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
