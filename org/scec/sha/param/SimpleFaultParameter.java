package org.scec.sha.param;

import org.scec.param.*;
import org.scec.sha.surface.EvenlyGriddedSurface;
import org.scec.exceptions.ParameterException;

/**
 * <p>Title: SimpleFaultParameter</p>
 * <p>Description: This is a more general parameter than the simple fault.
 * Actually it creates an object for the EvenlyGriddedSurface</p>
 * @author : Edward Field, Nitin Gupta and Vipin Gupta
 * @created : July 30, 2003
 * @version 1.0
 */

public class SimpleFaultParameter extends Parameter
             implements ParameterAPI, java.io.Serializable {

  /** Class name for debugging. */
  protected final static String C = "SimpleFaultParameter";
  /** If true print out debug statements. */
  protected final static boolean D = false;


  /**
   *  No constraints specified for this parameter. Sets the name of this
   *  parameter.
   *
   * @param  name  Name of the parameter
   */
  public SimpleFaultParameter(String name) {
    super(name,null,null,null);
  }

  /**
   * No constraints specified, all values allowed. Sets the name and value.
   *
   * @param  name   Name of the parameter
   * @param  surface  EvenlyGriddedSurface  object
   */
  public SimpleFaultParameter(String name, EvenlyGriddedSurface surface){
    super(name,null,null,surface);
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
  public void setValue( EvenlyGriddedSurface value ) throws ParameterException {
      setValue( (Object) value );
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
   * Returns the name of the parameter class
   */
  public String getType() {
    String type = C;
    return type;
  }

}


