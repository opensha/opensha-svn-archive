package org.opensha.refFaultParamDb.gui;

import javax.swing.*;
import java.util.ArrayList;
import org.opensha.param.*;
import org.opensha.param.editor.*;
import java.awt.Container;
import java.awt.*;
import ch.randelshofer.quaqua.QuaquaManager;
import org.opensha.param.editor.ConstrainedStringParameterEditor;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;


/**
 * <p>Title: AddPaleoSite.java </p>
 * <p>Description:  GUI to allow the user to add a new paleo site or edit a exisitng
 * paleo site. </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class AddEditPaleoSite extends JFrame implements ActionListener {

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
  private final static String TITLE = "Add New Site";


  // input parameters declaration
  private StringParameter siteNameParam;
  private LocationParameter siteLocationParam;
  private StringParameter assocWithFaultParam;
  private StringParameter siteTypeParam;
  private StringParameter siteRepresentationParam;

  // input parameter editors
  private StringParameterEditor siteNameParamEditor;
  private LocationParameterEditor siteLocationParamEditor;
  private ConstrainedStringParameterEditor assocWithFaultParamEditor;
  private ConstrainedStringParameterEditor siteTypeParamEditor;
  private ConstrainedStringParameterEditor siteRepresentationParamEditor;


  // various buttons in thos window
  private JButton addNewSiteButton = new JButton("Add New Site Type");
  private JButton okButton = new JButton("OK");
  private JButton cancelButton = new JButton("Cancel");


  public AddEditPaleoSite() {
    try {
      // initialize the parameters and editors
      initParametersAndEditors();
      // add the editors and buttons to the window
      jbInit();
      this.setTitle(TITLE);
      // add listeners for the buttons in this window
      addActionListeners();
    }catch(Exception e) {
      e.printStackTrace();
    }
    this.pack();
    this.show();
  }

  // add action listeners on the buttons in this window
  private void addActionListeners() {
    this.addNewSiteButton.addActionListener(this);
  }

  /**
   * Whenever user presses a button on this window, this function is called
   * @param event
   */
  public void actionPerformed(ActionEvent event) {
    // if it is "Add New Site" request, pop up another window to fill the new site type
     if(event.getSource()==this.addNewSiteButton)
        new AddNewSiteType();
  }

  /**
   * Add the editors and buttons to the window
   */
  private void jbInit() {
    Container contentPane = this.getContentPane();
    contentPane.setLayout(new GridBagLayout());
    // site name editor
    contentPane.add(siteNameParamEditor,  new GridBagConstraints(0, 0, 2, 1, 1.0, 1.0
        ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));
    // site location
    contentPane.add(siteLocationParamEditor,  new GridBagConstraints(0, 1, 2, 1, 1.0, 1.0
        ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));
    // associated with fault
    contentPane.add(assocWithFaultParamEditor,  new GridBagConstraints(0, 2, 2, 1, 1.0, 1.0
        ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));
    // site types
    contentPane.add(siteTypeParamEditor,  new GridBagConstraints(0, 3, 1, 1, 1.0, 1.0
        ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));
    // add new site type
    contentPane.add(addNewSiteButton,  new GridBagConstraints(1, 3, 1, 1, 1.0, 1.0
        ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
    // how representative is this site
    contentPane.add(siteRepresentationParamEditor,  new GridBagConstraints(0, 4, 2, 1, 1.0, 1.0
        ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));
    // ok button
    contentPane.add(okButton,  new GridBagConstraints(0, 5, 1, 1, 1.0, 1.0
        ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
    // cancel button
    contentPane.add(cancelButton,  new GridBagConstraints(1, 5, 1, 1, 1.0, 1.0
        ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
  }



  /**
   * Initialize all the parameters and the editors
   */
  private void initParametersAndEditors() throws Exception {
    // parameter so that user can enter the site name
   siteNameParam = new StringParameter(SITE_NAME_PARAM_NAME);
   siteNameParamEditor = new StringParameterEditor(siteNameParam);

   // Site Location(Lat/lon/depth)
   siteLocationParam = new LocationParameter(SITE_LOCATION_PARAM_NAME, LAT_PARAM_NAME,
                                             LON_PARAM_NAME, DEPTH_PARAM_NAME,
                                             DEFAULT_LAT_VAL,
                                             DEFAULT_LON_VAL, DEFAULT_DEPTH_VAL);
   siteLocationParamEditor = new LocationParameterEditor(siteLocationParam,true);

   // choose the fault with which this site is associated
   ArrayList faultNamesList = getFaultNames();
   assocWithFaultParam = new StringParameter(ASSOCIATED_WITH_FAULT_PARAM_NAME, faultNamesList,
                                              (String)faultNamesList.get(0));
   assocWithFaultParamEditor = new ConstrainedStringParameterEditor(assocWithFaultParam);

   // available study types
   ArrayList siteTypes = getSiteTypes();
   siteTypeParam = new StringParameter(SITE_TYPE_PARAM_NAME, siteTypes,
                                              (String)siteTypes.get(0));
    siteTypeParamEditor = new ConstrainedStringParameterEditor(siteTypeParam);

   // how representative is this site?
   ArrayList siteRepresentations = getSiteRepresentations();
   siteRepresentationParam = new StringParameter(SITE_REPRESENTATION_PARAM_NAME, siteRepresentations,
                                              (String)siteRepresentations.get(0));
   siteRepresentationParamEditor = new ConstrainedStringParameterEditor(siteRepresentationParam);
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
    siteTypesList.add("Trench");
    siteTypesList.add("Geologic");
    siteTypesList.add("Survey/Cultural");
    return siteTypesList;
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


  public static void main(String[] args) {
    AddEditPaleoSite addPaleoSite = new AddEditPaleoSite();
  }
}