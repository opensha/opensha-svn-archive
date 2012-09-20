package org.opensha.nshmp2.calc;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.opensha.commons.hpc.JavaShellScriptWriter;
import org.opensha.commons.hpc.mpj.MPJShellScriptWriter;
import org.opensha.commons.hpc.pbs.USC_HPCC_ScriptWriter;
import org.opensha.commons.util.ClassUtils;

public class ScriptGen {

	static final File MPJ_HOME = new File("/home/rcf-40/pmpowers/mpj-v0_38");
	static final String LOCAL_OUT_DIR = "/Users/pmpowers/Documents/OpenSHA/NSHMPdev2";
	static final String REMOTE_OUT_DIR = "/home/scec-00/pmpowers/hazmaps";
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		if (args.length != 4) {
			System.out.println("USAGE: "+ClassUtils.getClassNameWithoutPackage(ScriptGen.class)
					+" <jobName> <minutes> <nodes> <queue>");
//			System.exit(2);
			args = new String[] {"pmp-test", "20", "2", "nbns"};
		}
		
		String jobName = args[0];
		int mins = Integer.parseInt(args[1]);
		int nodes = Integer.parseInt(args[2]);
		String queue = args[3];
		if (queue.toLowerCase().equals("null"))
			queue = null;
		
		File mapDir = new File(REMOTE_OUT_DIR);
		File configFile = new File(mapDir, "calc.properties");
		
		File jobDir = new File(mapDir, jobName);
		if (!jobDir.exists()) jobDir.mkdir();

		File javaBin = USC_HPCC_ScriptWriter.JAVA_BIN;
		File libDir = new File(mapDir, "lib");
		File jarFile = new File(libDir, "OpenSHA_complete.jar");
		
		ArrayList<File> classpath = new ArrayList<File>();
		classpath.add(jarFile);
		classpath.add(new File(libDir, "commons-cli-1.2.jar"));
		
		MPJShellScriptWriter mpj = new MPJShellScriptWriter(javaBin, 4096,
			classpath, MPJ_HOME, false);		
		
		String cliArgs = configFile.getAbsolutePath();
		
		List<String> script = mpj.buildScript(HazardCalcDriverMPJ.class.getName(), cliArgs);
		USC_HPCC_ScriptWriter writer = new USC_HPCC_ScriptWriter();
			
		script = writer.buildScript(script, mins, nodes, 0, queue);
		
		File pbsFile = new File(LOCAL_OUT_DIR, "map_job.pbs");
		JavaShellScriptWriter.writeScript(pbsFile, script);
	}

}
