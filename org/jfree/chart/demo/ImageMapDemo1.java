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
 * ------------------
 * ImageMapDemo1.java
 * ------------------
 * (C) Copyright 2002, 2003, by Simba Management Limited and Contributors.
 *
 * Original Author:  David Gilbert (for Simba Management Limited);
 * Contributor(s):   Richard Atkinson (richard_c_atkinson@ntlworld.com);
 *
 * $Id$
 *
 * Changes
 * -------
 * 26-Jun-2002 : Version 1 (DG);
 * 05-Aug-2002 : Modified to demonstrate hrefs and alt tags in image map (RA);
 * 10-Oct-2002 : Fixed errors reported by Checkstyle (DG);
 *
 */

package org.jfree.chart.demo;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.HorizontalCategoryAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.axis.VerticalNumberAxis;
import org.jfree.chart.entity.StandardEntityCollection;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.VerticalCategoryPlot;
import org.jfree.chart.renderer.VerticalBarRenderer;
import org.jfree.chart.tooltips.StandardCategoryToolTipGenerator;
import org.jfree.chart.urls.StandardCategoryURLGenerator;
import org.jfree.data.CategoryDataset;
import org.jfree.data.DatasetUtilities;

/**
 * A demo showing how to create an HTML image map with JFreeChart.
 *
 * @author David Gilbert
 */
public class ImageMapDemo1 {

    /**
     * Default constructor.
     */
    public ImageMapDemo1() {
    }

    /**
     * Starting point for the demo.
     *
     * @param args  ignored.
     */
    public static void main(String[] args) {

        // create a chart
        double[][] data = new double[][] {
            { 56.0, -12.0, 34.0, 76.0, 56.0, 100.0, 67.0, 45.0 },
            { 37.0, 45.0, 67.0, 25.0, 34.0, 34.0, 100.0, 53.0 },
            { 43.0, 54.0, 34.0, 34.0, 87.0, 64.0, 73.0, 12.0 }
        };
		CategoryDataset dataset = DatasetUtilities.createCategoryDataset("Series ", "Type ", data);

        JFreeChart chart = null;
        boolean drilldown = true;

        if (drilldown) {
            CategoryAxis categoryAxis = new HorizontalCategoryAxis("Category");
            ValueAxis valueAxis = new VerticalNumberAxis("Value");
            VerticalBarRenderer renderer = new VerticalBarRenderer();
            renderer.setToolTipGenerator(new StandardCategoryToolTipGenerator());
            renderer.setURLGenerator(new StandardCategoryURLGenerator("bar_chart_detail.jsp"));
            Plot plot = new VerticalCategoryPlot(dataset, categoryAxis, valueAxis, renderer);
            chart = new JFreeChart("Vertical Bar Chart", JFreeChart.DEFAULT_TITLE_FONT, plot, true);
        }
        else {
            chart = ChartFactory.createVerticalBarChart("Vertical Bar Chart",  // chart title
                                                        "Category",            // domain axis label
                                                        "Value",               // range axis label
                                                        dataset,               // data
                                                        true,                  // include legend
                                                        true,
                                                        false
                                                        );
        }
        chart.setBackgroundPaint(java.awt.Color.white);

        // save it to an image
        try {
            ChartRenderingInfo info = new ChartRenderingInfo(new StandardEntityCollection());
            File file1 = new File("barchart100.png");
            ChartUtilities.saveChartAsPNG(file1, chart, 600, 400, info);

            // write an HTML page incorporating the image with an image map
            File file2 = new File("barchart100.html");
            OutputStream out = new BufferedOutputStream(new FileOutputStream(file2));
            PrintWriter writer = new PrintWriter(out);
            writer.println("<HTML>");
            writer.println("<HEAD><TITLE>JFreeChart Image Map Demo</TITLE></HEAD>");
            writer.println("<BODY>");
            ChartUtilities.writeImageMap(writer, "chart", info);
            writer.println("<IMG SRC=\"barchart100.png\" "
                           + "WIDTH=\"600\" HEIGHT=\"400\" BORDER=\"0\" USEMAP=\"#chart\">");
            writer.println("</BODY>");
            writer.println("</HTML>");
            writer.close();

        }
        catch (IOException e) {
            System.out.println(e.toString());
        }

    }

}
