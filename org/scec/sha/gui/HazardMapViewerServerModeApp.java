package org.scec.sha.gui;

import java.awt.*;
import java.awt.event.*;
import java.applet.*;
import javax.swing.*;
import javax.swing.border.*;
import java.util.Hashtable;
import java.util.Vector;
import java.util.StringTokenizer;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.BufferedReader;
import java.text.DecimalFormat;
import java.util.Enumeration;
import java.net.*;
import java.io.*;

import org.scec.sha.calc.HazardMapCalculator;
import org.scec.param.ParameterList;
import org.scec.param.StringParameter;
import org.scec.param.DoubleParameter;
import org.scec.param.editor.ParameterListEditor;
import org.scec.sha.gui.beans.IMLorProbSelectorGuiBean;
import org.scec.sha.gui.beans.MapGuiBean;

/**
 * <p>Title: HazardMapViewerServerModeApp </p>
 * <p>Description: This applet is needed for viewing the data sets generated by
 * HazardMapApplet </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author : Nitin Gupta & Vipin Gupta
 * @version 1.0
 */

public class HazardMapViewerServerModeApp extends JApplet {
  private boolean isStandalone = false;
  JSplitPane mainSplitPane = new JSplitPane();
  JSplitPane gmtSplitPane = new JSplitPane();
  JSplitPane siteSplitPane = new JSplitPane();
  JPanel dataSetPanel = new JPanel();
  JPanel gmtPanel = new JPanel();
  GridBagLayout gridBagLayout2 = new GridBagLayout();
  JPanel sitePanel = new JPanel();
  JPanel imlProbPanel = new JPanel();
  GridBagLayout gridBagLayout4 = new GridBagLayout();
  JLabel jLabel1 = new JLabel();
  JComboBox dataSetCombo = new JComboBox();
  JTextArea dataSetText = new JTextArea();
  Border border1;
  JLabel jLabel2 = new JLabel();
  JButton mapButton = new JButton();
  GridBagLayout gridBagLayout3 = new GridBagLayout();

  //HashTables for storing the metadata for each dataset
  Hashtable metaDataHash = new Hashtable();
  //Hashtable for storing the lons from each dataSet
  Hashtable lonHash= new Hashtable();
  //Hashtable for storing the lats from each dataSet
  Hashtable latHash= new Hashtable();

  // paramter list and editor to be made for specifyinh min/max lat/lon
  //and gridspacing
  ParameterList sitesParamList ;
  ParameterListEditor sitesEditor;

  // parameter names for min/max lat/lon and gridspacing
  private final static String MIN_LAT_PARAM_NAME = "Min Lat";
  private final static String MAX_LAT_PARAM_NAME = "Max Lat";
  private final static String MIN_LON_PARAM_NAME = "Min Lon";
  private final static String MAX_LON_PARAM_NAME = "Max Lon";
  private final static String GRIDSPACING_PARAM_NAME = "GridSpacing";
  private final static String SITES_TITLE = "Choose Region";

   // message to display if no data exits
  private static final String NO_DATA_EXISTS = "No Hazard Map Data Exists";
  // title of the window
  private static final String TITLE = "Hazard Map Viewer";

  // width and height
  private static final int W = 800;
  private static final int H = 800;

  // gui beans used here
  private IMLorProbSelectorGuiBean imlProbGuiBean;
  private MapGuiBean mapGuiBean;

  //formatting of the text double Decimal numbers for 2 places of decimal.
  DecimalFormat d= new DecimalFormat("0.00##");
  // default insets
  private Insets defaultInsets = new Insets( 4, 4, 4, 4 );
  JLabel jLabel4 = new JLabel();
  JTextField fileNameTextField = new JTextField();
  JLabel jLabel3 = new JLabel();
  GridBagLayout gridBagLayout1 = new GridBagLayout();
  GridBagLayout gridBagLayout5 = new GridBagLayout();

  //Get a parameter value
  public String getParameter(String key, String def) {
    return isStandalone ? System.getProperty(key, def) :
      (getParameter(key) != null ? getParameter(key) : def);
  }

  //Construct the applet
  public HazardMapViewerServerModeApp() {
  }

  //Initialize the applet
  public void init() {
    try {
      loadDataSets();
      jbInit();
      this.initIML_ProbGuiBean();
      this.initMapGuiBean();
      addDataInfo();
      fillLatLonAndGridSpacing();
    }
    catch(Exception e) {
      e.printStackTrace();
    }
  }

  //Component initialization
  private void jbInit() throws Exception {
    border1 = new EtchedBorder(EtchedBorder.RAISED,new Color(248, 254, 255),new Color(121, 124, 136));
    this.getContentPane().setLayout(gridBagLayout5);
    mainSplitPane.setBottomComponent(gmtSplitPane);
    mainSplitPane.setLeftComponent(null);
    sitePanel.setLayout(gridBagLayout3);
    imlProbPanel.setLayout(gridBagLayout4);
    jLabel1.setForeground(new Color(80, 80, 133));
    jLabel1.setText("Choose Data Set:");
    dataSetCombo.setBackground(new Color(200, 200, 230));
    dataSetCombo.setForeground(new Color(80, 80, 133));
    dataSetCombo.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        dataSetCombo_actionPerformed(e);
      }
    });
    dataSetText.setBorder(border1);
    dataSetText.setLineWrap(true);
    jLabel2.setForeground(new Color(80, 80, 133));
    jLabel2.setText("Data Set Info:");
    mapButton.setBackground(new Color(200, 200, 230));
    mapButton.setForeground(new Color(80, 80, 133));
    mapButton.setText("Show Map");
    mapButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        mapButton_actionPerformed(e);
      }
    });
    jLabel4.setForeground(new Color(80, 80, 133));
    jLabel4.setText("(This is filename used for generating xyz, ps and jpg file)");
    fileNameTextField.setBackground(new Color(200, 200, 230));
    fileNameTextField.setForeground(new Color(80, 80, 133));
    fileNameTextField.setText("test");
    jLabel3.setForeground(new Color(80, 80, 133));
    jLabel3.setText("Choose File Name:");
    mainSplitPane.add(gmtSplitPane, JSplitPane.BOTTOM);
    mainSplitPane.setRightComponent(gmtSplitPane);
    gmtSplitPane.setLeftComponent(siteSplitPane);
    gmtSplitPane.setRightComponent(gmtPanel);
    dataSetPanel.setLayout(gridBagLayout1);
    gmtPanel.setLayout(gridBagLayout2);
    siteSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
    this.getContentPane().add(mainSplitPane,  new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, -5, 22, 4), 207, 4));
    mainSplitPane.add(dataSetPanel, JSplitPane.TOP);
    gmtSplitPane.add(gmtPanel, JSplitPane.RIGHT);
    gmtSplitPane.add(siteSplitPane, JSplitPane.LEFT);
    siteSplitPane.add(sitePanel, JSplitPane.LEFT);
    siteSplitPane.add(imlProbPanel, JSplitPane.RIGHT);
    dataSetPanel.add(jLabel1,  new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(24, 10, 0, 0), 22, 4));
    dataSetPanel.add(dataSetCombo,  new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(24, 7, 0, 66), 12, 1));
    dataSetPanel.add(dataSetText,  new GridBagConstraints(0, 2, 2, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 10, 0, 11), 0, 365));
    dataSetPanel.add(jLabel2,  new GridBagConstraints(0, 1, 2, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(22, 16, 0, 170), 82, 1));
    dataSetPanel.add(mapButton,  new GridBagConstraints(0, 5, 2, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 98, 10, 116), 50, 11));
    dataSetPanel.add(jLabel3,  new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(13, 10, 0, 0), 11, 9));
    dataSetPanel.add(fileNameTextField,  new GridBagConstraints(1, 3, 1, 1, 1.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(13, 0, 0, 37), 150, 4));
    dataSetPanel.add(jLabel4,  new GridBagConstraints(0, 4, 2, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 10, 0, 1), 17, 5));
    mainSplitPane.setDividerLocation(350);
    gmtSplitPane.setDividerLocation(150);
    siteSplitPane.setDividerLocation(300);
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
    HazardMapViewerServerModeApp applet = new HazardMapViewerServerModeApp();
    applet.isStandalone = true;
    Frame frame;
    frame = new Frame() {
      protected void processWindowEvent(WindowEvent e) {
        super.processWindowEvent(e);
        if (e.getID() == WindowEvent.WINDOW_CLOSING) {
          System.exit(0);
        }
      }
      public synchronized void setTitle(String title) {
        super.setTitle(title);
        enableEvents(AWTEvent.WINDOW_EVENT_MASK);
      }
    };
    frame.setTitle(TITLE);
    frame.add(applet, BorderLayout.CENTER);
    applet.init();
    applet.start();
    frame.setSize(W,H);
    Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
    frame.setLocation((d.width - frame.getSize().width) / 2, (d.height - frame.getSize().height) / 2);
    frame.setVisible(true);
  }


  /**
   * Load all the available data sets by checking the data sets directory
   */
  private void loadDataSets() {
    try{

      URL hazardMapViewerServlet = new
                                   URL("http://scec.usc.edu:9999/examples/servlet/HazardMapViewerServlet");

      URLConnection servletConnection = hazardMapViewerServlet.openConnection();

      // inform the connection that we will send output and accept input
      servletConnection.setDoInput(true);
      servletConnection.setDoOutput(true);

      // Don't use a cached version of URL connection.
      servletConnection.setUseCaches (false);
      servletConnection.setDefaultUseCaches (false);
      // Specify the content type that we will send binary data
      servletConnection.setRequestProperty ("Content-Type","application/octet-stream");

      ObjectOutputStream outputToServlet = new
          ObjectOutputStream(servletConnection.getOutputStream());


      //sending the X values vector in the condProbVector to the servlet
      outputToServlet.writeObject("Get Data");

      outputToServlet.flush();
      outputToServlet.close();

      // Receive the "destroy" from the servlet after it has received all the data
      ObjectInputStream inputToServlet = new
          ObjectInputStream(servletConnection.getInputStream());

      metaDataHash=(Hashtable)inputToServlet.readObject();
      lonHash=(Hashtable)inputToServlet.readObject();
      latHash=(Hashtable)inputToServlet.readObject();

      inputToServlet.close();

    }catch (Exception e) {
      System.out.println("Exception in connection with servlet:" +e);
      e.printStackTrace();
    }

    // fill the combo box with available data sets
    Vector dirVector= new Vector();
    Enumeration enum=metaDataHash.keys();
    while(enum.hasMoreElements()) this.dataSetCombo.addItem(enum.nextElement());

  }

  /**
   * Whenever user chooses a data set in the combo box,
   * this function is called
   * It fills the data set infomation in text area and also the site info is filled
   * @param e
   */
  void dataSetCombo_actionPerformed(ActionEvent e) {
    addDataInfo();
    fillLatLonAndGridSpacing();
  }

  /**
 * It will read the sites.info file and fill the min and max Lat and Lon
 */
  private void fillLatLonAndGridSpacing() {

    // get the min and max lat and lat spacing
    String latitude=latHash.get(dataSetCombo.getSelectedItem()).toString();
    StringTokenizer tokenizer = new StringTokenizer(latitude);
    double minLat = Double.parseDouble(tokenizer.nextToken());
    double maxLat = Double.parseDouble(tokenizer.nextToken());
    Vector minLatVector = new Vector();
    Vector maxLatVector = new Vector();
    double intervalLat = Double.parseDouble(tokenizer.nextToken());
    double lat = minLat;
    // fill the in lat vector
    while(lat<maxLat) {
      minLatVector.add(""+d.format(lat)); // fill the min Lat combobox
      lat = lat+intervalLat;
    }

    // fill the max lat vector
    lat = maxLat;
    while(lat>minLat) {
     maxLatVector.add(""+d.format(lat)); // fill the max Lat combobox
     lat = lat-intervalLat;
   }



   // line in LonHashTable contains the min lon, max lon, discretization interval
   String longitude = lonHash.get(dataSetCombo.getSelectedItem()).toString();
   tokenizer = new StringTokenizer(longitude);
   double minLon = Double.parseDouble(tokenizer.nextToken());
   double maxLon = Double.parseDouble(tokenizer.nextToken());
   double intervalLon = Double.parseDouble(tokenizer.nextToken());
   Vector minLonVector = new Vector();
   Vector maxLonVector = new Vector();
   double lon = minLon;
   // fill the minlon Vector
   while(lon<maxLon) {
    minLonVector.add(""+d.format(lon)); // fill the min Lat combobox
    lon = lon+intervalLon;
   }
   // fill the max lon vector
   lon = maxLon;
   while(lon>minLon) {
     maxLonVector.add(""+d.format(lon)); // fill the max Lon combobox
     lon = lon-intervalLon;
   }

   // make the min and max lat param
   StringParameter minLatParam = new StringParameter(MIN_LAT_PARAM_NAME,
       minLatVector, (String)minLatVector.get(0));
   StringParameter maxLatParam = new StringParameter(MAX_LAT_PARAM_NAME,
       maxLatVector, (String)maxLatVector.get(0));
   // make the min and max lon param
   StringParameter minLonParam = new StringParameter(MIN_LON_PARAM_NAME,
       minLonVector, (String)minLonVector.get(0));
   StringParameter maxLonParam = new StringParameter(MAX_LON_PARAM_NAME,
       maxLonVector, (String)maxLonVector.get(0));
   // make the gridspacing param
   DoubleParameter gridSpacingParam = new DoubleParameter(GRIDSPACING_PARAM_NAME,
                                                    new Double(intervalLat));

   // add the params to the list
   this.sitesParamList = new ParameterList();
   sitesParamList.addParameter(minLatParam);
   sitesParamList.addParameter(maxLatParam);
   sitesParamList.addParameter(minLonParam);
   sitesParamList.addParameter(maxLonParam);
   sitesParamList.addParameter(gridSpacingParam);

   // make the editor
   String []searchPaths = new String[1];
   searchPaths[0] = ParameterListEditor.getDefaultSearchPath();
   this.sitesEditor = new ParameterListEditor(sitesParamList, searchPaths);
   sitesEditor.setTitle(SITES_TITLE);

   // show this gui bean the JPanel
   sitePanel.removeAll();
   this.sitePanel.add(sitesEditor,new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0,
       GridBagConstraints.CENTER, GridBagConstraints.BOTH, defaultInsets, 0, 0 ));

   // also set it in map gui bean
   this.mapGuiBean.setGMTRegionParams(minLat, maxLat, minLon, maxLon, intervalLat);

  }


  /**
   * reads the metadata file for each selected item in the combo box
   * and puts the info of the dataset in the textarea.
   */
  private void addDataInfo(){
    String dataSetDescription=metaDataHash.get(dataSetCombo.getSelectedItem()).toString();
    this.dataSetText.setEditable(true);
    dataSetText.setText(dataSetDescription);
    dataSetText.setEditable(false);
  }

  /**
   * initialize the IML prob selector GUI bean
   */
  private void initIML_ProbGuiBean() {
    imlProbGuiBean = new IMLorProbSelectorGuiBean();
     // show this gui bean the JPanel
    this.imlProbPanel.add(this.imlProbGuiBean,new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0,
         GridBagConstraints.CENTER, GridBagConstraints.BOTH, defaultInsets, 0, 0 ));
  }

  /**
   * initialize the map gui bean
   */
  private void initMapGuiBean() {
    mapGuiBean = new MapGuiBean();
    // show this gui bean the JPanel
    this.gmtPanel.add(this.mapGuiBean,new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0,
        GridBagConstraints.CENTER, GridBagConstraints.BOTH, defaultInsets, 0, 0 ));
    mapGuiBean.showGMTParams(false);
  }

  /**
   * this function is called when user chooses  "show Map"
   * @param e
   */
  void mapButton_actionPerformed(ActionEvent e) {
    // get he min/max lat/lon and gridspacing
    double minLat = Double.parseDouble((String)sitesParamList.getParameter(this.MIN_LAT_PARAM_NAME).getValue());
    double maxLat = Double.parseDouble((String)sitesParamList.getParameter(this.MAX_LAT_PARAM_NAME).getValue());
    double minLon = Double.parseDouble((String)sitesParamList.getParameter(this.MIN_LON_PARAM_NAME).getValue());
    double maxLon = Double.parseDouble((String)sitesParamList.getParameter(this.MAX_LON_PARAM_NAME).getValue());
    double gridSpacing = ((Double)sitesParamList.getParameter(this.GRIDSPACING_PARAM_NAME).getValue()).doubleValue();
    String selectedSet = this.dataSetCombo.getSelectedItem().toString();
    // set the lat and lon limits in mao gui bean
    mapGuiBean.setGMTRegionParams(minLat, maxLat, minLon, maxLon, gridSpacing);
    //establishes the connection with the servlet
    openConnection();
  }


   /**
    * sets up the connection with the servlet on the server (scec.usc.edu)
    */
   void openConnection() {

     try{

       URL hazardMapViewerServlet = new
                             URL("http://scec.usc.edu:9999/examples/servlet/HazardMapViewerServlet");
       URLConnection servletConnection = hazardMapViewerServlet.openConnection();

       // inform the connection that we will send output and accept input
       servletConnection.setDoInput(true);
       servletConnection.setDoOutput(true);

       // Don't use a cached version of URL connection.
       servletConnection.setUseCaches (false);
       servletConnection.setDefaultUseCaches (false);
       // Specify the content type that we will send binary data
       servletConnection.setRequestProperty ("Content-Type","application/octet-stream");

       ObjectOutputStream outputToServlet = new
           ObjectOutputStream(servletConnection.getOutputStream());
       outputToServlet.writeObject("Make Map");
       //sending the user which dataSet is selected
       outputToServlet.writeObject((String)this.dataSetCombo.getSelectedItem());

       //sending the GMT params object to the servlet
       outputToServlet.writeObject(mapGuiBean.getGMTObject());

       //sending the IML or Prob Selection to the servlet
       outputToServlet.writeObject(imlProbGuiBean.getSelectedOption());

       //sending the IML or Prob Selected value
       outputToServlet.writeObject(new Double(imlProbGuiBean.getIML_Prob()));

       // check thatuser has entered a valid filename
       if(fileNameTextField.getText().trim().equalsIgnoreCase("")) {
         JOptionPane.showMessageDialog(this, "Please enter the file name");
         return;
       }
       //sending the output file prefix
       outputToServlet.writeObject(fileNameTextField.getText().trim());

       outputToServlet.flush();
       outputToServlet.close();

       // Receive the "destroy" from the servlet after it has received all the data
       ObjectInputStream inputToServlet = new ObjectInputStream(servletConnection.getInputStream());

      String connectionCloseString=inputToServlet.readObject().toString();
      inputToServlet.close();
      this.getAppletContext().showDocument(new URL(connectionCloseString),"_blank");

     }catch (Exception e) {
       System.out.println("Exception in connection with servlet:" +e);
       e.printStackTrace();
     }
   }

}

