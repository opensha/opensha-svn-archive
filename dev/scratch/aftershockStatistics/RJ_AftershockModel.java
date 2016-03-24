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
 * This represents a Reasenberg-Jones (1989, 1994) aftershock model.
 * 
 * Subclasses should normalize Likelihood values so they sum to 1.0 over the range of parameter-values specified;
 * this can be accomplished using the convertArrayToLikelihood(double maxLogLikeVal) method.
 * 
 *
 * @author field
 *
 */
public abstract class RJ_AftershockModel {
	
	Boolean D=true;	// debug flag
	
	double b, magMain, magComplete;
	double min_a, max_a, delta_a=0, min_p, max_p, delta_p=0, min_c, max_c, delta_c=0;
	int num_a, num_p, num_c;
	double[] relativeEventTimes;
	double[][][]  array;
	int max_a_index=-1;
	int max_p_index=-1;
	int max_c_index=-1;

	
	/**
	 * This provides the rate at timeDays after the main shock, which is generally used for numerical integration
	 * for the log-likelihood in subclasses.
	 * @param timeDays
	 * @return
	 */
	abstract double getRateAboveMagCompleteAtTime(double timeDays);
	

	
	/**
	 * This converts the array from log-likelihood to likelihood values, making sure NaNs do not occur 
	 * due to high logLikelihoods, and re-normalizes the array so as values sum to 1.0.
	 * @param maxLogLikeVal - the maximum values in the input array
	 * @return
	 */
	protected double convertArrayToLikelihood(double maxLogLikeVal) {
		double total=0;
		double corr = 0;
		if(maxLogLikeVal>100.0)	// values above ~700 cause NaNs from Math.exp(), so we subtract some number from all values to avoid such numerical problems
			corr = maxLogLikeVal-100;
		for(int aIndex=0;aIndex<num_a;aIndex++) {
			for(int pIndex=0;pIndex<num_p;pIndex++) {
				for(int cIndex=0;cIndex<num_c;cIndex++) {
					double like = Math.exp(array[aIndex][pIndex][cIndex]-corr);
					array[aIndex][pIndex][cIndex] = like;
					total += array[aIndex][pIndex][cIndex];
				}
			}
		}

		// now re-normalize all likelihood values so they sum to 1.0
		double testTotal=0;
		for(int aIndex=0;aIndex<num_a;aIndex++) {
			for(int pIndex=0;pIndex<num_p;pIndex++) {
				for(int cIndex=0;cIndex<num_c;cIndex++) {
					array[aIndex][pIndex][cIndex] /= total;
					testTotal += array[aIndex][pIndex][cIndex];
				}
			}
		}
		return testTotal;
	}
	
	public double getMaxLikelihood_a() { return get_a(max_a_index);}
	
	public double getMaxLikelihood_p() { return get_p(max_p_index);}
	
	public double getMaxLikelihood_c() { return get_c(max_c_index);}
	
	/**
	 * This gives the k value (for associated b, magMain, and magComplete )
	 * @return
	 */
	public double getMaxLikelihood_k() { 
		return AftershockStatsCalc.convertProductivityTo_k(get_a(max_a_index), b, magMain, magComplete);
		}
	
	protected double get_a(int aIndex) { return min_a+aIndex*delta_a;}
	
	protected double get_p(int pIndex) { return min_p+pIndex*delta_p;}
	
	protected double get_c(int cIndex) { return min_c+cIndex*delta_c;}
	
	
	/**
	 * This gives the expected number of aftershocks, according to maximum likelihood values, above the 
	 * specified minimum magnitude over the specified time span.
	 * @param magMin
	 * @param tMinDays
	 * @param tMaxDays
	 * @return
	 */
	public double getExpectedNumEvents(double magMin, double tMinDays, double tMaxDays) {
		return AftershockStatsCalc.getExpectedNumEvents(getMaxLikelihood_a(), b, magMain, magMin, getMaxLikelihood_p(), getMaxLikelihood_c(), tMinDays, tMaxDays);
	}
	
	
	/**
	 * This gives the expected number of aftershocks with time according to maximum likelihood values, and above the 
	 * specified minimum magnitude over the specified time span.
	 * @param magMin
	 * @param tMinDays - left edge of first time intercal
	 * @param tMaxDays - right edge of last time interval
	 * @param tDelta
	 * @return
	 */
	public EvenlyDiscretizedFunc getExpectedCumNumEventsWithTime(double magMin, double tMinDays, double tMaxDays, double tDelta) {
		return AftershockStatsCalc.getExpectedCumulativeNumWithTimeFunc(getMaxLikelihood_a(), b, magMain, magMin, 
				getMaxLikelihood_p(), getMaxLikelihood_c(), tMinDays, tMaxDays, tDelta);
	}

	
	/**
	 * This returns an MFD with the expected number of events greater than or equal to each magnitude 
	 * for the current model and the given time span.
	 * @param minMag - the minimum magnitude considered
	 * @param maxMag - the maximum magnitude considered
	 * @param numMag - number of mags in the MFD
	 * @param tMinDays
	 * @param tMaxDays
	 * @return
	 */
	public EvenlyDiscretizedFunc getExpectedCumNumMFD(double minMag, double maxMag, int numMag, double tMinDays, double tMaxDays) {
		EvenlyDiscretizedFunc mfd = new EvenlyDiscretizedFunc(minMag, maxMag, numMag);
		for(int i=0;i<mfd.size();i++) {
			mfd.set(i, getExpectedNumEvents(mfd.getX(i), tMinDays, tMaxDays));
		}
		mfd.setName("Expected Num Events");
		mfd.setInfo("Cumulative distribution (greater than or equal to each magnitude)");
//		double totExpNum = getExpectedNumEvents(minMag, tMinDays, tMaxDays);
//		GutenbergRichterMagFreqDist mfd = new GutenbergRichterMagFreqDist(b, totExpNum, minMag, maxMag, numMag);
//		mfd.setName("Expected Num Incr. MFD");
//		mfd.setInfo("Total Expected Num = "+totExpNum);
		return mfd;
	}
	
	/**
	 * This returns the PDF of a, which is a marginal distribution if either c or p 
	 * are unconstrained (either num_p or num_c not equal to 1). Null is returned if
	 * a is constrained (num_a=1).
	 * @return
	 */
	public HistogramFunction getPDF_a() {
		if(num_a == 1) {
			return null;
		}
		else {
			HistogramFunction hist = new HistogramFunction(min_a, num_a, delta_a);
			for(int aIndex=0;aIndex<num_a;aIndex++) {
				for(int pIndex=0;pIndex<num_p;pIndex++) {
					for(int cIndex=0;cIndex<num_c;cIndex++) {
						hist.add(get_a(aIndex), array[aIndex][pIndex][cIndex]);
					}
				}
			}
			String name = "PDF of a-value";
			if(num_p !=1 || num_c != 1)
				name += " (marginal)";
			hist.setName(name);
			if(D) {
//				System.out.println("PDF of a-value:  "+hist);
				System.out.println("PDF of a-value: totalTest = "+hist.calcSumOfY_Vals());
			}
			return hist;
		}
	}
	
	/**
	 * This returns the PDF of p, which is a marginal distribution if either a or c 
	 * are unconstrained (either num_a or num_c not equal to 1). Null is returned if
	 * p is constrained (num_p=1).
	 * @return
	 */
	public HistogramFunction getPDF_p() {
		if(num_p == 1) {
			return null;
		}
		else {
			HistogramFunction hist = new HistogramFunction(min_p, num_p, delta_p);
			for(int aIndex=0;aIndex<num_a;aIndex++) {
				for(int pIndex=0;pIndex<num_p;pIndex++) {
					for(int cIndex=0;cIndex<num_c;cIndex++) {
						hist.add(get_p(pIndex), array[aIndex][pIndex][cIndex]);
					}
				}
			}
			String name = "PDF of p-value";
			if(num_a !=1 || num_c != 1)
				name += " (marginal)";
			hist.setName(name);
			if(D) {
				System.out.println("PDF of p-value: totalTest = "+hist.calcSumOfY_Vals());
			}
			return hist;
		}
	}
	
	/**
	 * This returns the PDF of c, which is a marginal distribution if either a or p 
	 * are unconstrained (either num_a or num_p not equal to 1). Null is returned if
	 * c is constrained (num_c=1).
	 * @return
	 */
	public HistogramFunction getPDF_c() {
		if(num_c == 1) {
			return null;
		}
		else {
			HistogramFunction hist = new HistogramFunction(min_c, num_c, delta_c);
			for(int aIndex=0;aIndex<num_a;aIndex++) {
				for(int pIndex=0;pIndex<num_p;pIndex++) {
					for(int cIndex=0;cIndex<num_c;cIndex++) {
						hist.add(get_c(cIndex), array[aIndex][pIndex][cIndex]);
					}
				}
			}
			String name = "PDF of c-value";
			if(num_a !=1 || num_p != 1)
				name += " (marginal)";
			hist.setName(name);
			if(D) {
				System.out.println("PDF of c-value: totalTest = "+hist.calcSumOfY_Vals());
			}
			return hist;
		}
	}

	
	/**
	 * This returns a 2D PDF for a and p, which is a marginal distribution if c 
	 * is unconstrained (num_c not equal to 1). Null is returned if either
	 * a or p are constrained (num_a=1 or num_p=1).
	 * @return
	 */
	public EvenlyDiscrXYZ_DataSet get2D_PDF_for_a_and_p() {
		if(num_a == 1 || num_p == 1) {
			return null;
		}
		else {
			EvenlyDiscrXYZ_DataSet hist2D = new EvenlyDiscrXYZ_DataSet(num_a, num_p, min_a, min_p, delta_a, delta_p);
			for(int aIndex=0;aIndex<num_a;aIndex++) {
				for(int pIndex=0;pIndex<num_p;pIndex++) {
					for(int cIndex=0;cIndex<num_c;cIndex++) {
						double prevVal = hist2D.get(aIndex,pIndex);
						hist2D.set(aIndex,pIndex, prevVal+array[aIndex][pIndex][cIndex]);
					}
				}
			}
//			String name = "2D PDF of a vs p";
//			if(num_c != 1)
//				name += " (marginal)";
			if(D) {
				System.out.println("2D PDF of a vs p: totalTest = "+hist2D.getSumZ());
			}
			return hist2D;
		}
	}

	
	
	/**
	 * This returns a 2D PDF for a and c, which is a marginal distribution if p 
	 * is unconstrained (num_p not equal to 1). Null is returned if either
	 * a or c are constrained (num_a=1 or num_c=1).
	 * @return
	 */
	public EvenlyDiscrXYZ_DataSet get2D_PDF_for_a_and_c() {
		if(num_a == 1 || num_c == 1) {
			return null;
		}
		else {
			EvenlyDiscrXYZ_DataSet hist2D = new EvenlyDiscrXYZ_DataSet(num_a, num_c, min_a, min_c, delta_a, delta_c);
			for(int aIndex=0;aIndex<num_a;aIndex++) {
				for(int pIndex=0;pIndex<num_p;pIndex++) {
					for(int cIndex=0;cIndex<num_c;cIndex++) {
						double prevVal = hist2D.get(aIndex,cIndex);
						hist2D.set(aIndex,cIndex, prevVal+array[aIndex][pIndex][cIndex]);
					}
				}
			}
//			String name = "2D PDF of a vs c";
//			if(num_p != 1)
//				name += " (marginal)";
			if(D) {
				System.out.println("2D PDF of a vs c: totalTest = "+hist2D.getSumZ());
			}
			return hist2D;
		}
	}

	/**
	 * This returns a 2D PDF for c and p, which is a marginal distribution if a 
	 * is unconstrained (num_a not equal to 1). Null is returned if either
	 * c or p are constrained (num_c=1 or num_p=1).
	 * @return
	 */
	public EvenlyDiscrXYZ_DataSet get2D_PDF_for_c_and_p() {
		if(num_c == 1 || num_p == 1) {
			return null;
		}
		else {
			EvenlyDiscrXYZ_DataSet hist2D = new EvenlyDiscrXYZ_DataSet(num_c, num_p, min_c, min_p, delta_c, delta_p);
			for(int aIndex=0;aIndex<num_a;aIndex++) {
				for(int pIndex=0;pIndex<num_p;pIndex++) {
					for(int cIndex=0;cIndex<num_c;cIndex++) {
						double prevVal = hist2D.get(cIndex,pIndex);
						hist2D.set(cIndex,pIndex, prevVal+array[aIndex][pIndex][cIndex]);
					}
				}
			}
//			String name = "2D PDF of c vs p";
//			if(num_a != 1)
//				name += " (marginal)";
			if(D) {
				System.out.println("2D PDF of c vs p: totalTest = "+hist2D.getSumZ());
			}
			return hist2D;
		}
	}

	// getters/setters commented out until needed:
//	public double getMin_a() { return min_a;}
//	public double getMin_p() { return min_p;}
//	public double getMin_c() { return min_c;}
//	public double getMax_a() { return max_a;}
//	public double getMax_p() { return max_p;}
//	public double getMax_c() { return max_c;}
//	public double getDelta_a() { return delta_a;}
//	public double getDelta_p() { return delta_p;}
//	public double getDelta_c() { return delta_c;}
//	public int getNum_a() { return num_a;}
//	public int getNum_p() { return num_p;}
//	public int getNum_c() { return num_c;}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
