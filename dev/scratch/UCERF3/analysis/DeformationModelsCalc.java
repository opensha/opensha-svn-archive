package scratch.UCERF3.analysis;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.opensha.commons.data.function.EvenlyDiscretizedFunc;
import org.opensha.commons.data.function.HistogramFunction;
import org.opensha.commons.data.function.XY_DataSet;
import org.opensha.commons.data.xyz.GriddedGeoDataSet;
import org.opensha.commons.data.xyz.XYZ_DataSetMath;
import org.opensha.commons.exceptions.Point2DException;
import org.opensha.commons.exceptions.XY_DataSetException;
import org.opensha.commons.geo.GriddedRegion;
import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.LocationList;
import org.opensha.commons.geo.Region;
import org.opensha.commons.gui.plot.PlotLineType;
import org.opensha.commons.gui.plot.PlotSymbol;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.opensha.sha.earthquake.calc.ERF_Calculator;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.UCERF2;
import org.opensha.sha.faultSurface.StirlingGriddedSurface;
import org.opensha.sha.gui.infoTools.GraphiWindowAPI_Impl;
import org.opensha.sha.gui.infoTools.PlotCurveCharacterstics;
import org.opensha.sha.magdist.GutenbergRichterMagFreqDist;

import scratch.UCERF3.enumTreeBranches.DeformationModels;
import scratch.UCERF3.enumTreeBranches.FaultModels;
import scratch.UCERF3.enumTreeBranches.InversionModels;
import scratch.UCERF3.enumTreeBranches.MaxMagOffFault;
import scratch.UCERF3.enumTreeBranches.ScalingRelationships;
import scratch.UCERF3.enumTreeBranches.SpatialSeisPDF;
import scratch.UCERF3.enumTreeBranches.TotalMag5Rate;
import scratch.UCERF3.inversion.InversionConfiguration;
import scratch.UCERF3.inversion.InversionFaultSystemRupSetFactory;
import scratch.UCERF3.utils.DeformationModelFetcher;
import scratch.UCERF3.utils.RELM_RegionUtils;
import scratch.UCERF3.utils.DeformationModelOffFaultMoRateData;
import scratch.UCERF3.utils.SmoothSeismicitySpatialPDF_Fetcher;
import scratch.UCERF3.utils.UCERF3_DataUtils;
import scratch.UCERF3.utils.FindEquivUCERF2_Ruptures.FindEquivUCERF2_FM3_Ruptures;
import scratch.UCERF3.utils.ModUCERF2.ModMeanUCERF2;
import scratch.UCERF3.utils.ModUCERF2.ModMeanUCERF2_FM2pt1;
import scratch.UCERF3.griddedSeismicity.FaultPolyMgr;
import scratch.UCERF3.griddedSeismicity.GriddedSeisUtils;

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
		
		double totWt=0;
		for(FaultSectionPrefData data : subsectData) {
			if(data.getAveLowerDepth()>25.0) {
//				System.out.println(data.toString());
				String info = data.getParentSectionName()+"\tLowSeisDep = "+Math.round(data.getAveLowerDepth());
				if(!largeValuesInfoLSD.contains(info)) largeValuesInfoLSD.add(info);
			}
			num+=1;
			meanLSD+= data.getAveLowerDepth();
			origDepthsHist.add(data.getAveLowerDepth(), 1.0);
			meanDDW += data.getReducedDownDipWidth();
			double wt = data.getReducedAveSlipRate();
			if(Double.isNaN(wt)) {
				System.out.println("NaN slip rate: "+data.getName());
				wt=0;
			}
			else {
				totWt += wt;
			}
			meanLowerMinusUpperSeisDepth += wt*(1.0-data.getAseismicSlipFactor())*(data.getAveLowerDepth()-data.getOrigAveUpperDepth());
			reducedDDW_Hist.add(data.getReducedDownDipWidth(), 1.0);
			if(data.getReducedDownDipWidth()>25.0) {
				String info = data.getParentSectionName()+"\tDownDipWidth = "+Math.round(data.getReducedDownDipWidth());
				if(!largeValuesInfoDDW.contains(info)) largeValuesInfoDDW.add(info);
			}
		}
		
		meanLSD /= num;
		meanDDW /= num;
//		meanLowerMinusUpperSeisDepth /= num;
		meanLowerMinusUpperSeisDepth /= totWt;
		
		System.out.println("meanLowerMinusUpperSeisDepth="+meanLowerMinusUpperSeisDepth);
		
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
	 * This tests whether any part of the surface fall outside the polygon?
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
	
	
	/**
	 * This computes the total moment rate on faults for the given deformation model
	 * @param fm
	 * @param dm
	 * @param creepReduced
	 * @return
	 */
	public static double calcFaultMoRateForDefModel(FaultModels fm, DeformationModels dm, boolean creepReduced) {
		DeformationModelFetcher defFetch = new DeformationModelFetcher(fm, dm, UCERF3_DataUtils.DEFAULT_SCRATCH_DATA_DIR, InversionFaultSystemRupSetFactory.DEFAULT_ASEIS_VALUE);
		return calculateTotalMomentRate(defFetch.getSubSectionList(),true);
	}
	
	/**
	 * This computes the total moment rate on faults for the given deformation model
	 * @param fm
	 * @param dm
	 * @param creepReduced
	 * @return
	 */
	public static double calcTotalMoRateForDefModel(FaultModels fm, DeformationModels dm, boolean creepReduced) {
		return calcFaultMoRateForDefModel(fm,dm,creepReduced)+calcMoRateOffFaultsForDefModel(fm,dm);
	}

	
	
	/**
	 * this returns the total off-fault moment rate for the given deformation model
	 * @param fm
	 * @param dm
	 * @return
	 */
	public static double calcMoRateOffFaultsForDefModel(FaultModels fm, DeformationModels dm) {
		DeformationModelOffFaultMoRateData offFaultData = DeformationModelOffFaultMoRateData.getInstance();
		return offFaultData.getTotalOffFaultMomentRate(fm, dm);
	}

	
	
	/**
	 * 
	 * @param fm
	 * @param dm
	 * @param rateM5 - rate of events great than or equal to M5
	 * @return
	 */
	private static String getTableLineForMoRateAndMmaxDataForDefModels(FaultModels fm, DeformationModels dm, double rateM5) {
		DeformationModelFetcher defFetch = new DeformationModelFetcher(fm, dm, UCERF3_DataUtils.DEFAULT_SCRATCH_DATA_DIR, InversionFaultSystemRupSetFactory.DEFAULT_ASEIS_VALUE);
		double moRate = calculateTotalMomentRate(defFetch.getSubSectionList(),true);
		System.out.println(fm.getName()+", "+dm.getName()+ " (reduced):\t"+(float)moRate);
		System.out.println(fm.getName()+", "+dm.getName()+ " (not reduced):\t"+(float)calculateTotalMomentRate(defFetch.getSubSectionList(),false));
		double moRateOffFaults = calcMoRateOffFaultsForDefModel(fm, dm);
		double totMoRate = moRate+moRateOffFaults;
		double fractOff = moRateOffFaults/totMoRate;
		GutenbergRichterMagFreqDist targetMFD = new GutenbergRichterMagFreqDist(0.0005,9.9995,10000);
		targetMFD.setAllButMagUpper(0.0005, totMoRate, rateM5*1e5, 1.0, true);
		
		// now get moment rate of new faults only
		ArrayList<String> getEquivUCERF2_SectionNames = FindEquivUCERF2_FM3_Ruptures.getAllSectionNames(fm);
		ArrayList<FaultSectionPrefData> newSectionData = new ArrayList<FaultSectionPrefData>();
		for(FaultSectionPrefData data:defFetch.getSubSectionList())
			if (!getEquivUCERF2_SectionNames.contains(data.getParentSectionName()))
				newSectionData.add(data);
		double newFaultMoRate = calculateTotalMomentRate(newSectionData,true);
		
		
		System.out.println("totMoRate="+(float)totMoRate+"\tgetTotalMomentRate()="+(float)targetMFD.getTotalMomentRate()+"\tMgt4rate="+(float)targetMFD.getCumRate(4.0005)+
				"\tupperMag="+targetMFD.getMagUpper()+"\tMgt8rate="+(float)targetMFD.getCumRate(8.0005));
		return dm+"\t"+(float)(moRate/1e19)+"\t"+(float)fractOff+"\t"+(float)(moRateOffFaults/1e19)+"\t"+(float)(totMoRate/1e19)+"\t"+
				(float)targetMFD.getMagUpper()+"\t"+(float)targetMFD.getCumRate(8.0005)+"\t"+(float)(1.0/targetMFD.getCumRate(8.0005))+
						"\t"+(float)(newFaultMoRate/1e19);
	}
	
	
	public static void writeMoRateOfParentSections(FaultModels fm, DeformationModels dm) {
		DeformationModelFetcher defFetch = new DeformationModelFetcher(fm, dm, UCERF3_DataUtils.DEFAULT_SCRATCH_DATA_DIR, InversionFaultSystemRupSetFactory.DEFAULT_ASEIS_VALUE);
		
		// get list of sections in UCERF2
		ArrayList<String> getEquivUCERF2_SectionNames = FindEquivUCERF2_FM3_Ruptures.getAllSectionNames(fm);

		String lastName = "";
		double moRateReduced=0, moRateNotReduced=0;
		System.out.println("Sect Name\t"+"moRateReduced\tmoRateNotReduced\tIn UCERF2?");

		for(FaultSectionPrefData data:defFetch.getSubSectionList())
			if(data.getParentSectionName().equals(lastName)) {
				moRateReduced += data.calcMomentRate(true);
				moRateNotReduced += data.calcMomentRate(false);
			}
			else {
				if(!lastName.equals("")) {
					System.out.println(lastName+"\t"+(float)moRateReduced+"\t"+(float)moRateNotReduced+"\t"+getEquivUCERF2_SectionNames.contains(lastName));
				}
				// set first values for new parent section
				moRateReduced=data.calcMomentRate(true);
				moRateNotReduced=data.calcMomentRate(false);
				lastName = data.getParentSectionName();
			}

	}
	
	
	public static void calcMoRateAndMmaxDataForDefModels() {
		
		double rateM5 = TotalMag5Rate.RATE_8p7.getRateMag5();
		
		ArrayList<String> tableData= new ArrayList<String>();
		FaultModels fm = FaultModels.FM3_1;
		
		tableData.add(getTableLineForMoRateAndMmaxDataForDefModels(fm,DeformationModels.ABM,rateM5));
		tableData.add(getTableLineForMoRateAndMmaxDataForDefModels(fm,DeformationModels.GEOLOGIC,rateM5));
//		tableData.add(getTableLineForMoRateAndMmaxDataForDefModels(fm,DeformationModels.GEOLOGIC_PLUS_ABM,rateM5));
		tableData.add(getTableLineForMoRateAndMmaxDataForDefModels(fm,DeformationModels.NEOKINEMA,rateM5));
		tableData.add(getTableLineForMoRateAndMmaxDataForDefModels(fm,DeformationModels.ZENG,rateM5));
//		tableData.add(getTableLineForMoRateAndMmaxDataForDefModels(fm,DeformationModels.GEOBOUND));

		System.out.println("\nmodel\tfltMoRate\tfractOff\tmoRateOff\ttotMoRate\tMmax\tRate_gtM8\tMRIgtM8\tnewFltMoRate");
		for(String tableLine : tableData)
			System.out.println(tableLine);
				
	}
	
	
	
	/**
	 * This writes out the names of sections that are new to UCERF3 (either the section was added or it now
	 * has a slip rate from the Geologic deformation model, as UCERF2 ignored sections with no slip rate)
	 */
	public static void writeListOfNewFaultSections() {
		
		// get section name from FM 3.1
		ArrayList<String> fm3_sectionNamesList = new ArrayList<String>();
		DeformationModelFetcher defFetch = new DeformationModelFetcher(FaultModels.FM3_1, DeformationModels.GEOLOGIC, UCERF3_DataUtils.DEFAULT_SCRATCH_DATA_DIR, InversionFaultSystemRupSetFactory.DEFAULT_ASEIS_VALUE);
		for(FaultSectionPrefData data : defFetch.getSubSectionList())
			if(!fm3_sectionNamesList.contains(data.getParentSectionName()))
				fm3_sectionNamesList.add(data.getParentSectionName());
		// add those from FM 3.2
		defFetch = new DeformationModelFetcher(FaultModels.FM3_2, DeformationModels.GEOLOGIC, UCERF3_DataUtils.DEFAULT_SCRATCH_DATA_DIR, InversionFaultSystemRupSetFactory.DEFAULT_ASEIS_VALUE);
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
	 * and also calculates some things (these latter things ignore the 
	 * creeping section for some reason).
	 * @param fm
	 * @param dm
	 */
	public static void plotMoRateReductionHist(FaultModels fm, DeformationModels dm) {
		DeformationModelFetcher defFetch = new DeformationModelFetcher(fm, dm, UCERF3_DataUtils.DEFAULT_SCRATCH_DATA_DIR, InversionFaultSystemRupSetFactory.DEFAULT_ASEIS_VALUE);
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
	 * This method makes the images used in Figure 8 of the preliminary model report.
	 */
	public static void plotAllSpatialMoRateMaps() {
		
//		ModMeanUCERF2_FM2pt1 erf = new ModMeanUCERF2_FM2pt1();
		ModMeanUCERF2 erf= new ModMeanUCERF2();
		erf.setParameter(UCERF2.PROB_MODEL_PARAM_NAME, UCERF2.PROB_MODEL_POISSON);
		erf.setParameter(UCERF2.FLOATER_TYPE_PARAM_NAME, UCERF2.FULL_DDW_FLOATER);

		erf.setParameter(UCERF2.BACK_SEIS_NAME, UCERF2.BACK_SEIS_ONLY);
		erf.updateForecast();
		GriddedGeoDataSet ucerf2_OffFault = ERF_Calculator.getMomentRatesInRegion(erf, RELM_RegionUtils.getGriddedRegionInstance());

		erf.setParameter(UCERF2.BACK_SEIS_NAME, UCERF2.BACK_SEIS_EXCLUDE);
		erf.updateForecast();
		GriddedGeoDataSet ucerf2_Faults = ERF_Calculator.getMomentRatesInRegion(erf, RELM_RegionUtils.getGriddedRegionInstance());

		// following was a test
//		erf.setParameter(UCERF2.BACK_SEIS_NAME, UCERF2.BACK_SEIS_INCLUDE);
//		erf.updateForecast();
//		System.out.println("Done updating ERF3");
//		GriddedGeoDataSet ucerf2_All = ERF_Calculator.getMomentRatesInRegion(erf, RELM_RegionUtils.getGriddedRegionInstance());

		GriddedGeoDataSet ucerf2_All = new GriddedGeoDataSet(RELM_RegionUtils.getGriddedRegionInstance(), true);

		double fltTest=0, offTest=0, allTest=0;
		for(int i=0;i<ucerf2_All.size();i++) {
			offTest += ucerf2_OffFault.get(i);
			fltTest += ucerf2_Faults.get(i);
			ucerf2_All.set(i, ucerf2_OffFault.get(i)+ucerf2_Faults.get(i));
			allTest += ucerf2_All.get(i);
		}
//		System.out.println((float)offTest+"\t"+(float)fltTest+"\t"+(float)allTest+"\t"+(float)(offTest+fltTest));
//		System.out.println("minMoRate="+(float)ucerf2_OffFault.getMinZ());
//		System.out.println("maxMoRate="+(float)ucerf2_All.getMaxZ());
		
		try {
			GMT_CA_Maps.plotSpatialMoRate_Map(ucerf2_Faults, "UCERF2 On-Fault MoRate-Nm/yr", " " , "UCERF2_OnFaultMoRateMap");
			GMT_CA_Maps.plotSpatialMoRate_Map(ucerf2_OffFault, "UCERF2 Off-Fault MoRate-Nm/yr", " " , "UCERF2_OffFaultMoRateMap");
			GMT_CA_Maps.plotSpatialMoRate_Map(ucerf2_All.copy(), "UCERF2 Total MoRate-Nm/yr", " " , "UCERF2_TotalMoRateMap");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		makeSpatialMoRateMaps(FaultModels.FM3_1, DeformationModels.ABM, ucerf2_All.copy());	// need the .copy() because method takes the log
		makeSpatialMoRateMaps(FaultModels.FM3_1, DeformationModels.NEOKINEMA, ucerf2_All.copy());
		makeSpatialMoRateMaps(FaultModels.FM3_1, DeformationModels.GEOBOUND, ucerf2_All.copy());
		makeSpatialMoRateMaps(FaultModels.FM3_1, DeformationModels.ZENG, ucerf2_All.copy());
		makeSpatialMoRateMaps(FaultModels.FM3_1, DeformationModels.GEOLOGIC, ucerf2_All.copy());
		makeSpatialMoRateMaps(FaultModels.FM3_1, DeformationModels.GEOLOGIC_PLUS_ABM, ucerf2_All.copy());
		
		
		// now make the smoothed seismicity implied moRate maps (assuming ABM has correct total)
		double totMoRate = calcTotalMoRateForDefModel(FaultModels.FM3_1, DeformationModels.ABM, true);
		GriddedGeoDataSet uncer2_SmSeisDist = SmoothSeismicitySpatialPDF_Fetcher.getUCERF2_PDF();
		for(int i=0;i< uncer2_SmSeisDist.size();i++)
			uncer2_SmSeisDist.set(i, totMoRate*uncer2_SmSeisDist.get(i));
		
		GriddedGeoDataSet uncer3_SmSeisDist = SmoothSeismicitySpatialPDF_Fetcher.getUCERF3_PDF();
		for(int i=0;i< uncer3_SmSeisDist.size();i++)
			uncer3_SmSeisDist.set(i, totMoRate*uncer3_SmSeisDist.get(i));
		
		try {
			GMT_CA_Maps.plotSpatialMoRate_Map(uncer3_SmSeisDist.copy(), "UCERF3_SmoothSeis MoRate-Nm/yr", " " , "UCERF3_SmSeisMoRateMap");
			GMT_CA_Maps.plotRatioOfRateMaps(uncer3_SmSeisDist, ucerf2_All.copy(), "UCERF3_SmoothSeis Ratio", " " , "UCERF3_SmoothSeisRatio");

			GMT_CA_Maps.plotSpatialMoRate_Map(uncer2_SmSeisDist.copy(), "UCERF2_SmoothSeis MoRate-Nm/yr", " " , "UCERF2_SmSeisMoRateMap");
			GMT_CA_Maps.plotRatioOfRateMaps(uncer2_SmSeisDist, ucerf2_All.copy(), "UCERF2_SmoothSeis Ratio", " " , "UCERF2_SmoothSeisRatio");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// adding ratios of some on-fault cases
		FaultModels fm=FaultModels.FM3_1;
		try {
			DeformationModels dm = DeformationModels.GEOLOGIC_PLUS_ABM;
			GriddedGeoDataSet onFaultGeoPlusABMData = getDefModFaultMoRatesInRELM_Region(fm, dm);
			GMT_CA_Maps.plotRatioOfRateMaps(onFaultGeoPlusABMData, ucerf2_Faults, dm+"On Fault Ratio", " " , dm.getShortName()+"_onFaultRatioMap");
			dm = DeformationModels.GEOLOGIC;
			GriddedGeoDataSet onFaultGeologicData = getDefModFaultMoRatesInRELM_Region(fm, dm);
			GMT_CA_Maps.plotRatioOfRateMaps(onFaultGeologicData, ucerf2_Faults, dm+"On Fault Ratio", " " , dm.getShortName()+"_onFaultRatioMap");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		
		
	}
	
	
	private static void makeSpatialMoRateMaps(FaultModels fm, DeformationModels dm, GriddedGeoDataSet refForRatioData) {
		DeformationModelOffFaultMoRateData spatPDFgen = DeformationModelOffFaultMoRateData.getInstance();
		
		GriddedGeoDataSet offFaultData = spatPDFgen.getDefModSpatialOffFaultMoRates(fm, dm);
		GriddedGeoDataSet onFaultData = getDefModFaultMoRatesInRELM_Region(fm, dm);
		GriddedGeoDataSet totalMoRateData = RELM_RegionUtils.getRELM_RegionGeoDataSetInstance();
		for(int i=0; i<totalMoRateData.size();i++)
			totalMoRateData.set(i, offFaultData.get(i)+onFaultData.get(i));

		System.out.println(dm+"\tmaxMoRate="+totalMoRateData.getMaxZ());
		System.out.println(dm+"\tminMoRate="+offFaultData.getMinZ());

		
		try {
			GMT_CA_Maps.plotSpatialMoRate_Map(offFaultData, dm+" MoRate-Nm/yr", " " , dm.getShortName()+"_OffFaultMoRateMap");
			GMT_CA_Maps.plotSpatialMoRate_Map(onFaultData, dm+" MoRate-Nm/yr", " " , dm.getShortName()+"_OnFaultMoRateMap");
			GMT_CA_Maps.plotSpatialMoRate_Map(totalMoRateData.copy(), dm+" MoRate-Nm/yr", " " , dm.getShortName()+"_TotalMoRateMap");
			GMT_CA_Maps.plotRatioOfRateMaps(totalMoRateData, refForRatioData, dm+" Ratio", " " , dm.getShortName()+"_RatioMap");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	/**
	 * This returns the average on-fault moment rates for the given fault model, where the average
	 * is taken over: ABM, NEOKINEMA, ZENG, and optionally GEOLOGIC..
	 * @param fm
	 */
	public static GriddedGeoDataSet getAveDefModSpatialOnFaultMomentRateData(FaultModels fm, boolean includeGeologic) {

		ArrayList<DeformationModels> dmListForAve = new ArrayList<DeformationModels>();
		if(includeGeologic) {
			dmListForAve.add(DeformationModels.GEOLOGIC);
		}
		dmListForAve.add(DeformationModels.ABM);
		dmListForAve.add(DeformationModels.NEOKINEMA);
		dmListForAve.add(DeformationModels.ZENG);

		GriddedGeoDataSet aveDefModOnFault = RELM_RegionUtils.getRELM_RegionGeoDataSetInstance();
		for(int i=0;i<aveDefModOnFault.size();i++) {// initialize to zero
			aveDefModOnFault.set(i,0);
		}
		for(DeformationModels dm : dmListForAve) {
			GriddedGeoDataSet onFaultData = getDefModFaultMoRatesInRELM_Region(fm, dm);
			for(int i=0;i<onFaultData.size();i++) {
				if(!Double.isNaN(onFaultData.get(i)))	// treat the Geo NaNs as zero
					aveDefModOnFault.set(i, aveDefModOnFault.get(i) + onFaultData.get(i)/dmListForAve.size());
			}
		}
		return aveDefModOnFault;
	}
	
	
	/**
	 * This returns the average moment rate data including both on- and off-fault sources,
	 * where the average is taken over  ABM, NEOKINEMA, ZENG, and optionally GEOLOGIC.
	 * 
	 * The off-fault geologic is included here (which has a uniform distribution off fault)
	 * @param fm
	 * @return
	 */
	public static GriddedGeoDataSet getAveDefModSpatialMomentRateData(FaultModels fm, boolean includeGeologic) {

		GriddedGeoDataSet aveDefModOnFault = getAveDefModSpatialOnFaultMomentRateData(fm, includeGeologic);
		GriddedGeoDataSet aveDefModOffFault = DeformationModelOffFaultMoRateData.getInstance().getAveDefModelSpatialOffFaultMoRates(fm, true);
		
		GriddedGeoDataSet aveDefModData = RELM_RegionUtils.getRELM_RegionGeoDataSetInstance();

		for(int i=0;i<aveDefModData.size();i++) {// initialize to zero
			aveDefModData.set(i,aveDefModOnFault.get(i)+aveDefModOffFault.get(i));
		}
		return aveDefModData;
	}
	
	/**
	 * This returns a pdf of the ave deformation model (including faults, both fault 
	 * models 3.1 and 3.2, and the following deformation models: ABM, NEOKINEMA, ZENG, 
	 * and optionally GEOLOGIC)
	 * @return
	 */
	public static GriddedGeoDataSet getAveDefModSpatialPDF_WithFaults(boolean includeGeologic) {
		GriddedGeoDataSet data1 = getAveDefModSpatialMomentRateData(FaultModels.FM3_1, includeGeologic);
		GriddedGeoDataSet data2 = getAveDefModSpatialMomentRateData(FaultModels.FM3_2, includeGeologic);
		GriddedGeoDataSet pdf = RELM_RegionUtils.getRELM_RegionGeoDataSetInstance();
		double sum=0;
		for(int i=0; i<pdf.size();i++) {
			pdf.set(i, data1.get(i)+data2.get(i));
			sum += pdf.get(i);
		}
		for(int i=0; i<pdf.size();i++) {
			pdf.set(i, pdf.get(i)/sum);
		}
		return pdf;
	}


	
	/**
	 * This was done for the May 8-9, 2012 review meeting (& final report analysis)
	 */
	public static void plotMoreSpatialMaps() {
		
		FaultModels fm = FaultModels.FM3_1;
		
		// make UCERF2 on, off, and total rates data
		ModMeanUCERF2 erf= new ModMeanUCERF2();
		erf.setParameter(UCERF2.PROB_MODEL_PARAM_NAME, UCERF2.PROB_MODEL_POISSON);
		erf.setParameter(UCERF2.FLOATER_TYPE_PARAM_NAME, UCERF2.FULL_DDW_FLOATER);
		erf.setParameter(UCERF2.BACK_SEIS_NAME, UCERF2.BACK_SEIS_ONLY);
		erf.updateForecast();
		GriddedGeoDataSet ucerf2_OffFault = ERF_Calculator.getMomentRatesInRegion(erf, RELM_RegionUtils.getGriddedRegionInstance());
		erf.setParameter(UCERF2.BACK_SEIS_NAME, UCERF2.BACK_SEIS_EXCLUDE);
		erf.updateForecast();
		GriddedGeoDataSet ucerf2_Faults = ERF_Calculator.getMomentRatesInRegion(erf, RELM_RegionUtils.getGriddedRegionInstance());
		GriddedGeoDataSet ucerf2_All = new GriddedGeoDataSet(RELM_RegionUtils.getGriddedRegionInstance(), true);
		double fltTest=0, offTest=0, allTest=0;
		for(int i=0;i<ucerf2_All.size();i++) {
			offTest += ucerf2_OffFault.get(i);
			fltTest += ucerf2_Faults.get(i);
			ucerf2_All.set(i, ucerf2_OffFault.get(i)+ucerf2_Faults.get(i));
			allTest += ucerf2_All.get(i);
		}
		try {
			GMT_CA_Maps.plotSpatialMoRate_Map(ucerf2_Faults.copy(), "UCERF2 On-Fault MoRate-Nm/yr", " " , "UCERF2_OnFaultMoRateMap");
			GMT_CA_Maps.plotSpatialMoRate_Map(ucerf2_OffFault.copy(), "UCERF2 Off-Fault MoRate-Nm/yr", " " , "UCERF2_OffFaultMoRateMap");
			GMT_CA_Maps.plotSpatialMoRate_Map(ucerf2_All.copy(), "UCERF2 Total MoRate-Nm/yr", " " , "UCERF2_TotalMoRateMap");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		// Now make ave def model data
		DeformationModelOffFaultMoRateData spatPDFgen = DeformationModelOffFaultMoRateData.getInstance();
		GriddedGeoDataSet aveDefModOnFault = getAveDefModSpatialOnFaultMomentRateData(fm, true);
		GriddedGeoDataSet aveDefModOffFault = spatPDFgen.getAveDefModelSpatialOffFaultMoRates(fm, true);
		GriddedGeoDataSet aveDefModTotal = getAveDefModSpatialMomentRateData(fm, true);

		
		try {
			GMT_CA_Maps.plotSpatialMoRate_Map(aveDefModOnFault.copy(), "AveDefModOnFaultMoRate-Nm/yr", " " , "AveDefModOnFaultMoRateMap");
			GMT_CA_Maps.plotSpatialMoRate_Map(aveDefModOffFault.copy(), "AveDefModOffFaultMoRate-Nm/yr", " " , "AveDefModOffFaultMoRateMap");
			GMT_CA_Maps.plotSpatialMoRate_Map(aveDefModTotal.copy(), "AveDefModTotalMoRate-Nm/yr", " " , "AveDefModTotalMoRate");
			GMT_CA_Maps.plotRatioOfRateMaps(aveDefModOnFault, ucerf2_Faults, "AveDefModOnFault_RatioToUCERF2_MoRateMap", " " , "AveDefModOnFault_RatioToUCERF2_MoRateMap");
			GMT_CA_Maps.plotRatioOfRateMaps(aveDefModOffFault, ucerf2_OffFault, "AveDefModOffFault_RatioToUCERF2_MoRateMap", " " , "AveDefModOffFault_RatioToUCERF2_MoRateMap");
			GMT_CA_Maps.plotRatioOfRateMaps(aveDefModTotal, ucerf2_All, "AveDefModTotal_RatioToUCERF2_MoRateMap", " " , "AveDefModTotal_RatioToUCERF2_MoRateMap");

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		// now do individual dm models relative to U2 and the average DM
		ArrayList<DeformationModels> dmList = new ArrayList<DeformationModels>();
		dmList.add(DeformationModels.GEOLOGIC);
		dmList.add(DeformationModels.ABM);
		dmList.add(DeformationModels.NEOKINEMA);
		dmList.add(DeformationModels.ZENG);
				

		for(DeformationModels dm : dmList) {
			GriddedGeoDataSet offFaultData = spatPDFgen.getDefModSpatialOffFaultMoRates(fm, dm);
			GriddedGeoDataSet onFaultData = getDefModFaultMoRatesInRELM_Region(fm, dm);
			GriddedGeoDataSet totalMoRateData = RELM_RegionUtils.getRELM_RegionGeoDataSetInstance();
			for(int i=0; i<totalMoRateData.size();i++) {
				// some GEOL on-fault values are NaN:
				if(Double.isNaN(onFaultData.get(i))) {
					System.out.println("NaN onFault:\t"+i+"\t"+dm.getShortName());
					totalMoRateData.set(i, offFaultData.get(i));
				}
				else {
					totalMoRateData.set(i, offFaultData.get(i)+onFaultData.get(i));
				}
			}
			
			try {
				GMT_CA_Maps.plotSpatialMoRate_Map(onFaultData.copy(), dm+" MoRate-Nm/yr", " " , dm.getShortName()+"_OnFaultMoRateMap");
				GMT_CA_Maps.plotSpatialMoRate_Map(offFaultData.copy(), dm+" MoRate-Nm/yr", " " , dm.getShortName()+"_OffFaultMoRateMap");
				GMT_CA_Maps.plotSpatialMoRate_Map(totalMoRateData.copy(), dm+" MoRate-Nm/yr", " " , dm.getShortName()+"_TotalMoRateMap");
				// ratios to U2
				GMT_CA_Maps.plotRatioOfRateMaps(onFaultData, ucerf2_Faults, dm+" OnFaulRatioToUCERF2", " " , dm.getShortName()+"_OnFaulRatioToUCERF2Map");
				GMT_CA_Maps.plotRatioOfRateMaps(offFaultData, ucerf2_OffFault, dm+" OffFaulRatioToUCERF2", " " , dm.getShortName()+"_OffFaulRatioToUCERF2Map");
				GMT_CA_Maps.plotRatioOfRateMaps(totalMoRateData, ucerf2_All, dm+" TotalRatioToUCERF2", " " , dm.getShortName()+"_TotalRatioToUCERF2Map");
				// ratios to Ave
				GMT_CA_Maps.plotRatioOfRateMaps(onFaultData, aveDefModOnFault, dm+" OnFaulRatioToAveDefMod", " " , dm.getShortName()+"_OnFaulRatioToAveDefModMap");
				GMT_CA_Maps.plotRatioOfRateMaps(offFaultData, aveDefModOffFault, dm+" OffFaulRatioToAveDefMod", " " , dm.getShortName()+"_OffFaulRatioToAveDefModMap");
				GMT_CA_Maps.plotRatioOfRateMaps(totalMoRateData, aveDefModTotal, dm+" TotalRatioToAveDefMod", " " , dm.getShortName()+"_TotalRatioToAveDefModMap");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		

		// Now do the smooth-seismicity results
		double totAveMomentRate=0;
		for(int i=0; i<aveDefModTotal.size();i++)
			totAveMomentRate+=aveDefModTotal.get(i);
		GriddedGeoDataSet ucerf2_SmSeisDist = SmoothSeismicitySpatialPDF_Fetcher.getUCERF2_PDF();
		for(int i=0;i< ucerf2_SmSeisDist.size();i++)
			ucerf2_SmSeisDist.set(i, totAveMomentRate*ucerf2_SmSeisDist.get(i));
		
		GriddedGeoDataSet ucerf3_SmSeisDist = SmoothSeismicitySpatialPDF_Fetcher.getUCERF3_PDF();
		for(int i=0;i< ucerf3_SmSeisDist.size();i++)
			ucerf3_SmSeisDist.set(i, totAveMomentRate*ucerf3_SmSeisDist.get(i));
		System.out.println("totAveMomentRate="+totAveMomentRate);
		
		// FOLLOWING COMMENTED OUT UNTIL PETER GIVES ME THE UPDATE TO THE 4TH LINE DOWN - Needs to get a faultSustemRupSet to do this properly
		// make aveDefModTotal inside and outside fault polygons
//		GriddedGeoDataSet aveDefModTotalInsideFaults = aveDefModTotal.copy();
//		GriddedGeoDataSet aveDefModTotalOutsideFaults = aveDefModTotal.copy();
//		double[] nodeFracsInside = FaultPolyMgr.getNodeFractions(fm);
//		for(int i=0;i<aveDefModTotal.size();i++) {
//			aveDefModTotalInsideFaults.set(i, aveDefModTotalInsideFaults.get(i)*nodeFracsInside[i]);
//			aveDefModTotalOutsideFaults.set(i, aveDefModTotalOutsideFaults.get(i)*(1-nodeFracsInside[i]));
//		}

		try {
			GMT_CA_Maps.plotSpatialMoRate_Map(ucerf2_SmSeisDist.copy(), "UCERF2_SmoothSeis MoRate-Nm/yr", " " , "UCERF2_SmSeisMoRateMap");
			GMT_CA_Maps.plotRatioOfRateMaps(ucerf2_SmSeisDist, aveDefModTotal, "UCERF2_SmSeisToTotAveDefModRatio", " " , "UCERF2_SmSeisToTotAveDefModRatioMap");
//			GMT_CA_Maps.plotRatioOfRateMaps(ucerf2_SmSeisDist, aveDefModTotalInsideFaults, "UCERF2_SmSeisToInsideFaultAveDefModRatio", " " , "UCERF2_SmSeisToInsideFaultAveDefModRatioMap");
//			GMT_CA_Maps.plotRatioOfRateMaps(ucerf2_SmSeisDist, aveDefModTotalOutsideFaults, "UCERF2_SmSeisToOutsideFaultAveDefModRatio", " " , "UCERF2_SmSeisToOutsideFaultAveDefModRatioMap");

			GMT_CA_Maps.plotSpatialMoRate_Map(ucerf3_SmSeisDist.copy(), "UCERF3_SmoothSeis MoRate-Nm/yr", " " , "UCERF3_SmSeisMoRateMap");
			GMT_CA_Maps.plotRatioOfRateMaps(ucerf3_SmSeisDist, aveDefModTotal, "UCERF3_SmSeisToTotAveDefModRatio", " " , "UCERF3_SmSeisToTotAveDefModRatioMap");
//			GMT_CA_Maps.plotRatioOfRateMaps(ucerf3_SmSeisDist, aveDefModTotalInsideFaults, "UCERF3_SmSeisToInsideFaultAveDefModRatio", " " , "UCERF3_SmSeisToInsideFaultAveDefModRatioMap");
//			GMT_CA_Maps.plotRatioOfRateMaps(ucerf3_SmSeisDist, aveDefModTotalOutsideFaults, "UCERF3_SmSeisToOutsideFaultAveDefModRatio", " " , "UCERF3_SmSeisToOutsideFaultAveDefModRatioMap");

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		
		// now make Mmax map plot
		double totObsRate = TotalMag5Rate.RATE_8p7.getRateMag5();
		GriddedGeoDataSet mMaxData = RELM_RegionUtils.getRELM_RegionGeoDataSetInstance();
		ucerf3_SmSeisDist = SmoothSeismicitySpatialPDF_Fetcher.getUCERF3_PDF();
		GutenbergRichterMagFreqDist gr = new GutenbergRichterMagFreqDist(0, 3000, 0.01);
		for(int i=0;i< ucerf3_SmSeisDist.size();i++) {
			double rate = totObsRate*ucerf3_SmSeisDist.get(i)*1e5;		// increase by 1e5 for rate at zero mag
			double moRate = aveDefModTotal.get(i);
			try {
				gr.setAllButMagUpper(0, moRate, rate, 1.0, false);
				mMaxData.set(i, gr.getMagUpper());
			} catch (XY_DataSetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Point2DException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		try {
			GMT_CA_Maps.plotMagnitudeMap(mMaxData, "Implied Mmax", " " , "AveDefMod_UCERF3_smSeis_ImpliedMmaxMap");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
	}
	
//	public static void plotSmSeisOverAveDefModelMap() {
//		FaultModels fm = FaultModels.FM3_1;
//		ArrayList<DeformationModels> dmList = new ArrayList<DeformationModels>();
//		dmList.add(DeformationModels.GEOLOGIC);
//		dmList.add(DeformationModels.ABM);
//		dmList.add(DeformationModels.NEOKINEMA);
////		dmList.add(DeformationModels.GEOBOUND);
//		dmList.add(DeformationModels.ZENG);
//		
//		DeformationModelOffFaultMoRateData spatPDFgen = DeformationModelOffFaultMoRateData.getInstance();
//		
//		ArrayList<GriddedGeoDataSet> dmMoRatesList = new ArrayList<GriddedGeoDataSet>();
//		
//		// compute aveDefModPDF
//		GriddedGeoDataSet aveDefModPDF = RELM_RegionUtils.getRELM_RegionGeoDataSetInstance();
//		GriddedGeoDataSet aveDefModMoRates = RELM_RegionUtils.getRELM_RegionGeoDataSetInstance();
//		for(int i=0;i<aveDefModPDF.size();i++) {// initialize to zero
//			aveDefModPDF.set(i,0);
//			aveDefModMoRates.set(i,0);
//		}
//
//		for(DeformationModels dm : dmList) {
//			System.out.println("adding "+dm+" to the mean");
//			GriddedGeoDataSet offFaultData = spatPDFgen.getDefModSpatialOffFaultMoRates(fm, dm);
//			GriddedGeoDataSet onFaultData = getDefModMoRatesInRELM_Region(fm, dm);
//			GriddedGeoDataSet totalMoRateData = RELM_RegionUtils.getRELM_RegionGeoDataSetInstance();
//			for(int i=0; i<totalMoRateData.size();i++) {
//				// some GEOL on-fault values are NaN:
//				if(Double.isNaN(onFaultData.get(i))) {
//					System.out.println("NaN onFault:\t"+i+"\t"+dm.getShortName());
//					totalMoRateData.set(i, offFaultData.get(i));
//				}
//				else {
//					totalMoRateData.set(i, offFaultData.get(i)+onFaultData.get(i));
//				}
//			}
//			double sum=0;
//			for(int i=0;i<totalMoRateData.size();i++) 
//				sum += totalMoRateData.get(i);
//			System.out.println("sum="+(float)sum);
//			for(int i=0;i<totalMoRateData.size();i++) {
//				double newVal = (totalMoRateData.get(i)/sum)/dmList.size();
//				aveDefModPDF.set(i, aveDefModPDF.get(i)+newVal);
//				aveDefModMoRates.set(i, aveDefModMoRates.get(i) + totalMoRateData.get(i)/dmList.size());
//			}
//			dmMoRatesList.add(totalMoRateData);
//
//		}
//		
//		double sum=0;
//		for(int i=0;i<aveDefModPDF.size();i++) 
//			sum += aveDefModPDF.get(i);
//		System.out.println("Test:  sum="+(float)sum+" (should be 1.0)");
//
//		GriddedGeoDataSet ucerf2_SmSeisData = SmoothSeismicitySpatialPDF_Fetcher.getUCERF2_PDF();
//		GriddedGeoDataSet ucerf3_SmSeisData = SmoothSeismicitySpatialPDF_Fetcher.getUCERF3_PDF();
//
//		try {
//			GMT_CA_Maps.plotRatioOfRateMaps(ucerf2_SmSeisData, aveDefModPDF, "UCERF2_SmSeis/AveDefMod (Ratio)", " " , "UCERF2_SmSeis_AveDefMod_RatioMap");
//			GMT_CA_Maps.plotRatioOfRateMaps(ucerf3_SmSeisData, aveDefModPDF, "UCERF3_SmSeis/AveDefMod (Ratio)", " " , "UCERF3_SmSeis_AveDefMod_RatioMap");
//			GMT_CA_Maps.plotSpatialMoRate_Map(aveDefModMoRates.copy(), "AveDevMod MoRate-Nm/yr", " " , "AveDefMod_TotalMoRateMap");
//
//			for(int i=0;i<dmList.size();i++) {
//				System.out.println(i+": plotting "+dmList.get(i)+" maps");
//				// first one here is a duplicate of what's already done in plotAllSpatialMoRateMaps(), but repeated as a test
//				GMT_CA_Maps.plotSpatialMoRate_Map(dmMoRatesList.get(i).copy(), dmList.get(i)+" MoRate-Nm/yr", " " , dmList.get(i).getShortName()+"_TotalMoRateMap");
//				GMT_CA_Maps.plotRatioOfRateMaps(dmMoRatesList.get(i), aveDefModMoRates, dmList.get(i)+" Ratio", " " , dmList.get(i).getShortName()+"_RatioToMeanMoRateMap");
//			}
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//	}
	

	/**
	 * This computes a GriddedGeoDataSet with the total fault moment rate in each bin.
	 * 
	 *  This include moment rate reductions
	 * @param fm
	 * @param dm
	 * @return
	 */
	public static GriddedGeoDataSet getDefModFaultMoRatesInRELM_Region(FaultModels fm, DeformationModels dm) {
		GriddedGeoDataSet moRates = RELM_RegionUtils.getRELM_RegionGeoDataSetInstance();
		GriddedRegion relmGrid = RELM_RegionUtils.getGriddedRegionInstance();
		System.out.println("moRates.size()="+moRates.size());
		System.out.println("relmGrid.getNodeCount()="+relmGrid.getNodeCount());

		DeformationModelFetcher defFetch = new DeformationModelFetcher(fm, dm, UCERF3_DataUtils.DEFAULT_SCRATCH_DATA_DIR, InversionFaultSystemRupSetFactory.DEFAULT_ASEIS_VALUE);
		for(FaultSectionPrefData data : defFetch.getSubSectionList()) {
			double mr = data.calcMomentRate(true);
			LocationList locList = data.getStirlingGriddedSurface(1.0).getEvenlyDiscritizedListOfLocsOnSurface();
			mr /= (double)locList.size();
			for(Location loc: locList) {
				int index = relmGrid.indexForLocation(loc);
				if(index >=0) {
					double oldVal = moRates.get(index);
					moRates.set(index,oldVal+mr);
				}
//				else
//					System.out.println(loc+"\t"+data.getName());
			}
		}
		
		return moRates;
	}
	
	
	public static void writeFractionRegionNodesInsideFaultPolygons() {
		
		double totRateMgt5 = TotalMag5Rate.RATE_8p7.getRateMag5();
		
		double[] nodeFracs = FaultPolyMgr.getNodeFractions(FaultModels.FM3_1, null, null);
		GriddedGeoDataSet ucerf2_SmSeisDist = SmoothSeismicitySpatialPDF_Fetcher.getUCERF2_PDF();
		GriddedGeoDataSet ucerf3_SmSeisDist = SmoothSeismicitySpatialPDF_Fetcher.getUCERF3_PDF();
		double sum = 0;
		double totRateInsideU2=0, totRateInsideU3=0;
		for(int i=0;i<nodeFracs.length;i++) {
			sum += nodeFracs[i];
			totRateInsideU2 += nodeFracs[i]*ucerf2_SmSeisDist.get(i)*totRateMgt5;
			totRateInsideU3 += nodeFracs[i]*ucerf3_SmSeisDist.get(i)*totRateMgt5;
		}
		float fracInside = (float)(sum/(double)nodeFracs.length);
		System.out.println("totFracNodesInFaultPolygons="+fracInside+"\t("+Math.round(100*fracInside)+"%)");

		double totRateOutsideU2 = totRateMgt5 - totRateInsideU2;
		double totRateOutsideU3 = totRateMgt5 - totRateInsideU3;
		System.out.println("UCERF\trateInside\t(%rateIn)\trateOut\t(%rateOut)\totRate");

		System.out.println("UCERF2\t"+(float)totRateInsideU2+"\t("
				+Math.round(100*totRateInsideU2/totRateMgt5)+"%)\t"+
				+(float)totRateOutsideU2+"\t("+
				Math.round(100*totRateOutsideU2/totRateMgt5)+"%)\t"+totRateMgt5);
		System.out.println("UCERF3\t"+(float)totRateInsideU3+"\t("
				+Math.round(100*totRateInsideU3/totRateMgt5)+"%)\t"+
				+(float)totRateOutsideU3+"\t("+
				Math.round(100*totRateOutsideU3/totRateMgt5)+"%)\t"+totRateMgt5);
//		System.out.println("UCERF3\t"+(float)totRateInsideU3+"\t("+Math.round(100*totRateInsideU3/totRateMgt5)+"%)");

		
	}
	
	/**
	 * This writes the fraction of off-fault moment rates (from all the deformation models) that are
	 * inside the polygons of FM 3.1.
	 */
	public static void writeFractionOffFaultMoRateInsideFaultPolygons() {
		
		FaultModels fm = FaultModels.FM3_1;
		DeformationModelOffFaultMoRateData spatPDFgen = DeformationModelOffFaultMoRateData.getInstance();
		double[] nodeFracs = FaultPolyMgr.getNodeFractions(fm, null, null);

		ArrayList<DeformationModels> dmList = new ArrayList<DeformationModels>();
		dmList.add(DeformationModels.GEOLOGIC);
		dmList.add(DeformationModels.ABM);
		dmList.add(DeformationModels.NEOKINEMA);
		dmList.add(DeformationModels.GEOBOUND);
		dmList.add(DeformationModels.ZENG);
				
		System.out.println("DefMod\t%Inside\tMoRateInside\ttotMoRate (all off fault for"+fm+")");
		for(DeformationModels dm : dmList) {
			GriddedGeoDataSet offFaultData = spatPDFgen.getDefModSpatialOffFaultMoRates(fm, dm);
			double totOffFaultMoRate=0, totOffFaultMoRateInside=0;
			for(int i=0;i<offFaultData.size();i++) {
				totOffFaultMoRate += offFaultData.get(i);
				totOffFaultMoRateInside += offFaultData.get(i)*nodeFracs[i];
			}
			int perc = (int)(100.0*totOffFaultMoRateInside/totOffFaultMoRate);
			System.out.println(dm.getShortName()+"\t"+perc+"\t"+(float)totOffFaultMoRateInside+"\t"+(float)totOffFaultMoRate);
		}
	}
	
	
	public static void plotMmaxVersusFractSeisOffFault(double moRateOnFault, double moRateOffFault, double totMge5_rate,
			String label, String fileName) {
		GutenbergRichterMagFreqDist gr = new GutenbergRichterMagFreqDist(0, 1100, 0.01);
		EvenlyDiscretizedFunc offMmaxFunc = new EvenlyDiscretizedFunc(0.1,9,0.1);
		EvenlyDiscretizedFunc onMmaxFunc = new EvenlyDiscretizedFunc(0.1,9,0.1);
		for(int i=0; i<offMmaxFunc.getNum();i++) {
			double fracOff = offMmaxFunc.getX(i);
			gr.setAllButMagUpper(0.0, moRateOffFault, fracOff*totMge5_rate*1e5, 1.0, false);
			offMmaxFunc.set(i,gr.getMagUpper());
			gr.setAllButMagUpper(0.0, moRateOnFault, (1-fracOff)*totMge5_rate*1e5, 1.0, false);
			onMmaxFunc.set(i,gr.getMagUpper());
		}
		offMmaxFunc.setName("offMmaxFunc");
		onMmaxFunc.setName("onMmaxFunc");
		ArrayList<XY_DataSet> funcs = new ArrayList<XY_DataSet>();
		funcs.add(offMmaxFunc);
		funcs.add(onMmaxFunc);
		ArrayList<PlotCurveCharacterstics> plotChars = new ArrayList<PlotCurveCharacterstics>();
		plotChars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 5, null, 0, Color.BLACK));
		plotChars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 5, null, 0, Color.BLUE));
		GraphiWindowAPI_Impl graph = new GraphiWindowAPI_Impl(funcs, label);
		graph.setX_AxisRange(0, 1);
		graph.setY_AxisRange(6.5, 10);
		graph.setX_AxisLabel("Fraction of Total Seismicity That is Off Fault");
		graph.setY_AxisLabel("Maximum Magnitude");
		graph.setTickLabelFontSize(14);
		graph.setAxisLabelFontSize(16);
		graph.setPlotLabelFontSize(18);
		if(fileName != null) {
			try {
				graph.saveAsPNG(fileName);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		}		
	}
	
	public static void plotAllMmaxVersusFractSeisOffFault() {
		
		double rateMge5 = TotalMag5Rate.RATE_8p7.getRateMag5();
		
		// hard-coded values for UCERF2
		plotMmaxVersusFractSeisOffFault(1.73e19, 0.54e19, rateMge5,"UCERF2 Def Mod 2.1", "mMaxVsOffFltSeis_UCERF2.png");
		
		ArrayList<DeformationModels> defModList= new ArrayList<DeformationModels>();
		FaultModels fm = FaultModels.FM3_1;
		
		defModList.add(DeformationModels.ABM);
		defModList.add(DeformationModels.GEOLOGIC);
		defModList.add(DeformationModels.GEOLOGIC_PLUS_ABM);
		defModList.add(DeformationModels.NEOKINEMA);
		defModList.add(DeformationModels.ZENG);
		defModList.add(DeformationModels.GEOBOUND);
		
		for(DeformationModels dm :defModList) {
			String label = dm+" Def Mod";
			String fileName = "mMaxVsOffFltSeis_"+dm+".png";
			plotMmaxVersusFractSeisOffFault(calcFaultMoRateForDefModel(fm,dm,true), 
					calcMoRateOffFaultsForDefModel(fm,dm), rateMge5, label, fileName);
		}

	}
	
	
	/**
	 * This method computes the fraction of a SpatialSeisPDF that's inside the fault-section polygons 
	 * of the given fltSectPrefDataList.
	 * @param fltSectPrefDataList
	 * @param spatialSeisPDF
	 * @return
	 */
	public static double getFractSpatialPDF_InsideSectionPolygons(
			List<FaultSectionPrefData> fltSectPrefDataList, 
			SpatialSeisPDF spatialSeisPDF) {
		double sum = 0;
		GriddedSeisUtils gsu = new GriddedSeisUtils(fltSectPrefDataList, spatialSeisPDF, 12.0);
		return gsu.pdfInPolys();
	}
	
	

	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
//		calcMoRateAndMmaxDataForDefModels();
		
		DeformationModelFetcher defFetch = new DeformationModelFetcher(FaultModels.FM3_1, 
				DeformationModels.GEOLOGIC, UCERF3_DataUtils.DEFAULT_SCRATCH_DATA_DIR, 
				InversionFaultSystemRupSetFactory.DEFAULT_ASEIS_VALUE);

		System.out.println(getFractSpatialPDF_InsideSectionPolygons(
			defFetch.getSubSectionList(), SpatialSeisPDF.UCERF3));
		
//		plotDDW_AndLowerSeisDepthDistributions(defFetch.getSubSectionList(),"FM3_1 & GEOLOGIC Def Mod");

		
//		plotAllMmaxVersusFractSeisOffFault();

//		writeFractionOffFaultMoRateInsideFaultPolygons();
		
//		writeFractionRegionNodesInsideFaultPolygons();
		
//		writeMoRateOfParentSections(FaultModels.FM3_1, DeformationModels.GEOLOGIC);
		
//		plotMoreSpatialMaps();

//		testFaultZonePolygons();
		
//		writeListOfNewFaultSections();
		
//		plotAllSpatialMoRateMaps();
		
//		writeMoRateOfParentSections(FaultModels.FM3_1,DeformationModels.GEOLOGIC);
		
//		File default_scratch_dir = new File(UCERF3_DataUtils.DEFAULT_SCRATCH_DATA_DIR, "FaultSystemRupSets");
		
//		plotMoRateReductionHist(FaultModels.FM3_1,DeformationModels.GEOLOGIC);
		
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

		
		
	}

}
