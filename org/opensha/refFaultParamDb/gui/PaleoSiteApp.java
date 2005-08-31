package org.opensha.refFaultParamDb.gui;

import javax.swing.*;
import java.awt.*;
import org.opensha.param.estimate.EstimateParameter;
import org.opensha.param.editor.estimate.ConstrainedEstimateParameterEditor;
import org.opensha.param.editor.*;
import java.util.ArrayList;
import org.opensha.param.estimate.EstimateConstraint;
import ch.randelshofer.quaqua.QuaquaManager;

/**
 * <p>Title: PaleoSiteApp.java </p>
 * <p>Description:  Gets all the available paleo sites from the database and
 * displays information about a user selected site </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class PaleoSiteApp extends JFrame {

  private final static int WIDTH = 850;
  private final static int HEIGHT = 725;

  private final static String TITLE = "Cal Ref. Fault GUI";

  // various parameters
  private ViewPaleoSites viewPaleoSites;
  private SiteInfoForTimePeriod siteInfoForTimePeriod;

  // various parameter editors
  private BorderLayout borderLayout2 = new BorderLayout();
  private JSplitPane topSplitPane = new JSplitPane();
  private JPanel mainPanel = new JPanel();
  private JSplitPane mainSplitPane = new JSplitPane();
  private JSplitPane summarySplitPane = new JSplitPane();
  private JSplitPane timespanSplitPane = new JSplitPane();
  private BorderLayout borderLayout1 = new BorderLayout();
  private JScrollPane statusScrollPane = new JScrollPane();
  private JTextArea statusTextArea = new JTextArea();

  /**
   * Constructor.
   * Gets all the available paleo sites from the database and displays
   * information about a user selected site
   */
  public PaleoSiteApp() {
    try {
      setTitle(TITLE);
      jbInit();
      addSitesPanel(); // add the available sites from database for viewing
      addTimeInfo(); // add start and end time estimates
      addSiteInfoForTimePeriod(); // add the info for the selected time period
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void addTimeInfo() {

  }

  public static void main(String[] args) {
    PaleoSiteApp paleoSiteApp = new PaleoSiteApp();
    paleoSiteApp.pack();
    paleoSiteApp.setSize(WIDTH, HEIGHT);
    paleoSiteApp.show();
  }

  /**
   * Add all the components to the GUI
   * @throws java.lang.Exception
   */
  private void jbInit() throws Exception {
    this.getContentPane().setLayout(borderLayout2);
    mainPanel.setLayout(borderLayout1);
    mainSplitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
    summarySplitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
    timespanSplitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
    topSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
    statusTextArea.setEnabled(false);
    statusTextArea.setEditable(false);
    statusTextArea.setText("");
    this.getContentPane().add(topSplitPane, BorderLayout.CENTER);
    topSplitPane.add(mainPanel, JSplitPane.TOP);
    mainPanel.add(mainSplitPane, BorderLayout.CENTER);
    mainSplitPane.add(summarySplitPane, JSplitPane.LEFT);
    mainSplitPane.add(timespanSplitPane, JSplitPane.RIGHT);
    topSplitPane.add(statusScrollPane, JSplitPane.BOTTOM);
    statusScrollPane.getViewport().add(statusTextArea, null);
    topSplitPane.setDividerLocation(625);
    mainSplitPane.setDividerLocation(425);
    summarySplitPane.setDividerLocation(212);
    timespanSplitPane.setDividerLocation(212);
  }

  /**
   * Add the panel to display the available paleo sites in the database
   */
  private void addSitesPanel() {
    viewPaleoSites = new ViewPaleoSites();
    summarySplitPane.add(viewPaleoSites, JSplitPane.LEFT);
  }

  //static initializer for setting look & feel
  static {
    String osName = System.getProperty("os.name");
    try {
      if(osName.startsWith("Mac OS"))
        UIManager.setLookAndFeel(QuaquaManager.getLookAndFeelClassName());
      else
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    }
    catch(Exception e) {
    }
  }

  /**
   * display the info for the selected time period
   */
  private void addSiteInfoForTimePeriod() {
    SiteInfoForTimePeriod siteInfoForTimePeriod = new SiteInfoForTimePeriod();
    timespanSplitPane.add(siteInfoForTimePeriod, JSplitPane.RIGHT);
  }


}