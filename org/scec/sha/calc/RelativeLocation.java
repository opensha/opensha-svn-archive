package org.scec.sha.calc;
import java.util.*;

import org.scec.sha.fault.*;
import org.scec.data.*;
//import com.egbs.utilities.DateUtils;


/**
 *  <b>Title:</b> RelativeLocation<p>
 *  <b>Description:</b> This class takes two Location objects & returns a
 *  Direction object, or takes a Location and Direction object and returns a
 *  Location object. The functions are static therefore this class is never
 *  instantiated<p>
 *  These calculations are an adoption from Frankel's FORTRAN code. Perhaps
 *  a better code base that is more accurate is the geod.exe program from the
 *  USGS. This code incorporates the idea of ellipsiod models into the program.
 *  This code is written in c, and can certainly be adopted to java. The geod
 *  program is part of the Proj codebase. Here are some reference URLS:
 *  <UL>
 *  <LI>http://kai.er.usgs.gov/intro/MAPGENdetails.html
 *  <LI>ftp://kai.er.usgs.gov/pub/Proj.4/
 *  <LI>http://www.geog.fu-berlin.de:/cgi-bin/man2html/usr/local/man/man1/geod.1#index
 *  </UL>
 *  <p>
 *
 *  SWR: Note: Depth = - vertical distance
 *
 *  <b>Copyright:</b> Copyright (c) 2001<p>
 *  <b>Company:</b> <p>
 *
 *
 * @author     Steven W. Rock
 * @created    February 26, 2002
 * @version    1.0
 */

public final class RelativeLocation {

    /**
     *  Description of the Field
     */
    public final static String C = "RelativeLocation";
    /**
     *  Description of the Field
     */
    public final static int R = 6367;

    public final static double RADIANS_CONVERSION = Math.PI / 180;

    public final static double D_COEFF = 111.11;


    final static boolean SPEED_TEST = false;
    /**
     *  private constructor guarentees it can never be instantiated
     */
    private RelativeLocation() { }


    /**
     *  Gets the direction attribute of the RelativeLocation class
     *
     * @param  location1                          Description of the Parameter
     * @param  location2                          Description of the Parameter
     * @return                                    The direction value
     * @exception  UnsupportedOperationException  Description of the Exception
     */
    public static Direction getDirection( Location location1, Location location2 ) throws UnsupportedOperationException {

        Direction dir = new Direction();

        double lat1 = location1.getLatitude();
        double lon1 = location1.getLongitude();
        double lat2 = location2.getLatitude();
        double lon2 = location2.getLongitude();

        double horzDistance = getLatLonDistance(lat1, lon1, lat2, lon2);
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
     *  Gets the location attribute of the RelativeLocation class
     *
     * @param  location                           Description of the Parameter
     * @param  direction                          Description of the Parameter
     * @return                                    The location value
     * @exception  UnsupportedOperationException  Description of the Exception
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
     *  Gets the min attribute of the RelativeLocation class
     *
     * @param  a  Description of the Parameter
     * @param  b  Description of the Parameter
     * @return    The min value
     */
    private static double getMin( double a, double b ) {
        if ( a <= b ) return a;
        else return b;
    }


    /**
     *  Great Circle distance Calculator. Radius of the Earth in km... change
     *  this to express the great cirle in other units. Coordinates must be
     *  entered in decimal degrees of longitude and latitude. Longitude for the
     *  western hemisphere and latitude for the southern hemisphere are
     *  expressed as negative values.

     * @return       Description of the Return Value
     */
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





    public static double getLatLonDistance( double lat1, double lon1, double lat2, double lon2 ){

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
     *  Description of the Method
     *
     * @param  argv  Description of the Parameter
     */
    public static void main( String argv[] ) {

        String S = C + ": main(): ";

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
        System.out.println( S + "B(l1,l2): " + getLatLonDistance( l1.getLatitude(), l1.getLongitude(), l2.getLatitude(), l2.getLongitude() ) );


        System.out.println( S + "A(l1,l3): " + latLonDistance( l1.getLatitude(), l1.getLongitude(), l3.getLatitude(), l3.getLongitude() ) );
        System.out.println( S + "B(l1,l3): " + getLatLonDistance( l1.getLatitude(), l1.getLongitude(), l3.getLatitude(), l3.getLongitude() ) );


        System.out.println( S + "A(l1,l4): " + latLonDistance( l1.getLatitude(), l4.getLongitude(), l4.getLatitude(), l1.getLongitude() ) );
        System.out.println( S + "B(l1,l4): " + getLatLonDistance( l1.getLatitude(), l4.getLongitude(), l4.getLatitude(), l1.getLongitude() ) );


        System.out.println( S + "A(l1,l5): " + latLonDistance( l1.getLatitude(), l5.getLongitude(), l5.getLatitude(), l5.getLongitude() ) );
        System.out.println( S + "B(l1,l5): " + getLatLonDistance( l1.getLatitude(), l5.getLongitude(), l5.getLatitude(), l5.getLongitude() ) );


        if(SPEED_TEST){

            //System.out.println( S + DateUtils.getDisplayTimeStamp() + ": latLonDistance");
            for(int k = 0; k < 2; k++){
                for(int j = -180; j < 180; j++){
                    for(int i = -90; i < 90; i++){
                        latLonDistance(i, j, i+1, j+1);
                    }
                }
            }
            //System.out.println( S + DateUtils.getDisplayTimeStamp() + ": latLonDistanceDone");

            //System.out.println( S + DateUtils.getDisplayTimeStamp() + ": getLatLonDistance");
            for(int k = 0; k < 2; k++){
                for(int j = -180; j < 180; j++){
                    for(int i = -90; i < 90; i++){
                        getLatLonDistance(i, j, i+1, j+1);
                    }
                }
            }
            //System.out.println( S + DateUtils.getDisplayTimeStamp() + ": getLatLonDistance");
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
        System.out.println( S + d.toString());

    }
}
