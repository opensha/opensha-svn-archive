package org.scec.param;

import org.scec.exceptions.ConstraintException;
import org.scec.exceptions.ParameterException;

import org.scec.sha.fault.*;
import org.scec.util.*;
import org.scec.data.*;
import org.scec.param.event.*;

/**
 *  <b>Title:</b> ParameterAPI Interface<p>
 *
 *  <b>Description:</b> All parameter classes must implement this API to
 *  "plug" into the framework. A parameter basically contains some type
 *  of java object, such as a String, Double, etc. This parameter
 *  framework extends on the basic Java DataTypes by adding constraint objects,
 *  a name, information string, units string, parameter change fail and succede
 *  listeners, etc. These parameters are "Supercharged" data types with alot
 *  of functionality added. This API defines the basic added functionality and
 *  getter and setter functions for adding these extra features. <p>
 *
 *  The parameter value can be any type of object as defined by subclasses. One
 *  reason for having this framework is to enable new types of parameter
 *  in the future to be defined and added to a Site, ProbEqkRupture,
 *  or PropagationEffect object without having to rewrite the Java code. <p>
 *
 *  By defining the parameter value here as a generic object, one is not
 *  restricted to adding scalar quantities. For example, one could create a
 *  subclass of parameter where the value is a moment tensor (which could then
 *  be added to a ProbEqkRupture object). As another example, one could
 *  define a subclass of parameter where the value is a shear-wave velocity
 *  profile (which could be added to a Site object). <p>
 *
 *  Representing such non-scalar quantities as Parameters might seem confusing
 *  semantically (e.g., perhaps Attribute would be better). However, the term
 *  Parameter is consistent with the notion that an IntensityMeasureRealtionship
 *  will used this information as an independent variable when computing
 *  earthquake motion. <p>
 *
 *  <b>Revision History</b> <br>
 *  1/1/2002 SWR
 *  <ul>
 *    <LI> Removed setName(), setUnits(), setConstraints. These can only be set
 *    in Constructors now. Only the value can be changed after creation.
 *    <LI> Added compareTo() and equals(). This will test if another parameter
 *    is equal to this one based on value, not if they point to the same object
 *    in the Java Virtual Machine as the default equals() does. CompareTo() will
 *    become useful for sorting a list of parameters.
 *    <LI>
 *  </ul>
 *  <p>
 *
 * @author     Steven W. Rock
 * @created    February 21, 2002
 * @version    1.0
 */

public interface ParameterAPI extends NamedObjectAPI, Comparable {

    /** Every parameter has a name, this function gets that name. */
    public String getName();

    /** Every parameter has a name, this function sets that name. */
    public void setName(String name);

    /**
     * Every parameter constraint has a name, this
     * function gets that name. Defaults to the name
     * of the parameter but in some cases may be different.
     */
    public String getConstraintName(  );

    /**
     * Returns the constraint of this parameter. Each
     * subclass may store any type of constraint it likes.
     */
    public ParameterConstraintAPI getConstraint();

    /**
     * Sets the constraints of this parameter. Each
     * subclass may store any type of constraint it likes.
     */
    public void setConstraint(ParameterConstraintAPI constraint);


    /** Returns the units of this parameter, represented as a String. */
    public String getUnits();

    /** Sets the string name of units of this parameter */
    public void setUnits(String units);

    /** Returns a description of this Parameter, typically used for tooltips. */
    public String getInfo();

    /** Sets the info attribute of the ParameterAPI object. */
    public void setInfo( String info );

    /** Returns the value onject stored in this parameter. */
    public Object getValue();

    /**
     *  Set's the parameter's value.
     *
     * @param  value                 The new value for this Parameter
     * @throws  ParameterException   Thrown if the object type of value to set
     *      is not the correct type.
     * @throws  ConstraintException  Thrown if the object value is not allowed
     */
    public void setValue( Object value ) throws ConstraintException, ParameterException;


     /** Needs to be called by subclasses when field change fails due to constraint problems. */
     public void unableToSetValue( Object value ) throws ConstraintException;



    /**
     *  Adds a feature to the ParameterChangeFailListener attribute
     *
     * @param  listener  The feature to be added to the
     *      ParameterChangeFailListener attribute
     */
    public void addParameterChangeFailListener( org.scec.param.event.ParameterChangeFailListener listener );


    /**
     *  Description of the Method
     *
     * @param  listener  Description of the Parameter
     */
    public void removeParameterChangeFailListener( org.scec.param.event.ParameterChangeFailListener listener );

    /**
     *  Description of the Method
     *
     * @param  event  Description of the Parameter
     */
    public void firePropertyChangeFailed( org.scec.param.event.ParameterChangeFailEvent event ) ;


    /**
     *  Adds a feature to the ParameterChangeListener attribute
     *
     * @param  listener  The feature to be added to the ParameterChangeListener
     *      attribute
     */
    public void addParameterChangeListener( org.scec.param.event.ParameterChangeListener listener );
    /**
     *  Description of the Method
     *
     * @param  listener  Description of the Parameter
     */
    public void removeParameterChangeListener( org.scec.param.event.ParameterChangeListener listener );

    /**
     *  Description of the Method
     *
     * @param  event  Description of the Parameter
     */
    public void firePropertyChange( ParameterChangeEvent event ) ;


    /**
     *  Returns the data type of the value object. Used to determine which type
     *  of Editor to use in a GUI.
     */
    public String getType();


    /**
     *  Compares the values to see if they are the same. Returns -1 if obj is
     *  less than this object, 0 if they are equal in value, or +1 if the object
     *  is greater than this.
     *
     * @param  parameter            the parameter to compare this object to.
     * @return                      -1 if this value < obj value, 0 if equal, +1
     *      if this value > obj value
     * @throws  ClassCastException  Thrown if the object type of the parameter
     *      argument are not the same.
     */
    public int compareTo( Object parameter ) throws ClassCastException;


    /**
     *  Compares passed in parameter value to this one to see if equal.
     *
     * @param  parameter            the parameter to compare this object to.
     * @return                      True if the values are identical
     * @throws  ClassCastException  Thrown if the object type of the parameter
     *      argument are not the same.
     */
    public boolean equals( Object parameter ) throws ClassCastException;


    /**
     * Proxy to constraint check when setting a value. If no
     * constraint then this always returns true.
     */
    public boolean isAllowed( Object value );


    /** Determines if the value can be edited, i.e. changed after initialization .*/
    public boolean isEditable();


    /**
     * Disables editing the value once it is set. This permits
     * a one time setup during initialization. Then the object ca
     * be changed to read only.
     */
    public void setNonEditable();


    /** Returns a copy so you can't edit or damage the origial. */
    public Object clone();

    /**
     * Checks if null values are permitted via the constraint. If true then
     * nulls are allowed.
     */
    public boolean isNullAllowed();

}
