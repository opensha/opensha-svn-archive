/* ======================================
 * JFreeChart : a free Java chart library
 * ======================================
 *
 * Project Info:  http://www.object-refinery.com/jfreechart/index.html
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
 * --------------------------
 * AbstractSeriesDataset.java
 * --------------------------
 * (C) Copyright 2001-2003, by Simba Management Limited.
 *
 * Original Author:  David Gilbert (for Simba Management Limited);
 * Contributor(s):   -;
 *
 * $Id$
 *
 * Changes
 * -------
 * 17-Nov-2001 : Version 1 (DG);
 * 28-Mar-2002 : Implemented SeriesChangeListener interface (DG);
 * 04-Oct-2002 : Fixed errors reported by Checkstyle (DG);
 * 04-Feb-2003 : Removed redundant methods (DG);
 *
 */

package com.jrefinery.data;

/**
 * An abstract implementation of the {@link SeriesDataset} interface, containing a 
 * mechanism for registering change listeners.
 *
 * @author David Gilbert
 */
public abstract class AbstractSeriesDataset extends AbstractDataset
                                            implements SeriesDataset, SeriesChangeListener {

    /**
     * Creates a new dataset.
     */
    protected AbstractSeriesDataset() {
        super();
    }

    /**
     * Returns the number of series in the dataset.
     *
     * @return the number of series in the dataset.
     */
    public abstract int getSeriesCount();

    /**
     * Returns the name of a series.
     *
     * @param series  the series (zero-based index).
     *
     * @return the series name.
     */
    public abstract String getSeriesName(int series);

    /**
     * Called when a series belonging to the dataset changes.
     *
     * @param event  information about the change.
     */
    public void seriesChanged(SeriesChangeEvent event) {
        fireDatasetChanged();
    }

}
