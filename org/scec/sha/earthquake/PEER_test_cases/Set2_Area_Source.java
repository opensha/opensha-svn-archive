package org.scec.sha.earthquake.PEER_test_cases;

import org.scec.sha.earthquake.*;
import java.util.Vector;
import org.scec.data.*;


/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Nitin Gupta and Vipin Gupta
 * Date : Oct 23, 2002
 * @version 1.0
 */

public class Set2_Area_Source extends ProbEqkSource {

  //for Debug purposes
  private static String  C = new String("Set2_Area_Source");
  private boolean D = false;

  private double timeSpan;


  public Set2_Area_Source() {
  }


  /** Set the time span in years
   *
   * @param yrs : timeSpan as specified in  Number of years
   */
  public void setTimeSpan(double yrs) {
   //set the time span in yrs
    timeSpan = yrs;
  }



  /** Set the time span in years
   * FIX Mehthod not implemented yet
   *
   * @param yrs : timeSpan
   *
   */
  public void setTimeSpan(TimeSpan timeSpan) {

     // set the probability according to the specifed timespan
    throw new UnsupportedOperationException(C+"setTimeSpan(timeSpan) Not implemented.");
  }
  public int getNumRuptures() {
    /**@todo Implement this org.scec.sha.earthquake.EqkSourceAPI abstract method*/
    throw new java.lang.UnsupportedOperationException("Method getNumRuptures() not yet implemented.");
  }
  public Vector getRuptureList() {
    /**@todo Implement this org.scec.sha.earthquake.EqkSourceAPI abstract method*/
    throw new java.lang.UnsupportedOperationException("Method getRuptureList() not yet implemented.");
  }
  public double getMinDistance(Site site) {
    /**@todo Implement this org.scec.sha.earthquake.ProbEqkSource abstract method*/
    throw new java.lang.UnsupportedOperationException("Method getMinDistance() not yet implemented.");
  }
  public ProbEqkRupture getRupture(int nRupture) {
    /**@todo Implement this org.scec.sha.earthquake.EqkSourceAPI abstract method*/
    throw new java.lang.UnsupportedOperationException("Method getRupture() not yet implemented.");
  }
}