/* ===================================================
 * JCommon : a free general purpose Java class library
 * ===================================================
 *
 * Project Info:  http://www.jfree.org/jcommon/index.html
 * Project Lead:  David Gilbert (david.gilbert@object-refinery.com);
 *
 * (C) Copyright 2000-2003, by Simba Management Limited and Contributors;
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
 * JCommonTestSuite.java
 * ---------------------
 * (C) Copyright 2001-2003, by Simba Management Limited.
 *
 * Original Author:  David Gilbert (for Simba Management Limited);
 * Contributor(s):   -;
 *
 * $Id$
 *
 * Changes
 * -------
 * 11-Nov-2001 : Version 1 (DG);
 * 02-Sep-2002 : Removed DataPackageTests (DG);
 *
 */

package org.jfree.junit;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.jfree.date.junit.DatePackageTests;

/**
 * A test suite for the JCommon class library that can be run using JUnit (http://www.junit.org).
 *
 * @author David Gilbert
 */
public class JCommonTestSuite extends TestCase {

    /**
     * Returns a test suite to the JUnit test runner.
     *
     * @return a test suite.
     */
    public static Test suite() {
        TestSuite suite = new TestSuite("JCommon");
        suite.addTest(DatePackageTests.suite());
        return suite;
    }

    /**
     * Constructs the test suite.
     *
     * @param name  the suite name.
     */
    public JCommonTestSuite(String name) {
        super(name);
    }

}
