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

package org.opensha.commons.util;

public class Preferences {
	
	/**
	 * This is the URL to the production OpenSHA servlets.
	 */
	private static final String OPENSHA_SERVLET_PRODUCTION_URL = "http://opensha.usc.edu:8080/OpenSHA/";
	
	/**
	 * This is the URL to the development OpenSHA servlets
	 */
	private static final String OPENSHA_SERVLET_DEV_URL = "http://opensha.usc.edu:8080/OpenSHA_dev/";
	
	/**
	 * This is the URL in use for OpenSHA servlets...it should always be link to the production URL
	 * when applications are final and being distributed, the the development URL should be used when
	 * changes are being made that would break the currently released apps.
	 */
	public static final String OPENSHA_SERVLET_URL = OPENSHA_SERVLET_DEV_URL;
}
