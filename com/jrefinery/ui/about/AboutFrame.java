/* ================================================================
 * JCommon : a general purpose, open source, class library for Java
 * ================================================================
 *
 * Project Info:  http://www.object-refinery.com/jcommon/index.html
 * Project Lead:  David Gilbert (david.gilbert@object-refinery.com);
 *
 * (C) Copyright 2000-2002, by Simba Management Limited and Contributors.
 *
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation;
 * either version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * library; if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307, USA.
 *
 * ---------------
 * AboutFrame.java
 * ---------------
 * (C) Copyright 2001, 2002, by Simba Management Limited.
 *
 * Original Author:  David Gilbert (for Simba Management Limited);
 * Contributor(s):   -;
 *
 * $Id$
 *
 * Changes (from 26-Nov-2001)
 * --------------------------
 * 26-Nov-2001 : Version 1, based on code from JFreeChart demo application (DG);
 * 27-Nov-2001 : Added getPreferredSize() method (DG);
 * 08-Feb-2002 : List of developers is now optional (DG);
 * 15-Mar-2002 : Modified to use a ResourceBundle for elements that require localisation (DG);
 * 25-Mar-2002 : Added new constructor (DG);
 *
 */

package com.jrefinery.ui.about;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.BorderFactory;
import javax.swing.border.Border;

/**
 * A frame that displays information about the demonstration application.
 */
public class AboutFrame extends JFrame {

    /** The preferred size for the frame. */
    public static final Dimension PREFERRED_SIZE = new Dimension(400, 300);

    /** The default border for the panels in the tabbed pane. */
    public static final Border STANDARD_BORDER = BorderFactory.createEmptyBorder(5, 5, 5, 5);

    /** Localised resources. */
    protected ResourceBundle resources;

    /** The application name. */
    protected String application;

    /** The application version. */
    protected String version;

    /** The copyright string. */
    protected String copyright;

    /** Other info about the application. */
    protected String info;

    /** A list of contributors. */
    protected List contributors;

    /** The licence. */
    protected String licence;

    /** A list of libraries. */
    protected List libraries;

    public AboutFrame(String title, ProjectInfo project) {

        this(title,
             project.getName(),
             "Version "+project.getVersion(),
             project.getInfo(),
             project.getCopyright(),
             project.getLicenceText(),
             project.getContributors(),
             project.getLibraries());

    }

    /**
     * Constructs an 'About' frame.
     *
     * @param title The frame title.
     * @param application The application name.
     * @param version The version.
     * @param info Other info.
     * @param copyright The copyright notice.
     * @param licence The licence.
     * @param contributors A list of developers/contributors.
     * @param libraries A list of libraries.
     */
    public AboutFrame(String title,
                      String application, String version, String info,
                      String copyright, String licence,
                      List contributors,
                      List libraries) {

        super(title);

        this.application = application;
        this.version = version;
        this.copyright = copyright;
        this.info = info;
        this.contributors = contributors;
        this.licence = licence;
        this.libraries = libraries;

        String baseName = "com.jrefinery.ui.about.resources.AboutResources";
        this.resources = ResourceBundle.getBundle(baseName);

        JPanel content = new JPanel(new BorderLayout());
        content.setBorder(STANDARD_BORDER);

        JTabbedPane tabs = this.createTabs();
        content.add(tabs);
        this.setContentPane(content);

        pack();

    }

    /**
     * Returns the preferred size for the about frame.
     */
    public Dimension getPreferredSize() {
        return PREFERRED_SIZE;
    }

    /**
     * Creates a tabbed pane containing an about panel and a system properties panel.
     */
    private JTabbedPane createTabs() {

        JTabbedPane tabs = new JTabbedPane();

        JPanel aboutPanel = createAboutPanel();
        aboutPanel.setBorder(AboutFrame.STANDARD_BORDER);
        String aboutTab = this.resources.getString("about-frame.tab.about");
        tabs.add(aboutTab, aboutPanel);

        JPanel systemPanel = new SystemPropertiesPanel();
        systemPanel.setBorder(AboutFrame.STANDARD_BORDER);
        String systemTab = this.resources.getString("about-frame.tab.system");
        tabs.add(systemTab, systemPanel);

        return tabs;

    }

    /**
     * Creates a panel showing information about the application, including the name, version,
     * copyright notice, URL for further information, and a list of contributors.
     */
    private JPanel createAboutPanel() {

        JPanel about = new JPanel(new BorderLayout());

        JPanel details = new AboutPanel(this.application, this.version, this.copyright, this.info);

        boolean includetabs = false;
        JTabbedPane tabs = new JTabbedPane();

        if (this.contributors!=null) {
            JPanel contributorsPanel = new ContributorsPanel(this.contributors);
            contributorsPanel.setBorder(AboutFrame.STANDARD_BORDER);
            String contributorsTab = this.resources.getString("about-frame.tab.contributors");
            tabs.add(contributorsTab, contributorsPanel);
            includetabs = true;
        }

        if (this.licence!=null) {
            JPanel licencePanel = createLicencePanel();
            licencePanel.setBorder(STANDARD_BORDER);
            String licenceTab = this.resources.getString("about-frame.tab.licence");
            tabs.add(licenceTab, licencePanel);
            includetabs = true;
        }

        if (this.libraries!=null) {
            JPanel librariesPanel = new LibraryPanel(this.libraries);
            librariesPanel.setBorder(AboutFrame.STANDARD_BORDER);
            String librariesTab = this.resources.getString("about-frame.tab.libraries");
            tabs.add(librariesTab, librariesPanel);
            includetabs = true;
        }

        about.add(details, BorderLayout.NORTH);
        if (includetabs) {
            about.add(tabs);
        }

        return about;

    }

    /**
     * Creates a panel showing the licence.
     */
    private JPanel createLicencePanel() {

        JPanel licence = new JPanel(new BorderLayout());
        JTextArea area = new JTextArea(this.licence);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setCaretPosition(0);
        area.setEditable(false);
        JScrollPane scroll = new JScrollPane(area);
        licence.add(new JScrollPane(area));
        return licence;

    }


}