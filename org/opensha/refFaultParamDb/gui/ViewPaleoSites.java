package org.opensha.refFaultParamDb.gui;

import javax.swing.*;
import org.opensha.param.*;
import org.opensha.param.event.*;
import org.opensha.param.editor.*;
import java.util.ArrayList;
import java.awt.event.*;
import java.awt.*;
import ch.randelshofer.quaqua.QuaquaManager;

/**
 * <p>Title: ViewPaleoSites.java </p>
 * <p>Description: This GUI allows usre to choose sites and view information about
 * them. </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class ViewPaleoSites extends JPanel implements ActionListener, ParameterChangeListener {
  // various input parameter names
  private final static String SITE_NAME_PARAM_NAME="Site Name";
  private final static String SITE_LOCATION_PARAM_NAME="Site Location";
  private final static String ASSOCIATED_WITH_FAULT_PARAM_NAME="Associated With Fault";
  private final static String SITE_TYPE_PARAM_NAME="Site Type";
  private final static String SITE_REPRESENTATION_PARAM_NAME="How Representative is this Site";
  private final static String LAT_PARAM_NAME="Site Latitude";
  private final static String LON_PARAM_NAME="Site Longitide";
  private final static String DEPTH_PARAM_NAME="Site Elevation";
  private final static Double DEFAULT_LAT_VAL=new Double(34.00);
  private final static Double DEFAULT_LON_VAL=new Double(-118.0);
  private final static Double DEFAULT_DEPTH_VAL=new Double(2.0);
  private final static String START_TIME_PARAM_NAME="Start Time";
  private final static String END_TIME_PARAM_NAME="End Time";

  private final static String TITLE = "View Sites";


  // input parameters declaration
  private StringParameter siteNameParam;
  private LocationParameter siteLocationParam;
  private StringParameter assocWithFaultParam;
  private StringParameter siteTypeParam;
  private StringParameter siteRepresentationParam;
  private StringParameter startTimeParam;
  private StringParameter endTimeParam;

  // input parameter editors
  private ConstrainedStringParameterEditor siteNameParamEditor;
  private LocationParameterEditor siteLocationParamEditor;
  private StringParameterEditor assocWithFaultParamEditor;
  private StringParameterEditor siteTypeParamEditor;
  private StringParameterEditor siteRepresentationParamEditor;
  private ConstrainedStringParameterEditor startTimeParamEditor;
  private ConstrainedStringParameterEditor endTimeParamEditor;

  // various buttons in thos window
  private JButton addNewSiteButton = new JButton("Add Site");
  private JButton editSiteButton = new JButton("Edit Site");
  private JButton viewEditTimeSpanInfoButton = new JButton("View/Edit Info for this Time Period");
  private JButton addTimeSpanInfoButton = new JButton("Add Info for another Time Period");
  private JButton closeButton = new JButton("Close");


  public ViewPaleoSites() {
    try {
      // initialize parameters and editors
      initParametersAndEditors();
      // add the editors to this window
      jbInit();
      // ad action listeners to catch the event on button click
      addActionListeners();
    }catch(Exception e)  {
      e.printStackTrace();
    }
  }

  /**
   * Add the editors to the window
   */
  private void jbInit() {
    int yPos = 0;
    setLayout(new GridBagLayout());
    // site name editor
    this.setMinimumSize(new Dimension(0, 0));
    add(siteNameParamEditor,  new GridBagConstraints(0, yPos++, 2, 1, 1.0, 1.0
       ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));
   // edit site button
    add(editSiteButton,  new GridBagConstraints(0, yPos, 1, 1, 1.0, 1.0
       ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
   // add site button
   add(addNewSiteButton,  new GridBagConstraints(1, yPos++, 1, 1, 1.0, 1.0
       ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));

   // site location
   add(siteLocationParamEditor,  new GridBagConstraints(0, yPos++, 2, 1, 1.0, 1.0
       ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));
   // associated with fault
   add(assocWithFaultParamEditor,  new GridBagConstraints(0, yPos++, 2, 1, 1.0, 1.0
       ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));
   // site types
   add(siteTypeParamEditor,  new GridBagConstraints(0, yPos++, 2, 1, 1.0, 1.0
       ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));
   // how representative is this site
   add(siteRepresentationParamEditor,  new GridBagConstraints(0, yPos++, 2, 1, 1.0, 1.0
       ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));
   // start times
   add(this.startTimeParamEditor,  new GridBagConstraints(0, yPos, 1, 1, 1.0, 1.0
       ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));
   // end times
   add(this.endTimeParamEditor,  new GridBagConstraints(1, yPos++, 1, 1, 1.0, 1.0
       ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));
   // view data for this time period
   add(this.viewEditTimeSpanInfoButton,  new GridBagConstraints(0, yPos, 1, 1, 1.0, 1.0
      ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
  // add data for a new time period
   add(this.addTimeSpanInfoButton,  new GridBagConstraints(1, yPos++, 1, 1, 1.0, 1.0
    ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
   // close button
   add(closeButton,  new GridBagConstraints(1, yPos++, 1, 1, 1.0, 1.0
       ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
  }

  /**
   * Add the action listeners to the button.
   */
  private void addActionListeners() {
    addNewSiteButton.addActionListener(this);
    editSiteButton.addActionListener(this);
    viewEditTimeSpanInfoButton.addActionListener(this);
    addTimeSpanInfoButton.addActionListener(this);
  }

  /**
  * Whenever user presses a button on this window, this function is called
  * @param event
  */
 public void actionPerformed(ActionEvent event) {
   // if it is "Add New Site" request, pop up another window to fill the new site type
   Object source = event.getSource();
    if(source==this.addNewSiteButton ||  source==this.editSiteButton)
       new AddEditPaleoSite();
    else if(source==viewEditTimeSpanInfoButton  || source==addTimeSpanInfoButton)
       new SiteInfoForTimePeriod();
 }


  /**
  * Initialize all the parameters and the editors
  */
 private void initParametersAndEditors() throws Exception {
   // available site names in the database
  ArrayList availableSites = getSiteNames();
  siteNameParam = new StringParameter(SITE_NAME_PARAM_NAME, availableSites, (String)availableSites.get(0));
  siteNameParamEditor = new ConstrainedStringParameterEditor(siteNameParam);
  siteNameParam.addParameterChangeListener(this);

  // Site Location(Lat/lon/depth)
  siteLocationParam = new LocationParameter(SITE_LOCATION_PARAM_NAME, LAT_PARAM_NAME,
                                            LON_PARAM_NAME, DEPTH_PARAM_NAME,
                                            DEFAULT_LAT_VAL,
                                            DEFAULT_LON_VAL, DEFAULT_DEPTH_VAL);
  siteLocationParamEditor = new LocationParameterEditor(siteLocationParam, true);
  siteLocationParamEditor.setEnabled(false);

  //  fault with which this site is associated
  assocWithFaultParam = new StringParameter(ASSOCIATED_WITH_FAULT_PARAM_NAME);
  assocWithFaultParamEditor = new StringParameterEditor(assocWithFaultParam);
  assocWithFaultParamEditor.setEnabled(false);

  // study type for this site
  siteTypeParam = new StringParameter(SITE_TYPE_PARAM_NAME);
  siteTypeParamEditor = new StringParameterEditor(siteTypeParam);
  siteTypeParamEditor.setEnabled(false);

  // Site representation
  siteRepresentationParam = new StringParameter(SITE_REPRESENTATION_PARAM_NAME);
  siteRepresentationParamEditor = new StringParameterEditor(siteRepresentationParam);
  siteRepresentationParamEditor.setEnabled(false);

  // get all the start times associated with this site
  ArrayList startTimes = getAllStartTimes();
  startTimeParam = new StringParameter(START_TIME_PARAM_NAME, startTimes, (String)startTimes.get(0));
  startTimeParamEditor = new ConstrainedStringParameterEditor(startTimeParam);

  // get all the end times associated with this site
  ArrayList endTimes = getAllEndTimes();
  endTimeParam = new StringParameter(END_TIME_PARAM_NAME, endTimes, (String)endTimes.get(0));
  endTimeParamEditor = new ConstrainedStringParameterEditor(endTimeParam);
 }

 /**
  * this is JUST A FAKE IMPLEMENTATION. IT SHOULD GET ALL START TIMES FROM
  * the DATABASE
  * @return
  */
 private ArrayList getAllStartTimes() {
   ArrayList startTimeList = new ArrayList();
   startTimeList.add("Start Time 1");
   startTimeList.add("Start Time 2");
   return startTimeList;

 }

 /**
  * this is JUST A FAKE IMPLEMENTATION. IT SHOULD GET ALL END TIMES FROM
  * the DATABASE
  * @return
  */
 private ArrayList getAllEndTimes() {
   ArrayList endTimeList = new ArrayList();
   endTimeList.add("End Time 1");
   endTimeList.add("End Time 2");
   return endTimeList;
 }


 public void parameterChange(ParameterChangeEvent event) {
   String paramName = event.getParameterName();

   if(paramName.equalsIgnoreCase(this.SITE_NAME_PARAM_NAME)) {
     // THIS IS JUST A FAKE IMPL.
     // IN REALITY, IT SHOULD GET DATA FROM database.
     String siteName = (String)event.getNewValue();
     String faultName = siteName.replaceAll("Site", "Fault");
     assocWithFaultParam.setValue(faultName);
     assocWithFaultParamEditor.refreshParamEditor();
   }

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

  public static void main(String[] args) {
    ViewPaleoSites viewPaleoSites = new ViewPaleoSites();
    /*this.setTitle(TITLE);
    this.pack();
    this.show();*/

  }
}