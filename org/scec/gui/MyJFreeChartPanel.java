package org.scec.gui;


import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.print.*;
import java.io.*;
import javax.swing.*;

import com.jrefinery.ui.*;
import com.jrefinery.chart.*;
import com.jrefinery.chart.event.*;
import com.jrefinery.chart.tooltips.*;
import com.jrefinery.chart.ui.*;

// FIX - Needs more comments

/**
 * <b>Title:</b> MyJFreeChartPanel<p>
 * <b>Description: </b> <p>
 *
 * @author Steven W. Rock
 * @version 1.0
 */

public class MyJFreeChartPanel
    extends ChartPanel
    implements ActionListener, MouseListener, Printable, ChartChangeListener
{

    protected final static String C = "MyJFreeChartPanel";
    protected final static boolean D = true;

    /**
     * Constructs a JFreeChart panel.
     *
     * @param chart The chart.
     */
    public MyJFreeChartPanel(JFreeChart chart) {
        super(chart);
    }

    /**
     * Constructs a JFreeChart panel.
     *
     * @param chart The chart.
     */
    public MyJFreeChartPanel(JFreeChart chart,
                      boolean properties, boolean save, boolean print, boolean zoom,
                      boolean tooltips) {

        super(chart, properties, save, print, zoom, tooltips);

    }

    /**
     * Constructs a JFreeChart panel.
     *
     * @param chart The chart.
     * @param width The preferred width of the panel.
     * @param height The preferred height of the panel.
     * @param useBuffer A flag that indicates whether to use the off-screen buffer to improve
     *                  performance (at the expense of memory).
     * @param properties A flag indicating whether or not the chart property editor should be
     *                   available via the popup menu.
     * @param save A flag indicating whether or not save options should be available via the popup
     *             menu.
     * @param print A flag indicating whether or not the print option should be available via the
     *              popup menu.
     * @param zoom A flag indicating whether or not zoom options should be added to the popup menu.
     * @param tooltips A flag indicating whether or not tooltips should be enabled for the chart.
     *
     */
    public MyJFreeChartPanel(JFreeChart chart,
                      int width,
                      int height,
                      int minimumDrawWidth,
                      int minimumDrawHeight,
                      int maximumDrawWidth,
                      int maximumDrawHeight,
                      boolean useBuffer,
                      boolean properties,
                      boolean save,
                      boolean print,
                      boolean zoom,
                      boolean tooltips) {

        super(chart,
            width,
            height,
            minimumDrawWidth,
            minimumDrawHeight,
            maximumDrawWidth,
            maximumDrawHeight,
            useBuffer,
            properties,
            save,
            print,
            zoom,
            tooltips);

    }


    /**
     * Handles a 'mouse released' event.
     * <P>
     * On Windows, we need to check if this is a popup trigger, but only if we haven't already
     * been tracking a zoom rectangle.
     *
     * @param e Information about the event.
     */
    public void mouseReleased(MouseEvent e) {

        if (zoomRectangle!=null) {

            if (Math.abs(e.getX()-zoomPoint.getX())>=MINIMUM_DRAG_ZOOM_SIZE) {
                if (e.getX() < zoomPoint.getX() && e.getY() < zoomPoint.getY()) {
                    autoRangeBoth();
                } else {
                    double w = Math.min(zoomRectangle.getWidth(),
                                    this.getScaledDataArea().getMaxX()-zoomPoint.getX());
                    double h = Math.min(zoomRectangle.getHeight(),
                                    this.getScaledDataArea().getMaxY()-zoomPoint.getY());
                    Rectangle2D zoomArea = new Rectangle2D.Double(zoomPoint.getX(), zoomPoint.getY(), w, h);
                    zoom(zoomArea);
                }
                this.zoomPoint = null;
                this.zoomRectangle = null;
            } else {
                Graphics2D g2 = (Graphics2D)getGraphics();
                g2.setXORMode(java.awt.Color.gray);
                if (fillZoomRectangle == true) {
                    g2.fill(zoomRectangle);
                } else {
                    g2.draw(zoomRectangle);
                }
                g2.dispose();
                this.zoomRectangle = null;
            }

        }

        else if (e.isPopupTrigger()) {
            if (popup!=null) {
                displayPopupMenu(e.getX(), e.getY());
            }
        }
        else{

            this.zoomPoint = RefineryUtilities.getPointInRectangle(e.getX(),
                                                                   e.getY(),
                                                                   this.getScaledDataArea());

            int x = (new Integer( "" + Math.round(zoomPoint.getX()))).intValue();
            int y = (new Integer( "" + Math.round(zoomPoint.getY()))).intValue();
            chart.handleClick(x, y, info);

        }

    }


    /**
     * Incomplete method - the idea is to modify the zooming options depending on the type of
     * chart being displayed by the panel.
     */
    private void displayPopupMenu(int x, int y) {

        if (popup!=null) {

            // go through each zoom menu item and decide whether or not to enable it...
            Plot plot = this.chart.getPlot();
            if (plot instanceof HorizontalValuePlot) {
                HorizontalValuePlot hvp = (HorizontalValuePlot)plot;
                ValueAxis hAxis = hvp.getHorizontalValueAxis();
            }
            popup.show(this, x, y);
        }

    }
}
