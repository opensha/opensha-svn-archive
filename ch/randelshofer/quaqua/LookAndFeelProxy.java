/*
 * @(#)LookAndFeelProxy.java  1.0  2003-07-20
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
import javax.swing.plaf.basic.*;
import java.awt.*;
/**
 * A proxy for LookAndFeel objects. This class enables us to override
 * the behaviour of LookAndFeel objects whithout subclassing them.
 * <p>
 * <b>Note</b> this class extends BasicLookAndFeel instead of
 * LookAndFeel. This is because some UI classes derived from the Basic LAF
 * don't work if they can't cast the current LookAndFeel to BasicLookAndFeel.
 *
 * @author Werner Randelshofer, Staldenmattweg 2, CH-6405 Immensee, Switzerland
 * @version 1.0 2003-07-20 Created.
 */
public class LookAndFeelProxy extends BasicLookAndFeel {
    /**
     * The target LookAndFeel.
     */
    protected LookAndFeel target;
    
    /**
     * Creates a new instance which proxies the supplied target.
     */
    public LookAndFeelProxy(LookAndFeel target) {
        this.target = target;
    }
    /**
     * Creates a new instance with a null target.
     */
    protected LookAndFeelProxy() {
    }
    
    /**
     * Sets the target of this proxy.
     */
    protected void setTarget(LookAndFeel target) {
        this.target = target;
    }
    
    /** 
     * Return a one line description of this look and feel implementation, 
     * e.g. "The CDE/Motif Look and Feel".   This string is intended for 
     * the user, e.g. in the title of a window or in a ToolTip message.
     */
    public String getDescription() {
        return target.getDescription();
    }
    
    /**
     * Return a string that identifies this look and feel.  This string 
     * will be used by applications/services that want to recognize
     * well known look and feel implementations.  Presently
     * the well known names are "Motif", "Windows", "Mac", "Metal".  Note 
     * that a LookAndFeel derived from a well known superclass 
     * that doesn't make any fundamental changes to the look or feel 
     * shouldn't override this method.
     */
    public String getID() {
        return target.getID();
    }
    
    /**
     * Return a short string that identifies this look and feel, e.g.
     * "CDE/Motif".  This string should be appropriate for a menu item.
     * Distinct look and feels should have different names, e.g. 
     * a subclass of MotifLookAndFeel that changes the way a few components
     * are rendered should be called "CDE/Motif My Way"; something
     * that would be useful to a user trying to select a L&F from a list
     * of names.
     */
    public String getName() {
        return target.getName();
    }
    
    /**
     * If the underlying platform has a "native" look and feel, and this
     * is an implementation of it, return true.  For example a CDE/Motif
     * look and implementation would return true when the underlying 
     * platform was Solaris.
     */
    public boolean isNativeLookAndFeel() {
        return target.isNativeLookAndFeel();
    }
    
    /**
     * Return true if the underlying platform supports and or permits
     * this look and feel.  This method returns false if the look 
     * and feel depends on special resources or legal agreements that
     * aren't defined for the current platform.  
     * 
     * @see UIManager#setLookAndFeel
     */
    public boolean isSupportedLookAndFeel() {
        return target.isSupportedLookAndFeel();
    }
    
    /**
     * Invoked when the user attempts an invalid operation, 
     * such as pasting into an uneditable <code>JTextField</code> 
     * that has focus. The default implementation beeps. Subclasses 
     * that wish different behavior should override this and provide 
     * the additional feedback.
     *
     * @param component Component the error occured in, may be null 
     *			indicating the error condition is not directly 
     *			associated with a <code>Component</code>.
     */
    public void provideErrorFeedback(Component component) {
        Toolkit.getDefaultToolkit().beep();
        /* FIXME - This works on JDK 1.4 only
        //target.provideErrorFeedback(component);
         */
    }
    
    /**
     * Returns true if the <code>LookAndFeel</code> returned
     * <code>RootPaneUI</code> instances support providing Window decorations
     * in a <code>JRootPane</code>.
     * <p>
     * The default implementation returns false, subclasses that support
     * Window decorations should override this and return true.
     *
     * @return True if the RootPaneUI instances created support client side
     *              decorations
     * @see JDialog#setDefaultLookAndFeelDecorated
     * @see JFrame#setDefaultLookAndFeelDecorated
     * @see JRootPane#setWindowDecorationStyle
     * @since 1.4
     */
    public boolean getSupportsWindowDecorations() {
        return false;
        /* FIXME - This works since JDK 1.4 only
        return target.getSupportsWindowDecorations();
         */
    }
    
    /**
     * UIManager.setLookAndFeel calls this method before the first
     * call (and typically the only call) to getDefaults().  Subclasses
     * should do any one-time setup they need here, rather than 
     * in a static initializer, because look and feel class objects
     * may be loaded just to discover that isSupportedLookAndFeel()
     * returns false.
     *
     * @see #uninitialize
     * @see UIManager#setLookAndFeel
     */
    public void initialize() {
        target.initialize();
    }


    /**
     * UIManager.setLookAndFeel calls this method just before we're
     * replaced by a new default look and feel.   Subclasses may 
     * choose to free up some resources here.
     *
     * @see #initialize
     */
    public void uninitialize() {
        target.uninitialize();
    }

    /**
     * This method is called once by UIManager.setLookAndFeel to create
     * the look and feel specific defaults table.  Other applications,
     * for example an application builder, may also call this method.
     *
     * @see #initialize
     * @see #uninitialize
     * @see UIManager#setLookAndFeel
     */
    public UIDefaults getDefaults() {
        return target.getDefaults();
    }

    /**
     * Returns a string that displays and identifies this
     * object's properties.
     *
     * @return a String representation of this object
     */
    public String toString() {
	return target.toString();
    }
}    