package org.scec.data.estimate;

import org.scec.data.function.ArbitrarilyDiscretizedFunc;
import org.scec.data.function.ArbDiscrEmpiricalDistFunc;
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
  public final static String NAME = "org.scec.data.estimate.PDF_Estimate";
  protected ArbitrarilyDiscretizedFunc func=null;

  private final static String MSG_FIRST_LAST_Y_ZERO = "First and Last Y values "+
      "should be 0 for PDF_Estimate";
  private final static String MSG_Y_POSITIVE = "All the Y values should be >= 0 "+
      "for PDF Estimate";

  /**
   * Constructor - Accepts ArbitrarilyDiscretizedFunc which is  list of X and Y
   * values. The X and Y values have some constraints which can be seen in
   * setValues function documentation
   *
   * @param func
   */
  public PDF_Estimate(ArbitrarilyDiscretizedFunc func) {
    setValues(func);
  }

  /**
   * First and Last Y  should be equal to 0
   * All Y >=0
   *
   * @param func
   */
  public void setValues(ArbitrarilyDiscretizedFunc func) {
    if(func.getY(0)!=0 || func.getY(func.getNum()-1)!=0)
      throw new InvalidParamValException(MSG_FIRST_LAST_Y_ZERO);
    for(int i = 0; i<func.getNum();++i)
      if(func.getY(i)<0) throw new InvalidParamValException(MSG_Y_POSITIVE);
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
    return func.getX(func.getNum() - 1);
  }


  public double getMean() {
    throw new java.lang.UnsupportedOperationException("Method getMean() not supported");
  }

  public double getMedian() {
    throw new java.lang.UnsupportedOperationException("Method getMedian() not supported");
  }

  public double getStdDev() {
    throw new java.lang.UnsupportedOperationException("Method getStdDev() not supported.");
  }

 public double getMode() {
    throw new java.lang.UnsupportedOperationException("Method getMode() not supported.");
 }

}