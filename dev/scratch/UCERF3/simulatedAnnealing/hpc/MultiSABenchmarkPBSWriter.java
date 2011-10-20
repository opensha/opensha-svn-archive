package scratch.UCERF3.simulatedAnnealing.hpc;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.opensha.commons.hpc.JavaShellScriptWriter;
import org.opensha.commons.hpc.mpj.MPJShellScriptWriter;
import org.opensha.commons.hpc.pbs.BatchScriptWriter;
import org.opensha.commons.hpc.pbs.RangerScriptWriter;
import org.opensha.commons.hpc.pbs.USC_HPCC_ScriptWriter;

import scratch.UCERF3.simulatedAnnealing.ThreadedSimulatedAnnealing;
import scratch.UCERF3.simulatedAnnealing.completion.CompletionCriteria;
import scratch.UCERF3.simulatedAnnealing.completion.TimeCompletionCriteria;
import scratch.UCERF3.simulatedAnnealing.params.CoolingScheduleType;

public class MultiSABenchmarkPBSWriter {
	
	public static File RUN_DIR = new File("/home/scec-02/kmilner/ucerf3/inversion_bench");
	
	public static ArrayList<File> getClasspath() {
		ArrayList<File> jars = new ArrayList<File>();
		jars.add(new File(RUN_DIR, "OpenSHA_complete.jar"));
		jars.add(new File(RUN_DIR, "parallelcolt-0.9.4.jar"));
		jars.add(new File(RUN_DIR, "commons-cli-1.2.jar"));
		jars.add(new File(RUN_DIR, "csparsej.jar"));
		return jars;
	}

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
//		String runName = "2011_09_29_new_create_test";
//		String runName = "2011_10_17-morgan-ncal1";
		String runName = "2011_10_19-threads_test";
		
		File writeDir = new File("/home/kevin/OpenSHA/UCERF3/test_inversion/bench/"+runName);
		if (!writeDir.exists())
			writeDir.mkdir();
		
		String queue = "nbns";
//		String queue = null;
		BatchScriptWriter batch = new USC_HPCC_ScriptWriter();
		int ppn = 8;
		File mpjHome = USC_HPCC_ScriptWriter.MPJ_HOME;
		File javaBin = USC_HPCC_ScriptWriter.JAVA_BIN;
		
//		String queue = "normal";
//		BatchScriptWriter batch = new RangerScriptWriter();
//		int ppn = 1;
//		File mpjHome = new File("/share/home/00950/kevinm/mpj-v0_38");
//		File javaBin = new File("/share/home/00950/kevinm/java/default/bin/java");
//		File runDir = new File("/work/00950/kevinm/ucerf3/inversion");
		
		File runSubDir = new File(RUN_DIR, runName);
		
		//		int annealMins = 60*8;
		//		int annealMins = 60*16;
		int dsaAnnealMins = 60*2;
		//		int tsaAnnealMins = dsaAnnealMins*2;
//		int tsaAnnealMins = 60*23;
		int tsaAnnealMins = 60*2;

		int dsaWallMins = dsaAnnealMins + 60;
		int tsaWallMins = tsaAnnealMins + 30;

		CompletionCriteria subCompletion = TimeCompletionCriteria.getInSeconds(5);

		ArrayList<File> jars = getClasspath();

		int heapSizeMB = 2048;
		
		boolean useMxdev = false;

		File aMat, dMat, initialMat;

		aMat = new File(runSubDir, "A.mat");
		dMat = new File(runSubDir, "d.mat");
		initialMat = null;
//		initialMat = new File(runSubDir, "initial.mat");
		
//		initialMat = new File(runDir, "initial.mat");
//		aMat = new File(runDir, "A_ncal_unconstrained.mat");
//		dMat = new File(runDir, "d_ncal_unconstrained.mat");
//		initialMat = null;

		CompletionCriteria dsaCriteria = TimeCompletionCriteria.getInMinutes(dsaAnnealMins);
		CompletionCriteria tsaCriteria = TimeCompletionCriteria.getInMinutes(tsaAnnealMins);
		
		MPJShellScriptWriter mpjWriter = new MPJShellScriptWriter(javaBin, heapSizeMB, jars, mpjHome, useMxdev);
		JavaShellScriptWriter javaWriter = new JavaShellScriptWriter(javaBin, heapSizeMB, jars);

		DistributedScriptCreator dsa_create = new DistributedScriptCreator(mpjWriter, aMat, dMat,
				initialMat, null, null, dsaCriteria, subCompletion, mpjHome, false);
		ThreadedScriptCreator tsa_create = new ThreadedScriptCreator(javaWriter, aMat, dMat,
				initialMat, null, null, tsaCriteria, subCompletion);

		int[] dsa_threads = { 4, 6, 8 };

		int[] tsa_threads = { 1 };
//		int[] tsa_threads = { 1,2,4,8 };
//		int[] tsa_threads = new int[0];

//		int[] nodes = { 20,50,100,200 };
//		int[] nodes = { 500 };
//		int[] nodes = { 50 };
//		int[] nodes = { 100, 200 };
		int[] nodes = { 5 };
//		int[] nodes = new int[0];

		//		int[] dSubIters = { 200, 600 };
		CompletionCriteria[] dSubComps = { subCompletion };
		CoolingScheduleType[] cools = { CoolingScheduleType.FAST_SA };

//		int numRuns = 1;
		int numRuns = 5;

		double nodeHours = 0;

		for (CoolingScheduleType cool : cools) {
			for (int numNodes : nodes) {
				for (int dsaThreads : dsa_threads) {
					for (CompletionCriteria dSubComp : dSubComps) {
						for (int r=0; r<numRuns; r++) {
							String name = "dsa_"+dsaThreads+"threads_"+numNodes+"nodes_"+cool.name();
							name += "_dSub"+ThreadedSimulatedAnnealing.subCompletionArgVal(dSubComp);
							name += "_sub"+ThreadedSimulatedAnnealing.subCompletionArgVal(subCompletion);
							name += "_run"+r;

							dsa_create.setProgFile(new File(runSubDir, name+".csv"));
							dsa_create.setSolFile(new File(runSubDir, name+".mat"));
							dsa_create.setNumThreads(""+dsaThreads);
							dsa_create.setCool(cool);
							if (dSubComp == subCompletion)
								dsa_create.setDistSubCompletion(null);
							else
								dsa_create.setDistSubCompletion(dSubComp);
							
							File pbs = new File(writeDir, name+".pbs");
							System.out.println("Writing: "+pbs.getName());
							batch.writeScript(pbs, dsa_create.buildScript(), dsaWallMins, numNodes, ppn, queue);
							
							nodeHours += (double)numNodes * ((double)dsaAnnealMins / 60d);
						}
					}
				}
			}
			for (int tsaThreads : tsa_threads) {
				for (int r=0; r<numRuns; r++) {
					String name = "tsa_"+tsaThreads+"threads_"+cool.name();
					name += "_sub"+ThreadedSimulatedAnnealing.subCompletionArgVal(subCompletion);
					name += "_run"+r;

					tsa_create.setProgFile(new File(runSubDir, name+".csv"));
					tsa_create.setSolFile(new File(runSubDir, name+".mat"));
					tsa_create.setNumThreads(""+tsaThreads);
					tsa_create.setCool(cool);
					tsa_create.setSubCompletion(subCompletion);

					File pbs = new File(writeDir, name+".pbs");
					System.out.println("Writing: "+pbs.getName());
					nodeHours += (double)tsaAnnealMins / 60d;
						batch.writeScript(pbs, tsa_create.buildScript(), tsaWallMins, 1, 1, queue);
					}
			}
		}

		System.out.println("Node hours: "+(float)nodeHours + " (/60: "+((float)nodeHours/60f)+")");
	}

}
