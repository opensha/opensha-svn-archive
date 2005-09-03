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
import org.opensha.gui.TitledBorderPanel;
import org.opensha.gui.LabeledBoxPanel;

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

  // various types of information that can be provided by the user
  private final static String AVAILABLE_INFO_PARAM_NAME="I have info on";
  private final static String SLIP_RATE_INFO = "Slip Rate";
  private final static String CUMULATIVE_DISPLACEMENT_INFO = "Cumulative Displacement";
  private final static String NUM_EVENTS_INFO = "Number of Events";
  private final static String INDIVIDUAL_EVENTS_INFO = "Individual Events & Sequences";
  // various types of information that can be provided by the user
  private JCheckBox slipRateCheckBox, cumDispCheckBox, numEventsCheckBox,
      individualEventsCheckBox;

  private final static String DATED_FEATURE_COMMENTS_PARAM_NAME="Description of Timespan";


  private final static String TITLE = "View Sites";

  // input parameters declaration
  private StringParameter siteNameParam;


  // input parameter editors
  private ConstrainedStringParameterEditor siteNameParamEditor;
  private InfoLabel siteLocationLabel ;
  private InfoLabel assocWithFaultLabel ;
  private InfoLabel siteTypeLabel;
  private InfoLabel siteRepresentationLabel;
  private LabeledBoxPanel iHaveInfoOnPanel;

  // various buttons in thos window
  private String ADD_SITE = "Add New Site";
  private JButton editSiteButton = new JButton("  Edit  ");
  private JButton eventSequenceButton = new JButton("Events and Seq.");
  private JButton addTimePdButton = new JButton("Add Info");

  private JPanel addEditSitePanel = new TitledBorderPanel("Site Characteristics");


  public ViewPaleoSites() {
    try {
      // initialize parameters and editors
      initParametersAndEditors();
      // add user provided info choices
      addUserProvidedInfoChoices();
      // add the editors to this window
      jbInit();
      // ad action listeners to catch the event on button click
      addActionListeners();
    }catch(Exception e)  {
      e.printStackTrace();
    }
  }

  /**
   * Add the panel which lists the information which can be provided by the user
   */
  private void addUserProvidedInfoChoices() {
    iHaveInfoOnPanel = new LabeledBoxPanel(new GridBagLayout());
    iHaveInfoOnPanel.setTitle(AVAILABLE_INFO_PARAM_NAME);
    slipRateCheckBox = new JCheckBox(this.SLIP_RATE_INFO);
    cumDispCheckBox = new JCheckBox(this.CUMULATIVE_DISPLACEMENT_INFO);
    numEventsCheckBox = new JCheckBox(this.NUM_EVENTS_INFO);
    individualEventsCheckBox = new JCheckBox(this.INDIVIDUAL_EVENTS_INFO);
    slipRateCheckBox.addActionListener(this);
    cumDispCheckBox.addActionListener(this);
    int yPos=0;
    iHaveInfoOnPanel.add(slipRateCheckBox, new GridBagConstraints(0, yPos++, 1, 1, 1.0, 1.0
                                                , GridBagConstraints.CENTER,
                                                GridBagConstraints.BOTH,
                                                new Insets(2, 2, 2, 2), 0, 0));
   iHaveInfoOnPanel.add(cumDispCheckBox, new GridBagConstraints(0, yPos++, 1, 1, 1.0, 1.0
                                                , GridBagConstraints.CENTER,
                                                GridBagConstraints.BOTH,
                                                new Insets(2, 2, 2, 2), 0, 0));
   iHaveInfoOnPanel.add(numEventsCheckBox, new GridBagConstraints(0, yPos++, 1, 1, 1.0, 1.0
                                                , GridBagConstraints.CENTER,
                                                GridBagConstraints.BOTH,
                                                new Insets(2, 2, 2, 2), 0, 0));
    iHaveInfoOnPanel.add(individualEventsCheckBox,
                         new GridBagConstraints(0, yPos++, 1, 1, 1.0, 1.0
                                                , GridBagConstraints.CENTER,
                                                GridBagConstraints.BOTH,
                                                new Insets(2, 2, 2, 2), 0, 0));
    iHaveInfoOnPanel.add(addTimePdButton,
                         new GridBagConstraints(0, yPos++, 1, 1, 1.0, 1.0
                                                , GridBagConstraints.CENTER,
                                                GridBagConstraints.BOTH,
                                                new Insets(2, 2, 2, 2), 0, 0));
  }

  /**
   * Add the editors to the window
   */
  private void jbInit() {
    int yPos = 0;
    setLayout(new GridBagLayout());
    addEditSitePanel.setLayout(new GridBagLayout());
    // site name editor
    this.setMinimumSize(new Dimension(0, 0));
    add(addEditSitePanel, new GridBagConstraints(0, yPos++, 1, 1, 1.0, 1.0
                                                 , GridBagConstraints.CENTER,
                                                 GridBagConstraints.BOTH,
                                                 new Insets(2, 2, 2, 2), 0, 0));
    addSiteCharacteristicsPanel();

    //adding the Events and Sequence button
    // various timespans
    /*add(eventSequenceButton,
        new GridBagConstraints(0, yPos++, 1, 1, 1.0, 1.0
                               , GridBagConstraints.CENTER,
                               GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2),
                               0, 0));

    //making the disabled for now
    eventSequenceButton.setEnabled(false);*/

    //adding the options so that user can provide the info
    add(this.iHaveInfoOnPanel,
        new GridBagConstraints(0, yPos++, 1, 1, 1.0, 1.0
                               , GridBagConstraints.CENTER,
                               GridBagConstraints.BOTH, new Insets(2, 2, 2, 2),
                               0, 0));
  }

  private void addSiteCharacteristicsPanel() {
    int siteYPos = 0;
    addEditSitePanel.add(siteNameParamEditor,
                         new GridBagConstraints(0, siteYPos++, 1, 1, 1.0,
                                                1.0
                                                , GridBagConstraints.CENTER,
                                                GridBagConstraints.BOTH,
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
                                               GridBagConstraints.NONE,
                                               new Insets(2, 2, 2, 2), 0, 0));
  }

  /**
   * Add the action listeners to the button.
   */
  private void addActionListeners() {
    editSiteButton.addActionListener(this);
    addTimePdButton.addActionListener(this);
  }

  /**
  * Whenever user presses a button on this window, this function is called
  * @param event
  */
 public void actionPerformed(ActionEvent event) {
   // if it is "Add New Site" request, pop up another window to fill the new site type
   Object source = event.getSource();
    if(source==this.editSiteButton)
       new AddEditPaleoSite();
    else if(source == this.addTimePdButton)
      new AddNewTimeSpan();
    // if user is providing information about the slip rate, disable the cum disp. check box
    else if(source == this.slipRateCheckBox) {
      if(slipRateCheckBox.isSelected()) this.cumDispCheckBox.setEnabled(false);
      else cumDispCheckBox.setEnabled(true);
    }
    // if user is providing information about the cum disp., disable the slip rate check box
   else if(source == this.cumDispCheckBox) {
     if(cumDispCheckBox.isSelected()) this.slipRateCheckBox.setEnabled(false);
     else slipRateCheckBox.setEnabled(true);
   }

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
  }


 public void parameterChange(ParameterChangeEvent event) {
   String paramName = event.getParameterName();
   if(paramName.equalsIgnoreCase(this.SITE_NAME_PARAM_NAME)) {
     String siteName = (String) this.siteNameParam.getValue();
     // if add site is selected, show window to add a site
     if(siteName.equalsIgnoreCase(this.ADD_SITE)) new AddEditPaleoSite();
     else setSiteInfo();
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
   siteNamesList.add(ADD_SITE);
   return siteNamesList;
 }

  public static void main(String[] args) {
    ViewPaleoSites viewPaleoSites = new ViewPaleoSites();
  }
}
