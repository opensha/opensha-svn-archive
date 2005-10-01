package org.opensha.sha.earthquake.griddedForecast;

import javaDevelopers.matt.calc.*;

import org.opensha.data.region.*;
import org.opensha.sha.earthquake.observedEarthquake.*;
import org.opensha.sha.fault.SimpleFaultData;

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
public class SequenceAfterHypoMagFreqDistForecast
    extends AfterShockHypoMagFreqDistForecast {
  private double seqNodeCompletenessMag;
  private double aVal_Sequence, bVal_Sequence, pVal_Sequence, cVal_Sequence,
      kVal_Sequence;
  private int numGridLocs;
  private double[] grid_Seq_kVal, grid_Seq_aVal, grid_Seq_bVal, grid_Seq_cVal,
      grid_Seq_pVal;
  public MaxLikeOmori_Calc omoriCalc;
  private RegionDefaults rDefs;
  private SimpleFaultData mainshockFault;
  private double[] kScaler;

  public SequenceAfterHypoMagFreqDistForecast(ObsEqkRupture mainshock,
                                              EvenlyGriddedGeographicRegionAPI
                                              aftershockZone,
                                              RegionDefaults rDefs,
                                              ObsEqkRupList aftershocks) {
    this.rDefs = rDefs;
    /**
     * initialise the aftershock zone and mainshock for this model
     */
    this.setMainShock(mainshock);
    region = aftershockZone;
    this.setAfterShocks(aftershocks);

    numGridLocs = aftershockZone.getNumGridLocs();
    grid_Seq_aVal = new double[numGridLocs];
    grid_Seq_bVal = new double[numGridLocs];
    grid_Seq_cVal = new double[numGridLocs];
    grid_Seq_pVal = new double[numGridLocs];
    grid_Seq_kVal = new double[numGridLocs];

    MaxLikeOmori_Calc omoriCalc = new MaxLikeOmori_Calc();

  }

  /**
   * setFaultModel
   */
  public void setFaultModel() {
  }

  /**
     public SequenceAfterHypoMagFreqDistForecast(ObsEqkRupture mainShock,
                                              EvenlyGriddedGeographicRegionAPI
   backGroundRatesGrid, RegionDefaults rDefs) {
    //pass mainShock object to generic model (this will initialise the aftershock zone
    super(mainShock,backGroundRatesGrid,rDefs);

    //EvenlyGriddedGeographicRegionAPI aftershockZone = this.getAfterShockZone();
    // numGridLocs = aftershockZone.getNumGridLocs();
     grid_Seq_aVal = new double[numGridLocs];
     grid_Seq_bVal = new double[numGridLocs];
     grid_Seq_cVal = new double[numGridLocs];
     grid_Seq_pVal = new double[numGridLocs];
     grid_Seq_kVal = new double[numGridLocs];

     MaxLikeOmori_Calc omoriCalc = new MaxLikeOmori_Calc();

     }  */


  /**
   * This sets the aftershock zone
   * @param aftershockZone EvenlyGriddedGeographicRegionAPI
   */
  public void setAfterShockZone(EvenlyGriddedGeographicRegionAPI aftershockZone) {
    this.region = aftershockZone;
  }


  /**
   * set_SequenceRJParms
   */
  public void set_SequenceRJParms() {
    ObsEqkRupList aftershockList = this.getAfterShocks();
    MaxLikeGR_Calc.setMags(aftershockList);
    aVal_Sequence = MaxLikeGR_Calc.get_aValueMaxLike();
    bVal_Sequence = MaxLikeGR_Calc.get_bValueMaxLike();
  }

  /**
   * set_SequenceOmoriParms
   */
  public void set_SequenceOmoriParms() {

    ObsEqkRupList aftershockListComplete =
        this.afterShocks.getObsEqkRupsAboveMag(seqNodeCompletenessMag);
    if (rDefs.useFixed_cValue) {
      omoriCalc.set_AfterShockListFixed_c(aftershockListComplete);
    }
    else {
      omoriCalc.set_AfterShockList(aftershockListComplete);
    }

    pVal_Sequence = omoriCalc.get_p_value();
    cVal_Sequence = omoriCalc.get_c_value();
    kVal_Sequence = omoriCalc.get_k_value();
  }

  /**
   * fillGridWithParms
   */
  public void fillGridWithSeqParms() {
    this.set_Gridded_Seq_aValue();
    this.set_Gridded_Seq_bValue();
    this.set_Gridded_Seq_cValue();
    this.set_Gridded_Seq_kValue();
    this.set_Gridded_Seq_pValue();
  }

  /**
     * set_kScaler
     */
    public void set_kScaler(double[] kScaler) {
      this.kScaler = kScaler;
  }


  /**
   * set_k_value
   * This will taper the  k value.  Each grid node will be assigned
   * a k value based on the distance from the fault.
   */

  public void set_Gridded_Seq_kValue() {
    SmoothKVal_Calc smooth_k = new SmoothKVal_Calc();
    smooth_k.setAftershockModel(this);
    smooth_k.setSeq_kVal(this.kVal_Sequence);
    grid_Seq_kVal = smooth_k.get_SmoothSeq_kVal();
  }

  /**
   * set_Gridded_aValue
   */
  public void set_Gridded_Seq_aValue() {
    java.util.Arrays.fill(grid_Seq_aVal, aVal_Sequence);
  }

  /**
   * set_Gridded_bValue
   */
  public void set_Gridded_Seq_bValue() {
    java.util.Arrays.fill(grid_Seq_bVal, bVal_Sequence);
  }

  /**
   * set_Gridded_pValue
   */
  public void set_Gridded_Seq_pValue() {
    java.util.Arrays.fill(grid_Seq_pVal, pVal_Sequence);
  }

  /**
   * set_Gridded_cValue
   */
  public void set_Gridded_Seq_cValue() {
    java.util.Arrays.fill(grid_Seq_cVal, cVal_Sequence);
  }

  /**
   * for the generic case, the min completeness mag Mc is the
   * same as the min forecast mag.
   */

  public void calc_SeqNodeCompletenessMag() {
    CompletenessMagCalc.setMcBest(this.getAfterShocks());
    seqNodeCompletenessMag = CompletenessMagCalc.getMcBest();
    seqNodeCompletenessMag = seqNodeCompletenessMag + rDefs.addToMc;
  }

  /**
   * set_FaultModel
   */
  public void set_FaultModel(SimpleFaultData mainshockFault) {
     this.mainshockFault = mainshockFault;
  }


  /**
   * get_FaultModel
   */
  public SimpleFaultData get_FaultModel() {
    return mainshockFault;
  }


  /**
   * get_aValSequence
   */
  public double get_aValSequence() {
    return aVal_Sequence;
  }

  /**
   * get_bValSequence
   */
  public double get_bValSequence() {
    return bVal_Sequence;
  }

  /**
   * get_pValSequence
   */
  public double get_pValSequence() {
    return pVal_Sequence;
  }

  /**
   * get_cVal_Sequence
   */
  public double get_cVal_Sequence() {
    return cVal_Sequence;
  }

  /**
   * getSeqNodeCompletenessMag
   */
  public double getSeqNodeCompletenessMag() {
    return seqNodeCompletenessMag;
  }

  public HypoMagFreqDistAtLoc getHypoMagFreqDistAtLoc(int ithLocation) {
    return null;
  }

}
