package org.scec.sha.earthquake;

import java.util.ListIterator;

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

  protected ParameterList adjustableParams = new ParameterList();

  /**
   * get the adjustable parameters for this forecast
   *
   * @return
   */
   public ListIterator getAdjustableParamsList() {
     return adjustableParams.getParametersIterator();
   }

}