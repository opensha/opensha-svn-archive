package scratch.kevin.ucerf3.inversion;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import scratch.UCERF3.simulatedAnnealing.SimulatedAnnealing;
import scratch.UCERF3.simulatedAnnealing.ThreadedSimulatedAnnealing;
import scratch.UCERF3.simulatedAnnealing.SimulatedAnnealing.CoolingScheduleType;

public class ThreadedSABenchmarkPBSWriter {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		File writeDir = new File("/home/kevin/OpenSHA/UCERF3/test_inversion/bench");
		File runDir = new File("/home/scec-02/kmilner/ucerf3/inversion_bench");
		File jarFile = new File(runDir, "OpenSHA_complete.jar");
		File coltJar = new File(runDir, "parallelcolt-0.9.4.jar");
		File cliJar = new File(runDir, "commons-cli-1.2.jar");
		File csparseJar = new File(runDir, "csparsej.jar");
		
		String javaPath = "/usr/usc/jdk/default/jre/bin/java";
		
		int mins = 60*2;
		
		int subIterations = 10000;
		
		boolean[] true_false = { true, false };
		int[] threads = { 1, 2, 4, 8 };
		
		for (CoolingScheduleType cool : SimulatedAnnealing.CoolingScheduleType.values()) {
			for (boolean startSubIterationsAtZero : true_false) {
				for (int numThreads : threads) {
					String name = "tsa_"+numThreads+"threads_"+cool.name();
					if (startSubIterationsAtZero)
						name += "_startSubIterationsAtZero";
					
					File progressFile = new File(runDir, name+".csv");
					File solutionFile = new File(runDir, name+".mat");
					File aMat = new File(runDir, "A.mat");
					File dMat = new File(runDir, "d.mat");
					File initialMat = new File(runDir, "initial.mat");
					
					String cmd = javaPath + " -Xmx2G -cp "+jarFile.getAbsolutePath()
							+":"+coltJar.getAbsolutePath()
							+":"+csparseJar.getAbsolutePath()
							+":"+cliJar.getAbsolutePath()
							+" "+ThreadedSimulatedAnnealing.class.getName()
							+" --a-matrix-file "+aMat.getAbsolutePath()
							+" --d-matrix-file "+dMat.getAbsolutePath()
							+" --initial-state-file "+initialMat.getAbsolutePath()
							+" --sub-iterations "+subIterations
							+" --num-threads "+numThreads
							+" --solution-file "+solutionFile.getAbsolutePath()
							+" --progress-file "+progressFile.getAbsolutePath()
							+" --completion-time "+(mins-10)+"m"
							+" --cool "+cool.name();
					if (startSubIterationsAtZero)
						cmd += " --start-sub-iters-zero";
					
					FileWriter fw = new FileWriter(new File(writeDir, name+".pbs"));
					
					fw.write("#!/bin/bash"+"\n");
					fw.write(""+"\n");
					fw.write("#PBS -l walltime=00:"+mins+":00,nodes=1:ppn=8"+"\n");
					fw.write("#PBS -V"+"\n");
					fw.write(""+"\n");
					fw.write(cmd+"\n");
					fw.write("exit $?"+"\n");
					
					fw.close();
				}
			}
		}
	}

}
