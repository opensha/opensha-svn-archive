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
 * ---------------------
 * FontChooserPanel.java
 * ---------------------
 * (C) Copyright 2000-2003, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   Arnaud Lelievre;
 *
 * $Id$
 *
 * Changes (from 26-Oct-2001)
 * --------------------------
 * 26-Oct-2001 : Changed package to com.jrefinery.ui.*;
 * 14-Oct-2002 : Fixed errors reported by Checkstyle (DG);
 * 08-Sep-2003 : Added internationalization via use of properties resourceBundle (RFE 690236) (AL);
 *
 */

package org.jfree.ui;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 * A panel for choosing a font from the available system fonts - still a bit of a hack at the
 * moment, but good enough for demonstration applications.
 *
 * @author David Gilbert
 */
public class FontChooserPanel extends JPanel {

    /** The font sizes that can be selected. */
    public static final String[] SIZES = {"9", "10", "11", "12", "14", "16", "18",
                                          "20", "22", "24", "28", "36", "48", "72"};

    /** The list of fonts. */
    private JList fontlist;

    /** The list of sizes. */
    private JList sizelist;

    /** The checkbox that indicates whether the font is bold. */
    private JCheckBox bold;

    /** The checkbox that indicates whether or not the font is italic. */
    private JCheckBox italic;

    /** The resourceBundle for the localization. */
    static protected ResourceBundle localizationResources = 
                                    ResourceBundle.getBundle("org.jfree.ui.LocalizationBundle");

    /**
     * Standard constructor - builds a FontChooserPanel initialised with the specified font.
     *
     * @param font  the initial font to display.
     */
    public FontChooserPanel(Font font) {

        GraphicsEnvironment g = GraphicsEnvironment.getLocalGraphicsEnvironment();
        String[] fonts = g.getAvailableFontFamilyNames();

        setLayout(new BorderLayout());
        JPanel right = new JPanel(new BorderLayout());

        JPanel fontPanel = new JPanel(new BorderLayout());
        fontPanel.setBorder(BorderFactory.createTitledBorder(
                            BorderFactory.createEtchedBorder(), 
                            localizationResources.getString("Font")));
        fontlist = new JList(fonts);
        JScrollPane fontpane = new JScrollPane(fontlist);
        fontpane.setBorder(BorderFactory.createEtchedBorder());
        fontPanel.add(fontpane);
        add(fontPanel);

        JPanel sizePanel = new JPanel(new BorderLayout());
        sizePanel.setBorder(BorderFactory.createTitledBorder(
                            BorderFactory.createEtchedBorder(), 
                            localizationResources.getString("Size")));
        sizelist = new JList(SIZES);
        JScrollPane sizepane = new JScrollPane(sizelist);
        sizepane.setBorder(BorderFactory.createEtchedBorder());
        sizePanel.add(sizepane);

        JPanel attributes = new JPanel(new GridLayout(1, 2));
        bold = new JCheckBox(localizationResources.getString("Bold"));
        italic = new JCheckBox(localizationResources.getString("Italic"));
        attributes.add(bold);
        attributes.add(italic);
        attributes.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                             localizationResources.getString("Attributes")));

        right.add(sizePanel, BorderLayout.CENTER);
        right.add(attributes, BorderLayout.SOUTH);

        add(right, BorderLayout.EAST);
    }

    /**
     * Returns a Font object representing the selection in the panel.
     *
     * @return the font.
     */
    public Font getSelectedFont() {
        return new Font(getSelectedName(), getSelectedStyle(), getSelectedSize());
    }

    /**
     * Returns the selected name.
     *
     * @return the name.
     */
    public String getSelectedName() {
        return (String) fontlist.getSelectedValue();
    }

    /**
     * Returns the selected style.
     *
     * @return the style.
     */
    public int getSelectedStyle() {
        if (bold.isSelected() && italic.isSelected()) {
            return Font.BOLD + Font.ITALIC;
        }
        if (bold.isSelected()) {
            return Font.BOLD;
        }
        if (italic.isSelected()) {
            return Font.ITALIC;
        }
        else {
            return Font.PLAIN;
        }
    }

    /**
     * Returns the selected size.
     *
     * @return the size.
     */
    public int getSelectedSize() {
        String selected = (String) sizelist.getSelectedValue();
        if (selected != null) {
            return Integer.parseInt(selected);
        }
        else {
            return 10;
        }
    }

}
