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
 * -------------------------
 * TimeSeriesCollection.java
 * -------------------------
 * (C) Copyright 2001, 2002, by Simba Management Limited.
 *
 * Original Author:  David Gilbert (for Simba Management Limited);
 * Contributor(s):   -;
 *
 * $Id$
 *
 * Changes
 * -------
 * 11-Oct-2001 : Version 1 (DG);
 * 18-Oct-2001 : Added implementation of IntervalXYDataSource so that bar plots (using numerical
 *               axes) can be plotted from time series data (DG);
 * 22-Oct-2001 : Renamed DataSource.java --> Dataset.java etc. (DG);
 * 15-Nov-2001 : Added getSeries(...) method (DG);
 *               Changed name from TimeSeriesDataset to TimeSeriesCollection (DG);
 * 07-Dec-2001 : TimeSeries --> BasicTimeSeries (DG);
 * 01-Mar-2002 : Added a time zone offset attribute, to enable fast calculation of the time period
 *               start and end values (DG);
 * 29-Mar-2002 : The collection now registers itself with all the time series objects as a
 *               SeriesChangeListener.  Removed redundant calculateZoneOffset method (DG);
 * 06-Jun-2002 : Added a setting to control whether the x-value supplied in the getXValue(...)
 *               method comes from the START, MIDDLE, or END of the time period.  This is a
 *               workaround for JFreeChart, where the current date axis always labels the start
 *               of a time period (DG);
 *
 */

package com.jrefinery.data;

import java.util.List;
import java.util.Date;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * A collection of time series objects.
 * <P>
 * This class implements the IntervalXYDataset interface.  One consequence of this is that this
 * class can be used quite easily to supply data to JFreeChart.
 */
public class TimeSeriesCollection extends AbstractSeriesDataset implements IntervalXYDataset {

    /** Useful constant for controlling the x-value returned for a time period. */
    public static final int START = 0;

    /** Useful constant for controlling the x-value returned for a time period. */
    public static final int MIDDLE = 1;

    /** Useful constant for controlling the x-value returned for a time period. */
    public static final int END = 2;

    /** Storage for the time series. */
    protected List data;

    /** A working calendar (to recycle) */
    protected Calendar workingCalendar;

    protected int position;

    /**
     * Constructs an empty dataset, tied to the default timezone.
     */
    public TimeSeriesCollection() {
        this(null, TimeZone.getDefault());
    }

    /**
     * Constructs an empty dataset, tied to a specific timezone.
     *
     * @param zone The timezone.
     */
    public TimeSeriesCollection(TimeZone zone) {
        this(null, zone);
    }

    /**
     * Constructs a dataset containing a single series (more can be added), tied to the default
     * timezone.
     *
     * @param series The series.
     */
    public TimeSeriesCollection(BasicTimeSeries series) {
        this(series, TimeZone.getDefault());
    }

    /**
     * Constructs a dataset containing a single series (more can be added), tied to a specific
     * timezone.
     *
     * @param series The series.
     * @param zone The timezone.
     */
    public TimeSeriesCollection(BasicTimeSeries series, TimeZone zone) {

        this.data = new java.util.ArrayList();
        if (series!=null) {
            data.add(series);
            series.addChangeListener(this);
        }
        this.workingCalendar = Calendar.getInstance(zone);
        this.position = START;

    }

    /**
     * Returns the position of the x-value returned for a time period (START, MIDDLE, or END).
     *
     * @return The position.
     */
    public int getPosition() {
        return this.position;
    }

    /**
     * Sets the position - this controls the x-value that is returned for a particular time period.
     * <P>
     * Use the constants START, MIDDLE and END.
     *
     * @param position The position.
     */
    public void setPosition(int position) {
        this.position = position;
    }

    /**
     * Returns the number of series in the collection.
     *
     * @return The series count.
     */
    public int getSeriesCount() {
        return this.data.size();
    }

    /**
     * Returns a series.
     *
     * @param series The index of the series (zero-based).
     *
     * @return The series.
     */
    public BasicTimeSeries getSeries(int series) {

        // check arguments...
        if ((series<0) || (series>this.getSeriesCount())) {
            throw new IllegalArgumentException("TimeSeriesDataset.getSeries(...): "
                                               +"index outside valid range.");
        }

        // fetch the series...
        BasicTimeSeries ts = (BasicTimeSeries)data.get(series);
        return ts;

    }

    /**
     * Returns the name of a series.
     * <P>
     * This method is provided for convenience.
     *
     * @param series The index of the series (zero-based).
     *
     * @return The name of a series.
     */
    public String getSeriesName(int series) {

        // check arguments...delegated
        // fetch the series name...
        return this.getSeries(series).getName();

    }

    /**
     * Adds a series to the collection.
     * <P>
     * Notifies all registered listeners that the dataset has changed.
     *
     * @param series The time series.
     */
    public void addSeries(BasicTimeSeries series) {

        // check arguments...
        if (series==null) {
            throw new IllegalArgumentException("TimeSeriesDataset.addSeries(...): "
                                               +"cannot add null series.");
        }

        // add the series...
        data.add(series);
        series.addChangeListener(this);
        this.fireDatasetChanged();

    }

    /**
     * Returns the number of items in the specified series.
     * <P>
     * This method is provided for convenience.
     *
     * @param series The index of the series of interest (zero-based).
     */
    public int getItemCount(int series) {

        return this.getSeries(series).getItemCount();

    }

    /**
     * Returns the x-value for the specified series and item.
     *
     * @param series The series (zero-based index).
     * @param item The item (zero-based index).
     */
    public Number getXValue(int series, int item) {

        BasicTimeSeries ts = (BasicTimeSeries)data.get(series);
        TimeSeriesDataPair dp = ts.getDataPair(item);
        TimePeriod period = dp.getPeriod();

        long result = 0L;
        switch (position) {
            case (START) : result = period.getStart(workingCalendar); break;
            case (MIDDLE) : result = period.getMiddle(workingCalendar); break;
            case (END) : result = period.getEnd(workingCalendar); break;
            default: result = period.getMiddle(workingCalendar);

        }

        return new Long(result);

    }

    /**
     * Returns the starting X value for the specified series and item.
     *
     * @param series The series (zero-based index).
     * @param item The item (zero-based index).
     */
    public Number getStartXValue(int series, int item) {

        BasicTimeSeries ts = (BasicTimeSeries)data.get(series);
        TimeSeriesDataPair dp = ts.getDataPair(item);
        return new Long(dp.getPeriod().getStart(workingCalendar));

    }

    /**
     * Returns the ending X value for the specified series and item.
     *
     * @param series The series (zero-based index).
     * @param item The item (zero-based index).
     */
    public Number getEndXValue(int series, int item) {

        BasicTimeSeries ts = (BasicTimeSeries)data.get(series);
        TimeSeriesDataPair dp = ts.getDataPair(item);
        return new Long(dp.getPeriod().getEnd(workingCalendar));

    }

    /**
     * Returns the y-value for the specified series and item.
     *
     * @param series The series (zero-based index).
     * @param item The item (zero-based index).
     */
    public Number getYValue(int series, int item) {

        BasicTimeSeries ts = (BasicTimeSeries)data.get(series);
        TimeSeriesDataPair dp = (TimeSeriesDataPair)ts.getDataPair(item);
        return dp.getValue();

    }

    /**
     * Returns the starting Y value for the specified series and item.
     *
     * @param series The series (zero-based index).
     * @param item The item (zero-based index).
     */
    public Number getStartYValue(int series, int item) {
        return getYValue(series, item);
    }

    /**
     * Returns the ending Y value for the specified series and item.
     *
     * @param series The series (zero-based index).
     * @param item The item (zero-based index).
     */
    public Number getEndYValue(int series, int item) {
        return getYValue(series, item);
    }

}
