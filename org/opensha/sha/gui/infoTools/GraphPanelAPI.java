package org.opensha.sha.gui.infoTools;

/**
 * <p>Title: GraphPanelAPI</p>
 * <p>Description: This interface has to be implemented by the application that uses
 * GraphPanel class</p>
 * @author : Nitin Gupta
 * @created : Jan 21,2004
 * @version 1.0
 */

public interface GraphPanelAPI {

  /**
   *
   * @returns the Min X-Axis Range Value, if custom Axis is choosen
   */
  public double getMinX();

  /**
   *
   * @returns the Max X-Axis Range Value, if custom axis is choosen
   */
  public double getMaxX();

  /**
   *
   * @returns the Min Y-Axis Range Value, if custom axis is choosen
   */
  public double getMinY();

  /**
   *
   * @returns the Max Y-Axis Range Value, if custom axis is choosen
   */
  public double getMaxY();

}
