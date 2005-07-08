package javaDevelopers.vipin.dao.db;

import java.sql.SQLException;
import java.sql.ResultSet;
import javaDevelopers.vipin.dao.SiteTypeDAO_API;
import javaDevelopers.vipin.vo.SiteType;
import javaDevelopers.vipin.dao.exception.*;
import java.util.ArrayList;

/**
 * <p>Title: SiteTypeDB_DAO.java </p>
 * <p>Description: Performs insert/delete/update on siteType on oracle database</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */


public class SiteTypeDB_DAO implements SiteTypeDAO_API {
  private final static String SEQUENCE_NAME="Site_Type_Sequence";
  private final static String TABLE_NAME="Site_Type";
  private final static String SITE_TYPE_ID="Site_Type_Id";
  private final static String CONTRIBUTOR_ID="Contributor_Id";
  private final static String SITE_TYPE_NAME="Site_Type";
  private DB_Connection dbConnection;


  public SiteTypeDB_DAO(DB_Connection dbConnection) {
   setDB_Connection(dbConnection);
  }

  public void setDB_Connection(DB_Connection connection) {
   this.dbConnection = connection;
 }

 /**
  * Add a new site type
  *
  * @param siteType
  * @throws InsertException
  */
  public int addSiteType(SiteType siteType) throws InsertException {
    int siteTypeId = -1;
    try {
      siteTypeId = dbConnection.getNextSequenceNumber(SEQUENCE_NAME);
    }catch(SQLException e) {
      throw new InsertException(e.getMessage());
    }
    String sql = "insert into "+TABLE_NAME+"("+ SITE_TYPE_ID+","+CONTRIBUTOR_ID+
        ","+SITE_TYPE_NAME+") "+
        " values ("+siteTypeId+","+siteType.getContributor().getId()+
        ",'"+siteType.getSiteType()+"')";
    try { dbConnection.insertUpdateOrDeleteData(sql); }
    catch(SQLException e) {
      //e.printStackTrace();
      throw new InsertException(e.getMessage());
    }
    return siteTypeId;
  }


  /**
   * Update a site type
   *
   * @param siteTypeId
   * @param siteType
   * @return
   * @throws UpdateException
   */
  public boolean updateSiteType(int siteTypeId, SiteType siteType) throws UpdateException {
    String sql = "update "+TABLE_NAME+" set "+SITE_TYPE_NAME+"= '"+
        siteType.getSiteType()+"',"+CONTRIBUTOR_ID+"="+siteType.getContributor().getId()+
       " where "+SITE_TYPE_ID+"="+siteTypeId;
    try {
      int numRows = dbConnection.insertUpdateOrDeleteData(sql);
      if(numRows==1) return true;
    }
    catch(SQLException e) { throw new UpdateException(e.getMessage()); }
    return false;

  }

  /**
   * Get a site type based on site type ID
   * @param siteTypeId
   * @return
   * @throws QueryException
   */
  public SiteType getSiteType(int siteTypeId) throws QueryException {
    SiteType siteType=null;
    String condition = " where "+SITE_TYPE_ID+"="+siteTypeId;
    ArrayList siteTypeList=query(condition);
    if(siteTypeList.size()>0) siteType = (SiteType)siteTypeList.get(0);
    return siteType;

  }

  /**
   * remove a site type from the database
   * @param siteTypeId
   * @return
   * @throws UpdateException
   */
  public boolean removeSiteType(int siteTypeId) throws UpdateException {
    String sql = "delete from "+TABLE_NAME+"  where "+SITE_TYPE_ID+"="+siteTypeId;
    try {
      int numRows = dbConnection.insertUpdateOrDeleteData(sql);
      if(numRows==1) return true;
    }
    catch(SQLException e) { throw new UpdateException(e.getMessage()); }
    return false;
  }


  /**
   * Get all the site types from the database
   * @return
   * @throws QueryException
   */
  public ArrayList getAllSiteTypes() throws QueryException {
   return query(" ");
  }

  private ArrayList query(String condition) throws QueryException {
    ArrayList siteTypeList = new ArrayList();
    String sql =  "select "+SITE_TYPE_ID+","+SITE_TYPE_NAME+","+CONTRIBUTOR_ID+" from "+TABLE_NAME+condition;
    try {
      ResultSet rs  = dbConnection.queryData(sql);
      ContributorDB_DAO contributorDAO = new ContributorDB_DAO(dbConnection);
      while(rs.next()) siteTypeList.add(new SiteType(rs.getInt(SITE_TYPE_ID),
            rs.getString(SITE_TYPE_NAME),
            contributorDAO.getContributor(rs.getInt(CONTRIBUTOR_ID))));
      rs.close();
    } catch(SQLException e) { throw new QueryException(e.getMessage()); }
    return siteTypeList;
  }

}