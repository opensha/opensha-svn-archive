package org.opensha.sha.gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import java.util.*;
import java.lang.reflect.InvocationTargetException;
import javax.swing.Timer;


import org.opensha.sha.earthquake.EqkRupForecast;
import org.opensha.sha.gui.beans.*;
import org.opensha.sha.imr.*;
import org.opensha.param.event.*;
import org.opensha.data.region.SitesInGriddedRectangularRegion;
import org.opensha.sha.gui.controls.*;
import org.opensha.sha.gui.infoTools.*;

import org.opensha.sha.calc.HazardCurveCalculator;
import org.opensha.sha.calc.HazusMapCalculator;

import org.opensha.util.ImageUtils;
import org.opensha.sha.gui.infoTools.ExceptionWindow;
import org.opensha.exceptions.RegionConstraintException;

/**
 * <p>Title: HuzusDataSetCalcApp</p>
 * <p>Description: This application allows the user to calculate the Hazus 
 * dataset on a server at SCEC. Once the dataset is computed an email
 * will be sent to the user that computation have been completed.
 * This application is smart enough to check if the calculation that you are trying
 * to do have already been done. If the computation have already been done, rather
 * then doing the computation again it will return URL to the dataset which user can 
 * download.</p>
 * @author: Ned Field & Nitin Gupta & Vipin Gupta
 * @created : March 15,2004
 * @version 1.0
 */

public class HazusDataSetCalcApp extends JApplet
    implements ParameterChangeListener, Runnable {


  /**
   * Name of the class
   */
  protected final static String C = "HazusDataSetCalcApplet";
  // for debug purpose
  protected final static boolean D = false;

  //variables that determine the width and height of the frame
  private static final int W=600;
  private static final int H=820;

  // default insets
  private Insets defaultInsets = new Insets( 4, 4, 4, 4 );

   //gets the instance of the selected AttenuationRelationship
  private AttenuationRelationship attenRel;

  private boolean isStandalone;

  /**
   *  The object class names for all the supported Eqk Rup Forecasts
   */
  public final static String FRANKEL96_ADJ_FORECAST_CLASS_NAME = "org.opensha.sha.earthquake.rupForecastImpl.Frankel96.Frankel96_AdjustableEqkRupForecast";
  public final static String STEP_FORECAST_CLASS_NAME = "org.opensha.sha.earthquake.rupForecastImpl.step.STEP_EqkRupForecast";
  public final static String STEP_ALASKAN_FORECAST_CLASS_NAME = "org.opensha.sha.earthquake.rupForecastImpl.step.STEP_AlaskanPipeForecast";
  public final static String FRANKEL02_ADJ_FORECAST_CLASS_NAME="org.opensha.sha.earthquake.rupForecastImpl.Frankel02.Frankel02_AdjustableEqkRupForecast";
  public final static String WG02_ADJ_FORECAST_CLASS_NAME = "org.opensha.sha.earthquake.rupForecastImpl.WG02.WG02_EqkRupForecast";
  public final static String FLOATING_POISSON_FAULT_ERF_CLASS_NAME = "org.opensha.sha.earthquake.rupForecastImpl.FloatingPoissonFaultERF";
  public final static String WGCEP_UCERF1_ERF_CLASS_NAME = "org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF1.WGCEP_UCERF1_EqkRupForecast";

  // Strings for control pick list
  private final static String CONTROL_PANELS = "Control Panels";
  private final static String REGIONS_OF_INTEREST_CONTROL = "Regions of Interest";
  private final static String DISTANCE_CONTROL = "Max Source-Site Distance";


  // objects for control panels
  private RegionsOfInterestControlPanel regionsOfInterest;
  private SetMinSourceSiteDistanceControlPanel distanceControlPanel;
  //private HazardMapSubmissionMethods mapSubmissionMethods;


  // instances of the GUI Beans which will be shown in this applet
  private ERF_GuiBean erfGuiBean;
  private IMR_GuiBean imrGuiBean;
  private SitesInGriddedRectangularRegionGuiBean sitesGuiBean;

  private JPanel mainPanel = new JPanel();
  private Border border1;
  private JSplitPane mainSplitPane = new JSplitPane();
  private JPanel buttonPanel = new JPanel();
  private JPanel eqkRupPanel = new JPanel();
  private JTabbedPane parameterTabbedPanel = new JTabbedPane();
  private JPanel imrPanel = new JPanel();
  private BorderLayout borderLayout2 = new BorderLayout();
  private GridBagLayout gridBagLayout = new GridBagLayout();
  private JPanel gridRegionSitePanel = new JPanel();


  private JPanel imrSelectionPanel = new JPanel();

  BorderLayout borderLayout1 = new BorderLayout();


  //images for the OpenSHA
  private final static String POWERED_BY_IMAGE = "PoweredBy.gif";



  //keeps track of the step in the application to update the user of the progress.
  private int step;
  //timer to show thw progress bar
  Timer timer;
  //instance of Progress Bar
  private CalcProgressBar calcProgress;
  private JPanel dataPanel = new JPanel();
  private JPanel imgPanel = new JPanel();
  private JLabel imgLabel = new JLabel(new ImageIcon(ImageUtils.loadImage(this.POWERED_BY_IMAGE)));
  private JButton addButton = new JButton();
  private JComboBox controlComboBox = new JComboBox();
  private GridBagLayout gridBagLayout7 = new GridBagLayout();
  private BorderLayout borderLayout3 = new BorderLayout();
  private JTextField datasetIdText = new JTextField();
  private JLabel datasetLabel = new JLabel();


  //Maximum source site Distance
  private Double maxDistance;
  private GridBagLayout gridBagLayout4 = new GridBagLayout();

  
  //Construct the applet
  public HazusDataSetCalcApp() {}
  //Initialize the applet
  public void init() {
    try {
      // initialize the control pick list
      initControlList();
      jbInit();
    }
    catch(Exception e) {
      ExceptionWindow bugWindow = new ExceptionWindow(this,e.getStackTrace(),"Exception occured while initializing the application "+
          "Parameters values have not been set yet.");
      bugWindow.setVisible(true);
      bugWindow.pack();
    }
    try{
      initIMRGuiBean();
    }catch(RuntimeException e){
      JOptionPane.showMessageDialog(this,"Invalid parameter value",e.getMessage(),JOptionPane.ERROR_MESSAGE);
      return;
    }
    try {
      this.initGriddedRegionGuiBean();
    }
    catch (RegionConstraintException ex) {
      ExceptionWindow bugWindow = new ExceptionWindow(this,ex.getStackTrace(),
          "Exception occured while initializing the  region parameters in Hazard Dataset Calc App"+
          " Parameters values have not been set yet.");
      bugWindow.setVisible(true);
      bugWindow.pack();
    }
    try{
        this.initERFSelector_GuiBean();
      }catch(RuntimeException e){
      JOptionPane.showMessageDialog(this,"Could not connect with ERF's","Error occur in ERF",
                                    JOptionPane.OK_OPTION);
      return;
      }
  }


  //Component initialization
  private void jbInit() throws Exception {
    border1 = new EtchedBorder(EtchedBorder.RAISED,new Color(248, 254, 255),new Color(121, 124, 136));
    this.setSize(new Dimension(564, 834));
    this.getContentPane().setLayout(borderLayout1);
    mainPanel.setBorder(border1);
    mainPanel.setLayout(gridBagLayout);
    mainSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
    buttonPanel.setLayout(borderLayout3);
    eqkRupPanel.setLayout(gridBagLayout);
    
    imrPanel.setLayout(borderLayout2);
    
    buttonPanel.setMinimumSize(new Dimension(391, 50));
    gridRegionSitePanel.setLayout(gridBagLayout);
    imrSelectionPanel.setLayout(gridBagLayout);
    //controlComboBox.setBackground(Color.white);
    dataPanel.setLayout(gridBagLayout4);
    imgPanel.setLayout(gridBagLayout7);
    addButton.setBorder(null);
    addButton.setText("Start Calc");
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
    datasetLabel.setText("Dataset Id:");
    dataPanel.setMinimumSize(new Dimension(548, 150));
    dataPanel.setPreferredSize(new Dimension(549, 150));
    this.getContentPane().add(mainPanel, BorderLayout.CENTER);
    mainPanel.add(mainSplitPane,  new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(1, 1, 2, 3), 0, 431));
    mainSplitPane.add(buttonPanel, JSplitPane.BOTTOM);
    buttonPanel.add(dataPanel, BorderLayout.CENTER);
    dataPanel.add(datasetIdText,  new GridBagConstraints(2, 0, 1, 1, 1.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(30,-10, 0,200), 100, 7));
 
    dataPanel.add(datasetLabel,  new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(30, 19, 0, 0), 28, 10));
    
    dataPanel.add(controlComboBox,  new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(30, 7, 0, 15), 35, 2));
    dataPanel.add(addButton,  new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(27, 20, 20, 100), 79, 12));
    buttonPanel.add(imgPanel, BorderLayout.SOUTH);
    imgPanel.add(imgLabel,  new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(15, 235, 3, 246), 57, 28));
    mainSplitPane.add(parameterTabbedPanel, JSplitPane.TOP);
    
    imrPanel.add(imrSelectionPanel, BorderLayout.CENTER);
    parameterTabbedPanel.addTab("Intensity-Measure Relationship", imrPanel);
    parameterTabbedPanel.addTab("Region & Site Params", gridRegionSitePanel);
    parameterTabbedPanel.addTab( "Earthquake Rupture Forecast", eqkRupPanel );
    mainSplitPane.setDividerLocation(550);
  }

  //Main method
  public static void main(String[] args) {
	HazusDataSetCalcApp application = new HazusDataSetCalcApp();
    application.isStandalone = true;
    JFrame frame = new JFrame();
    //EXIT_ON_CLOSE == 3
    frame.setDefaultCloseOperation(3);
    frame.setTitle("HazardMapDataCalc App ");//getAppVersion()+" )");
    frame.getContentPane().add(application, BorderLayout.CENTER);
    application.init();
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
   * Initialise the Gridded Region sites gui bean
   *
   */
  private void initGriddedRegionGuiBean() throws RegionConstraintException {
    // get the selected IMR
     attenRel = (AttenuationRelationship)imrGuiBean.getSelectedIMR_Instance();
     // create the Site Gui Bean object
     sitesGuiBean = new SitesInGriddedRectangularRegionGuiBean();
     sitesGuiBean.addSiteParams(attenRel.getSiteParamsIterator());
     // show the sitebean in JPanel
     gridRegionSitePanel.add(this.sitesGuiBean, new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0,
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

   erf_Classes.add(FRANKEL02_ADJ_FORECAST_CLASS_NAME);
   erf_Classes.add(FLOATING_POISSON_FAULT_ERF_CLASS_NAME);
   erf_Classes.add(FRANKEL96_ADJ_FORECAST_CLASS_NAME);
   erf_Classes.add(STEP_FORECAST_CLASS_NAME);
   erf_Classes.add(STEP_ALASKAN_FORECAST_CLASS_NAME);
   erf_Classes.add(WG02_ADJ_FORECAST_CLASS_NAME);
   erf_Classes.add(WGCEP_UCERF1_ERF_CLASS_NAME);
   try{
     erfGuiBean = new ERF_GuiBean(erf_Classes);
   }catch(InvocationTargetException e){
     throw new RuntimeException("Connection to ERF servlets failed");
   }
   eqkRupPanel.add(erfGuiBean, new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0,
                GridBagConstraints.CENTER,GridBagConstraints.BOTH, defaultInsets, 0, 0 ));
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
      sitesGuiBean.replaceSiteParams(attenRel.getSiteParamsIterator());
      sitesGuiBean.validate();
      sitesGuiBean.repaint();
    }


  }

  /**
   * Initialize the items to be added to the control list
   */
  private void initControlList() {
    controlComboBox.addItem(CONTROL_PANELS);
    controlComboBox.addItem(REGIONS_OF_INTEREST_CONTROL);
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
    regionsOfInterest.setVisible(true);
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
     distanceControlPanel.setVisible(true);
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
    
    else if(selectedControl.equalsIgnoreCase(this.DISTANCE_CONTROL))
      initDistanceControl();
    controlComboBox.setSelectedItem(this.CONTROL_PANELS);
  }


 /**
  * This function is called when user submits the calculation
  * @param e
  */
 void addButton_actionPerformed(ActionEvent e) {
   calcProgress = new CalcProgressBar("Hazus Application","Initializing Calculation ...");
   timer = new Timer(100, new ActionListener() {
       public void actionPerformed(ActionEvent evt) {
         if(step !=0)
           calcProgress.setProgressMessage("Submitting Calculations , Please wait ...");
         else if(step ==0){
           addButton.setEnabled(true);
           timer.stop();
           calcProgress.dispose();
           calcProgress = null;
         }
       }
     });
     Thread t = new Thread(this);
     t.start();
 }

 /**
  *
  */
 public void run(){
   timer.start();
   try{
     step =1;
     if(distanceControlPanel == null ) maxDistance = new Double(HazardCurveCalculator.MAX_DISTANCE_DEFAULT);
     else maxDistance = new Double(distanceControlPanel.getDistance());
     //starting the Hazus calculation in the new thread, make the call to the HazusMapCalculator Object.
     HazusMapCalculator hazusCalc = new HazusMapCalculator();
     hazusCalc.setMaxSourceDistance(maxDistance);
     String dirName = datasetIdText.getText().trim();
     AttenuationRelationshipAPI imr = imrGuiBean.getSelectedIMR_Instance();
     SitesInGriddedRectangularRegion griddedSites = sitesGuiBean.getGriddedRegionSite();
     EqkRupForecast eqkRupForecast = (EqkRupForecast) erfGuiBean.getSelectedERF();
     step =0;
     hazusCalc.getHazardMapCurves(dirName,griddedSites,imr,eqkRupForecast,this.getParametersInfo());
   }catch(Exception ee){
     ee.printStackTrace();
     timer.stop();
     step =0;
     JOptionPane.showMessageDialog(this,ee.getMessage(),"Input Error",JOptionPane.INFORMATION_MESSAGE);
     return;
   }

 }





 /**
  * Returns the metadata associated with this calculation
  *
  * @returns the String containing the values selected for different parameters
  */
 public String getParametersInfo() {
   String systemSpecificLineSeparator = org.opensha.util.SystemPropertiesUtils.getSystemLineSeparator();
   String metadata = "IMR Param List:" + systemSpecificLineSeparator +
       "---------------" + systemSpecificLineSeparator +
       this.imrGuiBean.getVisibleParametersCloned().
       getParameterListMetadataString() + systemSpecificLineSeparator +
       systemSpecificLineSeparator +
       "Region Param List: " + systemSpecificLineSeparator +
       "----------------" + systemSpecificLineSeparator +
       sitesGuiBean.getVisibleParametersCloned().
       getParameterListMetadataString() + systemSpecificLineSeparator +
       systemSpecificLineSeparator + "Forecast Param List: " +
       systemSpecificLineSeparator +
       "--------------------" + systemSpecificLineSeparator +
       erfGuiBean.getERFParameterList().getParameterListMetadataString() +
       systemSpecificLineSeparator +
       systemSpecificLineSeparator + "TimeSpan Param List: " +
       systemSpecificLineSeparator +
       "--------------------" + systemSpecificLineSeparator +
       erfGuiBean.getSelectedERFTimespanGuiBean().getParameterListMetadataString() + systemSpecificLineSeparator+
       systemSpecificLineSeparator + "Miscellaneous Metadata:"+
       systemSpecificLineSeparator +
       "--------------------" + systemSpecificLineSeparator+
       "Maximum Site Source Distance = "+maxDistance;

 
   return metadata;
 }
}
