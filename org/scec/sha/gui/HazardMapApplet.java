package org.scec.sha.gui;

import java.awt.*;
import java.awt.event.*;
import java.applet.*;
import javax.swing.*;
import javax.swing.border.*;
import java.util.Vector;
import java.util.Iterator;
import java.net.*;
import java.io.*;



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
import org.scec.sha.imr.AttenuationRelationshipAPI;
import org.scec.sha.earthquake.EqkRupForecastAPI;
import org.scec.sha.calc.HazardMapCalculator;
import org.scec.sha.calc.DisaggregationCalculator;
import org.scec.data.Site;

/**
 * <p>Title: HazardMapApplet</p>
 * <p>Description: </p>
 * @author Nitin Gupta and Vipin Gupta
 * Date : March 12 , 2003
 * @version 1.0
 */

public class HazardMapApplet extends JApplet implements
    ParameterChangeListener {

  /**
   * Name of the class
   */
  protected final static String C = "HazardMapApplet";
  // for debug purpose
  protected final static boolean D = false;

  /**
   *  The object class names for all the supported attenuation ralations (IMRs)
   *  Temp until figure out way to dynamically load classes during runtime
   */
  public final static String BJF_CLASS_NAME = "org.scec.sha.imr.attenRelImpl.BJF_1997_AttenRel";
  public final static String AS_CLASS_NAME = "org.scec.sha.imr.attenRelImpl.AS_1997_AttenRel";
  public final static String C_CLASS_NAME = "org.scec.sha.imr.attenRelImpl.Campbell_1997_AttenRel";
  public final static String SCEMY_CLASS_NAME = "org.scec.sha.imr.attenRelImpl.SCEMY_1997_AttenRel";
  public final static String F_CLASS_NAME = "org.scec.sha.imr.attenRelImpl.Field_2000_AttenRel";
  public final static String A_CLASS_NAME = "org.scec.sha.imr.attenRelImpl.Abrahamson_2000_AttenRel";
  public final static String CB_CLASS_NAME = "org.scec.sha.imr.attenRelImpl.CB_2003_AttenRel";

  /**
   *  The object class names for all the supported Eqk Rup Forecasts
   */
  public final static String PEER_FAULT_FORECAST_CLASS_NAME = "org.scec.sha.earthquake.PEER_TestCases.PEER_FaultForecast";
  public final static String PEER_AREA_FORECAST_CLASS_NAME = "org.scec.sha.earthquake.PEER_TestCases.PEER_AreaForecast";
  public final static String PEER_NON_PLANAR_FAULT_FORECAST_CLASS_NAME = "org.scec.sha.earthquake.PEER_TestCases.PEER_NonPlanarFaultForecast";
  public final static String PEER_LISTRIC_FAULT_FORECAST_CLASS_NAME = "org.scec.sha.earthquake.PEER_TestCases.PEER_ListricFaultForecast";
  public final static String PEER_MULTI_SOURCE_FORECAST_CLASS_NAME = "org.scec.sha.earthquake.PEER_TestCases.PEER_MultiSourceForecast";

  // instances of the GUI Beans which will be shown in this applet
  ERF_GuiBean erfGuiBean;
  IMR_GuiBean imrGuiBean;
  IMT_GuiBean imtGuiBean;
  GriddedRegionSiteGuiBean sitesGuiBean;


  // mesage needed in case of show data if plot is not available
  final static String NO_PLOT_MSG = "No Plot Data Available";

  private Insets plotInsets = new Insets( 4, 10, 4, 4 );

  private boolean isStandalone = false;
  private Border border1;


  //log flags declaration
  boolean xLog =false;
  boolean yLog =false;

  // default insets
  Insets defaultInsets = new Insets( 4, 4, 4, 4 );

  // height and width of the applet
  protected final static int W = 600;
  protected final static int H = 750;

  /**
   * FunctionList declared
   */
  DiscretizedFuncList totalProbFuncs = new DiscretizedFuncList();

  DiscretizedFunctionXYDataSet data = new DiscretizedFunctionXYDataSet();

  //Disaggregation Parameter
  DoubleParameter disaggregationParam = new DoubleParameter("Disaggregation Prob",
                                                             0,1,new Double(.01));






  private GridBagLayout gridBagLayout4 = new GridBagLayout();
  private GridBagLayout gridBagLayout6 = new GridBagLayout();
  private GridBagLayout gridBagLayout7 = new GridBagLayout();
  private GridBagLayout gridBagLayout3 = new GridBagLayout();

  /**
   * adding scroll pane for showing data
   */
  JScrollPane dataScrollPane = new JScrollPane();

  // text area to show the data values
  JTextArea pointsTextArea = new JTextArea();

  /**
   * chart panel
   */
  ChartPanel chartPanel;

  //flag to check for the disaggregation functionality
  private boolean disaggregationFlag= false;
  private String disaggregationString;

  // PEER Test Cases
  private String TITLE = new String("PEER Test Cases");

  // light blue color
  Color lightBlue = new Color( 200, 200, 230 );

  /**
   * for Y-log, 0 values will be converted to this small value
   */
  private double Y_MIN_VAL = 1e-16;

  //coordinates position of the centre of the applet
  int xCenter;
  int yCenter;

  protected boolean graphOn = false;
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
  private JSplitPane topSplitPane = new JSplitPane();
  private JPanel buttonPanel = new JPanel();
  private JButton addButton = new JButton();
  private Border border6;
  private Border border7;
  private Border border8;



  //images for the OpenSHA
  private final static String FRAME_ICON_NAME = "openSHA_Aqua_sm.gif";
  private final static String POWERED_BY_IMAGE = "PoweredBy.gif";

  //static string for the OPENSHA website
  private final static String OPENSHA_WEBSITE="http://www.OpenSHA.org";

  private JPanel erfPanel = new JPanel();
  private JSplitPane parameterSplitPane = new JSplitPane();
  private JSplitPane controlsSplit = new JSplitPane();
  private JSplitPane siteSplitPane = new JSplitPane();
  private JPanel imtPanel = new JPanel();
  private JPanel sitePanel = new JPanel();
  private JPanel imrPanel = new JPanel();
  private GridBagLayout gridBagLayout5 = new GridBagLayout();
  private GridBagLayout gridBagLayout8 = new GridBagLayout();
  private GridBagLayout gridBagLayout9 = new GridBagLayout();
  private GridBagLayout gridBagLayout10 = new GridBagLayout();
  private GridBagLayout gridBagLayout12 = new GridBagLayout();
  private GridBagLayout gridBagLayout13 = new GridBagLayout();
  private BorderLayout borderLayout1 = new BorderLayout();
  private FlowLayout flowLayout1 = new FlowLayout();

  //Get command-line parameter value
  public String getParameter(String key, String def) {
    return isStandalone ? System.getProperty(key, def) :
        (getParameter(key) != null ? getParameter(key) : def);
  }

  //Construct the applet
  public HazardMapApplet() {

  data.setFunctions(this.totalProbFuncs);
  // for Y-log, convert 0 values in Y axis to this small value
  data.setConvertZeroToMin(true,Y_MIN_VAL);
  }
  //Initialize the applet
  public void init() {
    try {

      jbInit();
      xCenter=getAppletXAxisCenterCoor();
      yCenter=getAppletYAxisCenterCoor();

      // create the IMR Gui Bean object
      // It accepts the vector of IMR class names
      Vector imrClasses = new Vector();
      imrClasses.add(this.A_CLASS_NAME);
      imrClasses.add(this.AS_CLASS_NAME);
      imrClasses.add(this.BJF_CLASS_NAME);
      imrClasses.add(this.C_CLASS_NAME);
      imrClasses.add(this.SCEMY_CLASS_NAME);
      imrClasses.add(this.CB_CLASS_NAME);
      imrClasses.add(this.F_CLASS_NAME);
      imrGuiBean = new IMR_GuiBean(imrClasses);
      imrGuiBean.getParameterEditor(imrGuiBean.IMR_PARAM_NAME).getParameter().addParameterChangeListener(this);
      // show this gui bean the JPanel
      imrPanel.add(this.imrGuiBean,
                new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0,
                GridBagConstraints.CENTER,
                 GridBagConstraints.BOTH, defaultInsets, 0, 0 ));

      // get the selected IMR
      AttenuationRelationshipAPI imr = imrGuiBean.getSelectedIMR_Instance();

      // create the IMT Gui Bean object
      imtGuiBean = new IMT_GuiBean(imr);
      imtPanel.setLayout(gridBagLayout8);
      imtPanel.add(imtGuiBean,
                new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0,
                GridBagConstraints.CENTER,
                 GridBagConstraints.BOTH, defaultInsets, 0, 0 ));


      // create the Site Gui Bean object
      sitesGuiBean = new GriddedRegionSiteGuiBean();
      sitesGuiBean.replaceSiteParams(imr.getSiteParamsIterator());
      // show the sitebean in JPanel
      sitePanel.add(this.sitesGuiBean,
              new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0,
              GridBagConstraints.CENTER,
                GridBagConstraints.BOTH, defaultInsets, 0, 0 ));


      // create the ERF Gui Bean object
      Vector erf_Classes = new Vector();
      erf_Classes.add(PEER_FAULT_FORECAST_CLASS_NAME);
      erf_Classes.add(PEER_AREA_FORECAST_CLASS_NAME);
      erf_Classes.add(PEER_NON_PLANAR_FAULT_FORECAST_CLASS_NAME);
      erf_Classes.add(PEER_LISTRIC_FAULT_FORECAST_CLASS_NAME);
      erf_Classes.add(PEER_MULTI_SOURCE_FORECAST_CLASS_NAME);
      erfGuiBean = new ERF_GuiBean(erf_Classes);
      erfPanel.setLayout(gridBagLayout5);
      erfPanel.add(erfGuiBean,
                   new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0,
                   GridBagConstraints.CENTER,
                    GridBagConstraints.BOTH, defaultInsets, 0, 0 ));

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
    this.setSize(new Dimension(419, 657));
    this.getContentPane().setLayout(borderLayout1);


    // for showing the data on click of "show data" button
    pointsTextArea.setBorder( BorderFactory.createEtchedBorder() );
    pointsTextArea.setText( NO_PLOT_MSG );
    pointsTextArea.setLineWrap(true);
    dataScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    dataScrollPane.setBorder( BorderFactory.createEtchedBorder() );
    jPanel1.setLayout(gridBagLayout13);



    jPanel1.setBackground(Color.white);
    jPanel1.setBorder(border4);
    jPanel1.setMinimumSize(new Dimension(959, 600));
    jPanel1.setPreferredSize(new Dimension(959, 600));
    topSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
    topSplitPane.setBottomComponent(buttonPanel);
    topSplitPane.setDividerSize(5);
    //clearButton.setBorder(null);
    //toggleButton.setBorder(null);
    buttonPanel.setBackground(Color.white);
    buttonPanel.setBorder(border1);
    buttonPanel.setMaximumSize(new Dimension(2147483647, 40));
    buttonPanel.setMinimumSize(new Dimension(726, 40));
    buttonPanel.setPreferredSize(new Dimension(726, 40));
    buttonPanel.setLayout(gridBagLayout12);
    addButton.setBackground(new Color(200, 200, 230));
    addButton.setFont(new java.awt.Font("Dialog", 1, 11));
    addButton.setForeground(new Color(80, 80, 133));
    addButton.setBorder(null);
    //addButton.setBorder(null);
    addButton.setMaximumSize(new Dimension(97, 31));
    addButton.setMinimumSize(new Dimension(97, 31));
    addButton.setPreferredSize(new Dimension(97, 31));
    addButton.setText("Run Map Solver");
    addButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        addButton_actionPerformed(e);
      }
    });

    //erfGuiBean.setLayout(flowLayout1);
    erfPanel.setLayout(gridBagLayout10);
    erfPanel.setBackground(Color.white);
    erfPanel.setBorder(border2);
    erfPanel.setMaximumSize(new Dimension(2147483647, 10000));
    erfPanel.setMinimumSize(new Dimension(2, 300));
    erfPanel.setPreferredSize(new Dimension(2, 300));
    parameterSplitPane.setLeftComponent(controlsSplit);
    parameterSplitPane.setRightComponent(erfPanel);
    controlsSplit.setOrientation(JSplitPane.VERTICAL_SPLIT);
    controlsSplit.setDividerSize(5);
    siteSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
    siteSplitPane.setDividerSize(5);
    imtPanel.setLayout(gridBagLayout9);
    imtPanel.setBackground(Color.white);
    sitePanel.setLayout(gridBagLayout8);
    sitePanel.setBackground(Color.white);
    imrPanel.setLayout(gridBagLayout5);
    imrPanel.setBackground(Color.white);
    dataScrollPane.getViewport().add( pointsTextArea, null );
    this.getContentPane().add(jPanel1, BorderLayout.CENTER);
    jPanel1.add(topSplitPane,  new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(-1, 9, 10, 4), -340, 272));
    topSplitPane.add(buttonPanel, JSplitPane.BOTTOM);
    buttonPanel.add(addButton,        new GridBagConstraints(0, 0, 1, 4, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(2, 14, 3, 246), 49, 28));
    topSplitPane.add(parameterSplitPane, JSplitPane.TOP);
    controlsSplit.add(imrPanel, JSplitPane.TOP);
    controlsSplit.add(siteSplitPane, JSplitPane.BOTTOM);
    siteSplitPane.add(sitePanel, JSplitPane.TOP);
    siteSplitPane.add(imtPanel, JSplitPane.BOTTOM);
    parameterSplitPane.add(erfPanel, JSplitPane.RIGHT);
    parameterSplitPane.add(controlsSplit, JSplitPane.LEFT);
    topSplitPane.setDividerLocation(550);
    erfPanel.validate();
    erfPanel.repaint();
    parameterSplitPane.setDividerLocation(220);
    controlsSplit.setDividerLocation(185);
    siteSplitPane.setDividerLocation(140);

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
    HazardMapApplet applet = new HazardMapApplet();
    applet.isStandalone = true;
    JFrame frame = new JFrame();
    //EXIT_ON_CLOSE == 3
    frame.setDefaultCloseOperation(3);
    frame.setTitle("Map DataSet Generator");
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
        sitesGuiBean.replaceSiteParams(imr.getSiteParamsIterator());
        sitesGuiBean.validate();
        sitesGuiBean.repaint();
      }
  }

  /**
   * Initialize the X values and the prob as 1
   *
   * @param arb
   */
  private void initDiscretizeValues(ArbitrarilyDiscretizedFunc arb){
    arb.set(.001,1);
    arb.set(.01,1);
    arb.set(.05,1);
    arb.set(.15,1);
    arb.set(.1,1);
    arb.set(.2,1);
    arb.set(.25,1);
    arb.set(.3,1);
    arb.set(.4,1);
    arb.set(.5,1);
    arb.set(.6,1);
    arb.set(.7,1);
    arb.set(.8,1);
    arb.set(.9,1);
    arb.set(1.0,1);
    arb.set(1.1,1);
    arb.set(1.2,1);
    arb.set(1.3,1);
    arb.set(1.4,1);
    arb.set(1.5,1);
  }

  /**
   * Gets the probabilities functiion based on selected parameters
   * this function is called when add Graph is clicked
   */
  public void computeHazardCurve() {

    // intialize the hazard function
    ArbitrarilyDiscretizedFunc hazFunction = new ArbitrarilyDiscretizedFunc();
    initDiscretizeValues(hazFunction);

    // get the selected forecast model
    EqkRupForecastAPI eqkRupForecast = erfGuiBean.getSelectedERF_Instance();

    // get the selected IMR
    AttenuationRelationshipAPI imr = imrGuiBean.getSelectedIMR_Instance();



    try {
      // this function will get the selected IMT parameter and set it in IMT
      imtGuiBean.setIMR_Param();
    } catch (Exception ex) {
      if(D) System.out.println(C + ":Param warning caught"+ex);
      ex.printStackTrace();
    }

    // calculate the hazard curve
   HazardMapCalculator calc = new HazardMapCalculator();
   try {
     //checks if the magFreqDistParameter exists inside it , if so then gets its Editor and
     //calls the method to update the magDistParams.
     erfGuiBean.updateMagDistParam();
     // make a site object to pass to IMR
    Iterator it = sitesGuiBean.getSites();
     // calculate the hazard curve for each site
     //creating the directory that stores all the HazardCurves for that region
     boolean success= (new File("tempdata").mkdir());
     while(it.hasNext()) {
       Site site = (Site)it.next();
       System.out.println(site.toString());
       calc.getHazardCurve(hazFunction, site, imr, eqkRupForecast);
       //hazFunction.setInfo("\n"+getCurveParametersInfo()+"\n");
     }
   }catch (RuntimeException e) {
     JOptionPane.showMessageDialog(this, e.getMessage(),
                                   "Parameters Invalid", JOptionPane.INFORMATION_MESSAGE);
     e.printStackTrace();
     return;
   }

   // add the function to the function list
   totalProbFuncs.add(hazFunction);

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
        "Site Param List: "+sitesGuiBean.getParameterList().toString()+"\n"+
        "IMT Param List: "+imtGuiBean.getParameterList().toString()+"\n"+
        "Forecast Param List: "+erfGuiBean.getParameterList().toString();
  }

}
