/*
 * @(#)DoubleBevelBorder.java  1.0  November 12, 2003
 *
 * Copyright (c) 2003 Werner Randelshofer
 * Staldenmattweg 2, Immensee, CH-6405, Switzerland.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Werner Randelshofer. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Werner Randelshofer.
 */

package ch.randelshofer.quaqua;

import java.awt.*;
import javax.swing.border.Border;
/**
 * DoubleBevelBorder takes two borders and draws one on the left and one
 * on the right half of the component.
 *
 * @author  Werner Randelshofer
 * @version 1.0 November 12, 2003 Created.
 */
public class DoubleBevelBorder implements javax.swing.border.Border {
    private Border leftBorder;
    private Border rightBorder;
    
    /** Creates a new instance. */
    public DoubleBevelBorder(Border leftBorder, Border rightBorder) {
        this.leftBorder = leftBorder;
        this.rightBorder = rightBorder;
    }
    
    public java.awt.Insets getBorderInsets(java.awt.Component c) {
        Insets leftInsets = leftBorder.getBorderInsets(c);
        Insets rightInsets = rightBorder.getBorderInsets(c);
        return new Insets(
        Math.max(leftInsets.top, rightInsets.top),
        leftInsets.left,
        Math.max(leftInsets.bottom, rightInsets.bottom),
        rightInsets.right
        );
    }
    
    public boolean isBorderOpaque() {
        return false;
    }
    
    public void paintBorder(java.awt.Component c, java.awt.Graphics g, int x, int y, int w, int h) {
        leftBorder.paintBorder(c, g, x, y, w / 2, h);
        rightBorder.paintBorder(c, g, x + w / 2, y, w - w / 2, h);
    }
}
