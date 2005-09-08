package org.opensha.data.region;

import java.util.ListIterator;
import java.awt.Polygon;

import org.opensha.data.*;
import org.opensha.calc.RelativeLocation;

/**
 * <p>Title: GeographicRegion </p>
 *
 * <p>Description: This class represents a geographical region using a polygon
 * specified with a LocationList.  It is assumed that the last location is to
 * be connected with the first.
 * WARNING - this region may be screwed up if lon is defined over a range of
 * greater than 360 (e.g., -360 to 360 could cause problems, but 0 to 360, -180 to 180,
 * and -360 to 0 will be OK).     </p>
 *
 * @author : Nitin Gupta, Vipin Gupta, and Edward field
 * @created : March 5, 2003
 * @version 1.0
 */

public class GeographicRegion implements GeographicRegionAPI,java.io.Serializable{

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
    createGeographicRegion(locs);

  }

  /**
   * Creates a Geographic with the given list of locations.
   * @param locs LocationList
   */
  public void createGeographicRegion(LocationList locs) {
    locList=locs;

    //calls the private method of the class to precompute the min..max lat & lon.
    setMinMaxLatLon();

    // create the polygon used for determining whether points are inside
    createPoly();

  }

  /**
   * This method checks whether the given location is inside the region by
   * converting the region outline into a cartesion-coordinate-system polygon, with
   * straight-line segment, and using the definition of insidedness given in the
   * java.awt Shape interface:<p>
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
    int[] lat= new int[getNumRegionOutlineLocations()];
    int[] lon= new int[getNumRegionOutlineLocations()];
    int index=0;

    ListIterator lt=getRegionOutlineIterator();
    while(lt.hasNext()){
      //putting all the lat and lon in the integer array
      Location l=(Location)lt.next();
      if(D) System.out.println(C+": index = "+index+"; lat="+l.getLatitude()+"; lon="+l.getLongitude());
      lat[index]=(int)(l.getLatitude()*DEGREES_TO_INT_FACTOR);
      lon[index]=(int)(l.getLongitude()*DEGREES_TO_INT_FACTOR);
      index += 1;
    }

    // create the Java polygon.
    poly = new Polygon(lat,lon,getNumRegionOutlineLocations());
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
  public int getNumRegionOutlineLocations(){
    return locList.size();
  }

  /**
   *
   * @returns the ListIterator to the LocationList
   */
  public ListIterator getRegionOutlineIterator(){
    return locList.listIterator();
  }


  /**
   *
   * @return the List of Locations (a polygon representing the outline of the region)
   */
  public LocationList getRegionOutline(){
    return locList;
  }



  /**
   * this method finds the minLat,maxLat,minLon and maxLon.
   */
  protected void setMinMaxLatLon(){
    ListIterator it=getRegionOutlineIterator();
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


  /**
   * This computes the minimum horizonatal distance (km) from the location the
   * region outline.  Zero is returned if the given location is inside the polygon.
   * This distance is approximate in that it uses the RelativeLocation.getApproxHorzDistToLine(*)
   * method to compute the distance to each line segment in the region outline.
   * @return
   */
  public double getMinHorzDistToRegion(Location loc) {
    if (isLocationInside(loc))
      return 0.0;
    else {
      double min = locList.getMinHorzDistToLine(loc);
      // now check the segment defined by the last and first points
      double temp = RelativeLocation.getApproxHorzDistToLine(loc,locList.getLocationAt(locList.size()-1),
                                                         locList.getLocationAt(0));
      if (temp < min) return temp;
      else return min;
    }
  }
}
