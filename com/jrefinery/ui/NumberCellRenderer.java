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
 * -----------------------
 * NumberCellRenderer.java
 * -----------------------
 * (C) Copyright 2000-2002, by Simba Management Limited.
 *
 * Original Author:  David Gilbert (for Simba Management Limited);
 * Contributor(s):   -;
 *
 * $Id$
 *
 * Changes (from 26-Oct-2001)
 * --------------------------
 * 26-Oct-2001 : Changed package to com.jrefinery.ui.*;
 * 11-Mar-2002 : Updated import statements (DG);
 *
 */

package com.jrefinery.ui;

import java.awt.Component;
import java.text.NumberFormat;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * A table cell renderer that centers information in each cell.
 */
public class NumberCellRenderer extends DefaultTableCellRenderer implements TableCellRenderer {

    /**
     * Default constructor - builds a renderer that right justifies the contents of a table cell.
     */
    public NumberCellRenderer() {
        super();
        setHorizontalAlignment(JLabel.RIGHT);
    }

    /**
     * Returns itself as the renderer. Supports the TableCellRenderer interface.
     * @param table The table;
     * @param value The data to be rendered;
     * @param isSelected A boolean that indicates whether or not the cell is selected;
     * @param hasFocus A boolean that indicates whether or not the cell has the focus;
     * @param row The (zero-based) row index;
     * @param column The (zero-based) column index;
     * @return The component that can render the contents of the cell;
     */
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                 boolean hasFocus, int row, int column) {

        setFont(null);
        NumberFormat nf = NumberFormat.getNumberInstance();
        setText(nf.format(value));
        if (isSelected) {
            setBackground(table.getSelectionBackground());
        }
        else {
            setBackground(null);
        }
        return this;
    }

}
