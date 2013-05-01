package scratch.peter.ucerf3.scripts;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.opensha.commons.hpc.mpj.FastMPJShellScriptWriter;
import org.opensha.commons.hpc.pbs.BatchScriptWriter;
import org.opensha.commons.util.ClassUtils;

import scratch.peter.ucerf3.calc.UC3_CalcMPJ_CurveCompound;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.io.Files;

public class CurvesFromCompound {

	private static final String NEWLINE = IOUtils.LINE_SEPARATOR;
	private static final Joiner J_NL = Joiner.on(NEWLINE);
	private static final Joiner J_SPACE = Joiner.on(" ");
	private static final File MPJ_HOME;
	private static final File JAVA_BIN;

	static {
		MPJ_HOME = new File("/home/scec-00/pmpowers/lib/FastMPJ");
		JAVA_BIN = new File("/usr/usc/jdk/default/jre/bin/java");
	}

	public static void main(String[] args) throws IOException {
		if (args.length != 10) {
			System.out
				.println("USAGE: " +
					ClassUtils.getClassNameWithoutPackage(CurvesFromCompound.class) +
					" <queue> <nodes> <hours> <libDir> <scriptPath>" +
					" <solfile> <sitefile> <branchfile> <periods> <outDir>");
			System.exit(1);
		}

		String queue = args[0];
		int nodes = Integer.parseInt(args[1]);
		int hours = Integer.parseInt(args[2]);
		String libDir = args[3];
		String scriptPath = args[4];
		
		Iterable<String> otherArgs = Iterables.skip(Arrays.asList(args), 5);
		String scriptArgs = J_SPACE.join(otherArgs);

		writeScript(libDir, hours, nodes, queue, scriptPath, scriptArgs);
	}

	private static void writeScript(String libDir, int hrs,
			int nodes, String queue, String scriptPath, String args) {
		try {
			File shaJAR = new File(libDir, "OpenSHA_complete.jar");
			File cliJAR = new File(libDir, "commons-cli-1.2.jar");
			ArrayList<File> classpath = Lists.newArrayList(shaJAR, cliJAR);
			FastMPJShellScriptWriter mpj = new FastMPJShellScriptWriter(
				JAVA_BIN, 4096, classpath, MPJ_HOME, false);

			List<String> script = mpj.buildScript(
				UC3_CalcMPJ_CurveCompound.class.getName(), args);
			script.add(NEWLINE);
			HPCC_ScriptWriter writer = new HPCC_ScriptWriter();
			script = writer.buildScript(script, hrs, nodes, 0, queue);

			File pbsFile = new File(scriptPath);
			String scriptStr = J_NL.join(script);
			Files.write(scriptStr, pbsFile, Charsets.US_ASCII);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	private static class HPCC_ScriptWriter extends BatchScriptWriter {
		
		String nodesAddition;
		
		HPCC_ScriptWriter() {
			this(null);
		}
		
		HPCC_ScriptWriter(String nodesAddition) {
			this.nodesAddition = nodesAddition;
		}

		@Override
		public List<String> getBatchHeader(int hours, int nodes,
				int ppn, String queue) {
			ArrayList<String> pbs = new ArrayList<String>();
			
			if (queue != null && !queue.isEmpty())
				pbs.add("#PBS -q "+queue);
			String dashL = "#PBS -l walltime="+hours+":00:00,nodes="+nodes;
			if (nodesAddition != null && !nodesAddition.isEmpty())
				dashL += ":"+nodesAddition;
			if (ppn > 0)
				dashL += ":ppn="+ppn;
			pbs.add(dashL);
			pbs.add("#PBS -V");
			pbs.add("");
			pbs.add("NEW_NODEFILE=\"/tmp/${USER}-hostfile-${PBS_JOBID}\"");
			pbs.add("echo \"creating PBS_NODEFILE: $NEW_NODEFILE\"");
			pbs.add("cat $PBS_NODEFILE | sort | uniq > $NEW_NODEFILE");
			pbs.add("export PBS_NODEFILE=$NEW_NODEFILE");
			pbs.add("");
			
			return pbs;
		}

	}

}
