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

    protected GregorianCalendar startTime;

    /** Elapsed time of the event since it's start time, in seconds. */
    protected double duration;

    /** End time of the event in milliseconds. */
    protected long endTime_mSec;

    private final static String START_YEAR = "Start Year";
    private IntegerParameter startYearParam;
    private final static String START_MONTH = "Start Month";
    private IntegerParameter startMonthParam;
    private final static String START_DAY = "Start Day";
    private IntegerParameter startDayParam;
    private final static String START_HOUR = "Start Hour";
    private IntegerParameter startHourParam;
    private final static String START_MINUTE = "Start Minute";
    private IntegerParameter startMinuteParam;
    private final static String START_SECOND = "Start Second";
    private IntegerParameter startSecondParam;

    private final static String DURATION = "Duration";
    private DoubleParameter durationParam;

    // various stings used
    public final static String YEARS = "Years";
    public final static String DAYS = "Days";
    public final static String HOURS = "Hours";
    public final static String MINUTES = "Minutes";
    public final static String SECONDS = "Seconds";
    public final static String ADJUSTABLE = "Adjustable";

    // to define whether start (and end) times are needed
    private boolean startTimeIndependent;

    // to define the units on duration if "Adjustable"
    private String durationUnits;
    private StringParameter durationUnitsParam;

    // to define the maximum precision for the start time
    private String startTimePrecision;

    /**
     *  Constructor
     */
    public TimeSpan(String startTimePrecision, String durationUnits) {
      this.startTimePrecision = startTimePrecision;
      this.durationUnits = durationUnits;

      // create the parameters
      startYearParam = new IntegerParameter(START_YEAR,1,Integer.MAX_VALUE,new Integer(2000));
      startMonthParam = new IntegerParameter(START_MONTH,1,12,new Integer(1));
      startDayParam = new IntegerParameter(START_DAY,1,32,new Integer(1));
      startHourParam = new IntegerParameter(START_HOUR,1,24,new Integer(1));
      startMinuteParam = new IntegerParameter(START_MINUTE,1,60,new Integer(1));
      startSecondParam = new IntegerParameter(START_SECOND,1,60,new Integer(1));

      durationParam = new DoubleParameter(DURATION,durationUnits);

    }


    /**
     *  Create a TimeSpan with a duration (seconds). Defaults to right
     *  now as the start time.
     *
     * @param  interval  duration  of the event
     */
    public TimeSpan( double interval ) {
        this.duration = interval;
        endTime_mSec =  startTime.getTime().getTime() + (long)(duration * 1000)  ;
    }


    /**
     * Create a TimeSpan with a start date and a  duration (seconds).
     *
     * @param  cal       Start time
     * @param  interval  Interval of the event
     */
    public TimeSpan( GregorianCalendar cal, double duration ) {

      startTime = new GregorianCalendar(TimeZone.getDefault(), Locale.getDefault());
      startTime.set(Calendar.ERA, GregorianCalendar.AD);
      startTime.set(Calendar.YEAR, cal.get(Calendar.YEAR));
      startTime.set(Calendar.MONTH, cal.get(Calendar.MONTH));
      startTime.set(Calendar.DATE, cal.get(Calendar.DATE));
      startTime.set(Calendar.HOUR_OF_DAY, cal.get(Calendar.HOUR_OF_DAY));
      startTime.set(Calendar.MINUTE, cal.get(Calendar.MINUTE));
      startTime.set(Calendar.SECOND, cal.get(Calendar.SECOND));
      startTime.set(Calendar.MILLISECOND, cal.get(Calendar.MILLISECOND));

      this.duration = duration;
      endTime_mSec =  startTime.getTime().getTime() + (long)(duration * 1000);

    }


    /** Sets the elapsed time of this event in seconds. */
    public void setDuration( double duration ) {
        this.duration = duration;
        endTime_mSec =  startTime.getTime().getTime() + (long)(duration * 1000)  ;
    }

    /** Sets the elapsed time of this event in seconds. */
    public void setStartTime( GregorianCalendar cal ) {

      startTime.set(Calendar.ERA, GregorianCalendar.AD);
      startTime.set(Calendar.YEAR, cal.get(Calendar.YEAR));
      startTime.set(Calendar.MONTH, cal.get(Calendar.MONTH));
      startTime.set(Calendar.DATE, cal.get(Calendar.DATE));
      startTime.set(Calendar.HOUR_OF_DAY, cal.get(Calendar.HOUR_OF_DAY));
      startTime.set(Calendar.MINUTE, cal.get(Calendar.MINUTE));
      startTime.set(Calendar.SECOND, cal.get(Calendar.SECOND));
      startTime.set(Calendar.MILLISECOND, cal.get(Calendar.MILLISECOND));

      endTime_mSec =  startTime.getTime().getTime() + (long)(duration * 1000)  ;
    }

    /** Sets the end time of this event in seconds.  The duration is then computed
     *  from the startTime
     */
    public void setEndTime( GregorianCalendar cal ) throws InvalidRangeException{

        String S = C + ": setEndTime():";

        long start = startTime.getTime().getTime();  //1st getTime returns a Date object, second (long) milliseconds
        long end = cal.getTime().getTime();

        if( end <= start ) throw new InvalidRangeException(S + "End time cannot be before or equal to the start time");

        endTime_mSec = end;
        this.duration =  Math.round( (double) ( ( end - start ) / 1000 ) );
    }

    /** Returns the elapsed time of this event in seconds. */
    public double getDuration() { return duration; }


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
    public GregorianCalendar getStartTime(  ) { return startTime; }


    // this is temporary for testing purposes
    public static void main(String[] args) {
      GregorianCalendar cal = new GregorianCalendar(2000,1,1,1,1,1);
      double dur = 3600;
      TimeSpan tspan = new TimeSpan(cal,dur);
      GregorianCalendar calEnd = tspan.getEndTime();
      System.out.println(cal.toString());
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
    }
}

