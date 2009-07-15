/**
 * 
 */
package org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_0.data;

import java.util.ArrayList;
import java.util.HashMap;

import org.opensha.refFaultParamDb.dao.db.DB_AccessAPI;
import org.opensha.refFaultParamDb.dao.db.DeformationModelDB_DAO;
import org.opensha.refFaultParamDb.dao.db.PrefFaultSectionDataDB_DAO;
import org.opensha.refFaultParamDb.vo.FaultSectionData;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_0.FaultSegmentData;

/**
 * @author vipingupta
 * This class generates a list of B faults (faults which are not A faults and which have non zero slip
 * rate in deformation model) 
 */
public class B_FaultsFetcher {
	private A_FaultsFetcher aFaultsFetcher=null;
	private DeformationModelDB_DAO deformationModelDB_DAO = new DeformationModelDB_DAO(DB_AccessAPI.dbConnection);
	private PrefFaultSectionDataDB_DAO prefFaultSectionDAO = new PrefFaultSectionDataDB_DAO(DB_AccessAPI.dbConnection);
	private ArrayList faultModelNames; 
	private int deformationModelId = -1;
	private HashMap faultSegmentMap;
	
	// This holds the special, multi-section B Faults
	private ArrayList specialFaults;
	private ArrayList allSpecialFaultIds;
	private int[] concordGreenValleySections = {1, 71, 3};
	private int[] greenvilleSections = {6, 7};
	private String[] specialFaultNames = {"Concord Green Valley","Greenville"};
	
	/**
	 * default constructor
	 *
	 */
	public B_FaultsFetcher() {
		aFaultsFetcher = new A_FaultsFetcher();
		// make special fault info
		specialFaults = new ArrayList();
		specialFaults.add(concordGreenValleySections);
		specialFaults.add(greenvilleSections);
		allSpecialFaultIds = new ArrayList();
		for(int i=0; i<specialFaults.size(); ++i) {
			int[] ids = (int[])specialFaults.get(i);
			for(int j=0; j<ids.length;++j) allSpecialFaultIds.add(new Integer(ids[j]));
		}
	}
	
	/**
	 * Get PrefFaultSectionData for B faults
	 * @param deformationModelId
	 * @return
	 */
	private void generateBFaults(int deformationModelId) {
		
		if(deformationModelId!=this.deformationModelId) {
			faultSegmentMap = new HashMap();
			this.deformationModelId = deformationModelId;
			faultModelNames = new ArrayList();
			ArrayList faultSectionsInDefModel = deformationModelDB_DAO.getFaultSectionIdsForDeformationModel(deformationModelId);
			ArrayList aFaultsList = this.aFaultsFetcher.getAllFaultSectionsIdList(); 
			for(int i=0; i<faultSectionsInDefModel.size(); ++i) {
				// if this is A type fault or a special fault, then do not process it
				if(aFaultsList.contains(faultSectionsInDefModel.get(i)) ||
					allSpecialFaultIds.contains(faultSectionsInDefModel.get(i))	) {
					//System.out.println(faultSectionId+" is A type fault");
					continue;
				}
				int faultSectionId = ((Integer)faultSectionsInDefModel.get(i)).intValue();
				FaultSectionPrefData faultSectionPrefData = prefFaultSectionDAO.getFaultSectionPrefData(faultSectionId);
				//if(!faultSectionPrefData.getSectionName().equalsIgnoreCase("Green Valley (No)")) continue;	
				faultSectionPrefData.setAveLongTermSlipRate(FaultSectionData.getPrefForEstimate(this.deformationModelDB_DAO.getSlipRateEstimate(deformationModelId, faultSectionId)));
				// add to B type faults only if slip is not 0 and not NaN
				if(faultSectionPrefData.getAveLongTermSlipRate()==0.0 || Double.isNaN(faultSectionPrefData.getAveLongTermSlipRate())) continue;
				faultSectionPrefData.setAseismicSlipFactor(FaultSectionData.getPrefForEstimate(this.deformationModelDB_DAO.getAseismicSlipEstimate(deformationModelId, faultSectionId)));
				faultModelNames.add(faultSectionPrefData.getSectionName());
				
				// Arraylist of segments of list of sections
				ArrayList sectionList = new ArrayList();
				sectionList.add(faultSectionPrefData);
				ArrayList segmentList = new ArrayList();
				segmentList.add(sectionList);
				faultSegmentMap.put(faultSectionPrefData.getSectionName(), segmentList);
			}
			processSpecialFaults(deformationModelId);
		}
	}
	
	/**
	 * Process special fault sections
	 *
	 */
	private void processSpecialFaults(int deformationModelId) {
		for(int i=0; i<specialFaults.size(); ++i) {
			int[] ids = (int[])specialFaults.get(i);
			ArrayList sectionList = new ArrayList();
			for(int j=0; j<ids.length; ++j) {
				int faultSectionId = ids[j];
				FaultSectionPrefData faultSectionPrefData = prefFaultSectionDAO.getFaultSectionPrefData(faultSectionId);
				//if(!faultSectionPrefData.getSectionName().equalsIgnoreCase("Green Valley (No)")) continue;	
				faultSectionPrefData.setAveLongTermSlipRate(FaultSectionData.getPrefForEstimate(this.deformationModelDB_DAO.getSlipRateEstimate(deformationModelId, faultSectionId)));
				// add to B type faults only if slip is not 0 and not NaN
				if(faultSectionPrefData.getAveLongTermSlipRate()==0.0 || Double.isNaN(faultSectionPrefData.getAveLongTermSlipRate())) continue;
				faultSectionPrefData.setAseismicSlipFactor(FaultSectionData.getPrefForEstimate(this.deformationModelDB_DAO.getAseismicSlipEstimate(deformationModelId, faultSectionId)));
				sectionList.add(faultSectionPrefData);
			}
			ArrayList segmentList = new ArrayList();
			segmentList.add(sectionList);
			faultModelNames.add(specialFaultNames[i]);
			faultSegmentMap.put(specialFaultNames[i], segmentList);
		}
	}
	
	/**
	 * Get a list of all segment names
	 * @return
	 */
	public ArrayList getAllFaultNames(int deformationModelId) {
		this.generateBFaults(deformationModelId);
		return this.faultModelNames;
	}
	
	/**
	 * This returns a list of FaultSegmentData object for all the Type A faults
	 * @param deformationModelId
	 * @param isAseisReducesArea
	 * @return
	 */
	public ArrayList getFaultSegmentDataList(int deformationModelId, boolean isAseisReducesArea) {
		this.generateBFaults(deformationModelId);
		ArrayList faultList = new ArrayList();
		for(int i=0; i< faultModelNames.size(); ++i)
			faultList.add(getFaultSegmentData((String)faultModelNames.get(i), deformationModelId, isAseisReducesArea));
		return faultList;
	}
	
	/**
	 * 
	 * @param faultModel
	 * @param deformationModelId
	 * @param isAseisReducesArea
	 * @return
	 */
	public FaultSegmentData getFaultSegmentData(String faultModel, int deformationModelId,
			boolean isAseisReducesArea) {
		this.generateBFaults(deformationModelId);
		ArrayList segmentList = (ArrayList) this.faultSegmentMap.get(faultModel);
		return  new FaultSegmentData(segmentList, null, isAseisReducesArea, faultModel,
				null);
		
	}
	
	public static void main(String[] args) {
		// def model ids from 42-49, 61 - 68
		B_FaultsFetcher b = new B_FaultsFetcher();
		ArrayList bFaults = b.getFaultSegmentDataList(42, true);
		for(int i=0; i<bFaults.size(); ++i) {
			FaultSegmentData faultSegmentData = (FaultSegmentData)bFaults.get(i);
			ArrayList faultSectionsList = faultSegmentData.getPrefFaultSectionDataList();
			System.out.print(faultSegmentData.getFaultName()+"\t"+faultSegmentData.getNumSegments()+
					"\t"+faultSectionsList.size()+"\t");
			for(int k=0; k<faultSectionsList.size(); ++k)
				System.out.print(((FaultSectionPrefData)faultSectionsList.get(k)).getSectionId()+",");
			System.out.println("");
		}
		System.out.println("Number of B faults="+bFaults.size());
	}
}
