package org.opensha.data.estimate;

/**
 * <p>Title: NormalEstimate.java  </p>
 * <p>Description:  This represents a Normal Distribution defined by a
 * mean and standard deviation (the latter must be positive).
 * The minimum and maximum X values serve to truncate the distribution, such that
 * probabilities are zero below and above these values, respectively (the defaults
 * are +/- infinity).
 *
 * </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class NormalEstimate extends Estimate {
  public final static String NAME = "org.opensha.data.estimate.NormalEstimate";
  private double mean=Double.NaN;
  private double stdDev=Double.NaN;

  /**
   * Default constructor - accepts mean and standard deviation.
   *
   * @param mean
   * @param stdDev
   */
  public NormalEstimate(double mean, double stdDev) {
    setMean(mean);
    setStdDev(stdDev);
    this.minX=Double.NEGATIVE_INFINITY;
    this.maxX=Double.POSITIVE_INFINITY;
  }


  /**
   * This accepts minimum and maximum x-axis values that will be used as trunctions.
   *
   * @param mean
   * @param stdDev
   */
  public NormalEstimate(double mean, double stdDev, double minX, double maxX) {
    setMean(mean);
    setStdDev(stdDev);
    this.setMinMaxX(minX,maxX);
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
   * This method remains to be implemented
   *
   * @param prob - probability value
   * @todo implement this
   */
 public double getFractile(double prob) {
   throw new java.lang.UnsupportedOperationException("Not yet implemented.");
 }

}
