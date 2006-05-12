package org.opensha.data.estimate;

import org.opensha.data.function.DiscretizedFunc;

/**
 * <p>Title: MinMaxPrefEstimate.java </p>
 * <p>Description: This stores min, max, and preferred values, and the corresonding
 * probabilites that the true value is less than or equal to each. Though this is
 * not a complete estimate, this is needed for Ref Fault paramter database GUI.</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class MinMaxPrefEstimate extends Estimate{
  public final static String NAME  =  "Min, Max and Preferred";
  private double prefX;
  private double minProb, maxProb, prefProb;
  private final static double tol = 1e-6;
  private final static String MSG_INVALID_X_VALS = "Error: Preferred value should be >  Min &"+
                                  "\n"+"Max should be > Preferred";
  private final static String MSG_INVALID_PROB_VALS = "Error: Preferred Prob should be > Min Prob &"+
  "\n"+"Max Prob should be >  Preferred Prob";

  /**
   * @param minX
   * @param maxX
   * @param prefX
   * @param minProb
   * @param maxProb
   * @param prefProb
   */
  public MinMaxPrefEstimate(double minX, double maxX, double prefX,
                            double minProb, double maxProb, double prefProb) {

    // check that minX<=prefX<=maxX
    if(!Double.isNaN(minX) && !Double.isNaN(prefX) && minX>=prefX)
      throw new InvalidParamValException(MSG_INVALID_X_VALS);
    if(!Double.isNaN(minX) && !Double.isNaN(maxX) && minX>=maxX)
      throw new InvalidParamValException(MSG_INVALID_X_VALS);
    if(!Double.isNaN(prefX) && !Double.isNaN(maxX) && prefX>=maxX)
      throw new InvalidParamValException(MSG_INVALID_X_VALS);

    // check that aprobabilites are in increasing order
    if(!Double.isNaN(minProb) && !Double.isNaN(prefProb) && minProb>=prefProb)
        throw new InvalidParamValException(MSG_INVALID_PROB_VALS);
      if(!Double.isNaN(minProb) && !Double.isNaN(maxProb) && minProb>=maxProb)
        throw new InvalidParamValException(MSG_INVALID_PROB_VALS);
      if(!Double.isNaN(prefProb) && !Double.isNaN(maxProb) && prefProb>=maxProb)
        throw new InvalidParamValException(MSG_INVALID_PROB_VALS);

    /* check whether probabilites are between 0 & 1. */
    if(!Double.isNaN(minProb) && (minProb<0 || minProb>1))
     	throw new InvalidParamValException(EST_MSG_INVLID_RANGE);
     if(!Double.isNaN(maxProb) && (maxProb<0 || maxProb>1))
     	throw new InvalidParamValException(EST_MSG_INVLID_RANGE);
     if(!Double.isNaN(prefProb) && (prefProb<0 || prefProb>1))
     	throw new InvalidParamValException(EST_MSG_INVLID_RANGE);

    this.minX = minX;
    this.maxX = maxX;
    this.prefX = prefX;
    this.minProb = minProb;
    this.maxProb = maxProb;
    this.prefProb = prefProb;
  }

  public String toString() {
    String UNKNOWN = "Unknown";
    String minXStr=UNKNOWN, maxXStr=UNKNOWN, prefXStr=UNKNOWN;
    String minProbStr=UNKNOWN, maxProbStr=UNKNOWN, prefProbStr=UNKNOWN;
    if(!Double.isNaN(minX)) minXStr = decimalFormat.format(minX);
    if(!Double.isNaN(maxX)) maxXStr = decimalFormat.format(maxX);
    if(!Double.isNaN(prefX)) prefXStr = decimalFormat.format(prefX);
    if(!Double.isNaN(minProb)) minProbStr = decimalFormat.format(minProb);
    if(!Double.isNaN(maxProb)) maxProbStr = decimalFormat.format(maxProb);
    if(!Double.isNaN(prefProb)) prefProbStr = decimalFormat.format(prefProb);
    return "Estimate Type="+getName()+"\n"+
        "Minimum "+"="+minXStr+"["+minProbStr+"]\n"+
        "Maximum "+"="+maxXStr+"["+maxProbStr+"]\n"+
        "Preferred "+"="+prefXStr+"["+prefProbStr+"]\n";
  }


  /**
   * This returns the original Min (even if it's NaN)
   * @return double
   */
  public double getMinimumX() { return this.minX; }

  /**
  * This returns the original Max (even if it's NaN)
  * @return double
  */
  public double getMaximumX() { return this.maxX; }

  public double getPreferredX() { return this.prefX; }
  public double getMinimumProb() { return this.minProb; }
  public double getMaximumProb() { return this.maxProb; }
  public double getPreferredProb() { return this.prefProb; }

  /**
   * Get the maximum value among min, preferred, and max (i.e., NaNs excluded)
   *
   * @return maximum value (on X axis)
   */
  public double getMaxX() {
    if(!Double.isNaN(maxX)) return maxX;
    if(!Double.isNaN(prefX)) return prefX;
    if(!Double.isNaN(minX)) return minX;
    return Double.NaN;
  }

  /**
   * Get the minimum X value among min, preferred, and max (i.e., NaNs excluded)
   *
   * @return minimum value (on X axis)
   */
  public double getMinX() {
    if(!Double.isNaN(minX)) return minX;
    if(!Double.isNaN(prefX)) return prefX;
    if(!Double.isNaN(maxX)) return maxX;
    return Double.NaN;
  }

 public String getName() {
   return NAME;
 }

}
