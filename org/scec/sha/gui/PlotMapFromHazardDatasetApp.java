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
import java.util.Iterator;
import java.util.Enumeration;
import java.util.ArrayList;
import java.util.Collections;
import java.net.*;
import java.io.*;

import org.scec.sha.calc.HazardMapCalculator;
import org.scec.param.ParameterList;
import org.scec.param.StringParameter;
import org.scec.param.DoubleParameter;
import org.scec.param.editor.ParameterListEditor;
import org.scec.sha.gui.beans.IMLorProbSelectorGuiBean;
import org.scec.sha.gui.beans.GMT_MapGuiBean;
import org.scec.sha.gui.beans.GMT_MapGuiBeanAPI;
import org.scec.sha.gui.infoTools.ImageViewerWindow;
import ch.randelshofer.quaqua.QuaquaManager;
import org.scec.sha.gui.infoTools.ExceptionWindow;

/**
 * <p>Title: PlotMapFromHazardDataSetApp </p>
 * <p>Description: This applet is needed for viewing the data sets generated by
 * HazardMapApplet. It connects to servlet hosted on web server gravity.usc.edu
 * gets all HazardMap datasets existing on server, selects one of the datasets.
 * User also has the option of selecting subset of the region for selected
 * dataset. He then sets the GMT parameters and punches "Make Map" button,
 * which will contact the servlet to create the map.</p>
 * @author : Nitin Gupta & Vipin Gupta
 * @version 1.0
 */


public class PlotMapFromHazardDataSetApp extends JApplet implements GMT_MapGuiBeanAPI{
  public static String SERVLET_URL  = "http://gravity.usc.edu/OpenSHA/servlet/HazardMapViewerServlet";
  private boolean isStandalone = false;
  Border border1;

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
  private static final int W = 950;
  private static final int H = 750;

  // gui beans used here
  private IMLorProbSelectorGuiBean imlProbGuiBean;
  private GMT_MapGuiBean mapGuiBean;

  //formatting of the text double Decimal numbers for 2 places of decimal.
  DecimalFormat d= new DecimalFormat("0.00##");
  // default insets
  private Insets defaultInsets = new Insets( 4, 4, 4, 4 );
  private JPanel jPanel1 = new JPanel();
  private JSplitPane siteSplitPane = new JSplitPane();
  private JPanel dataSetPanel = new JPanel();
  private JPanel gmtPanel = new JPanel();
  private JButton mapButton = new JButton();
  private JComboBox dataSetCombo = new JComboBox();
  private JSplitPane gmtSplitPane = new JSplitPane();
  private JPanel sitePanel = new JPanel();
  private JLabel jLabel2 = new JLabel();
  private JButton refreshButton = new JButton();
  private JLabel jLabel1 = new JLabel();
  private JPanel imlProbPanel = new JPanel();
  private JSplitPane mainSplitPane = new JSplitPane();
  private BorderLayout borderLayout1 = new BorderLayout();
  private GridBagLayout gridBagLayout1 = new GridBagLayout();
  private GridBagLayout gridBagLayout3 = new GridBagLayout();
  private GridBagLayout gridBagLayout4 = new GridBagLayout();
  private GridBagLayout gridBagLayout5 = new GridBagLayout();
  private JScrollPane metadataScrollPane = new JScrollPane();
  private JTextArea dataSetText = new JTextArea();
  private GridBagLayout gridBagLayout2 = new GridBagLayout();

  //Get a parameter value
  public String getParameter(String key, String def) {
    return isStandalone ? System.getProperty(key, def) :
      (getParameter(key) != null ? getParameter(key) : def);
  }

  //Construct the applet
  public PlotMapFromHazardDataSetApp() {
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
      ExceptionWindow bugWindow = new ExceptionWindow(this,e.toString());
      bugWindow.show();
      bugWindow.pack();
      e.printStackTrace();
    }
  }

  //Component initialization
  private void jbInit() throws Exception {
    border1 = new EtchedBorder(EtchedBorder.RAISED,new Color(248, 254, 255),new Color(121, 124, 136));
    this.getContentPane().setLayout(borderLayout1);
    jPanel1.setLayout(gridBagLayout1);
    siteSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
    dataSetPanel.setLayout(gridBagLayout2);
    gmtPanel.setLayout(gridBagLayout5);
    mapButton.setForeground(new Color(80, 80, 133));
    mapButton.setText("Make Map");
    mapButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        mapButton_actionPerformed(e);
      }
    });
    dataSetCombo.setForeground(new Color(80, 80, 133));
    dataSetCombo.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        dataSetCombo_actionPerformed(e);
      }
    });
    gmtSplitPane.setLeftComponent(siteSplitPane);
    gmtSplitPane.setRightComponent(gmtPanel);
    sitePanel.setLayout(gridBagLayout4);
    jLabel2.setForeground(new Color(80, 80, 133));
    jLabel2.setText("Data Set Info:");
    refreshButton.setText("Refresh");
    refreshButton.setForeground(new Color(80, 80, 133));
    refreshButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        refreshButton_actionPerformed(e);
      }
    });
    jLabel1.setForeground(new Color(80, 80, 133));
    jLabel1.setText("Choose Data Set:");
    imlProbPanel.setLayout(gridBagLayout3);
    mainSplitPane.setMinimumSize(new Dimension(50, 578));
    mainSplitPane.setBottomComponent(gmtSplitPane);
    mainSplitPane.setLastDividerLocation(150);
    mainSplitPane.setLeftComponent(null);
    mainSplitPane.setRightComponent(gmtSplitPane);
    dataSetText.setBorder(border1);
    dataSetText.setLineWrap(true);
    dataSetPanel.setMinimumSize(new Dimension(50, 581));
    this.getContentPane().add(jPanel1, BorderLayout.CENTER);
    jPanel1.add(mainSplitPane,  new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(10, 10, 10, 9), 600, 543));
    gmtSplitPane.add(gmtPanel, JSplitPane.RIGHT);
    gmtSplitPane.add(siteSplitPane, JSplitPane.LEFT);
    siteSplitPane.add(sitePanel, JSplitPane.LEFT);
    siteSplitPane.add(imlProbPanel, JSplitPane.RIGHT);
    mainSplitPane.add(dataSetPanel, JSplitPane.TOP);
    mainSplitPane.add(gmtSplitPane, JSplitPane.BOTTOM);
    dataSetPanel.add(dataSetCombo,  new GridBagConstraints(1, 0, 2, 1, 1.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(24, 6, 0, 37), 18, 1));
    dataSetPanel.add(refreshButton,  new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 44), 41, 1));
    dataSetPanel.add(mapButton,  new GridBagConstraints(0, 4, 3, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(17, 81, 24, 133), 29, 11));
    dataSetPanel.add(jLabel2,  new GridBagConstraints(0, 2, 2, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(20, 15, 0, 0), 74, 6));
    dataSetPanel.add(jLabel1,  new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(24, 15, 0, 0), 17, 4));
    dataSetPanel.add(metadataScrollPane,  new GridBagConstraints(0, 3, 3, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 15, 0, 18), 0, 354));
    metadataScrollPane.getViewport().add(dataSetText, null);
    siteSplitPane.setDividerLocation(300);
    gmtSplitPane.setDividerLocation(280);
    mainSplitPane.setDividerLocation(375);

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
    PlotMapFromHazardDataSetApp application = new PlotMapFromHazardDataSetApp();
    application.isStandalone = true;
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
    frame.add(application, BorderLayout.CENTER);
    application.init();
    application.start();
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
   * Load all the available data sets by checking the data sets directory
   */
  private void loadDataSets() {
    try{

      URL hazardMapViewerServlet = new URL(this.SERVLET_URL);

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

      // send the flag to servlet indicating to load the names of available datatsets
      outputToServlet.writeObject(org.scec.sha.gui.servlets.HazardMapViewerServlet.GET_DATA);

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
      ExceptionWindow bugWindow = new ExceptionWindow(this,e.toString());
      bugWindow.show();
      bugWindow.pack();
      System.out.println("Exception in connection with servlet:" +e);
      e.printStackTrace();
    }

    // fill the combo box with available data sets
    Enumeration enumeration=metaDataHash.keys();
    ArrayList keys = new ArrayList();
    while(enumeration.hasMoreElements()) keys.add(enumeration.nextElement());
    Collections.sort(keys);
    Iterator it = keys.iterator();

    dataSetCombo.removeAllItems();
    while(it.hasNext()) this.dataSetCombo.addItem(it.next());
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
    ArrayList minLatVector = new ArrayList();
    ArrayList maxLatVector = new ArrayList();
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
   ArrayList minLonVector = new ArrayList();
   ArrayList maxLonVector = new ArrayList();
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
   //creating the grid spacing constraint for parameter
   //does not allow the user to edit the gridspacing and it remains the same
   //using dataset was computed
   ArrayList gridSpacingConstraint = new ArrayList();
   gridSpacingConstraint.add(new String(""+intervalLat));

   StringParameter gridSpacingParam = new StringParameter(GRIDSPACING_PARAM_NAME,
                                                   gridSpacingConstraint,(String)gridSpacingConstraint.get(0));


   // add the params to the list
   this.sitesParamList = new ParameterList();
   sitesParamList.addParameter(minLatParam);
   sitesParamList.addParameter(maxLatParam);
   sitesParamList.addParameter(minLonParam);
   sitesParamList.addParameter(maxLonParam);
   sitesParamList.addParameter(gridSpacingParam);
   this.sitesEditor = new ParameterListEditor(sitesParamList);
   sitesEditor.setTitle(SITES_TITLE);

   // show this gui bean the JPanel
   sitePanel.removeAll();
   this.sitePanel.add(sitesEditor,new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0,
       GridBagConstraints.CENTER, GridBagConstraints.BOTH, defaultInsets, 0, 0 ));
   // also set it in map gui bean
   this.mapGuiBean.setRegionParams(minLat, maxLat, minLon, maxLon, intervalLat);

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
    mapGuiBean = new GMT_MapGuiBean(this);
    // show this gui bean the JPanel
    this.gmtPanel.add(this.mapGuiBean,new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0,
        GridBagConstraints.CENTER, GridBagConstraints.BOTH, defaultInsets, 0, 0 ));
    mapGuiBean.showRegionParams(false);
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
    double gridSpacing = Double.parseDouble((String)sitesParamList.getParameter(this.GRIDSPACING_PARAM_NAME).getValue());
    String selectedSet = this.dataSetCombo.getSelectedItem().toString();
    // set the lat and lon limits in mao gui bean
    mapGuiBean.setRegionParams(minLat, maxLat, minLon, maxLon, gridSpacing);
    //establishes the connection with the servlet
    openConnection();
  }


   /**
    * sets up the connection with the servlet on the server (scec.usc.edu)
    */
   void openConnection() {
     try{

       URL hazardMapViewerServlet = new URL(SERVLET_URL);
       URLConnection servletConnection = hazardMapViewerServlet.openConnection();

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
       toServlet.writeObject(org.scec.sha.gui.servlets.HazardMapViewerServlet.MAKE_MAP);
       //sending the user which dataSet is selected
       toServlet.writeObject((String)this.dataSetCombo.getSelectedItem());

       //sending the GMT params object to the servlet
       toServlet.writeObject(mapGuiBean.getGMTObject());

       //sending the IML or Prob Selection to the servlet
       toServlet.writeObject(imlProbGuiBean.getSelectedOption());

       //sending the IML or Prob Selected value
       toServlet.writeObject(new Double(imlProbGuiBean.getIML_Prob()));

       // metadata for this map
       String metadata = dataSetText.getText()+"\nGMT Param List: \n"+
           "--------------------\n"+
           mapGuiBean.getVisibleParameters().getParameterListMetadataString();
       metadata = metadata +"\nMap Type Param List: \n"+
           "--------------------\n"+
           this.imlProbGuiBean.getVisibleParameters().getParameterListMetadataString();
       metadata = metadata +"\nMap Region Param List: \n"+
           "--------------------\n"+
           this.sitesEditor.getVisibleParameters().getParameterListMetadataString();

       toServlet.writeObject(metadata);

       toServlet.flush();
       toServlet.close();

       // Receive the URL of the jpeg file from the servlet after it has received all the data
       ObjectInputStream fromServlet = new ObjectInputStream(servletConnection.getInputStream());

       String imgName=fromServlet.readObject().toString();
       fromServlet.close();
       // show the map in  a new window
       metadata = metadata.replaceAll("\n","<br>");
       String link = imgName.substring(0, imgName.lastIndexOf('/'));
       metadata +="<br><p>Click:  "+"<a href=\""+link+"\">"+link+"</a>"+"  to download files.</p>";
       ImageViewerWindow imgView = new ImageViewerWindow(imgName, metadata, true);

     }catch (Exception e) {
       ExceptionWindow bugWindow = new ExceptionWindow(this,e.toString());
       bugWindow.show();
       bugWindow.pack();
       System.out.println("Exception in connection with servlet:" +e);
       e.printStackTrace();
     }
   }

  void refreshButton_actionPerformed(ActionEvent e) {
    loadDataSets();
  }

  /**
   * Whenever user chooses a data set in the combo box,
   * this function is called
   * It fills the data set infomation in text area and also the site info is filled
   * @param e
   */

  void dataSetCombo_actionPerformed(ActionEvent e) {
    if(dataSetCombo.getItemCount()>0){
      addDataInfo();
      fillLatLonAndGridSpacing();
    }
  }

}

