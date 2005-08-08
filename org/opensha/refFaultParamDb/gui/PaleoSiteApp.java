package org.opensha.refFaultParamDb.gui;

import javax.swing.*;
import org.opensha.param.editor.ParameterListEditor;
import org.opensha.param.*;
import java.util.ArrayList;
import java.awt.*;
import ch.randelshofer.quaqua.QuaquaManager;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class PaleoSiteApp extends JFrame {
  private final static String TITLE_SITE_INFO="Site Info";
  private final static String SITE_PARAM_NAME = "Site Name";
  private final static String LOCATION_PARAM_NAME = "Site Location";
  private final static String FAULT_PARAM_NAME="Associated with Fault";
  private final static String TYPES_OF_STUDY_PARAM_NAME="Types of Study";
  private final static String REPRESENTATIVE_SITE_PARAM_NAME="How representative is this site";
  private final static String AVAILABLE_INFO_PARAM_NAME="I have info on";
  private final static String INDIVIDUAL_EVENTS_DATE_AVAILABLE_PARAM_NAME="I can provide dates for individual events";
  private final static String LAT_PARAM_NAME="Site Latitude";
  private final static String LON_PARAM_NAME="Site Longitide";
  private final static String DEPTH_PARAM_NAME="Site Elevation";
  private final static Double DEFAULT_LAT_VAL=new Double(34.00);
  private final static Double DEFAULT_LON_VAL=new Double(-118.0);
  private final static Double DEFAULT_DEPTH_VAL=new Double(2.0);



  private StringParameter siteNameParam;
  private LocationParameter locationParameter;
  private StringParameter availableFaultsParam;
  private StringParameter studyTypesParam;
  private StringParameter siteRepresentationParam;
  private StringParameter availableInfoParam;
  private BooleanParameter eventDatesAvailableParam;

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
    }
    catch(Exception e) {
      e.printStackTrace();
    }
  }


  /**
   * Initialize the parameters for the GUI
   */
  private void initParamListAndEditor() {

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
    studyTypesParam = new StringParameter(TYPES_OF_STUDY_PARAM_NAME, studyTypes,
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



    // add the parameters to the parameter list
    paramList = new ParameterList();
    paramList.addParameter(siteNameParam);
    paramList.addParameter(locationParameter);
    paramList.addParameter(availableFaultsParam);
    paramList.addParameter(studyTypesParam);
    paramList.addParameter(siteRepresentationParam);
    paramList.addParameter(availableInfoParam);
    paramList.addParameter(eventDatesAvailableParam);
    editor = new ParameterListEditor(paramList);
    editor.setTitle(TITLE_SITE_INFO);
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
     availableInfoList.add("Slip Rate");
     availableInfoList.add("Cumulative Displacement");
     availableInfoList.add("Events");
     availableInfoList.add("Slip Rate and Events");
     availableInfoList.add("Cumulative Displacement and Events");
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

    public static void main(String[] args) {
      PaleoSiteApp paleoSiteApp = new PaleoSiteApp();
      paleoSiteApp.pack();
      paleoSiteApp.show();
    }

}