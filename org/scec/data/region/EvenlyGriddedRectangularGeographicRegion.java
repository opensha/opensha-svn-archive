package org.scec.data.region;

import java.util.ListIterator;

import org.scec.data.LocationList;
import org.scec.data.Location;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class EvenlyGriddedRectangularGeographicRegion extends RectangularGeographicRegion
                                  implements EvenlyGriddedGeographicRegionAPI{

  /**
   * class variables
   */
  private double gridSpacing;

  /**
   * class constructor
   * @param minLat
   * @param maxLat
   * @param minLon
   * @param maxLon
   */
  public EvenlyGriddedRectangularGeographicRegion(double minLat,double maxLat,
      double minLon,double maxLon, double gridSpacing) {
    super(minLat,maxLat,minLon,maxLon);
    this.gridSpacing=gridSpacing;
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

    //creating the instance of the locationList
    LocationList locList=createGriddedLocationList();
    //return the ListIterator for the locationList
    return locList.listIterator();
  }

  /**
   *
   * @returns the GridLocations List
   */
  public LocationList getGridLocationsList(){
    //creating the instance of the locationList
    LocationList locList=createGriddedLocationList();
    return locList;
  }

  /**
   *
   * @param index: it starts from zero
   * @returns the Grid Location at that index.
   */
  public Location getGridLocation(int index){

    //getting the lat and lon grid points
    //number of grid points on each Lat
    int latGridPoints= (int)Math.ceil((getMaxLat()-getMinLat())/gridSpacing)+1;
    //number of gridPoints on each Lon
    int lonGridPoints= (int)Math.ceil((getMaxLon()-getMinLon())/gridSpacing)+1;

    //as the we are adding the Lons for each lat. so in a grid ew can assume the
    //scenario as the Lats being at the rows and the Lons being at the column.
    //so we are scanning each lat row to get the desired grid location.

    //gets the row for the latitude in which that index of grid exists
    int latGridLoc=index/latGridPoints;
    //gets the column in the row (longitude point) where that index exists
    int lonGridLoc=index%latGridPoints;

    //lat and lon for that indexed point
    double newLat=getMinLat()+latGridLoc*gridSpacing;
    double newLon=getMinLon()+lonGridLoc*gridSpacing;

    //new location at which that lat and lon exists
    Location location= new Location(newLat,newLon);

    //returns  the location at the specified index in the location list
    return location;
  }


  /**
   * Private method to create the Location List for the gridded Rectangular Geog. Region
   * @returns the LocationList
   */
  private LocationList createGriddedLocationList(){
    double minLat=getMinLat();
    double maxLat=getMaxLat();
    double minLon= getMinLon();
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