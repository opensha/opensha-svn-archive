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
 * ArrayUtils.java
 * ---------------
 * (C) Copyright 2003, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * $Id$
 *
 * Changes
 * -------
 * 21-Aug-2003 : Version 1 (DG);
 *
 */

package org.jfree.util;

import java.util.Arrays;

/**
 * Utility methods for working with arrays.
 * 
 * @author David Gilbert
 */
public abstract class ArrayUtils {
    
    /**
     * Clones a two dimensional array of floats.
     * 
     * @param array  the array.
     * 
     * @return A clone of the array.
     */
    public static float[][] clone(float[][] array) {
    
        float[][] result = null;
        
        if (array != null) {
            result = (float[][]) array.clone();
            for (int i = 0; i < array.length; i++) {
                result[i] = (float[]) array[i].clone();
            }
        }
        
        return result;
    
    }
    
    /**
     * Tests two float arrays for equality.
     * 
     * @param array1  the first array.
     * @param array2  the second arrray.
     * 
     * @return A boolean.
     */
    public static boolean equal(float[][] array1, float[][] array2) {
        boolean result = false;
        if (array1 == null) {
            result = (array2 == null);
        }
        else {
            if (array2 == null) {
                result = false;
            }
            else {
                if (array1.length == array2.length) {
                    result = true;
                    for (int i = 0; i < array1.length; i++) {
                        if (!Arrays.equals(array1[i], array2[i])) {
                            result = false;
                        }
                    }
                }
                else {
                    result = false;
                }
            }
  
        }
        return result;
    }
    
}
