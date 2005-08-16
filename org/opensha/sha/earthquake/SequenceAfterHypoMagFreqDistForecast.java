package org.opensha.sha.earthquake;

import javaDevelopers.matt.calc.*;
import java.util.*;
import org.opensha.sha.earthquake.observedEarthquake.*;
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
public class SequenceAfterHypoMagFreqDistForecast extends STEP_AftershockHypoMagForecast {
  private double nodeCompletenessMag;
  private double aVal_Sequence, bVal_Sequence, pVal_Sequence, cVal_Sequence;
  int numGridLocs;
  private double[] grid_kVal, grid_aVal, grid_bVal, grid_cVal, grid_pVal;
  public MaxLikeOmori_Calc omoriCalc;

  public SequenceAfterHypoMagFreqDistForecast() {
    EvenlyGriddedGeographicRegionAPI aftershockZone = this.getAfterShockZone();
     numGridLocs = aftershockZone.getNumGridLocs();
     grid_aVal = new double[numGridLocs];
     grid_bVal = new double[numGridLocs];
     grid_cVal = new double[numGridLocs];
     grid_pVal = new double[numGridLocs];
     grid_kVal = new double[numGridLocs];

     MaxLikeOmori_Calc omoriCalc = new MaxLikeOmori_Calc();
  }

  /**
   * set_SequenceRJParms
   */
  public void set_SequenceRJParms() {
    ObsEqkRupList aftershockList = this.getAfterShocks();
    MaxLikeGR_Calc.setMags(aftershockList);
  }

  /**
   * set_SequenceOmoriParms
   */
  public void set_SequenceOmoriParms() {
    ObsEqkRupList aftershockList = new ObsEqkRupList();
    aftershockList = this.getAfterShocks();
    ObsEqkRupList aftershockListComplete = aftershockList.getObsEqkRupsAboveMag(nodeCompletenessMag);
    if (useFixed_cValue)
      omoriCalc.set_AfterShockListFixed_c(aftershockListComplete);
    else
      omoriCalc.set_AfterShockList(aftershockListComplete);
  }

  /**
  * set_k_value
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
    java.util.Arrays.fill(grid_aVal,aVal_Sequence);
  }

  /**
   * set_Gridded_bValue
   */
  public void set_Gridded_bValue() {
      java.util.Arrays.fill(grid_bVal,bVal_Sequence);
  }

  /**
   * set_Gridded_pValue
   */
  public void set_Gridded_pValue() {
    java.util.Arrays.fill(grid_pVal,pVal_Sequence);
  }

  /**
   * set_Gridded_cValue
   */
  public void set_Gridded_cValue() {
    java.util.Arrays.fill(grid_cVal,cVal_Sequence);
  }

  /**
   * for the generic case, the min completeness mag Mc is the
   * same as the min forecast mag.
   */

  public void calc_NodeCompletenessMag() {
    CompletenessMagCalc.setMcBest(this.getAfterShocks());
    nodeCompletenessMag = CompletenessMagCalc.getMcBest();
    nodeCompletenessMag = nodeCompletenessMag + addToMc;
  }

  /**
   * get_aValSequence
   */
  public void get_aValSequence() {
    aVal_Sequence = MaxLikeGR_Calc.get_aValueMaxLike();
  }

  /**
   * get_bVal_Sequence
   */
  public void get_bVal_Sequence() {
    bVal_Sequence = MaxLikeGR_Calc.get_bValueMaxLike();
  }

  /**
   * get_pValSequence
   */
  public void get_pValSequence() {
    pVal_Sequence = omoriCalc.get_p_value();
  }

  /**
   * get_cVal_Sequence
   */
  public void get_cVal_Sequence() {
    cVal_Sequence = omoriCalc.get_c_value();
  }



}

