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
 * FontChooserDialog.java
 * ----------------------
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
 *
 */

package com.jrefinery.ui;

import java.awt.*;
import javax.swing.*;

/**
 * A dialog for choosing a font from the available system fonts.
 */
public class FontChooserDialog extends StandardDialog {

    /** The panel within the dialog that contains the font selection controls; */
    private FontChooserPanel fontChooserPanel;

    /**
     * Standard constructor - builds a font chooser dialog owned by another dialog.
     * @param owner The dialog that 'owns' this dialog;
     * @param title The title for the dialog;
     * @param modal A boolean that indicates whether or not the dialog is modal;
     * @param font The initial font displayed;
     */
    public FontChooserDialog(Dialog owner, String title, boolean modal, Font font) {
        super(owner, title, modal);
        setContentPane(createContent(font));
    }

    /**
     * Standard constructor - builds a font chooser dialog owned by a frame.
     * @param owner The frame that 'owns' this dialog;
     * @param title The title for the dialog;
     * @param modal A boolean that indicates whether or not the dialog is modal;
     * @param font The initial font displayed;
     */
    public FontChooserDialog(Frame owner, String title, boolean modal, Font font) {
        super(owner, title, modal);
        setContentPane(createContent(font));
    }

    /**
     * Returns the selected font.
     */
    public Font getSelectedFont() {
        return fontChooserPanel.getSelectedFont();
    }

    /**
     * Returns the panel that is the user interface.
     */
    private JPanel createContent(Font font) {
        JPanel content = new JPanel(new BorderLayout());
        content.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        if (font==null) {
            font = new Font("Dialog", 10, Font.PLAIN);
        }
            fontChooserPanel = new FontChooserPanel(font);
        content.add(fontChooserPanel);

        JPanel buttons = createButtonPanel();
        buttons.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
        content.add(buttons, BorderLayout.SOUTH);

        return content;
    }

}