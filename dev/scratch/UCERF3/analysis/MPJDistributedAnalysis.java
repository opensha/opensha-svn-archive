package scratch.UCERF3.analysis;

import java.io.File;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import mpi.MPI;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.opensha.commons.data.CSVFile;
import org.opensha.commons.hpc.mpj.taskDispatch.MPJTaskCalculator;

import scratch.UCERF3.enumTreeBranches.DeformationModels;
import scratch.UCERF3.enumTreeBranches.FaultModels;
import scratch.UCERF3.enumTreeBranches.InversionModels;
import scratch.UCERF3.enumTreeBranches.MaxMagOffFault;
import scratch.UCERF3.enumTreeBranches.MomentRateFixes;
import scratch.UCERF3.enumTreeBranches.ScalingRelationships;
import scratch.UCERF3.enumTreeBranches.SlipAlongRuptureModels;
import scratch.UCERF3.enumTreeBranches.SpatialSeisPDF;
import scratch.UCERF3.enumTreeBranches.TotalMag5Rate;
import scratch.UCERF3.inversion.InversionFaultSystemRupSet;
import scratch.UCERF3.inversion.InversionFaultSystemRupSetFactory;
import scratch.UCERF3.logicTree.ListBasedTreeTrimmer;
import scratch.UCERF3.logicTree.LogicTreeBranch;
import scratch.UCERF3.logicTree.LogicTreeBranchIterator;
import scratch.UCERF3.logicTree.LogicTreeBranchNode;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class MPJDistributedAnalysis extends MPJTaskCalculator {

	private File file;
	private List<LogicTreeBranch> branches;

	private Map<Integer, String> results;

	public MPJDistributedAnalysis(CommandLine cmd, File file) {
		super(cmd);

		this.file = file;

		buildTaskList();
	}

	private static List<LogicTreeBranchNode<?>> toList(LogicTreeBranchNode<?>... vals) {
		return Arrays.asList(vals);
	}

	private void buildTaskList(){
		branches = Lists.newArrayList();

		List<List<LogicTreeBranchNode<?>>> limitationsList = Lists.newArrayList();
		
		ListBasedTreeTrimmer trimmer;

		limitationsList.add(toList(FaultModels.FM3_1));
		limitationsList.add(toList(DeformationModels.GEOLOGIC, DeformationModels.ABM, DeformationModels.NEOKINEMA, DeformationModels.ZENG));
		limitationsList.add(toList(InversionModels.CHAR_CONSTRAINED, InversionModels.GR_CONSTRAINED));
		limitationsList.add(toList(SlipAlongRuptureModels.TAPERED, SlipAlongRuptureModels.UNIFORM));
		limitationsList.add(toList(ScalingRelationships.ELLSWORTH_B, ScalingRelationships.HANKS_BAKUN_08, ScalingRelationships.SHAW_2009_MOD));
		limitationsList.add(toList(TotalMag5Rate.RATE_8p7, TotalMag5Rate.RATE_7p1, TotalMag5Rate.RATE_10p6));
		limitationsList.add(toList(MaxMagOffFault.MAG_7p2, MaxMagOffFault.MAG_7p6, MaxMagOffFault.MAG_8p0));
		limitationsList.add(toList(MomentRateFixes.NONE, MomentRateFixes.APPLY_IMPLIED_CC));
		limitationsList.add(toList(SpatialSeisPDF.UCERF2, SpatialSeisPDF.UCERF3));
		
//		limitationsList.add(toList(FaultModels.FM3_1));
//		limitationsList.add(toList(DeformationModels.GEOLOGIC));
//		limitationsList.add(toList(InversionModels.CHAR_CONSTRAINED));
//		limitationsList.add(toList(SlipAlongRuptureModels.TAPERED));
//		limitationsList.add(toList(ScalingRelationships.ELLSWORTH_B));
//		limitationsList.add(toList(TotalMag5Rate.RATE_8p7));
//		limitationsList.add(toList(MaxMagOffFault.MAG_7p2));
//		limitationsList.add(toList(MomentRateFixes.NONE, MomentRateFixes.APPLY_IMPLIED_CC));
//		limitationsList.add(toList(SpatialSeisPDF.UCERF2, SpatialSeisPDF.UCERF3));
		
		trimmer = new ListBasedTreeTrimmer(limitationsList);
//		trimmer = ListBasedTreeTrimmer.getNonZeroWeightsTrimmer();

		for (LogicTreeBranch branch : new LogicTreeBranchIterator(trimmer)) {
			branches.add(branch);
		}

		results = Maps.newHashMap();
	}

	@Override
	protected int getNumTasks() {
		return branches.size();
	}

	@Override
	protected void calculateBatch(int[] batch) throws Exception {
		List<CalcThread> threads = Lists.newArrayList();

		int perThread = (int) Math.floor((double)batch.length / (double)getNumThreads());

		for (int i=0; i<getNumThreads(); i++) {
			int start = i*perThread;
			if (start >= batch.length)
				continue;
			int end = start + perThread;
			if (end > batch.length || i == getNumThreads()-1)
				end = batch.length;
			int[] subBatch = Arrays.copyOfRange(batch, start, end);
			threads.add(new CalcThread(subBatch));
		}

		for (CalcThread thread : threads)
			thread.start();

		for (CalcThread thread : threads) {
			thread.join();
			this.results.putAll(thread.map);
		}
	}

	private class CalcThread extends Thread {

		private int[] batch;

		private Map<Integer, String> map;

		public CalcThread(int[] batch) {
			this.batch = batch;
		}

		@Override
		public void run() {
			map = Maps.newHashMap();

			for (int index : batch) {
				LogicTreeBranch branch = branches.get(index);

				InversionFaultSystemRupSet faultSysRupSet = InversionFaultSystemRupSetFactory.forBranch(branch);

				String str = faultSysRupSet.getPreInversionAnalysisData(index == 0);

				map.put(index, str);
			}
		}
	}

	@Override
	protected void doFinalAssembly() throws Exception {
		// gather the loss

		int[] myIndexes = new int[results.size()];
		String[] myStrings = new String[results.size()];
		int cnt = 0;
		for (Integer index : results.keySet()) {
			myIndexes[cnt] = index;
			myStrings[cnt++] = results.get(index);
		}

		int TAG_GET_NUM = 0;
		int TAG_GET_INDEXES = 1;
		int TAG_GET_EALS = 2;

		if (rank == 0) {
			String[] lines = new String[getNumTasks()];
			for (int i=0; i<lines.length; i++)
				lines[i] = null;

			for (int source=0; source<size; source++) {
				int[] srcIndexes;
				String[] srcStrings;

				if (source == rank) {
					srcIndexes = myIndexes;
					srcStrings = myStrings;
				} else {
					// ask for size
					int[] size = new int[1];
					MPI.COMM_WORLD.Recv(size, 0, 1, MPI.INT, source, TAG_GET_NUM);

					// get indices
					srcIndexes = new int[size[0]];
					MPI.COMM_WORLD.Recv(srcIndexes, 0, srcIndexes.length, MPI.INT, source, TAG_GET_INDEXES);

					// get eals
					srcStrings = new String[size[0]];
					MPI.COMM_WORLD.Recv(srcStrings, 0, srcStrings.length, MPI.OBJECT, source, TAG_GET_EALS);
				}

				for (int i=0; i<srcIndexes.length; i++) {
					lines[srcIndexes[i]] = srcStrings[i];
				}
			}

			for (String line : lines)
				Preconditions.checkNotNull(line);

			FileWriter fw = new FileWriter(file);
			
			CSVFile<String> csv = new CSVFile<String>(true);
			
			// split header
			
			for (String line : lines) {
				if (!line.endsWith("\n"))
					line += "\n";
				while (line.contains("\n")) {
					int ind = line.indexOf('\n');
					String subLine = line.substring(0, ind);
					fw.write(subLine+"\n");
					csv.addLine(Lists.newArrayList(Splitter.on('\t').split(subLine.trim())));
					line = line.substring(ind+1);
				}
			}
			fw.close();
			
			String csvFileName = file.getName();
			csvFileName = csvFileName.replaceAll(".txt", "")+".csv";
			csv.writeToFile(new File(file.getParentFile(), csvFileName));
		} else {
			int[] size = { results.size() };
			MPI.COMM_WORLD.Send(size, 0, 1, MPI.INT, 0, TAG_GET_NUM);

			// get indices
			MPI.COMM_WORLD.Send(myIndexes, 0, myIndexes.length, MPI.INT, 0, TAG_GET_INDEXES);

			// get eals
			MPI.COMM_WORLD.Send(myStrings, 0, myStrings.length, MPI.OBJECT, 0, TAG_GET_EALS);
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		args = MPJTaskCalculator.initMPJ(args);

		try {
			Options options = createOptions();

			CommandLine cmd = parse(options, args, MPJDistributedAnalysis.class);

			args = cmd.getArgs();

			Preconditions.checkArgument(args.length == 1, "Must specify output file!");

			File file = new File(args[0]);

			Preconditions.checkArgument(file.getAbsoluteFile().getParentFile().exists(), "File cannot be created: "+file);
			
			MPJDistributedAnalysis driver = new MPJDistributedAnalysis(cmd, file);
			
			driver.run();
			
			finalizeMPJ();
			
			System.exit(0);
		} catch (Throwable t) {
			abortAndExit(t);
		}
	}

}
