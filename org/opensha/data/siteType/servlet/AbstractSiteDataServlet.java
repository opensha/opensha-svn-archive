package org.opensha.data.siteType.servlet;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opensha.data.Location;
import org.opensha.data.LocationList;
import org.opensha.data.siteType.SiteDataAPI;

public abstract class AbstractSiteDataServlet<Element> extends HttpServlet {
	
	public static DateFormat df = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy");
	
	private SiteDataAPI<Element> data;
	
	private String debugName;
	
	public AbstractSiteDataServlet(SiteDataAPI<Element> data) {
		super();
		
		this.data = data;
		this.debugName = data.getShortName() + " servlet";
	}
	
	public AbstractSiteDataServlet() {
		// if you use this, you better set the data!
	}
	
	public void setData(SiteDataAPI<Element> data) {
		this.data = data;
		this.debugName = data.getShortName() + " servlet";
	}
	
	//Process the HTTP Post request
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// call the doGet method
		doGet(request,response);
	}
	
	// Process the HTTP Get request
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		debug("Handling GET");
		
		// get an input stream from the applet
		ObjectInputStream in = new ObjectInputStream(request.getInputStream());
		ObjectOutputStream out = new ObjectOutputStream(response.getOutputStream());
		
		// get the location or location list object
		try {
			Object obj = in.readObject();
			
			if (obj instanceof Location) {
				// this is a single location request
				Location loc = (Location)obj;
				Element e = data.getValue(loc);
				out.writeObject(e);
			} else if (obj instanceof LocationList) {
				// this is a multiple location request
				LocationList locs = (LocationList)obj;
				ArrayList<Element> e = data.getValues(locs);
				out.writeObject(e);
			} else {
				fail(out, "You must give either a Location or a LocationList!");
			}
			
			out.flush();
			out.close();
		} catch (Exception e) {
			e.printStackTrace(System.out);
			fail(out, "Servlet Exception: " + e.getMessage());
		}
	}
	
	protected void fail(ObjectOutputStream out, String message) throws IOException {
		debug("Failing: " + message);
		out.writeObject(new Boolean(false));
		out.writeObject(message);
		out.flush();
		out.close();
	}
	
	protected void debug(String message) {
		String date = "[" + df.format(new Date()) + "]";
		System.out.println(debugName + " " + date + ": " + message);
	}
}
