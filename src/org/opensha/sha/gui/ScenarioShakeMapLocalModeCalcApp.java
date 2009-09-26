package org.opensha.sha.gui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Toolkit;

import org.opensha.sha.gui.beans.EqkRupSelectorGuiBean;
import org.opensha.sha.gui.infoTools.ApplicationVersionInfoWindow;

import java.util.ArrayList;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;

import org.opensha.commons.exceptions.RegionConstraintException;
import org.opensha.commons.util.FileUtils;

/**
 * <p>Title: ScenarioShakeMapLocalModeCalcApplication</p>
 *
 * <p>Description: This application allows user to run this application
 * without having to open non standard ports to get the Earthquake Rupture
 * Forecast(ERF). All the ERF's are generated on the user's machine and
 * so all the ScenarioShakemap calculations are done on the user's machine.</p>
 *
 * @author Nitin Gupta
 * @version 1.0
 */
public class ScenarioShakeMapLocalModeCalcApp
    extends ScenarioShakeMapApp {


	protected final static String appURL = "http://www.opensha.org/applications/scenShakeMapApp/ScenarioShakeMapLocalCalcApp.jar";		
		
  /**	
   *  The object class names for all the supported Eqk Rup Forecasts
   */	
  public final static String FRANKEL_ADJ_FORECAST_CLASS_NAME = "org.opensha.sha.earthquake.rupForecastImpl.Frankel96.Frankel96_AdjustableEqkRupForecast";
  public final static String STEP_FORECAST_CLASS_NAME = "org.opensha.sha.earthquake.rupForecastImpl.step.STEP_EqkRupForecast";
  public final static String STEP_ALASKA_ERF_CLASS_NAME = "org.opensha.sha.earthquake.rupForecastImpl.step.STEP_AlaskanPipeForecast";
  public final static String FLOATING_POISSON_FAULT_ERF_CLASS_NAME = "org.opensha.sha.earthquake.rupForecastImpl.FloatingPoissonFaultERF";
  public final static String FRANKEL02_ADJ_FORECAST_CLASS_NAME = "org.opensha.sha.earthquake.rupForecastImpl.Frankel02.Frankel02_AdjustableEqkRupForecast";
  public final static String PEER_AREA_FORECAST_CLASS_NAME = "org.opensha.sha.earthquake.rupForecastImpl.PEER_TestCases.PEER_AreaForecast";
  public final static String PEER_NON_PLANAR_FAULT_FORECAST_CLASS_NAME = "org.opensha.sha.earthquake.rupForecastImpl.PEER_TestCases.PEER_NonPlanarFaultForecast";
  public final static String PEER_MULTI_SOURCE_FORECAST_CLASS_NAME = "org.opensha.sha.earthquake.rupForecastImpl.PEER_TestCases.PEER_MultiSourceForecast";
  public final static String POINT2MULT_VSS_FORECAST_CLASS_NAME = "org.opensha.sha.earthquake.rupForecastImpl.Point2MultVertSS_Fault.Point2MultVertSS_FaultERF";
  public final static String POISSON_FAULT_ERF_CLASS_NAME = "org.opensha.sha.earthquake.rupForecastImpl.PoissonFaultERF";
  public final static String WG02_ERF_CLASS_NAME = "org.opensha.sha.earthquake.rupForecastImpl.WG02.WG02_EqkRupForecast";
  public final static String WGCEP_UCERF1_CLASS_NAME = "org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF1.WGCEP_UCERF1_EqkRupForecast";
  public final static String WGCEP_MEAN_UCERF2_CLASS_NAME = "org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.MeanUCERF2.MeanUCERF2";
  public final static String GEM_TEST_ERF_CLASS_NAME="org.opensha.sha.earthquake.rupForecastImpl.GEM.TestGEM_ERF";

  /**
   * Initialize the ERF Gui Bean
   */
  protected void initERFSelector_GuiBean() {
    // create the ERF Gui Bean object
    ArrayList erf_Classes = new ArrayList();

    /**
     *  The object class names for all the supported Eqk Rup Forecasts
     */
//    erf_Classes.add(GEM_TEST_ERF_CLASS_NAME);
    erf_Classes.add(POISSON_FAULT_ERF_CLASS_NAME);
    erf_Classes.add(WGCEP_MEAN_UCERF2_CLASS_NAME);
    erf_Classes.add(FRANKEL_ADJ_FORECAST_CLASS_NAME);
    //erf_Classes.add(STEP_FORECAST_CLASS_NAME);
    //   erf_Classes.add(STEP_ALASKA_ERF_CLASS_NAME);
    erf_Classes.add(FLOATING_POISSON_FAULT_ERF_CLASS_NAME);
    erf_Classes.add(FRANKEL02_ADJ_FORECAST_CLASS_NAME);
    //   erf_Classes.add(PEER_AREA_FORECAST_CLASS_NAME);
    //   erf_Classes.add(PEER_NON_PLANAR_FAULT_FORECAST_CLASS_NAME);
    //   erf_Classes.add(PEER_MULTI_SOURCE_FORECAST_CLASS_NAME);
    erf_Classes.add(WG02_ERF_CLASS_NAME);
    erf_Classes.add(WGCEP_UCERF1_CLASS_NAME);

    try {
      erfGuiBean = new EqkRupSelectorGuiBean(erf_Classes);
    }
    catch (InvocationTargetException e) {
      throw new RuntimeException("Connection to ERF's failed");
    }
    eqkRupPanel.add(erfGuiBean, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0,
        GridBagConstraints.CENTER, GridBagConstraints.BOTH, defaultInsets, 0, 0));
    calculationFromServer = false;
  }

  /**
   * Checks if the current version of the application is latest else direct the
   * user to the latest version on the website.
   */
  protected void checkAppVersion(){
      ArrayList scenarioShakeVersion = null;
      try {
    	  scenarioShakeVersion = FileUtils.loadFile(new URL(versionURL));
      }
      catch (Exception ex1) {
        return;
      }
      String appVersionOnWebsite = (String)scenarioShakeVersion.get(0);
      if(!appVersionOnWebsite.trim().equals(version.trim())){
        try{
          ApplicationVersionInfoWindow messageWindow =
              new ApplicationVersionInfoWindow(appURL,
                                               this.versionUpdateInfoURL,
                                               "App Version Update", this);
          Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
          messageWindow.setLocation( (dim.width -
                                      messageWindow.getSize().width) / 2,
                                    (dim.height -
                                     messageWindow.getSize().height) / 2);
          messageWindow.setVisible(true);
        }catch(Exception e){
          e.printStackTrace();
        }
      }

    return;

  }

  /**
   * Returns the Application version
   * @return String
   */
  public static String getAppVersion(){
    return version;
  }

  
  
  //Main method
  public static void main(String[] args) {
    ScenarioShakeMapLocalModeCalcApp applet = new ScenarioShakeMapLocalModeCalcApp();
    applet.checkAppVersion();
    applet.init();
    applet.setVisible(true);
  }

  /**
   * Initialize the items to be added to the control list
   */
  protected void initControlList() {
    this.controlComboBox.addItem(CONTROL_PANELS);
    this.controlComboBox.addItem(REGIONS_OF_INTEREST_CONTROL);
    this.controlComboBox.addItem(HAZUS_CONTROL);
    //this.controlComboBox.addItem(PUENTE_HILLS_TEST_CONTROL);
    //this.controlComboBox.addItem(PUENTE_HILLS_CONTROL_OLD);
    this.controlComboBox.addItem(PUENTE_HILLS_CONTROL);
    this.controlComboBox.addItem(SAN_ANDREAS_CONTROL);
    this.controlComboBox.addItem(IM_EVENT_SET_SCENARIO_CONTROL);
    //this.controlComboBox.addItem(SF_BAY_CONTROL);
    this.controlComboBox.addItem(CALC_PARAMS_CONTROL);
    //this.controlComboBox.addItem(RUN_ALL_CASES_FOR_PUENTE_HILLS);
  }

  /**
   * This function sets the Gridded region Sites and the type of plot user wants to see
   * IML@Prob or Prob@IML and it value.
   * This function also gets the selected AttenuationRelationships in a ArrayList and their
   * corresponding relative wts.
   * This function also gets the mode of map calculation ( on server or on local machine)
   */
  public void getGriddedSitesMapTypeAndSelectedAttenRels() throws
      RegionConstraintException, RuntimeException {
    //gets the IML or Prob selected value
    getIMLorProb();

    //get the site values for each site in the gridded region
    getGriddedRegionSites();

    //selected IMRs Wts
    attenRelWts = imrGuiBean.getSelectedIMR_Weights();
    //selected IMR's
    attenRel = imrGuiBean.getSelectedIMRs();
  }


}
