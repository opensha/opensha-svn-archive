/*
 * @(#)ButtonStateIcon.java  1.0  October 5, 2003
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

import javax.swing.*;
/**
 * An Icon with different visuals reflecting the state of the AbstractButton
 * on which it draws on.
 *
 * @author  Werner Randelshofer
 * @version 1.0 October 5, 2003 Create..
 */
public class ButtonStateIcon implements Icon {
    private Icon enabled, selected, armed, disabled, disabledSelected;
    private Icon sizeIcon;
    
    /**
     * Creates a new instance.
     * All icons must have the same dimensions.
     * If an icon is null, nothing is drawn for this state.
     */
    public ButtonStateIcon(Icon enabled, Icon selected, Icon armed, Icon disabled, Icon disabledSelected) {
        this.enabled = enabled;
        this.selected = selected;
        this.armed = armed;
        this.disabled = disabled;
        this.disabledSelected = disabledSelected;
        sizeIcon = (enabled != null) ? enabled : (
        (selected != null) ? selected : (
        (armed != null) ? armed : (
        (disabled != null) ? disabled : disabledSelected
        )));
        
    }
    
    public int getIconHeight() {
        return sizeIcon.getIconHeight();
    }
    
    public int getIconWidth() {
        return sizeIcon.getIconWidth();
    }
    
    public void paintIcon(java.awt.Component c, java.awt.Graphics g, int x, int y) {
        Icon target;
        if (c instanceof AbstractButton) {
            ButtonModel model = ((AbstractButton) c).getModel();
            if (model.isEnabled()) {
                if (model.isArmed() && model.isSelected()) {
                    target = armed;
                } else if (model.isSelected()) {
                    target = selected;
                } else {
                    target = enabled;
                }
            } else {
                if (model.isSelected()) {
                    target = disabledSelected;
                } else {
                    target = disabled;
                }
            }
        } else {
            if (c.isEnabled()) {
                target = enabled;
            } else {
                target = disabled;
            }
        }
        
        if (target != null) {
            target.paintIcon(c, g, x, y);
        }
    }
}
