package org.opensha.data.estimate.date;

import org.opensha.data.estimate.PDF_Estimate;
import org.opensha.data.function.EvenlyDiscretizedFunc;

/**
 * <p>Title: PDF_DateEstimate.java </p>
 * <p>Description: This class saves the date estimate </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class PDF_DateEstimate extends PDF_Estimate implements DateEstimateAPI {

  private boolean isDateCorrected=false;

  public PDF_DateEstimate(EvenlyDiscretizedFunc func, boolean isNormalized) {
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

}