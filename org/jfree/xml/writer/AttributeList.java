/* ===================================================
 * JCommon : a free general purpose Java class library
 * ===================================================
 *
 * Project Info:  http://www.jfree.org/jcommon/index.html
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
 * ------------------------------
 * AttributeList.java
 * ------------------------------
 * (C)opyright 2003, by Thomas Morgner and Contributors.
 *
 * Original Author:  Thomas Morgner;
 * Contributor(s):   David Gilbert (for Simba Management Limited);
 *
 * $Id$
 *
 * Changes
 * -------------------------
 * 25-Sep-2003 : Initial version
 *
 */

package org.jfree.xml.writer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * The attribute list is used by the writer to specify the attributes
 * of an XML element in a certain order.
 *
 * @author TM
 */
public class AttributeList {

    /**
     * An name/value pair of the attribute list.
     */
    private static class AttributeEntry {
        /** The name of the attribute entry. */
        private String name;
        /** The values of the attribute entry. */
        private String value;

        /**
         * Creates a new attribute entry for the given name and value.
         * Both parameters must not be null.
         *
         * @param name the name of the attribute
         * @param value the attribute's value
         * @throws NullPointerException if the attribute's name of value is
         * null.
         */
        public AttributeEntry(String name, String value) {
            if (name == null) {
                throw new NullPointerException("Name must not be null.");
            }
            if (value == null) {
                throw new NullPointerException("Value must not be null.");
            }
            this.name = name;
            this.value = value;
        }

        /**
         * Returns the name of the attribute entry.
         * @return the name.
         */
        public String getName() {
            return name;
        }

        /**
         * Returns the value of this attribute entry.
         * @return the value of the entry.
         */
        public String getValue() {
            return value;
        }

        /**
         * Checks whether the given object is an attribute entry with the same
         * name.
         * @param o the suspected other attribute entry.
         * @return true, if the given object is equal, false otherwise.
         */
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof AttributeEntry)) return false;

            final AttributeEntry attributeEntry = (AttributeEntry) o;

            if (!name.equals(attributeEntry.name)) return false;

            return true;
        }

        /**
         * Computes an hashcode for this entry.
         * @return the hashcode.
         */
        public int hashCode() {
            return name.hashCode();
        }
    }

    /**
     * An iterator over the attribute names of this list.
     */
    private static class AttributeIterator implements Iterator {
        /** The backend is an iterator over the attribute entries. */
        private Iterator backend;

        /**
         * Creates a new attribute iterator using the given iterator as backend.
         * @param backend the iterator over the attribute entries.
         */
        public AttributeIterator(Iterator backend) {
            if (backend == null) {
                throw new NullPointerException();
            }
            this.backend = backend;
        }

        /**
         * Returns <tt>true</tt> if the iteration has more elements. (In other
         * words, returns <tt>true</tt> if <tt>next</tt> would return an element
         * rather than throwing an exception.)
         *
         * @return <tt>true</tt> if the iterator has more elements.
         */
        public boolean hasNext() {
            return backend.hasNext();
        }

        /**
         * Returns the next element in the iteration.
         *
         * @return the next element in the iteration.
         * @exception NoSuchElementException iteration has no more elements.
         */
        public Object next() {
            AttributeEntry entry = (AttributeEntry) backend.next();
            if (entry != null) {
                return entry.getName();
            }
            return entry;
        }

        /**
         *
         * Removes from the underlying collection the last element returned by the
         * iterator (optional operation).  This method can be called only once per
         * call to <tt>next</tt>.  The behavior of an iterator is unspecified if
         * the underlying collection is modified while the iteration is in
         * progress in any way other than by calling this method.
         *
         * @exception UnsupportedOperationException if the <tt>remove</tt>
         *		  operation is not supported by this Iterator.

         * @exception IllegalStateException if the <tt>next</tt> method has not
         *		  yet been called, or the <tt>remove</tt> method has already
         *		  been called after the last call to the <tt>next</tt>
         *		  method.
         */
        public void remove() {
            backend.remove();
        }
    }

    /** The storage for all entries of this list. */
    private ArrayList entryList;

    /**
     * Creates an empty attribute list with no default values.
     */
    public AttributeList() {
        entryList = new ArrayList();
    }

    /**
     * Returns an iterator over all attribute names. The names are returned
     * in their oder of addition to the list. The iterator contains strings.
     *
     * @return the iterator over all attribute names.
     */
    public Iterator keys() {
        return new AttributeIterator(entryList.iterator());
    }

    /**
     * Defines a attribute.
     * @param name the name of the attribute to be defined
     * @param value the value of the attribute.
     * @throws NullPointerException if either name or value are null.
     */
    public synchronized void setAttribute(String name, String value) {
        AttributeEntry entry = new AttributeEntry(name, value);
        int pos = entryList.indexOf(entry);
        if (pos != -1) {
            entryList.remove(pos);
        }
        entryList.add(entry);
    }

    /**
     * Returns the attribute value for the given attribute name or null,
     * if the attribute is not defined in this list.
     *
     * @param name the name of the attribute
     * @return the attribute value or null.
     */
    public synchronized String getAttribute(String name) {
        return getAttribute(name, null);
    }

    /**
     * Returns the attribute value for the given attribute name or the given
     * defaultvalue, if the attribute is not defined in this list.
     *
     * @param name the name of the attribute
     * @return the attribute value or the defaultValue.
     */
    public synchronized String getAttribute(String name, String defaultValue) {
        for (int i = 0; i < entryList.size(); i++) {
            AttributeEntry ae = (AttributeEntry) entryList.get(i);
            if (ae.getName().equals(name)) {
                return ae.getValue();
            }
        }
        return defaultValue;
    }

    /**
     * Removes the attribute with the given name from the list.
     *
     * @param name the name of the attribute which should be removed..
     */
    public synchronized void removeAttribute(String name) {
        for (int i = 0; i < entryList.size(); i++) {
            AttributeEntry ae = (AttributeEntry) entryList.get(i);
            if (ae.getName().equals(name)) {
                entryList.remove(ae);
                return;
            }
        }
    }
}
