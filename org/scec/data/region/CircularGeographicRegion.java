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
  private final static boolean D = true;

  protected Location circleCenterLocation;
  protected double circleRadius;

  /**
   * default constructor
   *
   * @param centerLoc - the location of the circle center
   * @param radius - radius of the region (km)
   */
  public CircularGeographicRegion(Location centerLoc, double radius) {

    this.circleCenterLocation = centerLoc;
    this.circleRadius = radius;
    Location tempLoc;
    Direction dir;

    //set min and max lat and lons
    dir = new Direction(0,radius,180,0);
    tempLoc = RelativeLocation.getLocation(centerLoc,dir);
    super.minLat=tempLoc.getLatitude();
    dir = new Direction(0,radius,0,180);
    tempLoc = RelativeLocation.getLocation(centerLoc,dir);
    super.maxLat=tempLoc.getLatitude();
    dir = new Direction(0,radius,270,90);
    tempLoc = RelativeLocation.getLocation(centerLoc,dir);
    super.minLon=tempLoc.getLongitude();
    dir = new Direction(0,radius,90,270);
    tempLoc = RelativeLocation.getLocation(centerLoc,dir);
    super.maxLon=tempLoc.getLongitude();

    if (D) {
      System.out.println("minLat = "+minLat+";  maxLat = "+maxLat+";  minLon = "+minLon+";  maxLon = "+maxLon);
      double dist;
      dist = RelativeLocation.getHorzDistance(minLat,circleCenterLocation.getLongitude(), maxLat,circleCenterLocation.getLongitude());
      System.out.println("computed horz diameter = "+ dist+"; circleRadius = " +circleRadius);
      dist = RelativeLocation.getHorzDistance(circleCenterLocation.getLatitude(),minLon, circleCenterLocation.getLatitude(),maxLon);
      System.out.println("computed vert diameter = "+ dist+"; circleRadius = " +circleRadius);
    }

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

  public static void main(String[] args) {
    CircularGeographicRegion reg = new CircularGeographicRegion(new Location(34,-122,0),111);
  }


}