package org.scec.sha.gui;

import java.awt.*;
import java.awt.event.*;
import java.applet.*;
import javax.swing.*;
import javax.swing.border.*;
import java.util.Vector;
import java.util.Iterator;
import java.net.*;



import com.jrefinery.chart.*;
import com.jrefinery.chart.axis.*;
import com.jrefinery.chart.tooltips.*;
import com.jrefinery.data.*;


import org.scec.data.function.*;
import org.scec.gui.*;
import org.scec.gui.plot.LogPlotAPI;
import org.scec.gui.plot.jfreechart.*;
import org.scec.param.*;
import org.scec.param.editor.*;
import org.scec.param.event.*;
import org.scec.util.*;
import org.scec.sha.gui.controls.*;
import org.scec.sha.gui.beans.*;
import org.scec.sha.gui.infoTools.*;
import org.scec.sha.imr.AttenuationRelationshipAPI;
import org.scec.sha.imr.AttenuationRelationship;
import org.scec.sha.earthquake.EqkRupForecastAPI;
import org.scec.sha.earthquake.EqkRupForecast;
import org.scec.sha.earthquake.ERF_List;
import org.scec.sha.calc.HazardCurveCalculator;
import org.scec.sha.calc.DisaggregationCalculator;
import org.scec.sha.calc.FractileCurveCalculator;
import org.scec.data.Site;

/**
 * <p>Title: HazardCurveApplet</p>
 * <p>Description: </p>
 * @author Nitin Gupta and Vipin Gupta
 * Date : Sept 23 , 2002
 * @version 1.0
 */

public class HazardCurveApplet extends JApplet
    implements LogPlotAPI, ParameterChangeListener, AxisLimitsControlPanelAPI,
    DisaggregationControlPanelAPI, ERF_EpistemicListControlPanelAPI {

  /**
   * Name of the class
   */
  private final static String C = "PEER_TestApplet";
  // for debug purpose
  private final static boolean D = false;

  /**
   *  The object class names for all the supported attenuation ralations (IMRs)
   *  Temp until figure out way to dynamically load classes during runtime
   */
  public final static String BJF_CLASS_NAME = "org.scec.sha.imr.attenRelImpl.BJF_1997_AttenRel";
  public final static String AS_CLASS_NAME = "org.scec.sha.imr.attenRelImpl.AS_1997_AttenRel";
  public final static String C_CLASS_NAME = "org.scec.sha.imr.attenRelImpl.Campbell_1997_AttenRel";
  public final static String SCEMY_CLASS_NAME = "org.scec.sha.imr.attenRelImpl.SCEMY_1997_AttenRel";
  public final static String F_CLASS_NAME = "org.scec.sha.imr.attenRelImpl.Field_2000_AttenRel";
 // public final static String A_CLASS_NAME = "org.scec.sha.imr.attenRelImpl.Abrahamson_2000_AttenRel";
  public final static String CB_CLASS_NAME = "org.scec.sha.imr.attenRelImpl.CB_2003_AttenRel";

  /**
   *  The object class names for all the supported Eqk Rup Forecasts
   */
  public final static String PEER_FAULT_FORECAST_CLASS_NAME = "org.scec.sha.earthquake.rupForecastImpl.PEER_TestCases.PEER_FaultForecast";
  public final static String PEER_AREA_FORECAST_CLASS_NAME = "org.scec.sha.earthquake.rupForecastImpl.PEER_TestCases.PEER_AreaForecast";
  public final static String PEER_NON_PLANAR_FAULT_FORECAST_CLASS_NAME = "org.scec.sha.earthquake.rupForecastImpl.PEER_TestCases.PEER_NonPlanarFaultForecast";
  public final static String PEER_LISTRIC_FAULT_FORECAST_CLASS_NAME = "org.scec.sha.earthquake.rupForecastImpl.PEER_TestCases.PEER_ListricFaultForecast";
  public final static String PEER_MULTI_SOURCE_FORECAST_CLASS_NAME = "org.scec.sha.earthquake.rupForecastImpl.PEER_TestCases.PEER_MultiSourceForecast";
  public final static String PEER_LOGIC_TREE_FORECAST_CLASS_NAME = "org.scec.sha.earthquake.rupForecastImpl.PEER_TestCases.PEER_LogicTreeERF_List";
  public final static String FRANKEL_FORECAST_CLASS_NAME = "org.scec.sha.earthquake.rupForecastImpl.Frankel96.Frankel96_EqkRupForecast";
  public final static String FRANKEL_ADJ_FORECAST_CLASS_NAME = "org.scec.sha.earthquake.rupForecastImpl.Frankel96.Frankel96_AdjustableEqkRupForecast";
  public final static String STEP_FORECAST_CLASS_NAME = "org.scec.sha.earthquake.rupForecastImpl.step.STEP_EqkRupForecast";
  public final static String WG02_ERF_LIST_CLASS_NAME = "org.scec.sha.earthquake.rupForecastImpl.WG02.WG02_ERF_Epistemic_List";

  // instances of the GUI Beans which will be shown in this applet
  private ERF_GuiBean erfGuiBean;
  private IMR_GuiBean imrGuiBean;
  private IMT_GuiBean imtGuiBean;
  private Site_GuiBean siteGuiBean;
  private TimeSpanGuiBean timeSpanGuiBean;

  // Strings for control pick list
  private final static String CONTROL_PANELS = "Control Panels";
  private final static String PEER_TEST_CONTROL = "PEER Test Case Selector";
  private final static String DISAGGREGATION_CONTROL = "Disaggregation";
  private final static String EPISTEMIC_CONTROL = "ERF Epistemic Control";
  private final static String AXIS_CONTROL = "Axis Control";
  private final static String DISTANCE_CONTROL = "Max Source-Site Distance";
  private final static String SITES_OF_INTEREST_CONTROL = "Sites of Interest";
  private final static String CVM_CONTROL = "Set Site Params from CVM";

  // objects for control panels
  private PEER_TestCaseSelectorControlPanel peerTestsControlPanel;
  private DisaggregationControlPanel disaggregationControlPanel;
  private AxisLimitsControlPanel axisControlPanel;
  private ERF_EpistemicListControlPanel epistemicControlPanel;
  private SetMinSourceSiteDistanceControlPanel distanceControlPanel;
  private SitesOfInterestControlPanel sitesOfInterest;
  private SetSiteParamsFromCVMControlPanel cvmControlPanel;

  // message string to be dispalayed if user chooses Axis Scale
   // without first clicking on "Add Graph"
  private final static String AXIS_RANGE_NOT_ALLOWED =
      new String("First Choose Add Graph. Then choose Axis Scale option");



  // mesage needed in case of show data if plot is not available
  private final static String NO_PLOT_MSG = "No Plot Data Available";

  private Insets plotInsets = new Insets( 4, 10, 4, 4 );

  private boolean isStandalone = false;
  private Border border1;


  //log flags declaration
  private boolean xLog =false;
  private boolean yLog =false;

  // default insets
  private Insets defaultInsets = new Insets( 4, 4, 4, 4 );

  // height and width of the applet
  private final static int W = 970;
  private final static int H = 750;

  /**
   * FunctionList declared
   */
  private DiscretizedFuncList totalProbFuncs = new DiscretizedFuncList();
  private DiscretizedFunctionXYDataSet data = new DiscretizedFunctionXYDataSet();

  // make a array for saving the X values
  private  double [] xValues = { .001, .01, .05, .1, .15, .2, .25, .3, .4, .5,
                               .6, .7, .8, .9, 1, 1.1, 1.2, 1.3, 1.4, 1.5}  ;

  // Create the x-axis and y-axis - either normal or log
  private com.jrefinery.chart.axis.NumberAxis xAxis = null;
  private com.jrefinery.chart.axis.NumberAxis yAxis = null;


  // variable needed for plotting Epistemic list
  private boolean isEqkList = false; // whther we are plottin the Eqk List
  private boolean isAllCurves = true; // whether to plot all curves
  // whether user wants to plot No percentile, or 5, 50 and 95 percentile or custom percentile
  private String percentileOption = ERF_EpistemicListControlPanel.NO_PERCENTILE;
  // whether avg is selected by the user
  private boolean avgSelected = false;
  private FractileCurveCalculator fractileCalc;

  /**
   * these four values save the custom axis scale specified by user
   */
  private double minXValue;
  private double maxXValue;
  private  double minYValue;
  private double maxYValue;
  private boolean customAxis = false;


  private GridBagLayout gridBagLayout4 = new GridBagLayout();
  private GridBagLayout gridBagLayout6 = new GridBagLayout();
  private GridBagLayout gridBagLayout7 = new GridBagLayout();
  private GridBagLayout gridBagLayout3 = new GridBagLayout();

  /**
   * adding scroll pane for showing data
   */
  private JScrollPane dataScrollPane = new JScrollPane();

  // text area to show the data values
  private JTextArea pointsTextArea = new JTextArea();

  /**
   * chart panel
   */
  private ChartPanel chartPanel;

  //flag to check for the disaggregation functionality
  private boolean disaggregationFlag= false;
  private String disaggregationString;

  // PEER Test Cases
  private String TITLE = new String("Hazard Curves");

  // light blue color
  private Color lightBlue = new Color( 200, 200, 230 );

  /**
   * for Y-log, 0 values will be converted to this small value
   */
  private double Y_MIN_VAL = 1e-16;

  private boolean graphOn = false;
  private GridBagLayout gridBagLayout11 = new GridBagLayout();
  private JPanel jPanel1 = new JPanel();
  private GridBagLayout gridBagLayout2 = new GridBagLayout();
  private GridBagLayout gridBagLayout1 = new GridBagLayout();
  private Border border2;
  private final static String AUTO_SCALE = "Auto Scale";
  private final static String CUSTOM_SCALE = "Custom Scale";
  private final static Dimension COMBO_DIM = new Dimension( 180, 30 );
  private final static Dimension BUTTON_DIM = new Dimension( 80, 20 );
  private Border border3;
  private Border border4;
  private GridBagLayout gridBagLayout16 = new GridBagLayout();
  private Border border5;
  private Border border6;
  private Border border7;
  private Border border8;



  //images for the OpenSHA
  private final static String FRAME_ICON_NAME = "openSHA_Aqua_sm.gif";
  private final static String POWERED_BY_IMAGE = "PoweredBy.gif";

  //static string for the OPENSHA website
  private final static String OPENSHA_WEBSITE="http://www.OpenSHA.org";

  private JLabel imgLabel = new JLabel();
  private JSplitPane siteSplitPane = new JSplitPane();
  private JSplitPane chartSplit = new JSplitPane();
  private JPanel timeSpanPanel = new JPanel();
  private GridBagLayout gridBagLayout8 = new GridBagLayout();
  private GridBagLayout gridBagLayout5 = new GridBagLayout();
  private JSplitPane erfSplitPane = new JSplitPane();
  private JPanel sitePanel = new JPanel();
  private JPanel imtPanel = new JPanel();
  private JSplitPane controlsSplit = new JSplitPane();
  private JTabbedPane paramsTabbedPane = new JTabbedPane();
  private JPanel erfPanel = new JPanel();
  private GridBagLayout gridBagLayout15 = new GridBagLayout();
  private GridBagLayout gridBagLayout13 = new GridBagLayout();
  private GridBagLayout gridBagLayout12 = new GridBagLayout();
  private JPanel imrPanel = new JPanel();
  private JSplitPane buttonSplitPane = new JSplitPane();
  private JPanel panel = new JPanel();
  private GridBagLayout gridBagLayout9 = new GridBagLayout();
  private JButton clearButton = new JButton();
  private JCheckBox jCheckylog = new JCheckBox();
  private JButton toggleButton = new JButton();
  private JPanel buttonPanel = new JPanel();
  private JCheckBox progressCheckBox = new JCheckBox();
  private JButton addButton = new JButton();
  private JCheckBox jCheckxlog = new JCheckBox();
  private JComboBox controlComboBox = new JComboBox();
  private GridBagLayout gridBagLayout10 = new GridBagLayout();
  private GridBagLayout gridBagLayout14 = new GridBagLayout();
  private BorderLayout borderLayout1 = new BorderLayout();

  //Get command-line parameter value
  public String getParameter(String key, String def) {
    return isStandalone ? System.getProperty(key, def) :
        (getParameter(key) != null ? getParameter(key) : def);
  }

  //Construct the applet
  public HazardCurveApplet() {
    data.setFunctions(this.totalProbFuncs);
    // for Y-log, convert 0 values in Y axis to this small value
    data.setConvertZeroToMin(true,Y_MIN_VAL);
  }
  //Initialize the applet
  public void init() {
    try {

      // initialize the control pick list
      initControlList();

      // initialize the GUI components
      jbInit();

      // initialize the various GUI beans
      initIMR_GuiBean();
      initIMT_GuiBean();
      initSiteGuiBean();
      initERF_GuiBean();
      initTimeSpanGuiBean();

    }
    catch(Exception e) {
      e.printStackTrace();
    }
  }

  //Component initialization
  private void jbInit() throws Exception {
    border1 = BorderFactory.createLineBorder(SystemColor.controlText,1);
    border2 = BorderFactory.createLineBorder(SystemColor.controlText,1);
    border3 = BorderFactory.createEmptyBorder();
    border4 = BorderFactory.createLineBorder(SystemColor.controlText,1);
    border5 = BorderFactory.createLineBorder(SystemColor.controlText,1);
    border6 = BorderFactory.createBevelBorder(BevelBorder.RAISED,Color.white,Color.white,new Color(98, 98, 112),new Color(140, 140, 161));
    border7 = BorderFactory.createBevelBorder(BevelBorder.RAISED,Color.white,Color.white,new Color(98, 98, 112),new Color(140, 140, 161));
    border8 = BorderFactory.createBevelBorder(BevelBorder.RAISED,Color.white,Color.white,new Color(98, 98, 112),new Color(140, 140, 161));
    this.getContentPane().setBackground(Color.white);
    this.setSize(new Dimension(973, 670));
    this.getContentPane().setLayout(borderLayout1);


    // for showing the data on click of "show data" button
    pointsTextArea.setBorder( BorderFactory.createEtchedBorder() );
    pointsTextArea.setText( NO_PLOT_MSG );
    pointsTextArea.setLineWrap(true);
    dataScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    dataScrollPane.setBorder( BorderFactory.createEtchedBorder() );
    jPanel1.setLayout(gridBagLayout14);



    jPanel1.setBackground(Color.white);
    jPanel1.setBorder(border4);
    jPanel1.setMinimumSize(new Dimension(959, 600));
    jPanel1.setPreferredSize(new Dimension(959, 600));
    //clearButton.setBorder(null);
    //toggleButton.setBorder(null);
    //addButton.setBorder(null);

    //loading the OpenSHA Logo
    imgLabel.setText("");
    imgLabel.setIcon(new ImageIcon(ImageUtils.loadImage(this.POWERED_BY_IMAGE)));
    imgLabel.addMouseListener(new java.awt.event.MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        imgLabel_mouseClicked(e);
      }
    });

    siteSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
    siteSplitPane.setDividerSize(5);
    timeSpanPanel.setLayout(gridBagLayout12);
    erfSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
    erfSplitPane.setBottomComponent(timeSpanPanel);
    erfSplitPane.setTopComponent(erfPanel);
    sitePanel.setLayout(gridBagLayout13);
    sitePanel.setBackground(Color.white);
    imtPanel.setLayout(gridBagLayout8);
    imtPanel.setBackground(Color.white);
    controlsSplit.setOrientation(JSplitPane.VERTICAL_SPLIT);
    controlsSplit.setDividerSize(5);
    erfPanel.setLayout(gridBagLayout5);
    erfPanel.setBackground(Color.white);
    erfPanel.setBorder(border2);
    erfPanel.setMaximumSize(new Dimension(2147483647, 10000));
    erfPanel.setMinimumSize(new Dimension(2, 300));
    erfPanel.setPreferredSize(new Dimension(2, 300));
    imrPanel.setLayout(gridBagLayout15);
    imrPanel.setBackground(Color.white);
    panel.setLayout(gridBagLayout9);
    panel.setBackground(Color.white);
    panel.setBorder(border5);
    panel.setMinimumSize(new Dimension(0, 0));
    clearButton.setBackground(new Color(200, 200, 230));
    clearButton.setFont(new java.awt.Font("Dialog", 1, 11));
    clearButton.setForeground(new Color(80, 80, 133));
    clearButton.setBorder(null);
    clearButton.setMaximumSize(new Dimension(97, 31));
    clearButton.setMinimumSize(new Dimension(97, 31));
    clearButton.setPreferredSize(new Dimension(92, 33));
    clearButton.setText("Clear Plot");
    clearButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        clearButton_actionPerformed(e);
      }
    });
    jCheckylog.setBackground(Color.white);
    jCheckylog.setFont(new java.awt.Font("Dialog", 1, 11));
    jCheckylog.setForeground(new Color(80, 80, 133));
    jCheckylog.setText("Y Log");
    jCheckylog.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        jCheckylog_actionPerformed(e);
      }
    });
    toggleButton.setBackground(new Color(200, 200, 230));
    toggleButton.setFont(new java.awt.Font("Dialog", 1, 11));
    toggleButton.setForeground(new Color(80, 80, 133));
    toggleButton.setBorder(null);
    toggleButton.setMaximumSize(new Dimension(97, 31));
    toggleButton.setMinimumSize(new Dimension(97, 31));
    toggleButton.setPreferredSize(new Dimension(92, 33));
    toggleButton.setToolTipText("");
    toggleButton.setText("Show Data");
    toggleButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        toggleButton_actionPerformed(e);
      }
    });
    buttonPanel.setBackground(Color.white);
    buttonPanel.setMinimumSize(new Dimension(568, 20));
    buttonPanel.setLayout(gridBagLayout10);
    progressCheckBox.setBackground(Color.white);
    progressCheckBox.setFont(new java.awt.Font("Dialog", 1, 12));
    progressCheckBox.setForeground(new Color(80, 80, 133));
    progressCheckBox.setSelected(true);
    progressCheckBox.setText("Show Progress Bar");
    addButton.setBackground(new Color(200, 200, 230));
    addButton.setFont(new java.awt.Font("Dialog", 1, 11));
    addButton.setForeground(new Color(80, 80, 133));
    addButton.setBorder(null);
    addButton.setMaximumSize(new Dimension(97, 31));
    addButton.setMinimumSize(new Dimension(97, 31));
    addButton.setPreferredSize(new Dimension(97, 31));
    addButton.setText("Add Graph");
    addButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        addButton_actionPerformed(e);
      }
    });
    jCheckxlog.setBackground(Color.white);
    jCheckxlog.setFont(new java.awt.Font("Dialog", 1, 11));
    jCheckxlog.setForeground(new Color(80, 80, 133));
    jCheckxlog.setText("X Log");
    jCheckxlog.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        jCheckxlog_actionPerformed(e);
      }
    });
    controlComboBox.setBackground(new Color(200, 200, 230));
    controlComboBox.setForeground(new Color(80, 80, 133));
    controlComboBox.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        controlComboBox_actionPerformed(e);
      }
    });
    buttonSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
    buttonSplitPane.setBottomComponent(buttonPanel);
    buttonSplitPane.setTopComponent(panel);
    dataScrollPane.getViewport().add( pointsTextArea, null );
    this.getContentPane().add(jPanel1, BorderLayout.CENTER);
    buttonPanel.add(imgLabel,  new GridBagConstraints(0, 2, 0, 0, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(3, 3, 3, 3), 0, 0));
    jPanel1.add(chartSplit,  new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(9, 9, 2, 1), 185, 281));
    chartSplit.add(paramsTabbedPane, JSplitPane.RIGHT);
    controlsSplit.add(imrPanel, JSplitPane.TOP);
    controlsSplit.add(siteSplitPane, JSplitPane.BOTTOM);
    siteSplitPane.add(sitePanel, JSplitPane.TOP);
    siteSplitPane.add(imtPanel, JSplitPane.BOTTOM);
    paramsTabbedPane.add(controlsSplit, "IMR, IMT & Site");
    paramsTabbedPane.add(erfSplitPane, "ERF & Time Span");
    erfSplitPane.add(erfPanel, JSplitPane.LEFT);
    erfSplitPane.add(timeSpanPanel, JSplitPane.RIGHT);
    chartSplit.add(buttonSplitPane, JSplitPane.LEFT);
    buttonSplitPane.add(buttonPanel, JSplitPane.BOTTOM);
    buttonSplitPane.add(panel, JSplitPane.TOP);
    buttonPanel.add(toggleButton,   new GridBagConstraints(4, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(4, 4, 4, 4), 0, 0));
    buttonPanel.add(jCheckylog,   new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(4, 4, 4, 4), 1, 3));
    buttonPanel.add(controlComboBox,   new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(4, 4, 4, 4), 40, 0));
    buttonPanel.add(addButton,   new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(4, 4, 4, 4), 0, 0));
    buttonPanel.add(clearButton,   new GridBagConstraints(2, 0, 2, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(4, 4, 4, 4), 0, 0));
    buttonPanel.add(jCheckxlog,   new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(4, 4, 4, 4), 1, 7));
    buttonPanel.add(progressCheckBox,   new GridBagConstraints(3, 1, 2, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(4, 4, 4, 4), 14, 0));
    siteSplitPane.setDividerLocation(200);
    chartSplit.setDividerLocation(575);
    controlsSplit.setDividerLocation(200);
    erfPanel.validate();
    erfPanel.repaint();
    buttonSplitPane.setDividerLocation(500);
    erfSplitPane.setDividerLocation(475);


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
    return "Applet Information";
  }

  //Get parameter info
  public String[][] getParameterInfo() {
    return null;
  }

  //Main method
  public static void main(String[] args) {
    HazardCurveApplet applet = new HazardCurveApplet();
    applet.isStandalone = true;
    JFrame frame = new JFrame();
    //EXIT_ON_CLOSE == 3
    frame.setDefaultCloseOperation(3);
    frame.setTitle("Hazard Curve Calculator");
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
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      //UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
    }
    catch(Exception e) {
    }
  }


  /**
   *  Adds a feature to the GraphPanel attribute of the EqkForecastApplet object
   */
  private void addGraphPanel() {

      // Starting
      String S = C + ": addGraphPanel(): ";

      String newXYAxisName = this.totalProbFuncs.getXYAxesName();


      // create a default chart based on some sample data...

      // Determine which IM to add to the axis labeling
      String xAxisLabel = totalProbFuncs.getXAxisName();
      String yAxisLabel = totalProbFuncs.getYAxisName();


      //create the standard ticks so that smaller values too can plotted on the chart
      TickUnits units = MyTickUnits.createStandardTickUnits();


      /// check if x log is selected or not
      if(xLog) xAxis = new HorizontalLogarithmicAxis(xAxisLabel);
      else xAxis = new HorizontalNumberAxis( xAxisLabel );

      xAxis.setAutoRangeIncludesZero( false );
      xAxis.setStandardTickUnits(units);
      xAxis.setTickMarksVisible(false);

      /// check if y log is selected or not
      if(yLog) yAxis = new VerticalLogarithmicAxis(yAxisLabel);
      else yAxis = new VerticalNumberAxis( yAxisLabel );

      yAxis.setAutoRangeIncludesZero( false );
      yAxis.setStandardTickUnits(units);
      yAxis.setTickMarksVisible(false);

      int type = com.jrefinery.chart.renderer.StandardXYItemRenderer.LINES;


      com.jrefinery.chart.renderer.LogXYItemRenderer renderer
          = new com.jrefinery.chart.renderer.LogXYItemRenderer( type, new StandardXYToolTipGenerator() );

      // draw all plots in black color for Eqk List
      if(this.isEqkList) {
        int num = totalProbFuncs.size();
        int numFractiles;
        if(percentileOption.equalsIgnoreCase(ERF_EpistemicListControlPanel.CUSTOM_PERCENTILE))
          numFractiles = 1;
        else if(percentileOption.equalsIgnoreCase(ERF_EpistemicListControlPanel.FIVE_50_95_PERCENTILE))
          numFractiles = 3;
        else numFractiles = 0;
        int diff ;
        if(this.avgSelected) num= num - 1;
        diff = num - numFractiles ;
        int i;
        for(i=0; i<diff; ++i) // set black color for curves
          renderer.setSeriesPaint(i,Color.black);
        for(i=diff;i<num;++i) // set red color for fractiles
          renderer.setSeriesPaint(i,Color.red);
        // draw average in green color
        if(this.avgSelected) renderer.setSeriesPaint(i,Color.green);

      }

      /* to set the range of the axis on the input from the user if the range combo box is selected*/
      if(this.customAxis) {
          xAxis.setRange(this.minXValue,this.maxXValue);
          yAxis.setRange(this.minYValue,this.maxYValue);
        }

      // build the plot
      org.scec.gui.PSHALogXYPlot plot = new org.scec.gui.PSHALogXYPlot(this,data,
                                       xAxis, yAxis, xLog, yLog);

      plot.setDomainCrosshairLockedOnData(false);
      plot.setDomainCrosshairVisible(false);
      plot.setRangeCrosshairLockedOnData(false);
      plot.setRangeCrosshairVisible(false);
      plot.setBackgroundAlpha( .8f );
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


      if(D) System.out.println(this.totalProbFuncs.toString());
      if(D) System.out.println(S + "data:" + data);

      graphOn=false;
      togglePlot();
   }


   /**
    *  Toggle between showing the graph and showing the actual data
    */
   private void togglePlot() {

       // Starting
       String S = C + ": togglePlot(): ";
       panel.removeAll();
       if ( graphOn ) {

           this.toggleButton.setText( "Show Plot" );
           graphOn = false;

           panel.add( dataScrollPane, new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0
                   , GridBagConstraints.CENTER, GridBagConstraints.BOTH, plotInsets, 0, 0 ) );
       }
       else {
           graphOn = true;
           // dataScrollPane.setVisible(false);
           this.toggleButton.setText( "Show Data" );
                         // panel added here
           if(chartPanel !=null)
               panel.add( chartPanel, new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0
                       , GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 0, 0, 0, 0 ), 0, 0 ) );

           else
               // innerPlotPanel.setBorder(oval);
               panel.add( dataScrollPane, new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0
                       , GridBagConstraints.CENTER, GridBagConstraints.BOTH, plotInsets, 0, 0 ) );

       }


       validate();
       repaint();

       if ( D ) System.out.println( S + "Ending" );

    }

    /**
     * this function is called when Add Graph button is clicked
     * @param e
     */
    void addButton_actionPerformed(ActionEvent e) {
      addButton();
    }


    /**
     * this function is called to draw the graph
     */
    private void addButton() {


      // clear the function list
      //this.totalProbFuncs.clear();

      // do not show warning messages in IMR gui bean. this is needed
      // so that warning messages for site parameters are not shown when Add graph is clicked
      imrGuiBean.showWarningMessages(false);

      computeHazardCurve();

      // you can show warning messages now
      imrGuiBean.showWarningMessages(true);

      // set the log values
      data.setXLog(xLog);
      data.setYLog(yLog);

      // set the data in the text area
      String xAxisTitle =  totalProbFuncs.getXAxisName();
      String yAxisTitle =  totalProbFuncs.getYAxisName();

      this.pointsTextArea.setText(totalProbFuncs.toString());
      addGraphPanel();

      //displays the disaggregation string in the pop-up window
      if(disaggregationString !=null) {
        HazardCurveDisaggregationWindow disaggregation=new HazardCurveDisaggregationWindow(this, disaggregationString);
        disaggregation.pack();
        disaggregation.show();

      }
      disaggregationString=null;
    }

    /**
     * if we select or deselect x log
     * @param e
     */
    void jCheckxlog_actionPerformed(ActionEvent e) {
      xLog  = this.jCheckxlog.isSelected();
      data.setXLog(xLog);
      addGraphPanel();
    }

    /**
     * if we select or deselect x log
     * @param e
     */
    void jCheckylog_actionPerformed(ActionEvent e) {
      yLog  = this.jCheckylog.isSelected();
      data.setYLog(yLog);
      addGraphPanel();
  }

  /**
   * This function handles the Zero values in the X and Y data set when exception is thrown,
   * it reverts back to the linear scale displaying a message box to the user.
   */
  public void invalidLogPlot(String message) {


     if(message.equals("Log Value of the negative values and 0 does not exist for X-Log Plot")) {
       ShowMessage showMessage=new ShowMessage(this, "      X-Log Plot Error as it contains Zero Values");
       showMessage.pack();
       showMessage.show();
       panel.removeAll();
       this.jCheckxlog.setSelected(false);
       xLog  = false;
       data.setXLog(xLog);
     }

     if(message.equals("Log Value of the negative values and 0 does not exist for Y-Log Plot")) {
       ShowMessage showMessage=new ShowMessage(this, "      Y-Log Plot Error as it contains Zero Values");
       showMessage.pack();
       showMessage.show();
       panel.removeAll();
       this.jCheckylog.setSelected(false);
       yLog  = false;
       data.setYLog(yLog);
     }
     this.addGraphPanel();
  }


  /**
   * when "show data" button is clicked
   *
   * @param e
   */
  void toggleButton_actionPerformed(ActionEvent e) {
    this.togglePlot();
  }

  /**
   * this function is called when "clear plot" is selected
   *
   * @param e
   */
  void clearButton_actionPerformed(ActionEvent e) {
    clearPlot(true);
  }

  /**
   *  Clears the plot screen of all traces
   */
  private void clearPlot(boolean clearFunctions) {

    if ( D )
      System.out.println( "Clearing plot area" );

    int loc = this.chartSplit.getDividerLocation();
    int newLoc = loc;

    panel.removeAll();

    pointsTextArea.setText( NO_PLOT_MSG );
    if( clearFunctions) {
      this.totalProbFuncs.clear();
    }

    validate();
    repaint();
    chartSplit.setDividerLocation( newLoc );
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
     addGraphPanel();

  }

  /**
   * set the auto range for the axis. This function is called
   * from the AxisLimitControlPanel
   */
 public void setAutoRange() {
   this.customAxis=false;
   addGraphPanel();
 }

  /**
    * This function to specify whether disaggregation is selected or not
    * @param isSelected : True if disaggregation is selected , else false
    */
   public void setDisaggregationSelected(boolean isSelected) {
     disaggregationFlag = isSelected;
   }



  void imgLabel_mouseClicked(MouseEvent e) {
    try{
      this.getAppletContext().showDocument(new URL(OPENSHA_WEBSITE), "new_peer_win");
    }catch(java.net.MalformedURLException ee){
      JOptionPane.showMessageDialog(this,new String("No Internet Connection Available"),
                                    "Error Connecting to Internet",JOptionPane.OK_OPTION);
    }
  }


  /**
   *  Any time a control paramater or independent paramater is changed
   *  by the user in a GUI this function is called, and a paramater change
   *  event is passed in. This function then determines what to do with the
   *  information ie. show some paramaters, set some as invisible,
   *  basically control the paramater lists.
   *
   * @param  event
   */
  public void parameterChange( ParameterChangeEvent event ) {

    String S = C + ": parameterChange(): ";
    if ( D )  System.out.println( "\n" + S + "starting: " );

    String name1 = event.getParameterName();

    // if IMR selection changed, update the site parameter list and supported IMT
    if ( name1.equalsIgnoreCase(imrGuiBean.IMR_PARAM_NAME)) {
      AttenuationRelationshipAPI imr = imrGuiBean.getSelectedIMR_Instance();
      imtGuiBean.setIMR(imr);
      imtGuiBean.validate();
      imtGuiBean.repaint();
      siteGuiBean.replaceSiteParams(imr.getSiteParamsIterator());
      siteGuiBean.validate();
      siteGuiBean.repaint();
      }
      if(name1.equalsIgnoreCase(this.erfGuiBean.ERF_PARAM_NAME)) {
        /* get the selected ERF
        NOTE : We have used erfGuiBean.getSelectedERF_Instance()INSTEAD OF
        erfGuiBean.getSelectedERF.
        Difference is that erfGuiBean.getSelectedERF_Instance() does not update
        the forecast while erfGuiBean.getSelectedERF updates the
        */
        this.timeSpanGuiBean.setTimeSpan(erfGuiBean.getSelectedERF_Instance().getTimeSpan());
        controlComboBox.removeAllItems();
        this.initControlList();
        // add the Epistemic control panel option if Epistemic ERF is selected
        if(erfGuiBean.isEpistemicList()) {
          this.controlComboBox.addItem(EPISTEMIC_CONTROL);
          controlComboBox.setSelectedItem(EPISTEMIC_CONTROL);
        }
      }
  }


  /**
   * Gets the probabilities functiion based on selected parameters
   * this function is called when add Graph is clicked
   */
  private void computeHazardCurve() {
    this.isEqkList = false;



    // whwther to show progress bar in case of update forecast
    erfGuiBean.showProgressBar(this.progressCheckBox.isSelected());
    // get the selected forecast model
    EqkRupForecastAPI eqkRupForecast = erfGuiBean.getSelectedERF();


    // get the selected IMR
    AttenuationRelationshipAPI imr = imrGuiBean.getSelectedIMR_Instance();

    // make a site object to pass to IMR
    Site site = siteGuiBean.getSite();

    try {
      // this function will get the selected IMT parameter and set it in IMT
      imtGuiBean.setIMR_Param();
    } catch (Exception ex) {
      if(D) System.out.println(C + ":Param warning caught"+ex);
      ex.printStackTrace();
    }


    // check whether this forecast is a Forecast List
    // if this is forecast list , handle it differently
    boolean isEqkForecastList = false;
    if(eqkRupForecast instanceof ERF_List)  {
      handleForecastList(site, imr, eqkRupForecast);
      return;
    }
    // this is not a eqk list
   this.isEqkList = false;
    // calculate the hazard curve
   HazardCurveCalculator calc = new HazardCurveCalculator();
   // do not show progress bar if not desired by user
   calc.showProgressBar(this.progressCheckBox.isSelected());
   if(distanceControlPanel!=null)  calc.setMaxSourceDistance(distanceControlPanel.getDistance());
   // initialize the values in condProbfunc with log values as passed in hazFunction
   // intialize the hazard function
   ArbitrarilyDiscretizedFunc hazFunction = new ArbitrarilyDiscretizedFunc();
   initX_Values(hazFunction);
   try {
     // calculate the hazard curve
     calc.getHazardCurve(hazFunction, site, imr, (EqkRupForecast)eqkRupForecast);
     hazFunction.setInfo("\n"+getCurveParametersInfo()+"\n");
     hazFunction = toggleHazFuncLogValues(hazFunction);
   }catch (RuntimeException e) {
     JOptionPane.showMessageDialog(this, e.getMessage(),
                                   "Parameters Invalid", JOptionPane.INFORMATION_MESSAGE);
     e.printStackTrace();
     return;
   }

   //inititialising the disaggregation String
   disaggregationString=null;
   //checking the disAggregation flag
   if(this.disaggregationFlag){
     DisaggregationCalculator disaggCalc = new DisaggregationCalculator();
     // do not show progress bar if not desired by user
     disaggCalc.showProgressBar(this.progressCheckBox.isSelected());
     if(distanceControlPanel!=null)  calc.setMaxSourceDistance(distanceControlPanel.getDistance());
     int num = hazFunction.getNum();
     double disaggregationProb = this.disaggregationControlPanel.getDisaggregationProb();
     //if selected Prob is not within the range of the Exceed. prob of Hazard Curve function
    if(disaggregationProb > hazFunction.getY(0) || disaggregationProb < hazFunction.getY(num-1))
      JOptionPane.showMessageDialog(this,
                                    new String("Chosen Probability is not"+
                                    " within the range of the min and max prob."+
                                    " in the Hazard Curve"),
                                    "Disaggregation Prob. selection error message",
                                    JOptionPane.OK_OPTION);
    else{
      //gets the Disaggregation data
      double iml= hazFunction.getFirstInterpolatedX(disaggregationProb);
      disaggCalc.disaggregate(Math.log(iml),site,imr,(EqkRupForecast)eqkRupForecast);
      disaggregationString=disaggCalc.getResultsString();
    }
   }


   // add the function to the function list
   totalProbFuncs.add(hazFunction);

   // set the X-axis label
   totalProbFuncs.setXAxisName(imtGuiBean.getSelectedIMT());
   totalProbFuncs.setYAxisName("Probability of Exceedance");
  }


  /**
   * Handle the Eqk Forecast List.
   * @param site : Selected site
   * @param imr : selected IMR
   * @param eqkRupForecast : List of Eqk Rup forecasts
   */
  private void handleForecastList(Site site,
                                  AttenuationRelationshipAPI imr,
                                  EqkRupForecastAPI eqkRupForecast) {
   ERF_List erfList  = (ERF_List)eqkRupForecast;
   int numERFs = erfList.getNumERFs(); // get the num of ERFs in the list
   this.isEqkList = true; // set the flag to indicate thatwe are dealing with Eqk list
   // clear the function list
   totalProbFuncs.clear();
   // calculate the hazard curve
   HazardCurveCalculator calc = new HazardCurveCalculator();
   if(distanceControlPanel!=null) calc.setMaxSourceDistance(distanceControlPanel.getDistance());
   // do not show progress bar if not desired by user
   calc.showProgressBar(this.progressCheckBox.isSelected());

   // calculate hazard curve for each ERF within the list
   for(int i=0; i<numERFs; ++i) {
     ArbitrarilyDiscretizedFunc hazFunction = new ArbitrarilyDiscretizedFunc();
     // intialize the hazard function
     initX_Values(hazFunction);
     try {
       // calculate the hazard curve
       calc.getHazardCurve(hazFunction, site, imr, erfList.getERF(i));
       hazFunction.setInfo("\n"+getCurveParametersInfo()+"\n");
       hazFunction = toggleHazFuncLogValues(hazFunction);
     }catch (RuntimeException e) {
       JOptionPane.showMessageDialog(this, e.getMessage(),
                                     "Parameters Invalid", JOptionPane.INFORMATION_MESSAGE);
       e.printStackTrace();
       return;
     }
     totalProbFuncs.add(hazFunction);
   }


   // if fractile or average needs to be calculated
   if(!this.percentileOption.equalsIgnoreCase
      (ERF_EpistemicListControlPanel.NO_PERCENTILE) || this.avgSelected) {
     // set the function list and weights in the calculator
     if (fractileCalc==null)
       fractileCalc = new FractileCurveCalculator(totalProbFuncs,
           erfList.getRelativeWeightsList());
     else  fractileCalc.set(totalProbFuncs, erfList.getRelativeWeightsList());
   }

   if(!isAllCurves) totalProbFuncs.clear(); //if all curves are not needed to be drawn

   // if 5th, 50 and 95th percetile need to be plotted
   if(this.percentileOption.equalsIgnoreCase
      (ERF_EpistemicListControlPanel.FIVE_50_95_PERCENTILE)) {
     totalProbFuncs.add(fractileCalc.getFractile(.05)); // 5th fractile
     totalProbFuncs.add(fractileCalc.getFractile(.5)); // 50th fractile
     totalProbFuncs.add(fractileCalc.getFractile(.95)); // 95th fractile
   } else if(this.percentileOption.equalsIgnoreCase // for custom percentile
      (ERF_EpistemicListControlPanel.CUSTOM_PERCENTILE )) {
     double percentile = this.epistemicControlPanel.getCustomPercentileValue();
     totalProbFuncs.add(fractileCalc.getFractile(percentile/100));
   }
   // calculate average
   if(this.avgSelected) totalProbFuncs.add(fractileCalc.getMeanCurve());

   // set the X-axis label
   totalProbFuncs.setXAxisName(imtGuiBean.getSelectedIMT());
   totalProbFuncs.setYAxisName("Probability of Exceedance");
  }

  /**
   *
   * @returns the String containing the values selected for different parameters
   */
  public String getCurveParametersInfo(){
    return "IMR Param List: " +this.imrGuiBean.getParameterList().toString()+"\n"+
        "Site Param List: "+siteGuiBean.getParameterListEditor().getParameterList().toString()+"\n"+
        "IMT Param List: "+imtGuiBean.getParameterList().toString()+"\n"+
        "Forecast Param List: "+erfGuiBean.getParameterList().toString();
  }

  /**
   * Initialize the IMR Gui Bean
   */
  private void initIMR_GuiBean() {
    // create the IMR Gui Bean object
     // It accepts the vector of IMR class names
     Vector imrClasses = new Vector();
     //imrClasses.add(this.A_CLASS_NAME);
     imrClasses.add(this.AS_CLASS_NAME);
     imrClasses.add(this.BJF_CLASS_NAME);
     imrClasses.add(this.C_CLASS_NAME);
     imrClasses.add(this.SCEMY_CLASS_NAME);
     imrClasses.add(this.CB_CLASS_NAME);
     imrClasses.add(this.F_CLASS_NAME);
     imrGuiBean = new IMR_GuiBean(imrClasses);
     imrGuiBean.getParameterEditor(imrGuiBean.IMR_PARAM_NAME).getParameter().addParameterChangeListener(this);
     // show this gui bean the JPanel
     imrPanel.add(this.imrGuiBean,new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0,
         GridBagConstraints.CENTER, GridBagConstraints.BOTH, defaultInsets, 0, 0 ));
  }

  /**
   * Initialize the IMT Gui Bean
   */
  private void initIMT_GuiBean() {

     // get the selected IMR
     AttenuationRelationshipAPI imr = imrGuiBean.getSelectedIMR_Instance();
     // create the IMT Gui Bean object
     imtGuiBean = new IMT_GuiBean(imr);
     imtPanel.setLayout(gridBagLayout8);
     imtPanel.add(imtGuiBean, new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0,
               GridBagConstraints.CENTER, GridBagConstraints.BOTH, defaultInsets, 0, 0 ));

  }

  /**
   * Initialize the site gui bean
   */
  private void initSiteGuiBean() {

     // get the selected IMR
     AttenuationRelationshipAPI imr = imrGuiBean.getSelectedIMR_Instance();
     // create the Site Gui Bean object
     siteGuiBean = new Site_GuiBean();
     siteGuiBean.replaceSiteParams(imr.getSiteParamsIterator());
     // show the sitebean in JPanel
     sitePanel.add(this.siteGuiBean, new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0,
             GridBagConstraints.CENTER, GridBagConstraints.BOTH, defaultInsets, 0, 0 ));

  }


 /**
   * Initialize the ERF Gui Bean
   */
  private void initERF_GuiBean() {
     // create the ERF Gui Bean object
   Vector erf_Classes = new Vector();
   erf_Classes.add(PEER_FAULT_FORECAST_CLASS_NAME);
   erf_Classes.add(PEER_AREA_FORECAST_CLASS_NAME);
   erf_Classes.add(PEER_NON_PLANAR_FAULT_FORECAST_CLASS_NAME);
   erf_Classes.add(PEER_LISTRIC_FAULT_FORECAST_CLASS_NAME);
   erf_Classes.add(PEER_MULTI_SOURCE_FORECAST_CLASS_NAME);
   erf_Classes.add(PEER_LOGIC_TREE_FORECAST_CLASS_NAME);
   erf_Classes.add(FRANKEL_FORECAST_CLASS_NAME);
   erf_Classes.add(FRANKEL_ADJ_FORECAST_CLASS_NAME);
   erf_Classes.add(STEP_FORECAST_CLASS_NAME);
   erf_Classes.add(WG02_ERF_LIST_CLASS_NAME);
   erfGuiBean = new ERF_GuiBean(erf_Classes);
   erfPanel.setLayout(gridBagLayout5);
   erfPanel.add(erfGuiBean, new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0,
                GridBagConstraints.CENTER,GridBagConstraints.BOTH, defaultInsets, 0, 0 ));
   erfGuiBean.getParameterEditor(erfGuiBean.ERF_PARAM_NAME).getParameter().addParameterChangeListener(this);

  }

  /**
   * Initialize the site gui bean
   */
  private void initTimeSpanGuiBean() {

    /* get the selected ERF
     NOTE : We have used erfGuiBean.getSelectedERF_Instance()INSTEAD OF
     erfGuiBean.getSelectedERF.
    Dofference is that erfGuiBean.getSelectedERF_Instance() does not update
    the forecast while erfGuiBean.getSelectedERF updates the forecast
    */
    EqkRupForecastAPI eqkRupForecast = erfGuiBean.getSelectedERF_Instance();
    // create the TimeSpan Gui Bean object
    timeSpanGuiBean = new TimeSpanGuiBean(eqkRupForecast.getTimeSpan());
    // show the sitebean in JPanel
    this.timeSpanPanel.add(this.timeSpanGuiBean, new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0,
        GridBagConstraints.CENTER, GridBagConstraints.BOTH, defaultInsets, 0, 0 ));

  }

  /**
   * Initialize the items to be added to the control list
   */
  private void initControlList() {
    this.controlComboBox.addItem(CONTROL_PANELS);
    this.controlComboBox.addItem(PEER_TEST_CONTROL);
    this.controlComboBox.addItem(DISAGGREGATION_CONTROL);
    this.controlComboBox.addItem(AXIS_CONTROL);
    this.controlComboBox.addItem(DISTANCE_CONTROL);
    this.controlComboBox.addItem(SITES_OF_INTEREST_CONTROL);
     this.controlComboBox.addItem(CVM_CONTROL);
  }

  /**
   * This function is called when controls pick list is chosen
   * @param e
   */
  void controlComboBox_actionPerformed(ActionEvent e) {
    if(controlComboBox.getItemCount()<=0) return;
    String selectedControl = controlComboBox.getSelectedItem().toString();
    if(selectedControl.equalsIgnoreCase(this.PEER_TEST_CONTROL))
      initPEER_TestControl();
    else if(selectedControl.equalsIgnoreCase(this.DISAGGREGATION_CONTROL))
      initDisaggregationControl();
    else if(selectedControl.equalsIgnoreCase(this.EPISTEMIC_CONTROL))
      initEpistemicControl();
    else if(selectedControl.equalsIgnoreCase(this.AXIS_CONTROL))
      initAxisControl();
    else if(selectedControl.equalsIgnoreCase(this.DISTANCE_CONTROL))
      initDistanceControl();
    else if(selectedControl.equalsIgnoreCase(this.DISTANCE_CONTROL))
      initDistanceControl();
    else if(selectedControl.equalsIgnoreCase(this.SITES_OF_INTEREST_CONTROL))
      initSitesOfInterestControl();
    else if(selectedControl.equalsIgnoreCase(this.CVM_CONTROL))
      initCVMControl();

    controlComboBox.setSelectedItem(this.CONTROL_PANELS);
  }

  /**
   * Initialize the PEER Test control.
   * This function is called when user selects "Select Test and site"
   * from controls pick list
   */
  private void initPEER_TestControl() {
    //creating the instance of the PEER_TestParamSetter class which is extended from the
    //JComboBox, so it is like a control panel for creating the JComboBox containing the
    //name of different sets and the test cases
    //peerTestsParamSetter takes the instance of the hazardCurveGuiBean as its instance
    // distance control panel is needed here so that distance can be set for PEER cases
    if(distanceControlPanel==null) distanceControlPanel= new SetMinSourceSiteDistanceControlPanel(this);
    if(peerTestsControlPanel==null)
      peerTestsControlPanel=new PEER_TestCaseSelectorControlPanel(this,
          imrGuiBean, siteGuiBean, imtGuiBean, erfGuiBean, timeSpanGuiBean,
          this.distanceControlPanel);
    peerTestsControlPanel.pack();
    peerTestsControlPanel.show();
  }


  /**
   * Initialize the Disaggregation control.
   * This function is called when user selects "Disaggregation"
   * from controls pick list
   */
  private void initDisaggregationControl() {
    if(this.disaggregationControlPanel==null)
      disaggregationControlPanel = new DisaggregationControlPanel(this, this);
    disaggregationControlPanel.show();
  }

  /**
    * Initialize the Epistemic list control.
    * This function is called when user selects "ERF Epistemic Control"
    * from controls pick list
    */
   private void initEpistemicControl() {
     if(this.epistemicControlPanel==null)
       epistemicControlPanel = new ERF_EpistemicListControlPanel(this,this);
     epistemicControlPanel.show();
  }

  /**
   * Initialize the Min Source and site distance control.
   * This function is called when user selects "Source Site Distance Control"
   * from controls pick list
   */
  private void initDistanceControl() {
    if(this.distanceControlPanel==null)
      distanceControlPanel = new SetMinSourceSiteDistanceControlPanel(this);
    distanceControlPanel.pack();
    distanceControlPanel.show();
  }

  /**
   * Initialize the Interesting sites control panel
   * It will provide a pick list of interesting sites
   */
  private void initSitesOfInterestControl() {
    if(this.sitesOfInterest==null)
      sitesOfInterest = new SitesOfInterestControlPanel(this, this.siteGuiBean);
    sitesOfInterest.pack();
    sitesOfInterest.show();
  }

  /**
  * Initialize the Interesting sites control panel
  * It will provide a pick list of interesting sites
  */
  private void initCVMControl() {
    if(this.cvmControlPanel==null)
      cvmControlPanel = new SetSiteParamsFromCVMControlPanel(this, this.imrGuiBean, this.siteGuiBean);
    cvmControlPanel.pack();
    cvmControlPanel.show();
  }

  /**
   * Initialize the PEER Test control.
   * This function is called when user selects "Axis Control"
   * from controls pick list
   */
  private void initAxisControl() {
    if(xAxis==null || yAxis==null) {
      JOptionPane.showMessageDialog(this,AXIS_RANGE_NOT_ALLOWED);
      return;
    }
    Range rX = xAxis.getRange();
    Range rY= yAxis.getRange();
    double minX=rX.getLowerBound();
    double maxX=rX.getUpperBound();
    double minY=rY.getLowerBound();
    double maxY=rY.getUpperBound();
    if(this.customAxis) { // select the custom scale in the control window
      if(axisControlPanel == null)
        axisControlPanel=new AxisLimitsControlPanel(this, this,
            AxisLimitsControlPanel.CUSTOM_SCALE, minX,maxX,minY,maxY);
      else  axisControlPanel.setParams(AxisLimitsControlPanel.CUSTOM_SCALE,
                                       minX,maxX,minY,maxY);

    }
    else { // select the auto scale in the control window
      if(axisControlPanel == null)
        axisControlPanel=new AxisLimitsControlPanel(this, this,
            AxisLimitsControlPanel.AUTO_SCALE, minX,maxX,minY,maxY);
      else  axisControlPanel.setParams(AxisLimitsControlPanel.AUTO_SCALE,
                                       minX,maxX,minY,maxY);
    }
    axisControlPanel.pack();
    axisControlPanel.show();
  }



  /**
   * set x values in log space for Hazard Function to be passed to IMR
   * if the selected IMT are SA , PGA or PGV
   * It accepts 1 parameters
   *
   * @param originalFunc :  this is the function with X values set
   */
  private void initX_Values(DiscretizedFuncAPI arb){
    // take log only if it is PGA, PGV or SA
    if (isIMTLogEnabled()) {
      for(int i=0; i<this.xValues.length; ++i)
        arb.set(Math.log(xValues[i]),1 );
    } else
      throw new RuntimeException("Unsupported IMT");
  }

  /**
   * set x values back from the log space to the original linear values
   * for Hazard Function after completion of the Hazard Calculations
   * if the selected IMT are SA , PGA or PGV
   * It accepts 1 parameters
   *
   * @param hazFunction :  this is the function with X values set
   */
  private ArbitrarilyDiscretizedFunc toggleHazFuncLogValues(ArbitrarilyDiscretizedFunc hazFunc){
    int numPoints = hazFunc.getNum();
    DiscretizedFuncAPI tempFunc = hazFunc.deepClone();
    hazFunc = new ArbitrarilyDiscretizedFunc();
    // take log only if it is PGA, PGV or SA
    if (isIMTLogEnabled()) {
      for(int i=0; i<numPoints; ++i)
        hazFunc.set(xValues[i], tempFunc.getY(i));
    return hazFunc;
    } else
      throw new RuntimeException("Unsupported IMT");
  }



  /**
   * This function sets whether all curves are to drawn or only fractiles are to drawn
   * @param drawAllCurves :True if all curves are to be drawn else false
   */
  public void setPlotAllCurves(boolean drawAllCurves) {
    this.isAllCurves = drawAllCurves;
  }

  /**
   * This function sets the percentils option chosen by the user.
   * User can choose "No Percentile", "5th, 50th and 95th Percentile" or
   * "Custom Percentile"
   *
   * @param percentileOption : Option selected by the user. It can be set by
   * various constant String values in ERF_EpistemicListControlPanel
   */
  public void setPercentileOption(String percentileOption) {
    this.percentileOption = percentileOption;
  }

  /**
   * This function is needed to tell the applet whether avg is selected or not
   * This is called from ERF_EpistemicListControlPanel
   *
   * @param isAvgSelected : true if avg is selected else false
   */
  public void setAverageSelected(boolean isAvgSelected) {
    this.avgSelected = isAvgSelected;
  }

  /**
   * @return true if the selected IMT is PGA, PGV or SA
   * else returns false
   */
  private boolean isIMTLogEnabled(){
   String selectedIMT = imtGuiBean.getParameterList().getParameter(IMT_GuiBean.IMT_PARAM_NAME).getValue().toString();
    if(selectedIMT.equalsIgnoreCase(AttenuationRelationship.PGA_NAME) ||
       selectedIMT.equalsIgnoreCase(AttenuationRelationship.PGV_NAME) ||
       selectedIMT.equalsIgnoreCase(AttenuationRelationship.SA_NAME))
     return true;
    return false;
  }

}






