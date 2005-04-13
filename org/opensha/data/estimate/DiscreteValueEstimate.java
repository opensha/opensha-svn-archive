package org.opensha.data.estimate;

import org.opensha.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.data.function.ArbDiscrEmpiricalDistFunc;
/**
 * <p>Title: DiscreteValueEstimate.java </p>
 * <p>Description:  This can be used where specifies discrete values and
 * probabilites associated with them. For example user may say that dip can have
 *  value of 45,60,90 with probabilities of 0.2,0.3,0.5 respectively.
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

  private final static String MSG_Y_POSITIVE = "All the Y values should be >= 0 and <=1 "+
      "for DiscreteValueEstimate";

  /**
   * Constructor - Accepts ArbitrarilyDiscretizedFunc which is  list of X and Y
   * values. The X and Y values have some constraints which can be seen in
   * setValues function documentation
   *
   * @param func
   */
  public DiscreteValueEstimate(ArbitrarilyDiscretizedFunc func) {
    setValues(func);
  }

  /**
   * for All Y,  0<=Y<=1
   *
   * @param func
   */
  public void setValues(ArbitrarilyDiscretizedFunc func) {
    for(int i = 0; i<func.getNum();++i)
      if(func.getY(i)<0 || func.getY(i)>1) throw new InvalidParamValException(MSG_Y_POSITIVE);
    this.func = func;
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


  /**
   * Get the mode for this distribution. It is same as median for this case
   *
   * @return
   */
  public double getMode() {
    return getMedian();
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

}
