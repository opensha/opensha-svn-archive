package org.opensha.sha.gui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Toolkit;

import org.opensha.sha.gui.infoTools.ApplicationVersionInfoWindow;
import org.opensha.sha.gui.infoTools.ExceptionWindow;
import org.opensha.sha.earthquake.ERF_API;
import java.util.ArrayList;
import org.opensha.sha.gui.beans.ERF_GuiBean;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;

import org.opensha.sha.gui.beans.EqkRupSelectorGuiBean;
import org.opensha.sha.calc.SpectrumCalculator;
import org.opensha.sha.calc.remoteCalc.RemoteResponseSpectrumClient;
import org.opensha.util.FileUtils;

/**
 * <p>Title: HazardSpectrumServerModeApplication </p>
 *
 * <p>Description: This class allows the  </p>
 *
 * <p>Copyright: Copyright (c) 2002</p>
 *
 * <p>Company: </p>
 *
 * @author Nitin Gupta
 * @version 1.0
 */
public class HazardSpectrumServerModeApplication
    extends HazardSpectrumLocalModeApplication {

  protected final static String appURL = "http://www.opensha.org/applications/hazSpectrumApp/HazardSpectrumServerModeApp.jar";


  /**
   * Returns the Application version
   * @return String
   */
  
  public static String getAppVersion(){
    return version;
  }

  
  
  /**
   * Checks if the current version of the application is latest else direct the
   * user to the latest version on the website.
   */
  protected void checkAppVersion(){
      ArrayList hazCurveVersion = null;
      try {
    	  hazCurveVersion = FileUtils.loadFile(new URL(versionURL));
      }
      catch (Exception ex1) {
        return;
      }
      String appVersionOnWebsite = (String)hazCurveVersion.get(0);
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
   * Initialize the ERF Gui Bean
   */
  protected void initERF_GuiBean() {

    if (erfGuiBean == null) {
      try {
        // create the ERF Gui Bean object
        ArrayList erf_Classes = new ArrayList();
        //adding the RMI based ERF's to the application
        erf_Classes.add(RMI_FRANKEL_ADJ_FORECAST_CLASS_NAME);
        erf_Classes.add(RMI_WGCEP_UCERF1_ERF_CLASS_NAME);
        erf_Classes.add(RMI_STEP_FORECAST_CLASS_NAME);
        erf_Classes.add(RMI_STEP_ALASKA_ERF_CLASS_NAME);
        erf_Classes.add(RMI_FLOATING_POISSON_FAULT_ERF_CLASS_NAME);
        erf_Classes.add(RMI_FRANKEL02_ADJ_FORECAST_CLASS_NAME);
        erf_Classes.add(RMI_PEER_AREA_FORECAST_CLASS_NAME);
        erf_Classes.add(RMI_PEER_MULTI_SOURCE_FORECAST_CLASS_NAME);
        erf_Classes.add(RMI_PEER_NON_PLANAR_FAULT_FORECAST_CLASS_NAME);
        erf_Classes.add(RMI_POISSON_FAULT_ERF_CLASS_NAME);
        erf_Classes.add(RMI_POINT2MULT_VSS_FORECAST_CLASS_NAME);
        erf_Classes.add(RMI_WG02_ERF_LIST_CLASS_NAME);
        erf_Classes.add(RMI_PEER_LOGIC_TREE_ERF_LIST_CLASS_NAME);
        erf_Classes.add(RMI_POINT2MULT_VSS_ERF_LIST_CLASS_NAME);

        erfGuiBean = new ERF_GuiBean(erf_Classes);
        erfGuiBean.getParameter(erfGuiBean.ERF_PARAM_NAME).
            addParameterChangeListener(this);
      }
      catch (InvocationTargetException e) {
        ExceptionWindow bugWindow = new ExceptionWindow(this, e.getStackTrace(),
            "ERF's Initialization problem. Rest all parameters are default");
        bugWindow.setVisible(true);
        bugWindow.pack();
        //e.printStackTrace();
        //throw new RuntimeException("Connection to ERF's failed");
      }
    }
    else {
      boolean isCustomRupture = erfRupSelectorGuiBean.isCustomRuptureSelected();
      if (!isCustomRupture) {
        ERF_API eqkRupForecast = erfRupSelectorGuiBean.
            getSelectedEqkRupForecastModel();
        erfGuiBean.setERF(eqkRupForecast);
      }
    }
    erfPanel.removeAll();
    erfPanel.add(erfGuiBean, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0,
        GridBagConstraints.CENTER, GridBagConstraints.BOTH, defaultInsets, 0, 0));

    erfPanel.updateUI();

  }


  /**
   * Initialize the ERF Rup Selector Gui Bean
   */
  protected void initERFSelector_GuiBean() {

    ERF_API erf = null;
    try {
      erf = erfGuiBean.getSelectedERF();
    }
    catch (InvocationTargetException ex) {
      ex.printStackTrace();
    }
    if(erfRupSelectorGuiBean == null){
      // create the ERF Gui Bean object
      ArrayList erf_Classes = new ArrayList();

      /**
       *  The object class names for all the supported Eqk Rup Forecasts
       */
      erf_Classes.add(RMI_POISSON_FAULT_ERF_CLASS_NAME);
      erf_Classes.add(RMI_FRANKEL_ADJ_FORECAST_CLASS_NAME);
      erf_Classes.add(RMI_WGCEP_UCERF1_ERF_CLASS_NAME);
      erf_Classes.add(RMI_STEP_FORECAST_CLASS_NAME);
      erf_Classes.add(RMI_STEP_ALASKA_ERF_CLASS_NAME);
      erf_Classes.add(RMI_FLOATING_POISSON_FAULT_ERF_CLASS_NAME);
      erf_Classes.add(RMI_FRANKEL02_ADJ_FORECAST_CLASS_NAME);
      erf_Classes.add(RMI_PEER_AREA_FORECAST_CLASS_NAME);
      erf_Classes.add(RMI_PEER_NON_PLANAR_FAULT_FORECAST_CLASS_NAME);
      erf_Classes.add(RMI_PEER_MULTI_SOURCE_FORECAST_CLASS_NAME);
      erf_Classes.add(RMI_WG02_ERF_CLASS_NAME);

      try {
        erfRupSelectorGuiBean = new EqkRupSelectorGuiBean(erf,erf_Classes);
      }
      catch (InvocationTargetException e) {
        throw new RuntimeException("Connection to ERF's failed");
      }
    }
    erfPanel.removeAll();
    //erfGuiBean = null;
    erfPanel.add(erfRupSelectorGuiBean,
                 new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0,
                                        GridBagConstraints.CENTER,
                                        GridBagConstraints.BOTH, defaultInsets, 0,
                                        0));
    erfPanel.updateUI();
  }

  /**
   * This method creates the SpectrumCalc s.
   * If the internet connection is available then it creates a remote instances of
   * the calculators on server where the calculations take place, else
   * calculations are performed on the user's own machine.
   */
  protected void createCalcInstance() {
    try{
    if (calc == null && isProbCurve)
      calc = (new RemoteResponseSpectrumClient()).getRemoteSpectrumCalc();
    else if(calc == null && !isProbCurve)
      calc = new SpectrumCalculator();
    }catch (Exception ex) {
        ExceptionWindow bugWindow = new ExceptionWindow(this,
            ex.getStackTrace(), this.getParametersInfoAsString());
        bugWindow.setVisible(true);
        bugWindow.pack();
      }
  }

  public static void main(String[] args) {
    HazardSpectrumServerModeApplication applet = new
        HazardSpectrumServerModeApplication();
    applet.checkAppVersion();
    applet.init();
    applet.setVisible(true);
  }
}
