package org.opensha.refFaultParamDb.dao.db;

import java.sql.*;

import com.sun.rowset.*;
import java.net.URL;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.net.URLConnection;
import org.opensha.refFaultParamDb.gui.infotools.GUI_Utils;
import org.opensha.refFaultParamDb.gui.infotools.SessionInfo;
import java.util.ArrayList;
import java.io.IOException;


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
  //private final static String SERVLET_URL  = "http://gravity.usc.edu:8080/UCERF/servlet/DB_AccessServlet";
  private final static String SERVLET_URL = "http://136.177.30.208:8080/UCERF/servlet/DB_AccessServlet";

  /**
   * Gets the next unique sequence number to be insertd in the table.
   *
   * @param sequenceName String
   * @return int
   * @throws SQLException
   */
  public int getNextSequenceNumber(String sequenceName) throws SQLException {

    Object dataFromServlet = openServletConnection(DB_AccessAPI.
                                                   SEQUENCE_NUMBER, sequenceName, null, null);
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
        INSERT_UPDATE_QUERY, sql, null, null);
    if (dataFromServlet instanceof SQLException) {
      throw (SQLException) dataFromServlet;
    }
    else {
      int key = ( (Integer) dataFromServlet).intValue();
      return key;
    }
  }

  /**
     * Insert/Update/Delete record in the database.
     * This method should be used when one of the columns in the database is a spatial column
     * @param sql String
     * @return int
     * @throws SQLException
     */
    public int insertUpdateOrDeleteData(String sql, ArrayList geometryList) throws java.sql.SQLException {
      Object dataFromServlet = openServletConnection(DB_AccessAPI.
          INSERT_UPDATE_SPATIAL, sql, null, geometryList);
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
                                                   SELECT_QUERY, sql, null, null);
    if (dataFromServlet instanceof SQLException) {
      throw (SQLException) dataFromServlet;
    }
    else {
      CachedRowSetImpl rowSet = (CachedRowSetImpl) dataFromServlet;
      return rowSet;
    }
  }

  /**
     * Query the databse and returns the Results in a  object which contains CachedRowSet
     * as well as JGeomtery objects.
     * @param sql String
     * @return CachedRowSetImpl
     * @throws SQLException
     */
 public SpatialQueryResult queryData(String sqlWithSpatialColumnName,
                                    String sqlWithNoSpatialColumnName,
                                    ArrayList spatialColumnNames)
     throws java.sql.SQLException {
   Object dataFromServlet = openServletConnection(DB_AccessAPI.
                                                   SELECT_QUERY_SPATIAL,
                                                   sqlWithSpatialColumnName,
                                                   sqlWithNoSpatialColumnName,
                                                   spatialColumnNames);
    if (dataFromServlet instanceof SQLException) {
      throw (SQLException) dataFromServlet;
    }
    else {
      SpatialQueryResult spatialQueryResult = (SpatialQueryResult) dataFromServlet;
      return spatialQueryResult;
    }
  }


  /**
    * Get the system date
    * @return
    * @throws java.sql.SQLException
    */
   public String getSystemDate() throws java.sql.SQLException {
     /*String sql = "select to_char(sysdate,'YYYY-MM-DD HH24:MI:SS') from dual";
     ResultSet result = queryData(sql);
     result.next();
     return Timestamp.valueOf(result.getString(1));*/

    String sql = "select to_char(sysdate) from dual";
    ResultSet result = queryData(sql);
    result.next();
    return result.getString(1);


    /*String sql = "select current_timestamp from dual";
     ResultSet result = queryData(sql);
     result.next();
     return result.getTimestamp(1);*/

   }

   /**
    * Reset the password in the database for the provided email address
    *
    * @param sql
    * @param email
    * @return
    */
   public int resetPasswordByEmail(String sql) throws java.sql.SQLException {
     Object dataFromServlet = openServletConnection(DB_AccessAPI.RESET_PASSWORD,
           sql, null, null);
      if (dataFromServlet instanceof SQLException) {
        throw (SQLException) dataFromServlet;
      }
      else {
        int key = ( (Integer) dataFromServlet).intValue();
        return key;
      }

   }


  /**
   * This function allows to establish connection with the DB existing on a
   * remote server, using a servlet.
   * @param sqlFunction String : Kind of operation that user wants to perform
   * in the database
   * @param sql String : SQL statement
   * @return Object : Object returned from the servlet
   */
  private Object openServletConnection(String sqlFunction, String sql,
                                       String sql1,
                                       ArrayList geometryList){

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
      if(sql1!=null) outputToServlet.writeObject(sql1);
      // send the geomtery objects in case of spatial columns
      if(geometryList!=null) outputToServlet.writeObject(geometryList);


      outputToServlet.flush();
      outputToServlet.close();

      // Receive the "actual webaddress of all the gmt related files"
     // from the servlet after it has received all the data
      ObjectInputStream inputToServlet = new
          ObjectInputStream(servletConnection.getInputStream());

      outputFromRemoteDB = inputToServlet.readObject();
      if(outputFromRemoteDB instanceof Exception) throw (RuntimeException)outputFromRemoteDB;
      inputToServlet.close();
    }catch (IOException e) {
      e.printStackTrace();
    }catch(ClassNotFoundException e) {
      e.printStackTrace();
    }

    return outputFromRemoteDB;
  }

}
