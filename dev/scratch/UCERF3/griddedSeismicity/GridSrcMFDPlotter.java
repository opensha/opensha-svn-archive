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
	private static SmallMagScaling magScaling = SmallMagScaling.SPATIAL;
	private static boolean incremental = false;
	private static String fName = "tmp/invSols/reference_ch_sol.zip";
	
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
	
	
	Map<String, FaultSectionPrefData> sectionMap;
	
	GridSrcMFDPlotter() {

		UCERF3_GridSourceGenerator gridGen = new UCERF3_GridSourceGenerator(
			fss, null, SpatialSeisPDF.AVG_DEF_MODEL, 8.54, magScaling);
		System.out.println("init done");
		
		sectionMap = Maps.newLinkedHashMap();
		for (FaultSectionPrefData fault : fss.getFaultSectionDataList()) {
			sectionMap.put(fault.getName(), fault);
		}
		
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
	
	private static void addDistro(EvenlyDiscretizedFunc f1, EvenlyDiscretizedFunc f2) {
		for (int i=0; i<f1.getNum(); i++) {
			f1.set(i, f1.getX(i) + f2.getX(i));
		}
	}
	
//	public static void main(String[] args) {
//		
//		double totFltSolMR = fss.getTotalFaultSolutionMomentRate();
//		System.out.println("        totFltSolMR: " + totFltSolMR);
//		
//		double totOffFltMR = fss.getTotalOffFaultSeisMomentRate();
//		System.out.println("        totOffFltMR: " + totOffFltMR);
//
//		double totSubSeisReduction = fss.getTotalSubseismogenicMomentRateReduction();
//		System.out.println("totSubSeisReduction: " + totSubSeisReduction);
//		
//		double totSubSeisReducedMR = fss.getTotalSubseismogenicReducedMomentRate();
//		System.out.println("totSubSeisReducedMR: " + totSubSeisReducedMR);
//		
//		double totOriginalMR = fss.getTotalOrigMomentRate();
//		System.out.println("              totMR: " + totOriginalMR);
//		
//	}
	
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new GridSrcMFDPlotter();
			}
		});
	}

	
}
