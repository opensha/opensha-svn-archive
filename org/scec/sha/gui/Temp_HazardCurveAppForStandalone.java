package org.scec.sha.gui;


import java.util.*;
import java.lang.reflect.InvocationTargetException;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.JFrame;
import javax.swing.border.Border;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;

import org.scec.sha.gui.beans.ERF_GuiBean;
import org.scec.sha.calc.HazardCurveCalculator;
import org.scec.sha.calc.DisaggregationCalculator;

/**
 * <p>Title: Temp_HazardCurveAppForStandalone</p>
 * <p>Description: This application is extension of HazardCurveApplication, where
 * everything take place on the user's own machine. This version of application
 * does not require any internet connection, the only difference between this
 * application and its parent class that it uses user's system memory for doing
 * any computation. Whereas , in the HazardCurve application all calculations
 * take place on the users machine.</p>
 * @author : Nitin Gupta and Vipin Gupta
 * @version 1.0
 */

public class Temp_HazardCurveAppForStandalone extends Temp_HazardCurveApplication {

  public final static String PEER_AREA_FORECAST_CLASS_NAME = "org.scec.sha.earthquake.rupForecastImpl.PEER_TestCases.PEER_AreaForecast";
  public final static String PEER_NON_PLANAR_FAULT_FORECAST_CLASS_NAME = "org.scec.sha.earthquake.rupForecastImpl.PEER_TestCases.PEER_NonPlanarFaultForecast";
  public final static String PEER_MULTI_SOURCE_FORECAST_CLASS_NAME = "org.scec.sha.earthquake.rupForecastImpl.PEER_TestCases.PEER_MultiSourceForecast";
  public final static String PEER_LOGIC_TREE_FORECAST_CLASS_NAME = "org.scec.sha.earthquake.rupForecastImpl.PEER_TestCases.PEER_LogicTreeERF_List";
  public final static String FRANKEL_FORECAST_CLASS_NAME = "org.scec.sha.earthquake.rupForecastImpl.Frankel96.Frankel96_EqkRupForecast";
  public final static String FRANKEL_ADJ_FORECAST_CLASS_NAME = "org.scec.sha.earthquake.rupForecastImpl.Frankel96.Frankel96_AdjustableEqkRupForecast";
  public final static String STEP_FORECAST_CLASS_NAME = "org.scec.sha.earthquake.rupForecastImpl.step.STEP_EqkRupForecast";
  public final static String WG02_ERF_LIST_CLASS_NAME = "org.scec.sha.earthquake.rupForecastImpl.WG02.WG02_ERF_Epistemic_List";
  public final static String STEP_ALASKA_ERF_CLASS_NAME = "org.scec.sha.earthquake.rupForecastImpl.step.STEP_AlaskanPipeForecast";
  public final static String POISSON_FAULT_ERF_CLASS_NAME = "org.scec.sha.earthquake.rupForecastImpl.FloatingPoissonFaultERF";
  public final static String SIMPLE_FAULT_ERF_CLASS_NAME = "org.scec.sha.earthquake.rupForecastImpl.PoissonFaultERF";
  public final static String FRANKEL02_ADJ_FORECAST_CLASS_NAME = "org.scec.sha.earthquake.rupForecastImpl.Frankel02.Frankel02_AdjustableEqkRupForecast";
  public final static String POINT_SRC_FORECAST_CLASS_NAME="org.scec.sha.earthquake.rupForecastImpl.PointSourceERF";
  public final static String POINT2MULT_VSS_FORECAST_CLASS_NAME="org.scec.sha.earthquake.rupForecastImpl.Point2MultVertSS_Fault.Point2MultVertSS_FaultERF";
  public final static String POINT2MULT_VSS_ERF_LIST_CLASS_NAME="org.scec.sha.earthquake.rupForecastImpl.Point2MultVertSS_Fault.Point2MultVertSS_FaultERF_List";


  /**
   * Initialize the ERF Gui Bean
   */
  protected void initERF_GuiBean() {
    // create the ERF Gui Bean object
    ArrayList erf_Classes = new ArrayList();

    //adding the client based ERF's to the application
    erf_Classes.add(PEER_AREA_FORECAST_CLASS_NAME);
    erf_Classes.add(PEER_NON_PLANAR_FAULT_FORECAST_CLASS_NAME);
    erf_Classes.add(PEER_MULTI_SOURCE_FORECAST_CLASS_NAME);
    erf_Classes.add(PEER_LOGIC_TREE_FORECAST_CLASS_NAME);
    erf_Classes.add(FRANKEL_FORECAST_CLASS_NAME);
    erf_Classes.add(FRANKEL_ADJ_FORECAST_CLASS_NAME);
    erf_Classes.add(STEP_FORECAST_CLASS_NAME);
    erf_Classes.add(STEP_ALASKA_ERF_CLASS_NAME);
    erf_Classes.add(POISSON_FAULT_ERF_CLASS_NAME);
    erf_Classes.add(SIMPLE_FAULT_ERF_CLASS_NAME);
    erf_Classes.add(FRANKEL02_ADJ_FORECAST_CLASS_NAME);
    erf_Classes.add(WG02_ERF_LIST_CLASS_NAME);
    erf_Classes.add(POINT_SRC_FORECAST_CLASS_NAME);
    erf_Classes.add(POINT2MULT_VSS_FORECAST_CLASS_NAME);
    erf_Classes.add(POINT2MULT_VSS_ERF_LIST_CLASS_NAME);
    try{
      erfGuiBean = new ERF_GuiBean(erf_Classes);
    }catch(InvocationTargetException e){
      e.printStackTrace();
      throw new RuntimeException("Connection to ERF's failed");
    }
    erfPanel.setLayout(gridBagLayout5);
    erfPanel.add(erfGuiBean, new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0,
        GridBagConstraints.CENTER,GridBagConstraints.BOTH, defaultInsets, 0, 0 ));
    erfGuiBean.getParameterEditor(erfGuiBean.ERF_PARAM_NAME).getParameter().addParameterChangeListener(this);

  }


  /**
   * This method creates the HazardCurveCalc and Disaggregation Calc(if selected) instances.
   * Calculations are performed on the user's own machine, no internet connection
   * is required for it.
   */
  protected void createCalcInstance(){
    try{
      calc = new HazardCurveCalculator();
      if(disaggregationFlag)
        disaggCalc = new DisaggregationCalculator();
    }catch(Exception e){
      e.printStackTrace();
    }
  }

  public static void main(String[] args) {
    Temp_HazardCurveAppForStandalone applet = new Temp_HazardCurveAppForStandalone();

    applet.isStandalone = true;
    JFrame frame = new JFrame();
    //EXIT_ON_CLOSE == 3
    frame.setDefaultCloseOperation(3);
    frame.setTitle("Hazard Curve Calculator");
    frame.getContentPane().add(applet, BorderLayout.CENTER);
    applet.init();
    applet.start();
    frame.setSize(W,H);
    Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
    frame.setLocation((d.width - frame.getSize().width) / 2, (d.height - frame.getSize().height) / 2);
    frame.setVisible(true);
  }
}