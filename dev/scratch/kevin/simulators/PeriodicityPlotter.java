package scratch.kevin.simulators;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.math.stat.StatUtils;
import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.commons.data.function.DiscretizedFunc;
import org.opensha.commons.data.function.EvenlyDiscretizedFunc;
import org.opensha.commons.data.function.HistogramFunction;
import org.opensha.commons.gui.plot.PlotLineType;
import org.opensha.commons.gui.plot.PlotSymbol;
import org.opensha.commons.mapping.gmt.elements.GMT_CPT_Files;
import org.opensha.commons.util.ExceptionUtils;
import org.opensha.commons.util.cpt.CPT;
import org.opensha.sha.gui.infoTools.GraphiWindowAPI_Impl;
import org.opensha.sha.gui.infoTools.PlotCurveCharacterstics;
import org.opensha.sha.simulators.eqsim_v04.EQSIM_Event;
import org.opensha.sha.simulators.eqsim_v04.General_EQSIM_Tools;

import com.google.common.base.Joiner;
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
		rupIdens.add(new MagRangeRuptureIdentifier(7d, 10d));
		rupIdenNames.add("All 7+");
		colors.add(Color.GRAY);
		
		Color duplicateColor = Color.ORANGE;
		
		HashSet<Integer> duplicates = new HashSet<Integer>();
		Map<Integer, EQSIM_Event> eventsMap = Maps.newHashMap();
		List<HashSet<Integer>> idsList = Lists.newArrayList();
		
		for (int i=0; i<rupIdens.size(); i++) {
			RuptureIdentifier rupIden = rupIdens.get(i);
			String name = rupIdenNames.get(i);
			
			HistogramFunction hist;
			if (rupIden instanceof ElementMagRangeDescription)
				hist = new HistogramFunction(0d, 100, 10d);
			else
				hist = new HistogramFunction(0d, 100, 1d);
			
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
			funcs.add(getCmlGreaterOrEqual(hist));
			ArrayList<PlotCurveCharacterstics> chars = Lists.newArrayList();
			chars.add(new PlotCurveCharacterstics(PlotLineType.HISTOGRAM, 5f, colors.get(i)));
			chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 1f, Color.BLUE));
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
		
		int coachellaIndex = 1;
		int carrizoIndex = 2;
		
		plotTimeBetweenIdens(events, rupIdens.get(coachellaIndex), rupIdenNames.get(coachellaIndex),
				rupIdens.get(carrizoIndex), rupIdenNames.get(carrizoIndex));
		plotTimeBetweenIdens(events, rupIdens.get(carrizoIndex), rupIdenNames.get(carrizoIndex),
				rupIdens.get(coachellaIndex), rupIdenNames.get(coachellaIndex));
		
//		double[] windowLengths = { 5d, 10d, 25d, 50d, 100d };
		double[] windowLengths = new double[30];
		for (int i=1; i<=windowLengths.length; i++)
			windowLengths[i-1] = 5d*(double)i;
		double xInc = 1d;
		plotSlidingWindowCounts(windowLengths, xInc, events, getOnlyElemMagDescriptions(rupIdens));
	}
	
	private static List<RuptureIdentifier> getOnlyElemMagDescriptions(List<RuptureIdentifier> rupIdens) {
		List<RuptureIdentifier> ret = Lists.newArrayList();
		
		for (RuptureIdentifier rupIden : rupIdens)
			if (rupIden instanceof ElementMagRangeDescription)
				ret.add(rupIden);
		
		return ret;
	}
	
	private static void plotTimeBetweenIdens(List<EQSIM_Event> events, RuptureIdentifier iden1, String name1,
			RuptureIdentifier iden2, String name2) {
		double maxX = 500d;
		HistogramFunction hist = new HistogramFunction(0d, 50, 10d);
		HistogramFunction absHist = new HistogramFunction(0d, 50, 10d);
		
		List<EQSIM_Event> matches1 = iden1.getMatches(events);
		List<EQSIM_Event> matches2 = iden2.getMatches(events);
//		HashSet<EQSIM_Event> matches1set = new HashSet<EQSIM_Event>(matches1);
//		HashSet<EQSIM_Event> matches2set = new HashSet<EQSIM_Event>(matches2);
		
		for (EQSIM_Event event1 : matches1) {
			double timeYears1 = event1.getTimeInYears();
			double waitingTime = -1;
			double absMin = Double.MAX_VALUE;
			for (EQSIM_Event event2 : matches2) {
				double timeYears2 = event2.getTimeInYears();
				double absWaitingTime = Math.abs(timeYears2 - timeYears1);
				if (absWaitingTime < absMin)
					absMin = absWaitingTime;
				if (timeYears2 < timeYears1)
					continue;
				waitingTime = timeYears2 - timeYears1;
				break;
			}
			
			if (waitingTime >= 0 && waitingTime <= maxX) {
				hist.add(waitingTime, 1d);
			}
			
			if (absMin >= 0 && absMin <= maxX) {
				absHist.add(absMin, 1d);
			}
		}
		
		String plotTitle = "Inter-event time from "+name1+" to "+name2;
		ArrayList<DiscretizedFunc> funcs = Lists.newArrayList();
		funcs.add(hist);
		funcs.add(getCmlGreaterOrEqual(hist));
		ArrayList<PlotCurveCharacterstics> chars = Lists.newArrayList(
				new PlotCurveCharacterstics(PlotLineType.HISTOGRAM, 5f, Color.BLACK));
		chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 1f, Color.BLUE));
		new GraphiWindowAPI_Impl(funcs, plotTitle, chars);
		
		plotTitle = "Absolute Inter-event time from "+name1+" to "+name2;
		funcs = Lists.newArrayList();
		funcs.add(absHist);
		new GraphiWindowAPI_Impl(funcs, plotTitle, chars);
	}
	
	private static EvenlyDiscretizedFunc getCmlGreaterOrEqual(EvenlyDiscretizedFunc func) {
		EvenlyDiscretizedFunc cml = new EvenlyDiscretizedFunc(func.getMinX(), func.getNum(), func.getDelta());
		
		double tot = 0d;
		for (int i=func.getNum(); --i>=0;) {
			tot += func.getY(i);
			cml.set(i, tot);
		}
		
		return cml;
	}
	
	private static void plotSlidingWindowCounts(double[] windowLengths, double xInc,
			List<EQSIM_Event> events, List<RuptureIdentifier> rupIdens) {
		List<List<EQSIM_Event>> eventLists = Lists.newArrayList();
		
		double totEventTime = General_EQSIM_Tools.getSimulationDurationYears(events);
		
		Arrays.sort(windowLengths);
		
		for (RuptureIdentifier rupIden : rupIdens)
			eventLists.add(rupIden.getMatches(events));
		
		double maxWindow = StatUtils.max(windowLengths);
		// tim off half of the max window length from each end of the catalog
		double catalogTrim = maxWindow * 0.51;
		double minEventTime = catalogTrim;
		double maxEventTime = totEventTime - catalogTrim;
		
		int numWindows = (int)((totEventTime - 2d*catalogTrim) / xInc);
		double windowWeight = 1d / (double)numWindows;
		
		int numRupIdens = rupIdens.size();
		
		CPT cpt = null;
		try {
			cpt = GMT_CPT_Files.MAX_SPECTRUM.instance().rescale(windowLengths[0], windowLengths[windowLengths.length-1]);
		} catch (IOException e1) {
			ExceptionUtils.throwAsRuntimeException(e1);
		}
		ArrayList<EvenlyDiscretizedFunc> funcs = Lists.newArrayList();
		ArrayList<PlotCurveCharacterstics> chars = Lists.newArrayList();
		
		for (double windowLength : windowLengths) {
			EvenlyDiscretizedFunc func = new EvenlyDiscretizedFunc(0d, 10, 1d);
			double halfLength = windowLength * 0.5d;
			int[] eventIndexesToStart = new int[numRupIdens];
			for (double x=minEventTime; x<maxEventTime; x+=xInc) {
				int numInWindow = 0;
				double minTime = x-halfLength;
				double maxTime = x+halfLength;
				for (int i=0; i<numRupIdens; i++) {
					List<EQSIM_Event> idenEvents = eventLists.get(i);
					for (int e=eventIndexesToStart[i]; e<idenEvents.size(); e++) {
						double t = idenEvents.get(e).getTimeInYears();
						if (t < minTime) {
							eventIndexesToStart[i] = e+1;
							continue;
						}
						if (t > maxTime)
							break;
						numInWindow++;
					}
				}
				func.add(numInWindow, windowWeight);
			}
			func.setName("Window Length: "+windowLength+"\nFractions:\t"+Joiner.on(",\t").join(func.getYValuesIterator()));
			funcs.add(func);
			Color color = cpt.getColor((float)windowLength);
			chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, color));
		}
		
		GraphiWindowAPI_Impl gw =new GraphiWindowAPI_Impl(funcs, "# Matching Events In Windows", chars);
		gw.setX_AxisLabel("# Events (M7+ on specific faults)");
		gw.setY_AxisLabel("Fraction of windows");
	}
	
	/**
	 * This returns a catalog containing all matches from the given events/rup idens but with events randomly distributed
	 * according to their recurrence intervals following a normal distribution. Events involving multiple rup idens are maintained
	 * by using the recurrence intervals of all events using that same set of rup idens.
	 * @param events
	 * @param rupIDens
	 * @param splitMultiples
	 * @return
	 */
	private static List<EQSIM_Event> getRandomResampledCatalog(List<EQSIM_Event> events, List<RuptureIdentifier> rupIdens) {
		int numRupIdens = rupIdens.size();
		List<List<EQSIM_Event>> matchesLists = Lists.newArrayList();
		List<HashSet<EQSIM_Event>> matchesSets = Lists.newArrayList();
		HashSet<EQSIM_Event> allEventsSet = new HashSet<EQSIM_Event>();
		
		for (RuptureIdentifier rupIden : rupIdens) {
			List<EQSIM_Event> matches = rupIden.getMatches(events);
			matchesLists.add(matches);
			matchesSets.add(new HashSet<EQSIM_Event>(matches));
			allEventsSet.addAll(matches);
		}
		
		// now remove events involving multiple rup idens
		List<HashSet<RuptureIdentifier>> multiSets = Lists.newArrayList();
		List<List<EQSIM_Event>> multiEvents = Lists.newArrayList();
		HashSet<EQSIM_Event> multiEventsSet = new HashSet<EQSIM_Event>();
		
		for (EQSIM_Event e : allEventsSet) {
			HashSet<RuptureIdentifier> eventRupIdens = new HashSet<RuptureIdentifier>();
			for (int i=0; i<numRupIdens; i++)
				if (matchesSets.get(i).contains(e))
					eventRupIdens.add(rupIdens.get(i));
			
			if (eventRupIdens.size() > 1) {
				// we have multiple identifiers here
				// look for a matching set already
				int match = -1;
				setLoop:
				for (int i=0; i<multiSets.size(); i++) {
					HashSet<RuptureIdentifier> set = multiSets.get(i);
					if (set.size() != eventRupIdens.size())
						continue;
					for (RuptureIdentifier rupIden : eventRupIdens)
						if (!set.contains(rupIden))
							continue setLoop;
					// if we're here then it's a match
					match = -1;
					break;
				}
				if (match < 0) {
					multiSets.add(eventRupIdens);
					List<EQSIM_Event> eList = Lists.newArrayList();
					eList.add(e);
					multiEvents.add(eList);
				} else {
					multiEvents.get(match).add(e);
				}
				multiEventsSet.add(e);
			}
		}
		
		// now build return periods
		// TODO
		List<List<EQSIM_Event>> eventListsToResample = Lists.newArrayList();
//		List<RealDistribution> distributions = Lists.newArrayList();
		
		System.out.println("Detected "+multiSets.size()+" combinations of multi-events!");
		
		return null;
	}
	
	private static void plotCmlInterEventTime(List<EQSIM_Event> events, RuptureIdentifier rupIen) {
		
	}

}
