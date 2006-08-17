package org.opensha.data.region;

import org.opensha.data.Location;
import org.opensha.data.LocationList;


/**
 * Data Collection region for RELM testing center
 * 
 * @author vipingupta
 *
 */
public class RELM_CollectionRegion extends GeographicRegion {

	public RELM_CollectionRegion() {
		createGeographicRegion(getLocationList());
	}
	
	/**
	 * Location list which formas the outline of the ploygon for RELM region
	 * 
	 */
	private LocationList getLocationList() {
		LocationList locList = new LocationList();
		locList.addLocation(new Location(43.5, -125.7));
		locList.addLocation(new Location(43.5, -118.5));
		locList.addLocation(new Location(39.7, -118.5));
		locList.addLocation(new Location(36.1, -113.6));
		locList.addLocation(new Location(34.6, -112.6));
		locList.addLocation(new Location(34.3, -112.6));
		locList.addLocation(new Location(32.7, -113.1));
		locList.addLocation(new Location(31.8, -113.2));
		locList.addLocation(new Location(31.2, -114.5));
		locList.addLocation(new Location(31.0, -117.1));
		locList.addLocation(new Location(31.1, -117.4));
		locList.addLocation(new Location(31.5, -118.3));
		locList.addLocation(new Location(32.4, -118.8));
		locList.addLocation(new Location(33.3, -121.3));
		locList.addLocation(new Location(34.0, -122.0));
		locList.addLocation(new Location(37.5, -124.3));
		locList.addLocation(new Location(40.0, -125.9));
		locList.addLocation(new Location(40.5, -125.9));
		locList.addLocation(new Location(43.0, -125.7));
		locList.addLocation(new Location(43.5, -125.7));
		return locList;
	}
}
