package edu.uah.math.psol.distributions;

/**
 *  This class defines a partition of an interval into subintervals of equal
 *  width. These objects are used to define default domains. A finite domain can
 *  be modeled by the values (midpoints) of the partition. The boundary points
 *  are a + i * w for i = 0, ..., n, where n is the size of the partition, a is
 *  the lower bound and w the width. The values (midpoints) are a + (i + 1/2) *
 *  w, for i = 0, ..., n - 1.
 *
 *  
 *  
 */
public class Domain {
    //Variables
    /**
     *  Description of the Field
     */
    private double lowerBound, upperBound, width, lowerValue, upperValue;
    /**
     *  Description of the Field
     */
    private int size;

    /**
     *  This general constructor creates a new partition of a specified interval
     *  [a, b] into subintervals of width w
     *
     * @param  a  Description of the Parameter
     * @param  b  Description of the Parameter
     * @param  w  Description of the Parameter
     */
    public Domain( double a, double b, double w ) {
        if ( w <= 0 )
            w = 1;
        width = w;
        if ( b < a + w )
            b = a + w;
        lowerBound = a;
        upperBound = b;
        lowerValue = lowerBound + 0.5 * width;
        upperValue = upperBound - 0.5 * width;
        size = ( int ) Math.rint( ( b - a ) / w );
    }

    /**
     *  This special constructor creates a new partition of [0, b] into 10 equal
     *  subintervals
     *
     * @param  b  Description of the Parameter
     */
    public Domain( double b ) {
        this( 0, b, 0.1 * b );
    }

    /**
     *  This default constructor creates a new partition of [0, 1] into 10 equal
     *  subintervals
     */
    public Domain() {
        this( 1 );
    }

    /**
     *  This method returns the index of the interval containing a given value
     *  of x
     *
     * @param  x  Description of the Parameter
     * @return    The index value
     */
    public int getIndex( double x ) {
        if ( x < lowerBound )
            return -1;
        if ( x > upperBound )
            return size;
        else
            return ( int ) Math.rint( ( x - lowerValue ) / width );
    }

    /**
     *  This method returns the boundary point corresponding to a given index
     *
     * @param  i  Description of the Parameter
     * @return    The bound value
     */
    public double getBound( int i ) {
        return lowerBound + i * width;
    }

    /**
     *  This method return the midpoint of the interval corresponding to a given
     *  index
     *
     * @param  i  Description of the Parameter
     * @return    The value value
     */
    public double getValue( int i ) {
        return lowerValue + i * width;
    }

    /**
     *  This method returns the lower bound
     *
     * @return    The lowerBound value
     */
    public double getLowerBound() {
        return lowerBound;
    }

    /**
     *  This method returns the upper bound
     *
     * @return    The upperBound value
     */
    public double getUpperBound() {
        return upperBound;
    }

    /**
     *  This method returns the lower midpoint
     *
     * @return    The lowerValue value
     */
    public double getLowerValue() {
        return lowerValue;
    }

    /**
     *  This method returns the upper midpoint
     *
     * @return    The upperValue value
     */
    public double getUpperValue() {
        return upperValue;
    }

    /**
     *  This method returns the width of the partition
     *
     * @return    The width value
     */
    public double getWidth() {
        return width;
    }

    /**
     *  This method returns the size of the partition (the number of
     *  subintervals)
     *
     * @return    The size value
     */
    public int getSize() {
        return size;
    }
}


