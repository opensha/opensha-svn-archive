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

import java.util.NoSuchElementException;

/**
 * This enum stores preferences for servers that OpenSHA connects to. This includes hostnames
 * for servers such as apache tomcat, port numbers for RMI, and the current build type.
 * 
 * To get the server prefs instance in use by this build, use <code>ServerPrefUtils.SERVER_PREFS</code>.
 * 
 * @author kevin
 * @see ServerPrefUtils
 *
 */
public enum ServerPrefs {
	
	/**
	 * Preferences for development (trunk)
	 */
	DEV_PREFS(ServerPrefUtils.OPENSHA_SERVER_DEV_HOST,
			ServerPrefUtils.OPENSHA_SERVLET_DEV_URL,
			ServerPrefUtils.OPENSHA_TOMCAT_DEV_DIR,
			ServerPrefUtils.OPENSHA_RMI_DEV_PORT,
			DevStatus.DEVELOPMENT),
	/**
	 * Preferences for stable production releases
	 */
	PRODUCTION_PREFS(ServerPrefUtils.OPENSHA_SERVER_PRODUCTION_HOST,
			ServerPrefUtils.OPENSHA_SERVLET_PRODUCTION_URL,
			ServerPrefUtils.OPENSHA_TOMCAT_PRODUCTION_DIR,
			ServerPrefUtils.OPENSHA_RMI_PRODUCTION_PORT,
			DevStatus.PRODUCTION);
	
	private String hostName;
	private String servletURL;
	private String tomcatDir;
	private int rmiPort;
	private DevStatus buildType;
	
	private ServerPrefs(String hostName, String servletURL, String tomcatDir, int rmiPort, DevStatus buildType) {
		this.hostName = hostName;
		this.servletURL = servletURL;
		this.tomcatDir = tomcatDir;
		this.rmiPort = rmiPort;
		this.buildType = buildType;
	}

	/**
	 * The the base URL for servlets, for example: http://opensha.usc.edu:8080/OpenSHA/
	 * 
	 * @return servlet base URL
	 */
	public String getServletBaseURL() {
		return servletURL;
	}
	
	/**
	 * 
	 * @return path to the WEB-INF dir for this tomcat configuration
	 */
	public String getTomcatDir() {
		return tomcatDir;
	}
	
	/**
	 * The RMI port used for all RMI applications. This will differ based if this is a development
	 * or production release.
	 * 
	 * @return RMI port number
	 */
	public int getRMIPort() {
		return rmiPort;
	}
	
	/**
	 * The base URL for all RMI lookups 
	 * 
	 * @return RMI base URL
	 */
	public String getRMIBaseURL() {
		return "rmi://"+hostName+":"+rmiPort+"/";
	}

	/**
	 * String designating the build type, such as "nightly" or "dist".
	 * 
	 * @return build type string
	 */
	public DevStatus getBuildType() {
		return buildType;
	}
	
	/**
	 * The host name of the server being used, such as "opensha.usc.edu"
	 * 
	 * @return server host hame
	 */
	public String getHostName() {
		return hostName;
	}
	
	/**
	 * The minimum socket port used by RMI
	 * 
	 * @return min socket port used by RMI
	 */
	public int getMinRMISocketPort() {
		return ServerPrefUtils.OPENSHA_RMI_SOCKET_MIN_PORT;
	}
	
	/**
	 * The maximum socket port used by RMI
	 * 
	 * @return max socket port used by RMI
	 */
	public int getMaxRMISocketPort() {
		return ServerPrefUtils.OPENSHA_RMI_SOCKET_MAX_PORT;
	}
	
	/**
	 * Returns the server type with the given build type string
	 * 
	 * @param buildType
	 * @return
	 */
	public static ServerPrefs fromBuildType(String buildType) {
		for (ServerPrefs prefs : ServerPrefs.values()) {
			if (prefs.getBuildType().equals(buildType))
				return prefs;
		}
		throw new NoSuchElementException("No ServerPrefs instance exists with build type '" + buildType + "'");
	}
	
}
