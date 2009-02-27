package org.opensha.sha.gui.beans;

import java.util.*;
import java.lang.reflect.*;
import javax.swing.*;
import java.net.*;
import java.io.*;
import java.awt.*;
import java.awt.event.*;


import org.opensha.param.*;
import org.opensha.param.editor.*;
import org.opensha.param.event.*;
import org.opensha.sha.imr.AttenuationRelationshipAPI;
import org.opensha.data.LocationList;
import org.opensha.data.Site;
import org.opensha.data.Location;
import org.opensha.data.region.*;
import org.opensha.sha.calc.hazardMap.NamedGeographicRegion;
import org.opensha.sha.gui.infoTools.CalcProgressBar;
import org.opensha.sha.util.SiteTranslator;

import org.opensha.exceptions.RegionConstraintException;

/**
 * <p>Title:SitesInGriddedRectangularRegionGuiBean </p>
 * <p>Description: This creates the Gridded Region parameter Editor with Site Params
 * for the selected Attenuation Relationship in the Application.
 * </p>
 * @author Nitin Gupta & Vipin Gupta
 * @date March 11, 2003
 * @version 1.0
 */



public class SitesInGriddedRegionGuiBean extends ParameterListEditor implements
ParameterChangeFailListener, ParameterChangeListener, Serializable {

	// for debug purposes
	protected final static String C = "SiteParamList";

	/**
	 * Latitude and longitude are added to the site attenRelImplmeters
	 */
	public final static String MIN_LONGITUDE = "Min Longitude";
	public final static String MAX_LONGITUDE = "Max Longitude";
	public final static String MIN_LATITUDE =  "Min  Latitude";
	public final static String MAX_LATITUDE =  "Max  Latitude";
	public final static String GRID_SPACING =  "Grid Spacing";
	public final static String SITE_PARAM_NAME = "Set Site Params";
	public final static String REGION_SELECT_NAME = "Region Type/Preset";
	public final static String NUM_SITES_NAME = "Number Of Sites";

	public final static String DEFAULT = "Default  ";


	// min and max limits of lat and lon for which CVM can work
	private static final double MIN_CVM_LAT = 32.0;
	private static final double MAX_CVM_LAT = 36.0;
	private static final double MIN_CVM_LON = -121.0;
	private static final double MAX_CVM_LON = -114.0;


	// title for site paramter panel
	public final static String GRIDDED_SITE_PARAMS = "Set Gridded Region Params";

	//Site Params ArrayList
	ArrayList siteParams ;

	//Static String for setting the site Params
	public final static String SET_ALL_SITES = "Apply same site parameter(s) to all locations";
	public final static String SET_SITE_USING_WILLS_SITE_TYPE = "Use the CGS Preliminary Site Conditions Map of CA (web service)";
	public final static String SET_SITES_USING_SCEC_CVM = "Use both CGS Map and SCEC Basin Depth (web services)";

	/**
	 * Longitude and Latitude paramerts to be added to the site params list
	 */
	private DoubleParameter minLon = new DoubleParameter(MIN_LONGITUDE,
			new Double(-360), new Double(360),new Double(-119.5));
	private DoubleParameter maxLon = new DoubleParameter(MAX_LONGITUDE,
			new Double(-360), new Double(360),new Double(-117));
	private DoubleParameter minLat = new DoubleParameter(MIN_LATITUDE,
			new Double(-90), new Double(90), new Double(33.5));
	private DoubleParameter maxLat = new DoubleParameter(MAX_LATITUDE,
			new Double(-90), new Double(90), new Double(34.7));
	private DoubleParameter gridSpacing = new DoubleParameter(GRID_SPACING,
			new Double(.001),new Double(1.0),new String("Degrees"),new Double(.1));
	private IntegerParameter numSites = new IntegerParameter(NUM_SITES_NAME);


	//StringParameter to set site related params
	private StringParameter siteParam;

	//SiteTranslator
	SiteTranslator siteTrans = new SiteTranslator();

	//instance of class EvenlyGriddedRectangularGeographicRegion
	private SitesInGriddedRegionAPI gridRectRegion;
	
	public final static String RECTANGULAR_NAME = "Rectangular Region";
	public final static String CUSTOM_NAME = "Custom Region";
	public final static String RELM_TESTING_NAME = "RELM Testing Region";
	public final static String RELM_COLLECTION_NAME = "RELM Collection Region";
	public final static String SO_CAL_NAME = "Southern California Region";
	public final static String NO_CAL_NAME = "Northern Caliofnia Region";
	
	ArrayList<NamedGeographicRegion> presets;
	
	private StringParameter regionSelect;
	
	public SitesInGriddedRegionGuiBean(ArrayList<NamedGeographicRegion> presets) throws RegionConstraintException {
		ArrayList<String> presetsStr = new ArrayList<String>();
		presetsStr.add(SitesInGriddedRegionGuiBean.RECTANGULAR_NAME);
//		presetsStr.add(SitesInGriddedRegionGuiBean.CUSTOM_NAME);
		for (NamedGeographicRegion preset : presets) {
			presetsStr.add(preset.getName());
		}
		
		this.presets = presets;
		
		regionSelect = new StringParameter(REGION_SELECT_NAME, presetsStr);
		regionSelect.setValue(SitesInGriddedRegionGuiBean.RECTANGULAR_NAME);
		regionSelect.addParameterChangeListener(this);
		
		//defaultVs30.setInfo(this.VS30_DEFAULT_INFO);
		//parameterList.addParameter(defaultVs30);
		minLat.addParameterChangeFailListener(this);
		minLon.addParameterChangeFailListener(this);
		maxLat.addParameterChangeFailListener(this);
		maxLon.addParameterChangeFailListener(this);
		gridSpacing.addParameterChangeFailListener(this);
		
		minLat.addParameterChangeListener(this);
		minLon.addParameterChangeListener(this);
		maxLat.addParameterChangeListener(this);
		maxLon.addParameterChangeListener(this);
		gridSpacing.addParameterChangeListener(this);

		//creating the String Param for user to select how to get the site related params
		ArrayList siteOptions = new ArrayList();
		siteOptions.add(SET_ALL_SITES);
		siteOptions.add(SET_SITE_USING_WILLS_SITE_TYPE);
		siteOptions.add(SET_SITES_USING_SCEC_CVM);
		siteParam = new StringParameter(SITE_PARAM_NAME,siteOptions,(String)siteOptions.get(0));
		siteParam.addParameterChangeListener(this);

		// add the longitude and latitude paramters
		parameterList = new ParameterList();
		parameterList.addParameter(regionSelect);
		parameterList.addParameter(minLon);
		parameterList.addParameter(maxLon);
		parameterList.addParameter(minLat);
		parameterList.addParameter(maxLat);
		parameterList.addParameter(gridSpacing);
		parameterList.addParameter(numSites);
		parameterList.addParameter(siteParam);
		
		updateNumSites();
		
		editorPanel.removeAll();
		addParameters();
		createAndUpdateSites();

		try {
			jbInit();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * constuctor which builds up mapping between IMRs and their related sites
	 */
	public SitesInGriddedRegionGuiBean() throws RegionConstraintException {
		this(generateDefaultRegions());
	}
	
	public static ArrayList<NamedGeographicRegion> generateDefaultRegions() {
		ArrayList<NamedGeographicRegion> regions = new ArrayList<NamedGeographicRegion>();
		
		regions.add(new NamedGeographicRegion(new RELM_TestingRegion().getRegionOutline(), RELM_TESTING_NAME));
		regions.add(new NamedGeographicRegion(new RELM_CollectionRegion().getRegionOutline(), RELM_COLLECTION_NAME));
		regions.add(new NamedGeographicRegion(new EvenlyGriddedSoCalRegion().getRegionOutline(), SO_CAL_NAME));
		regions.add(new NamedGeographicRegion(new EvenlyGriddedNoCalRegion().getRegionOutline(), NO_CAL_NAME));
		
		return regions;
	}


	/**
	 * This function adds the site params to the existing list.
	 * Parameters are NOT cloned.
	 * If paramter with same name already exists, then it is not added
	 *
	 * @param it : Iterator over the site params in the IMR
	 */
	public void addSiteParams(Iterator it) {
		Parameter tempParam;
		ArrayList siteTempVector= new ArrayList();
		while(it.hasNext()) {
			tempParam = (Parameter)it.next();
			if(!parameterList.containsParameter(tempParam)) { // if this does not exist already
				parameterList.addParameter(tempParam);
				//adding the parameter to the vector,
				//ArrayList is used to pass the add the site parameters to the gridded region sites.
				siteTempVector.add(tempParam);
			}
		}
		//adding the Site Params to the ArrayList, so that we can add those later if we want to.
		siteParams = siteTempVector;
		gridRectRegion.addSiteParams(siteTempVector.iterator());
		setSiteParamsVisible();
	}

	/**
	 * This function adds the site params to the existing list.
	 * Parameters are cloned.
	 * If paramter with same name already exists, then it is not added
	 *
	 * @param it : Iterator over the site params in the IMR
	 */
	public void addSiteParamsClone(Iterator it) {
		Parameter tempParam;
		ArrayList v= new ArrayList();
		while(it.hasNext()) {
			tempParam = (Parameter)it.next();
			if(!parameterList.containsParameter(tempParam)) { // if this does not exist already
				Parameter cloneParam = (Parameter)tempParam.clone();
				parameterList.addParameter(cloneParam);
				//adding the cloned parameter in the siteList.
				v.add(cloneParam);
			}
		}
		gridRectRegion.addSiteParams(v.iterator());
		setSiteParamsVisible();
	}

	/**
	 * This function removes the previous site parameters and adds as passed in iterator
	 *
	 * @param it
	 */
	public void replaceSiteParams(Iterator it) {
		// first remove all the parameters except latitude and longitude
		Iterator siteIt = parameterList.getParameterNamesIterator();
		while(siteIt.hasNext()) { // remove all the parameters except latitude and longitude and gridSpacing
			String paramName = (String)siteIt.next();
			if(!paramName.equalsIgnoreCase(MIN_LATITUDE) &&
					!paramName.equalsIgnoreCase(MIN_LONGITUDE) &&
					!paramName.equalsIgnoreCase(MAX_LATITUDE) &&
					!paramName.equalsIgnoreCase(MAX_LONGITUDE) &&
					!paramName.equalsIgnoreCase(GRID_SPACING) &&
					!paramName.equalsIgnoreCase(SITE_PARAM_NAME) &&
					!paramName.equalsIgnoreCase(REGION_SELECT_NAME) &&
					!paramName.equalsIgnoreCase(NUM_SITES_NAME))
				parameterList.removeParameter(paramName);
		}
		//removing the existing sites Params from the gridded Region sites
		gridRectRegion.removeSiteParams();

		// now add all the new params
		addSiteParams(it);
	}



	/**
	 * gets the iterator of all the sites
	 *
	 * @return
	 */
	public Iterator getAllSites() {
		return gridRectRegion.getSitesIterator();
	}


	/**
	 * get the clone of site object from the site params
	 *
	 * @return
	 */
	public Iterator getSitesClone() {
		ListIterator lIt=gridRectRegion.getGridLocationsIterator();
		ArrayList newSiteVector=new ArrayList();
		while(lIt.hasNext())
			newSiteVector.add(new Site((Location)lIt.next()));

		ListIterator it  = parameterList.getParametersIterator();
		// clone the paramters
		while(it.hasNext()){
			ParameterAPI tempParam= (ParameterAPI)it.next();
			for(int i=0;i<newSiteVector.size();++i){
				if(!((Site)newSiteVector.get(i)).containsParameter(tempParam))
					((Site)newSiteVector.get(i)).addParameter((ParameterAPI)tempParam.clone());
			}
		}
		return newSiteVector.iterator();
	}

	/**
	 * this function updates the GriddedRegion object after checking with the latest
	 * lat and lons and gridSpacing
	 * So, we update the site object as well
	 *
	 */
	private void updateGriddedSiteParams() throws
	RegionConstraintException {

		ArrayList v= new ArrayList();
		createAndUpdateSites();
		//getting the site params for the first element of the siteVector
		//becuase all the sites will be having the same site Parameter
		Iterator it = siteParams.iterator();
		while(it.hasNext())
			v.add((ParameterAPI)it.next());
		gridRectRegion.addSiteParams(v.iterator());
	}



	/**
	 * Shown when a Constraint error is thrown on a ParameterEditor
	 *
	 * @param  e  Description of the Parameter
	 */
	public void parameterChangeFailed( ParameterChangeFailEvent e ) {


		String S = C + " : parameterChangeFailed(): ";



		StringBuffer b = new StringBuffer();

		ParameterAPI param = ( ParameterAPI ) e.getSource();


		ParameterConstraintAPI constraint = param.getConstraint();
		String oldValueStr = e.getOldValue().toString();
		String badValueStr = e.getBadValue().toString();
		String name = param.getName();

		b.append( "The value ");
		b.append( badValueStr );
		b.append( " is not permitted for '");
		b.append( name );
		b.append( "'.\n" );
		b.append( "Resetting to ");
		b.append( oldValueStr );
		b.append( ". The constraints are: \n");
		b.append( constraint.toString() );

		JOptionPane.showMessageDialog(
				this, b.toString(),
				"Cannot Change Value", JOptionPane.INFORMATION_MESSAGE
		);
	}
	
	private boolean updateNumSites() {
		String name = (String)(regionSelect.getValue());
		try {
			createAndUpdateSites();
		} catch (RegionConstraintException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (gridRectRegion != null) {
			numSites.setValue(gridRectRegion.getNumGridLocs());
			LocationList locs = gridRectRegion.getRegionOutline();
			for (int i=0; i<locs.size(); i++) {
				Location loc = locs.getLocationAt(i);
				System.out.println(loc.getLatitude() + " " + loc.getLongitude());
			}
			return true;
		}
		return false;
	}

	/**
	 * This function is called when value a parameter is changed
	 * @param e Description of the parameter
	 */
	public void parameterChange(ParameterChangeEvent e){
		ParameterAPI param = ( ParameterAPI ) e.getSource();
		
		boolean update = false;
		
		if (param == regionSelect || param == minLat || param == maxLat || param == minLon || param == maxLon || param == gridSpacing) {
			update = updateNumSites();
		}

		if(param.getName().equals(SITE_PARAM_NAME))
			setSiteParamsVisible();
		else if (param == regionSelect || update) {
			String name = (String)(regionSelect.getValue());
			parameterList.clear();
			parameterList.addParameter(regionSelect);
			if (name.equals(SitesInGriddedRegionGuiBean.RECTANGULAR_NAME)) {
				parameterList.addParameter(minLon);
				parameterList.addParameter(maxLon);
				parameterList.addParameter(minLat);
				parameterList.addParameter(maxLat);
			} else if (name.equals(SitesInGriddedRegionGuiBean.CUSTOM_NAME)) {
				
			} else {
				
			}
			
			parameterList.addParameter(gridSpacing);
			parameterList.addParameter(numSites);
			parameterList.addParameter(siteParam);
			
			editorPanel.removeAll();
		    addParameters();
			editorPanel.validate();
			editorPanel.repaint();
		}
	}

	/**
	 * This method creates the gridded region with the min -max Lat and Lon
	 * It also checks if the Max Lat is less than Min Lat and
	 * Max Lat is Less than Min Lonb then it throws an exception.
	 * @return
	 */
	private void createAndUpdateSites() throws RegionConstraintException {
		if (regionSelect == null)
			System.out.println("REGION SELECT NULL");
		if (regionSelect.getValue() == null)
			System.out.println("REGION SELECT VALUE NULL");
		String name = (String)(regionSelect.getValue());
		double gridSpacingD = ((Double)gridSpacing.getValue()).doubleValue();
		if (name.equalsIgnoreCase(SitesInGriddedRegionGuiBean.RECTANGULAR_NAME)) {
			double minLatitude= ((Double)minLat.getValue()).doubleValue();
			double maxLatitude= ((Double)maxLat.getValue()).doubleValue();
			double minLongitude=((Double)minLon.getValue()).doubleValue();
			double maxLongitude=((Double)maxLon.getValue()).doubleValue();
			//checkLatLonParamValues();
			gridRectRegion= new SitesInGriddedRectangularRegion(minLatitude,
					maxLatitude,minLongitude,maxLongitude,
					gridSpacingD);
		} else {
			for (NamedGeographicRegion region : presets) {
				if (name.equals(region.getName())) {
					gridRectRegion = new SitesInGriddedRegion(region.getRegionOutline(), gridSpacingD);
					break;
				}
			}
		}

	}
	
	/**
	 * 
	 * @return boolean specifying use of Wills Site Types from the CVM in calculation
	 */
	public boolean isSiteTypeFromCVM() {
		return ((String)siteParam.getValue()).equals(SET_SITES_USING_SCEC_CVM) || ((String)siteParam.getValue()).equals(SET_SITE_USING_WILLS_SITE_TYPE);
	}
	
	/**
	 * 
	 * @return boolean specifying use of Basin Depth from the CVM in calculation
	 */
	public boolean isBasinDepthFromCVM() {
		return ((String)siteParam.getValue()).equals(SET_SITES_USING_SCEC_CVM);
	}


	/**
	 *
	 * @return the object for the SitesInGriddedRectangularRegion class
	 */
	public SitesInGriddedRegionAPI getGriddedRegionSite() throws RuntimeException, RegionConstraintException {

		updateGriddedSiteParams();
		
		return gridRectRegion;
	}


	/**
	 * Make the site params visible depending on the choice user has made to
	 * set the site Params
	 */
	private void setSiteParamsVisible(){

		//getting the Gridded Region site Object ParamList Iterator
		Iterator it = parameterList.getParametersIterator();
		//if the user decides to fill the values from the CVM
		if(((String)siteParam.getValue()).equals(SET_SITES_USING_SCEC_CVM)||
				((String)siteParam.getValue()).equals(SET_SITE_USING_WILLS_SITE_TYPE)){
			//editorPanel.getParameterEditor(this.VS30_DEFAULT).setVisible(true);
			while(it.hasNext()){
				//adds the default site Parameters becuase each site will have different site types and default value
				//has to be given if site lies outside the bounds of CVM
				ParameterAPI tempParam= (ParameterAPI)it.next();
				if(!tempParam.getName().equalsIgnoreCase(this.MAX_LATITUDE) &&
						!tempParam.getName().equalsIgnoreCase(this.MIN_LATITUDE) &&
						!tempParam.getName().equalsIgnoreCase(this.MAX_LONGITUDE) &&
						!tempParam.getName().equalsIgnoreCase(this.MIN_LONGITUDE) &&
						!tempParam.getName().equalsIgnoreCase(this.GRID_SPACING) &&
						!tempParam.getName().equalsIgnoreCase(this.SITE_PARAM_NAME) &&
						!tempParam.getName().equalsIgnoreCase(this.REGION_SELECT_NAME) &&
						!tempParam.getName().equalsIgnoreCase(this.NUM_SITES_NAME)){

					//removing the existing site Params from the List and adding the
					//new Site Param with site as being defaults
					parameterList.removeParameter(tempParam.getName());
					if(!tempParam.getName().startsWith(this.DEFAULT))
						//getting the Site Param Value corresponding to the Will Site Class "DE" for the seleted IMR  from the SiteTranslator
						siteTrans.setParameterValue(tempParam,siteTrans.WILLS_DE,Double.NaN);

					//creating the new Site Param, with "Default " added to its name, with existing site Params
					ParameterAPI newParam = (ParameterAPI)tempParam.clone();
					//If the parameterList already contains the site param with the "Default" name, then no need to change the existing name.
					if(!newParam.getName().startsWith(this.DEFAULT))
						newParam.setName(this.DEFAULT+newParam.getName());
					//making the new parameter to uneditable same as the earlier site Param, so that
					//only its value can be changed and not it properties
					newParam.setNonEditable();
					newParam.addParameterChangeFailListener(this);

					//adding the parameter to the List if not already exists
					if(!parameterList.containsParameter(newParam.getName()))
						parameterList.addParameter(newParam);
				}
			}
		}
		//if the user decides to go in with filling all the sites with the same site parameter,
		//then make that site parameter visible to te user
		else if(((String)siteParam.getValue()).equals(SET_ALL_SITES)){
			while(it.hasNext()){
				//removing the default Site Type Params if same site is to be applied to whole region
				ParameterAPI tempParam= (ParameterAPI)it.next();
				if(tempParam.getName().startsWith(this.DEFAULT))
					parameterList.removeParameter(tempParam.getName());
			}
			//Adding the Site related params to the ParameterList
			ListIterator it1 = siteParams.listIterator();
			while(it1.hasNext()){
				ParameterAPI tempParam = (ParameterAPI)it1.next();
				if(!parameterList.containsParameter(tempParam.getName()))
					parameterList.addParameter(tempParam);
			}
		}

		//creating the ParameterList Editor with the updated ParameterList
		editorPanel.removeAll();
		addParameters();
		editorPanel.validate();
		editorPanel.repaint();
		setTitle(GRIDDED_SITE_PARAMS);
	}

	/**
	 * set the Site Params from the CVM
	 */
	private void setSiteParamsFromCVM(){

		// give latitude and longitude to the servlet
		Double lonMin = (Double)parameterList.getParameter(this.MIN_LONGITUDE).getValue();
		Double lonMax = (Double)parameterList.getParameter(this.MAX_LONGITUDE).getValue();
		Double latMin = (Double)parameterList.getParameter(MIN_LATITUDE).getValue();
		Double latMax = (Double)parameterList.getParameter(MAX_LATITUDE).getValue();
		Double gridSpacing = (Double)parameterList.getParameter(GRID_SPACING).getValue();

		// if values in longitude and latitude are invalid
		if(lonMin == null || latMin == null) {
			JOptionPane.showMessageDialog(this,"Check the values in longitude and latitude");
			return ;
		}

		CalcProgressBar calcProgress = new CalcProgressBar("Setting Gridded Region sites","Getting the site paramters from the CVM");
		if(((String)siteParam.getValue()).equals(SET_SITES_USING_SCEC_CVM))
			//if we are setting the each site type using Wills site type and SCEC basin depth
			gridRectRegion.setSiteParamsForRegionFromServlet(true);
		else if(((String)siteParam.getValue()).equals(SET_SITE_USING_WILLS_SITE_TYPE))
			//if we are setting each site using the Wills site type. basin depth is taken as default.
			gridRectRegion.setSiteParamsForRegionFromServlet(false);
		calcProgress.dispose();
	}

	/**
	 * This function makes sure that Lat and Lon params are within the
	 * range and min values are not greater than max values, ie. checks
	 * if the user has filled in the correct values.
	 */
	/*private void checkLatLonParamValues() throws ParameterException{

    double minLatitude= ((Double)minLat.getValue()).doubleValue();
    double maxLatitude= ((Double)maxLat.getValue()).doubleValue();
    double minLongitude=((Double)minLon.getValue()).doubleValue();
    double maxLongitude=((Double)maxLon.getValue()).doubleValue();

    if(maxLatitude <= minLatitude){
      throw new ParameterException("Max Lat. must be greater than Min Lat");
    }

    if(maxLongitude <= minLongitude){
      throw new ParameterException("Max Lon. must be greater than Min Lon");
    }

  }*/



	/**
	 * This function creates a Region object on the server and save it there. It then
	 * returns the path to the file where that gridded object is stored.
	 * @return
	 */
	public String openConnectionToServer() throws RegionConstraintException, RuntimeException{

		//checks the values of the Lat and Lon to see if user has filled in the values correctly.
		//checkLatLonParamValues();

		try{

			if(D) System.out.println("starting to make connection with servlet");
			URL griddedRegionCalcServlet = new
			URL("http://gravity.usc.edu/OpenSHA/servlet/GriddedRegionServlet");


			URLConnection servletConnection = griddedRegionCalcServlet.openConnection();
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


			//sends the parameterList in the SitesInGriddedRectangularRegionGuiBean to the server
			outputToServlet.writeObject(parameterList);
			//sends the arraylist of site Param List to the server
			outputToServlet.writeObject(siteParams);

			outputToServlet.flush();
			outputToServlet.close();

			// Receive the "actual webaddress of all the gmt related files"
			// from the servlet after it has received all the data
			ObjectInputStream inputToServlet = new
			ObjectInputStream(servletConnection.getInputStream());

			//absolute path of the file where we have stored the file for the region object
			Object regionFilePath =(Object)inputToServlet.readObject();
			//if(D) System.out.println("Receiving the Input from the Servlet:"+webaddr);
			inputToServlet.close();

			if(regionFilePath instanceof RegionConstraintException)
				throw (RegionConstraintException)regionFilePath;
			else if(regionFilePath instanceof String)
				return (String)regionFilePath;
			else
				throw (Exception)regionFilePath;
		}
		catch(RegionConstraintException e){
			throw new RegionConstraintException(e.getMessage());
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Server is down , please try again later");
		}

	}




}
