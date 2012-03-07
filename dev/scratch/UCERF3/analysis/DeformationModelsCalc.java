package scratch.UCERF3.analysis;

import java.awt.Color;
import java.io.File;
import java.util.ArrayList;

import org.opensha.commons.data.function.HistogramFunction;
import org.opensha.commons.gui.plot.PlotLineType;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.opensha.sha.gui.infoTools.GraphiWindowAPI_Impl;
import org.opensha.sha.gui.infoTools.PlotCurveCharacterstics;
import org.opensha.sha.magdist.GutenbergRichterMagFreqDist;

import scratch.UCERF3.enumTreeBranches.DeformationModels;
import scratch.UCERF3.enumTreeBranches.FaultModels;
import scratch.UCERF3.inversion.InversionConfiguration;
import scratch.UCERF3.utils.DeformationModelFetcher;
import scratch.UCERF3.utils.UCERF3_DataUtils;

public class DeformationModelsCalc {
	
	public static void plotDDW_AndLowerSeisDepthDistributions(ArrayList<FaultSectionPrefData> subsectData, String plotTitle) {
		
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
	public static double calculateTotalMomentRate(ArrayList<FaultSectionPrefData> sectData, boolean creepReduced) {
		double totMoRate=0;
		for(FaultSectionPrefData data : sectData) {
			double moRate = data.calcMomentRate(creepReduced);
			if(!Double.isNaN(moRate))
				totMoRate += moRate;
		}
		return totMoRate;
	}
	
	
	
	private static String getTableLineForMoRateAndMmaxDataForDefModels(FaultModels fm, DeformationModels dm) {
		File scratch_dir = new File(UCERF3_DataUtils.DEFAULT_SCRATCH_DATA_DIR, "FaultSystemRupSets");
		DeformationModelFetcher defFetch = new DeformationModelFetcher(fm, dm,scratch_dir);
		double moRate = calculateTotalMomentRate(defFetch.getSubSectionList(),true);
		System.out.println(fm.getName()+", "+dm.getName()+ " (reduced):\t"+(float)moRate);
		System.out.println(fm.getName()+", "+dm.getName()+ " (not reduced):\t"+(float)calculateTotalMomentRate(defFetch.getSubSectionList(),false));
		double fractOff = InversionConfiguration.findMomentFractionOffFaults(fm, dm);
		double totMoRate = calcTotalMomentRate(moRate,fractOff);
		GutenbergRichterMagFreqDist targetMFD = new GutenbergRichterMagFreqDist(0.0005,9.9995,10000);
		targetMFD.setAllButMagUpper(0.0005, totMoRate, 854000, 1.0, true);
		System.out.println("totMoRate="+(float)totMoRate+"\tgetTotalMomentRate()="+(float)targetMFD.getTotalMomentRate()+"\tMgt4rate="+(float)targetMFD.getCumRate(4.0005)+
				"\tupperMag="+targetMFD.getMagUpper()+"\tMgt8rate="+(float)targetMFD.getCumRate(8.0005));
		return dm+"\t"+(float)(moRate/1e19)+"\t"+(float)fractOff+"\t"+(float)((totMoRate-moRate)/1e19)+"\t"+(float)(totMoRate/1e19)+"\t"+
				(float)targetMFD.getMagUpper()+"\t"+(float)targetMFD.getCumRate(8.0005)+"\t"+(float)(1.0/targetMFD.getCumRate(8.0005));
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

		System.out.println("\nmodel\tfltMoRate\tfractOff\tmoRateOff\ttotMoRate\tMmax\tRate_gtM8\tMRIgtM8");
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
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		
		File default_scratch_dir = new File(UCERF3_DataUtils.DEFAULT_SCRATCH_DATA_DIR, "FaultSystemRupSets");
		
		calcMoRateAndMmaxDataForDefModels();
		
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
