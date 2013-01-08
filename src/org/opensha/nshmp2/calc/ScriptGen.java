package org.opensha.nshmp2.calc;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.opensha.commons.hpc.JavaShellScriptWriter;
import org.opensha.commons.hpc.mpj.MPJExpressShellScriptWriter;
import org.opensha.commons.hpc.pbs.BatchScriptWriter;
import org.opensha.commons.hpc.pbs.USC_HPCC_ScriptWriter;
import org.opensha.commons.util.ClassUtils;
import org.opensha.nshmp2.tmp.TestGrid;
import org.opensha.nshmp2.util.Period;

import cern.colt.Arrays;

import com.google.common.base.Charsets;
import com.google.common.base.Enums;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.io.Closeables;
import com.google.common.io.Files;
import com.google.common.io.Flushables;
import com.google.common.io.OutputSupplier;

public class ScriptGen {

	private static final String NEWLINE = IOUtils.LINE_SEPARATOR;
	private static final Joiner J = Joiner.on(NEWLINE);
	private static final Splitter S = Splitter.on(',');
	private static final File MPJ_HOME;
	private static final File JAVA_BIN;

	static {
		MPJ_HOME = new File("/home/rcf-40/pmpowers/mpj-v0_38");
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
		if (args.length < 9) {
			System.out
				.println("USAGE: " +
					ClassUtils.getClassNameWithoutPackage(ScriptGen.class) +
					" <name> <grids> <spacing ><periods> <erfIDs> <libDir> <outDir> <epi> <hours> <nodes> <queue> [<branchID>]");
			// branchID is only read if erfID is UCERF3_BRANCH
			System.exit(1);
		}

		String name = args[0];
		System.out.println(name);
		
		String grids = args[1];
		List<TestGrid> gridList = readArgAsList(grids, TestGrid.class);
		System.out.println(gridList);
		
		String spacing = args[2];
		double spacingVal = Double.parseDouble(spacing);
		System.out.println(spacing);

		String periods = args[3];
		List<Period> periodList = readArgAsList(periods, Period.class);
		System.out.println(periodList);
		
		String erfID = args[4];
		System.out.println(erfID);
		
		String libDir = args[5];
		System.out.println(libDir);

		String outDir = args[6];
		System.out.println(outDir);
		
		boolean epi = Boolean.parseBoolean(args[7]);
		System.out.println(epi);
		
		int hours = Integer.parseInt(args[8]);
		System.out.println(hours);
		
		int nodes = Integer.parseInt(args[9]);
		System.out.println(nodes);

		String queue = args[10];
		System.out.println(queue);
		
		String branch = null;
		if (ERF_ID.valueOf(erfID).equals(ERF_ID.UCERF3_BRANCH)) {
			branch = args[11];
			System.out.println(branch);
		}

		for (TestGrid grid : gridList) {
			for (Period period : periodList) {
				File props = writeProps(outDir, name, grid, spacingVal, period, erfID, epi, branch);
				writeScript(libDir, props, hours, nodes, queue);
			}
		}
		// List<String> script =
		// mpj.buildScript(HazardCalcDriverMPJ.class.getName(), cliArgs);
		// USC_HPCC_ScriptWriter writer = new USC_HPCC_ScriptWriter();
		//
		// script = writer.buildScript(script, mins, nodes, 0, queue);
		//
		// File pbsFile = new File(LOCAL_OUT_DIR, "map_job.pbs");
		// .writeScript(pbsFile, script);
		//
	}

	private static <T extends Enum<T>> List<T> readArgAsList(String arg,
			Class<T> clazz) {
		Iterable<T> it = Iterables.transform(S.split(arg),
			Enums.valueOfFunction(clazz));
		return Lists.newArrayList(it);
	}
	
	private static File writeProps(String outDir, String name, TestGrid grid, double spacing,
			Period period, String erfID, boolean epi, String branch) {
		File pFile = null;
		try {
			String freq = period.equals(Period.GM0P00) ? "pga" : period
				.equals(Period.GM1P00) ? "1hz" : "5hz";
			String epiStr = (epi) ? "_epi" : "";
			String spacStr = createSpacingID(spacing);
			String pFileName = name + epiStr + "_" + spacStr + "-" + freq + ".props";
			pFile = new File(pFileName);

			Properties props = new Properties();
			props.setProperty("name", name + epiStr + "_" + spacStr);
			props.setProperty("grid", grid.name());
			props.setProperty("spacing", Double.toString(spacing));
			props.setProperty("period", period.name());
			props.setProperty("erfID", erfID);
			props.setProperty("epiUnc", Boolean.toString(epi));
			props.setProperty("outDir", outDir);
			props.setProperty("singleFile", "false"); // ignored in MPJ clacs
			if (branch != null) props.setProperty("UC3branch",branch);

			String comment = "# hpc calculation configuration";
			BufferedWriter writer = Files.newWriter(pFile, Charsets.US_ASCII);
			props.store(writer, comment);
			Flushables.flushQuietly(writer);
			Closeables.closeQuietly(writer);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		return pFile;
	}

	private static void writeScript(String libDir, File config, int hrs,
			int nodes, String queue) {
		try {
			File shaJAR = new File(libDir, "OpenSHA_complete.jar");
			File cliJAR = new File(libDir, "commons-cli-1.2.jar");
			ArrayList<File> classpath = Lists.newArrayList(shaJAR, cliJAR);
			MPJExpressShellScriptWriter mpj = new MPJExpressShellScriptWriter(JAVA_BIN, 9216,
				classpath, MPJ_HOME, false);

			String cliArgs = config.getAbsolutePath();
			List<String> script = mpj.buildScript(
				HazardCalcDriverMPJ.class.getName(), cliArgs);
			script.add(NEWLINE);
			HPCC_ScriptWriter writer = new HPCC_ScriptWriter();
			script = writer.buildScript(script, hrs, nodes, 0, queue);

			String pbsName = StringUtils.substringBeforeLast(config.getName(),
				".") + ".pbs";
			File pbsFile = new File(pbsName);
			String scriptStr = J.join(script);
			Files.write(scriptStr, pbsFile, Charsets.US_ASCII);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	private static String createSpacingID(double spacing) {
		String str = Double.toString(spacing);
		int pIdx = str.indexOf('.');
		return str.substring(0,pIdx) + "p" + str.substring(pIdx+1);
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
