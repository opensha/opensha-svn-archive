package org.opensha.refFaultParamDb.dao.db;

import org.opensha.refFaultParamDb.vo.CombinedSlipRateInfo;
import java.sql.SQLException;
import org.opensha.refFaultParamDb.dao.exception.InsertException;
import java.sql.ResultSet;
import org.opensha.refFaultParamDb.dao.exception.QueryException;

/**
 * <p>Title: CombinedSlipRateInfoDB_DAO.java </p>
 * <p>Description: this class gets/puts slip rate data for Combined Events Info for
 * a site in the database. </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class CombinedSlipRateInfoDB_DAO {
  private final static String TABLE_NAME = "Combined_Slip_Rate_Info";
  private final static String SLIP_ASEISMIC_SLIP_FACTOR_EST_ID="Slip_Aseismic_Est_Id";
  private final static String SLIP_RATE_EST_ID= "Slip_Rate_Est_Id";
  private final static String SLIP_RATE_COMMENTS="Slip_Rate_Comments";
  private final static String ENTRY_DATE="Entry_Date";
  private final static String INFO_ID = "Info_Id";
  private DB_AccessAPI dbAccess;
  private EstimateInstancesDB_DAO estimateInstancesDAO;

  public CombinedSlipRateInfoDB_DAO(DB_AccessAPI dbAccess) {
    setDB_Connection(dbAccess);
  }

  public void setDB_Connection(DB_AccessAPI dbAccess) {
    this.dbAccess = dbAccess;
    estimateInstancesDAO = new EstimateInstancesDB_DAO(dbAccess);
  }

  /**
   * Add slip rate info into the database
   * @param infoId
   * @param entryDate
   * @param combinedSlipRateInfo
   */
  public void addSlipRateInfo(int infoId, String entryDate,
                              CombinedSlipRateInfo combinedSlipRateInfo) {
    int aSeisId = estimateInstancesDAO.addEstimateInstance(combinedSlipRateInfo.getASeismicSlipFactorEstimateForSlip());
    int slipRateId =  estimateInstancesDAO.addEstimateInstance(combinedSlipRateInfo.getSlipRateEstimate());
    String comments = combinedSlipRateInfo.getSlipRateComments();
    if(comments==null) comments="";
    String sql = "insert into "+TABLE_NAME+"("+SLIP_RATE_EST_ID+","+
        SLIP_ASEISMIC_SLIP_FACTOR_EST_ID+","+SLIP_RATE_COMMENTS+","+
        INFO_ID+","+ENTRY_DATE+") values ("+slipRateId+","+aSeisId+",'"+
        comments+"',"+infoId+",'"+entryDate+"')";
    try {
      dbAccess.insertUpdateOrDeleteData(sql);
    }catch(SQLException e) {
     throw new InsertException(e.getMessage());
   }
  }


  /**
   * Return the slip rate info for a combined info for a site
   * @param infoId
   * @param entryDate
   * @return
   */
  public CombinedSlipRateInfo getCombinedSlipRateInfo(int infoId, String entryDate) {
    CombinedSlipRateInfo combinedSlipRateInfo = null;
    String sql = "select "+SLIP_ASEISMIC_SLIP_FACTOR_EST_ID+","+
        SLIP_RATE_EST_ID+","+SLIP_RATE_COMMENTS+" from "+this.TABLE_NAME+
        " where "+INFO_ID+"="+infoId+" and "+ENTRY_DATE+"='"+entryDate+"'";
    try {
      ResultSet rs = dbAccess.queryData(sql);
      while(rs.next()) {
        combinedSlipRateInfo = new CombinedSlipRateInfo();
        combinedSlipRateInfo.setSlipRateComments(rs.getString(SLIP_RATE_COMMENTS));
        combinedSlipRateInfo.setSlipRateEstimate(estimateInstancesDAO.getEstimateInstance(rs.getInt(SLIP_RATE_EST_ID)));
        combinedSlipRateInfo.setASeismicSlipFactorEstimateForSlip(estimateInstancesDAO.getEstimateInstance(rs.getInt(SLIP_ASEISMIC_SLIP_FACTOR_EST_ID)));
      }
    }
    catch (SQLException ex) {
      throw new QueryException(ex.getMessage());
    }
    return combinedSlipRateInfo;
  }

}