/* ===================================================
 * JCommon : a free general purpose Java class library
 * ===================================================
 *
 * Project Info: http://www.object-refinery.com/jcommon/index.html
 * Project Lead: David Gilbert (david.gilbert@object-refinery.com);
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
 * ----------------------
 * AboutResources_fr.java
 * ----------------------
 *
 * Original Author: Anthony Boulestreau;
 * Contributor(s): -;
 *
 * $Id$
 *
 * Changes
 * -------
 * 26-Mar-2002 : Version 1 (AB);
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
 * @author Anthony Boulestreau
 */
public class AboutResources_fr extends ListResourceBundle {

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

        {"about-frame.tab.about", "A propos de"},
        {"about-frame.tab.system", "Syst�me"},
        {"about-frame.tab.contributors", "D�veloppeurs"},
        {"about-frame.tab.licence", "Licence"},
        {"about-frame.tab.libraries", "Biblioth�que"},

        {"contributors-table.column.name", "Nom:"},
        {"contributors-table.column.contact", "Contact:"},

        {"libraries-table.column.name", "Nom:"},
        {"libraries-table.column.version", "Version:"},
        {"libraries-table.column.licence", "Licence:"},
        {"libraries-table.column.info", "Autre Rensignement:"},

        {"system-frame.title", "Propri�t�s du Syst�me"},

        {"system-frame.button.close", "Fermer"},

        {"system-frame.menu.file", "Fichier"},
        {"system-frame.menu.file.mnemonic", new Character('F')},

        {"system-frame.menu.file.close", "Fermer"},
        {"system-frame.menu.file.close.mnemonic", new Character('C')},

        {"system-frame.menu.edit", "Edition"},
        {"system-frame.menu.edit.mnemonic", new Character('E')},

        {"system-frame.menu.edit.copy", "Copier"},
        {"system-frame.menu.edit.copy.mnemonic", new Character('C')},

        {"system-properties-table.column.name", "Nom de la Propri�t�:"},
        {"system-properties-table.column.value", "Valeur:"},

        {"system-properties-panel.popup-menu.copy", "Copier" },
        {"system-properties-panel.popup-menu.copy.accelerator",
        KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK) },

    };

}
