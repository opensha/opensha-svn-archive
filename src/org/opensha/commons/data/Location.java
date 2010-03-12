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

package org.opensha.commons.data;

import static org.opensha.commons.calc.RelativeLocation.LL_PRECISION;
import static org.opensha.commons.geo.GeoTools.TO_DEG;
import static org.opensha.commons.geo.GeoTools.TO_RAD;

import java.io.Serializable;

import org.apache.commons.math.util.MathUtils;
import org.dom4j.Element;
import org.opensha.commons.geo.GeoTools;
import org.opensha.commons.metadata.XMLSaveable;

/**
 * A <code>Location</code> represents a point with reference to the earth's
 * ellipsoid. It is expressed in terms of latitude, longitude, and depth.
 * As in seismology, the convention adopted in OpenSHA is for depth to be
 * positive-down, always. All utility methods in this package assume this
 * to be the case.<br/>
 * <br/>
 * For computational cenvenience, latitude and longitude values are converted
 * and stored internally in radians. Special <code>get***Rad()</code> methods
 * are provided to access this native format.
 * <br/>
 * <code>Location</code> instances are immutable. <br/>
 * 
 * @author Peter Powers
 * @author Sid Hellman
 * @author Steven W. Rock
 * @version $Id:$
 * 
 * TODO should do all error checking... any instantiated Location should be valid

 */
public class Location implements Serializable, XMLSaveable {

	private static final long serialVersionUID = 1L;
    
    public final static String XML_METADATA_NAME = "Location";
    public final static String XML_METADATA_LONGITUDE = "Longitude";
    public final static String XML_METADATA_LATITUDE = "Latitude";
    public final static String XML_METADATA_DEPTH = "Depth";

    private double lat;
    private double lon;
    private double depth;

    // this is meter scale precision and perhaps should be defined elsewhere 
    // NOTE: see also notes at Region.DECIMAL_SCALE
    //public final static DecimalFormat latLonFormat = new DecimalFormat("0.0####");
 
    /**
     * Constructs a new <code>Location</code> with the supplied latitude and
     * longitude and sets the depth to 0.
     * 
     * @param lat latitude in decimal degrees to set
     * @param lon longitude in decimal degrees to set
     * @throws IllegalArgumentException if any supplied values are out of range
     * @see GeoTools
     */
    public Location(double lat, double lon) {
        this(lat, lon, 0);
    }

    /**
     * Constructs a new <code>Location</code> with the supplied latitude, 
     * longitude, and depth values.
     * 
     * @param lat latitude in decimal degrees to set
     * @param lon longitude in decimal degrees to set
     * @param depth in km to set (positive down)
     * @throws IllegalArgumentException if any supplied values are out of range
     * @see GeoTools
     */
    public Location(double lat, double lon, double depth) {
    	GeoTools.validateLat(lat);
    	GeoTools.validateLon(lon);
    	GeoTools.validateDepth(depth);
        this.lat = lat * TO_RAD;
        this.lon = lon * TO_RAD;
        this.depth = depth;
    }

	/**
	 * Returns the depth of this <code>Location</code>.
	 * @return the <code>Location</code> depth in km
	 */
	public double getDepth() {
		return depth;
	}

	/** 
	 * Returns the latitude of this <code>Location</code>.
	 * @return the <code>Location</code> latitude in decimal degrees
	 */
	public double getLatitude() {
		return MathUtils.round(lat * TO_DEG, LL_PRECISION);
	}

	/** 
	 * Returns the longitude of this <code>Location</code>. 
	 * @return the <code>Location</code> longitude in decimal degrees
	 */
	public double getLongitude() {
		return MathUtils.round(lon * TO_DEG, LL_PRECISION);
	}

	/** 
	 * Returns the latitude of this <code>Location</code>.
	 * @return the <code>Location</code> latitude in radians
	 */
	public double getLatRad() {
		return lat;
	}

	/** 
	 * Returns the longitude of this <code>Location</code>. 
	 * @return the <code>Location</code> longitude in radians
	 */
	public double getLonRad() {
		return lon;
	}
    
    @Override
    public Location clone() {
    	return new Location(getLatitude(), getLongitude(), getDepth());
    }
        
    /**
     * Returns this <code>Location</code> formatted as a "lon,lat,depth"
     * <code>String</code> for use in KML documents. This differs from
     * {@link Location#toString()} in that the output lat-lon order are 
     * reversed.
     * @return the location as a <code>String</code> for use with KML markup
     */
    public String toKML() {
    	StringBuffer b = new StringBuffer();
    	b.append(getLongitude());
    	b.append(",");
    	b.append(getLatitude());
    	b.append(",");
    	b.append(getDepth());
    	return b.toString();    	
    }

    /**
     *
     * @param loc Location
     * @return boolean
     */
	///public boolean equalsLocation(Location loc) {
		// NOTE: Was Location comparison being done by floats because the
		// polygon class was being used internally? In any event, GeneralPath 
		// is currently used to define Region borders and it too only
		// takes floats. This is complicating test writing (e.g. comparison
		// to coordinates stored by generated files such as kml); the precision
		// and rounding rules of the formatter in toString() are providing
		// more consistently identical values for Locations that should be the
		// same and has been substituted.
		// TODO We may be able to revert to a numeric comparison if and when 
		// JDK 1.6+ is adopted; all old awt.geom classes that are float 
		// dependent must be modified to their double-based versions.
		// e.g. GeneralPath to Path2D.Double

		// old
//		if ((float) getLatitude() != (float) loc.getLatitude()) return false;
//		if ((float) getLongitude() != (float) loc.getLongitude()) return false;
//		if ((float) getDepth() != (float) loc.getDepth()) return false;
//		return true;
		
//		return toString().equals(loc.toString());
//	}
	
    @Override
	public boolean equals(Object obj){
    	if (this == obj) return true;
    	if (!(obj instanceof Location)) return false;
    	return toString().equals(obj.toString());
    }

    @Override
	public int hashCode() {
    	return (int) ((lat+lon+depth) * 1000);
    }
    
    public Element toXMLMetadata(Element root) {
    	Element xml = root.addElement(Location.XML_METADATA_NAME);
    	xml.addAttribute(Location.XML_METADATA_LATITUDE, getLatitude() + "");
    	xml.addAttribute(Location.XML_METADATA_LONGITUDE, getLongitude() + "");
    	xml.addAttribute(Location.XML_METADATA_DEPTH, getDepth() + "");
    	
    	return root;
    }
    
    public static Location fromXMLMetadata(Element root) {
    	double lat = Double.parseDouble(
    			root.attribute(Location.XML_METADATA_LATITUDE).getValue());
    	double lon = Double.parseDouble
    	(root.attribute(Location.XML_METADATA_LONGITUDE).getValue());
    	double depth = Double.parseDouble(
    			root.attribute(Location.XML_METADATA_DEPTH).getValue());
    	
    	return new Location(lat, lon, depth);
    }

    
}
