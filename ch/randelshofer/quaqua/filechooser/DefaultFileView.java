/*
 * @(#)DefaultFileView.java  1.0.1  2003-10-08
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

import java.io.*;
import javax.swing.*;
import javax.swing.filechooser.*;
import java.util.*;

/**
 * The DefaultFileView provides a LAF indepentent file view.
 * The icons provided by this file view might be looking boring, because we
 * are unable to deliver file specific icons. The only thing we can do, is
 * to make the difference between directories, files, computers, hard drives
 * and floppies.
 * <p>
 * This FileView is used by class OSXFileView, when it is unable to create an
 * AquaFileView instance.
 *
 * @see OSXFileView
 *
 * @author Werner Randelshofer
 * @version 1.0.1 2003-10-08 Diagnostic output to System.out removed.
 * <br>1.0 September 7, 2003  Created.
 */
public class DefaultFileView extends FileView {
    /* FileView type descriptions */
    protected Hashtable iconCache = new Hashtable();
    /* Descriptions. */
    private String fileDescriptionText;
    private String directoryDescriptionText;
    /* Icons. */
    protected Icon directoryIcon = null;
    protected Icon fileIcon = null;
    protected Icon computerIcon = null;
    protected Icon hardDriveIcon = null;
    protected Icon floppyDriveIcon = null;
    
    private JFileChooser fileChooser;
    
    public DefaultFileView() {
        this(null);
    }
    public DefaultFileView(JFileChooser fileChooser) {
        this.fileChooser = fileChooser;
        installStrings();
        installIcons();
    }
    
    protected void installStrings() {
        Locale l = Locale.getDefault();
        fileDescriptionText = UIManager.getString("FileChooser.fileDescriptionText"/*,l*/);
        directoryDescriptionText = UIManager.getString("FileChooser.directoryDescriptionText"/*,l*/);
    }
    
    protected void installIcons() {
        directoryIcon    = UIManager.getIcon("FileView.directoryIcon");
        fileIcon         = UIManager.getIcon("FileView.fileIcon");
        computerIcon     = UIManager.getIcon("FileView.computerIcon");
        hardDriveIcon    = UIManager.getIcon("FileView.hardDriveIcon");
        floppyDriveIcon  = UIManager.getIcon("FileView.floppyDriveIcon");
    }
    
    public void clearIconCache() {
        iconCache = new Hashtable();
    }
    
    protected FileSystemView getFileSystemView() {
        return
        (fileChooser == null)
        ? FileSystemView.getFileSystemView()
        : fileChooser.getFileSystemView();
    }
    
    public String getName(File f) {
        return f.getName();
        /* FIXME - This needs JDK 1.4 to work:
        // Note: Returns display name rather than file name
        String fileName = null;
        if(f != null) {
            fileName = getFileSystemView().getSystemDisplayName(f);
        }
        return fileName;
         */
    }
    
    
    public String getDescription(File f) {
        return f.getName();
    }
    
    public String getTypeDescription(File f) {
        return (f.isDirectory()) ? directoryDescriptionText : fileDescriptionText;
        /* FIXME - This needs JDK 1.4 to work:
        String type = getFileSystemView().getSystemTypeDescription(f);
        if (type == null) {
            if (f.isDirectory()) {
                type = directoryDescriptionText;
            } else {
                type = fileDescriptionText;
            }
        }
        return type;
         */
    }
    
    public Icon getCachedIcon(File f) {
        return (Icon) iconCache.get(f);
    }
    
    public void cacheIcon(File f, Icon i) {
        if(f == null || i == null) {
            return;
        }
        iconCache.put(f, i);
    }
    
    public Icon getIcon(File f) {
        Icon icon = getCachedIcon(f);
        if(icon != null) {
            return icon;
        }
        icon = (f.isDirectory()) ? directoryIcon : fileIcon;
        cacheIcon(f, icon);
        return icon;
        
        /* FIXME - This needs JDK 1.4 to work:
        Icon icon = getCachedIcon(f);
        if(icon != null) {
            return icon;
        }
        icon = fileIcon;
        if (f != null) {
            FileSystemView fsv = getFileSystemView();
            
            if (fsv.isFloppyDrive(f)) {
                icon = floppyDriveIcon;
            } else if (fsv.isDrive(f)) {
                icon = hardDriveIcon;
            } else if (fsv.isComputerNode(f)) {
                icon = computerIcon;
            } else if (f.isDirectory()) {
                icon = directoryIcon;
            }
        }
        cacheIcon(f, icon);
        return icon;
         */
    }
    
    public Boolean isHidden(File f) {
        String name = f.getName();
        if(name != null && name.charAt(0) == '.') {
            return Boolean.TRUE;
        } else {
            return Boolean.FALSE;
        }
    }
}

