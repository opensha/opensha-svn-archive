package org.scec.sha.gui;

import java.awt.*;
import java.awt.event.*;
import java.applet.*;
import javax.swing.*;
import javax.swing.border.*;
import java.util.*;
import java.io.*;
import java.net.*;
import java.lang.reflect.InvocationTargetException;

import ch.randelshofer.quaqua.QuaquaManager;

import org.scec.sha.gui.beans.*;
import org.scec.sha.imr.*;
import org.scec.sha.earthquake.EqkRupForecastAPI;
import org.scec.sha.earthquake.EqkRupForecast;
import org.scec.param.event.*;
import org.scec.data.region.SitesInGriddedRegion;
import org.scec.data.Site;
import org.scec.sha.gui.controls.*;
import org.scec.sha.gui.infoTools.*;
import org.scec.sha.earthquake.ERF_API;
import org.scec.exceptions.ParameterException;
import org.scec.sha.gui.controls.X_ValuesInCurveControlPanelAPI;
import org.scec.data.function.ArbitrarilyDiscretizedFunc;
import org.scec.sha.calc.HazardCurveCalculator;



/**
 * <p>Title: HazardMapApplet</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author: Ned Field & Nitin Gupta & Vipin Gupta
 * @created : March 15,2004
 * @version 1.0
 */

public class HazardMapApplet extends JApplet
    implements ParameterChangeListener, X_ValuesInCurveControlPanelAPI {


  /**
   * Name of the class
   */
  protected final static String C = "HazardMapApplet";
  // for debug purpose
  protected final static boolean D = false;
  public static String SERVLET_URL  = "http://gravity.usc.edu/OpenSHA/servlet/HazardMapCalcServlet";

  //variables that determine the width and height of the frame
  private static final int W=550;
  private static final int H=760;

  // default insets
  private Insets defaultInsets = new Insets( 4, 4, 4, 4 );

  //store the site values for each site in the griddded region
  private SitesInGriddedRegion griddedRegionSites;

  //gets the instance of the selected AttenuationRelationship
  private AttenuationRelationship attenRel;
  private boolean useCustomX_Values = false;

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
  public final static String SM_CLASS_NAME = "org.scec.sha.imr.attenRelImpl.ShakeMap_2003_AttenRel";

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
  public final static String RMI_FRANKEL02_ADJ_FORECAST_CLASS_NAME="org.scec.sha.earthquake.rupForecastImpl.Frankel02.ERFFrankel02Client";

  // Strings for control pick list
  private final static String CONTROL_PANELS = "Control Panels";
  private final static String REGIONS_OF_INTEREST_CONTROL = "Regions of Interest";
  private final static String X_VALUES_CONTROL = "Set X values for Hazard Curve Calc.";
  private final static String DISTANCE_CONTROL = "Max Source-Site Distance";


  // objects for control panels
  private RegionsOfInterestControlPanel regionsOfInterest;
  private X_ValuesInCurveControlPanel xValuesPanel;
  private SetMinSourceSiteDistanceControlPanel distanceControlPanel;


  // instances of the GUI Beans which will be shown in this applet
  private ERF_GuiBean erfGuiBean;
  private IMR_GuiBean imrGuiBean;
  private IMT_GuiBean imtGuiBean;
  private SitesInGriddedRegionGuiBean sitesGuiBean;
  private TimeSpanGuiBean timeSpanGuiBean;

  private boolean isStandalone = false;
  private JPanel mainPanel = new JPanel();
  private Border border1;
  private JSplitPane mainSplitPane = new JSplitPane();
  private JPanel buttonPanel = new JPanel();
  private JPanel eqkRupPanel = new JPanel();
  private GridBagLayout gridBagLayout3 = new GridBagLayout();
  private GridBagLayout gridBagLayout2 = new GridBagLayout();
  private JSplitPane imr_IMTSplit = new JSplitPane();
  private JTabbedPane parameterTabbedPanel = new JTabbedPane();
  private JPanel timespanPanel = new JPanel();
  private JPanel imrPanel = new JPanel();
  private JPanel imtPanel = new JPanel();
  private BorderLayout borderLayout2 = new BorderLayout();
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
  private int step;
  JLabel emailLabel = new JLabel();
  JTextField emailText = new JTextField();
  GridBagLayout gridBagLayout4 = new GridBagLayout();
  //holds the ArbitrarilyDiscretizedFunc
  private ArbitrarilyDiscretizedFunc function;
  //instance to get the default IMT X values for the hazard Curve
  private IMT_Info imtInfo = new IMT_Info();



  //Get a parameter value
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
    imr_IMTSplit.setOrientation(JSplitPane.VERTICAL_SPLIT);
    timespanPanel.setLayout(gridBagLayout3);
    imrPanel.setLayout(borderLayout2);
    imtPanel.setLayout(gridBagLayout8);
    addButton.setText("Start Calc");
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
    emailLabel.setText("Email:");
    emailText.setText("");
    this.getContentPane().add(mainPanel, BorderLayout.CENTER);
    mainPanel.add(mainSplitPane,  new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(1, 1, 2, 3), 0, 431));
    mainSplitPane.add(buttonPanel, JSplitPane.BOTTOM);
    buttonPanel.add(addButton,  new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(28, 115, 24, 109), 26, 9));
    buttonPanel.add(controlComboBox,  new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(19, 0, 0, 0), 153, 2));
    buttonPanel.add(emailLabel,  new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(26, 5, 33, 0), 11, 12));
    mainSplitPane.add(parameterTabbedPanel, JSplitPane.TOP);
    imr_IMTSplit.add(imtPanel, JSplitPane.BOTTOM);
    imr_IMTSplit.add(imrSelectionPanel, JSplitPane.TOP);
    imrPanel.add(imr_IMTSplit, BorderLayout.CENTER);
    buttonPanel.add(emailText,  new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(25, 0, 36, 0), 176, 4));
    parameterTabbedPanel.addTab("Intensity-Measure Relationship", imrPanel);
    parameterTabbedPanel.addTab("Region & Site Params", gridRegionSitePanel);
    parameterTabbedPanel.addTab( "Earthquake Rupture Forecast", eqkRupPanel );
    parameterTabbedPanel.addTab("Time Span", timespanPanel);
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
    HazardMapApplet applet = new HazardMapApplet();
    applet.isStandalone = true;
    JFrame frame = new JFrame();
    //EXIT_ON_CLOSE == 3
    frame.setDefaultCloseOperation(3);
    frame.setTitle("HazardMap App");
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

    imtPanel.add(imtGuiBean, new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0,
        GridBagConstraints.CENTER, GridBagConstraints.BOTH, defaultInsets, 0, 0 ));
  }

  /**
   * Initialize the IMR Gui Bean
   */
  private void initIMRGuiBean() {
    // create the IMR Gui Bean object
     // It accepts the vector of IMR class names
     ArrayList imrClasses = new ArrayList();
//     imrClasses.add(this.SM_CLASS_NAME);
     imrClasses.add(this.AS_CLASS_NAME);
     imrClasses.add(this.BJF_CLASS_NAME);
     imrClasses.add(this.C_CLASS_NAME);
     imrClasses.add(this.SCEMY_CLASS_NAME);
     imrClasses.add(this.CB_CLASS_NAME);
     imrClasses.add(this.F_CLASS_NAME);
     imrClasses.add(this.A_CLASS_NAME);
     imrClasses.add(this.SM_CLASS_NAME);
     imrGuiBean = new IMR_GuiBean(imrClasses);
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

   /*erf_Classes.add(FRANKEL_FORECAST_CLASS_NAME);
   erf_Classes.add(FRANKEL_ADJ_FORECAST_CLASS_NAME);
   erf_Classes.add(FRANKEL02_ADJ_FORECAST_CLASS_NAME);
   erf_Classes.add(PEER_AREA_FORECAST_CLASS_NAME);
   erf_Classes.add(PEER_NON_PLANAR_FAULT_FORECAST_CLASS_NAME);
   erf_Classes.add(SIMPLE_POISSON_FAULT_FORECAST_CLASS_NAME);
   erf_Classes.add(SIMPLE_FAULT_FORECAST_CLASS_NAME);
   erf_Classes.add(PEER_MULTI_SOURCE_FORECAST_CLASS_NAME);
   erf_Classes.add(PEER_LOGIC_TREE_FORECAST_CLASS_NAME);
   erf_Classes.add(STEP_FORECAST_CLASS_NAME);
   erf_Classes.add(WG02_FORECAST_CLASS_NAME);
   erf_Classes.add(PUENTE_HILLS_FORECAST_CLASS_NAME);*/
   erf_Classes.add(this.RMI_FRANKEL02_ADJ_FORECAST_CLASS_NAME);
   try{
     erfGuiBean = new ERF_GuiBean(erf_Classes);
   }catch(InvocationTargetException e){
     throw new RuntimeException("Connection to ERF servlets failed");
   }
   eqkRupPanel.add(erfGuiBean, new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0,
                GridBagConstraints.CENTER,GridBagConstraints.BOTH, defaultInsets, 0, 0 ));
   erfGuiBean.getParameterEditor(erfGuiBean.ERF_PARAM_NAME).getParameter().addParameterChangeListener(this);
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
   try {
     EqkRupForecastAPI eqkRupForecast = erfGuiBean.getSelectedERF_Instance();
     // create the TimeSpan Gui Bean object
     timeSpanGuiBean = new TimeSpanGuiBean(eqkRupForecast.getTimeSpan());
     // show the sitebean in JPanel
     this.timespanPanel.add(this.timeSpanGuiBean,
                            new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0,
         GridBagConstraints.CENTER, GridBagConstraints.BOTH, defaultInsets, 0,
         0));
   }catch(Exception e ) { e.printStackTrace(); }
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
      imtGuiBean.validate();
      imtGuiBean.repaint();
      sitesGuiBean.replaceSiteParams(attenRel.getSiteParamsIterator());
      sitesGuiBean.validate();
      sitesGuiBean.repaint();
    }
    if(name1.equalsIgnoreCase(this.erfGuiBean.ERF_PARAM_NAME)) {
      /* get the selected ERF
           NOTE : We have used erfGuiBean.getSelectedERF_Instance()INSTEAD OF
              erfGuiBean.getSelectedERF.
           Dofference is that erfGuiBean.getSelectedERF_Instance() does not update
              the forecast while erfGuiBean.getSelectedERF updates the ERF
       */
      try {
        this.timeSpanGuiBean.setTimeSpan(erfGuiBean.getSelectedERF_Instance().
                                         getTimeSpan());
      }catch(Exception e) { e.printStackTrace(); }
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
   * Initialize the items to be added to the control list
   */
  private void initControlList() {
    controlComboBox.addItem(CONTROL_PANELS);
    controlComboBox.addItem(REGIONS_OF_INTEREST_CONTROL);
    controlComboBox.addItem(X_VALUES_CONTROL);
    controlComboBox.addItem(DISTANCE_CONTROL);
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
    * initialize the X values for the Hazard Map
    * It will enable the user to set the X values
    */
   private void initX_ValuesControl(){
     if(xValuesPanel == null)
       xValuesPanel = new X_ValuesInCurveControlPanel(this,this);
     if(!useCustomX_Values) xValuesPanel.useDefaultX_Values();
     else xValuesPanel.setX_Values(function);
     xValuesPanel.pack();
     xValuesPanel.show();
   }

   /**
    * Initialize the Min Source and site distance control.
    * This function is called when user selects "Source Site Distance Control"
    * from controls pick list
    */
   private void initDistanceControl() {
     if (this.distanceControlPanel == null)
       distanceControlPanel = new SetMinSourceSiteDistanceControlPanel(this);
     distanceControlPanel.pack();
     distanceControlPanel.show();
   }


   /**
    *
    * @returns the selected IMT
    */
   public String getSelectedIMT() {
     return imtGuiBean.getSelectedIMT();
   }


   /**
    * This forces use of default X-axis values (according to the selected IMT)
    */
   public void setX_ValuesForHazardCurve() {
     useCustomX_Values = false;
   }

   /**
    * Sets the hazard curve x-axis values (if user wants custom values x-axis values).
    * Note that what's passed in is not cloned (the y-axis values will get modified).
    * @param func
    */
   public void setX_ValuesForHazardCurve(ArbitrarilyDiscretizedFunc func) {
     useCustomX_Values = true;
     function = func;
   }



  /**
   *
   * @returns the selected Attenuationrelationship model
   */
  public AttenuationRelationship getSelectedAttenuationRelationship(){
    return attenRel;
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
   else if(selectedControl.equalsIgnoreCase(this.X_VALUES_CONTROL))
      initX_ValuesControl();
   else if(selectedControl.equalsIgnoreCase(this.DISTANCE_CONTROL))
      initDistanceControl();
   controlComboBox.setSelectedItem(this.CONTROL_PANELS);
 }


 /**
  * This function is called when user submits the calculation
  * @param e
  */
 void addButton_actionPerformed(ActionEvent e) {
   // check that user has entered a valid email address
   if(this.emailText.getText().trim().equalsIgnoreCase("")) {
     JOptionPane.showMessageDialog(this, "Please Enter email Address");
     return;
   }
   // get the selected forecast model
   EqkRupForecast eqkRupForecast = null;
   // get the selected IMR
   AttenuationRelationshipAPI imr = imrGuiBean.getSelectedIMR_Instance();
   try {
     //gets the instance of the selected ERF
     String eqkRupForecastLocation =  erfGuiBean.saveSelectedERF();
     // this function will get the selected IMT parameter and set it in IMT
     imtGuiBean.setIMT();
     SitesInGriddedRegion griddedRegionSites = sitesGuiBean.getGriddedRegionSite();
     sendParametersToServlet(griddedRegionSites, imr, eqkRupForecastLocation);
   }
   catch (Exception ex) {
     if (D) System.out.println(C + ":Param warning caught" + ex);
     ex.printStackTrace();
   }
 }


 /**
  * sets up the connection with the servlet on the server (scec.usc.edu)
  */
 private void sendParametersToServlet(SitesInGriddedRegion regionSites,
                                       AttenuationRelationshipAPI imr,
                                       String eqkRupForecastLocation) {


   try{
     if(D) System.out.println("starting to make connection with servlet");
     URL hazardMapServlet = new URL(SERVLET_URL);


     URLConnection servletConnection = hazardMapServlet.openConnection();
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
     toServlet.writeObject(regionSites);
     //sending the IMR object to the servlet
     toServlet.writeObject(imr);
     //sending the EQK forecast object to the servlet
     toServlet.writeObject(eqkRupForecastLocation);
     //send the X values in a arraylist
     ArrayList list = new ArrayList();
     for(int i = 0; i<function.getNum(); ++i) list.add(new String(""+function.getX(i)));
     toServlet.writeObject(list);
     // send the MAX DISTANCE
     Double maxDistance;
     if(distanceControlPanel == null ) maxDistance = new Double(HazardCurveCalculator.MAX_DISTANCE_DEFAULT);
     else maxDistance = new Double(distanceControlPanel.getDistance());
     toServlet.writeObject(maxDistance);
     //sending email address to the servlet
     toServlet.writeObject(emailText.getText());
     //sending the parameters info. to the servlet
     toServlet.writeObject(getParametersInfo());
     toServlet.flush();
     toServlet.close();

     // Receive the datasetnumber from the servlet after it has received all the data
     ObjectInputStream fromServlet = new ObjectInputStream(servletConnection.getInputStream());
     String dataSetNumber=fromServlet.readObject().toString();
     JOptionPane.showMessageDialog(this, "Generated Dataset id is:"+dataSetNumber);
     if(D) System.out.println("Receiving the Input from the Servlet:"+dataSetNumber);
     fromServlet.close();

   }catch (Exception e) {
     System.out.println("Exception in connection with servlet:" +e);
     e.printStackTrace();
   }
 }

 /**
  * Returns the metadata associated with this calculation
  *
  * @returns the String containing the values selected for different parameters
  */
 public String getParametersInfo() {
   String systemSpecificLineSeparator = org.scec.util.SystemPropertiesUtils.getSystemLineSeparator();
   return "IMR Param List:" + systemSpecificLineSeparator +
       "---------------" + systemSpecificLineSeparator +
       this.imrGuiBean.getVisibleParametersCloned().
       getParameterListMetadataString() + systemSpecificLineSeparator +
       systemSpecificLineSeparator +
       "Region Param List: " + systemSpecificLineSeparator +
       "----------------" + systemSpecificLineSeparator +
       sitesGuiBean.getVisibleParametersCloned().
       getParameterListMetadataString() + systemSpecificLineSeparator +
       systemSpecificLineSeparator + "IMT Param List: " +
       systemSpecificLineSeparator +
       "---------------" + systemSpecificLineSeparator +
       imtGuiBean.getVisibleParametersCloned().getParameterListMetadataString() +
       systemSpecificLineSeparator +
       systemSpecificLineSeparator + "Forecast Param List: " +
       systemSpecificLineSeparator +
       "--------------------" + systemSpecificLineSeparator +
       erfGuiBean.getParameterList().getParameterListMetadataString() +
       systemSpecificLineSeparator +
       systemSpecificLineSeparator + "TimeSpan Param List: " +
       systemSpecificLineSeparator +
       "--------------------" + systemSpecificLineSeparator +
       timeSpanGuiBean.getVisibleParametersCloned().
       getParameterListMetadataString() + systemSpecificLineSeparator;
 }



}
