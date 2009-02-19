package org.opensha.data.siteType.servlet;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import org.opensha.data.Location;
import org.opensha.data.LocationList;

public class SiteDataServletAccessor<Element> {
	
	String url;
	
	public SiteDataServletAccessor(String servletURL) {
		this.url = servletURL;
	}
	
	public Element getValue(Location loc) throws IOException {
		return (Element)getResult(loc);
	}
	
	public ArrayList<Element> getValues(LocationList locs) throws IOException {
		return (ArrayList<Element>)getResult(locs);
	}
	
	private Object getResult(Object request) throws IOException {
		URLConnection servletConnection = this.openServletConnection();
		
		ObjectOutputStream outputToServlet = new
				ObjectOutputStream(servletConnection.getOutputStream());
		
		outputToServlet.writeObject(request);
		
		ObjectInputStream inputFromServlet = new
		ObjectInputStream(servletConnection.getInputStream());
		
		try {
			Object result = inputFromServlet.readObject();
			
			checkForError(result, inputFromServlet);
			
			inputFromServlet.close();
			
			return result;
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
	
	private void checkForError(Object obj, ObjectInputStream inputFromServlet) throws IOException, ClassNotFoundException {
		if (obj instanceof Boolean) {
			String message = (String)inputFromServlet.readObject();
			
			throw new RuntimeException("Status Request Failed: " + message);
		}
	}
	
	protected URLConnection openServletConnection() throws IOException {
		URL servlet = new URL(url);
		System.out.println("Connecting to: " + url + " ...");
		URLConnection servletConnection = servlet.openConnection();
		
		// inform the connection that we will send output and accept input
		servletConnection.setDoInput(true);
		servletConnection.setDoOutput(true);

		// Don't use a cached version of URL connection.
		servletConnection.setUseCaches (false);
		servletConnection.setDefaultUseCaches (false);
		// Specify the content type that we will send binary data
		servletConnection.setRequestProperty ("Content-Type","application/octet-stream");
		
		return servletConnection;
	}
}
