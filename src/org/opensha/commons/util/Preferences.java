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
	public static final String OPENSHA_SERVLET_URL = OPENSHA_SERVLET_PRODUCTION_URL;
}
