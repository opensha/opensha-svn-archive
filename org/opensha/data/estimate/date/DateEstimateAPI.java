package org.opensha.data.estimate.date;

/**
 * <p>Title: DateEstimateAPI.java </p>
 * <p>Description: These are the methods which must be implemented by all the
 * Date Estimate types</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public interface DateEstimateAPI {


  /**
   * This method allows to set whether date has been corrected
   * @param isDateCorrected
   */
  public void setIsDateCorrected(boolean isDateCorrected);
  public boolean isDateCorrected();

}