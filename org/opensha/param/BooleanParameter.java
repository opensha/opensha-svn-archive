package org.opensha.param;

import java.util.ListIterator;

import org.dom4j.Element;


/**
 * <p>Title: BooleanParameter</p>
 * <p>Description: Makes a parameter which is a boolean</p>
 * @author : Nitin Gupta
 * @created : Dec 28, 2003
 * @version 1.0
 */

public class BooleanParameter extends DependentParameter
    implements  java.io.Serializable{


  /** Class name for debugging. */
  protected final static String C = "BooleanParameter";
  /** If true print out debug statements. */
  protected final static boolean D = false;

  protected final static String PARAM_TYPE ="BooleanParameter";



  /**
   *  No constraints specified for this parameter. Sets the name of this
   *  parameter.
   *
   * @param  name  Name of the parameter
   */
  public BooleanParameter(String name) {
    super(name,null,null,new Boolean(false));
  }

  /**
   * No constraints specified, all values allowed. Sets the name and value.
   *
   * @param  name   Name of the parameter
   * @param  paramList  ParameterList  object
   */
  public BooleanParameter(String name, Boolean value){
    super(name,null,null,value);
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

  /** Returns a copy so you can't edit or damage the original. */
  public Object clone() {
    BooleanParameter param = null;
    param = new BooleanParameter(name,(Boolean)value);
    if( param == null ) return null;
    param.editable = true;
    param.info = info;
    return param;
  }

  /**
   * Parses the XML element for a boolean value
   */
  public boolean setValueFromXMLMetadata(Element el) {
	this.setValue(Boolean.parseBoolean(el.attributeValue("value")));
	return true;
  }

}
