/*
 * @(#)Quaqua141U1SeparatorUI.java 1.1  2003-10-29
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
import javax.swing.*;
import javax.swing.plaf.*;
import javax.swing.plaf.basic.*;
/**
 * A replacement for the AquaSeparatorUI.
 * <p>
 * This class provides the following workaround for an issue in Apple's
 * implementation of the Aqua Look and Feel in Java 1.4.1:
 * <ul>
 * <li>Menu separators are drawn as a blank space instead of a thin black line.
 * This fix affects JSeparator's.
 * </li>
 * </ul>
 *
 * @author Werner Randelshofer, Staldenmattweg 2, CH-6405 Immensee, Switzerland
 * @version 1.1 2003-10-29 Renamed from QuaquaSeparatorUI to Quaqua141U1SeparatorUI. 
 * <br>1.0 2003-07-19 Created.
 */
public class Quaqua141U1SeparatorUI extends BasicSeparatorUI {
    
    /** Creates a new instance of QuaquaSeparatorUI */
    public Quaqua141U1SeparatorUI() {
    }
    
    public static ComponentUI createUI(JComponent c) {
        return new Quaqua141U1SeparatorUI();
    }
    
    public void paint(Graphics g, JComponent c) {
        if ( !(c.getParent() instanceof JPopupMenu)) {
            Dimension s = c.getSize();
            
            if ( ((JSeparator)c).getOrientation() == JSeparator.VERTICAL ) {
                g.setColor( c.getForeground() );
                g.drawLine( 0, 0, 0, s.height );
                
                g.setColor( c.getBackground() );
                g.drawLine( 1, 0, 1, s.height );
            } else { // HORIZONTAL
                g.setColor( c.getForeground() );
                g.drawLine( 0, 0, s.width, 0 );
                
                g.setColor( c.getBackground() );
                g.drawLine( 0, 1, s.width, 1 );
            }
        }
    }
    
    public Dimension getPreferredSize( JComponent c ) {
        if (c.getParent() instanceof JPopupMenu) {
            if ( ((JSeparator)c).getOrientation() == JSeparator.VERTICAL )
                return new Dimension( 16, 0 );
            else
                return new Dimension( 0, 16 );
        } else {
            if ( ((JSeparator)c).getOrientation() == JSeparator.VERTICAL )
                return new Dimension( 2, 0 );
            else
                return new Dimension( 0, 2 );
        }
    }
}
