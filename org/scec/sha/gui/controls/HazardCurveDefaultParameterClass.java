package org.scec.sha.gui.controls;


import org.scec.sha.earthquake.PEER_TestCases.*;
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


/**
 *
 * <p>Title: GroupTestDefaultParameterClass</p>
 * <p>Description: this class sets the default parameters for the selected test
 * in the HazardCurveApplet</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author : Nitin Gupta and Vipin Gupta
 * @version 1.0
 */
public class HazardCurveDefaultParameterClass {


  protected final static String C = "GroupTestDefaultParameterClass";
  protected final static boolean D = false;

  private  PEER_FaultForecast peer_Fault_ERF;
  private  PEER_AreaForecast peer_Area_ERF;
  private  PEER_NonPlanarFaultForecast peer_NonPlanar_ERF;
  private  PEER_ListricFaultForecast peer_Listric_ERF;
  private  PEER_MultiSourceForecast peer_MultiSource_ERF;

  protected HazardCurveGuiBean hazardCurveGuiBean;



  public HazardCurveDefaultParameterClass(HazardCurveGuiBean hazardCurveGuiBean){

    this.hazardCurveGuiBean = hazardCurveGuiBean;
    // hard coded values for setting in the test cases
    peer_Fault_ERF = (PEER_FaultForecast)hazardCurveGuiBean.getErfVector().get(0);
    peer_Area_ERF =  (PEER_AreaForecast)hazardCurveGuiBean.getErfVector().get(1);
    peer_NonPlanar_ERF =(PEER_NonPlanarFaultForecast)hazardCurveGuiBean.getErfVector().get(2);
    peer_Listric_ERF =(PEER_ListricFaultForecast)hazardCurveGuiBean.getErfVector().get(3);
    peer_MultiSource_ERF = (PEER_MultiSourceForecast)hazardCurveGuiBean.getErfVector().get(4);
  }


  /**
   * This function sets the site Paramters and the IMR parameters based on the
   * selected test case and selected site number for that test case
   * @param siteNumber
   */

  public void setParams() {
    String S = C + ":setParams()";
    if(D) System.out.println(S+"::entering");

    //Gets the siteParamList
    ParameterList siteParams = hazardCurveGuiBean.getSiteEditor().getParameterList();

    //if set-1 PEER test case is selected
    if(hazardCurveGuiBean.getSelectedSet().equalsIgnoreCase(HazardCurveApplet.PEER_TESTS_SET_ONE))
      set_Set1Params(siteParams);

    //if set-2 PEER test case is selected
    else if(hazardCurveGuiBean.getSelectedSet().equalsIgnoreCase(HazardCurveApplet.PEER_TESTS_SET_TWO))
      set_Set2Params(siteParams);

    // refresh the editor according to parameter values
    hazardCurveGuiBean.getImrEditor().synchToModel();
    hazardCurveGuiBean.getIMTEditor().synchToModel();
    hazardCurveGuiBean.getSiteEditor().synchToModel();
    hazardCurveGuiBean.get_erf_Editor().synchToModel();
  }


  /**
   * sets the parameter values for the selected test cases in Set-1
   * @param siteParams
   */
  private void set_Set1Params(ParameterList siteParams){


    // ******* Set the IMR, IMT, & Site-Related Parameters (except lat and lon) first ************

    /*   the following settings apply to most test cases; these are subsequently
         overridded where needed below */
    hazardCurveGuiBean.getIMRParamList().getParameter(hazardCurveGuiBean.IMR_PARAM_NAME).setValue(SCEMY_1997_AttenRel.NAME);
    hazardCurveGuiBean.getIMRParamList().getParameter(AttenuationRelationship.SIGMA_TRUNC_TYPE_NAME).setValue(AttenuationRelationship.SIGMA_TRUNC_TYPE_NONE);
    hazardCurveGuiBean.getIMRParamList().getParameter(AttenuationRelationship.STD_DEV_TYPE_NAME).setValue(AttenuationRelationship.STD_DEV_TYPE_NONE);
    hazardCurveGuiBean.getIMTParamList().getParameter(hazardCurveGuiBean.IMT_PARAM_NAME).setValue(AttenuationRelationship.PGA_NAME);
    siteParams.getParameter(SCEMY_1997_AttenRel.SITE_TYPE_NAME).setValue(SCEMY_1997_AttenRel.SITE_TYPE_ROCK);

    //if the selected test case is number 8_1
    if(hazardCurveGuiBean.getSelectedTest().equals(HazardCurveApplet.TEST_CASE_EIGHT_ONE)){
      hazardCurveGuiBean.getIMRParamList().getParameter(AttenuationRelationship.SIGMA_TRUNC_TYPE_NAME).setValue(AttenuationRelationship.SIGMA_TRUNC_TYPE_NONE);
      hazardCurveGuiBean.getIMRParamList().getParameter(AttenuationRelationship.STD_DEV_TYPE_NAME).setValue(AttenuationRelationship.STD_DEV_TYPE_TOTAL);
    }

    //if the selected test case is number 8_2
    if(hazardCurveGuiBean.getSelectedTest().equals(HazardCurveApplet.TEST_CASE_EIGHT_TWO)){
      hazardCurveGuiBean.getIMRParamList().getParameter(AttenuationRelationship.SIGMA_TRUNC_TYPE_NAME).setValue(AttenuationRelationship.SIGMA_TRUNC_TYPE_1SIDED);
      hazardCurveGuiBean.getIMRParamList().getParameter(AttenuationRelationship.SIGMA_TRUNC_LEVEL_NAME).setValue(new Double(2.0));
      hazardCurveGuiBean.getIMRParamList().getParameter(AttenuationRelationship.STD_DEV_TYPE_NAME).setValue(AttenuationRelationship.STD_DEV_TYPE_TOTAL);
    }

    //if the selected test case is number 8_3
    if(hazardCurveGuiBean.getSelectedTest().equals(HazardCurveApplet.TEST_CASE_EIGHT_THREE)){
      hazardCurveGuiBean.getIMRParamList().getParameter(AttenuationRelationship.SIGMA_TRUNC_TYPE_NAME).setValue(AttenuationRelationship.SIGMA_TRUNC_TYPE_1SIDED);
      hazardCurveGuiBean.getIMRParamList().getParameter(AttenuationRelationship.SIGMA_TRUNC_LEVEL_NAME).setValue(new Double(3.0));
      hazardCurveGuiBean.getIMRParamList().getParameter(AttenuationRelationship.STD_DEV_TYPE_NAME).setValue(AttenuationRelationship.STD_DEV_TYPE_TOTAL);
    }

    //if the selected test case is number 9_1
    if(hazardCurveGuiBean.getSelectedTest().equals(HazardCurveApplet.TEST_CASE_NINE_ONE)){
      hazardCurveGuiBean.getIMRParamList().getParameter(hazardCurveGuiBean.IMR_PARAM_NAME).setValue(SCEMY_1997_AttenRel.NAME);
      hazardCurveGuiBean.getIMRParamList().getParameter(AttenuationRelationship.SIGMA_TRUNC_TYPE_NAME).setValue(AttenuationRelationship.SIGMA_TRUNC_TYPE_1SIDED);
      hazardCurveGuiBean.getIMRParamList().getParameter(AttenuationRelationship.SIGMA_TRUNC_LEVEL_NAME).setValue(new Double(3.0));
      hazardCurveGuiBean.getIMRParamList().getParameter(AttenuationRelationship.STD_DEV_TYPE_NAME).setValue(AttenuationRelationship.STD_DEV_TYPE_TOTAL);
      hazardCurveGuiBean.getIMTParamList().getParameter(hazardCurveGuiBean.IMT_PARAM_NAME).setValue(AttenuationRelationship.PGA_NAME);
      siteParams.getParameter(SCEMY_1997_AttenRel.SITE_TYPE_NAME).setValue(SCEMY_1997_AttenRel.SITE_TYPE_ROCK);
    }

    //if the selected test case is number 9_2
    if(hazardCurveGuiBean.getSelectedTest().equals(HazardCurveApplet.TEST_CASE_NINE_TWO)){
      hazardCurveGuiBean.getIMRParamList().getParameter(hazardCurveGuiBean.IMR_PARAM_NAME).setValue(AS_1997_AttenRel.NAME);
      hazardCurveGuiBean.getIMRParamList().getParameter(AttenuationRelationship.SIGMA_TRUNC_TYPE_NAME).setValue(AttenuationRelationship.SIGMA_TRUNC_TYPE_1SIDED);
      hazardCurveGuiBean.getIMRParamList().getParameter(AttenuationRelationship.SIGMA_TRUNC_LEVEL_NAME).setValue(new Double(3.0));
      hazardCurveGuiBean.getIMRParamList().getParameter(AttenuationRelationship.STD_DEV_TYPE_NAME).setValue(AttenuationRelationship.STD_DEV_TYPE_TOTAL);
      hazardCurveGuiBean.getIMTParamList().getParameter(hazardCurveGuiBean.IMT_PARAM_NAME).setValue(AttenuationRelationship.PGA_NAME);
      siteParams.getParameter(AS_1997_AttenRel.SITE_TYPE_NAME).setValue(AS_1997_AttenRel.SITE_TYPE_ROCK);
    }

    //if the selected test case is number 9_3
    if(hazardCurveGuiBean.getSelectedTest().equals(HazardCurveApplet.TEST_CASE_NINE_THREE)){
      hazardCurveGuiBean.getIMRParamList().getParameter(hazardCurveGuiBean.IMR_PARAM_NAME).setValue(Campbell_1997_AttenRel.NAME);
      hazardCurveGuiBean.getIMRParamList().getParameter(AttenuationRelationship.SIGMA_TRUNC_TYPE_NAME).setValue(AttenuationRelationship.SIGMA_TRUNC_TYPE_1SIDED);
      hazardCurveGuiBean.getIMRParamList().getParameter(AttenuationRelationship.SIGMA_TRUNC_LEVEL_NAME).setValue(new Double(3.0));
      hazardCurveGuiBean.getIMRParamList().getParameter(AttenuationRelationship.STD_DEV_TYPE_NAME).setValue(Campbell_1997_AttenRel.STD_DEV_TYPE_MAG_DEP);
      hazardCurveGuiBean.getIMTParamList().getParameter(hazardCurveGuiBean.IMT_PARAM_NAME).setValue(AttenuationRelationship.PGA_NAME);
      hazardCurveGuiBean.getSiteEditor().getParameterList().getParameter(Campbell_1997_AttenRel.SITE_TYPE_NAME).setValue(Campbell_1997_AttenRel.SITE_TYPE_SOFT_ROCK);
      siteParams.getParameter(Campbell_1997_AttenRel.BASIN_DEPTH_NAME).setValue(new Double(2.0));
    }

    // *********** Now fill in the forecast parameters ************************

    // if it's one of the "PEER fault" problems (cases 1-9)
    if(!hazardCurveGuiBean.getSelectedTest().equalsIgnoreCase(HazardCurveApplet.TEST_CASE_TEN) && !hazardCurveGuiBean.getSelectedTest().equalsIgnoreCase(HazardCurveApplet.TEST_CASE_ELEVEN)) {

      // set the ERF
      hazardCurveGuiBean.getERF_IndParamList().getParameter(hazardCurveGuiBean.ERF_PARAM_NAME).setValue(peer_Fault_ERF.getName());

      // set the common parameters like timespan, grid spacing
      hazardCurveGuiBean.getERF_IndParamList().getParameter(peer_Fault_ERF.SIGMA_PARAM_NAME).setValue(new Double(0));
      hazardCurveGuiBean.getERF_IndParamList().getParameter(peer_Fault_ERF.TIMESPAN_PARAM_NAME).setValue(new Double(1.0));
      hazardCurveGuiBean.getERF_IndParamList().getParameter(peer_Fault_ERF.GRID_PARAM_NAME).setValue(new Double(1.0));
      hazardCurveGuiBean.getERF_IndParamList().getParameter(peer_Fault_ERF.OFFSET_PARAM_NAME).setValue(new Double(1.0));

      // magLengthSigma parameter is changed if the test case chosen is 3
      if(hazardCurveGuiBean.getSelectedTest().equals(HazardCurveApplet.TEST_CASE_THREE))
        hazardCurveGuiBean.getERF_IndParamList().getParameter(peer_Fault_ERF.SIGMA_PARAM_NAME).setValue(new Double(0.2));

      // set the dip and rake
      if( hazardCurveGuiBean.getSelectedTest().equals(HazardCurveApplet.TEST_CASE_FOUR) ||
          hazardCurveGuiBean.getSelectedTest().equals(HazardCurveApplet.TEST_CASE_NINE_ONE) ||
          hazardCurveGuiBean.getSelectedTest().equals(HazardCurveApplet.TEST_CASE_NINE_TWO) ||
          hazardCurveGuiBean.getSelectedTest().equals(HazardCurveApplet.TEST_CASE_NINE_THREE) ) {
               hazardCurveGuiBean.getERF_IndParamList().getParameter(peer_Fault_ERF.DIP_PARAM_NAME).setValue(new Double(60.0));
               hazardCurveGuiBean.getERF_IndParamList().getParameter(peer_Fault_ERF.RAKE_PARAM_NAME).setValue(new Double(90.0));
      }
      else {
        hazardCurveGuiBean.getERF_IndParamList().getParameter(peer_Fault_ERF.DIP_PARAM_NAME).setValue(new Double(90.0));
        hazardCurveGuiBean.getERF_IndParamList().getParameter(peer_Fault_ERF.RAKE_PARAM_NAME).setValue(new Double(0.0));
      }

    }
    else {// if it area case
      hazardCurveGuiBean.getERF_IndParamList().getParameter(hazardCurveGuiBean.ERF_PARAM_NAME).setValue(peer_Area_ERF.getName());

      hazardCurveGuiBean.getERF_IndParamList().getParameter(peer_Area_ERF.DEPTH_UPPER_PARAM_NAME).setValue(new Double(5));
      hazardCurveGuiBean.getERF_IndParamList().getParameter(peer_Area_ERF.DIP_PARAM_NAME).setValue(new Double(90));
      hazardCurveGuiBean.getERF_IndParamList().getParameter(peer_Area_ERF.RAKE_PARAM_NAME).setValue(new Double(0));
      hazardCurveGuiBean.getERF_IndParamList().getParameter(peer_Area_ERF.TIMESPAN_PARAM_NAME).setValue(new Double(1.0));
      hazardCurveGuiBean.getERF_IndParamList().getParameter(peer_Area_ERF.GRID_PARAM_NAME).setValue(new Double(1.0));
      if(hazardCurveGuiBean.getSelectedTest().equals(HazardCurveApplet.TEST_CASE_TEN))
        hazardCurveGuiBean.getERF_IndParamList().getParameter(peer_Area_ERF.DEPTH_LOWER_PARAM_NAME).setValue(new Double(5));
      else
        hazardCurveGuiBean.getERF_IndParamList().getParameter(peer_Area_ERF.DEPTH_LOWER_PARAM_NAME).setValue(new Double(10));
    }

    // set magFreqDist parameters using seperate method
    MagFreqDistParameterEditor magDistEditor = hazardCurveGuiBean.getMagDistEditor();
    setMagDistParams_Set1(magDistEditor);


    // *********** set the Site latitude and longitude  **************************

    if(!hazardCurveGuiBean.getSelectedTest().equalsIgnoreCase(HazardCurveApplet.TEST_CASE_TEN) && !hazardCurveGuiBean.getSelectedTest().equalsIgnoreCase(HazardCurveApplet.TEST_CASE_ELEVEN)) {

      // for fault site 1
      if(hazardCurveGuiBean.getSelectedSite().equals(HazardCurveApplet.SITE_ONE)) {
        hazardCurveGuiBean.getSiteEditor().getParameterList().getParameter(hazardCurveGuiBean.getSiteEditor().LATITUDE).setValue(new Double(38.113));
        siteParams.getParameter(hazardCurveGuiBean.getSiteEditor().LONGITUDE).setValue(new Double(-122.0));
      }
      // for fault site 2
      if(hazardCurveGuiBean.getSelectedSite().equals(HazardCurveApplet.SITE_TWO)) {
        siteParams.getParameter(hazardCurveGuiBean.getSiteEditor().LATITUDE).setValue(new Double(38.113));
        siteParams.getParameter(hazardCurveGuiBean.getSiteEditor().LONGITUDE).setValue(new Double(-122.114));

      }
      // for fault site 3
      if(hazardCurveGuiBean.getSelectedSite().equals(HazardCurveApplet.SITE_THREE)) {
        siteParams.getParameter(hazardCurveGuiBean.getSiteEditor().LATITUDE).setValue(new Double(38.111));
        siteParams.getParameter(hazardCurveGuiBean.getSiteEditor().LONGITUDE).setValue(new Double(-122.570));

      }
      // for fault site 4
      if(hazardCurveGuiBean.getSelectedSite().equals(HazardCurveApplet.SITE_FOUR)) {
        siteParams.getParameter(hazardCurveGuiBean.getSiteEditor().LATITUDE).setValue(new Double(38.000));
        siteParams.getParameter(hazardCurveGuiBean.getSiteEditor().LONGITUDE).setValue(new Double(-122.0));

      }
      // for fault site 5
      if(hazardCurveGuiBean.getSelectedSite().equals(HazardCurveApplet.SITE_FIVE)) {
        siteParams.getParameter(hazardCurveGuiBean.getSiteEditor().LATITUDE).setValue(new Double(37.910));
        siteParams.getParameter(hazardCurveGuiBean.getSiteEditor().LONGITUDE).setValue(new Double(-122.0));

      }
      // for fault site 6
      if(hazardCurveGuiBean.getSelectedSite().equals(HazardCurveApplet.SITE_SIX)) {
        siteParams.getParameter(hazardCurveGuiBean.getSiteEditor().LATITUDE).setValue(new Double(38.225));
        siteParams.getParameter(hazardCurveGuiBean.getSiteEditor().LONGITUDE).setValue(new Double(-122.0));

      }
      // for fault site 7
      if(hazardCurveGuiBean.getSelectedSite().equals(HazardCurveApplet.SITE_SEVEN)) {
        siteParams.getParameter(hazardCurveGuiBean.getSiteEditor().LATITUDE).setValue(new Double(38.113));
        siteParams.getParameter(hazardCurveGuiBean.getSiteEditor().LONGITUDE).setValue(new Double(-121.886));
      }
    } else { // for area sites

      siteParams.getParameter(hazardCurveGuiBean.getSiteEditor().LONGITUDE).setValue(new Double(-122.0));
      // for area site 1
      if(hazardCurveGuiBean.getSelectedSite().equals(HazardCurveApplet.SITE_ONE))
        siteParams.getParameter(hazardCurveGuiBean.getSiteEditor().LATITUDE).setValue(new Double(38.0));

      // for area site 2
      if(hazardCurveGuiBean.getSelectedSite().equals(HazardCurveApplet.SITE_TWO))
        siteParams.getParameter(hazardCurveGuiBean.getSiteEditor().LATITUDE).setValue(new Double(37.550));

      // for area site 3
      if(hazardCurveGuiBean.getSelectedSite().equals(HazardCurveApplet.SITE_THREE))
        siteParams.getParameter(hazardCurveGuiBean.getSiteEditor().LATITUDE).setValue(new Double(37.099));

      // for area site 4
      if(hazardCurveGuiBean.getSelectedSite().equals(HazardCurveApplet.SITE_FOUR))
        siteParams.getParameter(hazardCurveGuiBean.getSiteEditor().LATITUDE).setValue(new Double(36.874));
    }
  }



  /**
   * sets the parameter values for the selected test cases in Set-2
   * @param siteParams
   */
  private void set_Set2Params(ParameterList siteParams){


    // ******* Set the IMR, IMT, & Site-Related Parameters (except lat and lon) first ************

    hazardCurveGuiBean.getIMRParamList().getParameter(hazardCurveGuiBean.IMR_PARAM_NAME).setValue(SCEMY_1997_AttenRel.NAME);
    hazardCurveGuiBean.getIMRParamList().getParameter(AttenuationRelationship.SIGMA_TRUNC_TYPE_NAME).setValue(AttenuationRelationship.SIGMA_TRUNC_TYPE_NONE);
    hazardCurveGuiBean.getIMRParamList().getParameter(AttenuationRelationship.STD_DEV_TYPE_NAME).setValue(AttenuationRelationship.STD_DEV_TYPE_NONE);
    hazardCurveGuiBean.getIMTParamList().getParameter(hazardCurveGuiBean.IMT_PARAM_NAME).setValue(AttenuationRelationship.PGA_NAME);
    siteParams.getParameter(SCEMY_1997_AttenRel.SITE_TYPE_NAME).setValue(SCEMY_1997_AttenRel.SITE_TYPE_ROCK);

    // change IMR sigma if it's Case 2
    if(hazardCurveGuiBean.getSelectedTest().equalsIgnoreCase(HazardCurveApplet.TEST_CASE_TWO)){
      hazardCurveGuiBean.getIMRParamList().getParameter(AttenuationRelationship.SIGMA_TRUNC_TYPE_NAME).setValue(AttenuationRelationship.SIGMA_TRUNC_TYPE_1SIDED);
      hazardCurveGuiBean.getIMRParamList().getParameter(AttenuationRelationship.SIGMA_TRUNC_LEVEL_NAME).setValue(new Double(3.0));
      hazardCurveGuiBean.getIMRParamList().getParameter(AttenuationRelationship.STD_DEV_TYPE_NAME).setValue(AttenuationRelationship.STD_DEV_TYPE_TOTAL);

    }


    // ********* set the site latitude and longitude ************

    if(hazardCurveGuiBean.getSelectedTest().equalsIgnoreCase(HazardCurveApplet.TEST_CASE_TWO)){
      siteParams.getParameter(hazardCurveGuiBean.getSiteEditor().LONGITUDE).setValue(new Double(-122));
      if(hazardCurveGuiBean.getSelectedSite().equalsIgnoreCase(HazardCurveApplet.SITE_ONE)){
        siteParams.getParameter(hazardCurveGuiBean.getSiteEditor().LATITUDE).setValue(new Double(37.5495));
      }
      else if(hazardCurveGuiBean.getSelectedSite().equalsIgnoreCase(HazardCurveApplet.SITE_TWO)){
        siteParams.getParameter(hazardCurveGuiBean.getSiteEditor().LATITUDE).setValue(new Double(37.0990));
      }
      else if(hazardCurveGuiBean.getSelectedSite().equalsIgnoreCase(HazardCurveApplet.SITE_THREE)){
        siteParams.getParameter(hazardCurveGuiBean.getSiteEditor().LATITUDE).setValue(new Double(36.8737));
      }
    }
    else {
      if(hazardCurveGuiBean.getSelectedSite().equalsIgnoreCase(HazardCurveApplet.SITE_ONE)){
        siteParams.getParameter(hazardCurveGuiBean.getSiteEditor().LONGITUDE).setValue(new Double(-121.886));
        siteParams.getParameter(hazardCurveGuiBean.getSiteEditor().LATITUDE).setValue(new Double(38.1126));
      }
      else if(hazardCurveGuiBean.getSelectedSite().equalsIgnoreCase(HazardCurveApplet.SITE_TWO)){
        siteParams.getParameter(hazardCurveGuiBean.getSiteEditor().LONGITUDE).setValue(new Double(-122.0));
        siteParams.getParameter(hazardCurveGuiBean.getSiteEditor().LATITUDE).setValue(new Double(38.2252));
      }
      else if(hazardCurveGuiBean.getSelectedSite().equalsIgnoreCase(HazardCurveApplet.SITE_THREE)){
        siteParams.getParameter(hazardCurveGuiBean.getSiteEditor().LONGITUDE).setValue(new Double(-122.0));
        siteParams.getParameter(hazardCurveGuiBean.getSiteEditor().LATITUDE).setValue(new Double(38.0));
      }
    }

    // ************ Set the ERF parameters ********************

    //if test case -1
    if(hazardCurveGuiBean.getSelectedTest().equalsIgnoreCase(HazardCurveApplet.TEST_CASE_ONE)){
      hazardCurveGuiBean.getERF_IndParamList().getParameter(hazardCurveGuiBean.ERF_PARAM_NAME).setValue(peer_NonPlanar_ERF.getName());

      // add sigma for maglength(0-1)
      hazardCurveGuiBean.getERF_IndParamList().getParameter(peer_NonPlanar_ERF.SIGMA_PARAM_NAME).setValue(new Double(0));
      hazardCurveGuiBean.getERF_IndParamList().getParameter(peer_NonPlanar_ERF.TIMESPAN_PARAM_NAME).setValue(new Double(1.0));
      hazardCurveGuiBean.getERF_IndParamList().getParameter(peer_NonPlanar_ERF.GRID_PARAM_NAME).setValue(new Double(1.0));
      hazardCurveGuiBean.getERF_IndParamList().getParameter(peer_NonPlanar_ERF.OFFSET_PARAM_NAME).setValue(new Double(1.0));
      hazardCurveGuiBean.getERF_IndParamList().getParameter(peer_NonPlanar_ERF.GR_MAG_UPPER).setValue(new Double(7.15));
      hazardCurveGuiBean.getERF_IndParamList().getParameter(peer_NonPlanar_ERF.SLIP_RATE_NAME).setValue(new Double(2.0));
      hazardCurveGuiBean.getERF_IndParamList().getParameter(peer_NonPlanar_ERF.SEGMENTATION_NAME).setValue(peer_NonPlanar_ERF.SEGMENTATION_NONE);

    }

    //if test case -2
    if(hazardCurveGuiBean.getSelectedTest().equalsIgnoreCase(HazardCurveApplet.TEST_CASE_TWO)){
      hazardCurveGuiBean.getERF_IndParamList().getParameter(hazardCurveGuiBean.ERF_PARAM_NAME).setValue(this.peer_MultiSource_ERF.getName());

      hazardCurveGuiBean.getERF_IndParamList().getParameter(peer_MultiSource_ERF.DEPTH_LOWER_PARAM_NAME).setValue(new Double(10));
      hazardCurveGuiBean.getERF_IndParamList().getParameter(peer_MultiSource_ERF.DEPTH_UPPER_PARAM_NAME).setValue(new Double(5));
      hazardCurveGuiBean.getERF_IndParamList().getParameter(peer_MultiSource_ERF.GRID_PARAM_NAME).setValue(new Double(1.0));
      hazardCurveGuiBean.getERF_IndParamList().getParameter(peer_MultiSource_ERF.OFFSET_PARAM_NAME).setValue(new Double(1.0));
      hazardCurveGuiBean.getERF_IndParamList().getParameter(peer_MultiSource_ERF.TIMESPAN_PARAM_NAME).setValue(new Double(1.0));
    }

    //if test case 3 or 4
    if(hazardCurveGuiBean.getSelectedTest().equalsIgnoreCase(HazardCurveApplet.TEST_CASE_THREE) || hazardCurveGuiBean.getSelectedTest().equalsIgnoreCase(HazardCurveApplet.TEST_CASE_FOUR) ) {
      hazardCurveGuiBean.getERF_IndParamList().getParameter(hazardCurveGuiBean.ERF_PARAM_NAME).setValue(this.peer_Fault_ERF.getName());

      hazardCurveGuiBean.getERF_IndParamList().getParameter(peer_Fault_ERF.DIP_PARAM_NAME).setValue(new Double(90.0));
      hazardCurveGuiBean.getERF_IndParamList().getParameter(peer_Fault_ERF.GRID_PARAM_NAME).setValue(new Double(1.0));
      hazardCurveGuiBean.getERF_IndParamList().getParameter(peer_Fault_ERF.OFFSET_PARAM_NAME).setValue(new Double(1.0));
      hazardCurveGuiBean.getERF_IndParamList().getParameter(peer_Fault_ERF.RAKE_PARAM_NAME).setValue(new Double(0.0));
      hazardCurveGuiBean.getERF_IndParamList().getParameter(peer_Fault_ERF.SIGMA_PARAM_NAME).setValue(new Double(0.0));
      hazardCurveGuiBean.getERF_IndParamList().getParameter(peer_Fault_ERF.TIMESPAN_PARAM_NAME).setValue(new Double(1.0));
    }

    //if test case -6
    if(hazardCurveGuiBean.getSelectedTest().equalsIgnoreCase(HazardCurveApplet.TEST_CASE_SIX)){
      hazardCurveGuiBean.getERF_IndParamList().getParameter(hazardCurveGuiBean.ERF_PARAM_NAME).setValue(this.peer_Listric_ERF.getName());

      hazardCurveGuiBean.getERF_IndParamList().getParameter(peer_Listric_ERF.GRID_PARAM_NAME).setValue(new Double(1.0));
      hazardCurveGuiBean.getERF_IndParamList().getParameter(peer_Listric_ERF.OFFSET_PARAM_NAME).setValue(new Double(1.0));
      hazardCurveGuiBean.getERF_IndParamList().getParameter(peer_Listric_ERF.RAKE_PARAM_NAME).setValue(new Double(0.0));
      hazardCurveGuiBean.getERF_IndParamList().getParameter(peer_Listric_ERF.SIGMA_PARAM_NAME).setValue(new Double(0.0));
      hazardCurveGuiBean.getERF_IndParamList().getParameter(peer_Listric_ERF.TIMESPAN_PARAM_NAME).setValue(new Double(1.0));
    }

    // now set the magFreqDist parameters (if there is one) using the separate method
    MagFreqDistParameterEditor magDistEditor =hazardCurveGuiBean.getMagDistEditor();
    if(magDistEditor !=null)
      setMagDistParams_Set2(magDistEditor);

 }



    /**
     * Sets the default magdist values for the set 2 (only cases 3, 4, and 6 have magFreqDist as
     * an adjustable parameter
     * @param magEditor
     */
    private void setMagDistParams_Set2(MagFreqDistParameterEditor magEditor){

       // mag dist parameters for test case 3
      if(hazardCurveGuiBean.getSelectedTest().equalsIgnoreCase(HazardCurveApplet.TEST_CASE_THREE)){
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
      if(hazardCurveGuiBean.getSelectedTest().equalsIgnoreCase(HazardCurveApplet.TEST_CASE_FOUR)){
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
      if(hazardCurveGuiBean.getSelectedTest().equalsIgnoreCase(HazardCurveApplet.TEST_CASE_SIX)){
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
      if(hazardCurveGuiBean.getSelectedTest().equalsIgnoreCase(HazardCurveApplet.TEST_CASE_ONE)) {
        magEditor.getParameter(MagFreqDistParameterEditor.DISTRIBUTION_NAME).setValue(SingleMagFreqDist.NAME);
        magEditor.getParameter(MagFreqDistParameterEditor.SINGLE_PARAMS_TO_SET).setValue(MagFreqDistParameterEditor.MAG_AND_MO_RATE);
        magEditor.getParameter(MagFreqDistParameterEditor.MAG).setValue(new Double(6.5));
        magEditor.getParameter(MagFreqDistParameterEditor.MO_RATE).setValue(new Double(1.8e16));
      }

      // mag dist parameters  for test case 2
      else if(hazardCurveGuiBean.getSelectedTest().equalsIgnoreCase(HazardCurveApplet.TEST_CASE_TWO)) {

        magEditor.getParameter(MagFreqDistParameterEditor.DISTRIBUTION_NAME).setValue(SingleMagFreqDist.NAME);
        magEditor.getParameter(MagFreqDistParameterEditor.SINGLE_PARAMS_TO_SET).setValue(MagFreqDistParameterEditor.MAG_AND_MO_RATE);
        magEditor.getParameter(MagFreqDistParameterEditor.MAG).setValue(new Double(6.0));
        magEditor.getParameter(MagFreqDistParameterEditor.MO_RATE).setValue(new Double(1.8e16));
      }

      // mag dist parameters  for test case 3
      else if(hazardCurveGuiBean.getSelectedTest().equalsIgnoreCase(HazardCurveApplet.TEST_CASE_THREE)) {

        magEditor.getParameter(MagFreqDistParameterEditor.DISTRIBUTION_NAME).setValue(SingleMagFreqDist.NAME);
        magEditor.getParameter(MagFreqDistParameterEditor.SINGLE_PARAMS_TO_SET).setValue(MagFreqDistParameterEditor.MAG_AND_MO_RATE);
        magEditor.getParameter(MagFreqDistParameterEditor.MAG).setValue(new Double(6.0));
        magEditor.getParameter(MagFreqDistParameterEditor.MO_RATE).setValue(new Double(1.8e16));
      }

      // mag dist parameters for test case 4
      else if(hazardCurveGuiBean.getSelectedTest().equalsIgnoreCase(HazardCurveApplet.TEST_CASE_FOUR)) {

        magEditor.getParameter(MagFreqDistParameterEditor.DISTRIBUTION_NAME).setValue(SingleMagFreqDist.NAME);
        magEditor.getParameter(MagFreqDistParameterEditor.SINGLE_PARAMS_TO_SET).setValue(MagFreqDistParameterEditor.MAG_AND_MO_RATE);
        magEditor.getParameter(MagFreqDistParameterEditor.MAG).setValue(new Double(6.0));
        magEditor.getParameter(MagFreqDistParameterEditor.MO_RATE).setValue(new Double(1.905e16));
      }

      // mag dist parameters for test case 5
      else if(hazardCurveGuiBean.getSelectedTest().equalsIgnoreCase(HazardCurveApplet.TEST_CASE_FIVE)) {
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
      else if(hazardCurveGuiBean.getSelectedTest().equalsIgnoreCase(HazardCurveApplet.TEST_CASE_SIX)) {
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
      else if(hazardCurveGuiBean.getSelectedTest().equalsIgnoreCase(HazardCurveApplet.TEST_CASE_SEVEN)) {
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
      else if(hazardCurveGuiBean.getSelectedTest().equalsIgnoreCase(HazardCurveApplet.TEST_CASE_EIGHT_ONE)) {

        magEditor.getParameter(MagFreqDistParameterEditor.DISTRIBUTION_NAME).setValue(SingleMagFreqDist.NAME);
        magEditor.getParameter(MagFreqDistParameterEditor.SINGLE_PARAMS_TO_SET).setValue(MagFreqDistParameterEditor.MAG_AND_MO_RATE);
        magEditor.getParameter(MagFreqDistParameterEditor.MAG).setValue(new Double(6.0));
        magEditor.getParameter(MagFreqDistParameterEditor.MO_RATE).setValue(new Double(1.8e16));
      }

      //mag dist parameters for the test case 8_2
      else if(hazardCurveGuiBean.getSelectedTest().equalsIgnoreCase(HazardCurveApplet.TEST_CASE_EIGHT_TWO)) {

        magEditor.getParameter(MagFreqDistParameterEditor.DISTRIBUTION_NAME).setValue(SingleMagFreqDist.NAME);
        magEditor.getParameter(MagFreqDistParameterEditor.SINGLE_PARAMS_TO_SET).setValue(MagFreqDistParameterEditor.MAG_AND_MO_RATE);
        magEditor.getParameter(MagFreqDistParameterEditor.MAG).setValue(new Double(6.0));
        magEditor.getParameter(MagFreqDistParameterEditor.MO_RATE).setValue(new Double(1.8e16));
      }

      //mag dist parameters for the test case 8_3
      else if(hazardCurveGuiBean.getSelectedTest().equalsIgnoreCase(HazardCurveApplet.TEST_CASE_EIGHT_THREE)) {

        magEditor.getParameter(MagFreqDistParameterEditor.DISTRIBUTION_NAME).setValue(SingleMagFreqDist.NAME);
        magEditor.getParameter(MagFreqDistParameterEditor.SINGLE_PARAMS_TO_SET).setValue(MagFreqDistParameterEditor.MAG_AND_MO_RATE);
        magEditor.getParameter(MagFreqDistParameterEditor.MAG).setValue(new Double(6.0));
        magEditor.getParameter(MagFreqDistParameterEditor.MO_RATE).setValue(new Double(1.8e16));
      }

      //mag dist parameters for the test case 9_1
      else if(hazardCurveGuiBean.getSelectedTest().equalsIgnoreCase(HazardCurveApplet.TEST_CASE_NINE_ONE)) {

        magEditor.getParameter(MagFreqDistParameterEditor.DISTRIBUTION_NAME).setValue(SingleMagFreqDist.NAME);
        magEditor.getParameter(MagFreqDistParameterEditor.SINGLE_PARAMS_TO_SET).setValue(MagFreqDistParameterEditor.MAG_AND_MO_RATE);
        magEditor.getParameter(MagFreqDistParameterEditor.MAG).setValue(new Double(6.0));
        magEditor.getParameter(MagFreqDistParameterEditor.MO_RATE).setValue(new Double(1.905e16));
      }

      //mag dist parameters for the test case 9_2
      else if(hazardCurveGuiBean.getSelectedTest().equalsIgnoreCase(HazardCurveApplet.TEST_CASE_NINE_TWO)) {

        magEditor.getParameter(MagFreqDistParameterEditor.DISTRIBUTION_NAME).setValue(SingleMagFreqDist.NAME);
        magEditor.getParameter(MagFreqDistParameterEditor.SINGLE_PARAMS_TO_SET).setValue(MagFreqDistParameterEditor.MAG_AND_MO_RATE);
        magEditor.getParameter(MagFreqDistParameterEditor.MAG).setValue(new Double(6.0));
        magEditor.getParameter(MagFreqDistParameterEditor.MO_RATE).setValue(new Double(1.905e16));
      }

      //mag dist parameters for the test case 9_1
      else if(hazardCurveGuiBean.getSelectedTest().equalsIgnoreCase(HazardCurveApplet.TEST_CASE_NINE_THREE)) {

        magEditor.getParameter(MagFreqDistParameterEditor.DISTRIBUTION_NAME).setValue(SingleMagFreqDist.NAME);
        magEditor.getParameter(MagFreqDistParameterEditor.SINGLE_PARAMS_TO_SET).setValue(MagFreqDistParameterEditor.MAG_AND_MO_RATE);
        magEditor.getParameter(MagFreqDistParameterEditor.MAG).setValue(new Double(6.0));
        magEditor.getParameter(MagFreqDistParameterEditor.MO_RATE).setValue(new Double(1.905e16));
      }

      // mag dist parameters for test case 10
      else if(hazardCurveGuiBean.getSelectedTest().equalsIgnoreCase(HazardCurveApplet.TEST_CASE_TEN)) {
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
      else if(hazardCurveGuiBean.getSelectedTest().equalsIgnoreCase(HazardCurveApplet.TEST_CASE_ELEVEN)) {
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


  }