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
					+" <nodes> <minutes> <sub-completion> <PPN> <threads> <dir>"
					+" [<A mat file> <d file> [<intial>] OR --zip <zip file> [jobName]]");
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
				if (cnt < args.length)
					initial = new File(args[cnt++]).getAbsoluteFile();
			}
			
			int wallMins = annealMins+60;

			BatchScriptWriter batch = new USC_HPCC_ScriptWriter();
			File mpjHome = USC_HPCC_ScriptWriter.MPJ_HOME;
			File javaBin = USC_HPCC_ScriptWriter.JAVA_BIN;

			CoolingScheduleType cool = CoolingScheduleType.FAST_SA;
			CompletionCriteria dsaCriteria = TimeCompletionCriteria.getInMinutes(annealMins);
			boolean useMXDev = false;
//			int heapSizeMB = 2048;
			int heapSizeMB = 8000;
			String queue = null;

			ArrayList<File> classpath = MultiSABenchmarkPBSWriter.getClasspath();

			MPJShellScriptWriter mpjWriter = new MPJShellScriptWriter(javaBin, heapSizeMB, classpath, mpjHome, useMXDev);

			DistributedScriptCreator dsa_create = new DistributedScriptCreator(mpjWriter,numThreads, null, dsaCriteria,
					subCompletion, mpjHome, false);
			dsa_create.setaMat(aFile);
			dsa_create.setdMat(dFile);
			dsa_create.setInitial(initial);
			dsa_create.setZipFile(zipFile);

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
