/**
 * 
 */
package org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.data.finalReferenceFaultParamDb;

import java.util.ArrayList;
import java.util.HashMap;

import org.opensha.refFaultParamDb.dao.db.DB_AccessAPI;
import org.opensha.refFaultParamDb.dao.db.DeformationModelPrefDataDB_DAO;
import org.opensha.refFaultParamDb.vo.DeformationModelSummary;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;

/**
 * 
 * This provides static access to the final preferred data for each deformation model
 * (this does not access the oracle database dynamically, except for the one-time creation
 * of the static data files).
 * 
 * TO DO: need to implement methods that saves the data in the HashMaps 
 * (slipRateMap;aseismicSlipMap;stdDevMap;faultSectionIdIndexMapMap;faultSectionIdMap)
 * to a static file (ascii or XML), which will be run only once to create the files,
 * and then another to read the HashMap data back in from those files (which the constructor
 * will use).
 * 
 * @author Ned Field
 *
 */
public class DeformationModelPrefDataFinal {
	
	/*
	 * For each deformation model we need to store a faultSectionIdList and arrays of the following:
	 * slipRate, slipRateStdDev, aseismicSlip
	 */
	
	// these will store the data for each deformation model
	private static HashMap slipRateMap;
	private static HashMap aseismicSlipMap;
	private static HashMap stdDevMap;
	private static HashMap faultSectionIdIndexMapMap; // a map of maps (the array index for each Id, for each def model)
	private static HashMap faultSectionIdMap; // contains Array list of fault sections Ids for each def model
	
	private PrefFaultSectionDataFinal prefFaultSectionDataFinal;
	
	DeformationModelSummaryFinal deformationModelSummaryFinal; // keep copy of this for accessing more info about def models
	
	public DeformationModelPrefDataFinal() {
		readDataFromDatabase();
	}
	
	
	private void readDataFromDatabase() {

		// need one of these for each deformation model
		HashMap faultSectionIdIndexMap;
		double[] slipRateList, slipRateStdDevList, aseismicSlipList;
		
		// these are where they are stored
		slipRateMap = new HashMap();
		aseismicSlipMap = new HashMap();
		stdDevMap = new HashMap();
		faultSectionIdIndexMapMap = new HashMap();
		faultSectionIdMap = new HashMap();
		
		DeformationModelPrefDataDB_DAO deformationModelPrefDB_DAO = new DeformationModelPrefDataDB_DAO(DB_AccessAPI.dbConnection);
		deformationModelSummaryFinal = new DeformationModelSummaryFinal();
		ArrayList<DeformationModelSummary> deformationModelSummaryList = deformationModelSummaryFinal.getAllDeformationModels();
		for(int i=0; i<deformationModelSummaryList.size();i++) {
			DeformationModelSummary dmSummary = deformationModelSummaryList.get(i);
//			System.out.println(dmSummary.getDeformationModelName()+",  "+dmSummary.getDeformationModelId());
			int defModId = dmSummary.getDeformationModelId();
			ArrayList faultSectionIdList = deformationModelPrefDB_DAO.getFaultSectionIdsForDeformationModel(defModId);
			faultSectionIdIndexMap = new HashMap();
			slipRateList = new double[faultSectionIdList.size()];
			slipRateStdDevList = new double[faultSectionIdList.size()];
			aseismicSlipList = new double[faultSectionIdList.size()];
			for(int j=0;j<faultSectionIdList.size();j++) {
				int faultSectionId = ((Integer) faultSectionIdList.get(j)).intValue();
				faultSectionIdIndexMap.put(faultSectionId, new Integer(j));
				slipRateList[j]=deformationModelPrefDB_DAO.getSlipRate(defModId, faultSectionId);
				slipRateStdDevList[j]=deformationModelPrefDB_DAO.getSlipStdDev(defModId, faultSectionId);
				aseismicSlipList[j] = deformationModelPrefDB_DAO.getAseismicSlipFactor(defModId, faultSectionId);
			}
			//now put these in the HashMaps
			slipRateMap.put(defModId, slipRateList);
			stdDevMap.put(defModId, slipRateStdDevList);
			aseismicSlipMap.put(defModId, aseismicSlipList);
			faultSectionIdIndexMapMap.put(defModId, faultSectionIdIndexMap);
			faultSectionIdMap.put(defModId, faultSectionIdList);
		}
	}

	/**
	 * Get Fault Section Pref data for a deformation model ID and Fault section Id
	 * @param deformationModelId
	 * @param faultSectionId
	 * @return
	 */
	public FaultSectionPrefData getFaultSectionPrefData(int deformationModelId, int faultSectionId) {

		// first get the default preferred data
		FaultSectionPrefData faultSectionPrefData = prefFaultSectionDataFinal.getFaultSectionPrefData(faultSectionId);
		
		HashMap faultSectionIdIndexMap = (HashMap) faultSectionIdIndexMapMap.get(deformationModelId);
		int indexForSectId = ((Integer) faultSectionIdIndexMap.get(faultSectionId)).intValue();
		
		double[] slipRateList = (double[]) slipRateMap.get(deformationModelId);
		faultSectionPrefData.setAveLongTermSlipRate(slipRateList[indexForSectId]);

		double[] stdDevList = (double[]) stdDevMap.get(deformationModelId);
		faultSectionPrefData.setSlipRateStdDev(stdDevList[indexForSectId]);

		double[] aseismicSlipList = (double[]) aseismicSlipMap.get(deformationModelId);
		faultSectionPrefData.setAseismicSlipFactor(aseismicSlipList[indexForSectId]);

		return faultSectionPrefData;
	}
	
	/**
	 * Get a list of all fault sections within this deformation model
	 * @param deformationModelId
	 * @return
	 */
	public ArrayList getFaultSectionIdsForDeformationModel(int deformationModelId) {
		return (ArrayList) faultSectionIdMap.get(deformationModelId);
	}

	
	public static void main(String[] args) {
		DeformationModelPrefDataFinal test = new DeformationModelPrefDataFinal();
	}
}
