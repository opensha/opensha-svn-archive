package org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_3.analysis;

import java.util.ArrayList;
import java.util.HashMap;

import org.opensha.refFaultParamDb.dao.db.DB_AccessAPI;
import org.opensha.refFaultParamDb.dao.db.DeformationModelPrefDataDB_DAO;
import org.opensha.refFaultParamDb.dao.db.DeformationModelSummaryDB_DAO;
import org.opensha.refFaultParamDb.dao.db.FaultSectionVer2_DB_DAO;
import org.opensha.refFaultParamDb.dao.db.PrefFaultSectionDataDB_DAO;
import org.opensha.refFaultParamDb.vo.DeformationModelSummary;
import org.opensha.refFaultParamDb.vo.FaultSectionData;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;



/**
 * 
 * @author vipingupta
 * It compares the preferred values (slip rate, slip rate std dev, aseismic slip factor)
 *  in fault section databse with values in deformation model. 
 *  If the values differ for nay fault section
 * , that section name and deformation model name is printed
 *
 */
public class CheckFaultSectionDatabase {
	public static void main(String args[]) {
		// slip rates and aseismic slip factors from fault section database
		HashMap<String, Double> prefFaultSectionSlipRates = new HashMap<String, Double>();
		HashMap<String, Double> prefFaultSectionSlipRateStdDevs = new HashMap<String, Double>();
		HashMap<String, Double> prefFaultSectionAseisFactors = new HashMap<String, Double>();
	
		// Get Values from fault section database
		FaultSectionVer2_DB_DAO faultSectionDAO = new FaultSectionVer2_DB_DAO(DB_AccessAPI.dbConnection);
		ArrayList<FaultSectionData> faultSectionsList =  faultSectionDAO.getAllFaultSections();
		for(int i=0; i<faultSectionsList.size(); ++i) {
			FaultSectionPrefData faultSectionPrefData  = faultSectionsList.get(i).getFaultSectionPrefData();
			prefFaultSectionSlipRates.put(faultSectionPrefData.getSectionName(), faultSectionPrefData.getAveLongTermSlipRate());
			prefFaultSectionAseisFactors.put(faultSectionPrefData.getSectionName(), faultSectionPrefData.getAseismicSlipFactor());
			prefFaultSectionSlipRateStdDevs.put(faultSectionPrefData.getSectionName(), faultSectionPrefData.getSlipRateStdDev());
		}
		
		// Deformation models
		ArrayList<String> defModels = new ArrayList<String>();
		defModels.add("D2.1");
		defModels.add("D2.2");
		defModels.add("D2.3");
		defModels.add("D2.4");
		defModels.add("D2.5");
		defModels.add("D2.6");
		DeformationModelSummaryDB_DAO defModelSummaryDAO = new DeformationModelSummaryDB_DAO(DB_AccessAPI.dbConnection);
		DeformationModelPrefDataDB_DAO defModelPrefDAO = new DeformationModelPrefDataDB_DAO(DB_AccessAPI.dbConnection);
		for(int i=0; i<defModels.size(); ++i) {
			String defModelName = defModels.get(i);
			DeformationModelSummary defModelSummary = defModelSummaryDAO.getDeformationModel(defModelName);
			int defModelId = defModelSummary.getDeformationModelId();
			ArrayList<Integer> faultSectionsIdList  = defModelPrefDAO.getFaultSectionIdsForDeformationModel(defModelId);
			for(int j=0; j<faultSectionsIdList.size(); ++j) {
				FaultSectionPrefData defModelData = defModelPrefDAO.getFaultSectionPrefData(defModelId, faultSectionsIdList.get(j));
				String faultName = defModelData.getSectionName();
				double origSlipRate = prefFaultSectionSlipRates.get(faultName);
				double origAseisFactor = prefFaultSectionAseisFactors.get(faultName);
				double origSlipRateStdDev = prefFaultSectionSlipRateStdDevs.get(faultName);
				
				if( (!Double.isNaN(defModelData.getAveLongTermSlipRate()) || 
						!Double.isNaN(origSlipRate)) &&
						Math.abs(defModelData.getAveLongTermSlipRate()-origSlipRate)>1e-6) {
					System.out.println(faultName+"\tOrigSlipRate\t"+origSlipRate+"\t"+defModelName+"\t"+defModelData.getAveLongTermSlipRate());
				}
				
				if((!Double.isNaN(defModelData.getSlipRateStdDev()) || 
						!Double.isNaN(origSlipRateStdDev)) &&
						Math.abs(defModelData.getSlipRateStdDev()-origSlipRateStdDev)>1e-6) {
					System.out.println(faultName+"\tOrigSlipStdDev\t"+origSlipRateStdDev+"\t"+defModelName+"\t"+defModelData.getSlipRateStdDev());
				}
				
				if((!Double.isNaN(defModelData.getAseismicSlipFactor()) || 
						!Double.isNaN(origAseisFactor)) &&
						Math.abs(defModelData.getAseismicSlipFactor()-origAseisFactor)>1e-6) {
					System.out.println(faultName+"\tOrigAseisSlipFactor\t"+origAseisFactor+"\t"+defModelName+"\t"+defModelData.getAseismicSlipFactor());
				}
			}
		}
			
	}
}
