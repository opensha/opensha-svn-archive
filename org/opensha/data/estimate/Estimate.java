package org.opensha.data.estimate;

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
  private String comments;


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
    return (getMinXValue()<0);
  }

  /**
   * Get the minimum among the list of X values in this list
   *
   * @return
   */
  public abstract double getMinXValue();

  /**
   * Get the maximum among the list of X values in this list
   *
   * @return
   */
  public abstract double getMaxXValue();

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

}
