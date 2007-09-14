package org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_3;

import org.opensha.data.Location;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_3.data.EmpiricalModelDataFetcher;

/**
 * 
 * @author field
 */
public class EmpiricalModel {
	private EmpiricalModelDataFetcher empiricalModelDataFetcher = new EmpiricalModelDataFetcher();
	/**
	 * This returns the ratio of short term rate to long term rate
	 * 
	 * @param loc
	 * @return
	 */
	 public  double getCorrection(Location loc) {
		 int numPolygons = empiricalModelDataFetcher.getNumRegions();
		 for(int i=0; i<(numPolygons-1); ++i) { //loop over all empirical regions except rest of california
			 if(empiricalModelDataFetcher.getRegion(i).isLocationInside(loc))
				 return empiricalModelDataFetcher.getRate(i);
		 }
		 // return for rest of California
		 return empiricalModelDataFetcher.getRate(numPolygons-1);
	 }
}
