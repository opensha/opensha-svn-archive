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

import java.util.HashMap;
import java.util.Iterator;

import org.jfree.util.Configuration;
import org.jfree.util.Log;



/**
 * An abstract class that implements the {@link ClassFactory} interface.
 *
 * @author Thomas Morgner.
 */
public abstract class ClassFactoryImpl implements ClassFactory {

    /** Storage for the classes. */
    private HashMap classes;
    /** A class comparator for searching the super class */
    private ClassComparator comparator;
    /** The parser/report configuration */
    private Configuration config;

    /**
     * Creates a new class factory.
     */
    public ClassFactoryImpl() {
        classes = new HashMap();
        comparator = new ClassComparator();
    }

    /**
     * Returns the class comparator used to sort the super classes of an object.
     *
     * @return the class comparator.
     */
    public ClassComparator getComparator() {
        return comparator;
    }

    /**
     * Returns an object-description for a class.
     *
     * @param c  the class.
     *
     * @return An object description.
     */
    public ObjectDescription getDescriptionForClass(Class c) {
        ObjectDescription od = (ObjectDescription) classes.get(c);
        if (od == null) {
            return null;
        }
        return od.getInstance();
    }

    /**
     * Returns the most concrete object-description for the super class of a class.
     *
     * @param d  the class.
     * @param knownSuperClass a known supported superclass or null, if no superclass
     * is known yet.
     *
     * @return The object description.
     */
    public ObjectDescription getSuperClassObjectDescription
        (Class d, ObjectDescription knownSuperClass) {
        Iterator enum = classes.keySet().iterator();
        while (enum.hasNext()) {
            Class keyClass = (Class) enum.next();
            if (keyClass.isAssignableFrom(d)) {
                ObjectDescription od = (ObjectDescription) classes.get(keyClass);
                if (knownSuperClass == null) {
                    knownSuperClass = od;
                }
                else {
                    if (comparator.isComparable
                        (knownSuperClass.getObjectClass(), od.getObjectClass())) {
                        if (comparator.compare
                            (knownSuperClass.getObjectClass(), od.getObjectClass()) < 0) {
                            knownSuperClass = od;
                        }
                    }
                }
            }
            else {
                Log.debug(keyClass + " is not assignable from " + d);
            }

        }
        return null;
    }

    /**
     * Registers an object description with the factory.
     *
     * @param key  the key.
     * @param od  the object description.
     */
    protected void registerClass(Class key, ObjectDescription od) {
        classes.put(key, od);
        if (config != null) {
            od.configure(config);
        }
    }

    /**
     * Returns an iterator that provides access to the registered object definitions.
     *
     * @return The iterator.
     */
    public Iterator getRegisteredClasses() {
        return classes.keySet().iterator();
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
        if (config == null) {
            throw new NullPointerException("The given configuration is null");
        }
        if (this.config != null) {
            // already configured ... ignored
            return;
        }

        this.config = config;
        Iterator it = classes.values().iterator();
        while (it.hasNext()) {
            ObjectDescription od = (ObjectDescription) it.next();
            od.configure(config);
        }
    }

    /**
     * Returns the currently set configuration or null, if none was set.
     *
     * @return the configuration.
     */
    public Configuration getConfig() {
        return config;
    }
}
