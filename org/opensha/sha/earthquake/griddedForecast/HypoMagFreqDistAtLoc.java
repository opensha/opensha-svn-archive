package org.opensha.sha.earthquake.griddedForecast;

import org.opensha.data.*;
import org.opensha.sha.magdist.*;
import org.opensha.sha.earthquake.*;

/**
 * <p>Title: HypoMagFreqDistAtLoc</p>
 *
 * <p>Description: This allows user to get a Mag Freq. Distribution for a given location.
 * This is generally used for forecasting hypocenters.</p>
 *
 * @author Nitin Gupta , Vipin Gupta
 * @version 1.0
 */
public class HypoMagFreqDistAtLoc {

  private Location location;
  private IncrementalMagFreqDist[] magFreqDist;
  private FocalMechanism[] focalMechanism;

  public HypoMagFreqDistAtLoc(IncrementalMagFreqDist[] magDist, Location loc) {
    magFreqDist = magDist;
    location = loc;
  }

  public HypoMagFreqDistAtLoc(IncrementalMagFreqDist[] magDist, Location loc,
      FocalMechanism[] focalMechanism) {
   magFreqDist = magDist;
   location = loc;
   this.focalMechanism = focalMechanism;
 }


  public Location getLocation() {
    return location;
  }

  public FocalMechanism[] getFocalMechanism() {
    return focalMechanism;
  }

  public IncrementalMagFreqDist[] getMagFreqDist() {
    return magFreqDist;
  }
}
