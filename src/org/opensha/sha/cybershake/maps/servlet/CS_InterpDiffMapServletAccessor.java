package org.opensha.sha.cybershake.maps.servlet;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.net.URLConnection;

import org.opensha.commons.data.xyz.GeographicDataSetAPI;
import org.opensha.commons.data.xyz.GeographicDataSetMath;
import org.opensha.commons.data.xyz.XYZ_DataSetAPI;
import org.opensha.sha.cybershake.maps.CyberShake_GMT_MapGenerator;
import org.opensha.sha.cybershake.maps.InterpDiffMap;

public class CS_InterpDiffMapServletAccessor {
	
	public static String DEFAULT_METADATA_FILE_NAME = "metadata.txt";
	
	private static void checkLog(InterpDiffMap map) {
		if (map.isLogPlot()) {
			GeographicDataSetAPI gridded = map.getGriddedData();
			GeographicDataSetAPI scatter = map.getScatter();
			
			if (gridded != null) {
				gridded = (GeographicDataSetAPI) gridded.clone();
				GeographicDataSetMath.log(gridded);
				map.setGriddedData(gridded);
			}
			if (scatter != null) {
				scatter = (GeographicDataSetAPI) gridded.clone();
				GeographicDataSetMath.log(scatter);
				map.setScatter(scatter);
			}
		}
	}
	
	public static String makeMap(String dirName, InterpDiffMap map, String metadata) throws IOException, ClassNotFoundException {
		URL gmtMapServlet = new URL(CS_InterpDiffMapServlet.SERVLET_URL);
		
		checkLog(map);

		URLConnection servletConnection = gmtMapServlet.openConnection();
		
		// inform the connection that we will send output and accept input
		servletConnection.setDoInput(true);
		servletConnection.setDoOutput(true);

		// Don't use a cached version of URL connection.
		servletConnection.setUseCaches (false);
		servletConnection.setDefaultUseCaches (false);
		// Specify the content type that we will send binary data
		servletConnection.setRequestProperty ("Content-Type","application/octet-stream");

		ObjectOutputStream outputToServlet = new
		ObjectOutputStream(servletConnection.getOutputStream());

		//sending the directory name to the servlet
		outputToServlet.writeObject(dirName);

		//sending the map specification
		outputToServlet.writeObject(map);

		//sending the contents of the Metadata file to the server.
		outputToServlet.writeObject(metadata);

		//sending the name of the MetadataFile to the server.
		outputToServlet.writeObject(DEFAULT_METADATA_FILE_NAME);

		outputToServlet.flush();
		outputToServlet.close();

		// Receive the "actual webaddress of all the gmt related files"
		// from the servlet after it has received all the data
		ObjectInputStream inputToServlet = new
		ObjectInputStream(servletConnection.getInputStream());

		Object messageFromServlet = inputToServlet.readObject();
		inputToServlet.close();
		if (messageFromServlet instanceof RuntimeException)
			throw (RuntimeException)messageFromServlet;
		return (String)messageFromServlet;
	}

}
