/*
 * @(#)QuaquaManager.java 1.2.1  2003-10-29
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

import java.awt.*;
import javax.swing.*;
/**
 * The QuaquaManager provides Swing Look and Feel classes with bug fixes
 * for the Mac Look and Feel and the Aqua Look and Feel on Apples Macintosh
 * Runtime for Java (MRJ) 1.3.1, 1.4.1 and 1.4.1 Update 1.
 * <p>
 * Usage:
 * <pre>
 * UIManager.setLookAndFeel(QuaquaManager.getLookAndFeelClassName());
 * </pre>
 *
 * @author  Werner Randelshofer
 * @version 1.2.1 2003-10-29 Support for Macc OS X 10.3 added.
 * <br>1.2 2003-09-28 Method getLookAndFeel added.
 * <br>1.1 2003-09-11 Workarounds for Java 1.4.1 Update 1 added.
 * <br>1.0 2003-07-20 Created.
 */
public class QuaquaManager {
    /** Prevent instance creation. */
    private QuaquaManager() {
    }
    
    
    /**
     * Returns the class name of a Quaqua look and feel if workarounds for the
     * system look and feel are available.
     * Returns UIManager.getSystemLookAndFeelClassName() if no workaround is
     * available.
     */
    public static String getLookAndFeelClassName() {
        String className;
        className = UIManager.getSystemLookAndFeelClassName();
        if (className.equals("com.apple.mrj.swing.MacLookAndFeel")) {
            className = "ch.randelshofer.quaqua.Quaqua131LookAndFeel";
        } else if (className.equals("apple.laf.AquaLookAndFeel")) {
            String vmversion = System.getProperty("java.vm.version");
            //System.out.println("java.vm.version:"+vmversion);
            if (vmversion.compareTo("1.4.1_01-21") <= 0) {
                className = "ch.randelshofer.quaqua.Quaqua141LookAndFeel";
            } else if (vmversion.compareTo("1.4.1_01-24") <= 0) {
                className = "ch.randelshofer.quaqua.Quaqua141U1LookAndFeel";
            } else {
                className = "ch.randelshofer.quaqua.Quaqua141UPLookAndFeel";
            }
        }
        return className;
    }
    
    /**
     * Returns a Quaqua look and feel, if workarounds for the
     * system look and feel are available.
     * Returns a UIManager.getSystemLookAndFeelClassName() instance if no
     * workaround is available.
     */
    public static LookAndFeel getLookAndFeel() {
        try {
        return (LookAndFeel) Class.forName(getLookAndFeelClassName()).newInstance();
        } catch (Exception e) {
            InternalError ie = new InternalError(e.toString());
            /* FIXME - This needs JDK 1.4 to work.
            ie.initCause(e);
             */
            throw ie;
        }
    }
}
