package org.opensha.data.estimate;
import org.opensha.calc.GaussianDistCalc;
import org.opensha.data.function.DiscretizedFunc;
import org.opensha.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.data.function.EvenlyDiscretizedFunc;

/**
 * <p>Title: NormalEstimate.java  </p>
 * <p>Description:  This represents a Normal Distribution defined by a
 * mean and standard deviation (the latter must be positive).
 * The minimum and maximum X values serve to truncate the distribution, such that
 * probabilities are zero below and above these values, respectively (the defaults
 * are +/- infinity).
 *
 * </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class NormalEstimate extends Estimate {
  public final static String NAME  =  "Normal(Gaussian)";
  private double mean=Double.NaN;
  private double stdDev=Double.NaN;

  /**
   * Default constructor - accepts mean and standard deviation.
   *
   * @param mean
   * @param stdDev
   */
  public NormalEstimate(double mean, double stdDev) {
    setMean(mean);
    setStdDev(stdDev);
    this.minX=Double.NEGATIVE_INFINITY;
    this.maxX=Double.POSITIVE_INFINITY;
  }


  /**
   * This accepts minimum and maximum x-axis values that will be used as trunctions.
   *
   * @param mean
   * @param stdDev
   */
  public NormalEstimate(double mean, double stdDev, double minX, double maxX) {
    setMean(mean);
    setStdDev(stdDev);
    this.setMinMaxX(minX,maxX);
  }

  /**
   * Set mean for this distribution
   *
   * @param value specifying the mean for this distribution
   */
  public void setMean(double mean) {
    this.mean = mean;
  }

  /**
   * Get the mean for this distribution
   *
   * @return double value containing the mean for this distribution
   */
  public double getMean() {
    return mean;
  }


  /**
   * Set the stanndard deviation. It should be >=0 else exception
   * will be thrown
   *
   * @param stdDev standard deviation
   */
  public void setStdDev(double stdDev) {
    if(stdDev<0) throw new InvalidParamValException(MSG_INVALID_STDDEV);
    this.stdDev = stdDev;
  }

  /**
   * Return the standard deviation
   *
   * @return standard deviation for this class
   */
  public double getStdDev() {
    return stdDev;
  }


  /**
   * Get median. It should be noted that mean, median and mode
   * have same values for a normal distribution
   *
   * @return median value
   */
  public double getMedian() {
    return getMean();
  }

  /**
   * Get mode. It should be noted that mean, median and mode
   * have same values for a normal distribution
   *
   * @return mode value
   */
  public double getMode() {
   return getMean();
  }

  /**
   *
   * Returns the max x value such that probability of occurrence of this x value
   * is <=prob
   *
   * @param prob - probability value
   */
 public double getFractile(double prob) {
   /**
    * NOTE: In the statement below, we have to use (1-prob) because GaussianDistCalc
    * accepts the probability of exceedance as the parameter
    */
   double stdRndVar = GaussianDistCalc.getStandRandVar(1-prob, getStandRandVar(minX),
       getStandRandVar(maxX), 1e-6);
   return getMean() + stdRndVar*getStdDev();
 }


 /**
  * get the truncation level
  * @param val
  * @return
  */
 private double getStandRandVar(double val) {
   /* if min is negative infinity, return negative infinity.
     If max is positive infinity, return positive infinity
    */
   if(Double.isInfinite(val)) return val;
   else return (val-mean)/stdDev;
 }

 /**
  * Set the minimum and maximum X-axis value
  *
  * @param minX double
  * @param maxX double
  */
 public void setMinMaxX(double minX, double maxX) {
   if(maxX < minX) throw new InvalidParamValException(EST_MSG_MAX_LT_MIN);
   this.maxX = maxX;
   this.minX = minX;
 }

 /**
  * Get the name displayed to the user
  * @return
  */
 public String getName() {
   return NAME;
 }


 /**
  * Get the probability density function.
  * It calculates the PDF for x values.
  * The PDF is calculated for evenly discretized X values with minX=(mean-4*stdDev),
  * maxX=(mean+4*stdDev), numX=80
  *
  * @return
  */
  public DiscretizedFunc getPDF_Test() {
    EvenlyDiscretizedFunc func = getEvenlyDiscretizedFunc();
    double deltaX = func.getDelta();
    int numPoints = func.getNum();
    double x;
    for(int i=0; i<numPoints; ++i) {
      x = func.getX(i);
      func.set(i, getProbLessThanEqual(x + deltaX / 2) - getProbLessThanEqual(x - deltaX / 2));
    }
    func.setInfo("PDF from Normal Distribution");
    return func;
  }

  /**
   * Get the probability for that the true value is less than or equal to provided
   * x value
   *
   * @param x
   * @return
   */
  public double getProbLessThanEqual(double x) {
    return (1-GaussianDistCalc.getExceedProb(getStandRandVar(x), getStandRandVar(minX),
       getStandRandVar(maxX)));
  }


  /**
   * Get the cumulative distribution function
   * @return
   */
  public DiscretizedFunc getCDF_Test() {
    EvenlyDiscretizedFunc func = getEvenlyDiscretizedFunc();
    double deltaX = func.getDelta();
    int numPoints = func.getNum();
    double x;
    for(int i=0; i<numPoints; ++i)
      func.set(i, getProbLessThanEqual(func.getX(i)));
    func.setInfo("CDF from Normal Distribution using getProbLessThanEqual() method");
    return func;
  }

  /**
   * Make the Evenly discretized function for use in getPDF_Test() and getCDF_Test()
   * @return
   */
  private EvenlyDiscretizedFunc getEvenlyDiscretizedFunc() {
    double minX = mean-4*stdDev;
    double maxX = mean+4*stdDev;
    int numPoints = 80;
    EvenlyDiscretizedFunc func = new EvenlyDiscretizedFunc(minX, maxX, numPoints);
    return func;
  }


}
