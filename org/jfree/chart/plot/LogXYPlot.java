package org.jfree.chart.plot;

import org.jfree.chart.*;
import org.jfree.chart.event.*;
import org.jfree.chart.tooltips.*;
import org.jfree.data.*;
import org.jfree.chart.axis.*;
import org.jfree.chart.renderer.*;

import java.awt.*;
import java.awt.geom.*;
import java.util.*;

import java.math.BigDecimal;
import javax.swing.*;

import org.scec.gui.plot.LogPlotAPI;

/**
 * <p>Title: LogXYPlot</p>
 * <p>Description:This class provides the Log extension to the XYPlot class
 * and make calls to the functions of the Log classes</p>
 * @author Ned Field , Nitin Gupta and Vipin Gupta
 * @version 1.0
 */

public class LogXYPlot
         extends XYPlot
         implements
        HorizontalValuePlot,
        VerticalValuePlot {


    protected final static String C = "LogXYPlot";
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
    public LogXYPlot(LogPlotAPI logPlot,XYDataset data, ValueAxis domainAxis, ValueAxis rangeAxis, boolean xlog,boolean ylog) {
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
    public LogXYPlot(XYDataset data,
                  ValueAxis domainAxis, ValueAxis rangeAxis, XYItemRenderer renderer) {
        super(data, domainAxis, rangeAxis, renderer);
    }




    /**
     *  Sets the component attribute of the XYPlot object
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


    /**
     * Draws a representation of the data within the dataArea region, using the current renderer.
     * This function has overloaded from the class XYPlot becuase it calls the drawItemRenderer()
     * for the class LogXYItemRenderer
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
                  //type casted to the LogXYItemRenderer call the drawItemRender for the Log result
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
     * As the Log for the Zero is undefined, so this function provides the functionality of the
     * handling the error thorn if the user tries to the Log for the Zero value.
     * This function catches an exception if anyone tries to get the Log Value for zero.
     * The Log Error Message is throm from the HorizontalLogarithmicAxix, VerticalLogarithmic and
     * LogXYItemRenderer classes. This function handles that exception thrown.
     * LogXYPlot relies on an LogXYItemRenderer to draw each item in the plot.  This allows the visual
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
        super.draw(g2,plotArea,info);
      }catch(java.lang.ArithmeticException ae){
        String message=new String(ae.getMessage());
        if(logPlot!=null) logPlot.invalidLogPlot(message);
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

    /**
    * Handles a 'click' on the plot by updating the anchor values...
    *
    * @param x  x-coordinate, where the click occured.
    * @param y  y-coordinate, where the click occured.
    * @param info  an object for collection dimension information.
    */
   public void handleClick(int x, int y, ChartRenderingInfo info) {

       // set the anchor value for the horizontal axis...
       ValueAxis hva = getDomainAxis();
       if (hva != null) {
           double hvalue ;
           if(this.xlogplot)
             hvalue = ((HorizontalLogarithmicAxis)hva).myTranslateJava2DtoValue((float) x, info.getDataArea());
           else
             hvalue = hva.translateJava2DtoValue((float) x, info.getDataArea());
           hva.setAnchorValue(hvalue);
           setDomainCrosshairValue(hvalue);
       }

       // set the anchor value for the vertical axis...
       ValueAxis vva = getRangeAxis();
       if (vva != null) {
         double vvalue;
         if(this.ylogplot)
           vvalue = ((VerticalLogarithmicAxis)vva).myTranslateJava2DtoValue((float) y, info.getDataArea());
          else
           vvalue = vva.translateJava2DtoValue((float) y, info.getDataArea());
           vva.setAnchorValue(vvalue);
           setRangeCrosshairValue(vvalue);
       }

   }

}
