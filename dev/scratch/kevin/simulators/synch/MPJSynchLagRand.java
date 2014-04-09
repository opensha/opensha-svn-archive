package scratch.kevin.simulators.synch;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.List;

import mpi.MPI;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.opensha.commons.hpc.mpj.taskDispatch.MPJTaskCalculator;
import org.opensha.commons.util.ClassUtils;
import org.opensha.commons.util.threads.Task;
import org.opensha.commons.util.threads.ThreadedTaskComputer;
import org.opensha.sha.simulators.eqsim_v04.EQSIM_Event;
import org.opensha.sha.simulators.eqsim_v04.iden.ElementMagRangeDescription;
import org.opensha.sha.simulators.eqsim_v04.iden.RuptureIdentifier;

import scratch.kevin.simulators.SimAnalysisCatLoader;
import scratch.kevin.simulators.dists.RandomDistType;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

public class MPJSynchLagRand extends MPJTaskCalculator {
	
	private List<double[][][]> gBarsList;
	
	private int numTrials;
	private List<EQSIM_Event> events;
	private List<RuptureIdentifier> rupIdens;
	
	private RandomDistType dist = RandomDistType.ACTUAL;
	private double distSpacing = 10d;
	
	private int nDims;
	
	private static int[] lags = SynchParamCalculator.rangeInclusive(-20, 20);
	
	private File outputDir;
	
	private MarkovChainBuilder origChain;

	public MPJSynchLagRand(CommandLine cmd, File outputDir) throws IOException {
		super(cmd);
		
		this.outputDir = outputDir;
		
		Preconditions.checkArgument(cmd.hasOption("trials"));
		numTrials = Integer.parseInt(cmd.getOptionValue("trials"));
		
		gBarsList = Lists.newArrayList();
		
		double minMag = 7;
		double maxMag = 10d;
		
		int[] include_elems = {
				ElementMagRangeDescription.SAF_CHOLAME_ELEMENT_ID,
				ElementMagRangeDescription.SAF_CARRIZO_ELEMENT_ID,
				ElementMagRangeDescription.GARLOCK_WEST_ELEMENT_ID,
				ElementMagRangeDescription.SAF_MOJAVE_ELEMENT_ID,
				ElementMagRangeDescription.SAF_COACHELLA_ELEMENT_ID,
				ElementMagRangeDescription.SAN_JACINTO__ELEMENT_ID
				};
		
		rupIdens = Lists.newArrayList();
		List<Color> colors = Lists.newArrayList();
		
		SimAnalysisCatLoader.loadElemMagIdens(include_elems, rupIdens, colors, minMag, maxMag);
		
		int[] all_elems = {
				ElementMagRangeDescription.SAF_CHOLAME_ELEMENT_ID,
				ElementMagRangeDescription.SAF_CARRIZO_ELEMENT_ID,
				ElementMagRangeDescription.GARLOCK_WEST_ELEMENT_ID,
				ElementMagRangeDescription.SAF_MOJAVE_ELEMENT_ID,
				ElementMagRangeDescription.SAF_COACHELLA_ELEMENT_ID,
				ElementMagRangeDescription.SAN_JACINTO__ELEMENT_ID
				};
		List<RuptureIdentifier> allIdens = Lists.newArrayList();
		SimAnalysisCatLoader.loadElemMagIdens(all_elems, allIdens, null, minMag, maxMag);
		
		events = new SimAnalysisCatLoader(true, allIdens).getEvents();
		
		nDims = rupIdens.size();
	}

	@Override
	protected int getNumTasks() {
		return numTrials;
	}

	@Override
	protected void calculateBatch(int[] batch) throws Exception {
		checkBuildOrigChain();
		
		for (int item : batch) {
			double[][][] gBars = new double[nDims][nDims][lags.length];
			MarkovChainBuilder chain = SynchParamCalculator.createRandomizedChain(events, rupIdens, dist, distSpacing);
			
			List<SynchCalc> tasks = Lists.newArrayList();
			
			for (int m=0; m<nDims; m++)
				for (int n=m; n<nDims; n++)
					for (int l=0; l<lags.length; l++)
						tasks.add(new SynchCalc(chain, m, n, l, gBars));
			
			ThreadedTaskComputer comp = new ThreadedTaskComputer(tasks);
			comp.computeThreaded(getNumThreads());
			
			gBarsList.add(gBars);
		}
	}
	
	private void checkBuildOrigChain() {
		if (rank == 0 && origChain == null) {
			List<List<EQSIM_Event>> matchesLists = Lists.newArrayList();
			for (int i=0; i<rupIdens.size(); i++)
				matchesLists.add(rupIdens.get(i).getMatches(events));
			origChain = new MarkovChainBuilder(distSpacing, events, matchesLists);
		}
	}
	
	private static class SynchCalc implements Task {
		
		private MarkovChainBuilder chain;
		private int m, n, l;
		private double[][][] gBars;

		public SynchCalc(MarkovChainBuilder chain, int m, int n, int l, double[][][] gBars) {
			super();
			this.chain = chain;
			this.m = m;
			this.n = n;
			this.l = l;
			this.gBars = gBars;
		}

		@Override
		public void compute() {
			int lag = lags[l];
			double gBar = new SynchParamCalculator(chain, m, n, lag).getGBar();
			
			gBars[m][n][l] = gBar;
			gBars[n][m][l] = gBar;
		}
		
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	protected void doFinalAssembly() throws Exception {
		List[] sendbuf = new List[] {gBarsList};
		List[] recvbuf = null;
		
		if (rank == 0)
			recvbuf = new List[size];
		
		MPI.COMM_WORLD.Gather(sendbuf, 0, 1, MPI.OBJECT, recvbuf, 0, 1, MPI.OBJECT, 0);
		
		if (rank == 0) {
			for (int i=1; i<size; i++) {
				List<double[][][]> o = recvbuf[i];
				gBarsList.addAll(o);
			}
			
			Preconditions.checkState(numTrials == gBarsList.size());
			
			double[][][][] gBars = new double[nDims][nDims][numTrials][lags.length];
			
			for (int t=0; t<numTrials; t++) {
				double[][][] trialGBars = gBarsList.get(t);
				for (int m=0; m<nDims; m++)
					for (int n=0; n<nDims; n++)
						for (int l=0; l<lags.length; l++)
							gBars[m][n][t][l] = trialGBars[m][n][l];
			}
			
			String indepStr;
			if (SynchParamCalculator.useIndepProbs)
				indepStr = "indep";
			else
				indepStr = "dep";
			
			File writeDir = new File(outputDir, "weight_"+SynchParamCalculator.weightingScheme.name()+"_"+indepStr);
			if (!writeDir.exists())
				writeDir.mkdir();
			
			checkBuildOrigChain();
			SynchParamCalculator.doWriteSynchStdDevParams(writeDir, rupIdens, origChain, lags, numTrials, distSpacing, nDims, gBars);
		}
	}
	
	public static Options createOptions() {
		Options ops = MPJTaskCalculator.createOptions();
		
		Option trialsOption = new Option("trials", "num-trials", true,
				"Number of random trials");
		trialsOption.setRequired(true);
		ops.addOption(trialsOption);
		
		
		return ops;
	}
	
	public static void main(String[] args) {
		args = MPJTaskCalculator.initMPJ(args);
		
		try {
			Options options = createOptions();
			
			CommandLine cmd = parse(options, args, MPJSynchLagRand.class);
			
			args = cmd.getArgs();
			
			if (args.length != 1) {
				System.err.println("USAGE: "+ClassUtils.getClassNameWithoutPackage(MPJSynchLagRand.class)
						+" [options] <output-dir>");
				abortAndExit(2);
			}
			
			File outputDir = new File(args[0]);
			if (!outputDir.exists() && MPI.COMM_WORLD.Rank() == 0)
				Preconditions.checkState(outputDir.mkdir());
			
			MPJSynchLagRand calc = new MPJSynchLagRand(cmd, outputDir);
			calc.run();
			
			finalizeMPJ();
			
			System.exit(0);
		} catch (Throwable t) {
			abortAndExit(t);
		}
	}

}