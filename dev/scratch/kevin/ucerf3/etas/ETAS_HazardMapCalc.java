package scratch.kevin.ucerf3.etas;

import java.awt.Color;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.swing.SwingUtilities;

import org.apache.commons.math3.stat.StatUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.opensha.commons.data.Site;
import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.commons.data.function.DiscretizedFunc;
import org.opensha.commons.data.region.CaliforniaRegions;
import org.opensha.commons.data.siteData.OrderedSiteDataProviderList;
import org.opensha.commons.data.siteData.SiteData;
import org.opensha.commons.data.siteData.SiteDataValue;
import org.opensha.commons.data.siteData.SiteDataValueList;
import org.opensha.commons.data.xyz.GriddedGeoDataSet;
import org.opensha.commons.exceptions.GMT_MapException;
import org.opensha.commons.geo.GriddedRegion;
import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.LocationList;
import org.opensha.commons.geo.LocationUtils;
import org.opensha.commons.geo.Region;
import org.opensha.commons.gui.plot.GraphWindow;
import org.opensha.commons.gui.plot.HeadlessGraphPanel;
import org.opensha.commons.gui.plot.PlotCurveCharacterstics;
import org.opensha.commons.gui.plot.PlotLineType;
import org.opensha.commons.gui.plot.PlotSpec;
import org.opensha.commons.mapping.gmt.GMT_Map;
import org.opensha.commons.mapping.gmt.elements.GMT_CPT_Files;
import org.opensha.commons.param.Parameter;
import org.opensha.commons.util.ExceptionUtils;
import org.opensha.commons.util.XMLUtils;
import org.opensha.commons.util.cpt.CPT;
import org.opensha.nshmp.NEHRP_TestCity;
import org.opensha.sha.calc.HazardCurveCalculator;
import org.opensha.sha.calc.hazardMap.BinaryHazardCurveReader;
import org.opensha.sha.calc.hazardMap.HazardDataSetLoader;
import org.opensha.sha.earthquake.ProbEqkRupture;
import org.opensha.sha.earthquake.ProbEqkSource;
import org.opensha.sha.earthquake.param.AleatoryMagAreaStdDevParam;
import org.opensha.sha.earthquake.param.ApplyGardnerKnopoffAftershockFilterParam;
import org.opensha.sha.earthquake.param.BPTAveragingTypeOptions;
import org.opensha.sha.earthquake.param.BPTAveragingTypeParam;
import org.opensha.sha.earthquake.param.BackgroundRupParam;
import org.opensha.sha.earthquake.param.BackgroundRupType;
import org.opensha.sha.earthquake.param.HistoricOpenIntervalParam;
import org.opensha.sha.earthquake.param.IncludeBackgroundOption;
import org.opensha.sha.earthquake.param.IncludeBackgroundParam;
import org.opensha.sha.earthquake.param.MagDependentAperiodicityOptions;
import org.opensha.sha.earthquake.param.MagDependentAperiodicityParam;
import org.opensha.sha.earthquake.param.ProbabilityModelOptions;
import org.opensha.sha.earthquake.param.ProbabilityModelParam;
import org.opensha.sha.gui.infoTools.IMT_Info;
import org.opensha.sha.imr.AttenRelRef;
import org.opensha.sha.imr.AttenuationRelationship;
import org.opensha.sha.imr.ScalarIMR;
import org.opensha.sha.imr.attenRelImpl.calc.Wald_MMI_Calc;
import org.opensha.sha.imr.param.IntensityMeasureParams.MMI_Param;
import org.opensha.sha.imr.param.IntensityMeasureParams.PGA_Param;
import org.opensha.sha.imr.param.IntensityMeasureParams.PGV_Param;
import org.opensha.sha.imr.param.IntensityMeasureParams.SA_Param;
import org.opensha.sha.util.SiteTranslator;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;
import com.google.common.collect.Table.Cell;
import com.google.common.primitives.Doubles;

import scratch.UCERF3.FaultSystemSolution;
import scratch.UCERF3.analysis.FaultBasedMapGen;
import scratch.UCERF3.erf.FaultSystemSolutionERF;
import scratch.UCERF3.erf.ETAS.ETAS_CatalogIO;
import scratch.UCERF3.erf.ETAS.ETAS_EqkRupture;
import scratch.UCERF3.erf.utils.ProbabilityModelsCalc;
import scratch.UCERF3.utils.FaultSystemIO;
import scratch.kevin.ucerf3.etas.ETAS_HazardMapCalc.MapType;

public class ETAS_HazardMapCalc {
	
	private static boolean force_serial = false;
	private static int debugCurvePlotModulus = 0;
	private static int debugStopIndex = 0;
	
	private boolean calcInLogSpace = true;
	private double distCutoff = 200;
	
	private List<List<ETAS_EqkRupture>> catalogs;
	private HashSet<Integer> faultIndexesTriggered;
	protected GriddedRegion region;
	private DiscretizedFunc xVals;
	private DiscretizedFunc calcXVals;
	
	private int printModulus = 100;
	
	// for precalc faults
	private DataInputStream in;
	int faultSiteIndex = 0;
	// for on the fly faults
	private FaultSystemSolutionERF faultERF;
	private ProbEqkSource[] sourcesForFSSRuptures;
	
	private ETAS_CatalogGridSourceProvider gridSources;
	private AttenRelRef gmpeRef;
	private String imtName;
	private double period;
	private Deque<ScalarIMR> gmpeDeque;
	
	private List<Site> sites;
	
	private Duration[] durations;
	private long minOT;
	private long[] maxOTs;
	
	protected Table<Duration, MapType, DiscretizedFunc[]> curves;
	private int curvesCalculated;
	
	enum MapType {
		FAULT_ONLY("faults"),
		GRIDDED_ONLY("gridded"),
		COMBINED("combined");
		
		final String fileName;
		private MapType(String fileName) {
			this.fileName = fileName;
		}
	}
	
	enum Duration {
		FULL("Full Catalog", "full", 0d),
		DAY("1 Day", "day", 1d/365.25),
		WEEK("1 Week", "week", 7d/365.25),
		MONTH("1 Month", "month", 1d/12d),
		YEAR("1 Year", "year", 1d);
		
		final String plotName;
		final String fileName;
		final double years;
		private Duration(String plotName, String fileName, double years) {
			this.plotName = plotName;
			this.fileName = fileName;
			this.years = years;
		}
	}
	
	private boolean calcFaults;
	private boolean calcGridded;
	
	public ETAS_HazardMapCalc(List<List<ETAS_EqkRupture>> catalogs, GriddedRegion region, DiscretizedFunc xVals,
			File precalcFile, FaultSystemSolution sol, ETAS_CatalogGridSourceProvider gridSources,
			AttenRelRef gmpeRef, String imtName, double period, List<Site> sites, Duration[] durations) throws IOException {
		this.catalogs = catalogs;
		this.region = region;
		initXVals(xVals);
		this.gridSources = gridSources;
		this.gmpeRef = gmpeRef;
		this.imtName = imtName;
		this.period = period;
		this.sites = sites;
		
		if (durations == null || durations.length == 0)
			durations = new Duration[] { Duration.FULL };
		this.durations = durations;
		// claculate the min OT
		minOT = Long.MAX_VALUE;
		for (List<ETAS_EqkRupture> catalog : catalogs) {
			if (catalog.isEmpty())
				continue;
			long ot = catalog.get(0).getOriginTime();
			if (ot < minOT)
				minOT = ot;
		}
		maxOTs = new long[durations.length];
		for (int i=0; i<durations.length; i++)
			maxOTs[i] = minOT + (long)(durations[i].years*ProbabilityModelsCalc.MILLISEC_PER_YEAR);
		
		calcGridded = (gridSources != null && gmpeRef != null && imtName != null && sites != null);
		Preconditions.checkState(sites == null || sites.size() == region.getNodeCount());
		
		// this is used to conserve memory and only load results for ruptures actually used
		faultIndexesTriggered = new HashSet<Integer>();
		for (List<ETAS_EqkRupture> catalog : catalogs)
			for (ETAS_EqkRupture rup : catalog)
				if (rup.getFSSIndex() > 0)
					faultIndexesTriggered.add(rup.getFSSIndex());
		
		if (precalcFile != null) {
			// load in precalculated fault data
			in = new DataInputStream(new BufferedInputStream(new FileInputStream(precalcFile)));
			int numSites = in.readInt();
			Preconditions.checkState(numSites == region.getNodeCount(), "Binary file has %s grid nodes, region has %s",
					numSites, region.getNodeCount());
			calcFaults = true;
		} else if (sol != null) {
			// calculate faults on the fly
			Preconditions.checkNotNull(sites);
			faultERF = new FaultSystemSolutionERF(sol);
			faultERF.setParameter(IncludeBackgroundParam.NAME, IncludeBackgroundOption.EXCLUDE);
			faultERF.updateForecast();
			// organize by FSS index
			sourcesForFSSRuptures = new ProbEqkSource[sol.getRupSet().getNumRuptures()];
			for (int sourceID=0; sourceID<faultERF.getNumFaultSystemSources(); sourceID++) {
				int fssIndex = faultERF.getFltSysRupIndexForSource(sourceID);
				sourcesForFSSRuptures[fssIndex] = faultERF.getSource(sourceID);
			}
			calcFaults = true;
		}
	}
	
	public ETAS_HazardMapCalc(GriddedRegion region, Table<Duration, MapType, File> curveFiles)	throws Exception {
		this(region, curveFiles, null, null, null, Double.NaN, null);
	}
	
	public ETAS_HazardMapCalc(GriddedRegion region, Table<Duration, MapType, File> curveFiles,
			DiscretizedFunc xVals, AttenRelRef gmpeRef, String imtName, double period, List<Site> sites)	throws Exception {
		this.region = region;
		this.gmpeRef = gmpeRef;
		this.imtName = imtName;
		this.period = period;
		this.sites = sites;
		
		curves = HashBasedTable.create();
		for (Cell<Duration, MapType, File> cell : curveFiles.cellSet()) {
			Duration duration = cell.getRowKey();
			MapType type = cell.getColumnKey();
			System.out.println("Loading "+region.getNodeCount()+" curves for "+duration+", "+type);
			curves.put(duration, type, loadCurves(cell.getValue()));
		}
		
		Preconditions.checkArgument(!curves.isEmpty(), "Must supply at least one curve file");
		xVals = curves.values().iterator().next()[0];
		initXVals(xVals);
	}
	
	private void initXVals(DiscretizedFunc xVals) {
		this.xVals = xVals;
		if (calcInLogSpace) {
			calcXVals = new ArbitrarilyDiscretizedFunc();
			for (int i=0; i<xVals.size(); i++)
				calcXVals.set(Math.log(xVals.getX(i)), 1d);
		} else {
			calcXVals = xVals;
		}
	}
	
	private ETAS_HazardMapCalc() {}
	
	private DiscretizedFunc[] loadCurves(File curvesFile) throws Exception {
		BinaryHazardCurveReader reader = new BinaryHazardCurveReader(curvesFile.getAbsolutePath());
		
		DiscretizedFunc[] curves = new DiscretizedFunc[region.getNodeCount()];
		
		for (int i=0; i<curves.length; i++) {
			curves[i] = reader.nextLightCurve();
			Location loc = reader.currentLocation();
			Location gridLoc = region.getNodeList().get(i);
			Preconditions.checkState(loc.equals(gridLoc), "Unexpected grid location!\n\tFile: %s\n\tGrid: %s", loc, gridLoc);
			Preconditions.checkNotNull(curves[i]);
		}
		Preconditions.checkState(reader.nextCurve() == null, "More curves than expected!");
		
		return curves;
	}
	
	private ExecutorService createExecutor() {
		int threads = Runtime.getRuntime().availableProcessors();
		if (threads > region.getNodeCount())
			threads = region.getNodeCount();
		return createExecutor(threads);
	}
	
	ExecutorService createExecutor(int threads) {
//		ExecutorService executor = Executors.newFixedThreadPool(threads);
		// max tasks in the pool at any given time, prevents pre loading too much data and using all memory
		// while waiting for hazard calculations to finish. When the queue is full, it will be run in this
		// thread, effectively blocking
		int maxTasks = threads * 10;
		ExecutorService executor = new ThreadPoolExecutor(threads, threads,
                0L, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<Runnable>(maxTasks), new ThreadPoolExecutor.CallerRunsPolicy());
		return executor;
	}
	
	public boolean isCalcFaults() {
		return calcFaults;
	}

	public void setCalcFaults(boolean calcFaults) {
		this.calcFaults = calcFaults;
	}

	public boolean isCalcGridded() {
		return calcGridded;
	}

	public void setCalcGridded(boolean calcGridded) {
		this.calcGridded = calcGridded;
	}

	public void calculate() throws IOException {
		if (in != null)
			Preconditions.checkState(faultSiteIndex == 0, "Can only process file once");
		
		curves = HashBasedTable.create();
		
		if (calcFaults) {
			for (Duration duration : durations)
				curves.put(duration, MapType.FAULT_ONLY, new DiscretizedFunc[region.getNodeCount()]);
		}
		if (calcGridded) {
			for (Duration duration : durations)
				curves.put(duration, MapType.GRIDDED_ONLY, new DiscretizedFunc[region.getNodeCount()]);
		}
		if (calcFaults && calcGridded) {
			for (Duration duration : durations)
				curves.put(duration, MapType.COMBINED, new DiscretizedFunc[region.getNodeCount()]);
		}
		
		ExecutorService executor = createExecutor();
		
		List<Future<Integer>> hazardFutures = Lists.newArrayList();
		
		Stopwatch watch = Stopwatch.createStarted();
		System.out.println("Calculating");
		
		curvesCalculated = 0;
		
		for (int index=0; index<region.getNodeCount(); index++) {
			if (index % printModulus == 0)
				System.out.println("Processing site "+index+"/"+region.getNodeCount());
			
			Map<Integer, double[]> precomputedFaultVals = null;
			if (calcFaults && in != null) {
				Preconditions.checkState(faultSiteIndex == index);
				precomputedFaultVals = loadNextSite();
			}
			
			Future<Integer> future = null;
			HazardCalcRunnable runnable = new HazardCalcRunnable(index, precomputedFaultVals);
			if (force_serial) {
				runnable.run();
			} else {
				future = executor.submit(runnable, index);
				hazardFutures.add(future);
			}
			
			if (debugStopIndex > 0 && index == debugStopIndex)
				break;
			
			if (debugCurvePlotModulus > 0 && index % debugCurvePlotModulus == 0) {
				if (future == null)
					plotCurve(index);
				else
					plotCurve(future); // asynchronous
			}
		}
		
		// wait until we're done
		System.out.println("Waiting for hazard calculations to finish");
		for (Future<?> future : hazardFutures) {
			try {
				future.get();
			} catch (InterruptedException e) {
				ExceptionUtils.throwAsRuntimeException(e);
			} catch (ExecutionException e) {
				ExceptionUtils.throwAsRuntimeException(e);
			}
		}
		System.out.println("Done");
		long secs = watch.elapsed(TimeUnit.SECONDS);
		System.out.println("Calculation took "+secs+" secs");
		watch.stop();
		double curvesPerSecond = (double)curvesCalculated/(double)secs;
		System.out.println((float)curvesPerSecond+" curves/sec");
		
		executor.shutdown();
	}
	
	private class HazardCalcRunnable implements Runnable {
		
		private int index;
		private Map<Integer, double[]> precomputedFaultVals;
		
		public HazardCalcRunnable(int index, Map<Integer, double[]> precomputedFaultVals) {
			this.index = index;
			this.precomputedFaultVals = precomputedFaultVals;
		}

		@Override
		public void run() {
			Table<Duration, MapType, DiscretizedFunc> result = calculateCurves(sites.get(index), precomputedFaultVals);
			for (Cell<Duration, MapType, DiscretizedFunc> cell : result.cellSet()) {
				Duration duration = cell.getRowKey();
				MapType type = cell.getColumnKey();
				DiscretizedFunc curve = result.get(duration, type);
				Preconditions.checkState(curve != null, "Curve not calculated for %s, %s! Size=%s", duration, type, curves.size());
				curves.get(duration, type)[index] = cell.getValue();
			}
			
			curvesCalculated++;
			if (curvesCalculated % printModulus == 0)
				System.out.println("Calculated "+curvesCalculated+"/"+region.getNodeCount()+" sites");
		}
	}
	
	/**
	 * Calculates a catalog based hazard curves. First computes a conditional exceedence curve for each individual
	 * catalog, then computes a hazard curve across all catalogs by summing the exceedence probabilities scaled
	 * by 1/numCatalogs.
	 * 
	 * @param site
	 * @param precomputedFaultVals
	 * @return HazardCalcResult instance
	 */
	Table<Duration, MapType, DiscretizedFunc> calculateCurves(Site site, Map<Integer, double[]> precomputedFaultVals) {
		Table<Duration, MapType, DiscretizedFunc> curves = getInitializedCurvesMap(xVals, 0d); // linear space
		
		// prepare inputs
		ScalarIMR gmpe = null;
		Map<Integer, DiscretizedFunc> faultNonExceeds = null;
		if (calcFaults) {
			if (precomputedFaultVals == null) {
				// calculate them now
				gmpe = checkOutGMPE();
				faultNonExceeds = calcFaultIMs(site, gmpe);
			} else {
				// use precomputed
				faultNonExceeds = calcFaultExceeds(precomputedFaultVals);
			}
			complimentCurve(faultNonExceeds); // they were actually exceeds
		}
		
		Table<Integer, Integer, DiscretizedFunc> griddedNonExceeds = null;
		if (calcGridded) {
			// will calculate on the fly as needed
			griddedNonExceeds = HashBasedTable.create();
			Preconditions.checkState(gridSources.isConditional());
			if (gmpe == null)
				gmpe = checkOutGMPE();
			gmpe.setSite(site);
		}
		
		Preconditions.checkState(calcFaults || calcGridded);
		
		// now actually calculate
		double rateEach = 1d/catalogs.size();
		
		HashSet<Integer> ignoreGriddedNodes = new HashSet<Integer>();
		
		for (List<ETAS_EqkRupture> catalog : catalogs) {
			Table<Duration, MapType, DiscretizedFunc> catCurves = getInitializedCurvesMap(calcXVals, 1d); // log space if applicable
			
			for (ETAS_EqkRupture rup : catalog) {
				DiscretizedFunc condNonExceed; // conditional non-exceedance probabilities
				MapType targetType; // hazard curve to apply this to
				if (rup.getFSSIndex() >= 0) {
					// fault based
					if (!calcFaults)
						continue;
					condNonExceed = faultNonExceeds.get(rup.getFSSIndex());
					if (condNonExceed == null)
						// not within cutoff dist
						continue;
					targetType = MapType.FAULT_ONLY;
				} else {
					// gridded
					if (!calcGridded)
						continue;
					int nodeIndex = gridSources.getNodeIndex(rup);
					int mfdIndex = gridSources.getMagIndex(rup);
					if (nodeIndex < 0 || mfdIndex < 0 || ignoreGriddedNodes.contains(nodeIndex))
						continue;
					double dist = LocationUtils.horzDistanceFast(site.getLocation(), rup.getHypocenterLocation());
					if (dist > distCutoff) {
						ignoreGriddedNodes.add(nodeIndex);
						continue;
					}
					condNonExceed = griddedNonExceeds.get(nodeIndex, mfdIndex);
					targetType = MapType.GRIDDED_ONLY;
					if (condNonExceed == null) {
						// calculate it
						// multiple ruptures with different focal mechanisms
						Iterable<ProbEqkRupture> rups = gridSources.getConditionalRuptures(rup);
						if (rups == null)
							continue;
						
						condNonExceed = calcXVals.deepClone();
						initializeCurve(condNonExceed, 1d);
						double sumRate = 0d;
						for (ProbEqkRupture subRup : rups) {
							double subMag = subRup.getMag();
							Preconditions.checkState(subMag >= rup.getMag()-0.06 && subMag <= rup.getMag()+0.06,
									"Unexpected mag in sub-rupture. Expected %s, got %s", rup.getMag(), subMag);
							gmpe.setEqkRupture(subRup);
							double rupProb = subRup.getProbability();
							double rupRate = -Math.log(1 - rupProb);
							sumRate += rupRate;
//							System.out.println(subRup.getAveRake()+": "+rupProb);
							
							for (int i=0; i<condNonExceed.size(); i++) {
								// TODO doing this right?
								double exceedProb = gmpe.getExceedProbability(condNonExceed.getX(i));
								// scale by the rate of this rupture
								condNonExceed.set(i, condNonExceed.getY(i)*(1-rupRate*exceedProb));
								// this way if treating it as poisson, but since it's an actual occurance, I don't
								// think that we should
//								condNonExceed.set(i, condNonExceed.getY(i)*Math.pow(1-rupProb, exceedProb));
							}
						}
//						System.out.println("SUM: "+sumProb);
//						System.exit(0);
						Preconditions.checkState((float)sumRate == 1f, "Rupture rates don't sum to 1! %s", sumRate);
						
						griddedNonExceeds.put(nodeIndex, mfdIndex, condNonExceed);
					}
				}
				
				long ot = rup.getOriginTime();
				
				// now add the rupture to the appropriate curves
				for (int i=0; i<durations.length; i++) {
					// duration of 0 means all
					if (durations[i].years > 0 && ot > maxOTs[i])
						// rup occurs after target time, skip
						continue;
					DiscretizedFunc targetCurve = catCurves.get(durations[i], targetType);
					for (int k=0; k<targetCurve.size(); k++) {
						// multiply this into the total non-exceedance probability
						// (get the product of all non-eceedance probabilities)
						targetCurve.set(k, targetCurve.getY(k) * condNonExceed.getY(k));
					}
				}
			}
			
			if (catCurves.containsColumn(MapType.COMBINED)) {
				// build combined catalog curves
				for (Duration duration : catCurves.rowKeySet()) {
					DiscretizedFunc catFaultCurve = catCurves.get(duration, MapType.FAULT_ONLY);
					DiscretizedFunc catGriddedCurve = catCurves.get(duration, MapType.GRIDDED_ONLY);
					DiscretizedFunc catCombinedCurve = catCurves.get(duration, MapType.COMBINED);
					for (int k=0; k<catCombinedCurve.size(); k++)
						catCombinedCurve.set(k, catFaultCurve.getY(k) * catGriddedCurve.getY(k));
				}
			}
			
			for (Cell<Duration, MapType, DiscretizedFunc> cell : catCurves.cellSet()) {
				DiscretizedFunc catCurve = cell.getValue();
				// convert from total non-exceed prob to total exceed prob
				complimentCurve(catCurve);
				
				// add into total curves
				DiscretizedFunc totalCurve = curves.get(cell.getRowKey(), cell.getColumnKey());
				for (int k=0; k<xVals.size(); k++)
					totalCurve.set(k, totalCurve.getY(k) + rateEach*catCurve.getY(k));
			}
		}
		
		if (gmpe != null)
			checkInGMPE(gmpe);
		
		return curves;
	}
	
	private Map<Integer, DiscretizedFunc> calcFaultIMs(Site site, ScalarIMR gmpe) {
		// used if no precomputed data file
		Map<Integer, DiscretizedFunc> rupVals = Maps.newHashMap();
		for (Integer fssIndex : faultIndexesTriggered) {
			ProbEqkSource source = sourcesForFSSRuptures[fssIndex];
			if (source == null)
				continue;
			Preconditions.checkState(source.getNumRuptures() == 1, "Must be a single rupture source");
			ProbEqkRupture rup = source.getRupture(0);
			
			double minDist = source.getMinDistance(site);
			if (minDist > distCutoff)
				continue;
			
			gmpe.setSite(site);
			gmpe.setEqkRupture(rup);
			
			rupVals.put(fssIndex, gmpe.getExceedProbabilities(calcXVals.deepClone()));
		}
		return rupVals;
	}
	
	private Map<Integer, DiscretizedFunc> calcFaultExceeds(Map<Integer, double[]> precomputedFaultVals) {
		Map<Integer, DiscretizedFunc> rupCondExceeds = Maps.newHashMap();
		for (int rupIndex : precomputedFaultVals.keySet()) {
			DiscretizedFunc condExceed = calcXVals.deepClone(); // log space if applicable
			double[] vals = precomputedFaultVals.get(rupIndex);
			double mean = vals[0];
			double stdDev = vals[1];
			
			for (int i=0; i<condExceed.size(); i++) {
				double exceedProb = AttenuationRelationship.getExceedProbability(
						mean, stdDev, condExceed.getX(i), null, null);
				condExceed.set(i, exceedProb);
			}
			rupCondExceeds.put(rupIndex, condExceed);
		}
		return rupCondExceeds;
	}
	
	private void complimentCurve(Map<Integer, DiscretizedFunc> curves) {
		for (DiscretizedFunc curve : curves.values())
			complimentCurve(curve);
	}
	
	private void complimentCurve(DiscretizedFunc curve) {
		for (int i=0; i<curve.size(); i++)
			curve.set(i, 1d-curve.getY(i));
	}
	
	private Map<Integer, double[]> loadNextSite() throws IOException {
		Map<Integer, double[]> rupVals = loadSiteFromInputStream(in, faultSiteIndex);
		
		faultSiteIndex++;
		return rupVals;
	}
	
	Map<Integer, double[]> loadSiteFromInputStream(DataInputStream in, int expectedIndex) throws IOException {
		int index = in.readInt();
		Preconditions.checkState(index == expectedIndex, "Bad site index. Expected %s, encountered %s", index, faultSiteIndex);
		double lat = in.readDouble();
		double lon = in.readDouble();
		Location myLoc = new Location(lat, lon);
		Location gridLoc = region.getLocation(index);
		Preconditions.checkState(gridLoc.equals(myLoc),
				"Grid locations don't match.\n\tFrom region: %s\n\tFrom file: %s", gridLoc, myLoc);
		int numRups = in.readInt();
		
		Map<Integer, double[]> rupVals = Maps.newHashMap();
		
		int fssIndex;
		double mean, stdDev;
		for (int i=0; i<numRups; i++) {
			fssIndex = in.readInt();
			mean = in.readDouble();
			stdDev = in.readDouble();
			if (!faultIndexesTriggered.contains(fssIndex))
				continue;
			
			Preconditions.checkState(!rupVals.containsKey(fssIndex));
			rupVals.put(fssIndex, new double[] { mean, stdDev });
		}
		
		return rupVals;
	}
	
	private void plotCurve(final Future<Integer> future) {
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				try {
					int index = future.get();
					plotCurve(index);
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (ExecutionException e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	private void plotCurve(int index) {
		List<DiscretizedFunc> funcs = Lists.newArrayList();
		List<PlotCurveCharacterstics> chars = Lists.newArrayList();
		
		Map<MapType, DiscretizedFunc[]> fullDurationCurves = curves.row(Duration.FULL);
		
		if (fullDurationCurves.containsKey(MapType.FAULT_ONLY)) {
			DiscretizedFunc curve = fullDurationCurves.get(MapType.FAULT_ONLY)[index];
			curve.setName("Fault Based");
			funcs.add(curve);
			chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, Color.RED));
		}
		if (fullDurationCurves.containsKey(MapType.GRIDDED_ONLY)) {
			DiscretizedFunc curve = fullDurationCurves.get(MapType.GRIDDED_ONLY)[index];
			curve.setName("Gridded");
			funcs.add(curve);
			chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, Color.BLUE));
		}
		if (fullDurationCurves.containsKey(MapType.COMBINED)) {
			DiscretizedFunc curve = fullDurationCurves.get(MapType.COMBINED)[index];
			curve.setName("Combined");
			funcs.add(curve);
			chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 4f, Color.BLACK));
		}
		
		PlotSpec spec = new PlotSpec(funcs, chars, "Hazard curves for site "+index, imtName, "Probability of Exceedance");
		spec.setLegendVisible(true);
		GraphWindow gw = new GraphWindow(spec);
		gw.setXLog(true);
		gw.setYLog(true);
	}
	
	public void plotComparisons(MapType type, Duration duration, Location loc, FaultSystemSolutionERF erf,
			int startYear, double fullSimDuration, String title, File outputDir, String prefix) throws IOException {
		int index = region.indexForLocation(loc);
		Preconditions.checkState(index >= 0, "Couldn't detect index for location: "+loc);
		double dist = LocationUtils.horzDistanceFast(loc, region.getLocation(index));
		System.out.println("Mapped curve location error: "+(float)dist+" km");
		
		plotComparisons(type, duration, index, erf, startYear, fullSimDuration, title, outputDir, prefix);
	}
	
	public void plotComparisons(MapType type, Duration duration, int index, FaultSystemSolutionERF erf,
			int startYear, double fullSimDuration, String title, File outputDir, String prefix) throws IOException {
		DiscretizedFunc etasCurve = curves.get(duration, type)[index];
		Preconditions.checkNotNull(etasCurve);
		etasCurve.setName("UCERF3-ETAS");
		
		if (outputDir.getAbsolutePath().contains("-gridded-only"))
			type = MapType.COMBINED; // actually combined for comparison purposes
		
		erf.setParameter(ApplyGardnerKnopoffAftershockFilterParam.NAME, false);
		switch (type) {
		case COMBINED:
			erf.setParameter(IncludeBackgroundParam.NAME, IncludeBackgroundOption.INCLUDE);
			erf.setParameter(BackgroundRupParam.NAME, BackgroundRupType.POINT);
			break;
		case FAULT_ONLY:
			erf.setParameter(IncludeBackgroundParam.NAME, IncludeBackgroundOption.EXCLUDE);
			break;
		case GRIDDED_ONLY:
			erf.setParameter(IncludeBackgroundParam.NAME, IncludeBackgroundOption.ONLY);
			erf.setParameter(BackgroundRupParam.NAME, BackgroundRupType.POINT);
			break;

		default:
			break;
		}
		
		if (prefix == null || prefix.isEmpty())
			prefix = "";
		else
			prefix += "_";
		prefix += type.fileName+"_"+duration.fileName;
		
		double durationYears;
		String yAxisLabel;
		if (duration == Duration.FULL) {
			durationYears = fullSimDuration;
			yAxisLabel = (int)durationYears+" year";
		} else {
			durationYears = duration.years;
			yAxisLabel = duration.plotName;
		}
		
		ScalarIMR gmpe = checkOutGMPE();
		Site site = sites.get(index);
		HazardCurveCalculator calc = new HazardCurveCalculator();
		
		// calculate UCERF3-TI
		erf.setParameter(ProbabilityModelParam.NAME, ProbabilityModelOptions.POISSON);
		erf.getTimeSpan().setDuration(durationYears);
		erf.updateForecast();
		
		DiscretizedFunc tiCurve = calc.getHazardCurve(calcXVals.deepClone(), site, gmpe, erf);
		tiCurve = getReplaceXVals(tiCurve, xVals);
		tiCurve.setName("UCERF3-TI");
		
		// calculate UCERF3-TD
		erf.setParameter(ProbabilityModelParam.NAME, ProbabilityModelOptions.U3_BPT);
		erf.setParameter(MagDependentAperiodicityParam.NAME, MagDependentAperiodicityOptions.MID_VALUES);
		BPTAveragingTypeOptions aveType = BPTAveragingTypeOptions.AVE_RI_AVE_NORM_TIME_SINCE;
		erf.setParameter(BPTAveragingTypeParam.NAME, aveType);
		erf.setParameter(AleatoryMagAreaStdDevParam.NAME, 0.0);
		erf.getTimeSpan().setDuration(durationYears);
		erf.getTimeSpan().setStartTime(startYear);
		erf.setParameter(HistoricOpenIntervalParam.NAME, (double)(startYear-1875));
		erf.updateForecast();

		DiscretizedFunc tdCurve = calc.getHazardCurve(calcXVals.deepClone(), site, gmpe, erf);
		tdCurve = getReplaceXVals(tdCurve, xVals);
		tdCurve.setName("UCERF3-TD");
		
		checkInGMPE(gmpe);
		
		List<DiscretizedFunc> funcs = Lists.newArrayList();
		List<PlotCurveCharacterstics> chars = Lists.newArrayList();
		
		funcs.add(tiCurve);
		chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, Color.BLACK));
		
		funcs.add(tdCurve);
		chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, Color.BLUE));
		
		funcs.add(etasCurve);
		chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, Color.RED));
		
		PlotSpec spec = new PlotSpec(funcs, chars, title, imtName, yAxisLabel+" Probability of Exceedance");
		spec.setLegendVisible(true);
		
		HeadlessGraphPanel gp = new HeadlessGraphPanel();
		gp.setUserBounds(1e-2, 1e1, 1e-10, 1d);
		
		gp.setBackgroundColor(Color.WHITE);
		gp.setTickLabelFontSize(22);
		gp.setAxisLabelFontSize(24);
		gp.setPlotLabelFontSize(24);
		
		gp.drawGraphPanel(spec, true, true);
//		gp.getChartPanel().setSize(1000, 800);
		gp.getChartPanel().setSize(600, 480);
		gp.saveAsPNG(new File(outputDir, prefix+".png").getAbsolutePath());
		gp.saveAsPDF(new File(outputDir, prefix+".pdf").getAbsolutePath());
		gp.saveAsTXT(new File(outputDir, prefix+".txt").getAbsolutePath());
	}
	
	private static DiscretizedFunc getReplaceXVals(DiscretizedFunc curve, DiscretizedFunc xVals) {
		Preconditions.checkState(curve.size() == xVals.size());
		
		ArbitrarilyDiscretizedFunc ret = new ArbitrarilyDiscretizedFunc();
		for (int i=0; i<curve.size(); i++)
			ret.set(xVals.getX(i), curve.getY(i));
		
		return ret;
	}
	
	public static List<Site> fetchSites(GriddedRegion region, ArrayList<SiteData<?>> provs, ScalarIMR gmpe) throws IOException {
		ArrayList<SiteDataValueList<?>> siteData = null;
		SiteTranslator siteTrans = null;
		if (provs != null) {
			System.out.print("Fetching site data...");
			siteData = new OrderedSiteDataProviderList(provs).getAllAvailableData(region.getNodeList());
			System.out.println("DONE.");
			siteTrans = new SiteTranslator();
		}
		
		List<Site> sites = Lists.newArrayList();
		for (int i=0; i<region.getNodeCount(); i++) {
			Site site = new Site(region.getLocation(i));
			for (Parameter<?> param : gmpe.getSiteParams())
				site.addParameter((Parameter<?>) param.clone());
			if (siteData != null) {
				List<SiteDataValue<?>> mySiteData = Lists.newArrayList();
				for (SiteDataValueList<?> vals : siteData)
					mySiteData.add(vals.getValue(i));
				for (Parameter<?> param : site)
					siteTrans.setParameterValue(param, mySiteData);
			}
			sites.add(site);
		}
		
		return sites;
	}
	
	private synchronized ScalarIMR checkOutGMPE() {
		if (gmpeDeque == null)
			gmpeDeque = new ArrayDeque<ScalarIMR>();
		if (gmpeDeque.isEmpty()) {
			// build a new one
			ScalarIMR gmpe = gmpeRef.instance(null);
			gmpe.setParamDefaults();
			gmpe.setIntensityMeasure(imtName);
			if (imtName.equals(SA_Param.NAME))
				SA_Param.setPeriodInSA_Param(gmpe.getIntensityMeasure(), period);
			return gmpe;
		}
		return gmpeDeque.pop();
	}
	
	private synchronized void checkInGMPE(ScalarIMR gmpe) {
		gmpeDeque.push(gmpe);
	}
	
	public GriddedGeoDataSet calcMap(MapType type, Duration duration, boolean isProbAt_IML, double level) {
		GriddedGeoDataSet map = new GriddedGeoDataSet(region, false);
		
		Preconditions.checkState(curves.contains(duration, type), "No curves for %s, %s", duration, type);
		
		for (int i=0; i<map.size(); i++) {
			DiscretizedFunc curve = curves.get(duration, type)[i];
			
			double val = HazardDataSetLoader.getCurveVal(curve, isProbAt_IML, level);
			if (Double.isInfinite(val))
				val = Double.NaN;
			
			map.set(i, val);
		}
		
		return map;
	}
	
	public void plotMap(MapType type, Duration duration, boolean isProbAt_IML, double level, String label,
			File outputDir, String prefix) throws IOException, GMT_MapException {
		GriddedGeoDataSet data = calcMap(type, duration, isProbAt_IML, level);
		System.out.println("Generating map for p="+level+", "+type.name()+", "+duration.name());
		System.out.println("Map range: "+data.getMinZ()+" "+data.getMaxZ());
		
		CPT cpt;
		if (prefix.toLowerCase().contains("_mmi")) {
			cpt = GMT_CPT_Files.SHAKEMAP.instance();
		} else {
			cpt = GMT_CPT_Files.MAX_SPECTRUM.instance();
			if (Double.isInfinite(data.getMaxZ()))
				cpt = cpt.rescale(0d, 1d); // no data
			else
				cpt = cpt.rescale(0d, data.getMaxZ());
		}
		cpt.setNanColor(Color.WHITE);
		
		GMT_Map map = new GMT_Map(region, data, region.getSpacing(), cpt);
		
		map.setLogPlot(false);
//		map.setTopoResolution(TopographicSlopeFile.CA_THREE);
		map.setTopoResolution(null);
		map.setUseGMTSmoothing(false);
		map.setBlackBackground(false);
		map.setCustomScaleMin((double)cpt.getMinValue());
		map.setCustomScaleMax((double)cpt.getMaxValue());
		map.setCustomLabel(duration.plotName+", "+label);
		map.setRescaleCPT(false);
		map.setJPGFileName(null);
		
		FaultBasedMapGen.plotMap(outputDir, prefix+"_"+type.fileName+"_"+duration.fileName, false, map);
	}
	
	private Table<Duration, MapType, DiscretizedFunc> getInitializedCurvesMap(DiscretizedFunc xVals, double initialVal) {
		Table<Duration, MapType, DiscretizedFunc> curves = HashBasedTable.create();
		
		if (calcFaults)
			for (Duration duration : durations)
				curves.put(duration, MapType.FAULT_ONLY, getInitializeClone(xVals, initialVal));
		
		if (calcGridded)
			for (Duration duration : durations)
				curves.put(duration, MapType.GRIDDED_ONLY, getInitializeClone(xVals, initialVal));
		
		if (calcFaults && calcGridded)
			for (Duration duration : durations)
				curves.put(duration, MapType.COMBINED, getInitializeClone(xVals, initialVal));
		
		return curves;
	}
	
	private static DiscretizedFunc getInitializeClone(DiscretizedFunc curve, double val) {
		curve = curve.deepClone();
		initializeCurve(curve, val);
		return curve;
	}
	
	private static void initializeCurve(DiscretizedFunc curve, double val) {
		for (int i=0; i<curve.size(); i++)
			curve.set(i, val);
	}
	
	private static Table<Duration, MapType, File> detectCurveFiles(File dir, String prefix) {
		Table<Duration, MapType, File> curveFiles = HashBasedTable.create();
		
		Preconditions.checkState(dir.exists() && dir.isDirectory());
		
		for (File file : dir.listFiles()) {
			String name = file.getName();
			
			if (!name.startsWith(prefix) || !name.endsWith(".bin"))
				continue;
			
			name = name.substring(prefix.length());
			
			// detect duration
			Duration duration = null;
			for (Duration d : Duration.values()) {
				if (name.contains(d.fileName)) {
					duration = d;
					break;
				}
			}
			if (duration == null)
				// assume full
				duration = Duration.FULL;
			
			// detect type
			MapType type = null;
			for (MapType t : MapType.values()) {
				if (name.contains(t.fileName)) {
					type = t;
					break;
				}
			}
			Preconditions.checkNotNull(type, "Couldn't detect type from file: %s", file.getName());
			
			curveFiles.put(duration, type, file);
		}
		
		Preconditions.checkState(!curveFiles.isEmpty(), "No matching curve files with prefix: %s", prefix);
		
		return curveFiles;
	}
	
	static List<Site> loadSitesFile(AttenRelRef gmpeRef, File sitesFile) {
		Document siteDoc;
		try {
			siteDoc = XMLUtils.loadDocument(sitesFile);
		} catch (Exception e) {
			throw ExceptionUtils.asRuntimeException(e);
		}
		Element sitesRoot = siteDoc.getRootElement();
		ArrayList<Parameter<?>> paramsToAdd = new ArrayList<Parameter<?>>();
		ScalarIMR gmpe = gmpeRef.instance(null);
		gmpe.setParamDefaults();
		for (Parameter<?> param : gmpe.getSiteParams())
			paramsToAdd.add(param);
		return Site.loadSitesFromXML(sitesRoot.element(Site.XML_METADATA_LIST_NAME), paramsToAdd);
	}
	
	private static class ETAS_MMI_HazardMapCalc extends ETAS_HazardMapCalc {
		
		private ETAS_HazardMapCalc pgaCalc, pgvCalc;
		
		public ETAS_MMI_HazardMapCalc(ETAS_HazardMapCalc pgaCalc, ETAS_HazardMapCalc pgvCalc) {
			this.pgaCalc = pgaCalc;
			this.pgvCalc = pgvCalc;
			
			Preconditions.checkState(pgaCalc.region.equals(pgvCalc.region));
			this.region = pgaCalc.region;
			
			// fill in curves map with empty curve arrays, just so that we know what types/durations are available
			curves = HashBasedTable.create();
			for (Cell<Duration, MapType, DiscretizedFunc[]> cell : pgaCalc.curves.cellSet()) {
				Duration duration = cell.getRowKey();
				MapType type = cell.getColumnKey();
				if (pgvCalc.curves.contains(duration, type))
					curves.put(duration, type, new DiscretizedFunc[0]);
			}
		}

		@Override
		public GriddedGeoDataSet calcMap(MapType type, Duration duration, boolean isProbAt_IML, double level) {
			GriddedGeoDataSet pgaMap = pgaCalc.calcMap(type, duration, isProbAt_IML, level);
			GriddedGeoDataSet pgvMap = pgvCalc.calcMap(type, duration, isProbAt_IML, level);
			
			GriddedGeoDataSet mmiMap = new GriddedGeoDataSet(pgaMap.getRegion(), pgaMap.isLatitudeX());
			
			for (int index=0; index<mmiMap.size(); index++) {
				double pga = pgaMap.get(index);
				if (!Doubles.isFinite(pga))
					pga = 0;
				double pgv = pgvMap.get(index);
				if (!Doubles.isFinite(pgv))
					pgv = 0;
				double mmi = Wald_MMI_Calc.getMMI(pga, pgv);
				if (mmi == 1d)
					// will speed things up
					mmi = Double.NaN;
				mmiMap.set(index, mmi);
			}
			
			return mmiMap;
		}
		
	}
	
	private static class FakeGriddedRegion extends GriddedRegion {
		
		private LocationList locs;
		
		public FakeGriddedRegion(Region region, List<Location> locs) {
			super(region, 1d, null);
			this.locs = new LocationList();
			this.locs.addAll(locs);
		}

		@Override
		public int getNodeCount() {
			return locs.size();
		}

		@Override
		public LocationList getNodeList() {
			return locs;
		}

		@Override
		public Location locationForIndex(int index) {
			return locs.get(index);
		}

		@Override
		public Location getLocation(int index) {
			return locs.get(index);
		}

		@Override
		public int indexForLocation(Location loc) {
			return locs.indexOf(loc);
		}
	}

	@SuppressWarnings("unused")
	public static void main(String[] args) throws Exception {
		force_serial = false;
		boolean calcFault = true;
		boolean calcGridded = true;
		boolean mapParallel = true;
		boolean plotCurves = false;
		boolean plotMaps = true;
//		HashSet<MapType> mapTypePlotSubset = new HashSet<MapType>(Lists.newArrayList(MapType.COMBINED));
		HashSet<MapType> mapTypePlotSubset = null;
		HashSet<Duration> durationPlotSubset = null;
//		calc.debugCurvePlotModulus = 10;
//		calc.debugStopIndex = 500;
		
//		File faultBasedPrecalc = null;
////		double spacing = 0.02d;
////		File precalcDir = new File("/home/kevin/OpenSHA/UCERF3/etas/hazard/"
////				+ "2017_02_28-mojave_m7_fulltd_descendents-NGA2-0.02-site-effects-with-basin");
//		double spacing = 0.01d;
//		File precalcDir = new File("/home/kevin/OpenSHA/UCERF3/etas/hazard/"
//				+ "2017_03_01-mojave_m7_fulltd_descendents-NGA2-0.01-site-effects-with-basin");
		
//		File faultBasedPrecalc = null;
//		double spacing = 1.0;
//		File precalcDir = new File("/tmp/etas_hazard_test");
		
		File faultBasedPrecalc = null;
		double spacing = 0.02;
		File precalcDir = new File("/home/kevin/OpenSHA/UCERF3/etas/hazard/"
//				+ "2017_03_03-haywired_m7_fulltd_descendents-NGA2-0.02-site-effects-with-basin");
				+ "2017_03_03-haywired_m7_gridded_descendents-NGA2-0.02-site-effects-with-basin");
		
//		File faultBasedPrecalc = null;
//		double spacing = 0.5;
//		File precalcDir = null; 
		
//		String etasDirName = "2016_02_19-mojave_m7-10yr-full_td-subSeisSupraNucl-gridSeisCorr-scale1.14-combined100k";
//		String etasDirName = "2016_06_15-haywired_m7-10yr-full_td-subSeisSupraNucl-gridSeisCorr-scale1.14-combined";
		String etasDirName = "2017_01_02-haywired_m7-10yr-gridded-only-200kcombined";
		String etasFileName = "results_descendents_m5_preserve.bin";
//		String etasFileName = "results_m5_preserve.bin";
		int etasStartYear = 2012;
		double etasSimDuration = 10d;
		
//		Duration[] durations = { Duration.FULL, Duration.DAY, Duration.MONTH };
		Duration[] durations = Duration.values();
		
//		String imtName = PGA_Param.NAME;
//		double period = Double.NaN;
//		String imtLabel = "PGA";
//		String imtFileLabel = "pga";
//		String imtName = PGV_Param.NAME;
//		double period = Double.NaN;
//		String imtLabel = "PGV";
//		String imtFileLabel = "pgv";
//		String imtName = SA_Param.NAME;
//		double period = 1d;
//		String imtLabel = "1s Sa";
//		String imtFileLabel = "sa_1s";
		String imtName = MMI_Param.NAME;
		double period = Double.NaN;
		String imtLabel = "MMI";
		String imtFileLabel = "mmi";
		GriddedRegion region = new CaliforniaRegions.RELM_TESTING_GRIDDED(spacing);
		DiscretizedFunc xVals = new IMT_Info().getDefaultHazardCurve(SA_Param.NAME);
		AttenRelRef gmpeRef = AttenRelRef.NGAWest_2014_AVG_NOIDRISS;
		ArrayList<SiteData<?>> provs = null;
		double griddedResolution = 0.01;
		boolean griddedConditional = true;
		
		List<Location> curveLocs = null;
		List<String> curveSiteNames = null;
		if (imtName.equals(MMI_Param.NAME)) {
			plotCurves = false;
			Preconditions.checkState(precalcDir != null);
		}
		
		if (plotCurves) {
			curveLocs = Lists.newArrayList();
			curveSiteNames = Lists.newArrayList();
			
			curveLocs.add(NEHRP_TestCity.OAKLAND.location());
			curveSiteNames.add("Oakland");
		}
		
		File etasCatalogs = new File(new File("/home/kevin/OpenSHA/UCERF3/etas/simulations/", etasDirName), etasFileName);
		
		if (plotCurves && !plotMaps && !curveLocs.isEmpty() && precalcDir == null) {
			// only calculate for sites of interest
			System.out.println("Creating fake gridded region for "+curveLocs.size()+" locations");
			region = new FakeGriddedRegion(region, curveLocs);
		}
		
		File outputDir = new File(etasCatalogs.getParentFile(), "hazard_maps");
		Preconditions.checkState(outputDir.exists() || outputDir.mkdir());
		if (faultBasedPrecalc == null && precalcDir == null) {
			if (region instanceof FakeGriddedRegion) {
				if (etasFileName.contains("descend"))
					outputDir = new File(outputDir, "site_specific_descendants");
				else
					outputDir = new File(outputDir, "site_specific_all_rups");
			} else {
				outputDir = new File(outputDir, "testing");
			}
		} else if (faultBasedPrecalc == null) {
			outputDir = new File(outputDir, precalcDir.getName());
		} else {
			outputDir = new File(outputDir, faultBasedPrecalc.getParentFile().getName());
		}
		Preconditions.checkState(outputDir.exists() || outputDir.mkdir());
		
		File solFile = new File("/home/kevin/workspace/OpenSHA/dev/scratch/UCERF3/data/scratch/InversionSolutions/"
				+ "2013_05_10-ucerf3p3-production-10runs_COMPOUND_SOL_FM3_1_SpatSeisU3_MEAN_BRANCH_AVG_SOL.zip");
		
		if (!calcFault)
			faultBasedPrecalc = null;
		
		FaultSystemSolution sol = null;
		ETAS_HazardMapCalc calc;
		if (precalcDir != null) {
			if (imtName.equals(MMI_Param.NAME)) {
				ETAS_HazardMapCalc pgaCalc = new ETAS_HazardMapCalc(region, detectCurveFiles(precalcDir, "results_pga"));
				ETAS_HazardMapCalc pgvCalc = new ETAS_HazardMapCalc(region, detectCurveFiles(precalcDir, "results_pgv"));
				calc = new ETAS_MMI_HazardMapCalc(pgaCalc, pgvCalc);
			} else {
				Table<Duration, MapType, File> curveFiles = detectCurveFiles(precalcDir, "results_"+imtFileLabel);
				List<Site> sites = null;
				if (plotCurves) {
					// load in the sites, we'll need them
					File sitesFile = new File(precalcDir, "sites.xml");
					Preconditions.checkState(sitesFile.exists(), "Need sites, but no sites.xml in %s", precalcDir.getAbsolutePath());
					sites = loadSitesFile(gmpeRef, sitesFile);
				}
				calc = new ETAS_HazardMapCalc(region, curveFiles, xVals, gmpeRef, imtName, period, sites);
			}
			
			durations = calc.curves.rowKeySet().toArray(new Duration[0]);
		} else {
			calcGridded = calcGridded && gmpeRef != null;
			List<List<ETAS_EqkRupture>> catalogs = ETAS_CatalogIO.loadCatalogsBinary(etasCatalogs, 5d);
			
			if (calcFault && faultBasedPrecalc == null)
				sol = FaultSystemIO.loadSol(solFile);
			ETAS_CatalogGridSourceProvider gridSources = null;
			if (calcGridded)
				gridSources = new ETAS_CatalogGridSourceProvider(catalogs, griddedResolution, griddedConditional);
			List<Site> sites = null;
			if (calcGridded || sol != null) {
				// need sites
				AttenuationRelationship gmpe = gmpeRef.instance(null);
				gmpe.setParamDefaults();
				sites = fetchSites(region, provs, gmpe);
			}
			calc = new ETAS_HazardMapCalc(catalogs, region, xVals, faultBasedPrecalc, sol, gridSources,
					gmpeRef, imtName, period, sites, durations);
			if (!calcFault)
				calc.setCalcFaults(false);
			if (!calcGridded)
				calc.setCalcGridded(false);
			
			Preconditions.checkState(calcFault || calcGridded);
			calc.calculate();
		}
		
//		for (int i=0; i<region.getNodeCount(); i+= 500)
//			calc.plotCurve(i);
		
		Set<MapType> types = calc.curves.columnKeySet();
		
		if (plotCurves) {
			File curveDir = new File(outputDir, "curves");
			Preconditions.checkState(curveDir.exists() || curveDir.mkdir());
			if (sol == null)
				sol = FaultSystemIO.loadSol(solFile);
			FaultSystemSolutionERF erf = new FaultSystemSolutionERF(sol);
			
			MapType type;
			if (types.contains(MapType.COMBINED))
				type = MapType.COMBINED;
			else
				type = types.iterator().next();
			for (int i=0; i<curveLocs.size(); i++) {
				Location loc = curveLocs.get(i);
				String name = curveSiteNames.get(i);
				System.out.println("Plotting curves for "+name);
				String prefix = name.toLowerCase().replaceAll(" ", "_");
				
				for (Duration duration : durations)
					calc.plotComparisons(type, duration, loc, erf, etasStartYear, etasSimDuration, name, curveDir, prefix);
			}
		}
		
		if (plotMaps) {
			Preconditions.checkState(!types.isEmpty());
			
			ExecutorService exec = null;
			List<Future<?>> futures = null;
			if (mapParallel) {
				exec = Executors.newFixedThreadPool(10);
				futures = Lists.newArrayList();
			}
			
			System.out.println("Plotting maps for "+Joiner.on(",").join(types)+", "+Joiner.on(",").join(durations));
			
			double[] probVals = { 0.5, 0.25, 0.1d, 0.01d, 0.001 };
			for (double p : probVals) {
				String probString;
				double p100 = p*100;
				if (p100 == Math.floor(p100))
					probString = (int)p100+"";
				else
					probString = (float)p100+"";
				String label = imtLabel+" @ "+probString+"% POE";
				String prefix = "map_"+imtFileLabel+"_p"+(float)p;
				
				for (MapType type : types) {
					if (mapTypePlotSubset != null && !mapTypePlotSubset.contains(type))
						continue;
					File typeDir = new File(outputDir, "maps_"+type.fileName);
					Preconditions.checkState(typeDir.exists() || typeDir.mkdir());
					for (Duration duration : durations) {
						if (durationPlotSubset != null && !durationPlotSubset.contains(duration))
							continue;
						File durationDir = new File(typeDir, duration.fileName);
						Preconditions.checkState(durationDir.exists() || durationDir.mkdir());
//						System.out.println("label: "+label+", "+type+", "+duration);
						MapPlotRunnable runnable = new MapPlotRunnable(p, type, duration, label, durationDir, prefix, calc);
						if (exec == null) {
							runnable.run();
						} else {
							futures.add(exec.submit(runnable));
							// sleep for just a bit to avoid dir name collisions
							Thread.sleep(1000);
						}
					}
				}
			}
			
			if (exec != null) {
				try {
					for (Future<?> future : futures)
						future.get();
				} catch (Exception e) {
					e.printStackTrace();
				}
				exec.shutdown();
			}
		}
	}
	
	private static class MapPlotRunnable implements Runnable {
		
		private double p;
		private MapType type;
		private Duration duration;
		private String label;
		private File outputDir;
		private String prefix;
		
		private ETAS_HazardMapCalc calc;
		
		public MapPlotRunnable(double p, MapType type, Duration duration, String label, File outputDir, String prefix,
				ETAS_HazardMapCalc calc) {
			super();
			this.p = p;
			this.type = type;
			this.duration = duration;
			this.label = label;
			this.outputDir = outputDir;
			this.prefix = prefix;
			this.calc = calc;
		}
		@Override
		public void run() {
			try {
				calc.plotMap(type, duration, false, p, label, outputDir, prefix);
				System.out.println("Done with "+prefix);
			} catch (Exception e) {
				ExceptionUtils.throwAsRuntimeException(e);
			}
		}
	}

}
