/*
 * @(#)FileSystemTreeModel.java  1.0  September 5, 2003
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

import java.util.*;
import java.awt.*;
import java.io.*;
import java.text.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import javax.swing.filechooser.*;

/**
 * The FileSystemTreeModel provides the data model for the JBrowser in a 
 * QuaquaFileChooserUI.
 *
 * @author Werner Randelshofer
 * @version 1.0 September 5, 2003  Created.
 */
public class FileSystemTreeModel implements javax.swing.tree.TreeModel {
    /** Listeners. */
    protected EventListenerList listenerList = new EventListenerList();
    private JFileChooser fileChooser;
    private FileSystemTreeModel.RootNode root;
    
    /** Creates a new instance. */
    public FileSystemTreeModel(JFileChooser fileChooser) {
        this.fileChooser = fileChooser;
        root = new FileSystemTreeModel.RootNode();
    }
    
    public Object getChild(Object parent, int index) {
        return ((FileSystemTreeModel.Node) parent).getChildAt(index);
    }
    
    public int getChildCount(Object parent) {
        return ((FileSystemTreeModel.Node) parent).getChildCount();
    }
    
    public int getIndexOfChild(Object parent, Object child) {
        return ((FileSystemTreeModel.Node) parent).getIndex((FileSystemTreeModel.Node) child);
    }
    
    private int getIndexOfChildForFile(FileSystemTreeModel.Node parent, File file) {
        for (int i=0; i < parent.getChildCount(); i++) {
            if (((FileSystemTreeModel.Node) parent.getChildAt(i)).getFile().equals(file)) {
                return i;
            }
        }
        return -1;
    }
    
    private Comparator createFileComparator() {
        FileView fileView = fileChooser.getFileView();
        Locale locale;
        try {
        locale = fileChooser.getLocale();
        } catch (IllegalComponentStateException e) {
            locale = Locale.getDefault();
        }
        return new FileComparator(Collator.getInstance(locale), fileChooser);
    }
    
    private int getInsertionIndexForFile(FileSystemTreeModel.Node parent, File file) {
        Comparator comparator = createFileComparator();
        int i;
        for (i=0; i < parent.getChildCount(); i++) {
            if (
            comparator.compare(((FileSystemTreeModel.Node) parent.getChildAt(i)).getFile(), file)
            >= 0) {
                return i;
            }
        }
        return i;
    }
    
    /**
     * Invoked this to insert newChild at location index in parents children.
     * This will then message nodesWereInserted to create the appropriate
     * event. This is the preferred way to add children as it will create
     * the appropriate event.
     */
    private void insertNodeInto(FileSystemTreeModel.Node newChild,
    FileSystemTreeModel.Node parent, int index){
        parent.insert(newChild, index);
        
        int[]           newIndexs = new int[1];
        
        newIndexs[0] = index;
        fireTreeNodesInserted(this, parent.getPath(), newIndexs, new Object[] {newChild});
    }
    
    public Object getRoot() {
        return root;
    }
    
    private FileSystemView getFileSystemView() {
        return fileChooser.getFileSystemView();
    }
    
    public TreePath toPath(File file) {
        LinkedList list = new LinkedList();
        FileSystemView fsv = getFileSystemView();
        File dir = file;
        do {
            list.addFirst(dir);
            if (fsv.isRoot(dir)) break;
            dir = dir.getParentFile();
        } while (dir != null);
        FileSystemTreeModel.Node[] pathComponents = new FileSystemTreeModel.Node[list.size() +  1];
        pathComponents[0] = (FileSystemTreeModel.Node) getRoot();
        LinkedList ll = new LinkedList();
        for (int i=1; i < pathComponents.length; i++) {
            File f = (File) list.get(i - 1);
            int index = getIndexOfChildForFile(pathComponents[i - 1], f);
            if (index == -1) {
                pathComponents[i] = new FileSystemTreeModel.Node(f);
                ll.add("I:"+f.getName());
                insertNodeInto(
                pathComponents[i],
                pathComponents[i - 1],
                getInsertionIndexForFile(pathComponents[i - 1], f)
                );
            } else {
                ll.add("X:"+f.getName());
                pathComponents[i] = (FileSystemTreeModel.Node) getChild(pathComponents[i - 1], index);
            }
        }
        return new TreePath((Object[]) pathComponents);
    }
    
    public boolean isLeaf(Object node) {
        return ((FileSystemTreeModel.Node) node).isLeaf();
    }
    
    /**
     * Messaged when the user has altered the value for the item identified
     * by <code>path</code> to <code>newValue</code>.
     * If <code>newValue</code> signifies a truly new value
     * the model should post a <code>treeNodesChanged</code> event.
     *
     * @param path path to the node that the user has altered
     * @param newValue the new value from the TreeCellEditor
     */
    public void valueForPathChanged(javax.swing.tree.TreePath path, Object newValue) {
        // XXX this should be used to rename/move a file.
    }
    
    public void invalidateCache() {
        root.invalidateCacheSubtree();
    }
    public void updatePath(TreePath path) {
        for (int i=0; i < path.getPathCount(); i++) {
            ((FileSystemTreeModel.Node) path.getPathComponent(i)).updateCache();
        }
    }
    
    //
    //  Events
    //
    
    /**
     * Adds a listener for the TreeModelEvent posted after the tree changes.
     *
     * @see     #removeTreeModelListener
     * @param   l       the listener to add
     */
    public void addTreeModelListener(TreeModelListener l) {
        listenerList.add(TreeModelListener.class, l);
    }
    
    /**
     * Removes a listener previously added with <B>addTreeModelListener()</B>.
     *
     * @see     #addTreeModelListener
     * @param   l       the listener to remove
     */
    public void removeTreeModelListener(TreeModelListener l) {
        listenerList.remove(TreeModelListener.class, l);
    }
    
    /**
     * Notifies all listeners that have registered interest for
     * notification on this event type.  The event instance
     * is lazily created using the parameters passed into
     * the fire method.
     *
     * @param source the node being changed
     * @param path the path to the root node
     * @param childIndicies the indices of the changed elements
     * @param children the changed elements
     * @see EventListenerList
     */
    protected void fireTreeNodesChanged(Object source, Object[] path,
    int[] childIndices,
    Object[] children) {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        TreeModelEvent e = null;
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i]==TreeModelListener.class) {
                // Lazily create the event:
                if (e == null)
                    e = new TreeModelEvent(source, path,
                    childIndices, children);
                ((TreeModelListener)listeners[i+1]).treeNodesChanged(e);
            }
        }
    }
    
    /**
     * Notifies all listeners that have registered interest for
     * notification on this event type.  The event instance
     * is lazily created using the parameters passed into
     * the fire method.
     *
     * @param source the node where new elements are being inserted
     * @param path the path to the root node
     * @param childIndicies the indices of the new elements
     * @param children the new elements
     * @see EventListenerList
     */
    protected void fireTreeNodesInserted(Object source, Object[] path,
    int[] childIndices,
    Object[] children) {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        TreeModelEvent e = null;
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i]==TreeModelListener.class) {
                // Lazily create the event:
                if (e == null)
                    e = new TreeModelEvent(source, path,
                    childIndices, children);
                ((TreeModelListener)listeners[i+1]).treeNodesInserted(e);
            }
        }
    }
    
    /**
     * Notifies all listeners that have registered interest for
     * notification on this event type.  The event instance
     * is lazily created using the parameters passed into
     * the fire method.
     *
     * @param source the node where elements are being removed
     * @param path the path to the root node
     * @param childIndicies the indices of the removed elements
     * @param children the removed elements
     * @see EventListenerList
     */
    protected void fireTreeNodesRemoved(Object source, Object[] path,
    int[] childIndices,
    Object[] children) {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        TreeModelEvent e = null;
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i]==TreeModelListener.class) {
                // Lazily create the event:
                if (e == null)
                    e = new TreeModelEvent(source, path,
                    childIndices, children);
                ((TreeModelListener)listeners[i+1]).treeNodesRemoved(e);
            }
        }
    }
    
    /**
     * Notifies all listeners that have registered interest for
     * notification on this event type.  The event instance
     * is lazily created using the parameters passed into
     * the fire method.
     *
     * @param source the node where the tree model has changed
     * @param path the path to the root node
     * @see EventListenerList
     */
    protected void fireTreeStructureChanged(Object source, Object[] path) {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        TreeModelEvent e = null;
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i]==TreeModelListener.class) {
                // Lazily create the event:
                if (e == null)
                    e = new TreeModelEvent(source, path);
                ((TreeModelListener)listeners[i+1]).treeStructureChanged(e);
            }
        }
    }
    
    /*
     * Notifies all listeners that have registered interest for
     * notification on this event type.  The event instance
     * is lazily created using the parameters passed into
     * the fire method.
     *
     * @param source the node where the tree model has changed
     * @param path the path to the root node
     * @see EventListenerList
     */
    private void fireTreeStructureChanged(Object source, TreePath path) {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        TreeModelEvent e = null;
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i]==TreeModelListener.class) {
                // Lazily create the event:
                if (e == null)
                    e = new TreeModelEvent(source, path);
                ((TreeModelListener)listeners[i+1]).treeStructureChanged(e);
            }
        }
    }
    
    
    
    public class Node extends DefaultMutableTreeNode implements Runnable {
        /**
         * The time when the cache will need to be refreshed again.
         */
        private volatile long cacheInvalidationTime = 0;
        private volatile boolean isUpdatingCache;
        
        public Node(File file) {
            super(file);
            if (file != null
            && file.exists()
            && ! fileChooser.isTraversable(file)) {
                cacheInvalidationTime = Long.MAX_VALUE;
            }
        }
        
        public boolean isUpdatingCache() {
            return isUpdatingCache;
        }
        
        public synchronized void invalidateCacheSubtree() {
            if (cacheInvalidationTime != Long.MAX_VALUE) {
                cacheInvalidationTime = 0;
            }
            Enumeration enum = super.children();
            while (enum.hasMoreElements()) {
                ((Node) enum.nextElement()).invalidateCacheSubtree();
            }
        }
        public synchronized void updateCacheSubtree() {
            Enumeration enum = super.children();
            while (enum.hasMoreElements()) {
                ((Node) enum.nextElement()).updateCacheSubtree();
            }
            if (cacheInvalidationTime != Long.MAX_VALUE) {
                updateCache();
            }
        }
        
        public File getFile() {
            return (File) getUserObject();
        }
        
        public Enumeration children() {
            updateCache();
            return super.children();
        }
        
        public boolean getAllowsChildren() {
            return fileChooser.isTraversable(getFile());
        }
        
        public TreeNode getChildAt(int childIndex) {
            updateCache();
            return super.getChildAt(childIndex);
        }
        
        public int getChildCount() {
            updateCache();
            return super.getChildCount();
        }
        
        public int getIndex(TreeNode node) {
            updateCache();
            return super.getIndex(node);
        }
        
        public boolean isLeaf() {
            return ! fileChooser.isTraversable(getFile());
        }
        
        protected synchronized void updateCache() {
            if (cacheInvalidationTime < System.currentTimeMillis() && ! isUpdatingCache) {
                cacheInvalidationTime = Long.MAX_VALUE;
                Thread t = new Thread(this);
                isUpdatingCache = true;
                t.start();
            }
        }
        
        protected File[] getFiles() {
            LinkedList list = new LinkedList();
            File[] files = getFileSystemView().getFiles(getFile(), fileChooser.isFileHidingEnabled());
            for (int i=0;i < files.length; i++) {
                if (fileChooser.accept(files[i])
                &&
                (fileChooser.getFileSelectionMode() != JFileChooser.DIRECTORIES_ONLY
                || files[i].isDirectory()
                )
                ) {
                    list.add(files[i]);
                }
            }
            return (File[]) list.toArray(new File[list.size()]);
        }
        
        public void run() {
            try {
                final File[] freshFiles = getFiles();
                final Comparator comparator = createFileComparator();
                Arrays.sort(freshFiles, comparator);
                SwingUtilities.invokeAndWait(new Runnable() {
                    public void run() {
                        
                        // Merge the fresh files we have read with our old
                        // children.
                        /*
                        int i = 0;
                        int j = 0;
                        int[] a, b, c;
                         
                        a[a.length] = Integer.MAX_VALUE;
                        b[b.length] = Integer.MAX_VALUE;
                        for (int k=0; k < a.length + b.length; k++) {
                            if (a[i] < b[j]) {
                                c[k] = a[i++];
                            } else {
                                c[k] = b[j++];
                            }
                        }*/
                        
                        LinkedList newChildren = new LinkedList();
                        int[] newChildIndices = new int[freshFiles.length];
                        LinkedList deletedChildren = new LinkedList();
                        int[] deletedChildIndices = new int[getChildCount()];
                        
                        int i, j, k, l, comparison;
                        Vector oldChildren = (children == null) ? new Vector() : (Vector) children.clone();
                        int count = freshFiles.length + oldChildren.size();
                        i = 0; j = 0; l = 0;
                        for (k = 0; k < count; k++) {
                            if (i >= freshFiles.length) comparison = (j >= oldChildren.size()) ? 0 : 1;
                            else if (j >= oldChildren.size()) comparison = -1;
                            else comparison = comparator.compare(freshFiles[i], ((FileSystemTreeModel.Node) oldChildren.get(j)).getFile());
                            
                            if (comparison < 0) {
                                FileSystemTreeModel.Node newChild = new FileSystemTreeModel.Node(freshFiles[i]);
                                newChildIndices[newChildren.size()] = l;
                                newChildren.add(newChild);
                                insert(newChild, l);
                                i++; l++;
                            } else if (comparison == 0) {
                                j++; i++; l++;
                            } else {
                                deletedChildIndices[deletedChildren.size()] = l + deletedChildren.size();
                                deletedChildren.add(oldChildren.get(j));
                                remove(l);
                                j++;
                            }
                        }
                        
                        
                        if (newChildren.size() > 0 && deletedChildren.size() == 0) {
                            newChildIndices = ArrayUtil.truncate(newChildIndices, 0, newChildren.size());
                            fireTreeNodesInserted(FileSystemTreeModel.Node.this, getPath(), newChildIndices, newChildren.toArray());
                        } else if (newChildren.size() == 0 && deletedChildren.size() > 0) {
                            deletedChildIndices = ArrayUtil.truncate(deletedChildIndices, 0, deletedChildren.size());
                            fireTreeNodesRemoved(FileSystemTreeModel.Node.this, getPath(), deletedChildIndices, deletedChildren.toArray());
                        } else if (newChildren.size() > 0 && deletedChildren.size() > 0) {
                            fireTreeStructureChanged(FileSystemTreeModel.Node.this, getPath());
                        }
                        DefaultMutableTreeNode parent = ((DefaultMutableTreeNode) getParent());
                        if (parent != null) {
                            fireTreeNodesChanged(FileSystemTreeModel.Node.this, parent.getPath(), new int[] { parent.getIndex(FileSystemTreeModel.Node.this) }, new Object[] {FileSystemTreeModel.Node.this});
                        }
                    }
                });
            } catch (InterruptedException e) {
                // Nothing to do
            } catch (java.lang.reflect.InvocationTargetException e) {
                e.printStackTrace();
            } catch (Throwable e) {
                e.printStackTrace();
            }
            synchronized (this) {
                isUpdatingCache = false;
                if (cacheInvalidationTime == Long.MAX_VALUE) {
                    cacheInvalidationTime = System.currentTimeMillis() + 2000;
                }
            }
        }
        
        public String toString() {
            return fileChooser.getName(getFile());
        }
    }
    
    private class RootNode extends Node {
        public RootNode() {
            super(null);
        }
        public boolean getAllowsChildren() {
            return true;
        }
        
        public boolean isLeaf() {
            return false;
        }
        
        public String toString() {
            return "Root";
        }
        
        protected File[] getFiles() {
            LinkedList list = new LinkedList();
            File[] files = getFileSystemView().getRoots();
            for (int i=0;i < files.length; i++) {
                if (fileChooser.accept(files[i])) {
                    list.add(files[i]);
                }
            }
            return (File[]) list.toArray(new File[list.size()]);
        }
    }
    
    private static class FileComparator implements Comparator {
        private Collator collator;
        private JFileChooser fileChooser;
        public FileComparator(Collator collator, JFileChooser fileChooser) {
            this.collator = collator;
            this.fileChooser = fileChooser;
        }
        public int compare(Object o1, Object o2) {
            return compare((File) o1, (File) o2);
        }
        public int compare(File f1, File f2) {
            return collator.compare(fileChooser.getName(f1), fileChooser.getName(f2));
            //return f1.getName().compareTo(f2.getName());
        }
        
    }
}
