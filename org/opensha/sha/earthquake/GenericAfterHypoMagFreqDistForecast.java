package org.opensha.sha.earthquake;

import org.opensha.sha.earthquake.*;
import org.opensha.sha.earthquake.GriddedHypoMagFreqDistAtLocAPI;
import org.opensha.data.region.EvenlyGriddedGeographicRegionAPI;
import java.util.ListIterator;
import org.opensha.data.Location;
import java.util.ArrayList;
import org.opensha.calc.RelativeLocation;
import org.opensha.sha.fault.*;
import org.opensha.sha.earthquake.observedEarthquake.ObsEqkRupture;


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
public class GenericAfterHypoMagFreqDistForecast
    extends AfterShockHypoMagFreqDistFoecast implements GriddedHypoMagFreqDistAtLocAPI{

  private double a_value = -1.67;
  private double b_value = 0.91;
  private double c_value = 0.05;
  private double p_value = 1.08;
  private double minForecastMag = 4.0;
  private double maxForecastMag = 8.0;
  private double nodeCompletenessMag;
  private SimpleFaultData mainshockFault;


  /**
   * set_ReasenbergJonesParms
   */
  public void set_ReasenbergJonesParms(double[] rjParms) {
    a_value = rjParms[0];
    b_value = rjParms[1];
    c_value = rjParms[2];
    p_value = rjParms[3];
  }





  /**
   * set_k_value
   * This will taper the generic k value
   */
  public void set_k_value() {
    double sumInvDist = 0;

    EvenlyGriddedGeographicRegionAPI aftershockZone = getAfterShockZone();
    int numLocs = aftershockZone.getNumGridLocs();
    double[] nodeTaper_k =  new double[numLocs];
    double[] nodeDistFromFault = new double[numLocs];
    double[] invDist = new double[numLocs];
    double[] nodePerc = new double[numLocs];

    //get the iterator of all the locations within that region
    ListIterator it = aftershockZone.getGridLocationsIterator();
    int ind = 0;
    FaultTrace faultTrace = mainshockFault.getFaultTrace();
    int numFaultPoints = faultTrace.size();
    double totDistFromFault = 0;
    while (it.hasNext()) {
      nodeDistFromFault[ind++] = RelativeLocation.getApproxHorzDistToLine( (
          Location) (it.next()), faultTrace.getLocationAt(0),
          faultTrace.getLocationAt(numFaultPoints));
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

    ObsEqkRupture mainshock = getMainShock();
    double mainshockMag = mainshock.getMag();
    double rightSide = a_value + b_value * (mainshockMag - nodeCompletenessMag);
    double generic_k = Math.pow(10,rightSide);
    for (int indLoop = 0; indLoop < ind -1; ++indLoop) {
         nodeTaper_k[indLoop] = generic_k * nodePerc[indLoop];
    };



  }

  /**
   * get_a_value
   */
  public double get_a_value() {
    return a_value;
  }

  /**
   * get_b_value
   */
  public double get_b_value() {
    return b_value;
  }

  /**
   * get_c_value
   */
  public double get_c_value() {
    return c_value;
  }

  /**
   * get_p_value
   */
  public double get_p_value() {
    return p_value;
  }




  public GenericAfterHypoMagFreqDistForecast(ObsEqkRupture mainShock,
      EvenlyGriddedGeographicRegionAPI afterShockZone, double minMag,
      double deltaMag, int numMag) {

  }



  /**
   * calc_NodeCompletenessMag
   * fore the generic model set this to the minimum
   * forecast magnitude
   */
  private void calc_NodeCompletenessMag() {
    nodeCompletenessMag = minForecastMag;
  }

  /**
   * set_minForecastMag
   */
  public void set_minForecastMag(double min_forecastMag) {
  minForecastMag = min_forecastMag;
  }

  /**
   * set_maxForecastMag
   */
  public void set_maxForecastMag(double max_forecastMag) {
  maxForecastMag = max_forecastMag;
  }

  /**
   * get_minForecastMag
   */
  public double get_minForecastMag() {
    return minForecastMag;
  }

  /**
   * get_maxForecastMag
   */
  public double get_maxForecastMag() {
    return maxForecastMag;
  }

  /**
   * set_completenessMag
   */
  public void set_completenessMag() {
    calc_NodeCompletenessMag();
  }

}
