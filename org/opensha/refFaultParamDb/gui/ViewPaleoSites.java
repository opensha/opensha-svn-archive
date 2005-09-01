package org.opensha.refFaultParamDb.gui;

import javax.swing.*;
import org.opensha.param.*;
import org.opensha.param.event.*;
import org.opensha.param.editor.*;
import java.util.ArrayList;
import java.awt.event.*;
import java.awt.*;
import org.opensha.refFaultParamDb.gui.infotools.InfoLabel;
import javax.swing.border.TitledBorder;
import javax.swing.border.Border;

/**
 * <p>Title: ViewPaleoSites.java </p>
 * <p>Description: This GUI allows user to choose sites and view information about
 * them. </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class ViewPaleoSites extends JPanel implements ActionListener, ParameterChangeListener {
  // various input parameter names
  private final static String SITE_NAME_PARAM_NAME="Site Name";
  private final static String SITE_LOCATION_PARAM_NAME="Site Location:";
  private final static String ASSOCIATED_WITH_FAULT_PARAM_NAME="Associated With Fault:";
  private final static String SITE_TYPE_PARAM_NAME="Site Type:";
  private final static String SITE_REPRESENTATION_PARAM_NAME="How Representative is this Site:";
  private final static String TIMESPAN_PARAM_NAME="TimeSpans";
  private final static String DATED_FEATURE_COMMENTS_PARAM_NAME="Description of Dated Features";


  private final static String TITLE = "View Sites";

  // input parameters declaration
  private StringParameter siteNameParam;
  private StringParameter timeSpanParam;
  private StringParameter datedFeatureCommentsParam;

  // input parameter editors
  private ConstrainedStringParameterEditor siteNameParamEditor;
  private InfoLabel siteLocationLabel ;
  private InfoLabel assocWithFaultLabel ;
  private InfoLabel siteTypeLabel;
  private InfoLabel siteRepresentationLabel;
  private ConstrainedStringParameterEditor timeSpanParamEditor;
  private CommentsParameterEditor datedFeatureCommentsParamEditor;

  // various buttons in thos window
  private JButton addNewSiteButton = new JButton("  Add  ");
  private JButton editSiteButton = new JButton("  Edit  ");
  private JButton viewEditTimeSpanInfoButton = new JButton("Edit Info for this Time Pd");
  private JButton addTimeSpanInfoButton = new JButton("Add Info for another Time Pd");
  private JButton eventSequenceButton = new JButton("Events and Seq.");
  private JButton addTimePdButton = new JButton("Add Time Pd");

  private JPanel addEditSitePanel = new JPanel();


  private Border border9 = BorderFactory.createLineBorder(new Color(80, 80, 140),
      1);
  private TitledBorder siteBorder = new TitledBorder(border9,
      "Site Characterstics");



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
    addEditSitePanel.setLayout(new GridBagLayout());
    addEditSitePanel.setBorder(siteBorder);
    siteBorder.setTitleColor(new Color(80,80,133));
    // site name editor
    this.setMinimumSize(new Dimension(0, 0));
    add(addEditSitePanel, new GridBagConstraints(0, yPos++, 1, 1, 1.0, 1.0
                                                 , GridBagConstraints.CENTER,
                                                 GridBagConstraints.BOTH,
                                                 new Insets(2, 2, 2, 2), 0, 0));
    int siteYPos = 0;
    addEditSitePanel.add(siteNameParamEditor,
                         new GridBagConstraints(0, siteYPos++, 1, 1, 1.0,
                                                1.0
                                                , GridBagConstraints.CENTER,
                                                GridBagConstraints.BOTH,
                                                new Insets(2, 2, 2, 2), 0, 0));
    // add site button
    addEditSitePanel.add(addNewSiteButton,
                         new GridBagConstraints(0, siteYPos++, 1, 1, 1.0, 1.0
                                                , GridBagConstraints.CENTER,
                                                GridBagConstraints.HORIZONTAL,
                                                new Insets(2, 2, 2, 2), 0, 0));
    // site location
    addEditSitePanel.add(siteLocationLabel,
                         new GridBagConstraints(0, siteYPos++, 1, 1, 1.0, 1.0
                                                , GridBagConstraints.CENTER,
                                                GridBagConstraints.BOTH,
                                                new Insets(2, 2, 2, 2), 0, 0));
    // associated with fault
    addEditSitePanel.add(assocWithFaultLabel, new GridBagConstraints(0, siteYPos++, 1, 1, 1.0, 1.0
                                                    , GridBagConstraints.CENTER,
                                                    GridBagConstraints.BOTH,
                                                    new Insets(2, 2 , 2, 2), 0, 0));
    // site types
    addEditSitePanel.add(siteTypeLabel, new GridBagConstraints(0, siteYPos++, 1, 1, 1.0, 1.0
                                              , GridBagConstraints.CENTER,
                                              GridBagConstraints.BOTH,
                                              new Insets(2, 2 , 2, 2), 0, 0));
    // how representative is this site
    addEditSitePanel.add(siteRepresentationLabel,
        new GridBagConstraints(0, siteYPos++, 1, 1, 1.0, 1.0
                               , GridBagConstraints.CENTER,
                               GridBagConstraints.BOTH, new Insets(2, 2, 2, 2),
                               0, 0));
    // edit site button
    addEditSitePanel.add(editSiteButton, new GridBagConstraints(0, siteYPos++, 1, 1, 1.0, 1.0
                                               , GridBagConstraints.CENTER,
                                               GridBagConstraints.HORIZONTAL,
                                               new Insets(2, 2, 2, 2), 0, 0));

    //adding the Events and Sequence button
    // various timespans
    add(eventSequenceButton,
        new GridBagConstraints(0, yPos++, 1, 1, 1.0, 1.0
                               , GridBagConstraints.CENTER,
                               GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2),
                               0, 0));

    //making the disabled for now
    eventSequenceButton.setEnabled(false);


    //adding the button to add the Time Pd for the given Site
    add(this.addTimePdButton,
        new GridBagConstraints(0, yPos++, 1, 1, 1.0, 1.0
                               , GridBagConstraints.CENTER,
                               GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2),
                               0, 0));


    // various timespans
    add(this.timeSpanParamEditor,
        new GridBagConstraints(0, yPos++, 1, 1, 1.0, 1.0
                               , GridBagConstraints.CENTER,
                               GridBagConstraints.BOTH, new Insets(2, 2, 2, 2),
                               0, 0));
    add(this.datedFeatureCommentsParamEditor,
        new GridBagConstraints(0, yPos++, 1, 1, 1.0, 1.0
                               , GridBagConstraints.CENTER,
                               GridBagConstraints.BOTH, new Insets(2, 2, 2, 2),
                               0, 0));
  // view data for this time period
    add(this.viewEditTimeSpanInfoButton,
        new GridBagConstraints(0, yPos++, 1, 1, 1.0, 1.0
                               , GridBagConstraints.CENTER,
                               GridBagConstraints.HORIZONTAL,
                               new Insets(2, 2, 2, 2), 0, 0));
    // add data for a new time period
    add(this.addTimeSpanInfoButton,
        new GridBagConstraints(0, yPos++, 1, 1, 1.0, 1.0
                               , GridBagConstraints.CENTER,
                               GridBagConstraints.HORIZONTAL,
                               new Insets(2, 2, 2, 2), 0, 0));
  }

  /**
   * Add the action listeners to the button.
   */
  private void addActionListeners() {
    addNewSiteButton.addActionListener(this);
    editSiteButton.addActionListener(this);
    viewEditTimeSpanInfoButton.addActionListener(this);
    addTimeSpanInfoButton.addActionListener(this);
    addTimePdButton.addActionListener(this);
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
    else if(source == this.addTimePdButton)
      new AddNewTimeSpan();
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
  setSiteInfo();
 }

 /**
  * Set the paleo site info based on selected Paleo Site
  * THIS IS A FAKE IMPLEMENTATION. NEEDS TO BE DONE CORRECTLY
  * @param paleoSite
  */
  private void setSiteInfo()  {
    siteLocationLabel = new InfoLabel(SITE_LOCATION_PARAM_NAME,"33.47,-118.25");
    //  fault with which this site is associated
    assocWithFaultLabel = new InfoLabel(ASSOCIATED_WITH_FAULT_PARAM_NAME,"Fault1");

    // site type for this site
    siteTypeLabel = new InfoLabel(SITE_TYPE_PARAM_NAME,"Trench");

    // Site representation
    siteRepresentationLabel = new InfoLabel(SITE_REPRESENTATION_PARAM_NAME,"Most Significant Strand");

    // get all the start times associated with this site
    ArrayList timeSpans = getAllTimeSpans();
    timeSpanParam = new StringParameter(TIMESPAN_PARAM_NAME, timeSpans, (String)timeSpans.get(0));
    timeSpanParamEditor = new ConstrainedStringParameterEditor(timeSpanParam);
    try {
      // dated feature comments
      datedFeatureCommentsParam = new StringParameter(this.DATED_FEATURE_COMMENTS_PARAM_NAME);
      datedFeatureCommentsParamEditor = new CommentsParameterEditor(datedFeatureCommentsParam);
    }catch(Exception e) {
      e.printStackTrace();
    }
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
     setSiteInfo();
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
  }
}
