package org.scec.gui.plot.jfreechart;

import org.jfree.chart.*;
import org.jfree.chart.axis.*;
import org.jfree.chart.renderer.*;
import org.jfree.chart.event.*;
import org.jfree.chart.tooltips.*;
import org.jfree.data.*;
import org.jfree.chart.plot.*;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.Layer;

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

public class GeoXYPlot
         extends XYPlot{


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
   /* public GeoXYPlot(XYDataset data, ValueAxis domainAxis, ValueAxis rangeAxis) {
        super(data, domainAxis, rangeAxis,false,false);
    }*/

    /**
     * Constructs an XYPlot with the specified axes and renderer (other attributes take default
     * values).
     *
     * @param domainAxis The domain axis.
     * @param rangeAxis The range axis.
     * @param renderer The renderer
     */
    public GeoXYPlot(XYDataset data,
                  ValueAxis domainAxis, ValueAxis rangeAxis, XYItemRenderer renderer) {
        super(data, domainAxis, rangeAxis, renderer);
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
     public void draw(Graphics2D g2, Rectangle2D plotArea, PlotRenderingInfo info) {

       // if the plot area is too small, just return...
       boolean b1 = (plotArea.getWidth() <= MINIMUM_WIDTH_TO_DRAW);
       boolean b2 = (plotArea.getHeight() <= MINIMUM_HEIGHT_TO_DRAW);
       if (b1 || b2) {
           return;
       }

       // record the plot area...
       if (info != null) {
           info.setPlotArea(plotArea);
       }

       // adjust the drawing area for the plot insets (if any)...
       Insets insets = getInsets();
       if (insets != null) {
           plotArea.setRect(plotArea.getX() + insets.left,
                            plotArea.getY() + insets.top,
                            plotArea.getWidth() - insets.left - insets.right,
                            plotArea.getHeight() - insets.top - insets.bottom);
       }

       AxisSpace space = calculateAxisSpace(g2, plotArea);
       Rectangle2D dataArea = space.shrink(plotArea, null);
       this.getAxisOffset().trim(dataArea);

       if (info != null) {
           info.setDataArea(dataArea);
        }
       if (info != null) {
           info.setPlotArea(dataArea);
       }

       Range rh = getDomainAxis().getRange();
       Range rv=  getRangeAxis().getRange();
       NumberAxis horz = (NumberAxis)getDomainAxis();
       NumberAxis vert = (NumberAxis)getRangeAxis();
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
       drawAxes(g2, plotArea, dataArea);

       CrosshairInfo crosshairInfo = new CrosshairInfo();
       crosshairInfo.setCrosshairDistance(Double.POSITIVE_INFINITY);
       crosshairInfo.setAnchorX(getDomainAnchor());
       crosshairInfo.setAnchorY(getRangeAnchor());
       double xx = getDomainAxis().translateValueToJava2D(getDomainAnchor(), dataArea, getDomainAxisEdge());
       double yy = getRangeAxis().translateValueToJava2D(getRangeAnchor(), dataArea, getRangeAxisEdge());
       crosshairInfo.setAnchorXView(xx);
       crosshairInfo.setAnchorYView(yy);

       if (this.getRenderer() != null) {
           Shape originalClip = g2.getClip();
           Composite originalComposite = g2.getComposite();

           g2.clip(dataArea);
           g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
                                                      getForegroundAlpha()));

           drawDomainTickBands(g2, dataArea);
           drawRangeTickBands(g2, dataArea);
           drawGridlines(g2, dataArea);

           // draw the markers...
           for (int i = 0; i < this.getSecondaryDomainAxisCount(); i++) {
               drawSecondaryDomainMarkers(g2, dataArea, i, Layer.BACKGROUND);
           }
           for (int i = 0; i < this.getSecondaryRangeAxisCount(); i++) {
               drawSecondaryRangeMarkers(g2, dataArea, i, Layer.BACKGROUND);
           }
           drawDomainMarkers(g2, dataArea, Layer.BACKGROUND);
           drawRangeMarkers(g2, dataArea, Layer.BACKGROUND);

           // draw...
           render(g2, dataArea, info, crosshairInfo);
           render2(g2, dataArea, info, crosshairInfo);

           for (int i = 0; i < this.getSecondaryDomainAxisCount(); i++) {
               drawSecondaryDomainMarkers(g2, dataArea, i, Layer.FOREGROUND);
           }
           for (int i = 0; i < this.getSecondaryRangeAxisCount(); i++) {
               drawSecondaryRangeMarkers(g2, dataArea, i, Layer.FOREGROUND);
           }

           drawDomainMarkers(g2, dataArea, Layer.FOREGROUND);
           drawRangeMarkers(g2, dataArea, Layer.FOREGROUND);


           drawAnnotations(g2, dataArea, info);

           g2.setClip(originalClip);
           g2.setComposite(originalComposite);
       }
       drawOutline(g2, dataArea);
   }


}
