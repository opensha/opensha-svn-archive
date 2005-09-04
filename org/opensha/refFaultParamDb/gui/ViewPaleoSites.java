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
import org.opensha.data.Location;

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
  private final static String TEST_SITE = "Test Site 1";


  // input parameter editors
  private ConstrainedStringParameterEditor siteNameParamEditor;
  private InfoLabel siteLocationLabel = new InfoLabel();
  private InfoLabel assocWithFaultLabel = new InfoLabel();
  private InfoLabel siteTypeLabel= new InfoLabel();
  private InfoLabel siteRepresentationLabel= new InfoLabel();
  private LabeledBoxPanel iHaveInfoOnPanel;

  // various buttons in thos window
  private String ADD_SITE = "Add New Site";
  private JButton editSiteButton = new JButton("Edit");
  private JButton qFaultsEntriesButton = new JButton("QFault entries for this site");
  private JButton eventSequenceButton = new JButton("Events and Seq.");
  private JButton addTimePdButton = new JButton("Add Info");

  private JPanel addEditSitePanel = new TitledBorderPanel("Site Characteristics");


  private ArrayList faultNamesList;
  private ArrayList siteLocationsList;
  private ArrayList siteNamesList;


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
    /*iHaveInfoOnPanel.add(individualEventsCheckBox,
                         new GridBagConstraints(0, yPos++, 1, 1, 1.0, 1.0
                                                , GridBagConstraints.CENTER,
                                                GridBagConstraints.BOTH,
                                                new Insets(2, 2, 2, 2), 0, 0));
    */
     iHaveInfoOnPanel.add(addTimePdButton,
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
    setLayout(new GridBagLayout());
    addEditSitePanel.setLayout(new GridBagLayout());
    // site name editor
    this.setMinimumSize(new Dimension(0, 0));
    add(addEditSitePanel, new GridBagConstraints(0, yPos++, 1, 1, 1.0, 1.0
                                                 , GridBagConstraints.CENTER,
                                                 GridBagConstraints.BOTH,
                                                 new Insets(2, 2, 2, 2), 0, 0));
    addEditSiteCharacteristicsPanel();

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

  private void addEditSiteCharacteristicsPanel() {
    int siteYPos = 0;

    // edit site button
    addEditSitePanel.add(editSiteButton, new GridBagConstraints(0, siteYPos++, 1, 1, 1.0, 1.0
                                               , GridBagConstraints.EAST,
                                               GridBagConstraints.NONE,
                                               new Insets(2, 2, 2, 2), 0, 0));


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
  setSiteInfo((String)this.siteNamesList.get(0));
 }

 /**
  * Set the paleo site info based on selected Paleo Site
  * THIS IS A FAKE IMPLEMENTATION. NEEDS TO BE DONE CORRECTLY
  * @param paleoSite
  */
  private void setSiteInfo(String siteName)  {
    int index = this.siteNamesList.indexOf(siteName);
    String faultName = (String)this.faultNamesList.get(index);
    Location location = (Location)this.siteLocationsList.get(index);
    String siteType="N/A", siteRepresentation="N/A";
    if(siteName.equalsIgnoreCase(this.TEST_SITE)) {
      siteType = "Trench";
      siteRepresentation = "Most Significant Strand";
    }
    siteLocationLabel.setTextAsHTML(SITE_LOCATION_PARAM_NAME,location.getLatitude()+","+location.getLongitude());
    //  fault with which this site is associated
    assocWithFaultLabel.setTextAsHTML(ASSOCIATED_WITH_FAULT_PARAM_NAME,faultName);
    // site type for this site
    siteTypeLabel.setTextAsHTML(SITE_TYPE_PARAM_NAME,siteType);
    // Site representation
    siteRepresentationLabel.setTextAsHTML(SITE_REPRESENTATION_PARAM_NAME,siteRepresentation);
  }


 public void parameterChange(ParameterChangeEvent event) {
   String paramName = event.getParameterName();
   if(paramName.equalsIgnoreCase(this.SITE_NAME_PARAM_NAME)) {
     String siteName = (String) this.siteNameParam.getValue();
     // if add site is selected, show window to add a site
     if(siteName.equalsIgnoreCase(this.ADD_SITE)) new AddEditPaleoSite();
     else setSiteInfo(siteName);
   }

 }

 /**
  * this is JUST A FAKE IMPLEMENTATION. IT SHOULD GET ALL SITE NAMES FROM
  * the DATABASE
  * @return
  */
 private ArrayList getSiteNames() {
   this.makeFaultNamesList();
   this.makeSiteLocationList();
   siteNamesList = new ArrayList();
   siteNamesList.add("	Airport Creek	");
   siteNamesList.add("	Alabama Gates	");
   siteNamesList.add("	Alder Creek	");
   siteNamesList.add("	Alegria Canyon	");
   siteNamesList.add("	Anza	");
   siteNamesList.add("	Arano Flat	");
   siteNamesList.add("	Archae Camp	");
   siteNamesList.add("	Arroyo Simi	");
   siteNamesList.add("	Bartholomaus Ranch	");
   siteNamesList.add("	Batdorf	");
   siteNamesList.add("	Bean Hill	");
   siteNamesList.add("	Bee Canyon	");
   siteNamesList.add("	Beebe Ranch	");
   siteNamesList.add("	Bidart fan	");
   siteNamesList.add("	Biskra Palms	");
   siteNamesList.add("	Bodega Harbor	");
   siteNamesList.add("	Bodick Road	");
   siteNamesList.add("	Bolinas Lagoon	");
   siteNamesList.add("	Burro Flat	");
   siteNamesList.add("	Camino-Palmero	");
   siteNamesList.add("	Camp Rock graben	");
   siteNamesList.add("	Castac Lake	");
   siteNamesList.add("	Cave Spring Wash	");
   siteNamesList.add("	Cholame Valley	");
   siteNamesList.add("	City Creek	");
   siteNamesList.add("	Clam Beach	");
   siteNamesList.add("	Coronado Bridge	");
   siteNamesList.add("	Cuddy Valley	");
   siteNamesList.add("	Cuesta	");
   siteNamesList.add("	Doda Ranch	");
   siteNamesList.add("	Dogtown	");
   siteNamesList.add("	Drainage Divide	");
   siteNamesList.add("	Dunsmore Canyon	");
   siteNamesList.add("	Eaton Wash	");
   siteNamesList.add("	El Paso Peaks	");
   siteNamesList.add("	Ellsworth	");
   siteNamesList.add("	Fan	");
   siteNamesList.add("	Fern Flat	");
   siteNamesList.add("	Ferrum	");
   siteNamesList.add("	Filoli	");
   siteNamesList.add("	Fish Creek Basin	");
   siteNamesList.add("	Frazier Mountain	");
   siteNamesList.add("	Fremont City Hall	");
   siteNamesList.add("	Galindo Creek	");
   siteNamesList.add("	Glen Ivy Marsh	");
   siteNamesList.add("	Goler Wash	");
   siteNamesList.add("	Grizzly Flat	");
   siteNamesList.add("	Hog Lake	");
   siteNamesList.add("	Hondo	");
   siteNamesList.add("	Huntington	");
   siteNamesList.add("	Imler Road	");
   siteNamesList.add("	Indian Creek fan (T-2)	");
   siteNamesList.add("	Indio	");
   siteNamesList.add("	Ingley	");
   siteNamesList.add("	JPL	");
   siteNamesList.add("	Kink Canyon	");
   siteNamesList.add("	LA County Arboretum	");
   siteNamesList.add("	Lake Henshaw	");
   siteNamesList.add("	Laughlin Range	");
   siteNamesList.add("	Laughlin Road	");
   siteNamesList.add("	Leyden Creek	");
   siteNamesList.add("	Little Salmon Creek	");
   siteNamesList.add("	Littlerock	");
   siteNamesList.add("	Loma Alta Park	");
   siteNamesList.add("	Lone Pine Creek	");
   siteNamesList.add("	Lone Tree Canyon	");
   siteNamesList.add("	Long Valley Creek	");
   siteNamesList.add("	Lopes Ranch	");
   siteNamesList.add("	Los Angeles Harbor	");
   siteNamesList.add("	Lost Lake	");
   siteNamesList.add("	LY4	");
   siteNamesList.add("	Manley Peak Canyon	");
   siteNamesList.add("	Marble Creek (T-1)	");
   siteNamesList.add("	Masonic Home	");
   siteNamesList.add("	Melendy Ranch	");
   siteNamesList.add("	Melville Gap	");
   siteNamesList.add("	Mil Potrero (San Emigdio Creek)	");
   siteNamesList.add("	Mill Canyon	");
   siteNamesList.add("	Mira Vista	");
   siteNamesList.add("	Mits	");
   siteNamesList.add("	Montclair Park	");
   siteNamesList.add("	Moss Beach	");
   siteNamesList.add("	Murrieta	");
   siteNamesList.add("	Murrieta Creek	");
   siteNamesList.add("	Mustard	");
   siteNamesList.add("	no name 1	");
   siteNamesList.add("	no name 2	");
   siteNamesList.add("	no name 3	");
   siteNamesList.add("	no name 4	");
   siteNamesList.add("	no name 5	");
   siteNamesList.add("	no name 6	");
   siteNamesList.add("	no name 7	");
   siteNamesList.add("	no name 8	");
   siteNamesList.add("	no name 9	");
   siteNamesList.add("	no name 10	");
   siteNamesList.add("	no name 11	");
   siteNamesList.add("	no name 12	");
   siteNamesList.add("	no name 13	");
   siteNamesList.add("	no name 14	");
   siteNamesList.add("	no name 15	");
   siteNamesList.add("	no name 16	");
   siteNamesList.add("	no name 17	");
   siteNamesList.add("	no name 18	");
   siteNamesList.add("	no name 19	");
   siteNamesList.add("	no name 20	");
   siteNamesList.add("	no name 21	");
   siteNamesList.add("	no name 22	");
   siteNamesList.add("	no name 23	");
   siteNamesList.add("	no name 24	");
   siteNamesList.add("	no name 25	");
   siteNamesList.add("	no name 26	");
   siteNamesList.add("	no name 27	");
   siteNamesList.add("	no name 28	");
   siteNamesList.add("	no name 29	");
   siteNamesList.add("	no name (All American Canal)	");
   siteNamesList.add("	no name (Ano Nuevo?)	");
   siteNamesList.add("	no name (Blue Lake)	");
   siteNamesList.add("	no name (Cedar Springs Dam)	");
   siteNamesList.add("	no name (Christmas Canyon)	");
   siteNamesList.add("	no name (City Creek North Branch)	");
   siteNamesList.add("	no name (Crystal Springs Reservoir area)	");
   siteNamesList.add("	no name (Day Canyon)	");
   siteNamesList.add("	no name (East Etiwanda Canyon)	");
   siteNamesList.add("	no name (East Etiwanda Canyon)	");
   siteNamesList.add("	no name (Evergreen Valley College)	");
   siteNamesList.add("	no name (Fish Springs)	");
   siteNamesList.add("	no name (Howell Creek)	");
   siteNamesList.add("	no name (Keough Hot Springs)	");
   siteNamesList.add("	no name (Koehn Lake)	");
   siteNamesList.add("	no name (Lake Pillsbury)	");
   siteNamesList.add("	no name (Lanom)	");
   siteNamesList.add("	no name (Lavic Lake Playa)	");
   siteNamesList.add("	no name (Lavic Lake Playa)	");
   siteNamesList.add("	no name (McGee Creek)	");
   siteNamesList.add("	no name (Ocotillo Ridge)	");
   siteNamesList.add("	no name (Pajaro River)	");
   siteNamesList.add("	no name (Point Pinole)	");
   siteNamesList.add("	no name (Shepherd Creek)	");
   siteNamesList.add("	no name (Talmadge)	");
   siteNamesList.add("	no name (Tinemaha Reservoir)	");
   siteNamesList.add("	no name (US-Mexico Border)	");
   siteNamesList.add("	no name (west of City Creek)	");
   siteNamesList.add("	Oak Creek Canyon	");
   siteNamesList.add("	Oak Flat	");
   siteNamesList.add("	Oak Hill	");
   siteNamesList.add("	Oak Knoll Creek	");
   siteNamesList.add("	Old Ghost alluvial fan complex	");
   siteNamesList.add("	Old Kane Springs Road	");
   siteNamesList.add("	Olinda Oil Field	");
   siteNamesList.add("	Pallett Creek	");
   siteNamesList.add("	Phelan Creek	");
   siteNamesList.add("	Phelan fan	");
   siteNamesList.add("	Pitman Canyon	");
   siteNamesList.add("	Playa 1	");
   siteNamesList.add("	Playa 2	");
   siteNamesList.add("	Plunge Creek	");
   siteNamesList.add("	Quaker	");
   siteNamesList.add("	Rabbit Spring	");
   siteNamesList.add("	Rancho San Marcos	");
   siteNamesList.add("	Sag	");
   siteNamesList.add("	Salt Creek	");
   siteNamesList.add("	San Dimas	");
   siteNamesList.add("	San Marino High School	");
   siteNamesList.add("	San Ysidro Creek	");
   siteNamesList.add("	Scaramella Ranch	");
   siteNamesList.add("	School Road	");
   siteNamesList.add("	SDG&E	");
   siteNamesList.add("	Searles Valley	");
   siteNamesList.add("	Shilo Ranch	");
   siteNamesList.add("	Sierra Madre Boulevard	");
   siteNamesList.add("	Smith Flat	");
   siteNamesList.add("	Soggy Lake playa	");
   siteNamesList.add("	Solstice Canyon	");
   siteNamesList.add("	Southern Searles Valley	");
   siteNamesList.add("	Springville	");
   siteNamesList.add("	Stone Ring Gullies	");
   siteNamesList.add("	Sunnyslope Reservoir	");
   siteNamesList.add("	Sylmar filtration plant	");
   siteNamesList.add("	T-3	");
   siteNamesList.add("	T-4	");
   siteNamesList.add("	T93-1	");
   siteNamesList.add("	T93-2a	");
   siteNamesList.add("	T93-2b	");
   siteNamesList.add("	T93-3	");
   siteNamesList.add("	Tapo Canyon	");
   siteNamesList.add("	Tecuya Fan	");
   siteNamesList.add("	Temecula	");
   siteNamesList.add("	Thousand Palms Oasis	");
   siteNamesList.add("	Thrust	");
   siteNamesList.add("	Triangle G	");
   siteNamesList.add("	Tule Pond	");
   siteNamesList.add("	Twin Lakes	");
   siteNamesList.add("	Upp Creek	");
   siteNamesList.add("	Upper Landis Trench (ULT)	");
   siteNamesList.add("	Vedanta Wind Gap	");
   siteNamesList.add("	Veterans Hospital	");
   siteNamesList.add("	Vincent Thomas Bridge	");
   siteNamesList.add("	Wallace Creek	");
   siteNamesList.add("	Waverly Road	");
   siteNamesList.add("	Welch Creek	");
   siteNamesList.add("	Western	");
   siteNamesList.add("	Willow Creek scarp	");
   siteNamesList.add("	Wilson Creek	");
   siteNamesList.add("	Winfield Ranch	");
   siteNamesList.add("	Winter Mesa	");
   siteNamesList.add("	Wrightwood	");
   siteNamesList.add("	Yorba Linda	");
   siteNamesList.add(TEST_SITE);
   siteNamesList.add(ADD_SITE);
   return siteNamesList;
 }


 private void makeFaultNamesList() {
   faultNamesList = new ArrayList();
   faultNamesList.add("	San Simeon 		");
   faultNamesList.add("	Owens Valley  		");
   faultNamesList.add("	San Andreas  		");
   faultNamesList.add("	Santa Ynez  		");
   faultNamesList.add("	San Jacinto  		");
   faultNamesList.add("	San Andreas  		");
   faultNamesList.add("	San Andreas  		");
   faultNamesList.add("	Simi-Santa Rosa  		");
   faultNamesList.add("	Sierra Madre  		");
   faultNamesList.add("	Johnson Valley  		");
   faultNamesList.add("	Zayante-Vergeles  		");
   faultNamesList.add("	Elsinore  		");
   faultNamesList.add("	Rodgers Creek 		");
   faultNamesList.add("	San Andreas  		");
   faultNamesList.add("	San Andreas  		");
   faultNamesList.add("	San Andreas  		");
   faultNamesList.add("	Johnson Valley  		");
   faultNamesList.add("	San Andreas  		");
   faultNamesList.add("	San Andreas  		");
   faultNamesList.add("	Hollywood 		");
   faultNamesList.add("	Camp Rock-Emerson  		");
   faultNamesList.add("	Garlock  		");
   faultNamesList.add("	Garlock  		");
   faultNamesList.add("	San Andreas  		");
   faultNamesList.add("	San Andreas  		");
   faultNamesList.add("	Mad River  		");
   faultNamesList.add("	Newport-Inglewood-Rose Canyon  		");
   faultNamesList.add("	San Andreas  		");
   faultNamesList.add("	Los Osos  		");
   faultNamesList.add("	San Andreas  		");
   faultNamesList.add("	San Andreas  		");
   faultNamesList.add("	Lavic Lake 		");
   faultNamesList.add("	Sierra Madre  		");
   faultNamesList.add("	Raymond 		");
   faultNamesList.add("	Garlock  		");
   faultNamesList.add("	Los Osos  		");
   faultNamesList.add("	Homestead Valley  		");
   faultNamesList.add("	Zayante-Vergeles  		");
   faultNamesList.add("	San Andreas  		");
   faultNamesList.add("	San Andreas  		");
   faultNamesList.add("	San Jacinto  		");
   faultNamesList.add("	San Andreas  		");
   faultNamesList.add("	Hayward  		");
   faultNamesList.add("	Concord 		");
   faultNamesList.add("	Elsinore  		");
   faultNamesList.add("	Panamint Valley  		");
   faultNamesList.add("	San Andreas  		");
   faultNamesList.add("	San Jacinto  		");
   faultNamesList.add("	Johnson Valley  		");
   faultNamesList.add("	Newport-Inglewood-Rose Canyon  		");
   faultNamesList.add("	San Jacinto  		");
   faultNamesList.add("	Fish Lake Valley  		");
   faultNamesList.add("	San Andreas  		");
   faultNamesList.add("	Los Osos  		");
   faultNamesList.add("	Sierra Madre  		");
   faultNamesList.add("	Pleito  		");
   faultNamesList.add("	Raymond 		");
   faultNamesList.add("	Elsinore  		");
   faultNamesList.add("	Maacama  		");
   faultNamesList.add("	Greenville  		");
   faultNamesList.add("	Calaveras  		");
   faultNamesList.add("	Little Salmon  		");
   faultNamesList.add("	San Andreas  		");
   faultNamesList.add("	Sierra Madre  		");
   faultNamesList.add("	Owens Valley  		");
   faultNamesList.add("	Garlock  		");
   faultNamesList.add("	Honey Lake  		");
   faultNamesList.add("	Green Valley 		");
   faultNamesList.add("	Palos Verdes  		");
   faultNamesList.add("	San Andreas  		");
   faultNamesList.add("	San Andreas  		");
   faultNamesList.add("	Panamint Valley  		");
   faultNamesList.add("	Fish Lake Valley  		");
   faultNamesList.add("	Hayward  		");
   faultNamesList.add("	San Andreas  		");
   faultNamesList.add("	Johnson Valley  		");
   faultNamesList.add("	San Andreas  		");
   faultNamesList.add("	San Andreas  		");
   faultNamesList.add("	Hayward  		");
   faultNamesList.add("	North Frontal thrust system		");
   faultNamesList.add("	Hayward  		");
   faultNamesList.add("	San Gregorio  		");
   faultNamesList.add("	Elsinore  		");
   faultNamesList.add("	Elsinore  		");
   faultNamesList.add("	Elsinore  		");
   faultNamesList.add("	Cordelia  		");
   faultNamesList.add("	Greenville  		");
   faultNamesList.add("	Greenville  		");
   faultNamesList.add("	Elsinore  		");
   faultNamesList.add("	Mohawk Valley  		");
   faultNamesList.add("	Elsinore  		");
   faultNamesList.add("	Elsinore  		");
   faultNamesList.add("	San Jacinto  		");
   faultNamesList.add("	San Jacinto  		");
   faultNamesList.add("	Little Salmon  		");
   faultNamesList.add("	Fish Slough 		");
   faultNamesList.add("	Hunting Creek-Berryessa  system		");
   faultNamesList.add("	Ortigalita  		");
   faultNamesList.add("	Ortigalita  		");
   faultNamesList.add("	Ortigalita  		");
   faultNamesList.add("	Ortigalita  		");
   faultNamesList.add("	Ortigalita  		");
   faultNamesList.add("	Ortigalita  		");
   faultNamesList.add("	Monte Vista-Shannon  		");
   faultNamesList.add("	Maacama  		");
   faultNamesList.add("	Owl Lake 		");
   faultNamesList.add("	Garlock  		");
   faultNamesList.add("	Healdsburg 		");
   faultNamesList.add("	Healdsburg 		");
   faultNamesList.add("	Mono Lake 		");
   faultNamesList.add("	Elsinore  		");
   faultNamesList.add("	Pisgah-Bullion  		");
   faultNamesList.add("	Pisgah-Bullion  		");
   faultNamesList.add("	Mesquite Lake 		");
   faultNamesList.add("	Imperial 		");
   faultNamesList.add("	San Gregorio  		");
   faultNamesList.add("	Mad River  		");
   faultNamesList.add("	Cleghorn  		");
   faultNamesList.add("	Garlock  		");
   faultNamesList.add("	San Andreas  		");
   faultNamesList.add("	San Andreas  		");
   faultNamesList.add("	Sierra Madre  		");
   faultNamesList.add("	Sierra Madre  		");
   faultNamesList.add("	Sierra Madre  		");
   faultNamesList.add("	Hayward  		");
   faultNamesList.add("	Owens Valley  		");
   faultNamesList.add("	Maacama  		");
   faultNamesList.add("	Owens Valley  		");
   faultNamesList.add("	Garlock  		");
   faultNamesList.add("	Bartlett Springs  system		");
   faultNamesList.add("	Johnson Valley  		");
   faultNamesList.add("	Lavic Lake 		");
   faultNamesList.add("	Lavic Lake 		");
   faultNamesList.add("	Hilton Creek 		");
   faultNamesList.add("	North Frontal thrust system		");
   faultNamesList.add("	Sargent  		");
   faultNamesList.add("	Hayward  		");
   faultNamesList.add("	Owens Valley  		");
   faultNamesList.add("	Maacama  		");
   faultNamesList.add("	Owens Valley  		");
   faultNamesList.add("	Imperial 		");
   faultNamesList.add("	San Andreas  		");
   faultNamesList.add("	Garlock  		");
   faultNamesList.add("	San Andreas  		");
   faultNamesList.add("	Sierra Madre  		");
   faultNamesList.add("	San Simeon 		");
   faultNamesList.add("	Black Mountain  		");
   faultNamesList.add("	San Jacinto  		");
   faultNamesList.add("	Elsinore  		");
   faultNamesList.add("	San Andreas  		");
   faultNamesList.add("	San Andreas  		");
   faultNamesList.add("	San Andreas  		");
   faultNamesList.add("	San Andreas  		");
   faultNamesList.add("	Camp Rock-Emerson  		");
   faultNamesList.add("	Homestead Valley  		");
   faultNamesList.add("	San Andreas  		");
   faultNamesList.add("	Owens Valley  		");
   faultNamesList.add("	Helendale-South Lockhart  		");
   faultNamesList.add("	Santa Ynez  		");
   faultNamesList.add("	Elsinore  		");
   faultNamesList.add("	San Andreas  		");
   faultNamesList.add("	Sierra Madre  		");
   faultNamesList.add("	Raymond 		");
   faultNamesList.add("	Calaveras  		");
   faultNamesList.add("	San Andreas  		");
   faultNamesList.add("	Mad River  		");
   faultNamesList.add("	Newport-Inglewood-Rose Canyon  		");
   faultNamesList.add("	Garlock  		");
   faultNamesList.add("	Rodgers Creek 		");
   faultNamesList.add("	Raymond 		");
   faultNamesList.add("	San Andreas  		");
   faultNamesList.add("	Lenwood-Lockhart  		");
   faultNamesList.add("	Malibu Coast  		");
   faultNamesList.add("	Garlock  		");
   faultNamesList.add("	Simi-Santa Rosa  		");
   faultNamesList.add("	San Andreas  		");
   faultNamesList.add("	Raymond 		");
   faultNamesList.add("	Sierra Madre  		");
   faultNamesList.add("	Fish Lake Valley  		");
   faultNamesList.add("	Fish Lake Valley  		");
   faultNamesList.add("	Fish Lake Valley  		");
   faultNamesList.add("	Fish Lake Valley  		");
   faultNamesList.add("	Fish Lake Valley  		");
   faultNamesList.add("	Fish Lake Valley  		");
   faultNamesList.add("	Sierra Madre  		");
   faultNamesList.add("	Pleito  		");
   faultNamesList.add("	Elsinore  		");
   faultNamesList.add("	San Andreas  		");
   faultNamesList.add("	Homestead Valley  		");
   faultNamesList.add("	Rodgers Creek 		");
   faultNamesList.add("	Hayward  		");
   faultNamesList.add("	Garlock  		");
   faultNamesList.add("	Maacama  		");
   faultNamesList.add("	San Andreas  		");
   faultNamesList.add("	San Andreas  		");
   faultNamesList.add("	Santa Monica 		");
   faultNamesList.add("	Palos Verdes  		");
   faultNamesList.add("	San Andreas  		");
   faultNamesList.add("	Helendale-South Lockhart  		");
   faultNamesList.add("	Calaveras  		");
   faultNamesList.add("	Pleito  		");
   faultNamesList.add("	Black Mountain  		");
   faultNamesList.add("	San Andreas  		");
   faultNamesList.add("	Calaveras  		");
   faultNamesList.add("	Malibu Coast  		");
   faultNamesList.add("	San Andreas  		");
   faultNamesList.add("	Elsinore  		");
   faultNamesList.add("Test Fault 1");
 }

 private void makeSiteLocationList() {
   siteLocationsList = new ArrayList();
   siteLocationsList.add(new Location(35.6509, -121.204));
   siteLocationsList.add(new Location(36.6653, -118.093));
   siteLocationsList.add(new Location(38.998, -123.688));
   siteLocationsList.add(new Location(34.4707, -120.271));
   siteLocationsList.add(new Location(33.5906, -116.667));
   siteLocationsList.add(new Location(36.9418, -121.673));
   siteLocationsList.add(new Location(38.5156, -123.231));
   siteLocationsList.add(new Location(34.2789, -118.803));
   siteLocationsList.add(new Location(34.2856, -118.384));
   siteLocationsList.add(new Location(34.3122, -116.456));
   siteLocationsList.add(new Location(37.019, -121.844));
   siteLocationsList.add(new Location(33.8844, -117.723));
   siteLocationsList.add(new Location(38.2695, -122.541));
   siteLocationsList.add(new Location(35.2332, -119.786));
   siteLocationsList.add(new Location(33.7837, -116.243));
   siteLocationsList.add(new Location(38.3136, -123.031));
   siteLocationsList.add(new Location(34.3372, -116.451));
   siteLocationsList.add(new Location(37.9339, -122.698));
   siteLocationsList.add(new Location(33.9988, -116.858));
   siteLocationsList.add(new Location(34.104, -118.349));
   siteLocationsList.add(new Location(34.6671, -116.69));
   siteLocationsList.add(new Location(34.8427, -118.831));
   siteLocationsList.add(new Location(35.5854, -116.437));
   siteLocationsList.add(new Location(35.7552, -120.306));
   siteLocationsList.add(new Location(34.1373, -117.191));
   siteLocationsList.add(new Location(40.9322, -124.125));
   siteLocationsList.add(new Location(32.6885, -117.162));
   siteLocationsList.add(new Location(34.8322, -119.036));
   siteLocationsList.add(new Location(35.2736, -120.715));
   siteLocationsList.add(new Location(38.5047, -123.221));
   siteLocationsList.add(new Location(37.9483, -122.712));
   siteLocationsList.add(new Location(34.5357, -116.261));
   siteLocationsList.add(new Location(34.2473, -118.253));
   siteLocationsList.add(new Location(34.1304, -118.082));
   siteLocationsList.add(new Location(35.4443, -117.68));
   siteLocationsList.add(new Location(35.2686, -120.707));
   siteLocationsList.add(new Location(34.4537, -116.499));
   siteLocationsList.add(new Location(37.0302, -121.87));
   siteLocationsList.add(new Location(33.4628, -115.86));
   siteLocationsList.add(new Location(37.4722, -122.315));
   siteLocationsList.add(new Location(32.9983, -115.945));
   siteLocationsList.add(new Location(34.8122, -118.902));
   siteLocationsList.add(new Location(37.5503, -121.969));
   siteLocationsList.add(new Location(37.9661, -122.031));
   siteLocationsList.add(new Location(33.7669, -117.487));
   siteLocationsList.add(new Location(35.8539, -117.167));
   siteLocationsList.add(new Location(37.0423, -121.791));
   siteLocationsList.add(new Location(33.6149, -116.709));
   siteLocationsList.add(new Location(34.2426, -116.436));
   siteLocationsList.add(new Location(33.6913, -118.023));
   siteLocationsList.add(new Location(32.9276, -115.697));
   siteLocationsList.add(new Location(37.776, -118.175));
   siteLocationsList.add(new Location(33.7413, -116.186));
   siteLocationsList.add(new Location(35.2764, -120.715));
   siteLocationsList.add(new Location(34.2022, -118.168));
   siteLocationsList.add(new Location(34.9312, -118.949));
   siteLocationsList.add(new Location(34.1386, -118.057));
   siteLocationsList.add(new Location(33.2027, -116.717));
   siteLocationsList.add(new Location(39.3406, -123.307));
   siteLocationsList.add(new Location(37.737, -121.715));
   siteLocationsList.add(new Location(37.5113, -121.833));
   siteLocationsList.add(new Location(40.6557, -124.188));
   siteLocationsList.add(new Location(34.487, -117.957));
   siteLocationsList.add(new Location(34.2037, -118.158));
   siteLocationsList.add(new Location(36.6085, -118.076));
   siteLocationsList.add(new Location(35.2059, -118.087));
   siteLocationsList.add(new Location(40.0529, -120.122));
   siteLocationsList.add(new Location(38.1314, -122.121));
   siteLocationsList.add(new Location(33.7287, -118.254));
   siteLocationsList.add(new Location(34.2722, -117.463));
   siteLocationsList.add(new Location(35.4692, -120.019));
   siteLocationsList.add(new Location(35.9055, -117.185));
   siteLocationsList.add(new Location(37.747, -118.157));
   siteLocationsList.add(new Location(37.5961, -122.004));
   siteLocationsList.add(new Location(36.5838, -121.176));
   siteLocationsList.add(new Location(34.4375, -116.591));
   siteLocationsList.add(new Location(34.8551, -119.138));
   siteLocationsList.add(new Location(36.9466, -121.679));
   siteLocationsList.add(new Location(37.9307, -122.296));
   siteLocationsList.add(new Location(34.3634, -116.884));
   siteLocationsList.add(new Location(37.8286, -122.21));
   siteLocationsList.add(new Location(37.5206, -122.512));
   siteLocationsList.add(new Location(33.5444, -117.193));
   siteLocationsList.add(new Location(33.5145, -117.176));
   siteLocationsList.add(new Location(33.4116, -117.042));
   siteLocationsList.add(new Location(38.2235, -122.135));
   siteLocationsList.add(new Location(37.7233, -121.708));
   siteLocationsList.add(new Location(37.665, -121.649));
   siteLocationsList.add(new Location(33.6117, -117.282));
   siteLocationsList.add(new Location(39.6629, -120.488));
   siteLocationsList.add(new Location(32.791, -116.048));
   siteLocationsList.add(new Location(32.4521, -115.622));
   siteLocationsList.add(new Location(34.0586, -117.283));
   siteLocationsList.add(new Location(33.1131, -116.071));
   siteLocationsList.add(new Location(40.5495, -124.086));
   siteLocationsList.add(new Location(37.3804, -118.377));
   siteLocationsList.add(new Location(38.8531, -122.391));
   siteLocationsList.add(new Location(37.1076, -121.134));
   siteLocationsList.add(new Location(36.8747, -120.991));
   siteLocationsList.add(new Location(36.8563, -120.973));
   siteLocationsList.add(new Location(36.7661, -120.899));
   siteLocationsList.add(new Location(36.9921, -121.096));
   siteLocationsList.add(new Location(36.9758, -121.074));
   siteLocationsList.add(new Location(37.3095, -122.063));
   siteLocationsList.add(new Location(39.1944, -123.197));
   siteLocationsList.add(new Location(35.6088, -116.868));
   siteLocationsList.add(new Location(35.5722, -116.297));
   siteLocationsList.add(new Location(38.6433, -122.865));
   siteLocationsList.add(new Location(38.6301, -122.86));
   siteLocationsList.add(new Location(37.9436, -119.108));
   siteLocationsList.add(new Location(33.9502, -117.688));
   siteLocationsList.add(new Location(34.4842, -116.257));
   siteLocationsList.add(new Location(34.4808, -116.254));
   siteLocationsList.add(new Location(34.2272, -116.07));
   siteLocationsList.add(new Location(32.6756, -115.358));
   siteLocationsList.add(new Location(37.1214, -122.312));
   siteLocationsList.add(new Location(40.8746, -124.013));
   siteLocationsList.add(new Location(34.3032, -117.317));
   siteLocationsList.add(new Location(35.5242, -117.365));
   siteLocationsList.add(new Location(34.1473, -117.189));
   siteLocationsList.add(new Location(37.5616, -122.393));
   siteLocationsList.add(new Location(34.1723, -117.546));
   siteLocationsList.add(new Location(34.1692, -117.542));
   siteLocationsList.add(new Location(34.1675, -117.528));
   siteLocationsList.add(new Location(37.3015, -121.76));
   siteLocationsList.add(new Location(37.0739, -118.268));
   siteLocationsList.add(new Location(39.1083, -123.154));
   siteLocationsList.add(new Location(37.337, -118.393));
   siteLocationsList.add(new Location(35.3714, -117.851));
   siteLocationsList.add(new Location(39.4547, -122.96));
   siteLocationsList.add(new Location(34.3493, -116.45));
   siteLocationsList.add(new Location(34.6768, -116.362));
   siteLocationsList.add(new Location(34.6654, -116.356));
   siteLocationsList.add(new Location(37.5612, -118.784));
   siteLocationsList.add(new Location(34.4283, -117.18));
   siteLocationsList.add(new Location(36.922, -121.536));
   siteLocationsList.add(new Location(37.9943, -122.359));
   siteLocationsList.add(new Location(36.7577, -118.118));
   siteLocationsList.add(new Location(39.1357, -123.164));
   siteLocationsList.add(new Location(36.9912, -118.221));
   siteLocationsList.add(new Location(32.6748, -115.357));
   siteLocationsList.add(new Location(34.1504, -117.208));
   siteLocationsList.add(new Location(35.0354, -118.398));
   siteLocationsList.add(new Location(34.7255, -118.603));
   siteLocationsList.add(new Location(34.2969, -118.4));
   siteLocationsList.add(new Location(35.6621, -121.214));
   siteLocationsList.add(new Location(36.5051, -116.873));
   siteLocationsList.add(new Location(33.0949, -116.053));
   siteLocationsList.add(new Location(33.9307, -117.844));
   siteLocationsList.add(new Location(34.4556, -117.886));
   siteLocationsList.add(new Location(35.2592, -119.814));
   siteLocationsList.add(new Location(35.2531, -119.807));
   siteLocationsList.add(new Location(34.2544, -117.433));
   siteLocationsList.add(new Location(34.5115, -116.525));
   siteLocationsList.add(new Location(34.4494, -116.497));
   siteLocationsList.add(new Location(34.1159, -117.138));
   siteLocationsList.add(new Location(36.6396, -118.083));
   siteLocationsList.add(new Location(34.4593, -116.967));
   siteLocationsList.add(new Location(34.5465, -119.863));
   siteLocationsList.add(new Location(33.4017, -117.038));
   siteLocationsList.add(new Location(33.4472, -115.841));
   siteLocationsList.add(new Location(34.1291, -117.799));
   siteLocationsList.add(new Location(34.1272, -118.097));
   siteLocationsList.add(new Location(37.0226, -121.492));
   siteLocationsList.add(new Location(38.9822, -123.676));
   siteLocationsList.add(new Location(40.9385, -124.113));
   siteLocationsList.add(new Location(32.8107, -117.218));
   siteLocationsList.add(new Location(35.5204, -117.385));
   siteLocationsList.add(new Location(38.5262, -122.761));
   siteLocationsList.add(new Location(34.1251, -118.106));
   siteLocationsList.add(new Location(34.9108, -119.351));
   siteLocationsList.add(new Location(34.4568, -116.7));
   siteLocationsList.add(new Location(34.0358, -118.752));
   siteLocationsList.add(new Location(35.5459, -117.265));
   siteLocationsList.add(new Location(34.2237, -119.082));
   siteLocationsList.add(new Location(33.5701, -115.98));
   siteLocationsList.add(new Location(34.1295, -118.086));
   siteLocationsList.add(new Location(34.3054, -118.486));
   siteLocationsList.add(new Location(37.4227, -117.866));
   siteLocationsList.add(new Location(37.4532, -117.89));
   siteLocationsList.add(new Location(37.5056, -117.937));
   siteLocationsList.add(new Location(37.5652, -118.005));
   siteLocationsList.add(new Location(37.5694, -118.009));
   siteLocationsList.add(new Location(37.4876, -117.953));
   siteLocationsList.add(new Location(34.3558, -118.715));
   siteLocationsList.add(new Location(34.9318, -118.957));
   siteLocationsList.add(new Location(33.476, -117.114));
   siteLocationsList.add(new Location(33.8369, -116.308));
   siteLocationsList.add(new Location(34.4242, -116.483));
   siteLocationsList.add(new Location(38.2735, -122.545));
   siteLocationsList.add(new Location(37.5585, -121.974));
   siteLocationsList.add(new Location(34.987, -118.506));
   siteLocationsList.add(new Location(39.4295, -123.366));
   siteLocationsList.add(new Location(40.0313, -124.06));
   siteLocationsList.add(new Location(38.0316, -122.787));
   siteLocationsList.add(new Location(34.0485, -118.45));
   siteLocationsList.add(new Location(33.7495, -118.272));
   siteLocationsList.add(new Location(35.2713, -119.826));
   siteLocationsList.add(new Location(34.4855, -116.999));
   siteLocationsList.add(new Location(37.5343, -121.85));
   siteLocationsList.add(new Location(34.9333, -118.966));
   siteLocationsList.add(new Location(36.0738, -116.733));
   siteLocationsList.add(new Location(34.0657, -116.994));
   siteLocationsList.add(new Location(36.741, -121.313));
   siteLocationsList.add(new Location(34.0337, -118.698));
   siteLocationsList.add(new Location(34.3701, -117.667));
   siteLocationsList.add(new Location(33.8815, -117.713));
   siteLocationsList.add(new Location(0.0,0.0));

 }


  public static void main(String[] args) {
    ViewPaleoSites viewPaleoSites = new ViewPaleoSites();
  }
}
