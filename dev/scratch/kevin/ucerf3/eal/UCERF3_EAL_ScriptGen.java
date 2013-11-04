package scratch.kevin.ucerf3.eal;

import java.io.File;

import org.opensha.commons.hpc.JavaShellScriptWriter;
import org.opensha.commons.hpc.pbs.BatchScriptWriter;
import org.opensha.commons.hpc.pbs.USC_HPCC_ScriptWriter;
import org.opensha.sra.calc.parallel.MPJ_CondLossCalc;

import scratch.UCERF3.erf.mean.CurveCalcTest;
import scratch.UCERF3.simulatedAnnealing.hpc.LogicTreePBSWriter;

public class UCERF3_EAL_ScriptGen {

	public static void main(String[] args) {
		File writeDir = new File("/home/kevin/OpenSHA/UCERF3/eal");
		if (!writeDir.exists())
			writeDir.mkdir();
		
		BatchScriptWriter pbsWrite = new USC_HPCC_ScriptWriter();
		File remoteDir = new File("/auto/scec-02/kmilner/ucerf3/curves/MeanUCERF3-curves");
		File javaBin = USC_HPCC_ScriptWriter.JAVA_BIN;
		File mpjHome = USC_HPCC_ScriptWriter.FMPJ_HOME;
		int maxHeapMB = 9000;
		
//		BatchScriptWriter pbsWrite = new StampedeScriptWriter();
//		File remoteDir = new File("/work/00950/kevinm/ucerf3/curves/MeanUCERF3-curves");
//		File javaBin = StampedeScriptWriter.JAVA_BIN;
//		File mpjHome = StampedeScriptWriter.FMPJ_HOME;
//		int maxHeapMB = 26000;
		
		String meanSolFileName = "2013_05_10-ucerf3p3-production-10runs_COMPOUND_SOL_TRUE_HAZARD_MEAN_SOL.zip";
		File meanSolFile = new File(remoteDir, meanSolFileName);
		
		JavaShellScriptWriter javaWrite = new JavaShellScriptWriter(javaBin, maxHeapMB,
				LogicTreePBSWriter.getClasspath(remoteDir, remoteDir));
		
		int mins = 1000;
		int nodes = 40;
		int ppn = 8;
		String queue = null;
		
		String className = MPJ_CondLossCalc.class.getName();
		
		int numJobs = 0;
	}

}
