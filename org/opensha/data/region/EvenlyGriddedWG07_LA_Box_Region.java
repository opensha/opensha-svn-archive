/**
 * 
 */
package org.opensha.data.region;
import org.opensha.calc.RelativeLocation;


import org.opensha.data.Location;
import org.opensha.data.LocationList;

/**
 * This is a Box around LA with the same dimensions as the WG02 region. The Lats and 
 * Lons come from calculations in the main method of EvenlyGriddedWG02_Region
 * @author ned field
 *
 */
public class EvenlyGriddedWG07_LA_Box_Region extends EvenlyGriddedGeographicRegion {
	protected final static double GRID_SPACING = 0.1;

	public EvenlyGriddedWG07_LA_Box_Region() {
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

		// 33.159	34.945
		// -119.551	-116.936

		locList.addLocation(new Location(33.159,-119.551));
		locList.addLocation(new Location(33.159, -116.936));
		locList.addLocation(new Location(34.945, -116.936));
		locList.addLocation(new Location(34.945, -119.551));

		return locList;
	}
}