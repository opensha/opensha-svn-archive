/*******************************************************************************
 * Copyright 2009 OpenSHA.org in partnership with
 * the Southern California Earthquake Center (SCEC, http://www.scec.org)
 * at the University of Southern California and the UnitedStates Geological
 * Survey (USGS; http://www.usgs.gov)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package org.opensha.commons.geo;

import static java.lang.Math.PI;
import static com.google.common.base.Preconditions.*;
import org.opensha.commons.util.DataUtils;

/**
 * This class provides static references to constants and conversions useful
 * for geographic calculations, as well as a variety of utility methods.
 *
 * @author Peter Powers
 * @version $Id$
 */
public class GeoTools {

	/**
	 * The Authalic mean radius (A<subscript>r</subscript>) of the earth 
	 * [6371.0072 km] (see <a 
	 * href="http://en.wikipedia.org/wiki/Earth_radius#Authalic_radius" 
	 * target="_blank">Wikipedia</a>).
	 */
	public static final double EARTH_RADIUS_MEAN = 6371.0072;

	/**
	 * The equatorial radius of the earth [6378.1370 km] (see 
	 * <a href="http://en.wikipedia.org/wiki/Earth_radius#Equatorial_radius" 
	 * target="_blank">Wikipedia</a>) as derived from the WGS-84 ellipsoid.
	 */
	public static final double EARTH_RADIUS_EQUATORIAL = 6378.1370; 
	
	/**
	 * The polar radius of the earth [6356.7523 km] (see <a 
	 * href="http://en.wikipedia.org/wiki/Earth_radius#Polar_radius" 
	 * target="_blank">Wikipedia</a>) as derived from the WGS-84 ellipsoid.
	 */
	public static final double EARTH_RADIUS_POLAR = 6356.7523;

    /** 
     * Minimum latitude value (-90&#176;) used for range checking.
     */
    public static final double LAT_MIN = -90.0;
    
    /** 
     * Maximum latitude value (90&#176;) used for range checking.
     */
    public static final double LAT_MAX = 90.0;
    
    /** 
     * Minimum longitude value (-180&#176;) used for range checking.
     */
    public static final double LON_MIN = -180.0;
    
    /** 
     * Maximum longitude value (180&#176;) used for range checking.
     */
    public static final double LON_MAX = 360.0;
    
    /** 
     * Minimum earthquake depth value (-5 km) used for range checking.
     * This follows the positive-down depth convention of seismology.
     */
    public static final double DEPTH_MIN = -5.0;
    
    /**
     * Maximum earthquake depth value (700 km) used for range checking.
     * This follows the positive-down depth convention of seismology.
     */
    public static final double DEPTH_MAX = 700.0;
    
    
	/** Conversion multiplier for degrees to radians */
	public static final double TO_RAD = Math.toRadians(1.0);

	/** Conversion multiplier for radians to degrees */
	public static final double TO_DEG = Math.toDegrees(1.0);
	
	/** Convenience constant for 2 * PI */
	public static final double TWOPI = 2 * PI;
	
	/** Convenience constant for PI / 2 */
	public static final double PI_BY_2 = PI / 2;
	
	/** Convenience constant for arcseconds per degree (3600). */
	public static final double SECONDS_PER_DEGREE = 3600;

	/** Convenience constant for arcminutes per degree (60). */
	public static final double MINUTES_PER_DEGREE = 60;
	

    /**
     * Verifies that an array of latitude values fall within range of 
     * <code>LAT_MIN</code> and <code>LAT_MAX</code> (inclusive).
     * 
     * @param lats latitudes to validate
	 * @throws NullPointerException if <code>lats</code> is <code>null</code>
     * @throws IllegalArgumentException if any <code>lats</code> value is 
     * 		   out of range
     * @see DataUtils#validate(double, double, double...)
     */
    public static final void validateLats(double[] lats) {
        DataUtils.validate(LAT_MIN, LAT_MAX, lats);
    }
        
    /**
     * Verifies that a latitude value falls within range of 
     * <code>LAT_MIN</code> and <code>LAT_MAX</code> (inclusive).
     * 
     * @param lat latitude to validate
     * @throws IllegalArgumentException if <code>lat</code> value is 
     * 		   out of range
     * @see DataUtils#validate(double, double, double)
     */
    public static final void validateLat(double lat) {
    	DataUtils.validate(LAT_MIN, LAT_MAX, lat);
    }

    /**
     * Verifies that a set of longitude values fall within range of 
     * <code>LON_MIN</code> and <code>LON_MAX</code> (inclusive).
     * 
     * @param lons longitudes to validate
     * @throws IllegalArgumentException if any <code>lons</code> value is 
     * 		   out of range
     * @see DataUtils#validate(double, double, double...)
     */
    public static void validateLons(double[] lons) {
    	DataUtils.validate(LON_MIN, LON_MAX, lons);
    }

    /**
     * Verifies that a longitude value falls within range of 
     * <code>LON_MIN</code> and <code>LON_MAX</code> (inclusive).
     * 
     * @param lon longitude to validate
     * @throws IllegalArgumentException if <code>lon</code> value is 
     * 		   out of range
     * @see DataUtils#validate(double, double, double)
     */
    public static void validateLon(double lon) {
    	DataUtils.validate(LON_MIN, LON_MAX, lon);
    }

    /**
     * Verifies that a set of depth values fall within range of 
     * <code>DEPTH_MIN</code> and <code>DEPTH_MAX</code> (inclusive).
     * 
     * @param depths depths to validate
     * @throws IllegalArgumentException if any <code>depths</code> value is 
     * 		   out of range
     * @see DataUtils#validate(double, double, double...)
     */
    public static void validateDepths(double[] depths) {
    	DataUtils.validate(DEPTH_MIN, DEPTH_MAX, depths);
    }

    /**
     * Verifies that a depth value falls within range of 
     * <code>DEPTH_MIN</code> and <code>DEPTH_MAX</code> (inclusive).
     * 
     * @param depth depth to validate
     * @throws IllegalArgumentException if a <code>depth</code> value is 
     * 		   out of range
     * @see DataUtils#validate(double, double, double)
     */
    public static void validateDepth(double depth) {
    	DataUtils.validate(DEPTH_MIN, DEPTH_MAX, depth);
    }
    
	/**
	 * Returns the radius of the earth at the latitude of the supplied
	 * <code>Location</code> (see <a 
	 * href="http://en.wikipedia.org/wiki/Earth_radius#Authalic_radius" 
	 * target="_blank">Wikipedia</a> for source).
	 * 
	 * @param p the <code>Location</code> at which to compute the earth's radius
	 * @return the earth's radius at the supplied <code>Location</code>
	 */
	public static double radiusAtLocation(Location p) {
		checkNotNull(p, "Supplied location is null");
		double cosL = Math.cos(p.getLatRad());
		double sinL = Math.sin(p.getLatRad());
		double C1 = cosL * EARTH_RADIUS_EQUATORIAL;
		double C2 = C1 * EARTH_RADIUS_EQUATORIAL;
		double C3 = sinL * EARTH_RADIUS_POLAR;
		double C4 = C3 * EARTH_RADIUS_POLAR;
		return Math.sqrt((C2*C2 + C4*C4) / (C1*C1 + C3*C3));
	}
	
	/**
	 * Returns the number of degrees of latitude per km at a given 
	 * <code>Location</code>. This can be used to convert between km-based 
	 * and degree-based grid spacing. The calculation takes into account
	 * the shape of the earth (oblate spheroid) and scales the conversion
	 * accordingly.
	 * 
	 * @param p the <code>Location</code> at which to conversion value
	 * @return the number of decimal degrees latitude per km at a given
	 * 		<code>Location</code>
	 * @see #radiusAtLocation(Location)
	 */
	public static double degreesLatPerKm(Location p) {
		return TO_DEG / radiusAtLocation(checkNotNull(p));
	}

	/**
	 * Returns the number of degrees of longitude per km at a given 
	 * <code>Location</code>. This can be used to convert between km-based
	 * and degree-based grid spacing. The calculation scales the degrees
	 * longitude per km at the equator by the cosine of the supplied
	 * latitude. (<i>Note</i>: The values returned are not based on the radius 
	 * of curvature of the earth at the supplied location.)
	 * 
	 * @param p the <code>Location</code> at which to conversion value
	 * @return the number of decimal degrees longitude per km at a given
	 * 		<code>Location</code>
	 */
	public static double degreesLonPerKm(Location p) {
		return TO_DEG / (EARTH_RADIUS_EQUATORIAL * Math.cos(checkNotNull(p).getLatRad()));
	}
	
	/**
	 * Converts arcseconds to decimal degrees.
	 * @param seconds value to convert
	 * @return the equivalent number of decimal degrees
	 */
	public static double secondsToDeg(double seconds) {
		return seconds / SECONDS_PER_DEGREE;
	}
	
	/**
	 * Converts arcminutes to decimal degrees.
	 * @param minutes value to convert
	 * @return the equivalent number of decimal degrees
	 */
	public static double minutesToDeg(double minutes) {
		return minutes / MINUTES_PER_DEGREE;
	}
	
    /**
     * Converts 'degree : decimal minutes' to decimal degrees.
     * 
     * @param degrees part to convert
     * @param minutes part to convert (decimal minutes)
     * @return converted value
     */
    public static double toDecimalDegrees(double degrees, double minutes) {
        return (degrees < 0) ?
                (degrees - minutesToDeg(minutes)) :
                (degrees + minutesToDeg(minutes));
    }

}
