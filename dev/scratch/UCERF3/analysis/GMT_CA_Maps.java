package scratch.UCERF3.analysis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.opensha.commons.data.region.CaliforniaRegions;
import org.opensha.commons.data.xyz.GeoDataSet;
import org.opensha.commons.data.xyz.GeoDataSetMath;
import org.opensha.commons.data.xyz.GriddedGeoDataSet;
import org.opensha.commons.data.xyz.XYZ_DataSet;
import org.opensha.commons.data.xyz.XYZ_DataSetMath;
import org.opensha.commons.exceptions.GMT_MapException;
import org.opensha.commons.geo.GriddedRegion;
import org.opensha.commons.mapping.gmt.GMT_MapGenerator;
import org.opensha.commons.mapping.gmt.elements.GMT_CPT_Files;
import org.opensha.commons.mapping.gmt.gui.GMT_MapGuiBean;
import org.opensha.commons.param.constraint.impl.ListBasedConstraint;
import org.opensha.commons.param.impl.CPTParameter;
import org.opensha.commons.util.ListUtils;
import org.opensha.commons.util.cpt.CPT;
import org.opensha.sha.earthquake.AbstractERF;
import org.opensha.sha.earthquake.ERF;
import org.opensha.sha.earthquake.calc.ERF_Calculator;
import org.opensha.sha.gui.infoTools.ImageViewerWindow;

import scratch.UCERF3.FaultSystemSolution;
import scratch.UCERF3.enumTreeBranches.FaultModelBranches;
import scratch.UCERF3.erf.FaultSystemSolutionPoissonERF;
import scratch.UCERF3.utils.FindEquivUCERF2_Ruptures.FindEquivUCERF2_FM3_Ruptures;
import scratch.UCERF3.utils.ModUCERF2.ModMeanUCERF2;


/**
 * This is for making GMT maps of nucleation rates, participation rates, or ratios of these.
 * 
 * To Do: 
 * 
 * 1) add option for non-black background in plots
 * 
 * 2) fix CPT parameter problem (can't set one, but can only use default)
 * 
 * 3) add participation rate plotting
 * 
 * 
 * @author field
 *
 *
 */
public class GMT_CA_Maps {
	
	final static boolean makeMapOnServer = false;
		
	final static double defaultMinLat = 31.5;
	final static double defaultMaxLat = 43.0;
	final static double defaultMinLon = -125.4;
	final static double defaultMaxLon = -113.1;
	final static double defaultGridSpacing = 0.1;
	final static String defaultCPT = GMT_CPT_Files.MAX_SPECTRUM.getFileName();
	final static String defaultColorScaleLimits=GMT_MapGenerator.COLOR_SCALE_MODE_MANUALLY;
	final static double defaultColorScaleMin = -5.5;
	final static double defaultColorScaleMax = -1.5;
	final static String defaultTopoResolution = GMT_MapGenerator.TOPO_RESOLUTION_NONE;
	final static String defaultShowHighways = GMT_MapGenerator.SHOW_HIWYS_NONE;
	final static String defaultCoast = GMT_MapGenerator.COAST_DRAW;
	final static double defaultImageWidth = 6.5; 
	final static boolean defaultApplyGMT_Smoothing = false;
	final static CaliforniaRegions.RELM_TESTING_GRIDDED defaultGridRegion  = new CaliforniaRegions.RELM_TESTING_GRIDDED();

	
	
	private static GMT_MapGenerator getDefaultGMT_MapGenerator() {
		
		GMT_MapGenerator gmt_MapGenerator = new GMT_MapGenerator();
		
//		CPTParameter cptParam = (CPTParameter )gmt_MapGenerator.getAdjustableParamsList().getParameter(GMT_MapGenerator.CPT_PARAM_NAME);
//		Collection<CPT> cpts = ((ListBasedConstraint<CPT>) cptParam.getConstraint()).getAllowed();
//		for(CPT cpt:cpts)
//			System.out.println(cpt.getName());
		
//		CPT cpt = getCPT_instance(defaultCPT);
//		System.out.println("cpt.getName()="+cpt.getName());
//		System.out.println("cpt.toString()="+cpt.toString());

		
		gmt_MapGenerator.setParameter(GMT_MapGenerator.MIN_LAT_PARAM_NAME, defaultMinLat);
		gmt_MapGenerator.setParameter(GMT_MapGenerator.MIN_LON_PARAM_NAME, defaultMinLon);
		gmt_MapGenerator.setParameter(GMT_MapGenerator.MAX_LAT_PARAM_NAME, defaultMaxLat);
		gmt_MapGenerator.setParameter(GMT_MapGenerator.MAX_LON_PARAM_NAME, defaultMaxLon);
		gmt_MapGenerator.setParameter(GMT_MapGenerator.GRID_SPACING_PARAM_NAME, defaultGridSpacing);
//		gmt_MapGenerator.setParameter(GMT_MapGenerator.CPT_PARAM_NAME, cpt);
		gmt_MapGenerator.setParameter(GMT_MapGenerator.COLOR_SCALE_MODE_NAME, defaultColorScaleLimits);
		gmt_MapGenerator.setParameter(GMT_MapGenerator.COLOR_SCALE_MIN_PARAM_NAME, defaultColorScaleMin);
		gmt_MapGenerator.setParameter(GMT_MapGenerator.COLOR_SCALE_MAX_PARAM_NAME, defaultColorScaleMax);
		gmt_MapGenerator.setParameter(GMT_MapGenerator.TOPO_RESOLUTION_PARAM_NAME, defaultTopoResolution);
		gmt_MapGenerator.setParameter(GMT_MapGenerator.SHOW_HIWYS_PARAM_NAME, defaultShowHighways);
		gmt_MapGenerator.setParameter(GMT_MapGenerator.COAST_PARAM_NAME, defaultCoast);
		gmt_MapGenerator.setParameter(GMT_MapGenerator.IMAGE_WIDTH_NAME, defaultImageWidth);
		gmt_MapGenerator.setParameter(GMT_MapGenerator.GMT_SMOOTHING_PARAM_NAME, defaultApplyGMT_Smoothing);
		
		return gmt_MapGenerator;

	}
	
	private static void makeMap(GeoDataSet geoDataSet, String scaleLabel, String metadata, 
			String dirName, GMT_MapGenerator gmt_MapGenerator) {
		
		try {
			String name;
			if(makeMapOnServer) {
				name = gmt_MapGenerator.makeMapUsingServlet(geoDataSet, scaleLabel, metadata, dirName);
				metadata += GMT_MapGuiBean.getClickHereHTML(gmt_MapGenerator.getGMTFilesWebAddress());
				ImageViewerWindow imgView = new ImageViewerWindow(name,metadata, true);				
			}
			else {
				name = gmt_MapGenerator.makeMapLocally(geoDataSet, scaleLabel, metadata, dirName);
			}
		} catch (GMT_MapException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	

	
	
	/**
	 * This makes a map of the log10 participation rates for the given GeoDataSet
	 * 
	 * @param geoDataSet
	 * @param scaleLabel
	 * @param metadata
	 * @param dirName
	 */
	public static void plotNucleationRateMap(GeoDataSet geoDataSet, String scaleLabel,
			String metadata, String dirName) {
		makeMap(geoDataSet, scaleLabel, metadata, dirName, getDefaultGMT_MapGenerator());
	}
	
	/**
	 * This returns and instance of the RELM gridded region used here
	 * @return
	 */
	public static GriddedRegion getDefaultGriddedRegion() {
		return defaultGridRegion;
	}
	
	
	/**
	 * This makes a map of the log10(ratio) of the participation rates for geoDataSet1 divided by geoDataSet2
	 * 
	 * @param geoDataSet1
	 * @param geoDataSet2
	 * @param scaleLabel
	 * @param metadata
	 * @param dirName
	 */
	public static void plotRatioOfRateMaps(GeoDataSet geoDataSet1, GeoDataSet geoDataSet2, String scaleLabel,
			String metadata, String dirName) {
		
		GeoDataSet ratioGeoDataSet = GeoDataSetMath.divide(geoDataSet1, geoDataSet2);

		GMT_MapGenerator gmt_MapGenerator = getDefaultGMT_MapGenerator();
		
		//override default scale
		gmt_MapGenerator.setParameter(GMT_MapGenerator.COLOR_SCALE_MIN_PARAM_NAME, -2.0);
		gmt_MapGenerator.setParameter(GMT_MapGenerator.COLOR_SCALE_MAX_PARAM_NAME, 2.0);

		makeMap(ratioGeoDataSet, scaleLabel, metadata, dirName, gmt_MapGenerator);	
		
		// restore default values
		gmt_MapGenerator.setParameter(GMT_MapGenerator.COLOR_SCALE_MIN_PARAM_NAME, defaultColorScaleMin);
		gmt_MapGenerator.setParameter(GMT_MapGenerator.COLOR_SCALE_MAX_PARAM_NAME, defaultColorScaleMax);
	}
	

	
	/**
	 * This makes a map of the log10 participation rates for events from the given erf 
	 * with mag>=minMag and mag<maxMag
	 * @param erf
	 * @param minMag
	 * @param maxMag
	 * @param scaleLabel
	 * @param metadata
	 * @param dirName
	 */
	public static void plotNucleationRateMap(ERF erf, double minMag, double maxMag, String scaleLabel,
			String metadata, String dirName) {
		
		GriddedGeoDataSet geoDataSet = ERF_Calculator.getNucleationRatesInRegion(erf, defaultGridRegion, minMag, maxMag);
		
		plotNucleationRateMap(geoDataSet, scaleLabel, metadata, dirName);
		
	}
	
	
	/**
	 * This makes a map of the log10 participation rates for events from the given erf 
	 * with mag>=minMag and mag<maxMag
	 * @param erf
	 * @param minMag
	 * @param maxMag
	 * @param scaleLabel
	 * @param metadata
	 * @param dirName
	 */
	public static void plotNucleationRateMap(FaultSystemSolution faultSysSolution, double minMag, double maxMag, String scaleLabel,
			String metadata, String dirName) {
		FaultSystemSolutionPoissonERF erf = new FaultSystemSolutionPoissonERF(faultSysSolution);
		erf.updateForecast();
		plotNucleationRateMap(erf, minMag, maxMag, scaleLabel, metadata, dirName);
	}


	
	private static CPT getCPT_instance(String name) {
		CPT cpt = null;
		try {
			ArrayList<CPT> cpts = GMT_CPT_Files.instances();
//			System.out.println("cpts.size()="+cpts.size());
			cpt = cpts.get(ListUtils.getIndexByName(cpts,name));
//			System.out.println("cpt.getName()="+cpt.getName());
//			System.out.println("cpt.toString()="+cpt.toString());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return cpt;

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("Getting ERF");
		ERF modMeanUCERF2 = FindEquivUCERF2_FM3_Ruptures.buildERF(FaultModelBranches.FM3_1);
		System.out.println("Making xyzData");
//		GriddedGeoDataSet geoDataSet = ERF_Calculator.getNucleationRatesInRegion(modMeanUCERF2, defaultGridRegion, 0, 10);

		GMT_CA_Maps.plotNucleationRateMap(modMeanUCERF2, 0, 10, "TEST", "test meta data", "test_GMT_Maps");
//		GMT_CA_Maps.plotNucleationRateMap(geoDataSet, "TEST", "test meta data", "test_GMT_Maps");

//		System.out.println("Plotting ratio");
//		GMT_CA_Maps.plotRatioOfRateMaps(geoDataSet, geoDataSet, "TEST", "test meta data", "testRatio_GMT_Maps");
		System.out.println("Done");

	}

}
