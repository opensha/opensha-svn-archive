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
 * -----------
 * Size2D.java
 * -----------
 * (C) Copyright 2000-2002, by Simba Management Limited.
 *
 * Original Author:  David Gilbert (for Simba Management Limited);
 * Contributor(s):   -;
 *
 * $Id$
 *
 * Changes (from 26-Oct-2001)
 * --------------------------
 * 26-Oct-2001 : Changed package to com.jrefinery.ui.*;
 *
 */

package com.jrefinery.ui;
/**
 * A simple class for representing the dimensions of an object.  I would use Dimension2D, but refer
 * to Bug ID 4189446 on the Java Developer Connection for why not (last checked 20 July 2000, maybe
 * it's been fixed now).
 */
public class Size2D {

    /** The width; */
    public double width;

    /** The height; */
    public double height;

    /**
     * Standard constructor - builds a Size2D with the specified width and height.
     * @param width The width;
     * @param height The height;
     */
    public Size2D(double width, double height) {
        this.width = width;
        this.height = height;
    }

    /**
     * Returns the height;
     */
    public double getHeight() {
        return this.height;
    }

    /**
     * Returns the width;
     */
    public double getWidth() {
        return this.width;
    }

}