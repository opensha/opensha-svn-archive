/**
 * 
 */
package org.opensha.commons.data.region;

import org.opensha.commons.data.Location;
import org.opensha.commons.data.LocationList;

/**
 * Testing  region for RELM testing center
 * 
 * @author vipingupta
 *
 */
@Deprecated
public class RELM_TestingRegion extends GeographicRegion {

//	public RELM_TestingRegion() {
//		createGeographicRegion(getLocationList());
//	}
	
	/**
	   * Location list which forms the outline of the ploygon for RELM region
	   * 
	   */
	  private LocationList getLocationList() {
	    LocationList locList = new LocationList();
	    locList.addLocation(new Location(43.0, -125.2));
	    locList.addLocation(new Location(43.0, -119.0));
	    locList.addLocation(new Location(39.4, -119.0));
	    locList.addLocation(new Location(35.7, -114.0));
	    locList.addLocation(new Location(34.3, -113.1));
	    locList.addLocation(new Location(32.9, -113.5));
	    locList.addLocation(new Location(32.2, -113.6));
	    locList.addLocation(new Location(31.7, -114.5));
	    locList.addLocation(new Location(31.5, -117.1));
	    locList.addLocation(new Location(31.9, -117.9));
	    locList.addLocation(new Location(32.8, -118.4));
	    locList.addLocation(new Location(33.7, -121.0));
	    locList.addLocation(new Location(34.2, -121.6));
	    locList.addLocation(new Location(37.7, -123.8));
	    locList.addLocation(new Location(40.2, -125.4));
	    locList.addLocation(new Location(40.5, -125.4));
	    locList.addLocation(new Location(43.0, -125.2));
	    return locList;
	  }
}

