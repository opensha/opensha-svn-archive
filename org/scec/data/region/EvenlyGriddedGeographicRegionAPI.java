package org.scec.data.region;

import java.util.ListIterator;

import org.scec.data.LocationList;
import org.scec.data.Location;

/**
 * <p>Title: EvenlyGriddedGeographicRegionAPI</p>
 * <p>Description: </p>
 * @author : Nitin Gupta & Vipin Gupta
 * @created: March 5,2003
 * @version 1.0
 */

interface EvenlyGriddedGeographicRegionAPI {

  /**
   * It samples out the grids location points based on the grid spacing(in degrees)
   * chosen.
   * @param degrees: sets the grid spacing
   */
  public void setGridSpacing(double degrees);

  /**
   *
   * @return  the grid spacing(in degrees)
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
}