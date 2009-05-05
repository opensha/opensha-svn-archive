package org.opensha.sha.gui.servlets;


import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;

import org.opensha.param.ParameterList;
import org.opensha.data.ArbDiscretizedXYZ_DataSet;
import org.opensha.sha.gui.beans.SitesInGriddedRectangularRegionGuiBean;
import org.opensha.data.region.SitesInGriddedRectangularRegion;
import org.opensha.data.siteType.SiteDataValueList;
import org.opensha.param.ParameterAPI;
import org.opensha.util.FileUtils;
import org.opensha.exceptions.RegionConstraintException;

/**
 * <p>Title: GriddedRegionServlet </p>
 * <p>Description: This servlet creates GriddedRegion object on the server
 * and save it in a file. It returns back the absolute path to the region file.</p>
 * @author :Ned Field , Nitin Gupta and Vipin Gupta
 * @version 1.0
 */

public class GriddedRegionServlet extends HttpServlet {


	//path on the server where all the object will be stored
	private final static String FILE_PATH="/opt/install/apache-tomcat-5.5.20/webapps/OpenSHA/MapCalculationSavedObjects/";
	private final static String REGION_DATA_DIR ="regionObjects/" ;


	//Process the HTTP Get request
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		//gets the current time in milliseconds to be the new file for the region object file
		String regionFileName ="";
		regionFileName += System.currentTimeMillis()+".obj";

		// get an ouput stream from the applet
		ObjectOutputStream outputToApplet = new ObjectOutputStream(response.getOutputStream());
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

			ArrayList<SiteDataValueList<?>> dataVals = (ArrayList<SiteDataValueList<?>>)inputFromApplet.readObject();

			//creates a gridded Region Object
			SitesInGriddedRectangularRegion griddedRegion = setRegionFromParamList(paramList,siteParams,dataVals);

			String regionFileWithAbsolutePath = FILE_PATH+REGION_DATA_DIR+regionFileName;

			//writes the gridded region object to the file
			createGriddedRegionFile(griddedRegion,regionFileWithAbsolutePath);



			//name of the image file as the URL
			outputToApplet.writeObject(regionFileWithAbsolutePath);
			outputToApplet.close();

		}catch(RegionConstraintException e){
			e.printStackTrace();
			outputToApplet.writeObject(e);
			outputToApplet.close();
		}
		catch (Exception e) {
			e.printStackTrace();
			outputToApplet.writeObject(e);
			outputToApplet.close();
		}
	}

	/**
	 * Saves the Gridded region object in file specified by the regionFileWithAbsolutePath
	 * @param griddedRegion
	 * @param regionFileWithAbsolutePath
	 */
	private void createGriddedRegionFile(SitesInGriddedRectangularRegion griddedRegion,String regionFileWithAbsolutePath){
		FileUtils.saveObjectInFile(regionFileWithAbsolutePath,griddedRegion);
	}


	/**
	 * This function creates SitesInGriddedRectangularRegionObject and writes that object
	 * to the file.
	 * @param paramList
	 * @param siteParams : Site related parameters
	 * return the GriddedRegion Object.
	 * @param dataVals 
	 */
	private SitesInGriddedRectangularRegion setRegionFromParamList(ParameterList paramList,ArrayList siteParams, ArrayList<SiteDataValueList<?>> dataVals) throws RegionConstraintException {
		double minLat = ((Double)paramList.getParameter(SitesInGriddedRectangularRegionGuiBean.MIN_LATITUDE).getValue()).doubleValue();
		double maxLat = ((Double)paramList.getParameter(SitesInGriddedRectangularRegionGuiBean.MAX_LATITUDE).getValue()).doubleValue();
		double minLon = ((Double)paramList.getParameter(SitesInGriddedRectangularRegionGuiBean.MIN_LONGITUDE).getValue()).doubleValue();
		double maxLon = ((Double)paramList.getParameter(SitesInGriddedRectangularRegionGuiBean.MAX_LONGITUDE).getValue()).doubleValue();
		double gridSpacing = ((Double)paramList.getParameter(SitesInGriddedRectangularRegionGuiBean.GRID_SPACING).getValue()).doubleValue();
		SitesInGriddedRectangularRegion gridRectRegion  = new SitesInGriddedRectangularRegion(minLat,maxLat,minLon,maxLon,gridSpacing);
		String regionSitesParamVal = (String)paramList.getParameter(SitesInGriddedRectangularRegionGuiBean.SITE_PARAM_NAME).getValue();

		//adding the site params to the gridded region object
		gridRectRegion.addSiteParams(siteParams.iterator());

		if(regionSitesParamVal.equals(SitesInGriddedRectangularRegionGuiBean.SET_ALL_SITES))
			//if the site params does not need to be set from the CVM
			gridRectRegion.setSameSiteParams();

		//if the site Params needs to be set from the WILLS Site type and SCEC basin depth
		else{
			//set the site params from the CVM
			setSiteParamsFromCVM(gridRectRegion,regionSitesParamVal,dataVals);
			//clone the site params with the default site param values, so that if
			//for any site we don't get the site value then apply this default value.
			ArrayList defaultSiteParams = new ArrayList();
			for(int i=0;i<siteParams.size();++i){
				ParameterAPI tempParam = (ParameterAPI)((ParameterAPI)siteParams.get(i)).clone();
				tempParam.setValue(paramList.getParameter(SitesInGriddedRectangularRegionGuiBean.DEFAULT+tempParam.getName()).getValue());
				defaultSiteParams.add(tempParam);
			}
			gridRectRegion.setDefaultSiteParams(defaultSiteParams);
		}
		return gridRectRegion;
	}


	/**
	 * set the Site Params from the CVM
	 * @param dataVals 
	 */
	private void setSiteParamsFromCVM(SitesInGriddedRectangularRegion gridRectRegion,String siteParamVal, ArrayList<SiteDataValueList<?>> dataVals){

		if(siteParamVal.equals(SitesInGriddedRectangularRegionGuiBean.SET_SITES_USING_SCEC_CVM)) {
			System.out.println("USING CVM");
			//if we are setting the each site type using Wills site type and SCEC basin depth
			gridRectRegion.setSiteParamsForRegionFromServlet(true);
		} else if(siteParamVal.equals(SitesInGriddedRectangularRegionGuiBean.SET_SITE_USING_WILLS_SITE_TYPE)) {
			System.out.println("USING Wills");
			//if we are setting each site using the Wills site type. basin depth is taken as default.
			gridRectRegion.setSiteParamsForRegionFromServlet(false);
		} else {
			System.out.println("USING All");
			gridRectRegion.setSiteDataValueLists(dataVals);
		}
	}


	//Process the HTTP Post request
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// call the doPost method
		doGet(request,response);
	}

}
