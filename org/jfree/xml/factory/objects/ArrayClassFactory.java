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
 * --------------------------
 * ClassFactoryCollector.java
 * --------------------------
 * (C)opyright 2003, by Thomas Morgner and Contributors.
 *
 * Original Author:  Thomas Morgner;
 * Contributor(s):   David Gilbert (for Simba Management Limited);
 *
 * $Id$
 *
 * Changes (from 19-Feb-2003)
 * -------------------------
 * 06-May-2003 : Initial version
 */
package org.jfree.xml.factory.objects;

import java.util.Iterator;
import java.util.ArrayList;

import org.jfree.util.Configuration;

/**
 * An class that implements the {@link ClassFactory} interface to
 * create Arrays of objects. The object descriptions are created on
 * demand.
 *
 * @author Thomas Morgner.
 */
public class ArrayClassFactory implements ClassFactory {

    /**
     * Returns an object description for a class.
     *
     * @param c  the class.
     *
     * @return The object description.
     */
    public ObjectDescription getDescriptionForClass(Class c) {
        if (c.isArray() == false) {
            return null;
        }
        else {
            return new ArrayObjectDescription(c);
        }
    }

    /**
     * Returns an object description for the super class of a class.
     * This method always returns null.
     *
     * @param d  the class.
     * @param knownSuperClass the last known super class or null.
     *
     * @return The object description.
     */
    public ObjectDescription getSuperClassObjectDescription
        (Class d, ObjectDescription knownSuperClass) {
        return null;
    }

    /**
     * Returns an iterator for the registered classes. This returns a list
     * of pre-registered classes known to this ClassFactory. A class may be able
     * to handle more than the registered classes.
     * <p>
     * This method exists to support query tools for UI design, do not rely on it
     * for day to day work.
     *
     * @return The iterator.
     */
    public Iterator getRegisteredClasses() {
        ArrayList l = new ArrayList();
        l.add(Object[].class);
        return l.iterator();
    }

    /**
     * Configures this factory. The configuration contains several keys and
     * their defined values. The given reference to the configuration object
     * will remain valid until the report parsing or writing ends.
     * <p>
     * The configuration contents may change during the reporting.
     *
     * @param config the configuration, never null
     */
    public void configure(Configuration config) {
    }
}
