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
import org.opensha.commons.geo.LocationList;
import org.opensha.commons.geo.Region;
import org.opensha.commons.mapping.gmt.GMT_Map;
import org.opensha.commons.mapping.gmt.GMT_MapGenerator;
import org.opensha.commons.mapping.gmt.elements.CoastAttributes;
import org.opensha.commons.mapping.gmt.elements.GMT_CPT_Files;
import org.opensha.commons.mapping.gmt.elements.PSXYPolygon;
import org.opensha.commons.mapping.gmt.gui.GMT_MapGuiBean;
import org.opensha.commons.util.ExceptionUtils;
import org.opensha.commons.util.FaultUtils;
import org.opensha.commons.util.FileUtils;
import org.opensha.commons.util.cpt.CPT;
import org.opensha.commons.util.cpt.CPTVal;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.opensha.sha.faultSurface.FaultTrace;
import org.opensha.sha.gui.infoTools.ImageViewerWindow;

import com.google.common.base.Preconditions;

import scratch.UCERF3.FaultSystemSolution;
import scratch.UCERF3.SimpleFaultSystemSolution;
import scratch.UCERF3.inversion.InversionConfiguration;
import scratch.UCERF3.utils.UCERF3_DataUtils;

public class FaultBasedMapGen {
	
	private static GMT_MapGenerator gmt;
	
	private static CPT slipCPT = null;
	private static CPT getSlipRateCPT() {
		if (slipCPT == null) {
			slipCPT = new CPT();
			
			slipCPT.setBelowMinColor(Color.GRAY);
			slipCPT.setNanColor(Color.GRAY);
			
//			slipCPT.add(new CPTVal(0f, Color.GRAY, 0f, Color.GRAY));
			slipCPT.add(new CPTVal(Float.MIN_VALUE, Color.BLUE, 10f, Color.MAGENTA));
			slipCPT.add(new CPTVal(10f, Color.MAGENTA, 20f, Color.RED));
			slipCPT.add(new CPTVal(20f, Color.RED, 30f, Color.ORANGE));
			slipCPT.add(new CPTVal(30f, Color.ORANGE, 40f, Color.YELLOW));
			
			slipCPT.setAboveMaxColor(Color.YELLOW);
		}
		return slipCPT;
	}
	
	private static CPT fractDiffCPT = null;
	private static CPT getFractionalDifferenceCPT() {
		if (fractDiffCPT == null) {
			try {
				fractDiffCPT = GMT_CPT_Files.GMT_POLAR.instance();
			} catch (IOException e) {
				ExceptionUtils.throwAsRuntimeException(e);
			}
			fractDiffCPT = fractDiffCPT.rescale(-1, 1);
		}
		
		return fractDiffCPT;
	}
	
	private static CPT participationCPT = null;
	private static CPT getParticipationCPT() {
		if (participationCPT == null) {
			try {
				participationCPT = GMT_CPT_Files.UCERF2_FIGURE_35.instance();
			} catch (IOException e) {
				ExceptionUtils.throwAsRuntimeException(e);
			}
		}
		
		return participationCPT;
	}
	
	private static CPT ratioCPT = null;
	private static CPT getRatioCPT() {
		if (ratioCPT == null) {
			try {
				ratioCPT = GMT_CPT_Files.GMT_POLAR.instance();
			} catch (IOException e) {
				ExceptionUtils.throwAsRuntimeException(e);
			}
			ratioCPT = ratioCPT.rescale(-1, 1);
//			ratioCPT.get(0).start = (float)(1d/3d);
//			ratioCPT.get(0).end = 1f;
//			ratioCPT.get(1).start = 1f;
//			ratioCPT.get(1).end = 3f;
			ratioCPT.setNanColor(Color.GRAY);
			ratioCPT.setAboveMaxColor(ratioCPT.getMaxColor());
			ratioCPT.setBelowMinColor(ratioCPT.getMinColor());
		}
		
		return ratioCPT;
	}
	
	private static CPT normalizedPairRatesCPT = null;
	private static CPT getNormalizedPairRatesCPT() {
		if (normalizedPairRatesCPT == null) {
			try {
				normalizedPairRatesCPT = GMT_CPT_Files.GMT_POLAR.instance().rescale(0, 1);
//				normalizedPairRatesCPT.get(0).maxColor = Color.GRAY;
//				normalizedPairRatesCPT.get(1).minColor = Color.GRAY;
				normalizedPairRatesCPT.setNanColor(Color.GRAY);
			} catch (IOException e) {
				ExceptionUtils.throwAsRuntimeException(e);
			}
		}
		
		return normalizedPairRatesCPT;
	}
	
	public static void plotOrigNonReducedSlipRates(FaultSystemSolution sol, Region region, File saveDir, String prefix, boolean display)
			throws GMT_MapException, RuntimeException, IOException {
		CPT cpt = getSlipRateCPT();
		List<FaultSectionPrefData> faults = sol.getFaultSectionDataList();
		double[] values = new double[faults.size()];
		for (int i=0; i<faults.size(); i++)
			values[i] = faults.get(i).getOrigAveSlipRate();
		
		makeFaultPlot(cpt, getTraces(faults), values, region, saveDir, prefix+"_orig_non_reduced_slip", display, false, "Original Non Reduced Slip Rate (mm/yr)");
	}
	
	public static void plotOrigCreepReducedSlipRates(FaultSystemSolution sol, Region region, File saveDir, String prefix, boolean display)
			throws GMT_MapException, RuntimeException, IOException {
		CPT cpt = getSlipRateCPT();
		List<FaultSectionPrefData> faults = sol.getFaultSectionDataList();
		double[] values = new double[faults.size()];
		for (int i=0; i<faults.size(); i++)
			values[i] = faults.get(i).getReducedAveSlipRate();
		
		makeFaultPlot(cpt, getTraces(faults), values, region, saveDir, prefix+"_orig_creep_reduced_slip", display, false, "Orig Creep Reduced Slip Rate (mm/yr)");
	}
	
	public static void plotTargetSlipRates(FaultSystemSolution sol, Region region, File saveDir, String prefix, boolean display)
			throws GMT_MapException, RuntimeException, IOException {
		CPT cpt = getSlipRateCPT();
		List<FaultSectionPrefData> faults = sol.getFaultSectionDataList();
		double[] values = scale(sol.getSlipRateForAllSections(), 1e3); // to mm
		
		makeFaultPlot(cpt, getTraces(faults), values, region, saveDir, prefix+"_target_slip", display, false, "Target Slip Rate (mm/yr)");
	}
	
	public static void plotSolutionSlipRates(FaultSystemSolution sol, Region region, File saveDir, String prefix, boolean display)
			throws GMT_MapException, RuntimeException, IOException {
		CPT cpt = getSlipRateCPT();
		List<FaultSectionPrefData> faults = sol.getFaultSectionDataList();
		double[] values = scale(sol.calcSlipRateForAllSects(), 1e3); // to mm
		
		makeFaultPlot(cpt, getTraces(faults), values, region, saveDir, prefix+"_solution_slip", display, false, "Solution Slip Rate (mm/yr)");
	}
	
	private static double calcFractionalDifferentce(double target, double comparison) {
		return (comparison - target) / target;
	}
	
	public static void plotSolutionSlipMisfit(FaultSystemSolution sol, Region region, File saveDir, String prefix, boolean display)
			throws GMT_MapException, RuntimeException, IOException {
		CPT cpt = getFractionalDifferenceCPT();
		List<FaultSectionPrefData> faults = sol.getFaultSectionDataList();
		double[] solSlips = sol.calcSlipRateForAllSects();
		double[] targetSlips = sol.getSlipRateForAllSections();
		double[] values = new double[faults.size()];
		for (int i=0; i<faults.size(); i++)
			values[i] = calcFractionalDifferentce(targetSlips[i], solSlips[i]);
		
		makeFaultPlot(cpt, getTraces(faults), values, region, saveDir, prefix+"_slip_misfit", display, false, "Solution Slip Rate Misfit (fractional diff)");
	}
	
	public static void plotParticipationRates(FaultSystemSolution sol, Region region, File saveDir, String prefix, boolean display,
			double minMag, double maxMag)
			throws GMT_MapException, RuntimeException, IOException {
		CPT cpt = getParticipationCPT();
		List<FaultSectionPrefData> faults = sol.getFaultSectionDataList();
		double[] values = sol.calcParticRateForAllSects(minMag, maxMag);
		
		// now log space
		values = log10(values);
		
		String name = prefix+"_partic_rates_"+(float)minMag;
		String title = "Log10(Participation Rates "+(float)+minMag;
		if (maxMag < 9) {
			name += "_"+(float)maxMag;
			title += "=>"+(float)maxMag;
		} else {
			name += "+";
			title += "+";
		}
		title += ")";
		
		makeFaultPlot(cpt, getTraces(faults), values, region, saveDir, name, display, false, title);
	}
	
	public static void plotParticipationRatios(FaultSystemSolution sol, FaultSystemSolution referenceSol, Region region,
			File saveDir, String prefix, boolean display, double minMag, double maxMag, boolean omitInfinites)
			throws GMT_MapException, RuntimeException, IOException {
		CPT cpt = getRatioCPT();
		List<FaultSectionPrefData> faults = sol.getFaultSectionDataList();
		double[] newVals = sol.calcParticRateForAllSects(minMag, maxMag);
		double[] refVals = referenceSol.calcParticRateForAllSects(minMag, maxMag);
		Preconditions.checkState(newVals.length == refVals.length, "solution rupture counts are incompatible!");
		
		double[] values = new double[newVals.length];
		for (int i=0; i<values.length; i++) {
			values[i] = newVals[i] / refVals[i];
			if (omitInfinites && Double.isInfinite(values[i]))
				values[i] = Double.NaN;
		}
		values = log10(values);
		
		String name = prefix+"_partic_ratio_"+(float)minMag;
		String title = "Log10(Participation Ratios "+(float)+minMag;
		if (maxMag < 9) {
			name += "_"+(float)maxMag;
			title += "=>"+(float)maxMag;
		} else {
			name += "+";
			title += "+";
		}
		title += ")";
		
		makeFaultPlot(cpt, getTraces(faults), values, region, saveDir, name, display, true, title);
	}
	
	public static void plotSectionPairRates(FaultSystemSolution sol, Region region,
			File saveDir, String prefix, boolean display) throws GMT_MapException, RuntimeException, IOException {
		CPT cpt = getNormalizedPairRatesCPT();
		List<FaultSectionPrefData> faults = sol.getFaultSectionDataList();
		double[][] rates = sol.getSectionPairRupRates();
		
		ArrayList<LocationList> lines = new ArrayList<LocationList>();
		ArrayList<Double> vals = new ArrayList<Double>();
		
		for (int sec1=0; sec1<rates.length; sec1++) {
			double[] secRates = rates[sec1];
			
			for (int sec2=0; sec2<secRates.length; sec2++) {
				// don't draw lines between the same section (==)
				// done draw lines if sec 1 is greater than sec 2, because it
				// will have already been added (>)
				if (sec1 >= sec2)
					continue;
				
				double rate = secRates[sec2];
				if (rate <= 0)
					continue;
				
				double sec1Rate = sol.calcParticRateForSect(sec1, 0, 10);
				double sec2Rate = sol.calcParticRateForSect(sec2, 0, 10);
				double avg = 0.5 * (sec1Rate + sec2Rate);
				rate /= avg;
				
				LocationList pts = new LocationList();
				pts.add(getTraceMidpoint(faults.get(sec1)));
				pts.add(getTraceMidpoint(faults.get(sec2)));
				
				lines.add(pts);
				vals.add(rate);
			}
		}
		double[] values = new double[vals.size()];
		for (int i=0; i<values.length; i++)
			values[i] = vals.get(i);
		
		makeFaultPlot(cpt, lines, values, region, saveDir, prefix+"_sect_pairs", display, true, "Normalized Section Pair Rates");
	}
	
	private static Location getTraceMidpoint(FaultSectionPrefData fault) {
		return FaultUtils.resampleTrace(fault.getFaultTrace(), 10).get(5);
	}
	
	public static double[] scale(double[] values, double scalar) {
		double[] ret = new double[values.length];
		for (int i=0; i<values.length; i++)
			ret[i] = values[i] * scalar;
		return ret;
	}
	
	public static double[] log10(double[] values) {
		double[] ret = new double[values.length];
		for (int i=0; i<values.length; i++)
			ret[i] = Math.log10(values[i]);
		return ret;
	}
	
	private static ArrayList<LocationList> getTraces(List<FaultSectionPrefData> faults) {
		ArrayList<LocationList> faultTraces = new ArrayList<LocationList>();
		for (FaultSectionPrefData fault : faults)
			faultTraces.add(fault.getFaultTrace());
		return faultTraces;
	}
	
	private synchronized static void makeFaultPlot(CPT cpt, List<LocationList> faults, double[] values, Region region,
			File saveDir, String prefix, boolean display, boolean skipNans, String label)
					throws GMT_MapException, RuntimeException, IOException {
		GMT_Map map = new GMT_Map(region, null, 1, cpt);
		
		map.setBlackBackground(false);
		map.setRescaleCPT(false);
		map.setCustomScaleMin((double)cpt.getMinValue());
		map.setCustomScaleMax((double)cpt.getMaxValue());
		map.setCoast(new CoastAttributes(Color.BLACK, 2));
		map.setCustomLabel(label);
		
		Preconditions.checkState(faults.size() == values.length, "faults and values are different lengths!");
		
		for (int i=0; i<faults.size(); i++) {
			if (skipNans && Double.isNaN(values[i]))
				continue;
			LocationList fault = faults.get(i);
			Color c = cpt.getColor((float)values[i]);
			for (PSXYPolygon poly : getPolygons(fault, c))
				map.addPolys(poly);
		}
		
		if (gmt == null)
			gmt = new GMT_MapGenerator();
		
		String url = gmt.makeMapUsingServlet(map, "metadata", null);
		System.out.println(url);
		String metadata = GMT_MapGuiBean.getClickHereHTML(gmt.getGMTFilesWebAddress());
		if (saveDir != null) {
			String baseURL = url.substring(0, url.lastIndexOf('/')+1);
			
			File pngFile = new File(saveDir, prefix+".png");
			FileUtils.downloadURL(baseURL+"map.png", pngFile);
			
			File pdfFile = new File(saveDir, prefix+".pdf");
			FileUtils.downloadURL(baseURL+"map.pdf", pdfFile);
		}
//		File zipFile = new File(downloadDir, "allFiles.zip");
//		// construct zip URL
//		String zipURL = url.substring(0, url.lastIndexOf('/')+1)+"allFiles.zip";
//		FileUtils.downloadURL(zipURL, zipFile);
//		FileUtils.unzipFile(zipFile, downloadDir);
		
		if (display) {
			new ImageViewerWindow(url,metadata, true);
		}
	}
	
	private static ArrayList<PSXYPolygon> getPolygons(LocationList locs, Color c) {
		ArrayList<PSXYPolygon> polys = new ArrayList<PSXYPolygon>();
		
		for (int i=1; i<locs.size(); i++) {
			Location loc1 = locs.get(i-1);
			Location loc2 = locs.get(i);
			PSXYPolygon poly = new PSXYPolygon(loc1, loc2);
			poly.setPenColor(c);
			poly.setPenWidth(8);
			polys.add(poly);
		}
		
		return polys;
	}
	
	public static void main(String[] args) throws IOException, DocumentException, GMT_MapException, RuntimeException {
		File invSolsDir = new File(UCERF3_DataUtils.DEFAULT_SCRATCH_DATA_DIR, "InversionSolutions");
		File solFile = new File(invSolsDir, "FM3_1_GLpABM_MaHB08_DsrTap_DrEllB_Char_VarAseis0.2_VarOffAseis0.5_VarMFDMod1_VarNone_sol.zip");
//		File solFile = new File(invSolsDir, "FM3_1_GLpABM_MaEllB_DsrTap_DrEllB_Char_VarAseis0.2_VarOffAseis0.5_VarMFDMod1_VarNone_sol.zip");
		FaultSystemSolution sol = SimpleFaultSystemSolution.fromZipFile(solFile);
		
		Region region = new CaliforniaRegions.RELM_TESTING();
		
		File saveDir = new File("/tmp");
		String prefix = solFile.getName().replaceAll(".zip", "");
		boolean display = true;
		
//		plotOrigNonReducedSlipRates(sol, region, saveDir, prefix, display);
//		plotOrigCreepReducedSlipRates(sol, region, saveDir, prefix, display);
//		plotTargetSlipRates(sol, region, saveDir, prefix, display);
//		plotSolutionSlipRates(sol, region, saveDir, prefix, display);
//		plotSolutionSlipMisfit(sol, region, saveDir, prefix, display);
//		plotParticipationRates(sol, region, saveDir, prefix, display, 6.7, 10);
//		plotParticipationRates(sol, region, saveDir, prefix, display, 6, 7);
//		plotParticipationRates(sol, region, saveDir, prefix, display, 7, 8);
//		plotParticipationRates(sol, region, saveDir, prefix, display, 8, 10);
		
		double[] ucerf2_rates = InversionConfiguration.getUCERF2Solution(sol);
		FaultSystemSolution ucerf2Sol = new SimpleFaultSystemSolution(sol, ucerf2_rates);
		for (int r=0; r<ucerf2Sol.getNumRuptures(); r++) {
			double mag = ucerf2Sol.getMagForRup(r);
			double rate = ucerf2_rates[r];
			if (mag>=8 && rate > 0)
				System.out.println("Nonzero M>=8!: "+r+": Mag="+mag+", rate="+rate);
		}
//		plotParticipationRates(sol, region, saveDir, prefix, display, 6, 7);
//		plotParticipationRates(ucerf2Sol, region, saveDir, prefix, display, 6, 7);
//		plotParticipationRatios(sol, ucerf2Sol, region, saveDir, prefix, display, 6, 7, true);
//		plotParticipationRates(sol, region, saveDir, prefix, display, 7, 8);
//		plotParticipationRates(ucerf2Sol, region, saveDir, prefix, display, 7, 8);
//		plotParticipationRatios(sol, ucerf2Sol, region, saveDir, prefix, display, 7, 8, true);
//		plotParticipationRates(sol, region, saveDir, prefix, display, 8, 10);
//		plotParticipationRates(ucerf2Sol, region, saveDir, prefix, display, 8, 10);
//		plotParticipationRatios(sol, ucerf2Sol, region, saveDir, prefix, display, 8, 10, true);
		
//		plotSectionPairRates(sol, region, saveDir, prefix, display);
	}

}
