/*
 * @(#)DefaultBrowserCellRenderer.java  1.0  September 14, 2003
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
 * DefaultBrowserCellRenderer.
 *
 * @author Werner Randelshofer
 * @version 1.0 September 14, 2003  Created.
 */
public class DefaultBrowserCellRenderer implements ListCellRenderer  {
    private JPanel panel;
    private JLabel textLabel;
    private JLabel arrowLabel;
    private EmptyBorder noFocusBorder;
    private JBrowser browser;
    protected Icon expandedIcon = null;
    protected Icon expandingIcon = null;
    
    public DefaultBrowserCellRenderer(JBrowser browser) {
        this.browser = browser;
        expandedIcon = new ImageIcon(
        Toolkit.getDefaultToolkit().createImage(
        getClass().getResource("images/Browser.expandedIcon.png")
        )
        );
        /*
        expandingIcon = UIManager.getIcon("Browser.expandedIcon");
        expandedIcon = UIManager.getIcon("Browser.expandedIcon");
         */
        
        noFocusBorder = new EmptyBorder(1, 1, 1, 1);
        panel = new JPanel(new BorderLayout()) {
            // Overridden for performance reasons.
            //public void validate() {}
            public void revalidate() {}
            public void repaint(long tm, int x, int y, int width, int height) {}
            public void repaint(Rectangle r) {}
            protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {}
            public void firePropertyChange(String propertyName, short oldValue, short newValue) {}
            public void firePropertyChange(String propertyName, int oldValue, int newValue) {}
            public void firePropertyChange(String propertyName, long oldValue, long newValue) {}
            public void firePropertyChange(String propertyName, float oldValue, float newValue) {}
            public void firePropertyChange(String propertyName, double oldValue, double newValue) {}
            public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {}
        };
        
        textLabel = new JLabel() {
            // Overridden for performance reasons.
            public void validate() {}
            public void revalidate() {}
            public void repaint(long tm, int x, int y, int width, int height) {}
            public void repaint(Rectangle r) {}
            protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
                // Strings get interned...
                if (propertyName=="text")
                    super.firePropertyChange(propertyName, oldValue, newValue);
            }
            public void firePropertyChange(String propertyName, short oldValue, short newValue) {}
            public void firePropertyChange(String propertyName, int oldValue, int newValue) {}
            public void firePropertyChange(String propertyName, long oldValue, long newValue) {}
            public void firePropertyChange(String propertyName, float oldValue, float newValue) {}
            public void firePropertyChange(String propertyName, double oldValue, double newValue) {}
            public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {}
        };
        arrowLabel = new JLabel() {
            // Overridden for performance reasons.
            public void validate() {}
            public void revalidate() {}
            public void repaint(long tm, int x, int y, int width, int height) {}
            public void repaint(Rectangle r) {}
            protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
                // Strings get interned...
                if (propertyName=="text")
                    super.firePropertyChange(propertyName, oldValue, newValue);
            }
            public void firePropertyChange(String propertyName, short oldValue, short newValue) {}
            public void firePropertyChange(String propertyName, int oldValue, int newValue) {}
            public void firePropertyChange(String propertyName, long oldValue, long newValue) {}
            public void firePropertyChange(String propertyName, float oldValue, float newValue) {}
            public void firePropertyChange(String propertyName, double oldValue, double newValue) {}
            public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {}
        };
        
        panel.setOpaque(true);
        panel.setBorder(noFocusBorder);
        
        panel.add(textLabel, BorderLayout.CENTER);
        
        arrowLabel.setIcon(expandedIcon);
        panel.add(arrowLabel, BorderLayout.EAST);
    }
    
    public Component getListCellRendererComponent(JList list, Object value,
    int index, boolean isSelected,
    boolean cellHasFocus) {
        //setComponentOrientation(list.getComponentOrientation());
        if (isSelected) {
            panel.setBackground(list.getSelectionBackground());
            textLabel.setForeground(list.getSelectionForeground());
            arrowLabel.setForeground(list.getSelectionForeground());
        } else {
            panel.setBackground(list.getBackground());
            textLabel.setForeground(list.getForeground());
            arrowLabel.setForeground(list.getForeground());
        }
        
        textLabel.setText((value == null) ? "null" : value.toString());
        //textLabel.setIcon(getFileChooser().getIcon(file));
        
        arrowLabel.setVisible(! browser.getModel().isLeaf(value));
        
        textLabel.setEnabled(list.isEnabled());
        textLabel.setFont(list.getFont());
        panel.setBorder((cellHasFocus) ? UIManager.getBorder("List.focusCellHighlightBorder") : noFocusBorder);
        
        return panel;
    }
}

