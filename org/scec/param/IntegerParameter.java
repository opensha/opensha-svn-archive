package org.scec.param;

import org.scec.exceptions.*;

/**
 *  <b>Title:</b> IntegerParameter<p>
 *
 *  <b>Description:</b> Integer Parameter that accepts Integers as it's values.
 * If constraints are present, setting the vlaue must pass the constraint
 * check. Since the parameter class in an ancestor, all the parameter's fields are
 * inherited. <p>
 *
 * The constraints are IntegerConstraint which means a min and max values are
 * stored in the constraint that setting the parameter's value cannnt exceed.
 * If no constraint object is present then all values are permitted. <p>
 *
 * @author     Sid Hellman, Steven W. Rock
 * @created    February 21, 2002
 * @version    1.0
 */

public class IntegerParameter
    extends DependentParameter
    implements DependentParameterAPI, ParameterAPI
{

    /** Class name for debugging. */
    protected final static String C = "IntegerParameter";
    /** If true print out debug statements. */
    protected final static boolean D = false;


    /**
     *  No constraints specified, all values allowed. Sets the name of this
     *  parameter.
     *
     * @param  name  Name of the parametet
     */
    public IntegerParameter( String name ) {
        super( name, null, null, null );
    }


    /**
     *  No constraints specified, all values allowed. Sets the name and untis of
     *  this parameter.
     *
     * @param  name                     Name of the parametet
     * @param  units                    Units of this parameter
     * @exception  ConstraintException  Description of the Exception
     * @throws  ConstraintException     Is thrown if the value is not allowed
     */
    public IntegerParameter( String name, String units ) throws ConstraintException {
        this( name, null, units, null );
    }


    /**
     *  Sets the name, defines the constraints min and max values. Creates the
     *  constraint object from these values.
     *
     * @param  name                     Name of the parametet
     * @param  min                      defines min of allowed values
     * @param  max                      defines max of allowed values
     * @exception  ConstraintException  thrown if the value is not allowed
     * @throws  ConstraintException     Is thrown if the value is not allowed
     */
    public IntegerParameter( String name, int min, int max ) throws ConstraintException {
        super( name, new IntegerConstraint( min, max ), null, null );
        //this.constraint.setName( name );
    }


    /**
     *  Sets the name, defines the constraints min and max values, and sets the
     *  units. Creates the constraint object from these values.
     *
     * @param  name                     Name of the parameter
     * @param  min                      defines min of allowed values
     * @param  max                      defines max of allowed values
     * @param  units                    Units of this parameter
     * @exception  ConstraintException  thrown if the value is not allowed
     * @throws  ConstraintException     Is thrown if the value is not allowed
     */
    public IntegerParameter( String name, int min, int max, String units ) throws ConstraintException {
        super( name, new IntegerConstraint( min, max ), null, null );
        //this.constraint.setName( name );
    }


    /**
     *  Sets the name, defines the constraints min and max values. Creates the
     *  constraint object from these values.
     *
     * @param  name                     Name of the parametet
     * @param  min                      defines min of allowed values
     * @param  max                      defines max of allowed values
     * @exception  ConstraintException  thrown if the value is not allowed
     * @throws  ConstraintException     Is thrown if the value is not allowed
     */
    public IntegerParameter( String name, Integer min, Integer max ) throws ConstraintException {
        super( name, new IntegerConstraint( min, max ), null, null );
        //this.constraint.setName( name );
    }


    /**
     *  Sets the name, defines the constraints min and max values, and sets the
     *  units. Creates the constraint object from these values.
     *
     * @param  name                     Name of the parameter
     * @param  min                      defines min of allowed values
     * @param  max                      defines max of allowed values
     * @param  units                    Units of this parameter
     * @exception  ConstraintException  thrown if the value is not allowed
     * @throws  ConstraintException     Is thrown if the value is not allowed
     */
    public IntegerParameter( String name, Integer min, Integer max, String units ) throws ConstraintException {
        super( name, new IntegerConstraint( min, max ), null, null );
        //this.constraint.setName( name );
    }


    /**
     *  Sets the name and Constraints object.
     *
     * @param  name                     Name of the parameter
     * @param  constraint               defines min and max range of allowed
     *      values
     * @exception  ConstraintException  thrown if the value is not allowed
     * @throws  ConstraintException     Is thrown if the value is not allowed
     */
    public IntegerParameter( String name, IntegerConstraint constraint ) throws ConstraintException {
        super( name, constraint, null, null );
        //if( this.constraint.getName() == null ) this.constraint.setName( this.name );
    }


    /**
     *  Sets the name, constraints, and sets the units.
     *
     * @param  name                     Name of the parameter
     * @param  constraint               defines min and max range of allowed
     *      values
     * @param  units                    Units of this parameter
     * @exception  ConstraintException  thrown if the value is not allowed
     * @throws  ConstraintException     Is thrown if the value is not
     *      allowedallowed one. Null values are always allowed in the
     *      constructors
     */
    public IntegerParameter( String name, IntegerConstraint constraint, String units ) throws ConstraintException {
        super( name, constraint, units, null );
        //if( this.constraint.getName() == null ) this.constraint.setName( this.name );
    }


    /**
     *  No constraints specified, all values allowed. Sets the name and value.
     *
     * @param  name   Name of the parameter
     * @param  value  Integer value of this parameter
     */
    public IntegerParameter( String name, Integer value ) {
        super(name, null, null, value);
    }


    /**
     *  Sets the name, units and value. All values allowed because constraints
     *  not set.
     *
     * @param  name                     Name of the parametet
     * @param  value                    Integer value of this parameter
     * @param  units                    Units of this parameter
     * @exception  ConstraintException  thrown if the value is not allowed
     * @throws  ConstraintException     Is thrown if the value is not allowed
     */
    public IntegerParameter( String name, String units, Integer value ) throws ConstraintException {
        super( name, null, units, value );
    }


    /**
     *  Sets the name, and value. Also defines the min and max from which the
     *  constraint is constructed.
     *
     * @param  name                     Name of the parameter
     * @param  value                    Integer value of this parameter
     * @param  min                      defines max of allowed values
     * @param  max                      defines min of allowed values
     * @exception  ConstraintException  thrown if the value is not allowed
     * @throws  ConstraintException     Is thrown if the value is not allowed
     */
    public IntegerParameter( String name, int min, int max, Integer value ) throws ConstraintException {
        super( name, new IntegerConstraint( min, max ), null, value );
        //if( this.constraint.getName() == null ) this.constraint.setName( this.name );
    }

    /**
     *  Sets the name, and value. Also defines the min and max from which the
     *  constraint is constructed.
     *
     * @param  name                     Name of the parameter
     * @param  value                    Integer value of this parameter
     * @param  min                      defines max of allowed values
     * @param  max                      defines min of allowed values
     * @exception  ConstraintException  thrown if the value is not allowed
     * @throws  ConstraintException     Is thrown if the value is not allowed
     */
    public IntegerParameter( String name, Integer min, Integer max, Integer value ) throws ConstraintException {
        super( name, new IntegerConstraint( min, max ), null, value );
        //if( this.constraint.getName() == null ) this.constraint.setName( this.name );
    }


    /**
     *  Sets the name, value and constraint. The value is checked if it is
     *  within constraints.
     *
     * @param  name                     Name of the parameter
     * @param  constraint               defines min and max range of allowed
     *      values
     * @param  value                    Integer value of this parameter
     * @exception  ConstraintException  thrown if the value is not allowed
     * @throws  ConstraintException     Is thrown if the value is not allowed
     */
    public IntegerParameter( String name, IntegerConstraint constraint, Integer value ) throws ConstraintException {
        super( name, constraint, null, value );
        //if( this.constraint.getName() == null ) this.constraint.setName( this.name );
    }


    /**
     *  Sets all values, and the constraint is created from the min and max
     *  values. The value is checked if it is within constraints.
     *
     * @param  name                     Name of the parameter
     * @param  value                    Integer value of this parameter
     * @param  min                      defines min of allowed values
     * @param  max                      defines max of allowed values
     * @param  units                    Units of this parameter
     * @exception  ConstraintException  Is thrown if the value is not allowed
     * @throws  ConstraintException     Is thrown if the value is not allowed
     */
    public IntegerParameter( String name, int min, int max, String units, Integer value ) throws ConstraintException {
        super( name, new IntegerConstraint( min, max ), units, value );
        //if( this.constraint.getName() == null ) this.constraint.setName( this.name );
    }

    /**
     *  Sets all values, and the constraint is created from the min and max
     *  values. The value is checked if it is within constraints.
     *
     * @param  name                     Name of the parameter
     * @param  value                    Integer value of this parameter
     * @param  min                      defines min of allowed values
     * @param  max                      defines max of allowed values
     * @param  units                    Units of this parameter
     * @exception  ConstraintException  Is thrown if the value is not allowed
     * @throws  ConstraintException     Is thrown if the value is not allowed
     */
    public IntegerParameter( String name, Integer min, Integer max, String units, Integer value ) throws ConstraintException {
        super( name, new IntegerConstraint( min, max ), units, value );
        //if( this.constraint.getName() == null ) this.constraint.setName( this.name );
    }


    /**
     *  This is the main constructor. All other constructors call this one.
     *  Constraints must be set first, because the value may not be an allowed
     *  one. Null values are always allowed in the constructor. If the
     *  constraints are null, all values are allowed.
     *
     * @param  name                     Name of the parameter
     * @param  constraint               defines min and max range of allowed
     *      values
     * @param  value                    Integer value of this parameter
     * @param  units                    Units of this parameter
     * @exception  ConstraintException  Is thrown if the value is not allowed
     * @throws  ConstraintException     Is thrown if the value is not allowed
     */
    public IntegerParameter( String name, IntegerConstraint constraint, String units, Integer value )
             throws ConstraintException {
        super( name, constraint, units, value );
        //if( this.constraint.getName() == null ) this.constraint.setName( this.name );
    }

    /**
     *  Sets the constraint if it is a StringConstraint and the parameter
     *  is currently editable.
     */
    public void setConstraint(ParameterConstraintAPI constraint) throws ParameterException{

        String S = C + ": setConstraint(): ";
        checkEditable(S);

        if ( !(constraint instanceof IntegerConstraint )) {
            throw new ParameterException( S +
                "This parameter only accepts IntegerConstraints, unable to set the constraint."
            );
        }
        else super.setConstraint( constraint );

    }

    /**
     *  Determine if the new value being set is allowed.
     *
     * @param  obj  Object to check if allowed via constraints
     * @return      True if the value is allowed
     */
    public boolean isAllowed( Integer i ){
        return isAllowed( (Object)i );
    }


    /**
     *  Determine if the new value being set is allowed.
     *
     * @param  obj  Object to check if allowed via constraints
     * @return      True if the value is allowed
     */
    public boolean isAllowed( int i ){
        return isAllowed( new Integer(i) );
    }



    /**
     *  Gets the min value of the constraint object.
     *
     * @return                The min value
     * @exception  Exception  Description of the Exception
     */
    public Integer getMin() throws Exception {
        if ( constraint != null )
            return ( ( IntegerConstraint ) constraint ).getMin() ;
        else return null;
    }


    /**
     *  Returns the maximum allowed values.
     *
     * @return    The max value
     */
    public Integer getMax() {
        if ( constraint != null )
            return ( ( IntegerConstraint ) constraint ).getMax();
        else return null;
    }


    /**
     *  Gets the type attribute of the IntegerParameter object.
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
     *  Compares the values to if this is less than, equal to, or greater than
     *  the comparing objects.
     *
     * @param  obj                     The object to compare this to
     * @return                         -1 if this value < obj value, 0 if equal,
     *      +1 if this value > obj value
     * @exception  ClassCastException  Is thrown if the comparing object is not
     *      a IntegerParameter.
     */
    public int compareTo( Object obj ) throws ClassCastException {

        String S = C + ":compareTo(): ";

        if ( !( obj instanceof IntegerParameter ) )
            throw new ClassCastException( S + "Object not a IntegerParameter, unable to compare" );


        IntegerParameter param = ( IntegerParameter ) obj;

        int result = 0;

        Integer n1 = ( Integer ) this.getValue();
        Integer n2 = ( Integer ) param.getValue();

        return n1.compareTo( n2 );
    }


    /**
     *  Compares value to see if equal.
     *
     * @param  obj                     The object to compare this to
     * @return                         True if the values are identical
     * @exception  ClassCastException  Is thrown if the comparing object is not
     *      a IntegerParameter.
     */
    public boolean equals( Object obj ) throws ClassCastException {

        String S = C + ":equals(): ";

        if ( !( obj instanceof IntegerParameter ) )
            throw new ClassCastException( S + "Object not a IntegerParameter, unable to compare" );

        String otherName = ( ( IntegerParameter ) obj ).getName();
        if ( ( compareTo( obj ) == 0 ) && getName().equals( otherName ) )
            return true;
         else return false;

    }


    /**
     *  Returns a copy so you can't edit or damage the origial.
     *
     * @return    Exact copy of this object's state
     */
    public Object clone() {
        IntegerConstraint c1 = ( IntegerConstraint ) constraint.clone();
        IntegerParameter param = null;
        if( value == null ) param = new IntegerParameter( name, c1, units);
        else param = new IntegerParameter( name, c1, units, new Integer( this.value.toString() )  );
        if( param == null ) return null;
        if( !editable ) param.setNonEditable();
        return param;
    }

}
