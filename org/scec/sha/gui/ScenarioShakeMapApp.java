package org.scec.sha.gui;

import java.awt.*;
import java.awt.event.*;
import java.applet.*;
import javax.swing.*;
import javax.swing.border.*;
import java.util.*;
import java.io.*;

import org.scec.sha.gui.beans.*;
import org.scec.sha.imr.*;
import org.scec.sha.earthquake.rupForecastImpl.*;
import org.scec.sha.earthquake.EqkRupForecastAPI;
import org.scec.sha.earthquake.EqkRupForecast;
import org.scec.param.event.*;
import org.scec.data.region.SitesInGriddedRegion;
import org.scec.data.Site;
import org.scec.sha.earthquake.ProbEqkRupture;

/**
 * <p>Title: ScenarioShakeMapApp</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author: Ned Field & Nitin Gupta & Vipin Gupta
 * @created : March 21,2003
 * @version 1.0
 */

public class ScenarioShakeMapApp extends JApplet implements
                                         ParameterChangeListener{


  /**
   * Name of the class
   */
  protected final static String C = "ScenarioShakeMapApp";
  // for debug purpose
  protected final static boolean D = false;



  //variables that determine the width and height of the frame
  private static final int W=480;
  private static final int H=760;
  // default insets
  private Insets defaultInsets = new Insets( 4, 4, 4, 4 );

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
  public final static String STEP_FORECAST_CLASS_NAME = "org.scec.sha.earthquake.rupForecastImpl.step.STEP_EqkRupForecast";


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
  private GridBagLayout gridBagLayout5 = new GridBagLayout();
  private JPanel eqkRupPanel = new JPanel();
  private GridBagLayout gridBagLayout3 = new GridBagLayout();
  private GridBagLayout gridBagLayout2 = new GridBagLayout();
  private JPanel siteRegionPanel = new JPanel();
  private JPanel gmtPanel = new JPanel();
  private JSplitPane imr_IMTSplit = new JSplitPane();
  private JSplitPane imr_RegionSplit = new JSplitPane();
  private JTabbedPane parameterTabbedPanel = new JTabbedPane();
  private JPanel timespanPanel = new JPanel();
  private JPanel imrSelectionPanel = new JPanel();
  private JPanel imrPanel = new JPanel();
  private JPanel imtPanel = new JPanel();
  private JPanel prob_IMLPanel = new JPanel();
  private BorderLayout borderLayout2 = new BorderLayout();
  private GridBagLayout gridBagLayout9 = new GridBagLayout();
  private GridBagLayout gridBagLayout8 = new GridBagLayout();
  private GridBagLayout gridBagLayout7 = new GridBagLayout();
  private JButton addButton = new JButton();
  private JComboBox controlPanelCombo = new JComboBox();
  private GridBagLayout gridBagLayout1 = new GridBagLayout();
  private GridBagLayout gridBagLayout4 = new GridBagLayout();
  private GridBagLayout gridBagLayout6 = new GridBagLayout();
  private BorderLayout borderLayout1 = new BorderLayout();
  //Get a parameter value
  public String getParameter(String key, String def) {
    return isStandalone ? System.getProperty(key, def) :
      (getParameter(key) != null ? getParameter(key) : def);
  }

  //Construct the applet
  public ScenarioShakeMapApp() {
  }
  //Initialize the applet
  public void init() {
    try {
      jbInit();
    }
    catch(Exception e) {
      e.printStackTrace();
    }
    this.initIMRPanel();
    this.initERFSelector_GuiBean();
    this.initImlProb_GuiBean();
    this.initTimeSpanGuiBean();
    this.initMapGuiBean();
  }
  //Component initialization
  private void jbInit() throws Exception {
    border1 = new EtchedBorder(EtchedBorder.RAISED,new Color(248, 254, 255),new Color(121, 124, 136));
    this.setSize(new Dimension(482, 721));
    this.getContentPane().setLayout(borderLayout1);
    mainPanel.setBorder(border1);
    mainPanel.setLayout(gridBagLayout4);
    mainSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
    buttonPanel.setLayout(gridBagLayout6);
    eqkRupPanel.setLayout(gridBagLayout1);
    siteRegionPanel.setLayout(gridBagLayout7);
    gmtPanel.setLayout(gridBagLayout9);
    imr_IMTSplit.setOrientation(JSplitPane.VERTICAL_SPLIT);
    imr_RegionSplit.setOrientation(JSplitPane.VERTICAL_SPLIT);
    timespanPanel.setLayout(gridBagLayout3);
    imrSelectionPanel.setLayout(gridBagLayout5);
    imrPanel.setLayout(borderLayout2);
    imtPanel.setLayout(gridBagLayout8);
    prob_IMLPanel.setLayout(gridBagLayout2);
    addButton.setText("Add Map");
    addButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        addButton_actionPerformed(e);
      }
    });
    buttonPanel.setMinimumSize(new Dimension(391, 50));
    this.getContentPane().add(mainPanel, BorderLayout.CENTER);
    mainPanel.add(mainSplitPane,  new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(1, 1, 2, 3), 430, 528));
    mainSplitPane.add(buttonPanel, JSplitPane.BOTTOM);
    buttonPanel.add(controlPanelCombo,             new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 16, 3));
    buttonPanel.add(addButton,      new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 7, 0, 0), 78, 18));
    mainSplitPane.add(parameterTabbedPanel, JSplitPane.TOP);
    imr_RegionSplit.add(imrSelectionPanel, JSplitPane.TOP);
    imr_RegionSplit.add(siteRegionPanel, JSplitPane.BOTTOM);
    imr_IMTSplit.add(imtPanel, JSplitPane.BOTTOM);
    parameterTabbedPanel.add(eqkRupPanel, "Forecast Model");
    parameterTabbedPanel.add(timespanPanel, "Time Span");
    parameterTabbedPanel.add(prob_IMLPanel, "MapType");
    parameterTabbedPanel.add(gmtPanel, "GMT Parameters");
    imr_IMTSplit.add(imr_RegionSplit, JSplitPane.TOP);
    parameterTabbedPanel.add(imrPanel, "IMR Model");
    imrPanel.add(imr_IMTSplit, BorderLayout.CENTER);
    mainSplitPane.setDividerLocation(580);
    imr_IMTSplit.setDividerLocation(400);
    imr_RegionSplit.setDividerLocation(100);
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
    ScenarioShakeMapApp applet = new ScenarioShakeMapApp();
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
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      //UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
    }
    catch(Exception e) {
    }
  }

  /**
   * Initialize the IMR Gui Bean
   */
  private void initIMRPanel() {
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
     //imrGuiBean.getParameterEditor(imrGuiBean.IMR_PARAM_NAME).getParameter().addParameterChangeListener(this);

     // show this IMRgui bean the Panel
    imrSelectionPanel.add(this.imrGuiBean,new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0,
         GridBagConstraints.CENTER, GridBagConstraints.BOTH, defaultInsets, 0, 0 ));

     // get the selected IMR
     AttenuationRelationshipAPI imr = imrGuiBean.getSelectedIMR_Instance();
     // create the Site Gui Bean object
     sitesGuiBean = new SitesInGriddedRegionGuiBean();
     sitesGuiBean.replaceSiteParams(imr.getSiteParamsIterator());
     // show the sitebean in JPanel
     siteRegionPanel.add(this.sitesGuiBean, new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0,
         GridBagConstraints.CENTER, GridBagConstraints.BOTH, defaultInsets, 0, 0 ));


     /**
      * Initialize the IMT Gui Bean
      */

     // create the IMT Gui Bean object
     imtGuiBean = new IMT_GuiBean(imr);

     imtPanel.add(imtGuiBean, new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0,
               GridBagConstraints.CENTER, GridBagConstraints.BOTH, defaultInsets, 0, 0 ));
  }

  /**
   * Initialize the ERF Gui Bean
   */
  private void initERFSelector_GuiBean() {
     // create the ERF Gui Bean object
   Vector erf_Classes = new Vector();
   erf_Classes.add(PEER_FAULT_FORECAST_CLASS_NAME);
   erf_Classes.add(PEER_AREA_FORECAST_CLASS_NAME);
   erf_Classes.add(PEER_NON_PLANAR_FAULT_FORECAST_CLASS_NAME);
   erf_Classes.add(PEER_LISTRIC_FAULT_FORECAST_CLASS_NAME);
   erf_Classes.add(PEER_MULTI_SOURCE_FORECAST_CLASS_NAME);
   erf_Classes.add(PEER_LOGIC_TREE_FORECAST_CLASS_NAME);
   erf_Classes.add(FRANKEL_FORECAST_CLASS_NAME);
   erf_Classes.add(this.STEP_FORECAST_CLASS_NAME);
   erfGuiBean = new EqkRupSelectorGuiBean(erf_Classes);

   eqkRupPanel.add(erfGuiBean, new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0,
                GridBagConstraints.CENTER,GridBagConstraints.BOTH, defaultInsets, 0, 0 ));
   //erfGuiBean.getParameterEditor(erfGuiBean.ERF_PARAM_NAME).getParameter().addParameterChangeListener(this);

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
    mapGuiBean.showGMTParams(false);
    gmtPanel.add(mapGuiBean, new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0,
        GridBagConstraints.CENTER, GridBagConstraints.BOTH, defaultInsets, 0, 0 ));
    double minLat=((Double)sitesGuiBean.getParameterList().getParameter(sitesGuiBean.MIN_LATITUDE).getValue()).doubleValue();
    double maxLat=((Double)sitesGuiBean.getParameterList().getParameter(sitesGuiBean.MAX_LATITUDE).getValue()).doubleValue();
    double minLon=((Double)sitesGuiBean.getParameterList().getParameter(sitesGuiBean.MIN_LONGITUDE).getValue()).doubleValue();
    double maxLon=((Double)sitesGuiBean.getParameterList().getParameter(sitesGuiBean.MAX_LONGITUDE).getValue()).doubleValue();
    double gridSpacing=((Double)sitesGuiBean.getParameterList().getParameter(sitesGuiBean.GRID_SPACING).getValue()).doubleValue();
    mapGuiBean.setGMTRegionParams(minLat,maxLat,minLon,maxLon,gridSpacing);
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
      AttenuationRelationshipAPI imr = imrGuiBean.getSelectedIMR_Instance();
      imtGuiBean.setIMR(imr);
      imtGuiBean.validate();
      imtGuiBean.repaint();
      sitesGuiBean.replaceSiteParams(imr.getSiteParamsIterator());
      sitesGuiBean.validate();
      sitesGuiBean.repaint();
    }
    if(name1.equalsIgnoreCase(this.erfGuiBean.ERF_PARAM_NAME))
       /* get the selected ERF
       NOTE : We have used erfGuiBean.getSelectedERF_Instance()INSTEAD OF
       erfGuiBean.getSelectedERF.
       Dofference is that erfGuiBean.getSelectedERF_Instance() does not update
       the forecast while erfGuiBean.getSelectedERF updates the
       */
      this.timeSpanGuiBean.setTimeSpan(erfGuiBean.getSelectedERF_Instance().getTimeSpan());

   if(name1.equalsIgnoreCase(sitesGuiBean.MIN_LATITUDE)||
      name1.equalsIgnoreCase(sitesGuiBean.MAX_LATITUDE)||
      name1.equalsIgnoreCase(sitesGuiBean.MIN_LONGITUDE)||
      name1.equalsIgnoreCase(sitesGuiBean.MAX_LONGITUDE)||
      name1.equalsIgnoreCase(sitesGuiBean.GRID_SPACING)){

     //updates the GMT region parameters if the sites region parameters are updated
     double minLat=((Double)sitesGuiBean.getParameterList().getParameter(sitesGuiBean.MIN_LATITUDE).getValue()).doubleValue();
     double maxLat=((Double)sitesGuiBean.getParameterList().getParameter(sitesGuiBean.MAX_LATITUDE).getValue()).doubleValue();
     double minLon=((Double)sitesGuiBean.getParameterList().getParameter(sitesGuiBean.MIN_LONGITUDE).getValue()).doubleValue();
     double maxLon=((Double)sitesGuiBean.getParameterList().getParameter(sitesGuiBean.MAX_LONGITUDE).getValue()).doubleValue();
     double gridSpacing=((Double)sitesGuiBean.getParameterList().getParameter(sitesGuiBean.GRID_SPACING).getValue()).doubleValue();
     mapGuiBean.setGMTRegionParams(minLat,maxLat,minLon,maxLon,gridSpacing);
   }
  }

  private void generateShakeMap(){
    boolean imlAtProb=false;
    boolean probAtIML=false;
    double imlProbValue=imlProbGuiBean.getIML_Prob();
    Site site;
    SitesInGriddedRegion griddedRegionSites = sitesGuiBean.getGriddedRegionSite();
    int numSites = griddedRegionSites.getNumGridLocs();
    String imlOrProb=imlProbGuiBean.getSelectedOption();
    if(imlOrProb.equalsIgnoreCase(imlProbGuiBean.IML_AT_PROB))
      imlAtProb=true;
    else
      probAtIML=true;

    // get the selected forecast model
    EqkRupForecast eqkRupForecast = (EqkRupForecast)erfGuiBean.getSelectedERF();

    // get the selected IMR
    AttenuationRelationship imr = (AttenuationRelationship)imrGuiBean.getSelectedIMR_Instance();
    try {
     // this function will get the selected IMT parameter and set it in IMT
     imtGuiBean.setIMR_Param();
   } catch (Exception ex) {
     if(D) System.out.println(C + ":Param warning caught"+ex);
     ex.printStackTrace();
    }

    Vector siteLat= new Vector();
    Vector siteLon= new Vector();
    Vector siteValue = new Vector();
    for(int i=0;i<numSites;++i){
      site = griddedRegionSites.getSite(i);
      siteLat.add(new Double(site.getLocation().getLatitude()));
      siteLon.add(new Double(site.getLocation().getLongitude()));
      imr.setSite(site);
      // set the PQkRup in the IMR
      try {
        int source = Integer.parseInt((String)erfGuiBean.getParameterList().getParameter(erfGuiBean.SOURCE_PARAM_NAME).getValue());
        int rupture = Integer.parseInt((String)erfGuiBean.getParameterList().getParameter(erfGuiBean.RUPTURE_PARAM_NAME).getValue());
        imr.setProbEqkRupture((ProbEqkRupture)eqkRupForecast.getRupture(source,rupture));
      } catch (Exception ex) {
        System.out.println("Parameter change warning caught");
      }
      if(probAtIML)
        siteValue.add(new Double(imr.getExceedProbability(imlProbValue)));
      else{
        imr.getParameter(imr.EXCEED_PROB_NAME).setValue(new Double(imlProbValue));
        siteValue.add(new Double(imr.getIML_AtExceedProb()));
      }
    }
    makeFile(siteLat,siteLon,siteValue);
    mapGuiBean.makeMap("temp.txt");
  }

  private void makeFile(Vector lat,Vector lon,Vector siteValue){
    try{
         int size=lat.size();
         FileWriter fr = new FileWriter("temp.txt");
         for(int i=0;i<size;++i)
           fr.write(lat.get(i)+" "+lon.get(i)+" "+siteValue.get(i)+"\n");
         fr.close();
     }catch(IOException e){
       e.printStackTrace();
      }
  }

  void addButton_actionPerformed(ActionEvent e) {
    this.generateShakeMap();
  }
}