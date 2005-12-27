package org.opensha.sha.gui;

import java.awt.*;
import java.awt.event.*;
import java.applet.*;
import javax.swing.*;
import javax.swing.border.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.StringTokenizer;
import java.net.*;
import java.lang.reflect.InvocationTargetException;



import org.jfree.chart.*;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.axis.*;
//import org.jfree.chart.tooltips.*;
import org.jfree.data.*;
import org.jfree.data.statistics.Statistics;
import org.jfree.data.time.Day;
import org.jfree.date.SerialDate;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.labels.TimeSeriesToolTipGenerator;
import org.jfree.chart.renderer.*;


import org.opensha.data.function.*;
import org.opensha.gui.*;
import org.opensha.gui.plot.jfreechart.*;
import org.opensha.param.*;
import org.opensha.param.editor.*;
import org.opensha.param.event.*;
import org.opensha.util.*;
import org.opensha.sha.gui.controls.*;
import org.opensha.sha.gui.beans.*;
import org.opensha.sha.gui.infoTools.*;
import org.opensha.sha.imr.AttenuationRelationshipAPI;
import org.opensha.sha.imr.AttenuationRelationship;
import org.opensha.sha.imr.attenRelImpl.WC94_DisplMagRel;
import org.opensha.data.Location;
import org.opensha.data.Site;
import org.opensha.sha.nga.*;
import org.opensha.sha.nga.calc.*;
import org.opensha.data.XYZ_DataSetAPI;



/**
 * <p>Title: ObservedVsPredictedApplet</p>
 * <p>Description: </p>
 * @author Nitin Gupta and Vipin Gupta and Vijesh Mehta
 * Date : Sept 23 , 2002
 * @version 1.0
 */

public class ObservedVsPredictedApplet extends JApplet
    implements ParameterChangeListener{

  /**
   * Name of the class
   */
  private final static String C = "Observed Vs Predicted Plot Application";
  // for debug purpose
  private final static boolean D = false;



  // instances of the GUI Beans which will be shown in this applet
  //private ERF_GuiBean erfGuiBean;
  private IMR_GuiBean imrGuiBean;
  private IMT_GuiBean imtGuiBean;
  private IMLorProbSelectorGuiBean imlProbGuiBean;
  private AttenuationSiteTypeParamsGuiBean siteGuiBean;
  private ObservedRuptureSelectorGuiBean ruptureGuiBean;

  // Strings for control pick list
  private final static String CONTROL_PANELS = "Control Panels";


  private static ScatterPlot splotter;


  // X and Y Magnitude
  private double XMAG = 1.0;
  private double YMAG = 100;
  private double CORRELATION;

  XYSeriesCollection functions = new XYSeriesCollection();



  // Site Variables for GUI
  private javax.swing.JButton browse;
  private JPanel siteInfoPanel;
  private JComboBox unitsDropDown;


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
  private final static int W = 1200;
  private final static int H = 750;

  /**
   * FunctionList declared
   */
  private DiscretizedFuncList totalProbFuncs = new DiscretizedFuncList();
  private DiscretizedFunctionXYDataSet data = new DiscretizedFunctionXYDataSet();

  //holds the ArbitrarilyDiscretizedFunc
  private ArbitrarilyDiscretizedFunc function;

  //instance to get the default IMT X values for the hazard Curve
  private IMT_Info imtInfo = new IMT_Info();


  /**
   * these four values save the custom axis scale specified by user
   */
  private double minXValue;
  private double maxXValue;
  private  double minYValue;
  private double maxYValue;
  private boolean customAxis = false;

  //flags to check which X Values the user wants to work with: default or custom
  boolean useCustomX_Values = false;


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
  private String TITLE = new String("Scatter Plot");

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




  JSplitPane topSplitPane = new JSplitPane();
  JButton clearButton = new JButton();
  JButton saveButton = new JButton();
  //JCheckBox jCheckylog = new JCheckBox();
  JButton toggleButton = new JButton();
  JPanel buttonPanel = new JPanel();
  //JCheckBox progressCheckBox = new JCheckBox();
  JButton addButton = new JButton();
  //JCheckBox jCheckxlog = new JCheckBox();
  JComboBox controlComboBox = new JComboBox();
  JSplitPane chartSplit = new JSplitPane();
  JPanel panel = new JPanel();
  GridBagLayout gridBagLayout9 = new GridBagLayout();
  //JPanel timeSpanPanel = new JPanel();
  GridBagLayout gridBagLayout8 = new GridBagLayout();
  JSplitPane imrSplitPane = new JSplitPane();
  GridBagLayout gridBagLayout5 = new GridBagLayout();
  //JSplitPane erfSplitPane = new JSplitPane();
  JPanel imtPanel = new JPanel();
  JPanel imlPanel = new JPanel();
  JPanel imrPanel = new JPanel();
  JPanel siteParamPanel = new JPanel();
  JSplitPane controlsSplit = new JSplitPane();
  JTabbedPane paramsTabbedPane = new JTabbedPane();

  JPanel erfPanel = new JPanel();
  GridBagLayout gridBagLayout15 = new GridBagLayout();
  GridBagLayout gridBagLayout13 = new GridBagLayout();
  GridBagLayout gridBagLayout12 = new GridBagLayout();

  GridBagLayout gridBagLayout10 = new GridBagLayout();
  BorderLayout borderLayout1 = new BorderLayout();
  CalcProgressBar progressClass;
  //CalcProgressBar disaggProgressClass;
  Timer timer;
  //Timer disaggTimer;
  JComboBox probDeterSelection = new JComboBox();
  FlowLayout flowLayout1 = new FlowLayout();



  //Get command-line parameter value
  public String getParameter(String key, String def) {
    return isStandalone ? System.getProperty(key, def) :
        (getParameter(key) != null ? getParameter(key) : def);
  }

  //Construct the applet
  public ObservedVsPredictedApplet() {
    data.setFunctions(this.totalProbFuncs);
    // for Y-log, convert 0 values in Y axis to this small value
    data.setConvertZeroToMin(true,Y_MIN_VAL);
  }

  //Initialize the applet
  public void init() {
    try {


      // initialize the GUI components
      jbInit();

      // initialize the various GUI beans
      initIMR_GuiBean();

      initImlProb_GuiBean();

      initIMT_GuiBean();
      initRuptureSelectorGuiBean();
      initSiteParamGuiBean();
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
    //this.getContentPane().setBackground(Color.white);
    this.setSize(new Dimension(1100, 670));
    this.getContentPane().setLayout(borderLayout1);


    // for showing the data on click of "show data" button
    pointsTextArea.setBorder( BorderFactory.createEtchedBorder() );
    pointsTextArea.setText( NO_PLOT_MSG );
    pointsTextArea.setLineWrap(true);
    dataScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    dataScrollPane.setBorder( BorderFactory.createEtchedBorder() );
    jPanel1.setLayout(gridBagLayout10);

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

    saveButton.setText("Save Predicted Data");
    saveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                    saveButton_actionPerformed(e);
            }
    });

    toggleButton.setToolTipText("");
    toggleButton.setText("Show Data");
    toggleButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        toggleButton_actionPerformed(e);
      }
    });
    //buttonPanel.setBackground(Color.white);
    buttonPanel.setMinimumSize(new Dimension(568, 20));
    buttonPanel.setLayout(flowLayout1);
    //progressCheckBox.setBackground(Color.white);
    //progressCheckBox.setFont(new java.awt.Font("Dialog", 1, 12));
    //progressCheckBox.setForeground(new Color(80, 80, 133));
    //progressCheckBox.setSelected(true);
    //progressCheckBox.setText("Show Progress Bar");
    addButton.setText("Create Scatter Plot");
    addButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        addButton_actionPerformed(e);
      }
    });
    //jCheckxlog.setBackground(Color.white);
    //jCheckxlog.setFont(new java.awt.Font("Dialog", 1, 11));
    //jCheckxlog.setForeground(new Color(80, 80, 133));
    //jCheckxlog.setText("X Log");
    //jCheckxlog.addActionListener(new java.awt.event.ActionListener() {
    //  public void actionPerformed(ActionEvent e) {
    //    jCheckxlog_actionPerformed(e);
    //  }
   // });
    //controlComboBox.setBackground(new Color(200, 200, 230));
    //controlComboBox.setForeground(new Color(80, 80, 133));
    controlComboBox.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        controlComboBox_actionPerformed(e);
      }
    });
    panel.setLayout(gridBagLayout9);
    panel.setBackground(Color.white);
    panel.setBorder(border5);
    panel.setMinimumSize(new Dimension(0, 0));
    //timeSpanPanel.setLayout(gridBagLayout12);
    imrSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
    imrSplitPane.setBottomComponent(imtPanel);
    imrSplitPane.setTopComponent(imrPanel);
    siteParamPanel.setLayout(gridBagLayout12);
    siteParamPanel.setBackground(Color.white);
    siteParamPanel.setBorder(border2);
    //erfSplitPane.setBottomComponent(i);
    //erfSplitPane.setTopComponent(erfPanel);

    imlPanel.setLayout(gridBagLayout8);
    imlPanel.setBackground(Color.white);
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
    probDeterSelection.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        //probDeterSelection_actionPerformed(e);
      }
    });
    dataScrollPane.getViewport().add( pointsTextArea, null );
    this.getContentPane().add(jPanel1, BorderLayout.CENTER);
    jPanel1.add(topSplitPane,  new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(11, 4, 5, 6), 243, 231));
    //buttonPanel.add(probDeterSelection, null);
    //buttonPanel.add(controlComboBox, null);
    buttonPanel.add(addButton, null);
    buttonPanel.add(saveButton,null);
    buttonPanel.add(clearButton, null);
    buttonPanel.add(toggleButton, null);
    topSplitPane.add(chartSplit, JSplitPane.TOP);
    chartSplit.add(panel, JSplitPane.LEFT);
    chartSplit.add(paramsTabbedPane, JSplitPane.RIGHT);
    imrSplitPane.add(imrPanel, JSplitPane.TOP);
    imrSplitPane.add(imtPanel, JSplitPane.BOTTOM);
    controlsSplit.add(imrSplitPane, JSplitPane.LEFT);
    paramsTabbedPane.add(controlsSplit, "IMR, IML/Prob, & Site");
    controlsSplit.add(siteParamPanel, JSplitPane.RIGHT);
    paramsTabbedPane.add(erfPanel, "Rupture Selector");
    paramsTabbedPane.add(imlPanel,"Set What to Plot?");

    topSplitPane.add(buttonPanel, JSplitPane.BOTTOM);
    topSplitPane.setDividerLocation(600);
    imrSplitPane.setDividerLocation(340);
    //erfSplitPane.setDividerLocation(260);
    controlsSplit.setDividerLocation(260);
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
    return "Applet Information";
  }

  //Get parameter info
  public String[][] getParameterInfo() {
    return null;
  }

  //Main method
  public static void main(String[] args) {
    ObservedVsPredictedApplet applet = new ObservedVsPredictedApplet();
    applet.isStandalone = true;
    JFrame frame = new JFrame();
    //EXIT_ON_CLOSE == 3
    frame.setDefaultCloseOperation(3);
    frame.setTitle(C);
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
         UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
     }
     catch(Exception e) {
     }
  }


  /**
   *  Adds a feature to the GraphPanel attribute of the EqkForecastApplet object
   */
  private void addGraphPanel()
  {
    try{

      // gets the chart from the Scatter Plotter
      JFreeChart chart = splotter.createOverlaidChart("Observed data - " + imtGuiBean.getParameterListMetadataString(),
                                                                                                      "Predicted data - " + imrGuiBean.getSelectedIMR_Name() + " " + imtGuiBean.getParameterListMetadataString());

      chartPanel = new ChartPanel(chart, true, true, true, true, true);
      chartPanel.setBorder( BorderFactory.createEtchedBorder( EtchedBorder.LOWERED ) );
      chartPanel.setMouseZoomable(true);
      chartPanel.setDisplayToolTips(true);
      chartPanel.setHorizontalAxisTrace(false);
      chartPanel.setVerticalAxisTrace(false);


    }catch(RuntimeException e){
      e.printStackTrace();
      JOptionPane.showMessageDialog(this,e.getMessage(),"Invalid Plot",JOptionPane.OK_OPTION);
      return;
    }
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

       panel.validate();
       panel.repaint();

       if ( D ) System.out.println( S + "Ending" );

    }

    /**
     * this function is called when Add Graph button is clicked
     * @param e
     */
    void addButton_actionPerformed(ActionEvent e) {
      addButton();
    }

    void saveButton_actionPerformed(ActionEvent e) {

            if (chartPanel != null ) {
                    String fileOut = saveFile();

                    //if (fileOut != null)
                      //      hazardCurveGenerator.outputDataFile(fileOut);
            }
    }



    /**
     * this function is called to draw the graph - scatter plot
     */
    private void addButton() {
      // do not show warning messages in IMR gui bean. this is needed
      // so that warning messages for site parameters are not shown when Add graph is clicked
      imrGuiBean.showWarningMessages(false);
      //calc = new HazardCurveCalculator();

      this.computeHazardCurve();
      this.drawGraph();
    }

    /**
     * to draw the graph
     */
    private void drawGraph() {
      // you can show warning messages now
     imrGuiBean.showWarningMessages(true);
/*
     // set the log values
     data.setXLog(xLog);
     data.setYLog(yLog);
*/
     // set the data in the text area
     String xAxisTitle =  totalProbFuncs.getXAxisName();
     String yAxisTitle =  totalProbFuncs.getYAxisName();

   //  this.pointsTextArea.setText(totalProbFuncs.toString());
     addGraphPanel();
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
    chartPanel = null;
    pointsTextArea.setText(NO_PLOT_MSG);
    //hazardCurveGenerator = null;
    this.togglePlot();
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
    if( name1.equalsIgnoreCase(imrGuiBean.IMR_PARAM_NAME)) {
      AttenuationRelationshipAPI imr = imrGuiBean.getSelectedIMR_Instance();
      imtGuiBean.setIMR(imr);
      imtGuiBean.getParameterEditor(imtGuiBean.IMT_PARAM_NAME).getParameter().addParameterChangeListener(this);
      // sets Site specific values
      siteGuiBean.replaceSiteParams(imr.getSiteParamsIterator());
      siteGuiBean.refreshParamEditor();
      siteParamPanel.repaint();
      siteParamPanel.validate();
    }
    else if(name1.equalsIgnoreCase(imtGuiBean.IMT_PARAM_NAME))
      siteGuiBean.readIM_DataFile();
    else if(name1.equalsIgnoreCase(this.ruptureGuiBean.RUPTURE_PARAM_NAME))
      siteGuiBean.readIM_DataFile();
    else if(name1.equalsIgnoreCase(siteGuiBean.SITE_PARAM_NAME))
      ruptureGuiBean.readSiteInfoFromFile();
  }

  /**
   * Gets the probabilities function based on selected parameters
   * this function is called when create plot is clicked
   */
  private void computeHazardCurve() {


    //gets the Label for the Y-axis for the earlier grph
    String oldY_AxisLabel="";
    if(totalProbFuncs.getYAxisName() !=null)
      oldY_AxisLabel = totalProbFuncs.getYAxisName();

    // get the selected IMR
    AttenuationRelationship imr = (AttenuationRelationship)imrGuiBean.getSelectedIMR_Instance();
    // this function will get the selected IMT parameter and set it in IMT
    imtGuiBean.setIMT();

    siteGuiBean.setSites();

    //what selection the user has made, IML@Prob or Prob@IML
    String imlOrProb=imlProbGuiBean.getSelectedOption();
    boolean isProbAtIml = false;
    if ( imlOrProb.equalsIgnoreCase(imlProbGuiBean.PROB_AT_IML) )
      isProbAtIml = true;

    //gets the IML or Prob value filled in by the user
    double imlProbValue=imlProbGuiBean.getIML_Prob();

    double value=imlProbValue;
    //if the IMT selected is Log supported then take the log if Prob @ IML
    if(IMT_Info.isIMT_LogNormalDist(getSelectedIMT()) && isProbAtIml)
      value = Math.log(imlProbValue);

    PEER_NGA_HazardCalc calc = new PEER_NGA_HazardCalc();
    EqkRuptureFromNGA rupture = (EqkRuptureFromNGA)ruptureGuiBean.getSelectedRupture();
    String imt = getSelectedIMT();
    XYZ_DataSetAPI xyzdata= calc.getXYZData(rupture,imrGuiBean.getSelectedIMR_Instance(),
        isProbAtIml,imlProbValue);

    //if IMT is log normal supported.
    if(IMT_Info.isIMT_LogNormalDist(imt) && !isProbAtIml){
      ArrayList zVals = xyzdata.getZ_DataSet();
      int size = zVals.size();
      for(int i=0;i<size;++i){
        double tempVal = Math.exp(((Double)(zVals.get(i))).doubleValue());
        zVals.set(i,new Double(tempVal));
      }
    }


    ArrayList xVals = rupture.getObservedRuptureSiteIMList();
    ArrayList yVals = xyzdata.getZ_DataSet();


   /* // Creates a list of sites from the observed XYZ file
    //ArrayList sites = makeSiteList(XFILE);

    //hazardCurveGenerator = new HazCurvGenerator();


    //hazardCurveGenerator.runShakeMapDataCalculation(imr, XFILE, sites, (EqkRupForecast)eqkRupForecast,
    //                                                                erfRupSelectorGuiBean.getSourceIndex(),
    //                                                                erfRupSelectorGuiBean.getRuptureIndex(),
    //                                                              isProbAtIml,
    //                                                            imlProbValue,
    //                                                          imtGuiBean.getParameterListMetadataString()	);

     USED FOR FAST LOCAL COMPUTATION *
    hazardCurveGenerator.runShakeMapDataCalculation(imr, XFILE, null, (EqkRupForecast)eqkRupForecast,
                                erfRupSelectorGuiBean.getSourceIndex(),
                        erfRupSelectorGuiBean.getRuptureIndex(),
                        isProbAtIml,
                        imlProbValue,
                        imtGuiBean.getParameterListMetadataString()	);


                        //String metadata = hazardCurveGenerator.getMetadata();


                        //progressClass.dispose();

                        // Setup the Scatter Plot
                        //setUnitMagnitude();
                        //ArrayList xVal = getZValues(XFILE,XMAG);
                        //ArrayList yVal = hazardCurveGenerator.getResults();*/

    /*System.out.println("Observed Values");
    for(int i=0;i<xVals.size();++i)
      System.out.println(i+": "+xVals.get(i));


    System.out.println("Predicted Values");
    for(int i=0;i<yVals.size();++i)
      System.out.println(i+": "+yVals.get(i));*/


    // use splotter (ScatterPlot class) to plot the values
    splotter = new ScatterPlot(xVals,yVals);
    ArbitrarilyDiscretizedFunc function = new ArbitrarilyDiscretizedFunc();
    splotter.fillValues(); // fill the function with proper scatter values

    String metadata = getCurveParametersInfo() +"\n\n\n"+
                      splotter.getInfoForPlot();
    pointsTextArea.setText(metadata);
    this.addGraphPanel();
  }



  /**
   *
   * @returns the String containing the values selected for different parameters
   */
  public String getCurveParametersInfo(){
    String paramInfo= null;

    paramInfo="IMR Param List: " +imrGuiBean.getParameterList().toString()+"\n"+
              "IMT Param List: " +imtGuiBean.getParameterListMetadataString()+"\n"+
              "Site Param List: "+siteGuiBean.getParameterList().toString()+"\n"+
              "IML OR Prob Param List: "+imlProbGuiBean.getParameterList().toString()+"\n"+
              "Rupture Param List: "+ruptureGuiBean.getParameterListEditor().getParameterList().toString();
    return paramInfo;
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
   * Initialise the IMT_Prob Selector Gui Bean
   */
  private void initImlProb_GuiBean(){
    imlProbGuiBean = new IMLorProbSelectorGuiBean();
    this.imlPanel.add(imlProbGuiBean, new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0,
                GridBagConstraints.CENTER,GridBagConstraints.BOTH, defaultInsets, 0, 0 ));
  }




  /**
   * Initialize the IMT Gui Bean
   */
  private void initIMT_GuiBean() {

     // get the selected IMR
     AttenuationRelationshipAPI imr = imrGuiBean.getSelectedIMR_Instance();
     // create the IMT Gui Bean object
     imtGuiBean = new IMT_GuiBean(imr);
     imtGuiBean.getParameterEditor(imtGuiBean.IMT_PARAM_NAME).getParameter().addParameterChangeListener(this);
     imtPanel.add(imtGuiBean, new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0,
               GridBagConstraints.CENTER, GridBagConstraints.BOTH, defaultInsets, 0, 0 ));

  }


  private void initSiteParamGuiBean(){
    siteGuiBean = new AttenuationSiteTypeParamsGuiBean(this);
    siteParamPanel.setLayout(gridBagLayout8);
    // get the selected IMR
     AttenuationRelationshipAPI imr = imrGuiBean.getSelectedIMR_Instance();
     siteGuiBean.replaceSiteParams(imr.getSiteParamsIterator());
     siteGuiBean.getParameterEditor(siteGuiBean.SITE_PARAM_NAME).getParameter().addParameterChangeListener(this);
    siteParamPanel.add(siteGuiBean, new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0,
               GridBagConstraints.CENTER, GridBagConstraints.BOTH, defaultInsets, 0, 0 ));
  }


  private void initRuptureSelectorGuiBean(){
    ReadNGA_DataFile dataFile = new ReadNGA_DataFile();
    dataFile.ReadData();
    ArrayList ruptureList = dataFile.getNGA_ObservedRuptureList();
    ruptureGuiBean = new ObservedRuptureSelectorGuiBean(ruptureList);
    ruptureGuiBean.getParameterListEditor().getParameterEditor(ruptureGuiBean.RUPTURE_PARAM_NAME).getParameter().addParameterChangeListener(this);
    erfPanel.add(ruptureGuiBean, new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0,
          GridBagConstraints.CENTER, GridBagConstraints.BOTH, defaultInsets, 0, 0 ));

  }


  /**
   * This function is called when controls pick list is chosen
   * @param e
   */
  void controlComboBox_actionPerformed(ActionEvent e) {
    if(controlComboBox.getItemCount()<=0) return;
    String selectedControl = controlComboBox.getSelectedItem().toString();

    controlComboBox.setSelectedItem(this.CONTROL_PANELS);
  }



  /**
   *
   * @returns the selected IMT
   */
  public String getSelectedIMT(){
    return imtGuiBean.getSelectedIMT();
  }


  /**
   *
   * @returns the selected Sa Period
   */
  public String getSelectedSAPeriod(){
    return imtGuiBean.getParameterEditor(AttenuationRelationship.PERIOD_NAME).getValue().toString();
  }


  /**
   *
   * @returns the selected rupture Id
   */
  public EqkRuptureFromNGA getSelectedRupture(){
    EqkRuptureFromNGA rupture = (EqkRuptureFromNGA)ruptureGuiBean.getSelectedRupture();
    return rupture;
  }



  /**
   * Save File - loads a dialog and gets a file which is compatible.
   * @return Absolute File name to save at or null otherwise.
   */
   private String saveFile()
   {

       boolean error = false;

       // 1) Open File Dialog to select file
       javax.swing.JFileChooser chooser = new javax.swing.JFileChooser();
       int option = chooser.showSaveDialog(this);
       if (option == javax.swing.JFileChooser.APPROVE_OPTION) {
           java.io.File file = chooser.getSelectedFile();
           if (file == null)
               return null;
           else
               return file.getAbsolutePath();
       }
       else if (option == javax.swing.JFileChooser.CANCEL_OPTION) {
           error = true;
           return null;
       }

       return null;
   }


   /**
    *
    * @returns the Observed Rupture Vs30 values
    */
   public ArrayList getObservedRuptureVs30Values(){
     return ((EqkRuptureFromNGA)ruptureGuiBean.getSelectedRupture()).getSiteVs30();
   }



  /** loads a file from the local machine using a browse dialog
   *
   * @return String the absolute path to the file
   */
  private String loadFile()
  {
      boolean error = false;

      // 1) Open File Dialog to select file
      javax.swing.JFileChooser chooser = new javax.swing.JFileChooser();
      chooser.setApproveButtonText("select");
      chooser.setFileSelectionMode(javax.swing.JFileChooser.FILES_ONLY);
      int option = chooser.showOpenDialog(this);
      if (option == javax.swing.JFileChooser.APPROVE_OPTION) {
          java.io.File file = chooser.getSelectedFile();
          if (file == null)
              return null;
          else
              return file.getAbsolutePath();
      }
      else if (option == javax.swing.JFileChooser.CANCEL_OPTION) {
          error = true;
          return null;
      }

      return null;
  }
}






