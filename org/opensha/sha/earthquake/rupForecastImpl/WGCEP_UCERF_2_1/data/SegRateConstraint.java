/**
 * 
 */
package org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_1.data;


/**
 * This class is used to save the segment Rates for the faults
 * @author vipingupta
 *
 */
public class SegRateConstraint {
	private String faultName; // fault name
	private int segIndex; // segment index
	private double meanSegRate; // mean Segment rate
	private double stdDevToMean; // Std dev to mean
	
	/**
	 * Save the faultName
	 * @param faultName
	 */
	public SegRateConstraint(String faultName) {
		this.faultName = faultName;
	}
	
	/**
	 * Get the fault name
	 * @return
	 */
	public String getFaultName() {
		return this.faultName;
	}
	
	/**
	 * Set the segment rate
	 * 
	 * @param segIndex
	 * @param meanRate
	 * @param stdDevtoMean
	 */
	public void setSegRate(int segIndex, double meanRate, double stdDevtoMean) {
		this.segIndex = segIndex;
		this.meanSegRate = meanRate;
		this.stdDevToMean = stdDevtoMean;
	}
	
	
	/**
	 * Get the segment index
	 * @return
	 */
	public int getSegIndex() {
		return this.segIndex;
	}
	
	/**
	 * Get mean Segment rate
	 * @return
	 */
	public double getMean() {
		return this.meanSegRate;
	}
	
	/**
	 * Get StdDev to mean for the rate
	 * @return
	 */
	public double getStdDevToMean() {
		return this.stdDevToMean;
	}
	
}
