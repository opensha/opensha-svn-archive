package org.scec.gui;

import com.jrefinery.chart.*;
import com.jrefinery.chart.event.*;
import com.jrefinery.chart.tooltips.*;
import com.jrefinery.data.*;

import java.awt.*;
import java.awt.geom.*;
import java.util.*;

import java.math.BigDecimal;
import javax.swing.*;
import org.scec.sha.imr.gui.IMRTesterApplet;
import org.scec.sha.magdist.gui.MagFreqDistTesterApplet;


/**
 * <p>Title: PSHALogXYPlot</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
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
    private IMRTesterApplet imrTesterApplet=null;
    private MagFreqDistTesterApplet magFreqDistTesterApplet=null;

    /**
     * Constructs an XYPlot with the specified axes (other attributes take default values).
     *
     * @param domainAxis The domain axis.
     * @param rangeAxis The range axis.
     */
    public PSHALogXYPlot(IMRTesterApplet imr,XYDataset data, ValueAxis domainAxis, ValueAxis rangeAxis, boolean xlog,boolean ylog) {
        super(data, domainAxis, rangeAxis);
        this.xlogplot=xlog;
        this.ylogplot=ylog;
        this.imrTesterApplet=imr;
    }


     /**
     * Constructs an XYPlot with the specified axes (other attributes take default values).
     *
     * @param domainAxis The domain axis.
     * @param rangeAxis The range axis.
     */
    public PSHALogXYPlot(MagFreqDistTesterApplet magFreq,XYDataset data, ValueAxis domainAxis, ValueAxis rangeAxis, boolean xlog,boolean ylog) {
        super(data, domainAxis, rangeAxis);
        this.xlogplot=xlog;
        this.ylogplot=ylog;
        this.magFreqDistTesterApplet=magFreq;
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
    public PSHALogXYPlot(XYDataset data,
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
                                                       this.foregroundAlpha));

            drawVerticalLines(g2, dataArea);
            drawHorizontalLines(g2, dataArea);

            renderer.initialise(g2, dataArea, this, data, info);

            /* boolean log plots added to renderer */
            ValueAxis domainAxis = this.getDomainAxis();
            ValueAxis rangeAxis = this.getRangeAxis();
            int seriesCount = data.getSeriesCount();

            for (int series=0; series<seriesCount; series++) {
                int itemCount = data.getItemCount(series);
                for (int item=0; item<itemCount; item++) {
                     ((LogXYItemRenderer)renderer).drawItem(g2, dataArea, info, this,
                                      domainAxis,rangeAxis,
                                      data, series, item,
                                      crosshairInfo,xlogplot,ylogplot);

                }
            }


            // draw vertical crosshair if required...
            domainAxis.setCrosshairValue(crosshairInfo.getCrosshairX());
            if (domainAxis.isCrosshairVisible()) {
                this.drawVerticalLine(g2, dataArea, domainAxis.getCrosshairValue(),
                                      domainAxis.getCrosshairStroke(),
                                      domainAxis.getCrosshairPaint());
            }

            // draw horizontal crosshair if required...
            rangeAxis.setCrosshairValue(crosshairInfo.getCrosshairY());
            if (rangeAxis.isCrosshairVisible()) {
                this.drawHorizontalLine(g2, dataArea, rangeAxis.getCrosshairValue(),
                                        rangeAxis.getCrosshairStroke(),
                                        rangeAxis.getCrosshairPaint());
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

        // draw the plot background and axes...

        drawOutlineAndBackground(g2, dataArea);
        if (this.domainAxis!=null) {
            this.domainAxis.draw(g2, plotArea, dataArea);
        }
        if (this.rangeAxis!=null) {
            this.rangeAxis.draw(g2, plotArea, dataArea);
        }

        render(g2, dataArea, info, crosshairInfo);

     }catch(java.lang.ArithmeticException ae){
       String message=new String(ae.getMessage());
       if(imrTesterApplet!=null)
         imrTesterApplet.invalidLogPlot(message);
       if(magFreqDistTesterApplet!=null)
         magFreqDistTesterApplet.invalidLogPlot(message);
    }
   }
}
