package org.scec.data.region;

import java.util.ListIterator;

import org.scec.data.LocationList;
import org.scec.data.Location;

/**
 * <p>Title: EvenlyGriddedGeographicRegion</p>
 * <p>Description: </p>
 * @author : Nitin Gupta & Vipin Gupta
 * @created: March 5,2003
 * @version 1.0
 */

public class EvenlyGriddedGeographicRegion extends GeographicRegion
                        implements EvenlyGriddedGeographicRegionAPI {


  /**
   * class variables
   */
  private double gridSpacing;

  private LocationList locList;

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
  }

  /**
   *
   * @return  the grid spacing(in degrees)
   */
  public double getGridSpacing(){
    return gridSpacing;
  }

  /**
   *
   * @returns the number of GridLocation points
   */
  public int getNumGridLocs(){
    //gets the grids points on the latitude based on the gridspacing
    int latGridPoints=(int)Math.ceil(getMaxLat()-getMinLat()/getGridSpacing())+1;
    //gets the grids points on the longitude based on the gridspacing
    int lonGridPoints=(int)Math.ceil(getMaxLon()-getMinLon()/getGridSpacing())+1;

    //total number of grid points locations
    return latGridPoints*lonGridPoints;
  }

  /**
   *
   * @returns the Grid Locations Iterator.
   */
  public ListIterator getGridLocationsIterator(){

    //return the ListIterator for the locationList
    return locList.listIterator();
  }

  /**
   *
   * @returns the GridLocations List
   */
  public LocationList getGridLocationsList(){
    return locList;
  }

  /**
   *
   * @param index
   * @returns the Grid Location at that index.
   */
  public Location getGridLocation(int index){

    //returns  the location at the specified index in the location list
    return locList.getLocationAt(index);
  }

  public LocationList createGriddedLocationList(){
    double minLat=getMinLat();
    double maxLat=getMaxLat();
    double minLon=getMinLon();
    double maxLon=getMaxLon();
    //creates a instance of new locationList
    LocationList locList=new LocationList();
    while(minLat <= maxLat){
      while(minLon <= maxLon){
        //adding the longitude for each gridded latitude to the location list
        locList.addLocation(new Location(minLat,minLon));
        minLon+=gridSpacing;
      }
      minLat+=gridSpacing;
    }
    return locList;
  }
}