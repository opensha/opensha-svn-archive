package scratch.UCERF3.analysis;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipException;

import org.apache.commons.lang3.ArrayUtils;
import org.dom4j.DocumentException;
import org.opensha.commons.data.region.CaliforniaRegions;
import org.opensha.commons.exceptions.GMT_MapException;
import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.Region;
import org.opensha.commons.mapping.gmt.GMT_Map;
import org.opensha.commons.mapping.gmt.GMT_MapGenerator;
import org.opensha.commons.mapping.gmt.elements.PSXYPolygon;
import org.opensha.commons.mapping.gmt.gui.GMT_MapGuiBean;
import org.opensha.commons.util.FileUtils;
import org.opensha.commons.util.cpt.CPT;
import org.opensha.commons.util.cpt.CPTVal;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.opensha.sha.faultSurface.FaultTrace;
import org.opensha.sha.gui.infoTools.ImageViewerWindow;

import com.google.common.base.Preconditions;

import scratch.UCERF3.FaultSystemSolution;
import scratch.UCERF3.SimpleFaultSystemSolution;
import scratch.UCERF3.utils.UCERF3_DataUtils;

public class FaultBasedMapGen {
	
	private static GMT_MapGenerator gmt;
	
	public static CPT getSlipRateCPT() {
		CPT cpt = new CPT();

		cpt.setBelowMinColor(Color.GRAY);
		cpt.setNanColor(Color.GRAY);

//		cpt.add(new CPTVal(0f, Color.GRAY, 0f, Color.GRAY));
		cpt.add(new CPTVal(Float.MIN_VALUE, Color.BLUE, 10f, Color.MAGENTA));
		cpt.add(new CPTVal(10f, Color.MAGENTA, 20f, Color.RED));
		cpt.add(new CPTVal(20f, Color.RED, 30f, Color.ORANGE));
		cpt.add(new CPTVal(30f, Color.ORANGE, 40f, Color.YELLOW));

		cpt.setAboveMaxColor(Color.YELLOW);

		return cpt;
	}
	
	public static void plotSolutionSlipRates(FaultSystemSolution sol, Region region, File saveDir, boolean display) throws GMT_MapException, RuntimeException {
		CPT cpt = getSlipRateCPT();
		List<FaultSectionPrefData> faults = sol.getFaultSectionDataList();
		double[] values = scale(sol.calcSlipRateForAllSects(), 1e3); // to mm
		
		makeFaultPlot(cpt, faults, values, region, saveDir, display);
	}
	
	public static double[] scale(double[] values, double scalar) {
		double[] ret = new double[values.length];
		for (int i=0; i<values.length; i++)
			ret[i] = values[i] * scalar;
		return ret;
	}
	
	private synchronized static void makeFaultPlot(CPT cpt, List<FaultSectionPrefData> faults, double[] values, Region region,
			File saveDir, boolean display) throws GMT_MapException, RuntimeException {
		GMT_Map map = new GMT_Map(region, null, 1, cpt);
		
		map.setRescaleCPT(false);
		map.setCustomScaleMin((double)cpt.getMinValue());
		map.setCustomScaleMax((double)cpt.getMaxValue());
		
		Preconditions.checkState(faults.size() == values.length, "faults and values are different lengths!");
		
		for (int i=0; i<faults.size(); i++) {
			FaultSectionPrefData fault = faults.get(i);
			Color c = cpt.getColor((float)values[i]);
			for (PSXYPolygon poly : getPolygons(fault, c))
				map.addPolys(poly);
		}
		
		if (gmt == null)
			gmt = new GMT_MapGenerator();
		
		String url = gmt.makeMapUsingServlet(map, "metadata", null);
		String metadata = GMT_MapGuiBean.getClickHereHTML(gmt.getGMTFilesWebAddress());
//		File zipFile = new File(downloadDir, "allFiles.zip");
//		// construct zip URL
//		String zipURL = url.substring(0, url.lastIndexOf('/')+1)+"allFiles.zip";
//		FileUtils.downloadURL(zipURL, zipFile);
//		FileUtils.unzipFile(zipFile, downloadDir);
		
		if (display) {
			new ImageViewerWindow(url,metadata, true);
		}
	}
	
	private static ArrayList<PSXYPolygon> getPolygons(FaultSectionPrefData fault, Color c) {
		ArrayList<PSXYPolygon> polys = new ArrayList<PSXYPolygon>();
		FaultTrace trace = fault.getFaultTrace();
		
		for (int i=1; i<trace.size(); i++) {
			Location loc1 = trace.get(i-1);
			Location loc2 = trace.get(i);
			PSXYPolygon poly = new PSXYPolygon(loc1, loc2);
			poly.setPenColor(c);
			poly.setPenWidth(8);
			polys.add(poly);
		}
		
		return polys;
	}
	
	public static void main(String[] args) throws IOException, DocumentException, GMT_MapException, RuntimeException {
		File invSolsDir = new File(UCERF3_DataUtils.DEFAULT_SCRATCH_DATA_DIR, "InversionSolutions");
		File solFile = new File(invSolsDir, "FM3_1_GLpABM_MaEllB_DsrTap_DrEllB_Char_VarAseis0.2_VarOffAseis0.5_VarMFDMod1_VarNone_sol.zip");
		FaultSystemSolution sol = SimpleFaultSystemSolution.fromZipFile(solFile);
		
		Region region = new CaliforniaRegions.RELM_TESTING();
		
		File saveDir = new File("/tmp");
		boolean display = true;
		
		plotSolutionSlipRates(sol, region, saveDir, display);
	}

}
