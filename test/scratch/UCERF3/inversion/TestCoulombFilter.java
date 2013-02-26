package scratch.UCERF3.inversion;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;

import scratch.UCERF3.inversion.coulomb.CoulombRates;
import scratch.UCERF3.inversion.coulomb.CoulombRatesRecord;
import scratch.UCERF3.inversion.laughTest.LaughTestFilter;
import scratch.UCERF3.utils.IDPairing;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class TestCoulombFilter {
	
	private static SectionCluster cluster;
	
	private static FaultSectionPrefData buildFaultSection(int sectID, int parentID) {
		FaultSectionPrefData sect = new FaultSectionPrefData();
		
		sect.setSectionId(sectID);
		sect.setParentSectionId(parentID);
		
		return sect;
	}
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		List<FaultSectionPrefData> sectionDataList = Lists.newArrayList();
		for (int i=0; i<15; i++)
			sectionDataList.add(buildFaultSection(i, (i/5)));
		
		List<List<Integer>> sectionConnectionsListList = Lists.newArrayList();
		Map<Integer, Double> rakesMap = Maps.newHashMap();
		for (FaultSectionPrefData sect : sectionDataList) {
			System.out.println(sect.getSectionId()+" ("+sect.getParentSectionId()+")");
			rakesMap.put(sect.getSectionId(), 0d);
			
			List<Integer> connections = Lists.newArrayList();
			int before = sect.getSectionId()-1;
			int after = sect.getSectionId()+1;
			if (before < 0)
				before = sectionDataList.size()-1;
			if (after == sectionDataList.size())
				after = 0;
			
			connections.add(before);
			connections.add(after);
			
			sectionConnectionsListList.add(connections);
		}
		
		Map<IDPairing, CoulombRatesRecord> rates = Maps.newHashMap();
		
		// 0 => 1 ALLOWED
		// 1 => 0 NOT ALLOWED
		rates.put(new IDPairing(0, 5), getRecord(0, 5, true));
		rates.put(new IDPairing(5, 0), getRecord(5, 0, false));
		
		// 1 => 2 NOT ALLOWED
		// 2 => 1 NOT ALLOWED
		rates.put(new IDPairing(5, 10), getRecord(5, 10, false));
		rates.put(new IDPairing(10, 5), getRecord(10, 5, false));
		
		// 2 => 0 ALLOWED
		// 0 => 2 ALLOWED
		rates.put(new IDPairing(15, 0), getRecord(15, 0, true));
		rates.put(new IDPairing(0, 15), getRecord(0, 15, true));
		
		CoulombRates coulombRates = new CoulombRates(rates);
		
		LaughTestFilter laughTestFilter = LaughTestFilter.getDefault();
		
		cluster = new SectionCluster(laughTestFilter, sectionDataList, sectionConnectionsListList,
				getMapForPairings(sectionDataList), rakesMap, getMapForPairings(sectionDataList), coulombRates);
		cluster.add(0);
		SectionClusterList.addClusterLinks(0, cluster, sectionConnectionsListList);
		
		System.out.println("Created "+cluster.getNumRuptures()+" ruptures");
	}
	
	private static Map<IDPairing, Double> getMapForPairings(List<FaultSectionPrefData> datas) {
		Map<IDPairing, Double> map = Maps.newHashMap();
		for (int i=0; i<datas.size(); i++) {
			int next = i+1;
			if (next == datas.size())
				next = 0;
			IDPairing pairing = new IDPairing(i, next);
			map.put(pairing, 0d);
			map.put(pairing.getReversed(), 0d);
		}
		return map;
	}
	
	private static CoulombRatesRecord getRecord(int id1, int id2, boolean allowed) {
		IDPairing pairing = new IDPairing(id1, id2);
		if (allowed)
			return new CoulombRatesRecord(pairing, 1, 1, 1, 1);
		else
			return new CoulombRatesRecord(pairing, 0, 0, 0, 0);
	}

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void test() {
		fail("Not yet implemented");
	}

}
