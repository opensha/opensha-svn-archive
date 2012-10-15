package org.opensha.nshmp2.calc;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.opensha.commons.hpc.JavaShellScriptWriter;
import org.opensha.commons.hpc.pbs.BatchScriptWriter;
import org.opensha.commons.util.ClassUtils;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.io.Files;

public class ScriptGenUC3 {

	private static final String NEWLINE = IOUtils.LINE_SEPARATOR;
	private static final Joiner J = Joiner.on(NEWLINE);
	private static final Splitter S = Splitter.on(',');
	private static final File JAVA_BIN;

	static {
		JAVA_BIN = new File("/usr/usc/jdk/default/jre/bin/java");
	}

	/**
	 * @param args
	 * @throws IOException java -cp
	 *         $JAVA_LIB/OpenSHA_complete.jar:$JAVA_LIB/commons-cli-1.2.jar
	 *         org.opensha.nshmp2.calc.ScriptGen $NAME $GRIDS $PERIODS $ERFID
	 *         $OUTDIR $EPI $HRS $NODES $QUEUE
	 */
	public static void main(String[] args) throws IOException {
		if (args.length != 3) {
			System.out
				.println("USAGE: " +
					ClassUtils.getClassNameWithoutPackage(ScriptGenUC3.class) +
					" <filepath> <erfIndex> <outDir>");
			System.exit(1);
		}

		String filepath = args[0];
		int erfIndex = Integer.parseInt(args[1]);
		System.out.println(erfIndex + " " + filepath);
		String outDir = args[2];
		System.out.println(outDir);
		
		int hours = 1;
		int nodes = 1;
		String queue = "nbns";

		writeScript(filepath, outDir, erfIndex, hours, nodes, queue);
	}

	private static void writeScript(String filepath, String outDir, int erfIdx,
			int hrs, int nodes, String queue) {
		try {
			File hazDatDir = new File(outDir);
			File libDir = new File(hazDatDir, "lib");
			File shaJAR = new File(libDir, "OpenSHA_complete.jar");
			File cliJAR = new File(libDir, "commons-cli-1.2.jar");
			ArrayList<File> classpath = Lists.newArrayList(shaJAR, cliJAR);
			JavaShellScriptWriter jssw = new JavaShellScriptWriter(JAVA_BIN, 5120,
				classpath);

			String cliArgs = filepath + " " + erfIdx + " " + outDir;
			List<String> script = jssw.buildScript(
				UC3_CalcDriver.class.getName(), cliArgs);
			script.add(NEWLINE);
			HPCC_ScriptWriter writer = new HPCC_ScriptWriter();
			script = writer.buildScript(script, hrs, nodes, 0, queue);

			String pbsName = "UC3job" + ".pbs";
			File pbsFile = new File(pbsName);
			String scriptStr = J.join(script);
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
