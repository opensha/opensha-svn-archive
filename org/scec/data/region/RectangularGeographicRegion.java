package org.scec.data.region;

import org.scec.data.*;

/**
 * <p>Title: RectangularGeographicRegion</p>
 *
 * <p>Description: This class defines a Rectangular Geographical region.</p>
 *
 * @author : Nitin Gupta, Vipin Gupta, and Edward Field
 * @created: March 5,2003
 * @version 1.0
 */

public class RectangularGeographicRegion extends GeographicRegion {


  private final static String C = "RectangularGeographicRegion";
  private final static boolean D = false;


  /**
   * default constructor
   */
  public RectangularGeographicRegion(double minLat,double maxLat,double minLon,
                                     double maxLon) {

    //sets the class variable
    super.minLat=minLat;
    super.maxLat=maxLat;
    super.minLon=minLon;
    super.maxLon=maxLon;

    //creates the Location List for this rectangular region.
    locList=new LocationList();
    locList.addLocation(new Location(minLat,minLon));
    locList.addLocation(new Location(minLat,maxLon));
    locList.addLocation(new Location(maxLat,maxLon));
    locList.addLocation(new Location(maxLat,minLon));
  }



  /**
   * This method checks whether the given location is within the region using the definition of
   * insidedness used in the parent class (true if on lower or left-hand boundary, but false
   * if on the upper or right-hand boundary)
   * @param location
   * @return
   */
  public boolean isLocationInside(Location location){
    double tempLat=location.getLatitude();
    double tempLon=location.getLongitude();

    if(D) System.out.println(C +": minLat="+minLat+"; maxLat="+maxLat+"; minLon="+minLon+"; maxLon="+maxLon);

    if((tempLat >= minLat && tempLat < maxLat) && (tempLon >= minLon && tempLon < maxLon))
      return true;
    return false;

  }
}