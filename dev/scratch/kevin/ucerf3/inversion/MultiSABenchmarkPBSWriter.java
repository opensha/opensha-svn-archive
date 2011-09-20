package scratch.kevin.ucerf3.inversion;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import scratch.UCERF3.simulatedAnnealing.completion.CompletionCriteria;
import scratch.UCERF3.simulatedAnnealing.completion.TimeCompletionCriteria;
import scratch.UCERF3.simulatedAnnealing.hpc.BatchScriptWriter;
import scratch.UCERF3.simulatedAnnealing.hpc.DistributedScriptCreator;
import scratch.UCERF3.simulatedAnnealing.hpc.RangerScriptWriter;
import scratch.UCERF3.simulatedAnnealing.hpc.ThreadedScriptCreator;
import scratch.UCERF3.simulatedAnnealing.hpc.USC_HPCC_ScriptWriter;
import scratch.UCERF3.simulatedAnnealing.params.CoolingScheduleType;

public class MultiSABenchmarkPBSWriter {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
//		String runName = "2011_08_17-morgan";
//		String runName = "2011_09-02-ncal_50node";
//		String runName = "2011_09-06-ncal_const_100node";
//		String runName = "2011_09_08-ranger-morgan-new";
//		String runName = "ncal_1_sup_1thread_long";
		String runName = "2011_09_16_genetic_test";
		
		File writeDir = new File("/home/kevin/OpenSHA/UCERF3/test_inversion/bench/"+runName);
		if (!writeDir.exists())
			writeDir.mkdir();
		
//		String queue = "nbns";
		String queue = null;
		BatchScriptWriter bath = new USC_HPCC_ScriptWriter();
		int ppn = 8;
		File mpjHome = new File("/home/rcf-12/kmilner/mpj-v0_38/");
		File javaBin = new File("/usr/usc/jdk/default/jre/bin/java");
		File runDir = new File("/home/scec-02/kmilner/ucerf3/inversion_bench");
		
//		String queue = "normal";
//		BatchScriptWriter bath = new RangerScriptWriter();
//		int ppn = 1;
//		File mpjHome = new File("/share/home/00950/kevinm/mpj-v0_38");
//		File javaBin = new File("/share/home/00950/kevinm/java/default/bin/java");
//		File runDir = new File("/work/00950/kevinm/ucerf3/inversion");
		
		File runSubDir = new File(runDir, runName);
		
		//		int annealMins = 60*8;
		//		int annealMins = 60*16;
		int dsaAnnealMins = 60*2;
		//		int tsaAnnealMins = dsaAnnealMins*2;
		int tsaAnnealMins = 60*23;

		int dsaWallMins = dsaAnnealMins + 60;
		int tsaWallMins = tsaAnnealMins + 30;

		//		int subIterations = 100;
		int subIterations = 200;

		ArrayList<File> jars = new ArrayList<File>();
		jars.add(new File(runDir, "OpenSHA_complete.jar"));
		jars.add(new File(runDir, "parallelcolt-0.9.4.jar"));
		jars.add(new File(runDir, "commons-cli-1.2.jar"));
		jars.add(new File(runDir, "csparsej.jar"));

		int heapSizeMB = 2048;

		File aMat, dMat, initialMat;

		aMat = new File(runSubDir, "A.mat");
		dMat = new File(runSubDir, "d.mat");
//		initialMat = null;
		initialMat = new File(runSubDir, "initial.mat");
		
//		initialMat = new File(runDir, "initial.mat");
//		aMat = new File(runDir, "A_ncal_unconstrained.mat");
//		dMat = new File(runDir, "d_ncal_unconstrained.mat");
//		initialMat = null;

		CompletionCriteria dsaCriteria = TimeCompletionCriteria.getInMinutes(dsaAnnealMins);
		CompletionCriteria tsaCriteria = TimeCompletionCriteria.getInMinutes(tsaAnnealMins);

		DistributedScriptCreator dsa_create = new DistributedScriptCreator(javaBin, jars, heapSizeMB, aMat, dMat,
				initialMat, subIterations, -1, null, dsaCriteria, mpjHome, false);
		ThreadedScriptCreator tsa_create = new ThreadedScriptCreator(javaBin, jars, heapSizeMB, aMat, dMat,
				initialMat, subIterations, -1, null, tsaCriteria);

		int[] dsa_threads = { 4 };

//		int[] tsa_threads = { 1 };
//		int[] tsa_threads = { 1,2,4,8 };
		int[] tsa_threads = new int[0];

//		int[] nodes = { 20,50,100,200 };
//		int[] nodes = { 500 };
//		int[] nodes = { 50 };
//		int[] nodes = { 100, 200 };
		int[] nodes = { 2, 5, 10 };
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
							bath.writeScript(pbs, dsa_create.buildScript(), dsaWallMins, numNodes, ppn, queue);
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
