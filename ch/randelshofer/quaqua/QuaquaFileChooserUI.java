/*
 * @(#)QuaquaFileChooserUI.java  1.0.1  2003-11-12
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

package ch.randelshofer.quaqua;

import ch.randelshofer.quaqua.filechooser.*;

//import ch.randelshofer.gui.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.*;
import javax.swing.event.*;
import javax.swing.plaf.*;
import javax.swing.plaf.basic.*;
import javax.swing.table.*;
import javax.swing.text.Position;
import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DateFormat;
import java.util.*;
import javax.swing.plaf.basic.*;
import javax.swing.tree.*;

//import sun.awt.shell.ShellFolder;

/**
 * A replacement for the AaquaFileChooserUI. Provides a column view similar
 * to the one provided with the native Aqua user interface on Mac OS X.
 *
 * @author Werner Randelshofer
 * @version 1.0.1 2003-11-12 Added approveSelectionAction as a listener
 * to the fileNameTextField.
 * <br>1.0 2003-10-06 Good enough to bear this version number.
 * <br>0.1 2003-07-24  Created.
 *
 */
public class QuaquaFileChooserUI extends BasicFileChooserUI {
    // Implementation derived from MetalFileChooserUI
    /* Browser icons */
    protected Icon expandedIcon = null;
    protected Icon expandingIcon = null;
    
    /* Models. */
    private DirectoryComboBoxModel directoryComboBoxModel;
    private Action directoryComboBoxAction = new DirectoryComboBoxAction();
    private OSXFileView fileView;
    private FilterComboBoxModel filterComboBoxModel;
    private FileSystemTreeModel model = null;
    
    // Preferred and Minimum sizes for the dialog box
    private static int PREF_WIDTH = 430;
    private static int PREF_HEIGHT = 330;
    private static Dimension PREF_SIZE = new Dimension(PREF_WIDTH, PREF_HEIGHT);
    
    private static int MIN_WIDTH = 430;
    private static int MIN_HEIGHT = 330;
    private static Dimension MIN_SIZE = new Dimension(MIN_WIDTH, MIN_HEIGHT);
    
    // Labels, mnemonics, and tooltips (oh my!)
    private int    lookInLabelMnemonic = 0;
    private String lookInLabelText = null;
    private String saveInLabelText = null;
    
    private int    fileNameLabelMnemonic = 0;
    private String fileNameLabelText = null;
    
    private int    filesOfTypeLabelMnemonic = 0;
    private String filesOfTypeLabelText = null;
    
    private String upFolderToolTipText = null;
    private String upFolderAccessibleName = null;
    
    private String homeFolderToolTipText = null;
    private String homeFolderAccessibleName = null;
    
    private String newFolderToolTipText = null;
    private String newFolderAccessibleName = null;
    
    private String
    newFolderDialogPrompt,
    newFolderDefaultName,
    newFolderErrorText,
    newFolderExistsErrorText,
    newFolderButtonText,
    newFolderTitleText;
    
    private boolean directorySelected = false;
    private File directory = null;
    
    /**
     * Actions.
     */
    private Action newFolderAction = new NewFolderAction();
    private Action approveSelectionAction = new ApproveSelectionAction();
    
    
    /**
     * Values greater zero indicate that the UI is adjusting.
     * This is required to prevent the UI from changing the FileChooser's state
     * while processing a PropertyChangeEvent fired from the FileChooser.
     */
    private int isAdjusting = 0;
    
    // Variables declaration - do not modify
    private javax.swing.JPanel accessoryPanel;
    private javax.swing.JButton approveButton;
    private ch.randelshofer.quaqua.JBrowser browser;
    private javax.swing.JScrollPane browserScrollPane;
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JButton cancelButton;
    private javax.swing.JComboBox directoryComboBox;
    private javax.swing.JLabel fileNameLabel;
    private javax.swing.JTextField fileNameTextField;
    private javax.swing.JLabel filesOfTypeLabel;
    private javax.swing.JComboBox filterComboBox;
    private javax.swing.JPanel formatPanel;
    private javax.swing.JPanel formatPanel2;
    private javax.swing.JPanel fromPanel;
    private javax.swing.JLabel lookInLabel;
    private javax.swing.JButton newFolderButton;
    private javax.swing.JPanel separatorPanel;
    private javax.swing.JPanel separatorPanel1;
    private javax.swing.JPanel separatorPanel2;
    private javax.swing.JPanel strutPanel1;
    private javax.swing.JPanel strutPanel2;
    // End of variables declaration
    
    //
    // ComponentUI Interface Implementation methods
    //
    public static ComponentUI createUI(JComponent c) {
        return new QuaquaFileChooserUI((JFileChooser) c);
    }
    
    public QuaquaFileChooserUI(JFileChooser filechooser) {
        super(filechooser);
    }
    
    public void installUI(JComponent c) {
        super.installUI(c);
    }
    
    public void uninstallComponents(JFileChooser fc) {
        fc.removeAll();
        buttonPanel = null;
    }
    
    public void installComponents(JFileChooser fc) {
        FileSystemView fsv = fc.getFileSystemView();
        
        // Form definition  - do not modify
        java.awt.GridBagConstraints gridBagConstraints;
        
        fromPanel = new javax.swing.JPanel();
        fileNameLabel = new javax.swing.JLabel();
        fileNameTextField = new javax.swing.JTextField();
        strutPanel1 = new javax.swing.JPanel();
        lookInLabel = new javax.swing.JLabel();
        directoryComboBox = new javax.swing.JComboBox();
        strutPanel2 = new javax.swing.JPanel();
        separatorPanel1 = new javax.swing.JPanel();
        separatorPanel2 = new javax.swing.JPanel();
        browserScrollPane = new javax.swing.JScrollPane();
        browser = new ch.randelshofer.quaqua.JBrowser();
        newFolderButton = new javax.swing.JButton();
        separatorPanel = new javax.swing.JPanel();
        formatPanel = new javax.swing.JPanel();
        formatPanel2 = new javax.swing.JPanel();
        filesOfTypeLabel = new javax.swing.JLabel();
        filterComboBox = new javax.swing.JComboBox();
        accessoryPanel = new javax.swing.JPanel();
        buttonPanel = new javax.swing.JPanel();
        cancelButton = new javax.swing.JButton();
        approveButton = new javax.swing.JButton();
        
        fc.setLayout(new java.awt.GridBagLayout());
        
        fromPanel.setLayout(new java.awt.GridBagLayout());
        
        fileNameLabel.setText("Save as:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 14, 0);
        fromPanel.add(fileNameLabel, gridBagConstraints);
        
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 14, 4);
        fromPanel.add(fileNameTextField, gridBagConstraints);
        
        strutPanel1.setLayout(null);
        
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.ipadx = 40;
        gridBagConstraints.ipady = 5;
        fromPanel.add(strutPanel1, gridBagConstraints);
        
        lookInLabel.setText("From:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
        fromPanel.add(lookInLabel, gridBagConstraints);
        
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 2, 0, 0);
        fromPanel.add(directoryComboBox, gridBagConstraints);
        
        strutPanel2.setLayout(new java.awt.BorderLayout());
        
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.ipadx = 40;
        gridBagConstraints.ipady = 5;
        fromPanel.add(strutPanel2, gridBagConstraints);
        
        separatorPanel1.setLayout(new java.awt.BorderLayout());
        
        separatorPanel1.setBackground((java.awt.Color) javax.swing.UIManager.getDefaults().get("Separator.foreground"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 40;
        gridBagConstraints.ipady = 1;
        fromPanel.add(separatorPanel1, gridBagConstraints);
        
        separatorPanel2.setLayout(new java.awt.BorderLayout());
        
        separatorPanel2.setBackground((java.awt.Color) javax.swing.UIManager.getDefaults().get("Separator.foreground"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.ipadx = 40;
        gridBagConstraints.ipady = 1;
        fromPanel.add(separatorPanel2, gridBagConstraints);
        
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(16, 0, 0, 0);
        fc.add(fromPanel, gridBagConstraints);
        
        browserScrollPane.setHorizontalScrollBarPolicy(javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        browserScrollPane.setVerticalScrollBarPolicy(javax.swing.JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        browserScrollPane.setViewportView(browser);
        
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(14, 24, 0, 24);
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        fc.add(browserScrollPane, gridBagConstraints);
        
        newFolderButton.setText("New Folder");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 0, 0);
        fc.add(newFolderButton, gridBagConstraints);
        
        separatorPanel.setLayout(new java.awt.BorderLayout());
        
        separatorPanel.setBackground((java.awt.Color) javax.swing.UIManager.getDefaults().get("Separator.foreground"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipady = 1;
        gridBagConstraints.insets = new java.awt.Insets(14, 0, 0, 0);
        gridBagConstraints.weightx = 1.0;
        fc.add(separatorPanel, gridBagConstraints);
        
        formatPanel.setLayout(new java.awt.GridBagLayout());
        
        formatPanel2.setLayout(new java.awt.BorderLayout(2, 0));
        
        filesOfTypeLabel.setText("Format:");
        formatPanel2.add(filesOfTypeLabel, java.awt.BorderLayout.WEST);
        
        formatPanel2.add(filterComboBox, java.awt.BorderLayout.CENTER);
        
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 40, 0, 40);
        formatPanel.add(formatPanel2, gridBagConstraints);
        
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(14, 0, 0, 0);
        fc.add(formatPanel, gridBagConstraints);
        
        accessoryPanel.setLayout(new java.awt.BorderLayout());
        
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(14, 20, 0, 20);
        fc.add(accessoryPanel, gridBagConstraints);
        
        buttonPanel.setLayout(new java.awt.GridBagLayout());
        
        cancelButton.setText("Cancel");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 20, 0);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.weightx = 1.0;
        buttonPanel.add(cancelButton, gridBagConstraints);
        
        approveButton.setText("Open");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 20, 24);
        buttonPanel.add(approveButton, gridBagConstraints);
        
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(14, 0, 0, 0);
        fc.add(buttonPanel, gridBagConstraints);
        // End of form definition
        
        //Configure JBrowser
        browser.setCellRenderer(new FileRenderer());
        if (fc.isMultiSelectionEnabled()) {
            browser.setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
        } else {
            browser.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        }
        browser.setModel(getTreeModel());
        browser.addTreeSelectionListener(createBrowserSelectionListener(fc));
        browser.addMouseListener(createDoubleClickListener(fc));
        /* I don't know if I really do need this.
        getTreeModel().addTreeModelListener(new TreeModelListener() {
            public void treeNodesChanged(TreeModelEvent e) {
            }
         
            public void treeNodesInserted(TreeModelEvent e) {
                new DelayedSelectionUpdater();
            }
         
            public void treeStructureChanged(TreeModelEvent e) {
                // Update the selection after JList has been updated
                new DelayedSelectionUpdater();
            }
         
            public void treeNodesRemoved(TreeModelEvent e) {
            }
        });
         */
        
        // Configure Format Panel
        formatPanel.setVisible(fc.getChoosableFileFilters().length > 1);
        
        // Configure Accessory Panel
        JComponent accessory = fc.getAccessory();
        if(accessory != null) {
            getAccessoryPanel().add(accessory);
        } else {
            accessoryPanel.setVisible(false);
        }
        
        // Text assignment
        lookInLabel.setText(lookInLabelText);
        lookInLabel.setDisplayedMnemonic(lookInLabelMnemonic);
        //newFolderButton.setText(newFolderText);
        newFolderButton.setToolTipText(newFolderToolTipText);
        fileNameLabel.setText(fileNameLabelText);
        fileNameLabel.setDisplayedMnemonic(fileNameLabelMnemonic);
        /*
        if (fc.isMultiSelectionEnabled()) {
            setFileName(fileNameString(fc.getSelectedFiles()));
        } else {
            setFileName(fileNameString(fc.getSelectedFile()));
        }*/
        // XXX Select the files!!!!
        
        approveButton.setText(getApproveButtonText(fc));
        // Note: Metal does not use mnemonics for approve and cancel
        approveButton.addActionListener(getApproveSelectionAction());
        approveButton.setToolTipText(getApproveButtonToolTipText(fc));
        
        cancelButton.setText(cancelButtonText);
        cancelButton.setToolTipText(cancelButtonToolTipText);
        cancelButton.addActionListener(getCancelSelectionAction());
        
        if(! fc.getControlButtonsAreShown()) {
            cancelButton.setVisible(false);
            approveButton.setVisible(false);
        }
        // End of Text assignment
        
        // Model and Renderer assignment
        directoryComboBoxModel = createDirectoryComboBoxModel(fc);
        directoryComboBox.setModel(directoryComboBoxModel);
        directoryComboBox.setRenderer(createDirectoryComboBoxRenderer(fc));
        filterComboBoxModel = createFilterComboBoxModel();
        fc.addPropertyChangeListener(filterComboBoxModel);
        filterComboBox.setModel(filterComboBoxModel);
        filterComboBox.setRenderer(createFilterComboBoxRenderer());
        // Model and Renderer assignment
        
        // Listener assignment
        directoryComboBox.addActionListener(directoryComboBoxAction);
        newFolderButton.addActionListener(getNewFolderAction());
        fileNameTextField.addFocusListener(new SaveTextFocusListener());
        fileNameTextField.getDocument()
        .addDocumentListener(new SaveTextDocumentListener());
        fileNameTextField.addActionListener(getApproveSelectionAction());
        // End of listener assignment
        
        // Change component visibility to match the dialog type
        boolean isSave = (fc.getDialogType() == JFileChooser.SAVE_DIALOG)
        || (fc.getDialogType() == JFileChooser.CUSTOM_DIALOG);
        lookInLabel.setText((isSave) ? saveInLabelText : lookInLabelText);
        fileNameLabel.setVisible(isSave);
        fileNameTextField.setVisible(isSave);
        fileNameTextField.setEnabled(isSave);
        separatorPanel.setVisible(isSave);
        separatorPanel1.setVisible(isSave);
        separatorPanel2.setVisible(isSave);
        separatorPanel1.setVisible(isSave);
        newFolderButton.setVisible(isSave);
    }
    
    public JPanel getAccessoryPanel() {
        return accessoryPanel;
    }
    
    protected void installIcons(JFileChooser fc) {
        super.installIcons(fc);
        expandingIcon = UIManager.getIcon("Browser.expandingIcon");
        expandedIcon = UIManager.getIcon("Browser.expandedIcon");
    }
    
    protected void installStrings(JFileChooser fc) {
        super.installStrings(fc);
        
        Locale l;
        try {
            l = fc.getLocale();
        } catch (IllegalComponentStateException e) {
            l = Locale.getDefault();
        }
        
        lookInLabelMnemonic = UIManager.getInt("FileChooser.lookInLabelMnemonic");
        lookInLabelText = UIManager.getString("FileChooser.lookInLabelText"/*,l*/);
        if (lookInLabelText == null) lookInLabelText = "From:";
        saveInLabelText = UIManager.getString("FileChooser.saveInLabelText"/*,l*/);
        if (saveInLabelText == null) saveInLabelText = "Where:";
        
        fileNameLabelMnemonic = UIManager.getInt("FileChooser.fileNameLabelMnemonic");
        fileNameLabelText = UIManager.getString("FileChooser.fileNameLabelText"/*,l*/);
        // XXX - Localize "Save as:" text.
        //if (fileNameLabelText == null || fileNameLabelText.charAt(fileNameLabelText.length() -1) != ':') fileNameLabelText = "Save as:";
        
        filesOfTypeLabelMnemonic = UIManager.getInt("FileChooser.filesOfTypeLabelMnemonic");
        filesOfTypeLabelText = UIManager.getString("FileChooser.filesOfTypeLabelText"/*,l*/);
        
        upFolderToolTipText =  UIManager.getString("FileChooser.upFolderToolTipText"/*,l*/);
        upFolderAccessibleName = UIManager.getString("FileChooser.upFolderAccessibleName"/*,l*/);
        
        homeFolderToolTipText =  UIManager.getString("FileChooser.homeFolderToolTipText"/*,l*/);
        homeFolderAccessibleName = UIManager.getString("FileChooser.homeFolderAccessibleName"/*,l*/);
        
        newFolderToolTipText = UIManager.getString("FileChooser.newFolderToolTipText"/*,l*/);
        newFolderAccessibleName = UIManager.getString("FileChooser.newFolderAccessibleName"/*,l*/);
        
        // New Folder Dialog
        newFolderErrorText = getString("FileChooser.newFolderErrorText", l, "Error occured during folder creation");
        newFolderExistsErrorText = getString("FileChooser.newFolderExistsErrorText", l, "That name is already taken");
        newFolderButtonText = getString("FileChooser.newFolderButtonText", l, "New");
        newFolderTitleText = getString("FileChooser.newFolderTitleText", l, "New Folder");
        newFolderDialogPrompt = getString("FileChooser.newFolderPromptText", l, "Name of new folder:");
        newFolderDefaultName = getString("FileChooser.untitledFolderName", l, "untitled folder");
        newFolderTitleText = UIManager.getString("FileChooser.newFolderTitleText"/*, l*/);
        newFolderToolTipText = UIManager.getString("FileChooser.newFolderToolTipText"/*, l*/);
        newFolderAccessibleName = getString("FileChooser.newFolderAccessibleName", l, newFolderTitleText);
    }
    
    /**
     * Property to remember whether a directory is currently selected in the UI.
     *
     * @return <code>true</code> iff a directory is currently selected.
     * @since 1.4
     */
    protected boolean isDirectorySelected() {
        return directorySelected;
    }
    /**
     * Property to remember the directory that is currently selected in the UI.
     *
     * @return the value of the <code>directory</code> property
     * @see #setDirectory
     * @since 1.4
     */
    protected File getDirectory() {
        return directory;
    }
    
    /**
     * Property to remember the directory that is currently selected in the UI.
     * This is normally called by the UI on a selection event.
     *
     * @param f the <code>File</code> object representing the directory that is
     *		currently selected
     * @since 1.4
     */
    protected void setDirectory(File f) {
        directory = f;
    }
    
    private String getString(String string, Locale l, String defaultValue) {
        String value = UIManager.getString(string/*, l*/);
        return (value == null) ? defaultValue : value;
    }
    
    protected void installListeners(JFileChooser fc) {
        super.installListeners(fc);
        /*
        ActionMap actionMap = getActionMap();
        SwingUtilities.replaceUIActionMap(fc, actionMap);
         */
    }
    /*
    protected ActionMap getActionMap() {
        return createActionMap();
    }
     
    protected ActionMap createActionMap() {
        AbstractAction escAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                getFileChooser().cancelSelection();
            }
            public boolean isEnabled(){
                return getFileChooser().isEnabled();
            }
        };
        ActionMap map = new ActionMapUIResource();
        map.put("approveSelection", getApproveSelectionAction());
        map.put("cancelSelection", escAction);
        map.put("Go Up", getChangeToParentDirectoryAction());
        return map;
    }
     */
    public void createModel() {
        JFileChooser fc = getFileChooser();
        model = new FileSystemTreeModel(fc);
        fileView =  new OSXFileView(fc);
        
        // XXX - We should not overwrite the FileSystemView attribute
        // of the JFileChooser.
        fc.setFileSystemView(new OSXFileSystemView());
    }
    public FileSystemTreeModel getTreeModel() {
        return model;
    }
    public void uninstallUI(JComponent c) {
        // Remove listeners
        c.removePropertyChangeListener(filterComboBoxModel);
        cancelButton.removeActionListener(getCancelSelectionAction());
        approveButton.removeActionListener(getApproveSelectionAction());
        fileNameTextField.removeActionListener(getApproveSelectionAction());
        
        super.uninstallUI(c);
    }
    
    /*
    private class DelayedSelectionUpdater implements Runnable {
        DelayedSelectionUpdater() {
            SwingUtilities.invokeLater(this);
        }
     
        public void run() {
            setFileSelected();
        }
    }*/
    
    
    void setFileSelected() {
        JFileChooser fc = getFileChooser();
        if (fc.isMultiSelectionEnabled() /*&& !isDirectorySelected()*/) {
            JFileChooser chooser = getFileChooser();
            File[] f = null;
            if (isDirectorySelected()) {
                f = new File[] {getDirectory()};
            } else {
                f = chooser.getSelectedFiles();
            }
            if (f.length != 0) { // && (i = getTreeModel().indexOf(f)) >= 0) {
                TreePath[] paths = new TreePath[f.length];
                for (int i=0; i < f.length; i++) {
                    paths[i] = getTreeModel().toPath(f[i]);
                }
                browser.setSelectionPaths(paths);
                //ensurePathIsVisible(fPath);
            } else {
                
                // We must not clear the selection here, because the JBrowser
                // shows the whole selection tree, not just the contents of
                // the current directory.
                //  browser.clearSelection();
            }
            
            /*
            File[] files = fc.getSelectedFiles();	// Should be selected
            if (files.length == 0 && fc.getSelectedFile() != null) {
                files = new File[] { fc.getSelectedFile() };
                }
            if (files.length == 0 && fc.getCurrentDirectory() != null) {
                files = new File[] { fc.getCurrentDirectory() };
                }
            //TreePath[] selectedObjects = browser.getSelectionPaths(); // Are actually selected
System.out.println("setFileSelected:"+Arrays.asList(files));
            TreePath[] shouldBeSelected = new TreePath[files.length];
            for (int i=0; i < files.length; i++) {
                shouldBeSelected[i] = model.toPath(files[i]);
            }
            browser.setSelectionPaths(shouldBeSelected);
             */
            /*
            // Remove files that shouldn't be selected
            for (int j = 0; j < selectedObjects.length; j++) {
                boolean found = false;
                for (int i = 0; i < files.length; i++) {
                    if (files[i].equals(selectedObjects[j].getLastPathComponent())) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    browser.removeSelectionPath(selectedObjects[j]);
                }
            }
            // Add files that should be selected
            for (int i = 0; i < files.length; i++) {
                boolean found = false;
                for (int j = 0; j < selectedObjects.length; j++) {
                    if (files[i].equals(selectedObjects[j].getLastPathComponent())) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    int index = getModel().indexOf(files[i]);
                    TreePath filePath = getTreeModel().toPath(files[i]);
                    if (index >= 0) {
                        browser.addSelectionPath(filePath);
                    }
                }
            }*/
        } else {
            JFileChooser chooser = getFileChooser();
            File f = null;
            if (isDirectorySelected()) {
                f = getDirectory();
            } else {
                f = chooser.getSelectedFile();
            }
            if (f != null) { // && (i = getTreeModel().indexOf(f)) >= 0) {
                TreePath fPath = getTreeModel().toPath(f);
                browser.setSelectionPath(fPath);
                ensurePathIsVisible(fPath);
            } else {
                
                // We must not clear the selection here, because the JBrowser
                // shows the whole selection tree, not just the contents of
                // the current directory.
                //  browser.clearSelection();
            }
        }
        updateApproveButtonState(fc);
    }
    
    private String fileNameString(File file) {
        if (file == null) {
            return null;
        } else {
            return file.getName();
            /*
            JFileChooser fc = getFileChooser();
            if (fc.isDirectorySelectionEnabled() && !fc.isFileSelectionEnabled()) {
                return file.getPath();
            } else {
                return file.getName();
            }*/
        }
    }
    
    private String fileNameString(File[] files) {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; files != null && i < files.length; i++) {
            if (i > 0) {
                buf.append(" ");
            }
            if (files.length > 1) {
                buf.append("\"");
            }
            buf.append(fileNameString(files[i]));
            if (files.length > 1) {
                buf.append("\"");
            }
        }
        return buf.toString();
    }
    private boolean textfieldIsValid() {
        String string = getFileName();
        return string != null && !string.equals("");
    }
    private void updateApproveButtonState(JFileChooser fc) {
        
        getApproveButton(fc).setEnabled(
        fc.isDirectorySelectionEnabled()
        || fc.getSelectedFile() != null || textfieldIsValid()
        );
        updateApproveButton(fc);
    }
    private void updateApproveButton(JFileChooser fc) {
        approveButton.setText(getApproveButtonText(fc));
        approveButton
        .setToolTipText(getApproveButtonToolTipText(fc));
        approveButton.setMnemonic(getApproveButtonMnemonic(fc));
        //cancelButton.setToolTipText(getCancelButtonToolTipText(fc));
    }
    protected TreeSelectionListener createBrowserSelectionListener(JFileChooser fc) {
        return new BrowserSelectionListener();
    }
    /**
     * Selection listener for the list of files and directories.
     */
    protected class BrowserSelectionListener implements TreeSelectionListener {
        public void valueChanged(TreeSelectionEvent e) {
            if (isAdjusting != 0) return;
            
            JFileChooser fc = getFileChooser();
            FileSystemView fsv = fc.getFileSystemView();
            JBrowser browser = (JBrowser) e.getSource();
            
            if (fc.isMultiSelectionEnabled()) {
                TreePath[] paths = browser.getSelectionPaths();
                
                File file;
                if (paths.length > 0) {
                    file = ((FileSystemTreeModel.Node) paths[0].getLastPathComponent()).getFile();
                } else {
                    file = null;
                }
                
                /* FIXME - This needs JDK 1.4 to work.
                if (paths.length == 1
                && file.isDirectory()
                && fc.isTraversable(file)
                && (fc.getFileSelectionMode() == fc.FILES_ONLY
                || !fsv.isFileSystem(file))) {
                 */
                if (paths.length == 1
                && file.isDirectory()
                && fc.isTraversable(file)
                && fc.getFileSelectionMode() == fc.FILES_ONLY
                ) {
                    setDirectorySelected(true);
                    setDirectory(file);
                    directoryComboBoxModel.addItem(file);
                    fc.setSelectedFile(null);
                    fc.setSelectedFiles(null);
                } else {
                    
                    // Remove directories from selection if selection mode is files only.
                    boolean filesOnly = fc.getFileSelectionMode() == fc.FILES_ONLY;
                    int j = 0;
                    File[] files = new File[paths.length];
                    for (int i = 0; i < paths.length; i++) {
                        File f = ((FileSystemTreeModel.Node) paths[i].getLastPathComponent()).getFile();
                        if (! filesOnly || ! f.isDirectory()) {
                            files[j++] = f;
                        }
                    }
                    if (j == 0) {
                        // Only directories selected?
                        // Choose lead selection path
                        files = new File[] { ((FileSystemTreeModel.Node)  browser.getSelectionModel().getLeadSelectionPath().getLastPathComponent()).getFile() };
                    } else {
                        if (j < paths.length) {
                            File[] tmpFiles = new File[j];
                            System.arraycopy(files, 0, tmpFiles, 0, j);
                            files = tmpFiles;
                        }
                    }
                    
                    if (files.length == 1 && files[0].isDirectory()) {
                        if (fc.getFileSelectionMode() == fc.FILES_ONLY) {
                            setDirectorySelected(true);
                        } else {
                            setDirectorySelected(false);
                        }
                        setDirectory(files[0]);
                        fc.setSelectedFiles(files);
                        directoryComboBoxModel.addItem(files[0]);
                    } else {
                        setDirectorySelected(false);
                        setDirectory(files[0].getParentFile());
                        fc.setSelectedFiles(files);
                        
                        directoryComboBoxModel.addItem(files[0].getParentFile());
                    }
                }
            } else {
                File file = ((FileSystemTreeModel.Node) browser.getSelectionPath().getLastPathComponent()).getFile();
                /* FIXME - This needs JDK 1.4 to work.
                if (file != null
                && file.isDirectory()
                && fc.isTraversable(file)
                && (fc.getFileSelectionMode() == fc.FILES_ONLY
                || !fsv.isFileSystem(file))) {*/
                if (file != null
                && file.isDirectory()
                && fc.isTraversable(file)
                && fc.getFileSelectionMode() == fc.FILES_ONLY
                ) {
                    setDirectorySelected(true);
                    setDirectory(file);
                    directoryComboBoxModel.addItem(file);
                    fc.setSelectedFile(null);
                } else {
                    setDirectorySelected(false);
                    if (file != null) {
                        // The JFileChooser hopefully changes the current
                        // Directory for us.
                        //fc.setCurrentDirectory(file.getParentFile());
                        fc.setSelectedFile(file);
                    }
                }
            }
        }
    }
    
    
    protected class FileRenderer implements ListCellRenderer  {
        private JPanel panel;
        private JLabel textLabel;
        private JLabel arrowLabel;
        private EmptyBorder noFocusBorder;
        public FileRenderer() {
            noFocusBorder = new EmptyBorder(1, 1, 1, 1);
            panel = new JPanel(new BorderLayout()) {
                // Overridden for performance reasons.
                //public void validate() {}
                public void revalidate() {}
                public void repaint(long tm, int x, int y, int width, int height) {}
                public void repaint(Rectangle r) {}
                protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {}
                public void firePropertyChange(String propertyName, short oldValue, short newValue) {}
                public void firePropertyChange(String propertyName, int oldValue, int newValue) {}
                public void firePropertyChange(String propertyName, long oldValue, long newValue) {}
                public void firePropertyChange(String propertyName, float oldValue, float newValue) {}
                public void firePropertyChange(String propertyName, double oldValue, double newValue) {}
                public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {}
            };
            
            textLabel = new JLabel() {
                // Overridden for performance reasons.
                public void validate() {}
                public void revalidate() {}
                public void repaint(long tm, int x, int y, int width, int height) {}
                public void repaint(Rectangle r) {}
                protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
                    // Strings get interned...
                    if (propertyName=="text")
                        super.firePropertyChange(propertyName, oldValue, newValue);
                }
                public void firePropertyChange(String propertyName, short oldValue, short newValue) {}
                public void firePropertyChange(String propertyName, int oldValue, int newValue) {}
                public void firePropertyChange(String propertyName, long oldValue, long newValue) {}
                public void firePropertyChange(String propertyName, float oldValue, float newValue) {}
                public void firePropertyChange(String propertyName, double oldValue, double newValue) {}
                public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {}
            };
            arrowLabel = new JLabel() {
                // Overridden for performance reasons.
                public void validate() {}
                public void revalidate() {}
                public void repaint(long tm, int x, int y, int width, int height) {}
                public void repaint(Rectangle r) {}
                protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
                    // Strings get interned...
                    if (propertyName=="text")
                        super.firePropertyChange(propertyName, oldValue, newValue);
                }
                public void firePropertyChange(String propertyName, short oldValue, short newValue) {}
                public void firePropertyChange(String propertyName, int oldValue, int newValue) {}
                public void firePropertyChange(String propertyName, long oldValue, long newValue) {}
                public void firePropertyChange(String propertyName, float oldValue, float newValue) {}
                public void firePropertyChange(String propertyName, double oldValue, double newValue) {}
                public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {}
            };
            
            panel.setOpaque(true);
            panel.setBorder(noFocusBorder);
            
            panel.add(textLabel, BorderLayout.CENTER);
            
            //arrowLabel.setIcon(expandedIcon);
            panel.add(arrowLabel, BorderLayout.EAST);
        }
        
        public Component getListCellRendererComponent(JList list, Object value,
        int index, boolean isSelected,
        boolean cellHasFocus) {
            
            FileSystemTreeModel.Node node = (FileSystemTreeModel.Node) value;
            File file = node.getFile();
            
            
            //setComponentOrientation(list.getComponentOrientation());
            if (isSelected) {
                panel.setBackground(list.getSelectionBackground());
                //textLabel.setBackground(list.getSelectionBackground());
                textLabel.setForeground(list.getSelectionForeground());
                //arrowLabel.setBackground(list.getSelectionBackground());
                arrowLabel.setForeground(list.getSelectionForeground());
            } else {
                panel.setBackground(list.getBackground());
                //textLabel.setBackground(list.getBackground());
                textLabel.setForeground(list.getForeground());
                //arrowLabel.setBackground(list.getBackground());
                arrowLabel.setForeground(list.getForeground());
            }
            
            textLabel.setText(getFileChooser().getName(file));
            textLabel.setIcon(getFileChooser().getIcon(file));
            
            arrowLabel.setVisible(! node.isLeaf());
            arrowLabel.setIcon((node.isUpdatingCache()) ? expandingIcon : expandedIcon);
            
            textLabel.setEnabled(list.isEnabled());
            textLabel.setFont(list.getFont());
            panel.setBorder((cellHasFocus) ? UIManager.getBorder("List.focusCellHighlightBorder") : noFocusBorder);
            
            return panel;
        }
    }
    
    /**
     * Returns the preferred size of the specified
     * <code>JFileChooser</code>.
     * The preferred size is at least as large,
     * in both height and width,
     * as the preferred size recommended
     * by the file chooser's layout manager.
     *
     * @param c  a <code>JFileChooser</code>
     * @return   a <code>Dimension</code> specifying the preferred
     *           width and height of the file chooser
     */
    public Dimension getPreferredSize(JComponent c) {
        Dimension d = c.getLayout().preferredLayoutSize(c);
        if (d != null) {
            return new Dimension(d.width < PREF_SIZE.width ? PREF_SIZE.width : d.width,
            d.height < PREF_SIZE.height ? PREF_SIZE.height : d.height);
        } else {
            return new Dimension(PREF_SIZE.width, PREF_SIZE.height);
        }
    }
    
    /**
     * Returns the minimum size of the <code>JFileChooser</code>.
     *
     * @param c  a <code>JFileChooser</code>
     * @return   a <code>Dimension</code> specifying the minimum
     *           width and height of the file chooser
     */
    public Dimension getMinimumSize(JComponent c) {
        return MIN_SIZE;
    }
    
    /**
     * Returns the maximum size of the <code>JFileChooser</code>.
     *
     * @param c  a <code>JFileChooser</code>
     * @return   a <code>Dimension</code> specifying the maximum
     *           width and height of the file chooser
     */
    public Dimension getMaximumSize(JComponent c) {
        return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
    }
    
    
    /* The following methods are used by the PropertyChange Listener */
    
    private void doSelectedFileChanged(PropertyChangeEvent e) {
        if (e.getNewValue() != null) {
            JFileChooser fc = getFileChooser();
            File f = (File) e.getNewValue();
            if (f != null) {
                //directoryComboBoxModel.setSelectedItem(f);
                browser.setSelectionPath(getTreeModel().toPath(f));
                directoryComboBoxModel.addItem(f.isDirectory() ? f : f.getParentFile());
            }
            /*
            if (! fc.isDirectorySelectionEnabled()) {
                setFileName(fileNameString(f));
            }*/
        }
        setFileSelected();
    }
    
    private void doSelectedFilesChanged(PropertyChangeEvent e) {
        if (e.getNewValue() != null) {
            JFileChooser fc = getFileChooser();
            File[] f = (File[]) e.getNewValue();
            if (f != null && f.length != 0) {
                //directoryComboBoxModel.setSelectedItem(f[0]);
                //browser.setSelectionPath(getTreeModel().toPath(f));
                directoryComboBoxModel.addItem(f[0].isDirectory() ? f[0] : f[0].getParentFile());
            }
            /*
            if (! fc.isDirectorySelectionEnabled()) {
                setFileName(fileNameString(f));
            }*/
        }
        setFileSelected();
        /*
        //applyEdit();
        File[] files = (File[]) e.getNewValue();
        JFileChooser fc = getFileChooser();
        if (files != null
        && files.length > 0
        && (files.length > 1 || fc.isDirectorySelectionEnabled() || !files[0].isDirectory())) {
            //setFileName(fileNameString(files));
            directoryComboBoxModel.addItem(files[0].getParentFile());
            setFileSelected();
        }*/
    }
    
    private void doDirectoryChanged(PropertyChangeEvent e) {
        JFileChooser fc = getFileChooser();
        FileSystemView fsv = fc.getFileSystemView();
        clearIconCache();
        
        // When directory selection is enabled,
        // we do not have to change the selection path here, because this
        // will be done by doFileChanged.
        if (isDirectorySelected() || ! fc.isDirectorySelectionEnabled()) {
            browser.setSelectionPath(model.toPath((File) e.getNewValue()));
        }
        /*
        browser.setSelectionPath(
        model.toPath(
        (fc.getSelectedFile() == null)
        ? (File) e.getNewValue()
        : fc.getSelectedFile()
        )
        );*/
        
        File currentDirectory = (File) e.getNewValue();
        if(currentDirectory != null) {
            directoryComboBoxModel.addItem(currentDirectory);
            getNewFolderAction().setEnabled(currentDirectory.canWrite());
            getChangeToParentDirectoryAction().setEnabled(!fsv.isRoot(currentDirectory));
            /*
            if (fc.isDirectorySelectionEnabled() && !fc.isFileSelectionEnabled()) {
                if (fsv.isFileSystem(currentDirectory)) {
                    setFileName(currentDirectory.getPath());
                } else {
                    setFileName(null);
                }
            }*/
            
            if (fc.getDialogType() == JFileChooser.OPEN_DIALOG) {
                setFileName(null);
                updateApproveButtonState(fc);
            }
        }
    }
    
    private void doFilterChanged(PropertyChangeEvent e) {
        //applyEdit();
        //resetEditIndex();
        clearIconCache();
        model.updatePath(browser.getSelectionPath());
        //browser.clearSelection();
    }
    
    private void doFileSelectionModeChanged(PropertyChangeEvent e) {
        //applyEdit();
        //resetEditIndex();
        clearIconCache();
        //browser.clearSelection();
        
        JFileChooser fc = getFileChooser();
        File currentDirectory = fc.getCurrentDirectory();
        /*
        if (currentDirectory != null
        && fc.isDirectorySelectionEnabled()
        && !fc.isFileSelectionEnabled()
        && fc.getFileSystemView().isFileSystem(currentDirectory)) {
         
            setFileName(currentDirectory.getName());
        } else {
            setFileName(null);
        }*/
        setFileName(null);
        updateApproveButtonState(fc);
    }
    
    private void doMultiSelectionChanged(PropertyChangeEvent e) {
        if (getFileChooser().isMultiSelectionEnabled()) {
            browser.setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
        } else {
            browser.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
            //browser.clearSelection();
            getFileChooser().setSelectedFiles(null);
        }
    }
    
    private void doChoosableFilterChanged(PropertyChangeEvent e) {
        JFileChooser fc = getFileChooser();
        boolean isChooserVisible = ((FileFilter[]) e.getNewValue()).length > 1;
        formatPanel.setVisible(isChooserVisible);
    }
    private void doAccessoryChanged(PropertyChangeEvent e) {
        if(getAccessoryPanel() != null) {
            if(e.getOldValue() != null) {
                getAccessoryPanel().remove((JComponent) e.getOldValue());
            }
            JComponent accessory = (JComponent) e.getNewValue();
            if(accessory != null) {
                getAccessoryPanel().add(accessory, BorderLayout.CENTER);
            }
            accessoryPanel.setVisible(accessory != null);
        }
    }
    
    private void doApproveButtonTextChanged(PropertyChangeEvent e) {
        JFileChooser chooser = getFileChooser();
        approveButton.setText(getApproveButtonText(chooser));
        approveButton.setToolTipText(getApproveButtonToolTipText(chooser));
    }
    
    private void doDialogTypeChanged(PropertyChangeEvent e) {
        JFileChooser fc = getFileChooser();
        approveButton.setText(getApproveButtonText(fc));
        approveButton.setToolTipText(getApproveButtonToolTipText(fc));
        boolean isSave = (fc.getDialogType() == JFileChooser.SAVE_DIALOG)
        || (fc.getDialogType() == JFileChooser.CUSTOM_DIALOG);
        lookInLabel.setText((isSave) ? saveInLabelText : lookInLabelText);
        fileNameLabel.setVisible(isSave);
        fileNameTextField.setVisible(isSave);
        fileNameTextField.setEnabled(isSave);
        separatorPanel.setVisible(isSave);
        separatorPanel1.setVisible(isSave);
        separatorPanel2.setVisible(isSave);
        separatorPanel1.setVisible(isSave);
        newFolderButton.setVisible(isSave);
    }
    
    private void doApproveButtonMnemonicChanged(PropertyChangeEvent e) {
        // Note: Metal does not use mnemonics for approve and cancel
    }
    
    private void doControlButtonsChanged(PropertyChangeEvent e) {
        if(getFileChooser().getControlButtonsAreShown()) {
            addControlButtons();
        } else {
            removeControlButtons();
        }
    }
    
    /*
     * Listen for filechooser property changes, such as
     * the selected file changing, or the type of the dialog changing.
     */
    public PropertyChangeListener createPropertyChangeListener(JFileChooser fc) {
        return new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent e) {
                isAdjusting++;
                
                String s = e.getPropertyName();
                if (s.equals(JFileChooser.SELECTED_FILE_CHANGED_PROPERTY)) {
                    doSelectedFileChanged(e);
                } else if (s.equals(JFileChooser.SELECTED_FILES_CHANGED_PROPERTY)) {
                    doSelectedFilesChanged(e);
                } else if(s.equals(JFileChooser.DIRECTORY_CHANGED_PROPERTY)) {
                    doDirectoryChanged(e);
                } else if(s.equals(JFileChooser.FILE_FILTER_CHANGED_PROPERTY)) {
                    doFilterChanged(e);
                } else if(s.equals(JFileChooser.FILE_SELECTION_MODE_CHANGED_PROPERTY)) {
                    doFileSelectionModeChanged(e);
                } else if(s.equals(JFileChooser.MULTI_SELECTION_ENABLED_CHANGED_PROPERTY)) {
                    doMultiSelectionChanged(e);
                } else if(s.equals(JFileChooser.ACCESSORY_CHANGED_PROPERTY)) {
                    doAccessoryChanged(e);
                } else if(s.equals(JFileChooser.CHOOSABLE_FILE_FILTER_CHANGED_PROPERTY)) {
                    doChoosableFilterChanged(e);
                } else if (s.equals(JFileChooser.APPROVE_BUTTON_TEXT_CHANGED_PROPERTY) ||
                s.equals(JFileChooser.APPROVE_BUTTON_TOOL_TIP_TEXT_CHANGED_PROPERTY)) {
                    doApproveButtonTextChanged(e);
                } else if(s.equals(JFileChooser.DIALOG_TYPE_CHANGED_PROPERTY)) {
                    doDialogTypeChanged(e);
                } else if(s.equals(JFileChooser.APPROVE_BUTTON_MNEMONIC_CHANGED_PROPERTY)) {
                    doApproveButtonMnemonicChanged(e);
                } else if(s.equals(JFileChooser.CONTROL_BUTTONS_ARE_SHOWN_CHANGED_PROPERTY)) {
                    doControlButtonsChanged(e);
                } else if(s.equals(JFileChooser.CANCEL_SELECTION)) {
                    // applyEdit();
                } else if (s.equals("componentOrientation")) {
                    /* FIXME - This needs JDK 1.4 to work.
                    ComponentOrientation o = (ComponentOrientation)e.getNewValue();
                    JFileChooser fc = (JFileChooser)e.getSource();
                    if (o != (ComponentOrientation)e.getOldValue()) {
                        fc.applyComponentOrientation(o);
                    }
                     */
                } else if (s.equals("ancestor")) {
                    if (e.getOldValue() == null && e.getNewValue() != null) {
                        // Ancestor was added, ensure path is visible and
                        // set initial focus
                        browser.ensurePathIsVisible(browser.getSelectionPath());
                        fileNameTextField.selectAll();
                        fileNameTextField.requestFocus();
                    }
                }
                
                isAdjusting--;
            }
        };
    }
    
    
    protected void removeControlButtons() {
        buttonPanel.setVisible(false);
    }
    
    protected void addControlButtons() {
        buttonPanel.setVisible(true);
    }
    
    
    private void ensurePathIsVisible(TreePath path) {
        browser.ensurePathIsVisible(path);
    }
    public void ensureFileIsVisible(JFileChooser fc, File f) {
        if (f != null) {
            ensurePathIsVisible(getTreeModel().toPath(f));
        }
    }
    
    public void rescanCurrentDirectory(JFileChooser fc) {
        clearIconCache();
        model.updatePath(browser.getSelectionPath());
    }
    
    public String getFileName() {
        if (fileNameTextField != null) {
            return fileNameTextField.getText();
        } else {
            return null;
        }
    }
    
    public void setFileName(String filename) {
        if (fileNameTextField != null) {
            fileNameTextField.setText(filename);
        }
    }
    
    /**
     * Property to remember whether a directory is currently selected in the UI.
     * This is normally called by the UI on a selection event.
     *
     * @param directorySelected if a directory is currently selected.
     * @since 1.4
     */
    protected void setDirectorySelected(boolean directorySelected) {
        this.directorySelected = directorySelected;
        /* FIXME - This needs JDK 1.4 to work
        super.setDirectorySelected(directorySelected);
         */
        
        JFileChooser chooser = getFileChooser();
        /*if(directorySelected) {
            approveButton.setText(directoryOpenButtonText);
            approveButton.setToolTipText(directoryOpenButtonToolTipText);
        } else {*/
        approveButton.setText(getApproveButtonText(chooser));
        approveButton.setToolTipText(getApproveButtonToolTipText(chooser));
        //}
    }
    
    
    protected DirectoryComboBoxRenderer createDirectoryComboBoxRenderer(JFileChooser fc) {
        return new DirectoryComboBoxRenderer();
    }
    
    
    //
    // Renderer for DirectoryComboBox
    //
    class DirectoryComboBoxRenderer extends DefaultListCellRenderer  {
        IndentIcon ii = new IndentIcon();
        public Component getListCellRendererComponent(JList list, Object value,
        int index, boolean isSelected,
        boolean cellHasFocus) {
            
            
            // String objects are used to denote delimiters.
            if (value instanceof String) {
                super.getListCellRendererComponent(list, value, index, false, cellHasFocus);
                setText((String) value);
                setPreferredSize(new Dimension(10, 14));
                return this;
            }
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            setPreferredSize(null);
            File directory = (File) value;
            if (directory == null) {
                File root = new File("/");
            setText(getFileChooser().getName(root));
            ii.icon = getFileChooser().getIcon(root);
            } else {
            setText(getFileChooser().getName(directory));
            ii.icon = getFileChooser().getIcon(directory);
            }
            //ii.depth = directoryComboBoxModel.getDepth(index);
            ii.depth = 0;
            setIcon(ii);
            
            return this;
        }
    }
    
    final static int space = 10;
    class IndentIcon implements Icon {
        
        Icon icon = null;
        int depth = 0;
        
        public void paintIcon(Component c, Graphics g, int x, int y) {
            if (icon != null) {
                if (c.getComponentOrientation().isLeftToRight()) {
                    icon.paintIcon(c, g, x+depth*space, y);
                } else {
                    icon.paintIcon(c, g, x, y);
                }
            }
        }
        
        public int getIconWidth() {
            return (icon == null) ? depth*space : icon.getIconWidth() + depth*space;
        }
        
        public int getIconHeight() {
            return (icon == null) ? 0 : icon.getIconHeight();
        }
        
    }
    
    //
    // DataModel for DirectoryComboxbox
    //
    protected DirectoryComboBoxModel createDirectoryComboBoxModel(JFileChooser fc) {
        return new DirectoryComboBoxModel();
    }
    
    /**
     * Data model for a directory selection combo-box.
     */
    protected class DirectoryComboBoxModel extends AbstractListModel
    implements ComboBoxModel {
        Object directories[] = new Object[5];
        //int[] depths = null;
        Object selectedDirectory = null;
        JFileChooser chooser = getFileChooser();
        FileSystemView fsv = chooser.getFileSystemView();
        
        public DirectoryComboBoxModel() {
            // Add the current directory to the model, and make it the
            // selectedDirectory
            File dir = getFileChooser().getCurrentDirectory();
            if(dir != null) {
                addItem(dir);
            }
            
            // Hardcode this.
            // The QuaquaFileChooserUI only works on Mac OS X anyway.
            directories[0] = new File(System.getProperty("user.home"));
            directories[1] = ""; // We use String's to denote separators.
            directories[2] = new File(System.getProperty("user.home"), "Desktop");
            directories[3] = new File(System.getProperty("user.home"));
            directories[4] = new File("/");
        }
        
        /**
         * Adds the directory to the model and sets it to be selected,
         * additionally clears out the previous selected directory and
         * the paths leading up to it, if any.
         */
        private void addItem(File directory) {
            isAdjusting++;
            directories[0] = directory;
            selectedDirectory = directory;
            fireContentsChanged(this, -1, -1);
            fireContentsChanged(this, 0, 0);
            isAdjusting--;
        }
        
        public void setSelectedItem(Object selectedDirectory) {
            if (selectedDirectory instanceof File) {
                this.selectedDirectory = (File) selectedDirectory;
                fireContentsChanged(this, -1, -1);
            }
        }
        
        public Object getSelectedItem() {
            return selectedDirectory;
        }
        
        public int getSize() {
            return directories.length;
        }
        
        public Object getElementAt(int index) {
            return directories[index];
        }
    }
    
    //
    // Renderer for Types ComboBox
    //
    protected FilterComboBoxRenderer createFilterComboBoxRenderer() {
        return new FilterComboBoxRenderer();
    }
    
    /**
     * Render different type sizes and styles.
     */
    public class FilterComboBoxRenderer extends DefaultListCellRenderer {
        public Component getListCellRendererComponent(JList list,
        Object value, int index, boolean isSelected,
        boolean cellHasFocus) {
            
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            
            if (value != null && value instanceof FileFilter) {
                setText(((FileFilter)value).getDescription());
            }
            
            return this;
        }
    }
    
    //
    // DataModel for Types Comboxbox
    //
    protected FilterComboBoxModel createFilterComboBoxModel() {
        return new FilterComboBoxModel();
    }
    
    /**
     * Data model for a type-face selection combo-box.
     */
    protected class FilterComboBoxModel
    extends AbstractListModel
    implements ComboBoxModel, PropertyChangeListener {
        protected FileFilter[] filters;
        protected FilterComboBoxModel() {
            super();
            filters = getFileChooser().getChoosableFileFilters();
        }
        
        public void propertyChange(PropertyChangeEvent e) {
            String prop = e.getPropertyName();
            if(prop == JFileChooser.CHOOSABLE_FILE_FILTER_CHANGED_PROPERTY) {
                filters = (FileFilter[]) e.getNewValue();
                fireContentsChanged(this, -1, -1);
            } else if (prop == JFileChooser.FILE_FILTER_CHANGED_PROPERTY) {
                fireContentsChanged(this, -1, -1);
            }
        }
        
        public void setSelectedItem(Object filter) {
            if(filter != null) {
                getFileChooser().setFileFilter((FileFilter) filter);
                setFileName(null);
                fireContentsChanged(this, -1, -1);
            }
        }
        
        public Object getSelectedItem() {
            // Ensure that the current filter is in the list.
            // NOTE: we shouldnt' have to do this, since JFileChooser adds
            // the filter to the choosable filters list when the filter
            // is set. Lets be paranoid just in case someone overrides
            // setFileFilter in JFileChooser.
            FileFilter currentFilter = getFileChooser().getFileFilter();
            boolean found = false;
            if(currentFilter != null) {
                for(int i=0; i < filters.length; i++) {
                    if(filters[i] == currentFilter) {
                        found = true;
                    }
                }
                if(found == false) {
                    getFileChooser().addChoosableFileFilter(currentFilter);
                }
            }
            return getFileChooser().getFileFilter();
        }
        
        public int getSize() {
            if(filters != null) {
                return filters.length;
            } else {
                return 0;
            }
        }
        
        public Object getElementAt(int index) {
            if(index > getSize() - 1) {
                // This shouldn't happen. Try to recover gracefully.
                return getFileChooser().getFileFilter();
            }
            if(filters != null) {
                return filters[index];
            } else {
                return null;
            }
        }
    }
    
    /**
     * Acts when DirectoryComboBox has changed the selected item.
     */
    protected class DirectoryComboBoxAction extends AbstractAction {
        protected DirectoryComboBoxAction() {
            super("DirectoryComboBoxAction");
        }
        
        public void actionPerformed(ActionEvent e) {
            if (isAdjusting != 0) return;
            /*
            File f = (File) directoryComboBox.getSelectedItem();
            JFileChooser fc = getFileChooser();
            if (f != null) {
                if (f.isDirectory()) {
                    fc.setCurrentDirectory(f);
                    //??? fc.setSelectedFile(null);
                } else {
                    fc.setSelectedFile(f);
                }
            }*/
            JFileChooser fc = getFileChooser();
            File file = (File) directoryComboBox.getSelectedItem();
            if (file != null) {
                if (fc.isMultiSelectionEnabled()) {
                /* FIXME - This needs JDK 1.4 to work.
                if (file.isDirectory()
                && fc.isTraversable(file)
                && (fc.getFileSelectionMode() == fc.FILES_ONLY
                || !fsv.isFileSystem(file))) {
                 */
                    if (file.isDirectory()
                    && fc.isTraversable(file)
                    && fc.getFileSelectionMode() == fc.FILES_ONLY
                    ) {
                        setDirectorySelected(true);
                        setDirectory(file);
                        directoryComboBoxModel.addItem(file);
                        fc.setSelectedFile(null);
                        fc.setSelectedFiles(null);
                    } else {
                        if (file.isDirectory()) {
                            if (fc.getFileSelectionMode() == fc.FILES_ONLY) {
                                setDirectorySelected(true);
                            } else {
                                setDirectorySelected(false);
                            }
                            setDirectory(file);
                            fc.setSelectedFiles(new File[] {file});
                        } else {
                            setDirectorySelected(false);
                            setDirectory(file.getParentFile());
                            fc.setSelectedFiles(new File[] {file});
                        }
                    }
                } else {
                /* FIXME - This needs JDK 1.4 to work.
                if (file.isDirectory()
                && fc.isTraversable(file)
                && (fc.getFileSelectionMode() == fc.FILES_ONLY
                || !fsv.isFileSystem(file))) {*/
                    
                    if (file.isDirectory()
                    && fc.isTraversable(file)
                    && fc.getFileSelectionMode() == fc.FILES_ONLY
                    ) {
                        setDirectorySelected(true);
                        setDirectory(file);
                        fc.setSelectedFile(null);
                    } else {
                        setDirectorySelected(false);
                        if (file != null) {
                            fc.setCurrentDirectory(file.getParentFile());
                            fc.setSelectedFile(file);
                        }
                    }
                }
            }
        }
    }
    
    protected JButton getApproveButton(JFileChooser fc) {
        return approveButton;
    }
    
    public Action getApproveSelectionAction() {
        return approveSelectionAction;
    }
    
    protected class DoubleClickListener extends MouseAdapter {
        private TreePath clickedPath;
        
        /**
         * The JList used for representing the files is created by subclasses, but the
         * selection is monitored in this class.  The TransferHandler installed in the
         * JFileChooser is also installed in the file list as it is used as the actual
         * transfer source.  The list is updated on a mouse enter to reflect the current
         * data transfer state of the file chooser.
         */
        public void mouseEntered(MouseEvent e) {
            // XXX to do
            /*
            TransferHandler th1 = filechooser.getTransferHandler();
            TransferHandler th2 = list.getTransferHandler();
            if (th1 != th2) {
                list.setTransferHandler(th1);
            }
            if (filechooser.getDragEnabled() != list.getDragEnabled()) {
                list.setDragEnabled(filechooser.getDragEnabled());
            }*/
        }
        
        public void mouseClicked(MouseEvent e) {
            if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 1) {
                clickedPath = browser.getPathForLocation(e.getX(), e.getY());
            }
            if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2) {
                if(clickedPath != null) {
                    File f = ((FileSystemTreeModel.Node) clickedPath.getLastPathComponent()).getFile();
                    try {
                        // Strip trailing ".."
                        f = f.getCanonicalFile();
                    } catch (IOException ex) {
                        // That's ok, we'll use f as is
                    }
                    JFileChooser fc = getFileChooser();
                    if(getFileChooser().isTraversable(f)) {
                        //browser.clearSelection();
                        fc.setCurrentDirectory(f);
                        if (fc.isDirectorySelectionEnabled()) {
                            fc.approveSelection();
                        }
                    } else {
                        fc.approveSelection();
                    }
                }
            }
        }
    }
    
    protected MouseListener createDoubleClickListener(JFileChooser fc) {
        return new DoubleClickListener();
    }
    
    /**
     * Responds to an Open or Save request
     */
    protected class ApproveSelectionAction extends AbstractAction {
        protected ApproveSelectionAction() {
            super("approveSelection");
        }
        public void actionPerformed(ActionEvent e) {
            JFileChooser fc = getFileChooser();
            File selectedFile = null;
            File[] selectedFiles = null;
            
            String filename = getFileName();
            if (filename.equals("")) filename = null;
            
            if (fc.isMultiSelectionEnabled()) {
                TreePath[] selectedPaths = browser.getSelectionPaths();
                if (filename != null) {
                    selectedFiles = new File[] {
                        new File(((File) ((DefaultMutableTreeNode) selectedPaths[0].getLastPathComponent()).getUserObject()).getParent(), filename)
                    };
                } else {
                    selectedFiles = new File[selectedPaths.length];
                    for (int i=0; i < selectedPaths.length; i++) {
                        selectedFiles[i] = (File) ((DefaultMutableTreeNode) selectedPaths[i].getLastPathComponent()).getUserObject();
                    }
                }
                
            } else {
                selectedFile = (File) ((DefaultMutableTreeNode) browser.getSelectionPath().getLastPathComponent()).getUserObject();
                if (filename != null) {
                    selectedFile = new File(selectedFile.isDirectory() ? selectedFile : selectedFile.getParentFile(), filename);
                }
                if (fc.getFileSelectionMode() == JFileChooser.FILES_ONLY
                && selectedFile.isDirectory() && fc.isTraversable(selectedFile)) {
                    // Abort we cannot approve a directory
                    return;
                }
            }
            
            if (selectedFiles != null || selectedFile != null) {
                if (selectedFiles != null) {
                    fc.setSelectedFiles(selectedFiles);
                } else if (fc.isMultiSelectionEnabled()) {
                    fc.setSelectedFiles(new File[] { selectedFile });
                } else {
                    fc.setSelectedFile(selectedFile);
                }
                fc.approveSelection();
            } else {
                if (fc.isMultiSelectionEnabled()) {
                    fc.setSelectedFiles(null);
                } else {
                    fc.setSelectedFile(null);
                }
                fc.cancelSelection();
            }
            
            
            /*
            JFileChooser chooser = getFileChooser();
             
            String filename = getFileName();
            FileSystemView fs = chooser.getFileSystemView();
            File dir = chooser.getCurrentDirectory();
             
            if (filename != null) {
                // Remove whitespace from beginning and end of filename
                filename = filename.trim();
            }
             
            if (chooser.getFileSelectionMode() == JFileChooser.FILES_ONLY
            && (filename == null || filename.equals(""))) {
             
                // no file selected, multiple selection off,
                // therefore cancel the approve action
                //resetGlobFilter();
                System.out.println("ApproveAction ABORT no file name");
                return;
            }
             
            if (filename == null || filename.equals("")) {
                if (chooser.getFileSelectionMode() == JFileChooser.FILES_ONLY) {
             
             
                // no file selected, multiple selection off,
                // therefore cancel the approve action
                //resetGlobFilter();
                System.out.println("ApproveAction ABORT no file name");
                return;
                } else {
                    filename = browser.getSelectionPath().getLastPathComponent().toString();
                }
            }
             
             
            File selectedFile = null;
            File[] selectedFiles = null;
             
            if (filename != null && !filename.equals("")) {
                if (chooser.isMultiSelectionEnabled() && filename.startsWith("\"")) {
                    ArrayList fList = new ArrayList();
             
                    filename = filename.substring(1);
                    if (filename.endsWith("\"")) {
                        filename = filename.substring(0, filename.length()-1);
                    }
                    File[] children = null;
                    int childIndex = 0;
                    do {
                        String str;
                        int i = filename.indexOf("\" \"");
                        if (i > 0) {
                            str = filename.substring(0, i);
                            filename = filename.substring(i+3);
                        } else {
                            str = filename;
                            filename = "";
                        }
                        File file = fs.createFileObject(str);
                        if (!file.isAbsolute()) {
                            if (children == null) {
                                children = fs.getFiles(dir, false);
                                Arrays.sort(children);
                            }
                            for (int k = 0; k < children.length; k++) {
                                int l = (childIndex + k) % children.length;
                                if (children[l].getName().equals(str)) {
                                    file = children[l];
                                    childIndex = l + 1;
                                    break;
                                }
                            }
                        }
                        fList.add(file);
                    } while (filename.length() > 0);
                    if (fList.size() > 0) {
                        selectedFiles = (File[])fList.toArray(new File[fList.size()]);
                    }
                    //resetGlobFilter();
                } else {
                    selectedFile = fs.createFileObject(filename);
                    if(!selectedFile.isAbsolute()) {
                        if (chooser.getSelectedFile() != null && chooser.getSelectedFile().isDirectory()) {
                            selectedFile = fs.getChild(chooser.getSelectedFile(), filename);
                        } else {
                            selectedFile = fs.getChild(dir, filename);
                        }
                    }
             
                    // Check for directory change action
                    boolean isDir = (selectedFile != null && selectedFile.isDirectory());
                    boolean isTrav = (selectedFile != null && chooser.isTraversable(selectedFile));
                    boolean isDirSelEnabled = chooser.isDirectorySelectionEnabled();
                    boolean isFileSelEnabled = chooser.isFileSelectionEnabled();
             
                    if (isDir && isTrav && !isDirSelEnabled) {
                        chooser.setCurrentDirectory(selectedFile);
                        return;
                    } else if ((isDir || !isFileSelEnabled)
                    && (!isDir || !isDirSelEnabled)
                    && (!isDirSelEnabled || selectedFile.exists())) {
                        selectedFile = null;
                    }
                }
            }
            if (selectedFiles != null || selectedFile != null) {
                if (selectedFiles != null) {
                    chooser.setSelectedFiles(selectedFiles);
                } else if (chooser.isMultiSelectionEnabled()) {
                    chooser.setSelectedFiles(new File[] { selectedFile });
                } else {
                    chooser.setSelectedFile(selectedFile);
                }
                chooser.approveSelection();
            } else {
                if (chooser.isMultiSelectionEnabled()) {
                    chooser.setSelectedFiles(null);
                } else {
                    chooser.setSelectedFile(null);
                }
                chooser.cancelSelection();
            }*/
        }
    }
    
    // *****************************
    // ***** Directory Actions *****
    // *****************************
    
    public Action getNewFolderAction() {
        return newFolderAction;
    }
    /**
     * Creates a new folder.
     */
    protected class NewFolderAction extends AbstractAction {
        protected NewFolderAction() {
            super("New Folder");
        }
        
        private String showNewFolderDialog() {
            JOptionPane optionPane = new JOptionPane(
            newFolderDialogPrompt,
            JOptionPane.PLAIN_MESSAGE,
            JOptionPane.OK_CANCEL_OPTION
            );
            optionPane.setWantsInput(true);
            optionPane.setInitialSelectionValue(newFolderDefaultName);
            JDialog dialog = optionPane.createDialog(getFileChooser(), newFolderTitleText);
            dialog.show();
            dialog.dispose();
            
            return (optionPane.getInputValue() == JOptionPane.UNINITIALIZED_VALUE) ? null : (String) optionPane.getInputValue();
        }
        
        public void actionPerformed(ActionEvent actionevent) {
            JFileChooser fc = getFileChooser();
            String newFolderName = showNewFolderDialog();
            /*
    newFolderErrorText,
    newFolderExistsErrorText,
             */
            if (newFolderName != null) {
                
                File newFolder;
                if (fc.getSelectedFile() != null && fc.getSelectedFile().isDirectory()) {
                    newFolder = new File(fc.getSelectedFile(), newFolderName);
                } else {
                    newFolder = new File(fc.getCurrentDirectory(), newFolderName);
                }
                if (newFolder.exists()) {
                    JOptionPane.showMessageDialog(
                    fc,
                    newFolderExistsErrorText,
                    newFolderTitleText, JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                try {
                    newFolder.mkdir();
                    fc.rescanCurrentDirectory();
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(
                    fc,
                    newFolderErrorText,
                    newFolderTitleText, JOptionPane.ERROR_MESSAGE);
                }
            }
            /*
            try {
                newFolder = fc.getFileSystemView().createNewFolder(currentDirectory);
                if (fc.isMultiSelectionEnabled()) {
                    fc.setSelectedFiles(new File[] { newFolder });
                } else {
                    fc.setSelectedFile(newFolder);
                }
            } catch (IOException exc) {
                JOptionPane.showMessageDialog(
                    fc,
                    newFolderErrorText + newFolderErrorSeparator + exc,
                    newFolderErrorText, JOptionPane.ERROR_MESSAGE);
                return;
            }
             
            fc.rescanCurrentDirectory();
             */
        }
    }
    protected class SaveTextFocusListener implements FocusListener {
        public void focusGained(FocusEvent focusevent) {
            updateApproveButtonState(getFileChooser());
        }
        
        public void focusLost(FocusEvent focusevent) {
            /* empty */
        }
    }
    protected class SaveTextDocumentListener implements DocumentListener {
        public void insertUpdate(DocumentEvent documentevent) {
            textChanged();
        }
        
        public void removeUpdate(DocumentEvent documentevent) {
            textChanged();
        }
        
        public void changedUpdate(DocumentEvent documentevent) {
            /* empty */
        }
        
        void textChanged() {
            updateApproveButtonState(getFileChooser());
        }
    }
    // *******************************************************
    // ************ FileChooser UI PLAF methods **************
    // *******************************************************
    
    public FileView getFileView(JFileChooser fc) {
        return fileView;
    }
    public void clearIconCache() {
        fileView.clearIconCache();
    }
}
