package org.opensha.refFaultParamDb.dao.db;

import java.util.ArrayList;
import org.opensha.refFaultParamDb.dao.exception.QueryException;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.opensha.refFaultParamDb.vo.FaultSection2002;
import oracle.spatial.geometry.JGeometry;
import org.opensha.sha.fault.FaultTrace;
import org.opensha.data.Location;
import java.awt.geom.Point2D;

/**
 * <p>Title: FaultSection2002DB_DAO.java </p>
 * <p>Description: This class connects with Fault Section database at Golden and
 *  gets info about fault sections in 2002</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class FaultSection2002DB_DAO {
  private final static String TABLE_NAME = "Fault_Section_Ght_ca";
  private final static String FAULT_ID = "Fault_Id";
  private final static String SECTION_NAME = "Section_Name";
  private final static String ENTRY_DATE = "Entry_Date";
  private final static String FAULT_MODEL = "Fault_Model";
  private final static String COMMENTS = "Comments";
  private final static String AVE_LT_SLIP_RATE_EST = "Ave_Lt_Slip_Rate_Est";
  private final static String AVE_DIP_EST = "Ave_Dip_Est";
  private final static String AVE_UPPER_SD_EST = "Ave_Upper_Sd_Est";
  private final static String AVE_LOWER_SD_EST = "Ave_Lower_Sd_Est";
  private final static String FAULT_GEOM = "Fault_Geom";
  private final static String SECTION_ID = "Section_Id";
  private final static String NSHM02_ID = "NSHM02_ID";

  private DB_AccessAPI dbAccessAPI;

  /**
   * Constructor.
   * @param dbConnection
   */
  public FaultSection2002DB_DAO(DB_AccessAPI dbAccessAPI) {
    setDB_Connection(dbAccessAPI);
  }

  public void setDB_Connection(DB_AccessAPI dbAccessAPI) {
    this.dbAccessAPI = dbAccessAPI;
  }

  /**
   * Get all the fault sections from the 2002 database
   * @return
   */
  public ArrayList getAllFaultSections() {
    String condition = " ";
    return query(condition);
  }

  /**
   * Query the fault section table based on some condition
   *
   * @param condition
   * @return
   */
   private ArrayList query(String condition) {
     ArrayList faultSectionList = new ArrayList();
     String sqlWithSpatialColumnName =  "select "+FAULT_ID+","+SECTION_NAME+",to_char("+ENTRY_DATE+") as "+ENTRY_DATE+","+
         FAULT_MODEL+","+COMMENTS+","+AVE_LT_SLIP_RATE_EST+","+
         FAULT_GEOM+","+
         AVE_DIP_EST+","+AVE_UPPER_SD_EST+","+AVE_LOWER_SD_EST+","
         +SECTION_ID+","+NSHM02_ID+" from "+
         this.TABLE_NAME+condition;

     String sqlWithNoSpatialColumnName =  "select "+FAULT_ID+","+SECTION_NAME+",to_char("+ENTRY_DATE+") as "+ENTRY_DATE+","+
       FAULT_MODEL+","+COMMENTS+","+AVE_LT_SLIP_RATE_EST+","+
       AVE_DIP_EST+","+AVE_UPPER_SD_EST+","+AVE_LOWER_SD_EST+","
       +SECTION_ID+","+NSHM02_ID+" from "+
       this.TABLE_NAME+condition;
   try {
     ArrayList spatialColumnList = new ArrayList();
     spatialColumnList.add(FAULT_GEOM);
     SpatialQueryResult spatialQueryResult = dbAccessAPI.queryData(sqlWithSpatialColumnName, sqlWithNoSpatialColumnName, spatialColumnList);
     ResultSet rs = spatialQueryResult.getCachedRowSet();
     //ResultSet rs = dbAccessAPI.queryData(sql);
     int i=0;
     while(rs.next())  {
       FaultSection2002 faultSection = new FaultSection2002();
       faultSection.setFaultId(rs.getString(FAULT_ID));
       faultSection.setSectionName(rs.getString(SECTION_NAME));
       faultSection.setEntryDate(rs.getString(ENTRY_DATE));
       faultSection.setFaultModel(rs.getString(FAULT_MODEL));
       faultSection.setComments(rs.getString(COMMENTS));
       faultSection.setAveLongTermSlipRate(rs.getFloat(AVE_LT_SLIP_RATE_EST));
       faultSection.setAveDip(rs.getFloat(AVE_DIP_EST));
       faultSection.setAveUpperSeisDepth(rs.getFloat(AVE_UPPER_SD_EST));
       faultSection.setAveLowerSeisDepth(rs.getFloat(AVE_LOWER_SD_EST));
       faultSection.setSectionId(rs.getString(SECTION_ID));
       faultSection.setNshm02Id(rs.getString(NSHM02_ID));
       FaultTrace faultTrace = new FaultTrace(rs.getString(SECTION_NAME));
       JGeometry faultTraceGeom  = (JGeometry)spatialQueryResult.getGeometryObjectsList(i++).get(0);
       int numPoints = faultTraceGeom.getNumPoints();
       double[] ordinatesArray = faultTraceGeom.getOrdinatesArray();
       for(int j=0; j<numPoints; ++j) {
        faultTrace.addLocation(new Location(ordinatesArray[2*j+1], ordinatesArray[2*j]));
       }
       faultSection.setFaultTrace(faultTrace);
       faultSectionList.add(faultSection);
     }
   }catch(SQLException e) {
      e.printStackTrace();
      throw new QueryException(e.getMessage());
    }
    return faultSectionList;
   }
}