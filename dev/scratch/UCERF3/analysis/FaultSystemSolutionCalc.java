package scratch.UCERF3.analysis;

import java.io.File;
import java.io.IOException;
import java.util.zip.ZipException;

import org.opensha.commons.data.function.HistogramFunction;
import org.opensha.sha.gui.infoTools.GraphiWindowAPI_Impl;

import scratch.UCERF3.CompoundFaultSystemSolution;
import scratch.UCERF3.FaultSystemSolution;
import scratch.UCERF3.FaultSystemSolutionFetcher;
import scratch.UCERF3.enumTreeBranches.ScalingRelationships;
import scratch.UCERF3.logicTree.LogicTreeBranch;

public class FaultSystemSolutionCalc {
	
	public static void plotPaleoObsSlipCOV_Histogram(FaultSystemSolution fltSysSol) {
		plotPaleoObsSlipCOV_Histogram(fltSysSol, null);
	}
	
	public static void plotPaleoObsSlipCOV_Histogram(FaultSystemSolution fltSysSol, File outputFile) {
		
		double delta = 0.02;
		int num = (int)Math.round(100.0/0.025);
		
		HistogramFunction covHist = new HistogramFunction(delta/2, num, delta);
		double aveCOV = 0;
		for(int s=0;s<fltSysSol.getNumSections();s++) {
			double cov = fltSysSol.calcPaloeObsSlipPFD_ForSect(s).getCOV();
			aveCOV += cov;
//			System.out.println("COV: "+cov+" MAX: "+covHist.getMaxX());
			covHist.add(cov, 1.0);
		}
		aveCOV /= fltSysSol.getNumSections();
		
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
			FaultSystemSolution sol = fetcher.getSolution(ref);
			
			File file;
			if (outputDir == null)
				file = null;
			else
				file = new File(outputDir, "paleo_obs_slip_COV_"+scale.getShortName());
			
			plotPaleoObsSlipCOV_Histogram(sol, file);
		}
	}

	/**
	 * @param args
	 * @throws IOException 
	 * @throws ZipException 
	 */
	public static void main(String[] args) throws ZipException, IOException {
		// This is the COMPOUND_SOL.zip file, you can download it from here:
		// http://opensha.usc.edu/ftp/kmilner/ucerf3/2012_10_29-logic-tree-fm3_1_x7-fm3_2_x1/2012_10_29-logic-tree-fm3_1_x7-fm3_2_x1_COMPOUND_SOL.zip
		
		File compoundSolFile = new File("/tmp/2012_10_29-logic-tree-fm3_1_x7-fm3_2_x1_COMPOUND_SOL.zip");
		CompoundFaultSystemSolution fetcher = CompoundFaultSystemSolution.fromZipFile(compoundSolFile);
		
		// output dir
		File outputDir = new File("/tmp");
		writePaleoObsSlipCOV_ForScalingRels(fetcher, outputDir);
	}

}
