package org.opensha.refFaultParamDb.dao.db;

import java.sql.*;

import com.sun.rowset.*;
import java.net.URL;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.net.URLConnection;
import org.opensha.refFaultParamDb.gui.infotools.GUI_Utils;
import org.opensha.refFaultParamDb.gui.infotools.SessionInfo;


/**
 * <p>Title: ServerDB_Access </p>
 *
 * <p>Description: This class provides access to the remote DB using the Servlet.</p>
 * @author Edward Field, Vipin Gupta and Nitin Gupta
 * @version 1.0
 */
public class ServerDB_Access
    implements DB_AccessAPI {


  //used for debugging
  private static final boolean D = false;
  private final static String SERVLET_URL  = "http://gravity.usc.edu:8080/UCERF/servlet/DB_AccessServlet";


  /**
   * Gets the next unique sequence number to be insertd in the table.
   *
   * @param sequenceName String
   * @return int
   * @throws SQLException
   * @todo Implement this javaDevelopers.vipin.dao.db.DB_AccessAPI method
   */
  public int getNextSequenceNumber(String sequenceName) throws SQLException {

    Object dataFromServlet = openServletConnection(DB_AccessAPI.
                                                   SEQUENCE_NUMBER, sequenceName);
    if (dataFromServlet instanceof SQLException) {
      throw (SQLException) dataFromServlet;
    }
    else {
      int SeqNo = ( (Integer) dataFromServlet).intValue();
      return SeqNo;
    }
  }

  /**
   * Insert/Update/Delete record in the database.
   *
   * @param sql String
   * @return int
   * @throws SQLException
   * @todo Implement this javaDevelopers.vipin.dao.db.DB_AccessAPI method
   */
  public int insertUpdateOrDeleteData(String sql) throws SQLException {
    Object dataFromServlet = openServletConnection(DB_AccessAPI.
        INSERT_UPDATE_QUERY, sql);
    if (dataFromServlet instanceof SQLException) {
      throw (SQLException) dataFromServlet;
    }
    else {
      int key = ( (Integer) dataFromServlet).intValue();
      return key;
    }

  }

  /**
   * Query the databse and returns the Results in a CachedRowset object.
   *
   * @param sql String
   * @return CachedRowSetImpl
   * @throws SQLException
   * @todo Implement this javaDevelopers.vipin.dao.db.DB_AccessAPI method
   */
  public CachedRowSetImpl queryData(String sql) throws SQLException {

    Object dataFromServlet = openServletConnection(DB_AccessAPI.
                                                   SELECT_QUERY, sql);
    if (dataFromServlet instanceof SQLException) {
      throw (SQLException) dataFromServlet;
    }
    else {
      CachedRowSetImpl rowSet = (CachedRowSetImpl) dataFromServlet;
      return rowSet;
    }
  }


  /**
    * Get the system date
    * @return
    * @throws java.sql.SQLException
    */
   public Date getSystemDate() throws java.sql.SQLException {
     String sql = "select sysdate from dual";
     ResultSet result = queryData(sql);
     result.next();
     return result.getDate(1);
   }


  /**
   * This function allows to establish connection with the DB existing on a
   * remote server, using a servlet.
   * @param sqlFunction String : Kind of operation that user wants to perform
   * in the database
   * @param sql String : SQL statement
   * @return Object : Object returned from the servlet
   */
  private Object openServletConnection(String sqlFunction, String sql){

    Object outputFromRemoteDB = null;
    try{

      if(D) System.out.println("starting to make connection with servlet");
      URL dbAccessServlet = new URL(SERVLET_URL);

      URLConnection servletConnection = dbAccessServlet.openConnection();
      if(D) System.out.println("connection established");

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
      //sending the username and password to the server
      outputToServlet.writeObject(SessionInfo.getUserName());
      //send the password
      outputToServlet.writeObject(SessionInfo.getPassword());
      //sending the type of operation that needs to be performed in the database
      outputToServlet.writeObject(sqlFunction);
      //sending the actual query to be performed in the database
      outputToServlet.writeObject(sql);


      outputToServlet.flush();
      outputToServlet.close();

      // Receive the "actual webaddress of all the gmt related files"
     // from the servlet after it has received all the data
      ObjectInputStream inputToServlet = new
          ObjectInputStream(servletConnection.getInputStream());

      outputFromRemoteDB = inputToServlet.readObject();
      if(outputFromRemoteDB instanceof Exception) throw (Exception)outputFromRemoteDB;
      inputToServlet.close();
    }catch (Exception e) {
      e.printStackTrace();
    }
    return outputFromRemoteDB;
  }

}
