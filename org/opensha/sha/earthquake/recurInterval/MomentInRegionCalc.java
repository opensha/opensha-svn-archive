/**
 * 
 */
package org.opensha.sha.earthquake.recurInterval;

import java.util.Iterator;

import org.opensha.calc.MomentMagCalc;
import org.opensha.data.Location;
import org.opensha.data.LocationList;
import org.opensha.data.region.GeographicRegion;
import org.opensha.sha.earthquake.EqkRupForecast;
import org.opensha.sha.earthquake.ProbEqkRupture;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_2.UCERF2;

/**
 * It calculates scalar moment in a region for a given ERF
 * @author vipingupta
 *
 */
public class MomentInRegionCalc {
	
	
	/**
	 * It calculates moment in Newton-meters in a region given an ERF.
	 * 
	 * @param erf EqkRupForecast to be used for calculating moment 
	 * @param region Polygon in which Moment  needs to be calculated
	 */
	public static double getMoment(EqkRupForecast erf, GeographicRegion region) {
		int numSources = erf.getNumSources();
		double totMoment = 0, rupMoment=0;
		int numRups, totRupLocs, rupLocsInside;
		for(int srcIndex=0; srcIndex<numSources; ++srcIndex) {
			numRups = erf.getNumRuptures(srcIndex);
			for(int rupIndex=0; rupIndex<numRups; ++rupIndex) {
				ProbEqkRupture rupture = erf.getRupture(srcIndex, rupIndex);
				rupMoment = MomentMagCalc.getMoment(rupture.getMag());
				Iterator it = rupture.getRuptureSurface().getLocationsIterator();
				totRupLocs=0;
				rupLocsInside=0;
				// find the fraction of rupture within the polygon
				while(it.hasNext()) {
					Location loc = (Location)it.next();
					++totRupLocs;
					if(region.isLocationInside(loc)) ++rupLocsInside;
				}
				totMoment = totMoment + rupMoment*((double)rupLocsInside)/totRupLocs;
			}
		}
		return totMoment;
	}

	
	
	public static void main(String args[]) {
		// all six regions for which moment needs to be calculated
		
		//REGION 1
		LocationList locList1 = new LocationList();
		locList1.addLocation(new Location(40.5, -127));
		locList1.addLocation(new Location(45, -122));
		locList1.addLocation(new Location(41.75, -119.025));
		locList1.addLocation(new Location(37.25, -124.025));
		GeographicRegion region1 = new GeographicRegion(locList1);
		
//		REGION 2
		LocationList locList2 = new LocationList();
		locList2.addLocation(new Location(37.25, -124.025));
		locList2.addLocation(new Location(41.75, -119.025));
		locList2.addLocation(new Location(41, -118.35));
		locList2.addLocation(new Location(36.5, -123.35));
		GeographicRegion region2 = new GeographicRegion(locList2);

//		REGION 3
		LocationList locList3 = new LocationList();
		locList3.addLocation(new Location(36.5, -123.35));
		locList3.addLocation(new Location(41, -118.35));
		locList3.addLocation(new Location(39.5, -117));
		locList3.addLocation(new Location(35, -122));
		GeographicRegion region3 = new GeographicRegion(locList3);

//		REGION 4
		LocationList locList4 = new LocationList();
		locList4.addLocation(new Location(35, -122));
		locList4.addLocation(new Location(39.5, -117));
		locList4.addLocation(new Location(37.5, -115.2));
		locList4.addLocation(new Location(33, -120.2));
		GeographicRegion region4 = new GeographicRegion(locList4);

//		REGION 5
		LocationList locList5 = new LocationList();
		locList5.addLocation(new Location(33, -120.2));
		locList5.addLocation(new Location(37.5, -115.2));
		locList5.addLocation(new Location(36.75, -114.525));
		locList5.addLocation(new Location(32.25, -119.525));
		GeographicRegion region5 = new GeographicRegion(locList5);

//		REGION 6
		LocationList locList6 = new LocationList();
		locList6.addLocation(new Location(32.25, -119.525));
		locList6.addLocation(new Location(36.75, -114.525));
		locList6.addLocation(new Location(34, -112));
		locList6.addLocation(new Location(29.5, -117));
		GeographicRegion region6 = new GeographicRegion(locList6);

		
		// ERF
		UCERF2 ucerf2 = new UCERF2();
		ucerf2.updateForecast();
		
		System.out.println(MomentInRegionCalc.getMoment(ucerf2, region1));
		System.out.println(MomentInRegionCalc.getMoment(ucerf2, region2));
		System.out.println(MomentInRegionCalc.getMoment(ucerf2, region3));
		System.out.println(MomentInRegionCalc.getMoment(ucerf2, region4));
		System.out.println(MomentInRegionCalc.getMoment(ucerf2, region5));
		System.out.println(MomentInRegionCalc.getMoment(ucerf2, region6));
	}
	
}
