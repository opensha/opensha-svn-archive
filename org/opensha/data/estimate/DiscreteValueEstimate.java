package org.opensha.data.estimate;

import org.opensha.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.data.function.ArbDiscrEmpiricalDistFunc;
import org.opensha.data.function.DiscretizedFuncAPI;
/**
 * <p>Title: DiscreteValueEstimate.java </p>
 * <p>Description:  This can be used to specify probabilities associated with
 * discrete values (with zero probabilities in between values). For example user
 * may say that dip can have value of 45,60,90 with probabilities of 0.2,0.3,0.5 respectively.
 *
 *
 * Rules followed in this case are:
 * 2. 0<=y<=1 for all y
 * </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class DiscreteValueEstimate extends Estimate {
  protected ArbitrarilyDiscretizedFunc func=null;
  public final static String NAME = "org.opensha.data.estimate.DiscreteValueEstimate";

  private double tol = 1e-6;

  /**
   * Constructor - Accepts ArbitrarilyDiscretizedFunc which is  list of X and Y
   * values. Note that the function passed in is not cloned, and will change if it
   * is not normalized.  MaxX and MinX are set by those in the function passed in.
   * @param func
   */
  public DiscreteValueEstimate(ArbitrarilyDiscretizedFunc func, boolean isNormalized) {
    setValues(func, isNormalized);
  }

  /**
   * As implemented, the function passed in is not cloned, and will therefor be
   * changed if normalization occurs.  MaxX and MinX are set by those in the function passed in.
   *
   * @param func
   */
  public void setValues(ArbitrarilyDiscretizedFunc newFunc, boolean isNormalized) {

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
   * Get the mode for this distribution. It is same as median for this case
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
  * Get the minimum among the list of X values in this list
  *
  * @return
  */
 public double getMinXValue() {
   return func.getX(0);
 }

 /**
  * Get the maximum among the list of X values in this list
  *
  * @return
  */
 public double getMaxXValue() {
   return func.getX(func.getNum()-1);
 }


  public double getStdDev() {
    throw new java.lang.UnsupportedOperationException("Method getStdDev() not supported.");
  }

  public double getMean() {
   throw new java.lang.UnsupportedOperationException("Method getMean() not supported");
 }

 public DiscretizedFuncAPI getValues() {
   return this.func;
 }


}
