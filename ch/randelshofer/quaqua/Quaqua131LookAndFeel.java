/*
 * @(#)Quaqua131LookAndFeel.java  1.2  2003-10-04
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
import javax.swing.plaf.*;
import javax.swing.plaf.basic.*;
import java.util.*;

/**
 * The Quaqua131LookAndFeel provides quick (and dirty) bug fixes for the Mac
 * Look and Feel which is part of Apple's implementation of the
 * Java 2 Runtime Environment, Standard Edition 1.3.1.
 * <p>
 * The Quaqua Look and Feel can not be used on other platforms than Mac OS X.
 * <p>
 * This class provides the following bug fixes to the Mac Look and Feel:
 *
 * <h4>General</h4>
 * <ul>
 * <li>The ID of this Look and Feel is "Aqua" instead of "Mac". (The ID "Mac"
 * was used for the Look and Feel provided with Swing 1.1.1 and below. The
 * "Mac" Look and Feel was a Java implementation of the Platinum Look and Feel
 * provided with Mac OS 8 to 9).</li>
 * </ul>
 *
 * <h4>FileChooserUI</h4>
 * <ul>
 * <li>FileChooserUI uses a column view similar to the one provided with the
 * native Mac OS X Aqua user interface.</li>
 * </ul>
 *
 * <h4>MenuUI's</h4>
 * <ul>
 * <li>Menu accelerators use the font "Lucida Grande 14" instead of 
 * ".Keyboard 14".</li>
 * </ul>
 *
 * <h4>TableUI</h4>
 * <ul>
 * <li>Tables use the font "Lucida Grande 13" instead of "LucidaGrande 12".</li>
 * <li>TableHeaders use the font "Lucida Grande 11" instead of "LucidaGrande 12".
 * </li>
 * </ul>
 *
 * <h4>TextUI's</h4>
 * <ul>
 * <li>Text fields use the font "Lucida Grande 13" instead of "SansSerif 12".
 * </li>
 * </ul>
 *
 * <h4>TreeUI</h4>
 * <ul>
 * <li>Trees use the font "Lucida Grande 13" instead of "LucidaGrande 11".</li>
 * </ul>
 *
 *
 * <h4>Usage</h4>
 * Please use the <code>QuaquaManager</code> to activate this look and feel in
 * your application. Or use the generic <code>QuaquaLookAndFeel</code>. Both
 * are designed to autodetect the appropriate Quaqua Look and Feel 
 * implementation for current Java VM.
 *
 * @see QuaquaManager
 * @see QuaquaLookAndFeel
 *
 * @author Werner Randelshofer, Staldenmattweg 2, CH-6405 Immensee, Switzerland
 * @version 1.2.1 2003-11-01 Browser.expandedIcon and Browser.expandingIcon added. 
 * <br>1.2 2003-10-04 JFileChooser with column view added.
 * <br>1.0 2003-07-20 Created.
 */
public class Quaqua131LookAndFeel extends LookAndFeelProxy {
    /**
     * Holds a bug fixed version of the UIDefaults provided by the target
     * LookAndFeel.
     * @see #initialize
     * @see #getDefaults
     */
    private UIDefaults myDefaults;
    
    /**
     * Creates a new instance.
     */
    public Quaqua131LookAndFeel() {
        // Our target look and feel is Apple's MacLookAndFeel.
        try {
            setTarget((LookAndFeel) Class.forName("com.apple.mrj.swing.MacLookAndFeel").newInstance());
        } catch (Exception e) {
            throw new InternalError("Unable to instanciate MacLookAndFeel:"+e.getMessage());
        }
    }
    
    /**
     * Return a one line description of this look and feel implementation,
     * e.g. "The CDE/Motif Look and Feel".   This string is intended for
     * the user, e.g. in the title of a window or in a ToolTip message.
     */
    public String getDescription() {
        return "Quaqua 1.3.1 Look and Feel for Mac OS X";
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
        // BUGFIX: We return "Aqua" here instead of "Mac".
        // We do this, to simplify Look and Feel dependent behaviour in
        // classes which needs to work with JDK 1.3.1 and 1.4.1 from Apple.
        // (Apple changed the Look and Feel name from "Mac" to "Aqua"
        // in JDK 1.4.1.).
        return "Aqua";
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
        return "Quaqua 1.3.1";
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
        myDefaults = target.getDefaults();
        initClassDefaults(myDefaults);
        initComponentDefaults(myDefaults);
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
        return myDefaults;
   }
    /**
     * Initialize the uiClassID to BasicComponentUI mapping.
     * The JComponent classes define their own uiClassID constants
     * (see AbstractComponent.getUIClassID).  This table must
     * map those constants to a BasicComponentUI class of the
     * appropriate type.
     *
     * @see #getDefaults
     */
    protected void initClassDefaults(UIDefaults table) {
        String basicPrefix = "javax.swing.plaf.basic.Basic";
        String quaquaPrefix = "ch.randelshofer.quaqua.Quaqua";

        // NOTE: Uncomment parts of the code below, to override additional
        // UI classes of the target look and feel.
        Object[] uiDefaults = {
            /*
                   "ButtonUI", basicPrefix + "ButtonUI",
                 "CheckBoxUI", basicPrefix + "CheckBoxUI",
             "ColorChooserUI", basicPrefix + "ColorChooserUI",
             */
             "FileChooserUI", quaquaPrefix + "FileChooserUI",
             /*
       "FormattedTextFieldUI", basicPrefix + "FormattedTextFieldUI",
                  "MenuBarUI", basicPrefix + "MenuBarUI",
            "MenuUI", basicPrefix + "MenuUI",
            "MenuItemUI", basicPrefix + "MenuItemUI",
            "CheckBoxMenuItemUI", basicPrefix + "CheckBoxMenuItemUI",
            "RadioButtonMenuItemUI", basicPrefix + "RadioButtonMenuItemUI",
              "RadioButtonUI", basicPrefix + "RadioButtonUI",
             "ToggleButtonUI", basicPrefix + "ToggleButtonUI",
                "PopupMenuUI", basicPrefix + "PopupMenuUI",
              "ProgressBarUI", basicPrefix + "ProgressBarUI",
                "ScrollBarUI", basicPrefix + "ScrollBarUI",
               "ScrollPaneUI", basicPrefix + "ScrollPaneUI",
                "SplitPaneUI", basicPrefix + "SplitPaneUI",
                   "SliderUI", basicPrefix + "SliderUI",
            "SeparatorUI", quaquaPrefix + "SeparatorUI",
                  "SpinnerUI", basicPrefix + "SpinnerUI",
         "ToolBarSeparatorUI", basicPrefix + "ToolBarSeparatorUI",
       "PopupMenuSeparatorUI", basicPrefix + "PopupMenuSeparatorUI",
               "TabbedPaneUI", basicPrefix + "TabbedPaneUI",
                 "TextAreaUI", basicPrefix + "TextAreaUI",
                "TextFieldUI", basicPrefix + "TextFieldUI",
            "PasswordFieldUI", basicPrefix + "PasswordFieldUI",
                 "TextPaneUI", basicPrefix + "TextPaneUI",
               "EditorPaneUI", basicPrefix + "EditorPaneUI",
                     "TreeUI", basicPrefix + "TreeUI",
                    "LabelUI", basicPrefix + "LabelUI",
                     "ListUI", basicPrefix + "ListUI",
                  "ToolBarUI", basicPrefix + "ToolBarUI",
                  "ToolTipUI", basicPrefix + "ToolTipUI",
                 "ComboBoxUI", basicPrefix + "ComboBoxUI",
                    "TableUI", basicPrefix + "TableUI",
              "TableHeaderUI", basicPrefix + "TableHeaderUI",
            "InternalFrameUI", basicPrefix + "InternalFrameUI",
              "DesktopPaneUI", basicPrefix + "DesktopPaneUI",
              "DesktopIconUI", basicPrefix + "DesktopIconUI",
               "OptionPaneUI", basicPrefix + "OptionPaneUI",
                    "PanelUI", basicPrefix + "PanelUI",
                 "ViewportUI", basicPrefix + "ViewportUI",
                 "RootPaneUI", basicPrefix + "RootPaneUI",
             */
        };
        
        table.putDefaults(uiDefaults);
    }
    protected void initComponentDefaults(UIDefaults table) {
        FontUIResource menuFont = getMenuFont();
        FontUIResource controlTextFont = getControlTextFont();
        FontUIResource controlTextSmallFont = getControlTextSmallFont();
        
        // NOTE: Comment parts of the code below, to deactivate
        // bug fixes.
        Object[] objects = {
            "Browser.expandedIcon", LookAndFeel.makeIcon(getClass(), "images/Browser.expandedIcon.png"),
            "Browser.expandingIcon", LookAndFeel.makeIcon(getClass(), "images/Browser.expandingIcon.png"),
            "CheckBoxMenuItem.acceleratorFont", menuFont,
            "EditorPane.font", controlTextFont,
            "Menu.acceleratorFont", menuFont,
            "MenuItem.acceleratorFont", menuFont,
            "PasswordField.font", controlTextFont,
            "RadioButtonMenuItem.acceleratorFont", menuFont,
            "Table.font", controlTextFont,
            "TableHeader.font", controlTextSmallFont,
            "TextArea.font", controlTextFont,
            "TextField.font", controlTextFont,
            "TextPane.font", controlTextFont,
            "Tree.font", controlTextFont,
        };
        table.putDefaults(objects);
    }
    
    protected static FontUIResource getControlTextFont() {
        FontUIResource fontuiresource
        = new FontUIResource("Lucida Grande", 0, 13);
        return fontuiresource;
    }
    
    protected static FontUIResource getControlTextSmallFont() {
        FontUIResource fontuiresource
        = new FontUIResource("Lucida Grande", 0, 11);
        return fontuiresource;
    }
    
    protected static FontUIResource getMenuFont() {
        FontUIResource fontuiresource
        = new FontUIResource("Lucida Grande", 0, 14);
        return fontuiresource;
    }
    
    protected static FontUIResource getAlertHeaderFont() {
        FontUIResource fontuiresource
        = new FontUIResource("Lucida Grande", 1, 13);
        return fontuiresource;
    }
    
    protected static FontUIResource getAlertMessageFont() {
        FontUIResource fontuiresource
        = new FontUIResource("Lucida Grande", 0, 11);
        return fontuiresource;
    }
}
