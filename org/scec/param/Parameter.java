package org.scec.param;

import java.util.Vector;

import org.scec.exceptions.ConstraintException;
import org.scec.exceptions.ParameterException;
import org.scec.exceptions.EditableException;
import org.scec.param.event.*;

/**
 * <b>Title: </b> Parameter<p>
 *
 * <b>Description: </b> Partial (abstract)  base implementation for
 * ParameterAPI of common functionality accross all parameter subclasses.
 * The common fields with get and setters are here, as well as a default
 * constructor that sets all these fields, and the setValue field that
 * always checks if the value is allowed before setting. The fields
 * with gettesr and setters are:
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
 * @author     Steve W. Rock
 * @created    February 21, 2002
 * @see        ParameterAPI
 * @version    1.0
 */
public abstract class Parameter
    implements
        ParameterAPI, java.io.Serializable
{

    /** Class name used for debug statements and building the parameter type for getType(). */
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

    /** The units of this parameter represented as a String */
    protected String units = "";

    /** The constraint for this Parameter. This is consulted when setting values */
    protected ParameterConstraintAPI constraint = null;

    /**
     * This value indicates if fields and constraint in this
     * parameter are editable  after it is first initialized.
     */
    protected boolean editable = true;

    /** The value object of this Parameter, subclasses will define the object type. */
    protected Object value = null;

    /**
     *  Vector of all the objects who want to listen on change of this paramter
     */
    private transient Vector changeListeners;

    /**
     * Vector of all the objects who want to listen if the value
     * for this paramter is not valid
     */
    private transient Vector failListeners;


    /** Empty no-arg constructor. Does nothing but initialize object.  */
    public Parameter() { }



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
     * @throws  ConstraintException     This is thrown if the passes in
     *      parameter is not allowed.
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
     *  Set's the parameter's value.
     *
     * @param  value                 The new value for this Parameter.
     * @throws  ParameterException   Thrown if the object is currenlty not
     *      editable.
     * @throws  ConstraintException  Thrown if the object value is not allowed.
     */
    public void setValue( Object value ) throws ConstraintException, ParameterException {
        String S = C + ": setValue(): ";

        if ( !isAllowed( value ) ) {
            throw new ConstraintException( S + "Value is not allowed: " + value.toString() );
        }

        this.value = value;

        org.scec.param.event.ParameterChangeEvent event = new org.scec.param.event.ParameterChangeEvent(
                        this, getName(),
                        getValue(), value
                    );

        firePropertyChange( event );
    }

    /**
      *  Needs to be called by subclasses when field change fails
      *  due to constraint problems.
      */
     public void unableToSetValue( Object value ) throws ConstraintException {

       String S = C + ": unableToSetValue():";
       org.scec.param.event.ParameterChangeFailEvent event =
           new org.scec.param.event.ParameterChangeFailEvent(this,
                         getName(), getValue(), value);

        firePropertyChangeFailed( event );

     }


    /**
     *  Adds a feature to the ParameterChangeFailListener attribute of the
     *  ParameterEditor object
     *
     * @param  listener  The feature to be added to the
     *      ParameterChangeFailListener attribute
     */
    public synchronized void addParameterChangeFailListener( org.scec.param.event.ParameterChangeFailListener listener ) {
        if ( failListeners == null ) failListeners = new Vector();
        if ( !failListeners.contains( listener ) ) failListeners.addElement( listener );
     }

    /**
     * Every parameter constraint has a name, this function gets that name.
     * Defaults to the name of the parameter but in a few cases may
     * be different.
     */
     public String getConstraintName(  ){
        if( constraint != null ) {
            String name = constraint.getName( );
            if( name == null ) return "";
            return name;
        }
        return "";
    }

    /**
     *  Description of the Method
     *
     * @param  listener  Description of the Parameter
     */
    public synchronized void removeParameterChangeFailListener( org.scec.param.event.ParameterChangeFailListener listener ) {
        if ( failListeners != null && failListeners.contains( listener ) )
            failListeners.removeElement( listener );
    }


    /**
     *  Description of the Method
     *
     * @param  event  Description of the Parameter
     */
    public void firePropertyChangeFailed( org.scec.param.event.ParameterChangeFailEvent event ) {

        String S = C + ": firePropertyChange(): ";
        if ( D ) System.out.println( S + "Firing failed change event for parameter = " + event.getParameterName() );
        if ( D ) System.out.println( S + "Old Value = " + event.getOldValue() );
        if ( D ) System.out.println( S + "Bad Value = " + event.getBadValue() );
        if ( D ) System.out.println( S + "Model Value = " + event.getSource().toString() );

        Vector vector;
        synchronized ( this ) {
            if ( failListeners == null ) return;
            vector = ( Vector ) failListeners.clone();
        }

        for ( int i = 0; i < vector.size(); i++ ) {
            org.scec.param.event.ParameterChangeFailListener listener = ( org.scec.param.event.ParameterChangeFailListener ) vector.elementAt( i );
            listener.parameterChangeFailed( event );
        }
    }



    /**
     *  Adds a feature to the ParameterChangeListener attribute of the
     *  ParameterEditor object
     *
     * @param  listener  The feature to be added to the ParameterChangeListener
     *      attribute
     *
     */

    public synchronized void addParameterChangeListener( org.scec.param.event.ParameterChangeListener listener ) {
        if ( changeListeners == null ) changeListeners = new Vector();
        if ( !changeListeners.contains( listener ) ) changeListeners.addElement( listener );
    }

    /**
     *  Description of the Method
     *
     * @param  listener  Description of the Parameter
     */
    public synchronized void removeParameterChangeListener( org.scec.param.event.ParameterChangeListener listener ) {
        if ( changeListeners != null && changeListeners.contains( listener ) )
            changeListeners.removeElement( listener );
    }




    /**

     *  Description of the Method
     *
     * @param  event  Description of the Parameter

     *  Every parameter constraint has a name, this function gets that name.
     *  Defaults to the name of the parameter but in a few cases may
     * be different.
*/
    public void firePropertyChange( ParameterChangeEvent event ) {

        String S = C + ": firePropertyChange(): ";
        if ( D ) System.out.println( S + "Firing change event for parameter = " + event.getParameterName() );
        if ( D ) System.out.println( S + "Old Value = " + event.getOldValue() );
        if ( D ) System.out.println( S + "New Value = " + event.getNewValue() );
        if ( D ) System.out.println( S + "Model Value = " + event.getSource().toString() );

        Vector vector;
        synchronized ( this ) {
            if ( changeListeners == null ) return;
            vector = ( Vector ) changeListeners.clone();
        }

        for ( int i = 0; i < vector.size(); i++ ) {
            org.scec.param.event.ParameterChangeListener listener = ( org.scec.param.event.ParameterChangeListener ) vector.elementAt( i );
            listener.parameterChange( event );
        }
    }

  /** Proxy function call to the constraint to see if null values are permitted */
    public boolean isNullAllowed(){
        if( constraint != null ) {
            return constraint.isNullAllowed();
        }
        else return true;
    }



    /**
     * Sets the info string of the Parameter object if editable. This is
     * usually used to describe what this object represents. May be used
     * in gui tooltips.
     */
    public void setInfo( String info ) throws EditableException{

        checkEditable(C + ": setInfo(): ");
        this.info = info;
    }



    /** Sets the units string of this parameter. Can be used in tooltips, etc.  */
    public void setUnits(String units) throws EditableException {
        checkEditable(C + ": setUnits(): ");
        this.units = units;
    }


    /** Returns the parameter's value. Each subclass defines what type of object it returns. */
    public Object getValue()  { return value;}

    /** Returns the units of this parameter, represented as a String. */
    public String getUnits() { return units; }

    /** Gets the constraints of this parameter. */
    public ParameterConstraintAPI getConstraint() { return constraint; }


    /**
     *  Sets the constraints of this parameter. Each subclass may implement any
     *  type of constraint it likes. An EditableException is thrown if this parameter
     *  is currently uneditable.
     *
     * @return    The constraint value
     */
    public void setConstraint(ParameterConstraintAPI constraint) throws EditableException{
        checkEditable(C + ": setConstraint(): ");
        //setting the new constraint for the parameter
        this.constraint = constraint;

        //getting the existing value for the Parameter
        Object value = getValue();

        /**
         * Check to see if the existing value of the parameter is within the
         * new constraint of the parameter, if so then leave the value of the parameter
         * as it is currently else if the value is outside the constraint then
         * give the parameter a temporaray null value, which can be changed later by the user.
         */
        if(!constraint.isAllowed(value)){

        /*allowing the constraint to have null values.This has to be done becuase
        if the previous value for the parameter is not within the constraints then it will
        throw the exception: "Value not allowed". so we have have allow "null" in the parameters.*/
        constraint.setNullAllowed(true);


        //now set the current param value to be null.
        /*null is just a new temp value of the parameter, which can be changed by setting
          a value in the parameter that is compatible with the parameter constraints.*/
        this.setValue(null);
        constraint.setNullAllowed(false);
        }
    }

    /** Returns a description of this Parameter, typically used for tooltips. */
    public String getInfo() { return info; }


    /**
     *  Uses the constraint object to determine if the new value being set is
     *  allowed. If no Constraints are present all values are allowed. This
     *  function is now available to all subclasses, since any type of
     *  constraint object follows the same api.
     *  Disables editing units, info, constraints, et. Basically all set()s disabled
     *  except for setValue(). Once set non-editable, it cannot be set back.
     *  This is a one-time operation.
     */
    public void setNonEditable() { editable = false; }


    /** Every parameter has a name, this function returns that name. */
    public String getName() { return name; }

    /** Every parameter has a name, this function sets that name, if editable. */
    public void setName(String name){
        checkEditable(C + ": setName(): ");
        this.name = name;
    }

    /**
     * Returns the short class name of this object. Used by the editor framework to
     * dynamically assign an editor to subclasses. If there are constraints
     * present, typically "Constrained" is prepended to the short class name.
     */
    public String getType() { return C; }


    /** Determines if the value can be edited, i.e. changed once initialized. */
    public boolean isEditable() { return editable; }

    /** Returns a copy so you can't edit or damage the origial. */
    public abstract Object clone();


}
