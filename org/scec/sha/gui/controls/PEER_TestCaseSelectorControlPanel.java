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
import org.scec.sha.magdist.parameter.*;
import org.scec.sha.magdist.gui.*;
import org.scec.sha.earthquake.*;
import org.scec.sha.imr.*;
import org.scec.sha.imr.attenRelImpl.*;
import org.scec.data.function.*;
import org.scec.util.*;
import org.scec.data.*;
import org.scec.sha.gui.beans.*;
import java.awt.*;
import javax.swing.*;


/**
 *
 * <p>Title: PEER_TestCaseSelectorControlPanel</p>
 * <p>Description: This class creates the a windoe which contains the
 * list of different PEER tests cases and user can make its selection from these cases.
 * This class also sets the default parameters for the selected test
 * in the HazardCurveApplet. </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author : Nitin Gupta and Vipin Gupta
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


  //Sites Supported
  public final static String SITE_ONE = "a";
  public final static String SITE_TWO = "b";
  public final static String SITE_THREE = "c";
  public final static String SITE_FOUR = "d";
  public final static String SITE_FIVE = "e";
  public final static String SITE_SIX = "f";
  public final static String SITE_SEVEN = "g";


  // various gui beans
  private IMT_GuiBean imtGuiBean;
  private IMR_GuiBean imrGuiBean;
  private Site_GuiBean siteGuiBean;
  private ERF_GuiBean erfGuiBean;
  private TimeSpanGuiBean timeSpanGuiBean;

  //Stores the test case,
  private String selectedTest;
  private String selectedSite;
  private String selectedSet;
  private JLabel jLabel2 = new JLabel();
  private JComboBox testCaseComboBox = new JComboBox();
  private GridBagLayout gridBagLayout1 = new GridBagLayout();


  public PEER_TestCaseSelectorControlPanel(Component parent, IMR_GuiBean imrGuiBean,
                               Site_GuiBean siteGuiBean,
                               IMT_GuiBean imtGuiBean,
                               ERF_GuiBean erfGuiBean,
                               TimeSpanGuiBean timeSpanGuiBean){

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
    // fill the combo box with tests and sites
    this.initializeTestsAndSites();

    // show the window at center of the parent component
    this.setLocation(parent.getX()+parent.getWidth()/2,
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
    ParameterList siteParams = siteGuiBean.getParameterList();

    //if set-1 PEER test case is selected
    if(selectedSet.equalsIgnoreCase(PEER_TESTS_SET_ONE))
      set_Set1Params(siteParams);

    //if set-2 PEER test case is selected
    else if(selectedSet.equalsIgnoreCase(PEER_TESTS_SET_TWO))
      set_Set2Params(siteParams);

    // refresh the editor according to parameter values
    imrGuiBean.synchToModel();
    imtGuiBean.synchToModel();
    siteGuiBean.synchToModel();
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
     imrGuiBean.getParameterList().getParameter(AttenuationRelationship.STD_DEV_TYPE_NAME).setValue(Campbell_1997_AttenRel.STD_DEV_TYPE_MAG_DEP);
     imtGuiBean.getParameterList().getParameter(IMT_GuiBean.IMT_PARAM_NAME).setValue(AttenuationRelationship.PGA_NAME);
     siteGuiBean.getParameterList().getParameter(Campbell_1997_AttenRel.SITE_TYPE_NAME).setValue(Campbell_1997_AttenRel.SITE_TYPE_SOFT_ROCK);
     siteParams.getParameter(Campbell_1997_AttenRel.BASIN_DEPTH_NAME).setValue(new Double(2.0));
   }

   // *********** Now fill in the forecast parameters ************************

   // if it's one of the "PEER fault" problems (cases 1-9)
   if(!selectedTest.equalsIgnoreCase(TEST_CASE_TEN) && !selectedTest.equalsIgnoreCase(TEST_CASE_ELEVEN)) {

     // set the ERF
     erfGuiBean.getParameterList().getParameter(ERF_GuiBean.ERF_PARAM_NAME).setValue(PEER_FaultForecast.NAME);

     // set the common parameters like timespan, grid spacing
     erfGuiBean.getParameterList().getParameter(PEER_FaultForecast.SIGMA_PARAM_NAME).setValue(new Double(0));
     timeSpanGuiBean.getParameterList().getParameter(TimeSpan.DURATION).setValue(new Double(1.0));
     erfGuiBean.getParameterList().getParameter(PEER_FaultForecast.GRID_PARAM_NAME).setValue(new Double(1.0));
     erfGuiBean.getParameterList().getParameter(PEER_FaultForecast.OFFSET_PARAM_NAME).setValue(new Double(1.0));

     // magLengthSigma parameter is changed if the test case chosen is 3
     if(selectedTest.equals(TEST_CASE_THREE))
       erfGuiBean.getParameterList().getParameter(PEER_FaultForecast.SIGMA_PARAM_NAME).setValue(new Double(0.2));

     // set the dip and rake
     if( selectedTest.equals(TEST_CASE_FOUR) ||
         selectedTest.equals(TEST_CASE_NINE_ONE) ||
         selectedTest.equals(TEST_CASE_NINE_TWO) ||
         selectedTest.equals(TEST_CASE_NINE_THREE) ) {
              erfGuiBean.getParameterList().getParameter(PEER_FaultForecast.DIP_PARAM_NAME).setValue(new Double(60.0));
              erfGuiBean.getParameterList().getParameter(PEER_FaultForecast.RAKE_PARAM_NAME).setValue(new Double(90.0));
     }
     else {
       erfGuiBean.getParameterList().getParameter(PEER_FaultForecast.DIP_PARAM_NAME).setValue(new Double(90.0));
       erfGuiBean.getParameterList().getParameter(PEER_FaultForecast.RAKE_PARAM_NAME).setValue(new Double(0.0));
     }

   }
   else {// if it area case
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
       siteGuiBean.getParameterList().getParameter(Site_GuiBean.LATITUDE).setValue(new Double(38.113));
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
   if(selectedTest.equalsIgnoreCase(TEST_CASE_TWO)){
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
     erfGuiBean.getParameterList().getParameter(ERF_GuiBean.ERF_PARAM_NAME).setValue(PEER_FaultForecast.NAME);

     erfGuiBean.getParameterList().getParameter(PEER_FaultForecast.DIP_PARAM_NAME).setValue(new Double(90.0));
     erfGuiBean.getParameterList().getParameter(PEER_FaultForecast.GRID_PARAM_NAME).setValue(new Double(1.0));
     erfGuiBean.getParameterList().getParameter(PEER_FaultForecast.OFFSET_PARAM_NAME).setValue(new Double(1.0));
     erfGuiBean.getParameterList().getParameter(PEER_FaultForecast.RAKE_PARAM_NAME).setValue(new Double(0.0));
     erfGuiBean.getParameterList().getParameter(PEER_FaultForecast.SIGMA_PARAM_NAME).setValue(new Double(0.0));
     timeSpanGuiBean.getParameterList().getParameter(TimeSpan.DURATION).setValue(new Double(1.0));
   }

   //if test case 5
     if(selectedTest.equalsIgnoreCase(TEST_CASE_FIVE) )
       erfGuiBean.getParameterList().getParameter(ERF_GuiBean.ERF_PARAM_NAME).setValue(PEER_LogicTreeERF_List.NAME);

   //if test case -6
   if(selectedTest.equalsIgnoreCase(TEST_CASE_SIX)){
     erfGuiBean.getParameterList().getParameter(ERF_GuiBean.ERF_PARAM_NAME).setValue(PEER_ListricFaultForecast.NAME);
     erfGuiBean.getParameterList().getParameter(PEER_ListricFaultForecast.GRID_PARAM_NAME).setValue(new Double(1.0));
     erfGuiBean.getParameterList().getParameter(PEER_ListricFaultForecast.OFFSET_PARAM_NAME).setValue(new Double(1.0));
     erfGuiBean.getParameterList().getParameter(PEER_ListricFaultForecast.RAKE_PARAM_NAME).setValue(new Double(0.0));
     erfGuiBean.getParameterList().getParameter(PEER_ListricFaultForecast.SIGMA_PARAM_NAME).setValue(new Double(0.0));
     timeSpanGuiBean.getParameterList().getParameter(TimeSpan.DURATION).setValue(new Double(1.0));
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
       magEditor.getParameter(MagFreqDistParameterEditor.DISTRIBUTION_NAME).setValue(YC_1985_CharMagFreqDist.NAME);
       magEditor.getParameter(MagFreqDistParameterEditor.MIN).setValue(new Double(0.05));
       magEditor.getParameter(MagFreqDistParameterEditor.MAX).setValue(new Double(9.95));
       magEditor.getParameter(MagFreqDistParameterEditor.NUM).setValue(new Integer(100));
       magEditor.getParameter(MagFreqDistParameterEditor.GR_BVALUE).setValue(new Double(0.9));
       magEditor.getParameter(MagFreqDistParameterEditor.YC_DELTA_MAG_CHAR).setValue(new Double(.5));
       magEditor.getParameter(MagFreqDistParameterEditor.YC_DELTA_MAG_PRIME).setValue(new Double(1.0));
       magEditor.getParameter(MagFreqDistParameterEditor.GR_MAG_LOWER).setValue(new Double(0.05));
       magEditor.getParameter(MagFreqDistParameterEditor.YC_MAG_PRIME).setValue(new Double(5.95));
       magEditor.getParameter(MagFreqDistParameterEditor.GR_MAG_UPPER).setValue(new Double(6.45));
       magEditor.getParameter(MagFreqDistParameterEditor.SET_ALL_PARAMS_BUT).setValue(MagFreqDistParameterEditor.TOT_MO_RATE);
       magEditor.getParameter(MagFreqDistParameterEditor.YC_TOT_CHAR_RATE).setValue(new Double(1e-3));
       }
       // mag dist parameters for test case 4
     if(selectedTest.equalsIgnoreCase(TEST_CASE_FOUR)){
       magEditor.getParameter(MagFreqDistParameterEditor.DISTRIBUTION_NAME).setValue(GaussianMagFreqDist.NAME);
       magEditor.getParameter(MagFreqDistParameterEditor.MIN).setValue(new Double(0.05));
       magEditor.getParameter(MagFreqDistParameterEditor.MAX).setValue(new Double(9.95));
       magEditor.getParameter(MagFreqDistParameterEditor.NUM).setValue(new Integer(100));
       magEditor.getParameter(MagFreqDistParameterEditor.SET_ALL_PARAMS_BUT).setValue(MagFreqDistParameterEditor.TOT_MO_RATE);
       magEditor.getParameter(MagFreqDistParameterEditor.TOT_CUM_RATE).setValue(new Double(1e-3));
       magEditor.getParameter(MagFreqDistParameterEditor.STD_DEV).setValue(new Double(0.25));
       magEditor.getParameter(MagFreqDistParameterEditor.MEAN).setValue(new Double(6.2));
       magEditor.getParameter(MagFreqDistParameterEditor.TRUNCATION_REQ).setValue(MagFreqDistParameterEditor.TRUNCATE_UPPER_ONLY);
       magEditor.getParameter(MagFreqDistParameterEditor.TRUNCATE_NUM_OF_STD_DEV).setValue(new Double(1.0));
    }

      // mag dist parameters for test case 6
     if(selectedTest.equalsIgnoreCase(TEST_CASE_SIX)){
       magEditor.getParameter(MagFreqDistParameterEditor.DISTRIBUTION_NAME).setValue(GutenbergRichterMagFreqDist.NAME);
       magEditor.getParameter(MagFreqDistParameterEditor.SET_ALL_PARAMS_BUT).setValue(MagFreqDistParameterEditor.TOT_CUM_RATE);
       magEditor.getParameter(MagFreqDistParameterEditor.MIN).setValue(new Double(0.05));
       magEditor.getParameter(MagFreqDistParameterEditor.MAX).setValue(new Double(9.95));
       magEditor.getParameter(MagFreqDistParameterEditor.NUM).setValue(new Integer(100));
        magEditor.getParameter(MagFreqDistParameterEditor.GR_MAG_LOWER).setValue(new Double(0.05));
       magEditor.getParameter(MagFreqDistParameterEditor.GR_MAG_UPPER).setValue(new Double(6.45));
       magEditor.getParameter(MagFreqDistParameterEditor.GR_BVALUE).setValue(new Double(0.9));
       magEditor.getParameter(MagFreqDistParameterEditor.TOT_MO_RATE).setValue(new Double(3.8055e16));
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
     magEditor.getParameter(MagFreqDistParameterEditor.MIN).setValue(new Double(6));
     magEditor.getParameter(MagFreqDistParameterEditor.MAX).setValue(new Double(6.5));
     magEditor.getParameter(MagFreqDistParameterEditor.NUM).setValue(new Integer(6));

     // mag dist parameters for test case 1
     if(selectedTest.equalsIgnoreCase(TEST_CASE_ONE)) {
       magEditor.getParameter(MagFreqDistParameterEditor.DISTRIBUTION_NAME).setValue(SingleMagFreqDist.NAME);
       magEditor.getParameter(MagFreqDistParameterEditor.SINGLE_PARAMS_TO_SET).setValue(MagFreqDistParameterEditor.MAG_AND_MO_RATE);
       magEditor.getParameter(MagFreqDistParameterEditor.MAG).setValue(new Double(6.5));
       magEditor.getParameter(MagFreqDistParameterEditor.MO_RATE).setValue(new Double(1.8e16));
     }

     // mag dist parameters  for test case 2
     else if(selectedTest.equalsIgnoreCase(TEST_CASE_TWO)) {

       magEditor.getParameter(MagFreqDistParameterEditor.DISTRIBUTION_NAME).setValue(SingleMagFreqDist.NAME);
       magEditor.getParameter(MagFreqDistParameterEditor.SINGLE_PARAMS_TO_SET).setValue(MagFreqDistParameterEditor.MAG_AND_MO_RATE);
       magEditor.getParameter(MagFreqDistParameterEditor.MAG).setValue(new Double(6.0));
       magEditor.getParameter(MagFreqDistParameterEditor.MO_RATE).setValue(new Double(1.8e16));
     }

     // mag dist parameters  for test case 3
     else if(selectedTest.equalsIgnoreCase(TEST_CASE_THREE)) {

       magEditor.getParameter(MagFreqDistParameterEditor.DISTRIBUTION_NAME).setValue(SingleMagFreqDist.NAME);
       magEditor.getParameter(MagFreqDistParameterEditor.SINGLE_PARAMS_TO_SET).setValue(MagFreqDistParameterEditor.MAG_AND_MO_RATE);
       magEditor.getParameter(MagFreqDistParameterEditor.MAG).setValue(new Double(6.0));
       magEditor.getParameter(MagFreqDistParameterEditor.MO_RATE).setValue(new Double(1.8e16));
     }

     // mag dist parameters for test case 4
     else if(selectedTest.equalsIgnoreCase(TEST_CASE_FOUR)) {

       magEditor.getParameter(MagFreqDistParameterEditor.DISTRIBUTION_NAME).setValue(SingleMagFreqDist.NAME);
       magEditor.getParameter(MagFreqDistParameterEditor.SINGLE_PARAMS_TO_SET).setValue(MagFreqDistParameterEditor.MAG_AND_MO_RATE);
       magEditor.getParameter(MagFreqDistParameterEditor.MAG).setValue(new Double(6.0));
       magEditor.getParameter(MagFreqDistParameterEditor.MO_RATE).setValue(new Double(1.905e16));
     }

     // mag dist parameters for test case 5
     else if(selectedTest.equalsIgnoreCase(TEST_CASE_FIVE)) {
       magEditor.getParameter(MagFreqDistParameterEditor.DISTRIBUTION_NAME).setValue(GutenbergRichterMagFreqDist.NAME);
       magEditor.getParameter(MagFreqDistParameterEditor.MIN).setValue(new Double(0.005));
       magEditor.getParameter(MagFreqDistParameterEditor.MAX).setValue(new Double(9.995));
       magEditor.getParameter(MagFreqDistParameterEditor.NUM).setValue(new Integer(1000));
       magEditor.getParameter(MagFreqDistParameterEditor.SET_ALL_PARAMS_BUT).setValue(MagFreqDistParameterEditor.TOT_CUM_RATE);
       magEditor.getParameter(MagFreqDistParameterEditor.GR_MAG_LOWER).setValue(new Double(0.005));
       magEditor.getParameter(MagFreqDistParameterEditor.GR_MAG_UPPER).setValue(new Double(6.495));
       magEditor.getParameter(MagFreqDistParameterEditor.GR_BVALUE).setValue(new Double(0.9));
       magEditor.getParameter(MagFreqDistParameterEditor.TOT_MO_RATE).setValue(new Double(1.8e16));
     }


     // mag dist parameters for test case 6
     else if(selectedTest.equalsIgnoreCase(TEST_CASE_SIX)) {
       magEditor.getParameter(MagFreqDistParameterEditor.DISTRIBUTION_NAME).setValue(GaussianMagFreqDist.NAME);
       magEditor.getParameter(MagFreqDistParameterEditor.MIN).setValue(new Double(0.05));
       magEditor.getParameter(MagFreqDistParameterEditor.MAX).setValue(new Double(9.95));
       magEditor.getParameter(MagFreqDistParameterEditor.NUM).setValue(new Integer(100));
       magEditor.getParameter(MagFreqDistParameterEditor.SET_ALL_PARAMS_BUT).setValue(MagFreqDistParameterEditor.TOT_CUM_RATE);
       magEditor.getParameter(MagFreqDistParameterEditor.TOT_MO_RATE).setValue(new Double(1.8e16));
       magEditor.getParameter(MagFreqDistParameterEditor.STD_DEV).setValue(new Double(0.25));
       magEditor.getParameter(MagFreqDistParameterEditor.MEAN).setValue(new Double(6.2));
       magEditor.getParameter(MagFreqDistParameterEditor.TRUNCATION_REQ).setValue(MagFreqDistParameterEditor.TRUNCATE_UPPER_ONLY);
       magEditor.getParameter(MagFreqDistParameterEditor.TRUNCATE_NUM_OF_STD_DEV).setValue(new Double(1.0));
     }
     // mag dist parameters for test case 7
     else if(selectedTest.equalsIgnoreCase(TEST_CASE_SEVEN)) {
       magEditor.getParameter(MagFreqDistParameterEditor.DISTRIBUTION_NAME).setValue(YC_1985_CharMagFreqDist.NAME);
       magEditor.getParameter(MagFreqDistParameterEditor.MIN).setValue(new Double(0.05));
       magEditor.getParameter(MagFreqDistParameterEditor.MAX).setValue(new Double(9.95));
       magEditor.getParameter(MagFreqDistParameterEditor.NUM).setValue(new Integer(100));
       magEditor.getParameter(MagFreqDistParameterEditor.GR_BVALUE).setValue(new Double(0.9));
       magEditor.getParameter(MagFreqDistParameterEditor.YC_DELTA_MAG_CHAR).setValue(new Double(0.5));
       magEditor.getParameter(MagFreqDistParameterEditor.YC_DELTA_MAG_PRIME).setValue(new Double(1.0));
       magEditor.getParameter(MagFreqDistParameterEditor.GR_MAG_LOWER).setValue(new Double(4.95));
       magEditor.getParameter(MagFreqDistParameterEditor.YC_MAG_PRIME).setValue(new Double(5.95));
       magEditor.getParameter(MagFreqDistParameterEditor.GR_MAG_UPPER).setValue(new Double(6.45));
       magEditor.getParameter(MagFreqDistParameterEditor.TOT_MO_RATE).setValue(new Double(1.8e16));
     }

     //mag dist parameters for the test case 8_1
     else if(selectedTest.equalsIgnoreCase(TEST_CASE_EIGHT_ONE)) {

       magEditor.getParameter(MagFreqDistParameterEditor.DISTRIBUTION_NAME).setValue(SingleMagFreqDist.NAME);
       magEditor.getParameter(MagFreqDistParameterEditor.SINGLE_PARAMS_TO_SET).setValue(MagFreqDistParameterEditor.MAG_AND_MO_RATE);
       magEditor.getParameter(MagFreqDistParameterEditor.MAG).setValue(new Double(6.0));
       magEditor.getParameter(MagFreqDistParameterEditor.MO_RATE).setValue(new Double(1.8e16));
     }

     //mag dist parameters for the test case 8_2
     else if(selectedTest.equalsIgnoreCase(TEST_CASE_EIGHT_TWO)) {

       magEditor.getParameter(MagFreqDistParameterEditor.DISTRIBUTION_NAME).setValue(SingleMagFreqDist.NAME);
       magEditor.getParameter(MagFreqDistParameterEditor.SINGLE_PARAMS_TO_SET).setValue(MagFreqDistParameterEditor.MAG_AND_MO_RATE);
       magEditor.getParameter(MagFreqDistParameterEditor.MAG).setValue(new Double(6.0));
       magEditor.getParameter(MagFreqDistParameterEditor.MO_RATE).setValue(new Double(1.8e16));
     }

     //mag dist parameters for the test case 8_3
     else if(selectedTest.equalsIgnoreCase(TEST_CASE_EIGHT_THREE)) {

       magEditor.getParameter(MagFreqDistParameterEditor.DISTRIBUTION_NAME).setValue(SingleMagFreqDist.NAME);
       magEditor.getParameter(MagFreqDistParameterEditor.SINGLE_PARAMS_TO_SET).setValue(MagFreqDistParameterEditor.MAG_AND_MO_RATE);
       magEditor.getParameter(MagFreqDistParameterEditor.MAG).setValue(new Double(6.0));
       magEditor.getParameter(MagFreqDistParameterEditor.MO_RATE).setValue(new Double(1.8e16));
     }

     //mag dist parameters for the test case 9_1
     else if(selectedTest.equalsIgnoreCase(TEST_CASE_NINE_ONE)) {

       magEditor.getParameter(MagFreqDistParameterEditor.DISTRIBUTION_NAME).setValue(SingleMagFreqDist.NAME);
       magEditor.getParameter(MagFreqDistParameterEditor.SINGLE_PARAMS_TO_SET).setValue(MagFreqDistParameterEditor.MAG_AND_MO_RATE);
       magEditor.getParameter(MagFreqDistParameterEditor.MAG).setValue(new Double(6.0));
       magEditor.getParameter(MagFreqDistParameterEditor.MO_RATE).setValue(new Double(1.905e16));
     }

     //mag dist parameters for the test case 9_2
     else if(selectedTest.equalsIgnoreCase(TEST_CASE_NINE_TWO)) {

       magEditor.getParameter(MagFreqDistParameterEditor.DISTRIBUTION_NAME).setValue(SingleMagFreqDist.NAME);
       magEditor.getParameter(MagFreqDistParameterEditor.SINGLE_PARAMS_TO_SET).setValue(MagFreqDistParameterEditor.MAG_AND_MO_RATE);
       magEditor.getParameter(MagFreqDistParameterEditor.MAG).setValue(new Double(6.0));
       magEditor.getParameter(MagFreqDistParameterEditor.MO_RATE).setValue(new Double(1.905e16));
     }

     //mag dist parameters for the test case 9_1
     else if(selectedTest.equalsIgnoreCase(TEST_CASE_NINE_THREE)) {

       magEditor.getParameter(MagFreqDistParameterEditor.DISTRIBUTION_NAME).setValue(SingleMagFreqDist.NAME);
       magEditor.getParameter(MagFreqDistParameterEditor.SINGLE_PARAMS_TO_SET).setValue(MagFreqDistParameterEditor.MAG_AND_MO_RATE);
       magEditor.getParameter(MagFreqDistParameterEditor.MAG).setValue(new Double(6.0));
       magEditor.getParameter(MagFreqDistParameterEditor.MO_RATE).setValue(new Double(1.905e16));
     }

     // mag dist parameters for test case 10
     else if(selectedTest.equalsIgnoreCase(TEST_CASE_TEN)) {
       magEditor.getParameter(MagFreqDistParameterEditor.DISTRIBUTION_NAME).setValue(GutenbergRichterMagFreqDist.NAME);
       magEditor.getParameter(MagFreqDistParameterEditor.MIN).setValue(new Double(0.05));
       magEditor.getParameter(MagFreqDistParameterEditor.MAX).setValue(new Double(9.95));
       magEditor.getParameter(MagFreqDistParameterEditor.NUM).setValue(new Integer(100));
       magEditor.getParameter(MagFreqDistParameterEditor.SET_ALL_PARAMS_BUT).setValue(MagFreqDistParameterEditor.TOT_MO_RATE);
       magEditor.getParameter(MagFreqDistParameterEditor.GR_MAG_LOWER).setValue(new Double(5.05));
       magEditor.getParameter(MagFreqDistParameterEditor.GR_MAG_UPPER).setValue(new Double(6.45));
       magEditor.getParameter(MagFreqDistParameterEditor.GR_BVALUE).setValue(new Double(0.9));
       magEditor.getParameter(MagFreqDistParameterEditor.TOT_CUM_RATE).setValue(new Double(.0395));
     }

     // mag dist parameters for test case 11
     else if(selectedTest.equalsIgnoreCase(TEST_CASE_ELEVEN)) {
       magEditor.getParameter(MagFreqDistParameterEditor.DISTRIBUTION_NAME).setValue(GutenbergRichterMagFreqDist.NAME);
       magEditor.getParameter(MagFreqDistParameterEditor.MIN).setValue(new Double(0.05));
       magEditor.getParameter(MagFreqDistParameterEditor.MAX).setValue(new Double(9.95));
       magEditor.getParameter(MagFreqDistParameterEditor.NUM).setValue(new Integer(100));
       magEditor.getParameter(MagFreqDistParameterEditor.SET_ALL_PARAMS_BUT).setValue(MagFreqDistParameterEditor.TOT_MO_RATE);
       magEditor.getParameter(MagFreqDistParameterEditor.GR_MAG_LOWER).setValue(new Double(5.05));
       magEditor.getParameter(MagFreqDistParameterEditor.GR_MAG_UPPER).setValue(new Double(6.45));
       magEditor.getParameter(MagFreqDistParameterEditor.GR_BVALUE).setValue(new Double(0.9));
       magEditor.getParameter(MagFreqDistParameterEditor.TOT_CUM_RATE).setValue(new Double(.0395));
     }

     // now have the editor create the magFreqDist
     magEditor.setMagDistFromParams();
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


     int size = v.size();
     for(int i=0;i<size;++i)
       this.testCaseComboBox.addItem(v.get(i));

   }

   /**
    * this sets the test case params in the GUI
    * @param e
    */
  void testCaseComboBox_actionPerformed(ActionEvent e) {
    String testSelected = this.testCaseComboBox.getSelectedItem().toString();
    setTestCaseAndSite(testSelected);
  }

}
