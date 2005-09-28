package org.opensha.sha.earthquake.griddedForecast;


import org.opensha.sha.earthquake.EqkRupForecastAPI;
/**
 * <p>Title: GriddedHypoMagFreqDistForecastWrappedERF</p>
 *
 * <p>Description: This class wraps any Earthquake Rupture Forecast into a
 * GriddedHypoMagFreqDistForecast.</p>
 *
 * @author Nitin Gupta
 * @since Sept 16, 2005
 * @version 1.0
 */
public class GriddedHypoMagFreqDistForecastWrappedERF
    extends GriddedHypoMagFreqDistForecast {

  //ERF Object
  private EqkRupForecastAPI eqkRupForecast;

  /**
   * Class constructor that accepts the EqkRupForecast as the argument.
   * @param eqkRupforecast EqkRupForecastAPI
   */
  public GriddedHypoMagFreqDistForecastWrappedERF(EqkRupForecastAPI eqkRupForecast) {
    this.eqkRupForecast = eqkRupForecast;
  }

  /**
   * If any parameter has been changed then update the forecast.
   */
  public void updateForecast() {
    if (parameterChangeFlag) {
      eqkRupForecast.updateForecast();
    }
  }


}
