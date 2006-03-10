package org.opensha.refFaultParamDb.dao.db;

import org.opensha.refFaultParamDb.vo.FaultSectionVer2;
import java.sql.SQLException;
import org.opensha.refFaultParamDb.dao.exception.InsertException;
import oracle.spatial.geometry.JGeometry;
import org.opensha.sha.fault.FaultTrace;
import org.opensha.data.Location;
import java.util.ArrayList;
import org.opensha.refFaultParamDb.gui.infotools.SessionInfo;

/**
 * <p>Title: FaultSectionVer2_DB_DAO.java </p>
 * <p>Description: This class interacts with Fault Section table in CA Ref Fault Param
 * Database.</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class FaultSectionVer2_DB_DAO {
  private final static String TABLE_NAME = "Fault_Section";
  private final static String SEQUENCE_NAME = "Fault_Section_Sequence";
  private final static String SECTION_ID = "Section_Id";
  private final static String FAULT_ID = "Fault_Id";
  private final static String AVE_LONG_TERM_SLIP_RATE_EST = "Ave_Long_Term_Slip_Rate_Est";
  private final static String AVE_DIP_EST = "Ave_Dip_Est";
  private final static String AVE_RAKE_EST = "Ave_Rake_Est";
  private final static String AVE_UPPER_DEPTH_EST = "Ave_Upper_Depth_Est";
  private final static String AVE_LOWER_DEPTH_EST = "Ave_Lower_Depth_Est";
  private final static String CONTRIBUTOR_ID =  "Contributor_Id";
  private final static String SECTION_NAME = "Name";
  private final static String ENTRY_DATE = "Entry_Date";
  private final static String COMMENTS   = "Comments";
  private final static String FAULT_TRACE = "Fault_Trace";
  private final static String ASEISMIC_SLIP_FACTOR_EST = "Aseismic_Slip_Factor_Est";
  private final static String DIP_DIRECTION = "Dip_Direction";
  private DB_AccessAPI dbAccess;
   // estimate instance DAO
   private EstimateInstancesDB_DAO estimateInstancesDAO;
   // fault DAO
   private FaultDB_DAO faultDAO;
   // SRID
   private final static int SRID=8307;

  public FaultSectionVer2_DB_DAO(DB_AccessAPI dbAccess) {
    setDB_Connection(dbAccess);
  }

  /**
   * Set the database connection
   * @param dbAccess
   */
  public void setDB_Connection(DB_AccessAPI dbAccess) {
    this.dbAccess = dbAccess;
    estimateInstancesDAO = new EstimateInstancesDB_DAO(dbAccess);
    faultDAO = new FaultDB_DAO(dbAccess);
  }

  /**
   * Add a new fault section to the database
   * @param faultSection
   * @return
   */
  public int addFaultSection(FaultSectionVer2 faultSection) {

    int faultSectionId = faultSection.getSectionId();
    String systemDate;
    try {
      // generate fault section Id
      if(faultSectionId<=0) faultSectionId = dbAccess.getNextSequenceNumber(SEQUENCE_NAME);
      systemDate = dbAccess.getSystemDate();
    }catch(SQLException e) {
      throw new InsertException(e.getMessage());
    }
    // fault Id for this section
    int faultId = faultDAO.getFault(faultSection.getFaultName()).getFaultId();

    // get JGeomtery object from fault trace
    JGeometry faultSectionTraceGeom =  getGeomtery(faultSection.getFaultTrace());

    // various estimate ids
    int aveLongTermSlipRateEstId = this.estimateInstancesDAO.addEstimateInstance(faultSection.getAveLongTermSlipRateEst());
    int aveDipEst = this.estimateInstancesDAO.addEstimateInstance(faultSection.getAveDipEst());
    int aveRakeEst =  this.estimateInstancesDAO.addEstimateInstance(faultSection.getAveRakeEst());
    int aveUpperDepthEst = this.estimateInstancesDAO.addEstimateInstance(faultSection.getAveUpperDepthEst());
    int aveLowerDepthEst = this.estimateInstancesDAO.addEstimateInstance(faultSection.getAveLowerDepthEst());
    int aseismicSlipFactorEst = this.estimateInstancesDAO.addEstimateInstance(faultSection.getAseismicSlipFactorEst());

    // insert the fault section into the database
    ArrayList geomteryObjectList = new ArrayList();
    geomteryObjectList.add(faultSectionTraceGeom);
    String sql = "insert into "+TABLE_NAME+"("+ SECTION_ID+","+FAULT_ID+","+
        AVE_LONG_TERM_SLIP_RATE_EST+","+AVE_DIP_EST+","+
        AVE_RAKE_EST+","+AVE_UPPER_DEPTH_EST+","+AVE_LOWER_DEPTH_EST+","+
        CONTRIBUTOR_ID+","+SECTION_NAME+","+ENTRY_DATE+","+COMMENTS+","+
        FAULT_TRACE+","+ASEISMIC_SLIP_FACTOR_EST+","+DIP_DIRECTION+") values ("+
        faultSectionId+","+faultId+","+aveLongTermSlipRateEstId+","+
        aveDipEst+","+aveRakeEst+","+aveUpperDepthEst+","+aveLowerDepthEst+","+
        SessionInfo.getContributor().getId()+",'"+faultSection.getSectionName()+"','"+
        systemDate+"','"+faultSection.getComments()+"',?,"+
        aseismicSlipFactorEst+","+faultSection.getDipDirection()+")";
    try {
      dbAccess.insertUpdateOrDeleteData(sql, geomteryObjectList);
      return faultSectionId;
    }
    catch(SQLException e) {
      throw new InsertException(e.getMessage());
    }
  }

  /**
   * Create JGeomtery object from FaultTrace
   * @param faultTrace
   * @return
   */
  private JGeometry getGeomtery(FaultTrace faultTrace) {
    int numLocations = faultTrace.getNumLocations();
    Object[] coords = new Object[numLocations];
    for(int j=0; j<numLocations; ++j) {
      Location loc= faultTrace.getLocationAt(j);
      double d[] = { loc.getLongitude(), loc.getLatitude()} ;
      coords[j] = d;
    }
    return JGeometry.createMultiPoint(coords, 2, SRID);
  }

}