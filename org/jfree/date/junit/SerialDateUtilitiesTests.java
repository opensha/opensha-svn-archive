/* ===================================================
 * JCommon : a free general purpose Java class library
 * ===================================================
 *
 * Project Info:  http://www.jfree.org/jcommon/index.html
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
 * -----------------------------
 * SerialDateUtilitiesTests.java
 * -----------------------------
 * (C) Copyright 2002, 2003, by Simba Management Limited.
 *
 * Original Author:  David Gilbert (for Simba Management Limited);
 * Contributor(s):   -;
 *
 * $Id$
 *
 * Changes
 * -------
 * 25-Jun-2002 : Version 1 (DG);
 * 24-Oct-2002 : Fixed errors reported by Checkstyle (DG);
 *
 */

package org.jfree.date.junit;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.jfree.date.SerialDate;
import org.jfree.date.SerialDateUtilities;

/**
 * Some tests for the SerialDateUtilities class.
 *
 * @author David Gilbert
 */
public class SerialDateUtilitiesTests extends TestCase {

    /**
     * Creates a new test case.
     *
     * @param name  the name.
     */
    public SerialDateUtilitiesTests(String name) {
        super(name);
    }

    /**
     * Returns a test suite for the JUnit test runner.
     *
     * @return the test suite.
     */
    public static Test suite() {
        return new TestSuite(SerialDateUtilitiesTests.class);
    }

    /**
     * Test actual day count.
     */
    public void testDayCountActual() {
        SerialDate d1 = SerialDate.createInstance(1, SerialDate.APRIL, 2002);
        SerialDate d2 = SerialDate.createInstance(2, SerialDate.APRIL, 2002);
        int count = SerialDateUtilities.dayCountActual(d1, d2);
        assertEquals(1, count);
    }

    /**
     * Test 30/360 day count.
     */
    public void testDayCount30() {
        SerialDate d1 = SerialDate.createInstance(1, SerialDate.APRIL, 2002);
        SerialDate d2 = SerialDate.createInstance(2, SerialDate.APRIL, 2002);
        int count = SerialDateUtilities.dayCount30(d1, d2);
        assertEquals(1, count);
    }

    /**
     * Test 30/360ISDA day count.
     */
    public void testDayCount30ISDA() {
        SerialDate d1 = SerialDate.createInstance(1, SerialDate.APRIL, 2002);
        SerialDate d2 = SerialDate.createInstance(2, SerialDate.APRIL, 2002);
        int count = SerialDateUtilities.dayCount30ISDA(d1, d2);
        assertEquals(1, count);
    }

    /**
     * Test 30/360PSA day count.
     */
    public void testDayCount30PSA() {
        SerialDate d1 = SerialDate.createInstance(1, SerialDate.APRIL, 2002);
        SerialDate d2 = SerialDate.createInstance(2, SerialDate.APRIL, 2002);
        int count = SerialDateUtilities.dayCount30PSA(d1, d2);
        assertEquals(1, count);
    }

    /**
     * Test 30E/360 day count.
     */
    public void testDayCount3030E() {
        SerialDate d1 = SerialDate.createInstance(1, SerialDate.APRIL, 2002);
        SerialDate d2 = SerialDate.createInstance(2, SerialDate.APRIL, 2002);
        int count = SerialDateUtilities.dayCount30E(d1, d2);
        assertEquals(1, count);
    }

}
