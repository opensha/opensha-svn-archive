package org.opensha.refFaultParamDb.dao.db;

import org.opensha.refFaultParamDb.vo.CombinedEventsInfo;
import java.sql.SQLException;
import org.opensha.refFaultParamDb.dao.exception.InsertException;
import org.opensha.refFaultParamDb.vo.EstimateInstances;
import org.opensha.refFaultParamDb.gui.infotools.SessionInfo;
import java.util.ArrayList;
import java.sql.ResultSet;
import org.opensha.refFaultParamDb.dao.exception.QueryException;

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
  private final static String TOTAL_SLIP_EST_ID = "Total_Slip_Est_Id";
  private final static String SLIP_RATE_EST_ID= "Slip_Rate_Est_Id";
  private final static String NUM_EVENTS_EST_ID = "Num_Events_Est_Id";
  private final static String ASEISMIC_SLIP_FACTOR_EST_ID="Aseismic_Slip_Factor_Est_Id";
  private final static String SLIP_RATE_COMMENTS="Slip_Rate_Comments";
  private final static String TOTAL_SLIP_COMMENTS="Total_Slip_Comments";
  private final static String NUM_EVENTS_COMMENTS = "Num_Events_Comments";
  private final static String DATED_FEATURE_COMMENTS = "Dated_Feature_Comments";
  //table for references
  private final static String REFERENCES_TABLE_NAME = "Combined_Events_References";
  private final static String COMBINED_EVENTS_ID = "Combined_Events_Id";
  private final static String COMBINED_EVENTS_ENTRY_DATE="Combined_Events_Entry_Date";
  private final static String REFERENCE_ID= "Reference_Id";

  private DB_AccessAPI dbAccess;
  private TimeInstanceDB_DAO timeInstanceDAO;
  private EstimateInstancesDB_DAO estimateInstancesDAO;
  private ReferenceDB_DAO referenceDAO;

  public CombinedEventsInfoDB_DAO(DB_AccessAPI dbAccess) {
    setDB_Connection(dbAccess);
  }

  public void setDB_Connection(DB_AccessAPI dbAccess) {
    this.dbAccess = dbAccess;
    timeInstanceDAO = new TimeInstanceDB_DAO(dbAccess);
    estimateInstancesDAO = new EstimateInstancesDB_DAO(dbAccess);
    referenceDAO = new ReferenceDB_DAO(dbAccess);
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

    String estColNames=" ", estColVals=" ";
    // put displacement estimate into database
    int totalSlipEstId = getEstimateId(combinedEventsInfo.getDisplacementEstimate());
    if(totalSlipEstId!=-1) {
      estColNames += TOTAL_SLIP_EST_ID+",";
      estColVals += totalSlipEstId+",";
    }
    // put slip rate estimate into database
    int slipRateEstId = getEstimateId(combinedEventsInfo.getSlipRateEstimate());
    if(slipRateEstId!=-1) {
     estColNames += SLIP_RATE_EST_ID+",";
     estColVals += slipRateEstId+",";
   }
    // put num events estimates into database
    int numEventsEstId = getEstimateId(combinedEventsInfo.getNumEventsEstimate());
    if(numEventsEstId!=-1) {
     estColNames += NUM_EVENTS_EST_ID+",";
     estColVals += numEventsEstId+",";
    }
    // put asesimic slipfactor estimate in database
    int aSeismicEstId = getEstimateId(combinedEventsInfo.getASeismicSlipFactorEstimate());
    if(aSeismicEstId!=-1) {
     estColNames += ASEISMIC_SLIP_FACTOR_EST_ID+",";
     estColVals += aSeismicEstId+",";
   }


    // comments to be put in database
    String totalSlipComments= getComments(combinedEventsInfo.getDisplacementComments());
    String slipRateComments=getComments(combinedEventsInfo.getSlipRateComments());
    String numEventsComments=getComments(combinedEventsInfo.getNumEventsComments());

    String sql = "insert into "+TABLE_NAME+"("+INFO_ID+","+SITE_ID+","+
        SITE_ENTRY_DATE+","+ENTRY_DATE+","+CONTRIBUTOR_ID+","+
        START_TIME_ID+","+END_TIME_ID+","+estColNames+
        SLIP_RATE_COMMENTS+","+TOTAL_SLIP_COMMENTS+","+NUM_EVENTS_COMMENTS+","+
        DATED_FEATURE_COMMENTS+") values ("+infoId+","+combinedEventsInfo.getSiteId()+",'"+
        combinedEventsInfo.getSiteEntryDate()+"','"+systemDate+"',"+
        SessionInfo.getContributor().getId()+","+startTimeId+","+endTimeId+","+
        estColVals+"'"+slipRateComments+"','"+totalSlipComments+"','"+numEventsComments+"','"+
        combinedEventsInfo.getDatedFeatureComments()+"')";

    try {
     dbAccess.insertUpdateOrDeleteData(sql);
      // now insert the references in the combined info references table
     ArrayList shortCitationList = combinedEventsInfo.getShortCitationList();
     for(int i=0; i<shortCitationList.size(); ++i) {
       int referenceId = referenceDAO.getReference((String)shortCitationList.get(i)).getReferenceId();
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
   * Get the comments. If comments are null ,return " "
   * @param comments
   * @return
   */
  private String getComments(String comments) {
    String comm = " ";
    if(comments ==null) comm=comments;
    return comm;
  }

  /**
   * It inserts the estInstance (if it is not null) and returns the id of the inserted row.
   * If estInstance is null, it returns -1 as the index
   * @param estInstance
   */
  private int getEstimateId(EstimateInstances estInstance) {
    int estId = -1;
    if(estInstance!=null) estId = estimateInstancesDAO.addEstimateInstance(estInstance);
    return estId;
  }

  /**
   * Get the combined events info list for a particular site
   *
   * @param siteId
   * @return
   */
  public ArrayList getCombinedEventsInfoList(int siteId) {
    String condition = " where "+SITE_ID+"="+siteId;
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
        START_TIME_ID+","+END_TIME_ID+","+TOTAL_SLIP_EST_ID+","+
        SLIP_RATE_EST_ID+","+NUM_EVENTS_EST_ID+","+ASEISMIC_SLIP_FACTOR_EST_ID+","+
        SLIP_RATE_COMMENTS+","+TOTAL_SLIP_COMMENTS+","+NUM_EVENTS_COMMENTS+","+
        DATED_FEATURE_COMMENTS+" from "+this.TABLE_NAME+condition;
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
        // set total slip if it is not null in database
        estId = rs.getInt(TOTAL_SLIP_EST_ID);
        if(!rs.wasNull()) {
          combinedEventsInfo.setDisplacementEstimate(this.estimateInstancesDAO.getEstimateInstance(estId));
          combinedEventsInfo.setDisplacementComments(rs.getString(TOTAL_SLIP_COMMENTS));
        }
        //set slip rate
        estId = rs.getInt(SLIP_RATE_EST_ID);
        if(!rs.wasNull()) {
         combinedEventsInfo.setSlipRateEstimate(this.estimateInstancesDAO.getEstimateInstance(estId));
         combinedEventsInfo.setSlipRateComments(rs.getString(SLIP_RATE_COMMENTS));
       }
       // set num events estimate
       estId = rs.getInt(NUM_EVENTS_EST_ID);
       if(!rs.wasNull()) {
         combinedEventsInfo.setNumEventsEstimate(this.estimateInstancesDAO.getEstimateInstance(estId));
         combinedEventsInfo.setNumEventsComments(rs.getString(NUM_EVENTS_COMMENTS));
       }
       //set asesimic slip factor estimate
       estId = rs.getInt(ASEISMIC_SLIP_FACTOR_EST_ID);
       if(!rs.wasNull()) {
         combinedEventsInfo.setASeismicSlipFactorEstimate(this.estimateInstancesDAO.getEstimateInstance(estId));
       }

        // get all the refernces for this site
        ArrayList referenceList = new ArrayList();
        sql = "select "+REFERENCE_ID+" from "+this.REFERENCES_TABLE_NAME+
            " where "+COMBINED_EVENTS_ID+"="+combinedEventsInfo.getInfoId()+" and "+
            COMBINED_EVENTS_ENTRY_DATE+"='"+combinedEventsInfo.getEntryDate()+"'";
        ResultSet referenceResultSet = dbAccess.queryData(sql);
        while(referenceResultSet.next()) {
          referenceList.add(referenceDAO.getReference(referenceResultSet.getInt(REFERENCE_ID)).getShortCitation());
        }
        referenceResultSet.close();
        // set the references in the VO
        combinedEventsInfo.setShortCitationList(referenceList);
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