package org.scec.data.estimate;

/**
 * <p>Title: LogNormalEstimate.java  </p>
 * <p>Description: The rules folloed for this estimate are :
 *  1. Std Dev should be >=0
 *  2. LinearMedian should be >=0
 * </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class LogNormalEstimate implements EstimateAPI {

  private double linearMedian;
  private double stdDev;
  private String comments;
  private boolean isBase10 = true;
  private final static String MSG_INVALID_STDDEV =
      "Invalid value for std dev. in Log Normal Estimate. It should be >=0";
  private final static String MSG_INVALID_MEDIAN =
      "Invalid value for median in Log Normal Estimate. It should be >=0";

  public LogNormalEstimate() {
  }


  public double getMean() {
    /**@todo Implement this org.scec.data.estimate.EstimateAPI method*/
    throw new java.lang.UnsupportedOperationException("Method getMean() not yet implemented.");
  }

  public void setComments(String comments) {
    this.comments  = comments;
  }

  public String getComments() {
    return this.comments;
  }



  public void setMedian(double median) {
    if(median<0) throw new InvalidParamValException(MSG_INVALID_MEDIAN);
    this.linearMedian = linearMedian;
  }

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
     if(stdDev<0) throw new InvalidParamValException(MSG_INVALID_STDDEV);
     this.stdDev = stdDev;
   }

  public double getStdDev() { return stdDev; }

  public boolean getIsBase10() {
    return this.isBase10;
  }

 public void setIsBase10(boolean isBase10) {
   this.isBase10 = isBase10;
 }

  public double getFractile(double prob) {
    /**@todo Implement this org.scec.data.estimate.EstimateAPI method*/
    throw new java.lang.UnsupportedOperationException("Method getFractile() not yet implemented.");
  }


}