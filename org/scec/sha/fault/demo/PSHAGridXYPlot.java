package org.scec.sha.fault.demo;

import com.jrefinery.chart.*;
import com.jrefinery.chart.event.*;
import com.jrefinery.chart.tooltips.*;
import com.jrefinery.data.*;
import org.scec.gui.PSHAXYPlot;
import java.awt.*;
import java.awt.geom.*;
import java.util.*;

import java.math.BigDecimal;
import javax.swing.*;

/**
 *  Title: Description: Copyright: Copyright (c) 2001 Company:
 *
 * @author
 * @created    February 22, 2002
 * @version    1.0
 */

public class PSHAGridXYPlot
         extends PSHAXYPlot{


    protected final static String C = "PSHAGridXYPlot";
    protected final static boolean D = true;


    /**
     * Constructs an XYPlot with the specified axes (other attributes take default values).
     *
     * @param domainAxis The domain axis.
     * @param rangeAxis The range axis.
     */
    public PSHAGridXYPlot(XYDataset data, ValueAxis domainAxis, ValueAxis rangeAxis) {
        super(data, domainAxis, rangeAxis,false,false);
    }

    /**
     * Constructs an XYPlot with the specified axes and renderer (other attributes take default
     * values).
     *
     * @param domainAxis The domain axis.
     * @param rangeAxis The range axis.
     * @param renderer The renderer
     */
    public PSHAGridXYPlot(XYDataset data,
                  ValueAxis domainAxis, ValueAxis rangeAxis, XYItemRenderer renderer) {
        super(data, domainAxis, rangeAxis, renderer);
    }

    /**
     * Constructs a new XY plot.
     *
     * @param domainAxis The domain axis.
     * @param rangeAxis The range axis.
     * @param insets Amount of blank space around the plot area.
     * @param backgroundPaint An optional color for the plot's background.
     * @param backgroundImage An optional image for the plot's background.
     * @param backgroundAlpha Alpha-transparency for the plot's background.
     * @param outlineStroke The Stroke used to draw an outline around the plot.
     * @param outlinePaint The color used to draw the plot outline.
     * @param alpha The alpha-transparency.
     * @param renderer The renderer.
     */
    public PSHAGridXYPlot(XYDataset data,
                  ValueAxis domainAxis, ValueAxis rangeAxis,
                  Insets insets,
                  Paint backgroundPaint, Image backgroundImage, float backgroundAlpha,
                  Stroke outlineStroke, Paint outlinePaint, float alpha,
                  XYItemRenderer renderer) {

        super(data, domainAxis, rangeAxis, insets,
            backgroundPaint, backgroundImage, backgroundAlpha,
            outlineStroke, outlinePaint, alpha, renderer
        );

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
         if (info!=null) {
             info.setPlotArea(plotArea);

         }

         // adjust the drawing area for plot insets (if any)...
         if (insets!=null) {
             plotArea.setRect(plotArea.getX()+insets.left,
                              plotArea.getY()+insets.top,
                              plotArea.getWidth()-insets.left-insets.right,
                              plotArea.getHeight()-insets.top-insets.bottom);
         }

         // estimate the area required for drawing the axes...
         double hAxisAreaHeight = 0;

         if (this.domainAxis!=null) {
             HorizontalAxis hAxis = (HorizontalAxis)this.domainAxis;
             hAxisAreaHeight = hAxis.reserveHeight(g2, this, plotArea);
         }

         double vAxisWidth = 0;
         if (this.rangeAxis!=null) {
             VerticalAxis vAxis = (VerticalAxis)this.rangeAxis;
             vAxisWidth = vAxis.reserveAxisArea(g2, this, plotArea, hAxisAreaHeight).getWidth();
         }

         // ...and therefore what is left for the plot itself...
         Rectangle2D dataArea = new Rectangle2D.Double(plotArea.getX()+vAxisWidth,
                                                       plotArea.getY(),
                                                       plotArea.getWidth()-vAxisWidth,
                                                       plotArea.getHeight()-hAxisAreaHeight);

         if (info!=null) {
             info.setDataArea(dataArea);
         }

         CrosshairInfo crosshairInfo = new CrosshairInfo();

         crosshairInfo.setCrosshairDistance(Double.POSITIVE_INFINITY);
         crosshairInfo.setAnchorX(this.getDomainAxis().getAnchorValue());
         crosshairInfo.setAnchorY(this.getRangeAxis().getAnchorValue());


        Range rh = this.domainAxis.getRange();
        Range rv=  this.rangeAxis.getRange();
        /*
         Following code has been added to make the Longitude the cos function of the latitude
         Converting to radians because java finds the cos of the radians.
         What we are doing is scaling the horizontal longitude line based on the cos function of the latitude
        */
        double verticaldiff = ((dataArea.getMaxY()-dataArea.getMinY())/(rv.getUpperBound()-rv.getLowerBound())) * Math.abs(Math.cos(Math.toRadians((rv.getLowerBound()+rv.getUpperBound())/2)));
        double upperh= (dataArea.getMaxX()-dataArea.getMinX())/verticaldiff +rh.getLowerBound();

        domainAxis.setRange(rh.getLowerBound(), upperh);

        drawOutlineAndBackground(g2, dataArea);
        if (this.domainAxis!=null) {
           this.domainAxis.draw(g2, plotArea, dataArea);
        }
        if (this.rangeAxis!=null) {
           this.rangeAxis.draw(g2, plotArea, dataArea);
        }


         render(g2, dataArea, info, crosshairInfo);



     }



}
