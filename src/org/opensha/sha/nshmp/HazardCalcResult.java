package org.opensha.sha.nshmp;

import org.opensha.commons.data.function.DiscretizedFunc;
import org.opensha.commons.geo.Location;

/**
 * Container for hzaard calculation results.
 *
 * @author Peter Powers
 * @version $Id:$
 */
public class HazardCalcResult {

	private DiscretizedFunc curve;
	private Location loc;
	
	HazardCalcResult(DiscretizedFunc curve, Location loc) {
		this.curve = curve;
		this.loc = loc;
	}
	
	/**
	 * Returns the hazard curve for this result.
	 * @return the hazard curve
	 */
	public DiscretizedFunc curve() {
		return curve;
	}
	
	/**
	 * Returns the location of this result.
	 * @return the result location
	 */
	public Location location() {
		return loc;
	}
	
}
