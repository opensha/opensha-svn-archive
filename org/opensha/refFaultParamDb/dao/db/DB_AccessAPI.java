package org.opensha.refFaultParamDb.dao.db;

import com.sun.rowset.CachedRowSetImpl;
import org.opensha.refFaultParamDb.gui.infotools.SessionInfo;
import java.sql.Timestamp;
import java.util.ArrayList;

/**
 * <p>Title: DB_AccessAPI</p>
 *
 * <p>Description: It provides mechnism the submitting the query the Database.</p>
 *
 * @author Edward Field, Nitin Gupta , Vipin Gupta
 * @version 1.0
 */
public interface DB_AccessAPI {


  /**
   * Static declaration on different functions that database supports
   */
  public static final String SEQUENCE_NUMBER = "get sequence number";
  public static final String INSERT_UPDATE_QUERY = "insert/update/delete query";
  public static final String INSERT_UPDATE_SPATIAL = "insert/update spatial";
  public static final String SELECT_QUERY = "select query";
  public static final String SELECT_QUERY_SPATIAL = "select query spatial";
  public static final String RESET_PASSWORD = "reset password";
  //public static final DB_AccessAPI dbConnection = new DB_ConnectionPool();
  public static final DB_AccessAPI dbConnection = new ServerDB_Access();

  /**
   * Gets the next unique sequence number to be insertd in the table.
   * @param sequenceName String
   * @return int
   * @throws SQLException
   */
  public int getNextSequenceNumber(String sequenceName) throws java.sql.
      SQLException;
  /**
   * Query the databse and returns the Results in a CachedRowset object.
   * @param sql String
   * @return CachedRowSetImpl
   * @throws SQLException
   */
  public CachedRowSetImpl queryData(String sql) throws java.sql.SQLException;

  /**
  * Query the databse and returns the Results in a  object which contains CachedRowSet
  * as well as JGeomtery objects.
  * @param sql String
  * @return CachedRowSetImpl
  * @throws SQLException
  */
  public SpatialQueryResult queryData(String sqlWithSpatialColumnNames,
                                     String  sqlWithNoSpatialColumnNames,
                                     ArrayList spatialColumnNames) throws java.sql.SQLException;

 /**
  * Reset the password in the database for the provided email address
  *
  * @param sql
  * @param email
  * @return
  */
  public int resetPasswordByEmail(String sql) throws java.sql.SQLException;


  /**
   * Insert/Update/Delete record in the database.
   * @param sql String
   * @return int
   * @throws SQLException
   */
  public int insertUpdateOrDeleteData(String sql) throws java.sql.SQLException;

  /**
   * Insert/Update/Delete record in the database.
   * This method should be used when one of the columns in the database is a spatial column
   * @param sql String
   * @return int
   * @throws SQLException
   */
  public int insertUpdateOrDeleteData(String sql, ArrayList geometryList) throws java.sql.SQLException;


  /**
   * Get the sytem date from oracle database
   * @return
   */
  public String getSystemDate() throws java.sql.SQLException;

}
