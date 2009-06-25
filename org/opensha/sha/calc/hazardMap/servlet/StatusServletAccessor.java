package org.opensha.sha.calc.hazardMap.servlet;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URLConnection;
import java.text.ParseException;
import java.util.ArrayList;

import org.opensha.commons.data.region.EvenlyGriddedGeographicRegion;
import org.opensha.sha.calc.hazardMap.CalculationStatus;

public class StatusServletAccessor extends ServletAccessor {
	
	public static final String SERVLET_URL = "http://opensha.usc.edu:8080/HazardMaps/HazardMapStatus";
	
	public StatusServletAccessor(String url) {
		super(url);
	}
	
	public CalculationStatus getStatus(String id) throws IOException, ClassNotFoundException {
		URLConnection servletConnection = this.openServletConnection(false);
		
		ObjectOutputStream outputToServlet = new
		ObjectOutputStream(servletConnection.getOutputStream());

		// send the operation to servlet
		System.out.println("Sending Operation...");
		outputToServlet.writeObject(StatusServlet.OP_GET_STATUS);
		
		System.out.println("Sending ID...");
		outputToServlet.writeObject(id);
		
		ObjectInputStream inputFromServlet = new
		ObjectInputStream(servletConnection.getInputStream());
		
		System.out.println("Getting status...");
		Object statusObj = inputFromServlet.readObject();
		
		if (statusObj instanceof Boolean) {
			String message = (String)inputFromServlet.readObject();
			
			throw new RuntimeException("Status Request Failed: " + message);
		}
		
		CalculationStatus status = (CalculationStatus)statusObj;
		
		inputFromServlet.close();
		
		return status;
	}
	
	public ArrayList<DatasetID> getDatasetIDs() throws IOException, ClassNotFoundException {
		URLConnection servletConnection = this.openServletConnection(false);
		
		ObjectOutputStream outputToServlet = new
		ObjectOutputStream(servletConnection.getOutputStream());

		// send the operation to servlet
		System.out.println("Sending Operation...");
		outputToServlet.writeObject(StatusServlet.OP_GET_DATASET_LIST);
		
		ObjectInputStream inputFromServlet = new
		ObjectInputStream(servletConnection.getInputStream());
		
		System.out.println("Getting dataset ids...");
		Object idObj = inputFromServlet.readObject();
		
		if (idObj instanceof Boolean) {
			String message = (String)inputFromServlet.readObject();
			
			throw new RuntimeException("ID List Request Failed: " + message);
		}
		
		ArrayList<DatasetID> ids = (ArrayList<DatasetID>)idObj;
		
		inputFromServlet.close();
		
		return ids;
	}
	
	/**
	 * Gets a list of datasets with at least 1 hazard curve completed
	 * 
	 * @return
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public ArrayList<DatasetID> getCurveDatasetIDs() throws IOException, ClassNotFoundException {
		URLConnection servletConnection = this.openServletConnection(false);
		
		ObjectOutputStream outputToServlet = new
		ObjectOutputStream(servletConnection.getOutputStream());

		// send the operation to servlet
		System.out.println("Sending Operation...");
		outputToServlet.writeObject(StatusServlet.OP_GET_DATASETS_WITH_CURVES_LIST);
		
		ObjectInputStream inputFromServlet = new
		ObjectInputStream(servletConnection.getInputStream());
		
		System.out.println("Getting dataset ids...");
		Object idObj = inputFromServlet.readObject();
		
		if (idObj instanceof Boolean) {
			String message = (String)inputFromServlet.readObject();
			
			throw new RuntimeException("ID List Request Failed: " + message);
		}
		
		ArrayList<DatasetID> ids = (ArrayList<DatasetID>)idObj;
		
		inputFromServlet.close();
		
		return ids;
	}
	
	public EvenlyGriddedGeographicRegion getRegion(String id) throws IOException, ClassNotFoundException {
		URLConnection servletConnection = this.openServletConnection(false);
		
		ObjectOutputStream outputToServlet = new
		ObjectOutputStream(servletConnection.getOutputStream());

		// send the operation to servlet
		System.out.println("Sending Operation...");
		outputToServlet.writeObject(StatusServlet.OP_GET_DATASET_REGION);
		
		System.out.println("Sending ID...");
		outputToServlet.writeObject(id);
		
		ObjectInputStream inputFromServlet = new
		ObjectInputStream(servletConnection.getInputStream());
		
		System.out.println("Getting status...");
		Object regionObj = inputFromServlet.readObject();
		
		if (regionObj instanceof Boolean) {
			String message = (String)inputFromServlet.readObject();
			
			throw new RuntimeException("Status Request Failed: " + message);
		}
		
		EvenlyGriddedGeographicRegion region = (EvenlyGriddedGeographicRegion)regionObj;
		
		inputFromServlet.close();
		
		return region;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
//		System.out.println(StatusServlet.getNumberFromStatus("[Wed Oct 22 16:09:57 PDT 2008] Workflow Execution Has Begun: 338", 0));
//		try {
//			System.out.println(StatusServlet.getDate("[Wed Oct 22 16:09:57 PDT 2008] Workflow Execution Has Begun: 338"));
//		} catch (ParseException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
		StatusServletAccessor access = new StatusServletAccessor(SERVLET_URL);
		
		String id = "servlet_test_3";
		
		try {
//			System.out.println(access.getStatus(id));
			for (DatasetID dataID : access.getDatasetIDs()) {
				System.out.println("ID: " + dataID.getID() + " " + dataID.getName());
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
