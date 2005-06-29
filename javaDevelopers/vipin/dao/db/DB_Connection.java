
package javaDevelopers.vipin.dao.db;

import java.sql.*;
import java.io.*;
import java.util.*;

/**
 *
 * <p>Title:DB_Connection </p>
 * <p>Description: Connect to Reference Fault Parameter oracle database</p>
 * @version 1.0
 */
public class DB_Connection
{

  private Connection conn = null;
  private Statement stat = null;
  private final static String HOSTNAME="iron.gps.caltech.edu";
  //private final static String DB_NAME="(DESCRIPTION = (ADDRESS_LIST = (ADDRESS = (PROTOCOL = TCP)(HOST = iron.gps.caltech.edu)(PORT = 1521)))(CONNECT_DATA = (SERVICE_NAME = irondb.iron.gps.caltech.edu)(SERVER = DEDICATED)))";
  private final static String DB_NAME="irondb";
  private final static int PORT = 1521;
  private String hostName, dbName;
  private int port;

  public DB_Connection() {
    hostName = HOSTNAME;
    dbName = DB_NAME;
    port = PORT;
  }

  public DB_Connection(String hostName, String dbName, int port) {
    this.hostName = hostName;
    this.dbName = dbName;
    this.port = port;
  }

  public void disconnect() throws SQLException{
    if(conn!=null && !conn.isClosed()) {
      stat.close();
      conn.close();
    }
  }


  /**
   * Establishes the connection with the database using the mysql driver
   *
   * @return the database connection
   * @return
   * @throws SQLException
   * @throws IOException
   * @throws Exception
   */
  public void connect(String userName, String password) throws SQLException  {

    if(conn!=null && !conn.isClosed()) return ;
    String drivers = "oracle.jdbc.driver.OracleDriver";
    String url = "jdbc:oracle:thin:@"+hostName+":"+port+":"+dbName;

    //Try to load the driver, if this fails then print an error
    //and the contents of the stack
    try { Class.forName(drivers).newInstance(); }
    catch (Exception e) { e.printStackTrace(); }

    // try to connect to the database
    try {
      conn = DriverManager.getConnection(url,userName,password);
      conn.setAutoCommit(true);
      stat = conn.createStatement();
    }
    catch (SQLException e) { e.printStackTrace();}

    return;
  }

  /**
  * Inserts  the data into the database
  * @param query
  */
 public int insertUpdateOrDeleteData(String sql) throws java.sql.SQLException {
   int rows = stat.executeUpdate(sql);
   return rows;
 }

 /**
   * Runs the select query on the database
   * @param query
   * @return
   */
  public ResultSet queryData(String sql) throws java.sql.SQLException {
    //gets the resultSet after running the query
    ResultSet result = stat.executeQuery(sql);
    return result;
  }



}
