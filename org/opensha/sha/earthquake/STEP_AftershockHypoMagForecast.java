package org.opensha.sha.earthquake;

import org.opensha.sha.fault.*;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2002</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public abstract class STEP_AftershockHypoMagForecast extends AfterShockHypoMagFreqDistForecast {
  public double minForecastMag = 4.0;
  private double maxForecastMag = 8.0;
  private double deltaMag = 0.1;
  private int numHypoLocation;
  //private AfterShockHypoMagFreqDistForecast STEP_Model;
  private double[] grid_aVal, grid_bVal, grid_cVal, grid_pVal, grid_kVal;
  private double[] node_CompletenessMag;
  private SimpleFaultData mainshockFault;
  public boolean useFixed_cValue = true;
  public double addToMc = .2;


  /**
  * calc_NodeCompletenessMag
  * calculate the completeness at each node
  */
 public abstract void calc_NodeCompletenessMag();

 /**
  * set_minForecastMag
  * the minimum forecast magnitude
  */
 public void set_minForecastMag(double min_forecastMag) {
 minForecastMag = min_forecastMag;
 }

 /**
  * set_maxForecastMag
  * the maximum forecast magnitude
  */
 public void set_maxForecastMag(double max_forecastMag) {
 maxForecastMag = max_forecastMag;
 }

  /**
   * set_deltaMag
   * the magnitude step for the binning of the forecasted magnitude
   */
  public void set_deltaMag(double delta_mag) {
  deltaMag = delta_mag;
  }

  /**
   * setUseFixed_cVal
   * if true c will be fixed for the Omori calculations
   * default is fixed
   */
  public void setUseFixed_cVal(boolean fix_cVal) {
    useFixed_cValue = fix_cVal;
  }

  /**
   * set_Gridded_aValue
   */
  public abstract void set_Gridded_aValue();

  /**
    * set_Gridded_bValue
    */
   public abstract void set_Gridded_bValue();


   /**
  * set_Gridded_pValue
  */
 public abstract void set_Gridded_pValue();

 /**
   * set_Gridded_cValue
   */
  public abstract void set_Gridded_cValue();

  /**
   * set_addToMcConstant
   */
  public void set_addToMcConstant(double mcConst) {
    addToMc = mcConst;
  }

  /**
  * get_minForecastMag
  */
 public double get_minForecastMag() {
   return minForecastMag;
 }

 /**
  * get_maxForecastMag
  */
 public double get_maxForecastMag() {
   return maxForecastMag;
 }

  /**
   * get_deltaMag
   */
  public double get_deltaMag() {
    return deltaMag;
  }

  /**
  * set_completenessMag
  */
 public void set_completenessMag() {
   calc_NodeCompletenessMag();
 }


  /**
   * Set the fault surface that will be used do define a Type II
   * aftershock zone.
   * This will not be used in a spatially varying model.
   */

  public void set_FaultSurface(){
    String faultName = "";
    FaultTrace fault_trace = new FaultTrace(faultName);
    mainshockFault = new SimpleFaultData();
    mainshockFault.setAveDip(90.0);

    //STILL NEED TO SET THE DIMENSIONS OF THE FAULT TRACE.
    mainshockFault.setFaultTrace(fault_trace);
  }

  /**
   * get_Gridded_aVal
   */
  public double[] get_Gridded_aVal() {
    return grid_aVal;
  }

  /**
   * get_Gridded_bVal
   */
  public double[] get_Gridded_bVal() {
    return grid_bVal;
  }

  /**
   * get_Gridded_cVal
   */
  public double[] get_Gridded_cVal() {
    return grid_cVal;
  }

  /**
   * get_Gridded_pVal
   */
  public double[] get_Gridded_pVal() {
    return grid_pVal;
  }


  /**
  * get_Gridded_kVal
  */
  public double[] get_Gridded_kVal() {
    return grid_kVal;
  }

  /**
   * get_nodeCompletenessMag
   */
  public double[] get_nodeCompletenessMag() {
    return node_CompletenessMag;
  }

  /**
   * get_FaultModel
   */
  public SimpleFaultData get_FaultModel() {
    return mainshockFault;
  }

  /**
   * get_addToMcConst
   */
  public double get_addToMcConst() {
    return addToMc;
  }

}
