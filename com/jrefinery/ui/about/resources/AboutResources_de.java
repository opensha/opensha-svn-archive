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
 * AboutResources_de.java
 * ----------------------
 * (C) Copyright 2002, by Simba Management Limited.
 *
 * Original Author:  Thomas Meier;
 * Contributor(s):   David Gilbert (for Simba Management Limited);
 *
 * $Id$
 *
 * Changes
 * -------
 * 04-Apr-2002 : Version 1, translation by Thomas Meier (DG);
 *
 */

package com.jrefinery.ui.about.resources;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.KeyStroke;
import java.util.ListResourceBundle;

/**
 * A resource bundle that stores all the user interface items that might=
 need localisation.
 */
public class AboutResources_de extends ListResourceBundle {

    /**
     * Returns the array of strings in the resource bundle.
     */
    public Object[][] getContents() {
        return contents;
    }

    /** The resources to be localised. */
    static final Object[][] contents = {

        { "about-frame.tab.about",             "=DCber" },
        { "about-frame.tab.system",            "System" },
        { "about-frame.tab.contributors",      "Entwickler" },
        { "about-frame.tab.licence",           "Lizenz" },
        { "about-frame.tab.libraries",         "Bibliotheken" },

        { "contributors-table.column.name",    "Name:" },
        { "contributors-table.column.contact", "Kontakt:" },

        { "libraries-table.column.name",       "Name:" },
        { "libraries-table.column.version",    "Version:" },
        { "libraries-table.column.licence",    "Lizenz:" },
        { "libraries-table.column.info",       "Zus. Information:" },

        { "system-frame.title",                "Systemeigenschaften" },

        { "system-frame.button.close",         "Schlie=DFen" },
        { "system-frame.button.close.mnemonic", new Character('C') },

        { "system-frame.menu.file",                "Datei" },
        { "system-frame.menu.file.mnemonic",       new Character('D') },

        { "system-frame.menu.file.close",          "Beenden" },
        { "system-frame.menu.file.close.mnemonic", new Character('B') },

        { "system-frame.menu.edit",                "Bearbeiten" },
        { "system-frame.menu.edit.mnemonic",       new Character('B') },

        { "system-frame.menu.edit.copy",           "Kopieren" },
        { "system-frame.menu.edit.copy.mnemonic",  new Character('K') },

        { "system-properties-table.column.name",   "Eigenschaft:"},
        { "system-properties-table.column.value",  "Wert:"},

        { "system-properties-panel.popup-menu.copy", "Kopieren" },
        { "system-properties-panel.popup-menu.copy.accelerator",
            KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK) }

    };

}