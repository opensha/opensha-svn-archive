/**
 * 
 */
package org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF2.A_Faults.fetchers;

import java.util.ArrayList;

import org.opensha.refFaultParamDb.dao.db.DB_AccessAPI;
import org.opensha.refFaultParamDb.dao.db.DeformationModelDB_DAO;
import org.opensha.refFaultParamDb.dao.db.PrefFaultSectionDataDB_DAO;
import org.opensha.refFaultParamDb.vo.FaultSectionData;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.opensha.refFaultParamDb.vo.FaultSectionSummary;

/**
 * @author vipingupta
 * This class generates a list of B faults (faults which are not A faults and which have non zero slip
 * rate in deformation model) 
 */
public class B_FaultsFetcher {
	private A_FaultsFetcher aFaultsFetcher=null;
	private DeformationModelDB_DAO deformationModelDB_DAO = new DeformationModelDB_DAO(DB_AccessAPI.dbConnection);
	private PrefFaultSectionDataDB_DAO prefFaultSectionDAO = new PrefFaultSectionDataDB_DAO(DB_AccessAPI.dbConnection);

	
	/**
	 * default constructor
	 *
	 */
	public B_FaultsFetcher() {
		setA_FaultFetcher(new A_FaultsFetcher());
	}
	
	public B_FaultsFetcher(A_FaultsFetcher aFaultsFetcher) {
		setA_FaultFetcher(aFaultsFetcher);
	}
	
	/**
	 * Set A_FaultFetcher - It is needed to get the Ids of all A Faults
	 * @param aFaultsFetcher
	 */
	public void setA_FaultFetcher(A_FaultsFetcher aFaultsFetcher) {
		this.aFaultsFetcher = aFaultsFetcher;
	}
	
	/**
	 * Get PrefFaultSectionData for B faults
	 * @param deformationModelId
	 * @return
	 */
	public ArrayList getBFaults(int deformationModelId) {
		ArrayList faultSectionsInDefModel = deformationModelDB_DAO.getFaultSectionIdsForDeformationModel(deformationModelId);
		ArrayList aFaultsList = this.aFaultsFetcher.getAllFaultSectionsIdList(); 
		ArrayList bFaultsList = new ArrayList();
		for(int i=0; i<faultSectionsInDefModel.size(); ++i) {
			// if this is A type fault
			int faultSectionId = ((Integer)faultSectionsInDefModel.get(i)).intValue();
			if(aFaultsList.contains(faultSectionsInDefModel.get(i))) {
				//System.out.println(faultSectionId+" is A type fault");
				continue;
			}
			FaultSectionPrefData faultSectionPrefData = prefFaultSectionDAO.getFaultSectionPrefData(faultSectionId);
			// get slip rate and aseimic slip factor from deformation model
			faultSectionPrefData.setAseismicSlipFactor(FaultSectionData.getPrefForEstimate(this.deformationModelDB_DAO.getAseismicSlipEstimate(deformationModelId, faultSectionId)));
			faultSectionPrefData.setAveLongTermSlipRate(FaultSectionData.getPrefForEstimate(this.deformationModelDB_DAO.getSlipRateEstimate(deformationModelId, faultSectionId)));
			// add to B type faults only if slip is not 0 and not NaN
			if(faultSectionPrefData.getAveLongTermSlipRate()!=0.0 && !Double.isNaN(faultSectionPrefData.getAveLongTermSlipRate()))
				bFaultsList.add(faultSectionPrefData);
		}
		return bFaultsList;
	}
	
	public static void main(String[] args) {
		// def model ids from 42-49, 61 - 68
		B_FaultsFetcher b = new B_FaultsFetcher();
		ArrayList bFaults = b.getBFaults(42);
		System.out.println("Number of B faults="+bFaults.size());
	}
}
