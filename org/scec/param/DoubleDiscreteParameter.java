package org.scec.param;

import java.util.Vector;
import org.scec.exceptions.*;

/**
 *  <b>Title:</b> DoubleDiscreteParameter<p>
 *  <b>Description:</b> Same as a DoubleParameter except the Constraints are a
 *  list of valid Double choices instead of a Min/Max DoubleConstraint with all
 *  possible values allowed in between.<p>
 *
 *
 * @author     Steven W. Rock
 * @created    February 20, 2002
 * @version    1.0
 */

public class DoubleDiscreteParameter
    extends DependentParameter
    implements DependentParameterAPI, ParameterAPI
{
    /**
     *  Class name - used for debugging
     */
    protected final static String C = "DoubleDiscreteParameter";
    /**
     *  If true, debugging statements printed
     */
    protected final static boolean D = false;


    /**
     *  Constructor for the DoubleDiscreteParameter object. No constraints
     *  specified, all values allowed.
     *
     * @param  name  Name of this parameter
     */
    public DoubleDiscreteParameter( String name ) {
        super( name, null, null, null );
    }


    /**
     *  Constructor for the DoubleDiscreteParameter object No constraints
     *  specified, all values allowed.
     *
     * @param  name   Name of this parameter
     * @param  units  Units string for this parameter
     */
    public DoubleDiscreteParameter( String name, String units ) {
        super( name, null, units, null );
    }


    /**
     *  Constructor for the DoubleDiscreteParameter object
     *
     * @param  name                     Name of this parameter
     * @param  doubles                  List of allowed doubles
     * @exception  ConstraintException  Description of the Exception
     */
    public DoubleDiscreteParameter( String name, Vector doubles ) throws ConstraintException {
        super( name, new DoubleDiscreteConstraint( doubles ), null, null );
        //if( constraint != null ) constraint.setName( name );
    }


    /**
     *  Constructor for the DoubleDiscreteParameter object
     *
     * @param  name                     Name of this parameter
     * @param  doubles                  List of allowed doubles
     * @param  units                    Units string for this parameter
     * @exception  ConstraintException  Description of the Exception
     */
    public DoubleDiscreteParameter( String name, Vector doubles, String units ) throws ConstraintException {
        super( name, new DoubleDiscreteConstraint( doubles ), units, null );
        //if( constraint != null ) constraint.setName( name );
    }


    /**
     *  Constructor for the DoubleDiscreteParameter object
     *
     * @param  name                     Name of this parameter
     * @param  constraint               List of allowed doubles
     * @exception  ConstraintException  Description of the Exception
     */
    public DoubleDiscreteParameter( String name, DoubleDiscreteConstraint constraint ) throws ConstraintException {
        super( name, constraint, null, null );
        //if( (constraint != null) && (constraint.getName() == null) )
            //constraint.setName( name );

    }


    /**
     *  Constructor for the DoubleDiscreteParameter object
     *
     * @param  name                     Name of this parameter
     * @param  constraint               List of allowed doubles
     * @param  units                    Units string for this parameter
     * @exception  ConstraintException  Description of the Exception
     */
    public DoubleDiscreteParameter( String name, DoubleDiscreteConstraint constraint, String units ) throws ConstraintException {
        super( name, constraint, units, null );
        //if( (constraint != null) && (constraint.getName() == null) )
            //constraint.setName( name );
    }


    /**
     *  No constraints specified, all values allowed *
     *
     * @param  name   Name for this parameter
     * @param  value  The value to set this parameter to
     */
    public DoubleDiscreteParameter( String name, Double value ) {
        super(name, null, null, value);
    }


    /**
     *  Constructor for the DoubleDiscreteParameter object
     *
     * @param  name                     Name for this parameter
     * @param  units                    Units for this parameter
     * @param  value                    The value to set this parameter to
     * @exception  ConstraintException  Thrown if value not allowed
     */
    public DoubleDiscreteParameter( String name, String units, Double value ) throws ConstraintException {
        super( name, null, units, value );
    }


    /**
     *  Constructor for the DoubleDiscreteParameter object
     *
     * @param  name                     Name for this parameter
     * @param  value                    The value to set this parameter to
     * @param  doubles                  list of allowed values
     * @exception  ConstraintException  Thrown if value not allowed
     */
    public DoubleDiscreteParameter( String name, Vector doubles, Double value ) throws ConstraintException {
        super( name, new DoubleDiscreteConstraint( doubles ), null, value );
        //if( constraint != null ) constraint.setName( name );
    }


    /**
     *  Constructor for the DoubleDiscreteParameter object
     *
     * @param  name                     Name for this parameter
     * @param  units                    Units for this parameter
     * @param  value                    The value to set this parameter to
     * @param  doubles                  list of allowed values
     * @exception  ConstraintException  Thrown if value not allowed
     */
    public DoubleDiscreteParameter( String name, Vector doubles, String units, Double value ) throws ConstraintException {
        super( name, new DoubleDiscreteConstraint( doubles ), units, value );
        //if( constraint != null ) constraint.setName( name );
    }


    /**
     *  Constructor for the DoubleDiscreteParameter object
     *
     * @param  name                     Name for this parameter
     * @param  constraint               List of allowed values
     * @param  value                    The value to set this parameter to
     * @exception  ConstraintException  Thrown if value not allowed
     */
    public DoubleDiscreteParameter( String name, DoubleDiscreteConstraint constraint, Double value ) throws ConstraintException {
        super( name, constraint, null, value );
        //if( (constraint != null) && (constraint.getName() == null) )
            //constraint.setName( name );
    }


    /**
     *  This is the main constructor. All other constructors call this one.
     *  Constraints must be set first, because the value may not be an allowed
     *  one. Null values are always allowed in the constructor
     *
     * @param  name                     Name for this parameter
     * @param  constraint               List of allowed values
     * @param  units                    Units for this parameter
     * @param  value                    The value to set this parameter to
     * @exception  ConstraintException  Thrown if value not allowed
     */
    public DoubleDiscreteParameter(
            String name,
            DoubleDiscreteConstraint constraint,
            String units,
            Double value
             ) throws ConstraintException {
        super( name, constraint, units, value );
        //if( (constraint != null) && (constraint.getName() == null) )
            //constraint.setName( name );
    }


    /**
     *  Sets the constraint if it is a DoubleDiscreteConstraint and the parameter
     *  is currently editable.
     */
    public void setConstraint(ParameterConstraintAPI constraint)
        throws ParameterException, EditableException
    {

        String S = C + ": setConstraint( ): ";
        checkEditable(S);

        if ( !(constraint instanceof DoubleDiscreteConstraint )) {
            throw new ParameterException( S +
                "This parameter only accepts DoubleDiscreteConstraints, unable to set the constraint."
            );
        }
        else super.setConstraint( constraint );

        //if( (constraint != null) && (constraint.getName() == null) )
            //constraint.setName( name );

    }

    /**
     *  Gets the type attribute of the DoubleDiscreteParameter object. This is
     *  used to determine which gui editor to use for this parameter
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
     *  Returns a clone of all allowed values.
     *
     * @return    List of allowed values
     */
    public Vector getAllowedDoubles() {
        return ( ( DoubleDiscreteConstraint ) this.constraint ).getAllowedDoubles();
    }


    /**
     *  Compares the parameter values to see if they are the same
     *
     * @param  obj                     Double Parameter to compare to
     * @return                         -1 if this < obj, 0 if this = obj, +1 if
     *      this > obj
     * @exception  ClassCastException  Thrown if passed in value is not a
     *      DoubleParameter
     */
    public int compareTo( Object obj ) throws ClassCastException {

        String S = C + ":compareTo(): ";

        if ( !( obj instanceof DoubleParameter ) && !( obj instanceof DoubleDiscreteParameter ) ) {
            throw new ClassCastException( S + "Object not a DoubleParameter, or DoubleDiscreteParameter, unable to compare" );
        }

        int result = 0;

        Double n1 = ( Double ) this.getValue();
        Double n2 = null;

        if ( obj instanceof DoubleParameter ) {
            DoubleParameter param = ( DoubleParameter ) obj;
            n2 = ( Double ) param.getValue();
        } else if ( obj instanceof DoubleDiscreteParameter ) {
            DoubleDiscreteParameter param = ( DoubleDiscreteParameter ) obj;
            n2 = ( Double ) param.getValue();
        }

        return n1.compareTo( n2 );
    }


    /**
     *  Compares value to see if equal
     *
     * @param  obj                     Double Parameter to compare to
     * @return                         true if these two parameter values are
     *      the same
     * @exception  ClassCastException  Thrown if passed in value is not a
     *      DoubleParameter
     */
    public boolean equals( Object obj ) throws ClassCastException {
        String S = C + ":equals(): ";

        if ( !( obj instanceof DoubleParameter ) && !( obj instanceof DoubleDiscreteParameter ) ) {
            throw new ClassCastException( S + "Object not a DoubleParameter, or DoubleDiscreteParameter, unable to compare" );
        }

        String otherName = ( ( ParameterAPI ) obj ).getName();
        if ( ( compareTo( obj ) == 0 ) && getName().equals( otherName ) ) {
            return true;
        } else {
            return false;
        }
    }


    /**
     *  Returns a copy so you can't edit or damage the origial
     *
     * @return    Exact copy of this object.
     */
    public Object clone() {
        DoubleDiscreteConstraint c1 = ( DoubleDiscreteConstraint ) constraint.clone();

        DoubleDiscreteParameter param = null;
        if( value == null ) param = new DoubleDiscreteParameter( name, c1, units);
        else param = new DoubleDiscreteParameter( name, c1, units, new Double( this.value.toString() )  );

        if( !editable ) param.setNonEditable();
        return param;
    }

}
