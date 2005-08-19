package org.opensha.sha.earthquake.observedEarthquake;

import java.util.Collections;

import org.opensha.sha.magdist.IncrementalMagFreqDist;
import org.opensha.sha.earthquake.EqkRuptureMagComparator;

/**
 * <p>Title: ObsEqkRupListCalc</p>
 *
 * <p>Description: </p>
 *
 * @author Nitin Gupta
 * @version 1.0
 */
public class ObsEqkRupListCalc {


  public static double getMeanMag(ObsEqkRupList obsEqkEvents){
    return 0.0;
  }

  /**
   * Returns the minimum magnitude for the observed Eqk Rupture events.
   * @param obsEqkEvents ObsEqkRupList list of observed eqk events
   * @return double min-mag
   */
  public static double getMinMag(ObsEqkRupList obsEqkEvents) {
    Double minMag = (Double)Collections.min(obsEqkEvents.getObsEqkRupEventList(),new EqkRuptureMagComparator());
    return minMag.doubleValue();
  }


  /**
   * Returns the maximum magnitude for the observed Eqk Rupture events.
   * @param obsEqkEvents ObsEqkRupList list of observed eqk events
   * @return double max-mag
   */
  public static double getMaxMag(ObsEqkRupList obsEqkEvents) {
    Double maxMag = (Double)Collections.max(obsEqkEvents.getObsEqkRupEventList(),new EqkRuptureMagComparator());
    return maxMag.doubleValue();
  }


  public static double[] getInterEventTimes(ObsEqkRupList obsEqkEvents) {
    return null;
  }

  public static IncrementalMagFreqDist getMagFreqDist(ObsEqkRupList obsEqkEvents) {
    return null;
  }

  public static IncrementalMagFreqDist getMagNumDist(ObsEqkRupList obsEqkEvents) {
    return null;
  }











}
