/*
 * @(#)OSXFileSystemView.java 1.1.1  2003-10-04
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

package ch.randelshofer.quaqua.filechooser;

import javax.swing.filechooser.*;
import java.io.*;
import java.util.*;
/**
 * A file system view for Mac OS X.
 * @author  Werner Randelshofer
 * @version  1.1.1 2003-10-04 Network folder to roots added.
 * <br>1.1 2003-03-16 Performance of method isRoot(File) improved.
 * <br>1.0 2002-04-05 Created.
 */
public class OSXFileSystemView extends FileSystemView {
    private FileSystemView sysView;
    private static OSXFileSystemView singleton;
    private File volumesFolder = new File("/Volumes");
    private File networkFolder = new File("/Network");
    
    /** Creates a new instance of OSXFileSystemView */
    public OSXFileSystemView() {
        sysView = FileSystemView.getFileSystemView();
    }
    /**
     * Returns the singleton file system view.
     */
    public static FileSystemView getFileSystemView() {
        if (singleton == null) {
            singleton = new OSXFileSystemView();
        }
        return singleton;
    }
    
    /**
     * Returns a File object constructed from the given path string.
     */
    public File createFileObject(String path) {
        return sysView.createFileObject(path);
    }
    
    /**
     * Returns a File object constructed in dir from the given filename.
     */
    public File createFileObject(File dir, String filename) {
        return sysView.createFileObject(dir, filename);
    }
    
    /**
     * creates a new folder with a default folder name.
     */
    public File createNewFolder(File containingDir) throws IOException {
        return sysView.createNewFolder(containingDir);
    }
    
    /**
     * gets the list of shown (i.e. not hidden) files
     */
    public File[] getFiles(File dir, boolean useFileHiding) {
        return sysView.getFiles(dir, useFileHiding);
    }
    
    public File getHomeDirectory() {
        return sysView.getHomeDirectory();
    }
    
    /**
     * Returns the parent directory of dir.
     */
    public File getParentDirectory(File dir) {
        //System.out.println("OSXFileSytemView parent of "+dir+" is\n  "+((isRoot(dir)) ? null : sysView.getParentDirectory(dir))+"\n  isRoot:"+isRoot(dir)+"\n  parentFile:"+dir.getParentFile());
        return (isRoot(dir)) ? null : sysView.getParentDirectory(dir);
        //return (isRoot(dir)) ? null : dir.getParentFile();
    }
    
    /**
     * Returns all root partitians on this system. For example, on Windows,
     * this would be the A: through Z: drives.
     */
    public File[] getRoots() {
        ArrayList roots = new ArrayList();
        roots.addAll(Arrays.asList(sysView.getRoots()));
        roots.addAll(Arrays.asList(volumesFolder.listFiles()));
        roots.add(networkFolder);
        return (File[]) roots.toArray(new File[roots.size()]);
    }
    
    /**
     * Returns whether a file is hidden or not.
     */
    public boolean isHiddenFile(File f) {
        return sysView.isHiddenFile(f);
    }
    
    /**
     * Determines if the given file is a root partition or drive.
     */
    public boolean isRoot(File f) {
        return f.getPath().equals("/") 
        || f.getParentFile().equals(volumesFolder)
        || f.equals(networkFolder);
    }
}
