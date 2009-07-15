package org.opensha.sha.gui.controls;


import org.opensha.sha.gui.infoTools.PlotCurveCharacterstics;

/**
 * <p>Title: PlotColorAndLineTypeSelectorControlPanelAPI</p>
 * <p>Description: Application using PlotColorAndLineTypeSelectorControlPanel
 * implements this interface, so if control panel needs to call a
 * method of application, it can do that without knowing which class method
 * needs to be called.</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public interface PlotColorAndLineTypeSelectorControlPanelAPI {

  /**
   * plots the curves with defined color,line width and shape.
   *
   */
   public void plotGraphUsingPlotPreferences();

   /**
    *
    * @returns the X Axis Label
    */
   public String getXAxisLabel();

   /**
    *
    * @returns Y Axis Label
    */
   public String getYAxisLabel();

   /**
    *
    * @returns plot Title
    */
   public String getPlotLabel();

   /**
    *
    * sets  X Axis Label
    */
   public void setXAxisLabel(String xAxisLabel);

   /**
    *
    * sets Y Axis Label
    */
   public void setYAxisLabel(String yAxisLabel);

   /**
    *
    * sets plot Title
    */
   public void setPlotLabel(String plotTitle);
}
