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
 * <p>Description: This stores a list magFreqDists with associated focal mechanisms.</p>
 *
 * @author Ned Field
 * @version 1.0
 */
public class MagFreqDistsForFocalMechs implements java.io.Serializable{

  private IncrementalMagFreqDist[] magFreqDist;
  private FocalMechanism[] focalMechanism = null;

  /**
   * Class Constructor.
   * In this case the no focalMechanisms are specified.
   * @param magDist IncrementalMagFreqDist[] list of MagFreqDist.
   */
  public MagFreqDistsForFocalMechs(IncrementalMagFreqDist[] magDist) {
    magFreqDist = magDist;
  }
  
  
  /**
   * Class Constructor.
   * This is for passing in a single magFreqDist (don't have to create an array) and no focal mechanism.
   * @param magDist IncrementalMagFreqDist MagFreqDist.
   */
  public MagFreqDistsForFocalMechs(IncrementalMagFreqDist magDist) {
    magFreqDist = new IncrementalMagFreqDist[1];
    magFreqDist[0] = magDist; 
  }

  /**
   * Class constructor.
   * This constructor allows user to give a list of focalMechanisms.
   * @param magDist IncrementalMagFreqDist[] list of magFreqDist, same as number of focal mechanisms.
   * @param focalMechanism FocalMechanism[] list of focal mechanism for a given location.
   *
   */
  public MagFreqDistsForFocalMechs(IncrementalMagFreqDist[] magDist, FocalMechanism[] focalMechanism) {
    magFreqDist = magDist;
    this.focalMechanism = focalMechanism;
    if(magDist.length != focalMechanism.length)
    	throw new RuntimeException("Error - array lengths differ");
  }


  /**
   * Returns the list of Focal Mechanism.
   * @return FocalMechanism[]
   */
  public FocalMechanism[] getFocalMechanism() {
    return focalMechanism;
  }

  /**
   * Returns the list of MagFreqDists.
   * @return IncrementalMagFreqDist[]
   */
  public IncrementalMagFreqDist[] getMagFreqDist() {
    return magFreqDist;
  }
  
  /**
   * Returns the first MagFreqDist from the list.
   * This function can be used when there are not multiple MagFreqDists and no Focal Mechanism, 
   * @return IncrementalMagFreqDist
   */
  public IncrementalMagFreqDist getFirstMagFreqDist(){
	return magFreqDist[0];
  }
}
