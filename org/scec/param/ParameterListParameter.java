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

public class ParameterListParameter extends DependentParameter
    implements java.io.Serializable{


  /** Class name for debugging. */
  protected final static String C = "ParameterListParameter";
  /** If true print out debug statements. */
  protected final static boolean D = false;

  protected final static String PARAM_TYPE ="ParameterListParameter";




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
    //setting the independent Param List for this parameter
    setIndependentParameters(paramList);
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
   * Set's the parameter's value, which is basically a parameterList.
   *
   * @param  value                 The new value for this Parameter
   * @throws  ParameterException   Thrown if the object is currenlty not
   *      editable
   */
  public void setValue( ParameterList value ) throws ParameterException {

    ListIterator it  = value.getParametersIterator();
    while(it.hasNext()){
      ParameterAPI param = (ParameterAPI)it.next();
    }
    setValue( (Object) value );
    //setting the independent Param List for this parameter
    this.setIndependentParameters(value);
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
   *
   * @returns the parameterList contained in this parameter
   */
  public ParameterList getParameter(){
    return (ParameterList)getValue();
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
   * @returns the matadata string for parameter.
   * This function returns the metadata which can be used to reset the values
   * of the parameters created.
   * *NOTE : Look at the function getMetadataXML() which return the values of
   * these parameters in the XML format and can used recreate the parameters
   * from scratch.
   */
  public String getMetadataString() {
    StringBuffer metadata = new StringBuffer();
    metadata.append(getName()+" [ ");
    ListIterator list = getIndependentParametersIterator();
    while(list.hasNext()){
     ParameterAPI tempParam = (ParameterAPI)list.next();
     metadata.append(tempParam.getMetadataString()+" ; ");
    }
    metadata.replace(metadata.length()-2,metadata.length()," ]");
    return metadata.toString();
  }


}


