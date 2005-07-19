package org.opensha.data.estimate;
import org.opensha.calc.GaussianDistCalc;

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
   double stdRndVar = GaussianDistCalc.getStandRandVar(prob, 0, 0, 1e-6);
   double logMedian = Math.log(linearMedian);
   if(this.isBase10) return Math.pow(10, logMedian/Math.log(10) + stdRndVar*stdDev);
   else return Math.exp( logMedian + stdRndVar*stdDev);
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


}
