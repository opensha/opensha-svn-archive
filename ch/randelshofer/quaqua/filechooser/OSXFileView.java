/*
 * @(#)OSXFileView.java  1.0  September 7, 2003
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

package ch.randelshofer.quaqua.filechooser;

import javax.swing.plaf.*;
import javax.swing.*;
import javax.swing.filechooser.*;
import java.io.*;
import java.lang.reflect.*;
import java.util.*;

/**
 * The OSXFileView is a proxy for the AquaFileView.
 *
 * @author Werner Randelshofer
 * @version 1.0 September 7, 2003  Created.
 */
public class OSXFileView extends FileView {
    private FileView target;
    private JFileChooser fileChooser;
    private static OSXFileView instance;
    
    public static OSXFileView getInstance() {
        if (instance == null) {
            instance = new OSXFileView();
        }
        return instance;
    }
    
    /** Creates a new instance. */
    public OSXFileView() {
        this(null);
    }
    /** Creates a new instance. */
    public OSXFileView(JFileChooser fileChooser) {
        this.fileChooser = fileChooser;
        this.target = createTarget();
    }
    
    /**
     * Creates the target.
     */
    private FileView createTarget() {
        try {
            JFileChooser aquaFileChooser = new JFileChooser() {
                public void updateUI() {
                    try {
                        FileChooserUI ui = (FileChooserUI)
                        Class.forName("apple.laf.AquaFileChooserUI")
                        .getMethod("createUI", new Class[] {javax.swing.JComponent.class})
                        .invoke(null, new Object[] {this});
                        
                        setUI(ui);
                    } catch (Exception e) {
                        try {
                            FileChooserUI ui = (FileChooserUI)
                            Class.forName("com.apple.mrj.swing.MacFileChooserUI")
                            .getMethod("createUI", new Class[] {javax.swing.JComponent.class})
                            .invoke(null, new Object[] {this});
                            
                            setUI(ui);
                        } catch (Exception e2) {
                            e2.printStackTrace();
                            throw new InternalError(e2.getMessage());
                        }
                    }
                }
            };
            return aquaFileChooser.getUI().getFileView(aquaFileChooser);
        } catch (InternalError e) {
            return new DefaultFileView(fileChooser);
        }
    }
    
    /**
     * The name of the file. Normally this would be simply f.getName()
     */
    public String getName(File f) {
        return target.getName(f);
    };
    
    /**
     * A human readable description of the file. For example,
     * a file named jag.jpg might have a description that read:
     * "A JPEG image file of James Gosling's face"
     */
    public String getDescription(File f) {
        return target.getDescription(f);
    }
    
    /**
     * A human readable description of the type of the file. For
     * example, a jpg file might have a type description of:
     * "A JPEG Compressed Image File"
     */
    public String getTypeDescription(File f) {
        return target.getTypeDescription(f);
    }
    
    /**
     * The icon that represents this file in the JFileChooser.
     */
    public Icon getIcon(File f) {
        return target.getIcon(f);
    }
    
    /**
     * Whether the directory is traversable or not. This might be
     * useful, for example, if you want a directory to represent
     * a compound document and don't want the user to descend into it.
     */
    public Boolean isTraversable(File f) {
        return target.isTraversable(f);
    }
    
    public void clearIconCache() {
        try {
            target.getClass()
            .getMethod("clearIconCache", new Class[0])
            .invoke(target, new Object[0]);
        } catch (Exception e) {
            // If we can't clear the icon cache, we create a new target
            // This may be a very expensive operation. :(
            target = createTarget();
        }
    }
    
}
