package org.scec.sha.param;


import org.scec.sha.surface.EvenlyGriddedSurface;
import org.scec.sha.param.*;

/**
 * <p>Title: SimpleFaultParameter</p>
 * <p>Description: This is a more general parameter than the simple fault.
 * Actually it creates an object for the EvenlyGriddedSurface</p>
 * @author : Edward Field, Nitin Gupta and Vipin Gupta
 * @created : July 30, 2003
 * @version 1.0
 */

public class SimpleFaultParameter extends SimpleFaultParameterCalculator {

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
    super(name);
  }

  /**
   * No constraints specified, all values allowed. Sets the name and value.
   *
   * @param  name   Name of the parameter
   * @param  surface  EvenlyGriddedSurface  object
   */
  public SimpleFaultParameter(String name, EvenlyGriddedSurface surface){
    super(name,surface);
  }

  /**
   * Returns the name of the parameter class
   */
  public String getType() {
    String type = C;
    return type;
  }
}


