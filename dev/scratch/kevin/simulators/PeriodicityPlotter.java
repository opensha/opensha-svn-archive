package scratch.kevin.simulators;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.distribution.RealDistribution;
import org.apache.commons.math3.stat.StatUtils;
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
import org.opensha.sha.gui.infoTools.HeadlessGraphPanel;
import org.opensha.sha.gui.infoTools.PlotCurveCharacterstics;
import org.opensha.sha.simulators.eqsim_v04.EQSIM_Event;
import org.opensha.sha.simulators.eqsim_v04.General_EQSIM_Tools;

import scratch.UCERF3.enumTreeBranches.MaxMagOffFault;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.primitives.Doubles;

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
		List<EQSIM_Event> events = tools.getEventsList();
		
		boolean doRandom = true;
		boolean display = false;
		boolean displayEventTimes = false;
		boolean randomNormDist = false;
		
		File writeDir = new File(dir, "period_plots");
		if (!writeDir.exists())
			writeDir.mkdir();
		
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
				ElementMagRangeDescription.SAF_CHOLAME_ELEMENT_ID, 7d, 10d));
		rupIdenNames.add("SAF Cholame 7+");
		colors.add(Color.RED);
		rupIdens.add(new ElementMagRangeDescription(
				ElementMagRangeDescription.GARLOCK_WEST_ELEMENT_ID, 7d, 10d));
		rupIdenNames.add("Garlock 7+");
		colors.add(Color.GREEN);
		rupIdens.add(new ElementMagRangeDescription(
				ElementMagRangeDescription.SAN_JACINTO__ELEMENT_ID, 7d, 10d));
		rupIdenNames.add("San Jacinto 7+");
		colors.add(Color.CYAN);
//		rupIdens.add(new MagRangeRuptureIdentifier(7d, 10d));
//		rupIdenNames.add("All 7+");
//		colors.add(Color.GRAY);
		
		int coachellaIndex = 1;
		int carrizoIndex = 2;
		int mojaveIndex = 0;
		int cholameIndex = 3;
		int garlockIndex = 4;
		
		
		List<RuptureIdentifier> elemRupIdens = getOnlyElemMagDescriptions(rupIdens);
		
		boolean[] randoms;
		if (doRandom) {
			randoms = new boolean[2];
			randoms[1] = true;
		} else {
			randoms = new boolean [1];
		}
		
		ArrayList<EvenlyDiscretizedFunc> slidingWindows = null;
		ArrayList<EvenlyDiscretizedFunc> randomizedSlidingWindows = null;
		
		for (boolean randomized : randoms) {
			if (randomized) {
				List<EQSIM_Event> randomResampledCatalog = getRandomResampledCatalog(events, elemRupIdens, randomNormDist);
				events = randomResampledCatalog;
			}
			
			Color duplicateColor = Color.ORANGE;
			
			HashSet<Integer> duplicates = new HashSet<Integer>();
			Map<Integer, EQSIM_Event> eventsMap = Maps.newHashMap();
			List<HashSet<Integer>> idsList = Lists.newArrayList();
			
			for (int i=0; i<rupIdens.size(); i++) {
				RuptureIdentifier rupIden = rupIdens.get(i);
				String name = rupIdenNames.get(i);
				
				HistogramFunction hist;
				if (rupIden instanceof ElementMagRangeDescription)
					hist = new HistogramFunction(5d, 100, 10d);
				else
					hist = new HistogramFunction(5d, 100, 1d);
				
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
				
				makePlot(writeDir, "period_"+getFileSafeString(name), display, randomized, funcs, chars, name+" Periodicity");
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
			
			makePlot(writeDir, "event_times", displayEventTimes, randomized, funcs, chars, "Event Times");
			
			plotTimeBetweenIdens(writeDir, display, randomized, events, rupIdens.get(coachellaIndex), rupIdenNames.get(coachellaIndex),
					rupIdens.get(carrizoIndex), rupIdenNames.get(carrizoIndex));
			plotTimeBetweenIdens(writeDir, display, randomized, events, rupIdens.get(carrizoIndex), rupIdenNames.get(carrizoIndex),
					rupIdens.get(coachellaIndex), rupIdenNames.get(coachellaIndex));
			plotTimeBetweenIdens(writeDir, display, randomized, events, rupIdens.get(carrizoIndex), rupIdenNames.get(carrizoIndex),
					rupIdens.get(mojaveIndex), rupIdenNames.get(mojaveIndex));
			plotTimeBetweenIdens(writeDir, display, randomized, events, rupIdens.get(mojaveIndex), rupIdenNames.get(mojaveIndex),
					rupIdens.get(carrizoIndex), rupIdenNames.get(carrizoIndex));
			plotTimeBetweenIdens(writeDir, display, randomized, events, rupIdens.get(cholameIndex), rupIdenNames.get(cholameIndex),
					rupIdens.get(carrizoIndex), rupIdenNames.get(carrizoIndex));
			plotTimeBetweenIdens(writeDir, display, randomized, events, rupIdens.get(carrizoIndex), rupIdenNames.get(carrizoIndex),
					rupIdens.get(cholameIndex), rupIdenNames.get(cholameIndex));
			plotTimeBetweenIdens(writeDir, display, randomized, events, rupIdens.get(mojaveIndex), rupIdenNames.get(mojaveIndex),
					rupIdens.get(garlockIndex), rupIdenNames.get(garlockIndex));
			plotTimeBetweenIdens(writeDir, display, randomized, events, rupIdens.get(mojaveIndex), rupIdenNames.get(mojaveIndex),
					rupIdens.get(coachellaIndex), rupIdenNames.get(coachellaIndex));
			
//			double[] windowLengths = { 5d, 10d, 25d, 50d, 100d };
			double[] windowLengths = new double[30];
			for (int i=1; i<=windowLengths.length; i++)
				windowLengths[i-1] = 5d*(double)i;
			double xInc = 1d;
			
			if (randomized)
				randomizedSlidingWindows =
					plotSlidingWindowCounts(writeDir, display, randomized, windowLengths, xInc, events, elemRupIdens);
			else
				slidingWindows =
					plotSlidingWindowCounts(writeDir, display, randomized, windowLengths, xInc, events, elemRupIdens);
			
			plotInterEventBetweenAllDist(writeDir, display, randomized, events, elemRupIdens);
			
			if (!randomized) {
				double[] omoris = { 6d, 6.5d, 7d, 7.5d };
				
				for (int i=0; i<elemRupIdens.size(); i++) {
					RuptureIdentifier rupIden = elemRupIdens.get(i);
					plotOmoriDecay(writeDir, display, randomized, events, rupIden, rupIdenNames.get(i), omoris, 365);
				}
			}
		}
		
		if (randomizedSlidingWindows != null && slidingWindows != null) {
			ArrayList<EvenlyDiscretizedFunc> ratios = Lists.newArrayList();
			
			CPT cpt = null;
			try {
				cpt = GMT_CPT_Files.MAX_SPECTRUM.instance().rescale(0, slidingWindows.size()-1);
			} catch (IOException e1) {
				ExceptionUtils.throwAsRuntimeException(e1);
			}
			
			double min = 0;
			int num = slidingWindows.get(0).getNum();
			double delta = 1d;
			
			ArrayList<PlotCurveCharacterstics> chars = Lists.newArrayList();
			for (int i=0; i<slidingWindows.size(); i++) {
				EvenlyDiscretizedFunc ratio = new EvenlyDiscretizedFunc(min, num, delta);
				
				EvenlyDiscretizedFunc slidingFunc = slidingWindows.get(i);
				EvenlyDiscretizedFunc randomFunc = randomizedSlidingWindows.get(i);
				
				for (int x=0; x<num; x++) {
					ratio.set(x, slidingFunc.getY(x) / randomFunc.getY(x));
				}
				
				ratios.add(ratio);
				chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, cpt.getColor((float)i)));
			}
			
			makePlot(writeDir, "matches_in_windows_ratio", display, false, ratios, chars,
					"Ratio of # Matching Events In Windows (catalog / randomized)",
					"# Events (M7+ on specific faults)",  "Ratio (catalog / randomized)", null, null);
		}
	}
	
	private static List<RuptureIdentifier> getOnlyElemMagDescriptions(List<RuptureIdentifier> rupIdens) {
		List<RuptureIdentifier> ret = Lists.newArrayList();
		
		for (RuptureIdentifier rupIden : rupIdens)
			if (rupIden instanceof ElementMagRangeDescription)
				ret.add(rupIden);
		
		return ret;
	}
	
	private static void plotTimeBetweenIdens(File writeDir, boolean display, boolean randomized,
			List<EQSIM_Event> events, RuptureIdentifier iden1, String name1,
			RuptureIdentifier iden2, String name2) throws IOException {
		double maxX = 500d;
		HistogramFunction hist = new HistogramFunction(5d, 50, 10d);
		HistogramFunction absHist = new HistogramFunction(5d, 50, 10d);
		
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
		
		double[][] ranges = new double[2][2];
		ranges[0][0] = 0;
		ranges[0][1] = maxX;
		ranges[1][0] = 0;
		ranges[1][1] = 3500;
		
		String plotTitle = "Inter-event time from "+name1+" to "+name2;
		ArrayList<DiscretizedFunc> funcs = Lists.newArrayList();
		funcs.add(hist);
//		funcs.add(getCmlGreaterOrEqual(hist));
		ArrayList<PlotCurveCharacterstics> chars = Lists.newArrayList(
				new PlotCurveCharacterstics(PlotLineType.HISTOGRAM, 5f, Color.BLACK));
		chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 1f, Color.BLUE));
		makePlot(writeDir, "inter_event_"+getFileSafeString(name1)+"_to_"+getFileSafeString(name2),
				display, randomized, funcs, chars, plotTitle, null, null, ranges, null);
		
		plotTitle = "Absolute Inter-event time from "+name1+" to "+name2;
		funcs = Lists.newArrayList();
		funcs.add(absHist);
		makePlot(writeDir, "inter_event_abs_"+getFileSafeString(name1)+"_to_"+getFileSafeString(name2),
				display, randomized, funcs, chars, plotTitle, null, null, ranges, null);
		
		HistogramFunction matrixHist = new HistogramFunction(-2500, 500, 10d);
		double matHistMin = matrixHist.getMinX() - 5d;
		double matHistMax = matrixHist.getMaxX() + 5d;
		HistogramFunction absMatrixHist = new HistogramFunction(5d, 250, 10d);
		
		for (EQSIM_Event event1 : matches1) {
			double timeYears1 = event1.getTimeInYears();
			for (EQSIM_Event event2 : matches2) {
				double timeYears2 = event2.getTimeInYears();
				
				double timeDelta = timeYears1 - timeYears2;
				double absDelta = Math.abs(timeDelta);
				
				if (timeDelta >= matHistMin && timeDelta <= matHistMax)
					matrixHist.add(timeDelta, 1d);
				if (absDelta <= matHistMax)
					absMatrixHist.add(absDelta, 1d);
			}
		}
		
		plotTitle = "Inter-event time from ALL "+name1+" to ALL "+name2;
		funcs = Lists.newArrayList();
		funcs.add(matrixHist);
//		funcs.add(getCmlGreaterOrEqual(hist));
		chars = Lists.newArrayList(
				new PlotCurveCharacterstics(PlotLineType.HISTOGRAM, 5f, Color.BLACK));
		chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 1f, Color.BLUE));
		makePlot(writeDir, "all_inter_event_"+getFileSafeString(name1)+"_to_"+getFileSafeString(name2),
				display, randomized, funcs, chars, plotTitle);
		
		plotTitle = "Absolute Inter-event time from ALL "+name1+" to ALL "+name2;
		funcs = Lists.newArrayList();
		funcs.add(absMatrixHist);
		makePlot(writeDir, "all_inter_event_abs_"+getFileSafeString(name1)+"_to_"+getFileSafeString(name2),
				display, randomized, funcs, chars, plotTitle);
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
	
	private static ArrayList<EvenlyDiscretizedFunc> plotSlidingWindowCounts(File writeDir, boolean display, boolean randomized,
			double[] windowLengths, double xInc,
			List<EQSIM_Event> events, List<RuptureIdentifier> rupIdens) throws IOException {
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
				if (numInWindow <= func.getMaxX())
					func.add((double)numInWindow, windowWeight);
			}
			func.setName("Window Length: "+windowLength+"\nFractions:\t"+Joiner.on(",\t").join(func.getYValuesIterator()));
			funcs.add(func);
			Color color = cpt.getColor((float)windowLength);
			chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, color));
		}
		
		makePlot(writeDir, "matches_in_windows", display, randomized, funcs, chars,
				"# Matching Events In Windows", "# Events (M7+ on specific faults)",  "Fraction of windows", null, null);
		
		return funcs;
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
	private static List<EQSIM_Event> getRandomResampledCatalog(
			List<EQSIM_Event> events, List<RuptureIdentifier> rupIdens, boolean normDist) {
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
					match = i;
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
		
		System.out.println("Detected "+multiSets.size()+" combinations of multi-events!");
		
		// now build return periods
		List<List<EQSIM_Event>> eventListsToResample = Lists.newArrayList();
		List<RandomReturnPeriodProvider> randomRPsList = Lists.newArrayList();
		
		double totTime = General_EQSIM_Tools.getSimulationDurationYears(events);
		
		for (int i=0; i<rupIdens.size(); i++) {
			List<EQSIM_Event> eventsToResample = Lists.newArrayList(matchesLists.get(i));
			eventsToResample.removeAll(multiEventsSet);
			eventListsToResample.add(eventsToResample);
			double[] rps = getRPs(eventsToResample);
			randomRPsList.add(getReturnPeriodProvider(normDist, rps, totTime));
		}
		
		for (int i=0; i<multiEvents.size(); i++) {
			List<EQSIM_Event> eventsToResample = Lists.newArrayList(multiEvents.get(i));
			Collections.sort(eventsToResample);
			eventListsToResample.add(eventsToResample);
			double[] rps = getRPs(eventsToResample);
			randomRPsList.add(getReturnPeriodProvider(normDist, rps, totTime));
		}
		
		List<EQSIM_Event> newList = Lists.newArrayList();
		
		for (int i=0; i<eventListsToResample.size(); i++) {
			RandomReturnPeriodProvider randomRP = randomRPsList.get(i);
			// start at a random interval through the first RP
			double time = Math.random() * randomRP.getReturnPeriod();
			for (EQSIM_Event e : eventListsToResample.get(i)) {
				double timeSecs = time * General_EQSIM_Tools.SECONDS_PER_YEAR;
				EQSIM_Event newE = EventsInWindowsMatcher.cloneNewTime(e, timeSecs);
				newList.add(newE);
				
				// move forward one RP
				time += randomRP.getReturnPeriod();
			}
		}
		
		// now sort to make it in order
		Collections.sort(newList);
		
		return newList;
	}
	
	private static RandomReturnPeriodProvider getReturnPeriodProvider(boolean normDist, double[] rps, double totTime) {
		if (rps.length == 0) {
			rps = new double[1];
			rps[0] = totTime;
			return new ActualDistReturnPeriodProvider(rps);
		}
		if (normDist)
			return new NormalDistReturnPeriodProvider(rps);
		else
			return new ActualDistReturnPeriodProvider(rps);
	}
	
	private static interface RandomReturnPeriodProvider {
		public double getReturnPeriod();
	}
	
	private static class NormalDistReturnPeriodProvider implements RandomReturnPeriodProvider {
		
		private NormalDistribution n;
		
		public NormalDistReturnPeriodProvider(double[] rps) {
			double mean = StatUtils.mean(rps);
			double sd = Math.sqrt(StatUtils.variance(rps, mean));
			n = new NormalDistribution(mean, sd);
		}

		@Override
		public double getReturnPeriod() {
			return n.sample();
		}
	}
	
	private static class ActualDistReturnPeriodProvider implements RandomReturnPeriodProvider {
		
		private int index = 0;
		private double[] random_rps;
		
		public ActualDistReturnPeriodProvider(double[] rps) {
			random_rps = Arrays.copyOf(rps, rps.length);
			List<Double> randomized = Doubles.asList(random_rps);
			Collections.shuffle(randomized);
			random_rps = Doubles.toArray(randomized);
		}

		@Override
		public double getReturnPeriod() {
			if (index == random_rps.length)
				index = 0;
			return random_rps[index++];
		}
	}
	
	private static double[] getRPs(List<EQSIM_Event> matches) {
		List<Double> rps = Lists.newArrayList();
		
		double prevTime = -1;
		for (EQSIM_Event e : matches) {
			double time = e.getTimeInYears();
			
			if (prevTime >= 0)
				rps.add(time - prevTime);
			
			prevTime = time;
		}
		
		return Doubles.toArray(rps);
	}
	
	private static void plotInterEventBetweenAllDist(File writeDir, boolean display, boolean randomized,
			List<EQSIM_Event> events, List<RuptureIdentifier> rupIdens) throws IOException {
		HashSet<EQSIM_Event> matchesSet = new HashSet<EQSIM_Event>();
		
		for (RuptureIdentifier rupIden : rupIdens)
			for (EQSIM_Event e : rupIden.getMatches(events))
				matchesSet.add(e);
		
		List<EQSIM_Event> matches = Lists.newArrayList(matchesSet);
		Collections.sort(matches);
		
		HistogramFunction hist = new HistogramFunction(5d, 50, 10d);
		
		double prevTime = matches.get(0).getTimeInYears();
		
		for (int i=1; i<matches.size(); i++) {
			EQSIM_Event e = matches.get(i);
			
			double timeDelta = e.getTimeInYears()-prevTime;
			
			if (timeDelta <= hist.getMaxX()+5d) {
				hist.add(timeDelta, 1d);
			}
			
			prevTime = e.getTimeInYears();
		}
		
		ArrayList<DiscretizedFunc> funcs = Lists.newArrayList();
		funcs.add(hist);
		ArrayList<PlotCurveCharacterstics> chars = Lists.newArrayList();
		chars.add(new PlotCurveCharacterstics(PlotLineType.HISTOGRAM, 5f, Color.BLACK));
		
		double[][] ranges = new double[2][2];
		ranges[0][0] = 0;
		ranges[0][1] = hist.getMaxX()+15d;
		ranges[1][0] = 0;
		ranges[1][1] = 6000;
		
		makePlot(writeDir, "inter_any_event_dist", display, randomized, funcs, chars, "Inter Event Time Between Any",
				null, null, ranges, null);
	}
	
	private static void plotOmoriDecay(File writeDir, boolean display, boolean randomized,
			List<EQSIM_Event> events, RuptureIdentifier rupIden, String idenName, double[] minMags, double maxDays)
					throws IOException {
		
		if (idenName.contains("7"))
			idenName = idenName.substring(0, idenName.indexOf("7")).trim();
		
		System.out.println("Plotting Omori "+idenName);
		List<EQSIM_Event> matches = rupIden.getMatches(events);
		
		double maxYears = maxDays / BatchPlotGen.DAYS_PER_YEAR;
		
		ArrayList<DiscretizedFunc> funcs = Lists.newArrayList();
		ArrayList<PlotCurveCharacterstics> chars = Lists.newArrayList();
		
		CPT cpt = null;
		try {
			cpt = GMT_CPT_Files.MAX_SPECTRUM.instance().rescale(0, minMags.length-1);
		} catch (IOException e1) {
			ExceptionUtils.throwAsRuntimeException(e1);
		}
		
		for (int m=0; m<minMags.length; m++) {
			int startEventI = 0;
			
			double minMag = minMags[m];
			HistogramFunction hist = new HistogramFunction(0.5d, (int)Math.ceil(maxDays), 1d);
			
			for (EQSIM_Event match : matches) {
				double matchTime = match.getTimeInYears();
				double maxTime = matchTime + maxYears;
				
				for (int i=startEventI; i<events.size(); i++) {
					EQSIM_Event e = events.get(i);
					if (e.getID() == match.getID()) {
						startEventI = i;
						continue;
					}
					if (e.getMagnitude() < minMag)
						continue;
					double eventYears = e.getTimeInYears();
					if (eventYears < matchTime)
						continue;
					if (eventYears > maxTime)
						break;
					
					double deltaDays = (eventYears - matchTime) * BatchPlotGen.DAYS_PER_YEAR;
					if (deltaDays <= hist.getMaxX()+0.5d)
						hist.add(deltaDays, 1);
				}
			}
			
			EvenlyDiscretizedFunc cmlWithOffset = new EvenlyDiscretizedFunc(
					hist.getMinX()+0.5d, hist.getMaxX()+0.5d, hist.getNum());
			double cnt = 0;
			for (int i=0; i<hist.getNum(); i++) {
				cnt += hist.getY(i);
				cmlWithOffset.set(i, cnt);
			}
			funcs.add(cmlWithOffset);
			chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, cpt.getColor((float)m)));
			
//			if (idenName.startsWith("SAF Mojave")) {
//				System.out.println("Funcs for M >= "+minMag);
//				for (int i=0; i<cmlWithOffset.getNum(); i++)
//					System.out.println((i+1)+". "+hist.getY(i)+"\t"+cmlWithOffset.getY(i));
//			}
		}
		
//		ArrayList<DiscretizedFunc> funcs = Lists.newArrayList();
//		funcs.add(hist);
//		ArrayList<PlotCurveCharacterstics> chars = Lists.newArrayList();
//		chars.add(new PlotCurveCharacterstics(PlotLineType.HISTOGRAM, 5f, Color.BLACK));
		
		String prefix = "omori_"+getFileSafeString(idenName);
		
		double minX = 0.5d;
		double maxX = 0d;
		double minY = Double.POSITIVE_INFINITY;
		double maxY = 0d;
		for (DiscretizedFunc func : funcs) {
			if (func.getMaxX() > maxX)
				maxX = func.getMaxX();
			if (func.getMinY() < minY)
				minY = func.getMinY();
			if (func.getMaxY() > maxY)
				maxY = func.getMaxY();
		}
		if (minY == 0)
			minY = 1d;
		
		double[][] ranges = new double[2][2];
		ranges[0][0] = minX * 0.9;
		ranges[0][1] = maxX * 1.1;
		ranges[1][0] = minY * 0.9;
		ranges[1][1] = maxY * 1.1;
		
		System.out.println(minX+", "+maxX+", "+minY+", "+maxY);
		
		boolean[] logs = { true, true };
		
		makePlot(writeDir, prefix, display, randomized, funcs, chars, "Omori Comparison "+idenName,
				"Day", "Cumulative # Events", ranges, logs);
	}
	
	private static void makePlot(File dir, String prefix, boolean display, boolean randomized,
			ArrayList<? extends DiscretizedFunc> funcs, ArrayList<PlotCurveCharacterstics> chars, String plotTitle)
					throws IOException {
		makePlot(dir, prefix, display, randomized, funcs, chars, plotTitle, null, null, null, null);
	}
	
	private static void makePlot(File dir, String prefix, boolean display, boolean randomized,
			ArrayList<? extends DiscretizedFunc> funcs, ArrayList<PlotCurveCharacterstics> chars, String plotTitle,
			String xAxisLabel, String yAxisLabel, double[][] ranges, boolean[] logs) throws IOException {
		if (randomized) {
			plotTitle = "RANDOMIZED "+plotTitle;
			prefix = prefix+"_randomized";
		}
		
		String fileName = new File(dir, prefix+".png").getAbsolutePath();
		
		if (display) {
			GraphiWindowAPI_Impl gw = new GraphiWindowAPI_Impl(funcs, plotTitle, chars, display);
			gw.getGraphWindow().setSize(600, 800);
			if (xAxisLabel != null)
				gw.setX_AxisLabel(xAxisLabel);
			if (yAxisLabel != null)
				gw.setY_AxisLabel(yAxisLabel);
			if (ranges != null) {
				gw.setX_AxisRange(ranges[0][0], ranges[0][1]);
				gw.setY_AxisRange(ranges[1][0], ranges[1][1]);
			}
			if (logs != null) {
				gw.setXLog(logs[0]);
				gw.setYLog(logs[1]);
			}
			
			gw.saveAsPNG(fileName);
		} else {
			HeadlessGraphPanel gp = new HeadlessGraphPanel();
			if (ranges != null) {
				gp.setUserBounds(ranges[0][0], ranges[0][1], ranges[1][0], ranges[1][1]);
			}
			if (logs != null) {
				gp.setXLog(logs[0]);
				gp.setYLog(logs[1]);
			}
			gp.drawGraphPanel(xAxisLabel, yAxisLabel, funcs, chars, ranges != null, plotTitle);
			gp.getCartPanel().setSize(1000, 800);
			gp.saveAsPNG(fileName);
		}
		
	}
	
	private static String getFileSafeString(String str) {
		return str.replaceAll("\\W+", "_");
	}

}
