package org.scec.data.region;

import org.scec.data.*;
import org.scec.calc.RelativeLocation;

/**
 * <p>Title: RectangularGeographicRegion</p>
 *
 * <p>Description: This class defines a Circular Geographical region.</p>
 *
 * @author : Edward Field
 * @created: Aug. 2,2004
 * @version 1.0
 */

public class CircularGeographicRegion extends GeographicRegion {


  private final static String C = "CircularGeographicRegion";
  private final static boolean D = false;

  protected Location circleCenterLocation;
  protected double circleRadius;

  /**
   * default constructor
   */
  public CircularGeographicRegion(Location loc, double radius) {

    this.circleCenterLocation = loc;
    this.circleRadius = radius;
    Location tempLoc;
    Direction dir;

    //set min and max lat and lons
    dir = new Direction(0,radius,180,0);
    tempLoc = RelativeLocation.getLocation(loc,dir);
    super.minLat=tempLoc.getLatitude();
    dir = new Direction(0,radius,0,180);
    tempLoc = RelativeLocation.getLocation(loc,dir);
    super.maxLat=tempLoc.getLatitude();
    dir = new Direction(0,radius,270,90);
    tempLoc = RelativeLocation.getLocation(loc,dir);
    super.minLon=tempLoc.getLongitude();
    dir = new Direction(0,radius,90,270);
    tempLoc = RelativeLocation.getLocation(loc,dir);
    super.maxLon=tempLoc.getLongitude();

    // set this null for now
    locList=null;
  }



  /**
   * This method checks whether the given location is within the region by seeing whether
   * the distance to the circle center is less than or equal to the circle radius.
   * @param location
   * @return
   */
  public boolean isLocationInside(Location location){
    double horzDist = RelativeLocation.getHorzDistance(this.circleCenterLocation, location);

    if(horzDist <= this.circleRadius)
      return true;
    return false;

  }
}