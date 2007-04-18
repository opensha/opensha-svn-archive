package org.opensha.sha.gui.infoTools;

import java.util.ArrayList;

import org.jfree.data.Range;
import org.opensha.sha.gui.controls.AxisLimitsControlPanelAPI;


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
   public ArrayList getPlottingFeatures();

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
