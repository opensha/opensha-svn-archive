package org.scec.data;
import org.scec.exceptions.InvalidRangeException;

import org.scec.param.*;

/**
 *  <b>Title:</b> Location <br>
 *  <b>Description:</b> This class represents a physical geographic location.
 *  <br>
 *  <b>Note:</b> Commented out elevation. May be put back in the future if
 *  needed. <br>
 *  <b>Copyright:</b> Copyright (c) 2001<br>
 *  <b>Company:</b> <br>
 *
 *
 * @author     Sid Hellman
 * @created    February 26, 2002
 * @version    1.0
 */

public class Location {

    /**
     *  Class name used for debugging strings
     */
    protected final static String C = "Location";

    /**
     *  Boolean for debugging, if true debugging statements printed out
     */
    protected final static boolean D = false;

    /**
     *  Location depth
     */
    protected double depth=Double.NaN;
    /**
     *  Location Latitude
     */
    protected double latitude=Double.NaN;

    /**
     *  Location longitude
     */
    protected double longitude=Double.NaN;


    /**
     *  No-Arg Constructor for the Location object. Currently does nothing.
     */
    public Location() { }


    /**
     *  Constructor that sets latitude and longitude. Depth is defaulted to zero
     *
     * @param  lat                        latitude value
     * @param  lon                        longitude value
     * @exception  InvalidRangeException  thrown if lat or lon are invalid values
     */
    public Location( double lat, double lon )
             throws InvalidRangeException {
        this( lat, lon, 0 );
    }


    /**
     *  Constructor that sets latitude, longitude, and depth.
     *
     * @param  lat                        latitude value
     * @param  lon                        longitude value
     * @param  depth                      location depth
     * @exception  InvalidRangeException  thrown if lat or lon are invalid values
     */
    public Location( double lat, double lon, double depth )
             throws InvalidRangeException {
        String S = C + ": Constructor2(): ";

        validateLatitude( lat, S );
        validateLongitude( lon, S );
        //validateDepth( depth, S );

        this.latitude = lat;
        this.longitude = lon;
        this.depth = depth;
    }





    /**
     *  Sets the depth of the Location
     *
     * @param  depth  The new depth value
     */
    public void setDepth( double depth ) {
        String S = C + ": setDepth(): ";
        //validateDepth( depth.doubleValue(), S );
        this.depth = depth;
    }





    /**
     *  Sets the latitude attribute of the Location object
     *
     * @param  latitude                   The new latitude value
     * @exception  InvalidRangeException  Description of the Exception
     */
    public void setLatitude( double latitude ) throws InvalidRangeException {
        String S = C + ": setLatitude(): ";
        validateLatitude( latitude, S );
        this.latitude = latitude;
    }


    /**
     *  Sets the longitude attribute of the Location object
     *
     * @param  longitude                  The new longitude value
     * @exception  InvalidRangeException  Description of the Exception
     */
    public void setLongitude( double longitude ) throws InvalidRangeException {
        String S = C + ": setLongitude(): ";
        validateLongitude( longitude, S );
        this.longitude = longitude;
    }


    /**
     *  Gets the depth attribute of the Location object
     *
     * @return    The depth value
     */
    public double getDepth() {
        return depth;
    }


    /**
     *  Gets the latitude attribute of the Location object
     *
     * @return    The latitude value
     */
    public double getLatitude() {
        return latitude;
    }


    /**
     *  Gets the longitude attribute of the Location object
     *
     * @return    The longitude value
     */
    public double getLongitude() {
        return longitude;
    }


    /**
     * Checks that latitude is -90 <= lat <= 90
     *
     * @param  lat                        Description of the Parameter
     * @param  S                          Description of the Parameter
     * @exception  InvalidRangeException  Description of the Exception
     */
    protected void validateLatitude( double lat, String S ) throws InvalidRangeException {

        if ( lat < -90 ) {
            throw new InvalidRangeException( S + "Latitude cannot be less than -90" );
        }
        if ( lat > 90 ) {
            throw new InvalidRangeException( S + "Latitude cannot be greater than 90" );
        }
    }


    /**
     *  Checks that latitude is -180 <= lon <= 180
     *
     * @param  lon                        Description of the Parameter
     * @param  S                          Description of the Parameter
     * @exception  InvalidRangeException  Description of the Exception
     */
    protected void validateLongitude( double lon, String S ) throws InvalidRangeException {

        if ( lon < -180 ) {
            throw new InvalidRangeException( S + "Longitude cannot be less than -180" );
        }
        if ( lon > 180 ) {
            throw new InvalidRangeException( S + "Longitude cannot be greater than 180" );
        }
    }


    /**
     *  Checks that depth is >= 0;
     *
     * @param  lon                        Description of the Parameter
     * @param  S                          Description of the Parameter
     * @exception  InvalidRangeException  Description of the Exception
     */
    protected void validateDepth( double depth, String S ) throws InvalidRangeException {

        if ( depth < 0 ) {
            throw new InvalidRangeException( S + "Depth is a negative number" );
        }
    }


    /**
     * Creates a new copy Location with all its values set to this locations values.
     * Since it is a clone, we can modify the copy without affecting the original
     * @return
     */
    public Object clone(){

        Location loc = new Location();
        loc.setDepth( this.depth );
        loc.setLatitude( this.latitude );
        loc.setLongitude( this.longitude );
        return loc;

    }

    private final static char TAB = '\t';
    public String toString(){

        StringBuffer b = new StringBuffer();
        //b.append(C);
        //b.append('\n');
        //b.append(" : ");


        //b.append("latitude = ");
        b.append("" + latitude + TAB + longitude + TAB + depth);
        //b.append('\n');

        /*
        b.append(" : ");

        b.append("longitude = ");
        b.append(longitude);
        //b.append('\n');
        b.append(" : ");

        b.append("depth = ");
        b.append(depth);
        */
        return b.toString();

    }

    public boolean equalsLocation(Location loc){

        if(this.latitude != loc.latitude ) return false;
        if(this.longitude != loc.longitude ) return false;
        if(this.depth != loc.depth ) return false;

        return true;
    }

    public boolean equals(Object obj){
        if(obj instanceof Location) return equalsLocation( (Location)obj );
        else return false;
    }

}
