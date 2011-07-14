package scratch.kevin.ucerf3.inversion;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import scratch.UCERF3.simulatedAnnealing.SimulatedAnnealing;
import scratch.UCERF3.simulatedAnnealing.ThreadedSimulatedAnnealing;
import scratch.UCERF3.simulatedAnnealing.completion.CompletionCriteria;
import scratch.UCERF3.simulatedAnnealing.completion.TimeCompletionCriteria;
import scratch.UCERF3.simulatedAnnealing.hpc.ThreadedScriptCreator;
import scratch.UCERF3.simulatedAnnealing.params.CoolingScheduleType;

public class ThreadedSABenchmarkPBSWriter {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		File writeDir = new File("/home/kevin/OpenSHA/UCERF3/test_inversion/bench");
		File runDir = new File("/home/scec-02/kmilner/ucerf3/inversion_bench");
		
		int mins = 60*2;
		
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
		
		CompletionCriteria criteria = TimeCompletionCriteria.getInMinutes(mins-10);
		
		ThreadedScriptCreator creator = new ThreadedScriptCreator(javaBin, jars, heapSizeMB, aMat, dMat,
				initialMat, subIterations, -1, null, criteria);
		
		int[] threads = { 1, 2, 4, 8 };
		CoolingScheduleType[] cools = { CoolingScheduleType.VERYFAST_SA };
		
		int numRuns = 5;

		for (CoolingScheduleType cool : cools) {
			for (int numThreads : threads) {
				for (int r=0; r<numRuns; r++) {
					String name = "tsa_"+numThreads+"threads_"+cool.name();
					name += "_run"+r;

					creator.setProgFile(new File(runDir, name+".csv"));
					creator.setSolFile(new File(runDir, name+".mat"));
					creator.setNumThreads(numThreads);
					creator.setCool(cool);

					creator.writeScript(new File(writeDir, name), creator.buildPBSScript(mins, 1, 8, "nbns"));
				}
			}
		}
	}

}
