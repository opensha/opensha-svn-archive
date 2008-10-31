package org.opensha.sha.calc.hazardMap.servlet;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Authenticator;
import java.net.URL;
import java.net.URLConnection;

import org.dom4j.Document;
import org.opensha.gridComputing.GridResourcesList;
import org.opensha.sha.calc.hazardMap.cron.CronOperation;
import org.opensha.util.http.HTTPAuthenticator;

public class ManagementServletAccessor extends ServletAccessor {
	
	public static final String SERVLET_URL = "http://opensha.usc.edu:8080/HazardMaps/restricted/HazardMapManagement";
	public static final String SERVLET_URL_SSL = "https://opensha.usc.edu:443/HazardMaps/restricted/HazardMapManagement";
	
	public static final String SUCCESS = "Success";
	
	boolean ssl;
	
	public ManagementServletAccessor(String url, boolean ssl) {
		super(url);
		this.ssl = ssl;
		
		Authenticator.setDefault(new HTTPAuthenticator());
	}
	
	public GridResourcesList getGridResourcesList() throws IOException, ClassNotFoundException {
		URLConnection servletConnection = this.openServletConnection(ssl);
		
		ObjectOutputStream outputToServlet = new
		ObjectOutputStream(servletConnection.getOutputStream());

		// send the operation to servlet
		System.out.println("Sending Operation...");
		outputToServlet.writeObject(ManagementServlet.OP_GET_RESOURCES);
		
		System.out.println("Closing Output...");
		outputToServlet.flush();
		outputToServlet.close();
		
		ObjectInputStream inputFromServlet = new
		ObjectInputStream(servletConnection.getInputStream());
		
		Object obj = inputFromServlet.readObject();
		
		if (obj instanceof Boolean) {
			String message = (String)inputFromServlet.readObject();
			
			throw new RuntimeException("Resource List Request Failed: " + message);
		}
		
		GridResourcesList list = (GridResourcesList)obj;
		
		return list;
	}
	
	public String submit(Document doc) throws IOException, ClassNotFoundException {
		URLConnection servletConnection = this.openServletConnection(ssl);
		
		ObjectOutputStream outputToServlet = new
		ObjectOutputStream(servletConnection.getOutputStream());

		// send the operation to servlet
		System.out.println("Sending Operation...");
		outputToServlet.writeObject(CronOperation.OP_SUBMIT);
		
		// send the XML document
		System.out.println("Sending XML Document...");
		outputToServlet.writeObject(doc);
		
		System.out.println("Closing Output...");
		outputToServlet.flush();
		outputToServlet.close();
		
		// Receive the "destroy" from the servlet after it has received all the data
		System.out.println("Getting Input...");
		ObjectInputStream inputFromServlet = new
		ObjectInputStream(servletConnection.getInputStream());
		
		boolean success = (Boolean)inputFromServlet.readObject();
		
		String message;
		if (success) {
			message = SUCCESS;
		} else {
			message = (String)inputFromServlet.readObject();
		}

		System.out.println("Closing Input...");
		inputFromServlet.close();
		
		return message;
	}

}
