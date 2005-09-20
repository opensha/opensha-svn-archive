package org.opensha.refFaultParamDb.dao.db;

import java.sql.SQLException;
import java.sql.ResultSet;
import org.opensha.refFaultParamDb.dao.PaleoSiteDAO_API;
import org.opensha.refFaultParamDb.vo.PaleoSite;
import org.opensha.refFaultParamDb.vo.Contributor;
import org.opensha.refFaultParamDb.vo.SiteType;
import org.opensha.refFaultParamDb.dao.exception.*;
import java.util.ArrayList;
import org.opensha.refFaultParamDb.dao.*;
import java.sql.Date;

/**
 * <p>Title: PaleoSiteDB_DAO.java </p>
 * <p>Description: Performs insert/delete/update on PaleoSite table on oracle database</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */


public class PaleoSiteDB_DAO implements PaleoSiteDAO_API {
  private final static String SEQUENCE_NAME = "Paleo_Site_Sequence";
  private final static String TABLE_NAME="Paleo_Site";
  private final static String REFERNCES_TABLE_NAME = "Paleo_Site_References";
  private final static String SITE_ID="Site_Id";
  private final static String FAULT_ID="Fault_Id";
  private final static String ENTRY_DATE="Entry_Date";
  private final static String ENTRY_COMMENTS="Entry_Comments";
  private final static String CONTRIBUTOR_ID="Contributor_Id";
  private final static String SITE_TYPE_ID="Site_Type_Id";
  private final static String SITE_NAME="Site_Name";
  private final static String SITE_LAT1="Site_Lat1";
  private final static String SITE_LON1="Site_Lon1";
  private final static String SITE_ELEVATION1="Site_Elevation1";
  private final static String SITE_LAT2="Site_Lat2";
  private final static String SITE_LON2="Site_Lon2";
  private final static String SITE_ELEVATION2="Site_Elevation2";
  private final static String REPRESENTATIVE_STRAND_INDEX="Representative_Strand_Index";
  private final static String GENERAL_COMMENTS="General_Comments";
  private final static String OLD_SITE_ID="Old_Site_Id";
  private final static String REFERENCE_ID="Reference_Id";

  private DB_AccessAPI dbAccess;
  // site type DAO
  private SiteTypeDB_DAO siteTypeDAO ;
  // references DAO
  private ReferenceDB_DAO referenceDAO ;
  // site representations DAO
  private SiteRepresentationDB_DAO siteRepresentationDAO;
  // fault DAO
  private FaultDB_DAO faultDAO;



  public PaleoSiteDB_DAO(DB_AccessAPI dbAccess) {
    setDB_Connection(dbAccess);
  }

  public void setDB_Connection(DB_AccessAPI dbAccess) {
   this.dbAccess = dbAccess;
   if(siteTypeDAO==null) {
     siteTypeDAO = new SiteTypeDB_DAO(DB_AccessAPI.dbConnection);
     referenceDAO = new ReferenceDB_DAO(DB_AccessAPI.dbConnection);
     siteRepresentationDAO = new SiteRepresentationDB_DAO(DB_AccessAPI.dbConnection);
     faultDAO = new FaultDB_DAO(DB_AccessAPI.dbConnection);
   } else { // set the DB connection
     siteTypeDAO.setDB_Connection(dbAccess);
     referenceDAO.setDB_Connection(dbAccess);
     siteRepresentationDAO.setDB_Connection(dbAccess);
     faultDAO.setDB_Connection(dbAccess);
   }
 }

 /**
  * Add a new paleo site
  *
  * @param paleoSite
  * @throws InsertException
  */
  public void addPaleoSite(PaleoSite paleoSite) throws InsertException {
    int paleoSiteId = paleoSite.getSiteId();
    Date systemDate;
    try {
      if(paleoSiteId<=0)
        paleoSiteId = dbAccess.getNextSequenceNumber(SEQUENCE_NAME);
      systemDate = dbAccess.getSystemDate();
    }catch(SQLException e) {
      throw new InsertException(e.getMessage());
    }

    int faultId = faultDAO.getFault(paleoSite.getFaultName()).getFaultId();
    int siteTypeId = siteTypeDAO.getSiteType(paleoSite.getSiteTypeName()).getSiteTypeId();
    int siteRepresentationId = siteRepresentationDAO.getSiteRepresentation(paleoSite.getRepresentativeStrandName()).getSiteRepresentationId();
    String sql = "insert into "+TABLE_NAME+"("+ SITE_ID+","+FAULT_ID+","+
        ENTRY_DATE+","+ENTRY_COMMENTS+
        ","+CONTRIBUTOR_ID+","+SITE_TYPE_ID+","+SITE_NAME+","+SITE_LAT1+","+
        SITE_LON1+","+SITE_ELEVATION1+","+SITE_LAT2+","+SITE_LON2+","+
        SITE_ELEVATION2+","+REPRESENTATIVE_STRAND_INDEX+","+
        GENERAL_COMMENTS+","+OLD_SITE_ID+") "+
        " values ("+paleoSiteId+","+faultId+",'"+systemDate+
        "','"+paleoSite.getEntryComments()+"',"+paleoSite.getSiteContributor().getId()+","+
        siteTypeId+",'"+paleoSite.getSiteName()+"',"+
        paleoSite.getSiteLat1()+","+paleoSite.getSiteLon1()+","+
        paleoSite.getSiteElevation1()+","+paleoSite.getSiteLat2()+","+
        paleoSite.getSiteLon2()+","+
        paleoSite.getSiteElevation2()+","+siteRepresentationId+
        ",'"+paleoSite.getGeneralComments()+"','"+paleoSite.getOldSiteId()+"')";

    try {
      dbAccess.insertUpdateOrDeleteData(sql);
      ArrayList shortCitationList = paleoSite.getReferenceShortCitationList();
      for(int i=0; i<shortCitationList.size(); ++i) {
        int referenceId = referenceDAO.getReference((String)shortCitationList.get(i)).getReferenceId();
        sql = "insert into "+this.REFERNCES_TABLE_NAME+"("+SITE_ID+","+
            CONTRIBUTOR_ID+","+ENTRY_DATE+","+REFERENCE_ID+") "+
            "values ("+paleoSiteId+","+paleoSite.getSiteContributor().getId()+",'"+
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
   * Get paleo site data based on paleoSiteId
   * @param paleoSiteId
   * @return
   * @throws QueryException
   */
  public ArrayList getPaleoSite(int paleoSiteId) throws QueryException {
    String condition = " where "+SITE_ID+"="+paleoSiteId;
    return query(condition);
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
    String sql =  "select "+SITE_ID+","+FAULT_ID+","+ENTRY_DATE+","+ENTRY_COMMENTS+
        ","+CONTRIBUTOR_ID+","+SITE_TYPE_ID+","+SITE_NAME+","+SITE_LAT1+","+
        SITE_LON1+","+SITE_ELEVATION1+","+SITE_LAT2+","+SITE_LON2+","+
        SITE_ELEVATION2+","+REPRESENTATIVE_STRAND_INDEX+","+
        GENERAL_COMMENTS+","+OLD_SITE_ID+
        " from "+TABLE_NAME+condition;
    try {
      ResultSet rs  = dbAccess.queryData(sql);
      ContributorDB_DAO contributorDAO = new ContributorDB_DAO(dbAccess);
      SiteTypeDB_DAO siteTypeDAO = new SiteTypeDB_DAO(dbAccess);
      while(rs.next())  {
        PaleoSite paleoSite = new PaleoSite();
        paleoSite.setSiteId(rs.getInt(SITE_ID));
        paleoSite.setFaultName(faultDAO.getFault(rs.getInt(FAULT_ID)).getFaultName());
        paleoSite.setEntryDate(rs.getDate(ENTRY_DATE));
        paleoSite.setEntryComments(rs.getString(ENTRY_COMMENTS));
        paleoSite.setSiteContributor(contributorDAO.getContributor(rs.getInt(CONTRIBUTOR_ID)));
        paleoSite.setSiteTypeName(siteTypeDAO.getSiteType(rs.getInt(SITE_TYPE_ID)).getSiteType());
        paleoSite.setSiteName(rs.getString(SITE_NAME));
        paleoSite.setSiteLat1(rs.getFloat(SITE_LAT1));
        paleoSite.setSiteLon1(rs.getFloat(SITE_LON1));
        paleoSite.setSiteElevation1(rs.getFloat(SITE_ELEVATION1));
        paleoSite.setSiteLat2(rs.getFloat(SITE_LAT2));
        paleoSite.setSiteLon2(rs.getFloat(SITE_LON2));
        paleoSite.setSiteElevation2(rs.getFloat(SITE_ELEVATION2));
        paleoSite.setRepresentativeStrandName(siteRepresentationDAO.getSiteRepresentation(rs.getInt(REPRESENTATIVE_STRAND_INDEX)).getSiteRepresentationName());
        paleoSite.setGeneralComments(rs.getString(GENERAL_COMMENTS));
        paleoSite.setOldSiteId(rs.getString(OLD_SITE_ID));
        // get all the refernces for this site
        ArrayList referenceList = new ArrayList();
        sql = "select "+REFERENCE_ID+" from "+this.REFERNCES_TABLE_NAME+
            " where "+SITE_ID+"="+paleoSite.getSiteId()+" and "+
            CONTRIBUTOR_ID+"="+rs.getInt(CONTRIBUTOR_ID)+" and "+
            ENTRY_DATE+"='"+rs.getDate(ENTRY_DATE)+"'";
        ResultSet referenceResultSet = dbAccess.queryData(sql);
        while(referenceResultSet.next()) {
          referenceList.add(referenceDAO.getReference(referenceResultSet.getInt(REFERENCE_ID)).getShortCitation());
        }
        referenceResultSet.close();
        // set the references in the VO
        paleoSite.setReferenceShortCitationList(referenceList);
        paleoSiteList.add(paleoSite);
      }
      rs.close();
    } catch(SQLException e) { throw new QueryException(e.getMessage()); }
    return paleoSiteList;
  }

}
