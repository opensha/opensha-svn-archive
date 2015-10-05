package org.opensha.sha.cybershake.calc.mcer;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.opensha.commons.hpc.mpj.FastMPJShellScriptWriter;
import org.opensha.commons.hpc.pbs.BatchScriptWriter;
import org.opensha.commons.hpc.pbs.StampedeScriptWriter;
import org.opensha.sha.imr.param.OtherParams.Component;

import com.google.common.base.Preconditions;

public class MPJ_GMPE_CacheGenScriptWriter {

	public static void main(String[] args) throws IOException {
		String jobName = "ucerf3_full_ngaw2";
		
		String erfFileName = "MeanUCERF3_full.xml";
		String[] gmpeNames = { "ask2014.xml", "bssa2014.xml", "cb2014.xml", "cy2014.xml" };
		String sitesFileName = "sites.xml";
		
		int mins = 60*23;
		int nodes = 40;
		Component comp = Component.RotD100;
		
		jobName = new SimpleDateFormat("yyyy_MM_dd").format(new Date())+"-"+jobName;
		
		int memGigs = 26;
		int ppn = 16;
		File remoteDir = new File("/work/00950/kevinm/cybershake/mcer_cache_gen");
		File remoteJobDir = new File(remoteDir, jobName);
		File localDir = new File("/home/kevin/CyberShake/MCER/gmpe_cache_gen");
		File localJobDir = new File(localDir, jobName);
		Preconditions.checkState(localJobDir.exists() || localJobDir.mkdir());
		
		File confDir = new File(remoteDir, "conf");
		
		List<File> classpath = new ArrayList<File>();
		classpath.add(new File(remoteDir, "commons-cli-1.2.jar"));
		classpath.add(new File(remoteDir, "OpenSHA_complete.jar"));
		
		FastMPJShellScriptWriter mpjWrite = new FastMPJShellScriptWriter(
				StampedeScriptWriter.JAVA_BIN, memGigs*1024, classpath, StampedeScriptWriter.FMPJ_HOME, false);
		BatchScriptWriter pbsWrite = new StampedeScriptWriter();
		
		String argz = "--output-dir "+remoteJobDir.getAbsolutePath();
		argz += " --erf-file "+confDir.getAbsolutePath()+"/"+erfFileName;
		argz += " --atten-rel-file ";
		for (int i=0; i<gmpeNames.length; i++) {
			if (i>0)
				argz += ",";
			argz += confDir.getAbsolutePath()+"/"+gmpeNames[i];
		}
		argz += " --sites "+remoteJobDir.getAbsolutePath()+"/"+sitesFileName;
		argz += " --component "+comp.name();
		
		File pbsFile = new File(localJobDir, "calc.pbs");
		
		List<String> script = mpjWrite.buildScript(MPJ_GMPE_CacheGen.class.getName(), argz);
		
		script = pbsWrite.buildScript(script, mins, nodes, ppn, null);
		pbsWrite.writeScript(pbsFile, script);
	}

}
