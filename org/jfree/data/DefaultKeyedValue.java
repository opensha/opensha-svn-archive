/* ======================================
 * JFreeChart : a free Java chart library
 * ======================================
 *
 * Project Info:  http://www.jfree.org/jfreechart/index.html
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
 * -----------------------
 * DefaultKeyedValues.java
 * -----------------------
 * (C) Copyright 2002, 2003, by Simba Management Limited.
 *
 * Original Author:  David Gilbert (for Simba Management Limited);
 * Contributor(s):   -;
 *
 * $Id$
 *
 * Changes:
 * --------
 * 31-Oct-2002 : Version 1 (DG);
 * 13-Mar-2003 : Added equals(...) method, and implemented Serializable (DG);
 *
 */

package org.jfree.data;

import java.io.Serializable;

/**
 * A (key, value) pair.
 * <P>
 * This class provides a default implementation of the {@link KeyedValue} interface.
 *
 * @author David Gilbert
 */
public class DefaultKeyedValue implements KeyedValue, Serializable {

    /** The key. */
    private Comparable key;

    /** The value. */
    private Number value;

    /**
     * Creates a new (key, value) pair.
     *
     * @param key  the key.
     * @param value  the value (<code>null</code> permitted).
     */
    public DefaultKeyedValue(Comparable key, Number value) {
        this.key = key;
        this.value = value;
    }

    /**
     * Returns the key.
     *
     * @return the key.
     */
    public Comparable getKey() {
        return this.key;
    }

    /**
     * Returns the value.
     *
     * @return the value.
     */
    public Number getValue() {
        return this.value;
    }

    /**
     * Sets the value.
     *
     * @param value  the value.
     */
    public void setValue(Number value) {
        this.value = value;
    }

    /**
     * Tests if this object is equal to another.
     * 
     * @param o  the other object.
     * 
     * @return A boolean.
     */
    public boolean equals(Object o) {
    
        if (o == null) {
            return false;
        }
        if (o == this) {
            return true;
        }
        
        if (o instanceof KeyedValue) {
            KeyedValue kv = (KeyedValue) o;
            if (this.key.equals(kv.getKey())) {
                if (this.value == null) {
                    return (kv.getValue() == null);
                }
                else {
                    return this.value.equals(kv.getValue());
                }
            }
        }
       
        return false;
            
    }
    
}
