/*
 * @(#)QuaquaGraphicsUtils.java  1.1.1  2003-10-08
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
import java.awt.event.*;
import javax.swing.*;
import javax.swing.plaf.*;
import javax.swing.plaf.basic.*;
/**
 * Utility class for the Quaqua LAF.
 *
 * @author Werner Randelshofer, Staldenmattweg 2, CH-6405 Immensee, Switzerland
 * @version 1.1.1 2003-10-08 Diagnostic output to System.out removed.
 * <br>1.1 2003-10-05 Methods getModifiersText and getModifiersUnicode 
 * added.
 * <br>1.0 2003-07-19 Created.
 */
public class QuaquaGraphicsUtils extends BasicGraphicsUtils {
    
    /** Creates a new instance of QuaquaGraphicsUtils */
    private QuaquaGraphicsUtils() {
    }
    
    /*
     * Convenience function for determining ComponentOrientation.  Helps us
     * avoid having Munge directives throughout the code.
     */
    static boolean isLeftToRight( Component c ) {
        return c.getComponentOrientation().isLeftToRight();
    }

    /**
     * Draw a string with the graphics <code>g</code> at location
     * (<code>x</code>, <code>y</code>)
     * just like <code>g.drawString</code> would.
     * The character at index <code>underlinedIndex</code>
     * in text will be underlined. If <code>index</code> is beyond the
     * bounds of <code>text</code> (including < 0), nothing will be
     * underlined.
     *
     * @param g Graphics to draw with
     * @param text String to draw
     * @param underlinedIndex Index of character in text to underline
     * @param x x coordinate to draw at
     * @param y y coordinate to draw at
     * @since 1.4
     */
    public static void drawStringUnderlineCharAt(Graphics g, String text,
                           int underlinedIndex, int x,int y) {
        g.drawString(text,x,y);
        if (underlinedIndex >= 0 && underlinedIndex < text.length() ) {
            FontMetrics fm = g.getFontMetrics();
            int underlineRectX = x + fm.stringWidth(text.substring(0,underlinedIndex));
            int underlineRectY = y;
            int underlineRectWidth = fm.charWidth(text.charAt(underlinedIndex));
            int underlineRectHeight = 1;
            g.fillRect(underlineRectX, underlineRectY + fm.getDescent() - 1,
                       underlineRectWidth, underlineRectHeight);
        }
    }
   /**
     * Returns index of the first occurrence of <code>mnemonic</code>
     * within string <code>text</code>. Matching algorithm is not
     * case-sensitive.
     *
     * @param text The text to search through, may be null
     * @param mnemonic The mnemonic to find the character for.
     * @return index into the string if exists, otherwise -1
     */
    static int findDisplayedMnemonicIndex(String text, int mnemonic) {
        if (text == null || mnemonic == '\0') {
            return -1;
        }

        char uc = Character.toUpperCase((char)mnemonic);
        char lc = Character.toLowerCase((char)mnemonic);

        int uci = text.indexOf(uc);
        int lci = text.indexOf(lc);

        if (uci == -1) {
            return lci;
        } else if(lci == -1) {
            return uci;
        } else {
            return (lci < uci) ? lci : uci;
        }
    }

    /**
     * Returns a Mac OS X specific String describing the modifier key(s),
     * such as "Shift", or "Ctrl+Shift".
     *
     * @return string a text description of the combination of modifier
     *                keys that were held down during the event
     */
    public static String getKeyModifiersText(int modifiers, boolean leftToRight) {
	return getKeyModifiersUnicode(modifiers, leftToRight);
    }
    static String getKeyModifiersUnicode(int modifiers, boolean leftToRight) {
	char[] cs = new char[4];
	int count = 0;
	if (leftToRight) {
	    if ((modifiers & InputEvent.CTRL_MASK) != 0)
		cs[count++] = '\u2303'; // Unicode: UP ARROWHEAD
	    if ((modifiers & (InputEvent.ALT_MASK | InputEvent.ALT_GRAPH_MASK)) != 0)
		cs[count++] = '\u2325'; // Unicode: OPTION KEY
	    if ((modifiers & InputEvent.SHIFT_MASK) != 0)
		cs[count++] = '\u21e7'; // Unicode: UPWARDS WHITE ARROW
	    if ((modifiers & InputEvent.META_MASK) != 0)
		cs[count++] = '\u2318'; // Unicode: PLACE OF INTEREST SIGN
	} else {
	    if ((modifiers & InputEvent.META_MASK) != 0)
		cs[count++] = '\u2318'; // Unicode: PLACE OF INTEREST SIGN
	    if ((modifiers & InputEvent.SHIFT_MASK) != 0)
		cs[count++] = '\u21e7'; // Unicode: UPWARDS WHITE ARROW
	    if ((modifiers & (InputEvent.ALT_MASK | InputEvent.ALT_GRAPH_MASK)) != 0)
		cs[count++] = '\u2325'; // Unicode: OPTION KEY
	    if ((modifiers & InputEvent.CTRL_MASK) != 0)
		cs[count++] = '\u2303'; // Unicode: UP ARROWHEAD
	}
	return new String(cs, 0, count);
    }
}
