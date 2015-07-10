package scratch.kevin.cybershake.etasCalcs;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.opensha.commons.geo.Location;
import org.opensha.commons.hpc.mpj.FastMPJShellScriptWriter;
import org.opensha.commons.hpc.pbs.BatchScriptWriter;
import org.opensha.commons.hpc.pbs.StampedeScriptWriter;
import org.opensha.commons.hpc.pbs.USC_HPCC_ScriptWriter;
import org.opensha.sha.cybershake.etas.ETASModProbConfig.ETAS_CyberShake_Scenarios;

import scratch.UCERF3.enumTreeBranches.FaultModels;
import scratch.kevin.ucerf3.etas.MPJ_ETAS_Simulator;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

public class ScriptGen {

	public static void main(String[] args) throws IOException {
		File localDir = new File("/home/kevin/OpenSHA/UCERF3/cybershake_etas/sims");
		
		String cacheDirName = "cache_u3rups_u2mapped";
		FaultModels fm = FaultModels.FM3_1;
		String solTypeStr = "u2mapped";
		String fssName = "ucerf2_mapped_sol.zip";
//		String cacheDirName = "cache_u2rups_u3inverted";
//		FaultModels fm = FaultModels.FM2_1;
//		String solTypeStr = "u3inverted";
//		String fssName = "ucerf2_u3inverted_sol.zip";
		
//		String dateStr = new SimpleDateFormat("yyyy_MM_dd").format(new Date());
//		String dateStr = "2015_03_23";
//		String dateStr = "2015_04_09";
		String dateStr = "2015_06_15";
		
//		Scenarios scenario = Scenarios.LA_HABRA;
//		Scenarios[] scenarios = Scenarios.values();
//		Scenarios[] scenarios = {Scenarios.BOMBAY_BEACH};
//		Scenarios[] scenarios = {Scenarios.BOMBAY_BEACH_M6};
//		Scenarios[] scenarios = {Scenarios.PARKFIELD};
		ETAS_CyberShake_Scenarios[] scenarios = {
//				ETAS_CyberShake_Scenarios.BOMBAY_BEACH_BRAWLEY_FAULT_M6,
//				ETAS_CyberShake_Scenarios.PARKFIELD,
//				ETAS_CyberShake_Scenarios.MOJAVE_S_POINT_M6};
//				ETAS_CyberShake_Scenarios.PARKFIELD};
//				ETAS_CyberShake_Scenarios.BOMBAY_BEACH_M6,
//				ETAS_CyberShake_Scenarios.PARKFIELD,
				ETAS_CyberShake_Scenarios.MOJAVE_S_POINT_M6};
		boolean timeIndep = false;
		int numSims = 50000;
		String nameAdd = "-round2";
//		String nameAdd = "-nospont-round6";
//		String nameAdd = "-round5";
		
		File remoteDir, remoteSolFile;
		FastMPJShellScriptWriter mpjWrite;
		BatchScriptWriter pbsWrite;
		
//		int memGigs = 10;
//		int perNodeMemGB = 32;
//		int mins = 24*60;
//		int nodes = 99;
//		int ppn = 8;
//		String queue = null;
//		int threads = 1;
//		remoteDir = new File("/home/scec-02/kmilner/ucerf3/etas_sim/cybershake");
//		remoteSolFile = new File(remoteDir, fssName);
//		mpjWrite = new FastMPJShellScriptWriter(USC_HPCC_ScriptWriter.JAVA_BIN, memGigs*1024,
//				null, USC_HPCC_ScriptWriter.FMPJ_HOME, false);
//		pbsWrite = new USC_HPCC_ScriptWriter();
//		((USC_HPCC_ScriptWriter)pbsWrite).setPerNodeMemGB(perNodeMemGB);
		
		int memGigs = 25;
		int mins = 12*60;
		int nodes = 128;
		int ppn = 16;
		String queue = null;
		int threads = 8;
		remoteDir = new File("/work/00950/kevinm/ucerf3/etas_sim/cybershake");
		remoteSolFile = new File(remoteDir, fssName);
		mpjWrite = new FastMPJShellScriptWriter(StampedeScriptWriter.JAVA_BIN, memGigs*1024,
				null, StampedeScriptWriter.FMPJ_HOME, false);
		pbsWrite = new StampedeScriptWriter();
		
		List<File> classpath = new ArrayList<File>();
		classpath.add(new File(remoteDir.getParentFile(), "commons-cli-1.2.jar"));
		
		for (ETAS_CyberShake_Scenarios scenario : scenarios) {
			String jobName = dateStr+"-"+solTypeStr+"-"+scenario.name().toLowerCase();
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
			
			String argz = "--min-dispatch 1 --max-dispatch "+threads+" --threads "+threads+" --no-spontaneous"
					+" --num "+numSims+" --sol-file "+remoteSolFile.getAbsolutePath();
//			case BOMBAY_BEACH_CAT:
//				argz += " --trigger-catalog "+(new File(remoteDir, "bombay_catalog.txt")).getAbsolutePath();
//				break;
//			case BOMBAY_BEACH_SINGLE:
//				argz += " --trigger-loc 33.31833333333334,-115.72833333333335,5.8 --trigger-mag 4.8";
//				break;
			Preconditions.checkState(scenario.getTriggerRupIndex(fm) >= 0
					|| (scenario.getTriggerLoc() != null && scenario.getTriggerMag() > 0d));
			if (scenario.getTriggerRupIndex(fm) >= 0) {
				argz += " --trigger-rupture-id "+scenario.getTriggerRupIndex(fm);
			} else {
				Location loc = scenario.getTriggerLoc();
				argz += " --trigger-loc "+loc.getLatitude()+","+loc.getLongitude()+","+loc.getDepth();
			}
			if (scenario.getTriggerMag() > 0d)
				argz += " --trigger-mag "+scenario.getTriggerMag();
			if (timeIndep)
				argz += " --indep";
			argz += " "+new File(remoteDir, cacheDirName).getAbsolutePath()+" "+remoteJobDir.getAbsolutePath();
			
			List<String> script = mpjWrite.buildScript(MPJ_ETAS_Simulator.class.getName(), argz);
			
			script = pbsWrite.buildScript(script, mins, nodes, ppn, queue);
			pbsWrite.writeScript(pbsFile, script);
		}
	}

}
