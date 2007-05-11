package org.opensha.cybershake.db;

import com.sun.rowset.CachedRowSetImpl;


/**
 * <p>Title: DB_AccessAPI</p>
 *
 * <p>Description: It provides mechnism the submitting the query the Database.</p>
 *
 * @author Nitin Gupta , Vipin Gupta
 * @version 1.0
 */
public interface DBAccessAPI {


  /**
   * Static declaration on different functions that database supports
   */

  public static final String INSERT_UPDATE_QUERY = "insert/update/delete query";
  public static final String SELECT_QUERY = "select query";

  //public static final DBAccessAPI dbConnection = new DBAccess();
 

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
