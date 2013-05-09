package scratch.peter.tmp;

import static org.opensha.nshmp2.util.SourceRegion.CA;
import static org.opensha.nshmp2.util.SourceType.GRIDDED;

import java.io.File;
import java.io.IOException;

import org.opensha.commons.geo.GriddedRegion;
import org.opensha.nshmp.NEHRP_TestCity;
import org.opensha.nshmp2.tmp.TestGrid;
import org.opensha.sha.earthquake.param.IncludeBackgroundOption;

import scratch.UCERF3.AverageFaultSystemSolution;
import scratch.UCERF3.CompoundFaultSystemSolution;
import scratch.UCERF3.FaultSystemSolution;
import scratch.UCERF3.erf.UCERF3_FaultSysSol_ERF;
import scratch.UCERF3.logicTree.LogicTreeBranch;
import scratch.UCERF3.utils.FaultSystemIO;
import scratch.peter.ucerf3.calc.UC3_CalcUtils;

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

//		GriddedRegion gr = TestGrid.LITTLE_SALMON.grid(0.02);
//		System.out.println(gr.getNodeCount());
		

//		tmp();
		
		double pp = 6. * (4. / 3.);
		System.out.println(pp);
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
