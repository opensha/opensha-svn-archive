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
 * --------------
 * LCBLayout.java
 * --------------
 * (C) Copyright 2000-2002, by Simba Management Limited.
 *
 * Original Author:  David Gilbert (for Simba Management Limited);
 * Contributor(s):   -;
 *
 * $Id$
 *
 * Changes (from 26-Oct-2001)
 * --------------------------
 * 26-Oct-2001 : Changed package to com.jrefinery.layout.*;
 *
 */

package com.jrefinery.layout;

import java.io.Serializable;

import java.awt.*;

/**
 * Specialised layout manager for a grid of components.
 */
public class LCBLayout implements LayoutManager, java.io.Serializable {

    /** A constant for the number of columns in the layout; */
    private static int COLUMNS = 3;

    /** Tracks the column widths; */
    int[] colWidth;

    /** Tracks the row heights; */
    int[] rowHeight;

    /** The gap between each label and component; */
    int labelGap;

    /** The gap between each component and button; */
    int buttonGap;

    /** The gap between rows; */
    int vGap;

    /**
     * Standard constructor - builds a LCBLayout with the specified maximum number of rows.
     * @param maxrows The maximum number of rows;
     */
    public LCBLayout(int maxrows) {
        labelGap = 10;
        buttonGap = 6;
        vGap = 2;
        colWidth = new int[COLUMNS];
        rowHeight = new int[maxrows];
    }

    /**
     * Returns the preferred size using this layout manager.
     */
    public Dimension preferredLayoutSize(Container parent) {
        synchronized (parent.getTreeLock()) {
            Insets insets = parent.getInsets();
            int ncomponents = parent.getComponentCount();
            int nrows = ncomponents / COLUMNS;
            for (int c=0;c<COLUMNS;c++) {
                for (int r=0;r<nrows;r++) {
                    Component component = parent.getComponent(r*COLUMNS+c);
                    Dimension d = component.getPreferredSize();
                    if (colWidth[c]<d.width) {
                        colWidth[c]=d.width;
                    }
                    if (rowHeight[r]<d.height) {
                        rowHeight[r]=d.height;
                    }
                }
            }
            int totalHeight = vGap*(nrows-1);
            for (int r=0; r<nrows; r++) {
                totalHeight = totalHeight+rowHeight[r];
            }
            int totalWidth = colWidth[0]+labelGap+colWidth[1]+buttonGap+colWidth[2];
            return new Dimension(insets.left+insets.right+totalWidth+labelGap+buttonGap,
                                 insets.top+insets.bottom+totalHeight+vGap);
        }
    }

    /**
     * Returns the minimum size using this layout manager.
     */
    public Dimension minimumLayoutSize(Container parent) {
        synchronized (parent.getTreeLock()) {
            Insets insets = parent.getInsets();
            int ncomponents = parent.getComponentCount();
            int nrows = ncomponents / COLUMNS;
            for (int c=0;c<COLUMNS;c++) {
                for (int r=0;r<nrows;r++) {
                    Component component = parent.getComponent(r*COLUMNS+c);
                    Dimension d = component.getMinimumSize();
                    if (colWidth[c]<d.width) {
                        colWidth[c]=d.width;
                    }
                    if (rowHeight[r]<d.height) {
                        rowHeight[r]=d.height;
                    }
                }
            }
            int totalHeight = vGap*(nrows-1);
            for (int r=0; r<nrows; r++) {
                totalHeight = totalHeight+rowHeight[r];
            }
            int totalWidth = colWidth[0]+labelGap+colWidth[1]+buttonGap+colWidth[2];
            return new Dimension(insets.left+insets.right+totalWidth+labelGap+buttonGap,
                                 insets.top+insets.bottom+totalHeight+vGap);
        }
    }

    /**
     * Lays out the components.
     */
    public void layoutContainer(Container parent) {

        synchronized (parent.getTreeLock()) {
            Insets insets = parent.getInsets();
            int ncomponents = parent.getComponentCount();
            int nrows = ncomponents / COLUMNS;
            for (int c=0;c<COLUMNS;c++) {
                for (int r=0;r<nrows;r++) {
                    Component component = parent.getComponent(r*COLUMNS+c);
                    Dimension d = component.getPreferredSize();
                    if (colWidth[c]<d.width) {
                        colWidth[c]=d.width;
                    }
                    if (rowHeight[r]<d.height) {
                        rowHeight[r]=d.height;
                    }
                }
            }
            int totalHeight = vGap*(nrows-1);
            for (int r=0; r<nrows; r++) {
                totalHeight = totalHeight+rowHeight[r];
            }
            int totalWidth = colWidth[0]+colWidth[1]+colWidth[2];

            // adjust the width of the second column to use up all of parent
            int available = parent.getWidth()-insets.left-insets.right-labelGap-buttonGap;
            colWidth[1] = colWidth[1]+(available-totalWidth);

            // *** DO THE LAYOUT ***
            int x = insets.left;
            for (int c=0; c<COLUMNS; c++) {
                int y = insets.top;
                for (int r=0; r<nrows; r++) {
                    int i=r*COLUMNS+c;
                    if (i<ncomponents) {
                        Component component = parent.getComponent(i);
                        Dimension d = component.getPreferredSize();
                        int h = d.height;
                        int adjust = (rowHeight[r]-h)/2;
                        parent.getComponent(i).setBounds(x, y+adjust, colWidth[c], h);
                    }
                    y = y+rowHeight[r]+vGap;
                }
                x = x+colWidth[c];
                if (c==0) { x=x+labelGap; }
                if (c==1) { x=x+buttonGap; }
            }

        }

    }

    /**
     * Not used.
     */
    public void addLayoutComponent(Component comp) {
    }

    /**
     * Not used.
     */
    public void removeLayoutComponent(Component comp) {
    }

    /**
     * Not used.
     */
    public void addLayoutComponent(String name, Component comp) {
    }

    /**
     * Not used.
     */
    public void removeLayoutComponent(String name, Component comp) {
    }

}