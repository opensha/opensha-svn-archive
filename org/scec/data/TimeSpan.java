package org.scec.data;

import java.util.*;
import org.scec.exceptions.InvalidRangeException;

/**
 *  <b>Title:</b> TimeSpan<br>
 *  <b>Description:</b> Represents a start time and a duration, from which you
 *  can calculate the end time of an event<br>
 *  <b>Copyright:</b> Copyright (c) 2001<br>
 *  <b>Company:</b> <br>
 *
 *
 * @author     Sid Hellman
 * @created    February 20, 2002
 * @version    1.0
 */
public class TimeSpan extends GregorianCalendar {

    /**
     *  The name of this class, used for debug statements
     */
    protected final static String C = "TimeSpan";
    /**
     *  Static boolean whether to print out debugging statements
     */
    protected final static boolean D = false;

    /**
     *  Elapsed time of the event since it's start time, in seconds.
     */
    protected double timeInterval;

    /**
     * End time of the event in milliseconds.
     */
    protected long endTime;

    /**
     *  No-Argument constructor. Defaults to right now as the start time, and 1
     *  second as the duration.
     */
    public TimeSpan() {
        super();
        timeInterval = 1;
        endTime =  this.getTimeInMillis() + (long)(timeInterval * 1000)  ;
    }


    /**
     *  create a TimeSpan with a time length (num seconds). Defaults to right
     *  now as the start time.
     *
     * @param  interval  Interval of the event
     */
    public TimeSpan( double interval ) {
        super();
        this.timeInterval = interval;
        endTime =  this.getTimeInMillis() + (long)(timeInterval * 1000)  ;
    }


    /**
     *  create a TimeSpan with a date and a time length (num seconds).
     *
     * @param  cal       Start time
     * @param  interval  Interval of the event
     */
    public TimeSpan( GregorianCalendar cal, double interval ) {

        super(TimeZone.getDefault(), Locale.getDefault());
        this.set(ERA, AD);
        this.set(YEAR, cal.get(Calendar.YEAR));
        this.set(MONTH, cal.get(Calendar.MONTH));
        this.set(DATE, cal.get(Calendar.DATE));
        this.set(HOUR_OF_DAY, cal.get(Calendar.HOUR_OF_DAY));
        this.set(MINUTE, cal.get(Calendar.MINUTE));
        this.set(SECOND, cal.get(Calendar.SECOND));
        this.set(MILLISECOND, cal.get(Calendar.MILLISECOND));

        this.timeInterval = interval;
        endTime =  this.getTimeInMillis() + (long)(timeInterval * 1000)  ;

    }


    /**
     *  Sets the elapsed time of this event in seconds.
     *
     * @param  timeInterval  The elapsed time of this event
     */
    public void setTimeInterval( double timeInterval ) {
        this.timeInterval = timeInterval;
        endTime =  this.getTimeInMillis() + (long)(timeInterval * 1000)  ;
    }

    /**
     *  Sets the elapsed time of this event in seconds.
     *
     * @param  timeInterval  The elapsed time of this event
     */
    public void setStartTime( GregorianCalendar cal ) {

        this.set(YEAR, cal.get(Calendar.YEAR));
        this.set(MONTH, cal.get(Calendar.MONTH));
        this.set(DATE, cal.get(Calendar.DATE));
        this.set(HOUR_OF_DAY, cal.get(Calendar.HOUR_OF_DAY));
        this.set(MINUTE, cal.get(Calendar.MINUTE));
        this.set(SECOND, cal.get(Calendar.SECOND));
        this.set(MILLISECOND, cal.get(Calendar.MILLISECOND));

        endTime =  this.getTimeInMillis() + (long)(timeInterval * 1000)  ;
    }

    /**
     *  Sets the elapsed time of this event in seconds.
     *
     * @param  timeInterval  The elapsed time of this event
     */
    public void setEndTime( GregorianCalendar cal ) throws InvalidRangeException{

        String S = C + ": setEndTime():";

        long start = this.getTime().getTime();
        long end = cal.getTime().getTime();

        if( end <= start ) throw new InvalidRangeException(S + "End time cannot be before or equal to the start time");

        endTime = end;
        this.timeInterval =  Math.round( (double) ( ( end - start ) / 1000 ) );
    }

    /**
     *  Returns the elapsed time of this event in seconds.
     *
     * @return    timeInterval
     */
    public double getTimeInterval() {
        return timeInterval;
    }


    /**
     *  create a TimeSpan with a date and a time length (num seconds).
     *
     * @param  cal       Start time
     * @param  interval  Interval of the event
     */
    public GregorianCalendar getEndTime(  ) {
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime( new Date( endTime ) );
        return cal;
    }

    public GregorianCalendar getStartTime(  ) {
        return (GregorianCalendar)this;
    }

}

