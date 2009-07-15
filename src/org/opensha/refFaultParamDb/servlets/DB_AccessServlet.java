package org.opensha.refFaultParamDb.servlets;

import javax.servlet.http.HttpServlet;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.ServletConfig;
import java.util.Properties;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ObjectInputStream;

import org.opensha.refFaultParamDb.dao.db.DB_AccessAPI;
import org.opensha.refFaultParamDb.dao.db.DB_ConnectionPool;
import java.sql.SQLException;
import com.sun.rowset.CachedRowSetImpl;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import org.opensha.refFaultParamDb.dao.exception.DBConnectException;
import java.util.Enumeration;
import java.util.ArrayList;
import org.opensha.refFaultParamDb.dao.db.SpatialQueryResult;
import org.opensha.refFaultParamDb.dao.db.ContributorDB_DAO;

/**
 * <p>Title: DB_AccessServlet</p>
 *
 * <p>Description: Creates a two-tier database connection pool that can be shared.</p>
 *
 * @author Edward (Ned) Field, Vipin Gupta, Nitin Gupta
 * @version 1.0
 */
public class DB_AccessServlet extends HttpServlet{
	//GOLDEN: jdbc:oracle:thin:@gldwebdb.cr.usgs.gov:1521:EQCATS
	//PASADENA: jdbc:oracle:thin:@iron.gps.caltech.edu:1521:irondb

	private final static String CONNECT_FAILURE_MSG = "Connection to the database server failed.\nCheck username/password or try again later";
	private final static String PROP_NAME = "DbConnectionPropertiesFileName";
	private DB_AccessAPI myBroker;
	private ContributorDB_DAO contributorDAO;
	public void init() throws ServletException {
		try {
			Properties p = new Properties();
			String fileName = getInitParameter(PROP_NAME);
			p.load(new FileInputStream(fileName));
			String dbDriver = (String) p.get("dbDriver");
			String dbServer = (String) p.get("dbServer");
			int minConns = Integer.parseInt( (String) p.get("minConns"));
			int maxConns = Integer.parseInt( (String) p.get("maxConns"));
			String logFileString = (String) p.get("logFileString");
			double maxConnTime =
				(new Double( (String) p.get("maxConnTime"))).doubleValue();
			String usrName = (String) p.get("userName");
			String password = (String)p.get("password");
			myBroker = new
			DB_ConnectionPool(dbDriver, dbServer, usrName, password,
					minConns, maxConns, logFileString, maxConnTime);
			contributorDAO = new ContributorDB_DAO(myBroker);
		}
		catch (FileNotFoundException f) {f.printStackTrace();}
		catch (IOException e) {e.printStackTrace();}
		catch (Exception e) { e.printStackTrace(); }
	}

	/**
	 *
	 * @param request HttpServletRequest
	 * @param response HttpServletResponse
	 * @throws ServletException
	 * @throws IOException
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws
	ServletException, IOException {

		// get an input stream from the applet
		ObjectInputStream inputFromApp = new ObjectInputStream(request.
				getInputStream());
		ObjectOutputStream outputToApp = new ObjectOutputStream(response.
				getOutputStream());
		try {
			// get the username
			String usrName = (String)inputFromApp.readObject();
			// get the password
			String passwd = (String)inputFromApp.readObject();
			//receiving the name of the Function to be performed
			String functionToPerform = (String) inputFromApp.readObject();

			// if this is a valid contributor (do not check if it is reset password)
			if(!functionToPerform.equalsIgnoreCase(DB_AccessAPI.RESET_PASSWORD) &&
					!functionToPerform.equalsIgnoreCase(DB_AccessAPI.SELECT_QUERY) &&
					!functionToPerform.equalsIgnoreCase(DB_AccessAPI.SELECT_QUERY_SPATIAL) &&
					contributorDAO.isContributorValid(usrName, passwd)==null) {
				inputFromApp.close();
				DBConnectException exception =  new DBConnectException(CONNECT_FAILURE_MSG);
				outputToApp.writeObject(exception);
				outputToApp.close();
				return;
			}

			//receiving the query
			String query = (String)inputFromApp.readObject();

			/**
			 * Checking the type of function that needs to be performed in the database
			 */
			//getting the sequence number from Data table
			if(functionToPerform.equals(DB_AccessAPI.SEQUENCE_NUMBER)){
				int seqNo = myBroker.getNextSequenceNumber(query);
				outputToApp.writeObject(new Integer(seqNo));
			}
			//inserting new data in the table
			else if(functionToPerform.equals(DB_AccessAPI.INSERT_UPDATE_QUERY)){
				int key = myBroker.insertUpdateOrDeleteData(query);
				outputToApp.writeObject(new Integer(key));
			}
			// inserting spatial data into database
			else if(functionToPerform.equals(DB_AccessAPI.INSERT_UPDATE_SPATIAL)){
				ArrayList geomteryObjectList = (ArrayList)inputFromApp.readObject();
				int key = myBroker.insertUpdateOrDeleteData(query, geomteryObjectList);
				outputToApp.writeObject(new Integer(key));
			}
			//reading the data form the database
			else if(functionToPerform.equals(DB_AccessAPI.SELECT_QUERY)){
				CachedRowSetImpl resultSet= myBroker.queryData(query);
				outputToApp.writeObject(resultSet);
			}
			//reading the data form the database
			else if(functionToPerform.equals(DB_AccessAPI.SELECT_QUERY_SPATIAL)){
				String sqlWithNoSaptialColumnNames = (String)inputFromApp.readObject();
				ArrayList geomteryObjectList = (ArrayList)inputFromApp.readObject();
				SpatialQueryResult resultSet= myBroker.queryData(query,
						sqlWithNoSaptialColumnNames, geomteryObjectList);
				outputToApp.writeObject(resultSet);
			}
			// reset the password
			else if(functionToPerform.equalsIgnoreCase(DB_AccessAPI.RESET_PASSWORD)) {
				int key = myBroker.insertUpdateOrDeleteData(query);
				outputToApp.writeObject(new Integer(key));
			}
			inputFromApp.close();
			outputToApp.close();
		}
		catch(SQLException e){
			outputToApp.writeObject(e);
			outputToApp.close();
		}
		catch (ClassNotFoundException ex) {
			ex.printStackTrace();
		}
		catch (IOException ex) {
			ex.printStackTrace();
		}catch(Exception ex) {
			ex.printStackTrace();
		}
	}



	//Process the HTTP Post request
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws
	ServletException, IOException {
		// call the doPost method
		doGet(request, response);
	}

}
