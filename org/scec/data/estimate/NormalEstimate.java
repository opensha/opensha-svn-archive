package org.scec.data.estimate;

/**
 * <p>Title: NormalEstimate.java  </p>
 * <p>Description:  This represents the mathematical Normal Distribution which
 * can be defined using mean and standard deviation
 * Rules followed are:
 * 1. StdDev should be >=0
 *
 * </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class NormalEstimate extends Estimate {
  public final static String NAME = "org.scec.data.estimate.NormalEstimate";
  private double mean;
  private double stdDev;
  private final static String MSG_INVALID_STDDEV =
      "Invalid value for std dev. in Normal Estimate. It should be >=0";

  /**
   * Default constructor - accepts mean and standard deviation.
   * Mean does not have any constraints.
   * Standard deviation should fullfil the constraints as specified in setStdDev
   * function
   *
   * @param mean
   * @param stdDev
   */
  public NormalEstimate(double mean, double stdDev) {
    setMean(mean);
    setStdDev(stdDev);
  }

  /**
   * Set mean for this distribution
   *
   * @param value specifying the mean for this distribution
   */
  public void setMean(double mean) {
    this.mean = mean;
  }

  /**
   * Get the mean for this distribution
   *
   * @return double value containing the mean for this distribution
   */
  public double getMean() {
    return mean;
  }


  /**
   * Set the stanndard deviation. It should be >=0 else exception
   * will be thrown
   *
   * @param stdDev standard deviation
   */
  public void setStdDev(double stdDev) {
    if(stdDev<0) throw new InvalidParamValException(MSG_INVALID_STDDEV);
    this.stdDev = stdDev;
  }

  /**
   * Return the standard deviation
   *
   * @return standard deviation for this class
   */
  public double getStdDev() {
    return stdDev;
  }


  /**
   * Get median. It should be noted that mean, median and mode
   * have same values for a normal distribution
   *
   * @return median value
   */
  public double getMedian() {
    return getMean();
  }

  /**
   * Get mode. It should be noted that mean, median and mode
   * have same values for a normal distribution
   *
   * @return mode value
   */
  public double getMode() {
   return getMean();
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


  /**
   * This method is not supported in this distribution
   *
   * @param prob
   * @return Always throws UnsupportedOperationException.
   */
 public double getFractile(double prob) {
   throw new java.lang.UnsupportedOperationException("Method getFractile() not supported.");
 }

}