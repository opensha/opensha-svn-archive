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
 * -----------------------
 * InsetsChooserPanel.java
 * -----------------------
 * (C) Copyright 2000-2003, by Andrzej Porebski and Contributors.
 *
 * Original Author:  Andrzej Porebski;
 * Contributor(s):   David Gilbert (for Simba Management Limited);
 *
 * $Id$
 *
 * Changes (from 7-Nov-2001)
 * -------------------------
 * 07-Nov-2001 : Added to com.jrefinery.ui package (DG);
 * 14-Oct-2002 : Fixed errors reported by Checkstyle (DG);
 * 03-Feb-2003 : Added Math.abs(...) to ensure no negative insets can be set (DG);
 *
 */

package org.jfree.ui;

import java.awt.Insets;
import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

/**
 * A component for editing an instance of the Insets class.
 *
 * @author Andrzej Porebski
 */
public class InsetsChooserPanel extends JPanel {

    /** A text field for the 'top' setting. */
    private JTextField topValueEditor;

    /** A text field for the 'left' setting. */
    private JTextField leftValueEditor;

    /** A text field for the 'bottom' setting. */
    private JTextField bottomValueEditor;

    /** A text field for the 'right' setting. */
    private JTextField rightValueEditor;

    /**
     * Creates a chooser panel that allows manipulation of Insets values.
     * The values are initialized to the empty insets (0,0,0,0).
     */
    public InsetsChooserPanel() {
        this(new Insets(0, 0, 0, 0));
    }

    /**
     * Creates a chooser panel that allows manipulation of Insets values.
     * The values are initialized to the current values of provided insets.
     *
     * @param current  the insets.
     */
    public InsetsChooserPanel(Insets current) {
        current = (current == null) ? new Insets(0, 0, 0, 0) : current;

        topValueEditor = new JTextField(new IntegerDocument(), "" + current.top, 0);
        leftValueEditor = new JTextField(new IntegerDocument(), "" + current.left, 0);
        bottomValueEditor = new JTextField(new IntegerDocument(), "" + current.bottom, 0);
        rightValueEditor = new JTextField(new IntegerDocument(), "" + current.right, 0);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new TitledBorder("Insets"));

        // First row
        panel.add(new JLabel("Top"),
                  new GridBagConstraints(1, 0, 3, 1, 0.0, 0.0,
                                         GridBagConstraints.CENTER, GridBagConstraints.NONE,
                                         new Insets(0, 0, 0, 0), 0, 0));

        // Second row
        panel.add(new JLabel(" "),
                  new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
                                         GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                                         new Insets(0, 12, 0, 12), 8, 0));
        panel.add(topValueEditor,
                  new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0,
                                         GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                                         new Insets(0, 0, 0, 0), 0, 0));
        panel.add(new JLabel(" "),
                  new GridBagConstraints(3, 1, 1, 1, 0.0, 0.0,
                                         GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                                         new Insets(0, 12, 0, 11), 8, 0));
        // Third row
        panel.add(new JLabel("Left"),
                  new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
                                         GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                                         new Insets(0, 4, 0, 4), 0, 0));
        panel.add(leftValueEditor,
                  new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
                                         GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                                         new Insets(0, 0, 0, 0), 0, 0));
        panel.add(new JLabel(" "),
                  new GridBagConstraints(2, 2, 1, 1, 0.0, 0.0,
                                         GridBagConstraints.CENTER, GridBagConstraints.NONE,
                                         new Insets(0, 12, 0, 12), 8, 0));
        panel.add(rightValueEditor,
                  new GridBagConstraints(3, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
                                         GridBagConstraints.HORIZONTAL,
                                         new Insets(0, 0, 0, 0), 0, 0));
        panel.add(new JLabel("Right"),
                  new GridBagConstraints(4, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
                                         GridBagConstraints.NONE,
                                         new Insets(0, 4, 0, 4), 0, 0));
        // Fourth row
        panel.add(bottomValueEditor,
                  new GridBagConstraints(2, 3, 1, 1, 0.0, 0.0,
                                         GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                                         new Insets(0, 0, 0, 0), 0, 0));
        // Fifth row
        panel.add(new JLabel("Bottom"),
                  new GridBagConstraints(1, 4, 3, 1, 0.0, 0.0,
                                         GridBagConstraints.CENTER, GridBagConstraints.NONE,
                                         new Insets(0, 0, 0, 0), 0, 0));
        setLayout(new BorderLayout());
        add(panel, BorderLayout.CENTER);

    }

    /**
     * Returns the current Insets.
     *
     * @return the insets.
     */
    public Insets getInsets() {
        return new Insets(Math.abs(stringToInt(topValueEditor.getText())),
                          Math.abs(stringToInt(leftValueEditor.getText())),
                          Math.abs(stringToInt(bottomValueEditor.getText())),
                          Math.abs(stringToInt(rightValueEditor.getText())));
    }

    /**
     * Converts a string representing an integer into its numerical value.
     * If this string does not represent a valid integer value, value of 0
     * is returned.
     *
     * @param value  the string.
     *
     * @return the value.
     */
    protected int stringToInt(String value) {
        value = value.trim();
        if (value.length() == 0) {
            return 0;
        }
        else {
            try {
                return Integer.parseInt(value);
            }
            catch (NumberFormatException e) {
                return 0;
            }
        }
    }

    /**
     * Calls super removeNotify and removes all subcomponents from this panel.
     */
    public void removeNotify() {
        super.removeNotify();
        removeAll();
    }

}
