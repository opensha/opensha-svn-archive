package org.opensha.data.estimate.date;

import org.opensha.data.estimate.DiscreteValueEstimate;
import org.opensha.data.function.ArbitrarilyDiscretizedFunc;

/**
 * <p>Title: DiscreteValueDateEstimate.java </p>
 * <p>Description:This class saves the date estimate. </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class DiscreteValueDateEstimate extends DiscreteValueEstimate implements DateEstimateAPI {
  public final static String NAME  =  "Date Discrete Values";
  private boolean isDateCorrected=false;

  public DiscreteValueDateEstimate(ArbitrarilyDiscretizedFunc func, boolean isNormalized) {
    super(func, isNormalized);
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