package org.scec.data;

import org.scec.exceptions.InvalidRangeException;


/**
 *  <b>Title:</b> DataPoint2DComparatorAPI<br>
 *  <b>Description:</b> Implementing comparator of DataPoint2d. The comparator
 *  uses a tolerance to specify that when two values are within tolerance of
 *  each other, they are equal<br>
 *
 *
 * @author     Steven W. Rock
 * @created    February 20, 2002
 * @see        DataPoint2DComparatorAPI
 * @version    1.0
 */

public class DataPoint2DToleranceComparator implements DataPoint2DComparatorAPI {

    /**
     *  The tlorance allowed in specifying a x-value near a real x-value, so
     *  that the real x-value is used. Note that the tolerance must be smaller
     *  than 1/2 the delta between data points. If you set the tolerance to 0,
     *  it is ignored, and defaults to no tolerance. If the tolerance is less
     *  than zero and InvalidRangeException is thrown
     */
    protected Double tolerance = new Double( 0.0 );


    /**
     *  No-Argument constructor
     *
     * @exception  InvalidRangeException
     */
    public DataPoint2DToleranceComparator() throws InvalidRangeException { }


    /**
     *  Constructor that sets the tolerance when created. Throws an
     *  InvalidRangeException if the tolerance is less than zero. Negative
     *  tolerance makes no sense.
     *
     * @param  tolerance                  The distance two values can be apart
     *      and still considered equal
     * @exception  InvalidRangeException  Thrown if tolerance is negative
     */
    public DataPoint2DToleranceComparator( Double tolerance ) throws InvalidRangeException {

        if ( tolerance.doubleValue() < 0 ) {
            throw new InvalidRangeException( "Tolerance must be larger or equal to 0" );
        }

        this.tolerance = tolerance;
    }


    /**
     *  Tolerance indicates the distance two values can be apart, but still
     *  considered equal. This function returns the tolerance.
     *
     * @param  newTolerance               The new tolerance value
     * @exception  InvalidRangeException  Thrown if tolerance is negative
     */
    public void setTolerance( Double newTolerance ) throws InvalidRangeException {
        if ( tolerance.doubleValue() < 0 ) {
            throw new InvalidRangeException( "Tolerance must be larger or equal to 0" );
        }

        tolerance = newTolerance;
    }


    /**
     *  Tolerance indicates the distance two values can be apart, but still
     *  considered equal. This function returns the tolerance.
     *
     * @return    The tolerance value
     */
    public Double getTolerance() {
        return tolerance;
    }


    /**
     *  Returns 0 if the two Objects are equal, -1 if the first object is less
     *  than the second, or +1 if it's greater. This function throws a
     *  ClassCastException if the two values are not DataPoint2Ds. Only the
     *  X-Value is compared, the Y-Value is ignored. If the distance between the
     *  two X-Values are less than or equal to the tolerance, they are
     *  considered equal. <P>
     *
     *  One use for this class is to sort a DiscretizedFunction by it's X-Values
     *  (independent variable) ascending, to prepare the function for plotting.
     *
     * @param  o1                      First DataPoint2D
     * @param  o2                      Second DataPoint2D
     * @return                         -1 if o1 < 02, 0 if o1 = o2, +1 if o1 >
     *      o2
     * @exception  ClassCastException  Thrown if either passed in arg is not a
     *      DataPoint2D
     */
    public int compare( Object o1, Object o2 ) throws ClassCastException {

        if ( !( o1 instanceof DataPoint2D ) ) {
            throw new ClassCastException( "The first object is not an DataPoint2D, unable to compare" );
        }

        if ( !( o2 instanceof DataPoint2D ) ) {
            throw new ClassCastException( "The second object is not an DataPoint2D, unable to compare" );
        }

        double x1 = ( ( DataPoint2D ) o1 ).getX().doubleValue();
        double x2 = ( ( DataPoint2D ) o2 ).getX().doubleValue();

        if ( Math.abs( x1 - x2 ) <= tolerance.doubleValue() ) {
            return 0;
        } else if ( x1 > x2 ) {
            return 1;
        } else {
            return -1;
        }

    }
}
