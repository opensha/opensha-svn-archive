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
 * -----------------
 * StrokeSample.java
 * -----------------
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
import java.awt.geom.*;

import javax.swing.*;

/**
 * A panel that displays a stroke sample.
 */
public class StrokeSample extends JComponent implements ListCellRenderer {

    /** The stroke being displayed. */
    protected Stroke stroke;

    /** The preferred size of the component; */
    protected Dimension preferredSize;

    /**
     * Creates a StrokeSample for the specified stroke.
     * @param stroke The sample stroke;
     */
    public StrokeSample(Stroke stroke) {
        this.stroke = stroke;
        this.preferredSize = new Dimension(80, 18);
    }

    /**
     * Returns the current Stroke object being displayed.
     */
    public Stroke getStroke() {
        return stroke;
    }

    /**
     * Sets the Stroke object being displayed.
     */
    public void setStroke(Stroke stroke) {
        this.stroke = stroke;
        this.repaint();
    }

    /**
     * Returns the preferred size of the component.
     */
    public Dimension getPreferredSize() {
        return preferredSize;
    }

    /**
     * Draws a line using the sample stroke.
     */
    public void paintComponent(Graphics g) {

        Graphics2D g2 = (Graphics2D)g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Dimension size = getSize();
        Insets insets = getInsets();
        double xx = insets.left;
        double yy = insets.top;
        double ww = size.getWidth()-insets.left-insets.right;
        double hh = size.getHeight()-insets.top-insets.bottom;

        // calculate point one
        Point2D one =  new Point2D.Double(xx+6, yy+hh/2);
        // calculate point two
        Point2D two =  new Point2D.Double(xx+ww-6, yy+hh/2);
        // draw a circle at point one
        Ellipse2D circle1 = new Ellipse2D.Double(one.getX()-5, one.getY()-5, 10, 10);
        Ellipse2D circle2 = new Ellipse2D.Double(two.getX()-6, two.getY()-5, 10, 10);

        // draw a circle at point two
        g2.draw(circle1);
        g2.fill(circle1);
        g2.draw(circle2);
        g2.fill(circle2);

        // draw a line connecting the points
        Line2D line = new Line2D.Double(one, two);
        g2.setStroke(stroke);
        g2.draw(line);

    }

    /**
     * Returns a list cell renderer for the stroke, so the sample can be displayed in a list or
     * combo.
     */
    public Component getListCellRendererComponent(JList list, Object value, int index,
                                                  boolean isSelected, boolean cellHasFocus) {
        if (value instanceof StrokeSample) {
            StrokeSample in = (StrokeSample)value;
            this.setStroke(in.getStroke());
        }
        return this;
    }

}
