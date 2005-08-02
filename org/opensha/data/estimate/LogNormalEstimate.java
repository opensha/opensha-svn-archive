package org.opensha.data.estimate;
import org.opensha.calc.GaussianDistCalc;
import org.opensha.data.function.DiscretizedFunc;
import org.opensha.data.function.ArbitrarilyDiscretizedFunc;

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


  public double getMean() {
    throw new java.lang.UnsupportedOperationException(
        "Method getMean() not yet implemented.");
  }

  /**
   * NED NEEDS TO CHECK THIS TO MAKE SURE THIS IS IMPLEMENTED CORRECTLY
   *
   * @param prob - probability value
   */
 public double getFractile(double prob) {
   double stdRndVar = GaussianDistCalc.getStandRandVar(prob, getTruncLevel(minX),
       getTruncLevel(maxX), 1e-6);
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
   * get the truncation level
   * @param val
   * @return
   */
  private double getTruncLevel(double val) {
    if(val==Double.NEGATIVE_INFINITY) return 0;
    else if(val==Double.POSITIVE_INFINITY) return Double.POSITIVE_INFINITY;
    else return getLogVal(val/linearMedian)/stdDev;
  }




  public double getMode() {
    throw new java.lang.UnsupportedOperationException(
        "Method getMode() not yet implemented.");
  }

  public double getMedian() {
    return 0.0;
  }

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
  * maxX=linearMedian*Math.exp(4*stdDev), numX=80
  *
  * @return
  */

 public DiscretizedFunc getPDF_Test() {
    ArbitrarilyDiscretizedFunc func = new ArbitrarilyDiscretizedFunc();
    double minX = 0;
    double maxX = linearMedian*getUnLogVal(4*stdDev);
    int numPoints = 80;
    double deltaX = (getLogVal(maxX)-minX)/numPoints;
    for(double x=minX; x<=maxX;) {
      func.set(x, getY(x));
      if(x!=0) x = getUnLogVal(getLogVal(x)+deltaX);
      else x = getUnLogVal(deltaX);
    }
    return func;
  }


  private double getY(double x) {
    if(x==0) return 0;
    return getUnLogVal(-Math.pow(getLogVal(x/linearMedian),2)/(2*stdDev*stdDev))/(x*stdDev*Math.sqrt(2*Math.PI));
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
