package org.opensha.sha.cybershake.maps.servlet;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.net.URLConnection;

import org.opensha.sha.cybershake.maps.CyberShake_GMT_MapGenerator;
import org.opensha.sha.cybershake.maps.InterpDiffMap;
import org.opensha.sha.cybershake.maps.ProbabilityGainMap;

public class CS_InterpDiffMapServletAccessor {
	
	public static String DEFAULT_METADATA_FILE_NAME = "metadata.txt";
	
	private static void checkLog(Object map) {
		if (map instanceof InterpDiffMap) {
			checkLog((InterpDiffMap)map);
		} else if (map instanceof ProbabilityGainMap) {
			ProbabilityGainMap pgMap = (ProbabilityGainMap)map;
			checkLog(pgMap.getReferenceMap());
			checkLog(pgMap.getModifiedMap());
		}
	}
	
	private static void checkLog(InterpDiffMap map) {
		if (map.isLogPlot()) {
			map.setGriddedData(CyberShake_GMT_MapGenerator.getLogXYZ(map.getGriddedData()));
			map.setScatter(CyberShake_GMT_MapGenerator.getLogXYZ(map.getScatter()));
		}
	}
	
	public static String makeMap(String dirName, InterpDiffMap map, String metadata) throws IOException, ClassNotFoundException {
		return (String)getMap(dirName, map, metadata);
	}
	
	public static String[] makeMap(String dirName, ProbabilityGainMap map, String metadata) throws IOException, ClassNotFoundException {
		return (String[])getMap(dirName, map, metadata);
	}
	
	private static Object getMap(String dirName, Object map, String metadata) throws IOException, ClassNotFoundException {
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
		return messageFromServlet;
	}

}
