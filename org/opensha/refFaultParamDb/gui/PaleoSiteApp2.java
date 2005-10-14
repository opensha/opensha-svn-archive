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
import org.opensha.refFaultParamDb.gui.view.*;
import org.opensha.refFaultParamDb.gui.view.ViewSlipRate;
import java.awt.event.*;
import org.opensha.refFaultParamDb.vo.PaleoSite;
import org.opensha.refFaultParamDb.dao.db.CombinedEventsInfoDB_DAO;
import org.opensha.refFaultParamDb.dao.db.DB_AccessAPI;
import org.opensha.data.function.DiscretizedFunc;
import org.opensha.refFaultParamDb.vo.CombinedEventsInfo;
import org.opensha.exceptions.*;
import org.opensha.param.event.ParameterChangeListener;
import org.opensha.param.event.ParameterChangeEvent;

/**
 * <p>Title: PaleoSiteApp2.java </p>
 * <p>Description:  Gets all the available paleo sites from the database and
 * displays information about a user selected site </p>
 * <p>Description:  This application allows user to add/view/edit information
 * for a paleo site </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class PaleoSiteApp2 extends JFrame implements SiteSelectionAPI, ParameterChangeListener {

  private final static int WIDTH = 925;
  private final static int HEIGHT = 800;

  private final static String TITLE =
      "California Reference Geologic Fault Parameter (Paleo Site) GUI";
  private final static String TIMESPAN_PARAM_NAME = "TimeSpans";
  private final static String DATA_SPECIFIC_TO_TIME_INTERVALS =
      "Data currently in this database";
  private final static String SLIP_DISP_NUMEVENTS_TITLE = "Slip Rate, Displacement and Num Events";
  private final static String EVENTS_SEQUENCES_TITLE = "Events & Sequences";

  // various parameters
  private ViewSiteCharacteristics viewPaleoSites;
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
  private JSplitPane eventSequencesSplitPane = new JSplitPane();
  private BorderLayout borderLayout1 = new BorderLayout();
  private JScrollPane statusScrollPane = new JScrollPane();
  private JTextArea statusTextArea = new JTextArea();
  private JTabbedPane tabbedPane = new JTabbedPane();

  // panel to display the start time/end time and comments
  private ViewTimeSpan timeSpanPanel = new ViewTimeSpan();
  private LabeledBoxPanel availableTimeSpansPanel;
  private GridBagLayout gridBagLayout = new GridBagLayout();

  // panels for viewing slip rate, displacement and num events
   private ViewSlipRate slipRatePanel = new ViewSlipRate();
   private ViewCumDisplacement displacementPanel = new ViewCumDisplacement();
   private ViewNumEvents numEventsPanel= new ViewNumEvents() ;
   private ViewIndividualEvent individualEventPanel = new ViewIndividualEvent();
   private ViewSequences sequencesPanel = new ViewSequences();

   private ArrayList combinedEventsInfoList;
   private PaleoSite paleoSite;
   private CombinedEventsInfoDB_DAO combinedEventsInfoDAO = new CombinedEventsInfoDB_DAO(DB_AccessAPI.dbConnection);

   private final static String NOT_AVAILABLE = "Not Available";

  /**
   * Constructor.
   * Gets all the available paleo sites from the database and displays
   * information about a user selected site
   */
  public PaleoSiteApp2() {
    try {
      setTitle(TITLE);
      jbInit();
      addTimeSpansPanel();
      addSitesPanel(); // add the available sites from database for viewing
      pack();
      setSize(WIDTH, HEIGHT);
      this.setLocationRelativeTo(null);
      show();
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Add the available time spans for this site
   */
  private void addTimeSpansPanel() {
    availableTimeSpansPanel = new LabeledBoxPanel(this.gridBagLayout);
    availableTimeSpansPanel.setTitle(DATA_SPECIFIC_TO_TIME_INTERVALS);
    timeSpanSelectionSplitPane.add(availableTimeSpansPanel, JSplitPane.TOP);
}

  private void makeTimeSpanParamAndEditor() throws ConstraintException {
    // remove the editor if it already exists
    if(timeSpanParamEditor!=null) availableTimeSpansPanel.remove(timeSpanParamEditor);
    // get all the start and end times associated with this site
    ArrayList timeSpans = getAllTimeSpans();
    timeSpanParam = new StringParameter(TIMESPAN_PARAM_NAME, timeSpans,
                                        (String) timeSpans.get(0));
    timeSpanParam.addParameterChangeListener(this);
    timeSpanParamEditor = new ConstrainedStringParameterEditor(timeSpanParam);
    availableTimeSpansPanel.add(timeSpanParamEditor,new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0
      ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 0, 0, 0, 0 ), 0, 0 ) );
    viewInfoBasedOnSelectedTimeSpan();
  }

  /**
   * Get all the timespans from the database.
   *
   *
   * @return
   */
  private ArrayList getAllTimeSpans() {
    ArrayList timeSpansList = new ArrayList();;
    if (isTestSite()) {
      timeSpansList.add("TimeSpan 1");
      timeSpansList.add("TimeSpan 2");
    } else if(!isValidSiteAndInfoAvailable()) {
          // this is a valid site but info available for this
      timeSpansList.add(this.NOT_AVAILABLE);
    } else {
      for(int i=0; i<combinedEventsInfoList.size(); ++i) {
        // valid site and info available for the site
        CombinedEventsInfo combinedEventsInfo = (CombinedEventsInfo)combinedEventsInfoList.get(i);
        ArrayList shortcitationList  = combinedEventsInfo.getShortCitationList();

        timeSpansList.add((i+1)+". "+"(Reference="+
                          getReferencesAsString(shortcitationList)+")(Start Time="+
                          getTimeString(combinedEventsInfo.getStartTime())+") "+
                          "(End Time="+getTimeString(combinedEventsInfo.getEndTime())+")");
      }
    }
    return timeSpansList;
  }

  /**
   * convert reference list to string of references
   * @param references
   * @return
   */
  private String getReferencesAsString(ArrayList references) {
    String str = "";
    for(int i=0; i<references.size(); ++i)
      str+=references.get(i)+";";
    return str;
  }


  /**
   * whether the current site selected by user is a test site
   * @return
   */
  private boolean isTestSite() {
    return (combinedEventsInfoList == null &&  // if it is test site
        (paleoSite==null || paleoSite.getSiteName().equalsIgnoreCase(ViewSiteCharacteristics.TEST_SITE)));
  }

  private boolean isValidSiteAndInfoAvailable() {
    return (combinedEventsInfoList != null && combinedEventsInfoList.size()>0);
  }


  public void parameterChange(ParameterChangeEvent event) {
    String paramName = event.getParameterName();
    if(paramName.equalsIgnoreCase(TIMESPAN_PARAM_NAME)) viewInfoBasedOnSelectedTimeSpan();
  }

  /**
   * View information or the selected time span
   */
  private void viewInfoBasedOnSelectedTimeSpan() {
    ArrayList allowedStrings = timeSpanParam.getAllowedStrings();
    String timeSpan = (String)timeSpanParam.getValue();
    int index = allowedStrings.indexOf(timeSpan);
    CombinedEventsInfo combinedEventsInfo ;
    if(this.isValidSiteAndInfoAvailable())
       combinedEventsInfo = (CombinedEventsInfo)this.combinedEventsInfoList.get(index);
    else combinedEventsInfo = null;
    viewSlipRateForTimePeriod(combinedEventsInfo);
    viewNumEventsForTimePeriod(combinedEventsInfo);
    viewDisplacementForTimePeriod(combinedEventsInfo);
    viewTimeSpanInfo(combinedEventsInfo);
  }


  /**
   * Get the timespan as a string value which can be displayed in the StringParameter
   * @param startTime
   * @param endTime
   * @return
   */
  private String getTimeString(TimeAPI time) {
    String timeString="";
    if(time instanceof ExactTime) { // if it is exact time
      ExactTime exactTime = (ExactTime)time;
      timeString+="Exact Time:"+exactTime.getMonth()+"/"+exactTime.getDay()+"/"+
          exactTime.getYear()+exactTime.getEra()+" "+exactTime.getHour()+":"+exactTime.getMinute()+":"+
          exactTime.getSecond();
    } else if(time instanceof TimeEstimate) { // if it is time estimate
      TimeEstimate timeEstimate = (TimeEstimate) time;
      Estimate estimate = timeEstimate.getEstimate();
      if (timeEstimate.isKaSelected()) // if user entered ka values
        timeString += "Time Estimate:Units=ka,Zero Year=" +
            timeEstimate.getZeroYear()+":";
      else  timeString += "Time Estimate:Units=Calendar years"+":";
      if (estimate instanceof NormalEstimate) // for normal estimate
        timeString+=estimate.getName()+":Mean="+estimate.getMean()+","+
            "StdDev="+estimate.getStdDev();
      else if(estimate instanceof LogNormalEstimate)  // if estimate is of log normal type
        timeString+=estimate.getName()+":Linear Median="+
            ((LogNormalEstimate)estimate).getLinearMedian()+","+
            "StdDev="+estimate.getStdDev();
      else if (estimate instanceof DiscretizedFuncEstimate) {
        DiscretizedFunc func = ( (DiscretizedFuncEstimate) estimate).getValues();
        timeString += estimate.getName() + ":";
        for (int i = 0; i < func.getNum(); ++i)
          timeString += func.getX(i) + ",";
      }
      else if (estimate instanceof FractileListEstimate) {
        DiscretizedFunc func = ((FractileListEstimate)estimate).getValues();
        timeString +=  estimate.getName()+":";
        for(int i=0; i<func.getNum(); ++i)
          timeString+=  func.getX(i)+",";
      }
    }
    return timeString;
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
    this.eventSequencesSplitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
    topSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
    slipDisplacementSplitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
    timeSpanSelectionSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
    statusTextArea.setEnabled(false);
    statusTextArea.setEditable(false);
    statusTextArea.setText("");
    this.setDefaultCloseOperation(EXIT_ON_CLOSE);
    this.getContentPane().add(topSplitPane, BorderLayout.CENTER);
    topSplitPane.add(mainPanel, JSplitPane.TOP);
    mainPanel.add(mainSplitPane, BorderLayout.CENTER);
    //mainSplitPane.add(timespanSplitPane, JSplitPane.LEFT);
    tabbedPane.add(SLIP_DISP_NUMEVENTS_TITLE, timeSpanSelectionSplitPane);
    tabbedPane.add(EVENTS_SEQUENCES_TITLE, this.eventSequencesSplitPane);
    mainSplitPane.add(tabbedPane, JSplitPane.RIGHT);
    timeSpanSelectionSplitPane.add(timespanSplitPane, JSplitPane.BOTTOM);
    timespanSplitPane.add(infoForTimeSpanSplitPane, JSplitPane.RIGHT);
    //mainSplitPane.add(infoForTimeSpanSplitPane, JSplitPane.RIGHT);
    topSplitPane.add(statusScrollPane, JSplitPane.BOTTOM);
    infoForTimeSpanSplitPane.add(slipDisplacementSplitPane, JSplitPane.LEFT);
    statusScrollPane.getViewport().add(statusTextArea, null);
    slipDisplacementSplitPane.add(slipRatePanel, JSplitPane.LEFT);
    infoForTimeSpanSplitPane.add(numEventsPanel, JSplitPane.RIGHT);
    timespanSplitPane.add(timeSpanPanel, JSplitPane.LEFT);
    slipDisplacementSplitPane.add(displacementPanel, JSplitPane.RIGHT);
    eventSequencesSplitPane.add(this.individualEventPanel, JSplitPane.LEFT);
    eventSequencesSplitPane.add(this.sequencesPanel, JSplitPane.RIGHT);
    eventSequencesSplitPane.setDividerLocation(375);
    slipDisplacementSplitPane.setDividerLocation(170);
    topSplitPane.setDividerLocation(625);
    mainSplitPane.setDividerLocation(170);
    timeSpanSelectionSplitPane.setDividerLocation(75);
    infoForTimeSpanSplitPane.setDividerLocation(345);
    timespanSplitPane.setDividerLocation(170);
  }

  /**
   * Add the panel to display the available paleo sites in the database
   */
  private void addSitesPanel() {
    viewPaleoSites = new ViewSiteCharacteristics(this);
    mainSplitPane.add(viewPaleoSites, JSplitPane.LEFT);
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
  private void viewSlipRateForTimePeriod(CombinedEventsInfo combinedEventsInfo) {
    if(this.isTestSite()) {
      // FAKE DATA FOR TEST SITE
      // Slip Rate Estimate
      LogNormalEstimate slipRateEstimate = new LogNormalEstimate(1.5, 0.25);
      // Aseismic slip rate estimate
      NormalEstimate aSiemsicSlipEstimate = new NormalEstimate(0.7, 0.5);
      // comments
      String comments = "Pertinent comments will be displayed here";
      slipRatePanel.setInfo(slipRateEstimate, aSiemsicSlipEstimate, comments);
    } else if(this.isValidSiteAndInfoAvailable() &&
              combinedEventsInfo.getSlipRateEstimate()!=null)  { // information available FOR THIS SITE
        this.slipRatePanel.setInfo(combinedEventsInfo.getSlipRateEstimate().getEstimate(),
                                   combinedEventsInfo.getASeismicSlipFactorEstimateForSlip().getEstimate(),
                                   combinedEventsInfo.getSlipRateComments());
    } else { // valid site but no info available
      slipRatePanel.setInfo(null, null, null);
    }

  }


  /**
   * Display the displacement info for the selected time period
   */
  private void viewDisplacementForTimePeriod(CombinedEventsInfo combinedEventsInfo) {
    if(this.isTestSite()) {
     // FAKE DATA FOR TEST SITE
     // Slip Rate Estimate
     LogNormalEstimate diplacementEstimate = new LogNormalEstimate(1.5, 0.25);
     // Aseismic slip rate estimate
     NormalEstimate aSiemsicSlipEstimate = new NormalEstimate(0.7, 0.5);
     // comments
     String comments = "Pertinent comments will be displayed here";
     displacementPanel.setInfo(diplacementEstimate, aSiemsicSlipEstimate, comments);
   } else if(this.isValidSiteAndInfoAvailable() &&
             combinedEventsInfo.getDisplacementEstimate()!=null)  { // information available FOR THIS SITE
       this.displacementPanel.setInfo(combinedEventsInfo.getDisplacementEstimate().getEstimate(),
                                  combinedEventsInfo.getASeismicSlipFactorEstimateForDisp().getEstimate(),
                                  combinedEventsInfo.getDisplacementComments());
   } else { // valid site but no info available
     displacementPanel.setInfo(null, null, null);
   }

  }

  /**
   * display the Num events info for the selected time period
   */
  private void viewNumEventsForTimePeriod(CombinedEventsInfo combinedEventsInfo) {
    if(isTestSite()) {
      // Num Events Estimate
      // FAKE DATA FOR TEST SITE
      ArbitrarilyDiscretizedFunc func = new ArbitrarilyDiscretizedFunc();
      func.set(4.0, 0.2);
      func.set(5.0, 0.3);
      func.set(6.0, 0.1);
      func.set(7.0, 0.4);
      func.setXAxisName("# Events");
      func.setYAxisName("Prob this is correct #");
      IntegerEstimate numEventsEstimate = new IntegerEstimate(func, false);
      String comments = "Pertinent comments will be displayed here";
      this.numEventsPanel.setInfo(numEventsEstimate, comments);
    }else if(this.isValidSiteAndInfoAvailable() && combinedEventsInfo.getNumEventsEstimate()!=null) {
        numEventsPanel.setInfo((IntegerEstimate)combinedEventsInfo.getNumEventsEstimate().getEstimate(),
                               combinedEventsInfo.getNumEventsComments());
    }
    else { // information not available yet
      this.numEventsPanel.setInfo(null, null);
    }
  }

  /**
  * Whenever a user selects a site, this function is called in the listener class
  * @param siteName
  */
  public void siteSelected(PaleoSite paleoSite) {
    this.paleoSite = paleoSite;
    String siteName;
    if(paleoSite==null) { // for test site
      siteName = ViewSiteCharacteristics.TEST_SITE;
      combinedEventsInfoList = null;
    }
    else { // for actual sites from database
      siteName = paleoSite.getSiteName();
      this.combinedEventsInfoList = combinedEventsInfoDAO.getCombinedEventsInfoList(paleoSite.getSiteId());
    }
    makeTimeSpanParamAndEditor(); // get a list of all the timespans for which data is available for this site
    this.individualEventPanel.setSite(paleoSite); // view the events for this site
    this.sequencesPanel.setSite(paleoSite); // view the sequences for this site
  }


  /**
   * Add the start and end time estimate parameters
   */
  private void viewTimeSpanInfo(CombinedEventsInfo combinedEventsInfo) {
    if (isTestSite()) {
      // FAKE DATA FOR TEST SITE
      ExactTime endTime = new ExactTime(1857, 1, 15, 10, 56, 21, TimeAPI.AD);
      TimeEstimate startTime = new TimeEstimate();
      startTime.setForKaUnits(new NormalEstimate(1000, 50), 1950);
      String comments = "Summary of Dating techniques and dated features ";
      ArrayList references = new ArrayList();
      references.add("Ref 4");
      references.add("Ref 1");
      // timeSpan panel which will contain start time and end time
      this.timeSpanPanel.setTimeSpan(startTime, endTime, comments, references,null, null);
    } else if(this.isValidSiteAndInfoAvailable()){
      timeSpanPanel.setTimeSpan(combinedEventsInfo.getStartTime(),
                                combinedEventsInfo.getEndTime(),
                                combinedEventsInfo.getDatedFeatureComments(),
                                combinedEventsInfo.getShortCitationList(),
                                combinedEventsInfo.getEntryDate(),
                                combinedEventsInfo.getContributorName());
    }
    else {
      this.timeSpanPanel.setTimeSpan(null, null, null, null,null,null);
    }
  }


}

