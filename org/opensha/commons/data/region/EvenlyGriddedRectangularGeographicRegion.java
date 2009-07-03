package org.opensha.commons.data.region;

import org.opensha.commons.data.Location;
import org.opensha.commons.data.LocationList;
import org.opensha.commons.exceptions.RegionConstraintException;

import java.util.ListIterator;


/**
 * <p>Title: EvenlyGriddedRectangularGrographicRegion</p>
 * <p>Description: This creates a evenly gridded rectangular geographic region.
 * All grid points are nice values in that lat/gridSpacing and lon/gridSpacing
 * are always whole numbers.</p>
 *
 * @author : Ned Field & Nitin Gupta & Vipin Gupta
 *
 * @see EvenlyGriddedGeographicRegionAPI
 * @version 1.0
 */

public class EvenlyGriddedRectangularGeographicRegion
extends EvenlyGriddedGeographicRegion {


	//number of grid lats and lons
	private int numLatGridPoints;
	private int numLonGridPoints;

	private final static String C = "EvenlyGriddedRectangularGeographicRegion";
	private final static boolean D = false;



	/**
	 *
	 * Class constructor that accepts the minLat, maxLat,minLon,maxLon,grid spacing
	 * for creation of a EvenlyGriddedRectangularGeographicRegion.
	 * @param minLat Min Latitude for the EvenlyGridded Rectangular Region
	 * @param maxLat Max Latitude for the EvenlyGridded Rectangular Region
	 * @param minLon Min Longitude for the EvenlyGridded Rectangular Region
	 * @param maxLon Max Longitude for the EvenlyGridded Rectangular Region
	 * @param gridSpacing double GridSpacing in degrees
	 * @throws RegionConstraintException if Min Lat/Lon is greater then Max Lat/Lon.
	 *
	 */
	public EvenlyGriddedRectangularGeographicRegion(double minLat, double maxLat,
			double minLon, double maxLon,
			double gridSpacing) throws
			RegionConstraintException {
		//sets the class variable
		super.minLat = minLat;
		super.maxLat = maxLat;
		super.minLon = minLon;
		super.maxLon = maxLon;

		if (minLat > maxLat)
			throw new RegionConstraintException(
					"Min. Lat must be less then Max. Lat.\n");
		if (minLon > maxLon)
			throw new RegionConstraintException(
					"Min. Lon must be less then Max. Lon.\n");

		//creates the Location List for this rectangular region.
		locList = new LocationList();
		locList.addLocation(new Location(minLat, minLon));
		locList.addLocation(new Location(minLat, maxLon));
		locList.addLocation(new Location(maxLat, maxLon));
		locList.addLocation(new Location(maxLat, minLon));

		//sets the region min/max lat/lon aswell creates the evenly gridded
		//Rectangular Geographic Region boundary.
		createEvenlyGriddedGeographicRegion(locList,gridSpacing);

		if (D) System.out.println("numLatGridPoints=" + numLatGridPoints +
				"; numLonGridPoints=" + numLonGridPoints);
	}


	/**
	 * Class constructor that accepts the minLat, maxLat,minLon,maxLon,grid spacing
	 * and EvenlyGriddedGeographicRegionAPI,for creating the list of locations
	 * in this region from passed in EvenlyGriddedGeographicRegionAPI, for creation
	 * of a EvenlyGriddedRectangularGeographicRegion.
	 *
	 * This method is helpful as avoid creating same location more then once and just
	 * refer to the location object in the passed in EvenlyGriddedGeographicRegionAPI.
	 *
	 * This class constructor allows the user to create list of locations for this
	 * EvenlyGriddedGeographic object from passed in EvenlyGriddedGeographicRegionAPI.
	 * Please refer to EvenlyGriddedGeographicRegionAPI for more details.
	 *
	 * @param minLat Min Latitude for the EvenlyGridded Rectangular Region
	 * @param maxLat Max Latitude for the EvenlyGridded Rectangular Region
	 * @param minLon Min Longitude for the EvenlyGridded Rectangular Region
	 * @param maxLon Max Longitude for the EvenlyGridded Rectangular Region
	 * @param gridSpacing double GridSpacing in degrees
	 * @param region EvenlyGriddedGeographicRegionAPI
	 * @throws RegionConstraintException if Min Lat/Lon is greater then Max Lat/Lon
	 *
	 * @see EvenlyGriddedGeographicRegionAPI.createRegionLocationsList(EvenlyGriddedGeographicRegionAPI)
	 * @see EvenlyGriddedGeographicRegionAPI
	 */
	public EvenlyGriddedRectangularGeographicRegion(double minLat, double maxLat,
			double minLon, double maxLon,
			double gridSpacing,
			EvenlyGriddedGeographicRegionAPI
			region) throws
			RegionConstraintException{
		this(minLat, maxLat, minLon,maxLon,gridSpacing);
		createRegionLocationsList(region);
	}



	/**
	 * It samples out the grids location points based on the grid spacing(in degrees)
	 * chosen.
	 * 
	 * !! note: gridPrecision is used to replace gridSpacing
	 * to explicitly specify the precision of grid coordinates
	 * 
	 * @param degrees: sets the grid spacing
	 */
	public void setGridSpacing(double gridSpacing){
		this.gridSpacing=gridSpacing;

		//getting the nice Min Lat and Lon , so that each Lat-Lon is a perfect
		//multiple of gridSpacing
		niceMinLat = Math.ceil(minLat/gridPrecision)*gridPrecision;
		niceMinLon = Math.ceil(minLon/gridPrecision)*gridPrecision;
		niceMaxLat = Math.floor(maxLat/gridPrecision)*gridPrecision;
		niceMaxLon = Math.floor(maxLon/gridPrecision)*gridPrecision;

		numLatGridPoints = (int)Math.rint((niceMaxLat - niceMinLat)/gridSpacing)+1;
		numLonGridPoints = (int)Math.rint((niceMaxLon - niceMinLon)/gridSpacing)+1;
	}


	/**
	 *
	 * @returns the number of Grid Locations
	 */
	public int getNumGridLocs(){

		return numLatGridPoints*numLonGridPoints;
	}


	/**
	 * This method checks whether the given location is within the region.
	 * NOTE - This uses a different definition of insidedness (all boundary locations are included,
	 * whereas those on the upper and right-hand boundary are excluded in GeographicRegions).  This
	 * was changed because these edge points are included in the discretized region, and the
	 * getNearestLocationIndex(loc) method was therefore failing.
	 * @param location Location
	 * @return true if location if within the regional bounds false otherwise
	 */
	public boolean isLocationInside(Location location){
		double tempLat=location.getLatitude();
		double tempLon=location.getLongitude();

		if(D) System.out.println(C +": minLat="+minLat+"; maxLat="+maxLat+"; minLon="+minLon+"; maxLon="+maxLon);

		if((tempLat >= minLat && tempLat <= maxLat) && (tempLon >= minLon && tempLon <= maxLon))
			return true;
		return false;

	}


	/**
	 * Return the number of gridded Lats in the Rectangular region
	 * @return
	 */
	public int getNumGridLats(){
		return numLatGridPoints;
	}

	/**
	 * Return the number of gridded Lons in the Rectangular region
	 * @return
	 */
	public int getNumGridLons(){
		return numLonGridPoints;
	}

	/**
	 * Returns the Gridded Locatio at the given index.
	 * @param index (starts from zero)
	 * @returns Grid Location at the index.
	 * @see EvelyGriddedGeographicRegionAPI.getGridLocationClone(int)
	 */
	public Location getGridLocationClone(int index)  {

		//gets the row for the latitude in which that index of grid exists
		int row=index/numLonGridPoints;


		//gets the column in the row (longitude point) where that index exists
		int col=index%numLonGridPoints;
		if(row > numLatGridPoints-1 || col > numLonGridPoints-1)
			return null;

		//lat and lon for that indexed point
		double newLat=niceMinLat+row*gridSpacing;
		double newLon=niceMinLon+col*gridSpacing;

		// return new location at which that lat and lon exists
		return new Location(newLat,newLon);
	}


	/**
	 * Returns the index of the nearest location in the given gridded region, to
	 * the provided Location.
	 * @param loc Location Location to which we have to find the nearest location.
	 * @return int index of the nearest location. User can use this index to
	 * retrive the location from the locationlist.
	 * 
	 * THERE IS A BUG HERE IN THAT THIS GRIDDED REGION HAS POINTS AT THE UPPER AND RIGHT BOUNDARIES, 
	 * YET THIS METHOD WILL FILTER THOSE OUT BECAUSE THE FAIL THE isLocationInside() TEST
	 * @see EvenlyGriddedGeographicRegionAPI.getNearestLocationIndex(Location)
	 */
	public int getNearestLocationIndex(Location loc){
		//getting the Location lat and Lon

		double lat = loc.getLatitude();
		double lon = loc.getLongitude();
		//throw exception if location is outside the region lat bounds.
		if (!this.isLocationInside(loc))
			return -1;
		else { //location is inside the polygon bounds but is outside the nice min/max lat/lon
			//constraints then assign it to the nice min/max lat/lon.
			if (lat < niceMinLat)
				lat = niceMinLat;
			else if (lat > niceMaxLat)
				lat = niceMaxLat;
			if (lon < niceMinLon)
				lon = niceMinLon;
			else if (lon > niceMaxLon)
				lon = niceMaxLon;
		}

		//Considering the Locations to be store in a 2D container where lats are rows
		//and lons are the Cols.
		//getting the Row index
		int rowIndex = (int)Math.rint((lat - niceMinLat)/gridSpacing);
		//getting the Col index
		int colIndex = (int)Math.rint((lon - niceMinLon)/gridSpacing);

		//System.out.println("rowIndex:"+ rowIndex+"  colIndex:"+colIndex);

		// getting the index of the location in the region.
		int index = rowIndex*numLonGridPoints+colIndex;
		return index;
	}




	/**
	 * Creates the list of location in the gridded region and keeps it in the
	 * memory until cleared.
	 */
	protected void createGriddedLocationList(){
		double lat,lon;

		//creates a instance of new locationList
		gridLocsList=new LocationList();
		for(int iLat=0;iLat < numLatGridPoints; iLat++){
			lat = niceMinLat + gridSpacing*iLat;
			for(int iLon=0; iLon < this.numLonGridPoints; iLon++){
				lon=niceMinLon+gridSpacing*iLon;
				gridLocsList.addLocation(new Location(lat,lon));
			}
		}
	}


	/*
	 * Main method to run the this class and produce a file with
	 * EvenlyGriddedRegion locations.
	 */
	public static void main(String[] args) {

		EvenlyGriddedRectangularGeographicRegion geoReg = null;
		try {
			geoReg = new EvenlyGriddedRectangularGeographicRegion(39.0, 39.99, 120.,
					121.0, .1);
			ListIterator lit =  geoReg.getGridLocationsIterator();
			while(lit.hasNext()){
				System.out.println((Location)lit.next());
			}
		}
		catch (RegionConstraintException ex) {
		}
		geoReg.getGridLocation(0);
		Location loc = null;

		long time = System.currentTimeMillis();
		for(int i=0; i<10000;i++) {
			try {
				loc = geoReg.getGridLocation(-10);
			}
			catch (RuntimeException e) {
				continue;
			}
		}
		System.out.println("time1 = "+(System.currentTimeMillis()-time));



		/*
    System.out.println(C+": numLocations="+ geoReg.getNumRegionOutlineLocations());

    System.out.println(C+": getMinLat ="+ geoReg.getMinLat());
    System.out.println(C+": getMaxLat ="+ geoReg.getMaxLat());
    System.out.println(C+": getMinLon ="+ geoReg.getMinLon());
    System.out.println(C+": getMaxLon ="+ geoReg.getMaxLon());

    System.out.println(C+": numGridLocs="+ geoReg.getNumGridLocs());

    Location locIn=new Location(39.98, 120.5);
    Location locOut = null;

    locOut = geoReg.getNearestLocationClone(locIn);

    System.out.println("locIn  inside = "+geoReg.isLocationInside(locIn));
    System.out.println("locOut inside = "+geoReg.isLocationInside(locOut));

    System.out.println("locOut = "+locOut.toString());
		 */

		/*
    LocationList list = geoReg.getGridLocationsList();

    System.out.print("numInList="+list.size()+"; getNumGridLocs="+geoReg.getNumGridLocs()+"\n");

    Location tempLoc;
    for(int i = 0; i < geoReg.getNumGridLocs(); i++) {
      tempLoc = (Location) list.getLocationAt(i);
      try {
        System.out.print("index=" + i + "; Loc from list:  " + tempLoc.toString() +
                         "; Loc from method:  " +
                         (geoReg.getGridLocation(i)).toString() + "\n");
      }
      catch (RegionConstraintException ex1) {
        ex1.printStackTrace();
      }
    }
		 */

	}
}
