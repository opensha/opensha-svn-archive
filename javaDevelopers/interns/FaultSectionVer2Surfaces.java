
package javaDevelopers.interns;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.HashMap;

import org.opensha.data.estimate.MinMaxPrefEstimate;
import org.opensha.data.estimate.NormalEstimate;
import org.opensha.refFaultParamDb.dao.db.DB_AccessAPI;
import org.opensha.refFaultParamDb.dao.db.FaultSectionVer2_DB_DAO;
import org.opensha.refFaultParamDb.vo.FaultSectionVer2;
import org.opensha.refFaultParamDb.vo.EstimateInstances;
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
	private HashMap faultSectionsMap = new HashMap();
	private HashMap faultSectionSlipMap = new HashMap();
	private double minSlipRate = Double.NaN;
	private double maxSlipRate = Double.NaN;

	public FaultSectionVer2Surfaces() {}

	/**
	 * Get the names and id of all fault sections
	 * @return
	 */
	public ArrayList getAllFaultSectionsSummary() {
		faultSectionSlipMap = faultSectionDAO.getSlipRateEstimates();
		// calculate the min and max slip rate
		Iterator it = faultSectionSlipMap.values().iterator();
		minSlipRate = Double.POSITIVE_INFINITY;
		maxSlipRate = Double.NEGATIVE_INFINITY;
		double val;
		while(it.hasNext()) {
			EstimateInstances estimateInstance   = (EstimateInstances)it.next();
			val = this.getValueFromEstimate(estimateInstance);
			if(val<minSlipRate) minSlipRate = val;
			if(val>maxSlipRate) maxSlipRate = val;
		}
		return this.faultSectionDAO.getAllFaultSectionsSummary();
		
	}

	/**
	 * Get the Gridded surface based on Frankel's method for a Fault Section ID
	 * @param faultSectionId
	 * @return
	 */
	public EvenlyGriddedSurfaceAPI getFrankelSurface(int faultSectionId) {
		FaultSectionVer2 faultSection = getFaultSection(faultSectionId);
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
		FaultSectionVer2 faultSection = getFaultSection(faultSectionId);
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
		faultSectionsMap.remove(new Integer(faultSectionId));
		getFaultSection(faultSectionId);
	}
	
	/**
	 * Refresh all the fault sections which are currently in cache
	 *
	 */
	public void reloadAllFaultSectionsFromDatabase() {
		// get Id list of all fault sections which are currently cached
		ArrayList cachedFaultSectionsIdList= new ArrayList();
		Iterator keySet = faultSectionsMap.keySet().iterator();
		while(keySet.hasNext()) {
			cachedFaultSectionsIdList.add(keySet.next());
		}
		// now refresh the fault sections
		for(int i=0; i<cachedFaultSectionsIdList.size(); ++i) {
			reloadFaultSectionFromDatabase(((Integer)cachedFaultSectionsIdList.get(i)).intValue());
		}
	}
	
	/**
	 * Get fault section based on section Id. Also Cache the fault section in case it is needed for future references
	 * @param faultSectionId
	 * @return
	 */
	private FaultSectionVer2 getFaultSection(int faultSectionId) {
		FaultSectionVer2 faultSection  = (FaultSectionVer2) faultSectionsMap.get(new Integer(faultSectionId));
		if(faultSection==null) {
			faultSection = faultSectionDAO.getFaultSection(faultSectionId);
			this.faultSectionsMap.put(new Integer(faultSectionId), faultSection);
			// update min/max slip rate values
			double slipRateVal = this.getValueFromEstimate(faultSection.getAveLongTermSlipRateEst());
			if(slipRateVal<minSlipRate) minSlipRate = slipRateVal;
			if(slipRateVal>maxSlipRate) maxSlipRate = slipRateVal;
			this.faultSectionSlipMap.put(new Integer(faultSectionId), faultSection.getAveLongTermSlipRateEst());
		}
		return faultSection;
	}

	/**
	 * Make simple fault data from faulSection. It assumes that all estimates are Min/Max/Pref Estimates. so, we just
	 * get Preffered values from these estimates
	 *
	 * @param faultSection
	 * @return
	 */
	private SimpleFaultData getSimpleFaultData(FaultSectionVer2 faultSection) {
		double prefDip = ((MinMaxPrefEstimate)faultSection.getAveDipEst().getEstimate()).getPreferredX();
		double prefUpperDepth = ((MinMaxPrefEstimate)faultSection.getAveUpperDepthEst().getEstimate()).getPreferredX();
		double prefLowerDepth = ((MinMaxPrefEstimate)faultSection.getAveLowerDepthEst().getEstimate()).getPreferredX();
		SimpleFaultData simpleFaultData = new SimpleFaultData(prefDip, prefLowerDepth, prefUpperDepth, faultSection.getFaultTrace());
		return simpleFaultData;
	}
	
	/**
	 * Get the Minimum value for slip rate 
	 * @return
	 */
	public double getMinSlipRate() {
		return minSlipRate;
	}
	
	/**
	 * Get the maximum value for slip rate
	 * @return
	 */
	public double getMaxSlipRate() {
		return maxSlipRate;
	}
	
	/**
	 * 
	 * @param faultSectionId
	 * @return
	 */
	public double getSlipRate(int faultSectionId) {
		EstimateInstances estimateInstance = (EstimateInstances)this.faultSectionSlipMap.get(new Integer(faultSectionId));
		return getValueFromEstimate(estimateInstance);
	}

	/**
	 * Get value from estimate
	 * @param estimateInstance
	 * @return
	 */
	private double getValueFromEstimate(EstimateInstances estimateInstance) {
		if(estimateInstance==null) return 0;
		return ((NormalEstimate)estimateInstance.getEstimate()).getMean();
	}
	
	
}
