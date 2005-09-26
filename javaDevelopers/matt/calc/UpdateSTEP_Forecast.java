package javaDevelopers.matt.calc;

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
  private STEP_AftershockForecast forecastModel;



  public UpdateSTEP_Forecast(STEP_AftershockForecast forecastModel) {
    this.forecastModel = forecastModel;
    aftershocks = forecastModel.getAfterShocks();
    updateAftershockZone();
    updateGenericModel();
  }

  /**
   * updateAftershockZone
   */
  public void updateAftershockZone() {
    int numAftershocks = aftershocks.size();
    boolean hasExternalFault = forecastModel.getHasExternalFaultModel();
    if ((numAftershocks >= 100) && (hasExternalFault = false)) {
      forecastModel.calcTypeII_AfterShockZone(aftershocks);
    }
  }

  /**
   * updateGenericModel
   */
  public void updateGenericModel() {
    //forecastModel.s
  }

}
