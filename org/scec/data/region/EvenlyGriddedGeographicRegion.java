package org.scec.data.region;

import java.util.ListIterator;

import org.scec.data.LocationList;
import org.scec.data.Location;

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

  private LocationList gridLocsList;

  /**
   * default class constructor
   */
  public EvenlyGriddedGeographicRegion(LocationList locList,double gridSpacing) {
    super(locList);
    this.gridSpacing=gridSpacing;

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