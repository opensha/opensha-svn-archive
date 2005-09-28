package org.opensha.refFaultParamDb.dao.db;

import org.opensha.refFaultParamDb.vo.SiteRepresentation;
import java.util.ArrayList;
import java.sql.SQLException;
import org.opensha.refFaultParamDb.dao.exception.*;
import java.sql.ResultSet;

/**
 * <p>Title: SiteRepresentationDB_DAO.java </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class SiteRepresentationDB_DAO  {

  private final static String TABLE_NAME="Site_Representations";
  private final static String SITE_REPRESENTATION_ID="Site_Representation_Id";
  public final static String SITE_REPRESENTATION_NAME="Site_Representation_Name";
  private DB_AccessAPI dbAccessAPI;


  public SiteRepresentationDB_DAO(DB_AccessAPI dbAccessAPI) {
   setDB_Connection(dbAccessAPI);
  }

  public void setDB_Connection(DB_AccessAPI dbAccessAPI) {
    this.dbAccessAPI = dbAccessAPI;
  }

  /**
  * Get all the representations with which a site can be associated
  * @return
  */
  public ArrayList getAllSiteRepresentations() {
    return query(" ");
  }

  /**
   * Get a representation based on site representation Id
   * @param siteRepresentationId
   * @return
   */
  public SiteRepresentation getSiteRepresentation(int siteRepresentationId) {
    SiteRepresentation siteRepresentation=null;
    String condition = " where "+this.SITE_REPRESENTATION_ID+"="+siteRepresentationId;
    ArrayList siteRepresentationList=query(condition);
    if(siteRepresentationList.size()>0) siteRepresentation = (SiteRepresentation)siteRepresentationList.get(0);
    return siteRepresentation;
  }

  /**
   * Get a  representation based on site representation name
   *
   * @param siteRepresentationName
   * @return
   */
  public SiteRepresentation getSiteRepresentation(String siteRepresentationName) {
    SiteRepresentation siteRepresentation=null;
   String condition = " where "+this.SITE_REPRESENTATION_NAME+"='"+siteRepresentationName+"'";
   ArrayList siteRepresentationList=query(condition);
   if(siteRepresentationList.size()>0) siteRepresentation = (SiteRepresentation)siteRepresentationList.get(0);
   return siteRepresentation;
  }

  /**
   *
   * @param condition
   * @return
   * @throws QueryException
   */
  private ArrayList query(String condition) throws QueryException {
   ArrayList siteRepresentationList = new ArrayList();
   String sql =  "select "+SITE_REPRESENTATION_ID+","+SITE_REPRESENTATION_NAME+" from "+TABLE_NAME+condition;
   try {
     ResultSet rs  = dbAccessAPI.queryData(sql);
     while(rs.next()) siteRepresentationList.add(new SiteRepresentation(rs.getInt(SITE_REPRESENTATION_ID),
           rs.getString(SITE_REPRESENTATION_NAME)));
     rs.close();
   } catch(SQLException e) { throw new QueryException(e.getMessage()); }
   return siteRepresentationList;
 }


}