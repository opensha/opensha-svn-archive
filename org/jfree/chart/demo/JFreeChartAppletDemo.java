/* ======================================
 * JFreeChart : a free Java chart library
 * ======================================
 *
 * Project Info:  http://www.jfree.org/jfreechart/index.html
 * Project Lead:  David Gilbert (david.gilbert@object-refinery.com);
 *
 * (C) Copyright 2000-2002, by Simba Management Limited and Contributors.
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
 * -------------------------
 * JFreeChartAppletDemo.java
 * -------------------------
 * (C) Copyright 2002, 2003, by Simba Management Limited.
 *
 * Original Author:  David Gilbert (for Simba Management Limited);
 * Contributor(s):   -;
 *
 * $Id$
 *
 * Changes
 * -------
 * 11-Feb-2002 : Version 1 (DG);
 * 10-Oct-2002 : Fixed errors reported by Checkstyle (DG);
 *
 */

package org.jfree.chart.demo;

import javax.swing.JApplet;
import javax.swing.JTabbedPane;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.CategoryDataset;
import org.jfree.data.XYDataset;

/**
 * A simple applet containing two sample charts in a JTabbedPane.
 *
 * @author David Gilbert
 */
public class JFreeChartAppletDemo extends JApplet {

    /**
     * Constructs the demo applet.
     */
    public JFreeChartAppletDemo() {

        JTabbedPane tabs = new JTabbedPane();

        XYDataset data1 = DemoDatasetFactory.createTimeSeriesCollection1();
        JFreeChart chart1 = ChartFactory.createTimeSeriesChart("Time Series", "Date", "Rate",
                                                               data1, true, true, false);
        ChartPanel panel1 = new ChartPanel(chart1, 400, 300, 200, 100, 400, 200,
                                           true, false, false, false, true, true);
        tabs.add("Chart 1", panel1);

		CategoryDataset data2 = DemoDatasetFactory.createCategoryDataset();
        JFreeChart chart2 = ChartFactory.createHorizontalBarChart("Bar Chart",
                                                                  "Categories", "Value",
                                                                  data2, 
                                                                  true,
                                                                  true,
                                                                  false);
        ChartPanel panel2 = new ChartPanel(chart2, 400, 300, 200, 100, 400, 200,
                                           true, false, false, false, true, true);
        tabs.add("Chart 2", panel2);

        getContentPane().add(tabs);

    }

}
