package org.scec.data;

/**
 *  <b>Title:</b> DataPoint2D<br>
 *  <b>Description:</b> Represents a 2 dimensional point in function space. Each
 *  coordinate is represented by a Double.<p>
 *
 *  The x value is the independent value, and the y value is the dependent
 *  value. Therefore this class is sorted on the x-value. Two DataPoint2D are
 *  equal if there x-values are equal, whatever the y value is. This will be
 *  useful in DiscretizedDFunctions.<p>
 *
 *  Note: This class needs to be enhanced to use Number instead of Double. Then
 *  the values can be a Byte, Double, Float, Integer, Long, or Short, i.e. any
 *  suibclass of Number.<p>
 *
 *  <b>Copyright:</b> Copyright (c) 2001<br>
 *  <b>Company:</b> <br>
 *
 *
 * @author     Steven W. Rock
 * @created    February 20, 2002
 * @see        DiscretizedFunction2DAPI
 * @version    1.0
 */

public class DataPoint2D implements Comparable {


    /*
     *  The name of this class, used for debug statements
     */
    protected final static String C = "IntegerConstraint";
    /**
     *  Static boolean whether to print out debugging statements
     */
    protected final static boolean D = false;

    /**
     *  X coordinate
     */
    private Double x;

    /**
     *  Y coordinate
     */
    private Double y;



    /**
     *  Constructor sets point in x/y space
     *
     * @param  x  Description of the Parameter
     * @param  y  Description of the Parameter
     */
    public DataPoint2D( Double x, Double y ) {
        this.x = x;
        this.y = y;
    }


    /**
     *  Set the X coordinate
     *
     * @param  newX  The new x value
     */
    public void setX( Double newX ) {
        x = newX;
    }


    /**
     *  Set the Y coordinate
     *
     * @param  newY  The new y value
     */
    public void setY( Double newY ) {
        y = newY;
    }


    /**
     *  Set both coordinates at once
     *
     * @param  x  The x coordinate
     * @param  y  The y coordinate
     */
    public void set( Double x, Double y ) {
        this.x = x;
        this.y = y;
    }


    /**
     *  Set both coordinates at once
     *
     * @param  point  Copies x and y value from this point
     */
    public void set( DataPoint2D point ) {
        this.x = point.getX();
        this.y = point.getY();
    }


    /**
     * @return    The x value
     * @todo      Getters / Setters
     */

    /**
     *  Get X coordinate
     *
     * @return    The x value
     */
    public Double getX() {
        return x;
    }


    /**
     *  Get the Y coordinate
     *
     * @return    The y value
     */
    public Double getY() {
        return y;
    }



    /**
     *  Special equals function that returns true if the X Coordinates are the
     *  same, totally ignores the Y Value. This is useful because these points
     *  are used in Discretized functions, and need to sort on the X-Value, the
     *  independent value. Useful for sorting the functions then plotting them
     *
     * @param  point  Point to compare x-value to
     * @return        true if the two x-values are the same
     */
    public boolean equals( DataPoint2D point ) {
        Double xx = point.getX();
        if ( x.equals( xx ) ) {
            return true;
        } else {
            return false;
        }
    }


    /**
     *  Same as equals(DataPoint2D) except that the X values only have to be
     *  close, within tolerance, to bec onsidered equal.
     *
     * @param  point      Point to compare x-value to
     * @param  tolerance  The distance that the two x-values can be and still
     *      considered equal
     * @return            true if the two x-values are the same within tolerance
     */
    public boolean equals( DataPoint2D point, Double tolerance ) {

        double x = this.x.doubleValue();
        double xx = point.getX().doubleValue();

        if ( Math.abs( x - xx ) <= tolerance.doubleValue() ) {
            return true;
        } else {
            return false;
        }
    }


    /**
     *  Special equals to test that both x and y are the same
     *
     * @param  x  The value to compare this object's x-value to
     * @param  y  The value to compare this object's y-value to
     * @return    true if this x-value and y-value equals the passed in x and y
     *      values
     */
    public boolean equals( Double x, Double y ) {
        if ( ( this.x == x ) && ( this.y == y ) ) {
            return true;
        } else {
            return false;
        }
    }


    /**
     *  Returns true if the x values are equal
     *
     * @param  x  The value to compare this x-value to
     * @return    true if this x-value equals the passed in x value
     */
    public boolean equals( Double x ) {
        if ( this.x == x ) {
            return true;
        } else {
            return false;
        }
    }


    /**
     *  Compares two DataPoint2D objects and returns 0 if they have the same x
     *  coordinates, -1 if the first object has a smaller X Value, and +1 if it
     *  has a larger x value.
     *
     * @param  obj                     The DataPoint2D to test
     * @return                         -1 if the x-value of this is smalleer
     *      than the comparing object are the same, 0 if the x-values are equal,
     *      and +1 if this x-value is larger.
     * @exception  ClassCastException  Is thrown if the passed in object is not
     *      a DataPoint2D
     */
    public int compareTo( Object obj ) throws ClassCastException {

        String S = C + ":compareTo(): ";

        if ( !( obj instanceof DataPoint2D ) ) {
            throw new ClassCastException( S + "Object not a DataPoint2D, unable to compare" );
        }

        DataPoint2D point = ( DataPoint2D ) obj;

        int result = 0;

        Double x = this.x;
        Double xx = point.getX();

        return x.compareTo( xx );
    }



    /**
     *  Compares value to see if equal
     *
     * @param  obj                     The DataPoint2D to test
     * @return                         True if this x-value is the same as the
     *      comparing object
     * @exception  ClassCastException  Is thrown if the passed in object is not
     *      a DataPoint2D
     */
    public boolean equals( Object obj ) throws ClassCastException {
        if ( compareTo( obj ) == 0 ) {
            return true;
        } else {
            return false;
        }
    }


    /**
     *  Useful for debugging. Returns the classname and the x & y coordinates of
     *  the point.
     *
     * @return    A descriptive string of the object's state
     */
    public String toString() {
        String TAB = "    ";
        StringBuffer b = new StringBuffer();
        b.append( C );
        b.append( TAB + "X = " + x + '\n' );
        b.append( TAB + "Y = " + y + '\n' );
        return b.toString();
    }


    /**
     *  sets the y-value to the x-value and vise versa
     */
    protected void invert() {

        Double xx = this.y;
        Double yy = this.x;
        y = yy;
        x = xx;

    }


    /**
     *  Returns a copy of this DataPoint2D. If you change the copy, this
     *  original is unaltered
     *
     * @return    An exact copy of this object.
     */
    public Object clone() {
        Double xx = new Double( x.doubleValue() );
        Double yy = new Double( y.doubleValue() );
        DataPoint2D point = new DataPoint2D( xx, yy );
        return point;
    }

}
