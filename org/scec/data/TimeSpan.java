package org.scec.data;

import java.util.*;
import org.scec.exceptions.InvalidRangeException;
import org.scec.param.*;

/**
 *  <b>Title:</b> TimeSpan<p>
 *
 *  <b>Description:</b> Represents a start time and a duration, from which you
 *  can calculate the end time of an event.<p>
 *
 * Start- and End-Times are based on YEAR, MONTH, DATE, HOUR, MINUTE, SECOND,
 * and MILLISECOND (ERA is restrcted to AD).<p>
 *
 * Duration can be specified in units of  YEARS, MONTHS, DAYS, HOURS, MINUTES,
 * SECONDS, or MILLISECONDS (only one cas be used to set duration).<p>
 *
 *
 *
 * @author     Sid Hellman, Steven W. Rock, and Ned Field
 * @created    February 20, 2002
 * @version    1.0
 */
public class TimeSpan {

    /** The name of this class, used for debug statements */
    protected final static String C = "TimeSpan";

    /** Static boolean whether to print out debugging statements */
    protected final static boolean D = false;

    protected GregorianCalendar startTimeCal;

    // End Time In Milliseconds
    protected long endTime_mSec;

    // temp duration????
    double duration;

    // Start-Time Parameters
    private final static String START_YEAR = "Start Year";
    private IntegerParameter startYearParam;
    private IntegerConstraint startYearConstraint = new IntegerConstraint(0,Integer.MAX_VALUE);
    private final static Integer START_YEAR_DEFAULT = new Integer(2003);
    private final static String START_MONTH = "Start Month";
    private IntegerParameter startMonthParam;
    private IntegerConstraint startMonthConstraint = new IntegerConstraint(1,12);
    private final static Integer START_MONTH_DEFAULT = new Integer(1);
    private final static String START_DAY = "Start Day";
    private IntegerParameter startDayParam;
    private final static Integer START_DAY_DEFAULT = new Integer(1);
    private IntegerConstraint startDayConstraint = new IntegerConstraint(1,31);
    private final static String START_HOUR = "Start Hour";
    private IntegerParameter startHourParam;
    private final static Integer START_HOUR_DEFAULT = new Integer(0);
    private IntegerConstraint startHourConstraint = new IntegerConstraint(0,59);
    private final static String START_MINUTE = "Start Minute";
    private IntegerParameter startMinuteParam;
    private final static Integer START_MINUTE_DEFAULT = new Integer(0);
    private IntegerConstraint startMinuteConstraint = new IntegerConstraint(0,59);
    private final static String START_SECOND = "Start Second";
    private IntegerParameter startSecondParam;
    private final static Integer START_SECOND_DEFAULT = new Integer(0);
    private IntegerConstraint startSecondConstraint = new IntegerConstraint(0,Integer.MAX_VALUE);
    private final static String START_MILLISECOND = "Start Second";
    private IntegerParameter startMillisecondParam;
    private IntegerConstraint startMillisecondConstraint = new IntegerConstraint(0,999);
    private final static Integer START_MILLISECOND_DEFAULT = new Integer(0);

    // Misc Strings
    public final static String YEARS = "Years";
    public final static String MONTHS = "Months";
    public final static String DAYS = "Days";
    public final static String HOURS = "Hours";
    public final static String MINUTES = "Minutes";
    public final static String SECONDS = "Seconds";
    public final static String MILLISECONDS = "Milliseconds";
    public final static String NONE = "None";

    // For Duration Units Parameter
    private final static String DURATION_UNITS = "Duration Units";
    private final static String DURATION_UNITS_DEFAULT = YEARS;
    private StringParameter durationUnitsParam;

    // For Duration Parameter
    private final static String DURATION = "Duration";
    private final static Double DURATION_DEFAULT = new Double(50.);
    private DoubleConstraint durationConstraint = new DoubleConstraint(0.0,Double.MAX_VALUE);
    private DoubleParameter durationParam;

   // to define the maximum precision for the start time
    public final static String START_TIME_PRECISION = "Start-Time Precision";
    private String START_TIME_PRECISION_DEFAULT = YEARS;
    private StringParameter startTimePrecisionParam;

    /**
     *  Constructor
     */
    public TimeSpan() {
      initParams();

    }


    /**
     *  Create a TimeSpan with a duration (seconds). Defaults to right
     *  now as the start time.
     *
     * @param  interval  duration  of the event
     */
    public TimeSpan( double interval ) {
        this.duration = interval;
        endTime_mSec =  startTimeCal.getTime().getTime() + (long)(duration * 1000)  ;
    }


    /**
     * Initialize Parameters
     */

    private void initParams() {

      // Start Time Parameters

      startYearParam = new IntegerParameter(START_YEAR,startYearConstraint,START_YEAR_DEFAULT);
      startMonthParam = new IntegerParameter(START_MONTH,startMonthConstraint,START_MONTH_DEFAULT);
      startDayParam = new IntegerParameter(START_DAY,startDayConstraint,START_DAY_DEFAULT);
      startHourParam = new IntegerParameter(START_HOUR,startHourConstraint,START_HOUR_DEFAULT);
      startMinuteParam = new IntegerParameter(START_MINUTE,startMinuteConstraint,START_MINUTE_DEFAULT);
      startSecondParam = new IntegerParameter(START_SECOND,startSecondConstraint,START_SECOND_DEFAULT);
      startMillisecondParam = new IntegerParameter(START_MILLISECOND,startMillisecondConstraint,START_MILLISECOND_DEFAULT);

      // Duration Units Parameter
      StringConstraint durationUnitsConstraint = new StringConstraint();
      durationUnitsConstraint.addString( YEARS );
      durationUnitsConstraint.addString( DAYS );
      durationUnitsConstraint.addString( HOURS );
      durationUnitsConstraint.addString( MINUTES );
      durationUnitsConstraint.addString( SECONDS );
      durationUnitsConstraint.addString( MILLISECONDS );
      durationUnitsConstraint.setNonEditable();
      durationUnitsParam = new StringParameter(this.DURATION_UNITS,durationUnitsConstraint,DURATION_UNITS_DEFAULT);

      // Duration Parameter
      durationParam = new DoubleParameter(DURATION,durationConstraint,DURATION_UNITS_DEFAULT,DURATION_DEFAULT);

      // Start Time Precision Parameter
      StringConstraint precisionConstraint = new StringConstraint();
      precisionConstraint.addString( YEARS );
      precisionConstraint.addString( MONTHS );
      precisionConstraint.addString( DAYS );
      precisionConstraint.addString( HOURS );
      precisionConstraint.addString( MINUTES );
      precisionConstraint.addString( SECONDS );
      precisionConstraint.addString( MILLISECONDS );
      precisionConstraint.addString( NONE );    // this one is for start-time independent models (e.g., Poissonian)
      precisionConstraint.setNonEditable();
      startTimePrecisionParam = new StringParameter(START_TIME_PRECISION,precisionConstraint,
                                                    START_TIME_PRECISION_DEFAULT);
    }



    /**
     * Sets the Start-Time Precision
     * @param startTimePrecision
     */
    public void setStartTimePrecision(String startTimePrecision) {
      startTimePrecisionParam.setValue(startTimePrecision);
    }

    private int getStartTimePrecInt() {
      String precisionUnitString = (String) startTimePrecisionParam.getValue();
      if(precisionUnitString.equalsIgnoreCase(NONE)) return 0;
      else if(precisionUnitString.equalsIgnoreCase(YEARS)) return 1;
      else if(precisionUnitString.equalsIgnoreCase(MONTHS)) return 2;
      else if(precisionUnitString.equalsIgnoreCase(DAYS)) return 3;
      else if(precisionUnitString.equalsIgnoreCase(HOURS)) return 4;
      else if(precisionUnitString.equalsIgnoreCase(MINUTES)) return 5;
      else if(precisionUnitString.equalsIgnoreCase(SECONDS)) return 6;
      else return 7; // milliseconds    }
    }


    // NEEDED ?
    private boolean isAbovePrecision(String timeUnitString) {
      int timeUnitInt, precisionInt;
      String precisionUnitString = (String) startYearParam.getValue();

      // Quantify the relative precision of the timeUnitString
      if(timeUnitString.equalsIgnoreCase(YEARS)) timeUnitInt = 1;
      else if(timeUnitString.equalsIgnoreCase(MONTHS)) timeUnitInt = 2;
      else if(timeUnitString.equalsIgnoreCase(DAYS)) timeUnitInt = 3;
      else if(timeUnitString.equalsIgnoreCase(HOURS)) timeUnitInt = 4;
      else if(timeUnitString.equalsIgnoreCase(MINUTES)) timeUnitInt = 5;
      else if(timeUnitString.equalsIgnoreCase(SECONDS)) timeUnitInt = 6;
      else timeUnitInt = 7; // milliseconds

      // Quantify the relative precision of the precisionUnitString
      if(precisionUnitString.equalsIgnoreCase(YEARS)) precisionInt = 1;
      else if(precisionUnitString.equalsIgnoreCase(MONTHS)) precisionInt = 2;
      else if(precisionUnitString.equalsIgnoreCase(DAYS)) precisionInt = 3;
      else if(precisionUnitString.equalsIgnoreCase(HOURS)) precisionInt = 4;
      else if(precisionUnitString.equalsIgnoreCase(MINUTES)) precisionInt = 5;
      else if(precisionUnitString.equalsIgnoreCase(SECONDS)) precisionInt = 6;
      else precisionInt = 7; // milliseconds

      if(timeUnitInt > precisionInt) return true;
      else return false;

    }

    /**
     * Gets the Start-Time Precision
     * @return
     */
    public String getStartTimePrecision() {
      return (String) startTimePrecisionParam.getValue();
    }

    /**
     * Sets the units for the duration
     * @param durationUnits
     */
    public void setDurationUnits(String durationUnits) {
      durationUnitsParam.setValue(durationUnits);
    }

    /**
     * Gets the units for the duration
     * @return
     */
    public String getDurationUnits() {
      return (String) durationUnitsParam.getValue();
    }


    /**
     * Sets the the duration
     * @param duration
     */
    public void setDuration( double duration ) {
      durationParam.setValue(duration);
    }

    /**
     * Gets the duration
     * @return
     */
    public double getDuration() {
      return ((Double)durationParam.getValue()).doubleValue();
    }

    /**
     * Set the Start Year.
     * @param startYear
     */
    public void setStartYear(int startYear) {
      if(getStartTimePrecInt() >= 1)
        startYearParam.setValue(new Integer(startYear));
      else
        startMonthParam.setValue(START_YEAR_DEFAULT);
    }

    /**
     * Set the Start Month.  This reverts to null
     * @param startMonth
     */
    public void setStartMonth(int startMonth) {
      if(getStartTimePrecInt() >= 2)
        startMonthParam.setValue(new Integer(startMonth));
      else
        startMonthParam.setValue(START_MONTH_DEFAULT);
    }

    /**
     * Set the Start Day
     * @param startDay
     */
    public void setStartDay(int startDay) {
      if(getStartTimePrecInt() >= 3)
        startDayParam.setValue(new Integer(startDay));
      else
        startDayParam.setValue(START_DAY_DEFAULT);
    }

    /**
     * Set the Start Hour
     * @param startHour
     */
    public void setStartHour(int startHour) {
      if(getStartTimePrecInt() >= 4)
        startHourParam.setValue(new Integer(startHour));
      else
        startHourParam.setValue(START_HOUR_DEFAULT);
    }

    /**
     * Set the Start Minute
     * @param startMinute
     */
    public void setStartMinute(int startMinute) {
      if(getStartTimePrecInt() >= 5)
        startMinuteParam.setValue(new Integer(startMinute));
      else
        startMinuteParam.setValue(START_MINUTE_DEFAULT);
    }

    /**
     * Set the Start Second
     * @param startSecond
     */
    public void setStartSecond(int startSecond) {
      if(getStartTimePrecInt() >= 6)
        startSecondParam.setValue(new Integer(startSecond));
      else
        startSecondParam.setValue(START_SECOND_DEFAULT);
    }

    /**
     * Set the Start Millisecond
     * @param startMillisecond
     */
    public void setStartMillisecond(int startMillisecond) {
      if(getStartTimePrecInt() >= 7)
        startMillisecondParam.setValue(new Integer(startMillisecond));
      else
        startMillisecondParam.setValue(START_MILLISECOND_DEFAULT);
    }


    /**
     * This sets the Start-Time Calendar, making any fields greater than the
     * Start-Time Precision equal to defaults (usually the lowest allowed value).
     * This throws an exception if the Day is incompatable with the chosen Month
     * @throws Exception
     */
    private void buildStartTimeCal() throws Exception {

      int year, month, day, hour, minute, second, millisecond;

      // get the precision integer
      int precisionInt = getStartTimePrecInt();

      // now set each field's primitave according to the precision

      // set year since this is always used
      year = ((Integer) startYearParam.getValue()).intValue();

      // set the month (subtract one to make compatible with GregorianCalendar indexing)
      if(precisionInt>1)
        month = ((Integer) startMonthParam.getValue()).intValue()-1;
      else
        month = this.START_MONTH_DEFAULT.intValue()-1;

      // set the day
      if(precisionInt>2)
        day = ((Integer) startDayParam.getValue()).intValue();
      else
        day = this.START_DAY_DEFAULT.intValue();

      // set the hour
      if(precisionInt>3)
        hour = ((Integer) startHourParam.getValue()).intValue();
      else
        hour = this.START_HOUR_DEFAULT.intValue();

      // set the minute
      if(precisionInt>4)
        minute = ((Integer) startMinuteParam.getValue()).intValue();
      else
        minute = this.START_MINUTE_DEFAULT.intValue();

      // set the second
      if(precisionInt>5)
        second = ((Integer) startSecondParam.getValue()).intValue();
      else
        second = this.START_SECOND_DEFAULT.intValue();

      // set the millisecond
      if(precisionInt>6)
        millisecond = ((Integer) startMillisecondParam.getValue()).intValue();
      else
        millisecond = START_MILLISECOND_DEFAULT.intValue();

      // now make the calendar
      startTimeCal = new GregorianCalendar();
      // make this throw exceptions for bogus values (rather than fixing them  )
      startTimeCal.setLenient(false);
      startTimeCal.set(Calendar.ERA, GregorianCalendar.AD);
      startTimeCal.set(Calendar.YEAR,year);
      startTimeCal.set(Calendar.MONTH,month);
      // make sure day is valid for the chosen month
      try {
        startTimeCal.set(Calendar.DATE,day);
      } catch (Exception e) {
        throw new Exception("TimeSpan: Invalid Day for the chosen Month");
      }
      startTimeCal.set(Calendar.HOUR_OF_DAY,hour);
      startTimeCal.set(Calendar.MINUTE,minute);
      startTimeCal.set(Calendar.SECOND,second);
      startTimeCal.set(Calendar.MILLISECOND,millisecond);
    }



    /** Sets the elapsed time of this event in seconds. */
    public void setStartTimeCal( GregorianCalendar cal ) {

    }

    /** Sets the end time of this event in seconds.  The duration is then computed
     *  from the startTime
     */
    public void setEndTime( GregorianCalendar cal ) throws InvalidRangeException{

        String S = C + ": setEndTime():";

        long start = startTimeCal.getTime().getTime();  //1st getTime returns a Date object, second (long) milliseconds
        long end = cal.getTime().getTime();

        if( end <= start ) throw new InvalidRangeException(S + "End time cannot be before or equal to the start time");

        endTime_mSec = end;
        this.duration =  Math.round( (double) ( ( end - start ) / 1000 ) );
    }


    /**
     *  get the end time.
     *
     */
    public GregorianCalendar getEndTime(  ) {
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime( new Date( endTime_mSec ) );
        return cal;
    }

    /** Returns the start time of this event */
    public GregorianCalendar getStartTimeCal(  ) throws Exception{

      buildStartTimeCal();
      return startTimeCal;
    }


    // this is temporary for testing purposes
    public static void main(String[] args) {
      TimeSpan tSpan = new TimeSpan();
      tSpan.setStartTimePrecision(MILLISECONDS);
      tSpan.setStartYear(1964);
      tSpan.setStartMonth(11);
      tSpan.setStartDay(18);
      tSpan.setStartHour(19);
      tSpan.setStartMinute(20);
      tSpan.setStartSecond(21);
      tSpan.setStartMillisecond(22);

      GregorianCalendar cal = new GregorianCalendar();
      try {
      cal = tSpan.getStartTimeCal();
      }
      catch(Exception e) {

      }
      System.out.println(cal.toString());
      System.out.print("Start: \nYear: "+cal.get(Calendar.YEAR)+"; ");
      System.out.print("Month: "+cal.get(Calendar.MONTH)+"; ");
      System.out.print("Day: "+cal.get(Calendar.DATE)+"; ");
      System.out.print("Hour: "+cal.get(Calendar.HOUR_OF_DAY)+"; ");
      System.out.print("Min: "+cal.get(Calendar.MINUTE)+"; ");
      System.out.print("Sec: "+cal.get(Calendar.SECOND)+"; ");
      System.out.print("mSec: "+cal.get(Calendar.MILLISECOND)+"; \n");

 /*
      GregorianCalendar cal = new GregorianCalendar();
      cal.setLenient(false);
      cal.set(Calendar.YEAR,2001);
      cal.set(Calendar.MONTH,0);
      cal.set(Calendar.DATE,32);
      cal.set(Calendar.HOUR_OF_DAY,0);
      cal.set(Calendar.MINUTE,0);
      cal.set(Calendar.SECOND,0);

      double dur = 3600;
      TimeSpan tspan = new TimeSpan(cal,dur);
      GregorianCalendar calEnd = tspan.getEndTime();

      System.out.println(cal.toString());
      System.out.print("getTime(): "+cal.getTime().getTime());
      System.out.print("Start: Year: "+cal.get(Calendar.YEAR)+"; ");
      System.out.print("Month: "+cal.get(Calendar.MONTH)+"; ");
      System.out.print("Day: "+cal.get(Calendar.DATE)+"; ");
      System.out.print("Hour: "+cal.get(Calendar.HOUR_OF_DAY)+"; ");
      System.out.print("Min: "+cal.get(Calendar.MINUTE)+"; ");
      System.out.print("Sec: "+cal.get(Calendar.SECOND)+"; \n");

      System.out.print("End:   Year: "+calEnd.get(Calendar.YEAR)+"; ");
      System.out.print("Month: "+calEnd.get(Calendar.MONTH)+"; ");
      System.out.print("Day: "+calEnd.get(Calendar.DATE)+"; ");
      System.out.print("Hour: "+calEnd.get(Calendar.HOUR_OF_DAY)+"; ");
      System.out.print("Min: "+calEnd.get(Calendar.MINUTE)+"; ");
      System.out.print("Sec: "+calEnd.get(Calendar.SECOND)+"; \n");
*/
    }
}

