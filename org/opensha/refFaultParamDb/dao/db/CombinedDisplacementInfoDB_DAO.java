package org.opensha.refFaultParamDb.dao.db;

import org.opensha.refFaultParamDb.vo.CombinedDisplacementInfo;

/**
 * <p>Title: CombinedDisplacementInfoDB_DAO.java </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class CombinedDisplacementInfoDB_DAO {
  private final static String INFO_ID = "Info_Id";
  private final static String ENTRY_DATE="Entry_Date";
  private final static String TOTAL_SLIP_EST_ID = "Total_Slip_Est_Id";
  private final static String DISP_ASEISMIC_SLIP_FACTOR_EST_ID="Disp_Aseismic_Est_Id";
  private final static String TOTAL_SLIP_COMMENTS="Total_Slip_Comments";
  private DB_AccessAPI dbAccess;

  public CombinedDisplacementInfoDB_DAO(DB_AccessAPI dbAccess) {
    setDB_Connection(dbAccess);
  }

  public void setDB_Connection(DB_AccessAPI dbAccess) {
    this.dbAccess = dbAccess;
  }

  public void addDisplacementInfo(int infoId, String entryDate,
                                  CombinedDisplacementInfo combinedDispInfo) {

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

  public CombinedDisplacementInfo getDisplacementInfo(int infoId, String entryDate) {
    return null;
  }


}