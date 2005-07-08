package javaDevelopers.vipin.dao.db;

import javaDevelopers.vipin.dao.LogTypeDAO_API;
import java.sql.SQLException;
import java.sql.ResultSet;
import javaDevelopers.vipin.dao.exception.QueryException;

/**
 * <p>Title: LogTypeDB_DAO.java </p>
 * <p>Description: Find log type id corresponding to a particular log base</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class LogTypeDB_DAO implements LogTypeDAO_API {

  private final static String TABLE_NAME="Log_Type";
  private final static String LOG_TYPE_ID="Log_Type_Id";
  private final static String LOG_BASE="Log_Base";
  public final static String LOG_BASE_E = "E";
  public final static String LOG_BASE_10="10";
  private DB_Connection dbConnection;

  /**
   * Constructor.
   * @param dbConnection
   */
  public LogTypeDB_DAO(DB_Connection dbConnection) {
    setDB_Connection(dbConnection);
  }


  public void setDB_Connection(DB_Connection connection) {
    this.dbConnection = connection;
  }

  /**
   * Get log type id for a particular log base from the table
   *
   * @param logBase
   * @return
   * @throws QueryException
   */
  public int getLogTypeId(String logBase)  throws QueryException {
    int logTypeId = -1;
    String sql =  "select "+LOG_TYPE_ID+","+LOG_BASE+" from "+TABLE_NAME+
        " where "+LOG_BASE+"='"+logBase+"'";
    try {
      ResultSet rs  = dbConnection.queryData(sql);
      if(rs.next()) logTypeId = rs.getInt(LOG_TYPE_ID);
      rs.close();
    } catch(SQLException e) { throw new QueryException(e.getMessage()); }
    return logTypeId;
  }


  public String getLogBase(int logTypeId) throws QueryException {
    String logBase = "";
    String sql =  "select "+LOG_TYPE_ID+","+LOG_BASE+" from "+TABLE_NAME+
        " where "+LOG_TYPE_ID+"="+logTypeId+"";
    try {
      ResultSet rs  = dbConnection.queryData(sql);
      if(rs.next()) logBase = rs.getString(LOG_BASE);
      rs.close();
    } catch(SQLException e) { throw new QueryException(e.getMessage()); }
    return logBase;
  }


}