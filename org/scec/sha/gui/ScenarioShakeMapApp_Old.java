package org.scec.sha.gui;

import java.awt.*;
import java.awt.event.*;
import java.applet.*;
import javax.swing.*;
import javax.swing.border.*;
import java.util.*;
import java.io.*;
import java.lang.reflect.InvocationTargetException;

import ch.randelshofer.quaqua.QuaquaManager;

import org.scec.sha.gui.beans.*;
import org.scec.sha.imr.*;
import org.scec.sha.earthquake.EqkRupForecastAPI;
import org.scec.sha.earthquake.EqkRupForecast;
import org.scec.param.event.*;
import org.scec.data.region.SitesInGriddedRegion;
import org.scec.data.Site;
import org.scec.sha.earthquake.ProbEqkRupture;
import org.scec.sha.gui.controls.*;
import org.scec.sha.gui.infoTools.*;
import org.scec.data.ArbDiscretizedXYZ_DataSet;
import org.scec.data.XYZ_DataSetAPI;
import org.scec.sha.calc.ScenarioShakeMapCalculator;
import org.scec.sha.earthquake.ERF_API;
import org.scec.exceptions.ParameterException;



/**
 * <p>Title: ScenarioShakeMapApp_Old</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author: Ned Field & Nitin Gupta & Vipin Gupta
 * @created : March 21,2003
 * @version 1.0
 */

public class ScenarioShakeMapApp_Old extends JApplet implements ParameterChangeListener,
    GenerateHazusFilesConrolPanelAPI,RunAll_PuenteHillsScenariosControlPanelAPI{


  /**
   * Name of the class
   */
  protected final static String C = "ScenarioShakeMapApp";
  // for debug purpose
  protected final static boolean D = false;



  //variables that determine the width and height of the frame
  private static final int W=550;
  private static final int H=760;

  // default insets
  private Insets defaultInsets = new Insets( 4, 4, 4, 4 );


  //reference to the  XYZ dataSet
  private XYZ_DataSetAPI xyzDataSet;


  //store the site values for each site in the griddded region
  private SitesInGriddedRegion griddedRegionSites;

  //stores the IML or Prob selection and their value for which we want to compute the
  //scenario shake map. Value we get from the respective guibeans.
  private boolean probAtIML=false;
  private  double imlProbValue;

  //gets the instance of the selected AttenuationRelationship
  private AttenuationRelationship attenRel;

  //Instance to the ShakeMap calculator to get the XYZ data for the selected scenario
  //making the object for the ScenarioShakeMapCalculator to get the XYZ data.
  private ScenarioShakeMapCalculator shakeMapCalc = new ScenarioShakeMapCalculator();


  /**
   *  The object class names for all the supported Eqk Rup Forecasts
   */

  public final static String PEER_AREA_FORECAST_CLASS_NAME = "org.scec.sha.earthquake.rupForecastImpl.PEER_TestCases.PEER_AreaForecast";
  public final static String PEER_NON_PLANAR_FAULT_FORECAST_CLASS_NAME = "org.scec.sha.earthquake.rupForecastImpl.PEER_TestCases.PEER_NonPlanarFaultForecast";
  public final static String SIMPLE_POISSON_FAULT_FORECAST_CLASS_NAME = "org.scec.sha.earthquake.rupForecastImpl.SimplePoissonFaultERF";
  public final static String SIMPLE_FAULT_FORECAST_CLASS_NAME = "org.scec.sha.earthquake.rupForecastImpl.SimpleFaultRuptureERF";
  public final static String PEER_MULTI_SOURCE_FORECAST_CLASS_NAME = "org.scec.sha.earthquake.rupForecastImpl.PEER_TestCases.PEER_MultiSourceForecast";
  public final static String PEER_LOGIC_TREE_FORECAST_CLASS_NAME = "org.scec.sha.earthquake.rupForecastImpl.PEER_TestCases.PEER_LogicTreeERF_List";
  public final static String FRANKEL_FORECAST_CLASS_NAME = "org.scec.sha.earthquake.rupForecastImpl.Frankel96.Frankel96_EqkRupForecast";
  public final static String FRANKEL_ADJ_FORECAST_CLASS_NAME = "org.scec.sha.earthquake.rupForecastImpl.Frankel96.Frankel96_AdjustableEqkRupForecast";
  public final static String STEP_FORECAST_CLASS_NAME = "org.scec.sha.earthquake.rupForecastImpl.step.STEP_EqkRupForecast";
  public final static String WG02_FORECAST_CLASS_NAME = "org.scec.sha.earthquake.rupForecastImpl.WG02.WG02_EqkRupForecast";
  public final static String PUENTE_HILLS_FORECAST_CLASS_NAME = "org.scec.sha.earthquake.rupForecastImpl.PuenteHillsERF.PuenteHillsFaultERF";
  public final static String FRANKEL02_ADJ_FORECAST_CLASS_NAME = "org.scec.sha.earthquake.rupForecastImpl.Frankel02.Frankel02_AdjustableEqkRupForecast";

  // Strings for control pick list
  private final static String CONTROL_PANELS = "Control Panels";
  private final static String REGIONS_OF_INTEREST_CONTROL = "Regions of Interest";
  private final static String PUENTE_HILLS_TEST_CONTROL = "Set Params for Puente Hills Test";
  private final static String PUENTE_HILLS_CONTROL = "Set Params for Puente Hills Scenario";
  private final static String HAZUS_CONTROL = "Generate Hazus Shape files for Scenario";
  //private final static String RUN_ALL_CASES_FOR_PUENTE_HILLS = "Run all Puente Hills Scenarios";

    // objects for control panels
  private RegionsOfInterestControlPanel regionsOfInterest;
  private PuenteHillsScenarioTestControlPanel puenteHillsTestControl;
  private PuenteHillsScenarioControlPanel puenteHillsControl;
  private GenerateHazusFilesControlPanel hazusControl;
  private RunAll_PuenteHillsScenariosControlPanel puenteHillsScenariosControl;

  // instances of the GUI Beans which will be shown in this applet
  private EqkRupSelectorGuiBean erfGuiBean;
  private IMR_GuiBean imrGuiBean;
  private IMT_GuiBean imtGuiBean;
  private SitesInGriddedRegionGuiBean sitesGuiBean;
  private IMLorProbSelectorGuiBean imlProbGuiBean;
  private MapGuiBean mapGuiBean;
  private TimeSpanGuiBean timeSpanGuiBean;

  private boolean isStandalone = false;
  private JPanel mainPanel = new JPanel();
  private Border border1;
  private JSplitPane mainSplitPane = new JSplitPane();
  private JPanel buttonPanel = new JPanel();
  private JPanel eqkRupPanel = new JPanel();
  private GridBagLayout gridBagLayout3 = new GridBagLayout();
  private GridBagLayout gridBagLayout2 = new GridBagLayout();
  private JPanel gmtPanel = new JPanel();
  private JSplitPane imr_IMTSplit = new JSplitPane();
  private JTabbedPane parameterTabbedPanel = new JTabbedPane();
  private JPanel timespanPanel = new JPanel();
  private JPanel imrPanel = new JPanel();
  private JPanel imtPanel = new JPanel();
  private JPanel prob_IMLPanel = new JPanel();
  private BorderLayout borderLayout2 = new BorderLayout();
  private GridBagLayout gridBagLayout9 = new GridBagLayout();
  private GridBagLayout gridBagLayout8 = new GridBagLayout();
  private JButton addButton = new JButton();
  private JPanel gridRegionSitePanel = new JPanel();
  private GridLayout gridLayout1 = new GridLayout();
  private GridBagLayout gridBagLayout1 = new GridBagLayout();
  private GridBagLayout gridBagLayout5 = new GridBagLayout();
  private JPanel imrSelectionPanel = new JPanel();
  JComboBox controlComboBox = new JComboBox();
  GridBagLayout gridBagLayout6 = new GridBagLayout();
  BorderLayout borderLayout1 = new BorderLayout();
  private CalcProgressBar calcProgress;
  private int step;


  private GridBagLayout gridBagLayout4 = new GridBagLayout();
  //Get a parameter value
  public String getParameter(String key, String def) {
    return isStandalone ? System.getProperty(key, def) :
      (getParameter(key) != null ? getParameter(key) : def);
  }

  //Construct the applet
  public ScenarioShakeMapApp_Old() {
  }
  //Initialize the applet
  public void init() {
    try {
      // initialize the control pick list
      initControlList();
      jbInit();
    }
    catch(Exception e) {
      e.printStackTrace();
    }
    try{
      initIMRGuiBean();
    }catch(RuntimeException e){
      JOptionPane.showMessageDialog(this,"Invalid parameter value",e.getMessage(),JOptionPane.ERROR_MESSAGE);
      return;
    }
    this.initGriddedRegionGuiBean();
    this.initIMTGuiBean();
    try{
        this.initERFSelector_GuiBean();
        initTimeSpanGuiBean();
      }catch(RuntimeException e){
      JOptionPane.showMessageDialog(this,"Could not create ERF Object","Error occur in ERF",
                                    JOptionPane.OK_OPTION);
      return;
      }

    this.initImlProb_GuiBean();
    this.initMapGuiBean();
  }
  //Component initialization
  private void jbInit() throws Exception {
    border1 = new EtchedBorder(EtchedBorder.RAISED,new Color(248, 254, 255),new Color(121, 124, 136));
    this.setSize(new Dimension(564, 721));
    this.getContentPane().setLayout(borderLayout1);
    mainPanel.setBorder(border1);
    mainPanel.setLayout(gridBagLayout6);
    mainSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
    buttonPanel.setLayout(gridBagLayout4);
    eqkRupPanel.setLayout(gridBagLayout1);
    gmtPanel.setLayout(gridBagLayout9);
    imr_IMTSplit.setOrientation(JSplitPane.VERTICAL_SPLIT);
    timespanPanel.setLayout(gridBagLayout3);
    imrPanel.setLayout(borderLayout2);
    imtPanel.setLayout(gridBagLayout8);
    prob_IMLPanel.setLayout(gridBagLayout2);
    addButton.setText("Make Map");
    addButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        addButton_actionPerformed(e);
      }
    });
    buttonPanel.setMinimumSize(new Dimension(391, 50));
    gridRegionSitePanel.setLayout(gridLayout1);
    imrSelectionPanel.setLayout(gridBagLayout5);
    controlComboBox.setBackground(Color.white);
    controlComboBox.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        controlComboBox_actionPerformed(e);
      }
    });
    this.getContentPane().add(mainPanel, BorderLayout.CENTER);
    mainPanel.add(mainSplitPane,  new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(1, 1, 2, 3), 0, 431));
    mainSplitPane.add(buttonPanel, JSplitPane.BOTTOM);
    buttonPanel.add(controlComboBox,  new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(48, 41, 47, 0), 5, 2));
    buttonPanel.add(addButton,  new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(48, 88, 39, 139), 26, 9));
    mainSplitPane.add(parameterTabbedPanel, JSplitPane.TOP);
    imr_IMTSplit.add(imtPanel, JSplitPane.BOTTOM);
    imr_IMTSplit.add(imrSelectionPanel, JSplitPane.TOP);
    imrPanel.add(imr_IMTSplit, BorderLayout.CENTER);
    parameterTabbedPanel.addTab("Intensity-Measure Relationship", imrPanel);
    parameterTabbedPanel.addTab("Region & Site Params", gridRegionSitePanel);
    parameterTabbedPanel.addTab("Earthquake Rupture from Forecast", eqkRupPanel );
    parameterTabbedPanel.addTab("Time Span", timespanPanel);
    parameterTabbedPanel.addTab( "Exceedance Level/Probability", prob_IMLPanel);
    parameterTabbedPanel.addTab("Map Attributes", gmtPanel);
    mainSplitPane.setDividerLocation(580);
    imr_IMTSplit.setDividerLocation(300);
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
    ScenarioShakeMapApp_Old applet = new ScenarioShakeMapApp_Old();
    applet.isStandalone = true;
    JFrame frame = new JFrame();
    //EXIT_ON_CLOSE == 3
    frame.setDefaultCloseOperation(3);
    frame.setTitle("ScenarioShakeMap App");
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
   * Initialise the Gridded Region sites gui bean
   *
   */
  private void initGriddedRegionGuiBean(){
    // get the selected IMR
     attenRel = (AttenuationRelationship)imrGuiBean.getSelectedIMR_Instance();
     // create the Site Gui Bean object
     sitesGuiBean = new SitesInGriddedRegionGuiBean();
     sitesGuiBean.replaceSiteParams(attenRel.getSiteParamsIterator());
     // show the sitebean in JPanel
     gridRegionSitePanel.add(this.sitesGuiBean, new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0,
         GridBagConstraints.CENTER, GridBagConstraints.BOTH, defaultInsets, 0, 0 ));
  }

  /**
   * Initialise the IMT gui Bean
   */
  private void initIMTGuiBean(){
    // get the selected IMR
    attenRel = (AttenuationRelationship)imrGuiBean.getSelectedIMR_Instance();
    /**
     * Initialize the IMT Gui Bean
     */

    // create the IMT Gui Bean object
    imtGuiBean = new IMT_GuiBean(attenRel);
    imtGuiBean.getParameterEditor(imtGuiBean.IMT_PARAM_NAME).getParameter().addParameterChangeListener(this);
    imtPanel.add(imtGuiBean, new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0,
        GridBagConstraints.CENTER, GridBagConstraints.BOTH, defaultInsets, 0, 0 ));
  }

  /**
   * Initialize the IMR Gui Bean
   */
  private void initIMRGuiBean() {

     imrGuiBean = new IMR_GuiBean();
     imrGuiBean.getParameterEditor(imrGuiBean.IMR_PARAM_NAME).getParameter().addParameterChangeListener(this);

     // show this IMRgui bean the Panel
    imrSelectionPanel.add(this.imrGuiBean,new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0,
         GridBagConstraints.CENTER, GridBagConstraints.BOTH, defaultInsets, 0, 0 ));

  }

  /**
   * Initialize the ERF Gui Bean
   */
  private void initERFSelector_GuiBean() {
     // create the ERF Gui Bean object
   ArrayList erf_Classes = new ArrayList();

//   erf_Classes.add(FRANKEL_FORECAST_CLASS_NAME);
   erf_Classes.add(FRANKEL_ADJ_FORECAST_CLASS_NAME);
   //erf_Classes.add(FRANKEL02_ADJ_FORECAST_CLASS_NAME);
//   erf_Classes.add(PEER_AREA_FORECAST_CLASS_NAME);
   erf_Classes.add(PEER_NON_PLANAR_FAULT_FORECAST_CLASS_NAME);
   erf_Classes.add(SIMPLE_POISSON_FAULT_FORECAST_CLASS_NAME);
   erf_Classes.add(SIMPLE_FAULT_FORECAST_CLASS_NAME);
//   erf_Classes.add(PEER_MULTI_SOURCE_FORECAST_CLASS_NAME);
//   erf_Classes.add(PEER_LOGIC_TREE_FORECAST_CLASS_NAME);
   erf_Classes.add(STEP_FORECAST_CLASS_NAME);
   erf_Classes.add(WG02_FORECAST_CLASS_NAME);
   erf_Classes.add(PUENTE_HILLS_FORECAST_CLASS_NAME);
   erf_Classes.add(FRANKEL02_ADJ_FORECAST_CLASS_NAME);
   try{
     erfGuiBean = new EqkRupSelectorGuiBean(erf_Classes);
   }catch(InvocationTargetException e){
     throw new RuntimeException("Problem creating ERF");
   }
   eqkRupPanel.add(erfGuiBean, new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0,
                GridBagConstraints.CENTER,GridBagConstraints.BOTH, defaultInsets, 0, 0 ));
   erfGuiBean.getParameter(erfGuiBean.ERF_PARAM_NAME).addParameterChangeListener(this);
  }

  /**
   * Initialise the IMT_Prob Selector Gui Bean
   */
  private void initImlProb_GuiBean(){
    imlProbGuiBean = new IMLorProbSelectorGuiBean();
    prob_IMLPanel.add(imlProbGuiBean, new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0,
                GridBagConstraints.CENTER,GridBagConstraints.BOTH, defaultInsets, 0, 0 ));
    imlProbGuiBean.setIMLConstraintBasedOnSelectedIMT(imtGuiBean.getSelectedIMT());
  }

  /**
   * Initialize the TimeSpan gui bean
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
    this.timespanPanel.add(this.timeSpanGuiBean, new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0,
        GridBagConstraints.CENTER, GridBagConstraints.BOTH, defaultInsets, 0, 0 ));
  }

  /**
   * Sets the GMT Params
   */
  private void initMapGuiBean(){
    mapGuiBean = new MapGuiBean();
    mapGuiBean.showRegionParams(false);
    gmtPanel.add(mapGuiBean, new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0,
        GridBagConstraints.CENTER, GridBagConstraints.BOTH, defaultInsets, 0, 0 ));
    double minLat=((Double)sitesGuiBean.getParameterList().getParameter(sitesGuiBean.MIN_LATITUDE).getValue()).doubleValue();
    double maxLat=((Double)sitesGuiBean.getParameterList().getParameter(sitesGuiBean.MAX_LATITUDE).getValue()).doubleValue();
    double minLon=((Double)sitesGuiBean.getParameterList().getParameter(sitesGuiBean.MIN_LONGITUDE).getValue()).doubleValue();
    double maxLon=((Double)sitesGuiBean.getParameterList().getParameter(sitesGuiBean.MAX_LONGITUDE).getValue()).doubleValue();
    double gridSpacing=((Double)sitesGuiBean.getParameterList().getParameter(sitesGuiBean.GRID_SPACING).getValue()).doubleValue();
    mapGuiBean.setRegionParams(minLat,maxLat,minLon,maxLon,gridSpacing);
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
  public void parameterChange(ParameterChangeEvent event){

    String S = C + ": parameterChange(): ";

    String name1 = event.getParameterName();

    // if IMR selection changed, update the site parameter list and supported IMT
    if ( name1.equalsIgnoreCase(imrGuiBean.IMR_PARAM_NAME)) {
      attenRel = (AttenuationRelationship)imrGuiBean.getSelectedIMR_Instance();
      imtGuiBean.setIMR(attenRel);
      imtGuiBean.getParameterEditor(imtGuiBean.IMT_PARAM_NAME).getParameter().addParameterChangeListener(this);
      imtGuiBean.validate();
      imtGuiBean.repaint();
      sitesGuiBean.replaceSiteParams(attenRel.getSiteParamsIterator());
      sitesGuiBean.validate();
      sitesGuiBean.repaint();
    }
    if(name1.equalsIgnoreCase(this.erfGuiBean.ERF_PARAM_NAME))
       /* get the selected ERF
       NOTE : We have used erfGuiBean.getSelectedERF_Instance()INSTEAD OF
       erfGuiBean.getSelectedERF.
       Dofference is that erfGuiBean.getSelectedERF_Instance() does not update
       the forecast while erfGuiBean.getSelectedERF updates the ERF
       */
      this.timeSpanGuiBean.setTimeSpan(erfGuiBean.getSelectedERF_Instance().getTimeSpan());

    if(name1.equalsIgnoreCase(imtGuiBean.IMT_PARAM_NAME)){
      System.out.println("Changing IMT");
      imlProbGuiBean.setIMLConstraintBasedOnSelectedIMT(imtGuiBean.getSelectedIMT());
    }


  }

  /**
   *
   * @returns the Sites Values for each site in the region chosen by the user
   */
  private void getGriddedRegionSites(){
    try {
      griddedRegionSites = sitesGuiBean.getGriddedRegionSite();
    }catch(ParameterException e) {
      throw  new ParameterException(e.getMessage());
    }
    catch(Exception e){
      throw new RuntimeException(e.getMessage());
    }
  }

  /**
   * gets the IML or Prob selected option and its value from the respective guiBean
   */
  private void getIMLorProb(){
    imlProbValue=imlProbGuiBean.getIML_Prob();
    String imlOrProb=imlProbGuiBean.getSelectedOption();
    if(imlOrProb.equalsIgnoreCase(imlProbGuiBean.PROB_AT_IML))
      probAtIML=true;
    else
      probAtIML= false;
  }

  /**
   * This method calculates the probablity or the IML for the selected Gridded Region
   * and stores the value in each vectors(lat-ArrayList, Lon-ArrayList and IML or Prob ArrayList)
   * The IML or prob vector contains value based on what the user has selected in the Map type
   */
  public XYZ_DataSetAPI generateShakeMap() throws ParameterException,RuntimeException{
    try {
      // this function will get the selected IMT parameter and set it in IMT
      imtGuiBean.setIMT();
      //if the IMT selected is Log supported then take the log if Prob @ IML
      if(IMT_Info.isIMT_LogNormalDist(imtGuiBean.getSelectedIMT()) && probAtIML)
        imlProbValue = Math.log(imlProbValue);
      //does the calculation for the ScenarioShakeMap Calc and gives back a XYZ dataset
      xyzDataSet = shakeMapCalc.getScenarioShakeMapData(griddedRegionSites,attenRel,erfGuiBean.getRupture(),
                                                        probAtIML,imlProbValue);
      //if the IMT is log supported then take the exponential of the Value if IML @ Prob
      if(IMT_Info.isIMT_LogNormalDist(imtGuiBean.getSelectedIMT()) && !probAtIML){
        ArrayList zVals = xyzDataSet.getZ_DataSet();
        int size = zVals.size();
        for(int i=0;i<size;++i){
          double tempVal = Math.exp(((Double)(zVals.get(i))).doubleValue());
          zVals.set(i,new Double(tempVal));
        }
      }
    }catch(ParameterException e){
      throw new ParameterException(e.getMessage());
    }
    return xyzDataSet;
  }


  /**
   * Sets the GMT Region coordinates
   */
  private void setRegionForGMT(){
    double minLat=((Double)sitesGuiBean.getParameterList().getParameter(sitesGuiBean.MIN_LATITUDE).getValue()).doubleValue();
    double maxLat=((Double)sitesGuiBean.getParameterList().getParameter(sitesGuiBean.MAX_LATITUDE).getValue()).doubleValue();
    double minLon=((Double)sitesGuiBean.getParameterList().getParameter(sitesGuiBean.MIN_LONGITUDE).getValue()).doubleValue();
    double maxLon=((Double)sitesGuiBean.getParameterList().getParameter(sitesGuiBean.MAX_LONGITUDE).getValue()).doubleValue();
    double gridSpacing=((Double)sitesGuiBean.getParameterList().getParameter(sitesGuiBean.GRID_SPACING).getValue()).doubleValue();
    mapGuiBean.setRegionParams(minLat,maxLat,minLon,maxLon,gridSpacing);
  }


  /**
   * This function sets the Gridded region Sites and the type of plot user wants to see
   * IML@Prob or Prob@IML and it value.
   */
  public void getGriddedSitesAndMapType(){
    calcProgress = new CalcProgressBar("ShakeMapApp","Starting ShakeMap Calculation");
    //gets the IML or Prob selected value
    getIMLorProb();
    //get the site values for each site in the gridded region
    getGriddedRegionSites();
    calcProgress.dispose();
    calcProgress.showProgress(false);
  }

  void addButton_actionPerformed(ActionEvent e) {
   //sets the Gridded region Sites and the type of plot user wants to see
   //IML@Prob or Prob@IML and it value.
    if(hazusControl == null || !hazusControl.isHazusShapeFilesButtonPressed())
      getGriddedSitesAndMapType();
    //checking if the person wants to run all the cases for the Puente Hill Scenarios
    if(puenteHillsScenariosControl !=null){
      puenteHillsScenariosControl.runAllScenarios(puenteHillsControl,hazusControl,imrGuiBean,erfGuiBean);
      puenteHillsScenariosControl = null;
    }
    else //if the person just want to run one scenario at time.
      addButton();
    hazusControl = null;
  }

  /**
   * when the generate Map button is pressed
   */
  public void addButton(){
    step = 1;
    try{
      if(step ==1)
        calcProgress = new CalcProgressBar("ShakeMapApp","  Calculating ShakeMap Data ...");
      if(hazusControl == null || !hazusControl.isHazusShapeFilesButtonPressed())
        generateShakeMap();
      //sets the region coordinates for the GMT using the MapGuiBean
      setRegionForGMT();
      ++step;
    }catch(ParameterException e){
      JOptionPane.showMessageDialog(this,e.getMessage(),"Invalid Parameters",JOptionPane.ERROR_MESSAGE);
      calcProgress.showProgress(false);
      calcProgress.dispose();
      return;
    }
    catch(Exception ee){
      ee.printStackTrace();
      JOptionPane.showMessageDialog(this,ee.getMessage(),"Server Problem",JOptionPane.INFORMATION_MESSAGE);
      calcProgress.showProgress(false);
      calcProgress.dispose();
      return;
    }
    if(step==2) {
      calcProgress.setProgressMessage("  Generating the Map ...");

      String label;
      String imlOrProb=imlProbGuiBean.getSelectedOption();
      if(imlOrProb.equalsIgnoreCase(imlProbGuiBean.PROB_AT_IML))
        label="Prob";
      else
        label=imtGuiBean.getSelectedIMT();
      try{
        if(hazusControl !=null && hazusControl.isHazusShapeFilesButtonPressed())
            mapGuiBean.makeHazusShapeFilesAndMap(hazusControl.getXYZ_DataForSA_03(),hazusControl.getXYZ_DataForSA_10(),
                hazusControl.getXYZ_DataForPGA(),hazusControl.getXYZ_DataForPGV(),
                erfGuiBean.getRupture(),label,getMapParametersInfo());
          else
            mapGuiBean.makeMap(xyzDataSet,erfGuiBean.getRupture(),label,getMapParametersInfo());
      }catch(RuntimeException e){
        e.printStackTrace();
        calcProgress.showProgress(false);
        calcProgress.dispose();
        return;
      }
      calcProgress.dispose();
    }
  }



  /**
   * Initialize the items to be added to the control list
   */
  private void initControlList() {
    this.controlComboBox.addItem(CONTROL_PANELS);
    this.controlComboBox.addItem(REGIONS_OF_INTEREST_CONTROL);
    this.controlComboBox.addItem(PUENTE_HILLS_TEST_CONTROL);
    this.controlComboBox.addItem(PUENTE_HILLS_CONTROL);
    this.controlComboBox.addItem(HAZUS_CONTROL);
    //this.controlComboBox.addItem(RUN_ALL_CASES_FOR_PUENTE_HILLS);
  }

  /**
   * This function is called when controls pick list is chosen
   * @param e
   */
  void controlComboBox_actionPerformed(ActionEvent e) {
    if(controlComboBox.getItemCount()<=0) return;
    String selectedControl = controlComboBox.getSelectedItem().toString();
    if(selectedControl.equalsIgnoreCase(this.REGIONS_OF_INTEREST_CONTROL))
      initRegionsOfInterestControl();
    else if(selectedControl.equalsIgnoreCase(this.PUENTE_HILLS_TEST_CONTROL))
      initPuenteHillTestScenarioControl();
    else if(selectedControl.equalsIgnoreCase(this.PUENTE_HILLS_CONTROL))
      initPuenteHillScenarioControl();
    else if(selectedControl.equalsIgnoreCase(this.HAZUS_CONTROL))
      initHazusScenarioControl();
    /*else if(selectedControl.equalsIgnoreCase(this.RUN_ALL_CASES_FOR_PUENTE_HILLS))
      initRunAllPuenteHillsScenariosControl();*/
    controlComboBox.setSelectedItem(this.CONTROL_PANELS);
  }



  /**
   * Initialises the Run all Puente Hills Scenario Control
   * to run all the cases for it after iterating over all the Attenuations and
   * Magnitudes.
   */
  private void initRunAllPuenteHillsScenariosControl(){
    int selectedOption = JOptionPane.showConfirmDialog(this,"Are you sure to run all cases for Puente Hill Scenarios?",
                                    "Run all Puente Hills Scenarios Control",JOptionPane.YES_NO_CANCEL_OPTION);
    if(selectedOption == JOptionPane.OK_OPTION){
      //creating the instance of the Puente Hills if not already instantiated
      if(puenteHillsControl == null){
        puenteHillsControl = new PuenteHillsScenarioControlPanel(erfGuiBean,imrGuiBean,
            sitesGuiBean,mapGuiBean,imtGuiBean);
        puenteHillsControl.setParamsForPuenteHillsScenario();
      }
      //creating the instance to generate the shape files for Hazus if not already instantiated
      if(hazusControl == null){
        hazusControl = new GenerateHazusFilesControlPanel(this,imtGuiBean,this);
      }
      //creating the instance to run all the scenarios for the Puente Hills.
      if(puenteHillsScenariosControl==null){
        puenteHillsScenariosControl = new RunAll_PuenteHillsScenariosControlPanel(this);
      }
    }
  }

  /**
   * Initialize the Interesting regions control panel
   * It will provide a pick list of interesting regions
   */
  private void initRegionsOfInterestControl() {
    if(this.regionsOfInterest==null)
      regionsOfInterest = new RegionsOfInterestControlPanel(this, this.sitesGuiBean);
    regionsOfInterest.pack();
    regionsOfInterest.show();
  }

  /**
   * Initialize the Interesting regions control panel
   * It will provide a pick list of interesting regions
   */
  private void initPuenteHillTestScenarioControl() {
    int selectedOption = JOptionPane.showConfirmDialog(this,"Are you sure to set the Parameters to Puente Hills Test?",
                                    "Puente Hills Control",JOptionPane.YES_NO_CANCEL_OPTION);
    if(selectedOption == JOptionPane.OK_OPTION){
      if(this.puenteHillsTestControl==null)
        puenteHillsTestControl = new PuenteHillsScenarioTestControlPanel(this.erfGuiBean, this.imrGuiBean,
                                                                this.sitesGuiBean,this.mapGuiBean,this.imtGuiBean);
      puenteHillsTestControl.setParamsForPuenteHillsScenario();
    }
  }

  /**
   *Initialise the Control panel to generate the shapefiles for hazus input.
   */
  private void initHazusScenarioControl(){
    if(hazusControl == null)
      hazusControl = new GenerateHazusFilesControlPanel(this,imtGuiBean,this);
    hazusControl.show();
    hazusControl.pack();
  }



  /**
   * Initialize the Interesting regions control panel
   * It will provide a pick list of interesting regions
   */
  private void initPuenteHillScenarioControl() {
    int selectedOption = JOptionPane.showConfirmDialog(this,"Are you sure to set the Parameters to Puente Hills Scenario?",
                                    "Puente Hills Control",JOptionPane.YES_NO_CANCEL_OPTION);
    if(selectedOption == JOptionPane.OK_OPTION){
      if(this.puenteHillsControl==null)
        puenteHillsControl = new PuenteHillsScenarioControlPanel(this.erfGuiBean, this.imrGuiBean,
                                                                this.sitesGuiBean,this.mapGuiBean,this.imtGuiBean);
      puenteHillsControl.setParamsForPuenteHillsScenario();
    }
  }

  /**
   *
   * @returns the selected Attenuationrelationship model
   */
  public AttenuationRelationship getSelectedAttenuationRelationship(){
    return attenRel;
  }

  /**
   *
   * @returns the String containing the values selected for different parameters
   */
  public String getMapParametersInfo(){

    String imtMetadata = null;
    //if the Hazus Control for Sceario is selected the get the metadata for IMT from there
    if(hazusControl !=null && hazusControl.isHazusShapeFilesButtonPressed())
      imtMetadata = hazusControl.getIMT_Metadata();
    else //else get the metadata from the IMT GuiBean.
      //imtMetadata = imtGuiBean.getVisibleParametersCloned().getParameterListMetadataString();
      imtMetadata = imtGuiBean.getParameterListMetadataString();
    return "IMR Param List:<br>\n " +
           "---------------<br>\n"+
        this.imrGuiBean.getVisibleParameters().getParameterListMetadataString()+"\n"+
        "<br><br>Region Param List: <br>\n"+
        "----------------<br>\n"+
        sitesGuiBean.getVisibleParameters().getParameterListMetadataString()+"\n"+
        "<br><br>IMT Param List: <br>\n"+
        "---------------<br>\n"+
        imtMetadata+"\n"+
        "<br><br>Forecast Param List: <br>\n"+
        "--------------------<br>\n"+
        erfGuiBean.getParameterListMetadataString()+"\n"+
        "<br><br>TimeSpan Param List: <br>\n"+
        "--------------------<br>\n"+
        timeSpanGuiBean.getVisibleParameters().getParameterListMetadataString()+"\n"+
        "<br><br>GMT Param List: <br>\n"+
        "--------------------<br>\n"+
        mapGuiBean.getVisibleParameters().getParameterListMetadataString()+"\n";
  }
}
