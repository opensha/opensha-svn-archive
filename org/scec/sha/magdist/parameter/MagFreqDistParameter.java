package org.scec.sha.magdist.parameter;

import java.util.*;

import org.scec.exceptions.*;
import org.scec.param.*;
import org.scec.sha.magdist.IncrementalMagFreqDist;

/**
 *  <b>Title:</b> MagFreqDistParameter<p>
 *
 *  <b>Description:</b> Generic Parameter that contains a IncremnetalMagFreqDist and
 *  optionally a list of allowed values stored in a constraint object. If no
 *  constraint object is present then all MagFreDists are permitted<p>
 *
 * @author     Nitin Gupta, Vipin Gupta
 * @created    Oct 18, 2002
 * @version    1.0
 */

public class MagFreqDistParameter
    extends DependentParameter
    implements DependentParameterAPI, ParameterAPI, java.io.Serializable
{

    /** Class name for debugging. */
    protected final static String C = "MagFreqDistParameter";
    /** If true print out debug statements. */
    protected final static boolean D = false;


    /**
     *  No constraints specified, all MagFreqDists allowed. Sets the name of this
     *  parameter.
     *
     * @param  name  Name of the parameter
     */
    public MagFreqDistParameter( String name ) {
        super( name, null, null, null );
    }



    /**
     *  Sets the name, defines the constraints as Vector of String values. Creates the
     *  constraint object from these values.
     *
     * @param  name                     Name of the parameter
     * @param  allowedMagDists          Vector of allowed values
     * @exception  ConstraintException  thrown if the value is not allowed
     * @throws  ConstraintException     Is thrown if the value is not allowed
     */
    public MagFreqDistParameter( String name, Vector allowedMagDists ) throws ConstraintException {
        super( name, new MagFreqDistConstraint( allowedMagDists ), null, null );
    }


    /**
     *  Sets the name and Constraints object.
     *
     * @param  name                     Name of the parameter
     * @param  constraint               defines vector of allowed values
     * @exception  ConstraintException  thrown if the value is not allowed
     * @throws  ConstraintException     Is thrown if the value is not allowed
     */
    public MagFreqDistParameter( String name, MagFreqDistConstraint constraint ) throws ConstraintException {
        super( name, constraint, null, null );
    }



    /**
     *  No constraints specified, all values allowed. Sets the name and value.
     *
     * @param  name   Name of the parameter
     * @param  value  IncrementalMagFreqDist  object
     */
    public MagFreqDistParameter( String name, IncrementalMagFreqDist value ) {
        super(name, null, null, value);
    }


    /**
     *  Sets the name, and value. Also defines the min and max from which the
     *  constraint is constructed.
     *
     * @param  name                     Name of the parameter
     * @param  value                    IncrementalMagFreqDist object of this parameter
     * @param  allowedMagDists          Vector  of allowed Mag Dists
     * @exception  ConstraintException  thrown if the value is not allowed
     * @throws  ConstraintException     Is thrown if the value is not allowed
     */
    public MagFreqDistParameter( String name, Vector allowedMagDists, IncrementalMagFreqDist value )
                                throws ConstraintException {
        super( name, new MagFreqDistConstraint( allowedMagDists ), null, value );
    }



    /**
     *  Sets the name, value and constraint. The value is checked if it is
     *  within constraints.
     *
     * @param  name                     Name of the parameter
     * @param  constraint               vector of allowed Mag Dists
     * @param  value                    IncrementalMagFreqDist object
     * @exception  ConstraintException  thrown if the value is not allowed
     * @throws  ConstraintException     Is thrown if the value is not allowed
     */
    public MagFreqDistParameter( String name, MagFreqDistConstraint constraint,
                                IncrementalMagFreqDist value ) throws ConstraintException {
        super( name, constraint, null, value );

    }




    /**
     *  Sets the constraint if it is a StringConstraint and the parameter
     *  is currently editable.
     */
    public void setConstraint(ParameterConstraintAPI constraint)
        throws ParameterException, EditableException
    {

        String S = C + ": setConstraint(): ";
        checkEditable(S);

        if ( !(constraint instanceof MagFreqDistConstraint )) {
            throw new ParameterException( S +
                "This parameter only accepts DoubleConstraints, unable to set the constraint."
            );
        }
        else super.setConstraint( constraint );

    }

    /**
     *  Gets the min value of the constraint object.
     *
     * @return                Vector of allowed Mag Dists
     */
    public Vector getAllowedMagDists()  {
        if ( constraint != null )
            return ( ( MagFreqDistConstraint ) constraint ).getAllowedMagDists();
        else return null;
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
    public int compareTo( Object obj ) throws UnsupportedOperationException {
      throw new java.lang.UnsupportedOperationException("This method not implemented yet");

    }


    /**
     *  Set's the parameter's value.
     *
     * @param  value                 The new value for this Parameter
     * @throws  ParameterException   Thrown if the object is currenlty not
     *      editable
     * @throws  ConstraintException  Thrown if the object value is not allowed
     */
    public void setValue( IncrementalMagFreqDist value ) throws ConstraintException, ParameterException {
        setValue( (Object) value );
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
    public boolean isAllowed( IncrementalMagFreqDist d ) {
        return isAllowed( (Object)d );
    }



    /**
     *  Compares value to see if equal.
     *
     * @param  obj                     The object to compare this to
     * @return                         True if the values are identical
     * @exception  ClassCastException  Is thrown if the comparing object is not
     *      a DoubleParameter, or DoubleDiscreteParameter.
     */
    public boolean equals( Object obj ) throws UnsupportedOperationException {
      throw new java.lang.UnsupportedOperationException("This method not implemented yet");

    }


    /**
     *  Returns a copy so you can't edit or damage the origial.
     *
     * @return    Exact copy of this object's state
     */
    public Object clone() throws UnsupportedOperationException {
      throw new java.lang.UnsupportedOperationException("This method not implemented yet");

    }

    /**
     * Returns the type(full path with the classname) of the MagDist Classes
     */
    public String getType() {
      String type = C;
      return type;
    }
}
