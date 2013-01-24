package scratch.UCERF3;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.opensha.commons.util.ExceptionUtils;

import scratch.UCERF3.CompoundFaultSystemSolution.ZipFileSolutionFetcher;
import scratch.UCERF3.enumTreeBranches.DeformationModels;
import scratch.UCERF3.enumTreeBranches.FaultModels;
import scratch.UCERF3.inversion.InversionFaultSystemRupSetFactory;
import scratch.UCERF3.logicTree.APrioriBranchWeightProvider;
import scratch.UCERF3.logicTree.BranchWeightProvider;
import scratch.UCERF3.logicTree.LogicTreeBranch;
import scratch.UCERF3.logicTree.LogicTreeBranchNode;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.primitives.Doubles;

public class BranchAveragedFSSBuilder {
	
	public static SimpleFaultSystemSolution build(FaultSystemSolutionFetcher fetch,
			BranchWeightProvider weightProvider, FaultModels fm, List<String> branchNames) {
		
		// rupIndex, solIndex
		List<List<Double>> ratesList = Lists.newArrayList();
		List<List<Double>> magsList = Lists.newArrayList();
		
		List<Double> weightsList = Lists.newArrayList();
		
		branchLoop:
		for (LogicTreeBranch branch : fetch.getBranches()) {
			if (branch.getValue(FaultModels.class) != fm)
				continue;
			
			if (branchNames != null) {
				for (String branchName : branchNames) {
					boolean found = false;
					for (LogicTreeBranchNode<?> node : branch) {
						if (node.name().equals(branchName)) {
							found = true;
							break;
						}
					}
					if (!found)
						continue branchLoop;
				}
			}
			
			double weight = weightProvider.getWeight(branch);
			if (weight == 0)
				continue;
			weightsList.add(weight);
			if (weightsList.size() % 10 == 0) {
				System.out.println("Loading solution "+weightsList.size());
				System.gc();
			}
			double[] rates = fetch.getRates(branch);
			double[] mags = fetch.getMags(branch);
			
			if (ratesList.isEmpty()) {
				for (int i=0; i<rates.length; i++) {
					ratesList.add(new ArrayList<Double>());
					magsList.add(new ArrayList<Double>());
				}
			} else {
				Preconditions.checkState(rates.length == ratesList.size(),
						"Rupture count discrepancy between branches!");
			}
			for (int i=0; i<rates.length; i++) {
				ratesList.get(i).add(rates[i]);
				magsList.get(i).add(mags[i]);
			}
		}
		
		System.out.println("Creating Branch Averaged FSS for "+weightsList.size()+" solutions!");
		
		double[] rates = new double[ratesList.size()];
		double[] mags = new double[ratesList.size()];
		double[] weights = Doubles.toArray(weightsList);
		
		for (int r=0; r<rates.length; r++) {
			double[] rateVals = Doubles.toArray(ratesList.get(r));
			double[] magVals = Doubles.toArray(magsList.get(r));
			
			rates[r] = FaultSystemSolutionFetcher.calcScaledAverage(rateVals, weights);
			mags[r] = FaultSystemSolutionFetcher.calcScaledAverage(magVals, weights);
		}
		
		FaultSystemRupSet reference = InversionFaultSystemRupSetFactory.forBranch(
				fm, DeformationModels.GEOLOGIC);
		
		Preconditions.checkState(reference.getNumRuptures() == rates.length,
				"Rupture count for GEOLOGIC reference different than from FSS fetcher! ("+reference.getNumRuptures()+" != "+rates.length);
		
		String info = reference.getInfoString();
		
		info = "****** BRANCH AVERAGED SOLUTION! ONLY MAGS/RATES VALID! ******\n\n"+info;
		
		List<List<Integer>> clusterRups = Lists.newArrayList();
		List<List<Integer>> clusterSects = Lists.newArrayList();
		for (int i=0; i<reference.getNumClusters(); i++) {
			clusterRups.add(reference.getRupturesForCluster(i));
			clusterSects.add(reference.getSectionsForCluster(i));
		}
		
		// first build the rup set
		SimpleFaultSystemRupSet rupSet = new SimpleFaultSystemRupSet(
				reference.getFaultSectionDataList(), mags, reference.getAveSlipForAllRups(),
				reference.getSlipOnSectionsForAllRups(), reference.getSlipAlongRuptureModel(),
				reference.getSlipRateForAllSections(), reference.getSlipRateStdDevForAllSections(),
				reference.getAveRakeForAllRups(), reference.getAreaForAllRups(),
				reference.getAreaForAllSections(), reference.getSectionIndicesForAllRups(),
				info, reference.getCloseSectionsListList(), fm, reference.getDeformationModel(),
				clusterRups, clusterSects);
		
		SimpleFaultSystemSolution sol = new SimpleFaultSystemSolution(rupSet, rates);
		
		return sol;
	}

	/**
	 * @param args
	 * @throws IOException 
	 * @throws ZipException 
	 */
	public static void main(String[] args) throws ZipException, IOException {
		File file, outputFile;
		List<String> branchNames = null;
		if (args.length >= 2) {
			file = new File(args[0]);
			outputFile = new File(args[1]);
			if (args.length > 2) {
				branchNames = Lists.newArrayList();
				for (int i=2; i<args.length; i++)
					branchNames.add(args[i]);
			}
		} else {
			file = new File("/tmp/2012_10_10-fm3-logic-tree-sample_COMPOUND_SOL.zip");
			outputFile = new File("/tmp/2012_10_10-fm3-logic-tree-sample_branch_avg_sol.zip");
		}
		System.out.println("Loading: "+file.getAbsolutePath());
		System.out.println("Will save to: "+outputFile.getAbsolutePath());
//		FaultSystemSolutionFetcher fetcher = CompoundFaultSystemSolution.fromZipFile(file);
		FaultSystemSolutionFetcher fetcher = new ZipFileSolutionFetcher(new ZipFile(file));
		BranchWeightProvider weightProvider = new APrioriBranchWeightProvider();
		FaultModels fm = null;
		if (branchNames != null) {
			for (String branchName : branchNames) {
				for (FaultModels testFM : FaultModels.values()) {
					if (testFM.name().equals(branchName) || testFM.getShortName().equals(branchName))
						fm = testFM;
				}
			}
		}
		if (fm == null)
			fm = FaultModels.FM3_1;
		
		SimpleFaultSystemSolution sol = build(fetcher, weightProvider, fm, branchNames);
		
		sol.toZipFile(outputFile);
	}

}
