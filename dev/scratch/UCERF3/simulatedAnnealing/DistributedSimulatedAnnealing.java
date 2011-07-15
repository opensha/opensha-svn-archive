package scratch.UCERF3.simulatedAnnealing;

import java.io.File;
import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang.time.StopWatch;

import scratch.UCERF3.simulatedAnnealing.completion.CompletionCriteria;
import scratch.UCERF3.simulatedAnnealing.completion.IterationCompletionCriteria;
import scratch.UCERF3.utils.MatrixIO;
import mpi.MPI;

public class DistributedSimulatedAnnealing {
	
	private static final boolean D = true;

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
		if (rank == 0)
			str = "[MASTER]: "+str;
		else
			str = "[WORKER "+rank+"]: "+str;
		System.out.println(str);
	}
	
	public boolean isMaster() {
		return rank == 0;
	}

	public void run() {
		debug("running");
		if (isMaster())
			runMaster();
		else
			runWorker();
		debug("done running");
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
			bcastSingleLong(subIterations);
			long startIter;
			if (startSubIterationsAtZero)
				startIter = 0;
			else
				startIter = iter;
			debug("sending start iteration");
			bcastSingleLong(startIter);
			
			// do work yourself
			doWork(startIter, new IterationCompletionCriteria(startIter+subIterations));
			
			single_double_buf[0] = annealer.getBestEnergy();
			debug("my best energy: "+single_double_buf[0]);
			debug("gathering best energy");
			MPI.COMM_WORLD.Gather(single_double_buf, 0, 1, MPI.DOUBLE, pool_double_buf, 0, 1, MPI.DOUBLE, 0);
			
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
			MPI.COMM_WORLD.Scatter(pool_boolean_buf, 0, 1, MPI.BOOLEAN, single_boolean_buf, 0, 1, MPI.BOOLEAN, 0);
			
			// sent requests for results
			double[] solution;
			if (bestRank == 0) {
				debug("I already have the best solution!");
				solution = annealer.getBestSolution();
			} else {
				debug("gathering solution from "+bestRank);
				solution = new double[annealer.getBestSolution().length];
				MPI.COMM_WORLD.Recv(solution, 0, solution.length, MPI.DOUBLE, bestRank, TAG_BEST_RESULT);
			}
			
			// distribute best energy
			single_double_buf[0] = Ebest;
			debug("distributing best energy");
			MPI.COMM_WORLD.Bcast(single_double_buf, 0, 1, MPI.DOUBLE, 0);
			
			// distribute best solution
			debug("distributing best solution");
			MPI.COMM_WORLD.Bcast(solution, 0, solution.length, MPI.DOUBLE, 0);
			
			// now set it myself
			debug("setting my own results");
			annealer.setResults(Ebest, solution);
			
			cnt++;
			iter += subIterations;
		}
		
		watch.stop();
		
		debug("DONE");
	}
	
	private void doWork(long startIter, CompletionCriteria criteria) {
		debug("starting my annealing. start Ebest: "+annealer.getBestEnergy()+
				", startIter: "+startIter+", criteria: "+criteria);
		annealer.iterate(startIter, criteria);
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
			long startIter = getBcastLong();

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
		MPI.COMM_WORLD.Gather(single_double_buf, 0, 1, MPI.DOUBLE, null, 0, 1, MPI.DOUBLE, 0);
//		MPI.COMM_WORLD.Send(single_double_buf, 0, 1, MPI.DOUBLE, 0, TAG_ENGERGY);

		// find out if we should send result
		debug("checking if i should send my result");
		MPI.COMM_WORLD.Scatter(null, 0, 1, MPI.BOOLEAN, single_boolean_buf, 0, 1, MPI.BOOLEAN, 0);
//		MPI.COMM_WORLD.Recv(single_boolean_buf, 0, 1, MPI.BOOLEAN, 0,
//				TAG_SHOULD_SEND_RESULT);
		if (single_boolean_buf[0]) {
			// this means the master wants our result, lets report it
			double[] sol = annealer.getBestSolution();
			debug("sending my best solution");
			MPI.COMM_WORLD.Send(sol, 0, sol.length, MPI.DOUBLE, 0, TAG_BEST_RESULT);
		}
	}
	
	private void fetchSolution() {
		// receive energy
		debug("receiving best energy");
//		MPI.COMM_WORLD.Recv(single_double_buf, 0, 1, MPI.DOUBLE, 0, TAG_BEST_ENGERGY);
		MPI.COMM_WORLD.Bcast(single_double_buf, 0, 1, MPI.DOUBLE, 0);
		double Ebest = single_double_buf[0];
		
		double[] sol = new double[annealer.getBestSolution().length];
		debug("receiving best solution");
//		MPI.COMM_WORLD.Recv(sol, 0, sol.length, MPI.DOUBLE, 0, TAG_BEST_RESULT);
		MPI.COMM_WORLD.Bcast(sol, 0, sol.length, MPI.DOUBLE, 0);
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
