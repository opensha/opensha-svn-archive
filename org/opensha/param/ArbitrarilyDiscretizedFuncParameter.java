package org.opensha.param;

import org.opensha.exceptions.*;
import org.opensha.data.function.DiscretizedFuncAPI;
import org.opensha.data.function.ArbitrarilyDiscretizedFunc;

/**
 * <p>Title: ArbitrarilyDiscretizedFuncParameter.java </p>
 * <p>Description: Makes a textfield to enter X and Y values</p>
 * @author : Nitin Gupta and Vipin Gupta
 * @created : April 01,2004
 * @version 1.0
 */

public class ArbitrarilyDiscretizedFuncParameter extends DependentParameter
    implements java.io.Serializable{


  /** Class name for debugging. */
  protected final static String C = "ArbitrarilyDiscretizedFuncParameter";
  /** If true print out debug statements. */
  protected final static boolean D = false;

  protected final static String PARAM_TYPE ="ArbitrarilyDiscretizedFuncParameter";




  /**
   * No constraints specified, all values allowed. Sets the name and value.
   *
   * @param  name   Name of the parameter
   * @param  discretizedFunc  DiscretizedFunc  object
   */
  public ArbitrarilyDiscretizedFuncParameter(String name, ArbitrarilyDiscretizedFunc discretizedFunc){
    super(name,null,null,discretizedFunc);

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

    if ( !( obj instanceof ArbitrarilyDiscretizedFunc ) ) {
      throw new ClassCastException( S + "Object not a DiscretizedFuncAPI, unable to compare" );
    }

    ArbitrarilyDiscretizedFuncParameter param = ( ArbitrarilyDiscretizedFuncParameter ) obj;

    if( ( this.value == null ) && ( param.value == null ) ) return 0;
    int result = 0;

    ArbitrarilyDiscretizedFunc n1 = ( ArbitrarilyDiscretizedFunc) this.getValue();
    ArbitrarilyDiscretizedFunc n2 = ( ArbitrarilyDiscretizedFunc ) param.getValue();

   if(n1.equals(n2)) return 0;
   else return -1;
  }



  /**
   * Set's the parameter's value, which is basically a ArbitrarilyDiscretizedFunc.
   *
   * @param  value                 The new value for this Parameter
   * @throws  ParameterException   Thrown if the object is currenlty not
   *      editable
   */
  public void setValue( ArbitrarilyDiscretizedFunc value ) throws ParameterException {
    setValue( (Object) value );
  }


  /*  This function just checks that we only allow an object of ArbitrarilyDiscretizedFunc.
   *
   * @param  obj  Object to check if allowed via constraints
   * @return      True if the value is allowed
   */
  public boolean isAllowed(Object obj) {
    if(obj == null && this.isNullAllowed()) return true;
    if(obj instanceof ArbitrarilyDiscretizedFunc) return true;
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

    if (! (obj instanceof ArbitrarilyDiscretizedFuncParameter)) {
      throw new ClassCastException(S +
          "Object not a DiscretizedFuncAPI, unable to compare");
    }
    return ((ArbitrarilyDiscretizedFunc)value).equals(((ArbitrarilyDiscretizedFuncParameter)obj).value);
  }

  /**
   *  Returns a copy so you can't edit or damage the origial.
   *
   * @return    Exact copy of this object's state
   */
  public Object clone(){

      ArbitrarilyDiscretizedFuncParameter param = null;
      param = new ArbitrarilyDiscretizedFuncParameter(name,(ArbitrarilyDiscretizedFunc)((ArbitrarilyDiscretizedFunc)value).deepClone());
      if( param == null ) return null;
      param.editable = true;
      return param;
  }


  /**
   *
   * @returns the ArbitrarilyDiscretizedFunc contained in this parameter
   */
  public ArbitrarilyDiscretizedFunc getParameter(){
    return (ArbitrarilyDiscretizedFunc)getValue();
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


