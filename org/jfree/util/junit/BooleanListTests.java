/* ===================================================
 * JCommon : a free general purpose Java class library
 * ===================================================
 *
 * Project Info:  http://www.jfree.org/jfreechart/index.html
 * Project Lead:  David Gilbert (david.gilbert@object-refinery.com);
 *
 * (C) Copyright 2000-2003, by Object Refinery Limited and Contributors.
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
 * BooleanListTests.java
 * ---------------------
 * (C) Copyright 2003 by Object Refinery Limited and Contributors.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * $Id$
 *
 * Changes
 * -------
 * 13-Aug-2003 : Version 1 (DG);
 *
 */

package org.jfree.util.junit;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.jfree.util.BooleanList;

/**
 * Tests for the {@link BooleanList} class.
 *
 * @author David Gilbert
 */
public class BooleanListTests extends TestCase {

    /**
     * Returns the tests as a test suite.
     *
     * @return the test suite.
     */
    public static Test suite() {
        return new TestSuite(BooleanListTests.class);
    }

    /**
     * Constructs a new set of tests.
     *
     * @param  name the name of the tests.
     */
    public BooleanListTests(String name) {
        super(name);
    }

    /**
     * Test the equals method.
     */
    public void testEquals() {
        
        BooleanList l1 = new BooleanList();
        l1.setBoolean(0, Boolean.TRUE);
        l1.setBoolean(1, Boolean.FALSE);
        l1.setBoolean(2, null);
        
        BooleanList l2 = new BooleanList();
        l2.setBoolean(0, Boolean.TRUE);
        l2.setBoolean(1, Boolean.FALSE);
        l2.setBoolean(2, null);
        
        assertTrue(l1.equals(l2));
        assertTrue(l2.equals(l2));
        
    }
    
    
    /**
     * Confirm that cloning works.
     */
    public void testCloning() {
        
        BooleanList l1 = new BooleanList();
        l1.setBoolean(0, Boolean.TRUE);
        l1.setBoolean(1, Boolean.FALSE);
        l1.setBoolean(2, null);
        
        BooleanList l2 = null;
        try {
            l2 = (BooleanList) l1.clone();
        }
        catch (CloneNotSupportedException e) {
            System.err.println("BooleanListTests.testCloning: failed to clone.");
        }
        assertTrue(l1 != l2);
        assertTrue(l1.getClass() == l2.getClass());
        assertTrue(l1.equals(l2));
        
        l2.setBoolean(0, Boolean.FALSE);
        assertFalse(l1.equals(l2));
        
    }
    
    /**
     * Serialize an instance, restore it, and check for equality.
     */
    public void testSerialization() {

        BooleanList l1 = new BooleanList();
        l1.setBoolean(0, Boolean.TRUE);
        l1.setBoolean(1, Boolean.FALSE);
        l1.setBoolean(2, null);

        BooleanList l2 = null;

        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(l1);
            out.close();

            ObjectInput in = new ObjectInputStream(new ByteArrayInputStream(buffer.toByteArray()));
            l2 = (BooleanList) in.readObject();
            in.close();
        }
        catch (Exception e) {
            System.out.println(e.toString());
        }
        assertEquals(l1, l2);

    }

}
