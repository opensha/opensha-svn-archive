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
 * -----------------------
 * SortButtonRenderer.java
 * -----------------------
 * (C) Copyright 2000-2003, by Nobuo Tamemasa and Contributors.
 *
 * Original Author:  Nobuo Tamemasa;
 * Contributor(s):   David Gilbert (for Simba Management Limited);
 *
 * $Id$
 *
 * Changes (from 26-Oct-2001)
 * --------------------------
 * 26-Oct-2001 : Changed package to com.jrefinery.ui.* (DG);
 * 26-Jun-2002 : Removed unnecessary import (DG);
 * 14-Oct-2002 : Fixed errors reported by Checkstyle (DG);
 *
 */

package org.jfree.ui;

import java.awt.Component;
import java.awt.Insets;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.JTableHeader;

/**
 * A table cell renderer for table headings - uses one of three JButton instances to indicate the
 * sort order for the table column.
 * <P>
 * This class (and also BevelArrowIcon) is adapted from original code by Nobuo Tamemasa (version
 * 1.0, 26-Feb-1999) posted on www.codeguru.com.
 *
 * @author Nabuo Tamemasa
 */
public class SortButtonRenderer implements TableCellRenderer {

    /** Useful constant indicating NO sorting. */
    public static final int NONE = 0;

    /** Useful constant indicating ASCENDING (that is, arrow pointing down) sorting in the table. */
    public static final int DOWN = 1;

    /** Useful constant indicating DESCENDING (that is, arrow pointing up) sorting in the table. */
    public static final int UP   = 2;

    /** The current pressed column (-1 for no column). */
    private int pressedColumn = -1;

    /** The three buttons that are used to render the table header cells. */
    private JButton normalButton, ascendingButton, descendingButton;

    /**
     * Constructs a SortButtonRenderer.
     */
    public SortButtonRenderer() {

        pressedColumn = -1;

        normalButton = new JButton();
        normalButton.setMargin(new Insets(0, 0, 0, 0));
        normalButton.setHorizontalAlignment(JButton.LEADING);

        ascendingButton = new JButton();
        ascendingButton.setMargin(new Insets(0, 0, 0, 0));
        ascendingButton.setHorizontalAlignment(JButton.LEADING);
        ascendingButton.setHorizontalTextPosition(JButton.LEFT);
        ascendingButton.setIcon(new BevelArrowIcon(BevelArrowIcon.DOWN, false, false));
        ascendingButton.setPressedIcon(new BevelArrowIcon(BevelArrowIcon.DOWN, false, true));

        descendingButton = new JButton();
        descendingButton.setMargin(new Insets(0, 0, 0, 0));
        descendingButton.setHorizontalAlignment(JButton.LEADING);
        descendingButton.setHorizontalTextPosition(JButton.LEFT);
        descendingButton.setIcon(new BevelArrowIcon(BevelArrowIcon.UP, false, false));
        descendingButton.setPressedIcon(new BevelArrowIcon(BevelArrowIcon.UP, false, true));

        Border border = UIManager.getBorder("TableHeader.cellBorder");
        normalButton.setBorder(border);
        ascendingButton.setBorder(border);
        descendingButton.setBorder(border);

    }

    /**
     * Returns the renderer component.
     *
     * @param table  the table.
     * @param value  the value.
     * @param isSelected  selected?
     * @param hasFocus  focussed?
     * @param row  the row.
     * @param column  the column.
     *
     * @return the renderer.
     */
    public Component getTableCellRendererComponent(JTable table,
                                                   Object value,
                                                   boolean isSelected,
                                                   boolean hasFocus,
                                                   int row, int column) {

        JButton button = normalButton;
        int cc = table.convertColumnIndexToModel(column);
        if (table != null) {
            SortableTableModel model = (SortableTableModel) table.getModel();
            if (model.getSortingColumn() == cc) {
                if (model.getAscending()) {
                    button = ascendingButton;
                }
                else {
                    button = descendingButton;
                }
            }
        }

        JTableHeader header = table.getTableHeader();
        if (header != null) {
            button.setForeground(header.getForeground());
            button.setBackground(header.getBackground());
            button.setFont(header.getFont());
        }

        button.setText((value == null) ? "" : value.toString());
        boolean isPressed = (cc == pressedColumn);
        button.getModel().setPressed(isPressed);
        button.getModel().setArmed(isPressed);
        return button;
    }

    /**
     * Sets the pressed column.
     *
     * @param column  the column.
     */
    public void setPressedColumn(int column) {
        this.pressedColumn = column;
    }

}
