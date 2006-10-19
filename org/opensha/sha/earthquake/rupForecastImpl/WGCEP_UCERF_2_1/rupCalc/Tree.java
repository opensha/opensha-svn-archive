package org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_1.rupCalc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * <p>Title: Tree.java </p>
 * <p>Description: This refers to a tree which can consist of various branches. </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */
public class Tree {
	// mapping of subsections name and their corresponding treemap
	public HashMap<String,TreeBranch> treeBranchMap = new HashMap<String,TreeBranch>();
	// mapping to mantain which branches have been traversed already
	public HashMap<String, Boolean> traversedBranchMap;
	public ArrayList<MultipleSectionRup> rupList;
	public void connectInTree(String subSection1, String subSection2, int subSectionId1, int subSectionId2) {		
		// This statement is needed to avoid cycles
		if(subSectionId1 > subSectionId2) {
			updateTreeBranch(subSection1, subSection2);
		}
		else if(subSectionId1 < subSectionId2) {
			updateTreeBranch(subSection2, subSection1);
		}
		else return;
		
		
	}

	/**
	 * Update tree branch
	 * @param subSection1
	 * @param subSection2
	 */
	private void updateTreeBranch(String subSection1, String subSection2) {
		TreeBranch treeBranch;
		treeBranch = treeBranchMap.get(subSection1);
		if(treeBranch==null) {
			 treeBranch = new TreeBranch(subSection1);
			 treeBranchMap.put(subSection1, treeBranch);
		}
		treeBranch.addAdjacentSubSection(subSection2);
	}
	
	/**
	 * Write the tree on System.out
	 *
	 */
	public void writeInfo() {
		Iterator<String> it  = treeBranchMap.keySet().iterator();
		while(it.hasNext()) {
			TreeBranch treeBranch = treeBranchMap.get(it.next());
			System.out.println("Adjacent nodes for "+treeBranch.getSubSectionName());
			for(int i=0; i<treeBranch.getNumAdjacentSubsections(); ++i)
				System.out.println(treeBranch.getAdjacentSubSection(i));
		}
	}
	
	/**
	 * Get all possible ruptures in this Tree
	 * @return
	 */
	public ArrayList getRuptures() {
		  //traversedBranchMap = new HashMap<String, Boolean>();
		  
		  rupList = new ArrayList<MultipleSectionRup>() ;
		  Iterator<String> it = this.treeBranchMap.keySet().iterator();
		  while(it.hasNext()) {
			  String subSecName = it.next();
			  ArrayList rupture= new ArrayList();
			  // if(isTraversed(subSecName)) continue;
			  traverse(subSecName, rupture);
		  }
		  return this.rupList;
	}
	
	/**
	 * Traverse all the adjacent subsections of the specified subsection
	 * @param subSecName
	 * @param subSecList
	 */
	private void traverse(String subSecName, ArrayList subSecList) {
		if(subSecList.contains(subSecName)) return;
		subSecList.add(subSecName);
		MultipleSectionRup rup = new MultipleSectionRup(subSecList);
		if(!this.rupList.contains(rup)) rupList.add(rup);
		//traversedBranchMap.put(subSecName, new Boolean(true));
		//if(isTraversed(subSecName)) return;
		TreeBranch branch = treeBranchMap.get(subSecName);
		for(int i=0; branch!=null && i<branch.getNumAdjacentSubsections(); ++i) {
			traverse(branch.getAdjacentSubSection(i), subSecList);
		}
		
	}
	
	
	/**
	 * Returns true if the subsection has already been traversed
	 * 
	 * @param subSecName
	 * @return
	 */
	private boolean isTraversed(String subSecName) {
		Boolean isTraversed = traversedBranchMap.get(subSecName);
		if(isTraversed !=null && isTraversed.booleanValue()) return true;
		return false;
	}
	

	
}
