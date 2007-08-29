/**
 * 
 */
package org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_3.data;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import org.opensha.refFaultParamDb.dao.db.DB_AccessAPI;
import org.opensha.refFaultParamDb.dao.db.PrefFaultSectionDataDB_DAO;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.opensha.util.FileUtils;

/**
 * It reads the Non-CA faults file to generate a list of non-CA faults.
 * This file is generated from Chris Wills email on Aug 28, 3:48 PM
 * 
 * 
 * @author vipingupta
 *
 */
public class NonCA_FaultsFetcher {
	public static final String FILENAME = "org/opensha/sha/earthquake/rupForecastImpl/WGCEP_UCERF_2_3/data/NonCA_FaultsFromChris.txt";
	private PrefFaultSectionDataDB_DAO prefFaultSectionDataDB_DAO = new PrefFaultSectionDataDB_DAO(DB_AccessAPI.dbConnection);
	private ArrayList<Integer> faultIds;
	
	public NonCA_FaultsFetcher() {
		try {
			// Read the Non-CA faults file
			ArrayList<String> faultNames = FileUtils.loadJarFile(FILENAME);
			faultIds = new ArrayList<Integer>();
			ArrayList<FaultSectionPrefData> prefFaultSectionDataList = prefFaultSectionDataDB_DAO.getAllFaultSectionPrefData();
			int numFaultSections = prefFaultSectionDataList.size();
			for(int i=0; i<numFaultSections; ++i) {
				FaultSectionPrefData faultSectionPrefData = prefFaultSectionDataList.get(i);
				if(faultNames.contains(faultSectionPrefData.getSectionName())) {
					faultIds.add(faultSectionPrefData.getSectionId());
					faultNames.remove(faultSectionPrefData.getSectionName());
				}
			}
			
			for(int i=0; i<faultNames.size(); ++i)
				System.out.println(faultNames.get(i)+ " : Non-CA fault not found in database");
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}
	
	
	/**
	 * Get a list of Ids of all Non-CA faults
	 * 
	 * @return
	 */
	public ArrayList<Integer> getNonCA_FaultIds() {
		return this.faultIds;
	}
	
	public static void main(String args[]) {
		NonCA_FaultsFetcher nonCA_FaultsFetcher = new NonCA_FaultsFetcher();
	}
	
}
