/*
 * @(#)QuaquaLookAndFeel.java  1.0  2003-09-28
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

/**
 * The QuaquaLookAndFeel provides quick (and dirty) bug fixes all
 * look and feels which are part of Apple's implementation of the
 * Java Runtime Environment on Mac OS X.
 * <p>
 * The Quaqua Look and Feel can not be used on other platforms than Mac OS X.
 * <p>
 * This class acts as a proxy to the look and feel provided by
 * <code>QuaquaManager.getLookAndFeel</code>.
 * <p>
 * This class may be less compatible than the look and feel provided by 
 * QuaquaManager, but it can be used in a chooser for look and feel's.
 * 
 * <h4>Usage</h4>
 * <pre>
 * import javax.swing.*;
 * 
 * UIManager.setLookAndFeel("ch.randelshofer.quaqua.QuaquaLookAndFeel");
 * <pre>
 *
 * @author Werner Randelshofer, Staldenmattweg 2, CH-6405 Immensee, Switzerland
 * @version 1.0 2003-07-20 Created.
 */
public class QuaquaLookAndFeel extends LookAndFeelProxy {
    
    /** Creates a new instance of QuaquaLookAndFeel */
    public QuaquaLookAndFeel() {
        super(QuaquaManager.getLookAndFeel());
    }
    
}
