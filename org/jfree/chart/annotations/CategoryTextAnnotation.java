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
 * ---------------------------
 * CategoryTextAnnotation.java
 * ---------------------------
 * (C) Copyright 2003 by Simba Management Limited.
 *
 * Original Author:  David Gilbert (for Simba Management Limited);
 * Contributor(s):   -;
 *
 * $Id$
 *
 * Changes:
 * --------
 * 02-Apr-2003 : Version 1 (DG);
 *
 */

package org.jfree.chart.annotations;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;

import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.data.CategoryDataset;

/**
 * A text annotation that can be placed on a {@link org.jfree.chart.plot.CategoryPlot}.
 *
 * @author David Gilbert
 */
public class CategoryTextAnnotation extends TextAnnotation 
                                    implements CategoryAnnotation, Serializable {

    /** The category. */
    private Comparable category;

    /** The value. */
    private double value;

    /**
     * Creates a new annotation to be displayed at the given location.
     *
     * @param text  the text.
     * @param category  the category.
     * @param value  the value.
     */
    public CategoryTextAnnotation(String text, Comparable category, double value) {
        this(text, TextAnnotation.DEFAULT_FONT, category, value);
    }

    /**
     * Creates a new annotation to be displayed at the given coordinates.
     *
     * @param text  the text.
     * @param font  the font.
     * @param category  the category.
     * @param value  the value.
     */
    public CategoryTextAnnotation(String text, Font font, Comparable category, double value) {
        this(text, font, TextAnnotation.DEFAULT_PAINT, category, value);
    }

    /**
     * Creates a new annotation to be displayed at the given coordinates.
     *
     * @param text  the text.
     * @param font  the font.
     * @param paint  the paint.
     * @param category  the category.
     * @param value  the value.
     */
    public CategoryTextAnnotation(String text, Font font, Paint paint, 
                                  Comparable category, double value) {
        super(text, font, paint);
        this.category = category;
        this.value = value;
    }

    /**
     * Draws the annotation.
     *
     * @param g2  the graphics device.
     * @param plot  the plot.
     * @param dataArea  the data area.
     * @param domainAxis  the domain axis.
     * @param rangeAxis  the range axis.
     */
    public void draw(Graphics2D g2, CategoryPlot plot, Rectangle2D dataArea,
                     CategoryAxis domainAxis, ValueAxis rangeAxis) {

        CategoryDataset dataset = plot.getCategoryDataset();
        int catIndex = dataset.getColumnIndex(this.category);
        int catCount = dataset.getColumnCount();
        
        float baseX = (float) domainAxis.getCategoryMiddle(catIndex, catCount, dataArea);
        float baseY = (float) rangeAxis.translateValueToJava2D(this.value, dataArea);

        g2.setFont(getFont());
        g2.setPaint(getPaint());
        g2.drawString(getText(), baseX, baseY);

    }

}
