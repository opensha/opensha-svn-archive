/* =====================================================
 * JCommon : a free, general purpose, Java class library
 * =====================================================
 *
 * Project Info:  http://www.object-refinery.com/jcommon/index.html
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
 * ------------------
 * PaletteSample.java
 * ------------------
 * (C) Copyright 2002, 2003, by David M. O'Donnell.
 *
 * Original Author:  David M. O'Donnell;
 * Contributor(s):   David Gilbert (for Simba Management Limited);
 *
 * $Id$
 *
 * Changes
 * -------
 * 21-Jan-2003 : Added standard header (DG);
 *
 */

package com.jrefinery.ui;

import java.awt.BasicStroke;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.geom.Line2D;

import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.ListCellRenderer;


/**
 * A panel that displays a palette sample.
 * 
 * @author David M. O'Donnell
 */
public class PaletteSample extends JComponent implements ListCellRenderer {

    /** The palette being displayed. */
    private ColorPalette palette;

    /** The preferred size of the component; */
    private Dimension preferredSize;

    /**
     * Creates a new sample.
     * 
     * @param palette  the palette.
     */
    public PaletteSample(ColorPalette palette) {
        this.palette = palette;
        this.preferredSize = new Dimension(80, 18);
    }

    /**
     * Returns a list cell renderer for the stroke, so the sample can be displayed in a list or
     * combo.
     * 
     * @param list  the list component.
     * @param value  the value.
     * @param index  the index.
     * @param isSelected  a flag that indicates whether or not the item is selected.
     * @param cellHasFocus  a flag that indicates whether or not the cell has the focus.
     * 
     * @return The renderer.
     */
    public Component getListCellRendererComponent(JList list, Object value, int index,
                                                  boolean isSelected, boolean cellHasFocus) {
        if (value instanceof PaletteSample) {
            PaletteSample in = (PaletteSample) value;
            this.setPalette(in.getPalette());
        }
        return this;
    }

    /**
     * Returns the current palette object being displayed.
     * 
     * @return The palette.
     */
    public ColorPalette getPalette() {
        return palette;
    }

    /**
     * Returns the preferred size of the component.
     * 
     * @return The preferred size.
     */
    public Dimension getPreferredSize() {
        return preferredSize;
    }

    /**
     * Draws the sample.
     * 
     * @param g  the graphics device.
     */
    public void paintComponent(Graphics g) {

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        Dimension size = getSize();
        Insets insets = getInsets();
        double ww = size.getWidth() - insets.left - insets.right;
        double hh = size.getHeight() - insets.top - insets.bottom;

        g2.setStroke(new BasicStroke(1.0f));

        double y1 = insets.top;
        double y2 = y1 + hh;
        double xx = insets.left;
        Line2D line = new Line2D.Double();
        int count = 0;
        while (xx <= insets.left + ww) {
            count++;
            line.setLine(xx, y1, xx, y2);
            g2.setPaint(palette.getColor(count));
            g2.draw(line);
            xx += 1;
        }
    }

    /**
     * Sets the palette object being displayed.
     * 
     * @param palette  the palette.
     */
    public void setPalette(ColorPalette palette) {
        this.palette = palette;
        this.repaint();
    }
    
}
