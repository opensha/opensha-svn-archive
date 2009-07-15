package org.opensha.sha.calc.hazardMap.servlet;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.text.DateFormat;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opensha.sha.calc.hazardMap.ConfLoader;
import org.opensha.sha.calc.hazardMap.HazardMapJobCreator;
import org.opensha.sha.calc.hazardMap.cron.CronConfLoader;

public abstract class ConfLoadingServlet extends HttpServlet {
	
	DateFormat df = HazardMapJobCreator.LINUX_DATE_FORMAT;

	public static final String CONF_FILE = "/home/aftershock/opensha/hazmaps/conf/conf.xml";

	protected ConfLoader confLoader = null;
	
	String debugName;

	public ConfLoadingServlet(String debugName) {
		super();
		
		this.debugName = debugName;

		try {
			confLoader = new ConfLoader(CONF_FILE);
		} catch (Exception e) {
			confLoader = null;
		}
	}

	protected void fail(ObjectOutputStream out, String message) throws IOException {
		debug("Failing: " + message);
		out.writeObject(new Boolean(false));
		out.writeObject(message);
		out.flush();
		out.close();
	}
	
	protected void fail(ObjectOutputStream out, Exception e) throws IOException {
		debug("Failing: " + e.getMessage());
		out.writeObject(new Boolean(false));
		out.writeObject(e);
		out.flush();
		out.close();
	}

	//Process the HTTP Post request
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// call the doGet method
		doGet(request,response);
	}

	@Override
	// Process the HTTP Get request
	abstract public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException;
	
	protected void debug(String message) {
		String date = "[" + df.format(new Date()) + "]";
		System.out.println(debugName + " " + date + ": " + message);
	}

}
