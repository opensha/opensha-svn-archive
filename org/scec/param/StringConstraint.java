package org.scec.param;
import java.util.ListIterator;

import java.util.Vector;
import org.scec.exceptions.ConstraintException;
import org.scec.exceptions.EditableException;

/**
 *  <b>Title:</b> StringConstraint<p>
 *  <b>Description:</b> Constraint Object containing a vector of allowed string
 *  values<p>
 *
 * @author     Sid Hellman, Steven W. Rock
 * @created    February 21, 2002
 * @version    1.0
 */

public class StringConstraint extends ParameterConstraint implements DiscreteParameterConstraintAPI {

    /**
     *  Class name for debugging.
     */
    protected final static String C = "StringConstraint";
    /**
     *  If true print out debug statements.
     */
    protected final static boolean D = false;


    /**
     *  Vector list of possible string values, i.e. allowed values.
     */
    private Vector strings = new Vector();




    /**
     *  No-Arg constructor for the StringConstraint object.
     */
    public StringConstraint() { super(); }


    /**
     *  Constructor for the StringConstraint object. Sets all allowed strings
     *  via a Vector, which is copied into this object's internal storage
     *  structure.
     *
     * @param  strings                  Vector of allowed strings
     * @exception  ConstraintException  Thrown if the passed in vector size is 0
     */
    public StringConstraint( Vector strings ) throws ConstraintException {
        if ( strings.size() > 0 ) this.strings = strings;
        else {
            String S = C + ": Constructor(Vector strings): ";
            throw new ConstraintException( S + "Input vector of constraint values cannot be empty" );
        }
    }


    /**
     *  Sets all allowed strings via a Vector, which is copied into this
     *  object's internal storage structure.
     *
     * @param  strings                  Vector of allowed strings
     * @exception  ConstraintException  Thrown if the passed in vector size is 0
     */
    public void setStrings( Vector strings ) throws ConstraintException, EditableException {

        String S = C + ": setStrings(): ";
        checkEditable(S);
        if ( ( strings != null ) && ( strings.size() > 0 ) ) this.strings = strings;
        else throw new ConstraintException( S + "Input vector of constraint values cannot be null or empty" );

    }



    /**
     *  Returns a cloned Vector of the allowed Strings.
     *
     * @return    The vector clone of allowed values
     */
    public Vector getAllowedStrings() { return ( Vector ) strings.clone(); }


    /**
     *  Returns a cloned Vector of the allowed Strings.
     *
     * @return    The vector clone of allowed values
     */
    public Vector getAllowedValues() { return getAllowedStrings(); }


    /**
     *  Determine if the new value being set is allowed.
     *
     * @param  obj  Object to check if allowed String
     * @return      True if the value is allowed
     */
    public boolean isAllowed( Object obj ) {

        if( nullAllowed && ( obj == null ) ) return true;
        else if ( !( obj instanceof String ) ) return false;
        else if ( !containsString( obj.toString() ) ) return false;
        else return true;
    }


    /**
     *  Returns Iterator over allowed values.
     *
     * @return    The list iterator over allowed values list
     */
    public ListIterator listIterator() { return strings.listIterator(); }


    /**
     *  Adds a String to the list of allowed values.
     *
     * @param  str  The new allowed value
     */
    public void addString( String str ) throws EditableException {

        String S = C + ": addString(): ";
        checkEditable(S);
        if ( !containsString( str ) ) strings.add( str );
    }


    /**
     *  Removes a String from the list of allowed values.
     *
     * @param  str  The allowed value to remove
     */
    public void removeString( String str ) throws EditableException {

        String S = C + ": removeString(): ";
        checkEditable(S);
        if ( containsString( str ) ) strings.remove( str );

    }


    /**
     *  Checks that a String is in the list of allowed values.
     *
     * @param  str  The string to check if it is an allowed value
     * @return      True if the string is in the allowed list, false otherwise
     */
    public boolean containsString( String str ) {
        if ( strings.contains( str ) ) return true;
        else return false;
    }



    /**
     *  Returns number of allowed values.
     *
     * @return    int size of the allowed values list
     */
    public int size() { return strings.size(); }


    /**
     *  Prints out the current state of this parameter, i.e. classname and
     *  allowed values. Useful for debugging.
     *
     * @return    Formatted String of object's current state.
     */
    public String toString() {
        String TAB = "    ";
        StringBuffer b = new StringBuffer();
        b.append( C );

        if( name != null) b.append( TAB + "Name = " + name + '\n' );
        //b.append( TAB + "Is Editable = " + this.editable + '\n' );
        b.append( TAB + "Null Allowed = " + this.nullAllowed+ '\n' );
        b.append( TAB + "Allowed values = " );

        boolean first = true;
        ListIterator it = strings.listIterator();
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
     *  Returns a copy so you can't edit or damage the origial.
     *
     * @return    Exact copy of this object's state
     */
    public Object clone() {

        StringConstraint c1 = new StringConstraint();
        c1.name = name;
        Vector v = getAllowedStrings();
        ListIterator it = v.listIterator();
        while ( it.hasNext() ) {
            String val = ( String ) it.next();
            c1.addString( val );
        }

        c1.setNullAllowed( nullAllowed );
        if( !editable ) c1.setNonEditable();
        return c1;
    }
}
