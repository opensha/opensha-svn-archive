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

public class DiscreteValueEstimate extends Estimate {
  protected DiscretizedFunc func=null;
  public final static String NAME = "org.opensha.data.estimate.DiscreteValueEstimate";

  // tolerance for checking normalization
  protected double tol = 1e-6;

  /**
   * Constructor - Accepts a DiscretizedFunc and an indication of whether it is
   * normalized. Note that the function passed in is not cloned, and will therefor
   * change if it is not normalized.  MaxX and MinX are set according to those of the function
   * passed in.
   * @param func
   */
  public DiscreteValueEstimate(DiscretizedFunc func, boolean isNormalized) {
    setValues(func, isNormalized);
  }

  /**
   * As implemented, the function passed in is not cloned, and will therefor be
   * changed if normalization occurs.  MaxX and MinX are set by those in the function passed in.
   *
   * @param func
   */
  public void setValues(DiscretizedFunc newFunc, boolean isNormalized) {

    this.func = newFunc; // or should it be a clone???

    minX = func.getMinX();
    maxX = func.getMaxX();

    // Check normalization and value range
    double sum=0, val;
    if(isNormalized) { // check values
      for (int i = 0; i < func.getNum(); ++i) {
        val = func.getY(i);
        if (val < 0 || val > 1)throw new InvalidParamValException(EST_MSG_INVLID_RANGE);
        sum += val;
      }
      // make sure sum is close to 1.0
      if ( (sum-tol) > 1.0 || (sum+tol) < 1.0)
        throw new InvalidParamValException(EST_MSG_NOT_NORMALIZED);
    }
    else { // sum y vals and check positivity
      for (int i = 0; i < func.getNum(); ++i) {
        val = func.getY(i);
        if (val < 0)throw new InvalidParamValException(EST_MSG_Y_POSITIVE);
        sum += val;
      }
      // normalize the function
      for (int i = 0; i < func.getNum(); ++i) {
        val = func.getY(i);
        func.set( i, val/sum );
      }
    }

  }


  /**
   * Return the discrete fractile for this probability value.
   *
   * @param prob Probability for which fractile is desired
   * @return
   */
  public double getFractile(double prob) {
    ArbDiscrEmpiricalDistFunc empiricalDistFunc = getEmpiricalDistFunc();
    return empiricalDistFunc.getDiscreteFractile(prob);
 }

 /**
  * Construct the ArbDiscrEmpiricalDistFunc function so that it can be used
  * to calculate fractile
  *
  * @return
  */
 public ArbDiscrEmpiricalDistFunc getEmpiricalDistFunc() {
   ArbDiscrEmpiricalDistFunc empiricalDistFunc = new ArbDiscrEmpiricalDistFunc();
   for(int i=0; i<func.getNum(); ++i)
      empiricalDistFunc.set(func.getX(i), func.getY(i));
   return empiricalDistFunc;
 }

  /**
   * Get the mode (X value where Y is maximum)
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


  public double getStdDev() {
    throw new java.lang.UnsupportedOperationException("Method getStdDev() not yet implement.");
  }

  public double getMean() {
   throw new java.lang.UnsupportedOperationException("Method getMean() not yet implement");
 }

 public DiscretizedFuncAPI getValues() {
   return this.func;
 }

 /**
  * This allows the user to set the tolerance used for checking normalization (and
  * perhaps other things in subclasses).
  * @param tol double
  */
 public void setTolerance(double tol) {this.tol = tol;}
}
