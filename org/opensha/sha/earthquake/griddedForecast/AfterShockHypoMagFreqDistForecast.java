package org.opensha.sha.earthquake.griddedForecast;

import org.opensha.data.region.*;
import org.opensha.sha.earthquake.observedEarthquake.*;


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

  protected ObsEqkRupture mainShock;
  protected ObsEqkRupList afterShocks;

  /**
   * Class no-arg constructor
   */
  public AfterShockHypoMagFreqDistForecast() {
  }

  /**
   * Class constructor
   * @param mainShock ObsEqkRupture main shock event
   * @param afterShocks ObsEqkRupList list of aftershocks
   * @param afterShockZone EvenlyGriddedGeographicRegionAPI AfterShockZone
   */
  public AfterShockHypoMagFreqDistForecast(ObsEqkRupture mainShock,
                                          ObsEqkRupList afterShocks,
                                          EvenlyGriddedGeographicRegionAPI afterShockZone){
    this.mainShock = mainShock;
    this.afterShocks = afterShocks;
    this.region = afterShockZone;
  }

  /**
   * Gets the Aftershock list for the forecast model.
   * @return ObsEqkRupList
   */
  public ObsEqkRupList getAfterShocks() {
    return afterShocks;
  }

  /**
   * Allows the user to set the AfterShockZone as EvelyGriddedGeographicRegion.
   * @return EvenlyGriddedGeographicRegionAPI AfterShockZone.
   */
  public EvenlyGriddedGeographicRegionAPI getAfterShockZone() {
    return region;
  }

  /**
   * Returns the main shock
   * @return ObsEqkRupture
   */
  public ObsEqkRupture getMainShock() {
    return mainShock;
  }

  /**
   * Sets the list of ObsEqkRuptures for the given AfterShockHypoMagFreqDistForecast.
   * @param afterShocks ObsEqkRupList
   */
  public void setAfterShocks(ObsEqkRupList afterShocks) {
    this.afterShocks = afterShocks;
  }

  /**
   * addToAftershockList
   */
  public void addToAftershockList(ObsEqkRupture newAftershock) {
    afterShocks.addObsEqkEvent(newAftershock);
  }



  /**
   * Sets the mainshock event for the given forecast model.
   * @param mainShock ObsEqkRupture
   */
  public void setMainShock(ObsEqkRupture mainShock) {
    this.mainShock = mainShock;
  }
}
