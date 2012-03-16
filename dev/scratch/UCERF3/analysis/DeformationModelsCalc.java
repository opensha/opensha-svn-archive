package scratch.UCERF3.analysis;

import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.opensha.commons.data.function.HistogramFunction;
import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.LocationList;
import org.opensha.commons.geo.Region;
import org.opensha.commons.gui.plot.PlotLineType;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.opensha.sha.faultSurface.StirlingGriddedSurface;
import org.opensha.sha.gui.infoTools.GraphiWindowAPI_Impl;
import org.opensha.sha.gui.infoTools.PlotCurveCharacterstics;
import org.opensha.sha.magdist.GutenbergRichterMagFreqDist;

import scratch.UCERF3.enumTreeBranches.DeformationModels;
import scratch.UCERF3.enumTreeBranches.FaultModels;
import scratch.UCERF3.inversion.InversionConfiguration;
import scratch.UCERF3.utils.DeformationModelFetcher;
import scratch.UCERF3.utils.UCERF3_DataUtils;
import scratch.UCERF3.utils.FindEquivUCERF2_Ruptures.FindEquivUCERF2_FM3_Ruptures;

public class DeformationModelsCalc {
	
	public static void plotDDW_AndLowerSeisDepthDistributions(List<FaultSectionPrefData> subsectData, String plotTitle) {
		
		HistogramFunction origDepthsHist = new HistogramFunction(0.5, 70, 1.0);
		HistogramFunction reducedDDW_Hist = new HistogramFunction(0.5, 70, 1.0);
		
		ArrayList<String> largeValuesInfoLSD = new ArrayList<String>();
		ArrayList<String> largeValuesInfoDDW = new ArrayList<String>();
		
		double meanLSD=0;
		double meanDDW=0;
		double meanLowerMinusUpperSeisDepth=0;
		int num=0;
		
		for(FaultSectionPrefData data : subsectData) {
			num+=1;
			
			meanLSD+= data.getAveLowerDepth();
			origDepthsHist.add(data.getAveLowerDepth(), 1.0);
			if(data.getAveLowerDepth()>25.0) {
				String info = data.getParentSectionName()+"\tLowSeeisDep = "+Math.round(data.getAveLowerDepth());
				if(!largeValuesInfoLSD.contains(info)) largeValuesInfoLSD.add(info);
			}
			meanDDW += data.getReducedDownDipWidth();
			meanLowerMinusUpperSeisDepth += (1.0-data.getAseismicSlipFactor())*(data.getAveLowerDepth()-data.getOrigAveUpperDepth());
			reducedDDW_Hist.add(data.getReducedDownDipWidth(), 1.0);
			if(data.getReducedDownDipWidth()>25.0) {
				String info = data.getParentSectionName()+"\tDownDipWidth = "+Math.round(data.getReducedDownDipWidth());
				if(!largeValuesInfoDDW.contains(info)) largeValuesInfoDDW.add(info);
			}
		}
		
		meanLSD /= num;
		meanDDW /= num;
		meanLowerMinusUpperSeisDepth /= num;
		
		System.out.println("meanLowerMinusUpperSeisDepth="+Math.round(meanLowerMinusUpperSeisDepth));
		
		origDepthsHist.normalizeBySumOfY_Vals();
		origDepthsHist.setName("Distribution of Lower Seis. Depths; mean = "+Math.round(meanLSD));
		String infoLSW = "(among all fault subsections, and not influcenced by aseismicity)\n\nValues greater than 25km:\n\n";
		for(String info:largeValuesInfoLSD)
			infoLSW += "\t"+ info+"\n";
		origDepthsHist.setInfo(infoLSW);

		reducedDDW_Hist.normalizeBySumOfY_Vals();
		reducedDDW_Hist.setName("Distribution of Down-Dip Widths; mean = "+Math.round(meanDDW));
		String infoDDW = "(among all fault subsections, and reduced by aseismicity)\n\nValues greater than 25km:\n\n";
		for(String info:largeValuesInfoDDW)
			infoDDW += "\t"+ info+"\n";
		reducedDDW_Hist.setInfo(infoDDW);

		
		ArrayList<HistogramFunction> hists = new ArrayList<HistogramFunction>();
		hists.add(origDepthsHist);
		hists.add(reducedDDW_Hist);
		
//		ArrayList<PlotCurveCharacterstics> list = new ArrayList<PlotCurveCharacterstics>();
//		list.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, Color.BLACK));
//		list.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, Color.BLUE));
		
		GraphiWindowAPI_Impl graph = new GraphiWindowAPI_Impl(hists, plotTitle); 
		graph.setX_AxisLabel("Depth or Width (km)");
		graph.setY_AxisLabel("Normalized Number");

		
	}
	
	/**
	 * This calculates the total moment rate for a given list of section data
	 * @param sectData
	 * @param creepReduced
	 * @return
	 */
	public static double calculateTotalMomentRate(List<FaultSectionPrefData> sectData, boolean creepReduced) {
		double totMoRate=0;
		for(FaultSectionPrefData data : sectData) {
			double moRate = data.calcMomentRate(creepReduced);
			if(!Double.isNaN(moRate))
				totMoRate += moRate;
		}
		return totMoRate;
	}
	
	
	/**
	 * These cannot yet be subsections
	 * @param sectData
	 */
	public static void testFaultZonePolygons() {
		
		
		ArrayList<FaultSectionPrefData> sectData =  FaultModels.FM3_1.fetchFaultSections();
		sectData.addAll(FaultModels.FM3_2.fetchFaultSections());
						
		ArrayList<String> nullNames = new ArrayList<String>();
		ArrayList<String> outsideZoneNames = new ArrayList<String>();
		ArrayList<String> goodZoneNames = new ArrayList<String>();

		for(FaultSectionPrefData data: sectData){
			Region zone = data.getZonePolygon();
			if(zone == null) {
				if(!nullNames.contains(data.getSectionName()))
					nullNames.add(data.getSectionName());
			}
			else {
				LocationList surfLocs = data.getStirlingGriddedSurface(1.0).getEvenlyDiscritizedListOfLocsOnSurface();
				boolean good = true;
				for(Location loc : surfLocs) {
					if(!zone.contains(loc)) {
						double dist = zone.distanceToLocation(loc);
						if(dist>0.5) {
							if(!outsideZoneNames.contains(data.getSectionName()))
								outsideZoneNames.add(data.getSectionName()+"\t\tLoc that's outside:"+(float)loc.getLatitude()+"\t"+(float)loc.getLongitude());
							good = false;
							break;							
						}
					}
				}
				if(good == true) {
					if(!goodZoneNames.contains(data.getSectionName()))
						goodZoneNames.add(data.getSectionName());

				}
			}
		}
		
		System.out.println("\nThese sections have null fault zone polygons\n");
		for(String name : nullNames)
			System.out.println("\t"+name);
		
		System.out.println("\nThese sections have surface points outside the fault zone polygon\n");
		for(String name : outsideZoneNames)
			System.out.println("\t"+name);
		
		System.out.println("\nThese sections are good (have all surface points inside the fault zone polygon)\n");
		for(String name : goodZoneNames)
			System.out.println("\t"+name);
	}
	
	
	
	private static String getTableLineForMoRateAndMmaxDataForDefModels(FaultModels fm, DeformationModels dm) {
		DeformationModelFetcher defFetch = new DeformationModelFetcher(fm, dm, UCERF3_DataUtils.DEFAULT_SCRATCH_DATA_DIR);
		double moRate = calculateTotalMomentRate(defFetch.getSubSectionList(),true);
		System.out.println(fm.getName()+", "+dm.getName()+ " (reduced):\t"+(float)moRate);
		System.out.println(fm.getName()+", "+dm.getName()+ " (not reduced):\t"+(float)calculateTotalMomentRate(defFetch.getSubSectionList(),false));
		double fractOff = InversionConfiguration.findMomentFractionOffFaults(null, fm, dm, 1d);
		double totMoRate = calcTotalMomentRate(moRate,fractOff);
		GutenbergRichterMagFreqDist targetMFD = new GutenbergRichterMagFreqDist(0.0005,9.9995,10000);
		targetMFD.setAllButMagUpper(0.0005, totMoRate, 854000, 1.0, true);
		
		// now get moment rate of new faults only
		ArrayList<String> getEquivUCERF2_SectionNames = FindEquivUCERF2_FM3_Ruptures.getAllSectionNames(fm);
		ArrayList<FaultSectionPrefData> newSectionData = new ArrayList<FaultSectionPrefData>();
		for(FaultSectionPrefData data:defFetch.getSubSectionList())
			if (!getEquivUCERF2_SectionNames.contains(data.getParentSectionName()))
				newSectionData.add(data);
		double newFaultMoRate = calculateTotalMomentRate(newSectionData,true);
		
		
		System.out.println("totMoRate="+(float)totMoRate+"\tgetTotalMomentRate()="+(float)targetMFD.getTotalMomentRate()+"\tMgt4rate="+(float)targetMFD.getCumRate(4.0005)+
				"\tupperMag="+targetMFD.getMagUpper()+"\tMgt8rate="+(float)targetMFD.getCumRate(8.0005));
		return dm+"\t"+(float)(moRate/1e19)+"\t"+(float)fractOff+"\t"+(float)((totMoRate-moRate)/1e19)+"\t"+(float)(totMoRate/1e19)+"\t"+
				(float)targetMFD.getMagUpper()+"\t"+(float)targetMFD.getCumRate(8.0005)+"\t"+(float)(1.0/targetMFD.getCumRate(8.0005))+
						"\t"+(float)(newFaultMoRate/1e19);
	}
	
	
	
	public static void calcMoRateAndMmaxDataForDefModels() {
		
		ArrayList<String> tableData= new ArrayList<String>();
		FaultModels fm = FaultModels.FM3_1;
		
		tableData.add(getTableLineForMoRateAndMmaxDataForDefModels(fm,DeformationModels.ABM));
		tableData.add(getTableLineForMoRateAndMmaxDataForDefModels(fm,DeformationModels.GEOLOGIC));
		tableData.add(getTableLineForMoRateAndMmaxDataForDefModels(fm,DeformationModels.GEOLOGIC_PLUS_ABM));
		tableData.add(getTableLineForMoRateAndMmaxDataForDefModels(fm,DeformationModels.NEOKINEMA));
		tableData.add(getTableLineForMoRateAndMmaxDataForDefModels(fm,DeformationModels.ZENG));
		tableData.add(getTableLineForMoRateAndMmaxDataForDefModels(fm,DeformationModels.GEOBOUND));

		System.out.println("\nmodel\tfltMoRate\tfractOff\tmoRateOff\ttotMoRate\tMmax\tRate_gtM8\tMRIgtM8\tnewFltMoRate");
		for(String tableLine : tableData)
			System.out.println(tableLine);
				
	}
	
	/**
	 * This computes the total moment rate for the given arguments
	 * @param moRateOnFaults
	 * @param fractMoRateOffFaults
	 * @return
	 */
	public static double calcTotalMomentRate(double moRateOnFaults, double fractMoRateOffFaults) {
		return 	moRateOnFaults*fractMoRateOffFaults/(1-fractMoRateOffFaults)+moRateOnFaults;

	}
	
	
	/**
	 * This writes out the names of sections that are new to UCERF3 (either the section was added or it now
	 * has a slip rate from the Geologic deformation model, as UCERF2 ignored sections with no slip rate)
	 */
	public static void writeListOfNewFaultSections() {
		
		// get section name from FM 3.1
		ArrayList<String> fm3_sectionNamesList = new ArrayList<String>();
		DeformationModelFetcher defFetch = new DeformationModelFetcher(FaultModels.FM3_1, DeformationModels.GEOLOGIC, UCERF3_DataUtils.DEFAULT_SCRATCH_DATA_DIR);
		for(FaultSectionPrefData data : defFetch.getSubSectionList())
			if(!fm3_sectionNamesList.contains(data.getParentSectionName()))
				fm3_sectionNamesList.add(data.getParentSectionName());
		// add those from FM 3.2
		defFetch = new DeformationModelFetcher(FaultModels.FM3_2, DeformationModels.GEOLOGIC, UCERF3_DataUtils.DEFAULT_SCRATCH_DATA_DIR);
		for(FaultSectionPrefData data : defFetch.getSubSectionList())
			if(!fm3_sectionNamesList.contains(data.getParentSectionName()))
				fm3_sectionNamesList.add(data.getParentSectionName());
		
		// Get those that existed in UCERF2
		ArrayList<String> equivUCERF2_SectionNames = FindEquivUCERF2_FM3_Ruptures.getAllSectionNames(FaultModels.FM3_1);
		ArrayList<String> equivUCERF2_SectionNamesTemp = FindEquivUCERF2_FM3_Ruptures.getAllSectionNames(FaultModels.FM3_2);
		for(String name: equivUCERF2_SectionNamesTemp)
			if(!equivUCERF2_SectionNames.contains(name))
				equivUCERF2_SectionNames.add(name);
		
		// now make the list of new names
		ArrayList<String> newSectionName = new ArrayList<String>();
		for(String name : fm3_sectionNamesList)
			if (!equivUCERF2_SectionNames.contains(name))
				newSectionName.add(name);
		
		for(String name: newSectionName)
			System.out.println(name);
		System.out.println("There are "+newSectionName.size()+" new sections listed above");


	}
	
	
	/**
	 * This plots a historgram of fractional reductions due to creep,
	 * and also calculates some things.
	 * @param fm
	 * @param dm
	 */
	public static void plotMoRateReductionHist(FaultModels fm, DeformationModels dm) {
		DeformationModelFetcher defFetch = new DeformationModelFetcher(fm, dm, UCERF3_DataUtils.DEFAULT_SCRATCH_DATA_DIR);
		HistogramFunction moRateReductionHist = new HistogramFunction(0d, 51, 0.02);

		double totNoReduction=0, totWithReductionNotRedeced=0, totWithReductionRedeced=0;
		double straightAve=0;
		ArrayList<String> parantNamesOfReduced = new ArrayList<String>();
		int numReduced=0, numNotReduced=0;
		for(FaultSectionPrefData data : defFetch.getSubSectionList()) {
			double ratio = data.calcMomentRate(true)/data.calcMomentRate(false);
			if(!Double.isNaN(ratio)) {
				moRateReductionHist.add(ratio, 1.0);
				if(moRateReductionHist.getClosestXIndex(ratio) == moRateReductionHist.getNum()-1) {	// no reduction
					totNoReduction += data.calcMomentRate(false);
					numNotReduced += 1;
				}
				else if (!data.getParentSectionName().equals("San Andreas (Creeping Section) 2011 CFM")) {
//				else {
					totWithReductionNotRedeced += data.calcMomentRate(false);
					totWithReductionRedeced += data.calcMomentRate(true);
					straightAve += data.calcMomentRate(true)/data.calcMomentRate(false);
					numReduced+=1;
					if(!parantNamesOfReduced.contains(data.getParentSectionName()))
						parantNamesOfReduced.add(data.getParentSectionName());
				}
			}
		}
		straightAve /= numReduced;
		double percReduced = 100d*(double)numReduced/(double)(numNotReduced+numReduced);
		double aveRatio = totWithReductionRedeced/totWithReductionNotRedeced;
		System.out.println(numReduced+" out of "+(numReduced+numNotReduced)+" subsections were reduced; ("+(float)percReduced+")");
		System.out.println("totNoReduction="+(float)totNoReduction);
		System.out.println("totWithReductionNotRedeced="+(float)totWithReductionNotRedeced);
		System.out.println("aveRatio="+(float)aveRatio);
		System.out.println("straightAve="+(float)straightAve);
		System.out.println("potential further reduction ((1.0-aveRatio)*totNoReduction)"+(float)((1.0-aveRatio)*totNoReduction));
		
		GraphiWindowAPI_Impl graph = new GraphiWindowAPI_Impl(moRateReductionHist, "Moment Rate Reduction Histogram"); 
		graph.setX_AxisLabel("Fractional Reduction (due to creep)");
		graph.setY_AxisLabel("Number of Fault Sub-Sections");
		
		System.out.println("Parent Names of those reduced");
		for(String name: parantNamesOfReduced)
			System.out.println("\t"+name);


		
	}

	
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		testFaultZonePolygons();
		
//		writeListOfNewFaultSections();
		
//		File default_scratch_dir = new File(UCERF3_DataUtils.DEFAULT_SCRATCH_DATA_DIR, "FaultSystemRupSets");
		
//		plotMoRateReductionHist(FaultModels.FM3_1,DeformationModels.GEOLOGIC);
		
//		calcMoRateAndMmaxDataForDefModels();
		
//		DeformationModelFetcher defFetch = new DeformationModelFetcher(FaultModels.FM3_1,
//				DeformationModels.GEOLOGIC,default_scratch_dir);
//		System.out.println("GEOLOGIC moment Rate (reduced):\t"+(float)calculateTotalMomentRate(defFetch.getSubSectionList(),true));
//		System.out.println("GEOLOGIC moment Rate (not reduced):\t"+(float)calculateTotalMomentRate(defFetch.getSubSectionList(),false));
//
//		defFetch = new DeformationModelFetcher(FaultModels.FM3_1,
//				DeformationModels.GEOLOGIC_PLUS_ABM,default_scratch_dir);
//		System.out.println("GEOLOGIC_PLUS_ABM moment Rate (reduced):\t"+(float)calculateTotalMomentRate(defFetch.getSubSectionList(),true));
//		System.out.println("GEOLOGIC_PLUS_ABM moment Rate (not reduced):\t"+(float)calculateTotalMomentRate(defFetch.getSubSectionList(),false));
//
//		defFetch = new DeformationModelFetcher(FaultModels.FM3_1,
//				DeformationModels.ABM,default_scratch_dir);
//		System.out.println("ABM moment Rate (reduced):\t"+(float)calculateTotalMomentRate(defFetch.getSubSectionList(),true));
//		System.out.println("ABM moment Rate (not reduced):\t"+(float)calculateTotalMomentRate(defFetch.getSubSectionList(),false));

		
		
//		DeformationModelFetcher defFetch = new DeformationModelFetcher(FaultModels.FM3_1,
//				DeformationModels.GEOLOGIC_PLUS_ABM,default_scratch_dir);
//		plotDDW_AndLowerSeisDepthDistributions(defFetch.getSubSectionList(),"FM3_1 & GEOLOGIC_PLUS_ABM");
	}

}
