package org.opensha.refFaultParamDb.dao.db;

import org.opensha.refFaultParamDb.dao.EstimateDAO_API;
import org.opensha.data.estimate.MinMaxPrefEstimate;
import org.opensha.data.estimate.Estimate;
import org.opensha.refFaultParamDb.dao.exception.*;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.util.ArrayList;
/**
 * <p>Title: MinMaxPrefEstimateDB_DAO.java </p>
 * <p>Description: It saves the min/max/preferred estimate into the database</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class MinMaxPrefEstimateDB_DAO implements EstimateDAO_API {
  private final static String TABLE_NAME="Min_Max_Pref_Est";
  private final static String MIN_X = "Min_X";
  private final static String MAX_X = "Max_X";
  private final static String PREF_X = "Pref_X";
  private final static String MIN_PROB = "Min_Prob";
  private final static String MAX_PROB = "Max_Prob";
  private final static String PREF_PROB = "Pref_Prob";
  private final static String EST_ID = "Est_Id ";
  private DB_AccessAPI dbAccessAPI;
  public final static String EST_TYPE_NAME="MinMaxPrefEstimate";
  private final static String ERR_MSG = "This class just deals with Min/Max/Pref Estimates";

 /**
  * Constructor.
  * @param dbConnection
  */
 public MinMaxPrefEstimateDB_DAO(DB_AccessAPI dbAccessAPI) {
   setDB_Connection(dbAccessAPI);
 }

 public MinMaxPrefEstimateDB_DAO() { }


 public void setDB_Connection(DB_AccessAPI dbAccessAPI) {
   this.dbAccessAPI = dbAccessAPI;
 }

 /**
  * Add the normal estimate into the database table
  * @param estimateInstanceId
  * @param estimate
  * @throws InsertException
  */
  public void addEstimate(int estimateInstanceId, Estimate estimate) throws InsertException {
    if(!(estimate instanceof MinMaxPrefEstimate)) throw new InsertException(ERR_MSG);
    MinMaxPrefEstimate minMaxPrefEstimate = (MinMaxPrefEstimate)estimate;
    String colNames="", colVals="";
    // min X
    double minX = minMaxPrefEstimate.getMinimumX();
    if(!Double.isNaN(minX)) {
      colNames +=MIN_X+",";
      colVals +=minX+",";
    }
    // max X
    double maxX = minMaxPrefEstimate.getMaximumX();
    if(!Double.isNaN(maxX)) {
      colNames +=MAX_X+",";
      colVals +=maxX+",";
    }
    // pref X
    double prefX = minMaxPrefEstimate.getPreferredX();
    if(!Double.isNaN(prefX)) {
      colNames +=PREF_X+",";
      colVals +=prefX+",";
    }
    // min Prob
    double minProb = minMaxPrefEstimate.getMinimumProb();
    if(!Double.isNaN(minProb)) {
      colNames +=MIN_PROB+",";
      colVals +=minProb+",";
    }
    // max Prob
    double maxProb = minMaxPrefEstimate.getMaximumProb();
    if(!Double.isNaN(maxProb)) {
      colNames +=MAX_PROB+",";
      colVals +=maxProb+",";
    }
    // pref prob
    double prefProb = minMaxPrefEstimate.getPreferredProb();
    if(!Double.isNaN(prefProb)) {
      colNames +=PREF_PROB+",";
      colVals +=prefProb+",";
    }


      // insert into min/max/pref table
    String sql = "insert into "+TABLE_NAME+"("+ colNames+EST_ID+")"+
        " values ("+colVals+estimateInstanceId+")";
    try { dbAccessAPI.insertUpdateOrDeleteData(sql); }
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
    MinMaxPrefEstimate estimate=null;
    String condition  =  " where "+EST_ID+"="+estimateInstanceId;
    ArrayList estimateList = query(condition);
    if(estimateList.size()>0) estimate = (MinMaxPrefEstimate)estimateList.get(0);
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
       int numRows = dbAccessAPI.insertUpdateOrDeleteData(sql);
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
   String sql = "select "+EST_ID+","+MIN_X+","+MAX_X+","+PREF_X+","+
       MIN_PROB+","+MAX_PROB+","+PREF_PROB+" from "+TABLE_NAME+" "+condition;
   try {
     ResultSet rs  = dbAccessAPI.queryData(sql);
     while(rs.next()) {
       // get min/max and preferred
       double minX = rs.getFloat(this.MIN_X);
       if(rs.wasNull()) minX = Double.NaN;
       double maxX = rs.getFloat(this.MAX_X);
       if(rs.wasNull()) maxX = Double.NaN;
       double prefX = rs.getFloat(this.PREF_X);
       if(rs.wasNull()) prefX = Double.NaN;
       double minProb = rs.getFloat(this.MIN_PROB);
       if(rs.wasNull()) minProb = Double.NaN;
       double maxProb = rs.getFloat(this.MAX_PROB);
       if(rs.wasNull()) maxProb = Double.NaN;
       double prefProb = rs.getFloat(this.PREF_PROB);
       if(rs.wasNull()) prefProb = Double.NaN;
       // min/max/pref estimate
       MinMaxPrefEstimate estimate = new MinMaxPrefEstimate(minX, maxX, prefX,
           minProb, maxProb, prefProb);
       estimateList.add(estimate);
     }
     rs.close();
   } catch(SQLException e) { throw new QueryException(e.getMessage()); }
   return estimateList;
 }

}
