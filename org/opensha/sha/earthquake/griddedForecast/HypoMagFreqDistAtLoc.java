package org.opensha.sha.earthquake.griddedForecast;

import org.opensha.data.*;
import org.opensha.sha.magdist.*;
import org.opensha.sha.earthquake.*;

/**
 * <p>Title: HypoMagFreqDistAtLoc</p>
 *
 * <p>Description: This allows user to get a Mag Freq. for a given location.</p>
 *
 * @author Nitin Gupta , Vipin Gupta
 * @version 1.0
 */
public class HypoMagFreqDistAtLoc {

  private Location location;
  private IncrementalMagFreqDist magFreqDist;
  private FocalMechanism focalMechanism;
  private boolean editable = true;
  private boolean ignoreFocalMechanism;

  public HypoMagFreqDistAtLoc(IncrementalMagFreqDist magDist, Location loc) {
    magFreqDist = magDist;
    location = loc;
    editable = false;
  }

  public HypoMagFreqDistAtLoc(IncrementalMagFreqDist magDist, Location loc,
      FocalMechanism focalMechanism) {
   magFreqDist = magDist;
   location = loc;
   this.focalMechanism = focalMechanism;
   editable = false;
 }


  public Location getLocation() {
    return location;
  }

  public FocalMechanism getFocalMechanism() {
    return focalMechanism;
  }

  public IncrementalMagFreqDist getMagFreqDist() {
    return magFreqDist;
  }

  public void setFocalMechanism(FocalMechanism focalMechanism) {
     if(editable)
       this.focalMechanism = focalMechanism;
  }

  public void setLocation(Location location) {
     if(editable)
       this.location = location;
  }

  public void setMagFreqDist(IncrementalMagFreqDist magFreqDist) {
    if(editable)
      this.magFreqDist = magFreqDist;
  }

  public void ignoreFocalMechanism(boolean focalMechanism){
    ignoreFocalMechanism = focalMechanism;
  }

  /**
   * Does not allows the user to edit any thing in this class.
   * User can only call get methods on the object.
   */
  public void makeUneditable(){
    editable = false;
  }
}
