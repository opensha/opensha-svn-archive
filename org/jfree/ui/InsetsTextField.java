/* ===================================================
 * JCommon : a free general purpose Java class library
 * ===================================================
 *
 * Project Info:  http://www.jfree.org/jcommon/index.html
 * Project Lead:  David Gilbert (david.gilbert@object-refinery.com);
 *
 * (C) Copyright 2000-2003, by Object Refinery Limited and Contributors.
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
 * --------------------
 * InsetsTextField.java
 * --------------------
 * (C) Copyright 2000-2003, by Andrzej Porebski.
 *
 * Original Author:  Andrzej Porebski;
 * Contributor(s):   Arnaud Lelievre;
 *
 * $Id$
 *
 * Changes (from 7-Nov-2001)
 * -------------------------
 * 07-Nov-2001 : Added to com.jrefinery.ui package (DG);
 * 08-Sep-2003 : Added internationalization via use of properties resourceBundle (RFE 690236) (AL);
 *
 */

package org.jfree.ui;

import java.awt.Insets;
import java.util.ResourceBundle;

import javax.swing.JTextField;

/**
 * A JTextField for displaying insets.
 *
 * @author Andrzej Porebski
 */
public class InsetsTextField extends JTextField {

    /** The resourceBundle for the localization. */
    static protected ResourceBundle localizationResources = 
                                    ResourceBundle.getBundle("org.jfree.ui.LocalizationBundle");

    /**
     * Default constructor. Initializes this text field with formatted string describing
     * provided insets.
     *
     * @param insets  the insets.
     */
    public InsetsTextField(Insets insets) {
        super();
        setInsets(insets);
        setEnabled(false);
    }

    /**
     * Returns a formatted string describing provided insets.
     *
     * @param insets  the insets.
     *
     * @return the string.
     */
    //public static String formatInsetsString(Insets insets) {
    public String formatInsetsString(Insets insets) {
        insets = (insets == null) ? new Insets(0, 0, 0, 0) : insets;
        String result = localizationResources.getString("T");
        return
            localizationResources.getString("T") + insets.top + ", "
             + localizationResources.getString("L") + insets.left + ", "
             + localizationResources.getString("B") + insets.bottom + ", "
             + localizationResources.getString("R") + insets.right;
    }

    /**
     * Sets the text of this text field to the formatted string
     * describing provided insets. If insets is null, empty insets
     * (0,0,0,0) are used.
     *
     * @param insets  the insets.
     */
    public void setInsets(Insets insets) {
        setText(formatInsetsString(insets));
    }

}
