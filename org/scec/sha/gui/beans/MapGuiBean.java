package org.scec.sha.gui.beans;

import java.awt.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import java.net.*;
import java.io.*;

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


  //some parameter values for hazus, that needs to have specific value for Hazus files generation
  //checking if hazus file generator param is selected, if not then make it selected and the deselect it again
  private boolean hazusFileGeneratorCheck;
  //checking if log map generator param is selected, if yes then make it unselected and the select it again
  boolean generateMapInLogSpace;
  //always making the map color scale from the data if the person has choosen the Hazus control panel
  String mapColorScaleValue;


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
  public void makeMap(String xyzVals,EqkRupture eqkRupture,String imt,String metadata){

    try{
      // this creates a conection with the server to generate the map on the server
      //after reading the xyz vals file from the server
      imgName = openConnectionToServerToGenerateShakeMap(xyzVals,eqkRupture,imt,metadata);
      //webaddr where all the GMT related file for this map resides on server
      String webaddr = imgName.substring(0,imgName.lastIndexOf("/")+1);
      metadata +="<br><p>Click:  "+"<a href=\""+webaddr+"\">"+webaddr+"</a>"+"  to download files.</p>";
    }catch(RuntimeException e){
      e.printStackTrace();
      JOptionPane.showMessageDialog(this,e.getMessage(),"Server Problem",JOptionPane.INFORMATION_MESSAGE);
      return;
    }

    //checks to see if the user wants to see the Map in a seperate window or not
    if(this.showMapInSeperateWindow){
      //adding the image to the Panel and returning that to the applet
      ImageViewerWindow imgView = new ImageViewerWindow(imgName,metadata,true);
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
    if(gmtServerCheck){
      //imgName=gmtMap.makeMapUsingWebServer(xyzVals);
      try{
        imgName =((GMT_MapGeneratorForShakeMaps)gmtMap).makeMapUsingServlet(xyzVals,eqkRupture,imt,metadata);
        metadata +="<br><p>Click:  "+"<a href=\""+gmtMap.getGMTFilesWebAddress()+"\">"+gmtMap.getGMTFilesWebAddress()+"</a>"+"  to download files.</p>";
      }catch(RuntimeException e){
        e.printStackTrace();
        JOptionPane.showMessageDialog(this,e.getMessage(),"Server Problem",JOptionPane.INFORMATION_MESSAGE);
        return;
      }
    }
    else{
      try{
        imgName = ((GMT_MapGeneratorForShakeMaps)gmtMap).makeMapLocally(xyzVals,eqkRupture,imt,metadata);
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
                      XYZ_DataSetAPI pga_xyzVals, XYZ_DataSetAPI pgv_xyzVals,
                      EqkRupture eqkRupture,String metadata){
    String[] imgNames = null;

    //boolean gmtServerCheck = ((Boolean)gmtMap.getAdjustableParamsList().getParameter(gmtMap.GMT_WEBSERVICE_NAME).getValue()).booleanValue();
     // gmtMap.getAdjustableParamsList().getParameter(gmtMap.GMT_WEBSERVICE_NAME).setValue(new Boolean(true));

    //if(gmtServerCheck){
    try{
      imgNames =((GMT_MapGeneratorForShakeMaps)gmtMap).makeHazusFileSetUsingServlet(sa03_xyzVals,sa10_xyzVals, pga_xyzVals,
          pgv_xyzVals,eqkRupture,metadata);
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

    //gmtMap.getAdjustableParamsList().getParameter(gmtMap.GMT_WEBSERVICE_NAME).setValue(new Boolean(gmtServerCheck));
  }


  /**
   * this function generates and displays a GMT map for XYZ dataset using
   * the settings in the GMT_SettingsControlPanel.
   *
   * @param fileName: name of the XYZ file
   */
  public void makeHazusShapeFilesAndMap(String sa03_xyzVals,String sa10_xyzVals,
                      String pga_xyzVals, String pgv_xyzVals,
                      EqkRupture eqkRupture,String metadata){
    String[] imgNames = null;
    try{
      imgNames = openConnectionToServerToGenerateShakeMapForHazus(sa03_xyzVals, sa10_xyzVals,
          pga_xyzVals, pgv_xyzVals,eqkRupture,metadata);

      //webaddr where all the GMT related file for this map resides on server
      String webaddr = imgNames[0].substring(0,imgNames[0].lastIndexOf("/")+1);
      /*imgNames =((GMT_MapGeneratorForShakeMaps)gmtMap).makeHazusFileSetUsingServlet(sa03_xyzVals,sa10_xyzVals, pga_xyzVals,
          pgv_xyzVals,eqkRupture,metadata);*/
      metadata +="<br><p>Click:  "+"<a href=\""+webaddr+"\">"+webaddr+"</a>"+"  to download files.</p>";
    }catch(RuntimeException e){
      e.printStackTrace();
      JOptionPane.showMessageDialog(this,e.getMessage(),"Server Problem",JOptionPane.INFORMATION_MESSAGE);
      return;
    }

    //checks to see if the user wants to see the Map in a seperate window or not
    if(this.showMapInSeperateWindow){
      //adding the image to the Panel and returning that to the applet
      ImageViewerWindow imgView = new ImageViewerWindow(imgNames,metadata,true);
    }
  }




  /**
   * This Method changes the value of the following GMT parameters to specific for Hazus:
   * Log Plot Param is selected to Linear plot
   * Make Hazus File Param is set to true
   * Map color scale param value is set always from data.
   * The changes to the parameters on specific for the Hazus and needs to reverted
   * back to the original values ,using the function setGMT_ParamsChangedForHazusToOriginalValue().
   * after map has been generated.
   */
  public void setGMT_ParamsForHazus(){

    //instance of the GMT parameter list
    ParameterList paramList = gmtMap.getAdjustableParamsList();

    //checking if hazus file generator param is selected, if not then make it selected and the deselect it again
    hazusFileGeneratorCheck = ((Boolean)paramList.getParameter(GMT_MapGeneratorForShakeMaps.HAZUS_SHAPE_PARAM_NAME).getValue()).booleanValue();
    if(!hazusFileGeneratorCheck)
      paramList.getParameter(GMT_MapGeneratorForShakeMaps.HAZUS_SHAPE_PARAM_NAME).setValue(new Boolean(true));


    //checking if log map generator param is selected, if yes then make it unselected and the select it again
    generateMapInLogSpace = ((Boolean)paramList.getParameter(GMT_MapGeneratorForShakeMaps.LOG_PLOT_NAME).getValue()).booleanValue();
    if(generateMapInLogSpace)
      paramList.getParameter(GMT_MapGeneratorForShakeMaps.LOG_PLOT_NAME).setValue(new Boolean(false));

    //always making the map color scale from the data if the person has choosen the Hazus control panel
    mapColorScaleValue = (String)paramList.getParameter(GMT_MapGeneratorForShakeMaps.COLOR_SCALE_MODE_NAME).getValue();
    if(!mapColorScaleValue.equals(GMT_MapGeneratorForShakeMaps.COLOR_SCALE_MODE_FROMDATA))
      paramList.getParameter(GMT_MapGeneratorForShakeMaps.COLOR_SCALE_MODE_NAME).setValue(GMT_MapGeneratorForShakeMaps.COLOR_SCALE_MODE_FROMDATA);

  }

  /**
   * This method reverts back the settings of the gmt parameters those were set specifically
   * for the Hazus files generation. This has been added seperately so that metadata can
   * show changed value of the parameters, so the user should be able to know the actual
   * parameter setting using which map was computed.
   */
  public void setGMT_ParamsChangedForHazusToOriginalValue(){
    //instance of the GMT parameter list
    ParameterList paramList = gmtMap.getAdjustableParamsList();

    //reverting the value for the Hazus file generation to the what was before the selection of the Hazus control panel.
    if(!hazusFileGeneratorCheck)
      paramList.getParameter(GMT_MapGeneratorForShakeMaps.HAZUS_SHAPE_PARAM_NAME).setValue(new Boolean(false));

    //reverting the value for the Log file generation to the what was before the selection of the Hazus control panel.
    if(generateMapInLogSpace)
      paramList.getParameter(GMT_MapGeneratorForShakeMaps.LOG_PLOT_NAME).setValue(new Boolean(true));

    //reverting the value for the map color generation to the what was before the selection of the Hazus control panel.
    if(!mapColorScaleValue.equals(GMT_MapGeneratorForShakeMaps.COLOR_SCALE_MODE_FROMDATA))
      paramList.getParameter(GMT_MapGeneratorForShakeMaps.COLOR_SCALE_MODE_NAME).setValue(mapColorScaleValue);

  }


  /**
   * Oening the connection to the Server to generate the maps
   * @param xyzVals
   * @param eqkRupture
   * @param imt
   * @param metadata
   * @return
   */
  private String openConnectionToServerToGenerateShakeMap(String xyzVals,
      EqkRupture eqkRupture,String imt,String metadata){
    String webaddr=null;
    try{
      if(D) System.out.println("starting to make connection with servlet");
      URL gmtMapServlet = new
                          URL("http://gravity.usc.edu/OpenSHA/servlet/ScenarioShakeMapGeneratorServlet");


      URLConnection servletConnection = gmtMapServlet.openConnection();
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

      //sending the GMT_MapGenerattor ForShakeMaps object to the servlet
      outputToServlet.writeObject(gmtMap);

      //sending the file of the XYZ file to read the XYZ object from.
      outputToServlet.writeObject(xyzVals);

      //sending the rupture object to the servlet
      outputToServlet.writeObject(eqkRupture);

      //sending the selected IMT to the server.
      outputToServlet.writeObject(imt);

      //sending the metadata of the map to the server.
      outputToServlet.writeObject(metadata);

      outputToServlet.flush();
      outputToServlet.close();

      // Receive the "actual webaddress of all the gmt related files"
      // from the servlet after it has received all the data
      ObjectInputStream inputToServlet = new
          ObjectInputStream(servletConnection.getInputStream());

      webaddr=(String)inputToServlet.readObject();
      if(D) System.out.println("Receiving the Input from the Servlet:"+webaddr);
      inputToServlet.close();

    }catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException("Server is down , please try again later");
    }
    return webaddr;
  }



  /**
   * Oening the connection to the Server to generate the maps for Hazus
   * @param xyzVals
   * @param eqkRupture
   * @param imt
   * @param metadata
   * @return
   */
  private String[] openConnectionToServerToGenerateShakeMapForHazus(String sa_03xyzVals,
      String sa_10xyzVals,String pga_xyzVals,String pgv_xyzVals,EqkRupture eqkRupture,String metadata){
    String webaddr[]=null;
    try{
      if(D) System.out.println("starting to make connection with servlet");
      URL gmtMapServlet = new
                          URL("http://gravity.usc.edu/OpenSHA/servlet/ScenarioShakeMapForHazusGeneratorServlet");


      URLConnection servletConnection = gmtMapServlet.openConnection();
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

      //sending the GMT_MapGenerattor ForShakeMaps object to the servlet
      outputToServlet.writeObject(gmtMap);

      //sending the file of the XYZ filename for SA_03 to read the XYZ object from.
      outputToServlet.writeObject(sa_03xyzVals);

      //sending the file of the XYZ filename for SA_10 to read the XYZ object from.
      outputToServlet.writeObject(sa_10xyzVals);

      //sending the file of the XYZ filename for PGA to read the XYZ object from.
      outputToServlet.writeObject(pga_xyzVals);

      //sending the file of the XYZ filename for PGV to read the XYZ object from.
      outputToServlet.writeObject(pgv_xyzVals);


      //sending the rupture object to the servlet
      outputToServlet.writeObject(eqkRupture);


      //sending the metadata of the map to the server.
      outputToServlet.writeObject(metadata);

      outputToServlet.flush();
      outputToServlet.close();

      // Receive the "actual webaddress of all the gmt related files"
      // from the servlet after it has received all the data
      ObjectInputStream inputToServlet = new
          ObjectInputStream(servletConnection.getInputStream());

      webaddr=(String[])inputToServlet.readObject();
      if(D) System.out.println("Receiving the Input from the Servlet:"+webaddr);
      inputToServlet.close();

    }catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException("Server is down , please try again later");
    }
    return webaddr;
  }




}