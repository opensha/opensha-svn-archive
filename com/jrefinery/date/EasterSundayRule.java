/* ================================================================
 * JCommon : a general purpose, open source, class library for Java
 * ================================================================
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
 * ---------------------
 * EasterSundayRule.java
 * ---------------------
 * (C) Copyright 2000-2002, by Simba Management Limited.
 *
 * Original Author:  David Gilbert (for Simba Management Limited);
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

package com.jrefinery.date;

/**
 * An annual date rule for Easter (Sunday).  The algorithm used here was
 * obtained from a Calendar FAQ which can be found at:
 * <P>
 * <a href="http://www.tondering.dk/claus/calendar.html"
 *     >http://www.tondering.dk/claus/calendar.html</a>.
 * <P>
 * It is based on an algorithm by Oudin (1940) and quoted in "Explanatory Supplement to the
 * Astronomical Almanac", P. Kenneth Seidelmann, editor.
 *
 * @author DG
 */
public class EasterSundayRule extends AnnualDateRule implements Cloneable {

    /**
     * Returns the date of Easter Sunday for the given year.  See the class
     * description for the source of the algorithm.
     * <P>
     * This method supports the AnnualDateRule interface.
     *
     * @param year  the year to check.
     *
     * @return the date of Easter Sunday for the given year.
     */
    public SerialDate getDate(int year) {
        int g = year % 19;
        int c = year / 100;
        int h = (c - c / 4 - (8 * c + 13) / 25 + 19 * g + 15) % 30;
        int i = h - h / 28 * (1 - h / 28 * 29 / (h + 1) * (21 - g) / 11);
        int j = (year + year / 4 + i + 2 - c + c / 4) % 7;
        int l = i - j;
        int month = 3 + (l + 40) / 44;
        int day = l + 28 - 31 * (month / 4);
        return SerialDate.createInstance(day, month, year);
    }

}
