package org.scec.sha.gui.infoTools;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.text.*;
import java.util.ArrayList;

import org.jfree.chart.*;
import org.jfree.chart.axis.*;
import org.jfree.chart.renderer.*;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.data.*;
import org.jfree.chart.plot.*;

import org.scec.data.function.*;
import org.scec.gui.plot.jfreechart.*;
import org.scec.gui.*;
import org.scec.sha.gui.infoTools.ButtonControlPanel;
import org.scec.util.*;
import org.scec.sha.gui.infoTools.WeightedFuncListforPlotting;

/**
 * <p>Title: GraphPanel</p>
 * <p>Description: This class shows the JFreechart Panel in a window</p>
 * @author : Nitin Gupta
 * @version 1.0
 */

public class GraphPanel extends JPanel {



  // mesage needed in case of show data if plot is not available
  private final static String NO_PLOT_MSG = "No Plot Data Available";


  Color[] defaultColor = {Color.red,Color.blue,Color.cyan,Color.darkGray,Color.magenta,
    Color.gray,Color.green,Color.orange,Color.pink,Color.yellow};

  Color[] legendColor = null;
  Paint[] legendPaint = null;

  private SimpleAttributeSet setLegend;

  private BorderLayout borderLayout1 = new BorderLayout();
  private JSplitPane chartSplitPane = new JSplitPane();
  private JScrollPane metadataScrollPane = new JScrollPane();
  private JPanel chartPane = new JPanel();

  private Insets plotInsets = new Insets( 4, 4, 4, 4 );

  //dataset to handover the data to JFreechart
  private DiscretizedFunctionXYDataSet data = new DiscretizedFunctionXYDataSet();
  //functionList
  private DiscretizedFuncList totalProbFuncs = new DiscretizedFuncList();

  /**
   * for Y-log, 0 values will be converted to this small value
   */
  private double Y_MIN_VAL = 1e-16;

  //graph chart panel
  private ChartPanel chartPanel;


  private XYPlot plot;

  // Create the x-axis and y-axis - either normal or log
  //xaxis1 and yAxis1 replica of the x-Axis and y-Axis object, in case error occurs
  //and we have revert back the Axis
  NumberAxis xAxis, xAxis1 ;
  NumberAxis yAxis, yAxis1;

  // light blue color
  private Color lightBlue = new Color( 200, 200, 230 );

  //Keeps track when to toggle between the data and chart.
  private boolean graphOn = false;


  //Instance of the application using this class
  GraphPanelAPI application;


  JTextPane metadataText = new JTextPane();

  /**
   * adding scroll pane for showing data
   */
  private JScrollPane dataScrollPane = new JScrollPane();

  // text area to show the data values
  private JTextArea pointsTextArea = new JTextArea();
  private GridBagLayout gridBagLayout1 = new GridBagLayout();

  /**
   * class constructor
   * @param api : Application instance
   */
  public GraphPanel(GraphPanelAPI api) {
    data.setFunctions(this.totalProbFuncs);
    // for Y-log, convert 0 values in Y axis to this small value, it just sets the minimum
    //value
    data.setConvertZeroToMin(true,Y_MIN_VAL);
    application = api;
    try {
      jbInit();
    }
    catch(Exception ex) {
      ex.printStackTrace();
    }
  }
  void jbInit() throws Exception {
    this.setLayout(borderLayout1);
    chartSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
    metadataText.setEditable(false);
    chartPane.setLayout(gridBagLayout1);
    add(chartSplitPane, BorderLayout.CENTER);
    chartSplitPane.add(metadataScrollPane, JSplitPane.BOTTOM);
    metadataScrollPane.getViewport().add(metadataText, null);
    chartSplitPane.add(chartPane,JSplitPane.TOP);
    chartSplitPane.setDividerLocation(450);

    // for showing the data on click of "show data" button
    pointsTextArea.setBorder( BorderFactory.createEtchedBorder() );
    pointsTextArea.setText( NO_PLOT_MSG );
    pointsTextArea.setLineWrap(true);
    dataScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    dataScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    dataScrollPane.setBorder( BorderFactory.createEtchedBorder() );
    dataScrollPane.getViewport().add( pointsTextArea, null );
  }


  /**
   * Draws the graph panel
   */

  public void drawGraphPanel(String xAxisName, String yAxisName,ArrayList funcList,
                             boolean xLog,boolean yLog,boolean customAxis, String title,
                             ButtonControlPanel buttonControlPanel ) {

    // Starting
    String S = "drawGraphPanel(): ";

    //if(legendColor == null)
    createColorSchemeAndFunctionList(funcList);

    totalProbFuncs.setXAxisName(xAxisName);
    totalProbFuncs.setYAxisName(yAxisName);
    // create a default chart based on some sample data...


    // set the log flag in the XY dataset
    data.setXLog(xLog);
    data.setYLog(yLog);


    //flags to check if the exception was thrown on selection of the x-log or y-log.
    boolean logErrorFlag = false;


    //create the standard ticks so that smaller values too can plotted on the chart
    TickUnits units = MyTickUnits.createStandardTickUnits();

    try{

      /// check if x log is selected or not
      if(xLog) xAxis = new LogarithmicAxis(xAxisName);
      else xAxis = new NumberAxis( xAxisName );

      if (!xLog)
        xAxis.setAutoRangeIncludesZero(true);
      else
        xAxis.setAutoRangeIncludesZero( false );
      xAxis.setStandardTickUnits(units);
      xAxis.setTickMarksVisible(false);
      //added to have the minimum range within the Upper and Lower Bound of the Axis
      //xAxis.setAutoRangeMinimumSize(.1);

      /* to set the range of the axis on the input from the user if the range combo box is selected*/
      if(customAxis)
        xAxis.setRange(application.getMinX(),application.getMaxX());

    }catch(Exception e){
      JOptionPane.showMessageDialog(this,e.getMessage(),"X-Plot Error",JOptionPane.OK_OPTION);
      graphOn=false;
      xLog = false;
      buttonControlPanel.setXLog(xLog);
      xAxis = xAxis1;
      logErrorFlag = true;
    }

    try{
      /// check if y log is selected or not
      if(yLog) yAxis = new LogarithmicAxis(yAxisName);
      else yAxis = new NumberAxis( yAxisName );

      if (!yLog)
        yAxis.setAutoRangeIncludesZero(true);
      else
        yAxis.setAutoRangeIncludesZero( false );

      yAxis.setStandardTickUnits(units);
      yAxis.setTickMarksVisible(false);
      //added to have the minimum range within the Upper and Lower Bound of the Axis
      //yAxis.setAutoRangeMinimumSize(.1);

      /* to set the range of the axis on the input from the user if the range combo box is selected*/
      if(customAxis)
        yAxis.setRange(application.getMinY(),application.getMaxY());

    }catch(Exception e){
      JOptionPane.showMessageDialog(this,e.getMessage(),"Y-Plot Error",JOptionPane.OK_OPTION);
      graphOn=false;
      yLog = true;
      buttonControlPanel.setYLog(yLog);
      yAxis = yAxis1;
      logErrorFlag = false;
    }
    int type = org.jfree.chart.renderer.StandardXYItemRenderer.LINES;


    org.jfree.chart.renderer.StandardXYItemRenderer renderer
        = new org.jfree.chart.renderer.StandardXYItemRenderer( type, new StandardXYToolTipGenerator() );


    // build the plot
    plot = new XYPlot(data,xAxis, yAxis, renderer);

    //setting the plot properties
    plot.setDomainCrosshairLockedOnData(false);
    plot.setDomainCrosshairVisible(false);
    plot.setRangeCrosshairLockedOnData(false);
    plot.setRangeCrosshairVisible(false);
    plot.setInsets(new Insets(10, 0, 0, 20));

    int numSeries = legendPaint.length;
    for(int i=0; i < numSeries; ++i) renderer.setSeriesPaint(i,legendPaint[i]);

    plot.setRenderer( renderer );
    plot.setBackgroundAlpha( .8f );

    JFreeChart chart = new JFreeChart(title, JFreeChart.DEFAULT_TITLE_FONT, plot, false );

    chart.setBackgroundPaint( lightBlue );

    // Put into a panel
    chartPanel = new ChartPanel(chart, true, true, true, true, false);

    chartPanel.setBorder( BorderFactory.createEtchedBorder( EtchedBorder.LOWERED ) );
    chartPanel.setMouseZoomable(true);
    chartPanel.setDisplayToolTips(true);
    chartPanel.setHorizontalAxisTrace(false);
    chartPanel.setVerticalAxisTrace(false);

    // set the font of legend
    int numOfColors = plot.getSeriesCount();

    /**
     * Adding the metadata text to the Window below the Chart
     */
    metadataText.removeAll();
    metadataText.setEditable(false);
    setLegend =new SimpleAttributeSet();
    setLegend.addAttribute(StyleConstants.CharacterConstants.Bold,
                           Boolean.TRUE);
    Document doc = metadataText.getStyledDocument();
    try {

      /**
       * formatting the metadata to be added , according to the colors of the
       * Curves. So now curves and metadata will be displayed in the same color.
       */
      doc.remove(0,doc.getLength());
      //keeps track of function number
      int count =0;
      for(int i=0,j=0;i<numOfColors;++i,++j){
          if(j==legendColor.length)
            j=0;
          String name = totalProbFuncs.get(i).getName();
          String functionInfo = totalProbFuncs.get(i).getInfo();
          if(functionInfo !=null && !functionInfo.trim().equals("")){
            ++count;
            String legend = new String(count+")  "+name+"  "+SystemPropertiesUtils.getSystemLineSeparator()+
                                       functionInfo+SystemPropertiesUtils.getSystemLineSeparator());
            setLegend =new SimpleAttributeSet();
            StyleConstants.setFontSize(setLegend,12);
            StyleConstants.setForeground(setLegend,legendColor[j]);
            doc.insertString(doc.getLength(),legend,setLegend);
          }
        }
      } catch (BadLocationException e) {
        return;
      }
      graphOn=false;

      //Check to see if there is no log Error and only  xLog or yLog are selected
      if(!logErrorFlag && !xLog)
        xAxis1 = xAxis;
      if(!logErrorFlag && !yLog)
        yAxis1 = yAxis;
      pointsTextArea.setText(totalProbFuncs.toString());
       return ;
  }


  /**
   * Sets the metadata in the Data window
   * @param metadata
   */
  public void setMetadata(String metadata){
   pointsTextArea.setText(metadata);
  }


  /**
   * Clears the plot and the Metadata Window
   */
  public void removeChartAndMetadata(){
    chartPane.removeAll();
    chartPanel = null;
    metadataText.setText("");
    pointsTextArea.setText(this.NO_PLOT_MSG);
  }


  /**
   *  Toggle between showing the graph and showing the actual data
   */
  public void togglePlot(ButtonControlPanel buttonControlPanel) {

    chartPane.removeAll();
    //showing the data window
    if ( graphOn ) {

      buttonControlPanel.setToggleButtonText( "Show Plot" );
      graphOn = false;

      chartPane.add(dataScrollPane,new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0
          , GridBagConstraints.CENTER, GridBagConstraints.BOTH, plotInsets, 0, 0 ) );
    }
    else {
      //showing the Plot window, if not null
      graphOn = true;

      buttonControlPanel.setToggleButtonText("Show Data");
      // panel added here
      if(chartPanel !=null) {
        chartPane.add(chartPanel,new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0
          , GridBagConstraints.CENTER, GridBagConstraints.BOTH, plotInsets, 0, 0 ) );

      }
      else chartPane.add(dataScrollPane, new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0
          , GridBagConstraints.CENTER, GridBagConstraints.BOTH, plotInsets, 0, 0 ) );

    }
    return ;
  }

  /**
   * sets the backgound for the plot
   * @param color
   */
  public void setPlotBackgroundColor(Color color){
   if(plot !=null)
     plot.setBackgroundPaint(color);
  }
  /**
   *
   * @returns the Range for the X-Axis
   */
  public Range getX_AxisRange(){
    return xAxis.getRange();
  }

  /**
   *
   * @returns the Range for the Y-Axis
   */
  public Range getY_AxisRange(){
    return yAxis.getRange();
  }


  /**
   * Sets the paint color of the curves from outside
   * @param color : Array of Color Object
   */
  public void setSeriesColor(Color[] color){
    int size = color.length;
    legendPaint = new Paint[size];
    legendColor = new Color[size];

    for(int i=0;i<size;++i){
      legendPaint[i] = color[i];
      if(color[i] !=null)
        legendColor[i] = new Color(color[i].getRGB());
    }
  }


  /**
   *
   * @returns the series color being used for the current graph schemes
   */
  public Color[] getSeriesColor(){
    return legendColor;
  }



  /**
   * Sets the default color scheme for the cureves drawn
   */
  public void setDefaultSeriesColor(){
    legendPaint = defaultColor;
    legendColor = defaultColor;
  }



  /**
   * This method extracts all the functions from the ArrayList and add that
   * to the DiscretizedFunction List. This method also creates the color scheme
   * depending on the different types of DiscretizedFunc added to the list.
   * @param functionList
   */
  private void createColorSchemeAndFunctionList(ArrayList functionList){

    totalProbFuncs.clear();
    int numCurves  = functionList.size();
    ArrayList numColorArray = new ArrayList();

    for(int i=0;i<numCurves;++i){
      Object obj = functionList.get(i);
      if(obj instanceof WeightedFuncListforPlotting){
        WeightedFuncListforPlotting weightedList = (WeightedFuncListforPlotting)obj;
        if(weightedList.areIndividualCurvesToPlot()){
         DiscretizedFuncList list= weightedList.getWeightedFunctionList();
         list.get(0).setInfo(weightedList.getInfo()+"\n"+"\t"+list.getInfo());
         numColorArray.add(new Integer(list.size()));
         totalProbFuncs.addAll(list);
        }
        if(weightedList.areFractilesToPlot()){
          DiscretizedFuncList list= weightedList.getFractileList();
          list.get(0).setInfo(list.getInfo());
          totalProbFuncs.addAll(list);
          numColorArray.add(new Integer(list.size()));
        }
        if(weightedList.isMeanToPlot()){
          DiscretizedFuncAPI meanFunc = weightedList.getMean();
          totalProbFuncs.add(meanFunc);
          numColorArray.add(new Integer(1));
        }
      }
      else{
        totalProbFuncs.add((DiscretizedFuncAPI)obj);
        numColorArray.add(new Integer(1));
      }
    }

    int size = totalProbFuncs.size();
    Color[] color = new Color[size];


    int numDiffColors = numColorArray.size();

    //int colorChoice = (int)255/numDiffColors;
    //creating the color array
    int index=0;
    //looping over all the default colors to add those to the color array
    for(int i=0,defaultColorIndex =0;i<numDiffColors;++i,++defaultColorIndex){
      //if the number of curves to be drawn are more in number then default colors then start from first again
      if(defaultColorIndex == defaultColor.length)
        defaultColorIndex = 0;
      int val = ((Integer)numColorArray.get(i)).intValue();
      for(int j=0;j<val;++j)
        color[index++] = defaultColor[defaultColorIndex];
      }
      //setting the color scheme
      setSeriesColor(color);
  }


}