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
 * -------------------------
 * WaterTemperatureDemo.java
 * -------------------------
 * (C) Copyright 2003, by Simba Management Limited.
 *
 * Original Author:  David Gilbert (for Simba Management Limited);
 * Contributor(s):   -;
 *
 * $Id$
 *
 * Changes
 * -------
 * 17-Jan-2003 : Version 1 (DG);
 * 
 */

package org.jfree.chart.demo;

import org.jfree.data.XYDataset;
import org.jfree.data.XYSeries;
import org.jfree.data.XYSeriesCollection;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.axis.HorizontalNumberAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.VerticalNumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.ReverseXYItemRenderer;
import org.jfree.chart.renderer.XYItemRenderer;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

/**
 * This demo shows the use of the {@link ReverseXYItemRenderer} to plot water temperature at 
 * various depths.
 *
 * @author David Gilbert
 */
public class WaterTemperatureDemo extends ApplicationFrame {

    /**
     * A demonstration application showing an XY series containing a null value.
     *
     * @param title  the frame title.
     */
    public WaterTemperatureDemo(String title) {

        super(title);
        XYDataset dataset = createDataset();
        
        HorizontalNumberAxis axis1 = new HorizontalNumberAxis("Temperature");
        axis1.setRange(-0.55, -0.15);

        VerticalNumberAxis axis2 = new VerticalNumberAxis("Depth");
        axis2.setInverted(true);
        axis2.setRange(0.0, 35.0);
        axis2.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        
        XYItemRenderer renderer = new ReverseXYItemRenderer();
       
        XYPlot plot = new XYPlot(dataset, axis1, axis2, renderer);
        JFreeChart chart = new JFreeChart("Water Temperature By Depth", plot);

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
        setContentPane(chartPanel);

    }

    /**
     * Creates a sample dataset.
     * 
     * @return The dataset.
     */
    private XYDataset createDataset() {
    
        XYSeries series = new XYSeries("Zone 1");
        series.add(1.0, -0.5);
        series.add(5.0, -0.5);
        series.add(10.0, -0.4);
        series.add(15.0, -0.4);
        series.add(20.0, -0.3);
        series.add(25.0, -0.3);
        series.add(30.0, -0.2);
        series.add(35.0, -0.2);
        
        return new XYSeriesCollection(series);
        
    }    
    
    /**
     * Starting point for the demonstration application.
     *
     * @param args  ignored.
     */
    public static void main(String[] args) {

        WaterTemperatureDemo demo = new WaterTemperatureDemo("Water Temperature Demo");
        demo.pack();
        RefineryUtilities.centerFrameOnScreen(demo);
        demo.setVisible(true);

    }

}
