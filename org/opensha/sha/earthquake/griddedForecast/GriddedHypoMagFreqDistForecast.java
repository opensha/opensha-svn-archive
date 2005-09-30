package org.opensha.sha.earthquake.griddedForecast;

import org.opensha.data.*;
import org.opensha.param.ParameterList;
import org.opensha.param.event.ParameterChangeEvent;
import org.opensha.param.event.ParameterChangeListener;
import org.opensha.data.region.EvenlyGriddedGeographicRegionAPI;
import java.util.ListIterator;
import org.opensha.sha.earthquake.ERF_API;
import org.opensha.data.region.GeographicRegion;

/**
 * <p>Title: GriddedHypoMagFreqForecast</p>
 *
 * <p>Description: This constitutes a Poissonian hypocenter forecast.
 *
 * Are locations unique?.
 *  </p>
 *<p>
 * Note : The rate provided by this forecast are always yearly rate.
 *</p>
 * @author Nitin Gupta , Edward (Ned) Field, Vipin Gupta
 * @version 1.0
 */
public abstract  class GriddedHypoMagFreqDistForecast
    implements ERF_API, HypoMagFreqDistAtMultLocsAPI, ParameterChangeListener {



  // Timespan for the given forecast
  private TimeSpan timeSpan;
  //number of hypocenter location.
  private int numHypoLocation;
  //Adjustable parameters fro the given forecast model
  private ParameterList adjustableParameters;
  //Only update the forecast if parameters have been changed.
  protected boolean parameterChangeFlag = true;


  //EvenlyGriddedGeographicAPI region
  protected EvenlyGriddedGeographicRegionAPI region;



  /**
   * This function finds whether a particular location lies in applicable
   * region of the forecast
   *
   * @param loc : location
   * @return: True if this location is within forecast's applicable region, else false
   */
  public boolean isLocWithinApplicableRegion(Location loc){
    return region.isLocationInside(loc);
  }


  /**
   * Gets the EvenlyGriddedGeographic Region
   * @return EvenlyGriddedGeographicRegionAPI
   */
  public EvenlyGriddedGeographicRegionAPI getEvenlyGriddedGeographicRegion(){
    return region;
  }



  /**
   * Get the region for which this forecast is applicable
   * @return : Geographic region object specifying the applicable region of forecast
   */
  public GeographicRegion getApplicableRegion() {
    return (GeographicRegion)region;
  }

  /**
   * Returns the adjustable parameters list
   * @return ParameterList
   */
  public ParameterList getAdjustableParameterList(){
    return adjustableParameters;
  }

  /**
   * Returns the adjustable parameters as the ListIterator
   * @return ListIterator
   */
  public ListIterator getAdjustableParamsIterator(){
    return adjustableParameters.getParametersIterator();
  }

  /**
   * Return the name for this class
   *
   * @return : return the name for this class
   */
  public String getName() {
    return null;
  }

  /**
   * Update and save the serialized forecast into the file
   */
  public String updateAndSaveForecast(){
    throw new UnsupportedOperationException(
        "updateAndSaveForecast() not supported");
  }

  /**
   * getNumHypoLocation
   *
   * @return int
   * @todo Implement this
   *   org.opensha.sha.earthquake.HypoMagFreqDistAtLocAPI method
   */
  public int getNumHypoLocs() {
    return region.getNumGridLocs();
  }


  /**
   *  This is the main function of this interface. Any time a control
   *  paramater or independent paramater is changed by the user in a GUI this
   *  function is called, and a paramater change event is passed in.
   *
   *  This sets the flag to indicate that the sources need to be updated
   *
   * @param  event
   */
  public void parameterChange(ParameterChangeEvent event) {
      parameterChangeFlag = true;
  }

  /**
   * Allows the user to get the Timespan for this GriddedHypoMagFreqDistForecast
   * @return TimeSpan
   */
  public TimeSpan getTimeSpan() {
    return timeSpan;
  }

  /**
   * Allows the user to set the Timespan for this GriddedHypoMagFreqDistForecast
   * @param timeSpan TimeSpan
   */
  public void setTimeSpan(TimeSpan timeSpan) {
     this.timeSpan = timeSpan;
  }

  /**
   * If any parameter has been changed then update the forecast.
   */
  public void updateForecast(){
    if(parameterChangeFlag){

    }
  }
}
