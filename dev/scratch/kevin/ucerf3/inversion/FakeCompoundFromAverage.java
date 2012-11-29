package scratch.kevin.ucerf3.inversion;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipException;

import org.dom4j.DocumentException;

import com.google.common.collect.Lists;

import scratch.UCERF3.AverageFaultSystemSolution;
import scratch.UCERF3.CompoundFaultSystemSolution;
import scratch.UCERF3.FaultSystemSolution;
import scratch.UCERF3.FaultSystemSolutionFetcher;
import scratch.UCERF3.logicTree.LogicTreeBranch;
import scratch.UCERF3.logicTree.VariableLogicTreeBranch;

public class FakeCompoundFromAverage {

	/**
	 * @param args
	 * @throws DocumentException 
	 * @throws IOException 
	 * @throws ZipException 
	 */
	public static void main(String[] args) throws ZipException, IOException, DocumentException {
		File dir = new File("/tmp");
		File inFile = new File(dir,
//				"FM3_1_ZENG_Shaw09Mod_DsrTap_CharConst_M5Rate8.7_MMaxOff7.6_NoFix_SpatSeisU3_mean_sol.zip");
				"FM3_1_ZENG_Shaw09Mod_DsrTap_CharConst_M5Rate8.7_MMaxOff7.6_NoFix_SpatSeisU3_VarZeros_mean_sol.zip");
		File outFile = new File(dir, "zeng_convergence_compound.zip");
		
		final AverageFaultSystemSolution avgSol = AverageFaultSystemSolution.fromZipFile(inFile);
		final LogicTreeBranch branch = LogicTreeBranch.fromFileName(inFile.getName());
		
		final List<LogicTreeBranch> branches = Lists.newArrayList();
		for (int s=0; s<avgSol.getNumSolutions(); s++)
			branches.add(new VariableLogicTreeBranch(branch, Lists.newArrayList("Var"+s)));
		
		FaultSystemSolutionFetcher fetcher = new FaultSystemSolutionFetcher() {
			
			@Override
			public Collection<LogicTreeBranch> getBranches() {
				return branches;
			}
			
			@Override
			protected FaultSystemSolution fetchSolution(LogicTreeBranch branch) {
				String var = ((VariableLogicTreeBranch)branch).getVariations().get(0);
				int s = Integer.parseInt(var.substring(3));
				return avgSol.getSolution(s);
			}
			
			@Override
			protected Map<String, Double> fetchMisfits(LogicTreeBranch branch) {
				return null;
			}
		};
		
		CompoundFaultSystemSolution.toZipFile(outFile, fetcher);
	}

}
