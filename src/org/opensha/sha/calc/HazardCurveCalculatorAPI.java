/*******************************************************************************
 * Copyright 2009 OpenSHA.org in partnership with
 * the Southern California Earthquake Center (SCEC, http://www.scec.org)
 * at the University of Southern California and the UnitedStates Geological
 * Survey (USGS; http://www.usgs.gov)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package org.opensha.sha.calc;

import java.rmi.Remote;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ListIterator;

import org.opensha.commons.data.Site;
import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.commons.data.function.DiscretizedFunc;
import org.opensha.commons.param.Parameter;
import org.opensha.commons.param.ParameterList;
import org.opensha.sha.earthquake.ERF;
import org.opensha.sha.earthquake.EqkRupture;
import org.opensha.sha.imr.ScalarIMR;
import org.opensha.sha.util.TectonicRegionType;


/**
 * <p>Title: HazardCurveCalculatorAPI</p>
 * <p>Description: Defines the interface for the HazardCurveCalculator.</p>
 * @author : Ned (Edward) Field, Nitin Gupta and Vipin Gupta
 * @version 1.0
 */

public interface HazardCurveCalculatorAPI extends Remote{

	/**
	 * Get the adjustable parameter list of calculation parameters
	 *
	 * @return the adjustable ParameterList
	 * @throws java.rmi.RemoteException
	 */
	public ParameterList getAdjustableParams()  throws java.rmi.RemoteException;

	/**
	 * Get iterator for the adjustable parameters
	 *
	 * @return parameter iterator
	 * @throws java.rmi.RemoteException
	 */
	public ListIterator<Parameter<?>> getAdjustableParamsIterator()  throws java.rmi.RemoteException;


	/**
	 * This sets the maximum distance of sources to be considered in the calculation.
	 * Sources more than this distance away are ignored.  This is simply a direct
	 * way of setting the parameter.
	 * Default value is 250 km.
	 *
	 * @param distance: the maximum distance in km
	 * @throws java.rmi.RemoteException
	 */
	public void setMaxSourceDistance(double distance) throws java.rmi.RemoteException;

	/**
	 * This is a direct way of getting the distance cutoff from that parameter
	 * 
	 * @return max source distance
	 * @throws java.rmi.RemoteException
	 */
	public double getMaxSourceDistance()  throws java.rmi.RemoteException;

	/**
	 * This sets the mag-dist filter function (distance on x-axis and 
	 * mag on y-axis), and also sets the value of includeMagDistFilterParam as true
	 * 
	 * @param magDistfunc function to set
	 * @throws java.rmi.RemoteException
	 */
	public void setMagDistCutoffFunc(ArbitrarilyDiscretizedFunc magDistfunc)  throws java.rmi.RemoteException;

	/**
	 * Set the number of stochastic event set realizations for average event set hazard
	 * curve calculation. This simply sets the <code>NumStochasticEventSetsParam</code>
	 * parameter.
	 * 
	 * @param numRealizations number of stochastic event set realizations
	 * @throws java.rmi.RemoteException
	 */
	public void setNumStochEventSetRealizations(int numRealizations) throws java.rmi.RemoteException;

	/**
	 * Sets the <code>IncludeMagDistFilterParam</code> parameter, which determines if the
	 * magnitude/distance filter is used in calculation.
	 * 
	 * @param include if true, the magnitude/distance filter is included
	 * @throws java.rmi.RemoteException
	 */
	public void setIncludeMagDistCutoff(boolean include)  throws java.rmi.RemoteException;

	/**
	 * This gets the mag-dist filter function (distance on x-axis and 
	 * mag on y-axis), returning null if the includeMagDistFilterParam
	 * has been set to false.
	 * 
	 * @return  mag-dist filter function
	 * @throws java.rmi.RemoteException
	 */
	public ArbitrarilyDiscretizedFunc getMagDistCutoffFunc()  throws java.rmi.RemoteException;

	/**
	 * This was created so new instances of this calculator could be
	 * given pointers to a set of parameter that already exist.
	 * 
	 * @param paramList parameters to be set
	 * @throws java.rmi.RemoteException
	 */
	public void setAdjustableParams(ParameterList paramList)  throws java.rmi.RemoteException;



	/**
	 * Returns the Annualized Rates for the Hazard Curves 
	 * 
	 * @param hazFunction Discretized Hazard Function
	 * @return annualized rates for the given hazard function
	 * @throws java.rmi.RemoteException
	 */
	public DiscretizedFunc getAnnualizedRates(DiscretizedFunc hazFunction,double years) 
	throws java.rmi.RemoteException;

	/**
	 * This function computes a hazard curve for the given Site, IMR, ERF, and 
	 * discretized function, where the latter supplies the x-axis values (the IMLs) for the 
	 * computation, and the result (probability) is placed in the y-axis values of this function.
	 * This always applies a source and rupture distance cutoff using the value of the
	 * maxDistanceParam parameter (set to a very high value if you don't want this).  It also 
	 * applies a magnitude-dependent distance cutoff on the sources if the value of 
	 * includeMagDistFilterParam is "true" and using the function in magDistCutoffParam.
	 * @param hazFunction: This function is where the hazard curve is placed
	 * @param site: site object
	 * @param imr: selected IMR object
	 * @param eqkRupForecast: selected Earthquake rup forecast
	 * @return
	 */
	public DiscretizedFunc getHazardCurve(DiscretizedFunc hazFunction,
			Site site, ScalarIMR imr, ERF eqkRupForecast)
	throws java.rmi.RemoteException ;

	/**
	 * This function computes a hazard curve for the given Site, imrMap, ERF, and 
	 * discretized function, where the latter supplies the x-axis values (the IMLs) for the 
	 * computation, and the result (probability) is placed in the y-axis values of this function.
	 * 
	 * This always applies a source and rupture distance cutoff using the value of the
	 * maxDistanceParam parameter (set to a very high value if you don't want this).  It also 
	 * applies a magnitude-dependent distance cutoff on the sources if the value of 
	 * includeMagDistFilterParam is "true" and using the function in magDistCutoffParam.
	 * 
	 * The IMR will be selected on a source by source basis by the <code>imrMap</code> parameter.
	 * If the mapping only contains a single IMR, then that IMR will be used for all sources.
	 * Otherwise, if a mapping exists for the source's tectonic region type (TRT), then the IMR
	 * from that mapping will be used for that source. If no mapping exists, a NullPointerException
	 * will be thrown.
	 * 
	 * Once the IMR is selected, it's TRT paramter can be set by the soruce, depending
	 * on the <code>SetTRTinIMR_FromSourceParam</code> param and <code>NonSupportedTRT_OptionsParam</code>
	 * param. If <code>SetTRTinIMR_FromSourceParam</code> is true, then the IMR's TRT param will be set by
	 * the source (otherwise it will be left unchanged). If it is to be set, but the source's TRT is not
	 * supported by the IMR, then <code>NonSupportedTRT_OptionsParam</code> is used.
	 * 
	 * @param hazFunction: This function is where the hazard curve is placed
	 * @param site: site object
	 * @param imrMap this <code>Map<TectonicRegionType,ScalarIntensityMeasureRelationshipAPI></code>
	 * specifies which IMR to use with each tectonic region.
	 * @param eqkRupForecast selected Earthquake rup forecast
	 * @return hazard curve. Function passed in is updated in place, so this is just a pointer to
	 * the <code>hazFunction</code> param.
	 * @throws java.rmi.RemoteException
	 * @throws NullPointerException if there are multiple IMRs in the mapping, but no mapping exists for
	 * a soruce in the ERF.
	 */
	public DiscretizedFunc getHazardCurve(
			DiscretizedFunc hazFunction,
			Site site,
			Map<TectonicRegionType, ScalarIMR> imrMap, 
			ERF eqkRupForecast) throws java.rmi.RemoteException;


	/**
	 * This computes the "deterministic" exceedance curve for the given Site, IMR, and ProbEqkrupture
	 * (conditioned on the event actually occurring).  The hazFunction passed in provides the x-axis
	 * values (the IMLs) and the result (probability) is placed in the y-axis values of this function.
	 * @param hazFunction This function is where the deterministic hazard curve is placed
	 * @param site site object
	 * @param imr selected IMR object
	 * @param rupture Single Earthquake Rupture
	 * @return hazard curve. Function passed in is updated in place, so this is just a pointer to
	 * the <code>hazFunction</code> param.
	 * @throws java.rmi.RemoteException
	 */
	public DiscretizedFunc getHazardCurve(DiscretizedFunc
			hazFunction,
			Site site, ScalarIMR imr, EqkRupture rupture) throws
			java.rmi.RemoteException;



	/**
	 * gets the current rupture that is being processed
	 * 
	 * @returncurrent rupture that is being processed
	 * @throws java.rmi.RemoteException
	 */
	public int getCurrRuptures() throws java.rmi.RemoteException;

	/**
	 * gets the total number of ruptures.
	 * 
	 * @return total number of ruptures.
	 * @throws java.rmi.RemoteException
	 */
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
	 * 
	 * @param hazFunction This function is where the hazard curve is placed
	 * @param site site object
	 * @param imr selected IMR object
	 * @param eqkRupForecast selected Earthquake rup forecast
	 * @return hazard curve. Function passed in is updated in place, so this is just a pointer to
	 * the <code>hazFunction</code> param.
	 * @throws java.rmi.RemoteException
	 */
	public DiscretizedFunc getAverageEventSetHazardCurve(DiscretizedFunc hazFunction,
			Site site, ScalarIMR imr, 
			ERF eqkRupForecast)
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
	 * 
	 * @param hazFunction This function is where the hazard curve is placed
	 * @param site site object
	 * @param imr selected IMR object
	 * @param eqkRupForecast selected Earthquake rup forecast
	 * @param updateCurrRuptures tells whether to update current ruptures (for the getCurrRuptures() method used for progress bars)
	 * @return hazard curve. Function passed in is updated in place, so this is just a pointer to
	 * the <code>hazFunction</code> param.
	 * @throws java.rmi.RemoteException
	 */
	public DiscretizedFunc getEventSetHazardCurve(DiscretizedFunc hazFunction,
			Site site, ScalarIMR imr, 
			List<EqkRupture> eqkRupList, boolean updateCurrRuptures)
	throws java.rmi.RemoteException;

}
