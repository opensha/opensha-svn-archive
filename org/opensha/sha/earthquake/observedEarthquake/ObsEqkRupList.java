package org.opensha.sha.earthquake.observedEarthquake;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import org.opensha.data.region.GeographicRegion;

/**
 * <p>Title: ObsEqkRupList</p>
 *
 * <p>Description: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class ObsEqkRupList extends ArrayList{
  public ObsEqkRupList() {
  }


  /**
   *
   * @param mag double
   * @return ArrayList
   */
  public ObsEqkRupList getObsEqkRupsAboveMag(double mag){
    return null;
  }


  /**
   *
   * @param mag double
   * @return ArrayList
   */
  public  ObsEqkRupList getObsEqkRupsBelowMag(double mag){
    return null;
  }


  /**
   *
   * @param mag1 double
   * @param mag2 double
   * @return ArrayList
   */
  public ObsEqkRupList getObsEqkRupsBetweenMag(double mag1, double mag2){
    return null;
  }


  /**
   *
   * @param cal GregorianCalendar
   * @return ArrayList
   */
  public ObsEqkRupList getObsEqkRupsBefore(GregorianCalendar cal) {
    return null;
  }


  /**
   *
   * @param cal GregorianCalendar
   * @return ArrayList
   */
  public ObsEqkRupList getObsEqkRupsAfter(GregorianCalendar cal) {
    return null;
  }

  /**
   *
   * @param cal1 GregorianCalendar
   * @param cal2 GregorianCalendar
   * @return ArrayList
   */
  public ObsEqkRupList getObsEqkRupsBetween(GregorianCalendar cal1,GregorianCalendar cal2) {
     return null;
  }

  /**
   *
   * @param region GeographicRegion
   * @return ArrayList
   */
  public ObsEqkRupList getObsEqkRupsInside(GeographicRegion region) {
     return null;
  }


  /**
   *
   * @param region GeographicRegion
   * @return ArrayList
   */
  public ObsEqkRupList getObsEqkRupsOutside(GeographicRegion region) {
     return null;
  }
}
