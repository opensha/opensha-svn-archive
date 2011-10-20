package scratch.UCERF3.simulatedAnnealing.hpc;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

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
		if (args.length < 8 || args.length > 9) {
			System.out.println("USAGE: "+ClassUtils.getClassNameWithoutPackage(PBSScriptWriter.class)
					+" <nodes> <minutes> <sub-completion> <PPN> <threads> <dir> <A mat file> <d file> [<intial>]");
			System.exit(2);
		}
		try {
			int cnt = 0;
			int nodes = Integer.parseInt(args[cnt++]);
			int annealMins = Integer.parseInt(args[cnt++]);
			CompletionCriteria subCompletion = ThreadedSimulatedAnnealing.parseSubCompletionCriteria(args[cnt++]);
			int ppn = Integer.parseInt(args[cnt++]);
			int numThreads = Integer.parseInt(args[cnt++]);
			File dir = new File(args[cnt++]).getCanonicalFile();
			File aFile = new File(args[cnt++]).getCanonicalFile();
			File dFile = new File(args[cnt++]).getCanonicalFile();
			File initial = null;
			if (cnt < args.length)
				initial = new File(args[cnt++]).getAbsoluteFile();

			int wallMins = annealMins+60;

			String name = dir.getName();

			BatchScriptWriter batch = new USC_HPCC_ScriptWriter();
			File mpjHome = USC_HPCC_ScriptWriter.MPJ_HOME;
			File javaBin = USC_HPCC_ScriptWriter.JAVA_BIN;

			CoolingScheduleType cool = CoolingScheduleType.FAST_SA;
			CompletionCriteria dsaCriteria = TimeCompletionCriteria.getInMinutes(annealMins);
			boolean useMXDev = false;
			int heapSizeMB = 2048;
			String queue = null;

			ArrayList<File> classpath = MultiSABenchmarkPBSWriter.getClasspath();

			MPJShellScriptWriter mpjWriter = new MPJShellScriptWriter(javaBin, heapSizeMB, classpath, mpjHome, useMXDev);

			DistributedScriptCreator dsa_create = new DistributedScriptCreator(mpjWriter, aFile, dFile,
					initial, numThreads, null, dsaCriteria, subCompletion, mpjHome, false);

			dsa_create.setProgFile(new File(dir, name+".csv"));
			dsa_create.setSolFile(new File(dir, name+".bin"));
			dsa_create.setCool(cool);

			File pbs = new File(dir, name+".pbs");
			System.out.println("Writing: "+pbs.getName());

			batch.writeScript(pbs, dsa_create.buildScript(), wallMins, nodes, ppn, queue);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}

		System.exit(0);
	}

}
