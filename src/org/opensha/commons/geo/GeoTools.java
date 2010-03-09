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

import org.opensha.commons.util.DataUtils;

/**
 * This class provides static references to constants and conversions useful
 * for geographic calculations, as well as a variety of utility methods.
 *
 * @author Peter Powers
 * @version $Id:$
 */
public class GeoTools {

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
    public static final double LON_MAX = 180.0;
    
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
    
	
    /**
     * Verifies that an array of latitude values fall within range of 
     * <code>LAT_MIN</code> and <code>LAT_MAX</code> (inclusive).
     * 
     * @param lats latitudes to validate
     * @throws IllegalArgumentException if a <code>lat</code> value is 
     * 		   out of range
     */
    public static final void validateLats(double[] lats) {
        DataUtils.validate(lats, LAT_MIN, LAT_MAX);
    }
        
    /**
     * Verifies that a latitude value falls within range of 
     * <code>LAT_MIN</code> and <code>LAT_MAX</code> (inclusive).
     * 
     * @param lat latitude to validate
     * @throws IllegalArgumentException if <code>lat</code> value is 
     * 		   out of range
     */
    public static final void validateLat(double lat) {
    	DataUtils.validate(lat, LAT_MIN, LAT_MAX);
    }

    /**
     * Verifies that a set of longitude values fall within range of 
     * <code>LON_MIN</code> and <code>LON_MAX</code> (inclusive).
     * 
     * @param lons longitudes to validate
     * @throws IllegalArgumentException if a <code>lon</code> value is 
     * 		   out of range
     */
    public static final void validateLons(double[] lons) {
    	DataUtils.validate(lons, LON_MIN, LON_MAX);
    }

    /**
     * Verifies that a longitude value falls within range of 
     * <code>LON_MIN</code> and <code>LON_MAX</code> (inclusive).
     * 
     * @param lon longitude to validate
     * @throws IllegalArgumentException if <code>lon</code> value is 
     * 		   out of range
     */
    public static final void validateLon(double lon) {
    	DataUtils.validate(lon, LON_MIN, LON_MAX);
    }

    /**
     * Verifies that a set of depth values fall within range of 
     * <code>DEPTH_MIN</code> and <code>DEPTH_MAX</code> (inclusive).
     * 
     * @param depths depths to validate
     * @throws IllegalArgumentException if a <code>depth</code> value is 
     * 		   out of range
     */
    public static final void validateDepths(double[] depths) {
    	DataUtils.validate(depths, DEPTH_MIN, DEPTH_MAX);
    }

    /**
     * Verifies that a depth value falls within range of 
     * <code>DEPTH_MIN</code> and <code>DEPTH_MAX</code> (inclusive).
     * 
     * @param depth depth to validate
     * @throws IllegalArgumentException if a <code>depth</code> value is 
     * 		   out of range
     */
    public static final void validateDepth(double depth) {
    	DataUtils.validate(depth, DEPTH_MIN, DEPTH_MAX);
    }
    

}
