/* ======================================
 * JFreeChart : a free Java chart library
 * ======================================
 *
 * Project Info:  http://www.jfree.org/jfreechart/index.html
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
 * ----------------------
 * VerticalValuePlot.java
 * ----------------------
 * (C) Copyright 2000-2003, by Simba Management Limited.
 *
 * Original Author:  David Gilbert (for Simba Management Limited);
 * Contributor(s):   -;
 *
 * $Id$
 *
 * Changes (from 18-Sep-2001)
 * --------------------------
 * 18-Sep-2001 : Added standard header and fixed DOS encoding problem (DG);
 * 23-Apr-2002 : Replaced existing methods with getVerticalDataRange() (DG);
 * 29-Apr-2002 : Added getVerticalAxis() method (DG);
 * 19-Nov-2002 : Added axis parameter to getVerticalDataRange(...) method (DG);
 *
 */

package org.jfree.chart.plot;

import org.jfree.chart.axis.ValueAxis;
import org.jfree.data.Range;

/**
 * An interface defining methods for interrogating a plot that displays values
 * along the vertical axis.
 * <P>
 * Used by vertical axes (when auto-adjusting the axis range) to determine the
 * minimum and maximum data values.  Also used by the ChartPanel class for zooming.
 *
 * @author David Gilbert
 */
public interface VerticalValuePlot {

    /**
     * Returns the range for the data to be plotted against the vertical axis.
     *
     * @param axis  the axis.
     * 
     * @return The range.
     */
    public Range getVerticalDataRange(ValueAxis axis);

    /**
     * Returns the vertical axis.
     *
     * @return the axis.
     */
    public ValueAxis getVerticalValueAxis();

}
