package org.opensha.refFaultParamDb.dao.db;

import org.opensha.refFaultParamDb.vo.Fault;
import org.opensha.refFaultParamDb.vo.FaultSectionVer2;
import org.opensha.refFaultParamDb.vo.PaleoSite;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.opensha.refFaultParamDb.dao.exception.InsertException;
import org.opensha.refFaultParamDb.dao.exception.QueryException;

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
  private final static String FAULT_TRACE = "Fault_Section_Trace";
  private final static String ASEISMIC_SLIP_FACTOR_EST = "Average_Aseismic_Slip_Est";
  private final static String DIP_DIRECTION = "Dip_Direction";
  private final static String SECTION_SOURCE_ID = "Section_Source_Id";
  private DB_AccessAPI dbAccess;
   // estimate instance DAO
   private EstimateInstancesDB_DAO estimateInstancesDAO;
   // fault DAO
   private FaultDB_DAO faultDAO;
   //section source DAO
   private SectionSourceDB_DAO sectionSourceDAO;
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
    sectionSourceDAO = new SectionSourceDB_DAO(dbAccess);
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
    Fault fault = faultDAO.getFault(faultSection.getFaultName());
    int faultId=1;
    if(fault!=null) {
    	faultId = fault.getFaultId(); 
    	//throw new InsertException("Unable to insert faultsection \""+faultSection.getSectionName()+"\" into database as Faultname \""+faultSection.getFaultName()+"\" does not exist in database");
    } else {
    	System.out.println("Inserting "+faultSection.getSectionName()+" with faultId of 1 as Faultname \""+faultSection.getFaultName()+"\" does not exist in database");
    }
    //int faultId = fault.getFaultId();

    // get JGeomtery object from fault trace
    JGeometry faultSectionTraceGeom =  getGeomtery(faultSection.getFaultTrace());

    // various estimate ids
    int aveLongTermSlipRateEstId = this.estimateInstancesDAO.addEstimateInstance(faultSection.getAveLongTermSlipRateEst());
    int aveDipEst = this.estimateInstancesDAO.addEstimateInstance(faultSection.getAveDipEst());
    int aveRakeEst =  this.estimateInstancesDAO.addEstimateInstance(faultSection.getAveRakeEst());
    int aveUpperDepthEst = this.estimateInstancesDAO.addEstimateInstance(faultSection.getAveUpperDepthEst());
    int aveLowerDepthEst = this.estimateInstancesDAO.addEstimateInstance(faultSection.getAveLowerDepthEst());
    int aseismicSlipFactorEst = this.estimateInstancesDAO.addEstimateInstance(faultSection.getAseismicSlipFactorEst());
    int sectionSourceId = this.sectionSourceDAO.getSectionSource(faultSection.getSource()).getSourceId();
    // insert the fault section into the database
    ArrayList geomteryObjectList = new ArrayList();
    geomteryObjectList.add(faultSectionTraceGeom);
    String sql = "insert into "+TABLE_NAME+"("+ SECTION_ID+","+FAULT_ID+","+
        AVE_LONG_TERM_SLIP_RATE_EST+","+AVE_DIP_EST+","+
        AVE_RAKE_EST+","+AVE_UPPER_DEPTH_EST+","+AVE_LOWER_DEPTH_EST+","+
        CONTRIBUTOR_ID+","+SECTION_NAME+","+ENTRY_DATE+","+COMMENTS+","+
        FAULT_TRACE+","+ASEISMIC_SLIP_FACTOR_EST+","+DIP_DIRECTION+","+
        SECTION_SOURCE_ID+") values ("+
        faultSectionId+","+faultId+","+aveLongTermSlipRateEstId+","+
        aveDipEst+","+aveRakeEst+","+aveUpperDepthEst+","+aveLowerDepthEst+","+
        SessionInfo.getContributor().getId()+",'"+faultSection.getSectionName()+"','"+
        systemDate+"','"+faultSection.getComments()+"',?,"+
        aseismicSlipFactorEst+","+faultSection.getDipDirection()+","+
        sectionSourceId+")";
    try {
      dbAccess.insertUpdateOrDeleteData(sql, geomteryObjectList);
      return faultSectionId;
    }
    catch(SQLException e) {
      throw new InsertException(e.getMessage());
    }
  }
  
  /**
   * Get the fault section based on fault section Id
   * @param faultSectionId
   * @return
   */
  public FaultSectionVer2 getFaultSection(int faultSectionId) {	
	  String condition = " where "+SECTION_ID+"="+faultSectionId;
	  ArrayList faultSectionsList = query(condition);	
	  FaultSectionVer2 faultSection = null;		
	  if(faultSectionsList.size()>0) faultSection = (FaultSectionVer2)faultSectionsList.get(0);
	  return faultSection;  
  }
  
  /**
   * Get all the fault sections from the database
   * @return
   */
  public ArrayList getAllFaultSections() {
	  return query(" ");
  }
  
  /**
   * Get the fault sections based on some filter parameter
   * @param condition
   * @return
   */
  private ArrayList query(String condition) {
	  ArrayList faultSectionsList = new ArrayList();
	  String sqlWithSpatialColumnNames =  "select "+SECTION_ID+","+FAULT_ID+",to_char("+ENTRY_DATE+") as "+ENTRY_DATE+
      ","+AVE_LONG_TERM_SLIP_RATE_EST+","+AVE_DIP_EST+","+AVE_RAKE_EST+","+AVE_UPPER_DEPTH_EST+","+
      AVE_LOWER_DEPTH_EST+","+SECTION_NAME+","+COMMENTS+","+FAULT_TRACE+","+ASEISMIC_SLIP_FACTOR_EST+
      ","+DIP_DIRECTION+","+SECTION_SOURCE_ID +" from "+TABLE_NAME+condition;
	  
	  String sqlWithNoSpatialColumnNames =  "select "+SECTION_ID+","+FAULT_ID+",to_char("+ENTRY_DATE+") as "+ENTRY_DATE+
      ","+AVE_LONG_TERM_SLIP_RATE_EST+","+AVE_DIP_EST+","+AVE_RAKE_EST+","+AVE_UPPER_DEPTH_EST+","+
      AVE_LOWER_DEPTH_EST+","+SECTION_NAME+","+COMMENTS+","+ASEISMIC_SLIP_FACTOR_EST+
      ","+DIP_DIRECTION+","+SECTION_SOURCE_ID +" from "+TABLE_NAME+condition;

	  ArrayList spatialColumnNames = new ArrayList();
	  spatialColumnNames.add(FAULT_TRACE);
	  try {
		  SpatialQueryResult spatialQueryResult  = dbAccess.queryData(sqlWithSpatialColumnNames, sqlWithNoSpatialColumnNames, spatialColumnNames);
		  ResultSet rs = spatialQueryResult.getCachedRowSet();
		  int i=0;
		  while(rs.next())  {
			  FaultSectionVer2 faultSection = new FaultSectionVer2();
			  faultSection.setSectionId(rs.getInt(SECTION_ID));
			  faultSection.setFaultName(this.faultDAO.getFault(rs.getInt(FAULT_ID)).getFaultName());
			  faultSection.setComments(rs.getString(COMMENTS));
			  faultSection.setDipDirection(rs.getFloat(this.DIP_DIRECTION));
			  faultSection.setEntryDate(rs.getString(ENTRY_DATE));
			  faultSection.setSectionName(rs.getString(SECTION_NAME));
			  faultSection.setSource(this.sectionSourceDAO.getSectionSource(rs.getInt(rs.getInt(SECTION_SOURCE_ID))).getSectionSourceName());
			  faultSection.setAseismicSlipFactorEst(this.estimateInstancesDAO.getEstimateInstance(rs.getInt(this.ASEISMIC_SLIP_FACTOR_EST)));
			  faultSection.setAveDipEst(this.estimateInstancesDAO.getEstimateInstance(rs.getInt(this.AVE_DIP_EST)));
			  faultSection.setAveLongTermSlipRateEst(this.estimateInstancesDAO.getEstimateInstance(rs.getInt(this.AVE_LONG_TERM_SLIP_RATE_EST)));
			  faultSection.setAveLowerDepthEst(this.estimateInstancesDAO.getEstimateInstance(rs.getInt(this.AVE_LOWER_DEPTH_EST)));
			  faultSection.setAveRakeEst(this.estimateInstancesDAO.getEstimateInstance(rs.getInt(this.AVE_RAKE_EST)));
			  faultSection.setAveUpperDepthEst(this.estimateInstancesDAO.getEstimateInstance(rs.getInt(this.AVE_UPPER_DEPTH_EST)));
		      // fault trace
			  ArrayList geometries = spatialQueryResult.getGeometryObjectsList(i++);
			  JGeometry faultSectionGeom =(JGeometry) geometries.get(0);
			  FaultTrace faultTrace = new FaultTrace(rs.getString(SECTION_NAME));
			  int numPoints = faultSectionGeom.getNumPoints();
			  double[] ordinatesArray = faultSectionGeom.getOrdinatesArray();
			  for(int j=0; j<numPoints; ++j) {
				  faultTrace.addLocation(new Location(ordinatesArray[2*j+1], ordinatesArray[2*j]));
			  }	
		      faultSection.setFaultTrace(faultTrace);
			  faultSectionsList.add(faultSection);
		  }
		  rs.close();
	  } catch(SQLException e) { throw new QueryException(e.getMessage()); }
	  	return faultSectionsList;
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