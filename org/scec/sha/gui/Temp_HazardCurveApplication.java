package org.scec.sha.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.SystemColor;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.rmi.RemoteException;


import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import java.io.*;
import java.net.*;

import org.jfree.data.Range;
import org.scec.data.Site;
import org.scec.data.function.ArbitrarilyDiscretizedFunc;
import org.scec.data.function.DiscretizedFuncAPI;
import org.scec.data.function.DiscretizedFuncList;
import org.scec.gui.plot.jfreechart.DiscretizedFunctionXYDataSet;
import org.scec.param.event.ParameterChangeEvent;
import org.scec.param.event.ParameterChangeListener;
import org.scec.sha.calc.DisaggregationCalculator;
import org.scec.calc.FractileCurveCalculator;
import org.scec.sha.calc.HazardCurveCalculator;
import org.scec.sha.earthquake.ERF_List;
import org.scec.sha.earthquake.EqkRupForecast;
import org.scec.sha.earthquake.EqkRupForecastAPI;
import org.scec.sha.gui.beans.ERF_GuiBean;
import org.scec.sha.gui.beans.IMR_GuiBean;
import org.scec.sha.gui.beans.IMT_GuiBean;
import org.scec.sha.gui.beans.Site_GuiBean;
import org.scec.sha.gui.beans.TimeSpanGuiBean;
import org.scec.sha.gui.controls.DisaggregationControlPanel;
import org.scec.sha.gui.controls.DisaggregationControlPanelAPI;
import org.scec.sha.gui.controls.ERF_EpistemicListControlPanel;
import org.scec.sha.gui.controls.ERF_EpistemicListControlPanelAPI;
import org.scec.sha.gui.controls.PEER_TestCaseSelectorControlPanel;
import org.scec.sha.gui.controls.PEER_TestCaseSelectorControlPanelAPI;
import org.scec.sha.gui.controls.RunAll_PEER_TestCasesControlPanel;
import org.scec.sha.gui.controls.SetMinSourceSiteDistanceControlPanel;
import org.scec.sha.gui.controls.SetSiteParamsFromWebServicesControlPanel;
import org.scec.sha.gui.controls.SitesOfInterestControlPanel;
import org.scec.sha.gui.controls.X_ValuesInCurveControlPanel;
import org.scec.sha.gui.controls.X_ValuesInCurveControlPanelAPI;
import org.scec.sha.gui.controls.PlottingOptionControl;
import org.scec.sha.gui.infoTools.ButtonControlPanel;
import org.scec.sha.gui.infoTools.ButtonControlPanelAPI;
import org.scec.sha.gui.infoTools.CalcProgressBar;
import org.scec.sha.gui.infoTools.GraphPanel;
import org.scec.sha.gui.infoTools.GraphPanelAPI;
import org.scec.sha.gui.infoTools.GraphWindow;
import org.scec.sha.gui.infoTools.GraphWindowAPI;
import org.scec.sha.gui.infoTools.HazardCurveDisaggregationWindow;
import org.scec.sha.gui.infoTools.IMT_Info;
import org.scec.sha.imr.AttenuationRelationshipAPI;
import org.scec.util.ImageUtils;
import org.scec.util.SystemPropertiesUtils;
import org.scec.util.FileUtils;
import org.scec.sha.gui.controls.CalcOptionControl;
import org.scec.sha.calc.remoteHazardCalc.RemoteHazardCurveClient;
import org.scec.sha.calc.HazardCurveCalculatorAPI;
import org.scec.sha.gui.infoTools.WeightedFuncListforPlotting;

import ch.randelshofer.quaqua.QuaquaManager;

/**
 * <p>Title: Temp_HazardCurveApplication</p>
 * <p>Description: </p>
 * @author Nitin Gupta and Vipin Gupta
 * Date : Sept 23 , 2002
 * @version 1.0
 */

public class Temp_HazardCurveApplication extends JApplet
    implements Runnable,  ParameterChangeListener,
    DisaggregationControlPanelAPI, ERF_EpistemicListControlPanelAPI ,
    X_ValuesInCurveControlPanelAPI, PEER_TestCaseSelectorControlPanelAPI,
    ButtonControlPanelAPI,GraphPanelAPI,GraphWindowAPI{

  /**
   * Name of the class
   */
  private final static String C = "Temp_HazardCurveApplication";
  // for debug purpose
  private final static boolean D = false;

  public static String SERVLET_URL  = "http://gravity.usc.edu/OpenSHA/servlet/HazardCurveCalcServlet";


  /**
   *  The object class names for all the supported Eqk Rup Forecasts
   */
  public final static String RMI_FRANKEL_ADJ_FORECAST_CLASS_NAME = "org.scec.sha.earthquake.rupForecastImpl.remoteERF_Clients.Frankel96_AdjustableEqkRupForecastClient";
  public final static String RMI_STEP_FORECAST_CLASS_NAME = "org.scec.sha.earthquake.rupForecastImpl.remoteERF_Clients.STEP_EqkRupForecastClient";
  public final static String RMI_STEP_ALASKA_ERF_CLASS_NAME = "org.scec.sha.earthquake.rupForecastImpl.remoteERF_Clients.STEP_AlaskanPipeForecastClient";
  public final static String RMI_FLOATING_POISSON_FAULT_ERF_CLASS_NAME = "org.scec.sha.earthquake.rupForecastImpl.remoteERF_Clients.FloatingPoissonFaultERF_Client";
  public final static String RMI_FRANKEL02_ADJ_FORECAST_CLASS_NAME="org.scec.sha.earthquake.rupForecastImpl.remoteERF_Clients.Frankel02_AdjustableEqkRupForecastClient";
  public final static String RMI_PEER_AREA_FORECAST_CLASS_NAME = "org.scec.sha.earthquake.rupForecastImpl.remoteERF_Clients.PEER_AreaForecastClient";
  public final static String RMI_PEER_NON_PLANAR_FAULT_FORECAST_CLASS_NAME = "org.scec.sha.earthquake.rupForecastImpl.remoteERF_Clients.PEER_NonPlanarFaultForecastClient";
  public final static String RMI_PEER_MULTI_SOURCE_FORECAST_CLASS_NAME = "org.scec.sha.earthquake.rupForecastImpl.remoteERF_Clients.PEER_MultiSourceForecastClient";
  public final static String RMI_WG02_ERF_LIST_CLASS_NAME = "org.scec.sha.earthquake.rupForecastImpl.remoteERF_Clients.WG02_FortranWrappedERF_EpistemicListClient";
  public final static String RMI_POISSON_FAULT_ERF_CLASS_NAME = "org.scec.sha.earthquake.rupForecastImpl.remoteERF_Clients.PoissonFaultERF_Client";



  // instances of the GUI Beans which will be shown in this applet
  protected ERF_GuiBean erfGuiBean;
  private IMR_GuiBean imrGuiBean;
  private IMT_GuiBean imtGuiBean;
  private Site_GuiBean siteGuiBean;
  private TimeSpanGuiBean timeSpanGuiBean;

  //instance for the ButtonControlPanel
  ButtonControlPanel buttonControlPanel;

  //instance of the GraphPanel (window that shows all the plots)
  GraphPanel graphPanel;

  //instance of the GraphWindow to pop up when the user wants to "Peel-Off" curves;
  GraphWindow graphWindow;

  // Strings for control pick list
  private final static String CONTROL_PANELS = "Control Panels";
  private final static String PEER_TEST_CONTROL = "PEER Test Case Selector";
  private final static String DISAGGREGATION_CONTROL = "Disaggregation";
  private final static String EPISTEMIC_CONTROL = "ERF Epistemic Control";
  private final static String DISTANCE_CONTROL = "Max Source-Site Distance";
  private final static String SITES_OF_INTEREST_CONTROL = "Sites of Interest";
  private final static String CVM_CONTROL = "Set Site Params from Web Services";
  private final static String X_VALUES_CONTROL = "Set X values for Hazard Curve Calc.";
  private final static String RUN_ALL_PEER_TESTS = "Run all PEER Test Cases";
  private final static String MAP_CALC_CONTROL = "Select Map Calcution Method";
  private final static String PLOTTING_OPTION = "Set new dataset plotting option";


  // objects for control panels
  private PEER_TestCaseSelectorControlPanel peerTestsControlPanel;
  private DisaggregationControlPanel disaggregationControlPanel;
  private ERF_EpistemicListControlPanel epistemicControlPanel;
  private SetMinSourceSiteDistanceControlPanel distanceControlPanel;
  private SitesOfInterestControlPanel sitesOfInterest;
  private SetSiteParamsFromWebServicesControlPanel cvmControlPanel;
  private X_ValuesInCurveControlPanel xValuesPanel;
  private RunAll_PEER_TestCasesControlPanel runAllPEER_Tests;
  private PlottingOptionControl plotOptionControl;



  /*setting the colors for the different plots so that legends
  *can be shown with the same color
  */
  Color [] defaultColor = {Color.red,Color.blue,Color.green,Color.orange,Color.magenta,
    Color.cyan,Color.pink,Color.yellow,Color.lightGray,Color.gray,Color.darkGray};


  private Insets plotInsets = new Insets( 4, 10, 4, 4 );

  protected boolean isStandalone = false;
  private Border border1;


  //log flags declaration
  private boolean xLog =false;
  private boolean yLog =false;

  // default insets
  protected Insets defaultInsets = new Insets( 4, 4, 4, 4 );

  // height and width of the applet
  protected final static int W = 1100;
  protected final static int H = 750;

  /**
   * FunctionList declared
   */
  private DiscretizedFuncList totalProbFuncs = new DiscretizedFuncList();
  private DiscretizedFunctionXYDataSet data = new DiscretizedFunctionXYDataSet();
  private ArrayList functionList = new ArrayList();

  //holds the ArbitrarilyDiscretizedFunc
  private ArbitrarilyDiscretizedFunc function;

  //instance to get the default IMT X values for the hazard Curve
  private IMT_Info imtInfo = new IMT_Info();




  // variable needed for plotting Epistemic list
  private boolean isEqkList = false; // whther we are plottin the Eqk List
  //private boolean isIndividualCurves = false; //to keep account that we are first drawing the individual curve for erf in the list
  private boolean isAllCurves = true; // whether to plot all curves
  // whether user wants to plot No percentile, or 5, 50 and 95 percentile or custom percentile
  private String percentileOption ;
  // whether avg is selected by the user
  private boolean avgSelected = false;

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



  //flags to check which X Values the user wants to work with: default or custom
  boolean useCustomX_Values = false;


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

  JSplitPane topSplitPane = new JSplitPane();
  JButton clearButton = new JButton();
  JPanel buttonPanel = new JPanel();
  JCheckBox progressCheckBox = new JCheckBox();
  JButton addButton = new JButton();
  JComboBox controlComboBox = new JComboBox();
  JSplitPane chartSplit = new JSplitPane();
  JPanel panel = new JPanel();
  GridBagLayout gridBagLayout9 = new GridBagLayout();
  JPanel timeSpanPanel = new JPanel();
  GridBagLayout gridBagLayout8 = new GridBagLayout();
  JSplitPane imrSplitPane = new JSplitPane();
  GridBagLayout gridBagLayout5 = new GridBagLayout();
  JSplitPane erfSplitPane = new JSplitPane();
  JPanel sitePanel = new JPanel();
  JPanel imtPanel = new JPanel();
  JSplitPane controlsSplit = new JSplitPane();
  JTabbedPane paramsTabbedPane = new JTabbedPane();
  JPanel erfPanel = new JPanel();
  GridBagLayout gridBagLayout15 = new GridBagLayout();
  GridBagLayout gridBagLayout13 = new GridBagLayout();
  GridBagLayout gridBagLayout12 = new GridBagLayout();
  JPanel imrPanel = new JPanel();
  GridBagLayout gridBagLayout10 = new GridBagLayout();
  BorderLayout borderLayout1 = new BorderLayout();

  //instances of various calculators
  HazardCurveCalculatorAPI calc;
  DisaggregationCalculator disaggCalc;
  CalcProgressBar progressClass;
  CalcProgressBar disaggProgressClass;
  Timer timer;
  Timer disaggTimer;
  //checks to see if HazardCurveCalculations are done
  boolean isHazardCalcDone= false;
  private JButton peelOffButton = new JButton();
  private JLabel imgLabel = new JLabel(new ImageIcon(ImageUtils.loadImage(this.POWERED_BY_IMAGE)));
  private FlowLayout flowLayout1 = new FlowLayout();



  //maintains which ERFList was previously selected
  private String prevSelectedERF_List = null;

  //keeps track which was the last selected Weighted function list.
  //It only initialises this weighted function list if user wants to add data to the existing ERF_List
  private WeightedFuncListforPlotting weightedFuncList;

  /**this boolean keeps track when to plot the new data on top of other and when to
  *add to the existing data.
  * If it is true then add new data on top of existing data, but if it is false
  * then add new data to the existing data(this option only works if it is ERF_List).
  * */
  boolean addData= true;

  //Get command-line parameter value
  public String getParameter(String key, String def) {
    return isStandalone ? System.getProperty(key, def) :
        (getParameter(key) != null ? getParameter(key) : def);
  }

  //Construct the applet
  public Temp_HazardCurveApplication() {
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
      try{
        initERF_GuiBean();
        initTimeSpanGuiBean();
      }catch(RuntimeException e){
        JOptionPane.showMessageDialog(this,"Connection to ERF's failed","Internet Connection Problem",
                                      JOptionPane.OK_OPTION);
        e.printStackTrace();
        System.exit(0);
      }
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

    this.setSize(new Dimension(1060, 670));
    this.getContentPane().setLayout(borderLayout1);


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

    buttonPanel.setAlignmentX((float) 0.0);
    buttonPanel.setAlignmentY((float) 0.0);
    buttonPanel.setMinimumSize(new Dimension(568, 20));
    buttonPanel.setLayout(flowLayout1);

    progressCheckBox.setFont(new java.awt.Font("Dialog", 1, 12));

    progressCheckBox.setSelected(true);
    progressCheckBox.setText("Show Progress Bar");

    addButton.setText("Add Graph");
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
    timeSpanPanel.setLayout(gridBagLayout12);
    imrSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
    imrSplitPane.setBottomComponent(imtPanel);
    imrSplitPane.setTopComponent(imrPanel);
    erfSplitPane.setBottomComponent(timeSpanPanel);
    erfSplitPane.setLastDividerLocation(210);
    erfSplitPane.setTopComponent(erfPanel);
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



    peelOffButton.setText("Peel Off");
    peelOffButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        peelOffButton_actionPerformed(e);
      }
    });


    imgLabel.addMouseListener(new java.awt.event.MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        imgLabel_mouseClicked(e);
      }
    });
    this.getContentPane().add(jPanel1, BorderLayout.CENTER);
    jPanel1.add(topSplitPane,  new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(11, 4, 5, 6), 243, 231));

    //object for the ButtonControl Panel
    buttonControlPanel = new ButtonControlPanel(this);

    buttonPanel.add(controlComboBox, 0);
    buttonPanel.add(addButton, 1);
    buttonPanel.add(clearButton, 2);
    buttonPanel.add(peelOffButton, 3);
    buttonPanel.add(progressCheckBox, 4);
    buttonPanel.add(buttonControlPanel,5);
    buttonPanel.add(imgLabel, 6);



    topSplitPane.add(chartSplit, JSplitPane.TOP);
    chartSplit.add(panel, JSplitPane.LEFT);
    chartSplit.add(paramsTabbedPane, JSplitPane.RIGHT);
    imrSplitPane.add(imrPanel, JSplitPane.TOP);
    imrSplitPane.add(imtPanel, JSplitPane.BOTTOM);
    controlsSplit.add(imrSplitPane, JSplitPane.LEFT);
    paramsTabbedPane.add(controlsSplit, "IMR, IMT & Site");
    controlsSplit.add(sitePanel, JSplitPane.RIGHT);
    paramsTabbedPane.add(erfSplitPane, "ERF & Time Span");
    erfSplitPane.add(erfPanel, JSplitPane.LEFT);
    erfSplitPane.add(timeSpanPanel, JSplitPane.RIGHT);
    topSplitPane.add(buttonPanel, JSplitPane.BOTTOM);
    topSplitPane.setDividerLocation(600);
    imrSplitPane.setDividerLocation(300);
    erfSplitPane.setDividerLocation(230);
    controlsSplit.setDividerLocation(230);
    erfPanel.validate();
    erfPanel.repaint();
    chartSplit.setDividerLocation(600);

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
    return "Hazard Curves Applet";
  }


  //Main method
  public static void main(String[] args) {
    Temp_HazardCurveApplication applet = new Temp_HazardCurveApplication();
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
    String osName = System.getProperty("os.name");
    try {
      if(osName.startsWith("Mac OS"))
        UIManager.setLookAndFeel(QuaquaManager.getLookAndFeelClassName());
      else
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
      graphPanel.drawGraphPanel(functionList,data,xLog,yLog,customAxis,TITLE,buttonControlPanel);
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
            String peerDirName = "peer/";
            //creating the peer directory in which we put all the peer related files
            File peerDir = new File(peerDirName);
            if(!peerDir.isDirectory()) { // if main directory does not exist
              boolean success = (new File(peerDirName)).mkdir();
            }

            ArrayList testCasesTwo = this.peerTestsControlPanel.getPEER_SetTwoTestCasesNames();

            int size = testCasesTwo.size();
            /*if(epistemicControlPanel == null)
              epistemicControlPanel = new ERF_EpistemicListControlPanel(this,this);
            epistemicControlPanel.setCustomPercentileValue(05);
            epistemicControlPanel.setVisible(false); */
            setAverageSelected(true);
            for(int i=18 ;i < size; ++i){
              System.out.println("Working on # "+(i+1)+" of "+size);

              // first do PGA
              peerTestsControlPanel.setTestCaseAndSite((String)testCasesTwo.get(i));
              addButton();

              FileWriter peerFile=new FileWriter(peerDirName+(String)testCasesTwo.get(i)+"-PGA_OpenSHA.dat");
              DiscretizedFuncAPI func = (DiscretizedFuncAPI)functionList.get(0);
              for(int j=0; j<func.getNum();++j)
                peerFile.write(func.get(j).getX()+" "+func.get(j).getY()+"\n");
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
            ee.printStackTrace();
          }
        }
      }
      else
        addButton();
    }


    public void run() {
      computeHazardCurve();

    }

    /**
     * This method creates the HazardCurveCalc instance. If the internet connection
     * is available then it creates a remote instance of the calculator on the server
     * where the calculations take place, else calculation are performed on the user's
     * own machine.
     */
    protected void createCalcInstance(){
      calc = (new RemoteHazardCurveClient()).getRemoteHazardCurveCalc();
    }

    /**
     * this function is called to draw the graph
     */
    private void addButton() {
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
        e.printStackTrace();
      }

      // check if progress bar is desired and set it up if so
      if(this.progressCheckBox.isSelected())  {
        //progressClass = new CalcProgressBar("Hazard-Curve Calc Status", "Beginning Calculation ");
        //progressClass.displayProgressBar();

        timer = new Timer(500, new ActionListener() {
          public void actionPerformed(ActionEvent evt) {
            try{
              if(calc.getCurrRuptures()!=-1)
                progressClass.updateProgress(calc.getCurrRuptures(), calc.getTotRuptures());
              if (isHazardCalcDone) {
                // Toolkit.getDefaultToolkit().beep();
                timer.stop();
                progressClass.dispose();
                drawGraph();
              }
            }catch(RemoteException e){
              e.printStackTrace();
            }
          }
        });

        // timer for disaggregation progress bar
        disaggTimer = new Timer(500, new ActionListener() {
          public void actionPerformed(ActionEvent evt) {
            if(disaggCalc.getCurrRuptures()!=-1)
              disaggProgressClass.updateProgress(disaggCalc.getCurrRuptures(), disaggCalc.getTotRuptures());
            if (disaggCalc.done()) {
              // Toolkit.getDefaultToolkit().beep();
              disaggTimer.stop();
              disaggProgressClass.dispose();
            }
          }
        });

        Thread t = new Thread(this);
        t.start();
      }
      //if it is ERF List but no progress bar is selected,
      //so we want to show curve as they are being drawn on the chart.
      else if(isEqkList && !progressCheckBox.isSelected()){
        Thread t = new Thread(this);
        t.start();
        drawGraph();
      }
      else {
        this.computeHazardCurve();
        this.drawGraph();
      }

    }

    /**
     * to draw the graph
     */
    private void drawGraph() {
      // you can show warning messages now
     imrGuiBean.showWarningMessages(true);
     // set the log values
     data.setXLog(xLog);
     data.setYLog(yLog);

     addGraphPanel();
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
    graphPanel.removeChartAndMetadata();
    panel.removeAll();
    if( clearFunctions) {
      totalProbFuncs.clear();
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

        String plottingOption = null;
        if(plotOptionControl !=null)
          plottingOption=this.plotOptionControl.getSelectedOption();
        /* get the selected ERF
        NOTE : We have used erfGuiBean.getSelectedERF_Instance()INSTEAD OF
        erfGuiBean.getSelectedERF.
        Difference is that erfGuiBean.getSelectedERF_Instance() does not update
        the forecast while erfGuiBean.getSelectedERF updates the
        */
        try{
          EqkRupForecastAPI erfAPI = erfGuiBean.getSelectedERF_Instance();
          this.timeSpanGuiBean.setTimeSpan(erfAPI.getTimeSpan());
        }catch(Exception ee){
          ee.printStackTrace();
        }

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
        }
        this.timeSpanGuiBean.validate();
        this.timeSpanGuiBean.repaint();
      }
  }


  /**
   * Gets the probabilities functiion based on selected parameters
   * this function is called when add Graph is clicked
   */
  private void computeHazardCurve() {
    //starting the calculation
    isHazardCalcDone= false;

    EqkRupForecastAPI forecast = null;
    // whwther to show progress bar in case of update forecast
    erfGuiBean.showProgressBar(this.progressCheckBox.isSelected());
    // get the selected forecast model
    try{
      //get the selected ERF instance
      forecast = erfGuiBean.getSelectedERF();
    }catch(Exception e){
      e.printStackTrace();
      JOptionPane.showMessageDialog(this,e.getMessage(),"Incorrect Values",JOptionPane.ERROR_MESSAGE);
      return;
    }
    if(this.progressCheckBox.isSelected())  {
      progressClass = new CalcProgressBar("Hazard-Curve Calc Status", "Beginning Calculation ");
      progressClass.displayProgressBar();
      timer.start();
    }

    // get the selected IMR
    AttenuationRelationshipAPI imr = imrGuiBean.getSelectedIMR_Instance();

    // make a site object to pass to IMR
    Site site =  siteGuiBean.getSite();

    try {
      // this function will get the selected IMT parameter and set it in IMT
      imtGuiBean.setIMT();
    } catch (Exception ex) {
      if(D) System.out.println(C + ":Param warning caught"+ex);
      ex.printStackTrace();
    }


    // check whether this forecast is a Forecast List
    // if this is forecast list , handle it differently
    boolean isEqkForecastList = false;
    if(forecast instanceof ERF_List)  {
      if(prevSelectedERF_List == null)
        prevSelectedERF_List = forecast.getName();
      else{
        if(!prevSelectedERF_List.equals(forecast.getName()) && !addData){
          JOptionPane.showMessageDialog(this,"Cannot add to existing without selecting ERF Epistemic list",
                                        "Input Error",JOptionPane.INFORMATION_MESSAGE);
          return;
        }
      }
      this.isEqkList = true; // set the flag to indicate thatwe are dealing with Eqk list
      handleForecastList(site, imr, forecast);
      isHazardCalcDone = true;
      return;
    }

    //making the previuos selected ERF List to be null
    prevSelectedERF_List = null;



      // this is not a eqk list
      this.isEqkList = false;
      // calculate the hazard curve
      try{
        if(distanceControlPanel!=null)  calc.setMaxSourceDistance(distanceControlPanel.getDistance());
      }catch(RemoteException e){
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
        try{
          hazFunction = (ArbitrarilyDiscretizedFunc)calc.getHazardCurve(hazFunction, site, imr, (EqkRupForecast)forecast);
        }catch(RemoteException e){
          e.printStackTrace();
        }
        //hazFunction = sendParametersToServlet(site,imr,eqkRupForecastLocation,hazFunction);
        hazFunction = toggleHazFuncLogValues(hazFunction);
        hazFunction.setInfo(getParametersInfo());
      }catch (RuntimeException e) {
        JOptionPane.showMessageDialog(this, e.getMessage(),
                                      "Parameters Invalid", JOptionPane.INFORMATION_MESSAGE);
        e.printStackTrace();
        return;
      }


   // add the function to the function list
      functionList.add(hazFunction);
   // set the X-axis label
   String imt = imtGuiBean.getSelectedIMT();
   totalProbFuncs.setXAxisName(imt + " ("+imr.getParameter(imt).getUnits()+")");
   totalProbFuncs.setYAxisName("Probability of Exceedance");

   isHazardCalcDone = true;
  // disaggregationString=null;
   //checking the disAggregation flag
   //if(this.disaggregationFlag) {
    // disaggCalc = new DisaggregationCalculator();
    // if(this.progressCheckBox.isSelected())  {
     //  disaggProgressClass = new CalcProgressBar("Disaggregation Calc Status", "Beginning Disaggregation ");
     //  disaggProgressClass.displayProgressBar();
     //  disaggTimer.start();
   // }

     //if(distanceControlPanel!=null)  disaggCalc.setMaxSourceDistance(distanceControlPanel.getDistance());
    // int num = hazFunction.getNum();
     //double disaggregationProb = this.disaggregationControlPanel.getDisaggregationProb();
     //if selected Prob is not within the range of the Exceed. prob of Hazard Curve function
   //  if(disaggregationProb > hazFunction.getY(0) || disaggregationProb < hazFunction.getY(num-1))
    //   JOptionPane.showMessageDialog(this,
     //                                new String("Chosen Probability is not"+
      //                               " within the range of the min and max prob."+
       //                              " in the Hazard Curve"),
         //                            "Disaggregation Prob. selection error message",
          //                           JOptionPane.OK_OPTION);
     //else{
       //gets the Disaggregation data
      // double iml= hazFunction.getFirstInterpolatedX_inLogXLogYDomain(disaggregationProb);
       //disaggCalc.disaggregate(Math.log(iml),site,imr,(EqkRupForecast)eqkRupForecast);
       //disaggregationString=disaggCalc.getResultsString();
    // }
  // }
   //displays the disaggregation string in the pop-up window
   //if(disaggregationString !=null) {
    // HazardCurveDisaggregationWindow disaggregation=new HazardCurveDisaggregationWindow(this, disaggregationString);
     //disaggregation.pack();
     //disaggregation.show();

   //}
   //disaggregationString=null;
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
    }catch(RemoteException e){
      e.printStackTrace();
    }

    DiscretizedFuncList hazardFuncList = new DiscretizedFuncList();
    for(int i=0; i<numERFs; ++i) {
      ArbitrarilyDiscretizedFunc hazFunction = new ArbitrarilyDiscretizedFunc();

      // intialize the hazard function
      initX_Values(hazFunction);
      try {
        try{
          // calculate the hazard curve
          hazFunction=(ArbitrarilyDiscretizedFunc)calc.getHazardCurve(hazFunction, site, imr, erfList.getERF(i));
          //System.out.println("Num points:" +hazFunction.toString());
        }catch(RemoteException e){
          e.printStackTrace();
        }
        hazFunction = toggleHazFuncLogValues(hazFunction);
      }catch (RuntimeException e) {
        JOptionPane.showMessageDialog(this, e.getMessage(),
                                      "Parameters Invalid", JOptionPane.INFORMATION_MESSAGE);
        e.printStackTrace();
        return;
      }
      hazardFuncList.add(hazFunction);
    }
    //System.out.println("ERF List size:"+erfList.getRelativeWeightsList().size());
    weightedFuncList.addList(erfList.getRelativeWeightsList(),hazardFuncList);
    //System.out.println("ERF Relative List size:"+weightedFuncList.getRelativeWtList().size());
    weightedFuncList.setInfo(getParametersInfo());


   //individual curves are to be plotted
   if(!isAllCurves)
     weightedFuncList.setIndividualCurvesToPlot(false);
   else
     weightedFuncList.setIndividualCurvesToPlot(true);

   // if 5th, 50 and 95th percetile need to be plotted
   if(this.percentileOption.equalsIgnoreCase
      (ERF_EpistemicListControlPanel.FIVE_50_95_PERCENTILE)) {
     weightedFuncList.setFractilesToPlot(true);
     ArrayList fractionList = new ArrayList();
     fractionList.add(new Double(.05));
     fractionList.add(new Double(.50));
     fractionList.add(new Double(.95));
     weightedFuncList.addFractiles(fractionList);
   } else if(this.percentileOption.equalsIgnoreCase // for custom percentile
      (ERF_EpistemicListControlPanel.CUSTOM_PERCENTILE )) {
     double fraction = this.epistemicControlPanel.getCustomPercentileValue();
     weightedFuncList.setFractilesToPlot(true);
     weightedFuncList.addFractile(fraction/100);
   }else weightedFuncList.setFractilesToPlot(false);

   // calculate average
   if(this.avgSelected) {
     weightedFuncList.setMeanToPlot(true);
     weightedFuncList.addMean();
   }else weightedFuncList.setMeanToPlot(false);

   //adding the data to the functionlist if adding on top
   if(addData)
     functionList.add(weightedFuncList);
   // set the X-axis label
   totalProbFuncs.setXAxisName(imtGuiBean.getSelectedIMT());
   totalProbFuncs.setYAxisName("Probability of Exceedance");
  }


  /**
   * Initialize the IMR Gui Bean
   */
  private void initIMR_GuiBean() {

     imrGuiBean = new IMR_GuiBean();
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
  protected void initERF_GuiBean() {
     // create the ERF Gui Bean object
   ArrayList erf_Classes = new ArrayList();
   //adding the RMI based ERF's to the application
   erf_Classes.add(RMI_FRANKEL_ADJ_FORECAST_CLASS_NAME);
   erf_Classes.add(RMI_STEP_FORECAST_CLASS_NAME);
   erf_Classes.add(RMI_STEP_ALASKA_ERF_CLASS_NAME);
   erf_Classes.add(RMI_FLOATING_POISSON_FAULT_ERF_CLASS_NAME);
   erf_Classes.add(RMI_FRANKEL02_ADJ_FORECAST_CLASS_NAME);
   erf_Classes.add(RMI_PEER_AREA_FORECAST_CLASS_NAME);
   erf_Classes.add(RMI_PEER_MULTI_SOURCE_FORECAST_CLASS_NAME);
   erf_Classes.add(RMI_PEER_NON_PLANAR_FAULT_FORECAST_CLASS_NAME);
   erf_Classes.add(RMI_WG02_ERF_LIST_CLASS_NAME);
   erf_Classes.add(RMI_POISSON_FAULT_ERF_CLASS_NAME);
   try{
     erfGuiBean = new ERF_GuiBean(erf_Classes);
   }catch(InvocationTargetException e){
     e.printStackTrace();
     throw new RuntimeException("Connection to ERF's failed");
   }
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
    try{
      EqkRupForecastAPI eqkRupForecast = erfGuiBean.getSelectedERF_Instance();
      // create the TimeSpan Gui Bean object
      timeSpanGuiBean = new TimeSpanGuiBean(eqkRupForecast.getTimeSpan());
    }catch(Exception e){
      e.printStackTrace();
    }
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
    this.controlComboBox.addItem(DISTANCE_CONTROL);
    this.controlComboBox.addItem(SITES_OF_INTEREST_CONTROL);
    this.controlComboBox.addItem(CVM_CONTROL);
    this.controlComboBox.addItem(X_VALUES_CONTROL);
    this.controlComboBox.addItem(RUN_ALL_PEER_TESTS);
    this.controlComboBox.addItem(MAP_CALC_CONTROL);
    this.controlComboBox.addItem(PLOTTING_OPTION);
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
    else if(selectedControl.equalsIgnoreCase(PLOTTING_OPTION))
      initPlotSelectionControl();
    controlComboBox.setSelectedItem(this.CONTROL_PANELS);
  }


  /**
   * This function allows the user to select new data would be added to the
   * existing plot , if any.
   */
  private void initPlotSelectionControl(){
    if(plotOptionControl ==  null)
      plotOptionControl = new PlottingOptionControl(this);
    plotOptionControl.show();
    plotOptionControl.pack();
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
          imrGuiBean, siteGuiBean, imtGuiBean, erfGuiBean, timeSpanGuiBean,
          this.distanceControlPanel);
    if(runAllPEER_Tests == null)
      runAllPEER_Tests = new RunAll_PEER_TestCasesControlPanel(this);
    runAllPEER_Tests.show();
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
          imrGuiBean, siteGuiBean, imtGuiBean, erfGuiBean, timeSpanGuiBean,
          this.distanceControlPanel);
    peerTestsControlPanel.setPEER_XValues();
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
    sitesOfInterest.show();
  }

  /**
   * Initialize the Interesting sites control panel
   * It will provide a pick list of interesting sites
   */
  private void initCVMControl() {
    if(this.cvmControlPanel==null)
      cvmControlPanel = new SetSiteParamsFromWebServicesControlPanel(this, this.imrGuiBean, this.siteGuiBean);
    cvmControlPanel.pack();
    cvmControlPanel.show();
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
    xValuesPanel.show();
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
   * @returns the instance to the JPanel showing the JFreechart adn metadata
   */
  public GraphPanel getGraphPanel(){
    return graphPanel;
  }

  /**
   *
   * @returns the String containing the values selected for different parameters
   */
  public String getParametersInfo(){
    String systemSpecificLineSeparator = SystemPropertiesUtils.getSystemLineSeparator();
    return "IMR Param List:" +systemSpecificLineSeparator+
           "---------------"+systemSpecificLineSeparator+
        this.imrGuiBean.getVisibleParametersCloned().getParameterListMetadataString()+systemSpecificLineSeparator+systemSpecificLineSeparator+
        "Site Param List: "+systemSpecificLineSeparator+
        "----------------"+systemSpecificLineSeparator+
        siteGuiBean.getParameterListEditor().getVisibleParametersCloned().getParameterListMetadataString()+systemSpecificLineSeparator+
        systemSpecificLineSeparator+"IMT Param List: "+systemSpecificLineSeparator+
        "---------------"+systemSpecificLineSeparator+
        imtGuiBean.getVisibleParametersCloned().getParameterListMetadataString()+systemSpecificLineSeparator+
        systemSpecificLineSeparator+"Forecast Param List: "+systemSpecificLineSeparator+
        "--------------------"+systemSpecificLineSeparator+
        erfGuiBean.getParameterList().getParameterListMetadataString()+systemSpecificLineSeparator+
        systemSpecificLineSeparator+"TimeSpan Param List: "+systemSpecificLineSeparator+
        "--------------------"+systemSpecificLineSeparator+
        timeSpanGuiBean.getParameterListMetadataString()+systemSpecificLineSeparator;
  }

  /**
   * sets up the connection with the servlet on the server (scec.usc.edu)
   */
  private ArbitrarilyDiscretizedFunc sendParametersToServlet(Site site,
                                       AttenuationRelationshipAPI imr,
                                       String eqkRupForecastLocation,ArbitrarilyDiscretizedFunc hazFunction) {

    try{
      if(D) System.out.println("starting to make connection with servlet");
      URL hazardCurveCalcServlet = new URL(SERVLET_URL);


      URLConnection servletConnection = hazardCurveCalcServlet.openConnection();
      if(D) System.out.println("connection established");

      // inform the connection that we will send output and accept input
      servletConnection.setDoInput(true);
      servletConnection.setDoOutput(true);

      // Don't use a cached version of URL connection.
      servletConnection.setUseCaches (false);
      servletConnection.setDefaultUseCaches (false);
      // Specify the content type that we will send binary data
      servletConnection.setRequestProperty ("Content-Type","application/octet-stream");

      ObjectOutputStream toServlet = new
          ObjectOutputStream(servletConnection.getOutputStream());

      if(!useCustomX_Values)
        function = imtInfo.getDefaultHazardCurve(imtGuiBean.getSelectedIMT());

      //sending the object of the gridded region sites to the servlet
      toServlet.writeObject(site);
      //sending the IMR object to the servlet
      toServlet.writeObject(imr);
      //sending the EQK forecast object to the servlet
      toServlet.writeObject(eqkRupForecastLocation);
      //Sending the serialized Arbitrary Discretized Func to the server
      //ArrayList list = new ArrayList();
      //for(int i = 0; i<function.getNum(); ++i) list.add(new String(""+function.getX(i)));
      toServlet.writeObject(hazFunction);
      // send the MAX DISTANCE
      Double maxDistance;
      if(distanceControlPanel == null ) maxDistance = new Double(HazardCurveCalculator.MAX_DISTANCE_DEFAULT);
      else maxDistance = new Double(distanceControlPanel.getDistance());
      toServlet.writeObject(maxDistance);

      toServlet.flush();
      toServlet.close();

      // Receive the datasetnumber from the servlet after it has received all the data
      ObjectInputStream fromServlet = new ObjectInputStream(servletConnection.getInputStream());

      //returns the ArbitrarilyDiscretizedFunc. from the servlet.
      ArbitrarilyDiscretizedFunc func=(ArbitrarilyDiscretizedFunc)fromServlet.readObject();
      fromServlet.close();
      return func;

    }catch (Exception e) {
      System.out.println("Exception in connection with servlet:" +e);
      e.printStackTrace();
    }
    return null;
 }




  /**
   *
   * @returns the DiscretizedFuncList for all the data curves
   */
  public DiscretizedFuncList getCurveFunctionList(){
    return totalProbFuncs;
  }


  /**
   *
   * @returns the DiscretizedFunctionXYDataSet to the data
   */
  public DiscretizedFunctionXYDataSet getXY_DataSet(){
    return data;
  }


  /**
   * Actual method implementation of the "Peel-Off"
   * This function peels off the window from the current plot and shows in a new
   * window. The current plot just shows empty window.
   */
  private void peelOffCurves(){
    graphWindow = new GraphWindow(this);
    clearPlot(true);
    graphWindow.show();
  }


  void imgLabel_mousePressed(MouseEvent e) {

  }
  void imgLabel_mouseReleased(MouseEvent e) {

  }
  void imgLabel_mouseEntered(MouseEvent e) {

  }
  void imgLabel_mouseExited(MouseEvent e) {

  }

  /**
   * Action method to "Peel-Off" the curves graph window in a seperate window.
   * This is called when the user presses the "Peel-Off" window.
   * @param e
   */
  void peelOffButton_actionPerformed(ActionEvent e) {
    peelOffCurves();
  }

}
