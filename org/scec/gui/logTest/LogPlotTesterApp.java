package org.scec.gui.logTest;

import java.awt.*;
import java.awt.event.*;
import java.applet.*;
import javax.swing.*;
import javax.swing.border.*;
import java.util.Vector;
import java.util.Iterator;

import com.jrefinery.chart.*;
import com.jrefinery.chart.tooltips.*;
import com.jrefinery.data.*;

import org.scec.data.function.*;
import org.scec.gui.*;
import org.scec.gui.plot.jfreechart.*;
import org.scec.gui.plot.*;
/**

 * <p>Title: LogPlotTesterApp</p>
 * <p>Description: This Applet checks for the log-plots</p>
 *
 * @author: Nitin Gupta & Vipin Gupta
 * @date:November 13,2002
 * @version 1.0
 */

public class LogPlotTesterApp extends JApplet implements LogPlotAPI {

  // for debug purposes
  protected final static String C = "LogPlotTesterApp";
  protected final static boolean D = false;

  //auto scales the graph
  private boolean autoScale =true;

  private boolean isStandalone = false;
  private BorderLayout borderLayout1 = new BorderLayout();
  private JSplitPane jSplitPane1 = new JSplitPane();
  private JPanel innerPlotPanel = new JPanel();
  private JPanel jPanel2 = new JPanel();
  private JLabel jLabel1 = new JLabel();
  private JComboBox rangeCombo = new JComboBox();
  private JLabel jLabel2 = new JLabel();
  private JLabel jLabel3 = new JLabel();
  private JLabel jLabel4 = new JLabel();
  private JLabel jLabel5 = new JLabel();
  private JTextField minXText = new JTextField();
  private JTextField maxXText = new JTextField();
  private JTextField minYText = new JTextField();
  private JTextField maxYText = new JTextField();
  private JButton addButton = new JButton();
  private JButton clearButton = new JButton();

  //this vector stores the different plot ranges text string
  private Vector logRanges = new Vector();

  //static string declaration for the test cases
  private static final String TEST_0= new String("Auto Scale"); // draws the graph according to the given default values
  private static final String TEST_1= new String("1");
  private static final String TEST_2= new String("2");
  private static final String TEST_3= new String("3");
  private static final String TEST_4= new String("4");
  private static final String TEST_5= new String("5");
  private static final String TEST_6= new String("6");
  private static final String TEST_7= new String("7");
  private static final String TEST_8= new String("8");
  private static final String TEST_9= new String("9");
  private static final String TEST_10= new String("10");
  private static final String TEST_11= new String("11");
  private static final String TEST_12= new String("12");
  private static final String TEST_13= new String("13");
  private static final String TEST_14= new String("14");
  private static final String TEST_15= new String("15");

  // title for the chart
  private static final String TITLE = "Log-Log Testing";

  // chart Panel
  private ChartPanel panel;

  //variables that determine the window size
  protected final static int W = 820;
  protected final static int H = 670;


  /**
   * for Y-log, 0 values will be converted to this small value
   */
  private double Y_MIN_VAL = 1e-8;

  /**
   * these four values save the log axis scale specified by selection of different
   * test cases for the logPlot
   */
  protected double minXValue;
  protected double maxXValue;
  protected double minYValue;
  protected double maxYValue;

  // Create the x-axis and y-axis - either normal or log
  com.jrefinery.chart.NumberAxis xAxis = null;
  com.jrefinery.chart.NumberAxis yAxis = null;
  DiscretizedFuncList functions = new DiscretizedFuncList();
  DiscretizedFunctionXYDataSet data = new DiscretizedFunctionXYDataSet();

  Color lightBlue = new Color( 200, 200, 230 );
  Insets defaultInsets = new Insets( 4, 4, 4, 4 );
  private GridBagLayout gridBagLayout1 = new GridBagLayout();
  private GridBagLayout gridBagLayout2 = new GridBagLayout();


  //Get a parameter value
  public String getParameter(String key, String def) {
    return isStandalone ? System.getProperty(key, def) :
        (getParameter(key) != null ? getParameter(key) : def);
  }

  //Construct the applet
  public LogPlotTesterApp() {

    logRanges.add(TEST_0);;
    logRanges.add(TEST_1);
    logRanges.add(TEST_2);
    logRanges.add(TEST_3);
    logRanges.add(TEST_4);
    logRanges.add(TEST_5);
    logRanges.add(TEST_6);
    logRanges.add(TEST_7);
    logRanges.add(TEST_8);
    logRanges.add(TEST_9);
    logRanges.add(TEST_10);
    logRanges.add(TEST_11);
    logRanges.add(TEST_12);
    logRanges.add(TEST_13);
    logRanges.add(TEST_14);
    logRanges.add(TEST_15);

    data.setFunctions(functions);
    // for Y-log, convert 0 values in Y axis to this small value
    data.setConvertZeroToMin(true,Y_MIN_VAL);
  }
  //Initialize the applet
  public void init() {
    try {
      jbInit();
    }
    catch(Exception e) {
      e.printStackTrace();
    }

    // initialize the current IMR
      initLogPlotGui();
  }


  /**
   *  This must be called before the logPlots are generated. This is what initializes the
   *  Gui
   */
  protected void initLogPlotGui() {

    // starting
    String S = C + ": initLogPlotGui(): ";

    Iterator it = this.logRanges.iterator();
    while ( it.hasNext() )
      rangeCombo.addItem(it.next());
    rangeCombo.setSelectedItem((String)rangeCombo.getItemAt(0));
  }


  //Component initialization
  private void jbInit() throws Exception {
    this.getContentPane().setLayout(borderLayout1);
    jPanel2.setLayout(gridBagLayout2);
    jLabel1.setFont(new java.awt.Font("Dialog", 1, 12));
    jLabel1.setForeground(new Color(80, 80, 133));
    jLabel1.setText("Test Case:");
    jLabel2.setFont(new java.awt.Font("Dialog", 1, 12));
    jLabel2.setForeground(new Color(80, 80, 133));
    jLabel2.setText("Min X:");
    jLabel3.setFont(new java.awt.Font("Dialog", 1, 12));
    jLabel3.setForeground(new Color(80, 80, 133));
    jLabel3.setText("Max X:");
    jLabel4.setFont(new java.awt.Font("Dialog", 1, 12));
    jLabel4.setForeground(new Color(80, 80, 133));
    jLabel4.setText("Min Y:");
    jLabel5.setFont(new java.awt.Font("Dialog", 1, 12));
    jLabel5.setForeground(new Color(80, 80, 133));
    jLabel5.setText("Max Y:");
    addButton.setBackground(new Color(200, 200, 230));
    addButton.setFont(new java.awt.Font("Dialog", 1, 10));
    addButton.setForeground(new Color(80, 80, 133));
    addButton.setText("Add Plot");
    addButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        addButton_actionPerformed(e);
      }
    });
    clearButton.setBackground(new Color(200, 200, 230));
    clearButton.setFont(new java.awt.Font("Dialog", 1, 10));
    clearButton.setForeground(new Color(80, 80, 133));
    clearButton.setText("Clear Plot");
    clearButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        clearButton_actionPerformed(e);
      }
    });
    innerPlotPanel.setLayout(gridBagLayout1);
    jSplitPane1.setDividerSize(5);
    jPanel2.setBackground(Color.white);
    rangeCombo.setBackground(new Color(200, 200, 230));
    rangeCombo.setForeground(new Color(80, 80, 133));
    rangeCombo.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        rangeCombo_actionPerformed(e);
      }
    });
    innerPlotPanel.setBackground(Color.white);
    minXText.addKeyListener(new java.awt.event.KeyAdapter() {
      public void keyTyped(KeyEvent e) {
        minXText_keyTyped(e);
      }
    });
    maxXText.addKeyListener(new java.awt.event.KeyAdapter() {
      public void keyTyped(KeyEvent e) {
        maxXText_keyTyped(e);
      }
    });
    minYText.addKeyListener(new java.awt.event.KeyAdapter() {
      public void keyTyped(KeyEvent e) {
        minYText_keyTyped(e);
      }
    });
    maxYText.addKeyListener(new java.awt.event.KeyAdapter() {
      public void keyTyped(KeyEvent e) {
        maxYText_keyTyped(e);
      }
    });
    this.getContentPane().add(jSplitPane1, BorderLayout.CENTER);
    jSplitPane1.add(innerPlotPanel, JSplitPane.LEFT);
    jSplitPane1.add(jPanel2, JSplitPane.RIGHT);
    jPanel2.add(minXText,  new GridBagConstraints(1, 1, 2, 1, 1.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 12), 110, 4));
    jPanel2.add(jLabel1,  new GridBagConstraints(0, 0, 2, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(11, 21, 0, 0), 25, 9));
    jPanel2.add(rangeCombo,  new GridBagConstraints(1, 0, 2, 1, 1.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(11, 0, 0, 12), -16, 2));
    jPanel2.add(jLabel3,  new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 21, 0, 7), 26, 9));
    jPanel2.add(maxXText,  new GridBagConstraints(1, 2, 2, 1, 1.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 12), 110, 4));
    jPanel2.add(jLabel4,  new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 21, 0, 19), 17, 9));
    jPanel2.add(minYText,  new GridBagConstraints(1, 3, 2, 1, 1.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 12), 110, 4));
    jPanel2.add(jLabel5,  new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 21, 0, 26), 7, 9));
    jPanel2.add(maxYText,  new GridBagConstraints(1, 4, 2, 1, 1.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 12), 110, 4));
    jPanel2.add(addButton,  new GridBagConstraints(0, 5, 2, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 4, 311, 0), 32, 6));
    jPanel2.add(clearButton,   new GridBagConstraints(2, 5, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 311, 12), 30, 6));
    jPanel2.add(jLabel2,    new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.NORTHEAST, GridBagConstraints.NONE, new Insets(4, 21, 5, 0), 36, 9));
    jSplitPane1.setDividerLocation(575);
  }
  //Get Applet information
  public String getAppletInfo() {
    return "Applet Information";
  }
  //Get parameter info
  public String[][] getParameterInfo() {
    return null;
  }
  //Main method
  public static void main(String[] args) {
    LogPlotTesterApp applet = new LogPlotTesterApp();
    applet.isStandalone = true;
    Frame frame;
    frame = new Frame() {
      protected void processWindowEvent(WindowEvent e) {
        super.processWindowEvent(e);
        if (e.getID() == WindowEvent.WINDOW_CLOSING) {
          System.exit(0);
        }
      }
      public synchronized void setTitle(String title) {
        super.setTitle(title);
        enableEvents(AWTEvent.WINDOW_EVENT_MASK);
      }
    };
    frame.setTitle("Log Plot Tester Applet");
    frame.add(applet, BorderLayout.CENTER);
    applet.init();
    applet.start();
    frame.setSize(W,H);
    Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
    frame.setLocation((d.width - frame.getSize().width) / 2, (d.height - frame.getSize().height) / 2);
    frame.setVisible(true);
  }

  /**
   *  This causes the model data to be calculated and a plot trace added to
   *  the current plot
   *
   * @param  e  The feature to be added to the Button_mouseClicked attribute
   */
  void addButton_actionPerformed(ActionEvent e){
     addButton();
  }

  private void addButton(){
    String S = C + ": addButton(): ";
    if ( D ) System.out.println( S + "Starting" );


    DiscretizedFuncAPI function =fillValues(new ArbitrarilyDiscretizedFunc());

    if ( D ) System.out.println( S + "New Function info = " + function.getInfo() );

    functions.clear();
    functions.add(function);
    addGraphPanel();
    return;
  }

  /**
   * this method is the interface between the JFreechart plotting capability
   * and our added requirements.
   */

  void addGraphPanel() {

    // Starting
       String S = C + ": addGraphPanel(): ";


       if(!autoScale){
         // get the min and max Y values
         minYValue=Double.parseDouble(minYText.getText());
         maxYValue=Double.parseDouble(maxYText.getText());

         //get the min and max Y values
         minXValue=Double.parseDouble(minXText.getText());
         maxXValue=Double.parseDouble(maxXText.getText());
       }

       //create the standard ticks so that smaller values too can plotted on the chart
       TickUnits units = MyTickUnits.createStandardTickUnits();

       xAxis = new com.jrefinery.chart.HorizontalLogarithmicAxis("X-Axis");
       xAxis.setAutoRangeIncludesZero( false );
       xAxis.setCrosshairLockedOnData( false );
       xAxis.setCrosshairVisible(false);
       xAxis.setStandardTickUnits(units);


       yAxis = new com.jrefinery.chart.VerticalLogarithmicAxis("Y-Axis");

       yAxis.setAutoRangeIncludesZero( false );
       yAxis.setCrosshairLockedOnData( false );
       yAxis.setCrosshairVisible( false);
       yAxis.setStandardTickUnits(units);

       int type = com.jrefinery.chart.StandardXYItemRenderer.LINES;


       LogXYItemRenderer renderer = new LogXYItemRenderer( type, new StandardXYToolTipGenerator() );


       //If the first test case is not chosen then plot the graph acording to the default x and y axis values
       if(!autoScale){
         xAxis.setRange(minXValue,maxXValue);
         yAxis.setRange(minYValue,maxYValue);
       }


       // build the plot
       org.scec.gui.PSHALogXYPlot plot = new org.scec.gui.PSHALogXYPlot(this,data, xAxis, yAxis, true, true);

       plot.setBackgroundAlpha( .8f );


       plot.setRenderer( renderer );


       JFreeChart chart = new JFreeChart(TITLE, JFreeChart.DEFAULT_TITLE_FONT, plot,false);
       chart.setBackgroundPaint( lightBlue );


       panel = new ChartPanel(chart, true, true, true, true, false);
       panel.setBorder( BorderFactory.createEtchedBorder( EtchedBorder.LOWERED ) );
       panel.setMouseZoomable(true);
       panel.setDisplayToolTips(true);
       panel.setHorizontalAxisTrace(false);
       panel.setVerticalAxisTrace(false);


       innerPlotPanel.removeAll();
       // panel added here
       innerPlotPanel.add( panel, new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0
           , GridBagConstraints.CENTER, GridBagConstraints.BOTH, defaultInsets, 0, 0 )
           );

     innerPlotPanel.validate();
     innerPlotPanel.repaint();
     if ( D ) System.out.println( S + "Done" );
  }


  /**
   * This function handles the Zero values in the X and Y data set when exception is thrown,
   * it reverts back to the linear scale displaying a message box to the user.
   */
  public void invalidLogPlot(String message) {

    int xCenter=getAppletXAxisCenterCoor();
    int yCenter=getAppletYAxisCenterCoor();
    if(message.equals("Log Value of the negative values and 0 does not exist for X-Log Plot")) {
      ShowMessage showMessage=new ShowMessage("      X-Log Plot Error as it contains Zero Values");
      showMessage.setBounds(xCenter-60,yCenter-50,370,145);
      showMessage.pack();
      showMessage.show();
    }
    else if(message.equals("Log Value of the negative values and 0 does not exist for Y-Log Plot")) {
      ShowMessage showMessage=new ShowMessage("      Y-Log Plot Error as it contains Zero Values");
      showMessage.setBounds(xCenter-60,yCenter-50,370,145);
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
   * sets the range for X-axis
   * @param xMin : minimum value for X-axis
   * @param xMax : maximum value for X-axis
   */
  public void setXRange(double xMin,double xMax) {
     minXText.setText(""+xMin);
     maxXText.setText(""+xMax);
  }

  /**
   * sets the range for Y-axis
   * @param yMin : minimum value for Y-axis
   * @param yMax : maximum value for Y-axis
   */
  public void setYRange(double yMin,double yMax) {
     minYText.setText(""+yMin);
     maxYText.setText(""+yMax);
     addGraphPanel();
  }

  void clearButton_actionPerformed(ActionEvent e) {
   functions.clear();
   innerPlotPanel.removeAll();
   panel = null;

   validate();
   repaint();
  }




  /**
   * this function sets the initial X and Y values for which log plot has to be generated.
   * @param function
   * @return the DiscretizedFuncAPI object
   */
  private  DiscretizedFuncAPI fillValues(DiscretizedFuncAPI function) {

   // function.set(0.0 , 0.3709240147258726);
    function.set(1.02, 0.3252989675766);
    function.set(2.03,0.28831584981256364);
    function.set(3.04, 0.25759059645019516);
    function.set(4.05 ,0.2317579929371139);
    function.set(5.06  , 0.2098100264835782);
    function.set(6.07 ,0.19098853513049038);
    function.set(7.08,0.17471387488216564);
    function.set(8.09 ,0.16053638059488);
    function.set(9.1 , 0.1481026319892149);
    function.set(10.11, 0.13713156153136677);
    function.set(11.12, 0.12739724227876123);
    function.set(12.13, 0.11871629546658767);
    function.set(13.14, 0.11093854783560243);
    function.set(14.15, 0.1039400106842495);
    function.set(15.16, 0.09761754132663052);
    function.set(16.17, 0.09188473966503793);
    function.set(17.18, 0.08666876245566242);
    function.set(18.19, 0.08190782703460522);
    function.set(19.2 , 0.07754923839501857);
    function.set(20.21, 0.07354781734946621);
    function.set(21.22, 0.06986463883316718);
    function.set(22.23, 0.06646601203555078);
    function.set(23.24, 0.06332265057486956);
    function.set(24.25, 0.06040899312057254);
    function.set(25.26, 0.0577026439432351);
    function.set(26.27, 0.055183909687492975);
    function.set(27.28, 0.05283541382461658);
    function.set(28.29, 0.050641774180271305);
    function.set(29.3 , 0.048589331961324214);
    function.set(30.31, 0.04666592305007844);
    function.set(31.32, 0.04486068416147701);
    function.set(32.33, 0.04316388789175837);
    function.set(33.34, 0.04156680181753229);
    function.set(34.35, 0.040061567701209534);
    function.set(35.36, 0.038641097574252305);
    function.set(36.37, 0.03729898404347587);
    function.set(37.38, 0.036029422627983136);
    function.set(38.39, 0.0348271443086532);
    function.set(39.4 , 0.03368735677655461);
    function.set(40.41, 0.03260569311533537);
    function.set(41.42, 0.03157816685661586);
    function.set(42.43, 0.03060113251538157);
    function.set(43.44, 0.02967125085122234);
    function.set(44.45, 0.02878545821647051);
    function.set(45.46, 0.027940939448209676);
    function.set(46.47, 0.027135103841264246);
    function.set(47.48, 0.026365563806454308);
    function.set(48.49, 0.025630115874880878);
    function.set(49.5 , 0.02492672375664805);
    function.set(50.51, 0.0242535032027259);
    function.set(51.52, 0.023608708452845288);
    function.set(52.53, 0.02299072008139646);
    function.set(53.54, 0.02239803407810713);
    function.set(54.55, 0.02182925202148683);
    function.set(55.56, 0.021283072221205696);
    function.set(56.57, 0.020758281721201435);
    function.set(57.58, 0.02025374906876637);
    function.set(58.59, 0.01976841776648345);
    function.set(59.6 , 0.01930130033393358);
    function.set(60.61, 0.018851472914810853);
    function.set(61.62, 0.01841807037265436);
    function.set(62.63, 0.018000281824998316);
    function.set(63.64, 0.01759734657149157);
    function.set(64.65, 0.017208550376563294);
    function.set(65.66, 0.01683322207161208);
    function.set(66.67, 0.016470730445554187);
    function.set(67.68, 0.016120481395959056);
    function.set(68.69, 0.01578191531598401);
    function.set(69.7,  0.015454504694952256);
    function.set(70.71, 0.01513775191274188);
    function.set(71.72, 0.014831187210208815);
    function.set(72.73, 0.014534366819687384);
    function.set(73.74, 0.014246871241227393);
    function.set(74.75, 0.01396830365166128);
    function.set(75.76, 0.013698288434872056);
    function.set(76.77, 0.013436469822769696);
    function.set(77.78, 0.013182510637499117);
    function.set(78.79, 0.012936091126308585);
    function.set(79.8 , 0.012696907881319344);
    function.set(80.81, 0.012464672837162585);
    function.set(81.82, 0.0122391123401022);
    function.set(82.83, 0.012019966282845403);
    function.set(83.84, 0.01180698729977027);
    function.set(84.85, 0.011599940017770862);
    function.set(85.86, 0.011398600358348401);
    function.set(86.87, 0.01120275488695998);
    function.set(87.88, 0.011012200205984302);
    function.set(88.89, 0.010826742387977394);
    function.set(89.9 , 0.010646196446175296);
    function.set(90.91, 0.010470385839457589);
    function.set(91.92, 0.010299142009219399);
    function.set(92.93, 0.010132303945810278);
    function.set(93.94, 0.009969717782391493);
    function.set(94.95, 0.009811236414237418);
    function.set(95.96, 0.009656719141666239);
    function.set(96.97, 0.00950603133492957);
    function.set(97.98, 0.00935904411952373);
    function.set(98.99, 0.00921563408050459);

   return function;
  }

  /**
   * if user types by hand in any of the fields, then do not autoscale
   * @param e
   */
  void minXText_keyTyped(KeyEvent e) {
    this.autoScale = false;
  }

  /**
   * if user types by hand in any of the fields, then do not autoscale
   * @param e
   */
  void maxXText_keyTyped(KeyEvent e) {
    this.autoScale = false;
  }

  /**
   * if user types by hand in any of the fields, then do not autoscale
   * @param e
   */
  void minYText_keyTyped(KeyEvent e) {
    this.autoScale = false;
  }

  /**
   * if user types by hand in any of the fields, then do not autoscale
   * @param e
   */
  void maxYText_keyTyped(KeyEvent e) {
    this.autoScale = false;
  }

  //sets the default range for the log Plots
  void rangeCombo_actionPerformed(ActionEvent e) {
    if(rangeCombo.getSelectedItem().toString().equalsIgnoreCase(TEST_0)){
      autoScale=true;
      addButton();
      Range rX = xAxis.getRange();
      Range rY= yAxis.getRange();
      this.minXText.setText(""+rX.getLowerBound());
      this.maxXText.setText(""+rX.getUpperBound());
      this.minYText.setText(""+rY.getLowerBound());
      this.maxYText.setText(""+rY.getUpperBound());
    }
    else {
      autoScale=false;
      if(rangeCombo.getSelectedItem().toString().equalsIgnoreCase(TEST_1)){
        setXRange(.5e-20,1e-20);
        setYRange(.5e-20,1e-20);
      }
      else if(rangeCombo.getSelectedItem().toString().equalsIgnoreCase(TEST_2)){
        setXRange(1e-20,1e-19);
        setYRange(1e-20,1e-19);
      }
      else if(rangeCombo.getSelectedItem().toString().equalsIgnoreCase(TEST_3)){
        setXRange(1e-20,1e-17);
        setYRange(1e-20,1e-17);
      }
      else if(rangeCombo.getSelectedItem().toString().equalsIgnoreCase(TEST_4)){
        setXRange(1e-20,1e-16);
        setYRange(1e-20,1e-16);
      }
      else if(rangeCombo.getSelectedItem().toString().equalsIgnoreCase(TEST_5)){
        setXRange(1e-20,1e-15);
        setYRange(1e-20,1e-15);
      }
      else if(rangeCombo.getSelectedItem().toString().equalsIgnoreCase(TEST_6)){
        setXRange(1e-11,1e-7);
        setYRange(1e-11,1e-7);
      }
      else if(rangeCombo.getSelectedItem().toString().equalsIgnoreCase(TEST_7)){
        setXRange(1e-2,10);
        setYRange(1e-2,10);
      }
      else if(rangeCombo.getSelectedItem().toString().equalsIgnoreCase(TEST_8)){
        setXRange(1e-2,100);
        setYRange(1e-2,100);
      }
      else if(rangeCombo.getSelectedItem().toString().equalsIgnoreCase(TEST_9)){
        setXRange(1e-2,1000);
        setYRange(1e-2,1000);
      }
      else if(rangeCombo.getSelectedItem().toString().equalsIgnoreCase(TEST_10)){
        setXRange(10,10000);
        setYRange(10,10000);
      }
      else if(rangeCombo.getSelectedItem().toString().equalsIgnoreCase(TEST_11)){
        setXRange(10,100000);
        setYRange(10,100000);
      }
      else if(rangeCombo.getSelectedItem().toString().equalsIgnoreCase(TEST_12)){
        setXRange(2,2);
        setYRange(2,2);
      }
      else if(rangeCombo.getSelectedItem().toString().equalsIgnoreCase(TEST_13)){
        setXRange(1,1);
        setYRange(1,1);
      }
      else if(rangeCombo.getSelectedItem().toString().equalsIgnoreCase(TEST_14)){
        setXRange(10e4,10e6);
        setYRange(10e4,10e6);
      }
      else if(rangeCombo.getSelectedItem().toString().equalsIgnoreCase(TEST_15)){
        setXRange(2,8);
        setYRange(2,8);
      }
    }
  }



}