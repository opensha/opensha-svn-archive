/* ===================================================
 * JCommon : a free general purpose Java class library
 * ===================================================
 *
 * Project Info:  http://www.object-refinery.com/jcommon/index.html
 * Project Lead:  David Gilbert (david.gilbert@object-refinery.com);
 *
 * (C) Copyright 2000-2003, by Simba Management Limited and Contributors.
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
 * ------------------
 * DateUtilities.java
 * ------------------
 * (C) Copyright 2002, 2003, by Simba Management Limited.
 *
 * Original Author:  David Gilbert (for Simba Management Limited);
 * Contributor(s):   -;
 *
 * $Id$
 *
 * Changes
 * -------
 * 11-Oct-2002 : Version 1 (DG);
 *
 */

package com.jrefinery.date;

import java.util.Calendar;
import java.util.Date;

/**
 * Some useful date methods. 
 * 
 * @author David Gilbert
 */
public class DateUtilities {

    /** A working calendar. */
    private static final Calendar CALENDAR = Calendar.getInstance();

    /**
     * Creates a date.
     *
     * @param yyyy  the year.
     * @param month  the month (1 - 12).
     * @param day  the day.
     *
     * @return a date.
     */
    public static synchronized Date createDate(int yyyy, int month, int day) {
        CALENDAR.set(yyyy, month - 1, day);
        return CALENDAR.getTime();
    }

    /**
     * Creates a date.
     *
     * @param yyyy  the year.
     * @param month  the month (1 - 12).
     * @param day  the day.
     * @param hour  the hour.
     * @param min  the minute.
     *
     * @return a date.
     */
    public static synchronized Date createDate(int yyyy, int month, int day, int hour, int min) {

        CALENDAR.set(yyyy, month, day, hour, min);
        return CALENDAR.getTime();

    }


}
