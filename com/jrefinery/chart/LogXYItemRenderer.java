/* =======================================
 * JFreeChart : a Java Chart Class Library
 * =======================================
 *
 * Project Info:  http://www.object-refinery.com/jfreechart/index.html
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
 * ---------------------------
 * LogXYItemRenderer.java
 * ---------------------------
 *
 *Author : Nitin Gupta and Vipin Gupta
 *
 *
 * $Id$
 *
 **/

package com.jrefinery.chart;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.Image;
import java.awt.Point;
import java.awt.image.ImageObserver;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.AffineTransform;
import com.jrefinery.data.XYDataset;
import com.jrefinery.chart.entity.EntityCollection;
import com.jrefinery.chart.entity.XYItemEntity;
import com.jrefinery.chart.tooltips.XYToolTipGenerator;
import com.jrefinery.chart.tooltips.StandardXYToolTipGenerator;

/**
 * Standard item renderer for an XYPlot.  This class can draw (a) shapes at each point, or (b) lines
 * between points, or (c) both shapes and lines.
 */
public class LogXYItemRenderer extends StandardXYItemRenderer{


    /**
     * Constructs a new renderer.
     */
    public LogXYItemRenderer() {

        super(LINES, new StandardXYToolTipGenerator());

    }

    /**
     * Constructs a new renderer.
     * <p>
     * To specify the type of renderer, use one of the constants: SHAPES, LINES or SHAPES_AND_LINES.
     *
     * @param type The type of renderer.
     * @param toolTipGenerator The tooltip generator.
     */
    public LogXYItemRenderer(int type, XYToolTipGenerator toolTipGenerator) {
        super(type,toolTipGenerator);
    }


    /**
     * Draws the visual representation of a single data item.
     *
     * @param g2 The graphics device.
     * @param dataArea The area within which the data is being drawn.
     * @param info Collects information about the drawing.
     * @param plot The plot (can be used to obtain standard color information etc).
     * @param horizontalAxis The horizontal axis.
     * @param verticalAxis The vertical axis.
     * @param data The dataset.
     * @param series The series index.
     * @param item The item index.
     */
    public void drawItem(Graphics2D g2, Rectangle2D dataArea, ChartRenderingInfo info,
                         XYPlot plot, ValueAxis horizontalAxis, ValueAxis verticalAxis,
                         XYDataset data, int series, int item,
                         CrosshairInfo crosshairInfo, boolean xlog, boolean ylog) throws java.lang.ArithmeticException {

        // setup for collecting optional entity info...
        Shape entityArea = null;
        EntityCollection entities = null;
        if (info!=null) {
            entities = info.getEntityCollection();
        }

        Paint seriesPaint = plot.getSeriesPaint(series);
        Stroke seriesStroke = plot.getSeriesStroke(series);
        g2.setPaint(seriesPaint);
        g2.setStroke(seriesStroke);

        // get the data point...
        Number x1 = data.getXValue(series, item);
        Number y1 = data.getYValue(series, item);


        if (y1!=null) {
            double valueX = x1.doubleValue();
            double valueY = y1.doubleValue();
            double transX1 = 0.0,transY1=0.0;
            boolean foundX=true,foundY=true;

            if(valueX<=0.0 && xlog){
              throw new java.lang.ArithmeticException("Log Value of the negative values and 0 does not exist");

            }
            if(valueY<=0.0 && ylog){
                throw new java.lang.ArithmeticException("Log Value of the negative values and 0 does not exist");

            }

            if(xlog && valueX !=0.0){
               valueX=Math.log(valueX)/Math.log(10);
            }

            if(ylog && valueY!=0.0) valueY=Math.log(valueY)/Math.log(10);
            if(xlog && foundX) transX1 = ((HorizontalLogarithmicAxis)horizontalAxis).myTranslateValueToJava2D(valueX, dataArea);
            if(ylog && foundY) transY1 = ((VerticalLogarithmicAxis)verticalAxis).myTranslateValueToJava2D(valueY, dataArea);
            if(!xlog) transX1 = horizontalAxis.translateValueToJava2D(valueX, dataArea);
            if(!ylog)transY1 = verticalAxis.translateValueToJava2D(valueY, dataArea);

            Paint paint = getPaint(plot, series, item, transX1, transY1);
            if (paint != null) {
              g2.setPaint(paint);
            }
            if (this.plotLines) {
                if (item>0) {
                    // get the previous data point...
                    Number x0 = data.getXValue(series, item-1);
                    Number y0 = data.getYValue(series, item-1);
                    if (y0!=null) {

                        double valueX0=x0.doubleValue();
                        double valueY0=y0.doubleValue();
                        double transX0=0.0,transY0=0.0;
                        boolean foundX0=true,foundY0=true;
                        if(valueX0<=0.0 && xlog) {
                           throw new java.lang.ArithmeticException("Log Value of the negative values and 0 does not exist for X-Log Plot");

                        }
                        if(valueY0<=0.0 && ylog) {
                           throw new java.lang.ArithmeticException("Log Value of the negative values and 0 does not exist for Y-Log Plot");

                        }
                        if (xlog && valueX0!=0.0) valueX0=Math.log(valueX0)/Math.log(10);
                        if(ylog && valueY0!=0.0) valueY0=Math.log(valueY0)/Math.log(10);

                        if(xlog && foundX0) transX0 = ((HorizontalLogarithmicAxis)horizontalAxis).myTranslateValueToJava2D(valueX0, dataArea);
                        if(ylog && foundY0) transY0 = ((VerticalLogarithmicAxis)verticalAxis).myTranslateValueToJava2D(valueY0, dataArea);
                        if(!xlog) transX0 = horizontalAxis.translateValueToJava2D(valueX0, dataArea);
                        if(!ylog) transY0 = verticalAxis.translateValueToJava2D(valueY0, dataArea);
                        line.setLine(transX0, transY0, transX1, transY1);
                        if (line.intersects(dataArea)) {
                            g2.draw(line);
                        }
                    }
                }
            }

            if (this.plotShapes) {

                shapeScale = getShapeScale(plot, series, item, transX1, transY1);
                Shape shape = getShape(plot, series, item, transX1, transY1, shapeScale);
                if (isShapeFilled(plot, series, item, transX1, transY1)) {
                    if (shape.intersects(dataArea)) g2.fill(shape);
                } else {
                    if (shape.intersects(dataArea)) g2.draw(shape);
                }
                entityArea = shape;

            }

            if (this.plotImages) {
                // use shape scale with transform??
                shapeScale = getShapeScale(plot, series, item, transX1, transY1);
                Image image = getImage(plot, series, item, transX1, transY1);
                if (image != null) {
                    Point hotspot = getImageHotspot(plot, series, item, transX1, transY1, image);
                    g2.drawImage(image,(int)(transX1-hotspot.getX()),(int)(transY1-hotspot.getY()),(ImageObserver)null);
                }
                // tooltipArea = image; not sure how to handle this yet
            }

            // add an entity for the item...
            if (entities!=null) {
                if (entityArea==null) {
                    entityArea = new Rectangle2D.Double(transX1-2, transY1-2, 4, 4);
                }
                String tip = "";
                if (this.toolTipGenerator!=null) {
                    tip = this.toolTipGenerator.generateToolTip(data, series, item);
                }
                XYItemEntity entity = new XYItemEntity(entityArea, tip, series, item);
                entities.addEntity(entity);
            }

            // do we need to update the crosshair values?
            double distance = 0.0;
            if (horizontalAxis.isCrosshairLockedOnData()) {
                if (verticalAxis.isCrosshairLockedOnData()) {
                    // both axes
                    crosshairInfo.updateCrosshairPoint(x1.doubleValue(), y1.doubleValue());
                }
                else {
                    // just the horizontal axis...
                    crosshairInfo.updateCrosshairX(x1.doubleValue());
                }
            }
            else {
                if (verticalAxis.isCrosshairLockedOnData()) {
                    // just the vertical axis...
                    crosshairInfo.updateCrosshairY(y1.doubleValue());
                }
            }
        }

    }

}
