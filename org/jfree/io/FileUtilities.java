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
 * ------------------
 * FileUtilities.java
 * ------------------
 * (C) Copyright 2000-2003, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * $Id$
 *
 * Changes (from 5-Nov-2001)
 * -------------------------
 * 05-Nov-2001 : Changed package to com.jrefinery.io.* (DG);
 * 04-Mar-2002 : Renamed Files.java --> FileUtilities.java (DG);
 * 10-Oct-2002 : Fixed errors reported by Checkstyle (DG);
 *
 */

package org.jfree.io;

import java.io.File;
import java.util.StringTokenizer;

/**
 * A class containing useful utility methods relating to files.
 *
 * @author David Gilbert
 */
public class FileUtilities {

    /**
     * To prevent unnecessary instantiation.
     */
    protected FileUtilities() {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns a reference to a file with the specified name that is located
     * somewhere on the classpath.  The code for this method is an adaptation
     * of code supplied by Dave Postill.
     *
     * @param name  the filename.
     *
     * @return a reference to a file or <code>null</code> if no file could be found.
     */
    public static File findFileOnClassPath(String name) {

        String classpath = System.getProperty("java.class.path");
        String pathSeparator = System.getProperty("path.separator");
        String fileSeparator = System.getProperty("file.separator");

        StringTokenizer tokenizer = new StringTokenizer(classpath, pathSeparator);

        while (tokenizer.hasMoreTokens()) {
            String pathElement = tokenizer.nextToken();

            File directoryOrJar = new File(pathElement);
            File absoluteDirectoryOrJar = directoryOrJar.getAbsoluteFile();

            if (absoluteDirectoryOrJar.isFile()) {
                File target = new File(absoluteDirectoryOrJar.getParent() + fileSeparator + name);
                if (target.exists()) {
                    return target;
                }
            }
            else {
                File target = new File(pathElement + fileSeparator + name);
                if (target.exists()) {
                    return target;
                }
            }

        }
        return null;

    }

}
