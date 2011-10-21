package scratch.kevin.ucerf3;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.opensha.commons.data.TimeSpan;
import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.commons.data.function.DiscretizedFunc;
import org.opensha.commons.data.function.EvenlyDiscretizedFunc;
import org.opensha.commons.gui.plot.PlotLineType;
import org.opensha.commons.gui.plot.PlotSymbol;
import org.opensha.commons.util.DataUtils.MinMaxAveTracker;
import org.opensha.sha.earthquake.ERF;
import org.opensha.sha.earthquake.ProbEqkRupture;
import org.opensha.sha.earthquake.ProbEqkSource;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.UCERF2;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.MeanUCERF2.MeanUCERF2;
import org.opensha.sha.gui.infoTools.GraphiWindowAPI_Impl;
import org.opensha.sha.gui.infoTools.PlotCurveCharacterstics;

import scratch.UCERF3.utils.MatrixIO;

public class PureScratch {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		double minX = -9d;
		double maxX = 0d;
		int num = 200;
		EvenlyDiscretizedFunc ucerf2Func = new EvenlyDiscretizedFunc(minX, maxX, num);
		double delta = ucerf2Func.getDelta();
		ucerf2Func.setName("UCERF2");
		
		boolean doUCERF2 = true;
		
		if (doUCERF2) {
			System.out.println("Creating UCERF2");
			ERF erf = new MeanUCERF2();
			erf.setParameter(UCERF2.PROB_MODEL_PARAM_NAME, UCERF2.PROB_MODEL_POISSON);
			erf.setParameter(UCERF2.BACK_SEIS_NAME, UCERF2.BACK_SEIS_EXCLUDE);
			erf.getTimeSpan().setDuration(1, TimeSpan.YEARS);
			erf.updateForecast();
			
			System.out.println("Setting UCERF2 rates");
			for (ProbEqkSource source : erf) {
				for (ProbEqkRupture rup : source) {
					if (Math.random() > 0.2d)
						continue;
					double prob = rup.getProbability();
					double log10prob = Math.log10(prob);
					if (log10prob < minX || log10prob > maxX) {
						System.out.println("Prob outside of bounds: "+prob + " (log10: "+log10prob+")");
					}
					int ind = (int)Math.round((log10prob-minX)/delta);
					ucerf2Func.set(ind, ucerf2Func.getY(ind)+1);
				}
			}
		}
		
		File dir = new File("/home/kevin/OpenSHA/UCERF3/test_inversion/bench/2011_09_08-morgan-CS_fixed");
		File binFile = new File(dir, "run1.mat");
		
		double[] rupRateSolution = MatrixIO.doubleArrayFromFile(binFile);
		int numNonZero = 0;
		for (double rate : rupRateSolution)
			if (rate > 0)
				 numNonZero++;
		double[] nonZeros = new double[numNonZero];
		int cnt = 0;
		for (double rate : rupRateSolution) {
			if (rate > 0)
				nonZeros[cnt++] = rate;
		}
		EvenlyDiscretizedFunc inversionFunc = new EvenlyDiscretizedFunc(minX, maxX, num);
		inversionFunc.setName("UCERF3 Inversion");
		
		
		System.out.println("Setting inversion rates");
		for (int i=0; i<nonZeros.length; i++) {
			double log10rate = Math.log10(nonZeros[i]);
			if (log10rate < minX || log10rate > maxX) {
				System.out.println("Prob outside of bounds: "+nonZeros[i] + " (log10: "+log10rate+")");
			}
			int ind = (int)Math.round((log10rate-minX)/delta);
			inversionFunc.set(ind, inversionFunc.getY(ind)+1);
		}
		
		ArrayList<DiscretizedFunc> funcs = new ArrayList<DiscretizedFunc>();
		funcs.add(ucerf2Func);
		funcs.add(inversionFunc);
		
		ArrayList<PlotCurveCharacterstics> chars = new ArrayList<PlotCurveCharacterstics>();
		chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 1f, Color.BLACK));
		chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 1f, Color.BLUE));
		
		System.out.println("Displaying graph!");
		
		new GraphiWindowAPI_Impl(funcs, "Rupture Rates", chars);
		System.out.println("DONE");
	}

}
