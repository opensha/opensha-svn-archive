package org.opensha.data.estimate;

import org.opensha.data.function.ArbDiscrEmpiricalDistFunc;
import org.opensha.data.function.DiscretizedFuncAPI;

/**
 * <p>Title: FractileListEstimate.java </p>
 * <p>Description: This estimate is the cumulative distribution
 * This estimate can also be used if user just provides
 * min,max and preferred values which is very common.
 *
 * The rules for this etimate are:
 * 1. 1>=y>=0
 * 2. y(i+1)>=y(i)
 * 3. To ensure that median is available:
 *    If number of values==1, ensure that y =  0.5
 *    If number of values > 1, first_y<=0.5 and last_y>=0.5
 *
 * </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Ned Field, Nitin Gupta, Vipin Gupta
 * @version 1.0
 */

public class FractileListEstimate extends Estimate {
  public final static String NAME = "org.opensha.data.estimate.FractileListEstimate";
  private ArbDiscrEmpiricalDistFunc func=null;
   private final static int MIN_Y_VAL = 0;
   private final static int MAX_Y_VAL = 1;
   private final static String MSG_Y_RANGE = "All the Y values should be >= 0 "+
       "and <=1 for FractileListEstimate";
   private final static String MSG_Y_INCREASING = "Y values should be increasing "+
      " for PDF Estimate";
   private final static String MEDIAN_UNDEFINED = "Invalid Y values as median is undefined"+
       " for these set of Y values. ";


   /**
    * Construnctor - Accepts the ArbDiscrEmpiricalDistFunc of X and Y values.
    * The values specified should follow the constraints as specified in
    * the setValues() function.
    *
    * @param func ArbitrarilyDiscretizedFunc function of  X and Y values
    */
   public FractileListEstimate(ArbDiscrEmpiricalDistFunc func) {
     setValues(func);
   }

   /**
    * 1. y(i+1)>=y(i) - This is implied because ArbDiscrEmpiricalDistFunc enforces that.
    * 2. All Y >=0
    * 3. To ensure that median is available:
    *    If number of values==1, ensure that y =  0.5
    *    If number of values > 1, first_y<=0.5 and last_y>=0.5
    *
    * @param func
    */
   public void setValues(ArbDiscrEmpiricalDistFunc func) {
     double y= Double.NEGATIVE_INFINITY;
     int numValues = func.getNum();
     // this check ensures that median is defined for the fractile list
     if(numValues==1 && func.getY(0)!=0.5)
       throw new InvalidParamValException(MEDIAN_UNDEFINED);
     else if(numValues>1 && (func.getY(0)>0.5 || func.getY(numValues-1)<0.5))
       throw new InvalidParamValException(MEDIAN_UNDEFINED);
     for(int i = 0; i<numValues;++i) {
       y = func.getY(i);
       if(y<MIN_Y_VAL || y>MAX_Y_VAL) throw new InvalidParamValException(MSG_Y_RANGE);
     }
     this.func = func;
   }


   /**
    * getMean() is not supported for FractileListEstimate
    *
    * @return throws an exception specifying that this function is not supported
    */
   public double getMean() {
     throw new java.lang.UnsupportedOperationException("Method getMean() not supported");
   }


   /**
    * Returns the X value corresponding to Y = 0.5
    * If there is no Y where Y =0.5, then linear interpolation is used to find
    * the interpolated X value.
    * It is ensured that median is available by constraining  the values which
    * can be set. See setValues() function documentation for details
    *
    * @return median value for this set of X and Y values
    */
   public double getMedian() {
     return getFractile(0.5);
  }


  /**
   * getStdDev() is not supported for FractileListEstimate
   *
   * @return throws an exception specifying that this function is not supported
   */
   public double getStdDev() {
     throw new java.lang.UnsupportedOperationException("Method getStdDev() not supported.");
   }


   /**
    *
    * @param prob
    * @return
    */
   public double getFractile(double prob) {
     return func.getFirstInterpolatedX(prob);
   }

  /**
   * Get the minimum among the list of X values in this list
   *
   * @return
   */
  public double getMinXValue() {
    return func.getX(0);
  }

  /**
   * Get the maximum among the list of X values in this list
   *
   * @return
   */
  public double getMaxXValue() {
    return func.getX(func.getNum() - 1);
  }



  /**
   * getMode() is not supported for FractileListEstimate
   *
   * @return throws an exception specifying that this function is not supported
   */
  public double getMode() {
     throw new java.lang.UnsupportedOperationException("Method getMode() not supported.");
  }

  public DiscretizedFuncAPI getValues() {
    return this.func;
  }


}
