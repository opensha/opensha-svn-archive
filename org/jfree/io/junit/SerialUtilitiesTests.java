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
 * -------------------------
 * SerialUtilitiesTests.java
 * -------------------------
 * (C) Copyright 2003 by Object Refinery Limited and Contributors.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * $Id$
 *
 * Changes
 * -------
 * 18-Sep-2003 : Version 1 (DG);
 *
 */

package org.jfree.io.junit;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Paint;
import java.awt.TexturePaint;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.jfree.io.SerialUtilities;

/**
 * Tests for the {@link SerialUtilities} class.
 *
 * @author David Gilbert
 */
public class SerialUtilitiesTests extends TestCase {

    /**
     * Returns the tests as a test suite.
     *
     * @return the test suite.
     */
    public static Test suite() {
        return new TestSuite(SerialUtilitiesTests.class);
    }

    /**
     * Constructs a new set of tests.
     *
     * @param  name the name of the tests.
     */
    public SerialUtilitiesTests(String name) {
        super(name);
    }

    /**
     * Tests the isSerializable(Class) method for some common cases.
     */
    public void testIsSerializable() {
        assertTrue(SerialUtilities.isSerializable(Color.class));
        assertFalse(SerialUtilities.isSerializable(GradientPaint.class));
        assertFalse(SerialUtilities.isSerializable(TexturePaint.class));
    }
    
    /**
     * Serialize a <code>Color</code> and check that it can be deserialized correctly.
     */
    public void testColorSerialization() {

        Paint p1 = Color.blue;
        Paint p2 = null;

        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(buffer);
            SerialUtilities.writePaint(p1, out);
            out.close();

            ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(buffer.toByteArray()));
            p2 = SerialUtilities.readPaint(in);
            in.close();
        }
        catch (Exception e) {
            System.out.println(e.toString());
        }
        assertEquals(p1, p2);

    }

    /**
     * Serialize a <code>GradientPaint</code>, restore it, and check for equality.
     */
    public void testGradientPaintSerialization() {

        Paint p1 = new GradientPaint(0.0f, 0.0f, Color.blue, 100.0f, 200.0f, Color.red);
        Paint p2 = null;

        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(buffer);
            SerialUtilities.writePaint(p1, out);
            out.close();

            ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(buffer.toByteArray()));
            p2 = SerialUtilities.readPaint(in);
            in.close();
        }
        catch (Exception e) {
            System.out.println(e.toString());
        }
        
        // we want to check that the two objects are equal, but can't rely on GradientPaint's
        // equals() method because it is just the default method inherited from Object...
        GradientPaint gp1 = (GradientPaint) p1;
        GradientPaint gp2 = (GradientPaint) p2;
        assertEquals(gp1.getColor1(), gp2.getColor1());
        assertEquals(gp1.getPoint1(), gp2.getPoint1());
        assertEquals(gp1.getColor2(), gp2.getColor2());
        assertEquals(gp1.getPoint2(), gp2.getPoint2());
        assertEquals(gp1.isCyclic(), gp2.isCyclic());

    }

    /**
     * Serialize a <code>TexturePaint</code>, restore it, and check for equality.  Since this
     * class is not serializable, we expect null as the result.
     */
    public void testTexturePaintSerialization() {

        Paint p1 = new TexturePaint(new BufferedImage(5, 5, BufferedImage.TYPE_INT_RGB), 
                                    new Rectangle2D.Double(0, 0, 5, 5));
        Paint p2 = null;

        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(buffer);
            SerialUtilities.writePaint(p1, out);
            out.close();

            ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(buffer.toByteArray()));
            p2 = SerialUtilities.readPaint(in);
            in.close();
        }
        catch (Exception e) {
            System.out.println(e.toString());
        }
        
        assertNull(p2);

    }

}
