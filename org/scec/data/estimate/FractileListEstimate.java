package org.scec.data.estimate;

import org.scec.data.function.ArbitrarilyDiscretizedFunc;

/**
 * <p>Title: FractileListEstimate.java </p>
 * <p>Description: This estimate can also be used if user just provides
 * min,max and preferred values which is very common.
 *  The rules for this etimate are:
 * 1. 1>=y>=0
 * 2. y(i+1)>=y(i)
 *
 * </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class FractileListEstimate {
  private ArbitrarilyDiscretizedFunc func=null;
   private String comments;
   private final static int MIN_Y_VAL = 0;
   private final static int MAX_Y_VAL = 1;
   private final static String MSG_Y_RANGE = "All the Y values should be >= 0 "+
       "and <=1 for FractileListEstimate";
   private final static String MSG_Y_INCREASING = "Y values should be increasing "+
      " for PDF Estimate";


   public FractileListEstimate(ArbitrarilyDiscretizedFunc func) {
     setValues(func);
   }

   /**
    * First and Last Y  should be qual to 0
    * All Y >=0
    *
    * @param func
    */
   public void setValues(ArbitrarilyDiscretizedFunc func) {
     double y= Double.NEGATIVE_INFINITY;
     for(int i = 0; i<func.getNum();++i) {
       if(func.getY(i)<y) throw new InvalidParamValException(MSG_Y_INCREASING);
       y  = func.getY(i);
       if(y<MIN_Y_VAL || y>MAX_Y_VAL) throw new InvalidParamValException(MSG_Y_RANGE);
     }
   }

   public double getMean() {
     /**@todo Implement this org.scec.data.estimate.EstimateAPI method*/
     throw new java.lang.UnsupportedOperationException("Method getMean() not yet implemented.");
   }

   public double getMedian() {
     /**@todo Implement this org.scec.data.estimate.EstimateAPI method*/
     throw new java.lang.UnsupportedOperationException("Method getMedian() not yet implemented.");
   }

   public double getStdDev() {
     /**@todo Implement this org.scec.data.estimate.EstimateAPI method*/
     throw new java.lang.UnsupportedOperationException("Method getStdDev() not yet implemented.");
   }

   public double getFractile(double prob) {
     /**@todo Implement this org.scec.data.estimate.EstimateAPI method*/
     throw new java.lang.UnsupportedOperationException("Method getFractile() not yet implemented.");
   }

   public void setComments(String comments) {
    this.comments  = comments;
  }

  public String getComments() {
    return this.comments;
  }


}