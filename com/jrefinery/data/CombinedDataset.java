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
 * --------------------
 * CombinedDataset.java
 * --------------------
 * (C) Copyright 2001, 2002, by Bill Kelemen.
 *
 * Original Author:  Bill Kelemen;
 * Contributor(s):   -;
 *
 * $Id$
 *
 * Changes
 * -------
 * 06-Dec-2001 : Version 1 (BK);
 * 27-Dec-2001 : Fixed bug in getChildPosition method (BK);
 * 29-Dec-2001 : Fixed bug in getChildPosition method with complex CombinePlot (BK);
 * 05-Feb-2002 : Small addition to the interface HighLowDataset, as requested by Sylvain
 *               Vieujot (DG);
 * 14-Feb-2002 : Added bug fix for IntervalXYDataset methods, submitted by Gyula Kun-Szabo (DG);
 * 11-Jun-2002 : Updated for change in event constructor (DG);
 *
 */

package com.jrefinery.data;

import java.util.List;

/**
 * This class can combine XYDatasets, HighLowDatasets and IntervalXYDatasets together exposing the
 * union of all the series under one Dataset.  This is required when using a CombinedPlot with a
 * combination of XYPlots, HighLowPlots, TimeSeriesPlot's and VerticalXYBarPlots.
 *
 * @see com.jrefinery.chart.CombinedPlot
 * @author Bill Kelemen (bill@kelemen-usa.com)
 */
public class CombinedDataset extends AbstractSeriesDataset implements XYDataset,
                                                                      HighLowDataset,
                                                                      IntervalXYDataset,
                                                                      CombinationDataset {

    // the Datasets we combine
    private List datasetInfo = new java.util.ArrayList();

    /**
     * Default constructor for an empty combination.
     */
    public CombinedDataset() {
    }

    /**
     * Creates a CombinedDataset initialized with an array of SeriesDatasets.
     * @param data Array of SeriesDataset that contains the SeriesDatasets to combine.
     */
    public CombinedDataset(SeriesDataset[] data) {
        add(data);
    }

    /**
     * Adds one SeriesDataset to the combination. Listeners are notified of the change.
     * @param data SeriesDataset to add.
     */
    public void add(SeriesDataset data) {

        fastAdd(data);
        DatasetChangeEvent event = new DatasetChangeEvent(this, this);
        notifyListeners(event);

    }

    /**
     * Adds an array of SeriesDataset's to the combination. Listeners are notified of the change.
     * @param data Array of SeriesDataset to add
     */
    public void add(SeriesDataset[] data) {

        for (int i=0; i<data.length; i++) {
            fastAdd(data[i]);
        }
        DatasetChangeEvent event = new DatasetChangeEvent(this, this);
        notifyListeners(event);

    }

    /**
     * Adds one series from a SeriesDataset to the combination. Listeners are notified of the
     * change.
     * @param data SeriesDataset where series is contained
     * @param series to add
     */
    public void add(SeriesDataset data, int series) {
        add(new SubSeriesDataset(data, series));
    }

    /**
     * Fast add of a SeriesDataset. Does not notify listeners of the change.
     * @param data SeriesDataset to add
     */
    private void fastAdd(SeriesDataset data) {
        for (int i=0; i<data.getSeriesCount(); i++) {
            datasetInfo.add(new DatasetInfo(data, i));
        }
    }

    //////////////////////////////////////////////////////////////////////////////
    // From SeriesDataset
    //////////////////////////////////////////////////////////////////////////////

    /**
     * Returns the number of series in the dataset.
     * @return The number of series in the dataset.
     */
    public int getSeriesCount() {
        return datasetInfo.size();
    }

    /**
     * Returns the name of a series.
     * @param series The series (zero-based index).
     */
    public String getSeriesName(int series) {
        DatasetInfo di = getDatasetInfo(series);
        return di.data.getSeriesName(di.series);
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
        DatasetInfo di = getDatasetInfo(series);
        return ((XYDataset)di.data).getXValue(di.series, item);
    }

    /**
     * Returns the Y-value for the specified series and item.
     * @param series The index of the series of interest (zero-based);
     * @param item The index of the item of interest (zero-based).
     * @exception ClassCastException if the series if not from a XYDataset
     */
    public Number getYValue(int series, int item) {
        DatasetInfo di = getDatasetInfo(series);
        return ((XYDataset)di.data).getYValue(di.series, item);
    }

    /**
     * Returns the number of items in a series.
     * @param series The index of the series of interest (zero-based);
     * @exception ClassCastException if the series if not from a XYDataset
     */
    public int getItemCount(int series) {
        DatasetInfo di = getDatasetInfo(series);
        return ((XYDataset)di.data).getItemCount(di.series);
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
        DatasetInfo di = getDatasetInfo(series);
        return ((HighLowDataset)di.data).getHighValue(di.series, item);
    }

    /**
     * Returns the low-value for the specified series and item.
     * @param series The index of the series of interest (zero-based);
     * @param item The index of the item of interest (zero-based).
     * @exception ClassCastException if the series if not from a HighLowDataset
     */
    public Number getLowValue(int series, int item) {
        DatasetInfo di = getDatasetInfo(series);
        return ((HighLowDataset)di.data).getLowValue(di.series, item);
    }

    /**
     * Returns the open-value for the specified series and item.
     * @param series The index of the series of interest (zero-based);
     * @param item The index of the item of interest (zero-based).
     * @exception ClassCastException if the series if not from a HighLowDataset
     */
    public Number getOpenValue(int series, int item) {
        DatasetInfo di = getDatasetInfo(series);
        return ((HighLowDataset)di.data).getOpenValue(di.series, item);
    }

    /**
     * Returns the close-value for the specified series and item.
     * @param series The index of the series of interest (zero-based);
     * @param item The index of the item of interest (zero-based).
     * @exception ClassCastException if the series if not from a HighLowDataset
     */
    public Number getCloseValue(int series, int item) {
        DatasetInfo di = getDatasetInfo(series);
        return ((HighLowDataset)di.data).getCloseValue(di.series, item);
    }

    public Number getVolumeValue(int series, int item) {
        DatasetInfo di = getDatasetInfo(series);
        return ((HighLowDataset)di.data).getVolumeValue(di.series, item);
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
        DatasetInfo di = getDatasetInfo(series);
        if (di.data instanceof IntervalXYDataset) {
            return ((IntervalXYDataset)di.data).getStartXValue(di.series, item);
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
        DatasetInfo di = getDatasetInfo(series);
        if (di.data instanceof IntervalXYDataset) {
            return ((IntervalXYDataset)di.data).getEndXValue(di.series, item);
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
        DatasetInfo di = getDatasetInfo(series);
        if (di.data instanceof IntervalXYDataset) {
            return ((IntervalXYDataset)di.data).getStartYValue(di.series, item);
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
        DatasetInfo di = getDatasetInfo(series);
        if (di.data instanceof IntervalXYDataset) {
            return ((IntervalXYDataset)di.data).getEndYValue(di.series, item);
        }
        else {
            return getYValue(series, item);
        }
    }

    //////////////////////////////////////////////////////////////////////////////
    // New methods from CombinationDataset
    //////////////////////////////////////////////////////////////////////////////

    /**
     * Returns the parent Dataset of this combination. If there is more than one
     * parent, or a child is found that is not a CombinationDataset, then returns null.
     */
    public SeriesDataset getParent() {

        SeriesDataset parent = null;
        for (int i=0; i<datasetInfo.size(); i++) {
            SeriesDataset child = getDatasetInfo(i).data;
            if (child instanceof CombinationDataset) {
                SeriesDataset childParent = ((CombinationDataset)child).getParent();
                if (parent == null) {
                    parent = childParent;
                }
                else if (parent != childParent) {
                    return null;
                }
            }
            else {
                return null;
            }
        }
        return parent;

    }

    /**
     * Returns a map or indirect indexing form our series into parent's series.
     * Prior to calling this method, the client should check getParent() to make
     * sure the CombinationDataset uses the same parent. If not, the map returned
     * by this method will be invalid or null.
     * @see #getParent()
     */
    public int[] getMap() {

        int[] map = null;
        for (int i=0; i<datasetInfo.size(); i++) {
            SeriesDataset child = getDatasetInfo(i).data;
            if (child instanceof CombinationDataset) {
                int[] childMap = ((CombinationDataset)child).getMap();
                if (childMap == null) {
                    return null;
                }
                map = joinMap(map, childMap);
            }
            else {
                return null;
            }
        }
        return map;
    }

    //////////////////////////////////////////////////////////////////////////////
    // New Methods
    //////////////////////////////////////////////////////////////////////////////

    public int getChildPosition(Dataset child) {

        int n=0;
        for (int i=0; i<datasetInfo.size(); i++) {
            SeriesDataset childDataset = getDatasetInfo(i).data;
            if (childDataset instanceof CombinedDataset) {
                int m = ((CombinedDataset)childDataset).getChildPosition(child);
                if (m>=0) {
                    return n+m;
                }
                n++;
            }
            else {
                if (child==childDataset) {
                    return n;
                }
                n++;
            }
        }
        return -1;
    }

    //////////////////////////////////////////////////////////////////////////////
    // Private
    //////////////////////////////////////////////////////////////////////////////

    /**
     * Returns the DatasetInfo object associated with the series.
     */
    private DatasetInfo getDatasetInfo(int series) {
        return (DatasetInfo)datasetInfo.get(series);
    }

    /**
     * Joins two map arrays (int[]) together.
     */
    private int[] joinMap(int[] a, int[] b) {
        if (a == null) return b;
        if (b == null) return a;
        int[] result = new int[a.length + b.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }

    /**
     * Private class to store as pairs (SeriesDataset, series) for all combined series.
     * @see add()
     */
    private class DatasetInfo {
        SeriesDataset data;
        int series;

        DatasetInfo(SeriesDataset data, int series) {
            this.data = data;
            this.series = series;
        }
    }

}