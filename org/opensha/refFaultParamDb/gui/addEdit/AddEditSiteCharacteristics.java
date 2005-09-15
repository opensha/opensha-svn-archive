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


  public AddEditSiteCharacteristics() {
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

  // add action listeners on the buttons in this window
  private void addActionListeners() {
    this.addNewSiteButton.addActionListener(this);
    addNewReferenceButton.addActionListener(this);
    addNewReferenceButton.setToolTipText(this.addNewReferenceToolTipText);
  }

  /**
   * Whenever user presses a button on this window, this function is called
   * @param event
   */
  public void actionPerformed(ActionEvent event) {
    // if it is "Add New Site" request, pop up another window to fill the new site type
     if(event.getSource()==this.addNewSiteButton) new AddNewSiteType();
     else if(event.getSource() == addNewReferenceButton) new AddNewReference();
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
   siteNameParam = new StringParameter(SITE_NAME_PARAM_NAME);
   siteNameParamEditor = new StringParameterEditor(siteNameParam);

    // parameter so that user can enter a site Id
   oldSiteIdParam = new StringParameter(OLD_SITE_ID_PARAM_NAME);
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
   this.commentsParam = new StringParameter(COMMENTS_PARAM_NAME);
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
   * This is just a FAKE implementation. It should get all the SITE TYPES
   * from the database
   *
   * @return
   */
  private ArrayList getSiteTypes() {
    ArrayList siteTypesList = new ArrayList();
    siteTypesList.add(this.BETWEEN_LOCATIONS_SITE_TYPE);
    siteTypesList.add("Trench");
    siteTypesList.add("Geologic");
    siteTypesList.add("Survey/Cultural");
    return siteTypesList;
  }
}
