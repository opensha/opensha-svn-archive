package org.opensha.sha.earthquake.observedEarthquake;

import java.util.Collections;

import org.opensha.sha.magdist.IncrementalMagFreqDist;
import org.opensha.sha.earthquake.EqkRuptureMagComparator;
import org.opensha.sha.earthquake.observedEarthquake.ObsEqkRupEventOriginTimeComparator;

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
    Collections.sort(obsEqkEvents.getObsEqkRupEventList(), new EqkRuptureMagComparator());
  }

  /**
   * Sorts the Observed Eqk Rupture Event list based on the Origin time.
   * @param obsEqkEvents ObsEqkRupList
   */
  public static void sortObsEqkRupListByOriginTime(ObsEqkRupList obsEqkEvents) {
    Collections.sort(obsEqkEvents.getObsEqkRupEventList(),
                     new ObsEqkRupEventOriginTimeComparator());
  }








}
