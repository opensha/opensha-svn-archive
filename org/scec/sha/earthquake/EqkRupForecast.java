package org.scec.sha.earthquake;

import java.util.ListIterator;


import org.scec.param.ParameterList;
import org.scec.data.Location;
import org.scec.data.region.GeographicRegion;
/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public abstract class EqkRupForecast implements EqkRupForecastAPI{

  protected ParameterList adjustableParams = new ParameterList();

  /**
   * get the adjustable parameters for this forecast
   *
   * @return
   */
   public ListIterator getAdjustableParamsList() {
     return adjustableParams.getParametersIterator();
   }

   /**
   * This function finds whether a particular location lies in applicable
   * region of the forecast
   *
   * @param loc : location
   * @return: True if this location is within forecast's applicable region, else false
   */
  public boolean isLocWithinApplicableRegion(Location loc) {
    return true;
  }


  /**
   * Get the region for which this forecast is applicable
   * @return : Geographic region object specifying the applicable region of forecast
   */
   public GeographicRegion getApplicableRegion() {
     return null;
   }

}