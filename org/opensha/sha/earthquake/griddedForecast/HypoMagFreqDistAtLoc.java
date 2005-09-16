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
 * @author Nitin Gupta
 * @version 1.0
 */
public class HypoMagFreqDistAtLoc {

  private Location location;
  private IncrementalMagFreqDist[] magFreqDist;
  private FocalMechanism[] focalMechanism;

  /**
   * Class Constructor.
   * In this case the no focalMechanism has been specified for the given location.
   * @param magDist IncrementalMagFreqDist[] list of MagFreqDist for the given location.
   * @param loc Location
   */
  public HypoMagFreqDistAtLoc(IncrementalMagFreqDist[] magDist, Location loc) {
    magFreqDist = magDist;
    location = loc;
  }

  /**
   * Class constructor.
   * This constructor allows user to give a list of focalMechanisms for a given
   * location.
   * @param magDist IncrementalMagFreqDist[] list of magFreqDist, based on number
   * of focal mechanism for a given location.
   * @param loc Location Location
   * @param focalMechanism FocalMechanism[] list of focal mechanism for a given location.
   *
   */
  public HypoMagFreqDistAtLoc(IncrementalMagFreqDist[] magDist, Location loc,
                              FocalMechanism[] focalMechanism) {
    magFreqDist = magDist;
    location = loc;
    this.focalMechanism = focalMechanism;
  }

  /**
   * Returns the Location at which MagFreqDist(s) is calculated.
   * @return Location
   */
  public Location getLocation() {
    return location;
  }


  /**
   * Returns the list of Focal Mechanism.
   * @return FocalMechanism[]
   */
  public FocalMechanism[] getFocalMechanism() {
    return focalMechanism;
  }

  /**
   * Returns the list of MagFreqDist for a given location.
   * @return IncrementalMagFreqDist[]
   */
  public IncrementalMagFreqDist[] getMagFreqDist() {
    return magFreqDist;
  }
}
