package scratch.kevin.ucerf3.etas;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.opensha.commons.hpc.mpj.FastMPJShellScriptWriter;
import org.opensha.commons.hpc.pbs.BatchScriptWriter;
import org.opensha.commons.hpc.pbs.StampedeScriptWriter;
import org.opensha.commons.hpc.pbs.USC_HPCC_ScriptWriter;

import com.google.common.collect.Lists;

public class MPJ_ETAS_SimulatorScriptGen {
	
	private static enum Scenarios {
		SPONTANEOUS,
		MOJAVE_7,
		LA_HABRA,
		NAPA
	}

	public static void main(String[] args) throws IOException {
		File localDir = new File("/home/kevin/OpenSHA/UCERF3/etas/simulations");
		
		boolean stampede = false;
		
//		Scenarios scenario = Scenarios.LA_HABRA;
//		Scenarios[] scenarios = Scenarios.values();
//		Scenarios[] scenarios = {Scenarios.MOJAVE_7};
//		Scenarios[] scenarios = {Scenarios.NAPA};
		Scenarios[] scenarios = {Scenarios.SPONTANEOUS};
		boolean timeIndep = false;
		int numSims = 5000;
		
		int memGigs;
		int mins = 24*60;
		int nodes = 40;
		int ppn;
		if (stampede)
			ppn = 16;
		else
			ppn = 8;
		String queue = null;
		
		File remoteDir, remoteSolFile;
		FastMPJShellScriptWriter mpjWrite;
		BatchScriptWriter pbsWrite;
		
		if (stampede) {
			memGigs = 26;
			remoteDir = new File("/work/00950/kevinm/ucerf3/etas_sim");
			remoteSolFile = new File("/work/00950/kevinm/ucerf3/inversion/compound_plots/2013_05_10-ucerf3p3-production-10runs/"
					+ "2013_05_10-ucerf3p3-production-10runs_COMPOUND_SOL_FM3_1_SpatSeisU3_MEAN_BRANCH_AVG_SOL.zip");
			mpjWrite = new FastMPJShellScriptWriter(StampedeScriptWriter.JAVA_BIN, memGigs*1024,
					null, StampedeScriptWriter.FMPJ_HOME, false);
			pbsWrite = new StampedeScriptWriter();
		} else {
			memGigs = 9;
			remoteDir = new File("/home/scec-02/kmilner/ucerf3/etas_sim");
			remoteSolFile = new File("/home/scec-02/kmilner/ucerf3/inversion_compound_plots/"
					+ "2013_05_10-ucerf3p3-production-10runs/"
					+ "2013_05_10-ucerf3p3-production-10runs_COMPOUND_SOL_FM3_1_SpatSeisU3_MEAN_BRANCH_AVG_SOL.zip");
			mpjWrite = new FastMPJShellScriptWriter(USC_HPCC_ScriptWriter.JAVA_BIN, memGigs*1024,
					null, USC_HPCC_ScriptWriter.FMPJ_HOME, false);
			pbsWrite = new USC_HPCC_ScriptWriter();
		}
		
		mpjWrite.setAutoMemDetect(true);
		
		List<File> classpath = new ArrayList<File>();
		classpath.add(new File(remoteDir, "commons-cli-1.2.jar"));
		
		for (Scenarios scenario : scenarios) {
			String jobName = new SimpleDateFormat("yyyy_MM_dd").format(new Date())+"-"+scenario.name().toLowerCase();
			if (timeIndep)
				jobName += "-indep";
			
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
			case LA_HABRA:
				argz += " --trigger-loc 33.932,-117.917,4.8 --trigger-mag 6.2";
				break;
			case MOJAVE_7:
				argz += " --trigger-rupture-id 197792";
				break;
			case NAPA:
				argz += " --trigger-rupture-id 93902 --trigger-mag 6.0";
				break;
			case SPONTANEOUS:
				// do nothing
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
