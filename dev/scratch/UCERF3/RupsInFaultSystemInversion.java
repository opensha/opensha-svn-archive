package scratch.UCERF3;

import java.io.FileWriter;
import java.util.ArrayList;
import org.opensha.commons.geo.Location;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;


/**
 * This does the "Grand Inversion" for UCERF3 (or other ERFs)
 * 
 * Important Notes:
 * 
 * 1) If the sections are actually subsections of larger sections, then the method 
 * computeCloseSubSectionsListList() only allows one connection between parent sections
 * (to avoid ruptures jumping back and forth for closely spaced and parallel sections).
 * Is this potentially problematic?
 * 
 * @author field & Page
 *
 */
public class RupsInFaultSystemInversion {

	protected final static boolean D = true;  // for debugging


	ArrayList<FaultSectionPrefData> faultSectionData;
	double sectionDistances[][],sectionAzimuths[][];;
	double maxJumpDist, maxAzimuthChange, maxStrikeDiff, maxRakeDiff;
	int minNumSectInRup;

	String endPointNames[];
	Location endPointLocs[];
	int numSections;
	ArrayList<ArrayList<Integer>> sectionConnectionsListList, endToEndSectLinksList;

	ArrayList<SectionCluster> sectionClusterList;


	/**
	 * 
	 * @param faultSectionData - this assumes subsections (if any) are in proper order (have adjacent indices)
	 * @param sectionDistances
	 * @param subSectionAzimuths
	 * @param maxJumpDist
	 * @param maxAzimuthChange
	 * @param maxTotAzimuthChange
	 * @param minNumSectInRup
	 */
	public RupsInFaultSystemInversion(ArrayList<FaultSectionPrefData> faultSectionData,
			double[][] sectionDistances, double[][] subSectionAzimuths, double maxJumpDist, 
			double maxAzimuthChange, double maxStrikeDiff, double maxRakeDiff, int minNumSectInRup) {

		if(D) System.out.println("Instantiating RupsInFaultSystemInversion");
		this.faultSectionData = faultSectionData;
		this.sectionDistances = sectionDistances;
		this.sectionAzimuths = subSectionAzimuths;
		this.maxJumpDist=maxJumpDist;
		this.maxAzimuthChange=maxAzimuthChange; 
		this.maxStrikeDiff=maxStrikeDiff;
		this.maxRakeDiff=maxRakeDiff;
		this.minNumSectInRup=minNumSectInRup;

		// write out settings if in debug mode
		if(D) System.out.println("faultSectionData.size() = "+faultSectionData.size() +
				"; sectionDistances.length = "+sectionDistances.length +
				"; subSectionAzimuths.length = "+subSectionAzimuths.length +
				"; maxJumpDist = "+maxJumpDist +
				"; maxAzimuthChange = "+maxAzimuthChange + 
				"; maxStrikeDiff = "+maxStrikeDiff +
				"; maxRakeDiff = "+maxRakeDiff +
				"; minNumSectInRup = "+minNumSectInRup);

		// check that indices are same as IDs
		for(int i=0; i<faultSectionData.size();i++)
			if(faultSectionData.get(i).getSectionId() != i)
				throw new RuntimeException("RupsInFaultSystemInversion: Error - indices of faultSectionData don't match IDs");

		numSections = faultSectionData.size();

		// make the list of nearby sections for each section (branches)
		if(D) System.out.println("Making sectionConnectionsListList");
		computeCloseSubSectionsListList();
		if(D) System.out.println("Done making sectionConnectionsListList");


		// make the list of SectionCluster objects 
		// (each represents a set of nearby sections and computes the possible
		//  "ruptures", each defined as a list of sections in that rupture)
		/* */
		makeClusterList();

		for(int i=0;i<this.sectionClusterList.size(); i++)
			System.out.println("Cluster "+i+" has "+getCluster(i).size()+" sections & "+getCluster(i).getNumRuptures()+" ruptures");
//			System.out.println("Cluster "+i+" has "+getCluster(i).getNumRuptures()+" ruptures");
		
	}


	/**
	 * This returns the list of FaultSectionPrefData used in the inversion
	 * @return
	 */
	public ArrayList<FaultSectionPrefData> getFaultSectionData() {
		return faultSectionData;
	}


	public int getNumClusters() {
		return sectionClusterList.size();
	}


	public SectionCluster getCluster(int clusterIndex) {
		return sectionClusterList.get(clusterIndex);
	}


	public ArrayList<ArrayList<Integer>> getRupList() {
		ArrayList<ArrayList<Integer>> rupList = new ArrayList<ArrayList<Integer>>();
		for(int i=0; i<sectionClusterList.size();i++) {
			if(D) System.out.println("Working on rupture list for cluster "+i);
			rupList.addAll(sectionClusterList.get(i).getSectionIndicesForRuptures());
		}
		return rupList;
	}


	public int getNumRupRuptures() {
		int num = 0;
		for(int i=0; i<sectionClusterList.size();i++) {
			num += sectionClusterList.get(i).getNumRuptures();
		}
		return num;
	}



	/**
	 * For each section, create a list of sections that are within maxJumpDist.  
	 * This generates an ArrayList of ArrayLists (named sectionConnectionsList).  
	 * Reciprocal duplicates are not filtered out.
	 * If sections are actually subsections (meaning getParentSectionId() != -1), then each parent section can only
	 * have one connection to another parent section (whichever subsections are closest).  This prevents parallel 
	 * and closely space faults from having connections back and forth all the way down the section.
	 */
	private void computeCloseSubSectionsListList() {

		sectionConnectionsListList = new ArrayList<ArrayList<Integer>>();
		for(int i=0;i<numSections;i++)
			sectionConnectionsListList.add(new ArrayList<Integer>());

		// in case the sections here are subsections of larger sections, create a subSectionDataListList where each
		// ArrayList<FaultSectionPrefData> is a list of subsections from the parent section
		ArrayList<ArrayList<FaultSectionPrefData>> subSectionDataListList = new ArrayList<ArrayList<FaultSectionPrefData>>();
		int lastID=-1;
		ArrayList<FaultSectionPrefData> newList = new ArrayList<FaultSectionPrefData>();
		for(int i=0; i<faultSectionData.size();i++) {
			FaultSectionPrefData subSect = faultSectionData.get(i);
			int parentID = subSect.getParentSectionId();
			if(parentID != lastID || parentID == -1) { // -1 means there is no parent
				newList = new ArrayList<FaultSectionPrefData>();
				subSectionDataListList.add(newList);
				lastID = subSect.getParentSectionId();
			}
			newList.add(subSect);
		}


		// First, if larger sections have been sub-sectioned, fill in neighboring subsection connections
		// (using the other algorithm below might lead to subsections being skipped if their width is < maxJumpDist) 
		for(int i=0; i<subSectionDataListList.size(); ++i) {
			ArrayList<FaultSectionPrefData> subSectList = subSectionDataListList.get(i);
			int numSubSect = subSectList.size();
			for(int j=0;j<numSubSect;j++) {
				// get index of section
				int sectIndex = subSectList.get(j).getSectionId();
				ArrayList<Integer> sectionConnections = sectionConnectionsListList.get(sectIndex);
				if(j != 0) // skip the first one since it has no previous subsection
					sectionConnections.add(subSectList.get(j-1).getSectionId());
				if(j != numSubSect-1) // the last one has no subsequent subsection
					sectionConnections.add(subSectList.get(j+1).getSectionId());
			}
		}

		// now add subsections on other sections, keeping only one connection between each section (the closest)
		for(int i=0; i<subSectionDataListList.size(); ++i) {
			ArrayList<FaultSectionPrefData> sect1_List = subSectionDataListList.get(i);
			for(int j=i+1; j<subSectionDataListList.size(); ++j) {
				ArrayList<FaultSectionPrefData> sect2_List = subSectionDataListList.get(j);
				double minDist=Double.MAX_VALUE;
				int subSectIndex1 = -1;
				int subSectIndex2 = -1;
				// find the closest pair
				for(int k=0;k<sect1_List.size();k++) {
					for(int l=0;l<sect2_List.size();l++) {
						int index1 = sect1_List.get(k).getSectionId();
						int index2 = sect2_List.get(l).getSectionId();;
						double dist = sectionDistances[index1][index2];
						if(dist < minDist) {
							minDist = dist;
							subSectIndex1 = index1;
							subSectIndex2 = index2;
						}					  
					}
				}
				// add to lists for each subsection
				if (minDist<maxJumpDist) {
					sectionConnectionsListList.get(subSectIndex1).add(subSectIndex2);
					sectionConnectionsListList.get(subSectIndex2).add(subSectIndex1);  // reciprocal of the above
				}
			}
		}
	}

	
	public ArrayList<ArrayList<Integer>> getCloseSubSectionsListList() {
		return sectionConnectionsListList;
	}


	private void makeClusterList() {

		// make an arrayList of section indexes
		ArrayList<Integer> availableSections = new ArrayList<Integer>();
		for(int i=0; i<numSections; i++) availableSections.add(i);

		sectionClusterList = new ArrayList<SectionCluster>();
		while(availableSections.size()>0) {
			if (D) System.out.println("WORKING ON CLUSTER #"+(sectionClusterList.size()+1));
			int firstSubSection = availableSections.get(0);
			SectionCluster newCluster = new SectionCluster(faultSectionData, minNumSectInRup,sectionConnectionsListList,
					sectionAzimuths, maxAzimuthChange, maxStrikeDiff, maxRakeDiff);
			newCluster.add(firstSubSection);
			if (D) System.out.println("\tfirst is "+faultSectionData.get(firstSubSection).getName());
			addLinks(firstSubSection, newCluster);
			// remove the used subsections from the available list
			for(int i=0; i<newCluster.size();i++) availableSections.remove(newCluster.get(i));
			// add this cluster to the list
			sectionClusterList.add(newCluster);
			if (D) System.out.println(newCluster.size()+"\tsubsections in cluster #"+sectionClusterList.size()+"\t"+
					availableSections.size()+"\t subsections left to allocate");
		}
	}


	private void addLinks(int subSectIndex, SectionCluster list) {
		ArrayList<Integer> branches = sectionConnectionsListList.get(subSectIndex);
		for(int i=0; i<branches.size(); i++) {
			Integer subSect = branches.get(i);
			if(!list.contains(subSect)) {
				list.add(subSect);
				addLinks(subSect, list);
			}
		}
	}


	/**
	 * This writes out the close subsections to each subsection (and the distance)
	 */
	public void writeCloseSubSections(String filePathAndName) {
		if (D) System.out.print("writing file closeSubSections.txt");
		try{
//			FileWriter fw = new FileWriter("/Users/field/workspace/OpenSHA/dev/scratch/UCERF3/closeSubSections.txt");
			FileWriter fw = new FileWriter(filePathAndName);
			//			FileWriter fw = new FileWriter("/Users/pagem/eclipse/workspace/OpenSHA/dev/scratch/pagem/rupsInFaultSystem/closeSubSections.txt");
			String outputString = new String();

			for(int sIndex1=0; sIndex1<sectionConnectionsListList.size();sIndex1++) {
				ArrayList<Integer> sectList = sectionConnectionsListList.get(sIndex1);
				String sectName = faultSectionData.get(sIndex1).getName();
				outputString += "\n"+ sectName + "  connections:\n\n";
				for(int i=0;i<sectList.size();i++) {
					int sIndex2 = sectList.get(i);
					String sectName2 = faultSectionData.get(sIndex2).getName();
					float dist = (float) sectionDistances[sIndex1][sIndex2];
					outputString += "\t"+ sectName2 + "\t"+dist+"\n";
				}
			}

			fw.write(outputString);
			fw.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
		if (D) System.out.println(" - done");
	}



	/**
	 * @param args
	 */
	public static void main(String[] args) {
	}

}
