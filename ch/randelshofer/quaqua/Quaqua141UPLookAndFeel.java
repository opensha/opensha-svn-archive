/*
 * @(#)Quaqua141UPLookAndFeel.java  1.0  2003-10-29
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
import java.awt.*;
import java.util.*;

/**
 * The Quaqua141UPLookAndFeel provides quick (and dirty) bug fix for the Aqua
 * Look and Feel which is part of Apple's implementation of the
 * Java 2 Runtime Environment, Standard Edition 1.4.1 which shipped with
 * Mac OS X 10.3 Build 7B85 (Panther).
 * <p>
 * The Quaqua Look and Feel can not be used on other platforms than Mac OS X.
 * <p>
 * This class provides the following bug fixes to the Aqua Look and Feel:
 *
 * <h4>ComboBoxUI</h4>
 * <ul>
 * <li>Combo boxes use the font "Lucida Grande 13" instead of "Lucida Grande 14".
 * </li>
 * </ul>
 *
 * <h4>FileChooserUI</h4>
 * <ul>
 * <li>The FileChooserUI uses a column view similar to the one provided
 * with the native Mac OS X Aqua user interface.</li>
 * <li>The look and feel provides an image showing a house for
 * <code>FileChooser.homeFolderIcon</code> and an icon showing an iMac for
 * <code>FileView.computerIcon</code> instead of an icon showing a computer
 * desktop for both properties. The FileChooserUI with column view does not use
 * these images, but your application might.</li>
 * </ul>
 *
 * <h4>TabbedPaneUI</h4>
 * <ul>
 * <li>Provides a tabbed pane with stacking tabs.
 * <br><b>XXX</b> This fix breaks Tabbed panes with tabs located to the left or 
 * to the right of the tabbed pane!</li>
 * <li>Tabbed panes are rendered in disabled state if the tabbed pane is
 * disabled instead of rendered in enabled state.
 * This fix affects JTabbedPane's.</li>
 * </ul>
 *
 * <h4>MenuUI's</h4>
 * <ul>
 * <li>Draws a border at the top and the bottom of JPopupMenu's.</li>
 * <li>Menu separators are drawn as a blank space instead of a thin black line.
 * This fix affects JSeparators and menu separators.
 * </li>
 * <li>Menu item accelerators use character symbols instead of writing "Meta",
 * "Delete" or "Backspace".</li>
 * <li>Menu item accelerators are highlighted with a white text instead of with
 * black text.</li>
 * <li>The check icon for radio button menu items is drawn smaller than with
 * Apple's LAF.</li>
 * </ul>
 *
 * <h4>TableUI</h4>
 * <ul>
 * <li>Table headers use the font "Lucida Grande 11" instead of "Lucida Grande 13".
 * This fix affects JTable's.
 * </li>
 * </ul>
 *
 * <h4>TreeUI</h4>
 * <ul>
 * <li>Tree disclosure icons are different for collapsed and expanded state.
 * This fix affects user classes which use tree disclosure icons from the LAF.
 * </li>
 * </ul>
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
 * @version 1.0 2003-10-29 Created.
 */
public class Quaqua141UPLookAndFeel extends LookAndFeelProxy {
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
    public Quaqua141UPLookAndFeel() {
        // Our target look and feel is Apple's AquaLookAndFeel.
        try {
            setTarget((LookAndFeel) Class.forName("apple.laf.AquaLookAndFeel").newInstance());
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
        return "Quaqua 1.4.1 Update 1 Look and Feel for Mac OS X";
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
        return "Quaqua 1.4.1 Panther";
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
        String quaqua141UPPrefix = "ch.randelshofer.quaqua.Quaqua141UP";
        
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
              */
      /*
              "RadioButtonUI", basicPrefix + "RadioButtonUI",
             "ToggleButtonUI", basicPrefix + "ToggleButtonUI",*/
              //  "PopupMenuUI", basicPrefix + "PopupMenuUI",
                "SeparatorUI", quaqua141UPPrefix + "SeparatorUI",
            /*  "ProgressBarUI", basicPrefix + "ProgressBarUI",
                "ScrollBarUI", basicPrefix + "ScrollBarUI",
               "ScrollPaneUI", basicPrefix + "ScrollPaneUI",
                "SplitPaneUI", basicPrefix + "SplitPaneUI",
                   "SliderUI", basicPrefix + "SliderUI",
             */
                /*
                  "SpinnerUI", basicPrefix + "SpinnerUI",
         "ToolBarSeparatorUI", basicPrefix + "ToolBarSeparatorUI",
                 */
       "PopupMenuSeparatorUI", quaqua141UPPrefix + "SeparatorUI",
            "TabbedPaneUI", quaquaPrefix + "TabbedPaneUI",
               /*
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
        
        
        // Menu related workarounds work only if useScreenMenuBar is off.
        if (System.getProperty("apple.laf.useScreenMenuBar", "false").equals("false")) {
            uiDefaults = new Object[] {
                "MenuUI", quaquaPrefix + "MenuUI",
                "MenuItemUI", quaquaPrefix + "MenuItemUI",
                "CheckBoxMenuItemUI", quaquaPrefix + "MenuItemUI",
                "RadioButtonMenuItemUI", quaquaPrefix + "MenuItemUI"
            };
            
            table.putDefaults(uiDefaults);
        }
    }
    /**
     * Load the SystemColors into the defaults table.  The keys
     * for SystemColor defaults are the same as the names of
     * the public fields in SystemColor.  If the table is being
     * created on a native Windows platform we use the SystemColor
     * values, otherwise we create color objects whose values match
     * the defaults Windows95 colors.
     */
    protected void initSystemColorDefaults(UIDefaults table) {
        // NOTE: Uncomment parts of the code below, to override additional
        // color defaults of the target look and feel.
        /*
        String[] defaultSystemColors = {
                "desktop", "#005C5C", // Color of the desktop background
          "activeCaption", "#000080", // Color for captions (title bars) when they are active.
      "activeCaptionText", "#FFFFFF", // Text color for text in captions (title bars).
    "activeCaptionBorder", "#C0C0C0", // Border color for caption (title bar) window borders.
        "inactiveCaption", "#808080", // Color for captions (title bars) when not active.
    "inactiveCaptionText", "#C0C0C0", // Text color for text in inactive captions (title bars).
  "inactiveCaptionBorder", "#C0C0C0", // Border color for inactive caption (title bar) window borders.
                 "window", "#FFFFFF", // Default color for the interior of windows
           "windowBorder", "#000000", // ???
             "windowText", "#000000", // ???
                   "menu", "#C0C0C0", // Background color for menus
               "menuText", "#000000", // Text color for menus
                   "text", "#C0C0C0", // Text background color
               "textText", "#000000", // Text foreground color
          "textHighlight", "#000080", // Text background color when selected
      "textHighlightText", "#FFFFFF", // Text color when selected
       "textInactiveText", "#808080", // Text color when disabled
                "control", "#C0C0C0", // Default color for controls (buttons, sliders, etc)
            "controlText", "#000000", // Default color for text in controls
       "controlHighlight", "#C0C0C0", // Specular highlight (opposite of the shadow)
     "controlLtHighlight", "#FFFFFF", // Highlight color for controls
          "controlShadow", "#808080", // Shadow color for controls
        "controlDkShadow", "#000000", // Dark shadow color for controls
              "scrollbar", "#E0E0E0", // Scrollbar background (usually the "track")
                   "info", "#FFFFE1", // ???
               "infoText", "#000000"  // ???
        };
         
        loadSystemColors(table, defaultSystemColors, isNativeLookAndFeel());
         */
    }
    protected void initComponentDefaults(UIDefaults table) {
        FontUIResource menuFont = getMenuFont();
        FontUIResource controlTextFont = getControlTextFont();
        FontUIResource controlTextSmallFont = getControlTextSmallFont();
        
        ColorUIResource disabledForeground = new ColorUIResource(new Color(128, 128, 128));
        
        IconUIResource radioButtonMenuItemCheckIcon = new IconUIResource(
        new ButtonStateIcon(
        null,
        new ImageIcon(getClass().getResource("images/RadioButtonMenuItem.checkIcon.png")),
        new ImageIcon(getClass().getResource("images/RadioButtonMenuItem.armedCheckIcon.png")),
        null,
        new ImageIcon(getClass().getResource("images/RadioButtonMenuItem.disabledCheckIcon.png"))
        ));
        
        /*
        ColorPaintUIResource menuSelectionBackground =
        QuaquaImageFactory.getMenuSelectionBackgroundColorUIResource();
         */
        ColorUIResource menuSelectionForeground = new ColorUIResource(255,255,255);
        
        // *** Shared Borders
        Object popupMenuBorder = new UIDefaults.ProxyLazyValue("ch.randelshofer.quaqua.QuaquaMenuBorder");
        
        // ** TabbedBane value objects
        
        // Metal LAF
        /*
        Object tabbedPaneContentBorderInsets = new InsetsUIResource(2, 2, 3, 3);
        Object tabbedPaneSelectedTabPadInsets = new InsetsUIResource(2, 2, 2, 1);
        Object tabbedPaneTabAreaInsets = new InsetsUIResource(4, 2, 0, 6);
        Object tabbedPaneTabInsets = new InsetsUIResource(0, 9, 1, 9);
         */
        // Apple Aqua LAF
        /*
        Object tabbedPaneTabInsets = new InsetsUIResource(0, 10, 3, 10);
        Object tabbedPaneTabPadInsets = new InsetsUIResource(2, 2, 2, 1);
        Object tabbedPaneTabAreaInsets = new InsetsUIResource(3, 9, -1, 9);
        Object tabbedPaneContentBorderInsets = new InsetsUIResource(8, 0, 0, 0);
        Object tabbedPaneLeftTabInsets = new InsetsUIResource(10, 0, 10, 3);
        Object tabbedPaneRightTabInsets = new InsetsUIResource(10, 0, 10, 3);
         */
        // Quaqua 1.4.1 Update 1 LAF
        Object tabbedPaneContentBorderInsets = new InsetsUIResource(5, 6, 9, 5);
        Object tabbedPaneTabPadInsets = new InsetsUIResource(2, 2, 2, 1);
        Object tabbedPaneTabAreaInsets = new InsetsUIResource(2, 6, 0, 6);
        Object tabbedPaneTabInsets = new InsetsUIResource(1, 10, 4, 9);
        Object menuMargin = new InsetsUIResource(0, 10, 0, 10);
        
        Object[] objects = {
            "Browser.expandedIcon", LookAndFeel.makeIcon(getClass(), "images/Browser.expandedIcon.png"),
            "Browser.expandingIcon", LookAndFeel.makeIcon(getClass(), "images/Browser.expandingIcon.png"),
            "ComboBox.font", controlTextFont,
            "FileChooser.homeFolderIcon", LookAndFeel.makeIcon(getClass(), "images/FileChooser.homeFolderIcon.png"),
            "FileView.computerIcon", LookAndFeel.makeIcon(getClass(), "images/FileView.computerIcon.png"),
            "Menu.margin", menuMargin,
            "MenuItem.acceleratorSelectionForeground", menuSelectionForeground,
            "PopupMenu.border", popupMenuBorder,
            "RadioButtonMenuItem.checkIcon", radioButtonMenuItemCheckIcon,
            //"RadioButtonMenuItem.selectedCheckIcon", LookAndFeel.makeIcon(getClass(), "images/RadioButtonMenuItem.selectedCheckIcon.png"),
            //"RadioButtonMenuItem.disabledCheckIcon", LookAndFeel.makeIcon(getClass(), "images/RadioButtonMenuItem.disabledCheckIcon.png"),
            "Separator.foreground", new ColorUIResource(139,139,139),
            "Separator.highlight", new ColorUIResource(243,243,243),
            "Separator.shadow", new ColorUIResource(213,213,213),
            "TabbedPane.disabledForeground", disabledForeground,
            "TabbedPane.tabInsets", tabbedPaneTabInsets,
            "TabbedPane.selectedTabPadInsets", tabbedPaneTabPadInsets,
            "TabbedPane.tabAreaInsets", tabbedPaneTabAreaInsets,
            "TabbedPane.contentBorderInsets", tabbedPaneContentBorderInsets,
            "TableHeader.font", controlTextSmallFont,
            "Tree.collapsedIcon", LookAndFeel.makeIcon(getClass(), "images/Tree.collapsedIcon.png"),
            "Tree.expandedIcon", LookAndFeel.makeIcon(getClass(), "images/Tree.expandedIcon.png"),
        };
        table.putDefaults(objects);
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


