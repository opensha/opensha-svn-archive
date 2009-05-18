package org.opensha.sha.earthquake.griddedForecast;

import org.opensha.commons.data.Location;
import org.opensha.commons.data.region.EvenlyGriddedGeographicRegionAPI;

import org.opensha.sha.faultSurface.SimpleFaultData;

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
public class STEP_AftershockForecast
    extends AfterShockHypoMagFreqDistForecast {
  

  protected double dayStart, dayEnd;
  protected SimpleFaultData mainshockFault;
  protected double minForecastMag, maxForecastMag, deltaForecastMag;
  protected boolean useFixed_cValue;
  protected double addToMc;
  protected HypoMagFreqDistAtLoc forecastAtloc;
  protected HypoMagFreqDistAtLoc griddedMagFreqDistForecast[];
  
  public STEP_AftershockForecast() {
  }

  /**
   * This sets the aftershock zone
   * @param aftershockZone EvenlyGriddedGeographicRegionAPI
   */
  public void setAfterShockZone(EvenlyGriddedGeographicRegionAPI aftershockZone) {
    this.region = aftershockZone;
  }

  /**
   * setForecastStartDay
   */
  public void setForecastStartDay(double dayStart) {
    this.dayStart = dayStart;
  }

  /**
   * setForecastDayEnd
   */
  public void setForecastDayEnd(double dayEnd) {
    this.dayEnd = dayEnd;
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
   * setMinForecastMag
   */
  public void setMinForecastMag(double minMag) {
    this.minForecastMag = minMag;
  }

  /**
   * setMaxForecastMag
   */
  public void setMaxForecastMag(double maxMag) {
    this.maxForecastMag = maxMag;
  }

  /**
   * setDeltaForecastMag
   */
  public void setDeltaForecastMag(double deltaMag) {
    this.deltaForecastMag = deltaMag;
  }

  /**
   * setUseFixed_cValue
   */
  public void setUseFixed_cValue(boolean useFix_cValue) {
    this.useFixed_cValue = useFix_cValue;
  }

  /**
   * set_addToMcConstant
   */
  public void set_addToMcConstant(double mcConst) {
    this.addToMc = mcConst;
  }
  
  /**
   * I am not sure if this is correct, but no griddedMFDF is ever
   * initialised if this is for the combined forecast, so this allows
   * that to happen.
   * 
   * @param numInd
   */
  //public void initGriddedMagFreqDistForecast(int numInd){
//	  griddedMagFreqDistForecast = new HypoMagFreqDistAtLoc[numInd];
 // }
  
  
  /**
   * setMagFreqDistAtLoc
   * 
   * @param forecastAtLoc
   * @param ithLocation
   */
  public void setGriddedMagFreqDistAtLoc(HypoMagFreqDistAtLoc forecastAtLoc, int ithLocation){
	  griddedMagFreqDistForecast[ithLocation] = forecastAtLoc;
  }
  
  /**
   *  initNumGridInForecast
   *
   */
  public void initNumGridInForecast(){
	  griddedMagFreqDistForecast = new HypoMagFreqDistAtLoc[this.getNumHypoLocs()];
  }
  
  /**
   *  getHypoMagFreqDistAtLoc
   */
  
  public HypoMagFreqDistAtLoc getHypoMagFreqDistAtLoc(int ithLocation) {
	    return griddedMagFreqDistForecast[ithLocation];
  }
  
  /**
   * getHypoMagFreqDistAtLoc(Location loc)
   * @param Location
   * return the dist at a give Location.  Note that there is no error checking
   * if the location does not match one in the list, null is returned.
   */
  public HypoMagFreqDistAtLoc getHypoMagFreqDistAtLoc(Location loc){
	  HypoMagFreqDistAtLoc locDist;
	  int numLocs = this.griddedMagFreqDistForecast.length;
	  int ithLoc = 0;
	  while ((loc.equalsLocation(this.griddedMagFreqDistForecast[ithLoc].getLocation()) == false)
	  	&& (ithLoc < numLocs)) {
		  ithLoc++;
	  }
	  if (ithLoc < numLocs)
		  return locDist = this.getHypoMagFreqDistAtLoc(ithLoc);
	  else
		  return null;
  }

}
