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
 * -------------------
 * XYItemRenderer.java
 * -------------------
 * (C) Copyright 2001-2003, by Object Refinery Limited and Contributors.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   Mark Watson (www.markwatson.com);
 *                   Sylvain Vieujot;
 *                   Focus Computer Services Limited;
 *                   Richard Atkinson;
 *
 * $Id$
 *
 * Changes
 * -------
 * 19-Oct-2001 : Version 1, based on code by Mark Watson (DG);
 * 22-Oct-2001 : Renamed DataSource.java --> Dataset.java etc. (DG);
 * 13-Dec-2001 : Changed return type of drawItem from void --> Shape.  The area returned can
 *               be used as the tooltip region.
 * 23-Jan-2002 : Added DrawInfo parameter to drawItem(...) method (DG);
 * 28-Mar-2002 : Added a property change listener mechanism.  Now renderers do not have to be
 *               immutable (DG);
 * 04-Apr-2002 : Added the initialise(...) method (DG);
 * 09-Apr-2002 : Removed the translated zero from the drawItem method, it can be calculated inside
 *               the initialise method if it is required.  Added a new getToolTipGenerator()
 *               method.  Changed the return type for drawItem() to void (DG);
 * 24-May-2002 : Added ChartRenderingInfo the initialise method API (DG);
 * 25-Jun-2002 : Removed redundant import (DG);
 * 20-Aug-2002 : Added get/setURLGenerator methods to interface (DG);
 * 02-Oct-2002 : Fixed errors reported by Checkstyle (DG);
 * 18-Nov-2002 : Added methods for drawing grid lines (DG);
 * 17-Jan-2003 : Moved plot classes into a separate package (DG);
 * 27-Jan-2003 : Added shape lookup table (DG);
 * 05-Jun-2003 : Added domain and range grid bands (sponsored by Focus Computer Services Ltd) (DG);
 * 27-Jul-2003 : Added getRangeType() to support stacked XY area charts (RA);
 * 16-Sep-2003 : Changed ChartRenderingInfo --> PlotRenderingInfo (DG);
 *
 */

package org.jfree.chart.renderer;

import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeListener;

import org.jfree.chart.CrosshairInfo;
import org.jfree.chart.LegendItem;
import org.jfree.chart.Marker;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.urls.XYURLGenerator;
import org.jfree.data.XYDataset;

/**
 * Interface for rendering the visual representation of a single (x, y) item on an
 * {@link XYPlot}.
 * <p>
 * To support cloning charts, it is recommended that renderers implement both the {@link Cloneable} 
 * and <code>PublicCloneable</code> interfaces.
 *
 * @author David Gilbert
 */
public interface XYItemRenderer {

    /**
     * Initialises the renderer then returns the number of 'passes' through the data that the
     * renderer will require (usually just one).  This method will be called before the first
     * item is rendered, giving the renderer an opportunity to initialise any
     * state information it wants to maintain.  The renderer can do nothing if it chooses.
     *
     * @param g2  the graphics device.
     * @param dataArea  the area inside the axes.
     * @param plot  the plot.
     * @param data  the data.
     * @param info  an optional info collection object to return data back to the caller.
     *
     * @return The number of passes the renderer requires.
     */
    public int initialise(Graphics2D g2,
                          Rectangle2D dataArea,
                          XYPlot plot,
                          XYDataset data,
                          PlotRenderingInfo info);

    /**
     * Returns the tool tip generator for the renderer (possibly null).
     *
     * @return the tool tip generator.
     */
    public XYToolTipGenerator getToolTipGenerator();

    /**
     * Sets the tool tip generator for the renderer.
     *
     * @param toolTipGenerator  the tool tip generator (null permitted).
     */
    public void setToolTipGenerator(XYToolTipGenerator toolTipGenerator);

    /**
     * Returns the URL generator for HTML image maps.
     *
     * @return the URL generator (possibly null).
     */
    public XYURLGenerator getURLGenerator();

    /**
     * Sets the URL generator for HTML image maps.
     *
     * @param urlGenerator the URL generator (null permitted).
     */
    public void setURLGenerator(XYURLGenerator urlGenerator);

    /**
     * Adds a property change listener to the renderer.
     *
     * @param listener  the listener.
     */
    public void addPropertyChangeListener(PropertyChangeListener listener);

    /**
     * Removes a property change listener from the renderer.
     *
     * @param listener  the listener.
     */
    public void removePropertyChangeListener(PropertyChangeListener listener);


    /**
     * Returns the paint used to fill an item.
     *
     * @param series  the series index (zero-based).
     * @param item  the item index (zero-based).
     *
     * @return The paint.
     */
    public Paint getItemPaint(int series, int item);

    /**
     * Returns the paint used to fill items in a series.
     *
     * @param series  the series index (zero-based).
     *
     * @return The paint.
     */
    public Paint getSeriesPaint(int series);

    /**
     * Sets the paint for a series in the primary dataset.
     *
     * @param series  the series index (zero-based).
     * @param paint  the paint.
     */
    public void setSeriesPaint(int series, Paint paint);

    /**
     * Returns the paint used to outline an item.
     *
     * @param series  the series index (zero-based).
     * @param item  the item index (zero-based).
     *
     * @return The paint.
     */
    public Paint getItemOutlinePaint(int series, int item);

    /**
     * Returns the paint used to outline items in a series.
     *
     * @param series  the series index (zero-based).
     *
     * @return The paint.
     */
    public Paint getSeriesOutlinePaint(int series);

    // STROKE 
    
    /**
     * Returns the stroke for an item.
     *
     * @param series  the series index (zero-based).
     * @param item  the item index (zero-based).
     *
     * @return The stroke.
     */
    public Stroke getItemStroke(int series, int item);

    /**
     * Returns the stroke for a series.
     *
     * @param series  the series index (zero-based).
     *
     * @return The stroke.
     */
    public Stroke getSeriesStroke(int series);

    /**
     * Sets the stroke for ALL series (optional).
     *
     * @param stroke  the stroke.
     */
    public void setStroke(Stroke stroke);
    
    /**
     * Sets the stroke for a series in the primary dataset.
     *
     * @param series  the series index (zero-based).
     * @param stroke  the stroke.
     */
    public void setSeriesStroke(int series, Stroke stroke);

    /**
     * Returns the base stroke.
     *
     * @return The stroke.
     */
    public Stroke getBaseStroke();

    /**
     * Sets the base stroke.
     *
     * @param stroke  the stroke.
     */
    public void setBaseStroke(Stroke stroke);

    /**
     * Returns the shape for an item.
     *
     * @param series  the series index (zero-based).
     * @param item  the item index (zero-based).
     *
     * @return The shape.
     */
    public Shape getItemShape(int series, int item);

    /**
     * Returns the shape for a series.
     *
     * @param series  the series index (zero-based).

     *
     * @return The shape.
     */
    public Shape getSeriesShape(int series);

    /**
     * Called for each item to be plotted.
     * <p>
     * The {@link XYPlot} can make multiple passes through the dataset, depending on the value
     * returned by the renderer's {@link #initialise} method.
     *
     * @param g2  the graphics device.
     * @param dataArea  the area within which the data is being rendered.
     * @param info  collects drawing info.
     * @param plot  the plot (can be used to obtain standard color information etc).
     * @param domainAxis  the domain axis.
     * @param rangeAxis  the range axis.
     * @param dataset  the dataset.
     * @param series  the series index (zero-based).
     * @param item  the item index (zero-based).
     * @param crosshairInfo  collects information about crosshairs.
     * @param pass  the pass index.
     */
    public void drawItem(Graphics2D g2,
                         Rectangle2D dataArea,
                         PlotRenderingInfo info,
                         XYPlot plot,
                         ValueAxis domainAxis,
                         ValueAxis rangeAxis,
                         XYDataset dataset,
                         int series,
                         int item,
                         CrosshairInfo crosshairInfo,
                         int pass);

    /**
     * Returns a legend item for a series from a dataset.
     *
     * @param datasetIndex  the dataset index.
     * @param series  the series (zero-based index).
     *
     * @return the legend item.
     */
    public LegendItem getLegendItem(int datasetIndex, int series);

    /**
     * Fills a band between two values on the axis.  This can be used to color bands between the
     * grid lines.
     *
     * @param g2  the graphics device.
     * @param plot  the plot.
     * @param axis  the domain axis.
     * @param dataArea  the data area.
     * @param start  the start value.
     * @param end  the end value.
     */
    public void fillDomainGridBand(Graphics2D g2,
                                   XYPlot plot,
                                   ValueAxis axis,
                                   Rectangle2D dataArea,
                                   double start, double end);

    /**
     * Fills a band between two values on the range axis.  This can be used to color bands between
     * the grid lines.
     *
     * @param g2  the graphics device.
     * @param plot  the plot.
     * @param axis  the range axis.
     * @param dataArea  the data area.
     * @param start  the start value.
     * @param end  the end value.
     */
    public void fillRangeGridBand(Graphics2D g2,
                                  XYPlot plot,
                                  ValueAxis axis,
                                  Rectangle2D dataArea,
                                  double start, double end);

    /**
     * Draws a grid line against the domain axis.
     *
     * @param g2  the graphics device.
     * @param plot  the plot.
     * @param axis  the value axis.
     * @param dataArea  the area for plotting data (not yet adjusted for any 3D effect).
     * @param value  the value.
     */
    public void drawDomainGridLine(Graphics2D g2,
                                   XYPlot plot,
                                   ValueAxis axis,
                                   Rectangle2D dataArea,
                                   double value);

    /**
     * Draws a grid line against the range axis.
     *
     * @param g2  the graphics device.
     * @param plot  the plot.
     * @param axis  the value axis.
     * @param dataArea  the area for plotting data (not yet adjusted for any 3D effect).
     * @param value  the value.
     */
    public void drawRangeGridLine(Graphics2D g2,
                                  XYPlot plot,
                                  ValueAxis axis,
                                  Rectangle2D dataArea,
                                  double value);

    /**
     * Draws a vertical line on the chart to represent a 'range marker'.
     *
     * @param g2  the graphics device.
     * @param plot  the plot.
     * @param axis  the value axis.
     * @param marker  the marker line.
     * @param dataArea  the axis data area.
     */
    public void drawDomainMarker(Graphics2D g2,
                                 XYPlot plot,
                                 ValueAxis axis,
                                 Marker marker,
                                 Rectangle2D dataArea);

    /**
     * Draws a horizontal line across the chart to represent a 'range marker'.
     *
     * @param g2  the graphics device.
     * @param plot  the plot.
     * @param axis  the value axis.
     * @param marker  the marker line.
     * @param dataArea  the axis data area.
     */
    public void drawRangeMarker(Graphics2D g2,
                                XYPlot plot,
                                ValueAxis axis,
                                Marker marker,
                                Rectangle2D dataArea);

    /**
     * Returns the plot that this renderer has been assigned to.
     *
     * @return the plot.
     */
    public XYPlot getPlot();

    /**
     * Sets the plot that this renderer is assigned to.
     * <P>
     * This method will be called by the plot class...you do not need to call it yourself.
     *
     * @param plot  the plot.
     */
    public void setPlot(XYPlot plot);

    /**
     * Returns the range type for the renderer.  The plot needs to know this information in order
     * to determine an appropriate axis range (when the axis auto-range calculation is on).
     * <P>
     * Two types are recognised:
     * <ul>
     *   <li><code>STANDARD</code> - data items are plotted individually, so the axis range should
     *     extend from the smallest value to the largest value;</li>
     * <li><code>STACKED</code> - data items are stacked on top of one another, so to determine
     *     the axis range, all the items in a series need to be summed together.</li>
     * </ul>
     *
     * If the data values are stacked, this affects the axis range required to
     * display all the data items.
     *
     * @return a flag indicating whether or not the data values are stacked.
     */
    public RangeType getRangeType();

}
