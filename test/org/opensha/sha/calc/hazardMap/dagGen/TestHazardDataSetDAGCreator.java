package org.opensha.sha.calc.hazardMap.dagGen;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.junit.BeforeClass;
import org.junit.Test;
import org.opensha.commons.data.Site;
import org.opensha.commons.data.region.CaliforniaRegions;
import org.opensha.commons.data.siteData.OrderedSiteDataProviderList;
import org.opensha.commons.data.siteData.SiteDataValue;
import org.opensha.commons.data.siteData.SiteDataValueList;
import org.opensha.commons.geo.GriddedRegion;
import org.opensha.commons.geo.Location;
import org.opensha.commons.gridComputing.condor.SubmitScript.Universe;
import org.opensha.commons.param.Parameter;
import org.opensha.commons.util.FileUtils;
import org.opensha.commons.util.XMLUtils;
import org.opensha.sha.calc.hazardMap.TestHazardCurveSetCalculator;
import org.opensha.sha.calc.hazardMap.components.AsciiFileCurveArchiver;
import org.opensha.sha.calc.hazardMap.components.CalculationInputsXMLFile;
import org.opensha.sha.calc.hazardMap.components.CalculationSettings;
import org.opensha.sha.calc.hazardMap.components.CurveResultsArchiver;
import org.opensha.sha.earthquake.EqkRupForecastAPI;
import org.opensha.sha.earthquake.rupForecastImpl.Frankel96.Frankel96_AdjustableEqkRupForecast;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.MeanUCERF2.MeanUCERF2;
import org.opensha.sha.gui.infoTools.IMT_Info;
import org.opensha.sha.imr.ScalarIMR;
import org.opensha.sha.imr.attenRelImpl.CB_2008_AttenRel;
import org.opensha.sha.imr.param.IntensityMeasureParams.SA_Param;
import org.opensha.sha.imr.param.SiteParams.DepthTo2pt5kmPerSecParam;
import org.opensha.sha.imr.param.SiteParams.Vs30_Param;
import org.opensha.sha.util.SiteTranslator;
import org.opensha.sha.util.TectonicRegionType;

public class TestHazardDataSetDAGCreator {
	
	protected static EqkRupForecastAPI erf;
	protected static ArrayList<HashMap<TectonicRegionType, ScalarIMR>> imrMaps;
	protected static ArrayList<Site> sites;
	protected static CalculationSettings calcSettings;
	protected static CurveResultsArchiver archiver;
	protected static File tempDir;
	protected static GriddedRegion region;
	
	public static double spacing = 0.1;

	@BeforeClass
	public static void setUp() throws Exception {
//		erf = new Frankel96_AdjustableEqkRupForecast();
		erf = new MeanUCERF2();
		
		CB_2008_AttenRel cb08 = new CB_2008_AttenRel(null);
		cb08.setParamDefaults();
		cb08.getParameter(DepthTo2pt5kmPerSecParam.NAME).setValue(null);
		cb08.setIntensityMeasure(SA_Param.NAME);
		SA_Param.setPeriodInSA_Param(cb08.getIntensityMeasure(), 3.0);
		
		imrMaps = new ArrayList<HashMap<TectonicRegionType,ScalarIMR>>();
		HashMap<TectonicRegionType,ScalarIMR> imrMap =
			new HashMap<TectonicRegionType, ScalarIMR>();
		imrMap.put(TectonicRegionType.ACTIVE_SHALLOW, cb08);
		imrMaps.add(imrMap);
		
		region = new CaliforniaRegions.RELM_TESTING_GRIDDED(spacing);
		
		sites = new ArrayList<Site>();
		for (Location loc : region) {
			Site site = new Site(loc);
			
			site.addParameter((Parameter)cb08.getParameter(Vs30_Param.NAME).clone());
			site.addParameter((Parameter)cb08.getParameter(DepthTo2pt5kmPerSecParam.NAME).clone());
			
			sites.add(site);
		}
		OrderedSiteDataProviderList provs = OrderedSiteDataProviderList.createSiteDataProviderDefaults();
		ArrayList<SiteDataValueList<?>> vals  = provs.getAllAvailableData(sites);
		
		SiteTranslator trans = new SiteTranslator();
		for (int i=0; i<sites.size(); i++) {
			ArrayList<SiteDataValue<?>> siteVals = new ArrayList<SiteDataValue<?>>();
			for (SiteDataValueList<?> valList : vals) {
				siteVals.add(valList.getValue(i));
			}
			Iterator<Parameter<?>> it = sites.get(i).getParametersIterator();
			while (it.hasNext()) {
				trans.setParameterValue(it.next(), siteVals);
			}
		}
		
		calcSettings = new CalculationSettings(IMT_Info.getUSGS_SA_Function(), 200);
		tempDir = FileUtils.createTempDir();
		String curvesDir = tempDir.getAbsolutePath();
		if (!curvesDir.endsWith(File.separator))
			curvesDir += File.separator;
		curvesDir += "curves";
		archiver = new AsciiFileCurveArchiver(curvesDir, true, false);
	}
	
	@Test
	public void testDAGCreation() throws IOException {
		CalculationInputsXMLFile inputs = new CalculationInputsXMLFile(erf, imrMaps, sites, calcSettings, archiver);
		
		XMLUtils.writeObjectToXMLAsRoot(inputs, tempDir.getAbsolutePath() + File.separator + "inputs.xml");
		
		String javaExec = "/auto/usc/jdk/1.6.0/jre/bin/java";
		String pwd = System.getProperty("user.dir");
//		String jarFile = pwd + File.separator + "dist" + File.separator + "OpenSHA_complete.jar";
		String jarFile = "/home/scec-00/kmilner/hazMaps/svn/dist/OpenSHA_complete.jar";
//		File jarFileFile = new File(jarFile);
//		if (!jarFileFile.exists())
//			throw new FileNotFoundException("Jar file 'OpenSHA_complete.jar' doesn't exist..." +
//					"run ant/CompleteJar.xml to build this jar");
		System.out.println("Jar file: " + jarFile);
		
		HazardDataSetDAGCreator dagCreator = new HazardDataSetDAGCreator(inputs, javaExec, jarFile);
		
		dagCreator.setUniverse(Universe.VANILLA);
		dagCreator.writeDAG(tempDir, 20, false);
	}
	
	public static void main(String args[]) throws Exception {
		if (args.length > 0) {
			double spacing = Double.parseDouble(args[0]);
			TestHazardDataSetDAGCreator.spacing = spacing;
		}
		TestHazardDataSetDAGCreator.setUp();
		TestHazardDataSetDAGCreator test = new TestHazardDataSetDAGCreator();
		test.testDAGCreation();
	}

}
