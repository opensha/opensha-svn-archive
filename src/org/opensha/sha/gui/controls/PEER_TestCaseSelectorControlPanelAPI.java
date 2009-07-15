package org.opensha.sha.gui.controls;

import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;

/**
 * <p>Title: PEER_TestCaseSelectorControlPanelAPI</p>
 * <p>Description: This interface sets the X Values for hazard curve using
 * PEER test cases </p>
 * @author unascribed
 * @version 1.0
 */

public interface PEER_TestCaseSelectorControlPanelAPI {

  /**
   * Set the X Values from the ArbitrarilyDiscretizedFunc passed as the parameter
   * @param func
   */
  public void setX_ValuesForHazardCurve(ArbitrarilyDiscretizedFunc func);
}
