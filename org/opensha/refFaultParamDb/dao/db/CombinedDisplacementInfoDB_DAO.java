package org.opensha.refFaultParamDb.dao.db;

import org.opensha.refFaultParamDb.vo.CombinedDisplacementInfo;
import java.sql.SQLException;
import org.opensha.refFaultParamDb.dao.exception.InsertException;
import java.sql.ResultSet;
import org.opensha.refFaultParamDb.dao.exception.QueryException;

/**
 * <p>Title: CombinedDisplacementInfoDB_DAO.java </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class CombinedDisplacementInfoDB_DAO {
  private final static String TABLE_NAME = "Combined_Displacement_Info";
  private final static String INFO_ID = "Info_Id";
  private final static String ENTRY_DATE="Entry_Date";
  private final static String TOTAL_SLIP_EST_ID = "Total_Slip_Est_Id";
  private final static String DISP_ASEISMIC_SLIP_FACTOR_EST_ID="Disp_Aseismic_Est_Id";
  private final static String TOTAL_SLIP_COMMENTS="Total_Slip_Comments";
  private final static String SENSE_OF_MOTION = "Sense_of_Motion";
  private final static String MEASURED_SLIP_COMP = "Measured_Slip_Comp";
  private DB_AccessAPI dbAccess;
  private EstimateInstancesDB_DAO estimateInstancesDAO;

  public CombinedDisplacementInfoDB_DAO(DB_AccessAPI dbAccess) {
    setDB_Connection(dbAccess);
  }

  public void setDB_Connection(DB_AccessAPI dbAccess) {
    this.dbAccess = dbAccess;
    estimateInstancesDAO = new EstimateInstancesDB_DAO(dbAccess);
  }

  /**
   * Add displacement info into the database
   *
   * @param infoId
   * @param entryDate
   * @param combinedDispInfo
   */
  public void addDisplacementInfo(int infoId, String entryDate,
                                  CombinedDisplacementInfo combinedDispInfo) {
    int aSeisId = estimateInstancesDAO.addEstimateInstance(combinedDispInfo.getASeismicSlipFactorEstimateForDisp());
    int displacementId =  estimateInstancesDAO.addEstimateInstance(combinedDispInfo.getDisplacementEstimate());
    String comments = combinedDispInfo.getDisplacementComments();
    if(comments==null) comments="";

    double somRake = combinedDispInfo.getSenseOfMotionRake();
    double measuredCompRake = combinedDispInfo.getMeasuredComponentRake();
    String colNames="", colVals="";
    if(!Double.isNaN(somRake)) { // check whether user entered Sense of motion rake
      colNames += this.SENSE_OF_MOTION+",";
      colVals += somRake+",";
    }
    if(!Double.isNaN(measuredCompRake)) { // check whether user entered measured comp rake
      colNames += this.MEASURED_SLIP_COMP+",";
      colVals += measuredCompRake+",";
    }
    String sql = "insert into "+TABLE_NAME+"("+TOTAL_SLIP_EST_ID+","+
        colNames+DISP_ASEISMIC_SLIP_FACTOR_EST_ID+","+TOTAL_SLIP_COMMENTS+","+
        INFO_ID+","+ENTRY_DATE+") values ("+displacementId+","+colVals+aSeisId+",'"+
        comments+"',"+infoId+",'"+entryDate+"')";
    try {
      dbAccess.insertUpdateOrDeleteData(sql);
    }catch(SQLException e) {
      throw new InsertException(e.getMessage());
    }
  }

  /**
   * Get the displacement based on combined events info Id
   * @param infoId
   * @param entryDate
   * @return
   */
  public CombinedDisplacementInfo getDisplacementInfo(int infoId, String entryDate) {
    CombinedDisplacementInfo combinedDisplacementInfo =null;
    String sql = "select "+DISP_ASEISMIC_SLIP_FACTOR_EST_ID+","+SENSE_OF_MOTION+","+
        this.MEASURED_SLIP_COMP+","+TOTAL_SLIP_EST_ID+","+TOTAL_SLIP_COMMENTS+
        " from "+this.TABLE_NAME+
         " where "+INFO_ID+"="+infoId+" and "+ENTRY_DATE+"='"+entryDate+"'";
     try {
       ResultSet rs = dbAccess.queryData(sql);
       while(rs.next()) {
         combinedDisplacementInfo = new CombinedDisplacementInfo();
         combinedDisplacementInfo.setDisplacementComments(rs.getString(TOTAL_SLIP_COMMENTS));
         combinedDisplacementInfo.setDisplacementEstimate(estimateInstancesDAO.getEstimateInstance(rs.getInt(TOTAL_SLIP_EST_ID)));
         combinedDisplacementInfo.setASeismicSlipFactorEstimateForDisp(estimateInstancesDAO.getEstimateInstance(rs.getInt(DISP_ASEISMIC_SLIP_FACTOR_EST_ID)));
         double senseOfMotionRake = rs.getFloat(SENSE_OF_MOTION);
         if(rs.wasNull()) senseOfMotionRake=Double.NaN;
         double measuredCompRake = rs.getFloat(MEASURED_SLIP_COMP);
         if(rs.wasNull()) measuredCompRake = Double.NaN;
         combinedDisplacementInfo.setSenseOfMotionRake(senseOfMotionRake);
         combinedDisplacementInfo.setMeasuredComponentRake(measuredCompRake);
       }
     }
     catch (SQLException ex) {
       throw new QueryException(ex.getMessage());
     }
     return combinedDisplacementInfo;
  }


}