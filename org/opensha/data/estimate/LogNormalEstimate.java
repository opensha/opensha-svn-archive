package org.opensha.data.estimate;

/**
 * <p>Title: LogNormalEstimate.java  </p>
 * <p>Description: The rules followed for this estimate are :
 *  1. Std Dev should be >=0
 *  2. LinearMedian should be >=0
 * </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class LogNormalEstimate
    extends Estimate {
  public final static String NAME = "org.opensha.data.estimate.LogNormalEstimate";
  private double linearMedian;
  private double stdDev;
  // flag to specify whether it will be base10 or natural log
  private boolean isBase10 = true;
  private final static String MSG_INVALID_STDDEV =
      "Invalid value for std dev. in Log Normal Estimate. It should be >=0";
  private final static String MSG_INVALID_MEDIAN =
      "Invalid value for median in Log Normal Estimate. It should be >=0";

  /**
   * Constructor - set the linear median and standard deviation.
   * For allowed values of median and stdDev, check their respective setValue
   * function documentation
   *
   * @param linearMedian
   * @param stdDev
   */
  public LogNormalEstimate(double linearMedian, double stdDev) {
    setMedian(linearMedian);
    setStdDev(stdDev);
  }

  /**
   * Set the linear median . Median should be > 0 else InvalidParamValException
   * is thrown
   *
   * @param median linear median for this estimate
   */
  public void setMedian(double median) {
    if (median < 0)
      throw new InvalidParamValException(MSG_INVALID_MEDIAN);
    this.linearMedian = median;
  }

  /**
   * Return the median for this distribution
   *
   * @return
   */
  public double getMedian() {
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
  * Get the minimum among the list of X values in this list. Always returns
  * Double.NEGATIVE_INFINITY for this case.
  *
  * @return
  */
 public double getMinXValue() {
   return Double.NEGATIVE_INFINITY;
 }

 /**
  * Get the maximum among the list of X values in this list. Always returns
  * Double.POSITIVE_INFINITY for this case
  *
  * @return
  */
 public double getMaxXValue() {
   return Double.POSITIVE_INFINITY;
 }



  public double getMean() {
    throw new java.lang.UnsupportedOperationException(
        "Method getMean() not yet implemented.");
  }

  public double getFractile(double prob) {
    throw new java.lang.UnsupportedOperationException(
        "Method getFractile() not yet implemented.");
  }


  public double getMode() {
    throw new java.lang.UnsupportedOperationException(
        "Method getMode() not yet implemented.");
  }

}
