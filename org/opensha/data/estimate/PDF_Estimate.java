package org.opensha.data.estimate;

import org.opensha.data.function.EvenlyDiscretizedFunc;
import org.opensha.data.function.ArbDiscrEmpiricalDistFunc;
import org.opensha.data.function.DiscretizedFuncAPI;

/**
 * <p>Title: PDF_Estimate.java </p>
 * <p>Description:  This is probability distribution function.
 *
 * Rules followed in this case are:
 * 1. First and Last y values should  be 0.
 * 2. all y >=0
 * </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class PDF_Estimate extends Estimate {
  public final static String NAME = "org.opensha.data.estimate.PDF_Estimate";
  protected EvenlyDiscretizedFunc func=null;
  private double tol = 1e-6;

  /**
   * Constructor - Accepts EvenlyDiscretizedFunc which is  list of X and Y
   * values. The X and Y values have some constraints which can be seen in
   * setValues function documentation
   *
   * @param func
   */
  public PDF_Estimate(EvenlyDiscretizedFunc func, boolean isNormalized) {
    setValues(func, isNormalized);
  }

  /**
   * First and Last Y  should be equal to 0
   * All Y >=0 & <= 1 (if not normalized)
   *
   * @param func
   */
  public void setValues(EvenlyDiscretizedFunc newFunc, boolean isNormalized) {

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
 private ArbDiscrEmpiricalDistFunc getEmpiricalDistFunc() {
   ArbDiscrEmpiricalDistFunc empiricalDistFunc = new ArbDiscrEmpiricalDistFunc();
   for(int i=0; i<func.getNum(); ++i)
      empiricalDistFunc.set(func.getX(i), func.getY(i));
   return empiricalDistFunc;
 }


  public double getMean() {
    throw new java.lang.UnsupportedOperationException("Method getMean() yet implemented");
  }

  public double getMedian() {
    throw new java.lang.UnsupportedOperationException("Method getMedian() yet implemented");
  }

  public double getStdDev() {
    throw new java.lang.UnsupportedOperationException("Method getStdDev() yet implemented");
  }

 public double getMode() {
    return func.getX(func.getXIndex(func.getMaxY()));
 }

 public DiscretizedFuncAPI getValues() {
   return func;
 }

}
