package scratch.kevin.ucerf3;

import java.io.File;
import java.io.IOException;

import org.dom4j.DocumentException;
import org.opensha.sha.earthquake.ProbEqkSource;
import org.opensha.sha.earthquake.param.IncludeBackgroundOption;
import org.opensha.sha.earthquake.param.IncludeBackgroundParam;
import org.opensha.sha.earthquake.param.ProbabilityModelOptions;
import org.opensha.sha.earthquake.param.ProbabilityModelParam;

import scratch.UCERF3.FaultSystemSolution;
import scratch.UCERF3.erf.FaultSystemSolutionERF;
import scratch.UCERF3.utils.FaultSystemIO;

public class ERF_MemDebug {
	
	public static void main(String[] args) throws IOException, DocumentException {
		FaultSystemSolution sol = FaultSystemIO.loadSol(new File("/home/kevin/workspace/OpenSHA/"
				+ "dev/scratch/UCERF3/data/scratch/InversionSolutions/"
				+ "2013_05_10-ucerf3p3-production-10runs_COMPOUND_SOL_FM3_1_MEAN_BRANCH_AVG_SOL.zip"));
		boolean timeDep = false;
		FaultSystemSolutionERF erf = new FaultSystemSolutionERF(sol);
		if (timeDep)
			erf.setParameter(ProbabilityModelParam.NAME, ProbabilityModelOptions.U3_BPT);
		erf.getTimeSpan().setDuration(50);
		erf.setParameter(IncludeBackgroundParam.NAME, IncludeBackgroundOption.EXCLUDE);
//		erf.setUseFSSDateOfLastEvents(true);
		erf.updateForecast();
		System.out.println("Done updating");
		
		while (true) {
			for (ProbEqkSource source : erf) {
				// do nothing
			}
		}
	}

}
