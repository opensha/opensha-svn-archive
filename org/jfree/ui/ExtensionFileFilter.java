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
 * ------------------------
 * ExtensionFileFilter.java
 * ------------------------
 * (C) Copyright 2000-2003, by Simba Management Limited.
 *
 * Original Author:  David Gilbert (for Simba Management Limited);
 * Contributor(s):   -;
 *
 * $Id$
 *
 * Changes (from 26-Oct-2001)
 * --------------------------
 * 26-Oct-2001 : Changed package to com.jrefinery.ui.* (DG);
 * 26-Jun-2002 : Updated imports (DG);
 * 14-Oct-2002 : Fixed errors reported by Checkstyle (DG);
 *
 */

package org.jfree.ui;

import java.io.File;
import javax.swing.filechooser.FileFilter;

/**
 * A filter for JFileChooser that filters files by extension.
 *
 * @author David Gilbert
 */
public class ExtensionFileFilter extends FileFilter {

    /** A description for the file type. */
    private String description;

    /** The extension (for example, "png" for *.png files). */
    private String extension;

    /**
     * Standard constructor.
     *
     * @param description  a description of the file type;
     * @param extension  the file extension;
     */
    public ExtensionFileFilter(String description, String extension) {
        this.description = description;
        this.extension = extension;
    }

    /**
     * Returns true if the file ends with the specified extension.
     *
     * @param file  the file to test.
     *
     * @return A boolean that indicates whether or not the file is accepted by the filter.
     */
    public boolean accept(File file) {

        if (file.isDirectory()) {
            return true;
        }

        String name = file.getName().toLowerCase();
        if (name.endsWith(extension)) {
            return true;
        }
        else {
            return false;
        }

    }

    /**
     * Returns the description of the filter.
     *
     * @return a description of the filter.
     */
    public String getDescription() {
        return description;
    }

}
