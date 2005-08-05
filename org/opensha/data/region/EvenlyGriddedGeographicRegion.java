package org.opensha.data.region;

import java.util.*;

import org.opensha.data.LocationList;
import org.opensha.data.Location;
import org.opensha.exceptions.RegionConstraintException;
import java.io.IOException;
import java.io.FileWriter;

/**
 * <p>Title: EvenlyGriddedGeographicRegion</p>
 * <p>Description: This class creates a EvenlyGridded Geographical region.
 * </p>
 * <p>
 * It accepts list of locations that will constitute the boundary around this
 * geographical region.
 * </p>
 * <p>
 * This class has been tested by having a main method that creates all the
 * locations within EvenlyGridded Geographical region. It dumps out all
 * these locations in a file called  "SausageRegionFile.txt",
 * in the sha project home directory. File format is "lat,lon,depth" on each line
 * of file. One can take this file and plot it in some kind plotting tool to see
 * if region looks like a EvenlyGridded Geographical region for the locations
 * provided by the user.
 * </p>
 * @author : Nitin Gupta & Vipin Gupta
 * @created: March 5,2003
 * @version 1.0
 */

public class EvenlyGriddedGeographicRegion
    extends GeographicRegion implements EvenlyGriddedGeographicRegionAPI {

  private final static String C = "EvenlyGriddedGeographicRegion";
  private final static boolean D = false;

  private double gridSpacing;

  // this makes the first lat and lon grid points nice in that niceMinLat/gridSpacing
  // is an integer and the point is within the polygon
  private double niceMinLat;
  private double niceMinLon;

  //this makes the last lat and Lon grid points nice so that niceMaxLat/gridSpacing
  // is an integer
  private double niceMaxLat;
  private double niceMaxLon;

  //list of of location in the given region
  private LocationList gridLocsList;

  //This array store number of locations below a given latitude
  private int[] locsBelowLat;

  //List for storing each for a given latitude
  private ArrayList lonsPerLatList;

  /**
   * default class constructor
   */
  public EvenlyGriddedGeographicRegion(LocationList locList, double gridSpacing) {
    super(locList);
    setGridSpacing(gridSpacing);

    //this function creates a Lon Array for each gridLat. It also creates a
    //int array which tells how many locations are there below a given lat
    initLatLonArray();
  }

  /*
   * this function creates a Lon Array for each gridLat. It also creates a
   * int array which tells how many locations are there below a given lat
   */
  protected void initLatLonArray() {
    //getting the number of grid lats in the given region
    int numLats = (int) Math.rint((niceMaxLat - niceMinLat) / gridSpacing) + 1;
    //initialising the array for storing number of location below a given lat
    //first element is 0 as first lat has 0 locations below it and last num is the
    //total number of locations.
    locsBelowLat = new int[numLats+1];

    lonsPerLatList = new ArrayList();

    int locBelowIndex = 0;
    //initializing the first element of number of locations to be 0 location for
    //min lat.
    //For each lat the number of locations keeps increasing.
    locsBelowLat[locBelowIndex++] = 0;
    //looping over all grid lats in the region to get longitudes at each lat and
    // and number of locations below each starting lat.
    for(int iLat = 0;iLat<numLats;++iLat) {
      double lat = niceMinLat + iLat*gridSpacing;
      double lon = minLon;
      ArrayList lonList = new ArrayList();
      while (lon <= niceMaxLon) {
        //creating the location object for the lat and lon that we got
        Location loc = new Location(lat, lon);
        //checking if this location lies in the given gridded region
        if (this.isLocationInside(loc))
          lonList.add(new Double(lon));
        lon += gridSpacing;
      }
      //assigning number of locations below a grid lat to the grid Lat above this lat.
      locsBelowLat[locBelowIndex] = locsBelowLat[locBelowIndex - 1];
      locsBelowLat[locBelowIndex] += lonList.size();
      lonsPerLatList.add(lonList);
      //incrementing the index counter for number of locations below a given latitude
      ++locBelowIndex;
    }
  }

  /**
   * It samples out the grids location points based on the grid spacing(in degrees)
   * chosen.
   * @param degrees: sets the grid spacing
   */
  public void setGridSpacing(double degrees) {
    gridSpacing = degrees;
    niceMinLat = Math.ceil(minLat / gridSpacing) * gridSpacing;
    niceMinLon = Math.ceil(minLon / gridSpacing) * gridSpacing;
    niceMaxLat = Math.floor(maxLat / gridSpacing) * gridSpacing;
    niceMaxLon = Math.floor(maxLon / gridSpacing) * gridSpacing;
  }

  /**
   *
   * @return  the grid spacing (in degrees)
   */
  public double getGridSpacing() {
    return gridSpacing;
  }

  /**
   *
   * @returns the number of GridLocation points
   */
  public int getNumGridLocs() {
    if(gridLocsList !=null)
      return gridLocsList.size();
    else
      return locsBelowLat[locsBelowLat.length - 1];
  }

  /**
   *
   * @returns the Grid Locations Iterator.
   */
  public ListIterator getGridLocationsIterator() {
    if (gridLocsList == null)
      createGriddedLocationList();
    //return the ListIterator for the locationList
    return gridLocsList.listIterator();
  }

  /**
   *
   * @returns the GridLocations List
   */
  public LocationList getGridLocationsList() {
    if (gridLocsList == null)
      createGriddedLocationList();
    return gridLocsList;
  }

  /**
   * Returns the Gridded Location at a given index. If user has already the gridded
   * location list then it returns the location from this gridded location list,
   * else creates a new location object whenever user request for a location a
   * given index.
   * @param index
   * @returns the Grid Location at that index.
   */
  public Location getGridLocation(int index) throws RegionConstraintException {

    //returns  the location at the specified index in the location list
    if(gridLocsList !=null)
      return gridLocsList.getLocationAt(index);
    else{// if location list has not been initialised
      //getting the size of array that maintains number of locations at each lat
      int size = locsBelowLat.length;
      int locIndex = 0;
      //iterating over all the lonsPerLat array to get the Lat index where given
      //index lies.
      int latIndex =0;
      boolean locationFound = false;
      for(int i=0;i<size-1;++i){
        int locsIndex2 = locsBelowLat[i + 1];
        if(index < locsIndex2){
          locIndex = locsBelowLat[i];
          latIndex = i;
          locationFound = true;
          break;
        }
      }

      if(!locationFound) throw new RegionConstraintException("Not a valid index in the region");
      ArrayList lonList = (ArrayList)lonsPerLatList.get(latIndex);
      double lon = ((Double)lonList.get(index - locIndex)).doubleValue();
      double lat = niceMinLat+latIndex*gridSpacing;
      return new Location(lat,lon);
    }

  }

  /**
   * Returns the nearest location in the gridded region to the provided Location.
   * @param loc Location Location to which we have to find the nearest location.
   * @return Location Nearest Location
   */
  public Location getNearestLocation(Location loc) throws RegionConstraintException{
    //Getting the nearest Location to the rupture point location


    //getting the nearest Latitude.
    double lat = Math.rint(loc.getLatitude() / gridSpacing) *
        gridSpacing;
    //getting the nearest Longitude.
    double lon = Math.rint(loc.getLongitude() / gridSpacing) *
        gridSpacing;

    //throw exception if location is outside the region lat bounds.
    if (!this.isLocationInside(loc))
      throw new RegionConstraintException("Location outside the given Gridded Region bounds");

    lat = Double.parseDouble(EvenlyGriddedGeographicRegionAPI.latLonFormat.
                             format(lat));
    lon = Double.parseDouble(EvenlyGriddedGeographicRegionAPI.latLonFormat.
                             format(lon));
    return new Location(lat, lon);
  }

  /**
   * Returns the index of the nearest location in the given gridded region, to
   * the provided Location.
   * @param loc Location Location to which we have to find the nearest location.
   * @return int
   */
  public int getNearestLocationIndex(Location loc) throws
      RegionConstraintException {

    double lat = loc.getLatitude();
    double lon = loc.getLongitude();

    //throw exception if location is outside the region lat bounds.
    if (!this.isLocationInside(loc))
      throw new RegionConstraintException("Location outside the given Gridded Region bounds");


    //getting the lat index
    int latIndex = (int)Math.rint((lat - niceMinLat)/gridSpacing);
    //number of locations below this latitude
    int locIndex = locsBelowLat[latIndex];
    ArrayList lonList = (ArrayList)lonsPerLatList.get(latIndex);

    int size = lonList.size();
    //iterating over all the lons for a given lat and finding the lon to the given lon.
    for(int i=0;i<size;++i){
      double latLon = ((Double)lonList.get(i)).doubleValue();
      if (Math.abs(latLon - lon) <= gridSpacing/2) {
        locIndex += i;
        break;
      }
    }

    return locIndex;
  }

  /*
   * Creates tha list of the location for the Gridded Geographic Region.
   */
  /*private void createGriddedLocationList(){
    double minLat=getMinLat();
    double maxLat=getMaxLat();
    double minLon=getMinLon();
    double maxLon=getMaxLon();


    Location tempLoc;

    //creates a instance of new locationList
    gridLocsList=new LocationList();

    // now loop over all grid points inside the max/min lat/lon and keep only those inside
    minLat = niceMinLat;

    while(minLat <= maxLat){
      minLon = niceMinLon;
      while(minLon <= maxLon){
        tempLoc = new Location(minLat,minLon);
        if (this.isLocationInside(tempLoc)) gridLocsList.addLocation(tempLoc);
        minLon+=gridSpacing;
      }
      minLat+=gridSpacing;
    }

    int i;
    if(D)
      for(i = 0; i < gridLocsList.size(); i++)
        System.out.println((float)gridLocsList.getLocationAt(i).getLatitude()+"  "+(float)gridLocsList.getLocationAt(i).getLongitude());

     }*/


  /**
   * Creates the locationlist from the
   */
  private void createGriddedLocationList() {

    //creates a instance of new locationList
    gridLocsList = new LocationList();
    //number of gridLats
    int lonsPerLatSize = lonsPerLatList.size();
    //initialising the lat with the nice min lat
    double lat = niceMinLat;
    //iterating over all lons for each lat, and creating a Location list from it.
    for (int i = 0; i < lonsPerLatSize; ++i) {
      ArrayList lonList = (ArrayList) lonsPerLatList.get(i);
      int numLons = lonList.size();
      for (int j = 0; j < numLons; ++j) {
        double lon = ( (Double) lonList.get(j)).doubleValue();
        //creating a new location
        Location loc = new Location(lat, lon);
        gridLocsList.addLocation(loc);
      }
      //getting the next grid lat.
      lat += gridSpacing;
    }
  }

  /**
   * Returns the minimum Lat so that this gridLat/gridSpacing is an int,
   * and this min Lat is within the polygon;
   * @return double
   */
  public double getMinGridLat() {
    return niceMinLat;
  }

  /**
   * Returns the maximum Lat so that this gridLat/gridSpacing is an int,
   * and this max Lat is within the polygon;
   * @return double
   */
  public double getMaxGridLat() {
    return niceMaxLat;
  }

  /**
   * Returns the minimum Lon so that this gridLon/gridSpacing is an int,
   * and this min Lon is within the polygon;
   * @return double
   */
  public double getMinGridLon() {
    return niceMinLon;
  }

  /**
   * Returns the maximum Lon so that this gridLon/gridSpacing is an int,
   * and this max Lon is within the polygon;
   * @return double
   */
  public double getMaxGridLon() {
    return niceMaxLon;
  }

  public static void main(String[] args) {
    LocationList locList = new LocationList();
    locList.addLocation(new Location(37.19, -120.61, 0.0));
    locList.addLocation(new Location(36.43, -122.09, 0.0));
    locList.addLocation(new Location(38.23, -123.61, 0.0));
    locList.addLocation(new Location(39.02, -122.08, 0.0));
    EvenlyGriddedGeographicRegion gridReg = new EvenlyGriddedGeographicRegion(
        locList, 0.05);
      try {
        FileWriter fw = new FileWriter("GeoRegionFile.txt");
        ListIterator it = gridReg.getGridLocationsIterator();
        while (it.hasNext()) {
          Location loc = (Location) it.next();
          fw.write(loc.toString() +"\n");
        }
        fw.close();
      }
      catch (IOException ex) {
        ex.printStackTrace();
      }

  }

}
