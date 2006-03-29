/**
 * 
 */
package javaDevelopers.interns;

import java.util.ArrayList;

import org.opensha.data.estimate.MinMaxPrefEstimate;
import org.opensha.refFaultParamDb.dao.db.DB_AccessAPI;
import org.opensha.refFaultParamDb.dao.db.FaultSectionVer2_DB_DAO;
import org.opensha.refFaultParamDb.vo.FaultSectionVer2;
import org.opensha.sha.fault.FrankelGriddedFaultFactory;
import org.opensha.sha.fault.SimpleFaultData;
import org.opensha.sha.fault.StirlingGriddedFaultFactory;
import org.opensha.sha.surface.GriddedSurfaceAPI;

/**
 * This class reads the fault sections ver2 from the database. It makes gridded suface using the fault trace.
 * This class is needed to visuaize the fault sections in SCEC-VDO.
 *
 * @author vipingupta
 *
 */
public class FaultSectionVer2Surfaces {

	private final static double GRID_SPACING = 1.0;
	private final FaultSectionVer2_DB_DAO faultSectionDAO = new FaultSectionVer2_DB_DAO(DB_AccessAPI.dbConnection);
	
	public FaultSectionVer2Surfaces() {}
	
	/**
	 * Get the names and id of all fault sections
	 * @return
	 */
	public ArrayList getAllFaultSections() {
		return this.faultSectionDAO.getAllFaultSectionsSummary();
	}

	/**
	 * Get the Gridded surface based on Frankel's method for a Fault Section ID
	 * @param faultSectionId
	 * @return
	 */
	public GriddedSurfaceAPI getFrankelSurface(int faultSectionId) {
		FaultSectionVer2 faultSection = faultSectionDAO.getFaultSection(faultSectionId);
		return getFrankelSurface(faultSection);
	}
	
	/**
	 * Get the Gridded surface based on Frankel's method for a Fault Section object
	 * @param faultSection
	 * @return
	 */
	public GriddedSurfaceAPI getFrankelSurface(FaultSectionVer2 faultSection) {
		SimpleFaultData simpleFaultData = getSimpleFaultData(faultSection);
//		 frankel fault factory
		FrankelGriddedFaultFactory frankelGriddedFaultFactory = new FrankelGriddedFaultFactory(simpleFaultData, GRID_SPACING);
		return frankelGriddedFaultFactory.getGriddedSurface();
	}
	
	/**
	 * Get the Gridded surface based on Stirling's method for a Fault Section object
	 * @param faultSection
	 * @return
	 */
	public GriddedSurfaceAPI getStirlingSurface(FaultSectionVer2 faultSection) {
		SimpleFaultData simpleFaultData = getSimpleFaultData(faultSection);
		// stirling fault factory
		StirlingGriddedFaultFactory stirlingGriddedFaultFactory = new StirlingGriddedFaultFactory(simpleFaultData, GRID_SPACING);
		return stirlingGriddedFaultFactory.getGriddedSurface();		
	}
	
	/**
	 * Get the Gridded surface based on Stirling's method for a Fault Section Id
	 * @param faultSectionId
	 * @return
	 */
	public GriddedSurfaceAPI getStirlingSurface(int faultSectionId) {
		FaultSectionVer2 faultSection = faultSectionDAO.getFaultSection(faultSectionId);
		return getStirlingSurface(faultSection);
	}
	
	private SimpleFaultData getSimpleFaultData(FaultSectionVer2 faultSection) {
		double prefDip = ((MinMaxPrefEstimate)faultSection.getAveDipEst().getEstimate()).getPreferredX();
		double prefUpperDepth = ((MinMaxPrefEstimate)faultSection.getAveUpperDepthEst().getEstimate()).getPreferredX();
		double prefLowerDepth = ((MinMaxPrefEstimate)faultSection.getAveLowerDepthEst().getEstimate()).getPreferredX();
		SimpleFaultData simpleFaultData = new SimpleFaultData(prefDip, prefLowerDepth, prefUpperDepth, faultSection.getFaultTrace());
		return simpleFaultData;
	}
	
	// get the fault section based on fault section Id
	public FaultSectionVer2 getFaultSection(int faultSectionId) {
		return faultSectionDAO.getFaultSection(faultSectionId);
	}
}
