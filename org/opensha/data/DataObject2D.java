package org.opensha.data;

/**
 *  <b>Title:</b> DataObject2D<p>
 *
 *  <b>Description:</b> Represents a 2 dimensional Object (X,Y) in function space. Each
 *  coordinate is represented by Object.<p>
 *
 *  X and Y can be any kind objects<p>
 *
 * @author     Nitin Gupta and Vipin Gupta
 *
 * @created    July 20,2005
 * @version    1.0
 */

public class DataObject2D implements java.io.Serializable {


    /* The name of this class, used for debug statements */
    protected final static String C = "DataObject2D";

    /** Static boolean whether to print out debugging statements  */
    protected final static boolean D = false;

    /**  X value Object */
    private Object x = null;

    /** Y value object */
    private Object y = null;



    /**
     *  Constructor sets point data in x/y space
     *
     * @param  x  x corrdinate value
     * @param  y  y corrdinate value
     */
    public DataObject2D( Object x, Object y ) {
        this.x = x;
        this.y = y;
    }


    /** Get the X value object */
    public Object getX() { return x; }

    /** Set the X value object. No validation checks are done on new value */
    public void setX( Object newX ) { x = newX; }

    /** Get the Y value Object */
    public Object getY() { return y; }

    /** Set the Y coordinate. No validation checks are done on new value */
    public void setY( Object newY ) { y = newY; }


    /**
     *  Set both coordinates at once.
     *  No validation checks are done on new values.
     *
     * @param  x  The X value object
     * @param  y  The y value object
     */
    public void set( Object x, Object y ) {
        this.x = x;
        this.y = y;
    }


    /**
     *  Set both objects at once from the passed in DataObject2D.
     *  In other words performs a copy. No validation checks are
     *  done on new values.
     *
     * @param  xyObjects  Copies x and y value from this new object
     */
    public void set( DataObject2D xyObjects ) {
        this.x = xyObjects.getX();
        this.y = xyObjects.getY();
    }


    /**
     *  Checks to see the Object is of type DataObject2D
     * @exception  ClassCastException  Is thrown if the passed in object is not
     *      a DataObject2D
     */
    public int compareTo( Object obj ) throws ClassCastException {

        String S = C + ":compareTo(): ";

        if ( !( obj instanceof DataObject2D ) ) {
            throw new ClassCastException( S + "Object not a DataPoint2D, unable to compare" );
        }

        return 0;

    }



    /**
     *  Checks to see the Object is of type DataObject2D
     * @exception  ClassCastException  Is thrown if the passed in object is not
     *      a DataObject2D
     */
    public boolean equals( Object obj ) throws ClassCastException {
        if ( compareTo( obj ) == 0 ) {
            return true;
        } else {
            return false;
        }
    }


    /**
     *  Useful for debugging. Returns the classname and the x & y Objects
     *
     * @return    A descriptive string of the object's state
     */
    public String toString() {
        String TAB = "    ";
        StringBuffer b = new StringBuffer();
        b.append( C );
        b.append( TAB + "X Object = " + x.toString() + '\n' );
        b.append( TAB + "Y Object = " + y.toString() + '\n' );
        return b.toString();
    }


    /**
     *  sets the y Object to the x Object and vise versa. <p>
     */
    protected void invert() {

        Object xx = this.y;
        Object yy = this.x;
        y = yy;
        x = xx;

    }


    /**
     *  Returns a copy of this DataObject2D. If you change the copy,
     *  original is unaltered.
     *
     * @return    An exact copy of this object.
     */
    public Object clone() {
        Object xx = this.x;
        Object yy = this.y;
        DataObject2D obj = new DataObject2D( xx, yy );
        return obj;
    }

}
