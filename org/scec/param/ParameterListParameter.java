package org.scec.param;

import java.util.*;
import org.scec.exceptions.*;


/**
 * <p>Title: ParameterListParameter</p>
 * <p>Description: Make a parameter which is basically a parameterList</p>
 * @author : Nitin Gupta and Vipin Gupta
 * @created : Aug 18, 2003
 * @version 1.0
 */

public class ParameterListParameter extends Parameter
    implements ParameterAPI, java.io.Serializable{


  /** Class name for debugging. */
  protected final static String C = "ParameterListParameter";
  /** If true print out debug statements. */
  protected final static boolean D = false;

  protected final static String PARAM_TYPE ="ParameterListParameter";

  private final static double tolerence = 0.01;


  /**
   *  No constraints specified for this parameter. Sets the name of this
   *  parameter.
   *
   * @param  name  Name of the parameter
   */
  public ParameterListParameter(String name) {
    super(name,null,null,null);
  }

  /**
   * No constraints specified, all values allowed. Sets the name and value.
   *
   * @param  name   Name of the parameter
   * @param  paramList  ParameterList  object
   */
  public ParameterListParameter(String name, ParameterList paramList){
    super(name,null,null,paramList);
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
   * Set's the parameter's value. It checks that all the Parameter in this parameterList
   * should be DoubleParameter.
   *
   * @param  value                 The new value for this Parameter
   * @throws  ParameterException   Thrown if the object is currenlty not
   *      editable
   * @throws  ConstraintException  Thrown if the object value is not allowed
   */
  public void setValue( ParameterList value ) throws ParameterException {

    ListIterator it  = value.getParametersIterator();
    while(it.hasNext()){
      ParameterAPI param = (ParameterAPI)it.next();
      if(!(param instanceof DoubleParameter))
        throw new RuntimeException(C+" Only DoubleParameter allowed in this Parameter");
    }
    setValue( (Object) value );
  }

  /**
   * Compares value to see if equal.
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
   * Returns the ListIterator of the parameters included within this parameter
   * @return
   */
  public ListIterator getParametersIterator(){
    return ((ParameterList)this.getValue()).getParametersIterator();
  }

  /**
   * Returns the name of the parameter class
   */
  public String getType() {
    String type = this.PARAM_TYPE;
    return type;
  }

  /**
   *
   * @returns true if the sum of the sum of the parameters value, inside the parameterList
   * lie within the range of "1".
   * else return false.
   */
  public boolean checkParametersSumtoOne(ParameterList paramList){
    ListIterator it =paramList.getParametersIterator();
    double paramsSum=0;
    while(it.hasNext()){
      paramsSum += ((Double)((ParameterAPI)it.next()).getValue()).doubleValue();
    }
    return isInTolerence(paramsSum);
  }

  /**
   * check if this parameter values  lies in tolerence
   * @param num - sum of the parameter value
   * @return
   */
  private boolean isInTolerence(double num){
    if((num <= (1+this.tolerence)) && (num >= (1-this.tolerence)))
      return true;
    return false;
  }
}


