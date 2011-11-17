package scratch.UCERF3.simulatedAnnealing;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.time.StopWatch;
import org.opensha.commons.util.ClassUtils;

import com.google.common.base.Preconditions;

import scratch.UCERF3.simulatedAnnealing.completion.CompletionCriteria;
import scratch.UCERF3.simulatedAnnealing.completion.CompoundCompletionCriteria;
import scratch.UCERF3.simulatedAnnealing.completion.EnergyCompletionCriteria;
import scratch.UCERF3.simulatedAnnealing.completion.IterationCompletionCriteria;
import scratch.UCERF3.simulatedAnnealing.completion.ProgressTrackingCompletionCriteria;
import scratch.UCERF3.simulatedAnnealing.completion.TimeCompletionCriteria;
import scratch.UCERF3.simulatedAnnealing.params.CoolingScheduleType;
import scratch.UCERF3.simulatedAnnealing.params.GenerationFunctionType;
import scratch.UCERF3.simulatedAnnealing.params.NonnegativityConstraintType;
import scratch.UCERF3.utils.MatrixIO;

import cern.colt.matrix.tdouble.DoubleMatrix2D;
import cern.colt.matrix.tdouble.impl.SparseCCDoubleMatrix2D;

public class ThreadedSimulatedAnnealing implements SimulatedAnnealing {
	
	private static final boolean D = false;
	
	public static final String XML_METADATA_NAME= "ThreadedSimulatedAnnealing";
	
	private CompletionCriteria subCompetionCriteria;
	private boolean startSubIterationsAtZero;
	
	private int numThreads;
	private ArrayList<SerialSimulatedAnnealing> sas;
	
	private double Ebest = Double.MAX_VALUE;
	private double[] xbest = null;
	
	public ThreadedSimulatedAnnealing(
			DoubleMatrix2D A, double[] d, double[] initialState,
			int numThreads, CompletionCriteria subCompetionCriteria) {
		// SA inputs are checked in SA constructor, no need to dupliate checks
		
		Preconditions.checkArgument(numThreads > 0, "numThreads must be > 0");
		Preconditions.checkNotNull(subCompetionCriteria, "subCompetionCriteria cannot be null");
		
		this.numThreads = numThreads;
		this.subCompetionCriteria = subCompetionCriteria;
		
		sas = new ArrayList<SerialSimulatedAnnealing>();
		for (int i=0; i<numThreads; i++)
			sas.add(new SerialSimulatedAnnealing(A, d, initialState));
	}
	
	public CompletionCriteria getSubCompetionCriteria() {
		return subCompetionCriteria;
	}
	
	public boolean isStartSubIterationsAtZero() {
		return startSubIterationsAtZero;
	}
	
	public void setStartSubIterationsAtZero(boolean startSubIterationsAtZero) {
		this.startSubIterationsAtZero = startSubIterationsAtZero;
	}
	
	protected static CompletionCriteria getForStartIter(long startIter, CompletionCriteria subComp) {
		if (subComp instanceof IterationCompletionCriteria) {
			long iters = ((IterationCompletionCriteria)subComp).getMinIterations();
			subComp = new IterationCompletionCriteria(startIter + iters);
		}
		return subComp;
	}
	
	private class SAThread extends Thread {
		private SimulatedAnnealing sa;
		private CompletionCriteria subComp;
		private long startIter;
		private long endIter;
		
		public SAThread(SimulatedAnnealing sa, long startIter, CompletionCriteria subComp) {
			this.sa = sa;
			this.subComp = subComp;
			this.startIter = startIter;
		}
		
		@Override
		public void run() {
			endIter = sa.iterate(startIter, getForStartIter(startIter, subComp));
		}
	}

	@Override
	public void setCalculationParams(CoolingScheduleType coolingFunc,
			NonnegativityConstraintType nonnegativeityConstraintAlgorithm,
			GenerationFunctionType perturbationFunc) {
		for (SerialSimulatedAnnealing sa : sas)
			sa.setCalculationParams(coolingFunc, nonnegativeityConstraintAlgorithm, perturbationFunc);
	}

	@Override
	public CoolingScheduleType getCoolingFunc() {
		return sas.get(0).getCoolingFunc();
	}

	@Override
	public void setCoolingFunc(CoolingScheduleType coolingFunc) {
		for (SerialSimulatedAnnealing sa : sas)
			sa.setCoolingFunc(coolingFunc);
	}

	@Override
	public NonnegativityConstraintType getNonnegativeityConstraintAlgorithm() {
		return sas.get(0).getNonnegativeityConstraintAlgorithm();
	}

	@Override
	public void setNonnegativeityConstraintAlgorithm(
			NonnegativityConstraintType nonnegativeityConstraintAlgorithm) {
		for (SerialSimulatedAnnealing sa : sas)
			sa.setNonnegativeityConstraintAlgorithm(nonnegativeityConstraintAlgorithm);
	}

	@Override
	public GenerationFunctionType getPerturbationFunc() {
		return sas.get(0).getPerturbationFunc();
	}

	@Override
	public void setPerturbationFunc(GenerationFunctionType perturbationFunc) {
		for (SerialSimulatedAnnealing sa : sas)
			sa.setPerturbationFunc(perturbationFunc);
	}

	@Override
	public double[] getBestSolution() {
		return xbest;
	}

	@Override
	public double getBestEnergy() {
		return Ebest;
	}

	@Override
	public void setResults(double Ebest, double[] xbest) {
		// TODO revisit
		this.Ebest = Ebest;
		this.xbest = xbest;
		for (SerialSimulatedAnnealing sa : sas)
			sa.setResults(Ebest, xbest);
	}

	@Override
	public long iterate(long numIterations) {
		return iterate(0l, new IterationCompletionCriteria(numIterations));
	}

	@Override
	public long iterate(CompletionCriteria completion) {
		return iterate(0l, completion);
	}

	@Override
	public long iterate(long startIter, CompletionCriteria criteria) {
		if (D) System.out.println("Threaded Simulated Annealing starting with "+numThreads
				+" threads, "+criteria+", SUB: "+subCompetionCriteria);
		
		StopWatch watch = new StopWatch();
		watch.start();
		
		int rounds = 0;
		long iter = startIter;
		while (!criteria.isSatisfied(watch, iter, Ebest)) {
			ArrayList<SAThread> threads = new ArrayList<ThreadedSimulatedAnnealing.SAThread>();
			
			// create the threads
			for (int i=0; i<numThreads; i++) {
				long start;
				if (startSubIterationsAtZero)
					start = 0l;
				else
					start = iter;
				threads.add(new SAThread(sas.get(i), start, subCompetionCriteria));
			}
			
			// start the threads
			for (Thread t : threads) {
				t.start();
			}
			
			// join the threads
			for (Thread t : threads) {
				try {
					t.join();
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
			}
			
			for (int i=0; i<numThreads; i++) {
				SimulatedAnnealing sa = sas.get(i);
				double E = sa.getBestEnergy();
				if (E < Ebest) {
					Ebest = E;
					xbest = sa.getBestSolution();
				}
				
				// now set the current iteration count to the max iteration acheived
				long endIter = threads.get(i).endIter;
				if (endIter > iter)
					iter = endIter;
			}
			
			rounds++;
			// this is now done in the loop above
//			iter += numSubIterations;
			
			if (D) {
				double secs = watch.getTime() / 1000d;
				System.out.println("Threaded total round "+rounds+" DONE after "
						+(float)secs+" seconds, "+iter+" total iterations. Best energy: "+Ebest);
			}
			
			for (SimulatedAnnealing sa : sas)
				sa.setResults(Ebest, xbest);
		}
		
		watch.stop();
		
		if(D) {
			System.out.println("Threaded annealing schedule completed.");
			double runSecs = watch.getTime() / 1000d;
			System.out.println("Done with Inversion after " + (float)runSecs + " seconds.");
			System.out.println("Rounds: "+rounds);
			System.out.println("Total Iterations: "+iter);
			System.out.println("Best energy: "+Ebest);
		}
		
		return iter;
	}
	
	protected static Options createOptions() {
		Options ops = SerialSimulatedAnnealing.createOptions();
		
		// REQUIRED
		Option aMatrix = new Option("a", "a-matrix-file", true, "A matrix file");
		aMatrix.setRequired(true);
		ops.addOption(aMatrix);
		
		Option dMatrix = new Option("d", "d-matrix-file", true, "D matrix file");
		dMatrix.setRequired(true);
		ops.addOption(dMatrix);
		
		Option subOption = new Option("s", "sub-completion", true, "number of sub iterations. Optionally, append 's'" +
		" to specify in seconds or 'm' to specify in minutes instead of iterations.");
		subOption.setRequired(true);
		ops.addOption(subOption);
		
		Option numThreadsOption = new Option("t", "num-threads", true, "number of threads (percentage of available" +
				" can also be specified, for example, '50%')");
		numThreadsOption.setRequired(true);
		ops.addOption(numThreadsOption);
		
		Option solutionFileOption = new Option("sol", "solution-file", true, "file to store solution");
		solutionFileOption.setRequired(true);
		ops.addOption(solutionFileOption);
		
		// Completion Criteria
		Option timeOption = new Option("time", "completion-time", true, "time to anneal. append 's' for secionds," +
				"'m' for minutes, 'h' for hours. default is millis.");
		timeOption.setRequired(false);
		ops.addOption(timeOption);
		
		Option iterOption = new Option("iter", "completion-iterations", true, "num iterations to anneal");
		iterOption.setRequired(false);
		ops.addOption(iterOption);
		
		Option energyOption = new Option("energy", "completion-energy", true, "energy maximum to anneal to");
		energyOption.setRequired(false);
		ops.addOption(energyOption);
		
		// other
		Option initial = new Option("i", "initial-state-file", true, "initial state file" +
				" (optional...default is all zeros)");
		initial.setRequired(false);
		ops.addOption(initial);
		
		Option progressFileOption = new Option("p", "progress-file", true, "file to store progress results");
		progressFileOption.setRequired(false);
		ops.addOption(progressFileOption);
		
		Option subIterationsStartOption = new Option("zero", "start-sub-iters-zero", false,
				"flag to start all sub iterations at zero instead of the true iteration count");
		subIterationsStartOption.setRequired(false);
		ops.addOption(subIterationsStartOption);
		
		return ops;
	}
	
	public static String subCompletionCriteriaToArgument(CompletionCriteria subCompletion) {
		return subCompletionCriteriaToArgument("sub-completion", subCompletion);
	}
	
	public static String subCompletionCriteriaToArgument(String argName, CompletionCriteria subCompletion) {
		return "--"+argName+" "+subCompletionArgVal(subCompletion);
	}
	
	public static String subCompletionArgVal(CompletionCriteria subCompletion) {
		if (subCompletion instanceof IterationCompletionCriteria)
			return ""+((IterationCompletionCriteria)subCompletion).getMinIterations();
		else if (subCompletion instanceof TimeCompletionCriteria)
			return ((TimeCompletionCriteria)subCompletion).getTimeStr();
		else
			throw new UnsupportedOperationException("Can't create command line argument for: "+subCompletion);
	}
	
	public static String completionCriteriaToArgument(CompletionCriteria criteria) {
		if (criteria instanceof EnergyCompletionCriteria) {
			return "--completion-energy "+((EnergyCompletionCriteria)criteria).getMaxEnergy();
		} else if (criteria instanceof IterationCompletionCriteria) {
			return "--completion-iterations "+((IterationCompletionCriteria)criteria).getMinIterations();
		} else if (criteria instanceof TimeCompletionCriteria) {
			return "--completion-time "+((TimeCompletionCriteria)criteria).getTimeStr();
		} else if (criteria instanceof CompoundCompletionCriteria) {
			String str = null;
			for (CompletionCriteria subCriteria : ((CompoundCompletionCriteria)criteria).getCriterias()) {
				if (str == null)
					str = "";
				else
					str += " ";
				str += completionCriteriaToArgument(subCriteria);
			}
			return str;
		} else if (criteria instanceof ProgressTrackingCompletionCriteria) {
			throw new IllegalArgumentException("ProgressTrackingCompletionCriteria not supported," +
					"use --progress-file instead");
		} else
			throw new UnsupportedOperationException("Can't create command line argument for: "+criteria);
	}
	
	protected static CompletionCriteria parseCompletionCriteria(CommandLine cmd) {
		ArrayList<CompletionCriteria> criterias = new ArrayList<CompletionCriteria>();
		
		if (cmd.hasOption("time")) {
			String timeStr = cmd.getOptionValue("time");
			long time;
			if (timeStr.toLowerCase().endsWith("s"))
				time = (long)(Double.parseDouble(timeStr.substring(0, timeStr.length()-1)) * 1000);
			else if (timeStr.toLowerCase().endsWith("m"))
				time = (long)(Double.parseDouble(timeStr.substring(0, timeStr.length()-1)) * 1000 * 60);
			else if (timeStr.toLowerCase().endsWith("h"))
				time = (long)(Double.parseDouble(timeStr.substring(0, timeStr.length()-1)) * 1000 * 60 * 60);
			else
				time = Long.parseLong(timeStr);
			
			criterias.add(new TimeCompletionCriteria(time));
		}
		if (cmd.hasOption("iter"))
			criterias.add(new IterationCompletionCriteria(Long.parseLong(cmd.getOptionValue("iter"))));
		if (cmd.hasOption("energy"))
			criterias.add(new IterationCompletionCriteria(Long.parseLong(cmd.getOptionValue("energy"))));
		
		CompletionCriteria criteria;
		if (criterias.size() == 0)
			throw new IllegalArgumentException("must specify at least one completion criteria!");
		else if (criterias.size() == 1)
			criteria = criterias.get(0);
		else
			criteria = new CompoundCompletionCriteria(criterias);
		
		if (cmd.hasOption("progress-file")) {
			File progressFile = new File(cmd.getOptionValue("progress-file"));
			criteria = new ProgressTrackingCompletionCriteria(criteria, progressFile);
		}
		
		return criteria;
	}
	
	public static CompletionCriteria parseSubCompletionCriteria(String optionVal) {
		if (optionVal.endsWith("s") || optionVal.endsWith("m") || optionVal.endsWith("h") || optionVal.endsWith("mi")) {
			return TimeCompletionCriteria.fromTimeString(optionVal);
		}
		return new IterationCompletionCriteria(Long.parseLong(optionVal));
	}
	
	public static int parseNumThreads(String threadsVal) {
		if (threadsVal.endsWith("%")) {
			threadsVal = threadsVal.substring(0, threadsVal.length()-1);
			double threadPercent = Double.parseDouble(threadsVal);
			int avail = Runtime.getRuntime().availableProcessors();
			double threadDouble = avail * threadPercent * 0.01;
			int numThreads = (int)(threadDouble + 0.5);
			System.out.println("Percentage based threads..."+threadsVal+"% of "+avail+" = "
					+threadDouble+" = "+numThreads);
			
			return numThreads < 1 ? 1 : numThreads;
		}
		return Integer.parseInt(threadsVal);
	}
	
	public static ThreadedSimulatedAnnealing parseOptions(CommandLine cmd) throws IOException {
		File aFile = new File(cmd.getOptionValue("a"));
		if (D) System.out.println("Loading A matrix from: "+aFile.getAbsolutePath());
		DoubleMatrix2D A = MatrixIO.loadSparse(aFile, SparseCCDoubleMatrix2D.class);
		
		File dFile = new File(cmd.getOptionValue("d"));
		if (D) System.out.println("Loading d matrix from: "+dFile.getAbsolutePath());
		double[] d = MatrixIO.doubleArrayFromFile(dFile);
		
		double[] initialState;
		if (cmd.hasOption("i")) {
			File initialFile = new File(cmd.getOptionValue("i"));
			if (D) System.out.println("Loading initialState from: "+initialFile.getAbsolutePath());
			initialState = MatrixIO.doubleArrayFromFile(initialFile);
		} else {
			initialState = new double[A.columns()]; // use default initial state of all zeros
		}
		
		CompletionCriteria subCompletionCriteria = parseSubCompletionCriteria(cmd.getOptionValue("s"));
		
		int numThreads = parseNumThreads(cmd.getOptionValue("t"));
		
		ThreadedSimulatedAnnealing tsa =
			new ThreadedSimulatedAnnealing(A, d, initialState, numThreads, subCompletionCriteria);
		
		for (SerialSimulatedAnnealing sa : tsa.sas)
			sa.setCalculationParamsFromOptions(cmd);
		
		if (cmd.hasOption("zero"))
			tsa.setStartSubIterationsAtZero(true);
		
		return tsa;
	}
	
	public static void printHelp(Options options) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp(
				ClassUtils.getClassNameWithoutPackage(ThreadedSimulatedAnnealing.class),
				options, true );
		System.exit(2);
	}
	
	public static void main(String[] args) {
		Options options = createOptions();
		
		CommandLineParser parser = new GnuParser();
		
		try {
			CommandLine cmd = parser.parse(options, args);
			
			ThreadedSimulatedAnnealing tsa = parseOptions(cmd);
			
			File outputFile = new File(cmd.getOptionValue("solution-file"));
			
			CompletionCriteria criteria = parseCompletionCriteria(cmd);
			
			tsa.iterate(criteria);
			
			double[] solution = tsa.getBestSolution();
			
			System.out.println("Writing solution to: "+outputFile.getAbsolutePath());
			MatrixIO.doubleArrayToFile(solution, outputFile);
			
			System.out.println("DONE...exiting.");
			System.exit(0);
		} catch (MissingOptionException e) {
			System.err.println(e.getMessage());
			printHelp(options);
		} catch (ParseException e) {
			System.err.println("Error parsing command line arguments:");
			e.printStackTrace();
			printHelp(options);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

}
