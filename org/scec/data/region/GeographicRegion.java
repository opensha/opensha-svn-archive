package org.scec.data.region;

import java.util.ListIterator;
import java.util.Collections;
import java.lang.Integer;
import java.awt.Polygon;

import org.scec.data.*;

/**
 * <p>Title: GeographicRegion </p>
 *
 * <p>Description: This class represents a geographical region using a polygon
 * specified with a LocationList  </p>
 *
 * @author : Nitin Gupta, Vipin Gupta, and Edward field
 * @created : March 5, 2003
 * @version 1.0
 */

public class GeographicRegion implements java.io.Serializable{

  protected LocationList locList;

  protected double minLat,minLon,maxLat,maxLon;

  // polygon used for determining if locations are inside
  private Polygon poly;

  // Factor to convert degrees from doubles to integers while not losing precision
  private int DEGREES_TO_INT_FACTOR=(int)Math.pow(10,7);      // good for cm precision


  private final static String C = "GeographicRegion";
  private final static boolean D = false;


  /**
   * default empty constructor
   */
  public GeographicRegion() {  }


  /**
   * @constructor takes a locationList to specify the geographical region.
   * @param locs = locationList
   */
  public GeographicRegion(LocationList locs) {

    locList=locs;

    //calls the private method of the class to precompute the min..max lat & lon.
    setMinMaxLatLon();

    // create the polygon used for determining whether points are inside
    createPoly();
  }

  /**
   * This method checks whether the given location is inside the region using the
   * definition of insidedness given in the java.awt Shape interface:<p>
   * A point is considered inside if an only if:
   * <UL>
   * <LI>it lies completely inside the boundary or
   * <LI>it lies exactly on the boundary and the space immediately adjacent
   * to the point in the increasing X direction is entirely inside the boundary or
   * <LI>it lies exactly on a horizontal boundary segment and the space immediately
   * adjacent to the point in the increasing Y direction is inside the boundary.
   * </UL><p>

   *
   * @param location
   * @return
   */
  public boolean isLocationInside(Location location){

    if(poly.contains((int)(location.getLatitude()*DEGREES_TO_INT_FACTOR),
                  (int)(location.getLongitude()*DEGREES_TO_INT_FACTOR)))
      return true;
    return false;
  }


  /**
   *  This private method creates the Java polygon used to see if locations are inside the region
   */
  private void createPoly() {
    //creates the integer array of size equal to the number of the locations in the list
    int[] lat= new int[getNumLocations()];
    int[] lon= new int[getNumLocations()];
    int index=0;

    ListIterator lt=getLocationsIterator();
    while(lt.hasNext()){
      //putting all the lat and lon in the integer array
      Location l=(Location)lt.next();
      if(D) System.out.println(C+": index = "+index+"; lat="+l.getLatitude()+"; lon="+l.getLongitude());
      lat[index]=(int)(l.getLatitude()*DEGREES_TO_INT_FACTOR);
      lon[index]=(int)(l.getLongitude()*DEGREES_TO_INT_FACTOR);
      index += 1;
    }

    // create the Java polygon.
    poly = new Polygon(lat,lon,getNumLocations());
  }


  /**
   *
   * @returns maxLat
   */
  public double getMaxLat(){
    return maxLat;
  }

  /**
   *
   * @return minLat
   */
  public double getMinLat(){
    return minLat;
  }

  /**
   *
   * @return minLon
   */
  public double getMaxLon(){
    return maxLon;
  }

  /**
   *
   * @return maxLon
   */
  public double getMinLon(){
    return minLon;
  }

  /**
   *
   * @return the LocationList size
   */
  public int getNumLocations(){
    return locList.size();
  }

  /**
   *
   * @returns the ListIterator to the LocationList
   */
  public ListIterator getLocationsIterator(){
    return locList.listIterator();
  }


  /**
   *
   * @return the List of Locations
   */
  public LocationList getLocationList(){
    return locList;
  }

  /**
   *
   * @param index
   * @return the Location at that index
   */
  public Location getLocation(int index){
    return locList.getLocationAt(index);
  }


  /**
   * this method finds the minLat,maxLat,minLon and maxLon.
   */
  private void setMinMaxLatLon(){
    ListIterator it=getLocationsIterator();
    Location l = (Location) it.next();
    minLat=l.getLatitude();
    minLon=l.getLongitude();
    maxLat=l.getLatitude();
    maxLon=l.getLongitude();
    while(it.hasNext()){
      l=(Location)it.next();
      if(l.getLatitude()< minLat)
        minLat=l.getLatitude();
      if(l.getLatitude()> maxLat)
        maxLat=l.getLatitude();
      if(l.getLongitude()<minLon)
        minLon=l.getLongitude();
      if(l.getLongitude()>maxLon)
        maxLon=l.getLongitude();
    }

    if(D) System.out.println(C +": minLat="+minLat+"; maxLat="+maxLat+"; minLon="+minLon+"; maxLon="+maxLon);
  }



    public static void main(String[] args) {
      Location tempLoc = new Location(33,120);
      LocationList tempLocList = new LocationList();
      tempLocList.addLocation(tempLoc);
      tempLoc = new Location(33,122);
      tempLocList.addLocation(tempLoc);
      tempLoc = new Location(34,122);
      tempLocList.addLocation(tempLoc);
      tempLoc = new Location(34,120);
      tempLocList.addLocation(tempLoc);

      GeographicRegion geoReg = new GeographicRegion(tempLocList);

      System.out.println(C+": numLocations="+ geoReg.getNumLocations());

      System.out.println(C+": isLocationInside (should be true)="+ geoReg.isLocationInside(new Location(33.5,121)));
      System.out.println(C+": isLocationInside (should be true)="+ geoReg.isLocationInside(new Location(33.001,120.001)));
      System.out.println(C+": isLocationInside (should be true)="+ geoReg.isLocationInside(new Location(33,120)));
      System.out.println(C+": isLocationInside (should be false)="+ geoReg.isLocationInside(new Location(33,122)));
      System.out.println(C+": isLocationInside (should be false)="+ geoReg.isLocationInside(new Location(34,122)));
      System.out.println(C+": isLocationInside (should be false)="+ geoReg.isLocationInside(new Location(34,120)));
      System.out.println(C+": isLocationInside (should be true)="+ geoReg.isLocationInside(new Location(33,121)));
      System.out.println(C+": isLocationInside (should be true)="+ geoReg.isLocationInside(new Location(33.5,120)));
    }




}
