package org.opensha.commons.data.region;

import java.util.ListIterator;

import org.opensha.commons.data.Location;
import org.opensha.commons.data.LocationList;
import org.opensha.commons.metadata.XMLSaveable;


/**
 * <p>Title: EvenlyGriddedGeographicRegionAPI</p>
 * <p>Description: This API lets the user to define a EvenlyGriddedGeographicRegion.
 *  This API defines  minimum functionality that all the EvenGriddedGeographicRegions are
 * supposed to provide.
 * </p>
 * <p>
 * When creating a EvelyGriddedGeographicRegion classes we adopt the following rules:
 * <ul>
 * <li>If user asks for location at a particular index in the region, then functions in
 * all the EvenlyGriddedGeographicRegion classes will return null, if invalid index
 * is given.
 * <li>If user asks for index of a location in the region, then functions in
 * all the EvenlyGridddedGeographicRegion classes will return -1, if location is
 * not in the region's bound.
 * </ul>
 * User can check for "null" and "< 0" if any processing has to done.
 * Some rules EvenlyGriddedGeographicRegionAPI classes follow when the creating the
 * region locations :
 * <ul>
 * <li>We don't create the region locations before hand, we store then in a efficient
 * way so that retreival is easy.
 * <li>Any method name ending with "clone" just creates a new location object. It
 * is good approach as we don't create the locationlist for the region before-hand.
 * But if user will be calling for same function repeatedly and it returns the same
 * location then it user should use the method that does not end with "clone".
 * <li> When user asks for a location or index of a location in the region and
 * calls a method WITHOUT "clone" then it creates list of locations in the region,
 * if not already exists, and returns the location or index of the location from
 * this list. Drawback of much a approach that this locationlist always exist in
 * the memory but it is much more efficient as not creating a new location everytime.
 * It is memory Vs efficency. We are getting efficency on cost of memory.
 * <li> When locations are created for the gridded region they are created as reading a
 * book. Locations are sorted based on Latitudes in increasing order. For each latitude
 * longitude ordering is maintained in increasing order. Suppose we have a gridded region
 * where Min-Lat = 33.0, Max-Lat = 34.0, Min-Lon = -119.0, Max-Lon = -118.0,GridSpacing = 0.5.
 * Then the locations for the region will be in the following order:
 * <br>33.0  -119.0
 * <br>33.0  -118.5
 * <br>33.0  -118.0
 * <br>33.5  -119.0
 * <br>33.5  -118.5
 * <br>33.5  -118.0
 * <br>33.0  -119.0
 * <br>33.0  -118.5
 * <br>33.0  -118.0
 * <li> All classes utilising this API utilize nice grid-point locations, meaning
 * lat/gridSpacing & lon/gridSpacing are always equal to whole numbers, for all
 * grid locations. This has been done to achieve  calculate the region locations
 * on the fly efficently. But calculating the nice values, that are exact multiple of the
 * gridspacing, might sometimes shorten the min/max lat/lon by maximum of one
 * gridspacing. Even the region bounds remain the same as provided by the user
 * but when list of locations are calculated they might only go upto min/max lat/lon
 * nice grid values.
 * <li> Allows the user to create list of location in the region from
 * another EvenlyGriddedGeographic region using the function
 * createRegionLocationsList(EvenlyGriddedGeographicRegionAPI).
 * If the region passed in as the argument to this function is smaller and do not
 * cover this EvenlyGriddedRegion then this list of locations for the region
 * can have "null" object. Once user has created the region locations using this
 * function then care must be taken to look for "null" objects in the list explicitly.
 * This function creates the list of locations for this EvenlyGriddedRegion, with
 * each location being corresponding to the location in the passed in EvenlyGriddedGeographic region.
 * But if this region does not completely lies within the range of the passed in region
 * then some of its locations can be null, if user uses this method to create the
 * list of locations for a given region.
 * User has to dealt with the null location objects explicitly.
 * If user passes in the region to this function that do not overlap with bounds
 * of this region then all the locations in the list of locations for this region
 * will be null.
 * If passed in region overlaps with this region then locations within the list
 * of locations will be null for the part of this that does not overlap with passed
 * in region.
 * <p>
 * This is helpful as avoid creating same location more then once and just
 * refer to the location object in the passed in EvenlyGriddedGeographicRegionAPI.
 * </p>
 *
 * <B> Note : User should know that once list of locations have been created using
 * the function createRegionLocationsList(EvenlyGriddedGeographicRegionAPI), it will
 * be utilizing these locations in this locationlist for functions like :
 * getGridLocation(int) , getNearestGridLocation(int,EvenlyGriddedGeographicRegionAPI),
 * getNearestGridLocationIndex(int,EvenlyGriddedGeographicRegionAPI),
 * getNearestLocation(Location), getNearestLocationIndex(Location).
 * So if there are null locations within the locationlist then these functions
 * will throw exception.
 * </B>
 * </ul>
 * </p>
 * @author : Nitin Gupta & Vipin Gupta
 * @created: March 5,2003
 * @version 1.0
 */
@Deprecated
public interface EvenlyGriddedGeographicRegionAPI extends 
		GeographicRegionAPI, java.io.Serializable,XMLSaveable{



  /**
   * This method sets the grid spacing in degrees.
   * @param degrees
   */
  public void setGridSpacing(double degrees);

  /**
   *
   * @return  the grid spacing (in degrees)
   */
  public double getGridSpacing();

  /**
   *
   * @returns the number of GridLocation points
   */
  public int getNumGridLocs();

  /**
   *
   * @returns the Grid Locations Iterator.
   */
  public ListIterator getGridLocationsIterator();

  /**
   *
   * @returns the GridLocations List
   */
  public LocationList getGridLocationsList();


  /**
   * Returns the minimum Lat so that this gridLat/gridSpacing is an int,
   * and this min Lat is within the polygon;
   * @return double
   */
  public double getMinGridLat();


  /**
   * Returns the maximum Lat so that this gridLat/gridSpacing is an int,
   * and this max Lat is within the polygon;
   * @return double
   */
  public double getMaxGridLat();

  /**
   * Returns the minimum Lon so that this gridLon/gridSpacing is an int,
   * and this min Lon is within the polygon;
   * @return double
   */
  public double getMinGridLon();

  /**
   * Returns the maximum Lon so that this gridLon/gridSpacing is an int,
   * and this max Lon is within the polygon;
   * @return double
   */
  public double getMaxGridLon();

  /**
   * It returns the grid location at a given index in the region. If the list
   * of locations in the region does not already exists then it creates it keeps
   * it in the memory until user destroys it.  By following
   * this approach we are improving performance on memory cost.
   * @param index location index in the region
   * @returns the Grid Location at that index. Returns null if invalid index is provided.
   */
  public Location getGridLocation(int index);

  /**
   * Returns the index of the nearest location in the given gridded region, to
   * the provided Location.
   * @param loc Location Location to which we have to find the nearest location index.
   * @return int Nearest Location index. Returns -1 if location is outside the regional bounds.
   */
  public int getNearestLocationIndex(Location loc);

//  /**
//   * Returns the nearest location in the gridded region to the provided Location.
//   * This method will create the list of locations for the
//   * region if not already exists then find the nearest location. By following
//   * this approach we are improving performance on memory cost.
//   * @param loc Location Location to which we have to find the nearest location.
//   * @return Location Nearest Location. Returns null if location is outside the regional bounds.
//   */
//  public Location getNearestLocation(Location loc);

//  /**
//   * Returns the nearest location in the gridded region to the provided location.
//   * This method will create a new location object every time this function is called.
//   * It improves the memory utilization as location for the gridded region are not
//   * created. This function gets the nearest location to the given location
//   * on the fly.
//   * @param loc Location Location to which we have to find the nearest location.
//   * @return Location Nearest Location. Returns null if location is outside the regional bounds.
//   */
//  public Location getNearestLocationClone(Location loc);

//  /**
//   * Returns the location in the gridded region to the provided location.
//   * This method will create a new location object every time this function is called.
//   * It improves the memory utilization as location for the gridded region are not
//   * created. This function gets the location to the given location
//   * on the fly.
//   * @param index int location index in the region.
//   * @return Location at the given index. Returns null if invalid index.
//   */
//  public Location getGridLocationClone(int index);

//  /**
//   * Clears the Region LocationList so as to make it empty. Once this function is
//   * called it will remove the list of locations in the region.
//   */
//  public void clearRegionLocations();

//  /**
//   * This gets the nearest location at the given index corresponding to the location in the
//   * given EvenlyGriddedGeographicRegionAPI region.
//   * @param index int index of the called EvenlyGriddedGeographicRegionAPI
//   * @param region EvenlyGriddedGeographicRegionAPI given index will be mapped to a
//   * location in this region.
//   * @return Location Returns the nearest location in the region for the index in the Region
//   * on which this function is called. Returns null if invalid index is provided.
//   */
//  public Location getNearestGridLocation(int index,
//                                  EvenlyGriddedGeographicRegionAPI region) ;

//  /**
//   * This gets the index of the nearest location at the given index
//   * corresponding to the location in the given EvenlyGriddedGeographicRegionAPI region.
//   * @param index int index of the called EvenlyGriddedGeographicRegionAPI
//   * @param region EvenlyGriddedGeographicRegionAPI given index will be mapped to a
//   * location in this region.
//   * @return int  Returns the nearest location index in the region for the index in the Region
//   * on which this function is called. Returns -1 if invalid index is provided.
//   */
//
//  public int getNearestGridLocationIndex(int index,
//                                  EvenlyGriddedGeographicRegionAPI region) ;

  /**
   * This function allows the user to create list of location in the region from
   * another EvenlyGriddedGeographic region.
   * If the region passed in as the argument to this function is smaller and do not
   * cover the this EvenlyGriddedRegion then this list of locations for the region
   * can have "null" object. Once user has created the region locations using this
   * function then care must be taken to look for "null" objects in the list explicitly.
   *
   * This function creates the list of locations for this EvenlyGriddedRegion, with
   * each location being corresponding to the location in the passed in EvenlyGriddedGeographic region.
   * But if this region does not completely lies within the range of the passed in region
   * then some of its locations can be null, if user uses this method to create the
   * list of locations for a given region.
   * User has to dealt with the null location objects explicitly.
   *
   * If user passes in the region to this function that do not overlap with bounds
   * of this region then all the locations in the list of locations for this region
   * will be null.
   * If passed in region overlaps with this region then locations within the list
   * of locations will be null for the part of this that does not overlap with passed
   * in region.
   *
   * <p>This method is helpful as avoid creating same location more then once and just
   * refer to the location object in the passed in EvenlyGriddedGeographicRegionAPI.</p>
   *
   * @param region EvenlyGriddedGeographicRegionAPI EvenGriddedGeographicRegion.
   * Locations created will be locations in this passed in region. So locations
   * will be added to list only for that part of region that overlaps with this
   * passed in region.
   *
   * <B> Note : User should know that once list of locations have been created using
   * the function createRegionLocationsList(EvenlyGriddedGeographicRegionAPI), it will
   * be utilizing these locations in this locationlist for functions like :
   * getGridLocation(int) , getNearestGridLocation(int,EvenlyGriddedGeographicRegionAPI),
   * getNearestGridLocationIndex(int,EvenlyGriddedGeographicRegionAPI),
   * getNearestLocation(Location), getNearestLocationIndex(Location).
   * So if there are null locations within the locationlist then these functions
   * will throw exception.
   * </B>

   */
  public LocationList createRegionLocationsList(EvenlyGriddedGeographicRegion region);

}

