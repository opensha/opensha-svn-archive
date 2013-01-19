package scratch.kevin.ucerf3.maps;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipException;

import org.opensha.commons.hpc.JavaShellScriptWriter;
import org.opensha.commons.hpc.mpj.FastMPJShellScriptWriter;
import org.opensha.commons.hpc.mpj.MPJExpressShellScriptWriter;
import org.opensha.commons.hpc.pbs.BatchScriptWriter;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import scratch.UCERF3.CompoundFaultSystemSolution;
import scratch.UCERF3.logicTree.LogicTreeBranch;
import scratch.UCERF3.simulatedAnnealing.hpc.LogicTreePBSWriter;
import scratch.UCERF3.simulatedAnnealing.hpc.LogicTreePBSWriter.RunSites;
import scratch.peter.ucerf3.calc.UC3_HazardCalcDriverMPJ;

public class MapScriptWriter {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws ZipException 
	 */
	public static void main(String[] args) throws ZipException, IOException {
		String runName = "ucerf3p2-pga-maps";
		if (args.length > 1)
			runName = args[1];
		
		// it is assumed that this file is also stored locally in InversionSolutions!
		String compoundFileName = "2013_01_14-stampede_3p2_production_runs_combined_COMPOUND_SOL.zip";
		
		RunSites site = RunSites.STAMPEDE;
		int nodes = 64;
		int bundleSize = 30; // TODO, must be >0
		int jobMins = 6*60; // TODO
		
		String regionCode = "CA_RELM";
		String resCode = "0.1";
		String imtCode = "GM0P00";
		
		File localMainDir = new File("/home/kevin/OpenSHA/UCERF3/maps");
		File remoteMainDir = new File(site.getRUN_DIR().getParentFile(), "maps");
		
		runName = LogicTreePBSWriter.df.format(new Date())+"-"+runName;
		
		File remoteDir = new File(remoteMainDir, runName);
		File writeDir = new File(localMainDir, runName);
		if (!writeDir.exists())
			writeDir.mkdir();
		
		File localCompoundFile = new File("/home/kevin/workspace/OpenSHA/dev/scratch/UCERF3/data/scratch/" +
				"InversionSolutions/"+compoundFileName);
		Preconditions.checkState(localCompoundFile.exists(), localCompoundFile.getAbsolutePath()+" doesn't exist!");
		File remoteCompoundfile = new File(remoteDir, compoundFileName);
		
		CompoundFaultSystemSolution fss = CompoundFaultSystemSolution.fromZipFile(localCompoundFile);
		
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
		
		List<List<LogicTreeBranch>> branchGroupLists = Lists.newArrayList();
		
		List<LogicTreeBranch> branchGroup = Lists.newArrayList();
		for (LogicTreeBranch branch : fss.getBranches()) {
			if (branchGroup.size() >= bundleSize) {
				branchGroupLists.add(branchGroup);
				branchGroup = Lists.newArrayList();
			}
			branchGroup.add(branch);
		}
		if (branchGroup.size() > 0)
			branchGroupLists.add(branchGroup);
		
		int numJobDigits = (""+(branchGroupLists.size()-1)).length();
		
		int batchCount = 0;
		int jobCount = 0;
		
		for (List<LogicTreeBranch> branches : branchGroupLists) {
			List<String> classNames = Lists.newArrayList();
			List<String> argss = Lists.newArrayList();
			
			for (LogicTreeBranch branch : branches) {
				classNames.add(UC3_HazardCalcDriverMPJ.class.getName());
				argss.add(remoteCompoundfile.getAbsolutePath()+" "+branch.buildFileName()
						+" "+regionCode+" "+resCode+" "+imtCode+" "+remoteDir.getAbsolutePath());
			}
			
			List<String> script = mpjWrite.buildScript(classNames, argss);
			
			String scriptName = batchCount+"";
			while (scriptName.length() < numJobDigits)
				scriptName = "0"+scriptName;
			
			scriptName = "maps_"+scriptName+".pbs";
			
			batchWrite.writeScript(new File(writeDir, scriptName), script, jobMins, nodes, site.getPPN(null), null);
			
			jobCount += branches.size();
			batchCount++;
		}
		
		System.out.println("Wrote "+batchCount+" batches ("+jobCount+" jobs)");
	}

}
