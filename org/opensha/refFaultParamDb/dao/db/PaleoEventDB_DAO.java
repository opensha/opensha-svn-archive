package org.opensha.refFaultParamDb.dao.db;

import org.opensha.refFaultParamDb.dao.exception.InsertException;
import org.opensha.refFaultParamDb.vo.PaleoEvent;
import java.sql.SQLException;
import org.opensha.refFaultParamDb.gui.infotools.SessionInfo;
import java.util.ArrayList;
import org.opensha.refFaultParamDb.dao.exception.QueryException;
import java.sql.ResultSet;

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
  private final static String PALEO_EVENT_ENTRY_DATE ="Paleo_Event_Entry_Date";
  private final static String PALEO_EVENT_ID="Paleo_Event_Id";


  private DB_AccessAPI dbAccess;
  // references DAO
  private ReferenceDB_DAO referenceDAO ;
  private TimeInstanceDB_DAO timeInstanceDAO;
  private EstimateInstancesDB_DAO estimateInstancesDAO;

  public PaleoEventDB_DAO(DB_AccessAPI dbAccess) {
    setDB_Connection(dbAccess);
  }

  public void setDB_Connection(DB_AccessAPI dbAccess) {
   this.dbAccess = dbAccess;
   referenceDAO = new ReferenceDB_DAO(dbAccess);
   timeInstanceDAO = new TimeInstanceDB_DAO(dbAccess);
   estimateInstancesDAO = new EstimateInstancesDB_DAO(this.dbAccess);
  }

  /**
  * Add a new paleo event
  *
  * @param paleoEvent
  * @throws InsertException
  */
  public void addPaleoevent(PaleoEvent paleoEvent) throws InsertException {
    int paleoEventId, eventTimeEstId, displacementEstId;
    String systemDate;
    try {
        paleoEventId = dbAccess.getNextSequenceNumber(SEQUENCE_NAME);
        systemDate = dbAccess.getSystemDate();
        eventTimeEstId = timeInstanceDAO.addTimeInstance(paleoEvent.getEventTime());
        if(paleoEvent.isDisplacementShared()) {
       // if displacement is shared, it is assumed that displacement id is  already set
          displacementEstId = paleoEvent.getDisplacementEstId();
        }
        else {
          // if displacement is not shared, add the time estimate and get the estimate Id
          displacementEstId = this.estimateInstancesDAO.addEstimateInstance(
              paleoEvent.getDisplacementEst());
        }
    }catch(SQLException e) {
      throw new InsertException(e.getMessage());
    }

    String sql = "insert into "+TABLE_NAME+"("+ EVENT_ID+","+EVENT_NAME+","+
        SITE_ID+","+SITE_ENTRY_DATE+","+CONTRIBUTOR_ID+","+EVENT_DATE_EST_ID+","+
        DISPLACEMENT_EST_ID+","+ENTRY_DATE+","+GENERAL_COMMENTS+")"+
        " values ("+paleoEventId+",'"+paleoEvent.getEventName()+"',"+paleoEvent.getSiteId()+
        ",'"+paleoEvent.getSiteEntryDate()+"',"+SessionInfo.getContributor().getId()+
        ","+eventTimeEstId+","+displacementEstId+
        ",'"+systemDate+"','"+paleoEvent.getComments()+"')";

    try {
      // insert into paleo event table
      dbAccess.insertUpdateOrDeleteData(sql);
      //add the references (for this paleo event) into the database
      ArrayList shortCitationList = paleoEvent.getShortCitationsList();
      for(int i=0; i<shortCitationList.size(); ++i) {
        int referenceId = referenceDAO.getReference((String)shortCitationList.get(i)).getReferenceId();
        sql = "insert into "+this.REFERENCES_TABLE_NAME+"("+PALEO_EVENT_ID+
            ","+PALEO_EVENT_ENTRY_DATE+","+REFERENCE_ID+") "+
            "values ("+paleoEventId+",'"+
            systemDate+"',"+referenceId+")";
        dbAccess.insertUpdateOrDeleteData(sql);
      }
    }
    catch(SQLException e) {
      throw new InsertException(e.getMessage());
    }
  }

  /**
   * Check whether the passed in event names share the same displacement.
   * If they share same displacement, the diplacement id is returned else
   * -1 is returned
   *
   * @param eventNames
   * @return
   */
  public int checkSameDisplacement(ArrayList eventNames) {
    String values ="(";
    for(int i=0; i<eventNames.size();++i) {
      values = values + "'" + (String) eventNames.get(i) + "'";
      if(i!=eventNames.size()-1) values= values+",";
    }
    values = values+")";
    String sql = "select "+this.DISPLACEMENT_EST_ID+" from "+this.TABLE_NAME+
        " where "+this.EVENT_NAME+" in "+values;
    int dispEstId = -1;
    try {
      ResultSet rs = dbAccess.queryData(sql);
      while (rs.next()) {
        if (dispEstId != -1 && dispEstId != rs.getInt(DISPLACEMENT_EST_ID))
          return -1;
        dispEstId = rs.getInt(DISPLACEMENT_EST_ID);
      }
    }catch(SQLException sqlException) {
      throw new QueryException(sqlException.getMessage());
    }

    return dispEstId;
  }

  /**
   * Get a list of all events for this site
   * It returns an ArrayList of PaleoEvent objects
   * @param siteId
   * @return
   */
  public ArrayList getAllEvents(int siteId)  throws QueryException {
     String condition = " where "+this.SITE_ID+"="+siteId;
     return query(condition);
  }

  public PaleoEvent getEvent(int eventId) throws QueryException {
    String condition = " where "+this.EVENT_ID+"="+eventId;
    ArrayList paleoEventList = query(condition);
    PaleoEvent paleoEvent = null;
    if(paleoEventList.size()>0) paleoEvent = (PaleoEvent)paleoEventList.get(0);
    return paleoEvent;

  }


  /**
   * Get a list of all event names sharing the given displacement estimate Id
   * @param displacementEstId
   * @return
   * @throws QueryException
   */
  public ArrayList getEventNamesForDisplacement(int displacementEstId) throws QueryException {
    ArrayList eventNames = new ArrayList();
    String sql = "select "+this.EVENT_NAME+" from "+this.TABLE_NAME+" where "+
        this.DISPLACEMENT_EST_ID+"="+displacementEstId;
    try {
      ResultSet rs = dbAccess.queryData(sql);
      while(rs.next()) eventNames.add(rs.getString(this.EVENT_NAME));
    }catch(SQLException e) { throw new QueryException(e.getMessage()); }
    return eventNames;
  }


  /**
   * Query the paleo event table to get paleo events based on the condition
   * @param condition
   * @return
   */
  private ArrayList query(String condition) {
    ArrayList paleoEventList = new ArrayList();
    String sql = "select "+EVENT_ID+","+EVENT_NAME+","+
        SITE_ID+",to_char("+SITE_ENTRY_DATE+") as "+SITE_ENTRY_DATE+","+
        CONTRIBUTOR_ID+","+EVENT_DATE_EST_ID+","+
        DISPLACEMENT_EST_ID+",to_char("+ENTRY_DATE+") as "+ENTRY_DATE+","+
        GENERAL_COMMENTS+" from "+
        this.TABLE_NAME+" "+condition;
    try {
     ResultSet rs  = dbAccess.queryData(sql);
     ContributorDB_DAO contributorDAO = new ContributorDB_DAO(dbAccess);
     while(rs.next())  {
       // create paleo event
       PaleoEvent paleoEvent= new PaleoEvent();
       paleoEvent.setEventId(rs.getInt(EVENT_ID));
       paleoEvent.setEventName(rs.getString(EVENT_NAME));
       paleoEvent.setSiteId(rs.getInt(this.SITE_ID));
       paleoEvent.setSiteEntryDate(rs.getString(this.SITE_ENTRY_DATE));
       paleoEvent.setContributorName(contributorDAO.getContributor(rs.getInt(CONTRIBUTOR_ID)).getName());
       paleoEvent.setEventTime(this.timeInstanceDAO.getTimeInstance(rs.getInt(EVENT_DATE_EST_ID)));
       paleoEvent.setDisplacementEstId(rs.getInt(DISPLACEMENT_EST_ID));
       paleoEvent.setDisplacementEst(this.estimateInstancesDAO.getEstimateInstance(rs.getInt(DISPLACEMENT_EST_ID)));
       paleoEvent.setEntryDate(rs.getString(ENTRY_DATE));
       paleoEvent.setComments(rs.getString(GENERAL_COMMENTS));

       // get all the references for this site
       ArrayList referenceList = new ArrayList();
       sql = "select "+REFERENCE_ID+" from "+this.REFERENCES_TABLE_NAME+
           " where "+this.PALEO_EVENT_ID+"="+paleoEvent.getEventId()+" and "+
           this.PALEO_EVENT_ENTRY_DATE+"='"+paleoEvent.getEntryDate()+"'";
       ResultSet referenceResultSet = dbAccess.queryData(sql);
       while(referenceResultSet.next()) {
         referenceList.add(referenceDAO.getReference(referenceResultSet.getInt(REFERENCE_ID)).getShortCitation());
       }
       referenceResultSet.close();
       // set the references in the VO
       paleoEvent.setShortCitationsList(referenceList);
       paleoEventList.add(paleoEvent);
     }
     rs.close();
   } catch(SQLException e) { throw new QueryException(e.getMessage()); }

    return paleoEventList;

  }



}