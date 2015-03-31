package org.opensha.sha.cybershake.calc;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.distribution.BetaDistribution;
import org.apache.commons.math3.distribution.RealDistribution;
import org.jfree.data.Range;
import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.commons.data.function.DiscretizedFunc;
import org.opensha.commons.data.xyz.ArbDiscrGeoDataSet;
import org.opensha.commons.data.xyz.GeoDataSet;
import org.opensha.commons.data.xyz.GeoDataSetMath;
import org.opensha.commons.exceptions.GMT_MapException;
import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.LocationUtils;
import org.opensha.commons.gui.plot.GraphWindow;
import org.opensha.commons.gui.plot.HeadlessGraphPanel;
import org.opensha.commons.gui.plot.PlotCurveCharacterstics;
import org.opensha.commons.gui.plot.PlotLineType;
import org.opensha.commons.gui.plot.PlotSpec;
import org.opensha.commons.gui.plot.PlotSymbol;
import org.opensha.commons.util.DataUtils;
import org.opensha.commons.util.ExceptionUtils;
import org.opensha.commons.util.FileUtils;
import org.opensha.sha.calc.hazardMap.HazardDataSetLoader;
import org.opensha.sha.cybershake.HazardCurveFetcher;
import org.opensha.sha.cybershake.db.CachedPeakAmplitudesFromDB;
import org.opensha.sha.cybershake.db.CybershakeIM;
import org.opensha.sha.cybershake.db.CybershakeRun;
import org.opensha.sha.cybershake.db.CybershakeSite;
import org.opensha.sha.cybershake.db.CybershakeSiteInfo2DB;
import org.opensha.sha.cybershake.db.Cybershake_OpenSHA_DBApplication;
import org.opensha.sha.cybershake.db.DBAccess;
import org.opensha.sha.cybershake.db.MeanUCERF2_ToDB;
import org.opensha.sha.cybershake.db.Runs2DB;
import org.opensha.sha.cybershake.maps.HardCodedInterpDiffMapCreator;
import org.opensha.sha.earthquake.ERF;
import org.opensha.sha.faultSurface.EvenlyGriddedSurface;
import org.opensha.sha.faultSurface.RuptureSurface;
import org.opensha.sha.imr.AttenRelRef;
import org.opensha.sha.imr.ScalarIMR;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;
import com.google.common.primitives.Doubles;

public class ConditionalHypocenterDistribution implements RuptureVariationProbabilityModifier {
	
	private static boolean debug_plots = false;
	private static boolean add_random_noise = false;
	
	private static boolean bundle_hypos_by_name = false;
	
	private ERF erf;
	private DBAccess db;
	private int erfID;
	private int rvScenID;
	private RealDistribution dist;
	
	private Table<Integer, Integer, Map<Double, List<Integer>>> varProbsCache = HashBasedTable.create();
	
	public ConditionalHypocenterDistribution(ERF erf, DBAccess db, int erfID, int rvScenID, RealDistribution dist) {
		this.erf = erf;
		this.db = db;
		this.erfID = erfID;
		this.rvScenID = rvScenID;
		this.dist = dist;
	}
	
	private List<Location> loadRVHypos(int sourceID, int rupID) {
		String sql = "SELECT Rup_Var_ID,Hypocenter_Lat,Hypocenter_Lon,Hypocenter_Depth,Rup_Var_LFN FROM Rupture_Variations " +
				"WHERE ERF_ID=" + erfID + " AND Rup_Var_Scenario_ID=" + rvScenID + " " +
				"AND Source_ID=" + sourceID + " AND Rupture_ID=" + rupID;
		
		List<Location> locs = Lists.newArrayList();
		List<String> lfns = null;
		if (bundle_hypos_by_name)
			lfns = Lists.newArrayList();
		
		try {
			ResultSet rs = db.selectData(sql);
			boolean success = rs.first();
			while (success) {
				int rvID = rs.getInt("Rup_Var_ID");
				// make sure the list is big enough
				while (locs.size() <= rvID)
					locs.add(null);
				double lat = rs.getDouble("Hypocenter_Lat");
				double lon = rs.getDouble("Hypocenter_Lon");
				double depth = rs.getDouble("Hypocenter_Depth");
				if (lfns != null)
					lfns.add(rs.getString("Rup_Var_LFN"));
				Location loc = new Location(lat, lon, depth);
				
				locs.set(rvID, loc);

				success = rs.next();
			}
		} catch (SQLException e) {
			ExceptionUtils.throwAsRuntimeException(e);
		}
		
		for (int i=0; i<locs.size(); i++) {
			Location loc = locs.get(i);
			Preconditions.checkNotNull(loc, "RV "+i+" has no hypo for rup with "+locs.size()+" RVs. SQL:\n\t"+sql);
		}
		
		if (bundle_hypos_by_name) {
			Map<String, List<Integer>> hypoBundles = Maps.newHashMap();
			for (int i=0; i<lfns.size(); i++) {
				String lfn = lfns.get(i);
				lfn = lfn.substring(lfn.indexOf("-h"));
				Preconditions.checkState(!lfn.isEmpty());
				List<Integer> ids = hypoBundles.get(lfn);
				if (ids == null) {
					ids = Lists.newArrayList();
					hypoBundles.put(lfn, ids);
				}
				ids.add(i);
			}
			for (String lfn : hypoBundles.keySet()) {
				double lat = 0d;
				double lon = 0d;
				double depth = 0d;
				int cnt = 0;
				for (int id : hypoBundles.get(lfn)) {
					Location loc = locs.get(id);
					lat += loc.getLatitude();
					lon += loc.getLongitude();
					depth += loc.getDepth();
					cnt++;
				}
				lat /= (double)cnt;
				lon /= (double)cnt;
				depth /= (double)cnt;
				Location loc = new Location(lat, lon, depth);
				for (int id : hypoBundles.get(lfn))
					locs.set(id, loc);
			}
		}
		
		return locs;
	}
	
	private List<Double> calcDAS(List<Location> hypos, EvenlyGriddedSurface gridSurf) {
		int cols = gridSurf.getNumCols();
		
		List<Double> dasVals = Lists.newArrayList();
		for (Location loc : hypos) {
			double minDist = Double.POSITIVE_INFINITY;
			int closestColIndex = -1;
			for (int col=0; col<gridSurf.getNumCols(); col++) {
				double dist = LocationUtils.horzDistanceFast(loc, gridSurf.get(0, col));
				if (dist < minDist) {
					minDist = dist;
					closestColIndex = col;
				}
			}
			Preconditions.checkState(Doubles.isFinite(minDist));
			
			// find second closest for interpolation
			int secondClosestIndex = -1;
			double secondClosestDist = Double.POSITIVE_INFINITY;
			// check column before
			if (closestColIndex > 0) {
				secondClosestDist = LocationUtils.horzDistanceFast(loc, gridSurf.get(0, closestColIndex-1));
				secondClosestIndex = closestColIndex - 1;
			}
			if (closestColIndex + 1 < cols) {
				double dist = LocationUtils.horzDistanceFast(loc, gridSurf.get(0, closestColIndex+1));
				if (dist < secondClosestDist) {
					secondClosestDist = dist;
					secondClosestIndex = closestColIndex + 1;
				}
			}
			Preconditions.checkState(Doubles.isFinite(secondClosestDist));
			Preconditions.checkState(secondClosestDist >= minDist);
			
			// first calculate without interpolation
			double das = (double)closestColIndex;
			
			// now do interpolation
			double fractInterpolation = (minDist)/(minDist+secondClosestDist);
			
			if (closestColIndex < secondClosestIndex)
				das += fractInterpolation;
			else
				das -= fractInterpolation;
			
			// now normalize
			das /= (double)(cols-1);
			
			Preconditions.checkState(das >= 0 && das <= 1, "DAS outside of range [0 1]: "+das);
			
			dasVals.add(das);
		}
		return dasVals;
	}

	@Override
	public Map<Double, List<Integer>> getVariationProbs(int sourceID,
			int rupID, double originalProb, CybershakeRun run, CybershakeIM im) {
		if (varProbsCache.contains(sourceID, rupID))
			return varProbsCache.get(sourceID, rupID);
		RuptureSurface surf = erf.getSource(sourceID).getRupture(rupID).getRuptureSurface();
		Preconditions.checkState(surf instanceof EvenlyGriddedSurface, "Must be evenly gridded surface");
		EvenlyGriddedSurface gridSurf = (EvenlyGriddedSurface)surf;
		
		// list of hypocenters ordered by RV ID
		List<Location> hypos = loadRVHypos(sourceID, rupID);
		
		// calculate DAS for each hypocenter
		List<Double> dasVals = calcDAS(hypos, gridSurf);
		
		if (debug_plots && sourceID == 128 && rupID == 1296)
			debugPlotDAS(dasVals, hypos, true);
		
		List<Double> hypocenterProbs = Lists.newArrayList();
		double sumHypoProbs = 0d;
		for (int rvIndex=0; rvIndex<dasVals.size(); rvIndex++) {
			double das = dasVals.get(rvIndex);
			double hypocenterProb = dist.density(das);
			if (add_random_noise
//					&& Math.random() < 0.1
					) {
				double r = Math.random()-0.5;
				r *= (hypocenterProb*0.0001);
				double newProb = hypocenterProb + r;
				Preconditions.checkState(hypocenterProb != newProb);
				Preconditions.checkState(DataUtils.getPercentDiff(newProb, hypocenterProb) < 2d);
				hypocenterProb = newProb;
			}
			hypocenterProbs.add(hypocenterProb);
			sumHypoProbs += hypocenterProb;
		}
		
		// normalize
		for (int i=0; i<hypocenterProbs.size(); i++)
			hypocenterProbs.set(i, hypocenterProbs.get(i)/sumHypoProbs);
		
		if (debug_plots && sourceID == 128 && rupID == 1296)
			debugPlotProbVsDAS(dasVals, hypocenterProbs);
		
		Map<Double, List<Integer>> ret = Maps.newHashMap();
		for (int rvID=0; rvID<hypocenterProbs.size(); rvID++) {
			double hypocenterProb = hypocenterProbs.get(rvID);
			hypocenterProb *= originalProb;
			// test that hypo prob is uniform, only applicable when alpha=beta=1
//			double expected = (originalProb/(double)hypos.size());
//			Preconditions.checkState((float)hypocenterProb == (float)expected, hypocenterProb+" != "+expected);
			
			List<Integer> idsAtProb = ret.get(hypocenterProb);
			if (idsAtProb == null) {
				idsAtProb = Lists.newArrayList();
				ret.put(hypocenterProb, idsAtProb);
			}
			idsAtProb.add(rvID);
		}
		
		Map<Double, List<Integer>> fixedRet = Maps.newHashMap();
		for (double prob : ret.keySet()) {
			List<Integer> ids = ret.get(prob);
			fixedRet.put(prob*(double)ids.size(), ids);
		}
		ret = fixedRet;
		
		// make sure the sum of all probabilities equals origProb
		double runningProb = 0d;
		for (double hypoProb : ret.keySet())
			runningProb += hypoProb;
		Preconditions.checkState((float)runningProb == (float)originalProb,
				"total probability doesn't equal original: "+runningProb+" != "+originalProb);
		
		synchronized (this) {
			varProbsCache.put(sourceID, rupID, ret);
		}
		
		return ret;
	}
	
	private void debugPlotProbVsDAS(List<Double> dasVals, List<Double> probs) {
		ArbitrarilyDiscretizedFunc func = new ArbitrarilyDiscretizedFunc();
		
		for (int i=0; i<dasVals.size(); i++) {
			double das = dasVals.get(i);
			double prob = probs.get(i);
			func.set(das, prob);
		}
		
		List<DiscretizedFunc> funcs = Lists.newArrayList();
		funcs.add(func);
		List<PlotCurveCharacterstics> chars = Lists.newArrayList(
				new PlotCurveCharacterstics(PlotLineType.SOLID, 1f, PlotSymbol.CROSS, 4f, Color.BLACK));
		PlotSpec spec = new PlotSpec(funcs, chars, "Prob vs DAS", "DAS", "Prob");
		new GraphWindow(spec);
	}
	
	private void debugPlotDAS(List<Double> dasVals, List<Location> hypos, boolean latitude) {
		ArbitrarilyDiscretizedFunc func = new ArbitrarilyDiscretizedFunc();
		
		for (int i=0; i<dasVals.size(); i++) {
			double das = dasVals.get(i);
			Location hypo = hypos.get(i);
			double x;
			if (latitude)
				x = hypo.getLatitude();
			else
				x = hypo.getLongitude();
			func.set(x, das);
		}
		
		List<DiscretizedFunc> funcs = Lists.newArrayList();
		funcs.add(func);
		List<PlotCurveCharacterstics> chars = Lists.newArrayList(
				new PlotCurveCharacterstics(PlotLineType.SOLID, 1f, PlotSymbol.CROSS, 4f, Color.BLACK));
		String xLabel;
		if (latitude)
			xLabel = "Latitude";
		else
			xLabel = "Longitude";
		PlotSpec spec = new PlotSpec(funcs, chars, "DAS vs Hypo Loc", xLabel, "DAS");
		new GraphWindow(spec);
	}

	public static void main(String[] args) throws FileNotFoundException, ClassNotFoundException, IOException, GMT_MapException, SQLException {
		// CVM-S4i26, AWP GPU
		int velModelID = 5;
		int datasetID = 35;
		int erfID = 35;
		int rvScenID = 4;
		
		File outputDir = new File("/home/kevin/CyberShake/cond_hypo");
		
		RealDistribution dist = new BetaDistribution(2.5d, 2.5d);
		outputDir = new File(outputDir, "results_betadist_a2.5_b2.5");
		
//		RealDistribution dist = new BetaDistribution(1d, 1d);
//		outputDir = new File(outputDir, "results_betadist_a1.0_b1.0");
		
//		RealDistribution dist = new BetaDistribution(1d, 1d);
//		outputDir = new File(outputDir, "results_betadist_a1.0_b1.0_noise");
//		add_random_noise = true;
		
//		RealDistribution dist = new BetaDistribution(0.25d, 0.25d);
//		outputDir = new File(outputDir, "results_betadist_a0.25_b0.25");
		
//		RealDistribution dist = new BetaDistribution(0.5d, 0.5d);
//		outputDir = new File(outputDir, "results_betadist_a0.5_b0.5");
		
//		RealDistribution dist = new BetaDistribution(0.75d, 0.75d);
//		outputDir = new File(outputDir, "results_betadist_a0.75_b0.75");
		
		Preconditions.checkState(outputDir.exists() || outputDir.mkdir());
		File curveDir = new File(outputDir, "curves");
		Preconditions.checkState(curveDir.exists() || curveDir.mkdir());
		
		List<String> curvePlotSites = Lists.newArrayList("USC", "SBSM", "STNI", "PACI2");
		
		int imTypeID = 21; // geom mean, 3s SA
		String xAxisLabel = "3s SA";
		
		DBAccess db = Cybershake_OpenSHA_DBApplication.db;
		Runs2DB runs2db = new Runs2DB(db);
		CybershakeSiteInfo2DB sites2db = new CybershakeSiteInfo2DB(db);
		
		HazardCurveComputation calc = new HazardCurveComputation(db);
		// cached peak amps for quick calculation
		ERF erf = MeanUCERF2_ToDB.createUCERF2ERF();
		calc.setPeakAmpsAccessor(new CachedPeakAmplitudesFromDB(db,
				new File("/home/kevin/CyberShake/MCER/.amps_cache"), erf));
		calc.setRupVarProbModifier(new ConditionalHypocenterDistribution(erf, db, erfID, rvScenID, dist));
		
		// the point on the hazard curve we are plotting
		boolean isProbAt_IML = false;
		double val = 0.0004;
		
		// use this to find run IDs, will recalculate curves
		HazardCurveFetcher fetch = new HazardCurveFetcher(db, datasetID, imTypeID);
		CybershakeIM im = fetch.getIM();
		List<Integer> runIDs = fetch.getRunIDs();
		
		GeoDataSet scatter = new ArbDiscrGeoDataSet(true);
		GeoDataSet origScatter = new ArbDiscrGeoDataSet(true);
		for (int i=0; i<runIDs.size(); i++) {
			int runID = runIDs.get(i);
			CybershakeRun run = runs2db.getRun(runID);
			CybershakeSite site = sites2db.getSiteFromDB(run.getSiteID());
			if (site.type_id == CybershakeSite.TYPE_TEST_SITE)
				continue;
			DiscretizedFunc origCurve = fetch.getFuncs().get(i);
			List<Double> xVals = Lists.newArrayList();
			for (Point2D pt : origCurve)
				xVals.add(pt.getX());
			DiscretizedFunc curve = calc.computeHazardCurve(xVals, run, im);
			Location loc = fetch.getCurveSites().get(i).createLocation();
			
			double newVal = HazardDataSetLoader.getCurveVal(curve, isProbAt_IML, val);
			double origVal = HazardDataSetLoader.getCurveVal(origCurve, isProbAt_IML, val);
			
			scatter.set(loc, newVal);
			origScatter.set(loc, origVal);
			
			if (curvePlotSites.contains(site.short_name)) {
				System.out.println("Plotting curves for "+site.short_name);
				// plot curves
				List<DiscretizedFunc> curves = Lists.newArrayList();
				List<PlotCurveCharacterstics> chars = Lists.newArrayList();
				
				curves.add(origCurve);
				chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, PlotSymbol.FILLED_SQUARE, 4f, Color.BLACK));
				origCurve.setName("Original");
				curves.add(curve);
				chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, PlotSymbol.FILLED_CIRCLE, 4f, Color.BLUE));
				curve.setName("Cond. Hypo. Modified");
				
				PlotSpec spec = new PlotSpec(curves, chars, site.short_name, xAxisLabel, "Exceed. Prob");
				spec.setLegendVisible(true);
				
				HeadlessGraphPanel gp = new HeadlessGraphPanel();
				gp.setTickLabelFontSize(18);
				gp.setAxisLabelFontSize(20);
				gp.setPlotLabelFontSize(21);
				gp.setBackgroundColor(Color.WHITE);
				
//				gp.setUserBounds(new Range(1e-2, 3e0), new Range(1.0E-5, 0.2));
//				gp.drawGraphPanel(spec, true, true);
				gp.setUserBounds(new Range(0, 2), new Range(1.0E-6, 1));
				gp.drawGraphPanel(spec, false, true);
				gp.getCartPanel().setSize(1000, 800);
				File outputFile = new File(curveDir, site.short_name+"_comparison");
				gp.saveAsPNG(outputFile.getAbsolutePath()+".png");
				gp.saveAsPDF(outputFile.getAbsolutePath()+".pdf");
			}
		}
		
		Double customMin = 0d;
		Double customMax = 1d;
		
		ScalarIMR baseMapIMR = AttenRelRef.NGA_2008_4AVG.instance(null);
		HardCodedInterpDiffMapCreator.setTruncation(baseMapIMR, 3.0);
		
		System.out.println("Modified:");
		String addr = HardCodedInterpDiffMapCreator.getMap(scatter, false, velModelID, imTypeID,
				customMin, customMax, isProbAt_IML, val, baseMapIMR, false, "Cond Prob Modified");
		FileUtils.downloadURL(addr+"/interpolated.150.png", new File(outputDir, "cond_hypo_mod_map.png"));
		System.out.println("Orig:");
		addr = HardCodedInterpDiffMapCreator.getMap(origScatter, false, velModelID, imTypeID,
				customMin, customMax, isProbAt_IML, val, baseMapIMR, false, "Original Map");
		FileUtils.downloadURL(addr+"/interpolated.150.png", new File(outputDir, "cond_hypo_orig_map.png"));
		
		// now ratio
		String[] addrs = HardCodedInterpDiffMapCreator.getCompareMap(
				false, scatter, origScatter, imTypeID, "Cond. Hypo. Dist", true);
		FileUtils.downloadURL(addrs[0]+"/interpolated.150.png", new File(outputDir, "cond_hypo_diff_map.png"));
		FileUtils.downloadURL(addrs[1]+"/interpolated.150.png", new File(outputDir, "cond_hypo_ratio_map.png"));
		db.destroy();
		System.exit(0);
	}

}
