package scratch.UCERF3;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.math3.stat.StatUtils;
import org.opensha.commons.util.ExceptionUtils;

import scratch.UCERF3.inversion.BatchPlotGen;
import scratch.UCERF3.logicTree.LogicTreeBranch;
import scratch.UCERF3.logicTree.VariableLogicTreeBranch;
import scratch.UCERF3.utils.MatrixIO;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class FileBasedFSSIterator extends FaultSystemSolutionFetcher {
	
	public static final String TAG_BUILD_MEAN = "BUILD_MEAN";
	
	private Map<LogicTreeBranch, File[]> filesMap;
	
	public FileBasedFSSIterator(Map<LogicTreeBranch, File[]> filesMap) {
		this.filesMap = filesMap;
	}
	
	public static FileBasedFSSIterator forDirectory(File dir) {
		return forDirectory(dir, Integer.MAX_VALUE);
	}
	
	public static FileBasedFSSIterator forDirectory(File dir, int maxDepth) {
		return forDirectory(dir, Integer.MAX_VALUE, null);
	}
	
	public static FileBasedFSSIterator forDirectory(File dir, int maxDepth, List<String> nameGreps) {
		return new FileBasedFSSIterator(solFilesForDirectory(dir, maxDepth, nameGreps));
	}
	
	private static Map<LogicTreeBranch, File[]> solFilesForDirectory(
			File dir, int maxDepth, List<String> nameGreps) {
		Map<LogicTreeBranch, File[]> files = Maps.newHashMap();
		
		boolean assembleMean = nameGreps != null && nameGreps.contains(TAG_BUILD_MEAN);
		
		List<String> myNameGreps;
		if (assembleMean) {
			myNameGreps = Lists.newArrayList(nameGreps);
			myNameGreps.remove(TAG_BUILD_MEAN);
		} else {
			myNameGreps = nameGreps;
		}
		
		fileLoop:
		for (File file : dir.listFiles()) {
			if (file.isDirectory() && maxDepth > 0) {
				Map<LogicTreeBranch, File[]> subFiles = solFilesForDirectory(file, maxDepth-1, nameGreps);
				for (LogicTreeBranch branch : subFiles.keySet()) {
					if (assembleMean) {
						File[] newFiles = subFiles.get(branch);
						if (files.containsKey(branch)) {
							File[] origFiles = files.get(branch);
							File[] combined = new File[newFiles.length+origFiles.length];
							System.arraycopy(origFiles, 0, combined, 0, origFiles.length);
							System.arraycopy(newFiles, 0, combined, origFiles.length, newFiles.length);
							files.put(branch, combined);
						} else {
							files.put(branch, newFiles);
						}
					} else {
						checkNoDuplicates(branch, subFiles.get(branch)[0], files);
						files.put(branch, subFiles.get(branch));
					}
				}
				continue;
			}
			String name = file.getName();
			if (!name.endsWith("_sol.zip"))
				continue;
			if (myNameGreps != null && !myNameGreps.isEmpty()) {
				for (String nameGrep : myNameGreps)
					if (!name.contains(nameGrep))
						continue fileLoop;
			} else if (name.contains("_run") && !assembleMean) {
				// mean solutions allowed, individual runs not allowed
				continue;
			}
			LogicTreeBranch branch = VariableLogicTreeBranch.fromFileName(name);
			if (assembleMean) {
				File[] array = files.get(branch);
				if (array == null) {
					array = new File[1];
					array[0] = file;
				} else {
					File[] newArray = new File[array.length+1];
					System.arraycopy(array, 0, newArray, 0, array.length);
					newArray[array.length] = file;
				}
				files.put(branch, array);
			} else {
				checkNoDuplicates(branch, file, files);
				File[] array = { file };
				files.put(branch, array);
			}
		}
		
		return files; 
	}
	
	private static void checkNoDuplicates(
			LogicTreeBranch branch, File file, Map<LogicTreeBranch, File[]> files) {
		if (files.containsKey(branch)) {
			LogicTreeBranch origBranch = null;
			File origFile = files.get(branch)[0];
			for (LogicTreeBranch candidateBranch : files.keySet()) {
				if (origFile == files.get(candidateBranch)[0]) {
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
			File[] files = filesMap.get(branch);
			FaultSystemSolution sol = SimpleFaultSystemSolution.fromFile(files[0]);
			if (files.length > 1) {
				List<double[]> ratesList = Lists.newArrayList(sol.getRateForAllRups());
				for (int i=1; i<files.length; i++) {
					ZipFile zip = new ZipFile(files[i]);
					ZipEntry ratesEntry = zip.getEntry("rates.bin");
					double[] rates = MatrixIO.doubleArrayFromInputStream(
							new BufferedInputStream(zip.getInputStream(ratesEntry)), ratesEntry.getSize());
					ratesList.add(rates);
				}
				sol = new AverageFaultSystemSolution(sol, ratesList);
				System.out.println("Built mean with "+ratesList.size()+" sols");
			}
			
			return sol;
		} catch (Exception e) {
			throw ExceptionUtils.asRuntimeException(e);
		}
	}

	@Override
	public Map<String, Double> fetchMisfits(LogicTreeBranch branch) {
		List<Map<String, Double>> misfits = Lists.newArrayList();
		for (File file : filesMap.get(branch)) {
			File misfitsFile = new File(file.getAbsolutePath()+".misfits");
			if (misfitsFile.exists()) {
				try {
					misfits.add(BatchPlotGen.loadMisfitsFile(misfitsFile));
				} catch (Exception e) {
					ExceptionUtils.throwAsRuntimeException(e);
				}
			}
		}
		if (misfits.size() > 0) {
			if (misfits.size() == 1)
				return misfits.get(0);
			Map<String, Double> avgMisfits = Maps.newHashMap();
			for (String key : misfits.get(0).keySet()) {
				double[] vals = new double[misfits.size()];
				for (int i=0; i<misfits.size(); i++)
					vals[i] = misfits.get(i).get(key);
				avgMisfits.put(key, StatUtils.mean(vals));
			}
			return avgMisfits;
		}
		return null;
	}

}
