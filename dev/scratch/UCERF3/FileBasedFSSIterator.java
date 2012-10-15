package scratch.UCERF3;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import org.opensha.commons.util.ExceptionUtils;

import scratch.UCERF3.inversion.BatchPlotGen;
import scratch.UCERF3.logicTree.LogicTreeBranch;
import scratch.UCERF3.logicTree.VariableLogicTreeBranch;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

public class FileBasedFSSIterator extends FaultSystemSolutionFetcher {
	
	private Map<LogicTreeBranch, File> filesMap;
	
	public FileBasedFSSIterator(Map<LogicTreeBranch, File> filesMap) {
		this.filesMap = filesMap;
	}
	
	public static FileBasedFSSIterator forDirectory(File dir) {
		return forDirectory(dir, Integer.MAX_VALUE);
	}
	
	public static FileBasedFSSIterator forDirectory(File dir, int maxDepth) {
		return forDirectory(dir, Integer.MAX_VALUE, null);
	}
	
	public static FileBasedFSSIterator forDirectory(File dir, int maxDepth, String nameGrep) {
		return new FileBasedFSSIterator(solFilesForDirectory(dir, maxDepth, nameGrep));
	}
	
	private static Map<LogicTreeBranch, File> solFilesForDirectory(
			File dir, int maxDepth, String nameGrep) {
		Map<LogicTreeBranch, File> files = Maps.newHashMap();
		
		for (File file : dir.listFiles()) {
			if (file.isDirectory() && maxDepth > 0) {
				Map<LogicTreeBranch, File> subFiles = solFilesForDirectory(file, maxDepth-1, nameGrep);
				for (LogicTreeBranch branch : subFiles.keySet()) {
					checkNoDuplicates(branch, subFiles.get(branch), files);
					files.put(branch, subFiles.get(branch));
				}
				continue;
			}
			String name = file.getName();
			if (!name.endsWith("_sol.zip"))
				continue;
			if (nameGrep != null && !nameGrep.isEmpty()) {
				if (!name.contains(nameGrep))
					continue;
			} else if (name.contains("_run")) {
				// mean solutions allowed, individual runs not allowed
				continue;
			}
			LogicTreeBranch branch = VariableLogicTreeBranch.fromFileName(name);
			checkNoDuplicates(branch, file, files);
			files.put(branch, file);
		}
		
		return files; 
	}
	
	private static void checkNoDuplicates(
			LogicTreeBranch branch, File file, Map<LogicTreeBranch, File> files) {
		if (files.containsKey(branch)) {
			LogicTreeBranch origBranch = null;
			File origFile = files.get(branch);
			for (LogicTreeBranch candidateBranch : files.keySet()) {
				if (origFile == files.get(candidateBranch)) {
					origBranch = candidateBranch;
					break;
				}
			}
			String err = "Duplicate branch found!";
			err += "\nOrig branch:\t"+origBranch;
			err += "\nOrig file:\t"+files.get(branch);
			err += "\nNew branch:\t"+branch;
			err += "\nNew file:\t"+file;
			throw new IllegalStateException(err);
		}
	}

	@Override
	public Collection<LogicTreeBranch> getBranches() {
		return filesMap.keySet();
	}

	@Override
	protected FaultSystemSolution fetchSolution(LogicTreeBranch branch) {
		try {
			return SimpleFaultSystemSolution.fromFile(filesMap.get(branch));
		} catch (Exception e) {
			throw ExceptionUtils.asRuntimeException(e);
		}
	}

	@Override
	public Map<String, Double> fetchMisfits(LogicTreeBranch branch) {
		File misfitsFile = new File(filesMap.get(branch).getAbsolutePath()+".misfits");
		if (misfitsFile.exists()) {
			try {
				return BatchPlotGen.loadMisfitsFile(misfitsFile);
			} catch (Exception e) {
				ExceptionUtils.throwAsRuntimeException(e);
			}
		}
		return null;
	}

}
