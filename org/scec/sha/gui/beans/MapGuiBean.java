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
  private boolean gmtFromServer = false;

  private ParameterListEditor editor;
  //Label to show the imageFile
  private JLabel gmtMapLabel = new JLabel();
  private JCheckBox gmtServerCheck = new JCheckBox();
  private GridBagLayout gridBagLayout1 = new GridBagLayout();

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
    String imgName=null;
    if(this.gmtServerCheck.isSelected()){
      //imgName = openConnection(xyzVals);
      gmtMap.makeMapUsingWebServer(xyzVals);
      String[] fileName = new String[2];
      fileName[0] = gmtMap.getGMT_FileName();
      fileName[1] = gmtMap.getXYZ_FileName();
      imgName=openWebServiceConnection(fileName);
    }
    else{
      try{
        imgName = gmtMap.makeMap(xyzVals);
      }catch(RuntimeException e){
        JOptionPane.showMessageDialog(this,e.getMessage());
        return;
      }
    }
    //adding the image to the Panel and returning that to the applet
    ImageViewerWindow imgView = new ImageViewerWindow(imgName,paramsInfo,gmtFromServer);
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
    gmtServerCheck.setText("Use GMT web Service");
    gmtServerCheck.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        gmtServerCheck_actionPerformed(e);
      }
    });
    this.setLayout(gridBagLayout1);
    this.add(editor,  new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(4, 4, 4, 4), 0,0));
    this.add(gmtServerCheck,  new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(4, 4, 4, 4), 0, 0));
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



 //For the webservices Implementation
 private String openWebServiceConnection(String[] fileName){
   int size=fileName.length;
   String imgWebAddr=null;
   FileDataSource[] fs = new FileDataSource[size+2];
   DataHandler dh[] = new DataHandler[size+2];
   System.out.println("File-0: "+fileName[0]);
   fs[0] =new FileDataSource(fileName[0]);
   dh[0] = new DataHandler(fs[0]);

   System.out.println("File-1: "+fileName[1]);
   fs[1] =new FileDataSource(fileName[1]);
   dh[1] = new DataHandler(fs[1]);

   GMT_WebService_Impl client = new GMT_WebService_Impl();
   GMT_WebServiceAPI gmt = client.getGMT_WebServiceAPIPort();
   try{
     imgWebAddr = gmt.runGMT_Script(fileName,dh);
     System.out.println("imgWebAddr: "+imgWebAddr);
   }catch(Exception e){
     e.printStackTrace();
   }
   return imgWebAddr;
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