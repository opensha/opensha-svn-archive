package org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_3;

import org.opensha.data.Location;

/**
 * 
 * @author field
 */
public class EmpiricalModel {

	/**
	 * This returns the ratio of short term rate to long term rate
	 * 
	 * @param loc
	 * @return
	 */
	 public  double getCorrection(Location loc) {
		 return 1;
	 }
}
