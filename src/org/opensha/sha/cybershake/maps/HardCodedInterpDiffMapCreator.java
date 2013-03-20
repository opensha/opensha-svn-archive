package org.opensha.sha.cybershake.maps;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.commons.data.function.DiscretizedFunc;
import org.opensha.commons.data.region.CaliforniaRegions;
import org.opensha.commons.data.xyz.AbstractGeoDataSet;
import org.opensha.commons.data.xyz.ArbDiscrGeoDataSet;
import org.opensha.commons.data.xyz.GeoDataSet;
import org.opensha.commons.exceptions.GMT_MapException;
import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.Region;
import org.opensha.commons.mapping.gmt.elements.GMT_CPT_Files;
import org.opensha.commons.mapping.gmt.elements.PSXYSymbol;
import org.opensha.commons.mapping.gmt.elements.TopographicSlopeFile;
import org.opensha.commons.mapping.gmt.elements.PSXYSymbol.Symbol;
import org.opensha.commons.util.cpt.CPT;
import org.opensha.sha.calc.hazardMap.HazardDataSetLoader;
import org.opensha.sha.cybershake.HazardCurveFetcher;
import org.opensha.sha.cybershake.bombay.BombayBeachHazardCurveCalc;
import org.opensha.sha.cybershake.bombay.ModProbConfig;
import org.opensha.sha.cybershake.bombay.ModProbConfigFactory;
import org.opensha.sha.cybershake.bombay.ScenarioBasedModProbConfig;
import org.opensha.sha.cybershake.db.AttenRelCurves2DB;
import org.opensha.sha.cybershake.db.AttenRelDataSets2DB;
import org.opensha.sha.cybershake.db.AttenRels2DB;
import org.opensha.sha.cybershake.db.CybershakeHazardCurveRecord;
import org.opensha.sha.cybershake.db.CybershakeRun;
import org.opensha.sha.cybershake.db.CybershakeSite;
import org.opensha.sha.cybershake.db.CybershakeSiteInfo2DB;
import org.opensha.sha.cybershake.db.Cybershake_OpenSHA_DBApplication;
import org.opensha.sha.cybershake.db.DBAccess;
import org.opensha.sha.cybershake.db.HazardCurve2DB;
import org.opensha.sha.cybershake.db.Runs2DB;
import org.opensha.sha.cybershake.maps.InterpDiffMap.InterpDiffMapType;
import org.opensha.sha.cybershake.maps.servlet.CS_InterpDiffMapServletAccessor;
import org.opensha.sha.imr.AttenRelRef;
import org.opensha.sha.imr.ScalarIMR;
import org.opensha.sha.imr.param.OtherParams.SigmaTruncLevelParam;
import org.opensha.sha.imr.param.OtherParams.SigmaTruncTypeParam;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

public class HardCodedInterpDiffMapCreator {
	
	private static ArbDiscrGeoDataSet getMainScatter(boolean isProbAt_IML, double val, int datasetID, int imTypeID) {
		List<Integer> datasetIDs = Lists.newArrayList(datasetID);
		return getMainScatter(isProbAt_IML, val, datasetIDs, imTypeID);
	}
	
	private static ArbDiscrGeoDataSet getMainScatter(boolean isProbAt_IML, double val, List<Integer> datasetIDs, int imTypeID) {
		Preconditions.checkArgument(!datasetIDs.isEmpty(), "Must supply at least one dataset ID");
		DBAccess db = Cybershake_OpenSHA_DBApplication.db;
		ArbDiscrGeoDataSet scatterData = new ArbDiscrGeoDataSet(true);
		for (int datasetID : datasetIDs) {
			HazardCurveFetcher fetcher = new HazardCurveFetcher(db, datasetID, imTypeID);
			ArrayList<CybershakeSite> sites = fetcher.getCurveSites();
			ArrayList<Double> vals = fetcher.getSiteValues(isProbAt_IML, val);
			
			for (int i=0; i<sites.size(); i++) {
				CybershakeSite site = sites.get(i);
				if (site.type_id == CybershakeSite.TYPE_TEST_SITE)
					continue;
				Location loc = site.createLocation();
				if (scatterData.contains(loc))
					continue;
				double siteVal = vals.get(i);
				scatterData.set(loc, siteVal);
			}
		}
		return scatterData;
	}
	
	private static ArbDiscrGeoDataSet getCustomScatter(ModProbConfig config, int imTypeID,
			boolean isProbAt_IML, double val) throws FileNotFoundException, IOException {
		if (imTypeID != 21)
			throw new IllegalArgumentException("IM type must be 21 for custom map");
		if (!isProbAt_IML)
			throw new IllegalArgumentException("isProbAt_IML must be true for custom map");
//		String dir = "/home/kevin/CyberShake/interpDiffInputFiles/"+singleName+"/";
//		String fname;
//		if (mod)
//			fname = "mod_";
//		else
//			fname = "orig_";
//		fname += (float)val+"g_singleDay.txt";
//		String fileName = dir + fname;
//		File file = new File(fileName);
//		if (file.exists()) {
//			System.out.println("Loading scatter from: " + fileName);
//			return ArbDiscrGeographicDataSet.loadXYZFile(fileName);
//		} else {
//			return loadCustomMapCurves(singleName, isProbAt_IML, val, mod);
//		}
		return loadCustomMapCurves(config, imTypeID, isProbAt_IML, val);
	}
	
	private static ArbDiscrGeoDataSet loadCustomMapCurves(ModProbConfig config, int imTypeID,
			boolean isProbAt_IML, double val) {
		DBAccess db = Cybershake_OpenSHA_DBApplication.db;
		
		int datasetID = config.getHazardDatasetID(35, 3, 5, 1, null);
		if (datasetID < 0)
			throw new RuntimeException("Couldn't get HC dataset id!");
		
		HazardCurveFetcher fetcher = new HazardCurveFetcher(db, datasetID, imTypeID);
		
		ArrayList<DiscretizedFunc> curves = fetcher.getFuncs();
		ArrayList<CybershakeSite> sites = fetcher.getCurveSites();
		
		ArbDiscrGeoDataSet xyz = new ArbDiscrGeoDataSet(true);
		
		for (int i=0; i<curves.size(); i++) {
			DiscretizedFunc curve = curves.get(i);
			CybershakeSite site = sites.get(i);
			
//			System.out.println("loaded curve with "+curve.getNum()+" vals");
			
			double zVal = HazardDataSetLoader.getCurveVal(curve, isProbAt_IML, val);
			xyz.set(new Location(site.lat, site.lon), zVal);
		}
		
		return xyz;
	}
	
	private static CybershakeRun getRun(int runID, ArrayList<CybershakeRun> runs) {
		for (CybershakeRun run : runs) {
			if (runID == run.getRunID())
				return run;
		}
		return null;
	}
	
	private static CybershakeSite getSite(int siteID, ArrayList<CybershakeSite> sites) {
		for (CybershakeSite site : sites) {
			if (siteID == site.id)
				return site;
		}
		return null;
	}

	private static ArbDiscrGeoDataSet loadCustomMapCurves(
			String singleName, boolean isProbAt_IML, double val, boolean mod)
			throws FileNotFoundException, IOException {
		String curveDir = "/home/kevin/CyberShake/"+singleName+"/";
		if (mod)
			curveDir += "mod";
		else
			curveDir += "orig";
		curveDir += "Curves";
		File curveDirFile = new File(curveDir);
		if (curveDirFile.exists()) {
			DBAccess db = Cybershake_OpenSHA_DBApplication.db;
			Runs2DB runs2db = new Runs2DB(db);
			ArrayList<CybershakeRun> runs = runs2db.getRuns();
			CybershakeSiteInfo2DB sites2db = new CybershakeSiteInfo2DB(db);
			ArrayList<CybershakeSite> sites = sites2db.getAllSitesFromDB();
			ArbDiscrGeoDataSet xyz = new ArbDiscrGeoDataSet(true);
			
			for (File curveFile : curveDirFile.listFiles()) {
				if (curveFile.isFile() && curveFile.getName().endsWith(".txt")
						&& curveFile.getName().startsWith("run_")) {
					ArbitrarilyDiscretizedFunc func =
						ArbitrarilyDiscretizedFunc.loadFuncFromSimpleFile(curveFile.getAbsolutePath());
//						System.out.println("Loaded func with "+func.getNum()+" pts from "+curveFile.getName());
					String[] split = curveFile.getName().split("_");
					int runID = Integer.parseInt(split[1]);
					CybershakeRun run = getRun(runID, runs);
					CybershakeSite site = getSite(run.getSiteID(), sites);
					if (site == null)
						throw new RuntimeException("run '"+runID+"' not found!");
					double zVal = HazardDataSetLoader.getCurveVal(func, isProbAt_IML, val);
					xyz.set(new Location(site.lat, site.lon), zVal);
				}
			}
			return xyz;
		} else {
			throw new FileNotFoundException("Couldn't locate file or curve dir for dataset '"+singleName+"'");
		}
	}
	
	private static GeoDataSet loadBaseMap(
			ScalarIMR imr,
			boolean isProbAt_IML,
			double level,
			int velModelID,
			int imTypeID) throws SQLException {
		
		DBAccess db = Cybershake_OpenSHA_DBApplication.db;
		
		AttenRels2DB ar2db = new AttenRels2DB(db);
		int attenRelID = ar2db.getAttenRelID(imr);
		
		AttenRelDataSets2DB ds2db = new AttenRelDataSets2DB(db);
		int datasetID = ds2db.getDataSetID(attenRelID, 35, velModelID, 1, 1, null);
		
		File cacheFile = new File(getCacheDir(), "ar_curves_"+attenRelID+"_"+datasetID+"_"
				+isProbAt_IML+"_"+(float)level+"_"+imTypeID+".txt");
		if (cacheFile.exists()) {
			try {
				return ArbDiscrGeoDataSet.loadXYZFile(cacheFile.getAbsolutePath(), true);
			} catch (Exception e) {
				// don't fail on cache problem
				e.printStackTrace();
			}
		}
		
		AttenRelCurves2DB curves2db = new AttenRelCurves2DB(db);
		GeoDataSet xyz = curves2db.fetchMap(datasetID, imTypeID, isProbAt_IML, level, true);
		System.out.println("Got "+xyz.size()+" basemap values!");
		
		try {
			ArbDiscrGeoDataSet.writeXYZFile(xyz, cacheFile.getAbsolutePath());
		} catch (IOException e) {
			// don't fail on cache problem
			e.printStackTrace();
		}
		
		return xyz;
	}
	
	private static File getCacheDir() {
		if (System.getProperties().containsKey("CyberShakeCache")) {
			return new File(System.getProperties().getProperty("CyberShakeCache"));
		}
		return new File("/home/kevin/CyberShake/cache");
	}
	
	private static AbstractGeoDataSet loadBaseMap(boolean singleDay, boolean isProbAt_IML,
			double val, int imTypeID, String name) throws FileNotFoundException, IOException {
		int period;
		if (imTypeID == 11)
			period = 5;
		else if (imTypeID == 21)
			period = 3;
		else if (imTypeID == 26)
			period = 2;
		else
			throw new IllegalArgumentException("Unknown IM type id: " + imTypeID);
		String dir = "/home/kevin/CyberShake/baseMaps/"+name+"/";
		String fname = name+"_base_map_"+period+"sec_";
		if (isProbAt_IML) {
			fname += (float)val+"g";
		} else {
			if (val == 0.0004)
				fname += "2percent";
			else if (val == 0.002)
				fname += "10precent";
			else
				throw new IllegalArgumentException("Unown probability val: " + val);
		}
		if (singleDay)
			fname += "_singleDay";
		fname += "_hiRes.txt";
		String fileName = dir + fname;
		System.out.println("Loading basemap from: " + fileName);
		return ArbDiscrGeoDataSet.loadXYZFile(fileName, true);
	}
	
	private static PSXYSymbol getHypoSymbol(Region region, Location hypo) {
		if (hypo == null)
			return null;
		Location northWest = new Location(region.getMaxLat(), region.getMinLon());
		Location southEast = new Location(region.getMinLat(), region.getMaxLon());
		Region squareReg = new Region(northWest, southEast);
		if (!squareReg.contains(hypo)) {
			System.out.println("Hypocenter: "+hypo+"\nisn't within region: "+region);
			return null;
		}
		Point2D pt = new Point2D.Double(hypo.getLongitude(), hypo.getLatitude());
		double width = 0.4;
		double penWidth = 5;
		Color penColor = Color.WHITE;
		Color fillColor = Color.RED;
		return new PSXYSymbol(pt, Symbol.STAR, width, penWidth, penColor, fillColor);
	}
	
	protected static void setTruncation(ScalarIMR imr, double trunc) {
		imr.getParameter(SigmaTruncLevelParam.NAME).setValue(trunc);
		if (trunc < 0)
			imr.getParameter(SigmaTruncTypeParam.NAME).setValue(SigmaTruncTypeParam.SIGMA_TRUNC_TYPE_NONE);
		else
			imr.getParameter(SigmaTruncTypeParam.NAME).setValue(SigmaTruncTypeParam.SIGMA_TRUNC_TYPE_1SIDED);
	}

	/**
	 * @param args
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 * @throws ClassNotFoundException 
	 */
	public static void main(String[] args){
		try {
			boolean logPlot = false;
			int imTypeID = 21;
			int velModelID = 1;
			List<Integer> datasetIDs = Lists.newArrayList(12, 21);
			List<Integer> compDatasetIDs = Lists.newArrayList(1);
			Double customMin = 0d;
			Double customMax = 1.4;
			
			
//			boolean isProbAt_IML = true;
//			double val = 0.2;
//			String baseMapName = "cb2008";
//			ModProbConfig config = null;
////			ModProbConfig config = ModProbConfigFactory.getScenarioConfig(BombayBeachHazardCurveCalc.PARKFIELD_LOC);
//			boolean probGain = false;
//			String customLabel;
//			if (probGain)
//				customLabel = "Probability Gain";
//			else
//				customLabel = "POE "+(float)val+"G 3sec SA in 1 day";
//			if (logPlot && !probGain) {
//				customMin = -8.259081006598409;
////				customMax = -3.25;
//				customMax = -2.5;
//			}
			
			ModProbConfig config = null;
			boolean isProbAt_IML = false;
			double val = 0.0004;
			ScalarIMR baseMapIMR = AttenRelRef.NGA_2008_4AVG.instance(null);
			baseMapIMR.setParamDefaults();
			setTruncation(baseMapIMR, 3.0);
			String customLabel = "3sec SA, 2% in 50 yrs";
			boolean probGain = false;
			
			
			String addr = getMap(logPlot, velModelID, datasetIDs, imTypeID, customMin, customMax,
					isProbAt_IML, val, baseMapIMR, config, probGain,
					customLabel);
			
			System.out.println("Map address: " + addr);
			
			if (compDatasetIDs != null && !compDatasetIDs.isEmpty()) {
				addr = getCompareMap(logPlot, datasetIDs, compDatasetIDs, imTypeID, isProbAt_IML, val, customLabel);
				
				System.out.println("Comp map address: " + addr);
			}
			
			System.exit(0);
		} catch (Throwable t) {
			// TODO Auto-generated catch block
			t.printStackTrace();
			System.exit(1);
		}
	}
	
//	protected static InterpDiffMapType[] normPlotTypes = null;
	protected static InterpDiffMapType[] normPlotTypes = { InterpDiffMapType.INTERP_NOMARKS,
			InterpDiffMapType.INTERP_MARKS, InterpDiffMapType.BASEMAP, InterpDiffMapType.DIFF, InterpDiffMapType.RATIO};
	protected static InterpDiffMapType[] gainPlotTypes = 
			{ InterpDiffMapType.INTERP_NOMARKS, InterpDiffMapType.INTERP_MARKS};
	
	protected static String getMap(boolean logPlot, int velModelID, int datasetID, int imTypeID,
			Double customMin, Double customMax, boolean isProbAt_IML,
			double val, ScalarIMR baseMapIMR, ModProbConfig config,
			boolean probGain, String customLabel) throws FileNotFoundException,
			IOException, ClassNotFoundException, GMT_MapException, SQLException {
		List<Integer> datasetIDs = Lists.newArrayList(datasetID);
		return getMap(logPlot, velModelID, datasetIDs, imTypeID, customMin, customMax, isProbAt_IML, val,
				baseMapIMR, config, probGain, customLabel);
	}
	
	protected static String getMap(boolean logPlot, int velModelID, List<Integer> datasetIDs, int imTypeID,
			Double customMin, Double customMax, boolean isProbAt_IML,
			double val, ScalarIMR baseMapIMR, ModProbConfig config,
			boolean probGain, String customLabel) throws FileNotFoundException,
			IOException, ClassNotFoundException, GMT_MapException, SQLException {
		boolean singleDay = config != null;
		double baseMapRes = 0.005;
		System.out.println("Loading basemap...");
		GeoDataSet baseMap;
		if (!probGain) {
			baseMap = loadBaseMap(baseMapIMR, isProbAt_IML, val, velModelID, imTypeID);
//			baseMap = loadBaseMap(singleDay, isProbAt_IML, val, imTypeID, baseMapName);
			System.out.println("Basemap has " + baseMap.size() + " points");
		} else {
			baseMap = null;
		}
		
		System.out.println("Fetching curves...");
		AbstractGeoDataSet scatterData;
		if (singleDay)
			scatterData = getCustomScatter(config, imTypeID, isProbAt_IML, val);
		else
			scatterData = getMainScatter(isProbAt_IML, val, datasetIDs, imTypeID);
		
		System.out.println("Creating map instance...");
		GMT_InterpolationSettings interpSettings = GMT_InterpolationSettings.getDefaultSettings();
		Region region = new CaliforniaRegions.CYBERSHAKE_MAP_REGION();
		
		InterpDiffMapType[] mapTypes = normPlotTypes;
		
		CPT cpt = CPT.loadFromStream(HardCodedInterpDiffMapCreator.class.getResourceAsStream(
				"/resources/cpt/MaxSpectrum2.cpt"));
		
		AbstractGeoDataSet refScatter = null;
		if (probGain) {
			ModProbConfig timeIndepModProb = ModProbConfigFactory.getModProbConfig(1);
			refScatter = getCustomScatter(timeIndepModProb, imTypeID, isProbAt_IML, val);
			scatterData = ProbGainCalc.calcProbGain(refScatter, scatterData);
			mapTypes = gainPlotTypes;;
		}
		
		InterpDiffMap map = new InterpDiffMap(region, baseMap, baseMapRes, cpt, scatterData, interpSettings, mapTypes);
		map.setCustomLabel(customLabel);
		map.setTopoResolution(TopographicSlopeFile.CA_THREE);
		map.setLogPlot(logPlot);
		map.setDpi(300);
		map.setXyzFileName("base_map.xyz");
		map.setCustomScaleMin(customMin);
		map.setCustomScaleMax(customMax);
		
		Location hypo = null;
		if (config != null && config instanceof ScenarioBasedModProbConfig) {
			hypo = ((ScenarioBasedModProbConfig)config).getHypocenter();
		}
		PSXYSymbol symbol = getHypoSymbol(region, hypo);
		if (symbol != null) {
			map.addSymbol(symbol);
		}
		
		String metadata = "isProbAt_IML: " + isProbAt_IML + "\n" +
						"val: " + val + "\n" +
						"singleDay: " + singleDay + "\n" +
						"imTypeID: " + imTypeID + "\n";
		
		System.out.println("Making map...");
		return CS_InterpDiffMapServletAccessor.makeMap(null, map, metadata);
	}
	
	protected static String getCompareMap(boolean logPlot, List<Integer> dataset1IDs, List<Integer> dataset2IDs, int imTypeID,
			boolean isProbAt_IML, double val, String customLabel) throws FileNotFoundException,
			IOException, ClassNotFoundException, GMT_MapException, SQLException {
		System.out.println("Fetching curves...");
		AbstractGeoDataSet scatterData1 = getMainScatter(isProbAt_IML, val, dataset1IDs, imTypeID);
		AbstractGeoDataSet scatterData2 = getMainScatter(isProbAt_IML, val, dataset2IDs, imTypeID);
		
		System.out.println("Creating map instance...");
		GMT_InterpolationSettings interpSettings = GMT_InterpolationSettings.getDefaultSettings();
		Region region = new CaliforniaRegions.CYBERSHAKE_MAP_REGION();
		
		InterpDiffMapType[] mapTypes = gainPlotTypes;
		
		CPT diffCPT = CyberShake_GMT_MapGenerator.getDiffCPT();
		CPT ratioCPT = CyberShake_GMT_MapGenerator.getRatioCPT();
//		CPT diffCPT = polar.rescale(-0.4, 0.4);
//		CPT ratioCPT = polar.rescale(0.5, 1.5);
		
		AbstractGeoDataSet diffData = ProbGainCalc.calcProbDiff(scatterData2, scatterData1);
		AbstractGeoDataSet ratioData = ProbGainCalc.calcProbGain(scatterData2, scatterData1);
		
		InterpDiffMap map = new InterpDiffMap(region, null, 0.005, diffCPT, diffData, interpSettings, mapTypes);
		map.setCustomLabel("Difference, "+customLabel);
//		map.setTopoResolution(null);
		map.setTopoResolution(TopographicSlopeFile.CA_THREE);
		map.setLogPlot(logPlot);
		map.setDpi(300);
		map.setXyzFileName("diff_map.xyz");
		map.setCustomScaleMin((double)diffCPT.getMinValue());
		map.setCustomScaleMax((double)diffCPT.getMaxValue());
		
		String metadata = "isProbAt_IML: " + isProbAt_IML + "\n" +
						"val: " + val + "\n" +
						"imTypeID: " + imTypeID + "\n";
		
		System.out.println("Making map...");
		String diffAddr = CS_InterpDiffMapServletAccessor.makeMap(null, map, metadata);
		
		map = new InterpDiffMap(region, null, 0.005, ratioCPT, ratioData, interpSettings, mapTypes);
		map.setCustomLabel("Ratio, "+customLabel);
//		map.setTopoResolution(null);
		map.setTopoResolution(TopographicSlopeFile.CA_THREE);
		map.setLogPlot(logPlot);
		map.setDpi(300);
		map.setXyzFileName("ratio_map.xyz");
		map.setCustomScaleMin((double)ratioCPT.getMinValue());
		map.setCustomScaleMax((double)ratioCPT.getMaxValue());
		
		System.out.println("Making map...");
		String ratioAddr = CS_InterpDiffMapServletAccessor.makeMap(null, map, metadata);
		return diffAddr+" "+ratioAddr;
	}

}
