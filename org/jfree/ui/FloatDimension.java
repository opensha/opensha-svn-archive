/* ===================================================
 * JCommon : a free general purpose Java class library
 * ===================================================
 *
 * Project Info:  http://www.jfree.org/jcommon/index.html
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
 * -------------------
 * FloatDimension.java
 * -------------------
 * (C)opyright 2000-2002, by Thomas Morgner and Contributors.
 *
 * Original Author:  Thomas Morgner;
 * Contributor(s):   David Gilbert (for Simba Management Limited);
 *
 * $Id$
 *
 * Changes
 * -------
 * 05-Dec-2002 : Updated Javadocs (DG);
 * 29-Apr-2003 : Moved to JCommon
 */
package org.jfree.ui;

import java.awt.geom.Dimension2D;
import java.io.Serializable;

/**
 * A dimension object specified using <code>float</code> values.
 *
 * @author Thomas Morgner
 */
public class FloatDimension extends Dimension2D implements Cloneable, Serializable {

    /** The width. */
    private float width;

    /** The height. */
    private float height;

    /**
     * Creates a new dimension object with width and height set to zero.
     */
    public FloatDimension() {
        width = 0.0f;
        height = 0.0f;
    }

    /**
     * Creates a new dimension that is a copy of another dimension.
     *
     * @param fd  the dimension to copy.
     */
    public FloatDimension(FloatDimension fd) {
        this.width = fd.width;
        this.height = fd.height;
    }

    /**
     * Creates a new dimension.
     *
     * @param width  the width.
     * @param height  the height.
     */
    public FloatDimension(float width, float height) {
        this.width = width;
        this.height = height;
    }

    /**
     * Returns the width.
     *
     * @return the width.
     */
    public double getWidth() {
        return width;
    }

    /**
     * Returns the height.
     *
     * @return the height.
     */
    public double getHeight() {
        return height;
    }

    /**
     * Sets the width.
     *
     * @param width  the width.
     */
    public void setWidth(double width) {
        this.width = (float) width;
    }

    /**
     * Sets the height.
     *
     * @param height  the height.
     */
    public void setHeight(double height) {
        this.height = (float) height;
    }

    /**
     * Sets the size of this <code>Dimension</code> object to the specified width and height.
     * This method is included for completeness, to parallel the
     * {@link java.awt.Component#getSize getSize} method of
     * {@link java.awt.Component}.
     * @param width  the new width for the <code>Dimension</code>
     * object
     * @param height  the new height for the <code>Dimension</code>
     * object
     */
    public void setSize(double width, double height) {
        setHeight((float) height);
        setWidth((float) width);
    }

    /**
     * Creates and returns a copy of this object.
     *
     * @return     a clone of this instance.
     * @exception  java.lang.OutOfMemoryError            if there is not enough memory.
     * @see        java.lang.Cloneable
     */
    public Object clone() {
        return super.clone();
    }

    /**
     * Returns a string representation of the object. In general, the
     * <code>toString</code> method returns a string that
     * "textually represents" this object. The result should
     * be a concise but informative representation that is easy for a
     * person to read.
     * <p>
     *
     * @return  a string representation of the object.
     */
    public String toString() {
        return getClass().getName() + ":={width=" + getWidth() + ", height=" + getHeight() + "}";
    }

    /**
     * Tests this object for equality with another object.
     *
     * @param o  the other object.
     *
     * @return <code>true</code> or <code>false</code>.
     */
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof FloatDimension)) {
            return false;
        }

        final FloatDimension floatDimension = (FloatDimension) o;

        if (height != floatDimension.height) {
            return false;
        }
        if (width != floatDimension.width) {
            return false;
        }

        return true;
    }

    /**
     * Returns a hash code.
     *
     * @return A hash code.
     */
    public int hashCode() {
        int result;
        result = Float.floatToIntBits(width);
        result = 29 * result + Float.floatToIntBits(height);
        return result;
    }
}

