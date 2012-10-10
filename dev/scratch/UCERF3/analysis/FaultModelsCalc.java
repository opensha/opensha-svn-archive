package scratch.UCERF3.analysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.opensha.sha.faultSurface.FaultTrace;

import scratch.UCERF3.enumTreeBranches.FaultModels;

public class FaultModelsCalc {
	
	
	/**
	 * This writes out sections names for Morgan's named-faults data file,
	 * which she uses in her multi-fault rupture statistics.
	 * 
	 * @return
	 */
	public static void writeSectionsForEachNamedFault(FaultModels fm) {
		Map<Integer, List<Integer>> namedMap = fm.getNamedFaultsMap();
		ArrayList<FaultSectionPrefData> sects = fm.fetchFaultSections();

		HashMap<Integer,String> idNameMap = new HashMap<Integer,String>();

		for(FaultSectionPrefData data:sects) {
			idNameMap.put(data.getSectionId(), data.getName());
		}
		
		for(Integer key:namedMap.keySet()) {
			if(namedMap.get(key).size()>1) {
				System.out.print(key);
				for(Integer id : namedMap.get(key))
					System.out.print("\t"+idNameMap.get(id));
				System.out.print("\n");			
			}
		}
	}

	
	/**
	 * This writes the subsections names associated with each subsection
	 * included in a named fault (as defined by the associated file:
	 * data/FaultModels/FM?_?FaultsByNameAlt txt).
	 * 
	 * Note that this lists "null" for the combined stepovers on the San
	 * Jacinto and Elsinore faults for FM 2.1 and 2.2 because those fault
	 * models used the overlapping stepovers (which were swaped out in the
	 * UCERF2 code for floating ruptures, and are swapped out for the DM 2.1
	 * and 2.2 used in the grand inversion).
	 * 
	 * @return
	 */
	public static void writeSectionsForEachNamedFaultAlt(FaultModels fm) {
		Map<String, List<Integer>> namedMap = fm.getNamedFaultsMapAlt();
		ArrayList<FaultSectionPrefData> sects = fm.fetchFaultSections();

		HashMap<Integer,String> idNameMap = new HashMap<Integer,String>();

		for(FaultSectionPrefData data:sects) {
			idNameMap.put(data.getSectionId(), data.getName());
		}
		
		for(String faultName:namedMap.keySet()) {
				System.out.println(faultName+" Sections");
				for(Integer id : namedMap.get(faultName))
					System.out.println("\t"+idNameMap.get(id));
				System.out.print("\n");			
		}
	}
	
	
	/**
	 * 
	 * @param fm
	 */
	public static void writeSectionsNamesAndSomeAttributes(FaultModels fm, boolean includeTrace) {
		ArrayList<FaultSectionPrefData> sects = fm.fetchFaultSections();
		for(FaultSectionPrefData data : fm.fetchFaultSections()) {
			System.out.print(data.getName()+"\t"+(float)data.getOrigDownDipWidth()+"\t"+(float)data.getReducedDownDipWidth()+
					"\t"+(float)data.getFaultTrace().getTraceLength()+"\t"+(float)data.getAseismicSlipFactor()+"\t"+
					data.getAveLowerDepth()+"\t"+data.getOrigAveUpperDepth());
			if(includeTrace) {
				FaultTrace trace = data.getFaultTrace();
				System.out.print("\t"+trace.size());
				for(int l=0; l<trace.size();l++) {
					System.out.print("\t"+(float)trace.get(l).getLatitude()+"\t"+ (float)trace.get(l).getLongitude());
				}
				System.out.print("\n");
			}
			else
				System.out.print("\n");
				
		}
	}



	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		writeSectionsNamesAndSomeAttributes(FaultModels.FM3_1, false);
//		writeSectionsForEachNamedFaultAlt(FaultModels.FM2_1);

	}

}
