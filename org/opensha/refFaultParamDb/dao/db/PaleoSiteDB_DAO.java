package org.opensha.refFaultParamDb.dao.db;

import java.sql.SQLException;
import java.sql.ResultSet;
import org.opensha.refFaultParamDb.vo.PaleoSite;
import org.opensha.refFaultParamDb.vo.Contributor;
import org.opensha.refFaultParamDb.vo.SiteType;
import org.opensha.refFaultParamDb.dao.exception.*;
import java.util.ArrayList;
import org.opensha.refFaultParamDb.dao.*;
import java.sql.Date;
import java.sql.Timestamp;
import org.opensha.refFaultParamDb.vo.PaleoSiteSummary;
import org.opensha.refFaultParamDb.gui.infotools.SessionInfo;
import org.opensha.refFaultParamDb.vo.Reference;
import oracle.spatial.geometry.JGeometry;
import oracle.sql.STRUCT;
import java.util.HashMap;
import org.opensha.refFaultParamDb.vo.EstimateInstances;

/**
 * <p>Title: PaleoSiteDB_DAO.java </p>
 * <p>Description: Performs insert/delete/update on PaleoSite table on oracle database</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */


public class PaleoSiteDB_DAO  {
  private final static String SEQUENCE_NAME = "Paleo_Site_Sequence";
  private final static String TABLE_NAME="Paleo_Site";
  private final static String VIEW_NAME = "Vw_Paleo_Site_Chars";
  private final static String REFERENCES_TABLE_NAME = "Paleo_Site_References";
  private final static String SITE_ID="Site_Id";
  private final static String FAULT_ID="Fault_Id";
  private final static String ENTRY_DATE="Entry_Date";
  private final static String CONTRIBUTOR_ID="Contributor_Id";
  private final static String SITE_NAME="Site_Name";
  private final static String SITE_LOCATION1="Site_Location1";
  private final static String SITE_LOCATION2="Site_Location2";
  private final static String REPRESENTATIVE_STRAND_INDEX="Representative_Strand_Index";
  private final static String GENERAL_COMMENTS="General_Comments";
  private final static String OLD_SITE_ID="Old_Site_Id";
  private final static String REFERENCE_ID="Reference_Id";
  private final static String DIP_EST_ID = "Dip_Est_Id";
  private final static String PALEO_SITE_STUDY_TABLE_NAME = "Paleo_Site_Study";
  private final static String SITE_TYPE_ID = "Site_Type_Id";
  private final static int SRID=8307;

  private DB_AccessAPI dbAccess;
  // site type DAO
  private SiteTypeDB_DAO siteTypeDAO ;
  // references DAO
  private ReferenceDB_DAO referenceDAO ;
  // site representations DAO
  private SiteRepresentationDB_DAO siteRepresentationDAO;
  // estimate instance DAO
  private EstimateInstancesDB_DAO estimateInstancesDAO;
  // fault DAO
  private FaultDB_DAO faultDAO;



  public PaleoSiteDB_DAO(DB_AccessAPI dbAccess) {
    setDB_Connection(dbAccess);
  }

  public void setDB_Connection(DB_AccessAPI dbAccess) {
   this.dbAccess = dbAccess;
   siteTypeDAO = new SiteTypeDB_DAO(DB_AccessAPI.dbConnection);
   referenceDAO = new ReferenceDB_DAO(DB_AccessAPI.dbConnection);
   siteRepresentationDAO = new SiteRepresentationDB_DAO(DB_AccessAPI.dbConnection);
   faultDAO = new FaultDB_DAO(DB_AccessAPI.dbConnection);
   estimateInstancesDAO = new EstimateInstancesDB_DAO(DB_AccessAPI.dbConnection);
 }

 /**
  * Add a new paleo site
  *
  * @param paleoSite
  * @throws InsertException
  */
  public void addPaleoSite(PaleoSite paleoSite) throws InsertException {
    int paleoSiteId = paleoSite.getSiteId();
    String systemDate;
    try {
      if(paleoSiteId<=0)
        paleoSiteId = dbAccess.getNextSequenceNumber(SEQUENCE_NAME);
        systemDate = dbAccess.getSystemDate();
    }catch(SQLException e) {
      throw new InsertException(e.getMessage());
    }

    int faultId = faultDAO.getFault(paleoSite.getFaultName()).getFaultId();
    int siteRepresentationId = siteRepresentationDAO.getSiteRepresentation(paleoSite.getRepresentativeStrandName()).getSiteRepresentationId();

    JGeometry location1 = new JGeometry(paleoSite.getSiteLon1(),
                                        paleoSite.getSiteLat1(),
                                        paleoSite.getSiteElevation1(),
                                        SRID);
    JGeometry location2 = new JGeometry(paleoSite.getSiteLon2(),
                                        paleoSite.getSiteLat2(),
                                        paleoSite.getSiteElevation2(),
                                        SRID);
    ArrayList geomteryObjectList = new ArrayList();
    geomteryObjectList.add(location1);
    geomteryObjectList.add(location2);
    String dipColName="", dipVal="";
    EstimateInstances dipEst = paleoSite.getDipEstimate();
    if(dipEst!=null) {
      dipColName = this.DIP_EST_ID+",";
      int id = this.estimateInstancesDAO.addEstimateInstance(dipEst);
      dipVal=""+id+",";
    }
    String sql = "insert into "+TABLE_NAME+"("+ SITE_ID+","+FAULT_ID+","+
        ENTRY_DATE+","+CONTRIBUTOR_ID+","+SITE_NAME+","+SITE_LOCATION1+","+
        SITE_LOCATION2+","+dipColName+REPRESENTATIVE_STRAND_INDEX+","+
        GENERAL_COMMENTS+","+OLD_SITE_ID+") "+
        " values ("+paleoSiteId+","+faultId+",'"+systemDate+
        "',"+SessionInfo.getContributor().getId()+
        ",'"+paleoSite.getSiteName()+"',?,?,"+dipVal+siteRepresentationId+
        ",'"+paleoSite.getGeneralComments()+"','"+paleoSite.getOldSiteId()+"')";

    try {
      dbAccess.insertUpdateOrDeleteData(sql, geomteryObjectList);
      // put the site references into another table
      ArrayList referenceList = paleoSite.getReferenceList();
      for(int i=0; i<referenceList.size(); ++i) {
        int referenceId = ((Reference)referenceList.get(i)).getReferenceId();
        sql = "insert into "+this.REFERENCES_TABLE_NAME+"("+SITE_ID+
            ","+ENTRY_DATE+","+REFERENCE_ID+") "+
            "values ("+paleoSiteId+",'"+
            systemDate+"',"+referenceId+")";
        dbAccess.insertUpdateOrDeleteData(sql);
      }
      // put site types (study types) into another table
      ArrayList siteTypeNames = paleoSite.getSiteTypeNames();
      for(int i=0; i<siteTypeNames.size(); ++i) {
        int siteTypeId = siteTypeDAO.getSiteType((String)siteTypeNames.get(i)).getSiteTypeId();
        sql = "insert into "+this.PALEO_SITE_STUDY_TABLE_NAME+"("+SITE_ID+","+
            ENTRY_DATE+","+SITE_TYPE_ID+") values ("+paleoSiteId+",'"+systemDate+
            "',"+siteTypeId+")";
        dbAccess.insertUpdateOrDeleteData(sql);
      }
    }
    catch(SQLException e) {
      e.printStackTrace();
      throw new InsertException(e.getMessage());
    }
  }


  /**
   * Get paleo site data based on paleoSiteId
   * @param paleoSiteId
   * @return
   * @throws QueryException
   */
  public PaleoSite getPaleoSite(int paleoSiteId) throws QueryException {
    String condition = " where "+SITE_ID+"="+paleoSiteId;
    ArrayList paleoSiteList = query(condition);
    PaleoSite paleoSite = null;
    if(paleoSiteList.size()>0) paleoSite = (PaleoSite)paleoSiteList.get(0);
    return paleoSite;
  }

  /**
  * It returns a list of PaleoSiteSummary objects. Each such object has a name
  * and id. If there is no name corresponding to paleo site in the database,
  * then this function gets the references for the paleo site and sets it as the name
  * which can then be used subsequently.
  *
  * @return
  * @throws QueryException
  */
 public ArrayList getAllPaleoSiteNames() throws QueryException {
   ArrayList paleoSiteSummaryList = new ArrayList();
   String sql =  "select "+SITE_ID+","+SITE_NAME+" from "+this.VIEW_NAME+" order by "+this.SITE_NAME;
   try {
     ResultSet rs  = dbAccess.queryData(sql);
     while(rs.next())  {
       PaleoSiteSummary paleoSiteSummary = new PaleoSiteSummary();
       paleoSiteSummary.setSiteId(rs.getInt(SITE_ID));
       paleoSiteSummary.setSiteName(rs.getString(SITE_NAME));
       paleoSiteSummaryList.add(paleoSiteSummary);
     }
     rs.close();
   } catch(SQLException e) { throw new QueryException(e.getMessage()); }
   return paleoSiteSummaryList;
 }


  /**
   * remove a paleo site from the database
   * @param paleoSiteId
   * @return
   * @throws UpdateException
   */
  public boolean removePaleoSite(int paleoSiteId) throws UpdateException {
    String sql = "delete from "+TABLE_NAME+"  where "+SITE_ID+"="+paleoSiteId;
    try {
      int numRows = dbAccess.insertUpdateOrDeleteData(sql);
      if(numRows==1) return true;
    }
    catch(SQLException e) { throw new UpdateException(e.getMessage()); }
    return false;
  }


  /**
   * Get all the paleo sites from the database
   * @return
   * @throws QueryException
   */
  public ArrayList getAllPaleoSites() throws QueryException {
   return query(" ");
  }



  private ArrayList query(String condition) throws QueryException {
    ArrayList paleoSiteList = new ArrayList();
    String sqlWithSpatialColumnNames =  "select "+SITE_ID+","+FAULT_ID+",to_char("+ENTRY_DATE+") as "+ENTRY_DATE+
        ","+SITE_NAME+","+SITE_LOCATION1+","+
        SITE_LOCATION2+","+SiteRepresentationDB_DAO.SITE_REPRESENTATION_NAME+","+
        DIP_EST_ID+","+GENERAL_COMMENTS+","+OLD_SITE_ID+","+ContributorDB_DAO.CONTRIBUTOR_NAME+
        " from "+VIEW_NAME+condition;
    String sqlWithNoSpatialColumnNames =  "select "+SITE_ID+","+FAULT_ID+",to_char("+ENTRY_DATE+") as "+ENTRY_DATE+
    ","+SITE_NAME+","+SiteRepresentationDB_DAO.SITE_REPRESENTATION_NAME+","+
    DIP_EST_ID+","+GENERAL_COMMENTS+","+OLD_SITE_ID+","+ContributorDB_DAO.CONTRIBUTOR_NAME+
    " from "+VIEW_NAME+condition;

    ArrayList spatialColumnNames = new ArrayList();
    spatialColumnNames.add(SITE_LOCATION1);
    spatialColumnNames.add(SITE_LOCATION2);
    try {
      SpatialQueryResult spatialQueryResult  = dbAccess.queryData(sqlWithSpatialColumnNames, sqlWithNoSpatialColumnNames, spatialColumnNames);
      ResultSet rs = spatialQueryResult.getCachedRowSet();
      int i=0;
      while(rs.next())  {
        PaleoSite paleoSite = new PaleoSite();
        paleoSite.setSiteId(rs.getInt(SITE_ID));
        paleoSite.setEntryDate(rs.getString(ENTRY_DATE));
        paleoSite.setFaultName(faultDAO.getFault(rs.getInt(FAULT_ID)).getFaultName());

        // get all the study types for this site
        String sql = "select "+SITE_TYPE_ID+" from "+this.PALEO_SITE_STUDY_TABLE_NAME+
            " where "+this.SITE_ID+"="+paleoSite.getSiteId()+" and "+
            ENTRY_DATE+"='"+rs.getString(ENTRY_DATE)+"'";;
        ResultSet studyTypeResultSet = dbAccess.queryData(sql);
        ArrayList studyTypes = new ArrayList();
        while(studyTypeResultSet.next()) {
          studyTypes.add(this.siteTypeDAO.getSiteType(studyTypeResultSet.getInt(SITE_TYPE_ID)).getSiteType());
        }
        paleoSite.setSiteTypeNames(studyTypes);

        paleoSite.setSiteName(rs.getString(SITE_NAME));
        // location 1
        ArrayList geometries = spatialQueryResult.getGeometryObjectsList(i++);
        JGeometry location1 =(JGeometry) geometries.get(0);
        double []point1 = location1.getPoint();
        //location 2
        JGeometry location2 = (JGeometry) geometries.get(1);
        double []point2 = location2.getPoint();

        paleoSite.setSiteLat1((float)point1[1]);
        paleoSite.setSiteLon1((float)point1[0]);
        paleoSite.setSiteElevation1((float)point1[2]);
        paleoSite.setSiteLat2((float)point2[1]);
        paleoSite.setSiteLon2((float)point2[0]);
        paleoSite.setSiteElevation2((float)point2[2]);
        paleoSite.setRepresentativeStrandName(rs.getString(SiteRepresentationDB_DAO.SITE_REPRESENTATION_NAME));
        int dipEstId = rs.getInt(this.DIP_EST_ID);
        if(!rs.wasNull()) paleoSite.setDipEstimate(this.estimateInstancesDAO.getEstimateInstance(dipEstId));

        paleoSite.setGeneralComments(rs.getString(GENERAL_COMMENTS));
        paleoSite.setOldSiteId(rs.getString(OLD_SITE_ID));
        paleoSite.setContributorName(rs.getString(ContributorDB_DAO.CONTRIBUTOR_NAME));
        // get all the references for this site
        ArrayList referenceList = new ArrayList();
        sql = "select "+REFERENCE_ID+" from "+this.REFERENCES_TABLE_NAME+
            " where "+SITE_ID+"="+paleoSite.getSiteId()+" and "+
            ENTRY_DATE+"='"+rs.getString(ENTRY_DATE)+"'";
        ResultSet referenceResultSet = dbAccess.queryData(sql);
        while(referenceResultSet.next()) {
          referenceList.add(referenceDAO.getReference(referenceResultSet.getInt(REFERENCE_ID)));
        }
        referenceResultSet.close();
        // set the references in the VO
        paleoSite.setReferenceList(referenceList);
        paleoSiteList.add(paleoSite);
      }
      rs.close();
    } catch(SQLException e) { throw new QueryException(e.getMessage()); }
    return paleoSiteList;
  }

}
