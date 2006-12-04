/**
 * 
 */
package org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_1.data;

import java.util.ArrayList;
import java.util.HashMap;

import org.opensha.refFaultParamDb.dao.db.DB_AccessAPI;
import org.opensha.refFaultParamDb.dao.db.PrefFaultSectionDataDB_DAO;
import org.opensha.refFaultParamDb.vo.DeformationModelSummary;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_1.FaultSegmentData;

/**
 * @author vipingupta
 * This class generates a list of B faults (faults which are not A faults and which have non zero slip
 * rate in deformation model) 
 */
public  class B_FaultsFetcher extends FaultsFetcher {
	private A_FaultsFetcher aFaultsFetcher=null;
	private ArrayList bFaultNames; 
	private ArrayList bFaultIds;
	private HashMap faultSegmentMap;
	
	// This holds the special, multi-section B Faults
	private ArrayList allSpecialFaultIds;

	private final static String B_CONNECT_MINIMAL = "org/opensha/sha/earthquake/rupForecastImpl/WGCEP_UCERF_2_1/data/B_FaultConnectionsMinimum.txt";
	private final static String B_CONNECT_MODEL1 = "org/opensha/sha/earthquake/rupForecastImpl/WGCEP_UCERF_2_1/data/B_FaultConnectionsF2.1.txt";
	private final static String B_CONNECT_MODEL2 = "org/opensha/sha/earthquake/rupForecastImpl/WGCEP_UCERF_2_1/data/B_FaultConnectionsF2.2.txt";
		
	/**
	 * default constructor
	 *
	 */
	public B_FaultsFetcher() {
		// cache the PrefFaultSectionData
		 PrefFaultSectionDataDB_DAO faultSectionPrefDAO = new PrefFaultSectionDataDB_DAO(DB_AccessAPI.dbConnection);
		 faultSectionPrefDAO.getAllFaultSectionPrefData();
	}
	
	/**
	 * Set the deformation model. This function needs to be called before any other function can be called.
	 * @param fileName
	 */
	public void setDeformationModel(boolean isConnected, DeformationModelSummary defModelSummary, A_FaultsFetcher aFaultsFetcher) {
		deformationModelId = defModelSummary.getDeformationModelId();
		String fileName=null;
		if(!isConnected)  { // if we do not have to connect B-Fsults
			fileName = B_CONNECT_MINIMAL;
		} else { // if B-Faults need to be connected
			String faultModelName = defModelSummary.getFaultModel().getFaultModelName();
			// get the B-Fault filename based on selected fault model
			if(faultModelName.equalsIgnoreCase("F2.1")) fileName = B_CONNECT_MODEL1;
			else if((faultModelName.equalsIgnoreCase("F2.2"))) fileName = B_CONNECT_MODEL2;
			else throw new RuntimeException("Unsupported Fault Model");
		}
		this.aFaultsFetcher = aFaultsFetcher;	
		this.loadSegmentModels(fileName);
		allSpecialFaultIds = super.getAllFaultSectionsIdList();
		generateBFaults();
		
	}
	
	/**
	 * Get PrefFaultSectionData for B faults
	 * @param deformationModelId
	 * @return
	 */
	private void generateBFaults() {	
		faultSegmentMap = new HashMap();
		bFaultNames = new ArrayList();
		bFaultIds = new ArrayList();
		ArrayList faultSectionsInDefModel = deformationModelPrefDB_DAO.getFaultSectionIdsForDeformationModel(this.deformationModelId);
		ArrayList aFaultsList = this.aFaultsFetcher.getAllFaultSectionsIdList(); 
		for(int i=0; i<faultSectionsInDefModel.size(); ++i) {
			// if this is A type fault or a special fault, then do not process it
			if(aFaultsList.contains(faultSectionsInDefModel.get(i)) ||
					allSpecialFaultIds.contains(faultSectionsInDefModel.get(i))	) {
				//System.out.println(faultSectionId+" is A type fault");
				continue;
			}
			int faultSectionId = ((Integer)faultSectionsInDefModel.get(i)).intValue();
			FaultSectionPrefData faultSectionPrefData = deformationModelPrefDB_DAO.getFaultSectionPrefData(this.deformationModelId, faultSectionId);
			// add to B type faults only if slip is not 0 and not NaN
			if(faultSectionPrefData.getAveLongTermSlipRate()==0.0 || Double.isNaN(faultSectionPrefData.getAveLongTermSlipRate())) continue;
			bFaultNames.add(faultSectionPrefData.getSectionName());
			bFaultIds.add(new Integer(faultSectionPrefData.getSectionId()));
			// Arraylist of segments of list of sections
			ArrayList sectionList = new ArrayList();
			sectionList.add(faultSectionPrefData);
			ArrayList segmentList = new ArrayList();
			segmentList.add(sectionList);
			faultSegmentMap.put(faultSectionPrefData.getSectionName(), segmentList);
		}
		bFaultNames.addAll(this.getAllFaultNames()); // add connecting fault names
		bFaultIds.addAll(super.getAllFaultSectionsIdList());
		
	}
	
	/**
	 * Return a list of ids of all fault sections in any of faults defined in text file.
	 * @return
	 */
	public ArrayList getAllFaultSectionsIdList() {
		return bFaultIds;
	}
	
	/**
	 * Return a list of Ids of connected B-Type fault sections
	 */
	public ArrayList getConnectedFaultSectionsIdList() {
		return super.getAllFaultSectionsIdList();
	}
	
	/**
	 * Get a list of all segment names
	 * @return
	 */
	public ArrayList<String> getAllFaultNames() {
		return this.bFaultNames;
	}
	
	/**
	 * This returns a list of FaultSegmentData object for all the Type A faults
	 * @param deformationModelId
	 * @param isAseisReducesArea
	 * @return
	 */
	public ArrayList getFaultSegmentDataList(boolean isAseisReducesArea) {
		ArrayList faultList = new ArrayList();
		for(int i=0; i< bFaultNames.size(); ++i)
			faultList.add(getFaultSegmentData((String)bFaultNames.get(i), isAseisReducesArea));
		return faultList;
	}
	
	/**
	 * Get recurrence intervals for selected segment model
	 * @param selectedSegmentModel
	 * @return
	 */
	public  ArrayList<SegRateConstraint> getSegRateConstraints(String selectedSegmentModel) {
		return null;
	}
	
	/**
	 * 
	 * @param faultModel
	 * @param deformationModelId
	 * @param isAseisReducesArea
	 * @return
	 */
	public FaultSegmentData getFaultSegmentData(String faultModel,
			boolean isAseisReducesArea) {
		
		ArrayList segmentList = (ArrayList) this.faultSegmentMap.get(faultModel);
		if(segmentList!=null) {
			return  new FaultSegmentData(segmentList, null, isAseisReducesArea, faultModel,
					null);
		} else {
			 // if it is a part of connecting B-faults
			FaultSegmentData faultSegmentData =  super.getFaultSegmentData(faultModel, isAseisReducesArea);
			/*ArrayList sectionList = faultSegmentData.getPrefFaultSectionDataList();
			for(int i=0; i<sectionList.size(); ++i) 
				System.out.print(((FaultSectionPrefData)sectionList.get(i)).getSectionName()+",");
			System.out.println("\n");*/
			return faultSegmentData;
		}
		
	}
	
	public static void main(String[] args) {
		// def model ids from 42-49, 61 - 68
		B_FaultsFetcher b = new B_FaultsFetcher();
		ArrayList bFaults = b.getFaultSegmentDataList( true);
		ArrayList<FaultSectionPrefData> preFaultSectionDataList = new ArrayList<FaultSectionPrefData>();
		
		for(int i=0; i<bFaults.size(); ++i) {
			FaultSegmentData faultSegmentData = (FaultSegmentData)bFaults.get(i);
			//ArrayList faultSectionsList = faultSegmentData.getPrefFaultSectionDataList();
			preFaultSectionDataList.addAll(faultSegmentData.getPrefFaultSectionDataList());
			//System.out.print(faultSegmentData.getFaultName()+"\t"+faultSegmentData.getNumSegments()+
			//		"\t"+faultSectionsList.size()+"\t");
			//for(int k=0; k<faultSectionsList.size(); ++k)
				//System.out.print(((FaultSectionPrefData)faultSectionsList.get(k)).getSectionId()+",");
			//System.out.println("");
		}
		
		/*try {
			FileWriter fw = new FileWriter("B_FaultDistances.txt");
			double minDist, distance;
			for(int i=0; i<preFaultSectionDataList.size(); ++i) {
				FaultTrace faultTrace1 = preFaultSectionDataList.get(i).getFaultTrace();
				
				for(int j=i+1; j<preFaultSectionDataList.size(); ++j) {
					FaultTrace faultTrace2 = preFaultSectionDataList.get(j).getFaultTrace();
					minDist = RelativeLocation.getApproxHorzDistance(faultTrace1.getLocationAt(0), faultTrace2.getLocationAt(0));
					distance = RelativeLocation.getApproxHorzDistance(faultTrace1.getLocationAt(0), faultTrace2.getLocationAt(faultTrace2.getNumLocations()-1));
					if(distance<minDist) minDist = distance;
					distance = RelativeLocation.getApproxHorzDistance(faultTrace1.getLocationAt(faultTrace1.getNumLocations()-1), faultTrace2.getLocationAt(0));
					if(distance<minDist) minDist = distance;
					distance = RelativeLocation.getApproxHorzDistance(faultTrace1.getLocationAt(faultTrace1.getNumLocations()-1), faultTrace2.getLocationAt(faultTrace2.getNumLocations()-1));
					if(distance<minDist) minDist = distance;
					fw.write(preFaultSectionDataList.get(i).getSectionName()+";"+
							preFaultSectionDataList.get(j).getSectionName()+";"+
							minDist+"\n");
				}
			}
			fw.close();
		}catch(Exception e) {
			e.printStackTrace();
		}*/
		//System.out.println("Number of B faults="+bFaults.size());
	}
}
