package org.scec.data;

import java.util.Comparator;
import org.scec.exceptions.InvalidRangeException;

/**
 *  <b>Title:</b> DataPoint2DComparatorAPI<br>
 *  <b>Description:</b> This interface must be implemented by all comparators of
 *  DataPoint2D. The comparator uses a tolerance to specify when two values are
 *  within tolerance of each other, they are equal<br>
 *
 *
 * @author     Steven W. Rock
 * @created    February 20, 2002
 * @see        DataPoint2D
 * @version    1.0
 */

public interface DataPoint2DComparatorAPI extends Comparator {

    /**
     *  Tolerance indicates the distance two values can be apart, but still
     *  considered equal. This function sets the tolerance.
     *
     * @param  newTolerance               The new tolerance value
     * @exception  InvalidRangeException  Is Thrown if the tolarance is negative
     */
    public void setTolerance( Double newTolerance ) throws InvalidRangeException;


    /**
     *  Tolerance indicates the distance two values can be apart, but still
     *  considered equal. This function returns the tolerance.
     *
     * @return    The tolerance value
     */
    public Double getTolerance();

}
