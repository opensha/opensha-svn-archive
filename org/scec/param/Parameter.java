package org.scec.param;

import java.util.Vector;

import org.scec.exceptions.ConstraintException;
import org.scec.exceptions.ParameterException;
import org.scec.exceptions.EditableException;
import org.scec.param.event.*;

/**
/** @merge workspace: Changes in the Workspace */
 *  <b>Title: </b> Parameter<p>
/** @merge repository: Changes from the Repository
 * <b>Title: </b> Parameter<p>
 *
 * <b>Description: </b> Partial (abstract)  base implementation for
 * ParameterAPI of common functionality accross all parameter subclasses.
 * The common fields with get and setters are here, as well as a default
 * constructor that sets all these fields, and the setValue field that
 * always checks if the value is allowed before setting. The fields
 * with gettesr and setters are:
*/
 *
/** @merge workspace: Changes in the Workspace */
 *  <b>Description: </b> Base implementation for ParameterAPI of common
 *  functionality accross all parameter subclasses. The common fields with get
 *  and setters are here, as well as a default constructor that sets all these
 *  fields, and the setValue field that always checks if the value is allowed
 *  before setting.<p>
/** @merge repository: Changes from the Repository
 * <ul>
 * <li>name
 * <li>units
 * <li>constraint
 * <li>editable
 * <li>value
 * </ul>
*/
 *
/** @merge workspace: Changes in the Workspace */
 *  <b>Change History:</b> 11/29/2001 - SWR - Added String units field with get
 *  and set methods<p>
 *
/** @merge repository: Changes from the Repository
 * These fields are common to all parameters. <p>
 *
*/
 * @author     Steve W. Rock
 * @created    February 21, 2002
 * @see        ParameterAPI
 * @version    1.0
 */
public abstract class Parameter
    implements
        ParameterAPI
{


/** @merge workspace: Changes in the Workspace */
    /**
     *  Class name for debugging.
     */
/** @merge repository: Changes from the Repository
    /** Class name for debugging. */
*/
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
    private Vector changeListeners;

    /**
     * Vector of all the objects who want to listen if the value
     * for this paramter is not valid
     */
    private Vector failListeners;


    /** Empty no-arg constructor. Does nothing but initialize object.  */
    public Parameter() { }

    /**
     *  Every parameter constraint has a name, this function gets that name.
     *  Defaults to the name of the parameter but in a few cases may
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

        org.scec.param.event.ParameterChangeEvent event = new org.scec.param.event.ParameterChangeEvent(
                        this, getName(),
                        getValue(), value
                    );

        firePropertyChange( event );
    }


        /** Name of the parameter. */
    protected String name = "";

    /**
/** @merge workspace: Changes in the Workspace */
      *  Needs to be called by subclasses when field change fails
      *  due to constraint problems
      *
      * @param  value                    Description of the Parameter
      * @exception  ConstraintException  Description of the Exception
      */
     public void unableToSetValue( Object value ) throws ConstraintException {
/** @merge repository: Changes from the Repository
     *  Information about this parameter. This is usually used to describe what
     *  this object represents. May be used in gui tooltips.
     */
    protected String info = "";
*/

/** @merge workspace: Changes in the Workspace */
       String S = C + ": unableToSetValue():";
       org.scec.param.event.ParameterChangeFailEvent event =
           new org.scec.param.event.ParameterChangeFailEvent(this,
                         getName(), getValue(), value);

        firePropertyChangeFailed( event );
/** @merge repository: Changes from the Repository
    /** The units of this parameter represented as a String */
    protected String units = "";

    /** The constraint for this Parameter. This is consulted when setting values */
    protected ParameterConstraintAPI constraint = null;

    /**
     * This value indicates if fields and constraint in this
     * parameter are editable  after it is first initialized.
     */
    protected boolean editable = true;
*/

/** @merge workspace: Changes in the Workspace */
     }
/** @merge repository: Changes from the Repository
    /** The value object of this Parameter, subclasses will define the object type. */
    protected Object value = null;
*/


/** @merge workspace: Changes in the Workspace */

/** @merge repository: Changes from the Repository
    /** Empty no-arg constructor. Does nothing but initialize object.  */
    public Parameter() { }

*/
    /**
/** @merge workspace: Changes in the Workspace */
     *  Adds a feature to the ParameterChangeFailListener attribute of the
     *  ParameterEditor object
     *
     * @param  listener  The feature to be added to the
     *      ParameterChangeFailListener attribute
/** @merge repository: Changes from the Repository
     *  Every parameter constraint has a name, this function gets that name.
     *  Defaults to the name of the parameter but in a few cases may
     * be different.
*/
     */
/** @merge workspace: Changes in the Workspace */
    public synchronized void addParameterChangeFailListener( org.scec.param.event.ParameterChangeFailListener listener ) {
        if ( failListeners == null ) failListeners = new Vector();
        if ( !failListeners.contains( listener ) ) failListeners.addElement( listener );
/** @merge repository: Changes from the Repository
    public String getConstraintName(  ){
        if( constraint != null ) {
            String name = constraint.getName( );
            if( name == null ) return "";
            return name;
        }
        return "";
*/
    }

/** @merge workspace: Changes in the Workspace */
    /**
     *  Description of the Method
     *
     * @param  listener  Description of the Parameter
     */
    public synchronized void removeParameterChangeFailListener( org.scec.param.event.ParameterChangeFailListener listener ) {
        if ( failListeners != null && failListeners.contains( listener ) )
            failListeners.removeElement( listener );
    }
/** @merge repository: Changes from the Repository
    /** Proxy function call to the constraint to see if null values are permitted */
    public boolean isNullAllowed(){
        if( constraint != null ) {
            return constraint.isNullAllowed();
        }
        else return true;
    }
*/

    /**
/** @merge workspace: Changes in the Workspace */
     *  Description of the Method
     *
     * @param  event  Description of the Parameter
/** @merge repository: Changes from the Repository
     * If the editable boolean is set to true, the parameter value can
     * be edited, else an EditableException is thrown.
*/
     */
/** @merge workspace: Changes in the Workspace */
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

/** @merge repository: Changes from the Repository
    protected void checkEditable(String S) throws EditableException{
        if( !this.editable ) throw new EditableException( S +
            "This parameter is currently not editable"
        );
*/
    }

        /** Name of the parameter. */
    protected String name = "";

/** @merge workspace: Changes in the Workspace */
/** @merge repository: Changes from the Repository
    /**
     *  Information about this parameter. This is usually used to describe what
     *  this object represents. May be used in gui tooltips.
     */
    protected String info = "";
*/

    /** The units of this parameter represented as a String */
    protected String units = "";

/** @merge workspace: Changes in the Workspace */

/** @merge repository: Changes from the Repository
    /** The constraint for this Parameter. This is consulted when setting values */
    protected ParameterConstraintAPI constraint = null;

*/
    /**
/** @merge workspace: Changes in the Workspace */
     *  Adds a feature to the ParameterChangeListener attribute of the
     *  ParameterEditor object
     *
     * @param  listener  The feature to be added to the ParameterChangeListener
     *      attribute
/** @merge repository: Changes from the Repository
     * This value indicates if fields and constraint in this
     * parameter are editable  after it is first initialized.
*/
     */
/** @merge workspace: Changes in the Workspace */
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
/** @merge repository: Changes from the Repository
    protected boolean editable = true;

    /** The value object of this Parameter, subclasses will define the object type. */
    protected Object value = null;

*/

/** @merge workspace: Changes in the Workspace */

/** @merge repository: Changes from the Repository
    /** Empty no-arg constructor. Does nothing but initialize object.  */
    public Parameter() { }

*/
    /**
/** @merge workspace: Changes in the Workspace */
     *  Description of the Method
     *
     * @param  event  Description of the Parameter
/** @merge repository: Changes from the Repository
     *  Every parameter constraint has a name, this function gets that name.
     *  Defaults to the name of the parameter but in a few cases may
     * be different.
*/
     */
/** @merge workspace: Changes in the Workspace */
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

/** @merge repository: Changes from the Repository
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
*/
    }

/** @merge workspace: Changes in the Workspace */
/** @merge repository: Changes from the Repository
    /**
     * If the editable boolean is set to true, the parameter value can
     * be edited, else an EditableException is thrown.
     */
    protected void checkEditable(String S) throws EditableException{
        if( !this.editable ) throw new EditableException( S +
            "This parameter is currently not editable"
        );
    }
*/

    /**
/** @merge workspace: Changes in the Workspace */
     *  Sets the info attribute of the Parameter object. This is usually used to
     *  describe what this object represents. May be used in gui tooltips.
     *
     * @param  info  The new info value
/** @merge repository: Changes from the Repository
     * Sets the info attribute of the Parameter object if editable. This is
     * usually used to describe what this object represents. May be used
     * in gui tooltips.
*/
     */
    public void setInfo( String info ) throws EditableException{

        checkEditable(C + ": setInfo(): ");
        this.info = info;
    }

/** @merge workspace: Changes in the Workspace */
    /**
     * Sets the units of this parameter
     * @param units
     */
    public void setUnits(String units) throws EditableException {
        checkEditable(C + ": setUnits(): ");
        this.units = units;
    }

/** @merge repository: Changes from the Repository
    /** Sets the units of this parameter */
    public void setUnits(String units) throws EditableException {
        checkEditable(C + ": setUnits(): ");
        this.units = units;
    }

*/


    /**
     *  Disables editing units, info, constraints, et. Basically all set()s disabled
     *  except for setValue(). Once set non-editable, it cannot be set back.
     */
    public void setNonEditable() {
        editable = false;
    }

/** @merge workspace: Changes in the Workspace */


/** @merge repository: Changes from the Repository
*/
    /**
     *  Returns the parameter's value. Each subclass defines what type of
     *  object. it returns
     *
     * @return    The value value
     */
/** @merge workspace: Changes in the Workspace */
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
/** @merge repository: Changes from the Repository
    public Object getValue() { return value; }


    /** Every parameter has a name, this function returns that name. */
    public String getName() { return name; }
    /** Every parameter has a name, this function sets that name, if editable. */
    public void setName(String name){
        checkEditable(C + ": setName(): ");
        this.name = name;
*/
    }


/** @merge workspace: Changes in the Workspace */
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

/** @merge repository: Changes from the Repository
    /** Returns the units of this parameter, represented as a String. */
    public String getUnits() { return units; }

    /** Gets the constraints of this parameter. */
    public ParameterConstraintAPI getConstraint() { return constraint; }

*/
    /**
/** @merge workspace: Changes in the Workspace */
     *  Gets the constraints of this parameter. Each subclass may implement any
     *  type of constraint it likes.
     *
     * @return    The constraint value
/** @merge repository: Changes from the Repository
     * Gets the constraints of this parameter if editable. Each
     * subclass may implement any type of constraint it likes.
*/
     */
    public void setConstraint(ParameterConstraintAPI constraint) throws EditableException{
        checkEditable(C + ": setConstraint(): ");
        this.constraint = constraint;
    }

/** @merge workspace: Changes in the Workspace */


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

/** @merge repository: Changes from the Repository
    /** Returns a description of this Parameter, typically used for tooltips. */
    public String getInfo() { return info; }

    /** Returns the short class name of this object. */
    public String getType() { return C; }


    /** Determines if the value can be edited, i.e. changed once initialized. */
    public boolean isEditable() { return editable; }


*/

    /**
/** @merge workspace: Changes in the Workspace */
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
/** @merge repository: Changes from the Repository
     *  Disables editing units, info, constraints, et. Basically all set()s disabled
     *  except for setValue(). Once set non-editable, it cannot be set back.
     */
    public void setNonEditable() {
        editable = false;
    }

    /**
     *  Returns the parameter's value. Each subclass defines what type of
     *  object. it returns
*/
     *
     * @return    The value value
     */
    public Object getValue() { return value; }


    /** Every parameter has a name, this function returns that name. */
    public String getName() { return name; }
    /** Every parameter has a name, this function sets that name, if editable. */
    public void setName(String name){
        checkEditable(C + ": setName(): ");
        this.name = name;
    }


    /** Returns the units of this parameter, represented as a String. */
    public String getUnits() { return units; }

    /** Gets the constraints of this parameter. */
    public ParameterConstraintAPI getConstraint() { return constraint; }

    /**
     * Gets the constraints of this parameter if editable. Each
     * subclass may implement any type of constraint it likes.
     */
    public void setConstraint(ParameterConstraintAPI constraint) throws EditableException{
        checkEditable(C + ": setConstraint(): ");
        this.constraint = constraint;
    }

    /** Returns a description of this Parameter, typically used for tooltips. */
    public String getInfo() { return info; }

    /** Returns the short class name of this object. */
    public String getType() { return C; }


    /** Determines if the value can be edited, i.e. changed once initialized. */
    public boolean isEditable() { return editable; }


}
