package org.opensha.sha.cybershake.calc;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.opensha.commons.data.function.DiscretizedFunc;
import org.opensha.commons.data.function.LightFixedXFunc;
import org.opensha.commons.data.xyz.EvenlyDiscrXYZ_DataSet;
import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.LocationUtils;
import org.opensha.commons.gui.plot.GraphWindow;
import org.opensha.commons.gui.plot.PlotCurveCharacterstics;
import org.opensha.commons.gui.plot.PlotElement;
import org.opensha.commons.gui.plot.PlotLineType;
import org.opensha.commons.gui.plot.PlotSpec;
import org.opensha.commons.gui.plot.PlotSymbol;
import org.opensha.commons.gui.plot.jfreechart.xyzPlot.XYZPlotSpec;
import org.opensha.commons.gui.plot.jfreechart.xyzPlot.XYZPlotWindow;
import org.opensha.commons.mapping.gmt.elements.GMT_CPT_Files;
import org.opensha.commons.util.DataUtils;
import org.opensha.commons.util.ExceptionUtils;
import org.opensha.commons.util.cpt.CPT;
import org.opensha.sha.cybershake.db.CachedPeakAmplitudesFromDB;
import org.opensha.sha.cybershake.db.CybershakeIM;
import org.opensha.sha.cybershake.db.CybershakeRun;
import org.opensha.sha.cybershake.db.Cybershake_OpenSHA_DBApplication;
import org.opensha.sha.cybershake.db.DBAccess;
import org.opensha.sha.cybershake.db.ERF2DB;
import org.opensha.sha.cybershake.db.HazardCurve2DB;
import org.opensha.sha.cybershake.db.MeanUCERF2_ToDB;
import org.opensha.sha.cybershake.db.Runs2DB;
import org.opensha.sha.earthquake.ERF;
import org.opensha.sha.faultSurface.EvenlyGriddedSurface;
import org.opensha.sha.faultSurface.InterpolatedEvenlyGriddedSurface;
import org.opensha.sha.faultSurface.RuptureSurface;
import org.opensha.sha.gui.infoTools.IMT_Info;
import org.opensha.sha.imr.param.IntensityMeasureParams.SA_Param;

import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;

public class TaperedHypocenterRupVarProbModifier implements
		RuptureVariationProbabilityModifier {
	
	// in order to find a hypocenter's location on the rupture surface we resample
	// the rupture to be much higher resolution and then find the closest point.
	// this is that higher resolution, in km
//	private static final double[] surface_oversampling_resolutions = {1.0, 0.5, 0.25, 0.1};
	private static final double[] surface_oversampling_resolutions = {1.0, 0.5};
	
	// make sure that all hypocenters are within this distance of the chosen surface point
//	private static final double surface_distance_max = 1d;
	private static final double surface_distance_max = 100d;
	
	// if true, use the fast closest surf point algorithm
	private static final boolean loc_map_fast = true;
	
	// taper parameters
	private static final double xTaperBeginLoc = 0.2;
	private static final double xTaperBeginVal = 0.1;
	private static final double xTaperEndLoc = 0.8;
	private static final double xTaperEndVal = 0.1;
	
	private static final double yTaperBeginLoc = 0.4;
	private static final double yTaperBeginVal = 0.01;
	private static final double yTaperEndLoc = 0.8;
	private static final double yTaperEndVal = 0.1;
	
	private ERF erf;
	private ERF2DB erf2db;
	
	private LoadingCache<PointCacheKey, List<Point2D>> pointCache;
	private LoadingCache<EvenlyGriddedSurface, EvenlyGriddedSurface[]> surfCache;
	
	private static double maxLocDiscrepancyEncountered = 0d;
	
	public TaperedHypocenterRupVarProbModifier(ERF erf, DBAccess db) {
		this.erf = erf;
		this.erf2db = new ERF2DB(db);
		
		this.pointCache = CacheBuilder.newBuilder().maximumSize(100000).build(new PointCacheLoader());
		this.surfCache = CacheBuilder.newBuilder().maximumSize(10000).build(new SurfCacheLoader());
	}

	@Override
	public List<Double> getVariationProbs(int sourceID, int rupID,
			double originalProb, CybershakeRun run, CybershakeIM im) {
		List<Point2D> points;
		try {
			points = pointCache.get(new PointCacheKey(sourceID, rupID, run));
		} catch (ExecutionException e) {
			throw ExceptionUtils.asRuntimeException(e);
		}
		List<Double> probs = Lists.newArrayList();
		double sumProbs = 0d;
		for (Point2D point : points) {
			double prob = getRelativeProb(point.getX(), point.getY());
			sumProbs += prob;
			probs.add(prob);
		}
		// now rescale to original prob
		double scalar = originalProb/sumProbs;
		for (int i=0; i<probs.size(); i++)
			probs.set(i, probs.get(i)*scalar);
		return probs;
	}
	
	private static double getRelativeProb(double x, double y) {
		double xTaper = getTaper(x, xTaperBeginLoc, xTaperBeginVal, xTaperEndLoc, xTaperEndVal);
		double yTaper = getTaper(y, yTaperBeginLoc, yTaperBeginVal, yTaperEndLoc, yTaperEndVal);
		return xTaper * yTaper;
	}
	
	private static double getTaper(double val, double beginLoc, double beginVal,
			double endLoc, double endVal) {
		if (val < beginLoc)
			return beginVal + (val/beginLoc)*(1d-beginVal);
		else if (val > endLoc)
			return endVal + ((1d-val)/(1d-endLoc))*(1d-endVal);
		return 1d;
	}
	
	/**
	 * Key for accessing point on rupture surface cache
	 * @author kevin
	 *
	 */
	private class PointCacheKey {
		private final int erfID, sourceID, rupID, rvID;
		
		public PointCacheKey(int sourceID, int rupID, CybershakeRun run) {
			this.erfID = run.getERFID();
			this.sourceID = sourceID;
			this.rupID = rupID;
			this.rvID = run.getRupVarScenID();
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + erfID;
			result = prime * result + rupID;
			result = prime * result + rvID;
			result = prime * result + sourceID;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			PointCacheKey other = (PointCacheKey) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (erfID != other.erfID)
				return false;
			if (rupID != other.rupID)
				return false;
			if (rvID != other.rvID)
				return false;
			if (sourceID != other.sourceID)
				return false;
			return true;
		}

		private TaperedHypocenterRupVarProbModifier getOuterType() {
			return TaperedHypocenterRupVarProbModifier.this;
		}
	}
	
	private class PointCacheLoader extends CacheLoader<PointCacheKey, List<Point2D>> {

		@Override
		public List<Point2D> load(PointCacheKey key) throws Exception {
			RuptureSurface surf = getRuptureSurface(key.sourceID, key.rupID);
			List<Location> hypos = getHypocenters(key.sourceID, key.rupID, key.erfID, key.rvID);
			
			return getRelativeLocations(surf, hypos);
		}
	}
	
	private class SurfCacheLoader extends CacheLoader<EvenlyGriddedSurface, EvenlyGriddedSurface[]> {

		@Override
		public EvenlyGriddedSurface[] load(EvenlyGriddedSurface gridSurf)
				throws Exception {
			double origRes = gridSurf.getAveGridSpacing();
			boolean doneOrig = false;
			EvenlyGriddedSurface[] testSurfaces = new EvenlyGriddedSurface[surface_oversampling_resolutions.length];
			for (int i=0; i<testSurfaces.length; i++) {
				double interpRes = surface_oversampling_resolutions[i];
				double pDiff = DataUtils.getPercentDiff(interpRes, origRes);
				if (pDiff < 20d || interpRes > origRes) {
					if (doneOrig) {
						testSurfaces[i] = null;
					} else {
						// use original surface
						testSurfaces[i] = gridSurf;
						doneOrig = true;
					}
				} else {
					testSurfaces[i] = new InterpolatedEvenlyGriddedSurface(
							gridSurf, interpRes);
				}
			}
			return testSurfaces;
		}
		
	}
	
	private RuptureSurface getRuptureSurface(int sourceID, int rupID) {
		return erf.getRupture(sourceID, rupID).getRuptureSurface();
	}
	
	private List<Location> getHypocenters(int sourceID, int rupID, int erfID, int rvScenID) {
		Map<Integer, Location> hypoMap = erf2db.getHypocenters(erfID, sourceID, rupID, rvScenID);
		List<Location> ret = Lists.newArrayList();
		for (int i=0; i<hypoMap.size(); i++) {
			Location loc = hypoMap.get(i);
			Preconditions.checkNotNull(loc);
			Preconditions.checkState(loc.getLatitude() != 0d || loc.getLongitude() != 0d,
					"Empty hypo for source=%s, rup=%s, erf=%s, rvScen=%s", sourceID, rupID, erfID, rvScenID);
			ret.add(loc);
		}
		return ret;
	}
	
	private List<Point2D> getRelativeLocations(RuptureSurface surf, List<Location> locs) {
		Preconditions.checkState(surf instanceof EvenlyGriddedSurface,
				"Currently only implemented for EvenlyGirddedSurfaces. Compound (UCERF3) "
				+"surfaces will take more thought. Like do we taper the whole rupture, or each "
				+"individual fault?");
		
		EvenlyGriddedSurface gridSurf = (EvenlyGriddedSurface)surf;
		EvenlyGriddedSurface[] testSurfaces;
		try {
			testSurfaces = surfCache.get(gridSurf);
		} catch (ExecutionException e) {
			throw ExceptionUtils.asRuntimeException(e);
		}
		
		List<Point2D> points = null;
		
		for (int s=0; s<testSurfaces.length; s++) {
			EvenlyGriddedSurface testSurf = testSurfaces[s];
			
			int numRows = testSurf.getNumRows();
			int numCols = testSurf.getNumCols();
			
			int searchRadius;
			if (s == 0) {
				points = Lists.newArrayList();
				for (int i=0; i<locs.size(); i++)
					points.add(null);
				
				searchRadius = numRows;
				if (numCols > numRows)
					searchRadius = numCols;
			} else {
				// smart search radius based off of previous resolution
				double prevRes = testSurfaces[s-1].getAveGridSpacing();
				Preconditions.checkState(!Double.isNaN(prevRes));
				double searchRadiusFract = prevRes*1.5;
				searchRadius = distToIndex(searchRadiusFract, numCols)+1;
			}
			
			for (int i=0; i<locs.size(); i++) {
				Location loc = locs.get(i);
				Point2D prev = points.get(i);
				int startRow, startCol;
				if (prev == null) {
					startRow = 0;
					startCol = 0;
				} else {
					startRow = distToIndex(prev.getY(), numRows);
					startCol = distToIndex(prev.getX(), numCols);
				}
				Point2D closest;
				if (loc_map_fast)
					closest = findClosestSurfPointFast(testSurf, loc, startRow, startCol, searchRadius);
				else
					closest = findClosestSurfPointSlow(testSurf, loc, startRow, startCol, searchRadius);
				points.set(i, closest);
			}
		}
		
		// sanity test
		for (int i=0; i<locs.size(); i++) {
			Location loc = locs.get(i);
			Point2D pt = points.get(i);
			EvenlyGriddedSurface hiResSurf = testSurfaces[testSurfaces.length-1];
			int row = distToIndex(pt.getY(), hiResSurf.getNumRows());
			int col = distToIndex(pt.getX(), hiResSurf.getNumCols());
			Location test = hiResSurf.get(row, col);
			double dist = LocationUtils.linearDistance(loc, test);
			Preconditions.checkState(dist <= surface_distance_max,
					"Hypocenter location mapping outside of tolerance. Dist=%s km\n\tHypo: %s\n\tClosest: %s",
					dist, loc, test);
			
			maxLocDiscrepancyEncountered = Math.max(maxLocDiscrepancyEncountered, dist);
		}
		
//		// now oversample the rupture surface in order to map locations
//		InterpolatedEvenlyGriddedSurface interpSurf = new InterpolatedEvenlyGriddedSurface(
//				(EvenlyGriddedSurface)surf, surface_oversampling_resolution);
//		
//		List<Point2D> points = Lists.newArrayList();
//		
//		for (Location loc : locs) {
//			if (loc_map_fast)
//				points.add(findClosestSurfPointFast(interpSurf, loc));
//			else
//				points.add(findClosestSurfPointSlow(interpSurf, loc));
//		}
		
		return points;
	}
	
	private static double indexToFractDist(int index, int num) {
		return (double)index/(double)(num-1);
	}
	
	private static int distToIndex(double dist, int num) {
		return (int)(dist*(num - 1d) + 0.5);
	}
	
	/**
	 * This finds the point on the rupture surface by first searching by row then by column
	 * @param surf
	 * @param loc
	 * @return
	 */
	private static Point2D findClosestSurfPointFast(EvenlyGriddedSurface surf, Location loc,
			int startingRow, int startingCol, int searchRadius) {
		int numCols = surf.getNumCols();
		int numRows = surf.getNumRows();
		
		// first find the correct depth
		double hypoDepth = loc.getDepth();
		int closestRow = -1;
		double closestRowDepthDiff = Double.POSITIVE_INFINITY;
		for (int row=startingRow-searchRadius; row<=startingRow+searchRadius; row++) {
			if (row < 0)
				continue;
			if (row == numRows)
				break;
			double rowDepth = surf.get(row, 0).getDepth();
			double diff = Math.abs(hypoDepth - rowDepth);
			if (diff < closestRowDepthDiff) {
				closestRow = row;
				closestRowDepthDiff = diff;
			}
		}

		// now find the column
		int closestCol = -1;
		double closestColDepthDist = Double.POSITIVE_INFINITY;
		for (int col=startingCol-searchRadius; col<=startingCol+searchRadius; col++) {
			if (col < 0)
				continue;
			if (col == numCols)
				break;
			double dist = LocationUtils.linearDistanceFast(loc, surf.get(closestRow, col));
			if (dist < closestColDepthDist) {
				closestCol = col;
				closestColDepthDist = dist;
			}
		}
		
//		// now make sure we have the actual closest
//		int finalRow = closestRow;
//		int finalCol = closestCol;
//		double finalDist = LocationUtils.linearDistanceFast(loc, surf.get(finalRow, finalCol));
//		for (int row=closestRow-5; row<=closestRow+5; row++) {
//			if (row < 0)
//				continue;
//			if (row >= numRows)
//				break;
//			for (int col=closestCol-5; col<=closestCol+5; col++) {
//				if (col < 0)
//					continue;
//				if (col >= numCols)
//					break;
//				double dist = LocationUtils.linearDistanceFast(loc, surf.get(finalRow, finalCol));
//				if (dist < finalDist) {
//					finalRow = row;
//					finalCol = col;
//					finalDist = dist;
//				}
//			}
//		}
//		if (finalRow != closestRow || finalCol != closestCol)
//			System.out.println("Ended up modifying the point on surf.");
		
		double x = indexToFractDist(closestCol, numCols);
		double y = indexToFractDist(closestRow, numRows);
		
		return new Point2D.Double(x, y);
	}
	
	/**
	 * This finds the point on the rupture surface by brute force checking every surface loc
	 * @param surf
	 * @param loc
	 * @return
	 */
	private static Point2D findClosestSurfPointSlow(EvenlyGriddedSurface surf, Location loc,
			int startingRow, int startingCol, int searchRadius) {
		int numCols = surf.getNumCols();
		int numRows = surf.getNumRows();
		
		double minDist = Double.POSITIVE_INFINITY;
		int closestCol = -1;
		int closestRow = -1;
		
		for (int row=startingRow-searchRadius; row<=startingRow+searchRadius; row++) {
			if (row < 0)
				continue;
			if (row == numRows)
				break;
			for (int col=startingCol-searchRadius; col<=startingCol+searchRadius; col++) {
				if (col < 0)
					continue;
				if (col == numCols)
					break;
				double dist = LocationUtils.linearDistanceFast(loc, surf.get(row, col));
				if (dist < minDist) {
					minDist = dist;
					closestRow = row;
					closestCol = col;
				}
			}
		}
		
		double x = indexToFractDist(closestCol, numCols);
		double y = indexToFractDist(closestRow, numRows);
		
		return new Point2D.Double(x, y);
	}
	
	public static void main(String[] args) throws IOException {
		File outputDir = new File("/home/kevin/CyberShake/tapered_hypocenter_dist");
		
		// first plot regular taper
		EvenlyDiscrXYZ_DataSet xyz = new EvenlyDiscrXYZ_DataSet(101, 101, 0d, -1d, 0.01);
		for (int xInd=0; xInd<xyz.getNumX(); xInd++) {
			for (int yInd=0; yInd<xyz.getNumY(); yInd++) {
				Point2D point = xyz.getPoint(xInd + xyz.getNumX()*yInd);
				double val = getRelativeProb(point.getX(), -point.getY());
				xyz.set(xInd, yInd, val);
			}
		}
		xyz.scale(1d/xyz.getSumZ());
		CPT cpt = GMT_CPT_Files.MAX_SPECTRUM.instance().rescale(0d, xyz.getMaxZ());
		XYZPlotSpec xyzSpec = new XYZPlotSpec(xyz, cpt,
				"Hypocenter PDF", "Fraction along Strike", "Fraction Down Dip", "Probability");
		
		XYZPlotWindow xyzGW = new XYZPlotWindow(xyzSpec);
		xyzGW.getXYZPanel().saveAsPNG(new File(outputDir, "hypocenter_pdf_theoretical.png").getAbsolutePath());
		xyzGW.setDefaultCloseOperation(XYZPlotWindow.EXIT_ON_CLOSE);
		
		// now lets plot an actual RV
//		ERF erf = MeanUCERF2_ToDB.createUCERF2_200mERF();
		ERF erf = MeanUCERF2_ToDB.createUCERF2ERF();
		DBAccess db = Cybershake_OpenSHA_DBApplication.db;
		TaperedHypocenterRupVarProbModifier mod = new TaperedHypocenterRupVarProbModifier(erf, db);
		
		int runID = 3870; // LADT
		String siteName = "LADT";
		int imTypeID = 21;
		
		Runs2DB runs2db = new Runs2DB(db);
		CybershakeRun run = runs2db.getRun(runID);
		CybershakeIM imType = new HazardCurve2DB(db).getIMFromID(imTypeID);
		
		int sourceID = 90;
		int rupID = 0;
		double origProb = erf.getRupture(sourceID, rupID).getProbability();
		RuptureSurface surf = erf.getSource(sourceID).getRupture(rupID).getRuptureSurface();
		List<Double> rvProbs = mod.getVariationProbs(sourceID, rupID, origProb, run, null);
		List<Location> hypos = mod.getHypocenters(sourceID, rupID, run.getERFID(), run.getRupVarScenID());
		List<Point2D> points = mod.getRelativeLocations(surf, hypos);
		System.out.println("Calculated probs for "+rvProbs.size()+" RVs");
		Preconditions.checkState(hypos.size() == rvProbs.size());
		
		// now plot actual hypos as a function of lat
		List<PlotElement> funcs = Lists.newArrayList();
		List<PlotCurveCharacterstics> chars = Lists.newArrayList();
		
		double sumProbs = 0;
		double maxRelative = 0;
		List<Double> relativeProbs = Lists.newArrayList();
		for (int i=0; i<rvProbs.size(); i++) {
			double prob = rvProbs.get(i);
			sumProbs += prob;
			double relative = prob/origProb;
			maxRelative = Math.max(maxRelative, relative);
			relativeProbs.add(relative);
		}
		
		cpt = cpt.rescale(0d, maxRelative);
		
		for (int i=0; i<rvProbs.size(); i++) {
			Location loc = hypos.get(i);
			double x = loc.getLatitude();
			double y = -loc.getDepth();
			Color c = cpt.getColor(relativeProbs.get(i).floatValue());
			funcs.add(new LightFixedXFunc(new double[] {x}, new double[] {y}));
			chars.add(new PlotCurveCharacterstics(PlotSymbol.FILLED_CIRCLE, 4f, c));
		}
		
		Preconditions.checkState((float)sumProbs == (float)origProb, sumProbs+" != "+origProb);
		
		PlotSpec spec = new PlotSpec(funcs, chars, "Test Rupture", "Latitude", "Altitude");
		GraphWindow gw = new GraphWindow(spec);
		gw.saveAsPNG(new File(outputDir, "hypocenter_pdf_mojave.png").getAbsolutePath());
		gw.setDefaultCloseOperation(GraphWindow.EXIT_ON_CLOSE);
		
		funcs = Lists.newArrayList();
		chars = Lists.newArrayList();
		
		int numCols = ((EvenlyGriddedSurface)surf).getNumCols();
		for (int i=0; i<rvProbs.size(); i++) {
			int col = distToIndex(points.get(i).getX(), numCols);
			double y = -hypos.get(i).getDepth();
			Color c = cpt.getColor(relativeProbs.get(i).floatValue());
			funcs.add(new LightFixedXFunc(new double[] {(double)col}, new double[] {y}));
			chars.add(new PlotCurveCharacterstics(PlotSymbol.FILLED_CIRCLE, 4f, c));
		}
		
		Preconditions.checkState((float)sumProbs == (float)origProb, sumProbs+" != "+origProb);
		
		spec = new PlotSpec(funcs, chars, "Test Rupture", "Column", "Altitude");
		gw = new GraphWindow(spec);
		gw.saveAsPNG(new File(outputDir, "hypocenter_pdf_mojave.png").getAbsolutePath());
		gw.setDefaultCloseOperation(GraphWindow.EXIT_ON_CLOSE);
		
		// now plot actual hypos as a function of mapped
		
		System.out.println("Max discrepancy: "+maxLocDiscrepancyEncountered+" km");
		
		System.out.println("Now doing curve test for "+runID);
		HazardCurveComputation calc = new HazardCurveComputation(db);
		calc.setPeakAmpsAccessor(new CachedPeakAmplitudesFromDB(
				db, new File("/home/kevin/CyberShake/MCER/.amps_cache"), erf));
		List<Double> xVals = Lists.newArrayList();
		for (Point2D pt : new IMT_Info().getDefaultHazardCurve(SA_Param.NAME))
			xVals.add(pt.getX());
		System.out.println("Calculating Original Curve");
		DiscretizedFunc origCurve = calc.computeHazardCurve(xVals, run, imType);
		origCurve.setName("Original");
		calc.setRupVarProbModifier(mod);
		System.out.println("Calculating Modified Curve");
		Stopwatch watch = Stopwatch.createStarted();
		DiscretizedFunc modCurve = calc.computeHazardCurve(xVals, run, imType);
		watch.stop();
		System.out.println("Mod curve took "+watch.elapsed(TimeUnit.SECONDS)+" s");
		System.out.println("Point cache size: "+mod.pointCache.size());
		System.out.println("Surf cache size: "+mod.surfCache.size());
		modCurve.setName("Hypcoenter Prob Tapered");
		System.out.println("Done calculating");
		
		funcs = Lists.newArrayList();
		chars = Lists.newArrayList();
		
		funcs.add(origCurve);
		chars.add(new PlotCurveCharacterstics(
				PlotLineType.SOLID, 2f, PlotSymbol.FILLED_CIRCLE, 4f, Color.BLACK));
		funcs.add(modCurve);
		chars.add(new PlotCurveCharacterstics(
				PlotLineType.SOLID, 2f, PlotSymbol.FILLED_CIRCLE, 4f, Color.BLUE));
		
		spec = new PlotSpec(funcs, chars, siteName+" Comparison", "3sec SA (g)", "1-year POE");
		spec.setLegendVisible(true);
		gw = new GraphWindow(spec);
		gw.setXLog(true);
		gw.setYLog(true);
		gw.setX_AxisRange(1e-2, 3d);
		gw.setY_AxisRange(1e-8, 1e-1);
		gw.saveAsPNG(new File(outputDir, "curve_comparison_"+siteName+".png").getAbsolutePath());
		gw.setDefaultCloseOperation(GraphWindow.EXIT_ON_CLOSE);
		
		System.out.println("Max discrepancy: "+maxLocDiscrepancyEncountered+" km");
	}

}
