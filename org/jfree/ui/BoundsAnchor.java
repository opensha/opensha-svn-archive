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
 * BoundsAnchor.java
 * -----------------
 * (C) Copyright 2003 by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * $Id$
 *
 * Changes:
 * --------
 * 21-May-2003 (DG);
 */

package org.jfree.ui;

import java.io.Serializable;

/**
 * Used to indicate the position of an anchor point for a bounding rectangle.
 *
 * @author David Gilbert
 */
public class BoundsAnchor implements Serializable {

    /** Top/left. */
    public static final BoundsAnchor TOP_LEFT = new BoundsAnchor("BoundsAnchor.TOP_LEFT");

    /** Top/center. */
    public static final BoundsAnchor TOP_CENTER = new BoundsAnchor("BoundsAnchor.TOP_CENTER");

    /** Top/right. */
    public static final BoundsAnchor TOP_RIGHT = new BoundsAnchor("BoundsAnchor.TOP_RIGHT");

    /** Middle/left. */
    public static final BoundsAnchor MIDDLE_LEFT = new BoundsAnchor("BoundsAnchor.MIDDLE_LEFT");

    /** Middle/center. */
    public static final BoundsAnchor CENTER = new BoundsAnchor("BoundsAnchor.CENTER");

    /** Middle/right. */
    public static final BoundsAnchor MIDDLE_RIGHT = new BoundsAnchor("BoundsAnchor.MIDDLE_RIGHT");

    /** Bottom/left. */
    public static final BoundsAnchor BOTTOM_LEFT = new BoundsAnchor("BoundsAnchor.BOTTOM_LEFT");

    /** Bottom/center. */
    public static final BoundsAnchor BOTTOM_CENTER = new BoundsAnchor("BoundsAnchor.BOTTOM_CENTER");

    /** Bottom/right. */
    public static final BoundsAnchor BOTTOM_RIGHT = new BoundsAnchor("BoundsAnchor.BOTTOM_RIGHT");

    /** The name. */
    private String name;

    /**
     * Private constructor.
     *
     * @param name  the name.
     */
    private BoundsAnchor(String name) {
        this.name = name;
    }

    /**
     * Returns a string representing the object.
     *
     * @return The string.
     */
    public String toString() {
        return this.name;
    }

    /**
     * Returns <code>true</code> if this object is equal to the specified object, and
     * <code>false</code> otherwise.
     *
     * @param o  the other object.
     *
     * @return A boolean.
     */
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (!(o instanceof BoundsAnchor)) {
            return false;
        }

        final BoundsAnchor order = (BoundsAnchor) o;
        if (!this.name.equals(order.toString())) {
            return false;
        }

        return true;

    }

}
