package org.scec.sha.earthquake.rupForecastImpl.step.gui;

import java.awt.*;
import java.awt.event.*;
import java.applet.*;
import javax.swing.*;
import javax.swing.border.*;
import java.util.Hashtable;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.io.File;
import java.io.FileReader;

import java.io.IOException;
import java.io.BufferedReader;
import java.text.DecimalFormat;

import org.scec.param.ParameterList;
import org.scec.param.StringParameter;
import org.scec.param.DoubleParameter;
import org.scec.param.editor.ParameterListEditor;
import org.scec.sha.gui.beans.IMLorProbSelectorGuiBean;
import org.scec.sha.gui.beans.MapGuiBean;
import org.scec.data.*;

/**
 * <p>Title: STEP_MapViewerApp </p>
 * <p>Description: This applet is needed for viewing the data sets generated by
 * STEP ERF </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class STEP_MapViewerApp extends JApplet {
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
  private static final String NO_DATA_EXISTS = "No STEP Map Data Exists";
  // title of the window
  private static final String TITLE = "STEP Map Viewer";

  //directory where we put all our step related directories (backGround, Addon and Combined)
  private static final String STEP_DIR = "step/";

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
  GridBagLayout gridBagLayout1 = new GridBagLayout();
  GridBagLayout gridBagLayout5 = new GridBagLayout();

  //Get a parameter value
  public String getParameter(String key, String def) {
    return isStandalone ? System.getProperty(key, def) :
      (getParameter(key) != null ? getParameter(key) : def);
  }

  //Construct the applet
  public STEP_MapViewerApp() {
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
    mainSplitPane.add(gmtSplitPane, JSplitPane.BOTTOM);
    mainSplitPane.setRightComponent(gmtSplitPane);
    gmtSplitPane.setLeftComponent(siteSplitPane);
    gmtSplitPane.setRightComponent(gmtPanel);
    dataSetPanel.setLayout(gridBagLayout1);
    gmtPanel.setLayout(gridBagLayout2);
    siteSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
    this.getContentPane().add(mainSplitPane,   new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 4), 274, -2));
    mainSplitPane.add(dataSetPanel, JSplitPane.TOP);
    gmtSplitPane.add(gmtPanel, JSplitPane.RIGHT);
    gmtSplitPane.add(siteSplitPane, JSplitPane.LEFT);
    siteSplitPane.add(sitePanel, JSplitPane.LEFT);
    siteSplitPane.add(imlProbPanel, JSplitPane.RIGHT);
    dataSetPanel.add(jLabel1,   new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(24, 8, 0, 0), 22, 4));
    dataSetPanel.add(dataSetCombo,            new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(24, 7, 0, 0), 65, 0));
    dataSetPanel.add(dataSetText,   new GridBagConstraints(0, 2, 2, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 8, 0, 11), 0, 369));
    dataSetPanel.add(jLabel2,   new GridBagConstraints(0, 1, 2, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(22, 16, 0, 170), 82, 1));
    dataSetPanel.add(mapButton,   new GridBagConstraints(0, 4, 2, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(11, 95, 10, 119), 50, 11));
    mainSplitPane.setDividerLocation(350);
    gmtSplitPane.setDividerLocation(190);
    siteSplitPane.setDividerLocation(340);
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
    STEP_MapViewerApp applet = new STEP_MapViewerApp();
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
    try {
      File dirs =new File(this.STEP_DIR);
      File[] dirList=dirs.listFiles(); // get the list of all the data in the parent directory
      if(dirList==null) {
        JOptionPane.showMessageDialog(this,NO_DATA_EXISTS);
        System.exit(0);
      }
      // for each data set, read the meta data and sites info
      for(int i=0;i<dirList.length;++i){
        if(dirList[i].isDirectory()){
          // read the meta data file
          String dataSetDescription= new String();
          try {
            FileReader dataReader = new FileReader(this.STEP_DIR+
                dirList[i].getName()+"/metadata.dat");
            this.dataSetCombo.addItem(dirList[i].getName());
            BufferedReader in = new BufferedReader(dataReader);
            dataSetDescription = "";
            String str=in.readLine();
            while(str!=null) {
              dataSetDescription += str+"\n";
              str=in.readLine();
            }
            metaDataHash.put(dirList[i].getName(),dataSetDescription);
            in.close();
          }catch(Exception ee) {
            ee.printStackTrace();
          }

          try {
            // read the sites file
            FileReader dataReader =
                new FileReader(this.STEP_DIR+dirList[i].getName()+
                "/sites.dat");
            BufferedReader in = new BufferedReader(dataReader);
            // first line in the file contains the min lat, max lat, discretization interval
            String latitude = in.readLine();
            latHash.put(dirList[i].getName(),latitude);
            // Second line in the file contains the min lon, max lon, discretization interval
            String longitude = in.readLine();
            lonHash.put(dirList[i].getName(),longitude);
          } catch(Exception e) {
            e.printStackTrace();
          }
        }
      }
    }catch(Exception e) {
      e.printStackTrace();
    }
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
   // fill the minlon ArrayList
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
    mapGuiBean = new MapGuiBean();
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
    double gridSpacing = ((Double)sitesParamList.getParameter(this.GRIDSPACING_PARAM_NAME).getValue()).doubleValue();
    String selectedSet = this.dataSetCombo.getSelectedItem().toString();
    // set the lat and lon limits in mao gui bean
    mapGuiBean.setRegionParams(minLat, maxLat, minLon, maxLon, gridSpacing);
    // whethert IML@prob is selected or vics versa
    boolean isProbAt_IML = true;
    if(imlProbGuiBean.getSelectedOption().equalsIgnoreCase(imlProbGuiBean.IML_AT_PROB))
      isProbAt_IML = false;
    double val = this.imlProbGuiBean.getIML_Prob();

    // make the xyz dataset and pass it to the mapGuiBean
    mapGuiBean.makeMap(this.readAndWriteFile(minLat, maxLat, minLon, maxLon,
        gridSpacing, selectedSet, isProbAt_IML, val),null,"Prob",this.dataSetText.getText());
  }

  /**
   * This method reads the file and generates the final outputfile
   * for the range of the lat and lon selected by the user . The final output is
   * generated based on the selcetion made by the user either for the iml@prob or
   * prob@iml. The data is appended to the end of the until all the list of the
   * files have been searched for thr input iml or prob value. The final output
   * file is given as the input to generate the grd file.
   * @param minLat
   * @param maxLat
   * @param minLon
   * @param maxLon
   */
   private XYZ_DataSetAPI readAndWriteFile(double minLat,double maxLat,double minLon,
                                 double maxLon,double gridSpacing,
                                 String selectedSet, boolean isProbAt_IML, double val){

     //searching the directory for the list of the files.
     File dir = new File(this.STEP_DIR+selectedSet+"/");
     XYZ_DataSetAPI xyzData;
     ArrayList xVals = new ArrayList();
     ArrayList yVals = new ArrayList();
     ArrayList zVals = new ArrayList();
     String[] fileList=dir.list();
     //formatting of the text double Decimal numbers for 2 places of decimal.
     DecimalFormat d= new DecimalFormat("0.00##");
     for(int i=0;i<fileList.length;++i){
       if(fileList[i].endsWith("txt")){
         String lat=fileList[i].substring(0,fileList[i].indexOf("_"));
         String lon=fileList[i].substring(fileList[i].indexOf("_")+1,fileList[i].indexOf(".txt"));
         double mLat = Double.parseDouble(lat);
         double mLon = Double.parseDouble(lon);
         double diffLat=Double.parseDouble(d.format(mLat-minLat));
         double diffLon=Double.parseDouble(d.format(mLon-minLon));

         //looking if the file we are reading has lat and lon multiple of gridSpacing
         //in Math.IEEEremainder method Zero is same as pow(10,-16)
         if(Math.abs(Math.IEEEremainder(diffLat,gridSpacing)) <.0001
            && Math.abs(Math.IEEEremainder(diffLon,gridSpacing)) < .0001){

           if(mLat>= minLat && mLat<=maxLat && mLon>=minLon && mLon<=maxLon){
             try{
               boolean readFlag=true;

               //reading the desired file line by line.
               FileReader fr= new FileReader(this.STEP_DIR+selectedSet+
                   "/"+fileList[i]);
               BufferedReader bf= new BufferedReader(fr);
               String dataLine=bf.readLine();
               StringTokenizer st;
               double prevIML=0 ;
               double prevProb=0;
               //reading the first of the file
               if(dataLine!=null){
                 st=new StringTokenizer(dataLine);
                 prevIML = Double.parseDouble(st.nextToken());
                 prevProb= Double.parseDouble(st.nextToken());
               }
               while(readFlag){
                 dataLine=bf.readLine();
                 //if the file has been read fully break out of the loop.
                 if(dataLine ==null || dataLine=="" || dataLine.trim().length()==0){
                   readFlag=false;
                   break;
                 }
                 st=new StringTokenizer(dataLine);
                 //using the currentIML and currentProb we interpolate the iml or prob
                 //value entered by the user.
                 double currentIML = Double.parseDouble(st.nextToken());
                 double currentProb= Double.parseDouble(st.nextToken());
                 if(isProbAt_IML){
                   //taking into account the both types of curves, interpolating the value
                   //interpolating the prob value for the iml value entered by the user.
                   if((val>=prevIML && val<=currentIML) ||
                      (val<=prevIML && val>=currentIML)){

                     //final iml value returned after interpolation
                     double finalProb=interpolateProb(val, prevIML,currentIML,prevProb,currentProb);
                     //String curveResult=lon+" "+lat+" "+Math.log(finalProb)+"\n";
                     //appending the iml result to the final output file.
                     xVals.add(lat);
                     yVals.add(lon);
                     zVals.add(new Double(Math.log(finalProb)));
                     break;
                   }
                 }
                 else if((val>=prevProb && val<=currentProb) ||
                         (val<=prevProb && val>=currentProb)){
                   //interpolating the iml value entered by the user to get the final iml for the
                   //corresponding prob.
                   double finalIML=interpolateIML(val, prevProb,currentProb,prevIML,currentIML);
                   //String curveResult=lon+" "+lat+" "+Math.log(finalIML)+"\n";
                   xVals.add(lat);
                   yVals.add(lon);
                   zVals.add(new Double(Math.log(finalIML)));
                   break;
                 }
                 prevIML=currentIML;
                 prevProb=currentProb;
               }
               fr.close();
               bf.close();
             }catch(IOException e){
               System.out.println("File Not Found :"+e);
             }

           }

         }
       }
     }
     xyzData = new ArbDiscretizedXYZ_DataSet(xVals,yVals,zVals);
     return xyzData;
   }


   /**
    * interpolating the prob values to get the final prob for the corresponding iml
    * @param x1=iml1
    * @param x2=iml2
    * @param y1=prob1
    * @param y2=prob2
    * @return prob value for the iml entered
    */
   private double interpolateProb(double iml, double x1,double x2,double y1,double y2){
     return ((iml-x1)/(x2-x1))*(y2-y1) +y1;
   }

   /**
    * interpolating the iml values to get the final iml for the corresponding prob
    * @param x1=iml1
    * @param x2=iml2
    * @param y1=prob1
    * @param y2=prob2
    * @return iml value for the prob entered
    */
   private double interpolateIML(double prob, double y1,double y2,double x1,double x2){
     return ((prob-y1)/(y2-y1))*(x2-x1)+x1;
   }


}

