package scratch.kevin.ucerf3.etas;

import java.awt.Color;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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
import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.commons.data.function.DiscretizedFunc;
import org.opensha.commons.data.region.CaliforniaRegions;
import org.opensha.commons.data.xyz.GriddedGeoDataSet;
import org.opensha.commons.exceptions.GMT_MapException;
import org.opensha.commons.geo.GriddedRegion;
import org.opensha.commons.geo.Location;
import org.opensha.commons.gui.plot.GraphWindow;
import org.opensha.commons.mapping.gmt.GMT_Map;
import org.opensha.commons.mapping.gmt.elements.GMT_CPT_Files;
import org.opensha.commons.util.ExceptionUtils;
import org.opensha.commons.util.cpt.CPT;
import org.opensha.sha.calc.hazardMap.HazardDataSetLoader;
import org.opensha.sha.gui.infoTools.IMT_Info;
import org.opensha.sha.imr.AttenuationRelationship;
import org.opensha.sha.imr.param.IntensityMeasureParams.SA_Param;

import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import scratch.UCERF3.FaultSystemSolution;
import scratch.UCERF3.analysis.FaultBasedMapGen;
import scratch.UCERF3.erf.ETAS.ETAS_CatalogIO;
import scratch.UCERF3.erf.ETAS.ETAS_EqkRupture;
import scratch.UCERF3.utils.FaultSystemIO;

public class ETAS_HazardMapCalc {
	
	private FaultSystemSolution sol;
	private List<List<ETAS_EqkRupture>> catalogs;
	private HashSet<Integer> faultIndexesTriggered;
	private GriddedRegion region;
	private DiscretizedFunc xVals;
	private DiscretizedFunc calcXVals;
	
	private int printModulus = 100;
	
	private DataInputStream in;
	int siteIndex = 0;
	
	private int curvesCalculated = 0;
	private DiscretizedFunc[] hazardCurves;
	
	private boolean calcInLogSpace = true;
	
	private int debugCurvePlotModulus = 0;
	private int debugStopIndex = 0;
	
	public ETAS_HazardMapCalc(FaultSystemSolution sol, List<List<ETAS_EqkRupture>> catalogs, File precalcFile,
			GriddedRegion region, DiscretizedFunc xVals) throws IOException {
		this.sol = sol;
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
		
		in = new DataInputStream(new BufferedInputStream(new FileInputStream(precalcFile)));
		int numSites = in.readInt();
		Preconditions.checkState(numSites == region.getNodeCount(), "Binary file has %s grid nodes, region has %s",
				numSites, region.getNodeCount());
	}
	
	public void calcFaultBased() throws IOException {
		Preconditions.checkState(siteIndex == 0, "Can only process file once");
		
		hazardCurves = new DiscretizedFunc[region.getNodeCount()];
		
		int threads = Runtime.getRuntime().availableProcessors();
//		ExecutorService executor = Executors.newFixedThreadPool(threads);
		// max tasks in the pool at any given time, prevents pre loading too much data and using all memory
		// while waiting for hazard calculations to finish. When the queue is full, it will be run in this
		// thread, effectively blocking
		int maxTasks = threads * 10;
		ExecutorService executor = new ThreadPoolExecutor(threads, threads,
                0L, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<Runnable>(maxTasks), new ThreadPoolExecutor.CallerRunsPolicy());
		
		List<Future<Integer>> hazardFutures = Lists.newArrayList();
		
		System.out.println("Calculating");
		
		while (siteIndex < region.getNodeCount()) {
			if (siteIndex % printModulus == 0)
				System.out.println("Processing site "+siteIndex);
			int index = siteIndex;
			Map<Integer, double[]> vals = loadNextSite();
			
			Future<Integer> future = executor.submit(new HazardCalcRunnable(index, vals), index);
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
	
	private class HazardCalcRunnable implements Runnable {
		
		private int index;
		private Map<Integer, double[]> rupVals;

		public HazardCalcRunnable(int index, Map<Integer, double[]> rupVals) {
			super();
			this.index = index;
			this.rupVals = rupVals;
		}
		
		@Override
		public void run() {
			DiscretizedFunc curve = calcHazardCurve(rupVals);
			
			synchronized (hazardCurves) {
				curvesCalculated++;
				hazardCurves[index] = curve;
				
				if (curvesCalculated % printModulus == 0)
					System.out.println("Calculated "+curvesCalculated+" curves");
			}
		}
		
	}
	
	private Map<Integer, double[]> loadNextSite() throws IOException {
		int index = in.readInt();
		Preconditions.checkState(index == siteIndex, "Bad site index. Expected %s, encountered %s", index, siteIndex);
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
		
		siteIndex++;
		return rupVals;
	}
	
	/**
	 * Calculates a catalog based hazard curve. First computes a conditional exceedence curve for each individual
	 * catalog, then computes a hazard curve across all catalogs by summing the exceedence probabilities scaled
	 * by 1/numCatalogs.
	 * @param rupVals
	 * @return
	 */
	private DiscretizedFunc calcHazardCurve(Map<Integer, double[]> rupVals) {
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
		new GraphWindow(hazardCurves[index], "Hazard Curve");
	}
	
	public GriddedGeoDataSet calcMap(boolean isProbAt_IML, double level) {
		GriddedGeoDataSet map = new GriddedGeoDataSet(region, false);
		
		for (int i=0; i<map.size(); i++) {
			DiscretizedFunc curve = hazardCurves[i];
			
			double val = HazardDataSetLoader.getCurveVal(curve, isProbAt_IML, level);
			if (Double.isInfinite(val))
				val = Double.NaN;
			
			map.set(i, val);
		}
		
		return map;
	}
	
	public void plotMap(boolean isProbAt_IML, double level, String label, File outputDir, String prefix)
			throws IOException, GMT_MapException {
		GriddedGeoDataSet data = calcMap(isProbAt_IML, level);
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
		
		FaultBasedMapGen.plotMap(outputDir, prefix, false, map);
	}
	
	private static void initializeCurve(DiscretizedFunc curve, double val) {
		for (int i=0; i<curve.size(); i++)
			curve.set(i, val);
	}

	public static void main(String[] args) throws IOException, DocumentException, GMT_MapException {
		File faultBasedPrecalc = new File("/home/kevin/OpenSHA/UCERF3/shakemap_precalc/"
				+ "2017_02_21-NGAWest_2014_NoIdr-spacing0.1-no-site-effects/results_sa_1.0s.bin");
		double spacing = 0.1d;
//		File faultBasedPrecalc = new File("/home/kevin/OpenSHA/UCERF3/shakemap_precalc/"
//				+ "2017_02_21-NGAWest_2014_NoIdr-spacing1.0-no-site-effects/results_sa_1.0s.bin");
//		double spacing = 1.0d;
		String imtLabel = "1s Sa";
		String imtFileLabel = "sa_1s";
		GriddedRegion region = new CaliforniaRegions.RELM_TESTING_GRIDDED(spacing);
		DiscretizedFunc xVals = new IMT_Info().getDefaultHazardCurve(SA_Param.NAME);
		
		File fssFile = new File("/home/kevin/workspace/OpenSHA/dev/scratch/UCERF3/data/scratch/InversionSolutions/"
				+ "2013_05_10-ucerf3p3-production-10runs_COMPOUND_SOL_FM3_1_SpatSeisU3_MEAN_BRANCH_AVG_SOL.zip");
		
		File etasCatalogs = new File("/home/kevin/OpenSHA/UCERF3/etas/simulations/"
				+ "2016_02_19-mojave_m7-10yr-full_td-subSeisSupraNucl-gridSeisCorr-scale1.14-combined100k/"
				+ "results_descendents_m5_preserve.bin");
		
		File outputDir = new File(etasCatalogs.getParentFile(), "hazard_maps");
		Preconditions.checkState(outputDir.exists() || outputDir.mkdir());
		
		FaultSystemSolution sol = FaultSystemIO.loadSol(fssFile);
		List<List<ETAS_EqkRupture>> catalogs = ETAS_CatalogIO.loadCatalogsBinary(etasCatalogs, 5d);
		
		ETAS_HazardMapCalc calc = new ETAS_HazardMapCalc(sol, catalogs, faultBasedPrecalc, region, xVals);
//		calc.debugCurvePlotModulus = 500;
//		calc.debugStopIndex = 500;
		Stopwatch watch = Stopwatch.createStarted();
		calc.calcFaultBased();
		long secs = watch.elapsed(TimeUnit.SECONDS);
		System.out.println("Took "+secs+" secs");
		watch.stop();
		double curvesPerSecond = (double)calc.curvesCalculated/(double)secs;
		System.out.println((float)curvesPerSecond+" curves/sec");
		
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
			calc.plotMap(false, p, label, outputDir, prefix);
		}
		
	}

}
