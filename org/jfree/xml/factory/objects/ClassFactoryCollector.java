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
 * 19-Feb-2003 : Added standard header and Javadocs (DG);
 * 29-Apr-2003 : Destilled from the JFreeReport project and moved into JCommon
 *
 */

package org.jfree.xml.factory.objects;

import java.util.ArrayList;
import java.util.Iterator;

import org.jfree.util.Configuration;

/**
 * A class factory collector.
 *
 * @author Thomas Morgner
 */
public class ClassFactoryCollector extends ClassFactoryImpl {

    /** Storage for the class factories. */
    private ArrayList factories;

    /**
     * Creates a new class factory collector.
     */
    public ClassFactoryCollector() {
        factories = new ArrayList();
    }

    /**
     * Adds a class factory to the collection.
     *
     * @param factory  the factory.
     */
    public void addFactory(ClassFactory factory) {
        factories.add(factory);
    }

    /**
     * Returns an iterator the provides access to all the factories in the collection.
     *
     * @return The iterator.
     */
    public Iterator getFactories() {
        return factories.iterator();
    }

    /**
     * Returns an object description for a class.
     *
     * @param c  the class.
     *
     * @return The object description.
     */
    public ObjectDescription getDescriptionForClass(Class c) {
        for (int i = 0; i < factories.size(); i++) {
            ClassFactory f = (ClassFactory) factories.get(i);
            ObjectDescription od = f.getDescriptionForClass(c);
            if (od != null) {
                return od;
            }
        }
        return super.getDescriptionForClass(c);
    }

    /**
     * Returns an object-description for the super class of a class.
     *
     * @param d  the class.
     * @param knownSuperClass the last known super class or null.
     * @return The object description.
     */
    public ObjectDescription getSuperClassObjectDescription
        (Class d, ObjectDescription knownSuperClass) {
        for (int i = 0; i < factories.size(); i++) {
            ClassFactory f = (ClassFactory) factories.get(i);
            ObjectDescription od = f.getSuperClassObjectDescription(d, knownSuperClass);
            if (od != null) {
                if (knownSuperClass == null) {
                    knownSuperClass = od;
                }
                else {
                    if (getComparator().isComparable(knownSuperClass.getObjectClass(),
                        od.getObjectClass())) {
                        if (getComparator().compare(knownSuperClass.getObjectClass(),
                            od.getObjectClass()) < 0) {
                            knownSuperClass = od;
                        }
                    }
                }
            }
        }
        return super.getSuperClassObjectDescription(d, knownSuperClass);
    }

    /**
     * Returns an iterator that provices access to the registered classes.
     *
     * @return The iterator.
     */
    public Iterator getRegisteredClasses() {
        ArrayList list = new ArrayList();
        for (int i = 0; i < factories.size(); i++) {
            ClassFactory f = (ClassFactory) factories.get(i);
            Iterator enum = f.getRegisteredClasses();
            while (enum.hasNext()) {
                list.add(enum.next());
            }
        }
        return list.iterator();
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
        if (getConfig() != null) {
            // already configured ...
            return;
        }
        super.configure(config);

        Iterator it = factories.iterator();
        while (it.hasNext()) {
            ClassFactory od = (ClassFactory) it.next();
            od.configure(config);
        }

    }
}
