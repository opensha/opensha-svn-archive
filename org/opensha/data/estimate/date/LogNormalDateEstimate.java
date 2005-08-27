package org.opensha.data.estimate.date;

import org.opensha.data.estimate.LogNormalEstimate;

/**
 * <p>Title: LogNormalDateEstimate.java </p>
 * <p>Description: This class saves the date estimate </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class LogNormalDateEstimate extends LogNormalEstimate implements DateEstimateAPI {
  public final static String NAME  =  "Time Log Normal";
  private boolean isDateCorrected=false;

  public LogNormalDateEstimate(double linearMedian, double stdDev) {
    super(linearMedian, stdDev);
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