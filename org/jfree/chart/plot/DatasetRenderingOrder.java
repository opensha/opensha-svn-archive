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
 * --------------------------
 * DatasetRenderingOrder.java
 * --------------------------
 * (C) Copyright 2003 by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * $Id$
 *
 * Changes:
 * --------
 * 02-May-2003 : Version 1 (DG);
 *
 */

package org.jfree.chart.plot;

import java.io.ObjectStreamException;
import java.io.Serializable;

/**
 * Used to indicate the rendering order for datasets in a {@link org.jfree.chart.plot.CategoryPlot}
 * or an {@link org.jfree.chart.plot.XYPlot}.
 *
 * @author David Gilbert
 */
public final class DatasetRenderingOrder implements Serializable {

    /** The standard order is to render the primary dataset last. */
    public static final DatasetRenderingOrder STANDARD
        = new DatasetRenderingOrder("DatasetRenderingOrder.STANDARD");

    /** The reverse order renders the primary dataset first. */
    public static final DatasetRenderingOrder REVERSE
        = new DatasetRenderingOrder("DatasetRenderingOrder.REVERSE");

    /** The name. */
    private String name;

    /**
     * Private constructor.
     *
     * @param name  the name.
     */
    private DatasetRenderingOrder(String name) {
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
        if (!(o instanceof DatasetRenderingOrder)) {
            return false;
        }

        final DatasetRenderingOrder order = (DatasetRenderingOrder) o;
        if (!this.name.equals(order.toString())) {
            return false;
        }

        return true;

    }
    
    /**
     * Ensures that serialization returns the unique instances.
     * 
     * @return The object.
     * 
     * @throws ObjectStreamException if there is a problem.
     */
    private Object readResolve() throws ObjectStreamException {
        if (this.equals(DatasetRenderingOrder.STANDARD)) {
            return DatasetRenderingOrder.STANDARD;
        }
        else if (this.equals(DatasetRenderingOrder.REVERSE)) {
            return DatasetRenderingOrder.REVERSE;
        }      
        return null;
    }

}
