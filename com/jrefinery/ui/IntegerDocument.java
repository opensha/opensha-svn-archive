/* =======================================================
 * JCommon : a free general purpose class library for Java
 * =======================================================
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
 * --------------------
 * IntegerDocument.java
 * --------------------
 * (C) Copyright 2000-2002, by Andrzej Porebski.
 *
 * Original Author:  Andrzej Porebski;
 * Contributor(s):   -;
 *
 * $Id$
 *
 * Changes (from 7-Nov-2001)
 * -------------------------
 * 07-Nov-2001 : Added to com.jrefinery.ui package (DG);
 * 14-Oct-2002 : Fixed errors reported by Checkstyle (DG);
 *
 */

package com.jrefinery.ui;

import javax.swing.text.PlainDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;

/**
 * A document for editing integers.
 *
 * @author AP
 */
public class IntegerDocument extends PlainDocument {

    /**
     * Inserts a string.
     *
     * @param i  i.
     * @param s  s.
     * @param attributes  the attributes.
     *
     * @throws BadLocationException ??
     */
    public void insertString(int i, String s, AttributeSet attributes)
        throws BadLocationException {

        super.insertString(i, s, attributes);
        if (s != null && (!s.equals("-") || i != 0 || s.length() >= 2)) {
            try {
                Integer.parseInt(getText(0, getLength()));
            }
            catch (NumberFormatException e) {
                remove(i, s.length());
            }
        }
    }

}
