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
 * ----------------
 * Millisecond.java
 * ----------------
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
 * 19-Dec-2001 : Added new constructors as suggested by Paul English (DG);
 * 26-Feb-2002 : Added new getStart(...) and getEnd(...) methods (DG);
 * 29-Mar-2002 : Fixed bug in getStart(...), getEnd(...) and compareTo(...) methods (DG);
 *
 */

package com.jrefinery.data;

import java.util.Date;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * Represents a millisecond.
 * <P>
 * This class is immutable, which is a requirement for all TimePeriod subclasses.
 */
public class Millisecond extends TimePeriod {

    public static final int FIRST_MILLISECOND_IN_SECOND = 0;

    public static final int LAST_MILLISECOND_IN_SECOND = 999;

    /** The millisecond. */
    protected int millisecond;

    protected Second second;

    /**
     * Constructs a millisecond based on the current system time.
     */
    public Millisecond() {
        this(new Date());
    }

    /**
     * Constructs a millisecond.
     * @param millisecond The millisecond (same encoding as java.util.Date).
     */
    public Millisecond(int millisecond, Second second) {

        this.millisecond = millisecond;
        this.second = second;

    }

    /**
     * Constructs a millisecond.
     * @param time The time.
     */
    public Millisecond(Date time) {
        this(time, TimePeriod.DEFAULT_TIME_ZONE);
    }

    public Millisecond(Date time, TimeZone zone) {

        this.second = new Second(time, zone);

        Calendar calendar = Calendar.getInstance(zone);
        calendar.setTime(time);
        this.millisecond = calendar.get(Calendar.MILLISECOND);

    }

    public Second getSecond() {
        return this.second;
    }

    /**
     * Returns the millisecond.
     */
    public long getMillisecond() {
        return this.millisecond;
    }

    /**
     * Returns the millisecond preceding this one.
     */
    public TimePeriod previous() {

        TimePeriod result = null;

        if (this.millisecond!=FIRST_MILLISECOND_IN_SECOND) {
            result = new Millisecond(this.millisecond-1, this.second);
        }
        else {
            Second previous = (Second)this.second.previous();
            if (previous!=null) {
                result = new Millisecond(LAST_MILLISECOND_IN_SECOND, previous);
            }
        }
        return result;

    }

    /**
     * Returns the millisecond following this one.
     */
    public TimePeriod next() {

        TimePeriod result = null;

        if (this.millisecond!=LAST_MILLISECOND_IN_SECOND) {
            result = new Millisecond(this.millisecond+1, this.second);
        }
        else {
            Second next = (Second)this.second.next();
            if (next!=null) {
                result = new Millisecond(FIRST_MILLISECOND_IN_SECOND, next);
            }
        }
        return result;

    }

    /**
     * Tests the equality of this object against an arbitrary Object.
     * <P>
     * This method will return true ONLY if the object is a Millisecond object representing the same
     * millisecond as this instance.
     */
    public boolean equals(Object object) {
        if (object instanceof Millisecond) {
            Millisecond m = (Millisecond)object;
            return ((this.millisecond==m.getMillisecond()) && (this.second.equals(m.getSecond())));
        }
        else return false;
    }

    /**
     * Returns an integer indicating the order of this Millisecond object relative to the specified
     * object: negative == before, zero == same, positive == after.
     *
     */
    public int compareTo(Object o1) {

        int result;
        long difference;

        // CASE 1 : Comparing to another Second object
        // -------------------------------------------
        if (o1 instanceof Millisecond) {
            Millisecond ms = (Millisecond)o1;
            difference = this.getStart()-ms.getStart();
            if (difference>0) result = 1;
            else if (difference<0) result = -1;
            else result = 0;
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
     * Returns the first millisecond of the time period.
     */
    public long getStart() {
        return this.second.getStart()+this.millisecond;
    }

    public long getStart(Calendar calendar) {

        return this.second.getStart(calendar)+this.millisecond;

    }

    /**
     * Returns the last millisecond of the time period.
     */
    public long getEnd() {
        return this.second.getStart()+millisecond;
    }

    public long getEnd(Calendar calendar) {

        return this.second.getStart(calendar)+this.millisecond;

    }

    public String toString() {
        return String.valueOf(this.getStart());
    }

    /**
     * Test code - please ignore.
     */
    public static void main(String[] args) {

        String[] ids = TimeZone.getAvailableIDs();

        for (int i=0; i<ids.length; i++) {
            System.out.println(ids[i]);
        }

        TimeZone zone = TimeZone.getTimeZone("Europe/Tallinn");
        Calendar calendar = new GregorianCalendar(zone);
        calendar.clear();
        calendar.set(2002, 2, 21, 16, 55, 59);
        calendar.set(Calendar.MILLISECOND, 123);
        System.out.println("4.55:59.123pm on 21-Mar-2002 in GMT: "+calendar.getTime().getTime());

        Date time = new Date(1014307200000L);
        calendar.setTime(time);
        Hour hour = new Hour(time);
        System.out.println("Hour = "+hour.toString());
        System.out.println("Start = "+hour.getStart(zone));
        System.out.println("End = "+hour.getEnd(zone));
        System.out.println("Offset = "+zone.getRawOffset());

    }

}