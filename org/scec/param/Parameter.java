package org.scec.param;

import org.scec.exceptions.ConstraintException;
import org.scec.exceptions.ParameterException;
import org.scec.exceptions.EditableException;

/**
 *  <b>Title: </b> Parameter<p>
 *
 *  <b>Description: </b> Partial (abstract)  base implementation for ParameterAPI of common
 *  functionality accross all parameter subclasses. The common fields with get
 *  and setters are here, as well as a default constructor that sets all these
 *  fields, and the setValue field that always checks if the value is allowed
 *  before setting. The fields with gettesr and setters are:
 *
 * <ul>
 * <li>name
 * <li>units
 * <li>constraint
 * <li>editable
 * <li>value
 * </ul>
 *
 * These fields are common to all parameters. <p>
 *
 *  <b>Change History:</b> 11/29/2001 - SWR - Added String units field with get
 *  and set methods<p>
 *
 * @author     Steve W. Rock
 * @created    February 21, 2002
 * @see        ParameterAPI
 * @version    1.0
 */

/*
 *  public class Parameter implements ParameterAPI, NamedObjectAPI
 */
public abstract class Parameter
    implements
        ParameterAPI
{

    /** Class name for debugging. */
    protected final static String C = "Parameter";
    /** If true print out debug statements. */
    protected final static boolean D = false;


    /** Name of the parameter. */
    protected String name = "";

    /**
     *  Information about this parameter. This is usually used to describe what
     *  this object represents. May be used in gui tooltips.
     */
    protected String info = "";

    /**
     *  The units of this parameter.
     */
    protected String units = "";

    /**
     *  THe constraint for this Parameter.
     */
    protected ParameterConstraintAPI constraint = null;

    /**
     *  This value indicates if the value is editable after it is first set.
     */
    protected boolean editable = true;

    /**
     *  The value object of this Parameter, subclasses will define the object
     *  type.
     */
    protected Object value = null;



    /** Empty no-arg constructor. Does nothing but initialize object.  */
    public Parameter() { }

    /**
     *  Every parameter constraint has a name, this function sets that name.
     *  Defaults to the name of the parameter
     *
     * @return    The name value
     */
    public String getConstraintName(  ){
        if( constraint != null ) {
            String name = constraint.getName( );
            if( name == null ) return "";
            return name;
        }
        return "";
    }

    /** Proxy function call to the constraint to see if null values are permitted */
    public boolean isNullAllowed(){
        if( constraint != null ) {
            return constraint.isNullAllowed();
        }
        else return true;
    }

    /**
     * If the editable boolean is set to true, the parameter value can
     * be edited, else an EditableException is thrown.
     */
    protected void checkEditable(String S) throws EditableException{
        if( !this.editable ) throw new EditableException( S +
            "This parameter is currently not editable"
        );
    }

    /**
     *  This is the main constructor. All subclass constructors call this one.
     *  Constraints must be set first, because the value may not be an allowed
     *  one. Null values are always allowed in the constructor.
     *
     * @param  name                     Name of this parameter
     * @param  constraint               Constraints for this Parameter. May be
     *      set to null
     * @param  units                    The units for this parameter
     * @param  value                    The value object of this parameter.
     * @exception  ConstraintException  Description of the Exception
     * @throws  ConstraintException     This is thrown if the passes in
     *      parameter is not allowed
     */
    public Parameter( String name, ParameterConstraintAPI constraint, String units, Object value )
             throws ConstraintException {

        String S = C + ": Constructor(): ";
        if ( D ) System.out.println( S + "Starting" );

        if ( value != null && constraint != null ) {
            if ( !constraint.isAllowed( value ) ) {
                System.out.println( S + "Value not allowed" );
                throw new ConstraintException( S + "Value not allowed" );
            }
        }

        this.constraint = constraint;
        this.name = name;
        this.value = value;
        this.units = units;

        //if( (constraint != null) && (constraint.getName() == null) ) constraint.setName( name );

        if ( D ) System.out.println( S + "Ending" );

    }


    /**
     *  Set's the parameter's value.
     *
     * @param  value                 The new value for this Parameter
     * @throws  ParameterException   Thrown if the object is currenlty not
     *      editable
     * @throws  ConstraintException  Thrown if the object value is not allowed
     */
    public void setValue( Object value ) throws ConstraintException, ParameterException {
        String S = C + ": setValue(): ";

        if ( !isAllowed( value ) ) {
            throw new ConstraintException( S + "Value is not allowed: " + value.toString() );
        }

        this.value = value;
    }


    /**
     *  Sets the info attribute of the Parameter object. This is usually used to
     *  describe what this object represents. May be used in gui tooltips.
     *
     * @param  info  The new info value
     */
    public void setInfo( String info ) throws EditableException{

        checkEditable(C + ": setInfo(): ");
        this.info = info;
    }

    /**
     * Sets the units of this parameter
     * @param units
     */
    public void setUnits(String units) throws EditableException {
        checkEditable(C + ": setUnits(): ");
        this.units = units;
    }



    /**
     *  Disables editing units, info, constraints, et. Basically all set()s disabled
     *  except for setValue(). Once set non-editable, it cannot be set back.
     */
    public void setNonEditable() {
        editable = false;
    }



    /**
     *  Returns the parameter's value. Each subclass defines what type of
     *  object. it returns
     *
     * @return    The value value
     */
    public Object getValue() {
        return value;
    }


    /**
     *  Every parameter has a name, this function returns that name.
     *
     * @return    The name value
     */
    public String getName() {
        return name;
    }


    /**
     *  Every parameter has a name, this function returns that name.
     *
     * @return    The name value
     */
    public void setName(String name){
        checkEditable(C + ": setName(): ");
        this.name = name;
    }


    /**
     *  Returns the units of this parameter, represented as a String.
     *
     * @return    The units value
     */
    public String getUnits() {
        return units;
    }

    /**
     *  Gets the constraints of this parameter.
     *
     * @return    The constraint value
     */
    public ParameterConstraintAPI getConstraint() {
        return constraint;
    }

    /**
     *  Gets the constraints of this parameter. Each subclass may implement any
     *  type of constraint it likes.
     *
     * @return    The constraint value
     */
    public void setConstraint(ParameterConstraintAPI constraint) throws EditableException{
        checkEditable(C + ": setConstraint(): ");
        this.constraint = constraint;
    }



    /**
     *  Returns a description of this Parameter, typically used for tooltips.
     *
     * @return    The info value
     */
    public String getInfo() {
        return info;
    }


    /**
     *  Returns the short class name of this object.
     *
     * @return    The type value
     */
    public String getType() {
        return C;
    }


    /**
     *  Determines if the value can be edited, i.e. changed once set.
     *
     * @return    The editable value
     */
    public boolean isEditable() {
        return editable;
    }


    /**
     *  Uses the constraint object to determine if the new value being set is
     *  allowed. If no Constraints are present all values are allowed. This
     *  function is now available to all subclasses, since any type of
     *  constraint object follows the same api.
     *
     * @param  obj  Object to check if allowed via constraints
     * @return      True if the value is allowed
     */
    public boolean isAllowed( Object obj ) {
        if ( constraint != null ) return constraint.isAllowed( obj );
         else return true;

    }



    /**
     *  Prints out the current state of this parameter, useful for debugging.
     *
     * @return    Formatted String of object's current state.
     */
    public String toString() {
        String TAB = "    ";
        StringBuffer b = new StringBuffer();
        b.append( C );
        if ( name != null ) b.append( TAB + "Name = " + name + '\n' );
        if ( value != null ) b.append( TAB + "Value = " + value.toString() + '\n' );
        if ( constraint != null ) b.append( TAB + "Constraint = " + constraint.toString() + '\n' );
        return b.toString();
    }


    /**
     *  Returns a copy so you can't edit or damage the origial.
     *
     * @return    Description of the Return Value
     */
    public abstract Object clone();

}
