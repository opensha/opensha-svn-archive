package javaDevelopers.matt.calc;

import org.opensha.data.region.*;
import org.opensha.sha.earthquake.griddedForecast.*;
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
public class UpdateSTEP_Forecast {
  private ObsEqkRupList aftershocks;
  private GenericAfterHypoMagFreqDistForecast forecastModelGen;
  private SequenceAfterHypoMagFreqDistForecast forecastModelSeq;
  private SpatialAfterHypoMagFreqDistForecast forecastModelSpa;
  private EvenlyGriddedGeographicRegionAPI backgroundRatesGrid;
  private boolean useSeqAndSpat, gridIsUpdated = false;
  private int numGridNodes;



  public UpdateSTEP_Forecast(GenericAfterHypoMagFreqDistForecast forecastModelGen,
                             EvenlyGriddedGeographicRegionAPI backgroundRatesGrid) {
    this.forecastModelGen = forecastModelGen;
    this.backgroundRatesGrid = backgroundRatesGrid;
    initUpdate();
  }



  /**
   * initUpdate
   */
  private void initUpdate() {
    aftershocks = forecastModelGen.getAfterShocks();

    /**
     * check to see if the aftershock zone needs to be updated
     * if it is, update the generic model
     */
    updateAftershockZone();

    numGridNodes = forecastModelGen.getNumHypoMagFreqDistAtLocs();

    if (gridIsUpdated) {
      updateGenericModelParms();
    }

    if ( forecastModelGen instanceof SequenceAfterHypoMagFreqDistForecast){
      SequenceAfterHypoMagFreqDistForecast forecastModelSeq =
          (SequenceAfterHypoMagFreqDistForecast) forecastModelGen;
      updateSequenceModelParms();
    }

    if ( forecastModelGen instanceof SpatialAfterHypoMagFreqDistForecast){
      SpatialAfterHypoMagFreqDistForecast forecastModelSpa =
          (SpatialAfterHypoMagFreqDistForecast) forecastModelGen;
      updateSpatialModelParms();
    }

  }

  /**
   * updateAftershockZone
   */
  public void updateAftershockZone() {
    int numAftershocks = aftershocks.size();
    boolean hasExternalFault = forecastModelGen.getHasExternalFaultModel();
    if ((numAftershocks >= 100) && (hasExternalFault = false)) {
      forecastModelGen.calcTypeII_AfterShockZone(aftershocks, backgroundRatesGrid);
      gridIsUpdated = true;
    }
  }

  /**
   * updateGenericModelParms
   */
  public void updateGenericModelParms() {
      forecastModelGen.set_Gridded_Gen_bValue();
      forecastModelGen.set_Gridded_Gen_cValue();
      forecastModelGen.set_Gridded_Gen_kValue();
      forecastModelGen.set_Gridded_Gen_pValue();
  }

  /**
   * updateSequenceModelParms
   */
  public void updateSequenceModelParms() {
    forecastModelSeq.calc_SeqNodeCompletenessMag();
    forecastModelSeq.set_SequenceRJParms();
    forecastModelSeq.set_SequenceOmoriParms();
    forecastModelSeq.fillGridWithSeqParms();
  }

  /**
   * updateSpatialModelParms
   */
  public void updateSpatialModelParms() {
    // this will calc a, b, c, p, k and completeness on the grid
    forecastModelSpa.calc_GriddedRJParms();
    forecastModelSpa.setAllGridedRJ_Parms();
  }

  /**
   * setBackGroundGrid
   * if the background needs to be changed - this also recalculates the
   * aftershock zone if necessary.
   */
  public void setBackGroundGrid(EvenlyGriddedGeographicRegionAPI backgroundRatesGrid) {
    this.backgroundRatesGrid = backgroundRatesGrid;
    forecastModelGen.calcTypeI_AftershockZone(backgroundRatesGrid);
    updateAftershockZone();
  }

  /**
   * getBackGroundGrid
   */
  public EvenlyGriddedGeographicRegionAPI getBackGroundGrid() {
    return this.backgroundRatesGrid;
  }

  /**
   * calcGenRateForecast
   */
  public void calcGenRateForecast() {
    double[] rateGrid = new double[numGridNodes];
    double[] rjParms = new double[5];
    double aVal_Gen = forecastModelGen.get_a_valueGeneric();
    double bVal_Gen = forecastModelGen.get_b_valueGeneric();
    rjParms[1] = forecastModelGen.get_c_valueGeneric();
    rjParms[2] = forecastModelGen.get_p_valueGeneric();
    double[] kVal_GenGrid = forecastModelGen.getGridded_k_value_generic();
    double[] timeParms = new double[2];
    //timeParms[0] = forecastModelGen.getForecastStartTime();
    //timeParms[1] = forecastModelGen.getForecastEndTime();

    OmoriRate_Calc omoriRateCalc = new OmoriRate_Calc();
    for (int nodeLoop = 0; nodeLoop < numGridNodes; ++nodeLoop){
      rjParms[0] = kVal_GenGrid[nodeLoop];
      omoriRateCalc.setTimeParms(timeParms);
      omoriRateCalc.set_OmoriParms(rjParms);
      rateGrid[nodeLoop] = omoriRateCalc.get_OmoriRate();
    }
  }

}
