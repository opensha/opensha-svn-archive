/* ==================================================
 * JCommon : a general purpose class library for Java
 * ==================================================
 *
 * Project Info:  http://www.object-refinery.com/jcommon/index.html
 * Project Lead:  David Gilbert (david.gilbert@object-refinery.com);
 *
 * (C) Copyright 2000-2002, by Simba Management Limited and Contributors.
 *
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation;
 * either version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * library; if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307, USA.
 *
 * ----------
 * Month.java
 * ----------
 * (C) Copyright 2001, 2002, by Simba Management Limited.
 *
 * Original Author:  David Gilbert (for Simba Management Limited);
 * Contributor(s):   -;
 *
 * $Id$
 *
 * Changes
 * -------
 * 11-Oct-2001 : Version 1 (DG);
 * 14-Nov-2001 : Added method to get year as primitive (DG);
 *               Override for toString() method (DG);
 * 18-Dec-2001 : Changed order of parameters in constructor (DG);
 * 19-Dec-2001 : Added a new constructor as suggested by Paul English (DG);
 * 29-Jan-2002 : Worked on the parseMonth(...) method (DG);
 * 14-Feb-2002 : Fixed bugs in the Month constructors (DG);
 * 26-Feb-2002 : Changed getStart(), getMiddle() and getEnd() methods to evaluate with reference
 *               to a particular time zone (DG);
 * 19-Mar-2002 : Changed API for TimePeriod classes (DG);
 *
 */

package com.jrefinery.data;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import com.jrefinery.date.SerialDate;

/**
 * Represents a single month.
 * <P>
 * This class is immutable, which is a requirement for all TimePeriod subclasses.
 */
public class Month extends TimePeriod {

    /** The month (1-12). */
    protected int month;

    /** The year in which the month falls. */
    protected Year year;

    /**
     * Constructs a new Month, based on the current system time.
     */
    public Month() {

        this(new Date());

    }

    /**
     * Constructs a new month instance.
     * @param month The month (in the range 1 to 12).
     * @param year The year.
     */
    public Month(int month, int year) {

        this(month, new Year(year));

    }

    /**
     * Constructs a new month instance.
     * @param month The month (in the range 1 to 12).
     * @param year The year.
     */
    public Month(int month, Year year) {

        if ((month<1) && (month>12)) {
            throw new IllegalArgumentException("Month(...): month outside valid range.");
        }

        this.month = month;
        this.year = year;

    }

    /**
     * Constructs a Month, based on a date/time and the default time zone.
     *
     * @param time The date/time.
     */
    public Month(Date time) {

        this(time, TimePeriod.DEFAULT_TIME_ZONE);

    }

    /**
     * Constructs a Month, based on a date/time and a time zone.
     *
     * @param time The date/time.
     * @param zone The time zone.
     */
    public Month(Date time, TimeZone zone) {

        Calendar calendar = Calendar.getInstance(zone);
        calendar.setTime(time);
        this.month = calendar.get(Calendar.MONTH)+1;
        this.year = new Year(calendar.get(Calendar.YEAR));

    }

    /**
     * Returns the year in which the month falls.
     * @return The year in which the month falls (as a Year object).
     */
    public Year getYear() {
        return this.year;
    }

    /**
     * Returns the year in which the month falls.
     * @return The year in which the monht falls (as an int).
     */
    public int getYearValue() {
        return this.year.getYear();
    }

    /**
     * Returns the month.
     *
     * Note that 1=JAN, 2=FEB, ...
     */
    public int getMonth() {
        return this.month;
    }

    /**
     * Returns the month preceding this one.
     */
    public TimePeriod previous() {

        Month result;
        if (this.month!=SerialDate.JANUARY) {
            result = new Month(month-1, year);
        }
        else {
            Year prevYear = (Year)year.previous();
            if (prevYear!=null) result = new Month(SerialDate.DECEMBER, prevYear);
            else result = null;
        }
        return result;

    }

    /**
     *  Returns the month following this one.
     */
    public TimePeriod next() {
        Month result;
        if (month != SerialDate.DECEMBER) {
            result = new Month(month+1, year);
        }
        else {
            Year nextYear = (Year)year.next();
            if (nextYear!=null) result = new Month(SerialDate.JANUARY, nextYear);
            else result = null;
        }
        return result;
    }

    /**
     * Returns a string representing the month (e.g. "January 2002").
     * <P>
     * To do: look at internationalisation.
     * @return A string representing the month.
     */
    public String toString() {
        return SerialDate.monthCodeToString(month)+" "+year;
    }

    /**
     * Tests the equality of this Month object to an arbitrary object.  Returns true if the
     * target is a Month instance representing the same month as this object.  In all other
     * cases, returns false.
     * @param object The object.
     */
    public boolean equals(Object obj) {
        if (obj!=null) {
            if (obj instanceof Month) {
                Month target = (Month)obj;
                return ((month==target.getMonth()) && (year.equals(target.getYear())));
            }
            else return false;
        }
        else return false;
    }

    /**
     * Returns an integer indicating the order of this Month object relative to the specified
     * object: negative == before, zero == same, positive == after.
     *
     */
    public int compareTo(Object o1) {

        int result;

        // CASE 1 : Comparing to another Month object
        // --------------------------------------------
        if (o1 instanceof Month) {
            Month m = (Month)o1;
            result = this.year.getYear() - m.getYear().getYear();
            if (result == 0) result = this.month - m.getMonth();
        }

        // CASE 2 : Comparing to another TimePeriod object
        // -----------------------------------------------
        else if (o1 instanceof TimePeriod) {
            // more difficult case - evaluate later...
            result = 0;
        }

        // CASE 3 : Comparing to a non-TimePeriod object
        // ---------------------------------------------
        else result = 1;  // consider time periods to be ordered after general objects

        return result;

    }

    /**
     * Returns the first millisecond of the month, evaluated using the supplied calendar (which
     * determines the time zone).
     *
     * @param calendar The calendar.
     * @return The first millisecond of the month.
     */
    public long getStart(Calendar calendar) {

        Day first = new Day(1, this.month, year.getYear());
        return first.getStart(calendar);

    }

    /**
     * Returns the last millisecond of the month, evaluated using the supplied calendar (which
     * determines the time zone).
     *
     * @param calendar The calendar.
     * @return The last millisecond of the month.
     */
    public long getEnd(Calendar calendar) {

        int eom = SerialDate.lastDayOfMonth(this.month, year.getYear());
        Day last = new Day(eom, this.month, year.getYear());
        return last.getEnd(calendar);

    }

    /**
     * Parses the string argument as a month.
     * <P>
     * This method is required to accept the format "YYYY-MM".  It will also accept "MM-YYYY".
     * Anything else, at the moment, is a bonus.
     */
    public static Month parseMonth(String s) throws TimePeriodFormatException {

        Month result = null;
        if (s!=null) {

            // trim whitespace from either end of the string
            s = s.trim();

            int i = Month.findSeparator(s);
            if (i!=-1) {
                String s1 = s.substring(0, i).trim();
                String s2 = s.substring(i+1, s.length()).trim();

                Year year = Month.evaluateAsYear(s1);
                int month;
                if (year!=null) {
                    month = SerialDate.stringToMonthCode(s2);
                    if (month==-1) {
                        throw new TimePeriodFormatException("Month.parseMonth(String): can't "
                                                            +"evaluate the month.");
                    }
                    result = new Month(month, year);
                }
                else {
                    year = Month.evaluateAsYear(s2);
                    if (year!=null) {
                        month = SerialDate.stringToMonthCode(s1);
                        if (month==-1) {
                            throw new TimePeriodFormatException("Month.parseMonth(String): can't "
                                                            +"evaluate the month.");
                        }
                        result = new Month(month, year);
                    }
                    else {
                        throw new TimePeriodFormatException("Month.parseMonth(String): can't "
                                                        +"evaluate the year.");
                    }
                }

            }
            else {
                throw new TimePeriodFormatException("Month.parseMonth(String): could not find "
                                                    +"separator.");
            }

        }
        return result;

    }

    /**
     * Finds the first occurrence of ' ', '-', ',' or '.'
     */
    private static int findSeparator(String s) {

        int result = -1;
        result = s.indexOf('-');
        if (result==-1) {
            result = s.indexOf(',');
        }
        if (result==-1) {
            result = s.indexOf(' ');
        }
        if (result==-1) {
            result = s.indexOf('.');
        }
        return result;
    }

    /**
     * Creates a year from a string, or returns null (format exceptions suppressed).
     */
    private static Year evaluateAsYear(String s) {

        Year result = null;
        try {
            result = Year.parseYear(s);
        }
        catch (TimePeriodFormatException e) {
            // suppress
        }
        return result;

    }

}
