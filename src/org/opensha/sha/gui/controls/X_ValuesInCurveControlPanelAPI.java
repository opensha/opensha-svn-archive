package org.opensha.sha.gui.controls;

import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;

/**
 * <p>Title: X_ValuesInCurveControlPanelAPI</p>
 * <p>Description: Interface to Application and XValueControlPanel. It gets the
 * IMT value from the application based on which it selects the default X Values</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public interface X_ValuesInCurveControlPanelAPI {

  /**
   * Get the selected IMT from the application, based on which it shows the
   * default X Values for the chosen IMT.
   * @return
   */
  public String getSelectedIMT();

  /**
   * Set the X Values from the ArbitrarilyDiscretizedFunc passed as the parameter
   * @param func
   */
  public void setX_ValuesForHazardCurve(ArbitrarilyDiscretizedFunc func);

  /**
   *Set the default X Values for the Hazard Curve for the selected IMT.
   */
  public void setX_ValuesForHazardCurve();
}
