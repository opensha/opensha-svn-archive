package org.opensha.refFaultParamDb.dao.db;

import org.opensha.refFaultParamDb.dao.exception.InsertException;
import org.opensha.refFaultParamDb.vo.PaleoEvent;
import java.sql.SQLException;
import org.opensha.refFaultParamDb.gui.infotools.SessionInfo;
import java.util.ArrayList;

/**
 * <p>Title: PaleoEventDB_DAO.java </p>
 * <p>Description: This class interacts with the database to put/get information
 * about paleo events</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class PaleoEventDB_DAO {
  private final static String TABLE_NAME = "Paleo_Event";
  private final static String EVENT_ID = "Event_Id";
  private final static String EVENT_NAME = "Event_Name";
  private final static String SITE_ID = "Site_Id";
  private final static String SITE_ENTRY_DATE = "Site_Entry_Date";
  private final static String CONTRIBUTOR_ID = "Contributor_Id";
  private final static String EVENT_DATE_EST_ID = "Event_Date_Est_Id";
  private final static String DISPLACEMENT_EST_ID = "Displacement_Est_Id";
  private final static String ENTRY_DATE  = "Entry_Date";
  private final static String GENERAL_COMMENTS = "General_Comments";
  private final static String SEQUENCE_NAME = "Paleo_Event_Sequence";

  private final static String REFERENCES_TABLE_NAME ="Paleo_Event_References";
  private final static String REFERENCE_ID = "Reference_Id";

  private DB_AccessAPI dbAccess;
  // references DAO
  private ReferenceDB_DAO referenceDAO ;
  private TimeInstanceDB_DAO timeInstanceDAO;

  public PaleoEventDB_DAO(DB_AccessAPI dbAccess) {
    setDB_Connection(dbAccess);
  }

  public void setDB_Connection(DB_AccessAPI dbAccess) {
   this.dbAccess = dbAccess;
   referenceDAO = new ReferenceDB_DAO(dbAccess);
   timeInstanceDAO = new TimeInstanceDB_DAO(dbAccess);
  }

  /**
  * Add a new paleo event
  *
  * @param paleoEvent
  * @throws InsertException
  */
  public void addPaleoevent(PaleoEvent paleoEvent) throws InsertException {
    int paleoEventId, eventTimeEstId;
    String systemDate;
    try {
        paleoEventId = dbAccess.getNextSequenceNumber(SEQUENCE_NAME);
        systemDate = dbAccess.getSystemDate();
        eventTimeEstId = timeInstanceDAO.addTimeInstance(paleoEvent.getEventTime());
    }catch(SQLException e) {
      throw new InsertException(e.getMessage());
    }

    String sql = "insert into "+TABLE_NAME+"("+ EVENT_ID+","+EVENT_NAME+","+
        SITE_ID+","+SITE_ENTRY_DATE+","+CONTRIBUTOR_ID+","+EVENT_DATE_EST_ID+","+
        DISPLACEMENT_EST_ID+","+ENTRY_DATE+","+GENERAL_COMMENTS+")"+
        " values ("+paleoEventId+",'"+paleoEvent.getEventName()+"',"+paleoEvent.getSiteId()+
        ",'"+paleoEvent.getSiteEntryDate()+"',"+SessionInfo.getContributor().getId()+
        ","+eventTimeEstId+","+paleoEvent.getDisplacementEstId()+","+
        ",'"+systemDate+"','"+paleoEvent.getComments()+"')";

    try {
      // insert into paleo event table
      dbAccess.insertUpdateOrDeleteData(sql);
      //add the references (for this paleo event) into the database
      ArrayList shortCitationList = paleoEvent.getShortCitationsList();
      for(int i=0; i<shortCitationList.size(); ++i) {
        int referenceId = referenceDAO.getReference((String)shortCitationList.get(i)).getReferenceId();
        sql = "insert into "+this.REFERENCES_TABLE_NAME+"("+EVENT_ID+
            ","+ENTRY_DATE+","+REFERENCE_ID+") "+
            "values ("+paleoEventId+",'"+
            systemDate+"',"+referenceId+")";
        dbAccess.insertUpdateOrDeleteData(sql);
      }
    }
    catch(SQLException e) {
      e.printStackTrace();
      throw new InsertException(e.getMessage());
    }
  }



}