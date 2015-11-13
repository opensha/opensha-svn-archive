package scratch.kevin.simulators.momRateVariation;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.stat.StatUtils;
import org.jfree.data.Range;
import org.opensha.commons.data.Site;
import org.opensha.commons.data.TimeSpan;
import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.commons.data.function.DefaultXY_DataSet;
import org.opensha.commons.data.function.DiscretizedFunc;
import org.opensha.commons.data.function.HistogramFunction;
import org.opensha.commons.data.function.UncertainArbDiscDataset;
import org.opensha.commons.data.region.CaliforniaRegions;
import org.opensha.commons.data.siteData.OrderedSiteDataProviderList;
import org.opensha.commons.data.siteData.SiteDataValue;
import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.LocationList;
import org.opensha.commons.gui.plot.GraphWindow;
import org.opensha.commons.gui.plot.PlotCurveCharacterstics;
import org.opensha.commons.gui.plot.PlotElement;
import org.opensha.commons.gui.plot.PlotLineType;
import org.opensha.commons.gui.plot.PlotSpec;
import org.opensha.commons.gui.plot.PlotSymbol;
import org.opensha.commons.param.Parameter;
import org.opensha.commons.util.DataUtils;
import org.opensha.commons.util.FaultUtils;
import org.opensha.sha.calc.HazardCurveCalculator;
import org.opensha.sha.calc.hazardMap.HazardCurveSetCalculator;
import org.opensha.sha.earthquake.AbstractERF;
import org.opensha.sha.earthquake.ProbEqkRupture;
import org.opensha.sha.earthquake.ProbEqkSource;
import org.opensha.sha.earthquake.param.IncludeBackgroundOption;
import org.opensha.sha.earthquake.param.IncludeBackgroundParam;
import org.opensha.sha.earthquake.param.ProbabilityModelOptions;
import org.opensha.sha.earthquake.param.ProbabilityModelParam;
import org.opensha.sha.faultSurface.RuptureSurface;
import org.opensha.sha.gui.infoTools.IMT_Info;
import org.opensha.sha.imr.ScalarIMR;
import org.opensha.sha.imr.attenRelImpl.NGAWest_2014_Averaged_AttenRel;
import org.opensha.sha.imr.param.IntensityMeasureParams.SA_Param;
import org.opensha.sha.simulators.EQSIM_Event;
import org.opensha.sha.simulators.EventRecord;
import org.opensha.sha.simulators.RectangularElement;
import org.opensha.sha.simulators.iden.RegionIden;
import org.opensha.sha.simulators.iden.RuptureIdentifier;
import org.opensha.sha.simulators.utils.General_EQSIM_Tools;
import org.opensha.sha.util.SiteTranslator;

import scratch.UCERF3.FaultSystemRupSet;
import scratch.UCERF3.FaultSystemSolution;
import scratch.UCERF3.erf.FaultSystemSolutionERF;
import scratch.UCERF3.utils.MatrixIO;
import scratch.kevin.simulators.SimAnalysisCatLoader;
import scratch.kevin.simulators.erf.SimulatorFaultSystemSolution;
import scratch.kevin.simulators.erf.SubSectionBiulder;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.primitives.Doubles;

public class MomRateVarHazardCalc {

	public static void main(String[] args) throws IOException {
		File outputDir = new File("/tmp/mom_rate_hazard");
		Preconditions.checkState(outputDir.exists() || outputDir.mkdir());
		
		// don't use idens in loading, but rather
		List<RuptureIdentifier> loadIdens = Lists.newArrayList();
		// only so cal ruptures
		loadIdens.add(new RegionIden(new CaliforniaRegions.RELM_SOCAL()));
		SimAnalysisCatLoader loader = new SimAnalysisCatLoader(true, loadIdens, false);
		List<EQSIM_Event> events = loader.getEvents();
		List<RectangularElement> elements = loader.getElements();
		
//		int windowLen = 75;
//		int windowLen = 25;
//		boolean before = false;
		
		int windowLen = 100;
		boolean before = true;
		
		double[] taper;
		if (before)
			taper = SimulatorMomRateVarCalc.getOnlyBeforeWindowTaper(windowLen);
		else
			taper = SimulatorMomRateVarCalc.buildHanningTaper(windowLen);
		
		double startYear = events.get(0).getTimeInYears() + windowLen;
		double endYear = events.get(events.size()-1).getTimeInYears();
		
		List<Double> yearsList = Lists.newArrayList();
		for (double year=startYear+windowLen; year<endYear-windowLen; year+=1d)
			yearsList.add(year);
		double[] years = Doubles.toArray(yearsList);
		double[] momRates;
		
		String beforeStr = "";
		if (before)
			beforeStr = "_before";
		File momRateFile = new File(outputDir,
				"mom_rates"+"_taper"+windowLen+"yr"+beforeStr+"_"+years.length+"yrs.bin");
		if (momRateFile.exists()) {
			momRates = MatrixIO.doubleArrayFromFile(momRateFile);
			Preconditions.checkState(momRates.length == years.length);
		} else {
			momRates = SimulatorMomRateVarCalc.calcTaperedMomRates(events, years, taper);
			MatrixIO.doubleArrayToFile(momRates, momRateFile);
		}
		if (before) {
			int startIndex = 0;
			ArbitrarilyDiscretizedFunc origFunc = new ArbitrarilyDiscretizedFunc();
			ArbitrarilyDiscretizedFunc newFunc = new ArbitrarilyDiscretizedFunc();
			for (int i=0; i<years.length; i++) {
				double endTime = years[i];
				double startTime = endTime-windowLen;
				
				if (i < 1000)
					origFunc.set(endTime, momRates[i]);
				
				momRates[i] = 0;
				
				for (int j=startIndex; j<events.size(); j++) {
					EQSIM_Event e = events.get(j);
					double t = e.getTimeInYears();
					if (t < startTime) {
						startIndex = j;
						continue;
					}
					if (t > endTime)
						break;
					for (EventRecord rec : e)
						momRates[i] += rec.getMoment();
				}
				
				momRates[i] /= windowLen;
				if (i < 1000)
					newFunc.set(endTime, momRates[i]);
			}
//			List<PlotElement> funcs = Lists.newArrayList();
//			List<PlotCurveCharacterstics> chars = Lists.newArrayList();
//			funcs.add(origFunc);
//			chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, Color.BLACK));
//			funcs.add(newFunc);
//			chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, Color.BLUE));
//			DefaultXY_DataSet xy = new DefaultXY_DataSet();
//			for (EQSIM_Event e : events) {
//				if (e.getMagnitude() < 7d)
//					continue;
//				double t = e.getTimeInYears();
//				if (t < years[0])
//					continue;
//				if (t > years[1000])
//					break;
//				xy.set(t, (e.getMagnitude()-6)*1e19);
//			}
//			funcs.add(xy);
//			chars.add(new PlotCurveCharacterstics(PlotSymbol.CROSS, 5f, Color.RED));
//			new GraphWindow(funcs, "Test", chars);
		}
		
//		double[] hazard_durations = { 5d, 30d, 50d, 75d };
//		double hazardMinMag = 5.5d;
//		doHazardCalc(outputDir, events, elements, hazard_durations,
//				hazardMinMag, years, momRates);
		
		double[] durations = { 1d, 5d, 10d, 15d, 30d, 50d, 100d };
//		doEventRateCalc(events, years, momRates, 7d, durations);
		
		doMomRateCalc(events, years, momRates, durations, windowLen);
	}

	private static void doHazardCalc(File outputDir, List<EQSIM_Event> events,
			List<RectangularElement> elements, double[] hazard_durations,
			double hazardMinMag, double[] years, double[] momRates)
			throws IOException {
		double upperMomRate = 1.5e19;
		double lowerMomRate = 9e18;
		int numAbove = 0;
		int numBelow = 0;
		int numBetween = 0;
		int numYears = 0;
		
		State[] states = new State[years.length];
		
		for (int i=0; i<years.length; i++) {
			if (momRates[i] > upperMomRate) {
				numAbove++;
				states[i] = State.ABOVE;
			} else if (momRates[i] < lowerMomRate) {
				numBelow++;
				states[i] = State.BELOW;
			} else {
				numBetween++;
				states[i] = State.BETWEEN;
			}
			numYears++;
		}
		System.out.println(numAbove+"/"+numYears+" ("+(float)(100d*numAbove/(double)numYears)+" %) above");
		System.out.println(numBelow+"/"+numYears+" ("+(float)(100d*numBelow/(double)numYears)+" %) below");
		System.out.println(numBetween+"/"+numYears+" ("+(float)(100d*numBetween/(double)numYears)+" %) between");
		
		// now build sub catalogs, use the midpoint in each span below/above/between
		int curStartIndex = 0;
		
		Map<State, List<Integer>> windowCenters = Maps.newHashMap();
		windowCenters.put(State.ABOVE, new ArrayList<Integer>());
		windowCenters.put(State.BELOW, new ArrayList<Integer>());
		windowCenters.put(State.BETWEEN, new ArrayList<Integer>());
		
		State curState = states[0];
		List<Double> curVals = Lists.newArrayList();
		
		for (int i=0; i<states.length; i++) {
			if (states[i] != curState) {
//				int endIndex = i-1;
//				int center = (endIndex+curStartIndex)/2;
				int center = curState.getCentralIndex(curVals)+curStartIndex;
				windowCenters.get(curState).add(center);
				
				curState = states[i];
				curStartIndex = i;
				curVals.clear();;
			}
			curVals.add(momRates[i]);
		}
		
		SubSectionBiulder subSectBuilder = new SubSectionBiulder(elements);
		
		Site site = new Site(new Location(34.055, -118.2467)); // LA Civic Center
		ScalarIMR imr = new NGAWest_2014_Averaged_AttenRel(null, false);
		imr.setParamDefaults();
		imr.setIntensityMeasure(SA_Param.NAME);
		SA_Param.setPeriodInSA_Param(imr.getIntensityMeasure(), 1d);
		OrderedSiteDataProviderList provs = OrderedSiteDataProviderList.createSiteDataProviderDefaults();
		ArrayList<SiteDataValue<?>> datas = provs.getBestAvailableData(site.getLocation());
		SiteTranslator trans = new SiteTranslator();
		for (Parameter<?> param : imr.getSiteParams()) {
			trans.setParameterValue(param, datas);
			site.addParameter(param);
		}
		DiscretizedFunc xVals = new IMT_Info().getDefaultHazardCurve(imr.getIntensityMeasure());
		
		List<EQSIM_Event> hazardEvents = Lists.newArrayList();
		for (EQSIM_Event event : events)
			if (event.getMagnitude() >= hazardMinMag)
				hazardEvents.add(event);
		
		for (double duration : hazard_durations) {
			List<DiscretizedFunc> funcs = Lists.newArrayList();
			List<PlotCurveCharacterstics> chars = Lists.newArrayList();
			for (State state : State.values()) {
				System.out.println("Calculating "+(int)duration+"yr, "+state);
				SimulatorFaultSystemSolution fss = buildFSS(
						events, subSectBuilder, hazardMinMag, windowCenters.get(state), years, duration);
				DiscretizedFunc func = calcHazardCurve(fss, duration, site, imr, xVals);
				func.setName(state.name());
				funcs.add(func);
				chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, state.c));
			}
//			// now full catalog
//			SimulatorFaultSystemSolution fss = SimulatorFaultSystemSolution.build(
//					subSectBuilder, hazardEvents, totalDurationYears);
//			DiscretizedFunc func = calcHazardCurve(fss, duration, site, imr, xVals);
//			func.setName("Full Catalog");
//			funcs.add(0, func);
//			chars.add(0, new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, Color.BLACK));
			
			PlotSpec spec = new PlotSpec(funcs, chars, (int)duration+"yr Hazard Curves",
					"1s SA", "Exceed Prob");
			spec.setLegendVisible(true);
			GraphWindow gw = new GraphWindow(spec);
			gw.setXLog(true);
			gw.setYLog(true);
			gw.setAxisRange(new Range(1e-2, 3), new Range(1e-4, 1));
			gw.saveAsPNG(new File(outputDir, "curves_"+(int)duration+"yr.png").getAbsolutePath());
		}
		
//		FaultSystemRupSet rupSet = buildRupSet(elements, events, totalDurationYears, subSectBuilder);
	}
	
	private static enum State {
		ABOVE(Color.RED),
		BELOW(Color.BLUE),
		BETWEEN(Color.GREEN);
		
		private Color c;
		
		private State(Color c) {
			this.c = c;
		}
		
		private int getCentralIndex(List<Double> values) {
			Preconditions.checkArgument(!values.isEmpty());
			
			switch (this) {
			case ABOVE:
				// find max value
				int maxIndex = -1;
				double maxVal = 0;
				for (int i=0; i<values.size(); i++) {
					double v = values.get(i);
					if (v > maxVal) {
						maxVal = v;
						maxIndex = i;
					}
				}
				return maxIndex;
			case BELOW:
				// find min value
				int minIndex = -1;
				double minVal = Double.POSITIVE_INFINITY;
				for (int i=0; i<values.size(); i++) {
					double v = values.get(i);
					if (v < minVal) {
						minVal = v;
						minIndex = i;
					}
				}
				return minIndex;
			case BETWEEN:
				// find median
				double median = DataUtils.median(Doubles.toArray(values));
				int closestIndex = 0-1;
				double closestDelta = Double.POSITIVE_INFINITY;
				for (int i=0; i<values.size(); i++) {
					double delta = Math.abs(values.get(i)-median);
					if (delta < closestDelta) {
						closestDelta = delta;
						closestIndex = i;
					}
				}
				return closestIndex;

			default:
				throw new IllegalStateException("Unknown Satate");
			}
		}
	}
	
	private static SimulatorFaultSystemSolution buildFSS(
			List<EQSIM_Event> allEvents, SubSectionBiulder subSectBuilder, double minMag,
			List<Integer> windowCenters, double[] years, double forecastDuration) {
		
		List<EQSIM_Event> includedEvents = Lists.newArrayList();
		double includedDuration = 0;
		
		Preconditions.checkArgument(!windowCenters.isEmpty());
		
		for (int center : windowCenters) {
			double year = years[center];
			double yearSecs = year*General_EQSIM_Tools.SECONDS_PER_YEAR;
			double endYear = year + forecastDuration;
			
			int startIndex = SimulatorMomRateVarCalc.findFirstEventIndexAfter(yearSecs, allEvents);
			
			for (int i=startIndex; i<allEvents.size(); i++) {
				EQSIM_Event e = allEvents.get(i);
				double t = e.getTimeInYears();
				Preconditions.checkState(t >= year);
				if (t > endYear)
					break;
				if (e.getMagnitude() >= minMag)
					includedEvents.add(e);
			}
			includedDuration += forecastDuration;
		}
		
		System.out.println("Built SimFSS from "+includedEvents.size()+" events, dur="+includedDuration);
		
		return SimulatorFaultSystemSolution.build(subSectBuilder, includedEvents, includedDuration);
	}
	
	private static DiscretizedFunc calcHazardCurve(FaultSystemSolution fss, double duration,
			Site site, ScalarIMR imr, DiscretizedFunc xVals) {
		FaultSystemSolutionERF erf = new FaultSystemSolutionERF(fss);
		erf.setParameter(ProbabilityModelParam.NAME, ProbabilityModelOptions.POISSON);
		erf.setParameter(IncludeBackgroundParam.NAME, IncludeBackgroundOption.EXCLUDE);
		erf.getTimeSpan().setDuration(duration);
		erf.updateForecast();
		
		HazardCurveCalculator calc = new HazardCurveCalculator();
		
		DiscretizedFunc func = xVals.deepClone();
		func = HazardCurveSetCalculator.getLogFunction(func);
		
		calc.getHazardCurve(func, site, imr, erf);
		
		func = HazardCurveSetCalculator.unLogFunction(xVals, func);
		
		return func;
	}
	
	private static void doEventRateCalc(List<EQSIM_Event> events, double[] years,
			double[] moRates, double minMag, double[] durations) {
		List<DiscretizedFunc> funcs = Lists.newArrayList();
		List<PlotCurveCharacterstics> chars = Lists.newArrayList();
		
		List<Color> colors = GraphWindow.generateDefaultColors();
		int colorIndex = 0;
		
		for (double duration : durations) {
			DiscretizedFunc hist = calcEventRatesForMomRates(events, years, moRates, minMag, duration);
			hist.setName((int)duration+"yr");
			funcs.add(hist);
			if (colorIndex == colors.size())
				colorIndex = 0;
			chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, colors.get(colorIndex++)));
		}
		
		PlotSpec spec = new PlotSpec(funcs, chars, "Event Rates vs Moment Rates",
				"Moment Rate Before", "Rate M>="+(double)minMag+" Following");
		spec.setLegendVisible(true);
		GraphWindow gw = new GraphWindow(spec);
		gw.setXLog(true);
		gw.setDefaultCloseOperation(GraphWindow.EXIT_ON_CLOSE);
		
		// now gain func
		int countAbove = 0;
		for (EQSIM_Event e : events)
			if (e.getMagnitude() >= minMag)
				countAbove++;
		double simDuration = events.get(events.size()-1).getTimeInYears() - events.get(0).getTimeInYears();
		double rateAbove = (double)countAbove/simDuration;
		
		List<DiscretizedFunc> gainFuncs = Lists.newArrayList();
		for (int i=0; i<durations.length; i++) {
			double duration = durations[i];
			DiscretizedFunc countFunc = funcs.get(i);
			DiscretizedFunc gainFunc = new ArbitrarilyDiscretizedFunc();
			gainFunc.setName(countFunc.getName());
			
			for (Point2D pt : countFunc) {
				double count = pt.getY();
				double myRate = count/duration;
				double gain = myRate/rateAbove;
				gainFunc.set(pt.getX(), gain);
			}
			
			gainFuncs.add(gainFunc);
		}
		
		spec = new PlotSpec(gainFuncs, chars, "Event Rate Gain vs Moment Rates",
				"Moment Rate Before", "Rate Gain M>="+(double)minMag+" Following");
		spec.setLegendVisible(true);
		gw = new GraphWindow(spec);
		gw.setXLog(true);
		gw.setDefaultCloseOperation(GraphWindow.EXIT_ON_CLOSE);
	}
	
	private static ArbitrarilyDiscretizedFunc calcEventRatesForMomRates(
			List<EQSIM_Event> events, double[] years, double[] moRates, double minMag, double durationYears) {
		Preconditions.checkArgument(moRates.length == years.length);
		
		double maxMoRate = StatUtils.max(moRates);
		double minMoRate = StatUtils.min(moRates);
		Preconditions.checkState(minMoRate > 0d, "Cannot have mo rate of zero");
		
		// bin in Log10 space
		HistogramFunction hist = HistogramFunction.getEncompassingHistogram(
				Math.log10(minMoRate), Math.log10(maxMoRate), 0.1);
		int[] binCounts = new int[hist.size()];
		
		int startEventIndex = 0;
		
		for (int i=0; i<years.length; i++) {
			double moRate = moRates[i];
			int xIndex = hist.getClosestXIndex(Math.log10(moRate));
			binCounts[xIndex]++;
			
			double startYear = years[i];
			double endYear = startYear + durationYears;
			
			for (int j=startEventIndex; j<events.size(); j++) {
				EQSIM_Event e = events.get(j);
				double t = e.getTimeInYears();
				if (t < startYear) {
					startEventIndex = j;
					continue;
				}
				if (t > endYear)
					break;
				if (e.getMagnitude() >= minMag) {
					hist.add(xIndex, 1d);
				}
			}
		}
		
		for (int i=0; i<hist.size(); i++)
			if (binCounts[i] > 0)
				hist.set(i, hist.getY(i)/(double)binCounts[i]);
		
		// now go back to linear space
		ArbitrarilyDiscretizedFunc ret = new ArbitrarilyDiscretizedFunc();
		for (int i=0; i<hist.size(); i++)
			if (binCounts[i] >= 10)
				ret.set(Math.pow(10d, hist.getX(i)), hist.getY(i));
		
		return ret;
	}
	
	private static void doMomRateCalc(List<EQSIM_Event> events, double[] years,
			double[] moRates, double[] durations, int durationBefore) {
		List<DiscretizedFunc> funcs = Lists.newArrayList();
		List<PlotCurveCharacterstics> chars = Lists.newArrayList();
		
		List<Color> colors = GraphWindow.generateDefaultColors();
		int colorIndex = 0;
		
		for (double duration : durations) {
			DiscretizedFunc hist = calcMomRatesForMomRates(events, years, moRates, duration);
			hist.setName((int)duration+"yr");
			funcs.add(hist);
			if (colorIndex == colors.size())
				colorIndex = 0;
			chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, colors.get(colorIndex++)));
//			chars.add(new PlotCurveCharacterstics(PlotLineType.SHADED_UNCERTAIN_TRANS, 2f, colors.get(colorIndex++)));
		}
		
		PlotSpec spec = new PlotSpec(funcs, chars, "Moment Rate Before/After",
				"Moment Rate for "+durationBefore+"yrs Before", "Moment Rate Following");
		spec.setLegendVisible(true);
		GraphWindow gw = new GraphWindow(spec);
		gw.setXLog(true);
		gw.setDefaultCloseOperation(GraphWindow.EXIT_ON_CLOSE);
		
		List<DiscretizedFunc> gainFuncs = Lists.newArrayList();
		for (int i=0; i<durations.length; i++) {
			DiscretizedFunc countFunc = funcs.get(i);
			DiscretizedFunc gainFunc = new ArbitrarilyDiscretizedFunc();
			gainFunc.setName(countFunc.getName());
			
			for (Point2D pt : countFunc) {
				double gain = pt.getY()/pt.getX();
				gainFunc.set(pt.getX(), gain);
			}
			
			gainFuncs.add(gainFunc);
		}
		
		spec = new PlotSpec(gainFuncs, chars, "Event Rate Gain vs Moment Rates",
				"Moment Rate for "+durationBefore+"yrs Before", "Moment Rate Gain Following");
		spec.setLegendVisible(true);
		gw = new GraphWindow(spec);
		gw.setXLog(true);
		gw.setDefaultCloseOperation(GraphWindow.EXIT_ON_CLOSE);
	}
	
	private static UncertainArbDiscDataset calcMomRatesForMomRates(
			List<EQSIM_Event> events, double[] years, double[] moRates, double durationYears) {
		Preconditions.checkArgument(moRates.length == years.length);
		
		double maxMoRate = StatUtils.max(moRates);
		double minMoRate = StatUtils.min(moRates);
		Preconditions.checkState(minMoRate > 0d, "Cannot have mo rate of zero");
		
		// bin in Log10 space
		HistogramFunction hist = HistogramFunction.getEncompassingHistogram(
				Math.log10(minMoRate), Math.log10(maxMoRate), 0.1);
		List<List<Double>> momRateAfters = Lists.newArrayList();
		for (int i=0; i<hist.size(); i++)
			momRateAfters.add(new ArrayList<Double>());
		
		int startEventIndex = 0;
		
		for (int i=0; i<years.length; i++) {
			double moRate = moRates[i];
			int xIndex = hist.getClosestXIndex(Math.log10(moRate));
			
			double startYear = years[i];
			double endYear = startYear + durationYears;
			
			double momentAfter = 0d;
			
			for (int j=startEventIndex; j<events.size(); j++) {
				EQSIM_Event e = events.get(j);
				double t = e.getTimeInYears();
				if (t < startYear) {
					startEventIndex = j;
					continue;
				}
				if (t > endYear)
					break;
				for (EventRecord rec : e)
					momentAfter += rec.getMoment();
			}
//			hist.add(xIndex, momentAfter/durationYears);
			momRateAfters.get(xIndex).add(momentAfter/durationYears);
		}
//		
//		for (int i=0; i<hist.size(); i++)
//			if (binCounts[i] > 0)
//				hist.set(i, hist.getY(i)/(double)binCounts[i]);
//		
//		// now go back to linear space
//		ArbitrarilyDiscretizedFunc ret = new ArbitrarilyDiscretizedFunc();
//		for (int i=0; i<hist.size(); i++)
//			if (binCounts[i] >= 10)
//				ret.set(Math.pow(10d, hist.getX(i)), hist.getY(i));
//		
//		return ret;
		ArbitrarilyDiscretizedFunc meanFunc = new ArbitrarilyDiscretizedFunc();
		ArbitrarilyDiscretizedFunc lowerFunc = new ArbitrarilyDiscretizedFunc();
		ArbitrarilyDiscretizedFunc upperFunc = new ArbitrarilyDiscretizedFunc();
		
		for (int i=0; i<hist.size(); i++) {
			double x = Math.pow(10d, hist.getX(i));
			List<Double> vals = momRateAfters.get(i);
			if (vals.size() < 10)
				continue;
			
			double[] valsArray = Doubles.toArray(vals);
			
			double mean = StatUtils.mean(valsArray);
			double stdDev = Math.sqrt(StatUtils.variance(valsArray));
			
			meanFunc.set(x, mean);
			lowerFunc.set(x, mean - stdDev);
			upperFunc.set(x, mean + stdDev);
		}
		
		return new UncertainArbDiscDataset(meanFunc, lowerFunc, upperFunc);
	}

}
