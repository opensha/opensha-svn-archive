package org.opensha.data.estimate;
import org.opensha.data.function.DiscretizedFunc;

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
  protected final static String EST_MSG_NOT_NORMALIZED = "Error: Function is not normalized";
  protected final static String EST_MSG_Y_POSITIVE = "Error: All Y values must be positive";
  protected final static String EST_MSG_INVLID_RANGE = "Error: All Y values must be >= 0 and <=1";
  protected final static String EST_MSG_FIRST_LAST_Y_ZERO = "Error: First and Last Y values must be 0";
  protected final static String MSG_INVALID_STDDEV = "Error: Standard devivation must be positive.";

  protected String comments="";
  protected double minX, maxX;


  public abstract double getMean();

  public abstract double getMedian();

  public abstract double getStdDev();

  public abstract double getFractile(double prob);

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

   public abstract String getName() ;

   public abstract DiscretizedFunc getXY_ValsForPlotting();


}
