package org.opensha.sha.earthquake;

import javaDevelopers.matt.calc.*;

import org.opensha.sha.fault.*;
import org.opensha.data.region.EvenlyGriddedGeographicRegionAPI;


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
public class GenericAfterHypoMagFreqDistForecast
    extends STEP_AftershockHypoMagForecast {

  private double a_valueGeneric = -1.67;
  private double b_valueGeneric = 0.91;
  private double c_valueGeneric = 0.05;
  private double p_valueGeneric = 1.08;
  private double nodeCompletenessMag;
  private SimpleFaultData mainshockFault;
  private double[] grid_kVal, grid_aVal, grid_bVal, grid_cVal, grid_pVal;
  int numGridLocs;


  public GenericAfterHypoMagFreqDistForecast() {
    EvenlyGriddedGeographicRegionAPI aftershockZone = this.getAfterShockZone();
     numGridLocs = aftershockZone.getNumGridLocs();
     grid_aVal = new double[numGridLocs];
     grid_bVal = new double[numGridLocs];
     grid_cVal = new double[numGridLocs];
     grid_pVal = new double[numGridLocs];
     grid_kVal = new double[numGridLocs];
  }

  /**
   * set_GenReasenbergJonesParms
   */
  public void set_GenReasenbergJonesParms(double[] rjParms) {
    a_valueGeneric = rjParms[0];
    b_valueGeneric = rjParms[1];
    c_valueGeneric = rjParms[2];
    p_valueGeneric = rjParms[3];
  }

  /**
  * set_Gridded_kValue
  * This will taper the generic k value.  Each grid node will be assigned
  * a k value based on the distance from the fault.
  */

  public void set_Gridded_kValue() {
    SmoothKVal_Calc smooth_k = new SmoothKVal_Calc();
    smooth_k.setAftershockModel(this);
    grid_kVal = smooth_k.get_Smooth_kVal();
  }

  /**
   * set_Gridded_aValue
   */
  public void set_Gridded_aValue() {
    java.util.Arrays.fill(grid_aVal,a_valueGeneric);
  }

  /**
   * set_Gridded_bValue
   */
  public void set_Gridded_bValue() {
    java.util.Arrays.fill(grid_bVal,b_valueGeneric);
  }

  /**
   * set_Gridded_pValue
   */
  public void set_Gridded_pValue() {
    java.util.Arrays.fill(grid_pVal,p_valueGeneric);
  }

  /**
   * set_Gridded_cValue
   */
  public void set_Gridded_cValue() {
    java.util.Arrays.fill(grid_cVal,c_valueGeneric);
  }

  /**
   * get_a_valueGeneric
   */
  public double get_a_valueGeneric() {
    return a_valueGeneric;
  }

  /**
   * get_b_valueGeneric
   */
  public double get_b_valueGeneric() {
    return b_valueGeneric;
  }

  /**
   * get_c_valueGeneric
   */
  public double get_c_valueGeneric() {
    return c_valueGeneric;
  }

  /**
   * get_p_valueGeneric
   */
  public double get_p_valueGeneric() {
    return p_valueGeneric;
  }

  /**
   * for the generic case, the min completeness mag Mc is the
   * same as the min forecast mag.
   */

  public void calc_NodeCompletenessMag() {
    nodeCompletenessMag = minForecastMag;
  }



}
