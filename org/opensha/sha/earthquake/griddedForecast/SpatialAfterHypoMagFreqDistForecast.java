package org.opensha.sha.earthquake.griddedForecast;

import java.util.*;
import javaDevelopers.matt.calc.*;

import org.opensha.data.region.*;
import org.opensha.sha.earthquake.observedEarthquake.*;

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
public class SpatialAfterHypoMagFreqDistForecast extends SequenceAfterHypoMagFreqDistForecast{
  private double[] spaNodeCompletenessMag;
  private int numGridLocs;
  private double[] grid_Spa_kValue, grid_Spa_aValue, grid_Spa_bValue, grid_Spa_cValue, grid_Spa_pValue;
  public MaxLikeOmori_Calc omoriCalc;
  private ArrayList rjParms;
  private ReasenbergJonesGriddedParms_Calc rjcalc;

  public SpatialAfterHypoMagFreqDistForecast(ObsEqkRupture mainShock,
                                             EvenlyGriddedGeographicRegionAPI
                                              backGroundRatesGrid, RegionDefaults rDefs) {
    super(mainShock,backGroundRatesGrid,rDefs);
  }

  /**
   * calc_GriddedRJParms
   * this calculates a, b, c, p, k and completeness magnitude for all grid nodes.
   */
  public void calc_GriddedRJParms() {

    EvenlyGriddedGeographicRegionAPI gridNodes = this.getAfterShockZone();
    ObsEqkRupList aftershockList = this.getAfterShocks();
    if (this.useFixed_cValue){
      rjcalc = new ReasenbergJonesGriddedParms_Calc(gridNodes, aftershockList);
    }
    else {
       rjcalc = new ReasenbergJonesGriddedParms_Calc(gridNodes, aftershockList, this.useFixed_cValue);
    }
    // returns an array list with all parms in it
    rjParms = rjcalc.getAllGriddedVals();

  }

  /**
   * setAllGridedRJ_Parms
   */
  public void setAllGridedRJ_Parms() {
    grid_Spa_aValue = (double[])rjParms.get(1);
    grid_Spa_bValue = (double[])rjParms.get(2);
    grid_Spa_pValue = (double[])rjParms.get(3);
    grid_Spa_cValue = (double[])rjParms.get(5);
    grid_Spa_kValue = (double[])rjParms.get(4);
    spaNodeCompletenessMag = (double[])rjParms.get(6);
  }

  /**
     * set_Gridded_aValue
     */
    public void set_Gridded_Spa_aValue() {
      grid_Spa_aValue = (double[])rjParms.get(1);
    }

    /**
     * set_Gridded_bValue
     */
    public void set_Gridded_Spa_bValue() {
      grid_Spa_bValue = (double[])rjParms.get(2);
    }

    /**
     * set_Gridded_pValue
     */
    public void set_Gridded_Spa_pValue() {
      grid_Spa_pValue = (double[])rjParms.get(3);
    }

    /**
     * set_Gridded_cValue
     */
    public void set_Gridded_Spa_cValue() {
      grid_Spa_cValue = (double[])rjParms.get(5);
    }

    /**
     * set_Gridded_kValue
     */
    public void set_Gridded_Spa_kValue() {
      grid_Spa_kValue = (double[])rjParms.get(4);
    }

    /**
     * This has already been calculated when calculating the
     * RJ parms on a grid, so it does not need to be recalculated
     * here - it is available in the rjParms ArrayList
     */

    public void calc_spaNodeCompletenessMag() {
      spaNodeCompletenessMag = (double[])rjParms.get(6);
  }






  /**
   * get_gridded_aValue
   */
  public double[] get_gridded_Spa_aValue() {
    return grid_Spa_aValue;
  }

  /**
   * get_gridded_bValue
   */
  public double[] get_gridded_Spa_bValue() {
    return grid_Spa_bValue;
  }

  /**
     * get_gridded_pValue
     */
    public double[] get_gridded_Spa_pValue() {
      return grid_Spa_pValue;
  }

  /**
   * get_gridded_kValue
   */
  public double[] get_gridded_Spa_kValue() {
    return grid_Spa_kValue;
  }

  /**
   * get_gridded_cValue
   */
  public double[] get_gridded_Spa_cValue() {
    return grid_Spa_cValue;
  }

  /**
   * getGriddedCompletenessMag
   */
  public double[] getGriddedCompletenessMag() {
    return spaNodeCompletenessMag;
  }

}

