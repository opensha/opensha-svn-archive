package org.opensha.sha.magdist.gui.infoTools;

import org.opensha.sha.gui.infoTools.*;

/**
 * <p>Title:MagFreqDistPlotWindow </p>
 *
 * <p>Description: Shows the Mag Dist plot in a pop up window.
 * This class was created so that if Plot Preferences are changed for
 * any of the Dist types then other plots are changed automatically.
 * For eg: If plot preferences are changed for Incremental Mag Freq. Dist then
 * they should be reflected in Cum. Mag Freq Dist plots or moment-rate plots.</p>
 *
 * @author Nitin Gupta
 * @version 1.0
 */
public class MagFreqDistPlotWindow
    extends GraphWindow {


  private String plotTitle = "Mag.Freq.Dist.";
  private MagFreqDistPlotWindowAPI application;
    /**
     *
     * @param api : Instance of this application using this object.
     */
   public MagFreqDistPlotWindow(MagFreqDistPlotWindowAPI api,GraphPanel graphPanel) {
     application = api;
     this.graphPanel = graphPanel;
     this.functionList = api.getCurveFunctionList();
     xAxisName = api.getXAxisLabel();
     yAxisName = api.getYAxisLabel();

     try {
        jbInit();
      }
      catch (Exception e) {
        e.printStackTrace();
      }

     //increasing the window number corresponding to the new window.
     ++windowNumber;
     /**
      * Recreating the chart with all the default settings that existed in the main application.
      */
     xLog = api.getXLog();
     yLog = api.getYLog();
     customAxis = api.isCustomAxis();
     if (customAxis)
       buttonControlPanel.setAxisRange(api.getMinX(), api.getMaxX(), api.getMinY(),
                                       api.getMaxY());
     if (xLog)
       buttonControlPanel.setXLog(xLog);
     if (yLog)
       buttonControlPanel.setYLog(yLog);
     if (!xLog && !yLog)
       drawGraph();
 }


 /**
  * Creates the graph. This method is different from  plotGraphUsingPlotPreferences
  * as it does not send the event back to the application that Plot Prefs have changed.
  */
  public void drawGraph() {
    super.drawGraph();
  }

 /**
  * plots the curves with defined color,line width and shape.
  * @param plotFeatures
  */
 public void plotGraphUsingPlotPreferences() {
   drawGraph();
   application.setPlotPreferencesChanged(true);
 }


}
