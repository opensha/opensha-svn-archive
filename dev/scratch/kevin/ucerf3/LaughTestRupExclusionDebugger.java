package scratch.kevin.ucerf3;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.opensha.commons.util.ClassUtils;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import scratch.UCERF3.enumTreeBranches.DeformationModels;
import scratch.UCERF3.enumTreeBranches.FaultModels;
import scratch.UCERF3.inversion.SectionClusterList;
import scratch.UCERF3.inversion.coulomb.CoulombRates;
import scratch.UCERF3.inversion.coulomb.CoulombRatesTester;
import scratch.UCERF3.inversion.laughTest.AbstractLaughTest;
import scratch.UCERF3.inversion.laughTest.AzimuthChangeFilter;
import scratch.UCERF3.inversion.laughTest.LaughTestFilter;
import scratch.UCERF3.utils.DeformationModelFetcher;
import scratch.UCERF3.utils.IDPairing;
import scratch.UCERF3.utils.UCERF3_DataUtils;

public class LaughTestRupExclusionDebugger {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		int[] sects = {612, 613, 1495, 1496};
		
		boolean applyGarlockPintoMtnFix = true;
		
		LaughTestFilter filter = LaughTestFilter.getDefault();
		AzimuthChangeFilter.INCLUDE_OWL_LAKE = true;
		LaughTestFilter.USE_BUGGY_COULOMB = false;
		CoulombRatesTester.BUGGY_MIN_STRESS = false;
		
		FaultModels fm = FaultModels.FM3_1;
		DeformationModels dm = DeformationModels.GEOLOGIC;
		DeformationModelFetcher fetch = new DeformationModelFetcher(fm, dm, UCERF3_DataUtils.DEFAULT_SCRATCH_DATA_DIR, 0.1);
		List<FaultSectionPrefData> datas = fetch.getSubSectionList();
		
		Map<IDPairing, Double> subSectionDistances = fetch.getSubSectionDistanceMap(filter.getMaxJumpDist());
		Map<IDPairing, Double> subSectionAzimuths = fetch.getSubSectionAzimuthMap(subSectionDistances.keySet());
		
		List<FaultSectionPrefData> rupture = Lists.newArrayList();
		for (int sect : sects)
			rupture.add(datas.get(sect));
		
		CoulombRates coulombRates = CoulombRates.loadUCERF3CoulombRates(fm);
		
		List<List<Integer>> sectionConnectionsListList = SectionClusterList.computeCloseSubSectionsListList(
				datas, subSectionDistances, filter.getMaxJumpDist(), coulombRates);
		
		List<AbstractLaughTest> laughTests = filter.buildLaughTests(subSectionAzimuths, subSectionDistances, null, coulombRates,
				applyGarlockPintoMtnFix, sectionConnectionsListList, datas);
		
		List<Integer> junctionIndexes = Lists.newArrayList();
		for (int i=1; i<rupture.size(); i++) {
			if (rupture.get(i).getParentSectionId() != rupture.get(i-1).getParentSectionId()) {
				junctionIndexes.add(i);
				IDPairing pairing = new IDPairing(rupture.get(i-1).getSectionId(), rupture.get(i).getSectionId());
				if (!sectionConnectionsListList.get(pairing.getID1()).contains(pairing.getID2())) {
					System.out.println("Pairing doesn't exist in connections list: "+pairing);
					System.out.println("\tPossibilities for "+pairing.getID1()+": "
							+Joiner.on(",").join(sectionConnectionsListList.get(pairing.getID1())));
					System.out.println("\tPossibilities for "+pairing.getID2()+": "
							+Joiner.on(",").join(sectionConnectionsListList.get(pairing.getID2())));
				}
			}
		}
		
		for (AbstractLaughTest test : laughTests) {
			if (!test.doesRupturePass(rupture, junctionIndexes)) {
				System.out.println("FAILED: "+ClassUtils.getClassNameWithoutPackage(test.getClass()));
			}
		}
	}

}
