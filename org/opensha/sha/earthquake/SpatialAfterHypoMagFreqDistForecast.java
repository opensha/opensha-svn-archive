package org.opensha.sha.earthquake;

import java.util.*;
import javaDevelopers.matt.calc.MaxLikeOmori_Calc;
import javaDevelopers.matt.calc.ReasenbergJonesGriddedParms_Calc;
import org.opensha.data.region.*;
import org.opensha.sha.earthquake.observedEarthquake.ObsEqkRupList;
import javaDevelopers.matt.calc.CompletenessMagCalc;

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
public class SpatialAfterHypoMagFreqDistForecast extends STEP_AftershockHypoMagForecast{
  private double[] nodeCompletenessMag;
  private int numGridLocs;
  private double[] grid_kValue, grid_aValue, grid_bValue, grid_cValue, grid_pValue;
  public MaxLikeOmori_Calc omoriCalc;
  private ArrayList rjParms;
  private ReasenbergJonesGriddedParms_Calc rjcalc;

  public SpatialAfterHypoMagFreqDistForecast() {
  }

  /**
   * calc_GriddedRJParms
   */
  public void calc_GriddedRJParms() {

    EvenlyGriddedGeographicRegionAPI gridNodes = this.getAfterShockZone();
    ObsEqkRupList aftershockList = this.getAfterShocks();
    if (this.useFixed_cValue){
      rjcalc = new ReasenbergJonesGriddedParms_Calc(gridNodes, aftershockList);
    }
    //else {
      // rjcalc = new ReasenbergJonesGriddedParms_Calc(gridNodes, aftershockList, this.useFixed_cValue);
    //}
    // returns an array list with all parms in it
    rjParms = rjcalc.getAllGriddedVals();

  }

  /**
     * set_Gridded_aValue
     */
    public void set_Gridded_aValue() {
      grid_aValue = (double[])rjParms.get(1);
    }

    /**
     * set_Gridded_bValue
     */
    public void set_Gridded_bValue() {
      grid_bValue = (double[])rjParms.get(2);
    }

    /**
     * set_Gridded_pValue
     */
    public void set_Gridded_pValue() {
      grid_pValue = (double[])rjParms.get(3);
    }

    /**
     * set_Gridded_cValue
     */
    public void set_Gridded_cValue() {
      grid_cValue = (double[])rjParms.get(5);
    }

    /**
     * set_Gridded_kValue
     */
    public void set_Gridded_kValue() {
      grid_kValue = (double[])rjParms.get(4);
    }

    /**
     * This has already been calculated when calculating the
     * RJ parms on a grid, so it does not need to be recalculated
     * here - it is available in the rjParms ArrayList
     */

    public void calc_NodeCompletenessMag() {
      nodeCompletenessMag = (double[])rjParms.get(6);
  }

  /**
   * get_gridded_aValue
   */
  public double[] get_gridded_aValue() {
    return grid_aValue;
  }

  /**
   * get_gridded_bValue
   */
  public double[] get_gridded_bValue() {
    return grid_bValue;
  }

  /**
     * get_gridded_pValue
     */
    public double[] get_gridded_pValue() {
      return grid_pValue;
  }

  /**
   * get_gridded_kValue
   */
  public double[] get_gridded_kValue() {
    return grid_kValue;
  }

  /**
   * get_gridded_cValue
   */
  public double[] get_gridded_cValue() {
    return grid_cValue;
  }


}

