/* ======================================
 * JFreeChart : a free Java chart library
 * ======================================
 *
 * Project Info:  http://www.jfree.org/jfreechart/index.html
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
 * -----------------
 * DateTickUnit.java
 * -----------------
 * (C) Copyright 2000-2003, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * $Id$
 *
 * Changes (from 8-Nov-2002)
 * --------------------------
 * 08-Nov-2002 : Moved to new package com.jrefinery.chart.axis (DG);
 * 27-Nov-2002 : Added IllegalArgumentException to getMillisecondCount(...) method (DG);
 * 26-Mar-2003 : Implemented Serializable (DG);
 *
 */

package org.jfree.chart.axis;

import java.io.Serializable;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * A tick unit for use by subclasses of {@link DateAxis}.  
 * <p>
 * Instances of this class are immutable.
 *
 * @author David Gilbert
 */
public class DateTickUnit extends TickUnit implements Serializable {

    /** A constant for years. */
    public static final int YEAR = 0;

    /** A constant for months. */
    public static final int MONTH = 1;

    /** A constant for days. */
    public static final int DAY = 2;

    /** A constant for hours. */
    public static final int HOUR = 3;

    /** A constant for minutes. */
    public static final int MINUTE = 4;

    /** A constant for seconds. */
    public static final int SECOND = 5;

    /** A constant for milliseconds. */
    public static final int MILLISECOND = 6;

    /** The unit. */
    private int unit;

    /** The unit count. */
    private int count;

    /** The date formatter. */
    private DateFormat formatter;

    /**
     * Creates a new date tick unit.  The dates will be formatted using a SHORT format for the
     * default locale.
     *
     * @param unit  the unit.
     * @param count  the unit count.
     */
    public DateTickUnit(int unit, int count) {
        this(unit, count, DateFormat.getDateInstance(DateFormat.SHORT));
    }

    /**
     * Creates a new date tick unit.
     * <P>
     * You can specify the units using one of the constants YEAR, MONTH, DAY, HOUR, MINUTE,
     * SECOND or MILLISECOND.  In addition, you can specify a unit count, and a date format.
     *
     * @param unit  the unit.
     * @param count  the unit count.
     * @param formatter  the date formatter.
     */
    public DateTickUnit(int unit, int count, DateFormat formatter) {

        super(DateTickUnit.getMillisecondCount(unit, count));
        this.unit = unit;
        this.count = count;
        this.formatter = formatter;

    }

    /**
     * Returns the date unit.  This will be one of the constants <code>YEAR</code>,
     * <code>MONTH</code>, <code>DAY</code>, <code>HOUR</code>, <code>MINUTE</code>,
     * <code>SECOND</code> or <code>MILLISECOND</code>, defined by this class.  Note that these
     * constants do NOT correspond to those defined in Java's <code>Calendar</code> class.
     *
     * @return the date unit.
     */
    public int getUnit() {
        return this.unit;
    }

    /**
     * Returns the unit count.
     *
     * @return the unit count.
     */
    public int getCount() {
        return this.count;
    }

    /**
     * Formats a value.
     *
     * @param milliseconds  date in milliseconds since 01-01-1970.
     *
     * @return the formatted date.
     */
    public String valueToString(double milliseconds) {
        return formatter.format(new Date((long) milliseconds));
    }

    /**
     * Formats a date using the tick unit's formatter.
     *
     * @param date  the date.
     *
     * @return the formatted date.
     */
    public String dateToString(Date date) {
        return formatter.format(date);
    }

    /**
     * Calculates a new date by adding this unit to the base date.
     *
     * @param base  the base date.
     *
     * @return a new date one unit after the base date.
     */
    public Date addToDate(Date base) {

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(base);
        calendar.add(getCalendarField(this.unit), this.count);
        return calendar.getTime();

    }

    /**
     * Returns a field code that can be used with the <code>Calendar</code> class.
     *
     * @return the field code.
     */
    public int getCalendarField() {
        return getCalendarField(this.unit);
    }

    /**
     * Returns a field code (that can be used with the Calendar class) for a given 'unit' code.
     * The 'unit' is one of:  YEAR, MONTH, DAY, HOUR, MINUTE, SECOND and MILLISECOND.
     *
     * @param unit  the unit.
     *
     * @return the field code.
     */
    private int getCalendarField(int unit) {

        switch (unit) {
            case (YEAR) : return Calendar.YEAR;
            case (MONTH) : return Calendar.MONTH;
            case (DAY) : return Calendar.DATE;
            case (HOUR) : return Calendar.HOUR_OF_DAY;
            case (MINUTE) : return Calendar.MINUTE;
            case (SECOND) : return Calendar.SECOND;
            case (MILLISECOND) : return Calendar.MILLISECOND;
            default: return Calendar.MILLISECOND;
        }

    }

    /**
     * Returns the (approximate) number of milliseconds for the given unit and unit count.
     * <P>
     * This value is an approximation some of the time (e.g. months are assumed to have 31 days)
     * but this shouldn't matter.
     *
     * @param unit  the unit.
     * @param count  the unit count.
     *
     * @return the number of milliseconds.
     */
    private static long getMillisecondCount(int unit, int count) {

        switch (unit) {
            case (YEAR) : return (365L * 24L * 60L * 60L * 1000L) * count;
            case (MONTH) : return (31L * 24L * 60L * 60L * 1000L) * count;
            case (DAY) : return (24L * 60L * 60L * 1000L) * count;
            case (HOUR) : return (60L * 60L * 1000L) * count;
            case (MINUTE) : return (60L * 1000L) * count;
            case (SECOND) : return 1000L * count;
            case (MILLISECOND) : return (long) count;
            default: throw new IllegalArgumentException("DateTickUnit.getMillisecondCount(...) : "
                              + "unit must be one of the constants YEAR, MONTH, DAY, HOUR, "
                              + "MINUTE, SECOND or MILLISECOND defined in the DateTickUnit class. "
                              + "Do *not* use the constants defined in java.util.Calendar.");
        }

    }

    /**
     * Tests this unit for equality with another object.
     *
     * @param obj  the object.
     *
     * @return <code>true</code> or <code>false</code>.
     */
    public boolean equals(Object obj) {

        if (obj == null) {
            return false;
        }

        if (obj == this) {
            return true;
        }

        if (obj instanceof DateTickUnit) {

            DateTickUnit dtu = (DateTickUnit) obj;

            if (super.equals(obj)) {
                boolean b0 = (this.unit == dtu.unit) && (this.count == dtu.count);
                boolean b1 = this.formatter.equals(dtu.formatter);
                return b0 && b1;
            }
            else {
                return false;
            }

        }

        return false;
    }

}
