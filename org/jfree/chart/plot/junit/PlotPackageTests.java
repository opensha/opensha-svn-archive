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
 * --------------------------
 * ChartPlotPackageTests.java
 * --------------------------
 * (C) Copyright 2003, by Simba Management Limited.
 *
 * Original Author:  David Gilbert (for Simba Management Limited);
 * Contributor(s):   -;
 *
 * $Id$
 *
 * Changes:
 * --------
 * 18-Mar-2003 : Version 1 (DG);
 *
 */

package org.jfree.chart.plot.junit;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * A collection of tests for the org.jfree.chart.plot package.
 * <P>
 * These tests can be run using JUnit (http://www.junit.org).
 *
 * @author David Gilbert
 */
public class PlotPackageTests extends TestCase {

    /**
     * Returns a test suite to the JUnit test runner.
     *
     * @return the test suite.
     */
    public static Test suite() {
        TestSuite suite = new TestSuite("org.jfree.chart.plot");
        suite.addTestSuite(CombinedXYPlotTests.class);
        suite.addTestSuite(CompassPlotTests.class);
        suite.addTestSuite(ContourPlotTests.class);
        suite.addTestSuite(FastScatterPlotTests.class);
        suite.addTestSuite(HorizontalCategoryPlotTests.class);
        suite.addTestSuite(MeterPlotTests.class);
        suite.addTestSuite(OverlaidVerticalCategoryPlotTests.class);
        suite.addTestSuite(OverlaidXYPlotTests.class);
        suite.addTestSuite(PiePlotTests.class);
        suite.addTestSuite(Pie3DPlotTests.class);
        suite.addTestSuite(ThermometerPlotTests.class);
        suite.addTestSuite(VerticalCategoryPlotTests.class);
        suite.addTestSuite(XYPlotTests.class);
        return suite;
    }

    /**
     * Constructs the test suite.
     *
     * @param name  the suite name.
     */
    public PlotPackageTests(String name) {
        super(name);
    }

}
