/* ================================================================
 * JCommon : a general purpose, open source, class library for Java
 * ================================================================
 *
 * Project Info:  http://www.object-refinery.com/jcommon/index.html
 * Project Lead:  David Gilbert (david.gilbert@object-refinery.com);
 *
 * (C) Copyright 2000-2002, by Simba Management Limited and Contributors.
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
 * ----------------------
 * LibraryTableModel.java
 * ----------------------
 * (C) Copyright 2002, by Simba Management Limited.
 *
 * Original Author:  David Gilbert (for Simba Management Limited);
 * Contributor(s):   -;
 *
 * $Id$
 *
 * Changes
 * -------
 * 28-Feb-2002 : Version 1 (DG);
 * 15-Mar-2002 : Modified to use ResourceBundle for elements that require localisation (DG);
 *
 */

package com.jrefinery.ui.about;

import java.util.List;
import java.util.ResourceBundle;
import javax.swing.table.AbstractTableModel;

/**
 * A table model containing a list of libraries used in a project.  Used in the
 * LibraryReferencePanel class.
 */
public class LibraryTableModel extends AbstractTableModel {

    /** Storage for the libraries. */
    protected List libraries;

    /** Localised name column label. */
    protected String nameColumnLabel;

    /** Localised version column label. */
    protected String versionColumnLabel;

    /** Localised licence column label. */
    protected String licenceColumnLabel;

    /** Localised info column label. */
    protected String infoColumnLabel;

    /**
     * Constructs a LibraryTableModel.
     *
     * @param libraries The libraries.
     */
    public LibraryTableModel(List libraries) {

        this.libraries = libraries;

        String baseName = "com.jrefinery.ui.about.resources.AboutResources";
        ResourceBundle resources = ResourceBundle.getBundle(baseName);

        this.nameColumnLabel = resources.getString("libraries-table.column.name");
        this.versionColumnLabel = resources.getString("libraries-table.column.version");
        this.licenceColumnLabel = resources.getString("libraries-table.column.licence");
        this.infoColumnLabel = resources.getString("libraries-table.column.info");

    }

    /**
     * Returns the number of rows in the table model.
     *
     * @return The number of rows.
     */
    public int getRowCount() {
        return libraries.size();
    }

    /**
     * Returns the number of columns in the table model.  In this case, there are always four
     * columns (name, version, licence and other info).
     *
     * @return The number of columns in the table model.
     */
    public int getColumnCount() {
        return 4;
    }

    /**
     * Returns the name of a column in the table model.
     *
     * @param column The column index (zero-based).
     * @return The name of the specified column.
     */
    public String getColumnName(int column) {

        String result = null;

        switch (column) {

            case 0:  result = this.nameColumnLabel;
                     break;

            case 1:  result = this.versionColumnLabel;
                     break;

            case 2:  result = this.licenceColumnLabel;
                     break;

            case 3:  result = this.infoColumnLabel;
                     break;

        }

        return result;

    }

    /**
     * Returns the value for a cell in the table model.
     *
     * @param row The row index (zero-based).
     * @param column The column index (zero-based).
     * @return The value.
     */
    public Object getValueAt(int row, int column) {

        Object result = null;
        Library library = (Library)libraries.get(row);

        if (column==0) {
            result = library.getName();
        }
        else if (column==1) {
            result = library.getVersion();
        }
        else if (column==2) {
            result = library.getLicence();
        }
        else if (column==3) {
            result = library.getInfo();
        }
        return result;

    }

}