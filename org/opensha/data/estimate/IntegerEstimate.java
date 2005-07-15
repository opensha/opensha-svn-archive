package org.opensha.data.estimate;

import org.opensha.data.function.DiscretizedFunc;

/**
 * <p>Title: IntegerEstimate.java </p>
 * <p>Description: the rules followed here:
 * 1. X(i) is integer value.
 * 2. Y(i)>=0.
 *
 * </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class IntegerEstimate extends DiscreteValueEstimate{
  public final static String NAME = "org.opensha.data.estimate.IntegerEstimate";

  private final static String EST_MSG_X_INTEGER = "All X values should be an integer "+
     " for Integer Estimate";

 /**
  * Constructor - Accepts DiscretizedFunc & an indication of whether it's
  * already normized. It checks that the X values in the function are integers
  * (or withing tolerance of integers)
  *
  * @param func DiscretizedFunc containing the X and Y values
  */
 public IntegerEstimate(DiscretizedFunc func, boolean isNormalized) {
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
   for(int i = 0; i<func.getNum();++i) {
     x = func.getX(i);
     diff  = Math.abs(x-Math.rint(x));
     if(diff > tol) throw new InvalidParamValException(EST_MSG_X_INTEGER);
   }
 }

}
