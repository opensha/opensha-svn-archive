package org.opensha.sha.calc.hazardMap.servlet;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.opensha.gridComputing.StorageHost;
import org.opensha.sha.calc.hazardMap.CalculationStatus;
import org.opensha.sha.calc.hazardMap.HazardMapJob;
import org.opensha.sha.calc.hazardMap.HazardMapJobCreator;
import org.opensha.util.FileUtils;

public class StatusServlet extends ConfLoadingServlet {
	
	public static final String NAME = "StatusServlet";
	public static final String WORKFLOW_LOG_DIR = "/home/aftershock/opensha/hazmaps/logs";
	
	public static final String OP_GET_DATASET_LIST = "Get Dataset List";
	public static final String OP_GET_STATUS = "Get Status";
	
	public static final String STATUS_WORKFLOW_BEGIN = "Workflow Execution Has Begun";
	public static final String STATUS_CALCULATING = "Calculating Curves";
	public static final String STATUS_RETRIEVING = "Retrieving Curves";
	
	int lineNum = 0;
	
	public StatusServlet() {
		super(NAME);
	}
	
	// Process the HTTP Get request
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		debug("Handling GET");
		
		// get an input stream from the applet
		ObjectInputStream in = new ObjectInputStream(request.getInputStream());
		ObjectOutputStream out = new ObjectOutputStream(response.getOutputStream());
		
		// get the function desired by the user
		String functionDesired = null;
		try {
			debug("Reading Operation");
			functionDesired  = (String) in.readObject();
			
			if (confLoader == null) {
				fail(out, "Failed to load server configuration file.");
				return;
			}
			
			if (functionDesired.equals(OP_GET_STATUS)) {
				debug("Handling STATUS Operation");
				handleStatus(in, out);
			} else if (functionDesired.equals(OP_GET_DATASET_LIST)) {
				debug("Handling LIST Operation");
				handleList(in, out);
			} else {
				fail(out, "Unknown request: " + functionDesired);
				return;
			}
		} catch (ClassNotFoundException e) {
			fail(out, "ClassNotFoundException: " + e.getMessage());
			return;
		}
		
	}
	
	private void handleList(ObjectInputStream in, ObjectOutputStream out) throws IOException, ClassNotFoundException {
		StorageHost storage = this.confLoader.getPresets().getStorageHosts().get(0);
		
		String dirName = storage.getPath();
		
		debug("Loading IDs for directory: " + dirName);
		
		File dirFile = new File(dirName);
		
		File dirList[] = dirFile.listFiles();
		
		ArrayList<String> ids = new ArrayList<String>();
		ArrayList<String> names = new ArrayList<String>();
		
		for (File mapDir : dirList) {
			if (!mapDir.isDirectory())
				continue;
			String datasetID = mapDir.getName();
			if (datasetID.equals("."))
				continue;
			if (datasetID.equals(".."))
				continue;
			
			String xmlFileName = mapDir.getAbsolutePath() + File.separator + datasetID + ".xml";
			File xmlFile = new File(xmlFileName);
			
			if (!xmlFile.exists())
				continue;
			
			debug("Loading id/name from: " + xmlFileName);
			String id[] = null;
			try {
				id = loadJobIDFromXML(xmlFileName);
			} catch (Exception e) {
				e.printStackTrace();
				continue;
			}
			
			if (!id[0].equals(datasetID)) {
				System.out.println("Dataset ID's don't match!!!!");
				System.out.println("From DirName: " + datasetID);
				System.out.println("From XML: " + id[0]);
				continue;
			}
			
			debug("Found dataset: " + datasetID);
			ids.add(id[0]);
			names.add(id[1]);
		}
		
		debug("Sending dataset IDs...");
		out.writeObject(ids);
		out.writeObject(names);
		
		out.flush();
		out.close();
		
		debug("Done handling dataset list");
	}
	
	private String[] loadJobIDFromXML(String file) throws MalformedURLException, DocumentException {
		SAXReader reader = new SAXReader();
		Document document = reader.read(new File(file));
		
		Element root = document.getRootElement();
		
		Element jobEl = root.element(HazardMapJob.XML_METADATA_NAME);
		
		String id = jobEl.attributeValue("jobID");
		String name = jobEl.attributeValue("jobName");
		
		String ret[] = {id, name};
		
		return ret;
	}
	
	private void handleStatus(ObjectInputStream in, ObjectOutputStream out) throws IOException, ClassNotFoundException {
		String id = (String)in.readObject();
		
		int totalCurves = 0;
		int ip = 0;
		int done = 0;
		int trans = -1;
		
		boolean remote = false;
		
		String status = HazardMapJobCreator.STATUS_NONE;
		
		debug("Detecting Status...");
		
		try {
			String logFile = getLogFileName(id);
			
			ArrayList<String> lines = FileUtils.loadFile(logFile);
			
			String lastLine = null;
			
			lineNum = 0;
			
			for (String line : lines) {
				lineNum++;
				line = line.trim();
				if (line.length() == 0)
					continue;
				status = getStatus(line);
				
				debug("Status: " + status);
				
				if (match(status, HazardMapJobCreator.STATUS_TEST_JOB))
					remote = false;
				else if (match(status, HazardMapJobCreator.STATUS_TEST_JOB_REMOTE)) {
					remote = true;
					trans = 0;
				}
				
				if (status.startsWith(HazardMapJobCreator.STATUS_WORKFLOW_BEGIN)) {
					totalCurves = this.getNumberFromStatus(status, lineNum);
					status = STATUS_WORKFLOW_BEGIN;
				} else if (status.startsWith(HazardMapJobCreator.STATUS_CURVE_CALCULATION_START)) {
					ip += this.getNumberFromStatus(status, lineNum);
					status = STATUS_CALCULATING;
				} else if (status.startsWith(HazardMapJobCreator.STATUS_CURVE_CALCULATION_END)) {
					done += this.getNumberFromStatus(status, lineNum);
					status = STATUS_CALCULATING;
				} else if (remote && status.startsWith(HazardMapJobCreator.STATUS_CURVE_RETRIEVED)) {
					trans += this.getNumberFromStatus(status, lineNum);
					status = STATUS_RETRIEVING;
				}
				
				lastLine = line;
			}
			
			debug("Final Status: " + status);
			
			Date date = null;
			if (lastLine != null)
				date = this.getDate(lastLine);
			
			CalculationStatus stat = new CalculationStatus(status, date, totalCurves, ip, done, trans);
			
			debug("Sending status...");
			out.writeObject(stat);
		} catch (Exception e) {
			fail(out, e.getMessage());
		}
		
		out.flush();
		out.close();
		
		debug("Done handling status");
	}
	
	private boolean match(String message, String status) {
		message = message.trim();
		status = status.trim();
		
		return message.equals(status);
	}
	
	private String stripNumberFromStatus(String status) {
		int strEnd = status.indexOf(":");
		if (strEnd < 0)
			throw new RuntimeException("Bad line parse! (line " + lineNum + ")");
		
		return status.substring(0, strEnd);
	}
	
	public static int getNumberFromStatus(String status, int lineNum) {
		status = status.trim();
		System.out.println("Getting number status from: " + status);
		int strEnd = status.lastIndexOf(" ");
		if (strEnd < 0)
			throw new RuntimeException("Bad number line parse! (line " + lineNum + ")");
		
		String numStr = status.substring(strEnd + 1).trim();
		
		return Integer.parseInt(numStr);
	}
	
	private String getStatus(String line) {
		int dateEnd = line.indexOf("]");
		if (dateEnd < 0)
			throw new RuntimeException("Bad line parse! (line " + lineNum + ")");
		line = line.substring(dateEnd + 1);
		
		line = line.trim();
		
		return line;
	}
	
	public static Date getDate(String line) throws ParseException {
		int dateStart = line.indexOf("[");
		if (dateStart < 0)
			throw new RuntimeException("Bad date parse!");
		int dateEnd = line.indexOf("]");
		if (dateEnd < 0)
			throw new RuntimeException("Bad date parse!");
		
		line = line.substring(dateStart + 1, dateEnd);
		
		SimpleDateFormat format = HazardMapJobCreator.LINUX_DATE_FORMAT;
		
		return format.parse(line);
	}
	
	public static String getLogFileName(String id) {
		return WORKFLOW_LOG_DIR + "/" + id + ".log";
	}

}
