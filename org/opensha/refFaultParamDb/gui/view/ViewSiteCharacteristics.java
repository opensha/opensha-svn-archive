package org.opensha.refFaultParamDb.gui.view;

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
import org.opensha.data.Location;
import org.opensha.refFaultParamDb.gui.addEdit.*;
import org.opensha.refFaultParamDb.gui.*;
import org.opensha.refFaultParamDb.gui.infotools.GUI_Utils;
import org.opensha.refFaultParamDb.dao.db.*;
import org.opensha.refFaultParamDb.vo.PaleoSite;
import org.opensha.refFaultParamDb.vo.PaleoSiteSummary;
import org.opensha.refFaultParamDb.gui.event.DbAdditionListener;
import org.opensha.refFaultParamDb.gui.event.DbAdditionSuccessEvent;
import org.opensha.refFaultParamDb.vo.Reference;

/**
 * <p>Title: ViewPaleoSites.java </p>
 * <p>Description: This GUI allows user to choose sites and view information about
 * them. </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class ViewSiteCharacteristics extends JPanel implements ActionListener,
    ParameterChangeListener, DbAdditionListener {
  // various input parameter names
  private final static String SITE_NAME_PARAM_NAME="Site Name";
  private final static String SITE_LOCATION_PARAM_NAME="Site Location";
  private final static String ASSOCIATED_WITH_FAULT_PARAM_NAME="Associated With Fault";
  private final static String SITE_TYPE_PARAM_NAME="Site Type";
  private final static String SITE_REPRESENTATION_PARAM_NAME="How Representative is this Site";
  private final static String SITE_REFERENCES_PARAM_NAME="References";
  // various types of information that can be provided by the user
  private final static String AVAILABLE_INFO_PARAM_NAME="I have data on";
  private final static String SLIP_RATE_INFO = "Slip Rate";
  private final static String CUMULATIVE_DISPLACEMENT_INFO = "Cumulative Displacement";
  private final static String NUM_EVENTS_INFO = "Number of Events";
  private final static String INDIVIDUAL_EVENTS_INFO = "Individual Events";
  private final static String SEQUENCE_INFO = "Sequence Info";
  private final static String NO_SITE_NAME="No Site Name-";
  private final static String CONTRIBUTOR_PARAM_NAME = "Last Updated by";
  private final static String ENTRY_DATE_PARAM_NAME = "Last Updated on";
  private final static String MSG_ADD_DATA_ALREADY_OPEN = "Add Data window is already open";
  private final static String MSG_EVENT_ALREADY_OPEN = "Add Events Data window is already open";

  // various types of information that can be provided by the user
  private JCheckBox slipRateCheckBox, cumDispCheckBox, numEventsCheckBox,
      individualEventsCheckBox, sequenceCheckBox;

  private final static String DATED_FEATURE_COMMENTS_PARAM_NAME="Description of Timespan";


  private final static String TITLE = "View Sites";

  // input parameters declaration
  private StringParameter siteNameParam;
  public final static String TEST_SITE = "A Sample Site";
  private final static String MSG_TEST_SITE_NOT_EDITABLE = "Sample site is non-editable";


  // input parameter editors
  private ConstrainedStringParameterEditor siteNameParamEditor;
  private InfoLabel siteLocationLabel = new InfoLabel();
  private InfoLabel assocWithFaultLabel = new InfoLabel();
  private InfoLabel siteTypeLabel= new InfoLabel();
  private InfoLabel siteRepresentationLabel= new InfoLabel();
  private InfoLabel siteReferencesLabel = new InfoLabel();
  private InfoLabel lastEntryDateLabel = new InfoLabel();
  private InfoLabel contributorNameLabel = new InfoLabel();
  private LabeledBoxPanel iHaveInfoOnPanel;

  // various buttons in thos window
  private String ADD_SITE = "Add New Site";
  private JButton editSiteButton = new JButton("Edit");
  private JButton qFaultsEntriesButton = new JButton("Show QFault entries");
  private JButton eventSequenceButton = new JButton("Events and Seq.");
  private JButton addInfoButton = new JButton("Add Data");
  private JSplitPane splitPane = new JSplitPane();

  private JPanel addEditSitePanel = new TitledBorderPanel("Site Characteristics");

  private ArrayList paleoSiteSummaryList;
  private ArrayList siteNamesList;
  private PaleoSite paleoSite; //currently selected paleo site

  // class listening to site change events
  private SiteSelectionAPI siteSelectionListener;
  private AddEditSiteCharacteristics addEditSiteChars;
  private AddSiteInfo addSiteInfo;
  private AddEditIndividualEvent addEditIndividualEvent;

  //dao
  private PaleoSiteDB_DAO paleoSiteDAO = new PaleoSiteDB_DAO(DB_AccessAPI.dbConnection);


  public ViewSiteCharacteristics(SiteSelectionAPI siteSelectionListener) {
    try {
      this.siteSelectionListener = siteSelectionListener;
      addEditSitePanel.setLayout(GUI_Utils.gridBagLayout);
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
    iHaveInfoOnPanel = new LabeledBoxPanel(GUI_Utils.gridBagLayout);
    iHaveInfoOnPanel.setTitle(AVAILABLE_INFO_PARAM_NAME);
    slipRateCheckBox = new JCheckBox(this.SLIP_RATE_INFO);
    cumDispCheckBox = new JCheckBox(this.CUMULATIVE_DISPLACEMENT_INFO);
    numEventsCheckBox = new JCheckBox(this.NUM_EVENTS_INFO);
    individualEventsCheckBox = new JCheckBox(this.INDIVIDUAL_EVENTS_INFO);
    sequenceCheckBox = new JCheckBox(this.SEQUENCE_INFO);
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
    iHaveInfoOnPanel.add(sequenceCheckBox,
                         new GridBagConstraints(0, yPos++, 1, 1, 1.0, 1.0
                                                , GridBagConstraints.CENTER,
                                                GridBagConstraints.BOTH,
                                                new Insets(2, 2, 2, 2), 0, 0));
    iHaveInfoOnPanel.add(addInfoButton,
                         new GridBagConstraints(0, yPos++, 1, 1, 1.0, 1.0
                                                , GridBagConstraints.CENTER,
                                                GridBagConstraints.NONE,
                                                new Insets(2, 2, 2, 2), 0, 0));
  }

  /**
   * Add the editors to the window
   */
  private void jbInit() {
    int yPos = 0;
    setLayout(GUI_Utils.gridBagLayout);
    splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
    // site name editor
    this.setMinimumSize(new Dimension(0, 0));
    add(splitPane, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
                               , GridBagConstraints.CENTER,
                               GridBagConstraints.BOTH, new Insets(2, 2, 2, 2),
                               0, 0));
    splitPane.add(addEditSitePanel, JSplitPane.TOP);
    addEditSiteCharacteristicsPanel();

    //adding the options so that user can provide the info
    splitPane.add(this.iHaveInfoOnPanel, JSplitPane.BOTTOM);
    splitPane.setDividerLocation(375);
  }

  private void addEditSiteCharacteristicsPanel() {
    int siteYPos = 1;

    // edit site button
    addEditSitePanel.add(editSiteButton, new GridBagConstraints(0, siteYPos++, 1, 1, 1.0, 1.0
                                               , GridBagConstraints.EAST,
                                               GridBagConstraints.NONE,
                                               new Insets(2, 2, 2, 2), 0, 0));

    //++siteYPos; // increment because site names editor exists at this place
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
    // site references
   addEditSitePanel.add(this.siteReferencesLabel,
       new GridBagConstraints(0, siteYPos++, 1, 1, 1.0, 1.0
                              , GridBagConstraints.CENTER,
                              GridBagConstraints.BOTH, new Insets(2, 2, 2, 2),
                              0, 0));
    // entry date
    addEditSitePanel.add(this.lastEntryDateLabel,
       new GridBagConstraints(0, siteYPos++, 1, 1, 1.0, 1.0
                              , GridBagConstraints.CENTER,
                              GridBagConstraints.BOTH, new Insets(2, 2, 2, 2),
                              0, 0));


    // contributor
    addEditSitePanel.add(this.contributorNameLabel,
       new GridBagConstraints(0, siteYPos++, 1, 1, 1.0, 1.0
                              , GridBagConstraints.CENTER,
                              GridBagConstraints.BOTH, new Insets(2, 2, 2, 2),
                              0, 0));



    // QFault entries for this site
    addEditSitePanel.add(qFaultsEntriesButton,
        new GridBagConstraints(0, siteYPos++, 1, 1, 1.0, 1.0
                               , GridBagConstraints.CENTER,
                               GridBagConstraints.NONE, new Insets(2, 2, 2, 2),
                               0, 0));


  }

  /**
   * Add the action listeners to the button.
   */
  private void addActionListeners() {
    editSiteButton.addActionListener(this);
    addInfoButton.addActionListener(this);
  }

  /**
  * Whenever user presses a button on this window, this function is called
  * @param event
  */
 public void actionPerformed(ActionEvent event) {
   // if it is "Add New Site" request, pop up another window to fill the new site type
   Object source = event.getSource();
    if(source==this.editSiteButton) {// edit the paleo site
      if(paleoSite!=null) {
        addEditSiteChars = new AddEditSiteCharacteristics(true, this.paleoSite);
        addEditSiteChars.addDbAdditionSuccessListener(this);
      }else JOptionPane.showMessageDialog(this, MSG_TEST_SITE_NOT_EDITABLE);
    }
    else if(source == this.addInfoButton) {
     try {
        if(paleoSite!=null) {
          if(slipRateCheckBox.isSelected() ||
             this.cumDispCheckBox.isSelected() ||
             numEventsCheckBox.isSelected()||
             this.sequenceCheckBox.isSelected()) {
             if(addSiteInfo!=null && addSiteInfo.isVisible()) {
               JOptionPane.showMessageDialog(this, MSG_ADD_DATA_ALREADY_OPEN);
               return;
             }
             addSiteInfo = new AddSiteInfo(this.paleoSite.getSiteId(),
                            this.paleoSite.getEntryDate(),
                            this.slipRateCheckBox.isSelected(),
                            this.cumDispCheckBox.isSelected(),
                            this.numEventsCheckBox.isSelected(),
                            this.sequenceCheckBox.isSelected());
            addSiteInfo.addDbAdditionSuccessListener(this);
          }
          if(this.individualEventsCheckBox.isSelected()) {
            if(addEditIndividualEvent!=null && addEditIndividualEvent.isVisible()) {
              JOptionPane.showMessageDialog(this, MSG_EVENT_ALREADY_OPEN);
              return;
            }
           showIndividualEventWindow();
          }
        }
     else JOptionPane.showMessageDialog(this, MSG_TEST_SITE_NOT_EDITABLE);
     }catch(Exception e) {
       e.printStackTrace();
       JOptionPane.showMessageDialog(this, e.getMessage());
     }
    }
 }


  /**
  * Initialize all the parameters and the editors
  */
 private void initParametersAndEditors() throws Exception {
   if(siteNameParamEditor!=null) addEditSitePanel.remove(siteNameParamEditor);
   // available site names in the database
  ArrayList availableSites = getSiteNames();
  siteNameParam = new StringParameter(SITE_NAME_PARAM_NAME, availableSites, (String)availableSites.get(0));
  siteNameParamEditor = new ConstrainedStringParameterEditor(siteNameParam);
  siteNameParam.addParameterChangeListener(this);

  addEditSitePanel.add(siteNameParamEditor,
                       new GridBagConstraints(0, 0, 1, 1, 1.0,
                                              1.0
                                              , GridBagConstraints.CENTER,
                                              GridBagConstraints.BOTH,
                                              new Insets(2, 2, 2, 2), 0, 0));

  setSiteInfo((String)availableSites.get(0));
 }

 /**
  * Set the paleo site info based on selected Paleo Site
  * @param paleoSite
  */
  private void setSiteInfo(String siteName)  {
    String siteType, siteRepresentation, faultName, references;
    String lastEntryDate, lastUpdatedBy;
    Location location;
    if(siteName.equalsIgnoreCase(this.TEST_SITE)) { // test site
      siteType = "Trench";
      siteRepresentation = "Most Significant Strand";
      faultName = "Fault1";
      location = new Location(34.00, -116, 0);
      references = "Ref 1";
      paleoSite=null;
      lastEntryDate = "Not Available";
      lastUpdatedBy="Test";
    }
    else { // paleo site information from the database
      int index = this.siteNamesList.indexOf(siteName)-1; // -1 IS NEEDED BECAUSE OF TEST SITE
      PaleoSiteSummary paleoSiteSummary = (PaleoSiteSummary)this.paleoSiteSummaryList.get(index);
      paleoSite = this.paleoSiteDAO.getPaleoSite(paleoSiteSummary.getSiteId());
      faultName = paleoSite.getFaultName();
      location = new Location(paleoSite.getSiteLat1(), paleoSite.getSiteLon1(),
                              paleoSite.getSiteElevation1());
      ArrayList studyTypes = paleoSite.getSiteTypeNames();
      siteType ="";
      for(int i=0; i<studyTypes.size(); ++i)
        siteType+=studyTypes.get(i)+",";

      siteRepresentation = paleoSite.getRepresentativeStrandName();
      lastEntryDate = paleoSite.getEntryDate();
      lastUpdatedBy=paleoSite.getContributorName();
      ArrayList referenceList = paleoSite.getReferenceList();
      references="";
      for(int i=0; i<referenceList.size();++i) {
        references = references+((Reference)referenceList.get(i)).getSummary()+
            ";";
      }
    }
    siteLocationLabel.setTextAsHTML(SITE_LOCATION_PARAM_NAME,
                                    GUI_Utils.decimalFormat.format(location.getLatitude())+","+ GUI_Utils.decimalFormat.format(location.getLongitude()));
    //  fault with which this site is associated
    assocWithFaultLabel.setTextAsHTML(ASSOCIATED_WITH_FAULT_PARAM_NAME,faultName);
    // site type for this site
    siteTypeLabel.setTextAsHTML(SITE_TYPE_PARAM_NAME,siteType);
    // Site representation
    siteRepresentationLabel.setTextAsHTML(SITE_REPRESENTATION_PARAM_NAME,siteRepresentation);
    // site references
    this.siteReferencesLabel.setTextAsHTML(this.SITE_REFERENCES_PARAM_NAME, references);
    // last entry date
    this.lastEntryDateLabel.setTextAsHTML(this.ENTRY_DATE_PARAM_NAME, lastEntryDate);
    // last entry by
    this.contributorNameLabel.setTextAsHTML(this.CONTRIBUTOR_PARAM_NAME, lastUpdatedBy);
    // call the listener
    siteSelectionListener.siteSelected(this.paleoSite); // call the listening class
  }


 public void parameterChange(ParameterChangeEvent event) {
   String paramName = event.getParameterName();
   if(paramName.equalsIgnoreCase(this.SITE_NAME_PARAM_NAME)) {
     String siteName = (String) this.siteNameParam.getValue();
     // if add site is selected, show window to add a site
     if(siteName.equalsIgnoreCase(this.ADD_SITE)) {
       addEditSiteChars = new AddEditSiteCharacteristics();
       addEditSiteChars.addDbAdditionSuccessListener(this);
     }
     else setSiteInfo(siteName);
   }

 }

 /**
  * It gets all the site names from the database
  * @return
  */
 private ArrayList getSiteNames() {
   paleoSiteSummaryList = paleoSiteDAO.getAllPaleoSiteNames();
   siteNamesList = new ArrayList();
   siteNamesList.add(this.TEST_SITE);
   int numSites = paleoSiteSummaryList.size();
   String siteName;
   PaleoSiteSummary paleoSiteSummary;
   for(int i=0; i<numSites; ++i) {
     paleoSiteSummary = (PaleoSiteSummary)paleoSiteSummaryList.get(i);
     siteName = paleoSiteSummary.getSiteName().trim();
     if(siteName==null || siteName.equalsIgnoreCase("")) siteName=NO_SITE_NAME+i;
     siteNamesList.add(siteName);
   }
   siteNamesList.add(ADD_SITE);
   return siteNamesList;
 }

 /**
  * This function is called whenever new info is added for a site into the database.
  * @param event
  */
 public void dbAdditionSuccessful(DbAdditionSuccessEvent event) {
   Object source = event.getSource();
   if(source == addEditIndividualEvent) {
     addEditIndividualEvent.dispose();
     showIndividualEventWindow();
   }
   String siteName = (String) this.siteNameParam.getValue();
   if(siteName.equalsIgnoreCase(this.ADD_SITE)) {
     try {
       initParametersAndEditors();
       this.updateUI();
     }catch(Exception e) {
       e.printStackTrace();
     }
   }
   else this.setSiteInfo(siteName);
 }

private void showIndividualEventWindow() {
   addEditIndividualEvent = new AddEditIndividualEvent(paleoSite.getSiteId(),
                                     paleoSite.getEntryDate());
  addEditIndividualEvent.addDbAdditionSuccessListener(this);
}

}
