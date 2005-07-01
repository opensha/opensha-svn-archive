package javaDevelopers.vipin.dao.db;

import java.sql.SQLException;
import java.sql.ResultSet;
import javaDevelopers.vipin.dao.PaleoSiteDAO_API;
import javaDevelopers.vipin.vo.PaleoSite;
import javaDevelopers.vipin.vo.Contributor;
import javaDevelopers.vipin.vo.SiteType;
import javaDevelopers.vipin.dao.exception.*;
import java.util.ArrayList;

/**
 * <p>Title: PaleoSiteDB_DAO.java </p>
 * <p>Description: Performs insert/delete/update on PaleoSite table on oracle database</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */


public class PaleoSiteDB_DAO implements PaleoSiteDAO_API {

  private final static String TABLE_NAME="Paleo_Site";

  private final static String SITE_ID="Site_Id";
  private final static String EFFECTIVE_DATE="Effective_Date";
  private final static String CONTRIBUTOR_ID="Contributor_Id";
  private final static String SITE_TYPE_ID="Site_Type_Id";
  private final static String SITE_NAME="Site_Name";
  private final static String SITE_LAT="Site_Lat";
  private final static String SITE_LON="Site_Lon";
  private final static String SITE_ELEVATION="Site_Elevation";
  private final static String REPRESENTATIVE_STRAND_INDEX="Representative_Strand_Index";
  private final static String COMMENTS="Comments";
  private final static String OLD_SITE_ID="Old_Site_Id";

  private DB_Connection dbConnection;


  public PaleoSiteDB_DAO(DB_Connection dbConnection) {
    this.dbConnection = dbConnection;
  }

  public void setDB_Connection(DB_Connection connection) {
   this.dbConnection = connection;
 }

 /**
  * Add a new paleo site
  *
  * @param paleoSite
  * @throws InsertException
  */
  public void addPaleoSite(PaleoSite paleoSite) throws InsertException {

    String sql = "insert into "+TABLE_NAME+"("+ SITE_ID+","+EFFECTIVE_DATE+
        ","+CONTRIBUTOR_ID+","+SITE_TYPE_ID+","+SITE_NAME+","+SITE_LAT+","+
        SITE_LON+","+SITE_ELEVATION+","+REPRESENTATIVE_STRAND_INDEX+","+
        COMMENTS+","+OLD_SITE_ID+") "+
        " values ("+paleoSite.getSiteId()+",sysdate"+
        ","+paleoSite.getSiteContributor().getId()+","+
        paleoSite.getSiteType().getSiteTypeId()+",'"+paleoSite.getSiteName()+"',"+
        paleoSite.getSiteLat()+","+paleoSite.getSiteLon()+","+
        paleoSite.getSiteElevation()+","+paleoSite.getRepresentativeStrandIndex()+
        ",'"+paleoSite.getComments()+"',"+paleoSite.getOldSiteId()+")";

    try { dbConnection.insertUpdateOrDeleteData(sql); }
    catch(SQLException e) {
      //e.printStackTrace();
      throw new InsertException(e.getMessage());
    }
  }


  /**
   * Update a paleo site
   *
   * @param paleoSiteId
   * @param paleoSite
   * @return
   * @throws UpdateException
   */
  public boolean updatePaleoSite(int paleoSiteId, PaleoSite paleoSite) throws UpdateException {
    String sql = "update "+TABLE_NAME+" set "+EFFECTIVE_DATE+"='"+
        paleoSite.getEffectiveDate()+"',"+CONTRIBUTOR_ID+"="+
        paleoSite.getSiteContributor().getId()+","+SITE_TYPE_ID+"="+
        paleoSite.getSiteType().getSiteTypeId()+","+SITE_NAME+"='"+
        paleoSite.getSiteName()+"',"+SITE_LAT+"="+paleoSite.getSiteLat()+","+
        SITE_LON+"="+paleoSite.getSiteLon()+","+SITE_ELEVATION+"="+
        paleoSite.getSiteElevation()+","+REPRESENTATIVE_STRAND_INDEX+"="+
        paleoSite.getRepresentativeStrandIndex()+","+COMMENTS+"='"+
        paleoSite.getComments()+"',"+OLD_SITE_ID+"="+paleoSite.getOldSiteId()+
       " where "+SITE_ID+"="+paleoSiteId;
    try {
      int numRows = dbConnection.insertUpdateOrDeleteData(sql);
      if(numRows==1) return true;
    }
    catch(SQLException e) { throw new UpdateException(e.getMessage()); }
    return false;

  }

  /**
   * Get a paleo site based on paleoSiteId
   * @param paleoSiteId
   * @return
   * @throws QueryException
   */
  public PaleoSite getPaleoSite(int paleoSiteId) throws QueryException {
    PaleoSite paleoSite=null;
    String condition = " where "+SITE_ID+"="+paleoSiteId;
    ArrayList paleoSiteList=query(condition);
    if(paleoSiteList.size()>0) paleoSite = (PaleoSite)paleoSiteList.get(0);
    return paleoSite;

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
      int numRows = dbConnection.insertUpdateOrDeleteData(sql);
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
    String sql =  "select "+SITE_ID+","+EFFECTIVE_DATE+
        ","+CONTRIBUTOR_ID+","+SITE_TYPE_ID+","+SITE_NAME+","+SITE_LAT+","+
        SITE_LON+","+SITE_ELEVATION+","+REPRESENTATIVE_STRAND_INDEX+","+
        COMMENTS+","+OLD_SITE_ID+
        " from "+TABLE_NAME+condition;
    try {
      ResultSet rs  = dbConnection.queryData(sql);
      ContributorDB_DAO contributorDAO = new ContributorDB_DAO(dbConnection);
      SiteTypeDB_DAO siteTypeDAO = new SiteTypeDB_DAO(dbConnection);
      while(rs.next())  {
        PaleoSite paleoSite = new PaleoSite();
        paleoSite.setSiteId(rs.getInt(SITE_ID));
        paleoSite.setEffectiveDate(rs.getDate(EFFECTIVE_DATE));
        paleoSite.setContributor(contributorDAO.getContributor(rs.getInt(CONTRIBUTOR_ID)));
        paleoSite.setSiteType(siteTypeDAO.getSiteType(rs.getInt(SITE_TYPE_ID)));
        paleoSite.setSiteName(rs.getString(SITE_NAME));
        paleoSite.setSiteLat(rs.getFloat(SITE_LAT));
        paleoSite.setSiteLon(rs.getFloat(SITE_LON));
        paleoSite.setSiteElevation(rs.getFloat(SITE_ELEVATION));
        paleoSite.setRepresentativeStrandIndex(rs.getInt(REPRESENTATIVE_STRAND_INDEX));
        paleoSite.setComments(rs.getString(COMMENTS));
        paleoSite.setOldSiteId(rs.getInt(OLD_SITE_ID));
        paleoSiteList.add(paleoSite);
      }
      rs.close();
      rs.getStatement().close();
    } catch(SQLException e) { throw new QueryException(e.getMessage()); }
    return paleoSiteList;
  }

}
