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
	ArrayList<ArrayList<Integer>> subSectionConnectionsListList;
	ArrayList<ArrayList<Integer>> rupListIndices;			// elements here are subsection IDs
	int minNumSubSectInRup;
	
	public SectionCluster(ArrayList<FaultSectionPrefData> subSectionPrefDataList, int minNumSubSectInRup, 
			ArrayList<ArrayList<Integer>> subSectionConnectionsListList) {
		this.minNumSubSectInRup = minNumSubSectInRup;
		this.subSectionPrefDataList = subSectionPrefDataList;
		this.subSectionConnectionsListList = subSectionConnectionsListList;
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
	
	
	public int getNumRuptures() {
		if(rupListIndices== null)  computeRupList();
		return rupListIndices.size();
	}
	
		
	public ArrayList<ArrayList<Integer>> getRuptures() {
		return new ArrayList<ArrayList<Integer>>();
		/*
		  if(rupListIndices== null)  computeRupList();
		  // now convert to holding subsection IDs
		  ArrayList<ArrayList<Integer>> rupList = new ArrayList<ArrayList<Integer>>();
		  for(int i=0;i<rupListIndices.size();i++) {
			  ArrayList<Integer> rup = rupListIndices.get(i);
			  ArrayList<Integer> newRup  = new ArrayList<Integer>();
			  for(int j=0;j<rup.size();j++)
				  newRup.add(subSectionPrefDataList.get(rup.get(j)).getSectionId());
			  rupList.add(newRup);
		  }
		  return rupList;
		  */
	}
	
	public ArrayList<ArrayList<Integer>> getRupturesByIndices() {
		  if(rupListIndices== null)
			  computeRupList();
		  return rupListIndices;
	}

	  
	  private void addRuptures(int subSectIndex,int lastSubSect, ArrayList<Integer> list) {
		  ArrayList<Integer> branches = subSectionConnectionsListList.get(subSectIndex);
		  for(int i=0; i<branches.size(); i++) {
			  Integer newSubSect = branches.get(i);
			  if(!list.contains(newSubSect) && newSubSect != lastSubSect) {	// avoid looping back on self or previous subsect
				  ArrayList<Integer> newList = (ArrayList<Integer>)list.clone();
				  newList.add(newSubSect);
				  if(newList.size() >= minNumSubSectInRup)  {// it's a rupture
					  rupListIndices.add(newList);
//					  System.out.println(newList);
				  }
				  addRuptures(newSubSect,subSectIndex,newList);
//				  System.out.println("\tadded "+this.subSectionPrefDataList.get(subSect).getName());
			  }
		  }
	  }

	
	private void computeRupList() {
//		System.out.println("Cluster: "+this);
		rupListIndices = new ArrayList<ArrayList<Integer>>();
		// loop over every subsection as the first in the rupture
		int progress = 0;
		int progressIncrement = 5;
		System.out.print("% Done:\t");

		for(int s=0;s<size();s++) {
			// show progress
			if(s*100/size() > progress) {
				System.out.print(progress+"\t");
				progress += progressIncrement;
			}
			ArrayList<Integer> subSectList = new ArrayList<Integer>();
			int subSectIndex = get(s);
			subSectList.add(subSectIndex);
			addRuptures(subSectIndex, -1, subSectList);
//			System.out.println(rupList.size()+" ruptures after subsection "+s);
		}
		System.out.print("\n");

		// now filter out duplicates & change from containing indices to IDs
		ArrayList<ArrayList<Integer>> newRupList = new ArrayList<ArrayList<Integer>>();
		for(int r=0; r< rupListIndices.size();r++) {
			ArrayList<Integer> rup = rupListIndices.get(r);
			ArrayList<Integer> reverseRup = new ArrayList<Integer>();
			for(int i=rup.size()-1;i>=0;i--) reverseRup.add(rup.get(i));
			if(!newRupList.contains(reverseRup)) { // keep if we don't already have
				newRupList.add(rup);
			}
		}
		rupListIndices = newRupList;
	}
}