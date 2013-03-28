package scratch.peter.tmp;

import static org.opensha.nshmp2.util.SourceRegion.CA;
import static org.opensha.nshmp2.util.SourceType.GRIDDED;

import java.io.File;
import java.io.IOException;

import org.opensha.nshmp.NEHRP_TestCity;

import scratch.UCERF3.AverageFaultSystemSolution;
import scratch.UCERF3.SimpleFaultSystemSolution;
import scratch.peter.ucerf3.calc.UC3_CalcUtils;

import com.google.common.io.Files;
import com.sun.org.apache.bcel.internal.classfile.SourceFile;

/**
 * Add comments here
 *
 * 
 * @author Peter Powers
 * @version $Id:$
 */
public class scratch {

	
	public static void main(String[] args) throws IOException {

		tmp();
		
//		String dir = "/Users/pmpowers/projects/OpenSHA/tmp/UC3maps/mapsUC32localRatio";
//		consolidateMaps(dir);
		
//		for (NEHRP_TestCity city : NEHRP_TestCity.values()) {
//			System.out.println(city);
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
		SimpleFaultSystemSolution fss = new SimpleFaultSystemSolution(avgFss.getSolution(83));
		File solZip = new File(sol83Path);
		fss.toZipFile(solZip);
	}
}
