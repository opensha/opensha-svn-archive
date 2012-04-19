package scratch.UCERF3.griddedSeismicity;

import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.SwingUtilities;

import org.opensha.commons.gui.plot.PlotLineType;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.opensha.sha.gui.infoTools.GraphiWindowAPI_Impl;
import org.opensha.sha.gui.infoTools.PlotCurveCharacterstics;
import org.opensha.sha.magdist.IncrementalMagFreqDist;
import org.opensha.sha.magdist.SummedMagFreqDist;

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
	
	static {
		
		// init fss
		try {
			File f = new File("tmp/invSols/reference_gr_sol.zip");
			SimpleFaultSystemSolution tmp = SimpleFaultSystemSolution.fromFile(f);
			fss = new InversionFaultSystemSolution(tmp);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// init plot characteristics
		plotChars = Lists.newArrayList(
			new PlotCurveCharacterstics(PlotLineType.SOLID,2f, Color.BLUE.darker()),
			new PlotCurveCharacterstics(PlotLineType.SOLID,2f, Color.GREEN.darker()),
			new PlotCurveCharacterstics(PlotLineType.SOLID,2f, Color.ORANGE),
			new PlotCurveCharacterstics(PlotLineType.SOLID,2f, Color.MAGENTA.darker()),
			new PlotCurveCharacterstics(PlotLineType.SOLID,2f, Color.BLACK),
			new PlotCurveCharacterstics(PlotLineType.SOLID,2f, Color.RED.brighter()),
			new PlotCurveCharacterstics(PlotLineType.SOLID,2f, Color.CYAN.darker()),
			new PlotCurveCharacterstics(PlotLineType.SOLID,2f, Color.YELLOW.darker()));

	}
	
	
	Map<String, FaultSectionPrefData> sectionMap;
	
	GridSrcMFDPlotter() {

		UCERF3_GridSourceGenerator gridGen = new UCERF3_GridSourceGenerator(
			fss, null, SpatialSeisPDF.AVG_DEF_MODEL, 8.54, SmallMagScaling.MO_REDUCTION);
		System.out.println("init done");
		
		sectionMap = Maps.newLinkedHashMap();
		for (FaultSectionPrefData fault : fss.getFaultSectionDataList()) {
			sectionMap.put(fault.getName(), fault);
		}
		
		ArrayList<IncrementalMagFreqDist> funcs = Lists.newArrayList();
		
		
		// Total on fault (seismogenic)
		IncrementalMagFreqDist tof = fss.getImpliedOffFaultStatewideMFD();
		tof.setName("Total off-fault MFD from inversion");
		funcs.add(tof);
		
		// Total sub seismogenic (section sum)
		IncrementalMagFreqDist tssSect = gridGen.getSectSubSeisMFD();
		funcs.add(tssSect);
		
		// Total sub seismogenic (node sum)
		IncrementalMagFreqDist tssNode = gridGen.getNodeSubSeisMFD();
		funcs.add(tssNode);
		
		// the two above should be equal
		
		// Total true off-fault (unassociated)
		IncrementalMagFreqDist trueOff = gridGen.getNodeUnassociatedMFD();
		funcs.add(trueOff);
		
		// node sum + section sum
		SummedMagFreqDist tss = new SummedMagFreqDist(tssSect.getMinX(), tssSect.getMaxX(), tssSect.getNum());
		tss.setName("Summed sub-seismogenic MFDs (node + unnasociated)");
		tss.addIncrementalMagFreqDist(trueOff);
		tss.addIncrementalMagFreqDist(tssSect);
		funcs.add(tss);

		
		// test scaling total off fault by total MoReduc
		IncrementalMagFreqDist testScale = tof.deepClone();
		testScale.setName("MFD scaled to total mo reduction");
		testScale.zeroAtAndAboveMag(6.6);
		double reduction = fss.getTotalSubseismogenicMomentRateReduction();
		testScale.scaleToTotalMomentRate(reduction);
		funcs.add(testScale);
		
		
		GraphiWindowAPI_Impl plotter = new GraphiWindowAPI_Impl(funcs,  "GR - Mo Reduce", plotChars);
		plotter.setX_AxisRange(tssSect.getMinX(), tssSect.getMaxX());
		plotter.setY_AxisRange(1e-7, 1e2);
		plotter.setYLog(true);
		
		
		
		
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
