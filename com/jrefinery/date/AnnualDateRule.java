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
 * -------------------
 * AnnualDateRule.java
 * -------------------
 * (C) Copyright 2000-2002, by Simba Management Limited.
 *
 * Original Author:  David Gilbert (for Simba Management Limited);
 * Contributor(s):   -;
 *
 * $Id$
 *
 * Changes (from 26-Oct-2001)
 * --------------------------
 * 26-Oct-2001 : Changed package to com.jrefinery.date.* (DG);
 * 12-Nov-2001 : Javadoc comments updated (DG);
 *
 */

package com.jrefinery.date;

/**
 * The base class for all 'annual' date rules: that is, rules for generating one date for any
 * given year.
 * <P>
 * One example is Easter Sunday (which can be calculated using published algorithms).
 */
public abstract class AnnualDateRule implements Cloneable {

    /**
     * Returns the date for this rule, given the year.
     * @param yyyy The year (1900 <= year <= 9999).
     * @return The date for this rule, given the year.
     */
    public abstract SerialDate getDate(int year);

    /**
     * Returns a clone of the rule.
     * <P>
     * You should refer to the documentation of the clone() method in each subclass for exact
     * details.
     * @return A clone of the rule.
     */
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

}