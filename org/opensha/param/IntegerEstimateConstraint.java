package org.opensha.param;
import org.opensha.data.estimate.IntegerEstimate;
import org.opensha.exceptions.EditableException;
import java.util.ArrayList;

/**
 * <p>Title: InetgerEstimateConstraint.java </p>
 * <p>Description: A InetgerEstimateConstraint represents a range of allowed
 * values between a min and max integer value, inclusive and a list of allowed
 * Estimate types.
 * The main purpose of this class is to call isAllowed() which will return true
 * if the value is an object of one of the allowed Estimate types and
 * all the values are withing the range.  See the
 * InetgerConstraint javadocs for further documentation. <p>
 * </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class IntegerEstimateConstraint extends IntegerConstraint {
    /** Class name for debugging. */
    protected final static String C = "IntegerEstimateConstraint";
    /** If true print out debug statements. */
    protected final static boolean D = false;


    /** No-Arg Constructor, constraints are null so all values allowed */
   public IntegerEstimateConstraint() { super(); }


   /**
    * Constructor that sets the constraints during instantiation.
    * Sets the min and max values allowed in this constraint. No checks
    * are performed that min and max are consistant with each other.<P>
    *
    * @param  min  The min value allowed
    * @param  max  The max value allowed
    */
   public IntegerEstimateConstraint( int min, int max ) {
       this.min = new Integer( min );
       this.max = new Integer( max );
   }

   /**
    * Constructor that sets the constraints during instantiation.
    * Sets the min and max values allowed in this constraint. No checks
    * are performed that min and max are consistant with each other.<P>
    *
    * @param  min  The min value allowed
    * @param  max  The max value allowed
    */
   public IntegerEstimateConstraint( Integer min, Integer max ) {
       this.min = min;
       this.max = max;
   }


    /**
     * This function first checks whether null values are allowed and if passed
     * in value is a null value. If null values are allowed and passed in value
     * is null value, it returns true. If null values are not allowed and passed
     * in value is a null value, it return false.
     *
     * Then this function checks whether passed in value is an object of IntegerEstimate.
     * If it is not an object of estimate, false is returned else it calls
     * another function isAllowed(Estimate) to check whether this value of
     * Estimate is allowed.
     *
     *
     * @param  obj  The object to check if allowed.
     * @return      True if this is a Estimate and one of the allowed values.
     */
    public boolean isAllowed( Object obj ) {
        if( nullAllowed && ( obj == null ) ) return true;
        else if ( !( obj instanceof IntegerEstimate ) ) return false;
        else return isAllowed( ( IntegerEstimate ) obj );
    }


    /**
     *
     * This function checks first checks that estimate object is one of the
     * allowed estimates. Then it compares the min and max value of constraint
     * with the min and max value from the estimate.
     *
     * @param estimate
     * @return
     */
    public boolean isAllowed(IntegerEstimate estimate) {

      // if this object is among list of allowed estimates, check min/max value
      double allowedMinValue = this.min.intValue();
      double allowedMaxValue = this.max.intValue();
      if (estimate.getMinX() >= allowedMinValue &&
          estimate.getMaxX() <= allowedMaxValue)
        return true;
      return false;
    }


    /**
     *
     * This function always returns false because this constraint only accepts
     * Estimate objects. Any other type of objects are not allowed
     *
     * @param  d  The object to check if allowed.
     * @return   Always return false as Double values are not allowed
     */
    public boolean isAllowed( Integer i ) {
        return isAllowed((Object)i);
    }


    /**
     *
     * This function always returns false because this constraint only accepts
     * Estimate objects. Any other type of objects are not allowed
     *
     * @param  d  The value to check if allowed.
     * @return   Always return false as double values are not allowed
     */
    public boolean isAllowed( int i ) { return isAllowed( new Integer( i ) ); }
}
