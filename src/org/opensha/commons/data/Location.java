package org.opensha.commons.data;
import java.text.DecimalFormat;

import org.dom4j.Element;
import org.opensha.commons.exceptions.InvalidRangeException;
import org.opensha.commons.metadata.XMLSaveable;


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

public class Location implements java.io.Serializable, XMLSaveable {

	private static final long serialVersionUID = 0xCE5BF55;
	
    /** Class name used for debugging strings  */
    protected final static String C = "Location";
    
    public final static String XML_METADATA_NAME = "Location";
    public final static String XML_METADATA_LONGITUDE = "Longitude";
    public final static String XML_METADATA_LATITUDE = "Latitude";
    public final static String XML_METADATA_DEPTH = "Depth";

    /**  Boolean for debugging, if true debugging statements printed out */
    protected final static boolean D = false;

    /** depth below the surface */
    protected double depth=Double.NaN;

    /** Location Latitude */
    protected double latitude=Double.NaN;

    /** Location longitude */
    protected double longitude=Double.NaN;



    //maximum Latitude
    public static final double MAX_LAT = 90.0;
    //minimum latitude
    public static final double MIN_LAT = -90.0;
    //maximum longitude
    public static final double MAX_LON = 360.0;
    //manimum longitude
    public static final double MIN_LON = -360.0;
    //minimum depth
    public static final double MIN_DEPTH =0.0;


    public final static DecimalFormat latLonFormat = new DecimalFormat("0.0#####");


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
    protected static void validateLatitude( double lat, String S ) throws InvalidRangeException {

        if ( lat < MIN_LAT ) throw new InvalidRangeException( S + "Latitude cannot be less than -90" );
        else if ( lat > MAX_LAT ) throw new InvalidRangeException( S + "Latitude cannot be greater than 90" );
    }


    /**
     *  Checks that longitude is -180 <= lon <= 180.
     *
     * @param  lon                        The longitude to check.
     * @param  S                          Debug String prefix of the calling function.
     * @exception  InvalidRangeException  Thrown if longitude not in the appropiate range.
     */
    protected static void validateLongitude( double lon, String S ) throws InvalidRangeException {
        if ( lon < MIN_LON )  throw new InvalidRangeException( S + "Longitude cannot be less than " + MIN_LON );
        if ( lon > MAX_LON ) throw new InvalidRangeException( S + "Longitude cannot be greater than " + MAX_LON  );
    }


    /**
     *  Checks that depth is >= 0.
     *
     * @param  depth                      The depth to check.
     * @param  S                          Debug String prefix of the calling function.
     * @exception  InvalidRangeException  Thrown if depth is negative.
     */
    protected  static void validateDepth( double depth, String S ) throws InvalidRangeException {
        if ( depth < MIN_DEPTH ) throw new InvalidRangeException( S + "Depth is a negative number" );
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

    // private final static char TAB = '\t';
    /** Prints out all field names and values. useful for debugging. */
    public String toString() {

      StringBuffer b = new StringBuffer();
      //b.append(C);
      //b.append('\n');
      //b.append(" : ");


      //b.append("latitude = ");
        b.append(latLonFormat.format(latitude)+","+latLonFormat.format(longitude)+","+latLonFormat.format(depth));
        //b.append(latitude+","+longitude+","+depth);
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

    /**
     *
     * @param loc Location
     * @return boolean
     */
    public boolean equalsLocation(Location loc) {

      if ( (float)this.latitude != (float) loc.latitude)return false;
      if ( (float)this.longitude != (float) loc.longitude)return false;
      if ( (float)this.depth != (float) loc.depth)return false;

      return true;
    }

    /**
     * Checks if the passed object (obj) is similar to the object locationObject
     * on which this function was called.
     * Indicates whether some other object is "equal to" this one.
     * The equals method implements an equivalence relation  on non-null object references:
     * It is reflexive: for any non-null reference value  x, x.equals(x) should return  true.
     * It is symmetric: for any non-null reference values  x and y, x.equals(y)  should return true if and only if  y.equals(x) returns true.
     * It is transitive: for any non-null reference values  x, y, and z, if  x.equals(y) returns true and  y.equals(z) returns true, then  x.equals(z) should return true.
     * It is consistent: for any non-null reference values  x and y, multiple invocations of  x.equals(y) consistently return true  or consistently return false, provided no  information used in equals comparisons on the  objects is modified.
     * For any non-null reference value x,  x.equals(null) should return false.
     * The equals method for class Object implements the most discriminating possible equivalence relation on objects; that is, for any non-null reference values x and  y, this method returns true if and only  if x and y refer to the same object  (x == y has the value true).
     *Note that it is generally necessary to override the hashCode  method whenever this method is overridden, so as to maintain the  general contract for the hashCode method, which states  that equal objects must have equal hash codes
     * @param obj Object the reference object with which to compare
     * @return boolean true if this object is the same as the obj  argument; false otherwise.
     */
    public boolean equals(Object obj){
        if(obj instanceof Location) return equalsLocation( (Location)obj );
        return false;
    }


    /**
     * This method has been implemented to override the Object's implementation
     * of hashcode. So 2 location objects are equal only if equals() method return
     * true and int value returned from hashcode method are equal.
     * @return int
     */
    public int hashCode() {
      return (int)(latitude+longitude+depth);
    }
    
    public Element toXMLMetadata(Element root) {
    	Element xml = root.addElement(Location.XML_METADATA_NAME);
    	xml.addAttribute(Location.XML_METADATA_LATITUDE, this.getLatitude() + "");
    	xml.addAttribute(Location.XML_METADATA_LONGITUDE, this.getLongitude() + "");
    	xml.addAttribute(Location.XML_METADATA_DEPTH, this.getDepth() + "");
    	
    	return root;
    }
    
    public static Location fromXMLMetadata(Element root) {
    	double lat = Double.parseDouble(root.attribute(Location.XML_METADATA_LATITUDE).getValue());
    	double lon = Double.parseDouble(root.attribute(Location.XML_METADATA_LONGITUDE).getValue());
    	double depth = Double.parseDouble(root.attribute(Location.XML_METADATA_DEPTH).getValue());
    	
    	return new Location(lat, lon, depth);
    }

    public static void main(String[] args) {
      @SuppressWarnings("unused")
	Location loc;
      long time = System.currentTimeMillis();
      for(int i=0; i < 10000;i++) {
        loc = new Location(44,30,0);
      }
      System.out.println("time = "+ (System.currentTimeMillis()-time));
    }

}
