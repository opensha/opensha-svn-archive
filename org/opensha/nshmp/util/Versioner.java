package org.opensha.nshmp.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

import javax.swing.JOptionPane;

public class Versioner {

	private static final String CLIENT_VERSION = GlobalConstants.getCurrentVersion();

	//private static final String PATH = 
		//"http://gldweb.cr.usgs.gov/GroundMotionTool/index.html";
	private static final String PATH = 
		"http://geohazards.cr.usgs.gov/GroundMotionTool/index.html";
		
	private static final String GET_CHANGES = 
		"http://earthquake.usgs.gov/research/hazmaps/design/updates.php";

//	private static String ERROR = 
//		"Connection failed.  Our servers may be temporarily down for maintenance.\n" + 
//		"If the problem persists please contact emartinez@usgs.gov for support.";

	private static String START_READ =
		"<!-- BEGIN RECENT REVISION -->";
	private static String END_READ = 
		"<!-- END RECENT REVISION -->";

	private static String START_READ_ALL =
		"<!-- BEGIN REVISION HISTORY -->";
	private static String END_READ_ALL = 
		"<!-- END REVISION HISTORY -->";

	private static String version = "UNKNOWN VERSION";
	private static String updates = "";
	private static String allUpdates = "";
	private static boolean connection = false;
////////////////////////////////////////////////////////////////////////////////////////////////////
//                                          CONSTRUCTORS                                          //
////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Constructor: Sets the values of version, updates, and connection
	 */
	public Versioner() {
		connection = false;
		updates = "";
		allUpdates = "";
		version = "UNKNOWN VERSION";
		setConnection(); // Sets the version and connection status

		if (!connection) {
			// Ask if they would like to try to use a proxy
			int ans = JOptionPane.showConfirmDialog(null, "Could not establish a " +
				"connection to the server.\nIf you use a proxy to connect and would " +
				"like to configure it now please click OK below.", "Connection Failure",
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.ERROR_MESSAGE);

			// If yes, then configure the proxy settings and try to connect again
			if (ans == JOptionPane.OK_OPTION) {
				AppConfigurer.setProxyConfig(true);
				setConnection();
			}

		} 
		setUpdates();    // Sets the updates
		setAllUpdates();

	}

////////////////////////////////////////////////////////////////////////////////////////////////////
//                                       PUBLIC FUNCTIONS                                         //
////////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Returns the value of connection
	 * @returns connection boolean true if successfully connected to server, false otherwise
	 */
	public boolean check() {
		return connection;
	}

	/**
	 * Returns the HTML formatted list of recent updates
	 * @return updates String An HTML formatted list of recent updates
	 */
	public String getUpdates() {
		return updates;
	}

	/**
	 * Returns the HTML formatted list of all updates
	 * @return allUpdates String An HTML formatted list of all updates
	 */
	public String getAllUpdates() {
		return allUpdates;
	}

	/**
	 * Returns the full client version string.  This is
	 * something like 'Version: 5.X.X - mm/dd/yyyy'
	 * @return CLIENT_VERSION String The current version the user is running
	 */
	public String getClientVersion() {
		return CLIENT_VERSION;
	}

	/**
	 * Returns the full server version string.  This is
	 * something like 'Version: 5.X.X - mm/dd/yyyy'
	 * @return version String The current version known to the server
	 */
	public String getServerVersion() {
		return version;
	}

	/**
	 * Return the client version string without the 'Version: '
	 * prepended to the number
	 * @return versionNumber String The version number and release date of the current version the user is running
	 */
	public String getClientVersionNumber() {
		return CLIENT_VERSION.substring(9); // Something like 5.x.x - mm/dd/yyyy
	}

	/**
	 * Return the server version string without the 'Version: '
	 * prepended to the number
	 * @return versionNumber String The version number and release date of the current version known to the server
	 */
	public String getServerVersionNumber() {
		return version.substring(9); // Something like 5.x.x - mm/dd/yyyy
	}

	/**
	 * Checks the version of the client the user is running against what the server
	 * knows to be the most recent version and returns as appropriate
	 * @return isCurrent boolean True if the client and server version match, false otherwise
	 */
	public boolean versionCheck() {
		return CLIENT_VERSION.equals(version);
	}

	/**   
	 * Creates the message to give the user when their client is out of date
	 * @return infoMessage String An HTML formatted message informing the user of the need to update their client
	 */
	public String getUpdateMessage() {
		String info = "<p>It appears your version of this application is out of date.<br><br>" +
		"You are currently running: " + CLIENT_VERSION.substring(9) + "<br>" +
		"The new version available is:  " + version.substring(9) + "<br><br>" +
		"Below is a list of the changes that have been made in this revision:</p>\n\n";
		info += updates;
		return info;
	}
////////////////////////////////////////////////////////////////////////////////////////////////////
//                                       PRIVATE FUNCTIONS                                        //
////////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Checks with the server and compares the client version
	 * with what the server knows to be the most recent version.
	 * @return connection boolean, true if connected successfully, else false
	 */
	private static void setConnection() {
		try {
			URL url = new URL(PATH);
			BufferedReader bin = new BufferedReader(
												new InputStreamReader(
												url.openStream()));
	
			while ( (version = bin.readLine()) != null ) {
				connection = true; // if we are reading the file, then connection succeeded.
				if (version.startsWith("<!-- Version:")) {
					int len = version.length();
					int start = 5;
					int end = len - 4;
					version = version.substring(start, end);
					break;
				}
			}
			bin.close();
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
			connection = false;
		} 
	}

	/**
	 * Gets an HTML formatted list of the most recent
	 * changes made to the application
	 */
	private static void setUpdates() {
		try {
			URL url = new URL(GET_CHANGES);
			BufferedReader bin = new BufferedReader(
											new InputStreamReader(
												url.openStream() ));

			String line;
			boolean reading = false;

			while( (line = bin.readLine()) != null) {
				if (line.equals(END_READ) )
					break;
				if ( reading ) 
					updates += line;
				if (line.equals(START_READ) )
					reading = true;
			}
		} catch (Exception ex) {
			System.out.println(ex.getMessage() + "Versioner::setUpdates()");
		}
	}


	/**
	 * Gets an HTML formatted list of all the the revisions made to this
	 * application since its Java release of 5.0.0
	 */
	private static void setAllUpdates() {
		try {
			URL url = new URL(GET_CHANGES);
			BufferedReader bin = new BufferedReader(
											new InputStreamReader(
												url.openStream() ));

			String line;
			boolean reading = false;

			while( (line = bin.readLine()) != null) {
				if (line.equals(END_READ_ALL) )
					break;
				if ( reading ) 
					allUpdates += line;
				if (line.equals(START_READ_ALL) )
					reading = true;
			}
		} catch (Exception ex) {
			System.out.println(ex.getMessage() + "Versioner::setAllUpdates()");
		}
	}

} // End of Class
