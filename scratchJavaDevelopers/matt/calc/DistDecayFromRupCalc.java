package scratchJavaDevelopers.matt.calc;

import java.util.*;

import org.opensha.commons.calc.RelativeLocation;
import org.opensha.data.*;
import org.opensha.data.region.*;
import org.opensha.sha.earthquake.observedEarthquake.*;
import org.opensha.sha.faultSurface.*;

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
  private static double decayParam = 2.0;

  public DistDecayFromRupCalc() {
  }

  public static double[] getDensity(FaultTrace faultTrace,
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
    double totDistFromFault = 0;

    // get the summed squared distance to all nodes from the fault trace
    while (zoneIT.hasNext()) {
      nodeDistFromFault[ind++] =
          faultTrace.getHorzDistToClosestLocation( (Location) zoneIT.next());
      totDistFromFault = totDistFromFault +
          Math.pow(nodeDistFromFault[ind - 1], decayParam);
    }
    for (int indLoop = 0; indLoop < numLocs; ++indLoop) {
      invDist[indLoop] = totDistFromFault /
          Math.pow(nodeDistFromFault[indLoop], decayParam);
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
  public static double[] getDensity(ObsEqkRupture mainshock,
                             EvenlyGriddedGeographicRegionAPI aftershockZone) {
    Location pointRupture;
    Location gLoc;
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
      // this is a point source fault so get the sum squared distance
      // from all grid nodes to the point source.
      pointRupture = mainshock.getHypocenterLocation();
      while (zoneIT.hasNext()) {
        nodeDistFromFault[ind++] =
            RelativeLocation.getApproxHorzDistance(pointRupture,
            (Location) zoneIT.next());
        totDistFromFault = totDistFromFault +
            Math.pow(nodeDistFromFault[ind - 1], decayParam);
      }
    }
    else {
      // this is a rupture surface.  get  the sum squared distance from
      // all grid nodes to the rupture surface.
      EvenlyGriddedSurfaceAPI ruptureSurface = mainshock.getRuptureSurface();

      while (zoneIT.hasNext()) {
    	  gLoc = (Location) zoneIT.next();
    	  if (gLoc!=null){
    		  nodeDistFromFault[ind++] = getRupDist(ruptureSurface,gLoc);
                                        //      (Location) zoneIT.next());
    		  totDistFromFault = totDistFromFault +
    		  	Math.pow(nodeDistFromFault[ind - 1], decayParam);
    	    	  }
    	  else
    		  nodeDistFromFault[ind++] = -1.0;
      }
    }

    for (int indLoop = 0; indLoop < numLocs; ++indLoop) {
    	if (nodeDistFromFault[indLoop] > 0){
    		invDist[indLoop] = totDistFromFault /
    			Math.pow(nodeDistFromFault[indLoop], decayParam);
    		sumInvDist = sumInvDist + invDist[indLoop];
    	}
    }

    for (int indLoop = 0; indLoop < ind - 1; ++indLoop) {
    	if (nodeDistFromFault[indLoop] > 0)
    		nodePerc[indLoop] = invDist[indLoop] / sumInvDist;
    	else
    		nodePerc[indLoop] = 0;
    }

    return nodePerc;
  }

  /**
   * getRupDist
   */
  private static double getRupDist(EvenlyGriddedSurfaceAPI ruptureSurface, Location gridLoc) {
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

  /**
   * setDecayParam
   * set the exponent to be used for calculating the decay.  Default is 2.
   */
  public void setDecayParam(double decayParam) {
    this.decayParam = decayParam;
  }

  /**
   * getDecayParam
   * return the exponent that is used for calculating the decay.
   */
  public double getDecayParam() {
    return this.decayParam;
  }

}
