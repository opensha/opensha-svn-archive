package org.opensha.data.region;

import java.util.ListIterator;

import org.opensha.data.LocationList;
import org.opensha.data.Location;
import org.opensha.exceptions.LocationOutOfRegionBoundsException;

/**
 * <p>Title: EvenlyGriddedGeographicRegionAPI</p>
 * <p>Description:
 * </p>
 * <p>
 * Note : All classes utilising this API utilize nice grid-point locations, meaning
 * lat/gridSpacing & lon/gridSpacing are always equal to whole numbers, for all
 * grid locations.
 * </p>
 * @author : Nitin Gupta & Vipin Gupta
 * @created: March 5,2003
 * @version 1.0
 */

public interface EvenlyGriddedGeographicRegionAPI extends GeographicRegionAPI,java.io.Serializable{



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
   *
   * @param index
   * @returns the Grid Location at that index.
   */
  public Location getGridLocation(int index) throws LocationOutOfRegionBoundsException;


  /**
   * Returns the index of the nearest location in the given gridded region, to
   * the provided Location.
   * @param loc Location Location to which we have to find the nearest location.
   * @return int Nearest Location index
   */
  public int getNearestLocationIndex(Location loc) throws LocationOutOfRegionBoundsException;

  /**
   * Returns the nearest location in the gridded region to the provided Location.
   * @param loc Location Location to which we have to find the nearest location.
   * @return Location Nearest Location
   */
  public Location getNearestLocation(Location loc) throws LocationOutOfRegionBoundsException;

  /**
   *
   * @param loc Location
   * @return Location
   * @throws RegionConstraintException
   */
  public Location getNearestLocationClone(Location loc) throws LocationOutOfRegionBoundsException;

  /**
   *
   * @param index int
   * @return Location
   * @throws RegionConstraintException
   */
  public Location getGridLocationClone(int index) throws LocationOutOfRegionBoundsException;

  /**
   * Clears the Region LocationList so as to make it empty.
   */
  public void clearRegionLocations();

  /**
   * This gets the nearest location at the given index corresponding to the location in the
   * given EvenlyGriddedGeographicRegionAPI region.
   * @param index int index of the called EvenlyGriddedGeographicRegionAPI
   * @param region EvenlyGriddedGeographicRegionAPI given index will be mapped to a
   * location in this region.
   * @return Location Returns the nearest location in the region for the index in the Region
   * on which this function is called.
   */
  public Location getNearestGridLocation(int index,
                                  EvenlyGriddedGeographicRegionAPI region) throws
      LocationOutOfRegionBoundsException;

  /**
   * This gets the index of the nearest location at the given index
   * corresponding to the location in the given EvenlyGriddedGeographicRegionAPI region.
   * @param index int index of the called EvenlyGriddedGeographicRegionAPI
   * @param region EvenlyGriddedGeographicRegionAPI given index will be mapped to a
   * location in this region.
   * @return int  Returns the nearest location index in the region for the index in the Region
   * on which this function is called.
   */

  public int getNearestGridLocationIndex(int index,
                                  EvenlyGriddedGeographicRegionAPI region) throws
      LocationOutOfRegionBoundsException;

}

