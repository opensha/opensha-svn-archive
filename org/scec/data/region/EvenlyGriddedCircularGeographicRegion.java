package org.scec.data.region;

import java.util.ListIterator;

import org.scec.data.LocationList;
import org.scec.data.Location;

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
  private final static boolean D = true;

  /**
   * class variables
   */
  private double gridSpacing;

  private LocationList gridLocsList;

  /**
   * default constructor
   *
   * @param centerLoc - the location of the circle center
   * @param radius - radius of the region (km)
   * @param gridSpacing - grid spacing (km)
   */
  public EvenlyGriddedCircularGeographicRegion(Location centerLoc, double radius, double gridSpacing) {
    super(centerLoc, radius);
    this.gridSpacing=gridSpacing;
    System.out.println("gridSpacing="+gridSpacing);

    //create the LocationList
    this.createGriddedLocationList();
  }

  /**
   * It samples out the grids location points based on the grid spacing(in degrees)
   * chosen.
   * @param degrees: sets the grid spacing
   */
  public void setGridSpacing(double degrees){
    gridSpacing = degrees;
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


  private void createGriddedLocationList(){
    double minLat=getMinLat();
    double maxLat=getMaxLat();
    double minLon=getMinLon();
    double maxLon=getMaxLon();

    // this rounds the min lat (and lon) to an integer number of gridSpacing increments above the next
    // lower integer lat (or lon)
    double niceMinLat = (Math.floor(minLat - Math.floor(minLat))/gridSpacing)*gridSpacing + Math.floor(minLat);
    double niceMinLon  = (Math.floor(minLon - Math.floor(minLon))/gridSpacing)*gridSpacing + Math.floor(minLon);

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