/**
 * 
 */
package org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.data.finalReferenceFaultParamDb;

import java.util.ArrayList;
import java.util.HashMap;

import org.opensha.refFaultParamDb.dao.db.DB_AccessAPI;
import org.opensha.refFaultParamDb.dao.db.PrefFaultSectionDataDB_DAO;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.opensha.sha.fault.FaultTrace;

/**
 * <p>Title: PrefFaultSectionDataFinal.java </p>
 * <p>Description: This class reads the Preferred Fault Section Data from an XML file.
 * @author Ned Field
 * @version 1.0
 *
 */
public class PrefFaultSectionDataFinal {
	private static ArrayList<FaultSectionPrefData> faultSectionsList;
	private static HashMap indexForID_Map;
	
	public PrefFaultSectionDataFinal() {
		// only need to run this once to create the XML file (then comment it out)
		writeFaultSectionDataFromDatabaseTo_XML();
		readFaultSectionDataFromXML();
		
	}


	private void writeFaultSectionDataFromDatabaseTo_XML() {
		PrefFaultSectionDataDB_DAO faultSectionDAO = new PrefFaultSectionDataDB_DAO(DB_AccessAPI.dbConnection);
		ArrayList faultSectionDataListFromDatabase = faultSectionDAO.getAllFaultSectionPrefData();
		
		// make the index to ID hashmap
		indexForID_Map = new HashMap();
		FaultSectionPrefData fspd;
		for(int i=0; i<faultSectionDataListFromDatabase.size(); i++) {
			fspd = (FaultSectionPrefData) faultSectionDataListFromDatabase.get(i);
			indexForID_Map.put(fspd.getSectionId(), new Integer(i));
//			System.out.println(fspd.getSectionId()+"\t"+fspd.getSectionName());
		}
		
		// save each fault section to an XML file (save all elements that have an associated set method in FaultSectionPrefData) 
		
		// ************* Need to add the code ****************** //
		
		// need the following until the read* method is implemented
		faultSectionsList = faultSectionDataListFromDatabase;
	}
	
	/**
	 * This reads the XML file and populates faultSectionsList 
	 */
	private void readFaultSectionDataFromXML() {
		
	}
	
	/**
	 * Get a list of all Fault Section Pref Data from the database
	 * @return
	 */
	public ArrayList getAllFaultSectionPrefData() {
		return faultSectionsList;
	}
	
	/**
	 * Get Preferred fault section data for a Fault Section Id
	 * @param faultSectionId
	 * @return
	 */
	public FaultSectionPrefData getFaultSectionPrefData(int faultSectionId) {
		int index = ((Integer)indexForID_Map.get(faultSectionId)).intValue();
		return faultSectionsList.get(index);
	}
	
	public static void main(String[] args) {
		PrefFaultSectionDataFinal test = new PrefFaultSectionDataFinal();
		ArrayList junk = test.getAllFaultSectionPrefData();
		FaultSectionPrefData faultSectionPrefData = (FaultSectionPrefData) junk.get(5);
		int id = faultSectionPrefData.getSectionId();
		System.out.println(id);
		FaultSectionPrefData faultSectionPrefData2 = test.getFaultSectionPrefData(id);
		System.out.println(faultSectionPrefData2.getSectionId());
		
	}

}
