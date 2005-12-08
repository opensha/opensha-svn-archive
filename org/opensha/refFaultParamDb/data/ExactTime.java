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
  int year, month, day, hour, minute, second;
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
    this(year, month, day, hour, minute, second);
    setEra(era);
  }

  public String toString() {
    return "Year="+year+" "+ era+"\n"+
        "Month="+month+"\n"+
        "Day="+day+"\n"+
        "Hour="+hour+"\n"+
        "Minute="+minute+"\n"+
        "Second="+second+"\n"+
        super.toString();
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
   this.year = year;
   this.month = month;
   this.day = day;
   this.hour = hour;
   this.minute = minute;
   this.second = second;
  }

  public String getEra() {
    return era;
  }

  public void setEra(String era) {
    this.era = era;
  }

  public int getYear() {
    return this.year;
  }
  public int getMonth() {
     // adjust month as gregorian calendar months go from 0 to 11
    return this.month;
  }
  public int getDay() {
    return this.day;
  }
  public int getHour() {
    return this.hour;
  }
  public int getMinute() {
    return this.month;
  }
  public int getSecond() {
    return this.second;
  }

}