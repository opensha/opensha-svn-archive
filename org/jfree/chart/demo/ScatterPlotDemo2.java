/* ======================================
 * JFreeChart : a free Java chart library
 * ======================================
 *
 * Project Info:  http://www.jfree.org/jfreechart/index.html
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
 * ---------------------
 * ScatterPlotDemo2.java
 * ---------------------
 * (C) Copyright 2002, 2003, by Simba Management Limited and Contributors.
 *
 * Original Author:  David Gilbert (for Simba Management Limited);
 * Contributor(s):   -;
 *
 * $Id$
 *
 * Changes
 * -------
 * 14-Oct-2002 : Version 1 (DG);
 *
 */

package org.jfree.chart.demo;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.XYDotRenderer;
import org.jfree.data.XYDataset;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

/**
 * A demo scatter plot.
 *
 * @author David Gilbert
 */
public class ScatterPlotDemo2 extends ApplicationFrame {

    /**
     * A demonstration application showing a scatter plot.
     *
     * @param title  the frame title.
     */
    public ScatterPlotDemo2(String title) {

        super(title);
        XYDataset data = new SampleXYDataset2();
        JFreeChart chart = ChartFactory.createScatterPlot("Scatter Plot Demo",
                                                          "X", "Y", data, 
                                                          true,  // legend
                                                          false,  // tooltips
                                                          false  // urls
                                                          );
        XYPlot plot = chart.getXYPlot();
        plot.setRenderer(new XYDotRenderer());
        NumberAxis domainAxis = (NumberAxis) plot.getDomainAxis();
        domainAxis.setAutoRangeIncludesZero(false);
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
        chartPanel.setVerticalAxisTrace(true);
        chartPanel.setHorizontalAxisTrace(true);
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

        ScatterPlotDemo2 demo = new ScatterPlotDemo2("Scatter Plot Demo 2");
        demo.pack();
        RefineryUtilities.centerFrameOnScreen(demo);
        demo.setVisible(true);

    }

}
