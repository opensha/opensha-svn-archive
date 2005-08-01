package org.opensha.sha.earthquake;

import org.opensha.data.*;
import org.opensha.sha.magdist.*;

/**
 * <p>Title: HypoMagFreqDistAtLoc</p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2002</p>
 *
 * <p>Company: </p>
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

  public HypoMagFreqDistAtLoc() {}

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
    this.focalMechanism = focalMechanism;
  }

  public void setLocation(Location location) {
    this.location = location;
  }

  public void setMagFreqDist(IncrementalMagFreqDist magFreqDist) {
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

  }
}
