package org.scec.param;

import java.util.ListIterator;
import java.util.Vector;
import org.scec.exceptions.ConstraintException;
import org.scec.exceptions.EditableException;

// Fix - Needs more comments

/**
 *  <b>Title:</b> DoubleDiscreteConstraint<p>
 *  <b>Description:</b> Identical to the StringConstraint in that a list of
 *  Doubles are stored in a Vector that are the possible values to choose from<p>
 *
 *
 * @author     Steven W. Rock
 * @created    February 20, 2002
 * @version    1.0
 */

public class DoubleDiscreteConstraint
    extends ParameterConstraint
    implements DiscreteParameterConstraintAPI
{

    /** Class name for debugging. */
    protected final static String C = "DoubleDiscreteConstraint";
    /** If true print out debug statements. */
    protected final static boolean D = false;


    /**
     *  List of possible Double values allowed by this constraint
     */
    private Vector doubles = new Vector();


    /**
     *  No-arg Constructor for the DoubleDiscreteConstraint object
     */
    public DoubleDiscreteConstraint() { super(); }


    /**
     *  Constructor for the DoubleDiscreteConstraint object
     *
     * @param  doubles                  The allowed Doubles in this constraint
     * @exception  ConstraintException  Is thrown if doubles is empty
     */
    public DoubleDiscreteConstraint( Vector doubles ) throws ConstraintException {

        if ( doubles.size() > 0 ) {
            this.doubles = doubles;
        } else {
            String S = "DoubleDiscreteConstraint: Constructor(Vector doubles): ";
            throw new ConstraintException( S + "Input vector of constraint values cannot be empty" );
        }
    }


    /**
     *  Sets the doubles attribute of the DoubleDiscreteConstraint object
     *
     * @param  doubles  The new list of allowed doubles
     */
    public void setDoubles( Vector doubles ) throws EditableException {

        if( !this.editable ) throw new EditableException(C + ": setStrings(): " +
            "This constraint is currently not editable." );

        this.doubles = doubles;
    }


    /**
     *  Returns cloned vector of allowed values, unable to modify original
     *  values
     *
     * @return    The allowedValues value
     */
    public Vector getAllowedValues() {
        return getAllowedDoubles();
    }


    /**
     *  Returns a cloned Vector of the allowed Doubles
     *
     * @return    The allowedDoubles value
     */
    public Vector getAllowedDoubles() {
        return ( Vector ) doubles.clone();
    }


    /**
     *  Checks if the value is allowed
     *
     * @param  d  value to check
     * @return    True if the value is allowed
     */
    public boolean isAllowed( Object obj ) {
        if( nullAllowed && ( obj == null ) ) return true;
        else if ( !( obj instanceof Double ) ) return false;
        else if ( !containsDouble( ( Double ) obj ) ) return false;
        else return true;
    }


    /**
     *  Checks if the value is allowed
     *
     * @param  d  value to check
     * @return    True if the value is allowed
     */
    public boolean isAllowed( Double d ) {
        if( nullAllowed && ( d == null ) ) return true;
        if ( !containsDouble( d ) ) return false;
        else return true;
    }


    /**
     *  Checks if the value is allowed
     *
     * @param  d  value to check
     * @return    True if the value is allowed
     */
    public boolean isAllowed( double d ) {
        return isAllowed( new Double( d ) );
    }


    /**
     *  Returns Iterator over all allowed values
     *
     * @return    The iterator over allowed values
     */
    public ListIterator listIterator() {
        return doubles.listIterator();
    }


    /**
     *  Adds a double to the list of allowed values
     *
     * @param  d  The value to add to the allowed values list
     */
    public void addDouble( double d ) throws EditableException {
        addDouble( new Double(d) );
    }

    /**
     *  Adds a Double to the list of allowed values
     *
     * @param  d  The value to add to the allowed values list
     */
    public void addDouble( Double d ) throws EditableException {

        String S = C + ": addDouble( Double ): ";
        checkEditable(S);

        if ( !containsDouble( d ) ) doubles.add( d );

    }

    /**
     *  Removes one of the allowed values from the list
     *
     * @param  d  The value to remove
     */
    public void removeDouble( Double d ) throws EditableException {

        String S = C + ": removeDouble( Double ): ";
        checkEditable(S);

        if ( containsDouble( d ) ) doubles.remove( d );
    }


    /**
     *  Removes one of the allowed values from the list
     *
     * @param  d  The value to remove
     */
    public void removeDouble( double d ) throws EditableException {
        removeDouble( new Double(d) );
    }

    /**
     *  Checks if the value is one of the allowed objects
     *
     * @param  d  The value to check
     * @return    True if this value is one of the allowed objects
     */
    public boolean containsDouble( Double d ) {
        if ( doubles.contains( d ) ) return true;
        else return false;
    }

    /**
     *  Checks if the value is one of the allowed objects
     *
     * @param  d  The value to check
     * @return    True if this value is one of the allowed objects
     */
    public boolean containsDouble( double d ) {
        return containsDouble( new Double(d) );
    }

    /**
     *  The number of allowed values for this constraint
     *
     * @return    number of allowed values
     */
    public int size() { return doubles.size(); }


    /**
     *  returns the classname of the constraint, and the min & max
     *
     * @return    Formatted String of this obejct's state
     */
    public String toString() {

        String TAB = "    ";
        StringBuffer b = new StringBuffer();
        b.append( this.getClass().getName() );
        if( name != null) b.append( TAB + "Name = " + name + '\n' );
        //b.append( TAB + "Is Editable = " + this.editable + '\n' );
        b.append( TAB + "Null Allowed = " + this.nullAllowed+ '\n' );
        b.append( TAB.concat( "Allowed values = " ) );

        ListIterator it = doubles.listIterator();
        boolean first = true;
        while ( it.hasNext() ) {
            if ( !first ) {
                b.append( TAB + ", " + it.next() );
            } else {
                b.append( TAB + it.next() );
                first = false;
            }
        }
        b.append( '\n' );
        return b.toString();
    }


    /**
     *  Creates a copy of this object instance. This makes the copy unable to
     *  alter the original, and any way that the cloned copy is used.
     *
     * @return    An exact copy of this constraint
     */
    public Object clone() {

        DoubleDiscreteConstraint c1 = new DoubleDiscreteConstraint();
        c1.setName( name );
        Vector v = getAllowedDoubles();
        ListIterator it = v.listIterator();
        while ( it.hasNext() ) {
            Double val = ( Double ) it.next();
            Double val2 = new Double( val.doubleValue() );
            c1.addDouble( val2 );
        }
        c1.setNullAllowed( nullAllowed );
        if( !editable ) c1.setNonEditable();
        return c1;
    }
}
