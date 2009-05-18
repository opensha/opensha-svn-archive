package org.opensha.sha.gui.infoTools;

import java.awt.Color;
import java.util.ArrayList;

import org.opensha.commons.data.function.DiscretizedFuncList;
import org.opensha.gui.plot.jfreechart.DiscretizedFunctionXYDataSet;


/**
 * <p>Title: GraphWindowAPI</p>
 * <p>Description: This interface has to be implemented by the application that uses
 * GraphWindow class</p>
 * @author : Nitin Gupta
 * @version 1.0
 */

public interface GraphWindowAPI {



  /**
   *
   * @returns the List for all the ArbitrarilyDiscretizedFunctions and Weighted Function list.
   */
  public ArrayList getCurveFunctionList();

  /**
   *
   * @returns the boolean: Log for X-Axis Selected
   */
  public boolean getXLog();

  /**
   *
   * @returns the boolean: Log for Y-Axis Selected
   */
  public boolean getYLog();

  //get Y axis Label
  public String getXAxisLabel();

  //gets X Axis Label
  public String getYAxisLabel();

  /**
   *
   * @returns the plotting feature like width, color and shape type of each
   * curve in list.
   */
   public ArrayList getPlottingFeatures();


  /**
   *
   * @returns boolean: Checks if Custom Axis is selected
   */
  public boolean isCustomAxis();

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
