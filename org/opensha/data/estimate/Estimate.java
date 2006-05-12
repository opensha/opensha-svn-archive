package org.opensha.data.estimate;
import org.opensha.data.function.DiscretizedFunc;
import org.opensha.data.function.ArbitrarilyDiscretizedFunc;
import java.text.DecimalFormat;

/**
 * <p>Title: Estimate.java </p>
 * <p>Description: This is the abstract class for various types of estimates.
 * Most methods here throw unsupported exceptions because this will often be the
 * case in subclasses.  </p>
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
  protected final static String EST_MSG_INVLID_RANGE = "Error: All probabilities must be ³ 0 and ² 1";
  protected final static String EST_MSG_FIRST_LAST_Y_ZERO = "Error: First and Last Y values must be 0";
  protected final static String MSG_INVALID_STDDEV = "Error: Standard devivation must be positive.";
  protected final static String MSG_ALL_Y_ZERO = "Error: At least one Y value must be > 0.";
  protected final static String EST_MSG_PROBS_NOT_INCREASING = "Probabilities must be in increasing order";
  protected final static String MEDIAN_UNDEFINED = "Error: Median is undefined";
  protected final static String FRACTILE_UNDEFINED = "Error: Fractile is undefined";

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
  public double getMean() {
    throw new java.lang.UnsupportedOperationException("Method getMean() not supported");
  }


  /**
   * Get median for this estimate
   *
   * @return
   */
  public double getMedian() {
    throw new java.lang.UnsupportedOperationException("Method getMedian() not supported");
  }


  /**
   * Get Std Dev for this estimate
   *
   * @return
   */
  public double getStdDev() {
    throw new java.lang.UnsupportedOperationException("Method getStdDev() not supported");
  }

  /**
   * Get fractile for a given probability (the value where the CDF equals prob).
   *
   * @param prob
   * @return
   */
  public double getFractile(double prob) {
    throw new java.lang.UnsupportedOperationException("Method getFractile() not supported");
  }


  /**
   * Get mode for this estimate
   *
   * @return
   */
  public double getMode() {
    throw new java.lang.UnsupportedOperationException("Method getMode() not supported");
  }



  /**
   * Checks whether there exist any X values which is less than 0.
   *
   * @return It returns true if any x<0. If all x>=0, it returns false
   */
  public boolean isNegativeValuePresent() {
    return (getMinX()<0.0);
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
   public DiscretizedFunc getPDF_Test() {
    throw new java.lang.UnsupportedOperationException("Method getPDF_Test() not supported");
  }


   /**
    * Test function to get the CDF for this estimate. It uses the
    * getProbLessThanEqual() function internally.
    *
    * @return
    */
   public DiscretizedFunc getCDF_Test() {
    throw new java.lang.UnsupportedOperationException("Method getCDF_Test() not supported");
  }


   /**
    * Get the probability that the true value is less than or equal to the provided
    * x value (the CDF for a probability density funtion)
    *
    * @param x
    * @return
    */
   public double getProbLessThanEqual(double x) {
    throw new java.lang.UnsupportedOperationException("Method getProbLessThanEqual() not supported");
  }


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

  public String toString() {
    String text = "Values from Methods:\n";
    try {
      text += "Mean = "+getMean()+"\n";
    }
    catch ( Exception e) {
      text += "Mean = NA\n";
    }
    try {
      text += "Mode = "+getMode()+"\n";
    }
    catch ( Exception e) {
      text += "Mode = NA\n";
    }
    return text;
  }

}
