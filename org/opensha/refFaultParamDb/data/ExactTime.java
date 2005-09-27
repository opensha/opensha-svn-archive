package org.opensha.refFaultParamDb.data;
import java.util.GregorianCalendar;
/**
 * <p>Title: ExactTime.java </p>
 * <p>Description: This class hold the exact time. This time can be a event time
 * or a start/end time in a timespan</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class ExactTime extends TimeAPI {
  private GregorianCalendar gregorianCalendar;
  private String era;

  /**
   *
   * @param year
   * @param month
   * @param day
   * @param hour
   * @param minute
   * @param second
   * @param era
   */
  public ExactTime(int year, int month, int day, int hour, int minute, int second, String era) {
    // adjust month as gregorian calendar months go from 0 to 11
    gregorianCalendar = new GregorianCalendar(year, month-1, day, hour, minute, second);
    setEra(era);
  }


  /**
   *
   * @param year
   * @param month
   * @param day
   * @param hour
   * @param minute
   * @param second
   */
  public ExactTime(int year, int month, int day, int hour, int minute, int second) {
    // adjust month as gregorian calendar months go from 0 to 11
    gregorianCalendar = new GregorianCalendar(year, month-1, day, hour, minute, second);
  }



  public String getEra() {
    return era;
  }

  public void setEra(String era) {
    this.era = era;
  }

  public int getYear() {
    return gregorianCalendar.get(GregorianCalendar.YEAR);
  }
  public int getMonth() {
     // adjust month as gregorian calendar months go from 0 to 11
    return gregorianCalendar.get(GregorianCalendar.MONTH)+1;
  }
  public int getDay() {
    return gregorianCalendar.get(GregorianCalendar.DATE);
  }
  public int getHour() {
    return gregorianCalendar.get(GregorianCalendar.HOUR);
  }
  public int getMinute() {
    return gregorianCalendar.get(GregorianCalendar.MINUTE);
  }
  public int getSecond() {
    return gregorianCalendar.get(GregorianCalendar.SECOND);
  }

}