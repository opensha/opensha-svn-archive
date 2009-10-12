/*******************************************************************************
 * Copyright 2009 OpenSHA.org in partnership with
 * the Southern California Earthquake Center (SCEC, http://www.scec.org)
 * at the University of Southern California and the UnitedStates Geological
 * Survey (USGS; http://www.usgs.gov)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package org.opensha.sha.earthquake.griddedForecast;

import org.opensha.commons.data.Location;
import org.opensha.sha.earthquake.FocalMechanism;
import org.opensha.sha.magdist.IncrementalMagFreqDist;

/**
 * <p>Title: HypoMagFreqDistAtLoc</p>
 *
 * <p>Description: This allows user to get a Mag Freq. Distribution for a given location.
 * This is generally used for forecasting hypocenters.</p>
 *
 * @author Nitin Gupta
 * @version 1.0
 */
public class HypoMagFreqDistAtLoc implements java.io.Serializable{

  private Location location;
  private IncrementalMagFreqDist[] magFreqDist;
  private FocalMechanism[] focalMechanism = null;

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
   * Class Constructor.
   * In this case the no focalMechanism has been specified for the given location.
   * @param magDist IncrementalMagFreqDist MagFreqDist for the given location.
   * @param loc Location
   */
  public HypoMagFreqDistAtLoc(IncrementalMagFreqDist magDist, Location loc) {
    magFreqDist = new IncrementalMagFreqDist[1];
    magFreqDist[0] = magDist; 
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
  
  /**
   * Returns the first MagFreqDist for a given location.
   * This function can be used when there are no multiple MagFreqDist as no Focal Mechanism, 
   * so just have one single MagFreqDist for a given location.
   * @return IncrementalMagFreqDist
   */
  public IncrementalMagFreqDist getFirstMagFreqDist(){
	return magFreqDist[0];
  }
}
