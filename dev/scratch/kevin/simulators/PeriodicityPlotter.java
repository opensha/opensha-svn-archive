package scratch.kevin.simulators;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.commons.data.function.DiscretizedFunc;
import org.opensha.commons.data.function.HistogramFunction;
import org.opensha.commons.gui.plot.PlotLineType;
import org.opensha.commons.gui.plot.PlotSymbol;
import org.opensha.sha.gui.infoTools.GraphiWindowAPI_Impl;
import org.opensha.sha.gui.infoTools.PlotCurveCharacterstics;
import org.opensha.sha.simulators.eqsim_v04.EQSIM_Event;
import org.opensha.sha.simulators.eqsim_v04.General_EQSIM_Tools;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class PeriodicityPlotter {

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
		ArrayList<EQSIM_Event> events = tools.getEventsList();
		
		List<RuptureIdentifier> rupIdens = Lists.newArrayList();
		List<String> rupIdenNames = Lists.newArrayList();
		List<Color> colors = Lists.newArrayList();
		rupIdens.add(new ElementMagRangeDescription(
				ElementMagRangeDescription.SAF_MOJAVE_ELEMENT_ID, 7d, 10d));
		rupIdenNames.add("SAF Mojave 7+");
		colors.add(Color.BLACK);
		rupIdens.add(new ElementMagRangeDescription(
				ElementMagRangeDescription.SAF_COACHELLA_ELEMENT_ID, 7d, 10d));
		rupIdenNames.add("SAF Coachella 7+");
		colors.add(Color.BLUE);
		rupIdens.add(new ElementMagRangeDescription(
				ElementMagRangeDescription.SAF_CARRIZO_ELEMENT_ID, 7d, 10d));
		rupIdenNames.add("SAF Carrizo 7+");
		colors.add(Color.RED);
		rupIdens.add(new ElementMagRangeDescription(
				ElementMagRangeDescription.GARLOCK_WEST_ELEMENT_ID, 7d, 10d));
		rupIdenNames.add("Garlock 7+");
		colors.add(Color.GREEN);
		rupIdens.add(new ElementMagRangeDescription(
				ElementMagRangeDescription.SAN_JACINTO__ELEMENT_ID, 7d, 10d));
		rupIdenNames.add("San Jacinto 7+");
		colors.add(Color.CYAN);
		
		Color duplicateColor = Color.ORANGE;
		
		HashSet<Integer> duplicates = new HashSet<Integer>();
		Map<Integer, EQSIM_Event> eventsMap = Maps.newHashMap();
		List<HashSet<Integer>> idsList = Lists.newArrayList();
		
		for (int i=0; i<rupIdens.size(); i++) {
			RuptureIdentifier rupIden = rupIdens.get(i);
			String name = rupIdenNames.get(i);
			
			HistogramFunction hist = new HistogramFunction(0d, 300, 10d);
			
			HashSet<Integer> ids = new HashSet<Integer>();
			idsList.add(ids);
			
			double prevTime = -1;
			
			for (EQSIM_Event match : rupIden.getMatches(events)) {
				double time = match.getTimeInYears();
				Integer id = match.getID();
				if (eventsMap.containsKey(id))
					duplicates.add(id);
				else
					eventsMap.put(id, match);
				ids.add(match.getID());
				
				if (prevTime > 0) {
					double diff = time - prevTime;
					int ind = hist.getXIndex(diff);
					if (ind >= 0)
						hist.add(ind, 1d);
				}
				prevTime = time;
			}
			
			ArrayList<DiscretizedFunc> funcs = Lists.newArrayList();
			funcs.add(hist);
			ArrayList<PlotCurveCharacterstics> chars = Lists.newArrayList();
			chars.add(new PlotCurveCharacterstics(PlotLineType.HISTOGRAM, 5f, colors.get(i)));
			new GraphiWindowAPI_Impl(funcs, name+" Periodicity", chars);
		}
		
		ArrayList<ArbitrarilyDiscretizedFunc> funcs = Lists.newArrayList();
		ArrayList<PlotCurveCharacterstics> chars = Lists.newArrayList();
		
		ArbitrarilyDiscretizedFunc duplicateFunc = new ArbitrarilyDiscretizedFunc();
		
		for (int i=0; i<rupIdens.size(); i++) {
			ArbitrarilyDiscretizedFunc overlayFunc = new ArbitrarilyDiscretizedFunc();
			HashSet<Integer> ids = idsList.get(i);
			for (Integer id : ids) {
				EQSIM_Event event = eventsMap.get(id);
				if (duplicates.contains(id))
					duplicates.add(id);
				else
					overlayFunc.set(event.getTimeInYears(), event.getMagnitude());
			}
			overlayFunc.setName(rupIdenNames.get(i)+" events"); 
			funcs.add(overlayFunc);
			chars.add(new PlotCurveCharacterstics(PlotLineType.HISTOGRAM, 1f, colors.get(i)));
		}
		
		for (Integer id : duplicates) {
			EQSIM_Event event = eventsMap.get(id);
			duplicateFunc.set(event.getTimeInYears(), event.getMagnitude());
		}
		duplicateFunc.setName("Duplicates");
		funcs.add(duplicateFunc);
		chars.add(new PlotCurveCharacterstics(PlotLineType.HISTOGRAM, 3f, duplicateColor));
		
		new GraphiWindowAPI_Impl(funcs, "Event Times", chars);
	}

}
