package org.scec.sha.gui;

import java.awt.*;
import java.awt.event.*;
import java.applet.*;
import javax.swing.*;
import javax.swing.border.*;
import java.util.*;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.Thread;
import ch.randelshofer.quaqua.QuaquaManager;
import javax.swing.Timer;

import org.scec.sha.gui.beans.*;
import org.scec.sha.imr.*;
import org.scec.sha.earthquake.EqkRupForecastAPI;
import org.scec.sha.earthquake.EqkRupForecast;
import org.scec.param.event.*;
import org.scec.param.*;
import org.scec.data.region.SitesInGriddedRegion;
import org.scec.data.Site;
import org.scec.sha.earthquake.ProbEqkRupture;
import org.scec.sha.gui.controls.*;
import org.scec.sha.gui.infoTools.*;
import org.scec.data.ArbDiscretizedXYZ_DataSet;
import org.scec.data.XYZ_DataSetAPI;
import org.scec.sha.calc.ScenarioShakeMapCalculator;
import org.scec.sha.calc.ScenarioShakeMapCalculatorWithPropagationEffect;
import org.scec.sha.earthquake.ERF_API;
import org.scec.exceptions.ParameterException;


/**
 * <p>Title: ScenarioShakeMapAttenRelApp_Temp</p>
 * <p>Description: This application provides the flexibility to plot shakemaps
 *  using the single Attenuation as well as the multiple attenuation relationships.</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class ScenarioShakeMapAttenRelApp_Temp extends JApplet implements ParameterChangeListener,
    AttenuationRelationshipSiteParamsRegionAPI,Runnable{

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

  // stores the instances of the selected AttenuationRelationships
  private ArrayList attenRel;
  //stores the instance of the selected AttenuationRelationships wts after normalization
  private ArrayList attenRelWts;

  //Instance to the ShakeMap calculator to get the XYZ data for the selected scenario
  //making the object for the ScenarioShakeMapCalculator to get the XYZ data.
  private ScenarioShakeMapCalculatorWithPropagationEffect shakeMapCalc = new ScenarioShakeMapCalculatorWithPropagationEffect();

  //timer to show thw progress bar
  Timer timer;

  //Metadata String
  private static String mapParametersInfo = null;

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
  public final static String FRANKEL02_ADJ_FORECAST_CLASS_NAME = "org.scec.sha.earthquake.rupForecastImpl.Frankel02.Frankel02_AdjustableEqkRupForecast";
  public final static String STEP_FORECAST_CLASS_NAME = "org.scec.sha.earthquake.rupForecastImpl.step.STEP_EqkRupForecast";
  public final static String WG02_FORECAST_CLASS_NAME = "org.scec.sha.earthquake.rupForecastImpl.WG02.WG02_EqkRupForecast";
  public final static String PUENTE_HILLS_FORECAST_CLASS_NAME = "org.scec.sha.earthquake.rupForecastImpl.PuenteHillsERF.PuenteHillsFaultERF";

  // Strings for control pick list
  private final static String CONTROL_PANELS = "Control Panels";
  private final static String REGIONS_OF_INTEREST_CONTROL = "Regions of Interest";
  /*private final static String PUENTE_HILLS_TEST_CONTROL = "Set Params for Puente Hills Test";
  private final static String PUENTE_HILLS_CONTROL = "Set Params for Puente Hills Scenario";*/
  private final static String HAZUS_CONTROL = "Generate Hazus Shape files for Scenario";
  //private final static String RUN_ALL_CASES_FOR_PUENTE_HILLS = "Run all Puente Hills Scenarios";

    // objects for control panels
  private RegionsOfInterestControlPanel regionsOfInterest;
  //private PuenteHillsScenarioTestControlPanel puenteHillsTestControl;
  //private PuenteHillsScenarioControlPanel puenteHillsControl;
  private GenerateHazusControlPanelForSingleMultipleIMRs hazusControl;

  // instances of the GUI Beans which will be shown in this applet
  private EqkRupSelectorGuiBean erfGuiBean;
  private AttenuationRelationshipGuiBean imrGuiBean;
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
  public ScenarioShakeMapAttenRelApp_Temp() {
  }
  //Initialize the applet
  public void init() {
    try {
      // initialize the control pick list
      initControlList();
      jbInit();
    }
    catch(Exception e) {
      step =0;
      e.printStackTrace();
    }
    try{
      //initialises the IMR and IMT Gui Bean
      initIMRGuiBean();
    }catch(RuntimeException e){
      e.printStackTrace();
      step =0;
      JOptionPane.showMessageDialog(this,"Invalid parameter value",e.getMessage(),JOptionPane.ERROR_MESSAGE);
      return;
    }
    this.initGriddedRegionGuiBean();
    try{
        this.initERFSelector_GuiBean();
        initTimeSpanGuiBean();
      }catch(RuntimeException e){
        //e.printStackTrace();
        step =0;
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
    this.setSize(new Dimension(564, 752));
    this.getContentPane().setLayout(borderLayout1);
    mainPanel.setBorder(border1);
    mainPanel.setLayout(gridBagLayout6);
    mainSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
    mainSplitPane.setLastDividerLocation(610);
    buttonPanel.setLayout(gridBagLayout4);
    eqkRupPanel.setLayout(gridBagLayout1);
    gmtPanel.setLayout(gridBagLayout9);

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
    imrPanel.setLayout(gridBagLayout5);
    controlComboBox.setBackground(SystemColor.control);
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



    parameterTabbedPanel.addTab("Intensity-Measure Relationship", imrPanel);
    parameterTabbedPanel.addTab("Region & Site Params", gridRegionSitePanel);
    parameterTabbedPanel.addTab("Earthquake Rupture from Forecast", eqkRupPanel );
    parameterTabbedPanel.addTab("Time Span", timespanPanel);
    parameterTabbedPanel.addTab( "Exceedance Level/Probability", prob_IMLPanel);
    parameterTabbedPanel.addTab("Map Attributes", gmtPanel);
    mainSplitPane.setDividerLocation(630);
  }


  //Main method
  public static void main(String[] args) {
    ScenarioShakeMapAttenRelApp_Temp applet = new ScenarioShakeMapAttenRelApp_Temp();
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

    // create the Site Gui Bean object
    sitesGuiBean = new SitesInGriddedRegionGuiBean();

    //sets the site parameters in the gridded region gui bean.
    setGriddedRegionSiteParams();
    // show the sitebean in JPanel
    gridRegionSitePanel.add(this.sitesGuiBean, new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0,
        GridBagConstraints.CENTER, GridBagConstraints.BOTH, defaultInsets, 0, 0 ));
  }



  /**
   * Initialize the IMR Gui Bean
   */
  private void initIMRGuiBean() {
    imrGuiBean = new AttenuationRelationshipGuiBean(this);
    imrGuiBean.getIntensityMeasureParamEditor().getParameterEditor(imrGuiBean.IMT_PARAM_NAME).getParameter().addParameterChangeListener(this);
    // show this IMRgui bean the Panel
    imrPanel.add(imrGuiBean,new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0,
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
   erf_Classes.add(FRANKEL02_ADJ_FORECAST_CLASS_NAME);
//   erf_Classes.add(PEER_AREA_FORECAST_CLASS_NAME);
   erf_Classes.add(PEER_NON_PLANAR_FAULT_FORECAST_CLASS_NAME);
   erf_Classes.add(SIMPLE_POISSON_FAULT_FORECAST_CLASS_NAME);
   erf_Classes.add(SIMPLE_FAULT_FORECAST_CLASS_NAME);
//   erf_Classes.add(PEER_MULTI_SOURCE_FORECAST_CLASS_NAME);
//   erf_Classes.add(PEER_LOGIC_TREE_FORECAST_CLASS_NAME);
   erf_Classes.add(STEP_FORECAST_CLASS_NAME);
   erf_Classes.add(WG02_FORECAST_CLASS_NAME);
   erf_Classes.add(PUENTE_HILLS_FORECAST_CLASS_NAME);
   try{
     erfGuiBean = new EqkRupSelectorGuiBean(erf_Classes);
   }catch(InvocationTargetException e){
     throw new RuntimeException("Connection to ERF servlets failed");
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

    //if the ERF Param Name changes
    if(name1.equalsIgnoreCase(this.erfGuiBean.ERF_PARAM_NAME))
      /* get the selected ERF
       NOTE : We have used erfGuiBean.getSelectedERF_Instance()INSTEAD OF
       erfGuiBean.getSelectedERF.
       Dofference is that erfGuiBean.getSelectedERF_Instance() does not update
       the forecast while erfGuiBean.getSelectedERF updates the ERF
       */
      this.timeSpanGuiBean.setTimeSpan(erfGuiBean.getSelectedERF_Instance().getTimeSpan());

    if(name1.equalsIgnoreCase(imrGuiBean.IMT_PARAM_NAME))
      imlProbGuiBean.setIMLConstraintBasedOnSelectedIMT(imrGuiBean.getSelectedIMT());


  }

  /**
   *
   */
  public void run(){

    try{
      addButton();
    }catch(ParameterException ee){
      ee.printStackTrace();
      step =0;
      JOptionPane.showMessageDialog(this,ee.getMessage(),"Invalid Parameters",JOptionPane.ERROR_MESSAGE);
      return;
    }
    catch(Exception ee){
      ee.printStackTrace();
      step =0;
      JOptionPane.showMessageDialog(this,ee.getMessage(),"Input Error",JOptionPane.INFORMATION_MESSAGE);
      return;
    }
    //make sures that next time user wants to generate the shapefiles for hazus
    //he would have to pull up the control panel again and punch the button.
    if(hazusControl !=null)
      hazusControl.setGenerateShapeFilesForHazus(false);
  }

  /**
   *
   * @returns the site parameters iterator for the selected AttenuationRelationships
   * It also avoids the duplicity of the site params if AttenuationRelationships
   * share them.
   */
  private Iterator getSelectedAttenRelSiteParams(){
    // get the selected IMR
    attenRel = imrGuiBean.getSelectedIMRs();

    //ArrayList to store the siteParams for all selected AttenRel
    ArrayList siteParams = new ArrayList();
    //getting all the selected AttenRels and iterating over their site params
    //adding them as clones to the vector but avoiding the duplicity.
    //There can be a scenario when the AttenRels have same site type, so we
    //don't want to duplicate the site params but do want to set their values in both
    //the selected attenRels.
    for(int i=0;i<attenRel.size();++i){
      AttenuationRelationshipAPI attenRelApp = (AttenuationRelationshipAPI)attenRel.get(i);
      ListIterator it = attenRelApp.getSiteParamsIterator();
      while(it.hasNext()){
        ParameterAPI tempParam = (ParameterAPI)it.next();
        boolean flag = true;
        //iterating over all the added siteParams to check if we have added that
        //site param before.
        for(int j=0;j<siteParams.size();++j)
          if(tempParam.getName().equals(((ParameterAPI)siteParams.get(j)).getName()))
            flag= false;
        if(flag)
          siteParams.add(tempParam.clone());
      }
    }
    return siteParams.iterator();
  }


  /**
   * sets the Site Params from the AttenuationRelationships in the GriddedRegion
   * Gui Bean.
   */
  public void setGriddedRegionSiteParams(){
    if(sitesGuiBean !=null){
      sitesGuiBean.replaceSiteParams(getSelectedAttenRelSiteParams());
      sitesGuiBean.refreshParamEditor();
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
      probAtIML = false;
  }

  /**
   * This method calculates the probablity or the IML for the selected Gridded Region
   * and stores the value in each vectors(lat-ArrayList, Lon-ArrayList and IML or Prob ArrayList)
   * The IML or prob vector contains value based on what the user has selected in the Map type
   * @param attenRel : Selected AttenuationRelationships
   */
  public XYZ_DataSetAPI generateShakeMap(ArrayList attenRel) throws ParameterException,RuntimeException{
    try {
      //if the IMT selected is Log supported then take the log if Prob @ IML
      if(IMT_Info.isIMT_LogNormalDist(imrGuiBean.getSelectedIMT()) && probAtIML)
        imlProbValue = Math.log(imlProbValue);
      //does the calculation for the ScenarioShakeMap Calc and gives back a XYZ dataset
      xyzDataSet = shakeMapCalc.getScenarioShakeMapData(attenRel,attenRelWts,griddedRegionSites,erfGuiBean.getRupture(),probAtIML,imlProbValue);

      //if the IMT is log supported then take the exponential of the Value if IML @ Prob
      if(IMT_Info.isIMT_LogNormalDist(imrGuiBean.getSelectedIMT()) && !probAtIML){
        ArrayList zVals = xyzDataSet.getZ_DataSet();
        int size = zVals.size();
        for(int i=0;i<size;++i){
          double tempVal = Math.exp(((Double)(zVals.get(i))).doubleValue());
          zVals.set(i,new Double(tempVal));
        }
      }
    }catch(ParameterException e){
      e.printStackTrace();
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
   * Returns the selected IM in the IMR GuiBean
   * @return
   */
  public ParameterAPI getSelectedIntensityMeasure(){
   return imrGuiBean.getSelectedIntensityMeasure();
  }



  /**
   * This function sets the Gridded region Sites and the type of plot user wants to see
   * IML@Prob or Prob@IML and it value.
   * This function also gets the selected AttenuationRelationships in a ArrayList and their
   * corresponding relative wts.
   */
  public void getGriddedSitesMapTypeAndSelectedAttenRels(){
    //gets the IML or Prob selected value
    getIMLorProb();
    //get the site values for each site in the gridded region
    getGriddedRegionSites();

    //selected IMRs Wts
    attenRelWts = imrGuiBean.getSelectedIMR_Weights();
    //selected IMR's
    attenRel= imrGuiBean.getSelectedIMRs();
  }

  void addButton_actionPerformed(ActionEvent e) {

    //gets the metadata as soon as the user presses the button to make map.
    mapParametersInfo = getMapParametersInfo();

    addButton.setEnabled(false);
    calcProgress = new CalcProgressBar("ScenarioShakeMapApp","Initialising for shakemap calculation");
   //sets the Gridded region Sites and the type of plot user wants to see
   //IML@Prob or Prob@IML and it value.
    if(hazusControl == null || !hazusControl.isGenerateShapeFilesForHazus()){
      getGriddedSitesMapTypeAndSelectedAttenRels();
      // this function will get the selected IMT parameter and set it in IMT
      imrGuiBean.setIMT();
    }

    timer = new Timer(200, new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        if(step == 1)
          calcProgress.setProgressMessage("Doing calculation for shakemap ...");
        else if(step == 2)
          calcProgress.setProgressMessage("Generating the shakemap ...");
        else if(step ==0){
          addButton.setEnabled(true);
          timer.stop();
          calcProgress.dispose();
        }
      }
    });
    Thread t = new Thread(this);
    t.start();
  }

  /**
   * when the generate Map button is pressed
   */
  public void addButton(){
    timer.start();
    step = 1;
    if(hazusControl == null || !hazusControl.isGenerateShapeFilesForHazus())
      generateShakeMap(attenRel);
    //sets the region coordinates for the GMT using the MapGuiBean
    setRegionForGMT();
    ++step;

    if(step==2) {
      String label;
      String imlOrProb=imlProbGuiBean.getSelectedOption();
      if(imlOrProb.equalsIgnoreCase(imlProbGuiBean.PROB_AT_IML))
        label="Prob";
      else
        label=imrGuiBean.getSelectedIMT();

      if(hazusControl !=null && hazusControl.isGenerateShapeFilesForHazus())
        mapGuiBean.makeHazusShapeFilesAndMap(hazusControl.getXYZ_DataForSA_03(),hazusControl.getXYZ_DataForSA_10(),
            hazusControl.getXYZ_DataForPGA(),hazusControl.getXYZ_DataForPGV(),
            erfGuiBean.getRupture(),label,getMapParametersInfo());
      else
        mapGuiBean.makeMap(xyzDataSet,erfGuiBean.getRupture(),label,mapParametersInfo);
    }
    step =0;
}



  /**
   * Initialize the items to be added to the control list
   */
  private void initControlList() {
    this.controlComboBox.addItem(CONTROL_PANELS);
    this.controlComboBox.addItem(REGIONS_OF_INTEREST_CONTROL);
    this.controlComboBox.addItem(HAZUS_CONTROL);
    /*this.controlComboBox.addItem(PUENTE_HILLS_TEST_CONTROL);
    this.controlComboBox.addItem(PUENTE_HILLS_CONTROL);*/

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
    else if(selectedControl.equalsIgnoreCase(this.HAZUS_CONTROL))
      initHazusScenarioControl();
    /*else if(selectedControl.equalsIgnoreCase(this.PUENTE_HILLS_TEST_CONTROL))
      initPuenteHillTestScenarioControl();
    else if(selectedControl.equalsIgnoreCase(this.PUENTE_HILLS_CONTROL))
      initPuenteHillScenarioControl();
    */
    controlComboBox.setSelectedItem(this.CONTROL_PANELS);
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
 /* private void initPuenteHillTestScenarioControl() {
    int selectedOption = JOptionPane.showConfirmDialog(this,"Are you sure to set the Parameters to Puente Hills Test?",
                                    "Puente Hills Control",JOptionPane.YES_NO_CANCEL_OPTION);
    if(selectedOption == JOptionPane.OK_OPTION){
      if(this.puenteHillsTestControl==null)
        puenteHillsTestControl = new PuenteHillsScenarioTestControlPanel(this.erfGuiBean,(IMR_GuiBeanAPI) imrGuiBean,
                                                                this.sitesGuiBean,this.mapGuiBean,(IMT_GuiBeanAPI)imtGuiBean);
      puenteHillsTestControl.setParamsForPuenteHillsScenario();
    }
  }*/

  /**
   *Initialise the Control panel to generate the shapefiles for hazus input.
   */
  private void initHazusScenarioControl(){
    if(hazusControl == null)
      hazusControl = new GenerateHazusControlPanelForSingleMultipleIMRs(this,this);

    hazusControl.show();
    hazusControl.pack();
 }



  /**
   * Initialize the Interesting regions control panel
   * It will provide a pick list of interesting regions
   */
  /*private void initPuenteHillScenarioControl() {
    int selectedOption = JOptionPane.showConfirmDialog(this,"Are you sure to set the Parameters to Puente Hills Scenario?",
                                    "Puente Hills Control",JOptionPane.YES_NO_CANCEL_OPTION);
    if(selectedOption == JOptionPane.OK_OPTION){
      if(this.puenteHillsTestControl==null)
        puenteHillsControl = new PuenteHillsScenarioControlPanel(this.erfGuiBean,(IMR_GuiBeanAPI)imrGuiBean,
                                                                this.sitesGuiBean,this.mapGuiBean,(IMT_GuiBeanAPI)imtGuiBean);
      puenteHillsControl.setParamsForPuenteHillsScenario();
    }
  }*/

  /**
   *
   * @returns the selected Attenuationrelationship model
   */
  public ArrayList getSelectedAttenuationRelationships(){
    attenRel = imrGuiBean.getSelectedIMRs();
    return attenRel;
  }

  /**
   *
   * @returns the String containing the values selected for different parameters
   */
  public String getMapParametersInfo(){

    String imrMetadata = "IMR Param List:<br>\n " +
           "---------------<br>\n"+
        this.imrGuiBean.getParameterListMetadataString()+"\n";
    //if the Hazus Control for Sceario is selected the get the metadata for IMT from there
    if(hazusControl !=null && hazusControl.isGenerateShapeFilesForHazus())
      imrMetadata = imrMetadata+hazusControl.getIMT_Metadata();

    return imrMetadata+
        "<br><br>Region Param List: <br>\n"+
        "----------------<br>\n"+
        sitesGuiBean.getVisibleParameters().getParameterListMetadataString()+"\n"+
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
