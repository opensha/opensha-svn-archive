package org.scec.data.estimate;

/**
 * <p>Title: EstimateAPI.java </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public interface EstimateAPI {
   public double getMean();
   public double getMedian();
   public double getStdDev();
   public double getFractile(double prob);
}