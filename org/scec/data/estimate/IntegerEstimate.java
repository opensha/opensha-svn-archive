package org.scec.data.estimate;

import org.scec.data.function.ArbitrarilyDiscretizedFunc;

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

public class IntegerEstimate extends PDF_Estimate{
  public final static String NAME = "org.scec.data.estimate.IntegerEstimate";
  private final static String MSG_X_INCREASING = "X values should be increasing "+
     " for Integer Estimate";
  private final static String MSG_X_INTEGER = "All X values should be an integer "+
     " for Integer Estimate";
  private final static String MSG_Y_POSITIVE = "All the Y values should be >= 0 "+
     "for Integer Estimate";

 /**
  * Constructor - Accepts ArbitrarilyDiscretizedFunc. It checks that the values
  * provided are valid by calling the checkValues function.
  *
  * @param func ArbitrarilyDiscretizedFunc containing the X and Y values
  */
 public IntegerEstimate(ArbitrarilyDiscretizedFunc func) {
   super(func);
   checkValues(this.func);

 }

 /**
  * It checks that user has provided the correct X and Y values according to
  * following constraints:
  *
  * 1. All Y >=0
  * 2. X(i) is integer value.
  *
  * @param func ArbitrarilyDiscretizedFunc containing the X and Y values
  */
 public void checkValues(ArbitrarilyDiscretizedFunc func) {
   double x= 0;
   for(int i = 0; i<func.getNum();++i) {
     x  = func.getX(i);
     if(Math.floor(x)!=x) throw new InvalidParamValException(MSG_X_INTEGER);
     if(func.getY(i)<0) throw new InvalidParamValException(MSG_Y_POSITIVE);
   }
 }

}