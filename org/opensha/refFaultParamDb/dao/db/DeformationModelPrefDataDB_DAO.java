/**
 * 
 */
package org.opensha.refFaultParamDb.dao.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import org.opensha.refFaultParamDb.dao.exception.InsertException;
import org.opensha.refFaultParamDb.dao.exception.QueryException;
import org.opensha.refFaultParamDb.dao.exception.UpdateException;
import org.opensha.refFaultParamDb.gui.infotools.SessionInfo;
import org.opensha.refFaultParamDb.vo.DeformationModelSummary;
import org.opensha.refFaultParamDb.vo.FaultSectionData;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.opensha.refFaultParamDb.vo.FaultSectionSummary;

/**
 * 
 * Get the preferred slip and aseismic slip factor from the deformation model
 * 
 * @author vipingupta
 *
 */
public class DeformationModelPrefDataDB_DAO {
	private final static String TABLE_NAME = "Pref_Deformation_Model_Data";
	private final static String DEFORMATION_MODEL_ID = "Deformation_Model_Id";
	private final static String SECTION_ID = "Section_Id";
	private final static String PREF_LONG_TERM_SLIP_RATE = "Pref_Long_Term_Slip_Rate";
	private final static String PREF_ASEISMIC_SLIP = "Pref_Aseismic_Slip";
	private static HashMap slipRateMap;
	private static HashMap aseismicSlipMap;
	private static int selectedDefModelId = -1;
	private DB_AccessAPI dbAccess;
	private PrefFaultSectionDataDB_DAO prefFaultSectionDAO;
	private DeformationModelDB_DAO deformationModelDB_DAO;
	
	public DeformationModelPrefDataDB_DAO(DB_AccessAPI dbAccess) {
		setDB_Connection(dbAccess);
	}

	/**
	 * Set the database connection
	 * @param dbAccess
	 */
	public void setDB_Connection(DB_AccessAPI dbAccess) {
		this.dbAccess = dbAccess;
		 prefFaultSectionDAO = new PrefFaultSectionDataDB_DAO(dbAccess); 
		 deformationModelDB_DAO = new DeformationModelDB_DAO(dbAccess);
	}
	
	/**
	 * Remove the exisiting data from preferred data table and re-populate it.
	 *
	 */
	public void rePopulatePrefDataTable() {
		removeAll(); // remove all the pref data
		
		// iterate over all deformation Models
		DeformationModelSummaryDB_DAO defModelSumDAO = new DeformationModelSummaryDB_DAO(this.dbAccess);
		ArrayList deformationModelList = defModelSumDAO.getAllDeformationModels();
		
		
		int faultSectionId, deformationModelId;
		double aseismicSlipFactor, slipRate;
		for(int i=0; i<deformationModelList.size(); ++i) {
			DeformationModelSummary defModelSummary = (DeformationModelSummary)deformationModelList.get(i);
			deformationModelId = defModelSummary.getDeformationModelId();
			// get the fault sections in each deformation model
			ArrayList faultSectionIdList = deformationModelDB_DAO.getFaultSectionIdsForDeformationModel(deformationModelId);
			for(int j=0; j<faultSectionIdList.size(); ++j) {
				faultSectionId = ((Integer)faultSectionIdList.get(j)).intValue();
				aseismicSlipFactor = FaultSectionData.getPrefForEstimate(deformationModelDB_DAO.getAseismicSlipEstimate(deformationModelId, faultSectionId));
				if(aseismicSlipFactor==1) {
					System.out.println(deformationModelId+","+faultSectionId+","+deformationModelDB_DAO.getAseismicSlipEstimate(deformationModelId, faultSectionId));
					System.exit(0);
				}
				slipRate = FaultSectionData.getPrefForEstimate(deformationModelDB_DAO.getSlipRateEstimate(deformationModelId, faultSectionId));
				addToTable(deformationModelId, faultSectionId, aseismicSlipFactor, slipRate);
			}
		}
	}
	
	
	/**
	 * Add data to table
	 * @param deformationModelId
	 * @param faultSectionId
	 * @param aseismicSlipFactor
	 * @param slipRate
	 */
	private void addToTable(int deformationModelId, int faultSectionId, 
			double aseismicSlipFactor, double slipRate) {
		String columnNames = "";
		String colVals = "";
		if(!Double.isNaN(slipRate)) {
			columnNames +=PREF_LONG_TERM_SLIP_RATE+",";
			colVals +=slipRate+",";
		}
		String sql = "insert into "+TABLE_NAME+" ("+DEFORMATION_MODEL_ID+","+
			SECTION_ID+","+columnNames+PREF_ASEISMIC_SLIP+") values ("+
			deformationModelId+","+faultSectionId+","+colVals+aseismicSlipFactor+")";
		try {
			dbAccess.insertUpdateOrDeleteData(sql);
		}
		catch(SQLException e) {
			throw new InsertException(e.getMessage());
		}
	}
	
	
	/**
	 * Get a list of all fault sections within this deformation model
	 * @param deformationModelId
	 * @return
	 */
	public ArrayList getFaultSectionIdsForDeformationModel(int deformationModelId) {
		return this.deformationModelDB_DAO.getFaultSectionIdsForDeformationModel(deformationModelId);
	}
	
	/**
	 * Get Fault Section Pref data for a deformation model ID and Fault section Id
	 * @param deformationModelId
	 * @param faultSectionId
	 * @return
	 */
	public FaultSectionPrefData getFaultSectionPrefData(int deformationModelId,
			int faultSectionId) {
		FaultSectionPrefData faultSectionPrefData = prefFaultSectionDAO.getFaultSectionPrefData(faultSectionId);
		// get slip rate and aseimic slip factor from deformation model
		faultSectionPrefData.setAseismicSlipFactor(this.getAseismicSlipFactor(deformationModelId, faultSectionId));
		faultSectionPrefData.setAveLongTermSlipRate(this.getSlipRate(deformationModelId, faultSectionId));
		return faultSectionPrefData;
	}
	
	/**
	 * Get the preferred Slip Rate value for selected Deformation Model and Fault Section
	 * Returns NaN if Slip rate is not available
	 * 
	 * @param deformationModelId
	 * @param faultSectionId
	 * @return
	 */
	public double getSlipRate(int deformationModelId, int faultSectionId) {
		if(this.selectedDefModelId!=deformationModelId) this.cacheSlipRateAndAseismicSlip(deformationModelId);
		Double slipRate =  (Double)slipRateMap.get(new Integer(faultSectionId));
		if(slipRate==null) return Double.NaN;
		else return slipRate.doubleValue();
	}	
	
	/**
	 * Get the preferred Aseismic Slip Factor for selected Deformation Model and Fault Section
	 * 
	 * 
	 * @param deformationModelId
	 * @param faultSectionId
	 * @return
	 */
	public double getAseismicSlipFactor(int deformationModelId, int faultSectionId) {
		if(this.selectedDefModelId!=deformationModelId) this.cacheSlipRateAndAseismicSlip(deformationModelId);
		Double aseismicSlip = (Double)aseismicSlipMap.get(new Integer(faultSectionId));
		if(aseismicSlip == null) return Double.NaN;
		else return aseismicSlip.doubleValue();
	}
	
	
	private void cacheSlipRateAndAseismicSlip(int defModelId) {
		slipRateMap = new HashMap();
		aseismicSlipMap = new HashMap();
		String sql= "select "+SECTION_ID+"," +
		" ("+PREF_ASEISMIC_SLIP+"+0) "+PREF_ASEISMIC_SLIP+","+
		" ("+PREF_LONG_TERM_SLIP_RATE+"+0) "+PREF_LONG_TERM_SLIP_RATE+
		" from "+TABLE_NAME+" where " + DEFORMATION_MODEL_ID+"="+defModelId;
		double aseismicSlipFactor=Double.NaN,slip=Double.NaN;
		int sectionId;
		try {
			ResultSet rs  = this.dbAccess.queryData(sql);
			while(rs.next()) {
				aseismicSlipFactor = rs.getFloat(PREF_ASEISMIC_SLIP);
				if(rs.wasNull()) aseismicSlipFactor = Double.NaN;
				slip = rs.getFloat(PREF_LONG_TERM_SLIP_RATE);
				if(rs.wasNull()) slip = Double.NaN;
				sectionId = rs.getInt(SECTION_ID);
				slipRateMap.put(new Integer(sectionId), new Double(slip)) ;
				aseismicSlipMap.put(new Integer(sectionId), new Double(aseismicSlipFactor));
			}
		} catch (SQLException e) {
			throw new QueryException(e.getMessage());
		}
		this.selectedDefModelId = defModelId;
		
	}
	
	
	/**
	 * Remove all preferred data
	 */
	private void removeAll() {
		String sql = "delete from "+TABLE_NAME;
		try {
			dbAccess.insertUpdateOrDeleteData(sql);
		} catch(SQLException e) { throw new UpdateException(e.getMessage()); }
	}
	
	public static void main(String[] args) {
		DB_AccessAPI dbAccessAPI = new ServerDB_Access();
		SessionInfo.setUserName(args[0]);
	    SessionInfo.setPassword(args[1]);
	    SessionInfo.setContributorInfo();
		DeformationModelPrefDataDB_DAO defModelPrefDataDB_DAO = new DeformationModelPrefDataDB_DAO(dbAccessAPI);
		defModelPrefDataDB_DAO.rePopulatePrefDataTable();
		System.exit(0);
	}
}
