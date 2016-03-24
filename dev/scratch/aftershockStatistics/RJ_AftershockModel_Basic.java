package scratch.aftershockStatistics;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.integration.IterativeLegendreGaussIntegrator;
import org.apache.commons.math3.analysis.integration.LegendreGaussIntegrator;
import org.apache.commons.math3.analysis.integration.RombergIntegrator;
import org.apache.commons.math3.analysis.integration.SimpsonIntegrator;
import org.apache.commons.math3.analysis.integration.TrapezoidIntegrator;
import org.jfree.data.Range;
import org.opensha.commons.data.function.EvenlyDiscretizedFunc;
import org.opensha.commons.data.function.HistogramFunction;
import org.opensha.commons.data.xyz.EvenlyDiscrXYZ_DataSet;
import org.opensha.commons.gui.plot.jfreechart.xyzPlot.XYZPlotSpec;
import org.opensha.commons.gui.plot.jfreechart.xyzPlot.XYZPlotWindow;
import org.opensha.commons.mapping.gmt.elements.GMT_CPT_Files;
import org.opensha.commons.util.cpt.CPT;
import org.opensha.sha.earthquake.observedEarthquake.ObsEqkRupList;
import org.opensha.sha.earthquake.observedEarthquake.ObsEqkRupture;
import org.opensha.sha.magdist.ArbIncrementalMagFreqDist;
import org.opensha.sha.magdist.GutenbergRichterMagFreqDist;


/**
 * This represents a Reasenberg-Jones (1989, 1994) aftershock model that assumes the magnitude completeness
 * value is constant (does not change with time).
 * 
 * This class gives the same result as RJ_AftershockModel_TimeDepMagComplete if capG in the latter is a large value,
 * but this is faster and more accurate because analytical expressions are used for the integration.
 * 
 * TODO:
 * 
 *  1) Carefully define jUnit tests that cover all cases, including the p=1 type cases, especially if this is used
 *  in an operational system.
 *
 * @author field
 *
 */
public class RJ_AftershockModel_Basic extends RJ_AftershockModel {
	
	Boolean D=true;	// debug flag
	double[] relativeEventTimes;
	double a, k, p, c;

	
	/**
	 * This solves for the Reasenberg-Jones parameters from the given mainShock, aftershockList,
	 * and other specifications as described below. Likelihood values are normalized so they sum to
	 * 1.0 over the range of parameter-values specified.
	 * @param mainShock
	 * @param aftershockList - events with mag below magComplete will be filtered out
	 * @param magComplete - assumed magnitude of completeness
	 * @param b - assumed b value
	 * @param min_a \
	 * @param max_a  | - range of a-values for grid search (set min=max and num=1 to constraint to single value)
	 * @param num_a /
	 * @param min_p \
	 * @param max_p  | - range of p-values for grid search (set min=max and num=1 to constraint to single value)
	 * @param num_p /
	 * @param min_c \
	 * @param max_c  | - range of c-values for grid search (set min=max and num=1 to constraint to single value)
	 * @param num_c /
	 */
	public RJ_AftershockModel_Basic(	ObsEqkRupture mainShock, ObsEqkRupList aftershockList, double magComplete, 
											double b, double dataStartTimeDays, double dataEndTimeDays,
											double min_a, double max_a, int num_a, 
											double min_p, double max_p, int num_p, 
											double min_c, double max_c, int num_c) {
		
		// check range values
		if(num_a == 1 && min_a != max_a) {
			throw new RuntimeException("Problem: num_a == 1 && min_a != max_a");
		}
		if(num_p == 1 && min_p != max_p) {
			throw new RuntimeException("Problem: num_p == 1 && min_p != max_p");
		}
		if(num_c == 1 && min_c != max_c) {
			throw new RuntimeException("Problem: num_c == 1 && min_c != max_c");
		}
		if(min_a > max_a) {
			throw new RuntimeException("Problem: min_a > max_a");
		}
		if(min_p > max_p) {
			throw new RuntimeException("Problem: min_p > max_p");
		}
		if(min_c > max_c) {
			throw new RuntimeException("Problem: min_c > max_c");
		}

		
		this.min_a = min_a;
		this.max_a = max_a;
		this.num_a = num_a;
		this.min_p = min_p;
		this.max_p = max_p;
		this.num_p = num_p;
		this.min_c = min_c;
		this.max_c = max_c;
		this.num_c = num_c;
		this.b = b;
		this.magComplete = magComplete;
		
		magMain = mainShock.getMag();
		relativeEventTimes = AftershockStatsCalc.getDaysSinceMainShockArray(mainShock, aftershockList.getRupsAboveMag(magComplete));
		
		if(D)
			System.out.println("Num Aftershocks above magComplete: "+relativeEventTimes.length);

		
		if(num_a>1) // otherwise defaults to zero
			delta_a = (max_a-min_a)/((double)num_a - 1.);
		if(num_p>1)
			delta_p = (max_p-min_p)/((double)num_p - 1.);
		if(num_c>1)
			delta_c = (max_c-min_c)/((double)num_c - 1.);
		
		if(D) {
			System.out.println("a-values range:\t"+min_a+"\t"+max_a+"\t"+num_a+"\t"+delta_a);
			System.out.println("p-values range:\t"+min_p+"\t"+max_p+"\t"+num_p+"\t"+delta_p);
			System.out.println("c-values range:\t"+min_c+"\t"+max_c+"\t"+num_c+"\t"+delta_c);
			System.out.println("magComplete:\t"+magComplete);
		}
		
		// alternative integration schemes
//		SimpsonIntegrator integrator = new SimpsonIntegrator();
//		IterativeLegendreGaussIntegrator integrator = new IterativeLegendreGaussIntegrator();
//		LegendreGaussIntegrator integrator = new LegendreGaussIntegrator();
//		RombergIntegrator integrator = new RombergIntegrator();
//		TrapezoidIntegrator integrator = new TrapezoidIntegrator();
//		MidPointIntegrator integrator = new MidPointIntegrator();

		array = new double[num_a][num_p][num_c];
		double maxVal= Double.NEGATIVE_INFINITY;
		long startTime = System.currentTimeMillis();
		for(int aIndex=0;aIndex<num_a;aIndex++) {
			a = get_a(aIndex);
			k = AftershockStatsCalc.convertProductivityTo_k(a, b, magMain, magComplete);
			for(int pIndex=0;pIndex<num_p;pIndex++) {
				p = get_p(pIndex);
				for(int cIndex=0;cIndex<num_c;cIndex++) {
					c = get_c(cIndex);

					double logLike = AftershockStatsCalc.getLogLikelihoodForOmoriParams(k, p, c, dataStartTimeDays, dataEndTimeDays, relativeEventTimes);
					
					if(D) {
						// test numerical integration results
						double sumLn_t=0;
						for(double t : relativeEventTimes)
							sumLn_t += Math.log(t+c);
					//	double integral = integrator.integrate(100000, this, dataStartTimeDays, dataEndTimeDays);
						double integral = AftershockStatsCalc.adaptiveQuadratureIntegration(this, dataStartTimeDays, dataEndTimeDays);
						double logLike2 =  relativeEventTimes.length*Math.log(k) - p*sumLn_t - integral;
						double ratio = logLike/logLike2;
						if((float)ratio != 1f)
							throw new RuntimeException("bad ratio "+ratio);
						//						System.out.println("ratio:\t"+(float)ratio+"\t"+logLike+"\t"+logLike2);
					}

// System.out.println(a+"\t"+p+"\t"+c+"\t"+logLike+"\t"+Math.exp(logLike));

					array[aIndex][pIndex][cIndex] = logLike;
					if(maxVal<logLike) {
						maxVal=logLike;
						max_a_index=aIndex;
						max_p_index=pIndex;
						max_c_index=cIndex;
					}
				}
			}
		}
		long runtime = System.currentTimeMillis()-startTime;
		System.out.println("Runtime (sec): "+(double)runtime/1000d);
		
		double testTotal = convertArrayToLikelihood(maxVal);

		if (D) {
			System.out.println("maxVal="+maxVal);
			System.out.println("testTotal="+testTotal);
			System.out.println("getMaxLikelihood_a()="+getMaxLikelihood_a());
			System.out.println("getMaxLikelihood_p()="+getMaxLikelihood_p());
			System.out.println("getMaxLikelihood_c()="+getMaxLikelihood_c());
		}
		
	}
	
	
	public double getRateAboveMagCompleteAtTime(double numDays) {
		return k*Math.pow(numDays+c,-p);
	}
	
	

	public static void main(String[] args) {
		double c = 0.05;
		
		double k_min = 17;
		double k_max = 34;
		double k_delta = 0.25;

		double p_min = 0.9; 
		double p_max = 1.15; 
		double p_delta = 0.0125;
		
		int k_num = (int)Math.round((k_max-k_min)/k_delta) + 1;		
		int p_num = (int)Math.round((p_max-p_min)/p_delta) + 1;
		
		
		if(true) {
			System.out.println("k1\t"+k_min+"\t"+k_max+"\t"+k_num+"\t"+k_delta);
			System.out.println("p1\t"+p_min+"\t"+p_max+"\t"+p_num+"\t"+p_delta);
		}

		
		double[] relativeEventTimes = AftershockStatsCalc.readAndysFile();
		double tStartDays = relativeEventTimes[0];
		double tEndDays = relativeEventTimes[relativeEventTimes.length-1];

		
		// test model assuming b=1
		double b = 1;
		double magMain = 7;	// assumed value for Andy's data
		double magComplete = 5;	// assumed value for Andy's data
		double a_min = AftershockStatsCalc.convertProductivityTo_a(k_min, b, magMain, magComplete);
		double a_max = AftershockStatsCalc.convertProductivityTo_a(k_max, b, magMain, magComplete);
		int a_num = k_num;
		ObsEqkRupture dummyMainShock = new ObsEqkRupture();
		dummyMainShock.setMag(magMain);
		dummyMainShock.setOriginTime(0l);
		ObsEqkRupList dummyAftershocks = new ObsEqkRupList();
		for(double relTime:relativeEventTimes) {
			ObsEqkRupture newRup = new ObsEqkRupture();
			long ot = (long)(relTime*(double)AftershockStatsCalc.MILLISEC_PER_DAY);
			newRup.setOriginTime(ot);
			newRup.setMag(magComplete+1); // anything above magComplete);
			dummyAftershocks.add(newRup);
		}
		
		RJ_AftershockModel distArray = new RJ_AftershockModel_Basic(dummyMainShock, dummyAftershocks, magComplete, b,
				tStartDays, tEndDays, a_min, a_max, a_num, p_min, p_max, p_num, c, c, 1);
		System.out.println("max likelihood gridded k =  "+distArray.getMaxLikelihood_k());
		System.out.println("distArray.getPDF_a():\n"+distArray.getPDF_a());
		System.out.println("distArray.getPDF_p():\n"+distArray.getPDF_p());
		System.out.println("distArray.getPDF_c():\n"+distArray.getPDF_c());
		AftershockStatsCalc.plot2D_PDF(distArray.get2D_PDF_for_a_and_p(), "PDF for a vs p", "a", "p", "density");
		EvenlyDiscretizedFunc mfd = distArray.getExpectedCumNumMFD(3.0, 10.0, 71, 0.0, 7.0);
		System.out.println("distArray.getExpectedNumMFD():\n"+mfd);

		
	}

}
