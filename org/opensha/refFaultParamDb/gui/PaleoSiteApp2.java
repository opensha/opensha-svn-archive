package org.opensha.refFaultParamDb.gui;

import javax.swing.*;
import java.awt.*;
import org.opensha.param.estimate.EstimateParameter;
import org.opensha.param.editor.estimate.ConstrainedEstimateParameterEditor;
import org.opensha.param.editor.*;
import java.util.ArrayList;
import org.opensha.param.estimate.EstimateConstraint;
import ch.randelshofer.quaqua.QuaquaManager;
import org.opensha.gui.LabeledBoxPanel;
import org.opensha.refFaultParamDb.data.*;
import org.opensha.data.estimate.*;

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
  private final static String SLIP_RATE_TITLE = "Slip Rate";
  private final static String DISPLACEMENT_TITLE = "Displacement";
  private final static String NUM_EVENTS_TITLE = "Number of Events";

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
  private JSplitPane slipDisplacementSplitPane = new JSplitPane();
  private BorderLayout borderLayout1 = new BorderLayout();
  private JScrollPane statusScrollPane = new JScrollPane();
  private JTextArea statusTextArea = new JTextArea();
  // panel to display the start time/end time and comments
  private LabeledBoxPanel timeSpanPanel;
  private LabeledBoxPanel slipRatePanel;
  private LabeledBoxPanel displacementPanel;
  private LabeledBoxPanel numEventsPanel;


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
      viewTimeSpanInfo(); // add start and end time estimates
      viewSlipRateForTimePeriod(); // add the slip rate for the selected time period
      viewDisplacementForTimePeriod(); // add displacement for the time period
      viewNumEventsForTimePeriod(); // add num events info for the time period
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
    slipDisplacementSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
    statusTextArea.setEnabled(false);
    statusTextArea.setEditable(false);
    statusTextArea.setText("");
    this.getContentPane().add(topSplitPane, BorderLayout.CENTER);
    topSplitPane.add(mainPanel, JSplitPane.TOP);
    mainPanel.add(mainSplitPane, BorderLayout.CENTER);
    mainSplitPane.add(timespanSplitPane, JSplitPane.LEFT);
    mainSplitPane.add(infoForTimeSpanSplitPane, JSplitPane.RIGHT);
    topSplitPane.add(statusScrollPane, JSplitPane.BOTTOM);
    infoForTimeSpanSplitPane.add(slipDisplacementSplitPane, JSplitPane.LEFT);
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
   * display the slip Rate info for the selected time period
   */
  private void viewSlipRateForTimePeriod() {
    this.slipRatePanel = new LabeledBoxPanel();
    slipRatePanel.setTitle(this.SLIP_RATE_TITLE);
    LogNormalEstimate slipRateEstimate = new LogNormalEstimate(1, 0.25);
    NormalEstimate asiesmicSlipFactorEstimate = new NormalEstimate(0.5, 0.05);
  }

  /**
   * Display the displacement info for the selected time period
   */
  private void viewDisplacementForTimePeriod() {

  }

  /**
   * display the Num events info for the selected time period
   */
  private void viewNumEventsForTimePeriod() {

  }


  /**
   * Add the start and end time estimate parameters
   */
  private void viewTimeSpanInfo() {
    ExactTime startTime = new ExactTime(246, 1, 15, 10, 56, 21, TimeAPI.BC);
    TimeEstimate endTime =  new TimeEstimate();
    endTime.setForKaUnits(new NormalEstimate(1000, 50), 1950);
    String comments = "Dating features comments and techniques will go here";
    // timeSpan panel which will conatin start time and end time
    timeSpanPanel = new ViewTimeSpan(startTime, endTime, comments);
    timespanSplitPane.add(timeSpanPanel, JSplitPane.RIGHT);
  }
}
