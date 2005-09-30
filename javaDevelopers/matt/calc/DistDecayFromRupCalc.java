package javaDevelopers.matt.calc;

import org.opensha.data.Location;
import java.util.ListIterator;
import org.opensha.calc.RelativeLocation;
import org.opensha.sha.earthquake.observedEarthquake.ObsEqkRupture;
import org.opensha.sha.surface.EvenlyGriddedSurface;
import org.opensha.data.region.EvenlyGriddedGeographicRegionAPI;
import org.opensha.sha.surface.GriddedSurfaceAPI;
import org.opensha.sha.fault.FaultTrace;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2002</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public final class DistDecayFromRupCalc {
  public DistDecayFromRupCalc() {
  }

  public double[] getDensity(FaultTrace faultTrace,
                             EvenlyGriddedGeographicRegionAPI aftershockZone) {
    double[] nodePerc = null;
    double sumInvDist = 0;

    int numLocs = aftershockZone.getNumGridLocs();
    double[] nodeDistFromFault = new double[numLocs];
    double[] invDist = new double[numLocs];
    nodePerc = new double[numLocs];

    //get the iterator of all the locations within that region
    ListIterator zoneIT = aftershockZone.getGridLocationsIterator();
    int ind = 0;
    // this may not be correct
    double totDistFromFault = 0;
    while (zoneIT.hasNext()) {
      nodeDistFromFault[ind++] =
          faultTrace.getHorzDistToClosestLocation( (Location) zoneIT.next());
      totDistFromFault = totDistFromFault +
          Math.pow(nodeDistFromFault[ind - 1], 2.0);
    }
    for (int indLoop = 0; indLoop < numLocs; ++indLoop) {
      invDist[indLoop] = totDistFromFault /
          Math.pow(nodeDistFromFault[indLoop], 2.0);
      sumInvDist = sumInvDist + invDist[indLoop];
    }

    for (int indLoop = 0; indLoop < ind - 1; ++indLoop) {
      nodePerc[indLoop] = invDist[indLoop] / sumInvDist;
    }

    return nodePerc;
  }

  /**
   * setNodePerc
   * This will taper assign a percentage of the k value that should
   * be assigned to each grid node.
   */
  public double[] getDensity(ObsEqkRupture mainshock,
                             EvenlyGriddedGeographicRegionAPI aftershockZone) {
    Location pointRupture;
    double[] nodePerc = null;

    //get the iterator of all the locations within that region
    ListIterator zoneIT = aftershockZone.getGridLocationsIterator();
    int ind = 0;
    double totDistFromFault = 0;
    double sumInvDist = 0;
    int numLocs = aftershockZone.getNumGridLocs();
    double[] nodeDistFromFault = new double[numLocs];
    double[] invDist = new double[numLocs];
    nodePerc = new double[numLocs];

    if (mainshock.getRuptureSurface() == null) {
      pointRupture = mainshock.getHypocenterLocation();
      while (zoneIT.hasNext()) {
        nodeDistFromFault[ind++] =
            RelativeLocation.getApproxHorzDistance(pointRupture,
            (Location) zoneIT.next());
        totDistFromFault = totDistFromFault +
            Math.pow(nodeDistFromFault[ind - 1], 2.0);
      }
    }
    else {
      GriddedSurfaceAPI ruptureSurface = mainshock.getRuptureSurface();

      while (zoneIT.hasNext()) {
        nodeDistFromFault[ind++] = getRupDist(ruptureSurface,
                                              (Location) zoneIT.next());
        totDistFromFault = totDistFromFault +
            Math.pow(nodeDistFromFault[ind - 1], 2.0);
      }
    }

    for (int indLoop = 0; indLoop < numLocs; ++indLoop) {
      invDist[indLoop] = totDistFromFault /
          Math.pow(nodeDistFromFault[indLoop], 2.0);
      sumInvDist = sumInvDist + invDist[indLoop];
    }

    for (int indLoop = 0; indLoop < ind - 1; ++indLoop) {
      nodePerc[indLoop] = invDist[indLoop] / sumInvDist;
    }

    return nodePerc;
  }

  /**
   * getRupDist
   */
  private double getRupDist(GriddedSurfaceAPI ruptureSurface, Location gridLoc) {
    int ind = 0;
    double nodeDistFromRup, minDistFromRup = 0;
    ListIterator rupIT = ruptureSurface.listIterator();
    while (rupIT.hasNext()) {
      nodeDistFromRup = RelativeLocation.getApproxHorzDistance(
          (Location) (rupIT.next()), gridLoc);
      if (ind == 0) {
        minDistFromRup = nodeDistFromRup;
        ind++;
      }
      else {
        if (nodeDistFromRup < minDistFromRup) {
          minDistFromRup = nodeDistFromRup;
        }
      }
    }
    return minDistFromRup;
  }

}
