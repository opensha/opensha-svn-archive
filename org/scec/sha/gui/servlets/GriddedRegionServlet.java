package org.scec.sha.gui.servlets;


import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;

import org.scec.param.ParameterList;
import org.scec.data.ArbDiscretizedXYZ_DataSet;
import org.scec.sha.gui.beans.SitesInGriddedRegionGuiBean;
import org.scec.data.region.SitesInGriddedRegion;
import org.scec.param.ParameterAPI;
import org.scec.util.FileUtils;



/**
 * <p>Title: GriddedRegionServlet </p>
 * <p>Description: This servlet creates GriddedRegion object on the server
 * and save it in a file. It returns back the absolute path to the region file.</p>
 * @author :Ned Field , Nitin Gupta and Vipin Gupta
 * @version 1.0
 */

public class GriddedRegionServlet extends HttpServlet {


  //path on the server where all the object will be stored
  private final static String FILE_PATH="/opt/install/jakarta-tomcat-4.1.24/webapps/OpenSHA/MapCalculationSavedObjects/";
  private final static String REGION_DATA_DIR ="regionObjects/" ;


  //Process the HTTP Get request
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    //gets the current time in milliseconds to be the new file for the region object file
    String regionFileName ="";
    regionFileName += System.currentTimeMillis()+".obj";

    try{
      //all the user gmt stuff will be stored in this directory
      File mainDir = new File(FILE_PATH+REGION_DATA_DIR);
      //create the main directory if it does not exist already
      if(!mainDir.isDirectory()){
        boolean success = (new File(FILE_PATH+REGION_DATA_DIR)).mkdir();
      }

      // get an input stream from the applet
      ObjectInputStream inputFromApplet = new ObjectInputStream(request.getInputStream());

      //gets the SitesIn griddedRegoinGuiBean parameterlist object from the application
      ParameterList paramList = (ParameterList)inputFromApplet.readObject();
      //gets the site params list for the selected AttenRels.
      ArrayList siteParams = (ArrayList)inputFromApplet.readObject();

      //creates a gridded Region Object
      SitesInGriddedRegion griddedRegion = setRegionFromParamList(paramList,siteParams);

      String regionFileWithAbsolutePath = FILE_PATH+REGION_DATA_DIR+regionFileName;

      //writes the gridded region object to the file
      createGriddedRegionFile(griddedRegion,regionFileWithAbsolutePath);

      // get an ouput stream from the applet
      ObjectOutputStream outputToApplet = new ObjectOutputStream(response.getOutputStream());

      //name of the image file as the URL
      outputToApplet.writeObject(regionFileWithAbsolutePath);
      outputToApplet.close();

    } catch (Exception e) {
      // report to the user whether the operation was successful or not
      e.printStackTrace();
    }
  }

  /**
   * Saves the Gridded region object in file specified by the regionFileWithAbsolutePath
   * @param griddedRegion
   * @param regionFileWithAbsolutePath
   */
  private void createGriddedRegionFile(SitesInGriddedRegion griddedRegion,String regionFileWithAbsolutePath){
    FileUtils.saveObjectInFile(regionFileWithAbsolutePath,griddedRegion);
  }


  /**
   * This function creates SitesInGriddedRegionObject and writes that object
   * to the file.
   * @param paramList
   * @param siteParams : Site related parameters
   * return the GriddedRegion Object.
   */
  private SitesInGriddedRegion setRegionFromParamList(ParameterList paramList,ArrayList siteParams){
    double minLat = ((Double)paramList.getParameter(SitesInGriddedRegionGuiBean.MIN_LATITUDE).getValue()).doubleValue();
    double maxLat = ((Double)paramList.getParameter(SitesInGriddedRegionGuiBean.MAX_LATITUDE).getValue()).doubleValue();
    double minLon = ((Double)paramList.getParameter(SitesInGriddedRegionGuiBean.MIN_LONGITUDE).getValue()).doubleValue();
    double maxLon = ((Double)paramList.getParameter(SitesInGriddedRegionGuiBean.MAX_LONGITUDE).getValue()).doubleValue();
    double gridSpacing = ((Double)paramList.getParameter(SitesInGriddedRegionGuiBean.GRID_SPACING).getValue()).doubleValue();
    SitesInGriddedRegion gridRectRegion  = new SitesInGriddedRegion(minLat,maxLat,minLon,maxLon,gridSpacing);
    String regionSitesParamVal = (String)paramList.getParameter(SitesInGriddedRegionGuiBean.SITE_PARAM_NAME).getValue();

    //adding the site params to the gridded region object
    gridRectRegion.addSiteParams(siteParams.iterator());

    if(regionSitesParamVal.equals(SitesInGriddedRegionGuiBean.SET_ALL_SITES))
      //if the site params does not need to be set from the CVM
      gridRectRegion.setSameSiteParams();

    //if the site Params needs to be set from the WILLS Site type and SCEC basin depth
    else{
      //set the site params from the CVM
      setSiteParamsFromCVM(gridRectRegion,regionSitesParamVal);
      //clone the site params with the default site param values, so that if
      //for any site we don't get the site value then apply this default value.
      ArrayList defaultSiteParams = new ArrayList();
      for(int i=0;i<siteParams.size();++i){
        ParameterAPI tempParam = (ParameterAPI)((ParameterAPI)siteParams.get(i)).clone();
        tempParam.setValue(paramList.getParameter(SitesInGriddedRegionGuiBean.DEFAULT+tempParam.getName()).getValue());
        defaultSiteParams.add(tempParam);
      }
      gridRectRegion.setDefaultSiteParams(defaultSiteParams);
    }
    return gridRectRegion;
  }


  /**
   * set the Site Params from the CVM
   */
  private void setSiteParamsFromCVM(SitesInGriddedRegion gridRectRegion,String siteParamVal){

    if(siteParamVal.equals(SitesInGriddedRegionGuiBean.SET_SITES_USING_SCEC_CVM))
      //if we are setting the each site type using Wills site type and SCEC basin depth
      gridRectRegion.setSiteParamsUsing_WILLS_VS30_AndBasinDepth();
    else if(siteParamVal.equals(SitesInGriddedRegionGuiBean.SET_SITE_USING_WILLS_SITE_TYPE))
      //if we are setting each site using the Wills site type. basin depth is taken as default.
      gridRectRegion.setSiteParamsUsing_WILLS_VS30();
  }


  //Process the HTTP Post request
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    // call the doPost method
    doGet(request,response);
  }

}