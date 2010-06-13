package scratch.ned.rupsInFaultSystem;

import java.util.ArrayList;
import java.util.HashMap;

import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;

/**
 * @author field
 *
 */
public class SectionCluster extends ArrayList<Integer> {
	
	ArrayList<FaultSectionPrefData> subSectionPrefDataList;
	ArrayList<Integer> allSubSectionsIdList = null;
	ArrayList<ArrayList<Integer>> rupList;
	int minNumSubSectInRup;
	
	public SectionCluster(ArrayList<FaultSectionPrefData> subSectionPrefDataList, int minNumSubSectInRup) {
		this.minNumSubSectInRup = minNumSubSectInRup;
		this.subSectionPrefDataList = subSectionPrefDataList;
	}
	
	
	/**
	 * This returns the number of subsections in the cluster
	 * @return
	 */
	public int getNumSubSections() {
		return this.size();
	}
	
	
	/**
	 * This returns the IDs of all the subsections in the cluster
	 * @return
	 */
	public ArrayList<Integer> getAllSubSectionsIdList() {
		if(allSubSectionsIdList==null) computeAllSubSectionsIdList();
		return allSubSectionsIdList;
	}
	
	
	private void computeAllSubSectionsIdList() {
		allSubSectionsIdList = new ArrayList<Integer>();
		for(int i=0; i<size();i++) allSubSectionsIdList.add(subSectionPrefDataList.get(get(i)).getSectionId());
	}
	
		
	public ArrayList<ArrayList<Integer>> getRuptures() {
		  if(rupList== null)
			  computeRupList();
		  return rupList;
	}

	
	
	private void computeRupList() {
		rupList = new ArrayList<ArrayList<Integer>>();
	}
	
	
}