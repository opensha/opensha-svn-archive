package org.scec.sha.gui.controls;

import org.scec.param.ParameterList;
/**
 * <p>Title: PropagationEffectControlPanelAPI</p>
 * <p>Description: This interface has to be implemented by the class that needs to use
 * the PropagationEffect control panel.</p>
 * @author : Ned Field, Nitin Gupta and Vipin Gupta
 * @version 1.0
 */

public interface PropagationEffectControlPanelAPI {


  /**
   *
   * @returns the Adjustable parameters for the Hazardcurve and Scenarioshakemap calculator
   */
  public ParameterList getCalcAdjustableParams();

}