/**
 * 
 */
package org.opensha.data.region;


import org.opensha.data.Location;
import org.opensha.data.LocationList;

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
	  
	
	}