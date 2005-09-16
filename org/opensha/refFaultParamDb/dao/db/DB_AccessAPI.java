package org.opensha.refFaultParamDb.dao.db;

import com.sun.rowset.CachedRowSetImpl;

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
  public static final String SELECT_QUERY = "select query";
  public static final DB_AccessAPI dbConnection = new DB_ConnectionPool();

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
   * Insert/Update/Delete record in the database.
   * @param sql String
   * @return int
   * @throws SQLException
   */
  public int insertUpdateOrDeleteData(String sql) throws java.sql.SQLException;

}
