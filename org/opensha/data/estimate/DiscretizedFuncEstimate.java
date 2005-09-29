package org.opensha.data.estimate;

import org.opensha.data.function.DiscretizedFunc;
import org.opensha.data.function.ArbDiscrEmpiricalDistFunc;
import org.opensha.data.function.DiscretizedFuncAPI;
/**
 * <p>Title: DiscreteValueEstimate.java </p>
 * <p>Description:  This can be used to specify probabilities associated with
 * discrete values from a DiscretizedFunction. Use an EvenlyDiscretizedFunction for
 * a continuous PDF (where it is asssumed that the first and last values are the
 * first and last non-zero values, respectively), or use an ArbitrarilyDiscretizedFunction
 * if the nonzero values are not evenly discretized.
 * </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public abstract class DiscretizedFuncEstimate extends Estimate {
  protected DiscretizedFunc func=null;
  protected DiscretizedFunc cumDistFunc = null;

  // tolerance for checking normalization
  protected double tol = 1e-6;


  /**
   * Constructor - Accepts a DiscretizedFunc and an indication of whether it is
   * normalized. Note that the function passed in is cloned.
   * MaxX and MinX are set according to those of the function
   * passed in.
   * @param func
   */
  public DiscretizedFuncEstimate(DiscretizedFunc func, boolean isNormalized) {
    setValues(func, isNormalized);
  }

  /**
   * As implemented, the function passed in is cloned.
   *  MaxX and MinX are set by those in the function passed in.
   *
   * @param func
   */
  public void setValues(DiscretizedFunc newFunc, boolean isNormalized) {

    this.func = (DiscretizedFunc) newFunc.deepClone();

    minX = func.getMinX();
    maxX = func.getMaxX();

    // Check normalization and value range
    double sum=0, val;
    int num = func.getNum();
    if(isNormalized) { // check values
      for (int i = 0; i < num; ++i) {
        val = func.getY(i);
        if (val < 0 || val > 1)throw new InvalidParamValException(EST_MSG_INVLID_RANGE);
        sum += val;
      }
      // make sure sum is close to 1.0
      if ( Math.abs(sum-1.0) <= tol)
        throw new InvalidParamValException(EST_MSG_NOT_NORMALIZED);
    }
    else { // sum y vals and check positivity
      for (int i = 0; i < num; ++i) {
        val = func.getY(i);
        if (val < 0)throw new InvalidParamValException(EST_MSG_Y_POSITIVE);
        sum += val;
      }
      if(sum==0) throw new InvalidParamValException(MSG_ALL_Y_ZERO);
      // normalize the function
      for (int i = 0; i < num; ++i) {
        val = func.getY(i);
        func.set( i, val/sum );
      }
    }
    this.cumDistFunc = (DiscretizedFunc) newFunc.deepClone();
    double runningSum=0;
    for(int i=0; i<num; ++i) {
      runningSum+=func.getY(i);
      cumDistFunc.set(i, runningSum);
    }
  }

  /**
   * get the X Y values for this estimate
   * @return
   */
  public DiscretizedFunc getValues() {
    return func;
  }


  /**
   * Get the mode (X value where Y is maximum).
   * Returns the smallest X value in case of multi-modal distribution
   *
   * @return
   */
  public double getMode() {
    return func.getX(func.getXIndex(func.getMaxY()));
 }

 /**
  * Get the median which is same as fractile at probability of 0.5.
  *
  * @return
  */
  public double getMedian() {
    return getFractile(0.5);
  }

  /**
   * Get standard deviation
   * @return
   */
  public double getStdDev() {
    throw new java.lang.UnsupportedOperationException("Method getStdDev() not yet implement.");
  }


  /**
   * Get mean
   * @return
   */
  public double getMean() {
   throw new java.lang.UnsupportedOperationException("Method getMean() not yet implement");
 }

 /**
  * This allows the user to set the tolerance used for checking normalization (and
  * perhaps other things in subclasses).
  * @param tol double
  */
 public void setTolerance(double tol) {this.tol = tol;}

}
