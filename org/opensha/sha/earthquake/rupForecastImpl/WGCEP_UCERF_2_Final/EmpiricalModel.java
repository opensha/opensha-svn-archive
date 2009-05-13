package org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final;

import java.util.Iterator;

import org.opensha.data.Location;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.data.EmpiricalModelDataFetcher;
import org.opensha.sha.faultSurface.GriddedSurfaceAPI;

/**
 * 
 * @author field
 */
public class EmpiricalModel  implements java.io.Serializable {
	private EmpiricalModelDataFetcher empiricalModelDataFetcher = new EmpiricalModelDataFetcher();
	
	/**
	 * Get the ratio of short term rate to long term rate.
	 * It iterates over all points of the rupture trace and averages the correction
	 * over all locations
	 * 
	 * @param surface
	 * @return
	 */
	public double getCorrection(GriddedSurfaceAPI surface) {
		Iterator locIt = surface.getColumnIterator(0);
		double totCorr = 0;
		while(locIt.hasNext()) 
			totCorr+=getCorrection((Location)locIt.next());
		return totCorr/surface.getNumCols();
 	}
	
	/**
	 * This returns the ratio of short term rate to long term rate
	 * 
	 * @param loc
	 * @return
	 */
	 private  double getCorrection(Location loc) {
		 int numPolygons = empiricalModelDataFetcher.getNumRegions();
		 for(int i=0; i<(numPolygons-1); ++i) { //loop over all empirical regions except rest of california
			 if(empiricalModelDataFetcher.getRegion(i).isLocationInside(loc))
				 return empiricalModelDataFetcher.getRate(i);
		 }
		 // return for rest of California
		 return empiricalModelDataFetcher.getRate(numPolygons-1);
	 }
}
