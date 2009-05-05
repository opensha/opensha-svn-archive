package org.opensha.sha.gui;

import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;

import java.util.ArrayList;
import java.util.ListIterator;
import java.rmi.RemoteException;


import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import java.io.*;


import org.jfree.data.Range;
import org.opensha.data.Site;
import org.opensha.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.data.function.DiscretizedFuncAPI;
import org.opensha.data.function.DiscretizedFuncList;
import org.opensha.exceptions.WarningException;

import org.opensha.param.ParameterAPI;
import org.opensha.param.ParameterConstraint;
import org.opensha.param.WarningDoubleParameter;
import org.opensha.param.event.ParameterChangeEvent;
import org.opensha.param.event.ParameterChangeListener;
import org.opensha.sha.calc.DisaggregationCalculator;
import org.opensha.sha.earthquake.ERF_List;
import org.opensha.sha.earthquake.EqkRupForecast;
import org.opensha.sha.earthquake.EqkRupForecastAPI;
import org.opensha.sha.gui.beans.ERF_GuiBean;
import org.opensha.sha.gui.beans.IMR_GuiBean;
import org.opensha.sha.gui.beans.IMR_GuiBeanAPI;
import org.opensha.sha.gui.beans.IMT_GuiBean;
import org.opensha.sha.gui.beans.Site_GuiBean;
import org.opensha.sha.gui.controls.CyberShakePlotFromDBControlPanel;
import org.opensha.sha.gui.controls.CyberShakeSiteSetterControlPanel;
import org.opensha.sha.gui.controls.DisaggregationControlPanel;
import org.opensha.sha.gui.controls.DisaggregationControlPanelAPI;
import org.opensha.sha.gui.controls.ERF_EpistemicListControlPanel;
import org.opensha.sha.gui.controls.ERF_EpistemicListControlPanelAPI;
import org.opensha.sha.gui.controls.PEER_TestCaseSelectorControlPanel;
import org.opensha.sha.gui.controls.PEER_TestCaseSelectorControlPanelAPI;
import org.opensha.sha.gui.controls.RunAll_PEER_TestCasesControlPanel;
import org.opensha.sha.gui.controls.SetMinSourceSiteDistanceControlPanel;
import org.opensha.sha.gui.controls.SetSiteParamsFromWebServicesControlPanel;
import org.opensha.sha.gui.controls.SiteDataControlPanel;
import org.opensha.sha.gui.controls.SitesOfInterestControlPanel;
import org.opensha.sha.gui.controls.X_ValuesInCurveControlPanel;
import org.opensha.sha.gui.controls.X_ValuesInCurveControlPanelAPI;
import org.opensha.sha.gui.controls.PlottingOptionControl;
import org.opensha.sha.gui.infoTools.ApplicationVersionInfoWindow;
import org.opensha.sha.gui.infoTools.ButtonControlPanel;
import org.opensha.sha.gui.infoTools.ButtonControlPanelAPI;
import org.opensha.sha.gui.infoTools.CalcProgressBar;
import org.opensha.sha.gui.infoTools.GraphPanel;
import org.opensha.sha.gui.infoTools.GraphPanelAPI;
import org.opensha.sha.gui.infoTools.GraphWindow;
import org.opensha.sha.gui.infoTools.GraphWindowAPI;
import org.opensha.sha.gui.infoTools.IMT_Info;
import org.opensha.sha.imr.AttenuationRelationshipAPI;
import org.opensha.util.FileUtils;
import org.opensha.util.ImageUtils;
import org.opensha.util.SystemPropertiesUtils;

import org.opensha.sha.calc.remoteCalc.RemoteHazardCurveClient;
import org.opensha.sha.calc.remoteCalc.RemoteDisaggregationCalcClient;
import org.opensha.sha.calc.HazardCurveCalculatorAPI;
import org.opensha.sha.calc.DisaggregationCalculatorAPI;
import org.opensha.sha.gui.infoTools.WeightedFuncListforPlotting;
import org.opensha.sha.earthquake.ERF_API;
import org.opensha.sha.gui.infoTools.PlotCurveCharacterstics;
import org.opensha.sha.gui.infoTools.ExceptionWindow;
import org.opensha.sha.gui.controls.XY_ValuesControlPanelAPI;
import org.opensha.sha.gui.controls.XY_ValuesControlPanel;
import java.awt.*;

import javax.swing.*;
import java.awt.event.*;
import org.opensha.sha.gui.controls.PlotColorAndLineTypeSelectorControlPanel;
import org.opensha.sha.gui.infoTools.DisaggregationPlotViewerWindow;
import org.opensha.sha.gui.beans.EqkRupSelectorGuiBean;
import org.opensha.sha.earthquake.ProbEqkRupture;
import org.opensha.sha.calc.HazardCurveCalculator;
import org.opensha.sha.gui.controls.CyberShakePlotControlPanelAPI;
import org.opensha.sha.gui.controls.CyberShakePlotControlPanel;
import org.opensha.sha.gui.beans.TimeSpanGuiBean;
import org.opensha.sha.gui.beans.EqkRupSelectorGuiBeanAPI;
import org.opensha.sha.gui.beans.EqkRuptureFromERFSelectorPanel;



/**
 * <p>Title: HazardCurveServerModeApplication</p>
 * <p>Description: This application computes Hazard Curve for selected
 * AttenuationRelationship model , Site and Earthquake Rupture Forecast (ERF)model.
 * This computed Hazard curve is shown in a panel using JFreechart.
 * This application works with/without internet connection.  If user using this
 * application has network connection then it creates the instances of ERF on server
 * and make all calls to server for any forecast updation. All the computation
 * in this application is done using the server. Once the computations complete, it
 * returns back the result.
 * All the server client relationship has been established using RMI, which allows
 * to make simple calls to the server similar to if things are existing on user's
 * own machine.
 * If network connection is not available to user then it will create all the
 * objects on users local machine and do all computation there itself.</p>
 * @author Nitin Gupta and Vipin Gupta
 * Date : Sept 23 , 2002
 * @version 1.0
 */

public class HazardCurveServerModeApplication extends JFrame
    implements Runnable,  ParameterChangeListener,
    DisaggregationControlPanelAPI, ERF_EpistemicListControlPanelAPI ,
    X_ValuesInCurveControlPanelAPI, PEER_TestCaseSelectorControlPanelAPI,
    ButtonControlPanelAPI,GraphPanelAPI,GraphWindowAPI,XY_ValuesControlPanelAPI,
    CyberShakePlotControlPanelAPI, IMR_GuiBeanAPI{

  /**
   * Name of the class
   */
  private final static String C = "HazardCurveServerModeApplication";
  // for debug purpose
  protected final static boolean D = false;



  /**
   *  The object class names for all the supported Eqk Rup Forecasts
   */
  public final static String RMI_FRANKEL_ADJ_FORECAST_CLASS_NAME = "org.opensha.sha.earthquake.rupForecastImpl.remoteERF_Clients.Frankel96_AdjustableEqkRupForecastClient";
  public final static String RMI_STEP_FORECAST_CLASS_NAME = "org.opensha.sha.earthquake.rupForecastImpl.remoteERF_Clients.STEP_EqkRupForecastClient";
  public final static String RMI_STEP_ALASKA_ERF_CLASS_NAME = "org.opensha.sha.earthquake.rupForecastImpl.remoteERF_Clients.STEP_AlaskanPipeForecastClient";
  public final static String RMI_FLOATING_POISSON_FAULT_ERF_CLASS_NAME = "org.opensha.sha.earthquake.rupForecastImpl.remoteERF_Clients.FloatingPoissonFaultERF_Client";
  public final static String RMI_FRANKEL02_ADJ_FORECAST_CLASS_NAME="org.opensha.sha.earthquake.rupForecastImpl.remoteERF_Clients.Frankel02_AdjustableEqkRupForecastClient";
  public final static String RMI_PEER_AREA_FORECAST_CLASS_NAME = "org.opensha.sha.earthquake.rupForecastImpl.remoteERF_Clients.PEER_AreaForecastClient";
  public final static String RMI_PEER_NON_PLANAR_FAULT_FORECAST_CLASS_NAME = "org.opensha.sha.earthquake.rupForecastImpl.remoteERF_Clients.PEER_NonPlanarFaultForecastClient";
  public final static String RMI_PEER_MULTI_SOURCE_FORECAST_CLASS_NAME = "org.opensha.sha.earthquake.rupForecastImpl.remoteERF_Clients.PEER_MultiSourceForecastClient";
  public final static String RMI_POINT2MULT_VSS_FORECAST_CLASS_NAME="org.opensha.sha.earthquake.rupForecastImpl.remoteERF_Clients.Point2MultVertSS_FaultERF_Client";
  public final static String RMI_POISSON_FAULT_ERF_CLASS_NAME = "org.opensha.sha.earthquake.rupForecastImpl.remoteERF_Clients.PoissonFaultERF_Client";
  public final static String RMI_WG02_ERF_LIST_CLASS_NAME = "org.opensha.sha.earthquake.rupForecastImpl.remoteERF_Clients.WG02_FortranWrappedERF_EpistemicListClient";
  public final static String RMI_PEER_LOGIC_TREE_ERF_LIST_CLASS_NAME = "org.opensha.sha.earthquake.rupForecastImpl.remoteERF_Clients.PEER_LogicTreeERF_ListClient";
  public final static String RMI_POINT2MULT_VSS_ERF_LIST_CLASS_NAME="org.opensha.sha.earthquake.rupForecastImpl.remoteERF_Clients.Point2MultVertSS_FaultERF_ListClient";
  public final static String RMI_WG02_ERF_CLASS_NAME = "org.opensha.sha.earthquake.rupForecastImpl.remoteERF_Clients.WG02_EqkRupForecastClient";
  public final static String RMI_WGCEP_UCERF1_ERF_CLASS_NAME = "org.opensha.sha.earthquake.rupForecastImpl.remoteERF_Clients.WGCEP_UCERF1_EqkRupForecastClient";



   //Strings for choosing ERFGuiBean or ERF_RupSelectorGUIBean
  protected final static String PROBABILISTIC = "Probabilistic";
  protected final static String DETERMINISTIC = "Deterministic";


  // instances of the GUI Beans which will be shown in this applet
  protected ERF_GuiBean erfGuiBean;
  protected IMR_GuiBean imrGuiBean;
  private IMT_GuiBean imtGuiBean;
  protected Site_GuiBean siteGuiBean;
  protected EqkRupSelectorGuiBean erfRupSelectorGuiBean;


  //instance for the ButtonControlPanel
  ButtonControlPanel buttonControlPanel;

  //instance of the GraphPanel (window that shows all the plots)
  GraphPanel graphPanel;

  //instance of the GraphWindow to pop up when the user wants to "Peel-Off" curves;
  GraphWindow graphWindow;

  //X and Y Axis  when plotting tha Curves Name
  protected String xAxisName;
  protected String yAxisName;


  // Strings for control pick list
  protected final static String CONTROL_PANELS = "Control Panels";
  private final static String PEER_TEST_CONTROL = "PEER Test Case Selector";
  protected final static String DISAGGREGATION_CONTROL = "Disaggregation";
  protected final static String EPISTEMIC_CONTROL = "Epistemic List Control";
  protected final static String DISTANCE_CONTROL = "Max Source-Site Distance";
  protected final static String SITES_OF_INTEREST_CONTROL = "Sites of Interest";
  protected final static String CVM_CONTROL = "Set Site Params from Web Services";
  protected final static String X_VALUES_CONTROL = "Set X values for Hazard Curve Calc.";
  private final static String RUN_ALL_PEER_TESTS = "Run all PEER Test Cases";
  //private final static String MAP_CALC_CONTROL = "Select Map Calcution Method";
  protected final static String PLOTTING_OPTION = "Set new dataset plotting option";
  protected final static String XY_Values_Control = "Set external XY dataset";
  private final static String PLOT_CYBERSHAKE_DATASET_CONTROL = "Plot Cybershake data";
  private final static String CYBERSHAKE_SITE_CONTROL = "CyberShake Sites";


  // objects for control panels
  protected PEER_TestCaseSelectorControlPanel peerTestsControlPanel;
  protected DisaggregationControlPanel disaggregationControlPanel;
  protected ERF_EpistemicListControlPanel epistemicControlPanel;
  protected SetMinSourceSiteDistanceControlPanel distanceControlPanel;
  protected SitesOfInterestControlPanel sitesOfInterest;
  protected SiteDataControlPanel cvmControlPanel;
  protected X_ValuesInCurveControlPanel xValuesPanel;
  protected RunAll_PEER_TestCasesControlPanel runAllPEER_Tests;
  protected PlottingOptionControl plotOptionControl;
  protected XY_ValuesControlPanel xyPlotControl;
  protected CyberShakePlotFromDBControlPanel cyberControlPanel;
  protected CyberShakeSiteSetterControlPanel cyberSiteControlPanel;

  private Insets plotInsets = new Insets( 4, 10, 4, 4 );

  private Border border1;


  //log flags declaration
  private boolean xLog =false;
  private boolean yLog =false;

  // default insets
  protected Insets defaultInsets = new Insets( 4, 4, 4, 4 );

  // height and width of the applet
  protected final static int W = 1100;
  protected final static int H = 770;

  /**
   * List of ArbitrarilyDiscretized functions and Weighted funstions
   */
  protected ArrayList functionList = new ArrayList();

  //holds the ArbitrarilyDiscretizedFunc
  protected ArbitrarilyDiscretizedFunc function;

  //instance to get the default IMT X values for the hazard Curve
  protected IMT_Info imtInfo = new IMT_Info();

  // variable needed for plotting Epistemic list
  protected boolean isEqkList = false; // whther we are plottin the Eqk List
  //private boolean isIndividualCurves = false; //to keep account that we are first drawing the individual curve for erf in the list
  protected boolean isAllCurves = true; // whether to plot all curves
  // whether user wants to plot custom fractile
  protected String fractileOption ;
  // whether avg is selected by the user
  protected boolean avgSelected = false;

  //Variables required to update progress bar if ERF List is selected
  //total number of ERF's in list
  protected int numERFsInEpistemicList =0;
  //index number of ERF for which Hazard Curve is being calculated
  protected int currentERFInEpistemicListForHazardCurve =0;


  /**
   * these four values save the custom axis scale specified by user
   */
  private double minXValue;
  private double maxXValue;
  private  double minYValue;
  private double maxYValue;
  private boolean customAxis = false;


  private JComboBox probDeterSelection = new JComboBox();



  //flags to check which X Values the user wants to work with: default or custom
  boolean useCustomX_Values = false;


  //flag to check for the disaggregation functionality
  protected boolean disaggregationFlag= false;
  private String disaggregationString;



  //checks if Deterministic or Probabilistic Calculations
  protected boolean isProbCurve = true;


  // PEER Test Cases
  protected String TITLE = new String("Hazard Curves");


  private JPanel jPanel1 = new JPanel();
  private Border border2;
  private final static Dimension COMBO_DIM = new Dimension( 180, 30 );
  private final static Dimension BUTTON_DIM = new Dimension( 80, 20 );
  private Border border3;
  private Border border4;
  private Border border5;
  private Border border6;
  private Border border7;
  private Border border8;



  private final static String POWERED_BY_IMAGE = "PoweredByOpenSHA_Agua.jpg";

  //static string for the OPENSHA website
  private final static String OPENSHA_WEBSITE="http://www.OpenSHA.org";

  JSplitPane topSplitPane = new JSplitPane();
  JButton clearButton = new JButton();
  JPanel buttonPanel = new JPanel();
  JCheckBox progressCheckBox = new JCheckBox();
  JButton addButton = new JButton();
  protected JComboBox controlComboBox = new JComboBox();
  JSplitPane chartSplit = new JSplitPane();
  JPanel panel = new JPanel();
  GridBagLayout gridBagLayout9 = new GridBagLayout();
  GridBagLayout gridBagLayout8 = new GridBagLayout();
  JSplitPane imrSplitPane = new JSplitPane();
  GridBagLayout gridBagLayout5 = new GridBagLayout();

  JPanel sitePanel = new JPanel();
  JPanel imtPanel = new JPanel();
  JSplitPane controlsSplit = new JSplitPane();
  JTabbedPane paramsTabbedPane = new JTabbedPane();
  protected JPanel erfPanel = new JPanel();
  GridBagLayout gridBagLayout15 = new GridBagLayout();
  GridBagLayout gridBagLayout13 = new GridBagLayout();
  GridBagLayout gridBagLayout12 = new GridBagLayout();
  protected JPanel imrPanel = new JPanel();
  GridBagLayout gridBagLayout10 = new GridBagLayout();
  BorderLayout borderLayout1 = new BorderLayout();

  //instances of various calculators
  protected HazardCurveCalculatorAPI calc;
  protected DisaggregationCalculatorAPI disaggCalc;
  CalcProgressBar progressClass;
  CalcProgressBar disaggProgressClass;
  protected CalcProgressBar startAppProgressClass;
  //timer threads to show the progress of calculations
  Timer timer;
  Timer disaggTimer;
  //calculation thead
  Thread calcThread;
  //checks to see if HazardCurveCalculations are done
  boolean isHazardCalcDone= false;
  private JButton peelOffButton = new JButton();
  JMenuBar menuBar = new JMenuBar();
  JMenu fileMenu = new JMenu();

  JMenuItem fileExitMenu = new JMenuItem();
  JMenuItem fileSaveMenu = new JMenuItem();
  JMenuItem filePrintMenu = new JCheckBoxMenuItem();
  JToolBar jToolBar = new JToolBar();

  JButton closeButton = new JButton();
  ImageIcon closeFileImage = new ImageIcon(ImageUtils.loadImage("closeFile.png"));

  JButton printButton = new JButton();
  ImageIcon printFileImage = new ImageIcon(ImageUtils.loadImage("printFile.jpg"));

  JButton saveButton = new JButton();
  ImageIcon saveFileImage = new ImageIcon(ImageUtils.loadImage("saveFile.jpg"));


  private JLabel imgLabel = new JLabel(new ImageIcon(ImageUtils.loadImage(this.POWERED_BY_IMAGE)));



  //maintains which ERFList was previously selected
  protected String prevSelectedERF_List = null;

  //keeps track which was the last selected Weighted function list.
  //It only initialises this weighted function list if user wants to add data to the existing ERF_List
  protected WeightedFuncListforPlotting weightedFuncList;

  /**this boolean keeps track when to plot the new data on top of other and when to
  *add to the existing data.
  * If it is true then add new data on top of existing data, but if it is false
  * then add new data to the existing data(this option only works if it is ERF_List).
  * */
  boolean addData= true;
  protected JButton cancelCalcButton = new JButton();
  private FlowLayout flowLayout1 = new FlowLayout();

  protected final static String version = "0.0.17";

  protected final static String versionURL = "http://www.opensha.org/applications/hazCurvApp/HazardCurveApp_Version.txt";
  protected final static String appURL = "http://www.opensha.org/applications/hazCurvApp/HazardCurveServerModeApp.jar";
  protected final static String versionUpdateInfoURL = "http://www.opensha.org/applications/hazCurvApp/versionUpdate.html";




  //Construct the applet
  public HazardCurveServerModeApplication() {

  }
  //Initialize the applet
  public void init() {
    try {

      // initialize the control pick list
      initControlList();
      //initialise the list to make selection whether to show ERF_GUIBean or ERF_RupSelectorGuiBean
      initProbOrDeterList();
      // initialize the GUI components
      startAppProgressClass = new CalcProgressBar("Starting Application", "Initializing Application .. Please Wait");
      jbInit();
      
      
      // initialize the various GUI beans
      initIMR_GuiBean();
      initIMT_GuiBean();
      initSiteGuiBean();
      try{
        initERF_GuiBean();
      }catch(RuntimeException e){
        JOptionPane.showMessageDialog(this,"Connection to ERF's failed","Internet Connection Problem",
                                      JOptionPane.OK_OPTION);
        e.printStackTrace();
        startAppProgressClass.dispose();
        System.exit(0);
      }
    }
    catch(Exception e) {
      ExceptionWindow bugWindow = new ExceptionWindow(this,e,"Exception occured while creating the GUI.\n"+
          "No Parameters have been set");
      bugWindow.setVisible(true);
      bugWindow.pack();
      //e.printStackTrace();
    }
    startAppProgressClass.dispose();
    ((JPanel)getContentPane()).updateUI();
  }

  //Component initialization
  protected void jbInit() throws Exception {
    border1 = BorderFactory.createLineBorder(SystemColor.controlText,1);
    border2 = BorderFactory.createLineBorder(SystemColor.controlText,1);
    border3 = BorderFactory.createEmptyBorder();
    border4 = BorderFactory.createLineBorder(SystemColor.controlText,1);
    border5 = BorderFactory.createLineBorder(SystemColor.controlText,1);
    border6 = BorderFactory.createBevelBorder(BevelBorder.RAISED,Color.white,Color.white,new Color(98, 98, 112),new Color(140, 140, 161));
    border7 = BorderFactory.createBevelBorder(BevelBorder.RAISED,Color.white,Color.white,new Color(98, 98, 112),new Color(140, 140, 161));
    border8 = BorderFactory.createBevelBorder(BevelBorder.RAISED,Color.white,Color.white,new Color(98, 98, 112),new Color(140, 140, 161));

    this.getContentPane().setLayout(borderLayout1);



    fileMenu.setText("File");
    fileExitMenu.setText("Exit");
    fileSaveMenu.setText("Save");
    filePrintMenu.setText("Print");

    fileExitMenu.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        fileExitMenu_actionPerformed(e);
      }
    });

    fileSaveMenu.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        fileSaveMenu_actionPerformed(e);
      }
    });

    filePrintMenu.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        filePrintMenu_actionPerformed(e);
      }
    });

    closeButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent actionEvent) {
        closeButton_actionPerformed(actionEvent);
      }
    });
    printButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent actionEvent) {
        printButton_actionPerformed(actionEvent);
      }
    });
    saveButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent actionEvent) {
        saveButton_actionPerformed(actionEvent);
      }
    });


    menuBar.add(fileMenu);
    fileMenu.add(fileSaveMenu);
    fileMenu.add(filePrintMenu);
    fileMenu.add(fileExitMenu);

    setJMenuBar(menuBar);
    closeButton.setIcon(closeFileImage);
    closeButton.setToolTipText("Exit Application");
    Dimension d = closeButton.getSize();
    jToolBar.add(closeButton);
    printButton.setIcon(printFileImage);
    printButton.setToolTipText("Print Graph");
    printButton.setSize(d);
    jToolBar.add(printButton);
    saveButton.setIcon(saveFileImage);
    saveButton.setToolTipText("Save Graph as image");
    saveButton.setSize(d);
    jToolBar.add(saveButton);
    jToolBar.setFloatable(false);

    this.getContentPane().add(jToolBar, BorderLayout.NORTH);

    jPanel1.setLayout(gridBagLayout10);

    //creating the Object the GraphPaenl class
    graphPanel = new GraphPanel(this);

    jPanel1.setBackground(Color.white);
    jPanel1.setBorder(border4);
    jPanel1.setMinimumSize(new Dimension(959, 600));
    jPanel1.setPreferredSize(new Dimension(959, 600));

    //loading the OpenSHA Logo

    topSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);

    clearButton.setText("Clear Plot");
    clearButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        clearButton_actionPerformed(e);
      }
    });

    buttonPanel.setMinimumSize(new Dimension(568, 20));
    buttonPanel.setLayout(flowLayout1);

    progressCheckBox.setFont(new java.awt.Font("Dialog", 1, 12));

    progressCheckBox.setSelected(true);
    progressCheckBox.setText("Show Progress Bar");

    addButton.setText("Compute");
    addButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        addButton_actionPerformed(e);
      }
    });

    controlComboBox.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        controlComboBox_actionPerformed(e);
      }
    });
    panel.setLayout(gridBagLayout9);
    panel.setBackground(Color.white);
    panel.setBorder(border5);
    panel.setMinimumSize(new Dimension(0, 0));
    imrSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
    imrSplitPane.setBottomComponent(imtPanel);
    imrSplitPane.setTopComponent(imrPanel);

    sitePanel.setLayout(gridBagLayout13);
    sitePanel.setBackground(Color.white);
    imtPanel.setLayout(gridBagLayout8);
    imtPanel.setBackground(Color.white);
    controlsSplit.setDividerSize(5);
    erfPanel.setLayout(gridBagLayout5);
    erfPanel.setBackground(Color.white);
    erfPanel.setBorder(border2);
    erfPanel.setMaximumSize(new Dimension(2147483647, 10000));
    erfPanel.setMinimumSize(new Dimension(2, 300));
    erfPanel.setPreferredSize(new Dimension(2, 300));
    imrPanel.setLayout(gridBagLayout15);
    imrPanel.setBackground(Color.white);
    chartSplit.setLeftComponent(panel);
    chartSplit.setRightComponent(paramsTabbedPane);

    probDeterSelection.addItemListener(new java.awt.event.ItemListener() {
          public void itemStateChanged(ItemEvent e) {
            probDeterSelection_itemStateChanged(e);
          }
    });

    peelOffButton.setText("Peel Off");
    peelOffButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        peelOffButton_actionPerformed(e);
      }
    });


    /*imgLabel.addMouseListener(new java.awt.event.MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        imgLabel_mouseClicked(e);
      }
    });*/
    cancelCalcButton.setText("Cancel");
    cancelCalcButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        cancelCalcButton_actionPerformed(e);
      }
    });
    this.getContentPane().add(jPanel1, BorderLayout.CENTER);
    jPanel1.add(topSplitPane,  new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(11, 4, 5, 6), 243, 231));

    //object for the ButtonControl Panel
    buttonControlPanel = new ButtonControlPanel(this);

    buttonPanel.add(probDeterSelection, 0);
    buttonPanel.add(controlComboBox, 1);
    buttonPanel.add(addButton, 2);
    buttonPanel.add(cancelCalcButton, 3);
    buttonPanel.add(clearButton, 4);
    buttonPanel.add(peelOffButton, 5);
    buttonPanel.add(progressCheckBox, 6);
    buttonPanel.add(buttonControlPanel, 7);
    buttonPanel.add(imgLabel, 8);

    //making the cancel button not visible until user has started to do the calculation
    cancelCalcButton.setVisible(false);

    topSplitPane.add(chartSplit, JSplitPane.TOP);
    chartSplit.add(panel, JSplitPane.LEFT);
    chartSplit.add(paramsTabbedPane, JSplitPane.RIGHT);
    imrSplitPane.add(imrPanel, JSplitPane.TOP);
    imrSplitPane.add(imtPanel, JSplitPane.BOTTOM);
    controlsSplit.add(imrSplitPane, JSplitPane.LEFT);
    paramsTabbedPane.add(controlsSplit, "IMR, IMT & Site");
    controlsSplit.add(sitePanel, JSplitPane.RIGHT);
    paramsTabbedPane.add(erfPanel, "ERF & Time Span");
    topSplitPane.add(buttonPanel, JSplitPane.BOTTOM);
    topSplitPane.setDividerLocation(590);
    imrSplitPane.setDividerLocation(300);

    controlsSplit.setDividerLocation(230);
    erfPanel.setLayout(gridBagLayout5);
    erfPanel.validate();
    erfPanel.repaint();
    chartSplit.setDividerLocation(590);
    this.setSize(W,H);
    Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
    this.setLocation((dim.width - this.getSize().width) / 2, 0);
    //EXIT_ON_CLOSE == 3
    this.setDefaultCloseOperation(3);
    this.setTitle("Hazard Curve Application ("+getAppVersion()+" )");

  }

  //Get Applet information
  public String getAppletInfo() {
    return "Hazard Curves Applet";
  }


	  /**
	   * Checks if the current version of the application is latest else direct the
	   * user to the latest version on the website.
	   */
	  protected void checkAppVersion(){
	      ArrayList hazCurveVersion = null;
	      try {
	    	  hazCurveVersion = FileUtils.loadFile(new URL(versionURL));
	      }
	      catch (Exception ex1) {
	        return;
	      }
	      String appVersionOnWebsite = (String)hazCurveVersion.get(0);
	      if(!appVersionOnWebsite.trim().equals(version.trim())){
	        try{
	          ApplicationVersionInfoWindow messageWindow =
	              new ApplicationVersionInfoWindow(appURL,
	                                               this.versionUpdateInfoURL,
	                                               "App Version Update", this);
	          Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
	          messageWindow.setLocation( (dim.width -
	                                      messageWindow.getSize().width) / 2,
	                                    (dim.height -
	                                     messageWindow.getSize().height) / 2);
	          messageWindow.setVisible(true);
	        }catch(Exception e){
	          e.printStackTrace();
	        }
	      }

	    return;

	  }


	  /**
	   * Returns the Application version
	   * @return String
	   */
	  public static String getAppVersion(){
	    return version;
	  }



	  //Main method
	  public static void main(String[] args) {
	    HazardCurveServerModeApplication applet = new HazardCurveServerModeApplication();
	    applet.checkAppVersion();
	    applet.init();
	    applet.setVisible(true);
	  }

	  //static initializer for setting look & feel
	  static {
	    String osName = System.getProperty("os.name");
	    try {
	        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
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
      graphPanel.drawGraphPanel(xAxisName,yAxisName,functionList,xLog,yLog,customAxis,TITLE,buttonControlPanel);
      togglePlot();
      //this.isIndividualCurves = false;
   }

   //checks if the user has plot the data window or plot window
   public void togglePlot(){
     panel.removeAll();
     graphPanel.togglePlot(buttonControlPanel);
     panel.add(graphPanel, new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0
            , GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 0, 0, 0, 0 ), 0, 0 ));
     panel.validate();
     panel.repaint();
   }

    /**
     * this function is called when Add Graph button is clicked
     * @param e
     */
    void addButton_actionPerformed(ActionEvent e) {
      if(this.runAllPEER_Tests !=null){
        if(this.runAllPEER_Tests.runAllPEER_TestCases()){
          try{
            progressCheckBox.setSelected(false);
            String peerDirName = "PEER_TESTS/";
            //creating the peer directory in which we put all the peer related files
            File peerDir = new File(peerDirName);
            if(!peerDir.isDirectory()) { // if main directory does not exist
              boolean success = (new File(peerDirName)).mkdir();
            }

//            ArrayList testCases = this.peerTestsControlPanel.getPEER_SetTwoTestCasesNames();
            ArrayList testCases = this.peerTestsControlPanel.getPEER_SetOneTestCasesNames();

            int size = testCases.size();
            /*if(epistemicControlPanel == null)
              epistemicControlPanel = new ERF_EpistemicListControlPanel(this,this);
            epistemicControlPanel.setCustomFractileValue(05);
            epistemicControlPanel.setVisible(false); */
            //System.out.println("size="+testCases.size());
            setAverageSelected(true);
            /* size=106 for Set 1
             * 	  Case1:  0-6
             *    Case2:  7-13
             *    Case3:  14-20
             *    Case4:  21-27
             *    Case5   28-34
             * 	  Case6:  35-41
             *    Case7:  42-48
             *    Case8a: 49-55
             *    Case8b: 56-62
             *    Case8c: 63-69
             *    Case9a: 70-76
             *    Case9b: 77-83
             *    Case9c: 84-90
             *    Case10: 91-95
             *    Case11: 96-99
             *    Case12: 100-106
             *    
             *    DOING ALL TAKES ~24 HOURS?
             */
          for(int i=0 ;i < size; ++i){
//            for(int i=35 ;i < 35; ++i){
              System.out.println("Working on # "+(i+1)+" of "+size);

              // first do PGA
              peerTestsControlPanel.setTestCaseAndSite((String)testCases.get(i));
              addButton();

              FileWriter peerFile=new FileWriter(peerDirName+(String)testCases.get(i)+"-PGA_OpenSHA.txt");
              DiscretizedFuncAPI func = (DiscretizedFuncAPI)functionList.get(0);
              for(int j=0; j<func.getNum();++j)
                peerFile.write(func.get(j).getX()+"\t"+func.get(j).getY()+"\n");
              peerFile.close();
              this.clearPlot(true);

              // now do SA
              /*imtGuiBean.getParameterList().getParameter(IMT_GuiBean.IMT_PARAM_NAME).setValue(AttenuationRelationship.SA_NAME);
              imtGuiBean.getParameterList().getParameter(AttenuationRelationship.PERIOD_NAME).setValue(new Double(1.0));
              addButton();
              peerFile = new FileWriter(peerDirName+(String)testCasesTwo.get(i)+"-1secSA_OpenSHA.dat");
              for(int j=0; j<totalProbFuncs.get(0).getNum();++j)
                peerFile.write(totalProbFuncs.get(0).get(j).getX()+" "+totalProbFuncs.get(0).get(j).getY()+"\n");
              peerFile.close();
              this.clearPlot(true);*/

            }
            System.exit(101);
            //peerResultsFile.close();
          }catch(Exception ee){
            ExceptionWindow bugWindow = new ExceptionWindow(this,ee,getParametersInfoAsString());
            bugWindow.setVisible(true);
            bugWindow.pack();
          }
        }
      }
      else{
        cancelCalcButton.setVisible(true);
        addButton();
      }
    }


    /**
     * Implementing the run method in the Runnable interface that creates a new thread
     * to do Hazard Curve Calculation, this thread created is seperate from the
     * timer thread, so that progress bar updation does not conflicts with Calculations.
     */
    public void run() {
      try{
        computeHazardCurve();
        cancelCalcButton.setVisible(false);
        //disaggCalc = null;
        calcThread = null;
      }catch(Exception e){
        e.printStackTrace();
        ExceptionWindow bugWindow = new ExceptionWindow(this,e,getParametersInfoAsString());
        bugWindow.setVisible(true);
        bugWindow.pack();
      }

    }

    /**
     * This method creates the HazardCurveCalc and Disaggregation Calc(if selected) instances.
     * If the internet connection is available then it creates a remote instances of
     * the calculators on server where the calculations take place, else
     * calculations are performed on the user's own machine.
     */
    protected void createCalcInstance(){
      if(isProbCurve)
        calc = (new RemoteHazardCurveClient()).getRemoteHazardCurveCalc();
      else if(calc == null && !isProbCurve){
        try {
          calc = new HazardCurveCalculator();
        }
        catch (Exception ex) {
          ExceptionWindow bugWindow = new ExceptionWindow(this,
              ex, this.getParametersInfoAsString());
          bugWindow.setVisible(true);
          bugWindow.pack();
        }
      }
      if(disaggregationFlag)
          disaggCalc = (new RemoteDisaggregationCalcClient()).getRemoteDisaggregationCalc();
    }

    /**
     * this function is called to draw the graph
     */
    protected void addButton() {
      setButtonsEnable(false);
      // do not show warning messages in IMR gui bean. this is needed
      // so that warning messages for site parameters are not shown when Add graph is clicked
      imrGuiBean.showWarningMessages(false);
      if(plotOptionControl !=null){
        if(this.plotOptionControl.getSelectedOption().equals(PlottingOptionControl.PLOT_ON_TOP))
          addData = true;
        else
          addData = false;
      }
      try{
          createCalcInstance();
      }catch(Exception e){
        setButtonsEnable(true);
        ExceptionWindow bugWindow = new ExceptionWindow(this,e,getParametersInfoAsString());
        bugWindow.setVisible(true);
        bugWindow.pack();
        e.printStackTrace();
      }

      // check if progress bar is desired and set it up if so
      if(this.progressCheckBox.isSelected())  {
        calcThread = new Thread(this);
        calcThread.start();
        timer = new Timer(200, new ActionListener() {
          public void actionPerformed(ActionEvent evt) {
            try{
              if(!isEqkList){
                int totRupture = calc.getTotRuptures();
                int currRupture = calc.getCurrRuptures();
                boolean totCurCalculated = true;
                if(currRupture ==-1){
                	progressClass.setProgressMessage("Please wait, calculating total rutures ....");
                	totCurCalculated = false;
                }
                if(!isHazardCalcDone && totCurCalculated)
                  progressClass.updateProgress(currRupture, totRupture);
              }
              else{
                if((numERFsInEpistemicList) !=0)
                  progressClass.updateProgress(currentERFInEpistemicListForHazardCurve,numERFsInEpistemicList);
              }
              if (isHazardCalcDone) {
                timer.stop();
                progressClass.dispose();
                drawGraph();
              }
            }catch(Exception e){
              //e.printStackTrace();
              timer.stop();
              setButtonsEnable(true);
              ExceptionWindow bugWindow = new ExceptionWindow(getApplicationComponent(),e,getParametersInfoAsString());
              bugWindow.setVisible(true);
              bugWindow.pack();
            }
          }
        });

        // timer for disaggregation progress bar
        disaggTimer = new Timer(200, new ActionListener() {
          public void actionPerformed(ActionEvent evt) {
            try{
              int totalRupture = disaggCalc.getTotRuptures();
              int currRupture = disaggCalc.getCurrRuptures();
              boolean calcDone = disaggCalc.done();
              if(!calcDone)
                disaggProgressClass.updateProgress(currRupture, totalRupture);
              if(calcDone){
                disaggTimer.stop();
                disaggProgressClass.dispose();
              }
            }catch(Exception e){
              disaggTimer.stop();
              setButtonsEnable(true);
              ExceptionWindow bugWindow = new ExceptionWindow(getApplicationComponent(),e,getParametersInfoAsString());
              bugWindow.setVisible(true);
              bugWindow.pack();
            }
          }
        });

       }
      else {
        this.computeHazardCurve();
        this.drawGraph();
      }
    }


    /**
     *
     * @returns the application component
     */
    protected Component getApplicationComponent(){
      return this;
    }

    /**
     * to draw the graph
     */
    protected void drawGraph() {
      // you can show warning messages now
     imrGuiBean.showWarningMessages(true);
     addGraphPanel();
     if(!disaggregationFlag)
       setButtonsEnable(true);
    }

    /**
     * plots the curves with defined color,line width and shape.
     *
     */
    public void plotGraphUsingPlotPreferences(){
      drawGraph();
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

    if( clearFunctions){
      graphPanel.removeChartAndMetadata();
      panel.removeAll();
      functionList.clear();
    }
    customAxis = false;
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
    drawGraph();

  }

  /**
   * set the auto range for the axis. This function is called
   * from the AxisLimitControlPanel
   */
  public void setAutoRange() {
    this.customAxis=false;
    drawGraph();
  }

  /**
   * This function to specify whether disaggregation is selected or not
   * @param isSelected : True if disaggregation is selected , else false
   */
  public void setDisaggregationSelected(boolean isSelected) {
    disaggregationFlag = isSelected;
  }



  /*void imgLabel_mouseClicked(MouseEvent e) {
    try{
      this.getAppletContext().showDocument(new URL(OPENSHA_WEBSITE), "new_peer_win");
    }catch(java.net.MalformedURLException ee){
      JOptionPane.showMessageDialog(this,new String("No Internet Connection Available"),
                                    "Error Connecting to Internet",JOptionPane.OK_OPTION);
      return;
    }
  }*/


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
      imtGuiBean.setIM(imr,imr.getSupportedIntensityMeasuresIterator());
      imtGuiBean.validate();
      imtGuiBean.repaint();
      siteGuiBean.replaceSiteParams(imr.getSiteParamsIterator());
      siteGuiBean.validate();
      siteGuiBean.repaint();
      }
      if(name1.equalsIgnoreCase(this.erfGuiBean.ERF_PARAM_NAME)) {

        String plottingOption = null;
        if(plotOptionControl !=null)
          plottingOption=this.plotOptionControl.getSelectedOption();
        controlComboBox.removeAllItems();
        this.initControlList();
        // add the Epistemic control panel option if Epistemic ERF is selected
        if(erfGuiBean.isEpistemicList()) {
          this.controlComboBox.addItem(EPISTEMIC_CONTROL);
          controlComboBox.setSelectedItem(EPISTEMIC_CONTROL);
        }
        else if(plottingOption!= null && plottingOption.equalsIgnoreCase(PlottingOptionControl.ADD_TO_EXISTING)){
          JOptionPane.showMessageDialog(this,"Cannot add to existing without selecting ERF Epistemic list",
                                        "Input Error",JOptionPane.INFORMATION_MESSAGE);
          plotOptionControl.setSelectedOption(PlottingOptionControl.PLOT_ON_TOP);
          setButtonsEnable(true);
        }
      }
  }



  /**
   * Function to make the buttons enable or disable in the application.
   * It is used in application to disable the button in the buttons panel
   * if some computation is already going on.
   * @param b
   */
  protected void setButtonsEnable(boolean b){
    addButton.setEnabled(b);
    clearButton.setEnabled(b);
    peelOffButton.setEnabled(b);
    buttonControlPanel.setEnabled(b);
    progressCheckBox.setEnabled(b);
  }




  /**
   * Gets the probabilities functiion based on selected parameters
   * this function is called when add Graph is clicked
   */
  protected void computeHazardCurve() {

    //starting the calculation
    isHazardCalcDone = false;

    ERF_API forecast = null;
    ProbEqkRupture rupture = null;
    if (!this.isProbCurve)
      rupture = (ProbEqkRupture)this.erfRupSelectorGuiBean.getRupture();

    // get the selected forecast model
    try {
      if (this.isProbCurve) {
        // whether to show progress bar in case of update forecast
        erfGuiBean.showProgressBar(this.progressCheckBox.isSelected());
        //get the selected ERF instance
        forecast = erfGuiBean.getSelectedERF();
      }
    }
    catch (Exception e) {
      e.printStackTrace();
      JOptionPane.showMessageDialog(this, e.getMessage(), "Incorrect Values",
                                    JOptionPane.ERROR_MESSAGE);
      setButtonsEnable(true);
      return;
    }
    if (this.progressCheckBox.isSelected()) {
      progressClass = new CalcProgressBar("Hazard-Curve Calc Status",
                                          "Beginning Calculation ");
      progressClass.displayProgressBar();
      timer.start();
    }

    // get the selected IMR
    AttenuationRelationshipAPI imr = imrGuiBean.getSelectedIMR_Instance();

    // make a site object to pass to IMR
    Site site = siteGuiBean.getSite();

    try {
      // this function will get the selected IMT parameter and set it in IMT
      imtGuiBean.setIMT();
    }
    catch (Exception ex) {
      ExceptionWindow bugWindow = new ExceptionWindow(this, ex,
          getParametersInfoAsString());
      bugWindow.setVisible(true);
      bugWindow.pack();
      if (D) System.out.println(C + ":Param warning caught" + ex);
      ex.printStackTrace();
    }
    // check whether this forecast is a Forecast List
    // if this is forecast list , handle it differently
    boolean isEqkForecastList = false;
    if (forecast instanceof ERF_List && isProbCurve) {
      //if add on top get the name of ERF List forecast
      if (addData)
        prevSelectedERF_List = forecast.getName();

      if (!prevSelectedERF_List.equals(forecast.getName()) && !addData) {
        JOptionPane.showMessageDialog(this,
            "Cannot add to existing without selecting same ERF Epistemic list",
                                      "Input Error",
                                      JOptionPane.INFORMATION_MESSAGE);
        return;
      }
      this.isEqkList = true; // set the flag to indicate thatwe are dealing with Eqk list
      handleForecastList(site, imr, forecast);
      //initializing the counters for ERF List to 0, for other ERF List calculations
      currentERFInEpistemicListForHazardCurve = 0;
      numERFsInEpistemicList = 0;
      isHazardCalcDone = true;
      return;
    }

    //making the previuos selected ERF List to be null
    prevSelectedERF_List = null;

    // this is not a eqk list
    this.isEqkList = false;
    // calculate the hazard curve
    try {
      if (distanceControlPanel != null) calc.setMaxSourceDistance(
          distanceControlPanel.getDistance());
    }
    catch (Exception e) {
      setButtonsEnable(true);
      ExceptionWindow bugWindow = new ExceptionWindow(this, e,
          getParametersInfoAsString());
      bugWindow.setVisible(true);
      bugWindow.pack();
      e.printStackTrace();
    }
    // initialize the values in condProbfunc with log values as passed in hazFunction
    // intialize the hazard function
    ArbitrarilyDiscretizedFunc hazFunction = new ArbitrarilyDiscretizedFunc();
    initX_Values(hazFunction);
    //System.out.println("22222222HazFunction: "+hazFunction.toString());
    try {
      // calculate the hazard curve
      //eqkRupForecast = (EqkRupForecastAPI)FileUtils.loadObject("erf.obj");
      try {
        if (isProbCurve)
          hazFunction = (ArbitrarilyDiscretizedFunc) calc.getHazardCurve(
              hazFunction, site,imr, (EqkRupForecastAPI) forecast);
        else {
          progressCheckBox.setSelected(false);
          progressCheckBox.setEnabled(false);
          hazFunction = (ArbitrarilyDiscretizedFunc) calc.getHazardCurve(
              hazFunction, site, imr, rupture);
          progressCheckBox.setSelected(true);
          progressCheckBox.setEnabled(true);
        }
      }
      catch (Exception e) {
        e.printStackTrace();
        setButtonsEnable(true);
        ExceptionWindow bugWindow = new ExceptionWindow(this, e,
            getParametersInfoAsString());
        bugWindow.setVisible(true);
        bugWindow.pack();

      }
      hazFunction = toggleHazFuncLogValues(hazFunction);
      hazFunction.setInfo(getParametersInfoAsString());
    }
    catch (RuntimeException e) {
      JOptionPane.showMessageDialog(this, e.getMessage(),
                                    "Parameters Invalid",
                                    JOptionPane.INFORMATION_MESSAGE);
      //e.printStackTrace();
      setButtonsEnable(true);
      return;
    }

    // add the function to the function list
    functionList.add(hazFunction);
    // set the X-axis label
    String imt = imtGuiBean.getSelectedIMT();
    xAxisName = imt + " (" + imr.getParameter(imt).getUnits() + ")";
    yAxisName = "Probability of Exceedance";

    isHazardCalcDone = true;
    disaggregationString = null;
    //checking the disAggregation flag and probability curve is being plotted.
    if (disaggregationFlag && isProbCurve) {
      if (this.progressCheckBox.isSelected()) {
        disaggProgressClass = new CalcProgressBar("Disaggregation Calc Status",
                                                  "Beginning Disaggregation ");
        disaggProgressClass.displayProgressBar();
        disaggTimer.start();
      }
      /*try{
        if(distanceControlPanel!=null)  disaggCalc.setMaxSourceDistance(distanceControlPanel.getDistance());
             }catch(Exception e){
        setButtonsEnable(true);
        ExceptionWindow bugWindow = new ExceptionWindow(this,e,getParametersInfoAsString());
        bugWindow.setVisible(true);
        bugWindow.pack();
        e.printStackTrace();
             }*/
      int num = hazFunction.getNum();
      //checks if successfully disaggregated.
      boolean disaggSuccessFlag = false;
      boolean disaggrAtIML = false;
      double disaggregationVal = disaggregationControlPanel.
          getDisaggregationVal();
      String disaggregationParamVal = disaggregationControlPanel.
          getDisaggregationParamValue();
      double minMag = disaggregationControlPanel.getMinMag();
      double deltaMag = disaggregationControlPanel.getdeltaMag();
      int numMag = disaggregationControlPanel.getNumMag();
      double minDist = disaggregationControlPanel.getMinDist();
      double deltaDist = disaggregationControlPanel.getdeltaDist();
      int numDist = disaggregationControlPanel.getNumDist();
      int numSourcesForDisag = disaggregationControlPanel.
          getNumSourcesForDisagg();
      double maxZAxis = disaggregationControlPanel.getZAxisMax();
      double imlVal=0,probVal=0;
      try {
        if (distanceControlPanel != null)
          disaggCalc.setMaxSourceDistance(distanceControlPanel.getDistance());
        if (disaggregationControlPanel.isCustomDistBinning()) {
        	double distBins[] = disaggregationControlPanel.getCustomBinEdges();
        	disaggCalc.setDistanceRange(distBins);
        } else {
        	disaggCalc.setDistanceRange(minDist, numDist, deltaDist);
        }
        disaggCalc.setMagRange(minMag, numMag, deltaMag);
        disaggCalc.setNumSourcestoShow(numSourcesForDisag);
        
      }
      catch (Exception e) {
        setButtonsEnable(true);
        ExceptionWindow bugWindow = new ExceptionWindow(this, e,
            getParametersInfoAsString());
        bugWindow.setVisible(true);
        bugWindow.pack();
        e.printStackTrace();
      }
      try {

        if (disaggregationParamVal.equals(disaggregationControlPanel.
                                          DISAGGREGATE_USING_PROB)) {
          disaggrAtIML = false;
          //if selected Prob is not within the range of the Exceed. prob of Hazard Curve function
          if (disaggregationVal > hazFunction.getY(0) ||
              disaggregationVal < hazFunction.getY(num - 1))
            JOptionPane.showMessageDialog(this,
                                          new String(
                "Chosen Probability is not" +
                " within the range of the min and max prob." +
                " in the Hazard Curve"),
                                          "Disaggregation error message",
                                          JOptionPane.ERROR_MESSAGE);
          else{
            //gets the Disaggregation data
            imlVal = hazFunction.
                getFirstInterpolatedX_inLogXLogYDomain(disaggregationVal);
            probVal = disaggregationVal;
          }
        }
        else if (disaggregationParamVal.equals(disaggregationControlPanel.
                                               DISAGGREGATE_USING_IML)) {
          disaggrAtIML = true;
          //if selected IML is not within the range of the IML values chosen for Hazard Curve function
          if (disaggregationVal < hazFunction.getX(0) ||
              disaggregationVal > hazFunction.getX(num - 1))
            JOptionPane.showMessageDialog(this,
                                          new String("Chosen IML is not" +
                " within the range of the min and max IML values" +
                " in the Hazard Curve"),
                                          "Disaggregation error message",
                                          JOptionPane.ERROR_MESSAGE);
          else{
            imlVal = disaggregationVal;
            probVal = hazFunction.getInterpolatedY_inLogXLogYDomain(disaggregationVal);
          }
        }
      
        disaggSuccessFlag = disaggCalc.disaggregate(Math.log(
            imlVal), site, imr, (EqkRupForecast) forecast);
        disaggCalc.setMaxZAxisForPlot(maxZAxis);
        disaggregationString = disaggCalc.getMeanAndModeInfo();
      }
      catch(WarningException warningException) {
    	  setButtonsEnable(true);
    	  JOptionPane.showMessageDialog(this, warningException.getMessage());
      }
      catch (Exception e) {
        setButtonsEnable(true);
        ExceptionWindow bugWindow = new ExceptionWindow(this, e,
            getParametersInfoAsString());
        bugWindow.setVisible(true);
        bugWindow.pack();
        e.printStackTrace();
      }
      //}
      if (disaggSuccessFlag)
        showDisaggregationResults(numSourcesForDisag,disaggrAtIML,imlVal,probVal);
      else
        JOptionPane.showMessageDialog(this,
                                      "Disaggregation failed because there is " +
                                      "no exceedance above \n " +
                                      "the given IML (or that interpolated from the chosen probability).",
                                      "Disaggregation Message",
                                      JOptionPane.OK_OPTION);
    }
    setButtonsEnable(true);
    //displays the disaggregation string in the pop-up window

    disaggregationString = null;
  }


  /**
   *
   * This function allows showing the disaggregation result in the HMTL to be shown
   * in the dissaggregation plot window.
   * @param numSourceToShow int : Number of sources to show for the disaggregation
   * @param imlBasedDisaggr boolean Disaggregation is done based on chosen IML
   * @param imlVal double iml value for the disaggregation
   * @param probVal double if disaggregation is done based on prob. then its value
   */
  private void showDisaggregationResults(int numSourceToShow,
                                         boolean imlBasedDisaggr,
                                         double imlVal, double probVal) {
    //String sourceDisaggregationListAsHTML = null;
    String sourceDisaggregationList = null;
    if (numSourceToShow > 0) {
      sourceDisaggregationList = getSourceDisaggregationInfo();
      //sourceDisaggregationListAsHTML = sourceDisaggregationList.
        //  replaceAll("\n", "<br>");
      //sourceDisaggregationListAsHTML = sourceDisaggregationListAsHTML.
        //  replaceAll("\t", "&nbsp;&nbsp;&nbsp;");
    }
    String binData = null;
    boolean binDataToShow = disaggregationControlPanel.
        isShowDisaggrBinDataSelected();
    if(binDataToShow){
      try {
        binData = disaggCalc.getBinData();
        //binDataAsHTML = binDataAsHTML.replaceAll("\n", "<br>");
        //binDataAsHTML = binDataAsHTML.replaceAll("\t", "&nbsp;&nbsp;&nbsp;");
      }
      catch (RemoteException ex) {
        setButtonsEnable(true);
        ExceptionWindow bugWindow = new ExceptionWindow(this, ex,
            getParametersInfoAsString());
        bugWindow.setVisible(true);
        bugWindow.pack();
        ex.printStackTrace();
      }
    }
    String modeString = "";
    if (imlBasedDisaggr)
      modeString = "Disaggregation Results for IML = " + imlVal+
          " (for Prob = " + (float)probVal + ")";
    else
      modeString = "Disaggregation Results for Prob = " + probVal +
          " (for IML = " + (float)imlVal + ")";
    modeString += "\n"+disaggregationString;

    String disaggregationPlotWebAddr = null;
    String metadata;
    //String pdfImageLink;
    try {
      disaggregationPlotWebAddr = getDisaggregationPlot();
      /*pdfImageLink = "<br>Click  " + "<a href=\"" +
          disaggregationPlotWebAddr +
          DisaggregationCalculator.DISAGGREGATION_PLOT_PDF_NAME +
          "\">" + "here" + "</a>" +
          " to view a PDF (non-pixelated) version of the image (this will be deleted at midnight).";*/

      metadata = getMapParametersInfoAsHTML();
      metadata += "<br><br>Click  " + "<a href=\"" + disaggregationPlotWebAddr +
          "\">" + "here" + "</a>" +
          " to download files. They will be deleted at midnight";
    }
    catch (RuntimeException e) {
      e.printStackTrace();
      JOptionPane.showMessageDialog(this, e.getMessage(), "Server Problem",
                                    JOptionPane.INFORMATION_MESSAGE);
      return;
    }
    String imgName = disaggregationPlotWebAddr +
        DisaggregationCalculator.DISAGGREGATION_PLOT_IMG_NAME;


    //adding the image to the Panel and returning that to the applet
    //new DisaggregationPlotViewerWindow(imgName,true,modeString,
      //                                 metadata,binData,sourceDisaggregationList);
      new DisaggregationPlotViewerWindow(disaggregationPlotWebAddr +
          DisaggregationCalculator.DISAGGREGATION_PLOT_PDF_NAME,true,modeString,
                                       metadata,binData,sourceDisaggregationList);
  }




  /**
   * Handle the Eqk Forecast List.
   * @param site : Selected site
   * @param imr : selected IMR
   * @param eqkRupForecast : List of Eqk Rup forecasts
   */
  protected void handleForecastList(Site site,
                                  AttenuationRelationshipAPI imr,
                                  ERF_API eqkRupForecast) {

    ERF_List erfList  = (ERF_List)eqkRupForecast;

    numERFsInEpistemicList = erfList.getNumERFs(); // get the num of ERFs in the list


    if(addData) //add new data on top of the existing data
      weightedFuncList = new WeightedFuncListforPlotting();
    //if we are adding to the exsintig data then there is no need to create the new instance
    //weighted functon list.
    else if(!addData && weightedFuncList == null){
      JOptionPane.showMessageDialog(this,"No ERF List Exists","Wrong selection",JOptionPane.OK_OPTION);
      return;
    }

    try{
      // calculate the hazard curve
      if(distanceControlPanel!=null) calc.setMaxSourceDistance(distanceControlPanel.getDistance());
    }catch(Exception e){
      setButtonsEnable(true);
      ExceptionWindow bugWindow = new ExceptionWindow(this,e,getParametersInfoAsString());
      bugWindow.setVisible(true);
      bugWindow.pack();
      e.printStackTrace();
    }

    DiscretizedFuncList hazardFuncList = new DiscretizedFuncList();
    for(int i=0; i<numERFsInEpistemicList; ++i) {
      //current ERF's being used to calculated Hazard Curve
      currentERFInEpistemicListForHazardCurve = i;
      ArbitrarilyDiscretizedFunc hazFunction = new ArbitrarilyDiscretizedFunc();

      // intialize the hazard function
      initX_Values(hazFunction);
      try {
        try{
          // calculate the hazard curve
          hazFunction=(ArbitrarilyDiscretizedFunc)calc.getHazardCurve(hazFunction, site, imr, erfList.getERF(i));
          //System.out.println("Num points:" +hazFunction.toString());
        }catch(Exception e){
          setButtonsEnable(true);
          ExceptionWindow bugWindow = new ExceptionWindow(this,e,getParametersInfoAsString());
          bugWindow.setVisible(true);
          bugWindow.pack();
          e.printStackTrace();
        }
        hazFunction = toggleHazFuncLogValues(hazFunction);
      }catch (RuntimeException e) {
        JOptionPane.showMessageDialog(this, e.getMessage(),
                                      "Parameters Invalid", JOptionPane.INFORMATION_MESSAGE);
        setButtonsEnable(true);
        //e.printStackTrace();
        return;
      }
      hazardFuncList.add(hazFunction);
    }
    weightedFuncList.addList(erfList.getRelativeWeightsList(),hazardFuncList);
    //setting the information inside the weighted function list if adding on top of exisintg data
    if(addData)
      weightedFuncList.setInfo(getParametersInfoAsString());
    else //setting the information inside the weighted function list if adding the data to the existing data
      weightedFuncList.setInfo(getParametersInfoAsString()+"\n"+"Previous List Info:\n"+
                               "--------------------\n"+weightedFuncList.getInfo());




   //individual curves are to be plotted
   if(!isAllCurves)
     weightedFuncList.setIndividualCurvesToPlot(false);
   else
     weightedFuncList.setIndividualCurvesToPlot(true);

   // if custom fractile needed to be plotted
   if(this.fractileOption.equalsIgnoreCase
      (ERF_EpistemicListControlPanel.CUSTOM_FRACTILE)) {
     weightedFuncList.setFractilesToPlot(true);
     weightedFuncList.addFractiles(epistemicControlPanel.getSelectedFractileValues());
   }
   else weightedFuncList.setFractilesToPlot(false);

   // calculate average
   if(this.avgSelected) {
     weightedFuncList.setMeanToPlot(true);
     weightedFuncList.addMean();
   }else weightedFuncList.setMeanToPlot(false);

   //adding the data to the functionlist if adding on top
   if(addData)
     functionList.add(weightedFuncList);
   // set the X, Y axis label
   xAxisName = imtGuiBean.getSelectedIMT();
   yAxisName = "Probability of Exceedance";
  }



  /**
   * This function is to whether to plot ERF_GuiBean or ERF_RupSelectorGuiBean
   * @param e
   */
  protected void probDeterSelection_itemStateChanged(ItemEvent e) {
    String selectedControl = this.probDeterSelection.getSelectedItem().toString();

    if(selectedControl.equalsIgnoreCase(this.PROBABILISTIC)){
     try{
       this.initERF_GuiBean();
       isProbCurve = true;
     }catch(RuntimeException ee){
       ee.printStackTrace();
      JOptionPane.showMessageDialog(this,"Connection to ERF failed","Internet Connection Problem",
                                    JOptionPane.OK_OPTION);
      System.exit(0);
      }
    }
    else if(selectedControl.equalsIgnoreCase(this.DETERMINISTIC)){
     try{
       this.initERFSelector_GuiBean();
       isProbCurve = false;
     }catch(RuntimeException ee){
       ee.printStackTrace();
      JOptionPane.showMessageDialog(this,"Connection to ERF failed","Internet Connection Problem",
                                    JOptionPane.OK_OPTION);
      System.exit(0);
      }
    }

    calc = null;
    createCalcInstance();
  }


  /**
   * Initialize the IMR Gui Bean
   */
  protected void initIMR_GuiBean() {

     imrGuiBean = new IMR_GuiBean(this);
     imrGuiBean.getParameterEditor(imrGuiBean.IMR_PARAM_NAME).getParameter().addParameterChangeListener(this);
     // show this gui bean the JPanel
     imrPanel.add(this.imrGuiBean,new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0,
         GridBagConstraints.CENTER, GridBagConstraints.BOTH, defaultInsets, 0, 0 ));
     imrPanel.updateUI();
  }

  /**
   * Initialize the IMT Gui Bean
   */
  private void initIMT_GuiBean() {

     // get the selected IMR
     AttenuationRelationshipAPI imr = imrGuiBean.getSelectedIMR_Instance();
     // create the IMT Gui Bean object
     imtGuiBean = new IMT_GuiBean(imr,imr.getSupportedIntensityMeasuresIterator());
     imtPanel.setLayout(gridBagLayout8);
     imtPanel.add(imtGuiBean, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0,
                                                     GridBagConstraints.CENTER,
                                                     GridBagConstraints.BOTH,
                                                     defaultInsets, 0, 0));
     imtPanel.updateUI();

  }

  /**
   * Initialize the site gui bean
   */
  protected void initSiteGuiBean() {

     // get the selected IMR
     AttenuationRelationshipAPI imr = imrGuiBean.getSelectedIMR_Instance();
     // create the Site Gui Bean object
     siteGuiBean = new Site_GuiBean();
     siteGuiBean.addSiteParams(imr.getSiteParamsIterator());
     // show the sitebean in JPanel
     sitePanel.add(this.siteGuiBean, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0,
         GridBagConstraints.CENTER, GridBagConstraints.BOTH, defaultInsets, 0, 0));
     sitePanel.updateUI();

  }


  /**
   * Initialize the ERF Gui Bean
   */
  protected void initERF_GuiBean() {

    if (erfGuiBean == null) {
      try {
        // create the ERF Gui Bean object
        ArrayList erf_Classes = new ArrayList();
        //adding the RMI based ERF's to the application
        erf_Classes.add(RMI_FRANKEL_ADJ_FORECAST_CLASS_NAME);
        erf_Classes.add(RMI_WGCEP_UCERF1_ERF_CLASS_NAME);
        //erf_Classes.add(RMI_STEP_FORECAST_CLASS_NAME);
        erf_Classes.add(RMI_STEP_ALASKA_ERF_CLASS_NAME);
        erf_Classes.add(RMI_FLOATING_POISSON_FAULT_ERF_CLASS_NAME);
        erf_Classes.add(RMI_FRANKEL02_ADJ_FORECAST_CLASS_NAME);
        erf_Classes.add(RMI_PEER_AREA_FORECAST_CLASS_NAME);
        erf_Classes.add(RMI_PEER_MULTI_SOURCE_FORECAST_CLASS_NAME);
        erf_Classes.add(RMI_PEER_NON_PLANAR_FAULT_FORECAST_CLASS_NAME);
        erf_Classes.add(RMI_POISSON_FAULT_ERF_CLASS_NAME);
        erf_Classes.add(RMI_POINT2MULT_VSS_FORECAST_CLASS_NAME);
        erf_Classes.add(RMI_WG02_ERF_LIST_CLASS_NAME);
        erf_Classes.add(RMI_PEER_LOGIC_TREE_ERF_LIST_CLASS_NAME);
        erf_Classes.add(RMI_POINT2MULT_VSS_ERF_LIST_CLASS_NAME);

        erfGuiBean = new ERF_GuiBean(erf_Classes);
        erfGuiBean.getParameter(erfGuiBean.ERF_PARAM_NAME).
            addParameterChangeListener(this);
      }
      catch (InvocationTargetException e) {
        ExceptionWindow bugWindow = new ExceptionWindow(this, e,
            "ERF's Initialization problem. Rest all parameters are default");
        bugWindow.setVisible(true);
        bugWindow.pack();
        //e.printStackTrace();
        //throw new RuntimeException("Connection to ERF's failed");
      }
    }
    else {
      boolean isCustomRupture = erfRupSelectorGuiBean.isCustomRuptureSelected();
      if (!isCustomRupture) {
        ERF_API eqkRupForecast = erfRupSelectorGuiBean.
            getSelectedEqkRupForecastModel();
        erfGuiBean.setERF(eqkRupForecast);
      }
    }
    erfPanel.removeAll();
    erfPanel.add(erfGuiBean, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0,
        GridBagConstraints.CENTER, GridBagConstraints.BOTH, defaultInsets, 0, 0));

    erfPanel.updateUI();

  }


  /**
   * Initialize the ERF Rup Selector Gui Bean
   */
  protected void initERFSelector_GuiBean() {

    ERF_API erf = null;
    try {
      erf = erfGuiBean.getSelectedERF();
    }
    catch (InvocationTargetException ex) {
      ex.printStackTrace();
    }
    if(erfRupSelectorGuiBean == null){
      // create the ERF Gui Bean object
      ArrayList erf_Classes = new ArrayList();

      /**
       *  The object class names for all the supported Eqk Rup Forecasts
       */
      erf_Classes.add(RMI_POISSON_FAULT_ERF_CLASS_NAME);
      erf_Classes.add(RMI_FRANKEL_ADJ_FORECAST_CLASS_NAME);
      erf_Classes.add(RMI_WGCEP_UCERF1_ERF_CLASS_NAME);
      erf_Classes.add(RMI_STEP_FORECAST_CLASS_NAME);
      erf_Classes.add(RMI_STEP_ALASKA_ERF_CLASS_NAME);
      erf_Classes.add(RMI_FLOATING_POISSON_FAULT_ERF_CLASS_NAME);
      erf_Classes.add(RMI_FRANKEL02_ADJ_FORECAST_CLASS_NAME);
      erf_Classes.add(RMI_PEER_AREA_FORECAST_CLASS_NAME);
      erf_Classes.add(RMI_PEER_NON_PLANAR_FAULT_FORECAST_CLASS_NAME);
      erf_Classes.add(RMI_PEER_MULTI_SOURCE_FORECAST_CLASS_NAME);
      erf_Classes.add(RMI_WG02_ERF_CLASS_NAME);

      try {
        erfRupSelectorGuiBean = new EqkRupSelectorGuiBean(erf,erf_Classes);
      }
      catch (InvocationTargetException e) {
        throw new RuntimeException("Connection to ERF's failed");
      }
    }
    erfPanel.removeAll();
    //erfGuiBean = null;
    erfPanel.add(erfRupSelectorGuiBean,
                 new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0,
                                        GridBagConstraints.CENTER,
                                        GridBagConstraints.BOTH, defaultInsets, 0,
                                        0));
    erfPanel.updateUI();
  }


  /**
   * Initialize the items to be added to the control list
   */
  protected void initControlList() {
    controlComboBox.addItem(CONTROL_PANELS);
    controlComboBox.addItem(PEER_TEST_CONTROL);
    controlComboBox.addItem(DISAGGREGATION_CONTROL);
    controlComboBox.addItem(DISTANCE_CONTROL);
    controlComboBox.addItem(SITES_OF_INTEREST_CONTROL);
    controlComboBox.addItem(CVM_CONTROL);
    controlComboBox.addItem(X_VALUES_CONTROL);
    controlComboBox.addItem(RUN_ALL_PEER_TESTS);
    controlComboBox.addItem(PLOT_CYBERSHAKE_DATASET_CONTROL);
    controlComboBox.addItem(CYBERSHAKE_SITE_CONTROL);
    //this.controlComboBox.addItem(MAP_CALC_CONTROL);
    controlComboBox.addItem(PLOTTING_OPTION);
    controlComboBox.addItem(XY_Values_Control);
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
    else if(selectedControl.equalsIgnoreCase(this.DISTANCE_CONTROL))
      initDistanceControl();
    else if(selectedControl.equalsIgnoreCase(this.SITES_OF_INTEREST_CONTROL))
      initSitesOfInterestControl();
    else if(selectedControl.equalsIgnoreCase(this.CVM_CONTROL))
      initCVMControl();
    else if(selectedControl.equalsIgnoreCase(this.X_VALUES_CONTROL))
      initX_ValuesControl();
    else if(selectedControl.equalsIgnoreCase(this.RUN_ALL_PEER_TESTS))
      initRunALL_PEER_TestCases();
    else if(selectedControl.equalsIgnoreCase(PLOT_CYBERSHAKE_DATASET_CONTROL))
      initCyberShakeDeterministicControlPanel();
    else if(selectedControl.equalsIgnoreCase(CYBERSHAKE_SITE_CONTROL))
    	initCyberShakeSiteControlPanel();
    else if(selectedControl.equalsIgnoreCase(PLOTTING_OPTION))
      initPlotSelectionControl();
    else if(selectedControl.equalsIgnoreCase(XY_Values_Control))
      this.initXYPlotSelectionControl();

    controlComboBox.setSelectedItem(this.CONTROL_PANELS);
  }


  /**
   * This function allows the user to select new data would be added to the
   * existing plot , if any.
   */
  private void initPlotSelectionControl(){
    if(plotOptionControl ==  null)
      plotOptionControl = new PlottingOptionControl(this);
    plotOptionControl.pack();
    plotOptionControl.setVisible(true);

  }


  /*
   * This function allows user to specify the XY values to be added to the existing
   * plot.
   */
  private void initXYPlotSelectionControl(){
    if(xyPlotControl==null){
      xyPlotControl = new XY_ValuesControlPanel(this,this);
    }
    xyPlotControl.setVisible(true);
  }


  /**
   * This function allows the user to plot the Cybershake Deterministic curve
   * from the Cybershake hazard data set
   */
  private void initCyberShakeDeterministicControlPanel(){
    if(cyberControlPanel == null)
      cyberControlPanel = new CyberShakePlotFromDBControlPanel(this);
    cyberControlPanel.setVisible(true);
  }
  
  /**
   * This function allows the user to plot the Cybershake Deterministic curve
   * from the Cybershake hazard data set
   */
  private void initCyberShakeSiteControlPanel(){
    if(cyberSiteControlPanel == null)
    	cyberSiteControlPanel = new CyberShakeSiteSetterControlPanel(this);
    cyberSiteControlPanel.setVisible(true);
  }


  /**
   * Initialises the Run All PEER Test Control Panel
   * This function is called when user seletes "Run all PEER Tests Cases"
   * from the control pick list
   */
  private void initRunALL_PEER_TestCases(){
    if(distanceControlPanel==null) distanceControlPanel= new SetMinSourceSiteDistanceControlPanel(this);
    if(peerTestsControlPanel==null)
      peerTestsControlPanel=new PEER_TestCaseSelectorControlPanel(this,this,
          imrGuiBean, siteGuiBean, imtGuiBean, erfGuiBean, erfGuiBean.getSelectedERFTimespanGuiBean(),
          this.distanceControlPanel);
    if(runAllPEER_Tests == null)
      runAllPEER_Tests = new RunAll_PEER_TestCasesControlPanel(this);
    runAllPEER_Tests.setVisible(true);
    runAllPEER_Tests.pack();
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
      peerTestsControlPanel=new PEER_TestCaseSelectorControlPanel(this,this,
          imrGuiBean, siteGuiBean, imtGuiBean, erfGuiBean, erfGuiBean.getSelectedERFTimespanGuiBean(),
          this.distanceControlPanel);
    peerTestsControlPanel.setPEER_XValues();
    peerTestsControlPanel.pack();
    peerTestsControlPanel.setVisible(true);
  }


  /**
   * Initialize the Disaggregation control.
   * This function is called when user selects "Disaggregation"
   * from controls pick list
   */
  private void initDisaggregationControl() {
    if(this.disaggregationControlPanel==null)
      disaggregationControlPanel = new DisaggregationControlPanel(this, this);
    disaggregationControlPanel.setVisible(true);
  }

  /**
    * Initialize the Epistemic list control.
    * This function is called when user selects "Epistemic List Control"
    * from controls pick list
    */
   private void initEpistemicControl() {
     if(this.epistemicControlPanel==null)
       epistemicControlPanel = new ERF_EpistemicListControlPanel(this,this);
     epistemicControlPanel.setVisible(true);
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
    distanceControlPanel.setVisible(true);
  }

  /**
   *
   * @returns the selected IMT
   */
  public String getSelectedIMT(){
    return imtGuiBean.getSelectedIMT();
  }


  /**
   * Initialize the Interesting sites control panel
   * It will provide a pick list of interesting sites
   */
  private void initSitesOfInterestControl() {
    if(this.sitesOfInterest==null)
      sitesOfInterest = new SitesOfInterestControlPanel(this, this.siteGuiBean);
    sitesOfInterest.pack();
    sitesOfInterest.setVisible(true);
  }

  /**
   * Initialize the Interesting sites control panel
   * It will provide a pick list of interesting sites
   */
  private void initCVMControl() {
    if(this.cvmControlPanel==null)
      cvmControlPanel = new SiteDataControlPanel(this, this.imrGuiBean, this.siteGuiBean);
    cvmControlPanel.pack();
    cvmControlPanel.setVisible(true);
  }
  
  public SiteDataControlPanel getCVMControl() {
	  if(this.cvmControlPanel==null)
	      cvmControlPanel = new SiteDataControlPanel(this, this.imrGuiBean, this.siteGuiBean);
	  return cvmControlPanel;
  }

  /**
   * initialize the X values for the Hazard Curve control Panel
   * It will enable the user to set the X values
   */
  private void initX_ValuesControl(){
    if(xValuesPanel == null)
      xValuesPanel = new X_ValuesInCurveControlPanel(this,this);
    if(!useCustomX_Values)
      xValuesPanel.useDefaultX_Values();
    else
      xValuesPanel.setX_Values(function);
    xValuesPanel.pack();
    xValuesPanel.setVisible(true);
  }

  /**
   * Initialise the item to be added to the Prob and Deter Selection
   */
  protected void initProbOrDeterList(){
    this.probDeterSelection.addItem(PROBABILISTIC);
    this.probDeterSelection.addItem(DETERMINISTIC);
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
   * This forces use of default X-axis values (according to the selected IMT)
   */
  public void setX_ValuesForHazardCurve(){
    useCustomX_Values = false;
  }

  /**
   * Sets the hazard curve x-axis values (if user wants custom values x-axis values).
   * Note that what's passed in is not cloned (the y-axis values will get modified).
   * @param func
   */
  public void setX_ValuesForHazardCurve(ArbitrarilyDiscretizedFunc func){
    useCustomX_Values = true;
    function =func;
  }

  /**
   * Sets ArbitraryDiscretizedFunc inside list containing all the functions.
   * @param function ArbitrarilyDiscretizedFunc
   */
  public void setArbitraryDiscretizedFuncInList(ArbitrarilyDiscretizedFunc function){
      functionList.add(function);
      ArrayList plotFeaturesList = getPlottingFeatures();
      plotFeaturesList.add(new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.CROSS_SYMBOLS,
          Color.BLACK,4.0,1));
      addGraphPanel();
  }


  /**
   * set x values in log space for Hazard Function to be passed to IMR
   * if the selected IMT are SA , PGA , PGV or FaultDispl
   * It accepts 1 parameters
   *
   * @param originalFunc :  this is the function with X values set
   */
  private void initX_Values(DiscretizedFuncAPI arb){

    // if not using custom values get the function according to IMT.
    if(!useCustomX_Values)
      function = imtInfo.getDefaultHazardCurve(imtGuiBean.getSelectedIMT());

    if (imtInfo.isIMT_LogNormalDist(imtGuiBean.getSelectedIMT())) {
      for(int i=0;i<function.getNum();++i)
        arb.set(Math.log(function.getX(i)),1);

      //System.out.println("11111111111HazFunction: "+arb.toString());
    }
    else
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
    // take log only if it is PGA, PGV ,SA or FaultDispl

    if (imtInfo.isIMT_LogNormalDist(imtGuiBean.getSelectedIMT())) {
      for(int i=0; i<numPoints; ++i)
        hazFunc.set(function.getX(i), tempFunc.getY(i));
      return hazFunc;
    }
    else
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
   * User can choose "No Fractiles", "5th, 50th and 95th Fractile" or
   * "Plot Fractile"
   *
   * @param fractileOption : Option selected by the user. It can be set by
   * various constant String values in ERF_EpistemicListControlPanel
   */
  public void setFractileOption(String fractileOption) {
    this.fractileOption = fractileOption;
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
  
  public ButtonControlPanel getButtonControlPanel() {
	  return buttonControlPanel;
  }

  /**
   * tells the application if the xLog is selected
   * @param xLog : boolean
   */
  public void setX_Log(boolean xLog){
	  if (xLog == this.xLog)
		  return;
	  this.xLog = xLog;
	  if (xLog != this.buttonControlPanel.isXLogSelected()) {
		  this.buttonControlPanel.setXLog(xLog);
	  }
	  drawGraph();
  }

  /**
   * tells the application if the yLog is selected
   * @param yLog : boolean
   */
  public void setY_Log(boolean yLog){
	  if (yLog == this.yLog)
		  return;
	  this.yLog = yLog;
	  if (yLog != this.buttonControlPanel.isYLogSelected()) {
		  this.buttonControlPanel.setYLog(yLog);
	  }
	  drawGraph();
  }


  /**
   *
   * @returns the boolean: Log for X-Axis Selected
   */
  public boolean getXLog(){
    return xLog;
  }

  /**
   *
   * @returns the boolean: Log for Y-Axis Selected
   */
  public boolean getYLog(){
    return yLog;
  }

  /**
   *
   * @returns boolean: Checks if Custom Axis is selected
   */
  public boolean isCustomAxis(){
    return customAxis;
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
   * @returns the X Axis Label
   */
  public String getXAxisLabel(){
    return xAxisName;
  }

  /**
   *
   * @returns Y Axis Label
   */
  public String getYAxisLabel(){
    return yAxisName;
  }

  /**
   *
   * @returns plot Title
   */
  public String getPlotLabel(){
    return TITLE;
  }


  /**
   *
   * sets  X Axis Label
   */
  public void setXAxisLabel(String xAxisLabel){
    xAxisName = xAxisLabel;
  }

  /**
   *
   * sets Y Axis Label
   */
  public void setYAxisLabel(String yAxisLabel){
    yAxisName = yAxisLabel;
  }

  /**
   *
   * sets plot Title
   */
   public void setPlotLabel(String plotTitle){
     TITLE = plotTitle;
   }

  /**
   *
   * @returns the String containing the values selected for different parameters
   */
  public String getParametersInfoAsString(){
    String systemSpecificLineSeparator = SystemPropertiesUtils.getSystemLineSeparator();
    return getMapParametersInfoAsHTML().replaceAll("<br>",systemSpecificLineSeparator);
  }


  /**
   *
   * @returns the String containing the values selected for different parameters
   */
  public String getMapParametersInfoAsHTML(){
    String imrMetadata;
    if(this.isProbCurve) //if Probabilistic calculation then only add the metadata
      //for visible parameters
      imrMetadata = imrGuiBean.getVisibleParametersCloned().getParameterListMetadataString();
    else //if deterministic calculations then add all IMR params metadata.
      imrMetadata = imrGuiBean.getSelectedIMR_Instance().getAllParamMetadata();

    double maxSourceSiteDistance;
    if (distanceControlPanel != null)
      maxSourceSiteDistance = distanceControlPanel.getDistance();
    else
      maxSourceSiteDistance = HazardCurveCalculator.MAX_DISTANCE_DEFAULT;

    return "<br>"+ "IMR Param List:" +"<br>"+
           "---------------"+"<br>"+
         imrMetadata+"<br><br>"+
        "Site Param List: "+"<br>"+
        "----------------"+"<br>"+
        siteGuiBean.getParameterListEditor().getVisibleParametersCloned().getParameterListMetadataString()+"<br><br>"+
        "IMT Param List: "+"<br>"+
        "---------------"+"<br>"+
        imtGuiBean.getVisibleParametersCloned().getParameterListMetadataString()+"<br><br>"+
        "Forecast Param List: "+"<br>"+
        "--------------------"+"<br>"+
        erfGuiBean.getERFParameterList().getParameterListMetadataString()+"<br><br>"+
        "TimeSpan Param List: "+"<br>"+
        "--------------------"+"<br>"+
        erfGuiBean.getSelectedERFTimespanGuiBean().getParameterListMetadataString()+"<br><br>"+
        "Max. Source-Site Distance = "+maxSourceSiteDistance;

  }

 /**
  *
  * @returns the List for all the ArbitrarilyDiscretizedFunctions and Weighted Function list.
  */
  public ArrayList getCurveFunctionList(){
    return functionList;
  }



  /**
   * Actual method implementation of the "Peel-Off"
   * This function peels off the window from the current plot and shows in a new
   * window. The current plot just shows empty window.
   */
  protected void peelOffCurves(){
    graphWindow = new GraphWindow(this);
    clearPlot(true);
    graphWindow.setVisible(true);
  }



  /**
   * Action method to "Peel-Off" the curves graph window in a seperate window.
   * This is called when the user presses the "Peel-Off" window.
   * @param e
   */
  void peelOffButton_actionPerformed(ActionEvent e) {
    peelOffCurves();
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
   * File | Exit action performed.
   *
   * @param actionEvent ActionEvent
   */
  private void fileExitMenu_actionPerformed(ActionEvent actionEvent) {
    close();
  }

  /**
   *
   */
  private void close() {
    int option = JOptionPane.showConfirmDialog(this,
        "Do you really want to exit the application?\n" +
                                               "You will loose all unsaved data.",
                                               "Exit App",
                                               JOptionPane.OK_CANCEL_OPTION);
    if (option == JOptionPane.OK_OPTION)
      System.exit(0);
  }

  /**
   * File | Exit action performed.
   *
   * @param actionEvent ActionEvent
   */
  private void fileSaveMenu_actionPerformed(ActionEvent actionEvent) {
    try {
      save();
    }
    catch (IOException e) {
      JOptionPane.showMessageDialog(this, e.getMessage(), "Save File Error",
                                    JOptionPane.OK_OPTION);
      return;
    }
  }

  /**
   * File | Exit action performed.
   *
   * @param actionEvent ActionEvent
   */
  private void filePrintMenu_actionPerformed(ActionEvent actionEvent) {
    print();
  }

  /**
   * Opens a file chooser and gives the user an opportunity to save the chart
   * in PNG format.
   *
   * @throws IOException if there is an I/O error.
   */
  public void save() throws IOException {
    graphPanel.save();
  }

  /**
   * Creates a print job for the chart.
   */
  public void print() {
    graphPanel.print(this);
  }

  public void closeButton_actionPerformed(ActionEvent actionEvent) {
    close();
  }

  public void printButton_actionPerformed(ActionEvent actionEvent) {
    print();
  }

  public void saveButton_actionPerformed(ActionEvent actionEvent) {
    try {
      save();
    }
    catch (IOException e) {
      JOptionPane.showMessageDialog(this, e.getMessage(), "Save File Error",
                                    JOptionPane.OK_OPTION);
      return;
    }
  }
  
  public GraphPanel getGraphPanel() {
	  return graphPanel;
  }


  /**
   * This function stops the hazard curve calculation if started, so that user does not
   * have to wait for the calculation to finish.
   * Note: This function has one advantage , it starts over the calculation again, but
   * if user has not changed any other parameter for the forecast, that won't
   * be updated, so saves time and memory for not updating the forecast everytime,
   * cancel is pressed.
   * @param e
   */
  void cancelCalcButton_actionPerformed(ActionEvent e) {
    //stopping the Hazard Curve calculation thread
    calcThread.stop();
    calcThread = null;
    //close the progress bar for the ERF GuiBean that displays "Updating Forecast".
    erfGuiBean.closeProgressBar();
    //stoping the timer thread that updates the progress bar
    if(timer !=null && progressClass !=null){
      timer.stop();
      timer = null;
      progressClass.dispose();
    }
    //stopping the Hazard Curve calculations on server
    if(calc !=null){
      try{
        calc.stopCalc();
        calc = null;
      }catch(RemoteException ee){
        ExceptionWindow bugWindow = new ExceptionWindow(this,ee,getParametersInfoAsString());
        bugWindow.setVisible(true);
        bugWindow.pack();
      }
    }
    this.isHazardCalcDone = false;
    //making the buttons to be visible
    setButtonsEnable(true);
    cancelCalcButton.setVisible(false);
  }


  /**
   * Returns the Disaggregation plot image webaddr to be shown in the plot window.
   * @return String
   */
  public String getDisaggregationPlot(){
    try{
      return disaggCalc.getDisaggregationPlotUsingServlet(this.
          getParametersInfoAsString());
    }
    catch (Exception ex) {
      ex.printStackTrace();
      setButtonsEnable(true);
      ExceptionWindow bugWindow = new ExceptionWindow(this, ex,
          getParametersInfoAsString());
      bugWindow.setVisible(true);
      bugWindow.pack();
    }
    return null;
  }

  /**
   * Returns the Source Disaggregated List
   * @return String
   */
  public String getSourceDisaggregationInfo() {
    try {
      return disaggCalc.getDisaggregationSourceInfo();
    }
    catch (Exception ex) {
      ex.printStackTrace();
      setButtonsEnable(true);
      ExceptionWindow bugWindow = new ExceptionWindow(this, ex,
          getParametersInfoAsString());
      bugWindow.setVisible(true);
      bugWindow.pack();
    }
    return null;
  }

  /**
   * Adding the Cybershake curve to the list of plots
   * @param function DiscretizedFuncAPI
   */
  public void addCybershakeCurveData(DiscretizedFuncAPI function) {
    functionList.add(function);
    ArrayList plotFeaturesList = getPlottingFeatures();
    plotFeaturesList.add(new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.LINE_AND_CIRCLES,
        Color.BLACK,1.0,1));
    addGraphPanel();
  }


  /**
   * Sets the application with the curve type chosen by the Cybershake application
   * @param isDeterministic boolean :If deterministic calculation then make the
   * applicaton to plot deterministic curves.
   */
    public void setCurveType(boolean isDeterministic){
       if(!isDeterministic)
        probDeterSelection.setSelectedItem(PROBABILISTIC);
      else
        probDeterSelection.setSelectedItem(DETERMINISTIC);
    }



  /**
   * Returns the IML values being used by the application
   * @return ArrayList
   */
  public ArrayList getIML_Values() {

    ArrayList imlList = new ArrayList();
    ArbitrarilyDiscretizedFunc func = null;
    if(function != null)
      func = function;
    else
      func = imtInfo.getDefaultHazardCurve(imtGuiBean.getSelectedIMT());

    int size = func.getNum();
    for (int i = 0; i < size; ++i)
      imlList.add(new Double(func.getX(i)));

    return imlList;
  }

  /**
   * This returns the Earthquake Forecast GuiBean which allows the the cybershake
   * control panel to set the forecast parameters from cybershake control panel,
   * similar to what they are set when calculating cybershaks curves.
   */
  public ERF_GuiBean getEqkRupForecastGuiBeanInstance(){
    return erfGuiBean;

  }

  /**
   * This returns instance to the EqkRupSelectorGuiBean, this allows the cybershake
   * control panel to set the forecast parameters and select the same source
   * and rupture as in the cybershake control panel.
   */
  public EqkRupSelectorGuiBean getEqkSrcRupSelectorGuiBeanInstance() {
    return erfRupSelectorGuiBean;
  }

  /**
   * This returns the Site Guibean using which allows to set the site locations
   * in the OpenSHA application from cybershake control panel.
   */
  public Site_GuiBean getSiteGuiBeanInstance() {
    return siteGuiBean;
  }


  /**
   * It returns the IMT Gui bean, which allows the Cybershake control panel
   * to set the same SA period value in the main application
   * similar to selected for Cybershake.
   */
  public IMT_GuiBean getIMTGuiBeanInstance() {
    return imtGuiBean;
  }
  
  /**
   * It returns the IMR Gui bean, which allows the Cybershake control panel
   * to set the gaussian truncation value in the main application
   * similar to selected for Cybershake.
   */
  public IMR_GuiBean getIMRGuiBeanInstance() {
    return imrGuiBean;
  }
  
 
  /**
   * Updates the IMT_GuiBean to reflect the chnaged IM for the selected AttenuationRelationship.
   * This method is called from the IMR_GuiBean to update the application with the Attenuation's
   * supported IMs.
   *
   */
  public void updateIM() {
    //get the selected IMR
	AttenuationRelationshipAPI imr = imrGuiBean.getSelectedIMR_Instance();
	imtGuiBean.setIM(imr,imr.getSupportedIntensityMeasuresIterator()) ;
  }
  
  /**
   * Updates the Site_GuiBean to reflect the chnaged SiteParams for the selected AttenuationRelationship.
   * This method is called from the IMR_GuiBean to update the application with the Attenuation's
   * Site Params.
   *
   */
  public void updateSiteParams() {
    //get the selected IMR
	AttenuationRelationshipAPI imr = imrGuiBean.getSelectedIMR_Instance();
	siteGuiBean.replaceSiteParams(imr.getSiteParamsIterator());
	siteGuiBean.validate();
    siteGuiBean.repaint();
  }
}
