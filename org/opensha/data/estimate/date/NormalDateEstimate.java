package org.opensha.data.estimate.date;

import org.opensha.data.estimate.NormalEstimate;

/**
 * <p>Title: NormalDateEstimate.java </p>
 * <p>Description: This class saves the date estimate.</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class NormalDateEstimate extends NormalEstimate implements DateEstimateAPI {
  public final static String NAME  =  "Time Normal";
  private boolean isDateCorrected=false;


  public NormalDateEstimate(double mean, double stdDev) {
    super(mean, stdDev);
  }

  public NormalDateEstimate(double mean, double stdDev, double minX, double maxX) {
    super(mean, stdDev, minX, maxX);
  }

  /**
   * This method allows to set whether date has been corrected
   * @param isDateCorrected
   */
  public void setIsDateCorrected(boolean isDateCorrected) {
    this.isDateCorrected = isDateCorrected;
  }

  public boolean isDateCorrected() {
    return isDateCorrected;
  }

  /**
  * Get the name displayed to the user
  * @return
  */
 public String getName() {
   return NAME;
 }



}