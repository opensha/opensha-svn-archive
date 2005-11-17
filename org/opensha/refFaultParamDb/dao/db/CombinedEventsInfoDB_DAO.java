package org.opensha.refFaultParamDb.dao.db;

import org.opensha.refFaultParamDb.vo.CombinedEventsInfo;
import java.sql.SQLException;
import org.opensha.refFaultParamDb.dao.exception.InsertException;
import org.opensha.refFaultParamDb.vo.EstimateInstances;
import org.opensha.refFaultParamDb.gui.infotools.SessionInfo;
import java.util.ArrayList;
import java.sql.ResultSet;
import org.opensha.refFaultParamDb.dao.exception.QueryException;
import org.opensha.refFaultParamDb.vo.Reference;
import org.opensha.refFaultParamDb.vo.CombinedDisplacementInfo;
import org.opensha.refFaultParamDb.vo.CombinedSlipRateInfo;
import org.opensha.refFaultParamDb.vo.CombinedNumEventsInfo;

/**
 * <p>Title: CombinedEventsInfoDB_DAO.java </p>
 * <p>Description: This class interacts with the Combined Events info table in the database.</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class CombinedEventsInfoDB_DAO {
  private final static String TABLE_NAME = "Combined_Events_Info";
  private final static String SEQUENCE_NAME="Combined_Events_Sequence";
  private final static String INFO_ID = "Info_Id";
  private final static String SITE_ID = "Site_Id";
  private final static String SITE_ENTRY_DATE = "Site_Entry_Date";
  private final static String ENTRY_DATE="Entry_Date";
  private final static String CONTRIBUTOR_ID ="Contributor_Id";
  private final static String START_TIME_ID = "Start_Time_Id";
  private final static String END_TIME_ID="End_Time_Id";
  private final static String DATED_FEATURE_COMMENTS = "Dated_Feature_Comments";
  //table for references
  private final static String REFERENCES_TABLE_NAME = "Combined_Events_References";
  private final static String COMBINED_EVENTS_ID = "Combined_Events_Id";
  private final static String COMBINED_EVENTS_ENTRY_DATE="Combined_Events_Entry_Date";
  private final static String REFERENCE_ID= "Reference_Id";
  private final static String IS_EXPERT_OPINION = "Is_Expert_Opinion";
  private final static String IS_RECORD_DELETED = "Is_Record_Deleted";
  private final static String NO = "N";
  private final static String YES = "Y";

  private DB_AccessAPI dbAccess;
  private TimeInstanceDB_DAO timeInstanceDAO;
  private ReferenceDB_DAO referenceDAO;
  private ContributorDB_DAO contributorDAO;
  private CombinedDisplacementInfoDB_DAO combinedDispInfoDB_DAO;
  private CombinedNumEventsInfoDB_DAO combinedNumEventsInfoDB_DAO;
  private CombinedSlipRateInfoDB_DAO combinedSlipRateInfoDB_DAO;
  private EventSequenceDB_DAO eventSequenceDAO;

  public CombinedEventsInfoDB_DAO(DB_AccessAPI dbAccess) {
    setDB_Connection(dbAccess);
  }

  public void setDB_Connection(DB_AccessAPI dbAccess) {
    this.dbAccess = dbAccess;
    timeInstanceDAO = new TimeInstanceDB_DAO(dbAccess);
    referenceDAO = new ReferenceDB_DAO(dbAccess);
    contributorDAO = new ContributorDB_DAO(dbAccess);
    combinedDispInfoDB_DAO = new CombinedDisplacementInfoDB_DAO(dbAccess);
    combinedNumEventsInfoDB_DAO = new CombinedNumEventsInfoDB_DAO(dbAccess);
    combinedSlipRateInfoDB_DAO = new CombinedSlipRateInfoDB_DAO(dbAccess);
    eventSequenceDAO = new EventSequenceDB_DAO(dbAccess);
  }


  /**
   * Add the combined events info into the database
   *
   * @param combinedEventsInfo
   */
  public void addCombinedEventsInfo(CombinedEventsInfo combinedEventsInfo) {
    String systemDate;
    int infoId;
    try {
      infoId = dbAccess.getNextSequenceNumber(SEQUENCE_NAME);
      systemDate = dbAccess.getSystemDate();
    }catch(SQLException e) {
      throw new InsertException(e.getMessage());
    }
    // get the start time Id
    int startTimeId = timeInstanceDAO.addTimeInstance(combinedEventsInfo.getStartTime());
    // get the end time Id
    int endTimeId = timeInstanceDAO.addTimeInstance(combinedEventsInfo.getEndTime());
    String expertOpinion = NO;
    if(combinedEventsInfo.getIsExpertOpinion()) expertOpinion= YES;


    String sql = "insert into "+TABLE_NAME+"("+INFO_ID+","+SITE_ID+","+
        SITE_ENTRY_DATE+","+ENTRY_DATE+","+CONTRIBUTOR_ID+","+
        START_TIME_ID+","+END_TIME_ID+","+
        DATED_FEATURE_COMMENTS+","+IS_EXPERT_OPINION+","+this.IS_EXPERT_OPINION+") "+
        "values ("+infoId+","+combinedEventsInfo.getSiteId()+",'"+
        combinedEventsInfo.getSiteEntryDate()+"','"+systemDate+"',"+
        SessionInfo.getContributor().getId()+","+startTimeId+","+endTimeId+",'"+
        combinedEventsInfo.getDatedFeatureComments()+"','"+expertOpinion+"','"+
        this.NO+"')";

    try {
     dbAccess.insertUpdateOrDeleteData(sql);
     // add displacement info
     CombinedDisplacementInfo combinedDispInfo = combinedEventsInfo.getCombinedDisplacementInfo();
     if(combinedDispInfo!=null) this.combinedDispInfoDB_DAO.addDisplacementInfo(infoId, systemDate, combinedDispInfo);
     // add slip rate info
     CombinedSlipRateInfo combinedSlipRateInfo = combinedEventsInfo.getCombinedSlipRateInfo();
     if(combinedSlipRateInfo!=null) this.combinedSlipRateInfoDB_DAO.addSlipRateInfo(infoId, systemDate, combinedSlipRateInfo);
     // add num events info
     CombinedNumEventsInfo combinedNumEventsInfo = combinedEventsInfo.getCombinedNumEventsInfo();
     if(combinedNumEventsInfo!=null) this.combinedNumEventsInfoDB_DAO.addNumEventsInfo(infoId, systemDate, combinedNumEventsInfo);
     // add the events sequences
     ArrayList eventSequenceList = combinedEventsInfo.getEventSequence();
     if(eventSequenceList!=null && eventSequenceList.size()!=0)
       this.eventSequenceDAO.addEventSequence(infoId, systemDate, eventSequenceList);
     // now insert the references in the combined info references table
     ArrayList referenceList = combinedEventsInfo.getReferenceList();
     for(int i=0; i<referenceList.size(); ++i) {
       int referenceId =((Reference)referenceList.get(i)).getReferenceId();
       sql = "insert into "+this.REFERENCES_TABLE_NAME+"("+COMBINED_EVENTS_ID+
           ","+COMBINED_EVENTS_ENTRY_DATE+","+REFERENCE_ID+") "+
           "values ("+infoId+",'"+
           systemDate+"',"+referenceId+")";
       dbAccess.insertUpdateOrDeleteData(sql);
     }
   }
   catch(SQLException e) {
     e.printStackTrace();
     throw new InsertException(e.getMessage());
   }
  }


  /**
   * Get the combined events info list for a particular site
   *
   * @param siteId
   * @return
   */
  public ArrayList getCombinedEventsInfoList(int siteId) {
    String condition = " where "+SITE_ID+"="+siteId+" and "+this.IS_RECORD_DELETED+
        "='"+this.NO+"'";
    return query(condition);
  }

  /**
   * Query the combined info table based on condition
   *
   * @param condition
   * @return
   */
  private ArrayList query(String condition) {
    ArrayList combinedInfoList = new ArrayList();
    String sql =  "select "+INFO_ID+","+SITE_ID+",to_char("+SITE_ENTRY_DATE+") as "+SITE_ENTRY_DATE+","+
        "to_char("+ENTRY_DATE+") as "+ENTRY_DATE+","+
        START_TIME_ID+","+END_TIME_ID+","+this.CONTRIBUTOR_ID+","+DATED_FEATURE_COMMENTS+
        ","+IS_EXPERT_OPINION+" from "+this.TABLE_NAME+condition;
    try {
      ResultSet rs  = dbAccess.queryData(sql);
      int estId;
      while(rs.next())  {
        CombinedEventsInfo combinedEventsInfo = new  CombinedEventsInfo();
        combinedEventsInfo.setInfoId(rs.getInt(INFO_ID));
        combinedEventsInfo.setEntryDate(rs.getString(ENTRY_DATE));
        combinedEventsInfo.setSiteId(rs.getInt(SITE_ID));
        combinedEventsInfo.setSiteEntryDate(rs.getString(SITE_ENTRY_DATE));
        combinedEventsInfo.setStartTime(this.timeInstanceDAO.getTimeInstance(rs.getInt(START_TIME_ID)));
        combinedEventsInfo.setEndTime(this.timeInstanceDAO.getTimeInstance(rs.getInt(END_TIME_ID)));
        combinedEventsInfo.setDatedFeatureComments(rs.getString(DATED_FEATURE_COMMENTS));
        // set displacement
        combinedEventsInfo.setCombinedDisplacementInfo(
            this.combinedDispInfoDB_DAO.getDisplacementInfo(rs.getInt(INFO_ID), rs.getString(ENTRY_DATE)));
        // set num events info
        combinedEventsInfo.setCombinedNumEventsInfo(
            this.combinedNumEventsInfoDB_DAO.getCombinedNumEventsInfo(rs.getInt(INFO_ID), rs.getString(ENTRY_DATE)));
        // set slip rate info
        combinedEventsInfo.setCombinedSlipRateInfo(
            this.combinedSlipRateInfoDB_DAO.getCombinedSlipRateInfo(rs.getInt(INFO_ID), rs.getString(ENTRY_DATE)));
        // set the sequences info
        combinedEventsInfo.setEventSequenceList(
            this.eventSequenceDAO.getSequences(rs.getInt(INFO_ID), rs.getString(ENTRY_DATE)));
        // get the contributor info
        combinedEventsInfo.setContributorName(this.contributorDAO.getContributor(rs.getInt(this.CONTRIBUTOR_ID)).getName());
        if(rs.getString(IS_EXPERT_OPINION).equalsIgnoreCase(YES))
         combinedEventsInfo.setIsExpertOpinion(true);
       else combinedEventsInfo.setIsExpertOpinion(false);

        // get all the references for this site
        ArrayList referenceList = new ArrayList();
        sql = "select "+REFERENCE_ID+" from "+this.REFERENCES_TABLE_NAME+
            " where "+COMBINED_EVENTS_ID+"="+combinedEventsInfo.getInfoId()+" and "+
            COMBINED_EVENTS_ENTRY_DATE+"='"+combinedEventsInfo.getEntryDate()+"'";
        ResultSet referenceResultSet = dbAccess.queryData(sql);
        while(referenceResultSet.next()) {
          referenceList.add(referenceDAO.getReference(referenceResultSet.getInt(REFERENCE_ID)));
        }
        referenceResultSet.close();

        // set the references in the VO
        combinedEventsInfo.setReferenceList(referenceList);
        combinedInfoList.add(combinedEventsInfo);
      }
      rs.close();
    } catch(SQLException e) {
      e.printStackTrace();
      throw new QueryException(e.getMessage());
    }
    return combinedInfoList;
  }

}