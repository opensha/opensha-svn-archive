package org.scec.sha.gui.infoTools;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.text.*;

import org.jfree.chart.*;
import org.jfree.chart.axis.*;
import org.jfree.chart.renderer.*;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.data.*;

import org.scec.data.function.*;
import org.scec.gui.plot.jfreechart.*;
import org.scec.gui.*;
import org.scec.sha.gui.infoTools.ButtonControlPanel;
import org.scec.util.*;

/**
 * <p>Title: GraphPanel</p>
 * <p>Description: This class shows the JFreechart Panel in a window</p>
 * @author : Nitin Gupta
 * @version 1.0
 */

public class GraphPanel extends JPanel {



  // mesage needed in case of show data if plot is not available
  private final static String NO_PLOT_MSG = "No Plot Data Available";


  /*setting the colors for the different plots so that legends
  *can be shown with the same color
  */

  Color [] defaultColor = {Color.red,Color.blue,Color.green,Color.orange,Color.magenta,
    Color.cyan,Color.pink,Color.yellow,Color.lightGray,Color.gray,Color.darkGray};
  Color[] legendColor = null;
  Paint[] legendPaint = null;

  private SimpleAttributeSet setLegend;

  private BorderLayout borderLayout1 = new BorderLayout();
  private JSplitPane chartSplitPane = new JSplitPane();
  private JScrollPane metadataScrollPane = new JScrollPane();
  private JPanel chartPane = new JPanel();

  private Insets plotInsets = new Insets( 4, 4, 4, 4 );

  //graph chart panel
  private ChartPanel chartPanel;

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
    application = api;
    try {
      jbInit();
    }
    catch(Exception ex) {
      ex.printStackTrace();
    }
    //sets the default series color range for the curves
    setDefaultSeriesColor();
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
    chartSplitPane.setDividerLocation(500);

    // for showing the data on click of "show data" button
    pointsTextArea.setBorder( BorderFactory.createEtchedBorder() );
    pointsTextArea.setText( NO_PLOT_MSG );
    pointsTextArea.setLineWrap(true);
    dataScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    dataScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    dataScrollPane.setBorder( BorderFactory.createEtchedBorder() );
    dataScrollPane.getViewport().add( pointsTextArea, null );
  }


  public void drawGraphPanel(DiscretizedFuncList totalProbFuncs,DiscretizedFunctionXYDataSet  data,
                             boolean xLog,boolean yLog,boolean customAxis, String title,
                             ButtonControlPanel buttonControlPanel ) {

    // Starting
    String S = "drawGraphPanel(): ";

    String newXYAxisName = totalProbFuncs.getXYAxesName();

    // create a default chart based on some sample data...

    // Determine which IM to add to the axis labeling
    String xAxisLabel = totalProbFuncs.getXAxisName();
    String yAxisLabel = totalProbFuncs.getYAxisName();

    //flags to check if the exception was thrown on selection of the x-log or y-log.
    boolean logErrorFlag = false;


    //create the standard ticks so that smaller values too can plotted on the chart
    TickUnits units = MyTickUnits.createStandardTickUnits();

    try{

      /// check if x log is selected or not
      if(xLog) xAxis = new LogarithmicAxis(xAxisLabel);
      else xAxis = new NumberAxis( xAxisLabel );

      if (!xLog)
        xAxis.setAutoRangeIncludesZero(true);
      else
        xAxis.setAutoRangeIncludesZero( false );
      xAxis.setStandardTickUnits(units);
      xAxis.setTickMarksVisible(false);

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
      if(yLog) yAxis = new LogarithmicAxis(yAxisLabel);
      else yAxis = new NumberAxis( yAxisLabel );

      if (!yLog)
        yAxis.setAutoRangeIncludesZero(true);
      else
        yAxis.setAutoRangeIncludesZero( false );

      yAxis.setStandardTickUnits(units);
      yAxis.setTickMarksVisible(false);

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
    org.jfree.chart.plot.XYPlot plot = new org.jfree.chart.plot.XYPlot(data,
        xAxis, yAxis, renderer);

    //setting the plot properties
    plot.setDomainCrosshairLockedOnData(false);
    plot.setDomainCrosshairVisible(false);
    plot.setRangeCrosshairLockedOnData(false);
    plot.setRangeCrosshairVisible(false);
    plot.setInsets(new Insets(0, 0, 0, 20));

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
      for(int i=0,j=0;i<numOfColors;++i,++j){
          if(j==legendColor.length)
            j=0;

          String legend = new String(i+1+") "+":"+SystemPropertiesUtils.getSystemLineSeparator()+
                                     totalProbFuncs.get(i).getInfo()+SystemPropertiesUtils.getSystemLineSeparator());
          setLegend =new SimpleAttributeSet();
          StyleConstants.setFontSize(setLegend,12);
          StyleConstants.setForeground(setLegend,legendColor[j]);
          doc.insertString(doc.getLength(),legend,setLegend);
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

      buttonControlPanel.getToggleButton().setText( "Show Plot" );
      graphOn = false;

      chartPane.add(dataScrollPane,new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0
          , GridBagConstraints.CENTER, GridBagConstraints.BOTH, plotInsets, 0, 0 ) );
    }
    else {
      //showing the Plot window, if not null
      graphOn = true;

      buttonControlPanel.getToggleButton().setText( "Show Data" );
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
   * Sets the default color scheme for the cureves drawn
   */
  public void setDefaultSeriesColor(){
    legendPaint = defaultColor;
    legendColor = defaultColor;
  }

  /**
   *
   * @returns the series color being used for the current graph schemes
   */
  public Color[] getSeriesColor(){
   return legendColor;
  }

}