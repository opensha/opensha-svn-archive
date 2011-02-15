package org.opensha.ui.components;

import java.net.URL;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

/**
 * Wrapper class for easy access to shared resources used by OpenSHA
 * applications such as icons and license files etc.
 * 
 * @author Peter Powers
 * @version $Id$
 */
public class Resources {

	private static String resPath = "/resources/";
	private static String imgPath = resPath + "images/";
	private static URL license;

	static {
		license = Resources.class.getResource(resPath + "LICENSE.html");
	}

	/**
	 * Returns the <code>URL</code> of the OpenSHA license/disclaimer file.
	 * @return the license/disclaier <code>URL</code>
	 */
	public static URL getLicense() {
		return license;
	}

	/**
	 * Retruns the 64x64 pixel OpenSHA logo.
	 * @return the logo
	 */
	public static Icon getLogo64() {
		return new ImageIcon(Resources.class.getResource(imgPath +
			"logos/opensha_64.png"));
	}
	
	// TODO clean
	public static void main(String[] args) {
		JFrame f = new JFrame();
		JLabel l = new JLabel(getLogo64());
		f.add(l);
		f.pack();
		f.setVisible(true);
	}
}
