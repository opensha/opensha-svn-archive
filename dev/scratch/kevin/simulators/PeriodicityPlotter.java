package scratch.kevin.simulators;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
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
		
		double[] interevent_mags = { 5d, 5.5d, 6d, 6.5d, 7d, 7.5d };
		for (double mag : interevent_mags) {
			List<RuptureIdentifier> rupIdens = Lists.newArrayList();
			List<String> rupIdenNames = Lists.newArrayList();
			
			rupIdens.add(new ElementMagRangeDescription(
					ElementMagRangeDescription.SAF_CHOLAME_ELEMENT_ID, mag, 10d));
			rupIdenNames.add("Cholame");
			
			rupIdens.add(new ElementMagRangeDescription(
					ElementMagRangeDescription.SAF_CARRIZO_ELEMENT_ID, mag, 10d));
			rupIdenNames.add("Carrizo");
			
			makeMultiRecurrPlots(writeDir, display, mag, events, rupIdens, rupIdenNames);
		}
		
		List<RuptureIdentifier> rupIdens = Lists.newArrayList();
		List<String> rupIdenNames = Lists.newArrayList();
		List<Color> colors = Lists.newArrayList();
		
		rupIdens.add(new ElementMagRangeDescription(
				ElementMagRangeDescription.SAF_CHOLAME_ELEMENT_ID, 7d, 10d));
		rupIdenNames.add("SAF Cholame 7+");
		colors.add(Color.RED);
		
		rupIdens.add(new ElementMagRangeDescription(
				ElementMagRangeDescription.SAF_CARRIZO_ELEMENT_ID, 7d, 10d));
		rupIdenNames.add("SAF Carrizo 7+");
		colors.add(Color.BLUE);
		
		rupIdens.add(new ElementMagRangeDescription(
				ElementMagRangeDescription.GARLOCK_WEST_ELEMENT_ID, 7d, 10d));
		rupIdenNames.add("Garlock 7+");
		colors.add(Color.GREEN);
		
		rupIdens.add(new ElementMagRangeDescription(
				ElementMagRangeDescription.SAF_MOJAVE_ELEMENT_ID, 7d, 10d));
		rupIdenNames.add("SAF Mojave 7+");
		colors.add(Color.BLACK);
		
		rupIdens.add(new ElementMagRangeDescription(
				ElementMagRangeDescription.SAF_COACHELLA_ELEMENT_ID, 7d, 10d));
		rupIdenNames.add("SAF Coachella 7+");
		colors.add(Color.RED);
		
		rupIdens.add(new ElementMagRangeDescription(
				ElementMagRangeDescription.SAN_JACINTO__ELEMENT_ID, 7d, 10d));
		rupIdenNames.add("San Jacinto 7+");
		colors.add(Color.CYAN);
		
//		rupIdens.add(new MagRangeRuptureIdentifier(7d, 10d));
//		rupIdenNames.add("All 7+");
//		colors.add(Color.GRAY);
		
		int cholameIndex = 0;
		int carrizoIndex = 1;
		int garlockIndex = 2;
		int mojaveIndex = 3;
		int coachellaIndex = 4;
		int sanJacintoIndex = 5;
		
		List<RuptureIdentifier> rupIdensSubset = Lists.newArrayList();
		List<String> rupIdenNamesSubset = Lists.newArrayList();
		List<Color> colorsSubset = Lists.newArrayList();
		rupIdensSubset.add(rupIdens.get(mojaveIndex));
		rupIdenNamesSubset.add(rupIdenNames.get(mojaveIndex));
		colorsSubset.add(colors.get(mojaveIndex));
		rupIdensSubset.add(rupIdens.get(coachellaIndex));
		rupIdenNamesSubset.add(rupIdenNames.get(coachellaIndex));
		colorsSubset.add(colors.get(coachellaIndex));
		
		List<RuptureIdentifier> rupIdensNoCholame = Lists.newArrayList(rupIdens);
		rupIdensNoCholame.remove(cholameIndex);
		List<String> rupIdenNamesNoCholame = Lists.newArrayList(rupIdenNames);
		rupIdenNamesNoCholame.remove(cholameIndex);
		List<Color> colorsNoCholame = Lists.newArrayList(colors);
		colorsNoCholame.remove(cholameIndex);
		
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
		
		List<EQSIM_Event> randomResampledCatalog = null;
		for (boolean randomized : randoms)
			if (randomized)
				randomResampledCatalog = getRandomResampledCatalog(events, elemRupIdens, randomNormDist);
		
		for (boolean randomized : randoms) {
			if (randomized)
				events = randomResampledCatalog;
			
			File myWriteDir;
			if (randomized)
				myWriteDir = new File(writeDir, "randomized");
			else
				myWriteDir = writeDir;
			if (!myWriteDir.exists())
				myWriteDir.mkdir();
			
			plotPeriodsAndEvents(events, display, displayEventTimes, myWriteDir,
					rupIdensSubset, rupIdenNamesSubset, colorsSubset, randomized);
			plotPeriodsAndEvents(events, display, displayEventTimes, myWriteDir,
					rupIdensNoCholame, rupIdenNamesNoCholame, colorsNoCholame, randomized);
			
			plotTimeBetweenIdens(myWriteDir, display, randomized, events, rupIdens.get(coachellaIndex), rupIdenNames.get(coachellaIndex),
					rupIdens.get(carrizoIndex), rupIdenNames.get(carrizoIndex));
			plotTimeBetweenIdens(myWriteDir, display, randomized, events, rupIdens.get(coachellaIndex), rupIdenNames.get(coachellaIndex),
					rupIdens.get(mojaveIndex), rupIdenNames.get(mojaveIndex));
			plotTimeBetweenIdens(myWriteDir, display, randomized, events, rupIdens.get(carrizoIndex), rupIdenNames.get(carrizoIndex),
					rupIdens.get(coachellaIndex), rupIdenNames.get(coachellaIndex));
			plotTimeBetweenIdens(myWriteDir, display, randomized, events, rupIdens.get(carrizoIndex), rupIdenNames.get(carrizoIndex),
					rupIdens.get(mojaveIndex), rupIdenNames.get(mojaveIndex));
			plotTimeBetweenIdens(myWriteDir, display, randomized, events, rupIdens.get(mojaveIndex), rupIdenNames.get(mojaveIndex),
					rupIdens.get(carrizoIndex), rupIdenNames.get(carrizoIndex));
			plotTimeBetweenIdens(myWriteDir, display, randomized, events, rupIdens.get(cholameIndex), rupIdenNames.get(cholameIndex),
					rupIdens.get(carrizoIndex), rupIdenNames.get(carrizoIndex));
			plotTimeBetweenIdens(myWriteDir, display, randomized, events, rupIdens.get(carrizoIndex), rupIdenNames.get(carrizoIndex),
					rupIdens.get(cholameIndex), rupIdenNames.get(cholameIndex));
			plotTimeBetweenIdens(myWriteDir, display, randomized, events, rupIdens.get(mojaveIndex), rupIdenNames.get(mojaveIndex),
					rupIdens.get(garlockIndex), rupIdenNames.get(garlockIndex));
			plotTimeBetweenIdens(myWriteDir, display, randomized, events, rupIdens.get(garlockIndex), rupIdenNames.get(garlockIndex),
					rupIdens.get(mojaveIndex), rupIdenNames.get(mojaveIndex));
			plotTimeBetweenIdens(myWriteDir, display, randomized, events, rupIdens.get(sanJacintoIndex), rupIdenNames.get(sanJacintoIndex),
					rupIdens.get(mojaveIndex), rupIdenNames.get(mojaveIndex));
			plotTimeBetweenIdens(myWriteDir, display, randomized, events, rupIdens.get(mojaveIndex), rupIdenNames.get(mojaveIndex),
					rupIdens.get(coachellaIndex), rupIdenNames.get(coachellaIndex));
			
//			double[] windowLengths = { 5d, 10d, 25d, 50d, 100d };
			double[] windowLengths = new double[30];
			for (int i=1; i<=windowLengths.length; i++)
				windowLengths[i-1] = 5d*(double)i;
			double xInc = 1d;
			
			if (randomized)
				randomizedSlidingWindows =
					plotSlidingWindowCounts(myWriteDir, display, randomized, windowLengths, xInc, events, elemRupIdens);
			else
				slidingWindows =
					plotSlidingWindowCounts(myWriteDir, display, randomized, windowLengths, xInc, events, elemRupIdens);
			
			plotInterEventBetweenAllDist(myWriteDir, display, randomized, events, elemRupIdens);
			
			boolean[] initials = {true, false};
			double cumulativePlotYears = 100d;
			if (!randomized) {
				if (randomResampledCatalog == null)
					randomResampledCatalog = getRandomResampledCatalog(events, elemRupIdens, randomNormDist);
				
				for (boolean includeInitialCorupture : initials) {
					plotConditionalProbs(myWriteDir, display, events, randomResampledCatalog,
							// target
							rupIdens.get(coachellaIndex), rupIdenNames.get(coachellaIndex),
							// given
							rupIdens.get(mojaveIndex), rupIdenNames.get(mojaveIndex), cumulativePlotYears, includeInitialCorupture);
					plotConditionalProbs(myWriteDir, display, events, randomResampledCatalog,
							// target
							rupIdens.get(mojaveIndex), rupIdenNames.get(mojaveIndex),
							// given
							rupIdens.get(coachellaIndex), rupIdenNames.get(coachellaIndex), cumulativePlotYears, includeInitialCorupture);
//					plotConditionalProbs(myWriteDir, display, randomized, events,
//							// target
//							rupIdens.get(coachellaIndex), rupIdenNames.get(coachellaIndex),
//							// given
//							rupIdens.get(mojaveIndex), rupIdenNames.get(mojaveIndex), 5d, includeInitialCorupture);
				}
			}
			
			if (!randomized) {
				double[] omoris = { 6d, 6.5d, 7d, 7.5d };
				
				for (int i=0; i<elemRupIdens.size(); i++) {
					RuptureIdentifier rupIden = elemRupIdens.get(i);
					plotOmoriDecay(myWriteDir, display, randomized, events, rupIden, rupIdenNames.get(i), omoris, 365);
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
	
	private static Color duplicateColor = Color.ORANGE.darker();

	private static void plotPeriodsAndEvents(List<EQSIM_Event> events,
			boolean display, boolean displayEventTimes, File writeDir,
			List<RuptureIdentifier> rupIdens, List<String> rupIdenNames,
			List<Color> colors, boolean randomized) throws IOException {
		int[] dimensions = { 1000, 500 };
		
		HashSet<Integer> duplicates = new HashSet<Integer>();
		Map<Integer, EQSIM_Event> eventsMap = Maps.newHashMap();
		List<HashSet<Integer>> idsList = Lists.newArrayList();
		
		ArrayList<DiscretizedFunc> allPeriodFuncs = Lists.newArrayList();
		ArrayList<PlotCurveCharacterstics> allPeriodChars = Lists.newArrayList();
		
		ArrayList<ArbitrarilyDiscretizedFunc> labelFuncs = Lists.newArrayList();
		ArrayList<PlotCurveCharacterstics> labelChars = Lists.newArrayList();
		
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
			
			ArbitrarilyDiscretizedFunc labelFunc = new ArbitrarilyDiscretizedFunc(rupIdenNames.get(i)+" Rups");
			
			double labelY = (rupIdens.size()-1) - i + 1;
			
			ArrayList<Double> rpList = Lists.newArrayList();
			
			for (EQSIM_Event match : rupIden.getMatches(events)) {
				double time = match.getTimeInYears();
				labelFunc.set(time, labelY);
				Integer id = match.getID();
				if (eventsMap.containsKey(id))
					duplicates.add(id);
				else
					eventsMap.put(id, match);
				ids.add(match.getID());
				
				if (prevTime > 0) {
					double diff = time - prevTime;
					rpList.add(diff);
					int ind = hist.getXIndex(diff);
					if (ind >= 0)
						hist.add(ind, 1d);
				}
				prevTime = time;
			}
			
			double[] rps = Doubles.toArray(rpList);
			Arrays.sort(rps);
			double mean = StatUtils.mean(rps);
			double median = BatchPlotGen.median(rps);
			
			double[] deviations = new double[rps.length];
			for (int j=0; j<rps.length; j++)
				deviations[j] = Math.abs(rps[j] - median);
			Arrays.sort(deviations);
			double mad = BatchPlotGen.median(deviations);
			
			System.out.println(rupIdenNames.get(i)+": mean="+mean+"\tmedian="+median+"\tmad="+mad);
			
			labelFuncs.add(labelFunc);
			labelChars.add(new PlotCurveCharacterstics(PlotSymbol.FILLED_SQUARE, 8f, colors.get(i)));
			
			ArrayList<DiscretizedFunc> funcs = Lists.newArrayList();
			funcs.add(hist);
			allPeriodFuncs.add(hist);
//			funcs.add(getCmlGreaterOrEqual(hist));
			ArrayList<PlotCurveCharacterstics> chars = Lists.newArrayList();
			chars.add(new PlotCurveCharacterstics(PlotLineType.HISTOGRAM, 5f, colors.get(i)));
			allPeriodChars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, colors.get(i)));
			chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 1f, Color.BLUE));
			
			makePlot(writeDir, "period_"+getFileSafeString(name), display, randomized,
					funcs, chars, name+" Inter-Event Times", "Years", "Number", null, null, dimensions);
		}
		
		double[][] periodRanges = new double[2][2];
		periodRanges[0][0] = 0;
		periodRanges[0][1] = 1000;
		periodRanges[1][0] = 0.9;
		periodRanges[1][1] = 2000;
		boolean[] periodLogs = { false, true };
		makePlot(writeDir, "period_all_log", display, randomized,
				allPeriodFuncs, allPeriodChars, " Inter-Event Times", "Years", "Number", periodRanges, periodLogs, dimensions);
		
		periodLogs[1] = false;
		periodRanges[1][0] = 0;
		makePlot(writeDir, "period_all", display, randomized,
				allPeriodFuncs, allPeriodChars, " Inter-Event Times", "Years", "Number", periodRanges, periodLogs, dimensions);
		
		
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
		
		String prefix = "event_times";
		if (rupIdens.size() < 3)
			prefix += "_subset";
		
		makePlot(writeDir, prefix, displayEventTimes, randomized, funcs, chars, "Event Times");
		
		List<int[]> xRanges = Lists.newArrayList();
		xRanges.add(toIntArray(0, 5000));
		xRanges.add(toIntArray(0, 10000));
		xRanges.add(toIntArray(5000, 10000));
		xRanges.add(toIntArray(200000, 205000));
		xRanges.add(toIntArray(205000, 210000));
		xRanges.add(toIntArray(210000, 215000));
		xRanges.add(toIntArray(215000, 220000));
		xRanges.add(toIntArray(220000, 225000));
		
		colors = Lists.newArrayList(colors);
		colors.add(duplicateColor);
		
		for (int[] xRange : xRanges) {
			double[][] ranges = new double[2][2];
			ranges[0][0] = xRange[0];
			ranges[0][1] = xRange[1];
			ranges[1][0] = 6.75;
			ranges[1][1] = 8;
			
			// need to do this as lines to make it readible, smallest on top
			
			List<HashSet<ArbitrarilyDiscretizedFunc>> funcSets = Lists.newArrayList();
			
			for (DiscretizedFunc func : funcs) {
				HashSet<ArbitrarilyDiscretizedFunc> subFuncs = new HashSet<ArbitrarilyDiscretizedFunc>();
				
				for (Point2D pt : func) {
					if (pt.getX() < ranges[0][0])
						continue;
					if (pt.getX() > ranges[0][1])
						break;
					ArbitrarilyDiscretizedFunc subFunc = new ArbitrarilyDiscretizedFunc();
					subFunc.set(pt.getX(), 6d);
					subFunc.set(pt.getX()+1e-10, pt.getY());
					subFuncs.add(subFunc);
				}
				
				funcSets.add(subFuncs);
			}
			
			ArrayList<ArbitrarilyDiscretizedFunc> subFuncs = Lists.newArrayList();
			ArrayList<PlotCurveCharacterstics> subChars = Lists.newArrayList();
			
			for (HashSet<ArbitrarilyDiscretizedFunc> set : funcSets)
				subFuncs.addAll(set);
			
			Collections.sort(subFuncs, new Comparator<ArbitrarilyDiscretizedFunc>() {

				@Override
				public int compare(ArbitrarilyDiscretizedFunc o1,
						ArbitrarilyDiscretizedFunc o2) {
					double y1 = o1.getY(1);
					double y2 = o2.getY(1);
					return -Double.compare(y1, y2);
				}
			});
			
			funcLoop:
			for (ArbitrarilyDiscretizedFunc func : subFuncs) {
				for (int i=0; i<funcSets.size(); i++) {
					HashSet<ArbitrarilyDiscretizedFunc> set = funcSets.get(i);
					if (set.contains(func)) {
						subChars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 3f, colors.get(i)));
						continue funcLoop;
					}
				}
				throw new IllegalStateException("Func not found in any set!");
			}
			
			makePlot(writeDir, prefix+"_"+xRange[0]+"_"+xRange[1], displayEventTimes, randomized,
					subFuncs, subChars, "Event Times", "Years", "Magnitude", ranges, null, dimensions);
			
			double[][] labelRange = new double[2][2];
			labelRange[0][0] = xRange[0];
			labelRange[0][1] = xRange[1];
			labelRange[1][0] = 0.5;
			labelRange[1][1] = labelFuncs.size()+0.5;
			
			int[] labelDims = { 1000, 300 };
			
			makePlot(writeDir, prefix+"_labels_"+xRange[0]+"_"+xRange[1], displayEventTimes, randomized,
					labelFuncs, labelChars, "Event Times", "Years", "Magnitude", labelRange, null, labelDims);
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
		HashSet<EQSIM_Event> matches1set = new HashSet<EQSIM_Event>(matches1);
		HashSet<EQSIM_Event> matches2set = new HashSet<EQSIM_Event>(matches2);
		
		int numWithin5years = 0;
		int totalNum = 0;
		int numCoruptures = 0;
		
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
			
			totalNum++;
			if (absMin <= 10d)
				numWithin5years++;
			if (absMin == 0d)
				numCoruptures++;
		}
		
		if (!randomized) {
			double percentWithin5 = (double)numWithin5years / (double)totalNum;
			percentWithin5 *= 100;
			System.out.println(name1+" to "+name2+": "+(float)percentWithin5+" % within 5 years");
			double percentCorupture = (double)numCoruptures / (double)totalNum;
			percentCorupture *= 100;
			System.out.println(name1+" to "+name2+": "+(float)percentCorupture+" % co-ruptures");
			double percentWithin5NonCorupture = (double)(numWithin5years - numCoruptures) / (double)totalNum;
			percentWithin5NonCorupture *= 100;
			System.out.println(name1+" to "+name2+": "+(float)percentWithin5NonCorupture+" % within 5 years no co-rupture");
//			double timeIndepExpectedWithin = 100d * ((double)(matches1.size() - numCoruptures) * 10d
//					/ General_EQSIM_Tools.getSimulationDurationYears(events));
//			System.out.println(name1+" to "+name2+": "+(float)timeIndepExpectedWithin+" % within 5 years no co-rupture (RANDOM)");
		}
		
		double[][] ranges = new double[2][2];
		ranges[0][0] = 0;
		ranges[0][1] = maxX;
		ranges[1][0] = 0;
		ranges[1][1] = 3500;
		
		String plotTitle = "Inter-Event Times From "+name1+" to "+name2;
		ArrayList<DiscretizedFunc> funcs = Lists.newArrayList();
		funcs.add(hist);
//		funcs.add(getCmlGreaterOrEqual(hist));
		ArrayList<PlotCurveCharacterstics> chars = Lists.newArrayList(
				new PlotCurveCharacterstics(PlotLineType.HISTOGRAM, 5f, Color.BLACK));
		chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 1f, duplicateColor));
		makePlot(writeDir, "inter_event_"+getFileSafeString(name1)+"_to_"+getFileSafeString(name2),
				display, randomized, funcs, chars, plotTitle, null, null, ranges, null);
		
		plotTitle = "Absolute Inter-Event Times From "+name1+" to "+name2;
		funcs = Lists.newArrayList();
		funcs.add(absHist);
		makePlot(writeDir, "inter_event_abs_"+getFileSafeString(name1)+"_to_"+getFileSafeString(name2),
				display, randomized, funcs, chars, plotTitle, null, null, ranges, null);
		
		HistogramFunction matrixHist = new HistogramFunction(-1000, 200, 10d);
		double matHistMin = matrixHist.getMinX() - 5d;
		double matHistMax = matrixHist.getMaxX() + 5d;
		HistogramFunction absMatrixHist = new HistogramFunction(5d, 100, 10d);
		HistogramFunction absBothMatrixHist = new HistogramFunction(5d, 100, 10d);
		
		for (EQSIM_Event event1 : matches1) {
			double timeYears1 = event1.getTimeInYears();
			for (EQSIM_Event event2 : matches2) {
				double timeYears2 = event2.getTimeInYears();
				
				double timeDelta = timeYears1 - timeYears2;
				double absDelta = Math.abs(timeDelta);
				
				if (timeDelta >= matHistMin && timeDelta <= matHistMax)
					matrixHist.add(timeDelta, 1d);
				if (absDelta <= matHistMax) {
					absMatrixHist.add(absDelta, 1d);
					if (matches1set.contains(event2) && matches2set.contains(event1))
						absBothMatrixHist.add(absDelta, 1d);
				}
			}
		}
		
		double[][] allRanges = new double[2][2];
		
		allRanges[0][0] = 0;
		allRanges[0][1] = 1000;
		allRanges[1][0] = 0;
		allRanges[1][1] = absMatrixHist.getMaxY()*1.1d;
		
		plotTitle = "Inter-Event Times From All "+name1+" to All "+name2;
		funcs = Lists.newArrayList();
		funcs.add(matrixHist);
//		funcs.add(getCmlGreaterOrEqual(hist));
		chars = Lists.newArrayList(
				new PlotCurveCharacterstics(PlotLineType.HISTOGRAM, 5f, Color.BLACK));
		makePlot(writeDir, "all_inter_event_"+getFileSafeString(name1)+"_to_"+getFileSafeString(name2),
				display, randomized, funcs, chars, plotTitle, "Years", "Number", null, null);
		
		plotTitle = "Absolute Inter-Event Times From All "+name1+" to All "+name2;
		funcs = Lists.newArrayList();
		funcs.add(absMatrixHist);
		funcs.add(absBothMatrixHist);
		chars.add(new PlotCurveCharacterstics(PlotLineType.HISTOGRAM, 5f, duplicateColor));
		makePlot(writeDir, "all_inter_event_abs_"+getFileSafeString(name1)+"_to_"+getFileSafeString(name2),
				display, randomized, funcs, chars, plotTitle, "Years", "Number", allRanges, null);
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
		
		HistogramFunction hist = new HistogramFunction(5d, 20, 10d);
		
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
	
	private static void makeMultiRecurrPlots(File dir, boolean display, double mag,
			List<EQSIM_Event> events, List<RuptureIdentifier> rupIdens, List<String> idenNames)
					throws IOException {
		HashSet<EQSIM_Event> matches = new HashSet<EQSIM_Event>();
		
		for (RuptureIdentifier rupIden : rupIdens)
			matches.addAll(rupIden.getMatches(events));
		
		List<EQSIM_Event> matchesList = Lists.newArrayList(matches);
		Collections.sort(matchesList);
		
		double delta = 2.5d;
		double min = delta * 0.5d;
		int num = (int)(500d / delta);
		HistogramFunction hist = new HistogramFunction(min, num, delta);
		
		double maxDelta = hist.getMaxX() + (delta * 0.5d);
		
		double prevTime = matchesList.get(0).getTimeInYears();
		
		double cnt = 0d;
		for (int i=1; i<matchesList.size(); i++) {
			double time = matchesList.get(i).getTimeInYears();
			double timeDelta = time - prevTime;
			
			if (timeDelta <= maxDelta) {
				hist.add(timeDelta, 1d);
				cnt += 1;
			}
			
			prevTime = time;
		}
		
		hist.scale(1d / cnt);
		
		ArrayList<DiscretizedFunc> funcs = Lists.newArrayList();
		funcs.add(hist);
		ArrayList<PlotCurveCharacterstics> chars = Lists.newArrayList();
		chars.add(new PlotCurveCharacterstics(PlotLineType.HISTOGRAM, 2f, Color.RED));
		
		String title = Joiner.on(", ").join(idenNames)+" M"+(float)mag+"+";
		String xAxisLabel = "Interevent Time (years)";
		String yAxisLabel = null;
		
		String prefix = null;
		for (String idenName : idenNames) {
			if (prefix == null)
				prefix = "";
			else
				prefix += "_";
			prefix += getFileSafeString(idenName);
		}
		prefix = "interevent_"+prefix+"_"+(float)mag+"+";
		
		makePlot(dir, prefix, display, false, funcs, chars, title, xAxisLabel, yAxisLabel,
				null, null);
	}
	
	private static void plotConditionalProbs(File dir, boolean display,
			List<EQSIM_Event> events, List<EQSIM_Event> randomizedEvents, RuptureIdentifier targetIden, String targetName,
			RuptureIdentifier givenIden, String givenName, double maxTimeYears, boolean includeInitialCorupture)
					throws IOException {
		ArbitrarilyDiscretizedFunc cumulativeFunc = getCumulativeProbDist(
				events, targetIden, givenIden, maxTimeYears,
				includeInitialCorupture);
		cumulativeFunc.setName("Cumulative Probabilities");
		ArbitrarilyDiscretizedFunc randCumulativeFunc = getCumulativeProbDist(
				randomizedEvents, targetIden, givenIden, maxTimeYears,
				includeInitialCorupture);
		randCumulativeFunc.setName("Cumulative Probabilities (Randomized Catalog)");
		
		ArrayList<DiscretizedFunc> funcs = Lists.newArrayList();
		funcs.add(cumulativeFunc);
		funcs.add(randCumulativeFunc);
		ArrayList<PlotCurveCharacterstics> chars = Lists.newArrayList();
		chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, Color.BLACK));
		chars.add(new PlotCurveCharacterstics(PlotLineType.DASHED, 1f, Color.GRAY));
		
		String title;
		if (includeInitialCorupture)
			title = "Prob("+targetName+"|"+givenName+") incl Initial Co-rupture";
		else
			title = "Prob("+targetName+"|"+givenName+") excl Initial Co-rupture";
		String xAxisLabel = "Time (years)";
		String yAxisLabel = "Cumulative Probability";
		
		String prefix;
		if (includeInitialCorupture)
			prefix = "cumulative_prob_"+getFileSafeString(targetName)+"_given_"+getFileSafeString(givenName);
		else
			prefix = "cumulative_prob_no_initial_"+getFileSafeString(targetName)+"_given_"+getFileSafeString(givenName);
		
		double[][] ranges = new double[2][2];
		ranges[0][0] = 0d;
		ranges[0][1] = maxTimeYears;
		if (maxTimeYears <= 20) {
			ranges[1][0] = 0d;
			ranges[1][1] = 0.25;
		} else {
			ranges[1][0] = 0d;
			ranges[1][1] = 1d;
		}
		
		makePlot(dir, prefix, display, false, funcs, chars, title, xAxisLabel, yAxisLabel,
				ranges, null);
	}

	public static ArbitrarilyDiscretizedFunc getCumulativeProbDist(
			List<EQSIM_Event> events, RuptureIdentifier targetIden,
			RuptureIdentifier givenIden, double maxTimeYears,
			boolean includeInitialCorupture) {
		List<EQSIM_Event> targetMatches = targetIden.getMatches(events);
		List<EQSIM_Event> givenMatches = givenIden.getMatches(events);
		
		HashSet<Integer> coruptures = null;
		if (!includeInitialCorupture) {
			coruptures = new HashSet<Integer>();
			for (EQSIM_Event e1 : targetMatches)
				for (EQSIM_Event e2 : givenMatches)
					if (e1.getID() == e2.getID())
						coruptures.add(e1.getID());
		}
		
		ArbitrarilyDiscretizedFunc timeFunc = new ArbitrarilyDiscretizedFunc();
		
		int targetStartIndex = 0;
		
		double yVal;
		if (includeInitialCorupture)
			yVal = 1d/(double)givenMatches.size();
		else
			yVal = 1d/(double)(givenMatches.size() - coruptures.size());
		
		for (EQSIM_Event given : givenMatches) {
			double givenTime = given.getTimeInYears();
			double targetMaxTime = givenTime + maxTimeYears;
			if (!includeInitialCorupture && coruptures.contains(given.getID()))
				continue;
			for (int i=targetStartIndex; i<targetMatches.size(); i++) {
				EQSIM_Event target = targetMatches.get(i);
				double targetTime = target.getTimeInYears();
				if (targetTime < givenTime) {
					targetStartIndex = i;
					continue;
				}
				if (targetTime > targetMaxTime)
					break;
				double deltaTime = targetTime - givenTime;
				int xIndex = timeFunc.getXIndex(deltaTime);
				if (xIndex < 0)
					timeFunc.set(deltaTime, yVal);
				else
					timeFunc.set(xIndex, yVal+timeFunc.getY(xIndex));
				// we only want the first occurrence as we're doing cumulative probabilities
				break;
			}
		}
		
		ArbitrarilyDiscretizedFunc cumulativeFunc = new ArbitrarilyDiscretizedFunc();
		
		double cumulativeRate = 0;
		for (int i=0; i<timeFunc.getNum(); i++) {
			double x = timeFunc.getX(i);
			cumulativeRate += timeFunc.getY(i);
			cumulativeFunc.set(x, cumulativeRate);
		}
		return cumulativeFunc;
	}
	
	private static void makePlot(File dir, String prefix, boolean display, boolean randomized,
			ArrayList<? extends DiscretizedFunc> funcs, ArrayList<PlotCurveCharacterstics> chars, String plotTitle)
					throws IOException {
		makePlot(dir, prefix, display, randomized, funcs, chars, plotTitle, null, null, null, null);
	}
	
	private static void makePlot(File dir, String prefix, boolean display, boolean randomized,
			ArrayList<? extends DiscretizedFunc> funcs, ArrayList<PlotCurveCharacterstics> chars, String plotTitle,
			String xAxisLabel, String yAxisLabel, double[][] ranges, boolean[] logs) throws IOException {
		makePlot(dir, prefix, display, randomized, funcs, chars, plotTitle,
				xAxisLabel, yAxisLabel, ranges, logs, null);
	}
	
	private static void makePlot(File dir, String prefix, boolean display, boolean randomized,
			ArrayList<? extends DiscretizedFunc> funcs, ArrayList<PlotCurveCharacterstics> chars, String plotTitle,
			String xAxisLabel, String yAxisLabel, double[][] ranges, boolean[] logs, int[] dimensions) throws IOException {
		if (randomized) {
			plotTitle = "RANDOMIZED "+plotTitle;
			prefix = prefix+"_randomized";
		}
		
		String fileName = new File(dir, prefix).getAbsolutePath();
		
		if (display) {
			GraphiWindowAPI_Impl gw = new GraphiWindowAPI_Impl(funcs, plotTitle, chars, display);
			if (dimensions == null)
				gw.getGraphWindow().setSize(600, 800);
			else
				gw.getGraphWindow().setSize(dimensions[0], dimensions[1]);
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
			
			gw.getGraphWindow().getGraphPanel().setBackgroundColor(Color.WHITE);
			gw.setTickLabelFontSize(18);
			gw.setAxisLabelFontSize(20);
			gw.setPlotLabelFontSize(21);
			
			gw.saveAsPNG(fileName+".png");
			gw.saveAsPDF(fileName+".pdf");
		} else {
			HeadlessGraphPanel gp = new HeadlessGraphPanel();
			if (ranges != null) {
				gp.setUserBounds(ranges[0][0], ranges[0][1], ranges[1][0], ranges[1][1]);
			}
			if (logs != null) {
				gp.setXLog(logs[0]);
				gp.setYLog(logs[1]);
			}
			gp.setBackgroundColor(Color.WHITE);
			gp.setTickLabelFontSize(18);
			gp.setAxisLabelFontSize(20);
			gp.setPlotLabelFontSize(21);
			gp.drawGraphPanel(xAxisLabel, yAxisLabel, funcs, chars, ranges != null, plotTitle);
			if (dimensions == null)
				gp.getCartPanel().setSize(1000, 800);
			else
				gp.getCartPanel().setSize(dimensions[0], dimensions[1]);
			gp.saveAsPNG(fileName+".png");
			gp.saveAsPDF(fileName+".pdf");
		}
		
	}
	
	static String getFileSafeString(String str) {
		return str.replaceAll("\\W+", "_");
	}
	
	public static int[] toIntArray(int... ints) {
		return ints;
	}

}
