/* ===================================================
 * JCommon : a free general purpose Java class library
 * ===================================================
 *
 * Project Info:  http://www.jfree.org/jcommon/index.html
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
 * ShapeUtils.java
 * ---------------
 * (C)opyright 2003, by Object Refinery Limited and Contributors.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * $$
 *
 * Changes
 * -------
 * 13-Aug-2003 : Version 1 (DG);
 * 
 */

package org.jfree.util;

import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.RectangularShape;

/**
 * Utility methods for {@link Shape} objects.
 * 
 * @author David Gilbert.
 */
public class ShapeUtils {

    /**
     * Returns a clone of the specified shape, or <code>null</code>.
     * 
     * @param shape  the shape to clone.
     * 
     * @return A clone.
     */
    public static Shape clone(Shape shape) {
    
        Shape result = null;
        
        if (shape instanceof Line2D) {
            Line2D line = (Line2D) shape;
            result = (Shape) line.clone();    
        }
        // RectangularShape includes:  Arc2D, Ellipse2D, Rectangle2D, RoundRectangle2D.
        else if (shape instanceof RectangularShape) {
            RectangularShape rectangle = (RectangularShape) shape;
            result = (Shape) rectangle.clone();
        }
        else if (shape instanceof Area) {
            Area area = (Area) shape;
            result = (Shape) area.clone();
        }
        else if (shape instanceof GeneralPath) {
            GeneralPath path = (GeneralPath) shape;
            result = (Shape) path.clone();
        }
        
        return result;
    }

    /**
     * Tests two polygons for equality.
     * 
     * @param p1  polygon 1.
     * @param p2  polygon 2.
     * 
     * @return A boolean.
     */    
    public boolean equal(Polygon p1, Polygon p2) {
        
        boolean result = false;
        if (p1 == null) {
            result = (p2 == null);
        }
        else {
            if (p2 != null) {
                if (p1.npoints == p2.npoints) {
                    result = true;
                    for (int i = 0; i < p1.npoints; i++) {
                        result = result && (p1.xpoints[i] == p2.xpoints[i]);
                        result = result && (p1.ypoints[i] == p2.ypoints[i]);
                    }
                }
                else {
                    result = false;
                }
                
            }
            else {
                result = false;
            }
        }
        return result;
        
    }
    
}
