package scratch.aftershockStatistics;

import java.util.ArrayList;

import org.opensha.commons.data.function.EvenlyDiscretizedFunc;
import org.opensha.commons.data.function.HistogramFunction;
import org.opensha.commons.data.xyz.EvenlyDiscrXYZ_DataSet;
import org.opensha.sha.earthquake.observedEarthquake.ObsEqkRupList;
import org.opensha.sha.earthquake.observedEarthquake.ObsEqkRupture;
import org.opensha.sha.magdist.ArbIncrementalMagFreqDist;
import org.opensha.sha.magdist.GutenbergRichterMagFreqDist;


/**
 * This is an old version based on analytical integration results given by Jeanne Hardebeck, but Morgan
 * ended up using a different formulation that requires numerical integration.
 *
 * @author field
 *
 */
@Deprecated
public class RJ_AftershockModel_TimeDepMagCompleteOld extends RJ_AftershockModel {
	
	Boolean D=true;	// debug flag
		
	
	/**
	 * This solves for the Reasenberg-Jones parameters from the given mainShock, aftershockList,
	 * and other specifications as described below, and for a time-dependent magnitude of completeness
	 * model defined as Mc(t,Mm) = Max(Mm-G-H*log10(t+c); Mcat), where Mm is the main-shock magnitude, G and H 
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
	public RJ_AftershockModel_TimeDepMagCompleteOld(	ObsEqkRupture mainShock, ObsEqkRupList aftershockList,
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
		}
		
		array = new double[num_a][num_p][num_c];
		double maxVal= Double.NEGATIVE_INFINITY;
		for(int cIndex=0;cIndex<num_c;cIndex++) {
			double c = get_c(cIndex);

			// make the list of event times and Mc at those times for the given c
			ArrayList<Double> timesForFilteredEventsList = new ArrayList<Double>();
			ArrayList<Double> magCompleteForFilteredEventsList = new ArrayList<Double>();
			double sum1=0;
			double sum2=0;
			for(ObsEqkRupture rup:aftershockList) {
				double timeSinceMainDays = (double)(rup.getOriginTime()-mainShock.getOriginTime()) / (double)AftershockStatsCalc.MILLISEC_PER_DAY;
				if(timeSinceMainDays<dataStartTimeDays || timeSinceMainDays>dataEndTimeDays) // not necessary if list already filtered
					continue;
				double magCompleteAtTime = magMain - capG - capH*Math.log10(timeSinceMainDays+c);
				if(magCompleteAtTime<magCat)
					magCompleteAtTime=magCat;
//System.out.println("TESTEVENTS\t"+(float)timeSinceMainDays+"\t"+(float)rup.getMag()+"\t"+(float)magCompleteAtTime+"\t"+(rup.getMag()<magCompleteAtTime));

				if(rup.getMag()>magCompleteAtTime) {
					timesForFilteredEventsList.add(timeSinceMainDays);
					magCompleteForFilteredEventsList.add(magCompleteAtTime);
					sum1 += magMain-magCompleteAtTime;
					sum2 += Math.log(timeSinceMainDays+c);
				}
			}

			double crossOverTime = Math.pow(10.0, (magMain-magCat-capG)/capH) - c;
			
			if(D) {
				System.out.println("crossOverTime:"+"\t"+crossOverTime);
				System.out.println("magMain:"+"\t"+magMain);
			}

			int n = timesForFilteredEventsList.size();
			double ln10 = Math.log(10);

			// now loop over p and a
			for(int pIndex=0;pIndex<num_p;pIndex++) {
				double p = get_p(pIndex);
				for(int aIndex=0;aIndex<num_a;aIndex++) {
					double a = get_a(aIndex);
					double logLike = 0;
					double logLikeTest=0;
					double term1 = n*a*ln10 + b*ln10*sum1 - p*sum2;
					double term2 = 0;
					double term3 = 0;
					double k = AftershockStatsCalc.convertProductivityTo_k(a, b, magMain, magCat);
					if(dataStartTimeDays>=crossOverTime) {	// no time-dep; use equation (8)
						term2 = -k*getFuncF(c, p, dataStartTimeDays, dataEndTimeDays);
						// test (slower way of doing it): TODO
						double[] relativeEventTimes = new double[timesForFilteredEventsList.size()];
						for(int i=0;i<timesForFilteredEventsList.size();i++)
							relativeEventTimes[i] = timesForFilteredEventsList.get(i);
						logLikeTest = AftershockStatsCalc.getLogLikelihoodForOmoriParams(k, p, c, dataStartTimeDays, dataEndTimeDays, relativeEventTimes);
					}
					else if (dataEndTimeDays<crossOverTime) {	// use equation (9)
						double temp = Math.pow(10, a+b*capG);
						term2 = -temp*getFuncF(c, p-b*capH, dataStartTimeDays, dataEndTimeDays);
					}
					else {	// use equation (7)
						// do the start-to-cross time (p+b*capH) term first
						term2 = -Math.pow(10, a+b*capG)*getFuncF(c, p-b*capH, dataStartTimeDays, crossOverTime);
						// now do the cross-to-end time (p) term
						term3 = -Math.pow(10, a+b*(magMain-magCat))*getFuncF(c, p, crossOverTime, dataEndTimeDays);
					}

					logLike = term1+term2+term3;
//					if(Double.isNaN(logLike)) {
//					System.out.println("NaN:\t"+(float)get_a(aIndex)+"\t"+(float)get_p(pIndex)+"\t"+logLike+"\t"+term1+"\t"+term2+"\t"+term3);
//				}
					
					// test implementation of equation (8) against other method
					if(logLikeTest != 0) {
						float test = (float)(logLike/logLikeTest);
						if(test != 1.0)
							throw new RuntimeException("LogLikeTest failed");
					}

// System.out.println((float)get_a(aIndex)+"\t"+(float)get_p(pIndex)+"\t"+(float)get_c(cIndex)+"\t"+logLike+"\t"+like+"\t"+term1+"\t"+term2+"\t"+term3);

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
		
		double testTotal = convertArrayToLikelihood(maxVal);
		
		if (D) {
			System.out.println("testTotal="+testTotal);
			System.out.println("getMaxLikelihood_a()="+getMaxLikelihood_a());
			System.out.println("getMaxLikelihood_p()="+getMaxLikelihood_p());
			System.out.println("getMaxLikelihood_c()="+getMaxLikelihood_c());
		}
		
	}

	
	/**
	 * This is the function defined in Jeanne's document
	 * @param c
	 * @param x
	 * @param t1
	 * @param t2
	 * @return
	 */
	private static double getFuncF(double c, double x, double t1, double t2) {
		if(x==1.0) {
			return Math.log(t2+c) - Math.log(t1+c);
		}
		else {
			return (Math.pow(t2+c,1-x) - Math.pow(t1+c,1-x))/(1-x);
		}
	}
	
	
	public double getRateAboveMagCompleteAtTime(double numDays) {
		return Double.NaN;
	}

	

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
