package scratch.UCERF3;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipException;

import scratch.UCERF3.enumTreeBranches.DeformationModels;
import scratch.UCERF3.enumTreeBranches.FaultModels;
import scratch.UCERF3.inversion.InversionFaultSystemRupSetFactory;
import scratch.UCERF3.logicTree.APrioriBranchWeightProvider;
import scratch.UCERF3.logicTree.BranchWeightProvider;
import scratch.UCERF3.logicTree.LogicTreeBranch;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.primitives.Doubles;

public class BranchAveragedFSSBuilder {
	
	public static SimpleFaultSystemSolution build(FaultSystemSolutionFetcher fetch,
			BranchWeightProvider weightProvider, FaultModels fm) {
		
		// rupIndex, solIndex
		List<List<Double>> ratesList = Lists.newArrayList();
		List<List<Double>> magsList = Lists.newArrayList();
		
		List<Double> weightsList = Lists.newArrayList();
		
		for (LogicTreeBranch branch : fetch.getBranches()) {
			if (branch.getValue(FaultModels.class) != fm)
				continue;
			double weight = weightProvider.getWeight(branch);
			if (weight == 0)
				continue;
			weightsList.add(weight);
			if (weightsList.size() % 10 == 0) {
				System.out.println("Loading solution "+weightsList.size());
			}
			FaultSystemSolution sol = fetch.getSolution(branch);
			double[] rates = sol.getRateForAllRups();
			double[] mags = sol.getMagForAllRups();
			
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
				"Rupture count for GEOLOGIC reference different than from FSS fetcher!");
		
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
		File file = new File("/tmp/2012_10_10-fm3-logic-tree-sample_COMPOUND_SOL.zip");
		File outputFile = new File("/tmp/2012_10_10-fm3-logic-tree-sample_branch_avg_sol.zip");
		FaultSystemSolutionFetcher fetcher = CompoundFaultSystemSolution.fromZipFile(file);
		BranchWeightProvider weightProvider = new APrioriBranchWeightProvider();
		FaultModels fm = FaultModels.FM3_1;
		
		SimpleFaultSystemSolution sol = build(fetcher, weightProvider, fm);
		
		sol.toZipFile(outputFile);
	}

}
