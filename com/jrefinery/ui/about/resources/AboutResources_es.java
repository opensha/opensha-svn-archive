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
 * ----------------------
 * AboutResources_es.java
 * ----------------------
 * (C) Copyright 2002, by Simba Management Limited.
 *
 * Original Author:  David Gilbert (for Simba Management Limited);
 * Contributor(s):   Hans-Jurgen Greiner;
 *
 * $Id$
 *
 * Changes
 * -------
 * 26-Mar-2002 : Version 1, translation by Hans-Jurgen Greiner (DG);
 * 14-Oct-2002 : Fixed errors reported by Checkstyle (DG);
 *
 */

package com.jrefinery.ui.about.resources;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.KeyStroke;
import java.util.ListResourceBundle;

/**
 * A resource bundle that stores all the user interface items that might need localisation.
 *
 * @author HJG
 */
public class AboutResources_es extends ListResourceBundle {

    /**
     * Returns the array of strings in the resource bundle.
     *
     * @return the resources.
     */
    public Object[][] getContents() {
        return CONTENTS;
    }

    /** The resources to be localised. */
    static final Object[][] CONTENTS = {

        {"about-frame.tab.about",             "Acerca"},
        {"about-frame.tab.system",            "Sistema"},
        {"about-frame.tab.contributors",      "Desarrollados"},
        {"about-frame.tab.licence",           "Licencia"},
        {"about-frame.tab.libraries",         "Bibliotecas"},

        {"contributors-table.column.name",    "Nombre:"},
        {"contributors-table.column.contact", "Contacto:"},

        {"libraries-table.column.name",       "Nombre::"},
        {"libraries-table.column.version",    "Versión:"},
        {"libraries-table.column.licence",    "Licencia:"},
        {"libraries-table.column.info",       "Otra Información:"},

        {"system-frame.title",                "propiedades del Sistema"},

        {"system-frame.button.close",         "Cerrar"},

        {"system-frame.menu.file",                "Archivo"},
        {"system-frame.menu.file.mnemonic",       new Character('F')},

        {"system-frame.menu.file.close",          "Cerrar"},
        {"system-frame.menu.file.close.mnemonic", new Character('C')},

        {"system-frame.menu.edit",                "Edición"},
        {"system-frame.menu.edit.mnemonic",       new Character('E')},

        {"system-frame.menu.edit.copy",           "Copiar"},
        {"system-frame.menu.edit.copy.mnemonic",  new Character('C')},

        {"system-properties-table.column.name",   "Nombre de Propiedad:"},
        {"system-properties-table.column.value",  "Valor:"},

        {"system-properties-panel.popup-menu.copy", "Copiar" },
        {"system-properties-panel.popup-menu.copy.accelerator",
                            KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK) },

    };

}

