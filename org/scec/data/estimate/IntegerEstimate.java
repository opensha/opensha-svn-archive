package org.scec.data.estimate;

import org.scec.data.function.ArbitrarilyDiscretizedFunc;

/**
 * <p>Title: IntegerEstimate.java </p>
 * <p>Description: the rules followed here:
 * 1. X(i) is integer value.
 * 2. Y(i)>=0.
 * 3. First and Last Y values are 0
 * 4. X are increasing
 *
 * </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class IntegerEstimate extends PDF_Estimate{
  private ArbitrarilyDiscretizedFunc func=null;
 private String comments;
 private final static String MSG_FIRST_LAST_Y_ZERO = "First and Last Y values "+
     "should be 0 for Integer Estimate";
 private final static String MSG_X_INCREASING = "X values should be increasing "+
     " for Integer Estimate";
 private final static String MSG_X_INTEGER = "All X values should be an integer "+
     " for Integer Estimate";
 private final static String MSG_Y_POSITIVE = "All the Y values should be >= 0 "+
     "for Integer Estimate";

 public IntegerEstimate(ArbitrarilyDiscretizedFunc func) {
   super(func);
   checkValues(func);

 }

 /**
  * First and Last Y  should be qual to 0
  * All Y >=0
  * X should be increasing
  *
  * @param func
  */
 public void checkValues(ArbitrarilyDiscretizedFunc func) {
   if(func.getY(0)!=0 || func.getY(func.getNum()-1)!=0)
     throw new InvalidParamValException(MSG_FIRST_LAST_Y_ZERO);
   double x= Double.NEGATIVE_INFINITY;
   for(int i = 0; i<func.getNum();++i) {
     if(func.getX(i)<x) throw new InvalidParamValException(MSG_X_INCREASING);
     x  = func.getX(i);
     if(Math.floor(x)!=x) throw new InvalidParamValException(MSG_X_INTEGER);
     if(func.getY(i)<0) throw new InvalidParamValException(MSG_Y_POSITIVE);
   }
 }

}