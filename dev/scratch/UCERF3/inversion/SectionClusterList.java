package scratch.UCERF3.inversion;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opensha.commons.util.ExceptionUtils;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;

import com.google.common.base.Preconditions;

import scratch.UCERF3.enumTreeBranches.DeformationModels;
import scratch.UCERF3.enumTreeBranches.FaultModels;
import scratch.UCERF3.inversion.coulomb.CoulombRates;
import scratch.UCERF3.utils.DeformationModelFetcher;
import scratch.UCERF3.utils.IDPairing;

public class SectionClusterList extends ArrayList<SectionCluster> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static final boolean D = true;
	
	private List<List<Integer>> sectionConnectionsListList;
	private LaughTestFilter filter;
	private DeformationModels defModel;
	private FaultModels faultModel;
	private List<FaultSectionPrefData> faultSectionData;
	private CoulombRates coulombRates;
	
	public SectionClusterList(FaultModels faultModel, DeformationModels defModel, File precomputedDataDir,
			LaughTestFilter filter) {
		this.faultModel = faultModel;
		this.defModel = defModel;
		this.filter = filter;
		
		if (filter.getCoulombFilter() != null) {
			try {
				this.coulombRates = CoulombRates.loadUCERF3CoulombRates(faultModel);
			} catch (IOException e) {
				ExceptionUtils.throwAsRuntimeException(e);
			}
		}
		
		DeformationModelFetcher deformationModelFetcher = new DeformationModelFetcher(faultModel, defModel, precomputedDataDir);
		faultSectionData = deformationModelFetcher.getSubSectionList();
		Map<IDPairing, Double> subSectionDistances = deformationModelFetcher.getSubSectionDistanceMap(filter.getMaxJumpDist());
		Map<IDPairing, Double> subSectionAzimuths = deformationModelFetcher.getSubSectionAzimuthMap(subSectionDistances.keySet());
		Map<Integer, Double> rakesMap = new HashMap<Integer, Double>();
		for (FaultSectionPrefData data : faultSectionData)
			rakesMap.put(data.getSectionId(), data.getAveRake());
		
		// check that indices are same as sectionIDs (this is assumed here)
		for(int i=0; i<faultSectionData.size();i++)
			Preconditions.checkState(faultSectionData.get(i).getSectionId() == i,
				"RupsInFaultSystemInversion: Error - indices of faultSectionData don't match IDs");

		// make the list of SectionCluster objects 
		// (each represents a set of nearby sections and computes the possible
		//  "ruptures", each defined as a list of sections in that rupture)
		makeClusterList(subSectionAzimuths,subSectionDistances, rakesMap);
	}
	
	private void makeClusterList(
			Map<IDPairing, Double> subSectionAzimuths,
			Map<IDPairing, Double> subSectionDistances,
			Map<Integer, Double> rakesMap) {
		
		// make the list of nearby sections for each section (branches)
		if(D) System.out.println("Making sectionConnectionsListList");
		sectionConnectionsListList = computeCloseSubSectionsListList(subSectionDistances);
		if(D) System.out.println("Done making sectionConnectionsListList");

		// make an arrayList of section indexes
		ArrayList<Integer> availableSections = new ArrayList<Integer>();
		for(int i=0; i<faultSectionData.size(); i++) availableSections.add(i);

		while(availableSections.size()>0) {
			if (D) System.out.println("WORKING ON CLUSTER #"+(size()+1));
			int firstSubSection = availableSections.get(0);
			SectionCluster newCluster = new SectionCluster(filter, faultSectionData,sectionConnectionsListList,
					subSectionAzimuths, rakesMap, subSectionDistances, coulombRates);
			newCluster.add(firstSubSection);
			if (D) System.out.println("\tfirst is "+faultSectionData.get(firstSubSection).getName());
			addClusterLinks(firstSubSection, newCluster, sectionConnectionsListList);
			// remove the used subsections from the available list
			for(int i=0; i<newCluster.size();i++) availableSections.remove(newCluster.get(i));
			// add this cluster to the list
			add(newCluster);
			if (D) System.out.println(newCluster.size()+"\tsubsections in cluster #"+size()+"\t"+
					availableSections.size()+"\t subsections left to allocate");
		}
	}
	
	private void addClusterLinks(int subSectIndex, SectionCluster list, List<List<Integer>> sectionConnectionsListList) {
		List<Integer> branches = sectionConnectionsListList.get(subSectIndex);
		for(int i=0; i<branches.size(); i++) {
			Integer subSect = branches.get(i);
			if(!list.contains(subSect)) {
				list.add(subSect);
				addClusterLinks(subSect, list, sectionConnectionsListList);
			}
		}
	}
	
	/**
	 * For each section, create a list of sections that are within maxJumpDist.  
	 * This generates an ArrayList of ArrayLists (named sectionConnectionsList).  
	 * Reciprocal duplicates are not filtered out.
	 * If sections are actually subsections (meaning getParentSectionId() != -1), then each parent section can only
	 * have one connection to another parent section (whichever subsections are closest).  This prevents parallel 
	 * and closely space faults from having connections back and forth all the way down the section.
	 */
	private List<List<Integer>> computeCloseSubSectionsListList(Map<IDPairing, Double> subSectionDistances) {

		ArrayList<List<Integer>> sectionConnectionsListList = new ArrayList<List<Integer>>();
		for(int i=0;i<faultSectionData.size();i++)
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
				List<Integer> sectionConnections = sectionConnectionsListList.get(sectIndex);
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
						FaultSectionPrefData data1 = sect1_List.get(k);
						FaultSectionPrefData data2 = sect2_List.get(l);
						IDPairing ind = new IDPairing(data1.getSectionId(), data2.getSectionId());
						if (subSectionDistances.containsKey(ind)) {
							double dist = subSectionDistances.get(ind);
							if(dist < minDist) {
								minDist = dist;
								subSectIndex1 = data1.getSectionId();
								subSectIndex2 = data2.getSectionId();
							}	
						}		  
					}
				}
				// add to lists for each subsection
				if (minDist<filter.getMaxJumpDist()) {
					sectionConnectionsListList.get(subSectIndex1).add(subSectIndex2);
					sectionConnectionsListList.get(subSectIndex2).add(subSectIndex1);  // reciprocal of the above
				}
			}
		}
		return sectionConnectionsListList;
	}

	public static boolean isD() {
		return D;
	}

	public List<List<Integer>> getSectionConnectionsListList() {
		return sectionConnectionsListList;
	}

	public LaughTestFilter getFilter() {
		return filter;
	}

	public DeformationModels getDefModel() {
		return defModel;
	}

	public FaultModels getFaultModel() {
		return faultModel;
	}

	public List<FaultSectionPrefData> getFaultSectionData() {
		return faultSectionData;
	}

}
