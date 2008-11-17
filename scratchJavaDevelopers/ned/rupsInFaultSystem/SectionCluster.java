package scratchJavaDevelopers.ned.rupsInFaultSystem;

import java.util.ArrayList;
import java.util.HashMap;

import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;


/**
 * TO DO
 * 
 * The equals() method of MultipleSectionRupIDs will break down if there is a multi-path for the 
 * rupture with the same endpoints and num sections involved.
 * @author field
 *
 */
public class SectionCluster {
	
	ArrayList<ArrayList<Integer>> localEndToEndSectLinksList;
	ArrayList<Integer> allSectEndPtsInCluster;
	ArrayList<ArrayList<FaultSectionPrefData>> subSectionPrefDataList;
	ArrayList<Integer> allSubSectionsIdList;
	ArrayList<MultipleSectionRupIDs> rupList;
	int minNumSubSectInRup;
	
	/**
	 * 
	 * @return
	 */
	public ArrayList<Integer> getSectionIndicesInCluster() {
		ArrayList<Integer> sectIndices = new ArrayList<Integer>();
		for(int i=0; i<localEndToEndSectLinksList.size();i++) {
			ArrayList<Integer> endToEndLink = localEndToEndSectLinksList.get(i);
//			System.out.println("endToEndLink="+endToEndLink);
			for(int j=0; j< endToEndLink.size(); j++) {
				Integer linkInt = endToEndLink.get(j);
				if(!sectIndices.contains(linkInt)) // keep out duplicates
					sectIndices.add(linkInt);
			}
		}
//		System.out.println("sectIndices="+sectIndices);
		return sectIndices;
	}
	
	
	/**
	 * 
	 * @param endToEndSectLinksList
	 * @return
	 */
	public ArrayList<ArrayList<Integer>> CreateCluster(ArrayList<ArrayList<Integer>> endToEndSectLinksList, 
			ArrayList<ArrayList<FaultSectionPrefData>> subSectionPrefDataList, int minNumSubSectInRup) {
		
		this.subSectionPrefDataList = subSectionPrefDataList;
		this.minNumSubSectInRup = minNumSubSectInRup;
		
		// make hashmap of endToEndSectLinksList to make it easier to remove those used from what's passed back
		HashMap endToEndLinkdListHashMap = new HashMap();
		for(int i=0;i<endToEndSectLinksList.size();i++)
			endToEndLinkdListHashMap.put(new Integer(i), endToEndSectLinksList.get(i));
		
		allSectEndPtsInCluster = new ArrayList<Integer>();
		
		ArrayList<Integer> linkIndicesToKeep = new ArrayList<Integer>();
		
		// add the first endToEndLink
		allSectEndPtsInCluster.addAll(endToEndSectLinksList.get(0));
		linkIndicesToKeep.add(0);
		
		// now find the indices of the others
		for(int i=1; i<endToEndSectLinksList.size(); i++) { // need double loop to make all connections
//System.out.println("\tlink "+i+" of "+endToEndSectLinksList.size());
			for(int j=1; j<endToEndSectLinksList.size(); j++) {
				ArrayList<Integer> endToEndLink = endToEndSectLinksList.get(j);
				for(int k=0; k<endToEndLink.size(); k++) {
					if(allSectEndPtsInCluster.contains(endToEndLink.get(k))) { // it's part of this cluster
						if(!linkIndicesToKeep.contains(j)) {  // make sure we don't already have it
							linkIndicesToKeep.add(j);
							for(int l=0;l<endToEndLink.size();l++) {
								Integer linkInt = endToEndLink.get(l);
								if(!allSectEndPtsInCluster.contains(linkInt)) // add it only if it's not already there
									allSectEndPtsInCluster.add(linkInt);								
							}
						}
						break;  // exit loop over endToEndLink Integers
					}
				}
			}	
		}
		
		localEndToEndSectLinksList = new ArrayList<ArrayList<Integer>>();
		for(int i=0; i<linkIndicesToKeep.size();i++)
			localEndToEndSectLinksList.add(endToEndSectLinksList.get(linkIndicesToKeep.get(i).intValue()));
//		System.out.println("localEndToEndSectLinksList.size()="+localEndToEndSectLinksList.size());
		
		ArrayList<ArrayList<Integer>> unusedEndToEndSectLinksList = new ArrayList<ArrayList<Integer>>();
		for(int i=1; i<endToEndSectLinksList.size();i++)
			if(!linkIndicesToKeep.contains(i))
				unusedEndToEndSectLinksList.add((ArrayList<Integer>)endToEndLinkdListHashMap.get(new Integer(i)));
		
		// compute subsection data
		computeSubsectionData();
		
		return unusedEndToEndSectLinksList;
	}
	
	/**
	 * This returns the number of subsections in the cluster
	 * @return
	 */
	public int getNumSubSections() {
		return allSubSectionsIdList.size();
	}
	
	/**
	 * This returns the IDs of all the subsections in the cluster
	 * @return
	 */
	public ArrayList<Integer> getAllSubSectionsIdList() {
		return allSubSectionsIdList;
	}
	
	
	private void computeSubsectionData() {
		allSubSectionsIdList = new ArrayList<Integer>();
		int sectionIndex;
		for(int i=0; i< allSectEndPtsInCluster.size();i+=2) {
			sectionIndex = allSectEndPtsInCluster.get(i).intValue()/2; // convert from endpoint index to section index
//			System.out.println("sectionIndex="+sectionIndex);
			ArrayList<FaultSectionPrefData> prefSubsectDataForSection = subSectionPrefDataList.get(sectionIndex);
			for(int k=0; k< prefSubsectDataForSection.size();k++) {
//				System.out.println(prefSubsectDataForSection.get(k).getSectionId());
				allSubSectionsIdList.add(prefSubsectDataForSection.get(k).getSectionId());
			}
		}
	}
	
	public ArrayList<MultipleSectionRupIDs> getRuptures() {
		  if(rupList== null)
			  computeRupList();
		  return rupList;
	}

	private void computeRupList() {
		rupList = new ArrayList<MultipleSectionRupIDs>();
		// loop over each end-to-end link list
		for(int l=0; l<localEndToEndSectLinksList.size();l++) {
System.out.println("\tWorking on Rups for link "+l+" of "+localEndToEndSectLinksList.size());
			// get the list of subsection IDs for this end-to-end link
			ArrayList<Integer> endToEndLink = localEndToEndSectLinksList.get(l);
			ArrayList<Integer> endToEndSubsectionIDs = new ArrayList<Integer>();
			int sectionIndex;
			for(int i=0; i< endToEndLink.size();i+=2) {
				sectionIndex = endToEndLink.get(i).intValue()/2; // convert from endpoint index to section index
				ArrayList<FaultSectionPrefData> prefSubsectDataForSection = subSectionPrefDataList.get(sectionIndex);
				for(int k=0; k< prefSubsectDataForSection.size();k++) {
					endToEndSubsectionIDs.add(prefSubsectDataForSection.get(k).getSectionId());
				}
			}
			// now create each MultipleSectionRupIDs object
			for(int numSubSectInRup=minNumSubSectInRup;numSubSectInRup<=endToEndSubsectionIDs.size();numSubSectInRup++) {
				for(int s=0;s<endToEndSubsectionIDs.size()-numSubSectInRup+1;s++) {
//					if(l==0) System.out.println("s="+s+"\tnumSubSectInRup="+numSubSectInRup);
					ArrayList<Integer> id_list = new ArrayList<Integer>();
					for(int t=0;t<numSubSectInRup;t++) id_list.add(endToEndSubsectionIDs.get(s+t));
					MultipleSectionRupIDs rupIDs = new MultipleSectionRupIDs(id_list);
					if(!rupList.contains(rupIDs)) rupList.add(rupIDs);
					
				}
			}
/*
			int predNum = endToEndSubsectionIDs.size()*(endToEndSubsectionIDs.size()+1)/2;
			if(l==0) {
				System.out.println("localEndToEndSectLinksList.size()="+localEndToEndSectLinksList.size());
				System.out.println("endToEndLink="+endToEndLink);
				System.out.println("endToEndSubsectionIDs="+endToEndSubsectionIDs);
				System.out.println("endToEndSubsectionIDs.size()="+endToEndSubsectionIDs.size());
				System.out.println("numRup = "+rupList.size()+"\t"+predNum);  // only check the first since none for this are duplicates
			}
*/
		}
	}
	
	
}