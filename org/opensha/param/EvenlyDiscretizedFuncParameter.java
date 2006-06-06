package org.opensha.param;

import org.opensha.exceptions.*;
import org.opensha.data.function.EvenlyDiscretizedFunc;

/**
 * <p>Title: EvenlyDiscretizedFuncParameter.java </p>
 * <p>Description: This parameter accepts a EvenlyDiscretizedFunc as a value.
 * It shows the GUI so that user can enter min/max/num values and then the correspoding
 * Y values for the EvelyDiscretizedFunc
 *
 *  This class  has 2 methods for setting the units :
 * 1. setUnits() : This method sets the units for Y values
 * 2. setXUnits(): This method sets the units for X values.
 * Similarly, we have 2 different methods for getting the units.
</p>
 * @author : Nitin Gupta and Vipin Gupta
 * @created : April 01,2004
 * @version 1.0
 */

public class EvenlyDiscretizedFuncParameter extends DependentParameter
    implements java.io.Serializable{


  /** Class name for debugging. */
  protected final static String C = "EvenlyDiscretizedFuncParameter";
  /** If true print out debug statements. */
  protected final static boolean D = false;

  protected final static String PARAM_TYPE ="EvenlyDiscretizedFuncParameter";
  private String xUnits="";



  /**
   * No constraints specified, all values allowed. Sets the name and value.
   *
   * @param  name   Name of the parameter
   * @param  discretizedFunc  DiscretizedFunc  object
   */
  public EvenlyDiscretizedFuncParameter(String name, EvenlyDiscretizedFunc discretizedFunc){
    super(name,null,null,discretizedFunc);

  }

  /**
   * Sets the units for X Values. To set the units for Y values, use
   * the setUnits() method
   *
   **/
  public void setXUnits(String units) throws EditableException {
    checkEditable(C + ": setUnits(): ");
    this.xUnits = units;
  }


  /**
   * Returns the units of X values for  this parameter. To get the units for
   * Y values, use getUnits() method
   * represented as a String.
   * */
  public String getXUnits() {
    return xUnits;
  }


  /**
   *  Compares the values to if this is less than, equal to, or greater than
   *  the comparing objects.
   *
   * @param  obj                     The object to compare this to
   * @return                         -1 if this value < obj value, 0 if equal,
   *      +1 if this value > obj value
   * @exception  ClassCastException  Is thrown if the comparing object is not
   *      a ParameterListParameter.
   */
  public int compareTo( Object obj ) {
    String S = C + ":compareTo(): ";

    if ( !( obj instanceof EvenlyDiscretizedFunc ) ) {
      throw new ClassCastException( S + "Object not a DiscretizedFuncAPI, unable to compare" );
    }

    EvenlyDiscretizedFuncParameter param = ( EvenlyDiscretizedFuncParameter ) obj;

    if( ( this.value == null ) && ( param.value == null ) ) return 0;
    int result = 0;

    EvenlyDiscretizedFunc n1 = ( EvenlyDiscretizedFunc) this.getValue();
    EvenlyDiscretizedFunc n2 = ( EvenlyDiscretizedFunc ) param.getValue();

   if(n1.equals(n2)) return 0;
   else return -1;
  }



  /**
   * Set's the parameter's value, which is basically a EvenlyDiscretizedFunc.
   *
   * @param  value                 The new value for this Parameter
   * @throws  ParameterException   Thrown if the object is currenlty not
   *      editable
   */
  public void setValue( EvenlyDiscretizedFunc value ) throws ParameterException {
    setValue( (Object) value );
  }


  /*  This function just checks that we only allow an object of EvenlyDiscretizedFunc.
   *
   * @param  obj  Object to check if allowed via constraints
   * @return      True if the value is allowed
   */
  public boolean isAllowed(Object obj) {
    if(obj == null && this.isNullAllowed()) return true;
    if(obj instanceof EvenlyDiscretizedFunc) return true;
    else return false;
  }


  /**
   * Compares value to see if equal.
   *
   * @param  obj                     The object to compare this to
   * @return                         True if the values are identical
   * @exception  ClassCastException  Is thrown if the comparing object is not
   *      a LocationListParameter.
   */
  public boolean equals(Object obj) {
    String S = C + ":equals(): ";

    if (! (obj instanceof EvenlyDiscretizedFunc)) {
      throw new ClassCastException(S +
          "Object not a DiscretizedFuncAPI, unable to compare");
    }
    return ((EvenlyDiscretizedFunc)value).equals(((EvenlyDiscretizedFuncParameter)obj).value);
  }

  /**
   *  Returns a copy so you can't edit or damage the origial.
   *
   * @return    Exact copy of this object's state
   */
  public Object clone(){

      EvenlyDiscretizedFuncParameter param = null;
      param = new EvenlyDiscretizedFuncParameter(name,(EvenlyDiscretizedFunc)((EvenlyDiscretizedFunc)value).deepClone());
      if( param == null ) return null;
      param.editable = true;
      param.info = info;
      return param;
  }


  /**
   *
   * @returns the EvenlyDiscretizedFunc contained in this parameter
   */
  public EvenlyDiscretizedFunc getParameter(){
    return (EvenlyDiscretizedFunc)getValue();
  }

  /**
   * Returns the name of the parameter class
   */
  public String getType() {
    String type = this.PARAM_TYPE;
    return type;
  }

  /**
   * This overrides the getmetadataString() method because the value here
   * does not have an ASCII representation (and we need to know the values
   * of the independent parameter instead).
   * @returns Sstring
   */
  public String getMetadataString() {
    return getDependentParamMetadataString();
  }


}


