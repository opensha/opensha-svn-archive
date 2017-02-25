package scratch.kevin.ucerf3.etas;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.opensha.commons.hpc.JavaShellScriptWriter;
import org.opensha.commons.hpc.mpj.FastMPJShellScriptWriter;
import org.opensha.commons.hpc.mpj.MPJExpressShellScriptWriter;
import org.opensha.commons.hpc.pbs.BatchScriptWriter;
import org.opensha.commons.hpc.pbs.StampedeScriptWriter;
import org.opensha.commons.hpc.pbs.USC_HPCC_ScriptWriter;

import com.google.common.base.Preconditions;

public class MPJ_ETAS_HazardMapCalcScriptGen {
	
	public static final DateFormat df = new SimpleDateFormat("yyyy_MM_dd");

	public static void main(String[] args) throws IOException {
		File localMainDir = new File("/home/kevin/OpenSHA/UCERF3/etas/hazard");
		File remoteMainDir = new File("/home/scec-02/kmilner/ucerf3/etas_hazard");
		
		File localShakemapDir = new File("/home/kevin/OpenSHA/UCERF3/shakemap_precalc");
		File remoteShakemapDir = new File("/home/scec-02/kmilner/ucerf3/shakemap_precalc");
		
		File localETASDir = new File("/home/kevin/OpenSHA/UCERF3/etas/simulations/");
		File remoteETASDir = new File("/home/scec-00/kmilner/ucerf3_etas_results_stampede/");
		
		String etasSimName = "2016_02_19-mojave_m7-10yr-full_td-subSeisSupraNucl-gridSeisCorr-scale1.14-combined100k";
		String etasFileName = "results_descendents_m5_preserve.bin";
		String etasShortName = "mojave_m7_fulltd_descendents";
		File remoteEtasCatalogFile = new File(new File(remoteETASDir, etasSimName), etasFileName);
		
		String shakemapRunName = "2017_02_23-NGAWest_2014_NoIdr-spacing0.05-site-effects-with-basin";
		String shakemapShortName = "NGA2-0.05-site-effects-with-basin";
		File remoteShakemapRunDir = new File(remoteShakemapDir, shakemapRunName);
		File sitesFile = new File(remoteShakemapRunDir, "sites.xml");
		String gmpeFileName = "NGAWest_2014_NoIdr.xml";
		File gmpeFile = new File(remoteShakemapRunDir, gmpeFileName);
		double spacing = 0.05;
		String[] imts = { "pgv", "pga" };
		double[] periods = { Double.NaN, Double.NaN };
		
		double griddedSpacing = 0.01;
		
		String dateStr = df.format(new Date());
		String jobName = dateStr+"-"+etasShortName+"-"+shakemapShortName;
		
		int threads = 20;
		String queue = "scec";
		
		int nodes = 34;
		int hours = 24;
		
		File localDir = new File(localMainDir, jobName);
		Preconditions.checkState(localDir.exists() || localDir.mkdir());
		
		System.out.println("Job name: "+jobName);
		System.out.println("Dir: "+localDir.getAbsolutePath());
		
		JavaShellScriptWriter mpjWrite;
		BatchScriptWriter pbsWrite;
		
		boolean stampede = false;
		int memGigs;
		int ppn;
		if (stampede) {
			memGigs = 26;
			ppn = 16;
			mpjWrite = new FastMPJShellScriptWriter(StampedeScriptWriter.JAVA_BIN, memGigs*1024,
					null, StampedeScriptWriter.FMPJ_HOME);
			((FastMPJShellScriptWriter)mpjWrite).setUseLaunchWrapper(true);
			pbsWrite = new StampedeScriptWriter();
		} else {
			if (queue == null) {
				memGigs = 9;
				ppn = 8;
			} else {
				memGigs = 60;
				ppn = 20;
			}
			boolean fmpj = nodes < 25;
			fmpj = false;
			if (fmpj) {
				mpjWrite = new FastMPJShellScriptWriter(USC_HPCC_ScriptWriter.JAVA_BIN, memGigs*1024,
						null, USC_HPCC_ScriptWriter.FMPJ_HOME);
				((FastMPJShellScriptWriter)mpjWrite).setUseLaunchWrapper(true);
			} else {
				mpjWrite = new MPJExpressShellScriptWriter(USC_HPCC_ScriptWriter.JAVA_BIN, memGigs*1024,
						null, USC_HPCC_ScriptWriter.MPJ_HOME);
			}
			pbsWrite = new USC_HPCC_ScriptWriter();
		}
		File remoteJobDir = new File(remoteMainDir, jobName);
		
		List<File> classpath = new ArrayList<File>();
		classpath.add(new File(remoteMainDir, "commons-cli-1.2.jar"));
		classpath.add(new File(remoteJobDir, "OpenSHA_complete.jar"));
		mpjWrite.setClasspath(classpath);
		
//		--catalogs /home/kevin/OpenSHA/UCERF3/etas/simulations/2016_02_19-mojave_m7-10yr-full_td-subSeisSupraNucl-gridSeisCorr-scale1.14-combined100k/results_descendents_m5_preserve.bin
//		--fault-data-file /home/kevin/OpenSHA/UCERF3/shakemap_precalc/2017_02_21-NGAWest_2014_NoIdr-spacing1.0-no-site-effects/results_sa_1.0s.bin
//		--spacing 1
//		--gmpe-file /home/kevin/OpenSHA/UCERF3/shakemap_precalc/2017_02_21-NGAWest_2014_NoIdr-spacing1.0-no-site-effects/NGAWest_2014_NoIdr.xml
//		--gridded-spacing 0.01
//		--imt SA
//		--period 1
//		--output-dir /tmp
//		--sites-file /home/kevin/OpenSHA/UCERF3/shakemap_precalc/2017_02_21-NGAWest_2014_NoIdr-spacing1.0-no-site-effects/sites.xml
//		--threads 4
		
		for (int i=0; i<imts.length; i++) {
			String imt = imts[i];
			double period = periods[i];
			String imtName = imt;
			if (!Double.isNaN(period) && period > 0)
				imtName += "_"+(float)period+"s";
			File remoteShakemapFile = new File(remoteShakemapRunDir, "results_"+imtName+".bin");
			
			String argz;
			if (threads > 0)
				argz = "--threads "+threads;
			else
				argz = "";
			argz += " --catalogs "+remoteEtasCatalogFile.getAbsolutePath();
			argz += " --fault-data-file "+remoteShakemapFile.getAbsolutePath();
			argz += " --spacing "+(float)spacing;
			argz += " --gmpe-file "+gmpeFile.getAbsolutePath();
			argz += " --sites-file "+sitesFile.getAbsolutePath();
			argz += " --imt "+imt;
			if (!Double.isNaN(period) && period > 0)
				argz += " --period"+(float)period;
			argz += " --gridded-spacing "+(float)griddedSpacing;
			argz += " --output-dir "+remoteJobDir.getAbsolutePath();
			
			List<String> script = mpjWrite.buildScript(MPJ_ETAS_HazardMapCalc.class.getName(), argz);
			
			int mins = hours*60;
			script = pbsWrite.buildScript(script, mins, nodes, ppn, queue);
			pbsWrite.writeScript(new File(localDir, imt+".pbs"), script);
		}
	}

}
