package org.opensha.sha.cybershake.calc;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.jfree.data.Range;
import org.opensha.commons.data.Site;
import org.opensha.commons.data.function.DiscretizedFunc;
import org.opensha.commons.data.siteData.OrderedSiteDataProviderList;
import org.opensha.commons.exceptions.IMRException;
import org.opensha.commons.exceptions.ParameterException;
import org.opensha.commons.geo.Location;
import org.opensha.commons.gui.plot.GraphWindow;
import org.opensha.commons.gui.plot.HeadlessGraphPanel;
import org.opensha.commons.gui.plot.PlotCurveCharacterstics;
import org.opensha.commons.gui.plot.PlotLineType;
import org.opensha.commons.gui.plot.PlotSpec;
import org.opensha.commons.util.ComparablePairing;
import org.opensha.sha.calc.HazardCurveCalculator;
import org.opensha.sha.calc.hazardMap.HazardCurveSetCalculator;
import org.opensha.sha.cybershake.db.CachedPeakAmplitudesFromDB;
import org.opensha.sha.cybershake.db.CybershakeIM;
import org.opensha.sha.cybershake.db.CybershakeIM.CyberShakeComponent;
import org.opensha.sha.cybershake.db.CybershakeIM.IMType;
import org.opensha.sha.cybershake.db.CybershakeRun;
import org.opensha.sha.cybershake.db.CybershakeSite;
import org.opensha.sha.cybershake.db.CybershakeSiteInfo2DB;
import org.opensha.sha.cybershake.db.Cybershake_OpenSHA_DBApplication;
import org.opensha.sha.cybershake.db.DBAccess;
import org.opensha.sha.cybershake.db.MeanUCERF2_ToDB;
import org.opensha.sha.cybershake.db.Runs2DB;
import org.opensha.sha.cybershake.db.SiteInfo2DB;
import org.opensha.sha.cybershake.plot.HazardCurvePlotter;
import org.opensha.sha.earthquake.AbstractERF;
import org.opensha.sha.earthquake.ERF;
import org.opensha.sha.earthquake.ERFTestSubset;
import org.opensha.sha.earthquake.EqkRupture;
import org.opensha.sha.earthquake.ProbEqkSource;
import org.opensha.sha.gui.infoTools.IMT_Info;
import org.opensha.sha.imr.ScalarIMR;
import org.opensha.sha.imr.attenRelImpl.NGAWest_2014_Averaged_AttenRel;
import org.opensha.sha.imr.param.IntensityMeasureParams.SA_Param;
import org.opensha.sha.imr.param.OtherParams.SigmaTruncLevelParam;
import org.opensha.sha.imr.param.OtherParams.SigmaTruncTypeParam;
import org.opensha.sha.util.SiteTranslator;
import org.opensha.sra.rtgm.RTGM;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;

public class HazardDecompositionPlotter {
	
	public static void main(String[] args) throws IOException {
		List<Integer> runIDs = Lists.newArrayList(2657, 3037, 2722, 3022, 3030, 3027, 2636, 2638,
				2660, 2703, 3504, 2988, 2965, 3007);
//		List<Integer> runIDs = Lists.newArrayList(2988, 2965, 3007);
		File baseDir = new File("/home/kevin/CyberShake/MCER");
		File outputDir = new File(baseDir, "hazard_decomposition");
		if (!outputDir.exists())
			outputDir.mkdir();
		File cacheDir = new File(baseDir, ".amps_cache");
		if (!cacheDir.exists())
			cacheDir.mkdir();
		
		// NGA2 without Idriss
		CachedGMPE gmpe = new CachedGMPE();
//		ScalarIMR gmpe = new NGAWest_2014_Averaged_AttenRel(null, false);
		gmpe.setParamDefaults();
		gmpe.getParameter(SigmaTruncTypeParam.NAME).setValue(SigmaTruncTypeParam.SIGMA_TRUNC_TYPE_1SIDED);
		gmpe.getParameter(SigmaTruncLevelParam.NAME).setValue(3d);
		gmpe.setIntensityMeasure(SA_Param.NAME);
		String periodFileStr = "3sec";
		SA_Param.setPeriodInSA_Param(gmpe.getIntensityMeasure(), 3d);
		String xAxisLabel = "3sec SA";
		CybershakeIM imType = new CybershakeIM(146, IMType.SA, 3d, null, CyberShakeComponent.RotD100);
//		String periodFileStr = "7.5sec";
//		SA_Param.setPeriodInSA_Param(gmpe.getIntensityMeasure(), 7.5d);
//		String xAxisLabel = "7.5sec SA";
//		CybershakeIM imType = new CybershakeIM(138, IMType.SA, 7.5d, null, CyberShakeComponent.RotD100);
		
		AbstractERF erf = MeanUCERF2_ToDB.createUCERF2ERF();
		
		Map<String, List<Integer>> combSourceMap = getCombinedSources(erf);
		
		DBAccess db = Cybershake_OpenSHA_DBApplication.db;
		Runs2DB run2db = new Runs2DB(db); 
		CybershakeSiteInfo2DB csSite2db = new CybershakeSiteInfo2DB(db);
		SiteInfo2DB site2db = new SiteInfo2DB(db);
		HazardCurveComputation csCalc = new HazardCurveComputation(db);
		CachedPeakAmplitudesFromDB amps2db = new CachedPeakAmplitudesFromDB(db, cacheDir, erf);
		csCalc.setPeakAmpsAccessor(amps2db);
		
		HazardCurveCalculator calc = new HazardCurveCalculator();
		
		DiscretizedFunc xVals = new IMT_Info().getDefaultHazardCurve(gmpe.getIntensityMeasure());
		
		SiteTranslator trans = new SiteTranslator();
		
		int numToInclude = 10;
		
		HazardCurvePlotter.SCALE_PRINT_SUCCESS = false;
		
		for (int runID : runIDs) {
			if (gmpe instanceof CachedGMPE)
				((CachedGMPE)gmpe).clearCache();
			
			CybershakeRun run = run2db.getRun(runID);
			int siteID = run.getSiteID();
			CybershakeSite csSite = csSite2db.getSiteFromDB(siteID);
			Location loc = csSite.createLocation();
			
			System.out.println("Doing "+csSite.short_name);
			
			Site site = new Site(loc);
			
			OrderedSiteDataProviderList providers =
					HazardCurvePlotter.createProviders(run.getVelModelID());
			
			trans.setAllSiteParams(gmpe, providers.getBestAvailableData(loc));
			site.addParameterList(gmpe.getSiteParams());
			site.setName(csSite.short_name);
			
			String prefix = csSite.short_name+"_decomposition_"+periodFileStr;
			
			System.out.println("Doing CyberShake for "+site.getName());
			doCyberShake(csCalc, site2db, run, csSite, imType, xVals, outputDir, numToInclude,
					xAxisLabel, prefix+"_CyberShake", erf, combSourceMap);
			
			System.out.println("Doing GMPE for "+site.getName());
			doGMPE(calc, site, gmpe, erf, xVals, outputDir, numToInclude,
					xAxisLabel, prefix+"_NGA2", imType, combSourceMap);
		}
		
		db.destroy();
		System.exit(0);
	}
	
	private static void doGMPE(HazardCurveCalculator calc, Site site, ScalarIMR imr,
			AbstractERF erf, DiscretizedFunc xVals, File outputDir, int numToInclude,
			String xAxisLabel, String prefix, CybershakeIM imType,
			Map<String, List<Integer>> combSourceMap) throws IOException {
		DiscretizedFunc logXVals = HazardCurveSetCalculator.getLogFunction(xVals.deepClone());
		DiscretizedFunc totalHazard = calc.getAnnualizedRates(
				HazardCurveSetCalculator.unLogFunction(xVals,
						calc.getHazardCurve(logXVals.deepClone(), site, imr, erf)), 1d);
		totalHazard = HazardCurvePlotter.getScaledCurveForComponent(imr, imType, totalHazard);
		totalHazard.setName("Total Hazard");
		
		List<DiscretizedFunc> sourceFuncs = Lists.newArrayList();
		List<Double> sourceRTGMContributions = Lists.newArrayList();
		
		double totRTGM = calcRTGM(totalHazard);
		
		for (String sourceName : combSourceMap.keySet()) {
			List<Integer> ids = combSourceMap.get(sourceName);
			HashSet<Integer> withinIDs = new HashSet<Integer>();
			for (int sourceID : ids) {
				ProbEqkSource source = erf.getSource(sourceID);
				if (source.getMinDistance(site) > 200d)
					continue;
				withinIDs.add(sourceID);
			}
			if (withinIDs.isEmpty())
				continue;
			
//			System.out.println("Source "+sourceID);
			ERFTestSubset subset = new ERFTestSubset(erf);
			for (int sourceID : withinIDs)
				subset.includeSource(sourceID);
			subset.updateForecast();
			Preconditions.checkState(subset.getNumSources() == withinIDs.size());
			DiscretizedFunc srcHazard = calc.getAnnualizedRates(
					HazardCurveSetCalculator.unLogFunction(xVals,
							calc.getHazardCurve(logXVals.deepClone(), site, imr, subset)), 1d);
			srcHazard = HazardCurvePlotter.getScaledCurveForComponent(imr, imType, srcHazard);
			sourceFuncs.add(srcHazard);
			
			// now all sources except this one for disagg
			subset = new ERFTestSubset(erf);
			for (int sourceID=0; sourceID<erf.getNumSources(); sourceID++)
				if (!withinIDs.contains(sourceID))
					subset.includeSource(sourceID);
			subset.updateForecast();
			Preconditions.checkState(subset.getNumSources() == erf.getNumSources()-withinIDs.size());
			DiscretizedFunc srcWithoutHazard = calc.getAnnualizedRates(
					HazardCurveSetCalculator.unLogFunction(xVals,
							calc.getHazardCurve(logXVals.deepClone(), site, imr, subset)), 1d);
			srcWithoutHazard = HazardCurvePlotter.getScaledCurveForComponent(imr, imType, srcWithoutHazard);
			
			double withoutRTGM = calcRTGM(srcWithoutHazard);
			double deltaRTGM = totRTGM - withoutRTGM;
//			Preconditions.checkState(deltaRTGM >= 0);
			sourceRTGMContributions.add(deltaRTGM);
			
			srcHazard.setName(sourceName);
			srcHazard.setInfo("RTGM Contribution: "+(float)totRTGM+" - "+(float)withoutRTGM+" = "+(float)deltaRTGM);
//			System.out.println(source.getName()+" "+srcHazard.getInfo());
		}
		
//		System.out.println("Num above zero: "+numAboveZero);
		
		List<ComparablePairing<Double, DiscretizedFunc>> pairings =
				ComparablePairing.build(sourceRTGMContributions, sourceFuncs);
		Collections.sort(pairings);
		Collections.reverse(pairings);
		
		List<DiscretizedFunc> funcs = Lists.newArrayList();
		List<Color> colors = GraphWindow.generateDefaultColors();
		List<PlotCurveCharacterstics> chars = Lists.newArrayList();
		
		funcs.add(totalHazard);
		chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, colors.get((funcs.size()-1) % colors.size())));
		
		for (int i=0; i<numToInclude; i++) {
			funcs.add(pairings.get(i).getData());
			chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 1f, colors.get((funcs.size()-1) % colors.size())));
		}
		
		PlotSpec spec = new PlotSpec(funcs, chars, site.getName()+" NGA2 Hazard Decomposition",
				xAxisLabel, "Annual Rate Of Exceedance");
		spec.setLegendVisible(true);
		
		HeadlessGraphPanel gp = new HeadlessGraphPanel();
		gp.setBackgroundColor(Color.WHITE);
//		gp.setRenderingOrder(DatasetRenderingOrder.REVERSE);
		gp.setTickLabelFontSize(18);
		gp.setAxisLabelFontSize(20);
		gp.setPlotLabelFontSize(21);
		
		gp.drawGraphPanel(spec, true, true, new Range(3e-3, 3e0), new Range(5e-7, 5e-1));
		gp.getCartPanel().setSize(1000, 800);
		gp.setVisible(true);
		
		gp.validate();
		gp.repaint();
		
		File file = new File(outputDir, prefix);
		gp.saveAsPDF(file.getAbsolutePath()+".pdf");
		gp.saveAsPNG(file.getAbsolutePath()+".png");
	}
	
	private static void doCyberShake(HazardCurveComputation calc, SiteInfo2DB site2db,
			CybershakeRun run, CybershakeSite site, CybershakeIM imType, DiscretizedFunc xVals,
			File outputDir, int numToInclude, String xAxisLabel, String prefix, AbstractERF erf,
			Map<String, List<Integer>> combSourceMap) throws IOException {
		List<Double> xValsList = Lists.newArrayList();
		for (Point2D pt : xVals)
			xValsList.add(pt.getX());
		
		HazardCurveCalculator gmpeCalc = new HazardCurveCalculator();
		
		DiscretizedFunc totalHazard = gmpeCalc.getAnnualizedRates(
				calc.computeHazardCurve(xValsList, run, imType), 1d);
		totalHazard.setName("Total Hazard");
		
		List<DiscretizedFunc> sourceFuncs = Lists.newArrayList();
		List<Double> sourceRTGMContributions = Lists.newArrayList();
		
		double totRTGM = calcRTGM(totalHazard);
		
		HashSet<Integer> sourceSet = new HashSet<Integer>(
				site2db.getSrcIdsForSite(run.getSiteID(), run.getERFID()));
		
		for (String sourceName : combSourceMap.keySet()) {
			List<Integer> ids = combSourceMap.get(sourceName);
			HashSet<Integer> withinIDs = new HashSet<Integer>();
			for (int sourceID : ids) {
				if (!sourceSet.contains(sourceID))
					continue;
				withinIDs.add(sourceID);
			}
			if (withinIDs.isEmpty())
				continue;
			
//			System.out.println("CS Source "+sourceID);
			DiscretizedFunc srcHazard = gmpeCalc.getAnnualizedRates(
					calc.computeHazardCurve(xValsList, run, imType, Lists.newArrayList(withinIDs)), 1d);
			sourceFuncs.add(srcHazard);
			
			// now all sources except this one for disagg
			List<Integer> sourcesWithout = Lists.newArrayList();
			for (int sourceID=0; sourceID<erf.getNumSources(); sourceID++)
				if (sourceSet.contains(sourceID) && !withinIDs.contains(sourceID))
					sourcesWithout.add(sourceID);
			Preconditions.checkState(sourcesWithout.size() == sourceSet.size()-withinIDs.size());
			DiscretizedFunc srcWithoutHazard = gmpeCalc.getAnnualizedRates(
					calc.computeHazardCurve(xValsList, run, imType, sourcesWithout), 1d);
			
			double withoutRTGM = calcRTGM(srcWithoutHazard);
			double deltaRTGM = totRTGM - withoutRTGM;
			Preconditions.checkState(deltaRTGM >= 0);
			sourceRTGMContributions.add(deltaRTGM);
			
			srcHazard.setName(sourceName);
			srcHazard.setInfo("CS RTGM Contribution: "+(float)totRTGM+" - "+(float)withoutRTGM+" = "+(float)deltaRTGM);
//			System.out.println(source.getName()+" "+srcHazard.getInfo());
		}
		
//		System.out.println("Num above zero: "+numAboveZero);
		
		List<ComparablePairing<Double, DiscretizedFunc>> pairings =
				ComparablePairing.build(sourceRTGMContributions, sourceFuncs);
		Collections.sort(pairings);
		Collections.reverse(pairings);
		
		List<DiscretizedFunc> funcs = Lists.newArrayList();
		List<Color> colors = GraphWindow.generateDefaultColors();
		List<PlotCurveCharacterstics> chars = Lists.newArrayList();
		
		funcs.add(totalHazard);
		chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, colors.get((funcs.size()-1) % colors.size())));
		
		for (int i=0; i<numToInclude; i++) {
			funcs.add(pairings.get(i).getData());
			chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 1f, colors.get((funcs.size()-1) % colors.size())));
		}
		
		PlotSpec spec = new PlotSpec(funcs, chars, site.short_name+" CyberShake Hazard Decomposition",
				xAxisLabel, "Annual Rate Of Exceedance");
		spec.setLegendVisible(true);
		
		HeadlessGraphPanel gp = new HeadlessGraphPanel();
		gp.setBackgroundColor(Color.WHITE);
//		gp.setRenderingOrder(DatasetRenderingOrder.REVERSE);
		gp.setTickLabelFontSize(18);
		gp.setAxisLabelFontSize(20);
		gp.setPlotLabelFontSize(21);
		
		gp.drawGraphPanel(spec, true, true, new Range(3e-3, 3e0), new Range(5e-7, 5e-1));
		gp.getCartPanel().setSize(1000, 800);
		gp.setVisible(true);
		
		gp.validate();
		gp.repaint();
		
		File file = new File(outputDir, prefix);
		gp.saveAsPDF(file.getAbsolutePath()+".pdf");
		gp.saveAsPNG(file.getAbsolutePath()+".png");
	}
	
	private static double calcRTGM(DiscretizedFunc curve) {
		RTGM calc = RTGM.create(curve, null, null);
		try {
			calc.call();
		} catch (RuntimeException e) {
			System.err.println("RTGM Calc failed for Hazard Curve:\n"+curve);
			System.err.flush();
			throw e;
		}
		double rtgm = calc.get();
		Preconditions.checkState(rtgm > 0, "RTGM is not positive");
		return rtgm;
	}
	
	public static class CachedGMPE extends NGAWest_2014_Averaged_AttenRel {
		
		int cacheHits = 0;
		int cacheMisses = 0;
		
		private Table<EqkRupture, Site, DiscretizedFunc> exceedProbsCache;
		
		public CachedGMPE() {
			super(null, false);
			
			exceedProbsCache = HashBasedTable.create();
		}

		@Override
		public synchronized DiscretizedFunc getExceedProbabilities(
				DiscretizedFunc intensityMeasureLevels)
				throws ParameterException {
			EqkRupture rup = getEqkRupture();
			Preconditions.checkNotNull(rup);
			Site site = getSite();
			Preconditions.checkNotNull(site);
			DiscretizedFunc probs = exceedProbsCache.get(rup, site);
			if (probs == null) {
				cacheMisses++;
				super.setEqkRupture(rup);
				probs = super.getExceedProbabilities(intensityMeasureLevels).deepClone();
				exceedProbsCache.put(rup, site, probs);
			} else {
				cacheHits++;
			}
			// now fill in passed func
			for (int i=0; i<intensityMeasureLevels.size(); i++)
				intensityMeasureLevels.set(i, probs.getY(i));
			return intensityMeasureLevels;
		}
		
		@Override
		public double getMean() {
			throw new UnsupportedOperationException();
		}

		@Override
		public double getStdDev() {
			throw new UnsupportedOperationException();
		}

		@Override
		public double getExceedProbability() throws ParameterException,
				IMRException {
			throw new UnsupportedOperationException();
		}

		@Override
		protected double getExceedProbability(double mean, double stdDev,
				double iml) throws ParameterException, IMRException {
			throw new UnsupportedOperationException();
		}

		@Override
		public double getExceedProbability(double iml)
				throws ParameterException, IMRException {
			throw new UnsupportedOperationException();
		}

		@Override
		public void setEqkRupture(EqkRupture eqkRupture) {
			this.eqkRupture = eqkRupture;
		}

		public void clearCache() {
			exceedProbsCache.clear();
			cacheHits = 0;
			cacheMisses = 0;
		}
		
	}
	
	private static Map<String, List<Integer>> getCombinedSources(ERF erf) {
		int commonPrefix = 8; // will pass if common prefix this long 
		int distance = 3; // or if lev distance this little and prefix as long as below
		int distPrefix = 6;
		
		Map<String, List<Integer>> map = Maps.newHashMap();
		
		HashSet<Integer> processedSources = new HashSet<Integer>();
		
		for (int sourceID=0; sourceID<erf.getNumSources(); sourceID++) {
			if (processedSources.contains(sourceID))
				continue;
			String name = getStrippedName(erf.getSource(sourceID).getName());
			List<Integer> matches = Lists.newArrayList();
			matches.add(sourceID);
			String commonAllPrefix = null;
			List<String> matchNames = Lists.newArrayList();
			matchNames.add("'"+erf.getSource(sourceID).getName()+"'");
			for (int s=0; s<erf.getNumSources(); s++) {
				if (s == sourceID || processedSources.contains(s))
					continue;
				String name2 = getStrippedName(erf.getSource(s).getName());
				String common = StringUtils.getCommonPrefix(name, name2);
				if (common.length() >= commonPrefix
						|| (common.length() >= distPrefix
							&& StringUtils.getLevenshteinDistance(name, name2) <= distance)) {
					matches.add(s);
					matchNames.add("'"+erf.getSource(s).getName()+"'");
					if (commonAllPrefix == null || common.length() < commonAllPrefix.length())
						commonAllPrefix = common;
				}
			}
			if (commonAllPrefix == null) {
				Preconditions.checkState(matches.size() == 1);
				commonAllPrefix = name;
			}
			String origPrefix = commonAllPrefix;
			commonAllPrefix = commonAllPrefix.trim();
			if (commonAllPrefix.contains("alt"))
				commonAllPrefix = commonAllPrefix.substring(0, commonAllPrefix.indexOf("alt")).trim();
			while (commonAllPrefix.endsWith(",") || commonAllPrefix.endsWith(";")
					|| commonAllPrefix.endsWith("("))
				commonAllPrefix = commonAllPrefix.substring(0, commonAllPrefix.length()-1);
			
			Preconditions.checkState(commonAllPrefix.length() > 2,
					"Common prefix too short: '"+commonAllPrefix+"', orig: '"+origPrefix+"'");
			map.put(commonAllPrefix, matches);
			processedSources.addAll(matches);
			
			System.out.println(commonAllPrefix+": "+Joiner.on(", ").join(matchNames));
		}
		
		return map;
	}
	
	private static String getStrippedName(String name) {
		name = name.trim();
		if (StringUtils.isNumeric(name.substring(0,1)) || name.startsWith("NV"))
			name = name.substring(name.indexOf(" ")+1);
		name = name.replaceAll("-", " ");
		if (name.toLowerCase().startsWith("eastern"))
			name = name.substring("eastern".length()).trim();
		if (name.toLowerCase().startsWith("western"))
			name = name.substring("western".length()).trim();
		return name;
	}

}
