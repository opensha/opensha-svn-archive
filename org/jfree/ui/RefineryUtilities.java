/* =======================================================
 * JCommon : a free general purpose class library for Java
 * =======================================================
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
 * ----------------------
 * RefineryUtilities.java
 * ----------------------
 * (C) Copyright 2000-2003, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   Jon Iles;
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
 * 26-Jun-2002 : Removed unnecessary imports (DG);
 * 12-Jul-2002 : Added workaround for rotated text (JI);
 * 14-Oct-2002 : Fixed errors reported by Checkstyle (DG);
 * 08-May-2003 : Added a new drawRotatedString(...) method (DG);
 * 09-May-2003 : Added a drawRotatedShape(...) method (DG);
 * 10-Jun-2003 : Updated aligned and rotated string methods (DG);
 *
 */

package org.jfree.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

/**
 * A collection of utility methods relating to user interfaces.
 *
 * @author David Gilbert
 */
public class RefineryUtilities {

    /** A flag that controls whether or not the rotated string workaround is used. */
    private static boolean useDrawRotatedStringWorkaround = true;

    /**
     * Sets the flag that controls whether or not the rotated string workaround is used.
     *
     * @param use  the new flag value.
     */
    public static void setUseDrawRotatedStringWorkaround(boolean use) {
        useDrawRotatedStringWorkaround = use;
    }

    /**
     * Positions the specified frame in the middle of the screen.
     *
     * @param frame  the frame to be centered on the screen.
     */
    public static void centerFrameOnScreen(Window frame) {
        positionFrameOnScreen(frame, 0.5, 0.5);
    }

    /**
     * Positions the specified frame at a relative position in the screen, where 50% is considered
     * to be the center of the screen.
     *
     * @param frame  the frame.
     * @param horizontalPercent  the relative horizontal position of the frame (0.0 to 1.0,
     *                           where 0.5 is the center of the screen).
     * @param verticalPercent  the relative vertical position of the frame (0.0 to 1.0, where
     *                         0.5 is the center of the screen).
     */
    public static void positionFrameOnScreen(Window frame,
                                             double horizontalPercent,
                                             double verticalPercent) {

        Dimension s = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension f = frame.getSize();
        int w = Math.max(s.width - f.width, 0);
        int h = Math.max(s.height - f.height, 0);
        int x = (int) (horizontalPercent * w);
        int y = (int) (verticalPercent * h);
        frame.setBounds(x, y, f.width, f.height);

    }

    /**
     * Positions the specified frame at a random location on the screen while ensuring that the
     * entire frame is visible (provided that the frame is smaller than the screen).
     *
     * @param frame  the frame.
     */
    public static void positionFrameRandomly(Window frame) {

        positionFrameOnScreen(frame, Math.random(), Math.random());

    }

    /**
     * Positions the specified dialog within its parent.
     *
     * @param dialog  the dialog to be positioned on the screen.
     */
    public static void centerDialogInParent(Dialog dialog) {
        positionDialogRelativeToParent(dialog, 0.5, 0.5);
    }

    /**
     * Positions the specified dialog at a position relative to its parent.
     *
     * @param dialog  the dialog to be positioned.
     * @param horizontalPercent  the relative location.
     * @param verticalPercent  the relative location.
     */
    public static void positionDialogRelativeToParent(Dialog dialog, double horizontalPercent,
                                                      double verticalPercent) {
        Dimension d = dialog.getSize();
        Container parent = dialog.getParent();
        Dimension p = parent.getSize();

        int baseX = parent.getX() - d.width;
        int baseY = parent.getY() - d.height;
        int w = d.width + p.width;
        int h = d.height + p.height;
        int x = baseX + (int) (horizontalPercent * w);
        int y = baseY + (int) (verticalPercent * h);

        // make sure the dialog fits completely on the screen...
        Dimension s = Toolkit.getDefaultToolkit().getScreenSize();
        x = Math.min(x, (s.width - d.width));
        x = Math.max(x, 0);
        y = Math.min(y, (s.height - d.height));
        y = Math.max(y, 0);

        dialog.setBounds(x, y, d.width, d.height);

    }

    /**
     * Creates a panel that contains a table based on the specified table model.
     *
     * @param model  the table model to use when constructing the table.
     *
     * @return the panel.
     */
    public static JPanel createTablePanel(TableModel model) {

        JPanel panel = new JPanel(new BorderLayout());
        JTable table = new JTable(model);
        for (int columnIndex = 0; columnIndex < model.getColumnCount(); columnIndex++) {
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
     * @param text  the text for the label.
     * @param font  the font.
     *
     * @return the label.
     */
    public static JLabel createJLabel(String text, Font font) {

        JLabel result = new JLabel(text);
        result.setFont(font);
        return result;

    }

    /**
     * Creates a label with a specific font and color.
     *
     * @param text  the text for the label.
     * @param font  the font.
     * @param color  the color.
     *
     * @return the label.
     */
    public static JLabel createJLabel(String text, Font font, Color color) {

        JLabel result = new JLabel(text);
        result.setFont(font);
        result.setForeground(color);
        return result;

    }

    /**
     * Creates a {@link JButton}.
     *
     * @param label  the label.
     * @param font  the font.
     *
     * @return the button.
     */
    public static JButton createJButton(String label, Font font) {

        JButton result = new JButton(label);
        result.setFont(font);
        return result;

    }

    /**
     * Draws a string such that the specified anchor point is aligned to the given (x, y)
     * location.
     *
     * @param text  the text.
     * @param g2  the graphics device.
     * @param x  the x coordinate (Java 2D).
     * @param y  the y coordinate (Java 2D).
     * @param anchor  the anchor location.
     */
    public static void drawAlignedString(String text, Graphics2D g2, float x, float y,
                                         TextAnchor anchor) {

        float[] adjust = deriveTextBoundsAnchorOffsets(g2, text, anchor);
        g2.drawString(text, (int) (x + adjust[0]), (int) (y + adjust[1]));

    }

    /**
     * Draws a string that is aligned by one anchor point and rotated about another anchor point.
     *
     * @param text  the text.
     * @param g2  the graphics device.
     * @param x  the location of the text anchor.
     * @param y  the location of the text anchor.
     * @param textAnchor  the text anchor.
     * @param rotationAnchor  the rotation anchor.
     * @param angle  the rotation angle.
     */
    public static void drawRotatedString(String text, Graphics2D g2, float x, float y,
                                         TextAnchor textAnchor,
                                         TextAnchor rotationAnchor,
                                         double angle) {

        if (text == null || text.equals("")) {
            return;
        }
        float[] textAdj = deriveTextBoundsAnchorOffsets(g2, text, textAnchor);
        float[] rotateAdj = deriveRotationAnchorOffsets(g2, text, rotationAnchor);
        drawRotatedString(text, g2, x + textAdj[0], y + textAdj[1],
                          angle, x + textAdj[0] + rotateAdj[0], y + textAdj[1] + rotateAdj[1]);


    }

    /**
     * A utility method for drawing rotated text.
     * <P>
     * A common rotation is -Math.PI/2 which draws text 'vertically' (with the top of the
     * characters on the left).
     *
     * @param text  the text.
     * @param g2  the graphics device.
     * @param x  the x-coordinate.
     * @param y  the y-coordinate.
     * @param angle  the angle of the (clockwise) rotation (in radians).
     */
    public static void drawRotatedString(String text,
                                         Graphics2D g2,
                                         float x,
                                         float y,
                                         double angle) {
        drawRotatedString(text, g2, x, y, angle, x, y);
    }

    /**
     * A utility method for drawing rotated text.
     * <P>
     * A common rotation is -Math.PI/2 which draws text 'vertically' (with the top of the
     * characters on the left).
     *
     * @param text  the text.
     * @param g2  the graphics device.
     * @param textX  the x-coordinate for the text (before rotation).
     * @param textY  the y-coordinate for the text (before rotation).
     * @param angle  the angle of the (clockwise) rotation (in radians).
     * @param rotateX  the point about which the text is rotated.
     * @param rotateY  the point about which the text is rotated.
     */
    public static void drawRotatedString(String text,
                                         Graphics2D g2,
                                         float textX,
                                         float textY,
                                         double angle,
                                         float rotateX,
                                         float rotateY) {

        if ((text == null) || (text.equals(""))) {
            return;
        }

        AffineTransform saved = g2.getTransform();

        // apply the rotation...
        AffineTransform rotate = AffineTransform.getRotateInstance(angle, rotateX, rotateY);
        g2.transform(rotate);

        if (useDrawRotatedStringWorkaround) {
            // workaround for JDC bug ID 4312117 and others...
            TextLayout tl = new TextLayout(text, g2.getFont(), g2.getFontRenderContext());
            tl.draw(g2, textX, textY);
        }
        else {
            // replaces this code...
            g2.drawString(text, textX, textY);
        }
        g2.setTransform(saved);

    }

    /**
     * A utility method that calculates the anchor offsets for a string.
     *
     * @param g2  the graphics device.
     * @param text  the text.
     * @param anchor  the anchor point.
     *
     * @return  The offsets.
     */
    private static float[] deriveTextBoundsAnchorOffsets(Graphics2D g2, String text,
                                                         TextAnchor anchor) {

        float[] result = new float[2];
        FontRenderContext frc = g2.getFontRenderContext();
        Font f = g2.getFont();
        LineMetrics metrics = f.getLineMetrics(text, frc);
        Rectangle2D bounds = g2.getFont().getStringBounds(text, frc);
        float ascent = metrics.getAscent();
        float halfAscent = ascent / 2.0f;
        float descent = metrics.getDescent();
        float leading = metrics.getLeading();
        float xAdj = 0.0f;
        float yAdj = 0.0f;

        if (anchor == TextAnchor.TOP_CENTER
                || anchor == TextAnchor.CENTER
                || anchor == TextAnchor.BOTTOM_CENTER
                || anchor == TextAnchor.BASELINE_CENTER
                || anchor == TextAnchor.HALF_ASCENT_CENTER) {
                    
            xAdj = (float) -bounds.getWidth() / 2.0f;
            
        }
        else if (anchor == TextAnchor.TOP_RIGHT
                || anchor == TextAnchor.CENTER_RIGHT
                || anchor == TextAnchor.BOTTOM_RIGHT
                || anchor == TextAnchor.BASELINE_RIGHT
                || anchor == TextAnchor.HALF_ASCENT_RIGHT) {
                    
            xAdj = (float) -bounds.getWidth();
            
        }

        if (anchor == TextAnchor.TOP_LEFT
                || anchor == TextAnchor.TOP_CENTER
                || anchor == TextAnchor.TOP_RIGHT) {
                    
            yAdj = -descent - leading + (float) bounds.getHeight();
            
        }
        else if (anchor == TextAnchor.HALF_ASCENT_LEFT
                || anchor == TextAnchor.HALF_ASCENT_CENTER
                || anchor == TextAnchor.HALF_ASCENT_RIGHT) {
                    
            yAdj = halfAscent;
            
        }
        else if (anchor == TextAnchor.CENTER_LEFT
                || anchor == TextAnchor.CENTER
                || anchor == TextAnchor.CENTER_RIGHT) {
                    
            yAdj = -descent - leading + (float) (bounds.getHeight() / 2.0);
            
        }
        else if (anchor == TextAnchor.BASELINE_LEFT
                || anchor == TextAnchor.BASELINE_CENTER
                || anchor == TextAnchor.BASELINE_RIGHT) {
                    
            yAdj = 0.0f;
            
        }
        else if (anchor == TextAnchor.BOTTOM_LEFT
                || anchor == TextAnchor.BOTTOM_CENTER
                || anchor == TextAnchor.BOTTOM_RIGHT) {
                    
            yAdj = -metrics.getDescent() - metrics.getLeading();
            
        }
        result[0] = xAdj;
        result[1] = yAdj;
        return result;

    }

    /**
     * A utility method that calculates the rotation anchor offsets for a string.  These offsets
     * are relative to the text starting coordinate (BASELINE_LEFT).
     *
     * @param g2  the graphics device.
     * @param text  the text.
     * @param anchor  the anchor point.
     *
     * @return  The offsets.
     */
    private static float[] deriveRotationAnchorOffsets(Graphics2D g2, String text,
                                                       TextAnchor anchor) {

        float[] result = new float[2];
        FontRenderContext frc = g2.getFontRenderContext();
        LineMetrics metrics = g2.getFont().getLineMetrics(text, frc);
        Rectangle2D bounds = g2.getFont().getStringBounds(text, frc);
        float ascent = metrics.getAscent();
        float halfAscent = ascent / 2.0f;
        float descent = metrics.getDescent();
        float leading = metrics.getLeading();
        float xAdj = 0.0f;
        float yAdj = 0.0f;

        if (anchor == TextAnchor.TOP_LEFT
                || anchor == TextAnchor.CENTER_LEFT
                || anchor == TextAnchor.BOTTOM_LEFT
                || anchor == TextAnchor.BASELINE_LEFT
                || anchor == TextAnchor.HALF_ASCENT_LEFT) {

            xAdj = 0.0f;

        }
        else if (anchor == TextAnchor.TOP_CENTER
                || anchor == TextAnchor.CENTER
                || anchor == TextAnchor.BOTTOM_CENTER
                || anchor == TextAnchor.BASELINE_CENTER
                || anchor == TextAnchor.HALF_ASCENT_CENTER) {
                    
           xAdj = (float) bounds.getWidth() / 2.0f;
           
        }
        else if (anchor == TextAnchor.TOP_RIGHT
                || anchor == TextAnchor.CENTER_RIGHT
                || anchor == TextAnchor.BOTTOM_RIGHT
                || anchor == TextAnchor.BASELINE_RIGHT
                || anchor == TextAnchor.HALF_ASCENT_RIGHT) {
                    
            xAdj = (float) bounds.getWidth();
            
        }

        if (anchor == TextAnchor.TOP_LEFT
                || anchor == TextAnchor.TOP_CENTER
                || anchor == TextAnchor.TOP_RIGHT) {
                    
            yAdj = descent + leading - (float) bounds.getHeight();
            
        }
        else if (anchor == TextAnchor.CENTER_LEFT
                || anchor == TextAnchor.CENTER
                || anchor == TextAnchor.CENTER_RIGHT) {
                    
            yAdj = descent + leading - (float) (bounds.getHeight() / 2.0);
            
        }
        else if (anchor == TextAnchor.HALF_ASCENT_LEFT
                || anchor == TextAnchor.HALF_ASCENT_CENTER
                || anchor == TextAnchor.HALF_ASCENT_RIGHT) {
                    
            yAdj = -halfAscent;
            
        }
        else if (anchor == TextAnchor.BASELINE_LEFT
                || anchor == TextAnchor.BASELINE_CENTER
                || anchor == TextAnchor.BASELINE_RIGHT) {
                    
            yAdj = 0.0f;
            
        }
        else if (anchor == TextAnchor.BOTTOM_LEFT
                || anchor == TextAnchor.BOTTOM_CENTER
                || anchor == TextAnchor.BOTTOM_RIGHT) {
                    
            yAdj = metrics.getDescent() + metrics.getLeading();
            
        }
        result[0] = xAdj;
        result[1] = yAdj;
        return result;

    }

    /**
     * Draws a rotated shape.
     *
     * @param shape  the shape.
     * @param g2  the graphics device.
     * @param x  the x coordinate for the rotation point.
     * @param y  the y coordinate for the rotation point.
     * @param angle  the angle.
     */
    public static void drawRotatedShape(Shape shape, Graphics2D g2,
                                        float x, float y, double angle) {

        AffineTransform saved = g2.getTransform();

        AffineTransform rotate = AffineTransform.getRotateInstance(angle, x, y);
        g2.transform(rotate);
        g2.draw(shape);

        g2.setTransform(saved);

    }

    /**
     * Returns a point based on (x, y) but constrained to be within the bounds of a given
     * rectangle.
     *
     * @param x  the x-coordinate.
     * @param y  the y-coordinate.
     * @param area  the constraining rectangle.
     *
     * @return a point within the rectangle.
     */
    public static Point2D getPointInRectangle(double x, double y, Rectangle2D area) {

        x = Math.max(area.getMinX(), Math.min(x, area.getMaxX()));
        y = Math.max(area.getMinY(), Math.min(y, area.getMaxY()));
        return new Point2D.Double(x, y);

    }

}


