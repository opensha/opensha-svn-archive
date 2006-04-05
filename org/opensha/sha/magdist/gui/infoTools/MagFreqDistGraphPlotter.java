package org.opensha.sha.magdist.gui.infoTools;

import org.opensha.sha.gui.infoTools.GraphPanel;
import java.util.ArrayList;
import org.opensha.sha.gui.infoTools.GraphWindow;
import org.opensha.sha.gui.infoTools.GraphPanelAPI;
import org.opensha.sha.gui.infoTools.ButtonControlPanel;
import org.opensha.sha.gui.infoTools.ButtonControlPanelAPI;
import org.jfree.data.Range;

import org.opensha.sha.gui.infoTools.GraphWindowAPI;

/**
 * <p>Title: IncrementalMagFreqDistPlotter</p>
 *
 * <p>Description: Shows Incremental MagFreqDist in the window.</p>
 *
 * @author Nitin Gupta
 * @version 1.0
 */
public class MagFreqDistGraphPlotter
    implements GraphPanelAPI,
    ButtonControlPanelAPI, GraphWindowAPI {

  //axis range
  private double minXValue, maxXValue,minYValue,maxYValue;
  private boolean customAxis,xLog,yLog;
  //gets all the functions to plot
  private ArrayList functionList;


  //instance for the ButtonControlPanel
  private ButtonControlPanel buttonControlPanel;
  private String xAxisLabel = "Magnitude";
  private String yAxisLabel = "Rate";
  private String chartTitle = "Mag.Freq.Dist.";

  //instance of the GraphPanel for creating the chart
  private GraphPanel graphPanel;


  /**
   * Class constructor that plots the MagFreq. Dist in a window
   * @param functionList ArrayList
   * @param plottingFeatures ArrayList
   */
  public MagFreqDistGraphPlotter(ArrayList functionList,ArrayList plottingFeatures) {

    this.functionList = functionList;
    //object for the ButtonControl Panel
    buttonControlPanel = new ButtonControlPanel(this);
    //instance of the GraphPanel (window that shows all the plots)
    graphPanel = new GraphPanel(this);

    //if plotting features is not null then use plotting features
    //passed as arguments
    if(plottingFeatures !=null && plottingFeatures.size()>0)
      graphPanel.setCurvePlottingCharacterstic(plottingFeatures);
  }


  /**
   * Shows the plot in a pop up window
   */
  public void showPlotWindow(){
    GraphWindow window = new GraphWindow(this,graphPanel);
    window.setPlotLabel(chartTitle);
    window.setVisible(true);
  }



  /**
   * Plots the incremental dist.
   */
  public GraphPanel drawGraph() {

    graphPanel.drawGraphPanel(xAxisLabel, yAxisLabel, functionList, xLog, yLog,
                              customAxis,
                              chartTitle, buttonControlPanel);
    return graphPanel;
  }



  /**
   *
   * @returns the Min X-Axis Range Value, if custom Axis is choosen
   */
  public double getMinX() {
    return minXValue;
  }

  /**
   *
   * @returns the Max X-Axis Range Value, if custom axis is choosen
   */
  public double getMaxX() {
    return maxXValue;
  }

  /**
   *
   * @returns the Min Y-Axis Range Value, if custom axis is choosen
   */
  public double getMinY() {
    return minYValue;
  }

  /**
   *
   * @returns the Max Y-Axis Range Value, if custom axis is choosen
   */
  public double getMaxY() {
    return maxYValue;
  }

  /**
   * sets the range for X and Y axis
   * @param xMin : minimum value for X-axis
   * @param xMax : maximum value for X-axis
   * @param yMin : minimum value for Y-axis
   * @param yMax : maximum value for Y-axis
   *
   */
  public void setAxisRange(double xMin,double xMax, double yMin, double yMax) {
    minXValue=xMin;
    maxXValue=xMax;
    minYValue=yMin;
    maxYValue=yMax;
    this.customAxis=true;
    drawGraph();

  }

  /**
   * set the auto range for the axis. This function is called
   * from the AxisLimitControlPanel
   */
  public void setAutoRange() {
    this.customAxis = false;
    drawGraph();
  }

  //checks if the user has plot the data window or plot window
  public void togglePlot() {
  }



  /**
  * tells the application if the xLog is selected
  * @param xLog : boolean
  */
 public void setX_Log(boolean xLog){
   this.xLog = xLog;
   drawGraph();
 }

 /**
  * tells the application if the yLog is selected
  * @param yLog : boolean
  */
 public void setY_Log(boolean yLog){
   this.yLog = yLog;
   drawGraph();
 }


 /**
  *
  * @returns the Range for the X-Axis
  */
 public Range getX_AxisRange(){
   return graphPanel.getX_AxisRange();
 }

 /**
  *
  * @returns the Range for the Y-Axis
  */
 public Range getY_AxisRange(){
   return graphPanel.getY_AxisRange();
  }


  /**
    *
    * @returns the list PlotCurveCharacterstics that contain the info about
    * plotting the curve like plot line color , its width and line type.
    */
  public ArrayList getPlottingFeatures(){
    return graphPanel.getCurvePlottingCharacterstic();
  }


  /**
   * plots the curves with defined color,line width and shape.
   *
   */
  public void plotGraphUsingPlotPreferences(){
    drawGraph();
  }


  /**
   *
   * @returns the X Axis Label
   */
  public String getXAxisLabel(){
    return xAxisLabel;
  }

  /**
   *
   * @returns Y Axis Label
   */
  public String getYAxisLabel(){
    return yAxisLabel;
  }

  /**
   *
   * @returns plot Title
   */
  public String getPlotLabel(){
    return chartTitle;
  }


  /**
   *
   * sets  X Axis Label
   */
  public void setXAxisLabel(String xAxisLabel){
    this.xAxisLabel = xAxisLabel;
  }

  /**
   *
   * sets Y Axis Label
   */
  public void setYAxisLabel(String yAxisLabel){
    this.yAxisLabel = yAxisLabel;
  }


  /**
  *
  * sets plot Title
  */
  public void setPlotLabel(String plotTitle){
    this.chartTitle = plotTitle;
  }

  /**
   * Returns the function list for the curves
   * @return ArrayList
   */
  public ArrayList getCurveFunctionList() {
    return functionList;
  }

  /**
   * Returns if X-Log Plot
   * @return boolean
   */
  public boolean getXLog() {
    return xLog;
  }

  /**
   * Returns if Y-Log Plot
   * @return boolean
   */
  public boolean getYLog() {
    return yLog;
  }

  /**
   * If custom axis needs to be plotted
   * @return boolean
   */
  public boolean isCustomAxis() {
    return customAxis;
  }

}
