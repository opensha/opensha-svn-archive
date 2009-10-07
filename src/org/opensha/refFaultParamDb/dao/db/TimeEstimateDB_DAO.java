package org.opensha.refFaultParamDb.dao.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.opensha.refFaultParamDb.dao.exception.InsertException;
import org.opensha.refFaultParamDb.dao.exception.QueryException;
import org.opensha.refFaultParamDb.dao.exception.UpdateException;
import org.opensha.refFaultParamDb.data.TimeEstimate;
import org.opensha.refFaultParamDb.vo.EstimateInstances;
/**
 * <p>Title: TimeEstimateDB_DAO.java </p>
 * <p>Description: This class puts/gets time estimates in the database</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class TimeEstimateDB_DAO  {

  private final static String TABLE_NAME="Time_Estimate_Info";
  private final static String TIME_INSTANCE_ID="Time_Instance_Id";
  private final static String TIME_EST_ID="Time_Est_Id";
  private final static String IS_KA="Is_Ka";
  private final static String ERA = "Era";
  private final static String ZERO_YEAR = "Zero_Year";
  private final static String YES = "Y";
  private final static String NO = "N";
  private final static String KA = "ka";
  private DB_AccessAPI dbAccessAPI;
  private EstimateInstancesDB_DAO estimateInstanceDAO;

 /**
  * Constructor.
  * @param dbConnection
  */
 public TimeEstimateDB_DAO(DB_AccessAPI dbAccessAPI) {
   setDB_Connection(dbAccessAPI);
 }


 public void setDB_Connection(DB_AccessAPI dbAccessAPI) {
   this.dbAccessAPI = dbAccessAPI;
   estimateInstanceDAO = new EstimateInstancesDB_DAO(dbAccessAPI);
 }

 /**
  * Add the time estimate into the database table
  * @param timeInstanceId
  * @param timeEstimate
  * @throws InsertException
  */
  public void addTimeEstimate(int timeInstanceId, TimeEstimate timeEstimate) throws InsertException {
    EstimateInstances estimateInstance = new EstimateInstances();
    estimateInstance.setEstimate(timeEstimate.getEstimate());
    String isKaSelected;
    if(timeEstimate.isKaSelected()) {
      estimateInstance.setUnits(KA);
      isKaSelected=YES;
    }
    else {
      estimateInstance.setUnits(timeEstimate.getEra());
      isKaSelected=NO;
    }
    //insert into estimateInstance
    int estimateInstanceId = estimateInstanceDAO.addEstimateInstance(estimateInstance);

    // insert into time estimate table
    String sql = "insert into "+TABLE_NAME+"("+ TIME_INSTANCE_ID+","+TIME_EST_ID+","+
        IS_KA+","+ERA+","+ZERO_YEAR+")"+
        " values ("+timeInstanceId+","+estimateInstanceId+",'"+isKaSelected+"','"+
        timeEstimate.getEra()+"',"+timeEstimate.getZeroYear()+")";

    try { dbAccessAPI.insertUpdateOrDeleteData(sql); }
    catch(SQLException e) {
      //e.printStackTrace();
      throw new InsertException(e.getMessage());
    }
  }

  /**
   *
   * @param timeInstanceId
   * @return
   * @throws QueryException
   */
  public TimeEstimate getTimeEstimate(int timeInstanceId) throws QueryException {
    TimeEstimate timeEstimate=null;
    String condition  =  " where "+TIME_INSTANCE_ID+"="+timeInstanceId;
    ArrayList timeEstimateList = query(condition);
    if(timeEstimateList.size()>0) timeEstimate = (TimeEstimate)timeEstimateList.get(0);
    return timeEstimate;
  }

  /**
   *
   * @param timeInstanceId
   * @return
   * @throws UpdateException
   */
  public boolean removeTimeEstimate(int timeInstanceId) throws UpdateException {
    String sql = "delete from "+TABLE_NAME+"  where "+TIME_INSTANCE_ID+"="+timeInstanceId;
     try {
       int numRows = dbAccessAPI.insertUpdateOrDeleteData(sql);
       if(numRows==1) return true;
     }
     catch(SQLException e) { throw new UpdateException(e.getMessage()); }
     return false;
  }


  private ArrayList query(String condition) throws QueryException {
   ArrayList timeEstimateList = new ArrayList();
   String sql = "select "+TIME_INSTANCE_ID+","+TIME_EST_ID+","+
        IS_KA+","+ERA+","+ZERO_YEAR+" from "+TABLE_NAME+" "+condition;
   try {
     ResultSet rs  = dbAccessAPI.queryData(sql);
     while(rs.next()) {
       TimeEstimate timeEstimate = new TimeEstimate();
       EstimateInstances estimateInstance  = estimateInstanceDAO.getEstimateInstance(rs.getInt(TIME_EST_ID));
       if(rs.getString(IS_KA).equalsIgnoreCase(this.YES)) // if ka was selected by user
         timeEstimate.setForKaUnits(estimateInstance.getEstimate(),
                                    rs.getInt(ZERO_YEAR));
       else  // if calendar years estimate was selected by user
        timeEstimate.setForCalendarYear(estimateInstance.getEstimate(),
                                   rs.getString(ERA));
       timeEstimateList.add(timeEstimate);
     }
     rs.close();
   } catch(SQLException e) { throw new QueryException(e.getMessage()); }
   return timeEstimateList;
 }

}
