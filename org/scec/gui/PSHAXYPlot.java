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

/**
 * <b>Title:</b> PSHAXYPlot<p>
 *
 * <b>Description:</b>  Subclass of the JFreeChart XYPlot. The sole
 * purpose this class was required was so that mouse clicks could be tracked
 * showing the x and y values at the click point. <p>
 *
 *
 * This is not a primary class of the SCEC framework so a full description
 * will not be provided here. More more information please consult the
 * JFreeChart framework. Only experienced programmers would need to modify
 * this class. <p>
 *
 * @author  Steven W. Rock, Gupta Brothers.
 * @created    February 22, 2002
 * @version    1.0
 */

public class PSHAXYPlot
         extends XYPlot
         implements
        HorizontalValuePlot,
        VerticalValuePlot {


    protected final static String C = "PSHAXYPlot";
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


    /**
     * Constructs an XYPlot with the specified axes (other attributes take default values).
     *
     * @param domainAxis The domain axis.
     * @param rangeAxis The range axis.
     */
    public PSHAXYPlot(XYDataset data, ValueAxis domainAxis, ValueAxis rangeAxis, boolean xlog,boolean ylog) {
        super(data, domainAxis, rangeAxis);
        this.xlogplot=xlog;
        this.ylogplot=ylog;
    }

    /**
     * Constructs an XYPlot with the specified axes and renderer (other attributes take default
     * values).
     *
     * @param domainAxis The domain axis.
     * @param rangeAxis The range axis.
     * @param renderer The renderer
     */
    public PSHAXYPlot(XYDataset data,
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


}
