package scratch.kevin.ucerf3;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;

import scratch.UCERF3.FaultSystemRupSet;
import scratch.UCERF3.enumTreeBranches.DeformationModels;
import scratch.UCERF3.enumTreeBranches.FaultModels;
import scratch.UCERF3.inversion.InversionFaultSystemRupSetFactory;

public class SingleParentRuptureChecker {
	
	private static void check(int parent, int numSects, Set<Integer> rups, FaultSystemRupSet rupSet) {
		int expected = (numSects + 1) * numSects / 2 - numSects;
		int actual = rups.size();
		if (actual != expected) {
			System.out.println(parent+". expected: "+expected+"\tactual: "+actual);
			for (FaultSectionPrefData data : rupSet.getFaultSectionDataList())
				if (data.getParentSectionId() == parent) {
					System.out.println(data.getSectionName());
					break;
				}
		}
	}
	
	private static boolean isMultiFault(int rup, FaultSystemRupSet rupSet) {
		int parentID = -1;
		for (int sectIndex : rupSet.getSectionsIndicesForRup(rup)) {
			FaultSectionPrefData data = rupSet.getFaultSectionData(sectIndex);
			if (parentID < 0)
				parentID = data.getParentSectionId();
			else if (parentID != data.getParentSectionId())
				return true;
		}
		return false;
	}

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		//(n+1) ( n ) /2        -        n
		FaultModels fm = FaultModels.FM3_2;
		DeformationModels dm = fm.getFilterBasis();
		
		FaultSystemRupSet rupSet = InversionFaultSystemRupSetFactory.cachedForBranch(dm, true);
		
		int prevParent = -1;
		int sectsForParent = 0;
		HashSet<Integer> rupsForParent = null;
		System.out.println("Starting the test!");
		for (int sectIndex=0; sectIndex<rupSet.getNumSections(); sectIndex++) {
			FaultSectionPrefData data = rupSet.getFaultSectionData(sectIndex);
			if (rupsForParent == null || prevParent != data.getParentSectionId()) {
				if (rupsForParent != null) {
					// do the test
					check(prevParent, sectsForParent, rupsForParent, rupSet);
				}
				sectsForParent = 0;
				rupsForParent = new HashSet<Integer>();
				prevParent = data.getParentSectionId();
			}
			
			sectsForParent++;
			for (int r : rupSet.getRupturesForSection(sectIndex))
				if (!isMultiFault(r, rupSet) && !rupsForParent.contains(r))
					rupsForParent.add(r);
		}
		check(prevParent, sectsForParent, rupsForParent, rupSet);
		System.out.println("Done with the test!");
		System.exit(0);
	}
	
	

}
