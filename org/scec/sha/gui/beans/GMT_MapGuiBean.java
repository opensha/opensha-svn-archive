package org.scec.sha.gui.beans;

import java.awt.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;

import java.awt.event.*;


import org.scec.mapping.gmtWrapper.GMT_MapGenerator;
import org.scec.param.*;
import org.scec.param.editor.*;
import org.scec.param.event.ParameterChangeListener;
import org.scec.param.event.ParameterChangeEvent;
import org.scec.sha.gui.infoTools.ImageViewerWindow;
import org.scec.util.FileUtils;
import org.scec.webservices.client.*;
import org.scec.data.*;
import org.scec.sha.earthquake.EqkRupture;

/**
 * <p>Title: GMT_MapGuiBean</p>
 * <p>Description: This class generates and displays a GMT map for an XYZ dataset using
 * the settings in the GMT_SettingsControlPanel. It displays the image file in a JPanel.
 * </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author: Ned Field, Nitin Gupta & Vipin Gupta
 * @version 1.0
 */

public class GMT_MapGuiBean extends ParameterListEditor implements
    ParameterChangeListener {

  /**
   * Name of the class
   */
  protected final static String C = "MapGuiBean";

  // for debug purpose
  protected final static boolean D = false;


  protected final static String GMT_TITLE = new String("Set GMT Parameters");

  //instance of the GMT Control Panel to get the GMT parameters value.
  protected GMT_MapGenerator gmtMap= new GMT_MapGenerator();


  private GridBagLayout gridBagLayout1 = new GridBagLayout();

  //boolean flag to check if we need to show the Map in a seperate window
  protected boolean showMapInSeperateWindow = true;

  //name of the image file( or else full URL to image file if using the webservice)
  protected String imgName=null;

  private GMT_MapGuiBeanAPI application;


  //class default constructor
  public GMT_MapGuiBean(){};

  /**
   * Class constructor accepts the GMT parameters list
   * @param api : Instance of the application using this Gui Bean.
   */
  public GMT_MapGuiBean(GMT_MapGuiBeanAPI api) {
    application = api;
    //initialise the param list and editor for the GMT Map Params and Editors
    initParamListAndEditor();
    try {
      jbInit();
    }
    catch(Exception e) {
      e.printStackTrace();
    }
  }


  /**
   * This function provides the flexibiltiy of creating the parameterList and editor
   * from application.
   */
  public void createParamListAndEditor(){
    //initialise the param list and editor for the GMT Map Params and Editors
     initParamListAndEditor();
     try {
       jbInit();
     }
     catch(Exception e) {
       e.printStackTrace();
    }
  }

  protected void initParamListAndEditor(){
    //get the adjustableParam List from the GMT_MapGenerator
    ListIterator it=gmtMap.getAdjustableParamsIterator();
    parameterList = new ParameterList();
    while(it.hasNext())
      parameterList.addParameter((ParameterAPI)it.next());
    editorPanel.removeAll();
    addParameters();
    setTitle(GMT_TITLE);
    parameterList.getParameter(GMT_MapGenerator.COLOR_SCALE_MODE_NAME).addParameterChangeListener(this);
    changeColorScaleModeValue(GMT_MapGenerator.COLOR_SCALE_MODE_DEFAULT);
  }

  /**
   *
   * @param regionParamsFlag: boolean flag to check if the region params are to be shown in the
   */
  public void showRegionParams(boolean regionParamsFlag) {
      getParameterEditor(gmtMap.MAX_LAT_PARAM_NAME).setVisible(regionParamsFlag);
      getParameterEditor(gmtMap.MIN_LAT_PARAM_NAME).setVisible(regionParamsFlag);
      getParameterEditor(gmtMap.MAX_LON_PARAM_NAME).setVisible(regionParamsFlag);
      getParameterEditor(gmtMap.MIN_LON_PARAM_NAME).setVisible(regionParamsFlag);
      getParameterEditor(gmtMap.GRID_SPACING_PARAM_NAME).setVisible(regionParamsFlag);
  }

  /**
   * private function that initialises the region params for the GMT plot region
   * @param minLat
   * @param maxLat
   * @param minLon
   * @param maxLon
   * @param gridSpacing
   */
  public void setRegionParams(double minLat,double maxLat,double minLon,double maxLon,
                               double gridSpacing){
    if(D) System.out.println(C+" setGMTRegionParams: " +minLat+"  "+maxLat+"  "+minLon+"  "+maxLon);
    getParameterList().getParameter(GMT_MapGenerator.MIN_LAT_PARAM_NAME).setValue(new Double(minLat));
    getParameterList().getParameter(GMT_MapGenerator.MAX_LAT_PARAM_NAME).setValue(new Double(maxLat));
    getParameterList().getParameter(GMT_MapGenerator.MIN_LON_PARAM_NAME).setValue(new Double(minLon));
    getParameterList().getParameter(GMT_MapGenerator.MAX_LON_PARAM_NAME).setValue(new Double(maxLon));
    getParameterList().getParameter(GMT_MapGenerator.GRID_SPACING_PARAM_NAME).setValue(new Double(gridSpacing));
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
   * If user chooses Manual or "From Data" color mode, then min and max color limits
   * have to be set Visible and invisible respectively
   * @param val
   */
  protected void changeColorScaleModeValue(String val) {
    if(val.equalsIgnoreCase(GMT_MapGenerator.COLOR_SCALE_MODE_FROMDATA)) {
      getParameterEditor(GMT_MapGenerator.COLOR_SCALE_MAX_PARAM_NAME).setVisible(false);
      getParameterEditor(GMT_MapGenerator.COLOR_SCALE_MIN_PARAM_NAME).setVisible(false);
    } else {
      getParameterEditor(GMT_MapGenerator.COLOR_SCALE_MAX_PARAM_NAME).setVisible(true);
      getParameterEditor(GMT_MapGenerator.COLOR_SCALE_MIN_PARAM_NAME).setVisible(true);
    }
  }

  /**
   * this function generates and displays a GMT map for an XYZ dataset using
   * the settings in the GMT_SettingsControlPanel.
   * @param xyzVals : Object for the XYZ values
   * @param metadata : Associated Metadata for the values.
   */
  public void makeMap(XYZ_DataSetAPI xyzVals,String metadata){

    boolean gmtServerCheck = ((Boolean)gmtMap.getAdjustableParamsList().getParameter(gmtMap.GMT_WEBSERVICE_NAME).getValue()).booleanValue();
    if(gmtServerCheck){
      //imgName=gmtMap.makeMapUsingWebServer(xyzVals);
      try{
        imgName =gmtMap.makeMapUsingServlet(xyzVals," ",metadata);
        metadata +="<br><p>Click:  "+"<a href=\""+gmtMap.getGMTFilesWebAddress()+"\">"+gmtMap.getGMTFilesWebAddress()+"</a>"+"  to download files.</p>";
      }catch(RuntimeException e){
        e.printStackTrace();
        JOptionPane.showMessageDialog(this,e.getMessage(),"Server Problem",JOptionPane.INFORMATION_MESSAGE);
       return;
      }
    }
    else{
      try{
        imgName = gmtMap.makeMapLocally(xyzVals," ",metadata);
      }catch(RuntimeException e){
        JOptionPane.showMessageDialog(this,e.getMessage());
        return;
      }
    }

    //checks to see if the user wants to see the Map in a seperate window or not
    if(this.showMapInSeperateWindow){
      //adding the image to the Panel and returning that to the applet
      ImageViewerWindow imgView = new ImageViewerWindow(imgName,metadata,gmtServerCheck);
    }
  }


  /**
   * Flag to determine whether to show the Map in a seperate pop up window
   * @param flag
   */
  public void setMapToBeShownInSeperateWindow(boolean flag){
    this.showMapInSeperateWindow = flag;
  }


  /**
   * return the GMT_MapGenerator object
   * @return
   */
  public GMT_MapGenerator getGMTObject() {
    return this.gmtMap;
  }

}