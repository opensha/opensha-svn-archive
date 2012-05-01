package scratch.UCERF3.griddedSeismicity;

import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.SwingUtilities;

import org.opensha.commons.data.function.EvenlyDiscretizedFunc;
import org.opensha.commons.gui.plot.PlotLineType;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.opensha.sha.gui.infoTools.GraphiWindowAPI_Impl;
import org.opensha.sha.gui.infoTools.PlotCurveCharacterstics;
import org.opensha.sha.magdist.IncrementalMagFreqDist;
import org.opensha.sha.magdist.SummedMagFreqDist;

import com.google.common.base.CaseFormat;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import scratch.UCERF3.SimpleFaultSystemSolution;
import scratch.UCERF3.inversion.InversionFaultSystemSolution;

/**
 * Add comments here
 *
 * 
 * @author Peter Powers
 * @version $Id:$
 */
public class GridSrcMFDPlotter {
	
	private static InversionFaultSystemSolution fss;
	private static ArrayList<PlotCurveCharacterstics> plotChars;
	private static SmallMagScaling magScaling = SmallMagScaling.MO_REDUCTION;
	private static boolean incremental = false;
	private static String fName = "tmp/invSols/reference_gr_sol2.zip";
	
	static {
		
		// init fss
		try {
			File f = new File(fName);
			SimpleFaultSystemSolution tmp = SimpleFaultSystemSolution.fromFile(f);
			fss = new InversionFaultSystemSolution(tmp);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// init plot characteristics
		plotChars = Lists.newArrayList(
			new PlotCurveCharacterstics(PlotLineType.SOLID,2f, Color.BLACK),
			new PlotCurveCharacterstics(PlotLineType.SOLID,2f, Color.MAGENTA.darker()),
			new PlotCurveCharacterstics(PlotLineType.SOLID,2f, Color.BLUE.darker()),
			new PlotCurveCharacterstics(PlotLineType.SOLID,2f, Color.ORANGE.darker()),
			new PlotCurveCharacterstics(PlotLineType.SOLID,2f, Color.RED.brighter()),
			new PlotCurveCharacterstics(PlotLineType.SOLID,2f, Color.GREEN.darker()),
			new PlotCurveCharacterstics(PlotLineType.SOLID,2f, Color.CYAN.darker()),
			new PlotCurveCharacterstics(PlotLineType.SOLID,2f, Color.YELLOW.darker()));
	}
	
	
//	Map<String, FaultSectionPrefData> sectionMap;
	GridSrcMFDPlotter() {
		if (fName.contains("_ch_")) plotChar();
		if (fName.contains("_gr_")) plotGR();
	}
	
	void plotChar() {

		UCERF3_GridSourceGenerator gridGen = new UCERF3_GridSourceGenerator(
			fss, null, SpatialSeisPDF.UCERF3, 8.54, magScaling);
		System.out.println("init done");
		
		ArrayList<EvenlyDiscretizedFunc> funcs = Lists.newArrayList();
		
		// Total on-fault
		IncrementalMagFreqDist tOnIncr = fss.calcTotalNucleationMFD(5.05, 8.45, 0.1);
		tOnIncr.setName("Total on-fault MFD from inversion");
		EvenlyDiscretizedFunc tOnCum = tOnIncr.getCumRateDist();
		funcs.add(incremental ? tOnIncr : tOnCum);
		
		// Total off fault
		IncrementalMagFreqDist tOffIncr = fss.getImpliedOffFaultStatewideMFD();
		tOffIncr.setName("Total off-fault MFD from inversion");
		EvenlyDiscretizedFunc tOffCum = tOffIncr.getCumRateDist();
		funcs.add(incremental ? tOffIncr : tOffCum);
		
		// Total on + off fault
		IncrementalMagFreqDist tOnOffIncr = tOnIncr.deepClone();
		addDistro(tOnOffIncr, tOffIncr);
		tOnOffIncr.setName("Total on + off");
		EvenlyDiscretizedFunc tOnOffCum = tOnOffIncr.getCumRateDist();
		funcs.add(incremental ? tOnOffIncr : tOnOffCum);
		
		// Total sub seismogenic (section sum)
		IncrementalMagFreqDist tssSectIncr = gridGen.getSectSubSeisMFD();
		EvenlyDiscretizedFunc tssSectCum = tssSectIncr.getCumRateDist();
		funcs.add(incremental ? tssSectIncr : tssSectCum);
		
		// Total sub seismogenic (node sum)
//		IncrementalMagFreqDist tssNodeIncr = gridGen.getNodeSubSeisMFD();
//		EvenlyDiscretizedFunc tssNodeCum = tssNodeIncr.getCumRateDist();
//		funcs.add(incremental ? tssNodeIncr : tssNodeCum);
		
		// the two above should be equal
		
		// Total true off-fault (unassociated)
		IncrementalMagFreqDist trueOffIncr = gridGen.getNodeUnassociatedMFD();
		EvenlyDiscretizedFunc trueOffCum = trueOffIncr.getCumRateDist();
		funcs.add(incremental ? trueOffIncr : trueOffCum);
		
		// unassoc sum + section sum
		IncrementalMagFreqDist tssIncr = tssSectIncr.deepClone();
		addDistro(tssIncr, trueOffIncr);
		tssIncr.setName("Summed sub-seismogenic MFDs (sect + unnassociated)");
		EvenlyDiscretizedFunc tssCum = tssIncr.getCumRateDist();
		funcs.add(incremental ? tssIncr : tssCum);

		
//		// test scaling total off fault by total MoReduc
//		if (magScaling.equals(SmallMagScaling.MO_REDUCTION)) {
//			IncrementalMagFreqDist testScale = tofIncr.deepClone();
//			testScale.setName("MFD scaled to total mo reduction");
//			testScale.zeroAtAndAboveMag(6.6);
//			double reduction = fss.getTotalSubseismogenicMomentRateReduction();
//			testScale.scaleToTotalMomentRate(reduction);
//			funcs.add(testScale);
//		}
		
		
		GraphiWindowAPI_Impl plotter = new GraphiWindowAPI_Impl(funcs,
			(fName.contains("_gr_") ? "GR" : "CH") +
				" : " +
				CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL,
					magScaling.toString()) + " : " +
				(incremental ? "Incremental" : "Cumulative"), plotChars);
		plotter.setX_AxisRange(tssSectIncr.getMinX(), tssSectIncr.getMaxX());
		plotter.setY_AxisRange(1e-6, 1e1);
		plotter.setYLog(true);
		
	}
	
	private static void addDistro(EvenlyDiscretizedFunc f1, EvenlyDiscretizedFunc f2) {
		for (int i=0; i<f1.getNum(); i++) {
			f1.set(i, f1.getY(i) + f2.getY(i));
		}
	}
	
	void plotGR() {

		UCERF3_GridSourceGenerator gridGen = new UCERF3_GridSourceGenerator(
			fss, null, SpatialSeisPDF.UCERF3, 8.54, magScaling);
		System.out.println("init done");
		
		ArrayList<EvenlyDiscretizedFunc> funcs = Lists.newArrayList();
		
		// Total on-fault
		IncrementalMagFreqDist tOnIncr = fss.calcTotalNucleationMFD(5.05, 8.45, 0.1);
		tOnIncr.setName("Total on-fault MFD from inversion");
		EvenlyDiscretizedFunc tOnCum = tOnIncr.getCumRateDist();
		funcs.add(incremental ? tOnIncr : tOnCum);
		
		// Total off fault
		IncrementalMagFreqDist tOffIncr = fss.getImpliedOffFaultStatewideMFD();
		tOffIncr.setName("Total off-fault MFD from inversion");
		EvenlyDiscretizedFunc tOffCum = tOffIncr.getCumRateDist();
		funcs.add(incremental ? tOffIncr : tOffCum);
		
//		// Total on + off fault
//		IncrementalMagFreqDist tOnOffIncr = tOnIncr.deepClone();
//		addDistro(tOnOffIncr, tOffIncr);
//		tOnOffIncr.setName("Total on + off");
//		EvenlyDiscretizedFunc tOnOffCum = tOnOffIncr.getCumRateDist();
//		funcs.add(incremental ? tOnOffIncr : tOnOffCum);
		
		// Total sub seismogenic (section sum)
		IncrementalMagFreqDist tssSectIncr = gridGen.getSectSubSeisMFD();
		EvenlyDiscretizedFunc tssSectCum = tssSectIncr.getCumRateDist();
		funcs.add(incremental ? tssSectIncr : tssSectCum);
		
		// Total sub seismogenic (node sum)
//		IncrementalMagFreqDist tssNodeIncr = gridGen.getNodeSubSeisMFD();
//		EvenlyDiscretizedFunc tssNodeCum = tssNodeIncr.getCumRateDist();
//		funcs.add(incremental ? tssNodeIncr : tssNodeCum);
		
		// the two above should be equal
		
		// Total true off-fault (unassociated)
		IncrementalMagFreqDist trueOffIncr = gridGen.getNodeUnassociatedMFD();
		EvenlyDiscretizedFunc trueOffCum = trueOffIncr.getCumRateDist();
		funcs.add(incremental ? trueOffIncr : trueOffCum);
		
		// unassoc sum + section sum
		IncrementalMagFreqDist unSectIncr = tssSectIncr.deepClone();
		addDistro(unSectIncr, trueOffIncr);
		unSectIncr.setName("Summed sub-seismogenic MFDs (sect + unnassociated)");
		EvenlyDiscretizedFunc unSectCum = unSectIncr.getCumRateDist();
		funcs.add(incremental ? unSectIncr : unSectCum);

		// subSeis + on
		IncrementalMagFreqDist tssSectOnIncr = tssSectIncr.deepClone();
		addDistro(tssSectOnIncr, tOnIncr);
		tssSectOnIncr.setName("Total subSeis + on-fault");
		EvenlyDiscretizedFunc tssSectOnCum = tssSectOnIncr.getCumRateDist();
		funcs.add(incremental ? tssSectOnIncr : tssSectOnCum);

		
//		// test scaling total off fault by total MoReduc
//		if (magScaling.equals(SmallMagScaling.MO_REDUCTION)) {
//			IncrementalMagFreqDist testScale = tofIncr.deepClone();
//			testScale.setName("MFD scaled to total mo reduction");
//			testScale.zeroAtAndAboveMag(6.6);
//			double reduction = fss.getTotalSubseismogenicMomentRateReduction();
//			testScale.scaleToTotalMomentRate(reduction);
//			funcs.add(testScale);
//		}
		
		
		GraphiWindowAPI_Impl plotter = new GraphiWindowAPI_Impl(funcs,
			(fName.contains("_gr_") ? "GR" : "CH") +
				" : " +
				CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL,
					magScaling.toString()) + " : " +
				(incremental ? "Incremental" : "Cumulative"), plotChars);
		plotter.setX_AxisRange(tssSectIncr.getMinX(), tssSectIncr.getMaxX());
		plotter.setY_AxisRange(1e-6, 1e1);
		plotter.setYLog(true);
		
	}
	
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new GridSrcMFDPlotter();
			}
		});
	}
	
	
	GridSrcMFDPlotter(double duh) {

		UCERF3_GridSourceGenerator gridGen = new UCERF3_GridSourceGenerator(
			fss, null, SpatialSeisPDF.UCERF3, 8.54, magScaling);
		System.out.println("init done");
		
		Map<String, FaultSectionPrefData> sectionMap = Maps.newLinkedHashMap();
		for (FaultSectionPrefData fault : fss.getFaultSectionDataList()) {
			sectionMap.put(fault.getName(), fault);
		}
		
//		int idx = 300;
//		System.out.println(fss.getFaultSectionData(idx).getSectionName());
		
//		fss.get
		ArrayList<EvenlyDiscretizedFunc> funcs = Lists.newArrayList();
		
		
		// Total on fault (seismogenic)
		IncrementalMagFreqDist tofIncr = fss.getImpliedOffFaultStatewideMFD();
		tofIncr.setName("Total off-fault MFD from inversion");
		EvenlyDiscretizedFunc tofCum = tofIncr.getCumRateDist();
		funcs.add(incremental ? tofIncr : tofCum);
		
		// Total sub seismogenic (section sum)
//		IncrementalMagFreqDist tssSect = gridGen.getSectSubSeisMFD();
//		funcs.add(tssSect);
		
		// Total sub seismogenic (node sum)
		IncrementalMagFreqDist tssNodeIncr = gridGen.getNodeSubSeisMFD();
		EvenlyDiscretizedFunc tssNodeCum = tssNodeIncr.getCumRateDist();
		funcs.add(incremental ? tssNodeIncr : tssNodeCum);
		
		// the two above should be equal
		
		// Total true off-fault (unassociated)
		IncrementalMagFreqDist trueOffIncr = gridGen.getNodeUnassociatedMFD();
		EvenlyDiscretizedFunc trueOffCum = trueOffIncr.getCumRateDist();
		funcs.add(incremental ? trueOffIncr : trueOffCum);
		
		// node sum + section sum
		IncrementalMagFreqDist tssIncr = tssNodeIncr.deepClone();
		addDistro(tssIncr, trueOffIncr);
		tssIncr.setName("Summed sub-seismogenic MFDs (node + unnassociated)");
		EvenlyDiscretizedFunc tssCum = tssIncr.getCumRateDist();
		funcs.add(incremental ? tssIncr : tssCum);

		
//		// test scaling total off fault by total MoReduc
//		if (magScaling.equals(SmallMagScaling.MO_REDUCTION)) {
//			IncrementalMagFreqDist testScale = tofIncr.deepClone();
//			testScale.setName("MFD scaled to total mo reduction");
//			testScale.zeroAtAndAboveMag(6.6);
//			double reduction = fss.getTotalSubseismogenicMomentRateReduction();
//			testScale.scaleToTotalMomentRate(reduction);
//			funcs.add(testScale);
//		}
		
		
		GraphiWindowAPI_Impl plotter = new GraphiWindowAPI_Impl(funcs,
			(fName.contains("_gr_") ? "GR" : "CH") +
				" : " +
				CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL,
					magScaling.toString()) + " : " +
				(incremental ? "Incremental" : "Cumulative"), plotChars);
		plotter.setX_AxisRange(tssNodeIncr.getMinX(), tssNodeIncr.getMaxX());
		plotter.setY_AxisRange(1e-6, 1e1);
		plotter.setYLog(true);
		
		
		
		
	}


	
}
