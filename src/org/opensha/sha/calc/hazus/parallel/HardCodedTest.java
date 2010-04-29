package org.opensha.sha.calc.hazus.parallel;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ListIterator;

import org.opensha.commons.data.Site;
import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.commons.geo.GriddedRegion;
import org.opensha.commons.geo.Location;
import org.opensha.commons.param.ParameterAPI;
import org.opensha.sha.calc.hazardMap.components.AsciiFileCurveArchiver;
import org.opensha.sha.calc.hazardMap.components.CalculationSettings;
import org.opensha.sha.calc.hazardMap.components.CurveResultsArchiver;
import org.opensha.sha.earthquake.EqkRupForecast;
import org.opensha.sha.earthquake.rupForecastImpl.Frankel02.Frankel02_AdjustableEqkRupForecast;
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
import org.opensha.sha.util.TectonicRegionType;

public class HardCodedTest {

	private static EqkRupForecast getERF() {
		EqkRupForecast erf = new Frankel02_AdjustableEqkRupForecast();
		//		forecast = new Frankel02_AdjustableEqkRupForecast();
		erf.getTimeSpan().setDuration(50.0);
		/*forecast.getAdjustableParameterList().getParameter(
	               Frankel02_AdjustableEqkRupForecast.BACK_SEIS_NAME).setValue(Frankel02_AdjustableEqkRupForecast.
	                                        BACK_SEIS_EXCLUDE);*/
		erf.getAdjustableParameterList().getParameter(
				Frankel02_AdjustableEqkRupForecast.BACK_SEIS_NAME).setValue(Frankel02_AdjustableEqkRupForecast.
						BACK_SEIS_EXCLUDE);
		erf.getAdjustableParameterList().getParameter(
				Frankel02_AdjustableEqkRupForecast.BACK_SEIS_RUP_NAME).setValue(
						Frankel02_AdjustableEqkRupForecast.BACK_SEIS_RUP_FINITE);
		erf.updateForecast();
		return erf;
	}

	private static ScalarIntensityMeasureRelationshipAPI getIMR(){
		ScalarIntensityMeasureRelationshipAPI attenRel = new USGS_Combined_2004_AttenRel(null);
		attenRel.setParamDefaults();
		attenRel.getParameter(Vs30_Param.NAME).setValue(new Double(760));
		attenRel.getParameter(SigmaTruncTypeParam.NAME).
		setValue(SigmaTruncTypeParam.SIGMA_TRUNC_TYPE_1SIDED);
		attenRel.getParameter(SigmaTruncLevelParam.NAME).
		setValue(new Double(3.0));
		attenRel.getParameter(ComponentParam.NAME).
		setValue(ComponentParam.COMPONENT_AVE_HORZ);
		return attenRel;
	}

	public static void main(String args[]) throws IOException, InvocationTargetException {
		EqkRupForecast erf = getERF();
		
		
		ScalarIntensityMeasureRelationshipAPI imr = getIMR();
		HashMap<TectonicRegionType, ScalarIntensityMeasureRelationshipAPI> imrMap =
			new HashMap<TectonicRegionType, ScalarIntensityMeasureRelationshipAPI>();
		imrMap.put(TectonicRegionType.ACTIVE_SHALLOW, imr);
		ArrayList<HashMap<TectonicRegionType, ScalarIntensityMeasureRelationshipAPI>> imrMaps = 
			new ArrayList<HashMap<TectonicRegionType,ScalarIntensityMeasureRelationshipAPI>>();
		imrMaps.add(imrMap);
		
		Location topLeft = new Location(42.1, -125.5);
		Location bottomRight = new Location(32.4, -114.1);
		double spacing = 0.1;
		GriddedRegion region = new GriddedRegion(topLeft, bottomRight, spacing, topLeft);
		
		ArrayList<Site> sites = new ArrayList<Site>();
		for (Location loc : region.getNodeList()) {
			Site site = new Site(loc);
			ListIterator<ParameterAPI<?>> it = imr.getSiteParamsIterator();
			while (it.hasNext()) {
				ParameterAPI<?> siteParam = it.next();
				site.addParameter((ParameterAPI) siteParam.clone());
			}
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
