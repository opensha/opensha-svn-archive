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
 * ---------------------
 * SubseriesDataset.java
 * ---------------------
 * (C) Copyright 2001, 2002, by Bill Kelemen and Contributors.
 *
 * Original Author:  Bill Kelemen;
 * Contributor(s):   Sylvain Vieujot;
 *
 * $Id$
 *
 * Changes
 * -------
 * 06-Dec-2001 : Version 1 (BK);
 * 05-Feb-2002 : Added SignalsDataset (and small change to HighLowDataset interface) as requested
 *               by Sylvain Vieujot (DG);
 * 28-Feb-2002 : Fixed bug: missing map[series] in IntervalXYDataset and SignalsDataset methods (BK);
 *
 */

package com.jrefinery.data;

/**
 * This class will create a Dataset with one or more series from another
 * SeriesDataset. This is required when using a CombinedPlot to assign a sub-dataset
 * to internal plots and avoid displaying all series on all internal plots.
 *
 * @see com.jrefinery.chart.CombinedPlot
 * @author Bill Kelemen (bill@kelemen-usa.com)
 */
public class SubSeriesDataset extends AbstractSeriesDataset implements HighLowDataset,
                                                                       SignalsDataset,
                                                                       IntervalXYDataset,
                                                                       CombinationDataset {

    private SeriesDataset parent = null;
    private int[] map;  // maps our series into our parent's

    /**
     * Creates a SubSeriesDataset using one or more series from <code>parent</code>.
     * The series to use are passed as an array of int.
     * @param parent Underlying dataset
     * @param map int[] of series from parent to include in this Dataset
     */
    public SubSeriesDataset(SeriesDataset parent, int[] map) {
        this.parent = parent;
        this.map = map;
        //parent.addChangeListener(this);
    }

    /**
     * Creates a SubSeriesDataset using one series from <code>parent</code>.
     * The series to is passed as an int.
     * @param parent Underlying dataset
     * @param series Series from parent to include in this Dataset
     */
    public SubSeriesDataset(SeriesDataset parent, int series) {
        this(parent, new int[] {series});
    }

    //////////////////////////////////////////////////////////////////////////////
    // From HighLowDataset
    //////////////////////////////////////////////////////////////////////////////

    /**
     * Returns the high-value for the specified series and item.
     * @param series The index of the series of interest (zero-based);
     * @param item The index of the item of interest (zero-based).
     * @exception ClassCastException if the series if not from a HighLowDataset
     */
    public Number getHighValue(int series, int item) {
        return ((HighLowDataset)parent).getHighValue(map[series], item);
    }

    /**
     * Returns the low-value for the specified series and item.
     * @param series The index of the series of interest (zero-based);
     * @param item The index of the item of interest (zero-based).
     * @exception ClassCastException if the series if not from a HighLowDataset
     */
    public Number getLowValue(int series, int item) {
        return ((HighLowDataset)parent).getLowValue(map[series], item);
    }

    /**
     * Returns the open-value for the specified series and item.
     * @param series The index of the series of interest (zero-based);
     * @param item The index of the item of interest (zero-based).
     * @exception ClassCastException if the series if not from a HighLowDataset
     */
    public Number getOpenValue(int series, int item) {
        return ((HighLowDataset)parent).getOpenValue(map[series], item);
    }

    /**
     * Returns the close-value for the specified series and item.
     * @param series The index of the series of interest (zero-based);
     * @param item The index of the item of interest (zero-based).
     * @exception ClassCastException if the series if not from a HighLowDataset
     */
    public Number getCloseValue(int series, int item) {
        return ((HighLowDataset)parent).getCloseValue(map[series], item);
    }

    public Number getVolumeValue(int series, int item) {
        return ((HighLowDataset)parent).getVolumeValue(map[series], item);
    }


    //////////////////////////////////////////////////////////////////////////////
    // From XYDataset
    //////////////////////////////////////////////////////////////////////////////

    /**
     * Returns the X-value for the specified series and item.
     * @param series The index of the series of interest (zero-based);
     * @param item The index of the item of interest (zero-based).
     * @exception ClassCastException if the series if not from a XYDataset
     */
    public Number getXValue(int series, int item) {
        return ((XYDataset)parent).getXValue(map[series], item);
    }

    /**
     * Returns the Y-value for the specified series and item.
     * @param series The index of the series of interest (zero-based);
     * @param item The index of the item of interest (zero-based).
     * @exception ClassCastException if the series if not from a XYDataset
     */
    public Number getYValue(int series, int item) {
        return ((XYDataset)parent).getYValue(map[series], item);
    }

    /**
     * Returns the number of items in a series.
     * @param series The index of the series of interest (zero-based);
     * @exception ClassCastException if the series if not from a XYDataset
     */
    public int getItemCount(int series) {
        return ((XYDataset)parent).getItemCount(map[series]);
    }

    //////////////////////////////////////////////////////////////////////////////
    // From SeriesDataset
    //////////////////////////////////////////////////////////////////////////////

    /**
     * Returns the number of series in the dataset.
     * @return The number of series in the dataset.
     */
    public int getSeriesCount() {
        return map.length;
    }

    /**
     * Returns the name of a series.
     * @param series The series (zero-based index).
     */
    public String getSeriesName(int series) {
        return parent.getSeriesName(map[series]);
    }

    //////////////////////////////////////////////////////////////////////////////
    // From IntervalXYDataset
    //////////////////////////////////////////////////////////////////////////////

    /**
     * Returns the starting X value for the specified series and item.
     * @param series The index of the series of interest (zero-based);
     * @param item The index of the item of interest (zero-based).
     */
    public Number getStartXValue(int series, int item) {
        if (parent instanceof IntervalXYDataset) {
            return ((IntervalXYDataset)parent).getStartXValue(map[series], item);
        }
        else {
            return getXValue(series, item);
        }
    }

    /**
     * Returns the ending X value for the specified series and item.
     * @param series The index of the series of interest (zero-based);
     * @param item The index of the item of interest (zero-based).
     */
    public Number getEndXValue(int series, int item) {
        if (parent instanceof IntervalXYDataset) {
            return ((IntervalXYDataset)parent).getEndXValue(map[series], item);
        }
        else {
            return getXValue(series, item);
        }
    }

    /**
     * Returns the starting Y value for the specified series and item.
     * @param series The index of the series of interest (zero-based);
     * @param item The index of the item of interest (zero-based).
     */
    public Number getStartYValue(int series, int item) {
        if (parent instanceof IntervalXYDataset) {
            return ((IntervalXYDataset)parent).getStartYValue(map[series], item);
        }
        else {
            return getYValue(series, item);
        }
    }

    /**
     * Returns the ending Y value for the specified series and item.
     * @param series The index of the series of interest (zero-based);
     * @param item The index of the item of interest (zero-based).
     */
    public Number getEndYValue(int series, int item) {
        if (parent instanceof IntervalXYDataset) {
            return ((IntervalXYDataset)parent).getEndYValue(map[series], item);
        }
        else {
            return getYValue(series, item);
        }
    }

    ///////////////////////////////////////////////////////////////////////////////
    // From SignalsDataset
    ///////////////////////////////////////////////////////////////////////////////

    public int getType(int series, int item) {
        if (parent instanceof SignalsDataset) {
            return ((SignalsDataset)parent).getType(map[series], item);
        }
        else {
            return getYValue(series, item).intValue();
        }
    }

    public double getLevel(int series, int item) {
        if (parent instanceof SignalsDataset) {
            return ((SignalsDataset)parent).getLevel(map[series], item);
        }
        else {
            return getYValue(series, item).doubleValue();
        }
    }

    //////////////////////////////////////////////////////////////////////////////
    // New methods from CombinationDataset
    //////////////////////////////////////////////////////////////////////////////

    /**
     * Returns the parent Dataset of this combination.
     */
    public SeriesDataset getParent() {
        return parent;
    }

    /**
     * Returns a map or indirect indexing form our series into parent's series.
     */
    public int[] getMap() {
        return (int[])map.clone();
    }

}
