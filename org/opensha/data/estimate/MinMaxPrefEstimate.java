package org.opensha.data.estimate;

import org.opensha.data.function.DiscretizedFunc;

/**
 * <p>Title: MinMaxPrefEstimate.java </p>
 * <p>Description: This saves the min/max and preferred values and the corresonding
 * probabilites. Though this is not a complete estimate, this is needed for Ref Fault paramter database GUI.</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class MinMaxPrefEstimate extends Estimate{
  public final static String NAME  =  "Min, Max and Preferred";
  private double minX, maxX, prefX;
  private double minProb, maxProb, prefProb;
  private final static double tol = 1e-6;

  public MinMaxPrefEstimate(double minX, double maxX, double prefX,
                            double minProb, double maxProb, double prefProb) {

    /* check whether probabilites sum upto 1. check only if at least one of the
    probs is not Nan */
    if(!Double.isNaN(minProb) || !Double.isNaN(maxProb) ||
       !Double.isNaN(prefProb)) {
      double probSum = 0.0;
      if(!Double.isNaN(minProb)) probSum+=minProb;
      if(!Double.isNaN(maxProb)) probSum+=maxProb;
      if(!Double.isNaN(prefProb)) probSum+=prefProb;
      if (Math.abs(probSum - 1) > tol)
        throw new InvalidParamValException(EST_MSG_NOT_NORMALIZED);
    }
    this.minX = minX;
    this.maxX = maxX;
    this.prefX = prefX;
    this.minProb = minProb;
    this.maxProb = maxProb;
    this.prefProb = prefProb;
  }

  public String toString() {
    return "Estimate Type="+getName()+"\n"+
        "Min X="+minX+"\n"+
        "Prob of Min X="+minProb+"\n"+
        "Max X="+maxX+"\n"+
        "Prob of Max X="+maxProb+"\n"+
        "Pref X="+prefX+"\n"+
        "Prob of Pref X="+prefProb;
  }


  public double getMinX() { return this.minX; }
  public double getMaxX() { return this.maxX; }
  public double getPrefX() { return this.prefX; }
  public double getMinProb() { return this.minProb; }
  public double getMaxProb() { return this.maxProb; }
  public double getPrefProb() { return this.prefProb; }

  /**
   * getMean() is not supported for MinMaxPrefEstimate
   *
   * @return throws an exception specifying that this function is not supported
   */
  public double getMean() {
    throw new java.lang.UnsupportedOperationException("Method getMean() not supported");
  }


  /**
   * getMedian() is not supported for MinMaxPrefEstimate
   *
   * @return throws an exception specifying that this function is not supported
   */
  public double getMedian() {
    throw new java.lang.UnsupportedOperationException("Method getMedian() not supported.");
 }


 /**
  * getStdDev() is not supported for minMaxPrefEstimate
  *
  * @return throws an exception specifying that this function is not supported
  */
  public double getStdDev() {
    throw new java.lang.UnsupportedOperationException("Method getStdDev() not supported.");
  }


  /**
   * getFractile() is not supported for MinMaxPrefEstimate
   *
   * @return throws an exception specifying that this function is not supported
   */
  public double getFractile(double prob) {
    throw new java.lang.UnsupportedOperationException("Method getFractile() not supported.");
  }


 /**
  * getMode() is not supported for MinMaxPrefEstimate
  *
  * @return throws an exception specifying that this function is not supported
  */
 public double getMode() {
    throw new java.lang.UnsupportedOperationException("Method getMode() not supported.");
 }


 public String getName() {
   return NAME;
 }

 /**
  *
  * @return throws an exception specifying that this function is not supported
  */
 public DiscretizedFunc getPDF_Test() {
   throw new java.lang.UnsupportedOperationException("Method getPDF_Test() not supported.");
 }

 /**
  * @return throws an exception specifying that this function is not supported
  */
 public DiscretizedFunc getCDF_Test() {
   throw new java.lang.UnsupportedOperationException("Method getCDF_Test() not supported.");
 }

 /**
  *
  * @param x
  * @return throws an exception specifying that this function is not supported
  */
 public  double getProbLessThanEqual(double x) {
   throw new java.lang.UnsupportedOperationException("Method getProbLessThanEqual() not supported.");
 }

 /**
  *
  * @return throws an exception specifying that this function is not supported
  */
 public  DiscretizedFunc getCDF_TestUsingFractile() {
   throw new java.lang.UnsupportedOperationException("Method getCDF_TestUsingFractile() not supported.");
 }

}