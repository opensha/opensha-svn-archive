package org.scec.param;

import java.util.*;
import org.scec.exceptions.EditableException;

/**
 *  <b>Title:</b> IntegerConstraint<p>
 *
 *  <b>Description:</b> Constraint Object containing a min and max integer value
 *  allowed. Need a check that min is less that max. If min == max that means
 *  only one discrete value is allowed <p>
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
 * @created    February 21, 2002
 * @version    1.0
 */

public class IntegerConstraint extends ParameterConstraint  {

    /** Class name for debugging. */
    protected final static String C = "IntegerConstraint";
    /** If true print out debug statements. */
    protected final static boolean D = false;

    /**
     *  Minimum allowed integer
     */
    protected Integer min = null;

    /**
     *  Maximum allowed integer
     */
    protected Integer max = null;

    protected String name;


    /**
     *  No-Arg Constructor for the IntegerConstraint object
     */
    public IntegerConstraint() { super(); }


    /**
     *  COnstructor that sets the constraints during instantiation<P>
     *
     *  Note: This should throws an exception if min is greater than max
     *
     * @param  min  Description of the Parameter
     * @param  max  Description of the Parameter
     */
    public IntegerConstraint( int min, int max ) {
        this.min = new Integer( min );
        this.max = new Integer( max );
    }

    /**
     *  COnstructor that sets the constraints during instantiation<P>
     *
     *  Note: This should throws an exception if min is greater than max
     *
     * @param  min  Description of the Parameter
     * @param  max  Description of the Parameter
     */
    public IntegerConstraint( Integer min, Integer max ) {
        this.min = min;
        this.max = max;
    }


    /**
     *  Sets the min and xax attribute of the IntegerConstraint object.<P>
     *
     *  Note: This should throws an exception if min is greater than max
     *
     * @param  min  The new min value
     * @param  max  The new max value
     */
    public void setMinMax( Integer min, Integer max ) throws EditableException {

        String S = C + ": setMinMax(): ";
        checkEditable(S);

        this.min = min;
        this.max = max;
    }

    /**
     *  Sets the min and xax attribute of the IntegerConstraint object.<P>
     *
     *  Note: This should throws an exception if min is greater than max
     *
     * @param  min  The new min value
     * @param  max  The new max value
     */
    public void setMinMax( int min, int max ) throws EditableException {
        setMinMax( new Integer( min ), new Integer( max ) );
    }



    /**
     *  Gets the min attribute of the IntegerConstraint object
     *
     * @return    The min value
     */
    public Integer getMin() { return min; }


    /**
     *  Gets the max attribute of the IntegerConstraint object
     *
     * @return    The max value
     */
    public Integer getMax() { return max; }


    public String getName(){ return name; }
    public void setName(String name){
        String S = C + ": setName(): ";
        checkEditable(S);
        this.name = name;
    }




    /**
     *  Tests if the Object is an Integer and within the min and max constraints
     *
     * @param  obj  The Integer to test if within constraints
     * @return      False if the obj is not an Integer, or if the value is not
     *      within the min and max values
     */
    public boolean isAllowed( Object obj ) {
        if( nullAllowed && ( obj == null ) ) return true;
        else if ( !( obj instanceof Integer ) ) return false;
        else  return isAllowed( ( Integer ) obj );
    }


    /**
     *  Tests if the Integer is within the min and max constraints
     *
     * @param  i  The Integer to test if within constraints
     * @return    False if the value is not within the min and max values
     */
    public boolean isAllowed( Integer i ) {
        if( nullAllowed && ( i == null ) ) return true;
        if( ( min == null ) || ( max == null ) ) return true;
        else if( ( i.compareTo( this.min ) >= 0 ) && ( i.compareTo( this.max ) <= 0 ) )
            return true;
        else return false;
    }


    /**
     *  Tests if the int is within the min and max constraints
     *
     * @param  i  The int to test if within constraints
     * @return    False if the value is not within the min and max values
     */
    public boolean isAllowed( int i ) {
        return isAllowed( new Integer( i ) );
    }


    /**
     *  returns the classname of the constraint, and the min & max as a
     *  formatted debuggin g string
     *
     * @return    Formatted string of object's state
     */
    public String toString() {
        String TAB = "    ";
        StringBuffer b = new StringBuffer();
        b.append( C );
        if( name != null) b.append( TAB + "Name = " + name + '\n' );
        if( min != null)  b.append( TAB + "Min = " + min.toString() + '\n' );
        if( max != null) b.append( TAB + "Max = " + max.toString() + '\n' );
        // b.append( TAB + "Is Editable = " + this.editable + '\n' );
        b.append( TAB + "Null Allowed = " + this.nullAllowed+ '\n' );
        return b.toString();
    }


    /**
     *  Returns an exact clone of this object's state. You can edit the clone
     *  without affecting this objects
     *
     * @return    Clone of this
     */
    public Object clone() {
        IntegerConstraint c1 = new IntegerConstraint( min, max );
        c1.setName( name );
        c1.setNullAllowed( nullAllowed );
        if( !editable ) c1.setNonEditable();
        return c1;
    }

}
