package org.opensha.data.region;

import java.util.ListIterator;

import org.opensha.data.LocationList;
import org.opensha.data.Location;

/**
 * <p>Title: EvenlyGriddedCircularGeographicRegion</p>
 * <p>Description: </p>
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


  /**
   * default constructor
   *
   * @param centerLoc - the location of the circle center
   * @param radius - radius of the region (km)
   * @param gridSpacing - grid spacing (km)
   */
  public EvenlyGriddedCircularGeographicRegion(Location centerLoc, double radius, double gridSpacing) {
    super(centerLoc, radius);
    this.setGridSpacing(gridSpacing);
  }

  /**
   * It samples out the grids location points based on the grid spacing(in degrees)
   * chosen.
   * @param degrees: sets the grid spacing
   */
  public void setGridSpacing(double degrees){
    this.gridSpacing=gridSpacing;
    if(D)
      System.out.println("gridSpacing="+gridSpacing);
    niceMinLat = Math.ceil(minLat/gridSpacing)*gridSpacing;
    niceMinLon = Math.ceil(minLon/gridSpacing)*gridSpacing;
    niceMaxLat = Math.floor(maxLat/gridSpacing)*gridSpacing;
    niceMaxLon = Math.floor(maxLon/gridSpacing)*gridSpacing;

    //create the LocationList
    this.createGriddedLocationList();

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
   * Returns the nearest location in the gridded region to the provided Location.
   * @param loc Location Location to which we have to find the nearest location.
   * @return Location Nearest Location
   */
  public Location getNearestLocation(Location loc) {
    //Getting the nearest Location to the rupture point location
    double lat = Math.rint(loc.getLatitude() / gridSpacing) *
        gridSpacing;
    if(lat > getMaxLat())
      lat = niceMaxLat;
    else if(lat < getMinLat())
      lat = niceMinLat;


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




  /**
   *
   * @param index
   * @returns the Grid Location at that index.
   */
  public Location getGridLocation(int index){

    //returns  the location at the specified index in the location list
    return gridLocsList.getLocationAt(index);
  }


  private void createGriddedLocationList(){
    double minLat=getMinLat();
    double maxLat=getMaxLat();
    double minLon=getMinLon();
    double maxLon=getMaxLon();



    if(D) {
      System.out.println("niceMinLat="+niceMinLat+";  niceMinLon="+niceMinLon);
    }
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
    EvenlyGriddedCircularGeographicRegion gridReg = new EvenlyGriddedCircularGeographicRegion(new Location(34,-122,0),111,0.02);
  }

}
