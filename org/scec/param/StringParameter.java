package org.scec.param;

import java.util.*;

import org.scec.exceptions.*;

/**
 *  <b>Title:</b> StringParameter<br>
 *  <b>Description:</b> Generic Data Object that contains a String and
 *  optionally a vector of allowed values stored in a constraint object. If no
 *  constraint object is present then all values are permitted.<br>
 *  <b>Note:</b> We are unable to have a constructor with signature:
 *  <ul>
 *    <li> <code>public StringParameter(String name, String units)</code>
 *  </ul>
 *  because we already have a constructor with the identical signature
 *  <ul>
 *    <li> <code>public StringParameter(String name, String value)</code><br>
 *
 *  </ul>
 *  <b>Copyright:</b> Copyright (c) 2001<br>
 *  <b>Company:</b> <br>
 *
 *
 * @author     Sid Hellman
 * @created    February 21, 2002
 * @version    1.0
 */

public class StringParameter
    extends DependentParameter
    implements DependentParameterAPI, ParameterAPI
{

    /**
     *  Class name for debugging.
     */
    protected final static String C = "StringParameter";
    /**
     *  If true print out debug statements.
     */
    protected final static boolean D = false;


    /**
     *  No constraints specified, all values allowed.
     *
     * @param  name  The name of this parameter
     */
    public StringParameter( String name ) {
        this.name = name;
    }


    /**
     *  Input vector is turned into StringConstraints object. If vector contains
     *  no elements an exception is thrown.
     *
     * @param  name                     Name of the parametet
     * @param  strings                  Converted to the Constraint object
     * @exception  ConstraintException  Thrown if vector of allowed values is
     *      empty
     * @throws  ConstraintException     Thrown if vector of allowed values is
     *      empty
     */
    public StringParameter( String name, Vector strings ) throws ConstraintException {
        this( name, new StringConstraint( strings ), null, null );
    }


    /**
     *  Sets the name and Constraint.
     *
     * @param  name                     Name of the parametet
     * @param  constraint               Constraint object
     * @exception  ConstraintException  Description of the Exception
     * @throws  ConstraintException     Is thrown if the value is not allowed
     */
    public StringParameter( String name, StringConstraint constraint ) throws ConstraintException {
        this( name, constraint, null, null );
    }


    /**
     *  No constraints specified, all values allowed
     *
     * @param  name   Name of the parametet
     * @param  value  value of this parameter
     */
    public StringParameter( String name, String value ) {
        this.name = name;
        this.value = value;
    }


    /**
     *  No constraints specified, all values allowed. Sets the name, units and
     *  value.
     *
     * @param  name                     Name of the parameter
     * @param  units                    Units of the parameter
     * @param  value                    value of this parameter
     * @exception  ConstraintException  Description of the Exception
     */
    public StringParameter( String name, String units, String value ) throws ConstraintException {
        this( name, null, units, value );
    }


    /**
     *  Sets the name, vector of string converted to a constraint, amd value.
     *
     * @param  name                     Name of the parametet
     * @param  strings                  vector of allowed values converted to a
     *      constraint
     * @param  value                    value of this parameter
     * @exception  ConstraintException  Is thrown if the value is not allowed
     * @throws  ConstraintException     Is thrown if the value is not allowed
     */
    public StringParameter( String name, Vector strings, String value ) throws ConstraintException {
        this( name, new StringConstraint( strings ), null, value );
    }


    /**
     *  Sets the name, constraint, and value.
     *
     * @param  name                     Name of the parametet
     * @param  constraint               List of allowed values
     * @param  value                    value of this parameter
     * @exception  ConstraintException  Is thrown if the value is not allowed
     * @throws  ConstraintException     Is thrown if the value is not allowed
     */
    public StringParameter( String name, StringConstraint constraint, String value ) throws ConstraintException {
        this( name, constraint, null, value );
    }



    /**
     *  This is the main constructor. All other constructors call this one.
     *  Constraints must be set first, because the value may not be an allowed
     *  one. Null values are always allowed in the constructor. All values are
     *  set in this constructor; name, value, units, and constructor
     *
     * @param  name                     Name of the parametet
     * @param  constraint               Lsit of allowed values
     * @param  value                    value object of this parameter
     * @param  units                    Units of this parameter
     * @exception  ConstraintException  Is thrown if the value is not allowed
     * @throws  ConstraintException     Is thrown if the value is not allowed
     */
    public StringParameter( String name, StringConstraint constraint, String units, String value )
             throws ConstraintException {
        super( name, constraint, units, value );
    }

    /**
     *  Sets the constraint if it is a StringConstraint and the parameter
     *  is currently editable.
     */
    public void setConstraint(ParameterConstraintAPI constraint) throws ParameterException, EditableException{

        String S = C + ": setConstraint(): ";
        checkEditable(S);

        if ( !(constraint instanceof StringConstraint )) {
            throw new ParameterException( S +
                "This parameter only accepts StringConstraints, unable to set the constraint."
            );
        }
        else super.setConstraint( constraint );

    }

    /**
     *  Gets the type attribute of the StringParameter object
     *
     * @return    The type value
     */
    public String getType() {
        String type = C;
        // Modify if constrained
        ParameterConstraintAPI constraint = this.constraint;
        if (constraint != null) type = "Constrained" + type;
        return type;
    }


    /**
     *  Returns a clone of the allowed strings of the constraint. Useful for
     *  presenting in a picklist
     *
     * @return    The allowedStrings value
     */
    public Vector getAllowedStrings() {
        return ( ( StringConstraint ) this.constraint ).getAllowedStrings();
    }


    /**
     *  Compares the values to if this is less than, equal to, or greater than
     *  the comparing objects. Implementation of comparable interface.
     *
     * @param  obj                     The object to compare this to
     * @return                         -1 if this value < obj value, 0 if equal,
     *      +1 if this value > obj value
     * @exception  ClassCastException  Is thrown if the comparing object is not
     *      a StringParameter *
     * @see                            Comparable
     */
    public int compareTo( Object obj ) throws ClassCastException {

        String S = C + ":compareTo(): ";

        if ( !( obj instanceof StringParameter ) ) {
            throw new ClassCastException( S + "Object not a StringParameter, unable to compare" );
        }

        StringParameter param = ( StringParameter ) obj;

        if( ( this.value == null ) && ( param.value == null ) ) return 0;
        int result = 0;

        String n1 = ( String ) this.getValue();
        String n2 = ( String ) param.getValue();

        return n1.compareTo( n2 );
    }


    /**
     *  Compares value to see if equal.
     *
     * @param  obj                     The object to compare this to
     * @return                         True if the values are identical
     * @exception  ClassCastException  Is thrown if the comparing object is not
     *      a StringParameter
     */
    public boolean equals( Object obj ) throws ClassCastException {
        String S = C + ":equals(): ";

        if ( !( obj instanceof StringParameter ) ) {
            throw new ClassCastException( S + "Object not a StringParameter, unable to compare" );
        }

        String otherName = ( ( StringParameter ) obj ).getName();
        if ( ( compareTo( obj ) == 0 ) && getName().equals( otherName ) ) {
            return true;
        } else {
            return false;
        }
    }


    /**
     *  Returns a copy so you can't edit or damage the origial. Clones this
     *  objects values, and the constraint.
     *
     * @return    Description of the Return Value
     */
    public Object clone() {

        StringConstraint c1 = ( StringConstraint ) constraint.clone();

        StringParameter param = null;
        if( value == null ) param = new StringParameter( name, c1, units);
        else param = new StringParameter( name, c1, units, this.value.toString() );
        if( param == null ) return null;
        if( !editable ) param.setNonEditable();
        return param;

    }

}
