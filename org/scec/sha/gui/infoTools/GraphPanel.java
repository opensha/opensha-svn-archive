package org.scec.sha.gui.infoTools;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.text.*;
import java.util.ArrayList;
import java.awt.geom.*;

import org.jfree.chart.*;
import org.jfree.chart.axis.*;
import org.jfree.chart.renderer.*;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.data.*;
import org.jfree.chart.plot.*;
import org.jfree.util.ShapeUtils;

import org.scec.data.function.*;
import org.scec.gui.plot.jfreechart.*;
import org.scec.gui.*;
import org.scec.sha.gui.infoTools.ButtonControlPanel;
import org.scec.util.*;
import org.scec.sha.gui.infoTools.WeightedFuncListforPlotting;
import org.scec.sha.gui.controls.PlotColorAndLineTypeSelectorControlPanel;


/**
 * <p>Title: GraphPanel</p>
 * <p>Description: This class shows the JFreechart Panel in a window. It plot curves
 * using JFrechart package and if application supports allowing user to specify
 * different styles, colors and width of each curve the this application plots that
 * for the person.</p>
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

  // these are coordinates and size of the circles visible in the plot
  private final static double SIZE = 3;
  private final static double DELTA = SIZE / 2.0;


  private Insets plotInsets = new Insets( 4, 4, 4, 4 );

  //dataset to handover the data to JFreechart
  private DiscretizedFunctionXYDataSet data = new DiscretizedFunctionXYDataSet();
  //functionList
  private DiscretizedFuncList totalProbFuncs = new DiscretizedFuncList();

  //variable to see how many datasets exists with different types of color coding scheme.
  private int numDataset =0;


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

  //characterstics for each curve that we plot which include the line color,line width,
  //line type.
  PlotCurveCharacterstics[] curvePlottingCharacterstics;

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
   * Draws curves using the plot preferences which defines color,width and line type
   * for each curve.
   * @param xAxisName : X-Axis Label
   * @param yAxisName : Y-Axis Label
   * @param funcList  : ArrayList containing individual functions and weighted functionlist
   * @param xLog      : boolean tell if xLog is selected
   * @param yLog      : boolean tells if yLog is selected
   * @param customAxis : boolean tells if graph needs to ne plotted using custom axis range
   * @param title  :
   * @param buttonControlPanel
   * @param plotCharacterstics
   */
  public void drawGraphPanel(String xAxisName,String yAxisName,ArrayList funcList,
                             boolean xLog,boolean yLog,boolean customAxis,String title,
                             ButtonControlPanel buttonControlPanel,
                             PlotCurveCharacterstics[] plotCharacterstics){

    //curve characterstics list
    curvePlottingCharacterstics = plotCharacterstics;

    // Starting
    String S = "drawGraphPanel(): ";

    totalProbFuncs.setXAxisName(xAxisName);
    totalProbFuncs.setYAxisName(yAxisName);


    // set the log flag in the XY dataset
    //data.setXLog(xLog);
    //data.setYLog(yLog);


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
      yLog = false;
      buttonControlPanel.setYLog(yLog);
      yAxis = yAxis1;
      logErrorFlag = false;
    }

    //DiscretizedFuncList primaryDataFunctions = new DiscretizedFuncList();
    //DiscretizedFunctionXYDataSet primaryDataset = new DiscretizedFunctionXYDataSet();

    //primaryDataFunctions.add(totalProbFuncs.get(0));
    //primaryDataset.setFunctions(primaryDataFunctions);

    plot = null;
    // build the plot
    plot = new XYPlot(null,xAxis, yAxis, null);

    //setting the plot properties
    plot.setDomainCrosshairLockedOnData(false);
    plot.setDomainCrosshairVisible(false);
    plot.setRangeCrosshairLockedOnData(false);
    plot.setRangeCrosshairVisible(false);
    plot.setInsets(new Insets(10, 0, 0, 20));


    //total number of funtions that need to be plotted differently
    int numFuncs = plotCharacterstics.length;
    int datasetIndex = 0;
    //secondarydataset index keeps track where do we have to add the seconadary data set in plot
    for(int j=0,secondaryDatasetIndex=0; j < numFuncs; ++j,++secondaryDatasetIndex){
      Color color = plotCharacterstics[j].getCurveColor();
      double lineWidth = plotCharacterstics[j].getCurveWidth();
      String lineType = plotCharacterstics[j].getCurveType();
      //getting the number of consecutive curves that have same plotting characterstics.
      int numCurves = plotCharacterstics[j].getNumContinuousCurvesWithSameCharacterstics();
      //if size of that plot size then don't add it to the dataset
      if(lineWidth ==0){
        datasetIndex +=numCurves;
        --secondaryDatasetIndex;
        continue;
      }
      DiscretizedFuncList dataFunctions = new DiscretizedFuncList();
      DiscretizedFunctionXYDataSet dataset = new DiscretizedFunctionXYDataSet();
      dataset.setXLog(xLog);
      dataset.setYLog(yLog);
      dataset.setConvertZeroToMin(true,Y_MIN_VAL);
      dataset.setFunctions(dataFunctions);


      //creating the secondary dataset to show it in different color and shapes
      for(int i=datasetIndex;i<(datasetIndex+numCurves);++i)
        dataFunctions.add(totalProbFuncs.get(i));
      datasetIndex +=numCurves;

      if(secondaryDatasetIndex!=0)
        plot.setSecondaryDataset(secondaryDatasetIndex-1,dataset);
      else
        plot.setDataset(dataset);

      drawCurvesUsingPlottingFeatures(lineType,color,lineWidth,secondaryDatasetIndex);
    }

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
      //total number of elements in the list containing individual functions and
      //weighted function list.
      int totalNumofFunctions = funcList.size();
      //getting the metadata associated with each function in the list
      for(int i=0,j=0;i<totalNumofFunctions;++i){
        String legend=null;
        //setting the font style for the legend
        setLegend =new SimpleAttributeSet();
        StyleConstants.setFontSize(setLegend,12);
        //checking if element in the list is weighted function list object
        Object obj = funcList.get(i);
        if(obj instanceof WeightedFuncListforPlotting){
          //getting the metadata for weighted functionlist
          WeightedFuncListforPlotting weightedList = (WeightedFuncListforPlotting)obj;

          String listInfo = weightedList.getInfo();
          legend = new String("DATASET #"+(i+1)+"\n\n"+
                              listInfo+SystemPropertiesUtils.getSystemLineSeparator());
          StyleConstants.setForeground(setLegend,Color.black);
          doc.insertString(doc.getLength(),legend,setLegend);
          //checking if individual curves need to be plotted
          if(weightedList.areIndividualCurvesToPlot()){
            //getting the metadata for each individual curves and creating the legend string
            String listFunctionsInfo = weightedList.getFunctionTraceInfo();

            legend = new String(listFunctionsInfo+SystemPropertiesUtils.getSystemLineSeparator());
            //System.out.println("Color for legend:"+plotCharacterstics[j].getCurveColor());
            //if(setLegend  == null)
              //System.out.println("setLegend is null");
            StyleConstants.setForeground(setLegend,plotCharacterstics[j].getCurveColor());
            doc.insertString(doc.getLength(),legend,setLegend);
            ++j;
          }
          //checking if fractiles need to be plotted
          if(weightedList.areFractilesToPlot()){
            //getting the fractile info for the weighted function list and adding that to the legend
            String fractileListInfo = weightedList.getFractileInfo();

            legend = new String(fractileListInfo+SystemPropertiesUtils.getSystemLineSeparator());
            StyleConstants.setForeground(setLegend,plotCharacterstics[j].getCurveColor());
            doc.insertString(doc.getLength(),legend,setLegend);
            ++j;
          }
          //checking if mean fractile need to be plotted
          if(weightedList.isMeanToPlot()){
            //getting the fractileinfo and showing it as legend
            String meanInfo = weightedList.getMeanFunctionInfo();

            legend = new String(meanInfo+SystemPropertiesUtils.getSystemLineSeparator());
            StyleConstants.setForeground(setLegend,plotCharacterstics[j].getCurveColor());
            doc.insertString(doc.getLength(),legend,setLegend);
            ++j;
           }
        }
        else{ //if element in the list are individual function then get their info and show as legend
          DiscretizedFuncAPI func = (DiscretizedFuncAPI)funcList.get(i);
          String functionInfo = func.getInfo();
          String name = func.getName();

          legend = new String("DATASET #"+(i+1)+"\n\n"+
                              name+"  "+SystemPropertiesUtils.getSystemLineSeparator()+
                              functionInfo+SystemPropertiesUtils.getSystemLineSeparator());
          StyleConstants.setForeground(setLegend,plotCharacterstics[j].getCurveColor());
          doc.insertString(doc.getLength(),legend,setLegend);
          ++j;
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

    //setting the info in the
    pointsTextArea.setText(this.showDataInWindow(funcList,xAxisName,yAxisName));
    return ;


  }

  /**
   * For each function in the list it sets the plotting characeterstics of the curve
   * so that when that list is given to JFreechart , it creates it with these characterstics.
   * @param lineType
   * @param color
   * @param curveWidth
   * @param functionIndex
   */
  private void drawCurvesUsingPlottingFeatures(String lineType,Color color,
      double curveWidth,int functionIndex){
    //Solid Line
    if(lineType.equals(PlotColorAndLineTypeSelectorControlPanel.SOLID_LINE)){
      StandardXYItemRenderer SOLID_LINE_RENDERER = new StandardXYItemRenderer(
          org.jfree.chart.renderer.StandardXYItemRenderer.LINES,
          new StandardXYToolTipGenerator()
          );
      if(functionIndex==0)
        plot.setRenderer(SOLID_LINE_RENDERER);
      else
        plot.setSecondaryRenderer(functionIndex-1,SOLID_LINE_RENDERER);
      SOLID_LINE_RENDERER.setStroke(new BasicStroke((float)curveWidth));
      SOLID_LINE_RENDERER.setPaint(color);
    }
    //Dashed Line
    else if(lineType.equals(PlotColorAndLineTypeSelectorControlPanel.DASHED_LINE)){
      StandardXYItemRenderer DASHED_LINE_RENDERER = new StandardXYItemRenderer(
          org.jfree.chart.renderer.StandardXYItemRenderer.LINES,
          new StandardXYToolTipGenerator()
          );
      if(functionIndex==0)
        plot.setRenderer(DASHED_LINE_RENDERER);
      else
        plot.setSecondaryRenderer(functionIndex-1,DASHED_LINE_RENDERER);

      DASHED_LINE_RENDERER.setStroke(new BasicStroke((float)curveWidth,BasicStroke.CAP_BUTT
          ,BasicStroke.JOIN_BEVEL,0,new float[] {9},0));
      DASHED_LINE_RENDERER.setPaint(color);
    }
    //Dotted Line
   else if(lineType.equals(PlotColorAndLineTypeSelectorControlPanel.DOTTED_LINE)){
     StandardXYItemRenderer DOTTED_LINE_RENDERER = new StandardXYItemRenderer(
         org.jfree.chart.renderer.StandardXYItemRenderer.LINES,
         new StandardXYToolTipGenerator()
         );
     if(functionIndex==0)
       plot.setRenderer(DOTTED_LINE_RENDERER);
     else
       plot.setSecondaryRenderer(functionIndex-1,DOTTED_LINE_RENDERER);
     DOTTED_LINE_RENDERER.setStroke(new BasicStroke((float)curveWidth,BasicStroke.CAP_BUTT
         ,BasicStroke.JOIN_BEVEL,0,new float[] {1},0));
     DOTTED_LINE_RENDERER.setPaint(color);
    }
    //Dash and Dotted Line
    else if(lineType.equals(PlotColorAndLineTypeSelectorControlPanel.DOT_DASH_LINE)){
      StandardXYItemRenderer DASH_DOTTED_LINE_RENDERER = new StandardXYItemRenderer(
          org.jfree.chart.renderer.StandardXYItemRenderer.LINES,
          new StandardXYToolTipGenerator()
          );
      if(functionIndex==0)
        plot.setRenderer(DASH_DOTTED_LINE_RENDERER);
      else
        plot.setSecondaryRenderer(functionIndex-1,DASH_DOTTED_LINE_RENDERER);
      DASH_DOTTED_LINE_RENDERER.setStroke(new BasicStroke((float)curveWidth,BasicStroke.CAP_BUTT
          ,BasicStroke.JOIN_BEVEL,0,new float[] {5,3,2,3},0));
      DASH_DOTTED_LINE_RENDERER.setPaint(color);
    }
    //Filled Circle
    else if(lineType.equals(PlotColorAndLineTypeSelectorControlPanel.FILLED_CIRCLES)){
      StandardXYItemRenderer FILLED_CIRCLES_SHAPE_RENDERER = new StandardXYItemRenderer(
          org.jfree.chart.renderer.StandardXYItemRenderer.SHAPES,
          new StandardXYToolTipGenerator()
          );
      FILLED_CIRCLES_SHAPE_RENDERER.setShape(new Ellipse2D.Double(-DELTA-curveWidth/2,
          -DELTA-curveWidth/2, SIZE+curveWidth, SIZE+curveWidth));
      if(functionIndex==0)
        plot.setRenderer(FILLED_CIRCLES_SHAPE_RENDERER);
      else
        plot.setSecondaryRenderer(functionIndex-1,FILLED_CIRCLES_SHAPE_RENDERER);
      //FILLED_CIRCLES_SHAPE_RENDERER.setStroke(new BasicStroke((float)curveWidth));
      FILLED_CIRCLES_SHAPE_RENDERER.setPaint(color);
    }
    //Circle
    else if(lineType.equals(PlotColorAndLineTypeSelectorControlPanel.CIRCLES)){
      StandardXYItemRenderer CIRCLES_SHAPE_RENDERER = new StandardXYItemRenderer(
          org.jfree.chart.renderer.StandardXYItemRenderer.SHAPES,
          new StandardXYToolTipGenerator()
          );
      CIRCLES_SHAPE_RENDERER.setShape(new Ellipse2D.Double(-DELTA-curveWidth/2,
          -DELTA-curveWidth/2, SIZE+curveWidth, SIZE+curveWidth));
      CIRCLES_SHAPE_RENDERER.setShapesFilled(false);
      if(functionIndex==0)
        plot.setRenderer(CIRCLES_SHAPE_RENDERER);
      else
        plot.setSecondaryRenderer(functionIndex-1,CIRCLES_SHAPE_RENDERER);
      //CIRCLES_SHAPE_RENDERER.setStroke(new BasicStroke((float)curveWidth));
      CIRCLES_SHAPE_RENDERER.setPaint(color);
    }
    //Filled Triangles
    else if(lineType.equals(PlotColorAndLineTypeSelectorControlPanel.FILLED_TRIANGLES)){
      StandardXYItemRenderer FILLED_TRIANGLES_SHAPE_RENDERER = new StandardXYItemRenderer(
          org.jfree.chart.renderer.StandardXYItemRenderer.SHAPES,
          new StandardXYToolTipGenerator()
          );
      FILLED_TRIANGLES_SHAPE_RENDERER.setShape(ShapeUtils.createUpTriangle((float)curveWidth));
      if(functionIndex==0)
        plot.setRenderer(FILLED_TRIANGLES_SHAPE_RENDERER);
      else
        plot.setSecondaryRenderer(functionIndex-1,FILLED_TRIANGLES_SHAPE_RENDERER);
      //UP_TRIANGLE_SHAPE_RENDERER.setSeriesShape(datasetIndex
      //FILLED_TRIANGLES_SHAPE_RENDERER.setStroke(new BasicStroke((float)curveWidth));
      FILLED_TRIANGLES_SHAPE_RENDERER.setPaint(color);
    }
    //Triangles
    else if(lineType.equals(PlotColorAndLineTypeSelectorControlPanel.TRIANGLES)){
      StandardXYItemRenderer TRIANGLES_SHAPE_RENDERER = new StandardXYItemRenderer(
          org.jfree.chart.renderer.StandardXYItemRenderer.SHAPES,
          new StandardXYToolTipGenerator()
          );
      TRIANGLES_SHAPE_RENDERER.setShape(ShapeUtils.createUpTriangle((float)curveWidth));
      TRIANGLES_SHAPE_RENDERER.setShapesFilled(false);
      if(functionIndex==0)
        plot.setRenderer(TRIANGLES_SHAPE_RENDERER);
      else
        plot.setSecondaryRenderer(functionIndex-1,TRIANGLES_SHAPE_RENDERER);
      //UP_TRIANGLE_SHAPE_RENDERER.setSeriesShape(datasetIndex
      //TRIANGLES_SHAPE_RENDERER.setStroke(new BasicStroke((float)curveWidth));
      TRIANGLES_SHAPE_RENDERER.setPaint(color);
    }
    //Filled Inv Triangles
    else if(lineType.equals(PlotColorAndLineTypeSelectorControlPanel.FILLED_INV_TRIANGLES)){
      StandardXYItemRenderer FILLED_INV_TRIANGLES_SHAPE_RENDERER = new StandardXYItemRenderer(
          org.jfree.chart.renderer.StandardXYItemRenderer.SHAPES,
          new StandardXYToolTipGenerator()
          );
      FILLED_INV_TRIANGLES_SHAPE_RENDERER.setShape(ShapeUtils.createDownTriangle((float)curveWidth));
      if(functionIndex==0)
        plot.setRenderer(FILLED_INV_TRIANGLES_SHAPE_RENDERER);
      else
        plot.setSecondaryRenderer(functionIndex-1,FILLED_INV_TRIANGLES_SHAPE_RENDERER);
      //FILLED_INV_TRIANGLES_SHAPE_RENDERER.setStroke(new BasicStroke((float)curveWidth));
      FILLED_INV_TRIANGLES_SHAPE_RENDERER.setPaint(color);
    }
    //Inverted Triangles
    else if(lineType.equals(PlotColorAndLineTypeSelectorControlPanel.INV_TRIANGLES)){
      StandardXYItemRenderer INV_TRIANGLES_SHAPE_RENDERER = new StandardXYItemRenderer(
          org.jfree.chart.renderer.StandardXYItemRenderer.SHAPES,
          new StandardXYToolTipGenerator()
          );
      INV_TRIANGLES_SHAPE_RENDERER.setShape(ShapeUtils.createDownTriangle((float)curveWidth));
      INV_TRIANGLES_SHAPE_RENDERER.setShapesFilled(false);
      if(functionIndex==0)
        plot.setRenderer(INV_TRIANGLES_SHAPE_RENDERER);
      else
        plot.setSecondaryRenderer(functionIndex-1,INV_TRIANGLES_SHAPE_RENDERER);
      //INV_TRIANGLES_SHAPE_RENDERER.setStroke(new BasicStroke((float)curveWidth));
      INV_TRIANGLES_SHAPE_RENDERER.setPaint(color);
    }
    //Filled Diamonds
    else if(lineType.equals(PlotColorAndLineTypeSelectorControlPanel.FILLED_DIAMONDS)){
      StandardXYItemRenderer FILLED_DIAMONDS_SHAPE_RENDERER = new StandardXYItemRenderer(
          org.jfree.chart.renderer.StandardXYItemRenderer.SHAPES,
          new StandardXYToolTipGenerator()
          );
      FILLED_DIAMONDS_SHAPE_RENDERER.setShape(ShapeUtils.createDiamond((float)curveWidth));
      if(functionIndex==0)
        plot.setRenderer(FILLED_DIAMONDS_SHAPE_RENDERER);
      else
        plot.setSecondaryRenderer(functionIndex-1,FILLED_DIAMONDS_SHAPE_RENDERER);
      //FILLED_DIAMONDS_SHAPE_RENDERER.setStroke(new BasicStroke((float)curveWidth));
      FILLED_DIAMONDS_SHAPE_RENDERER.setPaint(color);
    }
    //Diamonds
    else if(lineType.equals(PlotColorAndLineTypeSelectorControlPanel.DIAMONDS)){
      StandardXYItemRenderer DIAMONDS_SHAPE_RENDERER = new StandardXYItemRenderer(
          org.jfree.chart.renderer.StandardXYItemRenderer.SHAPES,
          new StandardXYToolTipGenerator()
          );
      DIAMONDS_SHAPE_RENDERER.setShape(ShapeUtils.createDiamond((float)curveWidth));
      DIAMONDS_SHAPE_RENDERER.setShapesFilled(false);
      if(functionIndex==0)
        plot.setRenderer(DIAMONDS_SHAPE_RENDERER);
      else
        plot.setSecondaryRenderer(functionIndex-1,DIAMONDS_SHAPE_RENDERER);
      //DIAMONDS_SHAPE_RENDERER.setStroke(new BasicStroke((float)curveWidth));
      DIAMONDS_SHAPE_RENDERER.setPaint(color);
    }
    //Line and circle
    else if(lineType.equals(PlotColorAndLineTypeSelectorControlPanel.LINE_AND_CIRCLES)){
      StandardXYItemRenderer LINE_AND_CIRCLES_RENDERER = new StandardXYItemRenderer(
          org.jfree.chart.renderer.StandardXYItemRenderer.SHAPES_AND_LINES,
          new StandardXYToolTipGenerator()
          );
      LINE_AND_CIRCLES_RENDERER.setShape(new Ellipse2D.Double(-DELTA-(curveWidth*4)/2,
          -DELTA-(curveWidth*4)/2, SIZE+(curveWidth*4), SIZE+(curveWidth*4)));
      if(functionIndex==0)
        plot.setRenderer(LINE_AND_CIRCLES_RENDERER);
      else
        plot.setSecondaryRenderer(functionIndex-1,LINE_AND_CIRCLES_RENDERER);
      LINE_AND_CIRCLES_RENDERER.setStroke(new BasicStroke((float)curveWidth));
      LINE_AND_CIRCLES_RENDERER.setPaint(color);
    }
    //Line and Triangles
    else if(lineType.equals(PlotColorAndLineTypeSelectorControlPanel.LINE_AND_TRIANGLES)){
      StandardXYItemRenderer LINE_AND_TRIANGLES_RENDERER = new StandardXYItemRenderer(
          org.jfree.chart.renderer.StandardXYItemRenderer.SHAPES_AND_LINES,
          new StandardXYToolTipGenerator()
          );
      LINE_AND_TRIANGLES_RENDERER.setShape(ShapeUtils.createUpTriangle((float)(curveWidth*4)));
      if(functionIndex==0)
        plot.setRenderer(LINE_AND_TRIANGLES_RENDERER);
      else
        plot.setSecondaryRenderer(functionIndex-1,LINE_AND_TRIANGLES_RENDERER);
      LINE_AND_TRIANGLES_RENDERER.setStroke(new BasicStroke((float)curveWidth));
      LINE_AND_TRIANGLES_RENDERER.setPaint(color);
    }
    //X symbols
    else if(lineType.equals(PlotColorAndLineTypeSelectorControlPanel.X)){
      StandardXYItemRenderer X_SHAPE_RENDERER = new StandardXYItemRenderer(
          org.jfree.chart.renderer.StandardXYItemRenderer.SHAPES,
          new StandardXYToolTipGenerator()
          );
      X_SHAPE_RENDERER.setShape(ShapeUtils.createDiagonalCross((float)curveWidth,0.1f));
      X_SHAPE_RENDERER.setShapesFilled(false);
      if(functionIndex==0)
        plot.setRenderer(X_SHAPE_RENDERER);
      else
        plot.setSecondaryRenderer(functionIndex-1,X_SHAPE_RENDERER);
      //X_SHAPE_RENDERER.setStroke(new BasicStroke((float)curveWidth));
      X_SHAPE_RENDERER.setPaint(color);
    }
    //+ symbols
    else if(lineType.equals(PlotColorAndLineTypeSelectorControlPanel.CROSS_SYMBOLS)){
      StandardXYItemRenderer X_SHAPE_RENDERER = new StandardXYItemRenderer(
          org.jfree.chart.renderer.StandardXYItemRenderer.SHAPES,
          new StandardXYToolTipGenerator()
          );
      X_SHAPE_RENDERER.setShape(ShapeUtils.createRegularCross((float)curveWidth,0.1f));
      X_SHAPE_RENDERER.setShapesFilled(false);
      if(functionIndex==0)
        plot.setRenderer(X_SHAPE_RENDERER);
      else
        plot.setSecondaryRenderer(functionIndex-1,X_SHAPE_RENDERER);
      //X_SHAPE_RENDERER.setStroke(new BasicStroke((float)curveWidth));
      X_SHAPE_RENDERER.setPaint(color);
    }
    //squares
    else if(lineType.equals(PlotColorAndLineTypeSelectorControlPanel.SQUARES)){
      StandardXYItemRenderer SQUARE_SHAPE_RENDERER = new StandardXYItemRenderer(
          org.jfree.chart.renderer.StandardXYItemRenderer.SHAPES,
          new StandardXYToolTipGenerator()
          );
      SQUARE_SHAPE_RENDERER.setShape(new Rectangle.Double(-DELTA-curveWidth/2,
          -DELTA-curveWidth/2, SIZE+curveWidth, SIZE+curveWidth));
      SQUARE_SHAPE_RENDERER.setShapesFilled(false);
      if(functionIndex==0)
        plot.setRenderer(SQUARE_SHAPE_RENDERER);
      else
        plot.setSecondaryRenderer(functionIndex-1,SQUARE_SHAPE_RENDERER);
      //SQUARE_SHAPE_RENDERER.setStroke(new BasicStroke((float)curveWidth));
      SQUARE_SHAPE_RENDERER.setPaint(color);
    }
    //filled squares
    else if(lineType.equals(PlotColorAndLineTypeSelectorControlPanel.FILLED_SQUARES)){
      StandardXYItemRenderer FILLED_SQUARE_SHAPE_RENDERER = new StandardXYItemRenderer(
          org.jfree.chart.renderer.StandardXYItemRenderer.SHAPES,
          new StandardXYToolTipGenerator()
          );
      FILLED_SQUARE_SHAPE_RENDERER.setShape(new Rectangle.Double(-DELTA-curveWidth/2,
          -DELTA-curveWidth/2, SIZE+curveWidth, SIZE+curveWidth));
      if(functionIndex==0)
        plot.setRenderer(FILLED_SQUARE_SHAPE_RENDERER);
      else
        plot.setSecondaryRenderer(functionIndex-1,FILLED_SQUARE_SHAPE_RENDERER);
      //FILLED_SQUARE_SHAPE_RENDERER.setStroke(new BasicStroke((float)curveWidth));
      FILLED_SQUARE_SHAPE_RENDERER.setPaint(color);
    }
  }


  /**
   * Draws the graph panel
   */
  public void drawGraphPanel(String xAxisName, String yAxisName,ArrayList funcList,
                             boolean xLog,boolean yLog,boolean customAxis, String title,
                             ButtonControlPanel buttonControlPanel ) {

    // Starting
    String S = "drawGraphPanel(): ";

    ArrayList plottingFeatures = new ArrayList();

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
      yLog = false;
      buttonControlPanel.setYLog(yLog);
      yAxis = yAxis1;
      logErrorFlag = false;
    }
    int type = org.jfree.chart.renderer.StandardXYItemRenderer.LINES;

   StandardXYItemRenderer LINE_RENDERER = new StandardXYItemRenderer(
            org.jfree.chart.renderer.StandardXYItemRenderer.LINES,
            new StandardXYToolTipGenerator()
            );


    // build the plot
    plot = new XYPlot(data,xAxis, yAxis, LINE_RENDERER);

    //setting the plot properties
    plot.setDomainCrosshairLockedOnData(false);
    plot.setDomainCrosshairVisible(false);
    plot.setRangeCrosshairLockedOnData(false);
    plot.setRangeCrosshairVisible(false);
    plot.setInsets(new Insets(10, 0, 0, 20));

    float lineWidth = ((BasicStroke)LINE_RENDERER.getBaseStroke()).getLineWidth();
    String plotType = PlotColorAndLineTypeSelectorControlPanel.SOLID_LINE;

    int numSeries = totalProbFuncs.size();
    for(int i=0; i < numSeries; ++i) LINE_RENDERER.setSeriesPaint(i,legendPaint[i]);

    plot.setRenderer( LINE_RENDERER );
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
      //total number of elements in the list containing individual functions and
      //weighted function list.
      int totalNumofFunctions = funcList.size();
      //getting the metadata associated with each function in the list
      for(int i=0,j=0;i<totalNumofFunctions;++i){
        String legend=null;
        //setting the font style for the legend
        setLegend =new SimpleAttributeSet();
        StyleConstants.setFontSize(setLegend,12);
        //checking if element in the list is weighted function list object
        Object obj = funcList.get(i);
        String datasetName = "DATASET #"+(i+1);
        if(obj instanceof WeightedFuncListforPlotting){
          //getting the metadata for weighted functionlist
          WeightedFuncListforPlotting weightedList = (WeightedFuncListforPlotting)obj;

          String listInfo = weightedList.getInfo();

          legend = new String(datasetName+"\n"+
                              listInfo+SystemPropertiesUtils.getSystemLineSeparator());
          StyleConstants.setForeground(setLegend,Color.black);
          doc.insertString(doc.getLength(),legend,setLegend);
          //checking if individual curves need to be plotted
          if(weightedList.areIndividualCurvesToPlot()){
            PlotCurveCharacterstics plotChar = new PlotCurveCharacterstics(datasetName+" Curves",
                plotType,legendColor[j],lineWidth,weightedList.getNumWeightedFunctions());
            plottingFeatures.add(plotChar);
            //getting the metadata for each individual curves and creating the legend string
            String listFunctionsInfo = weightedList.getFunctionTraceInfo();

            legend = new String(listFunctionsInfo+SystemPropertiesUtils.getSystemLineSeparator());

            StyleConstants.setForeground(setLegend,legendColor[j]);
            doc.insertString(doc.getLength(),legend,setLegend);
            j=j+weightedList.getNumWeightedFunctions();

          }
          //checking if fractiles need to be plotted
          if(weightedList.areFractilesToPlot()){
            PlotCurveCharacterstics plotChar = new PlotCurveCharacterstics(datasetName+" Fractiles",
                plotType,legendColor[j],lineWidth,weightedList.getNumFractileFunctions());
            plottingFeatures.add(plotChar);
             //getting the fractile info for the weighted function list and adding that to the legend
            String fractileListInfo = weightedList.getFractileInfo();

            legend = new String(fractileListInfo+SystemPropertiesUtils.getSystemLineSeparator());
            StyleConstants.setForeground(setLegend,legendColor[j]);
            doc.insertString(doc.getLength(),legend,setLegend);
            j = j+weightedList.getNumFractileFunctions();
          }
          //checking if mean fractile need to be plotted
          if(weightedList.isMeanToPlot()){
            PlotCurveCharacterstics plotChar = new PlotCurveCharacterstics(datasetName+" Mean",
                plotType,legendColor[j],lineWidth);
            plottingFeatures.add(plotChar);
            //getting the fractileinfo and showing it as legend
            String meanInfo = weightedList.getMeanFunctionInfo();

            legend = new String(meanInfo+SystemPropertiesUtils.getSystemLineSeparator());
            StyleConstants.setForeground(setLegend,legendColor[j]);
            doc.insertString(doc.getLength(),legend,setLegend);
            ++j;
           }
        }
        else{ //if element in the list are individual function then get their info and show as legend
          PlotCurveCharacterstics plotChar = new PlotCurveCharacterstics(datasetName,
              plotType,legendColor[j],lineWidth);
          plottingFeatures.add(plotChar);
          DiscretizedFuncAPI func = (DiscretizedFuncAPI)funcList.get(i);
          String functionInfo = func.getInfo();
          String name = func.getName();

          legend = new String(datasetName+"\n"+
                              name+"  "+SystemPropertiesUtils.getSystemLineSeparator()+
                              functionInfo+SystemPropertiesUtils.getSystemLineSeparator());
          StyleConstants.setForeground(setLegend,legendColor[j]);
          doc.insertString(doc.getLength(),legend,setLegend);
          ++j;
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

    //creating the plotting feature array
    int  numDiffPlots = plottingFeatures.size();
    curvePlottingCharacterstics = new PlotCurveCharacterstics[numDiffPlots];
    for(int i=0;i<numDiffPlots;++i)
      curvePlottingCharacterstics[i] =(PlotCurveCharacterstics)plottingFeatures.get(i);

    plottingFeatures =null;

    //setting the info in the
    pointsTextArea.setText(this.showDataInWindow(funcList,xAxisName,yAxisName));
    return ;
  }


  /**
   *
   * @param funcList
   * @param xAxisName
   * @param yAxisName
   * @returns data to be shown in the data window
   */
  private String showDataInWindow(ArrayList funcList,String xAxisName,String yAxisName){

    int size = funcList.size();

    StringBuffer b = new StringBuffer();
    b.append("\n");
    b.append("X-Axis: " + xAxisName + '\n');
    b.append("Y-Axis: " + yAxisName + '\n');
    b.append("Number of Data Sets: " + size + "\n\n");


    for(int i=0;i<size;++i){
      Object obj = funcList.get(i);

      if(!(obj instanceof WeightedFuncListforPlotting)){ //showing data for the individual function
        DiscretizedFuncAPI function = (DiscretizedFuncAPI)obj;
        b.append("\nDATASET #" + (i+1) + "\n\n");
        b.append(function.toString()+ '\n');
      }
      else{ //showing data for weighted function list
        WeightedFuncListforPlotting weightedList = (WeightedFuncListforPlotting)obj;
        b.append("\nDATASET #" + (i+1) + "   Weighted Function List"+'\n');
        b.append(weightedList.getInfo()+"\n\n");
        //checking if individual curves need to be plotted
        if(weightedList.areIndividualCurvesToPlot()){
          //getting the metadata for each individual curves and creating the legend string
          DiscretizedFuncList list = weightedList.getWeightedFunctionList();
          ArrayList wtList = weightedList.getRelativeWtList();
          int listSize = list.size();
          for(int j=0;j<listSize;++j){
            b.append("\nFunction #"+(j+1)+" of "+listSize+", from Dataset #"+(i+1)+
            ", with relative wt = "+(Double)wtList.get(j)+"\n");
            DiscretizedFuncAPI function = (DiscretizedFuncAPI)list.get(j);
            b.append(function.getMetadataString()+ '\n');
          }
        }
        //checking if fractiles need to be plotted
        if(weightedList.areFractilesToPlot()){

          //getting the fractile info for the weighted function list and adding that to the legend
          DiscretizedFuncList list = weightedList.getFractileList();
          ArrayList fractileValueList = weightedList.getFractileValuesList();
          int listSize = list.size();
          for(int j=0;j<listSize;++j){
            b.append("\n"+(Double)fractileValueList.get(j)+" Fractile for Dataset #"+(i+1)+"\n");
            DiscretizedFuncAPI function = (DiscretizedFuncAPI)list.get(j);
            b.append(function.getMetadataString()+ '\n');
          }
        }

        //checking if mean fractile need to be plotted
        if(weightedList.isMeanToPlot()){
          //getting the fractileinfo and showing it as legend
          b.append("\nMean for Dataset #"+(i+1)+"\n");
          b.append(weightedList.getMean().getMetadataString()+"\n");
        }
      }
    }

    return b.toString();
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
   *
   * @returns the Array of PlotCurveCharacterstics[] that contain the info about
   * plotting the curve like plot line color , its width and line type.
   */
  public PlotCurveCharacterstics[] getCurvePlottingCharactersticInfo(){
    return curvePlottingCharacterstics;
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
         //list.get(0).setInfo(weightedList.getInfo()+"\n"+"(a) "+list.getInfo());
         numColorArray.add(new Integer(list.size()));
         totalProbFuncs.addAll(list);
        }
        if(weightedList.areFractilesToPlot()){
          DiscretizedFuncList list= weightedList.getFractileList();
         // list.get(0).setInfo("(b) "+list.getInfo());
          totalProbFuncs.addAll(list);
          numColorArray.add(new Integer(list.size()));
        }
        if(weightedList.isMeanToPlot()){
          DiscretizedFuncAPI meanFunc = weightedList.getMean();
          //String info = meanFunc.getInfo();
          //meanFunc.setInfo("(c) "+info);
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

    //sets the num of datset with differnt color scheme.
    this.numDataset = numDiffColors;

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