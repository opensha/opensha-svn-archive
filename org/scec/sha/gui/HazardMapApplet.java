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
    implements ParameterChangeListener{


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

  // Strings for control pick list
  private final static String CONTROL_PANELS = "Control Panels";
  private final static String REGIONS_OF_INTEREST_CONTROL = "Regions of Interest";


    // objects for control panels
  private RegionsOfInterestControlPanel regionsOfInterest;


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


  private GridBagLayout gridBagLayout4 = new GridBagLayout();
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
     Vector imrClasses = new Vector();
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
   Vector erf_Classes = new Vector();

   erf_Classes.add(FRANKEL_FORECAST_CLASS_NAME);
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
   erf_Classes.add(PUENTE_HILLS_FORECAST_CLASS_NAME);
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
    this.controlComboBox.addItem(CONTROL_PANELS);
    this.controlComboBox.addItem(REGIONS_OF_INTEREST_CONTROL);
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
   controlComboBox.setSelectedItem(this.CONTROL_PANELS);
 }


 /**
  * This function is called when user submits the calculation
  * @param e
  */
 void addButton_actionPerformed(ActionEvent e) {

   // get the selected forecast model
   EqkRupForecast eqkRupForecast = null;
   // get the selected IMR
   AttenuationRelationshipAPI imr = imrGuiBean.getSelectedIMR_Instance();
   try {
     //gets the instance of the selected ERF
     eqkRupForecast = (EqkRupForecast) erfGuiBean.getSelectedERF();
     // this function will get the selected IMT parameter and set it in IMT
     imtGuiBean.setIMT();
     SitesInGriddedRegion griddedRegionSites = sitesGuiBean.getGriddedRegionSite();
     sendParametersToServlet(griddedRegionSites, imr, eqkRupForecast);
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
                                       EqkRupForecast eqkRupForecast) {


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

     //sending the object of the gridded region sites to the servlet
     toServlet.writeObject(regionSites);
     //sending the IMR object to the servlet
     toServlet.writeObject(imr);
     //sending the EQK forecast object to the servlet
     toServlet.writeObject(eqkRupForecast);

     //sending the Map parameters info. to the servlet
     toServlet.writeObject(getMapParametersInfo());
     toServlet.flush();
     toServlet.close();

     // Receive the datasetnumber from the servlet after it has received all the data
     ObjectInputStream fromServlet = new ObjectInputStream(servletConnection.getInputStream());
     String dataSetNumber=fromServlet.readObject().toString();
     if(D) System.out.println("Receiving the Input from the Servlet:"+dataSetNumber);
     fromServlet.close();

   }catch (Exception e) {
     System.out.println("Exception in connection with servlet:" +e);
     e.printStackTrace();
   }
 }

 /**
      * @returns the String containing the values selected for different parameters
  */
 public String getMapParametersInfo() {
   return "IMR Param List: \n" +
       "\t\t" + this.imrGuiBean.getParameterList().toString() + "\n" +
       "Site Param List: \n" +
       "\t\t" + sitesGuiBean.getParameterList().toString() + "\n" +
       "IMT Param List: \n" +
       "\t\t" + imtGuiBean.getParameterList().toString() + "\n" +
       "Forecast Param List: \n" +
       "\t\t" + erfGuiBean.getParameterList().toString();
 }


}
