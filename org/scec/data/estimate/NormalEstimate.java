package org.scec.data.estimate;

/**
 * <p>Title: NormalEstimate.java  </p>
 * <p>Description: rules followed are:
 * 1. StdDEv should be >=0
 *
 * </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class NormalEstimate implements EstimateAPI {
  private double mean;
  private double stdDev;
  private String comments;
  private final static String MSG_INVALID_STDDEV =
      "Invalid value for std dev. in Normal Estimate. It should be >=0";

  public NormalEstimate(double mean, double stdDev) {
    this.mean = mean;
    this.stdDev = stdDev;
  }

  public void setMean(double mean) {
    this.mean = mean;
  }

  public double getMean() {
    return mean;
  }


  public void setStdDev(double stdDev) {
    if(stdDev<0) throw new InvalidParamValException(MSG_INVALID_STDDEV);
    this.stdDev = stdDev;
  }

  public double getStdDev() {
    return stdDev;
  }

  public double getFractile(double prob) {
    /**@todo Implement this org.scec.data.estimate.EstimateAPI method*/
    throw new java.lang.UnsupportedOperationException("Method getFractile() not yet implemented.");
  }

  public double getMedian() {
    /**@todo Implement this org.scec.data.estimate.EstimateAPI method*/
    throw new java.lang.UnsupportedOperationException(
        "Method getMedian() not yet implemented.");
  }

  public void setComments(String comments) {
   this.comments  = comments;
 }

 public String getComments() {
   return this.comments;
 }

}