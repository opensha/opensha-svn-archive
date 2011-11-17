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
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.opensha.commons.data.Site;
import org.opensha.commons.data.function.DiscretizedFunc;
import org.opensha.commons.param.ParameterList;
import org.opensha.sha.earthquake.ERF;
import org.opensha.sha.earthquake.EqkRupture;
import org.opensha.sha.faultSurface.utils.PtSrcDistCorr;
import org.opensha.sha.imr.ScalarIMR;

public interface SpectrumCalculatorAPI extends Remote{
	/**
	 *
	 * @return the current rupture being traversed
	 * @throws java.rmi.RemoteException
	 */
	public int getCurrRuptures() throws RemoteException;

	/**
	 * This function computes a deterministic exceedance curve for the given Site, IMR, and ProbEqkrupture.  The curve
	 * in place in the passed in hazFunction (with the X-axis values being the IMLs for which
	 * exceedance probabilites are desired).
	 * @param hazFunction: This function is where the hazard curve is placed
	 * @param site: site object
	 * @param imr: selected IMR object
	 * @param rupture: Single Earthquake Rupture
	 * @return
	 */
	public DiscretizedFunc getDeterministicSpectrumCurve(Site site,
			ScalarIMR imr, EqkRupture rupture, boolean probAtIML,
			double imlProbVal) throws RemoteException;

	/**
	 * This function computes a spectrum curve for all SA Period supported
	 * by the IMR and then interpolates the IML value from all the computed curves.
	 * The curve in place in the passed in hazFunction
	 * (with the X-axis values being the IMLs for which exceedance probabilites are desired).
	 * @param specFunction: This function is where the final interplotaed spectrum
	 * for the IML@prob curve is placed.
	 * @param site: site object
	 * @param imr: selected IMR object
	 * @param eqkRupForecast: selected Earthquake rup forecast
	 * @return
	 */
	public DiscretizedFunc getIML_SpectrumCurve(DiscretizedFunc
			spectrumFunction, Site site,
			ScalarIMR imr,
			ERF
			eqkRupForecast, double probVal,
                                                 List supportedSA_Periods) throws
			RemoteException;

	/**
	 * This function computes a spectrum curve for the given Site, IMR, and ERF.  The curve
	 * in place in the passed in hazFunction (with the X-axis values being the SA
	 * Periods for which exceedance probabilites are desired).
	 * @param hazFunction: This function is where the hazard curve is placed
	 * @param site: site object
	 * @param imr: selected IMR object
	 * @param eqkRupForecast: selected Earthquake rup forecast
	 * @return
	 */
	public DiscretizedFunc getSpectrumCurve(Site site,
			ScalarIMR imr,
			ERF eqkRupForecast,
			double imlVal,
                                             List supportedSA_Periods)
	throws RemoteException;

	/**
	 *
	 * @return the total number of ruptures in the earthquake rupture forecast model
	 * @throws java.rmi.RemoteException
	 */
	public int getTotRuptures() throws RemoteException;

	/**
	 * This sets the maximum distance of sources to be considered in the calculation
	 * (as determined by the getMinDistance(Site) method of ProbEqkSource subclasses).
	 * Sources more than this distance away are ignored.
	 * Default value is 250 km.
	 *
	 * @param distance: the maximum distance in km
	 */
	public void setMaxSourceDistance(double distance) throws RemoteException;

	public double getMaxSourceDistance()  throws java.rmi.RemoteException;

	/**
	 *
	 * @return This was created so new instances of this calculator could be
	 * given pointers to a set of parameter that already exist.
	 */
	public void setAdjustableParams(ParameterList paramList)  throws java.rmi.RemoteException;


	/**
	 *
	 * @return the adjustable ParameterList
	 */
	public ParameterList getAdjustableParams()  throws java.rmi.RemoteException;

	/**
	 * get the adjustable parameters
	 *
	 * @return
	 */
	public ListIterator getAdjustableParamsIterator()  throws java.rmi.RemoteException;

	/**
	 * This sets the type of point-source distance correction that is desired
	 * (see the class PtSrcDistCorr for options)
	 * @param ptSrcDistCorrType
	 */
	public void setPtSrcDistCorrType(PtSrcDistCorr.Type ptSrcDistCorrType)  throws java.rmi.RemoteException;

	/**
	 * This gets the type of point-source distance correction that is desired
	 * (see the class PtSrcDistCorr for options)
	 * @param ptSrcDistCorrType
	 */
	public PtSrcDistCorr.Type getPtSrcDistCorrType()  throws java.rmi.RemoteException;


}
