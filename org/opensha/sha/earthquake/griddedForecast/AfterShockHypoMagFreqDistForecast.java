package org.opensha.sha.earthquake.griddedForecast;

import org.opensha.data.region.*;
import org.opensha.sha.earthquake.observedEarthquake.*;
import org.opensha.sha.fault.FaultTrace;
import org.opensha.sha.fault.SimpleFaultData;

/**
 * <p>Title: AfterShockHypoMagFreqDistForecast</p>
 *
 * <p>Description: This class represents a poissonian aftershock hypocenter
 * forecast.
 *
 * The indexing over HypMagFreqDistAtLoc objects is exactly the same as the
 * EvenlyGriddedGeographicRegionAPI afterShockZone.</p>
 *
 * @author Nitin Gupta, Vipin Gupta and Edward (Ned) Field
 * @version 1.0
 */
public class AfterShockHypoMagFreqDistForecast
    extends GriddedHypoMagFreqDistForecast {

  private ObsEqkRupture mainShock;
  private ObsEqkRupList afterShocks;
  private EvenlyGriddedGeographicRegionAPI afterShockZone;

  public AfterShockHypoMagFreqDistForecast() {
  }

  public AfterShockHypoMagFreqDistForecast(ObsEqkRupture mainShock,
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
