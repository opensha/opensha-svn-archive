package org.opensha.refFaultParamDb.gui;

import javax.swing.*;
import org.opensha.param.editor.ParameterListEditor;
import org.opensha.param.*;
import org.opensha.param.estimate.*;
import java.util.ArrayList;
import java.awt.*;
import ch.randelshofer.quaqua.QuaquaManager;
import org.opensha.exceptions.*;
import org.opensha.exceptions.*;
import org.opensha.param.event.*;

/**
 * <p>Title: PaleoSiteApp.java </p>
 * <p>Description: This GUI is developed to fill the data in California
 * Reference Fault Paramter database</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class PaleoSiteApp extends JFrame implements ParameterChangeListener {
  private final static String TITLE_SITE_INFO="Site Info";
  private final static String SITE_PARAM_NAME = "Site Name";

  private final static String FAULT_PARAM_NAME="Associated with Fault";
  private final static String TYPES_OF_SITE_PARAM_NAME="Types of Site";
  private final static String REPRESENTATIVE_SITE_PARAM_NAME="How representative is this site";
  private final static String AVAILABLE_INFO_PARAM_NAME="I have info on";
  private final static String INDIVIDUAL_EVENTS_DATE_AVAILABLE_PARAM_NAME="I can provide dates for individual events";
  private final static String DATED_FEATURE_COMMENTS_PARAM_NAME="Description of Dated Features";
  private final static String REFERENCES_PARAM_NAME="References";

  // Site Location
  private final static String LOCATION_PARAM_NAME = "Site Location";
  private final static String LAT_PARAM_NAME="Site Latitude";
  private final static String LON_PARAM_NAME="Site Longitide";
  private final static String DEPTH_PARAM_NAME="Site Elevation";
  private final static Double DEFAULT_LAT_VAL=new Double(34.00);
  private final static Double DEFAULT_LON_VAL=new Double(-118.0);
  private final static Double DEFAULT_DEPTH_VAL=new Double(2.0);


  // SLIP RATE
  private final static String SLIP_RATE_PARAM_NAME="Slip Rate Estimate";
  private final static String SLIP_RATE_COMMENTS_PARAM_NAME="Slip Rate Comments";
  private final static String SLIP_RATE_UNITS = "mm/yr";
  private final static double SLIP_RATE_MIN = 0;
  private final static double SLIP_RATE_MAX = Double.POSITIVE_INFINITY;

  // ASEISMICE SLIP FACTOR
  private final static String ASEISMIC_SLIP_FACTOR_PARAM_NAME="Aseismic Slip Factor Estimate";
  private final static double ASEISMIC_SLIP_FACTOR_MIN=0;
  private final static double ASEISMIC_SLIP_FACTOR_MAX=1;

   // CUMULATIVE DISPLACEMENT
  private final static String CUMULATIVE_DISPLACEMENT_PARAM_NAME="Total Displacement at this Site in this Time Span Estimate";
  private final static String CUMULATIVE_DISPLACEMENT_COMMENTS_PARAM_NAME="Cumulative Displacement Comments";
  private final static String CUMULATIVE_DISPLACEMENT_UNITS = "mm/yr";
  private final static double CUMULATIVE_DISPLACEMENT_MIN = 0;
  private final static double CUMULATIVE_DISPLACEMENT_MAX = Double.POSITIVE_INFINITY;

  // Number of events parameter
  private final static String NUM_EVENTS_PARAM_NAME="Number of Events in This Time Span";
  private final static double NUM_EVENTS_MIN=0;
  private final static double NUM_EVENTS_MAX=Integer.MAX_VALUE;

  // various types of information that user can provide
  private final static String SLIP_RATE_INFO = "Slip Rate";
  private final static String CUMULATIVE_DISPLACEMENT_INFO = "Cumulative Displacement";
  private final static String EVENTS_INFO = "Events";
  private final static String SLIP_RATE_AND_EVENTS_INFO = "Slip Rate and Events";
  private final static String CUMULATIVE_DISPLACEMENT_AND_EVENTS_INFO = "Cumulative Displacement & Events";

  // start time estimate param
  private final static String START_TIME_ESTIMATE_PARAM_NAME="Start Time Estimate";
  private final static double TIME_ESTIMATE_MIN=0;
  private final static double TIME_ESTIMATE_MAX=Double.MAX_VALUE;
  private final static String TIME_ESTIMATE_UNITS="years";

  // end time estimate param
  private final static String END_TIME_ESTIMATE_PARAM_NAME="End Time Estimate";



  // Title of this window
  private final static String FRAME_TITLE="Cal. Ref. Fault Database Entry GUI";

  private StringParameter siteNameParam;
  private LocationParameter locationParameter;
  private StringParameter availableFaultsParam;
  private StringParameter studyTypesParam;
  private StringParameter siteRepresentationParam;
  private StringParameter availableInfoParam;
  private BooleanParameter eventDatesAvailableParam;
  private StringParameter datedFeatureCommentsParam;
  private StringParameter referencesParam;
  private EstimateParameter slipRateEstimateParam;
  private EstimateParameter aSeismicSlipFactorParam;
  private StringParameter slipRateCommentsParam;
  private EstimateParameter cumDisplacementParam;
  private StringParameter displacementCommentsParam;
  private EstimateParameter numEventsParam;
  private EstimateParameter startTimeEstimateParam;
  private EstimateParameter endTimeEstimateParam;

  private ParameterListEditor editor;
  private ParameterList paramList;
  private JPanel mainPanel = new JPanel();
  private JLabel headerLabel = new JLabel();
  private GridBagLayout gridBagLayout1 = new GridBagLayout();
  private BorderLayout borderLayout1 = new BorderLayout();


  public PaleoSiteApp() {
    initParamListAndEditor();
    try {
      jbInit();
      this.setTitle(FRAME_TITLE);
    }
    catch(Exception e) {
      e.printStackTrace();
    }
  }


  /**
   * Initialize the parameters for the GUI
   */
  private void initParamListAndEditor() {
    paramList = new ParameterList();
    // add common parameters which are needed irrespective of what information is
    // provided by the user.
    addCommonParameters();

    //add parameters for slip rate info
    addSlipRateInfoParameters();

    // add parameters for cumulative displacement
    addCumulativeDisplacementParameters();

    // add parameters for events
    addEventsParameters();

    editor = new ParameterListEditor(paramList);
    editor.setTitle(TITLE_SITE_INFO);
    setParameters((String)this.availableInfoParam.getValue());
  }

  /**
   * Add the common parameters needed irrespective of which ever information is
   * provided by user from following options:
   * "Slip Rate", "Cumulative Displacement", "Events", "Slip Rate and Events",
   * "Cumulative Displacement and Events"

   * @throws ParameterException
   * @throws ConstraintException
   */
  private void addCommonParameters() throws ParameterException,
      ConstraintException {
     ArrayList siteNamesList = getSiteNames();
     // available site names
     siteNameParam = new StringParameter(SITE_PARAM_NAME, siteNamesList, (String)siteNamesList.get(0));
     // Site Location(Lat/lon/depth)
     locationParameter = new LocationParameter(LOCATION_PARAM_NAME, LAT_PARAM_NAME,
                                               LON_PARAM_NAME, DEPTH_PARAM_NAME,
                                               DEFAULT_LAT_VAL,
                                               DEFAULT_LON_VAL, DEFAULT_DEPTH_VAL);

     // choose the fault with which this site is associated
     ArrayList faultNamesList = getFaultNames();
     availableFaultsParam = new StringParameter(FAULT_PARAM_NAME, faultNamesList,
                                                (String)faultNamesList.get(0));
     // available study types
     ArrayList studyTypes = getStudyTypes();
     studyTypesParam = new StringParameter(TYPES_OF_SITE_PARAM_NAME, studyTypes,
                                                (String)studyTypes.get(0));

     // how representative is this site?
     ArrayList siteRepresentations = getSiteRepresentations();
     siteRepresentationParam = new StringParameter(REPRESENTATIVE_SITE_PARAM_NAME, siteRepresentations,
                                                (String)siteRepresentations.get(0));
    // availablle info for this site
    ArrayList availableInfoList = getAvailableInfoList();
    availableInfoParam = new StringParameter(AVAILABLE_INFO_PARAM_NAME, availableInfoList,
                                           (String)availableInfoList.get(0));
    // I can/cannot provide dates for individual events
    eventDatesAvailableParam = new BooleanParameter(INDIVIDUAL_EVENTS_DATE_AVAILABLE_PARAM_NAME);

    // ADD START TIME ESTIMATE & END TIME ESTIMATE HERE
    ArrayList dateEstimatesList =  EstimateConstraint.createConstraintForDateEstimates();
    startTimeEstimateParam = new EstimateParameter(this.START_TIME_ESTIMATE_PARAM_NAME,
        this.TIME_ESTIMATE_UNITS, this.TIME_ESTIMATE_MIN, this.TIME_ESTIMATE_MAX,
        dateEstimatesList);
    endTimeEstimateParam = new EstimateParameter(this.END_TIME_ESTIMATE_PARAM_NAME,
        this.TIME_ESTIMATE_UNITS, this.TIME_ESTIMATE_MIN, this.TIME_ESTIMATE_MAX,
        dateEstimatesList);



    // dated feature comments
    datedFeatureCommentsParam = new StringParameter(this.DATED_FEATURE_COMMENTS_PARAM_NAME);

     // availablle info for this site
    ArrayList availableReferences = getAvailableReferences();
    referencesParam = new StringParameter(this.REFERENCES_PARAM_NAME, availableReferences,
                                          (String)availableReferences.get(0));

    availableInfoParam.addParameterChangeListener(this);
     // add the parameters to the parameter list
     paramList.addParameter(siteNameParam);
     paramList.addParameter(locationParameter);
     paramList.addParameter(availableFaultsParam);
     paramList.addParameter(studyTypesParam);
     paramList.addParameter(siteRepresentationParam);
     paramList.addParameter(availableInfoParam);
     paramList.addParameter(eventDatesAvailableParam);
     paramList.addParameter(startTimeEstimateParam);
     paramList.addParameter(endTimeEstimateParam);
     paramList.addParameter(datedFeatureCommentsParam);
     paramList.addParameter(referencesParam);
  }

  /**
   *  make the parameters visible/invisible when user selects the info
   *  he can provide
   *
   * @param event
   */
  public void parameterChange(ParameterChangeEvent event) {
    if(event.getParameterName().equalsIgnoreCase(this.AVAILABLE_INFO_PARAM_NAME))
      setParameters((String)availableInfoParam.getValue());
  }

  /**
   * make the parameters visible/invisible when user selects the info
   * he can provide
   *
   * @param info
   */
  private void setParameters(String info) {

    // show parameters for slip rate only
    if(info.equalsIgnoreCase(this.SLIP_RATE_INFO)) {
      setParamsVisibleForCumDisplacementInfo(false);
      setParamsVisibleForEventsInfo(false);
      setParamsVisibleForSlipRateInfo(true);
    }
     // show parameters just for cumulative displacement
    else if(info.equalsIgnoreCase(this.CUMULATIVE_DISPLACEMENT_INFO)) {
      setParamsVisibleForSlipRateInfo(false);
      setParamsVisibleForEventsInfo(false);
      setParamsVisibleForCumDisplacementInfo(true);
    }
    // show parameters for events info only
    else if(info.equalsIgnoreCase(this.EVENTS_INFO)) {
      setParamsVisibleForSlipRateInfo(false);
     setParamsVisibleForCumDisplacementInfo(false);
     setParamsVisibleForEventsInfo(true);
    }
    // show parameters for slip rate and events info
    else if(info.equalsIgnoreCase(this.SLIP_RATE_AND_EVENTS_INFO)) {
      setParamsVisibleForCumDisplacementInfo(false);
      setParamsVisibleForSlipRateInfo(true);
      setParamsVisibleForEventsInfo(true);
    }
    // show parameters for cumulative displacement and event info
    else if(info.equalsIgnoreCase(this.CUMULATIVE_DISPLACEMENT_AND_EVENTS_INFO)) {
      setParamsVisibleForSlipRateInfo(false);
      setParamsVisibleForCumDisplacementInfo(true);
      setParamsVisibleForEventsInfo(true);
    }
  }

  /**
   * Set the parameters visible/invisible when user is providing just Slip Rate info
   */
  private void setParamsVisibleForSlipRateInfo(boolean isVisible) {
    editor.setParameterVisible(SLIP_RATE_PARAM_NAME,isVisible);
    editor.setParameterVisible(this.ASEISMIC_SLIP_FACTOR_PARAM_NAME,isVisible);
    editor.setParameterVisible(this.SLIP_RATE_COMMENTS_PARAM_NAME,isVisible);
  }

  /**
   *  Set the parameters visible/invisible when user is providing just Cumulative
   *  displacement info
   */
  private void setParamsVisibleForCumDisplacementInfo(boolean isVisible) {
    editor.setParameterVisible(CUMULATIVE_DISPLACEMENT_PARAM_NAME,isVisible);
    editor.setParameterVisible(CUMULATIVE_DISPLACEMENT_COMMENTS_PARAM_NAME,isVisible);
    editor.setParameterVisible(this.ASEISMIC_SLIP_FACTOR_PARAM_NAME,isVisible);
  }

  /**
  *  Set the parameters visible/invisible when user is providing just Events
  *   info
  */
  private void setParamsVisibleForEventsInfo(boolean isVisible) {
    editor.setParameterVisible(NUM_EVENTS_PARAM_NAME,isVisible);
  }



  /**
   * Add the input parameters if the user provides the slip rate info
   */
  private void addSlipRateInfoParameters() {
   ArrayList allowedEstimates = EstimateConstraint.createConstraintForPositiveDoubleValues();
   this.slipRateEstimateParam = new EstimateParameter(this.SLIP_RATE_PARAM_NAME,
        SLIP_RATE_UNITS, SLIP_RATE_MIN, SLIP_RATE_MAX, allowedEstimates);
    this.aSeismicSlipFactorParam = new EstimateParameter(this.ASEISMIC_SLIP_FACTOR_PARAM_NAME,
        ASEISMIC_SLIP_FACTOR_MIN, ASEISMIC_SLIP_FACTOR_MAX, allowedEstimates);
    slipRateCommentsParam = new StringParameter(this.SLIP_RATE_COMMENTS_PARAM_NAME);
    paramList.addParameter(slipRateEstimateParam);
    paramList.addParameter(aSeismicSlipFactorParam);
    paramList.addParameter(slipRateCommentsParam);
  }

  /**
   * Add the input parameters if user provides the cumulative displacement
   */
  private void addCumulativeDisplacementParameters() {
  ArrayList allowedEstimates = EstimateConstraint.createConstraintForPositiveDoubleValues();
  this.cumDisplacementParam = new EstimateParameter(this.CUMULATIVE_DISPLACEMENT_PARAM_NAME,
       CUMULATIVE_DISPLACEMENT_UNITS, CUMULATIVE_DISPLACEMENT_MIN, CUMULATIVE_DISPLACEMENT_MAX, allowedEstimates);
   displacementCommentsParam = new StringParameter(this.CUMULATIVE_DISPLACEMENT_COMMENTS_PARAM_NAME);
   paramList.addParameter(cumDisplacementParam);
   paramList.addParameter(displacementCommentsParam);
  }

  /**
   * Add the input parameters if user provides the events
   */
  private void addEventsParameters() {
  ArrayList allowedEstimates = EstimateConstraint.createConstraintForPositiveIntValues();
  this.numEventsParam = new EstimateParameter(this.NUM_EVENTS_PARAM_NAME,
                                              NUM_EVENTS_MIN, NUM_EVENTS_MAX, allowedEstimates);
   paramList.addParameter(numEventsParam);
  }


  /**
   * this is JUST A FAKE IMPLEMENTATION. IT SHOULD GET ALL SITE NAMES FROM
   * the DATABASE
   * @return
   */
  private ArrayList getSiteNames() {
     ArrayList siteNamesList = new ArrayList();
     siteNamesList.add("Site 1");
     siteNamesList.add("Site 2");
     return siteNamesList;
  }

  /**
   * This is just a FAKE implemenetation. It should get all the FAULT NAMES
   * from the database
   * @return
   */
  private ArrayList getFaultNames() {
    ArrayList faultNamesList = new ArrayList();
    faultNamesList.add("Fault 1");
    faultNamesList.add("Fault 2");
    faultNamesList.add("Fault 3");
    return faultNamesList;
  }

  /**
   * Get the study types.
   * This is just a FAKE implementation. It should get all the STUDY TYPES
   * from the database
   *
   * @return
   */
  private ArrayList getStudyTypes() {
    ArrayList studyTypesList = new ArrayList();
    studyTypesList.add("Trench");
    studyTypesList.add("Geologic");
    studyTypesList.add("Survey/Cultural");
    return studyTypesList;
  }

  /**
   * Get the site representations.
   * This is just a FAKE implementation. It should get the SITE REPRSENTATIONS
   * from the database
   *
   * @return
   */
  private ArrayList getSiteRepresentations() {
    ArrayList siteRepresentations = new ArrayList();
    siteRepresentations.add("Entire Fault");
    siteRepresentations.add("Most Significant Strand");
    siteRepresentations.add("One of Several Strands");
    siteRepresentations.add("Unknown");
    return siteRepresentations;
  }

  /**
   * Get the available information list
   *
   * @return
   */
  private ArrayList getAvailableInfoList() {
     ArrayList availableInfoList = new ArrayList();
     availableInfoList.add(SLIP_RATE_INFO);
     availableInfoList.add(CUMULATIVE_DISPLACEMENT_INFO);
     availableInfoList.add(EVENTS_INFO);
     availableInfoList.add(SLIP_RATE_AND_EVENTS_INFO);
     availableInfoList.add(CUMULATIVE_DISPLACEMENT_AND_EVENTS_INFO);
     return availableInfoList;
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


  private void jbInit() throws Exception {
    this.getContentPane().setLayout(borderLayout1);
    mainPanel.setLayout(gridBagLayout1);
    /*headerLabel.setFont(new java.awt.Font("Dialog", 0, 20));
    headerLabel.setMaximumSize(new Dimension(120000, 120000));
    headerLabel.setHorizontalAlignment(SwingConstants.CENTER);
    headerLabel.setText("Paleo Site");*/
    this.getContentPane().add(mainPanel, BorderLayout.CENTER);
    /*mainPanel.add(headerLabel,   new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
            ,GridBagConstraints.NORTH, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));*/
    mainPanel.add(editor,  new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
        ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));
    }

    /**
     * Get a list of available references.
     *  THIS IS JUST A FAKE IMPLEMENTATION. IT SHOULD GET THIS FROM THE DATABASE.
     * @return
     */
  private ArrayList getAvailableReferences() {
    ArrayList referencesNamesList = new ArrayList();
    referencesNamesList.add("Reference 1");
    referencesNamesList.add("Reference 2");
    return referencesNamesList;

  }



  public static void main(String[] args) {
      PaleoSiteApp paleoSiteApp = new PaleoSiteApp();
      paleoSiteApp.pack();
      paleoSiteApp.show();
    }

}