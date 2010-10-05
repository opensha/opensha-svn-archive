package org.opensha.sha.cybershake.maps;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import org.opensha.commons.data.ArbDiscretizedXYZ_DataSet;
import org.opensha.commons.data.XYZ_DataSetAPI;
import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.commons.data.region.CaliforniaRegions;
import org.opensha.commons.data.region.SitesInGriddedRegion;
import org.opensha.commons.data.siteData.OrderedSiteDataProviderList;
import org.opensha.commons.data.siteData.impl.WillsMap2000;
import org.opensha.commons.exceptions.ParameterException;
import org.opensha.commons.exceptions.RegionConstraintException;
import org.opensha.commons.geo.GriddedRegion;
import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.Region;
import org.opensha.commons.mapping.gmt.elements.TopographicSlopeFile;
import org.opensha.commons.param.ParameterAPI;
import org.opensha.commons.util.cpt.CPT;
import org.opensha.sha.calc.ScenarioShakeMapCalculator;
import org.opensha.sha.cybershake.HazardCurveFetcher;
import org.opensha.sha.cybershake.calc.ShakeMapComputation;
import org.opensha.sha.cybershake.db.CybershakeSite;
import org.opensha.sha.cybershake.db.Cybershake_OpenSHA_DBApplication;
import org.opensha.sha.cybershake.db.DBAccess;
import org.opensha.sha.cybershake.db.HazardCurveComputation;
import org.opensha.sha.cybershake.db.PeakAmplitudesFromDB;
import org.opensha.sha.cybershake.db.PeakAmplitudesRecord;
import org.opensha.sha.cybershake.db.Runs2DB;
import org.opensha.sha.cybershake.maps.InterpDiffMap.InterpDiffMapType;
import org.opensha.sha.cybershake.maps.servlet.CS_InterpDiffMapServletAccessor;
import org.opensha.sha.earthquake.EqkRupture;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.MeanUCERF2.MeanUCERF2;
import org.opensha.sha.gui.infoTools.IMT_Info;
import org.opensha.sha.imr.AttenuationRelationship;
import org.opensha.sha.imr.attenRelImpl.AS_2008_AttenRel;
import org.opensha.sha.imr.attenRelImpl.Abrahamson_2000_AttenRel;
import org.opensha.sha.imr.attenRelImpl.CB_2008_AttenRel;
import org.opensha.sha.imr.attenRelImpl.ShakeMap_2003_AttenRel;
import org.opensha.sha.imr.param.IntensityMeasureParams.SA_Param;

public class ScenarioShakeMapGen {
	
	private static final String cacheDir = "/home/kevin/CyberShake/M8/";
	
	private static EqkRupture getRupture(int sourceID, int rupID, Location hypo) {
		MeanUCERF2 ucerf = new MeanUCERF2();
		ucerf.updateForecast();
		EqkRupture rup = ucerf.getRupture(sourceID, rupID);
		rup.setHypocenterLocation(hypo);
		return rup;
	}
	
	private static AttenuationRelationship getAttenRel() {
//		AttenuationRelationship attenRel = new AS_2008_AttenRel(null);
//		AttenuationRelationship attenRel = new Abrahamson_2000_AttenRel(null);
//		AttenuationRelationship attenRel = new ShakeMap_2003_AttenRel(null);
		AttenuationRelationship attenRel = new CB_2008_AttenRel(null);
		
		attenRel.setParamDefaults();
		attenRel.setIntensityMeasure(SA_Param.NAME);
		SA_Param.setPeriodInSA_Param(attenRel.getIntensityMeasure(), 3.0);
		return attenRel;
	}
	
	private static SitesInGriddedRegion getSites(AttenuationRelationship attenRel, Region region, double spacing) throws IOException {
		SitesInGriddedRegion sites = new SitesInGriddedRegion(new GriddedRegion(region, spacing, null));
		
		sites.addSiteParams(attenRel.getSiteParamsIterator());
		
		OrderedSiteDataProviderList provs = OrderedSiteDataProviderList.createSiteDataProviderDefaults();
		for (int i=0; i<provs.size(); i++) {
			if (provs.getProvider(i).getName().equals(WillsMap2000.NAME)) {
				provs.remove(i);
				break;
			}
		}
		
		sites.setSiteParamsForRegion(provs);
		ArrayList<ParameterAPI<?>> defaultSiteParams = new ArrayList<ParameterAPI<?>>();
		for (ParameterAPI<?> param : attenRel.getSiteParamsList()) {
			defaultSiteParams.add((ParameterAPI<?>) param.clone());
		}
		sites.setDefaultSiteParams(defaultSiteParams);
		
		return sites;
	}
	
	private static XYZ_DataSetAPI computeBaseMap(EqkRupture rup,
			AttenuationRelationship attenRel,
			SitesInGriddedRegion sites,
			boolean isProbAtIML,double value) throws ParameterException, RegionConstraintException {
		ScenarioShakeMapCalculator calc = new ScenarioShakeMapCalculator();
		
		ArrayList<AttenuationRelationship> selectedAttenRels = new ArrayList<AttenuationRelationship>();
		selectedAttenRels.add(attenRel);
		ArrayList<Double> attenRelWts = new ArrayList<Double>();
		attenRelWts.add(1.0);
		
		XYZ_DataSetAPI xyz = calc.getScenarioShakeMapData(selectedAttenRels, attenRelWts, sites, rup, isProbAtIML, value);
		ArrayList<Double> vals = xyz.getZ_DataSet();
		ArrayList<Double> unLogVals = new ArrayList<Double>();
		for (Double val : vals) {
			unLogVals.add(Math.exp(val));
		}
		ArbDiscretizedXYZ_DataSet unLog = new ArbDiscretizedXYZ_DataSet(xyz.getX_DataSet(), xyz.getY_DataSet(), unLogVals);
		
		return unLog;
	}
	
	private static XYZ_DataSetAPI getScatterData(int datasetID, int erfID, int rupVarScenID, int imTypeID,
			int sourceID, int rupID, Location hypo,
			boolean isProbAtIML, double value)
			throws FileNotFoundException, IOException {
		if (isProbAtIML || value != 0.5)
			throw new RuntimeException("CS shakemaps can only be computed for IML @ 50% prob.");
		File file = new File(cacheDir+"csScatter_"+datasetID+"_"+imTypeID+"_"+sourceID+"_"
				+rupID+"_"+isProbAtIML+"_"+value+".txt");
		if (file.exists()) {
			return ArbDiscretizedXYZ_DataSet.loadXYZFile(file.getAbsolutePath());
		} else {
			ShakeMapComputation calc = new ShakeMapComputation(Cybershake_OpenSHA_DBApplication.db);
			
			XYZ_DataSetAPI xyz = calc.getShakeMap(datasetID, erfID, rupVarScenID, imTypeID,
					sourceID, rupID, hypo);
			ArbDiscretizedXYZ_DataSet.writeXYZFile(xyz, file.getAbsolutePath());
			
			return xyz;
		}
	}

	/**
	 * @param args
	 * @throws IOException 
	 * @throws RegionConstraintException 
	 * @throws ParameterException 
	 * @throws ClassNotFoundException 
	 */
	public static void main(String[] args) throws IOException, ParameterException, RegionConstraintException, ClassNotFoundException {
		int sourceID = 89;
		int rupID = 3;
		Location hypo = new Location(35.849, -120.3858, 15.5655);
		System.out.println("Creating Atten Rel...");
		AttenuationRelationship attenRel = getAttenRel();
		
		Region region = new CaliforniaRegions.CYBERSHAKE_MAP_REGION();
		double spacing = 0.005;
		
		boolean logPlot = true;
		
		boolean isProbAt_IML = false;
		double val = 0.5;
		
		File baseMapFile = new File(cacheDir+"baseMap_"+attenRel.getShortName()
				+"_"+spacing+"_"+sourceID+"_"+rupID+"_"+isProbAt_IML+"_"+val+".txt");
		
		XYZ_DataSetAPI baseMap;
		if (baseMapFile.exists()) {
			System.out.println("Loading Base MAp Data");
			baseMap = ArbDiscretizedXYZ_DataSet.loadXYZFile(baseMapFile.getAbsolutePath());
		} else {
			System.out.println("Getting rupture...");
			EqkRupture rup = getRupture(sourceID, rupID, hypo);
			System.out.println("Rup rake: " + rup.getAveRake());
			System.out.println("Loading sites...");
			SitesInGriddedRegion sites = getSites(attenRel, region, spacing);
			
			System.out.println("Generating ShakeMap...");
			baseMap = computeBaseMap(rup, attenRel, sites, isProbAt_IML, val);
			ArbDiscretizedXYZ_DataSet.writeXYZFile(baseMap, baseMapFile.getAbsolutePath());
		}
		
		CPT cpt = CPT.loadFromStream(HardCodedInterpDiffMapCreator.class.getResourceAsStream(
		"/resources/cpt/MaxSpectrum2.cpt"));
		
		GMT_InterpolationSettings interpSettings = GMT_InterpolationSettings.getDefaultSettings();
		
		int datasetID = 1;
		int imTypeID = 21;
		int erfID = 35;
		int rupVarScenID = 3;
		
		System.out.println("Loading Scatter Data");
		XYZ_DataSetAPI scatterData = getScatterData(datasetID, erfID, rupVarScenID, imTypeID,
				sourceID, rupID, hypo, isProbAt_IML, val);
		
		System.out.println("loaded "+scatterData.getX_DataSet().size()+" scatter vals");
		
		InterpDiffMapType[] types = { InterpDiffMapType.BASEMAP, InterpDiffMapType.INTERP_NOMARKS };
		InterpDiffMap map = new InterpDiffMap(region, baseMap, spacing, cpt, scatterData, interpSettings, types);
		map.setCustomLabel("3 Sec SA");
		map.setTopoResolution(TopographicSlopeFile.CA_THREE);
		map.setLogPlot(logPlot);
		map.setDpi(300);
		map.setXyzFileName("base_map.xyz");
		if (logPlot) {
			map.setCustomScaleMin(-1.7);
			map.setCustomScaleMax(-0.2);
		} else {
			map.setCustomScaleMin(0.02);
			map.setCustomScaleMax(0.6);
		}
		
		String metadata = "";
		
		System.out.println("Generating map...");
		String url = CS_InterpDiffMapServletAccessor.makeMap(null, map, metadata);
		
		String baseDir = "/home/kevin/CyberShake/M8/images/";
		PosterImageGen.saveCurves(url, baseDir, attenRel.getShortName(), true);
		PosterImageGen.saveCurves(url, baseDir, attenRel.getShortName()+"_plus_cybershake", false);
		
		System.out.println("Map address: "+url);
		
		System.exit(0);
	}

}
