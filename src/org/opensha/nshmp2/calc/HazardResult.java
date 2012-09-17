package org.opensha.nshmp2.calc;

import org.opensha.commons.data.function.DiscretizedFunc;
import org.opensha.commons.geo.Location;

/**
 * Container for hzaard calculation results.
 *
 * @author Peter Powers
 * @version $Id:$
 */
public class HazardResult {

	private DiscretizedFunc curve;
	private Location loc;
	
	/**
	 * Creates a new hazard result container.
	 * @param curve
	 * @param loc
	 */
	HazardResult(DiscretizedFunc curve, Location loc) {
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
