package org.scec.sha.gui.controls;

import java.util.Vector;
import java.awt.event.*;


import org.scec.sha.PEER_TestsGroupResults.*;
import org.scec.sha.earthquake.rupForecastImpl.PEER_TestCases.*;
import org.scec.sha.gui.*;
import org.scec.param.*;
import org.scec.param.editor.*;
import org.scec.param.event.*;
import org.scec.sha.magdist.*;
import org.scec.sha.param.*;
import org.scec.sha.param.editor.*;
import org.scec.sha.earthquake.*;
import org.scec.sha.imr.*;
import org.scec.sha.imr.attenRelImpl.*;
import org.scec.data.function.*;
import org.scec.util.*;
import org.scec.data.*;
import org.scec.sha.gui.beans.*;
import org.scec.sha.param.editor.gui.SimpleFaultParameterEditorPanel;
import org.scec.sha.gui.controls.SetMinSourceSiteDistanceControlPanel;
import org.scec.calc.magScalingRelations.magScalingRelImpl.PEER_testsMagAreaRelationship;
import org.scec.sha.earthquake.rupForecastImpl.*;
import java.awt.*;
import javax.swing.*;


/**
 *
 * <p>Title: PEER_TestCaseSelectorControlPanel</p>
 * <p>Description: This class creates the a window that contains the
 * list of different PEER tests cases so that a user can make a selection.
 * This class also sets the default parameters for the selected test
 * in the HazardCurveApplet. </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author : Nitin Gupta, Vipin Gupta, and Ned Field
 * @created : Feb 24,2003
 * @version 1.0
 */
public class PEER_TestCaseSelectorControlPanel extends JFrame {


  protected final static String C = "PEER_TestCaseSelectorControlPanel";
  protected final static boolean D = false;


  //Supported PEER Test Cases
  public final static String PEER_TESTS_SET_ONE = "Set1";
  public final static String PEER_TESTS_SET_TWO = "Set2";

  //Test Cases and the Site Lists
  public final static String TEST_CASE_ONE ="Case1";
  public final static String TEST_CASE_TWO ="Case2";
  public final static String TEST_CASE_THREE ="Case3";
  public final static String TEST_CASE_FOUR ="Case4";
  public final static String TEST_CASE_FIVE ="Case5";
  public final static String TEST_CASE_SIX ="Case6";
  public final static String TEST_CASE_SEVEN ="Case7";
  public final static String TEST_CASE_EIGHT_ONE ="Case8-noTrunc";
  public final static String TEST_CASE_EIGHT_TWO ="Case8-2sigTrunc";
  public final static String TEST_CASE_EIGHT_THREE ="Case8-3sigTrunc";
  public final static String TEST_CASE_NINE_ONE ="Case9-Sa97";
  public final static String TEST_CASE_NINE_TWO ="Case9-AS97";
  public final static String TEST_CASE_NINE_THREE ="Case9-Ca97";
  public final static String TEST_CASE_TEN ="Case10";
  public final static String TEST_CASE_ELEVEN ="Case11";
  public final static String TEST_CASE_TWELVE ="Case12";


  //Sites Supported
  public final static String SITE_ONE = "a";
  public final static String SITE_TWO = "b";
  public final static String SITE_THREE = "c";
  public final static String SITE_FOUR = "d";
  public final static String SITE_FIVE = "e";
  public final static String SITE_SIX = "f";
  public final static String SITE_SEVEN = "g";

   /* maximum permitted distance between fault and site to consider source in
  hazard analysis for that site; this default value is to allow all PEER test
  cases to pass through
  */
  private double MAX_DISTANCE = 300;

  // some of the universal parameter settings
  private double GRID_SPACING = 1.0;
  private String FAULT_TYPE = SimpleFaultParameterEditorPanel.STIRLING;

  // various gui beans
  private IMT_GuiBean imtGuiBean;
  private IMR_GuiBean imrGuiBean;
  private Site_GuiBean siteGuiBean;
  private ERF_GuiBeanAPI erfGuiBean;
  private TimeSpanGuiBean timeSpanGuiBean;
  private SetMinSourceSiteDistanceControlPanel distanceControlPanel;

  //Stores the test case,
  private String selectedTest;
  private String selectedSite;
  private String selectedSet;
  private JLabel jLabel2 = new JLabel();
  private JComboBox testCaseComboBox = new JComboBox();
  private GridBagLayout gridBagLayout1 = new GridBagLayout();

  //Vector to store the peer test cases names
  private Vector peerTestSetOne = new Vector();
  private Vector peerTestSetTwo = new Vector();

  //These hold the lats, lons, dips, and depths of the faults used in the SimplePoissonFaultERF
  private Vector fault1and2_Lats, fault1and2_Lons, fault1_Dips, fault2_Dips, fault1_Depths, fault2_Depths;
  private Vector faultE_Lats, faultE_Lons, faultE_Dips, faultE_Depths;



  public PEER_TestCaseSelectorControlPanel(Component parent, IMR_GuiBean imrGuiBean,
                               Site_GuiBean siteGuiBean,
                               IMT_GuiBean imtGuiBean,
                               ERF_GuiBeanAPI erfGuiBean,
                               TimeSpanGuiBean timeSpanGuiBean,
                               SetMinSourceSiteDistanceControlPanel distanceControlPanel){

    if (D) System.out.println(C+" Constructor: starting initializeFaultData()");
    initializeFaultData();

    try {
     jbInit();
    }
    catch(Exception e) {
     e.printStackTrace();
    }

    //save the instances of the beans
    this.imrGuiBean = imrGuiBean;
    this.siteGuiBean = siteGuiBean;
    this.imtGuiBean = imtGuiBean;
    this.erfGuiBean = erfGuiBean;
    this.timeSpanGuiBean = timeSpanGuiBean;
    this.distanceControlPanel = distanceControlPanel;

    if (D) System.out.println(C+" Constructor: starting initializeTestsAndSites()");
    // fill the combo box with tests and sites
    initializeTestsAndSites();

    // show the window at center of the parent component
    setLocation(parent.getX()+parent.getWidth()/2,
                parent.getY()+parent.getHeight()/2);


  }


  private void jbInit() throws Exception {
    this.getContentPane().setLayout(gridBagLayout1);
    jLabel2.setForeground(new Color(80, 80, 133));
    jLabel2.setText("Select Test and Site:");
    testCaseComboBox.setBackground(new Color(200, 200, 230));
    testCaseComboBox.setForeground(new Color(80, 80, 133));
    testCaseComboBox.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        testCaseComboBox_actionPerformed(e);
      }
    });
    this.setTitle("PEER Test Case Selector");
    this.getContentPane().add(jLabel2,  new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(15, 7, 2, 240), 22, 5));
    this.getContentPane().add(testCaseComboBox,  new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(7, 145, 2, 13), 92, -1));
  }


  /**
   * This method extracts the selected Site and the selected TestCase set
   * @param testAndSite: Contains both the site and the Selected Test Cases Set
   */
  public void setTestCaseAndSite(String testAndSite){
    int firstIndex=testAndSite.indexOf("-");
    int lastIndex = testAndSite.lastIndexOf("-");
    selectedSet = testAndSite.substring(0,firstIndex);
    selectedTest = testAndSite.substring(firstIndex+1,lastIndex);
    selectedSite = testAndSite.substring(lastIndex+1);
    setParams();
  }

  /**
   * This function sets the site Paramters and the IMR parameters based on the
   * selected test case and selected site number for that test case
   */

  public void setParams() {
    String S = C + ":setParams()";
    if(D) System.out.println(S+"::entering");

    //Gets the siteParamList
    ParameterList siteParams = siteGuiBean.getParameterListEditor().getParameterList();

    // set the distance in control panel
    this.distanceControlPanel.setDistance(this.MAX_DISTANCE);

    //if set-1 PEER test case is selected
    if(selectedSet.equalsIgnoreCase(PEER_TESTS_SET_ONE))
      set_Set1Params(siteParams);

    //if set-2 PEER test case is selected
    else if(selectedSet.equalsIgnoreCase(PEER_TESTS_SET_TWO))
      set_Set2Params(siteParams);

    // refresh the editor according to parameter values
    imrGuiBean.synchToModel();
    imtGuiBean.synchToModel();
    siteGuiBean.getParameterListEditor().synchToModel();
    erfGuiBean.synchToModel();
    timeSpanGuiBean.synchToModel();
  }

  /**
  * sets the parameter values for the selected test cases in Set-1
  * @param siteParams
  */
 private void set_Set1Params(ParameterList siteParams){


   // ******* Set the IMR, IMT, & Site-Related Parameters (except lat and lon) first ************

   /*   the following settings apply to most test cases; these are subsequently
        overridded where needed below */
   imrGuiBean.getParameterList().getParameter(IMR_GuiBean.IMR_PARAM_NAME).setValue(SCEMY_1997_AttenRel.NAME);
   imrGuiBean.getParameterList().getParameter(AttenuationRelationship.SIGMA_TRUNC_TYPE_NAME).setValue(AttenuationRelationship.SIGMA_TRUNC_TYPE_NONE);
   imrGuiBean.getParameterList().getParameter(AttenuationRelationship.STD_DEV_TYPE_NAME).setValue(AttenuationRelationship.STD_DEV_TYPE_NONE);
   imtGuiBean.getParameterList().getParameter(IMT_GuiBean.IMT_PARAM_NAME).setValue(AttenuationRelationship.PGA_NAME);
   siteParams.getParameter(SCEMY_1997_AttenRel.SITE_TYPE_NAME).setValue(SCEMY_1997_AttenRel.SITE_TYPE_ROCK);

   //if the selected test case is number 8_1
   if(selectedTest.equals(TEST_CASE_EIGHT_ONE)){
     imrGuiBean.getParameterList().getParameter(AttenuationRelationship.SIGMA_TRUNC_TYPE_NAME).setValue(AttenuationRelationship.SIGMA_TRUNC_TYPE_NONE);
     imrGuiBean.getParameterList().getParameter(AttenuationRelationship.STD_DEV_TYPE_NAME).setValue(AttenuationRelationship.STD_DEV_TYPE_TOTAL);
   }

   //if the selected test case is number 8_2
   if(selectedTest.equals(TEST_CASE_EIGHT_TWO)){
     imrGuiBean.getParameterList().getParameter(AttenuationRelationship.SIGMA_TRUNC_TYPE_NAME).setValue(AttenuationRelationship.SIGMA_TRUNC_TYPE_1SIDED);
     imrGuiBean.getParameterList().getParameter(AttenuationRelationship.SIGMA_TRUNC_LEVEL_NAME).setValue(new Double(2.0));
     imrGuiBean.getParameterList().getParameter(AttenuationRelationship.STD_DEV_TYPE_NAME).setValue(AttenuationRelationship.STD_DEV_TYPE_TOTAL);
   }

   //if the selected test case is number 8_3
   if(selectedTest.equals(TEST_CASE_EIGHT_THREE)){
     imrGuiBean.getParameterList().getParameter(AttenuationRelationship.SIGMA_TRUNC_TYPE_NAME).setValue(AttenuationRelationship.SIGMA_TRUNC_TYPE_1SIDED);
     imrGuiBean.getParameterList().getParameter(AttenuationRelationship.SIGMA_TRUNC_LEVEL_NAME).setValue(new Double(3.0));
     imrGuiBean.getParameterList().getParameter(AttenuationRelationship.STD_DEV_TYPE_NAME).setValue(AttenuationRelationship.STD_DEV_TYPE_TOTAL);
   }

   //if the selected test case is number 9_1
   if(selectedTest.equals(TEST_CASE_NINE_ONE)){
     imrGuiBean.getParameterList().getParameter(IMR_GuiBean.IMR_PARAM_NAME).setValue(SCEMY_1997_AttenRel.NAME);
     imrGuiBean.getParameterList().getParameter(AttenuationRelationship.SIGMA_TRUNC_TYPE_NAME).setValue(AttenuationRelationship.SIGMA_TRUNC_TYPE_1SIDED);
     imrGuiBean.getParameterList().getParameter(AttenuationRelationship.SIGMA_TRUNC_LEVEL_NAME).setValue(new Double(3.0));
     imrGuiBean.getParameterList().getParameter(AttenuationRelationship.STD_DEV_TYPE_NAME).setValue(AttenuationRelationship.STD_DEV_TYPE_TOTAL);
     imtGuiBean.getParameterList().getParameter(IMT_GuiBean.IMT_PARAM_NAME).setValue(AttenuationRelationship.PGA_NAME);
     siteParams.getParameter(SCEMY_1997_AttenRel.SITE_TYPE_NAME).setValue(SCEMY_1997_AttenRel.SITE_TYPE_ROCK);
   }

   //if the selected test case is number 9_2
   if(selectedTest.equals(TEST_CASE_NINE_TWO)){
     imrGuiBean.getParameterList().getParameter(IMR_GuiBean.IMR_PARAM_NAME).setValue(AS_1997_AttenRel.NAME);
     imrGuiBean.getParameterList().getParameter(AttenuationRelationship.SIGMA_TRUNC_TYPE_NAME).setValue(AttenuationRelationship.SIGMA_TRUNC_TYPE_1SIDED);
     imrGuiBean.getParameterList().getParameter(AttenuationRelationship.SIGMA_TRUNC_LEVEL_NAME).setValue(new Double(3.0));
     imrGuiBean.getParameterList().getParameter(AttenuationRelationship.STD_DEV_TYPE_NAME).setValue(AttenuationRelationship.STD_DEV_TYPE_TOTAL);
     imtGuiBean.getParameterList().getParameter(IMT_GuiBean.IMT_PARAM_NAME).setValue(AttenuationRelationship.PGA_NAME);
     siteParams.getParameter(AS_1997_AttenRel.SITE_TYPE_NAME).setValue(AS_1997_AttenRel.SITE_TYPE_ROCK);
   }

   //if the selected test case is number 9_3
   if(selectedTest.equals(TEST_CASE_NINE_THREE)){
     imrGuiBean.getParameterList().getParameter(IMR_GuiBean.IMR_PARAM_NAME).setValue(Campbell_1997_AttenRel.NAME);
     imrGuiBean.getParameterList().getParameter(AttenuationRelationship.SIGMA_TRUNC_TYPE_NAME).setValue(AttenuationRelationship.SIGMA_TRUNC_TYPE_1SIDED);
     imrGuiBean.getParameterList().getParameter(AttenuationRelationship.SIGMA_TRUNC_LEVEL_NAME).setValue(new Double(3.0));
     imrGuiBean.getParameterList().getParameter(AttenuationRelationship.STD_DEV_TYPE_NAME).setValue(Campbell_1997_AttenRel.STD_DEV_TYPE_TOTAL_MAG_DEP);
     imtGuiBean.getParameterList().getParameter(IMT_GuiBean.IMT_PARAM_NAME).setValue(AttenuationRelationship.PGA_NAME);
     siteGuiBean.getParameterListEditor().getParameterList().getParameter(Campbell_1997_AttenRel.SITE_TYPE_NAME).setValue(Campbell_1997_AttenRel.SITE_TYPE_SOFT_ROCK);
     siteParams.getParameter(Campbell_1997_AttenRel.BASIN_DEPTH_NAME).setValue(new Double(2.0));
   }

   //if the selected test case is number 12
   if(selectedTest.equals(TEST_CASE_TWELVE)){
     imrGuiBean.getParameterList().getParameter(AttenuationRelationship.SIGMA_TRUNC_TYPE_NAME).setValue(AttenuationRelationship.SIGMA_TRUNC_TYPE_1SIDED);
     imrGuiBean.getParameterList().getParameter(AttenuationRelationship.SIGMA_TRUNC_LEVEL_NAME).setValue(new Double(3.0));
     imrGuiBean.getParameterList().getParameter(AttenuationRelationship.STD_DEV_TYPE_NAME).setValue(AttenuationRelationship.STD_DEV_TYPE_TOTAL);
   }


   // *********** Now fill in the ERF parameters ************************

   // if it's one of the "PEER fault" problems (cases 1-9 or 12)
   if(!selectedTest.equalsIgnoreCase(TEST_CASE_TEN) && !selectedTest.equalsIgnoreCase(TEST_CASE_ELEVEN)) {

     // set the ERF
     erfGuiBean.getParameterList().getParameter(ERF_GuiBean.ERF_PARAM_NAME).setValue(SimplePoissonFaultERF.NAME);

     // set the common parameters like timespan
     erfGuiBean.getParameterList().getParameter(SimplePoissonFaultERF.OFFSET_PARAM_NAME).setValue(new Double(1.0));
     erfGuiBean.getParameterList().getParameter(SimplePoissonFaultERF.MAG_SCALING_REL_PARAM_NAME).setValue(PEER_testsMagAreaRelationship.NAME);
     erfGuiBean.getParameterList().getParameter(SimplePoissonFaultERF.SIGMA_PARAM_NAME).setValue(new Double(0));
     erfGuiBean.getParameterList().getParameter(SimplePoissonFaultERF.ASPECT_RATIO_PARAM_NAME).setValue(new Double(2.0));
     erfGuiBean.getParameterList().getParameter(SimplePoissonFaultERF.MIN_MAG_PARAM_NAME).setValue(new Double(5.0));
     timeSpanGuiBean.getParameterList().getParameter(TimeSpan.DURATION).setValue(new Double(1.0));

     // magScalingSigma parameter is changed if the test case chosen is 3
     if(selectedTest.equals(TEST_CASE_THREE))
       erfGuiBean.getParameterList().getParameter(SimplePoissonFaultERF.SIGMA_PARAM_NAME).setValue(new Double(0.2));

     // set the rake for all cases
     if( selectedTest.equals(TEST_CASE_FOUR) ||
         selectedTest.equals(TEST_CASE_NINE_ONE) ||
         selectedTest.equals(TEST_CASE_NINE_TWO) ||
         selectedTest.equals(TEST_CASE_NINE_THREE) ) {
              erfGuiBean.getParameterList().getParameter(SimplePoissonFaultERF.RAKE_PARAM_NAME).setValue(new Double(90.0));
     }
     else {
       erfGuiBean.getParameterList().getParameter(SimplePoissonFaultERF.RAKE_PARAM_NAME).setValue(new Double(0.0));
     }

     // set the Fault Parameter
     SimpleFaultParameterEditorPanel faultPanel = erfGuiBean.getSimpleFaultParamEditor().getParameterEditorPanel();
     if( selectedTest.equals(TEST_CASE_FOUR) ||
        selectedTest.equals(TEST_CASE_NINE_ONE) ||
        selectedTest.equals(TEST_CASE_NINE_TWO) ||
        selectedTest.equals(TEST_CASE_NINE_THREE) ) {
             faultPanel.setAll(GRID_SPACING,fault1and2_Lats,fault1and2_Lons,fault2_Dips,fault2_Depths,FAULT_TYPE);
    }
    else {

      faultPanel.setAll(GRID_SPACING,fault1and2_Lats,fault1and2_Lons,fault1_Dips,fault1_Depths,FAULT_TYPE);
    }
    faultPanel.setEvenlyGriddedSurfaceFromParams();

   }

   // it's an area ERF (case 10 or 11)
   else {
     erfGuiBean.getParameterList().getParameter(ERF_GuiBean.ERF_PARAM_NAME).setValue(PEER_AreaForecast.NAME);

     erfGuiBean.getParameterList().getParameter(PEER_AreaForecast.DEPTH_UPPER_PARAM_NAME).setValue(new Double(5));
     erfGuiBean.getParameterList().getParameter(PEER_AreaForecast.DIP_PARAM_NAME).setValue(new Double(90));
     erfGuiBean.getParameterList().getParameter(PEER_AreaForecast.RAKE_PARAM_NAME).setValue(new Double(0));
     timeSpanGuiBean.getParameterList().getParameter(TimeSpan.DURATION).setValue(new Double(1.0));
     erfGuiBean.getParameterList().getParameter(PEER_AreaForecast.GRID_PARAM_NAME).setValue(new Double(1.0));
     if(selectedTest.equals(TEST_CASE_TEN))
       erfGuiBean.getParameterList().getParameter(PEER_AreaForecast.DEPTH_LOWER_PARAM_NAME).setValue(new Double(5));
     else
       erfGuiBean.getParameterList().getParameter(PEER_AreaForecast.DEPTH_LOWER_PARAM_NAME).setValue(new Double(10));
   }

   // set magFreqDist parameters using seperate method
   MagFreqDistParameterEditor magDistEditor = erfGuiBean.getMagDistEditor();
   setMagDistParams_Set1(magDistEditor);


   // *********** set the Site latitude and longitude  **************************

   if(!selectedTest.equalsIgnoreCase(TEST_CASE_TEN) && !selectedTest.equalsIgnoreCase(TEST_CASE_ELEVEN)) {

     // for fault site 1
     if(selectedSite.equals(SITE_ONE)) {
       siteGuiBean.getParameterListEditor().getParameterList().getParameter(Site_GuiBean.LATITUDE).setValue(new Double(38.113));
       siteParams.getParameter(Site_GuiBean.LONGITUDE).setValue(new Double(-122.0));
     }
     // for fault site 2
     if(selectedSite.equals(SITE_TWO)) {
       siteParams.getParameter(Site_GuiBean.LATITUDE).setValue(new Double(38.113));
       siteParams.getParameter(Site_GuiBean.LONGITUDE).setValue(new Double(-122.114));

     }
     // for fault site 3
     if(selectedSite.equals(SITE_THREE)) {
       siteParams.getParameter(Site_GuiBean.LATITUDE).setValue(new Double(38.111));
       siteParams.getParameter(Site_GuiBean.LONGITUDE).setValue(new Double(-122.570));

     }
     // for fault site 4
     if(selectedSite.equals(SITE_FOUR)) {
       siteParams.getParameter(Site_GuiBean.LATITUDE).setValue(new Double(38.000));
       siteParams.getParameter(Site_GuiBean.LONGITUDE).setValue(new Double(-122.0));

     }
     // for fault site 5
     if(selectedSite.equals(SITE_FIVE)) {
       siteParams.getParameter(Site_GuiBean.LATITUDE).setValue(new Double(37.910));
       siteParams.getParameter(Site_GuiBean.LONGITUDE).setValue(new Double(-122.0));

     }
     // for fault site 6
     if(selectedSite.equals(SITE_SIX)) {
       siteParams.getParameter(Site_GuiBean.LATITUDE).setValue(new Double(38.225));
       siteParams.getParameter(Site_GuiBean.LONGITUDE).setValue(new Double(-122.0));

     }
     // for fault site 7
     if(selectedSite.equals(SITE_SEVEN)) {
       siteParams.getParameter(Site_GuiBean.LATITUDE).setValue(new Double(38.113));
       siteParams.getParameter(Site_GuiBean.LONGITUDE).setValue(new Double(-121.886));
     }
   } else { // for area sites

     siteParams.getParameter(Site_GuiBean.LONGITUDE).setValue(new Double(-122.0));
     // for area site 1
     if(selectedSite.equals(SITE_ONE))
       siteParams.getParameter(Site_GuiBean.LATITUDE).setValue(new Double(38.0));

     // for area site 2
     if(selectedSite.equals(SITE_TWO))
       siteParams.getParameter(Site_GuiBean.LATITUDE).setValue(new Double(37.550));

     // for area site 3
     if(selectedSite.equals(SITE_THREE))
       siteParams.getParameter(Site_GuiBean.LATITUDE).setValue(new Double(37.099));

     // for area site 4
     if(selectedSite.equals(SITE_FOUR))
       siteParams.getParameter(Site_GuiBean.LATITUDE).setValue(new Double(36.874));
   }
 }



 /**
  * sets the parameter values for the selected test cases in Set-2
  * @param siteParams
  */
 private void set_Set2Params(ParameterList siteParams){


   // ******* Set the IMR, IMT, & Site-Related Parameters (except lat and lon) first ************

   imrGuiBean.getParameterList().getParameter(IMR_GuiBean.IMR_PARAM_NAME).setValue(SCEMY_1997_AttenRel.NAME);
   imrGuiBean.getParameterList().getParameter(AttenuationRelationship.SIGMA_TRUNC_TYPE_NAME).setValue(AttenuationRelationship.SIGMA_TRUNC_TYPE_NONE);
   imrGuiBean.getParameterList().getParameter(AttenuationRelationship.STD_DEV_TYPE_NAME).setValue(AttenuationRelationship.STD_DEV_TYPE_NONE);
   imtGuiBean.getParameterList().getParameter(IMT_GuiBean.IMT_PARAM_NAME).setValue(AttenuationRelationship.PGA_NAME);
   siteParams.getParameter(SCEMY_1997_AttenRel.SITE_TYPE_NAME).setValue(SCEMY_1997_AttenRel.SITE_TYPE_ROCK);


   // change IMR sigma if it's Case 2
   if(selectedTest.equalsIgnoreCase(TEST_CASE_TWO) || selectedTest.equalsIgnoreCase(TEST_CASE_FIVE)){
     imrGuiBean.getParameterList().getParameter(AttenuationRelationship.SIGMA_TRUNC_TYPE_NAME).setValue(AttenuationRelationship.SIGMA_TRUNC_TYPE_1SIDED);
     imrGuiBean.getParameterList().getParameter(AttenuationRelationship.SIGMA_TRUNC_LEVEL_NAME).setValue(new Double(3.0));
     imrGuiBean.getParameterList().getParameter(AttenuationRelationship.STD_DEV_TYPE_NAME).setValue(AttenuationRelationship.STD_DEV_TYPE_TOTAL);

   }


   // ********* set the site latitude and longitude ************

   if(selectedTest.equalsIgnoreCase(TEST_CASE_TWO)){
     siteParams.getParameter(Site_GuiBean.LONGITUDE).setValue(new Double(-122));
     if(selectedSite.equalsIgnoreCase(SITE_ONE)){
       siteParams.getParameter(Site_GuiBean.LATITUDE).setValue(new Double(37.5495));
     }
     else if(selectedSite.equalsIgnoreCase(SITE_TWO)){
       siteParams.getParameter(Site_GuiBean.LATITUDE).setValue(new Double(37.0990));
     }
     else if(selectedSite.equalsIgnoreCase(SITE_THREE)){
       siteParams.getParameter(Site_GuiBean.LATITUDE).setValue(new Double(36.8737));
     }
   }
   else {
     if(selectedSite.equalsIgnoreCase(SITE_ONE)){
       siteParams.getParameter(Site_GuiBean.LONGITUDE).setValue(new Double(-121.886));
       siteParams.getParameter(Site_GuiBean.LATITUDE).setValue(new Double(38.1126));
     }
     else if(selectedSite.equalsIgnoreCase(SITE_TWO)){
       siteParams.getParameter(Site_GuiBean.LONGITUDE).setValue(new Double(-122.0));
       siteParams.getParameter(Site_GuiBean.LATITUDE).setValue(new Double(38.2252));
     }
     else if(selectedSite.equalsIgnoreCase(SITE_THREE)){
       siteParams.getParameter(Site_GuiBean.LONGITUDE).setValue(new Double(-122.0));
       siteParams.getParameter(Site_GuiBean.LATITUDE).setValue(new Double(38.0));
     }
   }

   // ************ Set the ERF parameters ********************

   //if test case -1
   if(selectedTest.equalsIgnoreCase(TEST_CASE_ONE)){
     erfGuiBean.getParameterList().getParameter(ERF_GuiBean.ERF_PARAM_NAME).setValue(PEER_NonPlanarFaultForecast.NAME);
     // add sigma for maglength(0-1)
     erfGuiBean.getParameterList().getParameter(PEER_NonPlanarFaultForecast.SIGMA_PARAM_NAME).setValue(new Double(0));
     timeSpanGuiBean.getParameterList().getParameter(TimeSpan.DURATION).setValue(new Double(1.0));
     erfGuiBean.getParameterList().getParameter(PEER_NonPlanarFaultForecast.GRID_PARAM_NAME).setValue(new Double(1.0));
     erfGuiBean.getParameterList().getParameter(PEER_NonPlanarFaultForecast.OFFSET_PARAM_NAME).setValue(new Double(1.0));
     erfGuiBean.getParameterList().getParameter(PEER_NonPlanarFaultForecast.GR_MAG_UPPER).setValue(new Double(7.15));
     erfGuiBean.getParameterList().getParameter(PEER_NonPlanarFaultForecast.SLIP_RATE_NAME).setValue(new Double(2.0));
     erfGuiBean.getParameterList().getParameter(PEER_NonPlanarFaultForecast.SEGMENTATION_NAME).setValue(PEER_NonPlanarFaultForecast.SEGMENTATION_NONE);

   }

   //if test case -2
   if(selectedTest.equalsIgnoreCase(TEST_CASE_TWO)){
     erfGuiBean.getParameterList().getParameter(ERF_GuiBean.ERF_PARAM_NAME).setValue(PEER_MultiSourceForecast.NAME);

     erfGuiBean.getParameterList().getParameter(PEER_MultiSourceForecast.DEPTH_LOWER_PARAM_NAME).setValue(new Double(10));
     erfGuiBean.getParameterList().getParameter(PEER_MultiSourceForecast.DEPTH_UPPER_PARAM_NAME).setValue(new Double(5));
     erfGuiBean.getParameterList().getParameter(PEER_MultiSourceForecast.GRID_PARAM_NAME).setValue(new Double(1.0));
     erfGuiBean.getParameterList().getParameter(PEER_MultiSourceForecast.OFFSET_PARAM_NAME).setValue(new Double(1.0));
     timeSpanGuiBean.getParameterList().getParameter(TimeSpan.DURATION).setValue(new Double(1.0));
   }

   //if test case 3 or 4
   if(selectedTest.equalsIgnoreCase(TEST_CASE_THREE) || selectedTest.equalsIgnoreCase(TEST_CASE_FOUR) ) {

     // set the ERF
     erfGuiBean.getParameterList().getParameter(ERF_GuiBean.ERF_PARAM_NAME).setValue(SimplePoissonFaultERF.NAME);

     erfGuiBean.getParameterList().getParameter(SimplePoissonFaultERF.OFFSET_PARAM_NAME).setValue(new Double(1.0));
     erfGuiBean.getParameterList().getParameter(SimplePoissonFaultERF.MAG_SCALING_REL_PARAM_NAME).setValue(PEER_testsMagAreaRelationship.NAME);
     erfGuiBean.getParameterList().getParameter(SimplePoissonFaultERF.SIGMA_PARAM_NAME).setValue(new Double(0));
     erfGuiBean.getParameterList().getParameter(SimplePoissonFaultERF.ASPECT_RATIO_PARAM_NAME).setValue(new Double(2.0));
     erfGuiBean.getParameterList().getParameter(SimplePoissonFaultERF.MIN_MAG_PARAM_NAME).setValue(new Double(5.0));
     timeSpanGuiBean.getParameterList().getParameter(TimeSpan.DURATION).setValue(new Double(1.0));
     erfGuiBean.getParameterList().getParameter(SimplePoissonFaultERF.RAKE_PARAM_NAME).setValue(new Double(0.0));

     // set the Fault Parameter
     SimpleFaultParameterEditorPanel faultPanel = erfGuiBean.getSimpleFaultParamEditor().getParameterEditorPanel();
     faultPanel.setAll(GRID_SPACING,fault1and2_Lats,fault1and2_Lons,fault1_Dips,fault1_Depths,FAULT_TYPE);
     faultPanel.setEvenlyGriddedSurfaceFromParams();

   }

   //if test case 5
   if(selectedTest.equalsIgnoreCase(TEST_CASE_FIVE) ) {
       erfGuiBean.getParameterList().getParameter(ERF_GuiBean.ERF_PARAM_NAME).setValue(PEER_LogicTreeERF_List.NAME);
       timeSpanGuiBean.getParameterList().getParameter(TimeSpan.DURATION).setValue(new Double(1.0));
   }

   //if test case -6
   if(selectedTest.equalsIgnoreCase(TEST_CASE_SIX)){
     // set the ERF
     erfGuiBean.getParameterList().getParameter(ERF_GuiBean.ERF_PARAM_NAME).setValue(SimplePoissonFaultERF.NAME);

     erfGuiBean.getParameterList().getParameter(SimplePoissonFaultERF.OFFSET_PARAM_NAME).setValue(new Double(1.0));
     erfGuiBean.getParameterList().getParameter(SimplePoissonFaultERF.MAG_SCALING_REL_PARAM_NAME).setValue(PEER_testsMagAreaRelationship.NAME);
     erfGuiBean.getParameterList().getParameter(SimplePoissonFaultERF.SIGMA_PARAM_NAME).setValue(new Double(0));
     erfGuiBean.getParameterList().getParameter(SimplePoissonFaultERF.ASPECT_RATIO_PARAM_NAME).setValue(new Double(2.0));
     erfGuiBean.getParameterList().getParameter(SimplePoissonFaultERF.MIN_MAG_PARAM_NAME).setValue(new Double(5.0));
     timeSpanGuiBean.getParameterList().getParameter(TimeSpan.DURATION).setValue(new Double(1.0));
     erfGuiBean.getParameterList().getParameter(SimplePoissonFaultERF.RAKE_PARAM_NAME).setValue(new Double(0.0));

     // set the Fault Parameter
     SimpleFaultParameterEditorPanel faultPanel = erfGuiBean.getSimpleFaultParamEditor().getParameterEditorPanel();
     faultPanel.setAll(GRID_SPACING,faultE_Lats,faultE_Lons,faultE_Dips,faultE_Depths,FAULT_TYPE);
     faultPanel.setEvenlyGriddedSurfaceFromParams();

   }

   // now set the magFreqDist parameters (if there is one) using the separate method
   MagFreqDistParameterEditor magDistEditor =erfGuiBean.getMagDistEditor();
   if(magDistEditor !=null)  setMagDistParams_Set2(magDistEditor);

}



   /**
    * Sets the default magdist values for the set 2 (only cases 3, 4, and 6 have magFreqDist as
    * an adjustable parameter
    * @param magEditor
    */
   private void setMagDistParams_Set2(MagFreqDistParameterEditor magEditor){

      // mag dist parameters for test case 3
     if(selectedTest.equalsIgnoreCase(TEST_CASE_THREE)){
       magEditor.getParameter(MagFreqDistParameter.DISTRIBUTION_NAME).setValue(YC_1985_CharMagFreqDist.NAME);
       magEditor.getParameter(MagFreqDistParameter.MIN).setValue(new Double(0.05));
       magEditor.getParameter(MagFreqDistParameter.MAX).setValue(new Double(9.95));
       magEditor.getParameter(MagFreqDistParameter.NUM).setValue(new Integer(100));
       magEditor.getParameter(MagFreqDistParameter.GR_BVALUE).setValue(new Double(0.9));
       magEditor.getParameter(MagFreqDistParameter.YC_DELTA_MAG_CHAR).setValue(new Double(.5));
       magEditor.getParameter(MagFreqDistParameter.YC_DELTA_MAG_PRIME).setValue(new Double(1.0));
       magEditor.getParameter(MagFreqDistParameter.GR_MAG_LOWER).setValue(new Double(0.05));
       magEditor.getParameter(MagFreqDistParameter.YC_MAG_PRIME).setValue(new Double(5.95));
       magEditor.getParameter(MagFreqDistParameter.GR_MAG_UPPER).setValue(new Double(6.45));
       magEditor.getParameter(MagFreqDistParameter.SET_ALL_PARAMS_BUT).setValue(MagFreqDistParameter.TOT_MO_RATE);
       magEditor.getParameter(MagFreqDistParameter.YC_TOT_CHAR_RATE).setValue(new Double(1e-3));
       }
       // mag dist parameters for test case 4
     if(selectedTest.equalsIgnoreCase(TEST_CASE_FOUR)){
       magEditor.getParameter(MagFreqDistParameter.DISTRIBUTION_NAME).setValue(GaussianMagFreqDist.NAME);
       magEditor.getParameter(MagFreqDistParameter.MIN).setValue(new Double(0.05));
       magEditor.getParameter(MagFreqDistParameter.MAX).setValue(new Double(9.95));
       magEditor.getParameter(MagFreqDistParameter.NUM).setValue(new Integer(100));
       magEditor.getParameter(MagFreqDistParameter.SET_ALL_PARAMS_BUT).setValue(MagFreqDistParameter.TOT_MO_RATE);
       magEditor.getParameter(MagFreqDistParameter.TOT_CUM_RATE).setValue(new Double(1e-3));
       magEditor.getParameter(MagFreqDistParameter.STD_DEV).setValue(new Double(0.25));
       magEditor.getParameter(MagFreqDistParameter.MEAN).setValue(new Double(6.2));
       magEditor.getParameter(MagFreqDistParameter.TRUNCATION_REQ).setValue(MagFreqDistParameter.TRUNCATE_UPPER_ONLY);
       magEditor.getParameter(MagFreqDistParameter.TRUNCATE_NUM_OF_STD_DEV).setValue(new Double(1.0));
    }

      // mag dist parameters for test case 6
     if(selectedTest.equalsIgnoreCase(TEST_CASE_SIX)){
       magEditor.getParameter(MagFreqDistParameter.DISTRIBUTION_NAME).setValue(GutenbergRichterMagFreqDist.NAME);
       magEditor.getParameter(MagFreqDistParameter.SET_ALL_PARAMS_BUT).setValue(MagFreqDistParameter.TOT_CUM_RATE);
       magEditor.getParameter(MagFreqDistParameter.MIN).setValue(new Double(0.05));
       magEditor.getParameter(MagFreqDistParameter.MAX).setValue(new Double(9.95));
       magEditor.getParameter(MagFreqDistParameter.NUM).setValue(new Integer(100));
        magEditor.getParameter(MagFreqDistParameter.GR_MAG_LOWER).setValue(new Double(0.05));
       magEditor.getParameter(MagFreqDistParameter.GR_MAG_UPPER).setValue(new Double(6.45));
       magEditor.getParameter(MagFreqDistParameter.GR_BVALUE).setValue(new Double(0.9));
       magEditor.getParameter(MagFreqDistParameter.TOT_MO_RATE).setValue(new Double(3.8055e16));
     }

     // now have the editor create the magFreqDist
     magEditor.setMagDistFromParams();
   }


   /**
    * Sets the default magdist values for the set-1
    * @param magEditor
    */
   private void setMagDistParams_Set1(MagFreqDistParameterEditor magEditor){

     // these apply to most (overridden below where not)
     magEditor.getParameter(MagFreqDistParameter.MIN).setValue(new Double(6));
     magEditor.getParameter(MagFreqDistParameter.MAX).setValue(new Double(6.5));
     magEditor.getParameter(MagFreqDistParameter.NUM).setValue(new Integer(6));

     // mag dist parameters for test case 1 & 12
     if(selectedTest.equalsIgnoreCase(TEST_CASE_ONE) || selectedTest.equalsIgnoreCase(TEST_CASE_TWELVE)) {
       magEditor.getParameter(MagFreqDistParameter.DISTRIBUTION_NAME).setValue(SingleMagFreqDist.NAME);
       magEditor.getParameter(MagFreqDistParameter.SINGLE_PARAMS_TO_SET).setValue(MagFreqDistParameter.MAG_AND_MO_RATE);
       magEditor.getParameter(MagFreqDistParameter.MAG).setValue(new Double(6.5));
       magEditor.getParameter(MagFreqDistParameter.MO_RATE).setValue(new Double(1.8e16));
     }

     // mag dist parameters  for test case 2
     else if(selectedTest.equalsIgnoreCase(TEST_CASE_TWO)) {

       magEditor.getParameter(MagFreqDistParameter.DISTRIBUTION_NAME).setValue(SingleMagFreqDist.NAME);
       magEditor.getParameter(MagFreqDistParameter.SINGLE_PARAMS_TO_SET).setValue(MagFreqDistParameter.MAG_AND_MO_RATE);
       magEditor.getParameter(MagFreqDistParameter.MAG).setValue(new Double(6.0));
       magEditor.getParameter(MagFreqDistParameter.MO_RATE).setValue(new Double(1.8e16));
     }

     // mag dist parameters  for test case 3
     else if(selectedTest.equalsIgnoreCase(TEST_CASE_THREE)) {

       magEditor.getParameter(MagFreqDistParameter.DISTRIBUTION_NAME).setValue(SingleMagFreqDist.NAME);
       magEditor.getParameter(MagFreqDistParameter.SINGLE_PARAMS_TO_SET).setValue(MagFreqDistParameter.MAG_AND_MO_RATE);
       magEditor.getParameter(MagFreqDistParameter.MAG).setValue(new Double(6.0));
       magEditor.getParameter(MagFreqDistParameter.MO_RATE).setValue(new Double(1.8e16));
     }

     // mag dist parameters for test case 4
     else if(selectedTest.equalsIgnoreCase(TEST_CASE_FOUR)) {

       magEditor.getParameter(MagFreqDistParameter.DISTRIBUTION_NAME).setValue(SingleMagFreqDist.NAME);
       magEditor.getParameter(MagFreqDistParameter.SINGLE_PARAMS_TO_SET).setValue(MagFreqDistParameter.MAG_AND_MO_RATE);
       magEditor.getParameter(MagFreqDistParameter.MAG).setValue(new Double(6.0));
       magEditor.getParameter(MagFreqDistParameter.MO_RATE).setValue(new Double(1.905e16));
     }

     // mag dist parameters for test case 5
     else if(selectedTest.equalsIgnoreCase(TEST_CASE_FIVE)) {
       magEditor.getParameter(MagFreqDistParameter.DISTRIBUTION_NAME).setValue(GutenbergRichterMagFreqDist.NAME);
       magEditor.getParameter(MagFreqDistParameter.MIN).setValue(new Double(0.005));
       magEditor.getParameter(MagFreqDistParameter.MAX).setValue(new Double(9.995));
       magEditor.getParameter(MagFreqDistParameter.NUM).setValue(new Integer(1000));
       magEditor.getParameter(MagFreqDistParameter.SET_ALL_PARAMS_BUT).setValue(MagFreqDistParameter.TOT_CUM_RATE);
       magEditor.getParameter(MagFreqDistParameter.GR_MAG_LOWER).setValue(new Double(0.005));
       magEditor.getParameter(MagFreqDistParameter.GR_MAG_UPPER).setValue(new Double(6.495));
       magEditor.getParameter(MagFreqDistParameter.GR_BVALUE).setValue(new Double(0.9));
       magEditor.getParameter(MagFreqDistParameter.TOT_MO_RATE).setValue(new Double(1.8e16));
     }


     // mag dist parameters for test case 6
     else if(selectedTest.equalsIgnoreCase(TEST_CASE_SIX)) {
       magEditor.getParameter(MagFreqDistParameter.DISTRIBUTION_NAME).setValue(GaussianMagFreqDist.NAME);
       magEditor.getParameter(MagFreqDistParameter.MIN).setValue(new Double(0.05));
       magEditor.getParameter(MagFreqDistParameter.MAX).setValue(new Double(9.95));
       magEditor.getParameter(MagFreqDistParameter.NUM).setValue(new Integer(100));
       magEditor.getParameter(MagFreqDistParameter.SET_ALL_PARAMS_BUT).setValue(MagFreqDistParameter.TOT_CUM_RATE);
       magEditor.getParameter(MagFreqDistParameter.TOT_MO_RATE).setValue(new Double(1.8e16));
       magEditor.getParameter(MagFreqDistParameter.STD_DEV).setValue(new Double(0.25));
       magEditor.getParameter(MagFreqDistParameter.MEAN).setValue(new Double(6.2));
       magEditor.getParameter(MagFreqDistParameter.TRUNCATION_REQ).setValue(MagFreqDistParameter.TRUNCATE_UPPER_ONLY);
       magEditor.getParameter(MagFreqDistParameter.TRUNCATE_NUM_OF_STD_DEV).setValue(new Double(1.0));
     }
     // mag dist parameters for test case 7
     else if(selectedTest.equalsIgnoreCase(TEST_CASE_SEVEN)) {
       magEditor.getParameter(MagFreqDistParameter.DISTRIBUTION_NAME).setValue(YC_1985_CharMagFreqDist.NAME);
       magEditor.getParameter(MagFreqDistParameter.MIN).setValue(new Double(0.0));
       magEditor.getParameter(MagFreqDistParameter.MAX).setValue(new Double(10));
       magEditor.getParameter(MagFreqDistParameter.NUM).setValue(new Integer(1001));
       magEditor.getParameter(MagFreqDistParameter.GR_BVALUE).setValue(new Double(0.9));
       magEditor.getParameter(MagFreqDistParameter.YC_DELTA_MAG_CHAR).setValue(new Double(0.5));
       magEditor.getParameter(MagFreqDistParameter.YC_DELTA_MAG_PRIME).setValue(new Double(1.0));
       magEditor.getParameter(MagFreqDistParameter.GR_MAG_LOWER).setValue(new Double(0.01));
       magEditor.getParameter(MagFreqDistParameter.YC_MAG_PRIME).setValue(new Double(5.95));
       magEditor.getParameter(MagFreqDistParameter.GR_MAG_UPPER).setValue(new Double(6.45));
       magEditor.getParameter(MagFreqDistParameter.TOT_MO_RATE).setValue(new Double(1.8e16));
     }

     //mag dist parameters for the test case 8_1
     else if(selectedTest.equalsIgnoreCase(TEST_CASE_EIGHT_ONE)) {

       magEditor.getParameter(MagFreqDistParameter.DISTRIBUTION_NAME).setValue(SingleMagFreqDist.NAME);
       magEditor.getParameter(MagFreqDistParameter.SINGLE_PARAMS_TO_SET).setValue(MagFreqDistParameter.MAG_AND_MO_RATE);
       magEditor.getParameter(MagFreqDistParameter.MAG).setValue(new Double(6.0));
       magEditor.getParameter(MagFreqDistParameter.MO_RATE).setValue(new Double(1.8e16));
     }

     //mag dist parameters for the test case 8_2
     else if(selectedTest.equalsIgnoreCase(TEST_CASE_EIGHT_TWO)) {

       magEditor.getParameter(MagFreqDistParameter.DISTRIBUTION_NAME).setValue(SingleMagFreqDist.NAME);
       magEditor.getParameter(MagFreqDistParameter.SINGLE_PARAMS_TO_SET).setValue(MagFreqDistParameter.MAG_AND_MO_RATE);
       magEditor.getParameter(MagFreqDistParameter.MAG).setValue(new Double(6.0));
       magEditor.getParameter(MagFreqDistParameter.MO_RATE).setValue(new Double(1.8e16));
     }

     //mag dist parameters for the test case 8_3
     else if(selectedTest.equalsIgnoreCase(TEST_CASE_EIGHT_THREE)) {

       magEditor.getParameter(MagFreqDistParameter.DISTRIBUTION_NAME).setValue(SingleMagFreqDist.NAME);
       magEditor.getParameter(MagFreqDistParameter.SINGLE_PARAMS_TO_SET).setValue(MagFreqDistParameter.MAG_AND_MO_RATE);
       magEditor.getParameter(MagFreqDistParameter.MAG).setValue(new Double(6.0));
       magEditor.getParameter(MagFreqDistParameter.MO_RATE).setValue(new Double(1.8e16));
     }

     //mag dist parameters for the test case 9_1
     else if(selectedTest.equalsIgnoreCase(TEST_CASE_NINE_ONE)) {

       magEditor.getParameter(MagFreqDistParameter.DISTRIBUTION_NAME).setValue(SingleMagFreqDist.NAME);
       magEditor.getParameter(MagFreqDistParameter.SINGLE_PARAMS_TO_SET).setValue(MagFreqDistParameter.MAG_AND_MO_RATE);
       magEditor.getParameter(MagFreqDistParameter.MAG).setValue(new Double(6.0));
       magEditor.getParameter(MagFreqDistParameter.MO_RATE).setValue(new Double(1.905e16));
     }

     //mag dist parameters for the test case 9_2
     else if(selectedTest.equalsIgnoreCase(TEST_CASE_NINE_TWO)) {

       magEditor.getParameter(MagFreqDistParameter.DISTRIBUTION_NAME).setValue(SingleMagFreqDist.NAME);
       magEditor.getParameter(MagFreqDistParameter.SINGLE_PARAMS_TO_SET).setValue(MagFreqDistParameter.MAG_AND_MO_RATE);
       magEditor.getParameter(MagFreqDistParameter.MAG).setValue(new Double(6.0));
       magEditor.getParameter(MagFreqDistParameter.MO_RATE).setValue(new Double(1.905e16));
     }

     //mag dist parameters for the test case 9_1
     else if(selectedTest.equalsIgnoreCase(TEST_CASE_NINE_THREE)) {

       magEditor.getParameter(MagFreqDistParameter.DISTRIBUTION_NAME).setValue(SingleMagFreqDist.NAME);
       magEditor.getParameter(MagFreqDistParameter.SINGLE_PARAMS_TO_SET).setValue(MagFreqDistParameter.MAG_AND_MO_RATE);
       magEditor.getParameter(MagFreqDistParameter.MAG).setValue(new Double(6.0));
       magEditor.getParameter(MagFreqDistParameter.MO_RATE).setValue(new Double(1.905e16));
     }

     // mag dist parameters for test case 10
     else if(selectedTest.equalsIgnoreCase(TEST_CASE_TEN)) {
       magEditor.getParameter(MagFreqDistParameter.DISTRIBUTION_NAME).setValue(GutenbergRichterMagFreqDist.NAME);
       magEditor.getParameter(MagFreqDistParameter.MIN).setValue(new Double(0.05));
       magEditor.getParameter(MagFreqDistParameter.MAX).setValue(new Double(9.95));
       magEditor.getParameter(MagFreqDistParameter.NUM).setValue(new Integer(100));
       magEditor.getParameter(MagFreqDistParameter.SET_ALL_PARAMS_BUT).setValue(MagFreqDistParameter.TOT_MO_RATE);
       magEditor.getParameter(MagFreqDistParameter.GR_MAG_LOWER).setValue(new Double(5.05));
       magEditor.getParameter(MagFreqDistParameter.GR_MAG_UPPER).setValue(new Double(6.45));
       magEditor.getParameter(MagFreqDistParameter.GR_BVALUE).setValue(new Double(0.9));
       magEditor.getParameter(MagFreqDistParameter.TOT_CUM_RATE).setValue(new Double(.0395));
     }

     // mag dist parameters for test case 11
     else if(selectedTest.equalsIgnoreCase(TEST_CASE_ELEVEN)) {
       magEditor.getParameter(MagFreqDistParameter.DISTRIBUTION_NAME).setValue(GutenbergRichterMagFreqDist.NAME);
       magEditor.getParameter(MagFreqDistParameter.MIN).setValue(new Double(0.05));
       magEditor.getParameter(MagFreqDistParameter.MAX).setValue(new Double(9.95));
       magEditor.getParameter(MagFreqDistParameter.NUM).setValue(new Integer(100));
       magEditor.getParameter(MagFreqDistParameter.SET_ALL_PARAMS_BUT).setValue(MagFreqDistParameter.TOT_MO_RATE);
       magEditor.getParameter(MagFreqDistParameter.GR_MAG_LOWER).setValue(new Double(5.05));
       magEditor.getParameter(MagFreqDistParameter.GR_MAG_UPPER).setValue(new Double(6.45));
       magEditor.getParameter(MagFreqDistParameter.GR_BVALUE).setValue(new Double(0.9));
       magEditor.getParameter(MagFreqDistParameter.TOT_CUM_RATE).setValue(new Double(.0395));
     }

     // now have the editor create the magFreqDist
     magEditor.setMagDistFromParams();
   }

   /**
    * This initializes the fault-data vectors needed for the tests that utilize the SimplePoissonFaultERF
    */
   private void initializeFaultData() {

     // Set1 faults
     fault1and2_Lats = new Vector();
     fault1and2_Lats.add(new Double(38.22480));
     fault1and2_Lats.add(new Double(38.0));

     fault1and2_Lons = new Vector();
     fault1and2_Lons.add(new Double(-122.0));
     fault1and2_Lons.add(new Double(-122.0));

     fault1_Dips = new Vector();
     fault1_Dips.add(new Double(90.0));

     fault1_Depths = new Vector();
     fault1_Depths.add(new Double(0.0));
     fault1_Depths.add(new Double(12.0));

     fault2_Dips = new Vector();
     fault2_Dips.add(new Double(60.0));

     fault2_Depths = new Vector();
     fault2_Depths.add(new Double(1.0));
     fault2_Depths.add(new Double(12.0));

     // Set2 faults
     faultE_Lats = new Vector();
     faultE_Lats.add(new Double(38.0));
     faultE_Lats.add(new Double(38.2248));

     faultE_Lons = new Vector();
     faultE_Lons.add(new Double(-122.0));
     faultE_Lons.add(new Double(-122.0));

     faultE_Dips = new Vector();
     faultE_Dips.add(new Double(50.0));
     faultE_Dips.add(new Double(20.0));

     faultE_Depths = new Vector();
     faultE_Depths.add(new Double(0.0));
     faultE_Depths.add(new Double(6.0));
     faultE_Depths.add(new Double(12.0));

   }


   /**
    * fill he pick list with the test case numbers and sites
    */
   private void initializeTestsAndSites() {
     //initialising the values inside the combobox for the supported test cases and sites
     Vector v = new Vector();

     //test case-1 ,Set-1
     v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_ONE+"-"+this.SITE_ONE));
     v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_ONE+"-"+this.SITE_TWO));
     v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_ONE+"-"+this.SITE_THREE));
     v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_ONE+"-"+this.SITE_FOUR));
     v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_ONE+"-"+this.SITE_FIVE));
     v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_ONE+"-"+this.SITE_SIX));
     v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_ONE+"-"+this.SITE_SEVEN));


     //test case-2,Set-1
     v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_TWO+"-"+this.SITE_ONE));
     v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_TWO+"-"+this.SITE_TWO));
     v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_TWO+"-"+this.SITE_THREE));
     v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_TWO+"-"+this.SITE_FOUR));
     v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_TWO+"-"+this.SITE_FIVE));
     v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_TWO+"-"+this.SITE_SIX));
     v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_TWO+"-"+this.SITE_SEVEN));


     //test case-3
     v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_THREE+"-"+this.SITE_ONE));
     v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_THREE+"-"+this.SITE_TWO));
     v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_THREE+"-"+this.SITE_THREE));
     v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_THREE+"-"+this.SITE_FOUR));
     v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_THREE+"-"+this.SITE_FIVE));
     v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_THREE+"-"+this.SITE_SIX));
     v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_THREE+"-"+this.SITE_SEVEN));


     //test case-4
     v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_FOUR+"-"+this.SITE_ONE));
     v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_FOUR+"-"+this.SITE_TWO));
     v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_FOUR+"-"+this.SITE_THREE));
     v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_FOUR+"-"+this.SITE_FOUR));
     v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_FOUR+"-"+this.SITE_FIVE));
     v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_FOUR+"-"+this.SITE_SIX));
     v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_FOUR+"-"+this.SITE_SEVEN));


     //test case-5
     v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_FIVE+"-"+this.SITE_ONE));
     v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_FIVE+"-"+this.SITE_TWO));
     v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_FIVE+"-"+this.SITE_THREE));
     v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_FIVE+"-"+this.SITE_FOUR));
     v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_FIVE+"-"+this.SITE_FIVE));
     v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_FIVE+"-"+this.SITE_SIX));
     v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_FIVE+"-"+this.SITE_SEVEN));

     //test case-6
     v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_SIX+"-"+this.SITE_ONE));
     v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_SIX+"-"+this.SITE_TWO));
     v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_SIX+"-"+this.SITE_THREE));
     v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_SIX+"-"+this.SITE_FOUR));
     v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_SIX+"-"+this.SITE_FIVE));
     v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_SIX+"-"+this.SITE_SIX));
     v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_SIX+"-"+this.SITE_SEVEN));


     //test case-7
     v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_SEVEN+"-"+this.SITE_ONE));
     v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_SEVEN+"-"+this.SITE_TWO));
     v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_SEVEN+"-"+this.SITE_THREE));
     v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_SEVEN+"-"+this.SITE_FOUR));
     v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_SEVEN+"-"+this.SITE_FIVE));
     v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_SEVEN+"-"+this.SITE_SIX));
     v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_SEVEN+"-"+this.SITE_SEVEN));

     //test case-8_0sig
     v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_EIGHT_ONE+"-"+this.SITE_ONE));
     v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_EIGHT_ONE+"-"+this.SITE_TWO));
     v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_EIGHT_ONE+"-"+this.SITE_THREE));
     v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_EIGHT_ONE+"-"+this.SITE_FOUR));
     v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_EIGHT_ONE+"-"+this.SITE_FIVE));
     v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_EIGHT_ONE+"-"+this.SITE_SIX));
     v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_EIGHT_ONE+"-"+this.SITE_SEVEN));

     //test case-8_1sig
     v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_EIGHT_TWO+"-"+this.SITE_ONE));
     v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_EIGHT_TWO+"-"+this.SITE_TWO));
     v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_EIGHT_TWO+"-"+this.SITE_THREE));
     v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_EIGHT_TWO+"-"+this.SITE_FOUR));
     v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_EIGHT_TWO+"-"+this.SITE_FIVE));
     v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_EIGHT_TWO+"-"+this.SITE_SIX));
     v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_EIGHT_TWO+"-"+this.SITE_SEVEN));

     //test case-8_2sig
     v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_EIGHT_THREE+"-"+this.SITE_ONE));
     v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_EIGHT_THREE+"-"+this.SITE_TWO));
     v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_EIGHT_THREE+"-"+this.SITE_THREE));
     v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_EIGHT_THREE+"-"+this.SITE_FOUR));
     v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_EIGHT_THREE+"-"+this.SITE_FIVE));
     v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_EIGHT_THREE+"-"+this.SITE_SIX));
     v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_EIGHT_THREE+"-"+this.SITE_SEVEN));


     //test case-9_Sa97
     v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_NINE_ONE+"-"+this.SITE_ONE));
     v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_NINE_ONE+"-"+this.SITE_TWO));
     v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_NINE_ONE+"-"+this.SITE_THREE));
     v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_NINE_ONE+"-"+this.SITE_FOUR));
     v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_NINE_ONE+"-"+this.SITE_FIVE));
     v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_NINE_ONE+"-"+this.SITE_SIX));
     v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_NINE_ONE+"-"+this.SITE_SEVEN));

     //test case-9_SA97
     v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_NINE_TWO+"-"+this.SITE_ONE));
     v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_NINE_TWO+"-"+this.SITE_TWO));
     v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_NINE_TWO+"-"+this.SITE_THREE));
     v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_NINE_TWO+"-"+this.SITE_FOUR));
     v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_NINE_TWO+"-"+this.SITE_FIVE));
     v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_NINE_TWO+"-"+this.SITE_SIX));
     v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_NINE_TWO+"-"+this.SITE_SEVEN));

     //test case-9_Ca97
     v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_NINE_THREE+"-"+this.SITE_ONE));
     v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_NINE_THREE+"-"+this.SITE_TWO));
     v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_NINE_THREE+"-"+this.SITE_THREE));
     v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_NINE_THREE+"-"+this.SITE_FOUR));
     v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_NINE_THREE+"-"+this.SITE_FIVE));
     v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_NINE_THREE+"-"+this.SITE_SIX));
     v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_NINE_THREE+"-"+this.SITE_SEVEN));

     //test case-10
     v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_TEN+"-"+this.SITE_ONE));
     v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_TEN+"-"+this.SITE_TWO));
     v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_TEN+"-"+this.SITE_THREE));
     v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_TEN+"-"+this.SITE_FOUR));


     //test case-11
     v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_ELEVEN+"-"+this.SITE_ONE));
     v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_ELEVEN+"-"+this.SITE_TWO));
     v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_ELEVEN+"-"+this.SITE_THREE));
     v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_ELEVEN+"-"+this.SITE_FOUR));

     //test case-12
     v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_TWELVE+"-"+this.SITE_ONE));
     v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_TWELVE+"-"+this.SITE_TWO));
     v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_TWELVE+"-"+this.SITE_THREE));
     v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_TWELVE+"-"+this.SITE_FOUR));
     v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_TWELVE+"-"+this.SITE_FIVE));
     v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_TWELVE+"-"+this.SITE_SIX));
     v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_TWELVE+"-"+this.SITE_SEVEN));


     //adding the SET ONE PEER test cases to the set one vector
     int size = v.size();
     for(int i=0;i<size;++i)
       this.peerTestSetOne.add(v.get(i));

     v.removeAllElements();
     //test case-1 , Set-2
     v.add(new String(this.PEER_TESTS_SET_TWO +"-"+this.TEST_CASE_ONE+"-"+this.SITE_ONE));
     v.add(new String(this.PEER_TESTS_SET_TWO +"-"+this.TEST_CASE_ONE+"-"+this.SITE_TWO));
     v.add(new String(this.PEER_TESTS_SET_TWO +"-"+this.TEST_CASE_ONE+"-"+this.SITE_THREE));

     //test case-2 , Set-2
     v.add(new String(this.PEER_TESTS_SET_TWO +"-"+this.TEST_CASE_TWO+"-"+this.SITE_ONE));
     v.add(new String(this.PEER_TESTS_SET_TWO +"-"+this.TEST_CASE_TWO+"-"+this.SITE_TWO));
     v.add(new String(this.PEER_TESTS_SET_TWO +"-"+this.TEST_CASE_TWO+"-"+this.SITE_THREE));

     //test case-3 , Set-2
     v.add(new String(this.PEER_TESTS_SET_TWO +"-"+this.TEST_CASE_THREE+"-"+this.SITE_ONE));
     v.add(new String(this.PEER_TESTS_SET_TWO +"-"+this.TEST_CASE_THREE+"-"+this.SITE_TWO));
     v.add(new String(this.PEER_TESTS_SET_TWO +"-"+this.TEST_CASE_THREE+"-"+this.SITE_THREE));


     //test case-4 , Set-2
     v.add(new String(this.PEER_TESTS_SET_TWO +"-"+this.TEST_CASE_FOUR+"-"+this.SITE_ONE));
     v.add(new String(this.PEER_TESTS_SET_TWO +"-"+this.TEST_CASE_FOUR+"-"+this.SITE_TWO));
     v.add(new String(this.PEER_TESTS_SET_TWO +"-"+this.TEST_CASE_FOUR+"-"+this.SITE_THREE));

      //test case-5 , Set-2
     v.add(new String(this.PEER_TESTS_SET_TWO +"-"+this.TEST_CASE_FIVE+"-"+this.SITE_ONE));
     v.add(new String(this.PEER_TESTS_SET_TWO +"-"+this.TEST_CASE_FIVE+"-"+this.SITE_TWO));
     v.add(new String(this.PEER_TESTS_SET_TWO +"-"+this.TEST_CASE_FIVE+"-"+this.SITE_THREE));

     //test case-6 , Set-2
     v.add(new String(this.PEER_TESTS_SET_TWO +"-"+this.TEST_CASE_SIX+"-"+this.SITE_ONE));
     v.add(new String(this.PEER_TESTS_SET_TWO +"-"+this.TEST_CASE_SIX+"-"+this.SITE_TWO));
     v.add(new String(this.PEER_TESTS_SET_TWO +"-"+this.TEST_CASE_SIX+"-"+this.SITE_THREE));

     //adding the SET TWO PEER test cases to the set two vector
     size = v.size();
     for(int i=0;i<size;++i)
       this.peerTestSetTwo.add(v.get(i));


     size = this.peerTestSetOne.size();
     for(int i=0;i<size;++i)
       this.testCaseComboBox.addItem(peerTestSetOne.get(i));


     size = this.peerTestSetTwo.size();
     for(int i=0;i<size;++i)
       this.testCaseComboBox.addItem(peerTestSetTwo.get(i));
   }

   /**
    * this sets the test case params in the GUI
    * @param e
    */
  void testCaseComboBox_actionPerformed(ActionEvent e) {
    String testSelected = this.testCaseComboBox.getSelectedItem().toString();
    setTestCaseAndSite(testSelected);
  }

  /**
   *
   * @returns the Vector of the PEER Test Case set One
   */
  public Vector getPEER_SetOneTestCasesNames(){
   return this.peerTestSetOne;
  }


  /**
   *
   * @returns the Vector of the PEER Test Case set Two
   */
  public Vector getPEER_SetTwoTestCasesNames(){
   return this.peerTestSetTwo;
  }
}
