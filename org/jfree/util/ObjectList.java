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
 * ---------------
 * ObjectList.java
 * ---------------
 * (C)opyright 2003, by Object Refinery Limited and Contributors.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * $Id$
 *
 * Changes
 * -------
 * 17-Jul-2003 : Version 1 (DG);
 * 13-Aug-2003 : Refactored to extend AbstractObjectList (DG);
 * 
 */
 
package org.jfree.util;

import java.io.Serializable;

/**
 * A list of objects that can grow as required.
 * <p>
 * When cloning, the objects in the list are NOT cloned, only the references.
 * 
 * @author David Gilbert
 */
public class ObjectList extends AbstractObjectList implements Cloneable, 
                                                              PublicCloneable, 
                                                              Serializable {
    
    /**
     * Default constructor.
     */
    public ObjectList() {
        super();
    }
    
    /**
     * Creates a new list.
     * 
     * @param initialCapacity  the initial capacity.
     */
    public ObjectList(int initialCapacity) {
        super(initialCapacity);
    }
    
    /**
     * Returns the object at the specified index, if there is one, or <code>null</code>.
     *
     * @param index  the object index.
     * 
     * @return The object or <code>null</code>.
     */
    public Object get(int index) {
        return super.get(index);
    }
    
    /**
     * Sets an object reference (overwriting any existing object).
     * 
     * @param index  the object index.
     * @param object  the object (<code>null</code> permitted).
     */
    public void set(int index, Object object) {
        super.set(index, object);
    }
    
    /**
     * Returns the index of the specified object, or -1 if the object is not in the list.
     * 
     * @param object  the object.
     * 
     * @return The index or -1.
     */
    public int indexOf(Object object) {
        return super.indexOf(object);
    }
    
}
