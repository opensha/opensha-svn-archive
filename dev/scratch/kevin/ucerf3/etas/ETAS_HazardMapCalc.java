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
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.swing.SwingUtilities;

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
import org.opensha.commons.geo.LocationUtils;
import org.opensha.commons.gui.plot.GraphWindow;
import org.opensha.commons.gui.plot.PlotCurveCharacterstics;
import org.opensha.commons.gui.plot.PlotLineType;
import org.opensha.commons.gui.plot.PlotSpec;
import org.opensha.commons.mapping.gmt.GMT_Map;
import org.opensha.commons.mapping.gmt.elements.GMT_CPT_Files;
import org.opensha.commons.param.Parameter;
import org.opensha.commons.util.ExceptionUtils;
import org.opensha.commons.util.cpt.CPT;
import org.opensha.sha.calc.HazardCurveCalculator;
import org.opensha.sha.calc.hazardMap.BinaryHazardCurveReader;
import org.opensha.sha.calc.hazardMap.HazardDataSetLoader;
import org.opensha.sha.earthquake.ProbEqkRupture;
import org.opensha.sha.earthquake.ProbEqkSource;
import org.opensha.sha.earthquake.param.IncludeBackgroundOption;
import org.opensha.sha.earthquake.param.IncludeBackgroundParam;
import org.opensha.sha.gui.infoTools.IMT_Info;
import org.opensha.sha.imr.AttenRelRef;
import org.opensha.sha.imr.AttenuationRelationship;
import org.opensha.sha.imr.ScalarIMR;
import org.opensha.sha.imr.param.IntensityMeasureParams.PGA_Param;
import org.opensha.sha.imr.param.IntensityMeasureParams.PGV_Param;
import org.opensha.sha.imr.param.IntensityMeasureParams.SA_Param;
import org.opensha.sha.util.SiteTranslator;

import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;

import scratch.UCERF3.FaultSystemSolution;
import scratch.UCERF3.analysis.FaultBasedMapGen;
import scratch.UCERF3.erf.FaultSystemSolutionERF;
import scratch.UCERF3.erf.ETAS.ETAS_CatalogIO;
import scratch.UCERF3.erf.ETAS.ETAS_EqkRupture;
import scratch.UCERF3.utils.FaultSystemIO;

public class ETAS_HazardMapCalc {
	
	private static boolean force_serial = false;
	private static int debugCurvePlotModulus = 0;
	private static int debugStopIndex = 0;
	
	private boolean calcInLogSpace = true;
	private double distCutoff = 200;
	
	private List<List<ETAS_EqkRupture>> catalogs;
	private HashSet<Integer> faultIndexesTriggered;
	private GriddedRegion region;
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
	
	private DiscretizedFunc[] faultHazardCurves;
	private DiscretizedFunc[] griddedHazardCurves;
	private DiscretizedFunc[] combinedHazardCurves;
	private int curvesCalculated;
	
	private boolean calcFaults;
	private boolean calcGridded;
	
	public ETAS_HazardMapCalc(List<List<ETAS_EqkRupture>> catalogs, GriddedRegion region, DiscretizedFunc xVals,
			File precalcFile, FaultSystemSolution sol, ETAS_CatalogGridSourceProvider gridSources,
			AttenRelRef gmpeRef, String imtName, double period, List<Site> sites) throws IOException {
		this.catalogs = catalogs;
		this.region = region;
		this.xVals = xVals;
		this.gridSources = gridSources;
		this.gmpeRef = gmpeRef;
		this.imtName = imtName;
		this.period = period;
		this.sites = sites;
		
		calcGridded = (gridSources != null && gmpeRef != null && imtName != null && sites != null);
		Preconditions.checkState(sites == null || sites.size() == region.getNodeCount());
		
		// this is used to conserve memory and only load results for ruptures actually used
		faultIndexesTriggered = new HashSet<Integer>();
		for (List<ETAS_EqkRupture> catalog : catalogs)
			for (ETAS_EqkRupture rup : catalog)
				if (rup.getFSSIndex() > 0)
					faultIndexesTriggered.add(rup.getFSSIndex());
		
		if (calcInLogSpace) {
			calcXVals = new ArbitrarilyDiscretizedFunc();
			for (int i=0; i<xVals.size(); i++)
				calcXVals.set(Math.log(xVals.getX(i)), 1d);
		} else {
			calcXVals = xVals;
		}
		
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
	
	public ETAS_HazardMapCalc(GriddedRegion region, File faultCurvesFile, File griddedCurvesFile, File combinedCurvesFile)
			throws Exception {
		this.region = region;
		if (faultCurvesFile != null)
			faultHazardCurves = loadCurves(faultCurvesFile);
		if (griddedCurvesFile != null)
			griddedHazardCurves = loadCurves(griddedCurvesFile);
		if (combinedCurvesFile != null)
			combinedHazardCurves = loadCurves(combinedCurvesFile);
		
		if (faultHazardCurves != null)
			xVals = faultHazardCurves[0];
		else if (griddedHazardCurves != null)
			xVals = griddedHazardCurves[0];
		else if (combinedHazardCurves != null)
			xVals = combinedHazardCurves[0];
		else
			throw new IllegalArgumentException("Must supply at least one curve file");
	}
	
	private DiscretizedFunc[] loadCurves(File curvesFile) throws Exception {
		BinaryHazardCurveReader reader = new BinaryHazardCurveReader(curvesFile.getAbsolutePath());
		
		DiscretizedFunc[] curves = new DiscretizedFunc[region.getNodeCount()];
		
		for (int i=0; i<curves.length; i++) {
			curves[i] = reader.nextCurve();
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
		
		if (calcFaults)
			faultHazardCurves = new DiscretizedFunc[region.getNodeCount()];
		if (calcGridded)
			griddedHazardCurves = new DiscretizedFunc[region.getNodeCount()];
		if (calcFaults && calcGridded)
			combinedHazardCurves = new DiscretizedFunc[region.getNodeCount()];
		
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
			DiscretizedFunc[] curves = calculateCurves(sites.get(index), precomputedFaultVals);
			if (curves[0] != null)
				faultHazardCurves[index] = curves[0];
			if (curves[1] != null)
				griddedHazardCurves[index] = curves[1];
			if (curves[2] != null)
				combinedHazardCurves[index] = curves[2];
			
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
	 * @return array of [faultCurve, griddedCurve, combinedCurve]
	 */
	DiscretizedFunc[] calculateCurves(Site site, Map<Integer, double[]> precomputedFaultVals) {
		// prepare inputs
		ScalarIMR gmpe = null;
		Map<Integer, DiscretizedFunc> faultNonExceeds = null;
		DiscretizedFunc faultCurve = null;
		if (calcFaults) {
			faultCurve = xVals.deepClone(); // linear space
			initializeCurve(faultCurve, 0d);
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
		DiscretizedFunc griddedCurve = null;
		if (calcGridded) {
			griddedCurve = xVals.deepClone(); // linear space
			initializeCurve(griddedCurve, 0d);
			// will calculate on the fly as needed
			griddedNonExceeds = HashBasedTable.create();
			Preconditions.checkState(gridSources.isConditional());
			if (gmpe == null)
				gmpe = checkOutGMPE();
		}
		
		DiscretizedFunc combinedCurve = null;
		if (calcFaults && calcGridded) {
			combinedCurve = xVals.deepClone(); // linear space
			initializeCurve(combinedCurve, 0d);
		}
		
		Preconditions.checkState(calcFaults || calcGridded);
		
		// now actually calculate
		double rateEach = 1d/catalogs.size();
		
		HashSet<Integer> ignoreGriddedNodes = new HashSet<Integer>();
		
		for (List<ETAS_EqkRupture> catalog : catalogs) {
			DiscretizedFunc catFaultCurve = null, catGriddedCurve = null;
			
			 // log space if applicable
			if (faultCurve != null) {
				catFaultCurve = calcXVals.deepClone();
				initializeCurve(catFaultCurve, 1d);
			}
			if (griddedCurve != null) {
				catGriddedCurve = calcXVals.deepClone();
				initializeCurve(catGriddedCurve, 1d);
			}
			
			for (ETAS_EqkRupture rup : catalog) {
				DiscretizedFunc condNonExceed; // conditional non-exceedance probabilities
				DiscretizedFunc targetCurve; // hazard curve to apply this to
				if (rup.getFSSIndex() >= 0) {
					// fault based
					if (catFaultCurve == null)
						continue;
					condNonExceed = faultNonExceeds.get(rup.getFSSIndex());
					if (condNonExceed == null)
						// not within cutoff dist
						continue;
					targetCurve = catFaultCurve;
				} else {
					// gridded
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
					targetCurve = catGriddedCurve;
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
				
				// now add the rupture to the appropriate curve
				for (int k=0; k<targetCurve.size(); k++) {
					// multiply this into the total non-exceedance probability
					// (get the product of all non-eceedance probabilities)
					targetCurve.set(k, targetCurve.getY(k) * condNonExceed.getY(k));
				}
			}
			
			DiscretizedFunc catCombinedCurve = null;
			if (combinedCurve != null) {
				// build combined catalog curved
				catCombinedCurve = calcXVals.deepClone();
				for (int k=0; k<catCombinedCurve.size(); k++)
					catCombinedCurve.set(k, catFaultCurve.getY(k) * catGriddedCurve.getY(k));
			}
			
			// now convert from total non-exceed prob to total exceed prob
			if (catFaultCurve != null)
				complimentCurve(catFaultCurve);
			if (catGriddedCurve != null)
				complimentCurve(catGriddedCurve);
			if (catCombinedCurve != null)
				complimentCurve(catCombinedCurve);
			
			// now add into total curves
			for (int k=0; k<xVals.size(); k++) {
				if (catFaultCurve != null)
					faultCurve.set(k, faultCurve.getY(k) + rateEach*catFaultCurve.getY(k));
				if (catGriddedCurve != null)
					griddedCurve.set(k, griddedCurve.getY(k) + rateEach*catGriddedCurve.getY(k));
				if (catCombinedCurve != null)
					combinedCurve.set(k, combinedCurve.getY(k) + rateEach*catCombinedCurve.getY(k));
			}
		}
		
		if (gmpe != null)
			checkInGMPE(gmpe);
		
		return new DiscretizedFunc[] { faultCurve, griddedCurve, combinedCurve };
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
		List<DiscretizedFunc> curves = Lists.newArrayList();
		List<PlotCurveCharacterstics> chars = Lists.newArrayList();
		
		if (faultHazardCurves != null) {
			faultHazardCurves[index].setName("Fault Based");
			curves.add(faultHazardCurves[index]);
			chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, Color.RED));
		}
		if (griddedHazardCurves != null) {
			griddedHazardCurves[index].setName("Gridded");
			curves.add(griddedHazardCurves[index]);
			chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, Color.BLUE));
		}
		if (combinedHazardCurves != null) {
			combinedHazardCurves[index].setName("Combined");
			curves.add(combinedHazardCurves[index]);
			chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 4f, Color.BLACK));
		}
		
		PlotSpec spec = new PlotSpec(curves, chars, "Hazard curves for site "+index, imtName, "Probability of Exceedance");
		spec.setLegendVisible(true);
		GraphWindow gw = new GraphWindow(spec);
		gw.setXLog(true);
		gw.setYLog(true);
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
	
	private enum MapType {
		FAULT_ONLY("faults"),
		GRIDDED_ONLY("gridded"),
		COMBINED("combined");
		
		private String fileName;
		private MapType(String fileName) {
			this.fileName = fileName;
		}
	}
	
	public GriddedGeoDataSet calcMap(MapType type, boolean isProbAt_IML, double level) {
		GriddedGeoDataSet map = new GriddedGeoDataSet(region, false);
		
		for (int i=0; i<map.size(); i++) {
			DiscretizedFunc curve;
			switch (type) {
			case FAULT_ONLY:
				curve = faultHazardCurves[i];
				break;
			case GRIDDED_ONLY:
				curve = griddedHazardCurves[i];
				break;
			case COMBINED:
				curve = combinedHazardCurves[i];
				break;

			default:
				throw new IllegalStateException("Unknown map type: "+type);
			}
			
			double val = HazardDataSetLoader.getCurveVal(curve, isProbAt_IML, level);
			if (Double.isInfinite(val))
				val = Double.NaN;
			
			map.set(i, val);
		}
		
		return map;
	}
	
	public void plotMap(MapType type, boolean isProbAt_IML, double level, String label, File outputDir, String prefix)
			throws IOException, GMT_MapException {
		GriddedGeoDataSet data = calcMap(type, isProbAt_IML, level);
		System.out.println("Generating map for p="+level+", "+type.name());
		System.out.println("Map range: "+data.getMinZ()+" "+data.getMaxZ());
		
		CPT cpt = GMT_CPT_Files.MAX_SPECTRUM.instance();
		if (Double.isInfinite(data.getMaxZ()))
			cpt = cpt.rescale(0d, 1d); // no data
		else
			cpt = cpt.rescale(0d, data.getMaxZ());
		cpt.setNanColor(Color.WHITE);
		
		GMT_Map map = new GMT_Map(region, data, region.getSpacing(), cpt);
		
		map.setLogPlot(false);
//		map.setTopoResolution(TopographicSlopeFile.CA_THREE);
		map.setTopoResolution(null);
		map.setUseGMTSmoothing(false);
		map.setBlackBackground(false);
		map.setCustomScaleMin((double)cpt.getMinValue());
		map.setCustomScaleMax((double)cpt.getMaxValue());
		map.setCustomLabel(label);
		map.setRescaleCPT(false);
		
		FaultBasedMapGen.plotMap(outputDir, prefix+"_"+type.fileName, false, map);
	}
	
	private static void initializeCurve(DiscretizedFunc curve, double val) {
		for (int i=0; i<curve.size(); i++)
			curve.set(i, val);
	}

	@SuppressWarnings("unused")
	public static void main(String[] args) throws Exception {
		force_serial = false;
		boolean calcFault = false;
		boolean calcGridded = true;
		boolean mapParallel = true;
//		calc.debugCurvePlotModulus = 10;
//		calc.debugStopIndex = 500;
		
//		File faultBasedPrecalc = new File("/home/kevin/OpenSHA/UCERF3/shakemap_precalc/"
//				+ "2017_02_21-NGAWest_2014_NoIdr-spacing0.1-no-site-effects/results_sa_1.0s.bin");
//		double spacing = 0.1d;
//		File precalcDir = null;
		
//		File faultBasedPrecalc = new File("/home/kevin/OpenSHA/UCERF3/shakemap_precalc/"
//				+ "2017_02_21-NGAWest_2014_NoIdr-spacing1.0-no-site-effects/results_sa_1.0s.bin");
//		double spacing = 1.0d;
//		File precalcDir = null;
		
//		File faultBasedPrecalc = new File("/home/kevin/OpenSHA/UCERF3/shakemap_precalc/"
//				+ "2017_02_23-NGAWest_2014_NoIdr-spacing0.05-site-effects-with-basin/results_pga.bin");
//		double spacing = 0.05d;
//		File precalcDir = new File("/home/kevin/OpenSHA/UCERF3/etas/hazard/"
//				+ "2017_02_24-mojave_m7_fulltd_descendents-NGA2-0.05-site-effects-with-basin");
		
//		File faultBasedPrecalc = null;
////		double spacing = 0.02d;
////		File precalcDir = new File("/home/kevin/OpenSHA/UCERF3/etas/hazard/"
////				+ "2017_02_28-mojave_m7_fulltd_descendents-NGA2-0.02-site-effects-with-basin");
//		double spacing = 0.01d;
//		File precalcDir = new File("/home/kevin/OpenSHA/UCERF3/etas/hazard/"
//				+ "2017_03_01-mojave_m7_fulltd_descendents-NGA2-0.01-site-effects-with-basin");
		
		File faultBasedPrecalc = null;
		double spacing = 0.5;
		File precalcDir = null;
		
//		String imtName = PGA_Param.NAME;
//		double period = Double.NaN;
//		String imtLabel = "PGA";
//		String imtFileLabel = "pga";
		String imtName = PGV_Param.NAME;
		double period = Double.NaN;
		String imtLabel = "PGV";
		String imtFileLabel = "pgv";
//		String imtName = SA_Param.NAME;
//		double period = 1d;
//		String imtLabel = "1s Sa";
//		String imtFileLabel = "sa_1s";
		GriddedRegion region = new CaliforniaRegions.RELM_TESTING_GRIDDED(spacing);
		DiscretizedFunc xVals = new IMT_Info().getDefaultHazardCurve(SA_Param.NAME);
		AttenRelRef gmpeRef = AttenRelRef.NGAWest_2014_AVG_NOIDRISS;
		ArrayList<SiteData<?>> provs = null;
		double griddedResolution = 0.01;
		boolean griddedConditional = true;
		
		File etasCatalogs = new File("/home/kevin/OpenSHA/UCERF3/etas/simulations/"
				+ "2016_02_19-mojave_m7-10yr-full_td-subSeisSupraNucl-gridSeisCorr-scale1.14-combined100k/"
				+ "results_descendents_m5_preserve.bin");
		File outputDir = new File(etasCatalogs.getParentFile(), "hazard_maps");
		Preconditions.checkState(outputDir.exists() || outputDir.mkdir());
		if (faultBasedPrecalc == null && precalcDir == null)
			outputDir = new File(outputDir, "testing");
		else if (faultBasedPrecalc == null)
			outputDir = new File(outputDir, precalcDir.getName());
		else
			outputDir = new File(outputDir, faultBasedPrecalc.getParentFile().getName());
		Preconditions.checkState(outputDir.exists() || outputDir.mkdir());
		
		File solFile = new File("/home/kevin/workspace/OpenSHA/dev/scratch/UCERF3/data/scratch/InversionSolutions/"
				+ "2013_05_10-ucerf3p3-production-10runs_COMPOUND_SOL_FM3_1_SpatSeisU3_MEAN_BRANCH_AVG_SOL.zip");
		
		if (!calcFault)
			faultBasedPrecalc = null;
		
		ETAS_HazardMapCalc calc;
		MapType[] types;
		if (precalcDir != null) {
			types = MapType.values();
			
			File faultCurvesFile = new File(precalcDir, "results_"+imtFileLabel+"_fault.bin");
			File griddedCurvesFile = new File(precalcDir, "results_"+imtFileLabel+"_gridded.bin");
			File combinedCurvesFile = new File(precalcDir, "results_"+imtFileLabel+"_combined.bin");
			
			calc = new ETAS_HazardMapCalc(region, faultCurvesFile, griddedCurvesFile, combinedCurvesFile);
		} else {
			calcGridded = calcGridded && gmpeRef != null;
			List<List<ETAS_EqkRupture>> catalogs = ETAS_CatalogIO.loadCatalogsBinary(etasCatalogs, 5d);
			
			FaultSystemSolution sol = null;
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
			calc = new ETAS_HazardMapCalc(catalogs, region, xVals, faultBasedPrecalc, sol, gridSources, gmpeRef, imtName, period, sites);
			if (!calcFault)
				calc.setCalcFaults(false);
			if (!calcGridded)
				calc.setCalcGridded(false);
			calc.calculate();
			
			Preconditions.checkState(calcFault || calcGridded);
			if (calcFault && calcGridded)
				types = MapType.values();
			else if (calcFault)
				types = new MapType[] {MapType.FAULT_ONLY};
			else
				types = new MapType[] {MapType.GRIDDED_ONLY};
		}
		
//		for (int i=0; i<region.getNodeCount(); i+= 500)
//			calc.plotCurve(i);
		
		ExecutorService exec = null;
		if (mapParallel)
			exec = Executors.newFixedThreadPool(6);
		
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
				if (exec == null)
					calc.plotMap(type, false, p, label, outputDir, prefix);
				else
					exec.submit(new MapPlotRunnable(p, type, label, outputDir, prefix, calc));
			}
		}
		
		if (exec != null)
			exec.shutdown();
	}
	
	private static class MapPlotRunnable implements Runnable {
		
		private double p;
		private MapType type;
		private String label;
		private File outputDir;
		private String prefix;
		
		private ETAS_HazardMapCalc calc;
		
		public MapPlotRunnable(double p, MapType type, String label, File outputDir, String prefix,
				ETAS_HazardMapCalc calc) {
			super();
			this.p = p;
			this.type = type;
			this.label = label;
			this.outputDir = outputDir;
			this.prefix = prefix;
			this.calc = calc;
		}
		@Override
		public void run() {
			try {
				calc.plotMap(type, false, p, label, outputDir, prefix);
				System.out.println("Done with "+prefix);
			} catch (Exception e) {
				ExceptionUtils.throwAsRuntimeException(e);
			}
		}
	}

}
