package org.scec.data.estimate;

import org.scec.data.function.ArbitrarilyDiscretizedFunc;
/**
 * <p>Title: PDF_Estimate.java </p>
 * <p>Description: Rules followe in this case are:
 * 1. First and Last y values should  be 0.
 * 2. X values should be increasing
 * 3. all y >=0
 * </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class PDF_Estimate implements EstimateAPI {
  private ArbitrarilyDiscretizedFunc func=null;
  private String comments;
  private final static String MSG_FIRST_LAST_Y_ZERO = "First and Last Y values "+
      "should be 0 for PDF_Estimate";
  private final static String MSG_X_INCREASING = "X values should be increasing "+
      " for PDF Estimate";
  private final static String MSG_Y_POSITIVE = "All the Y values should be >= 0 "+
      "for PDF Estimate";

  public PDF_Estimate(ArbitrarilyDiscretizedFunc func) {
    setValues(func);
  }

  /**
   * First and Last Y  should be qual to 0
   * All Y >=0
   * X should be increasing
   *
   * @param func
   */
  public void setValues(ArbitrarilyDiscretizedFunc func) {
    if(func.getY(0)!=0 || func.getY(func.getNum()-1)!=0)
      throw new InvalidParamValException(MSG_FIRST_LAST_Y_ZERO);
    double x= Double.NEGATIVE_INFINITY;
    for(int i = 0; i<func.getNum();++i) {
      if(func.getX(i)<x) throw new InvalidParamValException(MSG_X_INCREASING);
      x  = func.getX(i);
      if(func.getY(i)<0) throw new InvalidParamValException(MSG_Y_POSITIVE);
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