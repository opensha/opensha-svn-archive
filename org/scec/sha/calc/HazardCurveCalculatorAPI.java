package org.scec.sha.calc;

import java.rmi.Remote;

import org.scec.data.function.DiscretizedFuncAPI;
import org.scec.data.Site;
import org.scec.sha.earthquake.EqkRupForecastAPI;
import org.scec.sha.imr.AttenuationRelationshipAPI;


/**
 * <p>Title: HazardCurveCalculatorAPI</p>
 * <p>Description: Defines the interface for the HazardCurveCalculator.</p>
 * @author : Ned (Edward) Field, Nitin Gupta and Vipin Gupta
 * @version 1.0
 */

public interface HazardCurveCalculatorAPI extends Remote{

  /**
   * This sets the maximum distance of sources to be considered in the calculation
   * (as determined by the getMinDistance(Site) method of ProbEqkSource subclasses).
   * Sources more than this distance away are ignored.
   * Default value is 250 km.
   *
   * @param distance: the maximum distance in km
   */
  public void setMaxSourceDistance(double distance) throws java.rmi.RemoteException;




  /**
   * This function computes a hazard curve for the given Site, IMR, and ERF.  The curve
   * in place in the passed in hazFunction (with the X-axis values being the IMLs for which
   * exceedance probabilites are desired).
   * @param hazFunction: This function is where the hazard curve is placed
   * @param site: site object
   * @param imr: selected IMR object
   * @param eqkRupForecast: selected Earthquake rup forecast
   * @return
   */
  public DiscretizedFuncAPI getHazardCurve(DiscretizedFuncAPI hazFunction,
                             Site site, AttenuationRelationshipAPI imr, EqkRupForecastAPI eqkRupForecast)
      throws java.rmi.RemoteException ;


  //gets the current rupture that is being processed
  public int getCurrRuptures() throws java.rmi.RemoteException;

  //gets the total number of ruptures.
  public int getTotRuptures() throws java.rmi.RemoteException;

  /**
   * stops the Hazard Curve calculations.
   * @throws java.rmi.RemoteException
   */
  public void stopCalc() throws java.rmi.RemoteException;

}