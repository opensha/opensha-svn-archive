package org.scec.sha.gui.infoTools;

import java.awt.Component;

import org.scec.sha.gui.controls.AxisLimitsControlPanelAPI;
import org.jfree.data.Range;
import org.scec.sha.gui.infoTools.PlotCurveCharacterstics;

/**
 * <p>Title: ButtonControlPanelAPI</p>
 * <p>Description: This interface has to be implemented by the application that uses
 * ButtonControlPanel class</p>
 * @author : Nitin Gupta
 * @version 1.0
 */

public interface ButtonControlPanelAPI extends AxisLimitsControlPanelAPI{


   /**
    * Toggles between the Graph and the Data Window
    */
   public void togglePlot();


   /**
    * tells the application if the xLog is selected
    * @param xLog : boolean
    */
   public void setX_Log(boolean xLog);

   /**
    * tells the application if the yLog is selected
    * @param yLog : boolean
    */
   public void setY_Log(boolean yLog);

   /**
    * Gets the range for the X-Axis
    * @return
    */
   public Range getX_AxisRange();

   /**
    * Gets the range for the Y-Axis
    * @return
    */
   public Range getY_AxisRange();

   /**
    *
    * @returns the plotting feature like width, color and shape type of each
    * curve in list.
    */
   public PlotCurveCharacterstics[] getPlottingFeatures();

   /**
    * plots the curves with defined color,line width and shape.
    * @param plotFeatures
    */
   public void drawGraph(PlotCurveCharacterstics[] plotFeatures);

   /**
    *
    * @param usePlotPrefs: boolean for checking if curves
    * need to be plotted using the plotting preferences.
    */
   public void setCurvesToUsePlotPrefs(boolean usePlotPrefs);
}
