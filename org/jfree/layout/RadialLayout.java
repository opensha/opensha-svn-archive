/* ===================================================
 * JCommon : a free general purpose Java class library
 * ===================================================
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
 * -----------------
 * RadialLayout.java
 * -----------------
 * (C) Copyright 2003, by Bryan Scott (for Australian Antarctic Division).
 *
 * Original Author:  Bryan Scott (for Australian Antarctic Division);
 * Contributor(s):   David Gilbert (for Object Refinery Limited);
 *
 *
 * Changes:
 * --------
 * 30-Jun-2003 : Version 1 (BS);
 * 24-Jul-2003 : Completed missing Javadocs (DG);
 *
 */

package org.jfree.layout;

import java.awt.Checkbox;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Panel;
import java.io.Serializable;

/**
 * RadialLayout is a component layout manager.  Compents are laid out in a
 * circle. If only one component is contained in the layout it is positioned
 * centrally, otherwise components are evenly spaced around the centre with
 * the first component placed to the North.
 *<P>
 * This code was developed to display CTD rosette firing control
 *
 * WARNING: Not thoughly tested, use at own risk.
 * 
 * @author Bryan Scott (for Australian Antarctic Division)
 */

public class RadialLayout implements LayoutManager, Serializable {
    
    /** The minimum width. */
    private int minWidth = 0;
    
    /** The minimum height. */
    private int minHeight = 0;
    
    /** The maximum component width. */
    private int maxCompWidth = 0;
    
    /** The maximum component height. */
    private int maxCompHeight = 0;
    
    /** The preferred width. */
    private int preferredWidth = 0;
    
    /** The preferred height. */
    private int preferredHeight = 0;
    
    /** Size unknown flag. */
    private boolean sizeUnknown = true;

    /** 
     * Constructs this layout manager with default properties. 
     */
    public RadialLayout() {
    }

    /**
     * Not used.
     *
     * @param comp  the component.
     */
    public void addLayoutComponent(Component comp) {
    }

    /**
     * Not used.
     *
     * @param comp  the component.
     */
    public void removeLayoutComponent(Component comp) {
    }

    /**
     * Not used.
     *
     * @param name  the component name.
     * @param comp  the component.
     */
    public void addLayoutComponent(String name, Component comp) {
    }

    /**
     * Not used.
     *
     * @param name  the component name.
     * @param comp  the component.
     */
    public void removeLayoutComponent(String name, Component comp) {
    }

    /**
     * Sets the sizes attribute of the RadialLayout object
     *
     * @param  parent  the parent.
     * 
     * @see LayoutManager
     */
    private void setSizes(Container parent) {
        int nComps = parent.getComponentCount();
        Dimension d = null;
        //Reset preferred/minimum width and height.
        preferredWidth = 0;
        preferredHeight = 0;
        minWidth = 0;
        minHeight = 0;
        for (int i = 0; i < nComps; i++) {
            Component c = parent.getComponent(i);
            if (c.isVisible()) {
                d = c.getPreferredSize();
                if (maxCompWidth < d.width) {
                    maxCompWidth = d.width;
                }
                if (maxCompHeight < d.height) {
                    maxCompHeight = d.height;
                }
                preferredWidth += d.width;
                preferredHeight += d.height;
            }
        }
        preferredWidth  = preferredWidth / 2;
        preferredHeight = preferredHeight / 2;
        minWidth = preferredWidth;
        minHeight = preferredHeight;
    }

    /**
     * Returns the preferred size.
     *
     * @param parent  the parent.
     *
     * @return The preferred size.
     * @see LayoutManager
     */
    public Dimension preferredLayoutSize(Container parent) {
        Dimension dim = new Dimension(0, 0);
        int nComps = parent.getComponentCount();
        setSizes(parent);

        //Always add the container's insets!
        Insets insets = parent.getInsets();
        dim.width = preferredWidth + insets.left + insets.right;
        dim.height = preferredHeight + insets.top + insets.bottom;

        sizeUnknown = false;
        return dim;
    }

    /**
     * Returns the minimum size.
     *
     * @param parent  the parent.
     *
     * @return The minimum size.
     * @see LayoutManager
     */
    public Dimension minimumLayoutSize(Container parent) {
        Dimension dim = new Dimension(0, 0);
        int nComps = parent.getComponentCount();

        //Always add the container's insets!
        Insets insets = parent.getInsets();
        dim.width = minWidth + insets.left + insets.right;
        dim.height = minHeight + insets.top + insets.bottom;

        sizeUnknown = false;
        return dim;
    }

   /**
    * This is called when the panel is first displayed, and every time its size
    * changes.
    * Note: You CAN'T assume preferredLayoutSize or minimumLayoutSize will be
    * called -- in the case of applets, at least, they probably won't be.
    *
    * @param  parent  the parent.
    * @see LayoutManager
    */
    public void layoutContainer(Container parent) {
        Insets insets = parent.getInsets();
        int maxWidth = parent.getSize().width - (insets.left + insets.right);
        int maxHeight = parent.getSize().height - (insets.top + insets.bottom);
        int nComps = parent.getComponentCount();
        int previousWidth = 0;
        int previousHeight = 0;
        int x = 0;
        int y = 0;

        // Go through the components' sizes, if neither preferredLayoutSize nor
        // minimumLayoutSize has been called.
        if (sizeUnknown) {
            setSizes(parent);
        }

        if (nComps < 2) {
            Component c = parent.getComponent(0);
            if (c.isVisible()) {
                Dimension d = c.getPreferredSize();
                c.setBounds(x, y, d.width, d.height);
            }
        } 
        else {
            double radialCurrent = Math.toRadians(90);
            double radialIncrement = 2 * Math.PI / nComps;
            int midX = (int) maxWidth / 2;
            int midY = (int) maxHeight / 2;
            int a = midX - maxCompWidth;
            int b = midY - maxCompHeight;
            for (int i = 0; i < nComps; i++) {
                Component c = parent.getComponent(i);
                if (c.isVisible()) {
                    Dimension d = c.getPreferredSize();
                    x = (int) (midX
                               - (a * Math.cos(radialCurrent))
                               - (d.getWidth() / 2)
                               + insets.left);
                    y = (int) (midY
                               - (b * Math.sin(radialCurrent))
                               - (d.getHeight() / 2)
                               + insets.top);

                    // Set the component's size and position.
                    c.setBounds(x, y, d.width, d.height);
                }
                radialCurrent += radialIncrement;
            }
        }
    }

    /**
     * Returns the class name.
     * 
     * @return The class name.
     */
    public String toString() {
        return getClass().getName();
    }

    /**
     * Run a demonstration.
     *
     * @param args  ignored.
     * 
     * @throws Exception when an error occurs.
     */
    public static void main(String args[]) throws Exception {
        Frame frame = new Frame();
        Panel panel = new Panel();
        panel.setLayout(new RadialLayout());

        panel.add(new Checkbox("One"));
        panel.add(new Checkbox("Two"));
        panel.add(new Checkbox("Three"));
        panel.add(new Checkbox("Four"));
        panel.add(new Checkbox("Five"));
        panel.add(new Checkbox("One"));
        panel.add(new Checkbox("Two"));
        panel.add(new Checkbox("Three"));
        panel.add(new Checkbox("Four"));
        panel.add(new Checkbox("Five"));

        frame.add(panel);
        frame.setSize(300, 500);
        frame.show();
    }

}
