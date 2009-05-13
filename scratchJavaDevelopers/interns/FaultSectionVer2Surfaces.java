
package scratchJavaDevelopers.interns;

import java.util.ArrayList;

import org.opensha.refFaultParamDb.dao.db.DB_AccessAPI;
import org.opensha.refFaultParamDb.dao.db.FaultSectionVer2_DB_DAO;
import org.opensha.refFaultParamDb.dao.db.PrefFaultSectionDataDB_DAO;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.opensha.sha.faultSurface.*;

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
	private static ArrayList faultSectionsSummary=null;
	
	public FaultSectionVer2Surfaces() {
		if(faultSectionsSummary==null) {
			cacheFaultSectionPrefData();
		}
	}

	/**
	 * cache the data for faster access
	 *
	 */
	private void cacheFaultSectionPrefData() {
		faultSectionsSummary = this.faultSectionDAO.getAllFaultSectionsSummary();
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
		SimpleFaultData simpleFaultData = getFaultSection(faultSectionId).getSimpleFaultData(false);
		//frankel fault factory
		return new FrankelGriddedSurface(simpleFaultData, GRID_SPACING);
	}

	/**
	 * Get the Gridded surface based on Stirling's method for a Fault Section Id
	 * @param faultSectionId
	 * @return
	 */
	public EvenlyGriddedSurfaceAPI getStirlingSurface(int faultSectionId) {
		SimpleFaultData simpleFaultData = getFaultSection(faultSectionId).getSimpleFaultData(false);
		return new StirlingGriddedSurface(simpleFaultData, GRID_SPACING);
	}

	/**
	 * This function allows the user to refresh the fault section data for the specific sectionId from the database
	 *  
	 * @param faultSectionId
	 */
	public void reloadFaultSectionFromDatabase(int faultSectionId) {
		throw new RuntimeException ("Method not implemented");
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
		return this.faultSectionPrefDataDAO.getFaultSectionPrefData(faultSectionId);		
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
