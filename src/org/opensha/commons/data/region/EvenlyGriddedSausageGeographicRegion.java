package org.opensha.commons.data.region;

import java.util.ListIterator;

import java.util.ArrayList;
import org.opensha.commons.calc.RelativeLocation;
import org.opensha.commons.data.Direction;
import org.opensha.commons.data.Location;
import org.opensha.commons.data.LocationList;

import java.io.*;



/**
 * <p>Title: EvenlyGriddedSausageGeographicRegion</p>
 * <p>Description: This class creates a EvenlyGridded Sausage Geographical region
 * using a line (LocationList) and radius (km).
 * </p>
 * <p>
 * This is defined by all grid points that are within radius from the line.
 * </p>
 * <p>
 * This class has been tested by having a main method that creates all the
 * locations within EvenlyGridded Sausage Geographical region. It dumps out all
 * these locations in a file called  "SausageRegionFile.txt",
 * in the sha project home directory. File format is "lat,lon,depth" on each line
 * of file. One can take this file and plot it in some kind plotting tool to see
 * if region looks like a EvenlyGriddedSaugage Geographical region.
 * </p>
 * @see EvenlyGriddedGeographicRegionAPI
 *
 * @author : Nitin Gupta
 * @created: July 30,2005
 * @version 1.0
 * TODO retire; all functionality moved to EvenlyGriddedGeographicRegion
 */
@Deprecated
public class EvenlyGriddedSausageGeographicRegion
    extends EvenlyGriddedGeographicRegion
                        {
//
//  private final static String C = "EvenlyGriddedSausageGeographicRegion";
//  private final static boolean D = false;
//
//
//  //This array store number of locations below a given latitude
//  protected int[] locsBelowLat;
//
//  //List for storing each lon for a given latitude
//  protected ArrayList lonsPerLatList;
//
//
//  //gets the locations where min/max lat lons are found in the case of sausage region.
//  //these are the corresponding locaiton lat/lon for the locations provided by the
//  //user in which we found the min/max lat/lon.
//  protected double minLatLon, maxLatLon, minLonLat, maxLonLat;
//
//  //Sausage Region Min/Max lat/lon
//  protected double regionMinLat, regionMaxLat,regionMinLon,regionMaxLon;
//
//
//  //Radius of the Sausage region
//  protected double radius;
//
//
//  /**
//   * Class default constructor
//   */
//  public EvenlyGriddedSausageGeographicRegion(){}
//
//
//
//  /**
//   * Class constructor that accepts the list of locations as corner points on the
//   * line around which sausage region is to be created, circle radius,grid spacing
//   * for creation of a EvenlyGriddedSausageGeographicRegion.
//   * @param locList LocationList : Locations of the end points on the lines
//   * @param radius double Maximum distance for which locations in this sausage
//   * region are to be considered.
//   * @param gridSpacing double Grid Spacing in degrees
//   * This function creates a efficient way of retreving the locations from the
//   * EvenlyGriddedSausageGeographicRegion on the fly without stroing the list of
//   * location for this Evenly Gridded Geographic Region.
//   */
//  public EvenlyGriddedSausageGeographicRegion(LocationList locList, double radius, double gridSpacing) {
//    //createEvenlyGriddedSausageGeographicRegion(locList,radius,gridSpacing) ;
//	  super(locList, radius, gridSpacing);
//	    this.radius = radius;
//	    setGridSpacing(gridSpacing);
//
//  }
//
//
////  /**
////   * Class constructor that accepts the list of locations as corner points on the
////   * line around which sausage region is to be created, radius,grid spacing
////   * and EvenlyGriddedGeographicRegionAPI,for creating the list of locations
////   * in this region from passed in EvenlyGriddedGeographicRegionAPI,
////   * for creation of a EvenlyGriddedSausageGeographicRegion.
////   *
////   * This method is helpful as avoid creating same location more then once and just
////   * refer to the location object in the passed in EvenlyGriddedGeographicRegionAPI.
////   *
////   * This class constructor allows the user to create list of locations for this
////   * EvenlyGriddedGeographic object from passed in EvenlyGriddedGeographicRegionAPI.
////   * Please refer to EvenlyGriddedGeographicRegionAPI for more details.
////   *
////   * @param locList LocationList : Locations of the end points on the lines
////   * @param radius double Maximum distance for which locations in this sausage
////   * region are to be considered.
////   * @param gridSpacing double Grid Spacing in degrees
////   * @param region EvenlyGriddedGeographicRegionAPI
////   * @see EvenlyGriddedGeographicRegionAPI.createRegionLocationsList(EvenlyGriddedGeographicRegionAPI)
////   * @see EvenlyGriddedGeographicRegionAPI
////   */
////  public EvenlyGriddedSausageGeographicRegion(LocationList locList, double radius, double gridSpacing,
////      EvenlyGriddedGeographicRegionAPI region) {
////    this(locList,radius,gridSpacing);
////    createRegionLocationsList(region);
////  }
//
//
//
//  /**
//   * This function allows to create a sausage region around a given list of
//   * locations with the given radius.
//   *
//   * Note:Only end point locations for the given line segments should be included.
//   *
//   * @param locList LocationList List of locations for end-points on teh line segments
//   * @param radius double maximum distance from any given location in the
//   * locationlist
//   * @param gridSpacing double
//   */
////  public void createEvenlyGriddedSausageGeographicRegion(LocationList locList,
////                                                double radius,
////                                                double gridSpacing) {
////    //this.locList = locList;
////    //setMinMaxLatLon();
////    this.radius = radius;
////    setGridSpacing(gridSpacing);
////  }
//
//
//  /**
//   * These function have not been defined yet because there is is we don't know
//   * how to generate boundary around a Sausage Region.
//   * @return int
//   * @throws UnsupportedOperationException
//   */
//  public int getNumRegionOutlineLocations() throws UnsupportedOperationException{
//    throw new UnsupportedOperationException("Method not implemented");
//  }
//
//  /**
//   * These function have not been defined yet because there is we don't know
//   * how to generate boundary around a Sausage Region.
//   * @return ListIterator
//   * @throws UnsupportedOperationException
//   */
//  public ListIterator getRegionOutlineIterator() throws
//      UnsupportedOperationException {
//    throw new UnsupportedOperationException("Method not implemented");
//  }
//
//
//
//  /**
//   * These function have not been defined yet because there is we don't know
//   * how to generate boundary around a Sausage Region.
//   * @return LocationList
//   * @throws UnsupportedOperationException
//   */
//  public LocationList getRegionOutline() throws UnsupportedOperationException {
//    throw new UnsupportedOperationException("Method not implemented");
//  }
//
//
//
//  /**
//   * It gets the min/max lat/lon from the provided list of locations. It then
//   * creates the rectangular region around the Sausage Region and see which
//   * of the locations in this rectangular region are within the radius distance of
//   * given locations. It actually not saves all the locations in the Sausage in memory
//   * but save it in a efficient so as to conserve memory and compute the location
//   * in the gridded region on the fly.
//   * It samples out the grids location points based on the grid spacing(in degrees)
//   * chosen.
//   * @param degrees: sets the grid spacing
//   * @see EvenlyGriddedGeographicRegionAPI.setGridSpacing(double)
//   */
////  protected void setMinMaxLatLon(){
////    ListIterator it=locList.listIterator();
////    Location l = (Location) it.next();
////    minLat=l.getLatitude();
////    minLon=l.getLongitude();
////    maxLat=l.getLatitude();
////    maxLon=l.getLongitude();
////    //in addition to getting the min/max lat/Lon it gets the locations for these.
////    while(it.hasNext()){
////      l=(Location)it.next();
////      if(l.getLatitude()< minLat){
////        minLat = l.getLatitude();
////        minLatLon = l.getLongitude();
////      }
////      if(l.getLatitude()> maxLat){
////        maxLat = l.getLatitude();
////        maxLatLon = l.getLongitude();
////      }
////      if(l.getLongitude()<minLon){
////        minLon = l.getLongitude();
////        minLonLat = l.getLatitude();
////      }
////      if(l.getLongitude()>maxLon){
////        maxLon = l.getLongitude();
////        maxLonLat = l.getLatitude();
////      }
////    }
////
////    if(D) System.out.println(C +": minLat="+minLat+"; maxLat="+maxLat+"; minLon="+minLon+"; maxLon="+maxLon);
////  }
//
//
//  /**
//   * Returns the EvenlyGriddedSausageRegion radius
//   * @return double
//   */
//  public double getEvenlyGriddedSausageRegionRadius(){
//    return radius;
//  }
//
//
//  /**
//   * Creates the list of location in the gridded region and keeps it in the
//   * memory until cleared.
//   */
//  protected void createEvenlyGriddedRectangularRegionOutline(){
//    //location that finds the min lat for the rectangular region
//    Location minLatLoc = new Location(niceMinLat,minLatLon);
//    //set min and max lat and lons
//    Direction dir = new Direction(0, radius, 180, 0);
//    Location tempLoc = RelativeLocation.getLocation(minLatLoc, dir);
//    regionMinLat = tempLoc.getLatitude();
//
//    //location that finds the max lat for the rectangular region
//    Location maxLatLoc = new Location(niceMaxLat,maxLatLon);
//    dir = new Direction(0, radius,0, 180);
//    tempLoc = RelativeLocation.getLocation(maxLatLoc, dir);
//    regionMaxLat = tempLoc.getLatitude();
//
//    //location that finds the min lon for the rectangular region
//    Location minLonLoc = new Location(minLonLat, niceMinLon);
//    dir = new Direction(0, radius,270,90);
//    tempLoc = RelativeLocation.getLocation(minLonLoc, dir);
//    regionMinLon = tempLoc.getLongitude();
//
//    //location that finds the max lon for the rectangular region
//    Location maxLonLoc = new Location(maxLonLat, niceMaxLon);
//    dir = new Direction(0, radius,90, 270);
//    tempLoc = RelativeLocation.getLocation(maxLonLoc, dir);
//    regionMaxLon = tempLoc.getLongitude();
//
//
//  }
//
//  /**
//   * It samples out the grids location points based on the grid spacing(in degrees)
//   * chosen.It creates the rectangular region around the Sausage Region and see which
//   * of the locations in this rectangular region are within the radius distance of
//   * given locations. It actually not saves all the locations in the Sausage in memory
//   * but save it in a efficient so as to conserve memory and compute the location
//   * in the gridded region on the fly.
//
//   * @param degrees: sets the grid spacing
//   */
//  public void setGridSpacing(double gridSpacing){
//    this.gridSpacing=gridSpacing;
//    if(D)
//      System.out.println("gridSpacing="+gridSpacing);
//    niceMinLat = Math.ceil(getMinLat()/gridSpacing)*gridSpacing;
//    niceMinLon = Math.ceil(getMinLon()/gridSpacing)*gridSpacing;
//    niceMaxLat = Math.floor(getMaxLat()/gridSpacing)*gridSpacing;
//    niceMaxLon = Math.floor(getMaxLon()/gridSpacing)*gridSpacing;
//
//    //getting the nice Lat/Lon for the locations at min/max lat/lon.
//    minLatLon = Math.ceil(minLatLon/gridSpacing)*gridSpacing;
//    maxLatLon = Math.floor(maxLatLon/gridSpacing)*gridSpacing;
//    minLonLat = Math.ceil(minLonLat/gridSpacing)*gridSpacing;
//    maxLonLat = Math.floor(maxLonLat/gridSpacing)*gridSpacing;
//    //creates the Rectangular Region boundary around the sausage region
//    createEvenlyGriddedRectangularRegionOutline();
//    //creates the optimized version for retreiving the locations from the region.
//    initLatLonArray();
//  }
//
//
//  /*
//   * This function creates a Lon Array for each gridLat. It also creates a
//   * int array which tells how many locations are there below a given lat.
//   */
//  protected void initLatLonArray() {
//    //number of Grid Lat/Lons that can be in the Gridded Rectangular Region
//    int numLatGridPoints = (int) Math.ceil( (regionMaxLat - regionMinLat) /
//                                           gridSpacing) + 1;
//    int numLonGridPoints = (int) Math.ceil( (regionMaxLon - regionMinLon) /
//                                           gridSpacing) + 1;
//
//    //initialising the array for storing number of location below a given lat
//    //first element is 0 as first lat has 0 locations below it and last num is the
//    //total number of locations.
//    locsBelowLat = new int[numLatGridPoints+1];
//    //ArrayList for storing all the lons per grid Lat
//    lonsPerLatList = new ArrayList();
//    int locsBelowLatIndex = 0;
//
//    //initializing the first element of number of locations to be 0 location for
//    //min lat.
//    //For each lat the number of locations keeps increasing.
//    locsBelowLat[locsBelowLatIndex++] = 0;
//
//    //using this location for setting the Lon/Lat at each index in the rectangular region.
//    Location loc = new Location();
//    //looping over all grid lats in the region to get longitudes at each lat and
//    // and number of locations below each starting lat.
//    for (int iLat = 0; iLat < numLatGridPoints; iLat++) {
//
//      double lat = regionMinLat + gridSpacing * (double) iLat;
//      loc.setLatitude(lat);
//      ArrayList lonList = new ArrayList();
//      for (int iLon = 0; iLon < numLonGridPoints; iLon++) {
//        double lon = regionMinLon + gridSpacing * (double) iLon;
//        loc.setLongitude(lon);
//        boolean isLocInside = this.isLocationInside(loc);
//        if(isLocInside)
//          lonList.add(new Double(lon));
//      }
//      //assigning number of locations below a grid lat to the grid Lat above this lat.
//      locsBelowLat[locsBelowLatIndex] = locsBelowLat[locsBelowLatIndex - 1];
//      //adding the list of locations on this lat to the next lat index
//      locsBelowLat[locsBelowLatIndex++] += lonList.size();
//      lonsPerLatList.add(lonList);
//    }
//  }
//
//
//
//  /**
//   * Returns boolean if location is within the radius range of the sausage region.
//   * This function goes over all the lines in the sausage region and calculate the
//   * distance of location from each line. If this distance is less then or equal to
//   * the radius, only then this locaiton is within the region.
//   * @param location Location
//   * @return boolean
//   */
//  public boolean isLocationInside(Location loc){
//    if(getRegionOutline().getMinHorzDistToLine(loc) > radius) return false;
//    return true;
//   }
//
//
//
//
//  /**
//   * Creates the locationlist from the
//   */
//  protected void createGriddedLocationList() {
//
//    //creates a instance of new locationList
//    gridLocsList = new LocationList();
//    //number of gridLats
//    int lonsPerLatSize = lonsPerLatList.size();
//    //initialising the lat with the nice min lat
//    double lat = regionMinLat;
//    //iterating over all lons for each lat, and creating a Location list from it.
//    for (int i = 0; i < lonsPerLatSize; ++i) {
//      ArrayList lonList = (ArrayList) lonsPerLatList.get(i);
//      int numLons = lonList.size();
//      for (int j = 0; j < numLons; ++j) {
//        double lon = ( (Double) lonList.get(j)).doubleValue();
//        //creating a new location
//        Location loc = new Location(lat, lon);
//        gridLocsList.addLocation(loc);
//      }
//      //getting the next grid lat.
//      lat += gridSpacing;
//    }
//  }
//
//  /*
//   * Main method to run the this class and produce a file with
//   * EvenlyGriddedRegion locations.
//   */
//  public static void main(String[] args) {
//    LocationList locList = new LocationList();
//    locList.addLocation(new Location(32.0,-118.0));
//    locList.addLocation(new Location(32.3,-118.2));
//    locList.addLocation(new Location(33.0,-119.0));
//    locList.addLocation(new Location(32.7,-119.4));
//    locList.addLocation(new Location(33.5,-120.0));
//    EvenlyGriddedGeographicRegion gridReg = new EvenlyGriddedGeographicRegion(locList,111,0.02);
//
//    try {
//      FileWriter fw = new FileWriter("SausageRegionFile.txt");
//      ListIterator it = gridReg.getGridLocationsIterator();
//      while(it.hasNext()){
//        Location loc = (Location)it.next();
//        fw.write(loc.toString()+"\n");
//      }
//      fw.close();
//    }
//    catch (IOException ex) {
//      ex.printStackTrace();
//    }
//  }

}
