package org.opensha.data.region;

import org.opensha.data.*;
import org.opensha.exceptions.RegionConstraintException;

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
                                     double maxLon) throws RegionConstraintException{

    //sets the class variable
    super.minLat=minLat;
    super.maxLat=maxLat;
    super.minLon=minLon;
    super.maxLon=maxLon;

    /*if(minLat > maxLat){

        if (minLat < 0 && maxLat < 0) {
          throw new RegionConstraintException(
              "Min. Lat must be less then Max. Lat.\n" +
              "Two options to enter the Min and Max Lat :\n" +
              "1. MinLat = " + maxLat + " and MaxLat = " + minLat + "\n" +
              "2. MinLat = " + minLat + " and MaxLat = " +
              (90 - Math.abs(maxLat)));
        }
        else if (minLat > 0 && maxLat > 0) {
          throw new RegionConstraintException(
              "Min. Lat must be less then Max. Lat.\n" +
              "Two options to enter the Min and Max Lat :\n" +
              "1. MinLat = " + maxLat + " and MaxLat = " + minLat + "\n" +
              "2. MinLat = " + -(90 - minLat) + " and MaxLat = " + maxLat);
        }
        else{
          throw new RegionConstraintException(
              "Min. Lat must be less then Max. Lat.\n");
      }
    }

    if(minLon > maxLon){
      if (minLon < 0 && maxLon < 0) {
         throw new RegionConstraintException(
             "Min. Lon must be less then Max. Lon.\n" +
             "Two options to enter the Min and Max Lon :\n" +
             "1. MinLon = " + maxLon + " and MaxLon = " + minLon + "\n" +
             "2. MinLon = " + minLon + " and MaxLon = " +
             (360 - Math.abs(maxLon)));
       }
       else if (minLon > 0 && maxLon > 0) {
         throw new RegionConstraintException(
             "Min. Lon must be less then Max. Lon.\n" +
             "Two options to enter the Min and Max Lon :\n" +
             "1. MinLon = " + maxLon + " and MaxLon = " + minLon + "\n" +
             "2. MinLon = " + -(360 - minLon) + " and MaxLon = " + maxLon);
       }
       else{
         throw new RegionConstraintException(
             "Min. Lon must be less then Max. Lon.\n");
      }
    }*/

  if(minLat > maxLat)
    throw new RegionConstraintException(
        "Min. Lat must be less then Max. Lat.\n");
  if(minLon > maxLon)
    throw new RegionConstraintException(
        "Min. Lon must be less then Max. Lon.\n");



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
