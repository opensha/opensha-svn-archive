/* ===================================================
 * JCommon : a free general purpose Java class library
 * ===================================================
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
 * --------------------------
 * SystemPropertiesPanel.java
 * --------------------------
 * (C) Copyright 2001, 2002, by Simba Management Limited.
 *
 * Original Author:  David Gilbert (for Simba Management Limited);
 * Contributor(s):   -;
 *
 * $Id$
 *
 * Changes
 * -------
 * 26-Nov-2001 : Version 1 (DG);
 * 28-Feb-2002 : Changed package to com.jrefinery.ui.about (DG);
 * 04-Mar-2002 : Added popup menu code by Carl ?? (DG);
 * 15-Mar-2002 : Modified to use ResourceBundle for elements that require localisation (DG);
 * 26-Jun-2002 : Removed unnecessary import (DG);
 * 08-Oct-2002 : Fixed errors reported by Checkstyle (DG);
 *
 */

package com.jrefinery.ui.about;

import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;
import java.util.ResourceBundle;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.JPopupMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

/**
 * A panel containing a table of system properties.
 *
 * @author DG
 */
public class SystemPropertiesPanel extends JPanel {

    /** The table that displays the system properties. */
    private JTable table;

    /** Allows for a popup menu for copying */
    private JPopupMenu copyPopupMenu;

    /** A copy menu item. */
    private JMenuItem copyMenuItem;

    /** A popup listener. */
    private PopupListener copyPopupListener;

    /**
     * Constructs a new panel.
     */
    public SystemPropertiesPanel() {

        String baseName = "com.jrefinery.ui.about.resources.AboutResources";
        ResourceBundle resources = ResourceBundle.getBundle(baseName);

        setLayout(new BorderLayout());
        this.table = SystemProperties.createSystemPropertiesTable();
        add(new JScrollPane(table));

        // Add a popup menu to copy to the clipboard...
        copyPopupMenu = new JPopupMenu();

        String label = resources.getString("system-properties-panel.popup-menu.copy");
        KeyStroke accelerator = (KeyStroke)
            resources.getObject("system-properties-panel.popup-menu.copy.accelerator");
        copyMenuItem = new JMenuItem(label);
        copyMenuItem.setAccelerator(accelerator);
        copyMenuItem.getAccessibleContext().setAccessibleDescription(label);
        copyMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                copySystemPropertiesToClipboard();
            }
        });
        copyPopupMenu.add(copyMenuItem);

        // add popup Listener to the table
        copyPopupListener = new PopupListener();
        table.addMouseListener(copyPopupListener);

    }

    /**
     * Copies the selected cells in the table to the clipboard, in tab-delimited format.
     */
    public void copySystemPropertiesToClipboard() {

        StringBuffer buffer = new StringBuffer();
        ListSelectionModel selection = table.getSelectionModel();
        int firstRow = selection.getMinSelectionIndex();
        int lastRow = selection.getMaxSelectionIndex();
        if ((firstRow != -1) && (lastRow != -1)) {
            for (int r = firstRow; r <= lastRow; r++) {
                for (int c = 0; c < table.getColumnCount(); c++) {
                    buffer.append(table.getValueAt(r, c));
                    if (c != 2) {
                        buffer.append("\t");
                    }
                }
                buffer.append("\n");
            }
        }
        StringSelection ss = new StringSelection(buffer.toString());
        Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
        cb.setContents(ss, ss);

    }

    /**
     * A popup listener.
     */
    class PopupListener extends MouseAdapter {

        /**
         * Mouse pressed event.
         *
         * @param e  the event.
         */
        public void mousePressed(MouseEvent e) {
            maybeShowPopup(e);
        }

        /**
         * Mouse released event.
         *
         * @param e  the event.
         */
        public void mouseReleased(MouseEvent e) {
            maybeShowPopup(e);
        }

        /**
         * Event handler.
         *
         * @param e  the event.
         */
        private void maybeShowPopup(MouseEvent e) {
            if (e.isPopupTrigger()) {
                copyPopupMenu.show(table, e.getX(), e.getY());
            }
        }
    }


}

