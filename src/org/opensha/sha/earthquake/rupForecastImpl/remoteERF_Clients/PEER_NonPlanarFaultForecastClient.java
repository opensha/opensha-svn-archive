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

package org.opensha.sha.earthquake.rupForecastImpl.remoteERF_Clients;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;

import org.opensha.sha.earthquake.rupForecastImpl.PEER_TestCases.PEER_NonPlanarFaultForecast;
import org.opensha.sha.earthquake.rupForecastImpl.remote.RegisterRemoteERF_Factory;
import org.opensha.sha.earthquake.rupForecastImpl.remote.RemoteERF_Client;

/**
 * <p>Title: PEER_NonPlanarFaultForecastClient </p>
 * <p>Description: This is a client for remote PEER Non Planar Fault Forecast instance. It will
 * access ERF from the remote machine </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Nitin Gupta, Vipin Gupta
 * @version 1.0
 */

public class PEER_NonPlanarFaultForecastClient extends RemoteERF_Client {
	
	public static final String NAME = PEER_NonPlanarFaultForecast.NAME;

	public PEER_NonPlanarFaultForecastClient() throws java.rmi.RemoteException, MalformedURLException, NotBoundException{
		String className = PEER_NonPlanarFaultForecast.class.getName();
		String remoteRegistrationName = RegisterRemoteERF_Factory.registrationURL;
		getRemoteERF(className,remoteRegistrationName);
	}




}
