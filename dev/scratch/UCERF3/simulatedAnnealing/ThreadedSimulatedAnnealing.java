package scratch.UCERF3.simulatedAnnealing;

import java.awt.Color;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.math.stat.StatUtils;
import org.opensha.commons.data.CSVFile;
import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.commons.data.function.DiscretizedFunc;
import org.opensha.commons.data.function.EvenlyDiscretizedFunc;
import org.opensha.commons.gui.plot.PlotLineType;
import org.opensha.commons.util.ClassUtils;
import org.opensha.sha.gui.infoTools.HeadlessGraphPanel;
import org.opensha.sha.gui.infoTools.PlotCurveCharacterstics;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.primitives.Doubles;

import scratch.UCERF3.inversion.InversionInputGenerator;
import scratch.UCERF3.simulatedAnnealing.completion.CompletionCriteria;
import scratch.UCERF3.simulatedAnnealing.completion.CompoundCompletionCriteria;
import scratch.UCERF3.simulatedAnnealing.completion.EnergyChangeCompletionCriteria;
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
	
	private static final int plot_width = 1000;
	private static final int plot_height = 800;
	
	private CompletionCriteria subCompetionCriteria;
	private boolean startSubIterationsAtZero;
	private TimeCompletionCriteria checkPointCriteria;
	private File checkPointFileBase;
	
	private int numThreads;
	private ArrayList<SerialSimulatedAnnealing> sas;
	
	private double[] Ebest =  { Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY,
			Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY };
	private double[] xbest = null;
	private double[] misfit = null;
	private double[] misfit_ineq = null;
	private double[] minimumRuptureRates = null;
	
	private List<Integer> rangeEndRows;
	private List<String> rangeNames;
	
	public ThreadedSimulatedAnnealing(
			DoubleMatrix2D A, double[] d, double[] initialState,
			int numThreads, CompletionCriteria subCompetionCriteria) {
		this(A, d, initialState, 0d, null, null, null, numThreads, subCompetionCriteria);
	}
	
	public ThreadedSimulatedAnnealing(
			DoubleMatrix2D A, double[] d, double[] initialState, double relativeSmoothnessWt, 
			DoubleMatrix2D A_ineq,  double[] d_ineq, double[] minimumRuptureRates,
			int numThreads, CompletionCriteria subCompetionCriteria) {
		// SA inputs are checked in SA constructor, no need to duplicate checks
		
		Preconditions.checkArgument(numThreads > 0, "numThreads must be > 0");
		Preconditions.checkNotNull(subCompetionCriteria, "subCompetionCriteria cannot be null");
		
		this.numThreads = numThreads;
		this.subCompetionCriteria = subCompetionCriteria;
		this.minimumRuptureRates = minimumRuptureRates;
		
		sas = new ArrayList<SerialSimulatedAnnealing>();
		for (int i=0; i<numThreads; i++)
			sas.add(new SerialSimulatedAnnealing(A, d, initialState, relativeSmoothnessWt, A_ineq, d_ineq));
		
		xbest = sas.get(0).getBestSolution();
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
	
	public void setCheckPointCriteria(TimeCompletionCriteria checkPointCriteria, File checkPointFilePrefix) {
		this.checkPointCriteria = checkPointCriteria;
		this.checkPointFileBase = checkPointFilePrefix;
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
		private long startPerturbs;
		private long endPerturbs;
		
		private boolean fatal = false;
		private Throwable t;
		
		public SAThread(SimulatedAnnealing sa, long startIter, long startPerturbs, CompletionCriteria subComp) {
			this.sa = sa;
			this.subComp = subComp;
			this.startIter = startIter;
			this.startPerturbs = startPerturbs;
		}
		
		@Override
		public void run() {
			try {
				long[] ret = sa.iterate(startIter, startPerturbs, getForStartIter(startIter, subComp));
				endIter = ret[0];
				endPerturbs = ret[1];
			} catch (Throwable t) {
				System.err.println("FATAL ERROR in thread!");
				t.printStackTrace();
				fatal = true;
				this.t = t;
			}
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
	public void setVariablePerturbationBasis(double[] variablePerturbBasis) {
		for (SerialSimulatedAnnealing sa : sas)
			sa.setVariablePerturbationBasis(variablePerturbBasis);
	}

	@Override
	public double[] getBestSolution() {
		return xbest;
	}

	@Override
	public double[] getBestEnergy() {
		return Ebest;
	}
	
	@Override
	public double[] getBestMisfit() {
		return misfit;
	}

	@Override
	public double[] getBestInequalityMisfit() {
		return misfit_ineq;
	}
	
	@Override
	public void setResults(double[] Ebest, double[] xbest) {
		setResults(Ebest, xbest, null, null);
	}

	@Override
	public void setResults(double[] Ebest, double[] xbest, double[] misfit, double[] misfit_ineq) {
		// TODO revisit
		this.Ebest = Ebest;
		this.xbest = xbest;
		this.misfit = misfit;
		this.misfit_ineq = misfit_ineq;
		for (SerialSimulatedAnnealing sa : sas)
			sa.setResults(Ebest, xbest, misfit, misfit_ineq);
	}

	@Override
	public long iterate(long numIterations) {
		return iterate(0l, 0l, new IterationCompletionCriteria(numIterations))[0];
	}

	@Override
	public long iterate(CompletionCriteria completion) {
		return iterate(0l, 0l, completion)[0];
	}

	@Override
	public long[] iterate(long startIter, long startPerturbs, CompletionCriteria criteria) {
		if (D) System.out.println("Threaded Simulated Annealing starting with "+numThreads
				+" threads, "+criteria+", SUB: "+subCompetionCriteria);
		
		if (criteria instanceof ProgressTrackingCompletionCriteria
				&& rangeNames != null && !rangeNames.isEmpty())
			((ProgressTrackingCompletionCriteria)criteria).setRangeNames(rangeNames);
		
		StopWatch watch = new StopWatch();
		watch.start();
		StopWatch checkPointWatch = null;
		long numCheckPoints = 0;
		if (checkPointCriteria != null) {
			checkPointWatch = new StopWatch();
			checkPointWatch.start();
		}
		long perturbs = startPerturbs;
		
		int rounds = 0;
		long iter = startIter;
		while (!criteria.isSatisfied(watch, iter, Ebest, perturbs)) {
			if (checkPointCriteria != null &&
					checkPointCriteria.isSatisfied(checkPointWatch, iter, Ebest, perturbs)) {
				numCheckPoints++;
				System.out.println("Writing checkpoint after "+iter+" iterations. Ebest: "
						+Doubles.join(", ", Ebest));
				long millis = checkPointCriteria.getMillis();
				millis *= numCheckPoints;
				String name = checkPointFileBase.getName()+"_checkpoint_"
						+TimeCompletionCriteria.getTimeStr(millis);
				File checkPointFile = new File(checkPointFileBase.getParentFile(), name+".bin");
				try {
					writeBestSolution(checkPointFile);
					writeRateVsRankPlot(new File(checkPointFile.getParentFile(), name));
				} catch (IOException e) {
					// don't fail on a checkpoint, just continue
					e.printStackTrace();
				}
				checkPointWatch.reset();
				checkPointWatch.start();
			}
			
			ArrayList<SAThread> threads = new ArrayList<ThreadedSimulatedAnnealing.SAThread>();
			
			// create the threads
			for (int i=0; i<numThreads; i++) {
				long start;
				if (startSubIterationsAtZero)
					start = 0l;
				else
					start = iter;
				threads.add(new SAThread(sas.get(i), start, perturbs, subCompetionCriteria));
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
				SAThread thread = threads.get(i);
				if (thread.fatal)
					throw new RuntimeException(thread.t);
				SimulatedAnnealing sa = sas.get(i);
				double[] E = sa.getBestEnergy();
				if (E[0] < Ebest[0]) {
					Ebest = E;
					xbest = sa.getBestSolution();
					misfit = sa.getBestMisfit();
					misfit_ineq = sa.getBestInequalityMisfit();
					// set the number of perturbations to the perturbation count
					// of the solution we're actually keeping
					perturbs = thread.endPerturbs;
				}
				
				// now set the current iteration count to the max iteration achieved
				long endIter = thread.endIter;
				if (endIter > iter)
					iter = endIter;
			}
			
			rounds++;
			// this is now done in the loop above
//			iter += numSubIterations;
			
			if (D) {
				double secs = watch.getTime() / 1000d;
				System.out.println("Threaded total round "+rounds+" DONE after "
						+(float)secs+" seconds, "+iter+" total iterations.");
				System.out.println("Best energy after "+perturbs+" total perturbations: "
						+Doubles.join(", ", Ebest));
			}
			
			for (SimulatedAnnealing sa : sas)
				sa.setResults(Ebest, xbest, misfit, misfit_ineq);
		}
		
		watch.stop();
		
		if(D) {
			System.out.println("Threaded annealing schedule completed.");
			double runSecs = watch.getTime() / 1000d;
			System.out.println("Done with Inversion after " + (float)runSecs + " seconds.");
			System.out.println("Rounds: "+rounds);
			System.out.println("Total Iterations: "+iter);
			System.out.println("Total Perturbations: "+perturbs);
			System.out.println("Best energy: "+Doubles.join(", ", Ebest));
		}
		
		long[] ret = { iter, perturbs };
		return ret;
	}
	
	public int getNumThreads() {
		return numThreads;
	}
	
	public void setRanges(List<Integer> rangeEndRows, List<String> rangeNames) {
		if (rangeEndRows == null) {
			Preconditions.checkArgument(rangeNames == null);
		} else {
			Preconditions.checkNotNull(rangeNames);
			Preconditions.checkArgument(rangeEndRows.size() == rangeNames.size(),
					"range sizes inconsistant!");
			int prev = 0;
			for (int i=0; i<rangeEndRows.size(); i++) {
				int cur = rangeEndRows.get(i);
				Preconditions.checkState(cur >= prev);
				prev = cur;
			}
		}
		this.rangeEndRows = rangeEndRows;
		this.rangeNames = rangeNames;
		for (SerialSimulatedAnnealing sa : sas)
			sa.setEqualityRangeEnds(rangeEndRows);
	}
	
	public static Options createOptionsNoInputs() {
		Options ops = SerialSimulatedAnnealing.createOptions();
		
		Option subOption = new Option("s", "sub-completion", true, "number of sub iterations. Optionally, append 's'" +
		" to specify in seconds or 'm' to specify in minutes instead of iterations.");
		subOption.setRequired(true);
		ops.addOption(subOption);
		
		Option numThreadsOption = new Option("t", "num-threads", true, "number of threads (percentage of available" +
				" can also be specified, for example, '50%')");
		numThreadsOption.setRequired(true);
		ops.addOption(numThreadsOption);
		
		Option solutionFileOption = new Option("sol", "solution-file", true, "file to store solution");
		solutionFileOption.setRequired(false);
		ops.addOption(solutionFileOption);
		
		// Completion Criteria
		Option timeOption = new Option("time", "completion-time", true, "time to anneal. append 's' for secionds," +
				" 'm' for minutes, 'h' for hours. default is millis.");
		timeOption.setRequired(false);
		ops.addOption(timeOption);
		
		Option iterOption = new Option("iter", "completion-iterations", true, "num iterations to anneal");
		iterOption.setRequired(false);
		ops.addOption(iterOption);
		
		Option energyOption = new Option("energy", "completion-energy", true, "energy maximum to anneal to");
		energyOption.setRequired(false);
		ops.addOption(energyOption);
		
		Option deltaEnergyOption = new Option("delenergy", "completion-delta-energy", true, "energy change completion" +
				" criteria. Format: <time>,<%>,<diff>. For example: 60,1.5,2 means a look back period of 60 minutes," +
				" a minimum percent improvement of 1.5%, and a minimum actual energy change of 2.");
		deltaEnergyOption.setRequired(false);
		ops.addOption(deltaEnergyOption);
		
		// constraint weights
		Option smoothnessWeightOption = new Option("smoothness", "smoothness-weight", true, "weight for the entropy constraint");
		smoothnessWeightOption.setRequired(false);
		ops.addOption(smoothnessWeightOption);
		
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
		
		Option checkPointOption = new Option("chk", "checkpoint", true, "will write out solutions on the given time " +
				"interval. append 's' for secionds, 'm' for minutes, 'h' for hours. default is millis.");
		checkPointOption.setRequired(false);
		ops.addOption(checkPointOption);
		
		Option plotsOption = new Option("plot", "plots", false, "write a variety of plots to the filesystem" +
				" when annealing has completed");
		plotsOption.setRequired(false);
		ops.addOption(plotsOption);
		
		return ops;
	}
	
	public static Options createOptions() {
		Options ops = createOptionsNoInputs();
		
		// REQUIRED
		// inputs can now be supplied in a single zip file if needed, thus individual ones not required
		Option aMatrix = new Option("a", "a-matrix-file", true, "A matrix file");
		aMatrix.setRequired(false);
		ops.addOption(aMatrix);
		
		Option dMatrix = new Option("d", "d-matrix-file", true, "D matrix file");
		dMatrix.setRequired(false);
		ops.addOption(dMatrix);
		
		Option a_MFDMatrix = new Option("aineq", "a-ineq-matrix-file", true, "A inequality matrix file");
		a_MFDMatrix.setRequired(false);
		ops.addOption(a_MFDMatrix);
		
		Option d_MFDMatrix = new Option("dineq", "d-ineq-matrix-file", true, "D inequality matrix file");
		d_MFDMatrix.setRequired(false);
		ops.addOption(d_MFDMatrix);
		
		Option minimumRates = new Option("minrates", "minimum-rates-file", true, "minimum rates files to apply solution after annealing");
		minimumRates.setRequired(false);
		ops.addOption(minimumRates);
		
		Option zipInputs = new Option("zip", "zip-file", true, "Zip file containing all inputs. " +
				"File names must be a.bin, d.bin, and optionally: initial.bin, a_ineq.bin, d_ineq.bin, minimumRuptureRates.bin, metadata.txt");
		zipInputs.setRequired(false);
		ops.addOption(zipInputs);
		
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
	
	public static CompletionCriteria parseCompletionCriteria(CommandLine cmd) {
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
			criterias.add(new EnergyCompletionCriteria(Double.parseDouble(cmd.getOptionValue("energy"))));
		if (cmd.hasOption("delenergy"))
			criterias.add(EnergyChangeCompletionCriteria.fromCommandLineArgument(
					cmd.getOptionValue("delenergy")));
		
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
		DoubleMatrix2D A = null; // can't stay null
		double[] d = null; // can't stay null
		double[] initialState = null; // can be null, for now
		DoubleMatrix2D A_ineq = null; // can be null
		double[] d_ineq = null; // can be null
		double[] minimumRuptureRates = null; // can be null
		
		List<Integer> rangeEndRows = null;
		List<String> rangeNames = null;
		
		if (cmd.hasOption("zip")) {
			File zipFile = new File(cmd.getOptionValue("zip"));
			if (D) System.out.println("Opening zip file: "+zipFile.getAbsolutePath());
			ZipFile zip = new ZipFile(zipFile);
			
			ZipEntry a_entry = zip.getEntry("a.bin");
			A = MatrixIO.loadSparse(new BufferedInputStream(zip.getInputStream(a_entry)), SparseCCDoubleMatrix2D.class);
			ZipEntry d_entry = zip.getEntry("d.bin");
			d = MatrixIO.doubleArrayFromInputStream(new BufferedInputStream(zip.getInputStream(d_entry)), A.rows()*8);
			
			ZipEntry a_ineq_entry = zip.getEntry("a_ineq.bin");
			if (a_ineq_entry != null)
				A_ineq = MatrixIO.loadSparse(new BufferedInputStream(zip.getInputStream(a_ineq_entry)), SparseCCDoubleMatrix2D.class);
			ZipEntry d_ineq_entry = zip.getEntry("d_ineq.bin");
			if (d_ineq_entry != null && A_ineq != null)
				d_ineq = MatrixIO.doubleArrayFromInputStream(new BufferedInputStream(zip.getInputStream(d_ineq_entry)), A_ineq.rows()*8);
			
			ZipEntry initial_entry = zip.getEntry("initial.bin");
			if (initial_entry != null) {
				initialState = MatrixIO.doubleArrayFromInputStream(
						new BufferedInputStream(zip.getInputStream(initial_entry)), A.columns()*8);
			}
			
			ZipEntry minimumRuptureRates_entry = zip.getEntry("minimumRuptureRates.bin");
			if (minimumRuptureRates_entry != null)
				minimumRuptureRates = MatrixIO.doubleArrayFromInputStream(zip.getInputStream(minimumRuptureRates_entry), A.columns()*8);
			
			ZipEntry rangeEntry = zip.getEntry("energyRanges.csv");
			if (rangeEntry != null) {
				CSVFile<String> rangeCSV = CSVFile.readStream(zip.getInputStream(rangeEntry), true);
				rangeEndRows = Lists.newArrayList();
				rangeNames = Lists.newArrayList();
				for (int row=0; row<rangeCSV.getNumRows(); row++) {
					rangeEndRows.add(Integer.parseInt(rangeCSV.get(row, 0)));
					rangeNames.add(rangeCSV.get(row, 1));
				}
			}
		} else {
			File aFile = new File(cmd.getOptionValue("a"));
			if (D) System.out.println("Loading A matrix from: "+aFile.getAbsolutePath());
			A = MatrixIO.loadSparse(aFile, SparseCCDoubleMatrix2D.class);
			
			File dFile = new File(cmd.getOptionValue("d"));
			if (D) System.out.println("Loading d matrix from: "+dFile.getAbsolutePath());
			d = MatrixIO.doubleArrayFromFile(dFile);
			
			if (cmd.hasOption("aineq")) {
				File a_ineqFile = new File(cmd.getOptionValue("aineq"));
				if (D) System.out.println("Loading A_ineq matrix from: "+a_ineqFile.getAbsolutePath());
				A_ineq = MatrixIO.loadSparse(a_ineqFile, SparseCCDoubleMatrix2D.class);
			}
			
			if (cmd.hasOption("dineq")) {
				File d_ineqFile = new File(cmd.getOptionValue("dineq"));
				if (D) System.out.println("Loading d_ineq matrix from: "+d_ineqFile.getAbsolutePath());
				d_ineq = MatrixIO.doubleArrayFromFile(d_ineqFile);
			}
			
			if (cmd.hasOption("i")) {
				File initialFile = new File(cmd.getOptionValue("i"));
				if (D) System.out.println("Loading initialState from: "+initialFile.getAbsolutePath());
				initialState = MatrixIO.doubleArrayFromFile(initialFile);
			}
			
			if (cmd.hasOption("minrates")); {
				File minimumRuptureRatesFile = new File(cmd.getOptionValue("minrates"));
				if (D) System.out.println("Loading minimumRuptureRates from: "+minimumRuptureRatesFile.getAbsolutePath());
				minimumRuptureRates = MatrixIO.doubleArrayFromFile(minimumRuptureRatesFile);
			}
		}
		
		return parseOptions(cmd, A, d, initialState, A_ineq, d_ineq, minimumRuptureRates,
				rangeEndRows, rangeNames);
	}
	
	public static ThreadedSimulatedAnnealing parseOptions(CommandLine cmd,
			DoubleMatrix2D A,
			double[] d,
			double[] initialState,
			DoubleMatrix2D A_ineq,
			double[] d_ineq,
			double[] minimumRuptureRates,
			List<Integer> rangeEndRows,
			List<String> rangeNames) throws IOException {
		
		// load other weights
		double relativeSmoothnessWt;
		if (cmd.hasOption("smoothness"))
			relativeSmoothnessWt = Double.parseDouble(cmd.getOptionValue("smoothness"));
		else
			relativeSmoothnessWt = 0;
		
		if (initialState ==  null)
			// if we still don't have an initial state, use all zeros
			initialState = new double[A.columns()];
		
		CompletionCriteria subCompletionCriteria = parseSubCompletionCriteria(cmd.getOptionValue("s"));
		
		int numThreads = parseNumThreads(cmd.getOptionValue("t"));
		
		ThreadedSimulatedAnnealing tsa =
			new ThreadedSimulatedAnnealing(A, d, initialState,
					relativeSmoothnessWt, A_ineq, d_ineq, minimumRuptureRates, numThreads, subCompletionCriteria);
		
		for (SerialSimulatedAnnealing sa : tsa.sas)
			sa.setCalculationParamsFromOptions(cmd);
		
		if (cmd.hasOption("zero"))
			tsa.setStartSubIterationsAtZero(true);
		
		if (cmd.hasOption("checkpoint")) {
			String time = cmd.getOptionValue("checkpoint");
			TimeCompletionCriteria checkPointCriteria = TimeCompletionCriteria.fromTimeString(time);
			String outputStr = cmd.getOptionValue("solution-file");
			File checkPointFilePrefix = getFileWithoutBinSuffix(outputStr);
			tsa.setCheckPointCriteria(checkPointCriteria, checkPointFilePrefix);
		}
		
		tsa.setRanges(rangeEndRows, rangeNames);
		
		return tsa;
	}
	
	private static File getFileWithoutBinSuffix(String path) {
		if (path.endsWith(".bin"))
			path = path.substring(0, path.lastIndexOf(".bin"));
		return new File(path);
	}
	
	public static void printHelp(Options options) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp(
				ClassUtils.getClassNameWithoutPackage(ThreadedSimulatedAnnealing.class),
				options, true );
		System.exit(2);
	}
	
	public void writeBestSolution(File outputFile) throws IOException {
		double[] solution = getBestSolution();
		if (minimumRuptureRates != null) {
			String outputFilePath = outputFile.getAbsolutePath();
			if (outputFilePath.endsWith(".bin"))
				outputFilePath = outputFilePath.substring(0, outputFilePath.length()-4);
			File outputOrigFile = new File(outputFilePath+"_noMinRates.bin");
			System.out.println("Writing original solution to: "+outputOrigFile.getAbsolutePath());
			MatrixIO.doubleArrayToFile(solution, outputOrigFile);
			
			System.out.println("Applying minimum rupture rates");
			solution = InversionInputGenerator.adjustSolutionForMinimumRates(solution, minimumRuptureRates);
		}
		
		System.out.println("Writing solution to: "+outputFile.getAbsolutePath());
		MatrixIO.doubleArrayToFile(solution, outputFile);
	}
	
	private static double[] getSorted(double[] rates) {
		double[] newrates = Arrays.copyOf(rates, rates.length);
		Arrays.sort(newrates);
		return newrates;
	}
	
	private void writeRateVsRankPlot(File prefix) throws IOException {
		double[] solutionRates = getSorted(getBestSolution());
		double[] adjustedRates = null;
		if (minimumRuptureRates != null) {
			adjustedRates =
				InversionInputGenerator.adjustSolutionForMinimumRates(getBestSolution(), minimumRuptureRates);
			adjustedRates = getSorted(adjustedRates);
		}
		EvenlyDiscretizedFunc func = new EvenlyDiscretizedFunc(0d, solutionRates.length, 1d);
		int cnt = 0;
		for (int i=solutionRates.length; --i >= 0;)
			func.set(cnt++, solutionRates[i]);
		ArrayList<DiscretizedFunc> funcs = new ArrayList<DiscretizedFunc>();
		funcs.add(func);
		ArrayList<PlotCurveCharacterstics> chars = Lists.newArrayList(
//				new PlotCurveCharacterstics(PlotLineType.SOLID, 1f, PlotSymbol.CIRCLE, 5f, Color.BLACK));
				new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, Color.BLACK));
		if (adjustedRates != null) {
			EvenlyDiscretizedFunc adjFunc = new EvenlyDiscretizedFunc(0d, solutionRates.length, 1d);
			cnt = 0;
			for (int i=adjustedRates.length; --i >= 0;)
				adjFunc.set(cnt++, adjustedRates[i]);
			funcs.add(0, adjFunc);
			chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 4f, Color.BLUE));
		}
		HeadlessGraphPanel gp = new HeadlessGraphPanel();
		gp.setBackgroundColor(Color.WHITE);
		gp.setYLog(true);
		gp.drawGraphPanel("Rank", "Rate", funcs, chars, false, "Rupture Rate Distribution");
		File file = new File(prefix.getParentFile(), prefix.getName()+"_rate_dist.png");
		gp.saveAsPNG(file.getAbsolutePath(), plot_width, plot_height);
	}
	
	private void writeProgressPlots(ProgressTrackingCompletionCriteria track, File prefix) throws IOException {
		ArbitrarilyDiscretizedFunc perturbsVsIters = new ArbitrarilyDiscretizedFunc();
		
		ArrayList<double[]> energies = track.getEnergies();
		ArrayList<Long> times = track.getTimes();
		ArrayList<Long> perturbs = track.getPerturbs();
		ArrayList<Long> iters = track.getIterations();
		
		int num = energies.get(0).length;
		ArbitrarilyDiscretizedFunc[] energyVsTime = new ArbitrarilyDiscretizedFunc[num];
		ArbitrarilyDiscretizedFunc[] energyVsIters = new ArbitrarilyDiscretizedFunc[num];
		for (int i=0; i<num; i++) {
			energyVsTime[i] = new ArbitrarilyDiscretizedFunc();
			energyVsIters[i] = new ArbitrarilyDiscretizedFunc();
		}
		
		for (int i=0; i<energies.size(); i++) {
			double[] energy = energies.get(i);
			long time = times.get(i);
			double mins = time / 1000d / 60d;
			long perturb = perturbs.get(i);
			long iter = iters.get(i);
			
			for (int j=0; j<energy.length; j++) {
				energyVsTime[j].set(mins, energy[j]);
				energyVsIters[j].set((double)iter, energy[j]);
			}
			perturbsVsIters.set((double)iter, (double)perturb);
		}
		
		ArrayList<PlotCurveCharacterstics> energyChars = getEnergyBreakdownChars();
		
		PlotCurveCharacterstics perturbChar =
			new PlotCurveCharacterstics(PlotLineType.SOLID, 4f, Color.CYAN);
		
		String timeLabel = "Time (minutes)";
		String iterationsLabel = "Iterations";
		String energyLabel = "Energy";
		
		HeadlessGraphPanel gp = new HeadlessGraphPanel();
		gp.setBackgroundColor(Color.WHITE);
		
		// this chops off any huge energy values in the first 5% of the run so that the plots
		// are readable at the energy levels that are actually interesting
		double energyAter5percent = energyVsIters[1].getY((int)((energyVsIters[1].getNum()-1d)*0.05 + 0.5));
		double energyPlotMax = energyAter5percent*1.2;
		double energyPlotMin = 0;
		double timeMin = 0, itersMin = 0;
		double timeMax = energyVsTime[0].getMaxX()*1.1, iterMax = energyVsIters[0].getMaxX();
		
		// energy vs time plot
		gp.setUserBounds(timeMin, timeMax, energyPlotMin, energyPlotMax);
		gp.drawGraphPanel(timeLabel, energyLabel, Lists.newArrayList(energyVsTime), energyChars,
				true, "Energy Vs Time");
		gp.saveAsPNG(new File(prefix.getParentFile(),
				prefix.getName()+"_energy_vs_time.png").getAbsolutePath(),
				plot_width, plot_height);
		
		// energy vs iters plot
		gp.setUserBounds(itersMin, iterMax, energyPlotMin, energyPlotMax);
		gp.drawGraphPanel(iterationsLabel, energyLabel, Lists.newArrayList(energyVsIters), energyChars,
				true, "Energy Vs Time");
		gp.saveAsPNG(new File(prefix.getParentFile(),
				prefix.getName()+"_energy_vs_iters.png").getAbsolutePath(),
				plot_width, plot_height);
		
		// perturbations vs iters plots
		ArrayList<ArbitrarilyDiscretizedFunc> perturbWrap = new ArrayList<ArbitrarilyDiscretizedFunc>();
		perturbWrap.add(perturbsVsIters);
		gp.drawGraphPanel(iterationsLabel, "Perturbations", perturbWrap,
				Lists.newArrayList(perturbChar), false, "Perturbations Vs Iters");
		gp.saveAsPNG(new File(prefix.getParentFile(),
				prefix.getName()+"_perturb_vs_iters.png").getAbsolutePath(),
				plot_width, plot_height);
		
		ArrayList<PlotCurveCharacterstics> normChars = new ArrayList<PlotCurveCharacterstics>();
		normChars.addAll(energyChars);
		normChars.add(perturbChar);
		
		// normalized plot
		getNormalized(prefix, energyVsIters, perturbsVsIters, energies,
				perturbs, iters, iterationsLabel, gp, normChars,
				0, "_normalized.png", energyAter5percent);
		
		// zoomed normalized plots
		int middle = (iters.size()-1)/2;
		getNormalized(prefix, energyVsIters, perturbsVsIters, energies,
				perturbs, iters, iterationsLabel, gp, normChars,
				middle, "_normalized_zoomed_50.png", energyAter5percent);

		int end = (int)((iters.size()-1) * 0.75d+0.5);
		getNormalized(prefix, energyVsIters, perturbsVsIters, energies,
				perturbs, iters, iterationsLabel, gp, normChars,
				end, "_normalized_zoomed_75.png", energyAter5percent);
	}
	
	public static ArrayList<PlotCurveCharacterstics> getEnergyBreakdownChars() {
		ArrayList<PlotCurveCharacterstics> energyChars = new ArrayList<PlotCurveCharacterstics>();
		energyChars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 3f, Color.BLACK));
		energyChars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, Color.RED));
		energyChars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, Color.GREEN));
		energyChars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, Color.BLUE));
		return energyChars;
	}

	private void getNormalized(File prefix,
			ArbitrarilyDiscretizedFunc[] energyVsIters,
			ArbitrarilyDiscretizedFunc perturbsVsIters,
			ArrayList<double[]> energies, ArrayList<Long> perturbs,
			ArrayList<Long> iters, String iterationsLabel,
			HeadlessGraphPanel gp,
			ArrayList<PlotCurveCharacterstics> normChars, int startPoint, String suffix,
			double maxEnergy)
			throws IOException {
		ArbitrarilyDiscretizedFunc normPerturbs;
		ArrayList<ArbitrarilyDiscretizedFunc> normalizedFuncs = new ArrayList<ArbitrarilyDiscretizedFunc>();
		for (int i=0; i<energyVsIters.length; i++) {
			ArbitrarilyDiscretizedFunc norm = new ArbitrarilyDiscretizedFunc();
			ArbitrarilyDiscretizedFunc energyFunc = energyVsIters[i];
			for (int j=startPoint; j<energyFunc.getNum(); j++) {
				double normalized = energyFunc.getY(j) / maxEnergy;
				norm.set(energyFunc.getX(j), normalized);
			}
			normalizedFuncs.add(norm);
		}
		double minPerturbs = (double)perturbs.get(startPoint);
		double maxPerturbs = (double)perturbs.get(perturbs.size()-1);
		maxPerturbs -= minPerturbs;
		normPerturbs = new ArbitrarilyDiscretizedFunc();
		for (int i=startPoint; i<perturbsVsIters.getNum(); i++) {
			double normalized = (perturbsVsIters.getY(i) - minPerturbs) / maxPerturbs;
			normPerturbs.set(perturbsVsIters.getX(i), normalized);
		}
		normalizedFuncs.add(normPerturbs);
		String title = "Normalized";
		if (startPoint > 0) {
			long startIter = iters.get(startPoint);
			long endIter = iters.get(iters.size()-1);
			title += " (Iterations "+startIter+" => "+endIter+")";
		}
		double minX = normalizedFuncs.get(0).getMinX();
		double maxX = normalizedFuncs.get(0).getMaxX()*1.05;
		double minY = 0;
		double maxY = 1.15;
		gp.setUserBounds(minX, maxX, minY, maxY);
		gp.drawGraphPanel(iterationsLabel, "Normalized", normalizedFuncs, normChars, true,
				title);
		gp.saveAsPNG(new File(prefix.getParentFile(),
				prefix.getName()+suffix).getAbsolutePath(),
				plot_width, plot_height);
	}
	
	public void writePlots(CompletionCriteria criteria, File prefix) throws IOException {
		writeRateVsRankPlot(prefix);
		if (criteria instanceof ProgressTrackingCompletionCriteria)
			writeProgressPlots((ProgressTrackingCompletionCriteria)criteria, prefix);
	}
	
	public String getMetadata(String[] args, CompletionCriteria criteria) {
		StringBuilder builder = new StringBuilder();
		builder.append("Distributed Simulated Annealing run completed on "
				+new SimpleDateFormat().format(new Date())+"\n");
		builder.append(""+"\n");
		String argsStr = "";
		for (String arg : args)
			argsStr += " "+arg;
		builder.append("Arguments:"+argsStr+"\n");
		builder.append("Completion Criteria: "+criteria+"\n");
		builder.append("Threads per node: "+getNumThreads()+"\n");
		builder.append(""+"\n");
		builder.append("Solution size: "+getBestSolution().length+"\n");
		double[] e = getBestEnergy();
		builder.append("Best energy: "+Doubles.join(", ", e)+"\n");
		if (rangeNames != null) {
			builder.append("Energy type breakdown\n");
			for (int i=4; i<e.length && (i-4)<rangeNames.size(); i++) {
				builder.append("\t"+rangeNames.get(i-4)+" energy: "+e[i]+"\n");
			}
		}
		if (criteria instanceof ProgressTrackingCompletionCriteria) {
			ProgressTrackingCompletionCriteria track = (ProgressTrackingCompletionCriteria)criteria;
			int numSteps = track.getTimes().size();
			long millis = track.getTimes().get(numSteps-1);
			double totMins = millis / 1000d / 60d;
			builder.append("Total time: "+totMins+" mins\n");
			long iters = track.getIterations().get(numSteps-1);
			long perturbs = track.getPerturbs().get(numSteps-1);
			builder.append("Total iterations: "+iters+"\n");
			float pertPercent = (float)(((double)perturbs / (double)iters) * 100d);
			builder.append("Total perturbations: "+perturbs+" ("+pertPercent+" %)\n");
		}
		
		return builder.toString();
	}
	
	public void writeMetadata(File file, String[] args, CompletionCriteria criteria) throws IOException {
		FileWriter fw = new FileWriter(file);
		
		fw.write(getMetadata(args, criteria).toString());
		
		fw.close();
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
			
			tsa.writeBestSolution(outputFile);
			File prefix = getFileWithoutBinSuffix(outputFile.getAbsolutePath());
			tsa.writeMetadata(new File(prefix.getParentFile(), prefix.getName()+"_metadata.txt"),
					args, criteria);
			tsa.writePlots(criteria, prefix);
			
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
