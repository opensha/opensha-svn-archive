package org.opensha.sha.cybershake.maps;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.util.List;

import org.dom4j.DocumentException;
import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.commons.data.region.CaliforniaRegions;
import org.opensha.commons.data.siteData.SiteData;
import org.opensha.commons.data.siteData.impl.WillsMap2006;
import org.opensha.commons.data.xyz.ArbDiscrGeoDataSet;
import org.opensha.commons.data.xyz.GeoDataSet;
import org.opensha.commons.data.xyz.GeoDataSetMath;
import org.opensha.commons.exceptions.GMT_MapException;
import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.LocationUtils;
import org.opensha.commons.geo.Region;
import org.opensha.commons.mapping.gmt.GMT_Map;
import org.opensha.commons.mapping.gmt.elements.PSXYSymbol;
import org.opensha.commons.mapping.gmt.elements.PSXYSymbol.Symbol;
import org.opensha.commons.mapping.gmt.elements.PSXYSymbolSet;
import org.opensha.commons.mapping.gmt.elements.TopographicSlopeFile;
import org.opensha.commons.util.ExceptionUtils;
import org.opensha.commons.util.cpt.CPT;
import org.opensha.commons.util.cpt.CPTVal;
import org.opensha.sha.cybershake.HazardCurveFetcher;
import org.opensha.sha.cybershake.calc.mcer.CyberShakeDeterministicCalc;
import org.opensha.sha.cybershake.calc.mcer.DeterministicResult;
import org.opensha.sha.cybershake.calc.mcer.GMPEDeterministicComparisonCalc;
import org.opensha.sha.cybershake.calc.mcer.MCERDataProductsCalc;
import org.opensha.sha.cybershake.calc.mcer.RTGMCalc;
import org.opensha.sha.cybershake.db.CachedPeakAmplitudesFromDB;
import org.opensha.sha.cybershake.db.CybershakeIM;
import org.opensha.sha.cybershake.db.CybershakeIM.CyberShakeComponent;
import org.opensha.sha.cybershake.db.CybershakeSite;
import org.opensha.sha.cybershake.db.Cybershake_OpenSHA_DBApplication;
import org.opensha.sha.cybershake.db.DBAccess;
import org.opensha.sha.cybershake.db.MeanUCERF2_ToDB;
import org.opensha.sha.cybershake.db.Runs2DB;
import org.opensha.sha.earthquake.ERF;
import org.opensha.sha.earthquake.param.IncludeBackgroundOption;
import org.opensha.sha.earthquake.param.IncludeBackgroundParam;
import org.opensha.sha.earthquake.param.ProbabilityModelOptions;
import org.opensha.sha.earthquake.param.ProbabilityModelParam;
import org.opensha.sha.imr.AttenRelRef;
import org.opensha.sha.imr.AttenuationRelationship;
import org.opensha.sha.imr.attenRelImpl.MultiIMR_Averaged_AttenRel;

import scratch.UCERF3.analysis.FaultBasedMapGen;
import scratch.UCERF3.erf.FaultSystemSolutionERF;
import scratch.UCERF3.utils.FaultSystemIO;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

public class MCERMapGenerator {
	
	private static Region region = new CaliforniaRegions.CYBERSHAKE_MAP_REGION();
	private static final GMT_InterpolationSettings  interpSettings =
			GMT_InterpolationSettings.getDefaultSettings();
	
	private static boolean twoPercentIn50 = false;
	
	public static void calculateMaps(int datasetID, int imTypeID, double period, ERF erf,
			List<AttenuationRelationship> gmpes, SiteData<Double> vs30Prov, File outputDir)
					throws IOException, GMT_MapException {
		if (gmpes != null && gmpes.isEmpty())
			gmpes = null;
		ArbDiscrGeoDataSet probData = new ArbDiscrGeoDataSet(true);
		ArbDiscrGeoDataSet detData = new ArbDiscrGeoDataSet(true);
		ArbDiscrGeoDataSet detLowerLimit = new ArbDiscrGeoDataSet(true);
		
		DBAccess db = Cybershake_OpenSHA_DBApplication.db;
		
		HazardCurveFetcher fetcher = new HazardCurveFetcher(db, datasetID, imTypeID);
		CachedPeakAmplitudesFromDB amps2db = new CachedPeakAmplitudesFromDB(db, MCERDataProductsCalc.cacheDir, erf);
		Runs2DB runs2db = new Runs2DB(db);
		
		CyberShakeDeterministicCalc detCalc = new CyberShakeDeterministicCalc(amps2db, erf);
		
		List<CybershakeSite> sites = fetcher.getCurveSites();
		CybershakeIM im = fetcher.getIM();
		List<Integer> runIDs = fetcher.getRunIDs();
		List<Double> detVals = fetcher.calcDeterministic(detCalc);
		
		ArbitrarilyDiscretizedFunc xValsFunc = new ArbitrarilyDiscretizedFunc();
		xValsFunc.set(period, 0d);
		
		List<AttenuationRelationship> probGMPEs = null;
		ArbDiscrGeoDataSet gmpeProbData = null;
		ArbDiscrGeoDataSet gmpeDetData = null;
		File gmpeProbCache = null;
		File gmpeDetCache = null;
		if (gmpes != null) {
			if (gmpes.size() == 1) {
				probGMPEs = gmpes;
			} else {
				probGMPEs = Lists.newArrayList();
				probGMPEs.add(new MultiIMR_Averaged_AttenRel(gmpes));
			}
			
			gmpeProbCache = getGMPECacheFile(true, gmpes, datasetID, imTypeID, outputDir);
			gmpeDetCache = getGMPECacheFile(false, gmpes, datasetID, imTypeID, outputDir);
			// see if cached
			if (gmpeProbCache.exists())
				gmpeProbData = ArbDiscrGeoDataSet.loadXYZFile(gmpeProbCache.getAbsolutePath(), true);
			else
				gmpeProbData = new ArbDiscrGeoDataSet(true);
			if (gmpeDetCache.exists())
				gmpeDetData = ArbDiscrGeoDataSet.loadXYZFile(gmpeDetCache.getAbsolutePath(), true);
			else
				gmpeDetData = new ArbDiscrGeoDataSet(true);
		}
		
		CyberShakeComponent comp = im.getComponent();
		Preconditions.checkNotNull(comp);
		
		for (int i=0; i<sites.size(); i++) {
			CybershakeSite site = sites.get(i);
			if (site.type_id == CybershakeSite.TYPE_TEST_SITE)
				continue;
			Location loc = site.createLocation();
			if (probData.contains(loc))
				continue;
			
			int runID = runIDs.get(i);
			RTGMCalc probCalc = new RTGMCalc(runID, comp, null, db);
			probCalc.setUse2PercentIn50(twoPercentIn50);
			probCalc.setForceSingleIMTypeID(imTypeID);
			if (probGMPEs != null && !gmpeProbData.contains(loc))
				probCalc.setGMPEs(erf, probGMPEs);
			Preconditions.checkState(probCalc.calc());
			double probVal = probCalc.getCSSpectrumMap().get(comp).getY(im.getVal());
			
			probData.set(loc, probVal);
			detData.set(loc, detVals.get(i));
			// det lower limit
			double vs30 = vs30Prov.getValue(loc);
			detLowerLimit.set(loc, MCERDataProductsCalc.calcASCE_DetLowerLimit(xValsFunc, vs30, loc).getY(period));
			
			if (probGMPEs != null) {
				if (!gmpeProbData.contains(loc)) {
					double gmpeProbVal = probCalc.getGMPESpectrumMap().get(comp).get(0).getY(im.getVal());
					gmpeProbData.set(loc, gmpeProbVal);
				}
				
				if (!gmpeDetData.contains(loc)) {
					// calculate deterministic
					GMPEDeterministicComparisonCalc gmpeDetCalc = new GMPEDeterministicComparisonCalc(runs2db.getRun(runID),
							site, comp, Lists.newArrayList(im.getVal()), CyberShakeDeterministicCalc.percentile, erf, gmpes, null);
					gmpeDetCalc.calc();
					double maxGMPEDet = 0;
					for (DeterministicResult result : gmpeDetCalc.getResults().rowMap().get(im.getVal()).values())
						maxGMPEDet = Math.max(maxGMPEDet, result.getVal());
					
					gmpeDetData.set(loc, maxGMPEDet);
				}
			}
			System.out.println("Completed site "+i+"/"+sites.size());
		}
		
		if (gmpes != null) {
			// write out gmpe caches
			ArbDiscrGeoDataSet.writeXYZFile(gmpeProbData, gmpeProbCache);
			ArbDiscrGeoDataSet.writeXYZFile(gmpeDetData, gmpeDetCache);
		}
		
		File psvDir = new File(outputDir, "psv");
		Preconditions.checkState(psvDir.exists() || psvDir.mkdir());
		generateMaps(probData, detData, detLowerLimit, gmpeProbData, gmpeDetData, psvDir, period, true);
		File saDir = new File(outputDir, "sa");
		Preconditions.checkState(saDir.exists() || saDir.mkdir());
		generateMaps(probData, detData, detLowerLimit, gmpeProbData, gmpeDetData, saDir, period, false);
	}
	
	private static File getGMPECacheFile(boolean prob, List<AttenuationRelationship> gmpes, int datasetID,
			int imTypeID, File outputDir) {
		String name;
		if (prob)
			name = ".gmpe_prob";
		else
			name = ".gmpe_det";
		name += "_dataset"+datasetID;
		name += "_im"+imTypeID;
		for (AttenuationRelationship gmpe : gmpes)
			name += "_"+gmpe.getShortName();
		name += ".txt";
		return new File(outputDir, name);
	}
	
	public static void generateMaps(GeoDataSet probData, GeoDataSet detData,
			GeoDataSet detLowerLimit, File outputDir, double period, boolean psv)
					throws IOException, GMT_MapException {
		generateMaps(probData, detData, detLowerLimit, null, null, outputDir, period, psv);
	}
	
	public static void generateMaps(GeoDataSet probData, GeoDataSet detData,
			GeoDataSet detLowerLimit, GeoDataSet gmpeProbData, GeoDataSet gmpeDetData,
			File outputDir, double period, boolean psv) throws IOException, GMT_MapException {
		Preconditions.checkArgument(probData != null || detData != null || detLowerLimit != null);
		boolean generateMCER = probData != null && detData != null;
		if (generateMCER && detLowerLimit == null)
			System.err.println("WARNING: Calculating MCER without deterministic lower limit");
		
		CPT cpt = buildCPT(period, psv);
		
		String units = (float)period+"s";
		String prefixAdd;
		if (psv) {
			units += " PSV (cm/sec)";
			prefixAdd = "_psv";
		} else {
			units += " Sa (g)";
			prefixAdd = "_sa";
		}
		
		if (probData != null) {
			generateMaps(probData, outputDir, period, psv, "prob_mcer"+prefixAdd, "Prob. MCE@-R@-, "+units, cpt, true);
			if (gmpeProbData != null)
				generateMaps(gmpeProbData, outputDir, period, psv, "gmpe_prob_mcer"+prefixAdd,
						"GMPE Prob. MCE@-R@-, "+units, cpt, true);
		}
		
		if (detData != null) {
			generateMaps(detData, outputDir, period, psv, "det_mcer"+prefixAdd, "Det. MCE@-R@-, "+units, cpt, true);
			if (gmpeDetData != null)
				generateMaps(gmpeDetData, outputDir, period, psv, "gmpe_det_mcer"+prefixAdd,
						"GMPE Det. MCE@-R@-, "+units, cpt, true);
		}
		
		if (detLowerLimit != null) {
			generateMaps(detLowerLimit, outputDir, period, psv, "det_lower_limit"+prefixAdd,
					"Det. Lower Limit, "+units, cpt, true);
		}
		
		if (probData != null && detData != null && detLowerLimit != null) {
			ArbDiscrGeoDataSet combinedData = new ArbDiscrGeoDataSet(probData.isLatitudeX());
			for (Location loc : probData.getLocationList()) {
				double pVal = probData.get(loc);
				double dVal = detData.get(loc);
				double dLowVal = detLowerLimit.get(loc);
				
				double combinedVal = MCERDataProductsCalc.calcMCER(dVal, pVal, dLowVal);
				combinedData.set(loc, combinedVal);
			}
			generateMaps(combinedData, outputDir, period, psv, "combined_mcer"+prefixAdd, "Combined MCE@-R@-, "+units, cpt, true);
			if (gmpeProbData != null && gmpeDetData != null) {
				ArbDiscrGeoDataSet gmpeCombinedData = new ArbDiscrGeoDataSet(probData.isLatitudeX());
				for (Location loc : probData.getLocationList()) {
					double pVal = gmpeProbData.get(loc);
					double dVal = gmpeDetData.get(loc);
					double dLowVal = detLowerLimit.get(loc);
					
					double combinedVal = MCERDataProductsCalc.calcMCER(dVal, pVal, dLowVal);
					gmpeCombinedData.set(loc, combinedVal);
				}
				generateMaps(gmpeCombinedData, outputDir, period, psv, "gmpe_combined_mcer"+prefixAdd,
						"GMPE Combined MCE@-R@-, "+units, cpt, true);
				
				// now ratio
				GeoDataSet ratioData = GeoDataSetMath.divide(combinedData, gmpeCombinedData);
				CPT ratioCPT = CyberShake_GMT_MapGenerator.getRatioCPT();
				generateMaps(ratioData, outputDir, period, false, "gmpe_combined_ratio",
						"CyberShake/GMPE MCE@-R@- Ratio, "+units, ratioCPT, false);
				// log verions
//				CPT ratioCPT = CyberShake_GMT_MapGenerator.getRatioCPT();
//				CPT tempCPT = (CPT) ratioCPT.clone();
//				tempCPT.clear();
//				tempCPT.add(new CPTVal(-1f, ratioCPT.getMinColor(), 0f, ratioCPT.getColor(1f)));
//				tempCPT.add(new CPTVal(0f, ratioCPT.getColor(1f), 1f, ratioCPT.getMaxColor()));
//				ratioCPT = tempCPT;
//				generateMaps(ratioData, outputDir, period, false, "gmpe_combined_ratio",
//						"CyberShake/GMPE MCER Ratio", ratioCPT, true);
				// linear fractional diff
				// (CyberShake - GMPE)/GMPE
//				GeoDataSet fractDiffData = GeoDataSetMath.divide(
//						GeoDataSetMath.subtract(combinedData, gmpeCombinedData), gmpeCombinedData);
//				CPT ratioCPT = CyberShake_GMT_MapGenerator.getRatioCPT();
//				CPT tempCPT = (CPT) ratioCPT.clone();
//				tempCPT.clear();
//				tempCPT.add(new CPTVal(-1f, ratioCPT.getMinColor(), 0f, ratioCPT.getColor(1f)));
//				tempCPT.add(new CPTVal(0f, ratioCPT.getColor(1f), 1f, ratioCPT.getMaxColor()));
//				ratioCPT = tempCPT;
//				generateMaps(fractDiffData, outputDir, period, false, "gmpe_combined_ratio",
//						"(CyberShake-GMPE)/GMPE MCER", ratioCPT, false);
			}
			
			// now governing scatter
			GMT_Map govMap = buildGoverningScatterMap(probData, detData, detLowerLimit, "Governing Scatter");
			FaultBasedMapGen.plotMap(outputDir, "governing_scatter"+prefixAdd, false, govMap);
			if (gmpeProbData != null && gmpeDetData != null) {
				govMap = buildGoverningScatterMap(gmpeProbData, gmpeDetData, detLowerLimit, "GMPE Governing Scatter");
				FaultBasedMapGen.plotMap(outputDir, "gmpe_governing_scatter"+prefixAdd, false, govMap);
			}
		}
	}
	
	public static void generateGMPEOnlyMaps(List<ERF> erfs, List<String> erfNames, List<AttenuationRelationship> gmpes,
			int datasetID, int imTypeID, double period, SiteData<Double> vs30Prov, File outputDir)
					throws GMT_MapException, IOException {
		DBAccess db = Cybershake_OpenSHA_DBApplication.db;
		HazardCurveFetcher fetcher = new HazardCurveFetcher(db, datasetID, imTypeID);
		
		List<CybershakeSite> sites = fetcher.getCurveSites();
		List<Integer> runIDs = fetcher.getRunIDs();
		Preconditions.checkState(sites.size() == runIDs.size());
		CybershakeIM im = fetcher.getIM();
		CyberShakeComponent comp = im.getComponent();
		
		List<GeoDataSet> probDatas = Lists.newArrayList();
		List<File> cacheFiles = Lists.newArrayList();
		for (int i=0; i<erfs.size(); i++) {
			String name = erfNames.get(i);
			File cacheFile = getGMPECacheFile(true, gmpes, datasetID, imTypeID, outputDir);
			cacheFile = new File(cacheFile.getParentFile(), cacheFile.getName().replaceAll(".txt", "")+"_"+name+".txt");
			cacheFiles.add(cacheFile);
			GeoDataSet probData;
			if (cacheFile.exists())
				probData = ArbDiscrGeoDataSet.loadXYZFile(cacheFile.getAbsolutePath(), true);
			else
				probData = new ArbDiscrGeoDataSet(true);
			probDatas.add(probData);
		}
		
		List<AttenuationRelationship> probGMPEs = null;
		if (gmpes != null) {
			if (gmpes.size() == 1) {
				probGMPEs = gmpes;
			} else {
				probGMPEs = Lists.newArrayList();
				probGMPEs.add(new MultiIMR_Averaged_AttenRel(gmpes));
			}
		}
		
		for (int i=0; i<sites.size(); i++) {
			CybershakeSite site = sites.get(i);
			if (site.type_id == CybershakeSite.TYPE_TEST_SITE)
				continue;
			Location loc = site.createLocation();
			
			for (int j=0; j<erfs.size(); j++) {
				GeoDataSet probData = probDatas.get(j);
				if (probData.contains(loc))
					continue;
				ERF erf = erfs.get(j);
				RTGMCalc probCalc = new RTGMCalc(runIDs.get(i), comp, null, db);
				probCalc.setUse2PercentIn50(twoPercentIn50);
				probCalc.setForceSingleIMTypeID(imTypeID);
				probCalc.setGMPEs(erf, probGMPEs);
				try {
					Preconditions.checkState(probCalc.calc());
				} catch (IOException e) {
					ExceptionUtils.throwAsRuntimeException(e);
				}
				double gmpeProbVal = probCalc.getGMPESpectrumMap().get(comp).get(0).getY(im.getVal());
				probData.set(loc, gmpeProbVal);
			}
		}
		
		CPT ratioCPT = CyberShake_GMT_MapGenerator.getRatioCPT();
		
		String units = (float)period+"s";
		
		String dataType;
		if (twoPercentIn50)
			dataType = "2% in 50y";
		else
			dataType = "MCE@-R@-";
		
		for (boolean psv : new boolean[] { true, false }) {
			String prefixAdd;
			if (psv) {
				units += " PSV (cm/sec)";
				prefixAdd = "_psv";
			} else {
				units += " Sa (g)";
				prefixAdd = "_sa";
			}
			CPT cpt = buildCPT(period, psv);
			for (int i=0; i<erfs.size(); i++) {
				String name1 = erfNames.get(i);
				GeoDataSet data1 = probDatas.get(i);
				
				generateMaps(data1, outputDir, period, psv, name1+"_prob"+prefixAdd, name1+" GMPE Prob Map", cpt, true);
				for (int j=i+1; j<erfs.size(); j++) {
					String name2 = erfNames.get(j);
					GeoDataSet data2 = probDatas.get(j);
					
					GeoDataSet ratioData = GeoDataSetMath.divide(data1, data2);
					// find min/max
					double maxRatio = 0d;
					Location maxRatioLoc = null;
					double minRatio = Double.POSITIVE_INFINITY;
					Location minRatioLoc = null;
					
					for (int ind=0; ind<ratioData.size(); ind++) {
						double r = ratioData.get(ind);
						if (r > maxRatio) {
							maxRatio = r;
							maxRatioLoc = ratioData.getLocation(ind);
						}
						if (r < minRatio) {
							minRatio = r;
							minRatioLoc = ratioData.getLocation(ind);
						}
					}
					System.out.println("Max ratio: "+maxRatio+" at "
							+findClosestSite(sites, maxRatioLoc).short_name+", "+maxRatioLoc);
					System.out.println("Min ratio: "+minRatio+" at "
							+findClosestSite(sites, minRatioLoc).short_name+", "+minRatioLoc);
					generateMaps(ratioData, outputDir, period, false, name1+"_vs_"+name2+"_prob_ratio"+prefixAdd,
							"GMPE "+name1+"/"+name2+" Prob. "+dataType+" Ratio, "+units, ratioCPT, false);
				}
			}
		}
		
		// write out caches
		for (int i=0; i<cacheFiles.size(); i++)
			ArbDiscrGeoDataSet.writeXYZFile(probDatas.get(i), cacheFiles.get(i));
	}
	
	private static CybershakeSite findClosestSite(List<CybershakeSite> sites, Location loc) {
		CybershakeSite closest = null;
		double minDist = Double.POSITIVE_INFINITY;
		
		for (CybershakeSite site : sites) {
			double dist = LocationUtils.horzDistanceFast(loc, site.createLocation());
			if (dist < minDist) {
				minDist= dist;
				closest = site;
			}
		}
		
		return closest;
	}
	
	private static void generateMaps(GeoDataSet data, File outputDir, double period, boolean psv, String prefix,
			String title, CPT cpt, boolean log) throws GMT_MapException, IOException {
		GMT_Map map = buildScatterMap(data, psv, period, title, cpt, log);
		// I hate this hack...but don't want to add more variables
		if (!log && title.toLowerCase().contains("ratio"))
			map.setCPTEqualSpacing(true);
		FaultBasedMapGen.plotMap(outputDir, prefix+"_marks", false, map);
		map.setSymbolSet(null);
		FaultBasedMapGen.plotMap(outputDir, prefix, false, map);
		map.setContourIncrement(0.1);
		FaultBasedMapGen.plotMap(outputDir, prefix+"_contours", false, map);
		map.setContourOnly(true);
		FaultBasedMapGen.plotMap(outputDir, prefix+"_contours_only", false, map);
	}
	
	private static void applyGMTSettings(GMT_Map map, CPT cpt, String label) {
		map.setInterpSettings(interpSettings);
		map.setLogPlot(false); // already did manually
		map.setMaskIfNotRectangular(true);
		map.setTopoResolution(TopographicSlopeFile.CA_THREE);
//		map.setTopoResolution(null);
		map.setBlackBackground(false);
		map.setCustomScaleMin((double)cpt.getMinValue());
		map.setCustomScaleMax((double)cpt.getMaxValue());
		map.setCustomLabel(label);
		map.setRescaleCPT(false);
//		map.setDpi(150);
	}
	
	private static GMT_Map buildScatterMap(GeoDataSet data, boolean psv, double period, String label, CPT cpt, boolean log) {
		data = data.copy();
		if (psv)
			for (int index=0; index<data.size(); index++)
				data.set(index, RTGMCalc.saToPsuedoVel(data.get(index), period));
		if (log) {
			data.log10();
			label = "Log@-10@-("+label+")";
		}
		
		GMT_Map map = new GMT_Map(region, data, interpSettings.getInterpSpacing(), cpt);
		applyGMTSettings(map, cpt, label);
		
		// now add scatter
		PSXYSymbolSet xySet = new PSXYSymbolSet();
		CPT xyCPT = new CPT(0d, 1d, Color.WHITE, Color.WHITE);
		xySet.setCpt(xyCPT);
		for (Location loc : data.getLocationList()) {
			PSXYSymbol sym = new PSXYSymbol(new Point2D.Double(loc.getLongitude(), loc.getLatitude()),
					Symbol.INVERTED_TRIANGLE, 0.08f, 0f, null, Color.WHITE);
			xySet.addSymbol(sym, 0d);
//			symbols.add(sym);
		}
//		map.setSymbols(symbols);
		map.setSymbolSet(xySet);
		
		return map;
	}
	
	private static GMT_Map buildGoverningScatterMap(GeoDataSet probData, GeoDataSet detData,
			GeoDataSet detLowerLimit, String label) {
		GeoDataSet data = new ArbDiscrGeoDataSet(probData.isLatitudeX());
		
		// 0: prob, BLUE
		// 1: det, RED
		// 2: det lower, GRAY
		CPT xyCPT = new CPT();
		xyCPT.setBelowMinColor(Color.BLUE);
		xyCPT.add(new CPTVal(0f, Color.BLUE, 0.5f, Color.BLUE));
		xyCPT.add(new CPTVal(0.5f, Color.RED, 1.5f, Color.RED));
		xyCPT.add(new CPTVal(1.5f, Color.GRAY, 2f, Color.GRAY));
		xyCPT.setAboveMaxColor(Color.GRAY);
		
		for (Location loc : probData.getLocationList()) {
			double pVal = probData.get(loc);
			double dVal = detData.get(loc);
			double dLowVal = detLowerLimit.get(loc);
			
			double combinedVal = MCERDataProductsCalc.calcMCER(dVal, pVal, dLowVal);
			if (combinedVal == pVal)
				data.set(loc, 0d);
			else if (combinedVal == dVal)
				data.set(loc, 1d);
			else if (combinedVal == dLowVal)
				data.set(loc, 2d);
			else
				throw new IllegalStateException("Combined val not any of the inputs??");
		}
		
		// dummy CPT for plotting
		CPT cpt = new CPT(0, 1, Color.WHITE, Color.WHITE);
		
		GMT_Map map = new GMT_Map(region, null, interpSettings.getInterpSpacing(), cpt);
		applyGMTSettings(map, cpt, label);
		
		// now add scatter
		PSXYSymbolSet xySet = new PSXYSymbolSet();
		xySet.setCpt(xyCPT);
		for (Location loc : data.getLocationList()) {
			PSXYSymbol sym = new PSXYSymbol(new Point2D.Double(loc.getLongitude(), loc.getLatitude()),
					Symbol.INVERTED_TRIANGLE, 0.08f, 0f, null, Color.WHITE);
			xySet.addSymbol(sym, data.get(loc));
//			symbols.add(sym);
		}
//		map.setSymbols(symbols);
		map.setSymbolSet(xySet);
		
		return map;
	}
	
	private static CPT buildCPT(double period, boolean psv) throws IOException {
		CPT cpt = CyberShake_GMT_MapGenerator.getHazardCPT();
		
		if (psv)
			cpt = cpt.rescale(Math.log10(2e1), Math.log10(2e3));
		else
			cpt = cpt.rescale(-1, 1);
		
		return cpt;
	}

	public static void main(String[] args) throws IOException, GMT_MapException, DocumentException {
//		// -1 here means RTGM
//		GeoDataSet probData = HardCodedInterpDiffMapCreator.getMainScatter(true, -1, Lists.newArrayList(35), 21);
//		
//		generateMaps(probData, null, null, new File("/tmp/mcer_test"), 3d, false);
//		generateMaps(probData, null, null, new File("/tmp/mcer_test"), 3d, true);
		int datasetID = 57;
		// geom mean 3s
//		int imTypeID = 21;
//		double period = 3d;
//		String outputName = "test1_geom";
		
		// geom mean 5s
//		int imTypeID = 11;
//		double period = 5d;
//		String outputName = "test1_geom";
		
		// geom mean 10s
//		int imTypeID = 1;
//		double period = 10d;
//		String outputName = "test1_geom";
		
		// RotD100
		int imTypeID = 151;
		double period = 2d;
		
//		int imTypeID = 146;
//		double period = 3d;
		
//		int imTypeID = 142;
//		double period = 5d;
		
//		int imTypeID = 136;
//		double period = 10d;
		
		String outputName = "study_15_4_rotd100_old_det";
		
		File outputDir = new File("/home/kevin/CyberShake/MCER/maps/"+outputName+"/"+(int)period+"s");
		Preconditions.checkState(outputDir.exists() || outputDir.mkdirs());
		
		ERF erf = MeanUCERF2_ToDB.createUCERF2ERF();
		List<AttenuationRelationship> gmpes = Lists.newArrayList();
		gmpes.add(AttenRelRef.ASK_2014.instance(null));
		gmpes.add(AttenRelRef.BSSA_2014.instance(null));
		gmpes.add(AttenRelRef.CB_2014.instance(null));
		gmpes.add(AttenRelRef.CY_2014.instance(null));
		for (AttenuationRelationship gmpe : gmpes)
			gmpe.setParamDefaults();
		
		calculateMaps(datasetID, imTypeID, period, erf, gmpes, new WillsMap2006(), outputDir);
		
//		// UCERF3/UCERF2 comparisons
//		twoPercentIn50 = true;
//		
//		String twoP_add = "";
//		if (twoPercentIn50 == true)
//			twoP_add = "_2pin50";
//		File outputDir = new File("/home/kevin/CyberShake/MCER/maps/ucerf3_ucerf2_gmpe_rotd100/"+(int)period+"s"+twoP_add);
//		Preconditions.checkState(outputDir.exists() || outputDir.mkdirs());
//		
//		ERF ucerf2 = MeanUCERF2_ToDB.createUCERF2ERF();
//		ERF ucerf3 = new FaultSystemSolutionERF(FaultSystemIO.loadSol(new File(
//				"/home/kevin/workspace/OpenSHA/dev/scratch/UCERF3/data/scratch/InversionSolutions/"
//				+ "2013_05_10-ucerf3p3-production-10runs_COMPOUND_SOL_FM3_1_MEAN_BRANCH_AVG_SOL.zip")));
//		ucerf3.setParameter(ProbabilityModelParam.NAME, ProbabilityModelOptions.POISSON);
//		ucerf3.getTimeSpan().setDuration(1d);
//		ucerf3.setParameter(IncludeBackgroundParam.NAME, IncludeBackgroundOption.EXCLUDE);
//		ucerf3.updateForecast();
//		
//		List<ERF> erfs = Lists.newArrayList(ucerf3, ucerf2);
//		List<String> names = Lists.newArrayList("UCERF3", "UCERF2");
//		
//		List<AttenuationRelationship> gmpes = Lists.newArrayList();
//		gmpes.add(AttenRelRef.ASK_2014.instance(null));
//		gmpes.add(AttenRelRef.BSSA_2014.instance(null));
//		gmpes.add(AttenRelRef.CB_2014.instance(null));
//		gmpes.add(AttenRelRef.CY_2014.instance(null));
//		for (AttenuationRelationship gmpe : gmpes)
//			gmpe.setParamDefaults();
//		
//		generateGMPEOnlyMaps(erfs, names, gmpes, datasetID, imTypeID, period, new WillsMap2006(), outputDir);
		
		System.exit(0);
	}

}
