package scratch.UCERF3.inversion;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.dom4j.DocumentException;
import org.opensha.commons.data.region.CaliforniaRegions;
import org.opensha.commons.exceptions.GMT_MapException;
import org.opensha.commons.geo.Region;
import org.opensha.commons.util.ClassUtils;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

import scratch.UCERF3.AverageFaultSystemSolution;
import scratch.UCERF3.FaultSystemRupSet;
import scratch.UCERF3.FaultSystemSolution;
import scratch.UCERF3.SimpleFaultSystemRupSet;
import scratch.UCERF3.SimpleFaultSystemSolution;
import scratch.UCERF3.analysis.FaultBasedMapGen;
import scratch.UCERF3.enumTreeBranches.DeformationModels;
import scratch.UCERF3.enumTreeBranches.FaultModels;

public class BatchPlotGen {
	
	private static ArrayList<double[]> partic_mag_ranges = new ArrayList<double[]>();
	static {
		partic_mag_ranges.add(toArray(6d, 7d));
		partic_mag_ranges.add(toArray(7d, 8d));
		partic_mag_ranges.add(toArray(8d, 10d));
	}
	
	private static double[] toArray(double... vals) {
		return vals;
	}
	
	public static void makeMapPlots(FaultSystemSolution sol, File dir, String prefix)
			throws GMT_MapException, RuntimeException, IOException, DocumentException {
		Region region;
		if (sol.getDeformationModel() == DeformationModels.UCERF2_NCAL
				|| sol.getDeformationModel() == DeformationModels.UCERF2_BAYAREA)
			region = new CaliforniaRegions.RELM_NOCAL();
		else
			region = new CaliforniaRegions.RELM_TESTING();

		FaultBasedMapGen.plotOrigNonReducedSlipRates(sol, region, dir, prefix, false);
		FaultBasedMapGen.plotOrigCreepReducedSlipRates(sol, region, dir, prefix, false);
		FaultBasedMapGen.plotTargetSlipRates(sol, region, dir, prefix, false);
		FaultBasedMapGen.plotSolutionSlipRates(sol, region, dir, prefix, false);
		FaultBasedMapGen.plotSolutionSlipMisfit(sol, region, dir, prefix, false, true);
		FaultBasedMapGen.plotSolutionSlipMisfit(sol, region, dir, prefix, false, false);
		FaultSystemSolution ucerf2 = getUCERF2Comparision(sol.getFaultModel(), dir);
		for (double[] range : partic_mag_ranges) {
			FaultBasedMapGen.plotParticipationRates(sol, region, dir, prefix, false, range[0], range[1]);
			FaultBasedMapGen.plotParticipationRatios(sol, ucerf2, region, dir, prefix, false, range[0], range[1], true);
		}
		FaultBasedMapGen.plotSectionPairRates(sol, region, dir, prefix, false);
		FaultBasedMapGen.plotSegmentation(sol, region, dir, prefix, false, 0, 10);
		FaultBasedMapGen.plotSegmentation(sol, region, dir, prefix, false, 7, 10);
	}
	
	private static HashMap<FaultModels, FaultSystemSolution> ucerf2SolutionCache = Maps.newHashMap();
	
	private static FaultSystemSolution getUCERF2Comparision(FaultModels fm, File dir) throws IOException, DocumentException {
		if (ucerf2SolutionCache.containsKey(fm))
			return ucerf2SolutionCache.get(fm);
		File cachedFile = new File(dir, fm.getShortName()+"_UCERF2_COMPARISON_SOL.zip");
		SimpleFaultSystemSolution sol;
		if (cachedFile.exists()) {
			System.out.println("Loading UCERF2 comparison from: "+cachedFile.getName());
			sol = SimpleFaultSystemSolution.fromFile(cachedFile);
		} else {
			sol = UCERF2_ComparisonSolutionFetcher.getUCERF2Solution(fm);
			try {
				sol.toZipFile(cachedFile);
			} catch (Exception e) {
				// don't fail on a cache attempt
				e.printStackTrace();
			}
		}
		ucerf2SolutionCache.put(fm, sol);
		return sol;
	}
	
	public static void handleDir(File dir) throws IOException, DocumentException, GMT_MapException {
		for (File file : dir.listFiles()) {
			if (file.isDirectory()) {
				handleDir(file);
				continue;
			}
			String fileName = file.getName();
			if (!fileName.endsWith("_sol.zip"))
				continue;
			
			String prefix = fileName.substring(0, fileName.indexOf("_sol.zip"));
			
			if (prefix.contains("_run")) {
				// make sure that every run is done
				int total = 0;
				int completed = 0;
				for (File testFile : dir.listFiles()) {
					String testName = testFile.getName();
					if (testName.startsWith(prefix) && testName.endsWith(".pbs")) {
						total++;
						File binFile = new File(dir, testName.substring(0, testName.indexOf(".pbs"))+".bin");
						if (binFile.exists())
							completed++;
					}
				}
				if (completed < total) {
					System.out.println("Not quite done with '"+prefix+"' ("+completed+"/"+total+")");
					continue;
				}
				// this is an average of many run
				FaultSystemRupSet rupSet = SimpleFaultSystemRupSet.fromFile(file);
				AverageFaultSystemSolution avgSol = AverageFaultSystemSolution.fromDirectory(rupSet, dir, prefix);
				prefix = prefix.substring(0, prefix.indexOf("_run"))+"_mean";
				File avgSolFile = new File(dir, prefix+"_sol.zip");
				avgSol.toZipFile(avgSolFile);
				handleSolutionFile(avgSolFile, prefix, avgSol);
			} else {
				handleSolutionFile(file, prefix, null);
			}
		}
	}
	
	private static void handleSolutionFile(File file, String prefix, FaultSystemSolution sol) throws GMT_MapException, RuntimeException, IOException, DocumentException {
		File dir = file.getParentFile();
		
		File testMapDoneFile = new File(dir, prefix+"_sect_pairs.png");
		boolean hasMapPlots = testMapDoneFile.exists();
		File testMFDDoneFile = new File(dir, prefix+"_MFD_RELM_SOCAL_Region.png");
		boolean hasMFDPlots = testMFDDoneFile.exists();
//		boolean hasMFDPlots = 
		if (hasMapPlots && hasMFDPlots) {
			// we've already done this one, skip!
			System.out.println("Skipping (already done): "+prefix);
			return;
		}
		System.out.println("Processing: "+prefix);
		
		if (sol == null)
			sol = SimpleFaultSystemSolution.fromFile(file);
		
		if (!hasMapPlots) {
			makeMapPlots(sol, dir, prefix);
		}
		if (!hasMFDPlots) {
			InversionFaultSystemSolution invSol = new InversionFaultSystemSolution(sol);
			CommandLineInversionRunner.writeMFDPlots(invSol, dir, prefix);
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length != 1) {
			System.out.println("USAGE: "+ClassUtils.getClassNameWithoutPackage(BatchPlotGen.class)+" <directory>");
			System.exit(2);
		}
		
		try {
			File dir = new File(args[0]);
			Preconditions.checkArgument(dir.exists(), dir.getAbsolutePath()+" doesn't exist!");
			
			handleDir(dir);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		System.exit(0);
	}

}
