package org.opensha.sha.gui.controls;

/**
 * <p>Title: AxisLimitsControlPanelAPI </p>
 * <p>Description: Any applet which uses the AxisLimitsControlPanel needs
 * to implement this API </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public interface AxisLimitsControlPanelAPI {

  /**
   * sets the range for X and Y axis
   * @param xMin : minimum value for X-axis
   * @param xMax : maximum value for X-axis
   * @param yMin : minimum value for Y-axis
   * @param yMax : maximum value for Y-axis
   *
   */
  public void setAxisRange(double xMin,double xMax, double yMin, double yMax);

  /**
   * This function sets auto range for axis
   */
  public void setAutoRange();

}
