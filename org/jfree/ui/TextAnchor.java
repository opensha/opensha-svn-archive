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
 * ---------------
 * TextAnchor.java
 * ---------------
 * (C) Copyright 2003 by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * $Id$
 *
 * Changes:
 * --------
 * 10-Jun-2003 (DG);
 */

package org.jfree.ui;

import java.io.Serializable;

/**
 * Used to indicate the position of an anchor point for a text string.
 *
 * @author David Gilbert
 */
public final class TextAnchor implements Serializable {

    /** Top/left. */
    public static final TextAnchor TOP_LEFT = new TextAnchor("TextAnchor.TOP_LEFT");

    /** Top/center. */
    public static final TextAnchor TOP_CENTER = new TextAnchor("TextAnchor.TOP_CENTER");

    /** Top/right. */
    public static final TextAnchor TOP_RIGHT = new TextAnchor("TextAnchor.TOP_RIGHT");

    /** Half-ascent/left. */
    public static final TextAnchor HALF_ASCENT_LEFT = new TextAnchor("TextAnchor.HALF_ASCENT_LEFT");

    /** Half-ascent/center. */
    public static final TextAnchor HALF_ASCENT_CENTER 
        = new TextAnchor("TextAnchor.HALF_ASCENT_CENTER");

    /** Half-ascent/right. */
    public static final TextAnchor HALF_ASCENT_RIGHT 
        = new TextAnchor("TextAnchor.HALF_ASCENT_RIGHT");

    /** Middle/left. */
    public static final TextAnchor CENTER_LEFT = new TextAnchor("TextAnchor.CENTER_LEFT");

    /** Middle/center. */
    public static final TextAnchor CENTER = new TextAnchor("TextAnchor.CENTER");

    /** Middle/right. */
    public static final TextAnchor CENTER_RIGHT = new TextAnchor("TextAnchor.CENTER_RIGHT");

    /** Baseline/left. */
    public static final TextAnchor BASELINE_LEFT = new TextAnchor("TextAnchor.BASELINE_LEFT");

    /** Baseline/center. */
    public static final TextAnchor BASELINE_CENTER = new TextAnchor("TextAnchor.BASELINE_CENTER");

    /** Baseline/right. */
    public static final TextAnchor BASELINE_RIGHT = new TextAnchor("TextAnchor.BASELINE_RIGHT");

    /** Bottom/left. */
    public static final TextAnchor BOTTOM_LEFT = new TextAnchor("TextAnchor.BOTTOM_LEFT");

    /** Bottom/center. */
    public static final TextAnchor BOTTOM_CENTER = new TextAnchor("TextAnchor.BOTTOM_CENTER");

    /** Bottom/right. */
    public static final TextAnchor BOTTOM_RIGHT = new TextAnchor("TextAnchor.BOTTOM_RIGHT");

    /** The name. */
    private String name;

    /**
     * Private constructor.
     *
     * @param name  the name.
     */
    private TextAnchor(String name) {
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
        if (!(o instanceof TextAnchor)) {
            return false;
        }

        final TextAnchor order = (TextAnchor) o;
        if (!this.name.equals(order.toString())) {
            return false;
        }

        return true;

    }

    /**
     * Returns the anchor point that is horizontally opposite.
     *
     * @param anchor  the anchor point.
     * 
     * @return The horizontally opposite anchor point.
     */
    public static TextAnchor getHorizontalOpposite(TextAnchor anchor) {
        if (anchor == TOP_LEFT) {
            return TOP_RIGHT;
        }
        else if (anchor == TOP_CENTER) {
            return TOP_CENTER;
        }
        else if (anchor == TOP_RIGHT) {
            return TOP_LEFT;
        }
        else if (anchor == HALF_ASCENT_LEFT) {
            return HALF_ASCENT_RIGHT;
        }
        else if (anchor == HALF_ASCENT_CENTER) {
            return HALF_ASCENT_CENTER;
        }
        else if (anchor == HALF_ASCENT_RIGHT) {
            return HALF_ASCENT_LEFT;
        }
        else if (anchor == CENTER_LEFT) {
            return CENTER_RIGHT;
        }
        else if (anchor == CENTER) {
            return CENTER;
        }
        else if (anchor == CENTER_RIGHT) {
            return CENTER_LEFT;
        }
        else if (anchor == BASELINE_LEFT) {
            return BASELINE_RIGHT;
        }
        else if (anchor == BASELINE_CENTER) {
            return BASELINE_CENTER;
        }
        else if (anchor == BASELINE_RIGHT) {
            return BASELINE_LEFT;
        }
        else if (anchor == BOTTOM_LEFT) {
            return BOTTOM_RIGHT;
        }
        else if (anchor == BOTTOM_CENTER) {
            return BOTTOM_CENTER;
        }
        else if (anchor == BOTTOM_RIGHT) {
            return BOTTOM_LEFT;
        }
        return null;
    }

    /**
     * Returns the anchor point that is vertically opposite.
     *
     * @param anchor  the anchor point.
     * 
     * @return The vertically opposite anchor point.
     */
    public static TextAnchor getVerticalOpposite(TextAnchor anchor) {
        if (anchor == TOP_LEFT) {
            return BOTTOM_LEFT;
        }
        else if (anchor == TOP_CENTER) {
            return BOTTOM_CENTER;
        }
        else if (anchor == TOP_RIGHT) {
            return BOTTOM_RIGHT;
        }
        else if (anchor == HALF_ASCENT_LEFT) {
            return HALF_ASCENT_LEFT;
        }
        else if (anchor == HALF_ASCENT_CENTER) {
            return HALF_ASCENT_CENTER;
        }
        else if (anchor == HALF_ASCENT_RIGHT) {
            return HALF_ASCENT_RIGHT;
        }
        else if (anchor == CENTER_LEFT) {
            return CENTER_LEFT;
        }
        else if (anchor == CENTER) {
            return CENTER;
        }
        else if (anchor == CENTER_RIGHT) {
            return CENTER_RIGHT;
        }
        else if (anchor == BASELINE_LEFT) {
            return TOP_LEFT;
        }
        else if (anchor == BASELINE_CENTER) {
            return TOP_CENTER;
        }
        else if (anchor == BASELINE_RIGHT) {
            return TOP_RIGHT;
        }
        else if (anchor == BOTTOM_LEFT) {
            return TOP_LEFT;
        }
        else if (anchor == BOTTOM_CENTER) {
            return TOP_CENTER;
        }
        else if (anchor == BOTTOM_RIGHT) {
            return TOP_RIGHT;
        }
        return null;
    }

}
