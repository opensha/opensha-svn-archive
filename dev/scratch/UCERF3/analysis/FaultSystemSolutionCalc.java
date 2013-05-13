package scratch.UCERF3.analysis;

import java.awt.Color;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipException;

import org.dom4j.DocumentException;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.opensha.commons.data.function.DefaultXY_DataSet;
import org.opensha.commons.data.function.HistogramFunction;
import org.opensha.commons.gui.plot.PlotLineType;
import org.opensha.commons.gui.plot.PlotSymbol;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.opensha.sha.gui.infoTools.GraphiWindowAPI_Impl;
import org.opensha.sha.gui.infoTools.HeadlessGraphPanel;
import org.opensha.sha.gui.infoTools.PlotCurveCharacterstics;
import org.opensha.sha.magdist.GutenbergRichterMagFreqDist;
import org.opensha.sha.magdist.IncrementalMagFreqDist;

import scratch.UCERF3.CompoundFaultSystemSolution;
import scratch.UCERF3.FaultSystemRupSet;
import scratch.UCERF3.FaultSystemSolution;
import scratch.UCERF3.FaultSystemSolutionFetcher;
import scratch.UCERF3.enumTreeBranches.FaultModels;
import scratch.UCERF3.enumTreeBranches.ScalingRelationships;
import scratch.UCERF3.inversion.InversionFaultSystemSolution;
import scratch.UCERF3.logicTree.LogicTreeBranch;
import scratch.UCERF3.utils.RELM_RegionUtils;
import scratch.UCERF3.utils.UCERF3_DataUtils;
import scratch.peter.ucerf3.calc.UC3_CalcUtils;

public class FaultSystemSolutionCalc {
	
	
	/**
	 * This was for looking for transitions to water level rates, but Kevin has a better way of getting exact numbers
	 * @param fltSysSol
	 */
	public static void writeRupRatesToFile(FaultSystemSolution fltSysSol) {
		FaultSystemRupSet rupSet = fltSysSol.getRupSet();
		File dataFile = new File("tempFSS_Rates.txt");
		try {
			FileWriter fw = new FileWriter(dataFile);
			for(int r=0;r<rupSet.getNumRuptures();r++) {
				double mag =rupSet.getMagForRup(r);
				double rate = fltSysSol.getRateForRup(r);
				if(rate == 0)
					System.out.println("rup "+r+" has zero rate");
				String str = r+"\t"+mag+"\t"+rate+"\t"+(rate/Math.pow(10, -mag));
				fw.write(str+"\n");
			}
			fw.close ();
		}
		catch (IOException e) {
			System.out.println ("IO exception = " + e );
		}
	}
	
	public static void plotPaleoObsSlipCOV_Histogram(InversionFaultSystemSolution fltSysSol) {
		plotPaleoObsSlipCOV_Histogram(fltSysSol, null);
	}
	
	public static void plotPaleoObsSlipCOV_Histogram(InversionFaultSystemSolution fltSysSol, File outputFile) {
		
		double delta = 0.02;
		int num = (int)Math.round(100.0/0.025);
		
		HistogramFunction covHist = new HistogramFunction(delta/2, num, delta);
		double aveCOV = 0;
		for(int s=0;s<fltSysSol.getRupSet().getNumSections();s++) {
			double cov = fltSysSol.calcPaleoObsSlipPFD_ForSect(s).getCOV();
			aveCOV += cov;
//			System.out.println("COV: "+cov+" MAX: "+covHist.getMaxX());
			covHist.add(cov, 1.0);
		}
		aveCOV /= fltSysSol.getRupSet().getNumSections();
		
		covHist.setName("COV Histogram");
		covHist.setInfo("(mean COV = "+(float)aveCOV+")");
		
		GraphiWindowAPI_Impl graph = new GraphiWindowAPI_Impl(covHist, "COV Histogram");
		graph.setX_AxisRange(0, 2);
		graph.setX_AxisLabel("COV");
		graph.setY_AxisLabel("Fraction Per Bin");

		graph.setTickLabelFontSize(14);
		graph.setAxisLabelFontSize(16);
		graph.setPlotLabelFontSize(18);
		
		if (outputFile != null) {
			// stip out an extention if present
			File dir = outputFile.getParentFile();
			String name = outputFile.getName();
			if (name.endsWith(".png"))
				name = name.substring(0, name.indexOf(".png"));
			if (name.endsWith(".pdf"))
				name = name.substring(0, name.indexOf(".pdf"));
			
			try {
				graph.saveAsPDF(new File(dir, name+".pdf").getAbsolutePath());
				graph.saveAsPNG(new File(dir, name+".png").getAbsolutePath());
				graph.getGraphWindow().getGraphPanel().saveAsTXT(
						new File(dir, name+".txt").getAbsolutePath());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * This plots the paleo obs slip COV for each scaling relationship (all other parameters set to
	 * reference branch).
	 * @param fetcher
	 * @param outputDir
	 */
	public static void writePaleoObsSlipCOV_ForScalingRels(
			FaultSystemSolutionFetcher fetcher, File outputDir) {
		LogicTreeBranch ref = LogicTreeBranch.DEFAULT;
		
		for (ScalingRelationships scale : ScalingRelationships.values()) {
			if (scale.getRelativeWeight(null) == 0)
				continue;
			ref.setValue(scale);
			InversionFaultSystemSolution sol = fetcher.getSolution(ref);
			
			File file;
			if (outputDir == null)
				file = null;
			else
				file = new File(outputDir, "paleo_obs_slip_COV_"+scale.getShortName());
			
			plotPaleoObsSlipCOV_Histogram(sol, file);
		}
	}
	
	
	
	public static void plotRupLengthRateHistogram(FaultSystemSolution fss) {
		
//		double minLength=Double.MAX_VALUE;
//		double maxLength=Double.MIN_VALUE;
//		for(int r=0;r<fss.getNumRuptures();r++) {
//			double length = fss.getLengthForRup(r);
//			if(minLength>length) minLength=length;
//			if(maxLength<length) maxLength=length;
//		}
//		System.out.println("minLength="+minLength);
//		System.out.println("maxLength="+maxLength);
		
		HistogramFunction hist = new HistogramFunction(5.0,1235.0,124);
		for(int r=0;r<fss.getRupSet().getNumRuptures();r++) {
			double length = fss.getRupSet().getLengthForRup(r)/1000;;
			hist.add(length, fss.getRateForRup(r));
		}

		hist.normalizeBySumOfY_Vals();
		ArrayList<HistogramFunction> funcs2 = new ArrayList<HistogramFunction>();
		funcs2.add(hist);
		ArrayList<PlotCurveCharacterstics> plotChars = new ArrayList<PlotCurveCharacterstics>();
		plotChars.add(new PlotCurveCharacterstics(PlotLineType.HISTOGRAM, 2f, Color.RED));
		GraphiWindowAPI_Impl graph2 = new GraphiWindowAPI_Impl(funcs2, "Rupture Length Histogram"); 
		graph2.setX_AxisLabel("Length (km)");
		graph2.setY_AxisLabel("Fraction");
	}
	
	
	
	public static void testHeadlessMFD_Plot(FaultSystemSolution fss) {
		IncrementalMagFreqDist mfd = fss.calcNucleationMFD_forRegion(RELM_RegionUtils.getGriddedRegionInstance(), 5.05, 8.95, 0.1, true);
		HeadlessGraphPanel gp = new HeadlessGraphPanel();
		gp.setYLog(true);
		gp.setUserBounds(5.0, 9.0, 1e-6, 1);
		String title = "RELM REGION";
		String yAxisLabel = "Nucleation Rate (per yr)";
		ArrayList<IncrementalMagFreqDist> funcs = new ArrayList<IncrementalMagFreqDist>();
		funcs.add(mfd);
		scratch.UCERF3.inversion.CommandLineInversionRunner.setFontSizes(gp);
//		gp.setTickLabelFontSize(30);
//		gp.setAxisLabelFontSize(36);
//		gp.setPlotLabelFontSize(36);
		gp.drawGraphPanel("Magnitude", yAxisLabel, funcs, true, title);
		File file = new File("testRightHere");
//		gp.getCartPanel().setSize(1000, 800);
		gp.getCartPanel().setSize(500, 400);
		try {
			gp.saveAsPDF(file.getAbsolutePath()+".pdf");
			gp.saveAsPNG(file.getAbsolutePath()+".png");

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	
	public static void checkFinalSubseisOnFaultRates(InversionFaultSystemSolution invSol) {

		System.out.println("Starting check");

		if(invSol instanceof InversionFaultSystemSolution) {
			List<GutenbergRichterMagFreqDist>  grList = invSol.getFinalSubSeismoOnFaultMFD_List();
			for(int s=0;s<grList.size();s++) {
				double rate = grList.get(s).getTotalIncrRate();
				System.out.println(s+"\t"+(float)rate+"\t"+invSol.getRupSet().getFaultSectionData(s).getName());
				if(rate < 1e-10) {
					System.out.println(invSol.getRupSet().getFaultSectionData(s).getName());
				}
			}
		}
		else
			System.out.println("Not instance of InversionFaultSystemSolution");
			
		System.out.println("Done with check");

	}

	
	/**
	 * @param args
	 * @throws IOException 
	 * @throws ZipException 
	 */
	public static void main(String[] args) throws ZipException, IOException {
		
		String solPath = "/Users/pmpowers/projects/OpenSHA/tmp/invSols/tree/2013_01_14-UC32-COMPOUND_SOL.zip";
		String branch = "FM3_1_ZENGBB_Shaw09Mod_DsrTap_CharConst_M5Rate8.7_MMaxOff7.6_NoFix_SpatSeisU3";

		// some U3.1 file:
//		File fssFile = new File("dev/scratch/UCERF3/data/scratch/InversionSolutions/2012_10_14-fm3-logic-tree-sample-x5_MEAN_BRANCH_AVG_SOL.zip");
		
		// U3.2 files
//		File fssFile = new File("dev/scratch/UCERF3/data/scratch/InversionSolutions/2013_01_14-stampede_3p2_production_runs_combined_FM3_1_MEAN_BRANCH_AVG_SOL.zip");
////		File fssFile = new File("dev/scratch/UCERF3/data/scratch/InversionSolutions/2013_01_14-stampede_3p2_production_runs_combined_FM3_2_MEAN_BRANCH_AVG_SOL.zip");
//
		
		// U3.3 compuond file, assumed to be in data/scratch/InversionSolutions
		// download it from here: http://opensha.usc.edu/ftp/kmilner/ucerf3/2013_05_10-ucerf3p3-production-10runs/2013_05_10-ucerf3p3-production-10runs_COMPOUND_SOL.zip
		String fileName = "2013_05_10-ucerf3p3-production-10runs_COMPOUND_SOL.zip";
		File invDir = new File(UCERF3_DataUtils.DEFAULT_SCRATCH_DATA_DIR, "InversionSolutions");
		File compoundFile = new File(invDir, fileName);
		// output directory - you probably want to change this
		File outputDir = new File("/tmp");
		
		CompoundFaultSystemSolution fetcher = CompoundFaultSystemSolution.fromZipFile(compoundFile);
		writePaleoObsSlipCOV_ForScalingRels(fetcher, outputDir);
//		
//		try {
//			CompoundFaultSystemSolution cfss = UC3_CalcUtils.getCompoundSolution(solPath);
//			LogicTreeBranch ltb = LogicTreeBranch.fromFileName(branch);
//			InversionFaultSystemSolution fss = cfss.getSolution(ltb);
//			checkFinalSubseisOnFaultRates(fss);
//			
//			
//			
////			checkSubseisOnFaultRates(SimpleFaultSystemSolution.fromFile(fssFile));
////			writeRupRatesToFile(SimpleFaultSystemSolution.fromFile(fssFile));
////			testHeadlessMFD_Plot(SimpleFaultSystemSolution.fromFile(fssFile));
////			plotRupLengthRateHistogram(SimpleFaultSystemSolution.fromFile(fssFile));
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}

}
