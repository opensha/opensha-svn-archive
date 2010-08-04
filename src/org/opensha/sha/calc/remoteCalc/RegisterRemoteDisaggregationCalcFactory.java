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

package org.opensha.sha.calc.remoteCalc;

import java.rmi.Naming;

import org.opensha.commons.util.ServerPrefUtils;

/**
 * <p>Title: RegisterRemoteDisaggregationCalcFactory</p>
 * <p>Description: This class creates a RMI server that will listen all the
 * RMI request for disaggregation calculation coming on to the server.</p>
 * @author : Ned (Edward) Field, Nitin Gupta and Vipin Gupta
 * @version 1.0
 */

public class RegisterRemoteDisaggregationCalcFactory {

  public final static String registrationName =
	  ServerPrefUtils.SERVER_PREFS.getRMIBaseURL()+"DisaggregationCalc_FactoryServer";


   public static void main(String[] args) {
     try {
       // register the Disaggregation Calculator with the naming service
       RemoteDisaggregationCalcFactoryAPI disaggCalcServer = new RemoteDisaggregationCalcFactoryImpl();
       Naming.rebind(registrationName, disaggCalcServer);
       System.out.println("Registered Disaggregation Calc Factory Server as " + registrationName);
     }
     catch (Exception e) {
       System.out.println("exception in starting server");
       e.printStackTrace();
       e.getMessage();
       return;
     }

 }
}
