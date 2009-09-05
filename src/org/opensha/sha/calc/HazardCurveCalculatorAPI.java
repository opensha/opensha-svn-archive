package org.opensha.sha.calc;

import java.rmi.Remote;
import java.util.ArrayList;
import java.util.ListIterator;

import org.opensha.commons.data.Site;
import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.commons.data.function.DiscretizedFuncAPI;
import org.opensha.commons.param.ParameterList;
import org.opensha.sha.earthquake.EqkRupForecastAPI;
import org.opensha.sha.imr.ScalarIntensityMeasureRelationshipAPI;
import org.opensha.sha.earthquake.EqkRupture;


/**
 * <p>Title: HazardCurveCalculatorAPI</p>
 * <p>Description: Defines the interface for the HazardCurveCalculator.</p>
 * @author : Ned (Edward) Field, Nitin Gupta and Vipin Gupta
 * @version 1.0
 */

public interface HazardCurveCalculatorAPI extends Remote{
	
	  /**
	  *
	  * @returns the adjustable ParameterList
	  */
	 public ParameterList getAdjustableParams()  throws java.rmi.RemoteException;

	 /**
	  * get the adjustable parameters
	  *
	  * @return
	  */
	 public ListIterator getAdjustableParamsIterator()  throws java.rmi.RemoteException;


  /**
   * This sets the maximum distance of sources to be considered in the calculation
   * (as determined by the getMinDistance(Site) method of ProbEqkSource subclasses).
   * Sources more than this distance away are ignored.
   * Default value is 250 km.
   *
   * @param distance: the maximum distance in km
   */
  public void setMaxSourceDistance(double distance) throws java.rmi.RemoteException;
  
  public double getMaxSourceDistance()  throws java.rmi.RemoteException;
  
  /**
   * This sets the mag-dist filter function, and also sets 
   * the value of includeMagDistFilterParam as true
   * @param magDistfunc
   */
  public void setMagDistCutoffFunc(ArbitrarilyDiscretizedFunc magDistfunc)  throws java.rmi.RemoteException;
 
  public void setNumStochEventSetRealizations(int numRealizations) throws java.rmi.RemoteException;
  
  public void setIncludeMagDistCutoff(boolean include)  throws java.rmi.RemoteException;
  
  public ArbitrarilyDiscretizedFunc getMagDistCutoffFunc()  throws java.rmi.RemoteException;



	/**
	 * Returns the Annualized Rates for the Hazard Curves 
	 * @param hazFunction Discretized Hazard Function
	 * @return
	 */
	  public DiscretizedFuncAPI getAnnualizedRates(DiscretizedFuncAPI hazFunction,double years) 
	  throws java.rmi.RemoteException;

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
                             Site site, ScalarIntensityMeasureRelationshipAPI imr, EqkRupForecastAPI eqkRupForecast)
      throws java.rmi.RemoteException ;



  /**
   * This function computes a hazard curve for the given Site, IMR, and ProbEqkrupture.  The curve
   * in place in the passed in hazFunction (with the X-axis values being the IMLs for which
   * exceedance probabilites are desired).
   * @param hazFunction: This function is where the hazard curve is placed
   * @param site: site object
   * @param imr: selected IMR object
   * @param rupture: Single Earthquake Rupture
   * @return
   */
  public DiscretizedFuncAPI getHazardCurve(DiscretizedFuncAPI
      hazFunction,
      Site site, ScalarIntensityMeasureRelationshipAPI imr, EqkRupture rupture) throws
      java.rmi.RemoteException;



  //gets the current rupture that is being processed
  public int getCurrRuptures() throws java.rmi.RemoteException;

  //gets the total number of ruptures.
  public int getTotRuptures() throws java.rmi.RemoteException;

  /**
   * stops the Hazard Curve calculations.
   * @throws java.rmi.RemoteException
   */
  public void stopCalc() throws java.rmi.RemoteException;
  
  /**
   * This function computes an average hazard curve from a number of stochastic event sets
   * for the given Site, IMR, eqkRupForecast, where the number of event-set realizations
   * is specified as the value in numStochEventSetRealizationsParam. The passed in 
   * discretized function supplies the x-axis values (the IMLs) 
   * for the computation, and the result (probability) is placed in the 
   * y-axis values of this function. This always applies a rupture distance 
   * cutoff using the value of the maxDistanceParam parameter (set to a very high 
   * value if you don't want this).  This does not (yet?) apply the magnitude-dependent 
   * distance cutoff represented by includeMagDistFilterParam and magDistCutoffParam.
   * @param hazFunction: This function is where the hazard curve is placed
   * @param site: site object
   * @param imr: selected IMR object
   * @param eqkRupForecast: selected Earthquake rup forecast
   * @return
   */
  public DiscretizedFuncAPI getAverageEventSetHazardCurve(DiscretizedFuncAPI hazFunction,
		  Site site, ScalarIntensityMeasureRelationshipAPI imr, 
		  EqkRupForecastAPI eqkRupForecast)
  		throws java.rmi.RemoteException;
  
  /**
   * This function computes a hazard curve for the given Site, IMR, and event set
   * (eqkRupList), where it is assumed that each of the events occur (probability 
   * of each is 1.0). The passed in discretized function supplies the x-axis values 
   * (the IMLs) for the computation, and the result (probability) is placed in the 
   * y-axis values of this function. This always applies a rupture distance 
   * cutoff using the value of the maxDistanceParam parameter (set to a very high 
   * value if you don't want this).  This does not (yet?) apply the magnitude-dependent 
   * distance cutoff represented by includeMagDistFilterParam and magDistCutoffParam.
   * @param hazFunction: This function is where the hazard curve is placed
   * @param site: site object
   * @param imr: selected IMR object
   * @param eqkRupForecast: selected Earthquake rup forecast
   * @param updateCurrRuptures: tells whether to update current ruptures (for the getCurrRuptures() method used for progress bars)
   * @return
   */
  public DiscretizedFuncAPI getEventSetHazardCurve(DiscretizedFuncAPI hazFunction,
		  Site site, ScalarIntensityMeasureRelationshipAPI imr, 
		  ArrayList<EqkRupture> eqkRupList, boolean updateCurrRuptures)
  throws java.rmi.RemoteException;


}
