/*
 * @(#)Quaqua141UPSeparatorUI.java 1.0  2003-09-29
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
 * <li>Menu separators are drawn using Separator.highlight and Separator.shadow
 * colors instead of a blank and white line.
 * This fix affects JSeparator's.
 * </li>
 * </ul>
 *
 * @author Werner Randelshofer, Staldenmattweg 2, CH-6405 Immensee, Switzerland
 * @version 1.0 2003-09-29 Created.
 */
public class Quaqua141UPSeparatorUI extends BasicSeparatorUI {
    
    /** Creates a new instance of QuaquaSeparatorUI */
    public Quaqua141UPSeparatorUI() {
    }
    
    public static ComponentUI createUI(JComponent c) {
        return new Quaqua141UPSeparatorUI();
    }
    
    public void paint(Graphics g, JComponent c) {
        Dimension s = c.getSize();
        if (c.getParent() instanceof JPopupMenu) {
            Color highlightColor = UIManager.getColor("Separator.highlight");
            Color shadowColor = UIManager.getColor("Separator.shadow");
            if ( ((JSeparator)c).getOrientation() == JSeparator.VERTICAL ) {
                g.setColor( highlightColor );
                g.drawLine( s.width / 2 - 1 , 1, s.width / 2 - 1, s.height - 2 );
                g.setColor( shadowColor );
                g.drawLine( s.width / 2, 1, s.width / 2, s.height - 2);
            } else { // HORIZONTAL
                g.setColor( highlightColor );
                g.drawLine( 1, s.height / 2 - 1, s.width - 2, s.height / 2 - 1 );
                g.setColor( shadowColor );
                g.drawLine( 1, s.height / 2, s.width - 2, s.height / 2 );
            }
        } else {
            Color highlightColor = UIManager.getColor("Separator.foreground");
            Color shadowColor = UIManager.getColor("Separator.background");
            if ( ((JSeparator)c).getOrientation() == JSeparator.VERTICAL ) {
                g.setColor( highlightColor );
                g.drawLine( 0, 0, 0, s.height );
                g.setColor( shadowColor );
                g.drawLine( 1, 0, 1, s.height);
            } else { // HORIZONTAL
                g.setColor( highlightColor );
                g.drawLine( 0, 0, s.width, 0 );
                g.setColor( shadowColor );
                g.drawLine( 0, 1, s.width, 1 );
            }
        }
    }
    
    public Dimension getPreferredSize( JComponent c ) {
        if (c.getParent() instanceof JPopupMenu) {
            if ( ((JSeparator)c).getOrientation() == JSeparator.VERTICAL )
                return new Dimension( 12, 0 );
            else
                return new Dimension( 0, 12 );
        } else {
            if ( ((JSeparator)c).getOrientation() == JSeparator.VERTICAL )
                return new Dimension( 2, 0 );
            else
                return new Dimension( 0, 2 );
        }
    }
}
