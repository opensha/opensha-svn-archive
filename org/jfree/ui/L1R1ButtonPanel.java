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
 * --------------------
 * L1R1ButtonPanel.java
 * --------------------
 * (C) Copyright 2000-2003, by Simba Management Limited.
 *
 * Original Author:  David Gilbert (for Simba Management Limited);
 * Contributor(s):   -;
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

import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.JButton;

/**
 * A 'ready-made' panel that has one button on the left and another button on the right - a layout
 * manager takes care of resizing.
 *
 * @author David Gilbert
 */
public class L1R1ButtonPanel extends JPanel {

    /** The button on the left. */
    private JButton left;

    /** The button on the right. */
    private JButton right;

    /**
     * Standard constructor - creates a two-button panel with the specified labels.
     *
     * @param leftLabel  the label for the left button.
     * @param rightLabel  the label for the right button.
     */
    public L1R1ButtonPanel(String leftLabel, String rightLabel) {

        setLayout(new BorderLayout());
        left = new JButton(leftLabel);
        right = new JButton(rightLabel);
        add(left, BorderLayout.WEST);
        add(right, BorderLayout.EAST);

    }

    /**
     * Returns a reference to button 1, allowing the caller to set labels, action-listeners etc.
     *
     * @return the button.
     */
    public JButton getLeftButton() {
        return left;
    }

    /**
     * Returns a reference to button 2, allowing the caller to set labels, action-listeners etc.
     *
     * @return the button.
     */
    public JButton getRightButton() {
        return right;
    }

}
