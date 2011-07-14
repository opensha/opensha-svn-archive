package scratch.kevin.ucerf3.inversion;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import scratch.UCERF3.simulatedAnnealing.completion.CompletionCriteria;
import scratch.UCERF3.simulatedAnnealing.completion.TimeCompletionCriteria;
import scratch.UCERF3.simulatedAnnealing.hpc.DistributedScriptCreator;
import scratch.UCERF3.simulatedAnnealing.hpc.ThreadedScriptCreator;
import scratch.UCERF3.simulatedAnnealing.params.CoolingScheduleType;

public class DistributedSABenchmarkPBSWriter {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		File writeDir = new File("/home/kevin/OpenSHA/UCERF3/test_inversion/bench");
		File runDir = new File("/home/scec-02/kmilner/ucerf3/inversion_bench");
		File dsaDir = new File(runDir, "dsa");
		
		int mins = 60*2 + 20;
		
		int subIterations = 5000;
		
		ArrayList<File> jars = new ArrayList<File>();
		jars.add(new File(runDir, "OpenSHA_complete.jar"));
		jars.add(new File(runDir, "parallelcolt-0.9.4.jar"));
		jars.add(new File(runDir, "commons-cli-1.2.jar"));
		jars.add(new File(runDir, "csparsej.jar"));
		
		int heapSizeMB = 2048;
		
		File javaBin = new File("/usr/usc/jdk/default/jre/bin/java");
		
		File aMat = new File(runDir, "A.mat");
		File dMat = new File(runDir, "d.mat");
		File initialMat = new File(runDir, "initial.mat");
		
		CompletionCriteria criteria = TimeCompletionCriteria.getInMinutes(mins-30);
		
		File mpjHome = new File("/home/rcf-12/kmilner/mpj-v0_38/");
		
		DistributedScriptCreator creator = new DistributedScriptCreator(javaBin, jars, heapSizeMB, aMat, dMat,
				initialMat, subIterations, -1, null, criteria, mpjHome, false);
		
		int[] threads = { 1, 4 };
		int[] nodes = { 2, 5, 10 };
		CoolingScheduleType[] cools = { CoolingScheduleType.VERYFAST_SA };
		
		int numRuns = 5;

		for (CoolingScheduleType cool : cools) {
			for (int numNodes : nodes) {
				for (int numThreads : threads) {
					for (int r=0; r<numRuns; r++) {
						String name = "dsa_"+numThreads+"threads_"+numNodes+"nodes_"+cool.name();
						name += "_run"+r;

						creator.setProgFile(new File(dsaDir, name+".csv"));
						creator.setSolFile(new File(dsaDir, name+".mat"));
						creator.setNumThreads(numThreads);
						creator.setCool(cool);

						creator.writeScript(new File(writeDir, name+".pbs"),
								creator.buildPBSScript(mins, numNodes, 1, "nbns"));
					}
				}
			}
		}
	}

}
