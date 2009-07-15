/**
 * 
 */
package org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_2.rupCalc;

import java.util.ArrayList;

import org.opensha.refFaultParamDb.dao.db.DB_AccessAPI;
import org.opensha.refFaultParamDb.dao.db.DeformationModelPrefDataDB_DAO;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;

/**
 * This class finds the subsections for S. San Andreas fault 
 * 
 * @author vipingupta
 *
 */
public class SoSAF_SubSections {
	private final static int MAX_SUBSECTION_LEN = 10;
	private DeformationModelPrefDataDB_DAO deformationModelPrefDB_DAO = new DeformationModelPrefDataDB_DAO(DB_AccessAPI.dbConnection);
	private ArrayList<FaultSectionPrefData> subSectionList;
	
	public SoSAF_SubSections() {
		
		/**
		 * D2.1 = 82
		 * D2.2 = 83
		 * D2.3 = 84
		 * D2.4 = 85
		 * D2.5 = 86
		 * D2.6 = 87
		 */
		int deformationModelId = 82; //
		
		/*32:San Andreas (Parkfield)
		285:San Andreas (Cholame) rev
		300:San Andreas (Carrizo) rev
		287:San Andreas (Big Bend)
		286:San Andreas (Mojave N)
		301:San Andreas (Mojave S)
		282:San Andreas (San Bernardino N)
		283:San Andreas (San Bernardino S)
		284:San Andreas (San Gorgonio Pass-Garnet HIll)
		295:San Andreas (Coachella) rev*/
		ArrayList<Integer> faultSectionIds = new ArrayList<Integer>();
		faultSectionIds.add(32);
		faultSectionIds.add(285);
		faultSectionIds.add(300);
		faultSectionIds.add(287);
		faultSectionIds.add(286);
		faultSectionIds.add(301);
		faultSectionIds.add(282);
		faultSectionIds.add(283);
		faultSectionIds.add(284);
		faultSectionIds.add(295);
		
		subSectionList = new ArrayList<FaultSectionPrefData>();		
		for(int i=0; i<faultSectionIds.size(); ++i) {
			FaultSectionPrefData faultSectionPrefData = 
				deformationModelPrefDB_DAO.getFaultSectionPrefData(deformationModelId, faultSectionIds.get(i));
			subSectionList.addAll(faultSectionPrefData.getSubSectionsList(this.MAX_SUBSECTION_LEN));
		}
	}
	
	/**
	 * Get a list of all subsections
	 * 
	 * @return
	 */
	public ArrayList<FaultSectionPrefData> getAllSubsections() {
		return this.subSectionList;
	}
	
	/**
	 * It gets all the subsections for SoSAF and prints them on console
	 * @param args
	 */
	public static void main(String []args) {
		SoSAF_SubSections soSAF_SubSections = new  SoSAF_SubSections();
		ArrayList<FaultSectionPrefData> subsectionList = soSAF_SubSections.getAllSubsections();
		for(int i=0; i<subsectionList.size(); ++i) {
			FaultSectionPrefData subSection = subsectionList.get(i);
			System.out.println("-----"+subSection.getSectionName()+"------");
			System.out.println(subSection.getFaultTrace());
		}
	}

}
