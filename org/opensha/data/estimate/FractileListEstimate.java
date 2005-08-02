package org.opensha.data.estimate;

import org.opensha.data.function.ArbDiscrEmpiricalDistFunc;
import org.opensha.data.function.DiscretizedFuncAPI;
import org.opensha.data.function.DiscretizedFunc;
import org.opensha.data.function.ArbitrarilyDiscretizedFunc;

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
  public final static String NAME  =  "Min, Max and Preferred";
  private ArbDiscrEmpiricalDistFunc func=null;
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
     maxX = func.getMaxX();
     minX = func.getMinX();
     int numValues = func.getNum();
     // check that 0²Y²1
     double y;
     for(int i = 0; i<numValues;++i) {
       y = func.getY(i);
       if(y<0 || y>1) throw new InvalidParamValException(EST_MSG_INVLID_RANGE);
     }
     this.func = (ArbDiscrEmpiricalDistFunc)func.deepClone();
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
    * the interpolated X value (this object requires that a median exist).
    *
    * @return median value for this set of X and Y values
    */
   public double getMedian() {
     // check that median is defined
     int numValues = func.getNum();
     if(numValues==1 && func.getY(0)!=0.5)
       throw new InvalidParamValException(MEDIAN_UNDEFINED);
     else if(numValues>1 && (func.getY(0)>0.5 || func.getY(numValues-1)<0.5))
       throw new InvalidParamValException(MEDIAN_UNDEFINED);
     return func.getDiscreteFractile(0.5);
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

  public String getName() {
   return NAME;
 }

 public DiscretizedFunc getPDF_Test() {
   return this.func;
 }

 /**
  * Get the cumulative distribution function
  * @return
  */
 public DiscretizedFunc getCDF_Test() {
   ArbitrarilyDiscretizedFunc func = new ArbitrarilyDiscretizedFunc();
   return func;
 }

 /**
    * Get the probability for that the true value is less than or equal to provided
    * x value
    *
    * @param x
    * @return
    */
   public  double getProbLessThanEqual(double x) {
      throw new java.lang.UnsupportedOperationException("Method getProbLessThanEqual() not supported.");
   }

   /**
    * Test function to get the CDF for this estimate. It uses the
    * getFractile() function internally. It discretizes the Y values and then
    * calls the getFractile() method to get corresponding x values and then
    * plot them.
    *
    * @return
    */
   public  DiscretizedFunc getCDF_TestUsingFractile() {
      throw new java.lang.UnsupportedOperationException("Method getCDF_TestUsingFractile() not supported.");
   }


}
