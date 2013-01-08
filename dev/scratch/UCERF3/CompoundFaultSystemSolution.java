package scratch.UCERF3;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.opensha.commons.util.ExceptionUtils;
import org.opensha.commons.util.FileUtils;

import scratch.UCERF3.enumTreeBranches.DeformationModels;
import scratch.UCERF3.enumTreeBranches.FaultModels;
import scratch.UCERF3.enumTreeBranches.InversionModels;
import scratch.UCERF3.enumTreeBranches.MaxMagOffFault;
import scratch.UCERF3.enumTreeBranches.MomentRateFixes;
import scratch.UCERF3.enumTreeBranches.ScalingRelationships;
import scratch.UCERF3.enumTreeBranches.SpatialSeisPDF;
import scratch.UCERF3.enumTreeBranches.TotalMag5Rate;
import scratch.UCERF3.inversion.BatchPlotGen;
import scratch.UCERF3.logicTree.LogicTreeBranch;
import scratch.UCERF3.logicTree.LogicTreeBranchNode;
import scratch.UCERF3.logicTree.VariableLogicTreeBranch;
import scratch.UCERF3.utils.MatrixIO;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class CompoundFaultSystemSolution extends FaultSystemSolutionFetcher {
	
	// TODO add weight support
	
	private FaultSystemSolutionFetcher fetcher;
	
	public CompoundFaultSystemSolution(FaultSystemSolutionFetcher fetcher) {
		this.fetcher = fetcher;
	}
	
	@Override
	public Collection<LogicTreeBranch> getBranches() {
		return fetcher.getBranches();
	}
	
	@Override
	protected FaultSystemSolution fetchSolution(LogicTreeBranch branch) {
		return fetcher.fetchSolution(branch);
	}

	@Override
	public Map<String, Double> fetchMisfits(LogicTreeBranch branch) {
		return fetcher.fetchMisfits(branch);
	}
	
	/* *******************************************
	 * Zip file methods
	 * ******************************************* */
	
	/**
	 * *********************************************
	 * Files						Dependencies
	 * *********************************************
	 * close_sections.bin			FM
	 * cluster_rups.bin				FM
	 * cluster_sects.bin			FM
	 * fault_sections.xml			FM
	 * info.txt						ALL
	 * mags.bin						FM, DM, Scale
	 * rakes.bin					FM, DM
	 * rates.bin					ALL
	 * rup_areas.bin				FM, DM
	 * rup_avg_slips.bin			FM, DM, Scale
	 * rup_sec_slip_type.txt		N/A
	 * rup_sections.bin				FM
	 * sect_areas.bin				FM, DM
	 * sect_slips.bin				ALL BUT Dsr
	 * sect_slips_std_dev.bin		ALL BUT Dsr
	 * 
	 * null entry in map means ALL!
	 */
	private static Map<String, List<Class<? extends LogicTreeBranchNode<?>>>> dependencyMap;
	static {
		dependencyMap = Maps.newHashMap();
		
		dependencyMap.put("close_sections.bin", buildList(FaultModels.class));
		dependencyMap.put("cluster_rups.bin", buildList(FaultModels.class));
		dependencyMap.put("cluster_sects.bin", buildList(FaultModels.class));
		dependencyMap.put("fault_sections.xml", buildList(FaultModels.class));
		dependencyMap.put("info.txt", null);
		dependencyMap.put("mags.bin", buildList(FaultModels.class, DeformationModels.class, ScalingRelationships.class));
		dependencyMap.put("rakes.bin", buildList(FaultModels.class, DeformationModels.class));
		dependencyMap.put("rates.bin", null);
		dependencyMap.put("rup_areas.bin", buildList(FaultModels.class, DeformationModels.class));
		dependencyMap.put("rup_avg_slips.bin", buildList(FaultModels.class, DeformationModels.class, ScalingRelationships.class));
		dependencyMap.put("rup_sec_slip_type.txt", null);
		dependencyMap.put("rup_sections.bin", buildList(FaultModels.class));
		dependencyMap.put("rakes.bin", buildList(FaultModels.class, DeformationModels.class));
		dependencyMap.put("sect_areas.bin", buildList(FaultModels.class, DeformationModels.class));
		dependencyMap.put("sect_slips.bin", buildList(FaultModels.class, DeformationModels.class,
				ScalingRelationships.class, InversionModels.class, TotalMag5Rate.class,
				MaxMagOffFault.class, MomentRateFixes.class, SpatialSeisPDF.class));
		dependencyMap.put("sect_slips_std_dev.bin", buildList(FaultModels.class, DeformationModels.class,
				ScalingRelationships.class, InversionModels.class, TotalMag5Rate.class,
				MaxMagOffFault.class, MomentRateFixes.class, SpatialSeisPDF.class));
	}
	
	private static List<Class<? extends LogicTreeBranchNode<?>>> buildList(
			Class<? extends LogicTreeBranchNode<?>>... vals) {
		List<Class<? extends LogicTreeBranchNode<?>>> list = Lists.newArrayList();
		for (Class<? extends LogicTreeBranchNode<?>> val : vals)
			list.add(val);
		return list;
	}
	
	public void toZipFile(File file) throws IOException {
		toZipFile(file, fetcher);
	}
	
	public static void toZipFile(File file, FaultSystemSolutionFetcher fetcher) throws IOException {
		System.out.println("Making compound zip file: "+file.getName());
		File tempDir = FileUtils.createTempDir();
		
		HashSet<String> zipFileNames = new HashSet<String>();
		
		for (LogicTreeBranch branch : fetcher.getBranches()) {
			FaultSystemSolution sol = fetcher.getSolution(branch);
			
			Map<String, String> remappings = getRemappings(branch);
			
			SimpleFaultSystemRupSet rupSet = SimpleFaultSystemRupSet.toSimple(sol);
			rupSet.writeFilesForZip(tempDir, zipFileNames, remappings);
			
			// now write solution
			File ratesFile = new File(tempDir, remappings.get("rates.bin"));
			MatrixIO.doubleArrayToFile(sol.getRateForAllRups(), ratesFile);
			zipFileNames.add(ratesFile.getName());
			if (sol instanceof AverageFaultSystemSolution) {
				String ratesPrefix = ratesFile.getName();
				ratesPrefix = ratesPrefix.substring(0, ratesPrefix.indexOf(".bin"));
				AverageFaultSystemSolution avgSol = (AverageFaultSystemSolution)sol;
				int num = avgSol.getNumSolutions();
				int digits = ((num-1)+"").length();
				for (int i=0; i<num; i++) {
					String numStr = i+"";
					while (numStr.length() < digits)
						numStr = "0"+numStr;
					File ithRatesFile = new File(tempDir, ratesPrefix+"_"+numStr+".bin");
					MatrixIO.doubleArrayToFile(avgSol.getRates(i), ithRatesFile);
					zipFileNames.add(ithRatesFile.getName());
				}
			}
			
			// now write misfits, if applicable
			Map<String, Double> misfits = fetcher.getMisfits(branch);
			if (misfits != null && !misfits.isEmpty()) {
				File misfitsFile = new File(tempDir, branch.buildFileName()+".misfits");
				BatchPlotGen.writeMisfitsFile(misfits, misfitsFile);
				zipFileNames.add(misfitsFile.getName());
			}
		}
		
		FileUtils.createZipFile(file.getAbsolutePath(), tempDir.getAbsolutePath(), zipFileNames);
		
		System.out.println("Deleting temp files");
		FileUtils.deleteRecursive(tempDir);
		
		System.out.println("Done saving!");
	}
	
	private static Map<String, String> getRemappings(LogicTreeBranch branch) {
		Map<String, String> remappings = Maps.newHashMap();
		
		for (String name : dependencyMap.keySet())
			remappings.put(name, getRemappedName(name, branch));
		
		return remappings;
	}
	
	public static String getRemappedName(String name, LogicTreeBranch branch) {
		String nodeStr = "";
		List<Class<? extends LogicTreeBranchNode<?>>> dependencies = dependencyMap.get(name);
		if (dependencies == null)
			nodeStr = branch.buildFileName()+"_";
		else
			for (Class<? extends LogicTreeBranchNode<?>> clazz : dependencies)
				nodeStr += branch.getValueUnchecked(clazz).encodeChoiceString()+"_";
		return nodeStr+name;
	}
	
	public static CompoundFaultSystemSolution fromZipFile(File file) throws ZipException, IOException {
		ZipFile zip = new ZipFile(file);
		return new CompoundFaultSystemSolution(new ZipFileSolutionFetcher(zip));
	}
	
	public static class ZipFileSolutionFetcher extends FaultSystemSolutionFetcher {
		
		private ZipFile zip;
		private List<LogicTreeBranch> branches;
		
		public ZipFileSolutionFetcher(ZipFile zip) {
			this.zip = zip;
			branches = Lists.newArrayList();
			
			Enumeration<? extends ZipEntry> zipEnum = zip.entries();
			// need to sort to ensure consistant iteration order for parallel runs
			List<ZipEntry> entriesList = Lists.newArrayList();
			while (zipEnum.hasMoreElements())
				entriesList.add(zipEnum.nextElement());
			Collections.sort(entriesList, new Comparator<ZipEntry>() {

				@Override
				public int compare(ZipEntry o1, ZipEntry o2) {
					return o1.getName().compareTo(o2.getName());
				}
			});
			for (ZipEntry entry : entriesList)
				if (entry.getName().endsWith("_rates.bin"))
					branches.add(VariableLogicTreeBranch.fromFileName(entry.getName()));
			
			System.out.println("Detected "+branches.size()+" branches in zip file!");
		}

		@Override
		public Collection<LogicTreeBranch> getBranches() {
			return branches;
		}

		@Override
		protected FaultSystemSolution fetchSolution(LogicTreeBranch branch) {
			try {
				Map<String, String> nameRemappings = getRemappings(branch);
				SimpleFaultSystemRupSet rupSet = SimpleFaultSystemRupSet.fromZipFile(zip, nameRemappings);
				
				// TODO cache all of the values?
				
				ZipEntry ratesEntry = zip.getEntry(nameRemappings.get("rates.bin"));
				double[] rates = MatrixIO.doubleArrayFromInputStream(
						new BufferedInputStream(zip.getInputStream(ratesEntry)), ratesEntry.getSize());
				
				return new SimpleFaultSystemSolution(rupSet, rates);
			} catch (Exception e) {
				throw ExceptionUtils.asRuntimeException(e);
			}
		}
		
		public double[] getRates(LogicTreeBranch branch) throws IOException {
			Map<String, String> nameRemappings = getRemappings(branch);
			ZipEntry ratesEntry = zip.getEntry(nameRemappings.get("rates.bin"));
			return MatrixIO.doubleArrayFromInputStream(
					new BufferedInputStream(zip.getInputStream(ratesEntry)), ratesEntry.getSize());
		}
		
		public double[] getMags(LogicTreeBranch branch) throws IOException {
			Map<String, String> nameRemappings = getRemappings(branch);
			ZipEntry magsEntry = zip.getEntry(nameRemappings.get("mags.bin"));
			return MatrixIO.doubleArrayFromInputStream(
					new BufferedInputStream(zip.getInputStream(magsEntry)), magsEntry.getSize());
		}

		@Override
		public Map<String, Double> fetchMisfits(LogicTreeBranch branch) {
			String fName = branch.buildFileName()+".misfits";
			ZipEntry entry = zip.getEntry(fName);
			if (entry != null) {
				try {
					return BatchPlotGen.loadMisfitsFile(zip.getInputStream(entry));
				} catch (IOException e) {
					ExceptionUtils.throwAsRuntimeException(e);
				}
			}
			return null;
		}
		
	}
	
	public static void main(String[] args) throws IOException {
		if (args.length >= 1) {
			// command line run
			File dir = new File(args[0]);
			List<String> nameGreps = Lists.newArrayList();
			for (int i=1; i<args.length; i++)
				nameGreps.add(args[i]);
			BatchPlotGen.writeCombinedFSS(dir, nameGreps);
			System.exit(0);
		}
		File dir = new File("/tmp/avg_test");
		FileBasedFSSIterator it = FileBasedFSSIterator.forDirectory(dir, 1, Lists.newArrayList(FileBasedFSSIterator.TAG_BUILD_MEAN));
		
		File compoundFile = new File(dir, "COMPOUND_SOL.zip");
		Stopwatch watch = new Stopwatch();
		watch.start();
		toZipFile(compoundFile, it);
		watch.stop();
		System.out.println("Took "+(watch.elapsedMillis() / 1000d)+" seconds to save");
		
		watch.reset();
		watch.start();
		CompoundFaultSystemSolution compoundSol = fromZipFile(compoundFile);
		
		for (LogicTreeBranch branch : compoundSol.getBranches()) {
			System.out.println("Loading "+branch);
			compoundSol.getSolution(branch);
		}
		System.out.println("Took "+(watch.elapsedMillis() / 1000d)+" seconds to load");
	}

}
