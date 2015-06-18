package scratch.aftershockStatistics;

import org.opensha.commons.data.function.HistogramFunction;
import org.opensha.commons.data.xyz.EvenlyDiscrXYZ_DataSet;
import org.opensha.sha.earthquake.observedEarthquake.ObsEqkRupList;
import org.opensha.sha.earthquake.observedEarthquake.ObsEqkRupture;
import org.opensha.sha.magdist.GutenbergRichterMagFreqDist;


/**
 * This represents a Reasenberg-Jones (1989, 1994) aftershock model.
 * 
 * TODO Make a constructor that takes generic values, perhaps with a PDF for a-values
 *
 * @author field
 *
 */
public class ReasenbergJonesAftershockModel {
	
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
	public ReasenbergJonesAftershockModel(	ObsEqkRupture mainShock, ObsEqkRupList aftershockList, double magComplete, double b, 
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
		}
		
		array = new double[num_a][num_p][num_c];
		double total = 0;
		double maxVal= Double.NEGATIVE_INFINITY;
		for(int aIndex=0;aIndex<num_a;aIndex++) {
			double a = get_a(aIndex);
			double k = AftershockStatsCalc.convertProductivityTo_k(a, b, magMain, magComplete);
			for(int pIndex=0;pIndex<num_p;pIndex++) {
				double p = get_p(pIndex);
				for(int cIndex=0;cIndex<num_c;cIndex++) {
					double c = get_c(cIndex);
					double logLike = AftershockStatsCalc.getLogLikelihoodForOmoriParams(k, p, c, relativeEventTimes);
					double like = Math.exp(logLike);
// System.out.println(get_k(aIndex)+"\t"+get_p(pIndex)+"\t"+get_c(cIndex)+"\t"+logLike+"\t"+like);
					array[aIndex][pIndex][cIndex] = like;
					total += like;
					if(maxVal<like) {
						maxVal=like;
						max_a_index=aIndex;
						max_p_index=pIndex;
						max_c_index=cIndex;
					}
				}
			}
		}
		
		// normalize
		double testTotal=0;
		for(int aIndex=0;aIndex<num_a;aIndex++) {
			for(int pIndex=0;pIndex<num_p;pIndex++) {
				for(int cIndex=0;cIndex<num_c;cIndex++) {
					array[aIndex][pIndex][cIndex] /= total;
					testTotal += array[aIndex][pIndex][cIndex];
				}
			}
		}

		if (D) {
			System.out.println("testTotal="+testTotal);
			System.out.println("getMaxLikelihood_a()="+getMaxLikelihood_a());
			System.out.println("getMaxLikelihood_p()="+getMaxLikelihood_p());
			System.out.println("getMaxLikelihood_c()="+getMaxLikelihood_c());
		}
		
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
	
	private double get_a(int aIndex) { return min_a+aIndex*delta_a;}
	
	private double get_p(int pIndex) { return min_p+pIndex*delta_p;}
	
	private double get_c(int cIndex) { return min_c+cIndex*delta_c;}
	
	
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
	 * This returns the expected number of events as a function of magnitude (incremental distribution) assuming 
	 * a GR with the b-value here and the given min and max magnitude and the given time span.
	 * @param minMag
	 * @param maxMag
	 * @param numMag
	 * @param tMinDays
	 * @param tMaxDays
	 * @return
	 */
	public GutenbergRichterMagFreqDist getExpectedNumMFD(double minMag, double maxMag, int numMag, double tMinDays, double tMaxDays) {
		double totExpNum = getExpectedNumEvents(minMag, tMinDays, tMaxDays);
		GutenbergRichterMagFreqDist mfd = new GutenbergRichterMagFreqDist(b, totExpNum, minMag, maxMag, numMag);
		mfd.setName("Expected Num Incr. MFD");
		mfd.setInfo("Total Expected Num = "+totExpNum);
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
