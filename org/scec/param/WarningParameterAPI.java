package org.scec.param;

import org.scec.exceptions.*;
import org.scec.param.event.*;
import org.scec.param.event.ParameterChangeWarningListener;


/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public interface WarningParameterAPI extends ParameterAPI{


    public void setIgnoreWarning(boolean ignoreWarning);
    public boolean isIgnoreWarning();

    /**
     *  Sets the constraint if it is a StringConstraint and the parameter
     *  is currently editable.
     */
    public void setWarningConstraint(DoubleConstraint warningConstraint);

    /**
     *  Sets the constraint if it is a StringConstraint and the parameter
     *  is currently editable.
     */
    public DoubleConstraint getWarningConstraint() throws ParameterException;


    /**
     *  Adds a feature to the ParameterChangeFailListener attribute of the
     *  ParameterEditor object
     *
     * @param  listener  The feature to be added to the
     *      ParameterChangeFailListener attribute
     */
    public void addParameterChangeWarningListener( ParameterChangeWarningListener listener );

    /**
     *  Description of the Method
     *
     * @param  listener  Description of the Parameter
     */
    public void removeParameterChangeWarningListener( ParameterChangeWarningListener listener );




    /**
     *  Uses the constraint object to determine if the new value being set is
     *  within recommended range. If no Constraints are present all values are recommended.
     *
     * @param  obj  Object to check if allowed via constraints
     * @return      True if the value is allowed
     */
    public boolean isRecommended( Object obj );

    /**
     *  Set's the parameter's value.
     *
     * @param  value                 The new value for this Parameter
     * @throws  ParameterException   Thrown if the object is currenlty not
     *      editable
     * @throws  ConstraintException  Thrown if the object value is not allowed
     */
    public void setValueIgnoreWarning( Object value ) throws ConstraintException, ParameterException;


    /**
     *  Description of the Method
     *
     * @param  event  Description of the Parameter
     */
    public void fireParameterChangeWarning( ParameterChangeWarningEvent event );


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
    public int compareTo( Object obj ) throws ClassCastException;

    /**
     *  Compares value to see if equal.
     *
     * @param  obj                     The object to compare this to
     * @return                         True if the values are identical
     * @exception  ClassCastException  Is thrown if the comparing object is not
     *      a DoubleParameter, or DoubleDiscreteParameter.
     */
    public boolean equals( Object obj ) throws ClassCastException ;

    /**
     *  Returns a copy so you can't edit or damage the origial.
     *
     * @return    Exact copy of this object's state
     */
    public Object clone() ;


    /**
     *  Gets the min value of the constraint object.
     *
     * @return                The min value
     * @exception  Exception  Description of the Exception
     */
    public Double getWarningMin() throws Exception ;


    /**
     *  Returns the maximum allowed values.
     *
     * @return    The max value
     */
    public Double getWarningMax() ;

}
