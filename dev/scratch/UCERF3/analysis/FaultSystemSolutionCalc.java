package scratch.UCERF3.analysis;

import org.opensha.commons.data.function.HistogramFunction;
import org.opensha.sha.gui.infoTools.GraphiWindowAPI_Impl;

import scratch.UCERF3.FaultSystemSolution;

public class FaultSystemSolutionCalc {
	
	public static void plotPaleoObsSlipCOV_Histogram(FaultSystemSolution fltSysSol) {
		
		double delta = 0.02;
		int num = (int)Math.round(2.0/0.025);
		
		HistogramFunction covHist = new HistogramFunction(delta/2, num, delta);
		double aveCOV = 0;
		for(int s=0;s<fltSysSol.getNumSections();s++) {
			double cov = fltSysSol.calcPaloeObsSlipPFD_ForSect(s).getCOV();
			aveCOV += cov;
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
		String fileName = "COV_Histogram.pdf";
//		if(fileName != null) {
//			try {
//				graph.saveAsPDF(fileName);
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}			
//		}			

		
		
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
