package scratch.UCERF3.utils.UCERF2_Section_MFDs;

import java.awt.Color;
import java.io.File;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.opensha.commons.calc.FractileCurveCalculator;
import org.opensha.commons.data.function.EvenlyDiscretizedFunc;
import org.opensha.commons.data.function.XY_DataSet;
import org.opensha.commons.data.function.XY_DataSetList;
import org.opensha.commons.geo.Location;
import org.opensha.commons.gui.plot.PlotLineType;
import org.opensha.commons.gui.plot.PlotSymbol;
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
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.MeanUCERF2.MeanUCERF2;
import org.opensha.sha.faultSurface.FaultTrace;
import org.opensha.sha.gui.infoTools.HeadlessGraphPanel;
import org.opensha.sha.gui.infoTools.PlotCurveCharacterstics;
import org.opensha.sha.magdist.SummedMagFreqDist;


import scratch.UCERF3.SimpleFaultSystemSolution;
import scratch.UCERF3.enumTreeBranches.FaultModels;
import scratch.UCERF3.inversion.UCERF2_ComparisonSolutionFetcher;


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
	String MFD_PLOTS_DIR = "dev/scratch/UCERF3/data/scratch/UCERF2_SectionMFDs";
	HashMap<String,Integer> sectionIDfromNameMap;
	HashMap<Integer,String> sectionNamefromID_Map;
	
	// this will hold a list of MFDs for each fault section
	HashMap<Integer,XY_DataSetList> mfdList_ForSectID_Map;
	// this will hold the ERF index number associated with each MFD
	HashMap<Integer,ArrayList<Integer>> mfdBranches_ForSectID_Map;
	// this will hold the ERF branch weight associated with each MFD
	HashMap<Integer,ArrayList<Double>> mfdWts_ForSectID_Map;
	// this holds the total weight for each section (which equals the probability of existance)
	HashMap<Integer,Double> totWtForSectID_Map;
	
	// A map giving a list of fault sections for each segmented type-A source
//	HashMap<String,ArrayList<FaultSectionPrefData>> sectionsForTypeA_RupsMap;


	// MFDs for meanUCERF2 
	HashMap<String,SummedMagFreqDist> meanUCERF2_MFD_ForSectID_Map;
	
	boolean isParticipation;
	
	final public static double MIN_MAG = 5.05;
	final public static int NUM_MAG = 40;
	final public static double DELTA_MAG = 0.1;

	
	/**
	 * 
	 * @param isParticipation - set true for participation MFDs and false for nucleation MFDs
	 */
	public UCERF2_Source_MFDsCalc(boolean isParticipation) {
		this.isParticipation = isParticipation;
		
		makeMFD_Lists();
		
//		writeSectionsWithWtLessThanOne();
		
		makeMeanUCERF2_MFD_List();
	}
	
	public void writeSectionsWithWtLessThanOne() {
		for(int parIndex:sectionNamefromID_Map.keySet()) {
			double wt = totWtForSectID_Map.get(parIndex);
			if(wt < 0.99999999)
				System.out.println((float)wt+"\t"+sectionNamefromID_Map.get(parIndex)+"\t"+parIndex);
		}
	}
	
	
	public void saveAllMFDPlot() {
		
		for(int sectID:sectionNamefromID_Map.keySet()) {
			ArrayList<XY_DataSet> funcs = new ArrayList<XY_DataSet>();
			FractileCurveCalculator frCurveCalc = new FractileCurveCalculator(mfdList_ForSectID_Map.get(sectID),
					mfdWts_ForSectID_Map.get(sectID));
			funcs.add(frCurveCalc.getMeanCurve());
			funcs.add(frCurveCalc.getMinimumCurve());
			funcs.add(frCurveCalc.getMaximumCurve());
			funcs.add(meanUCERF2_MFD_ForSectID_Map.get(sectionNamefromID_Map.get(sectID)));
			
			ArrayList<PlotCurveCharacterstics> chars = new ArrayList<PlotCurveCharacterstics>();
			chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, Color.BLUE));
			chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 1f, Color.BLUE));
			chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 1f, Color.BLUE));
			chars.add(new PlotCurveCharacterstics(PlotSymbol.CROSS, 3f, Color.BLACK));
			
			HeadlessGraphPanel gp = new HeadlessGraphPanel();
			gp.setTickLabelFontSize(14);
			gp.setAxisLabelFontSize(16);
			gp.setPlotLabelFontSize(18);
			gp.setYLog(true);
			gp.setRenderingOrder(DatasetRenderingOrder.FORWARD);
			gp.setUserBounds(5, 9, 1e-7, 1.0);
			float totWt=totWtForSectID_Map.get(sectID).floatValue();
			String title;
			if(isParticipation)
				title = sectionNamefromID_Map.get(sectID)+" Participation MFDs (totWt="+totWt+")";
			else
				title = sectionNamefromID_Map.get(sectID)+" Nucleation MFDs (totWt="+totWt+")";

			gp.drawGraphPanel("Magnitude", "Rate (per year)", funcs, chars, true, title);

			String fileName = sectionNamefromID_Map.get(sectID).replace("\\s+","");
			File file = new File(MFD_PLOTS_DIR, fileName);
			gp.getCartPanel().setSize(1000, 800);
			try {
				gp.saveAsPDF(file.getAbsolutePath()+".pdf");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
//			gp.saveAsPNG(file.getAbsolutePath()+".png");

		}

	}
	
	
	public static void tempTest() {
		// get the UCERF2 mapped fault system solution
		SimpleFaultSystemSolution fltSysSol = UCERF2_ComparisonSolutionFetcher.getUCERF2Solution(FaultModels.FM2_1);
		
		int subsect=216;
		System.out.println(fltSysSol.getFaultSectionData(subsect).getSectionName());
		System.out.println(fltSysSol.getFaultSectionData(234).getSectionName());
//		for(int r=0; r<fltSysSol.getNumRuptures();r++) {
//			List<Integer>  subsectIDs = fltSysSol.getSectionsIndicesForRup(r);
//			if(subsectIDs.get(0) == subsect || subsectIDs.get(subsectIDs.size()-1) == subsect) {
//				System.out.println(r+"\t"+fltSysSol.getMagForRup(r)+"\t"+(float)fltSysSol.getRateForRup(r)+"\t"+
//						subsectIDs.get(0)+"\t"+subsectIDs.get(subsectIDs.size()-1));
//			}
//		}
		
//		245	Death Valley (Black Mtns Frontal)
//		45	Death Valley (No of Cucamongo)
//		46	Death Valley (No)
//		246	Death Valley (So)
		
//		int parID = 245;
//		
//		for(int rupID:fltSysSol.getRupturesForParentSection(245)){
//			List<Integer>  subsectIDs = fltSysSol.getSectionsIndicesForRup(rupID);
//			System.out.println(rupID+"\t"+fltSysSol.getMagForRup(rupID)+"\t"+(float)fltSysSol.getRateForRup(rupID)+"\t"+
//					subsectIDs.get(0)+"\t"+subsectIDs.get(subsectIDs.size()-1));
//		}
		
//		EvenlyDiscretizedFunc mappedMFD;
//		mappedMFD = fltSysSol.calcParticipationMFD_forParentSect(parID, MIN_MAG, MIN_MAG+DELTA_MAG*(NUM_MAG-1), NUM_MAG);
//		mappedMFD.setName("UCERF2 Mapped Participation MFD for "+parID);
//		
//		ArrayList<Integer> rupIDs = new ArrayList<Integer>();
//		for(FaultSectionPrefData data: fltSysSol.getFaultSectionDataList()) {
//			if(data.getParentSectionId() == parID) {
//				for(Integer rupID : fltSysSol.getRupturesForSection(data.getSectionId())) {
//					if(!rupIDs.contains(rupID)) rupIDs.add(rupID);
//				}
//			}
//		}
//		SummedMagFreqDist testMFD = new SummedMagFreqDist(MIN_MAG, NUM_MAG, DELTA_MAG);
//		for(int rupID:rupIDs) {
//			testMFD.addResampledMagRate(fltSysSol.getMagForRup(rupID), fltSysSol.getRateForRup(rupID), true);
//		}
//		
//		System.out.println(mappedMFD);
//		System.out.println(testMFD);


	}
	
	
	/**
	 * Note that step-overs on San Jacinto and Elsinore are not taken care of
	 */
	public void saveAllMFDPlotComparisonsWithMappedUCERF2_FM2pt1_FltSysSol() {
		
		// get the UCERF2 mapped fault system solution
		SimpleFaultSystemSolution fltSysSol = UCERF2_ComparisonSolutionFetcher.getUCERF2Solution(FaultModels.FM2_1);
		
		// get list of parent section IDs
		ArrayList<Integer> u2_parIds = new ArrayList<Integer>();
		for(FaultSectionPrefData data : fltSysSol.getFaultSectionDataList()) {
			int parID = data.getParentSectionId();
			if(!u2_parIds.contains(parID)) {
				// check that we have this one here
				if(sectionNamefromID_Map.keySet().contains(parID))
					u2_parIds.add(parID);
				else {
					System.out.println("Not including "+data.getParentSectionName());

				}
				
			}
		}
		
		for(Integer parID : u2_parIds) {
			System.out.println("Working on "+sectionNamefromID_Map.get(parID));
			
			EvenlyDiscretizedFunc mappedMFD;
			if(isParticipation) {
				mappedMFD = fltSysSol.calcParticipationMFD_forParentSect(parID, MIN_MAG, MIN_MAG+DELTA_MAG*(NUM_MAG-1), NUM_MAG).getCumRateDistWithOffset();
				mappedMFD.setName("UCERF2 Mapped Participation MFD for "+sectionNamefromID_Map.get(parID));
			}
			else {
				mappedMFD = fltSysSol.calcNucleationMFD_forParentSect(parID, MIN_MAG, MIN_MAG+DELTA_MAG*(NUM_MAG-1), NUM_MAG).getCumRateDistWithOffset();
				mappedMFD.setName("UCERF2 Mapped Nucleation MFD for "+sectionNamefromID_Map.get(parID));				
			}
			
			// apply 50% weight if that's what totWtForSectID_Map says it is (indicating it's either exclusive to FM 2.1 or 2.2)
			// this is neede for a meaningful comparison
			double tempWt = totWtForSectID_Map.get(parID);
			if(tempWt>0.49 && tempWt<0.51)	// to avoid numerical precision problems
				mappedMFD.scale(0.5);
				
			ArrayList<XY_DataSet> funcs = new ArrayList<XY_DataSet>();
			funcs.add(mappedMFD);
			ArrayList<PlotCurveCharacterstics> chars = new ArrayList<PlotCurveCharacterstics>();
			chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, PlotSymbol.CROSS, 3f, Color.RED));

			
			XY_DataSetList mfdList = mfdList_ForSectID_Map.get(parID);
			if(mfdList != null) {
				XY_DataSetList cumMFD_List = new XY_DataSetList();
				for(XY_DataSet mfd:mfdList)
					cumMFD_List.add(((SummedMagFreqDist)mfd).getCumRateDistWithOffset());
				FractileCurveCalculator frCurveCalc = new FractileCurveCalculator(cumMFD_List,mfdWts_ForSectID_Map.get(parID));
				funcs.add(frCurveCalc.getMeanCurve());
				funcs.add(frCurveCalc.getMinimumCurve());
				funcs.add(frCurveCalc.getMaximumCurve());				
			}
			else {
				System.out.println("Null MFD List for: id = "+parID+";  name = "+sectionNamefromID_Map.get(parID));
			}
			
			chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, Color.BLUE));
			chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 1f, Color.BLUE));
			chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 1f, Color.BLUE));
			
			HeadlessGraphPanel gp = new HeadlessGraphPanel();
			gp.setTickLabelFontSize(14);
			gp.setAxisLabelFontSize(16);
			gp.setPlotLabelFontSize(18);
			gp.setYLog(true);
			gp.setRenderingOrder(DatasetRenderingOrder.FORWARD);
			gp.setUserBounds(5, 9, 1e-7, 1.0);
			float totWt=totWtForSectID_Map.get(parID).floatValue();
			String title;
			if(isParticipation)
				title = sectionNamefromID_Map.get(parID)+" Participation Cum MFDs (totWt="+totWt+")";
			else
				title = sectionNamefromID_Map.get(parID)+" Nucleation Cum MFDs (totWt="+totWt+")";

			gp.drawGraphPanel("Magnitude", "Rate (per year)", funcs, chars, true, title);

			String fileName = sectionNamefromID_Map.get(parID).replace("\\s+","");
			File file = new File(MFD_PLOTS_DIR, fileName);
			gp.getCartPanel().setSize(1000, 800);
			try {
				gp.saveAsPDF(file.getAbsolutePath()+".pdf");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
//			gp.saveAsPNG(file.getAbsolutePath()+".png");

		}
		
		

	}


	
	private void makeMFD_Lists() {
		
		// populate sectionIDfromNameMap and sectionNamefromID_Map
		readListOfAllFaultSections();
		
		mfdList_ForSectID_Map = new HashMap<Integer,XY_DataSetList>();
		mfdBranches_ForSectID_Map = new HashMap<Integer,ArrayList<Integer>>();
		mfdWts_ForSectID_Map = new HashMap<Integer,ArrayList<Double>>();
		totWtForSectID_Map = new HashMap<Integer,Double>();
		
		UCERF2_TimeIndependentEpistemicList ucerf2EpistemicList = getUCERF2_TimeIndependentEpistemicList();
		int numERFs = ucerf2EpistemicList.getNumERFs();
		System.out.println("Num Branches="+numERFs);

		for(int branch=0; branch<numERFs; ++branch) {
//		for(int branch=0; branch<1; ++branch) {
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
						String srcName = srcGen.getFaultSegmentData().getFaultName()+";"+srcGen.getLongRupName(r);
						sectionsForTypeA_RupsMap.put(srcName, dataList);	// same data list for all sources of the given type-A fault
					}
				}
			}
			
//			System.out.println("Type-A fault segmented source names");
//			System.out.println(sectionsForTypeA_RupsMap.keySet());

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
						if(mfd_ForSectName_Map.keySet().contains(name)) { // actually, this shouldn't be in this list
							mfd = mfd_ForSectName_Map.get(name);
						}
						else {
							mfd = getNewSummedMagFreqDist();
							mfd_ForSectName_Map.put(name, mfd);
							mfd.setName(name);
							mfd.setInfo("Section ID = "+sectionIDfromNameMap.get(name));
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
//					else
//						System.out.println("Ignored source: " + name);
				}
			}
			
			// now populate the maps with the results of this branch
			double branchWt = ucerf2EpistemicList.getERF_RelativeWeight(branch);
			for(String name:mfd_ForSectName_Map.keySet()) {
				int id = sectionIDfromNameMap.get(name);
				if(!mfdList_ForSectID_Map.keySet().contains(id)) {
					mfdList_ForSectID_Map.put(id, new XY_DataSetList());
					mfdBranches_ForSectID_Map.put(id, new ArrayList<Integer>());
					mfdWts_ForSectID_Map.put(id, new ArrayList<Double>());

				}
				mfdList_ForSectID_Map.get(id).add(mfd_ForSectName_Map.get(name));
				mfdBranches_ForSectID_Map.get(id).add(branch);
				mfdWts_ForSectID_Map.get(id).add(branchWt);
			}
		}
				
		
		// now compute totWtForSectID_Map
		for(Integer id:mfdWts_ForSectID_Map.keySet()) {
			double totWt=0;
			for(Double wt:mfdWts_ForSectID_Map.get(id)) {
				totWt +=wt;
			}
			totWtForSectID_Map.put(id,totWt);
		}
		
		// add a zero MFD where the total wt is less than 1
		SummedMagFreqDist zeroMFD = getNewSummedMagFreqDist();
		for(Integer id:mfdWts_ForSectID_Map.keySet()) {
			double totWt=totWtForSectID_Map.get(id);
			if(totWt < 0.99999999) {
				mfdList_ForSectID_Map.get(id).add(zeroMFD);
				mfdWts_ForSectID_Map.get(id).add(1.0-totWt);
			}
		}

		
//		System.out.println(mfdList_ForSectID_Map.keySet().size());
//		System.out.println(mfdList_ForSectID_Map.get(1));
		
	}
	
	/**
	 * This method adds the given source to the participation MFDs of the given list of
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
				mfd.setName(name);
				mfd.setInfo("Section ID = "+sectionIDfromNameMap.get(name));

			}
			mfdList.add(mfd);
		}
		
		// make sure lists are the same size
		if(mfdList.size() != dataList.size())
			throw new RuntimeException("Problem");
		
		// now process each rupture of the source
		for(ProbEqkRupture rup : src) {
			double[] rateOnEachSect;
			
			if(isParticipation)
				rateOnEachSect = getSectionParticipationsForRup(rup, dataList);
			else
				rateOnEachSect = getFractionOfRupOnEachSection(rup, dataList);
			double mag = rup.getMag();
			for(int i=0;i<mfdList.size();i++) {
				if(rateOnEachSect[i] > 0)
					mfdList.get(i).addResampledMagRate(mag, rateOnEachSect[i]*rup.getMeanAnnualRate(duration), true);
			}
		}
	}
	
	
	/**
	 * This computes the fraction of nucleations for the given rupture on each 
	 * section in the dataList (assuming a uniform distribution of nucleation
	 * poriints)
	 * @param rup
	 * @param dataList
	 * @return
	 */
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
	
	
	/**
	 * This determines which sections of the given dataList are utilized by the given rupture
	 * 
	 * @param rup
	 * @param dataList
	 * @return double[] - one if utilized and zero if not
	 */
	private double[] getSectionParticipationsForRup(ProbEqkRupture rup, ArrayList<FaultSectionPrefData> dataList) {
		double[] partOfSect = new double[dataList.size()];
		
		FaultTrace rupTrace = rup.getRuptureSurface().getEvenlyDiscritizedUpperEdge();
		
		// loop over each location on rupture trace
		for(int l=0;l<rupTrace.size();l++) {
			Location loc = rupTrace.get(l);
			double minDist = Double.MAX_VALUE;
			int minLocSectIndex = -1;
			// find closest section for surface loc
			for(int s=0;s<dataList.size();s++) {
				double dist = dataList.get(s).getFaultTrace().minDistToLine(loc);
				if(dist<minDist) {
					minDist=dist;
					minLocSectIndex = s;
				}
			}
			partOfSect[minLocSectIndex] = 1.0;	// set that section participates
		}
		return partOfSect;
	}

	
	
	private SummedMagFreqDist getNewSummedMagFreqDist() {
		return new SummedMagFreqDist(MIN_MAG, NUM_MAG, DELTA_MAG);
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
	
	
	private void makeMeanUCERF2_MFD_List() {
		
		// make sectionsForTypeA_RupsMap
		UCERF2_TimeIndependentEpistemicList ucerf2EpistemicList = getUCERF2_TimeIndependentEpistemicList();
		ERF erf = ucerf2EpistemicList.getERF(0);
		double duration = erf.getTimeSpan().getDuration();
		// Make map of section list for each source for segmented Type-A sources (so we don't have to loop through all sections below):
		HashMap<String,ArrayList<FaultSectionPrefData>> sectionsForTypeA_RupsMap = new HashMap<String,ArrayList<FaultSectionPrefData>>();
		ArrayList<Object> objList = ((UCERF2)erf).get_A_FaultSourceGenerators();	// regrettably, this method returns different object types
		for(Object obj: objList) { 
			if(obj instanceof A_FaultSegmentedSourceGenerator) { // Segmented source (rather than an unsegmented source, which is the other possible type of object)
				A_FaultSegmentedSourceGenerator srcGen = (A_FaultSegmentedSourceGenerator)obj;	// cast for convenience below
				ArrayList<FaultSectionPrefData> dataList = srcGen.getFaultSegmentData().getPrefFaultSectionDataList();
				for(int r=0;r<srcGen.getNumRupSources();r++) {
					String srcName = srcGen.getFaultSegmentData().getFaultName()+";"+srcGen.getLongRupName(r);
					sectionsForTypeA_RupsMap.put(srcName, dataList);	// same data list for all sources of the given type-A fault
				}
			}
		}
		
//		System.out.println("keys for sectionsForTypeA_RupsMap");
//		for(String srcName:sectionsForTypeA_RupsMap.keySet())
//			System.out.println(srcName);

		
		meanUCERF2_MFD_ForSectID_Map = new HashMap<String,SummedMagFreqDist>();

		MeanUCERF2 meanUCERF2 = new MeanUCERF2();
		meanUCERF2.setParameter(UCERF2.PROB_MODEL_PARAM_NAME, UCERF2.PROB_MODEL_POISSON);
		meanUCERF2.setParameter(UCERF2.BACK_SEIS_NAME, UCERF2.BACK_SEIS_EXCLUDE);
		meanUCERF2.updateForecast();
		duration = meanUCERF2.getTimeSpan().getDuration();
		
		System.out.println(meanUCERF2.getAdjustableParameterList().toString());

		if(sectionsForTypeA_RupsMap == null)
			throw new RuntimeException("Error: sectionsForTypeA_RupsMap is null; need to run makeMFD_Lists()");

		// now loop over all sources
		for(int s=0;s<meanUCERF2.getNumSources();s++) {
			ProbEqkSource src = meanUCERF2.getSource(s);
			if(src instanceof UnsegmentedSource) {  // For Type-B & Unsegmented Type-A sources
				ArrayList<FaultSectionPrefData> dataList = ((UnsegmentedSource)src).getFaultSegmentData().getPrefFaultSectionDataList();

//				if(src.getName().equals("Death Valley Connected")) {
//					System.out.println(src.getName());
//					for(FaultSectionPrefData data:dataList)
//						System.out.println(data.getSectionName()+"\t"+data.getSectionId());
//				}


				if(dataList.size() == 1) { //it's a single-section type-B source
					String name = src.getName();
					// check that names are same
					if(!name.equals(dataList.get(0).getSectionName()))
						throw new RuntimeException("Problem");
					SummedMagFreqDist mfd;
					if(meanUCERF2_MFD_ForSectID_Map.keySet().contains(name)) { // actually, this shouldn't be in this list
						mfd = meanUCERF2_MFD_ForSectID_Map.get(name);
					}
					else {
						mfd = getNewSummedMagFreqDist();
						meanUCERF2_MFD_ForSectID_Map.put(name, mfd);
						mfd.setName(name);
						mfd.setInfo("Section ID = "+sectionIDfromNameMap.get(name));
					}
					for(ProbEqkRupture rup : src) {
						mfd.addResampledMagRate(rup.getMag(), rup.getMeanAnnualRate(duration), true);
					}
				}
				else {	// it's a stitched together unsegmented source
					processSource(src, dataList, meanUCERF2_MFD_ForSectID_Map, duration);
				}
			}
			else {
				String name = src.getName();
				if(sectionsForTypeA_RupsMap.keySet().contains(name)) {	// check whether it's a type A source
					processSource(src, sectionsForTypeA_RupsMap.get(name), meanUCERF2_MFD_ForSectID_Map, duration);
				}
				else
					System.out.println("Ignored source: " + name);
			}
		}
		
//		System.out.println("test: " + meanUCERF2_MFD_ForSectID_Map.get("Point Reyes"));

	}



	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		UCERF2_Source_MFDsCalc.tempTest();
//		UCERF2_Source_MFDsCalc test = new UCERF2_Source_MFDsCalc(true);
//		test.saveAllMFDPlotComparisonsWithMappedUCERF2_FM2pt1_FltSysSol();
		System.out.println("DONE");
		
//		writeListOfAllFaultSections();

		
	}

}
