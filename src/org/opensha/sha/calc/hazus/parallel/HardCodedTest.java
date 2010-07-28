package org.opensha.sha.calc.hazus.parallel;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ListIterator;

import org.opensha.commons.data.Site;
import org.opensha.commons.data.TimeSpan;
import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.commons.data.region.CaliforniaRegions;
import org.opensha.commons.geo.GriddedRegion;
import org.opensha.commons.geo.Location;
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
import org.opensha.sha.imr.attenRelImpl.USGS_Combined_2004_AttenRel;
import org.opensha.sha.imr.param.IntensityMeasureParams.PGA_Param;
import org.opensha.sha.imr.param.IntensityMeasureParams.PGV_Param;
import org.opensha.sha.imr.param.IntensityMeasureParams.SA_Param;
import org.opensha.sha.imr.param.OtherParams.ComponentParam;
import org.opensha.sha.imr.param.OtherParams.SigmaTruncLevelParam;
import org.opensha.sha.imr.param.OtherParams.SigmaTruncTypeParam;
import org.opensha.sha.imr.param.SiteParams.Vs30_Param;
import org.opensha.sha.util.TRTUtils;
import org.opensha.sha.util.TectonicRegionType;

public class HardCodedTest {
	
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

	private static ScalarIntensityMeasureRelationshipAPI getIMR(double sigmaTrunc){
		ScalarIntensityMeasureRelationshipAPI attenRel = getUSGSCombined2004IMR();
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
		int years = 30;
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
		
		ArrayList<Site> sites = new ArrayList<Site>();
		for (Location loc : region.getNodeList()) {
			Site site = new Site(loc);
			ListIterator<ParameterAPI<?>> it = imr.getSiteParamsIterator();
			while (it.hasNext()) {
				ParameterAPI<?> siteParam = it.next();
				site.addParameter((ParameterAPI) siteParam.clone());
			}
			sites.add(site);
		}
		IMT_Info imtInfo = new IMT_Info();
		HashMap<String, ArbitrarilyDiscretizedFunc> imtXValMap = new HashMap<String, ArbitrarilyDiscretizedFunc>();
		imtXValMap.put(PGA_Param.NAME, imtInfo.getDefaultHazardCurve(PGA_Param.NAME));
		imtXValMap.put(PGV_Param.NAME, imtInfo.getDefaultHazardCurve(PGV_Param.NAME));
		imtXValMap.put(SA_Param.NAME, imtInfo.getDefaultHazardCurve(SA_Param.NAME));
		CalculationSettings calcSet = new CalculationSettings(imtXValMap, 200.0);
		
		String jobDir = "/home/scec-00/kmilner/hazMaps/hazus_test/";
		String curveDir = jobDir + "curves/";
		CurveResultsArchiver archiver = new AsciiFileCurveArchiver(curveDir, true, false);
		
		String javaExec = "/auto/usc/jdk/1.6.0/jre/bin/java";
		String jarFile = "/home/scec-00/kmilner/hazMaps/svn/dist/OpenSHA_complete.jar";
		
		HazusDataSetDAGCreator dag = new HazusDataSetDAGCreator(erf, imrMaps, sites,
				calcSet, archiver, javaExec, jarFile);
		
		dag.writeDAG(new File(jobDir), 100, false);
	}

}
