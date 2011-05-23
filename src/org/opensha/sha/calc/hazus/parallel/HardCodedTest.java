package org.opensha.sha.calc.hazus.parallel;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ListIterator;

import org.opensha.commons.data.CSVFile;
import org.opensha.commons.data.Site;
import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.commons.data.siteData.AbstractSiteData;
import org.opensha.commons.data.siteData.SiteDataAPI;
import org.opensha.commons.data.siteData.SiteDataValue;
import org.opensha.commons.data.siteData.SiteDataValueList;
import org.opensha.commons.data.siteData.impl.CVM4BasinDepth;
import org.opensha.commons.data.siteData.impl.WillsMap2006;
import org.opensha.commons.data.siteData.util.SiteDataTypeParameterNameMap;
import org.opensha.commons.exceptions.ParameterException;
import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.LocationList;
import org.opensha.commons.param.Parameter;
import org.opensha.commons.param.impl.StringParameter;
import org.opensha.commons.util.ClassUtils;
import org.opensha.sha.calc.hazardMap.components.AsciiFileCurveArchiver;
import org.opensha.sha.calc.hazardMap.components.CalculationSettings;
import org.opensha.sha.calc.hazardMap.components.CurveResultsArchiver;
import org.opensha.sha.earthquake.EqkRupForecast;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.UCERF2;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.MeanUCERF2.MeanUCERF2;
import org.opensha.sha.gui.infoTools.IMT_Info;
import org.opensha.sha.imr.ScalarIntensityMeasureRelationshipAPI;
import org.opensha.sha.imr.attenRelImpl.CB_2008_AttenRel;
import org.opensha.sha.imr.attenRelImpl.MultiIMR_Averaged_AttenRel;
import org.opensha.sha.imr.attenRelImpl.NGA_2008_Averaged_AttenRel;
import org.opensha.sha.imr.attenRelImpl.NGA_2008_Averaged_AttenRel_NoAS;
import org.opensha.sha.imr.attenRelImpl.NSHMP_2008_CA;
import org.opensha.sha.imr.attenRelImpl.USGS_Combined_2004_AttenRel;
import org.opensha.sha.imr.param.IntensityMeasureParams.PGA_Param;
import org.opensha.sha.imr.param.IntensityMeasureParams.PGV_Param;
import org.opensha.sha.imr.param.IntensityMeasureParams.SA_Param;
import org.opensha.sha.imr.param.OtherParams.ComponentParam;
import org.opensha.sha.imr.param.OtherParams.SigmaTruncLevelParam;
import org.opensha.sha.imr.param.OtherParams.SigmaTruncTypeParam;
import org.opensha.sha.imr.param.SiteParams.DepthTo1pt0kmPerSecParam;
import org.opensha.sha.imr.param.SiteParams.DepthTo2pt5kmPerSecParam;
import org.opensha.sha.imr.param.SiteParams.Vs30_Param;
import org.opensha.sha.util.SiteTranslator;
import org.opensha.sha.util.TRTUtils;
import org.opensha.sha.util.TectonicRegionType;

public class HardCodedTest {
	
	private static SimpleDateFormat df = new SimpleDateFormat("yyyy_MM_dd-HH_mm");
	private static final boolean constrainBasinMin = false;
	
	private static MeanUCERF2 getUCERF2(int years, int startYear, boolean includeBackSeis) {
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
		
		StringParameter backSeisParam = (StringParameter) ucerf.getParameter(UCERF2.BACK_SEIS_NAME);
		if (includeBackSeis)
			backSeisParam.setValue(UCERF2.BACK_SEIS_INCLUDE);
		else
			backSeisParam.setValue(UCERF2.BACK_SEIS_EXCLUDE);
		
		ucerf.updateForecast();
		System.out.println("UCERF Params:");
		System.out.println(ucerf.getAdjustableParameterList().getParameterListMetadataString());
		
		return ucerf;
	}

	private static EqkRupForecast getERF(int years, int startYear, boolean includeBackSeis) {
		return getUCERF2(years, startYear, includeBackSeis);
	}
	
	private static ScalarIntensityMeasureRelationshipAPI getUSGSCombined2004IMR() {
		ScalarIntensityMeasureRelationshipAPI attenRel = new USGS_Combined_2004_AttenRel(null);
		attenRel.setParamDefaults();
		attenRel.getParameter(ComponentParam.NAME).
				setValue(ComponentParam.COMPONENT_AVE_HORZ);
		return attenRel;
	}
	
	private static ScalarIntensityMeasureRelationshipAPI getUSGSCombined2008IMR() {
		ScalarIntensityMeasureRelationshipAPI attenRel = new NSHMP_2008_CA(null);
		attenRel.setParamDefaults();
		return attenRel;
	}
	
	private static ScalarIntensityMeasureRelationshipAPI getCB_2008IMR() {
		ScalarIntensityMeasureRelationshipAPI imr = new CB_2008_AttenRel(null);
		imr.setParamDefaults();
		return imr;
	}
	
	private static ScalarIntensityMeasureRelationshipAPI getNGA_2008IMR(
			boolean propEffectSpeedup, boolean includeAS) {
		ScalarIntensityMeasureRelationshipAPI imr;
		if (includeAS)
			imr = new NGA_2008_Averaged_AttenRel(null);
		else
			imr = new NGA_2008_Averaged_AttenRel_NoAS(null);
		imr.setParamDefaults();
		imr.getParameter(MultiIMR_Averaged_AttenRel.PROP_EFFECT_SPEEDUP_PARAM_NAME).setValue(propEffectSpeedup);
		return imr;
	}

	private static ScalarIntensityMeasureRelationshipAPI getIMR(String name, double sigmaTrunc, boolean propEffectSpeedup){
		ScalarIntensityMeasureRelationshipAPI attenRel;
		if (name.equals(NSHMP_08_NAME))
			attenRel = getUSGSCombined2008IMR();
		else if (name.equals(MultiIMR_NAME))
			attenRel = getNGA_2008IMR(propEffectSpeedup, true);
		else if (name.equals(MultiIMR_NO_AS_NAME))
			attenRel = getNGA_2008IMR(propEffectSpeedup, false);
		else
			throw new IllegalArgumentException("Not valid IMR: "+name);
//		ScalarIntensityMeasureRelationshipAPI attenRel = getUSGSCombined2008IMR();
//		ScalarIntensityMeasureRelationshipAPI attenRel = getCB_2008IMR();
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
	
	public static LocationList loadCSV(File file) throws IOException {
		LocationList locs = new LocationList();
		
		CSVFile<String> csv = CSVFile.readFile(file);
		
		for (int i=0; i<csv.getNumLines(); i++) {
			ArrayList<String> line = csv.getLine(i);
			double lat = Double.parseDouble(line.get(1));
			double lon = Double.parseDouble(line.get(2));
			locs.add(new Location(lat, lon));
		}
		
		return locs;
	}
	
	private static final String NSHMP_08_NAME = "NSHMP08";
	private static final String MultiIMR_NAME = "MultiIMR";
	private static final String MultiIMR_NO_AS_NAME = "MultiIMRnoAS";

	public static void main(String args[]) throws IOException, InvocationTargetException {
		if (args.length != 6) {
			System.err.println("USAGE: "+ClassUtils.getClassNameWithoutPackage(HardCodedTest.class)+
					" <T/F: time dependent> <"+NSHMP_08_NAME+"/"+MultiIMR_NAME+"/"+MultiIMR_NO_AS_NAME+">"+
					" <T/F: prop effect speedup> <T/F: back seis>"+
					" <HardCoded Vs30 (or 'null' for site data providers>+" +
					" <dir name>");
			System.exit(2);
		}
		boolean timeDep = Boolean.parseBoolean(args[0]);
		String imrStr = args[1];
		boolean propEffectSpeedup = Boolean.parseBoolean(args[2]);
		boolean includeBackSeis = Boolean.parseBoolean(args[3]);
		String vs30Str = args[4];
		SiteDataValue<?> hardcodedVal;
		if (vs30Str.equals("null"))
			hardcodedVal = null;
		else
			hardcodedVal = new SiteDataValue<Double>(SiteDataAPI.TYPE_VS30, SiteDataAPI.TYPE_FLAG_INFERRED,
					Double.parseDouble(vs30Str));
		String dirName = args[5];
		
		int years = 50;
		int startYear;
		if (timeDep)
			startYear = 2011;
		else
			startYear = -1;
		EqkRupForecast erf = getERF(years, startYear, includeBackSeis);
		
//		SiteDataValue<?> hardcodedVal =
//			new SiteDataValue<String>(SiteDataAPI.TYPE_WILLS_CLASS, SiteDataAPI.TYPE_FLAG_INFERRED, "B");
		boolean nullBasin = true;
//		SiteDataValue<?> hardcodedVal =
//			new SiteDataValue<Double>(SiteDataAPI.TYPE_VS30, SiteDataAPI.TYPE_FLAG_INFERRED, 760.0);
//		SiteDataValue<?> hardcodedVal = null;
		
		double sigmaTrunc = 3;
		ScalarIntensityMeasureRelationshipAPI imr = getIMR(imrStr, sigmaTrunc, propEffectSpeedup);
		HashMap<TectonicRegionType, ScalarIntensityMeasureRelationshipAPI> imrMap =
			TRTUtils.wrapInHashMap(imr);
		ArrayList<HashMap<TectonicRegionType, ScalarIntensityMeasureRelationshipAPI>> imrMaps = 
			new ArrayList<HashMap<TectonicRegionType,ScalarIntensityMeasureRelationshipAPI>>();
		imrMaps.add(imrMap);
		
		Iterator<Parameter<?>> imrSiteParamsIt = imr.getSiteParamsIterator();
		while (imrSiteParamsIt.hasNext()) {
			Parameter<?> param =imrSiteParamsIt.next();
			String paramName = param.getName();
			if (nullBasin && (paramName.equals(DepthTo2pt5kmPerSecParam.NAME)
								|| paramName.equals(DepthTo1pt0kmPerSecParam.NAME))) {
				param.setValue(null);
			}
		}
		
		double spacing = 0.1;
//		double spacing = 0.05;
		String spacingCode = ""+(int)(spacing * 100d);
		if (spacingCode.length() < 2)
			spacingCode = "0"+spacingCode;
//		Location topLeft = new Location(42.1, -125.5);
//		Location bottomRight = new Location(32.4, -114.1);
//		GriddedRegion region = new GriddedRegion(topLeft, bottomRight, spacing, topLeft);
//		GriddedRegion region = new CaliforniaRegions.RELM_TESTING_GRIDDED(spacing);
		String spacingFile = "/home/scec-00/kmilner/hazMaps/"+spacingCode+"grid.csv";
		LocationList locs = loadCSV(new File(spacingFile));
		
		ArrayList<SiteDataAPI<?>> provs = null;
		if (hardcodedVal == null) {
			provs = new ArrayList<SiteDataAPI<?>>();
			SiteDataTypeParameterNameMap siteDataMap = SiteTranslator.DATA_TYPE_PARAM_NAME_MAP;
			if (siteDataMap.isTypeApplicable(SiteDataAPI.TYPE_VS30, imr))
				provs.add(new WillsMap2006());
			if (siteDataMap.isTypeApplicable(SiteDataAPI.TYPE_DEPTH_TO_2_5, imr))
				provs.add(new CVM4BasinDepth(SiteDataAPI.TYPE_DEPTH_TO_2_5));
			if (siteDataMap.isTypeApplicable(SiteDataAPI.TYPE_DEPTH_TO_1_0, imr))
				provs.add(new CVM4BasinDepth(SiteDataAPI.TYPE_DEPTH_TO_1_0));
		}
		
		if (constrainBasinMin) {
			// constrain basin depth to default minimums
			for (SiteDataAPI<?> prov : provs) {
				if (prov.getDataType().equals(SiteDataAPI.TYPE_DEPTH_TO_2_5)
						|| prov.getDataType().equals(SiteDataAPI.TYPE_DEPTH_TO_1_0)) {
					Parameter<Double> minBasinParam = null;
					try {
						minBasinParam = prov.getAdjustableParameterList()
							.getParameter(AbstractSiteData.PARAM_MIN_BASIN_DEPTH_DOUBLE_NAME);
					} catch (ParameterException e) {}
					if (minBasinParam != null) {
						ListIterator<Parameter<?>> siteParamsIt = imr.getSiteParamsIterator();
						while (siteParamsIt.hasNext()) {
							Parameter<?> param = siteParamsIt.next();
							if (param.getName().equals(DepthTo2pt5kmPerSecParam.NAME)
									&& prov.getDataType().equals(SiteDataAPI.TYPE_DEPTH_TO_2_5)) {
								minBasinParam.setValue((Double)param.getValue());
							} else if (param.getName().equals(DepthTo1pt0kmPerSecParam.NAME)
									&& prov.getDataType().equals(SiteDataAPI.TYPE_DEPTH_TO_1_0)) {
								Double minVal = (Double)param.getValue();
								// convert from KM to M
								minVal *= 1000;
								minBasinParam.setValue(minVal);
							}
						}
					}
				}
			}
		}
		
		ArrayList<SiteDataValue<?>>[] siteData = new ArrayList[locs.size()];
		if (hardcodedVal == null) {
			for (SiteDataAPI<?> prov : provs) {
				SiteDataValueList<?> vals = prov.getAnnotatedValues(locs);
				for (int i=0; i<siteData.length; i++) {
					if (siteData[i] == null)
						siteData[i] = new ArrayList<SiteDataValue<?>>();
					SiteDataValue<?> val = vals.getValue(i);
//					if ((val.getDataType().equals(SiteDataAPI.TYPE_DEPTH_TO_2_5)
//							&& (Double)val.getValue() > DepthTo2pt5kmPerSecParam.MAX)
//							|| (val.getDataType().equals(SiteDataAPI.TYPE_DEPTH_TO_1_0)
//							&& (Double)val.getValue() > DepthTo1pt0kmPerSecParam.MAX)) {
//						System.out.println("Got a super high: " + val);
//						val = new SiteDataValue<Double>(val.getDataType(),
//								val.getDataMeasurementType(), Double.NaN);
//					}
					siteData[i].add(val);
				}
			}
		} else {
			for (int i=0; i<siteData.length; i++) {
				siteData[i] = new ArrayList<SiteDataValue<?>>();
				siteData[i].add(hardcodedVal);
			}
		}
		
		SiteTranslator trans = new SiteTranslator();
		
		ArrayList<Site> sites = new ArrayList<Site>();
		for (int i=0; i<locs.size(); i++) {
			Location loc = locs.get(i);
			Site site = new Site(loc);
			ListIterator<Parameter<?>> it = imr.getSiteParamsIterator();
			ArrayList<SiteDataValue<?>> datas = siteData[i];
			while (it.hasNext()) {
				Parameter<?> siteParam = it.next();
				Parameter clonedParam = (Parameter) siteParam.clone();
				String paramName = siteParam.getName();
				if (nullBasin &&
						(paramName.equals(DepthTo2pt5kmPerSecParam.NAME)
								|| paramName.equals(DepthTo1pt0kmPerSecParam.NAME))) {
					clonedParam.setValue(null);
				} else {
					trans.setParameterValue(clonedParam, datas);
				}
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
		
//		String jobDir = "/home/scec-00/kmilner/hazMaps/hazus_test-" + df.format(new Date()) + "/";
		String jobDir = "/home/scec-00/kmilner/hazMaps/"+dirName+"/";
		String curveDir = jobDir + "curves/";
		CurveResultsArchiver archiver = new AsciiFileCurveArchiver(curveDir, true, false);
		
		String javaExec = "/auto/usc/jdk/1.6.0/jre/bin/java";
		String jarFile = "/home/scec-00/kmilner/hazMaps/svn/dist/OpenSHA_complete.jar";
		
		HazusDataSetDAGCreator dag = new HazusDataSetDAGCreator(erf, imrMaps, sites,
				calcSet, archiver, javaExec, jarFile, years, spacing);
		
		dag.writeDAG(new File(jobDir), 20, false);
	}

}
