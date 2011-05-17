package org.opensha.commons.eq;

/**
 * Utility class for working with magnitudes.
 * 
 * @author Peter Powers
 * @version $Id:$
 * 
 */
public class MagUtils {

	/**
	 * Convert moment magnitude, M<sub>w</sub>, to seismic moment M<sub>
	 * <em>0</em></sub>.
	 * @param magnitude to convert
	 * @return the equivalent seismic moment in Newton-meters
	 */
	public static double magToMoment(double magnitude) {
		return (Math.pow(10, 1.5 * magnitude + 9.05));
	}

	/**
	 * Convert seismic moment, M<sub><em>0</em></sub>, to moment magnutude,
	 * M<sub>w</sub>.
	 * @param moment to convert (in Newton-meters)
	 * @return the equivalent moment magnitude
	 */
	public static double momentToMag(double moment) {
		return (Math.log(moment) / Math.log(10) - 9.05) / 1.5;
	}

}
