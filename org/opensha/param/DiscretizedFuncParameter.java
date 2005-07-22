package org.opensha.param;

import org.opensha.exceptions.*;
import org.opensha.data.function.DiscretizedFuncAPI;

/**
 * <p>Title: DiscretizedFuncParameter.java </p>
 * <p>Description: Makes a textfield to enter X and Y values</p>
 * @author : Nitin Gupta and Vipin Gupta
 * @created : April 01,2004
 * @version 1.0
 */

public class DiscretizedFuncParameter extends DependentParameter
    implements java.io.Serializable{


  /** Class name for debugging. */
  protected final static String C = "DiscretizedFuncParameter";
  /** If true print out debug statements. */
  protected final static boolean D = false;

  protected final static String PARAM_TYPE ="DiscretizedFuncParameter";




  /**
   *  No constraints specified for this parameter. Sets the name of this
   *  parameter.
   *
   * @param  name  Name of the parameter
   */
  public DiscretizedFuncParameter(String name) {
    super(name,null,null,null);
  }


  /**
   * No constraints specified, all values allowed. Sets the name and value.
   *
   * @param  name   Name of the parameter
   * @param  discretizedFunc  DiscretizedFunc  object
   */
  public DiscretizedFuncParameter(String name, DiscretizedFuncAPI discretizedFunc){
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

    if ( !( obj instanceof DiscretizedFuncAPI ) ) {
      throw new ClassCastException( S + "Object not a DiscretizedFuncAPI, unable to compare" );
    }

    DiscretizedFuncParameter param = ( DiscretizedFuncParameter ) obj;

    if( ( this.value == null ) && ( param.value == null ) ) return 0;
    int result = 0;

    DiscretizedFuncAPI n1 = ( DiscretizedFuncAPI) this.getValue();
    DiscretizedFuncAPI n2 = ( DiscretizedFuncAPI ) param.getValue();

   if(n1.equals(n2)) return 0;
   else return -1;
  }



  /**
   * Set's the parameter's value, which is basically a DiscretizedFunction.
   *
   * @param  value                 The new value for this Parameter
   * @throws  ParameterException   Thrown if the object is currenlty not
   *      editable
   */
  public void setValue( DiscretizedFuncAPI value ) throws ParameterException {
    setValue( (Object) value );
  }


  /*  This function just checks that we only allow an object of DiscretizedFuncAPI.
   *
   * @param  obj  Object to check if allowed via constraints
   * @return      True if the value is allowed
   */
  public boolean isAllowed(Object obj) {
    if(obj instanceof DiscretizedFuncAPI) return true;
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

    if (! (obj instanceof DiscretizedFuncParameter)) {
      throw new ClassCastException(S +
          "Object not a DiscretizedFuncAPI, unable to compare");
    }
    return ((DiscretizedFuncAPI)value).equals(((DiscretizedFuncParameter)obj).value);
  }

  /**
   *  Returns a copy so you can't edit or damage the origial.
   *
   * @return    Exact copy of this object's state
   */
  public Object clone(){

      DiscretizedFuncParameter param = null;
      if( value == null ) param = new DiscretizedFuncParameter( name);
      else param = new DiscretizedFuncParameter(name,((DiscretizedFuncAPI)value).deepClone());
      if( param == null ) return null;
      param.editable = true;
      return param;
  }


  /**
   *
   * @returns the DiscretizedFuncAPI contained in this parameter
   */
  public DiscretizedFuncAPI getParameter(){
    return (DiscretizedFuncAPI)getValue();
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


