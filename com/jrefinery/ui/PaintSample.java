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
 * ----------------
 * PaintSample.java
 * ----------------
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
 * A panel that displays a paint sample.
 */
public class PaintSample extends JComponent {

    /** The paint; */
    protected Paint paint;

    /** The preferred size of the component; */
    protected Dimension preferredSize;

    /**
     * Standard constructor - builds a paint sample.
     * @param paint The paint to display;
     */
    public PaintSample(Paint paint) {
        this.paint = paint;
        this.preferredSize = new Dimension(80, 12);
    }

    /**
     * Returns the current Paint object being displayed in the panel.
     */
    public Paint getPaint() {
        return paint;
    }

    /**
     * Sets the Paint object being displayed in the panel.
     */
    public void setPaint(Paint paint) {
        this.paint = paint;
        this.repaint();
    }

    /**
     * Returns the preferred size of the component.
     */
    public Dimension getPreferredSize() {
        return preferredSize;
    }

    /**
     * Fills the component with the current Paint.
     */
    public void paintComponent(Graphics g) {

        Graphics2D g2 = (Graphics2D)g;
        Dimension size = getSize();
        Insets insets = getInsets();
        double xx = insets.left;
        double yy = insets.top;
        double ww = size.getWidth()-insets.left-insets.right-1;
        double hh = size.getHeight()-insets.top-insets.bottom-1;
        Rectangle2D area = new Rectangle2D.Double(xx, yy, ww, hh);
        g2.setPaint(paint);
        g2.fill(area);
        g2.setPaint(Color.black);
        g2.draw(area);

    }

}