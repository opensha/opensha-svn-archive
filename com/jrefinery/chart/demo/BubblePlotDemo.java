/* ======================================
 * JFreeChart : a free Java chart library
 * ======================================
 *
 * Project Info:  http://www.object-refinery.com/jfreechart/index.html
 * Project Lead:  David Gilbert (david.gilbert@object-refinery.com);
 *
 * (C) Copyright 2000-2003, by Simba Management Limited and Contributors.
 *
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation;
 * either version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * library; if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307, USA.
 *
 * -------------------
 * BubblePlotDemo.java
 * -------------------
 * (C) Copyright 2003, by Simba Management Limited and Contributors.
 *
 * Original Author:  David Gilbert (for Simba Management Limited);
 * Contributor(s):   -;
 *
 * $Id$
 *
 * Changes
 * -------
 * 28-Jan-2003 : Version 1 (DG);
 *
 */

package com.jrefinery.chart.demo;

import com.jrefinery.data.XYZDataset;
import com.jrefinery.chart.JFreeChart;
import com.jrefinery.chart.ChartFactory;
import com.jrefinery.chart.ChartPanel;
import com.jrefinery.chart.axis.NumberAxis;
import com.jrefinery.chart.plot.XYPlot;
import com.jrefinery.ui.ApplicationFrame;
import com.jrefinery.ui.RefineryUtilities;

/**
 * A bubble chart demo.
 *
 * @author David Gilbert
 */
public class BubblePlotDemo extends ApplicationFrame {

    /**
     * A demonstration application showing a bubble chart.
     *
     * @param title  the frame title.
     */
    public BubblePlotDemo(String title) {

        super(title);
        XYZDataset data = new SampleXYZDataset();
        JFreeChart chart = ChartFactory.createBubbleChart("Bubble Plot Demo",
                                                          "X", "Y", data, true, true, false);
        XYPlot plot = chart.getXYPlot();
        plot.setForegroundAlpha(0.65f);
        
        // increase the margins to account for the fact that the auto-range doesn't take into
        // account the bubble size...
        NumberAxis domainAxis = (NumberAxis) plot.getDomainAxis();
        domainAxis.setLowerMargin(0.15);
        domainAxis.setUpperMargin(0.15);
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setLowerMargin(0.15);
        rangeAxis.setUpperMargin(0.15);
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
        chartPanel.setVerticalZoom(true);
        chartPanel.setHorizontalZoom(true);
        setContentPane(chartPanel);

    }

    /**
     * Starting point for the demonstration application.
     *
     * @param args  ignored.
     */
    public static void main(String[] args) {

        BubblePlotDemo demo = new BubblePlotDemo("Bubble Plot Demo");
        demo.pack();
        RefineryUtilities.centerFrameOnScreen(demo);
        demo.setVisible(true);

    }

}
