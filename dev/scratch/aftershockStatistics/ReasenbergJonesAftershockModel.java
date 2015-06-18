package scratch.aftershockStatistics;


/**
 * This represents a 3D array of likelihoods (not log-likelihood!) for the range of parameter values given.
 * Values are normalized to sum to 1.0

 * @author field
 *
 */
public class ReasenbergJonesAftershockModel {
	
	Boolean D=true;	// debug flag
	
	double b, magMain, magMin;
	double min_a, max_a, delta_a=0, min_p, max_p, delta_p=0, min_c, max_c, delta_c=0;
	int num_a, num_p, num_c;
	double[] relativeEventTimes;
	double[][][]  array;
	int max_a_index=-1;
	int max_p_index=-1;
	int max_c_index=-1;

	
	public ReasenbergJonesAftershockModel(	double b, double magMain, double magMin,
											double min_a, double max_a, int num_a, 
											double min_p, double max_p, int num_p, 
											double min_c, double max_c, int num_c, 
											double[] relativeEventTimes) {
		this.min_a = min_a;
		this.max_a = max_a;
		this.num_a = num_a;
		this.min_p = min_p;
		this.max_p = max_p;
		this.num_p = num_p;
		this.min_c = min_c;
		this.max_c = max_c;
		this.num_c = num_c;
		this.relativeEventTimes = relativeEventTimes;
		
		if(num_a>1) // otherwise defaults to zero
			delta_a = (max_a-min_a)/((double)num_a - 1.);
		if(num_p>1)
			delta_p = (max_p-min_p)/((double)num_p - 1.);
		if(num_c>1)
			delta_c = (max_c-min_c)/((double)num_c - 1.);
		
		if(D) {
			System.out.println("a\t"+min_a+"\t"+max_a+"\t"+num_a+"\t"+delta_a);
			System.out.println("p\t"+min_p+"\t"+max_p+"\t"+num_p+"\t"+delta_p);
			System.out.println("c\t"+min_c+"\t"+max_c+"\t"+num_c+"\t"+delta_c);
		}
		
		array = new double[num_a][num_p][num_c];
		double total = 0;
		double maxVal= Double.NEGATIVE_INFINITY;
		for(int aIndex=0;aIndex<num_a;aIndex++) {
			double a = get_a(aIndex);
			double k = AftershockStatsCalc.getProductivity_k(a, b, magMain, magMin);
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
	
	public double get_a(int aIndex) { return min_a+aIndex*delta_a;}
	
	public double get_p(int pIndex) { return min_p+pIndex*delta_p;}
	
	public double get_c(int cIndex) { return min_c+cIndex*delta_c;}
	
	public double getMaxLikelihood_a() { return get_a(max_a_index);}
	
	public double getMaxLikelihood_p() { return get_p(max_p_index);}
	
	public double getMaxLikelihood_c() { return get_c(max_c_index);}
	

	public double getMin_a() { return min_a;}
	public double getMin_p() { return min_p;}
	public double getMin_c() { return min_c;}
	public double getMax_a() { return max_a;}
	public double getMax_p() { return max_p;}
	public double getMax_c() { return max_c;}
	public double getDelta_a() { return delta_a;}
	public double getDelta_p() { return delta_p;}
	public double getDelta_c() { return delta_c;}
	public int getNum_a() { return num_a;}
	public int getNum_p() { return num_p;}
	public int getNum_c() { return num_c;}



	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
