package org.scec.data.estimate;

/**
 * <p>Title: EstimateAPI.java </p>
 * <p>Description: This is the common interface for handling various types
 * of estimates. </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author
 * @version 1.0
 */

public interface EstimateAPI {
   public double getMean();
   public double getMedian();
   public double getStdDev();
   public double getFractile(double prob);
}