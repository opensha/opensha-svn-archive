package org.scec.sha.fault.demo;

import com.jrefinery.chart.*;
import com.jrefinery.chart.event.*;
import com.jrefinery.chart.tooltips.*;
import com.jrefinery.data.*;
import org.scec.gui.PSHAXYPlot;
import java.awt.*;
import java.awt.geom.*;
import java.text.DecimalFormat;
import java.util.*;

import java.math.BigDecimal;
import javax.swing.*;

/**
 *  Title: Description: Copyright: Copyright (c) 2001 Company:
 *
 * @author     Nitin Gupta and Vipin Gupta
 * @created    July 03, 2002
 * @version    1.0
 */

public class PSHAGridXYPlot
         extends PSHAXYPlot{


    protected final static String C = "PSHAGridXYPlot";
    protected final static boolean D = true;

    /**
     * counter to track the number of times draw function is called
     * We save the middle of Y value the first time it is called
     * That Y valye is used for cosine transformation
     */
    private int counter = 0;
    double cosineY; // Y value for cosine function is calculated

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
       if (info != null) {
           info.setPlotArea(plotArea);

       }

       // adjust the drawing area for plot insets (if any)...
       if (insets != null) {
           plotArea.setRect(plotArea.getX() + insets.left,
                            plotArea.getY() + insets.top,
                            plotArea.getWidth() - insets.left - insets.right,
                            plotArea.getHeight() - insets.top - insets.bottom);
       }

       // estimate the area required for drawing the axes...
       double hAxisAreaHeight = 0;

       if (this.getDomainAxis() != null) {
           HorizontalAxis hAxis = (HorizontalAxis) this.getDomainAxis();
           hAxisAreaHeight = hAxis.reserveHeight(g2, this, plotArea);
       }

       double vAxisWidth = 0;
       if (this.getRangeAxis() != null) {
           VerticalAxis vAxis = (VerticalAxis) this.getRangeAxis();
           vAxisWidth = vAxis.reserveAxisArea(g2, this, plotArea, hAxisAreaHeight).getWidth();
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
       drawOutlineAndBackground(g2, dataArea);


       if (this.getDomainAxis() != null) {
           this.getDomainAxis().draw(g2, plotArea, dataArea);
       }

       if (this.getRangeAxis() != null) {
           this.getRangeAxis().draw(g2, plotArea, dataArea);
       }

       if (this.getRenderer() != null) {
           Shape originalClip = g2.getClip();
           Composite originalComposite = g2.getComposite();

           g2.clip(dataArea);
           g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
                                                      this.foregroundAlpha));
           render(g2, dataArea, info, crosshairInfo);
           g2.setClip(originalClip);
           g2.setComposite(originalComposite);
       }

   }


}
