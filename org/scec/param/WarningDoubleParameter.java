package org.scec.param;

import java.util.*;
import org.scec.param.event.*;
import org.scec.exceptions.*;

// Fix - Needs more comments

/**
 * <b>Title:</b> WarningDoubleParameter<p>
 *
 * <b>Description:</b> <p>
 *
 * @author Steven W. Rock
 * @version 1.0
 */

public class WarningDoubleParameter
    extends DoubleParameter
    implements WarningParameterAPI
{

    /**
     *  Class name for debugging.
     */
    protected final static String C = "WarningDoubleParameter";
    /**
     *  If true print out debug statements.
     */
    protected final static boolean D = false;

    /**
     *  THe constraint for this Parameter.
     */
    protected DoubleConstraint warningConstraint = null;

    /**
     * Only created if needed, else kept null.
     */
    protected Vector warningListeners = null;


    /**
     * Set to true to turn off warnings, will automatically set the value, unless
     * exceeds Absolute contrsints.
     */
    private boolean ignoreWarning;


    /**
     *  No constraints specified, all values allowed. Sets the name of this
     *  parameter.
     *
     * @param  name  Name of the parameter
     */
    public WarningDoubleParameter( String name ) {
        super(name);
    }


    /**
     *  No constraints specified, all values allowed. Sets the name and untis of
     *  this parameter.
     *
     * @param  name   Name of the parameter
     * @param  units  Units of this parameter
     */
    public WarningDoubleParameter( String name, String units ) {
        super( name, units );
    }


    /**
     *  Sets the name, defines the constraints min and max values. Creates the
     *  constraint object from these values.
     *
     * @param  name                     Name of the parameter
     * @param  min                      defines min of allowed values
     * @param  max                      defines max of allowed values
     * @exception  ConstraintException  thrown if the value is not allowed
     * @throws  ConstraintException     Is thrown if the value is not allowed
     */
    public WarningDoubleParameter( String name, double min, double max ) throws ConstraintException {
        super( name, min, max );
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
    public WarningDoubleParameter( String name, double min, double max, String units ) throws ConstraintException {
        super( name, min, max , units );
    }


    /**
     *  Sets the name, defines the constraints min and max values. Creates the
     *  constraint object from these values.
     *
     * @param  name                     Name of the parameter
     * @param  min                      defines min of allowed values
     * @param  max                      defines max of allowed values
     * @exception  ConstraintException  thrown if the value is not allowed
     * @throws  ConstraintException     Is thrown if the value is not allowed
     */
    public WarningDoubleParameter( String name, Double min, Double max ) throws ConstraintException {
        super( name, min, max );
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
    public WarningDoubleParameter( String name, Double min, Double max, String units ) throws ConstraintException {
        super( name, min, max , units );
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
    public WarningDoubleParameter( String name, DoubleConstraint constraint ) throws ConstraintException {
        super( name, constraint);
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
    public WarningDoubleParameter( String name, DoubleConstraint constraint, String units ) throws ConstraintException {
        super( name, constraint, units );
    }


    /**
     *  No constraints specified, all values allowed. Sets the name and value.
     *
     * @param  name   Name of the parameter
     * @param  value  Double value of this parameter
     */
    public WarningDoubleParameter( String name, Double value ) {
        super( name, value );
    }


    /**
     *  Sets the name, units and value. All values allowed because constraints.
     *  not set.
     *
     * @param  name                     Name of the parameter
     * @param  value                    Double value of this parameter
     * @param  units                    Units of this parameter
     * @exception  ConstraintException  thrown if the value is not allowed
     * @throws  ConstraintException     Is thrown if the value is not allowed
     */
    public WarningDoubleParameter( String name, String units, Double value ) throws ConstraintException {
        super( name, units, value );
    }


    /**
     *  Sets the name, and value. Also defines the min and max from which the
     *  constraint is constructed.
     *
     * @param  name                     Name of the parameter
     * @param  value                    Double value of this parameter
     * @param  min                      defines max of allowed values
     * @param  max                      defines min of allowed values
     * @exception  ConstraintException  thrown if the value is not allowed
     * @throws  ConstraintException     Is thrown if the value is not allowed
     */
    public WarningDoubleParameter( String name, double min, double max, Double value ) throws ConstraintException {
        super( name, min, max, value );
    }


    /**
     *  Sets the name, value and constraint. The value is checked if it is
     *  within constraints.
     *
     * @param  name                     Name of the parameter
     * @param  constraint               defines min and max range of allowed
     *      values
     * @param  value                    Double value of this parameter
     * @exception  ConstraintException  thrown if the value is not allowed
     * @throws  ConstraintException     Is thrown if the value is not allowed
     */
    public WarningDoubleParameter( String name, DoubleConstraint constraint, Double value ) throws ConstraintException {
        super( name, constraint, value );
    }


    /**
     *  Sets all values, and the constraint is created from the min and max
     *  values. The value is checked if it is within constraints.
     *
     * @param  name                     Name of the parameter
     * @param  value                    Double value of this parameter
     * @param  min                      defines min of allowed values
     * @param  max                      defines max of allowed values
     * @param  units                    Units of this parameter
     * @exception  ConstraintException  Is thrown if the value is not allowed
     * @throws  ConstraintException     Is thrown if the value is not allowed
     */
    public WarningDoubleParameter( String name, double min, double max, String units, Double value ) throws ConstraintException {
        super( name, min, max, units, value );
    }

    /**
     *  Sets all values, and the constraint is created from the min and max
     *  values. The value is checked if it is within constraints.
     *
     * @param  name                     Name of the parameter
     * @param  value                    Double value of this parameter
     * @param  min                      defines min of allowed values
     * @param  max                      defines max of allowed values
     * @param  units                    Units of this parameter
     * @exception  ConstraintException  Is thrown if the value is not allowed
     * @throws  ConstraintException     Is thrown if the value is not allowed
     */
    public WarningDoubleParameter( String name, Double min, Double max, String units, Double value ) throws ConstraintException {
        super( name, min, max, units, value );
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
     * @param  value                    Double value of this parameter
     * @param  units                    Units of this parameter
     * @exception  ConstraintException  Is thrown if the value is not allowed
     * @throws  ConstraintException     Is thrown if the value is not allowed
     */

    public WarningDoubleParameter( String name, DoubleConstraint constraint, String units, Double value )
             throws ConstraintException {
        super( name, constraint, units, value );
        //if( (constraint != null) && (constraint.getName() == null) )
            //constraint.setName( name );
    }



    /**
     *  Adds a feature to the ParameterChangeFailListener attribute of the
     *  ParameterEditor object
     *
     * @param  listener  The feature to be added to the
     *      ParameterChangeFailListener attribute
     */
    public synchronized void addParameterChangeWarningListener( ParameterChangeWarningListener listener )
        throws EditableException
    {

        String S = C + ": addParameterChangeWarningListener(): ";
        //checkEditable(S);

        if ( warningListeners == null ) warningListeners = new Vector();
        if ( !warningListeners.contains( listener ) ) {
            if(D) System.out.println(S + "Adding listener: " + listener.getClass().getName() );
            warningListeners.addElement( listener );

        }

    }

    /**
     *  Description of the Method
     *
     * @param  listener  Description of the Parameter
     */
    public synchronized void removeParameterChangeWarningListener( ParameterChangeWarningListener listener )
        throws EditableException
    {
        String S = C + ": removeParameterChangeWarningListener(): ";
        //checkEditable(S);

        if ( warningListeners != null && warningListeners.contains( listener ) )
            warningListeners.removeElement( listener );
    }

    /**
     *  Sets the constraint if it is a StringConstraint and the parameter
     *  is currently editable.
     */
    public void setWarningConstraint(DoubleConstraint warningConstraint)
        throws ParameterException, EditableException
    {
        String S = C + ": setWarningConstraint(): ";
        checkEditable(S);

        this.warningConstraint = warningConstraint;

        //if( (this.warningConstraint != null) && (this.warningConstraint.getName() == null) )
            //this.warningConstraint.setName( this.getName() );
    }

    /**
     *  Sets the constraint if it is a StringConstraint and the parameter
     *  is currently editable.
     */
    public DoubleConstraint getWarningConstraint() throws ParameterException{
        return warningConstraint;
    }

    /**
     *  Gets the min value of the constraint object.
     *
     * @return                The min value
     * @exception  Exception  Description of the Exception
     */
    public Double getWarningMin() throws Exception {
        if ( warningConstraint != null ) return warningConstraint.getMin();
        else return null;
    }


    /**
     *  Returns the maximum allowed values.
     *
     * @return    The max value
     */
    public Double getWarningMax() {
        if ( warningConstraint != null ) return warningConstraint.getMax();
        else return null;

    }


    /**
     *  Set's the parameter's value.
     *
     * @param  value                 The new value for this Parameter
     * @throws  ParameterException   Thrown if the object is currenlty not
     *      editable
     * @throws  ConstraintException  Thrown if the object value is not allowed
     */
    public synchronized void setValue( Object value ) throws ConstraintException, WarningException {
        String S = C + ": setValue(): ";
        if(D) System.out.println(S + "Starting: ");

        if ( !isAllowed( value ) ) {
            String err = S + "Value is not allowed: ";
            if( value != null ) err += value.toString();
            if(D) System.out.println(err);
            throw new ConstraintException( err );
        }
        else if ( value == null ){
            if(D) System.out.println(S + "Setting allowed and recommended null value: ");
            this.value = null;
        }
        else if ( !ignoreWarning && !isRecommended( value ) ) {

            if(D) System.out.println(S + "Firing Warning Event");

            ParameterChangeWarningEvent event = new
                  ParameterChangeWarningEvent( (Object)this, this, this.value, value );

            fireParameterChangeWarning( event );
            throw new WarningException( S + "Value is not recommended: " + value.toString() );
        }
        else {
            if(D) System.out.println(S + "Setting allowed and recommended value: ");
            this.value = value;
        }
        if(D) System.out.println(S + "Ending: ");
    }

    /**
     *  Set's the parameter's value.
     *
     * @param  value                 The new value for this Parameter
     * @throws  ParameterException   Thrown if the object is currenlty not
     *      editable
     * @throws  ConstraintException  Thrown if the object value is not allowed
     */
    public void setValueIgnoreWarning( Object value ) throws ConstraintException, ParameterException {
        String S = C + ": setValueIgnoreWarning(): ";
        if(D) System.out.println(S + "Setting value ignoring warning and constraint: ");
        this.value = value;
    }

    /**
     *  Uses the constraint object to determine if the new value being set is
     *  within recommended range. If no Constraints are present all values are recommended.
     *
     * @param  obj  Object to check if allowed via constraints
     * @return      True if the value is allowed
     */
    public boolean isRecommended( Object obj ) {
        if ( warningConstraint != null ) return warningConstraint.isAllowed( obj );
        else return true;

    }


    /**
     *  Description of the Method
     *
     * @param  event  Description of the Parameter
     */
    public void fireParameterChangeWarning( ParameterChangeWarningEvent event ) {

        String S = C + ": firePropertyChange(): ";
        if(D) System.out.println(S + "Starting: " + this.getName() );


        Vector vector;
        synchronized ( this ) {
            if ( warningListeners == null ) return;
            vector = ( Vector ) warningListeners.clone();
       }
        for ( int i = 0; i < vector.size(); i++ ) {
            ParameterChangeWarningListener listener = ( ParameterChangeWarningListener ) vector.elementAt( i );
            if(D) System.out.println(S + "Firing warning to (" + i + ") " + listener.getClass().getName());
            listener.parameterChangeWarning( event );
        }

        if(D) System.out.println(S + "Ending: " + this.getName() );

    }



    /**
     *  Compares the values to if this is less than, equal to, or greater than
     *  the comparing objects.
     *
     * @param  obj                     The object to compare this to
     * @return                         -1 if this value < obj value, 0 if equal,
     *      +1 if this value > obj value
     * @exception  ClassCastException  Is thrown if the comparing object is not
     *      a DoubleParameter, or DoubleDiscreteParameter.
     */
    public int compareTo( Object obj ) throws ClassCastException {

        String S = C + ":compareTo(): ";

        if ( !( obj instanceof DoubleParameter )
            && !( obj instanceof DoubleDiscreteParameter )
            && !( obj instanceof WarningDoubleParameter )
        ) {
            throw new ClassCastException( S +
                "Object not a DoubleParameter, WarningDoubleParameter, or DoubleDiscreteParameter, unable to compare"
            );
        }

        int result = 0;

        Double n1 = ( Double ) this.getValue();
        Double n2 = null;

        if ( obj instanceof DoubleParameter ) {
            DoubleParameter param = ( DoubleParameter ) obj;
            n2 = ( Double ) param.getValue();
        }
        else if ( obj instanceof DoubleDiscreteParameter ) {
            DoubleDiscreteParameter param = ( DoubleDiscreteParameter ) obj;
            n2 = ( Double ) param.getValue();
        }
        else if ( obj instanceof WarningDoubleParameter ) {
            WarningDoubleParameter param = ( WarningDoubleParameter ) obj;
            n2 = ( Double ) param.getValue();
        }

        return n1.compareTo( n2 );
    }


    /**
     *  Compares value to see if equal.
     *
     * @param  obj                     The object to compare this to
     * @return                         True if the values are identical
     * @exception  ClassCastException  Is thrown if the comparing object is not
     *      a DoubleParameter, or DoubleDiscreteParameter.
     */
    public boolean equals( Object obj ) throws ClassCastException {
        String S = C + ":equals(): ";

        if ( !( obj instanceof DoubleParameter )
            && !( obj instanceof DoubleDiscreteParameter )
            && !( obj instanceof WarningDoubleParameter )
        ) {
            throw new ClassCastException( S + "Object not a DoubleParameter, WarningDoubleParameter, or DoubleDiscreteParameter, unable to compare" );
        }

        String otherName = ( ( DoubleParameter ) obj ).getName();
        if ( ( compareTo( obj ) == 0 ) && getName().equals( otherName ) ) {
            return true;
        }
        else return false;
    }


    /**
     *  Returns a copy so you can't edit or damage the origial.
     *
     * @return    Exact copy of this object's state
     */
    public Object clone() {

        String S = C + ":clone(): ";
        if(D) System.out.println(S + "Starting");

        DoubleConstraint c1 = null;
        DoubleConstraint c2 = null;

        if( constraint != null ) c1 = ( DoubleConstraint ) constraint.clone();
        if( warningConstraint != null ) c2 = ( DoubleConstraint ) warningConstraint.clone();


        WarningDoubleParameter param = null;
        if( value == null ) param = new WarningDoubleParameter( name, c1, units);
        else param = new WarningDoubleParameter( name, c1, units, new Double( this.value.toString() )  );
        if( param == null ) return null;

        param.setWarningConstraint(c2);


        ListIterator it = this.getIndependentParametersIterator();
        while( it.hasNext() ){

            ParameterAPI p1 = (ParameterAPI)it.next();
            ParameterAPI p2 = (ParameterAPI)p1.clone();
            param.addIndependentParameter(p2);

        }


        // NOTE: The listeners are NOT cloned. They were interested in the original,
        // so should be interested in the clone

        if( this.warningListeners != null ){
            it = this.warningListeners.listIterator();
            while( it.hasNext() ){
                ParameterChangeWarningListener listener = (ParameterChangeWarningListener)it.next();
                param.addParameterChangeWarningListener( listener );
            }
        }


        param.setInfo(info);
        param.setIgnoreWarning(this.ignoreWarning);


        if( !editable ) param.setNonEditable();

        if(D) System.out.println(S + "Ending");
        return param;
    }


    /**
     * Set to true to turn off warnings, will automatically set the value, unless
     * exceeds Absolute contrsints. Set to false so that warning constraints are
     * enabled, i.e. throw a WarningConstraintException if exceed recommened warnings.
     */
    public void setIgnoreWarning(boolean ignoreWarning) { this.ignoreWarning = ignoreWarning; }

    /**
     * Returns warning constraint enabled/disabled. If true warnings are turned off ,
     * will automatically set the value, unless exceeds Absolute contrsints.
     * If set to false warning constraints are enabled, i.e. throw a
     * WarningConstraintException if exceed recommened warnings.
     */
    public boolean isIgnoreWarning() { return ignoreWarning; }


}
