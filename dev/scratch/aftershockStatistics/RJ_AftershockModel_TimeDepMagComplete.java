package scratch.aftershockStatistics;

import java.util.ArrayList;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.integration.SimpsonIntegrator;
import org.opensha.commons.data.function.EvenlyDiscretizedFunc;
import org.opensha.commons.data.function.HistogramFunction;
import org.opensha.commons.data.xyz.EvenlyDiscrXYZ_DataSet;
import org.opensha.sha.earthquake.observedEarthquake.ObsEqkRupList;
import org.opensha.sha.earthquake.observedEarthquake.ObsEqkRupture;
import org.opensha.sha.magdist.ArbIncrementalMagFreqDist;
import org.opensha.sha.magdist.GutenbergRichterMagFreqDist;


/**
 * This computes a Reasenberg-Jones (1989, 1994) aftershock model from aftershock data and with an assumed time-dependent 
 * magnitude completeness model described in the constructor.
 * 
 * TODO:
 * 
 *  1) Carefully define jUnit tests in order to cover all cases.
 *
 * @author field
 *
 */
public class RJ_AftershockModel_TimeDepMagComplete extends RJ_AftershockModel implements UnivariateFunction {
	
	Boolean D=true;	// debug flag
	double capG, capH;
	double a, k, p, c;
	ObsEqkRupList aftershockList;
	ObsEqkRupture mainShock;
	double dataStartTimeDays, dataEndTimeDays;

		
	
	/**
	 * This solves for the Reasenberg-Jones parameters from the given mainShock, aftershockList,
	 * and other specifications as described below, and for a time-dependent magnitude of completeness
	 * model defined as Mc(t,Mm) = Max(Mm/2-G-H*log10(t); Mcat), where Mm is the main-shock magnitude, G and H 
	 * are model parameters, and Mcat is the magnitude of completeness for the network during normal times. 
	 * Likelihood values are normalized so they sum to 1.0 over the range of parameter-values specified.
	 * @param mainShock
	 * @param aftershockList - events with mag below magComplete will be filtered out
	 * @param magCat - "Mcat" in the in the time-dependent magnitude of completeness model defined above
	 * @param capG - the "G" parameter in the time-dependent magnitude of completeness model defined above
	 * @param capH - the "H" parameter in the time-dependent magnitude of completeness model defined above
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
	public RJ_AftershockModel_TimeDepMagComplete(ObsEqkRupture mainShock, ObsEqkRupList aftershockList,
			 								double magCat, double capG, double capH,
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
		this.magComplete = magCat;
		this.aftershockList=aftershockList;
		this.mainShock=mainShock;
		this.dataStartTimeDays=dataStartTimeDays;
		this.dataEndTimeDays=dataEndTimeDays;
		this.capG=capG;
		this.capH=capH;
		
		magMain = mainShock.getMag();

		if(num_a>1) // otherwise defaults to zero
			delta_a = (max_a-min_a)/((double)num_a - 1.);
		if(num_p>1)
			delta_p = (max_p-min_p)/((double)num_p - 1.);
		if(num_c>1)
			delta_c = (max_c-min_c)/((double)num_c - 1.);
		
		if(D) {
			System.out.println("a-values range:\t"+min_a+"\t"+max_a+"\t"+num_a+"\t"+(float)delta_a);
			System.out.println("p-values range:\t"+min_p+"\t"+max_p+"\t"+num_p+"\t"+(float)delta_p);
			System.out.println("c-values range:\t"+min_c+"\t"+max_c+"\t"+num_c+"\t"+(float)delta_c);
			System.out.println("capH:\t"+capH);
			System.out.println("capG:\t"+capG);
			System.out.println("magComplete:\t"+magComplete);
		}
		
		computeSequenceSpecificParams();
		
	}
	
	private void computeSequenceSpecificParams() {
//		SimpsonIntegrator integrator = new SimpsonIntegrator();
		array = new double[num_a][num_p][num_c];
		double maxVal= Double.NEGATIVE_INFINITY;
		double ln10 = Math.log(10);
		for(int cIndex=0;cIndex<num_c;cIndex++) {
			c = get_c(cIndex);

			// make the list of event times and Mc at those times for the given c
			double sum1=0;
			double sum2=0;
			int numEvents=0;
			for(ObsEqkRupture rup:aftershockList) {
				double timeSinceMainDays = (double)(rup.getOriginTime()-mainShock.getOriginTime()) / (double)AftershockStatsCalc.MILLISEC_PER_DAY;
				if(timeSinceMainDays<dataStartTimeDays || timeSinceMainDays>dataEndTimeDays) // not necessary if list already filtered
					continue;
				double magCompleteAtTime = getMagCompleteAtTime(timeSinceMainDays);
//				System.out.println("magCompleteAtTime"+magCompleteAtTime);

				if(rup.getMag()>=magCompleteAtTime) {
					numEvents += 1;
					sum1 += magMain-magCompleteAtTime;
					sum2 += Math.log(timeSinceMainDays+c);
				}
			}

			// now loop over p and a
			for(int pIndex=0;pIndex<num_p;pIndex++) {
				p = get_p(pIndex);
				for(int aIndex=0;aIndex<num_a;aIndex++) {
					a = get_a(aIndex);
					double integral = AftershockStatsCalc.adaptiveQuadratureIntegration(this, dataStartTimeDays, dataEndTimeDays);
//					double integral = integrator.integrate(100000, this, dataStartTimeDays, dataEndTimeDays);
//double term1=numEvents*a*ln10 + b*ln10*sum1 - p*sum2;
//System.out.println("term1="+term1);
//System.out.println("integral="+integral);


					double logLike = numEvents*a*ln10 + b*ln10*sum1 - p*sum2 - integral;
//  System.out.println((float)a+"\t"+(float)p+"\t"+(float)c+"\t"+logLike);

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
		
		// convert array from log-likelihood to likelihood
		double testTotal = convertArrayToLikelihood(maxVal);
		
		if (D) {
			System.out.println("testTotal="+testTotal);
			System.out.println("getMaxLikelihood_a()="+getMaxLikelihood_a());
			System.out.println("getMaxLikelihood_p()="+getMaxLikelihood_p());
			System.out.println("getMaxLikelihood_c()="+getMaxLikelihood_c());
		}

	}

	public double getMagCompleteAtTime(double timeSinceMainDays) {
		if(timeSinceMainDays==0d)
			return 10d;	// avoid infinity
		double magCompleteAtTime = magMain/2.0 - capG - capH*Math.log10(timeSinceMainDays);
		if(magCompleteAtTime>magComplete)
			return magCompleteAtTime;
		else 
			return magComplete;

	}

	public double getRateAboveMagCompleteAtTime(double timeSinceMainDays) {
		if(timeSinceMainDays==0d)
			return 0d;
		return Math.pow(10d,a+b*(magMain-getMagCompleteAtTime(timeSinceMainDays)))*Math.pow(timeSinceMainDays+c, -p);
	}

	public double value(double timeSinceMainDays) {
		return getRateAboveMagCompleteAtTime(timeSinceMainDays);
	}
	

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
