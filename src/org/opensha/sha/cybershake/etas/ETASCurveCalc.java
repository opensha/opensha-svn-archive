package org.opensha.sha.cybershake.etas;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.dom4j.DocumentException;
import org.opensha.commons.data.CSVFile;
import org.opensha.commons.data.TimeSpan;
import org.opensha.commons.data.function.DiscretizedFunc;
import org.opensha.commons.data.region.CaliforniaRegions;
import org.opensha.commons.data.xyz.AbstractGeoDataSet;
import org.opensha.commons.data.xyz.ArbDiscrGeoDataSet;
import org.opensha.commons.data.xyz.GeoDataSet;
import org.opensha.commons.data.xyz.XYZ_DataSet;
import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.Region;
import org.opensha.commons.gui.plot.PlotCurveCharacterstics;
import org.opensha.commons.gui.plot.PlotLineType;
import org.opensha.commons.gui.plot.PlotSpec;
import org.opensha.commons.gui.plot.PlotSymbol;
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
import org.opensha.sha.cybershake.calc.RuptureProbabilityModifier;
import org.opensha.sha.cybershake.calc.ShakeMapComputation;
import org.opensha.sha.cybershake.db.CybershakeIM;
import org.opensha.sha.cybershake.db.CybershakeRun;
import org.opensha.sha.cybershake.db.CybershakeSite;
import org.opensha.sha.cybershake.db.Cybershake_OpenSHA_DBApplication;
import org.opensha.sha.cybershake.db.DBAccess;
import org.opensha.sha.cybershake.db.HazardCurve2DB;
import org.opensha.sha.cybershake.db.HazardDataset2DB;
import org.opensha.sha.cybershake.db.MeanUCERF2_ToDB;
import org.opensha.sha.cybershake.db.Runs2DB;
import org.opensha.sha.cybershake.eew.ZeroProbMod;
import org.opensha.sha.cybershake.etas.ETASModProbConfig.ETAS_CyberShake_Scenarios;
import org.opensha.sha.cybershake.etas.ETASModProbConfig.ETAS_Cybershake_TimeSpans;
import org.opensha.sha.cybershake.etas.ETASModProbConfig.RVProbSortable;
import org.opensha.sha.cybershake.maps.CyberShake_GMT_MapGenerator;
import org.opensha.sha.cybershake.maps.GMT_InterpolationSettings;
import org.opensha.sha.cybershake.maps.HardCodedInterpDiffMapCreator;
import org.opensha.sha.cybershake.maps.InterpDiffMap;
import org.opensha.sha.cybershake.maps.ProbGainCalc;
import org.opensha.sha.cybershake.maps.InterpDiffMap.InterpDiffMapType;
import org.opensha.sha.cybershake.maps.servlet.CS_InterpDiffMapServletAccessor;
import org.opensha.sha.earthquake.AbstractERF;
import org.opensha.sha.gui.infoTools.HeadlessGraphPanel;
import org.opensha.sha.gui.infoTools.IMT_Info;

import scratch.UCERF3.FaultSystemSolution;
import scratch.UCERF3.utils.FaultSystemIO;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.google.common.primitives.Doubles;

public class ETASCurveCalc {
	
	protected DBAccess db = Cybershake_OpenSHA_DBApplication.db;
	
	private HazardCurveComputation calc;
	private ETASModProbConfig conf;
	private int refDatasetID;
	private int imTypeID;
	private CybershakeIM imType;
	
	private Map<CybershakeSite, DiscretizedFunc> prevCurves;
	
	private static boolean publish_curves = true;
	
	private static final double log_plot_min = 1e-8;
	
	private ArrayList<Double> xVals;
	
	private List<CybershakeSite> sites;
	private List<Integer> refCurveIDs;
	
	private HazardCurve2DB curve2db;
	private Runs2DB runs2db;
	
	private int erfID = 35;
	private int sgtVarID = 6;
	private int rupVarScenID = 4;
	private int velModelID = 5;
	
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
		
		xVals = Lists.newArrayList();
		for (Point2D pt : IMT_Info.getUSGS_PGA_Function())
			xVals.add(pt.getX());
		xVals.add(3d);
		xVals.add(4d);
		xVals.add(5d);
	}
	
	private void loadCurveSites() {
		HazardCurveFetcher fetch = new HazardCurveFetcher(db, refDatasetID, imTypeID);
		sites = fetch.getCurveSites();
		refCurveIDs = fetch.getCurveIDs();
		curve2db = new HazardCurve2DB(db);
		runs2db = new Runs2DB(db);
		imType = curve2db.getIMFromID(imTypeID);
	}
	
	public ArbDiscrGeoDataSet calcMap() throws IOException {
		return calcMap(null);
	}
	
	public ArbDiscrGeoDataSet calcMap(File outputFile) throws IOException {
		loadCurveSites();
		
		Preconditions.checkState(sites.size() == refCurveIDs.size());
		
		prevCurves = Maps.newHashMap();
		
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
//		double val = 0.1;
//		Double customMin = -7d;
//		Double customMax = -1d;
		
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
			int refCurveID = refCurveIDs.get(i);
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
			if (logPlot && zVal < log_plot_min)
				zVal = log_plot_min;
			Preconditions.checkState(!logPlot || zVal >= log_plot_min);
			xyz.set(new Location(site.lat, site.lon), zVal);
			prevCurves.put(site, func);
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
		
		InterpDiffMap map = new InterpDiffMap(region, null, 0.02, cpt, xyz.copy(), interpSettings, mapTypes);
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
	
	/**
	 * Useful for debugging
	 * @param siteShortName
	 */
	private void manualCalc(String siteShortName) {
		System.out.println("Manually calculating "+siteShortName+" ("+conf.getScenario().name()+")");
		if (prevCurves == null)
			prevCurves = Maps.newHashMap();
		
		if (calc == null) {
			calc = new HazardCurveComputation(db);
			calc.setRupVarProbModifier(conf.getRupVarProbModifier());
			calc.setRupProbModifier(conf.getRupProbModifier());
		}
		
		if (sites == null)
			loadCurveSites();
		
		// find matching site
		CybershakeSite site = null;
		int refCurveID = -1;
		for (int i=0; i<sites.size(); i++) {
			if (sites.get(i).short_name.equals(siteShortName)) {
				site = sites.get(i);
				refCurveID = refCurveIDs.get(i);
				break;
			}
		}
		Preconditions.checkNotNull(site, "Site not found: "+siteShortName);
		int runID = curve2db.getRunIDForCurve(refCurveID);
		CybershakeRun run = runs2db.getRun(runID);
		DiscretizedFunc func = calc.computeHazardCurve(xVals, run, imType);
		prevCurves.put(site, func);
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
		
		double minX = 0d;
		double maxX = 3d;
		
		CPT cpt = CyberShake_GMT_MapGenerator.getHazardCPT();
		cpt = cpt.rescale(minX, maxX);
		GeoDataSet xyz = ProbGainCalc.calcProbGain(denominator, numerator);
		
		InterpDiffMap map = new InterpDiffMap(region, null, 0.02, cpt, xyz, interpSettings, mapTypes);
		map.setCustomLabel(label);
		map.setTopoResolution(TopographicSlopeFile.CA_THREE);
		map.setLogPlot(true);
		map.setDpi(300);
		map.setXyzFileName("base_map.xyz");
		map.setCustomScaleMin(minX);
		map.setCustomScaleMax(maxX);
		
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
	
	private static void plotComparisonCurves(List<ETASCurveCalc> calcs, List<String> labels,
			List<Color> colors, String siteShortName, File outputDir) throws IOException {
		// input validation
		Preconditions.checkArgument(!calcs.isEmpty(), "Must supply at least one calc");
		Preconditions.checkState(calcs.size() == labels.size() && calcs.size() == colors.size(), "List sizes incorrect");
		for (ETASCurveCalc calc : calcs)
			Preconditions.checkNotNull(calc.prevCurves, "No curves calculated!");
		// now find actual cybershake site
		CybershakeSite site = null;
		siteShortName = siteShortName.trim().toLowerCase();
		for (CybershakeSite testSite : calcs.get(0).prevCurves.keySet()) {
			if (testSite.short_name.trim().toLowerCase().equals(siteShortName)) {
				site = testSite;
				break;
			}
		}
		Preconditions.checkNotNull(site, "Site not found: "+siteShortName);
		siteShortName = site.short_name;
		// make sure it's present in each of the others
		
		// now get the curves
		List<DiscretizedFunc> curves = Lists.newArrayList();
		List<PlotCurveCharacterstics> chars = Lists.newArrayList();
		for (int i = 0; i < calcs.size(); i++) {
			ETASCurveCalc calc = calcs.get(i);
			DiscretizedFunc curve = calc.prevCurves.get(site);
			Preconditions.checkNotNull(curve, "No curve for "+labels.get(i));
			curve.setName(labels.get(i));
			curves.add(curve);
			chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, PlotSymbol.FILLED_CIRCLE, 4f, colors.get(i)));
		}
		
		ETAS_Cybershake_TimeSpans timeSpan = calcs.get(0).conf.getTimeSpan();
		for (int i=1; i<calcs.size(); i++)
			Preconditions.checkState(calcs.get(i).conf.getTimeSpan() == timeSpan, "inconsistent time spans");
		
		PlotSpec spec = new PlotSpec(curves, chars, "CyberShake OEF Curves: "+site.short_name, "3s SA (g)",
				timeSpan.toString()+" POE");
		
		HeadlessGraphPanel gp = new HeadlessGraphPanel();
		gp.setTickLabelFontSize(18);
		gp.setAxisLabelFontSize(20);
		gp.setPlotLabelFontSize(21);
		gp.setBackgroundColor(Color.WHITE);
		
		gp.setXLog(true);
		gp.setYLog(true);
		gp.setUserBounds(1e-2, 1e0, 1e-7, 1e-1);
		gp.drawGraphPanel(spec);
		gp.getCartPanel().setSize(1000, 800);
		gp.saveAsPDF(new File(outputDir, "curve_"+siteShortName+".pdf").getAbsolutePath());
		gp.saveAsPNG(new File(outputDir, "curve_"+siteShortName+".png").getAbsolutePath());
		gp.saveAsTXT(new File(outputDir, "curve_"+siteShortName+".txt").getAbsolutePath());
	}
	
	private static void writeU2MappedCompare(ETASModProbConfig indep, ETASModProbConfig dep) throws IOException {
		CSVFile<String> csv = new CSVFile<String>(true);
		
		csv.addLine("Source ID", "Rup ID", "Time Indep Prob", "Time Dep Prob");
		
		AbstractERF erf = MeanUCERF2_ToDB.createUCERF2ERF();
		
		RuptureProbabilityModifier indepMod = indep.getRupProbModifier();
		RuptureProbabilityModifier depMod = dep.getRupProbModifier();
		
		int numIndep = 0;
		int numDep = 0;
		
		for (int sourceID=0; sourceID<erf.getNumSources(); sourceID++) {
			for (int rupID=0; rupID<erf.getNumRuptures(sourceID); rupID++) {
				double indepProb = indepMod.getModifiedProb(sourceID, rupID, 0d);
				double depProb = depMod.getModifiedProb(sourceID, rupID, 0d);
				csv.addLine(sourceID+"", rupID+"", indepProb+"", depProb+"");
				if (indepProb > 0)
					numIndep++;
				if (depProb > 0)
					numDep++;
			}
		}
		
		System.out.println("Indep >0 rups: "+numIndep);
		System.out.println("Dep >0 rups: "+numDep);
		
		csv.writeToFile(new File("/tmp/u2_map_test.csv"));
	}
	
	public void plotMostTriggeredRVs(File outputDir, String prefix, int numToPlot) throws IOException {
		conf.getRupVarProbModifier(); // make sure it gets updated
		
		List<RVProbSortable> rvProbsSortable = conf.getRVProbsSortable();
		if (rvProbsSortable == null)
			// probably a simple UCERF2 mapped or test, skip
			return;
		Collections.sort(rvProbsSortable);
		
		boolean logPlot = true;
		Double customMin = -2d;
		Double customMax = 0d;
		
		double valForMissing = customMin;
		if (logPlot)
			valForMissing = Math.pow(10d, valForMissing);
		
		ShakeMapComputation calc = new ShakeMapComputation(Cybershake_OpenSHA_DBApplication.db);
		calc.setValForMissing(valForMissing);
		
		for (int i=0; i<numToPlot && i<rvProbsSortable.size(); i++) {
			RVProbSortable rvProb = rvProbsSortable.get(i);
			System.out.println("Calculating shakemap for: "+prefix+", "+rvProb);
			
			File outputFile = new File(outputDir, prefix+"_shakemap_"+i+".png");
			File outputTxtFile = new File(outputDir, prefix+"_shakemap_"+i+".txt");
			
			GeoDataSet xyz = calc.getShakeMap(refDatasetID, erfID, rupVarScenID, imTypeID,
					rvProb.getSourceID(), rvProb.getRupID(), rvProb.getHypocenter());
			
			System.out.println("Creating map instance...");
			GMT_InterpolationSettings interpSettings = GMT_InterpolationSettings.getDefaultSettings();
			Region region = new CaliforniaRegions.CYBERSHAKE_MAP_REGION();
			
			InterpDiffMapType[] mapTypes = {InterpDiffMapType.INTERP_MARKS};
			
			CPT cpt = CyberShake_GMT_MapGenerator.getHazardCPT();
			
			AbstractGeoDataSet refScatter = null;
//			if (probGain) {
//				ModProbConfig timeIndepModProb = ModProbConfigFactory.getModProbConfig(1);
//				refScatter = getCustomScatter(timeIndepModProb, imTypeID, isProbAt_IML, val);
//				scatterData = ProbGainCalc.calcProbGain(refScatter, scatterData);
//				mapTypes = gainPlotTypes;
//			}
			
			InterpDiffMap map = new InterpDiffMap(region, null, 0.02, cpt, xyz.copy(), interpSettings, mapTypes);
			map.setCustomLabel(conf.getScenario().toString());
			map.setTopoResolution(TopographicSlopeFile.CA_THREE);
			map.setLogPlot(logPlot);
			map.setDpi(300);
			map.setXyzFileName("base_map.xyz");
			map.setCustomScaleMin(customMin);
			map.setCustomScaleMax(customMax);
			
//			Location hypo = null;
//			if (config != null && config instanceof ScenarioBasedModProbConfig) {
//				hypo = ((ScenarioBasedModProbConfig)config).getHypocenter();
//			}
//			PSXYSymbol symbol = getHypoSymbol(region, hypo);
//			if (symbol != null) {
//				map.addSymbol(symbol);
//			}
			
			String metadata = rvProb.toString()+"\n";
			
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
			
			// now write out info file
			Files.write(rvProb.toString()+"\n", outputTxtFile, Charset.defaultCharset());
		}
	}

	public static void main(String[] args) throws IOException, DocumentException {
		try {
			int refDatasetID = 12; // for rv scen id = 4
			
			boolean debugSiteCalcOnly = false;
			boolean makeMaps = false;
			boolean plotShakemaps = true;
			
			File mapDir = new File("/home/kevin/OpenSHA/UCERF3/cybershake_etas/maps");
			if (!mapDir.exists())
				mapDir.mkdir();
			File curveDir = new File("/home/kevin/OpenSHA/UCERF3/cybershake_etas/curves");
			if (!curveDir.exists())
				curveDir.mkdir();
			File shakemapDir = new File("/home/kevin/OpenSHA/UCERF3/cybershake_etas/shakemaps");
			if (!shakemapDir.exists())
				shakemapDir.mkdir();
			
			ETAS_Cybershake_TimeSpans timeSpan = ETAS_Cybershake_TimeSpans.ONE_WEEK;
			
			ETASModProbConfig conf;
			ETASCurveCalc calc;
			
			FaultSystemSolution sol = FaultSystemIO.loadSol(new File("/home/kevin/OpenSHA/UCERF3/cybershake_etas/ucerf2_mapped_sol.zip"));
			
			String[] curveSites = { "STNI", "S157", "MRSD" };
			List<ETASCurveCalc> calcs = Lists.newArrayList();
			List<String> names = Lists.newArrayList();
			List<String> filePrefixes = Lists.newArrayList();
			List<Color> colors = Lists.newArrayList();
			List<ArbDiscrGeoDataSet> maps = Lists.newArrayList();
			
			conf = new ETASModProbConfig(ETAS_CyberShake_Scenarios.PARKFIELD, timeSpan, sol,
					new File[] {new File("/home/kevin/OpenSHA/UCERF3/cybershake_etas/sims/2014_08_07-parkfield-nospont_combined.zip")},
					new File("/home/kevin/OpenSHA/UCERF3/cybershake_etas/mappings.csv"));
			calc = new ETASCurveCalc(conf, 21, refDatasetID);
			calcs.add(calc);
			names.add("Parkfield");
			filePrefixes.add("parkfield");
			colors.add(Color.RED);
			if (!debugSiteCalcOnly && makeMaps)
				maps.add(calc.calcMap(new File(mapDir, "parkfield_hazard.png")));
			
			conf = new ETASModProbConfig(ETAS_CyberShake_Scenarios.BOMBAY_M6, timeSpan, sol,
					new File[] {new File("/home/kevin/OpenSHA/UCERF3/cybershake_etas/sims/2014_08_07-bombay_beach_m6_combined.zip")},
					new File("/home/kevin/OpenSHA/UCERF3/cybershake_etas/mappings.csv"));
			calc = new ETASCurveCalc(conf, 21, refDatasetID);
			calcs.add(calc);
			names.add("Bombay Beach");
			filePrefixes.add("bombay");
			colors.add(Color.ORANGE);
			if (!debugSiteCalcOnly && makeMaps)
				maps.add(calc.calcMap(new File(mapDir, "bombay_hazard.png")));
			
//			conf = new ETASModProbConfig(ETAS_CyberShake_Scenarios.TEST_NEGLIGABLE, timeSpan, sol,
//					new File("/home/kevin/OpenSHA/UCERF3/cybershake_etas/sims/2014_08_01-parkfield/results.zip"),
//					new File("/home/kevin/OpenSHA/UCERF3/cybershake_etas/mappings.csv"));
//			publish_curves = false;
//			calc = new ETASCurveCalc(conf, 21, refDatasetID);
//			calcs.add(calc);
//			names.add("Test Negligable");
//			filePrefixes.add("test_negligable");
//			colors.add(Color.GRAY);
//			if (!debugSiteCalcOnly && makeMaps)
//				maps.add(calc.calcMap(new File(mapDir, "test_negligable_hazard.png")));
			
//			conf = new ETASModProbConfig(ETAS_CyberShake_Scenarios.TEST_BOMBAY_M6_SUBSET, timeSpan, sol,
//					new File("/home/kevin/OpenSHA/UCERF3/cybershake_etas/sims/2014_07_31-bombay_beach_m6/results.zip"),
//					new File("/home/kevin/OpenSHA/UCERF3/cybershake_etas/mappings.csv"));
//			publish_curves = false;
//			calc = new ETASCurveCalc(conf, 21, refDatasetID);
//			ArbDiscrGeoDataSet modMap = calc.calcMap(new File(mapDir, "test_bombay_subset_hazard.png"));
			
			if (curveSites.length > 0) {
				FaultSystemSolution timeDepSol = FaultSystemIO.loadSol(
						new File("/home/kevin/OpenSHA/UCERF3/cybershake_etas/ucerf2_mapped_timedep_sol.zip"));
				conf = new ETASModProbConfig(ETAS_CyberShake_Scenarios.MAPPED_UCERF2_TIMEDEP,
						timeSpan, timeDepSol,
						new File[0],
						new File("/home/kevin/OpenSHA/UCERF3/cybershake_etas/mappings.csv"));
				calc = new ETASCurveCalc(conf, 21, refDatasetID);
				calcs.add(calc);
				names.add("UCERF2 Time Dependent");
				filePrefixes.add("ucerf2_dep");
				colors.add(Color.GREEN);
				
				if (!debugSiteCalcOnly && makeMaps)
					maps.add(calc.calcMap(new File(mapDir, "ucerf2_dep_hazard.png")));
			}
			
			conf = new ETASModProbConfig(ETAS_CyberShake_Scenarios.MAPPED_UCERF2, timeSpan, sol,
					new File[0],
					new File("/home/kevin/OpenSHA/UCERF3/cybershake_etas/mappings.csv"));
			calc = new ETASCurveCalc(conf, 21, refDatasetID);
			
			if (debugSiteCalcOnly) {
				if (curveSites.length > 0)
					writeU2MappedCompare(conf, calcs.get(calcs.size()-1).conf);
				// calculate individual curves
				for (ETASCurveCalc subCalc : calcs)
					for (String siteName : curveSites)
						subCalc.manualCalc(siteName);
				// do UCERF2
				for (String siteName : curveSites)
					calc.manualCalc(siteName);
			} else if (makeMaps) {
				// create ratio maps
				ArbDiscrGeoDataSet baseMap = calc.calcMap(new File(mapDir, "ref_hazard.png"));
				for (int i=0; i<calcs.size(); i++)
					createRatioMap(maps.get(i), baseMap, names.get(i)+" Gain",
							new File(mapDir, filePrefixes.get(i)+"_gain.png"));
			}
			
//			createRatioMap(modMap, baseMap, "Bombay Beach Prob Gain", new File(outputDir, "bombay_gain.png"));
//			createRatioMap(modMap, baseMap, gainName, gainFile);
//			createRatioMap(modMap, baseMap, "Test Negligable Prob Gain", new File(outputDir, "test_negligable_gain.png"));
//			createRatioMap(modMap, baseMap, "Test Bombay 50% Subset Prob Gain", new File(outputDir, "test_bombay_subset_gain.png"));
			
			calcs.add(calc);
			names.add("UCERF2 Time Independent");
			filePrefixes.add("ucerf2");
			colors.add(Color.BLUE);
			
			if (debugSiteCalcOnly || makeMaps)
				for (String siteName : curveSites)
					plotComparisonCurves(calcs, names, colors, siteName, curveDir);
			
			if (plotShakemaps && !debugSiteCalcOnly) {
				for (int i=0; i<calcs.size(); i++)
					calcs.get(i).plotMostTriggeredRVs(shakemapDir, filePrefixes.get(i), 5);
			}
			
			System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

}
