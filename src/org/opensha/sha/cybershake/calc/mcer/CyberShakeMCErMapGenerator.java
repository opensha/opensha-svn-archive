package org.opensha.sha.cybershake.calc.mcer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.opensha.commons.data.Site;
import org.opensha.commons.data.function.DiscretizedFunc;
import org.opensha.commons.data.region.CaliforniaRegions;
import org.opensha.commons.data.siteData.OrderedSiteDataProviderList;
import org.opensha.commons.data.siteData.SiteDataValue;
import org.opensha.commons.exceptions.GMT_MapException;
import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.Region;
import org.opensha.commons.param.Parameter;
import org.opensha.commons.param.ParameterList;
import org.opensha.commons.util.ClassUtils;
import org.opensha.sha.calc.mcer.AbstractMCErDeterministicCalc;
import org.opensha.sha.calc.mcer.AbstractMCErProbabilisticCalc;
import org.opensha.sha.calc.mcer.CachedMCErDeterministicCalc;
import org.opensha.sha.calc.mcer.CachedMCErProbabilisticCalc;
import org.opensha.sha.calc.mcer.CombinedMultiMCErDeterministicCalc;
import org.opensha.sha.calc.mcer.CombinedMultiMCErProbabilisticCalc;
import org.opensha.sha.calc.mcer.GMPE_MCErDeterministicCalc;
import org.opensha.sha.calc.mcer.GMPE_MCErProbabilisticCalc;
import org.opensha.sha.calc.mcer.MCErCalcUtils;
import org.opensha.sha.calc.mcer.MCErMapGenerator;
import org.opensha.sha.cybershake.HazardCurveFetcher;
import org.opensha.sha.cybershake.calc.RupProbModERF;
import org.opensha.sha.cybershake.calc.RuptureProbabilityModifier;
import org.opensha.sha.cybershake.db.CachedPeakAmplitudesFromDB;
import org.opensha.sha.cybershake.db.CybershakeIM;
import org.opensha.sha.cybershake.db.CybershakeIM.CyberShakeComponent;
import org.opensha.sha.cybershake.db.CybershakeRun;
import org.opensha.sha.cybershake.db.CybershakeSite;
import org.opensha.sha.cybershake.db.Cybershake_OpenSHA_DBApplication;
import org.opensha.sha.cybershake.db.DBAccess;
import org.opensha.sha.cybershake.db.MeanUCERF2_ToDB;
import org.opensha.sha.cybershake.db.Runs2DB;
import org.opensha.sha.cybershake.plot.HazardCurvePlotter;
import org.opensha.sha.earthquake.ERF;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.MeanUCERF2.MeanUCERF2;
import org.opensha.sha.gui.infoTools.IMT_Info;
import org.opensha.sha.imr.AttenRelRef;
import org.opensha.sha.imr.AttenuationRelationship;
import org.opensha.sha.imr.param.IntensityMeasureParams.SA_Param;
import org.opensha.sha.imr.param.OtherParams.Component;
import org.opensha.sha.imr.param.SiteParams.Vs30_Param;
import org.opensha.sha.util.SiteTranslator;

import scratch.UCERF3.erf.FaultSystemSolutionERF;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

public class CyberShakeMCErMapGenerator {
	
	private static final boolean cache_cybershake = true;
	private static final boolean cache_gmpe = true;
	
	public static void calculateMaps(int datasetID, CyberShakeComponent component, double period,
			ERF erf, List<AttenuationRelationship> gmpes, File outputDir) throws IOException, GMT_MapException {
		DBAccess db = Cybershake_OpenSHA_DBApplication.db;
		
		CybershakeIM im = CyberShakeMCErProbabilisticCalc.getIMsForPeriods(db, component, Lists.newArrayList(period)).get(0);
		
		HazardCurveFetcher fetcher = new HazardCurveFetcher(db, datasetID, im.getID());
		CachedPeakAmplitudesFromDB amps2db = new CachedPeakAmplitudesFromDB(db, MCERDataProductsCalc.cacheDir, erf);
		Runs2DB runs2db = new Runs2DB(db);
		
		List<CybershakeSite> csSites = fetcher.getCurveSites();
		List<Integer> runIDs = fetcher.getRunIDs();
		
		Region region = new CaliforniaRegions.CYBERSHAKE_MAP_REGION();
		
		/*
		 * Create site list
		 */
		List<Site> sites = Lists.newArrayList();
		
		// to filter out duplicates
		HashSet<Location> locs = new HashSet<Location>();
		
		OrderedSiteDataProviderList provs = null; // will be created when we have a velocity model ID from a CybershakeRun
		SiteTranslator siteTrans = new SiteTranslator();
		ParameterList siteParams = new ParameterList();
		if (gmpes == null || gmpes.isEmpty()) {
			// need Vs30 for det lower limit even if no GMPEs
			siteParams.addParameter(new Vs30_Param());
		} else {
			for (AttenuationRelationship gmpe : gmpes)
				for (Parameter<?> param : gmpe.getSiteParams())
					if (!siteParams.containsParameter(param.getName()))
						siteParams.addParameter(param);
		}
		
		for (int i=0; i<csSites.size(); i++) {
			CybershakeSite csSite = csSites.get(i);
			if (csSite.type_id == CybershakeSite.TYPE_TEST_SITE)
				continue;
			Location loc = csSite.createLocation();
			if (locs.contains(loc))
				continue;

			int runID = runIDs.get(i);
			CybershakeRun run = runs2db.getRun(runID);
			
			if (provs == null)
				provs = HazardCurvePlotter.createProviders(run.getVelModelID());
			
			CyberShakeSiteRun site = new CyberShakeSiteRun(csSite, run);
			ArrayList<SiteDataValue<?>> datas = provs.getBestAvailableData(site.getLocation());
			for (Parameter<?> param : siteParams) {
				param = (Parameter<?>)param.clone();
				siteTrans.setParameterValue(param, datas);
				site.addParameter(param);
			}
			sites.add(site);
			
			locs.add(loc);
		}
		
		/*
		 * Create calculators
		 */
		
		AbstractMCErDeterministicCalc csDetCalc = new CyberShakeMCErDeterministicCalc(amps2db, erf, component);
		AbstractMCErProbabilisticCalc csProbCalc = new CyberShakeMCErProbabilisticCalc(db, component);
		
		RuptureProbabilityModifier detProbMod = ((CyberShakeMCErDeterministicCalc)csDetCalc).getRupProbMod();
		
		if (cache_cybershake) {
			// now cache
			File cacheDir = new File(outputDir, ".cs_cache");
			Preconditions.checkState(cacheDir.exists() || cacheDir.mkdir());
			
			String cachePrefix = "cs_dataset"+datasetID+"_"+component.name();
			File detCacheFile = new File(cacheDir, cachePrefix+"_deterministic.xml");
			File probCacheFile = new File(cacheDir, cachePrefix+"_probabilistic.xml");
			
			csDetCalc = new CachedMCErDeterministicCalc(csDetCalc, detCacheFile);
			csProbCalc = new CachedMCErProbabilisticCalc(csProbCalc, probCacheFile);
		}
		
		AbstractMCErDeterministicCalc gmpeDetCalc = null;
		AbstractMCErProbabilisticCalc gmpeProbCalc = null;
		
		if (gmpes != null && !gmpes.isEmpty()) {
			Component gmpeComponent = MCErCalcUtils.getSupportedTranslationComponent(
					gmpes.get(0), component.getGMPESupportedComponents());
			Preconditions.checkNotNull(gmpeComponent);
			
			gmpes.get(0).setIntensityMeasure(SA_Param.NAME);
			SA_Param.setPeriodInSA_Param(gmpes.get(0).getIntensityMeasure(), period);
			DiscretizedFunc xVals = new IMT_Info().getDefaultHazardCurve(gmpes.get(0).getIntensityMeasure());
			
			List<GMPE_MCErDeterministicCalc> detCalcs = Lists.newArrayList();
			List<GMPE_MCErProbabilisticCalc> probCalcs = Lists.newArrayList();
			
			List<String> gmpeNames = Lists.newArrayList();
			
			ERF gmpeDetERF = erf;
			if (detProbMod != null)
				gmpeDetERF = new RupProbModERF(erf, detProbMod);
			
			for (AttenuationRelationship gmpe : gmpes) {
				detCalcs.add(new GMPE_MCErDeterministicCalc(gmpeDetERF, gmpe, gmpeComponent));
				probCalcs.add(new GMPE_MCErProbabilisticCalc(erf, gmpe, gmpeComponent, xVals));
				
				gmpeNames.add(gmpe.getShortName());
			}
			
			if (gmpes.size() == 1){
				gmpeDetCalc = detCalcs.get(0);
				gmpeProbCalc = probCalcs.get(0);
			} else {
				// this will take the max determ val from each GMPE
				gmpeDetCalc = new CombinedMultiMCErDeterministicCalc(detCalcs);
				// this will average the prob values from each GMPE
				gmpeProbCalc = new CombinedMultiMCErProbabilisticCalc(probCalcs);
			}
			
			if (cache_gmpe) {
				// now cache
				File cacheDir = new File(outputDir, ".gmpe_cache");
				Preconditions.checkState(cacheDir.exists() || cacheDir.mkdir());
				
				String erfName;
				if (erf instanceof MeanUCERF2)
					erfName = "UCERF2";
				else if (erf instanceof FaultSystemSolutionERF)
					erfName = "UCERF3";
				else
					erfName = ClassUtils.getClassNameWithoutPackage(erf.getClass());
				
				String cachePrefix = Joiner.on("_").join(gmpeNames)+"_"+erfName+"_dataset"+datasetID+"_"
						+gmpeComponent.name();
				File detCacheFile = new File(cacheDir, cachePrefix+"_deterministic.xml");
				File probCacheFile = new File(cacheDir, cachePrefix+"_probabilistic.xml");
				
				gmpeDetCalc = new CachedMCErDeterministicCalc(gmpeDetCalc, detCacheFile);
				gmpeProbCalc = new CachedMCErProbabilisticCalc(gmpeProbCalc, probCacheFile);
			}
		}
		
		outputDir = new File(outputDir, (int)period+"s");
		Preconditions.checkState(outputDir.exists() || outputDir.mkdir());
		
		MCErMapGenerator.calculateMaps("CyberShake", csProbCalc, csDetCalc, "GMPE", gmpeProbCalc, gmpeDetCalc,
				region, sites, period, outputDir);
	}
	
	public static void main(String[] args) throws IOException, GMT_MapException {
		int datasetID = 57;
		String studyName = "study_15_4";
		
		CyberShakeComponent component = CyberShakeComponent.RotD100;
		double period = 2d;

		String outputName = studyName+"_"+component.name().toLowerCase();

		File outputDir = new File("/home/kevin/CyberShake/MCER/maps/"+outputName);
		Preconditions.checkState(outputDir.exists() || outputDir.mkdirs());

		ERF erf = MeanUCERF2_ToDB.createUCERF2ERF();
		List<AttenuationRelationship> gmpes = Lists.newArrayList();
		gmpes.add(AttenRelRef.ASK_2014.instance(null));
		gmpes.add(AttenRelRef.BSSA_2014.instance(null));
		gmpes.add(AttenRelRef.CB_2014.instance(null));
		gmpes.add(AttenRelRef.CY_2014.instance(null));
		for (AttenuationRelationship gmpe : gmpes)
			gmpe.setParamDefaults();

		calculateMaps(datasetID, component, period, erf, gmpes, outputDir);

		//// UCERF3/UCERF2 comparisons
		//twoPercentIn50 = true;
		//
		//String twoP_add = "";
		//if (twoPercentIn50 == true)
		//	twoP_add = "_2pin50";
		//File outputDir = new File("/home/kevin/CyberShake/MCER/maps/ucerf3_ucerf2_gmpe_rotd100/"+(int)period+"s"+twoP_add);
		//Preconditions.checkState(outputDir.exists() || outputDir.mkdirs());
		//
		//ERF ucerf2 = MeanUCERF2_ToDB.createUCERF2ERF();
		//ERF ucerf3 = new FaultSystemSolutionERF(FaultSystemIO.loadSol(new File(
		//		"/home/kevin/workspace/OpenSHA/dev/scratch/UCERF3/data/scratch/InversionSolutions/"
		//		+ "2013_05_10-ucerf3p3-production-10runs_COMPOUND_SOL_FM3_1_MEAN_BRANCH_AVG_SOL.zip")));
		//ucerf3.setParameter(ProbabilityModelParam.NAME, ProbabilityModelOptions.POISSON);
		//ucerf3.getTimeSpan().setDuration(1d);
		//ucerf3.setParameter(IncludeBackgroundParam.NAME, IncludeBackgroundOption.EXCLUDE);
		//ucerf3.updateForecast();
		//
		//List<ERF> erfs = Lists.newArrayList(ucerf3, ucerf2);
		//List<String> names = Lists.newArrayList("UCERF3", "UCERF2");
		//
		//List<AttenuationRelationship> gmpes = Lists.newArrayList();
		//gmpes.add(AttenRelRef.ASK_2014.instance(null));
		//gmpes.add(AttenRelRef.BSSA_2014.instance(null));
		//gmpes.add(AttenRelRef.CB_2014.instance(null));
		//gmpes.add(AttenRelRef.CY_2014.instance(null));
		//for (AttenuationRelationship gmpe : gmpes)
		//	gmpe.setParamDefaults();
		//
		//generateGMPEOnlyMaps(erfs, names, gmpes, datasetID, imTypeID, period, new WillsMap2006(), outputDir);

		System.exit(0);
	}

}
