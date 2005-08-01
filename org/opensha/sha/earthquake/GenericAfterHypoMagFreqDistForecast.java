package org.opensha.sha.earthquake;

import org.opensha.sha.earthquake.observedEarthquake.ObsEqkRupture;
import org.opensha.data.region.EvenlyGriddedGeographicRegionAPI;

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
public class GenericAfterHypoMagFreqDistForecast
    extends AfterShockHypoMagFreqDistFoecast implements GriddedHypoMagFreqDistAtLocAPI{


  public GenericAfterHypoMagFreqDistForecast(ObsEqkRupture mainShock,
      EvenlyGriddedGeographicRegionAPI afterShockZone, double minMag,
      double deltaMag, int numMag) {

  }




}
