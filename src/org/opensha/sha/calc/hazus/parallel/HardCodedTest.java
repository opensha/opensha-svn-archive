package org.opensha.sha.calc.hazus.parallel;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.ListIterator;

import org.opensha.commons.data.Site;
import org.opensha.commons.data.TimeSpan;
import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.commons.data.region.CaliforniaRegions;
import org.opensha.commons.data.siteData.OrderedSiteDataProviderList;
import org.opensha.commons.data.siteData.SiteDataAPI;
import org.opensha.commons.data.siteData.SiteDataProvidersTest;
import org.opensha.commons.data.siteData.SiteDataValue;
import org.opensha.commons.data.siteData.SiteDataValueList;
import org.opensha.commons.data.siteData.impl.CVM4BasinDepth;
import org.opensha.commons.data.siteData.impl.WillsMap2006;
import org.opensha.commons.geo.GriddedRegion;
import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.LocationList;
import org.opensha.commons.param.ParameterAPI;
import org.opensha.sha.calc.hazardMap.components.AsciiFileCurveArchiver;
import org.opensha.sha.calc.hazardMap.components.CalculationSettings;
import org.opensha.sha.calc.hazardMap.components.CurveResultsArchiver;
import org.opensha.sha.earthquake.EqkRupForecast;
import org.opensha.sha.earthquake.rupForecastImpl.Frankel02.Frankel02_AdjustableEqkRupForecast;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.UCERF2;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.MeanUCERF2.MeanUCERF2;
import org.opensha.sha.gui.infoTools.IMT_Info;
import org.opensha.sha.imr.ScalarIntensityMeasureRelationshipAPI;
import org.opensha.sha.imr.attenRelImpl.CB_2008_AttenRel;
import org.opensha.sha.imr.attenRelImpl.USGS_Combined_2004_AttenRel;
import org.opensha.sha.imr.param.IntensityMeasureParams.PGA_Param;
import org.opensha.sha.imr.param.IntensityMeasureParams.PGV_Param;
import org.opensha.sha.imr.param.IntensityMeasureParams.SA_Param;
import org.opensha.sha.imr.param.OtherParams.ComponentParam;
import org.opensha.sha.imr.param.OtherParams.SigmaTruncLevelParam;
import org.opensha.sha.imr.param.OtherParams.SigmaTruncTypeParam;
import org.opensha.sha.imr.param.SiteParams.Vs30_Param;
import org.opensha.sha.util.SiteTranslator;
import org.opensha.sha.util.TRTUtils;
import org.opensha.sha.util.TectonicRegionType;

public class HardCodedTest {
	
	private static SimpleDateFormat df = new SimpleDateFormat("yyyy_MM_dd-HH_mm");
	
	private static MeanUCERF2 getUCERF2(int years, int startYear) {
		MeanUCERF2 ucerf = new MeanUCERF2();
		
		if (startYear > 0) {
			ucerf.getAdjustableParameterList().getParameter(UCERF2.PROB_MODEL_PARAM_NAME)
					.setValue(MeanUCERF2.PROB_MODEL_WGCEP_PREF_BLEND);
			ucerf.getTimeSpan().setStartTime(startYear);
		} else {
			ucerf.getAdjustableParameterList().getParameter(UCERF2.PROB_MODEL_PARAM_NAME)
					.setValue(UCERF2.PROB_MODEL_POISSON);
		}
		ucerf.getTimeSpan().setDuration(years);
		
		ucerf.updateForecast();
		System.out.println("UCERF Params:");
		System.out.println(ucerf.getAdjustableParameterList().getParameterListMetadataString());
		
		return ucerf;
	}

	private static EqkRupForecast getERF(int years, int startYear) {
		return getUCERF2(years, startYear);
	}
	
	private static ScalarIntensityMeasureRelationshipAPI getUSGSCombined2004IMR() {
		ScalarIntensityMeasureRelationshipAPI attenRel = new USGS_Combined_2004_AttenRel(null);
		attenRel.setParamDefaults();
		attenRel.getParameter(ComponentParam.NAME).
				setValue(ComponentParam.COMPONENT_AVE_HORZ);
		return attenRel;
	}
	
	private static ScalarIntensityMeasureRelationshipAPI getCB_2008IMR() {
		ScalarIntensityMeasureRelationshipAPI imr = new CB_2008_AttenRel(null);
		imr.setParamDefaults();
		return imr;
	}

	private static ScalarIntensityMeasureRelationshipAPI getIMR(double sigmaTrunc){
		ScalarIntensityMeasureRelationshipAPI attenRel = getCB_2008IMR();
		attenRel.getParameter(Vs30_Param.NAME).setValue(new Double(760));
		if (sigmaTrunc > 0) {
			attenRel.getParameter(SigmaTruncTypeParam.NAME).
				setValue(SigmaTruncTypeParam.SIGMA_TRUNC_TYPE_1SIDED);
			attenRel.getParameter(SigmaTruncLevelParam.NAME).
				setValue(new Double(sigmaTrunc));
		} else {
			attenRel.getParameter(SigmaTruncTypeParam.NAME).
			setValue(SigmaTruncTypeParam.SIGMA_TRUNC_TYPE_NONE);
		}
		
		return attenRel;
	}

	public static void main(String args[]) throws IOException, InvocationTargetException {
		int years = 50;
		int startYear = 2010;
		EqkRupForecast erf = getERF(years, startYear);
		
		double sigmaTrunc = 0;
		ScalarIntensityMeasureRelationshipAPI imr = getIMR(sigmaTrunc);
		HashMap<TectonicRegionType, ScalarIntensityMeasureRelationshipAPI> imrMap =
			TRTUtils.wrapInHashMap(imr);
		ArrayList<HashMap<TectonicRegionType, ScalarIntensityMeasureRelationshipAPI>> imrMaps = 
			new ArrayList<HashMap<TectonicRegionType,ScalarIntensityMeasureRelationshipAPI>>();
		imrMaps.add(imrMap);
		
		double spacing = 0.1;
//		Location topLeft = new Location(42.1, -125.5);
//		Location bottomRight = new Location(32.4, -114.1);
//		GriddedRegion region = new GriddedRegion(topLeft, bottomRight, spacing, topLeft);
		GriddedRegion region = new CaliforniaRegions.RELM_TESTING_GRIDDED(spacing);
		
		ArrayList<SiteDataAPI<?>> provs = new ArrayList<SiteDataAPI<?>>();
		provs.add(new WillsMap2006());
		provs.add(new CVM4BasinDepth(SiteDataAPI.TYPE_DEPTH_TO_2_5));
		provs.add(new CVM4BasinDepth(SiteDataAPI.TYPE_DEPTH_TO_1_0));
		ArrayList<SiteDataValue<?>>[] siteData = new ArrayList[region.getNodeCount()];
		for (SiteDataAPI<?> prov : provs) {
			SiteDataValueList<?> vals = prov.getAnnotatedValues(region.getNodeList());
			for (int i=0; i<siteData.length; i++) {
				if (siteData[i] == null)
					siteData[i] = new ArrayList<SiteDataValue<?>>();
				siteData[i].add(vals.getValue(i));
			}
		}
		
		SiteTranslator trans = new SiteTranslator();
		
		ArrayList<Site> sites = new ArrayList<Site>();
		LocationList locs = region.getNodeList();
		for (int i=0; i<region.getNodeCount(); i++) {
			Location loc = locs.get(i);
			Site site = new Site(loc);
			ListIterator<ParameterAPI<?>> it = imr.getSiteParamsIterator();
			ArrayList<SiteDataValue<?>> datas = siteData[i];
			while (it.hasNext()) {
				ParameterAPI<?> siteParam = it.next();
				ParameterAPI clonedParam = (ParameterAPI) siteParam.clone();
				trans.setParameterValue(clonedParam, datas);
				site.addParameter(clonedParam);
			}
			sites.add(site);
		}
		IMT_Info imtInfo = new IMT_Info();
		HashMap<String, ArbitrarilyDiscretizedFunc> imtXValMap = new HashMap<String, ArbitrarilyDiscretizedFunc>();
		imtXValMap.put(PGA_Param.NAME, imtInfo.getDefaultHazardCurve(PGA_Param.NAME));
		imtXValMap.put(PGV_Param.NAME, imtInfo.getDefaultHazardCurve(PGV_Param.NAME));
		imtXValMap.put(SA_Param.NAME, imtInfo.getDefaultHazardCurve(SA_Param.NAME));
		CalculationSettings calcSet = new CalculationSettings(imtXValMap, 200.0);
		
		String jobDir = "/home/scec-00/kmilner/hazMaps/hazus_test-" + df.format(new Date()) + "/";
		String curveDir = jobDir + "curves/";
		CurveResultsArchiver archiver = new AsciiFileCurveArchiver(curveDir, true, false);
		
		String javaExec = "/auto/usc/jdk/1.6.0/jre/bin/java";
		String jarFile = "/home/scec-00/kmilner/hazMaps/svn/dist/OpenSHA_complete.jar";
		
		HazusDataSetDAGCreator dag = new HazusDataSetDAGCreator(erf, imrMaps, sites,
				calcSet, archiver, javaExec, jarFile, years);
		
		dag.writeDAG(new File(jobDir), 20, false);
	}

}
