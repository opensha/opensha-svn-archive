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

public class ExactTime implements TimeAPI {
  private GregorianCalendar gregorianCalendar;
  private int era;

  public ExactTime(int year, int month, int day, int hour, int minute, int second, int era) {
    gregorianCalendar = new GregorianCalendar(year, month, day, hour, minute, second);
    this.era = era;
  }

  public ExactTime(GregorianCalendar calendar, int era) {
    this.gregorianCalendar = calendar;
    this.era = era;
  }

  public int getEra() {
    return era;
  }

  public GregorianCalendar getGregorianCalendar() {
    return gregorianCalendar;
  }

}