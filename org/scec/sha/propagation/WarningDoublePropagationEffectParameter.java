package org.scec.sha.propagation;

import java.util.*;

import org.scec.exceptions.*;
import org.scec.param.*;
import org.scec.param.event.*;
import org.scec.sha.fault.*;
import org.scec.sha.earthquake.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public abstract class WarningDoublePropagationEffectParameter
    extends PropagationEffectParameter
    implements WarningParameterAPI
{

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

        if( !this.editable ) throw new EditableException(C + ": setStrings(): " +
            "This constraint is currently not editable." );

        if ( warningListeners == null ) warningListeners = new Vector();
        if ( !warningListeners.contains( listener ) ) warningListeners.addElement( listener );

    }

    /**
     *  Description of the Method
     *
     * @param  listener  Description of the Parameter
     */
    public synchronized void removeParameterChangeWarningListener( ParameterChangeWarningListener listener )
        throws EditableException
    {

        if( !this.editable ) throw new EditableException(C + ": setStrings(): " +
            "This constraint is currently not editable." );

        if ( warningListeners != null && warningListeners.contains( listener ) )
            warningListeners.removeElement( listener );
    }

    /**
     *  Sets the constraint if it is a DoubleConstraint and the parameter
     *  is currently editable.
     */
    public void setWarningConstraint(DoubleConstraint warningConstraint)
        throws ParameterException, EditableException
    {
        if( !this.editable ) throw new EditableException(C + ": setStrings(): " +
            "This constraint is currently not editable." );

        this.warningConstraint = warningConstraint;
    }

    /**
     *  Gets the warning constraint.
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
            else err += "null value";

            if(D) System.out.println(err);
            throw new ConstraintException( err );
        }
        else if ( value == null ){
            if(D) System.out.println(S + "Setting allowed and recommended null value: ");
            this.value = null;
        }
        else if ( !isRecommended( value ) ) {

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

        Vector vector;
        synchronized ( this ) {
            if ( warningListeners == null ) return;
            vector = ( Vector ) warningListeners.clone();
        }

        for ( int i = 0; i < vector.size(); i++ ) {
            ParameterChangeWarningListener listener = ( ParameterChangeWarningListener ) vector.elementAt( i );
            listener.parameterChangeWarning( event );
        }

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
            && !( obj instanceof WarningDoublePropagationEffectParameter )
        ) {
            throw new ClassCastException( S +
                "Object not a DoubleParameter, WarningDoubleParameter, DoubleDiscreteParameter, DistanceJBParameter, or WarningDoublePropagationEffectBParameter, unable to compare"
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

        else if ( obj instanceof WarningDoublePropagationEffectParameter ) {
            WarningDoublePropagationEffectParameter param = ( WarningDoublePropagationEffectParameter ) obj;
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
            && !( obj instanceof WarningDoublePropagationEffectParameter )
        ) {
            throw new ClassCastException( S + "Object not a DoubleParameter, WarningDoubleParameter, or DoubleDiscreteParameter, unable to compare" );
        }

        String otherName = ( ( ParameterAPI ) obj ).getName();
        if ( ( compareTo( obj ) == 0 ) && getName().equals( otherName ) ) {
            return true;
        }
        else return false;
    }


}
