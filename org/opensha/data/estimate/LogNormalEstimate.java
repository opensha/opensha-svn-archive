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
   double logMedian = Math.log(linearMedian);
   if(this.isBase10) return Math.pow(10, logMedian/Math.log(10) + stdRndVar*stdDev);
   else return Math.exp( logMedian + stdRndVar*stdDev);
 }

  /**
   * get the truncation level
   * @param val
   * @return
   */
  private double getTruncLevel(double val) {
    if(Double.isInfinite(val)) return 0;
    else return (val-linearMedian)/stdDev;
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

 public DiscretizedFunc getPDF() {
    ArbitrarilyDiscretizedFunc func = new ArbitrarilyDiscretizedFunc();
    double x, y, deltaX=stdDev/4;
    double limit = 1e-12;
    for(int i=0; ; ++i) {
       x = linearMedian - i*deltaX;
       if(x>0) func.set(x,getY(x));
       x= linearMedian + i*deltaX;
       y = getY(x);
       func.set(x,getY(x));
       if(y<=limit) break;
    }
    return func;
  }


  private double getY(double x) {
    return Math.exp(-Math.pow(Math.log(x)-linearMedian,2)/2*stdDev*stdDev)/x*stdDev*Math.sqrt(2*Math.PI);
  }

  /**
  * Get the cumulative distribution function
  * @return
  */
 public DiscretizedFunc getCDF() {
   ArbitrarilyDiscretizedFunc func = new ArbitrarilyDiscretizedFunc();
   return func;
 }



}
