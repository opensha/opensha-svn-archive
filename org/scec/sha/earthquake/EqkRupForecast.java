package org.scec.sha.earthquake;


import org.scec.param.ParameterList;
/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public abstract class EqkRupForecast implements EqkRupForecastAPI{

  private ParameterList adjustableParams = new ParameterList();

  /**
   * get the adjustable parameters for this forecast
   *
   * @return
   */
   public ParameterList getAdjustableParamsList() {
     return adjustableParams;
   }

}