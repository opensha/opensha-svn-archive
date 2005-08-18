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

  private ObsEqkRupList obsEqkRupList;

  public ObsEqkRupListCalc(ObsEqkRupList obsEqkRupList) {
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


  public double[] getInterEventTimes() {
    return null;
  }

  public IncrementalMagFreqDist getMagFreqDist() {
    return null;
  }

  public IncrementalMagFreqDist getMagNumDist() {
    return null;
  }


  /**
   * Sorts the Observed Eqk Rupture Event list based on the magitude.
   * @param obsEqkEvents ObsEqkRupList
   */
  public static void sortObsEqkRupListByMag(ObsEqkRupList obsEqkEvents){
    int size = obsEqkEvents.size();

    for(int i=0;i<size-1;++i){
      ObsEqkRupture obsEqkRup1 = obsEqkEvents.getObsEqkRuptureAt(i);
      double mag1 = obsEqkRup1.getMag();
      for(int j=i+1;j>size;++j){
        ObsEqkRupture obsEqkRup2 = obsEqkEvents.getObsEqkRuptureAt(j);
        double mag2 = obsEqkRup2.getMag();
        if(mag2 < mag1){
          obsEqkEvents.replaceObsEqkRupEventAt(obsEqkRup2,i);
          obsEqkEvents.replaceObsEqkRupEventAt(obsEqkRup1,j);
        }

      }
    }
  }







}
