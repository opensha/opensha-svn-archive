
package scratchJavaDevelopers.interns;

import java.util.ArrayList;
import java.util.HashMap;

import org.opensha.refFaultParamDb.dao.db.DB_AccessAPI;
import org.opensha.refFaultParamDb.dao.db.FaultSectionVer2_DB_DAO;
import org.opensha.refFaultParamDb.dao.db.PrefFaultSectionDataDB_DAO;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.opensha.refFaultParamDb.vo.FaultSectionSummary;
import org.opensha.sha.fault.SimpleFaultData;
import org.opensha.sha.surface.*;

/**
 * This class reads the fault sections ver2 from the database. It makes gridded suface using the fault trace.
 * This class is needed to visuaize the fault sections in SCEC-VDO.
 *
 * @author vipingupta
 *
 */
public class FaultSectionVer2Surfaces implements FaultSectionSurfaces {

	private final static double GRID_SPACING = 1.0;
	private final FaultSectionVer2_DB_DAO faultSectionDAO = new FaultSectionVer2_DB_DAO(DB_AccessAPI.dbConnection);
	private final PrefFaultSectionDataDB_DAO faultSectionPrefDataDAO = new PrefFaultSectionDataDB_DAO(DB_AccessAPI.dbConnection);
	private static HashMap faultSectionPrefDataMap=null;
	private static ArrayList faultSectionsSummary=null;
	
	public FaultSectionVer2Surfaces() {
		if(faultSectionPrefDataMap==null) {
			cacheFaultSectionPrefData();
		}
	}

	/**
	 * cache the data for faster access
	 *
	 */
	private void cacheFaultSectionPrefData() {
		faultSectionPrefDataMap  =new HashMap();
		faultSectionsSummary = new ArrayList();
		ArrayList faultSectionPrefList = faultSectionPrefDataDAO.getAllFaultSectionPrefData();
		for(int i=0; i<faultSectionPrefList.size(); ++i) {
			FaultSectionPrefData faultSectionPrefData = (FaultSectionPrefData)faultSectionPrefList.get(i);
			faultSectionPrefDataMap.put(new Integer(faultSectionPrefData.getSectionId()), faultSectionPrefData);
			faultSectionsSummary.add(new FaultSectionSummary(faultSectionPrefData.getSectionId(), faultSectionPrefData.getSectionName()));
		}
	}

	/**
	 * Get the names and id of all fault sections
	 * @return
	 */
	public ArrayList getAllFaultSectionsSummary() {
		if(faultSectionsSummary==null)
			faultSectionsSummary= this.faultSectionDAO.getAllFaultSectionsSummary();
		return faultSectionsSummary;
		
	}

	/**
	 * Get the Gridded surface based on Frankel's method for a Fault Section ID
	 * @param faultSectionId
	 * @return
	 */
	public EvenlyGriddedSurfaceAPI getFrankelSurface(int faultSectionId) {
		FaultSectionPrefData faultSection = getFaultSection(faultSectionId);
		SimpleFaultData simpleFaultData = getSimpleFaultData(faultSection);
		//frankel fault factory
		return new FrankelGriddedSurface(simpleFaultData, GRID_SPACING);
	}

	/**
	 * Get the Gridded surface based on Stirling's method for a Fault Section Id
	 * @param faultSectionId
	 * @return
	 */
	public EvenlyGriddedSurfaceAPI getStirlingSurface(int faultSectionId) {
		FaultSectionPrefData faultSection = getFaultSection(faultSectionId);
		SimpleFaultData simpleFaultData = getSimpleFaultData(faultSection);
		// stirling fault factory
		return new StirlingGriddedSurface(simpleFaultData, GRID_SPACING);
	}

	/**
	 * This function allows the user to refresh the fault section data for the specific sectionId from the database
	 *  
	 * @param faultSectionId
	 */
	public void reloadFaultSectionFromDatabase(int faultSectionId) {
		faultSectionPrefDataDAO.rePopulatePrefDataTable(faultSectionId);
		faultSectionPrefDataMap.put(new Integer(faultSectionId),
				this.faultSectionPrefDataDAO.getFaultSectionPrefData(faultSectionId));
	}
	
	/**
	 * Refresh all the fault sections which are currently in cache
	 *
	 */
	public void reloadAllFaultSectionsFromDatabase() {
		this.faultSectionPrefDataDAO.rePopulatePrefDataTable();
		cacheFaultSectionPrefData();
	}
	
	/**
	 * Get fault section based on section Id
	 * @param faultSectionId
	 * @return
	 */
	private FaultSectionPrefData getFaultSection(int faultSectionId) {
		return (FaultSectionPrefData)this.faultSectionPrefDataMap.get(new Integer(faultSectionId));		
	}

	/**
	 * Make simple fault data from faulSection. It assumes that all estimates are Min/Max/Pref Estimates. so, we just
	 * get Preffered values from these estimates
	 *
	 * @param faultSection
	 * @return
	 */
	private SimpleFaultData getSimpleFaultData(FaultSectionPrefData faultSectionPrefData) {
		SimpleFaultData simpleFaultData = new SimpleFaultData(faultSectionPrefData.getAveDip(),
				faultSectionPrefData.getAveLowerDepth(), faultSectionPrefData.getAveUpperDepth(), 
				faultSectionPrefData.getFaultTrace());
		return simpleFaultData;
	}
	
	/**
	 * Get the Minimum value for slip rate 
	 * @return
	 */
	public double getMinSlipRate() {
		return faultSectionPrefDataDAO.getMinSlipRate();
	}
	
	/**
	 * Get the maximum value for slip rate
	 * @return
	 */
	public double getMaxSlipRate() {
		return faultSectionPrefDataDAO.getMaxSlipRate();
	}
	
	/**
	 * 
	 * @param faultSectionId
	 * @return
	 */
	public double getSlipRate(int faultSectionId) {
		double slipRate = this.getFaultSection(faultSectionId).getAveLongTermSlipRate();
		if(Double.isNaN(slipRate)) slipRate = 0;
		return slipRate;
	}
	
}
