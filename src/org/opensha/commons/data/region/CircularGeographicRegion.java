package org.opensha.commons.data.region;


import org.opensha.commons.calc.RelativeLocation;
import org.opensha.commons.data.Direction;
import org.opensha.commons.data.Location;
import org.opensha.commons.data.LocationList;

import java.io.IOException;
import java.util.ListIterator;
import java.io.FileWriter;

/**
 * <p>Title: RectangularGeographicRegion</p>
 *
 * <p>Description: This class defines a Circular Geographical region.  Circle
 * radius is defined in km.</p>
 *
 * @author : Edward Field
 * @created: Aug. 2,2004
 * @version 1.0
 * TODO delete; all refs moved to all purpose GeographicRegion
 */
@Deprecated
public class CircularGeographicRegion extends GeographicRegion {


  private final static String C = "CircularGeographicRegion";
  private final static boolean D = false;

  protected Location circleCenterLocation;
  protected double circleRadius;

  /**
   * default constructor
   *
   * @param centerLoc - the location of the circle center
   * @param radius - radius of the region (km)
   */
//  public CircularGeographicRegion(Location centerLoc, double radius) {
//
//    this.circleCenterLocation = centerLoc;
//    this.circleRadius = radius;
//    Location tempLoc;
//    Direction dir;

    //set min and max lat and lons
//    dir = new Direction(0,radius,180,0);
//    tempLoc = RelativeLocation.getLocation(centerLoc,dir);
//    super.minLat=tempLoc.getLatitude();
//    dir = new Direction(0,radius,0,180);
//    tempLoc = RelativeLocation.getLocation(centerLoc,dir);
//    super.maxLat=tempLoc.getLatitude();
//    dir = new Direction(0,radius,270,90);
//    tempLoc = RelativeLocation.getLocation(centerLoc,dir);
//    super.minLon=tempLoc.getLongitude();
//    dir = new Direction(0,radius,90,270);
//    tempLoc = RelativeLocation.getLocation(centerLoc,dir);
//    super.maxLon=tempLoc.getLongitude();
//
//    if(minLon>maxLon) throw new RuntimeException("Problem in CircularGeographicRegion"+
//                                                "related to crossing the zero lat boundary");
//
//    if (D) {
//      System.out.println("minLat = "+minLat+";  maxLat = "+maxLat+";  minLon = "+minLon+";  maxLon = "+maxLon);
//      double dist;
//      dist = RelativeLocation.getHorzDistance(minLat,circleCenterLocation.getLongitude(), maxLat,circleCenterLocation.getLongitude());
//      System.out.println("computed horz diameter = "+ dist+"; circleRadius = " +circleRadius);
//      dist = RelativeLocation.getHorzDistance(circleCenterLocation.getLatitude(),minLon, circleCenterLocation.getLatitude(),maxLon);
//      System.out.println("computed vert diameter = "+ dist+"; circleRadius = " +circleRadius);
//    }

    // make the region outline (locList)
//    makeRegionOutline(10.0);
//  }


  /**
   * Returns the CircularGeographicRegion radius
   * @return double
   */
  public double getCircularRegionRadius(){
    return circleRadius;
  }

  /**
   * Returns the center location CircularRegionCenterLocation
   * @return Location Center Location
   */
  public Location getCircularRegionCenterLocation(){
    return circleCenterLocation;
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

//  public static void main(String[] args) {
//    CircularGeographicRegion reg = new CircularGeographicRegion(new Location(34,-122,0),111);
//    try {
//      FileWriter fw = new FileWriter("CircularRegionFile1.txt");
//      ListIterator it = reg.getRegionOutlineIterator();
//      while(it.hasNext()){
//        Location loc = (Location)it.next();
//        fw.write(loc.toString()+"\n");
//      }
//      fw.close();
//    }
//    catch (IOException ex) {
//      ex.printStackTrace();
//    }
//
//  }

  // this make the locList for the region outline
  /**
   * This creates the region outline for the circle, where there are 360/degreeIncrement
   * equally spaced points (the last two may be closer than this).  The constructor (default)
   * uses 10 degrees (36 points)
   *
   * @param degreeIncrement - difference in azimuth (from the center) between neighboring points
   */
  private void makeRegionOutline(double degreeIncrement) {
//    locList = new LocationList();
//    Direction tempDir;
//    for(double deg = 0; deg<360; deg += degreeIncrement) {
//      tempDir = new Direction(0.0,circleRadius,deg,180-deg);
//      locList.addLocation(RelativeLocation.getLocation(circleCenterLocation,tempDir));
//    }
//
//    if(D) {
//      Location tempLoc;
//      System.out.println("Region outline:");
//      for(int i = 0; i < locList.size(); i++) {
//        tempLoc = locList.getLocationAt(i);
//        System.out.println(tempLoc.getLatitude()+"  "+tempLoc.getLongitude());
//      }
//    }
  }


}
