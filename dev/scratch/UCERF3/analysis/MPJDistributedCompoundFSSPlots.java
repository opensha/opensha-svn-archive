package scratch.UCERF3.analysis;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import mpi.MPI;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.opensha.commons.geo.Region;
import org.opensha.commons.hpc.mpj.taskDispatch.MPJTaskCalculator;
import org.opensha.commons.util.ExceptionUtils;
import org.opensha.commons.util.FileUtils;
import org.opensha.commons.util.threads.Task;
import org.opensha.commons.util.threads.ThreadedTaskComputer;

import scratch.UCERF3.CompoundFaultSystemSolution;
import scratch.UCERF3.FaultSystemSolutionFetcher;
import scratch.UCERF3.analysis.CompoundFSSPlots.PaleoFaultPlot;
import scratch.UCERF3.analysis.CompoundFSSPlots.PlotSolComputeTask;
import scratch.UCERF3.analysis.CompoundFSSPlots.RegionalMFDPlot;
import scratch.UCERF3.inversion.CommandLineInversionRunner;
import scratch.UCERF3.logicTree.APrioriBranchWeightProvider;
import scratch.UCERF3.logicTree.BranchWeightProvider;
import scratch.UCERF3.logicTree.LogicTreeBranch;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

public class MPJDistributedCompoundFSSPlots extends MPJTaskCalculator {
	
	private FaultSystemSolutionFetcher fetcher;
	private List<LogicTreeBranch> branches;
	private List<CompoundFSSPlots> plots;
	
	private boolean invFSS;
	
	private int threads;
	
	private int myCalcs = 0;

	public MPJDistributedCompoundFSSPlots(CommandLine cmd, FaultSystemSolutionFetcher fetcher,
			List<CompoundFSSPlots> plots) {
		super(cmd);
		Preconditions.checkState(!plots.isEmpty(), "No plots specified!");
		
		branches = Lists.newArrayList();
		branches.addAll(fetcher.getBranches());
		
		invFSS = false;
		for (CompoundFSSPlots plot : plots) {
			if (plot.usesInversionFSS()) {
				invFSS = true;
				break;
			}
		}
		
		this.fetcher = fetcher;
		this.plots = plots;
		this.threads = getNumThreads();
		
		// InvFSS objects use tons of memory
		if (invFSS && threads > 4)
			threads = 4;
	}

	@Override
	protected int getNumTasks() {
		return branches.size();
	}

	@Override
	protected void calculateBatch(int[] batch) throws Exception {
		List<Task> tasks = Lists.newArrayList();
		
		for (int index : batch) {
			LogicTreeBranch branch = branches.get(index);
			tasks.add(new PlotSolComputeTask(plots, fetcher, branch, invFSS));
		}
		
		System.out.println("Making "+plots.size()+" plot(s) with "+tasks.size()+" branches");
		
		ThreadedTaskComputer comp = new ThreadedTaskComputer(tasks);
		try {
			comp.computThreaded(threads);
		} catch (InterruptedException e) {
			ExceptionUtils.throwAsRuntimeException(e);
		}
		myCalcs += batch.length;
	}

	@Override
	protected void doFinalAssembly() throws Exception {
		System.out.println(rank+". My number of calcs: "+myCalcs);
		int numPlots = plots.size();
		
		CompoundFSSPlots[] sendbuf = new CompoundFSSPlots[numPlots];
		for (int i=0; i<numPlots; i++)
			if (myCalcs > 0)
				sendbuf[i] = plots.get(i);
			else
				sendbuf[i] = null;
		
//		for (CompoundFSSPlots obj : sendbuf) {
//			if (obj != null) {
//				File tempFile = File.createTempFile("openSHA", "serial");
//				System.out.println(rank+". Test serializing to file: "+tempFile.getAbsolutePath());
//				try {
//					FileUtils.saveObjectInFile(tempFile.getAbsolutePath(), obj);
//					FileUtils.loadObject(tempFile.getAbsolutePath());
//					tempFile.delete();
//				} catch (Exception e) {
//					System.out.println("Error serializing with rank "+rank);
//					e.printStackTrace();
//				}
//			}
//		}
		
//		String[] sendbuf = new String[numPlots];
//		for (int i=0; i<numPlots; i++)
//			sendbuf[i] = rank+","+i;
		
		int recvcount = numPlots * size;
		CompoundFSSPlots[] recvbuf;
		if (rank == 0)
			recvbuf = new CompoundFSSPlots[recvcount];
		else
			recvbuf = null;
		
//		String[] recvbuf;
//		if (rank == 0)
//			recvbuf = new String[recvcount];
//		else
//			recvbuf = null;
		
//		System.out.println("Sendbuf size: "+numPlots);
//		System.out.println("Reczbuf size: "+recvcount);
		
//		MPI.COMM_WORLD.Gather(sendbuf, 0, numPlots, MPI.OBJECT, recvbuf, 0, numPlots, MPI.OBJECT, 0);
		List<List<CompoundFSSPlots>> otherPlotsList = Lists.newArrayList();
		for (int p=0; p<numPlots; p++)
			otherPlotsList.add(new ArrayList<CompoundFSSPlots>());
		if (rank == 0) {
			for (int source=1; source<size; source++) {
				System.out.println("Receiving from "+source);
				MPI.COMM_WORLD.Recv(sendbuf, 0, sendbuf.length, MPI.OBJECT, source, 0);
				for (int p=0; p<numPlots; p++)
					if (sendbuf[p] != null)
						otherPlotsList.get(p).add(sendbuf[p]);
			}
			for (int p=0; p<numPlots; p++)
				plots.get(p).combineDistributedCalcs(otherPlotsList.get(p));
			
			for (CompoundFSSPlots plot : plots)
				plot.finalizePlot();
		} else {
			MPI.COMM_WORLD.Send(sendbuf, 0, sendbuf.length, MPI.OBJECT, 0, 0);
		}
		
//		if (rank == 0) {
////			for (int i=0; i<recvbuf.length; i++)
////				System.out.println(i+": "+recvbuf[i]);
////			abortAndExit(0);
//			for (int p=0; p<numPlots; p++) {
//				Collection<CompoundFSSPlots> otherPlots = Lists.newArrayList();
//				for (int i=0; i<size; i++) {
//					// TODO uncomment
//					CompoundFSSPlots otherPlot = recvbuf[i*numPlots+p];
//					if (otherPlot != null)
//						otherPlots.add(otherPlot);
//				}
//				plots.get(p).combineDistributedCalcs(otherPlots);
//			}
//			
//			for (CompoundFSSPlots plot : plots)
//				plot.finalizePlot();
//		}
	}
	
	protected static Options createOptions() {
		Options options = MPJTaskCalculator.createOptions();
		
		Option mfdOption = new Option("mfd", "plot-mfds", false, "Flag for plotting MFDs");
		mfdOption.setRequired(false);
		options.addOption(mfdOption);
		
		Option paleoFaultOption = new Option("paleofault", "plot-paleo-faults", false,
				"Flag for plotting paleo faults");
		paleoFaultOption.setRequired(false);
		options.addOption(paleoFaultOption);
		
		Option randomSampleOption = new Option("rand", "random-sample", true,
				"If supplied, a random sample of the given size will be used.");
		randomSampleOption.setRequired(false);
		options.addOption(randomSampleOption);
		
		return options;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		args = MPJTaskCalculator.initMPJ(args);

		try {
			Options options = createOptions();

			CommandLine cmd = parse(options, args, MPJDistributedCompoundFSSPlots.class);

			args = cmd.getArgs();

			Preconditions.checkArgument(args.length == 2, "Must specify inputfile file/output dir!");

			File inputFile = new File(args[0]);
			File dir = new File(args[1]);
			
			if (!dir.exists())
				dir.mkdir();

			Preconditions.checkArgument(inputFile.exists(), "Input file doesn't exist!: "+inputFile);
			
			FaultSystemSolutionFetcher fetcher = CompoundFaultSystemSolution.fromZipFile(inputFile);
			if (cmd.hasOption("rand")) {
				int num = Integer.parseInt(cmd.getOptionValue("rand"));
				fetcher = FaultSystemSolutionFetcher.getRandomSample(fetcher, num);
			}
			
			BranchWeightProvider weightProvider = new APrioriBranchWeightProvider();
			
			List<CompoundFSSPlots> plots = Lists.newArrayList();
			
			if (cmd.hasOption("mfd")) {
				List<Region> regions = RegionalMFDPlot.getDefaultRegions();
				RegionalMFDPlot mfd = new RegionalMFDPlot(weightProvider, regions);
				plots.add(mfd);
			}
			
			if (cmd.hasOption("paleofault")) {
				PaleoFaultPlot paleo = new PaleoFaultPlot(weightProvider);
				plots.add(paleo);
			}
			
			MPJDistributedCompoundFSSPlots driver = new MPJDistributedCompoundFSSPlots(cmd, fetcher, plots);
			
			driver.run();
			
			String prefix = inputFile.getName();
			if (prefix.endsWith(".zip"))
				prefix = prefix.substring(0, prefix.indexOf(".zip"));
			
			if (driver.rank == 0) {
				for (CompoundFSSPlots plot : plots) {
					if (plot instanceof RegionalMFDPlot) {
						RegionalMFDPlot mfd = (RegionalMFDPlot)plot;
						
						CompoundFSSPlots.writeRegionalMFDPlots(mfd.getSpecs(), mfd.getRegions(), dir, prefix);
					} else if (plot instanceof PaleoFaultPlot) {
						PaleoFaultPlot paleo = (PaleoFaultPlot)plot;
						File paleoPlotsDir = new File(dir, CommandLineInversionRunner.PALEO_FAULT_BASED_DIR_NAME);
						if (!paleoPlotsDir.exists())
							paleoPlotsDir.mkdir();
						CompoundFSSPlots.writePaleoFaultPlots(paleo.getPlotsMap(), paleoPlotsDir);
					}
				}
			}
			
			finalizeMPJ();
			
			System.exit(0);
		} catch (Throwable t) {
			abortAndExit(t);
		}
	}

}
