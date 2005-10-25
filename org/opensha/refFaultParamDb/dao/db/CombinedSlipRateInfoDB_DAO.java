package org.opensha.refFaultParamDb.dao.db;

import org.opensha.refFaultParamDb.vo.CombinedSlipRateInfo;

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

  public CombinedSlipRateInfoDB_DAO(DB_AccessAPI dbAccess) {
    setDB_Connection(dbAccess);
  }

  public void setDB_Connection(DB_AccessAPI dbAccess) {
    this.dbAccess = dbAccess;
  }

  public void addSlipRateInfo(int infoId, String entryDate,
                              CombinedSlipRateInfo combinedSlipRateInfo) {

  }

  /**
 * Get the comments. If comments are null ,return " "
 * @param comments
 * @return
 */
private String getComments(String comments) {
  String comm = " ";
  if(comments ==null) comm=comments;
  return comm;
}


  public CombinedSlipRateInfo getCombinedSlipRateInfo(int infoId, String entryDate) {
   return null;
 }

}