package scratch.UCERF3.analysis;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.opensha.commons.hpc.JavaShellScriptWriter;
import org.opensha.commons.hpc.mpj.FastMPJShellScriptWriter;
import org.opensha.commons.hpc.mpj.MPJExpressShellScriptWriter;
import org.opensha.commons.hpc.pbs.BatchScriptWriter;
import org.opensha.commons.hpc.pbs.USC_HPCC_ScriptWriter;
import org.opensha.sha.earthquake.param.MagDependentAperiodicityOptions;

import scratch.UCERF3.simulatedAnnealing.hpc.LogicTreePBSWriter;
import scratch.UCERF3.simulatedAnnealing.hpc.LogicTreePBSWriter.RunSites;

import com.google.common.collect.Lists;

public class MPJ_ERF_ProbGainCalcScriptWriter {

	public static void main(String[] args) throws IOException {
		String runName = "ucerf3-prob-gains-main-30yr";
		if (args.length > 1)
			runName = args[1];
		
		boolean mainFaults = true;
		
		double duration = 30;
		
		// it is assumed that this file is also stored locally in InversionSolutions!
		String compoundFileName = "2013_05_10-ucerf3p3-production-10runs_COMPOUND_SOL.zip";
		
		RunSites site = RunSites.HPCC;
		int nodes = 20;
		int jobMins = 10*60; // TODO
		
//		String threadsArg = "";
		// trailing space is important
		
		File localMainDir = new File("/home/kevin/OpenSHA/UCERF3/probGains");
		if (!localMainDir.exists())
			localMainDir.mkdir();
		File remoteMainDir = new File(site.getRUN_DIR().getParentFile(), "prob_gains");
		
		runName = LogicTreePBSWriter.df.format(new Date())+"-"+runName;
		
		File remoteDir = new File(remoteMainDir, runName);
		File writeDir = new File(localMainDir, runName);
		if (!writeDir.exists())
			writeDir.mkdir();
		
		File remoteCompoundfile = new File(remoteDir, compoundFileName);
		
		List<File> classpath = LogicTreePBSWriter.getClasspath(remoteMainDir, remoteDir);
		
		JavaShellScriptWriter mpjWrite;
		if (site.isFastMPJ())
			mpjWrite = new FastMPJShellScriptWriter(site.getJAVA_BIN(), site.getMaxHeapSizeMB(null),
					classpath, site.getMPJ_HOME(), false);
		else
			mpjWrite = new MPJExpressShellScriptWriter(site.getJAVA_BIN(), site.getMaxHeapSizeMB(null),
					classpath, site.getMPJ_HOME(), false);
		
		mpjWrite.setInitialHeapSizeMB(site.getInitialHeapSizeMB(null));
		mpjWrite.setHeadless(true);
		
		BatchScriptWriter batchWrite = site.forBranch(null);
		if (site == RunSites.HPCC) {
			((USC_HPCC_ScriptWriter)batchWrite).setNodesAddition(null);
		}
		
		List<boolean[]> calcOpsList = Lists.newArrayList();
		if (!mainFaults)
			calcOpsList.add(new boolean[] { true, false });
		calcOpsList.add(new boolean[] { true, true });
		if (!mainFaults)
			calcOpsList.add(new boolean[] { false, true });
		
		MagDependentAperiodicityOptions[] covs = { MagDependentAperiodicityOptions.LOW_VALUES,
				MagDependentAperiodicityOptions.MID_VALUES, MagDependentAperiodicityOptions.HIGH_VALUES };
		
		String className = MPJ_ERF_ProbGainCalc.class.getName();
		
		for (boolean[] calcOps : calcOpsList) {
			String opsStr;
			if (calcOps[0])
				opsStr = "aveRI";
			else
				opsStr = "aveRate";
			if (calcOps[1])
				opsStr += "_aveNTS";
			else
				opsStr += "_aveTS";
			
			for (MagDependentAperiodicityOptions cov : covs) {
				String pbsName = opsStr+"_"+cov.name()+".pbs";
				
				File remoteOutput = new File(new File(remoteDir, opsStr), cov.name());
				
				String classArgs = "--compound-sol "+remoteCompoundfile.getAbsolutePath()
						+" --output-dir "+remoteOutput.getAbsolutePath()
						+" --duration "+duration+" --aperiodicity "+cov.name()
						+" --ave-ri "+calcOps[0]+" --ave-norm-time-since "+calcOps[1];
				if (mainFaults)
					classArgs += " --main-faults";
				
				List<String> script = mpjWrite.buildScript(className, classArgs);
				
				batchWrite.writeScript(new File(writeDir, pbsName), script, jobMins, nodes, site.getPPN(null), null);
			}
		}
	}

}
