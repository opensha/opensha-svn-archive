package scratch.UCERF3.simulatedAnnealing.hpc;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.opensha.commons.hpc.JavaShellScriptWriter;
import org.opensha.commons.hpc.mpj.MPJShellScriptWriter;
import org.opensha.commons.hpc.pbs.BatchScriptWriter;
import org.opensha.commons.hpc.pbs.USC_HPCC_ScriptWriter;
import org.opensha.commons.util.ClassUtils;

import scratch.UCERF3.simulatedAnnealing.ThreadedSimulatedAnnealing;
import scratch.UCERF3.simulatedAnnealing.completion.CompletionCriteria;
import scratch.UCERF3.simulatedAnnealing.completion.TimeCompletionCriteria;
import scratch.UCERF3.simulatedAnnealing.params.CoolingScheduleType;

public class PBSScriptWriter {

	public static void main(String[] args) {
		if (args.length < 8 || args.length > 12) {
			System.out.println("USAGE: "+ClassUtils.getClassNameWithoutPackage(PBSScriptWriter.class)
					+" <nodes> <minutes> <sub-completion> <PPN> <threads> <dir>"
					+" [<A mat file> <d file> [<intial> [<a_ineq> <d_ineq>]] OR --zip <zip file> [jobName]]");
			System.exit(2);
		}
		try {
			int cnt = 0;
			int nodes = Integer.parseInt(args[cnt++]);
			int annealMins = Integer.parseInt(args[cnt++]);
			CompletionCriteria subCompletion = ThreadedSimulatedAnnealing.parseSubCompletionCriteria(args[cnt++]);
			int ppn = Integer.parseInt(args[cnt++]);
			String numThreads = args[cnt++];
			File dir = new File(args[cnt++]).getCanonicalFile();
			String name = dir.getName();
			
			
			File aFile = null;
			File dFile = null;
			File a_ineqFile = null;
			File d_ineqFile = null;
			File zipFile = null;
			File initial = null;
			if (args[cnt].equals("--zip")) {
				cnt++;
				zipFile = new File(args[cnt++]).getCanonicalFile();
				if (cnt < args.length)
					name = args[cnt++];
			} else {
				aFile = new File(args[cnt++]).getCanonicalFile();
				dFile = new File(args[cnt++]).getCanonicalFile();
				if (cnt < args.length) {
					initial = new File(args[cnt++]).getAbsoluteFile();
					if (cnt < args.length) {
						a_ineqFile = new File(args[cnt++]).getAbsoluteFile();
						d_ineqFile = new File(args[cnt++]).getAbsoluteFile();
						if (cnt < args.length)
							name = args[cnt++];
					}
				}
			}
			
			int wallMins = annealMins+60;

			BatchScriptWriter batch = new USC_HPCC_ScriptWriter();
			File mpjHome = USC_HPCC_ScriptWriter.MPJ_HOME;
			File javaBin = USC_HPCC_ScriptWriter.JAVA_BIN;

			CoolingScheduleType cool = CoolingScheduleType.FAST_SA;
			CompletionCriteria criteria = TimeCompletionCriteria.getInMinutes(annealMins);
			boolean useMXDev = false;
//			int heapSizeMB = 2048;
			int heapSizeMB = 10000;
			try {
				int threadsInt = Integer.parseInt(numThreads);
				if (threadsInt >= 12)
					heapSizeMB = 40000;
			} catch (NumberFormatException e) {
			}
			String queue = null;

			ArrayList<File> classpath = MultiSABenchmarkPBSWriter.getClasspath();

			ThreadedScriptCreator creator;
			if (nodes > 1) {
				MPJShellScriptWriter mpjWriter = new MPJShellScriptWriter(javaBin, heapSizeMB, classpath, mpjHome, useMXDev);
				creator = new DistributedScriptCreator(mpjWriter,numThreads, null, criteria,
						subCompletion, mpjHome, false);
			} else {
				JavaShellScriptWriter javaWriter = new JavaShellScriptWriter(javaBin, heapSizeMB, classpath);
				creator = new ThreadedScriptCreator(javaWriter, numThreads, null, criteria, subCompletion);
			}
			creator.setaMat(aFile);
			creator.setdMat(dFile);
			creator.setA_ineqMat(a_ineqFile);
			creator.setD_ineqMat(d_ineqFile);
			creator.setInitial(initial);
			creator.setZipFile(zipFile);

			creator.setProgFile(new File(dir, name+".csv"));
			creator.setSolFile(new File(dir, name+".bin"));
			creator.setCool(cool);

			File pbs = new File(dir, name+".pbs");
			System.out.println("Writing: "+pbs.getName());

			batch.writeScript(pbs, creator.buildScript(), wallMins, nodes, ppn, queue);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}

		System.exit(0);
	}

}
