/*
 * -------------------
 * OverlaidGridXYPlot.java
 * -------------------
 *
 * Original Author: Nitin Gupta and Vipin Gupta
 * Created: July 3,2002
 * version : 1.0
 * Contributor(s):
 *
 *
 */

package org.scec.gui.plot.jfreechart;
import org.jfree.data.Range;
import java.awt.Graphics2D;
import java.awt.geom.*;
import java.awt.*;

import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.axis.HorizontalNumberAxis;
import org.jfree.chart.axis.VerticalNumberAxis;
import org.jfree.chart.axis.HorizontalAxis;
import org.jfree.chart.axis.VerticalAxis;
import org.jfree.chart.CrosshairInfo;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.plot.OverlaidXYPlot;
import org.jfree.chart.axis.NumberTickUnit;

import java.text.DecimalFormat;

/**
 * An extension of XYPlot that allows multiple XYPlots to be overlaid in one space, using common
 * axes.
 *
 * @author Bill Kelemen (bill@kelemen-usa.com)
 */
public class OverlaidGeoXYPlot extends OverlaidXYPlot {

     /**
      * counter to track the number of times draw function is called
      * We save the middle of Y value the first time it is called
      * That Y valye is used for cosine transformation
      */
       private int counter = 0;
       double cosineY; // Y value for cosine function is calculated

         /* Constructs a new overlaid XY plot.  Number axes are created for the X and Y axes, using
         * the supplied labels.
         * <P>
         * After creating a new OverlaidXYPlot, you need to add some subplots.
         * <P>
         * No dataset is required, because each of the subplots maintains its own dataset.
         * <P>
         * This constructor is provided for convenience.  If you need greater control over the axes,
         * use another constructor.
         *
         * @param domainAxisLabel The label for the domain axis.
         * @param rangeAxisLabel The label for the range axis.
         */
        public OverlaidGeoXYPlot(String domainAxisLabel, String rangeAxisLabel) {

            super(domainAxisLabel,rangeAxisLabel);

        }

        /**
         * Constructs an OverlaidXYPlot.
         *
         * @param domain Horizontal axis to use for all sub-plots.
         * @param range Vertical axis to use for all sub-plots.
         */
        public OverlaidGeoXYPlot(ValueAxis domain, ValueAxis range) {
            super(domain, range);
        }


   /**
  * Draws the XY plot on a Java 2D graphics device (such as the screen or a printer).
  * <P>
  * XYPlot relies on an XYItemRenderer to draw each item in the plot.  This allows the visual
  * representation of the data to be changed easily.
  * <P>
  * The optional info argument collects information about the rendering of the plot (dimensions,
  * tooltip information etc).  Just pass in null if you do not need this information.
  *
  * @param g2 The graphics device.
  * @param plotArea The area within which the plot (including axis labels) should be drawn.
  * @param info Collects chart drawing information (null permitted).
  */
 public void draw(Graphics2D g2, Rectangle2D plotArea, ChartRenderingInfo info) {

   // set up info collection...
        if (info != null) {
            info.setPlotArea(plotArea);

        }

        // adjust the drawing area for plot insets (if any)...
        if (getInsets() != null) {
            plotArea.setRect(plotArea.getX() + getInsets().left,
                             plotArea.getY() + getInsets().top,
                             plotArea.getWidth() - getInsets().left - getInsets().right,
                             plotArea.getHeight() - getInsets().top - getInsets().bottom);
        }

        // estimate the area required for drawing the axes...
        double hAxisAreaHeight = 0;

        if (this.getDomainAxis() != null) {
            HorizontalAxis hAxis = (HorizontalAxis) this.getDomainAxis();
            hAxisAreaHeight = hAxis.reserveHeight(g2, this, plotArea, this.getDomainAxisLocation());
        }

        double vAxisWidth = 0;
        if (this.getRangeAxis() != null) {
          VerticalAxis vAxis = (VerticalAxis)getRangeAxis();
          vAxisWidth = vAxis.reserveWidth(g2, this, plotArea, getRangeAxisLocation(),
                hAxisAreaHeight,
                getDomainAxisLocation());
        }

        // ...and therefore what is left for the plot itself...
        Rectangle2D dataArea = new Rectangle2D.Double(plotArea.getX() + vAxisWidth,
                                                      plotArea.getY(),
                                                      plotArea.getWidth() - vAxisWidth,
                                                      plotArea.getHeight() - hAxisAreaHeight);

        if (info != null) {
            info.setDataArea(dataArea);
        }

        CrosshairInfo crosshairInfo = new CrosshairInfo();

        crosshairInfo.setCrosshairDistance(Double.POSITIVE_INFINITY);
        crosshairInfo.setAnchorX(getDomainAxis().getAnchorValue());
        crosshairInfo.setAnchorY(getRangeAxis().getAnchorValue());


        Range rh = getDomainAxis().getRange();
                Range rv=  getRangeAxis().getRange();
                HorizontalNumberAxis horz = (HorizontalNumberAxis)getDomainAxis();
                VerticalNumberAxis vert = (VerticalNumberAxis)getRangeAxis();
                ++counter;
                if(counter == 1)
                  cosineY= Math.toRadians((rv.getLowerBound()+rv.getUpperBound())/2);
                /*
                Following code has been added to make the Longitude the cos function of the latitude
                Converting to radians because java finds the cos of the radians.
                What we are doing is scaling the horizontal longitude line based on the cos function of the latitude
                */
                 //double verticaldiff = ((dataArea.getMaxY()-dataArea.getMinY())/(rv.getUpperBound()-rv.getLowerBound())) * Math.abs(Math.cos(cosineY));
                double verticaldiff = ((dataArea.getMaxY()-dataArea.getMinY())/(rv.getUpperBound()-rv.getLowerBound()));
                double horizontaldiff = (dataArea.getMaxX()-dataArea.getMinX())/(rh.getUpperBound()-rh.getLowerBound());
                double upperh = (dataArea.getMaxX()-dataArea.getMinX())/verticaldiff +rh.getLowerBound();
                if(upperh >= rh.getUpperBound())  {// adjust the horizontal scale
                  getDomainAxis().setRange(rh.getLowerBound(), upperh);
                  //horz.setTickUnit(new NumberTickUnit(0.71*vert.getTickUnit().getSize(), new DecimalFormat("0.000")));
                }
                else {
                  // adjust the vertical scale according to horizontal scale
                 // double upperv=(dataArea.getMaxY()-dataArea.getMinY())*Math.abs(Math.cos(cosineY))/horizontaldiff + rv.getLowerBound();
                  double upperv=(dataArea.getMaxY()-dataArea.getMinY())/horizontaldiff + rv.getLowerBound();
                  getRangeAxis().setRange(rv.getLowerBound(),upperv);
                  //vert.setTickUnit(new NumberTickUnit(1/0.72*horz.getTickUnit().getSize(), new DecimalFormat("0.000")));
         }



        // draw the plot background and axes...
        drawBackground(g2, dataArea);


        if (this.getDomainAxis() != null) {
            this.getDomainAxis().draw(g2, plotArea, dataArea, this.getDomainAxisLocation());
        }

        if (this.getRangeAxis() != null) {
            this.getRangeAxis().draw(g2, plotArea, dataArea, this.getRangeAxisLocation());
        }

        if (this.getRenderer() != null) {
            Shape originalClip = g2.getClip();
            Composite originalComposite = g2.getComposite();

            g2.clip(dataArea);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
                                                       this.getForegroundAlpha()));
            render(g2, dataArea, info, crosshairInfo);
            g2.setClip(originalClip);
            g2.setComposite(originalComposite);
        }

    }

}