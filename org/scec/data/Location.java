package org.scec.data;
import org.scec.exceptions.InvalidRangeException;

import org.scec.param.*;

/**
 * <b>Title:</b> Location <p>
 *
 * <b>Description:</b> This class represents a physical geographic location
 * on or in the earth, as represented by 3 double fields, latitude,
 * longitude, and depth below the surface. <p>
 *
 * In java programming this class is simply a JavaBean, i.e. a class with
 * fields and getXXX() and setXXX() functions that match these field names.
 * This class is really a container of data. Not much business functionality
 * here. <p>
 *
 * <b>Note:</b> Commented out elevation. May be put back in the future if needed. <br>
 *
 * @author     Sid Hellman, Steven W. Rock
 * @created    February 26, 2002
 * @version    1.0
 */

public class Location implements java.io.Serializable {

    /** Class name used for debugging strings  */
    protected final static String C = "Location";

    /**  Boolean for debugging, if true debugging statements printed out */
    protected final static boolean D = false;

    /** depth below the surface */
    protected double depth=Double.NaN;

    /** Location Latitude */
    protected double latitude=Double.NaN;

    /** Location longitude */
    protected double longitude=Double.NaN;


    /** No-Arg Constructor for the Location object. Currently does nothing. */
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


    /** Sets the depth */
    public void setDepth( double depth ) { this.depth = depth; }

    /** Sets the latitude. Exception thrown if invalid value. */
    public void setLatitude( double latitude ) throws InvalidRangeException {
        validateLatitude( latitude, C + ": setLatitude(): " );
        this.latitude = latitude;
    }

    /** Sets the longitude. Exception thrown if invalid value. */
    public void setLongitude( double longitude ) throws InvalidRangeException {
        validateLongitude( longitude, C + ": setLongitude(): " );
        this.longitude = longitude;
    }


    /** Returns the depth of this location. */
    public double getDepth() { return depth; }

    /** Returns the latitude of this location. */
    public double getLatitude() { return latitude; }

    /** Returns the longitude of this location. */
    public double getLongitude() { return longitude; }


    /**
     * Checks that latitude is -90 <= lat <= 90.
     *
     * @param  lat                        The latitude to check.
     * @param  S                          Debug String prefix of the calling function.
     * @exception  InvalidRangeException  Thrown if latitude not in the appropiate range.
     */
    protected void validateLatitude( double lat, String S ) throws InvalidRangeException {
        if ( lat < -90 ) throw new InvalidRangeException( S + "Latitude cannot be less than -90" );
        else if ( lat > 90 ) throw new InvalidRangeException( S + "Latitude cannot be greater than 90" );
    }


    /**
     *  Checks that longitude is -180 <= lon <= 180.
     *
     * @param  lon                        The longitude to check.
     * @param  S                          Debug String prefix of the calling function.
     * @exception  InvalidRangeException  Thrown if longitude not in the appropiate range.
     */
    protected void validateLongitude( double lon, String S ) throws InvalidRangeException {
        if ( lon < -180 )  throw new InvalidRangeException( S + "Longitude cannot be less than -180" );
        if ( lon > 180 ) throw new InvalidRangeException( S + "Longitude cannot be greater than 180" );
    }


    /**
     *  Checks that depth is >= 0.
     *
     * @param  depth                      The depth to check.
     * @param  S                          Debug String prefix of the calling function.
     * @exception  InvalidRangeException  Thrown if depth is negative.
     */
    protected void validateDepth( double depth, String S ) throws InvalidRangeException {
        if ( depth < 0 ) throw new InvalidRangeException( S + "Depth is a negative number" );
    }


    /**
     * Creates a new copy Location with all its values set to this locations values.
     * Since it is a clone, we can modify the copy without affecting the original.
     */
    public Object clone(){

        Location loc = new Location();
        loc.setDepth( this.depth );
        loc.setLatitude( this.latitude );
        loc.setLongitude( this.longitude );
        return loc;

    }

    private final static char TAB = '\t';
    /** Prints out all field names and values. useful for debugging. */
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
