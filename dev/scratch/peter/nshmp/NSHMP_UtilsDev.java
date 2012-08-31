package scratch.peter.nshmp;

import org.opensha.commons.geo.GriddedRegion;
import org.opensha.commons.geo.Location;

/**
 * Class of experimental static utility methods.
 *
 * @author Peter Powers
 * @version $Id:$
 */
public class NSHMP_UtilsDev {

	private static double minLat = 24.6;
	private static double maxLat = 50.0;
	private static double minLon = -125.0;
	private static double maxLon = -65.0;
	private static double spacing = 0.05;

	/**
	 * Returns the gridded region spanned by any NSHMP national scale data set.
	 * Regions has a 0.05 degreee spacing by default.
	 * @return the NSHMP gridded region
	 */
	public static GriddedRegion getNSHMP_Region() {
		Location usHazLoc1 = new Location(minLat, minLon);
		Location usHazLoc2 = new Location(maxLat, maxLon);
		return new GriddedRegion(usHazLoc1, usHazLoc2, spacing,
			GriddedRegion.ANCHOR_0_0);
	}
	
	/**
	 * Returns the gridded region spanned by the NSHMP with a custom spacing.
	 * Such regions may be used to extract subsets of data from an
	 * NSHMP_CurveContainer
	 * @param spacing 
	 * @return the NSHMP gridded region
	 */
	public static GriddedRegion getNSHMP_Region(double spacing) {
		Location usHazLoc1 = new Location(minLat, minLon);
		Location usHazLoc2 = new Location(maxLat, maxLon);
		return new GriddedRegion(usHazLoc1, usHazLoc2, spacing,
			GriddedRegion.ANCHOR_0_0);
	}

	/**
	 * Short form to parse a string to a double; probably should include a call
	 * to trim().
	 * @param s
	 * @return the converted string
	 */
	public static double toNum(String s) {
		return Double.parseDouble(s);
	}

}
