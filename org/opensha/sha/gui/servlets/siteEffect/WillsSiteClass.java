package org.opensha.sha.gui.servlets.siteEffect;

import java.util.*;
import java.io.*;

import org.opensha.commons.data.Location;
import org.opensha.commons.data.LocationList;
import org.opensha.commons.data.region.EvenlyGriddedRectangularGeographicRegion;
import org.opensha.data.region.*;
import org.opensha.data.*;
import org.opensha.exceptions.RegionConstraintException;

/**
 * <p>Title: WillsSiteClass</p>
 * <p>Description:This class creates a gridded region from the given lat, lons
 * and gridSpacing. Then for each site in the gridded region gets the  Wills Site
 * Class values for each site in the region</p>
 * @author : Nitin Gupta
 * @created Feb 4,2004
 * @version 1.0
 */

public final class WillsSiteClass {

	public static final String WILLS_FILE = "data/siteType/Wills2000/usgs_cgs_geology_60s_mod.txt";


	//ArrayList for computing the lat and lons for the given gridded region
	ArrayList<Location> locations ;
	String willsSiteClassFile;
	boolean loadFromJar = false;
	//gridSpacing for the VS30 file and adding a small value to it.
	//  public final static double GRID_SPACING_FOR_VS30_IN_FILE= .0171;
	public final static double GRID_SPACING_FOR_VS30_IN_FILE= .0166;

	private Location lastLocation = null;

	/**
	 * Class constructor
	 * @param minLon
	 * @param maxLon
	 * @param minLat
	 * @param maxLat
	 * @param gridSpacing
	 * @param fileName : Name of the Wills Site Class file
	 */
	public WillsSiteClass(double minLon, double maxLon, double minLat,
			double maxLat, double gridSpacing,String fileName) throws
			RegionConstraintException {

		prepareSitesInput(minLon,maxLon,minLat,maxLat,gridSpacing);
		willsSiteClassFile = fileName;
	}


	/**
	 *
	 * @param locList : List of Locations
	 * @param fileName : Name of the Wills Site Class file
	 */
	public WillsSiteClass(LocationList locList,String fileName){
		int numLocations = locList.size();
		willsSiteClassFile = fileName;
		locations = new ArrayList<Location>();
		for(int i=0;i<numLocations;++i)
			locations.add(locList.getLocationAt(i));
	}


	/**
	 * Prepare the input of the all the location in the gridded region and provide that input
	 * to compute the Wills Site Class values for each region.
	 * @param minLon
	 * @param maxLon
	 * @param minLat
	 * @param maxLat
	 * @param gridSpacing
	 * @return
	 */
	private void prepareSitesInput(double minLon, double maxLon, double minLat,
			double maxLat, double gridSpacing) throws
			RegionConstraintException {

		locations = new ArrayList<Location>();
		EvenlyGriddedRectangularGeographicRegion region = new EvenlyGriddedRectangularGeographicRegion(minLat,maxLat,minLon,maxLon,gridSpacing);
		//System.out.println("Number of Grid Locations:"+region.getNumGridLocs());
		for (Location loc : region.getGridLocationsList())
			locations.add(loc);
	}

	public void setLoadFromJar(boolean jar) {
		loadFromJar = jar;
	}

	public Location getLastLocation() {
		return lastLocation;
	}

	/**
	 *
	 * @returns the ArrayList of the Wills Site Class Value for each site in the
	 * gridded region.
	 */
	public ArrayList<String> getWillsSiteClass() {


		try {

			//open the File Input Stream to read the file
			InputStreamReader input;
			if (loadFromJar)
				input = new InputStreamReader(this.getClass().getResourceAsStream(willsSiteClassFile));
			else
				input = new FileReader(willsSiteClassFile);
			BufferedReader iBuf= new BufferedReader(input);
			String str;
			// parsing the file line by line
			//reading the first line from the file
			str=iBuf.readLine();

			int size= locations.size();

			ArrayList<String> vs30= new ArrayList<String>();

			//initializing the vs30 vector with the Double.NaN values
			for(int i=0;i<size;++i)
				vs30.add("NA");

			for(int i=0;i<size;++i){
				double lat = ((Location)locations.get(i)).getLatitude();
				double lon = ((Location)locations.get(i)).getLongitude();
				boolean latFlag= false;

				//parse each line from the file one by one
				while(str!=null) {
					StringTokenizer st = new StringTokenizer(str);

					//reading the Lat from the file
					double valLat = Double.parseDouble(st.nextToken());

					//reading the Lons from the file
					double valLon = Double.parseDouble(st.nextToken());

					if((valLat -lat) > GRID_SPACING_FOR_VS30_IN_FILE/2)
						// if this lat does not exist in file. Lat is always increasing in the file and the location vector
						break;

					// add Vs30 for new location
					if(Math.abs(lat-valLat) <= (GRID_SPACING_FOR_VS30_IN_FILE/2))
						latFlag=true;

					//iterating over lon's for each lat
					if(((Math.abs(lon-valLon)) <= GRID_SPACING_FOR_VS30_IN_FILE/2) && latFlag){
						//if we found the desired lon in the file,
						//we get the value of the VS30 for the nearest point
						//returns the site type based on the Will's classification for Site type.
						vs30.set(i,st.nextToken());
						if (i == size - 1) {
							// this is the last one
							lastLocation = new Location(valLat, valLon);
						}
						break;

					}

					//this condition checks if the lat exists but lon does not exist
					if((valLon-lon) > (GRID_SPACING_FOR_VS30_IN_FILE/2 ) && latFlag)
						// if this location does not exist in this file
						break;

					// read next line
					str=iBuf.readLine();
				}
			}
			//returns the ArrayList containg the Wills Site Class vlaues for each gridded site
			return vs30;
		}catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}


}
