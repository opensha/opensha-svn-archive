package scratch.kevin.ucerf3.inversion;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.math3.stat.StatUtils;
import org.dom4j.DocumentException;
import org.opensha.commons.calc.FaultMomentCalc;
import org.opensha.commons.data.function.DiscretizedFunc;
import org.opensha.commons.data.function.HistogramFunction;
import org.opensha.commons.gui.plot.PlotLineType;
import org.opensha.commons.gui.plot.PlotSymbol;
import org.opensha.sha.gui.infoTools.GraphiWindowAPI_Impl;
import org.opensha.sha.gui.infoTools.PlotCurveCharacterstics;

import com.google.common.collect.Lists;

import scratch.UCERF3.FaultSystemSolution;
import scratch.UCERF3.SimpleFaultSystemSolution;
import scratch.UCERF3.enumTreeBranches.FaultModels;
import scratch.UCERF3.inversion.UCERF2_ComparisonSolutionFetcher;
import scratch.UCERF3.utils.UCERF3_DataUtils;

public class RupLengthHistogramGenerator {

	/**
	 * @param args
	 * @throws DocumentException 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException, DocumentException {
		File dir = new File(UCERF3_DataUtils.DEFAULT_SCRATCH_DATA_DIR, "InversionSolutions");
		File solFile = new File(dir, "2013_01_14-stampede_3p2_production_runs_combined_FM3_1_MEAN_BRANCH_AVG_SOL.zip");
		SimpleFaultSystemSolution sol = SimpleFaultSystemSolution.fromFile(solFile);
		
//		double scaleToTotal = 246;
		double scaleToTotal = 1;
		boolean doU2 = false;
		
		FaultSystemSolution u2sol = null;
		if (doU2)
			u2sol = UCERF2_ComparisonSolutionFetcher.getUCERF2Solution(FaultModels.FM2_1);
		
		HistogramFunction hist = new HistogramFunction(25, 25, 50d);
		HistogramFunction u2hist = new HistogramFunction(25, 25, 50d);
		HistogramFunction asDisrHist = new HistogramFunction(25, 25, 50d);
		HistogramFunction asDisrU2Hist = new HistogramFunction(25, 25, 50d);
		HistogramFunction momentHist = new HistogramFunction(25, 25, 50d);
		HistogramFunction u2momentHist = new HistogramFunction(25, 25, 50d);
		double maxLen = hist.getMaxX()+25;
		
		double totRate = StatUtils.sum(sol.getRateForAllRups());
		double rateScale = 1;
		if (scaleToTotal != 1)
			rateScale = scaleToTotal / totRate;
		
		for (int r=0; r<sol.getNumRuptures(); r++) {
			double rate = sol.getRateForRup(r);
			double moment = FaultMomentCalc.getMoment(sol.getAreaForRup(r), sol.getAveSlipForRup(r));
			double scaledRate = rate*rateScale;
			double length = sol.getLengthForRup(r)/1000d; // m to km
			
			if (length < maxLen) {
				hist.add(length, scaledRate);
				momentHist.add(length, moment*rate);
				asDisrHist.add(length, 1d);
			}
		}
//		asDisrHist.scale(1d/totRate);
		
		if (doU2) {
			totRate = StatUtils.sum(u2sol.getRateForAllRups());
			if (scaleToTotal != 1)
				rateScale = scaleToTotal / totRate;
			
			for (int r=0; r<u2sol.getNumRuptures(); r++) {
				double rate = u2sol.getRateForRup(r);
				double moment = FaultMomentCalc.getMoment(u2sol.getAreaForRup(r), u2sol.getAveSlipForRup(r));
				double scaledRate = rate*rateScale;
				double length = u2sol.getLengthForRup(r)/1000d; // m to km
				
				if (length < maxLen) {
					u2hist.add(length, scaledRate);
					u2momentHist.add(length, moment*rate);
					asDisrU2Hist.add(length, 1d);
				}
			}
//			asDisrU2Hist.scale(1d/totRate);
		}
		
		ArrayList<DiscretizedFunc> funcs = Lists.newArrayList();
		ArrayList<PlotCurveCharacterstics> chars = Lists.newArrayList();
		
		funcs.add(hist);
		if (doU2)
			funcs.add(u2hist);
		chars.add(new PlotCurveCharacterstics(PlotLineType.HISTOGRAM, 1f, Color.BLUE));
		chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, PlotSymbol.FILLED_CIRCLE, 5f, Color.RED));
		
		String title;
		if (scaleToTotal != 1)
			title = "Lengh Histogram (scaled to "+(int)scaleToTotal+" ruptures)";
		else
			title = "Length Histogram";
		GraphiWindowAPI_Impl gw = new GraphiWindowAPI_Impl(funcs, title, chars);
		gw.setX_AxisLabel("Length (km)");
		if (scaleToTotal != 1)
			gw.setY_AxisLabel("Num");
		else
			gw.setY_AxisLabel("Rate (1/yr)");
		
		funcs = Lists.newArrayList();
		funcs.add(asDisrHist);
		if (doU2)
			funcs.add(asDisrU2Hist);
		gw = new GraphiWindowAPI_Impl(funcs, "Length Histogram (as discretized)", chars);
		gw.setX_AxisLabel("Length (km)");
		gw.setY_AxisLabel("Num Ruptures");
		
		funcs = Lists.newArrayList();
		funcs.add(momentHist);
		if (doU2)
			funcs.add(u2momentHist);
		new GraphiWindowAPI_Impl(funcs, "Moment Rate Histogram", chars);
		
		funcs = Lists.newArrayList();
		funcs.add(momentHist.getCumulativeDistFunction());
		if (doU2)
			funcs.add(u2momentHist.getCumulativeDistFunction());
		chars.clear();
		chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, Color.BLUE));
		chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, PlotSymbol.FILLED_CIRCLE, 5f, Color.RED));
		new GraphiWindowAPI_Impl(funcs, "Cumulative Moment Rates", chars);
	}

}
