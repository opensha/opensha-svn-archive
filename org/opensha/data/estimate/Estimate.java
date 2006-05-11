package org.opensha.data.estimate;
import org.opensha.data.function.DiscretizedFunc;
import org.opensha.data.function.ArbitrarilyDiscretizedFunc;
import java.text.DecimalFormat;

/**
 * <p>Title: Estimate.java </p>
 * <p>Description: This is the abstract class for various types of estimates </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author
 * @version 1.0
 */

public abstract class Estimate {

  // comments associated with this object
  protected final static String EST_MSG_MAX_LT_MIN = "Error: Minimum must be less than Maximum";
  protected final static String EST_MSG_NOT_NORMALIZED = "Error: The probability values do not sum to 1";
  protected final static String EST_MSG_Y_POSITIVE = "Error: All Y values must be positive";
  protected final static String EST_MSG_INVLID_RANGE = "Error: All Y values must be >= 0 and <=1";
  protected final static String EST_MSG_FIRST_LAST_Y_ZERO = "Error: First and Last Y values must be 0";
  protected final static String MSG_INVALID_STDDEV = "Error: Standard devivation must be positive.";
  protected final static String MSG_ALL_Y_ZERO = "Error: At least one Y value must be > 0.";
  protected String comments="";
  protected double minX, maxX;
  protected String units;
  protected final static DecimalFormat decimalFormat = new DecimalFormat("0.0####");

  /**
   * Get units for this estimate
   * @return
   */
  public String getUnits() {
    return units;
  }

  /**
   * Set the units in this estimate
   * @param units
   */
  public void setUnits(String units) {
    this.units = units;
  }

  /**
   * Get the mean for this estimate
   *
   * @return
   */
  public abstract double getMean();

  /**
   * Get median for this estimate
   *
   * @return
   */
  public abstract double getMedian();

  /**
   * Get Std Dev for this estimate
   *
   * @return
   */
  public abstract double getStdDev();

  /**
   * Get fractile for a given probability. This returns the max x value such that
   * probability of occurrence of this x value is less than or equal to prob.
   *
   * @param prob
   * @return
   */
  public abstract double getFractile(double prob);

  /**
   * Get mode for this estimate
   *
   * @return
   */
  public abstract double getMode();


  /**
   * Checks whether there exist any X values which is less than 0.
   *
   * @return It returns true if any x<0. If all x>=0, it returns false
   */
  public boolean isNegativeValuePresent() {
    return (minX<0.0);
  }

  /**
   * Get the maximum X value
   *
   * @return maximum value (on X axis)
   */
  public double getMaxX() {return maxX;};

  /**
   * Get the minimum X value
   *
   * @return minimum value (on X axis)
   */
  public double getMinX() {return minX;}


   /**
    * Get the comments associated with this object
    *
    * @return String value containing the comments
    */
   public String getComments() {
     return comments;
   }

   /**
    * Set the comments in this object
    *
    * @param comments comments to be set for this object
    */
   public void setComments(String comments) {
     this.comments = comments;
   }

   /**
    * Get the name. this is the name displayed to the user in the estimate
    * type chooser.
    * @return
    */
   public abstract String getName() ;

   /**
    * Test function to find the PDF for this estimate. It uses the
    * getProbLessThanEqual() function internally.
    *
    * @return
    */
   public abstract DiscretizedFunc getPDF_Test();

   /**
    * Test function to get the CDF for this estimate. It uses the
    * getProbLessThanEqual() function internally.
    *
    * @return
    */
   public abstract DiscretizedFunc getCDF_Test();

   /**
    * Get the probability that the true value is less than or equal to the provided
    * x value
    *
    * @param x
    * @return
    */
   public abstract double getProbLessThanEqual(double x);

   /**
   * Test function to get the CDF for this estimate. It uses the
   * getFractile() function internally. It discretizes the Y values and then
   * calls the getFractile() method to get corresponding x values and then
   * plot them.
   *
   * @return
   */
  public  DiscretizedFunc getCDF_TestUsingFractile() {
    ArbitrarilyDiscretizedFunc func = new ArbitrarilyDiscretizedFunc();
    //discretize the Y values
    double minY = 0.00001;
    double maxY = 0.99999;
    int numPoints = 100;
    double deltaY = (maxY-minY)/(numPoints-1);
    // find the X values correpsoding to Y values
    for(double y=minY; y<=maxY;y=y+deltaY)
      func.set(getFractile(y),y);
    func.setInfo("CDF using getFractile() method");
    return func;
  }

  /**
   * Convert this estimate into a String
   * @return
   */
  public String toString() {
    return toString("X");
  }

  /**
   * X label is displayed instead of X .
   * @param xLabel
   * @param yLabel
   * @return
   */
  public abstract String toString(String xLabel);
}
