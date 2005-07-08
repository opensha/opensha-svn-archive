package javaDevelopers.vipin.dao.db;

import javaDevelopers.vipin.dao.EstimateDAO_API;
import org.opensha.data.estimate.LogNormalEstimate;
import org.opensha.data.estimate.Estimate;
import javaDevelopers.vipin.dao.exception.*;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.util.ArrayList;
/**
 * <p>Title: LogNormalEstimateDB_DAO.java </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class LogNormalEstimateDB_DAO implements EstimateDAO_API {

  private final static String TABLE_NAME="Log_Normal_Est";
  private final static String EST_ID="Est_Id";
  private final static String MEDIAN="Median";
  private final static String STD_DEV="Std_Dev";
  private final static String LOG_TYPE_ID = "Log_Type_Id";
  private DB_Connection dbConnection;
  public final static String EST_TYPE_NAME="LogNormalEstimate";
  private final static String ERR_MSG = "This class just deals with Log Normal Estimates";
  private LogTypeDB_DAO logTypeDB_DAO;

 /**
  * Constructor.
  * @param dbConnection
  */
 public LogNormalEstimateDB_DAO(DB_Connection dbConnection) {
   setDB_Connection(dbConnection);
 }

 public LogNormalEstimateDB_DAO() { }


 public void setDB_Connection(DB_Connection connection) {
   this.dbConnection = connection;
   logTypeDB_DAO = new LogTypeDB_DAO(dbConnection);
 }

 /**
  * Add the normal estimate into the database table
  * @param estimateInstanceId
  * @param estimate
  * @throws InsertException
  */
  public void addEstimate(int estimateInstanceId, Estimate estimate) throws InsertException {
    if(!(estimate instanceof LogNormalEstimate)) throw new InsertException(ERR_MSG);
    LogNormalEstimate logNormalEstimate = (LogNormalEstimate)estimate;
    int logTypeId;
    // get the log type id
    if(logNormalEstimate.getIsBase10())
      logTypeId = logTypeDB_DAO.getLogTypeId(LogTypeDB_DAO.LOG_BASE_10);
    else logTypeId = logTypeDB_DAO.getLogTypeId(LogTypeDB_DAO.LOG_BASE_E);
      // insert into log normal table
    String sql = "insert into "+TABLE_NAME+"("+ EST_ID+","+MEDIAN+","+
        STD_DEV+","+LOG_TYPE_ID+")"+
        " values ("+estimateInstanceId+","+estimate.getMedian()+","+
        estimate.getStdDev()+","+logTypeId+")";
    try { dbConnection.insertUpdateOrDeleteData(sql); }
    catch(SQLException e) {
      //e.printStackTrace();
      throw new InsertException(e.getMessage());
    }
  }

  /**
   *
   * @param estimateInstanceId
   * @return
   * @throws QueryException
   */
  public Estimate getEstimate(int estimateInstanceId) throws QueryException {
    LogNormalEstimate estimate=null;
    String condition  =  " where "+EST_ID+"="+estimateInstanceId;
    ArrayList estimateList = query(condition);
    if(estimateList.size()>0) estimate = (LogNormalEstimate)estimateList.get(0);
    return estimate;
  }

  /**
   *
   * @param estimateInstanceId
   * @return
   * @throws UpdateException
   */
  public boolean removeEstimate(int estimateInstanceId) throws UpdateException {
    String sql = "delete from "+TABLE_NAME+"  where "+EST_ID+"="+estimateInstanceId;
     try {
       int numRows = dbConnection.insertUpdateOrDeleteData(sql);
       if(numRows==1) return true;
     }
     catch(SQLException e) { throw new UpdateException(e.getMessage()); }
     return false;
  }

  public String getEstimateTypeName() {
    return EST_TYPE_NAME;
  }


  private ArrayList query(String condition) throws QueryException {
   ArrayList estimateList = new ArrayList();
   String sql = "select "+EST_ID+","+MEDIAN+","+STD_DEV+","+LOG_TYPE_ID+" from "+TABLE_NAME+" "+condition;
   try {
     ResultSet rs  = dbConnection.queryData(sql);
     while(rs.next()) {
       LogNormalEstimate estimate = new LogNormalEstimate(rs.getFloat(MEDIAN),
                                              rs.getFloat(STD_DEV));
       String logBase = this.logTypeDB_DAO.getLogBase(rs.getInt(LOG_TYPE_ID));
       if(logBase.equalsIgnoreCase(LogTypeDB_DAO.LOG_BASE_10)) estimate.setIsBase10(true);
       else estimate.setIsBase10(false);
       estimateList.add(estimate);
     }
     rs.close();
   } catch(SQLException e) { throw new QueryException(e.getMessage()); }
   return estimateList;
 }

}
