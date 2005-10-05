package org.opensha.sha.earthquake.griddedForecast;

import org.opensha.data.region.*;
import org.opensha.sha.fault.*;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2002</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class STEP_AftershockForecast
    extends AfterShockHypoMagFreqDistForecast {
  public HypoMagFreqDistAtLoc getHypoMagFreqDistAtLoc(int ithLocation) {
    return null;
  }

  protected double dayStart, dayEnd;
  protected SimpleFaultData mainshockFault;
  protected double minForecastMag, maxForecastMag, deltaForecastMag;
  protected boolean useFixed_cValue;
  protected double addToMc;

  public STEP_AftershockForecast() {
  }

  /**
   * This sets the aftershock zone
   * @param aftershockZone EvenlyGriddedGeographicRegionAPI
   */
  public void setAfterShockZone(EvenlyGriddedGeographicRegionAPI aftershockZone) {
    this.region = aftershockZone;
  }

  /**
   * setForecastStartDay
   */
  public void setForecastStartDay(double dayStart) {
    this.dayStart = dayStart;
  }

  /**
   * setForecastDayEnd
   */
  public void setForecastDayEnd(double dayEnd) {
    this.dayEnd = dayEnd;
  }

  /**
   * set_FaultModel
   */
  public void set_FaultModel(SimpleFaultData mainshockFault) {
    this.mainshockFault = mainshockFault;
  }

  /**
   * get_FaultModel
   */
  public SimpleFaultData get_FaultModel() {
    return mainshockFault;
  }

  /**
   * setMinForecastMag
   */
  public void setMinForecastMag(double minMag) {
    this.minForecastMag = minMag;
  }

  /**
   * setMaxForecastMag
   */
  public void setMaxForecastMag(double maxMag) {
    this.maxForecastMag = maxMag;
  }

  /**
   * setDeltaForecastMag
   */
  public void setDeltaForecastMag(double deltaMag) {
    this.deltaForecastMag = deltaMag;
  }

  /**
   * setUseFixed_cValue
   */
  public void setUseFixed_cValue(boolean useFix_cValue) {
    this.useFixed_cValue = useFix_cValue;
  }

  /**
   * set_addToMcConstant
   */
  public void set_addToMcConstant(double mcConst) {
    this.addToMc = mcConst;
  }


}
