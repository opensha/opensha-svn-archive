package org.opensha.refFaultParamDb.dao.db;

import org.opensha.refFaultParamDb.*;
import org.opensha.refFaultParamDb.vo.EstimateInstances;
import org.opensha.refFaultParamDb.dao.exception.*;
import java.util.ArrayList;
import org.opensha.data.estimate.*;
import java.sql.SQLException;
import java.sql.ResultSet;
import org.opensha.refFaultParamDb.dao.EstimateDAO_API;
import org.opensha.util.ClassUtils;

/**
 * <p>Title: EstimateInstancesDB_DAO.java </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class EstimateInstancesDB_DAO {
  private final static String TABLE_NAME="Est_Instances";
  private final static String SEQUENCE_NAME="Est_Instances_Sequence";
  private final static String EST_ID="Est_Id";
  private final static String EST_TYPE_ID="Est_Type_Id";
  private final static String UNITS="Est_Units";
  private final static String COMMENTS="Comments";
  private final static String ESTIMATES_DB_DAO_PACKAGE="org.opensha.refFaultParamDb.dao.db.";
  private final static String ESTIMATES_DB_DAO_SUFFIX = "DB_DAO";
  private DB_AccessAPI dbAccessAPI;

  /**
   * Constructor.
   * @param dbConnection
   */
  public EstimateInstancesDB_DAO(DB_AccessAPI dbAccessAPI) {
   setDB_Connection(dbAccessAPI);
  }


  public void setDB_Connection(DB_AccessAPI dbAccessAPI) {
    this.dbAccessAPI = dbAccessAPI;
  }

  /**
   * Add estimate instance to the table
   * @param estimateInstance
   * @throws InsertException
   */
  public int addEstimateInstance(EstimateInstances estimateInstance) throws InsertException {
    Estimate estimate = estimateInstance.getEstimate();
    EstimateDAO_API estimateDAO = getEstimateDAO(estimate);
    EstimateTypeDB_DAO estimateTypeDB_DAO = new EstimateTypeDB_DAO(dbAccessAPI);
    int estimateTypeId = estimateTypeDB_DAO.getEstimateType(estimateDAO.getEstimateTypeName()).getEstimateTypeId();
    int estimateInstanceId = -1;
    try {
       estimateInstanceId = dbAccessAPI.getNextSequenceNumber(SEQUENCE_NAME);
    }catch(SQLException e) {
      throw new InsertException(e.getMessage());
    }

    String sql = "insert into "+TABLE_NAME+"("+EST_ID+","+EST_TYPE_ID+","+UNITS+","+COMMENTS+")"+
        " values("+estimateInstanceId+","+estimateTypeId+",'"+estimateInstance.getUnits()+"','"+
        estimate.getComments()+"')";
    try {
      int numRows = dbAccessAPI.insertUpdateOrDeleteData(sql);
      estimateDAO.addEstimate(estimateInstanceId, estimate);
    }catch(SQLException e) {
      throw new InsertException(e.getMessage());
    }
    return estimateInstanceId;
  }

  // get the correct DAO according to estimate type
  private EstimateDAO_API getEstimateDAO(Estimate estimate) {
    EstimateDAO_API estimateDAO_API = null;
    if(estimate instanceof NormalEstimate) estimateDAO_API = new NormalEstimateDB_DAO(dbAccessAPI);
    else if(estimate instanceof LogNormalEstimate) estimateDAO_API = new LogNormalEstimateDB_DAO(dbAccessAPI);
    else if(estimate instanceof IntegerEstimate) estimateDAO_API = new IntegerEstimateDB_DAO(dbAccessAPI);
    else if(estimate instanceof FractileListEstimate) estimateDAO_API = new FractileListEstimateDB_DAO(dbAccessAPI);
    else if(estimate instanceof DiscreteValueEstimate) estimateDAO_API = new DiscreteValueEstimateDB_DAO(dbAccessAPI);
    else if(estimate instanceof PDF_Estimate) estimateDAO_API = new PDF_EstimateDB_DAO(dbAccessAPI);

    return estimateDAO_API;

  }


  public EstimateInstances getEstimateInstance(int estimateInstanceId) throws QueryException {
    EstimateInstances estimateInstance=null;
    String condition  =  " where "+EST_ID+"="+estimateInstanceId;
    ArrayList estimateList = query(condition);
    if(estimateList.size()>0) estimateInstance = (EstimateInstances)estimateList.get(0);
    return estimateInstance;

  }

  /**
   * remove the estimate instance from the table
   * @param estimateInstanceId
   * @return
   * @throws UpdateException
   */
  public boolean removeEstimateInstance(int estimateInstanceId) throws UpdateException {
    String sql = "select "+EST_ID+","+EST_TYPE_ID+","+UNITS+","+COMMENTS+" from "+
        TABLE_NAME+" where "+EST_ID+"="+estimateInstanceId;
    try {
      ResultSet rs  = dbAccessAPI.queryData(sql);
      EstimateTypeDB_DAO estimateTypeDB_DAO = new EstimateTypeDB_DAO(dbAccessAPI);
      while(rs.next())  {
        String estimateTypeName = estimateTypeDB_DAO.getEstimateType(rs.getInt(EST_TYPE_ID)).getEstimateName();
        // delete from specific table for each estimate
        EstimateDAO_API estimateDAO_API = getEstimateDAO_API(estimateTypeName);
        estimateDAO_API.removeEstimate(estimateInstanceId);
        //remove from master table of estimates
        String delSql = "delete from "+TABLE_NAME+" where "+EST_ID+"="+estimateInstanceId;
        int numRows = dbAccessAPI.insertUpdateOrDeleteData(delSql);
        if(numRows==1) return true;
      }
      rs.close();
    } catch(SQLException e) { throw new QueryException(e.getMessage()); }
    return false;
  }



  private EstimateDAO_API getEstimateDAO_API(String estimateTypeName) {
    EstimateDAO_API estimateDAO_API = (EstimateDAO_API)ClassUtils.createNoArgConstructorClassInstance(ESTIMATES_DB_DAO_PACKAGE+estimateTypeName+ESTIMATES_DB_DAO_SUFFIX);
    estimateDAO_API.setDB_Connection(this.dbAccessAPI);
    return estimateDAO_API;
  }

 public ArrayList getAllEstimateInstances() throws QueryException {
   return query(" ");
 }

  private ArrayList query(String condition) throws QueryException {
  ArrayList estimateInstancesList = new ArrayList();
  String sql = "select "+EST_ID+","+EST_TYPE_ID+","+UNITS+","+COMMENTS+" from "+
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
  } catch(SQLException e) { throw new QueryException(e.getMessage()); }
  return estimateInstancesList;
}

}