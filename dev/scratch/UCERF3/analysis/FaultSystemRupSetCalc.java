/**
 * 
 */
package scratch.UCERF3.analysis;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;

import org.opensha.commons.data.function.HistogramFunction;
import org.opensha.commons.gui.plot.PlotLineType;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.opensha.sha.gui.infoTools.GraphiWindowAPI_Impl;
import org.opensha.sha.gui.infoTools.PlotCurveCharacterstics;
import org.opensha.sha.magdist.GutenbergRichterMagFreqDist;

import scratch.UCERF3.FaultSystemRupSet;
import scratch.UCERF3.enumTreeBranches.DeformationModels;
import scratch.UCERF3.enumTreeBranches.FaultModels;
import scratch.UCERF3.enumTreeBranches.InversionModels;
import scratch.UCERF3.inversion.InversionFaultSystemRupSetFactory;

/**
 * This class hosts various calculations for a FaultSystemRupSet
 * @author field
 *
 */
public class FaultSystemRupSetCalc {
	
	final static boolean D=false;
	
	
	/**
	 * This computes a histogram of the minimum magnitude among all the fault 
	 * sections in the given FaultSystemRupSet
	 * @param faultSystemRupSet
	 * @param minMag
	 * @param numMag
	 * @param deltaMag
	 */
	public static HistogramFunction getMinMagHistogram(FaultSystemRupSet faultSystemRupSet, double minMag, int numMag, double deltaMag, boolean wtByMoRate) {
		HistogramFunction hist = new HistogramFunction(minMag, numMag, deltaMag);
		double wt=1;
		for(int i=0;i<faultSystemRupSet.getNumSections();i++) {
			if(wtByMoRate)
				wt = faultSystemRupSet.getAreaForSection(i)*faultSystemRupSet.getSlipRateForSection(i);
			if(D && Double.isNaN(wt)) {
				System.out.println(i+" has NaN moRate; "+faultSystemRupSet.getFaultSectionData(i).getName()+
						"\tarea="+(float)faultSystemRupSet.getAreaForSection(i)+"\tslipRate="+(float)faultSystemRupSet.getSlipRateForSection(i));
			}
			double min = faultSystemRupSet.getMinMagForSection(i);
			if(!Double.isNaN(wt)) {
				hist.add(min, wt);
			}
		}
		if(D) System.out.println(hist);
		hist.setName("Min Mag Histogram for FaultSystemRupSet");
		hist.setInfo("(among the "+faultSystemRupSet.getNumSections()+" sections)");
		hist.normalizeBySumOfY_Vals();
		return hist;
	}
	
	
	/**
	 * This computes a histogram of the maximum magnitude among all the fault 
	 * sections in the given FaultSystemRupSet
	 * @param faultSystemRupSet
	 * @param minMag
	 * @param numMag
	 * @param deltaMag
	 */
	public static HistogramFunction getMaxMagHistogram(FaultSystemRupSet faultSystemRupSet, double minMag, int numMag, double deltaMag, boolean wtByMoRate) {
		HistogramFunction hist = new HistogramFunction(minMag, numMag, deltaMag);
		double wt=1;
		for(int i=0;i<faultSystemRupSet.getNumSections();i++) {
			if(wtByMoRate)
				wt = faultSystemRupSet.getAreaForSection(i)*faultSystemRupSet.getSlipRateForSection(i);
			if(D && Double.isNaN(wt)) {
				System.out.println(i+" has NaN moRate; "+faultSystemRupSet.getFaultSectionData(i).getName()+
						"\tarea="+(float)faultSystemRupSet.getAreaForSection(i)+"\tslipRate="+(float)faultSystemRupSet.getSlipRateForSection(i));
			}
			double max = faultSystemRupSet.getMaxMagForSection(i);
			if(!Double.isNaN(wt)) {
				hist.add(max, wt);
			}
		}
		if(D) System.out.println(hist);
		hist.setName("Max Mag Histogram for FaultSystemRupSet");
		hist.setInfo("(among the "+faultSystemRupSet.getNumSections()+" sections)");
		hist.normalizeBySumOfY_Vals();
		return hist;
	}
	
	
	/**
	 * This computes a histogram of the the magnitudes among all ruptures 
	 * in the given FaultSystemRupSet
	 * @param faultSystemRupSet
	 * @param minMag
	 * @param numMag
	 * @param deltaMag
	 */
	public static HistogramFunction getMagHistogram(FaultSystemRupSet faultSystemRupSet, double minMag, int numMag, double deltaMag) {
		HistogramFunction hist = new HistogramFunction(minMag, numMag, deltaMag);
			for (int r=0;r<faultSystemRupSet.getNumRuptures(); r++) {
				hist.add(faultSystemRupSet.getMagForRup(r), 1.0);
			}
		if(D) System.out.println(hist);
		hist.normalizeBySumOfY_Vals();
		hist.setName("Mag Histogram for FaultSystemRupSet");
		hist.setInfo("(based on "+faultSystemRupSet.getNumRuptures()+" ruptures)");
		return hist;
	}

	
	public static void plotAllHistograms(FaultSystemRupSet faultSystemRupSet, double minMag, int numMag, double deltaMag, boolean wtByMoRate) {
		ArrayList<HistogramFunction> hists = new ArrayList<HistogramFunction>();
		hists.add(getMagHistogram(faultSystemRupSet, minMag, numMag, deltaMag));
		hists.add(getMaxMagHistogram(faultSystemRupSet, minMag, numMag, deltaMag, wtByMoRate));
		hists.add(getMinMagHistogram(faultSystemRupSet, minMag, numMag, deltaMag, wtByMoRate));
		
		hists.add(hists.get(0).getCumulativeDistFunction());
		hists.get(3).setName("Cumulative "+hists.get(0).getName());
		hists.get(3).setInfo(hists.get(0).getInfo());
		hists.add(hists.get(1).getCumulativeDistFunction());
		hists.get(4).setName("Cumulative "+hists.get(1).getName());
		hists.get(4).setInfo(hists.get(1).getInfo());
		hists.add(hists.get(2).getCumulativeDistFunction());
		hists.get(5).setName("Cumulative "+hists.get(2).getName());
		hists.get(5).setInfo(hists.get(2).getInfo());
		
		ArrayList<PlotCurveCharacterstics> list = new ArrayList<PlotCurveCharacterstics>();
		list.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, Color.BLACK));
		list.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, Color.BLUE));
		list.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, Color.RED));
		list.add(new PlotCurveCharacterstics(PlotLineType.DASHED, 2f, Color.BLACK));
		list.add(new PlotCurveCharacterstics(PlotLineType.DASHED, 2f, Color.BLUE));
		list.add(new PlotCurveCharacterstics(PlotLineType.DASHED, 2f, Color.RED));
		
		GraphiWindowAPI_Impl graph = new GraphiWindowAPI_Impl(hists, "Histograms",list); 
		graph.setX_AxisLabel("Magnitude");
		graph.setY_AxisLabel("Normalized Number");

	}
	
	/**
	 * This compute the fraction of moment released below the given minMag, and for
	 * the given maxMag, assuming a Gutenberg Richter distribution
	 * @param minMag
	 * @param maxMag
	 * @return
	 */
	public static double getFractMomentReductionForSmallMags(double magLower, double magUpper, double bValue) {
		double magLowerRounded = Math.round((magLower-0.05)*10)/10.0+0.05;
		double magUpperRounded = Math.round((magUpper-0.05)*10)/10.0+0.05;
		GutenbergRichterMagFreqDist gr = new GutenbergRichterMagFreqDist(0.05, 90, 0.1,0.05, magUpperRounded, 1.0, bValue);
		
		double moRateSum=0;
		for(int i=0; i<gr.getXIndex(magLowerRounded);i++)
			moRateSum += gr.getMomentRate(i);
		if(D) System.out.println("\n"+moRateSum +"\t"+gr.getTotalMomentRate());
		return moRateSum;
	}
	
	
	
	public static HistogramFunction getMomentRateReductionHistogram(FaultSystemRupSet faultSystemRupSet, boolean wtByMoRate, boolean plotResult) {
		HistogramFunction hist = new HistogramFunction(0.005, 100, 0.01);
		double wt=1;
		double mean=0, totWt=0;;
		for(int i=0;i<faultSystemRupSet.getNumSections();i++) {
			if(wtByMoRate)
				wt = faultSystemRupSet.getAreaForSection(i)*faultSystemRupSet.getSlipRateForSection(i);
			if(!Double.isNaN(wt)) {
				double reduction = faultSystemRupSet.getSubseismogenicMomentRateReductionFraction(i);
				if (Double.isNaN(reduction)) {
					System.out.println("NaN reduction for section: "+faultSystemRupSet.getFaultSectionData(i).getName()
							+" with slip: "+faultSystemRupSet.getSlipRateForSection(i));
				} else {
					hist.add(reduction, wt);
					mean += reduction*wt;
					totWt +=wt;
					if(reduction>0.5)
						System.out.println(reduction+"\t"+faultSystemRupSet.getFaultSectionData(i).getName()+
								"\tmagLower="+(float)faultSystemRupSet.getMinMagForSection(i)
								+"\tmagUpper="+(float)faultSystemRupSet.getMaxMagForSection(i));
				}
			}
		}
		mean /= totWt;
		if (D) System.out.println(hist);
		hist.setName("Distribution of Moment Rate Reductions for FaultSystemRupSet");
		hist.setInfo("(among the "+faultSystemRupSet.getNumSections()+" sections; mean = "+(float)mean+")");
		hist.normalizeBySumOfY_Vals();
		
		if(plotResult) {
			GraphiWindowAPI_Impl graph = new GraphiWindowAPI_Impl(hist, "Moment Rate Reductions Histogram"); 
			graph.setX_AxisLabel("Reduction");
			graph.setY_AxisLabel("Normalized Number");

		}
		return hist;
	}
	
	
	
	public static void listAllParentSectionNames(FaultSystemRupSet faultSystemRupSet) {
		ArrayList<String> parNames = new ArrayList<String>();
		for(FaultSectionPrefData data : faultSystemRupSet.getFaultSectionDataList()) {
			if(!parNames.contains(data.getParentSectionName())) {
				parNames.add(data.getParentSectionName());
			}
		}
		
		for(String name: parNames)
			System.out.println(name);
		
	}

	
	
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
//		getFractMomentReductionForSmallMags(6.0, 8.0, 1.0);
//		getFractMomentReductionForSmallMags(7.0, 8.0, 1.0);
		
   		try {
   			System.out.println("Getting rup set");
			FaultSystemRupSet faultSysRupSet = InversionFaultSystemRupSetFactory.cachedForBranch(
					FaultModels.FM3_1, DeformationModels.GEOLOGIC, InversionModels.GR, true);
			System.out.println("Done getting rup set");
			getMomentRateReductionHistogram(faultSysRupSet, true, true);
//			plotAllHistograms(faultSysRupSet, 5.05,40,0.1, true);
			
//			System.out.println(getMinMagHistogram(faultSysRupSet, 5.05,40,0.1, true).getCumulativeDistFunction());

   		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
