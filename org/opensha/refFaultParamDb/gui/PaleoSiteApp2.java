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
import javax.swing.border.*;
import org.opensha.refFaultParamDb.gui.infotools.InfoLabel;
import org.opensha.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.param.StringParameter;
import org.opensha.gui.TitledBorderPanel;

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


  private final static String TITLE = "California Reference Geologic Fault Parameter (Paleo Site) GUI";
  private final static String SLIP_RATE_TITLE = "Slip Rate";
  private final static String DISPLACEMENT_TITLE = "Displacement";
  private final static String NUM_EVENTS_TITLE = "Number of Events";
  private final static String TIMESPAN_PARAM_NAME="TimeSpans";
  private final static String DATA_SPECIFIC_TO_TIME_INTERVALS = "Data Specific to Time Intervals";

  // various parameters
  private TimeGuiBean startTimeBean;
  private TimeGuiBean endTimeBean;
  private ViewPaleoSites viewPaleoSites;
  private SiteInfoForTimePeriod siteInfoForTimePeriod;
  private StringParameter timeSpanParam;
  private ConstrainedStringParameterEditor timeSpanParamEditor;

  // various parameter editors
  private BorderLayout borderLayout2 = new BorderLayout();
  private JSplitPane topSplitPane = new JSplitPane();
  private JPanel mainPanel = new JPanel();
  private JSplitPane mainSplitPane = new JSplitPane();
  private JSplitPane infoForTimeSpanSplitPane = new JSplitPane();
  private JSplitPane timespanSplitPane = new JSplitPane();
  private JSplitPane timeSpanSelectionSplitPane = new JSplitPane();
  private JSplitPane slipDisplacementSplitPane = new JSplitPane();
  private BorderLayout borderLayout1 = new BorderLayout();
  private JScrollPane statusScrollPane = new JScrollPane();
  private JTextArea statusTextArea = new JTextArea();

  // panel to display the start time/end time and comments
  private LabeledBoxPanel timeSpanPanel;
  private LabeledBoxPanel slipRatePanel;
  private LabeledBoxPanel displacementPanel;
  private LabeledBoxPanel numEventsPanel;
  private LabeledBoxPanel availableTimeSpansPanel;
  private GridBagLayout gridBagLayout = new GridBagLayout();

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
      addAvailableTimeSpans(); // add the available timespans for this site
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
   * Add the available time spans for this site
   */
  private void addAvailableTimeSpans() {
    availableTimeSpansPanel = new LabeledBoxPanel(this.gridBagLayout);
    availableTimeSpansPanel.setTitle(DATA_SPECIFIC_TO_TIME_INTERVALS);
    // get all the start times associated with this site
    ArrayList timeSpans = getAllTimeSpans();
    timeSpanParam = new StringParameter(TIMESPAN_PARAM_NAME, timeSpans,
                                        (String) timeSpans.get(0));
    timeSpanParamEditor = new ConstrainedStringParameterEditor(timeSpanParam);
    availableTimeSpansPanel.add(timeSpanParamEditor,new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0
      ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 0, 0, 0, 0 ), 0, 0 ) );
    timeSpanSelectionSplitPane.add(availableTimeSpansPanel, JSplitPane.TOP);
}

  /**
   * this is JUST A FAKE IMPLEMENTATION. IT SHOULD GET ALL START TIMES FROM
   * the DATABASE
   * @return
   */
  private ArrayList getAllTimeSpans() {
    ArrayList timeSpansList = new ArrayList();
    timeSpansList.add("TimeSpan 1");
    timeSpansList.add("TimeSpan 2");
    return timeSpansList;

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
    timeSpanSelectionSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
    statusTextArea.setEnabled(false);
    statusTextArea.setEditable(false);
    statusTextArea.setText("");
    this.getContentPane().add(topSplitPane, BorderLayout.CENTER);
    topSplitPane.add(mainPanel, JSplitPane.TOP);
    mainPanel.add(mainSplitPane, BorderLayout.CENTER);
    //mainSplitPane.add(timespanSplitPane, JSplitPane.LEFT);
    mainSplitPane.add(timeSpanSelectionSplitPane, JSplitPane.RIGHT);
    timeSpanSelectionSplitPane.add(timespanSplitPane, JSplitPane.BOTTOM);
    timespanSplitPane.add(infoForTimeSpanSplitPane, JSplitPane.RIGHT);
    //mainSplitPane.add(infoForTimeSpanSplitPane, JSplitPane.RIGHT);
    topSplitPane.add(statusScrollPane, JSplitPane.BOTTOM);
    infoForTimeSpanSplitPane.add(slipDisplacementSplitPane, JSplitPane.LEFT);
    statusScrollPane.getViewport().add(statusTextArea, null);
    topSplitPane.setDividerLocation(625);
    mainSplitPane.setDividerLocation(212);
    timeSpanSelectionSplitPane.setDividerLocation(75);
    infoForTimeSpanSplitPane.setDividerLocation(212);
    timespanSplitPane.setDividerLocation(212);
  }

  /**
   * Add the panel to display the available paleo sites in the database
   */
  private void addSitesPanel() {
    viewPaleoSites = new ViewPaleoSites();
    mainSplitPane.add(viewPaleoSites, JSplitPane.LEFT);
    //timespanSplitPane.add(viewPaleoSites, JSplitPane.LEFT);
  }

  //static initializer for setting look & feel
  static {
    String osName = System.getProperty("os.name");
    try {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    }
    catch(Exception e) {
    }
  }

  /**
   * display the slip Rate info for the selected time period
   */
  private void viewSlipRateForTimePeriod() throws Exception {

    this.slipRatePanel = new LabeledBoxPanel(this.gridBagLayout);
    slipRatePanel.setTitle(this.SLIP_RATE_TITLE);

    // Slip Rate Estimate
    LogNormalEstimate slipRateEstimate = new LogNormalEstimate(1.5, 0.25);
    JPanel slipRateEstimatePanel = getPanel(new InfoLabel(slipRateEstimate), "Slip Rate Estimate(mm/yr)");

    // Aseismic slip rate estimate
    NormalEstimate aSiemsicSlipEstimate = new NormalEstimate(0.7, 0.5);
    JPanel aseismicPanel = getPanel(new InfoLabel(aSiemsicSlipEstimate), "Aseismic Slip Factor(0-1, 1=all aseismic)");

    // comments
    String comments = "Perinent comments will be displayed here";
    StringParameter commentsParam = new StringParameter("Slip Rate Comments", comments);
    CommentsParameterEditor commentsPanel = new CommentsParameterEditor(commentsParam);
    commentsPanel.setEnabled(false);

    // references
    ArrayList references = new ArrayList();
    references.add("Ref 1");
    references.add("Ref 2");
    JPanel referencesPanel = getPanel(new InfoLabel(references), "References");

    // add the slip rate info the panel
    int yPos=0;
    slipRatePanel.add(slipRateEstimatePanel,new GridBagConstraints( 0, yPos++, 1, 1, 1.0, 1.0
        ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 0, 0, 0, 0 ), 0, 0 ) );
    slipRatePanel.add(aseismicPanel,new GridBagConstraints( 0, yPos++, 1, 1, 1.0, 1.0
        ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 0, 0, 0, 0 ), 0, 0 ) );
    slipRatePanel.add(commentsPanel,new GridBagConstraints( 0, yPos++, 1, 1, 1.0, 1.0
        ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 0, 0, 0, 0 ), 0, 0 ) );
    slipRatePanel.add(referencesPanel,new GridBagConstraints( 0, yPos++, 1, 1, 1.0, 1.0
        ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 0, 0, 0, 0 ), 0, 0 ) );
    slipDisplacementSplitPane.add(slipRatePanel, JSplitPane.TOP);

  }

  /**
   * Get Panel for an estimate
   * @param estimate
   * @param borderTitle
   * @return
   */
  private JPanel getPanel(InfoLabel infoLabel, String borderTitle) {
    JPanel panel = new TitledBorderPanel(borderTitle+":");
    panel.setLayout(this.gridBagLayout);
    panel.add(infoLabel,new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0
        ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 0, 0, 0, 0 ), 0, 0 ) );
    return panel;
  }


  /**
   * Display the displacement info for the selected time period
   */
  private void viewDisplacementForTimePeriod() {
    this.displacementPanel = new LabeledBoxPanel(this.gridBagLayout);
    displacementPanel.setTitle(this.DISPLACEMENT_TITLE);

    // comments
    String comments = "Displacement is implied when Slip Rate is provided";
    InfoLabel commentsLabel = new InfoLabel(comments);
    int yPos=0;
    displacementPanel.add(commentsLabel,new GridBagConstraints( 0, yPos++, 1, 1, 1.0, 1.0
        ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 0, 0, 0, 0 ), 0, 0 ) );
    slipDisplacementSplitPane.add(displacementPanel, JSplitPane.BOTTOM);
    slipDisplacementSplitPane.setDividerLocation(450);

  }

  /**
   * display the Num events info for the selected time period
   */
  private void viewNumEventsForTimePeriod() throws Exception {

    this.numEventsPanel = new LabeledBoxPanel(this.gridBagLayout);
    numEventsPanel.setTitle(this.NUM_EVENTS_TITLE);

    // Num Events Estimate
    ArbitrarilyDiscretizedFunc func = new ArbitrarilyDiscretizedFunc();
    func.set(4.0, 0.2);
    func.set(5.0, 0.3);
    func.set(6.0, 0.1);
    func.set(7.0, 0.4);
    IntegerEstimate numEventsEstimate = new IntegerEstimate(func, false);
    JPanel slipRateEstimatePanel = getPanel(new InfoLabel(numEventsEstimate), "Num Events Estimate");


    // comments
    String comments = "Pertinent comments will be displayed here";
    StringParameter commentsParam = new StringParameter("Num Events Comments", comments);
    CommentsParameterEditor commentsPanel = new CommentsParameterEditor(commentsParam);
    commentsPanel.setEnabled(false);


    // references
    ArrayList references = new ArrayList();
    references.add("Ref 5");
    references.add("Ref 7");
    JPanel referencesPanel = getPanel(new InfoLabel(references), "References");

    // add the slip rate info the panel
    int yPos=0;
    numEventsPanel.add(slipRateEstimatePanel,new GridBagConstraints( 0, yPos++, 1, 1, 1.0, 1.0
        ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 0, 0, 0, 0 ), 0, 0 ) );
    numEventsPanel.add(commentsPanel,new GridBagConstraints( 0, yPos++, 1, 1, 1.0, 1.0
        ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 0, 0, 0, 0 ), 0, 0 ) );
    numEventsPanel.add(referencesPanel,new GridBagConstraints( 0, yPos++, 1, 1, 1.0, 1.0
        ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 0, 0, 0, 0 ), 0, 0 ) );
    infoForTimeSpanSplitPane.add(numEventsPanel, JSplitPane.RIGHT);
  }

  /**
   * Add the start and end time estimate parameters
   */
  private void viewTimeSpanInfo() {
    ExactTime endTime = new ExactTime(1857, 1, 15, 10, 56, 21, TimeAPI.AD);
    TimeEstimate startTime =  new TimeEstimate();
    startTime.setForKaUnits(new NormalEstimate(1000, 50), 1950);
    String comments = "Summary of Dating techniques and dated features ";
    // timeSpan panel which will conatin start time and end time
    timeSpanPanel = new ViewTimeSpan(startTime, endTime, comments);
    timespanSplitPane.add(timeSpanPanel, JSplitPane.LEFT);
  }

}
