package org.scec.gui;

import com.jrefinery.chart.*;
import com.jrefinery.chart.event.*;
import com.jrefinery.chart.tooltips.*;
import com.jrefinery.data.*;
import com.jrefinery.chart.plot.*;
import com.jrefinery.chart.axis.*;
import com.jrefinery.chart.renderer.*;

import java.awt.*;
import java.awt.geom.*;
import java.util.*;

import java.math.BigDecimal;
import javax.swing.*;

import org.scec.gui.plot.LogPlotAPI;

/**
 * <p>Title: PSHALogXYPlot</p>
 * <p>Description: </p>
 *
 * @author Nitin Gupta and Vipin Gupta
 * @version 1.0
 */

public class PSHALogXYPlot
         extends XYPlot
         implements
        HorizontalValuePlot,
        VerticalValuePlot {


    protected final static String C = "PSHALogXYPlot";
    protected final static boolean D = true;

    protected final static Color color = new Color(.9f, .9f, 1.0f, 1f);
    protected final static Font F = new Font("Dialog", Font.PLAIN, 9);

    protected int javaX = 0;
    protected int javaY = 0;
    protected double clickedX = 0, clickedY = 0;
    protected boolean mouseClicked = false;

    protected CrosshairInfo crosshairInfo = new CrosshairInfo();
    protected JPopupMenu menu = new JPopupMenu();
    protected JComponent menuComp = null;

    protected boolean xlogplot = false;;
    protected boolean ylogplot = false;;
    private boolean returnNoLabels = false;
    private LogPlotAPI  logPlot=null;

    /**
     * Constructs an XYPlot with the specified axes (other attributes take default values).
     *
     * @param LogPlotAPI : whichever applet needs log-log plotting capapbility
     * needs to implement this interface. It is needed so as to disaply message
     * if log-log plotting is not allowed in case of invalid data values
     *
     * @param domainAxis The domain axis.
     * @param rangeAxis The range axis.
     */
    public PSHALogXYPlot(LogPlotAPI logPlot,XYDataset data, ValueAxis domainAxis, ValueAxis rangeAxis, boolean xlog,boolean ylog) {
        super(data, domainAxis, rangeAxis);
        this.xlogplot=xlog;
        this.ylogplot=ylog;
        this.logPlot= logPlot;
    }




    /**
     * Constructs an XYPlot with the specified axes and renderer (other attributes take default
     * values).
     *
     * @param domainAxis The domain axis.
     * @param rangeAxis The range axis.
     * @param renderer The renderer
     */
    public PSHALogXYPlot(XYDataset data,
                  ValueAxis domainAxis, ValueAxis rangeAxis, XYItemRenderer renderer) {
        super(data, domainAxis, rangeAxis, renderer);
    }




    /**
     *  Sets the component attribute of the PSHAXYPlot object
     *
     * @param  comp  The new component value
     */
    public void setComponent( JComponent comp ) {
        this.menuComp = comp;
    }


    /**
     * Returns an array of labels to be displayed by the legend.
     *
     * @return An array of legend item labels (or null).
     */
    public java.util.List getLegendItemLabels() {


        java.util.List result = new java.util.ArrayList();
        if( returnNoLabels ) return result;

        SeriesDataset data = this.getXYDataset();
        if (data!=null) {
            int seriesCount = data.getSeriesCount();
            for (int i=0; i<seriesCount; i++) {
                result.add(data.getSeriesName(i));
            }
        }

        return result;

    }
    public void setReturnNoLabels(boolean returnNoLabels) {
        this.returnNoLabels = returnNoLabels;
    }
    public boolean isReturnNoLabels() {
        return returnNoLabels;
    }


    /**
     * Returns a Shape that can be used in plotting data.  Used in XYPlots.
     */
    public Shape getShape(int series, int item, double x, double y, double scale) {
        double delta = 0.5 * scale;
        return new Ellipse2D.Double(x-delta, y-delta, scale, scale);
    }

    /**
     * Returns a Shape that can be used in plotting data.  Should allow a plug-in object to
     * determine the shape...
     */
    public Shape getShape(int series, Object category, double x, double y, double scale) {
        double delta = 0.5 * scale;
        return new Ellipse2D.Double(x-delta, y-delta, scale, scale);
    }


    /* *
     * Handles a 'click' on the plot by updating the anchor values...
     * /
    public void handleClick(int x, int y, ChartRenderingInfo info) {

        String S = C + ": handleClick(): ";
        if( D ) System.out.println(S + "Starting");

        // set the anchor value for the horizontal axis...
        ValueAxis hva = this.getDomainAxis();
        double hvalue = hva.translateJava2DtoValue((float)x, info.getDataArea());

        hva.setAnchorValue(hvalue);
        hva.setCrosshairValue(hvalue);

        // set the anchor value for the vertical axis...
        ValueAxis vva = this.getRangeAxis();
        double vvalue = vva.translateJava2DtoValue((float)y, info.getDataArea());
        vva.setAnchorValue(vvalue);
        vva.setCrosshairValue(vvalue);


        mouseClicked = true;
        this.clickedX = hvalue;
        this.clickedY = vvalue;
        this.javaX = x;
        this.javaY = y;

        if( D ) System.out.println(S + "clickedX = " + clickedX);
        if( D ) System.out.println(S + "clickedY = " + clickedY);
        if( D ) System.out.println(S + "javaX = " + javaX);
        if( D ) System.out.println(S + "javaY = " + javaY);

        if( D ) System.out.println(S + "Ending");

    }

    */

    /**
     * Draws a representation of the data within the dataArea region, using the current renderer.
     *
     * @param g2 The graphics device.
     * @param dataArea The region in which the data is to be drawn.
     * @param info An optional object for collection dimension information.
     * @param crosshairInfo An optional object for collecting crosshair info.
     */
    public void render(Graphics2D g2, Rectangle2D dataArea,
                       ChartRenderingInfo info, CrosshairInfo crosshairInfo) {

        // now get the data and plot it (the visual representation will depend on the renderer
        // that has been set)...
        XYDataset data = this.getXYDataset();
        if (data!=null) {
            Shape originalClip = g2.getClip();
            Composite originalComposite = g2.getComposite();

            g2.clip(dataArea);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
                                                       this.getForegroundAlpha()));

            drawVerticalLine(g2, dataArea ,this.getDomainCrosshairValue(),
                                 this.getDomainCrosshairStroke(),
                                 this.getDomainCrosshairPaint());
            drawHorizontalLine(g2, dataArea,this.getRangeCrosshairValue(),
                                 this.getRangeCrosshairStroke(),
                                 this.getRangeCrosshairPaint());

            this.getRenderer().initialise(g2, dataArea, this, data, info);

            /* boolean log plots added to renderer */
            ValueAxis domainAxis = this.getDomainAxis();
            ValueAxis rangeAxis = this.getRangeAxis();
            int seriesCount = data.getSeriesCount();

            for (int series=0; series<seriesCount; series++) {
                int itemCount = data.getItemCount(series);
                for (int item=0; item<itemCount; item++) {
                     ((LogXYItemRenderer)getRenderer()).drawItem(g2, dataArea, info, this,
                                      domainAxis,rangeAxis,
                                      data,0, series, item,
                                      crosshairInfo,xlogplot,ylogplot);

                }
            }


            // draw vertical crosshair if required...
            setDomainCrosshairValue(crosshairInfo.getCrosshairX());
            if (this.isDomainCrosshairVisible()) {
                this.drawVerticalLine(g2, dataArea, getDomainCrosshairValue(),
                                      getDomainCrosshairStroke(),
                                      getDomainCrosshairPaint());
            }

            // draw horizontal crosshair if required...
            setRangeCrosshairValue(crosshairInfo.getCrosshairY());
            if (isRangeCrosshairVisible()) {
                this.drawHorizontalLine(g2, dataArea, getRangeCrosshairValue(),
                                        getRangeCrosshairStroke(),
                                        getRangeCrosshairPaint());
            }
            g2.setClip(originalClip);
            g2.setComposite(originalComposite);
        }

    }

    /**
     * Draws the XY plot on a Java 2D graphics device (such as the screen or a printer).
     * <P>
     * PSHAXYPlot relies on an LogXYItemRenderer to draw each item in the plot.  This allows the visual
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
       try{

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

        // estimate the height of the horizontal axis...
        double hAxisHeight = 0.0;
        ValueAxis domainAxis = this.getDomainAxis();
        if (domainAxis != null) {
            HorizontalAxis hAxis = (HorizontalAxis) domainAxis;
            hAxisHeight = hAxis.reserveHeight(g2, this, plotArea, this.getDomainAxisLocation());
        }

        // estimate the width of the vertical axis...
        double vAxis1Width = 0.0;
        ValueAxis rangeAxis = this.getRangeAxis();
        if (rangeAxis != null) {
            VerticalAxis vAxis1 = (VerticalAxis) rangeAxis;
            vAxis1Width = vAxis1.reserveWidth(g2, this, plotArea, getRangeAxisLocation(),
                                              hAxisHeight,
                                              getDomainAxisLocation());
        }

        // estimate the width of the secondary range axis (if any)...
        double vAxis2Width = 0.0;
        int secondaryAxisLocation = getOppositeAxisLocation(getRangeAxisLocation());
        VerticalAxis vAxis2 = (VerticalAxis) this.getSecondaryRangeAxis();
        if (vAxis2 != null) {
            vAxis2Width = vAxis2.reserveWidth(g2, this, plotArea, secondaryAxisLocation,
                                              hAxisHeight, getDomainAxisLocation());
        }

        // ...and therefore what is left for the plot itself...
        double x1 = getRectX(plotArea.getX(), vAxis1Width, vAxis2Width, getRangeAxisLocation());
        double y1 = getRectY(plotArea.getY(), hAxisHeight, 0.0, getDomainAxisLocation());
        Rectangle2D dataArea = new Rectangle2D.Double(x1, y1,
                                                  plotArea.getWidth() - vAxis1Width - vAxis2Width,
                                                  plotArea.getHeight() - hAxisHeight);

        if (info != null) {
            info.setDataArea(dataArea);
        }

        CrosshairInfo crosshairInfo = new CrosshairInfo();
        crosshairInfo.setCrosshairDistance(Double.POSITIVE_INFINITY);
        crosshairInfo.setAnchorX(getDomainAxis().getAnchorValue());
        crosshairInfo.setAnchorY(getRangeAxis().getAnchorValue());

        // draw the plot background and axes...
        drawBackground(g2, dataArea);
        if (domainAxis != null) {
            domainAxis.draw(g2, plotArea, dataArea, this.getDomainAxisLocation());
        }
        if (rangeAxis != null) {
            rangeAxis.draw(g2, plotArea, dataArea, this.getRangeAxisLocation());
        }
        if (this.getSecondaryRangeAxis() != null) {
            this.getSecondaryRangeAxis().draw(g2, plotArea, dataArea, secondaryAxisLocation);
        }
        XYItemRenderer renderer = this.getRenderer();
        if (renderer != null) {
            Shape originalClip = g2.getClip();
            Composite originalComposite = g2.getComposite();

            g2.clip(dataArea);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
                                                       getForegroundAlpha()));
            // draw the domain grid lines, if any...
            if (isDomainGridlinesVisible()) {
                Stroke gridStroke = getDomainGridlineStroke();
                Paint gridPaint = getDomainGridlinePaint();
                if ((gridStroke != null) && (gridPaint != null)) {
                    Iterator iterator = getDomainAxis().getTicks().iterator();
                    while (iterator.hasNext()) {
                        Tick tick = (Tick) iterator.next();
                        renderer.drawDomainGridLine(g2, this, getDomainAxis(), dataArea,
                                                    tick.getNumericalValue());
                    }
                }
            }

            // draw the range grid lines, if any...
            if (isRangeGridlinesVisible()) {
                Stroke gridStroke = getRangeGridlineStroke();
                Paint gridPaint = getRangeGridlinePaint();
                if ((gridStroke != null) && (gridPaint != null)) {
                    Iterator iterator = getRangeAxis().getTicks().iterator();
                    while (iterator.hasNext()) {
                        Tick tick = (Tick) iterator.next();
                        renderer.drawRangeGridLine(g2, this, getRangeAxis(), dataArea,
                                                   tick.getNumericalValue());
                    }
                }
            }




            render(g2, dataArea, info, crosshairInfo);
            render2(g2, dataArea, info, crosshairInfo);


            g2.setClip(originalClip);
            g2.setComposite(originalComposite);
        }
        drawOutline(g2, dataArea);

     }catch(java.lang.ArithmeticException ae){
       String message=new String(ae.getMessage());
       if(logPlot!=null)
         logPlot.invalidLogPlot(message);
    }
   }

   /**
    * Utility method for drawing a crosshair on the chart (if required).
    * This method was overridden from XYPlot so that cross hairs are correct
    *
    * @param g2  the graphics device.
    * @param dataArea  the data area.
    * @param value  the coordinate, where to draw the line.
    * @param stroke  the stroke to use.
    * @param paint  the paint to use.
    */
   protected void drawVerticalLine(Graphics2D g2, Rectangle2D dataArea,
                                   double value, Stroke stroke, Paint paint) {

     if(this.xlogplot)  {
       double xx = ((HorizontalLogarithmicAxis)getDomainAxis()).myTranslateValueToJava2D(value, dataArea);
       Line2D line = new Line2D.Double(xx, dataArea.getMinY(),
                                       xx, dataArea.getMaxY());
       g2.setStroke(stroke);
       g2.setPaint(paint);
       g2.draw(line);
     } else  super.drawVerticalLine(g2, dataArea, value, stroke, paint);

   }

   /**
    * Utility method for drawing a crosshair on the chart (if required).
    * This method was overridden from XYPlot so that cross hairs are correct
    *
    * @param g2  the graphics device.
    * @param dataArea  the data area.
    * @param value  the coordinate, where to draw the line.
    * @param stroke  the stroke to use.
    * @param paint  the paint to use.
    */
   protected void drawHorizontalLine(Graphics2D g2, Rectangle2D dataArea,
                                     double value, Stroke stroke, Paint paint) {
     if(this.ylogplot) {
       double yy = ((VerticalLogarithmicAxis)getRangeAxis()).myTranslateValueToJava2D(value, dataArea);
       Line2D line = new Line2D.Double(dataArea.getMinX(), yy,
                                       dataArea.getMaxX(), yy);
       g2.setStroke(stroke);
       g2.setPaint(paint);
       g2.draw(line);
     } else super.drawHorizontalLine(g2, dataArea, value, stroke, paint);
    }
}
