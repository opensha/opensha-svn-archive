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
 * ---------------------------
 * ContributorsTableModel.java
 * ---------------------------
 * (C) Copyright 2001, 2002, by Simba Management Limited.
 *
 * Original Author:  David Gilbert (for Simba Management Limited);
 * Contributor(s):   -;
 *
 * $Id$
 *
 * Changes
 * -------
 * 10-Dec-2001 : Version 1 (DG);
 * 28-Feb-2002 : Moved into package com.jrefinery.ui.about.  Changed import statements and
 *               updated Javadoc comments (DG);
 *
 */

package com.jrefinery.ui.about;

import java.util.List;
import java.util.ResourceBundle;
import javax.swing.table.AbstractTableModel;

/**
 * A table model containing a list of contributors to a project.  Used in the ContributorsPanel
 * class.
 */
public class ContributorsTableModel extends AbstractTableModel {

    /** Storage for the contributors. */
    protected List contributors;

    /** Localised version of the name column label. */
    protected String nameColumnLabel;

    /** Localised version of the contact column label. */
    protected String contactColumnLabel;

    /**
     * Constructs a ContributorsTableModel.
     *
     * @param contributors The contributors.
     */
    public ContributorsTableModel(List contributors) {

        this.contributors = contributors;

        String baseName = "com.jrefinery.ui.about.resources.AboutResources";
        ResourceBundle resources = ResourceBundle.getBundle(baseName);
        this.nameColumnLabel = resources.getString("contributors-table.column.name");
        this.contactColumnLabel = resources.getString("contributors-table.column.contact");

    }

    /**
     * Returns the number of rows in the table model.
     *
     * @return The number of rows.
     */
    public int getRowCount() {
        return contributors.size();
    }

    /**
     * Returns the number of columns in the table model.  In this case, there are always two
     * columns (name and e-mail address).
     *
     * @return The number of columns in the table model.
     */
    public int getColumnCount() {
        return 2;
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

            case 1:  result = this.contactColumnLabel;
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
        Contributor contributor = (Contributor)contributors.get(row);

        if (column==0) {
            result = contributor.getName();
        }
        else if (column==1) {
            result = contributor.getEmail();
        }
        return result;

    }

}