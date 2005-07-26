package org.opensha.data.region;

import java.util.ListIterator;

import org.opensha.data.LocationList;
import org.opensha.data.Location;
import java.text.DecimalFormat;

/**
 * <p>Title: EvenlyGriddedGeographicRegion</p>
 * <p>Description:   </p>
 * @author : Nitin Gupta & Vipin Gupta
 * @created: March 5,2003
 * @version 1.0
 */

public class EvenlyGriddedGeographicRegion extends GeographicRegion
                        implements EvenlyGriddedGeographicRegionAPI {

  private final static String C = "EvenlyGriddedGeographicRegion";
  private final static boolean D = false;

  private double gridSpacing;

  // this makes the first lat and lon grid points nice in that niceMinLat/gridSpacing
  // is an integer and the point is within the polygon
  private double niceMinLat;
  private double niceMinLon ;

  //this makes the last lat and Lon grid points nice so that niceMaxLat/gridSpacing
  // is an integer
  private double niceMaxLat;
  private double niceMaxLon;

  private LocationList gridLocsList;



  /**
   * default class constructor
   */
  public EvenlyGriddedGeographicRegion(LocationList locList,double gridSpacing) {
    super(locList);
    setGridSpacing(gridSpacing);
  }

  /**
   * It samples out the grids location points based on the grid spacing(in degrees)
   * chosen.
   * @param degrees: sets the grid spacing
   */
  public void setGridSpacing(double degrees){
    gridSpacing = degrees;
    niceMinLat = Math.ceil(minLat/gridSpacing)*gridSpacing;
    niceMinLon = Math.ceil(minLon/gridSpacing)*gridSpacing;
    niceMaxLat = Math.floor(maxLat/gridSpacing)*gridSpacing;
    niceMaxLon = Math.floor(maxLon/gridSpacing)*gridSpacing;
    createGriddedLocationList();
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
    return gridLocsList.size();
  }

  /**
   *
   * @returns the Grid Locations Iterator.
   */
  public ListIterator getGridLocationsIterator(){

    //return the ListIterator for the locationList
    return gridLocsList.listIterator();
  }

  /**
   *
   * @returns the GridLocations List
   */
  public LocationList getGridLocationsList(){
    return gridLocsList;
  }


  /**
   *
   * @param index
   * @returns the Grid Location at that index.
   */
  public Location getGridLocation(int index){

    //returns  the location at the specified index in the location list
    return gridLocsList.getLocationAt(index);
  }


  /**
   * Returns the nearest location in the gridded region to the provided Location.
   * @param loc Location Location to which we have to find the nearest location.
   * @return Location Nearest Location
   */
  public Location getNearestLocation(Location loc) {
    //Getting the nearest Location to the rupture point location


    //getting the nearest Latitude. If this Lon is greater then MaxLat, then
    //niceMaxLat will be the nearest Lon. If it is less then the MinLat, then
    //niceMinLat will be the nearest Lat.
    double lat = Math.rint(loc.getLatitude() / gridSpacing) *
        gridSpacing;
    if(lat > getMaxLat())
      lat = niceMaxLat;
    else if(lat < getMinLat())
      lat = niceMinLat;

    //getting the nearest Longitude. If this Lon is greater then MaxLon, then
    //niceMaxLon will be the nearest Lon. If it is less then the MinLon, then
    //niceMinLon will be the nearest Lon
    double lon = Math.rint(loc.getLongitude() / gridSpacing) *
        gridSpacing;
    if(lon > getMaxLon())
      lat = niceMaxLon;
    else if(lon < getMinLon())
      lat = niceMinLon;

    lat = Double.parseDouble(EvenlyGriddedGeographicRegionAPI.latLonFormat.format(lat));
    lon = Double.parseDouble(EvenlyGriddedGeographicRegionAPI.latLonFormat.format(lon));
    return new Location(lat, lon);
  }

  /**
   * Returns the index of the nearest location in the given gridded region, to
   * the provided Location.
   * @param loc Location Location to which we have to find the nearest location.
   * @return int
   */
  public int getNearestLocationIndex(Location rupPointLoc) throws RuntimeException{
    Location loc = getNearestLocation(rupPointLoc);
    return gridLocsList.getLocationIndex(loc);
  }



  /*
   * Creates tha list of the location for the Gridded Geographic Region.
   */
  private void createGriddedLocationList(){
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

  }

  public static void main(String[] args) {
    LocationList locList = new LocationList();
    locList.addLocation(new Location(37.19, -120.61, 0.0));
    locList.addLocation(new Location(36.43, -122.09, 0.0));
    locList.addLocation(new Location(38.23, -123.61, 0.0));
    locList.addLocation(new Location(39.02, -122.08, 0.0));
    EvenlyGriddedGeographicRegion gridReg = new EvenlyGriddedGeographicRegion(locList,0.05);
    System.out.println("num = "+gridReg.getNumGridLocs());
  }

}
