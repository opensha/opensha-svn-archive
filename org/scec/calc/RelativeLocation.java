package org.scec.calc;
import org.scec.data.*;

/**
 *  <b>Title:</b> RelativeLocation<p>
 *
 *  <b>Description:</b>
 *  This class is a distance calculator that deals with Location objects and
 *  a Direction object. From either two you can calculate the third. If you
 *  pass in 2 Location objects this class will return the Direction between
 *  the two. If you pass in a Location and a Direction it can calculate the
 *  second Location object. The functions are static therefore this class is never
 *  instantiated<p>
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
 *
 *  SWR: Note: Depth = - vertical distance<p>
 *
 * @author     Steven W. Rock
 * @created    February 26, 2002
 * @version    1.0
 */

public final class RelativeLocation {



    /** Class name used for debbuging */
    public final static String C = "RelativeLocation";
    /** if true print out debugging statements */
    protected final static boolean D = false;


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
     * @param  location1                            First geographic location
     * @param  location2                            Second geographic location
     * @return                                      The direction, decomposition of the vector between two locations
     * @exception  UnsupportedOperationException    Thrown if the Locations contain bad data such as invalid latitudes
     * @see     Distance                            to see the field definitions
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
     * @param  location1                            First geographic location
     * @param  direction                            Direction object pointing to second Location
     * @return location2                            The second location
     * @exception  UnsupportedOperationException    Thrown if the Location or Direction contain bad data such as invalid latitudes
     * @see     Location                            to see the field definitions
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
        double newDepth = depth + -1*vertDistance;

        //
        Location newLoc = new Location(newLat, newLon, newDepth);
        return newLoc;

    }

    /**
     * Internal helper method that calculates the latitude of a second location
     * given the input location and direction components
     *
     * @param delta             Horizontal distance
     * @param azimuth           angle towards new point
     * @param lat               latitude of original point
     * @param lon               longitude of original point
     * @return                  latitude of new point
     */
    private static double getLatitude( double delta, double azimuth, double lat, double lon){

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
        double ct1 = ( st0 * sdelt * cz0 ) + ( ct0 * cdelt );

        double x = (st0 * cdelt ) - ( ct0 * sdelt * cz0 );
        double y = sdelt * Math.sin( az2 );

        //
        double st1 =  Math.pow( ( ( x * x ) + ( y * y ) ), .5 );
        double dlon = Math.atan2( y, x );


        //
        double newLat = Math.atan2( ct1, st1 ) / RADIANS_CONVERSION;
        return newLat;

    }


    /**
     * Internal helper method that calculates the longitude of a second location
     * given the input location and direction components
     *
     * @param delta             Horizontal distance
     * @param azimuth           angle towards new point
     * @param lat               latitude of original point
     * @param lon               longitude of original point
     * @return                  longitude of new point
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
        double ct1 = ( st0 * sdelt * cz0 ) + ( ct0 * cdelt );

        double x = (st0 * cdelt ) - ( ct0 * sdelt * cz0 );
        double y = sdelt * Math.sin( az2 );

        //
        double st1 =  Math.pow( ( ( x * x ) + ( y * y ) ), .5 );
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
     * @return    a or b, whichever is smaller
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
     * Helper method that calculates the angle between two locations
     * on the earth.<p>
     *
     * @param lat1               latitude of first point
     * @param lon1               longitude of first point
     * @param lat2               latitude of second point
     * @param lon2               longitude of second point
     * @return                  angle between the two lat/lon locations
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
     * Note: SWR: I'm not quite sure of the difference between azimuth and
     * back azimuth. Ned, you will have to fill in the details.
     *
     * @param lat1               latitude of first point
     * @param lon1               longitude of first point
     * @param lat2               latitude of second point
     * @param lon2               longitude of second point
     * @return                  angle between the two lat/lon locations
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

    public static void main( String argv[] ) {

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

    }


    /**
     *  THIS ONE SHOULD Great Circle distance Calculator. Radius of the Earth in km... change
     *  this to express the great cirle in other units. Coordinates must be
     *  entered in decimal degrees of longitude and latitude. Longitude for the
     *  western hemisphere and latitude for the southern hemisphere are
     *  expressed as negative values.

     * @return       Description of the Return Value
     **/
    public static double latLonDistance( double lat1, double lon1, double lat2, double lon2 ) {

        double deltaLon = Math.toRadians( lon2 ) - Math.toRadians( lon1 );
        double deltaLat = Math.toRadians( lat2 ) - Math.toRadians( lat1 );

        double sin2DeltaLat = Math.pow( Math.sin( deltaLat / 2 ), 2 );
        double sin2DeltaLon = Math.pow( Math.sin( deltaLon / 2 ), 2 );
        double cosLat1 = Math.cos( Math.toRadians( lat1 ) );
        double cosLat2 = Math.cos( Math.toRadians( lat2 ) );

        double a = sin2DeltaLat + cosLat1 * cosLat2 * sin2DeltaLon;
        double b = getMin( 1, Math.sqrt( a ) );
        double c = 2 * Math.asin( b );
        return R * c;
    }


    /**
     * This computes the closest distance to any point on a line segment defined by two locations.
     * This will not work if points the step by 360 degrees across the zero longitude.
     * @param loc
     * @param lineLoc1 - 1st point defining the line
     * @param lineLoc2 - 2nd point defining the line
     * @return
     */
    public static double getApproxDistToLine(Location loc, Location lineLoc1, Location lineLoc2) {

      // get the horizontal compression from the latitudes
      double horzCorr = Math.cos( Math.PI*(0.5*loc.getLatitude() + 0.25*lineLoc1.getLatitude()+0.25*lineLoc2.getLatitude())/180.0);

      // get line-point corrdinates (in km) w/ loc transformed to the origin
      double x1 = 111.111*horzCorr*(lineLoc1.getLongitude()-loc.getLongitude());
      double x2 = 111.111*horzCorr*(lineLoc2.getLongitude()-loc.getLongitude());
      double y1 = 111.111*(lineLoc1.getLatitude()-loc.getLatitude());
      double y2 = 111.111*(lineLoc2.getLatitude()-loc.getLatitude());

      double slope = (y2-y1)/(x2-x1);
      double intercept = y2 - slope*x2;

      double xTarget = -slope*intercept/(1 + slope*slope);
      double yTarget = slope*xTarget+ intercept;

      // make sure the target point is in between the two endpoints
      boolean within = false;
      if(x2 > x1) {
        if( xTarget <= x2 && xTarget >= x1) within = true;
      }
      else {
        if( xTarget <= x1 && xTarget >= x2) within = true;
      }

      if (within)
        return Math.sqrt(xTarget*xTarget+yTarget*yTarget);
      else {
        double d1 = Math.sqrt(x1*x1+y2*y2);
        double d2 = Math.sqrt(x2*x2+y2*y2);
        return Math.min(d1,d2);
      }
    }
}



