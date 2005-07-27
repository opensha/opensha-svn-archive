package org.opensha.sha.earthquake.observedEarthquake;

import org.opensha.sha.magdist.IncrementalMagFreqDist;

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
public class ObsEqkRupListCalc {

  private ObservedEqkRupList obsEqkRupList;

  public ObsEqkRupListCalc(ObservedEqkRupList obsEqkRupList) {
    this.obsEqkRupList = obsEqkRupList;
  }


  public double getMeanMag(){
    return 0.0;
  }

  public double getMinMag() {
    return 0.0;
  }

  public double getMaxMag() {
      return 0.0;
  }


  public double getInterEventTimes() {
    return 0.0;
  }

  public IncrementalMagFreqDist getMagFreqDist() {
    return null;
  }

  public IncrementalMagFreqDist getMagNumDist() {
    return null;
  }


}
