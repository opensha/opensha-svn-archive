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
 * --------------------
 * PowerFunction2D.java
 * --------------------
 * (C) Copyright 2002, 2003, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * $Id$
 *
 * Changes:
 * --------
 * 01-Oct-2002 : Version 1 (DG);
 *
 */

package org.jfree.data;

/**
 * A function of the form y = a * x ^ b.
 *
 * @author David Gilbert
 */
public class PowerFunction2D implements Function2D {

    /** The 'a' coefficient. */
    private double a;

    /** The 'b' coefficient. */
    private double b;

    /**
     * Creates a new power function.
     *
     * @param a  the 'a' coefficient.
     * @param b  the 'b' coefficient.
     */
    public PowerFunction2D(double a, double b) {
        this.a = a;
        this.b = b;
    }

    /**
     * Returns the value of the function for a given input ('x').
     *
     * @param x  the x-value.
     *
     * @return the value.
     */
    public double getValue(double x) {
        return a * Math.pow(x, b);
    }

}
