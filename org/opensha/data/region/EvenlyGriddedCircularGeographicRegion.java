package org.opensha.data.region;

import java.util.ListIterator;

import org.opensha.data.LocationList;
import org.opensha.data.Location;
import java.util.ArrayList;
import org.opensha.exceptions.RegionConstraintException;
import java.io.IOException;
import java.io.FileWriter;

/**
 * <p>Title: EvenlyGriddedCircularGeographicRegion</p>
 * <p>Description: This class creates a EvenlyGridded circular Geographical region.
 * </p>
 * <p>
 * This class has been testde by having a main method that creates all the
 * locations within EvenlyGridded Circular Geographical region. It dumps out all
 * these locations in a file called  "CircularRegionFile.txt",
 * in the sha project home directory. File format is "lat,lon,depth" on each line
 * of file. One can take this file and plot it in some kind plotting tool to see
 * if region looks like a EvenlyGriddedCircular Geographical region.
 * </p>
 * @author : Edward Field
 * @created: March 5,2003
 * @version 1.0
 */

public class EvenlyGriddedCircularGeographicRegion extends CircularGeographicRegion
                        implements EvenlyGriddedGeographicRegionAPI {

  private final static String C = "EvenlyGriddedCircularGeographicRegion";
  private final static boolean D = false;

  /**
   * class variables
   */
  private double gridSpacing;

  private LocationList gridLocsList;


  // this makes the first lat and long grid points nice in that niceMinLat/gridSpacing
  // is and integer and the point is within the polygon
  private double niceMinLat;
  private double niceMinLon ;
  //this makes the last lat and Lon grid points nice so that niceMaxLat/gridSpacing
  // is an integer
  private double niceMaxLat;
  private double niceMaxLon;


  //This array store number of locations below a given latitude
  private int[] locsBelowLat;

  //List for storing starting lon for each lat
  private double[] firstLonPerLat;


  /**
   * default constructor
   *
   * @param centerLoc - the location of the circle center
   * @param radius - radius of the region (km)
   * @param gridSpacing - grid spacing (degree)
   */
  public EvenlyGriddedCircularGeographicRegion(Location centerLoc, double radius, double gridSpacing) {
    super(centerLoc, radius);
    setGridSpacing(gridSpacing);
  }

  /**
   * It samples out the grids location points based on the grid spacing(in degrees)
   * chosen.
   * @param degrees: sets the grid spacing
   */
  public void setGridSpacing(double degrees){
    this.gridSpacing=degrees;
    if(D)
      System.out.println("gridSpacing="+gridSpacing);
    niceMinLat = Math.ceil(minLat/gridSpacing)*gridSpacing;
    niceMinLon = Math.ceil(minLon/gridSpacing)*gridSpacing;
    niceMaxLat = Math.floor(maxLat/gridSpacing)*gridSpacing;
    niceMaxLon = Math.floor(maxLon/gridSpacing)*gridSpacing;
    initLatLonArray();
  }


  /*
   * this function creates a Lon Array for each gridLat. It also creates a
   * int array which tells how many locations are there below a given lat.
   */
  protected void initLatLonArray() {
    //getting the number of grid lats in the given region
    int numLats = (int)Math.rint((niceMaxLat - niceMinLat)/gridSpacing) + 1;
    //initialising the array for storing number of location below a given lat
    //first element is 0 as first lat has 0 locations below it and last num is the
    //total number of locations.
    locsBelowLat = new int[numLats+1];


    firstLonPerLat = new double[numLats];

    int locBelowIndex = 0;
    //initializing the first element of number of locations to be 0 location for
    //min lat.
    //For each lat the number of locations keeps increasing.
    locsBelowLat[locBelowIndex++] = 0;

    //looping over all grid lats in the region to get longitudes at each lat and
    // and number of locations below each starting lat.
    for(int iLat =0;iLat <numLats;++iLat) {
      double lat = niceMinLat +iLat*gridSpacing ;
      double lon = niceMinLon;
      //List for temporarily storing all the Lons for a given lat
      ArrayList lonList  = new ArrayList();
      while (lon <= niceMaxLon) {
        //creating the location object for the lat and lon that we got
        Location loc = new Location(lat, lon);
        //checking if this location lies in the given gridded region
        //once found the first lon on the lat that lies within the region
        //save it and jump to find first lon on the next lat.
        if (this.isLocationInside(loc))
          lonList.add(new Double(lon));
        lon += gridSpacing;
      }
      //assigning number of locations below a grid lat to the grid Lat above this lat.
      locsBelowLat[locBelowIndex] = locsBelowLat[locBelowIndex - 1];
      locsBelowLat[locBelowIndex] += lonList.size();

      //just storing the first Lon for all the given grid Lats in the region.
      firstLonPerLat[locBelowIndex -1] = ((Double)lonList.get(0)).doubleValue();
      //incrementing the index counter for number of locations below a given latitude
      ++locBelowIndex;
    }
  }




  /**
   *
   * @return  the grid spacing (in degrees)
   */
  public double getGridSpacing(){
    return gridSpacing;
  }

  /**
   *
   * @returns the number of GridLocation points
   */
  public int getNumGridLocs(){
    if(gridLocsList !=null)
      return gridLocsList.size();
    else
      return locsBelowLat[locsBelowLat.length];
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
      throw new RegionConstraintException(
          "Location outside the given Gridded Region bounds");
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
      throw new RegionConstraintException(
          "Location outside the given Gridded Region bounds");
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

    //getting the lat index
    int latIndex = (int)Math.rint((lat - niceMinLat)/gridSpacing);
    //number of locations below this latitude
    int locIndex = locsBelowLat[latIndex];
    //getting the first Longitude on this Latitude
    double latFirstLon = firstLonPerLat[latIndex];

    //finding the nearest longitude to a given location longitude in the region
    //As we have checked earlier that this location is within the region bounds
    //so there must a longitude nearest to given location longitude. This
    //loop will go until location index is found. Once found it breaks out of
    //the loop.
    while(true){
      if (Math.abs(latFirstLon - lon) <= gridSpacing/2)
        break;
      latFirstLon +=gridSpacing;
      ++locIndex;
    }

    return locIndex;
  }




  /**
   * Returns the Gridded Location at a given index. If user already has the gridded
   * location list then it returns the location from this gridded location list,
   * else creates a new location object whenever user request for a location a
   * given index.
   * @param index
   * @returns the Grid Location at that index.
   */
  public Location getGridLocation(int index) throws RegionConstraintException {

    //returns  the location at the specified index in the location list
    if (gridLocsList != null)
      return gridLocsList.getLocationAt(index);
    else { // if location list has not been initialised
      //getting the size of array that maintains number of locations at each lat
      int size = locsBelowLat.length;
      int locIndex = 0;
      //iterating over all the lonsPerLat array to get the Lat index where given
      //index lies.
      int latIndex = 0;
      boolean locationFound = false;
      for (int i = 0; i < size - 1; ++i) {
        int locsIndex2 = locsBelowLat[i + 1];
        if (index < locsIndex2) {
          locIndex = locsBelowLat[i];
          latIndex = i;
          locationFound = true;
          break;
        }
      }

      if (!locationFound)throw new RegionConstraintException(
          "Not a valid index in the region");
      double firstLonForLat = firstLonPerLat[latIndex];
      //this defines the position whichlocation has to be retrieved for the lat
      int lonIndex = index - locIndex;
      //getting the lon on the given location index, this is retrieved by using
      //first Lon on a given Lat and then getting the lon at given index on that lat index.
      double lon = firstLonForLat+ lonIndex * gridSpacing;
      //getting the lat for the given location index
      double lat = niceMinLat + latIndex * gridSpacing;
      return new Location(lat, lon);
    }
  }


  /**
   * Creates the locationlist from the
   */
  private void createGriddedLocationList() {

    //creates a instance of new locationList
    gridLocsList = new LocationList();
    //Length of Array that stores the locations below a given gridLat
    int locBelowLatSize = locsBelowLat.length;
    //initialising the lat with the nice min lat
    double lat = niceMinLat;
    //iterating over all lons for each lat, and creating a Location list from it.
    for (int i = 0; i < locBelowLatSize-1; ++i) {
      double firstLonForLat =  firstLonPerLat[i];
      //number of grid Lons for a given Lat
      int numLonsForLat = locsBelowLat[i+1] - locsBelowLat[i];
      for (int j = 0; j < numLonsForLat; ++j) {
        //getting each longitude for a given lat
        double lon = firstLonForLat + j*gridSpacing;
        //creating a new location
        Location loc = new Location(lat, lon);
        gridLocsList.addLocation(loc);
      }
      //getting the next grid lat.
      lat += gridSpacing;
    }
  }

  public static void main(String[] args) {
    EvenlyGriddedCircularGeographicRegion gridReg = new EvenlyGriddedCircularGeographicRegion(new Location(34,-122,0),111,0.02);
    try {
      FileWriter fw = new FileWriter("CircularRegionFile.txt");
      ListIterator it = gridReg.getGridLocationsIterator();
      while(it.hasNext()){
        Location loc = (Location)it.next();
        fw.write(loc.toString()+"\n");
      }
      fw.close();
    }
    catch (IOException ex) {
      ex.printStackTrace();
    }

  }

  /**
   * Returns the minimum Lat so that this gridLat/gridSpacing is an int,
   * and this min Lat is within the polygon;
   * @return double
   */
  public double getMinGridLat(){
    return niceMinLat;
  }


  /**
   * Returns the maximum Lat so that this gridLat/gridSpacing is an int,
   * and this max Lat is within the polygon;
   * @return double
   */
  public double getMaxGridLat(){
    return niceMaxLat;
  }

  /**
   * Returns the minimum Lon so that this gridLon/gridSpacing is an int,
   * and this min Lon is within the polygon;
   * @return double
   */
  public double getMinGridLon(){
    return niceMinLon;
  }

  /**
   * Returns the maximum Lon so that this gridLon/gridSpacing is an int,
   * and this max Lon is within the polygon;
   * @return double
   */
  public double getMaxGridLon(){
    return niceMaxLon ;
  }


}
