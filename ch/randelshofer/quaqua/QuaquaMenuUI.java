/*
 * @(#)QuaquaMenuUI.java 1.1  2003-10-06
 *
 * Copyright (c) 2001 Werner Randelshofer
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
import java.awt.event.*;
import java.awt.image.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.plaf.*;
import javax.swing.plaf.basic.*;
import javax.swing.plaf.metal.*;
import javax.swing.text.View;
/**
 * A replacement for the AquaMenuUI.
 * <p>
 * This class does not fix any particular bug in the Mac LAF or the Aqua LAF.
 * It is just here to achieve a consistent look with the other Quaqua menu UI
 * classes.
 *
 * @author  Werner Randelshofer
 * @version 1.1 2003-10-06 Layout code outplaced into class QuaquaMenuPainter
 * <br>1.0 2003-06-20 Created.
 */
public class QuaquaMenuUI extends BasicMenuUI implements QuaquaMenuPainterClient {
    public static ComponentUI createUI(JComponent x) {
        return new QuaquaMenuUI();
    }
    
    protected void paintMenuItem(Graphics g, JComponent c,
				 Icon checkIcon, Icon arrowIcon, Color background,
				 Color foreground, int defaultTextIconGap) {
                                     
	QuaquaMenuPainter.instance.paintMenuItem(this, g, c, checkIcon,
					      arrowIcon, background, foreground,
					      disabledForeground,
					      selectionForeground, defaultTextIconGap,
					      acceleratorFont);
    }
    
    protected Dimension getPreferredMenuItemSize(JComponent c,
                                                     Icon checkIcon,
                                                     Icon arrowIcon,
                                                     int defaultTextIconGap) {
	Dimension d = QuaquaMenuPainter.instance
        .getPreferredMenuItemSize(c, checkIcon, arrowIcon, defaultTextIconGap, acceleratorFont);
        //d.width += 10;
        return d;
    }
    

    public void paintBackground(Graphics g, JComponent component, int menuWidth, int menuHeight) {
        Color bgColor = selectionBackground; 
        AbstractButton menuItem = (AbstractButton) component;
        ButtonModel model = menuItem.getModel();
        Color oldColor = g.getColor();
        
        if(menuItem.isOpaque()) {
            if (model.isArmed()|| (menuItem instanceof JMenu && model.isSelected())) {
                g.setColor(bgColor);
                g.fillRect(0,0, menuWidth, menuHeight);
            } else {
                g.setColor(menuItem.getBackground());
                g.fillRect(0,0, menuWidth, menuHeight);
            }
            g.setColor(oldColor);
        }
    }
    
    
}

