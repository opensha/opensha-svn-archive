package org.scec.sha.gui.infoTools;

import java.awt.*;
import javax.swing.*;
import java.util.ArrayList;

import org.jfree.data.Range;
import org.scec.sha.gui.infoTools.*;
import org.scec.data.function.*;
import org.scec.gui.plot.jfreechart.DiscretizedFunctionXYDataSet;


/**
 * <p>Title: GraphWindow</p>
 * <p>Description: This window pops up when the user wants to see the plot curves
 * in a separate window ( peel the plot from the original window )</p>
 * @author : Nitin Gupta
 * @version 1.0
 */

public class GraphWindow extends JFrame implements ButtonControlPanelAPI,GraphPanelAPI{

  private final static int W=650;
  private final static int H=700;
  private JSplitPane chartSplitPane = new JSplitPane();
  private JPanel chartPane = new JPanel();
  private GridBagLayout gridBagLayout1 = new GridBagLayout();
  private BorderLayout borderLayout1 = new BorderLayout();
  private JPanel buttonPanel = new JPanel();
  private FlowLayout flowLayout1 = new FlowLayout();

  //boolean parameters for the Axis to check for log
  private boolean xLog = false;
  private boolean yLog = false;

  //boolean parameter to check for range of the axis
  private boolean customAxis = false;


  private static int windowNumber =0;

  private static final String TITLE = "Curves Window";

  private double minXValue, maxXValue, minYValue,maxYValue;

  //instance for the ButtonControlPanel
  private ButtonControlPanel buttonControlPanel;

  //instance of the application implementing the Graph Window class
  private GraphWindowAPI application ;

  //instance of the GraphPanel class
  private GraphPanel graphPanel;

  /**
   * List of ArbitrarilyDiscretized functions and Weighted funstions
   */
  private ArrayList functionList ;


  //X and Y Axis  when plotting tha Curves Name
  private String xAxisName;
  private String yAxisName;

  //boolean to check if the plot preferences to be used to draw the curves
  private boolean drawCurvesUsingPlotPrefs;

  /**
   * for Y-log, 0 values will be converted to this small value
   */
  private double Y_MIN_VAL = 1e-16;


  /**
   *
   * @param api : Instance of this application using this object.
   */
  public GraphWindow(GraphWindowAPI api) {
    application = api;
    graphPanel = new GraphPanel(this);
    graphPanel.setSeriesColor(api.getSeriesColor());

    //adding the list of Functions to the Peel-Off window
    functionList = new ArrayList();
    ArrayList applicationCurveList = api.getCurveFunctionList();
    int size = applicationCurveList.size();
    for(int i=0;i<size;++i)
      functionList.add(applicationCurveList.get(i));

    xAxisName = api.getXAxisName();
    yAxisName  = api.getYAxisName();
    try {
      jbInit();
    }
    catch(Exception e) {
      e.printStackTrace();
    }

    //increasing the window number corresponding to the new window.
    ++windowNumber;
    drawGraph();

    /**
     * Recreating the chart with all the default settings that existed in the main application.
     */
    xLog = api.getXLog();
    yLog = api.getYLog();
    customAxis = api.isCustomAxis();
    if(xLog)
      buttonControlPanel.setXLog(xLog);
    if(yLog)
      buttonControlPanel.setYLog(yLog);
    if(customAxis)
      buttonControlPanel.setAxisRange(api.getMinX(),api.getMaxX(),api.getMinY(),api.getMaxY());
  }

  //function to create the GUI component.
  private void jbInit() throws Exception {
    this.setSize(W,H);
    this.getContentPane().setLayout(borderLayout1);
    chartSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
    chartPane.setLayout(gridBagLayout1);
    buttonPanel.setLayout(flowLayout1);
    this.getContentPane().add(chartSplitPane, BorderLayout.CENTER);
    chartSplitPane.add(chartPane, JSplitPane.TOP);
    chartSplitPane.add(buttonPanel, JSplitPane.BOTTOM);
    chartSplitPane.setDividerLocation(600);
    //object for the ButtonControl Panel
    buttonControlPanel = new ButtonControlPanel(this);
    buttonPanel.add(buttonControlPanel,null);
    togglePlot();
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
     customAxis=true;
     drawGraph();
   }

  /**
   * set the auto range for the axis. This function is called
   * from the AxisLimitControlPanel
   */
   public void setAutoRange() {
     customAxis=false;
     drawGraph();
   }

   /**
    * to draw the graph
    */
   private void drawGraph() {
     if(drawCurvesUsingPlotPrefs)
       graphPanel.drawGraphPanel(xAxisName,yAxisName,functionList,xLog,yLog,customAxis,TITLE+"-"+windowNumber,buttonControlPanel,getPlottingFeatures());
     else
       graphPanel.drawGraphPanel(xAxisName,yAxisName,functionList,xLog,yLog,customAxis,TITLE+"-"+windowNumber,buttonControlPanel);
     togglePlot();
   }

   /**
    * plots the curves with defined color,line width and shape.
    * @param plotFeatures
    */
   public void drawGraph(PlotCurveCharacterstics[] plotFeatures){
     graphPanel.drawGraphPanel(xAxisName,yAxisName,functionList,xLog,yLog,customAxis,
                               TITLE+"-"+windowNumber,buttonControlPanel,plotFeatures);
     togglePlot();
   }

   //checks if the user has plot the data window or plot window
   public void togglePlot(){
     chartPane.removeAll();
     graphPanel.togglePlot(buttonControlPanel);
     chartPane.add(graphPanel, new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0
         , GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 0, 0, 0, 0 ), 0, 0 ));
     chartPane.validate();
     chartPane.repaint();
   }



   /**
    *
    * @returns the Min X-Axis Range Value, if custom Axis is choosen
    */
   public double getMinX(){
     return minXValue;
   }

   /**
    *
    * @returns the Max X-Axis Range Value, if custom axis is choosen
    */
   public double getMaxX(){
     return maxXValue;
   }

   /**
    *
    * @returns the Min Y-Axis Range Value, if custom axis is choosen
    */
   public double getMinY(){
     return minYValue;
   }

   /**
    *
    * @returns the Max Y-Axis Range Value, if custom axis is choosen
    */
   public double getMaxY(){
     return maxYValue;
  }

  /**
   *
   * @returns the plotting feature like width, color and shape type of each
   * curve in list.
   */
  public PlotCurveCharacterstics[] getPlottingFeatures(){
    return graphPanel.getCurvePlottingCharactersticInfo();
  }


  /**
   *
   * @param usePlotPrefs: boolean for checking if curves
   * need to be plotted using the plotting preferences.
   */
  public void setCurvesToUsePlotPrefs(boolean usePlotPrefs){
    drawCurvesUsingPlotPrefs = usePlotPrefs;
  }
}
