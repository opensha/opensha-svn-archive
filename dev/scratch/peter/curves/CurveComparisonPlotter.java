package scratch.peter.curves;

import static org.opensha.commons.mapping.gmt.GMT_MapGenerator.*;

import java.io.File;
import java.io.IOException;

import org.opensha.commons.data.xyz.GeoDataSet;
import org.opensha.commons.exceptions.GMT_MapException;
import org.opensha.commons.geo.GriddedRegion;
import org.opensha.commons.geo.Location;
import org.opensha.commons.mapping.gmt.GMT_MapGenerator;
import org.opensha.commons.mapping.gmt.elements.GMT_CPT_Files;
import org.opensha.commons.mapping.gmt.gui.GMT_MapGuiBean;
import org.opensha.commons.param.impl.CPTParameter;
import org.opensha.commons.util.FileUtils;
import org.opensha.sha.gui.infoTools.ImageViewerWindow;

import scratch.UCERF3.analysis.GMT_CA_Maps;
import scratch.UCERF3.utils.SmoothSeismicitySpatialPDF_Fetcher;
import scratch.peter.nshmp.NSHMP_GeoDataUtils;
import scratch.peter.nshmp.NSHMP_PlotUtils;

/**
 * Add comments here
 *
 * 
 * @author Peter Powers
 * @version $Id:$
 */
@Deprecated
public class CurveComparisonPlotter {


	
//	public static void testPlot() {
//		GMT_MapGenerator map = GMT_CA_Maps.getDefaultGMT_MapGenerator();
//		
//		//override default scale
//		map.setParameter(GMT_MapGenerator.COLOR_SCALE_MIN_PARAM_NAME, 0.0);
//		map.setParameter(GMT_MapGenerator.COLOR_SCALE_MAX_PARAM_NAME, 2.0);
//		
//		CPTParameter cptParam = (CPTParameter) map.getAdjustableParamsList()
//			.getParameter(CPT_PARAM_NAME);
//		cptParam.setValue(GMT_CPT_Files.GMT_NO_GREEN.getFileName());
//
//		map.setParameter(LOG_PLOT_NAME, false);
//		
////		GeoDataSet xyz = SmoothSeismicitySpatialPDF_Fetcher.getUCERF3_PDF();
//		
//		GeoDataSet xyz = CurveComparisons.test();
//		
//		try {
//			makeMap(xyz, "2p50", "No metadata", "PP_test2", map);
//		} catch (IOException ioe) {
//			ioe.printStackTrace();
//		}
//	
//	}
	
	public static void testPlot2() {
		GMT_MapGenerator map = GMT_CA_Maps.getDefaultGMT_MapGenerator();
		
		//override default scale
		map.setParameter(GMT_MapGenerator.COLOR_SCALE_MIN_PARAM_NAME, -0.05);
		map.setParameter(GMT_MapGenerator.COLOR_SCALE_MAX_PARAM_NAME, 0.05);
//		map.setParameter(GMT_MapGenerator.COLOR_SCALE_MIN_PARAM_NAME, -8.0);
//		map.setParameter(GMT_MapGenerator.COLOR_SCALE_MAX_PARAM_NAME, -1.0);

		CPTParameter cptParam = (CPTParameter) map.getAdjustableParamsList()
			.getParameter(CPT_PARAM_NAME);
		cptParam.setValue(GMT_CPT_Files.GMT_POLAR.getFileName());

		map.setParameter(LOG_PLOT_NAME, false);
		
//		GeoDataSet xyz = SmoothSeismicitySpatialPDF_Fetcher.getUCERF3_PDF();
		
//		GeoDataSet xyz = CurveComparisons.test();
		GeoDataSet xyz = null; //NSHMP_GeoDataUtils.getFortranOverR3();
		for (int i=0;i<xyz.size();i++) {
			xyz.set(i, 1 - xyz.get(i));
		}
		
		try {
			GMT_CA_Maps.makeMap(xyz,  "2p50", "No metadata", "PP_test2", map);
//			makeMap(xyz,);
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	
	}


	public static void main(String[] args) {
		testPlot2();
	}
}
