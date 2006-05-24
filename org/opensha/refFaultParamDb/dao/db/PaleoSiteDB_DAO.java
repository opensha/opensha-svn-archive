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
import org.opensha.refFaultParamDb.vo.PaleoSitePublication;
import org.opensha.refFaultParamDb.vo.FaultSectionSummary;

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
  private final static String SITE_ID="Site_Id";
  private final static String FAULT_SECTION_ID="Fault_Section_Id";
  private final static String ENTRY_DATE="Entry_Date";
  private final static String SITE_NAME="Site_Name";
  private final static String SITE_LOCATION1="Site_Location1";
  private final static String SITE_LOCATION2="Site_Location2";
  private final static String GENERAL_COMMENTS="General_Comments";
  private final static String OLD_SITE_ID="Old_Site_Id";
  private final static String DIP_EST_ID = "Dip_Est_Id";
  
  private final static int SRID=8307;

  private DB_AccessAPI dbAccess;
  // estimate instance DAO
  private EstimateInstancesDB_DAO estimateInstancesDAO;
  // paleo site publication DAO
  private PaleoSitePublicationsDB_DAO paleoSitePublicationDAO;
  // fault DAO
  private FaultSectionVer2_DB_DAO faultSectionDAO;



  public PaleoSiteDB_DAO(DB_AccessAPI dbAccess) {
    setDB_Connection(dbAccess);
  }

  public void setDB_Connection(DB_AccessAPI dbAccess) {
   this.dbAccess = dbAccess;
   faultSectionDAO = new FaultSectionVer2_DB_DAO(dbAccess);
   estimateInstancesDAO = new EstimateInstancesDB_DAO(dbAccess);
   paleoSitePublicationDAO = new PaleoSitePublicationsDB_DAO(dbAccess);
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
        paleoSite.setSiteId(paleoSiteId);
        paleoSite.setEntryDate(systemDate);
    }catch(SQLException e) {
      throw new InsertException(e.getMessage());
    }

    JGeometry location1;
    // if elevation is available
    if(!Float.isNaN(paleoSite.getSiteElevation1()))
      location1 = new JGeometry(paleoSite.getSiteLon1(),
                                paleoSite.getSiteLat1(),
                                paleoSite.getSiteElevation1(),
                                SRID);
      // if elevation not available
    else location1 = new JGeometry(paleoSite.getSiteLon1(),
                                   paleoSite.getSiteLat1(),
                                   SRID);


    
    ArrayList geomteryObjectList = new ArrayList();
    geomteryObjectList.add(location1);
    
    String colNames="", colVals="";
    
    // check whether dip estimate exists
    EstimateInstances dipEst = paleoSite.getDipEstimate();
    if(dipEst!=null) {
      colNames = DIP_EST_ID+",";
      int id = this.estimateInstancesDAO.addEstimateInstance(dipEst);
      colVals=""+id+",";
    }
    
    //check whether second location exists or not
    JGeometry location2;
    if(!Float.isNaN(paleoSite.getSiteLat1())) {
    	// if elevation is available
    	if(!Float.isNaN(paleoSite.getSiteElevation2()))
    		location2 = new JGeometry(paleoSite.getSiteLon2(),
                                paleoSite.getSiteLat2(),
                                paleoSite.getSiteElevation2(),
                                SRID);
    	// if elevation is not available
    	else location2 = new JGeometry(paleoSite.getSiteLon2(),
                                paleoSite.getSiteLat2(),
                                SRID);
    	geomteryObjectList.add(location2);
    	colNames+=SITE_LOCATION2+",";
    	colVals+="?,";
    }
    
    String sql = "insert into "+TABLE_NAME+"("+ SITE_ID+","+FAULT_SECTION_ID+","+
        ENTRY_DATE+","+SITE_NAME+","+SITE_LOCATION1+","+colNames+
        GENERAL_COMMENTS+","+OLD_SITE_ID+") "+
        " values ("+paleoSiteId+","+paleoSite.getFaultSectionId()+",'"+systemDate+
        "','"+paleoSite.getSiteName()+"',?,"+colVals+"'"+paleoSite.getGeneralComments()+"','"+
        paleoSite.getOldSiteId()+"')";
    try {	
      //System.out.println(sql);
      dbAccess.insertUpdateOrDeleteData(sql, geomteryObjectList);
      // put the reference, site type and representative strand index
      ArrayList paleoSitePubList = paleoSite.getPaleoSitePubList();
      for(int i=0; i<paleoSitePubList.size(); ++i ) {
        // set the site entry date and site id
        PaleoSitePublication paleoSitePub = (PaleoSitePublication)paleoSitePubList.get(i);
        paleoSitePub.setSiteId(paleoSiteId);
        paleoSitePub.setSiteEntryDate(systemDate);
        this.paleoSitePublicationDAO.addPaleoSitePublicationInfo(paleoSitePub);
      }
    }
    catch(SQLException e) {
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
   * Get paleo site data based on qfaultSiteId
   * @param qFaultSiteId
   * @return
   * @throws QueryException
   */
  public PaleoSite getPaleoSiteByQfaultId(String qFaultSiteId) throws QueryException {
    String condition = " where "+OLD_SITE_ID+"='"+qFaultSiteId+"'";
    ArrayList paleoSiteList = query(condition);
    PaleoSite paleoSite = null;
    if(paleoSiteList.size()>0) paleoSite = (PaleoSite)paleoSiteList.get(0);
    return paleoSite;
  }
  
  /**
  * Get paleo site data based on paleoSiteName
  * @param paleoSiteName
  * @return
  * @throws QueryException
  */
 public PaleoSite getPaleoSite(String paleoSiteName) throws QueryException {
   String condition = " where "+SITE_NAME+"='"+paleoSiteName+"'";
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
   String sql =  "select "+SITE_ID+","+SITE_NAME+" from "+TABLE_NAME+" order by "+SITE_NAME;
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
  * Get a list of PaleoSites which just have Id, Name and Locations
  * @return
  * @throws QueryException
  */
 public ArrayList getPaleoSiteNameIdAndLocations() throws QueryException {
	 ArrayList paleoSiteList = new ArrayList();
	    String sqlWithSpatialColumnNames =  "select "+SITE_ID+","+SITE_NAME+","+SITE_LOCATION1+","+
	        SITE_LOCATION2+" from "+TABLE_NAME;
	    String sqlWithNoSpatialColumnNames =  "select "+SITE_ID+","+SITE_NAME+" from "+TABLE_NAME;

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
	        paleoSite.setSiteName(rs.getString(SITE_NAME));
	        // location 1
	        ArrayList geometries = spatialQueryResult.getGeometryObjectsList(i++);
	        setPaleoSiteLocations(paleoSite, geometries);
	        paleoSiteList.add(paleoSite);
	      }
	      rs.close();
	    } catch(SQLException e) { throw new QueryException(e.getMessage()); }
	    return paleoSiteList;
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
    String sqlWithSpatialColumnNames =  "select "+SITE_ID+","+FAULT_SECTION_ID+",to_char("+ENTRY_DATE+") as "+ENTRY_DATE+
        ","+SITE_NAME+","+SITE_LOCATION1+","+
        SITE_LOCATION2+","+
        DIP_EST_ID+","+GENERAL_COMMENTS+","+OLD_SITE_ID+
        " from "+TABLE_NAME+condition;
    String sqlWithNoSpatialColumnNames =  "select "+SITE_ID+","+FAULT_SECTION_ID+",to_char("+ENTRY_DATE+") as "+ENTRY_DATE+
    ","+SITE_NAME+","+
    DIP_EST_ID+","+GENERAL_COMMENTS+","+OLD_SITE_ID+
    " from "+TABLE_NAME+condition;

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
        FaultSectionSummary faultSectionSummary =  faultSectionDAO.getFaultSectionSummary(rs.getInt(FAULT_SECTION_ID));
        paleoSite.setFaultSectionNameId(faultSectionSummary.getSectionName(), faultSectionSummary.getSectionId());

        paleoSite.setSiteName(rs.getString(SITE_NAME));
        // location 1
        ArrayList geometries = spatialQueryResult.getGeometryObjectsList(i++);
        setPaleoSiteLocations(paleoSite, geometries);
        int dipEstId = rs.getInt(DIP_EST_ID);
        if(!rs.wasNull()) paleoSite.setDipEstimate(this.estimateInstancesDAO.getEstimateInstance(dipEstId));
        
        paleoSite.setGeneralComments(rs.getString(GENERAL_COMMENTS));
        paleoSite.setOldSiteId(rs.getString(OLD_SITE_ID));
        paleoSite.setPaleoSitePubList(this.paleoSitePublicationDAO.getPaleoSitePublicationInfo(rs.getInt(SITE_ID)));
        paleoSiteList.add(paleoSite);
      }
      rs.close();
    } catch(SQLException e) { throw new QueryException(e.getMessage()); }
    return paleoSiteList;
  }

  /**
   * set the locations in paleo site
   * @param paleoSite
   * @param geometries
   */
private void setPaleoSiteLocations(PaleoSite paleoSite, ArrayList geometries) {
	JGeometry location1 =(JGeometry) geometries.get(0);
	double []point1 = location1.getPoint();
	//location 2
	JGeometry location2 = (JGeometry) geometries.get(1);
	

	paleoSite.setSiteLat1((float)point1[1]);
	paleoSite.setSiteLon1((float)point1[0]);
	// if elevation available, set it else set it as NaN
	if(point1.length>2)
	  paleoSite.setSiteElevation1((float)point1[2]);
	else paleoSite.setSiteElevation1(Float.NaN);
	
	// check whether second locations exists or not
	if(location2!=null) {
		double []point2 = location2.getPoint();
		paleoSite.setSiteLat2((float)point2[1]);
		paleoSite.setSiteLon2((float)point2[0]);
		// if elevation available, set it else set it as NaN
		if(point2.length>2)
			paleoSite.setSiteElevation2((float)point2[2]);
		else paleoSite.setSiteElevation2(Float.NaN);
	}
}

}
