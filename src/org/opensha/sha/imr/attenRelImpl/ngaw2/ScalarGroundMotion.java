package org.opensha.sha.imr.attenRelImpl.ngaw2;

/**
 * Wrapper class for ground motion prediction equation (GMPE) results.
 * 
 * @author Peter Powers
 * @version $Id:$
 */
public interface ScalarGroundMotion {

	/**
	 * Returns the median peak ground motion.
	 * @return the mean
	 */
	public double mean();
	
	/**
	 * Returns the standard deviation for a hazard calculation.
	 * @return the standard deviation
	 */
	public double stdDev();
	
}
