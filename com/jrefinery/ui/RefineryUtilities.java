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
 * ----------------------
 * RefineryUtilities.java
 * ----------------------
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
 * 26-Nov-2001 : Changed name to SwingRefinery.java to make it obvious that this is not part of
 *               the Java APIs (DG);
 * 10-Dec-2001 : Changed name (again) to JRefineryUtilities.java (DG);
 * 28-Feb-2002 : Moved system properties classes into com.jrefinery.ui.about (DG);
 * 19-Apr-2002 : Renamed JRefineryUtilities-->RefineryUtilities.  Added drawRotatedString(...)
 *               method (DG);
 * 21-May-2002 : Changed frame positioning methods to accept Window parameters, as suggested by
 *               Laurence Vanhelsuwe (DG);
 * 27-May-2002 : Added getPointInRectangle method (DG);
 *
 */

package com.jrefinery.ui;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Point2D;
import java.awt.Container;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.Dialog;
import javax.swing.JFrame;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.table.TableModel;
import javax.swing.table.TableColumn;

/**
 * A collection of utility methods relating to user interfaces.
 */
public class RefineryUtilities {

    /**
     * Positions the specified frame in the middle of the screen.
     *
     * @param frame The frame to be centered on the screen.
     */
    public static void centerFrameOnScreen(Window frame) {
        positionFrameOnScreen(frame, 0.5, 0.5);
    }

    /**
     * Positions the specified frame at a relative position in the screen, where 50% is considered
     * to be the center of the screen.
     *
     * @param frame The frame.
     * @param horizontalPercent The relative horizontal position of the frame (0.0 to 1.0, where 0.5
     *                          is the center of the screen).
     * @param verticalPercent The relative vertical position of the frame (0.0 to 1.0, where 0.5 is
     *                        the center of the screen).
     */
    public static void positionFrameOnScreen(Window frame,
                                             double horizontalPercent,
                                             double verticalPercent) {

        Dimension s = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension f = frame.getSize();
        int w = Math.max(s.width-f.width, 0);
        int h = Math.max(s.height-f.height, 0);
        int x = (int) (horizontalPercent*w);
        int y = (int) (verticalPercent*h);
        frame.setBounds(x, y, f.width, f.height);

    }

    /**
     * Positions the specified frame at a random location on the screen while ensuring that the
     * entire frame is visible (provided that the frame is smaller than the screen).
     *
     * @param frame The frame.
     */
    public static void positionFrameRandomly(Window frame) {

        positionFrameOnScreen(frame, Math.random(), Math.random());

    }

    /**
     * Positions the specified dialog within its parent.
     *
     * @param dialog The dialog to be positioned on the screen.
     */
    public static void centerDialogInParent(Dialog dialog) {
        positionDialogRelativeToParent(dialog, 0.5, 0.5);
    }

    /**
     * Positions the specified dialog at a position relative to its parent.
     *
     * @param dialog The dialog to be positioned.
     * @param horizontalPercent.
     * @param verticalPercent.
     */
    public static void positionDialogRelativeToParent(Dialog dialog, double horizontalPercent,
                                                      double verticalPercent) {
        Dimension d = dialog.getSize();
        Container parent = dialog.getParent();
        Dimension p = parent.getSize();

        int baseX = parent.getX()-d.width;
        int baseY = parent.getY()-d.height;
        int w = d.width + p.width;
        int h = d.height + p.height;
        int x = baseX + (int) (horizontalPercent*w);
        int y = baseY + (int) (verticalPercent*h);

        // make sure the dialog fits completely on the screen...
        Dimension s = Toolkit.getDefaultToolkit().getScreenSize();
        x = Math.min(x, (s.width-d.width));
        x = Math.max(x, 0);
        y = Math.min(y, (s.height-d.height));
        y = Math.max(y, 0);

        dialog.setBounds(x, y, d.width, d.height);

    }

    /**
     * Creates a panel that contains a table based on the specified table model.
     *
     * @param model The table model to use when constructing the table.
     */
    public static JPanel createTablePanel(TableModel model) {

        JPanel panel = new JPanel(new BorderLayout());
        JTable table = new JTable(model);
        for (int columnIndex=0; columnIndex<model.getColumnCount(); columnIndex++) {
            TableColumn column = table.getColumnModel().getColumn(columnIndex);
            Class c = model.getColumnClass(columnIndex);
            if (c.equals(Number.class)) {
                column.setCellRenderer(new NumberCellRenderer());
            }
        }
        panel.add(new JScrollPane(table));
        return panel;

    }

    /**
     * Creates a label with a specific font.
     *
     * @param text The text for the label.
     * @param font The font.
     */
    public static JLabel createJLabel(String text, Font font) {

        JLabel result = new JLabel(text);
        result.setFont(font);
        return result;

    }

    /**
     * Creates a label with a specific font and color.
     *
     * @param text The text for the label.
     * @param font The font.
     * @param color The color.
     */
    public static JLabel createJLabel(String text, Font font, Color color) {

        JLabel result = new JLabel(text);
        result.setFont(font);
        result.setForeground(color);
        return result;

    }

    public static JButton createJButton(String label, Font font) {

        JButton result = new JButton(label);
        result.setFont(font);
        return result;

    }

    /**
     * A utility method for drawing rotated text.
     * <P>
     * A common rotation is -Math.PI/2 which draws text 'vertically' (with the top of the
     * characters on the left).
     *
     * @param text The text.
     * @param g2 The graphics device.
     * @param x The x-coordinate.
     * @param y The y-coordinate.
     * @param rotation The clockwise rotation (in radians).
     */
    public static void drawRotatedString(String text, Graphics2D g2,
                                         float x, float y, double rotation) {

        AffineTransform saved = g2.getTransform();

        // apply the rotation...
        AffineTransform rotate = AffineTransform.getRotateInstance(rotation, x, y);
        g2.transform(rotate);
        g2.drawString(text, x, y);

        g2.setTransform(saved);

    }

    /**
     * Returns a point based on (x, y) but constrained to be within the bounds of a given
     * rectangle.
     *
     * @param x The x-coordinate.
     * @param y The y-coordinate.
     * @param area The constraining rectangle.
     *
     * @return A point within the rectangle.
     */
    public static Point2D getPointInRectangle(double x, double y, Rectangle2D area) {

        x = Math.max(area.getMinX(), Math.min(x, area.getMaxX()));
        y = Math.max(area.getMinY(), Math.min(y, area.getMaxY()));
        return new Point2D.Double(x, y);

    }

}


