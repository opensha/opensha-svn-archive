package scratch.aftershockStatisticsETAS;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.integration.SimpsonIntegrator;
import org.opensha.commons.data.function.EvenlyDiscretizedFunc;
import org.opensha.commons.data.function.HistogramFunction;
import org.opensha.commons.data.xyz.EvenlyDiscrXYZ_DataSet;
import org.opensha.sha.earthquake.observedEarthquake.ObsEqkRupList;
import org.opensha.sha.earthquake.observedEarthquake.ObsEqkRupture;
import org.opensha.sha.magdist.ArbIncrementalMagFreqDist;
import org.opensha.sha.magdist.GutenbergRichterMagFreqDist;

import com.google.common.primitives.Doubles;


/**
 * This computes an ETAS aftershock model from aftershock data and with an assumed time-dependent 
 * magnitude completeness model described in the constructor.
 * 
 * TODO:
 * 
 *  1) Carefully define jUnit tests in order to cover all cases.
 *
 * @author field
 *
 */
public class ETAS_AftershockModel_SequenceSpecific extends ETAS_AftershockModel implements UnivariateFunction {
	
	Boolean D=true;	// debug flag
//	double alpha = 1;
//	double mu = 1;
	double capG, capH;
	double a, k, p, c;	// these are used in the numerical integration
//	
	
	
	/**
	 * PLACEHOLDER: NOT IMPLEMENTED.
	 * Use this constructor to apply a time-independent magnitude of completeness.  This is faster because it
	 * uses an analytical solution for the integral. 
	 * 
	 */
	public ETAS_AftershockModel_SequenceSpecific(ObsEqkRupture mainshock, ObsEqkRupList aftershocks,
				double magCat, double[] aVec, double[] pVec, double[] cVec, double alpha, double b, double refMag, 	
				double dataStartTimeDays, double dataEndTimeDays, double forecastMinDays, double forecastMaxDays, 
				double maxMag, int maxGenerations, int nSims) {
		
		this(mainshock, aftershocks, magCat, Double.NaN, Double.NaN, aVec, pVec, cVec, alpha, b, refMag,
				dataStartTimeDays, dataEndTimeDays, forecastMinDays, forecastMaxDays, 
				maxMag, maxGenerations, nSims);
	}

	
	
	/**
	 * This solves for the Reasenberg-Jones parameters from the given mainShock, aftershockList,
	 * and other specifications as described below, and for a time-dependent magnitude of completeness
	 * model defined as Mc(t,Mm) = Max(Mm/2-G-H*log10(t); Mcat), where Mm is the main-shock magnitude, G and H 
	 * are model parameters, and Mcat is the magnitude of completeness for the network during normal times. 
	 * Likelihood values are normalized so they sum to 1.0 over the range of parameter-values specified.
	 * @param mainShock
	 * @param aftershockList - events with mag below magComplete will be filtered out
	 * @param magCat - "Mcat" in the in the time-dependent magnitude of completeness model defined above
	 * @param capG - the "G" parameter in the time-dependent magnitude of completeness model defined above; 
	 * 				 set as Double.NaN to apply time independent Mc (analytical integration), or set as 
	 * 				 10.0 to effectively make it time independent (but numerical integration still performed)
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
	public ETAS_AftershockModel_SequenceSpecific(ObsEqkRupture mainShock, ObsEqkRupList aftershockList,
			 								double magCat, double capG, double capH,
			 								double[] aVec, double[] pVec, double[] cVec, double alpha, double b, double refMag, 	
			 								double dataStartTimeDays, double dataEndTimeDays, double forecastMinDays, double forecastMaxDays, 
			 								double maxMag, int maxGenerations, int nSims) {
		
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

		this.min_a = aVec[0];
		this.max_a = aVec[aVec.length-1];
		this.num_a = aVec.length;
		this.min_p = pVec[0];
		this.max_p = pVec[pVec.length-1];
		this.num_p = pVec.length;
		this.min_c = cVec[0];
		this.max_c = cVec[cVec.length-1];
		this.num_c = cVec.length;
		
		this.a_vec = aVec;
		this.p_vec = pVec;
		this.c_vec = cVec;
		
		this.b = b;
		this.alpha = alpha;
		this.refMag = refMag;
		this.magComplete = magCat;
		this.aftershockList=aftershockList;
//		this.aftershockList = new ObsEqkRupList();
		this.mainShock=mainShock;
		this.magMain = mainShock.getMag();
		this.dataStartTimeDays=dataStartTimeDays;
		this.dataEndTimeDays=dataEndTimeDays;
		this.forecastMinDays = forecastMinDays;
		this.forecastMaxDays = forecastMaxDays;
		this.capG=capG;
		this.capH=capH;
		this.nSims = nSims;
		this.maxMag = maxMag;
		this.maxGenerations = maxGenerations;
		

//		if(num_a>1) // otherwise defaults to zero
//			delta_a = (max_a-min_a)/((double)num_a - 1.);
//		if(num_p>1)
//			delta_p = (max_p-min_p)/((double)num_p - 1.);
//		if(num_c>1)
//			delta_c = (max_c-min_c)/((double)num_c - 1.);

		
		
		if(D) {
			System.out.println("a-values range:\t"+min_a+"\t"+max_a+"\t"+num_a);
			System.out.println("p-values range:\t"+min_p+"\t"+max_p+"\t"+num_p+"\t");
			System.out.println("log c-values range:\t"+min_c+"\t"+max_c+"\t"+num_c+"\t");
			System.out.println("capH:\t"+capH);
			System.out.println("capG:\t"+capG);
			System.out.println("magComplete:\t"+magComplete);
		}
		
//		if(Double.isNaN(capG))
		
		// Find the max like parameters by grid search
		System.out.println("finding maximum likelihood parameters...");
		getLikelihoodMatrixGrid();
			
	
//		else
			//computeSequenceSpecificParams();
		
		if (D) {
			System.out.println("testTotalLikelihood="+testTotalLikelihood);
			System.out.println("getMaxLikelihood_a()="+getMaxLikelihood_a());
			System.out.println("getMaxLikelihood_p()="+getMaxLikelihood_p());
			System.out.println("getMaxLikelihood_c()="+getMaxLikelihood_c());
		}
		
		
		// get aftershock times and mags and store as simple doubles[]
		double[] relativeEventTimes = AftershockStatsCalc.getDaysSinceMainShockArray(mainShock, aftershockList.getRupsAboveMag(magComplete));
		double[] magAftershocks = getAftershockMags(aftershockList.getRupsAboveMag(magComplete));

		List<double[]> sortedEQlist = new ArrayList<double[]>();

		System.out.println(relativeEventTimes.length + " "+ dataEndTimeDays +" "+ magComplete);

		for(int i = 0; i < relativeEventTimes.length; i++){
			double[] temp = new double[]{relativeEventTimes[i], magAftershocks[i]};
			if(temp[0] < dataEndTimeDays)
				sortedEQlist.add(temp);
		}

		//sort double[] of times and magnitudes
		Collections.sort(sortedEQlist, new java.util.Comparator<double[]>() {
			public int compare(double[] a, double[] b) {
				return Double.compare(a[0], b[0]);
			}
		});

		for(int i = 0; i < relativeEventTimes.length; i++){
			double[] temp = sortedEQlist.get(i);
			relativeEventTimes[i] = temp[0];
			magAftershocks[i] = temp[1];
			//					sortedEQlist.add(temp);
		}

		this.magAftershocks = magAftershocks;
		this.relativeTimeAftershocks = relativeEventTimes;
		
//		// DEBUG
//		for(int i = 0; i < num_a; i++){
//			for(int j = 0; j < num_p; j++){
//				for(int k = 0; k < num_c; k++){
//					System.out.format("%4.2f ", this.likeArray[i][j][k]);
//				}
//				System.out.format("\n");
//			}
//			System.out.format("\n\n");
//		}
//		
		
//		
		// generate forecast object
		computeNewForecast(dataStartTimeDays, dataEndTimeDays, forecastMinDays, forecastMaxDays, nSims);
		
	}		

	public void computeNewForecast(double dataMinDays, double dataMaxDays, double forecastMinDays, double forecastMaxDays, int nSims){
		System.out.println("Data/Forecast duration: " + dataMinDays +" "+ dataMaxDays +" "+ forecastMinDays +" "+ forecastMaxDays +" "+ nSims);
		System.out.println("Params: "+ mean_a +" "+ getMaxLikelihood_a() +" "+ getMaxLikelihood_p() +" "+ getMaxLikelihood_c() +" "+ alpha +" "+ b +" "+ magComplete);
		
		ETAScatalog simulatedCatalog = new ETAScatalog(a_vec, p_vec, c_vec, likelihood, alpha, b, refMag, 
				mainShock, aftershockList, dataMinDays, dataMaxDays, forecastMinDays, forecastMaxDays, magComplete, maxMag, maxGenerations, nSims); //maxMag = 9.5, maxGeneratons = 100;
		
		this.forecastMinDays = forecastMinDays;
		this.forecastMaxDays = forecastMaxDays;
		this.simulatedCatalog = simulatedCatalog;
		this.nSims = nSims;
	}

	// TIME-DEPENDENT MC NOT YET IMPLEMENTED, FOLLOWING CODE IS FOR POTENTIAL SCAVENGING LATER
//	private void computeSequenceSpecificParams() {
////		SimpsonIntegrator integrator = new SimpsonIntegrator();
//		likeArray = new double[num_a][num_p][num_c];
//		double maxVal= Double.NEGATIVE_INFINITY;
//		double ln10 = Math.log(10);
//		for(int cIndex=0;cIndex<num_c;cIndex++) {
//			c = get_c(cIndex);
//
//			// make the list of event times and Mc at those times for the given c
//			double sum1=0;
//			double sum2=0;
//			int numEvents=0;
//			for(ObsEqkRupture rup:aftershockList) {
//				double timeSinceMainDays = (double)(rup.getOriginTime()-mainShock.getOriginTime()) / (double)AftershockStatsCalc.MILLISEC_PER_DAY;
//				if(timeSinceMainDays<dataStartTimeDays || timeSinceMainDays>dataEndTimeDays) // not necessary if list already filtered
//					continue;
//				double magCompleteAtTime = getMagCompleteAtTime(timeSinceMainDays);
////				System.out.println("magCompleteAtTime"+magCompleteAtTime);
//
//				if(rup.getMag()>=magCompleteAtTime) {
//					numEvents += 1;
//					sum1 += magMain-magCompleteAtTime;
//					sum2 += Math.log(timeSinceMainDays+c);
//				}
//			}
//
//			// now loop over p and a
//			for(int pIndex=0;pIndex<num_p;pIndex++) {
//				p = get_p(pIndex);
//				for(int aIndex=0;aIndex<num_a;aIndex++) {
//					a = get_a(aIndex);
//					double integral = ETAS_StatsCalc.adaptiveQuadratureIntegration(this, dataStartTimeDays, dataEndTimeDays);
////					double integral = integrator.integrate(100000, this, dataStartTimeDays, dataEndTimeDays);
////double term1=numEvents*a*ln10 + b*ln10*sum1 - p*sum2;
////System.out.println("term1="+term1);
////System.out.println("integral="+integral);
//
//
//					double logLike = numEvents*a*ln10 + b*ln10*sum1 - p*sum2 - integral;
////  System.out.println((float)a+"\t"+(float)p+"\t"+(float)c+"\t"+logLike);
//
//					likeArray[aIndex][pIndex][cIndex] = logLike;
//					if(maxVal<logLike) {
//						maxVal=logLike;
//						max_a_index=aIndex;
//						max_p_index=pIndex;
//						max_c_index=cIndex;
//					}
//				}
//			}
//		}
//		
//		// convert array from log-likelihood to likelihood
//		testTotalLikelihood = convertLogLikelihoodArrayToLikelihood(maxVal);
//		
//		

//	}
	
	
	/**
	 * Get likelihood matrix with no time dependent Mc. Checks for supercriticality and gives a warning if too many supercritical parameter sets are found;
	 */
	private void getLikelihoodMatrixGrid() {
		double[] relativeEventTimes = AftershockStatsCalc.getDaysSinceMainShockArray(mainShock, aftershockList.getRupsAboveMag(magComplete));
		double[] magAftershocks = getAftershockMags(aftershockList.getRupsAboveMag(magComplete));
				
		List<double[]> sortedEQlist = new ArrayList<double[]>();
		
		for(int i = 0; i < relativeEventTimes.length; i++){
			double[] temp = new double[]{relativeEventTimes[i], magAftershocks[i]};
			sortedEQlist.add(temp);
		}
		
		//sort times and magnitudes
		Collections.sort(sortedEQlist, new java.util.Comparator<double[]>() {
		    public int compare(double[] a, double[] b) {
		        return Double.compare(a[0], b[0]);
		    }
		});

		for(int i = 0; i < relativeEventTimes.length; i++){
			double[] temp = sortedEQlist.get(i);
			relativeEventTimes[i] = temp[0];
			magAftershocks[i] = temp[1];
			sortedEQlist.add(temp);
		}

		// instantiate two likelihood matrices -- one to hold subcritical likelihoods, one to hold supercritical likelihoods (for bookkeeping).
		likelihood = new double[num_a][num_p][num_c];
		double[][][] superCriticalLikelihood = new double[num_a][num_p][num_c];
		double[][][] subCriticalLikelihood = new double[num_a][num_p][num_c];
		
		double maxVal= Double.NEGATIVE_INFINITY;
		long startTime = System.currentTimeMillis();
		double warnTime = 3;
		boolean longRunFlag = false;
		
		double logLike;
		boolean subCritFlag;
		
//		System.out.println(mainShock.getMag());
		for(int aIndex=0;aIndex<num_a;aIndex++) {
			if(!longRunFlag){
				double toc = (System.currentTimeMillis() - startTime) / 1000;
				if(toc > warnTime){
					longRunFlag = true;
					double timeEstimate = (double)toc/(aIndex+1) * num_a;
					System.out.format("This might take a while. Probably about %d seconds...\n", (int) timeEstimate);
				}
			}
			
			double a = a_vec[aIndex];
			
			//double k = AftershockStatsCalc.convertProductivityTo_k(a, b, mainShock.getMag(), refMag);
			double k = Math.pow(10, a + alpha*(mainShock.getMag()-magComplete) );
			
			for(int pIndex=0;pIndex<num_p;pIndex++) {
				double p = p_vec[pIndex];
				
				for(int cIndex=0;cIndex<num_c;cIndex++) {
					
					
					double c = c_vec[cIndex];

					//check for supercritical parameters over the forecast time window
					double n;	//branching ratio
					if( p == 1)
						n = b * Math.log(10) * (maxMag - magComplete) * Math.pow(10, a) * ( Math.log(forecastMaxDays + c) - Math.log(c) );
					else
						n = b * Math.log(10) * (maxMag - magComplete) * Math.pow(10, a)/(1-p) * ( Math.pow(forecastMaxDays + c, 1-p) - Math.pow(c, 1-p) );
					subCritFlag = ( n<1 );
//					System.out.println(n);	//debug

					logLike = getLogLikelihoodForETASParams( k,  a,  p,  c,  alpha,  magComplete,
							dataStartTimeDays,  dataEndTimeDays, magAftershocks, relativeEventTimes);
					
					if(Doubles.isFinite(logLike)){
						if (subCritFlag){
							likelihood[aIndex][pIndex][cIndex] = logLike;
							subCriticalLikelihood[aIndex][pIndex][cIndex] = logLike;
							superCriticalLikelihood[aIndex][pIndex][cIndex] = Double.NEGATIVE_INFINITY;

							if(maxVal<logLike ) {
								maxVal=logLike;
								max_a_index=aIndex;
								max_p_index=pIndex;
								max_c_index=cIndex;
							}
						} else {
							likelihood[aIndex][pIndex][cIndex] = Double.NEGATIVE_INFINITY;
							subCriticalLikelihood[aIndex][pIndex][cIndex] = Double.NEGATIVE_INFINITY;
							superCriticalLikelihood[aIndex][pIndex][cIndex] = logLike;
						}
					}else{
						likelihood[aIndex][pIndex][cIndex] = Double.NEGATIVE_INFINITY;
						subCriticalLikelihood[aIndex][pIndex][cIndex] = Double.NEGATIVE_INFINITY;
						superCriticalLikelihood[aIndex][pIndex][cIndex] = Double.NEGATIVE_INFINITY;
					}
				}
			}
		}
		

		// convert array from log-likelihood to likelihood
		testTotalLikelihood = convertLogLikelihoodArrayToLikelihood(maxVal);
		System.out.println("Total likelihood  = " + testTotalLikelihood); //debug
		
		//measure the proportion of supercritical combinations
		double totalSubCriticalLikelihood = convertLogLikelihoodArrayToLikelihood_nonNormalized(subCriticalLikelihood, maxVal);
		double totalSuperCriticalLikelihood = convertLogLikelihoodArrayToLikelihood_nonNormalized(superCriticalLikelihood, maxVal);
		double fractionSubCritical = totalSubCriticalLikelihood/(totalSubCriticalLikelihood + totalSuperCriticalLikelihood);
		
		double toc = (System.currentTimeMillis() - startTime) / 1000;
		System.out.format("Grid search took %d seconds.\n", (int)toc);
		System.out.format("%3.2f percent of the solution space is subcritical.\n", fractionSubCritical*100);
	}

	/*
	 * To make this run fast, give it sorted vectors of magnitudes and times. Actually, it will not work otherwise.
	 * 
	 * todo: check for sorted vectors and sort if not. 
	 */
	public double getLogLikelihoodForETASParams(double k, double a, double p, double c, double alpha, double Mc,
			double tMinDays, double tMaxDays, double[] aftershockMagnitudes, double[] relativeEventTimes) {
		
		double LL;
		double timeIntegral = Double.NaN;
		int Nas = relativeEventTimes.length;	//the number of aftershocks, not counting the mainshock
				
		//compute total number at end of fit window
		double Ntot;
		if (p == 1)
			timeIntegral = Math.log(tMaxDays + c) - Math.log(tMinDays + c);
		else
			timeIntegral = (Math.pow(tMaxDays + c, 1-p) - Math.pow(tMinDays + c, 1-p)) / (1-p);
		Ntot = k*timeIntegral;		//mainshock contribution
//		System.out.println("mainshock: " + Ntot + " " + Nas +" "+ tMinDays + " " + tMaxDays + " " + Mref); //debug
	
		double[] productivity = new double[Nas];
		double[] lambda = new double[Nas];
		for(int i=0; i<Nas; i++){
			//compute productivity for this aftershock
			productivity[i] = Math.pow(10, a + alpha*(aftershockMagnitudes[i] - Mc));	//productivity of this aftershock
			
			//compute intensity at this moment due to previous earthquakes
			lambda[i] = k/Math.pow(relativeEventTimes[i] + c, p); //from the mainshock
			for(int j = 0; j < i; j++){
				if(relativeEventTimes[j] < relativeEventTimes[i])
					lambda[i] += productivity[j]/Math.pow(relativeEventTimes[i] - relativeEventTimes[j] + c, p);	//from the aftershocks
				
			}
	
			//compute number at end of window due to this aftershock
			if(relativeEventTimes[i] < tMaxDays){
				if(p == 1)
					timeIntegral = Math.log(tMaxDays - relativeEventTimes[i] + c) - Math.log(c); 
				else
					timeIntegral = (Math.pow(tMaxDays - relativeEventTimes[i] + c, 1-p) - Math.pow(c, 1-p)) / (1-p);

				Ntot += productivity[i]*timeIntegral;	//aftershock Contributions
			}
			//System.out.format(" %d", (int) Ntot); 
		}
			
		//compute likelihood (sum of the log-lambdas at times of all aftershocks minus the total number expected)
		LL = -Ntot;
		for(int i = 0; i<Nas; i++)
			LL += Math.log(lambda[i]);
		
		//debug mode
//		System.out.println("for k="+k+", a="+a+", p="+p+", c="+c+", Nsim="+ Ntot +", Nas="+Nas+", LL="+ LL);
		
		return LL;
	}
	
	
//	public double getMagCompleteAtTime(double timeSinceMainDays) {
//		if(timeSinceMainDays==0d)
//			return 10d;	// avoid infinity
//		double magCompleteAtTime = magMain/2.0 - capG - capH*Math.log10(timeSinceMainDays);
//		if(magCompleteAtTime>magComplete)
//			return magCompleteAtTime;
//		else 
//			return magComplete;
//
//	}

//	public double getRateAboveMagCompleteAtTime(double timeSinceMainDays) {
//		if(timeSinceMainDays==0d)
//			return 0d;
//		return Math.pow(10d,a+b*(magMain-getMagCompleteAtTime(timeSinceMainDays)))*Math.pow(timeSinceMainDays+c, -p);
//	}

//	public double value(double timeSinceMainDays) {
//		return getRateAboveMagCompleteAtTime(timeSinceMainDays);
//	}
//	

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}



@Override
public double value(double arg0) {
	// TODO Auto-generated method stub
	return 0;
}

}
