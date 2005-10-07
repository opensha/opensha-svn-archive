package org.opensha.refFaultParamDb.dao.db;

import java.util.ArrayList;
import org.opensha.refFaultParamDb.data.TimeAPI;
import org.opensha.refFaultParamDb.dao.exception.InsertException;
import java.sql.SQLException;
import org.opensha.refFaultParamDb.vo.EventSequence;
import org.opensha.refFaultParamDb.gui.infotools.SessionInfo;
import org.opensha.refFaultParamDb.vo.PaleoEvent;
import java.sql.ResultSet;
import org.opensha.refFaultParamDb.dao.exception.QueryException;

/**
 * <p>Title: EventSequenceDB_DAO.java </p>
 * <p>Description: It interacts with the database to put/get the information about
 * event sequences</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class EventSequenceDB_DAO {
  // main table name and attribute names
  private final static String TABLE_NAME = "Event_Sequence";
  private final static String TABLE_SEQUENCE_NAME = "Event_Sequence_Sequence";
  private final static String SEQUENCE_ID = "Sequence_Id";
  private final static String SEQUENCE_NAME = "Sequence_Name";
  private final static String SITE_ID = "Site_Id";
  private final static String SITE_ENTRY_DATE = "Site_Entry_Date";
  private final static String START_TIME_EST_ID = "Start_Time_Est_Id";
  private final static String SEQUENCE_PROB = "Sequence_Probability";
  private final static String END_TIME_EST_ID = "End_Time_Est_Id";
  private final static String ENTRY_DATE = "Entry_Date";
  private final static String CONTRIBUTOR_ID = "Contributor_Id";
  private final static String GENERAL_COMMENT = "General_Comments";
  // reference table name and attribute names
  private final static String REFERENCE_TABLE_NAME = "Event_Sequence_References";
  private final static String EVENT_SEQUENCE_ID = "Event_Sequence_Id";
  private final static String EVENT_SEQUENCE_ENTRY_DATE  = "Event_Sequence_Entry_Date";
  private final static String REFERENCE_ID = "Reference_Id";
  // table name which saves the all the events within a sequence
  private final static String SEQUENCE_EVENT_LIST_TABLE_NAME = "Event_Sequence_Event_List";
  private final static String EVENT_ID = "Event_Id";
  private final static String EVENT_ENTRY_DATE = "Event_Entry_Date";
  private final static String SEQUENCE_ENTRY_DATE = "Sequence_Entry_Date";
  private final static String MISSED_PROB = "MISSED_PROB";
  private final static String EVENT_INDEX_IN_SEQUENCE="Event_Index_In_Sequence";

  private DB_AccessAPI dbAccess; // database connection
  private TimeInstanceDB_DAO timeInstanceDAO;
  private ReferenceDB_DAO referenceDAO;

  public EventSequenceDB_DAO(DB_AccessAPI dbAccess) {
    setDB_Connection(dbAccess);
  }

  public void setDB_Connection(DB_AccessAPI dbAccess) {
    this.dbAccess = dbAccess;
    timeInstanceDAO = new TimeInstanceDB_DAO(dbAccess);
  }

  /**
   * Add a list of possible sequences in a particular timespan
   * @param sequenceList
   * @param startTime
   * @param endTime
   */
  public void addEventSequence(ArrayList sequenceList,
                               TimeAPI startTime,
                               TimeAPI endTime) {
    try {
      // put the start time and end time into the database
      int startTimeId = timeInstanceDAO.addTimeInstance(startTime);
      int endTimeId = timeInstanceDAO.addTimeInstance(endTime);
      // loop over each sequence in the list and put it in database
      for(int i=0; i<sequenceList.size(); ++i) {
        EventSequence eventSequence = (EventSequence)sequenceList.get(i);
        int sequenceId = dbAccess.getNextSequenceNumber(TABLE_SEQUENCE_NAME);
        String systemDate = dbAccess.getSystemDate();
        // put sequence in database
        String sql = "insert into "+TABLE_NAME+ "("+SEQUENCE_ID+","+SEQUENCE_NAME+
            ","+SITE_ID+","+SITE_ENTRY_DATE+","+START_TIME_EST_ID+","+
            END_TIME_EST_ID+","+ENTRY_DATE+","+SEQUENCE_PROB+","+CONTRIBUTOR_ID+","+GENERAL_COMMENT+
            ") values ("+sequenceId+",'"+eventSequence.getSequenceName()+"',"+
            eventSequence.getSiteId()+",'"+eventSequence.getSiteEntryDate()+"',"+
            startTimeId+","+endTimeId+",'"+systemDate+"',"+eventSequence.getSequenceProb()+","+
            SessionInfo.getContributor().getId()+",'"+eventSequence.getComments()+"')";
        dbAccess.insertUpdateOrDeleteData(sql);
        //put references for this sequence in the database
       /* ArrayList shortCitationList = startTime.getReferencesList();
        for(int j=0; j<shortCitationList.size(); ++j) {
          int referenceId = referenceDAO.getReference((String)shortCitationList.get(j)).getReferenceId();
          sql = "insert into "+this.REFERENCE_TABLE_NAME+"("+EVENT_SEQUENCE_ID+
              ","+EVENT_SEQUENCE_ENTRY_DATE+","+REFERENCE_ID+") "+
              "values ("+sequenceId+",'"+systemDate+"',"+referenceId+")";
          dbAccess.insertUpdateOrDeleteData(sql);
        }*/
        // put event list and missed event probs in the database
        ArrayList eventsInSequence = eventSequence.getEventsParam();
        double missedProbs[]  = eventSequence.getMissedEventsProbs();
        int numEventsInSequence = eventsInSequence.size();
        for(int j=0; j<numEventsInSequence; ++j ) {
          PaleoEvent paleoEvent = (PaleoEvent)eventsInSequence.get(j);
          sql = "insert into "+SEQUENCE_EVENT_LIST_TABLE_NAME+"("+
              EVENT_ID+","+EVENT_ENTRY_DATE+","+SEQUENCE_ID+","+
              SEQUENCE_ENTRY_DATE+","+MISSED_PROB+","+
              EVENT_INDEX_IN_SEQUENCE+") values ("+paleoEvent.getEventId()+",'"+
              paleoEvent.getEntryDate()+"',"+sequenceId+",'"+systemDate+"',"+
              missedProbs[j]+","+j+")";
          dbAccess.insertUpdateOrDeleteData(sql);
          if(j==(numEventsInSequence-1)) {
            ++j;
           // number of probs are 1 greater than number of number of events in the sequence
           sql = "insert into "+SEQUENCE_EVENT_LIST_TABLE_NAME+"("+
             EVENT_ID+","+EVENT_ENTRY_DATE+","+SEQUENCE_ID+","+
             SEQUENCE_ENTRY_DATE+","+MISSED_PROB+","+
             EVENT_INDEX_IN_SEQUENCE+") values ("+paleoEvent.getEventId()+",'"+
             paleoEvent.getEntryDate()+"',"+sequenceId+",'"+systemDate+"',"+
             missedProbs[j]+","+j+")";
            dbAccess.insertUpdateOrDeleteData(sql);
          }
        }
      }
    }catch(SQLException e) {
      throw new InsertException(e.getMessage());
    }
  }

  /**
   * Return a list of all the sequences for a particular site
   *
   * @param siteId
   * @return
   */
  public ArrayList getSequences(int siteId) {
    String condition = " where "+this.SITE_ID+"="+siteId;
    return query(condition);
  }

  /**
  * Query the event sequence table to get paleo sequences based on the condition
  * @param condition
  * @return
  */
 private ArrayList query(String condition) {
   ArrayList sequenceList = new ArrayList();
   String sql = "select "+SEQUENCE_ID+","+SEQUENCE_NAME+
            ","+SITE_ID+",to_char("+SITE_ENTRY_DATE+") as "+SITE_ENTRY_DATE+","+
            START_TIME_EST_ID+","+END_TIME_EST_ID+",to_char("+ENTRY_DATE+") as "+
            ENTRY_DATE+","+SEQUENCE_PROB+","+CONTRIBUTOR_ID+","+
            GENERAL_COMMENT+" from "+ this.TABLE_NAME+" "+condition;
   try {
    ResultSet rs  = dbAccess.queryData(sql);
    ContributorDB_DAO contributorDAO = new ContributorDB_DAO(dbAccess);
    PaleoEventDB_DAO paleoEventDAO = new PaleoEventDB_DAO(dbAccess);
    while(rs.next())  {
      // create event sequence
      EventSequence eventSequence = new EventSequence();
      eventSequence.setSequenceId(rs.getInt(SEQUENCE_ID));
      eventSequence.setSequenceName(rs.getString(SEQUENCE_NAME));
      eventSequence.setSiteId(rs.getInt(SITE_ID));
      eventSequence.setSiteEntryDate(rs.getString(SITE_ENTRY_DATE));
      eventSequence.setStartTime(this.timeInstanceDAO.getTimeInstance(rs.getInt(START_TIME_EST_ID)));
      eventSequence.setEndTime(this.timeInstanceDAO.getTimeInstance(rs.getInt(END_TIME_EST_ID)));
      eventSequence.setSequenceEntryDate(rs.getString(ENTRY_DATE));
      eventSequence.setSequenceProb(rs.getFloat(SEQUENCE_PROB));
      eventSequence.setComments(rs.getString(GENERAL_COMMENT));
      // get a list of all the events and missed event probs forming this sequence
      sql = "select "+EVENT_ID+","+MISSED_PROB+" from "+ SEQUENCE_EVENT_LIST_TABLE_NAME+
          " where "+SEQUENCE_ID+"="+eventSequence.getSequenceId()+" and "+
          SEQUENCE_ENTRY_DATE+"='"+eventSequence.getSequenceEntryDate()+"' order by "+
          EVENT_INDEX_IN_SEQUENCE;
      ResultSet eventsResults = dbAccess.queryData(sql);
      ArrayList events  = new ArrayList();
      ArrayList probsList = new ArrayList();
      while(eventsResults.next()){
        events.add(paleoEventDAO.getEvent(eventsResults.getInt(EVENT_ID)));
        probsList.add(new Double(eventsResults.getFloat(MISSED_PROB)));
      }
      events.remove(events.size()-1);
      eventSequence.setEventsParam(events);
      double probs []= new double[probsList.size()];
      for(int i=0; i<probsList.size(); ++i)
        probs[i] = ((Double)probsList.get(i)).doubleValue();
      eventSequence.setMissedEventsProbList(probs);
      sequenceList.add(eventSequence);
    }
    rs.close();
  } catch(SQLException e) { throw new QueryException(e.getMessage()); }

   return sequenceList;
 }


}