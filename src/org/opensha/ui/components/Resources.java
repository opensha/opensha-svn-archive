package org.opensha.ui.components;

import java.net.URL;

/**
 * Wrapper class for easy access to shared resources used by OpenSHA
 * applications such as icons and license files etc.
 * 
 * @author Peter Powers
 * @version $Id:$
 */
public class Resources {

	private static URL license;
	
	static {
		license = Resources.class.getResource("/resources/LICENSE.html");
	}
	
	/**
	 * Returns the <code>URL</code> of the OpenSHA license/disclaimer file.
	 * @return the license/disclaier <code>URL</code>
	 */
	public static URL getLicense() {
		return license;
	}
}
