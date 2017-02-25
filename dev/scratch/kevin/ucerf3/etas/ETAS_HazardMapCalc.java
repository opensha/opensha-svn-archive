package scratch.kevin.ucerf3.etas;

import java.awt.Color;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.swing.SwingUtilities;

import org.dom4j.DocumentException;
import org.opensha.commons.data.Site;
import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.commons.data.function.DiscretizedFunc;
import org.opensha.commons.data.region.CaliforniaRegions;
import org.opensha.commons.data.siteData.OrderedSiteDataProviderList;
import org.opensha.commons.data.siteData.SiteDataValue;
import org.opensha.commons.data.siteData.SiteDataValueList;
import org.opensha.commons.data.xyz.GriddedGeoDataSet;
import org.opensha.commons.exceptions.GMT_MapException;
import org.opensha.commons.geo.GriddedRegion;
import org.opensha.commons.geo.Location;
import org.opensha.commons.gui.plot.GraphWindow;
import org.opensha.commons.mapping.gmt.GMT_Map;
import org.opensha.commons.mapping.gmt.elements.GMT_CPT_Files;
import org.opensha.commons.param.Parameter;
import org.opensha.commons.util.ExceptionUtils;
import org.opensha.commons.util.cpt.CPT;
import org.opensha.sha.calc.HazardCurveCalculator;
import org.opensha.sha.calc.hazardMap.BinaryHazardCurveReader;
import org.opensha.sha.calc.hazardMap.HazardDataSetLoader;
import org.opensha.sha.earthquake.AbstractERF;
import org.opensha.sha.gui.infoTools.IMT_Info;
import org.opensha.sha.imr.AttenRelRef;
import org.opensha.sha.imr.AttenuationRelationship;
import org.opensha.sha.imr.ScalarIMR;
import org.opensha.sha.imr.param.IntensityMeasureParams.PGA_Param;
import org.opensha.sha.imr.param.IntensityMeasureParams.SA_Param;
import org.opensha.sha.util.SiteTranslator;

import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import scratch.UCERF3.analysis.FaultBasedMapGen;
import scratch.UCERF3.erf.ETAS.ETAS_CatalogIO;
import scratch.UCERF3.erf.ETAS.ETAS_EqkRupture;

public class ETAS_HazardMapCalc {
	
	private List<List<ETAS_EqkRupture>> catalogs;
	private HashSet<Integer> faultIndexesTriggered;
	private GriddedRegion region;
	private DiscretizedFunc xVals;
	private DiscretizedFunc calcXVals;
	
	private int printModulus = 100;
	
	private DataInputStream in;
	int faultSiteIndex = 0;
	
	private int faultCurvesCalculated = 0;
	private DiscretizedFunc[] faultHazardCurves;
	
	private boolean calcInLogSpace = true;
	
	private int debugCurvePlotModulus = 0;
	private int debugStopIndex = 0;
	
	private int griddedCurvesCalculated = 0;
	private DiscretizedFunc[] griddedHazardCurves;
	
	private AttenRelRef gmpeRef;
	private String imtName;
	private double period;
	private List<SiteDataValueList<?>> siteData;
	private SiteTranslator siteTrans;
	private Deque<ScalarIMR> gmpeDeque;
	private Deque<HazardCurveCalculator> curveCalcDeque;
	
	public ETAS_HazardMapCalc(List<List<ETAS_EqkRupture>> catalogs, File precalcFile,
			GriddedRegion region, DiscretizedFunc xVals) throws IOException {
		this.catalogs = catalogs;
		this.region = region;
		this.xVals = xVals;
		
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
			in = new DataInputStream(new BufferedInputStream(new FileInputStream(precalcFile)));
			int numSites = in.readInt();
			Preconditions.checkState(numSites == region.getNodeCount(), "Binary file has %s grid nodes, region has %s",
					numSites, region.getNodeCount());
		}
	}
	
	public ETAS_HazardMapCalc(GriddedRegion region, File faultCurvesFile, File griddedCurvesFile) throws Exception {
		this.region = region;
		if (faultCurvesFile != null)
			faultHazardCurves = loadCurves(faultCurvesFile);
		if (griddedCurvesFile != null)
			griddedHazardCurves = loadCurves(griddedCurvesFile);
		
		if (faultHazardCurves != null)
			xVals = faultHazardCurves[0];
		else if (griddedHazardCurves != null)
			xVals = griddedHazardCurves[0];
		else
			throw new IllegalArgumentException("Must supply at least one curve file");
	}
	
	private DiscretizedFunc[] loadCurves(File curvesFile) throws Exception {
		BinaryHazardCurveReader reader = new BinaryHazardCurveReader(curvesFile.getAbsolutePath());
		
		DiscretizedFunc[] curves = new DiscretizedFunc[region.getNodeCount()];
		
		for (int i=0; i<curves.length; i++) {
			curves[i] = reader.nextCurve();
			Preconditions.checkNotNull(curves[i]);
		}
		
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
	
	public void calcFaultBased() throws IOException {
		Preconditions.checkState(faultSiteIndex == 0, "Can only process file once");
		
		faultHazardCurves = new DiscretizedFunc[region.getNodeCount()];
		
		ExecutorService executor = createExecutor();
		
		List<Future<Integer>> hazardFutures = Lists.newArrayList();
		
		System.out.println("Calculating");
		
		while (faultSiteIndex < region.getNodeCount()) {
			if (faultSiteIndex % printModulus == 0)
				System.out.println("Processing site "+faultSiteIndex);
			int index = faultSiteIndex;
			Map<Integer, double[]> vals = loadNextSite();
			
			Future<Integer> future = executor.submit(new FaultHazardCalcRunnable(index, vals), index);
			hazardFutures.add(future);
			
			if (debugStopIndex > 0 && index == debugStopIndex)
				break;
			
			if (debugCurvePlotModulus > 0 && index % debugCurvePlotModulus == 0)
				// siteIndex has been incremented already thus -1
				plotCurve(future); // asynchronous
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
		
		executor.shutdown();
	}
	
	private class FaultHazardCalcRunnable implements Runnable {
		
		private int index;
		private Map<Integer, double[]> rupVals;

		public FaultHazardCalcRunnable(int index, Map<Integer, double[]> rupVals) {
			super();
			this.index = index;
			this.rupVals = rupVals;
		}
		
		@Override
		public void run() {
			DiscretizedFunc curve;
			try {
				curve = calcFaultHazardCurve(rupVals);
			} catch (RuntimeException e) {
				e.printStackTrace();
				throw e;
			}
			
			synchronized (faultHazardCurves) {
				faultCurvesCalculated++;
				faultHazardCurves[index] = curve;
				
				if (faultCurvesCalculated % printModulus == 0)
					System.out.println("Calculated "+faultCurvesCalculated+" curves");
			}
		}
		
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
	
	/**
	 * Calculates a catalog based hazard curve. First computes a conditional exceedence curve for each individual
	 * catalog, then computes a hazard curve across all catalogs by summing the exceedence probabilities scaled
	 * by 1/numCatalogs.
	 * @param rupVals
	 * @return
	 */
	DiscretizedFunc calcFaultHazardCurve(Map<Integer, double[]> rupVals) {
		// first calculate conditional exceedence for each rupture
		Map<Integer, DiscretizedFunc> rupCondExceeds = Maps.newHashMap();
		for (int rupIndex : rupVals.keySet()) {
			DiscretizedFunc condExceed = calcXVals.deepClone(); // log space if applicable
			double[] vals = rupVals.get(rupIndex);
			double mean = vals[0];
			double stdDev = vals[1];
			
			for (int i=0; i<condExceed.size(); i++) {
				double exceedProb = AttenuationRelationship.getExceedProbability(
						mean, stdDev, condExceed.getX(i), null, null);
				condExceed.set(i, exceedProb);
			}
			rupCondExceeds.put(rupIndex, condExceed);
		}
		
		DiscretizedFunc curve = xVals.deepClone(); // linear space
		initializeCurve(curve, 0d);
		
		double rateEach = 1d/catalogs.size();
		
		for (List<ETAS_EqkRupture> catalog : catalogs) {
			DiscretizedFunc catalogCurve = calcXVals.deepClone(); // log space if applicable
			initializeCurve(catalogCurve, 1d);
			
			for (ETAS_EqkRupture rup : catalog) {
				if (rup.getFSSIndex() < 0 || !rupVals.containsKey(rup.getFSSIndex()))
					continue;
				
//				double[] vals = rupVals.get(rup.getFSSIndex());
//				double mean = vals[0];
//				double stdDev = vals[1];
//
//				for (int k=0; k<catalogCurve.size(); k++) {
//					// TODO allow truncation
//					double exceedProb = AttenuationRelationship.getExceedProbability(
//							mean, stdDev, catalogCurve.getX(k), null, null);
//					
//					// multiply this into the total non-exceedance probability
//					// (get the product of all non-eceedance probabilities)
//					catalogCurve.set(k, catalogCurve.getY(k) * (1d-exceedProb));
//				}
				DiscretizedFunc condExceed = rupCondExceeds.get(rup.getFSSIndex());
				for (int k=0; k<catalogCurve.size(); k++) {
					// multiply this into the total non-exceedance probability
					// (get the product of all non-eceedance probabilities)
					catalogCurve.set(k, catalogCurve.getY(k) * (1d-condExceed.getY(k)));
				}
			}
			
			// now convert from total non-exceed prob to total exceed prob
			for(int k=0; k<catalogCurve.size(); k++)
				catalogCurve.set(k, 1.0-catalogCurve.getY(k));
			
			for (int k=0; k<catalogCurve.size(); k++)
				curve.set(k, curve.getY(k) + rateEach*catalogCurve.getY(k));
		}
		
		return curve;
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
		new GraphWindow(faultHazardCurves[index], "Hazard Curve");
	}
	
	public void calcGridded(AttenRelRef gmpeRef, String imtName, double period, OrderedSiteDataProviderList provs, double resolution)
			throws IOException {
		this.gmpeRef = gmpeRef;
		this.imtName = imtName;
		this.period = period;
		if (provs != null) {
			System.out.print("Fetching site data...");
			siteData  = provs.getAllAvailableData(region.getNodeList());
			System.out.println("DONE.");
			siteTrans = new SiteTranslator();
		}
		
		griddedHazardCurves = new DiscretizedFunc[region.getNodeCount()];
		
		ExecutorService executor = createExecutor();
		
		List<Future<Integer>> hazardFutures = Lists.newArrayList();
		
		System.out.println("Creating Gridded Sources");
		ETAS_CatalogGridSourceProvider sources = new ETAS_CatalogGridSourceProvider(catalogs, resolution);
		AbstractERF griddedERF = sources.buildGriddedERF();
		
		System.out.println("Calculating");
		
		// for site params
		ScalarIMR gmpe = checkOutGMPE();
		
		for (int index=0; index<region.getNodeCount(); index++) {
			if (index % printModulus == 0)
				System.out.println("Processing site "+index);
			
			Site site = new Site(region.getLocation(index));
			for (Parameter<?> param : gmpe.getSiteParams())
				site.addParameter((Parameter<?>) param.clone());
			if (siteData != null) {
				List<SiteDataValue<?>> mySiteData = Lists.newArrayList();
				for (SiteDataValueList<?> vals : siteData)
					mySiteData.add(vals.getValue(index));
				for (Parameter<?> param : site)
					siteTrans.setParameterValue(param, mySiteData);
			}
			
			Future<Integer> future = executor.submit(new GriddedHazardCalcRunnable(griddedERF, index, site), index);
			hazardFutures.add(future);
			
			if (debugStopIndex > 0 && index == debugStopIndex)
				break;
			
			if (debugCurvePlotModulus > 0 && index % debugCurvePlotModulus == 0)
				// siteIndex has been incremented already thus -1
				plotCurve(future); // asynchronous
		}
		
		checkInGMPE(gmpe);
		
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
		
		executor.shutdown();
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
	
	private synchronized HazardCurveCalculator checkOutCurveCalc() {
		if (curveCalcDeque == null)
			curveCalcDeque = new ArrayDeque<HazardCurveCalculator>();
		if (curveCalcDeque.isEmpty()) {
			// build a new one
			return new HazardCurveCalculator();
		}
		return curveCalcDeque.pop();
	}
	
	private synchronized void checkInCurveCalc(HazardCurveCalculator calc) {
		curveCalcDeque.push(calc);
	}
	
	private class GriddedHazardCalcRunnable implements Runnable {
		
		private AbstractERF erf;
		private int index;
		private Site site;

		public GriddedHazardCalcRunnable(AbstractERF erf, int index, Site site) {
			super();
			this.erf = erf;
			this.index = index;
			this.site = site;
		}

		@Override
		public void run() {
			ScalarIMR gmpe = checkOutGMPE();
			gmpe.setParamDefaults();
			
			DiscretizedFunc curve = calcGriddedHazardCurve(gmpe, site, index, erf);
			
			synchronized (griddedHazardCurves) {
				griddedCurvesCalculated++;
				griddedHazardCurves[index] = curve;
				
				if (griddedCurvesCalculated % printModulus == 0)
					System.out.println("Calculated "+griddedCurvesCalculated+" curves");
			}
			
			checkInGMPE(gmpe);
		}
	}
	
	DiscretizedFunc calcGriddedHazardCurve(ScalarIMR gmpe, Site site, int index, AbstractERF erf) {
		HazardCurveCalculator calc = checkOutCurveCalc();
		
		DiscretizedFunc calcCurve = calcXVals.deepClone();
		gmpe.setSite(site);
		calc.getHazardCurve(calcCurve, site, gmpe, erf);
		
		DiscretizedFunc curve = xVals.deepClone();
		for (int i=0; i<curve.size(); i++)
			curve.set(i, calcCurve.getY(i));
		
		checkInCurveCalc(calc);
		
		return curve;
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
				DiscretizedFunc faultCurve = faultHazardCurves[i];
				DiscretizedFunc griddedCurve = griddedHazardCurves[i];
				curve = xVals.deepClone();
				for (int j=0; j<curve.size(); j++) {
					// TODO is this the best way?
					double rateFault = -Math.log(1d-faultCurve.getY(j));
					double rateGridded = -Math.log(1d-griddedCurve.getY(j));
					double sumRate = rateFault + rateGridded;
					double sumProb = 1d - Math.exp(-sumRate);
					curve.set(j, sumProb);
				}
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
		System.out.println("Generating map for p="+level);
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

	public static void main(String[] args) throws Exception {
//		File faultBasedPrecalc = new File("/home/kevin/OpenSHA/UCERF3/shakemap_precalc/"
//				+ "2017_02_21-NGAWest_2014_NoIdr-spacing0.1-no-site-effects/results_sa_1.0s.bin");
//		double spacing = 0.1d;
//		File faultBasedPrecalc = new File("/home/kevin/OpenSHA/UCERF3/shakemap_precalc/"
//				+ "2017_02_21-NGAWest_2014_NoIdr-spacing1.0-no-site-effects/results_sa_1.0s.bin");
//		double spacing = 1.0d;
		File faultBasedPrecalc = new File("/home/kevin/OpenSHA/UCERF3/shakemap_precalc/"
				+ "2017_02_23-NGAWest_2014_NoIdr-spacing0.05-site-effects-with-basin/results_pga.bin");
		double spacing = 0.05d;
		String imtName = PGA_Param.NAME;
		double period = Double.NaN;
		String imtLabel = "PGA";
		String imtFileLabel = "pga";
		File precalcDir = new File("/home/kevin/OpenSHA/UCERF3/etas/hazard/"
				+ "2017_02_24-mojave_m7_fulltd_descendents-NGA2-0.05-site-effects-with-basin");
//		String imtName = SA_Param.NAME;
//		double period = 1d;
//		String imtLabel = "1s Sa";
//		String imtFileLabel = "sa_1s";
		GriddedRegion region = new CaliforniaRegions.RELM_TESTING_GRIDDED(spacing);
		DiscretizedFunc xVals = new IMT_Info().getDefaultHazardCurve(SA_Param.NAME);
		AttenRelRef gmpeRef = AttenRelRef.NGAWest_2014_AVG_NOIDRISS;
		OrderedSiteDataProviderList provs = null;
		double griddedResolution = 0.01;
		
		File etasCatalogs = new File("/home/kevin/OpenSHA/UCERF3/etas/simulations/"
				+ "2016_02_19-mojave_m7-10yr-full_td-subSeisSupraNucl-gridSeisCorr-scale1.14-combined100k/"
				+ "results_descendents_m5_preserve.bin");
		File outputDir = new File(etasCatalogs.getParentFile(), "hazard_maps");
		Preconditions.checkState(outputDir.exists() || outputDir.mkdir());
		outputDir = new File(outputDir, faultBasedPrecalc.getParentFile().getName());
		Preconditions.checkState(outputDir.exists() || outputDir.mkdir());
		
		ETAS_HazardMapCalc calc;
		MapType[] types;
		if (precalcDir != null) {
			types = MapType.values();
			
			File faultCurvesFile = new File(precalcDir, "/results_"+imtFileLabel+"_fault.bin");
			File griddedCurvesFile = new File(precalcDir, "/results_"+imtFileLabel+"_gridded.bin");
			
			calc = new ETAS_HazardMapCalc(region, faultCurvesFile, griddedCurvesFile);
		} else {
			List<List<ETAS_EqkRupture>> catalogs = ETAS_CatalogIO.loadCatalogsBinary(etasCatalogs, 5d);
			
			calc = new ETAS_HazardMapCalc(catalogs, faultBasedPrecalc, region, xVals);
//			calc.debugCurvePlotModulus = 500;
//			calc.debugStopIndex = 500;
			Stopwatch watch = Stopwatch.createStarted();
			calc.calcFaultBased();
			long secs = watch.elapsed(TimeUnit.SECONDS);
			System.out.println("Fault based took "+secs+" secs");
			watch.stop();
			double curvesPerSecond = (double)calc.faultCurvesCalculated/(double)secs;
			System.out.println((float)curvesPerSecond+" curves/sec");
			
			// gridded
			if (gmpeRef == null) {
				types = new MapType[] {MapType.FAULT_ONLY};
			} else {
				types = MapType.values();
				watch.reset();
				watch.start();
				calc.calcGridded(gmpeRef, imtName, period, provs, griddedResolution);
				secs = watch.elapsed(TimeUnit.SECONDS);
				System.out.println("Gridded took "+secs+" secs");
				watch.stop();
				curvesPerSecond = (double)calc.faultCurvesCalculated/(double)secs;
				System.out.println((float)curvesPerSecond+" curves/sec");
			}
		}
		
//		for (int i=0; i<region.getNodeCount(); i+= 500)
//			calc.plotCurve(i);
		
		double[] probVals = { 0.1d, 0.01d, 0.001 };
		for (double p : probVals) {
			String probString;
			double p100 = p*100;
			if (p100 == Math.floor(p100))
				probString = (int)p100+"";
			else
				probString = (float)p100+"";
			String label = imtLabel+" @ "+probString+"% POE";
			String prefix = "map_"+imtFileLabel+"_p"+(float)p;
			
			for (MapType type : types)
				calc.plotMap(type, false, p, label, outputDir, prefix);
		}
		
	}

}
