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
 * FormatLayout.java
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
 * 26-Oct-2001 : Changed package to com.jrefinery.layout.*;
 *
 */

package com.jrefinery.layout;

import java.awt.*;
import java.io.Serializable;
import java.util.*;

/**
 * A layout manager that spaces components over six columns in seven different formats.
 */
public class FormatLayout implements LayoutManager, Serializable {

    /** A useful constant representing layout format 1. */
    public static final int C = 1;

    /** A useful constant representing layout format 2; */
    public static final int LC = 2;

    /** A useful constant representing layout format 3; */
    public static final int LCB = 3;

    /** A useful constant representing layout format 4; */
    public static final int LCLC = 4;

    /** A useful constant representing layout format 5; */
    public static final int LCLCB = 5;

    /** A useful constant representing layout format 6; */
    public static final int LCBLC = 6;

    /** A useful constant representing layout format 7; */
    public static final int LCBLCB = 7;

    /** The layout format for each row; */
    int[] rowFormats;

    /** The gap between the rows; */
    int rowGap;

    /** The gaps between the columns (gap[0] is the gap following column zero); */
    int[] columnGaps;

    /** Working array for recording the height of each row; */
    int[] rowHeights;

    /** The total height of the layout; */
    int totalHeight;

    /** Working array for recording the width of each column; */
    int[] columnWidths;

    /** The total width of the layout; */
    int totalWidth;

    /** Combined width of columns 1 and 2; */
    int columns1and2Width;

    /** Combined width of columns 4 and 5; */
    int columns4and5Width;

    /** Combined width of columns 1 to 4; */
    int columns1to4Width;

    /** Combined width of columns 1 to 5; */
    int columns1to5Width;

    /** Combined width of columns 0 to 5; */
    int columns0to5Width;

    /**
     * Constructs a new layout manager that can be used to create input forms.  The layout manager
     * works by arranging components in rows using six columns (some components will use more than
     * one column).
     * <P>
     * Any component can be added, but I think of them in terms of Labels, Components, and Buttons.
     * The formats available are:  C, LC, LCB, LCLC, LCLCB, LCBLC or LCBLCB.
     * C - 1 component in this row (spread across all six columns).
     * LC - 2 components, a label in the 1st column, and a component using the remaining 5 columns).
     * LCB - 3 components, a label in the 1st column, a component spread across the next 4, and a
     *       button in the last column.
     * LCLC - 4 components, a label in column 1, a component in 2-3, a label in 4 and a component
     *        in 5-6.
     * LCLCB - 5 components, a label in column 1, a component in 2-3, a label in 4, a component
     *         in 5 and a button in 6.
     * LCBLC - 5 components, a label in column 1, a component in 2, a button in 3, a label in 4,
     *         a component in 5-6.
     * LCBLCB - 6 components, one in each column.
     * <P>
     * Columns 1 and 4 expand to accommodate the widest label, and 3 and 6 to accommodate the
     * widest button.
     * <P>
     * Each row will contain the number of components indicated by the format.  Be sure to
     * specify enough row formats to cover all the components you add to the layout.
     *
     * @param rowCount The number of rows.
     * @param rowFormats The row formats.
     */
    public FormatLayout(int rowCount, int[] rowFormats) {

        this.rowFormats = rowFormats;
        rowGap = 2;
        columnGaps = new int[5];
        columnGaps[0] = 10;
        columnGaps[1] = 5;
        columnGaps[2] = 5;
        columnGaps[3] = 10;
        columnGaps[4] = 5;

        // working structures...
        rowHeights = new int[rowCount];
        columnWidths = new int[6];
    }

    /**
     * Returns the preferred size of the component using this layout manager.
     */
    public Dimension preferredLayoutSize(Container parent) {

        Component c0, c1, c2, c3, c4, c5;

        synchronized (parent.getTreeLock()) {
            Insets insets = parent.getInsets();
            int componentIndex = 0;
            int rowCount = rowHeights.length;
            for (int i=0; i<columnWidths.length; i++) columnWidths[i]=0;
            columns1and2Width=0;
            columns4and5Width=0;
            columns1to4Width=0;
            columns1to5Width=0;
            columns0to5Width=0;

            totalHeight = 0;
            for (int rowIndex=0; rowIndex<rowCount; rowIndex++) {
            int format = rowFormats[rowIndex % rowFormats.length];
                switch (format) {
                    case FormatLayout.C:
                        c0 = parent.getComponent(componentIndex);
                        updateC(rowIndex, c0.getPreferredSize());
                        componentIndex = componentIndex+1;
                        break;
                    case FormatLayout.LC:
                        c0 = parent.getComponent(componentIndex);
                        c1 = parent.getComponent(componentIndex+1);
                        updateLC(rowIndex, c0.getPreferredSize(), c1.getPreferredSize());
                        componentIndex = componentIndex+2;
                        break;
                    case FormatLayout.LCB:
                        c0 = parent.getComponent(componentIndex);
                        c1 = parent.getComponent(componentIndex+1);
                        c2 = parent.getComponent(componentIndex+2);
                        updateLCB(rowIndex, c0.getPreferredSize(), c1.getPreferredSize(),
                                            c2.getPreferredSize());
                        componentIndex = componentIndex+3;
                        break;
                    case FormatLayout.LCLC:
                        c0 = parent.getComponent(componentIndex);
                        c1 = parent.getComponent(componentIndex+1);
                        c2 = parent.getComponent(componentIndex+2);
                        c3 = parent.getComponent(componentIndex+3);
                        updateLCLC(rowIndex, c0.getPreferredSize(), c1.getPreferredSize(),
                                            c2.getPreferredSize(), c3.getPreferredSize());
                        componentIndex = componentIndex+4;
                        break;
                    case FormatLayout.LCBLC:
                        c0 = parent.getComponent(componentIndex);
                        c1 = parent.getComponent(componentIndex+1);
                        c2 = parent.getComponent(componentIndex+2);
                        c3 = parent.getComponent(componentIndex+3);
                        c4 = parent.getComponent(componentIndex+4);
                        updateLCBLC(rowIndex, c0.getPreferredSize(), c1.getPreferredSize(),
                                            c2.getPreferredSize(), c3.getPreferredSize(),
                                            c4.getPreferredSize());
                        componentIndex = componentIndex+5;
                        break;
                    case FormatLayout.LCLCB:
                        c0 = parent.getComponent(componentIndex);
                        c1 = parent.getComponent(componentIndex+1);
                        c2 = parent.getComponent(componentIndex+2);
                        c3 = parent.getComponent(componentIndex+3);
                        c4 = parent.getComponent(componentIndex+4);
                        updateLCLCB(rowIndex, c0.getPreferredSize(), c1.getPreferredSize(),
                                            c2.getPreferredSize(), c3.getPreferredSize(),
                                            c4.getPreferredSize());
                        componentIndex = componentIndex+5;
                        break;
                    case FormatLayout.LCBLCB:
                        c0 = parent.getComponent(componentIndex);
                        c1 = parent.getComponent(componentIndex+1);
                        c2 = parent.getComponent(componentIndex+2);
                        c3 = parent.getComponent(componentIndex+3);
                        c4 = parent.getComponent(componentIndex+4);
                        c5 = parent.getComponent(componentIndex+5);
                        updateLCBLCB(rowIndex, c0.getPreferredSize(), c1.getPreferredSize(),
                            c2.getPreferredSize(), c3.getPreferredSize(),
                                            c4.getPreferredSize(), c5.getPreferredSize());
                        componentIndex = componentIndex+6;
                        break;
                }
            }
            complete();
            return new Dimension(totalWidth+insets.left+insets.right,
                 totalHeight+(rowCount-1)*rowGap+insets.top+insets.bottom);
        }
    }

    /**
     * Returns the minimum size of the component using this layout manager.
     */
    public Dimension minimumLayoutSize(Container parent) {
        Component c0, c1, c2, c3, c4, c5;

        synchronized (parent.getTreeLock()) {
            Insets insets = parent.getInsets();
            int componentIndex = 0;
            int rowCount = rowHeights.length;
            for (int i=0; i<columnWidths.length; i++) columnWidths[i]=0;
            columns1and2Width=0;
            columns4and5Width=0;
            columns1to4Width=0;
            columns1to5Width=0;
            columns0to5Width=0;
            int totalHeight = 0;
            for (int rowIndex=0; rowIndex<rowCount; rowIndex++) {

                int format = rowFormats[rowIndex % rowFormats.length];

                switch (format) {
                    case FormatLayout.C:
                        c0 = parent.getComponent(componentIndex);
                        this.columns0to5Width = Math.max(columns0to5Width,
                                                         c0.getMinimumSize().width);
                        componentIndex = componentIndex+1;
                        break;
                    case FormatLayout.LC:
                        c0 = parent.getComponent(componentIndex);
                        c1 = parent.getComponent(componentIndex+1);
                        updateLC(rowIndex, c0.getMinimumSize(), c1.getMinimumSize());
                        componentIndex = componentIndex+2;
                        break;
                    case FormatLayout.LCB:
                        c0 = parent.getComponent(componentIndex);
                        c1 = parent.getComponent(componentIndex+1);
                        c2 = parent.getComponent(componentIndex+2);
                        updateLCB(rowIndex, c0.getMinimumSize(), c1.getMinimumSize(),
                                            c2.getMinimumSize());
                        componentIndex = componentIndex+3;
                        break;
                    case FormatLayout.LCLC:
                        c0 = parent.getComponent(componentIndex);
                        c1 = parent.getComponent(componentIndex+1);
                        c2 = parent.getComponent(componentIndex+2);
                        c3 = parent.getComponent(componentIndex+3);
                        updateLCLC(rowIndex, c0.getMinimumSize(), c1.getMinimumSize(),
                                            c2.getMinimumSize(), c3.getMinimumSize());
                        componentIndex = componentIndex+3;
                        break;
                    case FormatLayout.LCBLC:
                        c0 = parent.getComponent(componentIndex);
                        c1 = parent.getComponent(componentIndex+1);
                        c2 = parent.getComponent(componentIndex+2);
                        c3 = parent.getComponent(componentIndex+3);
                        c4 = parent.getComponent(componentIndex+4);
                        updateLCBLC(rowIndex, c0.getMinimumSize(), c1.getMinimumSize(),
                                            c2.getMinimumSize(), c3.getMinimumSize(),
                                            c4.getMinimumSize());
                        componentIndex = componentIndex+4;
                        break;
                    case FormatLayout.LCLCB:
                        c0 = parent.getComponent(componentIndex);
                        c1 = parent.getComponent(componentIndex+1);
                        c2 = parent.getComponent(componentIndex+2);
                        c3 = parent.getComponent(componentIndex+3);
                        c4 = parent.getComponent(componentIndex+4);
                        updateLCLCB(rowIndex, c0.getMinimumSize(), c1.getMinimumSize(),
                                            c2.getMinimumSize(), c3.getMinimumSize(),
                                            c4.getMinimumSize());
                        componentIndex = componentIndex+4;
                        break;
                    case FormatLayout.LCBLCB:
                        c0 = parent.getComponent(componentIndex);
                        c1 = parent.getComponent(componentIndex+1);
                        c2 = parent.getComponent(componentIndex+2);
                        c3 = parent.getComponent(componentIndex+3);
                        c4 = parent.getComponent(componentIndex+4);
                        c5 = parent.getComponent(componentIndex+5);
                        updateLCBLCB(rowIndex, c0.getMinimumSize(), c1.getMinimumSize(),
                                            c2.getMinimumSize(), c3.getMinimumSize(),
                                            c4.getMinimumSize(), c5.getMinimumSize());
                        componentIndex = componentIndex+5;
                        break;
                }
            }
            complete();
            return new Dimension(totalWidth+insets.left+insets.right,
                  totalHeight+(rowCount-1)*rowGap+insets.top+insets.bottom);
        }
    }

    /**
     * Performs the layout of the container.
     */
    public void layoutContainer(Container parent) {
        Component c0, c1, c2, c3, c4, c5;

        synchronized (parent.getTreeLock()) {
            Insets insets = parent.getInsets();
            int componentIndex = 0;
            int rowCount = rowHeights.length;
            for (int i=0; i<columnWidths.length; i++) columnWidths[i]=0;
            columns1and2Width=0;
            columns4and5Width=0;
            columns1to4Width=0;
            columns1to5Width=0;
            columns0to5Width = parent.getBounds().width-insets.left-insets.right;

            totalHeight = 0;
            for (int rowIndex=0; rowIndex<rowCount; rowIndex++) {
                int format = rowFormats[rowIndex % rowFormats.length];
                switch (format) {
                    case FormatLayout.C:
                        c0 = parent.getComponent(componentIndex);
                        updateC(rowIndex, c0.getPreferredSize());
                        componentIndex = componentIndex+1;
                        break;
                    case FormatLayout.LC:
                        c0 = parent.getComponent(componentIndex);
                        c1 = parent.getComponent(componentIndex+1);
                        updateLC(rowIndex, c0.getPreferredSize(), c1.getPreferredSize());
                        componentIndex = componentIndex+2;
                        break;
                    case FormatLayout.LCB:
                        c0 = parent.getComponent(componentIndex);
                        c1 = parent.getComponent(componentIndex+1);
                        c2 = parent.getComponent(componentIndex+2);
                        updateLCB(rowIndex, c0.getPreferredSize(), c1.getPreferredSize(),
                                            c2.getPreferredSize());
                        componentIndex = componentIndex+3;
                        break;
                    case FormatLayout.LCLC:
                        c0 = parent.getComponent(componentIndex);
                        c1 = parent.getComponent(componentIndex+1);
                        c2 = parent.getComponent(componentIndex+2);
                        c3 = parent.getComponent(componentIndex+3);
                        updateLCLC(rowIndex, c0.getPreferredSize(), c1.getPreferredSize(),
                                            c2.getPreferredSize(), c3.getPreferredSize());
                        componentIndex = componentIndex+4;
                        break;
                    case FormatLayout.LCBLC:
                        c0 = parent.getComponent(componentIndex);
                        c1 = parent.getComponent(componentIndex+1);
                        c2 = parent.getComponent(componentIndex+2);
                        c3 = parent.getComponent(componentIndex+3);
                        c4 = parent.getComponent(componentIndex+4);
                        updateLCBLC(rowIndex, c0.getPreferredSize(), c1.getPreferredSize(),
                                            c2.getPreferredSize(), c3.getPreferredSize(),
                                            c4.getPreferredSize());
                        componentIndex = componentIndex+5;
                        break;
                    case FormatLayout.LCLCB:
                        c0 = parent.getComponent(componentIndex);
                        c1 = parent.getComponent(componentIndex+1);
                        c2 = parent.getComponent(componentIndex+2);
                        c3 = parent.getComponent(componentIndex+3);
                        c4 = parent.getComponent(componentIndex+4);
                        updateLCLCB(rowIndex, c0.getPreferredSize(), c1.getPreferredSize(),
                                            c2.getPreferredSize(), c3.getPreferredSize(),
                                            c4.getPreferredSize());
                        componentIndex = componentIndex+5;
                        break;
                    case FormatLayout.LCBLCB:
                        c0 = parent.getComponent(componentIndex);
                        c1 = parent.getComponent(componentIndex+1);
                        c2 = parent.getComponent(componentIndex+2);
                        c3 = parent.getComponent(componentIndex+3);
                        c4 = parent.getComponent(componentIndex+4);
                        c5 = parent.getComponent(componentIndex+5);
                        updateLCBLCB(rowIndex, c0.getPreferredSize(), c1.getPreferredSize(),
                                            c2.getPreferredSize(), c3.getPreferredSize(),
                                            c4.getPreferredSize(), c5.getPreferredSize());
                        componentIndex = componentIndex+6;
                        break;
                }
            }
            complete();
            Dimension d = new Dimension(totalWidth+insets.left+insets.right,
                        totalHeight+(rowCount-1)*rowGap+insets.top+insets.bottom);

            componentIndex = 0;
            int rowY = insets.top;
            int[] rowX = new int[6];
            rowX[0] = insets.left;
            rowX[1] = rowX[0]+columnWidths[0]+columnGaps[0];
            rowX[2] = rowX[1]+
                        columnWidths[1]+columnGaps[1];
            rowX[3] = rowX[2]+
                        columnWidths[2]+columnGaps[2];
            rowX[4] = rowX[3]+
                        columnWidths[3]+columnGaps[3];
            rowX[5] = rowX[4]+
                        columnWidths[4]+columnGaps[4];
            int w1to2 = columnWidths[1]+columnGaps[1]+
                        columnWidths[2];
            int w4to5 = columnWidths[4]+columnGaps[4]+
                        columnWidths[5];
            int w1to4 = w1to2+columnGaps[2]+
                        columnWidths[3]+columnGaps[3]+
                        columnWidths[4];
            int w1to5 = w1to4+columnGaps[4]+
                        columnWidths[5];
            int w0to5 = w1to5+columnWidths[0]+columnGaps[0];
            for (int rowIndex=0; rowIndex<rowCount; rowIndex++) {
                int format = rowFormats[rowIndex % rowFormats.length];

                switch (format) {
                case FormatLayout.C:
                    c0 = parent.getComponent(componentIndex);
                    c0.setBounds(rowX[0], rowY, w0to5, c0.getPreferredSize().height);
                    componentIndex = componentIndex+1;
                    break;
                case FormatLayout.LC:
                    c0 = parent.getComponent(componentIndex);
                    c0.setBounds(rowX[0],
                                 rowY+(rowHeights[rowIndex]-c0.getPreferredSize().height)/2,
                    columnWidths[0], c0.getPreferredSize().height);
                    c1 = parent.getComponent(componentIndex+1);
                    c1.setBounds(rowX[1],
                                 rowY+(rowHeights[rowIndex]-c1.getPreferredSize().height)/2,
                        w1to5, c1.getPreferredSize().height);
                    componentIndex = componentIndex+2;
                    break;
                case FormatLayout.LCB:
                    c0 = parent.getComponent(componentIndex);
                    c0.setBounds(rowX[0],
                                 rowY+(rowHeights[rowIndex]-c0.getPreferredSize().height)/2,
                                 columnWidths[0], c0.getPreferredSize().height);
                    c1 = parent.getComponent(componentIndex+1);
                    c1.setBounds(rowX[1],
                                 rowY+(rowHeights[rowIndex]-c1.getPreferredSize().height)/2,
                                 w1to4, c1.getPreferredSize().height);
                    c2 = parent.getComponent(componentIndex+2);
                    c2.setBounds(rowX[5],
                                 rowY+(rowHeights[rowIndex]-c2.getPreferredSize().height)/2,
                                 columnWidths[5], c2.getPreferredSize().height);
                    componentIndex = componentIndex+3;
                    break;
                case FormatLayout.LCLC:
                    c0 = parent.getComponent(componentIndex);
                    c0.setBounds(rowX[0],
                                 rowY+(rowHeights[rowIndex]-c0.getPreferredSize().height)/2,
                                 columnWidths[0], c0.getPreferredSize().height);
                    c1 = parent.getComponent(componentIndex+1);
                    c1.setBounds(rowX[1],
                                 rowY+(rowHeights[rowIndex]-c1.getPreferredSize().height)/2,
                                 w1to2, c1.getPreferredSize().height);
                    c2 = parent.getComponent(componentIndex+2);
                    c2.setBounds(rowX[3],
                                 rowY+(rowHeights[rowIndex]-c2.getPreferredSize().height)/2,
                                 columnWidths[3], c2.getPreferredSize().height);
                    c3 = parent.getComponent(componentIndex+3);
                    c3.setBounds(rowX[4],
                                 rowY+(rowHeights[rowIndex]-c3.getPreferredSize().height)/2,
                                 w4to5, c3.getPreferredSize().height);
                    componentIndex = componentIndex+4;
                    break;
                case FormatLayout.LCBLC:
                    c0 = parent.getComponent(componentIndex);
                    c0.setBounds(rowX[0],
                                 rowY+(rowHeights[rowIndex]-c0.getPreferredSize().height)/2,
                                 columnWidths[0], c0.getPreferredSize().height);
                    c1 = parent.getComponent(componentIndex+1);
                    c1.setBounds(rowX[1],
                                 rowY+(rowHeights[rowIndex]-c1.getPreferredSize().height)/2,
                                 columnWidths[1], c1.getPreferredSize().height);
                    c2 = parent.getComponent(componentIndex+2);
                    c2.setBounds(rowX[2],
                                 rowY+(rowHeights[rowIndex]-c2.getPreferredSize().height)/2,
                                 columnWidths[2], c2.getPreferredSize().height);
                    c3 = parent.getComponent(componentIndex+3);
                    c3.setBounds(rowX[3],
                                 rowY+(rowHeights[rowIndex]-c3.getPreferredSize().height)/2,
                                 columnWidths[3], c3.getPreferredSize().height);
                    c4 = parent.getComponent(componentIndex+4);
                    c4.setBounds(rowX[4],
                                 rowY+(rowHeights[rowIndex]-c4.getPreferredSize().height)/2,
                                 w4to5, c4.getPreferredSize().height);
                    componentIndex = componentIndex+5;
                    break;
                case FormatLayout.LCLCB:
                    c0 = parent.getComponent(componentIndex);
                    c0.setBounds(rowX[0],
                                 rowY+(rowHeights[rowIndex]-c0.getPreferredSize().height)/2,
                                 columnWidths[0], c0.getPreferredSize().height);
                    c1 = parent.getComponent(componentIndex+1);
                    c1.setBounds(rowX[1],
                                 rowY+(rowHeights[rowIndex]-c1.getPreferredSize().height)/2,
                                 w1to2, c1.getPreferredSize().height);
                    c2 = parent.getComponent(componentIndex+2);
                    c2.setBounds(rowX[3],
                                 rowY+(rowHeights[rowIndex]-c2.getPreferredSize().height)/2,
                                 columnWidths[3], c2.getPreferredSize().height);
                    c3 = parent.getComponent(componentIndex+3);
                    c3.setBounds(rowX[4],
                                 rowY+(rowHeights[rowIndex]-c3.getPreferredSize().height)/2,
                                 columnWidths[4], c3.getPreferredSize().height);
                    c4 = parent.getComponent(componentIndex+4);
                    c4.setBounds(rowX[5],
                                 rowY+(rowHeights[rowIndex]-c4.getPreferredSize().height)/2,
                                 columnWidths[5], c4.getPreferredSize().height);
                    componentIndex = componentIndex+5;
                    break;
                case FormatLayout.LCBLCB:
                    c0 = parent.getComponent(componentIndex);
                    c0.setBounds(rowX[0],
                                 rowY+(rowHeights[rowIndex]-c0.getPreferredSize().height)/2,
                                 columnWidths[0], c0.getPreferredSize().height);
                    c1 = parent.getComponent(componentIndex+1);
                    c1.setBounds(rowX[1],
                                 rowY+(rowHeights[rowIndex]-c1.getPreferredSize().height)/2,
                                 columnWidths[1], c1.getPreferredSize().height);
                    c2 = parent.getComponent(componentIndex+2);
                    c2.setBounds(rowX[2],
                                 rowY+(rowHeights[rowIndex]-c2.getPreferredSize().height)/2,
                                 columnWidths[2], c2.getPreferredSize().height);
                    c3 = parent.getComponent(componentIndex+3);
                    c3.setBounds(rowX[3],
                                 rowY+(rowHeights[rowIndex]-c3.getPreferredSize().height)/2,
                                 columnWidths[3], c3.getPreferredSize().height);
                    c4 = parent.getComponent(componentIndex+4);
                    c4.setBounds(rowX[4],
                                 rowY+(rowHeights[rowIndex]-c4.getPreferredSize().height)/2,
                                 columnWidths[4], c4.getPreferredSize().height);
                    c5 = parent.getComponent(componentIndex+5);
                    c5.setBounds(rowX[5],
                                 rowY+(rowHeights[rowIndex]-c5.getPreferredSize().height)/2,
                                 columnWidths[5], c5.getPreferredSize().height);
                    componentIndex = componentIndex+6;
                    break;
                    }
                rowY = rowY + rowHeights[rowIndex]+rowGap;
            }
        }
    }

    /**
     * Processes a row in 'C' format;
     */
    protected void updateC(int rowIndex, Dimension d0) {
        rowHeights[rowIndex] = d0.height;
        totalHeight = totalHeight+rowHeights[rowIndex];
        columns0to5Width = Math.max(columns0to5Width, d0.width);
    }

    /**
     * Processes a row in 'LC' format;
     */
    protected void updateLC(int rowIndex, Dimension d0, Dimension d1) {
        rowHeights[rowIndex] = Math.max(d0.height, d1.height);
        totalHeight = totalHeight+rowHeights[rowIndex];
        columnWidths[0] = Math.max(columnWidths[0], d0.width);
        columns1to5Width = Math.max(columns1to5Width, d1.width);
    }

    /**
     * Processes a row in 'LCB' format;
     */
    protected void updateLCB(int rowIndex, Dimension d0, Dimension d1, Dimension d2) {
        rowHeights[rowIndex] = Math.max(d0.height, Math.max(d1.height, d2.height));
        totalHeight = totalHeight+rowHeights[rowIndex];
        columnWidths[0] = Math.max(columnWidths[0], d0.width);
        columns1to4Width = Math.max(columns1to4Width, d1.width);
        columnWidths[5] = Math.max(columnWidths[5], d2.width);
    }

    /**
     * Processes a row in 'LCLC' format;
     */
    protected void updateLCLC(int rowIndex, Dimension d0, Dimension d1,
                                            Dimension d2, Dimension d3) {
        rowHeights[rowIndex] = Math.max(Math.max(d0.height, d1.height),
                                        Math.max(d2.height, d3.height));
        totalHeight = totalHeight+rowHeights[rowIndex];
        columnWidths[0] = Math.max(columnWidths[0], d0.width);
        columns1and2Width = Math.max(columns1and2Width, d1.width);
        columnWidths[3] = Math.max(columnWidths[3], d2.width);
        columns4and5Width = Math.max(columns4and5Width, d3.width);
    }

    /**
     * Processes a row in 'LCBLC' format;
     */
    protected void updateLCBLC(int rowIndex, Dimension d0, Dimension d1,
                                             Dimension d2, Dimension d3, Dimension d4) {
        rowHeights[rowIndex] = (Math.max(d0.height,
                                         Math.max(Math.max(d1.height, d2.height),
                                                  Math.max(d3.height, d4.height))));
        totalHeight = totalHeight+rowHeights[rowIndex];
        columnWidths[0] = Math.max(columnWidths[0], d0.width);
        columnWidths[1] = Math.max(columnWidths[1], d1.width);
        columnWidths[2] = Math.max(columnWidths[2], d2.width);
        columnWidths[3] = Math.max(columnWidths[3], d3.width);
        columns4and5Width = Math.max(columns4and5Width, d4.width);
    }

    /**
     * Processes a row in 'LCLCB' format;
     */
    protected void updateLCLCB(int rowIndex, Dimension d0, Dimension d1, Dimension d2,
                                             Dimension d3, Dimension d4) {
        rowHeights[rowIndex] = (Math.max(d0.height,
                                         Math.max(Math.max(d1.height, d2.height),
                                                  Math.max(d3.height, d4.height))));
        totalHeight = totalHeight+rowHeights[rowIndex];
        columnWidths[0] = Math.max(columnWidths[0], d0.width);
        columns1and2Width = Math.max(columns1and2Width, d1.width);
        columnWidths[3] = Math.max(columnWidths[3], d2.width);
        columnWidths[4] = Math.max(columnWidths[4], d3.width);
        columnWidths[5] = Math.max(columnWidths[5], d4.width);
    }

    /**
     * Processes a row in 'LCBLCB' format;
     */
    protected void updateLCBLCB(int rowIndex, Dimension d0, Dimension d1, Dimension d2,
                                              Dimension d3, Dimension d4, Dimension d5) {
        rowHeights[rowIndex] = Math.max(Math.max(d0.height, d1.height),
                                        Math.max(Math.max(d2.height, d3.height),
                                                 Math.max(d4.height, d5.height)));
        totalHeight = totalHeight+rowHeights[rowIndex];
        columnWidths[0] = Math.max(columnWidths[0], d0.width);
        columnWidths[1] = Math.max(columnWidths[1], d1.width);
        columnWidths[2] = Math.max(columnWidths[2], d2.width);
        columnWidths[3] = Math.max(columnWidths[3], d3.width);
        columnWidths[4] = Math.max(columnWidths[4], d4.width);
        columnWidths[5] = Math.max(columnWidths[5], d5.width);
    }

    /**
     * Finishes of the processing;
     */
    public void complete() {
        columnWidths[1] = Math.max(columnWidths[1],
                                   columns1and2Width-columnGaps[1]-columnWidths[2]);
        columnWidths[4] = Math.max(columnWidths[4],
                            Math.max(columns4and5Width-columnGaps[4]-columnWidths[5],
                              Math.max(columns1to4Width-columnGaps[1]-columnGaps[2]-columnGaps[3]
                                                   -columnWidths[1]-columnWidths[2]-columnWidths[3],
                                     columns1to5Width-columnGaps[1]-columnGaps[2]-columnGaps[3]
                                                   -columnWidths[1]-columnWidths[2]-columnWidths[3]
                                                   -columnGaps[4])));
        int leftWidth = columnWidths[0]+columnGaps[0]+
                        columnWidths[1]+columnGaps[1]+
                        columnWidths[2];
        int rightWidth = columnWidths[3]+columnGaps[3]+
                        columnWidths[4]+columnGaps[4]+
                        columnWidths[5];

        if (splitLayout()) {
            if (leftWidth>rightWidth) {
                int mismatch = leftWidth-rightWidth;
                columnWidths[4] = columnWidths[4]+mismatch;
                rightWidth = rightWidth+mismatch;
            }
            else {
                int mismatch = rightWidth-leftWidth;
                columnWidths[1] = columnWidths[1]+mismatch;
                leftWidth = leftWidth+mismatch;
            }
        }

        this.totalWidth=leftWidth+columnGaps[2]+rightWidth;

        if (columns0to5Width>totalWidth) {
            int spaceToAdd = (columns0to5Width-totalWidth);
            if (splitLayout()) {
                int halfSpaceToAdd = spaceToAdd/2;
                columnWidths[1] = columnWidths[1]+halfSpaceToAdd;
                columnWidths[4] = columnWidths[4]+spaceToAdd-halfSpaceToAdd;
                totalWidth = totalWidth+spaceToAdd;
            }
            else {
                columnWidths[1] = columnWidths[1]+spaceToAdd;
                totalWidth = totalWidth+spaceToAdd;
            }
        }

    }

    /**
     * Returns true if this layout involves a split into two sections;
     */
    private boolean splitLayout() {
        for (int i=0; i<this.rowFormats.length; i++) {
            if (rowFormats[i]>this.LCB) return true;
        }
        return false;
    }

    /**
     * Not used.
     */
    public void addLayoutComponent(Component component) {
    }

    /**
     * Not used.
     */
    public void removeLayoutComponent(Component component) {
    }

    /**
     * Not used.
     */
    public void addLayoutComponent(String name, Component component) {
    }

    /**
     * Not used.
     */
    public void removeLayoutComponent(String name, Component component) {
    }

}


