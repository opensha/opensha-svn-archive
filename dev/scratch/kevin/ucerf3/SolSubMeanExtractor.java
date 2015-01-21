package scratch.kevin.ucerf3;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipException;

import org.dom4j.DocumentException;
import org.opensha.commons.geo.GriddedRegion;
import org.opensha.sha.magdist.IncrementalMagFreqDist;

import scratch.UCERF3.CompoundFaultSystemSolution;
import scratch.UCERF3.analysis.CompoundFSSPlots.BranchAvgFSSBuilder;
import scratch.UCERF3.enumTreeBranches.DeformationModels;
import scratch.UCERF3.enumTreeBranches.FaultModels;
import scratch.UCERF3.enumTreeBranches.InversionModels;
import scratch.UCERF3.enumTreeBranches.ScalingRelationships;
import scratch.UCERF3.enumTreeBranches.SpatialSeisPDF;
import scratch.UCERF3.griddedSeismicity.GridSourceFileReader;
import scratch.UCERF3.griddedSeismicity.GridSourceProvider;
import scratch.UCERF3.inversion.InversionFaultSystemRupSet;
import scratch.UCERF3.inversion.InversionFaultSystemRupSetFactory;
import scratch.UCERF3.inversion.InversionFaultSystemSolution;
import scratch.UCERF3.logicTree.APrioriBranchWeightProvider;
import scratch.UCERF3.logicTree.LogicTreeBranch;
import scratch.UCERF3.logicTree.LogicTreeBranchNode;
import scratch.UCERF3.simulatedAnnealing.hpc.LogicTreePBSWriter;
import scratch.UCERF3.utils.FaultSystemIO;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class SolSubMeanExtractor {

	public static void main(String[] args) throws ZipException, IOException, DocumentException {
		File compoundFile = new File("/home/kevin/workspace/OpenSHA/dev/scratch/UCERF3/data/scratch/"
				+ "InversionSolutions/2013_05_10-ucerf3p3-production-10runs_COMPOUND_SOL_WITH_GRIDDED.zip");
		File outputDir = new File("/tmp/fm_dm_scale_seis_sols");
		Preconditions.checkState(outputDir.exists() || outputDir.mkdir());
		
		CompoundFaultSystemSolution cfss = CompoundFaultSystemSolution.fromZipFile(compoundFile);
		
		List<LogicTreeBranchNode<?>> fmBranches =
				LogicTreePBSWriter.getNonZeroChoices(FaultModels.class, InversionModels.CHAR_CONSTRAINED);
		List<LogicTreeBranchNode<?>> dmBranches =
				LogicTreePBSWriter.getNonZeroChoices(DeformationModels.class, InversionModels.CHAR_CONSTRAINED);
//		List<LogicTreeBranchNode<?>> scaleBranches =
//				LogicTreePBSWriter.getNonZeroChoices(ScalingRelationships.class, InversionModels.CHAR_CONSTRAINED);
		
		APrioriBranchWeightProvider weightProv = new APrioriBranchWeightProvider();
		
		for (FaultModels fm : FaultModels.values()) {
			if (fm.getRelativeWeight(InversionModels.CHAR_CONSTRAINED) <= 0d)
				continue;
			for (DeformationModels dm : DeformationModels.values()) {
				if (dm.getRelativeWeight(InversionModels.CHAR_CONSTRAINED) <= 0d)
					continue;
				for (ScalingRelationships scale : ScalingRelationships.values()) {
					if (scale.getRelativeWeight(InversionModels.CHAR_CONSTRAINED) <= 0d)
						continue;
					for (SpatialSeisPDF seis : SpatialSeisPDF.values()) {
						if (seis.getRelativeWeight(InversionModels.CHAR_CONSTRAINED) <= 0d)
							continue;
						String fName = fm.encodeChoiceString()+"_"+dm.encodeChoiceString()+"_"
							+scale.encodeChoiceString()+"_"+seis.encodeChoiceString()+"_BRANCH_AVG_SOL.zip";
						List<LogicTreeBranch> branches = getMatchingBranches(
								cfss.getBranches(), fm, dm, scale, seis);
						System.out.println("Making "+fName+" with "+branches.size()+" branches");
						
						LogicTreeBranch meanBranch = LogicTreeBranch.getMEAN_UCERF3(fm, dm);
						meanBranch.setValue(scale);
						meanBranch.setValue(seis);
						
						InversionFaultSystemRupSet reference =
								InversionFaultSystemRupSetFactory.forBranch(meanBranch);
						
						double[] rates = new double[reference.getNumRuptures()];
						double[] mags = new double[reference.getNumRuptures()];
						Map<Integer, IncrementalMagFreqDist> nodeSubSeisMFDs = Maps.newHashMap();
						Map<Integer, IncrementalMagFreqDist> nodeUnassociatedMFDs = Maps.newHashMap();
						GriddedRegion region = null;
						double totWeight = 0d;
						for (LogicTreeBranch branch : branches) {
							double weight = weightProv.getWeight(branch);
							
							double[] subRates = cfss.getRates(branch);
							double[] subMags = cfss.getMags(branch);
							
							Preconditions.checkState(rates.length == subRates.length);
							
							for (int r=0; r<rates.length; r++) {
								rates[r] += weight*subRates[r];
								mags[r] += weight*subMags[r];
							}
							
							// now grid sources
							GridSourceProvider gridProv = cfss.loadGridSourceProviderFile(branch);
							if (region == null)
								region = gridProv.getGriddedRegion();
							for (int i=0; i<gridProv.getGriddedRegion().getNodeCount(); i++) {
								BranchAvgFSSBuilder.addWeighted(
										nodeSubSeisMFDs, i, gridProv.getNodeSubSeisMFD(i), weight);
								BranchAvgFSSBuilder.addWeighted(
										nodeUnassociatedMFDs, i, gridProv.getNodeUnassociatedMFD(i), weight);
							}
							
							totWeight += weight;
						}
						
						// adjust for actual total weight
						for (int r=0; r<rates.length; r++) {
							rates[r] /= totWeight;
							mags[r] /= totWeight;
						}
						for (IncrementalMagFreqDist mfd : nodeSubSeisMFDs.values())
							mfd.scale(1d/totWeight);
						for (IncrementalMagFreqDist mfd : nodeUnassociatedMFDs.values())
							mfd.scale(1d/totWeight);
						
						String info = reference.getInfoString();
						
						info = "****** BRANCH AVERAGED SOLUTION FOR "+branches.size()+" SOLUTIONS ******\n\n"+info;
						
						List<List<Integer>> clusterRups = Lists.newArrayList();
						List<List<Integer>> clusterSects = Lists.newArrayList();
						for (int i=0; i<reference.getNumClusters(); i++) {
							clusterRups.add(reference.getRupturesForCluster(i));
							clusterSects.add(reference.getSectionsForCluster(i));
						}
						
						// first build the rup set
						InversionFaultSystemRupSet rupSet = new InversionFaultSystemRupSet(
								reference, reference.getLogicTreeBranch(), reference.getLaughTestFilter(),
								reference.getAveSlipForAllRups(), reference.getCloseSectionsListList(),
								reference.getRupturesForClusters(), reference.getSectionsForClusters());
						rupSet.setMagForallRups(mags);
						
						InversionFaultSystemSolution sol = new InversionFaultSystemSolution(rupSet, rates);
						
						GridSourceProvider gridSources = new GridSourceFileReader(region,
								nodeSubSeisMFDs, nodeUnassociatedMFDs);
						sol.setGridSourceProvider(gridSources);
						
						FaultSystemIO.writeSol(sol, new File(outputDir, fName));
					}
				}
			}
		}
	}
	
	private static List<LogicTreeBranch> getMatchingBranches(Collection<LogicTreeBranch> allBranches,
			LogicTreeBranchNode<?>... nodes) {
		List<LogicTreeBranch> matches = Lists.newArrayList();
		
		branchLoop:
		for (LogicTreeBranch branch : allBranches) {
			for (LogicTreeBranchNode<?> node : nodes) {
				if (branch.getValueUnchecked((Class<? extends LogicTreeBranchNode<?>>) node.getClass()) != node)
					continue branchLoop;
			}
			matches.add(branch);
		}
		
		return matches;
	}

}
