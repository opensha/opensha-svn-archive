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

package org.scec.sha.fault.demo;
import com.jrefinery.data.Range;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import com.jrefinery.chart.ValueAxis;
import com.jrefinery.chart.HorizontalNumberAxis;
import com.jrefinery.chart.VerticalNumberAxis;
import com.jrefinery.chart.HorizontalAxis;
import com.jrefinery.chart.VerticalAxis;
import com.jrefinery.chart.CrosshairInfo;
import com.jrefinery.chart.ChartRenderingInfo;
import com.jrefinery.chart.OverlaidXYPlot;
import com.jrefinery.chart.NumberTickUnit;

import java.text.DecimalFormat;

/**
 * An extension of XYPlot that allows multiple XYPlots to be overlaid in one space, using common
 * axes.
 *
 * @author Bill Kelemen (bill@kelemen-usa.com)
 */
public class OverlaidGridXYPlot extends OverlaidXYPlot {

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
        public OverlaidGridXYPlot(String domainAxisLabel, String rangeAxisLabel) {

            super(domainAxisLabel,rangeAxisLabel);

        }

        /**
         * Constructs an OverlaidXYPlot.
         *
         * @param domain Horizontal axis to use for all sub-plots.
         * @param range Vertical axis to use for all sub-plots.
         */
        public OverlaidGridXYPlot(ValueAxis domain, ValueAxis range) {
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

     ++counter;
     if(counter == 1)
        cosineY= Math.toRadians((rv.getLowerBound()+rv.getUpperBound())/2);

     /*
      Following code has been added to make the Longitude the cos function of the latitude
      Converting to radians because java finds the cos of the radians.
      What we are doing is scaling the horizontal longitude line based on the cos function of the latitude
     */
     HorizontalNumberAxis horz = (HorizontalNumberAxis)domainAxis;
     VerticalNumberAxis vert = (VerticalNumberAxis)rangeAxis;

     double verticaldiff = ((dataArea.getMaxY()-dataArea.getMinY())/(rv.getUpperBound()-rv.getLowerBound())) * Math.abs(Math.cos(cosineY));
     double horizontaldiff = (dataArea.getMaxX()-dataArea.getMinX())/(rh.getUpperBound()-rh.getLowerBound());
     double upperh= (dataArea.getMaxX()-dataArea.getMinX())/verticaldiff +rh.getLowerBound();
     if(upperh >= rh.getUpperBound()) {// adjust the horizontal scale
       //domainAxis.setRange(rh.getLowerBound(), upperh);
       horz.setTickUnit(new NumberTickUnit(0.71*vert.getTickUnit().getSize(), new DecimalFormat("0.000")));
     }
     else {
       // adjust the vertical scale according to horizontal scale
       //double upperv=(dataArea.getMaxY()-dataArea.getMinY())*Math.abs(Math.cos(cosineY)/horizontaldiff)+rv.getLowerBound();
       //rangeAxis.setRange(rv.getLowerBound(),upperv);
       vert.setTickUnit(new NumberTickUnit(1/0.72*horz.getTickUnit().getSize(), new DecimalFormat("0.000")));
     }
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