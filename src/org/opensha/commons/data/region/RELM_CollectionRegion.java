package org.opensha.commons.data.region;

import java.io.FileWriter;

import org.opensha.commons.data.Location;
import org.opensha.commons.data.LocationList;


/**
 * Data Collection region for RELM testing center
 * 
 * @author vipingupta
 *
 */
@Deprecated
public class RELM_CollectionRegion extends EvenlyGriddedGeographicRegion {
	 private final static double GRID_SPACING = 0.10;
	public RELM_CollectionRegion() {
		 // make polygon from the location list
	    //createEvenlyGriddedGeographicRegion(getLocationList(), GRID_SPACING);	
	}
	
	/**
	 * Location list which forms the outline of the ploygon for RELM region
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
	
	public static void main(String []args) {
		RELM_CollectionRegion relmCollectionRegion = new RELM_CollectionRegion();
		try {
			FileWriter fw = new FileWriter("RELM_CollectionRegion.txt");
			for(int i=0; i<relmCollectionRegion.getNumGridLocs(); ++i) {
				Location loc = relmCollectionRegion.getGridLocation(i);
				fw.write((float)loc.getLatitude()+","+(float)loc.getLongitude()+"\n");
			}
			fw.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
}
