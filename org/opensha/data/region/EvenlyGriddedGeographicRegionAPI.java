package org.opensha.data.region;

import java.util.ListIterator;

import org.opensha.data.LocationList;
import org.opensha.data.Location;
import java.text.DecimalFormat;

/**
 * <p>Title: EvenlyGriddedGeographicRegionAPI</p>
 * <p>Description:
 * </p>
 * <p>
 * Note : All classes utilising this API creates the niceMinLat and niceMinLon
 * so that all locations within the region match to perfect grid lat-lon.
 * </p>
 * @author : Nitin Gupta & Vipin Gupta
 * @created: March 5,2003
 * @version 1.0
 */

public interface EvenlyGriddedGeographicRegionAPI extends java.io.Serializable{


  public final static DecimalFormat latLonFormat = new DecimalFormat("0.00##");

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
   *
   * @param index
   * @returns the Grid Location at that index.
   */
  public Location getGridLocation(int index);


  /**
   * Returns the index of the nearest location in the given gridded region, to
   * the provided Location.
   * @param loc Location Location to which we have to find the nearest location.
   * @return int Nearest Location index
   */
  public int getNearestLocationIndex(Location loc);

  /**
   * Returns the nearest location in the gridded region to the provided Location.
   * @param loc Location Location to which we have to find the nearest location.
   * @return Location Nearest Location
   */
  public Location getNearestLocation(Location loc);
}

