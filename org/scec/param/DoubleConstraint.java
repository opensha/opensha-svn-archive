package org.scec.param;

import java.util.*;
import org.scec.exceptions.EditableException;

/**
 *  <b>Title:</b> DoubleConstraint<p>
 *  <b>Description:</b> Constraint Object containing a min and max double value
 *  allowed. Needs check that min is less that max. <p>
 *
 *  When is min verified to be less than max? When set each value individually,
 *  or after both values are set and call a verify() function that the class
 *  instance is in the correct state. Should throw an exception if not in the
 *  proper state <p>
 *
 *  When do you set the constarints? In the constructor or after the object is
 *  made? <p>
 *
 * @author     Sid Hellman, Steven W. Rock
 * @created    February 20, 2002
 * @version    1.0
 */

public class DoubleConstraint extends ParameterConstraint{

    /** Class name for debugging. */
    protected final static String C = "DoubleConstraint";
    /** If true print out debug statements. */
    protected final static boolean D = false;


    /**
     *  The minimum value allowed in this constraint
     */
    protected Double min = null;
    /**
     *  The maximum value allowed in this constraint
     */
    protected Double max = null;

    /**
     *  No-Arg Constructor
     */
    public DoubleConstraint() { super(); }


    /**
     *  Constructor for the DoubleConstraint object. Sets the min and max values
     *  allowed in this constraint.
     *
     * @param  min  The min value allowed
     * @param  max  The max value allowed
     */
    public DoubleConstraint( double min, double max ) {
        this.min = new Double(min);
        this.max = new Double(max);
    }


    /**
     *  Constructor for the DoubleConstraint object. Sets the min and max values
     *  allowed in this constraint.
     *
     * @param  min  The min value allowed
     * @param  max  The max value allowed
     */
    public DoubleConstraint( Double min, Double max ) {
        this.min = min;
        this.max = max;
    }

    /**
     *  Sets the min and max values allowed in this constraint
     *
     * @param  min  The new min value
     * @param  max  The new max value
     */
    public void setMinMax( double min, double max ) throws EditableException {

        String S = C + ": setMinMax(double, double): ";
        checkEditable(S);
        this.min = new Double( min ) ;
        this.max = new Double( max ) ;
    }

    /**
     *  Sets the min and max values allowed in this constraint
     *
     * @param  min  The new min value
     * @param  max  The new max value
     */
    public void setMinMax( Double min, Double max ) throws EditableException {

        String S = C + ": setMinMax(Double, Double): ";
        checkEditable(S);
        this.min = min;
        this.max = max;
    }


    /**
     *  Gets the min allowed value of this constraint.
     *
     * @return    The max value
     */
    public Double getMin() { return min; }

    /**
     *  Gets the max allowed value of this constraint
     *
     * @return    The max value
     */
    public Double getMax() { return max; }



    /**
     *  Checks if the passed in value is within the min and max, exclusive of
     *  the end points
     *
     * @param  obj  The object to check if allowed
     * @return      True if this is a Double and one of the allowed values
     */
    public boolean isAllowed( Object obj ) {
        if( nullAllowed && ( obj == null ) ) return true;
        else if ( !( obj instanceof Double ) ) return false;
        else return isAllowed( ( Double ) obj );

    }


    /**
     *  Checks if the passed in value is within the min and max, exclusive of
     *  the end points
     *
     * @param  d  The object to check if allowed
     * @return    True if this is one of the allowed values
     */
    public boolean isAllowed( Double d ) {
        if( nullAllowed && ( d == null ) ) return true;
        if( ( min == null ) || ( max == null ) ) return true;
        else if( ( d.compareTo(min) >= 0 ) && ( d.compareTo(max) <= 0 ) ) return true;
        return false;
    }


    /**
     *  Checks if the passed in value is within the min and max, exclusive of
     *  the end points
     *
     * @param  d  The value to check if is allowed
     * @return    true if this value is allowed
     */
    public boolean isAllowed( double d ) { return isAllowed( new Double( d ) ); }


    /**
     *  returns the classname of the constraint, and the min & max
     *
     * @return    Formatted String of this obejct's state
     */
    public String toString() {
        String TAB = "    ";
        StringBuffer b = new StringBuffer();
        //b.append(this.getClass().getName());
        if( name != null ) b.append( TAB + "Name = " + name + '\n' );
        //b.append( TAB + "Is Editable = " + this.editable + '\n' );
        b.append( TAB + "Null Allowed = " + this.nullAllowed+ '\n' );
        if( min != null ) b.append( TAB + "Min = " + min.toString() + '\n' );
        if( max != null ) b.append( TAB + "Max = " + max.toString() + '\n' );
        return b.toString();
    }


    /**
     *  Creates a copy of this object instance. This makes the copy unable to
     *  alter the original, and any way that the cloned copy is used.
     *
     * @return    An exact copy of this constraint
     */
    public Object clone() {
        DoubleConstraint c1 = new DoubleConstraint( min, max );
        c1.setName( name );
        c1.setNullAllowed( nullAllowed );
        if( !this.editable ) c1.setNonEditable();
        return c1;
    }

}
