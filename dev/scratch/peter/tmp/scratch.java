package scratch.peter.tmp;

import static org.opensha.nshmp2.util.SourceRegion.CA;
import static org.opensha.nshmp2.util.SourceType.GRIDDED;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.opensha.commons.data.function.ArbDiscrEmpiricalDistFunc;
import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.commons.data.function.DiscretizedFunc;
import org.opensha.commons.geo.GriddedRegion;
import org.opensha.commons.gui.plot.jfreechart.DiscretizedFunctionXYDataSet;
import org.opensha.commons.util.DataUtils;
import org.opensha.nshmp.NEHRP_TestCity;
import org.opensha.nshmp2.tmp.TestGrid;
import org.opensha.nshmp2.util.Period;
import org.opensha.nshmp2.util.Utils;
import org.opensha.sha.earthquake.param.IncludeBackgroundOption;
import org.opensha.sha.imr.attenRelImpl.NSHMP_2008_CA;

import scratch.UCERF3.AverageFaultSystemSolution;
import scratch.UCERF3.CompoundFaultSystemSolution;
import scratch.UCERF3.FaultSystemSolution;
import scratch.UCERF3.erf.UCERF3_FaultSysSol_ERF;
import scratch.UCERF3.logicTree.LogicTreeBranch;
import scratch.UCERF3.utils.FaultSystemIO;
import scratch.peter.curves.ProbOfExceed;
import scratch.peter.ucerf3.calc.UC3_CalcUtils;

import com.google.common.base.Preconditions;
import com.google.common.collect.Range;
import com.google.common.io.Files;

/**
 * Add comments here
 *
 * 
 * @author Peter Powers
 * @version $Id:$
 */
public class scratch {

	
	public static void main(String[] args) throws IOException {

//		System.out.println(TestGrid.CA_NSHMP.grid(0.05).getNodeCount());
		
		Range<Double> r = Range.open(1.0,23.0);
		System.out.println(r);
		
//		Period p = Period.GM1P00;
//		DiscretizedFunc f = p.getFunction();
//		f = Utils.getExceedProbabilities(f, -0.23573904887559857, 0.6874658901925597, false, 0.0);
//		f.scale(0.01);
//		System.out.println(f);
//		System.out.println(ProbOfExceed.get(f, ProbOfExceed.PE2IN50));
		
//		double[] seq = DataUtils.buildSequence(0, 10, 1.00000000000001, true);
//		System.out.println(Arrays.toString(seq));
//		double[] seq2 = DataUtils.buildSequence(0, 10, 0.99999999999998, true);
//		System.out.println(Arrays.toString(seq2));
		
//		double[] p1 = {1,2,3,4,5};
//		int idx = firstZeroValue(p1);
//		double[] p2 = Arrays.copyOf(p1,idx);
//		System.out.println(Arrays.toString(p2));
//		GriddedRegion gr = TestGrid.LITTLE_SALMON.grid(0.02);
//		System.out.println(gr.getNodeCount());
		

//		tmp();
		
//		double pp = 6. * (4. / 3.);
//		System.out.println(pp);
//		String dir = "/Users/pmpowers/projects/OpenSHA/tmp/UC3maps/mapsUC32b/UC32littleSalmon/multi-UC32/PE1IN100-sol9";
//		consolidateMaps(dir);
		
//		for (NEHRP_TestCity city : NEHRP_TestCity.values()) {
//			System.out.println(city);
//		}
		
//		UCERF3_FaultSysSol_ERF erf = UC3_CalcUtils.getUC3_ERF(
//			"/Users/pmpowers/projects/OpenSHA/tmp/invSols/conv/2013_02_01-ucerf3p2-convergence_bundle5_sol14.zip",
//			IncludeBackgroundOption.INCLUDE, false, true, 1.0);
//		erf.updateForecast();
		
		//		try {
//			String srcDir = "/Users/pmpowers/projects/OpenSHA/tmp/invSols/conv/";
//			String solPath = srcDir + "2013_02_01-ucerf3p2-convergence_bundle5_COMPOUND_SOL.zip";
//			CompoundFaultSystemSolution cfss = UC3_CalcUtils.getCompoundSolution(solPath);
//			for (LogicTreeBranch br : cfss.getBranches()) {
//				System.out.println(br.buildFileName());
//				
//			}
//			
//			int idx = 0;
//			for (FaultSystemSolution fss : cfss) {
//				System.out.println(fss.getClass());
//				String solName = "2013_02_01-ucerf3p2-convergence_bundle5_sol" + (idx++) + ".zip";
//				SimpleFaultSystemSolution sfss = new SimpleFaultSystemSolution(fss);
//				File solZip = new File(srcDir + solName);
//				sfss.toZipFile(solZip);
//			}
//			
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
	}
	
	private static int firstZeroValue(double[] data) {
		int idx = 0;
		for (double d : data) {
			if (d > 0.0) {
				idx++;
			} else {
				break;
			}
		}
		Preconditions.checkArgument(
			idx > 1, "Curve must have more than two non-zero y-values: " +
				Arrays.toString(data));
		return idx;
	}

	private static void consolidateMaps(String path) throws IOException {
		File srcDir = new File(path);
		File destDir = new File(path, "composite");
		destDir.mkdir();
		File[] mapDirs = srcDir.listFiles();
		for (File mapDir : mapDirs) {
			if (!mapDir.isDirectory() || mapDir.equals(destDir)) continue;
			String mapName = mapDir.getName();
			File srcMap = new File(mapDir, "map.pdf");
			File destMap = new File(destDir, mapName + ".pdf");
			Files.copy(srcMap, destMap);
		}
	}
	
	private static void tmp() throws IOException {
		String srcDir = "/Users/pmpowers/projects/OpenSHA/tmp/invSols/conv/";
		String avgFssPath = srcDir + "FM3_1_ZENG_Shaw09Mod_DsrTap_CharConst_M5Rate8.7_MMaxOff7.6_NoFix_SpatSeisU3_VarZeros_mean_sol.zip";
		String sol83Path = srcDir + "FM3_1_ZENG_Shaw09Mod_DsrTap_CharConst_M5Rate8.7_MMaxOff7.6_NoFix_SpatSeisU3_VarZeros_run83_sol.zip";
		AverageFaultSystemSolution avgFss = UC3_CalcUtils.getAvgSolution(avgFssPath);
		FaultSystemIO.writeSol(avgFss.getSolution(83), new File(sol83Path));
	}
}
