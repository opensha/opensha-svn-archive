package org.scec.sha.gui.beans;

import java.awt.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;

import java.awt.event.*;


import org.scec.sha.mapping.*;
import org.scec.param.*;
import org.scec.sha.gui.infoTools.ImageViewerWindow;
import org.scec.util.FileUtils;
import org.scec.webservices.client.*;
import org.scec.data.*;
import org.scec.sha.earthquake.EqkRupture;

/**
 * <p>Title: MapGuiBean</p>
 * <p>Description: This class generates and displays a GMT map for an XYZ dataset using
 * the settings in the GMT_SettingsControlPanel. It displays the image file in a JPanel.
 * This class is used in showing the ScenarioShakeMaps which also defines the rupture surface
 * and does special calculation if the person has choosen to generate the Hazus data.</p>
 * @author: Ned Field, Nitin Gupta & Vipin Gupta
 * @version 1.0
 */

public class MapGuiBean extends GMT_MapGuiBean {

  /**
   * Name of the class
   */
  protected final static String C = "MapGuiBean";


  /**
   * Class constructor accepts the GMT parameters list
   * @param gmtMap
   */
  public MapGuiBean() {
    gmtMap = null;
    //instance of the GMT Control Panel to get the GMT parameters value.
    gmtMap= new GMT_MapGeneratorForShakeMaps();
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
   * this function generates and displays a GMT map for an XYZ dataset using
   * the settings in the GMT_SettingsControlPanel.
   *
   * @param fileName: name of the XYZ file
   */
  public void makeMap(XYZ_DataSetAPI xyzVals,EqkRupture eqkRupture,String imt,String metadata){

    boolean gmtServerCheck = ((Boolean)gmtMap.getAdjustableParamsList().getParameter(gmtMap.GMT_WEBSERVICE_NAME).getValue()).booleanValue();
    //creating the Metadata file in the GMT_MapGenerator
    gmtMap.createMapInfoFile(metadata);
    if(gmtServerCheck){
      //imgName=gmtMap.makeMapUsingWebServer(xyzVals);
      try{
        imgName =((GMT_MapGeneratorForShakeMaps)gmtMap).makeMapUsingServlet(xyzVals,eqkRupture,imt);
        metadata +="<br><p>Click:  "+"<a href=\""+gmtMap.getGMTFilesWebAddress()+"\">"+gmtMap.getGMTFilesWebAddress()+"</a>"+"  to download files.</p>";
      }catch(RuntimeException e){
        e.printStackTrace();
        JOptionPane.showMessageDialog(this,e.getMessage(),"Server Problem",JOptionPane.INFORMATION_MESSAGE);
        return;
      }
    }
    else{
      try{
        imgName = ((GMT_MapGeneratorForShakeMaps)gmtMap).makeMapLocally(xyzVals,eqkRupture,imt);
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
   * this function generates and displays a GMT map for XYZ dataset using
   * the settings in the GMT_SettingsControlPanel.
   *
   * @param fileName: name of the XYZ file
   */
  public void makeHazusShapeFilesAndMap(XYZ_DataSetAPI sa03_xyzVals,XYZ_DataSetAPI sa10_xyzVals,
                      XYZ_DataSetAPI pga_xyzVals, XYZ_DataSetAPI pgv_pgvVals,
                      EqkRupture eqkRupture,String imt,String metadata){
    String[] imgNames = null;
    //boolean gmtServerCheck = ((Boolean)gmtMap.getAdjustableParamsList().getParameter(gmtMap.GMT_WEBSERVICE_NAME).getValue()).booleanValue();
    gmtMap.getAdjustableParamsList().getParameter(gmtMap.GMT_WEBSERVICE_NAME).setValue(new Boolean(true));
    //creating the Metadata file in the GMT_MapGenerator
    gmtMap.createMapInfoFile(metadata);
    //if(gmtServerCheck){
    try{
      imgNames =((GMT_MapGeneratorForShakeMaps)gmtMap).makeHazusFileSetUsingServlet(sa03_xyzVals,sa10_xyzVals, pga_xyzVals,
          pgv_pgvVals,eqkRupture);
      metadata +="<br><p>Click:  "+"<a href=\""+gmtMap.getGMTFilesWebAddress()+"\">"+gmtMap.getGMTFilesWebAddress()+"</a>"+"  to download files.</p>";
    }catch(RuntimeException e){
      e.printStackTrace();
      JOptionPane.showMessageDialog(this,e.getMessage(),"Server Problem",JOptionPane.INFORMATION_MESSAGE);
      return;
    }
   // }
    /*else{
      try{
        imgNames = ((GMT_MapGeneratorForShakeMaps)gmtMap).makeHazusFileSetLocally(sa03_xyzVals,sa10_xyzVals, pga_xyzVals,
                                                     pgv_pgvVals,eqkRupture);
      }catch(RuntimeException e){
        JOptionPane.showMessageDialog(this,e.getMessage());
        return;
      }
    }*/

    //checks to see if the user wants to see the Map in a seperate window or not
    if(this.showMapInSeperateWindow){
      //adding the image to the Panel and returning that to the applet
      ImageViewerWindow imgView = new ImageViewerWindow(imgNames,metadata,true);
    }
  }

}