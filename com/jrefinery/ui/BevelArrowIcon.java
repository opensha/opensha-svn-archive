/* ===================================================
 * JCommon : a free general purpose Java class library
 * ===================================================
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
 * -------------------
 * BevelArrowIcon.java
 * -------------------
 * (C) Copyright 2000-2003, by Nobuo Tamemasa and Contributors.
 *
 * Original Author:  Nobuo Tamemasa;
 * Contributor(s):   David Gilbert (for Simba Management Limited);
 *
 * $Id$
 *
 * Changes (from 26-Oct-2001)
 * --------------------------
 * 26-Oct-2001 : Changed package to com.jrefinery.ui.*;
 * 13-Oct-2002 : Fixed errors reported by Checkstyle (DG);
 *
 */

package com.jrefinery.ui;

import java.awt.Graphics;
import java.awt.Component;
import java.awt.Color;
import javax.swing.Icon;
import javax.swing.UIManager;

/**
 * An arrow icon that can point up or down (usually used to indicate the sort direction in a table).
 * <P>
 * This class (and also SortButtonRenderer) is based on original code by Nobuo Tamemasa (version
 * 1.0, 26-Feb-1999) posted on www.codeguru.com.
 *
 * @author Nobuo Tamemasa
 */
public class BevelArrowIcon implements Icon {

    /** Constant indicating that the arrow is pointing up. */
    public static final int UP = 0;

    /** Constant indicating that the arrow is pointing down. */
    public static final int DOWN = 1;

    /** The default arrow size. */
    private static final int DEFAULT_SIZE = 11;

    /** Edge color 1. */
    private Color edge1;

    /** Edge color 2. */
    private Color edge2;

    /** The fill color for the arrow icon. */
    private Color fill;

    /** The size of the icon. */
    private int size;

    /** The direction that the arrow is pointing (UP or DOWN); */
    private int direction;

    /**
     * Standard constructor - builds an icon with the specified attributes.
     *
     * @param direction .
     * @param isRaisedView .
     * @param isPressedView .
     */
    public BevelArrowIcon(int direction, boolean isRaisedView, boolean isPressedView) {
        if (isRaisedView) {
            if (isPressedView) {
                init(UIManager.getColor("controlLtHighlight"),
                     UIManager.getColor("controlDkShadow"),
                     UIManager.getColor("controlShadow"),
                     DEFAULT_SIZE, direction);
            }
            else {
                init(UIManager.getColor("controlHighlight"),
                     UIManager.getColor("controlShadow"),
                     UIManager.getColor("control"),
                     DEFAULT_SIZE, direction);
            }
        }
        else {
            if (isPressedView) {
                init(UIManager.getColor("controlDkShadow"),
                     UIManager.getColor("controlLtHighlight"),
                     UIManager.getColor("controlShadow"),
                     DEFAULT_SIZE, direction);
            }
            else {
                init(UIManager.getColor("controlShadow"),
                     UIManager.getColor("controlHighlight"),
                     UIManager.getColor("control"),
                     DEFAULT_SIZE, direction);
            }
        }
    }

    /**
     * Standard constructor - builds an icon with the specified attributes.
     *
     * @param edge1  the color of edge1.
     * @param edge2  the color of edge2.
     * @param fill  the fill color.
     * @param size  the size of the arrow icon.
     * @param direction  the direction that the arrow points.
     */
    public BevelArrowIcon(Color edge1, Color edge2, Color fill, int size, int direction) {
        init(edge1, edge2, fill, size, direction);
    }

    /**
     * Paints the icon at the specified position.  Supports the Icon interface.
     *
     * @param c .
     * @param g .
     * @param x .
     * @param y .
     */
    public void paintIcon(Component c, Graphics g, int x, int y) {
        switch (direction) {
            case DOWN: drawDownArrow(g, x, y); break;
            case   UP: drawUpArrow(g, x, y);   break;
        }
    }

    /**
     * Returns the width of the icon.  Supports the Icon interface.
     *
     * @return the icon width.
     */
    public int getIconWidth() {
        return size;
    }

    /**
     * Returns the height of the icon.  Supports the Icon interface.
     * @return the icon height.
     */
    public int getIconHeight() {
        return size;
    }

    /**
     * Initialises the attributes of the arrow icon.
     *
     * @param edge1  the color of edge1.
     * @param edge2  the color of edge2.
     * @param fill  the fill color.
     * @param size  the size of the arrow icon.
     * @param direction  the direction that the arrow points.
     */
    private void init(Color edge1, Color edge2, Color fill, int size, int direction) {
        this.edge1 = edge1;
        this.edge2 = edge2;
        this.fill = fill;
        this.size = size;
        this.direction = direction;
    }

    /**
     * Draws the arrow pointing down.
     *
     * @param g  the graphics device.
     * @param xo  ??
     * @param yo  ??
     */
    private void drawDownArrow(Graphics g, int xo, int yo) {
        g.setColor(edge1);
        g.drawLine(xo, yo,   xo + size - 1, yo);
        g.drawLine(xo, yo + 1, xo + size - 3, yo + 1);
        g.setColor(edge2);
        g.drawLine(xo + size - 2, yo + 1, xo + size - 1, yo + 1);
        int x = xo + 1;
        int y = yo + 2;
        int dx = size - 6;
        while (y + 1 < yo + size) {
            g.setColor(edge1);
            g.drawLine(x, y,   x + 1, y);
            g.drawLine(x, y + 1, x + 1, y + 1);
            if (0 < dx) {
                g.setColor(fill);
                g.drawLine(x + 2, y,   x + 1 + dx, y);
                g.drawLine(x + 2, y + 1, x + 1 + dx, y + 1);
            }
            g.setColor(edge2);
            g.drawLine(x + dx + 2, y,   x + dx + 3, y);
            g.drawLine(x + dx + 2, y + 1, x + dx + 3, y + 1);
            x += 1;
            y += 2;
            dx -= 2;
        }
        g.setColor(edge1);
        g.drawLine(xo + (size / 2), yo + size - 1, xo + (size / 2), yo + size - 1);
    }

    /**
     * Draws the arrow pointing up.
     *
     * @param g  the graphics device.
     * @param xo  ??
     * @param yo  ??
     */
    private void drawUpArrow(Graphics g, int xo, int yo) {
        g.setColor(edge1);
        int x = xo + (size / 2);
        g.drawLine(x, yo, x, yo);
        x--;
        int y = yo + 1;
        int dx = 0;
        while (y + 3 < yo + size) {
            g.setColor(edge1);
            g.drawLine(x, y,   x + 1, y);
            g.drawLine(x, y + 1, x + 1, y + 1);
            if (0 < dx) {
                g.setColor(fill);
                g.drawLine(x + 2, y,   x + 1 + dx, y);
                g.drawLine(x + 2, y + 1, x + 1 + dx, y + 1);
            }
            g.setColor(edge2);
            g.drawLine(x + dx + 2, y,   x + dx + 3, y);
            g.drawLine(x + dx + 2, y + 1, x + dx + 3, y + 1);
            x -= 1;
            y += 2;
            dx += 2;
        }
        g.setColor(edge1);
        g.drawLine(xo, yo + size - 3,   xo + 1, yo + size - 3);
        g.setColor(edge2);
        g.drawLine(xo + 2, yo + size - 2, xo + size - 1, yo + size - 2);
        g.drawLine(xo, yo + size - 1, xo + size, yo + size - 1);
    }

}
