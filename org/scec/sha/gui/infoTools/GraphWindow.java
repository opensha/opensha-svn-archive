package org.scec.sha.gui.infoTools;

import java.awt.*;
import javax.swing.*;

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
  private final static int H=800;
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
  /**
   * FunctionList declared
   */
  private DiscretizedFuncList totalProbFuncs = new DiscretizedFuncList();
  private DiscretizedFunctionXYDataSet data = new DiscretizedFunctionXYDataSet();

  private double minXValue, maxXValue, minYValue,maxYValue;

  //instance for the ButtonControlPanel
  ButtonControlPanel buttonControlPanel;

  //instance of the application implementing the Graph Window class
  GraphWindowAPI application ;

  //instance of the GraphPanel class
  GraphPanel graphPanel;

  /**
   *
   * @param api : Instance of this application using this object.
   */
  public GraphWindow(GraphWindowAPI api) {
    application = api;
    graphPanel = new GraphPanel(this);
    graphPanel.setSeriesPaint(api.getGraphPanel().getSeriesColor());
    data = api.getXY_DataSet().deepClone();
    totalProbFuncs = api.getCurveFunctionList().deepClone();
    try {
      jbInit();
    }
    catch(Exception e) {
      e.printStackTrace();
    }
    drawGraph();
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
    chartSplitPane.setDividerLocation(525);
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
    data.setXLog(xLog);
    drawGraph();
  }

  /**
   * tells the application if the yLog is selected
   * @param yLog : boolean
   */
  public void setY_Log(boolean yLog){
    this.yLog = yLog;
    data.setYLog(yLog);
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
     data.setXLog(xLog);
     data.setYLog(yLog);
     graphPanel.drawGraphPanel(totalProbFuncs,data,xLog,yLog,customAxis,TITLE+"-"+windowNumber);
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

}
