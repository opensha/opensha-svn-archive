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
 * ---------------------------
 * ColorObjectDescription.java
 * ---------------------------
 * (C)opyright 2003, by Thomas Morgner and Contributors.
 *
 * Original Author:  Thomas Morgner;
 * Contributor(s):   David Gilbert (for Simba Management Limited);
 *
 * $Id$
 *
 * Changes (from 19-Feb-2003)
 * -------------------------
 * 19-Feb-2003 : Added standard header and Javadocs (DG);
 * 29-Apr-2003 : Destilled from the JFreeReport project and moved into JCommon
 */

package org.jfree.xml.factory.objects;

import org.jfree.util.Log;

import java.awt.Color;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * An object-description for a <code>Color</code> object.
 *
 * @author Thomas Morgner
 */
public class ColorObjectDescription extends AbstractObjectDescription {

    /**
     * Creates a new object description.
     */
    public ColorObjectDescription() {
        super(Color.class);
        setParameterDefinition("value", String.class);
    }

    /**
     * Creates an object based on this description.
     *
     * @return The object.
     */
    public Object createObject() {
        String value = (String) getParameter("value");
        if (value == null) {
            return Color.black;
        }
        try {
            // get color by hex or octal value
            return Color.decode(value);
        }
        catch (NumberFormatException nfe) {
            // if we can't decode lets try to get it by name
            try {
                // try to get a color by name using reflection
                Field f = Color.class.getField(value);

                return (Color) f.get(null);
            }
            catch (Exception ce) {
                Log.info("No such Color : " + value);
                // if we can't get any color return black
                return Color.black;
            }
        }
    }

    /**
     * Sets the parameters of this description object to match the supplied object.
     *
     * @param o  the object (should be an instance of <code>Color</code>).
     *
     * @throws ObjectFactoryException if there is a problem while reading the
     * properties of the given object.
     */
    public void setParameterFromObject(Object o) throws ObjectFactoryException {
        if (o instanceof Color == false) {
            throw new ObjectFactoryException("Is no instance of java.awt.Color");
        }
        Color c = (Color) o;

        try {
            Field[] fields = Color.class.getFields();
            for (int i = 0; i < fields.length; i++) {
                Field f = fields[i];
                if (Modifier.isPublic(f.getModifiers())
                    && Modifier.isFinal(f.getModifiers())
                    && Modifier.isStatic(f.getModifiers())) {
                    String name = f.getName();
                    Object oColor = f.get(null);
                    if (oColor instanceof Color) {
                        if (c.equals(oColor)) {
                            setParameter("value", name);
                            return;
                        }
                    }
                }
            }
        }
        catch (Exception e) {
            //
        }

        // no defined constant color, so this must be a user defined color
        String color = Integer.toHexString(c.getRGB() & 0x00ffffff);
        StringBuffer retval = new StringBuffer(7);
        retval.append("#");

        int fillUp = 6 - color.length();
        for (int i = 0; i < fillUp; i++) {
            retval.append("0");
        }

        retval.append(color);
        setParameter("value", retval.toString());
    }
}
