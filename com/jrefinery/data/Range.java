/* =======================================
 * JFreeChart : a Java Chart Class Library
 * =======================================
 *
 * Project Info:  http://www.object-refinery.com/jfreechart/index.html
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
 * Range.java
 * ----------
 * (C) Copyright 2002, by Simba Management Limited and Contributors.
 *
 * Original Author:  David Gilbert (for Simba Management Limited);
 * Contributor(s):   -;
 *
 * $Id$
 *
 * Changes (from 23-Jun-2001)
 * --------------------------
 * 22-Apr-2002 : Version 1, loosely based by code by Bill Kelemen (DG);
 * 30-Apr-2002 : Added getLength() and getCentralValue() methods.  Changed argument check in
 *               constructor (DG);
 * 13-Jun-2002 : Added contains(double) method (DG);
 *
 */

package com.jrefinery.data;

/**
 * Represents the visible range for an axis.
 */
public class Range {

    /** The lower bound for the visible range. */
    protected double lower;

    /** The upper bound for the visible range. */
    protected double upper;

    /**
     * Constructs a new axis range.
     *
     * @param lower The lower bound.
     * @param upper The upper bound.
     */
    public Range(double lower, double upper) {

        if (lower>upper) {
            throw new IllegalArgumentException("Range(double, double): require lower<=upper.");
        }

        this.lower = lower;
        this.upper = upper;

    }

    /**
     * Returns the lower bound for the range.
     *
     * @return The lower bound.
     */
    public double getLowerBound() {
        return this.lower;
    }

    /**
     * Returns the upper bound for the range.
     *
     * @return The upper bound.
     */
    public double getUpperBound() {
        return this.upper;
    }

    /**
     * Returns the length of the range.
     *
     * @return The length.
     */
    public double getLength() {
        return upper-lower;
    }

    /**
     * Returns the central value for the range.
     *
     * @return The central value.
     */
    public double getCentralValue() {
        return lower/2 + upper/2;
    }

    /**
     * Returns true if the range contains the specified value.
     */
    public boolean contains(double value) {
        return (value>=lower && value<=upper);
    }

    /**
     * Creates a new range by combining two existing ranges.
     *
     * @param range1 The first range.
     * @param range2 The second range.
     */
    public static Range combine(Range range1, Range range2) {

        Range result = null;

        if ((range1!=null) && (range2==null)) {
            result = range1;
        }
        else if ((range1==null) && (range2!=null)) {
            result = range2;
        }
        else {
            double l = Math.min(range1.getLowerBound(), range2.getLowerBound());
            double u = Math.max(range1.getUpperBound(), range2.getUpperBound());
            result = new Range(l, u);
        }

        return result;

    }

}