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
 * --------------------
 * InsetsTextField.java
 * --------------------
 * (C) Copyright 2000-2002, by Andrzej Porebski.
 *
 * Original Author:  Andrzej Porebski;
 * Contributor(s):   -;
 *
 * $Id$
 *
 * Changes (from 7-Nov-2001)
 * -------------------------
 * 07-Nov-2001 : Added to com.jrefinery.ui package (DG);
 *
 */

package com.jrefinery.ui;

import javax.swing.*;
import java.awt.Insets;

/**
 * A JTextField for displaying insets.
 */
public class InsetsTextField extends JTextField {

    /**
     * Default constructor. Initializes this text field with formatted
     * string describing provided insets.
     */
    public InsetsTextField(Insets insets) {
        super();
        setInsets(insets);
        setEnabled(false);
    }

    /**
     * Returns a formatted string describing provided insets
     */
    static public String formatInsetsString(Insets insets) {
        insets = (insets == null) ? new Insets(0,0,0,0):insets;
        return
            "T: "+insets.top+", "+
            "L: "+insets.left+", "+
            "B: "+insets.bottom+", "+
            "R: "+insets.right;
    }

    /**
     * Sets the text of this text field to the formatted string
     * describing provided insets. If insets is null, empty insets
     * (0,0,0,0) are used.
     */
    public void setInsets(Insets insets) {
        setText(formatInsetsString(insets));
    }

}
