package org.opensha.sha.earthquake.observedEarthquake;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import org.opensha.data.region.GeographicRegion;

/**
 * <p>Title: ObservedEqkRupList</p>
 *
 * <p>Description: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class ObservedEqkRupList extends ArrayList{
  public ObservedEqkRupList() {
  }


  /**
   *
   * @param mag double
   * @return ArrayList
   */
  public ArrayList getObsEqkRupsAboveMag(double mag){
    return null;
  }


  /**
   *
   * @param mag double
   * @return ArrayList
   */
  public ArrayList getObsEqkRupsBelowMag(double mag){
    return null;
  }


  /**
   *
   * @param mag1 double
   * @param mag2 double
   * @return ArrayList
   */
  public ArrayList getObsEqkRupsBetweenMag(double mag1, double mag2){
    return null;
  }


  /**
   *
   * @param cal GregorianCalendar
   * @return ArrayList
   */
  public ArrayList getObsEqkRupsBefore(GregorianCalendar cal) {
    return null;
  }


  /**
   *
   * @param cal GregorianCalendar
   * @return ArrayList
   */
  public ArrayList getObsEqkRupsAfter(GregorianCalendar cal) {
    return null;
  }

  /**
   *
   * @param cal1 GregorianCalendar
   * @param cal2 GregorianCalendar
   * @return ArrayList
   */
  public ArrayList getObsEqkRupsBetween(GregorianCalendar cal1,GregorianCalendar cal2) {
     return null;
  }

  /**
   *
   * @param region GeographicRegion
   * @return ArrayList
   */
  public ArrayList getObsEqkRupsInside(GeographicRegion region) {
     return null;
  }


  /**
   *
   * @param region GeographicRegion
   * @return ArrayList
   */
  public ArrayList getObsEqkRupsOutside(GeographicRegion region) {
     return null;
  }
}
