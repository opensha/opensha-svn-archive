/* =======================================================
 * JCommon : a free general purpose class library for Java
 * =======================================================
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
 * ------------------
 * SortableTable.java
 * ------------------
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
 * 14-Oct-2002 : Fixed errors reported by Checkstyle (DG);
 *
 */

package com.jrefinery.ui;

import javax.swing.JTable;
import javax.swing.table.TableColumnModel;
import javax.swing.table.JTableHeader;

/**
 * A simple extension of JTable that supports the use of a SortableTableModel.
 *
 * @author DG
 */
public class SortableTable extends JTable {

    /** A listener for sorting. */
    private SortableTableHeaderListener headerListener;

    /**
     * Standard constructor - builds a table for the specified model.
     *
     * @param model  the data.
     */
    public SortableTable(SortableTableModel model) {

        super(model);

        SortButtonRenderer renderer = new SortButtonRenderer();
        TableColumnModel columnModel = getColumnModel();
        for (int i = 0; i < columnModel.getColumnCount(); i++) {
            columnModel.getColumn(i).setHeaderRenderer(renderer);
        }

        JTableHeader header = getTableHeader();
        headerListener = new SortableTableHeaderListener(model, renderer);
        header.addMouseListener(headerListener);
        header.addMouseMotionListener(headerListener);

        model.sortByColumn(0, true);

    }

    /**
     * Changes the model for the table.  Takes care of updating the header listener at the
     * same time.
     *
     * @param model  the table model.
     *
     */
    public void setSortableModel(SortableTableModel model) {

        super.setModel(model);
        headerListener.setTableModel(model);
        SortButtonRenderer renderer = new SortButtonRenderer();
        TableColumnModel columnModel = this.getColumnModel();
        for (int i = 0; i < columnModel.getColumnCount(); i++) {
            columnModel.getColumn(i).setHeaderRenderer(renderer);
        }
        model.sortByColumn(0, true);

    }

}
