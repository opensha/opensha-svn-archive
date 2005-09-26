package org.opensha.refFaultParamDb.data;
import org.opensha.data.estimate.Estimate;
/**
 * <p>Title: TimeEstimate.java </p>
 * <p>Description: Allows the user to specify a time estimate. This estimate
 * can be a start time estimate or an end time estimate in a time span.</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class TimeEstimate implements TimeAPI {
  private Estimate estimate;
  private int era;
  private boolean isKa; // whether ka is selected or user is providing calendar year estimate
  private int zeroYear;

  public TimeEstimate() {
  }

  public void setForKaUnits(Estimate estimate, int zeroYear) {
    this.zeroYear = zeroYear;
    this.estimate = estimate;
    isKa = true;
  }

  public void setForCalendarYear(Estimate estimate, int era) {
    this.estimate = estimate;
    this.era = era;
    isKa = false;
  }

  public boolean isKaSelected() {
    return isKa;
  }

  public int getEra() {
    return era;
  }

  public Estimate getEstimate() {
    return estimate;
  }

  public int getZeroYear() {
    return zeroYear;
  }
}