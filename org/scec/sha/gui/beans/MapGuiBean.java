package org.scec.sha.gui.beans;

import java.awt.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import java.net.*;
import java.io.*;
import java.awt.event.*;
import javax.activation.*;


import org.scec.mapping.gmtWrapper.*;
import org.scec.param.*;
import org.scec.param.editor.*;
import org.scec.param.event.ParameterChangeListener;
import org.scec.param.event.ParameterChangeEvent;
import org.scec.sha.gui.infoTools.ImageViewerWindow;
import org.scec.util.FileUtils;
import org.scec.webservices.client.*;
import org.scec.data.*;

/**
 * <p>Title: GMT_MapGenerator</p>
 * <p>Description: This class generates and displays a GMT map for an XYZ dataset using
 * the settings in the GMT_SettingsControlPanel. It displays the image file in a JPanel.
 * </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author: Ned Field, Nitin Gupta & Vipin Gupta
 * @version 1.0
 */

public class MapGuiBean extends JPanel implements
    ParameterChangeListener {

  /**
   * Name of the class
   */
  protected final static String C = "MapGuiBean";

  // for debug purpose
  protected final static boolean D = false;


  private final static String GMT_TITLE = new String("Set GMT Parameters");

  //instance of the GMT Control Panel to get the GMT parameters value.
  private GMT_MapGenerator gmtMap= new GMT_MapGenerator();

  //flag to see if one wants to run the GMT from the server
  private boolean gmtFromServer = true;

  private ParameterListEditor editor;

  //check to see if user user wants GMT from the GMT webservice
  private JCheckBox gmtServerCheck = new JCheckBox();
  //check to see if user wants linear or log plot
  private JCheckBox logPlotCheck = new JCheckBox();
  private GridBagLayout gridBagLayout1 = new GridBagLayout();

  //boolean flag to check if we need to show the Map in a seperate window
  private boolean showMapInSeperateWindow = true;

  //name of the image file( or else full URL to image file if using the webservice)
  String imgName=null;



  /**
   * Class constructor accepts the GMT parameters list
   * @param gmtMap
   */
  public MapGuiBean() {
    // search path needed for making editors
    String[] searchPaths = new String[1];
    searchPaths[0] = ParameterListEditor.getDefaultSearchPath();
    //get the adjustableParam List from the GMT_MapGenerator
    ListIterator it=gmtMap.getAdjustableParamsIterator();
    ParameterList parameterList = new ParameterList();
    while(it.hasNext())
      parameterList.addParameter((ParameterAPI)it.next());
    editor = new ParameterListEditor(parameterList);
    editor.setTitle(GMT_TITLE);
    parameterList.getParameter(GMT_MapGenerator.COLOR_SCALE_MODE_NAME).addParameterChangeListener(this);
    changeColorScaleModeValue(GMT_MapGenerator.COLOR_SCALE_MODE_DEFAULT);
    try {
      jbInit();
    }
    catch(Exception e) {
      e.printStackTrace();
    }
  }

  /**
   *
   * @param regionParamsFlag: boolean flag to check if the region params are to be shown in the
   */
  public void showGMTParams(boolean regionParamsFlag) {
      editor.getParameterEditor(gmtMap.MAX_LAT_PARAM_NAME).setVisible(regionParamsFlag);
      editor.getParameterEditor(gmtMap.MIN_LAT_PARAM_NAME).setVisible(regionParamsFlag);
      editor.getParameterEditor(gmtMap.MAX_LON_PARAM_NAME).setVisible(regionParamsFlag);
      editor.getParameterEditor(gmtMap.MIN_LON_PARAM_NAME).setVisible(regionParamsFlag);
      editor.getParameterEditor(gmtMap.GRID_SPACING_PARAM_NAME).setVisible(regionParamsFlag);
  }

  /**
   * private function that initialises the region params for the GMT plot region
   * @param minLat
   * @param maxLat
   * @param minLon
   * @param maxLon
   * @param gridSpacing
   */
  public void setGMTRegionParams(double minLat,double maxLat,double minLon,double maxLon,
                               double gridSpacing){
    if(D) System.out.println(C+" setGMTRegionParams: " +minLat+"  "+maxLat+"  "+minLon+"  "+maxLon);
    editor.getParameterList().getParameter(GMT_MapGenerator.MIN_LAT_PARAM_NAME).setValue(new Double(minLat));
    editor.getParameterList().getParameter(GMT_MapGenerator.MAX_LAT_PARAM_NAME).setValue(new Double(maxLat));
    editor.getParameterList().getParameter(GMT_MapGenerator.MIN_LON_PARAM_NAME).setValue(new Double(minLon));
    editor.getParameterList().getParameter(GMT_MapGenerator.MAX_LON_PARAM_NAME).setValue(new Double(maxLon));
    editor.getParameterList().getParameter(GMT_MapGenerator.GRID_SPACING_PARAM_NAME).setValue(new Double(gridSpacing));
  }


  /**
   * this function listens for parameter change
   * @param e
   */
  public void parameterChange(ParameterChangeEvent e) {
    String name = e.getParameterName();
    if(name.equalsIgnoreCase(GMT_MapGenerator.COLOR_SCALE_MODE_NAME))
      changeColorScaleModeValue((String)e.getNewValue());
  }

  /**
   * If user chooses Manula or "From Data" color mode, then min and max color limits
   * have to be set Visible and invisible respectively
   * @param val
   */
  private void changeColorScaleModeValue(String val) {
    if(val.equalsIgnoreCase(GMT_MapGenerator.COLOR_SCALE_MODE_FROMDATA)) {
      editor.getParameterEditor(GMT_MapGenerator.COLOR_SCALE_MAX_PARAM_NAME).setVisible(false);
      editor.getParameterEditor(GMT_MapGenerator.COLOR_SCALE_MIN_PARAM_NAME).setVisible(false);
    } else {
      editor.getParameterEditor(GMT_MapGenerator.COLOR_SCALE_MAX_PARAM_NAME).setVisible(true);
      editor.getParameterEditor(GMT_MapGenerator.COLOR_SCALE_MIN_PARAM_NAME).setVisible(true);
    }
  }

  /**
   * this function generates and displays a GMT map for an XYZ dataset using
   * the settings in the GMT_SettingsControlPanel.
   *
   * @param fileName: name of the XYZ file
   */
  public void makeMap(XYZ_DataSetAPI xyzVals,String paramsInfo){

    //checks to see if the user wants Log Plot, if so then convert the zValues to the Log Space
    if(this.logPlotCheck.isSelected()){
      //Vector of the Original z Values in the linear space
      Vector zLinearVals = xyzVals.getZ_DataSet();
      //Vector to add the Z Values as the Log space
      Vector zLogVals = new Vector();
      int size = zLinearVals.size();
      for(int i=0;i<size;++i){
        double zVal = ((Double)zLinearVals.get(i)).doubleValue();
        if(zVal ==0){
          /*JOptionPane.showMessageDialog(this,"Cannot take out log becuase Values contain zeros,"+
                                        "so reverting back to Linear","Cannot Plot Log",JOptionPane.OK_OPTION);
          this.logPlotCheck.setSelected(false);
          zLogVals = zLinearVals;
          break;*/
          zVal = StrictMath.pow(10,-16);
        }
        zLogVals.add(new Double(0.4343 * StrictMath.log(zVal)));
      }
      //setting the values in the XYZ Dataset.
      xyzVals.setXYZ_DataSet(xyzVals.getX_DataSet(),xyzVals.getY_DataSet(),zLogVals);
    }
    if(this.gmtServerCheck.isSelected()){
      //imgName = openConnection(xyzVals);
      imgName=gmtMap.makeMapUsingWebServer(xyzVals);
      //imgName=openWebServiceConnection(fileName);
      paramsInfo +="\n"+"You can download all the files from the following website:\n"+
                   gmtMap.getGMTFilesWebAddress();
    }
    else{
      try{
        imgName = gmtMap.makeMap(xyzVals);
      }catch(RuntimeException e){
        JOptionPane.showMessageDialog(this,e.getMessage());
        return;
      }
    }

    //checks to see if the user wants to see the Map in a seperate window or not
    if(this.showMapInSeperateWindow){
    //adding the image to the Panel and returning that to the applet
    ImageViewerWindow imgView = new ImageViewerWindow(imgName,paramsInfo,gmtFromServer);
    }
  }

  /**
   *
   * @returns the GMT_MapGenerator GMT object
   */
  public GMT_MapGenerator getGMTObject(){
    return gmtMap;
  }

  /**
   * Sets the gui elements for the map using GMT
   * @throws Exception
   */
  private void jbInit() throws Exception {
    gmtServerCheck.setSelected(true);
    gmtServerCheck.setText("Use GMT web Service");
    gmtServerCheck.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        gmtServerCheck_actionPerformed(e);
      }
    });
    this.setLayout(gridBagLayout1);
    logPlotCheck.setText("Log Plot");
    this.add(editor,  new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(4, 4, 0, 13), 361, 226));
    this.add(gmtServerCheck,  new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(8, 123, 0, 132), 0, 0));
    this.add(logPlotCheck,                   new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5, 112, 9, 148), 55, 1));
  }

  /**
   * sets up the connection with the servlet on the server (scec.usc.edu)
   */
  String openConnection(XYZ_DataSetAPI xyzDataVals) {

    String imgURL=null;

    try{

      if(D) System.out.println("starting to make connection with servlet");
      URL hazardMapServlet = new
                             URL("http://scec.usc.edu:9999/examples/servlet/GMT_MapGeneratorServlet");


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

      ObjectOutputStream outputToServlet = new
          ObjectOutputStream(servletConnection.getOutputStream());


      //sending the object of GMT_MapGenerator to servlet
      outputToServlet.writeObject(this.gmtMap);


      //sending the contents of the XYZ data set to the servlet
      outputToServlet.writeObject(xyzDataVals);

      outputToServlet.flush();
      outputToServlet.close();

      // Receive the "destroy" from the servlet after it has received all the data
      ObjectInputStream inputToServlet = new
          ObjectInputStream(servletConnection.getInputStream());

      imgURL=inputToServlet.readObject().toString();
      if(D) System.out.println("Receiving the Input from the Servlet:"+imgURL);
      inputToServlet.close();

    }
    catch (Exception e) {
      System.out.println("Exception in connection with servlet:" +e);
      e.printStackTrace();
    }
    return imgURL;
  }

  /**
   * Flag to determine whether to show the Map in a seperate pop up window
   * @param flag
   */
  public void setMapToBeShownInSeperateWindow(boolean flag){
    this.showMapInSeperateWindow = flag;
  }

  /**
   *
   * @returns the image name of the Map ( or the full URL address to the image file
   * if using the webService)
   */
  public String getImageName(){
    return this.imgName;
  }

  /**
   *
   * @returns whether the user wants the GMT from server or from his own machine
   */
  public boolean isGMT_FromServer(){
    return this.gmtFromServer;
  }

  void gmtServerCheck_actionPerformed(ActionEvent e) {

    if(this.gmtServerCheck.isSelected())
      this.gmtFromServer=true;
    else
      gmtFromServer=false;
  }
}