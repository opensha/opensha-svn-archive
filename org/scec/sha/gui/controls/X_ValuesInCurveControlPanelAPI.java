package org.scec.sha.gui.controls;

import org.scec.data.function.ArbitrarilyDiscretizedFunc;

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

  public String getSelectedIMT();
  public void setX_ValuesForHazardCurve(ArbitrarilyDiscretizedFunc func);
  public void setX_ValuesForHazardCurve();
}