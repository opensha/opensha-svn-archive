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
 * --------------------------------------
 * HorizontalLogarithmicColorBarAxis.java
 * --------------------------------------
 * (C) Copyright 2002, 2003, by David M. O'Donnell and Contributors.
 *
 * Original Author:  David M. O'Donnell;
 * Contributor(s):   David Gilbert (for Simba Management Limited);
 *
 * $Id$
 *
 * Changes
 * -------
 * 26-Nov-2002 : Version 1 contributed by David M. O'Donnell (DG);
 * 14-Jan-2003 : Changed autoRangeMinimumSize from Number --> double (DG);
 * 26-Mar-2003 : Implemented Serializable (DG);
 *
 */

package org.jfree.chart.axis;

import java.awt.BasicStroke;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import java.util.Iterator;

import org.jfree.chart.plot.ContourValuePlot;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.ui.ColorPalette;
import org.jfree.chart.ui.RainbowPalette;
import org.jfree.data.Range;
import org.jfree.ui.RefineryUtilities;

/**
 * A horizontal logarithmic ColorBar.  This class extends the HorizontalLogarithmicAxis to provide
 * a) tickmarks, b) ticklabels, c) axis label, d) mapping between java2D and plot units.
 *
 * @author David M. O'Donnell
 */
public class HorizontalLogarithmicColorBarAxis extends HorizontalLogarithmicAxis
                                               implements ColorBarAxis, Serializable {

    /** The default color bar thickness. */
    public static final int DEFAULT_COLORBAR_THICKNESS = 0;

    /** The default color bar thickness as a percentage. */
    public static final double DEFAULT_COLORBAR_THICKNESS_PCT = 0.05;

    /** The default outer gap. */
    public static final int DEFAULT_OUTERGAP = 20;

    /** The color palette. */
    private ColorPalette colorPalette = null;

    /** The color bar length. */
    private int colorBarLength = 0; // default make width of plotArea

    /** The color bar thickness. */
    private int colorBarThickness = DEFAULT_COLORBAR_THICKNESS;

    /** The color bar thickness as a percentage of the data area height. */
    private double colorBarThicknessPercent = DEFAULT_COLORBAR_THICKNESS_PCT;

    /** The amount of blank space around the colorbar. */
    private int outerGap;

    /**
     * Constructor for HorizontalLogarithmicColorBarAxis.
     *
     * @param label  the axis label.
     */
    public HorizontalLogarithmicColorBarAxis(String label) {

        super(label);
        this.colorPalette = new RainbowPalette();
        getColorPalette().setLogscale(true);
        this.colorBarThickness = DEFAULT_COLORBAR_THICKNESS;
        this.colorBarThicknessPercent = DEFAULT_COLORBAR_THICKNESS_PCT;
        this.outerGap = DEFAULT_OUTERGAP;
        this.colorPalette.setMinZ(getRange().getLowerBound());
        this.colorPalette.setMaxZ(getRange().getUpperBound());
        this.setLowerMargin(0.0);
        this.setUpperMargin(0.0);

    }

    /**
     * Draws the plot on a Java 2D graphics device (such as the screen or a printer).
     *
     * @param g2  the graphics device.
     * @param drawArea  the area within which the chart should be drawn.
     * @param dataArea  the area within which the plot should be drawn (a
     *                  subset of the drawArea).
     */
    public void draw(Graphics2D g2, Rectangle2D drawArea, Rectangle2D dataArea, int location) {

        // draw colorBar rectangle
        double length = dataArea.getWidth();
        if (colorBarLength > 0) {
            length = this.colorBarLength;
        }

        double thickness = colorBarThicknessPercent * dataArea.getHeight(); // plot height
        if (this.colorBarThickness > 0) {
            thickness = colorBarThickness;  //allow fixed thickness
        }

        Rectangle2D colorBarArea = new Rectangle2D.Double(dataArea.getX(),
                                                          dataArea.getMaxY() + outerGap,
                                                          length, thickness);

        // update, but dont draw tick marks (needed for stepped colors)
        refreshTicks(g2, drawArea, colorBarArea, Axis.BOTTOM);

        drawColorBar(g2, colorBarArea);

        // draw the tick labels and marks

        double yy = colorBarArea.getMaxX();

        double maxY = (float) colorBarArea.getMaxY();
        g2.setFont(getTickLabelFont());

        Iterator iterator = getTicks().iterator();
        while (iterator.hasNext()) {
            Tick tick = (Tick) iterator.next();
            double xx = translateValueToJava2D(tick.getNumericalValue(), colorBarArea);
            if (isTickLabelsVisible()) {
                Font labelFont = getLabelFont();
                g2.setPaint(getTickLabelPaint());
                String label = getLabel();
                Rectangle2D labelBounds
                    = labelFont.getStringBounds(label, g2.getFontRenderContext());
                if (isVerticalTickLabels()) {
                    xx = drawArea.getX() + getLabelInsets().left + labelBounds.getHeight();
                    yy = dataArea.getY() + dataArea.getHeight() / 2
                                         + labelBounds.getWidth() / 2;
                    RefineryUtilities.drawRotatedString(label, g2,
                                                        (float) xx, (float) yy, -Math.PI / 2);
                }
                else {
                    g2.drawString(tick.getText(), tick.getX(), tick.getY());
                }
            }
            if (isTickMarksVisible()) {
                g2.setStroke(this.getTickMarkStroke());
                Line2D mark = new Line2D.Double(xx, maxY - 2, xx, maxY + 2);
                g2.draw(mark);
            }

        }
        // draw the axis label...
        String label = getLabel();
        if (label != null) {
            double yTickMax = getMaxTickLabelHeight(g2, colorBarArea, isVerticalTickLabels());
            Font labelFont = getLabelFont();
            g2.setFont(labelFont);
            g2.setPaint(getLabelPaint());
            FontRenderContext frc = g2.getFontRenderContext();
            Rectangle2D labelBounds = labelFont.getStringBounds(label, frc);
            LineMetrics lm = labelFont.getLineMetrics(label, frc);
            float labelx = (float) (colorBarArea.getX() + colorBarArea.getWidth() / 2
                                                        - labelBounds.getWidth() / 2);
            float labely = (float) (yTickMax + colorBarArea.getMaxY()
                                             + getLabelInsets().bottom + 20
                                             + lm.getDescent()
                                             + lm.getLeading());
            g2.drawString(label, labelx, labely);
        }

    }

    /**
     * Draws the plot on a Java 2D graphics device (such as the screen or a printer).
     *
     * @param g2  the graphics device.
     * @param colorBarArea  the area within which the axis should be drawn.
     */
    public void drawColorBar(Graphics2D g2, Rectangle2D colorBarArea) {

        Object antiAlias = g2.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_OFF);

        colorPalette.setTickValues(getTicks()); // setTickValues was missing from ColorPalette v. 0.96

        Stroke strokeSaved = g2.getStroke();
        g2.setStroke(new BasicStroke(1.0f));

        double y1 = colorBarArea.getY();
        double y2 = colorBarArea.getMaxY();
        double xx = colorBarArea.getX();
        Line2D line = new Line2D.Double();
        while (xx <= colorBarArea.getMaxX()) {
            double value = this.translateJava2DtoValue((float) xx, colorBarArea);
            line.setLine(xx, y1, xx, y2);
            g2.setPaint(getPaint(value));
            g2.draw(line);
            xx += 1;
        }
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, antiAlias);
        g2.setStroke(strokeSaved);
    }


    /**
     * Returns the current ColorPalette.
     *
     * @return the palette.
     */
    public ColorPalette getColorPalette() {
        return colorPalette;
    }

    /**
     * A utility method for determining the height of the tallest tick label.
     *
     * @param g2  the graphics device.
     * @param drawArea  the area within which the plot and axes should be drawn.
     * @param vertical  a flag that indicates whether or not the tick labels are 'vertical'.
     *
     * @return the height.
     */
    private double getMaxTickLabelHeight(Graphics2D g2, Rectangle2D drawArea, boolean vertical) {

        Font font = getTickLabelFont();
        g2.setFont(font);
        FontRenderContext frc = g2.getFontRenderContext();
        double maxHeight = 0.0;
        if (vertical) {
            Iterator iterator = getTicks().iterator();
            while (iterator.hasNext()) {
                Tick tick = (Tick) iterator.next();
                Rectangle2D labelBounds = font.getStringBounds(tick.getText(), frc);
                if (labelBounds.getWidth() > maxHeight) {
                    maxHeight = labelBounds.getWidth();
                }
            }
        }
        else {
            LineMetrics metrics = font.getLineMetrics("Sample", frc);
            maxHeight = metrics.getHeight();
        }
        return maxHeight;
    }

    /**
     * Returns the Paint associated with the value.
     *
     * @param value  the value.
     *
     * @return the paint.
     */
    public Paint getPaint(double value) {
        return colorPalette.getPaint(value);
    }

    /**
     * Returns true if a plot is compatible with the axis, and false otherwise.
     * <P>
     * For this axis, the requirement is that the plot implements the ContourValuePlot interface.
     *
     * @param plot  the plot.
     *
     * @return boolean.
     */
    protected boolean isCompatiblePlot(Plot plot) {
        return (plot instanceof ContourValuePlot);

    }

    /**
     * Sets the current ColorPalette.
     *
     * @param palette  the palette.
     */
    public void setColorPalette(ColorPalette palette) {
        this.colorPalette = palette;
    }

    /**
     * Sets the maximum axis value.
     *
     * @param value  the value.
     */
    public void setMaximumAxisValue(double value) {
        this.colorPalette.setMaxZ(value);
        super.setMaximumAxisValue(value);
    }

    /**
     * Sets the minimum axis value.
     *
     * @param value  the value.
     */
    public void setMinimumAxisValue(double value) {
        this.colorPalette.setMinZ(value);
        super.setMinimumAxisValue(value);
    }

    /**
     * Rescales the axis to ensure that all data is visible.
     */
    public void autoAdjustRange() {

        Plot plot = getPlot();
        if (plot instanceof ContourValuePlot) {
            ContourValuePlot cvp = (ContourValuePlot) plot;

            Range r = cvp.getContourDataRange();
            if (r == null) {
              r = new Range(DEFAULT_LOWER_BOUND, DEFAULT_UPPER_BOUND);
            }

            double lower = computeLogFloor(r.getLowerBound());
            if (!this.getAllowNegativesFlag() && lower >= 0.0 && lower < SMALL_LOG_VALUE) {
                //negatives not allowed and lower range bound is zero
                lower = r.getLowerBound();    //use data range bound instead
            }

            double upper = r.getUpperBound();

            if (!this.getAllowNegativesFlag() && upper < 1.0 && upper > 0.0 && lower > 0.0) {
                  //negatives not allowed and upper bound between 0 & 1
                        //round up to nearest significant digit for bound:
                                                 //get negative exponent:
              double expVal = Math.log(upper) / LOG10_VALUE;
              expVal = Math.ceil(-expVal + 0.001); //get positive exponent
              expVal = Math.pow(10, expVal);      //create multiplier value
                        //multiply, round up, and divide for bound value:
              upper = (expVal > 0.0) ? Math.ceil(upper * expVal) / expVal : Math.ceil(upper);
            }
            else {  //negatives allowed or upper bound not between 0 & 1
              upper = Math.ceil(upper);     //use nearest integer value
            }
            // ensure the autorange is at least <minRange> in size...
            double minRange = getAutoRangeMinimumSize();
            if (upper - lower < minRange) {
              upper = (upper + lower + minRange) / 2;
              lower = (upper + lower - minRange) / 2;
            }

            setRangeAttribute(new Range(lower, upper));

            setupSmallLogFlag();      //setup flag based on bounds values

            this.colorPalette.setMinZ(lower);
            this.colorPalette.setMaxZ(upper);
        }

    }

    /**
     * Returns the height required to draw the axis in the specified draw area.
     *
     * @param g2  the graphics device.
     * @param plot  the plot that the axis belongs to.
     * @param drawArea  the area within which the plot should be drawn.
     *
     * @return the height required to draw the axis in the specified draw area.
     */
    public double reserveHeight(Graphics2D g2, Plot plot, Rectangle2D drawArea, int location) {
        //get the height from horizontal number axis
        double ht = super.reserveHeight(g2, plot, drawArea, location);
        //add the colorbar thickness and gap
        return ht + drawArea.getHeight() * colorBarThicknessPercent + outerGap;
    }

    /**
     * Returns area in which the axis will be displayed.
     *
     * @param g2  the graphics device.
     * @param plot  a reference to the plot.
     * @param drawArea  the area within which the plot and axes should be drawn.
     * @param location  the axis location.
     * @param reservedWidth  the space already reserved for the vertical axis.
     * @param verticalAxisLocation  the vertical axis location.
     *
     * @return area in which the axis will be displayed.
     */
    public double reserveHeight(Graphics2D g2, Plot plot,
                                Rectangle2D drawArea, int location,
                                double reservedWidth, int verticalAxisLocation) {

        double result = super.reserveHeight(g2, plot, drawArea, location, reservedWidth, Axis.LEFT);
        result = result + drawArea.getHeight() * colorBarThicknessPercent + outerGap;
        return result;

    }
    
    /**
     * This is cheat to make autoAdjustRange public.
     */
	public void doAutoRange() {
		autoAdjustRange();
	}

}
