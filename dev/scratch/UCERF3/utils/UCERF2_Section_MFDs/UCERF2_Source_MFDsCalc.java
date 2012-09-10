package scratch.UCERF3.utils.UCERF2_Section_MFDs;

import java.io.File;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.lang3.StringUtils;
import org.opensha.commons.geo.Location;
import org.opensha.commons.util.ExceptionUtils;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.opensha.sha.earthquake.ERF;
import org.opensha.sha.earthquake.ProbEqkRupture;
import org.opensha.sha.earthquake.ProbEqkSource;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.FaultSegmentData;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.UCERF2;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.UCERF2_TimeIndependentEpistemicList;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.UnsegmentedSource;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.A_Faults.A_FaultSegmentedSourceGenerator;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.oldClasses.UCERF2_Final_StirlingGriddedSurface;
import org.opensha.sha.faultSurface.FaultTrace;
import org.opensha.sha.magdist.SummedMagFreqDist;


import scratch.UCERF3.enumTreeBranches.FaultModels;


/**
 * Note that for the stitched together, unsegmented type-A fault, the following stepover substitutions were made:
 * ELSINORE_COMBINED_STEPOVER_FAULT_SECTION_ID = 402; replace the following two:
 * GLEN_IVY_STEPOVER_FAULT_SECTION_ID = 297;
 * TEMECULA_STEPOVER_FAULT_SECTION_ID = 298;
 * 
 * and
 * 
 * SJ_COMBINED_STEPOVER_FAULT_SECTION_ID = 401; replaced the following two:
 * SJ_VALLEY_STEPOVER_FAULT_SECTION_ID = 290;
 * SJ_ANZA_STEPOVER_FAULT_SECTION_ID = 291;
 * 
 * Type-A segmented sources are of type: FaultRuptureSource

 * @author field
 *
 */
public class UCERF2_Source_MFDsCalc {
	
	final static File DATAFILE = new File("dev/scratch/UCERF3/utils/UCERF2_Section_MFDs/UCERF2_sectionIDs_AndNames.txt");	
	HashMap<String,Integer> sectionIDfromNameMap;
	HashMap<Integer,String> sectionNamefromID_Map;

	
	public UCERF2_Source_MFDsCalc() {
		makeMFD_Lists();
	}
	
	private void makeMFD_Lists() {
		
		readListOfAllFaultSections();
		
		// this will hold a list of MFDs for each fault section
		HashMap<Integer,ArrayList<SummedMagFreqDist>> mfdList_ForSectID_Map = new HashMap<Integer,ArrayList<SummedMagFreqDist>>();
		// this will hold the ERF index number associated with each MFD
		HashMap<Integer,ArrayList<Integer>> mfdBranches_ForSectID_Map = new HashMap<Integer,ArrayList<Integer>>();
		// this will hold the ERF branch weight associated with each MFD
		HashMap<Integer,ArrayList<Double>> mfdWts_ForSectID_Map = new HashMap<Integer,ArrayList<Double>>();
		
		UCERF2_TimeIndependentEpistemicList ucerf2EpistemicList = getUCERF2_TimeIndependentEpistemicList();
		int numERFs = ucerf2EpistemicList.getNumERFs();
		System.out.println("Num Branches="+numERFs);

//		for(int i=0; i<numERFs; ++i) {
		for(int branch=0; branch<1; ++branch) {
			System.out.println(branch);
			ERF erf = ucerf2EpistemicList.getERF(branch);
			double duration = erf.getTimeSpan().getDuration();
			
			// make map of MFD for each section here
			HashMap<String,SummedMagFreqDist> mfd_ForSectName_Map = new HashMap<String,SummedMagFreqDist>();
			
			// Make map of section list for each source for segmented Type-A sources (so we don't have to loop through all sections below):
			HashMap<String,ArrayList<FaultSectionPrefData>> sectionsForTypeA_RupsMap = new HashMap<String,ArrayList<FaultSectionPrefData>>();
			ArrayList<Object> objList = ((UCERF2)erf).get_A_FaultSourceGenerators();	// regrettably, this method returns different object types
			for(Object obj: objList) { 
				if(obj instanceof A_FaultSegmentedSourceGenerator) { // Segmented source (rather than an unsegmented source, which is the other possible type of object)
					A_FaultSegmentedSourceGenerator srcGen = (A_FaultSegmentedSourceGenerator)obj;	// cast for convenience below
					ArrayList<FaultSectionPrefData> dataList = srcGen.getFaultSegmentData().getPrefFaultSectionDataList();
					for(int r=0;r<srcGen.getNumRupSources();r++) {
						String srcName = srcGen.getLongRupName(r);
						sectionsForTypeA_RupsMap.put(srcName, dataList);	// same data list for all sources of the given type-A fault
					}
				}
			}

			// now loop over all sources
			for(int s=0;s<erf.getNumSources();s++) {
				ProbEqkSource src = erf.getSource(s);
				if(src instanceof UnsegmentedSource) {  // For Type-B & Unsegmented Type-A sources
					ArrayList<FaultSectionPrefData> dataList = ((UnsegmentedSource)src).getFaultSegmentData().getPrefFaultSectionDataList();
					
					if(dataList.size() == 1) { //it's a single-section type-B source
						String name = src.getName();
						// check that names are same
						if(!name.equals(dataList.get(0).getSectionName()))
							throw new RuntimeException("Problem");
						SummedMagFreqDist mfd;
						if(mfd_ForSectName_Map.keySet().contains(name)) {
							mfd = mfd_ForSectName_Map.get(name);
						}
						else {
							mfd = getNewSummedMagFreqDist();
							mfd_ForSectName_Map.put(name, mfd);
						}
						for(ProbEqkRupture rup : src) {
							mfd.addResampledMagRate(rup.getMag(), rup.getMeanAnnualRate(duration), true);
						}
					}
					else {	// it's a stitched together unsegmented source
						processSource(src, dataList, mfd_ForSectName_Map, duration);
					}
				}
				else {
					String name = src.getName();
					if(sectionsForTypeA_RupsMap.keySet().contains(name)) {	// check whether it's a type A source
						processSource(src, sectionsForTypeA_RupsMap.get(name), mfd_ForSectName_Map, duration);
					}
				}
			}
			
			// now populate the maps with the results of this branch
			double branchWt = ucerf2EpistemicList.getERF_RelativeWeight(branch);
			for(String name:mfd_ForSectName_Map.keySet()) {
				int id = sectionIDfromNameMap.get(name);
				if(!mfdList_ForSectID_Map.keySet().contains(id)) {
					mfdList_ForSectID_Map.put(id, new ArrayList<SummedMagFreqDist>());
					mfdBranches_ForSectID_Map.put(id, new ArrayList<Integer>());
					mfdWts_ForSectID_Map.put(id, new ArrayList<Double>());

				}
				mfdList_ForSectID_Map.get(id).add(mfd_ForSectName_Map.get(name));
				mfdBranches_ForSectID_Map.get(id).add(branch);
				mfdWts_ForSectID_Map.get(id).add(branchWt);
			}
		}
	}
	
	/**
	 * This method adds the given source to the MFDs of the given list of
	 * fault sections
	 * @param src
	 * @param dataList
	 * @param mfd_ForSectName_Map
	 * @param duration
	 */
	private void processSource(ProbEqkSource src, ArrayList<FaultSectionPrefData> dataList,
			HashMap<String,SummedMagFreqDist> mfd_ForSectName_Map, double duration) {
		
		// first get list of MFDs for each fault section
		ArrayList<SummedMagFreqDist> mfdList = new ArrayList<SummedMagFreqDist>();
		for(FaultSectionPrefData data : dataList) {
			String name = data.getSectionName();
			SummedMagFreqDist mfd;
			if(mfd_ForSectName_Map.keySet().contains(name)) {
				mfd = mfd_ForSectName_Map.get(name);
			}
			else {
				mfd = getNewSummedMagFreqDist();
				mfd_ForSectName_Map.put(name, mfd);
			}
			mfdList.add(mfd);
		}
		
		// make sure lists are the same size
		if(mfdList.size() != dataList.size())
			throw new RuntimeException("Problem");
		
		// now process each rupture of the source
		for(ProbEqkRupture rup : src) {
			double[] fracOnEachSect = getFractionOfRupOnEachSection(rup, dataList);
			double mag = rup.getMag();
			for(int i=0;i<mfdList.size();i++) {
				if(fracOnEachSect[i] > 0)
					mfdList.get(i).addResampledMagRate(mag, fracOnEachSect[i]*rup.getMeanAnnualRate(duration), true);
			}
		}
	}
	
	
	
	private double[] getFractionOfRupOnEachSection(ProbEqkRupture rup, ArrayList<FaultSectionPrefData> dataList) {
		double[] fracOnEachSect = new double[dataList.size()];
		
		FaultTrace rupTrace = rup.getRuptureSurface().getEvenlyDiscritizedUpperEdge();
		
		// loop over each location on rupture trace
		for(int l=0;l<rupTrace.size();l++) {
			Location loc = rupTrace.get(l);
			double minDist = Double.MAX_VALUE;
			int minLocSectIndex = -1;
			for(int s=0;s<dataList.size();s++) {
				double dist = dataList.get(s).getFaultTrace().minDistToLine(loc);
				if(dist<minDist) {
					minDist=dist;
					minLocSectIndex = s;
				}
			}
			fracOnEachSect[minLocSectIndex] += 1d/(double)rupTrace.size();
		}
		
		// test sum
		double sum=0;
		for(double val:fracOnEachSect) sum += val;
		if(sum<0.99999 || sum>1.00001)
			throw new RuntimeException("Problem");
		
		return fracOnEachSect;
	}
	
	
	private SummedMagFreqDist getNewSummedMagFreqDist() {
		return new SummedMagFreqDist(5.05, 40, 0.1);
	}
	
	/**
	 * This writes out a list of all fault sections among all ERF branches
	 */
	public static void writeListOfAllFaultSections() {
		UCERF2_TimeIndependentEpistemicList ucerf2EpistemicList = getUCERF2_TimeIndependentEpistemicList();
		int numERFs = ucerf2EpistemicList.getNumERFs();
		System.out.println("Num Branches="+numERFs);
		ArrayList<String> sectionNames = new ArrayList<String>();
		ArrayList<Integer> sectionIDs = new ArrayList<Integer>();

		ArrayList<String> typeA_RupNamesList = new ArrayList<String>();

		for(int i=0; i<numERFs; ++i) {
//		for(int i=72; i<73; ++i) {
//			System.out.println("\nWeight of Branch "+i+"="+ucerf2EpistemicList.getERF_RelativeWeight(i));
//			System.out.println("Parameters of Branch "+i+":");
//			System.out.println(ucerf2EpistemicList.getParameterList(i).getParameterListMetadataString("\n"));
			System.out.println(i);
			ERF erf = ucerf2EpistemicList.getERF(i);
						
			// Get sections names for Type-A sections:
			ArrayList<Object> objList = ((UCERF2)erf).get_A_FaultSourceGenerators();
			for(Object obj: objList) { 
				if(obj instanceof A_FaultSegmentedSourceGenerator) { // Segmented source 
					A_FaultSegmentedSourceGenerator srcGen = (A_FaultSegmentedSourceGenerator)obj;
					FaultSegmentData segData = ((A_FaultSegmentedSourceGenerator)obj).getFaultSegmentData();
					ArrayList<FaultSectionPrefData> dataList = segData.getPrefFaultSectionDataList();
					for(FaultSectionPrefData data:dataList) {
						int id = data.getSectionId();
						if(!sectionIDs.contains(id)) {
							String name = data.getSectionName();
							if(sectionNames.contains(name)) // this is a double check
								throw new RuntimeException("Error - duplicate name but not id");
							sectionIDs.add(id);
							sectionNames.add(name);
						}
					}
				}
			}

			// now loop over sources to get the unsegmented sources
			for(int s=0;s<erf.getNumSources();s++) {
				ProbEqkSource src = erf.getSource(s);
				if(src instanceof UnsegmentedSource) {  // For Type-B & Unsegmented Type-A sources
					FaultSegmentData segData = ((UnsegmentedSource)src).getFaultSegmentData();
					ArrayList<FaultSectionPrefData> dataList = segData.getPrefFaultSectionDataList();
					for(FaultSectionPrefData data:dataList) {
						int id = data.getSectionId();
						if(!sectionIDs.contains(id)) {
							String name = data.getSectionName();
							if(sectionNames.contains(name)) // this is a double check
								throw new RuntimeException("Error - duplicate name but not id");
							sectionIDs.add(id);
							sectionNames.add(name);
						}
//						System.out.println(data.getSectionId()+"\t"+data.getSectionName());
					}
				}

			}
		}
		
		// write out results
		try {
			FileWriter fw = new FileWriter(DATAFILE);
			for(int i=0;i<sectionNames.size();i++) {
				System.out.println(sectionIDs.get(i)+"\t"+sectionNames.get(i));
				fw.write(sectionIDs.get(i)+"\t"+sectionNames.get(i)+"\n");
			}
			fw.close ();
		}
		catch (IOException e) {
			System.out.println ("IO exception = " + e );
		}
	}
	
	
	private void readListOfAllFaultSections() {
		sectionIDfromNameMap = new HashMap<String,Integer>();
		sectionNamefromID_Map = new HashMap<Integer,String>();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(DATAFILE));
			int l=-1;
			String line;
			while ((line = reader.readLine()) != null) {
				l+=1;
				String[] st = StringUtils.split(line,"\t");
				Integer id = Integer.valueOf(st[0]);
				String name = st[1];
				sectionIDfromNameMap.put(name, id);
				sectionNamefromID_Map.put(id,name);
			}
		} catch (Exception e) {
			ExceptionUtils.throwAsRuntimeException(e);
		}
		
		for(String name : sectionIDfromNameMap.keySet()){
			Integer id = sectionIDfromNameMap.get(name);
			System.out.println(id+"\t"+name);
			if(!name.equals(sectionNamefromID_Map.get(id)))
				throw new RuntimeException("Problem");
		}
	}
	
	
	/**
	 * This returns an instance with the background seismicity turned off
	 * @return
	 */
	public static UCERF2_TimeIndependentEpistemicList getUCERF2_TimeIndependentEpistemicList() {
		UCERF2_TimeIndependentEpistemicList ucerf2EpistemicList = new UCERF2_TimeIndependentEpistemicList();
		ucerf2EpistemicList.getAdjustableParameterList().getParameter(UCERF2.BACK_SEIS_NAME).setValue(UCERF2.BACK_SEIS_EXCLUDE);
		return ucerf2EpistemicList;
	}


	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		UCERF2_Source_MFDsCalc test = new UCERF2_Source_MFDsCalc();
		System.out.println("DONE");
		
//		writeListOfAllFaultSections();

		
	}

}
