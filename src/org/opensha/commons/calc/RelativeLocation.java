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

package org.opensha.commons.calc;

import static java.lang.Math.PI;

import java.text.DecimalFormat;

import org.opensha.commons.data.Direction;
import org.opensha.commons.data.Location;
import org.opensha.commons.data.LocationList;

/**
 * This class contains utility methods to operate on geographic location data.
 * <br>
 * <br>
 * See: <a href="http://williams.best.vwh.net/avform.htm#Dist" target="_blank">
 * Aviation Formulary</a> for formulae implemented in this class as well as
 * <a href="http://www.movable-type.co.uk/scripts/latlong.html"
 * target="_blank">Moveable Type Scripts</a> for other implementations.
 * 
 * TODO rename to GeographicUtils or GeoTools or similar after region merge
 * TODO should Location store lat lon internally in radians
 * TODO should exceptions be thrown for separations large enough to produce
 * 		bad values in any of the 'fast' methods
 *
 * @author Steven W. Rock
 * @author Peter Powers
 * @version $Id$
 * @see Location
 */
public final class RelativeLocation {

	/** Conversion multiplier for degrees to radians */
	public static final double TO_RAD = Math.toRadians(1.0);

	/** Conversion multiplier for radians to degrees */
	public static final double TO_DEG = Math.toDegrees(1.0);
	
	/** Convenience constant for 2 * PI */
	public static final double TWOPI = 2*PI;
	
	/** Convenience constant for PI / 2 */
	public static final double PI_BY_2 = PI/2;
	

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
	
	
	// NOTE: This vlaue is used to 'clean' decimal values that have been 
	// subject to narrowing conversions when creating border Areas. For
	// lat-lon values as high as 180, floats allow for 5 decimal places
	// which equates to m-scale precision. In the case of a buffered region, 
	// this precision loss carries over to the 'border' because it is the one
	// type of region whose border is intialized from the area, not the
	// other way around.
	//
	// If/when JRE 6 becomes standard requirement, new methods and classes 
	// will alleviate the ned for thisnote and allow greater precision
	// (sub-meter scale). Specifically:
	//
	// Region.createArea(LocationList) should be updated to use Path2D.Double()
	//
	// the offset in Region(Location, Location) should be decreased to some
	// value that matches the precision of the Location toString() output
	// format precision
	//
	// the Region test case data arrays will also have to be regenerated
	//
	// note also that with JRE6 Point2D.Double is serializable and DataPoint2D
	// could be retired.
	
	/** The decimal precision imposed on latitude and longitude values. */
	public static final int LL_PRECISION = 5;

	/** 
	 * The formatting imposed on latitude and longitude values to maintain
	 * consistency with <code>LL_PRECISION</code>.
	 */
	public static final DecimalFormat LL_FORMAT = new DecimalFormat("0.0####");

	/**
	 * Calculates the angle between two <code>Location</code>s using the 
	 * <a href="http://en.wikipedia.org/wiki/Haversine_formula" target="_blank">
	 * Haversine</a> formula. This method properly handles values spanning
	 * &#177;180&#176;. See 
	 * <a href="http://williams.best.vwh.net/avform.htm#Dist">
	 * Aviation Formulary</a> for source. Result is returned in radians.
	 * 
	 * @param p1 the first location point
	 * @param p2 the second location point
	 * @return the angle between the points (in radians)
	 */
	public static double angle(Location p1, Location p2) {
		double lat1 = p1.getLatRad();
		double lat2 = p2.getLatRad();
		double sinDlatBy2 = Math.sin((lat2 - lat1) / 2.0);
		double sinDlonBy2 = Math.sin((p2.getLonRad() - p1.getLonRad()) / 2.0);
		// half length of chord connecting points
		double c = (sinDlatBy2 * sinDlatBy2) +
				   (Math.cos(lat1) * Math.cos(lat2) * sinDlonBy2 * sinDlonBy2);
		return 2.0 * Math.atan2(Math.sqrt(c), Math.sqrt(1-c));
	}
	
	/**
	 * Calculates the great circle surface distance between two
	 * <code>Location</code>s using the Haversine formula for 
	 * computing the angle between two points.
	 * 
	 * @param p1 the first location point
	 * @param p2 the second location point
	 * @return the distance between the points in km
	 * @see RelativeLocation#angle(Location, Location)
	 */
	public static double surfaceDistance(Location p1, Location p2) {
		return EARTH_RADIUS_MEAN * angle(p1,p2);
	}
	
	/**
	 * Calculates the distance in three dimensions between two
	 * <code>Location</code>s. Method returns the straight line 
	 * distance taking into account the depths of the points.
	 * 
	 * @param p1 the first location point
	 * @param p2 the second location point
	 * @return the distance in km between the points
	 */
	public static double linearDistance(Location p1, Location p2) {
		double alpha = angle(p1,p2);
		double R1 = EARTH_RADIUS_MEAN - p1.getDepth();
		double R2 = EARTH_RADIUS_MEAN - p2.getDepth();
		double B = R1 * Math.sin(alpha);
		double C = R2 - R1 * Math.cos(alpha);
		return Math.sqrt(B*B + C*C);
	}

	/**
	 * Calculates approximate distance between two <code>Location</code>s. This
	 * method is about 6x faster than <code>surfaceDistance()</code>, but is 
	 * imprecise at large distances and fails where the numeric angle exceeds 
	 * 180&#176;. Method uses the latitudinal and longitudinal differences 
	 * between the points as the sides of a right triangle. The longitudinal 
	 * distance is scaled by the cosine of the mean latitude.
	 * 
	 * @param p1 the first location point
	 * @param p2 the second location point
	 * @return the distance between the points in km
	 */
	public static double fastSurfaceDistance(Location p1, Location p2) {
		// modified from J. Zechar:
		// calculates distance between two points, using formula
		// as specifed by P. Shebalin via email 5.8.2004
		double lat1 = p1.getLatRad();
		double lat2 = p2.getLatRad();
		double dLat = lat1 - lat2;
		double dLon = (p1.getLonRad() - p2.getLonRad()) * 
				Math.cos((lat1 + lat2) * 0.5);
		return  EARTH_RADIUS_MEAN * Math.sqrt((dLat*dLat) + (dLon*dLon));
	}

	/**
	 * Computes the initial azimuth (bearing) when moving from one
	 * <code>Location</code> to another. See 
	 * <a href="http://williams.best.vwh.net/avform.htm#Crs">
	 * Aviation Formulary</a> for source. For back azimuth, 
	 * reverse the <code>Location</code> arguments. Result is returned in
	 * radians.<br/>
	 * <br/>
	 * <b>Note:</b> It is more efficient to use this method for computation 
	 * because <code>Location</code>s store lat and lon in radians internally.
	 * Use <code>azimuth(Location, Location)</code> for presentation.
	 * 
	 * @param p1 first location point
	 * @param p2 second location point
	 * @return the azimuth (bearing) from p1 to p2 in radians
	 * @see #azimuth(Location, Location)
	 */
	public static double azimuthRad(Location p1, Location p2) {
		
		double lat1 = p1.getLatRad();
		double lon1 = p1.getLonRad();
		double lat2 = p2.getLatRad();
		double lon2 = p2.getLonRad();
		
		// check the poles using a small number ~ machine precision
		if (Math.cos(lat1) < 0.000000000001) {
			return ((lat1 > 0) ? PI : 0); // N : S pole
		}
		
		// for starting points other than the poles:
		double dLon = lon2-lon1;
		double cosLat2 = Math.cos(lat2);
		double azRad = Math.atan2(
				Math.sin(dLon) * cosLat2,
				Math.cos(lat1) * Math.sin(lat2) - 
				Math.sin(lat1) * cosLat2 * Math.cos(dLon));

		return (azRad + TWOPI) % TWOPI;
	}

	/**
	 * Computes the initial azimuth (bearing) when moving from one
	 * {@link Location} to another in degrees. See 
	 * <a href="http://williams.best.vwh.net/avform.htm#Crs">
	 * Aviation Formulary</a> for source. For back azimuth, 
	 * reverse the <code>Location</code> arguments. Result is returned in
	 * decimal degrees.
	 * 
	 * @param p1 first location point
	 * @param p2 second location point
	 * @return the azimuth (bearing) from p1 to p2 in decimal degrees
	 * @see #azimuthRad(Location, Location)
	 */
	public static double azimuth(Location p1, Location p2) {
		return azimuthRad(p1, p2) * TO_DEG;
	}
	
	/**
	 * Computes a <code>Location</code> given an origin point, bearing, and 
	 * distance. See <a href="http://williams.best.vwh.net/avform.htm#LL">
	 * Aviation Formulary</a> for source.
	 * 
	 * @param origin or starting location point
	 * @param bearing in radians away from origin
	 * @param distance along bearing
	 * @return the end location 
	 */
	public static Location location(
			Location origin, double bearing, double distance) {
		double lat1 = origin.getLatRad();
		double lon1 = origin.getLonRad();
		double sinLat1 = Math.sin(lat1);
		double cosLat1 = Math.cos(lat1);
		double d = distance / EARTH_RADIUS_MEAN; // angular distance
		double sinD = Math.sin(d);
		double cosD = Math.cos(d);
		
		double lat2 = Math.asin(
				sinLat1 * cosD +
				cosLat1 * sinD * Math.cos(bearing));
		
		double lon2 = lon1 + Math.atan2(
				Math.sin(bearing) * sinD * cosLat1,
				cosD - sinLat1 * Math.sin(lat2));
		
		//double lon2 = ((lon1 + dlon + PI) % TWOPI ) - PI;
		
		return new Location(lat2 * TO_DEG, lon2 * TO_DEG);
	}
	

	
	////////////////////////////////////
	
	 /*
	  * OLD COMMENTS FROM CLASS
	  * 
	 *  These calculations are an adoption from Frankel's FORTRAN code. Perhaps
	 *  a better code base that is more accurate is the geod.exe program from the
	 *  USGS. This code incorporates the idea of ellipsiod models into the program.
	 *  This code is written in c, and can certainly be adopted to java. The geod
	 *  program is part of the Proj codebase. Here are some reference URLS:
	 *
	 *  <UL>
	 *  <LI>http://kai.er.usgs.gov/intro/MAPGENdetails.html
	 *  <LI>ftp://kai.er.usgs.gov/pub/Proj.4/
	 *  <LI>http://www.geog.fu-berlin.de:/cgi-bin/man2html/usr/local/man/man1/geod.1#index
	 *  </UL>
	 *  <p>
*/
	
	/** Earth radius constant */
	public final static int R = 6367;

	/** Radians to degrees conversion constant */
	public final static double RADIANS_CONVERSION = Math.PI / 180;

	/** Degree to Km conversion at equator */
	public final static double D_COEFF = 111.11;


	/** Used for performance testing between two conversion models */
	final static boolean SPEED_TEST = false;

	/** private constructor guarentees it can never be instantiated */
	private RelativeLocation() { }


	/**
	 *  By passing in two Locations this calculator will determine the
	 *  Distance object between them. The four fields calculated are:
	 *
	 * <uL>
	 * <li>horzDistance
	 * <li>azimuth
	 * <li>backAzimuth
	 * <li>vertDistance
	 * </ul>
	 *
	 * @param  location1							First geographic location
	 * @param  location2							Second geographic location
	 * @return									  The direction, decomposition of the vector between two locations
	 * @exception  UnsupportedOperationException	Thrown if the Locations contain bad data such as invalid latitudes
	 * @see	 Distance							to see the field definitions
	 */
	public static Direction getDirection( Location location1, Location location2 ) throws UnsupportedOperationException {

		Direction dir = new Direction();

		double lat1 = location1.getLatitude();
		double lon1 = location1.getLongitude();
		double lat2 = location2.getLatitude();
		double lon2 = location2.getLongitude();

		double horzDistance = getHorzDistance(lat1, lon1, lat2, lon2);
		double azimuth = getAzimuth(lat1, lon1, lat2, lon2);
		double backAzimuth = getBackAzimuth(lat1, lon1, lat2, lon2);
		double vertDistance = -1 * ( location1.getDepth() - location2.getDepth() );

		dir.setHorzDistance(horzDistance);
		dir.setAzimuth(azimuth);
		dir.setBackAzimuth(backAzimuth);
		dir.setVertDistance(vertDistance);

		return dir;
	}

	/**
	 * This computes the total distance in km.
	 * @param loc1
	 * @param loc2
	 * @return
	 */
	public static double getTotalDistance(Location loc1, Location loc2) {

	  double hDist = getHorzDistance(loc1, loc2);
	  double vDist = getVertDistance(loc1, loc2);

	  return  Math.sqrt(hDist*hDist+vDist*vDist);
	}



	public static double getVertDistance(Location loc1, Location loc2) {

		return  -1 * ( loc1.getDepth() - loc2.getDepth() );
	}


	/**
	 *  Given a Location and a Direction object, this function calculates a
	 *  second Location the Direction points to (only the azimuth is used;
	 * backAzimuth is ignored). The fields calculated for the
	 *  second Location are:
	 *
	 * <uL>
	 * <li>Lat
	 * <li>Lon
	 * <li>Depth
	 * </ul>
	 *
	 * @param  location1							First geographic location
	 * @param  direction							Direction object pointing to second Location
	 * @return location2							The second location
	 * @exception  UnsupportedOperationException	Thrown if the Location or Direction contain bad data such as invalid latitudes
	 * @see	 Location							to see the field definitions
	 */
	public static Location getLocation( Location location, Direction direction ) throws UnsupportedOperationException {

		double lat1 = location.getLatitude();
		double lon1 = location.getLongitude();
		double depth = location.getDepth();

		double azimuth = direction.getAzimuth();
		double horzDistance = direction.getHorzDistance();
		double vertDistance = direction.getVertDistance();

		//
		double newLat = getLatitude( horzDistance, azimuth, lat1, lon1 );
		double newLon= getLongitude( horzDistance, azimuth, lat1, lon1 );
//		double newDepth = depth + -1*vertDistance;
		double newDepth = depth + vertDistance;

		//
		Location newLoc = new Location(newLat, newLon, newDepth);
		return newLoc;

	}

	/**
	 * Internal helper method that calculates the latitude of a second location
	 * given the input location and direction components
	 *
	 * @param delta			 Horizontal distance
	 * @param azimuth		   angle towards new point
	 * @param lat			   latitude of original point
	 * @param lon			   longitude of original point
	 * @return				  latitude of new point
	 */
	private static double getLatitude( double delta, double azimuth, double lat, double lon){

		delta = ( delta / D_COEFF ) * RADIANS_CONVERSION;

		double sdelt= Math.sin( delta );
		double cdelt= Math.cos( delta );

		//
		double xlat = lat * RADIANS_CONVERSION;
		//double xlon = lon * RADIANS_CONVERSION;

		//
		double az2 = azimuth * RADIANS_CONVERSION;

		double st0 = Math.cos( xlat );
		double ct0 = Math.sin( xlat );

		//double phi0 = xlon;

		//
		double cz0 = Math.cos( az2 );
		double ct1 = ( st0 * sdelt * cz0 ) + ( ct0 * cdelt );

		double x = (st0 * cdelt ) - ( ct0 * sdelt * cz0 );
		double y = sdelt * Math.sin( az2 );

		//
		double st1 =  Math.pow( ( ( x * x ) + ( y * y ) ), .5 );
		//double dlon = Math.atan2( y, x );


		//
		double newLat = Math.atan2( ct1, st1 ) / RADIANS_CONVERSION;
		return newLat;

	}


	/**
	 * Internal helper method that calculates the longitude of a second location
	 * given the input location and direction components
	 *
	 * @param delta			 Horizontal distance
	 * @param azimuth		   angle towards new point
	 * @param lat			   latitude of original point
	 * @param lon			   longitude of original point
	 * @return				  longitude of new point
	 */
	private static double getLongitude( double delta, double azimuth, double lat, double lon){

		delta = ( delta / D_COEFF ) * RADIANS_CONVERSION;

		double sdelt= Math.sin( delta );
		double cdelt= Math.cos( delta );

		//
		double xlat = lat * RADIANS_CONVERSION;
		double xlon = lon * RADIANS_CONVERSION;

		//
		double az2 = azimuth * RADIANS_CONVERSION;

		double st0 = Math.cos( xlat );
		double ct0 = Math.sin( xlat );

		double phi0 = xlon;

		//
		double cz0 = Math.cos( az2 );
	   // double ct1 = ( st0 * sdelt * cz0 ) + ( ct0 * cdelt );

		double x = (st0 * cdelt ) - ( ct0 * sdelt * cz0 );
		double y = sdelt * Math.sin( az2 );

		//
	  //  double st1 =  Math.pow( ( ( x * x ) + ( y * y ) ), .5 );
		double dlon = Math.atan2( y, x );

		//
		double newLon = ( phi0 + dlon ) / RADIANS_CONVERSION;
		return newLon;

	}


	/**
	 *  Internal helper method that returns the minimum of the two passed in values.
	 *
	 * @param  a  first value to compare
	 * @param  b  second  value to compare
	 * @return	a or b, whichever is smaller
	 */
	private static double getMin( double a, double b ) {
		if ( a <= b ) return a;
		else return b;
	}


	/**
	 * This computes the approximate horizontal distance (in km) using the standard cartesian
	 * coordinate transformation.  Not implemented correctly is lons straddle 360 or 0 degrees!
	*/
	public static double getApproxHorzDistance( double lat1, double lon1, double lat2, double lon2 ){
	  double d1 = (lat1-lat2)*111.111;
	  double d2 = (lon1-lon2)*111.111*Math.cos(((lat1+lat2)/(2))*Math.PI/180);
	  return Math.sqrt(d1*d1+d2*d2);

	}




	/**
	 * Second way to calculate the distance between two points. Obtained off the internet,
	 * but forgot where now. When used in comparision with the latLonDistance function you
	 * see they give practically the same values at the equator, and only start to diverge near the
	 * poles, but still reasonable close to each other. Good for point of comparision.<p>
	 *
	 * Note: This function is currently not used, only for testing<p>
	 */
	public static double getHorzDistance( double lat1, double lon1, double lat2, double lon2 ){

		//
		double xlat = lat1 * RADIANS_CONVERSION;
		double xlon = lon1 * RADIANS_CONVERSION;

		double st0 = Math.cos( xlat );
		double ct0 = Math.sin( xlat );

		double phi0 = xlon;

		//
		xlat = lat2 * RADIANS_CONVERSION;
		xlon = lon2 * RADIANS_CONVERSION;

		double st1 = Math.cos(xlat);
		double ct1 = Math.sin(xlat);


		double sdlon = Math.sin( xlon - phi0);
		double cdlon = Math.cos( xlon - phi0);

		double cdelt = ( st0 * st1 * cdlon ) + ( ct0 * ct1 );

		//
		double x = ( st0 * ct1 ) - ( st1 * ct0 * cdlon );
		double y = st1 * sdlon;

		//
		double sdelt=  Math.pow( ( ( x * x ) + ( y * y ) ), .5 );
		double delta = Math.atan2( sdelt, cdelt ) / RADIANS_CONVERSION;

		delta = delta * D_COEFF;

		return delta;


	}

	public static double getApproxHorzDistance( Location loc1, Location loc2 ) {

		return getApproxHorzDistance(loc1.getLatitude(), loc1.getLongitude(), loc2.getLatitude(), loc2.getLongitude());

	}


	public static double getHorzDistance( Location loc1, Location loc2 ) {

		return getHorzDistance(loc1.getLatitude(), loc1.getLongitude(), loc2.getLatitude(), loc2.getLongitude());

	}

	/**
	 * Helper method that calculates the angle between two locations (value returned is between -180 and 180 degrees)
	 * on the earth.<p>
	 *
	 * @param lat1			   latitude of first point
	 * @param lon1			   longitude of first point
	 * @param lat2			   latitude of second point
	 * @param lon2			   longitude of second point
	 * @return				  angle between the two lat/lon locations
	 */
	public static double getAzimuth( double lat1, double lon1, double lat2, double lon2 ){


		//
		double xlat = lat1 * RADIANS_CONVERSION;
		double xlon = lon1 * RADIANS_CONVERSION;

		double st0 = Math.cos( xlat );
		double ct0 = Math.sin( xlat );

		double phi0 = xlon;

		//
		xlat = lat2 * RADIANS_CONVERSION;
		xlon = lon2 * RADIANS_CONVERSION;

		double st1 = Math.cos(xlat);
		double ct1 = Math.sin(xlat);


		double sdlon = Math.sin( xlon - phi0);
		double cdlon = Math.cos( xlon - phi0);

		//
		double x = ( st0 * ct1 ) - ( st1 * ct0 * cdlon );
		double y = st1 * sdlon;

		double az = Math.atan2( y, x ) / RADIANS_CONVERSION;

		return az;
	}

	/**
	 * Helper method that calculates the angle between two locations
	 * on the earth.<p>
	 *
	 * @param loc1			   location of first point
	 * @param loc2			   location of second point
	 * @return				  angle between the two locations
	 */

	public static double getAzimuth( Location loc1, Location loc2 ){
	  return getAzimuth( loc1.getLatitude(), loc1.getLongitude(),
						 loc2.getLatitude(), loc2.getLongitude() );
	}



	/**
	 * Helper method that calculates the angle between two locations
	 * on the earth.<p>
	 *
	 * Note: SWR: I'm not quite sure of the difference between azimuth and
	 * back azimuth. Ned, you will have to fill in the details.
	 *
	 * @param lat1			   latitude of first point
	 * @param lon1			   longitude of first point
	 * @param lat2			   latitude of second point
	 * @param lon2			   longitude of second point
	 * @return				  angle between the two lat/lon locations
	 */
	public static double getBackAzimuth( double lat1, double lon1, double lat2, double lon2 ){


		//
		double xlat = lat1 * RADIANS_CONVERSION;
		double xlon = lon1 * RADIANS_CONVERSION;

		double st0 = Math.cos( xlat );
		double ct0 = Math.sin( xlat );

		double phi0 = xlon;

		//
		xlat = lat2 * RADIANS_CONVERSION;
		xlon = lon2 * RADIANS_CONVERSION;

		double st1 = Math.cos(xlat);
		double ct1 = Math.sin(xlat);


		double sdlon = Math.sin( xlon - phi0);
		double cdlon = Math.cos( xlon - phi0);


		double x = ( st1 * ct0 ) - ( st0 * ct1 * cdlon );
		double y = -sdlon * st0;

		double baz = Math.atan2( y, x ) / RADIANS_CONVERSION;

		return baz;
	}




	/**
	 * Converts the latitudes in Kms based on the gridSpacing
	 * @return
	 */

	public static double getDeltaLatFromKm(double km) {

	  //1 degree of Latitude is equal to 111.14kms.
	  return km/111.14;
	}

	/**
	 * As the earth is sperical, and does not have a constant radius for each longitude,
	 * so we calculate the longitude spacing (in Kms) for ever latitude
	 * @param lat= value of long for every lat according to gridSpacing
	 * @return
	 */
	public static double getDeltaLonFromKm(double lat,double km){

	  double radius = R * Math.cos(Math.toRadians(lat));
	  double longDistVal = 2*Math.PI *radius /360;
	  return km/longDistVal;
	}


	/**
	 * Returns the radius of the earth at a given latitude (see <a 
	 * href="http://en.wikipedia.org/wiki/Earth_radius#Authalic_radius" 
	 * target="_blank">Wikipedia</a> for source).
	 * 
	 * @param lat the latitude in radians at which to compute the earth's radius
	 * @return the radius for the latitude supplied
	 */
	public static double radiusAtLat(double lat) {
		double cosL = Math.cos(lat);
		double sinL = Math.sin(lat);
		double C1 = cosL * EARTH_RADIUS_EQUATORIAL;
		double C2 = C1 * EARTH_RADIUS_EQUATORIAL;
		double C3 = sinL * EARTH_RADIUS_POLAR;
		double C4 = C3 * EARTH_RADIUS_POLAR;
		return Math.sqrt((C2*C2 + C4*C4) / (C1*C1 + C3*C3));
	}

	// create a locationlist between two points; points are discretized
	// in longitude using 'lonInterval'; latitude intervals are whatever
	// they need to be to get to L2
	private static LocationList createLocList(
			Location L1, Location L2, double lonInterval) {
		int numPoints = (int) Math.floor(Math.abs(
				L2.getLongitude() - L1.getLongitude()) / lonInterval);
		double dLat = (L2.getLatitude() - L1.getLatitude()) / numPoints;
		double dLon = (L1.getLongitude() - L2.getLongitude() < 0) ? 
				lonInterval : -lonInterval;
		LocationList ll = new LocationList();
		double lat = L1.getLatitude();
		double lon = L1.getLongitude();
		for(int i=0; i<=numPoints; i++) {
			//System.out.println(lat + " " + lon);
			ll.addLocation(new Location(lat,lon));
			lat += dLat;
			lon += dLon;
		}
		return ll;
	}

	
	public static void main2(String[] args) {
		
		// commented values are distances calculated using Vincenty formulae
		
		Location L1a = new Location(20,-10); // 8818.496 km
		Location L1b = new Location(-20,60);
		
		Location L2a = new Location(90,10); // 4461.118 km
		Location L2b = new Location(50,80);

		Location L3a = new Location(-80,-30); // 3824.063 km
		Location L3b = new Location(-50,20);
		
		Location L4a = new Location(-42,178); // 560.148 km
		Location L4b = new Location(-38,-178);

		Location L5a = new Location(5,-90); // 784.028 km
		Location L5b = new Location(0,-85);

		Location L6a = new Location(70,-40); // 1148.942 km
		Location L6b = new Location(80,-50);

		Location L7a = new Location(-30,80); // 1497.148 km
		Location L7b = new Location(-20,90);
		
		Location L8a = new Location(70,70); // 234.662 km
		Location L8b = new Location(72,72);

		Location L9a = new Location(-20,120); // 305.532 km
		Location L9b = new Location(-18,122);
		
//		LocationList llL1 = createLocList(L1a,L1b,0.2);
//		LocationList llL2 = createLocList(L2a,L2b,0.2);
//		LocationList llL3 = createLocList(L3a,L3b,0.2);
//		LocationList llL4 = createLocList(L4a,L4b,356); // spans prime meridian
		LocationList llL5 = createLocList(L5a,L5b,0.05);
//		LocationList llL6 = createLocList(L6a,L6b,0.05);
//		LocationList llL7 = createLocList(L7a,L7b,0.05);
//		LocationList llL8 = createLocList(L8a,L8b,0.001);
//		LocationList llL9 = createLocList(L9a,L9b,0.001);
		
		LocationList LLtoUse = llL5;
		Location startPt = LLtoUse.getLocationAt(0);
		for (int i = 1; i < LLtoUse.size(); i++) {
			Location endPt = LLtoUse.getLocationAt(i);
			double surfDist = surfaceDistance(startPt, endPt);
			double fastSurfDist = fastSurfaceDistance(startPt, endPt);
			double delta1 = fastSurfDist - surfDist;
			double horizDist = getHorzDistance(startPt, endPt);
			double approxDist = getApproxHorzDistance(startPt, endPt);
			double delta2 = approxDist - horizDist;
			double delta3 = fastSurfDist - approxDist;
			String s = String.format(
					"sd: %03.4f  fsd: %03.4f  d: %03.4f  " + 
					"hd: %03.4f  ad: %03.4f  d: %03.4f  Df: %03.4f",
					surfDist, fastSurfDist, delta1,
					horizDist, approxDist, delta2, delta3);
			System.out.println(s);
		}
		
	}
	
	public static void main1(String[] args) {
		
		// VALUE GENERATION -- distanceToLine()
		// ====================================
		
		Location L1 = new Location(32.6, 20.4);
		Location L2 = new Location(32.4, 20);
		Location L3 = new Location(32.2, 20.6);
		Location L4 = new Location(32, 20.2);
		
		Location L5 = new Location(90, 0);
		Location L6 = new Location(-90, 0);
		
		//     vd			sd			fsd			angle		az-rad		az-deg
		// d51  6393.578 km	 6382.596	 6474.888	1.001818991	3.141592654	180.0
		// d25  6415.757 km	 6404.835	 6493.824	1.005309649	0.0			  0.0
		// d46 13543.818 km	13565.796	13707.303	2.129301687	3.141592654	180.0
		// d63 13565.996 km	13588.035	13735.216	2.132792346	0.0			  0.0
		
		// d12 43.645957 km	43.6090311	43.6090864  0.006844919 4.179125015 239.44623
		// d13 48.183337 km	48.2790582	48.2790921	0.007577932	2.741190313 157.05864
		// d14 69.150258 km	69.3145862	69.3146382	0.010879690 3.417161139 195.78891
		// d23 60.706703 km	60.6198752	60.6200022	0.009514959	1.943625801 111.36156
		// d42 48.198212 km	48.2952067	48.2952403	0.007580467	5.883856933	337.12017
		// d43 43.787840 km	43.7518411	43.7518956	0.006867335	1.035735858  59.34329
		
		Location p1 = L1;
		Location p2 = L3;
		System.out.println(surfaceDistance(p1, p2));
		System.out.println(fastSurfaceDistance(p1, p2));
		System.out.println(angle(p1,p2));
		System.out.println(azimuthRad(p1, p2));
		System.out.println(azimuth(p1, p2));
		
		System.out.println("----");
		
		Location L7 = new Location( 45.0, -20.0, 2);
		Location L8 = new Location(-40.0, 20.0, 17);
		Location L9 = new Location(-50.0, 20.0, 17);
		
		
		//System.out.println(surfaceDistance(L7,L8));
		System.out.println(linearDistance(L7,L8));
		System.out.println(linearDistance(L7,L9));
		
//		System.out.println("----");
//		double ad = getHorzDistance(L7, L9);
//		double vd = getVertDistance(L7, L9);
//		System.out.println(ad);
//		System.out.println(Math.sqrt(ad*ad + vd*vd));
//		System.out.println(getTotalDistance(L7, L9));
		
		
		
		/*
		// SPEED TEST -- distanceToLine()
		// ================================
		// This is a long distance calculation
		Location dtl1 = new Location(0, 0);
		Location dtl2 = new Location(20, 20);
		Location dtl3 = new Location(10, 10);
		
		// Demonstrates discrepancies between methods at long distances
		System.out.println("\nVALUE COMPARISON -- distanceToLine()\n");
		
		System.out.println(getApproxHorzDistToLine(dtl1,dtl2,dtl3));
		System.out.println(distanceToLine(dtl1,dtl2,dtl3));
		
		int numIter = 1000000;
		 
		System.out.println("\nSPEED TEST -- distanceToLine()\n");
		for (int i=0; i < 5; i++) {
			long T = System.currentTimeMillis();
			for (int j=0; j<numIter; j++) {
				double d = getApproxHorzDistToLine(dtl1,dtl2,dtl3);
			}
			T = (System.currentTimeMillis() - T);
			System.out.println(" AHDTL: " + T);
		}
		System.out.println("");
		for (int i=0; i < 5; i++) {
			long T = System.currentTimeMillis();
			for (int j=0; j<numIter; j++) {
				double d = distanceToLine(dtl1,dtl2,dtl3);
			}
			T = (System.currentTimeMillis() - T);
			System.out.println("   DTL: " + T);
		}
		*/
	}
	
	public static void main3(String[] args) {
		
		Location L1 = new Location(20,-10); // 8818.496 km
		Location L2 = new Location(-20,60);
		int numIter = 10000000;
		 
		for (int i=0; i < 5; i++) {
			long T = System.currentTimeMillis();
			for (int j=0; j<numIter; j++) {
				double surfDist = surfaceDistance(L1, L2);
			}
			T = (System.currentTimeMillis() - T);
			System.out.println(" SD: " + T);
		}
		System.out.println("");
		for (int i=0; i < 5; i++) {
			long T = System.currentTimeMillis();
			for (int j=0; j<numIter; j++) {
				double surfDist = fastSurfaceDistance(L1, L2);
			}
			T = (System.currentTimeMillis() - T);
			System.out.println("FSD: " + T);
		}
		System.out.println("");
//		for (int i=0; i < 5; i++) {
//			long T = System.currentTimeMillis();
//			for (int j=0; j<numIter; j++) {
//				double surfDist = getHorzDistance(L1, L2);
//			}
//			T = (System.currentTimeMillis() - T);
//			System.out.println(" HD: " + T);
//		}
//		System.out.println("");
		for (int i=0; i < 5; i++) {
			long T = System.currentTimeMillis();
			for (int j=0; j<numIter; j++) {
				double surfDist = getApproxHorzDistance(L1, L2);
			}
			T = (System.currentTimeMillis() - T);
			System.out.println(" AD: " + T);
		}
	}
	
	/**
	 *  Purely a tester function. I like to put main() functions for unit testing
	 *  java files. This is a convinient and quick test of the class, and shows
	 *  exact examples on how to use this class.
	 *
	 *  Tests various examples of Locations and Directions to calculate the
	 *  RelativeLocation and direction between points. This is mainly a test
	 *  function
	 *
	 * @param  argv  Passed in command line arguments
	 **/

	//public static void main( String argv[] ) {

//		Location line_loc1 = new Location (0.250405,0.0,5.0);
//		Location line_loc2 = new Location (9.250495,-0.0,5.0);
//		Location loc = new Location (0.9,0.0225,0.0);
//		System.out.println(RelativeLocation.getApproxHorzDistToLine(loc, line_loc1, line_loc2));
//		
//		
//		loc = new Location(0.8999999999999999,0.0225,0.0);
//		line_loc1 = new Location(0.25040500405004046,0.0,5.0);
//		line_loc2 = new Location(9.25049500495005,-2.2242586363405688E-15,5.0);
//		System.out.println(RelativeLocation.getApproxHorzDistToLine(loc, line_loc1, line_loc2));
			 	
		
//		Location L1 = new Location(35.0,-123.0);
//		Location L2 = new Location(35.155243,-123.917579);
//		Location L3 = new Location(35.300824,-122.831149);
//		
//		Location L4 = new Location(35.0,-120.0);
//		Location L5 = new Location(35.155243,-123.917579);
//		Location L6 = new Location(35.300824,-122.831149);
		
		//System.out.println(dist);
		
//		System.out.println(RelativeLocation.getApproxHorzDistToLine(loc, line_loc2, line_loc1));
		// 3.199187934236039
		
		
/*
	  System.out.println("test1:");
	  Location loc1 = new Location(34.5,-128,0);
	  Location loc2 = new Location (33.3,-125,0);
	  Location pt1 = new Location(35,-122,0);
	  Location pt2 = new Location(35,238,0);
	  //Location pt1 = new Location(34,-122,0);
	  //Location pt2 = new Location(35,-122,0);

	  //long t1, t2;
	  //double junk;
	  //t1 = System.currentTimeMillis();
	  //for(int i=0; i < 1000000; i++) //junk = getApproxHorzDistance(pt1,pt2);
	  //t2 = t1 - System.currentTimeMillis();
	  //System.out.println("approx time = "+ t2);
	  System.out.println("Horizontal distance1 = "+ getHorzDistance(loc1,pt1));
	  System.out.println("Horizontal distance2 = "+ getHorzDistance(loc1,pt2));
	  System.out.println("Approx. Horizontal distance1 = "+ getApproxHorzDistance(loc1,pt1));
	  System.out.println("Approx. Horizontal distance2 = "+ getApproxHorzDistance(loc1,pt2));


	  System.out.println("Approx. Horizontal distance Calc from loc1 to Line = "+ getApproxHorzDistToLine(pt1,loc1,loc2));
	  System.out.println("Approx. Horizontal distance Calc from loc2 to Line = "+ getApproxHorzDistToLine(pt2,loc1,loc2));

	  System.out.println("Approx. Horizontal distance(considering line is evenly discretized by 0.1km) from loc1 to Line = "+ getApproxHorzDistToLine_2(pt1,loc1,loc2));
	  System.out.println("Approx. Horizontal distance(considering line is evenly discretized by 0.1km) from loc2 to Line = "+ getApproxHorzDistToLine_2(pt2,loc1,loc2));
*/

	  //t1 = System.currentTimeMillis();
	  //for(int i=0; i < 1000000; i++) //junk = getHorzDistance(pt1,pt2);
	  //t2 = t1 - System.currentTimeMillis();
	  //System.out.println("time = "+ t2);

/*
	  System.out.println(getApproxHorzDistToLine(loc,pt1,pt2));
	  System.out.println(getApproxHorzDistToLine_2(loc,pt1,pt2));
	  System.out.println(getApproxHorzDistToLine(loc,pt2,pt1));
	  System.out.println(getApproxHorzDistToLine_2(loc,pt2,pt1));

	  System.out.println("test2:");
	  loc = new Location(35.00001,-121,0);
	  pt1 = new Location(34,-122,0);
	  pt2 = new Location(35,-122,0);
	  System.out.println(getApproxHorzDistToLine(loc,pt1,pt2));
	  System.out.println(getApproxHorzDistToLine_2(loc,pt1,pt2));
	  System.out.println(getApproxHorzDistToLine(loc,pt2,pt1));
	  System.out.println(getApproxHorzDistToLine_2(loc,pt2,pt1));

	  System.out.println("test3:");
	  loc = new Location(33.9999,-121,0);
	  pt1 = new Location(34,-122,0);
	  pt2 = new Location(35,-122,0);
	  System.out.println(getApproxHorzDistToLine(loc,pt1,pt2));
	  System.out.println(getApproxHorzDistToLine_2(loc,pt1,pt2));
	  System.out.println(getApproxHorzDistToLine(loc,pt2,pt1));
	  System.out.println(getApproxHorzDistToLine_2(loc,pt2,pt1));

	  System.out.println("test4:");
	  loc = new Location(34,-121.5,0);
	  pt1 = new Location(33,-122,0);
	  pt2 = new Location(33,-121,0);
	  System.out.println(getApproxHorzDistToLine(loc,pt1,pt2));
	  System.out.println(getApproxHorzDistToLine_2(loc,pt1,pt2));
	  System.out.println(getApproxHorzDistToLine(loc,pt2,pt1));
	  System.out.println(getApproxHorzDistToLine_2(loc,pt2,pt1));

	  System.out.println("test5:");
	  loc = new Location(34,-122.00001,0);
	  pt1 = new Location(33,-122,0);
	  pt2 = new Location(33,-121,0);
	  System.out.println(getApproxHorzDistToLine(loc,pt1,pt2));
	  System.out.println(getApproxHorzDistToLine_2(loc,pt1,pt2));
	  System.out.println(getApproxHorzDistToLine(loc,pt2,pt1));
	  System.out.println(getApproxHorzDistToLine_2(loc,pt2,pt1));

	  System.out.println("test6:");
	  loc = new Location(34,-120.9999,0);
	  pt1 = new Location(33,-122,0);
	  pt2 = new Location(33,-121,0);
	  System.out.println(getApproxHorzDistToLine(loc,pt1,pt2));
	  System.out.println(getApproxHorzDistToLine_2(loc,pt1,pt2));
	  System.out.println(getApproxHorzDistToLine(loc,pt2,pt1));
	  System.out.println(getApproxHorzDistToLine_2(loc,pt2,pt1));

	  System.out.println("test7:");
	  loc = new Location(33.5,-122,0);
	  pt1 = new Location(33,-122,0);
	  pt2 = new Location(34,-121,0);
	  System.out.println(getApproxHorzDistToLine(loc,pt1,pt2));
	  System.out.println(getApproxHorzDistToLine_2(loc,pt1,pt2));
	  System.out.println(getApproxHorzDistToLine(loc,pt2,pt1));
	  System.out.println(getApproxHorzDistToLine_2(loc,pt2,pt1));

	  System.out.println("test8:");
	  loc = new Location(33.00001,-121,0);
	  pt1 = new Location(33,-122,0);
	  pt2 = new Location(34,-121,0);
	  System.out.println(getApproxHorzDistToLine(loc,pt1,pt2));
	  System.out.println(getApproxHorzDistToLine_2(loc,pt1,pt2));
	  System.out.println(getApproxHorzDistToLine(loc,pt2,pt1));
	  System.out.println(getApproxHorzDistToLine_2(loc,pt2,pt1));

	  System.out.println("test9:");
	  loc = new Location(32.9999,-121,0);
	  pt1 = new Location(33,-122,0);
	  pt2 = new Location(34,-121,0);
	  System.out.println(getApproxHorzDistToLine(loc,pt1,pt2));
	  System.out.println(getApproxHorzDistToLine_2(loc,pt1,pt2));
	  System.out.println(getApproxHorzDistToLine(loc,pt2,pt1));
	  System.out.println(getApproxHorzDistToLine_2(loc,pt2,pt1));

	  System.out.println("test10:");
	  loc = new Location(33.9999,-122,0);
	  pt1 = new Location(33,-122,0);
	  pt2 = new Location(34,-121,0);
	  System.out.println(getApproxHorzDistToLine(loc,pt1,pt2));
	  System.out.println(getApproxHorzDistToLine_2(loc,pt1,pt2));
	  System.out.println(getApproxHorzDistToLine(loc,pt2,pt1));
	  System.out.println(getApproxHorzDistToLine_2(loc,pt2,pt1));

	  System.out.println("test11:");
	  loc = new Location(34.00001,-122,0);
	  pt1 = new Location(33,-122,0);
	  pt2 = new Location(34,-121,0);
	  System.out.println(getApproxHorzDistToLine(loc,pt1,pt2));
	  System.out.println(getApproxHorzDistToLine_2(loc,pt1,pt2));
	  System.out.println(getApproxHorzDistToLine(loc,pt2,pt1));
	  System.out.println(getApproxHorzDistToLine_2(loc,pt2,pt1));
*/

/*
	  System.out.println("Los Angeles Sement Length: = "+
						 (float)RelativeLocation.getHorzDistance( 34.019965922, 118.308353340, 33.971013662, 118.122170045 ));
	  System.out.println("Santa Fe Springs Sement Length: = "+
						 (float)RelativeLocation.getHorzDistance( 33.905266010, 118.144918182, 33.929699246, 118.014078570 ));
	  System.out.println("Coyote Hills Sement Length: = "+
						 (float)RelativeLocation.getHorzDistance( 33.894579252, 118.044407949, 33.899509717, 117.868192971 ));


/*
	  System.out.println("Accurate HorzDist="+RelativeLocation.getHorzDistance( 33, -118, 35, -117.5 ));
	  System.out.println("Approx HorzDist="+RelativeLocation.getApproxHorzDistance( 33, -118, 35, -117.5 ));

	  System.out.println("Starting with accurate horz dist calcs");
	  for(int i=0; i<10000000; i++)
		RelativeLocation.getHorzDistance( 33, -118, 35, -117.5 );
		System.out.println("Done with accurate horz dist calcs");
	  System.out.println("Starting with approx horz dist calcs");
	  for(int i=0; i<10000000; i++)
		RelativeLocation.getApproxHorzDistance( 33, -118, 35, -117.5 );
		System.out.println("Done with approx horz dist calcs");
*/

		/*String S = C + ": main(): ";

		Location l1 = new Location(20, 20);
		l1.setDepth(1);

		Location l2 = new Location(20, 21);
		l2.setDepth(2);

		Location l3 = new Location(20, 40);
		l3.setDepth(3);

		Location l4 = new Location(20, 20);
		l4.setDepth(4);

		Location l5 = new Location(90, 90);
		l5.setDepth(5);

		System.out.println( S + "A(l1,l2): " + latLonDistance( l1.getLatitude(), l1.getLongitude(), l2.getLatitude(), l2.getLongitude() ) );
		System.out.println( S + "B(l1,l2): " + getHorzDistance( l1.getLatitude(), l1.getLongitude(), l2.getLatitude(), l2.getLongitude() ) );


		System.out.println( S + "A(l1,l3): " + latLonDistance( l1.getLatitude(), l1.getLongitude(), l3.getLatitude(), l3.getLongitude() ) );
		System.out.println( S + "B(l1,l3): " + getHorzDistance( l1.getLatitude(), l1.getLongitude(), l3.getLatitude(), l3.getLongitude() ) );


		System.out.println( S + "A(l1,l4): " + latLonDistance( l1.getLatitude(), l4.getLongitude(), l4.getLatitude(), l1.getLongitude() ) );
		System.out.println( S + "B(l1,l4): " + getHorzDistance( l1.getLatitude(), l4.getLongitude(), l4.getLatitude(), l1.getLongitude() ) );


		System.out.println( S + "A(l1,l5): " + latLonDistance( l1.getLatitude(), l5.getLongitude(), l5.getLatitude(), l5.getLongitude() ) );
		System.out.println( S + "B(l1,l5): " + getHorzDistance( l1.getLatitude(), l5.getLongitude(), l5.getLatitude(), l5.getLongitude() ) );

		for(int j = 50; j < 51; j++){
		  for(int i = -360; i <= 0; i++){
			System.out.println("j="+j+"i="+i+"distance="+latLonDistance(j, i, j+1, i+1));
			System.out.println("j="+j+"i="+(i+360)+"distance="+latLonDistance(j, i+360, j+1, i+1));
		  }
		}
*/

	   /* if(SPEED_TEST){

			//System.out.println( S + DateUtils.getDisplayTimeStamp() + ": latLonDistance");
			for(int k = 0; k < 2; k++){
				for(int j = -180; j < 180; j++){
					for(int i = -90; i < 90; i++){
						latLonDistance(i, j, i+1, j+1);
					}
				}
			}
			//System.out.println( S + DateUtils.getDisplayTimeStamp() + ": latLonDistanceDone");

			//System.out.println( S + DateUtils.getDisplayTimeStamp() + ": getHorzDistance");
			for(int k = 0; k < 2; k++){
				for(int j = -180; j < 180; j++){
					for(int i = -90; i < 90; i++){
						getHorzDistance(i, j, i+1, j+1);
					}
				}
			}
			//System.out.println( S + DateUtils.getDisplayTimeStamp() + ": getHorzDistance");
		}

		System.out.println( S );
		System.out.println( S );
		System.out.println( S + l1.toString() );
		System.out.println( S + l2.toString());
		Direction d = RelativeLocation.getDirection(l1,l2);
		System.out.println( S + d.toString());

		System.out.println( S );
		System.out.println( S );
		System.out.println( S + l1.toString() );
		System.out.println( S + l3.toString());
		d = RelativeLocation.getDirection(l1,l3);
		System.out.println( S + d.toString());

		System.out.println( S );
		System.out.println( S );
		System.out.println( S + l1.toString() );
		System.out.println( S + l4.toString());
		d = RelativeLocation.getDirection(l1,l4);
		System.out.println( S + d.toString());

		System.out.println( S );
		System.out.println( S );
		System.out.println( S + l1.toString() );
		System.out.println( S + l5.toString());
		d = RelativeLocation.getDirection(l1,l5);
		System.out.println( S + d.toString());*/

	//}

	/**
	 * Computes the shortest distance between a point and a line.
	 * Both the line and point are assumed to be at the earth's surface. This
	 * is the true spherical trigonometric function for 'off-track distance'; 
	 * See <a href="http://williams.best.vwh.net/avform.htm#XTE">
	 * Aviation Formulary</a> for source. This method, though more accurate
	 * over longer distances and line lengths, is almost 20x slower than
	 * {@link RelativeLocation#getApproxHorzDistToLine(
	 * Location, Location, Location)}.
	 * 
	 * @param p1 the location of the first point defining a line
	 * @param p2 the location of the second point defining a line
	 * @param p3 the location for which the distance from the line will 
	 * 		be calculated
	 * @return the shortest distance in km between the supplied point and line
	 * @see RelativeLocation#distanceToLine(Location, Location, Location)
	 */
	public static double distanceToLine(Location p1, Location p2, Location p3) {
		double d13 = surfaceDistance(p1, p3);	// distance p1 to p3
		double d12 = surfaceDistance(p1, p2);	// distance p1 to p2
		double ad13 = d13 / EARTH_RADIUS_MEAN; 	// angular distance
		double az13 = azimuthRad(p1, p3);		// azimuth p1 to p3
		double az12 = azimuthRad(p1, p2);		// azimuth p1 to p2
		
		// compute cross-track distance
		double xtd = Math.asin( Math.sin(ad13) * 
				Math.sin(az13 - az12)) * EARTH_RADIUS_MEAN;
		// compute along-track distance
		double atd = Math.acos( Math.cos(ad13) / 
				Math.cos(xtd / EARTH_RADIUS_MEAN)) * EARTH_RADIUS_MEAN;
		
		// check if beyond p3
		if (atd > d12) return fastSurfaceDistance(p2, p3); //TODO change to surfaceDistance()
		// check if before p1
		if (Math.cos(az13 - az12) < 0) return d13;
		return xtd;
	}
	
	/**
	 * Computes the shortest distance between a point and a line. Both the 
	 * line and point are assumed to be at the earth's surface. This is a fast,
	 * geometric, cartesion (flat-earth approximation) solution in which
	 * longitude is scaled by the cosine of latitude, appropriate for short
	 * line lengths (e.g. &lt;200 km).
	 * 
	 * @param p1 first <code>Location</code> point on the line
	 * @param p2 second <code>Location</code> point on the line
	 * @param p3 <code>Location</code> point for which distance will 
	 * 		be calculated
	 * @return the shortest distance from the point to the line in km
	 * @see RelativeLocation#distanceToLine(Location, Location, Location)
	 */
	public static double getApproxHorzDistToLine(
			Location p1,
			Location p2,
			Location p3) {

		double lat1 = p1.getLatRad();
		double lat2 = p2.getLatRad();
		double lat3 = p3.getLatRad();
		double lon1 = p1.getLonRad();
		double lon2 = p2.getLonRad();
		double lon3 = p3.getLonRad();

		// use average latitude to scale longitude
		double lonScale = Math.cos(0.5 * lat3 + 0.25 * lat1 + 0.25 * lat2);

		// line-point corrdinates w/ loc transformed to the origin
		double x1 = (lon1 - lon3) * lonScale;
		double x2 = (lon2 - lon3) * lonScale;
		double y1 = lat1 - lat3;
		double y2 = lat2 - lat3;

		double dist;

		// check for values very close to zero
		if (Math.abs(x1 - x2) > 1e-6) {
			double m = (y2 - y1) / (x2 - x1); // slope
			double b = y2 - m * x2; 		  // intercept
			double xT = -m * b / (1 + m * m); // x target
			double yT = m * xT + b; 		  // y target

			// make sure the target point is in between the two endpoints
			boolean betweenPts = false;
			if (x2 > x1) {
				if (xT <= x2 && xT >= x1) betweenPts = true;
			} else {
				if (xT <= x1 && xT >= x2) betweenPts = true;
			}

			if (betweenPts)
				dist = Math.sqrt(xT * xT + yT * yT);
			// return Math.sqrt(xT*xT + yT*yT) * EARTH_RADIUS_MEAN;
			else {
				double d1 = Math.sqrt(x1 * x1 + y1 * y1);
				double d2 = Math.sqrt(x2 * x2 + y2 * y2);
				dist = Math.min(d1, d2);
			}
		} else {
			// the x1 = x2 case
			if (y2 > y1) {
				if (y2 <= 0.0) {
					dist = Math.sqrt(x2 * x2 + y2 * y2);
				} else if (y1 >= 0) {
					dist = Math.sqrt(x1 * x1 + y1 * y1);
				} else {
					dist = Math.abs(x1);
				}
			} else {
				// (y1 > y2)
				if (y1 <= 0.0) {
					dist = Math.sqrt(x1 * x1 + y1 * y1);
				} else if (y2 >= 0) {
					dist = Math.sqrt(x2 * x2 + y2 * y2);
				} else {
					dist = Math.abs(x1);
				}
			}
		}
		return dist * EARTH_RADIUS_MEAN;
	}
	
	public static void main(String[] args) {
		Location l1 = new Location(38.91,93.14);
		Location l2 = new Location(37.94,94.55);
		Location l3 = new Location(40,70);
		System.out.println(getApproxHorzDistToLine(l1,l2,l3));	
	}
	
}
