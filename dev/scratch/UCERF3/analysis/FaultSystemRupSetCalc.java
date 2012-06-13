/**
 * 
 */
package scratch.UCERF3.analysis;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.opensha.commons.calc.FaultMomentCalc;
import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.commons.data.function.DefaultXY_DataSet;
import org.opensha.commons.data.function.EvenlyDiscretizedFunc;
import org.opensha.commons.data.function.HistogramFunction;
import org.opensha.commons.data.function.XY_DataSet;
import org.opensha.commons.eq.MagUtils;
import org.opensha.commons.gui.plot.PlotLineType;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.opensha.sha.gui.infoTools.GraphiWindowAPI_Impl;
import org.opensha.sha.gui.infoTools.PlotCurveCharacterstics;
import org.opensha.sha.magdist.GutenbergRichterMagFreqDist;
import org.opensha.sha.magdist.IncrementalMagFreqDist;
import org.opensha.sha.magdist.SummedMagFreqDist;

import com.google.common.collect.Lists;

import scratch.UCERF3.FaultSystemRupSet;
import scratch.UCERF3.enumTreeBranches.ApplyImpliedCouplingCoeff;
import scratch.UCERF3.enumTreeBranches.AveSlipForRupModels;
import scratch.UCERF3.enumTreeBranches.DeformationModels;
import scratch.UCERF3.enumTreeBranches.FaultModels;
import scratch.UCERF3.enumTreeBranches.InversionModels;
import scratch.UCERF3.enumTreeBranches.MagAreaRelationships;
import scratch.UCERF3.enumTreeBranches.MaxMagOffFault;
import scratch.UCERF3.enumTreeBranches.SlipAlongRuptureModels;
import scratch.UCERF3.enumTreeBranches.SpatialSeisPDF;
import scratch.UCERF3.enumTreeBranches.TotalMag5Rate;
import scratch.UCERF3.griddedSeismicity.GriddedSeisUtils;
import scratch.UCERF3.inversion.InversionFaultSystemRupSet;
import scratch.UCERF3.inversion.InversionFaultSystemRupSetFactory;
import scratch.UCERF3.inversion.InversionMFDs;
import scratch.UCERF3.inversion.LaughTestFilter;
import scratch.UCERF3.utils.DeformationModelOffFaultMoRateData;

/**
 * This class hosts various calculations for a FaultSystemRupSet
 * @author field
 *
 */
public class FaultSystemRupSetCalc {
	
	final static boolean D=false;
	
	
	/**
	 * This the mean minimum magnitude among all the fault 
	 * sections in the given FaultSystemRupSet
	 * @param faultSystemRupSet
	 * @param wtByMoRate - determines whether or not it's a weighted average based on orignal moment rate
	 */
	public static double getMeanMinMag(FaultSystemRupSet faultSystemRupSet, boolean wtByMoRate) {
		double wt=1;
		double totWt=0;
		double sum=0;
		for(int i=0;i<faultSystemRupSet.getNumSections();i++) {
			if(wtByMoRate) {
				wt = faultSystemRupSet.getFaultSectionData(i).calcMomentRate(true);
				if(Double.isNaN(wt)) {
					wt=0;
					if(D) System.out.println(i+" has NaN moRate; "+faultSystemRupSet.getFaultSectionData(i).getName()+
							"\tarea="+(float)faultSystemRupSet.getAreaForSection(i)+"\tslipRate="+(float)faultSystemRupSet.getSlipRateForSection(i));
				}
			}
			sum += faultSystemRupSet.getMinMagForSection(i)*wt;
			totWt+=wt;
		}
		if(D) System.out.println("meanMinMag="+(sum/totWt));
		return sum/totWt;
	}

	
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
		hist.setInfo("(among the "+faultSystemRupSet.getNumSections()+" sections; wtByMoRate="+wtByMoRate+")");
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
	
	
	/**
	 * This computes a histogram of the the lengths (in KM) among all ruptures 
	 * in the given FaultSystemRupSet
	 * @param faultSystemRupSet
	 * @param minLength
	 * @param numLengths
	 * @param deltaLength
	 */
	public static HistogramFunction getLengthHistogram(FaultSystemRupSet faultSystemRupSet, double minLength, int numLengths, double deltaLength) {
		HistogramFunction hist = new HistogramFunction(minLength, numLengths, deltaLength);
			for (int r=0;r<faultSystemRupSet.getNumRuptures(); r++) {
				double length = 0;
				for (int sectID : faultSystemRupSet.getSectionsIndicesForRup(r))
					length += faultSystemRupSet.getFaultSectionData(sectID).getTraceLength();
				hist.add(length, 1.0);
			}
		if(D) System.out.println(hist);
		hist.normalizeBySumOfY_Vals();
		hist.setName("Length Histogram for FaultSystemRupSet");
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
				double reduction = faultSystemRupSet.getMomentRateReductionFraction(i);
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
	 * This plots the sum of all the implied GR nucleation MFDs for each section,
	 * assuming all moment at a section goes into a GR with b=1 from M=0 to the
	 * maximum magnitude the section participates in.
	 */
	public static void plotImpliedTotalSectGR_MFD(FaultSystemRupSet faultSysRupSet, String label) {
		SummedMagFreqDist mfd = calcImpliedGR_NucleationMFD(faultSysRupSet, 0.05, 90, 0.1);
		
		GutenbergRichterMagFreqDist gr = new GutenbergRichterMagFreqDist(0.0, 91, 0.1, 0.0, mfd.getMaxMagWithNonZeroRate(), mfd.getTotalMomentRate(), 1.0);
		gr.setName("Perfect GR");
		gr.setInfo("(up to mag of largest event in fault system; MoRate ="+(float)mfd.getTotalMomentRate()+"; Rate ge M5 = "+(float)gr.getCumRate(5.0)+")");

		ArrayList<XY_DataSet> funcs = new ArrayList<XY_DataSet>();
		funcs.add(mfd);
		funcs.add(mfd.getCumRateDistWithOffset());
		funcs.add(gr);
		funcs.add(gr.getCumRateDistWithOffset());	
		ArrayList<PlotCurveCharacterstics> plotChars = new ArrayList<PlotCurveCharacterstics>();
		plotChars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 3, null, 0, Color.BLUE));
		plotChars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 3, null, 0, Color.BLUE));
		plotChars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 1, null, 0, Color.GRAY));
		plotChars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 1, null, 0, Color.GRAY));
		String morelabel = " (M>=5 rate = "+((float)Math.round(mfd.getCumRate(5.0)*10.0))/10+")";
		// note that mendocino is included here (but filtered in the UCERF2 ERF)
		if(faultSysRupSet.getFaultModel() == FaultModels.FM2_1) {
			morelabel += " -- Mendocino included!";
		}
		GraphiWindowAPI_Impl graph = new GraphiWindowAPI_Impl(funcs, label+morelabel,plotChars);
		graph.setX_AxisRange(5, 9);
		graph.setY_AxisRange(1e-5, 20);
		graph.setYLog(true);
		graph.setX_AxisLabel("Mag");
		graph.setY_AxisLabel("Rate (per year)");

		graph.setTickLabelFontSize(14);
		graph.setAxisLabelFontSize(16);
		graph.setPlotLabelFontSize(18);
		String fileName = "TargetGR_"+label + ".png";
		if(fileName != null) {
			try {
				graph.saveAsPNG(fileName);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		}		

		
	}
	
	/**
	 * This computes the total implied nucleation MFD assuming each fault section has a perfect GR
	 * up to the maximum magnitude it participates in.
	 * @param faultSysRupSet
	 * @return
	 */
	public static SummedMagFreqDist calcImpliedGR_NucleationMFD(FaultSystemRupSet faultSysRupSet, double minMag, int numMag, double deltaMag) {
		
		ArrayList<GutenbergRichterMagFreqDist> gr_mfds = calcImpliedNuclMFD_ForEachSection(faultSysRupSet, minMag, numMag, deltaMag);
		SummedMagFreqDist mfd = new SummedMagFreqDist(minMag, numMag, deltaMag);
		double totMoRate=0;
		double mMaxInRegion=0;
		int index = 0;
		for(GutenbergRichterMagFreqDist gr : gr_mfds) {
			if(gr.getMagUpper()>mMaxInRegion) mMaxInRegion= gr.getMagUpper();
			totMoRate += gr.getTotalMomentRate();
			mfd.addIncrementalMagFreqDist(gr);
			index +=1;
		}
		mfd.setName(faultSysRupSet.getDeformationModel()+ " Target GR MFD");
		mfd.setInfo("Rate ge M5 = "+(float)mfd.getCumRate(5.05 )+"; totMoRate = "+(float)mfd.getTotalMomentRate());
		return mfd;
	}
	
	
	/**
	 * This computes the implied nucleation MFD for each fault section assuming it is a perfect GR
	 * from mag zero up to the maximum magnitude it participates in (w/ b=1).
	 * @param faultSysRupSet
	 * @return
	 */
	public static ArrayList<GutenbergRichterMagFreqDist> calcImpliedNuclMFD_ForEachSection(FaultSystemRupSet faultSysRupSet,
			double minMag, int numMag, double deltaMag) {
		List<FaultSectionPrefData> sectDataList = faultSysRupSet.getFaultSectionDataList();
		ArrayList<GutenbergRichterMagFreqDist> mfds = new ArrayList<GutenbergRichterMagFreqDist>();
		GutenbergRichterMagFreqDist tempGR = new GutenbergRichterMagFreqDist(minMag, numMag, deltaMag);
		for(int i=0; i< sectDataList.size();i++) {
			FaultSectionPrefData sectData = sectDataList.get(i);
			int mMaxIndex = tempGR.getClosestXIndex(faultSysRupSet.getMaxMagForSection(i));
			double mMax = tempGR.getX(mMaxIndex);
			double moRate = sectData.calcMomentRate(true);
			if(Double.isNaN(moRate)) {
				moRate = 0;
//				GutenbergRichterMagFreqDist gr = new GutenbergRichterMagFreqDist(0.05, 90, 0.1, 0.05, mMax, moRate, 1.0);
//System.out.println("NaN MoRate; set to zero");
//System.out.println(gr);
			}
			GutenbergRichterMagFreqDist gr = new GutenbergRichterMagFreqDist(minMag, numMag, deltaMag, minMag, mMax, moRate, 1.0);
//if(Double.isNaN(gr.getTotalIncrRate())) {
//	System.out.println(i+"\t"+mMax+"\t"+moRate);
//	System.exit(0);
//}
			mfds.add(gr);
		}
		return mfds;
	}

	
	public static void plotAllImpliedTotalSectGR_MFD() {
		ArrayList<MagAreaRelationships> magAreaList = new ArrayList<MagAreaRelationships>();
		magAreaList.add(MagAreaRelationships.ELL_B);
		magAreaList.add(MagAreaRelationships.HB_08);
		magAreaList.add(MagAreaRelationships.SHAW_09_MOD);
		
		ArrayList<DeformationModels> defModList= new ArrayList<DeformationModels>();
		FaultModels fm = FaultModels.FM3_1;
		
		defModList.add(DeformationModels.ABM);
		defModList.add(DeformationModels.GEOLOGIC);
		defModList.add(DeformationModels.GEOLOGIC_PLUS_ABM);
		defModList.add(DeformationModels.NEOKINEMA);
		defModList.add(DeformationModels.ZENG);
		defModList.add(DeformationModels.GEOBOUND);
		
		// for UCERF3
		for(DeformationModels dm :defModList) {
			for(MagAreaRelationships ma:magAreaList) {
				FaultSystemRupSet faultSysRupSet = InversionFaultSystemRupSetFactory.forBranch(fm, dm, 
						ma, AveSlipForRupModels.ELLSWORTH_B, SlipAlongRuptureModels.TAPERED, InversionModels.GR_CONSTRAINED);
				String label = faultSysRupSet.getDeformationModel().getShortName()+"_"+ma.getShortName();
				plotImpliedTotalSectGR_MFD(faultSysRupSet, label);				
			}
		}
		
		// now do UCERF2
		fm = FaultModels.FM2_1;
		DeformationModels dm = DeformationModels.UCERF2_ALL;
		for(MagAreaRelationships ma:magAreaList) {
			FaultSystemRupSet faultSysRupSet = InversionFaultSystemRupSetFactory.forBranch(fm, dm, 
					ma, AveSlipForRupModels.ELLSWORTH_B, SlipAlongRuptureModels.TAPERED, InversionModels.GR_CONSTRAINED);
			String label = faultSysRupSet.getDeformationModel().getShortName()+"_"+ma.getShortName();
			plotImpliedTotalSectGR_MFD(faultSysRupSet, label);				
		}
	}

	
	
	public static String testInversionGR_Setup(double totRegionalM5_Rate, double fractSeisOffFault, double mMaxOffFault,
			FaultSystemRupSet faultSysRupSet) {
		
		FaultModels fm = faultSysRupSet.getFaultModel();
		DeformationModels dm = faultSysRupSet.getDeformationModel();
		
		double offFaultRate = totRegionalM5_Rate*fractSeisOffFault;
		double onFaultRate = totRegionalM5_Rate-offFaultRate;
		
		// GR Branch
		SummedMagFreqDist mfd = calcImpliedGR_NucleationMFD(faultSysRupSet, 0.05, 90, 0.1);
		
		double onCoupCoeff = onFaultRate/mfd.getCumRate(5.05);
		double onFaultOrigMoRate = mfd.getTotalMomentRate();
		double onFaultReducedMoRate = onCoupCoeff*onFaultOrigMoRate;
		
		GutenbergRichterMagFreqDist offFaultGR = new GutenbergRichterMagFreqDist(0.05, 90, 0.1, 0.05, mMaxOffFault, 1.0, 1.0);
		offFaultGR.scaleToCumRate(0, offFaultRate*1e5);
		DeformationModelOffFaultMoRateData defModOffFaultMoRateData = DeformationModelOffFaultMoRateData.getInstance();
		double offFaultOrigMoRate = defModOffFaultMoRateData.getTotalOffFaultMomentRate(fm, dm);
		double offFaultReducedMoRate = offFaultGR.getTotalMomentRate();
		
		// solve for the maximum magnitude off-fault with full moment
		GutenbergRichterMagFreqDist tempOffFaultGR = new GutenbergRichterMagFreqDist(0.005, 900, 0.01);
		tempOffFaultGR.setAllButMagUpper(0.005, offFaultOrigMoRate, offFaultRate*1e5, 1.0, true);
		double maxOffMagWithFullMoment = tempOffFaultGR.getMagUpper();
		
		double offCoupCoeff = offFaultReducedMoRate/offFaultOrigMoRate;

		double moRateReduction = (onFaultReducedMoRate+offFaultReducedMoRate)/(onFaultOrigMoRate+offFaultOrigMoRate);
		// compute the average minimum seismogenic rupture mag
		double aveMinSeismoMag = getMeanMinMag(faultSysRupSet, true);
		
		return (float)mfd.getMaxMagWithNonZeroRate()+"\t"+(float)onCoupCoeff+"\t"+(float)offCoupCoeff+"\t"+(float)moRateReduction+"\t"+
				(float)onFaultOrigMoRate+"\t"+(float)onFaultReducedMoRate+"\t"+
				(float)offFaultOrigMoRate+"\t"+(float)offFaultReducedMoRate+"\t"+(float)maxOffMagWithFullMoment+"\t"+(float)aveMinSeismoMag;
		
	}
	
	
	
	public static String testInversionCharSetup(double totRegionalM5_Rate, double fractSeisOffFault, double mMaxOffFault,
			FaultSystemRupSet faultSysRupSet) {
		
		FaultModels fm = faultSysRupSet.getFaultModel();
		DeformationModels dm = faultSysRupSet.getDeformationModel();
		
		double offFaultMgt5_Rate = totRegionalM5_Rate*fractSeisOffFault;
		double totOnFaultMgt5_Rate = totRegionalM5_Rate-offFaultMgt5_Rate;
		
		DeformationModelOffFaultMoRateData defModOffFaultMoRateData = DeformationModelOffFaultMoRateData.getInstance();
		double offFaultOrigMoRate = defModOffFaultMoRateData.getTotalOffFaultMomentRate(fm, dm);
		
		// get fault moment rate and mMax for faultSysRupSet
		double onFaultOrigMoRate=0;
		double mMaxInRegion=0;
		List<FaultSectionPrefData> sectDataList = faultSysRupSet.getFaultSectionDataList();
		for(int i=0; i<sectDataList.size();i++) {
			double mMax = (double)Math.round(10*(faultSysRupSet.getMaxMagForSection(i)-0.05))/10.0 +0.05;
			double moRate = sectDataList.get(i).calcMomentRate(true);
			if(Double.isNaN(moRate))  moRate = 0;
			if(mMax>mMaxInRegion) mMaxInRegion = mMax;
			onFaultOrigMoRate += moRate;
		}
			
		// make the perfect target GR for region
		GutenbergRichterMagFreqDist totGR = new GutenbergRichterMagFreqDist(0.05, 90, 0.1, 0.05, mMaxInRegion, 1.0, 1.0);
		totGR.scaleToCumRate(0, totRegionalM5_Rate*1e5);
		
		double moRateReduction = totGR.getTotalMomentRate()/(onFaultOrigMoRate+offFaultOrigMoRate);
		
		double aveMinSeismoMag = getMeanMinMag(faultSysRupSet, true);
		
		IncrementalMagFreqDist offFaultMFD = getTriLinearCharOffFaultTargetMFD(totGR, totOnFaultMgt5_Rate, aveMinSeismoMag, mMaxOffFault);
		IncrementalMagFreqDist onFaultMFD = new IncrementalMagFreqDist(totGR.getMinX(),totGR.getNum(),totGR.getDelta());
		for(int i=0;i<onFaultMFD.getNum();i++) {
			onFaultMFD.set(i,totGR.getY(i)-offFaultMFD.getY(i));
		}
		
		//  	TEST *************************
//		ArrayList<IncrementalMagFreqDist> test = new ArrayList<IncrementalMagFreqDist>();
//		test.add(totGR);
//		test.add(onFaultMFD);
//		test.add(offFaultMFD);
//		GraphiWindowAPI_Impl graph = new GraphiWindowAPI_Impl(test, "MFDs");

		
		
		
		// solve for the max off-fault mag needed to satisfy off-fault moRate
		IncrementalMagFreqDist testOffFaultMFD = getTriLinearCharOffFaultTargetMFD(offFaultOrigMoRate,totGR, totOnFaultMgt5_Rate,aveMinSeismoMag);
		double maxOffMagWithFullMoment;
		if(testOffFaultMFD != null)
			maxOffMagWithFullMoment = testOffFaultMFD.getMaxMagWithNonZeroRate();
		else
			maxOffMagWithFullMoment = Double.NaN;

		// OLD STUFF
//		int mMaxOffFaultIndex = totGR.getXIndex(mMaxOffFault);
//		IncrementalMagFreqDist[] mfds = old_getCharOnFaultTargetMFD(totGR, totOnFaultMgt5_Rate, mMaxOffFaultIndex);
//		IncrementalMagFreqDist onFaultMFD = mfds[0];
//		IncrementalMagFreqDist offFaultMFD = mfds[1];
		
		// get off-fault moment rate assuming full def model moment is satisfied
//		mfds = old_getCharOnFaultTargetMFD(offFaultOrigMoRate,totGR, totOnFaultMgt5_Rate);
//		double maxOffMagWithFullMoment;
//		if(mfds != null)
//			maxOffMagWithFullMoment = mfds[1].getMaxMagWithNonZeroRate();
//		else
//			maxOffMagWithFullMoment = Double.NaN;


		
		// test
//		onFaultMFD.setName("onFaultMFD");
//		onFaultMFD.setInfo("(rate(M>=5) = "+(float)onFaultMFD.getCumRate(5.05)+")");
//		offFaultMFD.setName("offFaultMFD");
//		offFaultMFD.setInfo("(rate(M>=5) = "+(float)offFaultMFD.getCumRate(5.05)+")");
//		ArrayList<EvenlyDiscretizedFunc> funcs = new ArrayList<EvenlyDiscretizedFunc>();
//		funcs.add(totGR);
//		funcs.add(offFaultMFD);
//		funcs.add(onFaultMFD);
//		funcs.add(totGR.getCumRateDistWithOffset());
//		funcs.add(offFaultMFD.getCumRateDistWithOffset());
//		funcs.add(onFaultMFD.getCumRateDistWithOffset());
//		GraphiWindowAPI_Impl graph = new GraphiWindowAPI_Impl(funcs, "MFDs");
//		graph.setX_AxisRange(5, 9);
//		graph.setY_AxisRange(1e-5, 20);
//		graph.setYLog(true);
//		graph.setX_AxisLabel("Mag");
//		graph.setY_AxisLabel("Rate (per year)");


		double offFaultReducedMoRate = offFaultMFD.getTotalMomentRate();
		double offCoupCoeff = offFaultReducedMoRate/offFaultOrigMoRate;
		double onFaultReducedMoRate = onFaultMFD.getTotalMomentRate();
		double onCoupCoeff = onFaultReducedMoRate/onFaultOrigMoRate;
		
		return (float)mMaxInRegion+"\t"+(float)onCoupCoeff+"\t"+(float)offCoupCoeff+"\t"+(float)moRateReduction+"\t"+
				(float)onFaultOrigMoRate+"\t"+(float)onFaultReducedMoRate+"\t"+
				(float)offFaultOrigMoRate+"\t"+(float)offFaultReducedMoRate+"\t"+maxOffMagWithFullMoment+"\t"+(float)aveMinSeismoMag;
		
	}

	
	/**
	 * This solves for the on-fault target MFD for the characteristic model, where the off-fault wedge 
	 * is removed at lower magnitudes (up to the maximum mag of off-fault seismicity), and such that
	 * the total desired rate of events on fault is maintained.  This assumes the target MFD has a b-value
	 * of about 1.0 (or more precisely, that the final low-mag on-fault b-value is less that 1.0).
	 * 
	 * Note that you can get the implied off-fault mMax by calling the MFDs getMaxMagWithNonZeroRate() method.
	 * 
	 * Warning - this makes no checks that inputs are reasonable (e.g., that totOnFaultMgt5_Rate <= totalTargetGR.getCumRate(transMagIndex))
	 * 
	 * @param totalTargetGR - the total target GR for the region.
	 * @param totOnFaultMgt5_Rate - the total target rate of events on-fault for M ge 5.
	 * @param mMaxOffFaultIndex - the last non-zero magnitude index for off-fault seismicity (one mag bin below transition mag).
	 * @return on-fault mfd is in array element [0] and off-fault mfd is in [1]
	 */
	public static IncrementalMagFreqDist[] old_getCharOnFaultTargetMFD(GutenbergRichterMagFreqDist totalTargetGR, double totOnFaultMgt5_Rate, int mMaxOffFaultIndex) {
		
		int transMagIndex = mMaxOffFaultIndex+1;
		int mag5_Index = totalTargetGR.getXIndex(5.05);
		
		double onFaultRateBelowTransMag = totOnFaultMgt5_Rate - totalTargetGR.getCumRate(transMagIndex);
		
		// b-value is between 0 and 1, so make function and interpolate
		ArbitrarilyDiscretizedFunc rateVsBvalueFunc = new ArbitrarilyDiscretizedFunc();
		double b_incr = 0.01;
		for(double b=-1; b<=1.0; b+=b_incr) {
			GutenbergRichterMagFreqDist tempGR = new GutenbergRichterMagFreqDist(totalTargetGR.getMinX(), totalTargetGR.getNum(), 
					totalTargetGR.getDelta(), totalTargetGR.getMagLower(), totalTargetGR.getMagUpper(), 1.0, b);
			tempGR.scaleToIncrRate(transMagIndex, totalTargetGR.getY(transMagIndex));
			double testOnFaultRateBelowTransMag = tempGR.getCumRate(mag5_Index) - tempGR.getCumRate(transMagIndex);
			rateVsBvalueFunc.set(testOnFaultRateBelowTransMag,b);
		}
		
		double bVal = rateVsBvalueFunc.getInterpolatedY(onFaultRateBelowTransMag);
		
		GutenbergRichterMagFreqDist tempGR = new GutenbergRichterMagFreqDist(totalTargetGR.getMinX(), totalTargetGR.getNum(), 
				totalTargetGR.getDelta(), totalTargetGR.getMagLower(), totalTargetGR.getMagUpper(), 1.0, bVal);
		tempGR.scaleToIncrRate(transMagIndex, totalTargetGR.getY(transMagIndex));	// match rates at transition mag
		
		IncrementalMagFreqDist onFaultMFD = new IncrementalMagFreqDist(totalTargetGR.getMinX(), totalTargetGR.getNum(), totalTargetGR.getDelta());		
		IncrementalMagFreqDist offFaultMFD = new IncrementalMagFreqDist(totalTargetGR.getMinX(), totalTargetGR.getNum(), totalTargetGR.getDelta());		
		for(int i=0; i<transMagIndex;i++) {
			onFaultMFD.set(i,tempGR.getY(i));
			offFaultMFD.set(i,totalTargetGR.getY(i)-tempGR.getY(i));
		}
		for(int i=transMagIndex; i<totalTargetGR.getNum(); i++) {
			onFaultMFD.set(i,totalTargetGR.getY(i));
		}

		onFaultMFD.setName("onFaultMFD");
		onFaultMFD.setInfo("(rate(M>=5)="+(float)onFaultMFD.getCumRate(mag5_Index)+"; transMag="+onFaultMFD.getX(transMagIndex)+")");
		offFaultMFD.setName("offFaultMFD");
		offFaultMFD.setInfo("(rate(M>=5)="+(float)offFaultMFD.getCumRate(mag5_Index)+"; maxMag="+onFaultMFD.getX(transMagIndex-1)+")");
		

		// TESTS
		System.out.println("\nInputs:\n");
		System.out.println("\ttotOnFaultMgt5_Rate="+(float)totOnFaultMgt5_Rate);
		double totRate = totalTargetGR.getCumRate(5.05);
		double totMoRate = totalTargetGR.getTotalMomentRate();
		System.out.println("\ttotalTargetGR.getCumRate(5.05)="+(float)totRate);
		System.out.println("\ttotalTargetGR.getTotalMomentRate()="+(float)totMoRate);
		System.out.println("\ttransMag="+(float)onFaultMFD.getX(transMagIndex));
		System.out.println("\tmaxMagOff="+(float)onFaultMFD.getX(transMagIndex-1));

		System.out.println("\nResults:\n");
		System.out.println("\tonFaultMFD.getCumRate(5.05)="+(float)onFaultMFD.getCumRate(5.05)+"\tfraction="+((float)(onFaultMFD.getCumRate(5.05)/totRate)));
		System.out.println("\toffFaultMFD.getCumRate(5.05)="+(float)offFaultMFD.getCumRate(5.05)+"\tfraction="+((float)(offFaultMFD.getCumRate(5.05)/totRate)));
		System.out.println("\tonFaultMFD.getTotalMomentRate()="+(float)onFaultMFD.getTotalMomentRate()+"\tfraction="+((float)(onFaultMFD.getTotalMomentRate()/totMoRate)));
		System.out.println("\toffFaultMFD.getTotalMomentRate()="+(float)offFaultMFD.getTotalMomentRate()+"\tfraction="+((float)(offFaultMFD.getTotalMomentRate()/totMoRate)));
		System.out.println("\nTests (all should be close to 1.0):\n");
		System.out.println("\tTotMoRate: "+(float)(totMoRate/(onFaultMFD.getTotalMomentRate()+offFaultMFD.getTotalMomentRate()))+"\t(totMoRate/(onFaultMFD.getTotalMomentRate()+offFaultMFD.getTotalMomentRate()))");
		System.out.println("\tTotCumRate: "+(float)(totRate/(onFaultMFD.getCumRate(5.05)+offFaultMFD.getCumRate(5.05)))+"\t(totRate/(onFaultMFD.getCumRate(5.05)+offFaultMFD.getCumRate(5.05)))");
		System.out.println("\tOnFaultCumRate: "+(float)(totOnFaultMgt5_Rate/onFaultMFD.getCumRate(5.05))+"\t(totOnFaultMgt5_Rate/onFaultMFD.getCumRate(5.05))");
		System.out.println("\tOffFaultCumRate: "+(float)((totRate-totOnFaultMgt5_Rate)/+offFaultMFD.getCumRate(5.05))+"\t((totRate-totOnFaultMgt5_Rate)/+offFaultMFD.getCumRate(5.05))");

		ArrayList<EvenlyDiscretizedFunc> funcs = new ArrayList<EvenlyDiscretizedFunc>();
		funcs.add(totalTargetGR);
		funcs.add(offFaultMFD);
		funcs.add(onFaultMFD);
		funcs.add(totalTargetGR.getCumRateDistWithOffset());
		funcs.add(offFaultMFD.getCumRateDistWithOffset());
		funcs.add(onFaultMFD.getCumRateDistWithOffset());
		GraphiWindowAPI_Impl graph = new GraphiWindowAPI_Impl(funcs, "MFDs");
		graph.setX_AxisRange(5, 9);
		graph.setY_AxisRange(1e-5, 10);
		graph.setYLog(true);
		graph.setX_AxisLabel("Mag");
		graph.setY_AxisLabel("Rate (per year)");

		IncrementalMagFreqDist[] mfds = {onFaultMFD,offFaultMFD};
		return mfds;
	}
	
	

	/**
	 * This returns null if the moRateOffFault is too high or too low to be satisfied 
	 * (mMax would have to be lower than minOffFaultMag (given below) or higher than totalTargetGR.getMagUpper())
	 * @param moRateOffFault
	 * @param totalTargetGR
	 * @param totOnFaultMgt5_Rate
	 * @return
	 */
	public static IncrementalMagFreqDist[]  old_getCharOnFaultTargetMFD(double moRateOffFault, GutenbergRichterMagFreqDist totalTargetGR, double totOnFaultMgt5_Rate) {

		double minOffFaultMag = 6.05;
		double totMoRate = totalTargetGR.getTotalMomentRate();
		double moRateOnFault = totMoRate-moRateOffFault;
		
		// test whether given moRateOffFault is too high and can't be satisfied (if off-fault mMax is one below magUpper)
		int testIndex = totalTargetGR.getXIndex(totalTargetGR.getMagUpper()) - 1;
		IncrementalMagFreqDist[] testMFDs = old_getCharOnFaultTargetMFD(totalTargetGR, totOnFaultMgt5_Rate, testIndex);
		double maxOffFaultMoRate = testMFDs[1].getTotalMomentRate();
		if(maxOffFaultMoRate < moRateOffFault) {
			System.out.println("Error - Can't satisfy given moment (given="+moRateOffFault+" and max possible is "+maxOffFaultMoRate+")");
			return null;
		}
		
		// now test whether given moRateOffFault is too low (satisfying minOffFaultMag would have more moment rate)
		testIndex = totalTargetGR.getXIndex(minOffFaultMag) - 1;
		testMFDs = old_getCharOnFaultTargetMFD(totalTargetGR, totOnFaultMgt5_Rate, testIndex);
		double minOffFaultMoRate = testMFDs[1].getTotalMomentRate();
		if(minOffFaultMoRate > moRateOffFault) {
			System.out.println("Error - Can't satisfy given moment (given="+moRateOffFault+" and min possible is "+minOffFaultMoRate+")");
			return null;
		}

		double fracMoRateDiff = Double.MAX_VALUE;
		int transMagIndex=-1;
		IncrementalMagFreqDist onFaultMFD=null, offFaultMFD=null;
		
		// loop over mMax (from 6.55 to mag upper)
		for(int mMaxOffIndex=totalTargetGR.getXIndex(minOffFaultMag); mMaxOffIndex<totalTargetGR.getXIndex(totalTargetGR.getMagUpper()); mMaxOffIndex++) {
			// mMaxOffIndex ends at 1 increment below totalTargetGR.getMagUpper()
			IncrementalMagFreqDist[] mfds = old_getCharOnFaultTargetMFD(totalTargetGR, totOnFaultMgt5_Rate, mMaxOffIndex);
			IncrementalMagFreqDist tempOnFaultMFD = mfds[0];
			double thisMoRateFracDiff = Math.abs(moRateOnFault-tempOnFaultMFD.getTotalMomentRate())/moRateOnFault;
			if(thisMoRateFracDiff<fracMoRateDiff) {
				onFaultMFD = tempOnFaultMFD;
				offFaultMFD = mfds[1];
				transMagIndex = mMaxOffIndex+1;
				fracMoRateDiff=thisMoRateFracDiff;
			}

		}
		
		// make sure that offFaultMFD.getMinMagWithNonZeroRate() returns correct value
		double mMaxOff = offFaultMFD.getMaxMagWithNonZeroRate();
		double mMaxTest = totalTargetGR.getX(transMagIndex-1);
		if(mMaxOff != mMaxTest)
			throw new RuntimeException("Error: discrepancy with off-fault max mags: "+mMaxOff+" vs "+mMaxTest);

		// TESTS
//		System.out.println("Inputs:");
//		System.out.println("\ttotOnFaultMgt5_Rate="+(float)totOnFaultMgt5_Rate);
//		double totRate = totalTargetGR.getCumRate(5.05);
//		System.out.println("\ttotalTargetGR.getCumRate(5.05)="+(float)totRate);
//		System.out.println("\tmoRateOffFault="+(float)moRateOffFault);
//		System.out.println("\ttotalTargetGR.getTotalMomentRate()="+(float)totMoRate);
//		System.out.println("Results:");
//		System.out.println("\ttransMag="+(float)onFaultMFD.getX(transMagIndex));
//		System.out.println("\tmaxMagOff="+(float)totalTargetGR.getX(transMagIndex-1));
//		System.out.println("\tonFaultMFD.getCumRate(5.05)="+(float)onFaultMFD.getCumRate(5.05)+"\tfraction="+((float)(onFaultMFD.getCumRate(5.05)/totRate)));
//		System.out.println("\toffFaultMFD.getCumRate(5.05)="+(float)offFaultMFD.getCumRate(5.05)+"\tfraction="+((float)(offFaultMFD.getCumRate(5.05)/totRate)));
//		System.out.println("\tonFaultMFD.getTotalMomentRate()="+(float)onFaultMFD.getTotalMomentRate());
//		System.out.println("\toffFaultMFD.getTotalMomentRate()="+(float)offFaultMFD.getTotalMomentRate());
//		System.out.println("Tests (all should be close to 1.0):");
//		System.out.println("\tTotMoRate: "+(float)(totMoRate/(onFaultMFD.getTotalMomentRate()+offFaultMFD.getTotalMomentRate()))+"\t(totMoRate/(onFaultMFD.getTotalMomentRate()+offFaultMFD.getTotalMomentRate()))");
//		System.out.println("\tOnFaultMoRate: "+(float)((totMoRate-moRateOffFault)/onFaultMFD.getTotalMomentRate())+"\t((totMoRate-moRateOffFault)/onFaultMFD.getTotalMomentRate())");
//		System.out.println("\tOffFaultMoRate: "+(float)(moRateOffFault/offFaultMFD.getTotalMomentRate())+"\t(moRateOffFault/offFaultMFD.getTotalMomentRate())");
//		System.out.println("\tTotCumRate: "+(float)(totRate/(onFaultMFD.getCumRate(5.05)+offFaultMFD.getCumRate(5.05)))+"\t(totRate/(onFaultMFD.getCumRate(5.05)+offFaultMFD.getCumRate(5.05)))");
//		System.out.println("\tOnFaultCumRate: "+(float)(totOnFaultMgt5_Rate/onFaultMFD.getCumRate(5.05))+"\t(totOnFaultMgt5_Rate/onFaultMFD.getCumRate(5.05))");
//		System.out.println("\tOffFaultCumRate: "+(float)((totRate-totOnFaultMgt5_Rate)/+offFaultMFD.getCumRate(5.05))+"\t((totRate-totOnFaultMgt5_Rate)/+offFaultMFD.getCumRate(5.05))");
//
//		ArrayList<EvenlyDiscretizedFunc> funcs = new ArrayList<EvenlyDiscretizedFunc>();
//		funcs.add(totalTargetGR);
//		funcs.add(offFaultMFD);
//		funcs.add(onFaultMFD);
//		funcs.add(totalTargetGR.getCumRateDistWithOffset());
//		funcs.add(offFaultMFD.getCumRateDistWithOffset());
//		funcs.add(onFaultMFD.getCumRateDistWithOffset());
//		GraphiWindowAPI_Impl graph = new GraphiWindowAPI_Impl(funcs, "MFDs");
//		graph.setX_AxisRange(5, 9);
//		graph.setY_AxisRange(1e-5, 10);
//		graph.setYLog(true);
//		graph.setX_AxisLabel("Mag");
//		graph.setY_AxisLabel("Rate (per year)");

		IncrementalMagFreqDist[] mfds = {onFaultMFD,offFaultMFD};
		return mfds;
	}
	
	
	public static void testAllInversionSetups() {
		ArrayList<MagAreaRelationships> magAreaList = new ArrayList<MagAreaRelationships>();
		magAreaList.add(MagAreaRelationships.ELL_B);
		magAreaList.add(MagAreaRelationships.HB_08);
		magAreaList.add(MagAreaRelationships.SHAW_09_MOD);
		
		ArrayList<DeformationModels> defModList= new ArrayList<DeformationModels>();
		FaultModels fm = FaultModels.FM3_1;
		
		defModList.add(DeformationModels.ABM);
		defModList.add(DeformationModels.GEOLOGIC);
		defModList.add(DeformationModels.GEOLOGIC_PLUS_ABM);
		defModList.add(DeformationModels.NEOKINEMA);
//		defModList.add(DeformationModels.ZENG);
//		defModList.add(DeformationModels.GEOBOUND);
		
		double totRegionalM5_Rate = 8.7;
//		double[] fractSeisOffFault = {0.3,0.5,0.7};
		double[] mMaxOffFault = {7.05,7.35,7.65,7.95, 8.25};
		double[] fractSeisOffFault = {0.59};
//		double[] mMaxOffFault = {7.55};
		
		ArrayList<String> allLinesGR = new ArrayList<String>();
		ArrayList<String> allLinesChar = new ArrayList<String>();
		// for UCERF3
		for(DeformationModels dm :defModList) {
			for(MagAreaRelationships ma:magAreaList) {
				FaultSystemRupSet faultSysRupSet = InversionFaultSystemRupSetFactory.forBranch(fm, dm, 
						ma, AveSlipForRupModels.ELLSWORTH_B, SlipAlongRuptureModels.TAPERED, InversionModels.GR_CONSTRAINED);
				for(double frSeisOff : fractSeisOffFault) {
					for(double mMaxOff : mMaxOffFault) {
						String lineFirstPart = faultSysRupSet.getDeformationModel().getShortName()+"\t"+ma.getShortName()+
						"\t"+totRegionalM5_Rate+"\t"+frSeisOff+"\t"+mMaxOff+"\t";
						allLinesGR.add(lineFirstPart+testInversionGR_Setup(totRegionalM5_Rate, frSeisOff, mMaxOff, faultSysRupSet));
						allLinesChar.add(lineFirstPart+testInversionCharSetup(totRegionalM5_Rate, frSeisOff, mMaxOff, faultSysRupSet));
					}			
				}		
			}
		}
		
		System.out.println("\nGR:");
		System.out.println("DefMod\tM(A)\tRge5\tfrSeisOff\tmMaxOff\tmMaxOn\tonCoupCoeff\toffCoupCoeff\tfrMoRateReduct\tonFltOrigMoRate\tonFltReducMoRate\toffFltOrigMoRate\toffFltReducMoRate\tmMaxOffWithFullMo\taveMinSeismoMag");
		for(String line:allLinesGR) {
			System.out.println(line);
		}
		
		System.out.println("\nChar:");
		System.out.println("DefMod\tM(A)\tRge5\tfrSeisOff\tmMaxOff\tmMaxOn\tonCoupCoeff\toffCoupCoeff\tfrMoRateReduct\tonFltOrigMoRate\tonFltReducMoRate\toffFltOrigMoRate\toffFltReducMoRate\tmMaxOffWithFullMo\taveMinSeismoMag");
		for(String line:allLinesChar) {
			System.out.println(line);
		}
		
		// now do UCERF2
//		fm = FaultModels.FM2_1;
//		DeformationModels dm = DeformationModels.UCERF2_ALL;
//		for(MagAreaRelationships ma:magAreaList) {
//			FaultSystemRupSet faultSysRupSet = InversionFaultSystemRupSetFactory.forBranch(fm, dm, 
//					ma, AveSlipForRupModels.ELLSWORTH_B, SlipAlongRuptureModels.TAPERED, InversionModels.GR);
//			String label = faultSysRupSet.getDeformationModel().getShortName()+"_"+ma.getShortName();
//			plotImpliedTotalSectGR_MFD(faultSysRupSet, label);				
//		}
	}
	
	
	/**
	 * This computes a tri-linear MFD for the off-fault MFD on the characteristic branch
	 * 
	 * @param totalTargetGR - the total target MFD
	 * @param totOnFaultMgt5_Rate - the rate of on-fault events at Mge5
	 * @param mMinSeismoOnFault - the average minimum magnitude for seismogenic on-fault ruptures
	 * @param mMaxOffFault - the maximum magnitude for off-fault events.
	 * @return
	 */
	public static IncrementalMagFreqDist getTriLinearCharOffFaultTargetMFD(GutenbergRichterMagFreqDist totalTargetGR, double totOnFaultMgt5_Rate, 
			double mMinSeismoOnFault, double mMaxOffFault) {
		
		int mMinSeismoOnFaultIndex = totalTargetGR.getClosestXIndex(mMinSeismoOnFault);
		int mMaxOffFaultIndex = totalTargetGR.getClosestXIndex(mMaxOffFault);
		
		double offFaultMgt5_Rate = totalTargetGR.getCumRate(5.05) - totOnFaultMgt5_Rate;
		
		// rate corrections since final MFDs are not perfect GRs (found by hand)
		double onCorr = 0.98;
		double offCorr = 1.01;
		
		// create a temp GR with on-fault rate
		GutenbergRichterMagFreqDist tempOnFaultGR = new GutenbergRichterMagFreqDist(totalTargetGR.getMinX(), totalTargetGR.getNum(), 
				totalTargetGR.getDelta(), totalTargetGR.getMagLower(), totalTargetGR.getMagUpper(), 1.0, 1.0);
		tempOnFaultGR.scaleToCumRate(5.05, totOnFaultMgt5_Rate*onCorr);

		// create a temp GR with off-fault rate
		GutenbergRichterMagFreqDist tempOffFaultGR = new GutenbergRichterMagFreqDist(totalTargetGR.getMinX(), totalTargetGR.getNum(), 
				totalTargetGR.getDelta(), totalTargetGR.getMagLower(), totalTargetGR.getMagUpper(), 1.0, 1.0);
		tempOffFaultGR.scaleToCumRate(5.05, offFaultMgt5_Rate*offCorr);

		// now create the desired MFDs
		IncrementalMagFreqDist onFaultMFD = new IncrementalMagFreqDist(totalTargetGR.getMinX(), totalTargetGR.getNum(), totalTargetGR.getDelta());		
		IncrementalMagFreqDist offFaultMFD = new IncrementalMagFreqDist(totalTargetGR.getMinX(), totalTargetGR.getNum(), totalTargetGR.getDelta());		
		for(int i=0; i<mMinSeismoOnFaultIndex;i++) {
			onFaultMFD.set(i,tempOnFaultGR.getY(i));
			offFaultMFD.set(i,tempOffFaultGR.getY(i));
		}
		for(int i=mMinSeismoOnFaultIndex; i<=mMaxOffFaultIndex+1; i++) {
			double wtOnTotRate = (double)(i-mMinSeismoOnFaultIndex)/(double)((mMaxOffFaultIndex+1)-mMinSeismoOnFaultIndex); // starts at zero and builds
			double wtOnFaultRate = 1.0-wtOnTotRate;
			// way 1
//			double onFltRate = wtOnFaultRate*tempOnFaultGR.getY(i)+wtOnTotRate*totalTargetGR.getY(i);
//			onFaultMFD.set(i,onFltRate);
//			offFaultMFD.set(i,totalTargetGR.getY(i)-onFltRate);
			// way 2 (same as Way 1)
//			offFaultMFD.set(i,tempOffFaultGR.getY(i)*wtOnFaultRate);
//			onFaultMFD.set(i,totalTargetGR.getY(i)-offFaultMFD.getY(i));
			// way 3
			double onFltRate = Math.pow(10,wtOnFaultRate*Math.log10(tempOnFaultGR.getY(i)) + wtOnTotRate*Math.log10(totalTargetGR.getY(i)));
			onFaultMFD.set(i,onFltRate);
			offFaultMFD.set(i,totalTargetGR.getY(i)-onFltRate);
			if(offFaultMFD.getY(i) < 0 ) offFaultMFD.set(i,0); // numerical precision issue at last point if mMaxOffFault = totalTargetGR.getMagUpper()
		}
		for(int i=mMaxOffFaultIndex+1; i<totalTargetGR.getNum(); i++) {
			onFaultMFD.set(i,totalTargetGR.getY(i));
			offFaultMFD.set(i,0);
		}

		onFaultMFD.setName("onFaultMFD");
		onFaultMFD.setInfo("(rate(M>=5)="+(float)onFaultMFD.getCumRate(5.05)+"; totMoRate="+onFaultMFD.getTotalMomentRate()+")");
		offFaultMFD.setName("offFaultMFD");
		offFaultMFD.setInfo("(rate(M>=5)="+(float)offFaultMFD.getCumRate(5.05)+"; totMoRate="+offFaultMFD.getTotalMomentRate()+")");
		

//		// TESTS
//		System.out.println("\nInputs:\n");
//		System.out.println("\ttotOnFaultMgt5_Rate = "+(float)totOnFaultMgt5_Rate);
//		double totRate = totalTargetGR.getCumRate(5.05);
//		double totMoRate = totalTargetGR.getTotalMomentRate();
//		System.out.println("\ttotalTargetGR.getCumRate(5.05) = "+(float)totRate);
//		System.out.println("\ttotalTargetGR.getTotalMomentRate() = "+(float)totMoRate);
//		System.out.println("\tmMinSeismoOnFault="+(float)mMinSeismoOnFault);
//		System.out.println("\tmMaxOffFault="+(float)mMaxOffFault);
//
//		System.out.println("\nResults:\n");
//		System.out.println("\tonFaultMFD.getCumRate(5.05) = "+(float)onFaultMFD.getCumRate(5.05)+"\tfraction="+((float)(onFaultMFD.getCumRate(5.05)/totRate)));
//		System.out.println("\toffFaultMFD.getCumRate(5.05) = "+(float)offFaultMFD.getCumRate(5.05)+"\tfraction="+((float)(offFaultMFD.getCumRate(5.05)/totRate)));
//		System.out.println("\ttotal implied Rate(>=5.05) = "+(float)(offFaultMFD.getCumRate(5.05)+onFaultMFD.getCumRate(5.05)));
//		System.out.println("\tonFaultMFD.getTotalMomentRate() = "+(float)onFaultMFD.getTotalMomentRate()+"\tfraction="+((float)(onFaultMFD.getTotalMomentRate()/totMoRate)));
//		System.out.println("\toffFaultMFD.getTotalMomentRate() = "+(float)offFaultMFD.getTotalMomentRate()+"\tfraction="+((float)(offFaultMFD.getTotalMomentRate()/totMoRate)));
//		System.out.println("\nTests (all should be close to 1.0):\n");
//		System.out.println("\tTotMoRate: "+(float)(totMoRate/(onFaultMFD.getTotalMomentRate()+offFaultMFD.getTotalMomentRate()))+"\t(totMoRate/(onFaultMFD.getTotalMomentRate()+offFaultMFD.getTotalMomentRate()))");
//		System.out.println("\tTotCumRate: "+(float)(totRate/(onFaultMFD.getCumRate(5.05)+offFaultMFD.getCumRate(5.05)))+"\t(totRate/(onFaultMFD.getCumRate(5.05)+offFaultMFD.getCumRate(5.05)))");
//		System.out.println("\tOnFaultCumRate: "+(float)(totOnFaultMgt5_Rate/onFaultMFD.getCumRate(5.05))+"\t(totOnFaultMgt5_Rate/onFaultMFD.getCumRate(5.05))");
//		System.out.println("\tOffFaultCumRate: "+(float)((totRate-totOnFaultMgt5_Rate)/+offFaultMFD.getCumRate(5.05))+"\t((totRate-totOnFaultMgt5_Rate)/+offFaultMFD.getCumRate(5.05))");
//
//		ArrayList<EvenlyDiscretizedFunc> funcs = new ArrayList<EvenlyDiscretizedFunc>();
//		funcs.add(totalTargetGR);
//		funcs.add(offFaultMFD);
//		funcs.add(onFaultMFD);
////		funcs.add(totalTargetGR.getCumRateDistWithOffset());
////		funcs.add(offFaultMFD.getCumRateDistWithOffset());
////		funcs.add(onFaultMFD.getCumRateDistWithOffset());
//		GraphiWindowAPI_Impl graph = new GraphiWindowAPI_Impl(funcs, "MFDs");
//		graph.setX_AxisRange(5, 9);
//		graph.setY_AxisRange(1e-5, 10);
//		graph.setYLog(true);
//		graph.setX_AxisLabel("Mag");
//		graph.setY_AxisLabel("Rate (per year)");
//		graph.setPlotLabelFontSize(18);
//		graph.setAxisLabelFontSize(16);
//		graph.setTickLabelFontSize(14);

//		IncrementalMagFreqDist[] mfds = {onFaultMFD,offFaultMFD};
		return offFaultMFD;
	}
	
	/**
	 * This solves for the mMaxOffFault for the given moRateOffFault.
	 * This assumes that mMaxOffFault > mMinSeismoOnFault, and null is returned if it's 
	 * impossible to match the given moment (there is no mMaxOffFault large or small enough to match the given moment)
	 * 
	 * @param moRateOffFault
	 * @param totalTargetGR
	 * @param totOnFaultMgt5_Rate
	 * @param mMinSeismoOnFault
	 * @return
	 */
	public static IncrementalMagFreqDist getTriLinearCharOffFaultTargetMFD(double moRateOffFault, GutenbergRichterMagFreqDist totalTargetGR, double totOnFaultMgt5_Rate, 
			double mMinSeismoOnFault) {

		double min_mMaxOffFault = totalTargetGR.getX(totalTargetGR.getClosestXIndex(mMinSeismoOnFault)+1);	// the mimim that the off-fault Mmax can be
		
		// test whether given moRateOffFault is too high and can't be satisfied (if off-fault mMax is one below magUpper)
		double magUpper = totalTargetGR.getMagUpper();
		IncrementalMagFreqDist maxOffFaultMFD = getTriLinearCharOffFaultTargetMFD(totalTargetGR, totOnFaultMgt5_Rate, mMinSeismoOnFault, magUpper);
		double maxOffFaultMoRate = maxOffFaultMFD.getTotalMomentRate();
// System.out.println("maxOffFaultMoRate="+(float)maxOffFaultMoRate);
		if(maxOffFaultMoRate < moRateOffFault) {
			System.out.println("Error - Can't satisfy given moment (given="+moRateOffFault+" and max possible is "+maxOffFaultMoRate+")");
			return null;
		}
		
		// now test whether given moRateOffFault is too low (satisfying minOffFaultMag would have more moment rate)
		IncrementalMagFreqDist minOffFaultMFD = getTriLinearCharOffFaultTargetMFD(totalTargetGR, totOnFaultMgt5_Rate, mMinSeismoOnFault, min_mMaxOffFault);
		double minOffFaultMoRate = minOffFaultMFD.getTotalMomentRate();
// System.out.println("minOffFaultMoRate="+(float)minOffFaultMoRate);
		if(minOffFaultMoRate > moRateOffFault) {
			System.out.println("Error - Can't satisfy given moment (given="+moRateOffFault+" and min possible is "+minOffFaultMoRate+")");
			return null;
		}

		IncrementalMagFreqDist offFaultMFD=null;
		double fracMoRateDiff = Double.MAX_VALUE;
		double mMaxOff=0;
		// now loop over all mMaxOff values and keep the one with the clostest moment rate
		for(int mMaxOffIndex=totalTargetGR.getXIndex(min_mMaxOffFault); mMaxOffIndex<=totalTargetGR.getXIndex(totalTargetGR.getMagUpper()); mMaxOffIndex++) {
			double test_mMaxOff = totalTargetGR.getX(mMaxOffIndex);
			IncrementalMagFreqDist testOffFaultMFD = getTriLinearCharOffFaultTargetMFD(totalTargetGR, totOnFaultMgt5_Rate, mMinSeismoOnFault, test_mMaxOff);
			double thisMoRateFracDiff = Math.abs(moRateOffFault-testOffFaultMFD.getTotalMomentRate())/moRateOffFault;
			System.out.println(mMaxOffIndex+"\t"+(float)test_mMaxOff+"\t"+(float)thisMoRateFracDiff);
			if(thisMoRateFracDiff<fracMoRateDiff) {
				offFaultMFD = testOffFaultMFD;
				mMaxOff = test_mMaxOff;
				fracMoRateDiff=thisMoRateFracDiff;
			}
		}
		
		// make sure that offFaultMFD.getMinMagWithNonZeroRate() returns correct value
		double mMaxTest = offFaultMFD.getMaxMagWithNonZeroRate();
		if(mMaxOff != mMaxTest)
			throw new RuntimeException("Error: discrepancy with off-fault max mags: "+mMaxOff+" vs "+mMaxTest);

		System.out.println("mMaxOff = "+mMaxOff);
		double finalMoRate = offFaultMFD.getTotalMomentRate();
		System.out.println("moRate (and ratio to target) = "+(float)finalMoRate+"\t("+(float)(finalMoRate/moRateOffFault)+")");
		double targetRate = totalTargetGR.getCumRate(5.05)-totOnFaultMgt5_Rate;
		double finalRate = offFaultMFD.getCumRate(5.05);
		System.out.println("rate(M>=5) (and ratio to target) = "+(float)finalRate+"\t("+(float)(finalRate/targetRate)+")");

		return offFaultMFD;
	}
	
	
	/**
	 * This gets the total (summed) sub-seismogenic MFD for all fault sections for the characteristic model 
	 * (summing the MFDs from getCharSubSeismoOnFaultMFD_forEachSection(*))
	 * @param fltSysRupSet
	 * @param spatialSeisPDF
	 * @param totalTargetGR
	 * @return
	 */
	public static SummedMagFreqDist getCharSubSeismoOnFaultMFD(FaultSystemRupSet fltSysRupSet, SpatialSeisPDF spatialSeisPDF, 
			GutenbergRichterMagFreqDist totalTargetGR) {
		SummedMagFreqDist mfd = new SummedMagFreqDist(totalTargetGR.getMinX(), totalTargetGR.getNum(), totalTargetGR.getDelta());
		for(GutenbergRichterMagFreqDist gr : getCharSubSeismoOnFaultMFD_forEachSection(fltSysRupSet, spatialSeisPDF, totalTargetGR)) {
			mfd.addIncrementalMagFreqDist(gr);
		}
		
		ArrayList<IncrementalMagFreqDist> funcs = new ArrayList<IncrementalMagFreqDist>();
		funcs.add(mfd);
		funcs.add(totalTargetGR);
		GraphiWindowAPI_Impl graph = new GraphiWindowAPI_Impl(funcs, "Total On Fault MFD");

		return mfd;
	}


	/**
	 * This gets the sub-seismogenic MFD for each fault section for the characteristic model,
	 * where each fault gets a GR up to just below the minimum seismogenic magnitude, with a total rate
	 * equal to the rate of events inside the fault section polygon (as determined by the
	 * spatialSeisPDF and tatal regional rate).
	 * @param fltSysRupSet
	 * @param spatialSeisPDF
	 * @param totalTargetGR
	 * @return
	 */
	public static ArrayList<GutenbergRichterMagFreqDist> getCharSubSeismoOnFaultMFD_forEachSection(FaultSystemRupSet fltSysRupSet, 
			SpatialSeisPDF spatialSeisPDF, GutenbergRichterMagFreqDist totalTargetGR) {
		
		ArrayList<GutenbergRichterMagFreqDist> mfds = new ArrayList<GutenbergRichterMagFreqDist>();
		double totMgt5_rate = totalTargetGR.getCumRate(5.05);
		GriddedSeisUtils gridSeisUtils = new GriddedSeisUtils(fltSysRupSet.getFaultSectionDataList(), spatialSeisPDF);
		for(int s=0; s<fltSysRupSet.getNumSections(); s++) {
			double sectRate = gridSeisUtils.pdfValForSection(s)*totMgt5_rate;
			int mMaxIndex = totalTargetGR.getClosestXIndex(fltSysRupSet.getMinMagForSection(s))-1;	// subtract 1 to avoid overlap
			double mMax = totalTargetGR.getX(mMaxIndex); // rounded to nearest MFD value
			GutenbergRichterMagFreqDist tempOnFaultGR = new GutenbergRichterMagFreqDist(totalTargetGR.getMinX(), totalTargetGR.getNum(), 
					totalTargetGR.getDelta(), totalTargetGR.getMagLower(), mMax, 1.0, 1.0);
			tempOnFaultGR.scaleToCumRate(5.05, sectRate);
			mfds.add(tempOnFaultGR);
		}
		return mfds;
	}

	
	public static void plotPreInversionMFDs(InversionFaultSystemRupSet invFltSysRupSet) {
		
		InversionMFDs inversionMFDs = invFltSysRupSet.getInversionMFDs();
		
		SummedMagFreqDist targetOnFaultSupraSeisMFD  =inversionMFDs.getTargetOnFaultSupraSeisMFD();
		targetOnFaultSupraSeisMFD.setName("targetOnFaultSupraSeisMFD");
		targetOnFaultSupraSeisMFD.setInfo("Rate(M>=5)="+(float)targetOnFaultSupraSeisMFD.getCumRate(5.05)+"\tMoRate="+(float)targetOnFaultSupraSeisMFD.getTotalMomentRate());
		
		IncrementalMagFreqDist trulyOffFaultMFD = inversionMFDs.getTrulyOffFaultMFD();
		trulyOffFaultMFD.setName("trulyOffFaultMFD");
		trulyOffFaultMFD.setInfo("Rate(M>=5)="+(float)trulyOffFaultMFD.getCumRate(5.05)+"\tMoRate="+(float)trulyOffFaultMFD.getTotalMomentRate());
				
		SummedMagFreqDist totalSubSeismoOnFaultMFD = inversionMFDs.getTotalSubSeismoOnFaultMFD();
		totalSubSeismoOnFaultMFD.setName("totalSubSeismoOnFaultMFD");
		totalSubSeismoOnFaultMFD.setInfo("Rate(M>=5)="+(float)totalSubSeismoOnFaultMFD.getCumRate(5.05)+"\tMoRate="+(float)totalSubSeismoOnFaultMFD.getTotalMomentRate());
		
		GutenbergRichterMagFreqDist totalTargetGR = inversionMFDs.getTotalTargetGR();
		totalTargetGR.setName("totalTargetGR");
		totalTargetGR.setInfo("Rate(M>=5)="+(float)totalTargetGR.getCumRate(5.05)+"\tMoRate="+(float)totalTargetGR.getTotalMomentRate());
		
		IncrementalMagFreqDist noCalTargetFaultMFD  =inversionMFDs.getMFD_ConstraintsForNoAndSoCal().get(0).getMagFreqDist();
		noCalTargetFaultMFD.setName("noCalTargetFaultMFD");
		noCalTargetFaultMFD.setInfo("Rate(M>=5)="+(float)noCalTargetFaultMFD.getCumRate(5.05)+"\tMoRate="+(float)noCalTargetFaultMFD.getTotalMomentRate());

		IncrementalMagFreqDist soCalTargetFaultMFD  =inversionMFDs.getMFD_ConstraintsForNoAndSoCal().get(1).getMagFreqDist();
		soCalTargetFaultMFD.setName("soCalTargetFaultMFD");
		soCalTargetFaultMFD.setInfo("Rate(M>=5)="+(float)soCalTargetFaultMFD.getCumRate(5.05)+"\tMoRate="+(float)soCalTargetFaultMFD.getTotalMomentRate());

		SummedMagFreqDist testTarget = new SummedMagFreqDist(soCalTargetFaultMFD.getX(0),soCalTargetFaultMFD.getNum(),soCalTargetFaultMFD.getDelta());
		testTarget.addIncrementalMagFreqDist(noCalTargetFaultMFD);
		testTarget.addIncrementalMagFreqDist(soCalTargetFaultMFD);
		testTarget.setName("testTarget (should equal targetOnFaultSupraSeisMFD");
		testTarget.setInfo("Rate(M>=5)="+(float)testTarget.getCumRate(5.05)+"\tMoRate="+(float)testTarget.getTotalMomentRate());

		
		ArrayList<IncrementalMagFreqDist> mfds = new ArrayList<IncrementalMagFreqDist>();
		mfds.add(targetOnFaultSupraSeisMFD);
		mfds.add(trulyOffFaultMFD);
		mfds.add(totalSubSeismoOnFaultMFD);
		mfds.add(totalTargetGR);
		mfds.add(noCalTargetFaultMFD);
		mfds.add(soCalTargetFaultMFD);
		mfds.add(testTarget);
		
		GraphiWindowAPI_Impl graph = new GraphiWindowAPI_Impl(mfds, "Pre-Inversion MFDs");
		graph.setX_AxisRange(5, 9);
		graph.setY_AxisRange(1e-5, 20);
		graph.setYLog(true);
		graph.setX_AxisLabel("Mag");
		graph.setY_AxisLabel("Rate (per year)");

		graph.setTickLabelFontSize(14);
		graph.setAxisLabelFontSize(16);
		graph.setPlotLabelFontSize(18);


	}
	
	
	public static String calcImplDDWvsDDW_Ratio(InversionFaultSystemRupSet invFltSysRupSet) {
		String result = "";
		
//		DefaultXY_DataSet ratioVsLengthData = new DefaultXY_DataSet();
		HistogramFunction hist = new HistogramFunction(0.1, 100, 0.1);
		double ave=0, min=Double.MAX_VALUE, max=Double.NEGATIVE_INFINITY;
		int minIndex=-1;
		for(int r=0; r<invFltSysRupSet.getNumRuptures();r++) {
			double moment = MagUtils.magToMoment(invFltSysRupSet.getMagForRup(r));
			double slip = invFltSysRupSet.getAveSlipForRup(r);
			double length = invFltSysRupSet.getLengthForRup(r);	
			double implWidth = moment/(slip*length*FaultMomentCalc.SHEAR_MODULUS);
			
			double width = invFltSysRupSet.getAveWidthForRup(r);
			
			double ratio = implWidth/width;
			hist.add(ratio, 1.0);
			ave += ratio;
			if(min>ratio) {
				min=ratio;
				if(ratio >=1.01 || ratio <= 0.99)
					minIndex=r;
			}
			if(max<ratio) max=ratio;
			
//			ratioVsLengthData.set(length/1000d, ratio);
		}
		
		ave /= invFltSysRupSet.getNumRuptures();
		
//		GraphiWindowAPI_Impl graph = new GraphiWindowAPI_Impl(ratioVsLengthData, "ratio vs length");
//		graph.setX_AxisLabel("length (km)");
//		graph.setY_AxisLabel("implied/actual DDW");
		
		result = "\taveRatio="+(float)ave+"\tmin="+(float)min+"\tmax="+(float)max+"\tminIndex="+minIndex+"\n";
		result += "\tnon-zero bins (ratio, num rups):\n";
		for(int i=0; i<hist.getNum();i++)
			if(hist.getY(i) > 0.0)
				result += "\t\t"+(float)hist.getX(i)+"\t"+(float)hist.getY(i)+"\n";
		
//		System.out.println("aveRatio="+ave+"\tmin="+min+"\tmax="+max+"\tminIndex="+minIndex+"\n");
		
//		ArrayList<Integer>  secIndicesForRup = invFltSysRupSet.getSectionsIndicesForRup(minIndex);
//		for(int i:secIndicesForRup) {
//			System.out.println(invFltSysRupSet.getFaultSectionData(i).getName());
//		}

		return result;
	}
	
	
	public static void testAllImpliedDDWs() {
		String result="";
		
		ArrayList<MagAreaRelationships> magAreaList = new ArrayList<MagAreaRelationships>();
		magAreaList.add(MagAreaRelationships.ELL_B);
		magAreaList.add(MagAreaRelationships.HB_08);
		magAreaList.add(MagAreaRelationships.SHAW_09_MOD);
		
		ArrayList<AveSlipForRupModels> aveSlipForRupModelsList= new ArrayList<AveSlipForRupModels>();
		aveSlipForRupModelsList.add(AveSlipForRupModels.ELLSWORTH_B);
		aveSlipForRupModelsList.add(AveSlipForRupModels.HANKS_BAKUN_08);
		aveSlipForRupModelsList.add(AveSlipForRupModels.SHAW_2009_MOD);
		aveSlipForRupModelsList.add(AveSlipForRupModels.SHAW12_SQRT_LENGTH);
		aveSlipForRupModelsList.add(AveSlipForRupModels.SHAW_12_CONST_STRESS_DROP);
		
		
		FaultModels fm = FaultModels.FM3_1;
		DeformationModels dm = DeformationModels.GEOLOGIC;
		
		result += "RESULTS FOR:\t"+fm+"  &  "+dm+"\n";
		
		for(MagAreaRelationships ma : magAreaList) {
			for(AveSlipForRupModels asm : aveSlipForRupModelsList) {
				InversionFaultSystemRupSet faultSysRupSet = InversionFaultSystemRupSetFactory.forBranch(fm, dm, 
						ma, asm, SlipAlongRuptureModels.TAPERED, InversionModels.GR_CONSTRAINED);
				result += "\n"+ma+"\t"+asm+":\n";
				result += calcImplDDWvsDDW_Ratio(faultSysRupSet);
						
			}
		}
		
		System.out.println(result);

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		
//		testAllInversionSetups();

//		double mMaxInRegion = 8.45;
//		double totRegionalM5_Rate = 8.7;
//		double totOnFaultMgt5_Rate = totRegionalM5_Rate*0.6;
//		double mMaxOffFault = 7.95;
//		double mMinSeismoOnFault = 6.45;
//		GutenbergRichterMagFreqDist totGR = new GutenbergRichterMagFreqDist(0.05, 90, 0.1, 0.05, mMaxInRegion, 1.0, 1.0);
//		totGR.scaleToCumRate(0, totRegionalM5_Rate*1e5);
////		old_getCharOnFaultTargetMFD(totGR, totOnFaultMgt5_Rate, totGR.getXIndex(mMaxOffFault));
//		old_getCharOnFaultTargetMFD(1E18, totGR, totOnFaultMgt5_Rate);
//		getTriLinearCharOffFaultTargetMFD(totGR, totOnFaultMgt5_Rate, mMinSeismoOnFault, mMaxOffFault);
//		getTriLinearCharOffFaultTargetMFD(4E18, totGR, totOnFaultMgt5_Rate,mMinSeismoOnFault);
		
		testAllImpliedDDWs();
		
//		LaughTestFilter laughTest = LaughTestFilter.getDefault();
//		double defaultAseismicityValue = InversionFaultSystemRupSetFactory.DEFAULT_ASEIS_VALUE;
//
//		InversionFaultSystemRupSet faultSysRupSet = InversionFaultSystemRupSetFactory.forBranch(laughTest, defaultAseismicityValue,
//				FaultModels.FM3_1, DeformationModels.GEOLOGIC, MagAreaRelationships.SHAW_09_MOD,
//				AveSlipForRupModels.SHAW_12_CONST_STRESS_DROP, SlipAlongRuptureModels.TAPERED,
//				InversionModels.CHAR_CONSTRAINED, TotalMag5Rate.RATE_8p8, MaxMagOffFault.MAG_7p65,
//				ApplyImpliedCouplingCoeff.FALSE, SpatialSeisPDF.UCERF3);
//
//		System.out.println(calcImplDDWvsDDW_Ratio(faultSysRupSet));
		
//		plotPreInversionMFDs(faultSysRupSet);
//		
//		System.out.println("getFractSpatialPDF_InsideSectionPolygons(faultSysRupSet, SpatialSeisPDF.UCERF3)="+getFractSpatialPDF_InsideSectionPolygons(faultSysRupSet, SpatialSeisPDF.UCERF3));
//		System.out.println("getFractSpatialPDF_InsideSectionPolygons(faultSysRupSet, SpatialSeisPDF.UCERF2)="+getFractSpatialPDF_InsideSectionPolygons(faultSysRupSet, SpatialSeisPDF.UCERF2));
		
		
//		getCharSubSeismoOnFaultMFD(faultSysRupSet, SpatialSeisPDF.UCERF3, totGR);

//
//		System.out.println("getMeanMinMag="+getMeanMinMag(faultSysRupSet, true));
//		System.out.println("getMeanMinMag="+getMeanMinMag(faultSysRupSet, false));

//		FaultSystemRupSet faultSysRupSet = InversionFaultSystemRupSetFactory.forBranch(FaultModels.FM3_1, DeformationModels.GEOLOGIC, 
//					MagAreaRelationships.ELL_B, AveSlipForRupModels.ELLSWORTH_B, SlipAlongRuptureModels.TAPERED, InversionModels.GR);
//		HistogramFunction hist1 = getMinMagHistogram(faultSysRupSet, 5.05, 40, 0.1, false);
//		HistogramFunction hist2 = getMinMagHistogram(faultSysRupSet, 5.05, 40, 0.1, true);
//		ArrayList<HistogramFunction> hists = new ArrayList<HistogramFunction>();
//		hists.add(hist1.getCumulativeDistFunction());
//		hists.add(hist2.getCumulativeDistFunction());
//		GraphiWindowAPI_Impl graph = new GraphiWindowAPI_Impl(hists, "Histograms"); 
//		graph.setX_AxisLabel("Magnitude");
//		graph.setY_AxisLabel("Normalized Number");


		
		

		
//		plotAllImpliedTotalSectGR_MFD();
		
//		FaultSystemRupSet faultSysRupSet = InversionFaultSystemRupSetFactory.forBranch(FaultModels.FM3_1, DeformationModels.GEOLOGIC, 
//				MagAreaRelationships.HB_08, AveSlipForRupModels.ELLSWORTH_B, SlipAlongRuptureModels.TAPERED, InversionModels.GR);
//		getMomentRateReductionHistogram(faultSysRupSet, true, true);
//		plotAllHistograms(faultSysRupSet, 5.05,40,0.1, true);

		
//		getFractMomentReductionForSmallMags(6.0, 8.0, 1.0);
//		getFractMomentReductionForSmallMags(7.0, 8.0, 1.0);
		
//   		try {
//  			System.out.println("Getting rup set");
//			FaultSystemRupSet faultSysRupSet = InversionFaultSystemRupSetFactory.cachedForBranch(
//					FaultModels.FM2_1, DeformationModels.UCERF2_ALL, InversionModels.GR, true);
			
			
//			System.out.println(getMinMagHistogram(faultSysRupSet, 5.05,40,0.1, true).getCumulativeDistFunction());

//   		} catch (IOException e) {
//			e.printStackTrace();
//		}

	}

}
