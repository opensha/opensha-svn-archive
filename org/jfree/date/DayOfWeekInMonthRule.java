/* ===================================================
 * JCommon : a free general purpose Java class library
 * ===================================================
 *
 * Project Info:  http://www.jfree.org/jcommon/index.html
 * Project Lead:  David Gilbert (david.gilbert@object-refinery.com);
 *
 * (C) Copyright 2000-2003, by Object Refinery Limited and Contributors.
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
 * -------------------------
 * DayOfWeekInMonthRule.java
 * -------------------------
 * (C) Copyright 2000-2003, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * $Id$
 *
 * Changes (from 26-Oct-2001)
 * --------------------------
 * 26-Oct-2001 : Changed package to com.jrefinery.date.*;
 * 03-Oct-2002 : Fixed errors reported by Checkstyle (DG);
 *
 */

package org.jfree.date;

/**
 * An annual date rule that specifies the nth day of the week in a given month
 * (for example, the third Wednesday in June, or the last Friday in November).
 *
 * @author David Gilbert
 */
public class DayOfWeekInMonthRule extends AnnualDateRule implements Cloneable {

    /** FIRST, SECOND, THIRD, FOURTH or LAST. */
    private int count;

    /** The day of the week (SerialDate.MONDAY, SerialDate.TUESDAY...). */
    private int dayOfWeek;

    /** The month (1 to 12, or SerialDate.JANUARY, SerialDate.FEBRUARY...). */
    private int month;

    /**
     * Default constructor: builds a rule for the first Monday in January by default.
     */
    public DayOfWeekInMonthRule() {
        this(1, SerialDate.MONDAY, SerialDate.JANUARY);
    }

    /**
     * Standard constructor: builds a rule with the specified attributes.
     *
     * @param count  one of: FIRST, SECOND, THIRD, FOURTH or LAST.
     * @param dayOfWeek  the day-of-the-week (SerialDate.MONDAY, SerialDate.TUESDAY, etc.).
     * @param month  the month (SerialDate.JANUARY, SerialDate.FEBRUARY, etc.).
     */
    public DayOfWeekInMonthRule(int count, int dayOfWeek, int month) {
        this.count = count;
        this.dayOfWeek = dayOfWeek;
        this.month = month;
    }

    /**
     * Returns the 'count' for this rule (one of FIRST, SECOND, THIRD, FOURTH and LAST).
     *
     * @return the 'count'.
     */
    public int getCount() {
        return count;
    }

    /**
     * Sets the 'count' for this rule (one of FIRST, SECOND, THIRD, FOURTH and LAST).
     *
     * @param count the 'count'.
     */
    public void setCount(int count) {
        this.count = count;
    }

    /**
     * Returns the day-of-the-week for this rule (SerialDate.MONDAY, SerialDate.TUESDAY, etc.).
     *
     * @return the day-of-the-week.
     */
    public int getDayOfWeek() {
        return dayOfWeek;
    }

    /**
     * Sets the day-of-the-week for this rule.
     *
     * @param dayOfWeek  the day-of-the-week.
     */
    public void setDayOfWeek(int dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    /**
     * Returns the month for this rule.
     *
     * @return the month.
     */
    public int getMonth() {
        return month;
    }

    /**
     * Sets the month for this rule.
     *
     * @param month  the month (SerialDate.JANUARY, SerialDate.FEBRUARY, etc.).
     */
    public void setMonth(int month) {
        this.month = month;
    }

    /**
     * Return the date for this rule, given the year.
     *
     * @param year  the year.
     *
     * @return the date generated by the rule for the given year.
     */
    public SerialDate getDate(int year) {
        SerialDate result;
        if (count != SerialDate.LAST_WEEK_IN_MONTH) {
            // start at the beginning of the month
            result = SerialDate.createInstance(1, month, year);
            while (result.getDayOfWeek() != dayOfWeek) {
                result = SerialDate.addDays(1, result);
            }
            result = SerialDate.addDays(7 * (count - 1), result);

        }
        else {
            // start at the end of the month and work backwards...
            result = SerialDate.createInstance(1, month, year);
            result = result.getEndOfCurrentMonth(result);
            while (result.getDayOfWeek() != dayOfWeek) {
                result = SerialDate.addDays(-1, result);
            }

        }
        return result;
    }

}
