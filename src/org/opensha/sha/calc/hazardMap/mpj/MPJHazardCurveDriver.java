package org.opensha.sha.calc.hazardMap.mpj;

import java.io.File;
import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.dom4j.Document;
import org.opensha.commons.data.Site;
import org.opensha.commons.util.ClassUtils;
import org.opensha.commons.util.XMLUtils;
import org.opensha.sha.calc.hazardMap.HazardCurveSetCalculator;
import org.opensha.sha.calc.hazardMap.ThreadedHazardCurveSetCalculator;
import org.opensha.sha.calc.hazardMap.components.CalculationInputsXMLFile;

import scratch.UCERF3.simulatedAnnealing.ThreadedSimulatedAnnealing;

import com.google.common.base.Preconditions;

import mpi.MPI;

public class MPJHazardCurveDriver {
	
	protected static final int TAG_READY_FOR_BATCH = 1;
	protected static final int TAG_NEW_BATCH_LENGH = 2;
	protected static final int TAG_NEW_BATCH = 3;
	
	private static final int MIN_DISPATCH_DEFAULT = 5;
	private static final int MAX_DISPATCH_DEFAULT = 100;
	
	public static final boolean D = true;
	private static final SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss.SSS");
	
	private HazardCurveSetCalculator[] calcs;
	private List<Site> sites;
	
	private int rank;
	
	private DispatcherThread dispatcher;
	
	public MPJHazardCurveDriver(int rank, HazardCurveSetCalculator[] calcs, List<Site> sites, int minDispatch, int maxDispatch) {
		init(rank, calcs, sites, minDispatch, maxDispatch);
	}
	
	private void init(int rank, HazardCurveSetCalculator[] calcs, List<Site> sites, int minDispatch, int maxDispatch) {
		Preconditions.checkNotNull(calcs, "calcs cannot be null!");
		Preconditions.checkArgument(calcs.length > 0, "calcs cannot be empty!");
		for (HazardCurveSetCalculator calc : calcs)
			Preconditions.checkNotNull(calc, "calc cannot be null!");
		Preconditions.checkNotNull(sites, "sites cannot be null!");
		Preconditions.checkArgument(!sites.isEmpty(), "sites cannot be empty!");
		this.calcs = calcs;
		this.sites = sites;
		this.rank = rank;
	}
	
	private void setDispatcher(DispatcherThread dispatcher) {
		this.dispatcher = dispatcher;
	}
	
	private void debug(String message) {
		debug(rank, message);
	}
	
	private static void debug(int rank, String message) {
		if (!D)
			return;
		
		System.out.println("["+df.format(new Date())+" Process "+rank+"]: "+message);
	}
	
	public void run() throws IOException, InterruptedException {
		
		ThreadedHazardCurveSetCalculator threadCalc = new ThreadedHazardCurveSetCalculator(calcs);
		
		int[] my_id = { rank };
		
		int[] batch_lengh_buf = new int[1];
		
		while (true) {
			int[] batch;
			if (dispatcher == null) {
				// this is a non-root thread, use MPJ to get the next batch
				
				debug("sending READY message");
				// report to dispatcher as ready
				MPI.COMM_WORLD.Send(my_id, 0, 1, MPI.INT, 0, TAG_READY_FOR_BATCH);
				
				debug("receiving batch lengh");
				// receive a new batch length
				MPI.COMM_WORLD.Recv(batch_lengh_buf, 0, 1, MPI.INT, 0, TAG_NEW_BATCH_LENGH);
				
				if (batch_lengh_buf[0] == 0) {
					debug("DONE!");
					// we're done
					break;
				}
				
				batch = new int[batch_lengh_buf[0]];
				
				debug("receiving batch of length "+batch.length);
				MPI.COMM_WORLD.Recv(batch, 0, batch.length, MPI.INT, 0, TAG_NEW_BATCH);
			} else {
				debug("getting next batch directly");
				batch = dispatcher.getNextBatch();
				
				if (batch == null || batch.length == 0) {
					debug("DONE!");
					// we're done
					break;
				}
			}
			
			// now calculate the batch
			debug("calculating batch");
			threadCalc.calculateCurves(sites, batch);
		}
		
		System.out.println("Process "+rank+" DONE!");
	}
	
	private static Options createOptions() {
		Options ops = new Options();
		
		Option threadsOption = new Option("t", "threads", true,
				"Number of calculation threads on each node. Default is the number" +
				" of available processors (in this case: "+Runtime.getRuntime().availableProcessors()+")");
		threadsOption.setRequired(false);
		ops.addOption(threadsOption);
		
		Option minDispatchOption = new Option("min", "min-dispatch", true, "Minimum number of tasks to dispatch" +
				" to a compute node at a time. Default: "+MIN_DISPATCH_DEFAULT);
		minDispatchOption.setRequired(false);
		ops.addOption(minDispatchOption);
		
		Option maxDispatchOption = new Option("max", "max-dispatch", true, "Maximum number of tasks to dispatch" +
				" to a compute node at a time. Actual tasks per node will never be greater than the number of" +
				" sites divided by the number of nodes. Default: "+MAX_DISPATCH_DEFAULT);
		maxDispatchOption.setRequired(false);
		ops.addOption(maxDispatchOption);
		
		Option rootDispatchOnlyOption = new Option("rdo", "root-dispatch-only", false, "Flag for root node only" +
				"dispatching tasks and not calculating itself");
		rootDispatchOnlyOption.setRequired(false);
		ops.addOption(rootDispatchOnlyOption);
		
		return ops;
	}
	
	public static void main(String[] args) {
		Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
			
			@Override
			public void uncaughtException(Thread t, Throwable e) {
				abortAndExit(e);
			}
		});
		
		try {
			args = MPI.Init(args);
			
			Options options = createOptions();
			
			CommandLineParser parser = new GnuParser();
			
			CommandLine cmd = null;
			int numThreads = Runtime.getRuntime().availableProcessors();
			int minDispatch = MIN_DISPATCH_DEFAULT;
			int maxDispatch = MAX_DISPATCH_DEFAULT;
			boolean rootDispatchOnly = false;
			try {
				cmd = parser.parse(options, args);
				
				if (cmd.hasOption("threads"))
					numThreads = Integer.parseInt(cmd.getOptionValue("threads"));
				
				if (cmd.hasOption("min-dispatch"))
					minDispatch = Integer.parseInt(cmd.getOptionValue("min-dispatch"));
				
				if (cmd.hasOption("max-dispatch"))
					maxDispatch = Integer.parseInt(cmd.getOptionValue("max-dispatch"));
				
				if (cmd.hasOption("root-dispatch-only"))
					rootDispatchOnly = true;
			} catch (Exception e1) {
				HelpFormatter formatter = new HelpFormatter();
				formatter.printHelp(
						ClassUtils.getClassNameWithoutPackage(ThreadedSimulatedAnnealing.class),
						options, true );
				System.exit(2);
			}
			
			args = cmd.getArgs();
			
			if (args.length != 1) {
				System.err.println("USAGE: HazardCurveDriver [<options>] <XML input file>");
				MPI.COMM_WORLD.Abort(2);
				System.exit(2);
			}
			
			int rank = MPI.COMM_WORLD.Rank();
			int size = MPI.COMM_WORLD.Size();
			
			File xmlFile = new File(args[0]);
			
			if (!xmlFile.exists()) {
				throw new IOException("XML Input file '" + args[0] + "' not found!");
			}
			
			Document doc = XMLUtils.loadDocument(xmlFile.getAbsolutePath());
			
			Preconditions.checkArgument(numThreads >= 1, "threads must be >= 1. you supplied: "+numThreads);
			
			debug(rank, "loading inputs for "+numThreads+" threads");
			CalculationInputsXMLFile[] inputs = CalculationInputsXMLFile.loadXML(doc, numThreads);
			List<Site> sites = inputs[0].getSites();
			
			DispatcherThread dispatch = null;
			
			if (rank == 0) {
				// launch the dispatcher
				dispatch = new DispatcherThread(size, sites.size(),
						minDispatch, maxDispatch);
				if (rootDispatchOnly) {
					debug(0, "starting dispatcher serially");
					dispatch.run();
				} else {
					debug(0, "starting dispatcher threaded");
					dispatch.start();
				}
			}
			
			if (rank != 0 || !rootDispatchOnly) {
				HazardCurveSetCalculator[] calcs = new HazardCurveSetCalculator[numThreads];
				for (int i=0; i<inputs.length; i++)
					calcs[i] = new HazardCurveSetCalculator(inputs[i]);
				
				MPJHazardCurveDriver driver = new MPJHazardCurveDriver(rank, calcs, sites,
						minDispatch, maxDispatch);
				
				driver.setDispatcher(dispatch);
				
				driver.run();
			}
			
			MPI.Finalize();
			System.exit(0);
		} catch (Throwable t) {
			abortAndExit(t);
		}
	}
	
	protected static void abortAndExit(Throwable t) {
		t.printStackTrace();
		MPI.COMM_WORLD.Abort(1);
		System.exit(1);
	}

}
