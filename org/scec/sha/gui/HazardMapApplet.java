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
import org.scec.data.region.SitesInGriddedRegion;
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
import org.scec.sha.imr.AttenuationRelationship;
import org.scec.sha.earthquake.EqkRupForecastAPI;
import org.scec.sha.earthquake.EqkRupForecast;
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
  //public final static String A_CLASS_NAME = "org.scec.sha.imr.attenRelImpl.Abrahamson_2000_AttenRel";
  public final static String CB_CLASS_NAME = "org.scec.sha.imr.attenRelImpl.CB_2003_AttenRel";

  /**
   *  The object class names for all the supported Eqk Rup Forecasts
   */
  public final static String PEER_FAULT_FORECAST_CLASS_NAME = "org.scec.sha.earthquake.rupForecastImpl.PEER_TestCases.PEER_FaultForecast";
  public final static String PEER_AREA_FORECAST_CLASS_NAME = "org.scec.sha.earthquake.rupForecastImpl.PEER_TestCases.PEER_AreaForecast";
  public final static String PEER_NON_PLANAR_FAULT_FORECAST_CLASS_NAME = "org.scec.sha.earthquake.rupForecastImpl.PEER_TestCases.PEER_NonPlanarFaultForecast";
  public final static String PEER_LISTRIC_FAULT_FORECAST_CLASS_NAME = "org.scec.sha.earthquake.rupForecastImpl.PEER_TestCases.PEER_ListricFaultForecast";
  public final static String PEER_MULTI_SOURCE_FORECAST_CLASS_NAME = "org.scec.sha.earthquake.rupForecastImpl.PEER_TestCases.PEER_MultiSourceForecast";
  public final static String FRANKEL_FORECAST_CLASS_NAME = "org.scec.sha.earthquake.rupForecastImpl.Frankel96.Frankel96_EqkRupForecast";


  // instances of the GUI Beans which will be shown in this applet
  private ERF_GuiBean erfGuiBean;
  private IMR_GuiBean imrGuiBean;
  private IMT_GuiBean imtGuiBean;
  private SitesInGriddedRegionGuiBean griddedRegionGuiBean;
  private TimeSpanGuiBean timeSpanGuiBean;

  private boolean isStandalone = false;
  private Border border1;


  // default insets
  Insets defaultInsets = new Insets( 4, 4, 4, 4 );

  // height and width of the applet
  protected final static int W = 600;
  protected final static int H = 750;

  // PEER Test Cases
  public final static String TITLE = new String("Map DataSet Generator");

  // light blue color
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
  private JSplitPane topSplitPane = new JSplitPane();
  private JPanel buttonPanel = new JPanel();
  private JButton addButton = new JButton();
  private Border border6;
  private Border border7;
  private Border border8;

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
  private FlowLayout flowLayout1 = new FlowLayout();
  private JSplitPane jSplitPane1 = new JSplitPane();
  private JPanel timeSpanPanel = new JPanel();
  private GridBagLayout gridBagLayout12 = new GridBagLayout();
  private GridBagLayout gridBagLayout3 = new GridBagLayout();
  private GridBagLayout gridBagLayout4 = new GridBagLayout();
  private BorderLayout borderLayout1 = new BorderLayout();

  //Get command-line parameter value
  public String getParameter(String key, String def) {
    return isStandalone ? System.getProperty(key, def) :
        (getParameter(key) != null ? getParameter(key) : def);
  }

  //Construct the applet
  public HazardMapApplet() {
  }

  //Initialize the applet
  public void init() {
    try {

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
    this.setSize(new Dimension(419, 657));
    this.getContentPane().setLayout(borderLayout1);

    jPanel1.setLayout(gridBagLayout4);

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
    timeSpanPanel.setLayout(gridBagLayout3);
    this.getContentPane().add(jPanel1, BorderLayout.CENTER);
    jPanel1.add(topSplitPane,  new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(-1, 9, 10, 4), -324, 288));
    topSplitPane.add(buttonPanel, JSplitPane.BOTTOM);
    buttonPanel.add(jSplitPane1,  new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(1, 0, 0, 0), 241, 33));
    jSplitPane1.add(addButton, JSplitPane.LEFT);
    jSplitPane1.add(timeSpanPanel, JSplitPane.RIGHT);
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
    jSplitPane1.setDividerLocation(150);

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
    frame.setTitle(TITLE);
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
      griddedRegionGuiBean.replaceSiteParams(imr.getSiteParamsIterator());
      griddedRegionGuiBean.validate();
      griddedRegionGuiBean.repaint();
      }
      if(name1.equalsIgnoreCase(this.erfGuiBean.ERF_PARAM_NAME)) {
        /* get the selected ERF
        NOTE : We have used erfGuiBean.getSelectedERF_Instance()INSTEAD OF
        erfGuiBean.getSelectedERF.
        Dofference is that erfGuiBean.getSelectedERF_Instance() does not update
        the forecast while erfGuiBean.getSelectedERF updates the
        */
        this.timeSpanGuiBean.setTimeSpan(erfGuiBean.getSelectedERF_Instance().getTimeSpan());
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
    EqkRupForecast eqkRupForecast = (EqkRupForecast)erfGuiBean.getSelectedERF();

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

   // initialize the values in condProbfunc with log values as passed in hazFunction
   ArbitrarilyDiscretizedFunc condProbFunc = new ArbitrarilyDiscretizedFunc();
   initCondProbFunc(hazFunction, condProbFunc);
   try {

     SitesInGriddedRegion griddedRegionSites = griddedRegionGuiBean.getGriddedRegionSite();
     // calculate the hazard curve for each site
     calc.getHazardMapCurves(condProbFunc,hazFunction,griddedRegionSites ,
                             imr, eqkRupForecast, this.getMapParametersInfo());
   }catch (RuntimeException e) {
     JOptionPane.showMessageDialog(this, e.getMessage(),
                                   "Parameters Invalid", JOptionPane.INFORMATION_MESSAGE);
     e.printStackTrace();
     return;
   }

  }


  /**
   *
   * @returns the String containing the values selected for different parameters
   */
  public String getMapParametersInfo(){
    return "IMR Param List: " +this.imrGuiBean.getParameterList().toString()+"\n"+
        "Site Param List: "+griddedRegionGuiBean.getParameterList().toString()+"\n"+
        "IMT Param List: "+imtGuiBean.getParameterList().toString()+"\n"+
        "Forecast Param List: "+erfGuiBean.getParameterList().toString();
  }

  /**
   * set x values in log space for condition Prob function to be passed to IMR
   * It accepts 2 parameters
   *
   * @param originalFunc :  this is the function with X values set
   * @param logFunc : this is the functin in which log X values are set
   */
  private void initCondProbFunc(DiscretizedFuncAPI originalFunc,
                                DiscretizedFuncAPI logFunc){

    int numPoints = originalFunc.getNum();
    String selectedIMT = imtGuiBean.getParameterList().getParameter(IMT_GuiBean.IMT_PARAM_NAME).getValue().toString();

    // take log only if it is PGA, PGV or SA
    if (selectedIMT.equalsIgnoreCase(AttenuationRelationship.PGA_NAME) ||
        selectedIMT.equalsIgnoreCase(AttenuationRelationship.PGV_NAME) ||
        selectedIMT.equalsIgnoreCase(AttenuationRelationship.SA_NAME)) {
      for(int i=0; i<numPoints; ++i)
        logFunc.set(Math.log(originalFunc.getX(i)), 1);
    } else
      throw new RuntimeException("Unsupported IMT");
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
    griddedRegionGuiBean = new SitesInGriddedRegionGuiBean();
    griddedRegionGuiBean.replaceSiteParams(imr.getSiteParamsIterator());
    // show the sitebean in JPanel
    sitePanel.add(this.griddedRegionGuiBean, new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0,
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
  erf_Classes.add(FRANKEL_FORECAST_CLASS_NAME);
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


}
