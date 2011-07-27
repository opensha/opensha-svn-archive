package scratch.kevin.ucerf3.inversion;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import scratch.UCERF3.simulatedAnnealing.completion.CompletionCriteria;
import scratch.UCERF3.simulatedAnnealing.completion.TimeCompletionCriteria;
import scratch.UCERF3.simulatedAnnealing.hpc.DistributedScriptCreator;
import scratch.UCERF3.simulatedAnnealing.hpc.ThreadedScriptCreator;
import scratch.UCERF3.simulatedAnnealing.params.CoolingScheduleType;

public class MultiSABenchmarkPBSWriter {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		File writeDir = new File("/home/kevin/OpenSHA/UCERF3/test_inversion/bench");
		File runDir = new File("/home/scec-02/kmilner/ucerf3/inversion_bench");
		File runSubDir = new File(runDir, "mult_statewide");

		int mins = 60*2 + 20;

		int subIterations = 200;
		//		int dSubIterations = 10000;

		ArrayList<File> jars = new ArrayList<File>();
		jars.add(new File(runDir, "OpenSHA_complete.jar"));
		jars.add(new File(runDir, "parallelcolt-0.9.4.jar"));
		jars.add(new File(runDir, "commons-cli-1.2.jar"));
		jars.add(new File(runDir, "csparsej.jar"));

		int heapSizeMB = 2048;

		File javaBin = new File("/usr/usc/jdk/default/jre/bin/java");

		File aMat = new File(runDir, "A_statewide.mat");
		File dMat = new File(runDir, "d_statewide.mat");
		File initialMat = null;
		//		File initialMat = new File(runDir, "initial.mat");

		CompletionCriteria criteria = TimeCompletionCriteria.getInMinutes(mins-30);

		File mpjHome = new File("/home/rcf-12/kmilner/mpj-v0_38/");

		DistributedScriptCreator dsa_create = new DistributedScriptCreator(javaBin, jars, heapSizeMB, aMat, dMat,
				initialMat, subIterations, -1, null, criteria, mpjHome, false);
		ThreadedScriptCreator tsa_create = new ThreadedScriptCreator(javaBin, jars, heapSizeMB, aMat, dMat,
				initialMat, subIterations, -1, null, criteria);

		//		int[] threads = { 1, 4 };
		int[] dsa_threads = { 4 };
		int[] tsa_threads = { 1,2,4,8 };
		//		int[] nodes = { 2, 5, 10 };
		int[] nodes = { 5 };
		int[] dSubIters = { 200 };
		CoolingScheduleType[] cools = { CoolingScheduleType.VERYFAST_SA };

		int numRuns = 5;

		String queue = "nbns";

		for (CoolingScheduleType cool : cools) {
			for (int numNodes : nodes) {
				for (int dsaThreads : dsa_threads) {
					for (int numDistSubIterations : dSubIters) {
						for (int r=0; r<numRuns; r++) {
							String name = "dsa_"+dsaThreads+"threads_"+numNodes+"nodes_"+cool.name();
							name += "_run"+r;

							dsa_create.setProgFile(new File(runSubDir, name+".csv"));
							dsa_create.setSolFile(new File(runSubDir, name+".mat"));
							dsa_create.setNumThreads(dsaThreads);
							dsa_create.setCool(cool);
							dsa_create.setNumDistSubIterations(numDistSubIterations);
							int tmpSubIters = subIterations;
							if (tmpSubIters > numDistSubIterations)
								tmpSubIters = numDistSubIterations;
							dsa_create.setSubIterations(tmpSubIters);

							dsa_create.writeScript(new File(writeDir, name+".pbs"),
									dsa_create.buildPBSScript(mins, numNodes, 1, queue));
						}
					}
				}
			}
			for (int tsaThreads : tsa_threads) {
					for (int r=0; r<numRuns; r++) {
						String name = "tsa_"+tsaThreads+"threads_"+cool.name();
						name += "_run"+r;

						tsa_create.setProgFile(new File(runSubDir, name+".csv"));
						tsa_create.setSolFile(new File(runSubDir, name+".mat"));
						tsa_create.setNumThreads(tsaThreads);
						tsa_create.setCool(cool);
						int tmpSubIters = subIterations;
						tsa_create.setSubIterations(subIterations);

						tsa_create.writeScript(new File(writeDir, name+".pbs"),
								dsa_create.buildPBSScript(mins, 1, 1, queue));
					}
			}
		}
	}

}