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
  private STEP_CombineForecastModels combinedModel;
  private static boolean useSeqAndSpat, gridIsUpdated = false;
  private static int numGridNodes;
  private HypoMagFreqDistAtLoc genForecastAtLoc,seqForecastAtLoc,spaForecastAtLoc;
  private GriddedHypoMagFreqDistForecast genGriddedForecast;


  public UpdateSTEP_Forecast(STEP_CombineForecastModels combinedModel) {
    this.combinedModel = combinedModel;
    initUpdate();
  }



  /**
   * initUpdate
   */
  private void initUpdate() {
	  forecastModelGen = combinedModel.getGenElement();
	  aftershocks = forecastModelGen.getAfterShocks();
	  
	  /**
	   * check to see if the aftershock zone needs to be updated
	   * if it is, update the generic model
	   */
	  updateAftershockZone();
	  
	  //numGridNodes = forecastModelGen.getNumHypoLocs();
	  
	  if (gridIsUpdated) {
		  updateGenericModelParms();
	  }
	  
	  if ( combinedModel.get_useSeqAndSpatial()){
		  forecastModelSeq = combinedModel.getSeqElement();
		  updateSequenceModelParms();
		  forecastModelSpa = combinedModel.getSpaElement();
		  updateSpatialModelParms();
	  }
	  
	  combinedModel.calcTimeSpan();  //IS THIS THE BEST PLACE FOR THIS?
	  
	  // now calculate the forecasts for each of the elements
	  this.updateGenericModelForecast();
	  if ( combinedModel.get_useSeqAndSpatial()){
		  this.updateSequenceModelForecast();
		  this.updateSpatialModelForecast();
	  }
	  
  }
  
  /**
   * updateAftershockZone
   */
  public void updateAftershockZone() {
	  int numAftershocks = aftershocks.size();
	  
	  boolean hasExternalFault = combinedModel.getHasExternalFaultModel();
	  
	  if ((numAftershocks >= 100) && (hasExternalFault = false)) {
		  combinedModel.calcTypeII_AfterShockZone(aftershocks, backgroundRatesGrid);
		  gridIsUpdated = true;
	  } 
  }
  
  /**
   * updateGenericModelParms
   * 
   * Redistribute the generic values on the aftershock zone grid
   * (which has probably been updated)
   */
  public void updateGenericModelParms() {
	  forecastModelGen.setNumGridLocs();
	  double[] kScaler = DistDecayFromRupCalc.getDensity(combinedModel.getMainShock(),combinedModel.getAfterShockZone());
	  forecastModelGen.set_kScaler(kScaler);
	  forecastModelGen.set_Gridded_Gen_bValue();
	  forecastModelGen.set_Gridded_Gen_cValue();
	  forecastModelGen.set_Gridded_Gen_kValue();
	  forecastModelGen.set_Gridded_Gen_pValue();
  }
  
  /**
   * updateSequenceModelParms
   */
  public void updateSequenceModelParms() {
	  double[] kScaler = DistDecayFromRupCalc.getDensity(combinedModel.getMainShock(),combinedModel.getAfterShockZone());
	  forecastModelSeq.set_kScaler(kScaler);
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
 *  updateGenericModelForecast
 */
public void updateGenericModelForecast() {
	//first initialise the array that will contain the forecast
	forecastModelGen.initNumGridInForecast();
	int numGridLocs = combinedModel.getAfterShockZone().getNumGridLocs();
	int gLoop = 0;
	while ( gLoop < numGridLocs) {
		 genForecastAtLoc = forecastModelGen.calcHypoMagFreqDist(gLoop);
		 forecastModelGen.setGriddedMagFreqDistAtLoc(genForecastAtLoc, gLoop++);
	}
	  
  }

/**
 *  updateSequenceModelForecast
 */
public void updateSequenceModelForecast() {
	//first initialise the array that will contain the forecast
	forecastModelSeq.initNumGridInForecast();
	int numGridLocs = combinedModel.getAfterShockZone().getNumGridLocs();
	int gLoop = 0;
	while ( gLoop < numGridLocs) {
		 seqForecastAtLoc = forecastModelSeq. calcHypoMagFreqDistAtLoc(gLoop);
		 forecastModelSeq.setGriddedMagFreqDistAtLoc(seqForecastAtLoc, gLoop++);
	}
	  
  }
  

/**
 *  updateSpatialModelForecast
 */
public void updateSpatialModelForecast() {
	//first initialise the array that will contain the forecast
	forecastModelSpa.initNumGridInForecast();
	int numGridLocs = combinedModel.getAfterShockZone().getNumGridLocs();
	int gLoop = 0;
	while ( gLoop < numGridLocs) {
		 spaForecastAtLoc = forecastModelSpa.calcHypoMagFreqDistAtLoc(gLoop);
		 forecastModelSpa.setGriddedMagFreqDistAtLoc(spaForecastAtLoc, gLoop++);
	}
	  
  }
  /**
   * setBackGroundGrid
   * if the background needs to be changed - this also recalculates the
   * aftershock zone if necessary.
   */
  public void setBackGroundGrid(EvenlyGriddedGeographicRegionAPI backgroundRatesGrid) {
	  this.backgroundRatesGrid = backgroundRatesGrid;
	  // forecastModelGen.calcTypeI_AftershockZone(backgroundRatesGrid);
	  updateAftershockZone();
  }
  
  /**
   * getBackGroundGrid
   */
  public EvenlyGriddedGeographicRegionAPI getBackGroundGrid() {
	  return this.backgroundRatesGrid;
  }
  
  
  
}
