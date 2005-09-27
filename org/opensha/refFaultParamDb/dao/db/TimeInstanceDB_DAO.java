package org.opensha.refFaultParamDb.dao.db;

import org.opensha.refFaultParamDb.*;
import org.opensha.refFaultParamDb.dao.exception.*;
import java.util.ArrayList;
import java.sql.SQLException;
import java.sql.ResultSet;
import org.opensha.refFaultParamDb.dao.EstimateDAO_API;
import org.opensha.refFaultParamDb.data.*;
import org.opensha.refFaultParamDb.dao.TimeInstanceDAO_API;

/**
 * <p>Title: TimeInstanceDB_DAO.java </p>
 * <p>Description: Time Instance DB DAO. It adds the time instance to the database.
 * It checks whether time is exact time or an estimate and handles appropriately </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class TimeInstanceDB_DAO  implements TimeInstanceDAO_API  {
  private final static String TABLE_NAME="Time_Instances";
  private final static String SEQUENCE_NAME="Time_Instances_Sequence";
  private final static String TIME_ID="Time_Id";
  private final static String TIME_TYPE_ID="Time_Type_Id";
  private final static String COMMENTS="Comments";
  private DB_AccessAPI dbAccessAPI;

  /**
   * Constructor.
   * @param dbConnection
   */
  public TimeInstanceDB_DAO(DB_AccessAPI dbAccessAPI) {
   setDB_Connection(dbAccessAPI);
  }


  public void setDB_Connection(DB_AccessAPI dbAccessAPI) {
    this.dbAccessAPI = dbAccessAPI;
  }

  /**
   * Add time instance to the database
   * @param estimateInstance
   * @throws InsertException
   */
  public int addTimeInstance(TimeAPI timeInstance) throws InsertException {
    String timeType;
    if(timeInstance instanceof ExactTime)  // exact time
      timeType = TimeTypeDB_DAO.EXACT_TIME;
    else timeType = TimeTypeDB_DAO.TIME_ESTIMATE; // time estimate

    // get id for the time type (time type refers to whether time is estimate or exact time)
    TimeTypeDB_DAO timeTypeDB_DAO = new TimeTypeDB_DAO(dbAccessAPI);
    int timeTypeId = timeTypeDB_DAO.getTimeTypeId(timeType);
    int timeInstanceId = -1;
    try {
       timeInstanceId = dbAccessAPI.getNextSequenceNumber(SEQUENCE_NAME);
    }catch(SQLException e) {
      throw new InsertException(e.getMessage());
    }

    String sql = "insert into "+TABLE_NAME+"("+TIME_ID+","+TIME_TYPE_ID+","+COMMENTS+")"+
        " values("+timeInstanceId+","+timeTypeId+",'"+
        timeInstance.getDatingComments()+"')";
    try {
      int numRows = dbAccessAPI.insertUpdateOrDeleteData(sql);
      if(timeType.equalsIgnoreCase(TimeTypeDB_DAO.EXACT_TIME)) // add exact time to database
        addExactTimeToDatabase(timeInstanceId, (ExactTime)timeInstance );
      // add time estimate to database
      else addTimeEstimateToDatabase(timeInstanceId,(TimeEstimate)timeInstance);
    }catch(SQLException e) {
      throw new InsertException(e.getMessage());
    }
    return timeInstanceId;
  }

  /**
   * Add exact time to the database
   * @param timeInstanceId
   * @param timeInstance
   */
  private void addExactTimeToDatabase(int timeInstanceId, ExactTime timeInstance) {
    ExactTimeDB_DAO exactTimeDAO = new ExactTimeDB_DAO(this.dbAccessAPI);
    exactTimeDAO.addExactTime(timeInstanceId, timeInstance);
  }

  /**
   * Add  time estimate to the database
   * @param timeInstanceId
   * @param timeInstance
   */
  private void addTimeEstimateToDatabase(int timeInstanceId, TimeEstimate timeInstance) {
    TimeEstimateDB_DAO timeEstimateDAO = new TimeEstimateDB_DAO(this.dbAccessAPI);
    timeEstimateDAO.addTimeEstimate(timeInstanceId, timeInstance);
  }

  /**
   * Get time instance based on time instances id
   * @param timeInstanceId
   * @return
   * @throws QueryException
   */
  public TimeAPI getTimeInstance(int timeInstanceId) throws QueryException {
    TimeAPI timAPI=null;
    String condition  =  " where "+TIME_ID+"="+timeInstanceId;
    ArrayList timeInstancesList = query(condition);
    if(timeInstancesList.size()>0) timAPI = (TimeAPI)timeInstancesList.get(0);
    return timAPI;

  }

  /**
   * remove the time instance from the table
   * @param timeInstanceId
   * @return
   * @throws UpdateException
   */
  public boolean removeTimeInstance(int timeInstanceId) throws UpdateException {
    /*String sql = "select "+TIME_ID+","+TIME_TYPE_ID+","+COMMENTS+" from "+
        TABLE_NAME+" where "+TIME_ID+"="+timeInstanceId;
    try {
      ResultSet rs  = dbAccessAPI.queryData(sql);
      EstimateTypeDB_DAO estimateTypeDB_DAO = new EstimateTypeDB_DAO(dbAccessAPI);
      while(rs.next())  {
        String estimateTypeName = estimateTypeDB_DAO.getEstimateType(rs.getInt(EST_TYPE_ID)).getEstimateName();
        // delete from specific table for each time type
        EstimateDAO_API estimateDAO_API = getEstimateDAO_API(estimateTypeName);
        estimateDAO_API.removeEstimate(estimateInstanceId);
        //remove from master table of time instances
        String delSql = "delete from "+TABLE_NAME+" where "+EST_ID+"="+estimateInstanceId;
        int numRows = dbAccessAPI.insertUpdateOrDeleteData(delSql);
        if(numRows==1) return true;
      }
      rs.close();
    } catch(SQLException e) { throw new QueryException(e.getMessage()); }*/
    return false;
  }


  private ArrayList query(String condition) throws QueryException {
  ArrayList estimateInstancesList = new ArrayList();
  /*String sql = "select "+EST_ID+","+EST_TYPE_ID+","+UNITS+","+COMMENTS+" from "+
      TABLE_NAME+" "+condition;
  try {
    ResultSet rs  = dbAccessAPI.queryData(sql);
    EstimateTypeDB_DAO estimateTypeDB_DAO = new EstimateTypeDB_DAO(dbAccessAPI);
    while(rs.next())  {
      EstimateInstances estimateInstances = new EstimateInstances();
      estimateInstances.setUnits(rs.getString(UNITS));
      estimateInstances.setEstimateInstanceId(rs.getInt(EST_ID));
      String estimateTypeName = estimateTypeDB_DAO.getEstimateType(rs.getInt(EST_TYPE_ID)).getEstimateName();
      EstimateDAO_API estimateDAO_API = getEstimateDAO_API(estimateTypeName);
      Estimate estimate = estimateDAO_API.getEstimate(rs.getInt(EST_ID));
      estimate.setComments(rs.getString(COMMENTS));
      estimateInstances.setEstimate(estimate);
      estimateInstancesList.add(estimateInstances);
    }
    rs.close();
  } catch(SQLException e) { throw new QueryException(e.getMessage()); }*/
  return estimateInstancesList;
}

}
