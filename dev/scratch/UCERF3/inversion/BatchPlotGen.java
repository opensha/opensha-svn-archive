package scratch.UCERF3.inversion;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.dom4j.DocumentException;
import org.opensha.commons.data.CSVFile;
import org.opensha.commons.data.region.CaliforniaRegions;
import org.opensha.commons.exceptions.GMT_MapException;
import org.opensha.commons.geo.Region;
import org.opensha.commons.util.ClassUtils;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import scratch.UCERF3.AverageFaultSystemSolution;
import scratch.UCERF3.FaultSystemRupSet;
import scratch.UCERF3.FaultSystemSolution;
import scratch.UCERF3.SimpleFaultSystemRupSet;
import scratch.UCERF3.SimpleFaultSystemSolution;
import scratch.UCERF3.analysis.FaultBasedMapGen;
import scratch.UCERF3.enumTreeBranches.DeformationModels;
import scratch.UCERF3.enumTreeBranches.FaultModels;
import scratch.UCERF3.logicTree.LogicTreeBranch;
import scratch.UCERF3.logicTree.LogicTreeBranchNode;
import scratch.UCERF3.logicTree.VariableLogicTreeBranch;
import scratch.UCERF3.utils.DeformationModelFetcher;
import scratch.UCERF3.utils.IDPairing;
import scratch.UCERF3.utils.MatrixIO;
import scratch.UCERF3.utils.RELM_RegionUtils;
import scratch.UCERF3.utils.UCERF3_DataUtils;
import scratch.UCERF3.utils.aveSlip.AveSlipConstraint;
import scratch.UCERF3.utils.paleoRateConstraints.PaleoRateConstraint;
import scratch.UCERF3.utils.paleoRateConstraints.UCERF3_PaleoProbabilityModel;

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
		FaultBasedMapGen.plotSegmentation(sol, region, dir, prefix, false, 7.5, 10);
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
		Map<VariableLogicTreeBranch, Map<String, Double>> misfitsMap = Maps.newHashMap();
		
		handleDir(dir, misfitsMap, 1);
		
		if (!misfitsMap.isEmpty()) {
			List<String> misfitNames = Lists.newArrayList();
			for (Map<String, Double> misfits : misfitsMap.values())
				for (String name : misfits.keySet())
					if (!misfitNames.contains(name))
						misfitNames.add(name);
			Collections.sort(misfitNames);
			
			File misfitsCSV = new File(dir, dir.getName()+"_misfits.csv");
			
			int numLogicTreeElems = -1;
			for (VariableLogicTreeBranch branch : misfitsMap.keySet()) {
				int num = branch.size() + branch.getVariations().size();
				if (numLogicTreeElems < 0)
					numLogicTreeElems = num;
				else
					Preconditions.checkState(numLogicTreeElems == num, "Logic Tree Branch Lengths Inconsistent!");
			}
			
			Map<String, Integer> misfitCols = Maps.newHashMap();
			CSVFile<String> csv = new CSVFile<String>(true);
			List<String> header = Lists.newArrayList();

			VariableLogicTreeBranch branch1 = misfitsMap.keySet().iterator().next();

			for (LogicTreeBranchNode<?> node : branch1)
				header.add(ClassUtils.getClassNameWithoutPackage(node.getClass()));

			for (int i=0; i<branch1.getVariations().size(); i++)
				header.add("Variation "+(i+1));

			for (String misfitName : misfitNames) {
				int col = header.size();
				header.add(misfitName);
				misfitCols.put(misfitName, col);
			}
			
			csv.addLine(header);
			
			int numCols = csv.getNumCols();
			
			for (VariableLogicTreeBranch branch : misfitsMap.keySet()) {
				Map<String, Double> misfits = misfitsMap.get(branch);
				
				List<String> line = Lists.newArrayList();
				
				for (LogicTreeBranchNode<?> node : branch)
					line.add(node.getShortName());
				
				for (int i=0; i<branch.getVariations().size(); i++)
					line.add(branch.getVariations().get(i));
				
				while (line.size() < numCols)
					line.add("");
				
				for (String misfitName : misfits.keySet())
					line.set(misfitCols.get(misfitName), misfits.get(misfitName)+"");
				
				csv.addLine(line);
			}
			
			csv.writeToFile(misfitsCSV);
		}
	}
	
	public static void handleDir(File dir, Map<VariableLogicTreeBranch, Map<String, Double>> misfitsMap, int maxDepth)
			throws IOException, DocumentException, GMT_MapException {
		for (File file : dir.listFiles()) {
			if (file.isDirectory()) {
				if (maxDepth > 0)
					handleDir(file, misfitsMap, maxDepth - 1);
				continue;
			}
			String fileName = file.getName();
			if (!fileName.endsWith("_sol.zip"))
				continue;
			
			String prefix = fileName.substring(0, fileName.indexOf("_sol.zip"));
			
			handleSolutionFile(file, prefix, null, misfitsMap);
			
			if (prefix.contains("_run")) {
				// make sure that every run is done
				prefix = prefix.substring(0, prefix.indexOf("_run"));
				// if we're in a subdirectory, skip out to main
				File myDir;
				if (file.getParentFile().getName().startsWith(prefix))
					myDir = dir.getParentFile();
				else
					myDir = dir;
				int total = 0;
				int completed = 0;
				for (File testFile : myDir.listFiles()) {
					String testName = testFile.getName();
					if (testName.startsWith(prefix) && testName.endsWith(".pbs")) {
						total++;
						testName = testName.substring(0, testName.indexOf(".pbs"));
						File binFile = new File(myDir, testName+".bin");
						if (binFile.exists() || new File(new File(myDir, testName), testName+".bin").exists())
							completed++;
					}
				}
				if (completed < total) {
					System.out.println("Not quite done with '"+prefix+"' ("+completed+"/"+total+")");
					continue;
				}
				String meanPrefix = prefix + "_mean";
				File meanSolDir = new File(myDir, meanPrefix);
				if (!meanSolDir.exists())
					meanSolDir.mkdir();
				File avgSolFile = new File(meanSolDir, meanPrefix+"_sol.zip");
				if (avgSolFile.exists() && doAvgPlotsExist(meanSolDir, meanPrefix)) {
					System.out.println("Skipping (mean sol already done): "+meanPrefix);
					continue;
				}
				// this is an average of many runs
				FaultSystemRupSet rupSet = SimpleFaultSystemRupSet.fromFile(file);
				AverageFaultSystemSolution avgSol = AverageFaultSystemSolution.fromDirectory(rupSet, myDir, prefix);
				if (!doAvgPlotsExist(meanSolDir, meanPrefix))
					try {
						writeAvgSolPlots(avgSol, meanSolDir, meanPrefix);
					} catch (Exception e) {
						e.printStackTrace();
					}
				avgSol.toZipFile(avgSolFile);
				// write bin file as well
				MatrixIO.doubleArrayToFile(avgSol.getRateForAllRups(), new File(meanSolDir, meanPrefix+".bin"));
				handleSolutionFile(avgSolFile, meanPrefix, avgSol, null);
			}
		}
	}
	
	private static void handleSolutionFile(File file, String prefix, FaultSystemSolution sol,
			Map<VariableLogicTreeBranch, Map<String, Double>> misfitsMap)
			throws GMT_MapException, RuntimeException, IOException, DocumentException {
		File dir = file.getParentFile();
		
		System.out.println("Handling solution file: "+file.getAbsolutePath());
		
		InversionFaultSystemSolution invSol = null;
		if (misfitsMap != null) {
			VariableLogicTreeBranch branch = null;
			try {
				System.out.println("Prefix: "+prefix);
				branch = VariableLogicTreeBranch.fromName(prefix);
				if (!branch.getVariations().isEmpty())
					System.out.println("Variations: "+Joiner.on(",").join(branch.getVariations()));
			} catch (Exception e) {
				System.err.println("WARNING: Couldn't parse prefix into branch: "+prefix);
				e.printStackTrace();
			}
			if (branch != null) {
				File misfitsFile = new File(file.getAbsolutePath()+".misfits");
				if (misfitsFile.exists()) {
					Map<String, Double> misfits = Maps.newHashMap();
					for (String line : FileUtils.readLines(misfitsFile)) {
						line = line.trim();
						if (line.isEmpty())
							continue;
						int ind = line.indexOf(":");
						String name = line.substring(0, ind);
						Double val = Double.parseDouble(line.substring(ind+1).trim());
						misfits.put(name, val);
					}
					misfitsMap.put(branch, misfits);
				} else {
					try {
						if (sol == null)
							sol = SimpleFaultSystemSolution.fromFile(file);
						invSol = new InversionFaultSystemSolution(sol);
						Map<String, Double> misfits = invSol.getMisfits();
						FileWriter fw = new FileWriter(misfitsFile);
						for (String misfit : misfits.keySet()) {
							double val = misfits.get(misfit);
							fw.write(misfit+": "+val+"\n");
						}
						fw.close();
						misfitsMap.put(branch, misfits);
					} catch (Exception e) {
						System.err.println("WARNING: Couldn't load InversionFaultSystemSolution for: "+prefix);
						e.printStackTrace();
					}
				}
			}
		}
		
		File testMapDoneFile = new File(dir, prefix+"_sect_pairs.png");
		boolean hasMapPlots = testMapDoneFile.exists();
		boolean hasMFDPlots = CommandLineInversionRunner.doMFDPlotsExist(dir, prefix);
		boolean hasJumpPlots = CommandLineInversionRunner.doJumpPlotsExist(dir, prefix);
		boolean hasPaleoPlots = CommandLineInversionRunner.doPaleoPlotsExist(dir, prefix);
		boolean hasSAFSegPlots = CommandLineInversionRunner.doSAFSegPlotsExist(dir, prefix);
		boolean hasPaleoCorrelationPlots = new File(dir, CommandLineInversionRunner.PALEO_CORRELATION_DIR_NAME).exists();
		boolean hasParentMFDPlots = new File(dir, CommandLineInversionRunner.PARENT_SECT_MFD_DIR_NAME).exists();
		boolean hasPaleoFaultBasedPlots = new File(dir, CommandLineInversionRunner.PALEO_FAULT_BASED_DIR_NAME).exists();
//		boolean hasMFDPlots = 
		if (hasMapPlots && hasMFDPlots && hasJumpPlots && hasJumpPlots && hasPaleoPlots
				&& hasSAFSegPlots && hasPaleoCorrelationPlots && hasParentMFDPlots && hasPaleoFaultBasedPlots) {
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
			try {
				if (invSol == null)
					invSol = new InversionFaultSystemSolution(sol);
				CommandLineInversionRunner.writeMFDPlots(invSol, dir, prefix);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (!hasJumpPlots) {
			try {
				Map<IDPairing, Double> distsMap = new DeformationModelFetcher(sol.getFaultModel(), sol.getDeformationModel(),
						UCERF3_DataUtils.DEFAULT_SCRATCH_DATA_DIR, 0.1).getSubSectionDistanceMap(
								LaughTestFilter.getDefault().getMaxJumpDist());
				CommandLineInversionRunner.writeJumpPlots(sol, distsMap, dir, prefix);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		ArrayList<PaleoRateConstraint> paleoRateConstraints = null;
		List<AveSlipConstraint> aveSlipConstraints = null;
		if (!hasPaleoPlots || !hasPaleoFaultBasedPlots) {
			paleoRateConstraints =
					CommandLineInversionRunner.getPaleoConstraints(sol.getFaultModel(), sol);
			aveSlipConstraints = AveSlipConstraint.load(sol.getFaultSectionDataList());
		}
		if (!hasPaleoPlots) {
			CommandLineInversionRunner.writePaleoPlots(paleoRateConstraints, aveSlipConstraints, sol, dir, prefix);
		}
		if (!hasSAFSegPlots) {
			try {
				CommandLineInversionRunner.writeSAFSegPlots(sol, dir, prefix);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (!hasPaleoCorrelationPlots) {
			try {
				CommandLineInversionRunner.writePaleoCorrelationPlots(sol,
						new File(dir, CommandLineInversionRunner.PALEO_CORRELATION_DIR_NAME), UCERF3_PaleoProbabilityModel.load());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (!hasParentMFDPlots) {
			try {
				CommandLineInversionRunner.writeParentSectionMFDPlots(sol,
						new File(dir, CommandLineInversionRunner.PARENT_SECT_MFD_DIR_NAME));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (!hasPaleoFaultBasedPlots) {
			try {
				CommandLineInversionRunner.writePaleoFaultPlots(paleoRateConstraints, aveSlipConstraints, sol,
						new File(dir, CommandLineInversionRunner.PALEO_FAULT_BASED_DIR_NAME));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private static boolean doAvgPlotsExist(File dir, String prefix) {
		return new File(dir, prefix+"_partic_rates_8.0+.png").exists();
	}
	
	public static void writeAvgSolPlots(AverageFaultSystemSolution avgSol, File dir, String prefix) throws GMT_MapException, RuntimeException, IOException, InterruptedException {
		CommandLineInversionRunner.writeParentSectionMFDPlots(avgSol, new File(dir, "parent_sect_mfds"));
		CommandLineInversionRunner.writePaleoCorrelationPlots(
				avgSol, new File(dir, "paleo_correlation"), UCERF3_PaleoProbabilityModel.load());
		Region region = RELM_RegionUtils.getGriddedRegionInstance();
		if (avgSol.getNumSolutions() <= 10)
			FaultBasedMapGen.plotSolutionSlipRateStdDevs(avgSol, avgSol.calcSlipRates(), region, dir, prefix, false);
		if (avgSol.getNumSolutions() <= 60) {
			FaultBasedMapGen.plotParticipationStdDevs(avgSol, avgSol.calcParticRates(6, 7), region, dir, prefix, false, 6, 7);
			FaultBasedMapGen.plotParticipationStdDevs(avgSol, avgSol.calcParticRates(7, 8), region, dir, prefix, false, 7, 8);
			FaultBasedMapGen.plotParticipationStdDevs(avgSol, avgSol.calcParticRates(8, 10), region, dir, prefix, false, 8, 10);
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
