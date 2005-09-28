package org.opensha.refFaultParamDb.gui.addEdit;

import javax.swing.*;
import java.util.ArrayList;
import org.opensha.param.*;
import org.opensha.param.event.*;
import org.opensha.param.editor.*;
import java.awt.Container;
import org.opensha.data.Location;
import java.awt.*;
import ch.randelshofer.quaqua.QuaquaManager;
import org.opensha.param.editor.ConstrainedStringParameterEditor;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import org.opensha.refFaultParamDb.gui.*;
import org.opensha.refFaultParamDb.gui.infotools.GUI_Utils;
import org.opensha.gui.LabeledBoxPanel;
import org.opensha.exceptions.*;
import org.opensha.exceptions.*;
import org.opensha.exceptions.*;
import org.opensha.refFaultParamDb.dao.db.SiteTypeDB_DAO;
import org.opensha.refFaultParamDb.dao.db.DB_AccessAPI;
import org.opensha.refFaultParamDb.dao.db.ReferenceDB_DAO;
import org.opensha.refFaultParamDb.vo.SiteType;
import org.opensha.refFaultParamDb.vo.Reference;
import org.opensha.refFaultParamDb.dao.db.SiteRepresentationDB_DAO;
import org.opensha.refFaultParamDb.vo.SiteRepresentation;
import org.opensha.refFaultParamDb.vo.PaleoSite;
import org.opensha.refFaultParamDb.gui.infotools.SessionInfo;
import org.opensha.refFaultParamDb.dao.db.PaleoSiteDB_DAO;
import org.opensha.refFaultParamDb.dao.db.FaultDB_DAO;
import org.opensha.refFaultParamDb.vo.Fault;
import org.opensha.refFaultParamDb.dao.exception.InsertException;


/**
 * <p>Title: AddPaleoSite.java </p>
 * <p>Description:  GUI to allow the user to add a new paleo site or edit an exisitng
 * paleo site. </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class AddEditSiteCharacteristics extends JFrame implements ActionListener, ParameterChangeListener {

  // various input parameter names
  private final static String SITE_NAME_PARAM_NAME="Site Name";
  private final static String OLD_SITE_ID_PARAM_NAME="Old Site Id";
  private final static String SITE_LOCATION_PARAM_NAME="Site Location";
  private final static String COMMENTS_PARAM_NAME = "Comments";
  private final static String CHOOSE_REFERENCE_PARAM_NAME = "Choose Reference";
  private final static String ASSOCIATED_WITH_FAULT_PARAM_NAME="Associated With Fault";
  private final static String SITE_TYPE_PARAM_NAME="Site Type";
  private final static String SITE_REPRESENTATION_PARAM_NAME="How Representative is this Site";

  // params for entering a site
  private final static String LAT_PARAM_NAME="Site Latitude";
  private final static String LON_PARAM_NAME="Site Longitude";
  private final static String ELEVATION_PARAM_NAME="Site Elevation";
  private final static double DEFAULT_LAT_VAL=34.00;
  private final static double DEFAULT_LON_VAL=-118.0;
  private final static double DEFAULT_ELEVATION_VAL=2.0;

  private final static String TITLE = "Add/Edit Paleo Site";
  private final static String BETWEEN_LOCATIONS_SITE_TYPE = "Between Locations";
  private final static String LAT_LON_UNITS = "Decimal Degrees";
  private final static String ELEVATION_UNITS = "km";
  private final static int WIDTH = 400;
  private final static int HEIGHT = 700;

  // various messages
  private final static String MSG_COMMENTS_MISSING = "Please Enter Comments";
  private final static String MSG_REFERENCES_MISSING = "Please choose atleast 1 reference";
  private final static String MSG_INSERT_SUCCESS = "Site added sucessfully to the database";
  private final static String MSG_UPDATE_SUCCESS = "Site updated sucessfully in the database";


  // input parameters declaration
  private StringParameter siteNameParam;
  private LocationParameter siteLocationParam;
  private LocationParameter siteLocationParam2;
  private StringParameter assocWithFaultParam;
  private StringParameter siteTypeParam;
  private StringParameter siteRepresentationParam;
  private StringListParameter siteReferenceParam;
  private StringParameter commentsParam;
  private StringParameter oldSiteIdParam;

  // input parameter editors
  private StringParameterEditor siteNameParamEditor;
  private LocationParameterEditor siteLocationParamEditor;
  private LocationParameterEditor siteLocationParamEditor2;
  private ConstrainedStringParameterEditor assocWithFaultParamEditor;
  private ConstrainedStringParameterEditor siteTypeParamEditor;
  private ConstrainedStringParameterEditor siteRepresentationParamEditor;
  private ConstrainedStringListParameterEditor siteReferenceParamEditor;
  private CommentsParameterEditor commentsParamEditor;
  private StringParameterEditor oldSiteIdParamEditor;


  // various buttons in thos window
  private JButton addNewSiteButton = new JButton("Add New Site Type");
  private JButton okButton = new JButton("OK");
  private JButton cancelButton = new JButton("Cancel");
  private JButton addNewReferenceButton = new JButton("Add New Reference");
  private final static String addNewReferenceToolTipText = "Add Reference not currently in database";

  // site type DAO
  private SiteTypeDB_DAO siteTypeDAO = new SiteTypeDB_DAO(DB_AccessAPI.dbConnection);
  // references DAO
  private ReferenceDB_DAO referenceDAO = new ReferenceDB_DAO(DB_AccessAPI.dbConnection);
  // site representations DAO
  private SiteRepresentationDB_DAO siteRepresentationDAO = new SiteRepresentationDB_DAO(DB_AccessAPI.dbConnection);
  // paleo site DAO
  private PaleoSiteDB_DAO paleoSiteDAO = new PaleoSiteDB_DAO(DB_AccessAPI.dbConnection);
  // fault DAO
  private FaultDB_DAO faultDAO = new FaultDB_DAO(DB_AccessAPI.dbConnection);
  private boolean isEdit = false;
  private PaleoSite paleoSiteVO;

  /**
   * This constructor allows the editing of an existing site
   *
   * @param isEdit
   * @param paleoSite
   */
  public AddEditSiteCharacteristics(boolean isEdit, PaleoSite paleoSite) {
    this.isEdit = isEdit;
    if(isEdit) this.paleoSiteVO = paleoSite;
    try {
      // initialize the parameters and editors
      initParametersAndEditors();
      // add the editors and buttons to the window
      jbInit();
      this.setTitle(TITLE);
      // add listeners for the buttons in this window
      addActionListeners();
      // show/not show second site location
      setSecondLocationVisible();
    }catch(Exception e) {
      e.printStackTrace();
    }
    this.pack();
    setSize(this.WIDTH, this.HEIGHT);
    this.setLocationRelativeTo(null);
    this.show();
  }

  /**
   * this constructor can be used if a new site has to be added
   */
  public AddEditSiteCharacteristics() {
    this(false, null);
  }

  // add action listeners on the buttons in this window
  private void addActionListeners() {
    this.addNewSiteButton.addActionListener(this);
    addNewReferenceButton.addActionListener(this);
    addNewReferenceButton.setToolTipText(this.addNewReferenceToolTipText);
    okButton.addActionListener(this);
    cancelButton.addActionListener(this);
  }

  /**
   * Whenever user presses a button on this window, this function is called
   * @param event
   */
  public void actionPerformed(ActionEvent event) {
    Object source  = event.getSource();
    // if it is "Add New Site" request, pop up another window to fill the new site type
     if(source==this.addNewSiteButton) new AddNewSiteType();
     else if(source == addNewReferenceButton) new AddNewReference();
     else if(source == okButton) {
       putSiteInDatabase();
     }
     else if (source==cancelButton) {
       this.dispose();
     }
  }

  /**
   * Put the site into the database
   */
  private void putSiteInDatabase() {
    PaleoSite paleoSite = new PaleoSite();
    // set the site Id to update a existing site
    /**
     * There is always insertion operation in database. Even in case of update,
     * a new row is entered into database but site id is retained. This insertion allows
     * us to hold the multiple versions.
     */
    if(this.isEdit) paleoSite.setSiteId(this.paleoSiteVO.getSiteId());

    paleoSite.setSiteName((String)this.siteNameParam.getValue());
    String comments = (String)this.commentsParam.getValue();
    // user must provide comments
    if(comments==null || comments.trim().equalsIgnoreCase("")) {
      JOptionPane.showMessageDialog(this, MSG_COMMENTS_MISSING);
      return;
    }
    paleoSite.setGeneralComments(comments);
    paleoSite.setOldSiteId((String)this.oldSiteIdParam.getValue());
    paleoSite.setFaultName((String)this.assocWithFaultParam.getValue());
    // see that user chooses at least 1 site reference
    ArrayList siteReferences = (ArrayList)this.siteReferenceParam.getValue();
    if(siteReferences==null || siteReferences.size()==0) {
      JOptionPane.showMessageDialog(this, MSG_REFERENCES_MISSING);
      return;
    }
    paleoSite.setReferenceShortCitationList((ArrayList)this.siteReferenceParam.getValue());
    paleoSite.setRepresentativeStrandName((String)this.siteRepresentationParam.getValue());
    paleoSite.setSiteTypeName((String)this.siteTypeParam.getValue());

    // location 1
    Location location1 = (Location)siteLocationParam.getValue();
    paleoSite.setSiteLat1((float)location1.getLatitude());
    paleoSite.setSiteLon1((float)location1.getLongitude());
    paleoSite.setSiteElevation1((float)location1.getDepth());

    //location 2
    Location location2 = (Location)siteLocationParam2.getValue();
    paleoSite.setSiteLat2((float)location2.getLatitude());
    paleoSite.setSiteLon2((float)location2.getLongitude());
    paleoSite.setSiteElevation2((float)location2.getDepth());
    try {
      // add the paleo site to the database
      paleoSiteDAO.addPaleoSite(paleoSite);

      // show the success message to the user
      String msg;
      if(this.isEdit) msg = this.MSG_UPDATE_SUCCESS;
      else msg = this.MSG_INSERT_SUCCESS;
      JOptionPane.showMessageDialog(this,msg);
      this.dispose();
    }catch(InsertException e) {
      JOptionPane.showMessageDialog(this, e.getMessage());
    }
  }

  /**
   * Add the editors and buttons to the window
   */
  private void jbInit() {
    LabeledBoxPanel labeledBoxPanel = new LabeledBoxPanel(GUI_Utils.gridBagLayout);
    labeledBoxPanel.setTitle(TITLE);
    int yPos = 0;
    // site name editor
    labeledBoxPanel.add(siteNameParamEditor,  new GridBagConstraints(0, yPos++, 2, 1, 1.0, 1.0
        ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));
    // old site id
    labeledBoxPanel.add(this.oldSiteIdParamEditor,  new GridBagConstraints(0, yPos++, 2, 1, 1.0, 1.0
        ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));
    // site location
    labeledBoxPanel.add(siteLocationParamEditor,  new GridBagConstraints(0, yPos++, 2, 1, 1.0, 1.0
        ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));
    // associated with fault
    labeledBoxPanel.add(assocWithFaultParamEditor,  new GridBagConstraints(0, yPos++, 2, 1, 1.0, 1.0
        ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));
    // site types
    labeledBoxPanel.add(siteTypeParamEditor,  new GridBagConstraints(0, yPos, 1, 1, 1.0, 1.0
        ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));
    // add new site type
    labeledBoxPanel.add(addNewSiteButton,  new GridBagConstraints(1, yPos++, 1, 1, 1.0, 1.0
        ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
    // site location 2
    labeledBoxPanel.add(siteLocationParamEditor2,  new GridBagConstraints(0, yPos++, 2, 1, 1.0, 1.0
        ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));
    // how representative is this site
    labeledBoxPanel.add(siteRepresentationParamEditor,  new GridBagConstraints(0, yPos++, 2, 1, 1.0, 1.0
        ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));
    // comments
     labeledBoxPanel.add(this.commentsParamEditor,  new GridBagConstraints(0, yPos++, 2, 1, 1.0, 1.0
         ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));
    // references
   labeledBoxPanel.add(this.siteReferenceParamEditor,  new GridBagConstraints(0, yPos++, 2, 1, 1.0, 1.0
       ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));
   // references
   labeledBoxPanel.add(this.addNewReferenceButton,  new GridBagConstraints(0, yPos++, 1, 1, 1.0, 1.0
       ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
   // ok button
   labeledBoxPanel.add(okButton,  new GridBagConstraints(0, yPos, 1, 1, 1.0, 1.0
       ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
   // cancel button
   labeledBoxPanel.add(cancelButton,  new GridBagConstraints(1, yPos++, 1, 1, 1.0, 1.0
       ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));

    Container contentPane = this.getContentPane();
    contentPane.setLayout(GUI_Utils.gridBagLayout);
    contentPane.add(labeledBoxPanel,  new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
        ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));

  }



  /**
   * Initialize all the parameters and the editors
   */
  private void initParametersAndEditors() throws Exception {

    String defaultSiteName, defaultOldSiteId, defaultFaultName, defaultSiteType;
    String defaultSiteRepresentation, defaultComments;
    ArrayList dafaultReference;
    Location defaultLocation1, defaultLocation2;

    // get various lists from the database
    ArrayList faultNamesList = getFaultNames();
    ArrayList siteTypes = getSiteTypes();
    ArrayList siteRepresentations = getSiteRepresentations();
    ArrayList referencesList = this.getAvailableReferences();

    if(this.isEdit) { // if site is to be edit, default values are current values for that site
      defaultSiteName = this.paleoSiteVO.getSiteName();
      defaultOldSiteId = paleoSiteVO.getOldSiteId();
      defaultFaultName = paleoSiteVO.getFaultName();
      defaultSiteType = paleoSiteVO.getSiteTypeName();
      defaultSiteRepresentation = paleoSiteVO.getRepresentativeStrandName();
      defaultComments = paleoSiteVO.getGeneralComments();
      dafaultReference = paleoSiteVO.getReferenceShortCitationList();
      defaultLocation1 = new Location(paleoSiteVO.getSiteLat1(), paleoSiteVO.getSiteLon1(),
                                      paleoSiteVO.getSiteElevation1());
      defaultLocation2 = new Location(this.paleoSiteVO.getSiteLat1(), paleoSiteVO.getSiteLon2(),
                                      paleoSiteVO.getSiteElevation2());
    } else { // if a new site has to be added, set some default values
      defaultSiteName =" ";
      defaultOldSiteId = " ";
      defaultFaultName = (String)faultNamesList.get(0);
      defaultSiteType = (String)siteTypes.get(0);
      defaultSiteRepresentation = (String)siteRepresentations.get(0);
      defaultComments = " ";
      dafaultReference = new ArrayList();
      defaultLocation1 = new Location(this.DEFAULT_LAT_VAL, this.DEFAULT_LON_VAL,
                                      this.DEFAULT_ELEVATION_VAL);
      defaultLocation2 = new Location(this.DEFAULT_LAT_VAL, this.DEFAULT_LON_VAL,
                                      this.DEFAULT_ELEVATION_VAL);
    }


    // parameter so that user can enter the site name
    siteNameParam = new StringParameter(SITE_NAME_PARAM_NAME, defaultSiteName);
    siteNameParamEditor = new StringParameterEditor(siteNameParam);

    // parameter so that user can enter a site Id
   oldSiteIdParam = new StringParameter(OLD_SITE_ID_PARAM_NAME,defaultOldSiteId);
   oldSiteIdParamEditor = new StringParameterEditor(oldSiteIdParam);

   // site location parameter
   siteLocationParam = createLocationParam(defaultLocation1);
   siteLocationParamEditor = new LocationParameterEditor(siteLocationParam,true);

   // second site location, in "Between Locations" is selected as the Site type
   siteLocationParam2 = createLocationParam(defaultLocation2);
   siteLocationParamEditor2 = new LocationParameterEditor(siteLocationParam2,true);

   // choose the fault with which this site is associated
   assocWithFaultParam = new StringParameter(ASSOCIATED_WITH_FAULT_PARAM_NAME, faultNamesList,
                                              defaultFaultName);
   assocWithFaultParamEditor = new ConstrainedStringParameterEditor(assocWithFaultParam);

   // available study types
   siteTypeParam = new StringParameter(SITE_TYPE_PARAM_NAME, siteTypes,
                                       defaultSiteType);
   siteTypeParamEditor = new ConstrainedStringParameterEditor(siteTypeParam);
   siteTypeParam.addParameterChangeListener(this);

   // how representative is this site?
   siteRepresentationParam = new StringParameter(SITE_REPRESENTATION_PARAM_NAME, siteRepresentations,
                                              defaultSiteRepresentation);
   siteRepresentationParamEditor = new ConstrainedStringParameterEditor(siteRepresentationParam);

   // references for this site
   this.siteReferenceParam = new StringListParameter(this.CHOOSE_REFERENCE_PARAM_NAME,
       referencesList, dafaultReference);
    this.siteReferenceParamEditor = new ConstrainedStringListParameterEditor(siteReferenceParam);

   // user comments
   this.commentsParam = new StringParameter(COMMENTS_PARAM_NAME,defaultComments);
   this.commentsParamEditor = new CommentsParameterEditor(commentsParam);


  }

  /**
   * create location parameter
   *
   * @throws InvalidRangeException
   * @throws ParameterException
   * @throws ConstraintException
   */
  private LocationParameter createLocationParam(Location loc) throws InvalidRangeException,
      ParameterException, ConstraintException {
    //creating the Location parameterlist for the Site
    DoubleParameter siteLocLatParam = new DoubleParameter(LAT_PARAM_NAME,
        Location.MIN_LAT,Location.MAX_LAT,LAT_LON_UNITS,new Double(loc.getLatitude()));
    DoubleParameter siteLocLonParam = new DoubleParameter(LON_PARAM_NAME,
        Location.MIN_LON,Location.MAX_LON,LAT_LON_UNITS, new Double(loc.getLongitude()));
    DoubleParameter siteLocElevationParam = new DoubleParameter(ELEVATION_PARAM_NAME,
        Location.MIN_DEPTH, Double.MAX_VALUE, ELEVATION_UNITS, new Double(loc.getDepth()));
    ParameterList siteLocParamList = new ParameterList();
    siteLocParamList.addParameter(siteLocLatParam);
    siteLocParamList.addParameter(siteLocLonParam);
    siteLocParamList.addParameter(siteLocElevationParam);
    Location siteLoc = new Location(loc.getLatitude(),
                                    loc.getLongitude(),
                                    loc.getDepth());

    // Site Location(Lat/lon/)
    return (new LocationParameter(SITE_LOCATION_PARAM_NAME,siteLocParamList,
        siteLoc));
  }


  /**
   * If site type added is "BETWEEN LOCATIONS", then  allow the user to enter
   * the second location
   *
   * @param event
   */
  public void parameterChange(ParameterChangeEvent event) {
    if(event.getParameterName().equalsIgnoreCase(this.SITE_TYPE_PARAM_NAME))
      setSecondLocationVisible();
  }

  /**
   * If site type added is "BETWEEN LOCATIONS", then  allow the user to enter
   * the second location
   *
   */
  private void setSecondLocationVisible() {
    String selectedSiteType =  (String)this.siteTypeParam.getValue();
    if(selectedSiteType.equalsIgnoreCase(this.BETWEEN_LOCATIONS_SITE_TYPE))
      this.siteLocationParamEditor2.setVisible(true);
    else this.siteLocationParamEditor2.setVisible(false);
  }


  /**
  * Get the site representations.
  * It gets the SITE REPRSENTATIONS from the database
  *
  * @return
  */
 private ArrayList getSiteRepresentations() {
   ArrayList siteRepresentationVOs = siteRepresentationDAO.getAllSiteRepresentations();
   ArrayList siteRepresentations = new ArrayList();
   for(int i=0; i<siteRepresentationVOs.size(); ++i) {
     siteRepresentations.add(((SiteRepresentation)siteRepresentationVOs.get(i)).getSiteRepresentationName());
   }
   return siteRepresentations;
 }

 /**
  * Get a list of available references. It gets this from the database.
  * @return
  */
 private ArrayList getAvailableReferences() {
   return referenceDAO.getAllShortCitations();
 }

 /**
   * It gets all the FAULT NAMES from the database
   * @return
   */
  private ArrayList getFaultNames() {
    ArrayList faultVOs = faultDAO.getAllFaults();
    ArrayList faultNamesList = new ArrayList();
    for(int i=0; i<faultVOs.size(); ++i) {
      faultNamesList.add(((Fault)faultVOs.get(i)).getFaultName());
    }
    return faultNamesList;
  }

  /**
   * Get the study types.
   *  It gets all the SITE TYPES from the database
   *
   * @return
   */
  private ArrayList getSiteTypes() {
    ArrayList siteTypeVOs = siteTypeDAO.getAllSiteTypes();
    ArrayList siteTypesList = new ArrayList();
    for(int i=0; i<siteTypeVOs.size(); ++i)
      siteTypesList.add(((SiteType)siteTypeVOs.get(i)).getSiteType());
    return siteTypesList;
  }
}
