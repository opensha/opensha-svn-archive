package org.scec.data;

import java.util.*;
import org.scec.exceptions.InvalidRangeException;
import org.scec.param.*;

/**
 *  <b>Title:</b> TimeSpan<p>
 *
 *  <b>Description:</b> This object represents a start time and a duration.<p>
 *
 * The start-time is represented with a Year, Month, Day, Hour, Minute, Second,
 * and Millisecond, all of which are stored internally with IntegerParameter objects.
 * The default constraints (range of allowed values) for these parameters are:<p>
 * <UL>
 * <LI>Year - 0 to Integer.MAX_VALUE (AD or "common erra")
 * <LI>Month - 1 to 12
 * <LI>Day - 1 to 31
 * <LI>Hour - 0 to 23
 * <LI>Minute - 0 to 59
 * <LI>Second - 0 to 59
 * <LI>Millisecond - 0 to 999
 * </UL><p>
 * <p>

 * Important Notes: 1) the Month parameter constratints here (1 to 12) differ from the 0-11
 * range in the java.util.GregorianCalendar object.  This means that what's returned from
 * the getStartTimeMonth() method is always one greater than what's obtained using
 * getStartTimeCalendar().get(Calendar.MONTH)). Keep this in mind if you use the setStartTimeCalendar(),
 * getStartTimeCalendar(), or getEndTimeCalendar methods.  2) the Day and Hour fields here correspond
 * to the DATE and HOUR_OF_DAY fields, respecively in java.util.GregorianCalendar (the HOUR field of
 * GregorianCalendar goes from 0 to 11 rather than 0 to 23).<p>
 *
 * The above start-time parameter constraints can be overridden using the ???? method.
 * NEEDS TO BE ADDED!!!!!!!!!!! <p>
 *
 * The startTimePrecision field specifies the level of precision.  For example, if this
 * is set as "Days", then one cannot set or get the Hours, Minute, Second, or Millisecond
 * fields (and the associated methods throw exceptions).  Setting the startTimePrecsion as
 * "None" indicates that only the Duration is relevant (e.g., for a Poissonian forecast).
 * Presently one can only set the startTimePrecision in the constructor, but we could relax
 * this later. <p>
 *
 * Before a value is returned from any one of the getStartTime*() methods, it is
 * first confirmed that the start-time parameter settings correspond to an acutual
 * date.  For example, assuming the startTimePrecision is "Months", one could execute
 * the method: setStartTime(2003,2,29).  However, when they go to get one of these
 * fields (e.g., getStartYear() or getStartMonth()) an exception will be thrown
 * because there are not 29 days in Feburary (unless it's a leap year).  This check
 * is made in the get* rather than set* methods to allow users to finish their settings
 * (e.g., in a ParameterListEditor of a GUI) before checking values.
 *
 * The Units on the Duration field must be set in the constructor, ALTHOUGH WE MAY MAKE THIS
 * ADJUSTABLE LATER. These Units are assumed when using the getDuration() and
 * setDuration(double duration) methods.  If one wishes to get or set the duration with other
 * units, they can use the setDuration(String units, double duration) and
 * getDuration(String units, double duration) methods, but note that the units will
 * not have changed internally as a result. BUILD THESE LATTER TWO METHODS<p>
 * The constraints on the units can be set using the ???????????? ADD METHOD LATER.<p>
 *
 * Finally, one can get an end-time calendar object that corresponds to the start time
 * plus the duration. FINISH THIS
 *
 *
 *
 *
 * @author     Edward Field, based on an earlier version by Sid Hellman and Steven W. Rock.
 * @created    March, 2003
 * @version    1.0
 */
public class TimeSpan {

    /** The name of this class, used for debug statements */
    protected final static String C = "TimeSpan";

    /** Static boolean whether to print out debugging statements */
    protected final static boolean D = false;

    protected GregorianCalendar startTimeCal;

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

    // Misc Strings (e.g., for setting units)
    public final static String YEARS = "Years";
    public final static String MONTHS = "Months";
    public final static String DAYS = "Days";
    public final static String HOURS = "Hours";
    public final static String MINUTES = "Minutes";
    public final static String SECONDS = "Seconds";
    public final static String MILLISECONDS = "Milliseconds";
    public final static String NONE = "None";

    private final static String START_TIME_ERR = "Start-Time Precision Violation: ";

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
     *  Constructor; this should actually take the start-time precision string since it
     *  cannot be set publically (do later or I'll have to change TimeSpan construction
     *  in existing ERFs)
     */
    public TimeSpan(String startTimePrecision, String durationUnits) {
      initParams();
      setStartTimePrecision(startTimePrecision);
      setDurationUnits(durationUnits);
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
     * Sets the Start-Time Precision.  Options are "Years", "Months", "Days",
     * "Hours", "Minutes", "Seconds" and "Milliseconds".  "None" can also be
     * set if the start-time is not needed (e.g., for Poissonian models).
     * @param startTimePrecision
     */
    private void setStartTimePrecision(String startTimePrecision) {
      startTimePrecisionParam.setValue(startTimePrecision);
    }

    /**
     * This returns the start-time precision's integer equivalent (0 for NONE,
     * 1 for YEARS, 2 for MONTHS, 3 for DAYS, 4 for HOURS, 5 for MINUTES, 6 for
     * SECONDS, and 7 for MILLISECONDS).
     * @return precision integer
     */
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


    /**
     * This returns the Start-Time Precision String
     * @return
     */
    public String getStartTimePrecision() {
      return (String) startTimePrecisionParam.getValue();
    }


    /**
     * @return Start-time year
     * @throws RuntimeException if year is not within the start-time precision.
     */
    public int getStartTimeYear() throws RuntimeException {
      if(getStartTimePrecInt() >= 1) {
        // check the start-time parameter settings (e.g., to make sure day exists in chosen month)
        checkStartTimeValues();
        return ((Integer)startYearParam.getValue()).intValue();
      }
      else {
        String str = "cannot use the getStartTimeYear() method because start-time precision is \"";
        String prec = getStartTimePrecision();
        throw new RuntimeException(START_TIME_ERR+str+prec+"\"");
      }
    }


    /**
     * @return Start-time month
     * @throws RuntimeException if month is not within the start-time precision.
     */
    public int getStartTimeMonth() throws RuntimeException {
      if(getStartTimePrecInt() >= 2) {
        // check the start-time parameter settings (e.g., to make sure day exists in chosen month)
        checkStartTimeValues();
        return ((Integer)startMonthParam.getValue()).intValue();
      }
      else {
        String str = "cannot use the getStartTimeMonth() method because start-time precision is \"";
        String prec = getStartTimePrecision();
        throw new RuntimeException(START_TIME_ERR+str+prec+"\"");
      }
    }


    /**
     * @return Start-time day
     * @throws RuntimeException if day is not within the start-time precision.
     */
    public int getStartTimeDay() throws RuntimeException {
      if(getStartTimePrecInt() >= 3) {
        // check the start-time parameter settings (e.g., to make sure day exists in chosen month)
        checkStartTimeValues();
        return ((Integer)startDayParam.getValue()).intValue();
      }
      else {
        String str = "cannot use the getStartTimeDay() method because start-time precision is \"";
        String prec = getStartTimePrecision();
        throw new RuntimeException(START_TIME_ERR+str+prec+"\"");
      }
    }


    /**
     * @return Start-time hour
     * @throws RuntimeException if hour is not within the start-time precision.
     */
    public int getStartTimeHour() throws RuntimeException {
      if(getStartTimePrecInt() >= 4) {
        // check the start-time parameter settings (e.g., to make sure day exists in chosen month)
        checkStartTimeValues();
        return ((Integer)startHourParam.getValue()).intValue();
      }
      else {
        String str = "cannot use the getStartTimeHour() method because start-time precision is \"";
        String prec = getStartTimePrecision();
        throw new RuntimeException(START_TIME_ERR+str+prec+"\"");
      }
    }

    /**
     * @return Start-time minute
     * @throws RuntimeException if minute is not within the start-time precision.
     */
    public int getStartTimeMinute() throws RuntimeException {
      if(getStartTimePrecInt() >= 5) {
        // check the start-time parameter settings (e.g., to make sure day exists in chosen month)
        checkStartTimeValues();
        return ((Integer)startMinuteParam.getValue()).intValue();
      }
      else {
        String str = "cannot use the getStartTimeMinute() method because start-time precision is \"";
        String prec = getStartTimePrecision();
        throw new RuntimeException(START_TIME_ERR+str+prec+"\"");
      }
    }

    /**
     * @return Start-time second
     * @throws RuntimeException if second is not within the start-time precision.
     */
    public int getStartTimeSecond() throws RuntimeException {
      if(getStartTimePrecInt() >= 6) {
        // check the start-time parameter settings (e.g., to make sure day exists in chosen month)
        checkStartTimeValues();
        return ((Integer)startSecondParam.getValue()).intValue();
      }
      else {
        String str = "cannot use the getStartTimeSecond() method because start-time precision is \"";
        String prec = getStartTimePrecision();
        throw new RuntimeException(START_TIME_ERR+str+prec+"\"");
      }
    }

    /**
     * @return Start-time millisecond
     * @throws RuntimeException if millisecond is not within the start-time precision.
     */
    public int getStartTimeMillisecond() throws RuntimeException {
      if(getStartTimePrecInt() >= 7) {
        // check the start-time parameter settings (e.g., to make sure day exists in chosen month)
        checkStartTimeValues();
        return ((Integer)startMillisecondParam.getValue()).intValue();
      }
      else {
        String str = "cannot use the getStartTimeMillisecond() method because start-time precision is \"";
        String prec = getStartTimePrecision();
        throw new RuntimeException(START_TIME_ERR+str+prec+"\"");
      }
    }

    /**
     * Sets the units for the duration; presently private until we know how to
     * handle changes after instantiation.
     * @param durationUnits
     */
    private void setDurationUnits(String durationUnits) {
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
     * Sets the the duration; assumes the units are as they've been set
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
     * Sets the start time if start-time precision = "Years".
     * @param year
     * @throws RuntimeException if start-time precision is not "Years"
     */
    public void setStartTime(int year) throws RuntimeException {

      if(getStartTimePrecInt() == 1)
        startYearParam.setValue(new Integer(year));
      else {
        String prec = (String) startTimePrecisionParam.getValue();
        String method = "setStartTime(int year)";
        throw new RuntimeException(START_TIME_ERR+
                                   "can't use the "+method+" method because start-time precision is \""+
                                   prec+"\"");
      }
    }

    /**
     * Sets the start time if start-time precision = "Months".
     * @params year, month
     * @throws RuntimeException if start-time precision is not "Months"
     */
    public void setStartTime(int year, int month) throws RuntimeException {

      if(getStartTimePrecInt() == 2) {
        startYearParam.setValue(new Integer(year));
        startMonthParam.setValue(new Integer(month));
      }
      else {
        String prec = (String) startTimePrecisionParam.getValue();
        String method = "setStartTime(int year, int month)";
        throw new RuntimeException(START_TIME_ERR+
                                   "can't use the "+method+" method because start-time precision is \""+
                                   prec+"\"");
      }
    }

    /**
     * Sets the start time if start-time precision = "Days".
     * @params year, month, day
     * @throws RuntimeException if start-time precision is not "Days"
     */
    public void setStartTime(int year, int month, int day) throws RuntimeException {

      if(getStartTimePrecInt() == 3) {
        startYearParam.setValue(new Integer(year));
        startMonthParam.setValue(new Integer(month));
        startDayParam.setValue(new Integer(day));
      }
      else {
        String prec = (String) startTimePrecisionParam.getValue();
        String method = "setStartTime(int year, int month, int day)";
        throw new RuntimeException(START_TIME_ERR+
                                   "can't use the "+method+" method because start-time precision is \""+
                                   prec+"\"");
      }
    }

    /**
     * Sets the start time if start-time precision = "Hours".
     * @params year, month, day, hour
     * @throws RuntimeException if start-time precision is not "Hours"
     */
    public void setStartTime(int year, int month, int day, int hour) throws RuntimeException {

      if(getStartTimePrecInt() == 4) {
        startYearParam.setValue(new Integer(year));
        startMonthParam.setValue(new Integer(month));
        startDayParam.setValue(new Integer(day));
        startHourParam.setValue(new Integer(hour));
      }
      else {
        String prec = (String) startTimePrecisionParam.getValue();
        String method = "setStartTime(int year, int month, int day, int hour)";
        throw new RuntimeException(START_TIME_ERR+
                                   "can't use the "+method+" method because start-time precision is \""+
                                   prec+"\"");
      }
    }


    /**
     * Sets the start time if start-time precision = "Minutes".
     * @params year, month, day, hour, minute
     * @throws RuntimeException if start-time precision is not "Minutes"
     */
    public void setStartTime(int year, int month, int day, int hour, int minute) throws RuntimeException {

      if(getStartTimePrecInt() == 5) {
        startYearParam.setValue(new Integer(year));
        startMonthParam.setValue(new Integer(month));
        startDayParam.setValue(new Integer(day));
        startHourParam.setValue(new Integer(hour));
        startMinuteParam.setValue(new Integer(minute));
      }
      else {
        String prec = (String) startTimePrecisionParam.getValue();
        String method = "setStartTime(int year, int month, int day, int hour, int minute)";
        throw new RuntimeException(START_TIME_ERR+
                                   "can't use the "+method+" method because start-time precision is \""+
                                   prec+"\"");
      }
    }

    /**
     * Sets the start time if start-time precision = "Seconds".
     * @params year, month, day, hour, minute, second
     * @throws RuntimeException if start-time precision is not "Seconds"
     */
    public void setStartTime(int year, int month, int day, int hour, int minute, int second) throws RuntimeException {

      if(getStartTimePrecInt() == 6) {
        startYearParam.setValue(new Integer(year));
        startMonthParam.setValue(new Integer(month));
        startDayParam.setValue(new Integer(day));
        startHourParam.setValue(new Integer(hour));
        startMinuteParam.setValue(new Integer(minute));
        startSecondParam.setValue(new Integer(second));
      }
      else {
        String prec = (String) startTimePrecisionParam.getValue();
        String method = "setStartTime(int year, int month, int day, int hour, int minute, int second)";
        throw new RuntimeException(START_TIME_ERR+
                                   "can't use the "+method+" method because start-time precision is \""+
                                   prec+"\"");
      }
    }

    /**
     * Sets the start time if start-time precision = "Milliseconds".
     * @params year, month, day, hour, minute, second, millisecond
     * @throws RuntimeException if start-time precision is not "Milliseconds"
     */
    public void setStartTime(int year, int month, int day, int hour,
                             int minute, int second, int millisecond)
                             throws RuntimeException {

      if(getStartTimePrecInt() == 7) {
        startYearParam.setValue(new Integer(year));
        startMonthParam.setValue(new Integer(month));
        startDayParam.setValue(new Integer(day));
        startHourParam.setValue(new Integer(hour));
        startMinuteParam.setValue(new Integer(minute));
        startSecondParam.setValue(new Integer(second));
        startMillisecondParam.setValue(new Integer(millisecond));
      }
      else {
        String prec = (String) startTimePrecisionParam.getValue();
        String method = "setStartTime(int year, int month, int day, int hour, int minute, int second, int millisecond)";
        throw new RuntimeException(START_TIME_ERR+
                                   "can't use the "+method+" method because start-time precision is \""+
                                   prec+"\"");
      }
    }

    /**
     * This checks whether the start-time parameter values correspond to an actaul
     * date (e.g., can't have day=29 if month=2, unless it's a leap year).
     * Currently this is done by simply rebuilding the startTimeCalendar
     * (which will throw and exception if there is a problem), but a
     * more efficient approach could be implemented later.
     * @return
     */
    private void checkStartTimeValues() {
        // for efficiency there should be an if statement here to check whether any parameters have changed
        buildStartTimeCalendar();
    }

    /**
     * This sets the Start-Time Calendar, making any fields greater than the
     * Start-Time Precision equal to defaults (usually the lowest allowed value).
     * This throws an exception if the Day is incompatable with the chosen Month
     * @throws Exception
     */
    private void buildStartTimeCalendar() throws RuntimeException {

      int year, month, day, hour, minute, second, millisecond;

      // get the precision integer
      int precisionInt = getStartTimePrecInt();

      // get a primitave for each field according to the precision

      // set the year
      if(precisionInt>0)
        year = ((Integer) startYearParam.getValue()).intValue();
      else
        year = this.START_YEAR_DEFAULT.intValue();

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
        throw new RuntimeException("Calendar Error: Invalid Day for the chosen Month");
      }
      startTimeCal.set(Calendar.HOUR_OF_DAY,hour);
      startTimeCal.set(Calendar.MINUTE,minute);
      startTimeCal.set(Calendar.SECOND,second);
      startTimeCal.set(Calendar.MILLISECOND,millisecond);
    }



    /**
     * This sets the start-time fields (year, month, day, hour, minute, second,
     * and millisecond) from the inpute GregorianCalendar.  Fields above
     * the start-time precision are igored.  For example, if the start-
     * time precision equals "Hour", then the year, month, day, and hour are
     * set, but the minute, second, and millisecond fields are not.  If start-
     * time precision equals "None", then none of the fields are filled in.
     * @param cal
     */
    public void setStartTimeCalendar( GregorianCalendar cal ) {
      int year = cal.get(Calendar.YEAR);
      int month = cal.get(Calendar.MONTH) + 1; // our indexing starts from 1
      int day = cal.get(Calendar.DATE);
      int hour = cal.get(Calendar.HOUR_OF_DAY);
      int minute = cal.get(Calendar.MINUTE);
      int second = cal.get(Calendar.SECOND);
      int millisecond = cal.get(Calendar.MILLISECOND);
      if(getStartTimePrecInt() == 7)
        setStartTime(year,month,day,hour,minute,second,millisecond);
      else if(getStartTimePrecInt() == 6)
        setStartTime(year,month,day,hour,minute,second);
      else if(getStartTimePrecInt() == 5)
        setStartTime(year,month,day,hour,minute);
      else if(getStartTimePrecInt() == 4)
        setStartTime(year,month,day,hour);
      else if(getStartTimePrecInt() == 3)
        setStartTime(year,month,day);
      else if(getStartTimePrecInt() == 2)
        setStartTime(year,month);
      else if(getStartTimePrecInt() == 1)
        setStartTime(year);
      else {} // do nothing if getStartTimePrecInt() == 0

    }


    /**
     *  NEEDS TO BE FINISHED & TESTED
     *  Not that fields above start-time precision are given their default values
     */
    public GregorianCalendar getEndTime() {
      if(getStartTimePrecInt() > 0) {
        // build the startTime Calendar
        buildStartTimeCalendar();
        // compute duration in mSec from the duration parameter FINISH
        long duration_MSec = 1000; //?????????????????????????????????
        long endTime_mSec =  startTimeCal.getTime().getTime() + duration_MSec;
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime( new Date( endTime_mSec ) );
        return cal;
      }
      else {
        String str = "Can't use getEndTime() method because start-time precision = \"";
        String prec = (String) startTimePrecisionParam.getValue();
        throw new RuntimeException(START_TIME_ERR+str+prec+"\"");
      }
    }

    /**
     * This returns a GregorianCalendar representation of the start-time fields.
     * Those fields above the start-time precision are set to their defaults
     * (generally the lowest possible value).  For example, if start-time precision
     * equals "Day", then the HOUR_OF_DAY, MINUTE, SECOND, and MILLISECOND fields
     * of the returned GregorianCalendar are all set to 0.
     * @return
     * @throws Exception
     */
    public GregorianCalendar getStartTimeCalendar(  ) throws RuntimeException{
      buildStartTimeCalendar();
      return startTimeCal;
    }


    // this is temporary for testing purposes
    public static void main(String[] args) {
      TimeSpan tSpan = new TimeSpan(YEARS,YEARS);
      try {
        tSpan.setStartTime(1964);
//        tSpan.setStartTime(1964,11,18,19,20,21);
      }catch (Exception e) {
        System.out.println(e.getMessage());
      }
      try {
        int i = tSpan.getStartTimeMonth();
      }catch (Exception e) {
        System.out.println(e.getMessage());
      }
/*

      GregorianCalendar cal = tSpan.getStartTimeCalendar();

      int int1, int2;

      int1 = tSpan.getStartTimeCalendar().get(Calendar.MONTH);
      int2 = tSpan.getStartTimeMonth();
      System.out.println("getStartTimeCalendar().get(Calendar.MONTH)) = "+int1);
      System.out.println("getStartTimeMonth() = "+int2);

/*
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

