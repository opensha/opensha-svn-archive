/*
 * @(#)QuaquaMenuBorder.java  1.0  August 31, 2003
 *
 * Copyright (c) 2003 Werner Randelshofer
 * Staldenmattweg 2, Immensee, CH-6405, Switzerland.
 * http://www.randelshofer.ch
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
import javax.swing.*;
import javax.swing.border.*;
/**
 * A replacement for the AquaMenuBorder.
 * <p>
 * This class provides the following workaround for a bug in Apple's
 * implementation of the Aqua Look and Feel in Java 1.4.1:
 * <ul>
 * <li>Draws a border at the top and the bottom of JPopupMenu's.
 * </ul>
 *
 * @author Werner Randelshofer
 * @version 1.0 August 31, 2003  Created.
 */
public class QuaquaMenuBorder implements Border {
    
    protected static Insets popupBorderInsets;
    protected static Insets itemBorderInsets;
    
    public void paintBorder(Component component, Graphics graphics, int x,
    int y, int width, int height) {
        /* empty */
    }
    
    public Insets getBorderInsets(Component component) {
        Insets insets;
        
        // FIXME
        // I'd like to test for instanceof only. But Apple's ComboBoxUI
        // already draws a border around its popup menu.
        // To do an instanceof test, I would also have to replace the
        // ComboBoxUI
        
        //if (component instanceof JPopupMenu) {
        if (component.getClass() == javax.swing.JPopupMenu.class) {
            if (popupBorderInsets == null)
                popupBorderInsets = new Insets(4, 0, 4, 0);
            insets = popupBorderInsets;
        } else {
            if (itemBorderInsets == null)
                itemBorderInsets = new Insets(0, 0, 0, 0);
            insets = itemBorderInsets;
        }
        return insets;
    }
    
    public boolean isBorderOpaque() {
        return false;
    }
}