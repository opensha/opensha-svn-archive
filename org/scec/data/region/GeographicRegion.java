package org.scec.data.region;

import java.util.ListIterator;
import java.util.Collections;
import java.lang.Integer;
import java.awt.Polygon;

import org.scec.data.*;

/**
 * <p>Title: GeographicRegion </p>
 *
 * <p>Description: This class specifies the Geographical region that needs to be
 * defined for generating the maps </p>
 *
 * @author : Nitin Gupta & Vipin Gupta
 * @created : March 5, 2003
 * @version 1.0
 */

public class GeographicRegion {

  /**
   * @List to store all the locations
   */
  private LocationList locList;

  private double minLat,minLon,maxLat,maxLon;

  /**
   * default constructor
   */
  public GeographicRegion(){
  }

  /**
   * @constructor takes the locationList as its parameter to specify the
   * geographical region for those locations.
   * @param locs = locationList
   */
  public GeographicRegion(LocationList locs) {
    locList=locs;
    //calls the private method of the class to precompute the min..max lat & lon.
    setMinMaxLatLon();
  }

  /**
   * Methods checks if the given location is within the region specified in terms of the
   * polygon.
   * @param location
   * @return
   */
  public boolean isLocationInside(Location location){
    //creates the integer array of size equal to the number of the locations in the list
    int[] lat= new int[getNumLocations()+1];
    int[] lon= new int[getNumLocations()+1];
    int latlonIndex=0;

    //As the Lat and Lon are in double so multipling them by a factor to make
    //the precision to cms.
    int LAT_LON_TO_INT_FACTOR=(int)Math.pow(10,7);
    ListIterator lt=getLocationsIterator();
    while(lt.hasNext()){
      //putting all the lat and lon in the integer array
      Location l=(Location)lt.next();
      lat[latlonIndex]=(int)(l.getLatitude()*LAT_LON_TO_INT_FACTOR);
      lon[latlonIndex]=(int)(l.getLongitude()*LAT_LON_TO_INT_FACTOR);
    }
    //specifiing the geographical regoin in the terms of polygon.
    Polygon poly=new Polygon(lat,lon,getNumLocations());
    //checking if the location is contained within that polygon.
    if(poly.contains((int)(location.getLatitude()*LAT_LON_TO_INT_FACTOR),
                  (int)(location.getLongitude()*LAT_LON_TO_INT_FACTOR)))
      return true;
    return false;
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
   * This method can only be called by the class that inherits. This method
   * otherwise can't be called.
   * It initialises the List of Locations if new location list is be made
   * @param locs
   */
  protected void setLocationList(LocationList locs){
    locList=locs;
    setMinMaxLatLon();
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
    minLat=((Location)it.next()).getLatitude();
    minLon=((Location)it.next()).getLongitude();
    maxLat=((Location)it.next()).getLatitude();
    maxLon=((Location)it.next()).getLongitude();
    while(it.hasNext()){
      Location l=(Location)it.next();
      if(l.getLatitude()< minLat)
        minLat=l.getLatitude();
      if(l.getLatitude()> maxLat)
        maxLat=l.getLatitude();
      if(l.getLongitude()<minLon)
        minLon=l.getLongitude();
      if(l.getLongitude()>maxLon)
        maxLon=l.getLongitude();
    }
  }
}
