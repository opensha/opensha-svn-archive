package scratch.aftershockStatisticsETAS;

public class GenericETAS_Parameters implements java.io.Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 578249832338308677L;
	
	/**
	 * 
	 */
	
	double aValue_mean;
	double aValue_sigma;	// magnitude independent sigma
	double bValue;
	double pValue;
	double pValue_sigma;
	double cValue;
	double log_cValue_sigma;
	double alpha;
	double refMag;
	
	/**
	 * This class is a container for the Generic ETAS parameters defined by van der Elst and Page (in prep).
	 * In development -- The distribution of a-values is assumed
	 * to be Gaussian, with a mean of aValue_mean and a standard deviation of aValue_sigma.  
	 * For now, alpha (the magnitude scaling exponent) is set to 1.0. Model is limited to k,c, and p. 
	 * 
	 * @param aValue_mean
	 * @param aValue_sigma
	 * @param bValue
	 * @param alpha
	 * @param pValue
	 * @param cValue
	 * @param refMag
	 */
	public GenericETAS_Parameters() {
		//if called without arguments, initialize with global average parameters
		this.aValue_mean = -2.43;
		this.aValue_sigma = 0.4;	// magnitude independent sigma
		this.bValue = 1;
		this.pValue = 0.96;
		this.pValue_sigma = 0.13;
		this.cValue = Math.pow(10, -2.64);
		this.log_cValue_sigma = 0.75;
		this.alpha = 1;
		this.refMag = 4.5;
	}
	
	public GenericETAS_Parameters(double aValue_mean, double aValue_sigma,  double pValue, double pValue_sigma,
			double cValue, double log_cValue_sigma, double alpha, double bValue, double refMag) {
		this.aValue_mean = aValue_mean;
		this.aValue_sigma = aValue_sigma;	// magnitude independent sigma
		this.bValue = bValue;
		this.pValue = pValue;
		this.pValue_sigma = pValue_sigma;
		this.cValue = cValue;
		this.log_cValue_sigma = log_cValue_sigma;
		this.alpha = alpha;
		this.refMag = refMag;
	}
	
	/**
	 * This returns the mean a-value (aValue_mean).
	 * @return
	 */
	public double get_aValueMean() {return aValue_mean;}
	
	/**
	 * This returns the magnitude-independent a-value standard deviation (aValue_sigma).
	 * @return
	 */
	public double get_aValueSigma() {return aValue_sigma;}
	
	/**
	 * This returns the b-value.
	 * @return
	 */
	public double get_bValue() {return bValue;}

	/**
	 * This returns the p-value.
	 * @return
	 */
	public double get_pValue() {return pValue;}

	public double get_pValueSigma() {return pValue_sigma;}

	/**
	 * This returns the b-value.
	 * @return
	 */
	public double get_cValue() {return cValue;}

	public double get_log_cValueSigma() {return log_cValue_sigma;}
	
	public double get_alpha() {return alpha;}
	
	public double get_refMag() {return refMag;}

	@Override
	public String toString() {
		return "ETAS_Params[a="+get_aValueMean()+", aSigma="+get_aValueSigma()+","
				+ " b="+get_bValue()+", p="+get_pValue()+", c="+get_cValue()+
				", alpha="+get_alpha()+", refMag="+get_refMag()+"]";
	}
	
}
