package org.opensha.sha.cybershake.etas;

import java.awt.Color;
import java.awt.Font;
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
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.ui.TextAnchor;
import org.opensha.commons.data.CSVFile;
import org.opensha.commons.data.Site;
import org.opensha.commons.data.TimeSpan;
import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.commons.data.function.DefaultXY_DataSet;
import org.opensha.commons.data.function.DiscretizedFunc;
import org.opensha.commons.data.region.CaliforniaRegions;
import org.opensha.commons.data.siteData.OrderedSiteDataProviderList;
import org.opensha.commons.data.siteData.SiteDataValue;
import org.opensha.commons.data.xyz.AbstractGeoDataSet;
import org.opensha.commons.data.xyz.ArbDiscrGeoDataSet;
import org.opensha.commons.data.xyz.GeoDataSet;
import org.opensha.commons.data.xyz.XYZ_DataSet;
import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.LocationUtils;
import org.opensha.commons.geo.Region;
import org.opensha.commons.gui.plot.GraphWindow;
import org.opensha.commons.gui.plot.HeadlessGraphPanel;
import org.opensha.commons.gui.plot.PlotCurveCharacterstics;
import org.opensha.commons.gui.plot.PlotElement;
import org.opensha.commons.gui.plot.PlotLineType;
import org.opensha.commons.gui.plot.PlotSpec;
import org.opensha.commons.gui.plot.PlotSymbol;
import org.opensha.commons.mapping.gmt.elements.PSXYSymbol;
import org.opensha.commons.mapping.gmt.elements.TopographicSlopeFile;
import org.opensha.commons.param.Parameter;
import org.opensha.commons.util.ExceptionUtils;
import org.opensha.commons.util.FileUtils;
import org.opensha.commons.util.cpt.CPT;
import org.opensha.commons.util.threads.Task;
import org.opensha.commons.util.threads.ThreadedTaskComputer;
import org.opensha.sha.calc.HazardCurveCalculator;
import org.opensha.sha.calc.ScenarioShakeMapCalculator;
import org.opensha.sha.calc.hazardMap.BinaryHazardCurveReader;
import org.opensha.sha.calc.hazardMap.BinaryHazardCurveWriter;
import org.opensha.sha.calc.hazardMap.HazardCurveSetCalculator;
import org.opensha.sha.calc.hazardMap.HazardDataSetLoader;
import org.opensha.sha.cybershake.HazardCurveFetcher;
import org.opensha.sha.cybershake.ModProbConfig;
import org.opensha.sha.cybershake.bombay.ModProbConfigFactory;
import org.opensha.sha.cybershake.bombay.ScenarioBasedModProbConfig;
import org.opensha.sha.cybershake.calc.HazardCurveComputation;
import org.opensha.sha.cybershake.calc.RuptureProbabilityModifier;
import org.opensha.sha.cybershake.calc.ShakeMapComputation;
import org.opensha.sha.cybershake.db.CachedPeakAmplitudesFromDB;
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
import org.opensha.sha.cybershake.maps.HardCodedScenarioShakeMapGen;
import org.opensha.sha.cybershake.maps.InterpDiffMap;
import org.opensha.sha.cybershake.maps.ProbGainCalc;
import org.opensha.sha.cybershake.maps.InterpDiffMap.InterpDiffMapType;
import org.opensha.sha.cybershake.maps.servlet.CS_InterpDiffMapServletAccessor;
import org.opensha.sha.cybershake.plot.HazardCurvePlotter;
import org.opensha.sha.earthquake.AbstractERF;
import org.opensha.sha.earthquake.ERF;
import org.opensha.sha.earthquake.ProbEqkRupture;
import org.opensha.sha.faultSurface.RuptureSurface;
import org.opensha.sha.gui.infoTools.IMT_Info;
import org.opensha.sha.imr.AttenuationRelationship;
import org.opensha.sha.imr.attenRelImpl.NGAWest_2014_Averaged_AttenRel;
import org.opensha.sha.imr.param.IntensityMeasureParams.SA_Param;
import org.opensha.sha.util.SiteTranslator;

import scratch.UCERF3.FaultSystemSolution;
import scratch.UCERF3.enumTreeBranches.FaultModels;
import scratch.UCERF3.erf.ETAS.ETAS_EqkRupture;
import scratch.UCERF3.utils.FaultSystemIO;
import scratch.UCERF3.utils.IDPairing;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.google.common.primitives.Doubles;

public class ETASCurveCalc {
	
	protected DBAccess db = Cybershake_OpenSHA_DBApplication.db;
	
	private static final File amps_cache_dir = new File("/home/kevin/CyberShake/amps_cache");
	private static final File gmpe_cache_dir = new File("/home/kevin/OpenSHA/UCERF3/cybershake_etas/.gmpe_cache");
	
	private HazardCurveComputation calc;
	private ETASModProbConfig conf;
	private int refDatasetID;
	private int imTypeID;
	private CybershakeIM imType;
	
	private Map<CybershakeSite, DiscretizedFunc> prevCurves;
	private Map<CybershakeSite, DiscretizedFunc> prevGMPECurves;
	
	private static boolean publish_curves = true;
	private static boolean force_recalc = false;
	
	private static final double log_plot_min = 1e-8;
	
	private ArrayList<Double> xVals;
	
	private List<CybershakeSite> sites;
	private List<Integer> refCurveIDs;
	
	private HazardCurve2DB curve2db;
	private Runs2DB runs2db;
	
	private static final int ref_dataset_ID = 35;
	private static final int erfID = 35;
	private static final int sgtVarID = 8;
	private static final int rupVarScenID = 4;
	private static final int velModelID = 5;
	
	private static boolean isProbAt_IML = true;
	private static boolean logPlot = true;
//	private static double val = 0.1;
//	private static Double customMin = -6d;
//	private static Double customMax = -2d;
	private static double val = 0.2;
	private static Double customMin = -6d;
	private static Double customMax = -2d;
	private static Region region = new CaliforniaRegions.CYBERSHAKE_MAP_REGION();
	private static GMT_InterpolationSettings interpSettings = GMT_InterpolationSettings.getDefaultSettings();
	private static InterpDiffMapType[] mapTypes = {InterpDiffMapType.INTERP_MARKS};
	
	private NGAWest_2014_Averaged_AttenRel imr;
	
	private static final TopographicSlopeFile topo_slope_res = TopographicSlopeFile.CA_THREE;
//	private static final TopographicSlopeFile topo_slope_res = TopographicSlopeFile.US_EIGHTEEN;
	
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
		
//		double val = 0.1;
//		Double customMin = -7d;
//		Double customMax = -1d;
		
		String labelAdd = ", "+(float)val+"G 3s SA";
		
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
		
		List<CalcTask> calcTasks = Lists.newArrayList();
		
		for (int i = 0; i < sites.size(); i++) {
			CybershakeSite site = sites.get(i);
			int refCurveID = refCurveIDs.get(i);
			if (site.type_id == CybershakeSite.TYPE_TEST_SITE)
				continue; // TEST
			
			System.out.println("Calculating for: "+site.name);
			
			int runID = curve2db.getRunIDForCurve(refCurveID);
			CybershakeRun run = runs2db.getRun(runID);
			Preconditions.checkState(run.getVelModelID() == velModelID,
					"Vel Model Mismatch: "+run.getVelModelID()+" != "+velModelID);
			Preconditions.checkState(run.getSgtVarID() == sgtVarID,
					"SGT ID Mismatch: "+run.getSgtVarID()+" != "+sgtVarID);
			Preconditions.checkState(run.getRupVarScenID() == rupVarScenID,
					"RV ID Mismatch: "+run.getRupVarScenID()+" != "+rupVarScenID);
//			CybershakeRun run = runs2db.getLatestRun(site.id, erfID, sgtVarID, rupVarScenID, velModelID, null, null, null, null);
			DiscretizedFunc func = null;
			if (publishDatasetID >= 0 && !force_recalc) {
				int curveID = curve2db.getHazardCurveID(run.getRunID(), publishDatasetID, imTypeID);
//				System.out.println("Curve: "+curveID);
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
					buildCurveCalc();
					calc.setRupVarProbModifier(conf.getRupVarProbModifier());
					calc.setRupProbModifier(conf.getRupProbModifier());
				}
				calcTasks.add(new CalcTask(run, site, publishDatasetID));
				
				if (publish_curves) {
					// post to DB
					if (!authenticated) {
						db = Cybershake_OpenSHA_DBApplication.getAuthenticatedDBAccess(true);
						authenticated = true;
						curve2db = new HazardCurve2DB(db);
					}
				}
			} else {
				double zVal = HazardDataSetLoader.getCurveVal(func, isProbAt_IML, val);
				Preconditions.checkState(Doubles.isFinite(zVal), "Z not finite: "+zVal);
				if (logPlot && zVal < log_plot_min)
					zVal = log_plot_min;
				Preconditions.checkState(!logPlot || zVal >= log_plot_min);
				xyz.set(new Location(site.lat, site.lon), zVal);
				prevCurves.put(site, func);
			}
		}
		// now calculate threaded if needed
		if (!calcTasks.isEmpty()) {
			try {
				new ThreadedTaskComputer(calcTasks).computeThreaded();
			} catch (InterruptedException e) {
				ExceptionUtils.throwAsRuntimeException(e);
			}
			for (CalcTask task : calcTasks) {
				DiscretizedFunc func = task.func;
				if (func == null)
					continue;
				CybershakeSite site = task.site;
				double zVal = HazardDataSetLoader.getCurveVal(func, isProbAt_IML, val);
				Preconditions.checkState(Doubles.isFinite(zVal), "Z not finite: "+zVal);
				if (logPlot && zVal < log_plot_min)
					zVal = log_plot_min;
				Preconditions.checkState(!logPlot || zVal >= log_plot_min);
				xyz.set(new Location(site.lat, site.lon), zVal);
				prevCurves.put(site, func);
			}
		}
		
		System.out.println("Creating map instance...");
		
		CPT cpt = CyberShake_GMT_MapGenerator.getHazardCPT();
		
		InterpDiffMap map = new InterpDiffMap(region, null, 0.02, cpt, xyz.copy(), interpSettings, mapTypes);
		map.setAutoLabel(false);
		map.setCustomLabel(conf.getScenario().toString()+" Hazard"+labelAdd);
		map.setTopoResolution(topo_slope_res);
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
	
	private synchronized void checkInitGMPE() {
		if (imr == null) {
			imr = new NGAWest_2014_Averaged_AttenRel(null, false);
			imr.setParamDefaults();
			imr.setIntensityMeasure(SA_Param.NAME);
			SA_Param.setPeriodInSA_Param(imr.getIntensityMeasure(), 3d);
		}
	}
	
	public ArbDiscrGeoDataSet calcGMPEMap() throws IOException {
		return calcGMPEMap(null);
	}
	
	public ArbDiscrGeoDataSet calcGMPEMap(File outputFile) throws IOException {
		loadCurveSites();
		
		Preconditions.checkState(sites.size() == refCurveIDs.size());
		
		prevGMPECurves = Maps.newHashMap();
		
		checkInitGMPE();
		
		File cacheFile = new File(gmpe_cache_dir, conf.getScenario().name()+"_"+conf.getTimeSpan().name()
				+"_"+imr.getShortName()+"_ref"+refDatasetID+".bin");
		
		Map<String, ArbitrarilyDiscretizedFunc> cachedCurveMap = Maps.newHashMap();
		if (cacheFile.exists()) {
			try {
				BinaryHazardCurveReader br = new BinaryHazardCurveReader(cacheFile.getAbsolutePath());
				Map<Location, ArbitrarilyDiscretizedFunc> tempMap = br.getCurveMap();
				for (Location loc : tempMap.keySet()) {
					String str = (int)(loc.getLatitude()*100d)+"_"+(int)(loc.getLongitude()*100d);
					cachedCurveMap.put(str, tempMap.get(loc));
				}
				System.out.println("Loaded "+cachedCurveMap.size()+" curves from "+cacheFile.getName());
			} catch (Exception e) {
				throw ExceptionUtils.asRuntimeException(e);
			}
		} else {
			cachedCurveMap = Maps.newHashMap();
		}
		
		ArbDiscrGeoDataSet xyz = new ArbDiscrGeoDataSet(true);
//		boolean logPlot = false;
//		boolean isProbAt_IML = false;
//		double val = 0.0004;
////		double val = 0.02;
//		Double customMin = 0d;
//		Double customMax = 1d;
		
//		double val = 0.1;
//		Double customMin = -7d;
//		Double customMax = -1d;
		
		String labelAdd = ", "+(float)val+"G 3s SA";
		
		ERF gmpeERF = conf.getModERFforGMPE();
		HazardCurveCalculator calc = new HazardCurveCalculator();
		
		List<Parameter<?>> siteParams = Lists.newArrayList();
		for (Parameter<?> param : imr.getSiteParams())
			siteParams.add((Parameter<?>) param.clone());
		
		DiscretizedFunc xVals = IMT_Info.getUSGS_SA_Function();
		
		OrderedSiteDataProviderList provs = HazardCurvePlotter.createProviders(velModelID);
		SiteTranslator trans = new SiteTranslator();
		
		Map<Location, ArbitrarilyDiscretizedFunc> curvesToBeWritten = Maps.newHashMap();
		
		for (int i = 0; i < sites.size(); i++) {
			CybershakeSite site = sites.get(i);
			if (site.type_id == 4)
				continue; // TEST
			
			Location loc = site.createLocation();
			ArbitrarilyDiscretizedFunc func = null;
			String cacheStr = (int)(loc.getLatitude()*100d)+"_"+(int)(loc.getLongitude()*100d);
			if (cachedCurveMap.containsKey(cacheStr)) {
				System.out.println("Loaded "+site.name+" from cache");
				func = cachedCurveMap.get(cacheStr);
			} else {
				System.out.println("Calculating GMPE for: "+site.name);
				// create site with site params
				Site gmpeSite = new Site(loc);
				ArrayList<SiteDataValue<?>> datas = provs.getBestAvailableData(gmpeSite.getLocation());
				for (Parameter<?> param : siteParams) {
					param = (Parameter<?>) param.clone();
					trans.setParameterValue(param, datas);
					gmpeSite.addParameter(param);
				}
				
				// now calculate
				func = HazardCurveSetCalculator.getLogFunction(xVals);
				calc.getHazardCurve(func, gmpeSite, imr, gmpeERF);
				// un-log
				func = HazardCurveSetCalculator.unLogFunction(xVals, func);
				
				cachedCurveMap.put(cacheStr, func);
			}
			
			double zVal = HazardDataSetLoader.getCurveVal(func, isProbAt_IML, val);
			Preconditions.checkState(Doubles.isFinite(zVal), "Z not finite: "+zVal);
			if (logPlot && zVal < log_plot_min)
				zVal = log_plot_min;
			Preconditions.checkState(!logPlot || zVal >= log_plot_min);
			xyz.set(new Location(site.lat, site.lon), zVal);
			prevGMPECurves.put(site, func);
			
			curvesToBeWritten.put(loc, func);
		}
		
		// write cache
		BinaryHazardCurveWriter bw = new BinaryHazardCurveWriter(cacheFile);
		bw.writeCurves(curvesToBeWritten);
		
		System.out.println("Creating map instance...");
		
		CPT cpt = CyberShake_GMT_MapGenerator.getHazardCPT();
		
		InterpDiffMap map = new InterpDiffMap(region, null, 0.02, cpt, xyz.copy(), interpSettings, mapTypes);
		map.setAutoLabel(false);
		map.setCustomLabel(conf.getScenario().toString()+" NGA2 Hazard"+labelAdd);
		map.setTopoResolution(topo_slope_res);
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
	
	private class CalcTask implements Task {
		
		private CybershakeRun run;
		private CybershakeSite site;
		private DiscretizedFunc func;
		private int publishDatasetID;
		
		public CalcTask(CybershakeRun run, CybershakeSite site, int publishDatasetID) {
			this.run = run;
			this.site = site;
			this.publishDatasetID = publishDatasetID;
		}

		@Override
		public void compute() {
			try {
				func = calc.computeHazardCurve(xVals, run, imType);
				
				if (func == null) {
					System.out.println("skipping, null curve?");
					return;
				}
				
				if (publish_curves) {
					// post to DB
					synchronized (CalcTask.class) {
						curve2db.insertHazardCurve(run.getRunID(), imTypeID, func, publishDatasetID);
					}
				}
			} catch (Exception e) {
				System.out.println("FATAL EXCEPTOIN");
				e.printStackTrace();
				System.exit(0);
			}
		}
		
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
			buildCurveCalc();
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
		double minX = 0d;
		double maxX = 3d;
		
		createRatioMap(numerator, denominator, label, outputFile, minX, maxX);
	}
	
	public static void createRatioMap(GeoDataSet numerator, GeoDataSet denominator, String label, File outputFile,
			double minX, double maxX) throws IOException {
		System.out.println("Creating map instance...");
		
		CPT cpt = CyberShake_GMT_MapGenerator.getHazardCPT();
		cpt = cpt.rescale(minX, maxX);
		GeoDataSet xyz = ProbGainCalc.calcProbGain(denominator, numerator);
		
		InterpDiffMap map = new InterpDiffMap(region, null, 0.02, cpt, xyz, interpSettings, mapTypes);
		map.setAutoLabel(false);
		map.setCustomLabel(label);
		map.setTopoResolution(topo_slope_res);
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
			PlotLineType plt;
//			if (labels.get(i).toLowerCase().contains("fault"))
//				plt = PlotLineType.DASHED;
//			else
				plt = PlotLineType.SOLID;
			chars.add(new PlotCurveCharacterstics(plt, 3f, PlotSymbol.FILLED_CIRCLE, 8f, colors.get(i)));
		}
		
		ETAS_Cybershake_TimeSpans timeSpan = calcs.get(0).conf.getTimeSpan();
		for (int i=1; i<calcs.size(); i++)
			Preconditions.checkState(calcs.get(i).conf.getTimeSpan() == timeSpan, "inconsistent time spans");
		
		PlotSpec spec = new PlotSpec(curves, chars, "CyberShake OEF Curves: "+site.short_name, "3s SA (g)",
				timeSpan.toString()+" Probability of Exceedance");
		spec.setLegendVisible(true);
		
		HeadlessGraphPanel gp = new HeadlessGraphPanel();
		gp.setTickLabelFontSize(18);
		gp.setAxisLabelFontSize(20);
		gp.setPlotLabelFontSize(21);
		gp.setBackgroundColor(Color.WHITE);
		
		gp.setXLog(true);
		gp.setYLog(true);
		gp.setUserBounds(1e-2, 1e0, 1e-7, 1e-2);
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
	
	public void plotMostTriggeredRVs(File outputDir, String prefix, int numToPlot, boolean gmpe) throws IOException {
		conf.getRupVarProbModifier(); // make sure it gets updated
		
		List<RVProbSortable> rvProbsSortable = conf.getRVProbsSortable();
		if (rvProbsSortable == null)
			// probably a simple UCERF2 mapped or test, skip
			return;
		
		// now combine RVs for the same rupture
		Map<IDPairing, RVProbSortable> combinedMap = Maps.newHashMap();
		for (RVProbSortable rvProb : rvProbsSortable) {
			IDPairing pair = new IDPairing(rvProb.getSourceID(), rvProb.getRupID());
			RVProbSortable combined = combinedMap.get(pair);
			if (combined == null) {
				combined = new RVProbSortable(rvProb.getSourceID(), rvProb.getRupID(), rvProb.getMag());
				combinedMap.put(pair, combined);
			}
			combined.addRV(rvProb.getOccurances(), rvProb.getTriggerRate(),
					rvProb.getRvIDs().get(0), rvProb.getHypocenters().get(0));
		}
		rvProbsSortable = Lists.newArrayList(combinedMap.values());
		
		Collections.sort(rvProbsSortable);
		
		boolean logPlot = true;
		Double customMin = -2d;
		Double customMax = 0d;
		
		double valForMissing = customMin;
		if (logPlot)
			valForMissing = Math.pow(10d, valForMissing);
		
		ShakeMapComputation calc = new ShakeMapComputation(Cybershake_OpenSHA_DBApplication.db);
		calc.setValForMissing(valForMissing);
		
		ScenarioShakeMapCalculator gmpeCalc = null;
		ArrayList<Site> gmpeSites = null;
		if (gmpe) {
			checkInitGMPE();
			gmpeCalc = new ScenarioShakeMapCalculator();
			gmpeSites = Lists.newArrayList();
			
			List<Parameter<?>> siteParams = Lists.newArrayList();
			for (Parameter<?> param : imr.getSiteParams())
				siteParams.add((Parameter<?>) param.clone());
			
			OrderedSiteDataProviderList provs = HazardCurvePlotter.createProviders(velModelID);
			SiteTranslator trans = new SiteTranslator();
			for (CybershakeSite site : sites) {
				Site gmpeSite = new Site(site.createLocation());
				ArrayList<SiteDataValue<?>> datas = provs.getBestAvailableData(gmpeSite.getLocation());
				for (Parameter<?> param : siteParams) {
					param = (Parameter<?>) param.clone();
					trans.setParameterValue(param, datas);
					gmpeSite.addParameter(param);
				}
				gmpeSites.add(gmpeSite);
			}
		}
		
		for (int i=0; i<numToPlot && i<rvProbsSortable.size(); i++) {
			RVProbSortable rvProb = rvProbsSortable.get(i);
			System.out.println("Calculating shakemap for: "+prefix+", "+rvProb);
			
			File outputFile = new File(outputDir, prefix+"_shakemap_"+i+".png");
			File gmpeOutputFile = new File(outputDir, prefix+"_shakemap_gmpe_"+i+".png");
			File outputTxtFile = new File(outputDir, prefix+"_shakemap_"+i+".txt");
			
			GeoDataSet xyz = calc.getShakeMap(refDatasetID, erfID, rupVarScenID, imTypeID,
					rvProb.getSourceID(), rvProb.getRupID(), rvProb.getHypocenters());
			
			System.out.println("Creating map instance...");
			GMT_InterpolationSettings interpSettings = GMT_InterpolationSettings.getDefaultSettings();
			Region region = new CaliforniaRegions.CYBERSHAKE_MAP_REGION();
			
			InterpDiffMapType[] mapTypes = {InterpDiffMapType.INTERP_MARKS};
			
			CPT cpt = CyberShake_GMT_MapGenerator.getHazardCPT();
			
			InterpDiffMap map = new InterpDiffMap(region, null, 0.02, cpt, xyz.copy(), interpSettings, mapTypes);
			map.setAutoLabel(false);
			map.setCustomLabel(conf.getScenario().toString()+", M"+(float)rvProb.getMag()+" ShakeMap");
			map.setTopoResolution(topo_slope_res);
			map.setLogPlot(logPlot);
			map.setDpi(300);
			map.setXyzFileName("base_map.xyz");
			map.setCustomScaleMin(customMin);
			map.setCustomScaleMax(customMax);
			
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
			
			if (gmpe) {
				ERF erf = conf.getCS_UCERF2_ERF();
				ArrayList<AttenuationRelationship> gmpes = Lists.newArrayList();
				gmpes.add(imr);
				
				ProbEqkRupture rupture = erf.getRupture(rvProb.getSourceID(), rvProb.getRupID());
				
//				GeoDataSet gmpeXYZ = gmpeCalc.getScenarioShakeMapData(gmpes, Lists.newArrayList(1d),
//						gmpeSites, rupture, false, 0.5);
				GeoDataSet gmpeXYZ = HardCodedScenarioShakeMapGen.computeBaseMap(rupture, imr, gmpeSites, false, 0.5);
				
				System.out.println("Creating map instance...");
				
				map = new InterpDiffMap(region, null, 0.02, cpt, gmpeXYZ.copy(), interpSettings, mapTypes);
				map.setAutoLabel(false);
				map.setCustomLabel(conf.getScenario().toString()+", M"+(float)rvProb.getMag()+" NGA2 ShakeMap");
				map.setTopoResolution(topo_slope_res);
				map.setLogPlot(logPlot);
				map.setDpi(300);
				map.setXyzFileName("base_map.xyz");
				map.setCustomScaleMin(customMin);
				map.setCustomScaleMax(customMax);
				
				System.out.println("Making map...");
				try {
					String address = CS_InterpDiffMapServletAccessor.makeMap(null, map, metadata);
					System.out.println(address);
					if (outputFile != null)
						FileUtils.downloadURL(address+"interpolated_marks.150.png", gmpeOutputFile);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	private void buildCurveCalc() {
		calc = new HazardCurveComputation(db);
		if (amps_cache_dir.exists())
			calc.setPeakAmpsAccessor(new CachedPeakAmplitudesFromDB(db, amps_cache_dir, conf.getCS_UCERF2_ERF()));
	}
	
	private Map<Integer, Map<CybershakeSite, DiscretizedFunc>> calcConvergeCurves(int catalogInterval, boolean randomizeOrder) {
		Map<Integer, Map<CybershakeSite, DiscretizedFunc>> numCurvesMaps = Maps.newHashMap();
		
		conf.getRupVarProbModifier(); // make sure everything is loaded
		if (calc == null)
			buildCurveCalc();
		List<List<ETAS_EqkRupture>> origCatalogs = conf.getCatalogs();
		if (origCatalogs == null) {
			System.out.println("Can't do convergence for: "+conf.getScenario().toString());
			return null;
		}
		if (randomizeOrder) {
			origCatalogs = Lists.newArrayList(origCatalogs);
			Collections.shuffle(origCatalogs);
		}
		
		List<List<ETAS_EqkRupture>> curCatalogs = Lists.newArrayList();
		while (curCatalogs.size() < origCatalogs.size()) {
			int maxSize = curCatalogs.size()+catalogInterval;
			for (int i=curCatalogs.size(); i<origCatalogs.size() && i<maxSize; i++)
				curCatalogs.add(origCatalogs.get(i));
			
			Map<CybershakeSite, DiscretizedFunc> curveMap = Maps.newHashMap();
			numCurvesMaps.put(curCatalogs.size(), curveMap);
			
			System.out.println("Calculating for catalog size "+curCatalogs.size());
			
			conf.setCatalogs(curCatalogs);
			calc.setRupVarProbModifier(conf.getRupVarProbModifier());
			calc.setRupProbModifier(conf.getRupProbModifier());
			
			for (int i=0; i<sites.size(); i++) {
				CybershakeSite site = sites.get(i);
				int refCurveID = refCurveIDs.get(i);
				int runID = curve2db.getRunIDForCurve(refCurveID);
				CybershakeRun run = runs2db.getRun(runID);
				DiscretizedFunc func = calc.computeHazardCurve(xVals, run, imType);
				
				curveMap.put(site, func);
			}
		}
		
		return numCurvesMaps;
	}
	
	private void writeConvergenceLinesPlot(File outputDir, int catalogInterval, boolean randomizeOrder) throws IOException {
		Map<Integer, Map<CybershakeSite, DiscretizedFunc>> numCurvesMaps = calcConvergeCurves(catalogInterval, randomizeOrder);
		if (numCurvesMaps == null)
			return;
		writeConvergenceLinesPlot(outputDir, numCurvesMaps);
	}
	
	private void writeConvergenceLinesPlot(File outputDir, Map<Integer, Map<CybershakeSite, DiscretizedFunc>> numCurvesMaps)
			throws IOException {
		Map<CybershakeSite, List<Double>> siteValsMap = Maps.newHashMap();
		
		List<Double> catalogSizes = Lists.newArrayList();
		
		for (int catSize : numCurvesMaps.keySet()) {
			catalogSizes.add((double)catSize);
			Map<CybershakeSite, DiscretizedFunc> curveMap = numCurvesMaps.get(catSize);
			for (CybershakeSite site : curveMap.keySet()) {
				DiscretizedFunc func = curveMap.get(site);
				List<Double> siteVals = siteValsMap.get(site);
				if (siteVals == null) {
					siteVals = Lists.newArrayList();
					siteValsMap.put(site, siteVals);
				}
				siteVals.add(HazardDataSetLoader.getCurveVal(func, isProbAt_IML, val));
			}
		}
		
		List<DiscretizedFunc> funcs = Lists.newArrayList();
		
		double startY = 1d;
		double deltaY = 1d;
		double halfDelta = 0.5*deltaY;
		
		List<XYTextAnnotation> anns = Lists.newArrayList();
		List<PlotCurveCharacterstics> chars = Lists.newArrayList();
		
		for (CybershakeSite site : sites) {
			List<Double> siteVals = siteValsMap.get(site);
			double meanVal = siteVals.get(siteVals.size()-1);
			if (meanVal == 0d)
				continue;
			ArbitrarilyDiscretizedFunc func = new ArbitrarilyDiscretizedFunc();
			ArbitrarilyDiscretizedFunc flatFunc = new ArbitrarilyDiscretizedFunc();
			
			double topVal = startY + halfDelta;
			double botVal = startY - halfDelta;
			
			for (int i=0; i<siteVals.size(); i++) {
				double siteVal = siteVals.get(i);
				double plotVal = botVal + siteVal/(2d*meanVal);
				func.set(catalogSizes.get(i), plotVal);
				flatFunc.set(catalogSizes.get(i), startY);
			}
			
			XYTextAnnotation ann = new XYTextAnnotation(site.short_name, catalogSizes.get(0)*0.98, startY);
			ann.setTextAnchor(TextAnchor.CENTER_RIGHT);
			ann.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
			anns.add(ann);
			
			func.setName(site.short_name+" reference");
			funcs.add(flatFunc);
			chars.add(new PlotCurveCharacterstics(PlotLineType.DASHED, 1f, Color.GRAY));
			
			func.setName(site.short_name);
			funcs.add(func);
			chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, Color.BLACK));
			
			startY += deltaY;
		}
		
		PlotSpec spec = new PlotSpec(funcs, chars, conf.getScenario().toString()+" Convergence",
				"# Simulations", "Relative Prob");
		spec.setPlotAnnotations(anns);
		
		String prefix = "converge_lines_"+conf.getScenario().name();
		
		HeadlessGraphPanel gp = new HeadlessGraphPanel();
		gp.setTickLabelFontSize(18);
		gp.setAxisLabelFontSize(20);
		gp.setPlotLabelFontSize(21);
		gp.setBackgroundColor(Color.WHITE);
		
		gp.setUserBounds(0, funcs.get(0).getMaxX(), 0, startY+halfDelta);
		gp.drawGraphPanel(spec);
		gp.getCartPanel().setSize(1000, 10000);
		gp.saveAsPDF(new File(outputDir, prefix+".pdf").getAbsolutePath());
		gp.saveAsPNG(new File(outputDir, prefix+".png").getAbsolutePath());
	}
	
	private GeoDataSet extractMap(Map<CybershakeSite, DiscretizedFunc> curvesMap) {
		ArbDiscrGeoDataSet xyz = new ArbDiscrGeoDataSet(true);
		
		for (CybershakeSite site : curvesMap.keySet()) {
			double zVal = HazardDataSetLoader.getCurveVal(curvesMap.get(site), isProbAt_IML, val);
			Preconditions.checkState(Doubles.isFinite(zVal), "Z not finite: "+zVal);
			if (logPlot && zVal < log_plot_min)
				zVal = log_plot_min;
			Preconditions.checkState(!logPlot || zVal >= log_plot_min);
			xyz.set(new Location(site.lat, site.lon), zVal);
		}
		
		return xyz;
	}
	
	private void writeConvergenceMaps(File outputDir, Map<Integer, Map<CybershakeSite, DiscretizedFunc>> numCurvesMaps)
			throws IOException {
		List<Integer> sizes = Lists.newArrayList(numCurvesMaps.keySet());
		Collections.sort(sizes);
		
		GeoDataSet finalMap = extractMap(numCurvesMaps.get(sizes.get(sizes.size()-1)));
		
		int maxSize = sizes.get(sizes.size()-1);
		int maxSizeDigits = (maxSize+"").length();
		
		for (int size : sizes) {
			String sizeStr = size+"";
			while (sizeStr.length() < maxSizeDigits)
				sizeStr = "0"+sizeStr;
			
			GeoDataSet xyz = extractMap(numCurvesMaps.get(size));
			
			CPT cpt = CyberShake_GMT_MapGenerator.getHazardCPT();
			
			InterpDiffMap map = new InterpDiffMap(region, null, 0.02, cpt, xyz.copy(), interpSettings, mapTypes);
			map.setAutoLabel(false);
			map.setCustomLabel(conf.getScenario().toString()+" Hazard "+size+"/"+maxSize+" Sims");
			map.setTopoResolution(topo_slope_res);
			map.setLogPlot(logPlot);
			map.setDpi(300);
			map.setXyzFileName("base_map.xyz");
			map.setCustomScaleMin(customMin);
			map.setCustomScaleMax(customMax);
			
			String metadata = "isProbAt_IML: " + isProbAt_IML + "\n" +
							"val: " + val + "\n" +
							"imTypeID: " + imTypeID + "\n";
			
			File outputFile = new File(outputDir, "converge_hazard_"+conf.getScenario().name()+"_"+sizeStr+".png");
			
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
			
			// now ratio
			outputFile = new File(outputDir, "converge_ratio_"+conf.getScenario().name()+"_"+sizeStr+".png");
			createRatioMap(xyz, finalMap, conf.getScenario().toString()+" "+size+"/"+maxSize+" Sims Ratio", outputFile, -1d, 1d);
		}
	}
	
	private void writeDistanceGainPlot(GeoDataSet numerator, GeoDataSet denominator, FaultSystemSolution sol,
			FaultModels fm, File outputDir)
			throws IOException {
		ETAS_CyberShake_Scenarios scenario = conf.getScenario();
		if (!scenario.isETAS())
			return;
		RuptureSurface surf = null;
		Location triggerLoc = null;
		if (scenario.getTriggerRupIndex(fm) >= 0)
			surf = sol.getRupSet().getSurfaceForRupupture(scenario.getTriggerRupIndex(fm), 1d, false);
		else
			triggerLoc = scenario.getTriggerLoc();
		GeoDataSet xyz = ProbGainCalc.calcProbGain(denominator, numerator);
		
		DefaultXY_DataSet xy = new DefaultXY_DataSet();
		
		for (Location loc : xyz.getLocationList()) {
			double gain = xyz.get(loc);
			
			double dist;
			if (surf == null)
				dist = LocationUtils.horzDistance(loc, triggerLoc);
			else
				dist = surf.getDistanceJB(loc); // dist JB is distance to surface projection
			
			xy.set(dist, gain);
		}
		
		List<PlotElement> funcs = Lists.newArrayList();
		List<PlotCurveCharacterstics> chars = Lists.newArrayList();
		
		funcs.add(xy);
		chars.add(new PlotCurveCharacterstics(PlotSymbol.CROSS, 3f, Color.BLACK));
		
		PlotSpec spec = new PlotSpec(funcs, chars, scenario.toString(), "Trigger Rup/Site Dist (km)",
				(float)val+"G 3s SA Gain");
		
		HeadlessGraphPanel gp = new HeadlessGraphPanel();
		gp.setTickLabelFontSize(18);
		gp.setAxisLabelFontSize(20);
		gp.setPlotLabelFontSize(21);
		gp.setBackgroundColor(Color.WHITE);
		
//		gp.setXLog(true);
//		gp.setYLog(true);
//		gp.setUserBounds(1e-2, 1e0, 1e-7, 1e-2);
		gp.drawGraphPanel(spec);
		gp.getCartPanel().setSize(1000, 800);
		gp.saveAsPDF(new File(outputDir, "dist_gain_"+scenario.name()+".pdf").getAbsolutePath());
		gp.saveAsPNG(new File(outputDir, "dist_gain_"+scenario.name()+".png").getAbsolutePath());
	}

	public static void main(String[] args) throws IOException, DocumentException {
		try {
			int refDatasetID = ref_dataset_ID;
			
//			ETAS_Cybershake_TimeSpans timeSpan = ETAS_Cybershake_TimeSpans.ONE_WEEK;
			ETAS_Cybershake_TimeSpans timeSpan = ETAS_Cybershake_TimeSpans.ONE_DAY;
			FaultModels fm = FaultModels.FM3_1;
			
			File mainDir = new File(new File("/home/kevin/OpenSHA/UCERF3/cybershake_etas"),
					"results_"+timeSpan.name().toLowerCase());
			if (!mainDir.exists())
				mainDir.mkdir();
			
			boolean debugSiteCalcOnly = false;
			boolean makeMaps = true; // change
			boolean plotShakemaps = false; // change
			boolean doConvergence = false;
			boolean doNormalizedCalcs = false;
			boolean doDistGain = false; // only when makeMaps=true
			boolean doGMPE = false;
			boolean doAllRV_Equal = false; // change
			publish_curves = false;
			force_recalc = true;
			
			double normRate = 0.01;
			
			int numShakemaps = 10;
			int convergeInterval = 10000;
			
			File mapDir = new File(mainDir, "maps_"+(float)val);
			if (!mapDir.exists())
				mapDir.mkdir();
			File curveDir = new File(mainDir, "curves");
			if (!curveDir.exists())
				curveDir.mkdir();
			File shakemapDir = new File(mainDir, "shakemaps_"+(float)val);
			if (plotShakemaps && !shakemapDir.exists())
				shakemapDir.mkdir();
			File convergeCurveDir = new File(mainDir, "converg_curves");
			File convergeMapDir = new File(mainDir, "converg_maps_"+(float)val);
			if (doConvergence) {
				if (!convergeCurveDir.exists())
					convergeCurveDir.mkdir();
				if (!convergeMapDir.exists())
					convergeMapDir.mkdir();
			}
			File normCurvesDir = new File(mainDir, "norm_curves");
			File normMapDir = new File(mainDir, "norm_maps_"+(float)val);
			if (doNormalizedCalcs) {
				if (!normCurvesDir.exists())
					normCurvesDir.mkdir();
				if (!normMapDir.exists())
					normMapDir.mkdir();
			}
			File mfdDir = new File(mainDir, "mfds");
			if (!mfdDir.exists())
				mfdDir.mkdir();
			File distGainDir = new File(mainDir, "dist_gain");
			if (doDistGain && !distGainDir.exists())
				distGainDir.mkdir();
			File gmpeMapDir = new File(mainDir, "gmpe_maps_"+(float)val);
			if (doGMPE && !gmpeMapDir.exists())
				gmpeMapDir.mkdir();
			File allRV_EqualMapDir = new File(mainDir, "all_rvs_equal_maps_"+(float)val);
			File allRV_EqualCurveDir = new File(mainDir, "all_rvs_equal_curves");
			if (doAllRV_Equal) {
				if (!allRV_EqualMapDir.exists())
					allRV_EqualMapDir.mkdir();
				if (!allRV_EqualCurveDir.exists())
					allRV_EqualCurveDir.mkdir();
			}
			
			ETASModProbConfig conf;
			ETASCurveCalc calc;
			
			FaultSystemSolution sol = FaultSystemIO.loadSol(
					new File("/home/kevin/OpenSHA/UCERF3/cybershake_etas/ucerf2_mapped_sol.zip"));
			File mappingFile = new File("/home/kevin/OpenSHA/UCERF3/cybershake_etas/u2_mapped_mappings.csv");
//					new File("/home/kevin/OpenSHA/UCERF3/cybershake_etas/ucerf2_u3inverted_sol.zip"));
//			File mappingFile = new File("/home/kevin/OpenSHA/UCERF3/cybershake_etas/u3_inverted_mappings.csv");
			
			String[] curveSites = { "STNI", "S157", "S716", "S323", "S361", "MRSD", "SBSM" };
//			String[] curveSites = null;
			List<ETASCurveCalc> calcs = Lists.newArrayList();
			List<String> filePrefixes = Lists.newArrayList();
			List<Color> colors = Lists.newArrayList();
			List<ArbDiscrGeoDataSet> maps = Lists.newArrayList();
			
			conf = new ETASModProbConfig(ETAS_CyberShake_Scenarios.PARKFIELD, timeSpan, sol,
					new File[] {new File("/home/kevin/OpenSHA/UCERF3/cybershake_etas/sims/"
//							+ "2014_12_01-parkfield-combined.zip")},
							+ "2015_03_23-u2mapped-parkfield-round1/results.zip")},
//							+ "2015_03_23-u3inverted-parkfield-round1/results.zip")},
					mappingFile);
			calc = new ETASCurveCalc(conf, 21, refDatasetID);
			calcs.add(calc);
			filePrefixes.add("parkfield");
			colors.add(Color.RED);
			if (!debugSiteCalcOnly && makeMaps)
				maps.add(calc.calcMap(new File(mapDir, "parkfield_hazard.png")));
			
			conf = new ETASModProbConfig(ETAS_CyberShake_Scenarios.BOMBAY_BEACH_M6, timeSpan, sol,
					new File[] {new File("/home/kevin/OpenSHA/UCERF3/cybershake_etas/sims/"
//							+ "2014_12_01-bombay_beach_m6-combined.zip")},
							+ "2015_03_23-u2mapped-bombay_beach_m6-round1/results.zip")},
//							+ "2015_03_23-u3inverted-bombay_beach_m6-round1/results.zip")},
					mappingFile);
			calc = new ETASCurveCalc(conf, 21, refDatasetID);
			calcs.add(calc);
			filePrefixes.add("bombay");
			colors.add(Color.ORANGE);
			if (!debugSiteCalcOnly && makeMaps)
				maps.add(calc.calcMap(new File(mapDir, "bombay_hazard.png")));
			
//			conf = new ETASModProbConfig(ETAS_CyberShake_Scenarios.BOMBAY_BEACH_BRAWLEY_FAULT_M6, timeSpan, sol,
//					new File[] {new File("/home/kevin/OpenSHA/UCERF3/cybershake_etas/sims/"
//							+ "2014_12_01-bombay_beach_brawley_fault_m6-combined.zip")},
//					mappingFile);
//			calc = new ETASCurveCalc(conf, 21, refDatasetID);
//			calcs.add(calc);
//			filePrefixes.add("bombay_flt");
//			colors.add(Color.ORANGE);
//			if (!debugSiteCalcOnly && makeMaps)
//				maps.add(calc.calcMap(new File(mapDir, "bombay_flt_hazard.png")));
			
//			conf = new ETASModProbConfig(ETAS_CyberShake_Scenarios.MOJAVE_S_POINT_M6, timeSpan, sol,
//					new File[] {new File("/home/kevin/OpenSHA/UCERF3/cybershake_etas/sims/"
//							+ "2014_12_01-mojave_s_point_m6-combined.zip")},
//					mappingFile);
//			calc = new ETASCurveCalc(conf, 21, refDatasetID);
//			calcs.add(calc);
//			filePrefixes.add("mojave");
//			colors.add(Color.MAGENTA);
//			if (!debugSiteCalcOnly && makeMaps)
//				maps.add(calc.calcMap(new File(mapDir, "mojave_hazard.png")));
			
//			conf = new ETASModProbConfig(ETAS_CyberShake_Scenarios.TEST_NEGLIGABLE, timeSpan, sol,
//					new File("/home/kevin/OpenSHA/UCERF3/cybershake_etas/sims/2014_08_01-parkfield/results.zip"),
//					mappingFile);
//			publish_curves = false;
//			calc = new ETASCurveCalc(conf, 21, refDatasetID);
//			calcs.add(calc);
//			filePrefixes.add("test_negligable");
//			colors.add(Color.GRAY);
//			if (!debugSiteCalcOnly && makeMaps)
//				maps.add(calc.calcMap(new File(mapDir, "test_negligable_hazard.png")));
			
//			conf = new ETASModProbConfig(ETAS_CyberShake_Scenarios.TEST_BOMBAY_M6_SUBSET, timeSpan, sol,
//					new File("/home/kevin/OpenSHA/UCERF3/cybershake_etas/sims/2014_07_31-bombay_beach_m6/results.zip"),
//					mappingFile);
//			publish_curves = false;
//			calc = new ETASCurveCalc(conf, 21, refDatasetID);
//			ArbDiscrGeoDataSet modMap = calc.calcMap(new File(mapDir, "test_bombay_subset_hazard.png"));
			
//			conf = new ETASModProbConfig(ETAS_CyberShake_Scenarios.TEST_BOMBAY_M6_SUBSET_FIRST, timeSpan, sol,
//					new File[] {new File("/home/kevin/OpenSHA/UCERF3/cybershake_etas/sims/2014_09_02-bombay_beach_m6-nospont/results.zip")},
//					mappingFile);
//			publish_curves = false;
//			calc = new ETASCurveCalc(conf, 21, refDatasetID);
//			calcs.add(calc);
//			filePrefixes.add("bombay_first");
//			colors.add(Color.MAGENTA);
//			if (!debugSiteCalcOnly && makeMaps)
//				maps.add(calc.calcMap(new File(mapDir, "test_bombay_first_hazard.png")));
			
//			conf = new ETASModProbConfig(ETAS_CyberShake_Scenarios.TEST_BOMBAY_M6_SUBSET_SECOND, timeSpan, sol,
//					new File[] {new File("/home/kevin/OpenSHA/UCERF3/cybershake_etas/sims/2014_09_02-bombay_beach_m6-nospont/results.zip")},
//					mappingFile);
//			publish_curves = false;
//			calc = new ETASCurveCalc(conf, 21, refDatasetID);
//			calcs.add(calc);
//			filePrefixes.add("bombay_second");
//			colors.add(Color.PINK);
//			if (!debugSiteCalcOnly && makeMaps)
//				maps.add(calc.calcMap(new File(mapDir, "test_bombay_second_hazard.png")));
			
			if (curveSites != null && curveSites.length > 0) {
				FaultSystemSolution timeDepSol = FaultSystemIO.loadSol(
						new File("/home/kevin/OpenSHA/UCERF3/cybershake_etas/ucerf2_mapped_timedep_sol.zip"));
				conf = new ETASModProbConfig(ETAS_CyberShake_Scenarios.MAPPED_UCERF2_TIMEDEP,
						timeSpan, timeDepSol,
						new File[0],
						new File("/home/kevin/OpenSHA/UCERF3/cybershake_etas/u2_mapped_mappings.csv"));
				calc = new ETASCurveCalc(conf, 21, refDatasetID);
				calcs.add(calc);
				filePrefixes.add("ucerf2_dep");
				colors.add(Color.GREEN);
				
				if (!debugSiteCalcOnly && makeMaps)
					maps.add(calc.calcMap(new File(mapDir, "ucerf2_dep_hazard.png")));
			}
			
			conf = new ETASModProbConfig(ETAS_CyberShake_Scenarios.MAPPED_UCERF2, timeSpan, sol,
					new File[0],
					mappingFile);
			calc = new ETASCurveCalc(conf, 21, refDatasetID);
			calcs.add(calc);
			filePrefixes.add("ucerf2");
			colors.add(Color.BLUE);
			
			List<String> names = Lists.newArrayList();
			for (ETASCurveCalc myCalc : calcs)
				names.add(myCalc.conf.getScenario().toString());
			
			ETASCurveCalc refCalc = calc;
			
			if (debugSiteCalcOnly) {
				if (curveSites != null && curveSites.length > 0)
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
				ArbDiscrGeoDataSet baseMap = refCalc.calcMap(new File(mapDir, "ref_hazard.png"));
				for (int i=0; i<calcs.size() && makeMaps; i++) {
					if (calcs.get(i) == refCalc)
						continue;
					createRatioMap(maps.get(i), baseMap, names.get(i)+" Gain",
							new File(mapDir, filePrefixes.get(i)+"_gain.png"));
					if (doDistGain)
						calcs.get(i).writeDistanceGainPlot(maps.get(i), baseMap, sol, fm, distGainDir);
				}
			}
			
//			createRatioMap(modMap, baseMap, "Bombay Beach Prob Gain", new File(outputDir, "bombay_gain.png"));
//			createRatioMap(modMap, baseMap, gainName, gainFile);
//			createRatioMap(modMap, baseMap, "Test Negligable Prob Gain", new File(outputDir, "test_negligable_gain.png"));
//			createRatioMap(modMap, baseMap, "Test Bombay 50% Subset Prob Gain", new File(outputDir, "test_bombay_subset_gain.png"));
			
			if (debugSiteCalcOnly || makeMaps)
				for (String siteName : curveSites)
					plotComparisonCurves(calcs, names, colors, siteName, curveDir);
			
			if (doConvergence && !debugSiteCalcOnly) {
				for (int i=0; i<calcs.size(); i++) {
					Map<Integer, Map<CybershakeSite, DiscretizedFunc>> sizeCurvesMap =
							calcs.get(i).calcConvergeCurves(convergeInterval, false);
					if (sizeCurvesMap == null)
						continue;
					calcs.get(i).writeConvergenceLinesPlot(convergeCurveDir, sizeCurvesMap);
					calcs.get(i).writeConvergenceMaps(convergeMapDir, sizeCurvesMap);
				}
			}
			
			if (doAllRV_Equal) {
				List<ArbDiscrGeoDataSet> equalMaps = Lists.newArrayList();
				for (int i=0; i<calcs.size(); i++) {
					ETASCurveCalc myCalc = calcs.get(i);
					myCalc.conf.setTriggerAllHyposEqually(true);
					myCalc.prevCurves = null;
					myCalc.calc = null;
					if (!debugSiteCalcOnly && makeMaps)
						equalMaps.add(myCalc.calcMap(new File(allRV_EqualMapDir,
								filePrefixes.get(i)+"_rv_equal_hazard.png")));
				}
				// create ratio maps
				if (makeMaps) {
					ArbDiscrGeoDataSet baseMap = refCalc.calcMap(new File(allRV_EqualMapDir, "ref_hazard.png"));
					for (int i=0; i<calcs.size(); i++)
						createRatioMap(equalMaps.get(i), baseMap, names.get(i)+" Gain",
								new File(allRV_EqualMapDir, filePrefixes.get(i)+"_gain.png"));
				}
				// curves
				if (debugSiteCalcOnly || makeMaps)
					for (String siteName : curveSites)
						plotComparisonCurves(calcs, names, colors, siteName, allRV_EqualCurveDir);
				for (int i=0; i<calcs.size(); i++) {
					ETASCurveCalc myCalc = calcs.get(i);
					myCalc.conf.setTriggerAllHyposEqually(false);
					myCalc.prevCurves = null;
					myCalc.calc = null;
				}
			}
			
			if (doNormalizedCalcs) {
				List<ArbDiscrGeoDataSet> normMaps = Lists.newArrayList();
				for (int i=0; i<calcs.size(); i++) {
					ETASCurveCalc myCalc = calcs.get(i);
					myCalc.conf.setNormTriggerRate(normRate);
					myCalc.prevCurves = null;
					myCalc.calc = null;
					if (!debugSiteCalcOnly && makeMaps)
						normMaps.add(myCalc.calcMap(new File(normMapDir, filePrefixes.get(i)+"_norm_hazard.png")));
				}
				// create ratio maps
				if (makeMaps) {
					ArbDiscrGeoDataSet baseMap = refCalc.calcMap(new File(normMapDir, "ref_hazard.png"));
					for (int i=0; i<calcs.size(); i++)
						createRatioMap(normMaps.get(i), baseMap, names.get(i)+" Gain",
								new File(normMapDir, filePrefixes.get(i)+"_gain.png"));
				}
				// curves
				if (debugSiteCalcOnly || makeMaps)
					for (String siteName : curveSites)
						plotComparisonCurves(calcs, names, colors, siteName, normCurvesDir);
			}
			
			for (ETASCurveCalc myCalc : calcs) {
				conf = myCalc.conf;
				if (conf.getScenario().isETAS())
					conf.writeTriggerMFD(mfdDir, conf.getScenario().name().toLowerCase()+"_trigger_mfd");
			}
			
			if (doGMPE) {
				ArbDiscrGeoDataSet baseMap = refCalc.calcGMPEMap(new File(gmpeMapDir, "ref_hazard.png"));
				for (int i=0; i<calcs.size(); i++) {
					ArbDiscrGeoDataSet newMap = calcs.get(i).calcGMPEMap(
							new File(gmpeMapDir, filePrefixes.get(i)+"_gmpe_hazard.png"));
					createRatioMap(newMap, baseMap, names.get(i)+" Gain",
							new File(gmpeMapDir, filePrefixes.get(i)+"_gmpe_gain.png"));
				}
			}
			
			if (plotShakemaps && !debugSiteCalcOnly) {
				for (int i=0; i<calcs.size(); i++)
					calcs.get(i).plotMostTriggeredRVs(shakemapDir, filePrefixes.get(i), numShakemaps, doGMPE);
			}
			
			System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

}
