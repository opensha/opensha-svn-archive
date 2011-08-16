package scratch.kevin.ucerf3.inversion;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import scratch.UCERF3.simulatedAnnealing.completion.CompletionCriteria;
import scratch.UCERF3.simulatedAnnealing.completion.TimeCompletionCriteria;
import scratch.UCERF3.simulatedAnnealing.hpc.BatchScriptWriter;
import scratch.UCERF3.simulatedAnnealing.hpc.DistributedScriptCreator;
import scratch.UCERF3.simulatedAnnealing.hpc.ThreadedScriptCreator;
import scratch.UCERF3.simulatedAnnealing.hpc.USC_HPCC_ScriptWriter;
import scratch.UCERF3.simulatedAnnealing.params.CoolingScheduleType;

public class MultiSABenchmarkPBSWriter {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		File writeDir = new File("/home/kevin/OpenSHA/UCERF3/test_inversion/bench/multi");
		File runDir = new File("/home/scec-02/kmilner/ucerf3/inversion_bench");

		boolean nCal = true;
		//		int annealMins = 60*8;
		//		int annealMins = 60*16;
		int dsaAnnealMins = 60*8;
		//		int tsaAnnealMins = dsaAnnealMins*2;
		int tsaAnnealMins = 60*23;

		File runSubDir;
		if (nCal)
			runSubDir = new File(runDir, "mult_ncal");
		else
			runSubDir = new File(runDir, "mult_statewide");

		int dsaWallMins = dsaAnnealMins + 30;
		int tsaWallMins = tsaAnnealMins + 30;

		//		int subIterations = 100;
		int subIterations = 200;
		
		String queue = "nbns";
		BatchScriptWriter bath = new USC_HPCC_ScriptWriter();

		ArrayList<File> jars = new ArrayList<File>();
		jars.add(new File(runDir, "OpenSHA_complete.jar"));
		jars.add(new File(runDir, "parallelcolt-0.9.4.jar"));
		jars.add(new File(runDir, "commons-cli-1.2.jar"));
		jars.add(new File(runDir, "csparsej.jar"));

		int heapSizeMB = 2048;

		File javaBin = new File("/usr/usc/jdk/default/jre/bin/java");

		File aMat, dMat, initialMat;

		if (nCal) {
			//			aMat = new File(runDir, "A.mat");
			//			dMat = new File(runDir, "d.mat");
			//			initialMat = new File(runDir, "initial.mat");
			aMat = new File(runDir, "A_ncal_unconstrained.mat");
			dMat = new File(runDir, "d_ncal_unconstrained.mat");
			initialMat = null;
		} else {
			aMat = new File(runDir, "A_statewide.mat");
			dMat = new File(runDir, "d_statewide.mat");
			initialMat = null;
		}

		CompletionCriteria dsaCriteria = TimeCompletionCriteria.getInMinutes(dsaAnnealMins);
		CompletionCriteria tsaCriteria = TimeCompletionCriteria.getInMinutes(tsaAnnealMins);

		File mpjHome = new File("/home/rcf-12/kmilner/mpj-v0_38/");

		DistributedScriptCreator dsa_create = new DistributedScriptCreator(javaBin, jars, heapSizeMB, aMat, dMat,
				initialMat, subIterations, -1, null, dsaCriteria, mpjHome, false);
		ThreadedScriptCreator tsa_create = new ThreadedScriptCreator(javaBin, jars, heapSizeMB, aMat, dMat,
				initialMat, subIterations, -1, null, tsaCriteria);

		int[] dsa_threads = { 4 };

		//		int[] tsa_threads = { 1,2,4,8 };
		int[] tsa_threads = { 1 };
		//		int[] tsa_threads = new int[0];

		//		int[] nodes = { 2, 5, 10 };
		//		int[] nodes = { 2, 5, 10, 20 };
		int[] nodes = { 10 };
		//		int[] nodes = new int[0];

		//		int[] dSubIters = { 200, 600 };
		int[] dSubIters = { 200 };
		CoolingScheduleType[] cools = { CoolingScheduleType.FAST_SA };

		int numRuns = 5;

		double nodeHours = 0;

		for (CoolingScheduleType cool : cools) {
			for (int numNodes : nodes) {
				for (int dsaThreads : dsa_threads) {
					for (int numDistSubIterations : dSubIters) {
						for (int r=0; r<numRuns; r++) {
							String name = "dsa_"+dsaThreads+"threads_"+numNodes+"nodes_"+cool.name();
							name += "_dSub"+numDistSubIterations;
							name += "_sub"+subIterations;
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
							
							File pbs = new File(writeDir, name+".pbs");
							System.out.println("Writing: "+pbs.getName());
							nodeHours += (double)numNodes * ((double)dsaAnnealMins / 60d);
							bath.writeScript(pbs, dsa_create.buildScript(), dsaWallMins, numNodes, 1, queue);
						}
					}
				}
			}
			for (int tsaThreads : tsa_threads) {
				for (int r=0; r<numRuns; r++) {
					String name = "tsa_"+tsaThreads+"threads_"+cool.name();
					name += "_sub"+subIterations;
					name += "_run"+r;

					tsa_create.setProgFile(new File(runSubDir, name+".csv"));
					tsa_create.setSolFile(new File(runSubDir, name+".mat"));
					tsa_create.setNumThreads(tsaThreads);
					tsa_create.setCool(cool);
					tsa_create.setSubIterations(subIterations);

					File pbs = new File(writeDir, name+".pbs");
					System.out.println("Writing: "+pbs.getName());
					nodeHours += (double)tsaAnnealMins / 60d;
						bath.writeScript(pbs, tsa_create.buildScript(), tsaWallMins, 1, 1, queue);
					}
			}
		}

		System.out.println("Node hours: "+(float)nodeHours + " (/60: "+((float)nodeHours/60f)+")");
	}

}
