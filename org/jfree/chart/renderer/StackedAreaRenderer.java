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
 * ------------------------
 * StackedAreaRenderer.java
 * ------------------------
 * (C) Copyright 2002, 2003 by Dan Rivett (d.rivett@ukonline.co.uk) and Contributors.
 *
 * Original Author:  Dan Rivett (adapted from AreaCategoryItemRenderer);
 * Contributor(s):   Jon Iles;
 *                   David Gilbert (for Simba Management Limited);
 *
 * $Id$
 *
 * Changes:
 * --------
 * 20-Sep-2002 : Version 1, contributed by Dan Rivett;
 * 24-Oct-2002 : Amendments for changes in CategoryDataset interface and CategoryToolTipGenerator
 *               interface (DG);
 * 01-Nov-2002 : Added tooltips (DG);
 * 06-Nov-2002 : Renamed drawCategoryItem(...) --> drawItem(...) and now using axis for
 *               category spacing.
 *               Renamed StackedAreaCategoryItemRenderer --> StackedAreaRenderer (DG);
 * 26-Nov-2002 : Switched CategoryDataset --> TableDataset (DG);
 * 26-Nov-2002 : Replaced isStacked() method with getRangeType() method (DG);
 * 17-Jan-2003 : Moved plot classes to a separate package (DG);
 * 25-Mar-2003 : Implemented Serializable (DG);
 *
 */

package org.jfree.chart.renderer;

import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;

import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.CategoryItemEntity;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.tooltips.CategoryToolTipGenerator;
import org.jfree.chart.urls.CategoryURLGenerator;
import org.jfree.data.CategoryDataset;

/**
 * A category item renderer that draws stacked area charts. 
 * <p>
 * For use with the {@link org.jfree.chart.plot.VerticalCategoryPlot} class.
 *
 * @author Dan Rivett
 */
public class StackedAreaRenderer extends AreaRenderer implements Serializable {

    /**
     * Creates a new renderer.
     */
    public StackedAreaRenderer() {
        this(null, null);
    }

    /**
     * Creates a new renderer.
     *
     * @param toolTipGenerator  the tool tip generator (<code>null</code> permitted).
     * @param urlGenerator  the URL generator (<code>null</code> permitted).
     */
    public StackedAreaRenderer(CategoryToolTipGenerator toolTipGenerator,
                               CategoryURLGenerator urlGenerator) {

        super(toolTipGenerator, urlGenerator);

    }

    /**
     * Returns the range type.
     *
     * @return the range type.
     */
    public int getRangeType() {
        return CategoryItemRenderer.STACKED;
    }

    /**
     * Draw a single data item.
     *
     * @param g2  the graphics device.
     * @param dataArea  the data plot area.
     * @param plot  the plot.
     * @param domainAxis  the domain axis.
     * @param rangeAxis  the range axis.
     * @param data  the data.
     * @param dataset  the dataset index (zero-based).
     * @param row  the row index (zero-based).
     * @param column  the column index (zero-based).
     */
    public void drawItem(Graphics2D g2,
                         Rectangle2D dataArea,
                         CategoryPlot plot,
                         CategoryAxis domainAxis,
                         ValueAxis rangeAxis,
                         CategoryDataset data,
                         int dataset,
                         int row,
                         int column) {

        // plot non-null values...
        Number value = data.getValue(row, column);
        if (value != null) {
            // leave the y values (y1, y0) untranslated as it is going to be be stacked
            // up later by previous series values, after this it will be translated.
            double x1 = domainAxis.getCategoryMiddle(column, getColumnCount(), dataArea);
            double y1 = 0.0;  // calculate later
            double y1Untranslated = value.doubleValue();

            g2.setPaint(getItemPaint(dataset, row, column));
            g2.setStroke(getItemStroke(dataset, row, column));

            if (column != 0) {

                Number previousValue = data.getValue(row, column - 1);
                if (previousValue != null) {

                    double x0 = domainAxis.getCategoryMiddle(column - 1,
                                                             getColumnCount(), dataArea);
                    double y0Untranslated = previousValue.doubleValue();

                    // Get the previous height, but this will be different for both y0 and y1 as
                    // the previous series values could differ.
                    double previousHeightx0Untranslated = getPreviousHeight(data, row, column - 1);
                    double previousHeightx1Untranslated = getPreviousHeight(data, row, column);

                    // Now stack the current y values on top of the previous values.
                    y0Untranslated += previousHeightx0Untranslated;
                    y1Untranslated += previousHeightx1Untranslated;

                    // Now translate the previous heights
                    double previousHeightx0
                        = rangeAxis.translateValueToJava2D(previousHeightx0Untranslated, dataArea);
                    double previousHeightx1
                        = rangeAxis.translateValueToJava2D(previousHeightx1Untranslated, dataArea);

                    // Now translate the current y values.
                    double y0 = rangeAxis.translateValueToJava2D(y0Untranslated, dataArea);
                    y1 = rangeAxis.translateValueToJava2D(y1Untranslated, dataArea);

                    // create the Polygon of these stacked, translated values.
                    Polygon p = new Polygon();
                    p.addPoint((int) x0, (int) y0);
                    p.addPoint((int) x1, (int) y1);
                    p.addPoint((int) x1, (int) previousHeightx1);
                    p.addPoint((int) x0, (int) previousHeightx0);

                    g2.setPaint(getItemPaint(dataset, row, column));
                    g2.setStroke(getItemStroke(dataset, row, column));
                    g2.fill(p);
                }

            }

            // collect entity and tool tip information...
            if (getInfo() != null) {
                EntityCollection entities = getInfo().getEntityCollection();
                Shape shape = new Rectangle2D.Double(x1 - 3.0, y1 - 3.0, 6.0, 6.0);
                if (entities != null && shape != null) {
                    String tip = null;
                    if (getToolTipGenerator() != null) {
                        tip = getToolTipGenerator().generateToolTip(data, row, column);
                    }
                    String url = null;
                    if (getURLGenerator() != null) {
                        url = getURLGenerator().generateURL(data, row, column);
                    }
                    CategoryItemEntity entity = new CategoryItemEntity(shape, tip, url, row, 
                                                        data.getColumnKey(column), column);
                    entities.addEntity(entity);
                }
            }

        }
    }

    /**
     * Calculates the stacked value of the all series up to, but not including <code>series</code>
     * for the specified category, <code>category</code>.  It returns 0.0 if <code>series</code>
     * is the first series, i.e. 0.
     *
     * @param data  the data.
     * @param series  the series.
     * @param category  the category.
     *
     * @return double returns a cumulative value for all series' values up to but excluding
     *                <code>series</code> for Object <code>category</code>.
     */
    protected double getPreviousHeight(CategoryDataset data, int series, int category) {

        double result = 0.0;

        Number tmp;
        for (int i = 0; i < series; i++) {
            tmp = data.getValue(i, category);
            if (tmp != null) {
                result += tmp.doubleValue();
            }
        }

        return result;

    }

}
