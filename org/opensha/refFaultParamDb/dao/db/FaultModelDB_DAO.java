package org.opensha.refFaultParamDb.dao.db;

import java.sql.SQLException;
import java.sql.ResultSet;
import org.opensha.refFaultParamDb.dao.FaultModelDAO_API;
import org.opensha.refFaultParamDb.vo.FaultModel;
import org.opensha.refFaultParamDb.dao.exception.*;
import java.util.ArrayList;

/**
 * <p>Title: FaultModelDB_DAO.java </p>
 * <p>Description: Performs insert/delete/update on fault model on oracle database</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */


public class FaultModelDB_DAO implements FaultModelDAO_API {
  private final static String SEQUENCE_NAME="Fault_Model_Sequence";
  private final static String TABLE_NAME="Fault_Model";
  private final static String FAULT_MODEL_ID="Fault_Model_Id";
  private final static String CONTRIBUTOR_ID="Contributor_Id";
  private final static String FAULT_MODEL_NAME="Fault_Model_Name";
  private DB_AccessAPI dbAccessAPI;


  public FaultModelDB_DAO(DB_AccessAPI dbAccessAPI) {
   setDB_Connection(dbAccessAPI);
  }

  public void setDB_Connection(DB_AccessAPI dbAccessAPI) {
   this.dbAccessAPI = dbAccessAPI;
 }

 /**
  * Add a new fault model
  *
  * @param faultModel
  * @throws InsertException
  */
  public int addFaultModel(FaultModel faultModel) throws InsertException {
    int faultModelId = -1;
    try {
      faultModelId = dbAccessAPI.getNextSequenceNumber(SEQUENCE_NAME);
    }catch(SQLException e) {
      throw new InsertException(e.getMessage());
    }
    String sql = "insert into "+TABLE_NAME+"("+ FAULT_MODEL_ID+","+CONTRIBUTOR_ID+
        ","+FAULT_MODEL_NAME+") "+
        " values ("+faultModelId+","+faultModel.getContributor().getId()+
        ",'"+faultModel.getFaultModelName()+"')";
    try { dbAccessAPI.insertUpdateOrDeleteData(sql); }
    catch(SQLException e) {
      //e.printStackTrace();
      throw new InsertException(e.getMessage());
    }
    return faultModelId;
  }


  /**
   * Update a fault Model
   *
   * @param faultModelId
   * @param faultModel
   * @return
   * @throws UpdateException
   */
  public boolean updateFaultModel(int faultModelId, FaultModel faultModel) throws UpdateException {
    String sql = "update "+TABLE_NAME+" set "+FAULT_MODEL_NAME+"= '"+
        faultModel.getFaultModelName()+"',"+CONTRIBUTOR_ID+"="+faultModel.getContributor().getId()+
       " where "+FAULT_MODEL_ID+"="+faultModelId;
    try {
      int numRows = dbAccessAPI.insertUpdateOrDeleteData(sql);
      if(numRows==1) return true;
    }
    catch(SQLException e) { throw new UpdateException(e.getMessage()); }
    return false;

  }

  /**
   * Get a fault model based on fault model ID
   * @param faultModelId
   * @return
   * @throws QueryException
   */
  public FaultModel getFaultModel(int faultModelId) throws QueryException {
    FaultModel faultModel=null;
    String condition = " where "+FAULT_MODEL_ID+"="+faultModelId;
    ArrayList faultModelList=query(condition);
    if(faultModelList.size()>0) faultModel = (FaultModel)faultModelList.get(0);
    return faultModel;

  }

  /**
   * remove a fault model from the database
   * @param faultModelId
   * @return
   * @throws UpdateException
   */
  public boolean removeFaultModel(int faultModelId) throws UpdateException {
    String sql = "delete from "+TABLE_NAME+"  where "+FAULT_MODEL_ID+"="+faultModelId;
    try {
      int numRows = dbAccessAPI.insertUpdateOrDeleteData(sql);
      if(numRows==1) return true;
    }
    catch(SQLException e) { throw new UpdateException(e.getMessage()); }
    return false;
  }


  /**
   * Get all the fault Models from the database
   * @return
   * @throws QueryException
   */
  public ArrayList getAllFaultModels() throws QueryException {
   return query(" ");
  }

  private ArrayList query(String condition) throws QueryException {
    ArrayList faultModelList = new ArrayList();
    String sql =  "select "+FAULT_MODEL_ID+","+FAULT_MODEL_NAME+","+CONTRIBUTOR_ID+" from "+TABLE_NAME+condition;
    try {
      ResultSet rs  = dbAccessAPI.queryData(sql);
      ContributorDB_DAO contributorDAO = new ContributorDB_DAO(dbAccessAPI);
      while(rs.next()) faultModelList.add(new FaultModel(rs.getInt(FAULT_MODEL_ID),
            rs.getString(FAULT_MODEL_NAME),
            contributorDAO.getContributor(rs.getInt(CONTRIBUTOR_ID))));
      rs.close();
    } catch(SQLException e) { throw new QueryException(e.getMessage()); }
    return faultModelList;
  }

}
