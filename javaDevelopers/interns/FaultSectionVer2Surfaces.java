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

/**
 * This class reads the fault sections ver2 from the database. It makes gridded suface using the fault trace.
 * This class is needed to visuaize the fault sections in SCEC-VDO.
 *
 * @author vipingupta
 *
 */
public class FaultSectionVer2Surfaces {
	private ArrayList frankelGriddedSurfaceList = new ArrayList();
	private ArrayList stirlingGriddedSurfaceList = new ArrayList();
	private ArrayList faultSectionNames = new ArrayList();
	private final static double GRID_SPACING = 0.1;
	
	public FaultSectionVer2Surfaces() {
		// read the fault sections from the database
		FaultSectionVer2_DB_DAO faultSectionDAO = new FaultSectionVer2_DB_DAO(DB_AccessAPI.dbConnection);
		// get all the fault sections from the database
		ArrayList faultSections  = faultSectionDAO.getAllFaultSections();
		int numSections = faultSections.size();
		// iterate over all the fault sections and write them to the file
		for(int i=0; i<numSections; ++i) {
			// make SimpleFaultData object to get the surface
			FaultSectionVer2 faultSection = (FaultSectionVer2)faultSections.get(i);
			faultSectionNames.add(faultSection.getSectionName());
			double prefDip = ((MinMaxPrefEstimate)faultSection.getAveDipEst().getEstimate()).getPreferredX();
			double prefUpperDepth = ((MinMaxPrefEstimate)faultSection.getAveUpperDepthEst().getEstimate()).getPreferredX();
			double prefLowerDepth = ((MinMaxPrefEstimate)faultSection.getAveLowerDepthEst().getEstimate()).getPreferredX();
			SimpleFaultData simpleFaultData = new SimpleFaultData(prefDip, prefLowerDepth, prefUpperDepth, faultSection.getFaultTrace());
			// frankel fault factory
			FrankelGriddedFaultFactory frankelGriddedFaultFactory = new FrankelGriddedFaultFactory(simpleFaultData, GRID_SPACING);
			frankelGriddedSurfaceList.add(frankelGriddedFaultFactory.getGriddedSurface());
			// stirling fault factory
			StirlingGriddedFaultFactory stirlingGriddedFaultFactory = new StirlingGriddedFaultFactory(simpleFaultData, GRID_SPACING);
			stirlingGriddedSurfaceList.add(stirlingGriddedFaultFactory.getGriddedSurface());
		}
	}
 
   /**
    * Get the names of the fault sections
    * @return
    */
   public ArrayList getFaultSectionNames() {
	   return this.faultSectionNames;
   }
	
	/**
	 * ArrayList of GriddedSurfaceAPI objects
	 * @return
	 */
  public ArrayList getFrankelGriddedSurfaceList() {
	  return frankelGriddedSurfaceList;
  }
  
  /**
   * Arraylist of GriddedSurfaceAPI objects
   * @return
   */
  public ArrayList getStirlingGriddedSurfaceList() {
	  return this.stirlingGriddedSurfaceList;
  }
  
  public static void main(String args[]) {
	  new FaultSectionVer2Surfaces();
  }
}
