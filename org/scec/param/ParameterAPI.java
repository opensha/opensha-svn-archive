package org.scec.param;

import org.scec.exceptions.ConstraintException;
import org.scec.exceptions.ParameterException;

import org.scec.sha.fault.*;
import org.scec.util.*;
import org.scec.data.*;

/**
 *  <b>Title:</b> ParameterAPI Interface<br>
 *  <b>Description:</b> The parameter value can be any type of object. One
 *  reason for having this class is to enable new types of attributes to be
 *  defined and added to a Site, ProbEqkRupture, or PropagationEffect
 *  object without having to rewrite the Java code. <br>
 *  By defining the parameter “value” here as a generic object, one is not
 *  restricted to adding scalar quantities. For example, one could create a
 *  subclass of parameter where the value is a moment tensor (which could then
 *  be added to a ProbEqkRupture object). As another example, one could
 *  define a subclass of parameter where the value is a shear-wave velocity
 *  profile (which could be added to a Site object). <br>
 *  Representing such non-scalar quantities as “Parameters” might seem confusing
 *  semantically (e.g., perhaps “Attribute” would be better). However, the term
 *  “Parameter” is consistent with the notion that an
 *  IntensityMeasureRealtionship will used this information as an independent
 *  variable when computing earthquake motion. <br>
 *  Revision History <br>
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
 *  <br>
 *  <b>Copyright:</b> Copyright (c) 2001<br>
 *  <b>Company:</b> <br>
 *
 *
 * @author     Steven W. Rock
 * @created    February 21, 2002
 * @version    1.0
 */

public interface ParameterAPI extends NamedObjectAPI, Comparable {

    /**
     *  Every parameter has a name, this function returns that name.
     *
     * @return    The name value
     */
    public String getName();

    /**
     *  Every parameter has a name, this function returns that name.
     *
     * @return    The name value
     */
    public void setName(String name);

    /**
     *  Every parameter constraint has a name, this function sets that name.
     *  Defaults to the name of the parameter
     *
     * @return    The name value
     */
    public String getConstraintName(  );

    /**
     *  Gets the constraints of this parameter. Each subclass may implement any
     *  type of constraint it likes.
     *
     * @return    The constraint value
     */
    public ParameterConstraintAPI getConstraint();

    /**
     *  Gets the constraints of this parameter. Each subclass may implement any
     *  type of constraint it likes.
     *
     * @return    The constraint value
     */
    public void setConstraint(ParameterConstraintAPI constraint);


    /**
     *  Returns the units of this parameter, represented as a String.
     *
     * @return    The units value
     */
    public String getUnits();

    /**
     * Sets the units of this parameter
     * @param units
     */
    public void setUnits(String units);

    /**
     *  Returns a description of this Parameter, typically used for tooltips.
     *
     * @return    The info value
     */
    public String getInfo();


    /**
     *  Sets the info attribute of the ParameterAPI object.
     *
     * @param  info  The new info value
     */
    public void setInfo( String info );



    /**
     *  Returns the value onject stored in this parameter.
     *
     * @return    The value value
     */
    public Object getValue();


    /**
     *  Set's the parameter's value.
     *
     * @param  value                 The new value for this Parameter
     * @throws  ParameterException   Thrown if the object type of value to set
     *      is not the correct type
     * @throws  ConstraintException  Thrown if the object value is not allowed
     */
    public void setValue( Object value ) throws ConstraintException, ParameterException;


    /**
     *  Returns the data type of the value object. Used to determine which type
     *  of Editor to use in a GUI.
     *
     * @return    The type value
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
     *  Compares value to see if equal.
     *
     * @param  parameter            the parameter to compare this object to.
     * @return                      True if the values are identical
     * @throws  ClassCastException  Thrown if the object type of the parameter
     *      argument are not the same.
     */
    public boolean equals( Object parameter ) throws ClassCastException;


    /**
     *  Proxy to constraint check when setting a value.
     *
     * @param  value  Description of the Parameter
     * @return        The allowed value
     */
    public boolean isAllowed( Object value );


    /**
     *  Determines if the value can be edited, i.e. changed once set.
     *
     * @return    The editable value
     */
    public boolean isEditable();


    /**
     *  Disables editing the value once it is set.
     */
    public void setNonEditable();


    /**
     *  Returns a copy so you can't edit or damage the origial.
     *
     * @return    Description of the Return Value
     */
    public Object clone();


    public boolean isNullAllowed();

}
