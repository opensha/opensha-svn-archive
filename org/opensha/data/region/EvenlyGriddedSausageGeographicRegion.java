package org.opensha.data.region;

import java.util.ListIterator;

import org.opensha.data.LocationList;
import org.opensha.data.Location;
import java.util.ArrayList;
import org.opensha.exceptions.RegionConstraintException;
import org.opensha.data.Direction;
import org.opensha.calc.RelativeLocation;
import java.io.FileWriter;
import java.io.*;

/**
 * <p>Title: EvenlyGriddedCircularGeographicRegion</p>
 * <p>Description: </p>
 * @author : Edward Field
 * @created: March 5,2003
 * @version 1.0
 */

public class EvenlyGriddedSausageGeographicRegion extends GeographicRegion
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

  //List for storing each for a given latitude
  private ArrayList lonsPerLatList;


  //gets the locations where min/max lat lons are found in the case of sausage region.
  //these are the corresponding locaiton lat/lon for the locations provided by the
  //user in which we found the min/max lat/lon.
  private double minLatLon, maxLatLon, minLonLat, maxLonLat;

  //Sausage Region Min/Max lat/lon
  private double regionMinLat, regionMaxLat,regionMinLon,regionMaxLon;


  //Radius of the Sausage region
  private double radius;



  /**
   *
   * @param locList LocationList : Locations of the end points on the lines
   * @param radius double
   * @param gridSpacing double
   */
  public EvenlyGriddedSausageGeographicRegion(LocationList locList, double radius, double gridSpacing) {
    this.locList = locList;
    setMinMaxLatLon();
    this.radius = radius;
    setGridSpacing(gridSpacing);
  }



  /**
   * this method finds the minLat,maxLat,minLon and maxLon.
   */
  protected void setMinMaxLatLon(){
    ListIterator it=locList.listIterator();
    Location l = (Location) it.next();
    minLat=l.getLatitude();
    minLon=l.getLongitude();
    maxLat=l.getLatitude();
    maxLon=l.getLongitude();
    //in addition to getting the min/max lat/Lon it gets the locations for these.
    while(it.hasNext()){
      l=(Location)it.next();
      if(l.getLatitude()< minLat){
        minLat = l.getLatitude();
        minLatLon = l.getLongitude();
      }
      if(l.getLatitude()> maxLat){
        maxLat = l.getLatitude();
        maxLatLon = l.getLongitude();
      }
      if(l.getLongitude()<minLon){
        minLon = l.getLongitude();
        minLonLat = l.getLatitude();
      }
      if(l.getLongitude()>maxLon){
        maxLon = l.getLongitude();
        maxLonLat = l.getLatitude();
      }
    }

    if(D) System.out.println(C +": minLat="+minLat+"; maxLat="+maxLat+"; minLon="+minLon+"; maxLon="+maxLon);
  }




  /*
   * This function creates the Gridded Rectangular Region Outline
   */
  private void createEvenlyGriddedRectangularRegionOutline(){
    //location that finds the min lat for the rectangular region
    Location minLatLoc = new Location(niceMinLat,minLatLon);
    //set min and max lat and lons
    Direction dir = new Direction(0, radius, 180, 0);
    Location tempLoc = RelativeLocation.getLocation(minLatLoc, dir);
    regionMinLat = tempLoc.getLatitude();

    //location that finds the max lat for the rectangular region
    Location maxLatLoc = new Location(niceMaxLat,maxLatLon);
    dir = new Direction(0, radius,0, 180);
    tempLoc = RelativeLocation.getLocation(maxLatLoc, dir);
    regionMaxLat = tempLoc.getLatitude();

    //location that finds the min lon for the rectangular region
    Location minLonLoc = new Location(minLonLat, niceMinLon);
    dir = new Direction(0, radius,270,90);
    tempLoc = RelativeLocation.getLocation(minLonLoc, dir);
    regionMinLon = tempLoc.getLongitude();

    //location that finds the max lon for the rectangular region
    Location maxLonLoc = new Location(maxLonLat, niceMaxLon);
    dir = new Direction(0, radius,90, 270);
    tempLoc = RelativeLocation.getLocation(maxLonLoc, dir);
    regionMaxLon = tempLoc.getLongitude();

    initLatLonArray();
  }

  /**
   * It samples out the grids location points based on the grid spacing(in degrees)
   * chosen.
   * @param degrees: sets the grid spacing
   */
  public void setGridSpacing(double gridSpacing){
    this.gridSpacing=gridSpacing;
    if(D)
      System.out.println("gridSpacing="+gridSpacing);
    niceMinLat = Math.ceil(minLat/gridSpacing)*gridSpacing;
    niceMinLon = Math.ceil(minLon/gridSpacing)*gridSpacing;
    niceMaxLat = Math.floor(maxLat/gridSpacing)*gridSpacing;
    niceMaxLon = Math.floor(maxLon/gridSpacing)*gridSpacing;

    //getting the nice Lat/Lon for the locations at min/max lat/lon.
    minLatLon = Math.ceil(minLatLon/gridSpacing)*gridSpacing;
    maxLatLon = Math.floor(maxLatLon/gridSpacing)*gridSpacing;
    minLonLat = Math.ceil(minLonLat/gridSpacing)*gridSpacing;
    maxLonLat = Math.floor(maxLonLat/gridSpacing)*gridSpacing;
    createEvenlyGriddedRectangularRegionOutline();
  }


  /*
   * this function creates a Lon Array for each gridLat. It also creates a
   * int array which tells how many locations are there below a given lat.
   */
  protected void initLatLonArray() {
    //number of Grid Lat/Lons that can be in the Gridded Rectangular Region
    int numLatGridPoints = (int) Math.ceil( (regionMaxLat - regionMinLat) /
                                           gridSpacing) + 1;
    int numLonGridPoints = (int) Math.ceil( (regionMaxLon - regionMinLon) /
                                           gridSpacing) + 1;

    //initialising the array for storing number of location below a given lat
    //first element is 0 as first lat has 0 locations below it and last num is the
    //total number of locations.
    locsBelowLat = new int[numLatGridPoints+1];
    //ArrayList for storing all the lons per grid Lat
    lonsPerLatList = new ArrayList();
    int locsBelowLatIndex = 0;

    //initializing the first element of number of locations to be 0 location for
    //min lat.
    //For each lat the number of locations keeps increasing.
    locsBelowLat[locsBelowLatIndex++] = 0;

    //using this location for setting the Lon/Lat at each index in the rectangular region.
    Location loc = new Location();
    //looping over all grid lats in the region to get longitudes at each lat and
    // and number of locations below each starting lat.
    for (int iLat = 0; iLat < numLatGridPoints; iLat++) {

      double lat = regionMinLat + gridSpacing * (double) iLat;
      loc.setLatitude(lat);
      ArrayList lonList = new ArrayList();
      for (int iLon = 0; iLon < numLonGridPoints; iLon++) {
        double lon = regionMinLon + gridSpacing * (double) iLon;
        loc.setLongitude(lon);
        boolean isLocInside = this.isLocationInside(loc);
        if(isLocInside)
          lonList.add(new Double(lon));
      }
      //assigning number of locations below a grid lat to the grid Lat above this lat.
      locsBelowLat[locsBelowLatIndex] = locsBelowLat[locsBelowLatIndex - 1];
      //adding the list of locations on this lat to the next lat index
      locsBelowLat[locsBelowLatIndex++] += lonList.size();
      lonsPerLatList.add(lonList);
    }
  }



  /**
   * Returns boolean if location is within the radius range of the sausage region.
   * This function goes over all the lines in the sausage region and calculate the
   * distance of location from each line. If this distance is less then or equal to
   * the radius, only then this locaiton is within the region.
   * @param location Location
   * @return boolean
   */
  public boolean isLocationInside(Location location){
     int numLines = locList.size() -1;
     for (int i = 0; i < numLines; ++i) {
       Location loc1 = locList.getLocationAt(i);
       Location loc2 = locList.getLocationAt(i + 1);
       double distance = RelativeLocation.getApproxHorzDistToLine(location, loc1, loc2);
       if (distance <= radius) {
         return true;
       }
     }
     return false;
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
   * Creates the locationlist from the
   */
  private void createGriddedLocationList() {

    //creates a instance of new locationList
    gridLocsList = new LocationList();
    //number of gridLats
    int lonsPerLatSize = lonsPerLatList.size();
    //initialising the lat with the nice min lat
    double lat = regionMinLat;
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


  public static void main(String[] args) {
    LocationList locList = new LocationList();
    locList.addLocation(new Location(32.0,-118.0));
    locList.addLocation(new Location(32.3,-118.2));
    locList.addLocation(new Location(33.0,-119.0));
    locList.addLocation(new Location(32.7,-119.4));
    locList.addLocation(new Location(33.5,-120.0));
    EvenlyGriddedSausageGeographicRegion gridReg = new EvenlyGriddedSausageGeographicRegion(locList,111,0.02);

    try {
      FileWriter fw = new FileWriter("SausageRegionFile.txt");
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
