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
 * <p>Description:  This application allows user to add/view/edit information
 * for a paleo site </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class PaleoSiteApp2 extends JFrame {

  private final static int WIDTH = 850;
  private final static int HEIGHT = 725;

  private final static String TITLE = "Cal Ref. Fault GUI";

  // start time estimate param
  private final static String START_TIME_PARAM_NAME="Start Time";
  private final static double TIME_ESTIMATE_MIN=0;
  private final static double TIME_ESTIMATE_MAX=Double.MAX_VALUE;
  private final static String TIME_ESTIMATE_UNITS="years";

  // end time estimate param
  private final static String END_TIME_PARAM_NAME="End Time";

  // various parameters
  private TimeGuiBean startTimeBean;
  private TimeGuiBean endTimeBean;
  private ViewPaleoSites viewPaleoSites;
  private SiteInfoForTimePeriod siteInfoForTimePeriod;

  // various parameter editors
  private BorderLayout borderLayout2 = new BorderLayout();
  private JSplitPane topSplitPane = new JSplitPane();
  private JPanel mainPanel = new JPanel();
  private JSplitPane mainSplitPane = new JSplitPane();
  private JSplitPane infoForTimeSpanSplitPane = new JSplitPane();
  private JSplitPane timespanSplitPane = new JSplitPane();
  private BorderLayout borderLayout1 = new BorderLayout();
  private JScrollPane statusScrollPane = new JScrollPane();
  private JTextArea statusTextArea = new JTextArea();

  /**
   * Constructor.
   * Gets all the available paleo sites from the database and displays
   * information about a user selected site
   */

  public PaleoSiteApp2() {
    try {
      setTitle(TITLE);
      jbInit();
      addSitesPanel(); // add the available sites from database for viewing
      addTimeSpanInfo(); // add start and end time estimates
      addSiteInfoForTimePeriod(); // add the info for the selected time period
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }


  public static void main(String[] args) {
    PaleoSiteApp2 paleoSiteApp = new PaleoSiteApp2();
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
    infoForTimeSpanSplitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
    timespanSplitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
    topSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
    statusTextArea.setEnabled(false);
    statusTextArea.setEditable(false);
    statusTextArea.setText("");
    this.getContentPane().add(topSplitPane, BorderLayout.CENTER);
    topSplitPane.add(mainPanel, JSplitPane.TOP);
    mainPanel.add(mainSplitPane, BorderLayout.CENTER);
    mainSplitPane.add(timespanSplitPane, JSplitPane.LEFT);
    mainSplitPane.add(infoForTimeSpanSplitPane, JSplitPane.RIGHT);
    topSplitPane.add(statusScrollPane, JSplitPane.BOTTOM);
    statusScrollPane.getViewport().add(statusTextArea, null);
    topSplitPane.setDividerLocation(625);
    mainSplitPane.setDividerLocation(425);
    infoForTimeSpanSplitPane.setDividerLocation(212);
    timespanSplitPane.setDividerLocation(212);
  }

  /**
   * Add the panel to display the available paleo sites in the database
   */
  private void addSitesPanel() {
    viewPaleoSites = new ViewPaleoSites();
    timespanSplitPane.add(viewPaleoSites, JSplitPane.LEFT);
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
    infoForTimeSpanSplitPane.add(siteInfoForTimePeriod, JSplitPane.RIGHT);
  }


  /**
   * Add the start and end time estimate parameters
   */
  private void addTimeSpanInfo() {

  }
}
