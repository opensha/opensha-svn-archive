/*
 * -------------------
 * OverlaidGridXYPlot.java
 * -------------------
 *
 * Original Author:
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

/**
 * An extension of XYPlot that allows multiple XYPlots to be overlaid in one space, using common
 * axes.
 *
 * @author Bill Kelemen (bill@kelemen-usa.com)
 */
public class OverlaidGridXYPlot extends OverlaidXYPlot {

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
   // System.out.println("OverlaidGridXYPlot::draw::horizontalrange:"+rh.getLowerBound()+","+rh.getUpperBound());
    //System.out.println("OverlaidGridXYPlot::draw::verticalrange:"+rv.getLowerBound()+","+rv.getUpperBound());
    double verticaldiff = (dataArea.getMaxY()-dataArea.getMinY())/(rv.getUpperBound()-rv.getLowerBound());
    double upperh= ((dataArea.getMaxX()-dataArea.getMinX())+(verticaldiff*rh.getLowerBound()))/verticaldiff;
    if(upperh < rh.getUpperBound()) {
       double horizontaldiff=(dataArea.getMaxX()-dataArea.getMinX())/(rh.getUpperBound()-rh.getLowerBound());
       double upperv = ((dataArea.getMaxY()-dataArea.getMinY())+(horizontaldiff*rv.getLowerBound()))/horizontaldiff;
       rangeAxis.setRange(rv.getLowerBound(), upperv);
    }
    else
       domainAxis.setRange(rh.getLowerBound(), upperh);


     // draw the plot background and axes...
    //System.out.println("OverlaidGridXYPlot::draw::horizontalrange:"+domainAxis.getRange().getLowerBound()+","+domainAxis.getRange().getUpperBound());
   // System.out.println("OverlaidGridXYPlot::draw::verticalrange:"+rangeAxis.getRange().getLowerBound()+","+rangeAxis.getRange().getUpperBound());

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