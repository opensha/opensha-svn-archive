/**
 * 
 */
package scratch.peter.nshmp;

import static org.opensha.commons.mapping.gmt.GMT_MapGenerator.*;
import static org.opensha.nshmp2.tmp.TestGrid.*;
import static org.opensha.nshmp2.util.Period.*;
import static scratch.peter.curves.ProbOfExceed.*;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.opensha.commons.data.xyz.GeoDataSet;
import org.opensha.commons.exceptions.GMT_MapException;
import org.opensha.commons.geo.GriddedRegion;
import org.opensha.commons.mapping.gmt.GMT_MapGenerator;
import org.opensha.commons.mapping.gmt.elements.GMT_CPT_Files;
import org.opensha.commons.mapping.gmt.gui.GMT_MapGuiBean;
import org.opensha.commons.param.impl.CPTParameter;
import org.opensha.commons.util.FileUtils;
import org.opensha.nshmp2.tmp.TestGrid;
import org.opensha.nshmp2.util.Period;
import org.opensha.sha.gui.infoTools.ImageViewerWindow;
import org.opensha.sra.rtgm.RTGM;
import org.opensha.sra.rtgm.RTGM.Frequency;

import scratch.peter.curves.ProbOfExceed;

import com.google.common.io.Files;

/**
 *  Class of static methods for plotting USGS NSHMP and OpenSHA hazard data.
 *
 *
 * @author Peter Powers
 * @version $Id:$
 */
public class NSHMP_PlotUtils {

	private final static String DL_DIR = "/Users/pmpowers/Documents/OpenSHA/NSHMPdev2/figs/";

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
//		makeNationalPE("HazardTool", "DataR3", 0.05, GM0P20, PE2IN50);
//		makeNationalPE("DataR1", "DataR3", 0.05, GM0P20, PE2IN50);
//		makeNationalPE("FortranUpdate", "HazardTool", 0.05, GM1P00, PE2IN50);
		
		makeRegionalPE(LOS_ANGELES, "FortranUpdate", 0.1, GM0P00, PE2IN50);
		makeRegionalPE(LOS_ANGELES, "FortranUpdate", 0.1, GM0P20, PE2IN50);
		makeRegionalPE(LOS_ANGELES, "FortranUpdate", 0.1, GM1P00, PE2IN50);

//		makeRegionalPE(SAN_FRANCISCO, "FortranUpdate", 0.05, GM0P00, PE2IN50);
//		makeRegionalPE(SAN_FRANCISCO, "FortranUpdate", 0.05, GM0P20, PE2IN50);
//		makeRegionalPE(SAN_FRANCISCO, "FortranUpdate", 0.05, GM1P00, PE2IN50);

//		makeRegionalPE(SALT_LAKE_CITY, "FortranUpdate", 0.05, GM0P00, PE2IN50);
//		makeRegionalPE(SALT_LAKE_CITY, "FortranUpdate", 0.05, GM0P20, PE2IN50);
//		makeRegionalPE(SALT_LAKE_CITY, "FortranUpdate", 0.05, GM1P00, PE2IN50);

//		makeRegionalPE(SEATTLE, "FortranUpdate", 0.05, GM0P00, PE2IN50);
//		makeRegionalPE(SEATTLE, "FortranUpdate", 0.05, GM0P20, PE2IN50);
//		makeRegionalPE(SEATTLE, "FortranUpdate", 0.05, GM1P00, PE2IN50);

//		makeRegionalPE(MEMPHIS, "FortranUpdate", 0.05, GM0P00, PE2IN50);
//		makeRegionalPE(MEMPHIS, "FortranUpdate", 0.05, GM0P20, PE2IN50);
//		makeRegionalPE(MEMPHIS, "FortranUpdate", 0.05, GM1P00, PE2IN50);



		//		makeRegionalPE(SEATTLE, "FortranUpdate", 0.05, GM0P00, PE2IN50);
//		makeRegionalPE(LOS_ANGELES, "HazardTool", 0.1, GM1P00, PE2IN50);
//		makeRegionalPE(LOS_ANGELES, "FortranLatest", 0.1, GM1P00, PE2IN50);
	}
	

	
	/*
	 * Make a national scale prob. exceedance map with the supplied datasets.
	 */
	private static void makeNationalPE(String over, String under,
			double maxScale, Period p, ProbOfExceed pe) {
		String name = over + " over " + under + " " + pe + " " + p;
		GriddedRegion gr = NSHMP_UtilsDev.getNSHMP_Region(0.1);
		GeoDataSet xyz = NSHMP_GeoDataUtils.getPE_Ratio(over, under,
			p, pe, gr);
		NSHMP_GeoDataUtils.minusOne(xyz);
		makeRatioPlot(xyz, NATIONAL.bounds(), -maxScale, maxScale, name);
	}
	
	/*
	 * Make a regional over national plot
	 */
	private static void makeRegionalPE(TestGrid over, String under,
			double maxScale, Period p, ProbOfExceed pe) {
		String name = over + " over " + under + " " + pe + " " + p;
		GeoDataSet xyz = NSHMP_GeoDataUtils.getPE_Ratio(over, under,
			p, pe);
		NSHMP_GeoDataUtils.minusOne(xyz);
		makeRatioPlot(xyz, over.bounds(), -maxScale, maxScale, name);
	}

	

//	private static void makeNationalRTGM(String over, String under,
//			double maxScale, Frequency f) {
//		String name = over + " over " + under + " " + f;
//		GriddedRegion gr = NSHMP_UtilsDev.getNSHMP_Region(0.1);
//		GeoDataSet xyz = NSHMP_GeoDataUtils.getPE_Ratio(over, under, p, pe, gr);
//		NSHMP_GeoDataUtils.minusOne(xyz);
//		makeRatioPlot(xyz, NATIONAL.bounds(), -maxScale, maxScale, name);
//	}	
	
	private static void makeRatioPlot(GeoDataSet xyz, double[] bounds,
			double scaleMin, double scaleMax, String name) {
		GMT_MapGenerator map = create(bounds);
		map.setParameter(COLOR_SCALE_MIN_PARAM_NAME, scaleMin);
		map.setParameter(COLOR_SCALE_MAX_PARAM_NAME, scaleMax);
		CPTParameter cptParam = (CPTParameter) map.getAdjustableParamsList()
				.getParameter(CPT_PARAM_NAME);
			cptParam.setValue(GMT_CPT_Files.GMT_POLAR.getFileName());
		map.setParameter(LOG_PLOT_NAME, false);
		try {
			makeMap(xyz, map, name, "No metadata", DL_DIR + name + File.separator);
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		
	}
	
//	public static void makePolarPlot() {
//		GMT_MapGenerator map = getMapGenNational();
//		
//		//override default scale
//		map.setParameter(COLOR_SCALE_MIN_PARAM_NAME, -0.02);
//		map.setParameter(COLOR_SCALE_MAX_PARAM_NAME, 0.02);
////		map.setParameter(COLOR_SCALE_MIN_PARAM_NAME, 0.9);
////		map.setParameter(COLOR_SCALE_MAX_PARAM_NAME, 1.1);
//		
//		CPTParameter cptParam = (CPTParameter) map.getAdjustableParamsList()
//			.getParameter(CPT_PARAM_NAME);
//		cptParam.setValue(GMT_CPT_Files.GMT_POLAR.getFileName());
//
//		map.setParameter(LOG_PLOT_NAME, false);
//		
////		GeoDataSet xyz = SmoothSeismicitySpatialPDF_Fetcher.getUCERF3_PDF();
//		
////		GeoDataSet xyz = CurveComparisons.test();
//		GeoDataSet xyz = CurveComparisons.getFortranOverR3();
//		for (int i=0;i<xyz.size();i++) {
//			xyz.set(i, 1 - xyz.get(i));
//		}
//		
//		try {
//			makeMap(xyz, "2p50", "No metadata", "PP_test2", map);
//		} catch (IOException ioe) {
//			ioe.printStackTrace();
//		}
//	
//	}

		
	/**
	 * Make a friggin map.
	 * 
	 * @param xyz
	 * @param gmt_MapGenerator
	 * @param scaleLabel
	 * @param metadata
	 * @param dirName
	 * @param dlDir
	 * @throws IOException
	 */
	public static void makeMap(GeoDataSet xyz,
			GMT_MapGenerator gmt_MapGenerator, String scaleLabel,
			String metadata, String dlDir) throws IOException {
		try {
			if(makeMapOnServer) {
				String url = gmt_MapGenerator.makeMapUsingServlet(xyz, scaleLabel, metadata, null);
				metadata += GMT_MapGuiBean.getClickHereHTML(gmt_MapGenerator.getGMTFilesWebAddress());
				File zipFile = new File(dlDir, "allFiles.zip");
				Files.createParentDirs(zipFile);
				// construct zip URL
				String zipURL = url.substring(0, url.lastIndexOf('/')+1)+"allFiles.zip";
				FileUtils.downloadURL(zipURL, zipFile);
				FileUtils.unzipFile(zipFile, new File(dlDir));
				new ImageViewerWindow(url,metadata, true);
			} else {
				gmt_MapGenerator.makeMapLocally(xyz, scaleLabel, metadata, dlDir);
			}
		} catch (GMT_MapException e) {
			e.printStackTrace();
		}
	}


	
	/*
	 ************************************************************************
	 * 
	 * Basic map generator creation
	 * 
	 ************************************************************************
	 */
		
	private final static boolean makeMapOnServer = true;
	private final static double spacing = 0.1;
	private final static String scaling = COLOR_SCALE_MODE_MANUALLY;
	private final static double minScale = -1.0;
	private final static double maxScale = 1.0;
	private final static String topoRes = TOPO_RESOLUTION_NONE;
	private final static String showHwy = SHOW_HIWYS_NONE;
	private final static String showCoast = COAST_DRAW;
	private final static double imgWidth = 6.5;
	private final static boolean smooth = false;
	private final static boolean bgBlack = false;
	
	/**
	 * MapGenerator creator.
	 * @param bounds
	 * @return a map generator
	 */
	public static GMT_MapGenerator create(double[] bounds) {
		GMT_MapGenerator map = new GMT_MapGenerator();
		CPTParameter cptParam = (CPTParameter) map.getAdjustableParamsList()
			.getParameter(CPT_PARAM_NAME);
		cptParam.setValue(GMT_CPT_Files.GMT_OCEAN2.getFileName());
		map.setParameter(GRID_SPACING_PARAM_NAME, spacing);
		map.setParameter(COLOR_SCALE_MODE_NAME, scaling);
		map.setParameter(COLOR_SCALE_MIN_PARAM_NAME, minScale);
		map.setParameter(COLOR_SCALE_MAX_PARAM_NAME, maxScale);
		map.setParameter(TOPO_RESOLUTION_PARAM_NAME, topoRes);
		map.setParameter(SHOW_HIWYS_PARAM_NAME, showHwy);
		map.setParameter(COAST_PARAM_NAME, showCoast);
		map.setParameter(IMAGE_WIDTH_NAME, imgWidth);
		map.setParameter(GMT_SMOOTHING_PARAM_NAME, smooth);
		map.setParameter(BLACK_BACKGROUND_PARAM_NAME, bgBlack);
		initBounds(map, bounds);
		return map;
	}
	
	private static void initBounds(GMT_MapGenerator map, double[] bounds) {
		map.setParameter(MIN_LAT_PARAM_NAME, bounds[0]);
		map.setParameter(MIN_LON_PARAM_NAME, bounds[2]);
		map.setParameter(MAX_LAT_PARAM_NAME, bounds[1]);
		map.setParameter(MAX_LON_PARAM_NAME, bounds[3]);
	}
	
}

//	// DataR3 over HazardTool datas
//private static void makeR1overR3(double maxScale, Period p) {
//	String name = "DataR1 over DataR3 " + PE2IN50 + " " + p;
//	GriddedRegion gr = NSHMP_UtilsDev.getNSHMP_Region(0.1);
//	GeoDataSet xyz = NSHMP_GeoDataUtils.getPE_Ratio("DataR1", "DataR3",
//		p, PE2IN50, gr);
//	for (int i = 0; i < xyz.size(); i++) {
//		xyz.set(i, 1 - xyz.get(i));
//	}
//	makeRatioPlot(xyz, MapExtent.NATIONAL, -maxScale, maxScale, name);
//}
//
//// DataR3 over HazardTool datas
//private static void makeHToverR3(double maxScale, Period p) {
//	String name = "HazardTool over DataR3 " + PE2IN50 + " " + p;
//	GriddedRegion gr = NSHMP_UtilsDev.getNSHMP_Region(0.1);
//	GeoDataSet xyz = NSHMP_GeoDataUtils.getPE_Ratio("HazardTool", "DataR3",
//		p, PE2IN50, gr);
//	for (int i=0;i<xyz.size();i++) {
//		xyz.set(i, 1 - xyz.get(i));
//	}
//	makeRatioPlot(xyz, MapExtent.NATIONAL, -maxScale, maxScale, name);
//}
//
//// FortranLatest over R3 data
//private static void makeFoverHT(double maxScale, Period p) {
//	String name = "Fortran over HazardTool " + PE2IN50 + " " + p;
//	GriddedRegion gr = NSHMP_UtilsDev.getNSHMP_Region(0.1);
//	GeoDataSet xyz = NSHMP_GeoDataUtils.getPE_Ratio("FortranLatest", "HazardTool",
//		p, PE2IN50, gr);
//	for (int i=0;i<xyz.size();i++) {
//		xyz.set(i, 1 - xyz.get(i));
//	}
//	makeRatioPlot(xyz, MapExtent.NATIONAL, -maxScale, maxScale, name);
//}


