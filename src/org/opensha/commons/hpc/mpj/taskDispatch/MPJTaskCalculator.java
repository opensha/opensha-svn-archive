package org.opensha.commons.hpc.mpj.taskDispatch;

import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.text.SimpleDateFormat;
import java.util.Date;

import mpi.MPI;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.opensha.commons.util.ClassUtils;

import scratch.kevin.DeadlockDetectionThread;

public abstract class MPJTaskCalculator {
	
	protected static final int TAG_READY_FOR_BATCH = 1;
	protected static final int TAG_NEW_BATCH_LENGH = 2;
	protected static final int TAG_NEW_BATCH = 3;
	
	private static final int MIN_DISPATCH_DEFAULT = 5;
	private static final int MAX_DISPATCH_DEFAULT = 100;
	
	public static final boolean D = true;
	protected static final SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss.SSS");
	
	protected int rank;
	protected int size;
	private int minDispatch;
	private int maxDispatch;
	private boolean rootDispatchOnly;
	private int numThreads;
	
	private DispatcherThread dispatcher;
	
	private static DeadlockDetectionThread deadlock;
	
	public MPJTaskCalculator(CommandLine cmd) {
		int numThreads = Runtime.getRuntime().availableProcessors();
		int minDispatch = MIN_DISPATCH_DEFAULT;
		int maxDispatch = MAX_DISPATCH_DEFAULT;
		boolean rootDispatchOnly = false;
		
		if (cmd.hasOption("threads"))
			numThreads = Integer.parseInt(cmd.getOptionValue("threads"));
		
		if (cmd.hasOption("min-dispatch"))
			minDispatch = Integer.parseInt(cmd.getOptionValue("min-dispatch"));
		
		if (cmd.hasOption("max-dispatch"))
			maxDispatch = Integer.parseInt(cmd.getOptionValue("max-dispatch"));
		
		if (cmd.hasOption("root-dispatch-only"))
			rootDispatchOnly = true;
		
		if (cmd.hasOption("deadlock")) {
			deadlock = new DeadlockDetectionThread(5000);
			deadlock.start();
		}
		
		init(numThreads, minDispatch, maxDispatch, rootDispatchOnly);
	}
	
	public MPJTaskCalculator(int numThreads, int minDispatch, int maxDispatch, boolean rootDispatchOnly) {
		init(numThreads, minDispatch, maxDispatch, rootDispatchOnly);
	}
	
	private void init(int numThreads, int minDispatch, int maxDispatch, boolean rootDispatchOnly) {
		this.rank = MPI.COMM_WORLD.Rank();
		this.size = MPI.COMM_WORLD.Size();
		this.numThreads = numThreads;
		this.minDispatch = minDispatch;
		this.maxDispatch = maxDispatch;
		this.rootDispatchOnly = rootDispatchOnly;
	}
	
	protected int getNumThreads() {
		return numThreads;
	}
	
	protected void debug(String message) {
		debug(rank, message);
	}
	
	protected static void debug(int rank, String message) {
		if (!D)
			return;
		
		System.out.println("["+df.format(new Date())+" Process "+rank+"]: "+message);
	}
	
	protected abstract int getNumTasks();
	
	public void run() throws IOException, InterruptedException {
		if (rank == 0) {
			// launch the dispatcher
			dispatcher = new DispatcherThread(size, getNumTasks(),
					minDispatch, maxDispatch);
			if (rootDispatchOnly) {
				debug(0, "starting dispatcher serially");
				dispatcher.run();
			} else {
				debug(0, "starting dispatcher threaded");
				dispatcher.start();
			}
		}
		
		int[] my_id = { rank };
		
		int[] batch_lengh_buf = new int[1];
		
		while (true) {
			if (rank == 0 && rootDispatchOnly)
				break;
			
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
			try {
				calculateBatch(batch);
			} catch (Exception e) {
				abortAndExit(e);
			}
		}
		
		debug("waiting for other processes with Barrier()");
		
		// wait for everyone
		MPI.COMM_WORLD.Barrier();
		try {
			doFinalAssembly();
		} catch (Exception e) {
			abortAndExit(e);
		}
		
		debug("Process "+rank+" DONE!");
	}
	
	protected abstract void calculateBatch(int[] batch) throws Exception;
	
	protected abstract void doFinalAssembly() throws Exception;
	
	protected static Options createOptions() {
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
		
		Option deadlockOption = new Option("dead", "deadlock", true,
				"If supplied, dedlock detection will be enabled (no recovery, however).");
		deadlockOption.setRequired(false);
		ops.addOption(deadlockOption);
		
		return ops;
	}
	
	protected static String[] initMPJ(String[] args) {
		Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
			
			@Override
			public void uncaughtException(Thread t, Throwable e) {
				abortAndExit(e);
			}
		});
		
		return MPI.Init(args);
	}
	
	protected static CommandLine parse(Options options, String args[], Class<?> clazz) {
		try {
			CommandLineParser parser = new GnuParser();
			
			CommandLine cmd = parser.parse(options, args);
			return cmd;
		} catch (Exception e) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp(
					ClassUtils.getClassNameWithoutPackage(clazz),
					options, true );
			abortAndExit(2);
			return null; // not accessible
		}
	}
	
	protected static void finalizeMPJ() {
		if (deadlock != null)
			deadlock.kill();
		MPI.Finalize();
		System.exit(0);
	}
	
	public static void abortAndExit(int ret) {
		abortAndExit(null, ret);
	}
	
	protected static void abortAndExit(Throwable t) {
		abortAndExit(t, 1);
	}
	
	protected static void abortAndExit(Throwable t, int ret) {
		if (t != null)
			t.printStackTrace();
		if (deadlock != null)
			deadlock.kill();
		MPI.COMM_WORLD.Abort(ret);
		System.exit(ret);
	}

}
