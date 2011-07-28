package scratch.UCERF3.simulatedAnnealing;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.time.StopWatch;

import scratch.UCERF3.simulatedAnnealing.completion.CompletionCriteria;
import scratch.UCERF3.simulatedAnnealing.completion.IterationCompletionCriteria;
import scratch.UCERF3.utils.MatrixIO;
import mpi.MPI;

public class DistributedSimulatedAnnealing {
	
	private static final boolean D = true;
	private static final SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss.SSS");

	private int rank;
	private int size;

	private SimulatedAnnealing annealer;

	private CompletionCriteria criteria;
	
	private long subIterations;
	
	private boolean startSubIterationsAtZero;

	private static final int TAG_BEST_RESULT = 6;

	private static final long WORK_DONE = -1;

	private double[] single_double_buf = new double[1];
	private long[] single_long_buf = new long[1];
	private boolean[] single_boolean_buf = new boolean[1];
	
	private StopWatch workWatch;
	private StopWatch totWatch;
	private StopWatch commWatch;

	public DistributedSimulatedAnnealing(CompletionCriteria criteria, long subIterations,
			boolean startSubIterationsAtZero,
			SimulatedAnnealing annealer) {
		rank = MPI.COMM_WORLD.Rank();
		size = MPI.COMM_WORLD.Size();
		
		debug("constructor start");
		
		this.annealer = annealer;
		this.criteria = criteria;
		
		this.subIterations = subIterations;
		this.startSubIterationsAtZero = startSubIterationsAtZero;
		
		debug("constructor end");
	}
	
	private void debug(String str) {
		if (!D)
			return;
		
		String print = "["+df.format(new Date());
		if (rank == 0)
			print += " MASTER]: "+str;
		else
			print += " WORKER "+rank+"]: "+str;
		System.out.println(print);
	}
	
	public boolean isMaster() {
		return rank == 0;
	}

	public void run() {
		debug("running");
		totWatch = new StopWatch();
		totWatch.start();
		if (isMaster())
			runMaster();
		else
			runWorker();
		totWatch.stop();
		debug("done running");
		if (D) {
			long totTime = totWatch.getTime();
			long workTime = workWatch.getTime();
			long commTime = commWatch.getTime();
			long otherTime = totTime - workTime - commTime;
			
			double totMins = totTime / 1000d / 60d;
			double workMins = workTime / 1000d / 60d;
			double commMins = commTime / 1000d / 60d;
			double otherMins = otherTime / 1000d / 60d;
			
			double workPercent = workMins / totMins * 100d;
			double commPercent = commMins / totMins * 100d;
			double otherPercent = otherMins / totMins * 100d;
			
			debug("Total run time: "+(float)totMins+" mins");
			debug("Work time: "+(float)workMins+" mins");
			debug("Work percentage: "+(float)workPercent+" %");
			debug("Communication time: "+(float)commMins+" mins");
			debug("Communication percentage: "+(float)commPercent+" %");
			debug("Other time: "+(float)otherMins+" mins");
			debug("Other percentage: "+(float)otherPercent+" %");
			
		}
		
		// make sure everyone is done before exiting.
		MPI.COMM_WORLD.Barrier();
	}
	
	private void bcastSingleLong(long val) {
		single_long_buf[0] = val;
		MPI.COMM_WORLD.Bcast(single_long_buf, 0, 1, MPI.LONG, 0);
	}

	private void runMaster() {
		debug("starting watch");
		StopWatch watch = new StopWatch();
		watch.start();
		
		double[] pool_double_buf = new double[size];
		boolean[] pool_boolean_buf = new boolean[size];
		
		double Ebest = Double.MAX_VALUE;
		
		int cnt = 0;
		long iter = 0;
		while (!criteria.isSatisfied(watch, iter, Ebest)) {
			debug("starting loop "+cnt+", iter: "+iter);
			
			// send num and start interations
			debug("sending num iterations");
			if (D) commWatch = new StopWatch();
			if (D) commWatch.start();
			bcastSingleLong(subIterations);
			if (D) commWatch.suspend();
			long startIter;
			if (startSubIterationsAtZero)
				startIter = 0;
			else
				startIter = iter;
			debug("sending start iteration");
			if (D) commWatch.resume();
			bcastSingleLong(startIter);
			if (D) commWatch.suspend();
			
			// do work yourself
			doWork(startIter, new IterationCompletionCriteria(startIter+subIterations));
			
			single_double_buf[0] = annealer.getBestEnergy();
			debug("my best energy: "+single_double_buf[0]);
			debug("gathering best energy");
			if (D) commWatch.resume();
			MPI.COMM_WORLD.Gather(single_double_buf, 0, 1, MPI.DOUBLE, pool_double_buf, 0, 1, MPI.DOUBLE, 0);
			if (D) commWatch.suspend();
			
			int bestRank = 0;
			
			for (int i=0; i<size; i++) {
				double energy = pool_double_buf[i];
				
				if (energy < Ebest) {
					Ebest = energy;
					bestRank = i;
				}
			}
			
			debug("Process "+bestRank+" has best solution with energy: "+Ebest);
			
			for (int i=0; i<size; i++)
				pool_boolean_buf[i] = i == bestRank;
			
			debug("sending report booleans");
			if (D) commWatch.resume();
			MPI.COMM_WORLD.Scatter(pool_boolean_buf, 0, 1, MPI.BOOLEAN, single_boolean_buf, 0, 1, MPI.BOOLEAN, 0);
			if (D) commWatch.suspend();
			
			// sent requests for results
			double[] solution;
			if (bestRank == 0) {
				debug("I already have the best solution!");
				solution = annealer.getBestSolution();
			} else {
				debug("gathering solution from "+bestRank);
				solution = new double[annealer.getBestSolution().length];
				if (D) commWatch.resume();
				MPI.COMM_WORLD.Recv(solution, 0, solution.length, MPI.DOUBLE, bestRank, TAG_BEST_RESULT);
				if (D) commWatch.suspend();
			}
			
			// distribute best energy
			single_double_buf[0] = Ebest;
			debug("distributing best energy");
			if (D) commWatch.resume();
			MPI.COMM_WORLD.Bcast(single_double_buf, 0, 1, MPI.DOUBLE, 0);
			if (D) commWatch.suspend();
			
			// distribute best solution
			debug("distributing best solution");
			if (D) commWatch.resume();
			MPI.COMM_WORLD.Bcast(solution, 0, solution.length, MPI.DOUBLE, 0);
			if (D) commWatch.suspend();
			
			// now set it myself
			debug("setting my own results");
			annealer.setResults(Ebest, solution);
			
			cnt++;
			iter += subIterations;
		}
		
		if (D) commWatch.resume();
		bcastSingleLong(WORK_DONE);
		if (D) commWatch.suspend();
		
		watch.stop();
		
		debug("DONE");
	}
	
	private void doWork(long startIter, CompletionCriteria criteria) {
		debug("starting my annealing. start Ebest: "+annealer.getBestEnergy()+
				", startIter: "+startIter+", criteria: "+criteria);
		if (workWatch == null) {
			workWatch = new StopWatch();
			workWatch.start();
		} else {
			workWatch.resume();
		}
		annealer.iterate(startIter, criteria);
		workWatch.suspend();
		debug("done with my annealing. Ebest: "+annealer.getBestEnergy());
	}

	private long getBcastLong() {
		// buf, offset, count, type, source, tag
//		MPI.COMM_WORLD.Recv(single_long_buf, 0, 1, MPI.LONG, from, tag);
		MPI.COMM_WORLD.Bcast(single_long_buf, 0, 1, MPI.LONG, 0);
		return single_long_buf[0];
	}

	private void runWorker() {
		debug("getting num iterations");
		long numIterations = getBcastLong();
		while (numIterations != WORK_DONE) {
			debug("getting start iteration");
			if (D) commWatch = new StopWatch();
			if (D) commWatch.start();
			long startIter = getBcastLong();
			if (D) commWatch.suspend();

			doWork(startIter, new IterationCompletionCriteria(startIter+numIterations));

			reportResults();
			
			fetchSolution();

			debug("getting num iterations");
			numIterations = getBcastLong();
		}
	}

	private void reportResults() {
		// send energy
		single_double_buf[0] = annealer.getBestEnergy();
		debug("sending my best energy ("+single_double_buf[0]+")");
		if (D) commWatch.resume();
		MPI.COMM_WORLD.Gather(single_double_buf, 0, 1, MPI.DOUBLE, null, 0, 1, MPI.DOUBLE, 0);
		if (D) commWatch.suspend();
//		MPI.COMM_WORLD.Send(single_double_buf, 0, 1, MPI.DOUBLE, 0, TAG_ENGERGY);

		// find out if we should send result
		debug("checking if i should send my result");
		if (D) commWatch.resume();
		MPI.COMM_WORLD.Scatter(null, 0, 1, MPI.BOOLEAN, single_boolean_buf, 0, 1, MPI.BOOLEAN, 0);
		if (D) commWatch.suspend();
//		MPI.COMM_WORLD.Recv(single_boolean_buf, 0, 1, MPI.BOOLEAN, 0,
//				TAG_SHOULD_SEND_RESULT);
		if (single_boolean_buf[0]) {
			// this means the master wants our result, lets report it
			double[] sol = annealer.getBestSolution();
			debug("sending my best solution");
			if (D) commWatch.resume();
			MPI.COMM_WORLD.Send(sol, 0, sol.length, MPI.DOUBLE, 0, TAG_BEST_RESULT);
			if (D) commWatch.suspend();
		}
	}
	
	private void fetchSolution() {
		// receive energy
		debug("receiving best energy");
//		MPI.COMM_WORLD.Recv(single_double_buf, 0, 1, MPI.DOUBLE, 0, TAG_BEST_ENGERGY);
		if (D) commWatch.resume();
		MPI.COMM_WORLD.Bcast(single_double_buf, 0, 1, MPI.DOUBLE, 0);
		if (D) commWatch.suspend();
		double Ebest = single_double_buf[0];
		
		double[] sol = new double[annealer.getBestSolution().length];
		debug("receiving best solution");
//		MPI.COMM_WORLD.Recv(sol, 0, sol.length, MPI.DOUBLE, 0, TAG_BEST_RESULT);
		if (D) commWatch.resume();
		MPI.COMM_WORLD.Bcast(sol, 0, sol.length, MPI.DOUBLE, 0);
		if (D) commWatch.suspend();
		int numZero = 0;
		for (int i=0; i<sol.length; i++)
			if (sol[i] == 0)
				numZero++;
		debug("num zero: "+numZero);
		
		debug("setting my own results");
		annealer.setResults(Ebest, sol);
	}
	
	public static Options createOptions() {
		Options options = ThreadedSimulatedAnnealing.createOptions();
		
		Option dsubIterOption = new Option("ds", "dist-sub-iterations", true,
				"number of distributed sub iterations (optional...defaults to subIterations)");
		dsubIterOption.setRequired(false);
		options.addOption(dsubIterOption);
		
		return options;
	}
	
	public static DistributedSimulatedAnnealing parseOptions(CommandLine cmd) throws IOException {
		ThreadedSimulatedAnnealing annealer = ThreadedSimulatedAnnealing.parseOptions(cmd);
		CompletionCriteria criteria = ThreadedSimulatedAnnealing.parseCompletionCriteria(cmd);
		
		int numSubIterations;
		if (cmd.hasOption("dist-sub-iterations"))
			numSubIterations = Integer.parseInt(cmd.getOptionValue("dist-sub-iterations"));
		else
			numSubIterations = annealer.getNumSubIterations();
		
		return new DistributedSimulatedAnnealing(criteria, numSubIterations,
				annealer.isStartSubIterationsAtZero(), annealer);
	}
	
	public static void main(String[] args) {
		args = MPI.Init(args);
		
		Options options = createOptions();
		
		CommandLineParser parser = new GnuParser();
		
		try {
			CommandLine cmd = parser.parse(options, args);
			
			DistributedSimulatedAnnealing dsa = parseOptions(cmd);
			
			File outputFile = new File(cmd.getOptionValue("solution-file"));
			
			dsa.run();
			
			if (dsa.isMaster()) {
				double[] solution = dsa.annealer.getBestSolution();
				
				System.out.println("Writing solution to: "+outputFile.getAbsolutePath());
				MatrixIO.doubleArrayToFile(solution, outputFile);
			}
			
			System.out.println("DONE...exiting.");
			MPI.Finalize();
			System.exit(0);
		} catch (MissingOptionException e) {
			System.err.println(e.getMessage());
			ThreadedSimulatedAnnealing.printHelp(options);
		} catch (ParseException e) {
			System.err.println("Error parsing command line arguments:");
			e.printStackTrace();
			ThreadedSimulatedAnnealing.printHelp(options);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

}
