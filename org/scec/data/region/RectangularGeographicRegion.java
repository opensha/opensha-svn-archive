package org.scec.data.region;

import org.scec.data.*;

/**
 * <p>Title: RectangularGeographicRegion</p>
 *
 * <p>Description: This class specifies the Rectangle Geographical region that needs to be
 * defined for generating the maps. This class specifically deals with the
 * Rectangular Geographical Regions.</p>
 *
 * @author : Nitin Gupta & Vipin Gupta
 * @created: March 5,2003
 * @version 1.0
 */

public class RectangularGeographicRegion extends GeographicRegion {

  /**
   * class variables
   */
  private double minLat,minLon,maxLat,maxLon;

  /**
   * default constructor
   */
  public RectangularGeographicRegion(double minLat,double maxLat,double minLon,
                                     double maxLon) {

    //sets the class variable
    this.minLat=minLat;
    this.maxLat=maxLat;
    this.minLon=minLon;
    this.maxLon=maxLon;
    //creates a new Location List for this rectangular region.
    LocationList locList=new LocationList();
    locList.addLocation(new Location(minLat,minLon));
    locList.addLocation(new Location(minLat,maxLon));
    locList.addLocation(new Location(maxLat,maxLon));
    locList.addLocation(new Location(maxLat,minLon));
    //initialises the locationList in the GeographicRegion class
    setLocationList(locList);
  }



  /**
   * Methods checks if the given location is within the region specified in terms of the
   * rectangle
   * @param location
   * @return
   */
  public boolean isLocationInside(Location location){
    double tempLat=location.getLatitude();
    double tempLon=location.getLongitude();
    if((minLat <= tempLat && maxLat >= tempLat) && (minLon <= tempLon && maxLon >= tempLon))
      return true;
    return false;

  }
}