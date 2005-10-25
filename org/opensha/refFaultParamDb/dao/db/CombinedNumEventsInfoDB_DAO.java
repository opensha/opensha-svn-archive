package org.opensha.refFaultParamDb.dao.db;

import org.opensha.refFaultParamDb.vo.CombinedNumEventsInfo;

/**
 * <p>Title: CombinedNumEventsInfoDB_DAO.java </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class CombinedNumEventsInfoDB_DAO {
  private final static String NUM_EVENTS_EST_ID = "Num_Events_Est_Id";
  private final static String NUM_EVENTS_COMMENTS = "Num_Events_Comments";
  private final static String ENTRY_DATE="Entry_Date";
  private final static String INFO_ID = "Info_Id";
  private DB_AccessAPI dbAccess;

  public CombinedNumEventsInfoDB_DAO(DB_AccessAPI dbAccess) {
    setDB_Connection(dbAccess);
  }

  public void setDB_Connection(DB_AccessAPI dbAccess) {
    this.dbAccess = dbAccess;
  }

  public void addNumEventsInfo(int infoId, String entryDate,
                                  CombinedNumEventsInfo combinedDispInfo) {

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


  public CombinedNumEventsInfo getCombinedNumEventsInfo(int infoId, String entryDate) {
    return null;
  }


}