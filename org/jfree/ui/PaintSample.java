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
 * ----------------
 * PaintSample.java
 * ----------------
 * (C) Copyright 2000-2003, by Simba Management Limited.
 *
 * Original Author:  David Gilbert (for Simba Management Limited);
 * Contributor(s):   -;
 *
 * $Id$
 *
 * Changes (from 26-Oct-2001)
 * --------------------------
 * 26-Oct-2001 : Changed package to com.jrefinery.ui.*;
 * 14-Oct-2002 : Fixed errors reported by Checkstyle (DG);
 *
 */

package org.jfree.ui;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Paint;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.geom.Rectangle2D;
import javax.swing.JComponent;

/**
 * A panel that displays a paint sample.
 *
 * @author David Gilbert
 */
public class PaintSample extends JComponent {

    /** The paint. */
    private Paint paint;

    /** The preferred size of the component. */
    private Dimension preferredSize;

    /**
     * Standard constructor - builds a paint sample.
     *
     * @param paint   the paint to display.
     */
    public PaintSample(Paint paint) {
        this.paint = paint;
        this.preferredSize = new Dimension(80, 12);
    }

    /**
     * Returns the current Paint object being displayed in the panel.
     *
     * @return the paint.
     */
    public Paint getPaint() {
        return paint;
    }

    /**
     * Sets the Paint object being displayed in the panel.
     *
     * @param paint  the paint.
     */
    public void setPaint(Paint paint) {
        this.paint = paint;
        repaint();
    }

    /**
     * Returns the preferred size of the component.
     *
     * @return the preferred size.
     */
    public Dimension getPreferredSize() {
        return preferredSize;
    }

    /**
     * Fills the component with the current Paint.
     *
     * @param g  the graphics device.
     */
    public void paintComponent(Graphics g) {

        Graphics2D g2 = (Graphics2D) g;
        Dimension size = getSize();
        Insets insets = getInsets();
        double xx = insets.left;
        double yy = insets.top;
        double ww = size.getWidth() - insets.left - insets.right - 1;
        double hh = size.getHeight() - insets.top - insets.bottom - 1;
        Rectangle2D area = new Rectangle2D.Double(xx, yy, ww, hh);
        g2.setPaint(paint);
        g2.fill(area);
        g2.setPaint(Color.black);
        g2.draw(area);

    }

}
