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
 * ---------------------
 * ReadOnlyIterator.java
 * ---------------------
 * (C)opyright 2003, by Thomas Morgner and Contributors.
 *
 * Original Author:  Thomas Morgner;
 * Contributor(s):   David Gilbert (for Object Refinery Limited);
 *
 * $Id$
 *
 * Changes
 * -------------------------
 * 18-Jul-2003 : Initial version
 *
 */

package org.jfree.util;

import java.util.Iterator;

/**
 * Protects an given iterator by preventing calls to remove().
 *
 * @author Thomas Morgner
 */
public class ReadOnlyIterator implements Iterator {

    /** The base iterator which we protect. */
    private Iterator base;

    /**
     * Creates a new read-only iterator for the given iterator.
     *
     * @param it the iterator.
     */
    public ReadOnlyIterator(Iterator it) {
        if (it == null) {
            throw new NullPointerException("Base iterator is null.");
        }
        base = it;
    }

    /**
     * Returns <tt>true</tt> if the iteration has more elements. (In other
     * words, returns <tt>true</tt> if <tt>next</tt> would return an element
     * rather than throwing an exception.)
     *
     * @return <tt>true</tt> if the iterator has more elements.
     */
    public boolean hasNext() {
        return base.hasNext();
    }

    /**
     * Returns the next element in the iteration.
     * Throws NoSuchElementException when iteration has no more elements.
     * 
     * @return the next element in the iteration.
     */
    public Object next() {
        return base.next();
    }

    /**
     * Throws <code>UnsupportedOperationException</code>.
     */
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
