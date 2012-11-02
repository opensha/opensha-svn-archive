package scratch.kevin.ucerf3;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.zip.ZipException;

import org.opensha.commons.data.region.CaliforniaRegions;
import org.opensha.commons.geo.GriddedRegion;
import org.opensha.commons.geo.Region;
import org.opensha.sha.earthquake.calc.ERF_Calculator;

import com.google.common.collect.Lists;

import scratch.UCERF3.CompoundFaultSystemSolution;
import scratch.UCERF3.FaultSystemSolution;
import scratch.UCERF3.erf.FaultSystemSolutionPoissonERF;
import scratch.UCERF3.erf.UCERF3_FaultSysSol_ERF;
import scratch.UCERF3.inversion.InversionFaultSystemSolution;
import scratch.UCERF3.logicTree.LogicTreeBranch;

public class FSS_ERF_MemLeakTest {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws ZipException 
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws ZipException, IOException, InterruptedException {
//		System.out.println("Start the profiler!");
//		Thread.sleep(15000);
		
		File solFile = new File("/tmp/2012_10_29-logic-tree-fm3_1_x7-fm3_2_x1_COMPOUND_SOL.zip");
		CompoundFaultSystemSolution fetch = CompoundFaultSystemSolution.fromZipFile(solFile);
		
		List<LogicTreeBranch> branches = Lists.newArrayList(fetch.getBranches());
		Collections.shuffle(branches);
		
		Region region = new CaliforniaRegions.RELM_TESTING();
		GriddedRegion griddedRegion = new GriddedRegion(region, 1d, null);
		Random r = new Random();
		
		int cnt = 0;
		for (LogicTreeBranch branch : branches) {
			FaultSystemSolution sol = fetch.getSolution(branch);
			System.gc();
			System.out.println("*** Loaded solution "+(cnt++));
			
//			UCERF3_FaultSysSol_ERF erf = new UCERF3_FaultSysSol_ERF(new InversionFaultSystemSolution(fetch.getSolution(branch)));
			FaultSystemSolutionPoissonERF erf = new FaultSystemSolutionPoissonERF(sol);
			erf.updateForecast();
//			System.out.println("Calculating MFDs");
//			ERF_Calculator.getMagFreqDistInRegion(erf, region, 5d, 41, 0.1d, r.nextBoolean());
//			System.out.println("Calculating Participation Rates");
//			ERF_Calculator.getParticipationRatesInRegion(erf, griddedRegion, 6.7d, 10d);
			System.out.println("DONE");
			
			if (cnt == 3)
				Thread.sleep(1000000000);
		}
	}

}
