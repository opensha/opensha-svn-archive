/* ==================================================
 * JCommon : a general purpose class library for Java
 * ==================================================
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
 * -------------
 * XYSeries.java
 * -------------
 * (C) Copyright 2001, Simba Management Limited and Contributors.
 *
 * Original Author:  David Gilbert (for Simba Management Limited);
 * Contributor(s):   Aaron Metzger;
 *
 * $Id$
 *
 * Changes
 * -------
 * 15-Nov-2001 : Version 1 (DG);
 * 03-Apr-2002 : Added an add(double, double) method (DG);
 * 29-Apr-2002 : Added a clear() method (ARM);
 * 06-Jun-2002 : Updated Javadoc comments (DG);
 *
 */

package com.jrefinery.data;

import java.util.Collections;
import java.util.List;

/**
 * Represents a sequence of zero or more data pairs in the form (x, y).
 */
public class XYSeries extends Series {

    /** The list of data pairs in the series. */
    protected List data;

    /**
     * Constructs a new xy-series that contains no data.
     *
     * @param name The series name.
     */
    public XYSeries(String name) {

        super(name);
        data = new java.util.ArrayList();

    }

    /**
     * Returns the number of items in the series.
     *
     * @return The item count.
     */
    public int getItemCount() {
        return data.size();
    }

    /**
     * Adds a data item to the series.
     *
     * @param pair The (x, y) pair.
     */
    public void add(XYDataPair pair) throws SeriesException {

        // check arguments...
        if (pair==null) {
            throw new IllegalArgumentException("XYSeries.add(...): null item not allowed.");
        }

        // make the change (if it's not a duplicate x-value)...
        int index = Collections.binarySearch(data, pair);
        if (index<0) {
            data.add(-index-1, pair);
            this.fireSeriesChanged();
        }
        else {
            throw new SeriesException("XYSeries.add(...): x-value already exists.");
        }

    }

    /**
     * Adds a data item to the series.
     *
     * @param x The x value.
     * @param y The y value.
     */
    public void add(double x, double y) throws SeriesException {

        this.add(new Double(x), new Double(y));

    }

    /**
     * Adds new data to the series.
     * <P>
     * Throws an exception if the x-value is a duplicate.
     *
     * @param x The x-value.
     * @param y The y-value.
     */
    public void add(Number x, Number y) throws SeriesException {

        XYDataPair pair = new XYDataPair(x, y);
        add(pair);

    }

    /**
     * Removes all data pairs from the series.
     */
    public void clear() {

        if (data.size()>0) {
            data.clear();
            fireSeriesChanged();
        }

    }

    /**
     * Return the data pair with the specified index.
     *
     * @param index The index.
     */
    public XYDataPair getDataPair(int index) {
        return (XYDataPair)data.get(index);
    }

    /**
     * Returns the x-value at the specified index.
     *
     * @param index The index.
     *
     * @return The x-value.
     */
    public Number getXValue(int index) {
        return getDataPair(index).getX();
    }

    /**
     * Returns the y-value at the specified index.
     *
     * @param index The index.
     *
     * @return The y-value.
     */
    public Number getYValue(int index) {
        return getDataPair(index).getY();
    }

    /**
     * Updates the value of an item in the series.
     *
     * @param index The item (zero based index).
     * @param y The new value.
     */
    public void update(int item, Number y) {
        XYDataPair pair = getDataPair(item);
        pair.setY(y);
        fireSeriesChanged();
    }

}
