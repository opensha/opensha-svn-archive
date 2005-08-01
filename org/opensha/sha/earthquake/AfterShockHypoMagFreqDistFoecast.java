package org.opensha.sha.earthquake;

import org.opensha.data.region.*;
import org.opensha.sha.earthquake.observedEarthquake.*;

/**
 * <p>Title: AfterShockHypoMagFreqDistFoecast</p>
 *
 * <p>Description: This class allows user to create the After shocks forecast.</p>
 *
 * @author Nitin Gupta, Vipin Gupta and Edward (Ned) Field
 * @version 1.0
 */
public class AfterShockHypoMagFreqDistFoecast
    extends GriddedHypoMagFreqDistForecast {

  private ObsEqkRupture mainShock;
  private ObsEqkRupList afterShocks;
  private EvenlyGriddedGeographicRegionAPI afterShockZone;

  public AfterShockHypoMagFreqDistFoecast() {
  }

  public AfterShockHypoMagFreqDistFoecast(ObsEqkRupture mainShock,
                                          ObsEqkRupList afterShocks,
                                          EvenlyGriddedGeographicRegionAPI afterShockZone){
    this.mainShock = mainShock;
    this.afterShocks = afterShocks;
    this.afterShockZone = afterShockZone;
  }

  public ObsEqkRupList getAfterShocks() {
    return afterShocks;
  }

  public EvenlyGriddedGeographicRegionAPI getAfterShockZone() {
    return afterShockZone;
  }

  public ObsEqkRupture getMainShock() {
    return mainShock;
  }

  public void setAfterShocks(ObsEqkRupList afterShocks) {
    this.afterShocks = afterShocks;
  }

  public void setAfterShockZone(EvenlyGriddedGeographicRegionAPI afterShockZone) {
    this.afterShockZone = afterShockZone;
  }

  public void setMainShock(ObsEqkRupture mainShock) {
    this.mainShock = mainShock;
  }
}
