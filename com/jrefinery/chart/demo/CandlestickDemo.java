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
 * --------------------
 * CandlestickDemo.java
 * --------------------
 * (C) Copyright 2002, 2003, by Simba Management Limited and Contributors.
 *
 * Original Author:  David Gilbert (for Simba Management Limited);
 * Contributor(s):   -;
 *
 * $Id$
 *
 * Changes
 * -------
 * 16-Oct-2002 : Version 1 (DG);
 *
 */

package com.jrefinery.chart.demo;

import com.jrefinery.chart.JFreeChart;
import com.jrefinery.chart.ChartFactory;
import com.jrefinery.chart.ChartPanel;
import com.jrefinery.data.HighLowDataset;
import com.jrefinery.ui.ApplicationFrame;
import com.jrefinery.ui.RefineryUtilities;

/**
 * A demo showing a candlestick chart.
 *
 * @author David Gilbert
 */
public class CandlestickDemo extends ApplicationFrame {

    /**
     * A demonstration application showing a candlestick chart.
     *
     * @param title  the frame title.
     */
    public CandlestickDemo(String title) {

        super(title);

        HighLowDataset dataset = DemoDatasetFactory.createHighLowDataset();
        JFreeChart chart = ChartFactory.createCandlestickChart("Candlestick Demo",
                                                               "Time", "Value",
                                                               dataset, true);
        //XYPlot plot = chart.getXYPlot();
        //plot.addRangeMarker(new Marker(64, Color.blue));
        //Calendar calendar = new GregorianCalendar(2001, Calendar.JANUARY, 16);
        //plot.addDomainMarker(new Marker((double) calendar.getTime().getTime(), Color.blue));
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
        setContentPane(chartPanel);

    }

    /**
     * Starting point for the demonstration application.
     *
     * @param args  ignored.
     */
    public static void main(String[] args) {

        CandlestickDemo demo = new CandlestickDemo("Candlestick Demo");
        demo.pack();
        RefineryUtilities.centerFrameOnScreen(demo);
        demo.setVisible(true);

    }

}
