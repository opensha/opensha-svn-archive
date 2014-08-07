package org.opensha.sha.cybershake.etas;

import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.dom4j.DocumentException;
import org.opensha.commons.data.function.DiscretizedFunc;
import org.opensha.commons.data.region.CaliforniaRegions;
import org.opensha.commons.data.xyz.AbstractGeoDataSet;
import org.opensha.commons.data.xyz.ArbDiscrGeoDataSet;
import org.opensha.commons.data.xyz.GeoDataSet;
import org.opensha.commons.data.xyz.XYZ_DataSet;
import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.Region;
import org.opensha.commons.mapping.gmt.elements.PSXYSymbol;
import org.opensha.commons.mapping.gmt.elements.TopographicSlopeFile;
import org.opensha.commons.util.FileUtils;
import org.opensha.commons.util.cpt.CPT;
import org.opensha.sha.calc.hazardMap.HazardDataSetLoader;
import org.opensha.sha.cybershake.HazardCurveFetcher;
import org.opensha.sha.cybershake.ModProbConfig;
import org.opensha.sha.cybershake.bombay.ModProbConfigFactory;
import org.opensha.sha.cybershake.bombay.ScenarioBasedModProbConfig;
import org.opensha.sha.cybershake.calc.HazardCurveComputation;
import org.opensha.sha.cybershake.db.CybershakeIM;
import org.opensha.sha.cybershake.db.CybershakeRun;
import org.opensha.sha.cybershake.db.CybershakeSite;
import org.opensha.sha.cybershake.db.Cybershake_OpenSHA_DBApplication;
import org.opensha.sha.cybershake.db.DBAccess;
import org.opensha.sha.cybershake.db.HazardCurve2DB;
import org.opensha.sha.cybershake.db.HazardDataset2DB;
import org.opensha.sha.cybershake.db.Runs2DB;
import org.opensha.sha.cybershake.eew.ZeroProbMod;
import org.opensha.sha.cybershake.etas.ETASModProbConfig.ETAS_CyberShake_Scenarios;
import org.opensha.sha.cybershake.etas.ETASModProbConfig.ETAS_Cybershake_TimeSpans;
import org.opensha.sha.cybershake.maps.CyberShake_GMT_MapGenerator;
import org.opensha.sha.cybershake.maps.GMT_InterpolationSettings;
import org.opensha.sha.cybershake.maps.HardCodedInterpDiffMapCreator;
import org.opensha.sha.cybershake.maps.InterpDiffMap;
import org.opensha.sha.cybershake.maps.ProbGainCalc;
import org.opensha.sha.cybershake.maps.InterpDiffMap.InterpDiffMapType;
import org.opensha.sha.cybershake.maps.servlet.CS_InterpDiffMapServletAccessor;
import org.opensha.sha.gui.infoTools.IMT_Info;

import scratch.UCERF3.FaultSystemSolution;
import scratch.UCERF3.utils.FaultSystemIO;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.primitives.Doubles;

public class ETASCurveCalc {
	
	protected DBAccess db = Cybershake_OpenSHA_DBApplication.db;
	
	private HazardCurveComputation calc;
	private ETASModProbConfig conf;
	private int refDatasetID;
	private int imTypeID;
	private CybershakeIM imType;
	
	private static boolean publish_curves = true;
	
	/**
	 * 
	 * @param conf
	 * @param imTypeID
	 * @param refDatasetID we use this to find site as which we should compute a hazard curve
	 */
	public ETASCurveCalc(ETASModProbConfig conf, int imTypeID, int refDatasetID) {
		this.conf = conf;
		this.refDatasetID = refDatasetID;
		this.imTypeID = imTypeID;
	}
	
	public ArbDiscrGeoDataSet calcMap() throws IOException {
		return calcMap(null);
	}
	
	public ArbDiscrGeoDataSet calcMap(File outputFile) throws IOException {
		HazardCurveFetcher fetch = new HazardCurveFetcher(db, refDatasetID, imTypeID);
//		fetch.get
		imType = new HazardCurve2DB(db).getIMFromID(imTypeID);
		List<CybershakeSite> sites = fetch.getCurveSites();
		List<Integer> curveIDs = fetch.getCurveIDs();
		Preconditions.checkState(sites.size() == curveIDs.size());
		Runs2DB runs2db = new Runs2DB(db);
		
		ArbDiscrGeoDataSet xyz = new ArbDiscrGeoDataSet(true);
//		boolean logPlot = false;
//		boolean isProbAt_IML = false;
//		double val = 0.0004;
////		double val = 0.02;
//		Double customMin = 0d;
//		Double customMax = 1d;
		
		boolean logPlot = true;
		boolean isProbAt_IML = true;
		double val = 0.2;
		Double customMin = -8d;
		Double customMax = -2d;
		
		ArrayList<Double> xVals = Lists.newArrayList();
		for (Point2D pt : IMT_Info.getUSGS_PGA_Function())
			xVals.add(pt.getX());
		xVals.add(3d);
		xVals.add(4d);
		xVals.add(5d);
		
		int erfID = 35;
		int sgtVarID = 6;
		int rupVarScenID = 4;
		int velModelID = 5;
		
		boolean authenticated = false;
		
		HazardDataset2DB dataset2db = new HazardDataset2DB(db);
		
		int probModelID = conf.getScenario().getProbModelID();
		int timeSpanID = conf.getTimeSpanID();
		Date timeSpanStart = conf.getTimeSpanStart();
		double maxFreq = Double.NaN;
		double lowFreqCutoff = Double.NaN;
		
		int publishDatasetID = dataset2db.getDatasetID(erfID, rupVarScenID, sgtVarID, velModelID, probModelID,
				timeSpanID, timeSpanStart, maxFreq, lowFreqCutoff);
		
		if (publish_curves && publishDatasetID < 0) {
			System.out.println("Inserting new Hazard Dataset ID");
			db = Cybershake_OpenSHA_DBApplication.getAuthenticatedDBAccess(true);
			authenticated = true;
			dataset2db = new HazardDataset2DB(db);
			publishDatasetID = dataset2db.addNewDataset(erfID, rupVarScenID, sgtVarID, velModelID, probModelID,
					timeSpanID, timeSpanStart, maxFreq, lowFreqCutoff);
		}
		System.out.println("Dataset ID: "+publishDatasetID);
		HazardCurve2DB curve2db = new HazardCurve2DB(db);
		
		for (int i = 0; i < sites.size(); i++) {
			CybershakeSite site = sites.get(i);
			int refCurveID = curveIDs.get(i);
			if (site.type_id == 4)
				continue; // TEST
			
			System.out.println("Calculating for: "+site.name);
			
			int runID = curve2db.getRunIDForCurve(refCurveID);
			CybershakeRun run = runs2db.getRun(runID);
//			CybershakeRun run = runs2db.getLatestRun(site.id, erfID, sgtVarID, rupVarScenID, velModelID, null, null, null, null);
			DiscretizedFunc func = null;
			if (publishDatasetID >= 0) {
				int curveID = curve2db.getHazardCurveID(run.getRunID(), publishDatasetID, imTypeID);
				System.out.println("Curve: "+curveID);
				if (curveID >= 0)
					func = curve2db.getHazardCurve(curveID);
				else {
					// see if another one has been finished already
					List<CybershakeRun> alternateRuns = runs2db.getRuns(site.id, erfID, sgtVarID, rupVarScenID,
							velModelID, null, null, null, null);
					for (CybershakeRun alternate : alternateRuns) {
						curveID = curve2db.getHazardCurveID(alternate.getRunID(), publishDatasetID, imTypeID);
						System.out.println("Curve: "+curveID);
						if (curveID >= 0) {
							func = curve2db.getHazardCurve(curveID);
							if (func != null) {
								run = alternate;
								runID = run.getRunID();
							}
						}
					}
				}
			}
			if (func == null) {
				// have to calculate
				if (calc == null) {
					calc = new HazardCurveComputation(db);
					calc.setRupVarProbModifier(conf.getRupVarProbModifier());
					calc.setRupProbModifier(conf.getRupProbModifier());
				}
				func = calc.computeHazardCurve(xVals, run, imType);
				if (func == null) {
					System.out.println("skipping, null curve?");
					continue;
				}
				
				if (publish_curves) {
					// post to DB
					if (!authenticated) {
						db = Cybershake_OpenSHA_DBApplication.getAuthenticatedDBAccess(true);
						authenticated = true;
						curve2db = new HazardCurve2DB(db);
					}
					curve2db.insertHazardCurve(run.getRunID(), imTypeID, func, publishDatasetID);
				}
			}
			
			double zVal = HazardDataSetLoader.getCurveVal(func, isProbAt_IML, val);
			Preconditions.checkState(Doubles.isFinite(zVal), "Z not finite: "+zVal);
			if (logPlot && zVal < 1e-16)
				zVal = 1e-16;
			Preconditions.checkState(!logPlot || zVal >= 1e-16);
			xyz.set(new Location(site.lat, site.lon), zVal);
		}
		
		System.out.println("Creating map instance...");
		GMT_InterpolationSettings interpSettings = GMT_InterpolationSettings.getDefaultSettings();
		Region region = new CaliforniaRegions.CYBERSHAKE_MAP_REGION();
		
		InterpDiffMapType[] mapTypes = {InterpDiffMapType.INTERP_MARKS};
		
		CPT cpt = CyberShake_GMT_MapGenerator.getHazardCPT();
		
		AbstractGeoDataSet refScatter = null;
//		if (probGain) {
//			ModProbConfig timeIndepModProb = ModProbConfigFactory.getModProbConfig(1);
//			refScatter = getCustomScatter(timeIndepModProb, imTypeID, isProbAt_IML, val);
//			scatterData = ProbGainCalc.calcProbGain(refScatter, scatterData);
//			mapTypes = gainPlotTypes;
//		}
		
		InterpDiffMap map = new InterpDiffMap(region, null, 0.02, cpt, xyz, interpSettings, mapTypes);
		map.setCustomLabel(conf.getScenario().toString());
		map.setTopoResolution(TopographicSlopeFile.CA_THREE);
		map.setLogPlot(logPlot);
		map.setDpi(300);
		map.setXyzFileName("base_map.xyz");
		map.setCustomScaleMin(customMin);
		map.setCustomScaleMax(customMax);
		
//		Location hypo = null;
//		if (config != null && config instanceof ScenarioBasedModProbConfig) {
//			hypo = ((ScenarioBasedModProbConfig)config).getHypocenter();
//		}
//		PSXYSymbol symbol = getHypoSymbol(region, hypo);
//		if (symbol != null) {
//			map.addSymbol(symbol);
//		}
		
		String metadata = "isProbAt_IML: " + isProbAt_IML + "\n" +
						"val: " + val + "\n" +
						"imTypeID: " + imTypeID + "\n";
		
		System.out.println("Making map...");
		try {
			String address = CS_InterpDiffMapServletAccessor.makeMap(null, map, metadata);
			System.out.println(address);
			if (outputFile != null)
				FileUtils.downloadURL(address+"interpolated_marks.150.png", outputFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return xyz;
	}
	
	public static void createRatioMap(GeoDataSet numerator, GeoDataSet denominator, String label) throws IOException {
		createRatioMap(numerator, denominator, label, null);
	}
	
	public static void createRatioMap(GeoDataSet numerator, GeoDataSet denominator, String label, File outputFile)
			throws IOException {
		System.out.println("Creating map instance...");
		GMT_InterpolationSettings interpSettings = GMT_InterpolationSettings.getDefaultSettings();
		Region region = new CaliforniaRegions.CYBERSHAKE_MAP_REGION();
		
		InterpDiffMapType[] mapTypes = {InterpDiffMapType.INTERP_MARKS};
		
		CPT cpt = CyberShake_GMT_MapGenerator.getHazardCPT();
		GeoDataSet xyz = ProbGainCalc.calcProbGain(denominator, numerator);
		
		InterpDiffMap map = new InterpDiffMap(region, null, 0.02, cpt, xyz, interpSettings, mapTypes);
		map.setCustomLabel(label);
		map.setTopoResolution(TopographicSlopeFile.CA_THREE);
		map.setLogPlot(true);
		map.setDpi(300);
		map.setXyzFileName("base_map.xyz");
		map.setCustomScaleMin(0d);
		map.setCustomScaleMax(2d);
		
//		Location hypo = null;
//		if (config != null && config instanceof ScenarioBasedModProbConfig) {
//			hypo = ((ScenarioBasedModProbConfig)config).getHypocenter();
//		}
//		PSXYSymbol symbol = getHypoSymbol(region, hypo);
//		if (symbol != null) {
//			map.addSymbol(symbol);
//		}
		
		String metadata = "Probability Gain Map\n";
		
		System.out.println("Making map...");
		try {
			String address = CS_InterpDiffMapServletAccessor.makeMap(null, map, metadata);
			System.out.println(address);
			if (outputFile != null)
				FileUtils.downloadURL(address+"interpolated_marks.150.png", outputFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws IOException, DocumentException {
		try {
			int refDatasetID = 12; // for rv scen id = 4
			
			File outputDir = new File("/home/kevin/OpenSHA/UCERF3/cybershake_etas/maps");
			
			ETASModProbConfig conf;
			ETASCurveCalc calc;
			
			FaultSystemSolution sol = FaultSystemIO.loadSol(new File("/home/kevin/OpenSHA/UCERF3/cybershake_etas/ucerf2_mapped_sol.zip"));
			
//			conf = new ETASModProbConfig(ETAS_CyberShake_Scenarios.PARKFIELD, ETAS_Cybershake_TimeSpans.ONE_WEEK, sol,
//					new File("/home/kevin/OpenSHA/UCERF3/cybershake_etas/sims/2014_08_01-parkfield/results.zip"),
//					new File("/home/kevin/OpenSHA/UCERF3/cybershake_etas/mappings.csv"));
//			calc = new ETASCurveCalc(conf, 21, refDatasetID);
//			ArbDiscrGeoDataSet modMap = calc.calcMap(new File(outputDir, "parkfield_hazard.png"));
//			conf = new ETASModProbConfig(ETAS_CyberShake_Scenarios.BOMBAY_M6, ETAS_Cybershake_TimeSpans.ONE_WEEK, sol,
//					new File("/home/kevin/OpenSHA/UCERF3/cybershake_etas/sims/2014_07_31-bombay_beach_m6/results.zip"),
//					new File("/home/kevin/OpenSHA/UCERF3/cybershake_etas/mappings.csv"));
//			calc = new ETASCurveCalc(conf, 21, refDatasetID);
//			ArbDiscrGeoDataSet modMap = calc.calcMap(new File(outputDir, "bombay_hazard.png"));
//			conf = new ETASModProbConfig(ETAS_CyberShake_Scenarios.TEST_NEGLIGABLE, ETAS_Cybershake_TimeSpans.ONE_WEEK, sol,
//					new File("/home/kevin/OpenSHA/UCERF3/cybershake_etas/sims/2014_08_01-parkfield/results.zip"),
//					new File("/home/kevin/OpenSHA/UCERF3/cybershake_etas/mappings.csv"));
//			publish_curves = false;
//			calc = new ETASCurveCalc(conf, 21, refDatasetID);
//			ArbDiscrGeoDataSet modMap = calc.calcMap(new File(outputDir, "test_negligable_hazard.png"));
			conf = new ETASModProbConfig(ETAS_CyberShake_Scenarios.TEST_BOMBAY_M6_SUBSET, ETAS_Cybershake_TimeSpans.ONE_WEEK, sol,
					new File("/home/kevin/OpenSHA/UCERF3/cybershake_etas/sims/2014_07_31-bombay_beach_m6/results.zip"),
					new File("/home/kevin/OpenSHA/UCERF3/cybershake_etas/mappings.csv"));
			publish_curves = false;
			calc = new ETASCurveCalc(conf, 21, refDatasetID);
			ArbDiscrGeoDataSet modMap = calc.calcMap(new File(outputDir, "test_bombay_subset_hazard.png"));
			
			
			conf = new ETASModProbConfig(ETAS_CyberShake_Scenarios.MAPPED_UCERF2, ETAS_Cybershake_TimeSpans.ONE_WEEK, sol,
					null,
					new File("/home/kevin/OpenSHA/UCERF3/cybershake_etas/mappings.csv"));
			calc = new ETASCurveCalc(conf, 21, refDatasetID);
			ArbDiscrGeoDataSet baseMap = calc.calcMap(new File(outputDir, "ref_hazard.png"));
			
//			createRatioMap(modMap, baseMap, "Bombay Beach Prob Gain", new File(outputDir, "bombay_gain.png"));
//			createRatioMap(modMap, baseMap, "Parkfield Prob Gain", new File(outputDir, "parkfield_gain.png"));
//			createRatioMap(modMap, baseMap, "Test Negligable Prob Gain", new File(outputDir, "test_negligable_gain.png"));
			createRatioMap(modMap, baseMap, "Test Bombay 50% Subset Prob Gain", new File(outputDir, "test_bombay_subset_gain.png"));
			
			System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

}
