package scratch.UCERF3.analysis;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipException;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math.stat.StatUtils;
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

import scratch.UCERF3.FaultSystemRupSet;
import scratch.UCERF3.FaultSystemSolution;
import scratch.UCERF3.SimpleFaultSystemSolution;
import scratch.UCERF3.inversion.InversionConfiguration;
import scratch.UCERF3.utils.MatrixIO;
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
	
	private static CPT linearRatioCPT = null;
	private static CPT getLinearRatioCPT() {
		if (linearRatioCPT == null) {
			try {
				linearRatioCPT = GMT_CPT_Files.UCERF3_RATIOS.instance();
			} catch (IOException e) {
				ExceptionUtils.throwAsRuntimeException(e);
			}
			linearRatioCPT = linearRatioCPT.rescale(0, 2);
		}
		
		return linearRatioCPT;
	}
	
	private static CPT logRatioCPT = null;
	private static CPT getLogRatioCPT() {
		if (logRatioCPT == null) {
			try {
				logRatioCPT = GMT_CPT_Files.UCERF3_RATIOS.instance();
			} catch (IOException e) {
				ExceptionUtils.throwAsRuntimeException(e);
			}
			logRatioCPT = logRatioCPT.rescale(-3, 3);
		}
		
		return logRatioCPT;
	}
	
	private static CPT normalizedPairRatesCPT = null;
	private static CPT getNormalizedPairRatesCPT() {
		if (normalizedPairRatesCPT == null) {
			try {
				normalizedPairRatesCPT = GMT_CPT_Files.MAX_SPECTRUM.instance().rescale(0, 1);
//				normalizedPairRatesCPT = GMT_CPT_Files.GMT_POLAR.instance().rescale(0, 1);
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
	
	public static void plotSolutionSlipMisfit(FaultSystemSolution sol, Region region, File saveDir, String prefix, boolean display, boolean logRatio)
			throws GMT_MapException, RuntimeException, IOException {
		List<FaultSectionPrefData> faults = sol.getFaultSectionDataList();
		double[] solSlips = sol.calcSlipRateForAllSects();
		double[] targetSlips = sol.getSlipRateForAllSections();
		double[] values = new double[faults.size()];
//		for (int i=0; i<faults.size(); i++)
//			values[i] = calcFractionalDifferentce(targetSlips[i], solSlips[i]);
//		CPT cpt = getFractionalDifferenceCPT();
//		makeFaultPlot(cpt, getTraces(faults), values, region, saveDir, prefix+"_slip_misfit", display, false, "Solution Slip Rate Misfit (fractional diff)");
		for (int i=0; i<faults.size(); i++)
			values[i] = solSlips[i] / targetSlips[i];
		CPT cpt;
		prefix += "_slip_misfit";
		String name = "Solution Slip / Target Slip";
		if (logRatio) {
			values = log10(values);
			cpt = getLogRatioCPT().rescale(-1, 1);
			prefix += "_log";
			name = "Log10("+name+")";
		} else {
			cpt = getLinearRatioCPT();
		}
		makeFaultPlot(cpt, getTraces(faults), values, region, saveDir, prefix, display, false, name);
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
	
	public static void plotParticipationStdDevs(FaultSystemRupSet rupSet, double[][] partRates, Region region, File saveDir,
			String prefix, boolean display, double minMag, double maxMag)
			throws GMT_MapException, RuntimeException, IOException {
		CPT cpt = getParticipationCPT();
		List<FaultSectionPrefData> faults = rupSet.getFaultSectionDataList();
		
		double[] stdDevs = new double[partRates.length];
		double[] mean = new double[partRates.length];
		double[] meanOverStdDev = new double[partRates.length];
		for (int i=0; i<partRates.length; i++) {
			mean[i] = StatUtils.mean(partRates[i]);
			stdDevs[i] = Math.sqrt(StatUtils.variance(partRates[i], mean[i]));
			meanOverStdDev[i] = mean[i] / stdDevs[i];
		}
		
		String name = prefix+"_partic_std_dev_"+(float)minMag;
		String title = "Log10(Participation Rates Std. Dev. "+(float)+minMag;
		if (maxMag < 9) {
			name += "_"+(float)maxMag;
			title += "=>"+(float)maxMag;
		} else {
			name += "+";
			title += "+";
		}
		title += ")";
		
		MatrixIO.doubleArrayToFile(stdDevs, new File(saveDir, name+".bin"));
		
		// now log space
		double[] logStdDevs = log10(stdDevs);
		
		makeFaultPlot(cpt, getTraces(faults), logStdDevs, region, saveDir, name, display, false, title);
		
		title = title.replaceAll("Dev. ", "Dev. / Mean ");
		name = name.replaceAll("_dev_", "_dev_norm_");
		double[] norm = new double[mean.length];
		for (int i=0; i<mean.length; i++)
			norm[i] = stdDevs[i] / mean[i];
		norm = log10(norm);
		cpt = cpt.rescale(-3, 2);
		
		makeFaultPlot(cpt, getTraces(faults), norm, region, saveDir, name, display, false, title);
		
		double[] logMeanOverStdDevs = log10(meanOverStdDev);
		
		cpt = getLogRatioCPT();
		name = prefix+"_partic_mean_over_std_dev_"+(float)minMag;
		title = "Log10(Participation Rates Mean / Std. Dev. "+(float)+minMag;
		if (maxMag < 9) {
			name += "_"+(float)maxMag;
			title += "=>"+(float)maxMag;
		} else {
			name += "+";
			title += "+";
		}
		title += ")";
		
		makeFaultPlot(cpt, getTraces(faults), logMeanOverStdDevs, region, saveDir, name, display, false, title);
	}
	
	public static void plotSolutionSlipRateStdDevs(FaultSystemRupSet rupSet, double[][] slipRates, Region region, File saveDir, String prefix, boolean display)
			throws GMT_MapException, RuntimeException, IOException {
		CPT cpt = getParticipationCPT().rescale(-4, 1);
		List<FaultSectionPrefData> faults = rupSet.getFaultSectionDataList();
		
		double[] stdDev = new double[slipRates.length];
		double[] mean = new double[slipRates.length];
		for (int i=0; i<slipRates.length; i++) {
			double[] rates = scale(slipRates[i], 1e3); // to mm
			mean[i] = StatUtils.mean(rates);
			stdDev[i] = Math.sqrt(StatUtils.variance(rates, mean[i]));
		}
		MatrixIO.doubleArrayToFile(stdDev, new File(saveDir, prefix+"_solution_slip_std_dev.bin"));
		// now log space
		double[] logStdDev = log10(stdDev);
		
		makeFaultPlot(cpt, getTraces(faults), logStdDev, region, saveDir, prefix+"_solution_slip_std_dev", display, false, "Log10(Solution Slip Rate Std Dev (mm/yr))");

		double[] norm = new double[mean.length];
		for (int i=0; i<mean.length; i++)
			norm[i] = stdDev[i] / mean[i];
		norm = log10(norm);
		cpt = cpt.rescale(-3, 2);
		
		makeFaultPlot(cpt, getTraces(faults), norm, region, saveDir, prefix+"_solution_slip_std_dev_norm", display, false, "Log10(Solution Slip Rate Std Dev / Mean)");
	}
	
	public static void plotParticipationRatios(FaultSystemSolution sol, FaultSystemSolution referenceSol, Region region,
			File saveDir, String prefix, boolean display, double minMag, double maxMag, boolean omitInfinites)
			throws GMT_MapException, RuntimeException, IOException {
		CPT cpt = getLogRatioCPT();
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
	
	public static double plotParticipationDiffs(FaultSystemSolution sol, FaultSystemSolution referenceSol, Region region,
			File saveDir, String prefix, boolean display, double minMag, double maxMag)
			throws GMT_MapException, RuntimeException, IOException {
//		CPT cpt = getLinearRatioCPT().rescale(-3, 3);
		CPT cpt = getLinearRatioCPT().rescale(-0.005, 0.005);
		List<FaultSectionPrefData> faults = sol.getFaultSectionDataList();
		double[] newVals = sol.calcParticRateForAllSects(minMag, maxMag);
		double[] refVals = referenceSol.calcParticRateForAllSects(minMag, maxMag);
		Preconditions.checkState(newVals.length == refVals.length, "solution rupture counts are incompatible!");
		
		double[] values = new double[newVals.length];
		double total = 0;
		for (int i=0; i<values.length; i++) {
			double diff = newVals[i] - refVals[i];
			if (!Double.isNaN(diff))
				total += diff;
			values[i] = diff;
		}
		
		String name = prefix+"_ref_partic_diff_"+(float)minMag;
//		String title = "Log10(Sol Partic Rate) - Log10(Ref Partic Rate) "+(float)+minMag;
		String title = "Participation Rate Diff "+(float)+minMag;
		if (maxMag < 9) {
			name += "_"+(float)maxMag;
			title += "=>"+(float)maxMag;
		} else {
			name += "+";
			title += "+";
		}
		
		makeFaultPlot(cpt, getTraces(faults), values, region, saveDir, name, display, true, title);
		return total;
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
	
	private static class TraceValue implements Comparable<TraceValue> {
		private LocationList trace;
		private double value;
		public TraceValue(LocationList trace, double value) {
			this.trace = trace;
			this.value = value;
		}

		@Override
		public int compareTo(TraceValue o) {
			return Double.compare(value, o.value);
		}
		
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
		
		ArrayList<TraceValue> vals = new ArrayList<FaultBasedMapGen.TraceValue>();
		for (int i=0; i<faults.size(); i++) {
			if (skipNans && Double.isNaN(values[i]))
				continue;
			LocationList fault = faults.get(i);
			vals.add(new TraceValue(fault, values[i]));
		}
		Collections.sort(vals); // so that high values are on top
		
		for (TraceValue val : vals) {
			LocationList fault = val.trace;
			double value = val.value;
			Color c = cpt.getColor((float)value);
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
//		File solFile = new File(invSolsDir, "FM3_1_GLpABM_MaHB08_DsrTap_DrEllB_Char_VarAseis0.2_VarOffAseis0.5_VarMFDMod1_VarNone_sol.zip");
//		File solFile = new File(invSolsDir, "FM3_1_GLpABM_MaEllB_DsrTap_DrEllB_Char_VarAseis0.2_VarOffAseis0.5_VarMFDMod1_VarNone_sol.zip");
//		File solFile = new File(invSolsDir, "FM3_1_GLpABM_MaEllB_DsrTap_DrEllB_Char_VarAseis0.1_VarOffAseis0.5_VarMFDMod1_VarNone_PREVENT_run0_sol.zip");
		File solFile = new File(invSolsDir, "FM3_1_GLpABM_MaEllB_DsrTap_DrEllB_Char_VarAseis0.1_VarOffAseis0_VarMFDMod1.3_VarNone_sol.zip");
//		File solFile = new File("/tmp/ucerf2_fm2_compare.zip");
		FaultSystemSolution sol = SimpleFaultSystemSolution.fromZipFile(solFile);
		
		Region region = new CaliforniaRegions.RELM_TESTING();
		
		File saveDir = new File("/tmp");
		String prefix = solFile.getName().replaceAll(".zip", "");
		boolean display = true;
		
//		plotOrigNonReducedSlipRates(sol, region, saveDir, prefix, display);
//		plotOrigCreepReducedSlipRates(sol, region, saveDir, prefix, display);
//		plotTargetSlipRates(sol, region, saveDir, prefix, display);
//		plotSolutionSlipRates(sol, region, saveDir, prefix, display);
		plotSolutionSlipMisfit(sol, region, saveDir, prefix, display, true);
		plotSolutionSlipMisfit(sol, region, saveDir, prefix, display, false);
//		plotParticipationRates(sol, region, saveDir, prefix, display, 6.7, 10);
//		plotParticipationRates(sol, region, saveDir, prefix, display, 6, 7);
//		plotParticipationRates(sol, region, saveDir, prefix, display, 7, 8);
//		plotParticipationRates(sol, region, saveDir, prefix, display, 8, 10);
		
		double[] ucerf2_rates = InversionConfiguration.getUCERF2Solution(sol);
		FaultSystemSolution ucerf2Sol = new SimpleFaultSystemSolution(sol, ucerf2_rates);
//		for (int r=0; r<ucerf2Sol.getNumRuptures(); r++) {
//			double mag = ucerf2Sol.getMagForRup(r);
//			double rate = ucerf2_rates[r];
//			if (mag>=8 && rate > 0)
//				System.out.println("Nonzero M>=8!: "+r+": Mag="+mag+", rate="+rate);
//		}
//		plotParticipationRates(sol, region, saveDir, prefix, display, 6, 7);
//		plotParticipationRates(ucerf2Sol, region, saveDir, prefix, display, 6, 7);
		plotParticipationRatios(sol, ucerf2Sol, region, saveDir, prefix, display, 6, 7, true);
//		plotParticipationRates(sol, region, saveDir, prefix, display, 7, 8);
//		plotParticipationRates(ucerf2Sol, region, saveDir, prefix, display, 7, 8);
//		plotParticipationRatios(sol, ucerf2Sol, region, saveDir, prefix, display, 7, 8, true);
//		plotParticipationRates(sol, region, saveDir, prefix, display, 8, 10);
//		plotParticipationRates(ucerf2Sol, region, saveDir, prefix, display, 8, 10);
//		plotParticipationRatios(sol, ucerf2Sol, region, saveDir, prefix, display, 8, 10, true);
		
//		plotSectionPairRates(sol, region, saveDir, prefix, display);
	}

}
