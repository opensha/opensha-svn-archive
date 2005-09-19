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
import org.opensha.refFaultParamDb.dao.SiteTypeDAO_API;
import org.opensha.refFaultParamDb.dao.db.SiteTypeDB_DAO;
import org.opensha.refFaultParamDb.dao.db.DB_AccessAPI;
import org.opensha.refFaultParamDb.dao.ReferenceDAO_API;
import org.opensha.refFaultParamDb.dao.db.ReferenceDB_DAO;
import org.opensha.refFaultParamDb.vo.SiteType;
import org.opensha.refFaultParamDb.vo.Reference;
import org.opensha.refFaultParamDb.dao.db.SiteRepresentationDB_DAO;
import org.opensha.refFaultParamDb.dao.SiteRepresentationDAO_API;
import org.opensha.refFaultParamDb.vo.SiteRepresentation;
import org.opensha.refFaultParamDb.vo.PaleoSite;
import org.opensha.refFaultParamDb.gui.infotools.SessionInfo;
import org.opensha.refFaultParamDb.dao.db.PaleoSiteDB_DAO;
import org.opensha.refFaultParamDb.dao.PaleoSiteDAO_API;
import org.opensha.refFaultParamDb.dao.FaultDAO_API;
import org.opensha.refFaultParamDb.dao.db.FaultDB_DAO;
import org.opensha.refFaultParamDb.vo.Fault;


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
  private final static Double DEFAULT_LAT_VAL=new Double(34.00);
  private final static Double DEFAULT_LON_VAL=new Double(-118.0);
  private final static Double DEFAULT_ELEVATION_VAL=new Double(2.0);

  private final static String TITLE = "Add/Edit Paleo Site";
  private final static String BETWEEN_LOCATIONS_SITE_TYPE = "Between Locations";
  private final static String LAT_LON_UNITS = "Decimal Degrees";
  private final static String ELEVATION_UNITS = "km";
  private final static int WIDTH = 400;
  private final static int HEIGHT = 700;

  private final static String MSG_COMMENTS_MISSING = "Please Enter Comments";


  // input parameters declaration
  private StringParameter siteNameParam;
  private LocationParameter siteLocationParam;
  private LocationParameter siteLocationParam2;
  private StringParameter assocWithFaultParam;
  private StringParameter siteTypeParam;
  private StringParameter siteRepresentationParam;
  private StringParameter siteReferenceParam;
  private StringParameter commentsParam;
  private StringParameter oldSiteIdParam;

  // input parameter editors
  private StringParameterEditor siteNameParamEditor;
  private LocationParameterEditor siteLocationParamEditor;
  private LocationParameterEditor siteLocationParamEditor2;
  private ConstrainedStringParameterEditor assocWithFaultParamEditor;
  private ConstrainedStringParameterEditor siteTypeParamEditor;
  private ConstrainedStringParameterEditor siteRepresentationParamEditor;
  private ConstrainedStringParameterEditor siteReferenceParamEditor;
  private CommentsParameterEditor commentsParamEditor;
  private StringParameterEditor oldSiteIdParamEditor;


  // various buttons in thos window
  private JButton addNewSiteButton = new JButton("Add New Site Type");
  private JButton okButton = new JButton("OK");
  private JButton cancelButton = new JButton("Cancel");
  private JButton addNewReferenceButton = new JButton("Add New Reference");
  private final static String addNewReferenceToolTipText = "Add Reference not currently in database";

  // site type DAO
  private SiteTypeDAO_API siteTypeDAO = new SiteTypeDB_DAO(DB_AccessAPI.dbConnection);
  // references DAO
  private ReferenceDAO_API referenceDAO = new ReferenceDB_DAO(DB_AccessAPI.dbConnection);
  // site representations DAO
  private SiteRepresentationDAO_API siteRepresentationDAO = new SiteRepresentationDB_DAO(DB_AccessAPI.dbConnection);
  // paleo site DAO
  private PaleoSiteDAO_API paleoSiteDAO = new PaleoSiteDB_DAO(DB_AccessAPI.dbConnection);
  // fault DAO
  private FaultDAO_API faultDAO = new FaultDB_DAO(DB_AccessAPI.dbConnection);
  private boolean isEdit = false;
  private PaleoSite paleoSiteVO;

  /**
   * This constructor allows the editing of an existing site
   *
   * @param isEdit
   * @param paleoSite
   */
  public AddEditSiteCharacteristics(boolean isEdit, PaleoSite paleoSite) {
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
  }

  /**
   * Whenever user presses a button on this window, this function is called
   * @param event
   */
  public void actionPerformed(ActionEvent event) {
    // if it is "Add New Site" request, pop up another window to fill the new site type
     if(event.getSource()==this.addNewSiteButton) new AddNewSiteType();
     else if(event.getSource() == addNewReferenceButton) new AddNewReference();
     else if(event.getSource() == okButton) putSiteInDatabase();
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
    if(comments==null || comments.trim().equalsIgnoreCase("")) {
      JOptionPane.showMessageDialog(this, MSG_COMMENTS_MISSING);
      return;
    }
    paleoSite.setGeneralComments(comments);
    paleoSite.setOldSiteId((String)this.oldSiteIdParam.getValue());
    paleoSite.setEntryComments(comments);
    paleoSite.setFaultName((String)this.assocWithFaultParam.getValue());
    paleoSite.setReferenceShortCitation((String)this.siteReferenceParam.getValue());
    paleoSite.setRepresentativeStrandName((String)this.siteRepresentationParam.getValue());
    paleoSite.setSiteContributor(SessionInfo.getContributor());
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

    // add the paleo site to the database
    paleoSiteDAO.addPaleoSite(paleoSite);
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
    // parameter so that user can enter the site name
   siteNameParam = new StringParameter(SITE_NAME_PARAM_NAME," ");
   siteNameParamEditor = new StringParameterEditor(siteNameParam);

    // parameter so that user can enter a site Id
   oldSiteIdParam = new StringParameter(OLD_SITE_ID_PARAM_NAME," ");
   oldSiteIdParamEditor = new StringParameterEditor(oldSiteIdParam);

   // site location parameter
   siteLocationParam = createLocationParam();
   siteLocationParamEditor = new LocationParameterEditor(siteLocationParam,true);

   // second site location, in "Between Locations" is selected as the Site type
   siteLocationParam2 = createLocationParam();
   siteLocationParamEditor2 = new LocationParameterEditor(siteLocationParam2,true);

   // choose the fault with which this site is associateda
   ArrayList faultNamesList = getFaultNames();
   assocWithFaultParam = new StringParameter(ASSOCIATED_WITH_FAULT_PARAM_NAME, faultNamesList,
                                              (String)faultNamesList.get(0));
   assocWithFaultParamEditor = new ConstrainedStringParameterEditor(assocWithFaultParam);

   // available study types
   ArrayList siteTypes = getSiteTypes();
   siteTypeParam = new StringParameter(SITE_TYPE_PARAM_NAME, siteTypes,
                                       (String)siteTypes.get(0));
   siteTypeParamEditor = new ConstrainedStringParameterEditor(siteTypeParam);
   siteTypeParam.addParameterChangeListener(this);

   // how representative is this site?
   ArrayList siteRepresentations = getSiteRepresentations();
   siteRepresentationParam = new StringParameter(SITE_REPRESENTATION_PARAM_NAME, siteRepresentations,
                                              (String)siteRepresentations.get(0));
   siteRepresentationParamEditor = new ConstrainedStringParameterEditor(siteRepresentationParam);

   // references for this site
   ArrayList referencesList = this.getAvailableReferences();
   this.siteReferenceParam = new StringParameter(this.CHOOSE_REFERENCE_PARAM_NAME,
       referencesList, (String)referencesList.get(0));
    this.siteReferenceParamEditor = new ConstrainedStringParameterEditor(siteReferenceParam);

   // user comments
   this.commentsParam = new StringParameter(COMMENTS_PARAM_NAME," ");
   this.commentsParamEditor = new CommentsParameterEditor(commentsParam);


  }

  /**
   * create location parameter
   *
   * @throws InvalidRangeException
   * @throws ParameterException
   * @throws ConstraintException
   */
  private LocationParameter createLocationParam() throws InvalidRangeException,
      ParameterException, ConstraintException {
    //creating the Location parameterlist for the Site
    DoubleParameter siteLocLatParam = new DoubleParameter(LAT_PARAM_NAME,
        Location.MIN_LAT,Location.MAX_LAT,LAT_LON_UNITS,DEFAULT_LAT_VAL);
    DoubleParameter siteLocLonParam = new DoubleParameter(LON_PARAM_NAME,
        Location.MIN_LON,Location.MAX_LON,LAT_LON_UNITS,DEFAULT_LON_VAL);
    DoubleParameter siteLocElevationParam = new DoubleParameter(ELEVATION_PARAM_NAME,
        Location.MIN_DEPTH, Double.MAX_VALUE, ELEVATION_UNITS,DEFAULT_ELEVATION_VAL);
    ParameterList siteLocParamList = new ParameterList();
    siteLocParamList.addParameter(siteLocLatParam);
    siteLocParamList.addParameter(siteLocLonParam);
    siteLocParamList.addParameter(siteLocElevationParam);
    Location siteLoc = new Location(DEFAULT_LAT_VAL.doubleValue(),
                                    DEFAULT_LON_VAL.doubleValue(),
                                    DEFAULT_ELEVATION_VAL.doubleValue());

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
   ArrayList referenceVOs = referenceDAO.getAllReferences();
   ArrayList referencesNamesList = new ArrayList();
   for(int i=0; i<referenceVOs.size(); ++i) {
     referencesNamesList.add(((Reference)referenceVOs.get(i)).getShortCitation());
   }
   return referencesNamesList;
 }

 /**
   *  It gets all the FAULT NAMES from the database
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
