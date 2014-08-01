package scratch.kevin.cybershake.etasCalcs;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.opensha.commons.hpc.mpj.FastMPJShellScriptWriter;
import org.opensha.commons.hpc.pbs.BatchScriptWriter;
import org.opensha.commons.hpc.pbs.USC_HPCC_ScriptWriter;

import scratch.kevin.ucerf3.etas.MPJ_ETAS_Simulator;

import com.google.common.collect.Lists;

public class ScriptGen {

	private static enum Scenarios {
		BOMBAY_BEACH_CAT,
		BOMBAY_BEACH_SINGLE,
		BOMBAY_BEACH_M6,
		PARKFIELD
	}

	public static void main(String[] args) throws IOException {
		File localDir = new File("/home/kevin/OpenSHA/UCERF3/cybershake_etas/sims");
		
//		Scenarios scenario = Scenarios.LA_HABRA;
//		Scenarios[] scenarios = Scenarios.values();
//		Scenarios[] scenarios = {Scenarios.BOMBAY_BEACH};
//		Scenarios[] scenarios = {Scenarios.BOMBAY_BEACH_M6};
		Scenarios[] scenarios = {Scenarios.PARKFIELD};
		boolean timeIndep = false;
		int numSims = 10000;
		String nameAdd = null;
		
		int memGigs;
		int mins = 24*60;
		int nodes = 40;
		int ppn = 8;
		String queue = null;
		
		File remoteDir, remoteSolFile;
		FastMPJShellScriptWriter mpjWrite;
		BatchScriptWriter pbsWrite;
		
		memGigs = 9;
		remoteDir = new File("/home/scec-02/kmilner/ucerf3/etas_sim/cybershake");
		remoteSolFile = new File(remoteDir, "ucerf2_mapped_sol.zip");
		mpjWrite = new FastMPJShellScriptWriter(USC_HPCC_ScriptWriter.JAVA_BIN, memGigs*1024,
				null, USC_HPCC_ScriptWriter.FMPJ_HOME, false);
		pbsWrite = new USC_HPCC_ScriptWriter();
		
		List<File> classpath = new ArrayList<File>();
		classpath.add(new File(remoteDir.getParentFile(), "commons-cli-1.2.jar"));
		
		for (Scenarios scenario : scenarios) {
			String jobName = new SimpleDateFormat("yyyy_MM_dd").format(new Date())+"-"+scenario.name().toLowerCase();
			if (timeIndep)
				jobName += "-indep";
			if (nameAdd != null)
				jobName += nameAdd;
			
			File localJobDir = new File(localDir, jobName);
			if (!localJobDir.exists())
				localJobDir.mkdir();
			File remoteJobDir = new File(remoteDir, jobName);
			
			List<File> subClasspath = Lists.newArrayList(classpath);
			subClasspath.add(new File(remoteJobDir, "OpenSHA_complete.jar"));
			mpjWrite.setClasspath(subClasspath);
			
			File pbsFile = new File(localJobDir, jobName+".pbs");
			
			String argz = "--min-dispatch 1 --max-dispatch 1 --num "+numSims+" --sol-file "+remoteSolFile.getAbsolutePath();
			switch (scenario) {
			case BOMBAY_BEACH_CAT:
				argz += " --trigger-catalog "+(new File(remoteDir, "bombay_catalog.txt")).getAbsolutePath();
				break;
			case BOMBAY_BEACH_SINGLE:
				argz += " --trigger-loc 33.31833333333334,-115.72833333333335,5.8 --trigger-mag 4.8";
				break;
			case BOMBAY_BEACH_M6:
				argz += " --trigger-loc 33.31833333333334,-115.72833333333335,5.8 --trigger-mag 6.0";
				break;
			case PARKFIELD:
				argz += " --trigger-rupture-id 30473";
				break;

			default:
				throw new IllegalStateException("unknown scenario: "+scenario);
			}
			if (timeIndep)
				argz += " --indep";
			argz += " "+remoteDir.getAbsolutePath()+" "+remoteJobDir.getAbsolutePath();
			
			List<String> script = mpjWrite.buildScript(MPJ_ETAS_Simulator.class.getName(), argz);
			
			script = pbsWrite.buildScript(script, mins, nodes, ppn, queue);
			pbsWrite.writeScript(pbsFile, script);
		}
	}

}
