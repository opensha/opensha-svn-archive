package org.opensha.sha.earthquake.griddedForecast;

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
public class SequenceAfterHypoMagFreqDistForecast extends GenericAfterHypoMagFreqDistForecast {
  private double seqNodeCompletenessMag;
  private double aVal_Sequence, bVal_Sequence, pVal_Sequence, cVal_Sequence;
  //int numGridLocs;
  private double[] grid_Seq_kVal, grid_Seq_aVal, grid_Seq_bVal, grid_Seq_cVal, grid_Seq_pVal;
  public MaxLikeOmori_Calc omoriCalc;

  public SequenceAfterHypoMagFreqDistForecast(ObsEqkRupture mainShock) {
    //pass mainShock object to generic model (this will initialise the aftershock zone
    super(mainShock);

    //EvenlyGriddedGeographicRegionAPI aftershockZone = this.getAfterShockZone();
    // numGridLocs = aftershockZone.getNumGridLocs();
     grid_Seq_aVal = new double[numGridLocs];
     grid_Seq_bVal = new double[numGridLocs];
     grid_Seq_cVal = new double[numGridLocs];
     grid_Seq_pVal = new double[numGridLocs];
     grid_Seq_kVal = new double[numGridLocs];

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
    ObsEqkRupList aftershockListComplete =
        aftershockList.getObsEqkRupsAboveMag(seqNodeCompletenessMag);
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

  public void set_Gridded_Seq_kValue() {
    SmoothKVal_Calc smooth_k = new SmoothKVal_Calc();
    smooth_k.setAftershockModel(this);
    grid_Seq_kVal = smooth_k.get_Smooth_kVal();
  }

  /**
   * set_Gridded_aValue
   */
  public void set_Gridded_Seq_aValue() {
    java.util.Arrays.fill(grid_Seq_aVal,aVal_Sequence);
  }

  /**
   * set_Gridded_bValue
   */
  public void set_Gridded_Seq_bValue() {
      java.util.Arrays.fill(grid_Seq_bVal,bVal_Sequence);
  }

  /**
   * set_Gridded_pValue
   */
  public void set_Gridded_Seq_pValue() {
    java.util.Arrays.fill(grid_Seq_pVal,pVal_Sequence);
  }

  /**
   * set_Gridded_cValue
   */
  public void set_Gridded_Seq_cValue() {
    java.util.Arrays.fill(grid_Seq_cVal,cVal_Sequence);
  }

  /**
   * for the generic case, the min completeness mag Mc is the
   * same as the min forecast mag.
   */

  public void calc_SeqNodeCompletenessMag() {
    CompletenessMagCalc.setMcBest(this.getAfterShocks());
    seqNodeCompletenessMag = CompletenessMagCalc.getMcBest();
    seqNodeCompletenessMag = seqNodeCompletenessMag + addToMc;
  }

  /**
   * get_aValSequence
   */
  public double get_aValSequence() {
    aVal_Sequence = MaxLikeGR_Calc.get_aValueMaxLike();
    return aVal_Sequence;
  }

  /**
   * get_bValSequence
   */
  public double get_bValSequence() {
    bVal_Sequence = MaxLikeGR_Calc.get_bValueMaxLike();
    return bVal_Sequence;
  }

  /**
   * get_pValSequence
   */
  public double get_pValSequence() {
    pVal_Sequence = omoriCalc.get_p_value();
    return pVal_Sequence;
  }

  /**
   * get_cVal_Sequence
   */
  public double get_cVal_Sequence() {
    cVal_Sequence = omoriCalc.get_c_value();
    return cVal_Sequence;
  }

  /**
   * getSeqNodeCompletenessMag
   */
  public double getSeqNodeCompletenessMag() {
    return seqNodeCompletenessMag;
  }

}

