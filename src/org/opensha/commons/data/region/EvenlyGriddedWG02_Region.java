/**
 * 
 */
package org.opensha.commons.data.region;
import org.opensha.commons.calc.RelativeLocation;
import org.opensha.commons.data.Location;
import org.opensha.commons.data.LocationList;



/**
 * WG02 region. Based on email forwarded by Ned on Sep 14, 2007
 * @author vipingupta
 *
 */
public class EvenlyGriddedWG02_Region extends EvenlyGriddedGeographicRegion {
	protected final static double GRID_SPACING = 0.1;

	public EvenlyGriddedWG02_Region() {
		/**
		 * Location list for WG02 region
		 */
		LocationList locList = getLocationList();
		// make polygon from the location list
		createEvenlyGriddedGeographicRegion(locList, GRID_SPACING);
	}

	/**
	 * Location list which formas the outline of the ploygon for WG02 region
	 */
	protected LocationList getLocationList() {
		LocationList locList = new LocationList();

		locList.addLocation(new Location(37.19,-120.61));
		locList.addLocation(new Location(36.43, -122.09));
		locList.addLocation(new Location(38.23, -123.61));
		locList.addLocation(new Location(39.02, -122.08));

		return locList;
	}

	public static void main(String[] args) {
		System.out.println(RelativeLocation.getApproxHorzDistance(36.43, -122.09, 37.19,-120.61));
		System.out.println(RelativeLocation.getApproxHorzDistance(38.23, -123.61, 39.02, -122.08));
		System.out.println(RelativeLocation.getApproxHorzDistance(36.43, -122.09, 38.23, -123.61));
		System.out.println(RelativeLocation.getApproxHorzDistance(37.19,-120.61, 39.02, -122.08));
		
		double wg02Length = (RelativeLocation.getApproxHorzDistance(36.43, -122.09, 38.23, -123.61) +
							RelativeLocation.getApproxHorzDistance(37.19,-120.61, 39.02, -122.08))/2;
		
		double wg02Height = (RelativeLocation.getApproxHorzDistance(36.43, -122.09, 37.19,-120.61)+
							RelativeLocation.getApproxHorzDistance(38.23, -123.61, 39.02, -122.08))/2;
		System.out.println(wg02Length+"\t"+wg02Height+"\n");
		
		// LA locations from Google Earth
		double laLat = (7.87/60.0 + 3.0)/60.0 + 34;
		double laLon = -((36.31/60.0 + 14.0)/60.0 + 118);
		System.out.println("LA Location: "+(float)laLat+"\t"+(float)laLon);
		double lowerLA_lat=laLat-wg02Height/(2*111.1);
		double upperLA_lat=laLat+wg02Height/(2*111.1);
		System.out.println(lowerLA_lat+"\t"+upperLA_lat);
		double lowerLA_lon=laLon-wg02Length/(2*Math.cos(laLat*Math.PI/180)*111.1);
		double upperLA_lon=laLon+wg02Length/(2*Math.cos(laLat*Math.PI/180)*111.1);
		System.out.println(lowerLA_lon+"\t"+upperLA_lon);
		
		// new length & height
		double laLength = (RelativeLocation.getApproxHorzDistance(lowerLA_lat, lowerLA_lon, lowerLA_lat,upperLA_lon)+
							RelativeLocation.getApproxHorzDistance(upperLA_lat, lowerLA_lon, upperLA_lat,upperLA_lon))/2;
		double laHeight = (RelativeLocation.getApproxHorzDistance(lowerLA_lat, lowerLA_lon, upperLA_lat, lowerLA_lon)+
							RelativeLocation.getApproxHorzDistance(lowerLA_lat, upperLA_lon, upperLA_lat, upperLA_lon))/2;
		System.out.println(laLength+"\t"+laHeight);


		
	}

}