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
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.opensha.commons.data.CSVFile;
import org.opensha.commons.util.ClassUtils;
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
import scratch.UCERF3.inversion.InversionFaultSystemSolution;
import scratch.UCERF3.logicTree.LogicTreeBranch;
import scratch.UCERF3.logicTree.LogicTreeBranchNode;
import scratch.UCERF3.logicTree.VariableLogicTreeBranch;
import scratch.UCERF3.utils.MatrixIO;
import scratch.UCERF3.utils.FaultSystemIO;
import scratch.UCERF3.utils.UCERF3_DataUtils;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class CompoundFaultSystemSolution extends FaultSystemSolutionFetcher {
	
	// TODO add weight support
	
	private ZipFile zip;
	private List<LogicTreeBranch> branches;
	
	public CompoundFaultSystemSolution(ZipFile zip) {
		this.zip = zip;
		branches = Lists.newArrayList();
		
		Enumeration<? extends ZipEntry> zipEnum = zip.entries();
		// need to sort to ensure consistent iteration order for parallel runs
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
	protected InversionFaultSystemSolution fetchSolution(LogicTreeBranch branch) {
		try {
			Map<String, String> nameRemappings = getRemappings(branch);
			FaultSystemSolution sol = FaultSystemIO.loadSolAsApplicable(zip, nameRemappings);
			Preconditions.checkState(sol instanceof InversionFaultSystemSolution,
					"Non IVFSS in Compound Sol?");
			
			// TODO cache all of the values?
			
			return (InversionFaultSystemSolution)sol;
		} catch (Exception e) {
			throw ExceptionUtils.asRuntimeException(e);
		}
	}
	
	public double[] getRates(LogicTreeBranch branch) {
		try {
			Map<String, String> nameRemappings = getRemappings(branch);
			ZipEntry ratesEntry = zip.getEntry(nameRemappings.get("rates.bin"));
			return MatrixIO.doubleArrayFromInputStream(
					new BufferedInputStream(zip.getInputStream(ratesEntry)), ratesEntry.getSize());
		} catch (IOException e) {
			throw ExceptionUtils.asRuntimeException(e);
		}
	}
	
	public String getInfo(LogicTreeBranch branch) {
		try {
			Map<String, String> nameRemappings = getRemappings(branch);
			ZipEntry infoEntry = zip.getEntry(nameRemappings.get("info.txt"));
			StringBuilder text = new StringBuilder();
		    String NL = System.getProperty("line.separator");
		    Scanner scanner = new Scanner(
					new BufferedInputStream(zip.getInputStream(infoEntry)));
		    try {
		      while (scanner.hasNextLine()){
		        text.append(scanner.nextLine() + NL);
		      }
		    }
		    finally{
		      scanner.close();
		    }
		    return text.toString();
		} catch (IOException e) {
			throw ExceptionUtils.asRuntimeException(e);
		}
	}
	
	public double[] getMags(LogicTreeBranch branch) {
		try {
			Map<String, String> nameRemappings = getRemappings(branch);
			ZipEntry magsEntry = zip.getEntry(nameRemappings.get("mags.bin"));
			return MatrixIO.doubleArrayFromInputStream(
					new BufferedInputStream(zip.getInputStream(magsEntry)), magsEntry.getSize());
		} catch (IOException e) {
			throw ExceptionUtils.asRuntimeException(e);
		}
	}
	
	public double[] getLengths(LogicTreeBranch branch) {
		try {
			Map<String, String> nameRemappings = getRemappings(branch);
			ZipEntry magsEntry = zip.getEntry(nameRemappings.get("rup_lengths.bin"));
			return MatrixIO.doubleArrayFromInputStream(
					new BufferedInputStream(zip.getInputStream(magsEntry)), magsEntry.getSize());
		} catch (IOException e) {
			throw ExceptionUtils.asRuntimeException(e);
		}
	}
	
	/**
	 * *********************************************
	 * Files						Dependencies
	 * *********************************************
	 * close_sections.bin			FM
	 * cluster_rups.bin				FM
	 * cluster_sects.bin			FM
	 * fault_sections.xml			FM, DM
	 * info.txt						ALL
	 * mags.bin						FM, DM, Scale
	 * rakes.bin					FM, DM
	 * rates.bin					ALL
	 * rup_areas.bin				FM, DM
	 * rup_lengths.bin				FM
	 * rup_avg_slips.bin			FM, DM, Scale
	 * rup_sec_slip_type.txt		N/A
	 * rup_sections.bin				FM
	 * sect_areas.bin				FM, DM
	 * sect_slips.bin				ALL BUT Dsr
	 * sect_slips_std_dev.bin		ALL BUT Dsr
	 * inv_rup_set_metadata.xml		ALL
	 * inv_sol_metadata.xml			ALL
	 * grid_sources.xml				ALL
	 * 
	 * null entry in map means ALL!
	 */
	private static Map<String, List<Class<? extends LogicTreeBranchNode<?>>>> dependencyMap;
	static {
		dependencyMap = Maps.newHashMap();
		
		dependencyMap.put("close_sections.bin", buildList(FaultModels.class));
		dependencyMap.put("cluster_rups.bin", buildList(FaultModels.class));
		dependencyMap.put("cluster_sects.bin", buildList(FaultModels.class));
		dependencyMap.put("fault_sections.xml", buildList(FaultModels.class, DeformationModels.class));
		dependencyMap.put("info.txt", null);
		dependencyMap.put("mags.bin", buildList(FaultModels.class, DeformationModels.class, ScalingRelationships.class));
		dependencyMap.put("rakes.bin", buildList(FaultModels.class, DeformationModels.class));
		dependencyMap.put("rates.bin", null);
		dependencyMap.put("rup_areas.bin", buildList(FaultModels.class, DeformationModels.class));
		dependencyMap.put("rup_lengths.bin", buildList(FaultModels.class));
		dependencyMap.put("rup_avg_slips.bin", buildList(FaultModels.class, DeformationModels.class, ScalingRelationships.class));
		dependencyMap.put("rup_sec_slip_type.txt", null); // kept for backwards compatibility
		dependencyMap.put("rup_sections.bin", buildList(FaultModels.class));
		dependencyMap.put("rakes.bin", buildList(FaultModels.class, DeformationModels.class));
		dependencyMap.put("sect_areas.bin", buildList(FaultModels.class, DeformationModels.class));
		dependencyMap.put("sect_slips.bin", buildList(FaultModels.class, DeformationModels.class,
				ScalingRelationships.class, InversionModels.class, TotalMag5Rate.class,
				MaxMagOffFault.class, MomentRateFixes.class, SpatialSeisPDF.class));
		dependencyMap.put("sect_slips_std_dev.bin", buildList(FaultModels.class, DeformationModels.class,
				ScalingRelationships.class, InversionModels.class, TotalMag5Rate.class,
				MaxMagOffFault.class, MomentRateFixes.class, SpatialSeisPDF.class));
		dependencyMap.put("inv_rup_set_metadata.xml", null);
		dependencyMap.put("inv_sol_metadata.xml", null);
		dependencyMap.put("grid_sources.xml", null);
	}
	
	private static List<Class<? extends LogicTreeBranchNode<?>>> buildList(
			Class<? extends LogicTreeBranchNode<?>>... vals) {
		List<Class<? extends LogicTreeBranchNode<?>>> list = Lists.newArrayList();
		for (Class<? extends LogicTreeBranchNode<?>> val : vals)
			list.add(val);
		return list;
	}
	
	public void toZipFile(File file) throws IOException {
		toZipFile(file, this);
	}
	
	public static void toZipFile(File file, FaultSystemSolutionFetcher fetcher) throws IOException {
		System.out.println("Making compound zip file: "+file.getName());
		File tempDir = FileUtils.createTempDir();
		
		HashSet<String> zipFileNames = new HashSet<String>();
		
		for (LogicTreeBranch branch : fetcher.getBranches()) {
			FaultSystemSolution sol = fetcher.getSolution(branch);
			
			Map<String, String> remappings = getRemappings(branch);
			
			FaultSystemIO.writeSolFilesForZip(sol, tempDir, zipFileNames, remappings);
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
		return new CompoundFaultSystemSolution(zip);
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
//		File dir = new File(UCERF3_DataUtils.DEFAULT_SCRATCH_DATA_DIR, "InversionSolutions");
		File dir = new File("/home/kevin/workspace/OpenSHA/dev/scratch/UCERF3/data/scratch/InversionSolutions");
//		File dir = new File("/tmp/compound_tests_data/subset/");
//		FileBasedFSSIterator it = FileBasedFSSIterator.forDirectory(dir, 1, Lists.newArrayList(FileBasedFSSIterator.TAG_BUILD_MEAN));
		
		File compoundFile = new File(dir, "2013_05_10-ucerf3p3-production-10runs_COMPOUND_SOL.zip");
//		File compoundFile = new File(dir, "subset_COMPOUND_SOL.zip");
		Stopwatch watch = new Stopwatch();
//		watch.start();
//		toZipFile(compoundFile, it);
//		watch.stop();
//		System.out.println("Took "+(watch.elapsedMillis() / 1000d)+" seconds to save");
		
		watch.reset();
		watch.start();
		CompoundFaultSystemSolution compoundSol = fromZipFile(compoundFile);
//		System.exit(0);
		
		for (LogicTreeBranch branch : compoundSol.getBranches()) {
			System.out.println("Loading "+branch);
			System.out.println(ClassUtils.getClassNameWithoutPackage(
					compoundSol.getSolution(branch).getClass()));
		}
		System.out.println("Took "+(watch.elapsedMillis() / 1000d)+" seconds to load");
	}

}
