package scratch.peter.curves;

import static org.opensha.commons.mapping.gmt.GMT_MapGenerator.*;

import java.io.File;
import java.io.IOException;

import org.opensha.commons.data.xyz.GeoDataSet;
import org.opensha.commons.exceptions.GMT_MapException;
import org.opensha.commons.mapping.gmt.GMT_MapGenerator;
import org.opensha.commons.mapping.gmt.elements.GMT_CPT_Files;
import org.opensha.commons.mapping.gmt.gui.GMT_MapGuiBean;
import org.opensha.commons.param.impl.CPTParameter;
import org.opensha.commons.util.FileUtils;
import org.opensha.sha.gui.infoTools.ImageViewerWindow;

import scratch.UCERF3.analysis.GMT_CA_Maps;
import scratch.UCERF3.utils.SmoothSeismicitySpatialPDF_Fetcher;

/**
 * Add comments here
 *
 * 
 * @author Peter Powers
 * @version $Id:$
 */
public class CurveComparisonPlotter {

	final static boolean makeMapOnServer = true;
	final static String dlDir = "/Users/pmpowers/Documents/OpenSHA/NSHMPdev2/";

	private final static double[] natBounds = { 24.6, 50.0, -125.0, -65.0 };
	private final static double[] caBounds = { 31.5, 43.0, -125.4, -113.1 };
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
	
	
	
	public static GMT_MapGenerator getNational() {
		GMT_MapGenerator map = create(natBounds);
		return map;
	}

	public static GMT_MapGenerator getCA() {
		GMT_MapGenerator map = create(caBounds);
		return map;
	}
	
	private static GMT_MapGenerator create(double[] bounds) {
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
		map.setParameter(MIN_LON_PARAM_NAME, bounds[1]);
		map.setParameter(MAX_LAT_PARAM_NAME, bounds[2]);
		map.setParameter(MAX_LON_PARAM_NAME, bounds[3]);
	}
	
	private static void makeMap(GeoDataSet geoDataSet, String scaleLabel, String metadata, 
			String dirName, GMT_MapGenerator gmt_MapGenerator) throws IOException {
		
		try {
			if(makeMapOnServer) {
				new File(dlDir).mkdir();
				String url = gmt_MapGenerator.makeMapUsingServlet(geoDataSet, scaleLabel, metadata, null);
				metadata += GMT_MapGuiBean.getClickHereHTML(gmt_MapGenerator.getGMTFilesWebAddress());
				File downloadDir = new File(dlDir, dirName);
				if (!downloadDir.exists())
					downloadDir.mkdir();
				File zipFile = new File(downloadDir, "allFiles.zip");
				// construct zip URL
				String zipURL = url.substring(0, url.lastIndexOf('/')+1)+"allFiles.zip";
				FileUtils.downloadURL(zipURL, zipFile);
				FileUtils.unzipFile(zipFile, downloadDir);
				
				ImageViewerWindow imgView = new ImageViewerWindow(url,metadata, true);
			}
			else {
				gmt_MapGenerator.makeMapLocally(geoDataSet, scaleLabel, metadata, dirName);
			}
		} catch (GMT_MapException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	
	public static void testPlot() {
		GMT_MapGenerator map = GMT_CA_Maps.getDefaultGMT_MapGenerator();
		
		//override default scale
		map.setParameter(GMT_MapGenerator.COLOR_SCALE_MIN_PARAM_NAME, 0.0);
		map.setParameter(GMT_MapGenerator.COLOR_SCALE_MAX_PARAM_NAME, 2.0);
		
		CPTParameter cptParam = (CPTParameter) map.getAdjustableParamsList()
			.getParameter(CPT_PARAM_NAME);
		cptParam.setValue(GMT_CPT_Files.GMT_NO_GREEN.getFileName());

		map.setParameter(LOG_PLOT_NAME, false);
		
//		GeoDataSet xyz = SmoothSeismicitySpatialPDF_Fetcher.getUCERF3_PDF();
		
		GeoDataSet xyz = CurveComparisons.test();
		
		try {
			makeMap(xyz, "2p50", "No metadata", "PP_test2", map);
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	
	}
	
	public static void testPlot2() {
		GMT_MapGenerator map = GMT_CA_Maps.getDefaultGMT_MapGenerator();
		
		//override default scale
		map.setParameter(GMT_MapGenerator.COLOR_SCALE_MIN_PARAM_NAME, -0.05);
		map.setParameter(GMT_MapGenerator.COLOR_SCALE_MAX_PARAM_NAME, 0.05);
		
		CPTParameter cptParam = (CPTParameter) map.getAdjustableParamsList()
			.getParameter(CPT_PARAM_NAME);
		cptParam.setValue(GMT_CPT_Files.GMT_POLAR.getFileName());

		map.setParameter(LOG_PLOT_NAME, false);
		
//		GeoDataSet xyz = SmoothSeismicitySpatialPDF_Fetcher.getUCERF3_PDF();
		
//		GeoDataSet xyz = CurveComparisons.test();
		GeoDataSet xyz = CurveComparisons.getFortranOverR3();
		for (int i=0;i<xyz.size();i++) {
			xyz.set(i, 1 - xyz.get(i));
		}
		
		try {
			makeMap(xyz, "2p50", "No metadata", "PP_test2", map);
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	
	}

	public static void main(String[] args) {
		testPlot2();
	}
}
