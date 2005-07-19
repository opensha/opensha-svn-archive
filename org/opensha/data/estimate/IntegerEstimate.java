package org.opensha.data.estimate;

import org.opensha.data.function.ArbitrarilyDiscretizedFunc;

/**
 * <p>Title: IntegerEstimate.java </p>
 * <p>Description:  This can be used to specify probabilities associated with
 * discrete values from an ArbitrarilyDiscretizedFunction. the discrete Values
 * should be integer values.
 *
 * </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class IntegerEstimate extends DiscreteValueEstimate{
  public final static String NAME  =  "Integer";

  private final static String EST_MSG_X_INTEGER = "All X values should be an integer "+
     " for Integer Estimate";

 /**
  * Constructor - Accepts DiscretizedFunc & an indication of whether it's
  * already normized. It checks that the X values in the function are integers
  * (or withing tolerance of integers)
  *
  * @param func DiscretizedFunc containing the X and Y values
  */
 public IntegerEstimate(ArbitrarilyDiscretizedFunc func, boolean isNormalized) {
   super(func, isNormalized);
   checkValues();

 }

 /**
  * It checks whether x values are indeed integers:
  *
  * @param func ArbitrarilyDiscretizedFunc containing the X and Y values
  */
 public void checkValues() {
   double diff= 0, x;
   if(!func.areAllXValuesInteger(this.tol)) throw new InvalidParamValException(EST_MSG_X_INTEGER);
 }

 public String getName() {
   return NAME;
 }


}
