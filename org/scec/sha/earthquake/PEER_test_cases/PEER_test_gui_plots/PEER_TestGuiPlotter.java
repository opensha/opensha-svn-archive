package org.scec.sha.earthquake.PEER_test_cases.PEER_test_gui_plots;

import java.awt.*;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.awt.event.*;
import java.applet.*;
import java.io.IOException;
import javax.swing.*;
import javax.swing.border.*;
import java.util.Iterator;
import java.util.Vector;
import java.lang.reflect.*;
import java.io.*;

import com.jrefinery.chart.*;
import com.jrefinery.chart.tooltips.*;
import com.jrefinery.data.*;


import org.scec.data.function.*;
import org.scec.gui.*;
import org.scec.gui.plot.LogPlotAPI;
import org.scec.gui.plot.jfreechart.*;
import org.scec.gui.plot.*;
import org.scec.util.FileUtils;
import org.scec.param.*;
import org.scec.param.editor.*;
import org.scec.param.event.*;
import org.scec.data.*;
import org.scec.calc.*;


/**
 * <p>Title: PEER_Test_GuiPlotter</p>
 * <p>Description: This class provides the services of plotting PEER test cases
 * result from the differents files</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * @author : Nitin Gupta & Vipin Gupta
 * @created : Dec 06,2002
 * @version 1.0
 */

public class PEER_TestGuiPlotter extends JApplet implements
                                          NamedObjectAPI,
                                          ParameterChangeListener,LogPlotAPI,
                                          ActionListener{
  private boolean isStandalone = false;
  private JPanel mainPanel = new JPanel();
  private JSplitPane mainSplitPane = new JSplitPane();
  private JSplitPane plotSplitPane = new JSplitPane();

  private static final String C="PEER_Test_GuiPlotter";

  private static final boolean D= true;

  // default insets
  Insets defaultInsets = new Insets( 4, 4, 4, 4 );

  //log flags declaration
  boolean xLog =false;
  boolean yLog =false;

  //variables that determine the window size
  protected final static int W = 850;
  protected final static int H = 700;

  //Directory from which to search for all the PEER test files
  String DIR = "GroupTestDataFiles/";
  String FILE_EXTENSION=".dat";

  // mesage needed in case of show data if plot is not available
  final static String NO_PLOT_MSG = "No Plot Data Available";

  private Insets plotInsets = new Insets( 4, 10, 4, 4 );
  protected boolean graphOn = false;


  DiscretizedFuncList functions = new DiscretizedFuncList();
  DiscretizedFunctionXYDataSet data = new DiscretizedFunctionXYDataSet();


  // Create the x-axis and y-axis - either normal or log
  com.jrefinery.chart.NumberAxis xAxis = null;
  com.jrefinery.chart.NumberAxis yAxis = null;


  /**
   * adding scroll pane for showing data
   */
  JScrollPane dataScrollPane = new JScrollPane();

  // text area to show the data values
  JTextArea pointsTextArea = new JTextArea();

 /**
  * chart panel, that is plot window
  */
  ChartPanel chartPanel;

 /**
  * these four values save the custom axis scale specified by user
  */
  protected double minXValue;
  protected double maxXValue;
  protected double minYValue;
  protected double maxYValue;
  protected boolean customAxis = false;

  private final static String AUTO_SCALE = "Auto Scale";
  private final static String CUSTOM_SCALE = "Custom Scale";
  private final static String PEER_TEST_TITLE ="PEER Test Selection";


  // light blue color
  Color lightBlue = new Color( 200, 200, 230 );

  /**
   * for Y-log, 0 values will be converted to this small value
   */
  private double Y_MIN_VAL = 1e-16;

  //X and Y AXIS Labels
  String X_AXIS_TITLE = "Intensity Measure Level";
  String Y_AXIS_TITLE = "Probability";


  //static definition  of the strings
  private String TEST_CASE_FILES= new String("PEER Test Sets");

  //static definition PLOT Title
  private final static String TITLE = new String("PEER Test Plots");

  private JPanel buttonPanel = new JPanel();
  private JPanel plotPanel = new JPanel();
  private Border border1;
  private Border border2;
  private Border border3;
  private JButton toggleButton = new JButton();
  private JCheckBox xLogCheckBox = new JCheckBox();
  private JCheckBox yLogCheckBox = new JCheckBox();
  private JCheckBox BGColorCheckBox = new JCheckBox();
  private JLabel rangeLabel = new JLabel();
  private JComboBox rangeComboBox = new JComboBox();
  private Border border4;
  private Border border5;


  //  TestCases ParameterList & its editor
  private ParameterList testCasesParamList = new ParameterList();
  private ParameterListEditor testCasesEditor = null;

  //Vector that stores all the testCasesFiles for selected set of test cases
  Vector testCasesFiles = new Vector();

  // reads the full file names of all the stored files in the directory
  Vector testFiles = new Vector();

  //Vector to store all the checkboxes for the selected test file
  Vector checkBoxVector = new Vector();

  /*setting the colors for the different plots so that legends checkboxes
   *can be shown with the same color
   */
  Color[] legendColor ={Color.red,Color.blue,Color.green,Color.orange,Color.magenta,
                       Color.cyan,Color.pink,Color.yellow,Color.gray,new Color(137,114,40),
                       new Color(137,199,205),new Color(255,199,205),new Color(255,228,188),
                       new Color(125,108,142),new Color(125,108,23), new Color(255,182,23),
                       new Color(255,182,255)};
  Paint[] legendPaint;

  // search path needed for making editors
  private String[] searchPaths;
  private JScrollPane jScrollPane1 = new JScrollPane();
  private JPanel testCasesPanel = new JPanel();
  private GridBagLayout gridBagLayout1 = new GridBagLayout();
  private GridBagLayout gridBagLayout2 = new GridBagLayout();
  private JCheckBox averageCheck = new JCheckBox();
  private BorderLayout borderLayout1 = new BorderLayout();
  private BorderLayout borderLayout2 = new BorderLayout();
  private GridBagLayout gridBagLayout3 = new GridBagLayout();

  //Construct the applet
  public PEER_TestGuiPlotter() {
    searchPaths = new String[3];
    searchPaths[0] = ParameterListEditor.getDefaultSearchPath();
    // for Y-log, convert 0 values in Y axis to this small value
    data.setConvertZeroToMin(true,Y_MIN_VAL);
    data.setFunctions(functions);
    searchTestFiles();
  }

  //Initialize the applet
  public void init() {
    try {
      jbInit();
    }
    catch(Exception e) {
      e.printStackTrace();
    }
  }
  //Component initialization
  private void jbInit() throws Exception {
    border1 = new EtchedBorder(EtchedBorder.RAISED,Color.white,new Color(178, 178, 178));
    border2 = new EtchedBorder(EtchedBorder.RAISED,Color.white,new Color(178, 178, 178));
    border3 = new EtchedBorder(EtchedBorder.RAISED,Color.white,new Color(178, 178, 178));
    border4 = new EtchedBorder(EtchedBorder.RAISED,Color.white,new Color(178, 178, 178));
    border5 = new EtchedBorder(EtchedBorder.RAISED,Color.white,new Color(178, 178, 178));
    this.setSize(new Dimension(749, 614));
    this.getContentPane().setLayout(borderLayout2);
    mainPanel.setBorder(BorderFactory.createEtchedBorder());
    mainPanel.setLayout(borderLayout1);
    mainSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
    mainSplitPane.setDividerSize(5);
    buttonPanel.setLayout(gridBagLayout3);
    plotPanel.setLayout(gridBagLayout2);
    plotSplitPane.setDividerSize(5);
    plotSplitPane.setLastDividerLocation(500);
    buttonPanel.setBorder(border1);
    plotPanel.setBorder(border3);
    // for showing the data on click of "show data" button
    pointsTextArea.setBorder( BorderFactory.createEtchedBorder() );
    pointsTextArea.setText( NO_PLOT_MSG );
    dataScrollPane.setBorder( BorderFactory.createEtchedBorder() );

    xLogCheckBox.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        xLogCheckBox_actionPerformed(e);
      }
    });
    yLogCheckBox.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        yLogCheckBox_actionPerformed(e);
      }
    });
    toggleButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        toggleButton_actionPerformed(e);
      }
    });
    averageCheck.setText("Average");
    averageCheck.setBounds(new Rectangle(9, 12, 90, 25));
    dataScrollPane.getViewport().add( pointsTextArea, null );
    toggleButton.setText("Show Data");
    xLogCheckBox.setText("XLog");
    yLogCheckBox.setText("YLog");
    BGColorCheckBox.setText("BlackBG");
    rangeLabel.setText("SetAxisRange:");
    rangeComboBox.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        rangeComboBox_actionPerformed(e);
      }
    });
    this.getContentPane().add(mainPanel, BorderLayout.CENTER);
    mainPanel.add(mainSplitPane, BorderLayout.CENTER);
    plotSplitPane.add(plotPanel, JSplitPane.LEFT);
    plotSplitPane.add(jScrollPane1, JSplitPane.RIGHT);
    jScrollPane1.getViewport().add(testCasesPanel, null);
    mainSplitPane.add(plotSplitPane, JSplitPane.TOP);
    buttonPanel.add(averageCheck,  new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(7, 7, 15, 0), 14, -4));
    buttonPanel.add(rangeLabel,  new GridBagConstraints(5, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(7, 0, 15, 107), 22, 6));
    buttonPanel.add(rangeComboBox,  new GridBagConstraints(5, 0, 1, 1, 1.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(7, 102, 15, 19), -32, 1));
    buttonPanel.add(BGColorCheckBox,  new GridBagConstraints(4, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(7, 10, 15, 0), 17, -8));
    buttonPanel.add(yLogCheckBox,  new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(7, 8, 15, 0), 14, -8));
    buttonPanel.add(xLogCheckBox,  new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(7, 21, 15, 0), 21, -8));
    buttonPanel.add(toggleButton,  new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(7, 22, 15, 0), 26, -8));
    mainSplitPane.add(buttonPanel, JSplitPane.BOTTOM);
    mainSplitPane.setDividerLocation(540);
    plotSplitPane.setDividerLocation(575);
    //shows the selection for the different Test Cases files
    initTestParamList();
    rangeComboBox.addItem(new String(AUTO_SCALE));
    rangeComboBox.addItem(new String(CUSTOM_SCALE));
    averageCheck.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        averageCheck_actionPerformed(e);
      }
    });

  }
  //Start the applet
  public void start() {
  }
  //Stop the applet
  public void stop() {
  }
  //Destroy the applet
  public void destroy() {
  }
  //Get Applet information
  public String getAppletInfo() {
    return C;
  }


  /**
   * This function initiates test cases and files for those test cases after searching
   * in a common directory.
   */
  void initTestParamList(){

    StringParameter availableTestFiles= new StringParameter(TEST_CASE_FILES,
        testCasesFiles,(String)testCasesFiles.get(0));

    availableTestFiles.addParameterChangeListener(this);
    testCasesParamList.addParameter(availableTestFiles);
    testCasesEditor = new ParameterListEditor( testCasesParamList, searchPaths);
    testCasesEditor.setTitle(PEER_TEST_TITLE);
    testCasesPanel.setLayout(gridBagLayout1);
    testCasesPanel.add(testCasesEditor, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 46, 456, 46), 0, 0));
    testCasesPanel.validate();
    testCasesPanel.repaint();

    addButton();

  }


  //Main method
  public static void main(String[] args) {
    PEER_TestGuiPlotter applet = new PEER_TestGuiPlotter();
    applet.isStandalone = true;
    JFrame frame = new JFrame();
    //EXIT_ON_CLOSE == 3
    frame.setDefaultCloseOperation(3);
    frame.setTitle("PEER Tests Plots");
    frame.getContentPane().add(applet, BorderLayout.CENTER);
    applet.init();
    applet.start();
    frame.setSize(W,H);
    Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
    frame.setLocation((d.width - frame.getSize().width) / 2, (d.height - frame.getSize().height) / 2);
    frame.setVisible(true);
  }

  //static initializer for setting look & feel
  static {
    try {
      //UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      //UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
    }
    catch(Exception e) {
    }
  }

  /**
   * Reads the selected test case file
   * @param fileName
   */
  void readFile(String fileName){
    ArbitrarilyDiscretizedFunc function= new ArbitrarilyDiscretizedFunc();
    try{
    System.out.println("file Reading:"+fileName);
    Iterator it = FileUtils.loadInCharFile(fileName).iterator();
    while(it.hasNext()){
      StringTokenizer st= new StringTokenizer(it.next().toString());
      while(st.hasMoreTokens())
        function.set(Double.parseDouble(st.nextToken()),Double.parseDouble(st.nextToken()));
        function.setName(fileName);
        function.setInfo(fileName);
    }

    functions.add(function);
    }catch(IOException e) {
      e.printStackTrace();
    }
  }


 /**removes all the components from the parameter paenl, and adds them in fresh
  * Based on the selected Test Case checkboxes are added to the panel
  **/

  public void addButton(){
    int colorIndex=0;
    int displayIndex =0;
    //checking which test case is selected
    String testSet= testCasesParamList.getParameter(TEST_CASE_FILES).getValue().toString();
    testCasesPanel.removeAll();
    testCasesPanel.add(testCasesEditor,
                    new GridBagConstraints( 0, displayIndex, 1, 1, 1.0, 1.0,
                    GridBagConstraints.NORTH,
                    GridBagConstraints.HORIZONTAL, defaultInsets, 0, 0 ));
    ++displayIndex;
    //gets the iterator for the files, so that we can extract the string as the test for the check boxes
    //for the selected test case
    Iterator it=testFiles.iterator();
    checkBoxVector= new Vector();
    while(it.hasNext()) {
      String tempName=it.next().toString();
      int index=tempName.indexOf("_");
      String tempStr=tempName.substring(0,index);
      if(tempStr.equalsIgnoreCase(testSet)){
        int i= tempName.indexOf(".");
        String testSubmitterName = tempName.substring(index+1,i);

        //adding the checkBoxes for the selceted test case
        JCheckBox testFilesCheck= new JCheckBox(testSubmitterName);
        checkBoxVector.add(testFilesCheck);
        testFilesCheck.setSelected(true);
        testFilesCheck.addActionListener(this);
        testCasesPanel.add(testFilesCheck,
                           new GridBagConstraints( 0, displayIndex, 1, 1, 1.0, 1.0,
                           GridBagConstraints.NORTH,
                           GridBagConstraints.HORIZONTAL, defaultInsets, 0, 0 ));
        ++displayIndex;
      }
    }

    int count = checkBoxVector.size();
    int numColor = legendColor.length;
    for(int j=0;j<count;j++) {
      JCheckBox checkBox=(JCheckBox)checkBoxVector.get(j);
      checkBox.setForeground(legendColor[j%numColor]);
    }
    averageCheck.setForeground(legendColor[count%numColor]);
    averageCheck.setSelected(true);
    testCasesPanel.validate();
    testCasesPanel.repaint();
    addGraphPanel();
  }


  /**
   *  Adds a feature to the GraphPanel attribute of the EqkForecastApplet object
   */
  protected void addGraphPanel() {

      // Starting
      String S = C + ": addGraphPanel(): ";

      // clearing all the plots before adding them to the functionList
      clearPlot(true);


      // Determine which IM to add to the axis labeling
      String xAxisLabel = X_AXIS_TITLE;
      String yAxisLabel = Y_AXIS_TITLE;


      //create the standard ticks so that smaller values too can plotted on the chart
      TickUnits units = MyTickUnits.createStandardTickUnits();

      //checking which of the checkBoxes are selected
      int size= this.checkBoxVector.size();
      legendPaint= new Paint[size+1];

      //checking which test case is selected at the time of adding of the plot
      String testSelected = testCasesParamList.getParameter(TEST_CASE_FILES).getValue().toString();
      for(int i=0,k=0;i<size;i++){

        JCheckBox checkBox=(JCheckBox)checkBoxVector.get(i);
        //checking if the check box is selected for the selected test case
        if(checkBox.isSelected()) {
          legendPaint[k]=checkBox.getForeground();
          ++k;
          String checkText =checkBox.getText();
          Iterator it= testFiles.iterator();

          //getting the correct filename to read the data from
          while(it.hasNext())
          {
          String fileName=testSelected +"_"+checkText;
          String fname=(String)it.next();
          String tempString = fname.substring(0,fname.indexOf("."));
          if(fileName.equalsIgnoreCase(tempString))
            readFile(DIR+fname);
          }
        }
      }

      //adding the color in the legendPaint for the Average checkBox
      legendPaint[size]=averageCheck.getForeground();


      //see if the average checkBox is selected to calculate the average for all plotted prob's
      if(averageCheck.isSelected() && functions.size()>0)
        calcAvgFunc();
      else
        averageCheck.setSelected(false);

      /// check if x log is selected or not
      if(xLog) xAxis = new HorizontalLogarithmicAxis(xAxisLabel);
      else xAxis = new HorizontalNumberAxis( xAxisLabel );

      xAxis.setAutoRangeIncludesZero( false );
      xAxis.setCrosshairLockedOnData( false );
      xAxis.setCrosshairVisible(false);
      xAxis.setStandardTickUnits(units);

      /// check if y log is selected or not
      if(yLog) yAxis = new VerticalLogarithmicAxis(yAxisLabel);
      else yAxis = new VerticalNumberAxis( yAxisLabel );

      yAxis.setAutoRangeIncludesZero( false );
      yAxis.setCrosshairLockedOnData( false );
      yAxis.setCrosshairVisible( false);
      yAxis.setStandardTickUnits(units);

      int type = com.jrefinery.chart.StandardXYItemRenderer.LINES;


      LogXYItemRenderer renderer = new LogXYItemRenderer( type, new StandardXYToolTipGenerator() );


      /* to set the range of the axis on the input from the user if the range combo box is selected*/
      if(this.customAxis) {
          xAxis.setRange(this.minXValue,this.maxXValue);
          yAxis.setRange(this.minYValue,this.maxYValue);
        }

      // build the plot
      org.scec.gui.PSHALogXYPlot plot
          = new org.scec.gui.PSHALogXYPlot(this,data,
                                       xAxis, yAxis, xLog, yLog);


      plot.setBackgroundAlpha( .8f );

      plot.setSeriesPaint(legendPaint);

      plot.setRenderer( renderer );


      JFreeChart chart = new JFreeChart(TITLE, JFreeChart.DEFAULT_TITLE_FONT, plot, false );

      chart.setBackgroundPaint( lightBlue );

      // chart.setBackgroundImage(image);
      // chart.setBackgroundImageAlpha(.3f);

      // Put into a panel
      chartPanel = new ChartPanel(chart, true, true, true, true, false);
      //panel.setMouseZoomable(true);

      chartPanel.setBorder( BorderFactory.createEtchedBorder( EtchedBorder.LOWERED ) );
      chartPanel.setMouseZoomable(true);
      chartPanel.setDisplayToolTips(true);
      chartPanel.setHorizontalAxisTrace(false);
      chartPanel.setVerticalAxisTrace(false);
      graphOn=false;
      pointsTextArea.setText("TestCase:"+testSelected+"\n" +" X Axis:"+ X_AXIS_TITLE + "\n" +
                                 "Y Axis:" + Y_AXIS_TITLE +"\n" +
                                  functions.toString());
      togglePlot();
   }


   /**
    *  Description of the Method
    */
   protected void togglePlot() {

     // Starting
     String S = C + ": togglePlot(): ";
     plotPanel.removeAll();
     if ( graphOn ) {

       toggleButton.setText( "Show Plot" );
       graphOn = false;

       plotPanel.add( dataScrollPane, new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0
           , GridBagConstraints.CENTER, GridBagConstraints.BOTH, plotInsets, 0, 0 ) );
     }
     else {
       graphOn = true;
       // dataScrollPane.setVisible(false);
       this.toggleButton.setText( "Show Data" );
       // panel added here
       if(chartPanel !=null)
         plotPanel.add( chartPanel, new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0
             , GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 0, 0, 0, 0 ), 0, 0 ) );

       else
         // innerPlotPanel.setBorder(oval);
         plotPanel.add( dataScrollPane, new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0
         , GridBagConstraints.CENTER, GridBagConstraints.BOTH, plotInsets, 0, 0 ) );

     }


     validate();
     repaint();

     if ( D ) System.out.println( S + "Ending" );

   }


   /**
    * This function looks for all the test cases files within the directory
    * and stores their name in Vector
    */
  private  void searchTestFiles(){

    File thedir = new File(DIR);
    String[] files = thedir.list();
    boolean flag=true;
    int numFiles=0;
    for(int f=0;f<files.length;f++) {
      if(files[f].endsWith(FILE_EXTENSION))
        testFiles.add(files[f]);
      else
        continue;
      for(int i=0;i<testCasesFiles.size();++i)
        if(testCasesFiles.get(i).toString().equalsIgnoreCase(files[f])){
           flag=false;
           break;
        }
      if(flag){
        int index=files[f].indexOf("_");
        String testCases = files[f].substring(0,index);
        testCasesFiles.add(testCases);
      }
    }
  }


  /**
   *  This is the main function of this interface. Any time a control
   *  paramater or independent paramater is changed by the user in a GUI this
   *  function is called, and a paramater change event is passed in. This
   *  function then determines what to do with the information ie. show some
   *  paramaters, set some as invisible, basically control the paramater
   *  lists.
   *
   * @param  event
   */
  public void parameterChange( ParameterChangeEvent event ) {

      String S = C + ": parameterChange(): ";
      if ( D )
          System.out.println( "\n" + S + "starting: " );

      String name1 = event.getParameterName();
      if(name1.equalsIgnoreCase(TEST_CASE_FILES))
        addButton();
  }

  /**
  * whenever selection is made in the combo box
  * @param e
  */
  void rangeComboBox_actionPerformed(ActionEvent e) {

    String str=(String)rangeComboBox.getSelectedItem();
    if(str.equalsIgnoreCase(AUTO_SCALE)){
      customAxis=false;
      addGraphPanel();
    }
    if(str.equalsIgnoreCase(CUSTOM_SCALE))  {
       Range rX = xAxis.getRange();
       Range rY= yAxis.getRange();
       double minX=rX.getLowerBound();
       double maxX=rX.getUpperBound();
       double minY=rY.getLowerBound();
       double maxY=rY.getUpperBound();


       int xCenter=getAppletXAxisCenterCoor();
       int yCenter=getAppletYAxisCenterCoor();
       AxisScale axisScale=new AxisScale(this,minX,maxX,minY,maxY);
       axisScale.setBounds(xCenter-60,yCenter-50,375,148);
       axisScale.pack();
       axisScale.show();
    }
  }

  /**
   * sets the range for X-axis
   * @param xMin : minimum value for X-axis
   * @param xMax : maximum value for X-axis
   */
  public void setXRange(double xMin,double xMax) {
     minXValue=xMin;
     maxXValue=xMax;
     this.customAxis=true;

  }

  /**
   * sets the range for Y-axis
   * @param yMin : minimum value for Y-axis
   * @param yMax : maximum value for Y-axis
   */
  public void setYRange(double yMin,double yMax) {
     minYValue=yMin;
     maxYValue=yMax;
     this.customAxis=true;
     addGraphPanel();
  }

  /**
   * This function handles the Zero values in the X and Y data set when exception is thrown,
   * it reverts back to the linear scale displaying a message box to the user.
   */
  public void invalidLogPlot(String message) {

     int xCenter=getAppletXAxisCenterCoor();
     int yCenter=getAppletYAxisCenterCoor();
     if(message.equals("Log Value of the negative values and 0 does not exist for X-Log Plot")) {
       this.xLogCheckBox.setSelected(false);
       ShowMessage showMessage=new ShowMessage("      X-Log Plot Error as it contains Zero Values");
       showMessage.setBounds(xCenter-60,yCenter-50,370,145);
       showMessage.pack();
       showMessage.show();
     }
     if(message.equals("Log Value of the negative values and 0 does not exist for Y-Log Plot")) {
       this.yLogCheckBox.setSelected(false);
       ShowMessage showMessage=new ShowMessage("      Y-Log Plot Error as it contains Zero Values");
       showMessage.setBounds(xCenter-60,yCenter-50,375,148);
       showMessage.pack();
       showMessage.show();
     }
  }

  /**
   * gets the Applets X-axis center coordinates
   * @return
   */
  private int getAppletXAxisCenterCoor() {
    return (this.getX()+this.getWidth())/2;
  }

  /**
   * gets the Applets Y-axis center coordinates
   * @return
   */
  private int getAppletYAxisCenterCoor() {
    return (this.getY() + this.getHeight())/2;
  }

   /**
    *  Clears the plot screen of all traces
    */
   void clearPlot(boolean clearFunctions) {

     if ( D )
       System.out.println( "Clearing plot area" );

     int loc = this.plotSplitPane.getDividerLocation();
     int newLoc = loc;

     plotPanel.removeAll();

     pointsTextArea.setText( NO_PLOT_MSG );
     if( clearFunctions) {
       functions.clear();
     }

     validate();
     repaint();
     plotSplitPane.setDividerLocation( newLoc );
  }


  /**
   * if we select or deselect x log
   * @param e
   */
  void xLogCheckBox_actionPerformed(ActionEvent e) {
    xLog  = xLogCheckBox.isSelected();
    data.setXLog(xLog);
    addGraphPanel();
  }

 /**
  * if we select or deselect y log
  * @param e
  */
  void yLogCheckBox_actionPerformed(ActionEvent e) {
    yLog  = yLogCheckBox.isSelected();
    data.setYLog(yLog);
    addGraphPanel();
  }

 //toggles between the Data window and the plot window
  void toggleButton_actionPerformed(ActionEvent e) {
    togglePlot();
  }

  public void actionPerformed(ActionEvent e){
    if(e.getSource() instanceof JCheckBox)
      addGraphPanel();
  }

  void averageCheck_actionPerformed(ActionEvent e) {
    if(averageCheck.isSelected())
     calcAvgFunc();
    addGraphPanel();
  }

  private void calcAvgFunc(){
    functions.add(FunctionListCalc.getMean(functions));
  }
}