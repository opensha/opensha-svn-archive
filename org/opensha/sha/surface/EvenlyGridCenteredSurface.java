package org.opensha.sha.surface;

import org.opensha.data.Location;


/**
 * <p>Title:EvenlyGridCenteredSurface </p>
 *
 * <p>Description:Creates a Evenly GridCentered Surface.
 * Creates EvenlyGriddedSurface that has one less
 * row and col then the original surface. It averages the 4 corner location
 * on each grid surface to get the grid centered location.
 </p>
 *
 * @author Edward Field, Nitin Gupta
 * @version 1.0
 */
public class EvenlyGridCenteredSurface
    extends EvenlyGriddedSurface {


  private EvenlyGriddedSurfaceAPI surface;

  /**
   * Class constructor that takes in a EvenGriddedSurface and computes a EvenlyGridCentered
   * Surface.
   * @param surface EvenlyGriddedSurface
   */
  public EvenlyGridCenteredSurface(EvenlyGriddedSurfaceAPI surface) {
    if (surface instanceof FrankelGriddedSurface)
      throw new UnsupportedOperationException(
          "Grid-Centered Surface not defined " +
          "for Frankel surface");
    this.surface = surface;
    this.gridSpacing = surface.getGridSpacing();
    getGridCenteredSurface();
  }


  /**
   * Returns the grid centered location on each grid surface.
   * @param surface EvenlyGriddedSurface surface for which grid centered surface
   * needs to be computed.
   *

   */
  private void getGridCenteredSurface() {

    int numRows = surface.getNumRows() - 1;
    int numCols = surface.getNumCols() - 1;
    setNumRowsAndNumCols(numRows, numCols);
    for (int i = 0; i < numRows; ++i) {
      for (int j = 0; j < numCols; ++j) {
        Location loc;
        Location loc1 = surface.getLocation(i, j);
        Location loc2 = surface.getLocation(i, j + 1);
        Location loc3 = surface.getLocation(i + 1, j);
        Location loc4 = surface.getLocation(i + 1, j + 1);
        double locLat = (loc1.getLatitude() + loc2.getLatitude() +
                         loc3.getLatitude() +
                         loc4.getLatitude()) / 4;
        double locLon = (loc1.getLongitude() + loc2.getLongitude() +
                         loc3.getLongitude() +
                         loc4.getLongitude()) / 4;
        double locDepth = (loc1.getDepth() + loc2.getDepth() + loc3.getDepth() +
                           loc4.getDepth()) / 4;
        loc = new Location(locLat, locLon, locDepth);
        setLocation(i, j, loc);
      }
    }
  }



  /** Returns the average strike of this surface on the Earth.  */
  public double getAveStrike() { return surface.getAveStrike(); }

  /** Returns the average dip of this surface into the Earth.  */
  public double getAveDip() { return surface.getAveDip(); }


}
