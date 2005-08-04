package org.opensha.data.estimate;
import org.opensha.calc.GaussianDistCalc;
import org.opensha.data.function.DiscretizedFunc;
import org.opensha.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.data.function.EvenlyDiscretizedFunc;

/**
 * <p>Title: LogNormalEstimate.java  </p>
 * <p>Description: This exstimate assumes a log-normal distribution.  The linear-median,
 * and standard deviation must be positive, and minX and maxX can only be 0.0 and Infinity,
 * respectively (at least for now.  One must also specify
 * whether natural or base-10 log is assumed.
 * </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class LogNormalEstimate extends Estimate {
  public final static String NAME  =  "Log Normal";

  private double linearMedian;
  private double stdDev;
  // flag to specify whether it will be base10 or natural log
  private boolean isBase10 = true;
  private final static String MSG_INVALID_MEDIAN = "Error: linear-median must be positive.";
  private final static String MSG_INVALID_MINMAX =
      "Error: the minimum and maximum X-axis values can only be 0.0 and  Infinity, respectively.";
  private final static double LOG10_VAL = Math.log(10.0);

  /**
   * Constructor - set the linear median and standard deviation.
   * For allowed values of median and stdDev, check their respective setValue
   * function documentation
   *
   * @param linearMedian
   * @param stdDev
   */
  public LogNormalEstimate(double linearMedian, double stdDev) {
    setLinearMedian(linearMedian);
    setStdDev(stdDev);
    minX = 0.0;
    maxX = Double.POSITIVE_INFINITY;
  }



  /**
   * Set the linear median . Median should be > 0 else InvalidParamValException
   * is thrown
   *
   * @param median linear median for this estimate
   */
  public void setLinearMedian(double median) {
    if (median < 0)
      throw new InvalidParamValException(MSG_INVALID_MEDIAN);
    this.linearMedian = median;
  }

  /**
   * Return the median for this distribution
   *
   * @return
   */
  public double getLinearMedian() {
    return linearMedian;
  }

  /**
   * Set the standard deviation. It should be >=0 else InvalidParamValException
   * is thrown
   *
   * @param stdDev
   */
  public void setStdDev(double stdDev) {
    if (stdDev < 0)
      throw new InvalidParamValException(MSG_INVALID_STDDEV);
    this.stdDev = stdDev;
  }

  /**
   * Get the standard deviation
   *
   * @return
   */
  public double getStdDev() {
    return stdDev;
  }

  /**
   * Whether we are using natural log or log to base 10 for this
   *
   * @return True if we are using log to base of 10, returns false if natural
   * log is being used
   */
  public boolean getIsBase10() {
    return this.isBase10;
  }

  /**
   * set whether to use natural log or log to base 10
   *
   * @param isBase10 true if you user wants to use log to base 10, false if
   * natural log is desired
   */
  public void setIsBase10(boolean isBase10) {
    this.isBase10 = isBase10;
  }

  /**
   * Return the mean
   * @return
   */
  public double getMean() {
    throw new java.lang.UnsupportedOperationException(
        "Method getMean() not yet implemented.");
  }

  /**
  *
  * Returns the max x value such that probability of occurrence of this x value
  * is <=prob
  *
  * @param prob - probability value
  */
public double getFractile(double prob) {
  /**
   * NOTE: In the statement below, we have to use (1-prob) because GaussianDistCalc
   * accepts the probability of exceedance as the parameter
   */
   double stdRndVar = GaussianDistCalc.getStandRandVar(1-prob, getStandRandVar(minX),
       getStandRandVar(maxX), 1e-6);
   return getUnLogVal(getLogVal(linearMedian) + stdRndVar*stdDev);
 }

 /**
  * It gets the log value for passed in "val". This checks whether the log normal estimate
  * is for base 10 or for base E and then returns the log value based on it.
  *
  * @param val
  * @return
  */
 private double getLogVal(double val) {
   double logVal = Math.log(val);
   if(this.isBase10) return logVal/LOG10_VAL;
   else return logVal;
 }

 /**
  * It unlogs the value. It checks whether this estimate is based on base 10 or
  * base E and unlogs the value depending on that.
  *
  * @param val Value in log domain
  * @return
  */
 private double getUnLogVal(double logVal) {
   if(this.isBase10) return Math.pow(10, logVal);
   else return Math.exp(logVal);
 }

  /**
   * get the standard random variable
   *
   * @param val
   * @return
   */
  private double getStandRandVar(double val) {
    if(val==Double.NEGATIVE_INFINITY) return 0;
    else if(val==Double.POSITIVE_INFINITY) return Double.POSITIVE_INFINITY;
    else return getLogVal(val/linearMedian)/stdDev;
  }


  /**
   * Get the mode
   * @return
   */
  public double getMode() {
    throw new java.lang.UnsupportedOperationException(
        "Method getMode() not yet implemented.");
  }

  /**
   * Get the median
   * @return
   */
  public double getMedian() {
    return 0.0;
  }

  /**
   * Get the name of this estimate. This is the name displayed to the user
   * @return
   */
  public String getName() {
   return NAME;
 }

 /**
  * Set the minimum and maximum X-axis value
  *
  * @param minX double
  * @param maxX double
  */
 public void setMinMaxX(double minX, double maxX) {
   if(maxX < minX) throw new InvalidParamValException(EST_MSG_MAX_LT_MIN);
   if(minX<0 || maxX<0) throw new InvalidParamValException(MSG_INVALID_MINMAX);
   this.maxX = maxX;
   this.minX = minX;
 }

 /**
  * Get the probability density function.
  * It calculates the PDF for x values.
  * The PDF is calculated for evenly discretized X values with minX=0,
  * maxX=linearMedian*Math.exp(4*stdDev), numX=160
  *
  * @return
  */

 public DiscretizedFunc getPDF_Test() {
   EvenlyDiscretizedFunc func = getEvenlyDiscretizedFunc();
   double deltaX = func.getDelta();
   int numPoints = func.getNum();
   double x;
   for(int i=0; i<numPoints; ++i) {
     x = func.getX(i);
     if((x - deltaX / 2)<=0) // log values does not exist for negative values
       func.set(i, getProbLessThanEqual(x + deltaX / 2));
     else func.set(i, getProbLessThanEqual(x + deltaX / 2) - getProbLessThanEqual(x - deltaX / 2));
   }
   func.setInfo("PDF from LogNormal Distribution");
   return func;
  }



 /**
  * Get the cumulative distribution function
  * @return
  */
 public DiscretizedFunc getCDF_Test() {
   EvenlyDiscretizedFunc func = getEvenlyDiscretizedFunc();
   double deltaX = func.getDelta();
   int numPoints = func.getNum();
   double x;
   for(int i=0; i<numPoints; ++i)
     func.set(i, getProbLessThanEqual(func.getX(i)));
   func.setInfo("CDF from LogNormal Distribution using getProbLessThanEqual() method");
   return func;
 }


 /**
  * Make the Evenly discretized function for use in getPDF_Test() and getCDF_Test()
  * @return
  */
 private EvenlyDiscretizedFunc getEvenlyDiscretizedFunc() {
   double minX = 0;
   double maxX = linearMedian*getUnLogVal(3*stdDev);
   int numPoints = 320;
   EvenlyDiscretizedFunc func = new EvenlyDiscretizedFunc(minX, maxX, numPoints);
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
    return (1-GaussianDistCalc.getExceedProb(getStandRandVar(x), getStandRandVar(minX),
       getStandRandVar(maxX)));
  }


}
