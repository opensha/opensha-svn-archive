/*
 * @(#)QuaquaTabbedPaneUI.java 1.1  2003-11-12
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

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.plaf.*;
import java.io.Serializable;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import java.util.*;
/**
 * A replacement for the AquaTabbedPaneUI.
 * <p>
 * This class provides the following workarounds for issues in Apple's
 * implementation of the Aqua Look and Feel in Java 1.4.1:
 * <ul>
 * <li>Tabs of tabbed panes are stacked instead of moved into a popup menu,
 * if not enough space is available to render all tabs in a single line.
 * This fix affects JTabbedPane's.<br>
 * <b>XXX</b> This fix breaks Tabbed panes with tabs located to the left or to the
 * right of the tabbed pane!!!
 * </li>
 * <li>Tabbed panes are rendered in disabled state if the tabbed pane is
 * disabled instead of rendered in enabled state.
 * This fix affects JTabbedPane's.
 * </li>
 * </ul>
 *
 * @author Werner Randelshofer, Staldenmattweg 2, CH-6405 Immensee, Switzerland
 * @version 1.1 2003-11-12 Support for tabs on the left and on the right added.
 * <br>1.0.2 2003-10-04 Content Border Insets match now visually the insets
 * of Apple's original Aqua LAF.
 * <br>1.0.1 2003-09-12 Shift values in method getTabLabelShiftY changed.
 * <br>1.0 2003-07-20 Created.
 */
public class QuaquaTabbedPaneUI extends BasicTabbedPaneUI {
    
    protected int minTabWidth = 40;
    protected Color tabAreaBackground;
    protected Color selectColor;
    protected Color selectHighlight;
    protected Color disabledForeground;
    private int tabCount;
    
    /**
     * This is the border painted around the content area.
     */
    private static final ImageBevelBorder contentBorder = new ImageBevelBorder(
    Toolkit.getDefaultToolkit().createImage(QuaquaTabbedPaneUI.class.getResource("images/TabbedPane.contentBorder.png")),
    new Insets(8, 7, 8, 7),
    new Insets(1, 3, 5, 3),
    false
    );
    /**
     * This is the bar used when the tabs are at the top.
     */
    private static final ImageBevelBorder barTopBorder = new ImageBevelBorder(
    Toolkit.getDefaultToolkit().createImage(QuaquaTabbedPaneUI.class.getResource("images/TabbedPane.barTop.png")),
    new Insets(0, 1, 0, 1)
    );
    
    /**
     * This is the inactive bar used when the tabs are at the top.
     */
    private static final ImageBevelBorder barTopBorderInactive = new ImageBevelBorder(
    Toolkit.getDefaultToolkit().createImage(QuaquaTabbedPaneUI.class.getResource("images/TabbedPane.barTopI.png")),
    new Insets(0, 1, 0, 1)
    );
    
    /**
     * This is the disabled bar used when the tabs are at the top.
     */
    private static final ImageBevelBorder barTopBorderDisabled= new ImageBevelBorder(
    Toolkit.getDefaultToolkit().createImage(QuaquaTabbedPaneUI.class.getResource("images/TabbedPane.barTopD.png")),
    new Insets(0, 1, 0, 1)
    );
    /**
     * This is the bar used when the tabs are at the bottom.
     */
    private static final ImageBevelBorder barBottomBorder = new ImageBevelBorder(
    Toolkit.getDefaultToolkit().createImage(QuaquaTabbedPaneUI.class.getResource("images/TabbedPane.barBottom.png")),
    new Insets(0, 1, 0, 1)
    );
    
    /**
     * This is the inactive bar used when the tabs are at the bottom.
     */
    private static final ImageBevelBorder barBottomBorderInactive = new ImageBevelBorder(
    Toolkit.getDefaultToolkit().createImage(QuaquaTabbedPaneUI.class.getResource("images/TabbedPane.barBottomI.png")),
    new Insets(0, 1, 0, 1)
    );
    
    /**
     * This is the disabled bar used when the tabs are at the bottom.
     */
    private static final ImageBevelBorder barBottomBorderDisabled= new ImageBevelBorder(
    Toolkit.getDefaultToolkit().createImage(QuaquaTabbedPaneUI.class.getResource("images/TabbedPane.barBottomD.png")),
    new Insets(0, 1, 0, 1)
    );
    
    //--
    /**
     * This is the bar used when the tabs are at the right.
     */
    private static final ImageBevelBorder barRightBorder = new ImageBevelBorder(
    Toolkit.getDefaultToolkit().createImage(QuaquaTabbedPaneUI.class.getResource("images/TabbedPane.barRight.png")),
    new Insets(1, 0, 1, 0)
    );
    
    /**
     * This is the inactive bar used when the tabs are at the right.
     */
    private static final ImageBevelBorder barRightBorderInactive = new ImageBevelBorder(
    Toolkit.getDefaultToolkit().createImage(QuaquaTabbedPaneUI.class.getResource("images/TabbedPane.barRightI.png")),
    new Insets(1, 0, 1, 0)
    );
    
    /**
     * This is the disabled bar used when the tabs are at the right.
     */
    private static final ImageBevelBorder barRightBorderDisabled= new ImageBevelBorder(
    Toolkit.getDefaultToolkit().createImage(QuaquaTabbedPaneUI.class.getResource("images/TabbedPane.barRightD.png")),
    new Insets(1, 0, 1, 0)
    );
    //--
    /**
     * This is the bar used when the tabs are at the right.
     */
    private static final ImageBevelBorder barLeftBorder = new ImageBevelBorder(
    Toolkit.getDefaultToolkit().createImage(QuaquaTabbedPaneUI.class.getResource("images/TabbedPane.barLeft.png")),
    new Insets(1, 0, 1, 0)
    );
    
    /**
     * This is the inactive bar used when the tabs are at the right.
     */
    private static final ImageBevelBorder barLeftBorderInactive = new ImageBevelBorder(
    Toolkit.getDefaultToolkit().createImage(QuaquaTabbedPaneUI.class.getResource("images/TabbedPane.barLeftI.png")),
    new Insets(1, 0, 1, 0)
    );
    
    /**
     * This is the disabled bar used when the tabs are at the left.
     */
    private static final ImageBevelBorder barLeftBorderDisabled= new ImageBevelBorder(
    Toolkit.getDefaultToolkit().createImage(QuaquaTabbedPaneUI.class.getResource("images/TabbedPane.barLeftD.png")),
    new Insets(1, 0, 1, 0)
    );
    //--
    
    /**
     * This is a tab when the tabs are at the top.
     */
    private static final ImageBevelBorder tabTopBorder = new ImageBevelBorder(
    Toolkit.getDefaultToolkit().createImage(QuaquaTabbedPaneUI.class.getResource("images/TabbedPane.tabTop.png")),
    new Insets(12, 8, 11, 8)
    );
    /**
     * This is a selected tab when the tabs are at the top.
     */
    private static final ImageBevelBorder tabTopBorderSelected = new ImageBevelBorder(
    Toolkit.getDefaultToolkit().createImage(QuaquaTabbedPaneUI.class.getResource("images/TabbedPane.tabTopS.png")),
    new Insets(12, 8, 11, 8)
    );
    /**
     * This is an inactive tab when the tabs are at the top.
     */
    private static final ImageBevelBorder tabTopBorderInactive = new ImageBevelBorder(
    Toolkit.getDefaultToolkit().createImage(QuaquaTabbedPaneUI.class.getResource("images/TabbedPane.tabTopI.png")),
    new Insets(12, 8, 11, 8)
    );
    /**
     * This is a disabled tab when the tabs are at the top.
     */
    private static final ImageBevelBorder tabTopBorderDisabled = new ImageBevelBorder(
    Toolkit.getDefaultToolkit().createImage(QuaquaTabbedPaneUI.class.getResource("images/TabbedPane.tabTopD.png")),
    new Insets(12, 8, 11, 8)
    );
    /**
     * This is a disabled selected tab when the tabs are at the top.
     */
    private static final ImageBevelBorder tabTopBorderDisabledSelected = new ImageBevelBorder(
    Toolkit.getDefaultToolkit().createImage(QuaquaTabbedPaneUI.class.getResource("images/TabbedPane.tabTopDS.png")),
    new Insets(12, 8, 11, 8)
    );
    //--
    /**
     * This is a tab when the tabs are at the bottom.
     */
    private static final ImageBevelBorder tabBottomBorder = new ImageBevelBorder(
    Toolkit.getDefaultToolkit().createImage(QuaquaTabbedPaneUI.class.getResource("images/TabbedPane.tabBottom.png")),
    new Insets(11, 8, 12, 8)
    );
    /**
     * This is a selected tab when the tabs are at the bottom.
     */
    private static final ImageBevelBorder tabBottomBorderSelected = new ImageBevelBorder(
    Toolkit.getDefaultToolkit().createImage(QuaquaTabbedPaneUI.class.getResource("images/TabbedPane.tabBottomS.png")),
    new Insets(11, 8, 12, 8)
    );
    /**
     * This is an inactive tab when the tabs are at the bottom.
     */
    private static final ImageBevelBorder tabBottomBorderInactive = new ImageBevelBorder(
    Toolkit.getDefaultToolkit().createImage(QuaquaTabbedPaneUI.class.getResource("images/TabbedPane.tabBottomI.png")),
    new Insets(11, 8, 12, 8)
    );
    /**
     * This is a disabled tab when the tabs are at the bottom.
     */
    private static final ImageBevelBorder tabBottomBorderDisabled = new ImageBevelBorder(
    Toolkit.getDefaultToolkit().createImage(QuaquaTabbedPaneUI.class.getResource("images/TabbedPane.tabBottomD.png")),
    new Insets(11, 8, 12, 8)
    );
    /**
     * This is a disabled selected tab when the tabs are at the bottom.
     */
    private static final ImageBevelBorder tabBottomBorderDisabledSelected = new ImageBevelBorder(
    Toolkit.getDefaultToolkit().createImage(QuaquaTabbedPaneUI.class.getResource("images/TabbedPane.tabBottomDS.png")),
    new Insets(11, 8, 12, 8)
    );
    
    /**
     * This is a tab when the tabs are at the right.
     *//*
    private static final ImageBevelBorder tabRightBorder = new ImageBevelBorder(
    Toolkit.getDefaultToolkit().createImage(QuaquaTabbedPaneUI.class.getResource("images/TabbedPane.tabRight.png")),
    new Insets(4, 12, 4, 11)
    );*/
    private static final Border tabRightBorder = 
    new DoubleBevelBorder(
    new ImageBevelBorder(
    Toolkit.getDefaultToolkit().createImage(QuaquaTabbedPaneUI.class.getResource("images/TabbedPane.tabRight1.png")),
    new Insets(4, 9, 4, 2)
    ),
    new ImageBevelBorder(
    Toolkit.getDefaultToolkit().createImage(QuaquaTabbedPaneUI.class.getResource("images/TabbedPane.tabRight2.png")),
    new Insets(4, 1, 4, 10)
    )
    );
    /**
     * This is a selected tab when the tabs are at the right.
     *//*
    private static final ImageBevelBorder tabRightBorderSelected = new ImageBevelBorder(
    Toolkit.getDefaultToolkit().createImage(QuaquaTabbedPaneUI.class.getResource("images/TabbedPane.tabRightS.png")),
    new Insets(4, 13, 4, 11)
    );*/
    private static final Border tabRightBorderSelected = 
    new DoubleBevelBorder(
    new ImageBevelBorder(
    Toolkit.getDefaultToolkit().createImage(QuaquaTabbedPaneUI.class.getResource("images/TabbedPane.tabRightS1.png")),
    new Insets(4, 10, 4, 2)
    ),
    new ImageBevelBorder(
    Toolkit.getDefaultToolkit().createImage(QuaquaTabbedPaneUI.class.getResource("images/TabbedPane.tabRightS2.png")),
    new Insets(4, 1, 4, 10)
    )
    );
    /**
     * This is an inactive tab when the tabs are at the right.
     *//*
    private static final ImageBevelBorder tabRightBorderInactive = new ImageBevelBorder(
    Toolkit.getDefaultToolkit().createImage(QuaquaTabbedPaneUI.class.getResource("images/TabbedPane.tabRightI.png")),
    new Insets(4, 15, 4, 9)
    );*/
    private static final Border tabRightBorderInactive = 
    new DoubleBevelBorder(
    new ImageBevelBorder(
    Toolkit.getDefaultToolkit().createImage(QuaquaTabbedPaneUI.class.getResource("images/TabbedPane.tabRightI1.png")),
    new Insets(4, 10, 4, 2)
    ),
    new ImageBevelBorder(
    Toolkit.getDefaultToolkit().createImage(QuaquaTabbedPaneUI.class.getResource("images/TabbedPane.tabRightI2.png")),
    new Insets(4, 1, 4, 10)
    )
    );
    /**
     * This is a disabled tab when the tabs are at the right.
     *//*
    private static final ImageBevelBorder tabRightBorderDisabled = new ImageBevelBorder(
    Toolkit.getDefaultToolkit().createImage(QuaquaTabbedPaneUI.class.getResource("images/TabbedPane.tabRightD.png")),
    new Insets(4, 12, 4, 11)
    );*/
    private static final Border tabRightBorderDisabled = 
    new DoubleBevelBorder(
    new ImageBevelBorder(
    Toolkit.getDefaultToolkit().createImage(QuaquaTabbedPaneUI.class.getResource("images/TabbedPane.tabRightD1.png")),
    new Insets(4, 9, 4, 2)
    ),
    new ImageBevelBorder(
    Toolkit.getDefaultToolkit().createImage(QuaquaTabbedPaneUI.class.getResource("images/TabbedPane.tabRightD2.png")),
    new Insets(4, 1, 4, 10)
    )
    );
    /**
     * This is a disabled selected tab when the tabs are at the right.
     *//*
    private static final ImageBevelBorder tabRightBorderDisabledSelected = new ImageBevelBorder(
    Toolkit.getDefaultToolkit().createImage(QuaquaTabbedPaneUI.class.getResource("images/TabbedPane.tabRightDS.png")),
    new Insets(4, 15, 4, 9)
    );*/
    private static final Border tabRightBorderDisabledSelected = 
    new DoubleBevelBorder(
    new ImageBevelBorder(
    Toolkit.getDefaultToolkit().createImage(QuaquaTabbedPaneUI.class.getResource("images/TabbedPane.tabRightDS1.png")),
    new Insets(4, 10, 4, 2)
    ),
    new ImageBevelBorder(
    Toolkit.getDefaultToolkit().createImage(QuaquaTabbedPaneUI.class.getResource("images/TabbedPane.tabRightDS2.png")),
    new Insets(4, 1, 4, 10)
    )
    );
    
    /**
     * This is a tab when the tabs are at the left.
     */
    /*
    private static final ImageBevelBorder tabLeftBorder = new ImageBevelBorder(
    Toolkit.getDefaultToolkit().createImage(QuaquaTabbedPaneUI.class.getResource("images/TabbedPane.tabLeft.png")),
    new Insets(4, 13, 4, 10)
    );*/
    private static final Border tabLeftBorder = 
    new DoubleBevelBorder(
    new ImageBevelBorder(
    Toolkit.getDefaultToolkit().createImage(QuaquaTabbedPaneUI.class.getResource("images/TabbedPane.tabLeft1.png")),
    new Insets(4, 10, 4, 1)
    ),
    new ImageBevelBorder(
    Toolkit.getDefaultToolkit().createImage(QuaquaTabbedPaneUI.class.getResource("images/TabbedPane.tabLeft2.png")),
    new Insets(4, 2, 4, 9)
    )
    );
    /**
     * This is a selected tab when the tabs are at the left.
     */
    /*
    private static final ImageBevelBorder tabLeftBorderSelected = new ImageBevelBorder(
    Toolkit.getDefaultToolkit().createImage(QuaquaTabbedPaneUI.class.getResource("images/TabbedPane.tabLeftS.png")),
    new Insets(4, 13, 4, 11)
    );*/
    private static final Border tabLeftBorderSelected = 
    new DoubleBevelBorder(
    new ImageBevelBorder(
    Toolkit.getDefaultToolkit().createImage(QuaquaTabbedPaneUI.class.getResource("images/TabbedPane.tabLeftS1.png")),
    new Insets(4, 10, 4, 1)
    ),
    new ImageBevelBorder(
    Toolkit.getDefaultToolkit().createImage(QuaquaTabbedPaneUI.class.getResource("images/TabbedPane.tabLeftS2.png")),
    new Insets(4, 2, 4, 10)
    )
    );
    /**
     * This is an inactive tab when the tabs are at the left.
     *//*
    private static final ImageBevelBorder tabLeftBorderInactive = new ImageBevelBorder(
    Toolkit.getDefaultToolkit().createImage(QuaquaTabbedPaneUI.class.getResource("images/TabbedPane.tabLeftI.png")),
    new Insets(4, 9, 4, 15)
    );*/
    private static final Border tabLeftBorderInactive = 
    new DoubleBevelBorder(
    new ImageBevelBorder(
    Toolkit.getDefaultToolkit().createImage(QuaquaTabbedPaneUI.class.getResource("images/TabbedPane.tabLeftI1.png")),
    new Insets(4, 10, 4, 1)
    ),
    new ImageBevelBorder(
    Toolkit.getDefaultToolkit().createImage(QuaquaTabbedPaneUI.class.getResource("images/TabbedPane.tabLeftI2.png")),
    new Insets(4, 2, 4, 10)
    )
    );
    
    /**
     * This is a disabled tab when the tabs are at the left.
     *//*
    private static final ImageBevelBorder tabLeftBorderDisabled = new ImageBevelBorder(
    Toolkit.getDefaultToolkit().createImage(QuaquaTabbedPaneUI.class.getResource("images/TabbedPane.tabLeftD.png")),
    new Insets(4, 13, 4, 10)
    );*/
    private static final Border tabLeftBorderDisabled = 
    new DoubleBevelBorder(
    new ImageBevelBorder(
    Toolkit.getDefaultToolkit().createImage(QuaquaTabbedPaneUI.class.getResource("images/TabbedPane.tabLeftD1.png")),
    new Insets(4, 10, 4, 1)
    ),
    new ImageBevelBorder(
    Toolkit.getDefaultToolkit().createImage(QuaquaTabbedPaneUI.class.getResource("images/TabbedPane.tabLeftD2.png")),
    new Insets(4, 2, 4, 9)
    )
    );
    /**
     * This is a disabled selected tab when the tabs are at the left.
     *//*
    private static final ImageBevelBorder tabLeftBorderDisabledSelected = new ImageBevelBorder(
    Toolkit.getDefaultToolkit().createImage(QuaquaTabbedPaneUI.class.getResource("images/TabbedPane.tabLeftDS.png")),
    new Insets(4, 9, 4, 15)
    );*/
    private static final Border tabLeftBorderDisabledSelected = 
    new DoubleBevelBorder(
    new ImageBevelBorder(
    Toolkit.getDefaultToolkit().createImage(QuaquaTabbedPaneUI.class.getResource("images/TabbedPane.tabLeftDS1.png")),
    new Insets(4, 10, 4, 1)
    ),
    new ImageBevelBorder(
    Toolkit.getDefaultToolkit().createImage(QuaquaTabbedPaneUI.class.getResource("images/TabbedPane.tabLeftDS2.png")),
    new Insets(4, 2, 4, 10)
    )
    );
    
    private Hashtable mnemonicToIndexMap;
    /**
     * InputMap used for mnemonics. Only non-null if the JTabbedPane has
     * mnemonics associated with it. Lazily created in initMnemonics.
     */
    private InputMap mnemonicInputMap;
    
    
    public static ComponentUI createUI( JComponent x ) {
        return new QuaquaTabbedPaneUI();
    }
    
    protected LayoutManager createLayoutManager() {
        /* XXX - This needs JDK 1.4 to work. We do not support scroll tab layout.
        if (tabPane.getTabLayoutPolicy() == JTabbedPane.SCROLL_TAB_LAYOUT) {
            return super.createLayoutManager();
        }*/
        return new TabbedPaneLayout();
    }
    
    protected void installDefaults() {
        super.installDefaults();
        
        tabAreaBackground = UIManager.getColor("TabbedPane.tabAreaBackground");
        selectColor = UIManager.getColor("TabbedPane.selected");
        selectHighlight = UIManager.getColor("TabbedPane.selectHighlight");
        selectColor = UIManager.getColor("MenuItem.selectionBackground");
        disabledForeground = UIManager.getColor("TabbedPane.disabledForeground");
    }
    
    
    protected void paintTabBorder( Graphics g, int tabPlacement,
    int tabIndex, int x, int y, int w, int h,
    boolean isSelected) {
        int bottom = y + (h-1);
        int right = x + (w-1);
        
        switch ( tabPlacement ) {
            case LEFT:
                paintLeftTabBorder(tabIndex, g, x, y, w, h, bottom, right, isSelected);
                break;
            case BOTTOM:
                paintBottomTabBorder(tabIndex, g, x, y, w, h, bottom, right, isSelected);
                break;
            case RIGHT:
                paintRightTabBorder(tabIndex, g, x, y, w, h, bottom, right, isSelected);
                break;
            case TOP:
            default:
                paintTopTabBorder(tabIndex, g, x, y, w, h, bottom, right, isSelected);
        }
    }
    
    
    protected void paintTopTabBorder( int tabIndex, Graphics g,
    int x, int y, int w, int h,
    int btm, int rght,
    boolean isSelected ) {
        if (isSelected) {
            if (tabPane.isEnabled()) {
                tabTopBorderSelected.paintBorder(tabPane, g, x - 1, y - 1 , w + 3, h + 2);
            } else {
                tabTopBorderDisabledSelected.paintBorder(tabPane, g, x - 1, y - 1 , w + 3, h + 2);
            }
        } else {
            if (tabPane.isEnabled()) {
                tabTopBorder.paintBorder(tabPane, g, x - 1, y - 1, w + 3, h + 1);
            } else {
                tabTopBorderDisabled.paintBorder(tabPane, g, x - 1, y - 1 , w + 3, h + 1);
            }
        }
        /*
        int currentRun = getRunForTab( tabPane.getTabCount(), tabIndex );
        int lastIndex = lastTabInRun( tabPane.getTabCount(), currentRun );
        int firstIndex = tabRuns[ currentRun ];
        boolean leftToRight = QuaquaGraphicsUtils.isLeftToRight(tabPane);
        int bottom = h - 1;
        int right = w - 1;
         
        //
        // Paint Gap
        //
         
        if ( shouldFillGap( currentRun, tabIndex, x, y ) ) {
            g.translate( x, y );
         
            if ( leftToRight ) {
                g.setColor( getColorForGap( currentRun, x, y + 1 ) );
                g.fillRect( 1, 0, 5, 3 );
                g.fillRect( 1, 3, 2, 2 );
            } else {
                g.setColor( getColorForGap( currentRun, x + w - 1, y + 1 ) );
                g.fillRect( right - 5, 0, 5, 3 );
                g.fillRect( right - 2, 3, 2, 2 );
            }
         
            g.translate( -x, -y );
        }
         
        g.translate( x, y );
         
        //
        // Paint Border
        //
         
        g.setColor( darkShadow );
         
        if ( leftToRight ) {
         
            // Paint slant
            g.drawLine( 1, 5, 6, 0 );
         
            // Paint top
            g.drawLine( 6, 0, right, 0 );
         
            // Paint right
            if ( tabIndex==lastIndex ) {
                // last tab in run
                g.drawLine( right, 1, right, bottom );
            }
         
            // Paint left
            if ( tabIndex != tabRuns[ runCount - 1 ] ) {
                // not the first tab in the last run
                g.drawLine( 0, 0, 0, bottom );
            } else {
                // the first tab in the last run
                g.drawLine( 0, 6, 0, bottom );
            }
        } else {
         
            // Paint slant
            g.drawLine( right - 1, 5, right - 6, 0 );
         
            // Paint top
            g.drawLine( right - 6, 0, 0, 0 );
         
            // Paint right
            if ( tabIndex != tabRuns[ runCount - 1 ] ) {
                // not the first tab in the last run
                g.drawLine( right, 0, right, bottom );
            } else {
                // the first tab in the last run
                g.drawLine( right, 6, right, bottom );
            }
         
            // Paint left
            if ( tabIndex==lastIndex ) {
                // last tab in run
                g.drawLine( 0, 1, 0, bottom );
            }
        }
         
        //
        // Paint Highlight
        //
         
        g.setColor( isSelected ? selectHighlight : highlight );
         
        if ( leftToRight ) {
         
            // Paint slant
            g.drawLine( 1, 6, 6, 1 );
         
            // Paint top
            g.drawLine( 6, 1, right, 1 );
         
            // Paint left
            g.drawLine( 1, 6, 1, bottom );
         
            // paint highlight in the gap on tab behind this one
            // on the left end (where they all line up)
            if ( tabIndex==firstIndex && tabIndex!=tabRuns[runCount - 1] ) {
                //  first tab in run but not first tab in last run
                if (tabPane.getSelectedIndex()==tabRuns[currentRun+1]) {
                    // tab in front of selected tab
                    g.setColor( selectHighlight );
                }
                else {
                    // tab in front of normal tab
                    g.setColor( highlight );
                }
                g.drawLine( 1, 0, 1, 4 );
            }
        } else {
         
            // Paint slant
            g.drawLine( right - 1, 6, right - 6, 1 );
         
            // Paint top
            g.drawLine( right - 6, 1, 1, 1 );
         
            // Paint left
            if ( tabIndex==lastIndex ) {
                // last tab in run
                g.drawLine( 1, 1, 1, bottom );
            } else {
                g.drawLine( 0, 1, 0, bottom );
            }
        }
         
        g.translate( -x, -y );
         */
    }
    
    protected boolean shouldFillGap( int currentRun, int tabIndex, int x, int y ) {
        boolean result = false;
        
        if ( currentRun == runCount - 2 ) {  // If it's the second to last row.
            Rectangle lastTabBounds = getTabBounds( tabPane, tabPane.getTabCount() - 1 );
            Rectangle tabBounds = getTabBounds( tabPane, tabIndex );
            if (QuaquaGraphicsUtils.isLeftToRight(tabPane)) {
                int lastTabRight = lastTabBounds.x + lastTabBounds.width - 1;
                
                // is the right edge of the last tab to the right
                // of the left edge of the current tab?
                if ( lastTabRight > tabBounds.x + 2 ) {
                    return true;
                }
            } else {
                int lastTabLeft = lastTabBounds.x;
                int currentTabRight = tabBounds.x + tabBounds.width - 1;
                
                // is the left edge of the last tab to the left
                // of the right edge of the current tab?
                if ( lastTabLeft < currentTabRight - 2 ) {
                    return true;
                }
            }
        } else {
            // fill in gap for all other rows except last row
            result = currentRun != runCount - 1;
        }
        
        return result;
    }
    
    protected Color getColorForGap( int currentRun, int x, int y ) {
        final int shadowWidth = 4;
        int selectedIndex = tabPane.getSelectedIndex();
        int startIndex = tabRuns[ currentRun + 1 ];
        int endIndex = lastTabInRun( tabPane.getTabCount(), currentRun + 1 );
        int tabOverGap = -1;
        // Check each tab in the row that is 'on top' of this row
        for ( int i = startIndex; i <= endIndex; ++i ) {
            Rectangle tabBounds = getTabBounds( tabPane, i );
            int tabLeft = tabBounds.x;
            int tabRight = (tabBounds.x + tabBounds.width) - 1;
            // Check to see if this tab is over the gap
            if ( QuaquaGraphicsUtils.isLeftToRight(tabPane) ) {
                if ( tabLeft <= x && tabRight - shadowWidth > x ) {
                    return selectedIndex == i ? selectColor : tabPane.getBackgroundAt( i );
                }
            }
            else {
                if ( tabLeft + shadowWidth < x && tabRight >= x ) {
                    return selectedIndex == i ? selectColor : tabPane.getBackgroundAt( i );
                }
            }
        }
        
        return tabPane.getBackground();
    }
    
    protected void paintLeftTabBorder( int tabIndex, Graphics g,
    int x, int y, int w, int h,
    int btm, int rght,
    boolean isSelected ) {
        if (isSelected) {
            if (tabPane.isEnabled()) {
                tabLeftBorderSelected.paintBorder(tabPane, g, x - 1, y , w + 2, h + 1 );
            } else {
                tabLeftBorderDisabledSelected.paintBorder(tabPane, g, x - 1, y , w + 2, h + 1);
            }
        } else {
            if (tabPane.isEnabled()) {
                tabLeftBorder.paintBorder(tabPane, g, x - 1, y, w + 1, h + 1);
            } else {
                tabLeftBorderDisabled.paintBorder(tabPane, g, x - 1, y, w + 1, h + 1);
            }
        }
        /*
        int tabCount = tabPane.getTabCount();
        int currentRun = getRunForTab( tabCount, tabIndex );
        int lastIndex = lastTabInRun( tabCount, currentRun );
        int firstIndex = tabRuns[ currentRun ];
         
        g.translate( x, y );
         
        int bottom = h - 1;
        int right = w - 1;
         
        //
        // Paint part of the tab above
        //
         
        if ( tabIndex != firstIndex ) {
            g.setColor( tabPane.getSelectedIndex() == tabIndex - 1 ?
            selectColor :
                tabPane.getBackgroundAt( tabIndex - 1 ) );
                g.fillRect( 2, 0, 4, 3 );
                g.drawLine( 2, 3, 2, 3 );
        }
         
         
        //
        // Paint Highlight
        //
         
        g.setColor( isSelected ? selectHighlight : highlight );
         
        // Paint slant
        g.drawLine( 1, 6, 6, 1 );
         
        // Paint top
        g.drawLine( 6, 1, right, 1 );
         
        // Paint left
        g.drawLine( 1, 6, 1, bottom );
         
        if ( tabIndex != firstIndex ) {
            g.setColor( tabPane.getSelectedIndex() == tabIndex - 1 ?
            selectHighlight :
                highlight );
                g.drawLine( 1, 0, 1, 4 );
        }
         
        //
        // Paint Border
        //
         
        g.setColor( darkShadow );
         
        // Paint slant
        g.drawLine( 1, 5, 6, 0 );
         
        // Paint top
        g.drawLine( 6, 0, right, 0 );
         
        // Paint left
        if ( tabIndex != firstIndex ) {
            g.drawLine( 0, 0, 0, bottom );
        } else {
            g.drawLine( 0, 6, 0, bottom );
        }
         
        // Paint bottom
        if ( tabIndex == lastIndex ) {
            g.drawLine( 0, bottom, right, bottom );
        }
         
        g.translate( -x, -y );
         */
    }
    
    
    protected void paintBottomTabBorder( int tabIndex, Graphics g,
    int x, int y, int w, int h,
    int btm, int rght,
    boolean isSelected ) {
        if (isSelected) {
            if (tabPane.isEnabled()) {
                tabBottomBorderSelected.paintBorder(tabPane, g, x - 1, y - 1 , w + 3, h + 2);
            } else {
                tabBottomBorderDisabledSelected.paintBorder(tabPane, g, x - 1, y - 1 , w + 3, h + 2);
            }
        } else {
            if (tabPane.isEnabled()) {
                tabBottomBorder.paintBorder(tabPane, g, x - 1, y , w + 3, h + 1);
            } else {
                tabBottomBorderDisabled.paintBorder(tabPane, g, x - 1, y , w + 3, h + 1);
            }
        }
    }
    
    protected void paintRightTabBorder( int tabIndex, Graphics g,
    int x, int y, int w, int h,
    int btm, int rght,
    boolean isSelected ) {
        if (isSelected) {
            if (tabPane.isEnabled()) {
                tabRightBorderSelected.paintBorder(tabPane, g, x - 1, y , w + 2, h + 1 );
            } else {
                tabRightBorderDisabledSelected.paintBorder(tabPane, g, x - 1, y , w + 2, h + 1);
            }
        } else {
            if (tabPane.isEnabled()) {
                tabRightBorder.paintBorder(tabPane, g, x, y, w + 1, h + 1);
            } else {
                tabRightBorderDisabled.paintBorder(tabPane, g, x, y, w + 1, h + 1);
            }
        }
    }
    
    public void update( Graphics g, JComponent c ) {
        if ( c.isOpaque() ) {
            g.setColor( tabAreaBackground );
            g.fillRect( 0, 0, c.getWidth(),c.getHeight() );
        }
        paint( g, c );
    }
    
    /**
     * Overridden to do nothing for the Quaqua L&F.
     */
    protected void paintTabBackground( Graphics g, int tabPlacement,
    int tabIndex, int x, int y, int w, int h, boolean isSelected ) {
    }
    
    /**
     * Overridden to do nothing for the Quaqua L&F.
     */
    protected int getTabLabelShiftX( int tabPlacement, int tabIndex, boolean isSelected ) {
        return 0;
    }
    
    
    /**
     * Overridden to return specific shift values for the Quaqua L&F.
     * FIXME We should find another way to align the labels properly.
     */
    protected int getTabLabelShiftY( int tabPlacement, int tabIndex, boolean isSelected ) {
        switch (tabPlacement) {
            case LEFT:
                return 0;
            case BOTTOM:
                return -1;
            case RIGHT:
                return 0;
            case TOP:
            default:
                return 1;
        }
    }
    
    
    public void paint( Graphics g, JComponent c ) {
        int tabPlacement = tabPane.getTabPlacement();
        
        Insets insets = c.getInsets(); Dimension size = c.getSize();
        
        // Paint the background for the tab area
        if ( tabPane.isOpaque() ) {
            g.setColor( c.getBackground() );
            switch ( tabPlacement ) {
                case LEFT:
                    g.fillRect( insets.left, insets.top,
                    calculateTabAreaWidth( tabPlacement, runCount, maxTabWidth ),
                    size.height - insets.bottom - insets.top );
                    break;
                case BOTTOM:
                    int totalTabHeight = calculateTabAreaHeight( tabPlacement, runCount, maxTabHeight );
                    g.fillRect( insets.left, size.height - insets.bottom - totalTabHeight,
                    size.width - insets.left - insets.right,
                    totalTabHeight );
                    break;
                case RIGHT:
                    int totalTabWidth = calculateTabAreaWidth( tabPlacement, runCount, maxTabWidth );
                    g.fillRect( size.width - insets.right - totalTabWidth,
                    insets.top, totalTabWidth,
                    size.height - insets.top - insets.bottom );
                    break;
                case TOP:
                default:
                    g.fillRect( insets.left, insets.top,
                    size.width - insets.right - insets.left,
                    calculateTabAreaHeight(tabPlacement, runCount, maxTabHeight) );
                    paintHighlightBelowTab();
            }
        }
        
        int tc = tabPane.getTabCount();
        
        if (tabCount != tc) {
            tabCount = tc;
            updateMnemonics();
        }
        
        int selectedIndex = tabPane.getSelectedIndex();
        //int tabPlacement = tabPane.getTabPlacement();
        
        ensureCurrentLayout();
        
        // Paint content border
        paintContentBorder(g, tabPlacement, selectedIndex);
        
        
        // Paint tab area
        // If scrollable tabs are enabled, the tab area will be
        // painted by the scrollable tab panel instead.
        //
        
        //if (!scrollableTabLayoutEnabled()) { // WRAP_TAB_LAYOUT
        paintTabArea(g, tabPlacement, selectedIndex);
        //}
        
        
    }
    
    /**
     * Paints the tabs in the tab area.
     * Invoked by paint().
     * The graphics parameter must be a valid <code>Graphics</code>
     * object.  Tab placement may be either:
     * <code>JTabbedPane.TOP</code>, <code>JTabbedPane.BOTTOM</code>,
     * <code>JTabbedPane.LEFT</code>, or <code>JTabbedPane.RIGHT</code>.
     * The selected index must be a valid tabbed pane tab index (0 to
     * tab count - 1, inclusive) or -1 if no tab is currently selected.
     * The handling of invalid parameters is unspecified.
     *
     * @param g the graphics object to use for rendering
     * @param tabPlacement the placement for the tabs within the JTabbedPane
     * @param selectedIndex the tab index of the selected component
     *
     * @since 1.4
     */
    protected void paintTabArea(Graphics g, int tabPlacement, int selectedIndex) {
        int tabCount = tabPane.getTabCount();
        
        Rectangle iconRect = new Rectangle(),
        textRect = new Rectangle();
        Rectangle clipRect = g.getClipBounds();
        
        // Paint tabRuns of tabs from back to front
        for (int i = runCount - 1; i >= 0; i--) {
            int start = tabRuns[i];
            int next = tabRuns[(i == runCount - 1)? 0 : i + 1];
            int end = (next != 0? next - 1: tabCount - 1);
            for (int j = start; j <= end; j++) {
                if (rects[j].intersects(clipRect)) {
                    paintTab(g, tabPlacement, rects, j, iconRect, textRect);
                }
            }
        }
        
        // Paint selected tab if its in the front run
        // since it may overlap other tabs
        if (selectedIndex >= 0 && getRunForTab(tabCount, selectedIndex) == 0) {
            if (rects[selectedIndex].intersects(clipRect)) {
                paintTab(g, tabPlacement, rects, selectedIndex, iconRect, textRect);
            }
        }
    }
    
    
    private void ensureCurrentLayout() {
        if (!tabPane.isValid()) {
            tabPane.validate();
        }
        /* If tabPane doesn't have a peer yet, the validate() call will
         * silently fail.  We handle that by forcing a layout if tabPane
         * is still invalid.  See bug 4237677.
         */
        if (!tabPane.isValid()) {
            TabbedPaneLayout layout = (TabbedPaneLayout)tabPane.getLayout();
            layout.calculateLayoutInfo();
        }
    }
    
    /**
     * Reloads the mnemonics. This should be invoked when a memonic changes,
     * when the title of a mnemonic changes, or when tabs are added/removed.
     */
    private void updateMnemonics() {
        /* XXX - This needs JDK 1.4 to work.
        resetMnemonics();
        for (int counter = tabPane.getTabCount() - 1; counter >= 0;
        counter--) {
            int mnemonic = tabPane.getMnemonicAt(counter);
         
            if (mnemonic > 0) {
                addMnemonic(counter, mnemonic);
            }
        }
         */
    }
    
    /**
     * Resets the mnemonics bindings to an empty state.
     */
    private void resetMnemonics() {
        if (mnemonicToIndexMap != null) {
            mnemonicToIndexMap.clear();
            mnemonicInputMap.clear();
        }
    }
    /**
     * Adds the specified mnemonic at the specified index.
     */
    private void addMnemonic(int index, int mnemonic) {
        if (mnemonicToIndexMap == null) {
            initMnemonics();
        }
        mnemonicInputMap.put(KeyStroke.getKeyStroke(mnemonic, Event.ALT_MASK),
        "setSelectedIndex");
        mnemonicToIndexMap.put(new Integer(mnemonic), new Integer(index));
    }
    
    /**
     * Installs the state needed for mnemonics.
     */
    private void initMnemonics() {
        mnemonicToIndexMap = new Hashtable();
        mnemonicInputMap = new InputMapUIResource();
        mnemonicInputMap.setParent(SwingUtilities.getUIInputMap(tabPane,
        JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT));
        SwingUtilities.replaceUIInputMap(tabPane,
        JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT,
        mnemonicInputMap);
    }
    
    
    protected void paintHighlightBelowTab( ) {
        
    }
    
    
    /**
     * Overridden to do nothing for the Quaqua L&F.
     */
    protected void paintFocusIndicator(Graphics g, int tabPlacement,
    Rectangle[] rects, int tabIndex,
    Rectangle iconRect, Rectangle textRect,
    boolean isSelected) {
    }
    
    protected boolean isFrameActive() {
        return true; //tabPane.getRootPane();
    }
    
    protected Insets getTabInsets(int tabPlacement, int tabIndex) {
        if (tabPlacement == RIGHT || tabPlacement == LEFT) {
            return new Insets(tabInsets.top, 6/*tabInsets.left / 2 - 1*/, tabInsets.bottom,5/* tabInsets.right / 2 + 1*/);
        } else {
            return tabInsets;
        }
    }
    
    protected Insets getContentBorderInsets(int tabPlacement) {
        // We eliminate the insets at the tab location
        // because the content border is drawn as a shadow,
        // which runs through below the tabs.
        
        Insets insets = (Insets) contentBorderInsets.clone();
        switch(tabPlacement) {
            case LEFT:
                insets.left = 7+4;
                break;
            case RIGHT:
                insets.right = 6+4;
                break;
            case BOTTOM:
                insets.bottom = 6+4;
                break;
            case TOP:
            default:
                insets.top = 7+4;
        }
        return insets;
    }
    
    protected void paintContentBorder(Graphics g, int tabPlacement, int selectedIndex) {
        int width = tabPane.getWidth();
        int height = tabPane.getHeight();
        Insets insets = tabPane.getInsets();
        
        int x = insets.left;
        int y = insets.top;
        int w = width - insets.right - insets.left;
        int h = height - insets.top - insets.bottom;
        
        switch(tabPlacement) {
            case LEFT:
                x += calculateTabAreaWidth(tabPlacement, runCount, maxTabWidth);
                w -= (x - insets.left);
                paintContentBorderLeftEdge(g, tabPlacement, selectedIndex, x, y, w, h);
                break;
            case RIGHT:
                w -= calculateTabAreaWidth(tabPlacement, runCount, maxTabWidth);
                paintContentBorderRightEdge(g, tabPlacement, selectedIndex, x, y, w, h);
                break;
            case BOTTOM:
                h -= calculateTabAreaHeight(tabPlacement, runCount, maxTabHeight);
                paintContentBorderBottomEdge(g, tabPlacement, selectedIndex, x, y, w, h);
                break;
            case TOP:
            default:
                y += calculateTabAreaHeight(tabPlacement, runCount, maxTabHeight);
                h -= (y - insets.top);
                paintContentBorderTopEdge(g, tabPlacement, selectedIndex, x, y, w, h);
        }
    }
    
    protected void paintContentBorderTopEdge( Graphics g, int tabPlacement,
    int selectedIndex,
    int x, int y, int w, int h ) {
        //contentBorder.paintBorder(tabPane, g, x - 1, y, w + 2, h);
        Insets contentBorderInsets = contentBorder.getBorderInsets(tabPane);
        contentBorder.paintBorder(tabPane, g, x - 1, y - contentBorderInsets.top - 1, w + 2, h + contentBorderInsets.top + 1);
        
        Border bar;
        if (tabPane.isEnabled()) {
            bar = barTopBorder;
        } else {
            bar = barTopBorderDisabled;
        }
        
        bar.paintBorder(
        tabPane, g,
        x + contentBorderInsets.left - 1,
        y,
        w - contentBorderInsets.left - contentBorderInsets.right + 2,
        7
        );
    }
    
    protected void paintContentBorderBottomEdge(Graphics g, int tabPlacement,
    int selectedIndex,
    int x, int y, int w, int h) {
        Insets contentBorderInsets = contentBorder.getBorderInsets(tabPane);
        contentBorder.paintBorder(tabPane, g, x - 1, y, w + 2, h + contentBorderInsets.bottom);
        
        Border bar;
        if (tabPane.isEnabled()) {
            bar = barBottomBorder;
        } else {
            bar = barBottomBorderDisabled;
        }
        
        bar.paintBorder(
        tabPane, g,
        x + contentBorderInsets.left - 1,
        y + h - 6,
        w - contentBorderInsets.left - contentBorderInsets.right + 2,
        6
        );
    }
    
    protected void paintContentBorderLeftEdge(Graphics g, int tabPlacement,
    int selectedIndex,
    int x, int y, int w, int h) {
        contentBorder.paintBorder(tabPane, g, x - 1, y, w + 2, h);
        // XXX to do
        Border bar;
        if (tabPane.isEnabled()) {
            bar = barLeftBorder;
        } else {
            bar = barLeftBorderDisabled;
        }
        
        bar.paintBorder(
        tabPane, g,
        x, // - contentBorderInsets.right, //contentBorderInsets.left - 1,
        y + 1,
        7,
        y + h - 6
        );
    }
    
    protected void paintContentBorderRightEdge(Graphics g, int tabPlacement,
    int selectedIndex,
    int x, int y, int w, int h) {
        contentBorder.paintBorder(tabPane, g, x - 1, y, w + 4, h);

        Border bar;
        if (tabPane.isEnabled()) {
            bar = barRightBorder;
        } else {
            bar = barRightBorderDisabled;
        }
        
        bar.paintBorder(
        tabPane, g,
        x + w - 6,// - contentBorderInsets.right, //contentBorderInsets.left - 1,
        y + 1,
        6,
        y + h - 6
        );
    }
    
    
    
    
    protected int calculateMaxTabHeight( int tabPlacement ) {
        FontMetrics metrics = getFontMetrics();
        int height = metrics.getHeight();
        boolean tallerIcons = false;
        
        for ( int i = 0; i < tabPane.getTabCount(); ++i ) {
            Icon icon = tabPane.getIconAt( i );
            if ( icon != null ) {
                if ( icon.getIconHeight() > height ) {
                    tallerIcons = true;
                    break;
                }
            }
        }
        return super.calculateMaxTabHeight(tabPlacement) -
        (tallerIcons ? (tabInsets.top + tabInsets.bottom) : 0);
    }
    
    
    protected int getTabRunOverlay(int tabPlacement) {
        if ( tabPlacement == LEFT || tabPlacement == RIGHT ) {
            return 2;
        } else {
            return 1;
        }
        /*
        // Tab runs laid out vertically should overlap
        // at least as much as the largest slant
        if ( tabPlacement == LEFT || tabPlacement == RIGHT ) {
            int maxTabHeight = calculateMaxTabHeight(tabPlacement);
            return maxTabHeight / 2;
        }
        return 0;
         */
    }
    
    // Don't rotate runs!
    protected boolean shouldRotateTabRuns( int tabPlacement, int selectedRun ) {
        return false;
    }
    
    // Pad all tab runs if there is more than one run.
    protected boolean shouldPadTabRun( int tabPlacement, int run ) {
        return runCount > 1;
    }
    
    private boolean isLastInRun( int tabIndex ) {
        int run = getRunForTab( tabPane.getTabCount(), tabIndex );
        int lastIndex = lastTabInRun( tabPane.getTabCount(), run );
        return tabIndex == lastIndex;
    }
    
    
    protected void paintText(Graphics g, int tabPlacement,
    Font font, FontMetrics metrics, int tabIndex,
    String title, Rectangle textRect,
    boolean isSelected) {
        
        g.setFont(font);
        
        View v = null;
        /* XXX - This needs JDK 1.4 to work.
        View v = getTextViewForTab(tabIndex);
         */
        if (v != null) {
            // html
            v.paint(g, textRect);
        } else {
            // plain text
            int mnemIndex = -1;
            /* XXX - This needs JDK 1.4 to work.
            int mnemIndex = tabPane.getDisplayedMnemonicIndexAt(tabIndex);
             */
            if (tabPane.isEnabled() && tabPane.isEnabledAt(tabIndex)) {
                g.setColor(tabPane.getForegroundAt(tabIndex));
                QuaquaGraphicsUtils.drawStringUnderlineCharAt(g,
                title, mnemIndex,
                textRect.x, textRect.y + metrics.getAscent());
                
            } else { // tab disabled
                g.setColor(disabledForeground);
                QuaquaGraphicsUtils.drawStringUnderlineCharAt(g,
                title, mnemIndex,
                textRect.x, textRect.y + metrics.getAscent());
            }
        }
    }
    
    /**
     * This inner class is marked &quot;public&quot; due to a compiler bug.
     * This class should be treated as a &quot;protected&quot; inner class.
     * Instantiate it only within subclasses of MetalTabbedPaneUI.
     */
    public class TabbedPaneLayout extends BasicTabbedPaneUI.TabbedPaneLayout {
        
        protected void normalizeTabRuns( int tabPlacement, int tabCount,
        int start, int max ) {
            // Only normalize the runs for top & bottom;  normalizing
            // doesn't look right for Metal's vertical tabs
            // because the last run isn't padded and it looks odd to have
            // fat tabs in the first vertical runs, but slimmer ones in the
            // last (this effect isn't noticeable for horizontal tabs).
            if ( tabPlacement == TOP || tabPlacement == BOTTOM ) {
                super.normalizeTabRuns( tabPlacement, tabCount, start, max );
            }
        }
        
        // Don't rotate runs!
        protected void rotateTabRuns( int tabPlacement, int selectedRun ) {
        }
        
        // Don't pad selected tab
        protected void padSelectedTab( int tabPlacement, int selectedIndex ) {
        }
        
        
        public void calculateLayoutInfo() {
            int tabCount = tabPane.getTabCount();
            assureRectsCreated(tabCount);
            calculateTabRects(tabPane.getTabPlacement(), tabCount);
        }
        
        protected void padTabRun(int tabPlacement, int start, int end, int max) {
            // Only pad tab runs if they are on top or bottom
            if (tabPlacement == TOP || tabPlacement == BOTTOM) {
                super.padTabRun(tabPlacement, start, end, max);
            }
        }
        
        protected void calculateTabRects(int tabPlacement, int tabCount) {
            Dimension size = tabPane.getSize();
            Insets insets = tabPane.getInsets();
            Insets tabAreaInsets = getTabAreaInsets(tabPlacement);
            
            super.calculateTabRects(tabPlacement, tabCount);
            
            //
            // Center tabs vertically or horizontally
            // If centered horizontally ensure that all tab runs have
            // the same width.
            switch(tabPlacement) {
                case LEFT:
                case RIGHT: {
                    int availableTabAreaHeight = size.height - insets.top - insets.bottom - tabAreaInsets.top - tabAreaInsets.bottom;
                    int usedTabAreaHeight = 0;
                    int pad = 0;
                    for (int run = 0; run < runCount; run++) {
                        int firstIndex = tabRuns[run];
                        int lastIndex = lastTabInRun(tabCount, run);
                        if (run == 0) {
                            usedTabAreaHeight = 0;
                            for (int i=firstIndex; i <= lastIndex; i++) {
                                usedTabAreaHeight += rects[i].height;
                            }
                            pad = (availableTabAreaHeight - usedTabAreaHeight) / 2;
                        }
                        for (int i=firstIndex; i <= lastIndex; i++) {
                            rects[i].y += pad;
                        }
                    }
                    break;
                }
                case BOTTOM:
                case TOP:
                default: {
                    int availableTabAreaWidth = size.width
                    - insets.left - insets.right
                    - tabAreaInsets.left - tabAreaInsets.right;
                    for (int run = 0; run < runCount; run++) {
                        int firstIndex = tabRuns[run];
                        int lastIndex = lastTabInRun(tabCount, run);
                        int usedTabAreaWidth = 0;
                        for (int i=firstIndex; i <= lastIndex; i++) {
                            usedTabAreaWidth += rects[i].width;
                        }
                        int pad = (availableTabAreaWidth - usedTabAreaWidth) / 2;
                        for (int i=firstIndex; i <= lastIndex; i++) {
                            rects[i].x += pad;
                        }
                    }
                    
                    break;
                }
            }
        }
    }
}
