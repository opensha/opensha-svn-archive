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
 * -------------------
 * AboutResources.java
 * -------------------
 * (C) Copyright 2002, by Simba Management Limited.
 *
 * Original Author:  David Gilbert (for Simba Management Limited);
 * Contributor(s):   -;
 *
 * $Id$
 *
 * Changes
 * -------
 * 15-Mar-2002 : Version 1 (DG);
 *
 */

package com.jrefinery.ui.about.resources;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.KeyStroke;
import java.util.ListResourceBundle;

/**
 * A resource bundle that stores all the user interface items that might need localisation.
 */
public class AboutResources extends ListResourceBundle {

    /**
     * Returns the array of strings in the resource bundle.
     */
    public Object[][] getContents() {
        return contents;
    }

    /** The resources to be localised. */
    static final Object[][] contents = {

        {"about-frame.tab.about",             "About"},
        {"about-frame.tab.system",            "System"},
        {"about-frame.tab.contributors",      "Developers"},
        {"about-frame.tab.licence",           "Licence"},
        {"about-frame.tab.libraries",         "Libraries"},

        {"contributors-table.column.name",    "Name:"},
        {"contributors-table.column.contact", "Contact:"},

        {"libraries-table.column.name",       "Name:"},
        {"libraries-table.column.version",    "Version:"},
        {"libraries-table.column.licence",    "Licence:"},
        {"libraries-table.column.info",       "Other Information:"},

        {"system-frame.title",                "System Properties"},

        {"system-frame.button.close",         "Close"},
        {"system-frame.button.close.mnemonic", new Character('C')},

        {"system-frame.menu.file",                "File"},
        {"system-frame.menu.file.mnemonic",       new Character('F')},

        {"system-frame.menu.file.close",          "Close"},
        {"system-frame.menu.file.close.mnemonic", new Character('C')},

        {"system-frame.menu.edit",                "Edit"},
        {"system-frame.menu.edit.mnemonic",       new Character('E')},

        {"system-frame.menu.edit.copy",           "Copy"},
        {"system-frame.menu.edit.copy.mnemonic",  new Character('C')},

        {"system-properties-table.column.name",   "Property Name:"},
        {"system-properties-table.column.value",  "Value:"},

        {"system-properties-panel.popup-menu.copy", "Copy" },
        {"system-properties-panel.popup-menu.copy.accelerator",
                            KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK) },

    };

}